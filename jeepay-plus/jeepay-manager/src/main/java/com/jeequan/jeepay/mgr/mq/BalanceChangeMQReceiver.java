package com.jeequan.jeepay.mgr.mq;

import cn.hutool.json.JSONUtil;
import com.jeequan.jeepay.components.mq.model.BalanceChangeMQ;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单统计异步MQ接收
 */
@Slf4j
@Component
public class BalanceChangeMQReceiver implements BalanceChangeMQ.IMQReceiver {

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


    @Override
    public void receive(BalanceChangeMQ.MsgPayload payload) {
        try {
            //此处只处理流水、余额类的任务,只统计成功的订单
            PayOrder payOrder = payload.getPayOrder();
            if (payOrder.getState() == PayOrder.STATE_SUCCESS) {
                PushDataToCache(payOrder);
            } else if (payOrder.getState() == PayOrder.STATE_REFUND) {
                payOrder.setAmount(payOrder.getAmount() * -1);
                payOrder.setMchFeeAmount(payOrder.getMchFeeAmount() * -1);
                payOrder.setPassageFeeAmount(payOrder.getPassageFeeAmount() * -1);
                payOrder.setAgentFeeAmount(payOrder.getAgentFeeAmount() * -1);
                payOrder.setAgentPassageFee(payOrder.getAgentPassageFee() * -1);
//                PushDataToCache(payOrder);
                ProcessRedoOrder(payOrder);
                log.info("BalanceChangeMQReceiver-[{}]-测试冲正订单资金冲正 {}", payOrder.getPayOrderId(), JSONUtil.toJsonStr(payOrder));
            } else {
                log.error("BalanceChangeMQReceiver-[{}]-待统计流水订单状态错误 {}", payOrder.getPayOrderId(), JSONUtil.toJsonStr(payOrder));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 存入缓存
     *
     * @param payOrder
     */
    public void PushDataToCache(PayOrder payOrder) {
        RedisUtil.addToQueue(CS.CHANGE_BALANCE_REDIS_SUFFIX, payOrder);
    }

    /**
     * 这种订单单条处理
     *
     * @param payOrder
     */
    private void ProcessRedoOrder(PayOrder payOrder) {
        //商户资金流水、余额
        String mchNo = payOrder.getMchNo();
        MchInfo mchInfo = mchInfoService.queryMchInfo(mchNo);

        MchHistory mchHistory = new MchHistory();
        mchHistory.setMchNo(payOrder.getMchNo());
        mchHistory.setMchName(payOrder.getMchName());

        //是否有代理
        String agentNo = payOrder.getAgentNo();
        AgentAccountInfo agentMchInfo = new AgentAccountInfo();
        agentMchInfo.setAgentName("");
        agentMchInfo.setAgentNo(agentNo);
        if (StringUtils.isNotEmpty(agentNo)) {
            agentMchInfo = agentAccountInfoService.queryAgentInfo(agentNo);
        }

        Long mchChangAmount = payOrder.getAmount() - payOrder.getMchFeeAmount();
        Long mchBeforeBalance = mchInfo.getBalance();
        Long mchAfterBalance = mchBeforeBalance + mchChangAmount;

        //计算并存储商户余额变动
        mchInfoService.updateBalance(mchNo, mchChangAmount);

        mchHistory.setAmount(mchChangAmount);
        mchHistory.setPayOrderAmount(payOrder.getAmount());
        mchHistory.setBeforeBalance(mchBeforeBalance);
        mchHistory.setAfterBalance(mchAfterBalance);
        mchHistory.setMchRateAmount(payOrder.getMchFeeAmount());
        mchHistory.setFundDirection(CS.FUND_DIRECTION_REDUCE);
        mchHistory.setBizType(CS.BIZ_TYPE_REDO);
        mchHistory.setPayOrderId(payOrder.getPayOrderId());
        mchHistory.setPassageOrderId(payOrder.getPassageOrderNo());
        mchHistory.setPlatIncome(CalPlatProfit(payOrder));
        mchHistory.setAgentIncome(payOrder.getAgentFeeAmount());
        mchHistory.setAgentNo(payOrder.getAgentNo());
        mchHistory.setAgentName(agentMchInfo.getAgentName());
        mchHistory.setAgentIncome(payOrder.getAgentFeeAmount());
        mchHistory.setMchOrderNo(payOrder.getMchOrderNo());
        mchHistoryService.save(mchHistory);

        //代理资金流水、余额
        //区分商户代理、通道代理
        String agentNoPassage = payOrder.getAgentNoPassage();
        //相同,合并
        if (StringUtils.isNotEmpty(agentNoPassage) && StringUtils.isNotEmpty(agentNo) && agentNoPassage.equals(agentNo)) {
            //计算并存储代理余额变动
            Long beforeBalance = agentMchInfo.getBalance();
            Long changeAgentAmount = payOrder.getAgentPassageFee() + payOrder.getAgentFeeAmount();
            Long afterBalance = beforeBalance + changeAgentAmount;

            agentAccountInfoService.updateBalance(agentNo, changeAgentAmount);

            AgentAccountHistory agentAccountHistory = new AgentAccountHistory();
            agentAccountHistory.setAgentNo(agentNo);
            agentAccountHistory.setAgentName(agentMchInfo.getAgentName());
            agentAccountHistory.setAmount(changeAgentAmount);
            agentAccountHistory.setBeforeBalance(beforeBalance);
            agentAccountHistory.setAfterBalance(afterBalance);
            agentAccountHistory.setFundDirection(CS.FUND_DIRECTION_REDUCE);
            agentAccountHistory.setBizType(CS.BIZ_TYPE_REDO);
            agentAccountHistory.setPayOrderId(payOrder.getPayOrderId());
            agentAccountHistory.setPayOrderAmount(payOrder.getAmount());

            agentAccountHistoryService.save(agentAccountHistory);
        } else {
            //商户代理
            if (StringUtils.isNotEmpty(agentNo)) {
                Long beforeBalance = agentMchInfo.getBalance();
                Long changeAgentAmount = payOrder.getAgentFeeAmount();
                Long afterBalance = beforeBalance + changeAgentAmount;
                agentAccountInfoService.updateBalance(agentNo, changeAgentAmount);

                AgentAccountHistory agentAccountHistory = new AgentAccountHistory();
                agentAccountHistory.setAgentNo(agentNo);
                agentAccountHistory.setAgentName(agentMchInfo.getAgentName());
                agentAccountHistory.setAmount(changeAgentAmount);
                agentAccountHistory.setBeforeBalance(beforeBalance);
                agentAccountHistory.setAfterBalance(afterBalance);
                agentAccountHistory.setFundDirection(CS.FUND_DIRECTION_REDUCE);
                agentAccountHistory.setBizType(CS.BIZ_TYPE_REDO);
                agentAccountHistory.setPayOrderId(payOrder.getPayOrderId());
                agentAccountHistory.setPayOrderAmount(payOrder.getAmount());

                agentAccountHistoryService.save(agentAccountHistory);
            }

            //通道代理
            if (StringUtils.isNotEmpty(agentNoPassage)) {
                AgentAccountInfo agentPassageInfo = agentAccountInfoService.queryAgentInfo(agentNoPassage);

                Long beforeBalance = agentPassageInfo.getBalance();
                Long changeAgentAmount = payOrder.getAgentPassageFee();
                Long afterBalance = beforeBalance + changeAgentAmount;

                agentAccountInfoService.updateBalance(agentNoPassage, changeAgentAmount);

                AgentAccountHistory agentAccountHistory = new AgentAccountHistory();
                agentAccountHistory.setAgentNo(agentNoPassage);
                agentAccountHistory.setAgentName(agentPassageInfo.getAgentName());
                agentAccountHistory.setAmount(changeAgentAmount);
                agentAccountHistory.setBeforeBalance(beforeBalance);
                agentAccountHistory.setAfterBalance(afterBalance);
                agentAccountHistory.setFundDirection(CS.FUND_DIRECTION_REDUCE);
                agentAccountHistory.setBizType(CS.BIZ_TYPE_REDO);
                agentAccountHistory.setPayOrderId(payOrder.getPayOrderId());
                agentAccountHistory.setPayOrderAmount(payOrder.getAmount());

                agentAccountHistoryService.save(agentAccountHistory);
            }
        }

        //通道余额、授信
        //通道入账余额= 订单金额-通道成本-通道代理成本
        Long passageChangeAmount = payOrder.getAmount() - payOrder.getPassageFeeAmount() - payOrder.getAgentPassageFee();
        PayPassage payPassage = payPassageService.queryPassageInfo(payOrder.getPassageId());

        Long beforeBalance = payPassage.getBalance();
        Long afterBalance = payPassage.getBalance() + passageChangeAmount;

        //保存流水入通道流水记录
        //插入通道流水记录
        PassageTransactionHistory passageTransactionHistory = new PassageTransactionHistory();
        passageTransactionHistory.setAmount(passageChangeAmount);
        passageTransactionHistory.setPayPassageId(payPassage.getPayPassageId());
        passageTransactionHistory.setPayPassageName(payPassage.getPayPassageName());
        passageTransactionHistory.setPayOrderId(payOrder.getPayOrderId());
        passageTransactionHistory.setBeforeBalance(beforeBalance);
        passageTransactionHistory.setAfterBalance(afterBalance);
        passageTransactionHistory.setCreatedUid(0L);
        passageTransactionHistory.setCreatedLoginName(payOrder.getForceChangeLoginName());
        passageTransactionHistory.setFundDirection(passageChangeAmount >= 0 ? CS.FUND_DIRECTION_INCREASE : CS.FUND_DIRECTION_REDUCE);
        passageTransactionHistory.setBizType(PassageTransactionHistory.BIZ_TYPE_ORDER);

        passageTransactionHistory.setRemark("测试冲正");
        passageTransactionHistoryService.save(passageTransactionHistory);

        //通道余额变动
        payPassageService.updateBalance(payOrder.getPassageId(), passageChangeAmount);
        //额度限制打开的情况
        if (payPassage.getQuotaLimitState() == CS.YES) {
            payPassageService.updateQuota(payOrder.getPassageId(), passageChangeAmount);
        }
    }

    private Long CalPlatProfit(PayOrder payOrder) {
        return payOrder.getMchFeeAmount() - payOrder.getPassageFeeAmount() - payOrder.getAgentFeeAmount() - payOrder.getAgentPassageFee();
    }

}