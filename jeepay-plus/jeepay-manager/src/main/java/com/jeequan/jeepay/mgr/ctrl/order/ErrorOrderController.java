package com.jeequan.jeepay.mgr.ctrl.order;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.ErrorOrder;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.ErrorOrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 异常订单类
 */
@RestController
@RequestMapping("/api/errorOrder")
public class ErrorOrderController extends CommonCtrl {
    @Autowired
    private ErrorOrderService errorOrderService;


    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:15
     * @describe: 订单信息列表
     */
    @PreAuthorize("hasAuthority('ENT_ORDER_LIST')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        try {
            ErrorOrder errorOrder = getObject(ErrorOrder.class);
            JSONObject paramJSON = getReqParamJSON();
            LambdaQueryWrapper<ErrorOrder> wrapper = ErrorOrder.gw();
            //商户号、名、商户订单号、金额、创建时间
            wrapper.select(ErrorOrder::getErrorOrderId, ErrorOrder::getMchNo, ErrorOrder::getMchNo, ErrorOrder::getMchName, ErrorOrder::getAmount, ErrorOrder::getCreatedAt,ErrorOrder::getMchOrderNo);
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

            IPage<ErrorOrder> pages = errorOrderService.page(getIPage(), wrapper);
            List<ErrorOrder> records = pages.getRecords();
            pages.setRecords(records);
            return ApiRes.page(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:15
     * @describe: 支付订单信息
     */
    @PreAuthorize("hasAuthority('ENT_PAY_ORDER_VIEW')")
    @RequestMapping(value = "/{errorOrderId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("errorOrderId") Long errorOrderId) {
        ErrorOrder errorOrder = errorOrderService.getById(errorOrderId);
        if (errorOrder == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        return ApiRes.ok(errorOrder);
    }
}
