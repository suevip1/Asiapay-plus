package com.jeequan.jeepay.pay.channel.maiyi;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class MayiParamsModel extends NormalMchParams {

    private String channelCode;
}
