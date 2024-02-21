package com.jeequan.jeepay.agent.ctrl.passage;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.agent.ctrl.CommonCtrl;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.entity.StatisticsAgentMch;
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
@RequestMapping("/api/passageInfo")
public class AgentPassageInfoController extends CommonCtrl {

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
            JSONObject jsonObject = getReqParamJSON();
            Date date = DateUtil.parse(jsonObject.getString("date"));


            LambdaQueryWrapper<PayPassage> wrapper = PayPassage.gw();
            wrapper.ne(PayPassage::getState, CS.HIDE);
            wrapper.eq(PayPassage::getAgentNo, getCurrentAgentNo());

            if (payPassage.getProductId() != null) {
                wrapper.eq(PayPassage::getProductId, payPassage.getProductId());
            }

//            wrapper.orderByDesc(PayPassage::getBalance);

            IPage<PayPassage> pages = payPassageService.page(getIPage(true), wrapper.select(PayPassage::getPayPassageId, PayPassage::getPayPassageName, PayPassage::getState));

            List<PayPassage> list = pages.getRecords();

            for (int i = 0; i < list.size(); i++) {
                StatisticsAgentPassage statisticsAgentPassage = statisticsAgentPassageService.getOne(StatisticsAgentPassage.gw().eq(StatisticsAgentPassage::getPayPassageId, list.get(i).getPayPassageId()).eq(StatisticsAgentPassage::getStatisticsDate, date).eq(StatisticsAgentPassage::getAgentNo, getCurrentAgentNo()));
                if (statisticsAgentPassage == null) {
                    statisticsAgentPassage = new StatisticsAgentPassage();
                    statisticsAgentPassage.setTotalAgentIncome(0L);
                    statisticsAgentPassage.setStatisticsDate(date);
                    statisticsAgentPassage.setOrderSuccessCount(0);
                    statisticsAgentPassage.setOrderSuccessCount(0);

                    statisticsAgentPassage.setTotalAmount(0L);
                    statisticsAgentPassage.setTotalOrderCount(0);

                    statisticsAgentPassage.setPayPassageId(list.get(i).getPayPassageId());
                    statisticsAgentPassage.addExt("payPassageName", list.get(i).getPayPassageName());
                }
                list.get(i).addExt("stat", statisticsAgentPassage);
            }

            return ApiRes.page(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }
}