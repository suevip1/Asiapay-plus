package com.jeequan.jeepay.mch.ctrl.stat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchProduct;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.Product;
import com.jeequan.jeepay.core.entity.StatisticsMch;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchProductService;
import com.jeequan.jeepay.service.impl.ProductService;
import com.jeequan.jeepay.service.impl.StatisticsMchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 商户支付产品管理类
 */
@Slf4j
@RestController
@RequestMapping("/api/mchDayStat")
public class MchDayStatController extends CommonCtrl {

    @Autowired
    private StatisticsMchService statisticsMchService;

    @GetMapping
    public ApiRes list() {
        LambdaQueryWrapper<StatisticsMch> wrapper = StatisticsMch.gw();
        wrapper.orderByDesc(StatisticsMch::getCreatedAt);
        IPage<StatisticsMch> pages = statisticsMchService.page(getIPage(true), wrapper.eq(StatisticsMch::getMchNo, getCurrentMchNo()));
        return ApiRes.ok(pages);
    }
}