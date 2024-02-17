package com.jeequan.jeepay.agent.ctrl.history;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.agent.ctrl.CommonCtrl;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.AgentAccountHistory;
import com.jeequan.jeepay.core.entity.MchHistory;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.service.impl.AgentAccountHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Slf4j
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
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        try {
            AgentAccountHistory agentAccountHistory = getObject(AgentAccountHistory.class);
            LambdaQueryWrapper<AgentAccountHistory> wrapper = AgentAccountHistory.gw();
            JSONObject paramJSON = getReqParamJSON();

            //商号
            wrapper.eq(AgentAccountHistory::getAgentNo, getCurrentAgentNo());

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
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }

    @PostMapping(value = "/exportExcel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportExcel() throws Exception {
        AgentAccountHistory agentAccountHistory = getObject(AgentAccountHistory.class);
        JSONObject paramJSON = getReqParamJSON();
        if (agentAccountHistory == null) {
            agentAccountHistory = new AgentAccountHistory();
        }
        LambdaQueryWrapper<AgentAccountHistory> wrapper = AgentAccountHistory.gw();

        //商号
        wrapper.eq(AgentAccountHistory::getAgentNo, getCurrentAgentNo());

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

        List<AgentAccountHistory> pages = agentAccountHistoryService.list(wrapper);

        List<List> excelData = new ArrayList();
        List<String> header = Arrays.asList("记录ID", "代理商户号", "变更前余额(元)", "变更金额(元)", "变更后余额(元)", "业务类型", "业务订单号", "订单金额", "时间");
        excelData.add(header);
        Iterator iteratorRecord = pages.iterator();

        while (iteratorRecord.hasNext()) {
            AgentAccountHistory record = (AgentAccountHistory) iteratorRecord.next();
            List<String> rowData = new ArrayList();
            //id
            rowData.add(String.valueOf(record.getAgentAccountHistoryId()));

            rowData.add(record.getAgentNo());
            //变更前余额
            rowData.add(AmountUtil.convertCent2Dollar(record.getBeforeBalance() + ""));
            //变更金额
            String prefix = record.getFundDirection() == CS.FUND_DIRECTION_INCREASE ? "+" : "";
            rowData.add(prefix + AmountUtil.convertCent2Dollar(record.getAmount() + ""));
            //变更后余额
            rowData.add(AmountUtil.convertCent2Dollar(record.getAfterBalance() + ""));
            //业务类型
            if (record.getBizType() == CS.BIZ_TYPE_CHANGE) {
                rowData.add(CS.GetAgentBizTypeString(record.getBizType()) + "-(操作员用户名)-" + record.getCreatedLoginName());
            } else {
                rowData.add(CS.GetAgentBizTypeString(record.getBizType()));
            }

            //业务订单号
            rowData.add(record.getPayOrderId());
            //订单金额
            if (record.getPayOrderAmount() != null) {
                rowData.add(AmountUtil.convertCent2Dollar(record.getPayOrderAmount() + ""));
            } else {
                rowData.add("");
            }

            //时间
            rowData.add(DateUtil.format(record.getCreatedAt(), "yyyy-MM-dd HH-mm-ss"));
            excelData.add(rowData);
        }

        this.writeExcelStream(excelData);
    }

}