package com.jeequan.jeepay.pay.task;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeequan.jeepay.core.entity.MchHistory;
import com.jeequan.jeepay.core.entity.MchNotifyRecord;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class DataClearTask {
    /**
     * 单次批量操作数据
     */
    private static int QUERY_PAGE_SIZE = 500;

    @Autowired
    private PayOrderService payOrderService;

    @Autowired
    private MchNotifyRecordService mchNotifyService;

    @Autowired
    private AgentAccountHistoryService agentAccountHistoryService;

    @Autowired
    private MchHistoryService mchHistoryService;

    @Autowired
    private PassageTransactionHistoryService passageTransactionHistoryService;

    @Autowired
    private StatisticsPlatService statisticsPlatService;

    @Autowired
    private StatisticsPassageService statisticsPassageService;

    @Autowired
    private StatisticsMchService statisticsMchService;

    @Autowired
    private StatisticsAgentService statisticsAgentService;

    @Autowired
    private StatisticsMchProductService statisticsMchProductService;

    @Autowired
    private StatisticsProductService statisticsProductService;

    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private DivisionRecordService divisionRecordService;

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private PayPassageService payPassageService;

    @Autowired
    private ProductService productService;

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private AgentAccountInfoService agentAccountInfoService;

    @Async
    @Scheduled(cron = "0 0 04 * * ?") // 每天凌晨四点执行
//    @Scheduled(cron = "00 03 00 * * ?") // 每天凌晨四点执行
    public void start() {
        int dayOffset = -(Integer.parseInt(sysConfigService.getDBApplicationConfig().getDataOffset()) - 1);
        Date date = DateUtil.parse(DateUtil.today());
        Date offsetDate = DateUtil.offsetDay(date, dayOffset);
        //过期订单
        payOrderService.ClearPayOrder(offsetDate, QUERY_PAGE_SIZE);
        //过期通知
        mchNotifyService.ClearMchNotify(offsetDate, QUERY_PAGE_SIZE);
        //统计数据删除
        statisticsPlatService.ClearPlatStatHistory(offsetDate, QUERY_PAGE_SIZE);
        statisticsPassageService.ClearPassageStatHistory(offsetDate, QUERY_PAGE_SIZE);
        statisticsMchService.ClearMchStatHistory(offsetDate, QUERY_PAGE_SIZE);
        statisticsAgentService.ClearAgentStatHistory(offsetDate, QUERY_PAGE_SIZE);
        statisticsMchProductService.ClearMchProductStatHistory(offsetDate, QUERY_PAGE_SIZE);
        statisticsProductService.ClearProductStatHistory(offsetDate, QUERY_PAGE_SIZE);
        //资金流水删除
        mchHistoryService.ClearMchHistory(offsetDate, QUERY_PAGE_SIZE);
        agentAccountHistoryService.ClearAgentHistory(offsetDate, QUERY_PAGE_SIZE);
        passageTransactionHistoryService.ClearPassageTransactionHistory(offsetDate, QUERY_PAGE_SIZE);
        //过期分账记录删除
        divisionRecordService.ClearDivisionRecordHistory(offsetDate, QUERY_PAGE_SIZE);
        //系统操作日志清理
        sysLogService.ClearSysLog(offsetDate, QUERY_PAGE_SIZE);
        //检查有无可删除的通道
        payPassageService.removeUnusedPayPassage();

        //检查并清理可删除产品
        productService.removeUnusedProduct();

        //检查并真正清理商户
        mchInfoService.removeMchAuto();

        //检查并清理代理
        agentAccountInfoService.removeAgentAuto();
    }

}