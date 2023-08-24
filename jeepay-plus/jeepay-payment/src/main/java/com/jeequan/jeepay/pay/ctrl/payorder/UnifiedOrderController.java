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

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.components.mq.model.StatisticsOrderMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.pay.channel.IPaymentService;
import com.jeequan.jeepay.pay.model.PayConfigContext;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/*
 * 统一下单 controller
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/6/8 17:27
 */
@Slf4j
@RestController
public class UnifiedOrderController extends AbstractPayOrderController {

    @Autowired
    private ConfigContextQueryService configContextQueryService;

    /**
     * 统一下单接口
     **/
    @PostMapping("/api/pay/unifiedOrder")
    public ApiRes unifiedOrder() {

        //获取参数 & 验签
        UnifiedOrderRQ rq = getRQByWithMchSign(UnifiedOrderRQ.class);

        UnifiedOrderRQ bizRQ = buildBizRQ(rq);
        log.info("[{}]接收到统一下单请求:商户订单号[{}] {} 金额:{}", bizRQ.getMchNo(), bizRQ.getMchOrderNo(), bizRQ, bizRQ.getAmount());

        //实现子类的res
        ApiRes apiRes = unifiedOrder(bizRQ.getProductId(), bizRQ);
        if (apiRes.getData() == null) {
            return apiRes;
        }

        UnifiedOrderRS bizRes = (UnifiedOrderRS) apiRes.getData();

        return ApiRes.okWithSign(bizRes, configContextQueryService.queryMchInfo(rq.getMchNo()).getSecret());
    }

    @PostMapping("/api/pay/unifiedOrderPassageTest")
    public ApiRes unifiedOrderPassageTest() {
        try {
            PayOrder payOrderParams = getObject(PayOrder.class);
            log.info(JSONObject.toJSONString(payOrderParams));

            //只做拉起通道测试，不走订单流程，主要校验通道代码以及配置等是否正常
            PayPassage payPassage = configContextQueryService.queryPayPassage(payOrderParams.getPassageId());
            if (payPassage == null) {
                return ApiRes.customFail("通道不存在!");
            }
            //支付接口service
            IPaymentService paymentService = getService(payPassage.getIfCode());
            log.info("支付接口:" + paymentService.toString());
            PayConfigContext payConfigContext = new PayConfigContext();
            payConfigContext.setPayPassage(payPassage);

            // 响应数据
            UnifiedOrderRS bizRS = (UnifiedOrderRS) paymentService.pay(null, payOrderParams, payConfigContext);
            log.info("订单号-[{}] 接口响应数据:{}", payOrderParams.getPayOrderId(), JSONObject.toJSONString(bizRS.getChannelRetMsg()));
            if (StringUtils.isEmpty(bizRS.getPayData())) {
                bizRS.setErrMsg(JSONObject.toJSONString(bizRS.getChannelRetMsg().getChannelOriginResponse()));
                return ApiRes.customFail(JSONObject.toJSONString(bizRS.getChannelRetMsg().getChannelOriginResponse()));
            }
            return ApiRes.ok(bizRS);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ApiRes.customFail(e.getMessage());
        }
    }

    private UnifiedOrderRQ buildBizRQ(UnifiedOrderRQ rq) {
        //转换为 bizRQ
        return rq.buildBizRQ();
    }


}
