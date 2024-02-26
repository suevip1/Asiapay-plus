package com.jeequan.jeepay.mgr.ctrl.order;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.PayPassageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 支付订单类
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021-06-07 07:15
 */
@RestController
@RequestMapping("/api/payOrderForceList")
public class PayOrderForceController extends CommonCtrl {
    @Autowired
    private PayOrderService payOrderService;

    @Autowired
    private PayPassageService payPassageService;

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:15
     * @describe: 订单信息列表
     */
    @PreAuthorize("hasAuthority('ENT_ORDER_LIST')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        try {
            PayOrder payOrder = getObject(PayOrder.class);
            JSONObject paramJSON = getReqParamJSON();
            LambdaQueryWrapper<PayOrder> wrapper = PayOrder.gw();
            IPage<PayOrder> pages = payOrderService.listByPageUpdatedAt(getIPage(), payOrder, paramJSON, wrapper);
            List<PayOrder> records = pages.getRecords();
            Map<Long, PayPassage> payPassageMap = payPassageService.getPayPassageMap();
            for (int i = 0; i < records.size(); i++) {
                records.get(i).addExt("passageName", payPassageMap.get(records.get(i).getPassageId()).getPayPassageName());
            }
            pages.setRecords(records);
            return ApiRes.page(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }
}
