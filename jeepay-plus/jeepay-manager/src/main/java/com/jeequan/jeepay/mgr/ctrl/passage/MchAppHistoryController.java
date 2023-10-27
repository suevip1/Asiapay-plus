package com.jeequan.jeepay.mgr.ctrl.passage;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.AgentAccountHistory;
import com.jeequan.jeepay.core.entity.MchHistory;
import com.jeequan.jeepay.core.entity.PassageTransactionHistory;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchHistoryService;
import com.jeequan.jeepay.service.impl.PassageTransactionHistoryService;
import com.jeequan.jeepay.service.impl.PayPassageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/passageHistory")
public class MchAppHistoryController extends CommonCtrl {

    @Autowired
    private PassageTransactionHistoryService passageTransactionHistoryService;

    @Autowired
    private PayPassageService payPassageService;


    /**
     * 查看代理商资金流水
     *
     * @return
     */
    @PreAuthorize("hasAnyAuthority('ENT_PASSAGE_HISTORY')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        try {
            PassageTransactionHistory passageTransactionHistory = getObject(PassageTransactionHistory.class);
            LambdaQueryWrapper<PassageTransactionHistory> wrapper = PassageTransactionHistory.gw();
            JSONObject paramJSON = getReqParamJSON();

            //通道名
            if (StringUtils.isNotEmpty(passageTransactionHistory.getPayPassageName())) {
                wrapper.like(PassageTransactionHistory::getPayPassageName, passageTransactionHistory.getPayPassageName().trim());
            }

            //通道ID
            if (passageTransactionHistory.getPayPassageId() != null) {
                wrapper.eq(PassageTransactionHistory::getPayPassageId, passageTransactionHistory.getPayPassageId());
            }
            //资金方向
            if (passageTransactionHistory.getFundDirection() != null && passageTransactionHistory.getFundDirection() != 0) {
                wrapper.eq(PassageTransactionHistory::getFundDirection, passageTransactionHistory.getFundDirection());
            }
            //业务类型
            if (passageTransactionHistory.getBizType() != null && passageTransactionHistory.getBizType() != 0) {
                wrapper.eq(PassageTransactionHistory::getBizType, passageTransactionHistory.getBizType());
            }

            if (paramJSON != null) {
                if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                    wrapper.ge(PassageTransactionHistory::getCreatedAt, paramJSON.getString("createdStart"));
                }
                if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                    wrapper.le(PassageTransactionHistory::getCreatedAt, paramJSON.getString("createdEnd"));
                }
            }
            wrapper.orderByDesc(PassageTransactionHistory::getCreatedAt);

            IPage<PassageTransactionHistory> pages = passageTransactionHistoryService.page(getIPage(true), wrapper);

            return ApiRes.page(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }
}