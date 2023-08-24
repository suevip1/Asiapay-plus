package com.jeequan.jeepay.mgr.ctrl.merchant;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.AgentAccountHistory;
import com.jeequan.jeepay.core.entity.MchHistory;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/mchHistory")
public class MchHistoryController extends CommonCtrl {

    @Autowired
    private MchHistoryService mchHistoryService;

    /**
     * 查看代理商资金流水
     *
     * @return
     */
    @PreAuthorize("hasAnyAuthority('ENT_MCH_INFO_HISTORY')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        try {
            MchHistory mchHistory = getObject(MchHistory.class);
            LambdaQueryWrapper<MchHistory> wrapper = MchHistory.gw();
            JSONObject paramJSON = getReqParamJSON();

            //商号
            if (StringUtils.isNotEmpty(mchHistory.getMchNo())) {
                wrapper.eq(MchHistory::getMchNo, mchHistory.getMchNo());
            }

            //订单
            if (StringUtils.isNotEmpty(mchHistory.getPayOrderId())) {
                wrapper.eq(MchHistory::getPayOrderId, mchHistory.getPayOrderId());
            }
            //资金方向
            if (mchHistory.getFundDirection() != null && mchHistory.getFundDirection() != 0) {
                wrapper.eq(MchHistory::getFundDirection, mchHistory.getFundDirection());
            }
            //业务类型
            if (mchHistory.getBizType() != null && mchHistory.getBizType() != 0) {
                wrapper.eq(MchHistory::getBizType, mchHistory.getBizType());
            }

            if (paramJSON != null) {
                if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                    wrapper.ge(MchHistory::getCreatedAt, paramJSON.getString("createdStart"));
                }
                if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                    wrapper.le(MchHistory::getCreatedAt, paramJSON.getString("createdEnd"));
                }
            }
            wrapper.orderByDesc(MchHistory::getCreatedAt);

            IPage<AgentAccountHistory> pages = mchHistoryService.page(getIPage(true), wrapper);

            return ApiRes.page(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }
}