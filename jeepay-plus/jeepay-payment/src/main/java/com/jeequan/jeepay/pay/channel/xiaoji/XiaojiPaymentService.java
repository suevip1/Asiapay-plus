package com.jeequan.jeepay.pay.channel.xiaoji;

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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * xxpay支付
 */
@Service
@Slf4j
public class XiaojiPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[小鸡支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.XIAOJI;
    }

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return "";
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

            String mch_id = normalMchParams.getMchNo();
            String out_trade_no = payOrder.getPayOrderId();
            String attach = "goods";

            String type = normalMchParams.getPayType();
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String timestamp = System.currentTimeMillis() + "";
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String return_url = notify_url;

            String client_ip = payOrder.getClientIp();

            map.put("mch_id", mch_id);
            map.put("out_trade_no", out_trade_no);
            map.put("attach", attach);

            map.put("amount", amount);
            map.put("type", type);
            map.put("timestamp", timestamp);

            map.put("notify_url", notify_url);
            map.put("return_url", return_url);
            if (StringUtils.isNotEmpty(client_ip)) {
                map.put("client_ip", client_ip);
            }

            String signContent = out_trade_no + attach + type + amount + timestamp + mch_id + key;
            String sign = SignatureUtils.md5(signContent).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {
                JSONObject data = result.getJSONObject("data");

                String payUrl = data.getString("payUrl");
                String passageOrderId = data.getString("transactionId");

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
//        String key = "pbJAzwkWGtcMaMfZCq5IsRwGa62NLFE7";
//
//        String mch_id = "1691049614448652288";
//        String out_trade_no = RandomStringUtils.random(15, true, true);
//        String attach = "goods";
//
//        String type = "14";
//        String amount = AmountUtil.convertCent2Dollar(100L);
//        String timestamp = System.currentTimeMillis() + "";
//        String notify_url = "https://www.test.com";
//        String return_url = notify_url;
//
//        map.put("mch_id", mch_id);
//        map.put("out_trade_no", out_trade_no);
//        map.put("attach", attach);
//
//        map.put("amount", amount);
//        map.put("type", type);
//        map.put("timestamp", timestamp);
//
//        map.put("notify_url", notify_url);
//        map.put("return_url", return_url);
//
//
//        String signContent = out_trade_no + attach + type + amount + timestamp + mch_id + key;
//        String sign = SignatureUtils.md5(signContent).toLowerCase();
//        map.put("sign", sign);
        map.put("out_trade_no", "N4FBSBALBRJn10i");
        String payGateway = "http://pay.jingyihai.top/v1/common/checkOrder";
        log.info("[{}]请求:{}", LOG_TAG, map);

        raw = HttpUtil.get(payGateway, map);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}