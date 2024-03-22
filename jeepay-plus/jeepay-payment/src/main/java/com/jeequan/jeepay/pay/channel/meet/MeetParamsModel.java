package com.jeequan.jeepay.pay.channel.meet;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

@Data
public class MeetParamsModel extends NormalMchParams {

    //支付类型(枚举类型:[guide、native、h5])
    private String way;

    // 支付方式(枚举类型:[guide、wechat、alipay])
    private String payment;
}
