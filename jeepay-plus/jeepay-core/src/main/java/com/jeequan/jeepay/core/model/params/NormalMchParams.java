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
package com.jeequan.jeepay.core.model.params;

import lombok.Data;

/**
 * 支付接口通用商户配置基类
 */
@Data
public class NormalMchParams {

    /**
     * 商户号、ID等
     */
    private String mchNo;

    /**
     * 支付网关
     */
    private String payGateway;

    /**
     * 支付类型
     */
    private String payType;

    /**
     * 密钥
     */
    private String secret;

    /**
     * 回调地址
     */
    private String notifyUrl;

    /**
     * 查单网关
     */
    private String queryUrl;

    /**
     * 白名单列表 | 分割
     */
    private String whiteList;

//    [{"desc": "商户号", "name": "mchNo", "type": "text", "verify": "required"}, {"desc": "秘钥", "name": "secret", "type": "textarea", "verify": "required"}, {"desc": "支付类型", "name": "payType", "type": "text", "verify": "required"}, {"desc": "支付网关", "name": "payGateway", "type": "textarea", "verify": "required"}, {"desc": "回调白名单(多个地址以 | 分隔)", "name": "whiteList", "type": "textarea", "verify": "required"}]

}
