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
package com.jeequan.jeepay.pay.service;

import cn.hutool.core.collection.CollUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.pay.model.*;
import com.jeequan.jeepay.pay.util.PayCommonUtil;
import com.jeequan.jeepay.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * 配置信息查询服务 （兼容 缓存 和 直接查询方式）
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/11/18 14:41
 */
@Slf4j
@Service
public class ConfigContextQueryService {

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private MchPayPassageService mchPayPassageService;

    @Autowired
    private PayPassageService payPassageService;

    @Autowired
    private MchProductService mchProductService;

    @Autowired
    private ProductService productService;

    public PayPassage queryPayPassage(Long payPassageId) {
        return payPassageService.queryPassageInfo(payPassageId);
    }

    public Product queryProduct(Long productId) {
        return productService.getById(productId);
    }

    /**
     * 查询相关支付信息
     *
     * @return
     */
    public PayConfigContext queryAndCheckPayConfig(String mchNo, Long productId, Long orderAmount) {
        PayConfigContext payConfigContext = null;

        List<PayConfigContext> payConfigContextList = getAvailablePassage(mchNo, productId);

        if (!CollUtil.isNotEmpty(payConfigContextList)) {
            throw new BizException(MessageFormat.format("[{0}]产品下无可用通道[{1}]", productId, mchNo));
        }

        List<PayConfigContext> filterAmountList = new ArrayList<>();
        // 检查通道授信是否满足,通道额度不足-关闭通道
        for (int i = 0; i < payConfigContextList.size(); i++) {
            PayConfigContext configContext = payConfigContextList.get(i);
            PayPassage payPassage = configContext.getPayPassage();

            if (payPassage.getQuotaLimitState() == PayPassage.STATE_CLOSE) {
                filterAmountList.add(configContext);
            } else {
                if (payPassage.getQuota().longValue() >= orderAmount.longValue()) {
                    filterAmountList.add(configContext);
                } else {
                    if (payPassage.getQuota().longValue() <= 0) {
                        //关闭此通道
                        payPassage.setState(CS.NO);
                        payPassageService.updatePassageInfo(payPassage);
                    }
                }
            }
        }
        if (filterAmountList.size() == 0) {
            log.error("[{}]产品下通道授信不足", productId);
            throw new BizException(MessageFormat.format("[{0}]产品下无可用通道[{1}]", productId.toString(), mchNo));
        }

        // 检查金额是否符合收款规则
        List<PayConfigContext> filterRuleList = new ArrayList<>();
        for (int i = 0; i < filterAmountList.size(); i++) {
            PayConfigContext configContext = filterAmountList.get(i);

            //范围金额
            if (configContext.getPayPassage().getPayType() == PayPassage.PAY_TYPE_RANGE) {
                //此处存的是 元
                String[] range = configContext.getPayPassage().getPayRules().trim().split("-");
                Long min = Long.parseLong(range[0]) * 100;
                Long max = Long.parseLong(range[1]) * 100;
                if (orderAmount.longValue() >= min.longValue() && orderAmount.longValue() <= max.longValue()) {
                    filterRuleList.add(configContext);
                    continue;
                }
            }

            //指定金额
            if (configContext.getPayPassage().getPayType() == PayPassage.PAY_TYPE_SPECIFIED) {
                String[] amounts = configContext.getPayPassage().getPayRules().trim().split("\\|");
                if (amounts != null) {
                    for (int index = 0; index < amounts.length; index++) {
                        Long amount = Long.parseLong(amounts[index]) * 100;
                        if (orderAmount.longValue() == amount.longValue()) {
                            filterRuleList.add(configContext);
                            break;
                        }
                    }
                }
            }
        }
        if (filterRuleList.size() == 0) {
            log.error("[{}]产品[{}]下无满足收款规则通道,订单金额[{}]", mchNo, productId, orderAmount.longValue() / 100);
            throw new BizException(MessageFormat.format("[{0}]产品下无可用通道[{1}]", productId.toString(), mchNo));
        }

        //去重
        List<PayConfigContext> resultList = CollUtil.distinct(filterRuleList);

        //商户信息
        MchInfo mchInfo = queryMchInfo(mchNo);

        //检查 实际成本与商户-产品费率 去除费率过高的通道配置
        List<PayConfigContext> rateFilterList = new ArrayList<>();
        for (int index = 0; index < resultList.size(); index++) {
            PayConfigContext payConfigFilter = resultList.get(index);
            //商户-产品费率-总收费
            BigDecimal mchRate = payConfigFilter.getMchProduct().getMchRate();
            //通道费率
            BigDecimal passageRate = payConfigFilter.getPayPassage().getRate();
            //通道代理费率
            BigDecimal agentRate = BigDecimal.ZERO;
            if (StringUtils.isNotEmpty(payConfigFilter.getPayPassage().getAgentNo())) {
                agentRate = payConfigFilter.getPayPassage().getAgentRate();
            }

            //商户代理费率
            BigDecimal agentMchRate = BigDecimal.ZERO;
            //代理不为空时计算 产品-商户-代理费
            if (StringUtils.isNotEmpty(mchInfo.getAgentNo())) {
                if (payConfigFilter.getMchProduct().getAgentRate() != null) {
                    agentMchRate = payConfigFilter.getMchProduct().getAgentRate();
                }
            }
            BigDecimal totalCostRate = passageRate.add(agentRate).add(agentMchRate);
            if (mchRate.compareTo(totalCostRate) > 0) {
                payConfigFilter.setMchInfo(mchInfo);
                rateFilterList.add(payConfigFilter);
            }
        }

        if (!CollUtil.isNotEmpty(rateFilterList)) {
            log.error("[{}]产品[{}]下无符合费率要求通道,订单金额[{}]", mchNo, productId, orderAmount.longValue() / 100);
            throw new BizException(MessageFormat.format("[{0}]产品下无可用通道[{1}]", productId.toString(), mchNo));
        }

        //检索出所有符合条件的通道并根据权重分配通道
        payConfigContext = PayCommonUtil.getPayPassageByWeights(rateFilterList);

        return payConfigContext;
    }

    /**
     * 查询商户信息
     *
     * @param mchNo
     * @return
     */
    public MchInfo queryMchInfo(String mchNo) {
        return mchInfoService.queryMchInfo(mchNo);
    }

    /**
     * 检索符合条件的通道 商户号-产品号 且可用已绑定
     *
     * @param mchNo
     * @param productId
     * @return
     */
    public List<PayConfigContext> getAvailablePassage(String mchNo, Long productId) {
        //1、产品是否对商户开通
        List<MchProduct> mchProductList = mchProductService.list(MchProduct.gw().eq(MchProduct::getMchNo, mchNo).eq(MchProduct::getProductId, productId).eq(MchProduct::getState, CS.YES));
        if (mchProductList.size() != 1) {
            throw new BizException(MessageFormat.format("[{0}]该商户[{1}]产品不可用", mchNo, productId.toString()));
        }
        MchProduct mchProduct = mchProductList.get(0);

        //2、产品下所有可用的通道
        List<PayPassage> payPassageList = payPassageService.list(PayPassage.gw().eq(PayPassage::getProductId, productId).eq(PayPassage::getState, PayPassage.STATE_OPEN));

        if (CollUtil.isNotEmpty(payPassageList)) {
            List<Long> payPassageIds = new ArrayList<>();
            Map<Long, PayPassage> payPassageMap = new HashMap<>();
            for (int i = 0; i < payPassageList.size(); i++) {
                payPassageIds.add(payPassageList.get(i).getPayPassageId());
                payPassageMap.put(payPassageList.get(i).getPayPassageId(), payPassageList.get(i));
            }

            //产品下可用通道中对这个商户开放的通道  已绑定且状态为启用的通道
            List<MchPayPassage> mchPayPassageList = mchPayPassageService.list(MchPayPassage.gw().and(i -> i.eq(MchPayPassage::getMchNo, mchNo).eq(MchPayPassage::getState, CS.YES).in(MchPayPassage::getPayPassageId, payPassageIds)));
            if (CollUtil.isNotEmpty(mchPayPassageList)) {
                List<PayConfigContext> availablePassage = new ArrayList<>();
                for (int i = 0; i < mchPayPassageList.size(); i++) {
                    MchPayPassage item = mchPayPassageList.get(i);
                    PayConfigContext payConfigContext = new PayConfigContext();
                    payConfigContext.setPayPassage(payPassageMap.get(item.getPayPassageId()));
                    payConfigContext.setMchPayPassage(item);
                    payConfigContext.setMchProduct(mchProduct);
                    availablePassage.add(payConfigContext);
                }
                return availablePassage;
            } else {
                log.error("[{}]该产品下无此商户[{}]可用通道", productId, mchNo);
                throw new BizException(MessageFormat.format("[{0}]该产品下无可用通道", productId.toString()));
            }
        } else {
            throw new BizException(MessageFormat.format("[{0}]该产品下无可用通道", productId.toString()));
        }
    }

}
