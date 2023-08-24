package com.jeequan.jeepay.mgr.ctrl.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.entity.AgentAccountInfo;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.AgentAccountInfoService;
import com.jeequan.jeepay.service.impl.PayPassageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
        wrapper.orderByDesc(AgentAccountInfo::getBalance);
        IPage<AgentAccountInfo> pages = agentAccountInfoService.page(getIPage(), wrapper);
        return ApiRes.page(pages);
    }

    @PreAuthorize("hasAuthority('ENT_ISV_LIST')")
    @RequestMapping(value = "/statAgentInfo", method = RequestMethod.GET)
    public ApiRes statAgentInfo() {
        return ApiRes.ok(agentAccountInfoService.sumAgentInfo());
    }
}