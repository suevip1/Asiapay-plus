package com.jeequan.jeepay.mgr.ctrl.merchant;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.MchProduct;
import com.jeequan.jeepay.core.entity.Product;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户统计相关
 */
@RestController
@RequestMapping("/api/mchStatInfo")
public class MchInfoStatController extends CommonCtrl {

    @Autowired
    private MchInfoService mchInfoService;

    /**
     * 商户信息列表
     *
     * @return
     */
    @PreAuthorize("hasAuthority('ENT_C_MAIN_PAY_COUNT')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        LambdaQueryWrapper<MchInfo> wrapper = MchInfo.gw();
        wrapper.ne(MchInfo::getState,CS.HIDE);
        wrapper.orderByDesc(MchInfo::getBalance);
        IPage<MchInfo> pages = mchInfoService.page(getIPage(), wrapper);
        return ApiRes.page(pages);
    }

    @PreAuthorize("hasAuthority('ENT_MCH_LIST')")
    @RequestMapping(value = "/statMchInfo", method = RequestMethod.GET)
    public ApiRes statMchInfo() {
        return ApiRes.ok(mchInfoService.sumMchInfo());
    }
}