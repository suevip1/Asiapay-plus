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
package com.jeequan.jeepay.pay.rqrs.payorder;

import com.jeequan.jeepay.pay.rqrs.AbstractMchAppRQ;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/*
* 创建订单请求参数对象
* 聚合支付接口（统一下单）
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:33
*/
@Data
public class UnifiedOrderRQ extends AbstractMchAppRQ {

    /** 商户订单号 **/
    @NotBlank(message="商户订单号不能为空")
    private String mchOrderNo;

    /** 客户端IP地址 **/
    @NotBlank(message="客户端IP地址不能为空")
    private String clientIp;

    /** 异步通知地址 **/
    @NotBlank(message="异步通知地址不能为空")
    private String notifyUrl;

    /**
     * 额外参数
     */
    private String extParam;


    /** 返回真实的bizRQ **/
    public UnifiedOrderRQ buildBizRQ(){
        return this;
    }

}
