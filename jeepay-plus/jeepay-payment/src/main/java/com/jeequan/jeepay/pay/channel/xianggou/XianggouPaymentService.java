package com.jeequan.jeepay.pay.channel.xianggou;

import cn.hutool.http.HttpUtil;
import com.alibaba.druid.util.StringUtils;
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

import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class XianggouPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[享购支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.XIANGGOU;
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

            String paymentType = normalMchParams.getPayType();
            String uid = normalMchParams.getMchNo();
            String merchantTransNo = payOrder.getPayOrderId();
            String totalAmount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String callBackUrl = getNotifyUrl(payOrder.getPayOrderId());
            String returnUrl = callBackUrl;
            String realPayerName = StringUtils.isEmpty(bizRQ.getExtParam()) ? "" : bizRQ.getExtParam();

            map.put("paymentType", paymentType);
            map.put("uid", uid);
            map.put("merchantTransNo", merchantTransNo);
            map.put("totalAmount", totalAmount);
            map.put("realPayerName", realPayerName);
            map.put("returnUrl", returnUrl);
            map.put("callBackUrl", callBackUrl);

            String signStr = SignatureUtils.getSignContent(map, new String[]{"returnUrl", "callBackUrl"}, new String[]{}) + "&key=" + key;
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("Status").equals("1")) {

                String payUrl = result.getString("Data");
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
        String key = "da7b1429af1209b4ca1243e25d96c7bc4a4888d9d7ff1139bb9c669721eb8d80e4e1f99c6bcde7d7dd8383d22defd8ccc87a34cd79e0c4ae4d2169e1607e0fd170645cc8bf6c25063c120d1e6244885cf20b5be137d19ff85f4fee1fd36eeef3";

        String paymentType = "3";
        String uid = "2333";
        String merchantTransNo = RandomStringUtils.random(15, true, true);
        String totalAmount = AmountUtil.convertCent2Dollar(10000L);
        String callBackUrl = "http://www.test.com";
        String returnUrl = callBackUrl;
        String realPayerName = "name";

        map.put("paymentType", paymentType);
        map.put("uid", uid);
        map.put("merchantTransNo", merchantTransNo);
        map.put("totalAmount", totalAmount);
        map.put("realPayerName", realPayerName);
        map.put("returnUrl", returnUrl);
        map.put("callBackUrl", callBackUrl);

        String signStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{"returnUrl", "callBackUrl"}) + "&key=" + key;
        String sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("sign", sign);

        String payGateway = "https://sgscapp.gyyfd.cn/api/order/CreateOrderUrlForJson";
        log.info("[{}]请求:{}", LOG_TAG, map);
        // 发送POST请求并指定JSON数据
//        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
//        // 处理响应
//        raw = response.body();
        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
