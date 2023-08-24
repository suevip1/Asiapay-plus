package com.jeequan.jeepay.agent.ctrl.order;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.agent.ctrl.CommonCtrl;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.service.impl.PayOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付订单管理类
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@RestController
@RequestMapping("/api/payOrder")
public class MchPayOrderController extends CommonCtrl {
    //todo PayOrderController
    @Autowired
    private PayOrderService payOrderService;

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
            wrapper.eq(PayOrder::getAgentNo, getCurrentAgentNo());
            wrapper.select(PayOrder::getPayOrderId, PayOrder::getMchNo, PayOrder::getMchName, PayOrder::getAmount, PayOrder::getState, PayOrder::getNotifyState, PayOrder::getProductId, PayOrder::getProductName, PayOrder::getCreatedAt, PayOrder::getUpdatedAt, PayOrder::getSuccessTime, PayOrder::getClientIp, PayOrder::getNotifyUrl, PayOrder::getForceChangeState, PayOrder::getForceChangeBeforeState, PayOrder::getForceChangeLoginName, PayOrder::getMchFeeAmount, PayOrder::getMchFeeRate, PayOrder::getMchOrderNo);
            IPage<PayOrder> pages = payOrderService.listByPage(getIPage(), payOrder, paramJSON, wrapper);
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
        PayOrder payOrder = payOrderService.getOne(PayOrder.gw().select(PayOrder::getPayOrderId, PayOrder::getMchNo, PayOrder::getMchName, PayOrder::getMchOrderNo, PayOrder::getAmount, PayOrder::getState, PayOrder::getNotifyState, PayOrder::getProductId, PayOrder::getProductName, PayOrder::getCreatedAt, PayOrder::getUpdatedAt, PayOrder::getSuccessTime, PayOrder::getClientIp, PayOrder::getNotifyUrl, PayOrder::getForceChangeState, PayOrder::getForceChangeBeforeState, PayOrder::getForceChangeLoginName, PayOrder::getMchFeeAmount, PayOrder::getMchFeeRate, PayOrder::getAgentNo, PayOrder::getAgentFeeAmount, PayOrder::getAgentRate, PayOrder::getSuccessTime, PayOrder::getExpiredTime).eq(PayOrder::getPayOrderId, payOrderId));

        if (payOrder == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        if (!payOrder.getAgentNo().equals(getCurrentAgentNo())) {
            return ApiRes.fail(ApiCodeEnum.SYS_PERMISSION_ERROR);
        }
        return ApiRes.ok(payOrder);
    }
}
