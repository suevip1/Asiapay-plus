package com.jeequan.jeepay.pay.channel.yunyin;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class YunyinParamsModel extends NormalMchParams {

    private String reserve;
}
