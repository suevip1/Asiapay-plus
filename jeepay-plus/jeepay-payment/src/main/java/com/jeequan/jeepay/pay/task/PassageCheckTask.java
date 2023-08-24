package com.jeequan.jeepay.pay.task;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PassageTransactionHistory;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.pay.service.ChannelOrderReissueService;
import com.jeequan.jeepay.service.impl.PassageTransactionHistoryService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.PayPassageService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class PassageCheckTask {

    @Autowired
    private PayPassageService payPassageService;

    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private PassageTransactionHistoryService passageTransactionHistoryService;


    @Scheduled(cron = "0 0/1 * * * ?") // 每分钟执行一次
    public void start() {

        //定时开关打开的通道
        List<PayPassage> passageList = payPassageService.list(PayPassage.gw().eq(PayPassage::getTimeLimit, CS.YES));

        //存储格式 00:45|02:48
        for (int i = 0; i < passageList.size(); i++) {
            PayPassage payPassage = passageList.get(i);
            String[] timeRules = payPassage.getTimeRules().split("\\|");
            try {
                String[] timeRulesStart = timeRules[0].split(":");
                String[] timeRulesEnd = timeRules[1].split(":");
                Date dateStart = DateUtil.parse(DateUtil.today());
                dateStart.setHours(Integer.parseInt(timeRulesStart[0]));
                dateStart.setMinutes(Integer.parseInt(timeRulesStart[1]));

                Date dateEnd = DateUtil.parse(DateUtil.today());
                dateEnd.setHours(Integer.parseInt(timeRulesEnd[0]));
                dateEnd.setMinutes(Integer.parseInt(timeRulesEnd[1]));
                //处理跨天 前小于后
                if (dateStart.compareTo(dateEnd) >= 0) {
                    Date newEndDate = DateUtil.offsetDay(dateEnd, 1);
                    CheckPassageState(dateStart, newEndDate, payPassage);
                } else {
                    CheckPassageState(dateStart, dateEnd, payPassage);
                }
            } catch (Exception e) {
                log.error("检查通道定时任务异常，请检查");
                log.error(e.getMessage(), e);
            }
        }
    }


    @Scheduled(cron = "0 0 0 * * ?")// 在每天的 00:00:00 分执行的任务
    public void autoCleanPassageTask() {
        //检查通道自动清零是否打开
        AutoCleanCheck();
    }

//    @Scheduled(cron = "0 08 21 * * ?")// 在每天的 00:00:00 分执行的任务
//    public void autoCleanPassageTaskTest() {
//        //检查通道自动清零是否打开
//        AutoCleanCheck();
//    }


    private void AutoCleanCheck() {
        Byte state = sysConfigService.getPassageAutoClearConfig();
        if (state == CS.YES) {
            log.info("====执行通道自动日切余额清零====");
            List<PayPassage> passageList = payPassageService.list();

            for (int i = 0; i < passageList.size(); i++) {
                PayPassage passage = passageList.get(i);
                Long amount = -passage.getBalance();

                Long beforeBalance = passage.getBalance();
                Long afterBalance = passage.getBalance() + amount;

                if (beforeBalance.longValue() != 0) {

                    //插入通道流水记录
                    PassageTransactionHistory passageTransactionHistory = new PassageTransactionHistory();
                    passageTransactionHistory.setAmount(amount);
                    passageTransactionHistory.setPayPassageId(passage.getPayPassageId());
                    passageTransactionHistory.setPayPassageName(passage.getPayPassageName());

                    passageTransactionHistory.setBeforeBalance(beforeBalance);
                    passageTransactionHistory.setAfterBalance(afterBalance);
                    passageTransactionHistory.setFundDirection(amount >= 0 ? CS.FUND_DIRECTION_INCREASE : CS.FUND_DIRECTION_REDUCE);
                    passageTransactionHistory.setBizType(PassageTransactionHistory.BIZ_TYPE_RESET);

                    passageTransactionHistory.setCreatedUid(0L);
                    passageTransactionHistory.setCreatedLoginName("日切清零");
                    passageTransactionHistory.setRemark("");
                    passageTransactionHistoryService.save(passageTransactionHistory);

                    payPassageService.updateBalance(passage.getPayPassageId(), amount);
                }
            }

            log.info("====执行通道自动日切余额清零完毕====");
        }
    }

    private void CheckPassageState(Date start, Date end, PayPassage payPassage) {
        Date now = DateUtil.date();

        boolean conformTime = false;

        if (now.compareTo(start) >= 0 && now.compareTo(end) <= 0) {
            conformTime = true;
        }

        Byte state = conformTime ? CS.YES : CS.NO;
        payPassage.setState(state);
        payPassageService.updatePassageInfo(payPassage);
    }
}