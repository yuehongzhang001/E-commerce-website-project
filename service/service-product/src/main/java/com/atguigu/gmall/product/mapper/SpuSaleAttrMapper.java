package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author mqx
 * @date 2021-4-13 09:43:05
 */
@Mapper
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {
    /**
     * 根据spuId 查询销售属性列表
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> selectSpuSaleAttrList(Long spuId);

    /**
     * 根据spuId,skuId 查询销售属性+销售属性值
     * @param skuId
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(@Param("skuId") Long skuId, @Param("spuId")Long spuId);
}
