package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.constant.EsConstant;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.mapper.*;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.to.es.EsProduct;
import com.atguigu.gmall.to.es.EsProductAttributeValue;
import com.atguigu.gmall.to.es.EsSkuProductInfo;
import com.atguigu.gmall.vo.PageInfo;
import com.atguigu.gmall.vo.product.PmsProductParam;
import com.atguigu.gmall.vo.product.PmsProductQueryParam;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import groovy.util.IFileNameFinder;
import io.searchbox.client.JestClient;
import io.searchbox.core.Delete;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.swagger.models.properties.PropertyBuilder;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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

    @Autowired
    JestClient jestClient;
    ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    @Override
    public Product productInfo(Long id) {
        return productMapper.selectById(id);
    }

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

    @Override
    public void updatePublishStatus(List<Long> ids, Integer publishStatus) {

        if(publishStatus == 1){
            ids.forEach((id)->{
                //1数据库修改
                setProductPublishStatus(publishStatus, id);

                saveProductToEs(id);
            });

        }else{

            ids.forEach((id)->{
                //1改数据库状态
                setProductPublishStatus(publishStatus, id);
                //2在ES中删除
                deleteProductFromEs(id);

            });


        }



    }

    public void deleteProductFromEs(Long id) {
        Delete build = new Delete.Builder(id.toString())
                .index(EsConstant.PRODUCT_ES_INDEX)
                .type(EsConstant.PRODUCT_INFO_ES_TYPE)
                .build();
        try {
            DocumentResult execute = jestClient.execute(build);
            boolean succeeded = execute.isSucceeded();
            if(succeeded){

            }else{
                //deleteProductFromEs(id);
            }
        }catch (Exception e){

        }


    }

    public void saveProductToEs(Long id) {
        Product productInfo = productInfo(id);
        //2es服务器修改
        EsProduct esProduct = new EsProduct();
        //复制基本信息
        BeanUtils.copyProperties(productInfo,esProduct);
        //复制sku信息
        List<SkuStock> stocks = skuStockMapper.selectList(new QueryWrapper<SkuStock>().eq("product_id", id));
        List<EsSkuProductInfo> esSkuProductInfos = new ArrayList<>(stocks.size());

        List<ProductAttribute> skuAttributeNames =  productAttributeValueMapper.selectProductSaleAttrName(id);

        stocks.forEach((stock)->{
            EsSkuProductInfo info = new EsSkuProductInfo();
            BeanUtils.copyProperties(stock,info);

            String subTitle = esProduct.getName();
            if(!StringUtils.isEmpty(stock.getSp1())){
                subTitle +=" "+stock.getSp1();
            }
            if(!StringUtils.isEmpty(stock.getSp2())){
                subTitle +=" "+stock.getSp2();
            }
            if(!StringUtils.isEmpty(stock.getSp3())){
                subTitle +=" "+stock.getSp3();
            }
            info.setSkuTitle(subTitle);



            List<EsProductAttributeValue> skuAttributeValues = new ArrayList<>();


            for (int i = 0; i < skuAttributeNames.size(); i++) {
                EsProductAttributeValue value = new EsProductAttributeValue();
                value.setName(skuAttributeNames.get(i).getName());
                value.setProductId(id);
                value.setProductAttributeId(skuAttributeNames.get(i).getId());
                value.setType(skuAttributeNames.get(i).getType());

                if(i == 0){
                    value.setValue(stock.getSp1());
                }
                if(i == 1){
                    value.setValue(stock.getSp2());
                }
                if(i == 2){
                    value.setValue(stock.getSp3());
                }
                //可能有问题(新改好的)
                skuAttributeValues.add(value);
            }


            info.setAttributeValues(skuAttributeValues);
            esSkuProductInfos.add(info);
            //查出sku所有销售属性的对应值


        } );

        esProduct.setSkuProductInfos(esSkuProductInfos);
        //复制公共属性信息

        List<EsProductAttributeValue> attributeValues = productAttributeValueMapper.selectProductBaseAttrAndValue(id);
        esProduct.setAttrValueList(attributeValues);
        try {
            Index build = new Index.Builder(esProduct)
                    .index(EsConstant.PRODUCT_ES_INDEX)
                    .type(EsConstant.PRODUCT_INFO_ES_TYPE)
                    .id(id.toString())
                    .build();
            DocumentResult execute = jestClient.execute(build);
            boolean succeeded = execute.isSucceeded();
            if(succeeded){

            }else{
                //saveProductToEs(id);

            }
        }catch (Exception e){

        }

    }

    public void setProductPublishStatus(Integer publishStatus, Long id) {
        Product product = new Product();
        product.setId(id);
        product.setPublishStatus(publishStatus);
        //其他字段不会变为null
        productMapper.updateById(product);
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
