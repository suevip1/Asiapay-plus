package com.jeequan.jeepay.pay.channel.bolin;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class BolinParamsModel extends NormalMchParams {

    // RSA私钥
    private String requestPrivateKey;

    // RSA公钥
    private String requestPublicKey;
}
