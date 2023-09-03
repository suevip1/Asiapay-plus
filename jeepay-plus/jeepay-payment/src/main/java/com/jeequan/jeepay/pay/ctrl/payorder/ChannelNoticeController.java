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
package com.jeequan.jeepay.pay.ctrl.payorder;

import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jeequan.jeepay.components.mq.model.BalanceChangeMQ;
import com.jeequan.jeepay.components.mq.model.StatisticsOrderMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.ctrls.AbstractCtrl;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.core.utils.StringKit;
import com.jeequan.jeepay.pay.channel.IChannelNoticeService;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.pay.service.PayMchNotifyService;
import com.jeequan.jeepay.pay.service.PayOrderProcessService;
import com.jeequan.jeepay.pay.util.PayCommonUtil;
import com.jeequan.jeepay.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
 * 渠道侧的通知入口Controller 【分为同步跳转（doReturn）和异步回调(doNotify) 】
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/6/8 17:26
 */
@Slf4j
@Controller
public class ChannelNoticeController extends AbstractCtrl {

    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private ConfigContextQueryService configContextQueryService;
    @Autowired
    private PayOrderProcessService payOrderProcessService;
    @Autowired
    private IMQSender mqSender;

    /**
     * 异步回调入口
     **/
    @ResponseBody
    @RequestMapping(value = {"/api/pay/notify/{ifCode}", "/api/pay/notify/{ifCode}/{payOrderId}"})
    public ResponseEntity doNotify(HttpServletRequest request, @PathVariable("ifCode") String ifCode, @PathVariable(value = "payOrderId", required = false) String urlOrderId) {

        String payOrderId = null;
        String requestIp = getRequestIP(request);
        String logPrefix = "进入[" + ifCode + "]支付回调：urlOrderId：[" + StringUtils.defaultIfEmpty(urlOrderId, "") + "] ";
        log.info("===== {} =====", logPrefix);
        log.info("[{}]接口回调，回调IP[{}]", ifCode, requestIp);
        try {

            // 参数有误
            if (StringUtils.isEmpty(ifCode)) {
                return ResponseEntity.badRequest().body("ifCode is empty");
            }

            //查询支付接口是否存在
            IChannelNoticeService payNotifyService = SpringBeansUtil.getBean(ifCode + "ChannelNoticeService", IChannelNoticeService.class);

            // 支付通道接口实现不存在
            if (payNotifyService == null) {
                log.error("{}, interface not exists ", logPrefix);
                return ResponseEntity.badRequest().body("[" + ifCode + "] interface not exists");
            }

            // 解析订单号 和 请求参数
            MutablePair<String, Object> mutablePair = payNotifyService.parseParams(request, urlOrderId, IChannelNoticeService.NoticeTypeEnum.DO_NOTIFY);
            if (mutablePair == null) { // 解析数据失败， 响应已处理
                log.error("{}, mutablePair is null ", logPrefix);
                throw new BizException("解析数据异常！"); //需要实现类自行抛出ResponseException, 不应该在这抛此异常。
            }

            //解析到订单号
            payOrderId = mutablePair.left;
            log.info("{}, 解析数据为：payOrderId:{}, params:{}", logPrefix, payOrderId, mutablePair.getRight());

            if (StringUtils.isNotEmpty(urlOrderId) && !urlOrderId.equals(payOrderId)) {
                log.error("{}, 订单号不匹配. urlOrderId={}, payOrderId={} ", logPrefix, urlOrderId, payOrderId);
                throw new BizException("订单号不匹配！");
            }

            //获取订单号 和 订单数据
            PayOrder payOrder = payOrderService.getById(payOrderId);

            // 订单不存在
            if (payOrder == null) {
                log.error("{}, 订单不存在. payOrderId={} ", logPrefix, payOrderId);
                return payNotifyService.doNotifyOrderNotExists(request);
            }

            //订单已是成功状态，此时是重复的订单
            if (payOrder.getState() == PayOrder.STATE_SUCCESS) {
                log.error("{}, 订单已是成功状态. payOrderId={} ", logPrefix, payOrderId);
                throw new BizException("订单已支付成功,重复的回调！");
            }

            //查询出商户应用的配置信息
            PayPassage payPassage = configContextQueryService.queryPayPassage(payOrder.getPassageId());
            NormalMchParams normalMchParams = new NormalMchParams();
            try {
                normalMchParams = JSON.parseObject(payPassage.getPayInterfaceConfig(), NormalMchParams.class);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                log.error("通道支付配置转换错误:" + payPassage.getPayInterfaceConfig());
            }

            if (normalMchParams == null) {
                log.error("通道配置错误[{}]-ID:{} {}", payPassage.getIfCode(), payPassage.getPayPassageId(), payPassage.getPayPassageName());
                throw new BizException("通道配置错误！");
            }

            if (StringUtils.isEmpty(normalMchParams.getWhiteList())) {
                log.error("回调IP未设置[{}]-ID:{} {}", payPassage.getIfCode(), payPassage.getPayPassageId(), payPassage.getPayPassageName());
                throw new BizException("回调IP白名单校验异常！");
            }

            //判断IP是否在白名单中
            if (!StringKit.checkInWhiteList(requestIp, normalMchParams.getWhiteList())) {
                log.error("回调IP[{}]不在白名单之内,白名单列表[{}]:", requestIp, normalMchParams.getWhiteList());
                throw new BizException("回调IP白名单校验异常！");
            }

            //保存回调的请求
            JSONObject jsonNotifyParams = (JSONObject) mutablePair.getRight();
            payOrder.setNotifyParams(jsonNotifyParams.toJSONString());

            //调起接口的回调判断
            ChannelRetMsg notifyResult = payNotifyService.doNotice(request, mutablePair.getRight(), payOrder, payPassage, IChannelNoticeService.NoticeTypeEnum.DO_NOTIFY);

            // 返回null 表明出现异常， 无需处理通知下游等操作。
            if (notifyResult == null || notifyResult.getChannelState() == null || notifyResult.getResponseEntity() == null) {
                log.error("{}, 处理回调事件异常  notifyResult data error, notifyResult ={} ", logPrefix, notifyResult);
                throw new BizException("处理回调事件异常！"); //需要实现类自行抛出ResponseException, 不应该在这抛此异常。
            }

            boolean updateOrderSuccess = true; //默认更新成功
            // 订单是 【支付中状态】
            if (payOrder.getState() == PayOrder.STATE_ING || payOrder.getState() == PayOrder.STATE_CLOSED) {

                //明确成功
                if (ChannelRetMsg.ChannelState.CONFIRM_SUCCESS == notifyResult.getChannelState()) {
                    if (StringUtils.isNotEmpty(notifyResult.getChannelOrderId())) {
                        payOrder.setPassageOrderNo(notifyResult.getChannelOrderId());
                    }
                    updateOrderSuccess = payOrderService.updateIng2Success(payOrder);
                    //防止同时多笔回调
                    if (updateOrderSuccess) {
                        PayOrder payOrderCopy = new PayOrder();
                        BeanUtils.copyProperties(payOrder, payOrderCopy);
                        payOrderCopy.setState(PayOrder.STATE_SUCCESS);
                        payOrderCopy.setSuccessTime(new Date());
                        //订单成功统计通知
                        mqSender.send(StatisticsOrderMQ.build(payOrder.getPayOrderId(), payOrderCopy));
                        //余额更新mq
                        mqSender.send(BalanceChangeMQ.build(payOrderCopy));
                    }

                } else if (ChannelRetMsg.ChannelState.CONFIRM_FAIL == notifyResult.getChannelState()) {
                    //明确失败
                    updateOrderSuccess = payOrderService.updateIng2Fail(payOrder);
                }
            }

            // 更新订单 异常
            if (!updateOrderSuccess) {
                log.error("{}, 更新订单 异常 = {} ", logPrefix, payOrder.getPayOrderId());
                return payNotifyService.doNotifyOrderStateUpdateFail(request);
            }

            //订单支付成功 其他业务逻辑
            if (notifyResult.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS) {
                payOrderProcessService.confirmSuccess(payOrder);
            }

            log.info("===== {}, 订单通知完成。 payOrderId={}, parseState = {} =====", logPrefix, payOrderId, notifyResult.getChannelState());

            return notifyResult.getResponseEntity();

        } catch (BizException e) {
            log.error("{}, payOrderId={}, BizException", logPrefix, payOrderId, e);
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (ResponseException e) {
            log.error("{}, payOrderId={}, ResponseException", logPrefix, payOrderId, e);
            return e.getResponseEntity();

        } catch (Exception e) {
            log.error("{}, payOrderId={}, 系统异常", logPrefix, payOrderId, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    /*  跳转到支付成功页面 **/
    private String toReturnPage(String errInfo) {
        return "cashier/returnPage";
    }

    private String getRequestIP(HttpServletRequest request) {
        return ServletUtil.getClientIP(request);
    }
}
