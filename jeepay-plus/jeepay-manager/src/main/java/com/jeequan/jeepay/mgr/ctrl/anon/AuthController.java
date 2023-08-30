/*
 * Copyright (c) 2021-2031
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.mgr.ctrl.anon;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.components.mq.model.PayOrderForceSuccessMQ;
import com.jeequan.jeepay.components.mq.model.StatisticsOrderMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.SysUserAuth;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.GoogleAuthUtil;
import com.jeequan.jeepay.core.utils.StringKit;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.mgr.service.AuthService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import com.jeequan.jeepay.service.impl.SysUserAuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/*
 * 认证接口
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/6/8 17:09
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
    @MethodLog(remark = "登录认证")
    @Transactional(rollbackFor = Exception.class)
    public ApiRes validate() throws BizException {
        String whiteListStr = sysConfigService.getDBApplicationConfig().getLoginWhiteList();
        if (StringUtils.isNotEmpty(whiteListStr)) {
            if (!StringKit.checkInWhiteList(getIp(), whiteListStr)) {
                throw new BizException("登录IP限制，不在白名单内！");
            }
        }
        String account = Base64.decodeStr(getValStringRequired("ia"));  //用户名 i account, 已做base64处理
        String ipassport = Base64.decodeStr(getValStringRequired("ip"));    //密码 i passport,  已做base64处理
        String vercode = Base64.decodeStr(getValStringRequired("vc"));    //验证码 vercode,  已做base64处理
        String vercodeToken = Base64.decodeStr(getValStringRequired("vt"));    //验证码token, vercode token ,  已做base64处理
        String googleCodeStr = Base64.decodeStr(getValString("gc"));    //google code ,  已做base64处理

        String cacheCode = RedisUtil.getString(CS.getCacheKeyImgCode(vercodeToken));
        if (StringUtils.isEmpty(cacheCode) || !cacheCode.equalsIgnoreCase(vercode)) {
            throw new BizException("验证码有误！");
        }

        SysUserAuth sysUserAuth = sysUserAuthService.selectByLogin(account, CS.AUTH_TYPE.LOGIN_USER_NAME, CS.SYS_TYPE.MGR);
        if (sysUserAuth == null) {
            //清除之前的登录信息 单点登录
            throw new BizException("登录名或密码错误！");
        }
        authService.delAuthentication(Arrays.asList(sysUserAuth.getUserId()));
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
