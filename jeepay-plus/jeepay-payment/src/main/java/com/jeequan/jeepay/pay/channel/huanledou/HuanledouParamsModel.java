package com.jeequan.jeepay.pay.channel.huanledou;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class HuanledouParamsModel extends NormalMchParams {

    private String appId;
}
