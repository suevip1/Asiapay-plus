package com.jeequan.jeepay.agent.ctrl.stat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.agent.ctrl.CommonCtrl;
import com.jeequan.jeepay.core.entity.StatisticsAgent;
import com.jeequan.jeepay.core.entity.StatisticsMch;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.service.impl.StatisticsAgentService;
import com.jeequan.jeepay.service.impl.StatisticsMchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/agentDayStat")
public class AgentDayStatController extends CommonCtrl {

    @Autowired
    private StatisticsAgentService statisticsAgentService;

    @GetMapping
    public ApiRes list() {
        LambdaQueryWrapper<StatisticsAgent> wrapper = StatisticsAgent.gw();
        wrapper.orderByDesc(StatisticsAgent::getCreatedAt);
        IPage<StatisticsAgent> pages = statisticsAgentService.page(getIPage(true), wrapper.eq(StatisticsAgent::getAgentNo, getCurrentAgentNo()));
        return ApiRes.ok(pages);
    }
}