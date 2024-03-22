package com.jeequan.jeepay.pay.channel.shpay;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.SignatureUtils;
import com.jeequan.jeepay.pay.channel.AbstractPaymentService;
import com.jeequan.jeepay.pay.model.PayConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class ShpayPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[SH支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.SHPAY;
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
            NormalMchParams normalMchParams = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), NormalMchParams.class);

            Map<String, Object> map = new HashMap<>();
            String key = normalMchParams.getSecret();

            String mchId = normalMchParams.getMchNo();
            String orderSn = payOrder.getPayOrderId();
            String money = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String goodsDesc = "goodsDesc";
            String time = System.currentTimeMillis() / 1000 + "";

            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());

            map.put("mchId", mchId);
            map.put("orderSn", orderSn);
            map.put("money", money);
            map.put("goodsDesc", goodsDesc);
            map.put("time", time);
            map.put("notifyUrl", notifyUrl);
            String signStr = "mchId=" + mchId + "&orderSn=" + orderSn + "&money=" + money + "&goodsDesc=" + goodsDesc + "&notifyUrl=" + notifyUrl + "&time=" + time + "&key=" + key;
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                    .execute();
            // 处理响应
            raw = response.body();
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {
                JSONObject data = result.getJSONObject("data");

                URL urlObj = new URL(payGateway);

                String payUrl = urlObj.getHost() + data.getString("payUrl");
                String passageOrderId = data.getString("orderNum");

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
        String key = "UXNBbFR2YU1TYzZYZUFFWFpCVG90ZXZT";

        String mchId = "10005";
        String orderSn = RandomStringUtils.random(15, true, true);
        String money = AmountUtil.convertCent2Dollar(20000L);
        String goodsDesc = "goodsDesc";
        String time = System.currentTimeMillis() / 1000 + "";

        String notifyUrl = "https://www.test.com";

        map.put("mchId", mchId);
        map.put("orderSn", orderSn);
        map.put("money", money);
        map.put("goodsDesc", goodsDesc);
        map.put("time", time);
        map.put("notifyUrl", notifyUrl);
        String signStr = "mchId=" + mchId + "&orderSn=" + orderSn + "&money=" + money + "&goodsDesc=" + goodsDesc + "&notifyUrl=" + notifyUrl + "&time=" + time + "&key=" + key;
        String sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("sign", sign);

        String payGateway = "http://107.151.247.97:8081/prod-api/outOrder/outsideOrder/create";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                .execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}