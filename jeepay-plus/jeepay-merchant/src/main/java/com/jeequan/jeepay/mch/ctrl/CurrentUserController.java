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
package com.jeequan.jeepay.mch.ctrl;

import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.cache.ITokenService;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.SysEntitlement;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.entity.SysUserAuth;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import com.jeequan.jeepay.core.utils.GoogleAuthUtil;
import com.jeequan.jeepay.core.utils.TreeDataBuilder;
import com.jeequan.jeepay.service.impl.SysEntitlementService;
import com.jeequan.jeepay.service.impl.SysUserAuthService;
import com.jeequan.jeepay.service.impl.SysUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 当前登录者的信息相关接口
 *
 * @author terrfly
 * @modify zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@RestController
@RequestMapping("api/current")
public class CurrentUserController extends CommonCtrl {
    @Autowired
    private SysEntitlementService sysEntitlementService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysUserAuthService sysUserAuthService;

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public ApiRes currentUserInfo() {

        ///当前用户信息
        JeeUserDetails jeeUserDetails = getCurrentUser();
        SysUser user = jeeUserDetails.getSysUser();

        //1. 当前用户所有权限ID集合
        List<String> entIdList = new ArrayList<>();
        jeeUserDetails.getAuthorities().stream().forEach(r -> entIdList.add(r.getAuthority()));

        List<SysEntitlement> allMenuList = new ArrayList<>();    //所有菜单集合

        //2. 查询出用户所有菜单集合 (包含左侧显示菜单 和 其他类型菜单 )
        if (!entIdList.isEmpty()) {
            allMenuList = sysEntitlementService.list(SysEntitlement.gw()
                    .in(SysEntitlement::getEntId, entIdList)
                    .in(SysEntitlement::getEntType, Arrays.asList(CS.ENT_TYPE.MENU_LEFT, CS.ENT_TYPE.MENU_OTHER))
                    .eq(SysEntitlement::getSysType, CS.SYS_TYPE.MCH)
                    .eq(SysEntitlement::getState, CS.PUB_USABLE));
        }
        SysUserAuth sysUserAuth = sysUserAuthService.getOne(SysUserAuth.gw().eq(SysUserAuth::getUserId, user.getSysUserId()));

        //4. 转换为json树状结构
        JSONArray jsonArray = (JSONArray) JSON.toJSON(allMenuList);
        List<JSONObject> allMenuRouteTree = new TreeDataBuilder(jsonArray,
                "entId", "pid", "children", "entSort", true)
                .buildTreeObject();

        //1. 所有权限ID集合
        user.addExt("entIdList", entIdList);
        user.addExt("allMenuRouteTree", allMenuRouteTree);
        user.addExt("googleAuth", sysUserAuth.getGoogleAuthStatus());
        return ApiRes.ok(user);
    }


    /**
     * 修改个人信息
     */
    @RequestMapping(value = "/user", method = RequestMethod.PUT)
    @MethodLog(remark = "修改信息")
    public ApiRes modifyCurrentUserInfo() {
        //0 1
        String googleAuth = getValString("googleAuth");
        String googleCode = getValString("googleCode");
        String googleKey = getValString("googleKey");

        Byte googleAuthState = googleAuth.equals("1") ? CS.YES : CS.NO;

        SysUserAuth sysUserAuth = sysUserAuthService.getOne(SysUserAuth.gw().eq(SysUserAuth::getUserId, getCurrentUser().getSysUser().getSysUserId()));

        //原来没打开，现在要打开,验证验证码对不对
        if (sysUserAuth.getGoogleAuthStatus() == CS.NO && googleAuthState == CS.YES) {
            if (StringUtils.isNotEmpty(googleKey) && StringUtils.isNotEmpty(googleCode)) {
                int code = Integer.parseInt(googleCode);
                boolean isSuccess = GoogleAuthUtil.CheckCode(googleKey, code);
                if (isSuccess) {
                    sysUserAuth.setGoogleAuthStatus(CS.YES);
                    sysUserAuth.setGoogleAuthKey(googleKey);
                } else {
                    throw new BizException("谷歌验证码错误！");
                }
            } else {
                throw new BizException("googleKey或googleCode为空！");
            }
        } else if (sysUserAuth.getGoogleAuthStatus() == CS.YES && googleAuthState == CS.NO) {
            //原来已经打开，现在没打开,验证验证码对不对
            if (StringUtils.isNotEmpty(googleCode)) {
                int code = Integer.parseInt(googleCode);
                boolean isSuccess = GoogleAuthUtil.CheckCode(sysUserAuth.getGoogleAuthKey(), code);
                if (isSuccess) {
                    sysUserAuth.setGoogleAuthStatus(googleAuthState);
                } else {
                    throw new BizException("谷歌验证码错误！");
                }
            } else {
                throw new BizException("googleCode为空！");
            }
        }

        sysUserAuthService.saveOrUpdate(sysUserAuth);
        //保存redis最新数据
        JeeUserDetails currentUser = getCurrentUser();
        currentUser.setSysUser(sysUserService.getById(getCurrentUser().getSysUser().getSysUserId()));
        ITokenService.refData(currentUser);

        return ApiRes.ok();
    }

    @RequestMapping(value = "/getGoogleKey", method = RequestMethod.GET)
    public ApiRes getGoogleKey() {
        //0 1
        String key = GoogleAuthUtil.GenerateSecretKey();
        String qrCode = GoogleAuthUtil.GetQRCodeStr(getCurrentUser().getSysUser().getLoginUsername(), key);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("qrCode", qrCode);
        jsonObject.put("key", key);
        return ApiRes.ok(jsonObject);
    }

    @RequestMapping(value = "/verifyGoogleCode", method = RequestMethod.POST)
    public ApiRes verifyGoogleCode() {
        //0 1
        String key = GoogleAuthUtil.GenerateSecretKey();
        String qrCode = GoogleAuthUtil.GetQRCodeStr(getCurrentUser().getSysUser().getLoginUsername(), key);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("qrCode", qrCode);
        jsonObject.put("key", key);
        return ApiRes.ok(jsonObject);

//        int googleCode;
//        boolean isVerify = false;
//        try {
//            googleCode = Integer.parseInt(googleCodeStr);
//            isVerify = GoogleAuthUtil.CheckCode(sysUserAuth.getGoogleAuthKey(), googleCode);
    }


    /**
     * 修改密码
     */
    @RequestMapping(value = "modifyPwd", method = RequestMethod.PUT)
    @MethodLog(remark = "修改密码")
    public ApiRes modifyPwd() throws BizException {

        //更改密码，验证当前用户信息
        String currentUserPwd = Base64.decodeStr(getValStringRequired("originalPwd")); //当前用户登录密码
        //验证当前密码是否正确
        if (!sysUserAuthService.validateCurrentUserPwd(currentUserPwd)) {
            throw new BizException("原密码验证失败！");
        }

        String opUserPwd = Base64.decodeStr(getValStringRequired("confirmPwd"));

        // 验证原密码与新密码是否相同
        if (opUserPwd.equals(currentUserPwd)) {
            throw new BizException("新密码与原密码不能相同！");
        }

        sysUserAuthService.resetAuthInfo(getCurrentUser().getSysUser().getSysUserId(), opUserPwd, CS.SYS_TYPE.MCH);
        //调用登出接口
        return logout();
    }

    /**
     * 登出
     */
    @RequestMapping(value = "logout", method = RequestMethod.POST)
    @MethodLog(remark = "登出")
    public ApiRes logout() throws BizException {

        ITokenService.removeIToken(getCurrentUser().getCacheKey(), getCurrentUser().getSysUser().getSysUserId());
        return ApiRes.ok();
    }
}
