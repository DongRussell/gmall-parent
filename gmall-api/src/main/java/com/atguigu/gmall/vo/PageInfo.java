package com.atguigu.gmall.vo;



import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Brodie
 * @date 2020/1/5 - 12:14
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel
public class PageInfo implements Serializable {
        @ApiModelProperty("总记录数")
        private Long total;

        @ApiModelProperty("总页数")
        private Long totalPage;

        @ApiModelProperty("每页显示的记录数")
        private Long pageSize;

        @ApiModelProperty("分页查出的数据")
        private List<? extends Object> list;

        @ApiModelProperty("当前页的页码")
        private Long pageNum;

        public static PageInfo getVo(IPage iPage , Long size ){
             return new PageInfo(iPage.getTotal(),
                                 iPage.getPages(),
                                 size,
                                 iPage.getRecords(),
                                 iPage.getCurrent()
             );
        }


}
