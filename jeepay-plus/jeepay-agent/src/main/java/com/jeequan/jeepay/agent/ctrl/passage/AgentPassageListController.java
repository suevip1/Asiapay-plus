package com.jeequan.jeepay.agent.ctrl.passage;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.agent.ctrl.CommonCtrl;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.entity.StatisticsAgentPassage;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.service.impl.PayPassageService;
import com.jeequan.jeepay.service.impl.StatisticsAgentPassageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/agentPassage")
public class AgentPassageListController extends CommonCtrl {

    @Autowired
    private PayPassageService payPassageService;

    @Autowired
    private StatisticsAgentPassageService statisticsAgentPassageService;

    /**
     * 查看代理商资金流水
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        try {
            PayPassage payPassage = getObject(PayPassage.class);

            LambdaQueryWrapper<PayPassage> wrapper = PayPassage.gw();

            wrapper.eq(PayPassage::getAgentNo, getCurrentAgentNo());

            if (payPassage.getPayPassageId() != null) {
                wrapper.eq(PayPassage::getPayPassageId, payPassage.getPayPassageId());
            }

            if (StringUtils.isNotEmpty(payPassage.getPayPassageName())) {
                wrapper.like(PayPassage::getPayPassageName, payPassage.getPayPassageName().trim());
            }

            wrapper.orderByDesc(PayPassage::getCreatedAt);

            IPage<PayPassage> pages = payPassageService.page(getIPage(), wrapper.select(PayPassage::getPayPassageId, PayPassage::getPayPassageName, PayPassage::getState, PayPassage::getAgentNo, PayPassage::getAgentRate, PayPassage::getCreatedAt));

            return ApiRes.page(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }
}