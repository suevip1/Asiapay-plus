package com.jeequan.jeepay.pay.channel.yipay;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.JeepayKit;
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
public class YipayPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[易支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YIPAY;
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

            String money = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String name = "下单";
            String type = normalMchParams.getPayType();
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String out_trade_no = payOrder.getPayOrderId();
            String pid = normalMchParams.getMchNo();
            String return_url = notify_url;

            map.put("money", money);
            map.put("name", name);
            map.put("type", type);
            map.put("notify_url", notify_url);
            map.put("out_trade_no", out_trade_no);
            map.put("pid", pid);
            map.put("return_url", return_url);

            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + key;
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("1")) {

                String payUrl = result.getString("payurl");
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
        String key = "pbJAzwkWGtcMaMfZCq5IsRwGa62NLFE7";

        String money = AmountUtil.convertCent2Dollar(10000L);
        String name = "下单";
        String type = "wxpay";
        String notify_url = "https://www.test.com";
        String out_trade_no = RandomStringUtils.random(15, true, true);
        String pid = "1691049614448652288";
        String return_url = notify_url;

        map.put("money", money);
        map.put("name", name);
        map.put("type", type);
        map.put("notify_url", notify_url);
        map.put("out_trade_no", out_trade_no);
        map.put("pid", pid);
        map.put("return_url", return_url);

        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + key;
        String sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("sign", sign);

        String payGateway = "https://openapi.qianyiwl.top/v4/api/epayOrder/submit.php";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
