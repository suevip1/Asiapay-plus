package com.jeequan.jeepay.mgr.ctrl.merchant;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.AgentAccountInfo;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.AgentAccountInfoService;
import com.jeequan.jeepay.service.impl.PayPassageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 代理统计相关
 */
@RestController
@RequestMapping("/api/agentStatInfo")
public class AgentStatController extends CommonCtrl {

    @Autowired
    private AgentAccountInfoService agentAccountInfoService;

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 代理信息列表
     */
    @PreAuthorize("hasAuthority('ENT_C_MAIN_PAY_COUNT')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        LambdaQueryWrapper<AgentAccountInfo> wrapper = AgentAccountInfo.gw();
        wrapper.ne(AgentAccountInfo::getState, CS.HIDE);
        wrapper.orderByDesc(AgentAccountInfo::getBalance);
        IPage<AgentAccountInfo> pages = agentAccountInfoService.page(getIPage(), wrapper);
        return ApiRes.page(pages);
    }

    @PreAuthorize("hasAuthority('ENT_ISV_LIST')")
    @RequestMapping(value = "/statAgentInfo", method = RequestMethod.POST)
    public ApiRes statAgentInfo() {
        AgentAccountInfo agentAccountInfo = getObject(AgentAccountInfo.class);
        QueryWrapper<AgentAccountInfo> wrapper = new QueryWrapper();
        wrapper.ne("state", CS.HIDE);
        if (StringUtils.isNotEmpty(agentAccountInfo.getAgentNo())) {
            wrapper.eq("agent_no", agentAccountInfo.getAgentNo());
        }
        if (StringUtils.isNotEmpty(agentAccountInfo.getAgentName())) {
            wrapper.like("agent_name", agentAccountInfo.getAgentName().trim());
        }
        if (agentAccountInfo.getState() != null) {
            wrapper.eq("state", agentAccountInfo.getState());
        }

        wrapper.select("balance", "agent_no", "freeze_balance");
        List<AgentAccountInfo> agentAccountInfos = agentAccountInfoService.list(wrapper);
        Long totalBalance = 0L;
        Long freezeBalance = 0L;
        for (int i = 0; i < agentAccountInfos.size(); i++) {
            totalBalance += agentAccountInfos.get(i).getBalance();
            freezeBalance += agentAccountInfos.get(i).getFreezeBalance();
        }
        JSONObject resp = new JSONObject();
        resp.put("totalBalance", totalBalance);
        resp.put("freezeBalance", freezeBalance);
        resp.put("agentNum", agentAccountInfos.size());
        return ApiRes.ok(resp);
    }
}