package com.jeequan.jeepay.agent.ctrl.product;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.agent.ctrl.CommonCtrl;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.MchProduct;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.entity.Product;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.service.impl.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 支付方式管理类
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@RestController
@RequestMapping("api/payWays")
public class PayWayController extends CommonCtrl {

    @Autowired
    private ProductService productService;
    @Autowired
    MchPayPassageService mchPayPassageService;
    @Autowired
    PayOrderService payOrderService;

    @Autowired
    PayPassageService passageService;

    @Autowired
    MchProductService mchProductService;

    @GetMapping
    public ApiRes list() {
        try {
            Product queryObject = getObject(Product.class);
            LambdaQueryWrapper<Product> condition = Product.gw();
            if (queryObject.getProductId() != null) {
                condition.like(Product::getProductId, queryObject.getProductId());
            }
            if (StringUtils.isNotEmpty(queryObject.getProductName())) {
                condition.like(Product::getProductName, queryObject.getProductName().trim());
            }
            if (queryObject.getState() != null) {
                condition.like(Product::getState, queryObject.getState());
            }
            if (queryObject.getLimitState() != null) {
                condition.like(Product::getLimitState, queryObject.getLimitState());
            }
            condition.orderByAsc(Product::getProductId);

            IPage<Product> pages = productService.page(getIPage(true), condition);

            return ApiRes.page(pages);
        } catch (Exception e) {
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }


    @GetMapping("/{wayCode}")
    public ApiRes detail(@PathVariable("wayCode") Long wayCode) {
        return ApiRes.ok(productService.getById(wayCode));
    }

}