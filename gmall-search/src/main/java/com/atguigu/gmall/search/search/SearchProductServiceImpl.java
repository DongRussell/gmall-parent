package com.atguigu.gmall.search.search;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constant.EsConstant;
import com.atguigu.gmall.search.SearchProductService;
import com.atguigu.gmall.to.es.EsProduct;
import com.atguigu.gmall.vo.search.SearchParam;
import com.atguigu.gmall.vo.search.SearchResponseAttrVo;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.ChildrenAggregation;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atguigu.gmall.vo.search.SearchResponse;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Brodie
 * @date 2020/1/9 - 15:20
 */
@Service
@Component
public class SearchProductServiceImpl implements SearchProductService {
    @Autowired
    JestClient jestClient;

    @Override
    public SearchResponse searchProduct(SearchParam searchParam)   {

        String dsl = buildDsl(searchParam);
        Search build = new Search.Builder(dsl).addIndex(EsConstant.PRODUCT_ES_INDEX)
                .addType(EsConstant.PRODUCT_INFO_ES_TYPE).build();
        SearchResult execute = null;
        try {
             execute = jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SearchResponse searchResponse = buildSearchResponse(execute);

        searchResponse.setPageNum(searchParam.getPageNum());
        searchResponse.setPageSize(searchParam.getPageSize());
        return searchResponse;
    }

    private SearchResponse buildSearchResponse(SearchResult execute) {
        SearchResponse searchResponse = new SearchResponse();
        MetricAggregation aggregations = execute.getAggregations();

        //品牌信息
        TermsAggregation brand_agg = aggregations.getTermsAggregation("brand_agg");
        List<String> brandNames = new ArrayList<>();
        brand_agg.getBuckets().forEach((bucket)->{
            String keyAsString = bucket.getKeyAsString();
            brandNames.add(keyAsString);
        });
        SearchResponseAttrVo attrVo = new SearchResponseAttrVo();
        attrVo.setName("品牌");
        attrVo.setValue(brandNames);
        searchResponse.setBrand(attrVo);

        //分类
        TermsAggregation category_agg = aggregations.getTermsAggregation("category_agg");
        List<String> categoryValues = new ArrayList<>();
        category_agg.getBuckets().forEach((bucket)->{
            String categoryName = bucket.getKeyAsString();
            TermsAggregation categoryId_agg = bucket.getTermsAggregation("categoryId_agg");
            String categoryId = categoryId_agg.getBuckets().get(0).getKeyAsString();

            Map<String,String> map = new HashMap<>();
            map.put("id",categoryId);
            map.put("name",categoryName);
            String cateInfo = JSON.toJSONString(map);
            categoryValues.add(cateInfo);
        });
        SearchResponseAttrVo catelog = new SearchResponseAttrVo();
        catelog.setName("分类");
        catelog.setValue(categoryValues);
        searchResponse.setCatelog(catelog);

        //属性
        TermsAggregation termsAggregation = aggregations.getChildrenAggregation("attr_agg")
                .getTermsAggregation("attrName_agg");
        List<SearchResponseAttrVo> attrList = new ArrayList<>();

        termsAggregation.getBuckets().forEach((bucket)->{
            SearchResponseAttrVo vo = new SearchResponseAttrVo();

            String attrName = bucket.getKeyAsString();
            vo.setName(attrName);

            TermsAggregation attrId_agg = bucket.getTermsAggregation("attrId_agg");
            String attrId = attrId_agg.getBuckets().get(0).getKeyAsString();
            vo.setProductAttributeId(Long.parseLong(attrId));


            TermsAggregation attrValue_agg = bucket.getTermsAggregation("attrValue_agg");
            List<String> valueList = new ArrayList<>();
            attrValue_agg.getBuckets().forEach((value)->{
                valueList.add(value.getKeyAsString());
            });
            vo.setValue(valueList);
            attrList.add(vo);
        });
        searchResponse.setAttrs(attrList);

        //商品数据
        List<SearchResult.Hit<EsProduct,Void>> hits = execute.getHits(EsProduct.class);
        List<EsProduct> esProducts = new ArrayList<>();
        hits.forEach((hit)->{
            EsProduct source = hit.source;
            String title = hit.highlight.get("skuProductInfos.skuTitle").get(0);
            source.setName(title);
            esProducts.add(source);
        });
        searchResponse.setProducts(esProducts);

        searchResponse.setTotal(execute.getTotal());
        return searchResponse;
    }

    private String buildDsl(SearchParam searchParam) {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //1 查询
        //1.1检索
        if(!StringUtils.isEmpty(searchParam.getKeyword())){
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("skuProductInfos.skuTitle", searchParam.getKeyword());
            NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("skuProductInfos", matchQuery, ScoreMode.None);
            boolQuery.must(nestedQuery);
        }
        //1.2过滤
        if(searchParam.getCatelog3()!=null&&searchParam.getCatelog3().length>0){
            //按照三级分类的条件过滤
            boolQuery.filter(QueryBuilders.termsQuery("productCategoryId",searchParam.getCatelog3()));
        }
        if(searchParam.getBrand()!=null&&searchParam.getBrand().length>0){
            //按照品牌的条件过滤 //有坑的地方brandName
            boolQuery.filter(QueryBuilders.termsQuery("brandName.keyword",searchParam.getBrand()));
        }
        //1.2.1属性过滤 品牌 分类
        if(searchParam.getProps()!=null&&searchParam.getProps().length>0){
            //按照属性的条件过滤
            String[] props = searchParam.getProps();
            for (String prop : props) {
                String[] split = prop.split(":");
                BoolQueryBuilder must = QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("attrValueList.productAttributeId", split[0]))
                        .must(QueryBuilders.termsQuery("attrValueList.value.keyword", split[1].split("-")));
                NestedQueryBuilder query = QueryBuilders.nestedQuery("attrValueList", must, ScoreMode.None);
                boolQuery.filter(query);
            }
        }

        if(searchParam.getPriceFrom()!=null||searchParam.getPriceTo()!=null){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            if(searchParam.getPriceFrom()!=null){
                rangeQuery.gte(searchParam.getPriceFrom());
            }
            if(searchParam.getPriceTo()!=null){
                rangeQuery.lte(searchParam.getPriceTo());
            }
            boolQuery.filter(rangeQuery);
        }



        builder.query(boolQuery);
        //2 高亮
        if(!StringUtils.isEmpty(searchParam.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuProductInfos.skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            builder.highlighter(highlightBuilder);
        }

        //3 聚合
        //品牌
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandName.keyword");
        brand_agg.subAggregation(AggregationBuilders.terms("brandId").field("brandId"));
        builder.aggregation(brand_agg);
        //分类
        TermsAggregationBuilder category_agg = AggregationBuilders.terms("category_agg").field("productCategoryName.keyword");
        category_agg.subAggregation(AggregationBuilders.terms("categoryId_agg").field("productCategoryId"));
        builder.aggregation(category_agg);
        //属性
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrValueList");
        TermsAggregationBuilder attrName_agg = AggregationBuilders.terms("attrName_agg").field("attrValueList.name");

        attrName_agg.subAggregation(AggregationBuilders.terms("attrValue_agg").field("attrValueList.value.keyword"));
        attrName_agg.subAggregation(AggregationBuilders.terms("attrId_agg").field("attrValueList.productAttributeId"));
        attr_agg.subAggregation(attrName_agg);
        builder.aggregation(attr_agg);

        //4 分页
        builder.from((searchParam.getPageNum()-1)*searchParam.getPageSize());
        builder.size(searchParam.getPageSize());

        //5 排序
        if(!StringUtils.isEmpty(searchParam.getOrder())){
            String order = searchParam.getOrder();
            String[] split = order.split(":");
            if(split[0].equals("0")){
                //默认
            }
            if(split[0].equals("1")){
                //销量
                FieldSortBuilder sale = SortBuilders.fieldSort("sale");
                if(split[1].equalsIgnoreCase("asc")){
                    sale.order(SortOrder.ASC);
                }else{
                    sale.order(SortOrder.DESC);
                }
                builder.sort(sale);
            }
            if(split[0].equals("2")){
                //价格
                FieldSortBuilder price = SortBuilders.fieldSort("price");
                if(split[1].equalsIgnoreCase("asc")){
                    price.order(SortOrder.ASC);
                }else{
                    price.order(SortOrder.DESC);
                }
                builder.sort(price);
            }
        }
        return builder.toString();
    }

}
