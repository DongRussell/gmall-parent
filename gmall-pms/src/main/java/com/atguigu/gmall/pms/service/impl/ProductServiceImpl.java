package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.mapper.*;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.vo.PageInfo;
import com.atguigu.gmall.vo.product.PmsProductParam;
import com.atguigu.gmall.vo.product.PmsProductQueryParam;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.models.properties.PropertyBuilder;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 商品信息 服务实现类
 * </p>
 *
 * @author Brodie
 * @since 2020-01-04
 */

@Component
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    ProductMapper productMapper;
    @Autowired
    ProductAttributeValueMapper productAttributeValueMapper;
    @Autowired
    ProductFullReductionMapper productFullReductionMapper;
    @Autowired
    ProductLadderMapper productLadderMapper;
    @Autowired
    SkuStockMapper skuStockMapper;

    ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    @Override
    public PageInfo productPageInfo(PmsProductQueryParam param) {

        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        if (param.getBrandId()!=null){
            wrapper.eq("brand_id",param.getBrandId());
        }

        if (!StringUtils.isEmpty(param.getKeyword())){
            wrapper.like("name",param.getKeyword());
        }

        if (param.getProductCategoryId()!=null){
            wrapper.eq("product_category_id",param.getProductCategoryId());
        }

        if (!StringUtils.isEmpty(param.getProductSn())){
            wrapper.like("product_sn",param.getProductSn());
        }

        if (param.getPublishStatus()!=null){
            wrapper.eq("publish_status",param.getPublishStatus());
        }

        if (param.getVerifyStatus()!=null){
            wrapper.eq("verify_status",param.getVerifyStatus());
        }

        IPage<Product> page = productMapper.selectPage(new Page<Product>(param.getPageNum(), param.getPageSize()), wrapper);
        PageInfo pageInfo = new PageInfo(   page.getTotal(),
                                            page.getPages(),
                                            param.getPageSize(),
                                            page.getRecords(),
                                            page.getCurrent());
        return pageInfo;
    }

    /*
        大保存
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveProduct(PmsProductParam productParam) {
        ProductServiceImpl proxy = (ProductServiceImpl) AopContext.currentProxy();
        //1.商品基本信息 pms_product
        proxy.saveBaseInfo(productParam);

        //2.商品属性值 pms_product_attribute_value
        proxy.saveProductAttributeValue(productParam);

        //3.商品满减信息 pms_product_full_reduction
        proxy.savaProductFullReduction(productParam);

        //4.商品满减表 pms_product_ladder
        proxy.saveProductLadder(productParam);

        //5.商品库存表 pms_sku_stock
        proxy.saveProductSkuStock(productParam);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProductSkuStock(PmsProductParam productParam) {
        List<SkuStock> skuStockList = productParam.getSkuStockList();
        for (int i = 1; i <= skuStockList.size(); i++) {
            SkuStock skuStock = skuStockList.get(i-1);

            if(StringUtils.isEmpty(skuStock.getSkuCode())){
                skuStock.setSkuCode(threadLocal.get()+"_"+i);
            }
            skuStock.setProductId(threadLocal.get());
            skuStockMapper.insert(skuStock);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProductLadder(PmsProductParam productParam) {
        List<ProductLadder> productLadderList = productParam.getProductLadderList();
        productLadderList.forEach((ladder)->{
            ladder.setProductId(threadLocal.get());
            productLadderMapper.insert(ladder);

        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savaProductFullReduction(PmsProductParam productParam) {
        List<ProductFullReduction> productFullReductionList = productParam.getProductFullReductionList();
        productFullReductionList.forEach((reduction)->{
            reduction.setProductId(threadLocal.get());
            productFullReductionMapper.insert(reduction);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBaseInfo(PmsProductParam productParam) {
        //1.商品基本信息 pms_product
        Product product = new Product();
        BeanUtils.copyProperties(productParam, product);
        productMapper.insert(product);
        threadLocal.set(product.getId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProductAttributeValue(PmsProductParam productParam) {
        //2.商品属性值 pms_product_attribute_value
        List<ProductAttributeValue> productAttributeValueList = productParam.getProductAttributeValueList();
        productAttributeValueList.forEach((item)->{
            item.setProductId(threadLocal.get());
            productAttributeValueMapper.insert(item);
        });
    }
}
