package com.jeequan.jeepay.mgr.ctrl.merchant;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchPayPassage;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.MchPayPassageService;
import com.jeequan.jeepay.service.impl.PayPassageService;
import com.mysql.cj.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    @PreAuthorize("hasAuthority('ENT_MCH_INFO_EDIT')")
    @GetMapping
    public ApiRes list() {
        try {
            PayPassage queryObj = getObject(PayPassage.class);

            JSONObject paramJSON = getReqParamJSON();
            String mchNo = paramJSON.getString("mchNo");

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
                payPassageWrapper.like(PayPassage::getPayPassageName, queryObj.getPayPassageName());
            }
            List<PayPassage> passageList = payPassageService.list(payPassageWrapper.orderByAsc(PayPassage::getPayPassageId));

            List<MchPayPassage> result = new ArrayList<>();
            for (int i = 0; i < passageList.size(); i++) {
                MchPayPassage item = passageMchMap.get(passageList.get(i).getPayPassageId());
                if (item == null) {
                    item = new MchPayPassage();
                    item.setPayPassageId(passageList.get(i).getPayPassageId());
                    item.setMchNo(mchNo);
                    item.setState(CS.NO);
                }
                item.addExt("payPassageName", passageList.get(i).getPayPassageName());
                item.addExt("rate", passageList.get(i).getRate());
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

}