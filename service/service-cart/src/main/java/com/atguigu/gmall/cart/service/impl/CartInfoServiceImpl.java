package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartAsyncService;
import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author mqx
 */
@Service
public class CartInfoServiceImpl implements CartInfoService {

    //  注入mapper
    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Autowired
    private CartAsyncService cartAsyncService;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addToCart(Long skuId, String userId, Integer skuNum) {

        //  添加数据之前：
        //  查询一下缓存中是否有购物车的key！ 如果没有这个key，则说明缓存过期，加载到缓存！ 如果有，说明这个缓存的数据想全的！

        //  addToCartOne(skuId, userId, skuNum);
        //  获取到购物车key
        String  cartKey = this.getCartKey(userId);
        //  购物车的key 在缓存中不存在！
        if (!redisTemplate.hasKey(cartKey)){
            //  加载数据库中的数据到缓存！
            this.loadCartCache(userId);
        }
        CartInfo cartInfoExist = null;
        try {
            //  代码走到这，说明缓存一定有数据了！ hget key field
            cartInfoExist = (CartInfo) redisTemplate.boundHashOps(cartKey).get(skuId.toString());
            //  cartInfoExist = (CartInfo)  redisTemplate.opsForHash().get(cartKey,skuId.toString());

            //  select * from cart_info where sku_id = skuId and user_id = ?
            //            QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
            //            queryWrapper.eq("user_id",userId);
            //            queryWrapper.eq("sku_id",skuId);
            //            cartInfoExist = cartInfoMapper.selectOne(queryWrapper);

            if (cartInfoExist !=null){
                //  数量相加
                cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
                //  赋值商品的实时价格
                cartInfoExist.setSkuPrice(productFeignClient.getSkuPrice(skuId));
                //  从新设置修改时间
                cartInfoExist.setUpdateTime(new Timestamp(new Date().getTime()));
                //  再次选中
                cartInfoExist.setIsChecked(1);
                //  执行更新语句
                //  cartInfoMapper.updateById(cartInfoExist);
                cartAsyncService.updateCartInfo(cartInfoExist);
                //  添加的时候，直接将数据添加到缓存！
                //  redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfoExist);
            }else {
                //  insert
                SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

                CartInfo cartInfo = new CartInfo();
                cartInfo.setUserId(userId);
                cartInfo.setSkuId(skuId);
                cartInfo.setCartPrice(skuInfo.getPrice());
                cartInfo.setSkuPrice(skuInfo.getPrice());
                cartInfo.setSkuNum(skuNum);
                cartInfo.setSkuName(skuInfo.getSkuName());
                cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
                cartInfo.setCreateTime(new Timestamp(new Date().getTime()));
                cartInfo.setUpdateTime(new Timestamp(new Date().getTime()));

                //  int i = 1/0;
                //  cartInfoMapper.insert(cartInfo)
                cartAsyncService.saveCartInfo(cartInfo);// 异步操作此时还没有执行insert，所以没有Id
                //  执行insert into 的时候出错，则redis 会继续执行！
                cartInfoExist = cartInfo;
                //  添加的时候，直接将数据添加到缓存！
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //  添加购物车到缓存！
        redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfoExist);
        //  购物车：添加到了mysql ，redis!
        this.setCartKeyExpire(cartKey);
    }

    /**
     *
     * @param userId
     * @param userTempId
     * @return
     */
    @Override
    public List<CartInfo> getCartList(String userId, String userTempId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        /*
            1.  先查询缓存
            2.  判断查询结果
                true:   返回
                false:  查询数据库并将数据放入缓存！
         */
        //  查询登录时的购物车
        if (!StringUtils.isEmpty(userId)){
            //  有可能会发生合并购物车：
            List<CartInfo> cartList = new ArrayList<>();
            //  获取临时购物车数据：""
            if (StringUtils.isEmpty(userTempId)){
                //  停止了！
                cartInfoList=this.getCartList(userId);
                return cartInfoList;
            }else {
                cartList = this.getCartList(userTempId);
            }
            //  判断临时购物车获取到的集合数据
            if(!CollectionUtils.isEmpty(cartList)){
                //  说明未登录购物车数据有值！需要发生合并操作！
                cartInfoList = this.mergeToCartList(cartList,userId);
                //  删除临时购物车数据：
                this.deleteCartList(userTempId);
            }else {
                //  只查询登录购物车数据！
                cartInfoList=this.getCartList(userId);
            }
        }
        //  查询临时购物车数据：
        if (StringUtils.isEmpty(userId)){
            cartInfoList=this.getCartList(userTempId);
        }
        return cartInfoList;
    }

    @Override
    public void checkCart(String userId, Integer isChecked, Long skuId) {
        //  操作数据库
        cartAsyncService.checkCart(userId,isChecked,skuId);
        //  同步redis！获取购物车key
        String cartKey = this.getCartKey(userId);
        //  获取修改的cartInfo   hget key field  field = skuId
        CartInfo cartInfo = (CartInfo) redisTemplate.opsForHash().get(cartKey, skuId.toString());
        if (cartInfo!=null){
            //  变更状态
            cartInfo.setIsChecked(isChecked);
            //  将变更之后的数据放回缓存！ hset key field value;
            redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfo);

            //  可以考虑是否需要从新设置过期时间！
            this.setCartKeyExpire(cartKey);
        }
        //  判断当前购物车中是否有当前商品
        //        Boolean flag = redisTemplate.boundHashOps(cartKey).hasKey(skuId.toString());
        //        if (flag){
        //            //  有数据
        //            CartInfo cartInfo = (CartInfo) redisTemplate.boundHashOps(cartKey).get(skuId.toString());
        //            //  变更状态
        //            cartInfo.setIsChecked(isChecked);
        //            //  将变更之后的数据放回缓存！ hset key field value;
        //            redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfo);
        //
        //            //  可以考虑是否需要从新设置过期时间！
        //            this.setCartKeyExpire(cartKey);
        //
        //        }

    }

    @Override
    public void deleteCart(Long skuId, String userId) {
        //  删除数据库
        cartAsyncService.deleteCartInfo(userId,skuId);
        //  删除缓存
        //  获取key
        String cartKey = this.getCartKey(userId);
        //  删除！
        //   redisTemplate.delete(cartKey);
        Boolean result = redisTemplate.boundHashOps(cartKey).hasKey(skuId.toString());
        if (result){
            //  删除购物车的某一项
            redisTemplate.boundHashOps(cartKey).delete(skuId.toString());
        }
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        //  数据从缓存！
        String cartKey = this.getCartKey(userId);
        //  获取购物车数据接口
        List<CartInfo> cartInfoList = redisTemplate.boundHashOps(cartKey).values();
        //  List values1 = redisTemplate.opsForHash().values(cartKey);
        //  判断选中
        //        cartInfoList.stream().filter(new Predicate<CartInfo>() {
        //            @Override
        //            public boolean test(CartInfo cartInfo) {
        //                return cartInfo.getIsChecked().intValue()==1;
        //            }
        //        });
        cartInfoList.stream().filter((cartInfo)->{
            return cartInfo.getIsChecked().intValue()==1;
        });
        //  声明一个集合 List<CartInfo>
        //        List<CartInfo> cartInfos = new ArrayList<>();
        //        for (CartInfo cartInfo : cartInfoList) {
        //            if (cartInfo.getIsChecked().intValue()==1){
        //                cartInfos.add(cartInfo);
        //            }
        //        }
        //
        //        return cartInfos;
        return cartInfoList;
    }

    /**
     * 删除临时购物车
     * @param userTempId
     */
    private void deleteCartList(String userTempId) {
        // 删除数据库，redis
        //  delete from cart_info where user_id = userTempId
        //        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        //        queryWrapper.eq("user_id",userTempId);
        //        cartInfoMapper.delete(queryWrapper);
        //        CartInfo cartInfo = new CartInfo();
        //        cartInfo.setUserId(userTempId);
        //        cartAsyncService.deleteCartInfo(cartInfo);
        cartAsyncService.deleteCartInfo(userTempId);
        //  redis.del(key);
        String cartKey = this.getCartKey(userTempId);
        if (redisTemplate.hasKey(cartKey)){
            redisTemplate.delete(cartKey);
        }
    }

    /**
     * 合并购物车
     * @param cartInfoNoLoginList
     * @param userId
     * @return
     */
    private List<CartInfo> mergeToCartList(List<CartInfo> cartInfoNoLoginList, String userId) {
        /*
        1.  根据userId 获取到登录用户的购物车集合数据
        2.  做合并处理? 处理条件： skuId 相同 数量相加;
        demo1:
        登录：
            37 1
            38 1
        未登录：
            37 1
            38 1
            39 1
        合并之后的数据
            37 2
            38 2
            39 1

            第一件：相同的做update
            第二件：没有相同insert
            最后一件事：将最终的合并结果查询并返回！
         */
        List<CartInfo> cartInfoLoginList = this.getCartList(userId);
        //  第一种方案：双重for 遍历：根据skuId 是否相同！
        //  第二种方案：使用map做包含 cartInfoLoginList 变成map 集合 key = skuId ,value = CartInfo
        Map<Long, CartInfo> longCartInfoMap = cartInfoLoginList.stream().collect(Collectors.toMap(CartInfo::getSkuId, cartInfo -> {
            return cartInfo;
        }));
        //  Map<Long, CartInfo> longCartInfoMap = cartInfoLoginList.stream().collect(Collectors.toMap(CartInfo::getSkuId, cartInfo -> cartInfo));
        //  遍历临时的购物车数据
        for (CartInfo cartInfo : cartInfoNoLoginList) {
            //  未登录购物车购物项的skuId
            Long skuId = cartInfo.getSkuId();
            if (longCartInfoMap.containsKey(skuId)){
                //  数量相加 37,38 数量相加
                CartInfo cartLonginInfo = longCartInfoMap.get(skuId);
                cartLonginInfo.setSkuNum(cartLonginInfo.getSkuNum()+cartInfo.getSkuNum());
                cartLonginInfo.setUpdateTime(new Timestamp(new Date().getTime()));
                //  选中状态 ischecked = 1： 还想多买点东西！
                if (cartInfo.getIsChecked().intValue()==1){
                    cartLonginInfo.setIsChecked(1);
                }
                //  更新数据库: 有可能会出现Id 为空的情况！
                //  cartInfoMapper.updateById(cartLonginInfo);
                //  userId skuId; 异步操作！
                //  cartAsyncService.updateCartInfo(cartLonginInfo);    // 后执行！ 建议同步！
                UpdateWrapper<CartInfo> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("user_id",cartLonginInfo.getUserId());
                updateWrapper.eq("sku_id",cartLonginInfo.getSkuId());

                cartInfoMapper.update(cartLonginInfo,updateWrapper);

            }else {
                //  39 1 插入数据操作！
                //  但是临时购物车数据的userId 是类似于一个uuid 的东西！
                cartInfo.setUserId(userId);
                //  cartInfoMapper.insert(cartInfo);
                cartInfo.setCreateTime(new Timestamp(new Date().getTime()));
                cartInfo.setUpdateTime(new Timestamp(new Date().getTime()));
                // cartAsyncService.saveCartInfo(cartInfo);    //  后执行！    建议同步！ 建议redis 没有过期时间！
                cartInfoMapper.insert(cartInfo);
            }
        }

        //  最终要返回 37,38,39 :
        List<CartInfo> cartInfoList = loadCartCache(userId); // 查询都是走缓存！
        return cartInfoList;
    }

    //  查询购物车列表
    private List<CartInfo> getCartList(String userId) {
        //  获取购物车的key
        String cartKey = this.getCartKey(userId);
        //  使用的hash 获取数据 hvals user:1:cart
        List<CartInfo> cartInfoList = redisTemplate.opsForHash().values(cartKey);
        //  判断当前集合是否为空
        if (CollectionUtils.isEmpty(cartInfoList)){
            //  缓存中没有数据！ 从数据库获取并放入缓存！
            cartInfoList = this.loadCartCache(userId);
            //  返回数据
            //  return cartInfoList;
        }
        //  排序！
        cartInfoList.sort(new Comparator<CartInfo>() {
            @Override
            public int compare(CartInfo o1, CartInfo o2) {
                //  按照修改时间进行排序
                //  o2.getUpdateTime()-o1.getUpdateTime();
                return DateUtil.truncatedCompareTo(o2.getUpdateTime(),o1.getUpdateTime(), Calendar.SECOND);
            }
        });
        return cartInfoList;
    }

    /**
     * 根据用户Id 查询数据并放入缓存！
     * @param userId
     * @return
     */
    private List<CartInfo> loadCartCache(String userId) {
        //  当缓存中的购物车数据为空的时候，当前购物车的价格可能会发生变动！所以要查询一下实时价格！
        // List<CartInfo> = select * from cart_info where user_id = 2;
        QueryWrapper queryWrapper = new QueryWrapper<CartInfo>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("update_time");
        //  可以使用order by 排序！

        List<CartInfo> cartInfoList  = cartInfoMapper.selectList(queryWrapper);
        //  如果数据库中不存在，则直接返回
        if (CollectionUtils.isEmpty(cartInfoList)){
            return new ArrayList<CartInfo>();
        }
        //  获取到当前购物车key
        String cartKey = this.getCartKey(userId);
        HashMap<String, CartInfo> map = new HashMap<>();
        //  循环遍历当前购物车集合，放入缓存，同时将skuPrice 赋值！
        for (CartInfo cartInfo : cartInfoList) {
            //  要查询一下实时价格
            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));
            //  redisTemplate.opsForHash().put(cartKey,cartInfo.getSkuId().toString(),cartInfo);
            map.put(cartInfo.getSkuId().toString(),cartInfo);
        }
        //  hmset 一次写入多条数据
        redisTemplate.opsForHash().putAll(cartKey,map);
        // 设置一个过期时间
        this.setCartKeyExpire(cartKey);
        //  返回
        return cartInfoList;
    }

    private void addToCartOne(Long skuId, String userId, Integer skuNum) {
        String cartKey = null;
        CartInfo cartInfoExist = null;
        int flag = 0;
        try {
            //  获取到购物车key
            cartKey = this.getCartKey(userId);
            //  select * from cart_info where sku_id = skuId and user_id = ?
            QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id",userId);
            queryWrapper.eq("sku_id",skuId);
            cartInfoExist = cartInfoMapper.selectOne(queryWrapper);
            if (cartInfoExist !=null){
                //  数量相加
                cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
                //  赋值商品的实时价格
                cartInfoExist.setSkuPrice(productFeignClient.getSkuPrice(skuId));
                //  从新设置修改时间
                cartInfoExist.setUpdateTime(new Timestamp(new Date().getTime()));
                //  执行更新语句
                //  cartInfoMapper.updateById(cartInfoExist);
                cartAsyncService.updateCartInfo(cartInfoExist);

                //  添加的时候，直接将数据添加到缓存！
                //  redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfoExist);
            }else {
                //  insert
                SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

                CartInfo cartInfo = new CartInfo();
                cartInfo.setUserId(userId);
                cartInfo.setSkuId(skuId);
                cartInfo.setCartPrice(skuInfo.getPrice());
                cartInfo.setSkuPrice(skuInfo.getPrice());
                cartInfo.setSkuNum(skuNum);
                cartInfo.setSkuName(skuInfo.getSkuName());
                cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
                cartInfo.setCreateTime(new Timestamp(new Date().getTime()));
                cartInfo.setUpdateTime(new Timestamp(new Date().getTime()));

                //  int i = 1/0;
                //  cartInfoMapper.insert(cartInfo)
                cartAsyncService.saveCartInfo(cartInfo);// 异步操作此时还没有执行insert，所以没有Id
                //  执行insert into 的时候出错，则redis 会继续执行！
                cartInfoExist = cartInfo;
                //  添加的时候，直接将数据添加到缓存！
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (cartInfoExist!=null && flag!=-1){
            //  添加购物车到缓存！
            redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfoExist);
            //  购物车：添加到了mysql ，redis!
            this.setCartKeyExpire(cartKey);
        }
    }

    //  获取购物车key
    private String getCartKey(String userId) {
        //  定义个key ，数据类型！Hash！hset key field vlaue ; hget key field  redisTemplate
        //  cartKey=user:userId:cart    查看谁的购物车！
        //  field=skuId
        //  value=cartInfo
        return RedisConst.USER_KEY_PREFIX+userId+RedisConst.USER_CART_KEY_SUFFIX;
    }

    //  设置购物车过期时间
    private void setCartKeyExpire(String cartKey) {
        //  给当前购物车设置一个过期时间
        redisTemplate.expire(cartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }
}
