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

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.components.mq.model.BalanceChangeMQ;
import com.jeequan.jeepay.components.mq.model.PayOrderForceSuccessMQ;
import com.jeequan.jeepay.components.mq.model.StatisticsOrderMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 支付订单类
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021-06-07 07:15
 */
@RestController
@RequestMapping("/api/payOrder")
public class PayOrderController extends CommonCtrl {
    @Autowired
    private PayOrderService payOrderService;

    @Autowired
    private PayPassageService payPassageService;

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
//            Long start = System.currentTimeMillis();
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
//            Long end = System.currentTimeMillis();
//            logger.error((end - start) / 1000f + "");
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
}
