package com.jeequan.jeepay.pay.channel.zhouyi;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class ZhouyiMchParams extends NormalMchParams {

    private String cardType;
}
