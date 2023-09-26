package com.jeequan.jeepay.pay.channel.boxin;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * 博鑫支付
 */
@Service
@Slf4j
public class BoxinPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[博鑫支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.BOXIN;
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

            String p1_merchantno = normalMchParams.getMchNo();
            String p2_amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String p3_orderno = payOrder.getPayOrderId();
            String p4_paytype = normalMchParams.getPayType();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String p5_reqtime = dateFormat.format(new Date());

            String p6_goodsname = "goods";

            String p8_returnurl = getNotifyUrl(payOrder.getPayOrderId());
            String p9_callbackurl = p8_returnurl;

            map.put("p1_merchantno", p1_merchantno);
            map.put("p2_amount", p2_amount);
            map.put("p3_orderno", p3_orderno);

            map.put("p4_paytype", p4_paytype);
            map.put("p5_reqtime", p5_reqtime);
            map.put("p6_goodsname", p6_goodsname);

            map.put("p8_returnurl", p8_returnurl);
            map.put("p9_callbackurl", p9_callbackurl);

            String sign = JeepayKit.getSign(map, key).toUpperCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("rspcode").equals("A0")) {

                String payUrl = result.getString("data");
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
        String key = "00a9e24ebcc54eebb12e1639bf20a5c0";

        String p1_merchantno = "MER20230922154048637785";
        String p2_amount = AmountUtil.convertCent2Dollar(10000L);
        String p3_orderno = RandomStringUtils.random(15, true, true);

        String p4_paytype = "AlipayH5_LC_Super";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String p5_reqtime = dateFormat.format(new Date());

        String p6_goodsname = "goods";
        String p8_returnurl = "https://www.test.com";
        String p9_callbackurl = p8_returnurl;


        map.put("p1_merchantno", p1_merchantno);
        map.put("p2_amount", p2_amount);
        map.put("p3_orderno", p3_orderno);

        map.put("p4_paytype", p4_paytype);
        map.put("p5_reqtime", p5_reqtime);
        map.put("p6_goodsname", p6_goodsname);

        map.put("p8_returnurl", p8_returnurl);
        map.put("p9_callbackurl", p9_callbackurl);

        String sign = JeepayKit.getSign(map, key).toUpperCase();
        map.put("sign", sign);
        String payGateway = "http://api.cherrysms.net/pay";
        log.info("[{}]请求Map:{}", LOG_TAG, map);

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}