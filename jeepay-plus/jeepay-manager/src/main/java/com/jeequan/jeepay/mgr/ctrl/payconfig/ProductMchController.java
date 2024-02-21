package com.jeequan.jeepay.mgr.ctrl.payconfig;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.LimitRequest;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.AgentAccountInfo;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.MchProduct;
import com.jeequan.jeepay.core.entity.Product;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.AgentAccountInfoService;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.MchProductService;
import com.jeequan.jeepay.service.impl.ProductService;
import com.mysql.cj.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 产品-商户绑定
 */
@Slf4j
@RestController
@RequestMapping("/api/productMchInfo")
public class ProductMchController extends CommonCtrl {

    @Autowired
    private MchProductService mchProductService;

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private AgentAccountInfoService agentAccountInfoService;

    @GetMapping
    public ApiRes list() {
        try {
            JSONObject paramJSON = getReqParamJSON();
            String mchNo = paramJSON.getString("mchNo");
            String mchName = paramJSON.getString("mchName");
            String haveAgent = paramJSON.getString("haveAgent");
            String agentNo = paramJSON.getString("agentNo");

            Long productId = paramJSON.getLong("productId");

            LambdaQueryWrapper<MchProduct> wrapper = MchProduct.gw();
            wrapper.eq(MchProduct::getProductId, productId);

            IPage<MchProduct> pages = mchProductService.page(getIPage(true), wrapper);

            List<MchProduct> listBlind = mchProductService.list(wrapper);
            Map<String, MchProduct> mchProductMap = new HashMap<>();

            for (int i = 0; i < listBlind.size(); i++) {
                mchProductMap.put(listBlind.get(i).getMchNo(), listBlind.get(i));
            }

            LambdaQueryWrapper<MchInfo> mchInfoWrapper = MchInfo.gw();
            //查询参数 商户号
            if (!StringUtils.isNullOrEmpty(mchNo)) {
                mchInfoWrapper.eq(MchInfo::getMchNo, mchNo);
            }
            //查询参数 商户名
            if (!StringUtils.isNullOrEmpty(mchName)) {
                mchInfoWrapper.like(MchInfo::getMchName, mchName.trim());
            }
            //是否有代理 0 无 1有
            if (!StringUtils.isNullOrEmpty(haveAgent)) {
                if (haveAgent.equals("1")) {
                    mchInfoWrapper.ne(MchInfo::getAgentNo, "");
                } else {
                    mchInfoWrapper.eq(MchInfo::getAgentNo, "");
                }
            }
            //代理号
            if (!StringUtils.isNullOrEmpty(agentNo)) {
                mchInfoWrapper.eq(MchInfo::getAgentNo, agentNo);
            }
            //MchInfo.gw().orderByAsc(MchInfo::getCreatedAt)
            List<MchInfo> mchList = mchInfoService.list(mchInfoWrapper);
            Map<String, AgentAccountInfo> agentAccountInfoMap = agentAccountInfoService.getAgentInfoMap();

            List<MchProduct> result = new ArrayList<>();
            for (int i = 0; i < mchList.size(); i++) {
                MchProduct item = mchProductMap.get(mchList.get(i).getMchNo());
                if (item == null) {
                    item = new MchProduct();
                    item.setProductId(productId);
                    item.setMchNo(mchList.get(i).getMchNo());
                    item.setMchRate(BigDecimal.ZERO);
                    item.setAgentRate(BigDecimal.ZERO);
                    item.setState(CS.NO);
                }
                item.addExt("mchName", mchList.get(i).getMchName());
                String agentNoItem = mchList.get(i).getAgentNo();
                if (!StringUtils.isNullOrEmpty(agentNoItem)) {
                    item.addExt("agentNo", mchList.get(i).getAgentNo());
                    item.addExt("agentName", agentAccountInfoMap.get(mchList.get(i).getAgentNo()).getAgentName());
                } else {
                    item.addExt("agentNo", "");
                    item.addExt("agentName", "");
                }
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
    @MethodLog(remark = "更新产品-商户绑定信息")
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
    @MethodLog(remark = "产品-商户一键全绑定")
    @RequestMapping(value = "/blindAll/{productId}", method = RequestMethod.POST)
    @LimitRequest
    public ApiRes blindAll(@PathVariable("productId") Long productId) {
        List<MchProduct> listBlind = mchProductService.list(MchProduct.gw().eq(MchProduct::getProductId, productId));
        Map<String, MchProduct> productMchMap = new HashMap<>();

        for (int i = 0; i < listBlind.size(); i++) {
            productMchMap.put(listBlind.get(i).getMchNo(), listBlind.get(i));
        }
        List<MchInfo> mchList = mchInfoService.list(MchInfo.gw().orderByAsc(MchInfo::getCreatedAt));
        List<MchProduct> result = new ArrayList<>();
        for (int i = 0; i < mchList.size(); i++) {
            MchProduct item = productMchMap.get(mchList.get(i).getMchNo());
            if (item == null) {
                item = new MchProduct();
                item.setProductId(productId);
                item.setMchRate(BigDecimal.ZERO);
                item.setAgentRate(BigDecimal.ZERO);
                item.setState(CS.YES);
                item.setMchNo(mchList.get(i).getMchNo());
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
    @MethodLog(remark = "产品-商户一键全解绑")
    @RequestMapping(value = "/unBlindAll/{productId}", method = RequestMethod.POST)
    public ApiRes unBlindAll(@PathVariable("productId") Long productId) {
        List<MchProduct> listBlind = mchProductService.list(MchProduct.gw().eq(MchProduct::getProductId, productId));
        Map<String, MchProduct> productMchMap = new HashMap<>();

        for (int i = 0; i < listBlind.size(); i++) {
            productMchMap.put(listBlind.get(i).getMchNo(), listBlind.get(i));
        }
        List<MchInfo> mchList = mchInfoService.list(MchInfo.gw().orderByAsc(MchInfo::getCreatedAt));
        List<MchProduct> result = new ArrayList<>();
        for (int i = 0; i < mchList.size(); i++) {
            MchProduct item = productMchMap.get(mchList.get(i).getMchNo());
            if (item == null) {
                item = new MchProduct();
                item.setProductId(productId);
                item.setMchRate(BigDecimal.ZERO);
                item.setAgentRate(BigDecimal.ZERO);
                item.setState(CS.NO);
                item.setMchNo(mchList.get(i).getMchNo());
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
    @RequestMapping(value = "/setAllRate/{productId}", method = RequestMethod.POST)
    public ApiRes setAllRate(@PathVariable("productId") Long productId) {

        JSONObject reqJson = getReqParamJSON();
        String setAllRate = reqJson.getString("setAllRate");
        String setAllAgentRate = reqJson.getString("setAllAgentRate");
        Byte changeAllState = reqJson.getByte("changeAllState");
        JSONArray selectedIds = reqJson.getJSONArray("selectedIds");

        List<String> mchNoList = new LinkedList<>();

        if (selectedIds == null || selectedIds.isEmpty()) {
            return ApiRes.customFail("请先选中需要批量修改的商户");
        }

        for (int i = 0; i < selectedIds.size(); i++) {
            mchNoList.add(selectedIds.getString(i));
        }

        /**
         * 已有的绑定关系 mch product
         */
        List<MchProduct> listBlind = mchProductService.list(MchProduct.gw().eq(MchProduct::getProductId, productId));
        Map<String, MchProduct> productMchBlindMap = new HashMap<>();

        for (int i = 0; i < listBlind.size(); i++) {
            productMchBlindMap.put(listBlind.get(i).getMchNo(), listBlind.get(i));
        }

        List<MchProduct> result = new ArrayList<>();
        //需要批量操作的商户
        for (int i = 0; i < mchNoList.size(); i++) {
            MchProduct item = productMchBlindMap.get(mchNoList.get(i));
            if (item == null) {
                item = new MchProduct();
                item.setProductId(productId);
                item.setMchNo(mchNoList.get(i));
            }
            if (!StringUtils.isNullOrEmpty(setAllRate)) {
                item.setMchRate(new BigDecimal(setAllRate));
            }
            if (!StringUtils.isNullOrEmpty(setAllAgentRate)) {
                item.setAgentRate(new BigDecimal(setAllAgentRate));
            }
            if (changeAllState != null) {
                item.setState(changeAllState);
            }
            result.add(item);
        }
        boolean isSuccess = mchProductService.saveOrUpdateBatch(result);
        if (!isSuccess) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        return ApiRes.ok();
    }

}
