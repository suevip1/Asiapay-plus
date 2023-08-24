package com.jeequan.jeepay.core.model.params;

import com.jeequan.jeepay.core.entity.PayOrder;
import lombok.Data;

/**
 * 商户余额变动参数类
 */
@Data
public class BalanceChangeParams {

    /**
     * 变动类型,1：入账 2：调账
     **/
    private Byte changeType;

    /**
     * 订单对象
     */
    private PayOrder payOrder;

    /**
     * 变动金额
     */
    private Long changeAmount;
}
