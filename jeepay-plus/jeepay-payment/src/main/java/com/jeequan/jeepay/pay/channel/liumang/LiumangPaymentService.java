package com.jeequan.jeepay.pay.channel.liumang;

import cn.hutool.http.HttpUtil;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class LiumangPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[流氓支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.LIUMANG;
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

            String pid = normalMchParams.getMchNo();
            String type = normalMchParams.getPayType();
            String out_trade_no = payOrder.getPayOrderId();
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String return_url = notify_url;
            String name = "下单";
            String money = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String sign_type = "MD5";

            map.put("pid", pid);
            map.put("type", type);
            map.put("out_trade_no", out_trade_no);
            map.put("notify_url", notify_url);
            map.put("return_url", return_url);
            map.put("name", name);
            map.put("money", money);

            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + key;
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            map.put("sign", sign);
            map.put("sign_type", sign_type);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            // 处理响应
            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);

            // 提取请求支付网址
            String payUrl = extractURL(raw);
            log.info("[{}]提取请求支付网址:{}", LOG_TAG, payUrl);

            res.setPayDataType(CS.PAY_DATA_TYPE.PAY_URL);
            res.setPayData(payUrl);

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
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
        String key = "wVCyKI0zBzDWGNoaQBMX0gultaQ35Gwg";

        String pid = "18802787";
        String type = "wxpay";
        String out_trade_no = RandomStringUtils.random(15, true, true);
        String notify_url = "https://www.test.com";
        String return_url = notify_url;
        String name = "下单";
        String money = AmountUtil.convertCent2Dollar(10000L);
        String sign_type = "MD5";

        map.put("pid", pid);
        map.put("type", type);
        map.put("out_trade_no", out_trade_no);
        map.put("notify_url", notify_url);
        map.put("return_url", return_url);
        map.put("name", name);
        map.put("money", money);

        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + key;
        String sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("sign", sign);
        map.put("sign_type", sign_type);

        String payGateway = "http://zfwg.sfbtzfj.cn/submit.php";

        raw = HttpUtil.post(payGateway, map, 10000);

        // 提取网址
        String url = extractURL(raw);
        log.info("[{}]请求响应:{}", LOG_TAG, url);
    }

    public static String extractURL(String response) {
        // 使用正则表达式匹配网址
        Pattern pattern = Pattern.compile("https?://[\\w.-]+(/[\\w\\s.-]*)*");
        Matcher matcher = pattern.matcher(response);

        // 查找并返回匹配到的网址
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }
}