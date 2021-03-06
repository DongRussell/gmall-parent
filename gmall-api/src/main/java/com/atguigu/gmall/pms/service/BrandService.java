package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.Brand;
import com.atguigu.gmall.vo.PageInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 品牌表 服务类
 * </p>
 *
 * @author Brodie
 * @since 2020-01-04
 */
public interface BrandService extends IService<Brand> {

    PageInfo brandPageInf(String keyword, Integer pageNum, Integer pageSize);
}
