package com.jeequan.jeepay.pay.channel.gulang;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;


@Data
public class GulangParams extends NormalMchParams {

    private String apiKey;
}
