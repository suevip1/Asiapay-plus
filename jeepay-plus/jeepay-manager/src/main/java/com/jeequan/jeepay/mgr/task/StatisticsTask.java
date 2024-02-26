package com.jeequan.jeepay.mgr.task;


import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.service.CommonService.StatisticsService;
import com.jeequan.jeepay.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;

@Slf4j
@Component
public class StatisticsTask {

    @Autowired
    private StatisticsMchService statisticsMchService;

    @Autowired
    private StatisticsAgentService statisticsAgentService;

    @Autowired
    private StatisticsAgentMchService statisticsAgentMchService;

    @Autowired
    private StatisticsAgentPassageService statisticsAgentPassageService;

    @Autowired
    private StatisticsPassageService statisticsPassageService;

    @Autowired
    private StatisticsPlatService statisticsPlatService;

    @Autowired
    private StatisticsMchProductService statisticsMchProductService;

    @Autowired
    private StatisticsProductService statisticsProductService;

    @Autowired
    private StatisticsService statisticsService;
    private final Object lock = new Object();

    @Scheduled(fixedRate = 5000) // 每5秒执行一次 5000
    public void start() {
        synchronized (lock) {
            //入库订单统计
            UpdateRecord(statisticsService.PopPayOrderListFromCache());
            //成功的订单统计
            UpdateSuccessRecord(statisticsService.PopSuccessPayOrderListFromCache());
        }
    }

    private void UpdateRecord(List<PayOrder> payOrderList) {
        if (payOrderList.size() == 0) {
            return;
        }
        log.info("===订单入库统计开始, " + payOrderList.size());

        //平台统计
        Map<Date, StatisticsPlat> statisticsPlatMap = new HashMap<>();
        //商户统计
        List<StatisticsMch> statisticsMchList = new ArrayList<>();
        //代理统计
        List<StatisticsAgent> statisticsAgentList = new ArrayList<>();
        //代理-商户统计
        List<StatisticsAgentMch> statisticsAgentMchList = new ArrayList<>();
        //代理-通道统计
        List<StatisticsAgentPassage> statisticsAgentPassageList = new ArrayList<>();
        //通道统计
        List<StatisticsPassage> statisticsPassageList = new ArrayList<>();
        //产品统计
        List<StatisticsProduct> statisticsProductList = new ArrayList<>();
        //产品-商户统计 商户号-产品号-值
        List<StatisticsMchProduct> statisticsMchProductList = new ArrayList<>();

        for (int index = 0; index < payOrderList.size(); index++) {
            PayOrder payOrder = payOrderList.get(index);
            String countDayStr = DateUtil.format(payOrder.getCreatedAt(), "yyyy-MM-dd");
            Date countDay = DateUtil.parse(countDayStr, "yyyy-MM-dd");

            //平台统计
            StatisticsPlat statisticsPlat = statisticsPlatMap.get(countDay);
            if (statisticsPlat == null) {
                statisticsPlat = new StatisticsPlat();
                statisticsPlat.setCreatedAt(new Date());
                statisticsPlat.setTotalAmount(payOrder.getAmount());
                statisticsPlat.setTotalOrderCount(1);
                statisticsPlat.setStatisticsDate(countDay);
                statisticsPlatMap.put(countDay, statisticsPlat);
            } else {
                statisticsPlat.setTotalAmount(statisticsPlat.getTotalAmount() + payOrder.getAmount());
                statisticsPlat.setTotalOrderCount(statisticsPlat.getTotalOrderCount() + 1);
            }

            //商户 商户号-日期
            StatisticsMch statisticsMch = statisticsMchList.stream()
                    .filter(item -> item.getMchNo().equals(payOrder.getMchNo()) && DateUtil.between(item.getStatisticsDate(), countDay, DateUnit.DAY) == 0)
                    .findFirst().orElse(null);
            if (statisticsMch == null) {
                statisticsMch = new StatisticsMch();
                statisticsMch.setMchNo(payOrder.getMchNo());
                statisticsMch.setStatisticsDate(countDay);
                statisticsMch.setTotalOrderCount(1);
                statisticsMch.setTotalAmount(payOrder.getAmount());

                statisticsMchList.add(statisticsMch);
            } else {
                statisticsMch.setTotalOrderCount(statisticsMch.getTotalOrderCount() + 1);
                statisticsMch.setTotalAmount(statisticsMch.getTotalAmount() + payOrder.getAmount());
            }

            //通道 通道号-日期
            StatisticsPassage statisticsPassage = statisticsPassageList.stream()
                    .filter(item -> item.getPayPassageId().longValue() == payOrder.getPassageId().longValue() && DateUtil.between(item.getStatisticsDate(), countDay, DateUnit.DAY) == 0)
                    .findFirst().orElse(null);
            if (statisticsPassage == null) {
                statisticsPassage = new StatisticsPassage();
                statisticsPassage.setPayPassageId(payOrder.getPassageId());
                statisticsPassage.setStatisticsDate(countDay);
                statisticsPassage.setTotalOrderCount(1);
                statisticsPassage.setTotalAmount(payOrder.getAmount());

                statisticsPassageList.add(statisticsPassage);
            } else {
                statisticsPassage.setTotalOrderCount(statisticsPassage.getTotalOrderCount() + 1);
                statisticsPassage.setTotalAmount(statisticsPassage.getTotalAmount() + payOrder.getAmount());
            }


            //产品 产品号-日期
            StatisticsProduct statisticsProduct = statisticsProductList.stream()
                    .filter(item -> item.getProductId().longValue() == payOrder.getProductId().longValue() && DateUtil.between(item.getStatisticsDate(), countDay, DateUnit.DAY) == 0)
                    .findFirst().orElse(null);

            if (statisticsProduct == null) {
                statisticsProduct = new StatisticsProduct();
                statisticsProduct.setProductId(payOrder.getProductId());
                statisticsProduct.setStatisticsDate(countDay);
                statisticsProduct.setTotalOrderCount(1);
                statisticsProduct.setTotalAmount(payOrder.getAmount());

                statisticsProductList.add(statisticsProduct);
            } else {
                statisticsProduct.setTotalOrderCount(statisticsProduct.getTotalOrderCount() + 1);
                statisticsProduct.setTotalAmount(statisticsProduct.getTotalAmount() + payOrder.getAmount());
            }

            //产品-商户  日期、产品ID、商户号相等
            StatisticsMchProduct statisticsMchProduct = statisticsMchProductList.stream()
                    .filter(item -> item.getProductId().longValue() == payOrder.getProductId().longValue() && DateUtil.between(item.getStatisticsDate(), countDay, DateUnit.DAY) == 0 && item.getMchNo().equals(payOrder.getMchNo()))
                    .findFirst().orElse(null);

            if (statisticsMchProduct == null) {
                //add
                statisticsMchProduct = new StatisticsMchProduct();
                statisticsMchProduct.setProductId(payOrder.getProductId());
                statisticsMchProduct.setMchNo(payOrder.getMchNo());
                statisticsMchProduct.setStatisticsDate(countDay);
                statisticsMchProduct.setTotalOrderCount(1);
                statisticsMchProduct.setTotalAmount(payOrder.getAmount());

                statisticsMchProductList.add(statisticsMchProduct);
            } else {
                //update
                statisticsMchProduct.setTotalOrderCount(statisticsMchProduct.getTotalOrderCount() + 1);
                statisticsMchProduct.setTotalAmount(statisticsMchProduct.getTotalAmount() + payOrder.getAmount());
            }


            //代理 statisticsAgentList
            String mchAgentNo = payOrder.getAgentNo();
            String passageAgentNo = payOrder.getAgentNoPassage();
            //代理为同一个合并处理  代理号-日期
            if (StringUtils.isNotBlank(mchAgentNo) && StringUtils.isNotBlank(passageAgentNo) && passageAgentNo.equals(mchAgentNo)) {
                StatisticsAgent statisticsAgentTotal = statisticsAgentList.stream()
                        .filter(item -> item.getAgentNo().equals(mchAgentNo) && DateUtil.between(item.getStatisticsDate(), countDay, DateUnit.DAY) == 0)
                        .findFirst().orElse(null);

                if (statisticsAgentTotal == null) {
                    statisticsAgentTotal = new StatisticsAgent();
                    statisticsAgentTotal.setAgentNo(mchAgentNo);
                    statisticsAgentTotal.setStatisticsDate(countDay);
                    statisticsAgentTotal.setTotalOrderCount(1);
                    statisticsAgentTotal.setTotalAmount(payOrder.getAmount());

                    statisticsAgentList.add(statisticsAgentTotal);
                } else {
                    statisticsAgentTotal.setTotalOrderCount(statisticsAgentTotal.getTotalOrderCount() + 1);
                    statisticsAgentTotal.setTotalAmount(statisticsAgentTotal.getTotalAmount() + payOrder.getAmount());
                }
            } else {
                //商户代理
                if (StringUtils.isNotBlank(mchAgentNo)) {

                    StatisticsAgent statisticsAgentMch = statisticsAgentList.stream()
                            .filter(item -> item.getAgentNo().equals(mchAgentNo) && DateUtil.between(item.getStatisticsDate(), countDay, DateUnit.DAY) == 0)
                            .findFirst().orElse(null);

                    if (statisticsAgentMch == null) {
                        statisticsAgentMch = new StatisticsAgent();
                        statisticsAgentMch.setAgentNo(mchAgentNo);
                        statisticsAgentMch.setStatisticsDate(countDay);
                        statisticsAgentMch.setTotalOrderCount(1);
                        statisticsAgentMch.setTotalAmount(payOrder.getAmount());

                        statisticsAgentList.add(statisticsAgentMch);
                    } else {
                        statisticsAgentMch.setTotalOrderCount(statisticsAgentMch.getTotalOrderCount() + 1);
                        statisticsAgentMch.setTotalAmount(statisticsAgentMch.getTotalAmount() + payOrder.getAmount());
                    }
                }

                //通道代理
                if (StringUtils.isNotBlank(passageAgentNo)) {

                    StatisticsAgent statisticsAgentPassage = statisticsAgentList.stream()
                            .filter(item -> item.getAgentNo().equals(passageAgentNo) && DateUtil.between(item.getStatisticsDate(), countDay, DateUnit.DAY) == 0)
                            .findFirst().orElse(null);

                    if (statisticsAgentPassage == null) {
                        statisticsAgentPassage = new StatisticsAgent();
                        statisticsAgentPassage.setAgentNo(passageAgentNo);
                        statisticsAgentPassage.setStatisticsDate(countDay);
                        statisticsAgentPassage.setTotalOrderCount(1);
                        statisticsAgentPassage.setTotalAmount(payOrder.getAmount());

                        statisticsAgentList.add(statisticsAgentPassage);
                    } else {
                        statisticsAgentPassage.setTotalOrderCount(statisticsAgentPassage.getTotalOrderCount() + 1);
                        statisticsAgentPassage.setTotalAmount(statisticsAgentPassage.getTotalAmount() + payOrder.getAmount());
                    }
                }
            }
            //代理-商户  代理号-商户号-日期
            if (StringUtils.isNotBlank(mchAgentNo)) {
                //statisticsAgentMchList

                StatisticsAgentMch statisticsAgentMch = statisticsAgentMchList.stream()
                        .filter(item -> item.getAgentNo().equals(mchAgentNo) && DateUtil.between(item.getStatisticsDate(), countDay, DateUnit.DAY) == 0 && item.getMchNo().equals(payOrder.getMchNo()))
                        .findFirst().orElse(null);

                if (statisticsAgentMch == null) {
                    statisticsAgentMch = new StatisticsAgentMch();
                    statisticsAgentMch.setAgentNo(mchAgentNo);
                    statisticsAgentMch.setMchNo(payOrder.getMchNo());
                    statisticsAgentMch.setStatisticsDate(countDay);
                    statisticsAgentMch.setTotalOrderCount(1);
                    statisticsAgentMch.setTotalAmount(payOrder.getAmount());

                    statisticsAgentMchList.add(statisticsAgentMch);
                } else {
                    statisticsAgentMch.setTotalOrderCount(statisticsAgentMch.getTotalOrderCount() + 1);
                    statisticsAgentMch.setTotalAmount(statisticsAgentMch.getTotalAmount() + payOrder.getAmount());
                }
            }


            //代理-通道 代理商户号-通道ID-值  statisticsAgentPassageList
            if (StringUtils.isNotBlank(passageAgentNo)) {

                StatisticsAgentPassage statisticsAgentPassage = statisticsAgentPassageList.stream()
                        .filter(item -> item.getAgentNo().equals(passageAgentNo) && DateUtil.between(item.getStatisticsDate(), countDay, DateUnit.DAY) == 0 && item.getPayPassageId().longValue() == payOrder.getPassageId().longValue())
                        .findFirst().orElse(null);

                if (statisticsAgentPassage == null) {
                    statisticsAgentPassage = new StatisticsAgentPassage();
                    statisticsAgentPassage.setAgentNo(passageAgentNo);
                    statisticsAgentPassage.setPayPassageId(payOrder.getPassageId());
                    statisticsAgentPassage.setStatisticsDate(countDay);
                    statisticsAgentPassage.setTotalOrderCount(1);
                    statisticsAgentPassage.setTotalAmount(payOrder.getAmount());

                    statisticsAgentPassageList.add(statisticsAgentPassage);
                } else {
                    statisticsAgentPassage.setTotalOrderCount(statisticsAgentPassage.getTotalOrderCount() + 1);
                    statisticsAgentPassage.setTotalAmount(statisticsAgentPassage.getTotalAmount() + payOrder.getAmount());
                }
            }
        }
        SaveData(statisticsPlatMap, statisticsMchList, statisticsAgentList, statisticsAgentMchList, statisticsAgentPassageList, statisticsPassageList, statisticsProductList, statisticsMchProductList);
        log.info("===订单入库统计完成, " + payOrderList.size());
    }

    /**
     * 更新到数据库
     *
     * @param statisticsPlatMap
     * @param statisticsMchList
     * @param statisticsAgentList
     * @param statisticsAgentMchList
     * @param statisticsAgentPassageList
     * @param statisticsPassageList
     * @param statisticsProductList
     * @param statisticsMchProductList
     */
    public void SaveData(Map<Date, StatisticsPlat> statisticsPlatMap, List<StatisticsMch> statisticsMchList, List<StatisticsAgent> statisticsAgentList, List<StatisticsAgentMch> statisticsAgentMchList, List<StatisticsAgentPassage> statisticsAgentPassageList, List<StatisticsPassage> statisticsPassageList, List<StatisticsProduct> statisticsProductList, List<StatisticsMchProduct> statisticsMchProductList) {
        //平台
        statisticsPlatMap.forEach((date, newValue) -> {
            // 处理键值对
            StatisticsPlat statisticsPlat = statisticsPlatService.getById(date);
            if (statisticsPlat == null) {
                statisticsPlatService.save(newValue);
            } else {
                statisticsPlat.setTotalOrderCount(statisticsPlat.getTotalOrderCount() + newValue.getTotalOrderCount());
                statisticsPlat.setTotalAmount(statisticsPlat.getTotalAmount() + newValue.getTotalAmount());
                statisticsPlatService.updateById(statisticsPlat);
            }
        });


        //商户 商户号-日期
        statisticsMchList.forEach((newValue) -> {
            StatisticsMch statisticsMch = statisticsMchService.getOne(StatisticsMch.gw().eq(StatisticsMch::getMchNo, newValue.getMchNo()).eq(StatisticsMch::getStatisticsDate, newValue.getStatisticsDate()));
            if (statisticsMch == null) {
                statisticsMchService.save(newValue);
            } else {
                statisticsMch.setTotalOrderCount(statisticsMch.getTotalOrderCount() + newValue.getTotalOrderCount());
                statisticsMch.setTotalAmount(statisticsMch.getTotalAmount() + newValue.getTotalAmount());
                statisticsMchService.updateById(statisticsMch);
            }
        });

        //代理
        statisticsAgentList.forEach((newValue) -> {
            StatisticsAgent statisticsAgent = statisticsAgentService.getOne(StatisticsAgent.gw().eq(StatisticsAgent::getAgentNo, newValue.getAgentNo()).eq(StatisticsAgent::getStatisticsDate, newValue.getStatisticsDate()));
            if (statisticsAgent == null) {
                statisticsAgentService.save(newValue);
            } else {
                statisticsAgent.setTotalOrderCount(statisticsAgent.getTotalOrderCount() + newValue.getTotalOrderCount());
                statisticsAgent.setTotalAmount(statisticsAgent.getTotalAmount() + newValue.getTotalAmount());
                statisticsAgentService.updateById(statisticsAgent);
            }
        });

        //代理-商户 代理号-商户号-日期
        statisticsAgentMchList.forEach((newValue) -> {
            StatisticsAgentMch statisticsAgentMch = statisticsAgentMchService.getOne(StatisticsAgentMch.gw().eq(StatisticsAgentMch::getAgentNo, newValue.getAgentNo()).eq(StatisticsAgentMch::getMchNo, newValue.getMchNo()).eq(StatisticsAgentMch::getStatisticsDate, newValue.getStatisticsDate()));
            if (statisticsAgentMch == null) {
                statisticsAgentMchService.save(newValue);
            } else {
                statisticsAgentMch.setTotalOrderCount(statisticsAgentMch.getTotalOrderCount() + newValue.getTotalOrderCount());
                statisticsAgentMch.setTotalAmount(statisticsAgentMch.getTotalAmount() + newValue.getTotalAmount());
                statisticsAgentMchService.updateById(statisticsAgentMch);
            }
        });

        //代理-通道-日期
        statisticsAgentPassageList.forEach((newValue) -> {
            StatisticsAgentPassage statisticsAgentPassage = statisticsAgentPassageService.getOne(StatisticsAgentPassage.gw().eq(StatisticsAgentPassage::getAgentNo, newValue.getAgentNo()).eq(StatisticsAgentPassage::getPayPassageId, newValue.getPayPassageId()).eq(StatisticsAgentPassage::getStatisticsDate, newValue.getStatisticsDate()));
            if (statisticsAgentPassage == null) {
                statisticsAgentPassageService.save(newValue);
            } else {
                statisticsAgentPassage.setTotalOrderCount(statisticsAgentPassage.getTotalOrderCount() + newValue.getTotalOrderCount());
                statisticsAgentPassage.setTotalAmount(statisticsAgentPassage.getTotalAmount() + newValue.getTotalAmount());
                statisticsAgentPassageService.updateById(statisticsAgentPassage);
            }
        });

        //通道-日期
        statisticsPassageList.forEach((newValue) -> {
            StatisticsPassage statisticsPassage = statisticsPassageService.getOne(StatisticsPassage.gw().eq(StatisticsPassage::getPayPassageId, newValue.getPayPassageId()).eq(StatisticsPassage::getStatisticsDate, newValue.getStatisticsDate()));
            if (statisticsPassage == null) {
                statisticsPassageService.save(newValue);
            } else {
                statisticsPassage.setTotalOrderCount(statisticsPassage.getTotalOrderCount() + newValue.getTotalOrderCount());
                statisticsPassage.setTotalAmount(statisticsPassage.getTotalAmount() + newValue.getTotalAmount());
                statisticsPassageService.updateById(statisticsPassage);
            }
        });

        //产品 日期
        statisticsProductList.forEach((newValue) -> {
            StatisticsProduct statisticsProduct = statisticsProductService.getOne(StatisticsProduct.gw().eq(StatisticsProduct::getProductId, newValue.getProductId()).eq(StatisticsProduct::getStatisticsDate, newValue.getStatisticsDate()));
            if (statisticsProduct == null) {
                statisticsProductService.save(newValue);
            } else {
                statisticsProduct.setTotalOrderCount(statisticsProduct.getTotalOrderCount() + newValue.getTotalOrderCount());
                statisticsProduct.setTotalAmount(statisticsProduct.getTotalAmount() + newValue.getTotalAmount());
                statisticsProductService.updateById(statisticsProduct);
            }
        });


        //产品-商户-日期

        statisticsMchProductList.forEach((newValue) -> {
            StatisticsMchProduct statisticsMchProduct = statisticsMchProductService.getOne(StatisticsMchProduct.gw().eq(StatisticsMchProduct::getProductId, newValue.getProductId()).eq(StatisticsMchProduct::getStatisticsDate, newValue.getStatisticsDate()).eq(StatisticsMchProduct::getMchNo, newValue.getMchNo()));

            if (statisticsMchProduct == null) {
                statisticsMchProductService.save(newValue);
            } else {
                statisticsMchProduct.setTotalOrderCount(statisticsMchProduct.getTotalOrderCount() + newValue.getTotalOrderCount());
                statisticsMchProduct.setTotalAmount(statisticsMchProduct.getTotalAmount() + newValue.getTotalAmount());
                statisticsMchProductService.updateById(statisticsMchProduct);
            }
        });
    }

    /**
     * 统计成功的订单
     *
     * @param payOrderList
     */
    private void UpdateSuccessRecord(List<PayOrder> payOrderList) {
        if (payOrderList.size() == 0) {
            return;
        }
        log.info("===订单成交统计开始, " + payOrderList.size());

        //平台统计
        Map<Date, StatisticsPlat> statisticsPlatMap = new HashMap<>();
        //商户统计
        List<StatisticsMch> statisticsMchList = new ArrayList<>();
        //代理统计
        List<StatisticsAgent> statisticsAgentList = new ArrayList<>();
        //代理-商户统计
        List<StatisticsAgentMch> statisticsAgentMchList = new ArrayList<>();
        //代理-通道统计
        List<StatisticsAgentPassage> statisticsAgentPassageList = new ArrayList<>();
        //通道统计
        List<StatisticsPassage> statisticsPassageList = new ArrayList<>();
        //产品统计
        List<StatisticsProduct> statisticsProductList = new ArrayList<>();
        //产品-商户统计 商户号-产品号-值
        List<StatisticsMchProduct> statisticsMchProductList = new ArrayList<>();

        for (int index = 0; index < payOrderList.size(); index++) {
            PayOrder payOrder = payOrderList.get(index);
            String countDayStr = DateUtil.format(payOrder.getCreatedAt(), "yyyy-MM-dd");
            Date countDay = DateUtil.parse(countDayStr, "yyyy-MM-dd");

            //平台统计
            StatisticsPlat statisticsPlat = statisticsPlatMap.get(countDay);
            if (statisticsPlat == null) {
                statisticsPlat = new StatisticsPlat();
                statisticsPlat.setCreatedAt(new Date());
                statisticsPlat.setStatisticsDate(countDay);

                statisticsPlat.setTotalSuccessAmount(payOrder.getAmount());

                if (payOrder.getState() != PayOrder.STATE_REFUND) {
                    statisticsPlat.setOrderSuccessCount(1);
                } else {
                    statisticsPlat.setOrderSuccessCount(0);
                }
                statisticsPlat.setPlatTotalIncome(CalPlatIncome(payOrder));

                statisticsPlatMap.put(countDay, statisticsPlat);
            } else {
                statisticsPlat.setTotalSuccessAmount(statisticsPlat.getTotalSuccessAmount() + payOrder.getAmount());
                statisticsPlat.setPlatTotalIncome(statisticsPlat.getPlatTotalIncome() + CalPlatIncome(payOrder));
                if (payOrder.getState() != PayOrder.STATE_REFUND) {
                    statisticsPlat.setOrderSuccessCount(statisticsPlat.getOrderSuccessCount() + 1);
                }
            }

            //商户 商户号-日期
            StatisticsMch statisticsMch = statisticsMchList.stream()
                    .filter(item -> item.getMchNo().equals(payOrder.getMchNo()) && DateUtil.between(item.getStatisticsDate(), countDay, DateUnit.DAY) == 0)
                    .findFirst().orElse(null);
            if (statisticsMch == null) {
                statisticsMch = new StatisticsMch();
                statisticsMch.setMchNo(payOrder.getMchNo());
                statisticsMch.setStatisticsDate(countDay);

                if (payOrder.getState() != PayOrder.STATE_REFUND) {
                    statisticsMch.setOrderSuccessCount(1);
                } else {
                    statisticsMch.setOrderSuccessCount(0);
                }
                statisticsMch.setTotalSuccessAmount(payOrder.getAmount());
                //商户总成本
                statisticsMch.setTotalMchCost(payOrder.getMchFeeAmount());

                statisticsMchList.add(statisticsMch);
            } else {
                if (payOrder.getState() != PayOrder.STATE_REFUND) {
                    statisticsMch.setOrderSuccessCount(statisticsMch.getOrderSuccessCount() + 1);
                }
                statisticsMch.setTotalSuccessAmount(statisticsMch.getTotalSuccessAmount() + payOrder.getAmount());
                //商户总成本
                statisticsMch.setTotalMchCost(statisticsMch.getTotalMchCost() + payOrder.getMchFeeAmount());
            }

            //通道 通道号-日期
            StatisticsPassage statisticsPassage = statisticsPassageList.stream()
                    .filter(item -> item.getPayPassageId().longValue() == payOrder.getPassageId().longValue() && DateUtil.between(item.getStatisticsDate(), countDay, DateUnit.DAY) == 0)
                    .findFirst().orElse(null);
            if (statisticsPassage == null) {
                statisticsPassage = new StatisticsPassage();
                statisticsPassage.setPayPassageId(payOrder.getPassageId());
                statisticsPassage.setStatisticsDate(countDay);

                if (payOrder.getState() != PayOrder.STATE_REFUND) {
                    statisticsPassage.setOrderSuccessCount(1);
                } else {
                    statisticsPassage.setOrderSuccessCount(0);
                }
                statisticsPassage.setTotalSuccessAmount(payOrder.getAmount());
                statisticsPassage.setTotalPassageCost(payOrder.getPassageFeeAmount());

                statisticsPassageList.add(statisticsPassage);
            } else {
                if (payOrder.getState() != PayOrder.STATE_REFUND) {
                    statisticsPassage.setOrderSuccessCount(statisticsPassage.getOrderSuccessCount() + 1);
                }
                statisticsPassage.setTotalSuccessAmount(statisticsPassage.getTotalSuccessAmount() + payOrder.getAmount());
                statisticsPassage.setTotalPassageCost(statisticsPassage.getTotalPassageCost() + payOrder.getPassageFeeAmount());
            }


            //产品 产品号-日期
            StatisticsProduct statisticsProduct = statisticsProductList.stream()
                    .filter(item -> item.getProductId().longValue() == payOrder.getProductId().longValue() && DateUtil.between(item.getStatisticsDate(), countDay, DateUnit.DAY) == 0)
                    .findFirst().orElse(null);

            if (statisticsProduct == null) {
                statisticsProduct = new StatisticsProduct();
                statisticsProduct.setProductId(payOrder.getProductId());
                statisticsProduct.setStatisticsDate(countDay);

                if (payOrder.getState() != PayOrder.STATE_REFUND) {
                    statisticsProduct.setOrderSuccessCount(1);
                } else {
                    statisticsProduct.setOrderSuccessCount(0);
                }
                statisticsProduct.setTotalSuccessAmount(payOrder.getAmount());

                statisticsProductList.add(statisticsProduct);
            } else {
                if (payOrder.getState() != PayOrder.STATE_REFUND) {
                    statisticsProduct.setOrderSuccessCount(statisticsProduct.getOrderSuccessCount() + 1);
                }
                statisticsProduct.setTotalSuccessAmount(statisticsProduct.getTotalSuccessAmount() + payOrder.getAmount());
            }

            //产品-商户  日期、产品ID、商户号相等
            StatisticsMchProduct statisticsMchProduct = statisticsMchProductList.stream()
                    .filter(item -> item.getProductId().longValue() == payOrder.getProductId().longValue() && DateUtil.between(item.getStatisticsDate(), countDay, DateUnit.DAY) == 0 && item.getMchNo().equals(payOrder.getMchNo()))
                    .findFirst().orElse(null);

            if (statisticsMchProduct == null) {
                //add
                statisticsMchProduct = new StatisticsMchProduct();
                statisticsMchProduct.setProductId(payOrder.getProductId());
                statisticsMchProduct.setMchNo(payOrder.getMchNo());
                statisticsMchProduct.setStatisticsDate(countDay);

                if (payOrder.getState() != PayOrder.STATE_REFUND) {
                    statisticsMchProduct.setOrderSuccessCount(1);
                } else {
                    statisticsMchProduct.setOrderSuccessCount(0);
                }
                statisticsMchProduct.setTotalSuccessAmount(payOrder.getAmount());
                statisticsMchProduct.setTotalCost(payOrder.getMchFeeAmount());

                statisticsMchProductList.add(statisticsMchProduct);
            } else {
                //update
                if (payOrder.getState() != PayOrder.STATE_REFUND) {
                    statisticsMchProduct.setOrderSuccessCount(statisticsMchProduct.getOrderSuccessCount() + 1);
                }
                statisticsMchProduct.setTotalSuccessAmount(statisticsMchProduct.getTotalSuccessAmount() + payOrder.getAmount());
                statisticsMchProduct.setTotalCost(statisticsMchProduct.getTotalCost() + payOrder.getMchFeeAmount());
            }


            //代理 statisticsAgentList
            String mchAgentNo = payOrder.getAgentNo();
            String passageAgentNo = payOrder.getAgentNoPassage();
            //代理为同一个合并处理  代理号-日期
            if (StringUtils.isNotBlank(mchAgentNo) && StringUtils.isNotBlank(passageAgentNo) && passageAgentNo.equals(mchAgentNo)) {
                StatisticsAgent statisticsAgentTotal = statisticsAgentList.stream()
                        .filter(item -> item.getAgentNo().equals(mchAgentNo) && DateUtil.between(item.getStatisticsDate(), countDay, DateUnit.DAY) == 0)
                        .findFirst().orElse(null);

                if (statisticsAgentTotal == null) {
                    statisticsAgentTotal = new StatisticsAgent();
                    statisticsAgentTotal.setAgentNo(mchAgentNo);
                    statisticsAgentTotal.setStatisticsDate(countDay);

                    if (payOrder.getState() != PayOrder.STATE_REFUND) {
                        statisticsAgentTotal.setOrderSuccessCount(1);
                    } else {
                        statisticsAgentTotal.setOrderSuccessCount(0);
                    }
                    statisticsAgentTotal.setTotalSuccessAmount(payOrder.getAmount());
                    statisticsAgentTotal.setTotalAgentIncome(payOrder.getAgentFeeAmount() + payOrder.getAgentPassageFee());

                    statisticsAgentList.add(statisticsAgentTotal);
                } else {
                    if (payOrder.getState() != PayOrder.STATE_REFUND) {
                        statisticsAgentTotal.setOrderSuccessCount(statisticsAgentTotal.getOrderSuccessCount() + 1);
                    }
                    statisticsAgentTotal.setTotalSuccessAmount(statisticsAgentTotal.getTotalSuccessAmount() + payOrder.getAmount());
                    statisticsAgentTotal.setTotalAgentIncome(payOrder.getAgentFeeAmount() + payOrder.getAgentPassageFee() + statisticsAgentTotal.getTotalAgentIncome());
                }
            } else {
                //商户代理
                if (StringUtils.isNotBlank(mchAgentNo)) {

                    StatisticsAgent statisticsAgentMch = statisticsAgentList.stream()
                            .filter(item -> item.getAgentNo().equals(mchAgentNo) && DateUtil.between(item.getStatisticsDate(), countDay, DateUnit.DAY) == 0)
                            .findFirst().orElse(null);

                    if (statisticsAgentMch == null) {
                        statisticsAgentMch = new StatisticsAgent();
                        statisticsAgentMch.setAgentNo(mchAgentNo);
                        statisticsAgentMch.setStatisticsDate(countDay);

                        if (payOrder.getState() != PayOrder.STATE_REFUND) {
                            statisticsAgentMch.setOrderSuccessCount(1);
                        } else {
                            statisticsAgentMch.setOrderSuccessCount(0);
                        }

                        statisticsAgentMch.setTotalSuccessAmount(payOrder.getAmount());
                        statisticsAgentMch.setTotalAgentIncome(payOrder.getAgentFeeAmount());

                        statisticsAgentList.add(statisticsAgentMch);
                    } else {
                        if (payOrder.getState() != PayOrder.STATE_REFUND) {
                            statisticsAgentMch.setOrderSuccessCount(statisticsAgentMch.getOrderSuccessCount() + 1);
                        }
                        statisticsAgentMch.setTotalSuccessAmount(statisticsAgentMch.getTotalSuccessAmount() + payOrder.getAmount());
                        statisticsAgentMch.setTotalAgentIncome(payOrder.getAgentFeeAmount() + statisticsAgentMch.getTotalAgentIncome());
                    }
                }

                //通道代理
                if (StringUtils.isNotBlank(passageAgentNo)) {

                    StatisticsAgent statisticsAgentPassage = statisticsAgentList.stream()
                            .filter(item -> item.getAgentNo().equals(passageAgentNo) && DateUtil.between(item.getStatisticsDate(), countDay, DateUnit.DAY) == 0)
                            .findFirst().orElse(null);

                    if (statisticsAgentPassage == null) {
                        statisticsAgentPassage = new StatisticsAgent();
                        statisticsAgentPassage.setAgentNo(passageAgentNo);
                        statisticsAgentPassage.setStatisticsDate(countDay);

                        if (payOrder.getState() != PayOrder.STATE_REFUND) {
                            statisticsAgentPassage.setOrderSuccessCount(1);
                        } else {
                            statisticsAgentPassage.setOrderSuccessCount(0);
                        }
                        statisticsAgentPassage.setTotalSuccessAmount(payOrder.getAmount());
                        statisticsAgentPassage.setTotalAgentIncome(payOrder.getAgentPassageFee());

                        statisticsAgentList.add(statisticsAgentPassage);
                    } else {
                        if (payOrder.getState() != PayOrder.STATE_REFUND) {
                            statisticsAgentPassage.setOrderSuccessCount(statisticsAgentPassage.getOrderSuccessCount() + 1);
                        }
                        statisticsAgentPassage.setTotalSuccessAmount(statisticsAgentPassage.getTotalSuccessAmount() + payOrder.getAmount());
                        statisticsAgentPassage.setTotalAgentIncome(payOrder.getAgentPassageFee() + statisticsAgentPassage.getTotalAgentIncome());
                    }
                }
            }


            //代理-商户  代理号-商户号-值
            if (StringUtils.isNotBlank(mchAgentNo)) {

                StatisticsAgentMch statisticsAgentMch = statisticsAgentMchList.stream()
                        .filter(item -> item.getAgentNo().equals(mchAgentNo) && DateUtil.between(item.getStatisticsDate(), countDay, DateUnit.DAY) == 0 && item.getMchNo().equals(payOrder.getMchNo()))
                        .findFirst().orElse(null);

                if (statisticsAgentMch == null) {
                    statisticsAgentMch = new StatisticsAgentMch();
                    statisticsAgentMch.setAgentNo(mchAgentNo);
                    statisticsAgentMch.setMchNo(payOrder.getMchNo());
                    statisticsAgentMch.setStatisticsDate(countDay);

                    if (payOrder.getState() != PayOrder.STATE_REFUND) {
                        statisticsAgentMch.setOrderSuccessCount(1);
                    } else {
                        statisticsAgentMch.setOrderSuccessCount(0);
                    }
                    statisticsAgentMch.setTotalSuccessAmount(payOrder.getAmount());
                    statisticsAgentMch.setTotalAgentIncome(payOrder.getAgentFeeAmount());

                    statisticsAgentMchList.add(statisticsAgentMch);
                } else {
                    if (payOrder.getState() != PayOrder.STATE_REFUND) {
                        statisticsAgentMch.setOrderSuccessCount(statisticsAgentMch.getOrderSuccessCount() + 1);
                    }
                    statisticsAgentMch.setTotalSuccessAmount(statisticsAgentMch.getTotalSuccessAmount() + payOrder.getAmount());
                    statisticsAgentMch.setTotalAgentIncome(payOrder.getAgentFeeAmount() + statisticsAgentMch.getTotalAgentIncome());
                }
            }

            //代理-通道  代理商号-通道号-值
            if (StringUtils.isNotBlank(passageAgentNo)) {
                StatisticsAgentPassage statisticsAgentPassage = statisticsAgentPassageList.stream()
                        .filter(item -> item.getAgentNo().equals(passageAgentNo) && DateUtil.between(item.getStatisticsDate(), countDay, DateUnit.DAY) == 0 && item.getPayPassageId().longValue() == payOrder.getPassageId().longValue())
                        .findFirst().orElse(null);

                if (statisticsAgentPassage == null) {
                    statisticsAgentPassage = new StatisticsAgentPassage();
                    statisticsAgentPassage.setAgentNo(passageAgentNo);
                    statisticsAgentPassage.setPayPassageId(payOrder.getPassageId());
                    statisticsAgentPassage.setStatisticsDate(countDay);

                    if (payOrder.getState() != PayOrder.STATE_REFUND) {
                        statisticsAgentPassage.setOrderSuccessCount(1);
                    } else {
                        statisticsAgentPassage.setOrderSuccessCount(0);
                    }
                    statisticsAgentPassage.setTotalSuccessAmount(payOrder.getAmount());
                    statisticsAgentPassage.setTotalAgentIncome(payOrder.getAgentPassageFee());

                    statisticsAgentPassageList.add(statisticsAgentPassage);
                } else {
                    if (payOrder.getState() != PayOrder.STATE_REFUND) {
                        statisticsAgentPassage.setOrderSuccessCount(statisticsAgentPassage.getOrderSuccessCount() + 1);
                    }
                    statisticsAgentPassage.setTotalSuccessAmount(statisticsAgentPassage.getTotalSuccessAmount() + payOrder.getAmount());
                    statisticsAgentPassage.setTotalAgentIncome(statisticsAgentPassage.getTotalAgentIncome() + payOrder.getAgentPassageFee());
                }
            }
        }
        SaveSuccessData(statisticsPlatMap, statisticsMchList, statisticsAgentList, statisticsAgentMchList, statisticsAgentPassageList, statisticsPassageList, statisticsProductList, statisticsMchProductList);
        log.info("===订单成交统计完成, " + payOrderList.size());
    }

    public void SaveSuccessData(Map<Date, StatisticsPlat> statisticsPlatMap, List<StatisticsMch> statisticsMchList, List<StatisticsAgent> statisticsAgentList, List<StatisticsAgentMch> statisticsAgentMchList, List<StatisticsAgentPassage> statisticsAgentPassageList, List<StatisticsPassage> statisticsPassageList, List<StatisticsProduct> statisticsProductList, List<StatisticsMchProduct> statisticsMchProductList) {

        //平台
        statisticsPlatMap.forEach((date, newValue) -> {
            // 处理键值对
            StatisticsPlat statisticsPlat = statisticsPlatService.getById(date);
            if (statisticsPlat == null) {
                statisticsPlatService.save(newValue);
            } else {
                int orderTotalSuccess = statisticsPlat.getOrderSuccessCount() + newValue.getOrderSuccessCount();
                statisticsPlat.setOrderSuccessCount(orderTotalSuccess);
                statisticsPlat.setTotalSuccessAmount(statisticsPlat.getTotalSuccessAmount() + newValue.getTotalSuccessAmount());
                statisticsPlat.setPlatTotalIncome(newValue.getPlatTotalIncome() + statisticsPlat.getPlatTotalIncome());

                statisticsPlatService.updateById(statisticsPlat);
            }
        });


        //商户
        statisticsMchList.forEach((newValue) -> {
            StatisticsMch statisticsMch = statisticsMchService.getOne(StatisticsMch.gw().eq(StatisticsMch::getMchNo, newValue.getMchNo()).eq(StatisticsMch::getStatisticsDate, newValue.getStatisticsDate()));

            if (statisticsMch == null) {
                statisticsMchService.save(newValue);
            } else {
                statisticsMch.setOrderSuccessCount(statisticsMch.getOrderSuccessCount() + newValue.getOrderSuccessCount());
                statisticsMch.setTotalSuccessAmount(statisticsMch.getTotalSuccessAmount() + newValue.getTotalSuccessAmount());
                statisticsMch.setTotalMchCost(statisticsMch.getTotalMchCost() + newValue.getTotalMchCost());
                statisticsMchService.updateById(statisticsMch);
            }
        });

        //代理
        statisticsAgentList.forEach((newValue) -> {
            StatisticsAgent statisticsAgent = statisticsAgentService.getOne(StatisticsAgent.gw().eq(StatisticsAgent::getAgentNo, newValue.getAgentNo()).eq(StatisticsAgent::getStatisticsDate, newValue.getStatisticsDate()));
            if (statisticsAgent == null) {
                statisticsAgentService.save(newValue);
            } else {
                statisticsAgent.setOrderSuccessCount(statisticsAgent.getOrderSuccessCount() + newValue.getOrderSuccessCount());
                statisticsAgent.setTotalSuccessAmount(statisticsAgent.getTotalSuccessAmount() + newValue.getTotalSuccessAmount());
                statisticsAgent.setTotalAgentIncome(statisticsAgent.getTotalAgentIncome() + newValue.getTotalAgentIncome());
                statisticsAgentService.updateById(statisticsAgent);
            }
        });


        //代理-商户  代理号-商户号-值
        statisticsAgentMchList.forEach((newValue) -> {
            StatisticsAgentMch statisticsAgentMch = statisticsAgentMchService.getOne(StatisticsAgentMch.gw().eq(StatisticsAgentMch::getAgentNo, newValue.getAgentNo()).eq(StatisticsAgentMch::getMchNo, newValue.getMchNo()).eq(StatisticsAgentMch::getStatisticsDate, newValue.getStatisticsDate()));
            if (statisticsAgentMch == null) {
                statisticsAgentMchService.save(newValue);
            } else {
                statisticsAgentMch.setOrderSuccessCount(statisticsAgentMch.getOrderSuccessCount() + newValue.getOrderSuccessCount());
                statisticsAgentMch.setTotalSuccessAmount(statisticsAgentMch.getTotalSuccessAmount() + newValue.getTotalSuccessAmount());
                statisticsAgentMch.setTotalAgentIncome(statisticsAgentMch.getTotalAgentIncome() + newValue.getTotalAgentIncome());
                statisticsAgentMchService.updateById(statisticsAgentMch);
            }
        });


        //代理-通道
        statisticsAgentPassageList.forEach((newValue) -> {
            StatisticsAgentPassage statisticsAgentPassage = statisticsAgentPassageService.getOne(StatisticsAgentPassage.gw().eq(StatisticsAgentPassage::getAgentNo, newValue.getAgentNo()).eq(StatisticsAgentPassage::getPayPassageId, newValue.getPayPassageId()).eq(StatisticsAgentPassage::getStatisticsDate, newValue.getStatisticsDate()));
            if (statisticsAgentPassage == null) {
                statisticsAgentPassageService.save(newValue);
            } else {
                statisticsAgentPassage.setOrderSuccessCount(statisticsAgentPassage.getOrderSuccessCount() + newValue.getOrderSuccessCount());
                statisticsAgentPassage.setTotalSuccessAmount(statisticsAgentPassage.getTotalSuccessAmount() + newValue.getTotalSuccessAmount());
                statisticsAgentPassage.setTotalAgentIncome(statisticsAgentPassage.getTotalAgentIncome() + newValue.getTotalAgentIncome());
                statisticsAgentPassageService.updateById(statisticsAgentPassage);
            }
        });

        //通道
        statisticsPassageList.forEach((newValue) -> {
            StatisticsPassage statisticsPassage = statisticsPassageService.getOne(StatisticsPassage.gw().eq(StatisticsPassage::getPayPassageId, newValue.getPayPassageId()).eq(StatisticsPassage::getStatisticsDate, newValue.getStatisticsDate()));
            if (statisticsPassage == null) {
                statisticsPassageService.save(newValue);
            } else {
                statisticsPassage.setOrderSuccessCount(statisticsPassage.getOrderSuccessCount() + newValue.getOrderSuccessCount());
                statisticsPassage.setTotalSuccessAmount(statisticsPassage.getTotalSuccessAmount() + newValue.getTotalSuccessAmount());
                statisticsPassage.setTotalPassageCost(statisticsPassage.getTotalPassageCost() + newValue.getTotalPassageCost());
                statisticsPassageService.updateById(statisticsPassage);
            }
        });


        //产品
        statisticsProductList.forEach((newValue) -> {
            StatisticsProduct statisticsProduct = statisticsProductService.getOne(StatisticsProduct.gw().eq(StatisticsProduct::getProductId, newValue.getProductId()).eq(StatisticsProduct::getStatisticsDate, newValue.getStatisticsDate()));
            if (statisticsProduct == null) {
                statisticsProductService.save(newValue);
            } else {
                statisticsProduct.setOrderSuccessCount(statisticsProduct.getOrderSuccessCount() + newValue.getOrderSuccessCount());
                statisticsProduct.setTotalSuccessAmount(statisticsProduct.getTotalSuccessAmount() + newValue.getTotalSuccessAmount());
                statisticsProductService.updateById(statisticsProduct);
            }
        });


        //产品-商户
        statisticsMchProductList.forEach((newValue) -> {
            StatisticsMchProduct statisticsMchProduct = statisticsMchProductService.getOne(StatisticsMchProduct.gw().eq(StatisticsMchProduct::getProductId, newValue.getProductId()).eq(StatisticsMchProduct::getStatisticsDate, newValue.getStatisticsDate()).eq(StatisticsMchProduct::getMchNo, newValue.getMchNo()));
            if (statisticsMchProduct == null) {
                statisticsMchProduct = newValue;
                statisticsMchProductService.save(statisticsMchProduct);
            } else {
                statisticsMchProduct.setOrderSuccessCount(statisticsMchProduct.getOrderSuccessCount() + newValue.getOrderSuccessCount());
                statisticsMchProduct.setTotalSuccessAmount(statisticsMchProduct.getTotalSuccessAmount() + newValue.getTotalSuccessAmount());
                statisticsMchProduct.setTotalCost(statisticsMchProduct.getTotalCost() + newValue.getTotalCost());
                statisticsMchProductService.updateById(statisticsMchProduct);
            }
        });
    }

    /**
     * 计算平台利润
     *
     * @param payOrder
     * @return
     */
    private Long CalPlatIncome(PayOrder payOrder) {
        return payOrder.getMchFeeAmount() - payOrder.getPassageFeeAmount() - payOrder.getAgentFeeAmount() - payOrder.getAgentPassageFee();
    }
}