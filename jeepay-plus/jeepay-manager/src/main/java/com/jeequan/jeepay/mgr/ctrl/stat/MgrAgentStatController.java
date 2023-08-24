package com.jeequan.jeepay.mgr.ctrl.stat;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.entity.AgentAccountInfo;
import com.jeequan.jeepay.core.entity.StatisticsAgent;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.AgentAccountInfoService;
import com.jeequan.jeepay.service.impl.StatisticsAgentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/agentStat")
public class MgrAgentStatController extends CommonCtrl {

    @Autowired
    private StatisticsAgentService statisticsAgentService;

    @Autowired
    private AgentAccountInfoService agentAccountInfoService;

    @GetMapping
    public ApiRes list() {
        AgentAccountInfo agentAccountInfo = getObject(AgentAccountInfo.class);
        JSONObject paramJSON = getReqParamJSON();

        Map<String, AgentAccountInfo> agentAccountInfoMap = agentAccountInfoService.getAgentInfoMap();
        LambdaQueryWrapper<StatisticsAgent> wrapper = StatisticsAgent.gw();

        if (StringUtils.isNotEmpty(agentAccountInfo.getAgentNo())) {
            wrapper.eq(StatisticsAgent::getAgentNo, agentAccountInfo.getAgentNo());
        }

        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                wrapper.ge(StatisticsAgent::getStatisticsDate, paramJSON.getString("createdStart"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                wrapper.le(StatisticsAgent::getStatisticsDate, paramJSON.getString("createdEnd"));
            }
        }


        wrapper.orderByDesc(StatisticsAgent::getStatisticsDate);
        IPage<StatisticsAgent> pages = statisticsAgentService.page(getIPage(true), wrapper);
        List<StatisticsAgent> records = pages.getRecords();
        for (int i = 0; i < records.size(); i++) {
            records.get(i).addExt("agentName", agentAccountInfoMap.get(records.get(i).getAgentNo()).getAgentName());
        }
        pages.setRecords(records);
        return ApiRes.ok(pages);
    }
}