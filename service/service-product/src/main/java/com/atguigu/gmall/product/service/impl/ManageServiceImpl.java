package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageSerivce;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author mqx
 * @date 2021-4-10 10:30:46
 */
@Service
public class ManageServiceImpl implements ManageSerivce {
    //  服务层调用mapper 层
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
        //  根据分类Id 查询平台属性列表！
        //  后续有可能需要查询平台属性+ 平台属性值，因此在这个方法中。一次性将所有数据全部查询即可！
        //  多表关联查询！ 配置*Mapper.xml
        return baseAttrInfoMapper.selectBaseAttrInfoList(category1Id,category2Id,category3Id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) //    如果有异常会回滚，如果当前代码块中出现了非运行时异常，则照样回滚！
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        //  实现类 base_attr_info，base_attr_value DML 语句 需要注意事务！
        //  判断baseAttrInfo.id 是否为空！
        if (baseAttrInfo.getId()!=null){
            baseAttrInfoMapper.updateById(baseAttrInfo);
        }else {
            //  调用mapper
            baseAttrInfoMapper.insert(baseAttrInfo);
        }

        //  什么时候新增，什么时候修改?
        //  不知道什么时候！因此：先删除！再新增！
        QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<BaseAttrValue>().eq("attr_id", baseAttrInfo.getId());
        //  delete from base_attr_value where attr_id = baseAttrInfo.getId();
        baseAttrValueMapper.delete(queryWrapper);

        //  int i = 1/0;
        //  先获取到平台属性值集合
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        //  判断集合不为空
        if (!CollectionUtils.isEmpty(attrValueList)){
            //  循环遍历当前集合，插入到平台属性表中
            for (BaseAttrValue baseAttrValue : attrValueList) {
                //  attr_id 这个字段需要处理, base_attr_value.attr_id = base_attr_info.id
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        //  select * from base_attr_value where attr_id = attrId;
        return baseAttrValueMapper.selectList(new QueryWrapper<BaseAttrValue>().eq("attr_id",attrId));

    }

    @Override
    public BaseAttrInfo getBaseAttrInfo(Long attrId) {
        //  select * from base_attr_info where id  =  attrId;
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        if (baseAttrInfo!=null){
            //  获取平台属性值集合数据
            List<BaseAttrValue> attrValueList = getAttrValueList(attrId);
            if (!CollectionUtils.isEmpty(attrValueList)){
                baseAttrInfo.setAttrValueList(attrValueList);
            }
        }
        //  返回数据
        return baseAttrInfo;
    }

    @Override
    public IPage<SpuInfo> getSpuInfoList(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo) {
        //  设置查询条件
        QueryWrapper<SpuInfo> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.eq("category3_id",spuInfo.getCategory3Id());
        //  设置一个排序
        spuInfoQueryWrapper.orderByDesc("id");
        //  调用mapper
        return spuInfoMapper.selectPage(spuInfoPage,spuInfoQueryWrapper);

    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        //  服务层调用mapper层对象获取
        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrMapper.selectList(null);
        return baseSaleAttrList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuInfo(SpuInfo spuInfo) {
        /*
        1.  spu_info
        2.  spu_image
        3.  spu_sale_attr
        4.  spu_sale_attr_value
         */
        spuInfoMapper.insert(spuInfo);
        //  先获取到spuImageList 集合数据
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (!CollectionUtils.isEmpty(spuImageList)){
            //  循环遍历
            for (SpuImage spuImage : spuImageList) {
                //  将spuId 进行赋值
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insert(spuImage);
            }
        }
        //  获取当前的销售属性集合
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        //  判断
        if (!CollectionUtils.isEmpty(spuSaleAttrList)){
            //  循环遍历
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                //  将spuId 进行赋值
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);

                //  获取当前的销售属性值集合
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (!CollectionUtils.isEmpty(spuSaleAttrValueList)){
                    //  循环遍历
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        //  将spuId 进行赋值
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        //  赋值销售属性名称
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }
                }
            }
        }
    }

    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        //  select * from spu_image where spu_id = spuId;
        return spuImageMapper.selectList(new QueryWrapper<SpuImage>().eq("spu_id",spuId));
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        //  服务层调用mapper层方法
        List<SpuSaleAttr>  spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
        return spuSaleAttrList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
        /*
        1.  sku_info
        2.  sku_attr_value
        3.  sku_sale_attr_value
        4.  sku_image
         */
        skuInfoMapper.insert(skuInfo);
        //  获取sku_attr_value 表对应的数据
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        //  判断
        if(!CollectionUtils.isEmpty(skuAttrValueList)){
            //  循环遍历
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                //  赋值skuId
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }
        //  获取sku_sale_attr_value数据
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)){
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                //  赋值spuId,skuId
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }

        //  获取sku_image 数据
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (!CollectionUtils.isEmpty(skuImageList)){
            //  循环遍历
            for (SkuImage skuImage : skuImageList) {
                //  赋值skuId
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            }
        }
    }

    @Override
    public IPage getSkuInfoList(Page<SkuInfo> skuInfoPage) {
        //  select * from sku_info order by id limit 0, 10;
        QueryWrapper<SkuInfo> skuInfoQueryWrapper = new QueryWrapper<>();
        skuInfoQueryWrapper.orderByDesc("id");
        return skuInfoMapper.selectPage(skuInfoPage,skuInfoQueryWrapper);
    }

    @Override
    public void onSale(Long skuId) {
        //  更新状态
        //  update  sku_info set is_sale = 1 where id = 45;
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
    }

    @Override
    public void cancelSale(Long skuId) {
        //  更新状态
        //  update  sku_info set is_sale = 0 where id = 45;
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
    }

}
