package com.jeequan.jeepay.agent.ctrl.division;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.agent.ctrl.CommonCtrl;
import com.jeequan.jeepay.core.aop.LimitRequest;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.AgentAccountHistory;
import com.jeequan.jeepay.core.entity.AgentAccountInfo;
import com.jeequan.jeepay.core.entity.DivisionRecord;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.PayConfig;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.service.impl.AgentAccountHistoryService;
import com.jeequan.jeepay.service.impl.AgentAccountInfoService;
import com.jeequan.jeepay.service.impl.DivisionRecordService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;


@RestController
@RequestMapping("/api/agentDivision")
public class AgentDivisionController extends CommonCtrl {

    @Autowired
    private DivisionRecordService divisionRecordService;
    @Autowired
    private AgentAccountInfoService agentAccountInfoService;

    @Autowired
    private AgentAccountHistoryService agentAccountHistoryService;

    @Autowired
    private SysConfigService sysConfigService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        try {
            JSONObject paramJSON = getReqParamJSON();
            DivisionRecord divisionRecord = getObject(DivisionRecord.class);
            LambdaQueryWrapper<DivisionRecord> wrapper = DivisionRecord.gw();
            wrapper.eq(DivisionRecord::getUserNo, getCurrentAgentNo()).eq(DivisionRecord::getUserType, DivisionRecord.USER_TYPE_AGENT);
            if (divisionRecord.getRecordId() != null) {
                wrapper.eq(DivisionRecord::getRecordId, divisionRecord.getRecordId());
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

    /**
     * 新增商户信息
     *
     * @return
     */
    @MethodLog(remark = "代理结算申请")
    @RequestMapping(value = "", method = RequestMethod.POST)
    @LimitRequest
    public ApiRes add() {
        DivisionRecord divisionRecord = getObject(DivisionRecord.class);
        AgentAccountInfo agentAccountInfo = agentAccountInfoService.queryAgentInfo(getCurrentAgentNo());

        if (agentAccountInfo.getState() == CS.NO) {
            return ApiRes.customFail("代理状态异常!");
        }

        if ((agentAccountInfo.getBalance() < divisionRecord.getAmount())) {
            return ApiRes.customFail("申请金额大于可提现余额!");
        }

        PayConfig payConfig = sysConfigService.getPayConfig();

        if (payConfig.getAgentMinWithdraw() > divisionRecord.getAmount()) {
            return ApiRes.customFail("申请金额小于最小提现限制，最小提现金额![" + AmountUtil.convertCent2DollarShort(payConfig.getAgentMinWithdraw()) + "]元");
        }

        Long fee = AmountUtil.calPercentageFee(divisionRecord.getAmount(), payConfig.getAgentFeeRate()) + payConfig.getAgentFee();

        //检测最小提现额度，算出手续费
        Long divisionAmount = divisionRecord.getAmount() - fee;
        if (divisionAmount <= 0) {
            return ApiRes.customFail("申请金额过小无法体现!需要手续费[" + AmountUtil.convertCent2DollarShort(fee) + "]元");
        }

        Date submitDate = new Date();
        boolean isSuccess = divisionRecordService.SaveDivisionRecord(agentAccountInfo.getAgentNo(), agentAccountInfo.getAgentName(), divisionRecord.getAmount(), fee, divisionRecord.getRemark(), DivisionRecord.USER_TYPE_AGENT, submitDate);
        if (isSuccess) {
            //增加代理资金流水记录
            agentAccountInfo = agentAccountInfoService.queryAgentInfo(getCurrentAgentNo());
            Long amount = divisionRecord.getAmount();
            Long beforeBalance = agentAccountInfo.getBalance();
            Long afterBalance = agentAccountInfo.getBalance() - amount;

            //插入更新记录
            AgentAccountHistory agentAccountHistory = new AgentAccountHistory();
            agentAccountHistory.setAgentNo(agentAccountInfo.getAgentNo());
            agentAccountHistory.setAgentName(agentAccountInfo.getAgentName());
            agentAccountHistory.setAmount(-amount);
            agentAccountHistory.setCreatedAt(submitDate);
            agentAccountHistory.setBeforeBalance(beforeBalance);
            agentAccountHistory.setAfterBalance(afterBalance);
            agentAccountHistory.setFundDirection(CS.FUND_DIRECTION_REDUCE);
            agentAccountHistory.setBizType(CS.BIZ_TYPE_WITHDRAW);
            agentAccountHistoryService.save(agentAccountHistory);

            agentAccountInfo.setFreezeBalance(agentAccountInfo.getFreezeBalance() + divisionRecord.getAmount());
            agentAccountInfoService.updateBalance(agentAccountInfo.getAgentNo(), -divisionRecord.getAmount());
            agentAccountInfoService.updateAgentInfo(agentAccountInfo);

            return ApiRes.ok();
        }
        return ApiRes.fail(ApiCodeEnum.DB_ERROR);
    }

    @RequestMapping(value = "/getConfig", method = RequestMethod.POST)
    public ApiRes getConfig() {
        PayConfig payConfig = sysConfigService.getPayConfig();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("agentFee", payConfig.getAgentFee());
        jsonObject.put("agentFeeRate", payConfig.getAgentFeeRate());
        jsonObject.put("agentMinWithdraw", payConfig.getAgentMinWithdraw());
        return ApiRes.ok(jsonObject);
    }
}