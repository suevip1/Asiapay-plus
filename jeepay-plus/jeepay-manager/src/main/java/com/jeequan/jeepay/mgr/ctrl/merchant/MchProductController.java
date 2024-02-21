package com.jeequan.jeepay.mgr.ctrl.merchant;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.LimitRequest;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchProductService;
import com.jeequan.jeepay.service.impl.ProductService;
import com.mysql.cj.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/mchProductInfo")
public class MchProductController extends CommonCtrl {

    @Autowired
    private ProductService productService;

    @Autowired
    private MchProductService mchProductService;

    @PreAuthorize("hasAuthority('ENT_MCH_INFO_EDIT')")
    @GetMapping
    public ApiRes list() {
        try {
            JSONObject paramJSON = getReqParamJSON();
            String mchNo = paramJSON.getString("mchNo");
            String productName = paramJSON.getString("productName");
            Long productId = paramJSON.getLong("productId");

            LambdaQueryWrapper<MchProduct> wrapper = MchProduct.gw();
            wrapper.eq(MchProduct::getMchNo, mchNo);

            IPage<MchProduct> pages = mchProductService.page(getIPage(true), wrapper);

            List<MchProduct> listBlind = mchProductService.list(wrapper);
            Map<Long, MchProduct> productMchMap = new HashMap<>();

            for (int i = 0; i < listBlind.size(); i++) {
                productMchMap.put(listBlind.get(i).getProductId(), listBlind.get(i));
            }
            LambdaQueryWrapper<Product> productWrapper = Product.gw();
            productWrapper.ne(Product::getState, CS.HIDE);
            //查询参数 商户号
            if (productId != null) {
                productWrapper.eq(Product::getProductId, productId);
            }
            //查询参数 商户名
            if (!StringUtils.isNullOrEmpty(productName)) {
                productWrapper.like(Product::getProductName, productName.trim());
            }
            productWrapper.orderByAsc(Product::getProductId);

            List<Product> productList = productService.list(productWrapper);

            List<MchProduct> result = new ArrayList<>();
            for (int i = 0; i < productList.size(); i++) {
                MchProduct item = productMchMap.get(productList.get(i).getProductId());
                if (item == null) {
                    item = new MchProduct();
                    item.setProductId(productList.get(i).getProductId());
                    item.setMchRate(BigDecimal.ZERO);
                    item.setAgentRate(BigDecimal.ZERO);
                    item.setState(CS.NO);
                }
                item.addExt("productName", productList.get(i).getProductName());
                result.add(item);
            }
            pages.setTotal(result.size());
            pages.setRecords(result);
            return ApiRes.page(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }

    @PreAuthorize("hasAuthority('ENT_PC_WAY_EDIT')")
    @MethodLog(remark = "更新商户-产品绑定信息")
    @PutMapping
    @LimitRequest
    public ApiRes update() {
        MchProduct mchProduct = getObject(MchProduct.class);
        List<MchProduct> listBlind = mchProductService.list(MchProduct.gw().eq(MchProduct::getMchNo, mchProduct.getMchNo()).eq(MchProduct::getProductId, mchProduct.getProductId()));
        if (listBlind != null) {
            if (listBlind.size() == 0) {
                boolean result = mchProductService.saveOrUpdate(mchProduct);
                if (result) {
                    return ApiRes.ok();
                }
            } else if (listBlind.size() == 1) {
                mchProduct.setMchProductId(listBlind.get(0).getMchProductId());
                boolean result = mchProductService.saveOrUpdate(mchProduct);
                if (result) {
                    return ApiRes.ok();
                }
            }

        }
        return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
    }

    @PreAuthorize("hasAuthority('ENT_PC_WAY_EDIT')")
    @MethodLog(remark = "商户-产品一键全绑定")
    @RequestMapping(value = "/blindAll/{mchNo}", method = RequestMethod.POST)
    @LimitRequest
    public ApiRes blindAll(@PathVariable("mchNo") String mchNo) {
        List<MchProduct> listBlind = mchProductService.list(MchProduct.gw().eq(MchProduct::getMchNo, mchNo));
        Map<Long, MchProduct> productMchMap = new HashMap<>();

        for (int i = 0; i < listBlind.size(); i++) {
            productMchMap.put(listBlind.get(i).getProductId(), listBlind.get(i));
        }

        List<Product> productList = productService.list(Product.gw().orderByAsc(Product::getProductId));
        List<MchProduct> result = new ArrayList<>();
        for (int i = 0; i < productList.size(); i++) {
            MchProduct item = productMchMap.get(productList.get(i).getProductId());
            if (item == null) {
                item = new MchProduct();
                item.setProductId(productList.get(i).getProductId());
                item.setMchRate(BigDecimal.ZERO);
                item.setAgentRate(BigDecimal.ZERO);
                item.setState(CS.YES);
                item.setMchNo(mchNo);
            }
            item.setState(CS.YES);
            result.add(item);
        }
        boolean isSuccess = mchProductService.saveOrUpdateBatch(result);
        if (!isSuccess) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        return ApiRes.ok();
    }

    @PreAuthorize("hasAuthority('ENT_PC_WAY_EDIT')")
    @MethodLog(remark = "商户-产品一键全解绑")
    @RequestMapping(value = "/unBlindAll/{mchNo}", method = RequestMethod.POST)
    public ApiRes unBlindAll(@PathVariable("mchNo") String mchNo) {
        List<MchProduct> listBlind = mchProductService.list(MchProduct.gw().eq(MchProduct::getMchNo, mchNo));
        Map<Long, MchProduct> productMchMap = new HashMap<>();

        for (int i = 0; i < listBlind.size(); i++) {
            productMchMap.put(listBlind.get(i).getProductId(), listBlind.get(i));
        }

        List<Product> productList = productService.list(Product.gw().orderByAsc(Product::getProductId));
        List<MchProduct> result = new ArrayList<>();
        for (int i = 0; i < productList.size(); i++) {
            MchProduct item = productMchMap.get(productList.get(i).getProductId());
            if (item == null) {
                item = new MchProduct();
                item.setProductId(productList.get(i).getProductId());
                item.setMchRate(BigDecimal.ZERO);
                item.setAgentRate(BigDecimal.ZERO);
                item.setState(CS.NO);
                item.setMchNo(mchNo);
            }
            item.setState(CS.NO);
            result.add(item);
        }
        boolean isSuccess = mchProductService.saveOrUpdateBatch(result);
        if (!isSuccess) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        return ApiRes.ok();
    }

    @PreAuthorize("hasAuthority('ENT_PC_WAY_EDIT')")
    @RequestMapping(value = "/setAllRate/{mchNo}", method = RequestMethod.POST)
    public ApiRes setAllRate(@PathVariable("mchNo") String mchNo) {

        JSONObject reqJson = getReqParamJSON();
        String setAllRate = reqJson.getString("setAllRate");
        String setAllAgentRate = reqJson.getString("setAllAgentRate");
        Byte changeAllState = reqJson.getByte("changeAllState");
        JSONArray selectedIds = reqJson.getJSONArray("selectedIds");

        List<Long> productIdList = new LinkedList<>();

        if (selectedIds == null || selectedIds.size() == 0) {
            return ApiRes.customFail("请先选中需要批量修改的商户");
        }

        for (int i = 0; i < selectedIds.size(); i++) {
            productIdList.add(selectedIds.getLong(i));
        }

        /**
         * 已有的绑定关系 mch product
         */
        List<MchProduct> listBlind = mchProductService.list(MchProduct.gw().eq(MchProduct::getMchNo, mchNo));
        Map<Long, MchProduct> productMchBlindMap = new HashMap<>();

        for (int i = 0; i < listBlind.size(); i++) {
            productMchBlindMap.put(listBlind.get(i).getProductId(), listBlind.get(i));
        }

        List<MchProduct> result = new ArrayList<>();
        //需要批量操作的商户
        for (int i = 0; i < productIdList.size(); i++) {
            MchProduct item = productMchBlindMap.get(productIdList.get(i));
            if (item == null) {
                item = new MchProduct();
                item.setProductId(productIdList.get(i));
                item.setMchNo(mchNo);
            }
            item.setMchRate(new BigDecimal(setAllRate));
            item.setAgentRate(new BigDecimal(setAllAgentRate));
            item.setState(changeAllState);
            result.add(item);
        }
        boolean isSuccess = mchProductService.saveOrUpdateBatch(result);
        if (!isSuccess) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        return ApiRes.ok();
    }
}
