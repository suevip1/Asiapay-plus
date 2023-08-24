package com.jeequan.jeepay.service.impl;

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

    public boolean AddRecord(String mchNo, Long amount,String userName) {
        RobotsMchRecords robotsMchRecords = new RobotsMchRecords();
        robotsMchRecords.setMchNo(mchNo);
        robotsMchRecords.setAmount(amount);
        robotsMchRecords.setUserName(userName);
        robotsMchRecords.setCreatedAt(new Date());
        return save(robotsMchRecords);
    }
}
