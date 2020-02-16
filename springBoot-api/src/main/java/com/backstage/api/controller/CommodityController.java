package com.backstage.api.controller;

import com.backstage.base.models.Commodity;
import com.backstage.base.service.CommodityService;
import com.backstage.result.ApiResultType;
import com.backstage.result.CommonResult;
import com.backstage.util.FileUtil;
import com.backstage.util.PropertyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@SuppressWarnings("Duplicates")
@Api(description = "商品管理接口")
@CrossOrigin
@RestController
public class CommodityController {

    @Resource
    private CommodityService commodityService;

    @ApiOperation(value = "获取商品列表", notes = "notes", httpMethod = "GET")
    @GetMapping(value = "/queryCommodities")
    @ResponseBody
    public CommonResult query(String name, String type) {
        try {
            List<Commodity> commodities = commodityService.queryCommodity(name, type);
            return CommonResult.Builder.SUCC().initSuccCodeAndMsg("0", "suc").initSuccData(commodities);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    @GetMapping(value = "/queryCommodityById")
    @ResponseBody
    public CommonResult queryById(Integer id) {
        try {
            Commodity commodity = commodityService.queryById(id);
            return CommonResult.Builder.SUCC().initSuccCodeAndMsg("0", "suc").initSuccData(commodity);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    @PostMapping(value = "/modifyCommodity")
    @ResponseBody
    public Object edit(MultipartFile file, Commodity commodity) {
        try {
            if(file != null) {
                String filePath = PropertyUtil.getProperties("commodityImgFilePath");
                String baseUrl = PropertyUtil.getProperties("commodityImgBaseUrl");
                String fileName = FileUtil.writeToLocal(file, filePath);
                String imgUrl = baseUrl + fileName;
                commodity.setImgUrl(imgUrl);
            }
            if (commodity != null) {
                commodityService.update(commodity);
            }
            return CommonResult.Builder.SUCC().initSuccCodeAndMsg("0", "suc");
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    @ApiOperation(value = "录入商品信息", notes = "notes", httpMethod = "POST")
    @PostMapping(value = "/insertCommodity")
    @ResponseBody
    public CommonResult insert(MultipartFile file, Commodity commodity) {
        try {
            String filePath = PropertyUtil.getProperties("commodityImgFilePath");
            String baseUrl = PropertyUtil.getProperties("commodityImgBaseUrl");
            String fileName = FileUtil.writeToLocal(file, filePath);

            if(StringUtils.isNotBlank(fileName)) {
                String imgUrl = baseUrl + fileName;
                commodity.setImgUrl(imgUrl);
                commodityService.insert(commodity);
                ApiResultType resultType = ApiResultType.SUCCESS;
                return CommonResult.Builder.SUCC().initSuccData(commodity)
                        .initSuccCodeAndMsg(resultType.getCode(), resultType.getMessage());
            } else {
                ApiResultType resultType = ApiResultType.IMG_UPLOAD_FAIL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

}