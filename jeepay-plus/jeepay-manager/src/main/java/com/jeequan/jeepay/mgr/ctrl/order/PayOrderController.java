/*
 * Copyright (c) 2021-2031
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.mgr.ctrl.order;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.components.mq.model.BalanceChangeMQ;
import com.jeequan.jeepay.components.mq.model.PayOrderForceSuccessMQ;
import com.jeequan.jeepay.components.mq.model.StatisticsOrderMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.aop.LimitRequest;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * 支付订单类
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021-06-07 07:15
 */
@Slf4j
@RestController
@RequestMapping("/api/payOrder")
public class PayOrderController extends CommonCtrl {
    @Autowired
    private PayOrderService payOrderService;

    @Autowired
    private PayPassageService payPassageService;
    @Autowired
    private MchInfoService mchInfoService;
    @Autowired
    private AgentAccountInfoService agentAccountInfoService;

    @Autowired
    private MchHistoryService mchHistoryService;
    @Autowired
    private AgentAccountHistoryService agentAccountHistoryService;
    @Autowired
    private PassageTransactionHistoryService passageTransactionHistoryService;

    @Autowired
    private IMQSender mqSender;

    @Autowired
    private SysUserAuthService sysUserAuthService;

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:15
     * @describe: 订单信息列表
     */
    @PreAuthorize("hasAuthority('ENT_ORDER_LIST')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        try {
            PayOrder payOrder = getObject(PayOrder.class);
            JSONObject paramJSON = getReqParamJSON();
            LambdaQueryWrapper<PayOrder> wrapper = PayOrder.gw();
            //商户号、名 订单号、商户订单号、产品、通道、状态、金额、是否手动补单、回调状态、创建时间
            wrapper.select(PayOrder::getPayOrderId, PayOrder::getMchOrderNo, PayOrder::getMchNo, PayOrder::getMchName, PayOrder::getProductId, PayOrder::getProductName, PayOrder::getPassageId, PayOrder::getAmount, PayOrder::getState, PayOrder::getForceChangeState
                    , PayOrder::getCreatedAt, PayOrder::getSuccessTime, PayOrder::getNotifyState);
            IPage<PayOrder> pages = payOrderService.listByPage(getIPage(), payOrder, paramJSON, wrapper);
            List<PayOrder> records = pages.getRecords();
            Map<Long, PayPassage> payPassageMap = payPassageService.getPayPassageMap();
            for (int i = 0; i < records.size(); i++) {
                records.get(i).addExt("passageName", payPassageMap.get(records.get(i).getPassageId()).getPayPassageName());
            }
            pages.setRecords(records);
            return ApiRes.page(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:15
     * @describe: 支付订单信息
     */
    @PreAuthorize("hasAuthority('ENT_PAY_ORDER_VIEW')")
    @RequestMapping(value = "/{payOrderId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("payOrderId") String payOrderId) {
        PayOrder payOrder = payOrderService.getById(payOrderId);
        if (payOrder == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        Map<Long, PayPassage> payPassageMap = payPassageService.getPayPassageMap();
        payOrder.addExt("passageName", payPassageMap.get(payOrder.getPassageId()).getPayPassageName());
        return ApiRes.ok(payOrder);
    }

    /**
     * 强制补单
     *
     * @param payOrderId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasAuthority('ENT_PAY_ORDER_EDIT')")
    @MethodLog(remark = "强制补单")
    @RequestMapping(value = "/{payOrderId}/forcePayOrderSuccess", method = RequestMethod.GET)
    @LimitRequest
    public ApiRes forcePayOrderSuccess(@PathVariable("payOrderId") String payOrderId) {
        //检查当前用户是否绑定谷歌
        String account = getCurrentUser().getSysUser().getLoginUsername();
        SysUserAuth sysUserAuth = sysUserAuthService.selectByLogin(account, CS.AUTH_TYPE.LOGIN_USER_NAME, CS.SYS_TYPE.MGR);
        if (sysUserAuth == null || sysUserAuth.getGoogleAuthStatus() == CS.NO) {
            return ApiRes.fail(ApiCodeEnum.NO_GOOGLE_ERROR);
        }

        PayOrder payOrder = payOrderService.getById(payOrderId);
        if (payOrder == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        //<!-- 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-测试冲正, 6-订单关闭 ,7-出码失败 --> 1 3 6
        if (payOrder.getState() == PayOrder.STATE_SUCCESS || payOrder.getState() == PayOrder.STATE_ERROR || payOrder.getState() == PayOrder.STATE_CANCEL) {
            return ApiRes.fail(ApiCodeEnum.ORDER_STATE_ERROR);
        }

        String loginUsername = getCurrentUser().getSysUser().getLoginUsername();
        Date successTime = new Date();
        //强制更新
        boolean isSuccess = payOrderService.updateOrderSuccessForce(payOrder.getPayOrderId(), loginUsername, payOrder.getState(), successTime);
        if (isSuccess) {
            PayOrder payOrderCopy = new PayOrder();
            BeanUtils.copyProperties(payOrder, payOrderCopy);
            payOrderCopy.setState(PayOrder.STATE_SUCCESS);
            payOrderCopy.setSuccessTime(successTime);
            //订单成功统计通知，如果跨天了，则不发进统计,只更新余额
            mqSender.send(StatisticsOrderMQ.build(payOrder.getPayOrderId(), payOrderCopy));
            //发回调
            mqSender.send(PayOrderForceSuccessMQ.build(payOrderCopy));
            //余额更新mq
            mqSender.send(BalanceChangeMQ.build(payOrderCopy));
        } else {
            throw new BizException("订单状态异常,操作失败！");
        }
        return ApiRes.ok(payOrder);
    }

    /**
     * 强制补单
     *
     * @param payOrderId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasAuthority('ENT_PAY_ORDER_EDIT')")
    @MethodLog(remark = "订单测试冲正")
    @RequestMapping(value = "/{payOrderId}/forcePayOrderRedo", method = RequestMethod.GET)
    @LimitRequest
    public ApiRes forcePayOrderRedo(@PathVariable("payOrderId") String payOrderId) {
        //检查当前用户是否绑定谷歌
        String account = getCurrentUser().getSysUser().getLoginUsername();
        SysUserAuth sysUserAuth = sysUserAuthService.selectByLogin(account, CS.AUTH_TYPE.LOGIN_USER_NAME, CS.SYS_TYPE.MGR);
        if (sysUserAuth == null || sysUserAuth.getGoogleAuthStatus() == CS.NO) {
            return ApiRes.fail(ApiCodeEnum.NO_GOOGLE_ERROR);
        }

        PayOrder payOrder = payOrderService.getById(payOrderId);
        if (payOrder == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        //<!-- 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-测试冲正, 6-订单关闭 ,7-出码失败 --> 1 3 6
        if (payOrder.getState() != PayOrder.STATE_SUCCESS) {
            return ApiRes.fail(ApiCodeEnum.ORDER_STATE_ERROR);
        }

        //测试冲正
        boolean isSuccess = payOrderService.updateOrderSuccessToRedo(payOrder.getPayOrderId());
        if (isSuccess) {
            PayOrder payOrderCopy = new PayOrder();
            BeanUtils.copyProperties(payOrder, payOrderCopy);
            payOrderCopy.setState(PayOrder.STATE_REFUND);
            //金额改负数？
            //订单成功统计通知
            mqSender.send(StatisticsOrderMQ.build(payOrder.getPayOrderId(), payOrderCopy));
            //余额更新mq
            mqSender.send(BalanceChangeMQ.build(payOrderCopy));
        } else {
            throw new BizException("订单状态异常,操作失败！");
        }
        return ApiRes.ok(payOrder);
    }


    /**
     * 强制补单
     *
     * @param payOrderId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasAuthority('ENT_PAY_ORDER_EDIT')")
    @MethodLog(remark = "订单调额入账")
    @RequestMapping(value = "/{payOrderId}/changePayOrder/{changeAmount}", method = RequestMethod.GET)
    @LimitRequest
    public ApiRes changePayOrder(@PathVariable("payOrderId") String payOrderId, @PathVariable("changeAmount") Long changeAmount) {
        PayOrder payOrder = payOrderService.getById(payOrderId);

        if (payOrder == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        //<!-- 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-测试冲正, 6-订单关闭 ,7-出码失败 --> 1 3 6
        if (!(payOrder.getState() == PayOrder.STATE_ING || payOrder.getState() == PayOrder.STATE_FAIL || payOrder.getState() == PayOrder.STATE_CLOSED)) {
            return ApiRes.fail(ApiCodeEnum.ORDER_STATE_ERROR);
        }

        if (changeAmount == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }

        String loginUsername = getCurrentUser().getSysUser().getLoginUsername();

        String remarkStr = "调额入账订单，订单号[" + payOrderId + "] 原订单金额:" + AmountUtil.convertCent2Dollar(payOrder.getAmount()) + "元，入账订单金额:" + AmountUtil.convertCent2Dollar(changeAmount) + "元";

        Date updateTime = new Date();
        //更新订单状态、更新时间  不改成功？
        boolean isSuccess = payOrderService.updateOrderChange(payOrder.getPayOrderId(), loginUsername, payOrder.getState(), updateTime);
        if (isSuccess) {
            //入账-通道-商户-代理-并插入记录
            //入账重新计算费率
            payOrder.setAmount(changeAmount);
            payOrder.setState(PayOrder.STATE_CHANGE);
            payOrder.setUpdatedAt(updateTime);
            payOrder.setForceChangeLoginName(loginUsername);

            PayOrder payOrderNew = reCalPayOrder(payOrder);
            changeOrderRecord(payOrderNew, getCurrentUser().getSysUser(), remarkStr);
        } else {
            throw new BizException("订单状态异常,操作失败！");
        }
        return ApiRes.ok();
    }

    private void changeOrderRecord(PayOrder payOrder, SysUser sysUser, String remarkStr) {
        MchInfo mchInfo = mchInfoService.queryMchInfo(payOrder.getMchNo());

        //商户资金流水、余额
        Long mchChangAmount = payOrder.getAmount() - payOrder.getMchFeeAmount();
        Long mchBeforeBalance = mchInfo.getBalance();

        Long mchAfterBalance = mchBeforeBalance + mchChangAmount;
        mchInfoService.updateBalance(mchInfo.getMchNo(), mchChangAmount);
        AddMchOrderSuccessHistory(payOrder, mchInfo, mchBeforeBalance, mchAfterBalance, mchChangAmount, sysUser, remarkStr);


        //代理资金流水、余额
        //区分商户代理、通道代理

        String agentNoPassage = payOrder.getAgentNoPassage();
        String agentNo = payOrder.getAgentNo();
        //相同,合并
        if (StringUtils.isNotEmpty(agentNoPassage) && StringUtils.isNotEmpty(agentNo) && agentNoPassage.equals(agentNo)) {
            AgentAccountInfo agentMchInfo = agentAccountInfoService.queryAgentInfo(agentNo);

            Long changeAgentAmount = payOrder.getAgentPassageFee() + payOrder.getAgentFeeAmount();
            Long agentBeforeBalance = agentMchInfo.getBalance();

            Long agentAfterBalance = agentBeforeBalance + changeAgentAmount;
            AddAgentOrderSuccessHistory(payOrder, agentMchInfo, agentBeforeBalance, agentAfterBalance, changeAgentAmount, sysUser, remarkStr);

        } else {
            //商户代理
            if (StringUtils.isNotEmpty(agentNo)) {
                AgentAccountInfo agentMchInfo = agentAccountInfoService.queryAgentInfo(agentNo);

                Long agentBeforeBalance = agentMchInfo.getBalance();
                Long changeAgentAmount = payOrder.getAgentFeeAmount();

                Long agentAfterBalance = agentBeforeBalance + changeAgentAmount;
                AddAgentOrderSuccessHistory(payOrder, agentMchInfo, agentBeforeBalance, agentAfterBalance, changeAgentAmount, sysUser, remarkStr);
            }

            //通道代理
            if (StringUtils.isNotEmpty(agentNoPassage)) {
                AgentAccountInfo agentPassageInfo = agentAccountInfoService.queryAgentInfo(agentNoPassage);
                Long changeAgentAmount = payOrder.getAgentPassageFee();

                Long agentBeforeBalance = agentPassageInfo.getBalance();

                Long agentAfterBalance = agentBeforeBalance + changeAgentAmount;
                AddAgentOrderSuccessHistory(payOrder, agentPassageInfo, agentBeforeBalance, agentAfterBalance, changeAgentAmount, sysUser, remarkStr);
            }
        }

        //通道余额、授信
        //通道入账余额= 订单金额-通道成本-通道代理成本
        Long passageChangeAmount = payOrder.getAmount() - payOrder.getPassageFeeAmount() - payOrder.getAgentPassageFee();
        PayPassage payPassage = payPassageService.queryPassageInfo(payOrder.getPassageId());
        //updatePassageInfo 这个方法中设置了余额会变为null,提前取出
        Long passageBeforeBalance = payPassage.getBalance();

        Long passageAfterBalance = passageBeforeBalance + passageChangeAmount;
        //保存流水入通道流水记录
        //插入通道流水记录
        AddPassageOrderSuccessHistory(payOrder, payPassage, passageBeforeBalance, passageAfterBalance, passageChangeAmount, sysUser, remarkStr);


    }


    private void AddMchOrderSuccessHistory(PayOrder payOrder, MchInfo mchInfo, Long mchBeforeBalance, Long mchAfterBalance, Long mchChangAmount, SysUser sysUser, String remarkStr) {
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
        mchHistory.setBizType(CS.BIZ_TYPE_CHANGE);
        mchHistory.setPayOrderId(payOrder.getPayOrderId());
        mchHistory.setPassageOrderId(payOrder.getPassageOrderNo());
        mchHistory.setPlatIncome(CalPlatProfit(payOrder));
        mchHistory.setAgentIncome(payOrder.getAgentFeeAmount());
        mchHistory.setAgentNo(payOrder.getAgentNo());
        mchHistory.setAgentName(agentMchInfo.getAgentName());
        mchHistory.setAgentIncome(payOrder.getAgentFeeAmount());
        mchHistory.setMchOrderNo(payOrder.getMchOrderNo());

        mchHistory.setCreatedLoginName(sysUser.getLoginUsername());
        mchHistory.setCreatedUid(sysUser.getSysUserId());
        mchHistory.setRemark(remarkStr);

        mchHistory.setCreatedAt(new Date());

        mchHistoryService.save(mchHistory);
    }

    private void AddAgentOrderSuccessHistory(PayOrder payOrder, AgentAccountInfo agentInfo, Long beforeBalance, Long afterBalance, Long changeAgentAmount, SysUser sysUser, String remarkStr) {
        //代理资金流水、余额
        //区分商户代理、通道代理
        AgentAccountHistory agentAccountHistory = new AgentAccountHistory();
        agentAccountHistory.setAgentNo(agentInfo.getAgentNo());
        agentAccountHistory.setAgentName(agentInfo.getAgentName());
        agentAccountHistory.setAmount(changeAgentAmount);
        agentAccountHistory.setBeforeBalance(beforeBalance);
        agentAccountHistory.setAfterBalance(afterBalance);
        agentAccountHistory.setFundDirection(CS.FUND_DIRECTION_INCREASE);
        agentAccountHistory.setBizType(CS.BIZ_TYPE_CHANGE);
        agentAccountHistory.setPayOrderId(payOrder.getPayOrderId());
        agentAccountHistory.setPayOrderAmount(payOrder.getAmount());

        agentAccountHistory.setCreatedLoginName(sysUser.getLoginUsername());
        agentAccountHistory.setCreatedUid(sysUser.getSysUserId());
        agentAccountHistory.setRemark(remarkStr);

        agentAccountHistory.setCreatedAt(new Date());

        agentAccountHistoryService.save(agentAccountHistory);
    }

    private void AddPassageOrderSuccessHistory(PayOrder payOrder, PayPassage payPassage, Long beforeBalance, Long afterBalance, Long passageChangeAmount, SysUser sysUser, String remarkStr) {
        //通道资金流水
        PassageTransactionHistory passageTransactionHistory = new PassageTransactionHistory();
        passageTransactionHistory.setAmount(passageChangeAmount);
        passageTransactionHistory.setPayPassageId(payPassage.getPayPassageId());
        passageTransactionHistory.setPayPassageName(payPassage.getPayPassageName());
        passageTransactionHistory.setPayOrderId(payOrder.getPayOrderId());

        passageTransactionHistory.setBeforeBalance(beforeBalance);
        passageTransactionHistory.setAfterBalance(afterBalance);
        passageTransactionHistory.setFundDirection(passageChangeAmount >= 0 ? CS.FUND_DIRECTION_INCREASE : CS.FUND_DIRECTION_REDUCE);
        passageTransactionHistory.setBizType(PassageTransactionHistory.BIZ_TYPE_CHANGE);
        passageTransactionHistory.setRemark(remarkStr);

        passageTransactionHistory.setCreatedLoginName(sysUser.getLoginUsername());
        passageTransactionHistory.setCreatedUid(sysUser.getSysUserId());

        passageTransactionHistory.setCreatedAt(new Date());

        passageTransactionHistoryService.save(passageTransactionHistory);
    }

    private Long CalPlatProfit(PayOrder payOrder) {
        return payOrder.getMchFeeAmount() - payOrder.getPassageFeeAmount() - payOrder.getAgentFeeAmount() - payOrder.getAgentPassageFee();
    }


    private PayOrder reCalPayOrder(PayOrder payOrderOld) {
        PayOrder payOrder = new PayOrder();
        BeanUtils.copyProperties(payOrderOld, payOrder);


        //商户手续费费率快照-总收费
        Long mchFeeAmount = AmountUtil.calPercentageFee(payOrderOld.getAmount(), payOrderOld.getMchFeeRate());
        payOrder.setMchFeeAmount(mchFeeAmount);

        //通道手续费费率快照
        Long passageFeeAmount = AmountUtil.calPercentageFee(payOrderOld.getAmount(), payOrderOld.getPassageRate());
        payOrder.setPassageFeeAmount(passageFeeAmount);

        //代理-商户手续费 - 代理状态是否可用
        String mchAgentNo = payOrderOld.getAgentNo();
        if (StringUtils.isNotEmpty(mchAgentNo) && payOrderOld.getAgentFeeAmount() != 0L) {
            Long agentRateFee = AmountUtil.calPercentageFee(payOrderOld.getAmount(), payOrderOld.getAgentRate());
            payOrder.setAgentFeeAmount(agentRateFee);
        }

        //代理-通道手续费快照 - 代理状态是否可用
        String passageAgentNo = payOrderOld.getAgentNoPassage();

        if (StringUtils.isNotEmpty(passageAgentNo) && payOrderOld.getAgentPassageFee() != 0L) {
            Long agentRateFee = AmountUtil.calPercentageFee(payOrderOld.getAmount(), payOrderOld.getAgentPassageRate());
            payOrder.setAgentPassageFee(agentRateFee);
        }

        return payOrder;
    }
}
