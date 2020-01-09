package com.atguigu.gmall.search;

import com.atguigu.gmall.vo.search.SearchParam;
import com.atguigu.gmall.vo.search.SearchResponse;

import java.io.IOException;

/**
 * @author Brodie
 * @date 2020/1/9 - 15:20
 */
public interface SearchProductService {

    SearchResponse searchProduct(SearchParam searchParam) throws IOException;
}
