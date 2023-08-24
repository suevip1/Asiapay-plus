package com.jeequan.jeepay.mgr.ctrl.agent;

import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.AgentAccountHistory;
import com.jeequan.jeepay.core.entity.AgentAccountInfo;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.AgentAccountHistoryService;
import com.jeequan.jeepay.service.impl.AgentAccountInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/isvBalance")
public class AgentBalanceController extends CommonCtrl {

    @Autowired
    private AgentAccountInfoService agentAccountInfoService;

    @Autowired
    private AgentAccountHistoryService agentAccountHistoryService;

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:13
     * @describe: 更新代理商信息
     */
    @PreAuthorize("hasAuthority('ENT_ISV_INFO_EDIT')")
    @MethodLog(remark = "代理余额调整")
    @RequestMapping(value = "/{agentNo}", method = RequestMethod.PUT)
    public ApiRes update(@PathVariable("agentNo") String agentNo) {

        String changeAmount = getValString("changeAmount");
        String changeRemark = getValString("changeRemark");

        if (StringUtils.isNotEmpty(changeAmount) && StringUtils.isNotEmpty(changeRemark)) {
            Long amount = (long) (Double.valueOf(changeAmount) * 100);
            AgentAccountInfo selectAgent = agentAccountInfoService.queryAgentInfo(agentNo);
            Long beforeBalance = selectAgent.getBalance();
            Long afterBalance = selectAgent.getBalance() + amount;

            agentAccountInfoService.updateBalance(agentNo, amount);


            //插入更新记录 agentAccountHistoryService
            AgentAccountHistory agentAccountHistory = new AgentAccountHistory();
            agentAccountHistory.setAgentNo(agentNo);
            agentAccountHistory.setAgentName(selectAgent.getAgentName());
            agentAccountHistory.setAmount(amount);

            agentAccountHistory.setBeforeBalance(beforeBalance);
            agentAccountHistory.setAfterBalance(afterBalance);
            agentAccountHistory.setFundDirection(amount >= 0 ? CS.FUND_DIRECTION_INCREASE : CS.FUND_DIRECTION_REDUCE);
            agentAccountHistory.setBizType(CS.BIZ_TYPE_CHANGE);
            SysUser sysUser = getCurrentUser().getSysUser();
            agentAccountHistory.setCreatedUid(sysUser.getSysUserId());
            agentAccountHistory.setCreatedLoginName(sysUser.getLoginUsername());
            agentAccountHistory.setRemark(changeRemark);
            agentAccountHistoryService.save(agentAccountHistory);
        } else {
            throw new BizException("金额或备注不能为空");
        }

        return ApiRes.ok();
    }
}