package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Yuehong Zhang
 * @date 2021-4-10 10:23:45
 */
@Mapper
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {
    /**
     * Query the platform attribute collection according to the category Id
     * When using xml to query mybatis, if you pass a single parameter, you can directly use the parameter name or use #{0}
     * If there are multiple parameters: use the annotation @Param mapping, and use the name in the xml to get it!
     * There are two configuration files in mybatis
     * 1. Core configuration file mybatis-cfg.xml
     * <mappers>
     * <mapper resource="/com/atguigu/mapper/BaseAttrInfoMapper.xml"/>
     * <mapper resource="/com/atguigu/mapper/BaseAttrInfoMapper.xml"/>
     * <mapper resource="/com/atguigu/mapper/BaseAttrInfoMapper.xml"/>
     * <package name="com.atguigu.mapper"></package>
     * Need to put mapper.java mapper.xml in the same package!
     * </mappers>
     * 2. Mapping file *Mapper.xml
     *
     *
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    List<BaseAttrInfo> selectBaseAttrInfoList(@Param("category1Id") Long category1Id,
                                              @Param("category2Id") Long category2Id,
                                              @Param("category3Id") Long category3Id);

    /**
     * Query platform attributes and attribute value data according to skuId
     * @param skuId
     * @return
     */
    List<BaseAttrInfo> selectBaseAttrInfoListBySkuId(Long skuId);

}