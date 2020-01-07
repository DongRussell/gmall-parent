package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.Product;
import com.atguigu.gmall.vo.PageInfo;
import com.atguigu.gmall.vo.product.PmsProductParam;
import com.atguigu.gmall.vo.product.PmsProductQueryParam;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 商品信息 服务类
 * </p>
 *
 * @author Brodie
 * @since 2020-01-04
 */
public interface ProductService extends IService<Product> {

    Product productInfo(Long id);

    PageInfo productPageInfo(PmsProductQueryParam productQueryParam);

    void saveProduct(PmsProductParam productParam);

    void updatePublishStatus(List<Long> ids, Integer publishStatus);
}
