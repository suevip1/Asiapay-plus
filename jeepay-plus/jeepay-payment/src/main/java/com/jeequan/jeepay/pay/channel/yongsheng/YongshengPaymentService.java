package com.jeequan.jeepay.pay.channel.yongsheng;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.JeepayKit;
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

import java.util.HashMap;
import java.util.Map;


/**
 * 永胜支付
 */
@Service
@Slf4j
public class YongshengPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "永胜支付";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YONGSHENG;
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
            String productId = normalMchParams.getPayType();
            String mchOrderNo = payOrder.getPayOrderId();
            long amount = payOrder.getAmount();
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String subject = "subject";
            String body = "body";

            map.put("mchId", mchId);
            map.put("productId", productId);
            map.put("mchOrderNo", mchOrderNo);
            map.put("amount", amount);
            map.put("notifyUrl", notifyUrl);
            map.put("subject", subject);
            map.put("body", body);
            String sign = JeepayKit.getSign(map, key).toUpperCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            raw = response.body();
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("retCode").equals("SUCCESS")) {
                String payUrl = result.getString("payUrl");
                String passageOrderId = result.getString("payOrderId");

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
        String key = "OOMTJP4WVWAEJCWEJWVTBP1L9MFRQ57IX4BFDJGEEWJYGX10FD1CQBMI582BUO5J7JEAS2GFNAUVBXAXZBEKLSXNQ3WXFH0PKNJPLH7BQYHWDFSTBCYZUJVVFPX499JX";

        String mchId = "20230501176";
        String productId = "20003";
        String mchOrderNo = RandomStringUtils.random(15, true, true);
        long amount = 10000;
        String notifyUrl = "https://www.test.com";
        String subject = "subject";
        String body = "body";

        map.put("mchId", mchId);
        map.put("productId", productId);
        map.put("mchOrderNo", mchOrderNo);
        map.put("amount", amount);
        map.put("notifyUrl", notifyUrl);
        map.put("subject", subject);
        map.put("body", body);
        String sign = JeepayKit.getSign(map, key).toUpperCase();
        map.put("sign", sign);

        String payGateway = "http://www.eefnsfk.cn:12033/api/pay/create";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }

}