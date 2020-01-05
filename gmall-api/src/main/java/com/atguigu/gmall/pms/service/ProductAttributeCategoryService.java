package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.ProductAttributeCategory;
import com.atguigu.gmall.vo.PageInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 产品属性分类表 服务类
 * </p>
 *
 * @author Brodie
 * @since 2020-01-04
 */
public interface ProductAttributeCategoryService extends IService<ProductAttributeCategory> {

    PageInfo pageInfo(Integer pageSize, Integer pageNum);
}
