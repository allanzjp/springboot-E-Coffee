package com.backstage.api.controller;

import com.backstage.base.models.Token;
import com.backstage.base.models.User;
import com.backstage.base.service.TokenService;
import com.backstage.base.service.UserService;
import com.backstage.base.service.ValidCodeApiService;
import com.backstage.util.MD5Util;
import com.backstage.util.PropertyUtil;
import com.backstage.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

@Controller
public class BaseApiAct extends BaseAct {
    static final Logger logger = LoggerFactory.getLogger(BaseApiAct.class);

    // token生成固定串定义
    private static final String MD5_TOKEN_STRING = "vende@2019";

    @Resource
    private TokenService tokenService;
    @Resource
    private UserService userService;
    @Resource
    private ValidCodeApiService validCodeApiService;
    @Resource
    private RedisTemplate<Serializable, Serializable> redisTemplate;


    /**
     * 通过token 获取用户信息
     */
    User getUser(String token) {
        Token tokenObject = tokenService.findByToken(token);
        if (tokenObject == null) {
            return null;
        }
        return userService.findById(tokenObject.getUserId());
    }

    /**
     * 获取token
     */
    Map<String, Object> getToken(User user) {
        try {
            //生成token
            String token = MD5Util.getMD5(MD5_TOKEN_STRING
                    + user.getId().toString() + System.currentTimeMillis() + Math.random());
            Calendar calendar = Calendar.getInstance();

            //添加token到数据库
            Token t = new Token();
            t.setUserId(user.getId());
            t.setToken(token);
            t.setCtime(calendar.getTime());
            t.setMtime(calendar.getTime());
            tokenService.removeToken(user.getId());
            tokenService.add(t);

            calendar.add(Calendar.DATE, 7);
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("expire", calendar.getTime());
            map.put("mobileNumber", StringUtil.hideMobileNumber(user.getMobileNumber()));
            map.put("user", user);
//            map.put("userId", user.getId());
//            map.put("nickname", user.getNickname());
//            map.put("photo", user.getPhoto());
//            map.put("username", user.getUsername());
//            map.put("signature", user.getSignature());

            return map;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 检查token是否有效
     */
    boolean checkToken(String token) {
        if (token == null || token.isEmpty()) {
            return true;
        }

        Integer timeout = Integer.parseInt(PropertyUtil.getProperties("token_timeout"));
        Token t = tokenService.checkToken(token, timeout);
        if (t != null) {
            t.setMtime(new Date());
            tokenService.updateToken(t);//更新token
            return false;
        }
        return true;
    }

    /**
     * 签名校验
     */
    boolean checkSign(Map<String, Object> params, String sign) {

        // todo 当前暂时不做签名校验
        if(StringUtils.isNotBlank(sign)){
			return false;
		}

        if (StringUtils.isBlank(sign)) {
            return true;
        }

        String secret = PropertyUtil.getProperties("sign_secret");
        // 先将参数以其参数名的字典序升序进行排序
        Map<String, Object> sortedParams = new TreeMap<>(params);
        Set<Entry<String, Object>> entrys = sortedParams.entrySet();

        // 遍历排序后的字典，将所有参数按"key=value"格式拼接在一起
        StringBuilder baseString = new StringBuilder();
        for (Entry<String, Object> param : entrys) {
            if (param.getKey().equals("sign")) {//去掉签名字段
                continue;
            }
            baseString.append(param.getKey());
            if (param.getValue() != null) {
                baseString.append(param.getValue().toString());
            }
        }
        baseString.append(secret);
        logger.info("basestring = " + baseString.toString());
        // 使用MD5对待签名串求签
        String curSign = MD5Util.getMD5(baseString.toString());
        logger.info("test basesign={}==============sign={}, serverSign={}", baseString.toString(), sign, curSign);
        return !curSign.equals(sign);
    }


    /**
     * 短信验证码校验
     */
    boolean checkSMSValidateCode(String country, String mobile, String code) {
        try {
            if(StringUtils.isNotBlank(code)) {
                return false;
            }
            String smsCode = validCodeApiService.getSmsCode(country, mobile);

            int checkTime = 0;
            if (!StringUtils.isBlank(this.get(country + mobile + smsCode))) {
                checkTime = Integer.parseInt(this.get(country + mobile + smsCode));
                if (checkTime >= 10) {
                    validCodeApiService.removeSmsCode(country, mobile);
                    return true;
                }
            }
            logger.info("checkSMSValidateCode param_code = {},redis_code = {}", code, smsCode);
            if (smsCode != null && smsCode.equals(code)) {
                validCodeApiService.removeSmsCode(country, mobile);
                return false;
            } else {
                int newCheckTime = checkTime + 1;
                this.set(country + mobile + smsCode, Integer.toString(newCheckTime));
                //短信验证码不正确
                return true;
            }
        } catch (Exception e) {
            logger.info("checkSMSValidateCode Exception = {}", e);
            return true;
        }
    }


    /**
     * 向redis中写值
     */
    private void set(String redisKey, String value) {
        redisTemplate.execute((RedisCallback<String>) connection -> {
            connection.set(redisKey.getBytes(), value.getBytes());
            connection.expire(redisKey.getBytes(), 300L);
            return null;
        });
    }

    /**
     * 从redis中取值
     */
    private String get(String redisKey) {
        return redisTemplate.execute((RedisCallback<String>) connection -> {
            byte[] key = redisKey.getBytes();
            byte[] value = connection.get(key);
            if (value == null) {
                return null;
            }
            return new String(value);
        });
    }
}
