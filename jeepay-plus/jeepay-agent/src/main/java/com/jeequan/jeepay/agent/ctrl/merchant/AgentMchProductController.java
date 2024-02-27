package com.jeequan.jeepay.agent.ctrl.merchant;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.agent.ctrl.CommonCtrl;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.MchProduct;
import com.jeequan.jeepay.core.entity.Product;
import com.jeequan.jeepay.core.entity.StatisticsAgentMch;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.MchProductService;
import com.jeequan.jeepay.service.impl.ProductService;
import com.jeequan.jeepay.service.impl.StatisticsAgentMchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/mchProduct")
public class AgentMchProductController extends CommonCtrl {

    @Autowired
    private MchProductService mchProductService;

    @Autowired
    private ProductService productService;


    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        try {
            MchProduct mchProduct = getObject(MchProduct.class);
            String mchNo = mchProduct.getMchNo();
            LambdaQueryWrapper<MchProduct> wrapper = MchProduct.gw();

            wrapper.eq(MchProduct::getMchNo, mchNo);
            wrapper.eq(MchProduct::getState, CS.YES);

            if (mchProduct.getProductId() != null) {
                wrapper.eq(MchProduct::getProductId, mchProduct.getProductId());
            }

            wrapper.orderByAsc(MchProduct::getProductId);

            IPage<MchProduct> pages = mchProductService.page(getIPage(), wrapper);

            Map<Long, Product> productMap = productService.getProductMap();
            List<MchProduct> records = new ArrayList<>();
            for (int i = 0; i < pages.getRecords().size(); i++) {
                MchProduct item = pages.getRecords().get(i);
                Product product = productMap.get(item.getProductId());
                item.addExt("productName", product.getProductName());
                records.add(item);
            }
            pages.setRecords(records);
            return ApiRes.page(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }
}