package com.jeequan.jeepay.agent.ctrl.order;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.agent.ctrl.CommonCtrl;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.PayPassageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/passagePayOrder")
public class PayPassagePayOrderController extends CommonCtrl {
    @Autowired
    private PayOrderService payOrderService;

    @Autowired
    private PayPassageService payPassageService;

    /**
     * @Author: ZhuXiao
     * @Description: 订单信息列表
     * @Date: 10:43 2021/5/13
     */
    @GetMapping
    public ApiRes list() {
        try {
            PayOrder payOrder = getObject(PayOrder.class);

            JSONObject paramJSON = getReqParamJSON();
            LambdaQueryWrapper<PayOrder> wrapper = PayOrder.gw();
            Map<Long, PayPassage> payPassageMap = payPassageService.getPayPassageMap();

            wrapper.eq(PayOrder::getAgentNoPassage, getCurrentAgentNo());

            wrapper.select(PayOrder::getPayOrderId, PayOrder::getAmount, PayOrder::getState, PayOrder::getNotifyState, PayOrder::getProductId, PayOrder::getProductName, PayOrder::getCreatedAt, PayOrder::getUpdatedAt, PayOrder::getSuccessTime, PayOrder::getClientIp, PayOrder::getNotifyUrl, PayOrder::getAgentPassageRate, PayOrder::getAgentPassageFee, PayOrder::getPassageId, PayOrder::getPassageOrderNo);
            IPage<PayOrder> pages = payOrderService.listByPage(getIPage(), payOrder, paramJSON, wrapper);
            List<PayOrder> list = pages.getRecords();
            for (int i = 0; i < list.size(); i++) {
                list.get(i).addExt("payPassageName", payPassageMap.get(list.get(i).getPassageId()).getPayPassageName());
            }
            return ApiRes.page(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }

    }

    /**
     * @Author: ZhuXiao
     * @Description: 支付订单信息
     * @Date: 10:43 2021/5/13
     */
    @GetMapping("/{payOrderId}")
    public ApiRes detail(@PathVariable("payOrderId") String payOrderId) {
        PayOrder payOrder = payOrderService.getOne(PayOrder.gw().select(PayOrder::getPayOrderId, PayOrder::getMchNo, PayOrder::getMchName, PayOrder::getMchOrderNo, PayOrder::getAmount, PayOrder::getState, PayOrder::getNotifyState, PayOrder::getProductId, PayOrder::getProductName, PayOrder::getCreatedAt, PayOrder::getUpdatedAt, PayOrder::getSuccessTime, PayOrder::getClientIp, PayOrder::getNotifyUrl, PayOrder::getAgentPassageRate, PayOrder::getAgentPassageFee, PayOrder::getAgentNo, PayOrder::getSuccessTime, PayOrder::getExpiredTime, PayOrder::getAgentNoPassage).eq(PayOrder::getPayOrderId, payOrderId.trim()));
        if (payOrder == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        if (!payOrder.getAgentNoPassage().equals(getCurrentAgentNo())) {
            return ApiRes.fail(ApiCodeEnum.SYS_PERMISSION_ERROR);
        }
        return ApiRes.ok(payOrder);
    }
}