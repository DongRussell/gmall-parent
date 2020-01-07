package com.atguigu.gmall.to.es;

import com.atguigu.gmall.pms.entity.SkuStock;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Brodie
 * @date 2020/1/7 - 12:56
 */
@Data
public class EsSkuProductInfo extends SkuStock implements Serializable{
    private String skuTitle;
    private List<EsProductAttributeValue> attributeValues;
}
