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
import com.jeequan.jeepay.service.impl.*;
import com.mysql.cj.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;


/**
 * 通道-商户绑定
 */
@RestController
@RequestMapping("/api/mchPassageInfo")
public class MchPassageController extends CommonCtrl {

    @Autowired
    private MchPayPassageService mchPayPassageService;

    @Autowired
    private PayPassageService payPassageService;

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private AgentAccountInfoService agentAccountInfoService;

    @Autowired
    private MchProductService mchProductService;

    @PreAuthorize("hasAuthority('ENT_MCH_INFO_EDIT')")
    @GetMapping
    public ApiRes list() {
        try {
            PayPassage queryObj = getObject(PayPassage.class);

            JSONObject paramJSON = getReqParamJSON();
            String mchNo = paramJSON.getString("mchNo");
            String haveAgent = paramJSON.getString("haveAgent");

            LambdaQueryWrapper<MchPayPassage> wrapper = MchPayPassage.gw();
            wrapper.eq(MchPayPassage::getMchNo, mchNo);

            IPage<MchPayPassage> pages = mchPayPassageService.page(getIPage(true), wrapper);

            List<MchPayPassage> listBlind = mchPayPassageService.list(wrapper);
            Map<Long, MchPayPassage> passageMchMap = new HashMap<>();

            for (int i = 0; i < listBlind.size(); i++) {
                passageMchMap.put(listBlind.get(i).getPayPassageId(), listBlind.get(i));
            }

            LambdaQueryWrapper<PayPassage> payPassageWrapper = PayPassage.gw();
            //查询参数
            if (queryObj.getPayPassageId() != null) {
                payPassageWrapper.eq(PayPassage::getPayPassageId, queryObj.getPayPassageId());
            }

            if (!StringUtils.isNullOrEmpty(queryObj.getPayPassageName())) {
                payPassageWrapper.like(PayPassage::getPayPassageName, queryObj.getPayPassageName().trim());
            }
            //是否有代理 0 无 1有
            if (!StringUtils.isNullOrEmpty(haveAgent)) {
                if (haveAgent.equals("1")) {
                    payPassageWrapper.ne(PayPassage::getAgentNo, "");
                } else {
                    payPassageWrapper.eq(PayPassage::getAgentNo, "");
                }
            }
            List<PayPassage> passageList = payPassageService.list(payPassageWrapper.orderByAsc(PayPassage::getPayPassageId));
            Map<String, AgentAccountInfo> agentAccountInfoMap = agentAccountInfoService.getAgentInfoMap();
            List<MchPayPassage> result = new ArrayList<>();

            //商户-产品费率关系表
            Map<Long, MchProduct> mchProductMap = mchProductService.GetFullMchProductMap(mchNo);
            for (int i = 0; i < passageList.size(); i++) {
                MchPayPassage item = passageMchMap.get(passageList.get(i).getPayPassageId());
                if (item == null) {
                    item = new MchPayPassage();
                    item.setPayPassageId(passageList.get(i).getPayPassageId());
                    item.setMchNo(mchNo);
                    item.setState(CS.NO);
                }
                item.addExt("payPassageName", passageList.get(i).getPayPassageName());

                String passageAgentNo = passageList.get(i).getAgentNo();
                if (!StringUtils.isNullOrEmpty(passageAgentNo)) {
                    item.addExt("passageAgentNo", passageAgentNo);
                    item.addExt("passageAgentName", agentAccountInfoMap.get(passageAgentNo).getAgentName());
                } else {
                    item.addExt("passageAgentNo", "");
                    item.addExt("passageAgentName", "");
                }

                item.addExt("rate", passageList.get(i).getRate());
                //添加产品-商户费率
                BigDecimal productRate = mchProductMap.get(passageList.get(i).getProductId()).getMchRate();
                item.addExt("productRate", productRate);

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

    @PreAuthorize("hasAuthority('ENT_MCH_APP_EDIT')")
    @MethodLog(remark = "更新商户-通道绑定信息")
    @PutMapping
    @LimitRequest
    public ApiRes update() {
        MchPayPassage mchPayPassage = getObject(MchPayPassage.class);
        List<MchPayPassage> listBlind = mchPayPassageService.list(MchPayPassage.gw().eq(MchPayPassage::getMchNo, mchPayPassage.getMchNo()).eq(MchPayPassage::getPayPassageId, mchPayPassage.getPayPassageId()));
        if (listBlind != null) {
            if (listBlind.size() == 0) {
                boolean result = mchPayPassageService.saveOrUpdate(mchPayPassage);
                if (result) {
                    return ApiRes.ok();
                }
            } else if (listBlind.size() == 1) {
                mchPayPassage.setMchPayPassageId(listBlind.get(0).getMchPayPassageId());
                boolean result = mchPayPassageService.saveOrUpdate(mchPayPassage);
                if (result) {
                    return ApiRes.ok();
                }
            }

        }
        return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
    }

    @PreAuthorize("hasAuthority('ENT_MCH_APP_EDIT')")
    @MethodLog(remark = "商户-通道一键全绑定")
    @RequestMapping(value = "/blindAll/{mchNo}", method = RequestMethod.POST)
    @LimitRequest
    public ApiRes blindAll(@PathVariable("mchNo") String mchNo) {
        List<MchPayPassage> listBlind = mchPayPassageService.list(MchPayPassage.gw().eq(MchPayPassage::getMchNo, mchNo));
        Map<Long, MchPayPassage> payPassageMchMap = new HashMap<>();

        for (int i = 0; i < listBlind.size(); i++) {
            payPassageMchMap.put(listBlind.get(i).getPayPassageId(), listBlind.get(i));
        }

        List<PayPassage> passageList = payPassageService.list(PayPassage.gw().orderByAsc(PayPassage::getPayPassageId));
        List<MchPayPassage> result = new ArrayList<>();
        for (int i = 0; i < passageList.size(); i++) {
            MchPayPassage item = payPassageMchMap.get(passageList.get(i).getPayPassageId());
            if (item == null) {
                item = new MchPayPassage();
                item.setPayPassageId(passageList.get(i).getPayPassageId());
                item.setState(CS.YES);
                item.setMchNo(mchNo);
            }
            item.setState(CS.YES);
            result.add(item);
        }
        boolean isSuccess = mchPayPassageService.saveOrUpdateBatch(result);
        if (!isSuccess) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        return ApiRes.ok();
    }

    @PreAuthorize("hasAuthority('ENT_MCH_APP_EDIT')")
    @MethodLog(remark = "商户-通道一键全解绑")
    @RequestMapping(value = "/unBlindAll/{mchNo}", method = RequestMethod.POST)
    public ApiRes unBlindAll(@PathVariable("mchNo") String mchNo) {
        List<MchPayPassage> listBlind = mchPayPassageService.list(MchPayPassage.gw().eq(MchPayPassage::getMchNo, mchNo));
        Map<Long, MchPayPassage> payPassageMchMap = new HashMap<>();

        for (int i = 0; i < listBlind.size(); i++) {
            payPassageMchMap.put(listBlind.get(i).getPayPassageId(), listBlind.get(i));
        }

        List<PayPassage> passageList = payPassageService.list(PayPassage.gw().orderByAsc(PayPassage::getPayPassageId));
        List<MchPayPassage> result = new ArrayList<>();
        for (int i = 0; i < passageList.size(); i++) {
            MchPayPassage item = payPassageMchMap.get(passageList.get(i).getPayPassageId());
            if (item == null) {
                item = new MchPayPassage();
                item.setPayPassageId(passageList.get(i).getPayPassageId());
                item.setState(CS.NO);
                item.setMchNo(mchNo);
            }
            item.setState(CS.NO);
            result.add(item);
        }
        boolean isSuccess = mchPayPassageService.saveOrUpdateBatch(result);
        if (!isSuccess) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        return ApiRes.ok();
    }

    @PreAuthorize("hasAuthority('ENT_MCH_APP_EDIT')")
    @RequestMapping(value = "/setAll/{mchNo}", method = RequestMethod.POST)
    public ApiRes setAll(@PathVariable("mchNo") String mchNo) {

        JSONObject reqJson = getReqParamJSON();
        Byte changeAllState = reqJson.getByte("changeAllState");
        JSONArray selectedIds = reqJson.getJSONArray("selectedIds");

        List<Long> passageIdList = new LinkedList<>();

        if (selectedIds == null || selectedIds.size() == 0) {
            return ApiRes.customFail("请先选中需要批量修改的商户");
        }

        for (int i = 0; i < selectedIds.size(); i++) {
            passageIdList.add(selectedIds.getLong(i));
        }

        /**
         * 已有的绑定关系 mch passage
         */
        List<MchPayPassage> listBlind = mchPayPassageService.list(MchPayPassage.gw().eq(MchPayPassage::getMchNo, mchNo));
        Map<Long, MchPayPassage> passageMchBlindMap = new HashMap<>();

        for (int i = 0; i < listBlind.size(); i++) {
            passageMchBlindMap.put(listBlind.get(i).getPayPassageId(), listBlind.get(i));
        }

        List<MchPayPassage> result = new ArrayList<>();
        //需要批量操作的商户
        for (int i = 0; i < passageIdList.size(); i++) {
            MchPayPassage item = passageMchBlindMap.get(passageIdList.get(i));
            if (item == null) {
                item = new MchPayPassage();
                item.setPayPassageId(passageIdList.get(i));
                item.setMchNo(mchNo);
            }
            item.setState(changeAllState);
            result.add(item);
        }
        boolean isSuccess = mchPayPassageService.saveOrUpdateBatch(result);
        if (!isSuccess) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        return ApiRes.ok();
    }


}