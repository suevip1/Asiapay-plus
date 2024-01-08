package com.jeequan.jeepay.mgr.ctrl.merchant;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 只用订单页查所有通道接口数据
 */
@RestController
@RequestMapping("/api/mchAppsList")
public class PassageListController extends CommonCtrl {

    @Autowired
    private PayPassageService payPassageService;


    /**
     * 所有应用列表
     *
     * @return
     */
    @PreAuthorize("hasAuthority('ENT_MCH_APP_LIST')")
    @GetMapping
    public ApiRes list() {
        try {
            LambdaQueryWrapper<PayPassage> wrapper = PayPassage.gw();

            wrapper.select(PayPassage::getPayPassageId, PayPassage::getPayPassageName);

            List<PayPassage> payPassageList = payPassageService.list(wrapper);

            return ApiRes.ok(payPassageList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }
}
