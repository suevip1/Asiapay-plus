package com.jeequan.jeepay.pay.channel.xique;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class XiqueParamsModel extends NormalMchParams {

    // RSA私钥
    private String requestPrivateKey;

    // RSA公钥
    private String requestPublicKey;
}
