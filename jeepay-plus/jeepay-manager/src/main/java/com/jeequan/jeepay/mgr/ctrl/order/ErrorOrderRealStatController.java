package com.jeequan.jeepay.mgr.ctrl.order;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.entity.ErrorOrder;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.ErrorOrderService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 实时统计
 */
@Slf4j
@RestController
@RequestMapping("/api/errorRealTimeStatOrder")
public class ErrorOrderRealStatController extends CommonCtrl {
    @Autowired
    private ErrorOrderService errorOrderService;


    @RequestMapping(value = "", method = RequestMethod.POST)
    public ApiRes countReal() {
        ErrorOrder errorOrder = getObject(ErrorOrder.class);
        JSONObject paramJSON = getReqParamJSON();
        LambdaQueryWrapper<ErrorOrder> wrapper = ErrorOrder.gw();

        //金额
        wrapper.select(ErrorOrder::getAmount);
        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                wrapper.ge(ErrorOrder::getCreatedAt, paramJSON.getString("createdStart"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                wrapper.le(ErrorOrder::getCreatedAt, paramJSON.getString("createdEnd"));
            }
        }

        //商户号
        if (StringUtils.isNotEmpty(errorOrder.getMchNo())) {
            wrapper.like(ErrorOrder::getMchNo, errorOrder.getMchNo().trim());
        }
        //商户名
        if (StringUtils.isNotEmpty(errorOrder.getMchName())) {
            wrapper.like(ErrorOrder::getMchName, errorOrder.getMchName().trim());
        }
        //商户订单号
        if (StringUtils.isNotEmpty(errorOrder.getMchOrderNo())) {
            wrapper.eq(ErrorOrder::getMchOrderNo, errorOrder.getMchOrderNo().trim());
        }

        wrapper.orderByDesc(ErrorOrder::getCreatedAt);

        List<ErrorOrder> records = errorOrderService.list(wrapper);
        return ApiRes.ok(genReturnJson(records));
    }

    private JSONObject genReturnJson(List<ErrorOrder> errorOrders) {

        int totalCount = errorOrders.size();//订单总数
        Long totalAmount = 0L;//订单总金额

        for (int i = 0; i < errorOrders.size(); i++) {
            ErrorOrder item = errorOrders.get(i);
            totalAmount += item.getAmount();
        }
        JSONObject result = new JSONObject();
        result.put("totalCount", totalCount);
        result.put("totalAmount", totalAmount);
        return result;
    }
}
