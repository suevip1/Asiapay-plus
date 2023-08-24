package com.jeequan.jeepay.pay.model;


import com.jeequan.jeepay.core.entity.*;
import lombok.Data;

/**
 * 支付信息缓存
 */
@Data
public class PayConfigContext {

    /**
     * 商户信息
     */
    private MchInfo mchInfo;

    /**
     * 产品信息
     */
    private Product product;

    /**
     *通道对象
     */
    private PayPassage payPassage;

    /**
     * 通道-商户绑定关系
     */
    private MchPayPassage mchPayPassage;

    /**
     * 商户-产品绑定关系  商户产品费率
     */
    private MchProduct mchProduct;


}
