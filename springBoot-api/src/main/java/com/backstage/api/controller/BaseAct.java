package com.backstage.api.controller;

import com.backstage.base.models.User;
import com.backstage.base.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

@Controller
@Scope("prototype")
public class BaseAct {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final String LOGIN_USER = "user";

    public static final String AUTH_STATUS = "1";

    public static final Integer SUCCESS_ONE = 1;        //操作成功

    public static final Integer EXCEPTION_ONE = -1;        //未登陆

    public static final Integer EXCEPTION_TWO = -2;        //操作失败

    public static final Integer EXCEPTION_THREE = -3;    //短信验证码或邮箱验证码不正确

    public static final Integer EXCEPTION_FOUR = -4;    //校验码不正确

    public static final Integer EXCEPTION_FIVE = -5;    //手机号已经注册

    public static final Integer EXCEPTION_SIX = -6;        //邮箱已经注册

    public static final Integer EXCEPTION_999 = 999;

    public static final Integer CURRENT_PAGE_SIZE = 5;

    public static final Integer Order_PAGE_SIZE = 10;

    public static final Integer MAX_NOTICE_COUNT = 20;  // 前端用户一次读取最近及置顶的最多20条重要公告

    public final static SimpleDateFormat yyyy_format = new SimpleDateFormat("yyyy");

    public final static SimpleDateFormat ym_format = new SimpleDateFormat("yyyy-MM");

    public final static SimpleDateFormat ym_format_x = new SimpleDateFormat("yyyy/MM");

    final static SimpleDateFormat ymd_format = new SimpleDateFormat("yyyy-MM-dd");

    final static SimpleDateFormat ymdhms_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final String TIME_START_STR = " 00:00:00";

    public static final String TIME_END_STR = " 23:59:59";

    private final static String SMS_VALID_CODE = "smsValidCode";

    public final static String MOBILE_AND_SMS_VALID_CODE = "smsValidCodeMobile";

    public final static String SEND_SMS_MOBILE_TIME = "sendSmsMobileAndTime";

    public final static String SEND_EMAIL_TIME = "sendEmailAndTime";

    public final static String USER_CAPITAL_PWORD_TIME = "userCapitalPwordTime";

    public final static String EMAIL_VALID_CODE = "emainValidCode";

    public final static String USER_MD5_KEY = "jys20170921";            //用于用户密码生成的固定串

    public final static String USER_BALANCE_SHOW_PRE = "user.balance.show.pre"; //用户总资产是否显示的前缀

    @Resource
    protected MessageSource messageSource;
    @Resource
    private UserService userService;

    String getRemoteAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null) {
            ip = request.getRemoteAddr();
        } else {
            StringTokenizer tokenizer = new StringTokenizer(ip, ",");
            for (int i = 0; i < tokenizer.countTokens() - 1; i++) {
                tokenizer.nextElement();
            }
            ip = tokenizer.nextToken().trim();
            if (ip.equals("")) {
                ip = null;
            }
        }
        if (ip == null) {
            ip = "0.0.0.0";
        }
        return ip;
    }

    /**
     * 验证码校验
     */
    public boolean checkAuthCode(String verificationCode, HttpServletRequest request) {
        String userCaptchaResponse = "";
        if (request.getSession().getAttribute("strRandom") != null) {
            userCaptchaResponse = request.getSession().getAttribute("strRandom").toString().toLowerCase();
            // 清除session中的验证码
            request.getSession().removeAttribute("strRandom");
            if (!userCaptchaResponse.equals(verificationCode.toLowerCase())) {
                // 校验码不正确 -4
                return false;
            } else {
                return true;
            }
        } else {
            // 校验码过期 -4
            return false;
        }
    }

    /**
     * 短信验证码校验
     */
    public boolean checkSMSValidateCode(String smsValidateCode, HttpServletRequest request) {
        if (request.getSession().getAttribute(SMS_VALID_CODE) != null) {
            String smsCode = request.getSession().getAttribute(SMS_VALID_CODE).toString();
            if (!smsValidateCode.equals(smsCode)) {
                //短信验证码不正确
                return false;
            } else {
                request.getSession().removeAttribute(SMS_VALID_CODE);
                return true;
            }
        } else {
            //短信验证码过期
            return false;
        }
    }

    /**
     * 带手机号的短信验证码校验
     */
    public boolean checkSMSValidateCode(String mobileNumber, String smsValidateCode, HttpServletRequest request) {
        if (request.getSession().getAttribute(SMS_VALID_CODE + mobileNumber) != null) {
            String smsCode = request.getSession().getAttribute(SMS_VALID_CODE + mobileNumber).toString();
            if (!smsValidateCode.equals(smsCode)) {
                //短信验证码不正确
                return false;
            } else {
                request.getSession().removeAttribute(SMS_VALID_CODE + mobileNumber);
                return true;
            }
        } else {
            return false;
        }
    }


}
