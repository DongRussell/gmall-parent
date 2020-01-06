package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.pms.entity.ProductAttribute;
import com.atguigu.gmall.pms.mapper.ProductAttributeMapper;
import com.atguigu.gmall.pms.service.ProductAttributeService;
import com.atguigu.gmall.vo.PageInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * <p>
 * 商品属性参数表 服务实现类
 * </p>
 *
 * @author Brodie
 * @since 2020-01-04
 */
@Service
@Component
public class ProductAttributeServiceImpl extends ServiceImpl<ProductAttributeMapper, ProductAttribute> implements ProductAttributeService {

    @Autowired
    ProductAttributeMapper productAttributeMapper;

    @Override
    public PageInfo getCategoryAttribute(Long cid, Integer type, Integer pageSize, Integer pageNum) {
        QueryWrapper<ProductAttribute> wrapper = new QueryWrapper<ProductAttribute>()
                .eq("product_attribute_category_id",cid)
                .eq("TYPE",type);

        IPage<ProductAttribute> page = productAttributeMapper.selectPage(new Page<ProductAttribute>(pageNum, pageSize), wrapper);
        return PageInfo.getVo(page,pageSize.longValue());
    }
}
