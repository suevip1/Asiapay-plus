package com.jeequan.jeepay.pay.channel.tangbang;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class TangBangParamsModel extends NormalMchParams {

    //AES密钥
    private String aesSecret;
}
