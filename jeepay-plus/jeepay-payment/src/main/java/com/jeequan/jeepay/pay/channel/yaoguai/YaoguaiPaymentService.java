package com.jeequan.jeepay.pay.channel.yaoguai;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
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
public class YaoguaiPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[妖怪支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YAOGUAI;
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

            String merchno = normalMchParams.getMchNo();
            String paytype = normalMchParams.getPayType();
            String traceno = payOrder.getPayOrderId();
            Long total_fee = payOrder.getAmount();
            String notifyurl = getNotifyUrl(payOrder.getPayOrderId());
            String payIp = payOrder.getClientIp();

            map.put("merchno", merchno);
            map.put("paytype", paytype);
            map.put("traceno", traceno);
            map.put("total_fee", total_fee);
            map.put("notifyurl", notifyurl);
            map.put("payIp", payIp);

            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + key;
            String sign = SignatureUtils.md5(SignatureUtils.md5(signStr)).toUpperCase();
            map.put("sign", sign);


            String payGateway = normalMchParams.getPayGateway();

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            raw = response.body();
            log.info("[{}]请求响应:{}", LOG_TAG, raw);

            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("respCode").equals("E0000")) {

                String payUrl = result.getString("barUrl");
                String passageOrderId = result.getString("trade_no");

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
        String key = "A792C79FC94569A467322A95B8F8308F";

        String merchno = "G80190";
        String paytype = "ZFBUID";
        String traceno = RandomStringUtils.random(15, true, true);
        Long total_fee = 20000L;
        String notifyurl = "http://www.test.com";
        String payIp = "127.0.0.1";

        map.put("merchno", merchno);
        map.put("paytype", paytype);
        map.put("traceno", traceno);
        map.put("total_fee", total_fee);
        map.put("notifyurl", notifyurl);
        map.put("payIp", payIp);

        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + key;
        String sign = SignatureUtils.md5(SignatureUtils.md5(signStr)).toUpperCase();
        map.put("sign", sign);


        String payGateway = "https://zpay.yaoguaiymtdptzf444.com/pay";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}