package com.jeequan.jeepay.service.CommonService;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 通用统计service
 */
@Slf4j
@Service
public class StatisticsService {
    /**
     * 入库订单统计前缀
     */
    private static final String REDIS_SUFFIX = "Statistics";

    /**
     * 成功的订单统计支付逻辑
     */
    private static final String REDIS_SUFFIX_SUCCESS = "Statistics_Success";
    /**
     * 一次最多处理2000条数据
     */
    private static final int COUNT_SIZE = 2000;

    @Autowired
    private StatisticsPlatService statisticsPlatService;

    @Autowired
    private StatisticsMchService statisticsMchService;

    @Autowired
    private StatisticsPassageService statisticsPassageService;

    @Autowired
    private StatisticsMchProductService statisticsMchProductService;

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    public ProductService productService;

    @Autowired
    public MchProductService mchProductService;


    public StatisticsPlat QueryStatisticsPlatByDate(Date date) {
        return statisticsPlatService.getById(date);
    }

    public StatisticsMch QueryStatisticsMchByDate(String mchNO, Date date) {
        return statisticsMchService.getOne(StatisticsMch.gw().eq(StatisticsMch::getMchNo, mchNO).eq(StatisticsMch::getStatisticsDate, date));
    }

    public StatisticsPassage QueryStatisticsPassageByDate(Long passageId, Date date) {
        return statisticsPassageService.getOne(StatisticsPassage.gw().eq(StatisticsPassage::getPayPassageId, passageId).eq(StatisticsPassage::getStatisticsDate, date));
    }

    public List<StatisticsMchProduct> QueryStatMchProduct(String mchNo, Date date) {

        Map<String, MchInfo> mchMap = mchInfoService.getMchInfoMap();
        Map<Long, Product> productMap = productService.getProductMap();

        LambdaQueryWrapper<StatisticsMchProduct> wrapper = StatisticsMchProduct.gw();
        wrapper.eq(StatisticsMchProduct::getMchNo, mchNo);
        wrapper.eq(StatisticsMchProduct::getStatisticsDate, date);

        wrapper.orderByAsc(StatisticsMchProduct::getProductId);
        List<StatisticsMchProduct> records = statisticsMchProductService.list(wrapper);

        Map<Long, MchProduct> mchProductMap = GetMchProduct(mchNo);
        //查费率并附上去
        for (int i = 0; i < records.size(); i++) {
            records.get(i).addExt("mchName", mchMap.get(records.get(i).getMchNo()).getMchName());
            records.get(i).addExt("productName", productMap.get(records.get(i).getProductId()).getProductName());
            String rate = mchProductMap.get(records.get(i).getProductId()).getMchRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) + "%";
            records.get(i).addExt("rate", rate);
        }
        return records;
    }

    /**
     * 获取商户-产品费率
     *
     * @param mchNo
     * @return
     */
    public Map<Long, MchProduct> GetMchProduct(String mchNo) {

        List<MchProduct> records = mchProductService.list(MchProduct.gw().select(MchProduct::getMchNo, MchProduct::getCreatedAt, MchProduct::getMchRate, MchProduct::getProductId).eq(MchProduct::getMchNo, mchNo).eq(MchProduct::getState, CS.YES));
        Map<Long, MchProduct> mchProductMap = new HashMap<>();
        for (int i = 0; i < records.size(); i++) {
            mchProductMap.put(records.get(i).getProductId(), records.get(i));
        }
        return mchProductMap;
    }

    /**
     * 入库订单存入缓存
     *
     * @param payOrder
     */
    public void PushPayOrderToCache(PayOrder payOrder) {
        RedisUtil.addToQueue(REDIS_SUFFIX, payOrder);
    }

    /**
     * 获取入库的订单
     *
     * @return
     */
    public List<PayOrder> PopPayOrderListFromCache() {

        List<PayOrder> list = new ArrayList<>();
        Long cacheSize = RedisUtil.getQueueLength(REDIS_SUFFIX);
        if (cacheSize.intValue() == 0) {
            return list;
        }
//        log.info("读取入库订单缓存,当前数量{}", cacheSize);
        int count = 0;
        if (cacheSize > COUNT_SIZE) {
            count = COUNT_SIZE;
        } else {
            count = cacheSize.intValue();
        }
        for (int index = 0; index < count; index++) {
            list.add(RedisUtil.removeFromQueue(REDIS_SUFFIX, PayOrder.class));
        }
//        log.info("获取入库订单缓存-{},剩余{}", count, RedisUtil.getQueueLength(REDIS_SUFFIX));
        return list;
    }

    /**
     * 成功订单存入缓存
     *
     * @param payOrder
     */
    public void PushSuccessPayOrderToCache(PayOrder payOrder) {
        RedisUtil.addToQueue(REDIS_SUFFIX_SUCCESS, payOrder);
    }

    /**
     * 获取已成功的订单
     *
     * @return
     */
    public List<PayOrder> PopSuccessPayOrderListFromCache() {

        List<PayOrder> list = new ArrayList<>();
        Long cacheSize = RedisUtil.getQueueLength(REDIS_SUFFIX_SUCCESS);
        if (cacheSize.intValue() == 0) {
            return list;
        }
//        log.info("读取成功订单缓存,当前数量{}", cacheSize);
        int count = 0;
        if (cacheSize > COUNT_SIZE) {
            count = COUNT_SIZE;
        } else {
            count = cacheSize.intValue();
        }
        for (int index = 0; index < count; index++) {
            list.add(RedisUtil.removeFromQueue(REDIS_SUFFIX_SUCCESS, PayOrder.class));
        }
//        log.info("获取成功订单缓存-{},剩余{}", count, RedisUtil.getQueueLength(REDIS_SUFFIX_SUCCESS));
        return list;
    }

}