package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.pms.entity.Brand;
import com.atguigu.gmall.pms.mapper.BrandMapper;
import com.atguigu.gmall.pms.service.BrandService;
import com.atguigu.gmall.vo.PageInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 品牌表 服务实现类
 * </p>
 *
 * @author Brodie
 * @since 2020-01-04
 */
@Service
@Component
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {

    @Autowired
    BrandMapper brandMapper;

    @Override
    public PageInfo brandPageInf(String keyword, Integer pageNum, Integer pageSize) {
        QueryWrapper<Brand> name = null;

        if(!StringUtils.isEmpty(keyword)){
            name = new QueryWrapper<Brand>().like("name",keyword);
        }
        IPage<Brand> page = brandMapper.selectPage(new Page<Brand>(pageNum.longValue(), pageSize.longValue()), name);
        PageInfo pageInfo = new PageInfo(   page.getTotal(),
                                            page.getPages(),
                                            pageSize.longValue(),
                                            page.getRecords(),
                                            page.getCurrent());
        return pageInfo;
    }
}
