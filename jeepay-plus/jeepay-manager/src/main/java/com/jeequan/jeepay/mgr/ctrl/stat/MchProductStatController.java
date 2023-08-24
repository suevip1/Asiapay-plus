package com.jeequan.jeepay.mgr.ctrl.stat;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.Product;
import com.jeequan.jeepay.core.entity.StatisticsMchProduct;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.ProductService;
import com.jeequan.jeepay.service.impl.StatisticsMchProductService;
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
@RequestMapping("/api/mchProductStat")
public class MchProductStatController extends CommonCtrl {

    @Autowired
    private StatisticsMchProductService statisticsMchProductService;

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public ApiRes list() {
        try {
            StatisticsMchProduct statisticsMchProduct = getObject(StatisticsMchProduct.class);
            JSONObject paramJSON = getReqParamJSON();

            Map<String, MchInfo> mchMap = mchInfoService.getMchInfoMap();
            Map<Long, Product> productMap = productService.getProductMap();

            LambdaQueryWrapper<StatisticsMchProduct> wrapper = StatisticsMchProduct.gw();

            if (StringUtils.isNotEmpty(statisticsMchProduct.getMchNo())) {
                wrapper.eq(StatisticsMchProduct::getMchNo, statisticsMchProduct.getMchNo());
            }

            if (statisticsMchProduct.getProductId() != null) {
                wrapper.eq(StatisticsMchProduct::getProductId, statisticsMchProduct.getProductId());
            }

            if (paramJSON != null) {
                if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                    wrapper.ge(StatisticsMchProduct::getStatisticsDate, paramJSON.getString("createdStart"));
                }
                if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                    wrapper.le(StatisticsMchProduct::getStatisticsDate, paramJSON.getString("createdEnd"));
                }
            }

            wrapper.orderByDesc(StatisticsMchProduct::getStatisticsDate);
            wrapper.orderByDesc(StatisticsMchProduct::getMchNo);
            IPage<StatisticsMchProduct> pages = statisticsMchProductService.page(getIPage(true), wrapper);
            List<StatisticsMchProduct> records = statisticsMchProductService.list(wrapper);

            for (int i = 0; i < records.size(); i++) {
                records.get(i).addExt("mchName", mchMap.get(records.get(i).getMchNo()).getMchName());
                records.get(i).addExt("productName", productMap.get(records.get(i).getProductId()).getProductName());
            }
            pages.setTotal(records.size());
            pages.setRecords(records);
            return ApiRes.ok(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }
}