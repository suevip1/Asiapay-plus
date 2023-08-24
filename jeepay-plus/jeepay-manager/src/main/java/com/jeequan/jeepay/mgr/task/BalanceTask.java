package com.jeequan.jeepay.mgr.task;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BalanceTask {

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private AgentAccountInfoService agentAccountInfoService;

    @Autowired
    private PayPassageService payPassageService;

    @Autowired
    private MchHistoryService mchHistoryService;

    @Autowired
    private AgentAccountHistoryService agentAccountHistoryService;

    @Autowired
    private PassageTransactionHistoryService passageTransactionHistoryService;


    /**
     * 每5秒监控redis中是否有新的需要处理的余额操作
     */
    @Scheduled(fixedRate = 5000) // 每5秒执行一次 5000
    public void start() {
        List<PayOrder> list = PopDataListFromCache();
        if (list.size() != 0) {
            Map<String, Long> mchBalanceChange = new HashMap<>();
            Map<String, Long> agentBalanceChange = new HashMap<>();
            Map<Long, Long> payPassageBalanceChange = new HashMap<>();
            Map<Long, Long> payPassageQuotaChange = new HashMap<>();

            //通道余额更新-检查授信
            for (int i = 0; i < list.size(); i++) {
                PayOrder payOrder = list.get(i);
                String mchNo = payOrder.getMchNo();
                MchInfo mchInfo = mchInfoService.queryMchInfo(mchNo);

                if (payOrder.getState() == PayOrder.STATE_SUCCESS) {
                    //正常回调订单
                    //商户资金流水、余额
                    Long mchChangAmount = payOrder.getAmount() - payOrder.getMchFeeAmount();
                    Long mchBeforeBalance = mchInfo.getBalance();

                    //计算并存储商户余额变动 是否有余额变动缓存
                    if (mchBalanceChange.containsKey(mchNo)) {
                        //加上之前变动的金额
                        mchBeforeBalance += mchBalanceChange.get(mchNo);

                        Long tempMchAmount = mchBalanceChange.get(mchNo) + mchChangAmount;
                        mchBalanceChange.replace(mchNo, tempMchAmount);
                    } else {
                        mchBalanceChange.put(mchNo, mchChangAmount);
                    }
                    Long mchAfterBalance = mchBeforeBalance + mchChangAmount;
                    AddMchOrderSuccessHistory(payOrder, mchInfo, mchBeforeBalance, mchAfterBalance, mchChangAmount);

                    //代理资金流水、余额
                    //区分商户代理、通道代理

                    String agentNoPassage = payOrder.getAgentNoPassage();
                    String agentNo = payOrder.getAgentNo();
                    //相同,合并
                    if (StringUtils.isNotEmpty(agentNoPassage) && StringUtils.isNotEmpty(agentNo) && agentNoPassage.equals(agentNo)) {
                        AgentAccountInfo agentMchInfo = agentAccountInfoService.queryAgentInfo(agentNo);

                        Long changeAgentAmount = payOrder.getAgentPassageFee() + payOrder.getAgentFeeAmount();
                        Long agentBeforeBalance = agentMchInfo.getBalance();

                        //计算并存储代理余额变动
                        if (agentBalanceChange.containsKey(agentNo)) {
                            agentBeforeBalance += agentBalanceChange.get(agentNo);

                            Long tempAgentAmount = agentBalanceChange.get(agentNo) + changeAgentAmount;
                            agentBalanceChange.replace(agentNo, tempAgentAmount);
                        } else {
                            agentBalanceChange.put(agentNo, changeAgentAmount);
                        }
                        Long agentAfterBalance = agentBeforeBalance + changeAgentAmount;
                        AddAgentOrderSuccessHistory(payOrder, agentMchInfo, agentBeforeBalance, agentAfterBalance, changeAgentAmount);
                    } else {
                        //商户代理
                        if (StringUtils.isNotEmpty(agentNo)) {
                            AgentAccountInfo agentMchInfo = agentAccountInfoService.queryAgentInfo(agentNo);

                            Long agentBeforeBalance = agentMchInfo.getBalance();
                            Long changeAgentAmount = payOrder.getAgentFeeAmount();

                            //计算并存储代理余额变动
                            if (agentBalanceChange.containsKey(agentNo)) {
                                agentBeforeBalance += agentBalanceChange.get(agentNo);

                                Long tempAgentAmount = agentBalanceChange.get(agentNo) + changeAgentAmount;
                                agentBalanceChange.replace(agentNo, tempAgentAmount);
                            } else {
                                agentBalanceChange.put(agentNo, changeAgentAmount);
                            }
                            Long agentAfterBalance = agentBeforeBalance + changeAgentAmount;
                            AddAgentOrderSuccessHistory(payOrder, agentMchInfo, agentBeforeBalance, agentAfterBalance, changeAgentAmount);
                        }

                        //通道代理
                        if (StringUtils.isNotEmpty(agentNoPassage)) {
                            AgentAccountInfo agentPassageInfo = agentAccountInfoService.queryAgentInfo(agentNoPassage);
                            Long changeAgentAmount = payOrder.getAgentPassageFee();

                            Long agentBeforeBalance = agentPassageInfo.getBalance();

                            //计算并存储代理余额变动
                            if (agentBalanceChange.containsKey(agentNoPassage)) {
                                agentBeforeBalance += agentBalanceChange.get(agentNoPassage);
                                Long tempAgentAmount = agentBalanceChange.get(agentNoPassage) + changeAgentAmount;
                                agentBalanceChange.replace(agentNoPassage, tempAgentAmount);
                            } else {
                                agentBalanceChange.put(agentNoPassage, changeAgentAmount);
                            }
                            Long agentAfterBalance = agentBeforeBalance + changeAgentAmount;
                            AddAgentOrderSuccessHistory(payOrder, agentPassageInfo, agentBeforeBalance, agentAfterBalance, changeAgentAmount);
                        }
                    }

                    //通道余额、授信
                    //通道入账余额= 订单金额-通道成本-通道代理成本
                    Long passageChangeAmount = payOrder.getAmount() - payOrder.getPassageFeeAmount() - payOrder.getAgentPassageFee();
                    PayPassage payPassage = payPassageService.queryPassageInfo(payOrder.getPassageId());

                    //额度限制打开的情况
                    if (payPassage.getQuotaLimitState() == CS.YES) {
                        if (payPassageQuotaChange.containsKey(payOrder.getPassageId())) {
                            Long tempPassageAmount = payPassageQuotaChange.get(payOrder.getPassageId()) - passageChangeAmount;
                            payPassageQuotaChange.replace(payOrder.getPassageId(), tempPassageAmount);
                        } else {
                            payPassageQuotaChange.put(payOrder.getPassageId(), -passageChangeAmount);
                        }
                        //授信是否小于change
                        Long afterQuota = payPassage.getQuota() + payPassageQuotaChange.get(payOrder.getPassageId());
                        if (afterQuota.longValue() <= 0) {
                            payPassage.setState(CS.NO);
                            payPassageService.updatePassageInfo(payPassage);
                        }
                    }

                    //通道余额处理
                    Long passageBeforeBalance = payPassage.getBalance();

                    if (payPassageBalanceChange.containsKey(payOrder.getPassageId())) {
                        passageBeforeBalance += payPassageBalanceChange.get(payOrder.getPassageId());

                        Long tempPassageAmount = payPassageBalanceChange.get(payOrder.getPassageId()) + passageChangeAmount;
                        payPassageBalanceChange.replace(payOrder.getPassageId(), tempPassageAmount);
                    } else {
                        payPassageBalanceChange.put(payOrder.getPassageId(), passageChangeAmount);
                    }
                    Long passageAfterBalance = passageBeforeBalance + passageChangeAmount;
                    //保存流水入通道流水记录
                    //插入通道流水记录
                    AddPassageOrderSuccessHistory(payOrder, payPassage, passageBeforeBalance, passageAfterBalance, passageChangeAmount);
                } else {
                    log.error("BalanceTask 余额统计订单状态错误 订单号[{}] , {}", payOrder.getPayOrderId(), JSONObject.toJSONString(payOrder));
                }
            }
            //统一更新余额
            //商户
            mchBalanceChange.forEach((mchNo, newValue) -> {
                mchInfoService.updateBalance(mchNo, newValue);
            });
            //代理
            agentBalanceChange.forEach((agentNo, newValue) -> {
                agentAccountInfoService.updateBalance(agentNo, newValue);
            });
            //通道余额
            payPassageBalanceChange.forEach((payPassageId, newValue) -> {
                payPassageService.updateBalance(payPassageId, newValue);
            });
            //通道授信
            payPassageQuotaChange.forEach((payPassageId, newValue) -> {
                payPassageService.updateQuota(payPassageId, newValue);
            });
        }
    }

    /**
     * 这种订单单条处理
     *
     * @param payOrder
     */
//    private void ProcessRedoOrder(PayOrder payOrder) {
//        //商户资金流水、余额
//        String mchNo = payOrder.getMchNo();
//        MchInfo mchInfo = mchInfoService.queryMchInfo(mchNo);
//
//        MchHistory mchHistory = new MchHistory();
//        mchHistory.setMchNo(payOrder.getMchNo());
//        mchHistory.setMchName(payOrder.getMchName());
//
//        //是否有代理
//        String agentNo = payOrder.getAgentNo();
//        AgentAccountInfo agentMchInfo = new AgentAccountInfo();
//        agentMchInfo.setAgentName("");
//        agentMchInfo.setAgentNo(agentNo);
//        if (StringUtils.isNotEmpty(agentNo)) {
//            agentMchInfo = agentAccountInfoService.queryAgentInfo(agentNo);
//        }
//
//        Long mchChangAmount = payOrder.getAmount() - payOrder.getMchFeeAmount();
//        Long mchBeforeBalance = mchInfo.getBalance();
//        Long mchAfterBalance = mchBeforeBalance + mchChangAmount;
//
//        //计算并存储商户余额变动
//        mchInfoService.updateBalance(mchNo, mchChangAmount);
//
//        mchHistory.setAmount(mchChangAmount);
//        mchHistory.setPayOrderAmount(payOrder.getAmount());
//        mchHistory.setBeforeBalance(mchBeforeBalance);
//        mchHistory.setAfterBalance(mchAfterBalance);
//        mchHistory.setMchRateAmount(payOrder.getMchFeeAmount());
//        mchHistory.setFundDirection(CS.FUND_DIRECTION_REDUCE);
//        mchHistory.setBizType(CS.BIZ_TYPE_REDO);
//        mchHistory.setPayOrderId(payOrder.getPayOrderId());
//        mchHistory.setPassageOrderId(payOrder.getPassageOrderNo());
//        mchHistory.setPlatIncome(CalPlatProfit(payOrder));
//        mchHistory.setAgentIncome(payOrder.getAgentFeeAmount());
//        mchHistory.setAgentNo(payOrder.getAgentNo());
//        mchHistory.setAgentName(agentMchInfo.getAgentName());
//        mchHistory.setAgentIncome(payOrder.getAgentFeeAmount());
//        mchHistory.setMchOrderNo(payOrder.getMchOrderNo());
//        mchHistoryService.save(mchHistory);
//
//        //代理资金流水、余额
//        //区分商户代理、通道代理
//        String agentNoPassage = payOrder.getAgentNoPassage();
//        //相同,合并
//        if (StringUtils.isNotEmpty(agentNoPassage) && StringUtils.isNotEmpty(agentNo) && agentNoPassage.equals(agentNo)) {
//            //计算并存储代理余额变动
//            Long beforeBalance = agentMchInfo.getBalance();
//            Long changeAgentAmount = payOrder.getAgentPassageFee() + payOrder.getAgentFeeAmount();
//            Long afterBalance = beforeBalance + changeAgentAmount;
//
//            agentAccountInfoService.updateBalance(agentNo, changeAgentAmount);
//
//            AgentAccountHistory agentAccountHistory = new AgentAccountHistory();
//            agentAccountHistory.setAgentNo(agentNo);
//            agentAccountHistory.setAgentName(agentMchInfo.getAgentName());
//            agentAccountHistory.setAmount(changeAgentAmount);
//            agentAccountHistory.setBeforeBalance(beforeBalance);
//            agentAccountHistory.setAfterBalance(afterBalance);
//            agentAccountHistory.setFundDirection(CS.FUND_DIRECTION_REDUCE);
//            agentAccountHistory.setBizType(CS.BIZ_TYPE_REDO);
//            agentAccountHistory.setPayOrderId(payOrder.getPayOrderId());
//            agentAccountHistory.setPayOrderAmount(payOrder.getAmount());
//
//            agentAccountHistoryService.save(agentAccountHistory);
//        } else {
//            //商户代理
//            if (StringUtils.isNotEmpty(agentNo)) {
//                Long beforeBalance = agentMchInfo.getBalance();
//                Long changeAgentAmount = payOrder.getAgentFeeAmount();
//                Long afterBalance = beforeBalance + changeAgentAmount;
//                agentAccountInfoService.updateBalance(agentNo, changeAgentAmount);
//
//                AgentAccountHistory agentAccountHistory = new AgentAccountHistory();
//                agentAccountHistory.setAgentNo(agentNo);
//                agentAccountHistory.setAgentName(agentMchInfo.getAgentName());
//                agentAccountHistory.setAmount(changeAgentAmount);
//                agentAccountHistory.setBeforeBalance(beforeBalance);
//                agentAccountHistory.setAfterBalance(afterBalance);
//                agentAccountHistory.setFundDirection(CS.FUND_DIRECTION_REDUCE);
//                agentAccountHistory.setBizType(CS.BIZ_TYPE_REDO);
//                agentAccountHistory.setPayOrderId(payOrder.getPayOrderId());
//                agentAccountHistory.setPayOrderAmount(payOrder.getAmount());
//
//                agentAccountHistoryService.save(agentAccountHistory);
//            }
//
//            //通道代理
//            if (StringUtils.isNotEmpty(agentNoPassage)) {
//                AgentAccountInfo agentPassageInfo = agentAccountInfoService.queryAgentInfo(agentNoPassage);
//
//                Long beforeBalance = agentPassageInfo.getBalance();
//                Long changeAgentAmount = payOrder.getAgentPassageFee();
//                Long afterBalance = beforeBalance + changeAgentAmount;
//
//                agentAccountInfoService.updateBalance(agentNoPassage, changeAgentAmount);
//
//                AgentAccountHistory agentAccountHistory = new AgentAccountHistory();
//                agentAccountHistory.setAgentNo(agentNoPassage);
//                agentAccountHistory.setAgentName(agentPassageInfo.getAgentName());
//                agentAccountHistory.setAmount(changeAgentAmount);
//                agentAccountHistory.setBeforeBalance(beforeBalance);
//                agentAccountHistory.setAfterBalance(afterBalance);
//                agentAccountHistory.setFundDirection(CS.FUND_DIRECTION_REDUCE);
//                agentAccountHistory.setBizType(CS.BIZ_TYPE_REDO);
//                agentAccountHistory.setPayOrderId(payOrder.getPayOrderId());
//                agentAccountHistory.setPayOrderAmount(payOrder.getAmount());
//
//                agentAccountHistoryService.save(agentAccountHistory);
//            }
//        }
//
//        //通道余额、授信
//        //通道入账余额= 订单金额-通道成本-通道代理成本
//        Long passageChangeAmount = payOrder.getAmount() - payOrder.getPassageFeeAmount() - payOrder.getAgentPassageFee();
//        PayPassage payPassage = payPassageService.queryPassageInfo(payOrder.getPassageId());
//
//        Long beforeBalance = payPassage.getBalance();
//        Long afterBalance = payPassage.getBalance() + passageChangeAmount;
//
//        //保存流水入通道流水记录
//        //插入通道流水记录
//        PassageTransactionHistory passageTransactionHistory = new PassageTransactionHistory();
//        passageTransactionHistory.setAmount(passageChangeAmount);
//        passageTransactionHistory.setPayPassageId(payPassage.getPayPassageId());
//        passageTransactionHistory.setPayPassageName(payPassage.getPayPassageName());
//        passageTransactionHistory.setPayOrderId(payOrder.getPayOrderId());
//        passageTransactionHistory.setBeforeBalance(beforeBalance);
//        passageTransactionHistory.setAfterBalance(afterBalance);
//        passageTransactionHistory.setCreatedUid(0L);
//        passageTransactionHistory.setCreatedLoginName(payOrder.getForceChangeLoginName());
//        passageTransactionHistory.setFundDirection(passageChangeAmount >= 0 ? CS.FUND_DIRECTION_INCREASE : CS.FUND_DIRECTION_REDUCE);
//        passageTransactionHistory.setBizType(PassageTransactionHistory.BIZ_TYPE_ORDER);
//
//        passageTransactionHistory.setRemark("测试冲正");
//        passageTransactionHistoryService.save(passageTransactionHistory);
//
//        //通道余额变动
//        payPassageService.updateBalance(payOrder.getPassageId(), passageChangeAmount);
//
//        //额度限制打开的情况
//        if (payPassage.getQuotaLimitState() == CS.YES) {
//            payPassageService.updateQuota(payOrder.getPassageId(), passageChangeAmount);
//        }
//    }

    /**
     * 订单成功商户流水记录
     *
     * @param payOrder
     * @param mchInfo
     * @param mchBeforeBalance
     * @param mchAfterBalance
     * @param mchChangAmount
     */
    private void AddMchOrderSuccessHistory(PayOrder payOrder, MchInfo mchInfo, Long mchBeforeBalance, Long mchAfterBalance, Long mchChangAmount) {
        //是否有代理
        String agentNo = payOrder.getAgentNo();
        AgentAccountInfo agentMchInfo = new AgentAccountInfo();
        agentMchInfo.setAgentName("");
        agentMchInfo.setAgentNo(agentNo);
        if (StringUtils.isNotEmpty(agentNo)) {
            agentMchInfo = agentAccountInfoService.queryAgentInfo(agentNo);
        }

        MchHistory mchHistory = new MchHistory();
        mchHistory.setMchNo(mchInfo.getMchNo());
        mchHistory.setMchName(mchInfo.getMchName());

        mchHistory.setAmount(mchChangAmount);
        mchHistory.setPayOrderAmount(payOrder.getAmount());
        mchHistory.setBeforeBalance(mchBeforeBalance);
        mchHistory.setAfterBalance(mchAfterBalance);
        mchHistory.setMchRateAmount(payOrder.getMchFeeAmount());
        mchHistory.setFundDirection(CS.FUND_DIRECTION_INCREASE);
        mchHistory.setBizType(CS.BIZ_TYPE_PAY_OR_INCOME);
        mchHistory.setPayOrderId(payOrder.getPayOrderId());
        mchHistory.setPassageOrderId(payOrder.getPassageOrderNo());
        mchHistory.setPlatIncome(CalPlatProfit(payOrder));
        mchHistory.setAgentIncome(payOrder.getAgentFeeAmount());
        mchHistory.setAgentNo(payOrder.getAgentNo());
        mchHistory.setAgentName(agentMchInfo.getAgentName());
        mchHistory.setAgentIncome(payOrder.getAgentFeeAmount());
        mchHistory.setMchOrderNo(payOrder.getMchOrderNo());
        mchHistoryService.save(mchHistory);
    }

    /**
     * 增加一条代理记录
     *
     * @param payOrder
     * @param agentInfo
     * @param beforeBalance
     * @param afterBalance
     * @param changeAgentAmount
     */
    private void AddAgentOrderSuccessHistory(PayOrder payOrder, AgentAccountInfo agentInfo, Long beforeBalance, Long afterBalance, Long changeAgentAmount) {
        //代理资金流水、余额
        //区分商户代理、通道代理
        AgentAccountHistory agentAccountHistory = new AgentAccountHistory();
        agentAccountHistory.setAgentNo(agentInfo.getAgentNo());
        agentAccountHistory.setAgentName(agentInfo.getAgentName());
        agentAccountHistory.setAmount(changeAgentAmount);
        agentAccountHistory.setBeforeBalance(beforeBalance);
        agentAccountHistory.setAfterBalance(afterBalance);
        agentAccountHistory.setFundDirection(CS.FUND_DIRECTION_INCREASE);
        agentAccountHistory.setBizType(CS.BIZ_TYPE_PAY_OR_INCOME);
        agentAccountHistory.setPayOrderId(payOrder.getPayOrderId());
        agentAccountHistory.setPayOrderAmount(payOrder.getAmount());

        agentAccountHistoryService.save(agentAccountHistory);
    }

    private void AddPassageOrderSuccessHistory(PayOrder payOrder, PayPassage payPassage, Long beforeBalance, Long afterBalance, Long passageChangeAmount) {
        //通道资金流水
        PassageTransactionHistory passageTransactionHistory = new PassageTransactionHistory();
        passageTransactionHistory.setAmount(passageChangeAmount);
        passageTransactionHistory.setPayPassageId(payPassage.getPayPassageId());
        passageTransactionHistory.setPayPassageName(payPassage.getPayPassageName());
        passageTransactionHistory.setPayOrderId(payOrder.getPayOrderId());
        passageTransactionHistory.setCreatedUid(0L);
        passageTransactionHistory.setCreatedLoginName("");
        passageTransactionHistory.setBeforeBalance(beforeBalance);
        passageTransactionHistory.setAfterBalance(afterBalance);
        passageTransactionHistory.setFundDirection(passageChangeAmount >= 0 ? CS.FUND_DIRECTION_INCREASE : CS.FUND_DIRECTION_REDUCE);
        passageTransactionHistory.setBizType(PassageTransactionHistory.BIZ_TYPE_ORDER);
        passageTransactionHistory.setRemark("");

        passageTransactionHistoryService.save(passageTransactionHistory);
    }

    /**
     * 获取入库的订单
     *
     * @return
     */
    public List<PayOrder> PopDataListFromCache() {

        List<PayOrder> list = new ArrayList<>();
        Long cacheSize = RedisUtil.getQueueLength(CS.CHANGE_BALANCE_REDIS_SUFFIX);
        if (cacheSize.intValue() == 0) {
            return list;
        }
        log.info("读取入库资金流水操作缓存,当前数量{}", cacheSize);
        int count = cacheSize.intValue();

        for (int index = 0; index < count; index++) {
            list.add(RedisUtil.removeFromQueue(CS.CHANGE_BALANCE_REDIS_SUFFIX, PayOrder.class));
        }
        log.info("获取入库资金流水操作缓存-{},剩余{}", count, RedisUtil.getQueueLength(CS.CHANGE_BALANCE_REDIS_SUFFIX));
        return list;
    }

    private Long CalPlatProfit(PayOrder payOrder) {
        return payOrder.getMchFeeAmount() - payOrder.getPassageFeeAmount() - payOrder.getAgentFeeAmount() - payOrder.getAgentPassageFee();
    }
}
