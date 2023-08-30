package com.jeequan.jeepay.agent.ctrl.anon;


import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.agent.ctrl.CommonCtrl;
import com.jeequan.jeepay.agent.service.AuthService;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.SysUserAuth;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.GoogleAuthUtil;
import com.jeequan.jeepay.service.impl.SysConfigService;
import com.jeequan.jeepay.service.impl.SysUserAuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * 登录鉴权
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@Slf4j
@RestController
@RequestMapping("/api/anon/auth")
public class AuthController extends CommonCtrl {

    @Autowired
    private AuthService authService;

    @Autowired
    private SysUserAuthService sysUserAuthService;

    @Autowired
    private SysConfigService sysConfigService;

    /**
     * 用户信息认证 获取iToken
     **/
    @RequestMapping(value = "/validate", method = RequestMethod.POST)
//    @MethodLog(remark = "登录认证")
    public ApiRes validate() throws BizException {

        String account = Base64.decodeStr(getValStringRequired("ia"));  //用户名 i account, 已做base64处理
        String ipassport = Base64.decodeStr(getValStringRequired("ip"));    //密码 i passport,  已做base64处理
        String vercode = Base64.decodeStr(getValStringRequired("vc"));    //验证码 vercode,  已做base64处理
        String vercodeToken = Base64.decodeStr(getValStringRequired("vt"));    //验证码token, vercode token ,  已做base64处理
        String googleCodeStr = Base64.decodeStr(getValString("gc"));    //google code ,  已做base64处理

        String cacheCode = RedisUtil.getString(CS.getCacheKeyImgCode(vercodeToken));
        if (StringUtils.isEmpty(cacheCode) || !cacheCode.equalsIgnoreCase(vercode)) {
            throw new BizException("验证码有误！");
        }

        SysUserAuth sysUserAuth = sysUserAuthService.selectByLogin(account, CS.AUTH_TYPE.LOGIN_USER_NAME, CS.SYS_TYPE.AGENT);
        if (sysUserAuth == null) {
            //清除之前的登录信息 单点登录
            throw new BizException("登录名或密码错误！");
        }
//        authService.delAuthentication(Arrays.asList(sysUserAuth.getUserId()));
        boolean needGoogleKey = sysUserAuth.getGoogleAuthStatus() == CS.YES;

        if (needGoogleKey) {
            int googleCode;
            boolean isVerify = false;
            try {
                googleCode = Integer.parseInt(googleCodeStr);
                isVerify = GoogleAuthUtil.CheckCode(sysUserAuth.getGoogleAuthKey(), googleCode);
            } catch (Exception e) {
                throw new BizException("谷歌验证码有误！");
            }
            if (!isVerify) {
                throw new BizException("谷歌验证码有误！");
            }
        }

// 返回前端 accessToken
        String accessToken = authService.auth(account, ipassport);
// 删除图形验证码缓存数据
        RedisUtil.del(CS.getCacheKeyImgCode(vercodeToken));
        return ApiRes.ok4newJson(CS.ACCESS_TOKEN_NAME, accessToken);
    }

    /**
     * 图片验证码
     **/
    @RequestMapping(value = "/vercode", method = RequestMethod.GET)
    public ApiRes vercode() throws BizException {

        //定义图形验证码的长和宽 // 4位验证码
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(137, 40, 4, 80);
        RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
        lineCaptcha.setGenerator(randomGenerator);
        lineCaptcha.createCode(); //生成code

        //redis
        String vercodeToken = UUID.fastUUID().toString();
        RedisUtil.setString(CS.getCacheKeyImgCode(vercodeToken), lineCaptcha.getCode(), CS.VERCODE_CACHE_TIME); //图片验证码缓存时间: 1分钟

        JSONObject result = new JSONObject();
        result.put("imageBase64Data", lineCaptcha.getImageBase64Data());
        result.put("vercodeToken", vercodeToken);
        result.put("expireTime", CS.VERCODE_CACHE_TIME);

        return ApiRes.ok(result);
    }

    @RequestMapping(value = "/getTitle", method = RequestMethod.GET)
    public ApiRes getTitle() throws BizException {
        return ApiRes.ok(sysConfigService.getDBApplicationConfig().getPlatName());
    }

}