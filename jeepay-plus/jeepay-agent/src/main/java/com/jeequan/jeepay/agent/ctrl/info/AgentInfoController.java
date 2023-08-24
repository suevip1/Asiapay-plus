package com.jeequan.jeepay.agent.ctrl.info;

import com.jeequan.jeepay.agent.ctrl.CommonCtrl;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.AgentAccountInfo;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.service.impl.AgentAccountInfoService;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.PayPassageService;
import com.jeequan.jeepay.service.impl.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agentInfo")
public class AgentInfoController extends CommonCtrl {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private AgentAccountInfoService agentAccountInfoService;

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private PayPassageService payPassageService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes detail() {
        AgentAccountInfo agentAccountInfo = agentAccountInfoService.queryAgentInfo(getCurrentAgentNo());
        if (agentAccountInfo == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        SysUser sysUser = sysUserService.getById(agentAccountInfo.getInitUserId());
        if (sysUser != null) {
            agentAccountInfo.addExt("loginUserName", sysUser.getLoginUsername());
        }
        int mchCount = mchInfoService.count(MchInfo.gw().eq(MchInfo::getAgentNo, getCurrentAgentNo()).eq(MchInfo::getState, CS.YES));
        agentAccountInfo.addExt("mchCount", mchCount);

        int passageCount = payPassageService.count(PayPassage.gw().eq(PayPassage::getAgentNo,getCurrentAgentNo()));
        agentAccountInfo.addExt("passageCount", passageCount);

        return ApiRes.ok(agentAccountInfo);
    }

}