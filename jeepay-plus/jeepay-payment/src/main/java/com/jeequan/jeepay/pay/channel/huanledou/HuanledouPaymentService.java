package com.jeequan.jeepay.pay.channel.huanledou;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
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
public class HuanledouPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[欢乐豆支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.HUANLEDOU;
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
            HuanledouParamsModel huanledouParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), HuanledouParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = huanledouParamsModel.getSecret();

            String mchntCd = huanledouParamsModel.getMchNo();
            String appid = huanledouParamsModel.getAppId();
            String mchntOrderNo = payOrder.getPayOrderId();
            String orderTp = huanledouParamsModel.getPayType();
            String goodsName = "下单";
            Long amount = payOrder.getAmount();
            String custId = RandomStringUtils.random(8, true, false);
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());

            map.put("mchntCd", mchntCd);
            map.put("appid", appid);
            map.put("mchntOrderNo", mchntOrderNo);
            map.put("orderTp", orderTp);
            map.put("goodsName", goodsName);
            map.put("amount", amount);
            map.put("custId", custId);
            map.put("notifyUrl", notifyUrl);

            String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
            String sign = SignatureUtils.md5(signContent + key).toUpperCase();
            map.put("sign", sign);

            String payGateway = huanledouParamsModel.getPayGateway();

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            raw = response.body();
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {

                String payUrl = result.getString("payUrl");
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
        String key = "v2oc9qn8t7yncjikpr6wlmdtqvpgi7ex";

        String mchntCd = "676977";
        String appid = "A2312356";
        String mchntOrderNo = RandomStringUtils.random(15, true, true);
        String orderTp = "19";
        String goodsName = "下单";
        String amount = "10000";
        String custId = RandomStringUtils.random(8, true, false);
        String notifyUrl = "http://www.test.com";


        map.put("mchntCd", mchntCd);
        map.put("appid", appid);
        map.put("mchntOrderNo", mchntOrderNo);
        map.put("orderTp", orderTp);
        map.put("goodsName", goodsName);
        map.put("amount", amount);
        map.put("custId", custId);
        map.put("notifyUrl", notifyUrl);

        String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
        String sign = SignatureUtils.md5(signContent + key).toUpperCase();
        map.put("sign", sign);

        String payGateway = "http://123.60.159.24:18001/api/v1.0/order/pay";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
