package com.jeequan.jeepay.pay.channel.xxpay11;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.SignatureUtils;
import com.jeequan.jeepay.pay.channel.AbstractPaymentService;
import com.jeequan.jeepay.pay.channel.rongfu.RongFuParamsModel;
import com.jeequan.jeepay.pay.model.PayConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class Xxpay11PaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[xxpay11支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.XXPAY11;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ bizRQ, PayOrder payOrder, PayConfigContext payConfigContext) {
        log.info("[{}]开始下单:{}", LOG_TAG, payOrder.getPayOrderId());
        UnifiedOrderRS res = ApiResBuilder.buildSuccess(UnifiedOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);
        String raw = "";
        try {
            PayPassage payPassage = payConfigContext.getPayPassage();
            //支付参数转换
            RongFuParamsModel rongFuParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), RongFuParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = rongFuParamsModel.getSecret();

            String mchOrderNo = payOrder.getPayOrderId();
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String returnUrl = notifyUrl;
            String mchId = rongFuParamsModel.getMchNo();
            String productId = rongFuParamsModel.getPayType();
            String clientIp = payOrder.getClientIp();

            map.put("mchOrderNo", mchOrderNo);
            map.put("amount", amount);
            map.put("notifyUrl", notifyUrl);
            map.put("returnUrl", returnUrl);
            map.put("mchId", mchId);
            map.put("productId", productId);
            map.put("clientIp", clientIp);

            String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
            String sign = SignatureUtils.md5(signContent + key).toLowerCase();
            map.put("sign", sign);
            map.put("backType", 1);

            String payGateway = rongFuParamsModel.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("retCode").equals("SUCCESS")) {

                String payUrl = result.getJSONObject("payParams").getString("payUrl");
                String passageOrderId = "";

                res.setPayDataType(CS.PAY_DATA_TYPE.PAY_URL);
                res.setPayData(payUrl);

                channelRetMsg.setChannelOrderId(passageOrderId);
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
            } else {
                //出码失败
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            }
        } catch (Exception e) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.SYS_ERROR);
            log.error("[{}] 异常: {}", LOG_TAG, payOrder.getPayOrderId());
            log.error(e.getMessage(), e);
        }
        return res;
    }

    public static void main(String[] args) {
        String raw = "";

        Map<String, Object> map = new HashMap<>();
        String key = "H9TYYAN5R0Q34E36MX0AA0JS1DGQ6WCY0M582WGZ2CN8W998Z4J73PUD150RWBFA7PXFP2F5GXKXBZJA20JFTJCIAH2KZ02XPZQTSLD6AKVAJ30HMIYF8G5IOZUI2ZPD";

        String mchOrderNo = RandomStringUtils.random(15, true, true);
        String amount = AmountUtil.convertCent2Dollar(10000L);
        String notifyUrl = "https://www.test.com";
        String returnUrl = notifyUrl;
        String mchId = "1278";
        String productId = "206";
        String clientIp = "127.0.0.1";

        map.put("mchOrderNo", mchOrderNo);
        map.put("amount", amount);
        map.put("notifyUrl", notifyUrl);
        map.put("returnUrl", returnUrl);
        map.put("mchId", mchId);
        map.put("productId", productId);
        map.put("clientIp", clientIp);

        String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
        String sign = SignatureUtils.md5(signContent + key).toLowerCase();
        map.put("sign", sign);
        map.put("backType", 1);

        String payGateway = "http://206.119.73.132:22887/api/pay1/create_order";

        raw = HttpUtil.post(payGateway, map,10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}