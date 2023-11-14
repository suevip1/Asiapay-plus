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

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.components.mq.model.PayOrderReissueMQ;
import com.jeequan.jeepay.components.mq.model.StatisticsOrderMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.*;
import com.jeequan.jeepay.pay.channel.IPaymentService;
import com.jeequan.jeepay.pay.ctrl.ApiController;
import com.jeequan.jeepay.pay.exception.ChannelException;
import com.jeequan.jeepay.pay.model.PayConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.pay.service.PayOrderProcessService;
import com.jeequan.jeepay.pay.util.PayCommonUtil;
import com.jeequan.jeepay.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import sun.management.Agent;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg.ChannelState.SYS_ERROR;

/*
 * 创建支付订单抽象类
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/6/8 17:26
 */
@Slf4j
public abstract class AbstractPayOrderController extends ApiController {

    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private ConfigContextQueryService configContextQueryService;
    @Autowired
    private AgentAccountInfoService agentAccountInfoService;
    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private IMQSender mqSender;

    private static final String ORDER_TAG = "[下单校验]";

    private static final String TEST_ORDER_TAG = "[测试入库下单校验]";

    /**
     * 最大轮询次数
     */
    private static int MAX_POLLING_TIME = 4;

    /**
     * 统一下单(新建订单模式)
     **/
    protected ApiRes unifiedOrder(Long productId, UnifiedOrderRQ bizRQ) {

        // 响应数据
        UnifiedOrderRS bizRS = null;
        PayOrder payOrder = null;
        byte orderState = PayOrder.STATE_INIT;
        //是否新订单模式 [  一般接口都为新订单模式，需要先 在DB插入一个新订单， 导致此处需要特殊判断下。 如果已存在则直接更新，否则为插入。  ]
        try {
//            if (payOrder != null) { //当订单存在时，封装公共参数。
//                if (payOrder.getState() != PayOrder.STATE_INIT) {
//                    throw new BizException("订单状态异常");
//                }
//                payOrder.setProductId(productId); // 需要将订单更新 支付方式
//
//                bizRQ.setMchNo(payOrder.getMchNo());
//                bizRQ.setProductId(productId);
//
//                bizRQ.setMchOrderNo(payOrder.getMchOrderNo());
//                bizRQ.setAmount(payOrder.getAmount());
//                bizRQ.setClientIp(payOrder.getClientIp());
//                bizRQ.setNotifyUrl(payOrder.getNotifyUrl());
//            }

            String mchNo = bizRQ.getMchNo();

            // 只有新订单进行校验
            if (payOrderService.count(PayOrder.gw().eq(PayOrder::getMchNo, mchNo).eq(PayOrder::getMchOrderNo, bizRQ.getMchOrderNo())) > 0) {
                log.error("{}商户订单[{}]已存在", ORDER_TAG, bizRQ.getMchOrderNo());
                throw new BizException("商户订单[" + bizRQ.getMchOrderNo() + "]已存在");
            }

            if (StringUtils.isNotEmpty(bizRQ.getNotifyUrl()) && !StringKit.isAvailableUrl(bizRQ.getNotifyUrl())) {
                log.error("{}异步通知地址协议仅支持http:// 或 https:// !,商户回调地址[{}]", ORDER_TAG, bizRQ.getNotifyUrl());
                throw new BizException("异步通知地址协议仅支持http:// 或 https:// !");
            }

            Product product = configContextQueryService.queryProduct(productId);
            if (product == null) {
                throw new BizException("下单失败，[" + bizRQ.getProductId() + "] 产品不存在");
            }
            if (product.getState() == CS.NO) {
                throw new BizException("下单失败，[" + bizRQ.getProductId() + "] 产品状态不可用");
            }

            //1、查询所有可用通道
            List<PayConfigContext> payConfigList = configContextQueryService.queryAllPayConfig(mchNo, product, bizRQ.getAmount());

            if (payConfigList == null || payConfigList.size() == 0) {
                log.error("{}没有可用的通道[{}]", ORDER_TAG, bizRQ.getMchOrderNo());
                throw new BizException("没有可用的通道");
            }
            //轮询次数
            int pollingTime = 0;
            String payOrderId = SeqKit.genPayOrderId();
            //有通道且小于最大轮询次数时
            while ((payConfigList.size() > 0) && pollingTime < MAX_POLLING_TIME) {
                //2、根据权重分配通道
                PayConfigContext payConfigContextCurrent = PayCommonUtil.getPayPassageByWeights(payConfigList);
                if (payConfigContextCurrent == null) {
                    log.error("{}没有可用的支付通道[{}]", ORDER_TAG, bizRQ.getMchOrderNo());
                    throw new BizException("没有可用的支付通道");
                }
                //复制元素，防止remove引用乱
                PayConfigContext payConfigCopy = new PayConfigContext();
                BeanUtils.copyProperties(payConfigContextCurrent, payConfigCopy);
                payConfigList.remove(payConfigContextCurrent);

                payConfigCopy.setProduct(product);
                MchInfo mchInfo = payConfigCopy.getMchInfo();
                //3、获取支付接口
                IPaymentService paymentService = getService(payConfigCopy.getPayPassage().getIfCode());
                //4、生成对应订单对象,一次下单只生成一个订单号
                payOrder = genPayOrder(bizRQ, mchInfo, payConfigCopy);
                payOrder.setPayOrderId(payOrderId);

                String newPayOrderId = paymentService.customPayOrderId(bizRQ, payOrder);
                if (StringUtils.isNotBlank(newPayOrderId)) { // 自定义订单号
                    payOrder.setPayOrderId(newPayOrderId);
                }
                //5、调起上游支付接口
                bizRS = (UnifiedOrderRS) paymentService.pay(bizRQ, payOrder, payConfigCopy);
                pollingTime++;

                //只处理错误、失败以及支付中两种状态
                switch (bizRS.getChannelRetMsg().getChannelState()) {
                    case CONFIRM_FAIL:
                    case SYS_ERROR:
                        // 明确失败
                        // 系统异常：出码失败
                        //判断是否最后一条通道,如果是则订单入库,并返回出码失败
                        //逻辑是取出后马上移除,所以此处判断0
                        orderState = PayOrder.STATE_ERROR;
                        log.info("{}-[{}]通道[{}]{} 出码失败，第【{}】次下单", payOrder.getPayOrderId(), payOrder.getIfCode(), payConfigCopy.getPayPassage().getPayPassageId(), payConfigCopy.getPayPassage().getPayPassageName(), pollingTime);
                        if (payConfigList.size() != 0) {
                            continue;
                        }
                        break;
                    case CONFIRM_SUCCESS:
                    case WAITING:
                        // 上游处理中 || 确认成功  订单为支付中状态
                        orderState = PayOrder.STATE_ING;
                        log.info("{}-[{}]通道[{}]{} 第【{}】次下单，调起成功", payOrder.getPayOrderId(), payOrder.getIfCode(), payConfigCopy.getPayPassage().getPayPassageId(), payConfigCopy.getPayPassage().getPayPassageName(), pollingTime);
                        //清空待循环列表,退出循环
                        payConfigList.clear();
                        continue;
                    default:
                        throw new BizException("ChannelState 返回异常！");
                }
            }
            //循环结束订单入库
            //订单入库 订单状态： 生成状态  此时没有和任何上游渠道产生交互。
            //保存响应
            payOrder.setState(orderState);
            if (bizRS != null) {
                if (bizRS.getChannelRetMsg() != null) {
                    if (StringUtils.isNotEmpty(bizRS.getChannelRetMsg().getChannelOrderId())) {
                        payOrder.setPassageOrderNo(bizRS.getChannelRetMsg().getChannelOrderId());
                    }
                    if (StringUtils.isNotEmpty(bizRS.getChannelRetMsg().getChannelOriginResponse())) {
                        payOrder.setPassageResp(bizRS.getChannelRetMsg().getChannelOriginResponse());
                    }
                    if (StringUtils.isNotEmpty(bizRS.getChannelRetMsg().getChannelErrCode())) {
                        payOrder.setErrCode(bizRS.getChannelRetMsg().getChannelErrCode());
                    }
                    if (StringUtils.isNotEmpty(bizRS.getChannelRetMsg().getChannelErrMsg())) {
                        payOrder.setErrMsg(bizRS.getChannelRetMsg().getChannelErrMsg());
                    }
                }
            }
            payOrderService.save(payOrder);
            log.info("{}-订单入库 [{}]", payOrder.getPayOrderId(), JSONObject.toJSONString(payOrder));

            //todo ======================判断状态 如果是出码失败,则拉起下一条,

            //订单入库,更新统计订单表使用 mq
            mqSender.send(StatisticsOrderMQ.build(payOrder.getPayOrderId(), payOrder));
            return packageApiResByPayOrder(bizRQ, bizRS, payOrder);

        } catch (BizException e) {
            return ApiRes.customFail(e.getMessage());
        } catch (ChannelException e) {
            //处理上游返回数据
            this.processOrderChannelMsg(bizRS.getChannelRetMsg(), payOrder);
            if (e.getChannelRetMsg().getChannelState() == SYS_ERROR) {
                return ApiRes.customFail(e.getMessage());
            }
            return this.packageApiResByPayOrder(bizRQ, bizRS, payOrder);
        } catch (Exception e) {
            log.error("系统异常：{}", e);
            return ApiRes.customFail("系统异常");
        }
    }

    /**
     * 统一下单
     **/
    protected ApiRes unifiedTestInOrder(PayOrder payOrderParams) {

        // 响应数据

        PayOrder payOrder = null;
        UnifiedOrderRS bizRS = null;

        Long productId = payOrderParams.getProductId();
        Long passageId = payOrderParams.getPassageId();
        Long amount = payOrderParams.getAmount();
        String mchOrderNo = payOrderParams.getMchOrderNo();
        String mchNo = CS.TEST_MCH_NO;
        String notifyUrl = sysConfigService.getDBApplicationConfig().getMchSiteUrl() + "/api/anon/paytestNotify/payOrder";

        UnifiedOrderRQ bizRQ = new UnifiedOrderRQ();
        bizRQ.setMchOrderNo(mchOrderNo);
        bizRQ.setAmount(amount);
        bizRQ.setMchNo(mchNo);
        bizRQ.setProductId(productId);
        bizRQ.setNotifyUrl(notifyUrl);
        try {

            PayConfigContext payConfigContext = new PayConfigContext();

            Product product = configContextQueryService.queryProduct(productId);
            MchInfo mchInfo = configContextQueryService.queryMchInfo(mchNo);
            PayPassage payPassage = configContextQueryService.queryPayPassage(passageId);

            payConfigContext.setMchInfo(mchInfo);
            payConfigContext.setPayPassage(payPassage);
            payConfigContext.setProduct(product);


            //获取支付接口
            IPaymentService paymentService = getService(payConfigContext.getPayPassage().getIfCode());

            //生成订单
            payOrder = genTestInPayOrder(bizRQ, payConfigContext);

            //订单入库 订单状态： 生成状态  此时没有和任何上游渠道产生交互。
            payOrderService.save(payOrder);
            log.info("{}-订单入库 [{}]", TEST_ORDER_TAG, JSONObject.toJSONString(payOrder));

            //调起上游支付接口
            bizRS = (UnifiedOrderRS) paymentService.pay(null, payOrder, payConfigContext);
            log.info("{}-调起[{}]三方接口返回:{}", TEST_ORDER_TAG, payOrder.getIfCode(), JSONObject.toJSONString(bizRS.getChannelRetMsg().getChannelOriginResponse()));
            //保存响应
            //处理上游返回数据 此处待修改,状态只到支付中  这里的CONFIRM_SUCCESS 也只修改订单状态为支付中
            byte orderState = this.processOrderChannelMsg(bizRS.getChannelRetMsg(), payOrder);
            //返回的最新订单状态
            payOrder.setState(orderState);
            //订单入库,更新统计订单表使用 mq
            mqSender.send(StatisticsOrderMQ.build(payOrder.getPayOrderId(), payOrder));
            return packageApiResByPayOrder(bizRQ, bizRS, payOrder);

        } catch (BizException e) {
            return ApiRes.customFail(e.getMessage());
        } catch (ChannelException e) {
            //处理上游返回数据
            this.processOrderChannelMsg(bizRS.getChannelRetMsg(), payOrder);
            if (e.getChannelRetMsg().getChannelState() == SYS_ERROR) {
                return ApiRes.customFail(e.getMessage());
            }
            return this.packageApiResByPayOrder(bizRQ, bizRS, payOrder);
        } catch (Exception e) {
            log.error("系统异常：{}", e);
            return ApiRes.customFail("系统异常");
        }
    }


    /**
     * 生成/更新订单
     *
     * @param rq
     * @param mchInfo
     * @param payConfigContext
     * @return
     */
    private PayOrder genPayOrder(UnifiedOrderRQ rq, MchInfo mchInfo, PayConfigContext payConfigContext) {

        PayOrder payOrder = new PayOrder();

//        payOrder.setPayOrderId(SeqKit.genPayOrderId()); //生成订单ID
        payOrder.setMchNo(mchInfo.getMchNo()); //商户号
        payOrder.setMchName(mchInfo.getMchName()); //商户名称
        payOrder.setMchOrderNo(rq.getMchOrderNo()); //商户订单号
        payOrder.setIfCode(payConfigContext.getPayPassage().getIfCode()); //接口代码
        payOrder.setProductId(rq.getProductId()); //支付方式
        payOrder.setProductName(payConfigContext.getProduct().getProductName());
        payOrder.setAmount(rq.getAmount()); //订单金额
        payOrder.setClientIp(rq.getClientIp()); // 设置IP

        if (StringUtils.isNotEmpty(rq.getExtParam())) {
            payOrder.setExtParamJson(rq.getExtParam());
        }

        //商户手续费费率快照-总收费
        Long mchFeeAmount = AmountUtil.calPercentageFee(rq.getAmount(), payConfigContext.getMchProduct().getMchRate());
        payOrder.setMchFeeRate(payConfigContext.getMchProduct().getMchRate());
        payOrder.setMchFeeAmount(mchFeeAmount);

        //通道手续费费率快照
        Long passageFeeAmount = AmountUtil.calPercentageFee(rq.getAmount(), payConfigContext.getPayPassage().getRate());
        payOrder.setPassageRate(payConfigContext.getPayPassage().getRate());
        payOrder.setPassageFeeAmount(passageFeeAmount);

        //代理-商户手续费 - 代理状态是否可用
        String mchAgentNo = payConfigContext.getMchInfo().getAgentNo();
        payOrder.setAgentRate(BigDecimal.ZERO);
        payOrder.setAgentNo("");
        payOrder.setAgentFeeAmount(0L);
        if (StringUtils.isNotEmpty(mchAgentNo)) {
            AgentAccountInfo agentAccountInfo = agentAccountInfoService.queryAgentInfo(mchAgentNo);
            if (agentAccountInfo != null && agentAccountInfo.getState() == CS.YES) {
                payOrder.setAgentRate(payConfigContext.getMchProduct().getAgentRate());
                payOrder.setAgentNo(payConfigContext.getMchInfo().getAgentNo());

                Long agentRateFee = AmountUtil.calPercentageFee(rq.getAmount(), payConfigContext.getMchProduct().getAgentRate());
                payOrder.setAgentFeeAmount(agentRateFee);
            }
        }

        //代理-通道手续费快照 - 代理状态是否可用
        PayPassage payPassage = payConfigContext.getPayPassage();
        String passageAgentNo = payPassage.getAgentNo();
        payOrder.setAgentPassageRate(BigDecimal.ZERO);
        payOrder.setAgentNoPassage("");
        payOrder.setAgentPassageFee(0L);

        if (payPassage != null && StringUtils.isNotEmpty(passageAgentNo)) {
            AgentAccountInfo agentPassageInfo = agentAccountInfoService.queryAgentInfo(passageAgentNo);
            if (agentPassageInfo != null && agentPassageInfo.getState() == CS.YES) {
                payOrder.setAgentPassageRate(payPassage.getAgentRate());
                payOrder.setAgentNoPassage(payPassage.getAgentNo());

                Long agentRateFee = AmountUtil.calPercentageFee(rq.getAmount(), payPassage.getAgentRate());
                payOrder.setAgentPassageFee(agentRateFee);
            }
        }

        payOrder.setState(PayOrder.STATE_INIT); //订单状态, 默认订单生成状态
        payOrder.setClientIp(StringUtils.defaultIfEmpty(rq.getClientIp(), getClientIp())); //客户端IP
        payOrder.setNotifyUrl(rq.getNotifyUrl()); //异步通知地址

        Date nowDate = new Date();

        //订单过期时间 单位： 秒
        payOrder.setExpiredTime(DateUtil.offsetMinute(nowDate, CS.ORDER_EXPIRED_TIME)); //订单过期时间 默认120min

        payOrder.setCreatedAt(nowDate); //订单创建时间

        //支付渠道信息
        payOrder.setPassageId(payConfigContext.getPayPassage().getPayPassageId());
        payOrder.setIfCode(payConfigContext.getPayPassage().getIfCode());

        return payOrder;
    }

    /**
     * 生成需要入库的测试订单
     *
     * @param rq
     * @param payConfigContext
     * @return
     */
    private PayOrder genTestInPayOrder(UnifiedOrderRQ rq, PayConfigContext payConfigContext) {

        PayOrder payOrder = new PayOrder();

        payOrder.setPayOrderId(SeqKit.genPayOrderId()); //生成订单ID
        payOrder.setMchNo(payConfigContext.getMchInfo().getMchNo()); //商户号
        payOrder.setMchName(payConfigContext.getMchInfo().getMchName()); //商户名称

        payOrder.setMchOrderNo(rq.getMchOrderNo()); //商户订单号
        payOrder.setAmount(rq.getAmount()); //订单金额

        payOrder.setIfCode(payConfigContext.getPayPassage().getIfCode()); //接口代码
        payOrder.setProductId(payConfigContext.getProduct().getProductId()); //支付方式
        payOrder.setProductName(payConfigContext.getProduct().getProductName());

        //通道手续费费率快照
        Long passageFeeAmount = AmountUtil.calPercentageFee(rq.getAmount(), payConfigContext.getPayPassage().getRate());
        payOrder.setPassageRate(payConfigContext.getPayPassage().getRate());
        payOrder.setPassageFeeAmount(passageFeeAmount);

        //商户手续费 费率快照-总收费
        payOrder.setMchFeeRate(payConfigContext.getPayPassage().getRate());
        payOrder.setMchFeeAmount(passageFeeAmount);

        //代理-商户手续费 - 代理状态是否可用
        payOrder.setAgentRate(BigDecimal.ZERO);
        payOrder.setAgentNo("");
        payOrder.setAgentFeeAmount(0L);

        //代理-通道手续费快照 - 代理状态是否可用
        payOrder.setAgentPassageRate(BigDecimal.ZERO);
        payOrder.setAgentNoPassage("");
        payOrder.setAgentPassageFee(0L);

        payOrder.setState(PayOrder.STATE_INIT); //订单状态, 默认订单生成状态
        payOrder.setClientIp(getClientIp()); //客户端IP
        payOrder.setNotifyUrl(rq.getNotifyUrl()); //异步通知地址

        Date nowDate = new Date();

        //订单过期时间 单位： 秒
        payOrder.setExpiredTime(DateUtil.offsetMinute(nowDate, CS.ORDER_EXPIRED_TIME)); //订单过期时间 默认120min

        payOrder.setCreatedAt(nowDate); //订单创建时间

        //支付渠道信息
        payOrder.setPassageId(payConfigContext.getPayPassage().getPayPassageId());
        payOrder.setIfCode(payConfigContext.getPayPassage().getIfCode());

        return payOrder;
    }


    /**
     * 查询三方支付接口
     *
     * @param ifCode
     * @return
     */
    public IPaymentService getService(String ifCode) {
        // 接口代码
        IPaymentService paymentService = SpringBeansUtil.getBean(ifCode + "PaymentService", IPaymentService.class);
        if (paymentService == null) {
            throw new BizException("无此支付通道接口");
        }
        return paymentService;
    }


    /**
     * 处理返回的渠道信息，并更新订单状态
     * payOrder将对部分信息进行 赋值操作。
     *
     * @param channelRetMsg
     * @param payOrder
     * @return 返回订单对象
     */
    private byte processOrderChannelMsg(ChannelRetMsg channelRetMsg, PayOrder payOrder) {

        //对象为空 || 上游返回状态为空， 则无需操作
        if (channelRetMsg == null || channelRetMsg.getChannelState() == null) {
            return PayOrder.STATE_INIT;
        }

        //只处理错误、失败以及支付中两种状态
        switch (channelRetMsg.getChannelState()) {
            case CONFIRM_FAIL:
            case SYS_ERROR:
                // 明确失败
                // 系统异常：出码失败
                this.updateInitOrderStateThrowException(PayOrder.STATE_ERROR, payOrder, channelRetMsg);
                return PayOrder.STATE_ERROR;
            case CONFIRM_SUCCESS:
            case WAITING:
                // 上游处理中 || 确认成功  订单为支付中状态
                this.updateInitOrderStateThrowException(PayOrder.STATE_ING, payOrder, channelRetMsg);
                break;
            default:
                throw new BizException("ChannelState 返回异常！");
        }

        //判断是否需要轮询查单
//        if (channelRetMsg.isNeedQuery()) {
//            mqSender.send(PayOrderReissueMQ.build(payOrderId, 1), 5);
//        }
        return PayOrder.STATE_ING;
    }


    /**
     * 更新订单状态 --》 订单生成--》 其他状态  (向外抛出异常)
     **/
    private void updateInitOrderStateThrowException(byte orderState, PayOrder payOrder, ChannelRetMsg channelRetMsg) {
        payOrder.setState(orderState);
        payOrder.setPassageOrderNo(channelRetMsg.getChannelOrderId());
        payOrder.setErrCode(channelRetMsg.getChannelErrCode());
        payOrder.setErrMsg(channelRetMsg.getChannelErrMsg());
        payOrder.setPassageResp(channelRetMsg.getChannelOriginResponse());
        boolean isSuccess = true;
        switch (channelRetMsg.getChannelState()) {
            case CONFIRM_SUCCESS:
            case WAITING:
                // 上游处理中 || 确认成功  订单为支付中状态
                isSuccess = payOrderService.updateInit2Ing(payOrder);
                break;
            case CONFIRM_FAIL:
            case SYS_ERROR:
                // 系统异常：出码失败
                isSuccess = payOrderService.updateInit2Error(payOrder);
                break;
            default:
                throw new BizException("ChannelState 返回异常！");
        }
        if (!isSuccess) {
            throw new BizException("更新订单异常!");
        }
    }


    /**
     * 统一封装订单数据
     **/
    private ApiRes packageApiResByPayOrder(UnifiedOrderRQ bizRQ, UnifiedOrderRS bizRS, PayOrder payOrder) {

        // 返回接口数据
        bizRS.setPayOrderId(payOrder.getPayOrderId());
        bizRS.setOrderState(payOrder.getState());
        bizRS.setMchOrderNo(payOrder.getMchOrderNo());

        if (payOrder.getState() == PayOrder.STATE_FAIL || payOrder.getState() == PayOrder.STATE_ERROR) {
            bizRS.setErrCode(bizRS.getChannelRetMsg() != null ? bizRS.getChannelRetMsg().getChannelErrCode() : null);
            bizRS.setErrMsg("出码失败");
        }

        return ApiRes.okWithSign(bizRS, configContextQueryService.queryMchInfo(bizRQ.getMchNo()).getSecret());
    }


}
