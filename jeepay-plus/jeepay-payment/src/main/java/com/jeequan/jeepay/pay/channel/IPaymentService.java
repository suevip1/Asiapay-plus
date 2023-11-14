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
package com.jeequan.jeepay.pay.channel;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.model.PayConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;

/*
* 调起上游渠道侧支付接口
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/5/8 15:13
*/
public interface IPaymentService {

    /**
     * 获取到接口code
     * @return
     */
    String getIfCode();

    /**
     * 自定义支付订单号， 若返回空则使用系统生成订单号
     * @param bizRQ
     * @param payOrder
     * @return
     */
    String customPayOrderId(UnifiedOrderRQ bizRQ, PayOrder payOrder);

    /**
     * 调起支付接口
     * @param bizRQ
     * @param payOrder
     * @param payConfigContext
     * @return
     * @throws Exception
     */
    AbstractRS pay(UnifiedOrderRQ bizRQ, PayOrder payOrder, PayConfigContext payConfigContext) throws Exception;
}
