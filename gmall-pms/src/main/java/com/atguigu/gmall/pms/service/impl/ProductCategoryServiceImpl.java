package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.constant.SysConstant;
import com.atguigu.gmall.pms.entity.ProductCategory;
import com.atguigu.gmall.pms.mapper.ProductCategoryMapper;
import com.atguigu.gmall.pms.service.ProductCategoryService;
import com.atguigu.gmall.vo.product.PmsProductCategoryWithChildrenItem;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


import java.util.List;

/**
 * <p>
 * 产品分类 服务实现类
 * </p>
 *
 * @author Brodie
 * @since 2020-01-04
 */
@Slf4j
@Service
@Component
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {

    @Autowired
    ProductCategoryMapper productCategoryMapper;
    @Autowired
    RedisTemplate<Object,Object> redisTemplate;

    @Override
    public List<PmsProductCategoryWithChildrenItem> listCategoryWithChildren(int i) {
        Object cacheMenu = redisTemplate.opsForValue().get(SysConstant.CATEGORY_MEMU_CACHE_KEY);
        List<PmsProductCategoryWithChildrenItem> items;
        if(cacheMenu != null){
            items = (List<PmsProductCategoryWithChildrenItem>) cacheMenu;
        }else{//缓存没有
            items = productCategoryMapper.listCategoryWithChildren(i);
            //魔法值
            redisTemplate.opsForValue().set(SysConstant.CATEGORY_MEMU_CACHE_KEY,items);

        }



        return items;
    }
}
