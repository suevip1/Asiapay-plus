package com.jeequan.jeepay.pay.channel.wangting;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class WangTingParamsModel extends NormalMchParams {

    private String channelId;
}
