package com.jeequan.jeepay.pay.channel.huayue;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class HuayueParamsModel extends NormalMchParams {

    // 支付方式
    private String payMethod;
}
