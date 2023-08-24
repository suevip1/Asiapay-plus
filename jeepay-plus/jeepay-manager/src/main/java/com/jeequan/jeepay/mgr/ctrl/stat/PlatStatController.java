package com.jeequan.jeepay.mgr.ctrl.stat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.entity.StatisticsPlat;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.StatisticsPlatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/platStat")
public class PlatStatController extends CommonCtrl {

    @Autowired
    private StatisticsPlatService statisticsPlatService;

    @GetMapping
    public ApiRes list() {
        LambdaQueryWrapper<StatisticsPlat> wrapper = StatisticsPlat.gw();
        wrapper.orderByDesc(StatisticsPlat::getStatisticsDate);
        IPage<StatisticsPlat> pages = statisticsPlatService.page(getIPage(true), wrapper);
        return ApiRes.ok(pages);
    }
}