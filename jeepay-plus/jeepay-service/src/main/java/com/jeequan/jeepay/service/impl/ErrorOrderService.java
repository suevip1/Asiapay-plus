package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeequan.jeepay.core.entity.ErrorOrder;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.service.mapper.ErrorOrderMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 异常异常订单表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2024-02-25
 */
@Slf4j
@Service
public class ErrorOrderService extends ServiceImpl<ErrorOrderMapper, ErrorOrder> {

    /**
     * 保存异常异常订单
     * @param mchInfo
     * @param mchOrderNo
     * @param amount
     * @param errorMsg
     * @param reqParams
     */
    public void saveErrorOrder(MchInfo mchInfo, String mchOrderNo, Long amount, String errorMsg, String reqParams) {
        ErrorOrder errorOrder = new ErrorOrder();
        errorOrder.setMchNo(mchInfo.getMchNo());
        errorOrder.setMchName(mchInfo.getMchName());

        errorOrder.setMchOrderNo(mchOrderNo);
        errorOrder.setMchReq(reqParams);
        errorOrder.setMchResp(errorMsg);

        errorOrder.setAmount(amount);
        save(errorOrder);
    }

    public void ClearErrorOrder(Date offsetDate, int pageSize) {
        long currentPageIndex = 1;

        log.info("【数据定时清理任务开始执行】{}", new Date());
        LambdaQueryWrapper<ErrorOrder> lambdaQueryWrapper = ErrorOrder.gw().le(ErrorOrder::getCreatedAt, offsetDate);

        while (true) {

            try {
                IPage<ErrorOrder> errorOrderIPage = page(new Page(1, pageSize), lambdaQueryWrapper);

                log.info("【过期异常订单共计】{}", errorOrderIPage.getTotal());
                log.info("【过期异常订单页数】{}", errorOrderIPage.getPages());

                if (errorOrderIPage == null || errorOrderIPage.getRecords().isEmpty()) { //本次查询无结果, 不再继续查询;
                    log.info("【本次查询无结果】{} 中止定时任务", new Date());
                    break;
                }

                List ids = new ArrayList();
                for (ErrorOrder payOrder : errorOrderIPage.getRecords()) {
                    ids.add(payOrder.getErrorOrderId());
                }
                boolean resultOrder = removeByIds(ids);

                log.info("【数据定时清理任务执行中】 本次清理过期异常订单{}条", pageSize);
                log.info("【数据定时清理任务执行中】 本次清理 order {}", resultOrder);

                currentPageIndex++;

                log.info("【数据定时清理任务执行中】 当前次数 {}", currentPageIndex);
                log.info("【过期异常订单页数】{}", errorOrderIPage.getPages());
            } catch (Exception e) { //出现异常，直接退出，避免死循环。
                log.info("【数据定时清理任务异常】error {}", e);
                break;
            }
        }
    }

}
