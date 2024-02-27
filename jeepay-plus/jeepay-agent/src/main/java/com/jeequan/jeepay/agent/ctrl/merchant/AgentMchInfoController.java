package com.jeequan.jeepay.agent.ctrl.merchant;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.agent.ctrl.CommonCtrl;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.StatisticsAgentMchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


@Slf4j
@RestController
@RequestMapping("/api/mchInfo")
public class AgentMchInfoController extends CommonCtrl {

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private StatisticsAgentMchService statisticsAgentMchService;

    /**
     * 查看代理商资金流水
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        try {
            MchInfo mchInfo = getObject(MchInfo.class);
            JSONObject jsonObject = getReqParamJSON();
            Date date = DateUtil.parse(jsonObject.getString("date"));


            LambdaQueryWrapper<MchInfo> wrapper = MchInfo.gw();

            wrapper.eq(MchInfo::getAgentNo, getCurrentAgentNo());
            wrapper.ne(MchInfo::getState, CS.HIDE);

            if (StringUtils.isNotEmpty(mchInfo.getMchNo())) {
                wrapper.eq(MchInfo::getMchNo, mchInfo.getMchNo().trim());
            }
            if (StringUtils.isNotEmpty(mchInfo.getMchName())) {
                wrapper.like(MchInfo::getMchName, mchInfo.getMchName().trim());
            }

            wrapper.orderByDesc(MchInfo::getBalance);

            IPage<MchInfo> pages = mchInfoService.page(getIPage(), wrapper.select(MchInfo::getMchNo, MchInfo::getMchName, MchInfo::getState, MchInfo::getBalance));

            List<MchInfo> list = pages.getRecords();

            for (int i = 0; i < list.size(); i++) {
                StatisticsAgentMch statisticsAgentMch = statisticsAgentMchService.getOne(StatisticsAgentMch.gw().eq(StatisticsAgentMch::getMchNo, list.get(i).getMchNo()).eq(StatisticsAgentMch::getStatisticsDate, date).eq(StatisticsAgentMch::getAgentNo, getCurrentAgentNo()));
                if (statisticsAgentMch == null) {
                    statisticsAgentMch = new StatisticsAgentMch();
                    statisticsAgentMch.setTotalAgentIncome(0L);
                    statisticsAgentMch.setStatisticsDate(date);
                    statisticsAgentMch.setOrderSuccessCount(0);
                    statisticsAgentMch.setOrderSuccessCount(0);

                    statisticsAgentMch.setTotalAmount(0L);
                    statisticsAgentMch.setTotalOrderCount(0);
                    statisticsAgentMch.setTotalSuccessAmount(0L);
                }
                list.get(i).addExt("stat", statisticsAgentMch);
            }

            return ApiRes.page(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }

}