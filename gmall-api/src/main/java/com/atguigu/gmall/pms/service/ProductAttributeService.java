package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.ProductAttribute;
import com.atguigu.gmall.vo.PageInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 商品属性参数表 服务类
 * </p>
 *
 * @author Brodie
 * @since 2020-01-04
 */
public interface ProductAttributeService extends IService<ProductAttribute> {

    PageInfo getCategoryAttribute(Long cid, Integer type, Integer pageSize, Integer pageNum);
}
