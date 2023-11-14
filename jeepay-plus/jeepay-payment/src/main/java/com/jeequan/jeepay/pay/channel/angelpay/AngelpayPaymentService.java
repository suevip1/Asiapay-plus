package com.jeequan.jeepay.pay.channel.angelpay;

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

/**
 * 天使支付
 */
@Service
@Slf4j
public class AngelpayPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[天使支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ANGELPAY;
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
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String mch_no = normalMchParams.getMchNo();
            String order_no = payOrder.getPayOrderId();
            String date = dateFormat.format(new Date());
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String notifyurl = getNotifyUrl(payOrder.getPayOrderId());
            String callbackurl = notifyurl;
            String detail = "detail";
            String email = "test@gmail.com";
            String phone = "13122336688";
            String name = "zhang san";
            String ccy = normalMchParams.getPayType();

            map.put("mch_no", mch_no);
            map.put("order_no", order_no);
            map.put("date", date);
            map.put("amount", amount);
            map.put("notifyurl", notifyurl);
            map.put("callbackurl", callbackurl);
            map.put("detail", detail);
            map.put("email", email);
            map.put("phone", phone);
            map.put("name", name);
            map.put("ccy", ccy);

            String sign = JeepayKit.getSign(map, key).toLowerCase();
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
            if (result.getString("code").equals("0")) {
                JSONObject data = result.getJSONObject("data");

                String payUrl = data.getString("pay_url");
                String passageOrderId = data.getString("pt_order_no");

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
        String key = "234c5f683ea942e82acfa3f2de5e9f92";

        String mch_no = "100004";
        String order_no = RandomStringUtils.random(15, true, true);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = dateFormat.format(new Date());
        String amount = AmountUtil.convertCent2Dollar(10000L);
        String notifyurl = "http://www.test.com";
        String callbackurl = notifyurl;
        String detail = "detail";
        String email = "test@gmail.com";
        String phone = "13122336688";
        String name = "zhang san";
        String ccy = "USD";

        map.put("mch_no", mch_no);
        map.put("order_no", order_no);
        map.put("date", date);
        map.put("amount", amount);
        map.put("notifyurl", notifyurl);
        map.put("callbackurl", callbackurl);
        map.put("detail", detail);
        map.put("email", email);
        map.put("phone", phone);
        map.put("name", name);
        map.put("ccy", ccy);

        String sign = JeepayKit.getSign(map, key).toLowerCase();
        map.put("sign", sign);

        String payGateway = "http://www.angelpay.vip/api/create_order";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
