package com.jeequan.jeepay.pay.channel.kunpeng;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class KunpengPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[鲲鹏支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.KUNPENG;
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
            String code = normalMchParams.getPayType();

            String mchOrderNo = payOrder.getPayOrderId();
            String amount = AmountUtil.convertCent2DollarShort(payOrder.getAmount());
            String title = "pay";
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String requestTime = dateFormat.format(new Date());

            String ip = payOrder.getClientIp();
            String version = "1.0";


            map.put("mchId", mchId);
            map.put("code", code);
            map.put("mchOrderNo", mchOrderNo);
            map.put("amount", amount);
            map.put("title", title);
            map.put("notifyUrl", notifyUrl);
            map.put("ip", ip);
            map.put("requestTime", requestTime);
            map.put("version", version);


            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + key;
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            raw = response.body();

            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {
                JSONObject data = result.getJSONObject("data");
                String payUrl = data.getString("payUrl");
                String passageOrderId = data.getString("sysOrderNo");

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
        String key = "da32b93531c84d76a60d35bfd8f637af";

        String mchId = "1746601631627218944";
        String code = "1080";

        String mchOrderNo = RandomStringUtils.random(15, true, true);
        String amount = AmountUtil.convertCent2DollarShort(10000L);
        String title = "pay";
        String notifyUrl = "http://www.test.com";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String requestTime = dateFormat.format(new Date());

        String ip = "127.0.0.1";
        String version = "1.0";


        map.put("mchId", mchId);
        map.put("code", code);
        map.put("mchOrderNo", mchOrderNo);
        map.put("amount", amount);
        map.put("title", title);
        map.put("notifyUrl", notifyUrl);
        map.put("ip", ip);
        map.put("requestTime", requestTime);
        map.put("version", version);


        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + key;
        String sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("sign", sign);


        String payGateway = "https://pay.qe.qwguagua.com/rest/pay/create";

        // 发送POST请求并指定JSON数据
        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();

        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}