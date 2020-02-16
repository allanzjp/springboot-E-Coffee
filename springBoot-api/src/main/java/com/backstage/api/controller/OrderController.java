package com.backstage.api.controller;

import com.backstage.base.models.Orders;
import com.backstage.base.service.OrderService;
import com.backstage.result.ApiResultType;
import com.backstage.result.CommonResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
public class OrderController {

    @Resource
    private OrderService orderService;

    /**
     * @param type 0 商品销售额，1 商品发货日 2 客户销售额
     */
    @GetMapping(value = "/queryOrders")
    @ResponseBody
    public CommonResult queryOrders(String name, Integer type, Integer categoryId, Integer customerId, Long date) {
        try {
            List<Orders> orders = orderService.findAllOrdersInfo(type, categoryId, customerId, date);
            return CommonResult.Builder.SUCC().initSuccCodeAndMsg("0", "suc").initSuccData(orders);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    @GetMapping(value = "/pieChartOrderData", produces = "application/json;charset=utf-8")
    @ResponseBody
    public CommonResult pieChartOrderData(Integer type, Long date) {
        try {
            Map data = orderService.findPieChartOrderData(type, date);
            ApiResultType resultType = ApiResultType.SUCCESS;
            return CommonResult.Builder.SUCC().initSuccData(data)
                    .initSuccCodeAndMsg(resultType.getCode(), resultType.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    @GetMapping(value = "/findOrderCustomer", produces = "application/json;charset=utf-8")
    @ResponseBody
    public CommonResult findOrderCustomer() {
        try {
            List<Orders> data = orderService.findOrderCustomer();
            ApiResultType resultType = ApiResultType.SUCCESS;
            return CommonResult.Builder.SUCC().initSuccData(data)
                    .initSuccCodeAndMsg(resultType.getCode(), resultType.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    @PostMapping(value = "/placeOrders", produces = "application/json;charset=utf-8")
    @ResponseBody
    public CommonResult placeOrders(@RequestBody Orders orders) {
        try {
            orderService.insert(orders);
            ApiResultType resultType = ApiResultType.SUCCESS;
            return CommonResult.Builder.SUCC().initSuccCodeAndMsg(resultType.getCode(), resultType.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

}