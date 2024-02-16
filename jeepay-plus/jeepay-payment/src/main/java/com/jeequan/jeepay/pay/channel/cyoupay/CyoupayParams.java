package com.jeequan.jeepay.pay.channel.cyoupay;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class CyoupayParams extends NormalMchParams {

    private String key;

    private String merchantKey;

    private String code;
}
