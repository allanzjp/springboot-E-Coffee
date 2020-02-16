package com.backstage.api.controller;

import com.backstage.base.models.Customers;
import com.backstage.base.service.CustomersService;
import com.backstage.result.ApiResultType;
import com.backstage.result.CommonResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@CrossOrigin
@RestController
public class CustomersController {

    @Resource
    private CustomersService customersService;

    @GetMapping(value = "/queryCustomers")
    @ResponseBody
    public CommonResult query(String name) {
        try {
            List<Customers> customers = customersService.queryAll();
            return CommonResult.Builder.SUCC().initSuccCodeAndMsg("0", "suc").initSuccData(customers);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    @GetMapping(value = "/queryCustomerById")
    @ResponseBody
    public CommonResult queryById(Integer id) {
        try {
            Customers customer = customersService.queryById(id);
            return CommonResult.Builder.SUCC().initSuccCodeAndMsg("0", "suc").initSuccData(customer);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    @PostMapping(value = "/modifyCustomer")
    @ResponseBody
    public Object edit( @RequestBody Customers customer) {
        try {
            if (customer != null) {
                customersService.update(customer);
            }
            return CommonResult.Builder.SUCC().initSuccCodeAndMsg("0", "suc");
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    @PostMapping(value = "/addCustomer", produces = "application/json;charset=utf-8")
    @ResponseBody
    public CommonResult insert(@RequestBody Customers customers) {
        try {
            customersService.insert(customers);
            ApiResultType resultType = ApiResultType.SUCCESS;
            return CommonResult.Builder.SUCC().initSuccData(customers)
                    .initSuccCodeAndMsg(resultType.getCode(), resultType.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

}