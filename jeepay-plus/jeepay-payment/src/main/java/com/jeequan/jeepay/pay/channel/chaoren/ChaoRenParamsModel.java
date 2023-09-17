package com.jeequan.jeepay.pay.channel.chaoren;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class ChaoRenParamsModel extends NormalMchParams {

    private String channelId;

    // 请求RSA私钥
    private String requestPrivateKey;
}
