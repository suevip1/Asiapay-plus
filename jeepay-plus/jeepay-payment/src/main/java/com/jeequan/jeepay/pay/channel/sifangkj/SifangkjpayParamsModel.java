package com.jeequan.jeepay.pay.channel.sifangkj;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class SifangkjpayParamsModel extends NormalMchParams {

    // RSA私钥
    private String privateKey;
}
