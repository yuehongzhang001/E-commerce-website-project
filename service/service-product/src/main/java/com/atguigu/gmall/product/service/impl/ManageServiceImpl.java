package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Yuehong Zhang
 * @date 2021-4-10 10:30:46
 */
@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    @Autowired
    private RabbitService rabbitService;



    @Override
    public List<BaseCategory1> getBaseCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    @Override
    public List<BaseCategory2> getBaseCategory2(Long category1Id) {
        //  select * from  base_category2 where category1_id = category1Id;
        return baseCategory2Mapper.selectList(new QueryWrapper<BaseCategory2>().eq("category1_id",category1Id));
    }

    @Override
    public List<BaseCategory3> getBaseCategory3(Long category2Id) {
        //  select * from base_category3 where category2_id = category2Id
        return baseCategory3Mapper.selectList(new QueryWrapper<BaseCategory3>().eq("category2_id",category2Id));
    }

    @Override
    public List<BaseAttrInfo> getBaseAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        // Query the platform attribute list according to the category Id!
        // You may need to query platform attributes + platform attribute values ​​later, so in this method. Query all the data at once!
        // Multi-table related query! Configure *Mapper.xml
        return baseAttrInfoMapper.selectBaseAttrInfoList(category1Id,category2Id,category3Id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // If there is an exception, it will be rolled back. If there is a non-runtime exception in the current code block, it will still be rolled back!
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        // Implementation class base_attr_info, base_attr_value DML statement, need to pay attention to affairs!
        // Determine whether baseAttrInfo.id is empty!
        if (baseAttrInfo.getId()!=null){
            baseAttrInfoMapper.updateById(baseAttrInfo);
        }else {
            // call mapper
            baseAttrInfoMapper.insert(baseAttrInfo);
        }

        // When will it be added and when will it be modified?
        //  do not know when! Therefore: delete first! Add again!
        QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<BaseAttrValue>().eq("attr_id", baseAttrInfo.getId());
        // delete from base_attr_value where attr_id = baseAttrInfo.getId();
        baseAttrValueMapper.delete(queryWrapper);

        // int i = 1/0;
        // Get the platform attribute value collection first
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        // Determine if the collection is not empty
        if (!CollectionUtils.isEmpty(attrValueList)){
            // Loop through the current collection and insert it into the platform attribute table
            for (BaseAttrValue baseAttrValue: attrValueList) {
                // attr_id this field needs to be processed, base_attr_value.attr_id = base_attr_info.id
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        // select * from base_attr_value where attr_id = attrId;
        return baseAttrValueMapper.selectList(new QueryWrapper<BaseAttrValue>().eq("attr_id",attrId));

    }

    @Override
    public BaseAttrInfo getBaseAttrInfo(Long attrId) {
        // select * from base_attr_info where id = attrId;
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        if (baseAttrInfo!=null){
            // Get platform attribute value collection data
            List<BaseAttrValue> attrValueList = getAttrValueList(attrId);
            if (!CollectionUtils.isEmpty(attrValueList)){
                baseAttrInfo.setAttrValueList(attrValueList);
            }
        }
        // return data
        return baseAttrInfo;
    }

    @Override
    public IPage<SpuInfo> getSpuInfoList(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo) {
        // Set query conditions
        QueryWrapper<SpuInfo> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.eq("category3_id",spuInfo.getCategory3Id());
        // set a sort
        spuInfoQueryWrapper.orderByDesc("id");
        // call mapper
        return spuInfoMapper.selectPage(spuInfoPage,spuInfoQueryWrapper);

    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        // The service layer calls the mapper layer object to obtain
        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrMapper.selectList(null);
        return baseSaleAttrList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuInfo(SpuInfo spuInfo) {
        /*
        1. spu_info
        2. spu_image
        3. spu_sale_attr
        4. spu_sale_attr_value
         */
        spuInfoMapper.insert(spuInfo);
        // Get the spuImageList collection data first
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (!CollectionUtils.isEmpty(spuImageList)){
            // loop traversal
            for (SpuImage spuImage: spuImageList) {
                // Assign spuId
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insert(spuImage);
            }
        }
        // Get the current collection of sales attributes
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        //  judge
        if (!CollectionUtils.isEmpty(spuSaleAttrList)){
            // loop traversal
            for (SpuSaleAttr spuSaleAttr: spuSaleAttrList) {
                // Assign spuId
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);

                // Get the current collection of sales attribute values
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (!CollectionUtils.isEmpty(spuSaleAttrValueList)){
                    // loop traversal
                    for (SpuSaleAttrValue spuSaleAttrValue: spuSaleAttrValueList) {
                        // Assign spuId
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        // Assign the sales attribute name
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }
                }
            }
        }
    }

    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        // select * from spu_image where spu_id = spuId;
        return spuImageMapper.selectList(new QueryWrapper<SpuImage>().eq("spu_id",spuId));
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        // The service layer calls the mapper layer method
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
        return spuSaleAttrList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
        /*
        1. sku_info
        2. sku_attr_value
        3. sku_sale_attr_value
        4. sku_image
         */
        skuInfoMapper.insert(skuInfo);
        // Get the data corresponding to the sku_attr_value table
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        //  judge
        if(!CollectionUtils.isEmpty(skuAttrValueList)){
            // loop traversal
            for (SkuAttrValue skuAttrValue: skuAttrValueList) {
                // Assign skuId
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }
        // Get sku_sale_attr_value data
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)){
            for (SkuSaleAttrValue skuSaleAttrValue: skuSaleAttrValueList) {
                // Assign spuId, skuId
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }

        // Get sku_image data
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (!CollectionUtils.isEmpty(skuImageList)){
            // loop traversal
            for (SkuImage skuImage: skuImageList) {
                // Assign skuId
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            }
        }
    }

    @Override
    public IPage getSkuInfoList(Page<SkuInfo> skuInfoPage) {
        // select * from sku_info order by id limit 0, 10;
        QueryWrapper<SkuInfo> skuInfoQueryWrapper = new QueryWrapper<>();
        skuInfoQueryWrapper.orderByDesc("id");
        return skuInfoMapper.selectPage(skuInfoPage,skuInfoQueryWrapper);
    }

    @Override
    public void onSale(Long skuId) {
        //  update status
        // update sku_info set is_sale = 1 where id = 45;
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);

        // Send a message!
        // Send message body: skuId
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_GOODS,MqConst.ROUTING_GOODS_UPPER,skuId);

    }

    @Override
    public void cancelSale(Long skuId) {
        //  update status
        // update sku_info set is_sale = 0 where id = 45;
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);

        // Send a message!
        // Send message body: skuId
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_GOODS,MqConst.ROUTING_GOODS_LOWER,skuId);

    }

    /**
     * Query skuInfo and skuImageList according to skuId
     * @param skuId
     * @return
     */
    @Override
    // Add a comment to replace the business logic of distributed locks: lock getSkuInfo sku:skuId:lock;
    @GmallCache(prefix = "sku:")
    public SkuInfo getSkuInfo(Long skuId) {
        // call redisson
        // return getSkuInfoRedisson(skuId);
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoRedisson(Long skuId) {
        // Declare the object
        SkuInfo skuInfo = new SkuInfo();
        try {
            // skuKey = sku:skuId:info;
            String skuKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
            // Because String and Hash have been processed for serial numbers in the configuration class!
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            //  judge
            if (skuInfo==null){

                // Define the key of the lock
                String skuLockKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
                RLock lock = redissonClient.getLock(skuLockKey);
//------------------------------------------------ -------------------------------------------------- -------------------
                // locked
                boolean flag = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                //  judge
                if (flag){
                    // Successfully locked! Execute business logic
                    try {
                        // Query the database and put it in the cache
                        // To query the database to prevent cache penetration
                        skuInfo = getSkuInfoDB(skuId);
                        if (skuInfo==null){
                            // put in an empty object
                            SkuInfo skuInfo1 = new SkuInfo();
                            redisTemplate.opsForValue().set(skuKey,skuInfo1,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                            return skuInfo1;
                        }
                        // skuInfo is not empty
                        redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                        // Return data!
                        return skuInfo;
                    } finally {
                        lock.unlock();
                    }
                }else {
                    // These people didn't get the lock!
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // spin
                    return getSkuInfo(skuId);
                }

            }else {
                // Cached data
                return skuInfo;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // The bottom of the database
        return getSkuInfoDB(skuId);
    }

    /**
     * Obtain skuInfo according to skuId --- redis does distributed lock!
     * @param skuId
     * @return
     */
    private SkuInfo getSkuInfoRedis(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        try {
            // Get cached data How to get data? String List Set Hash ZSet
            // redisTemplate.opsForHash() Yes! hset(key,field,value) field = the attribute name of the entity class! Easy to modify! hget(field,value);
            // But: Store String directly!
            // skuKey = sku:skuId:info;
            String skuKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
            // Because String and Hash have been processed for serial numbers in the configuration class!
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            //        //  Because
            // redisTemplate.opsForValue().set(skuKey,skuInfo);
            // Prevent cache breakdown
            if(skuInfo==null){
                // Locked!
                // Define a lock key=sku:skuId:lock
                String skuLockKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
                String uuid = UUID.randomUUID().toString();
                // start to lock
                Boolean flag = redisTemplate.opsForValue().setIfAbsent(skuLockKey, uuid, RedisConst.SKULOCK_EXPIRE_PX1, TimeUnit.SECONDS);
                if (flag){
                    // Successfully locked!
                    // To query the database to prevent cache penetration
                    skuInfo = getSkuInfoDB(skuId);
                    if (skuInfo==null){
                        // put in an empty object
                        SkuInfo skuInfo1 = new SkuInfo();
                        redisTemplate.opsForValue().set(skuKey,skuInfo1,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                        return skuInfo1;
                    }
                    // Put the data in the cache and return!
                    redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                    String script= "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

                    // Release the lock Use lua script!
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                    redisScript.setScriptText(script);
                    redisScript.setResultType(Long.class);
                    redisTemplate.execute(redisScript, Arrays.asList(skuLockKey),uuid);
                    // Return data!
                    return skuInfo;
                }
            }else {
                // Indicates that there is data in the cache!
                return skuInfo;
            }
        } catch (Exception e) {
            // sout("redis --- down")
            // Call the method of sending text messages: Engineer!
            e.printStackTrace();
        }
        // The bottom of the database!
        return getSkuInfoDB(skuId);
    }

    // Query the database based on skuId! ctrl+alt+m
    private SkuInfo getSkuInfoDB(Long skuId) {
        // select * from sku_info where id = skuId;
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);

        // select * from sku_image where sku_id = skuId;
        // Construct query conditions
        QueryWrapper<SkuImage> skuImageQueryWrapper = new QueryWrapper<>();
        skuImageQueryWrapper.eq("sku_id",skuId);
        List<SkuImage> skuImageList = skuImageMapper.selectList(skuImageQueryWrapper);
        // Assign the skuImageList collection to the skuInfo object;
        // Solve the null pointer exception!
        if (skuInfo!=null){
            skuInfo.setSkuImageList(skuImageList);
        }

        return skuInfo;
    }

    @Override
    @GmallCache(prefix = "categoryViewByCategory3Id:")
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {

        // select * from base_category_view where id = 61;
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Override
    @GmallCache(prefix= "skuPrice:") // Who is the key of the lock? skuPrice:skuId
    public BigDecimal getSkuPrice(Long skuId) {
        // select price from sku_info where id = skuId;
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (skuInfo!=null){
            return skuInfo.getPrice();
        }
        // give default value
        return new BigDecimal(0);
    }

    @Override
    @GmallCache(prefix = "spuSaleAttrListCheckBySku:")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        // Multi-table related query
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);
    }

    @Override
    @GmallCache(prefix = "skuValueIdsMap:")
    public Map getSkuValueIdsMap(Long spuId) {
        // Declare a return value object
        Map map = new HashMap();
        // Execute sql statement to get data result set: write xml? Which mapper.xml should be written in?
        List<Map> mapList = skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(spuId);
        // Determine that the current collection is not empty
        if (!CollectionUtils.isEmpty(mapList)){
            // Loop through the execution results of the current sql
            for (Map maps: mapList) {
                // Put the data obtained in the loop into the outer map
                // Convert to Json via Map {"126|124":"46","126|125":"47"} value_ids = 126|124 skuId = 46;
                // map.put("126|124","46"); map.put("126|125","47");
                map.put(maps.get("value_ids"),maps.get("sku_id"));
            }
        }
        //  return
        return map;
    }

    @Override
    @GmallCache(prefix = "baseCategoryList:")
    public List<JSONObject> getBaseCategoryList() {
        // Declare a collection
        List<JSONObject> list = new ArrayList<>();
        // Query all classified data!
        // select * from base_category_view;
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);

        // Declare an index
        int index = 1;
        // Group according to the first-level classification Id
        // key = category1Id value = List<BaseCategoryView>
        Map<Long, List<BaseCategoryView>> category1Map = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        // Loop through the current collection data
        Iterator<Map.Entry<Long, List<BaseCategoryView>>> iterator = category1Map.entrySet().iterator();
        // The data is from 1-60 rows!
        while (iterator.hasNext()){
            Map.Entry<Long, List<BaseCategoryView>> entry = iterator.next();
            // Get the corresponding key, value
            Long category1Id = entry.getKey();
            List<BaseCategoryView> category2List = entry.getValue();
            // Get the name of the first level category
            String categoryName = category2List.get(0).getCategory1Name();

            // Assignment
            JSONObject category1 = new JSONObject();
            category1.put("index",index);
            category1.put("categoryName",categoryName);
            category1.put("categoryId",category1Id);

            // index iteration
            index++;
            // Get secondary classification data:
            Map<Long, List<BaseCategoryView>> category2Map = category2List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));

            // Declare a secondary classification collection data
            List<JSONObject> category2Child = new ArrayList<>();

            // loop traversal
            Iterator<Map.Entry<Long, List<BaseCategoryView>>> iterator1 = category2Map.entrySet().iterator();
            while (iterator1.hasNext()){
                // The first loop: 1-4 rows of data The second time: 5-7
                Map.Entry<Long, List<BaseCategoryView>> entry1 = iterator1.next();
                // Get key, value
                Long category2Id = entry1.getKey();
                List<BaseCategoryView> category3List = entry1.getValue();
                // Get the name of the secondary category
                String category2Name = category3List.get(0).getCategory2Name();
                // Assignment
                JSONObject category2 = new JSONObject();
                category2.put("categoryName",category2Name);
                category2.put("categoryId",category2Id);


                // Declare a set to store all the secondary classification data!
                category2Child.add(category2);

                // Declare a three-level classification collection data
                List<JSONObject> category3Child = new ArrayList<>();
                // Get three-level classification data
                category3List.forEach((baseCategoryView)->{
                    JSONObject category3 = new JSONObject();
                    category3.put("categoryName",baseCategoryView.getCategory3Name());
                    category3.put("categoryId",baseCategoryView.getCategory3Id());
                    category3Child.add(category3);
                });
                // Put the three-level classification data into the second-level object
                category2.put("categoryChild",category3Child);
            }
            // Put the second-level classification data into the first-level object
            category1.put("categoryChild",category2Child);
            // Put the first-level classification data into the list
            list.add(category1);
        }
        // return collection data
        return list;
    }

    @Override
    public BaseTrademark getTrademarkByTmId(Long tmId) {
        // select * from base_trademark where id = tmId;
        return baseTrademarkMapper.selectById(tmId);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        // The platform attribute and platform attribute value corresponding to the current skuId!
        return baseAttrInfoMapper.selectBaseAttrInfoListBySkuId(skuId);
    }



}
