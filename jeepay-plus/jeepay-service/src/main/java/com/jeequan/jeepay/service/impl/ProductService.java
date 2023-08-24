package com.jeequan.jeepay.service.impl;

import com.jeequan.jeepay.core.entity.Product;
import com.jeequan.jeepay.service.mapper.ProductMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    /**
     * 获取产品对应表
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
}