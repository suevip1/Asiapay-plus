package com.jeequan.jeepay.pay.channel.bichi;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class BichiParamsModel extends NormalMchParams {

    private String channel;
}
