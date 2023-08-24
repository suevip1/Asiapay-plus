package com.jeequan.jeepay.mgr.ctrl.agent;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.entity.AgentAccountHistory;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.AgentAccountHistoryService;
import com.jeequan.jeepay.service.impl.SysUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/agentHistory")
public class AgentHistoryController extends CommonCtrl {

    @Autowired
    private AgentAccountHistoryService agentAccountHistoryService;


    /**
     * 查看代理商资金流水
     *
     * @return
     */
    @PreAuthorize("hasAnyAuthority('ENT_ISV_INFO_HISTORY')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        AgentAccountHistory agentAccountHistory = getObject(AgentAccountHistory.class);
        LambdaQueryWrapper<AgentAccountHistory> wrapper = AgentAccountHistory.gw();
        JSONObject paramJSON = getReqParamJSON();

        //代理商号
        if (StringUtils.isNotEmpty(agentAccountHistory.getAgentNo())) {
            wrapper.eq(AgentAccountHistory::getAgentNo, agentAccountHistory.getAgentNo());
        }

        //订单
        if (StringUtils.isNotEmpty(agentAccountHistory.getPayOrderId())) {
            wrapper.eq(AgentAccountHistory::getPayOrderId, agentAccountHistory.getPayOrderId());
        }
        //资金方向
        if (agentAccountHistory.getFundDirection() != null && agentAccountHistory.getFundDirection() != 0) {
            wrapper.eq(AgentAccountHistory::getFundDirection, agentAccountHistory.getFundDirection());
        }
        //业务类型
        if (agentAccountHistory.getBizType() != null && agentAccountHistory.getBizType() != 0) {
            wrapper.eq(AgentAccountHistory::getBizType, agentAccountHistory.getBizType());
        }
        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                wrapper.ge(AgentAccountHistory::getCreatedAt, paramJSON.getString("createdStart"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                wrapper.le(AgentAccountHistory::getCreatedAt, paramJSON.getString("createdEnd"));
            }
        }
        wrapper.orderByDesc(AgentAccountHistory::getCreatedAt);

        IPage<AgentAccountHistory> pages = agentAccountHistoryService.page(getIPage(), wrapper);

        return ApiRes.page(pages);
    }
}
