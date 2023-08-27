package com.jeequan.jeepay.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.RobotsMchRecords;
import com.jeequan.jeepay.core.entity.RobotsMch;
import com.jeequan.jeepay.core.entity.RobotsMchRecords;
import com.jeequan.jeepay.service.mapper.RobotsMchRecordsMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-08-21
 */
@Slf4j
@Service
public class RobotsMchRecordsService extends ServiceImpl<RobotsMchRecordsMapper, RobotsMchRecords> {

    public boolean AddDayRecord(Long chatId, Long amount, String userName) {
        RobotsMchRecords robotsMchRecords = new RobotsMchRecords();
        robotsMchRecords.setAmount(amount);
        robotsMchRecords.setChatId(chatId);
        robotsMchRecords.setUserName(userName);
        robotsMchRecords.setCreatedAt(new Date());
        robotsMchRecords.setType(RobotsMchRecords.DAY_TYPE);
        return save(robotsMchRecords);
    }

    /**
     * 撤销下发
     *
     * @param chatId
     * @param userName
     * @return
     */
    public boolean RemoveRecentlyRecord(Long chatId, String userName, Date date) {

        //一天的开始，结果：2017-03-01 00:00:00
        Date beginOfDay = DateUtil.beginOfDay(date);

        //一天的结束，结果：2017-03-01 23:59:59
        Date endOfDay = DateUtil.endOfDay(date);

        QueryWrapper<RobotsMchRecords> wrapper = new QueryWrapper<>();
        wrapper.eq("chat_id", chatId).eq("state", CS.YES).eq("type", RobotsMchRecords.DAY_TYPE).orderByDesc("created_at").last("LIMIT 1");
        wrapper.ge("created_at", DateUtil.format(beginOfDay, "yyyy-MM-dd HH:mm:ss"));
        wrapper.le("created_at", DateUtil.format(endOfDay, "yyyy-MM-dd HH:mm:ss"));

        RobotsMchRecords robotsMchRecords = getOne(wrapper);
        if (robotsMchRecords == null) {
            return false;
        } else {
            robotsMchRecords.setState(CS.NO);
            robotsMchRecords.setRemark(userName);
            return updateById(robotsMchRecords);
        }
    }

    public boolean RemoveAllRecordByDate(Long chatId, String userName, Date date) {
        QueryWrapper<RobotsMchRecords> wrapper = new QueryWrapper<>();

        //一天的开始，结果：2017-03-01 00:00:00
        Date beginOfDay = DateUtil.beginOfDay(date);

        //一天的结束，结果：2017-03-01 23:59:59
        Date endOfDay = DateUtil.endOfDay(date);
        wrapper.ge("created_at", DateUtil.format(beginOfDay, "yyyy-MM-dd HH:mm:ss"));
        wrapper.le("created_at", DateUtil.format(endOfDay, "yyyy-MM-dd HH:mm:ss"));
        wrapper.eq("chat_id", chatId).eq("state", CS.YES).eq("type", RobotsMchRecords.DAY_TYPE).orderByDesc("created_at");
        List<RobotsMchRecords> robotsMchRecords = list(wrapper);

        for (int i = 0; i < robotsMchRecords.size(); i++) {
            robotsMchRecords.get(i).setState(CS.NO);
            robotsMchRecords.get(i).setRemark(userName);
        }
        return updateBatchById(robotsMchRecords);
    }

    /**
     * 撤销记账
     *
     * @param chatId
     * @param userName
     * @return
     */
    public Long RemoveRecentlyRecordTotal(Long chatId, String userName, Date date) {
        //一天的开始，结果：2017-03-01 00:00:00
        Date beginOfDay = DateUtil.beginOfDay(date);
        //一天的结束，结果：2017-03-01 23:59:59
        Date endOfDay = DateUtil.endOfDay(date);

        QueryWrapper<RobotsMchRecords> wrapper = new QueryWrapper<>();
        wrapper.eq("chat_id", chatId).eq("state", CS.YES).eq("type", RobotsMchRecords.TOTAL_TYPE).orderByDesc("created_at").last("LIMIT 1");
        wrapper.ge("created_at", DateUtil.format(beginOfDay, "yyyy-MM-dd HH:mm:ss"));
        wrapper.le("created_at", DateUtil.format(endOfDay, "yyyy-MM-dd HH:mm:ss"));

        RobotsMchRecords robotsMchRecords = getOne(wrapper);
        if (robotsMchRecords == null) {
            return 0L;
        } else {
            robotsMchRecords.setState(CS.NO);
            robotsMchRecords.setRemark(userName);
            updateById(robotsMchRecords);
            return robotsMchRecords.getAmount();
        }
    }

    public Long RemoveAllRecordTotalByDate(Long chatId, String userName, Date date) {
        QueryWrapper<RobotsMchRecords> wrapper = new QueryWrapper<>();

        //一天的开始，结果：2017-03-01 00:00:00
        Date beginOfDay = DateUtil.beginOfDay(date);

        //一天的结束，结果：2017-03-01 23:59:59
        Date endOfDay = DateUtil.endOfDay(date);
        wrapper.ge("created_at", DateUtil.format(beginOfDay, "yyyy-MM-dd HH:mm:ss"));
        wrapper.le("created_at", DateUtil.format(endOfDay, "yyyy-MM-dd HH:mm:ss"));
        wrapper.eq("chat_id", chatId).eq("state", CS.YES).eq("type", RobotsMchRecords.TOTAL_TYPE).orderByDesc("created_at");
        List<RobotsMchRecords> robotsMchRecords = list(wrapper);

        Long amount = 0L;
        for (int i = 0; i < robotsMchRecords.size(); i++) {
            robotsMchRecords.get(i).setState(CS.NO);
            robotsMchRecords.get(i).setRemark(userName);
            amount += robotsMchRecords.get(i).getAmount();
        }
        updateBatchById(robotsMchRecords);
        return amount;
    }

    public boolean AddTotalRecord(Long chatId, Long amount, String userName) {
        RobotsMchRecords robotsMchRecords = new RobotsMchRecords();
        robotsMchRecords.setAmount(amount);
        robotsMchRecords.setChatId(chatId);
        robotsMchRecords.setUserName(userName);
        robotsMchRecords.setCreatedAt(new Date());
        robotsMchRecords.setType(RobotsMchRecords.TOTAL_TYPE);
        return save(robotsMchRecords);
    }


    public void ClearRecord(Date offsetDate, int pageSize) {
        long currentPageIndex = 1;

        log.info("【数据定时清理任务开始执行】{}", new Date());
        LambdaQueryWrapper<RobotsMchRecords> lambdaQueryWrapper = RobotsMchRecords.gw().le(RobotsMchRecords::getCreatedAt, offsetDate);

        while (true) {

            try {
                IPage<RobotsMchRecords> payOrderIPage = page(new Page(1, pageSize), lambdaQueryWrapper);

                log.info("【过期记账共计】{}", payOrderIPage.getTotal());
                log.info("【过期记账页数】{}", payOrderIPage.getPages());

                if (payOrderIPage == null || payOrderIPage.getRecords().isEmpty()) { //本次查询无结果, 不再继续查询;
                    log.info("【本次查询无结果】{} 中止定时任务", new Date());
                    break;
                }

                List ids = new ArrayList();
                for (RobotsMchRecords payOrder : payOrderIPage.getRecords()) {
                    ids.add(payOrder.getRobotMchRecordId());
                }
                boolean resultOrder = removeByIds(ids);

                log.info("【数据定时清理任务执行中】 本次清理过期记账{}条", pageSize);
                log.info("【数据定时清理任务执行中】 本次清理 order {}", resultOrder);

                currentPageIndex++;

                log.info("【数据定时清理任务执行中】 当前次数 {}", currentPageIndex);
                log.info("【过期记账页数】{}", payOrderIPage.getPages());
            } catch (Exception e) { //出现异常，直接退出，避免死循环。
                log.info("【数据定时清理任务异常】error {}", e);
                break;
            }
        }
    }
}
