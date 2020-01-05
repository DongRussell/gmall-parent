package com.atguigu.gmall.admin.ums.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.to.CommonResult;
import com.atguigu.gmall.ums.entity.MemberLevel;
import com.atguigu.gmall.ums.service.MemberLevelService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Brodie
 * @date 2020/1/5 - 21:55
 */

@CrossOrigin
@RestController
public class UmsMemberLevelController {
    @Reference
    MemberLevelService memberLevelService;
    /*
        查出所有等级信息
    */

    @GetMapping("/memberLevel/list")
    public Object memeberLevelList(){
        List<MemberLevel> list = memberLevelService.list();
        return new CommonResult().success(list);
    }
}
