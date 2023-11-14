package com.jeequan.jeepay.pay.channel.testpay;


import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.channel.AbstractPaymentService;
import com.jeequan.jeepay.pay.model.PayConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestpayPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[Test Pay]";
    @Override
    public String getIfCode() {
        return CS.IF_CODE.TESTPAY;
    }

  

    @Override
    public AbstractRS pay(UnifiedOrderRQ bizRQ, PayOrder payOrder, PayConfigContext payConfigContext) throws Exception {
        log.info("[{}]开始下单:{}", LOG_TAG, payOrder.getPayOrderId());
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        UnifiedOrderRS res = ApiResBuilder.buildSuccess(UnifiedOrderRS.class);
        res.setChannelRetMsg(channelRetMsg);
        channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
        res.setPayDataType(CS.PAY_DATA_TYPE.PAY_URL);
        res.setPayData("https://www.google.com/testpay");

        return res;
    }

}
