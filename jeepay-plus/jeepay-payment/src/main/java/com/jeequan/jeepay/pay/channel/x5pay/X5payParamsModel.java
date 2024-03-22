package com.jeequan.jeepay.pay.channel.x5pay;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class X5payParamsModel extends NormalMchParams {

    //支付通道
    private String payment;
}
