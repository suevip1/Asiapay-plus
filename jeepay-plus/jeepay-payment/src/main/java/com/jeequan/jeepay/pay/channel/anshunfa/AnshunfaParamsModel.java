package com.jeequan.jeepay.pay.channel.anshunfa;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class AnshunfaParamsModel extends NormalMchParams {

    private String groupCode;
}
