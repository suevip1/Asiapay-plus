package com.jeequan.jeepay.pay.channel.xique;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
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
public class XiquePaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[喜鹊支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.XIQUE;
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
            XiqueParamsModel xiqueParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), XiqueParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = xiqueParamsModel.getSecret();
            String privateKey = xiqueParamsModel.getRequestPrivateKey();

            String merId = xiqueParamsModel.getMchNo();
            String orderId = payOrder.getPayOrderId();
            String orderAmt = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String channel = xiqueParamsModel.getPayType();
            String desc = "下单";
            String ip = payOrder.getClientIp();
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String returnUrl = notifyUrl;
            String nonceStr = RandomStringUtils.random(8, true, false);

            map.put("merId", merId);
            map.put("orderId", orderId);
            map.put("orderAmt", orderAmt);
            map.put("channel", channel);
            map.put("desc", desc);
            map.put("ip", ip);
            map.put("notifyUrl", notifyUrl);
            map.put("returnUrl", returnUrl);
            map.put("nonceStr", nonceStr);

            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + key;
            String pay_md5sign = SignatureUtils.md5(signStr).toUpperCase();
            String sign = SignatureUtils.buildSHA256WithRSASignByPrivateKey(pay_md5sign, privateKey);
            map.put("sign", sign);

            String payGateway = xiqueParamsModel.getPayGateway();

            raw = HttpUtil.post(payGateway, map,10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getInteger("code") == 1) {
                JSONObject data = result.getJSONObject("data");

                String payUrl = data.getString("payurl");
                String passageOrderId = data.getString("sysorderno");

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
        String key = "zSEumObtyNLgQBUvlfiPoDMVYCWTKRIG";
        String rsa = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC2nK8eawtLg+YZAhk051Y9fvLGI/ZykljOlzHL1yzRCiZKYYdQZvOKwJdsk/WkV+pIyxYRRS0dlHL3O5CaTO6+x07QmbFTvBWFw9Mbyd6vtFDkFLQ5T8UWdh+Lb8eQu5q0R3kokMiSakyJV4VfjJpcy4FihdCxVptHOSQJlgP00fYQZX/MPXOCxtFbDPnalwjP2pV+x2MA/WtZr6ARYm/rhSImFMW7YyK9TDlIuF7nhVBhMALLEMDmjhhgdggWOnXH7O6DOv3QPC4vzc9iKo5/55s2o+mUFAXuJFBDqs1W0XzvL7+J89nSEtyIXKi3acXLdERCcvl4KoWWHZfZhJP/AgMBAAECggEAFeo91d+wzlI49f1bew7cOasXhZHhTKSlhb19W2IqXbfjGoAT1iFUzu0H/x7Olvd6BVxh/JaZsHgVR/hdBTeAW2Ld4YjgUBU9W/1BQ495awqvvn+BlXMuSuDILlquz37ENShysNyXRYL+Rmk7d1kPWV2O3H0Eq5OiNOz1lzTqqqGoCWSrFFovKMgJTfJzWJxY1mWJ7/8wJ0idSE/Zb3gaxE2nyaAETJoEI7IqwMFnjT54lTokUwFgDmnWrtVrCLvxdn5N45MGst216YYyE+Gl7zZd1LdxB2PaUE0UqRUOVfXIMXELlVMvFX79//u9GKycKT+F9MSl2KEEeyubIh+9qQKBgQDZvQw0NPfoiVQWX9mxuqdyfwPZokQtp+1ER5cXM90qIRKhKXFqMrErm/9RkLZ9F19A+Ngjbw3VNh9evHNRAVDK3cwGm4rMuzJ2qZukA4wvsCjjh3IEe//gk0s7m+xddDS3uYrm88f2Xhg40U4z8vZmcMy/mKz8PSwSYrbEJ98XtQKBgQDWs3n1mFZSVv0HiaTWKvKRLmwGxblVOxfAL1IfXN5JhUyEEeYvBNxBWg5tsbALEH9yNFamdWCHxRJ6Nnqhh3RSNB7gGN96F/yS/wxDMXL8+RKgzKz40wupAvwSiMQm8RfNNZqG8OTR87MtfzXe5akViXhyAMIH3XOoBarHqNRlYwKBgQC29N74TzmwLxR06H1nlct1rbxydqKLKnt30B/o4y+HYtrgiI5ACfBNEHts8ggk/CqR0Ybr3zjkWuHpHLhY7J4SmGQDeVcbyIASrVmWa1S0DXWOGhV4JylObvXcVJw7upVBe6p3fYgcP9xigZ0QkarxUP76TzOrj0sk98VS9Z3DsQKBgGsYveZfZffOwornAx198N+wq1w5dvhn8LICKNHSLmO4JXHfx9V+dCv5MydOskTipuAd8Xc5J7yx9kSWNleqzMBQlXItlhQ+MNRWexRtbzF3QO6LCmFYTQGypnDMXiQasUfAzGMxeMKt8JvqT8HvB4fhuf4MGL15nr6ajRTpR4GHAoGANEEsfkotCNhQg/kKUE9WpVNvDiYLSH38yPCqcT6Sp5mkQ7StuWl39hihhEuz0kIBY2JjjFK0tY6IOppDvJA8RCq+NqE8I06cnDTtMBxwA5G6WxES6ZN6AsRytD4sIy6J0vpxk2nGiI+mBjp+jwQsTW0zyKKAaMzBaMEXJ+mq6HE=";

        String merId = "20231115";
        String orderId = RandomStringUtils.random(15, true, true);
        String orderAmt = AmountUtil.convertCent2Dollar(300000L);
        String channel = "zfbkk";
        String desc = "desc";
        String ip = "127.0.0.1";
        String notifyUrl = "http://www.test.com";
        String returnUrl = notifyUrl;
        String nonceStr = RandomStringUtils.random(8, true, false);

        map.put("merId", merId);
        map.put("orderId", orderId);
        map.put("orderAmt", orderAmt);
        map.put("channel", channel);
        map.put("desc", desc);
        map.put("ip", ip);
        map.put("notifyUrl", notifyUrl);
        map.put("returnUrl", returnUrl);
        map.put("nonceStr", nonceStr);


        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + key;
        String pay_md5sign = SignatureUtils.md5(signStr).toUpperCase();
        String sign = SignatureUtils.buildSHA256WithRSASignByPrivateKey(pay_md5sign, rsa);
        map.put("sign", sign);

        String payGateway = "http://api.zzcc.shop/pay";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
