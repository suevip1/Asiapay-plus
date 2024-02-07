package com.jeequan.jeepay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.MchProduct;
import com.jeequan.jeepay.core.entity.Product;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.service.mapper.MchProductMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mysql.cj.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商户-产品关系表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
@Service
public class MchProductService extends ServiceImpl<MchProductMapper, MchProduct> {
    @Autowired
    private ProductService productService;

    @Autowired
    private MchInfoService mchInfoService;

    /**
     * 根据商户号 获取所有绑定关系，包括未绑定的
     *
     * @param mchNo
     * @return
     */
    public Map<Long, MchProduct> GetFullMchProductMap(String mchNo) {

        LambdaQueryWrapper<MchProduct> wrapper = MchProduct.gw();
        wrapper.eq(MchProduct::getMchNo, mchNo);

        List<MchProduct> listBlind = list(wrapper);
        Map<Long, MchProduct> productMchMap = new HashMap<>();

        for (int i = 0; i < listBlind.size(); i++) {
            productMchMap.put(listBlind.get(i).getProductId(), listBlind.get(i));
        }

        LambdaQueryWrapper<Product> productWrapper = Product.gw();
        productWrapper.orderByAsc(Product::getProductId);
        List<Product> productList = productService.list(productWrapper);

        Map<Long, MchProduct> resultMap = new HashMap<>();
        for (int i = 0; i < productList.size(); i++) {
            Product product = productList.get(i);
            MchProduct item = productMchMap.get(product.getProductId());
            if (item == null) {
                item = new MchProduct();
                item.setProductId(product.getProductId());
                item.setMchRate(BigDecimal.ZERO);
                item.setAgentRate(BigDecimal.ZERO);
                item.setState(CS.NO);
            }
            item.addExt("productName", product.getProductName());

            resultMap.put(product.getProductId(), item);
        }
        return resultMap;
    }

    /**
     * 根据产品号，查所有商户费率配置
     *
     * @param productId
     * @return
     */
    public Map<String, MchProduct> GetFullMchProductMap(Long productId) {

        LambdaQueryWrapper<MchProduct> wrapper = MchProduct.gw();
        wrapper.eq(MchProduct::getProductId, productId);

        List<MchProduct> listBlind = list(wrapper);
        Map<String, MchProduct> productMchMap = new HashMap<>();

        for (int i = 0; i < listBlind.size(); i++) {
            productMchMap.put(listBlind.get(i).getMchNo(), listBlind.get(i));
        }

        LambdaQueryWrapper<MchInfo> mchInfoLambdaQueryWrapper = MchInfo.gw();
        mchInfoLambdaQueryWrapper.orderByAsc(MchInfo::getCreatedAt);
        List<MchInfo> mchInfoList = mchInfoService.list(mchInfoLambdaQueryWrapper);

        Map<String, MchProduct> resultMap = new HashMap<>();
        for (int i = 0; i < mchInfoList.size(); i++) {
            MchInfo mchInfo = mchInfoList.get(i);
            MchProduct item = productMchMap.get(mchInfo.getMchNo());
            if (item == null) {
                item = new MchProduct();
                item.setMchNo(mchInfo.getMchNo());
                item.setMchRate(BigDecimal.ZERO);
                item.setAgentRate(BigDecimal.ZERO);
                item.setState(CS.NO);
            }
            item.addExt("mchName", mchInfo.getMchName());

            resultMap.put(mchInfo.getMchNo(), item);
        }
        return resultMap;
    }

}
