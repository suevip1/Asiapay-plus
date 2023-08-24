package com.jeequan.jeepay.mgr.ctrl.passage;

import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PassageTransactionHistory;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.entity.SysUserAuth;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.GoogleAuthUtil;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.PassageTransactionHistoryService;
import com.jeequan.jeepay.service.impl.PayPassageService;
import com.jeequan.jeepay.service.impl.SysUserAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/mchAppsBalanceReset")
public class MchAppBalanceResetController extends CommonCtrl {

    @Autowired
    private PayPassageService payPassageService;

    @Autowired
    private PassageTransactionHistoryService passageTransactionHistoryService;


    @Autowired
    private SysUserAuthService sysUserAuthService;


    @PreAuthorize("hasAuthority('ENT_MCH_APP_EDIT')")
    @MethodLog(remark = "通道余额一键清零")
    @RequestMapping(value = "/resetAll", method = RequestMethod.POST)
    public ApiRes resetAll() {
        //校验谷歌
        String googleCodeStr = getValString("googleCode");

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
            throw new BizException("谷歌验证码有误！");
        }
        if (!isVerify) {
            throw new BizException("谷歌验证码有误！");
        }

        List<PayPassage> passageList = payPassageService.list();

        for (int i = 0; i < passageList.size(); i++) {
            PayPassage passage = passageList.get(i);
            Long amount = -passage.getBalance();

            Long beforeBalance = passage.getBalance();
            Long afterBalance = passage.getBalance() + amount;

            if (beforeBalance.longValue() != 0) {

                //插入通道流水记录
                PassageTransactionHistory passageTransactionHistory = new PassageTransactionHistory();
                passageTransactionHistory.setAmount(amount);
                passageTransactionHistory.setPayPassageId(passage.getPayPassageId());
                passageTransactionHistory.setPayPassageName(passage.getPayPassageName());

                passageTransactionHistory.setBeforeBalance(beforeBalance);
                passageTransactionHistory.setAfterBalance(afterBalance);
                passageTransactionHistory.setFundDirection(amount >= 0 ? CS.FUND_DIRECTION_INCREASE : CS.FUND_DIRECTION_REDUCE);
                passageTransactionHistory.setBizType(PassageTransactionHistory.BIZ_TYPE_CHANGE);
                SysUser sysUser = getCurrentUser().getSysUser();
                passageTransactionHistory.setCreatedUid(sysUser.getSysUserId());
                passageTransactionHistory.setCreatedLoginName(sysUser.getLoginUsername());
                passageTransactionHistory.setRemark("一键清空");
                passageTransactionHistoryService.save(passageTransactionHistory);

                payPassageService.updateBalance(passage.getPayPassageId(), amount);
            }
        }
        return ApiRes.ok();
    }

}