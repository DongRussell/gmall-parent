package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.pms.entity.ProductAttributeCategory;
import com.atguigu.gmall.pms.mapper.ProductAttributeCategoryMapper;
import com.atguigu.gmall.pms.service.ProductAttributeCategoryService;
import com.atguigu.gmall.vo.PageInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * <p>
 * 产品属性分类表 服务实现类
 * </p>
 *
 * @author Brodie
 * @since 2020-01-04
 */
@Component
@Service
public class ProductAttributeCategoryServiceImpl extends ServiceImpl<ProductAttributeCategoryMapper, ProductAttributeCategory> implements ProductAttributeCategoryService {

    @Autowired
    ProductAttributeCategoryMapper productAttributeCategoryMapper;
    @Override
    public PageInfo pageInfo(Integer pageSize, Integer pageNum) {
        IPage<ProductAttributeCategory> page = productAttributeCategoryMapper.selectPage(
                new Page<ProductAttributeCategory>(pageNum, pageSize),
                null);

        return PageInfo.getVo(page,pageSize.longValue());
    }
}
