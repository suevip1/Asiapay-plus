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
package com.jeequan.jeepay.mch.ctrl.merchant;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.MchProduct;
import com.jeequan.jeepay.core.entity.Product;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchProductService;
import com.jeequan.jeepay.service.impl.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户支付产品管理类
 */
@Slf4j
@RestController
@RequestMapping("/api/mchApps")
public class MchAppController extends CommonCtrl {
    @Autowired
    private ProductService productService;
    @Autowired
    private MchProductService mchProductService;


    @GetMapping
    public ApiRes list() {
        Map<Long, Product> productMap = productService.getProductMap();

        IPage<MchProduct> pages = mchProductService.page(getIPage(true), MchProduct.gw().select(MchProduct::getMchNo, MchProduct::getCreatedAt, MchProduct::getUpdatedAt, MchProduct::getMchRate, MchProduct::getProductId).eq(MchProduct::getMchNo, getCurrentMchNo()).eq(MchProduct::getState, CS.YES));

        List<MchProduct> records = pages.getRecords();

        for (int i = 0; i < records.size(); i++) {
            Long productId = records.get(i).getProductId();
            String productName = productMap.get(productId).getProductName();
            records.get(i).addExt("productName", productName);
        }
        pages.setRecords(records);
        return ApiRes.ok(pages);
    }
}
