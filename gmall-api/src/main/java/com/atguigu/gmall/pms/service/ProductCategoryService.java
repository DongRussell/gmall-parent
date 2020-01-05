package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.ProductCategory;
import com.atguigu.gmall.vo.product.PmsProductCategoryWithChildrenItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 产品分类 服务类
 * </p>
 *
 * @author Brodie
 * @since 2020-01-04
 */
public interface ProductCategoryService extends IService<ProductCategory> {

    List<PmsProductCategoryWithChildrenItem> listCategoryWithChildren(int i);
}
