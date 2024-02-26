package com.jeequan.jeepay.mch.ctrl.division;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.LimitRequest;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.PayConfig;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.DivisionRecordService;
import com.jeequan.jeepay.service.impl.MchHistoryService;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/mchDivision")
public class MchDivisionController extends CommonCtrl {

    @Autowired
    private DivisionRecordService divisionRecordService;
    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private MchHistoryService mchHistoryService;

    @Autowired
    private SysConfigService sysConfigService;


    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {

        JSONObject paramJSON = getReqParamJSON();
        DivisionRecord divisionRecord = getObject(DivisionRecord.class);

        LambdaQueryWrapper<DivisionRecord> wrapper = DivisionRecord.gw();

        wrapper.eq(DivisionRecord::getUserNo, getCurrentMchNo()).eq(DivisionRecord::getUserType, DivisionRecord.USER_TYPE_MCH);

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
    }

    /**
     * 新增商户信息
     *
     * @return
     */
    @MethodLog(remark = "商户结算申请")
    @RequestMapping(value = "", method = RequestMethod.POST)
    @LimitRequest
    public ApiRes add() {
        DivisionRecord divisionRecord = getObject(DivisionRecord.class);
        MchInfo mchInfo = mchInfoService.queryMchInfo(getCurrentMchNo());

        if (mchInfo.getState() == CS.NO) {
            return ApiRes.customFail("商户状态异常!");
        }

        if ((mchInfo.getBalance() < divisionRecord.getAmount())) {
            return ApiRes.customFail("申请金额大于可提现余额!");
        }

        PayConfig payConfig = sysConfigService.getPayConfig();

        if (payConfig.getMchMinWithdraw() > divisionRecord.getAmount()) {
            return ApiRes.customFail("申请金额小于最小提现限制，最小提现金额![" + AmountUtil.convertCent2DollarShort(payConfig.getMchMinWithdraw()) + "]元");
        }

        Long fee = AmountUtil.calPercentageFee(divisionRecord.getAmount(), payConfig.getMchFeeRate()) + payConfig.getMchFee();
        //检测最小提现额度，算出手续费
        Long divisionAmount = divisionRecord.getAmount() - fee;
        if (divisionAmount <= 0) {
            return ApiRes.customFail("申请金额过小无法体现!需要手续费[" + AmountUtil.convertCent2DollarShort(fee) + "]元");
        }

        Date submitDate = new Date();
        boolean isSuccess = divisionRecordService.SaveDivisionRecord(mchInfo.getMchNo(), mchInfo.getMchName(), divisionRecord.getAmount(), fee, divisionRecord.getRemark(), DivisionRecord.USER_TYPE_MCH, submitDate);
        if (isSuccess) {
            //增加商户资金流水记录
            mchInfo = mchInfoService.queryMchInfo(getCurrentMchNo());
            Long amount = divisionRecord.getAmount();
            Long beforeBalance = mchInfo.getBalance();
            Long afterBalance = mchInfo.getBalance() - amount;

            //插入更新记录
            MchHistory mchHistory = new MchHistory();
            mchHistory.setMchNo(mchInfo.getMchNo());
            mchHistory.setMchName(mchInfo.getMchName());
            mchHistory.setAmount(-amount);

            mchHistory.setBeforeBalance(beforeBalance);
            mchHistory.setAfterBalance(afterBalance);
            mchHistory.setFundDirection(CS.FUND_DIRECTION_REDUCE);
            mchHistory.setBizType(CS.BIZ_TYPE_WITHDRAW);
            mchHistory.setCreatedAt(submitDate);
            mchHistoryService.save(mchHistory);

            mchInfo.setFreezeBalance(mchInfo.getFreezeBalance() + divisionRecord.getAmount());
            mchInfoService.updateBalance(mchInfo.getMchNo(), -divisionRecord.getAmount());
            mchInfoService.updateMchInfo(mchInfo);

            return ApiRes.ok();
        }
        return ApiRes.fail(ApiCodeEnum.DB_ERROR);
    }

    @RequestMapping(value = "/getConfig", method = RequestMethod.POST)
    public ApiRes getConfig() {
        PayConfig payConfig = sysConfigService.getPayConfig();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mchFee", payConfig.getMchFee());
        jsonObject.put("mchFeeRate", payConfig.getMchFeeRate());
        jsonObject.put("mchMinWithdraw", payConfig.getMchMinWithdraw());
        return ApiRes.ok(jsonObject);
    }

}
