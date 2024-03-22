package com.jeequan.jeepay.pay.channel.digua;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
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


@Service
@Slf4j
public class DiguaPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[地瓜支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.DIGUA;
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


            String key = normalMchParams.getSecret();

            String appKey = normalMchParams.getMchNo();

            String channelCode = normalMchParams.getPayType();
            String tradeNo = payOrder.getPayOrderId();
            String money = AmountUtil.convertCent2DollarShort(payOrder.getAmount());
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String userIp = payOrder.getClientIp();


            String Nonce = RandomStringUtils.random(32, true, true);
            String CurTime = System.currentTimeMillis() / 1000 + "";
            String CheckSum = SignatureUtils.md5(Nonce + CurTime + key).toLowerCase();

            String payGateway = normalMchParams.getPayGateway();


            HttpRequest request = HttpRequest.post(payGateway)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Nonce", Nonce)
                    .header("AppKey", appKey)
                    .header("CurTime", CurTime)
                    .header("CheckSum", CheckSum)
                    .form("channelCode", channelCode)
                    .form("tradeNo", tradeNo)
                    .form("money", money)
                    .form("userIp", userIp)
                    .form("notifyUrl", notifyUrl);
            // 根据需要添加更多表单数据...

            HttpResponse response = request.timeout(10000).execute();
            // 处理响应
            raw = response.body();

            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {

                JSONObject data = result.getJSONObject("data");

                String payUrl = data.getString("payUrl");

                String passageOrderId = data.getString("orderNo");

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

        String key = "7a8544ccf403e9efa1d136808f66d359";

        String appKey = "j9y4a80UUP";

        String channelCode = "szrmb";
        String tradeNo = RandomStringUtils.random(15, true, true);
        String money = AmountUtil.convertCent2DollarShort(10000L);
        String notifyUrl = "https://www.test.com";
        String userIp = "127.0.0.1";


        String Nonce = RandomStringUtils.random(32, true, true);
        String CurTime = System.currentTimeMillis() / 1000 + "";
        String CheckSum = SignatureUtils.md5(Nonce + CurTime + key).toLowerCase();

        String payGateway = "http://8.217.119.232/ylzfws/openapi/wsl/merchant/po";


        HttpRequest request = HttpRequest.post(payGateway)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Nonce", Nonce)
                .header("AppKey", appKey)
                .header("CurTime", CurTime)
                .header("CheckSum", CheckSum)
                .form("channelCode", channelCode)
                .form("tradeNo", tradeNo)
                .form("money", money)
                .form("userIp", userIp)
                .form("notifyUrl", notifyUrl);
        // 根据需要添加更多表单数据...

        HttpResponse response = request.timeout(10000).execute();
        // 处理响应
        raw = response.body();

        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}