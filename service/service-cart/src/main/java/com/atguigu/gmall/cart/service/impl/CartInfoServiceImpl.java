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
import java.util.stream.Collectors;

/**
 * @author Yuehong Zhang
 */
@Service
public class CartInfoServiceImpl implements CartInfoService {

    // inject mapper
    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Autowired
    private CartAsyncService cartAsyncService;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    //@Transactional(rollbackFor = Exception.class)
    public void addToCart(Long skuId, String userId, Integer skuNum) {

        // Before adding data:
        // Check if there is a shopping cart key in the cache! If there is no such key, it means that the cache has expired and is loaded into the cache! If so, it means that the cached data is complete!

        // addToCartOne(skuId, userId, skuNum);
        // Get the shopping cart key
        String cartKey = this.getCartKey(userId);
        // The key of the shopping cart does not exist in the cache!
        if (!redisTemplate.hasKey(cartKey)){
            // Load the data in the database to the cache!
            this.loadCartCache(userId);
        }
        CartInfo cartInfoExist = null;
        try {
            // The code is here, indicating that there must be data in the cache! hget key field
            cartInfoExist = (CartInfo) redisTemplate.boundHashOps(cartKey).get(skuId.toString());
            // cartInfoExist = (CartInfo) redisTemplate.opsForHash().get(cartKey,skuId.toString());

            // select * from cart_info where sku_id = skuId and user_id =?
            // QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
            // queryWrapper.eq("user_id",userId);
            // queryWrapper.eq("sku_id",skuId);
            // cartInfoExist = cartInfoMapper.selectOne(queryWrapper);

            if (cartInfoExist !=null){
                // Add the quantity
                cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
                // Real-time price of assigned goods
                cartInfoExist.setSkuPrice(productFeignClient.getSkuPrice(skuId));
                // Modify the time from the new setting
                cartInfoExist.setUpdateTime(new Timestamp(new Date().getTime()));
                // select again
                cartInfoExist.setIsChecked(1);
                // Execute update statement
                // cartInfoMapper.updateById(cartInfoExist);
                cartAsyncService.updateCartInfo(cartInfoExist);
                // When adding, directly add the data to the cache!
                // redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfoExist);
            }else {
                // insert
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

                // int i = 1/0;
                // cartInfoMapper.insert(cartInfo)
                cartAsyncService.saveCartInfo(cartInfo);// The asynchronous operation has not executed insert yet, so there is no Id
                // If an error occurs when executing insert into, redis will continue to execute!
                cartInfoExist = cartInfo;
                // When adding, directly add the data to the cache!
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // Add the shopping cart to the cache!
        redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfoExist);
        // Shopping cart: added to mysql, redis!
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
            1. Query the cache first
            2. Determine the query result
                true: return
                false: Query the database and put the data in the cache!
         */
        // Query the shopping cart when logging in
        if (!StringUtils.isEmpty(userId)){
            // It is possible to merge shopping carts:
            List<CartInfo> cartList = new ArrayList<>();
            // Get temporary shopping cart data: ""
            if (StringUtils.isEmpty(userTempId)){
                //  stopped!
                cartInfoList=this.getCartList(userId);
                return cartInfoList;
            }else {
                cartList = this.getCartList(userTempId);
            }
            // Determine the collection data obtained by the temporary shopping cart
            if(!CollectionUtils.isEmpty(cartList)){
                // It means that the shopping cart data is valuable if not logged in! A merge operation needs to occur!
                cartInfoList = this.mergeToCartList(cartList,userId);
                // Delete temporary shopping cart data:
                this.deleteCartList(userTempId);
            }else {
                // Only query the login shopping cart data!
                cartInfoList=this.getCartList(userId);
            }
        }
        // Query temporary shopping cart data:
        if (StringUtils.isEmpty(userId)){
            cartInfoList=this.getCartList(userTempId);
        }
        return cartInfoList;
    }

    @Override
    public void checkCart(String userId, Integer isChecked, Long skuId) {
        // Operation database
        cartAsyncService.checkCart(userId,isChecked,skuId);
        // Sync redis! Get the shopping cart key
        String cartKey = this.getCartKey(userId);
        // Get the modified cartInfo hget key field field = skuId
        CartInfo cartInfo = (CartInfo) redisTemplate.opsForHash().get(cartKey, skuId.toString());
        if (cartInfo!=null){
            // change status
            cartInfo.setIsChecked(isChecked);
            // Put the changed data back into the cache! hset key field value;
            redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfo);

            // You can consider whether you need to re-set the expiration time!
            this.setCartKeyExpire(cartKey);
        }
        // Determine whether there are current products in the current shopping cart
        // Boolean flag = redisTemplate.boundHashOps(cartKey).hasKey(skuId.toString());
        // if (flag){
        // // have data
        // CartInfo cartInfo = (CartInfo) redisTemplate.boundHashOps(cartKey).get(skuId.toString());
        // // change status
        // cartInfo.setIsChecked(isChecked);
        // // Put the changed data back into the cache! hset key field value;
        // redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfo);
        //
        // // You can consider whether you need to re-set the expiration time!
        // this.setCartKeyExpire(cartKey);
        //
        //}
    }

    @Override
    public void deleteCart(Long skuId, String userId) {
        // Delete the database
        cartAsyncService.deleteCartInfo(userId,skuId);
        // delete cache
        // Get key
        String cartKey = this.getCartKey(userId);
        //  delete!
        // redisTemplate.delete(cartKey);
        Boolean result = redisTemplate.boundHashOps(cartKey).hasKey(skuId.toString());
        if (result){
            // Delete an item in the shopping cart
            redisTemplate.boundHashOps(cartKey).delete(skuId.toString());
        }
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        // Data from the cache!
        String cartKey = this.getCartKey(userId);
        // Get shopping cart data interface
        List<CartInfo> cartInfoList = redisTemplate.boundHashOps(cartKey).values();
        // List values1 = redisTemplate.opsForHash().values(cartKey);
        // Judge selected
        // cartInfoList.stream().filter(new Predicate<CartInfo>() {
        // @Override
        // public boolean test(CartInfo cartInfo) {
        // return cartInfo.getIsChecked().intValue()==1;
        //}
        // });
        List<CartInfo> cartInfos = cartInfoList.stream().filter((cartInfo) -> {
            return cartInfo.getIsChecked().intValue() == 1;
        }).collect(Collectors.toList());
        // Declare a collection List<CartInfo>
        // List<CartInfo> cartInfos = new ArrayList<>();
        // for (CartInfo cartInfo: cartInfoList) {
        // if (cartInfo.getIsChecked().intValue()==1){
        // cartInfos.add(cartInfo);
        //}
        //}
        // return cartInfos;
        return cartInfos;
    }

    /**
     * Delete temporary shopping cart
     * @param userTempId
     */
    private void deleteCartList(String userTempId) {
        // Delete the database, redis
        // delete from cart_info where user_id = userTempId
        // QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        // queryWrapper.eq("user_id",userTempId);
        // cartInfoMapper.delete(queryWrapper);
        // CartInfo cartInfo = new CartInfo();
        // cartInfo.setUserId(userTempId);
        // cartAsyncService.deleteCartInfo(cartInfo);
        cartAsyncService.deleteCartInfo(userTempId);
        // redis.del(key);
        String cartKey = this.getCartKey(userTempId);
        if (redisTemplate.hasKey(cartKey)){
            redisTemplate.delete(cartKey);
        }
    }

    /**
     * Combine shopping cart
     * @param cartInfoNoLoginList
     * @param userId
     * @return
     */
    private List<CartInfo> mergeToCartList(List<CartInfo> cartInfoNoLoginList, String userId) {
        /*
        1. Get the shopping cart collection data of the logged-in user according to userId
        2. Do merge processing? Processing conditions: skuId is the same and the quantity is added;
        demo1:
        Log in:
            37 1
            38 1
        Not logged in:
            37 1
            38 1
            39 1
        The combined data
            37 2
            38 2
            39 1

            The first item: do the same update
            The second piece: there is no same insert
            One last thing: query and return the final combined result!
         */
        List<CartInfo> cartInfoLoginList = this.getCartList(userId);
        // The first solution: double for traversal: according to whether the skuId is the same!
        // The second solution: use map to do the inclusion of cartInfoLoginList into a map collection key = skuId ,value = CartInfo
        Map<Long, CartInfo> longCartInfoMap = cartInfoLoginList.stream().collect(Collectors.toMap(CartInfo::getSkuId, cartInfo -> {
            return cartInfo;
        }));
        // Map<Long, CartInfo> longCartInfoMap = cartInfoLoginList.stream().collect(Collectors.toMap(CartInfo::getSkuId, cartInfo -> cartInfo));
        // Traverse the temporary shopping cart data
        for (CartInfo cartInfo: cartInfoNoLoginList) {
            // The skuId of the shopping item not logged in the shopping cart
            Long skuId = cartInfo.getSkuId();
            if (longCartInfoMap.containsKey(skuId)){
                // Add the number 37, 38 Add the number
                CartInfo cartLonginInfo = longCartInfoMap.get(skuId);
                cartLonginInfo.setSkuNum(cartLonginInfo.getSkuNum()+cartInfo.getSkuNum());
                cartLonginInfo.setUpdateTime(new Timestamp(new Date().getTime()));
                // Checked state ischecked = 1: I want to buy more!
                if (cartInfo.getIsChecked().intValue()==1){
                    cartLonginInfo.setIsChecked(1);
                }
                // Update the database: Id may be empty!
                // cartInfoMapper.updateById(cartLonginInfo);
                // userId skuId; Asynchronous operation!
                // cartAsyncService.updateCartInfo(cartLonginInfo); // execute later! It is recommended to synchronize!
                UpdateWrapper<CartInfo> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("user_id",cartLonginInfo.getUserId());
                updateWrapper.eq("sku_id",cartLonginInfo.getSkuId());

                cartInfoMapper.update(cartLonginInfo,updateWrapper);

            }else{
                // 39 1 Insert data operation!
                // But the userId of the temporary shopping cart data is similar to a uuid!
                cartInfo.setUserId(userId);
                // cartInfoMapper.insert(cartInfo);
                cartInfo.setCreateTime(new Timestamp(new Date().getTime()));
                cartInfo.setUpdateTime(new Timestamp(new Date().getTime()));
                // cartAsyncService.saveCartInfo(cartInfo); // execute later! It is recommended to synchronize! It is recommended that redis has no expiration time!
                cartInfoMapper.insert(cartInfo);
            }
        }

        // In the end it will return 37, 38, 39:
        List<CartInfo> cartInfoList = loadCartCache(userId); // All queries are cached!
        return cartInfoList;
    }

    // Query the shopping cart list
    private List<CartInfo> getCartList(String userId) {
        // Get the key of the shopping cart
        String cartKey = this.getCartKey(userId);
        // Use hash to get data hvals user:1:cart
        List<CartInfo> cartInfoList = redisTemplate.opsForHash().values(cartKey);
        // Determine whether the current collection is empty
        if (CollectionUtils.isEmpty(cartInfoList)){
            // There is no data in the cache! Get it from the database and put it in the cache!
            cartInfoList = this.loadCartCache(userId);
            // return data
            // return cartInfoList;
        }
        // Sort!
        cartInfoList.sort(new Comparator<CartInfo>() {
            @Override
            public int compare(CartInfo o1, CartInfo o2) {
                // Sort by modification time
                // o2.getUpdateTime()-o1.getUpdateTime();
                return DateUtil.truncatedCompareTo(o2.getUpdateTime(),o1.getUpdateTime(), Calendar.SECOND);
            }
        });
        return cartInfoList;
    }

    /**
     * Query the data according to the user Id and put it into the cache!
     * @param userId
     * @return
     */
    public List<CartInfo> loadCartCache(String userId) {
        // When the shopping cart data in the cache is empty, the current shopping cart price may change! So check real-time prices!
        // List<CartInfo> = select * from cart_info where user_id = 2;
        QueryWrapper queryWrapper = new QueryWrapper<CartInfo>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("update_time");
        // You can use order by to sort!

        List<CartInfo> cartInfoList = cartInfoMapper.selectList(queryWrapper);
        // If it does not exist in the database, return directly
        if (CollectionUtils.isEmpty(cartInfoList)){
            return new ArrayList<CartInfo>();
        }
        // Get the current shopping cart key
        String cartKey = this.getCartKey(userId);
        HashMap<String, CartInfo> map = new HashMap<>();
        // Loop through the current shopping cart collection, put it in the cache, and assign skuPrice to it at the same time!
        for (CartInfo cartInfo: cartInfoList) {
            // To check the real-time price
            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));
            // redisTemplate.opsForHash().put(cartKey,cartInfo.getSkuId().toString(),cartInfo);
            map.put(cartInfo.getSkuId().toString(),cartInfo);
        }
        // hmset writes multiple data at once
        redisTemplate.opsForHash().putAll(cartKey,map);
        // Set an expiration time
        this.setCartKeyExpire(cartKey);
        //  return
        return cartInfoList;
    }

    private void addToCartOne(Long skuId, String userId, Integer skuNum) {
        String cartKey = null;
        CartInfo cartInfoExist = null;
        int flag = 0;
        try {
            // Get the shopping cart key
            cartKey = this.getCartKey(userId);
            // select * from cart_info where sku_id = skuId and user_id =?
            QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id",userId);
            queryWrapper.eq("sku_id",skuId);
            cartInfoExist = cartInfoMapper.selectOne(queryWrapper);
            if (cartInfoExist !=null){
                // Add the quantity
                cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
                // Real-time price of assigned goods
                cartInfoExist.setSkuPrice(productFeignClient.getSkuPrice(skuId));
                // Modify the time from the new setting
                cartInfoExist.setUpdateTime(new Timestamp(new Date().getTime()));
                // Execute update statement
                // cartInfoMapper.updateById(cartInfoExist);
                cartAsyncService.updateCartInfo(cartInfoExist);

                // When adding, directly add the data to the cache!
                // redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfoExist);
            }else {
                // insert
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

                // int i = 1/0;
                // cartInfoMapper.insert(cartInfo)
                cartAsyncService.saveCartInfo(cartInfo);// The asynchronous operation has not executed insert yet, so there is no Id
                // If an error occurs when executing insert into, redis will continue to execute!
                cartInfoExist = cartInfo;
                // When adding, directly add the data to the cache!
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (cartInfoExist!=null && flag!=-1){
            // Add the shopping cart to the cache!
            redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfoExist);
            // Shopping cart: added to mysql, redis!
            this.setCartKeyExpire(cartKey);
        }
    }

    // Get the shopping cart key
    private String getCartKey(String userId) {
        // Define a key, data type! Hash! hset key field vlaue; hget key field redisTemplate
        // cartKey=user:userId:cart Check whose shopping cart!
        // field=skuId
        // value=cartInfo
        return RedisConst.USER_KEY_PREFIX+userId+RedisConst.USER_CART_KEY_SUFFIX;
    }

    // Set the expiration time of the shopping cart
    private void setCartKeyExpire(String cartKey) {
        // Set an expiration time for the current shopping cart
        redisTemplate.expire(cartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }
}
