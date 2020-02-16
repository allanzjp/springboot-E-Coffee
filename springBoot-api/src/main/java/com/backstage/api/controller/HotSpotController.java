package com.backstage.api.controller;

import com.backstage.base.models.HotSpot;
import com.backstage.base.service.HotSpotService;
import com.backstage.result.ApiResultType;
import com.backstage.result.CommonResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@CrossOrigin
@RestController
public class HotSpotController {

    @Resource
    private HotSpotService hotSpotService;

    @GetMapping(value = "/queryHotSpot")
    @ResponseBody
    public CommonResult query(String name) {
        try {
            List<HotSpot> plans = hotSpotService.queryAll();
            return CommonResult.Builder.SUCC().initSuccCodeAndMsg("0", "suc").initSuccData(plans);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

//    @PostMapping(value = "/addMyPlan", produces = "application/json;charset=utf-8")
//    @ResponseBody
//    public CommonResult insert(@RequestBody HotSpot hotSpot) {
//        try {
//            hotSpotService.insert(hotSpot);
//            ApiResultType resultType = ApiResultType.SUCCESS;
//            return CommonResult.Builder.SUCC().initSuccCodeAndMsg(resultType.getCode(), resultType.getMessage());
//        } catch (Exception e) {
//            e.printStackTrace();
//            ApiResultType resultType = ApiResultType.SYS_ERROR;
//            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
//        }
//    }

}
