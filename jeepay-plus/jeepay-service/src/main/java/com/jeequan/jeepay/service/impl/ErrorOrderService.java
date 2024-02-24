package com.jeequan.jeepay.service.impl;

import com.jeequan.jeepay.core.entity.ErrorOrder;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.service.mapper.ErrorOrderMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 异常订单表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2024-02-25
 */
@Service
public class ErrorOrderService extends ServiceImpl<ErrorOrderMapper, ErrorOrder> {

    /**
     * 保存异常订单
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

}
