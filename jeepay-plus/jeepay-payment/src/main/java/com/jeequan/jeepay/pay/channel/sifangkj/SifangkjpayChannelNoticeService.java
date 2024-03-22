package com.jeequan.jeepay.pay.channel.sifangkj;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.pay.channel.AbstractChannelNoticeService;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.util.BigDecimalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;


@Slf4j
@Service
public class SifangkjpayChannelNoticeService extends AbstractChannelNoticeService {

    private static final String LOG_TAG = "[四方科技支付]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "SUCCESS";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.SIFANGKJPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum) {
        try {
            JSONObject params = getReqParamJSON();
            return MutablePair.of(urlOrderId, params);
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, PayOrder payOrder, PayPassage payPassage, NoticeTypeEnum noticeTypeEnum) {
        ChannelRetMsg result = ChannelRetMsg.confirmSuccess(null);
        try {
            // 获取请求参数
            JSONObject jsonParams = (JSONObject) params;
            log.info("{} 回调参数, jsonParams：{}", LOG_TAG, jsonParams);

            // 解密回调参数
            SifangkjpayParamsModel sifangkjpayParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), SifangkjpayParamsModel.class);
            if (sifangkjpayParamsModel == null) {
                log.info("{} 获取商户配置失败！ 参数：parameter = {}", LOG_TAG, jsonParams);
            }
            String encryptedMessage = jsonParams.getString("data");
            RSADecryptor decryptor = new RSADecryptor( sifangkjpayParamsModel.getPrivateKey());
            String decryptedMessage = decryptor.decrypt(encryptedMessage); // 设置分段大小
            log.info("解密后的内容: " + decryptedMessage);
            JSONObject decryptResult = JSON.parseObject(decryptedMessage);

            // 校验支付回调
            boolean verifyResult = verifyParams(decryptResult, payOrder, payPassage);
            // 验证参数失败
            if (!verifyResult) {
                //回调参数有问题得
                throw ResponseException.buildText(ON_FAIL);
            }
            log.info("{}验证支付通知数据及签名通过", LOG_TAG);

            ResponseEntity okResponse = textResp(ON_SUCCESS);
            result.setResponseEntity(okResponse);

            //-1用户未提交 0处理中 1成功 2失败
            int status = decryptResult.getInteger("status");

            if (status != 1) {
                log.info("[{}]回调通知订单状态错误:{}", LOG_TAG, status);
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            } else {
                //验签成功后判断上游订单状态
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
            }
            return result;
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText(ON_FAIL);
        }
    }

    /**
     * 校验签名及金额
     *
     * @param jsonParams
     * @param payOrder
     * @param payPassage
     * @return
     */
    public boolean verifyParams(JSONObject jsonParams, PayOrder payOrder, PayPassage payPassage) throws Exception {
        String orderNo = jsonParams.getString("pay_osn");        // 商户订单号
        String txnAmt = jsonParams.getString("in_price");        // 支付金额

        if (StringUtils.isEmpty(orderNo)) {
            log.info("订单ID为空 [orderNo]={}", orderNo);
            return false;
        }
        if (StringUtils.isEmpty(txnAmt)) {
            log.info("金额参数为空 [txnAmt] :{}", txnAmt);
            return false;
        }

        BigDecimal channelNotifyAmount = new BigDecimal(txnAmt);
        BigDecimal orderAmount = BigDecimalUtil.INSTANCE.divide(payOrder.getAmount(), 100f);
        if (orderAmount.compareTo(channelNotifyAmount) == 0) {
            return true;
        } else {
            log.error("{} 校验金额失败！ 回调参数：parameter = {}", LOG_TAG, jsonParams);
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        // 假设这是从PHP获取并处理过的加密消息
        String encryptedMessage = "TsnlPa22knBc/rYpAlfzDOh2BX63pT+iYe4UII5XZzHMYOzVLEcLtwcB/AE+xkE8pOUjt1N6lem8oiJt1VW0K0jgTzL+Z9nefe17j6cG/ZBPrQhf7uuqr36ZTra3DuQScrXc9wqcdfGVYFxD+moRdaj2NfH4YOpVCVZP2mAHcUo=";

        RSADecryptor decryptor = new RSADecryptor( "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJkyFXQ3IPbE8rbY\n" +
                "FvFICbPkBS3LeTR4YeRkn6bd/ANtbXLGqjBtNjnLnyIhXqNOZp/sHiFnFHb1RWB4\n" +
                "HbA7zBC1O0CJCiww/By9xdsEXWwgrE5y5cLy/ildwLT5N7t/3ZD+kEmsIeb1MfX4\n" +
                "HJGxLc6sn+j0trDsljL0+SWtk/FLAgMBAAECgYB+nPM4UtvuuAX18pC7qBNE118k\n" +
                "YzfwktItZrDAxFUmLAm6Q9GjJpAbolwKZFQIWqLc59RbPQuf6cUGHyBCaJmVqiVe\n" +
                "Vxbcj+Lbr+XgfO96H0Zw20ZiiLdtHnJ6E1pjd6bC9Lo8PK4vqbS2AojM+t/bAYdv\n" +
                "drY7jh/PF8VE/vDrgQJBAMgn8GW/EV6rG50cSrjImThY75ouiYr23U+o/kJzNs5b\n" +
                "q3d6rrlxKd+iiKH2cN5EJKwc6wWS5DWrilx+O7j6JAsCQQDD8AT7mdi4gQ3RoGNZ\n" +
                "WWZa5KpvgwrFgC6zWrTgT0OxQX5AQVLeHC4RIUSSLuvi2cGDdg+ROGLbn8O9ocF2\n" +
                "2m/BAkApELirx2XC7Iw/0bwq6U02WFjgY0fo8Pk7DMsNyyGleP1XCB6tAWC791bd\n" +
                "E/jYQVbc0RoPDLIxqL48d9G9A+U9AkAxhNCNkdUu1Bbc+s8A/tIaeJ4GbrTjM0Ea\n" +
                "PPtnkUDHZIj2zkuWLMMLuz+sPkWJYIVCpyzKA9kgLmF25JGTayNBAkBPS7MgFsU9\n" +
                "1S5DzWxPnCdYQKeQJnVtASU6A/RtVPURWtQSClQq+0bbVRnKgrJ1Gu6WEjRg0Av5\n" +
                "6XfD5CrU7CGK");

        // 解密消息
        String decryptedMessage = decryptor.decrypt(encryptedMessage); // 设置分段大小

        System.out.println("解密后的内容: " + decryptedMessage);
    }
}