package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author mqx
 * @date 2021-4-13 14:41:21
 */
@Mapper
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {
    /**
     * 通过spuId 获取到销售属性值对应的skuId 组合
     * @param spuId
     * @return
     */
    List<Map> selectSaleAttrValuesBySpu(Long spuId);
}
