package com.jeequan.jeepay.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.RobotsMch;
import com.jeequan.jeepay.core.entity.RobotsMchRecords;
import com.jeequan.jeepay.service.mapper.RobotsMchRecordsMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-08-21
 */
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
        wrapper.eq("chat_id", chatId).eq("state", CS.YES).eq("type", RobotsMchRecords.DAY_TYPE).orderByDesc("created_at").last("LIMIT 1");
        RobotsMchRecords robotsMchRecords = getOne(wrapper);
        if (robotsMchRecords == null) {
            return false;
        } else {
            robotsMchRecords.setState(CS.NO);
            robotsMchRecords.setRemark(userName);
            return updateById(robotsMchRecords);
        }
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

    public boolean AddTotalRecord(Long chatId, Long amount, String userName) {
        RobotsMchRecords robotsMchRecords = new RobotsMchRecords();
        robotsMchRecords.setAmount(amount);
        robotsMchRecords.setChatId(chatId);
        robotsMchRecords.setUserName(userName);
        robotsMchRecords.setCreatedAt(new Date());
        robotsMchRecords.setType(RobotsMchRecords.TOTAL_TYPE);
        return save(robotsMchRecords);
    }
}
