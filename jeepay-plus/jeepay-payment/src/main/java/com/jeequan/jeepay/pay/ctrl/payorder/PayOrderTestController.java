package com.jeequan.jeepay.pay.ctrl.payorder;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.pay.channel.IPaymentService;
import com.jeequan.jeepay.pay.ctrl.ApiController;
import com.jeequan.jeepay.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 支付通道管理类
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-06-16 09:15
 */
@Slf4j
@RestController
@RequestMapping("/api/payOrderTest")
public class PayOrderTestController extends AbstractPayOrderController {

    @Autowired
    private PayPassageService payPassageService;

    @Autowired
    private ProductService productService;

    @Autowired
    private PassageTransactionHistoryService passageTransactionHistoryService;

    @Autowired
    private PayOrderService payOrderService;

    @Autowired
    private MchPayPassageService mchPayPassageService;

    /**
     * 测试下单
     *
     * @return
     */
    @RequestMapping(value = "/pay", method = RequestMethod.POST)
    public ApiRes testPayOrder() {
        JSONObject paramJSON = getReqParamJSON();
        log.info(paramJSON.toJSONString());
        JSONObject ifParams = paramJSON.getJSONObject("ifParams");
        Long amount = paramJSON.getLongValue("amount");
        //获取支付接口
        IPaymentService paymentService = getService(ifParams.getString("ifCode"));
        //todo 测试下单
        return ApiRes.ok();
    }
}
