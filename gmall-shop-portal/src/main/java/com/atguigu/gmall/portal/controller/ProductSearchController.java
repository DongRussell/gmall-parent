package com.atguigu.gmall.portal.controller;

import com.atguigu.gmall.search.SearchProductService;
import com.atguigu.gmall.vo.search.SearchParam;
import com.atguigu.gmall.vo.search.SearchResponse;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Brodie
 * @date 2020/1/9 - 15:31
 */
@RestController
public class ProductSearchController {

    @Reference
    SearchProductService searchProductService;

    @GetMapping("/search")
    public SearchResponse productSearchResponse(SearchParam searchParam){
        SearchResponse searchResponse = searchProductService.searchProduct(searchParam);
        return searchResponse;
    }
}
