package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author mqx
 * @date 2021-4-10 10:23:45
 */
@Mapper
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {
    /**
     * 根据分类Id 查询平台属性集合
     * mybatis 在使用xml 进行查询的时候，如果传递单个参数，则直接使用参数名称即可或者使用#{0}
     * 如果是多个参数：使用注解@Param 映射，xml 中使用名称来获取！
     * mybatis 中有两个配置文件
     * 1.   核心配置文件 mybatis-cfg.xml
     *      <mappers>
     *          <mapper resource="/com/atguigu/mapper/BaseAttrInfoMapper.xml"/>
     *          <mapper resource="/com/atguigu/mapper/BaseAttrInfoMapper.xml"/>
     *          <mapper resource="/com/atguigu/mapper/BaseAttrInfoMapper.xml"/>
     *          <package name="com.atguigu.mapper"></package>
     *          需要将mapper.java mapper.xml 放在同一个包下！
     *      </mappers>
     * 2.   映射文件*Mapper.xml
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

}
