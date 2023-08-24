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
package com.jeequan.jeepay.pay.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.pay.channel.IPaymentService;
import com.jeequan.jeepay.pay.model.PayConfigContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 支付通用工具类  Utils
 */
public class PayCommonUtil {

    private static final String PAYWAY_PACKAGE_NAME = "payway";

    /**
     * 获取真实的支付方式Service
     **/
    public static IPaymentService getRealPaywayService(Object obj, String wayCode) {

        try {

            //下划线转换驼峰 & 首字母大写
            String clsName = StrUtil.upperFirst(StrUtil.toCamelCase(wayCode.toLowerCase()));
            return (IPaymentService) SpringBeansUtil.getBean(
                    Class.forName(obj.getClass().getPackage().getName()
                            + "." + PAYWAY_PACKAGE_NAME
                            + "." + clsName)
            );

        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * 通过权重获取通道
     *
     * @param payPassageList
     * @return
     */
    public static PayConfigContext getPayPassageByWeights(List<PayConfigContext> payPassageList) {
        List<PayConfigContext> filter = new ArrayList<>();
        int weightAll = 0;

        //去掉权重为0的
        for (int i = 0; i < payPassageList.size(); i++) {
            if (payPassageList.get(i).getPayPassage().getWeights() != 0) {
                filter.add(payPassageList.get(i));
                weightAll += filter.get(i).getPayPassage().getWeights();
            }
        }

        int random = GetRandom(1, weightAll);
        int temp = 0;
        for (int i = 0; i < filter.size(); i++) {
            temp += filter.get(i).getPayPassage().getWeights();
            if (temp >= random) {
                return filter.get(i);
            }
        }

        return null;
    }

    private static int GetRandom(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }

    /**
     * 计算平台收入
     * @param payOrder
     * @return
     */
    public static Long CalPlatProfit(PayOrder payOrder) {
        return payOrder.getMchFeeAmount() - payOrder.getPassageFeeAmount() - payOrder.getAgentFeeAmount() - payOrder.getAgentPassageFee();
    }



}
