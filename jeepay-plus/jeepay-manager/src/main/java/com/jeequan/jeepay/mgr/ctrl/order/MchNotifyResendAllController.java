package com.jeequan.jeepay.mgr.ctrl.order;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.components.mq.model.PayOrderMchNotifyMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.MchNotifyRecord;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchNotifyRecordService;
import com.jeequan.jeepay.service.impl.PayPassageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mchNotifyResend")
public class MchNotifyResendAllController extends CommonCtrl {
    @Autowired
    private MchNotifyRecordService mchNotifyService;

    @Autowired
    private IMQSender mqSender;


    /*
     * 功能描述: 商户通知重发操作
     */
    @PreAuthorize("hasAuthority('ENT_MCH_NOTIFY_RESEND')")
    @RequestMapping(value = "resendAll", method = RequestMethod.POST)
    public ApiRes resendAll() {

        try {
            MchNotifyRecord mchNotify = getObject(MchNotifyRecord.class);
            JSONObject paramJSON = getReqParamJSON();
            LambdaQueryWrapper<MchNotifyRecord> wrapper = MchNotifyRecord.gw();
            if (StringUtils.isNotEmpty(mchNotify.getOrderId())) {
                wrapper.eq(MchNotifyRecord::getOrderId, mchNotify.getOrderId());
            }
            if (StringUtils.isNotEmpty(mchNotify.getMchNo())) {
                wrapper.eq(MchNotifyRecord::getMchNo, mchNotify.getMchNo());
            }
            if (StringUtils.isNotEmpty(mchNotify.getPassageOrderNo())) {
                wrapper.eq(MchNotifyRecord::getPassageOrderNo, mchNotify.getPassageOrderNo());
            }
            if (mchNotify.getOrderType() != null) {
                wrapper.eq(MchNotifyRecord::getOrderType, mchNotify.getOrderType());
            }
            wrapper.eq(MchNotifyRecord::getState, MchNotifyRecord.STATE_FAIL);

            if (paramJSON != null) {
                if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                    wrapper.ge(MchNotifyRecord::getCreatedAt, paramJSON.getString("createdStart"));
                }
                if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                    wrapper.le(MchNotifyRecord::getCreatedAt, paramJSON.getString("createdEnd"));
                }
            }
            wrapper.orderByDesc(MchNotifyRecord::getCreatedAt);
            List<MchNotifyRecord> records = mchNotifyService.list(wrapper);
            for (int i = 0; i < records.size(); i++) {
                MchNotifyRecord mchNotifyItem = records.get(i);
                //更新通知中
                mchNotifyService.getBaseMapper().updateIngAndAddNotifyCountLimit(mchNotifyItem.getNotifyId());
                //调起MQ重发
                mqSender.send(PayOrderMchNotifyMQ.build(mchNotifyItem.getNotifyId()));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        return ApiRes.ok();
    }

}