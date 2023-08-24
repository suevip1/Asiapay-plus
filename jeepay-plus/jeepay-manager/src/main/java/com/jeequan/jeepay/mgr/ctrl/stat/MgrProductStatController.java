package com.jeequan.jeepay.mgr.ctrl.stat;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.entity.Product;
import com.jeequan.jeepay.core.entity.StatisticsProduct;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.ProductService;
import com.jeequan.jeepay.service.impl.StatisticsProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/productStat")
public class MgrProductStatController extends CommonCtrl {

    @Autowired
    private StatisticsProductService statisticsProductService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public ApiRes list() {
        StatisticsProduct statisticsProduct = getObject(StatisticsProduct.class);
        JSONObject paramJSON = getReqParamJSON();

        Map<Long, Product> productMap = productService.getProductMap();

        LambdaQueryWrapper<StatisticsProduct> wrapper = StatisticsProduct.gw();

        if (statisticsProduct.getProductId() != null) {
            wrapper.eq(StatisticsProduct::getProductId, statisticsProduct.getProductId());
        }

        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                wrapper.ge(StatisticsProduct::getStatisticsDate, paramJSON.getString("createdStart"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                wrapper.le(StatisticsProduct::getStatisticsDate, paramJSON.getString("createdEnd"));
            }
        }


        wrapper.orderByDesc(StatisticsProduct::getStatisticsDate);
        wrapper.orderByDesc(StatisticsProduct::getTotalSuccessAmount);
        IPage<StatisticsProduct> pages = statisticsProductService.page(getIPage(true), wrapper);
        List<StatisticsProduct> records = pages.getRecords();

        for (int i = 0; i < records.size(); i++) {
            Product product = productMap.get(records.get(i).getProductId());
            records.get(i).addExt("productName", product.getProductName());
        }
        pages.setRecords(records);
        return ApiRes.ok(pages);
    }
}