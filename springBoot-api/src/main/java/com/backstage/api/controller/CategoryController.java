package com.backstage.api.controller;

import com.backstage.base.models.Category;
import com.backstage.base.models.Commodity;
import com.backstage.base.service.CategoryService;
import com.backstage.base.service.CommodityService;
import com.backstage.result.ApiResultType;
import com.backstage.result.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(description = "商品管理接口")
@CrossOrigin
@RestController
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @GetMapping(value = "/queryCategories")
    @ResponseBody
    public CommonResult query(String name) {
        try {
            List<Category> categories = categoryService.queryCategory(name);
            return CommonResult.Builder.SUCC().initSuccCodeAndMsg("0", "suc").initSuccData(categories);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    @PostMapping(value = "/addCategory", produces = "application/json;charset=utf-8")
    @ResponseBody
    public CommonResult insert(@RequestBody Category category) {
        try {
            categoryService.insert(category);
            ApiResultType resultType = ApiResultType.SUCCESS;
            return CommonResult.Builder.SUCC().initSuccCodeAndMsg(resultType.getCode(), resultType.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

}