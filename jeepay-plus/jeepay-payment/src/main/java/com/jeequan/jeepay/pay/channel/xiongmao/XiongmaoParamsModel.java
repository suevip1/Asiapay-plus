package com.jeequan.jeepay.pay.channel.xiongmao;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class XiongmaoParamsModel extends NormalMchParams {

    // 支付类型
    private String typeCode;
}
