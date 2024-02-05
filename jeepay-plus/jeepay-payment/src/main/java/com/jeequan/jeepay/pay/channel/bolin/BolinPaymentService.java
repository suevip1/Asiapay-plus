package com.jeequan.jeepay.pay.channel.bolin;

import cn.hutool.core.date.DateUtil;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 铂霖支付
 */
@Service
@Slf4j
public class BolinPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[铂霖支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.BOLIN;
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
            BolinParamsModel bolinParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), BolinParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = bolinParamsModel.getSecret();
            String requestPrivateKey= bolinParamsModel.getRequestPrivateKey();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String pay_memberid = bolinParamsModel.getMchNo();
            String pay_orderid = payOrder.getPayOrderId();
            String pay_applydate = dateFormat.format(new Date());
            String pay_bankcode = bolinParamsModel.getPayType();
            String pay_notifyurl = getNotifyUrl(payOrder.getPayOrderId());
            String pay_callbackurl = pay_notifyurl;
            String pay_amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());

            map.put("pay_memberid", pay_memberid);
            map.put("pay_orderid", pay_orderid);
            map.put("pay_applydate", pay_applydate);
            map.put("pay_bankcode", pay_bankcode);
            map.put("pay_notifyurl", pay_notifyurl);
            map.put("pay_callbackurl", pay_callbackurl);
            map.put("pay_amount", pay_amount);

            String signValue = JeepayKit.getSign(map, key).toUpperCase();
            String pay_md5sign = SignatureUtils.buildSHA256WithRSASignByPrivateKey(signValue, requestPrivateKey);
            map.put("pay_md5sign", pay_md5sign);
            map.put("type", "json");

            String payGateway = bolinParamsModel.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);

            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("status").equals("200")) {

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


    public static void main(String[] args) throws UnsupportedEncodingException {
        String raw = "";

        Map<String, Object> map = new HashMap<>();
        String key = "kDTQJ7D4nUJwhbcxnlTek17rQlVdDaHU";
        String rsa = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCZryLUDqUAxs4TxoR7C4gzBl3H3xF/zg6TSB8wg62wEir9PFI5WivSYPKPCGL5e7zUnpW3/JFtqkSvtp9wZ1RRNTe1xSzCbp2Vopa8qwTvZaCb7lC0Cv4MDMJ4j7G9O+dWFnHO/aQ/ggiUjguEoqqOmSuF/FTb4Fi1uutPMONE821VK1ACy/nm69XGCQYil/YOsSsuB2qShXLgiEjYhzlwEim/opGXiG4eUSMlszNyIdO9KiPGES9ggi3/uwEiPQ3n117gVA8GeKvAwoFswGQ4aCz7lxQyO0WhbPIqQg9O7V76PyRpYE/SMz8Pw9Q7uyYjVt+PxIbipdheT9UFGN7/AgMBAAECggEAN6uVdjOH2ewqIke7HcdjovgfaJOJhz5hUUDC65QHisxJ/kmnj1b2oNK1itKC59TG5k479dXr6EnRb8U7pEAJ7xDUCWhPem8PbnDjIZzhwMaTk2iUm2OP+1Vz7LBAtkfI2QpwV4bRxJ+Z9BYwXe9+0QDzHyn0o2F1QP9g1eLjsMna+pkVikc3oqZkYVEpIGEIkH50bqPwGoNPmOxIlLWbbWU1o5coKzMLp0w5L5ua5dQVYZqeuFHvWkhaq0QYhKGoQxgVQ4y9viNPfe4xsd6PJT5oWTvo6TxbxsNLosPZ/KtDsmssQT5ZrHsP4CKLLS4zscjcfBPfWoPQADhYB8g3oQKBgQDvvI0kuinlkjf3Xuz+oMS+dRZ0y/2hy+YM2y+n5qhUtupyYzY8owKBIB1WwyIY1hDkEN1uzxLSZKKnLwO6YTJisSojgY9H/Nq2XjpSkV1NrIkYdSRHuppl65b+R+PfVLf06NeV9qelWPB16tfZvFISVEOWBuaiIESIJVLaKOeliwKBgQCkHCHxGlecDaOeIuXpumS9r98+BwWHDnJuIKa6YhTlS3VpMZUjoK3hUlCbTOewnTLZGIXQEHqMpQEpLPMbAiaGETCHHgvOH6WpdKxVVO+QfPkzwg6IQppkkoTgy/ynR7//8Liek2OH+qehCc69uZVSyNupwaCQMOX3XccaWLei3QKBgQCqjVlcZ5Sm3f8y3TV9cKKHXIU9UmJ+WLCxNL4SlOXsXrUr66kKOm3trwswTmJTiPc7SUQ/MNITG8UbazcbwUo3LBLub52feSOf2ilj1Easm7Js0+mbngV5vMEO5dYXbA3zDjNRXMinuT5YpVoqbO92Wrw6X250qMAYm3T1/2PNQQKBgQCL4L69dqp+BNIVE8HL+merlqA9ilaXyySV4Za6/qa42hx1USHBgE804qh59o705shUaqaDtcf+4RWgNwPvXWHgm/407NM7GZZmXWAWS5L2IM934kvosHxQhOJliPkMzAdG67Mw1ofbezYt+OISrIAuU62Hf9aPkF4y/eea+UtssQKBgH4zfHcgTRDVLumxiQOFSpY4w4Qp+NRlweKC/VNz7R+YDy54vhCcgzFK4Q9A1M7fzRPSr4O0odgMB4c2txnGsL665HLHC3Fg0wYqw/SaWhFKJmvxP6RSI7AjKqoySrQJdjywKvJkI5rLDb24Qvo+uvTVkExHanEZWry9D0GhT3Gs";

        String pay_memberid = "240126276";
        String pay_orderid = RandomStringUtils.random(15, true, true);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String today = dateFormat.format(new Date());
        String pay_applydate = today;

        String pay_bankcode = "201";
        String pay_notifyurl = "https://www.test.com";
        String pay_callbackurl = pay_notifyurl;
        String pay_amount = AmountUtil.convertCent2Dollar(10000L);

        map.put("pay_memberid", pay_memberid);
        map.put("pay_orderid", pay_orderid);
        map.put("pay_applydate", pay_applydate);
        map.put("pay_bankcode", pay_bankcode);
        map.put("pay_notifyurl", pay_notifyurl);
        map.put("pay_callbackurl", pay_callbackurl);
        map.put("pay_amount", pay_amount);

        String signValue = JeepayKit.getSign(map, key).toUpperCase();
        String pay_md5sign = SignatureUtils.buildSHA256WithRSASignByPrivateKey(signValue, rsa);
        map.put("pay_md5sign", pay_md5sign);
        map.put("type", "json");

        String payGateway = "http://api.bolinpay.cc/pay";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
