package com.jeequan.jeepay.service.impl;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.service.mapper.ProductMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 支付产品表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
@Slf4j
@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private PayPassageService passageService;
    @Autowired
    private MchProductService mchProductService;

    /**
     * 获取产品对应表
     *
     * @return
     */
    public Map<Long, Product> getProductMap() {
        List<Product> productList = list();
        Map<Long, Product> productMap = new HashMap<>();

        for (int i = 0; i < productList.size(); i++) {
            productMap.put(productList.get(i).getProductId(), productList.get(i));
        }
        return productMap;
    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = {Exception.class})
    public void removeUnusedProduct() {
        List<Product> list = list(Product.gw().eq(Product::getState, CS.HIDE));
        log.info("【过期产品数据定时清理任务开始执行】{}", new Date());
        for (int i = 0; i < list.size(); i++) {
            Long productId = list.get(i).getProductId();
            // 校验该支付产品是否有商户已配置通道或者已有订单
            if (passageService.count(PayPassage.gw().eq(PayPassage::getProductId, productId)) == 0
                    && payOrderService.count(PayOrder.gw().eq(PayOrder::getProductId, productId)) == 0) {
                mchProductService.remove(MchProduct.gw().eq(MchProduct::getProductId, productId));
                log.info("自动清理任务，清理产品成功: " + productId);
            }
        }
        log.info("【过期产品数据定时清理任务执行完成】{}", new Date());
    }
}