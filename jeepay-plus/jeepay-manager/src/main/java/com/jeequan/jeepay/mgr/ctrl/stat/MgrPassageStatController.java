package com.jeequan.jeepay.mgr.ctrl.stat;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.entity.StatisticsPassage;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/passageStat")
public class MgrPassageStatController extends CommonCtrl {

    @Autowired
    private StatisticsPassageService statisticsPassageService;

    @Autowired
    private ProductService productService;

    @Autowired
    private PayPassageService payPassageService;

    @GetMapping
    public ApiRes list() {
        StatisticsPassage statisticsPassage = getObject(StatisticsPassage.class);
        JSONObject paramJSON = getReqParamJSON();

        Map<Long, PayPassage> payPassageMap = payPassageService.getPayPassageMap();
        Map<Long, Product> productMap = productService.getProductMap();

        LambdaQueryWrapper<StatisticsPassage> wrapper = StatisticsPassage.gw();

        if (statisticsPassage.getPayPassageId() != null) {
            wrapper.eq(StatisticsPassage::getPayPassageId, statisticsPassage.getPayPassageId());
        }

        Long productId = null;
        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                wrapper.ge(StatisticsPassage::getStatisticsDate, paramJSON.getString("createdStart"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                wrapper.le(StatisticsPassage::getStatisticsDate, paramJSON.getString("createdEnd"));
            }
            productId = paramJSON.getLong("productId");
        }

        wrapper.orderByDesc(StatisticsPassage::getStatisticsDate);
        wrapper.orderByAsc(StatisticsPassage::getPayPassageId);
        IPage<StatisticsPassage> pages = statisticsPassageService.page(getIPage(true), wrapper);
        List<StatisticsPassage> records = statisticsPassageService.list(wrapper);
        List<StatisticsPassage> result = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            PayPassage payPassage = payPassageMap.get(records.get(i).getPayPassageId());
            StatisticsPassage statisticsPassageItem = records.get(i);
            if (productId != null) {
                if (productId.longValue() == payPassage.getProductId()) {
                    statisticsPassageItem.addExt("payPassageName", payPassage.getPayPassageName());
                    statisticsPassageItem.addExt("productName", productMap.get(payPassage.getProductId()).getProductName());
                    statisticsPassageItem.addExt("productId", payPassage.getProductId());
                    result.add(statisticsPassageItem);
                }
            } else {
                statisticsPassageItem.addExt("payPassageName", payPassage.getPayPassageName());
                statisticsPassageItem.addExt("productName", productMap.get(payPassage.getProductId()).getProductName());
                statisticsPassageItem.addExt("productId", payPassage.getProductId());
                result.add(statisticsPassageItem);
            }
        }
        pages.setTotal(records.size());
        pages.setRecords(result);
        return ApiRes.ok(pages);


    }
}