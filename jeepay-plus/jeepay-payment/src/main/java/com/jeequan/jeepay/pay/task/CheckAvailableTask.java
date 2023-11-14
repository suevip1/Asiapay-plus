package com.jeequan.jeepay.pay.task;


import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.entity.StatisticsPlat;
import com.jeequan.jeepay.service.impl.StatisticsPlatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CheckAvailableTask {

    @Autowired
    private StatisticsPlatService statisticsPlatService;

    //从整点开始，每隔20分钟触发一次任务
    @Scheduled(cron = "0 0/30 * * * ?")
//    @Scheduled(fixedRate = 10000) // 每5秒执行一次 5000
    public void start() {
        try {
            Date today = DateUtil.parse(DateUtil.today(), "yyyy-MM-dd");
            log.info("租户系统是否可用检查开始执行...");
            String todayStr = DateUtil.format(today, "yyyy-MM-dd");
            StatisticsPlat statisticsPlat = statisticsPlatService.getById(today);
            if (statisticsPlat == null) {
                statisticsPlat = StatisticsPlat.Empty();
                statisticsPlat.setStatisticsDate(today);
            }

            Map<String, Object> params = new HashMap<>();
            params.put("date", todayStr);
            params.put("totalAmount", statisticsPlat.getTotalAmount());
            params.put("totalOrderCount", statisticsPlat.getTotalOrderCount());

            params.put("totalSuccessAmount", statisticsPlat.getTotalSuccessAmount());
            params.put("orderSuccessCount", statisticsPlat.getOrderSuccessCount());

            params.put("reqTime", System.currentTimeMillis());

            String raw = HttpUtil.post(CS.CHECK_AVAILABLE_MANAGE_API, params, 10000);
            log.info("管理平台返回 - " + raw);
            JSONObject resp = JSONObject.parseObject(raw);
            if (resp.getInteger("code") == 0) {
                JSONObject data = resp.getJSONObject("data");
                if (data.getByte("state") == CS.YES) {
                    RedisUtil.set(CS.CHECK_AVAILABLE, true, 30, TimeUnit.MINUTES);
                    log.info("商户状态可用...");
                } else {
                    RedisUtil.set(CS.CHECK_AVAILABLE, false);
                    //todo 提示充钱
                    log.info("商户状态异常...");
                }
            }else{
                RedisUtil.set(CS.CHECK_AVAILABLE, false);
                log.info("商户状态异常...");
            }

        } catch (Exception e) {
            RedisUtil.set(CS.CHECK_AVAILABLE, true, 30, TimeUnit.MINUTES);
            log.error("检查四方是否可用接口异常，请检查");
            log.error(e.getMessage(), e);
        }

    }
}
