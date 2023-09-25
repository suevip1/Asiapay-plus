package com.jeequan.jeepay.mgr.ctrl.stat;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.StatisticsMchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/mchStat")
public class MchStatController extends CommonCtrl {

    @Autowired
    private StatisticsMchService statisticsMchService;

    @Autowired
    private MchInfoService mchInfoService;

    @GetMapping
    public ApiRes list() {
        MchInfo mchInfo = getObject(MchInfo.class);
        JSONObject paramJSON = getReqParamJSON();

        Map<String, MchInfo> mchMap = mchInfoService.getMchInfoMap();
        LambdaQueryWrapper<StatisticsMch> wrapper = StatisticsMch.gw();

        if (StringUtils.isNotEmpty(mchInfo.getMchNo())) {
            wrapper.like(StatisticsMch::getMchNo, mchInfo.getMchNo());
        }

        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                wrapper.ge(StatisticsMch::getStatisticsDate, paramJSON.getString("createdStart"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                wrapper.le(StatisticsMch::getStatisticsDate, paramJSON.getString("createdEnd"));
            }
            String mchName = paramJSON.getString("mchName");
            if (StringUtils.isNotEmpty(mchName)) {
                //查询对应的id
                List<String> mchNos = new ArrayList<>();
                mchMap.forEach((key, value) -> {
                    if (value.getMchName().indexOf(mchName.trim()) != -1) {
                        mchNos.add(key);
                    }
                });
                if (mchNos.size() > 0) {
                    wrapper.in(StatisticsMch::getMchNo, mchNos);
                }else{
                    wrapper.eq(StatisticsMch::getMchNo, "");
                }
            }
        }

        wrapper.orderByDesc(StatisticsMch::getStatisticsDate);
        wrapper.orderByDesc(StatisticsMch::getTotalSuccessAmount);
        IPage<StatisticsMch> pages = statisticsMchService.page(getIPage(true), wrapper);
        List<StatisticsMch> records = statisticsMchService.list(wrapper);
//        log.info(records.size() + "");
//        List<StatisticsMch> records = pages.getRecords();
        for (int i = 0; i < records.size(); i++) {
            records.get(i).addExt("mchName", mchMap.get(records.get(i).getMchNo()).getMchName());
        }
        pages.setTotal(records.size());
        pages.setRecords(records);
        return ApiRes.ok(pages);
    }
}