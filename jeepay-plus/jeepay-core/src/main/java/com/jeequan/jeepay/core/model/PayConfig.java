package com.jeequan.jeepay.core.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 支付配置
 */
@Data
public class PayConfig implements Serializable {

    /**
     * 通道余额过零点自动清零
     */
    private String payPassageAutoClean;

    /**
     * 商户提现手续费(固定)
     */
    private Long mchFee;

    /**
     * 商户提现手续费(比例)
     */
    private BigDecimal mchFeeRate;

    private Long mchMinWithdraw;

    /**
     * 代理提现手续费(固定)
     */
    private Long agentFee;

    /**
     * 代理提现手续费(比例)
     */
    private BigDecimal agentFeeRate;

    private Long agentMinWithdraw;

}