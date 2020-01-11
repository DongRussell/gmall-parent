package com.atguigu.gmall.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.to.CommonResult;
import com.atguigu.gmall.to.es.EsProduct;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Brodie
 * @date 2020/1/11 - 20:54
 */

@RestController
public class ProductItemController {

    @Reference
    ProductService productService;

    public EsProduct productInfo2(Long id){
        //1、第一次查。肯定长。
        //1、商品基本数据（名字介绍等） 100ms   异步
        new Thread(()->{
            System.out.println("查基本信息");
        }).start();

        //2、商品的属性数据  300ms
        new Thread(()->{
            System.out.println("查属性信息");
        }).start();

        //3、商品的营销数据  SmsService 1s 500ms
        new Thread(()->{
            System.out.println("查营销信息");
        }).start();
        //4、商品的配送数据  WuliuService 2s  700ms
        new Thread(()->{
            System.out.println("查配送信息");
        }).start();
        //5、商品的增值服务数据  SaleService  1s 1s
        new Thread(()->{
            System.out.println("查增值信息");
        }).start();

        //8s估计就不看了 可以开启异步化查询
        //1缓存
        //2异步
        return null;
    }


    @GetMapping("/item/{id}.html")
    public CommonResult productInfo(@PathVariable("id") Long id){

        EsProduct esProduct = productService.productAllInfo(id);
        return new CommonResult().success(esProduct);
    }


    @GetMapping("/item/sku/{id}.html")
    public CommonResult productSkuInfo(@PathVariable("id")Long id){

        EsProduct esProduct = productService.productSkuInfo(id);
        return new CommonResult().success(esProduct);
    }
}
