package com.jeequan.jeepay.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.DivisionRecord;
import com.jeequan.jeepay.core.entity.PassageTransactionHistory;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.service.mapper.DivisionRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 分账记录表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-07-18
 */
@Slf4j
@Service
public class DivisionRecordService extends ServiceImpl<DivisionRecordMapper, DivisionRecord> {
    /**
     * 申请结算订单入库
     *
     * @param userNO
     * @param userName
     * @param amount
     * @param fee
     * @param remark
     * @param userType 结算用户类型
     * @return
     */
    public boolean SaveDivisionRecord(String userNO, String userName, Long amount, Long fee, String remark, Byte userType) {
        DivisionRecord divisionRecord = new DivisionRecord();
        divisionRecord.setUserNo(userNO);
        divisionRecord.setUserName(userName);
        divisionRecord.setAmount(amount);
        divisionRecord.setDivisionAmount(amount - fee);
        divisionRecord.setDivisionAmountFee(0L);
        divisionRecord.setDivisionFeeRate(BigDecimal.ZERO);
        divisionRecord.setState(DivisionRecord.STATE_WAIT);
        divisionRecord.setUserType(userType);
        Date nowDate = new Date();
        divisionRecord.setCreatedAt(nowDate);
        divisionRecord.setExpiredTime(DateUtil.offsetMinute(nowDate, CS.ORDER_EXPIRED_TIME)); //订单过期时间
        divisionRecord.setPayType(DivisionRecord.PAY_TYPE_MANUAL);
        divisionRecord.setAccType(DivisionRecord.ACC_TYPE_MANUAL);
        divisionRecord.setRemark(remark);
        return save(divisionRecord);
    }

    public boolean SaveDivisionRecord(String userNO, String userName, Long amount, Long fee, String remark, Byte userType, Date nowDate) {
        DivisionRecord divisionRecord = new DivisionRecord();
        divisionRecord.setUserNo(userNO);
        divisionRecord.setUserName(userName);
        divisionRecord.setAmount(amount);
        divisionRecord.setDivisionAmount(amount - fee);
        divisionRecord.setDivisionAmountFee(fee);
        divisionRecord.setDivisionFeeRate(BigDecimal.ZERO);
        divisionRecord.setState(DivisionRecord.STATE_WAIT);
        divisionRecord.setUserType(userType);
        divisionRecord.setCreatedAt(nowDate);
        divisionRecord.setExpiredTime(DateUtil.offsetMinute(nowDate, CS.ORDER_EXPIRED_TIME)); //订单过期时间
        divisionRecord.setPayType(DivisionRecord.PAY_TYPE_MANUAL);
        divisionRecord.setAccType(DivisionRecord.ACC_TYPE_MANUAL);
        divisionRecord.setRemark(remark);
        return save(divisionRecord);
    }

    public Integer updateRecordExpired() {

        DivisionRecord record = new DivisionRecord();
        record.setState(PayOrder.STATE_CLOSED);

        return baseMapper.update(record,
                DivisionRecord.gw()
                        .in(DivisionRecord::getState, DivisionRecord.STATE_WAIT)
                        .le(DivisionRecord::getExpiredTime, new Date())
        );
    }

    /**
     * 清理分账记录
     *
     * @param offsetDate
     * @param pageSize
     */
    public void ClearDivisionRecordHistory(Date offsetDate, int pageSize) {
        long currentPageIndex = 1;
        while (true) {
            try {
                LambdaQueryWrapper<DivisionRecord> lambdaQueryWrapper = DivisionRecord.gw().le(DivisionRecord::getCreatedAt, offsetDate);
                IPage<DivisionRecord> iPage = page(new Page(1, pageSize), lambdaQueryWrapper);

                log.info("【过期分账记录共计】{}", iPage.getTotal());
                log.info("【过期分账记录页数】{}", iPage.getPages());

                if (iPage == null || iPage.getRecords().isEmpty()) { //本次查询无结果, 不再继续查询;
                    log.info("【本次查询分账记录无结果】{} 中止定时任务", new Date());
                    break;
                }

                List ids = new ArrayList();
                for (DivisionRecord record : iPage.getRecords()) {
                    ids.add(record.getRecordId());
                }
                boolean result = removeByIds(ids);

                log.info("【数据定时清理任务执行中】 本次清理分账记录{}条", pageSize);
                log.info("【数据定时清理任务执行中】 本次清理 DivisionRecord {}", result);

                currentPageIndex++;

                log.info("【数据定时清理任务执行中】 当前次数 {}", currentPageIndex);
                log.info("【过期分账记录页数】{}", iPage.getPages());
            } catch (Exception e) { //出现异常，直接退出，避免死循环。
                log.info("【数据定时清理分账记录任务异常】error {}", e);
                break;
            }
        }
    }
}
