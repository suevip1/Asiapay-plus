package com.jeequan.jeepay.pay.channel.rongfu;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class RongFuParamsModel extends NormalMchParams {

    private String appId;
}
