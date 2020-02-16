package com.backstage.api.controller;

import com.backstage.base.models.Commodity;
import com.backstage.base.models.User;
import com.backstage.base.models.UserLoginFail;
import com.backstage.base.service.TokenService;
import com.backstage.base.service.UserLoginFailService;
import com.backstage.base.service.UserService;
import com.backstage.constant.Constants;
import com.backstage.result.ApiResultType;
import com.backstage.result.CommonResult;
import com.backstage.util.FileUtil;
import com.backstage.util.MD5Util;
import com.backstage.util.PropertyUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
@Controller
public class UserController extends BaseApiAct {

    @Resource
    private UserService userService;
    @Resource
    private TokenService tokenService;
    @Resource
    private UserLoginFailService userLoginFailService;


    /**
     * 手机登录
     */
    @PostMapping(value = "/login")
    @ResponseBody
    public CommonResult login(@RequestBody Map<String, String> requestMap) {
        try {
            String country = requestMap.get("country");
//            String mobile = requestMap.get("mobile");
            String password = requestMap.get("password");
            String sign = requestMap.get("sign");
            String time = requestMap.get("time");
            String username = requestMap.get("username");
            if (StringUtils.isBlank(country) || StringUtils.isBlank(username)
                    || StringUtils.isBlank(password) || time == null) {
                ApiResultType resultType = ApiResultType.PARAMETER_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            Map<String, Object> params = new HashMap<>();
            params.put("country", country);
//            params.put("mobile", mobile);
            params.put("password", password);
            params.put("time", time);
            params.put("username", username);
            if (checkSign(params, sign)) { // sign 验证
                ApiResultType resultType = ApiResultType.PARAMETER_SIGN_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            User user = new User();
//            user.setCountryCode(country);
//            user.setMobileNumber(mobile);
            user.setUsername(username);
            user.setLoginPword(password);
            if (!userService.findUserExisted(user.getCountryCode(), user.getMobileNumber(), username)) {
                // 用户不存在
                ApiResultType resultType = ApiResultType.USER_NOT_EXIST;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            // 登录失败次数校验
            int maxLoginCount = Integer.parseInt(PropertyUtil.getProperties("system_max_login_fail_count"));
            String mobileCode = user.getCountryCode() + user.getMobileNumber();
            UserLoginFail loginFail = userLoginFailService.find(mobileCode);
            if (loginFail != null && loginFail.getCount() >= maxLoginCount) {
                // 用户被锁定
                ApiResultType resultType = ApiResultType.USER_LOCKED_2HOURS;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            User loginUser = userService.login(user);
            if (loginUser != null) { // 登陆成功
                userLoginFailService.del(username);// 清除登录失败信息
                Map<String, Object> data = getToken(loginUser);
                ApiResultType resultType = ApiResultType.SUCCESS;
                return CommonResult.Builder.SUCC().initSuccData(data).
                        initSuccCodeAndMsg(resultType.getCode(), resultType.getMessage());
            } else { // 用户名或密码错误
                loginFail = userLoginFailService.add(username);
                if (loginFail.getCount() >= maxLoginCount) {// 账户已被锁定2小时
                    ApiResultType resultType = ApiResultType.USER_LOCKED_2HOURS;
                    return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
                } else {
                    // 用户名或密码错误,您还有%s次机会
                    ApiResultType resultType = ApiResultType.USER_PASSWORD_ERROR;
                    String msg = String.format(resultType.getMessage(), maxLoginCount - loginFail.getCount());
                    return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    /**
     * 退出登陆
     */
    @GetMapping(value = "/logout")
    @ResponseBody
    public CommonResult logout(String token, Long time, String sign) {
        try {

            if (StringUtils.isBlank(token) || time == null) {
                ApiResultType resultType = ApiResultType.PARAMETER_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            Map<String, Object> params = new HashMap<>();
            params.put("token", token);
            params.put("time", time);
            if (checkSign(params, sign)) { // sign 验证
                ApiResultType resultType = ApiResultType.PARAMETER_SIGN_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            if (checkToken(token)) { // token 验证
                ApiResultType resultType = ApiResultType.PARAMETER_TOKEN_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }
            User user = getUser(token);

            tokenService.removeToken(user.getId());
            ApiResultType resultType = ApiResultType.SUCCESS;
            return CommonResult.Builder.SUCC().initSuccCodeAndMsg(resultType.getCode(), resultType.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    /**
     * 手机注册
     */
    @PostMapping(value = "/register", produces = "application/json;charset=utf-8")
    @ResponseBody
    public CommonResult register(@RequestBody Map<String, String> requestMap) {
        try {
            String country = requestMap.get("country");
            String mobile = requestMap.get("mobile");
            String username = requestMap.get("username");
            String password = requestMap.get("password");
            String verifyCode = requestMap.get("verifyCode");
            String sign = requestMap.get("sign");
            String time = requestMap.get("time");

            if (StringUtils.isBlank(country) || StringUtils.isBlank(mobile) || StringUtils.isBlank(verifyCode)
                    || StringUtils.isBlank(username) || StringUtils.isBlank(password) || time == null) { // 参数校验
                ApiResultType resultType = ApiResultType.PARAMETER_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            Map<String, Object> params = new HashMap<>();
            params.put("country", country);
            params.put("mobile", mobile);
            params.put("username", username);
            params.put("password", password);
            params.put("verifyCode", verifyCode);
            params.put("time", time);
            if (checkSign(params, sign)) { // sign 验证
                ApiResultType resultType = ApiResultType.PARAMETER_SIGN_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            // 短信验证码校验
            if (this.checkSMSValidateCode(country, mobile, verifyCode)) {
                ApiResultType resultType = ApiResultType.SMSCODE_FAIL; // 验证码错误
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }
            User user = new User();
            user.setCountryCode(country);
            user.setMobileNumber(mobile);
            user.setLoginPword(password);
            user.setUsername(username);
            user = userService.registerPhoneSubmit(user);
            if (user != null) { // 注册成功, 设置登陆信息
                Map<String, Object> data = getToken(user);
                ApiResultType resultType = ApiResultType.SUCCESS;
                return CommonResult.Builder.SUCC().initSuccData(data).initSuccCodeAndMsg(resultType.getCode(),
                        resultType.getMessage());
            } else { // 该手机已经注册
                ApiResultType resultType = ApiResultType.USER_NAME_EXIST;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    /**
     * 找回密码提交(手机)
     */
    @PostMapping(value = "/resetPasswordByPhone", produces = "application/json;charset=utf-8")
    @ResponseBody
    public CommonResult resetPasswordByPhone(@RequestBody Map<String, String> requestMap) {
        try {
            String country = requestMap.get("country");
            String mobile = requestMap.get("mobile");
            String password = requestMap.get("password");
            String verifyCode = requestMap.get("verifyCode");
            String sign = requestMap.get("sign");
            String time = requestMap.get("time");

            if (StringUtils.isBlank(country) || StringUtils.isBlank(mobile) || StringUtils.isBlank(verifyCode)
                    || StringUtils.isBlank(password) || time == null) { // 参数校验
                ApiResultType resultType = ApiResultType.PARAMETER_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            Map<String, Object> params = new HashMap<>();
            params.put("country", country);
            params.put("verifyCode", verifyCode);
            params.put("loginPwd", password);
            params.put("mobile", mobile);
            params.put("time", time);
            if (checkSign(params, sign)) { // sign 验证
                ApiResultType resultType = ApiResultType.PARAMETER_SIGN_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            // 短信验证码校验
            if (this.checkSMSValidateCode(country, mobile, verifyCode)) {
                ApiResultType resultType = ApiResultType.SMSCODE_FAIL; // 验证码错误
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            country = "+" + country;
            User loginUser = userService.resetPasswordSubmitByMobile(mobile, country, password);
            if (loginUser != null) {//找回密码成功
                ApiResultType resultType = ApiResultType.SUCCESS;
                return CommonResult.Builder.SUCC().initSuccCodeAndMsg(resultType.getCode(), resultType.getMessage());
            } else {
                //找回密码失败 展示系统异常
                ApiResultType resultType = ApiResultType.MODIFY_LOGIN_ERROR;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("==============trace={}", e.getMessage());
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    /**
     * 修改手机号码
     */
    @RequestMapping(value = "/user/modifyBindMobile",
            method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public CommonResult modifyBindMobile(@RequestBody Map<String, String> requestMap) {
        try {
            String country = requestMap.get("country");
            String mobile = requestMap.get("mobile");
            String password = requestMap.get("password");
            String verifyCodeOld = requestMap.get("verifyCodeOld");
            String verifyCodeNew = requestMap.get("verifyCodeNew");
            String token = requestMap.get("token");
            String time = requestMap.get("time");
            String sign = requestMap.get("sign");

            // 参数校验
            if (StringUtils.isBlank(country) || StringUtils.isBlank(mobile) || StringUtils.isBlank(verifyCodeOld)
                    || StringUtils.isBlank(verifyCodeNew) || StringUtils.isBlank(token) || time == null) {
                ApiResultType resultType = ApiResultType.PARAMETER_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            // sign 验证
            Map<String, Object> params = new HashMap<>();
            params.put("country", country);
            params.put("mobile", mobile);
            params.put("verifyCodeOld", verifyCodeOld);
            params.put("verifyCodeNew", verifyCodeNew);
            params.put("token", token);
            params.put("time", time);
            if (checkSign(params, sign)) {
                ApiResultType resultType = ApiResultType.PARAMETER_SIGN_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            // token 验证
            if (checkToken(token)) {
                ApiResultType resultType = ApiResultType.PARAMETER_TOKEN_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }
            User user = getUser(token);

            // 旧手机短信验证码校验
            if (!this.checkSMSValidateCode(user.getCountryCode().trim()
                    .replace("+", ""), user.getMobileNumber(), verifyCodeOld)) {
                ApiResultType resultType = ApiResultType.SMSCODE_FAIL; // 旧手机验证码输入错误
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            // 新手机短信验证码校验
            if (this.checkSMSValidateCode(country, mobile, verifyCodeNew)) {
                ApiResultType resultType = ApiResultType.SMSCODE_FAIL; // 新手机验证码错误
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            User loginUser = userService.changeUserMobileNumber(user, country, mobile);
            if (loginUser != null) { // 手机号码修改成功
                ApiResultType resultType = ApiResultType.SUCCESS;
                return CommonResult.Builder.SUCC().initSuccCodeAndMsg(resultType.getCode(), resultType.getMessage());
            } else {
                // 手机号码已注册
                ApiResultType resultType = ApiResultType.USER_MOBILE_EXIST;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping(value = "/user/me", produces = "application/json;charset=utf-8")
    @ResponseBody
    public CommonResult me(String token, Long time, String sign) {
        try {
            if (StringUtils.isBlank(token) || time == null) {
                ApiResultType resultType = ApiResultType.PARAMETER_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            Map<String, Object> params = new HashMap<>();
            params.put("token", token);
            params.put("time", time);
            if (checkSign(params, sign)) { // sign 验证
                ApiResultType resultType = ApiResultType.PARAMETER_SIGN_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            if (checkToken(token)) { // token 验证
                ApiResultType resultType = ApiResultType.PARAMETER_TOKEN_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }
            User curUser = getUser(token);
//            Map<String, Object> data = new HashMap<>();
//            //添加user
//            JSONObject user = new JSONObject();
//            user.put("userId", curUser.getId());
//            user.put("userName", curUser.getNickname());
//            user.put("mobile", curUser.getMobileNumber());
//            user.put("email", curUser.getEmail());
//            String countryCode = "";
//            if (!curUser.getCountryCode().equals("")) {
//                countryCode = curUser.getCountryCode().replace("+", "");
//            }
//            user.put("countryCode", countryCode);

            ApiResultType resultType = ApiResultType.SUCCESS;
            return CommonResult.Builder.SUCC().initSuccData(curUser)
                    .initSuccCodeAndMsg(resultType.getCode(), resultType.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    /**
     * 修改登陆密码
     */
    @PostMapping(value = "/user/modifyLoginPassword", produces = "application/json;charset=utf-8")
    @ResponseBody
    public CommonResult modifyLoginPassword(@RequestBody Map<String, String> requestMap) {
        try {
            String passwordOld = requestMap.get("passwordOld");
            String passwordNew = requestMap.get("passwordNew");
            String verifyCode = requestMap.get("verifyCode");
            String token = requestMap.get("token");
            String time = requestMap.get("time");
            String sign = requestMap.get("sign");

            //首先验证参数是否存在
            if (passwordOld == null || passwordNew == null || StringUtils.isBlank(token)
                    || StringUtils.isBlank(verifyCode) || time == null) {
                ApiResultType resultType = ApiResultType.PARAMETER_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            Map<String, Object> params = new HashMap<>();
            params.put("passwordOld", passwordOld);
            params.put("passwordNew", passwordNew);
            params.put("verifyCode", verifyCode);
            params.put("token", token);
            params.put("time", time);
            if (checkSign(params, sign)) { // sign 验证
                ApiResultType resultType = ApiResultType.PARAMETER_SIGN_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            if (checkToken(token)) { // token 验证
                ApiResultType resultType = ApiResultType.PARAMETER_TOKEN_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }
            User user = getUser(token);

            //短信验证
//            if (!this.checkSMSValidateCode(user.getCountryCode().trim()
//                    .replace("+", ""), user.getMobileNumber(), verifyCode)) {
//                ApiResultType resultType = ApiResultType.SMSCODE_FAIL; // 验证码错误
//                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
//            }

            User loginUser = userService.modifyLoginPasswordSave(user, passwordOld, passwordNew);
            if (loginUser == null) {
                //旧密码不对 -1
                ApiResultType resultType = ApiResultType.OLD_LOGIN_PWD_ERROR;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            ApiResultType resultType = ApiResultType.SUCCESS;
            return CommonResult.Builder.SUCC().initSuccCodeAndMsg(resultType.getCode(), resultType.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    @PostMapping(value = "/user/checkPassword", produces = "application/json;charset=utf-8")
    @ResponseBody
    public CommonResult checkPassword(@RequestBody Map<String, String> requestMap) {
        try {
            String password = requestMap.get("password");
            String token = requestMap.get("token");
            String time = requestMap.get("time");
            String sign = requestMap.get("sign");

            //首先验证参数是否存在
            if (password == null || StringUtils.isBlank(token) || time == null) {
                ApiResultType resultType = ApiResultType.PARAMETER_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            Map<String, Object> params = new HashMap<>();
            params.put("password", password);
            params.put("token", token);
            params.put("time", time);
            if (checkSign(params, sign)) { // sign 验证
                ApiResultType resultType = ApiResultType.PARAMETER_SIGN_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

            if (checkToken(token)) { // token 验证
                ApiResultType resultType = ApiResultType.PARAMETER_TOKEN_ILLEGAL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }
            User user = getUser(token);
            password = MD5Util.getMD5(Constants.USER_MD5_KEY + user.getId().toString() + password);
            logger.info("check_password param password = {}", password);
            if (user.getLoginPword().equals(password)) {
                ApiResultType resultType = ApiResultType.SUCCESS;
                return CommonResult.Builder.SUCC().initSuccCodeAndMsg(resultType.getCode(), resultType.getMessage());
            } else {
                ApiResultType resultType = ApiResultType.USER_TOKEN_PASSWORD_ERROR;
                return CommonResult.Builder.SUCC().initSuccCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    /**
     * 修改个人信息
     */
    @PostMapping(value = "/personalSetting", produces = "application/json;charset=utf-8")
    @ResponseBody
    public CommonResult personalSetting(MultipartFile file, User user) {
        try {
            String fileDir = PropertyUtil.getProperties("photoFilePath");
            String filePath = PropertyUtil.getProperties("baseImgFilePath") + fileDir;
            if(file != null) {
                String fileName = FileUtil.writeToLocal(file, filePath);
                String imgUrl = fileDir + fileName;
                user.setPhoto(imgUrl);
            }

            userService.updateByPrimaryKeySelective(user);
            ApiResultType resultType = ApiResultType.SUCCESS;
            return CommonResult.Builder.SUCC().initSuccData(user)
                    .initSuccCodeAndMsg(resultType.getCode(), resultType.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            logger.info("==============trace={}", e.getMessage());
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }



}
