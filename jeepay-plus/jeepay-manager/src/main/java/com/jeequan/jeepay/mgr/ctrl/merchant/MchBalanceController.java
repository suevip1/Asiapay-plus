package com.jeequan.jeepay.mgr.ctrl.merchant;


import cn.hutool.core.codec.Base64;
import com.jeequan.jeepay.components.mq.model.CleanMchLoginAuthCacheMQ;
import com.jeequan.jeepay.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/mchBalance")
public class MchBalanceController extends CommonCtrl {
    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private MchHistoryService mchHistoryService;


    @PreAuthorize("hasAuthority('ENT_MCH_INFO_EDIT')")
    @MethodLog(remark = "商户余额调整")
    @RequestMapping(value = "/{mchNo}", method = RequestMethod.PUT)
    public ApiRes update(@PathVariable("mchNo") String mchNo) {
        String changeAmount = getValString("changeAmount");
        String changeRemark = getValString("changeRemark");

        if (StringUtils.isNotEmpty(changeAmount) && StringUtils.isNotEmpty(changeRemark)) {
            // 使用 BigDecimal 进行精确计算
            double changeAmountValue = Double.valueOf(changeAmount);
            BigDecimal bigDecimalValue = BigDecimal.valueOf(changeAmountValue);
            BigDecimal result = bigDecimalValue.multiply(BigDecimal.valueOf(100));
            Long amount = result.longValue();

            MchInfo selectMch = mchInfoService.queryMchInfo(mchNo);
            Long beforeBalance = selectMch.getBalance();
            Long afterBalance = selectMch.getBalance() + amount;
            mchInfoService.updateBalance(mchNo, amount);

            //插入更新记录
            MchHistory mchHistory = new MchHistory();
            mchHistory.setMchNo(mchNo);
            mchHistory.setMchName(selectMch.getMchName());
            mchHistory.setAmount(amount);

            mchHistory.setBeforeBalance(beforeBalance);
            mchHistory.setAfterBalance(afterBalance);
            mchHistory.setFundDirection(amount >= 0 ? CS.FUND_DIRECTION_INCREASE : CS.FUND_DIRECTION_REDUCE);
            mchHistory.setBizType(CS.BIZ_TYPE_CHANGE);
            SysUser sysUser = getCurrentUser().getSysUser();
            mchHistory.setCreatedUid(sysUser.getSysUserId());
            mchHistory.setCreatedLoginName(sysUser.getLoginUsername());
            mchHistory.setRemark(changeRemark);
            mchHistoryService.save(mchHistory);

        } else {
            throw new BizException("金额或备注不能为空");
        }

        return ApiRes.ok();
    }
}
