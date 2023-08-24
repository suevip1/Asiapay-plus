package com.jeequan.jeepay.pay.task;

import com.jeequan.jeepay.service.impl.DivisionRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 结算订单定时任务
 */
@Slf4j
@Component
public class DivisionOrderExpiredTask {

//    @Autowired
//    private DivisionRecordService divisionRecordService;
//
//    @Scheduled(cron = "0 0/1 * * * ?") // 每分钟执行一次
//    public void start() {
//        int updateCount = divisionRecordService.updateRecordExpired();
////        log.info("处理审核超时{}条.", updateCount);
//    }
}
