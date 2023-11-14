package com.jeequan.jeepay.pay.channel.xuanjie;

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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 炫捷支付
 */
@Service
@Slf4j
public class XuanjiePaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[炫捷支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.XUANJIE;
    }

  

    @Override
    public AbstractRS pay(UnifiedOrderRQ bizRQ, PayOrder payOrder, PayConfigContext payConfigContext) {
        log.info("[{}]开始下单:{}", LOG_TAG, payOrder.getPayOrderId());
        UnifiedOrderRS res = ApiResBuilder.buildSuccess(UnifiedOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);
        try {
            PayPassage payPassage = payConfigContext.getPayPassage();
            //支付参数转换
            NormalMchParams normalMchParams = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), NormalMchParams.class);

            Map<String, Object> map = new HashMap<>();
            String key = normalMchParams.getSecret();

            String merchantId = normalMchParams.getMchNo();
            String orderNo = payOrder.getPayOrderId();
            BigDecimal money = new BigDecimal(AmountUtil.convertCent2Dollar(payOrder.getAmount()));
            String payType = normalMchParams.getPayType();
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            long ts = System.currentTimeMillis();

            map.put("merchantId", merchantId);
            map.put("orderNo", orderNo);
            map.put("money", money);
            map.put("payType", payType);
            map.put("notifyUrl", notifyUrl);
            map.put("ts", ts);

            String signContent = SignatureUtils.getSignContent(map);
            String sign = SignatureUtils.md5(signContent + key).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            String contentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{""});
            log.info("[{}]请求参数:{}", LOG_TAG, contentStr);

            //请求支付页面
            String payUrl = payGateway+"?"+contentStr;

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
        String key = "OzvxrQb8713852EAErEUWlvhx062894006";

        String merchantId = "10";
        String orderNo = RandomStringUtils.random(15, true, true);
        BigDecimal money = new BigDecimal(10000L / 100f);
        String payType = "alipay";
        String notifyUrl = "https://www.test.com";
        long ts = System.currentTimeMillis();

        map.put("merchantId", merchantId);
        map.put("orderNo", orderNo);
        map.put("money", money);
        map.put("payType", payType);
        map.put("notifyUrl", notifyUrl);
        map.put("ts", ts);

        String signContent = SignatureUtils.getSignContent(map);
        String sign = SignatureUtils.md5(signContent + key).toLowerCase();
        map.put("sign", sign);

        String contentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{""});
        log.info("[{}]请求参数:{}", LOG_TAG, contentStr);

        String payGateway = "http://101.132.151.57:8302/sp_h5/paySucc.html";
        log.info("[{}]请求:{}", LOG_TAG, payGateway+"?"+contentStr);
    }
}