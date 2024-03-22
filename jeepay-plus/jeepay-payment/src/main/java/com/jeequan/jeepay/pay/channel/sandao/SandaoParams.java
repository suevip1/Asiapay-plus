package com.jeequan.jeepay.pay.channel.sandao;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;


@Data
public class SandaoParams extends NormalMchParams {

    private String username;
    private String password;
    private String channel;
    private String tokenUrl;
}
