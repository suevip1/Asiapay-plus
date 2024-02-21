package com.jeequan.jeepay.mgr.ctrl.merchant;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.entity.SysUserAuth;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.GoogleAuthUtil;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.PayPassageService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import com.jeequan.jeepay.service.impl.SysUserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * 通道统计相关
 */
@RestController
@RequestMapping("/api/passageStatInfo")
public class PassageStatController extends CommonCtrl {

    @Autowired
    private PayPassageService payPassageService;

    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private SysUserAuthService sysUserAuthService;

    /**
     * 通道信息列表
     *
     * @return
     */
    @PreAuthorize("hasAuthority('ENT_C_MAIN_PAY_COUNT')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        LambdaQueryWrapper<PayPassage> wrapper = PayPassage.gw();
        wrapper.ne(PayPassage::getState, CS.HIDE);
        wrapper.orderByDesc(PayPassage::getBalance);
        IPage<PayPassage> pages = payPassageService.page(getIPage(), wrapper);
        return ApiRes.page(pages);
    }

    //    @PreAuthorize("hasAuthority('ENT_MCH_LIST')")
    @RequestMapping(value = "/statPassageInfo", method = RequestMethod.GET)
    public ApiRes statPassageInfo() {
        JSONObject result = payPassageService.sumPassageInfo();
        // 获取日切自动清零设置
        result.put("payPassageAutoClean", sysConfigService.getPassageAutoClearConfig());
        return ApiRes.ok(result);
    }

    @RequestMapping(value = "/setPassageAutoClean", method = RequestMethod.POST)
    public ApiRes setPassageAutoClean() {
        //{"googleCode":"123123","autoCleanEnable":1}
        JSONObject params = getReqParamJSON();
        try {
            String googleCodeStr = getValString("googleCode");
            Byte autoCleanEnable = getValByte("autoCleanEnable");

            //检查当前用户是否绑定谷歌
            String account = getCurrentUser().getSysUser().getLoginUsername();
            SysUserAuth sysUserAuth = sysUserAuthService.selectByLogin(account, CS.AUTH_TYPE.LOGIN_USER_NAME, CS.SYS_TYPE.MGR);
            if (sysUserAuth == null || sysUserAuth.getGoogleAuthStatus() == CS.NO) {
                return ApiRes.customFail("请先绑定谷歌验证码");
            }

            boolean isVerify = false;
            try {
                int googleCode = Integer.parseInt(googleCodeStr);
                isVerify = GoogleAuthUtil.CheckCode(sysUserAuth.getGoogleAuthKey(), googleCode);
            } catch (Exception e) {
                return ApiRes.customFail("谷歌验证码有误！");
            }
            if (!isVerify) {
                return ApiRes.customFail("谷歌验证码有误！");
            }

            boolean isSuccess = sysConfigService.updatePassageAutoClearConfig(autoCleanEnable);

            if (!isSuccess) {
                return ApiRes.customFail("更新失败");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.customFail("更新失败");
        }
        return ApiRes.ok();
    }
}