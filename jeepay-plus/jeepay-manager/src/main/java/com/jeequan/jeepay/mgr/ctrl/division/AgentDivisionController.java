package com.jeequan.jeepay.mgr.ctrl.division;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/agentDivision")
public class AgentDivisionController extends CommonCtrl {

    @Autowired
    private DivisionRecordService divisionRecordService;

    @Autowired
    private AgentAccountInfoService agentAccountInfoService;

    @Autowired
    private AgentAccountHistoryService agentAccountHistoryService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        try {
            DivisionRecord divisionRecord = getObject(DivisionRecord.class);
            JSONObject paramJSON = getReqParamJSON();

            LambdaQueryWrapper<DivisionRecord> wrapper = DivisionRecord.gw();
            wrapper.eq(DivisionRecord::getUserType, DivisionRecord.USER_TYPE_AGENT);
            if (StringUtils.isNotEmpty(divisionRecord.getUserNo())) {
                wrapper.like(DivisionRecord::getUserNo, divisionRecord.getUserNo());
            }
            if (divisionRecord.getState() != null) {
                wrapper.eq(DivisionRecord::getState, divisionRecord.getState());
            }

            if (paramJSON != null) {
                if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                    wrapper.ge(DivisionRecord::getCreatedAt, paramJSON.getString("createdStart"));
                }
                if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                    wrapper.le(DivisionRecord::getCreatedAt, paramJSON.getString("createdEnd"));
                }
            }
            wrapper.orderByDesc(DivisionRecord::getCreatedAt);

            IPage<DivisionRecord> pages = divisionRecordService.page(getIPage(), wrapper);

            return ApiRes.page(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }

    @PreAuthorize("hasAuthority('ENT_DIVISION_AGENT')")
    @RequestMapping(value = "/reviewOk/{recordId}", method = RequestMethod.POST)
    public ApiRes reviewOk(@PathVariable("recordId") Long recordId) {
        JSONObject jsonObject = getReqParamJSON();
        DivisionRecord divisionRecord = divisionRecordService.getById(recordId);
        if (divisionRecord.getState() != DivisionRecord.STATE_WAIT) {
            return ApiRes.customFail("申请账单状态错误");
        }
        String remark = jsonObject.getString("remark");
        if (StringUtils.isNotEmpty(remark)) {
            divisionRecord.setRemark(remark);
        }
        AgentAccountInfo agentAccountInfo = agentAccountInfoService.queryAgentInfo(divisionRecord.getUserNo());
        if (agentAccountInfo != null) {
            //更新订单状态
            divisionRecord.setState(DivisionRecord.STATE_SUCCESS);
            divisionRecordService.update(divisionRecord, DivisionRecord.gw().eq(DivisionRecord::getRecordId, divisionRecord.getRecordId()));

            agentAccountInfo.setFreezeBalance(agentAccountInfo.getFreezeBalance() - divisionRecord.getAmount());
            agentAccountInfo.setUpdatedAt(new Date());
            agentAccountInfoService.updateAgentInfo(agentAccountInfo);
        } else {
            return ApiRes.customFail("系统异常");
        }
        return ApiRes.ok();
    }


    /**
     * 拒接申请
     *
     * @param recordId
     * @return
     */
    @PreAuthorize("hasAuthority('ENT_DIVISION_AGENT')")
    @RequestMapping(value = "/reviewRefuse/{recordId}", method = RequestMethod.POST)
    public ApiRes reviewRefuse(@PathVariable("recordId") Long recordId) {
        JSONObject jsonObject = getReqParamJSON();
        DivisionRecord divisionRecord = divisionRecordService.getById(recordId);
        if (divisionRecord.getState() != DivisionRecord.STATE_WAIT) {
            return ApiRes.customFail("申请账单状态错误");
        }
        String remark = jsonObject.getString("remark");
        if (StringUtils.isNotEmpty(remark)) {
            divisionRecord.setRemark(remark);
        }
        //更新代理信息-冻结余额
        AgentAccountInfo agentAccountInfo = agentAccountInfoService.queryAgentInfo(divisionRecord.getUserNo());
        if (agentAccountInfo != null) {

            //增加代理资金流水记录
            Long amount = divisionRecord.getAmount();
            Long beforeBalance = agentAccountInfo.getBalance();
            Long afterBalance = agentAccountInfo.getBalance() + amount;

            //插入更新记录
            AgentAccountHistory agentAccountHistory = new AgentAccountHistory();
            agentAccountHistory.setAgentNo(agentAccountInfo.getAgentNo());
            agentAccountHistory.setAgentName(agentAccountInfo.getAgentName());
            agentAccountHistory.setAmount(amount);

            agentAccountHistory.setBeforeBalance(beforeBalance);
            agentAccountHistory.setAfterBalance(afterBalance);
            agentAccountHistory.setFundDirection(CS.FUND_DIRECTION_INCREASE);
            agentAccountHistory.setBizType(CS.BIZ_TYPE_UNFREEZE);
            agentAccountHistoryService.save(agentAccountHistory);


            //更新订单状态
            divisionRecord.setState(DivisionRecord.STATE_FAIL);
            divisionRecordService.update(divisionRecord, DivisionRecord.gw().eq(DivisionRecord::getRecordId, divisionRecord.getRecordId()));

            agentAccountInfo.setFreezeBalance(agentAccountInfo.getFreezeBalance() - divisionRecord.getAmount());
            agentAccountInfo.setUpdatedAt(new Date());
            agentAccountInfoService.updateBalance(agentAccountInfo.getAgentNo(), divisionRecord.getAmount());
            agentAccountInfoService.updateAgentInfo(agentAccountInfo);
        } else {
            return ApiRes.customFail("系统异常，商户不存在");
        }

        return ApiRes.ok();
    }

    @PreAuthorize("hasAuthority('ENT_DIVISION_AGENT')")
    @RequestMapping(value = "/count", method = RequestMethod.POST)
    public ApiRes count() {
        LambdaQueryWrapper<DivisionRecord> wrapper = DivisionRecord.gw();
        wrapper.eq(DivisionRecord::getUserType, DivisionRecord.USER_TYPE_AGENT);
        wrapper.eq(DivisionRecord::getState, DivisionRecord.STATE_WAIT);

        List<DivisionRecord> list = divisionRecordService.list(wrapper);
        Long totalAmount = 0L;
        for (int i = 0; i < list.size(); i++) {
            totalAmount += list.get(i).getAmount();
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("count", list.size());
        jsonObject.put("totalAmount", totalAmount);
        return ApiRes.ok(jsonObject);
    }
}