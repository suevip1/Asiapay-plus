/*
 * Copyright (c) 2021-2031
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.mgr.ctrl.order;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.components.mq.model.PayOrderMchNotifyMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
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
import java.util.Map;

/**
 * 商户通知类
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021-06-07 07:15
 */
@RestController
@RequestMapping("/api/mchNotify")
public class MchNotifyController extends CommonCtrl {
    @Autowired
    private MchNotifyRecordService mchNotifyService;

    @Autowired
    private PayPassageService payPassageService;

    @Autowired
    private IMQSender mqSender;

    /**
     * @describe: 商户通知列表
     */
    @PreAuthorize("hasAuthority('ENT_NOTIFY_LIST')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
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
            if (mchNotify.getState() != null) {
                wrapper.eq(MchNotifyRecord::getState, mchNotify.getState());
            }

            if (paramJSON != null) {
                if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                    wrapper.ge(MchNotifyRecord::getCreatedAt, paramJSON.getString("createdStart"));
                }
                if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                    wrapper.le(MchNotifyRecord::getCreatedAt, paramJSON.getString("createdEnd"));
                }
            }
            wrapper.orderByDesc(MchNotifyRecord::getCreatedAt);
            IPage<MchNotifyRecord> pages = mchNotifyService.page(getIPage(), wrapper);

            Map<Long, PayPassage> payPassageMap = payPassageService.getPayPassageMap();
            List<MchNotifyRecord> records = pages.getRecords();
            for (int i = 0; i < records.size(); i++) {
                records.get(i).addExt("payPassageName", payPassageMap.get(records.get(i).getPayPassageId()).getPayPassageName());
            }
            pages.setRecords(records);
            return ApiRes.page(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }

    /**
     * @describe: 商户通知信息
     */
    @PreAuthorize("hasAuthority('ENT_MCH_NOTIFY_VIEW')")
    @RequestMapping(value = "/{notifyId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("notifyId") String notifyId) {
        MchNotifyRecord mchNotify = mchNotifyService.getById(notifyId);
        if (mchNotify == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        return ApiRes.ok(mchNotify);
    }

    /*
     * 功能描述: 商户通知重发操作
     */
    @PreAuthorize("hasAuthority('ENT_MCH_NOTIFY_RESEND')")
    @RequestMapping(value = "resend/{notifyId}", method = RequestMethod.POST)
    public ApiRes resend(@PathVariable("notifyId") Long notifyId) {
        MchNotifyRecord mchNotify = mchNotifyService.getById(notifyId);
        if (mchNotify == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        if (mchNotify.getState() != MchNotifyRecord.STATE_FAIL) {
            throw new BizException("请选择失败的通知记录");
        }

        //更新通知中
        mchNotifyService.getBaseMapper().updateIngAndAddNotifyCountLimit(notifyId);

        //调起MQ重发
        mqSender.send(PayOrderMchNotifyMQ.build(notifyId));

        return ApiRes.ok(mchNotify);
    }

}
