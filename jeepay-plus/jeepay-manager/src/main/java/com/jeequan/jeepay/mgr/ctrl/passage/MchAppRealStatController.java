package com.jeequan.jeepay.mgr.ctrl.passage;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.entity.Product;
import com.jeequan.jeepay.core.entity.StatisticsPassage;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.PayPassageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/passageRealTimeStat")
public class MchAppRealStatController extends CommonCtrl {
    @Autowired
    private PayPassageService payPassageService;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ApiRes countReal() {
        try {
            PayPassage payPassage = getObject(PayPassage.class);

            QueryWrapper<PayPassage> wrapper = new QueryWrapper<>();

            if (StringUtils.isNotEmpty(payPassage.getPayPassageName())) {
                wrapper.like("pay_passage_name", payPassage.getPayPassageName().trim());
            }

            if (payPassage.getPayPassageId() != null) {
                wrapper.eq("pay_passage_id", payPassage.getPayPassageId());
            }

            if (payPassage.getState() != null) {
                wrapper.eq("state", payPassage.getState());
            }

            if (payPassage.getProductId() != null) {
                wrapper.eq("product_id", payPassage.getProductId());
            }

            List<PayPassage> payPassageList = payPassageService.list(wrapper);
            Long totalAmount = 0L;//订单总金额
            for (int i = 0; i < payPassageList.size(); i++) {
                totalAmount += payPassageList.get(i).getBalance();
            }
            JSONObject result = new JSONObject();
            result.put("totalBalance", totalAmount);
            result.put("passageNum", payPassageList.size());

            return ApiRes.ok(result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }
}