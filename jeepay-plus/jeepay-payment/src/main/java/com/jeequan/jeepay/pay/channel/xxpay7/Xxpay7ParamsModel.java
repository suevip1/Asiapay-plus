package com.jeequan.jeepay.pay.channel.xxpay7;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class Xxpay7ParamsModel extends NormalMchParams {

    /**
     * appId
     */
    private String appId;
}
