package com.jeequan.jeepay.mgr.ctrl.passage;

import com.jeequan.jeepay.core.aop.LimitRequest;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PassageTransactionHistory;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api/mchAppsBalance")
public class MchAppBalanceController extends CommonCtrl {
    @Autowired
    private PayPassageService payPassageService;

    @Autowired
    private PassageTransactionHistoryService passageTransactionHistoryService;


    @PreAuthorize("hasAuthority('ENT_MCH_APP_EDIT')")
    @MethodLog(remark = "调整通道余额")
    @PutMapping("/{payPassageId}")
    @LimitRequest
    public ApiRes update(@PathVariable("payPassageId") Long payPassageId) {

        String changeAmount = getValString("changeAmount");
        String changeRemark = getValString("changeRemark");

        if (StringUtils.isNotEmpty(changeAmount) && StringUtils.isNotEmpty(changeRemark)) {
            Long amount = (long) (Double.valueOf(changeAmount) * 100);
            PayPassage passage = payPassageService.queryPassageInfo(payPassageId);

            Long beforeBalance = passage.getBalance();
            Long afterBalance = passage.getBalance() + amount;

            //插入更新记录 agentAccountHistoryService
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
            passageTransactionHistory.setRemark(changeRemark);
            passageTransactionHistoryService.save(passageTransactionHistory);


            payPassageService.updateBalance(passage.getPayPassageId(), amount);
        } else {
            throw new BizException("金额或备注不能为空");
        }
        return ApiRes.ok();
    }

}
