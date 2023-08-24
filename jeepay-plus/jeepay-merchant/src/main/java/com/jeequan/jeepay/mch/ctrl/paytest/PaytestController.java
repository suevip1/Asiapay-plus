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
package com.jeequan.jeepay.mch.ctrl.paytest;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.MchProduct;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.DBApplicationConfig;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.MchPayPassageService;
import com.jeequan.jeepay.service.impl.MchProductService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/*
 * 支付测试类
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/6/22 9:43
 */
@Slf4j
@RestController
@RequestMapping("/api/paytest")
public class PaytestController extends CommonCtrl {

    @Autowired
    private MchInfoService mchInfoService;
    @Autowired
    private MchPayPassageService mchPayPassageService;
    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private MchProductService mchProductService;

    /**
     * 调起下单接口
     **/
    @PostMapping("/payOrders")
    public ApiRes doPay() {
        //获取请求参数
        Long amount = getValLong("amount");
        String mchOrderNo = getValStringRequired("mchOrderNo");
        Long productId = getValLongRequired("productId");

        log.info("商户[{}],产品ID[{}],测试拉单请求,[{}]金额:", getCurrentMchNo(), productId, mchOrderNo, amount);

        MchInfo mchInfo = mchInfoService.queryMchInfo(getCurrentMchNo());
        if (mchInfo == null || mchInfo.getState() != CS.PUB_USABLE) {
            throw new BizException("商户不存在或不可用");
        }

        MchProduct mchProduct = mchProductService.getOne(MchProduct.gw().eq(MchProduct::getMchNo, getCurrentMchNo()).eq(MchProduct::getState, CS.YES).eq(MchProduct::getProductId, productId));

        if (mchProduct == null || mchProduct.getState() != CS.PUB_USABLE) {
            throw new BizException("该商户下产品[" + productId + "]不存在或不可用");
        }

        DBApplicationConfig dbApplicationConfig = sysConfigService.getDBApplicationConfig();

        try {
            Map<String, Object> map = new HashMap<>();

            long reqTime = System.currentTimeMillis();

            String notifyUrl = dbApplicationConfig.getMchSiteUrl() + "/api/anon/paytestNotify/payOrder";

            map.put("mchNo", getCurrentMchNo());
            map.put("mchOrderNo", mchOrderNo);
            map.put("amount", amount);
            map.put("productId", productId);
            map.put("reqTime", reqTime);
            map.put("clientIp", getClientIp());
            map.put("notifyUrl", notifyUrl);
            String sign = JeepayKit.getSign(map, mchInfo.getSecret()).toUpperCase();
            map.put("sign", sign);
            log.info("商户[{}],测试拉单请求参数:{}", getCurrentMchNo(), JSONObject.toJSONString(map));
            String raw = HttpUtil.post(dbApplicationConfig.getPaySiteUrl() + "/api/pay/unifiedOrder", map);
            log.info("商户[{}],测试拉单返回:{}", getCurrentMchNo(), raw);
            JSONObject jsonObject = JSONObject.parseObject(raw);
            Integer code = jsonObject.getInteger("code");
            if (code != null && code == 0) {
                return ApiRes.ok(jsonObject);
            } else {
                return ApiRes.fail(ApiCodeEnum.SYSTEM_ERROR, jsonObject.toJSONString());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException(e.getMessage());
        }
    }

}
