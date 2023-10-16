package com.jeequan.jeepay.mgr.ctrl.merchant;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.AgentAccountHistory;
import com.jeequan.jeepay.core.entity.MchHistory;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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

            //商号
            if (StringUtils.isNotEmpty(mchHistory.getMchName())) {
                wrapper.like(MchHistory::getMchName, mchHistory.getMchName());
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

            IPage<MchHistory> pages = mchHistoryService.page(getIPage(true), wrapper);

            return ApiRes.page(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }

    @PostMapping(value = "/exportExcel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportExcel() throws Exception {
        MchHistory mchHistory = getObject(MchHistory.class);
        JSONObject paramJSON = getReqParamJSON();
        if (mchHistory == null) {
            mchHistory = new MchHistory();
        }
        LambdaQueryWrapper<MchHistory> wrapper = MchHistory.gw();
        String mchNo = paramJSON.getString("mchNo");
        String mchName = paramJSON.getString("mchName");
        if (StringUtils.isNotEmpty(mchNo)) {
            //商号
            wrapper.eq(MchHistory::getMchNo, mchNo);
        }
        if (StringUtils.isNotEmpty(mchName)) {
            //商号
            wrapper.like(MchHistory::getMchName, mchName);
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

        List<MchHistory> records = mchHistoryService.list(wrapper);

        int count = records.size();
        if (count > 65535) {
            throw new BizException("导出最大数据不能超过65535行！");
        } else {
            List<List> excelData = new ArrayList();
            List<String> header = Arrays.asList("记录ID", "商户号","商户名", "变更前余额(元)", "变更金额(元)", "变更后余额(元)", "业务类型", "业务订单号", "商户订单号", "订单金额", "时间");
            excelData.add(header);
            Iterator iteratorRecord = records.iterator();

            while (iteratorRecord.hasNext()) {
                MchHistory record = (MchHistory) iteratorRecord.next();
                List<String> rowData = new ArrayList();
                //id
                rowData.add(String.valueOf(record.getMchHistoryId()));
                //商户号
                rowData.add(String.valueOf(record.getMchNo()));
                //商户名
                rowData.add(String.valueOf(record.getMchName()));
                //变更前余额
                rowData.add(AmountUtil.convertCent2Dollar(record.getBeforeBalance() + ""));
                //变更金额
                String prefix = record.getFundDirection() == CS.FUND_DIRECTION_INCREASE ? "+" : "";
                rowData.add(prefix + AmountUtil.convertCent2Dollar(record.getAmount() + ""));
                //变更后余额
                rowData.add(AmountUtil.convertCent2Dollar(record.getAfterBalance() + ""));
                //业务类型
                rowData.add(CS.GetMchBizTypeString(record.getBizType()));

                //业务订单号
                rowData.add(record.getPayOrderId());
                //商户订单号
                rowData.add(record.getMchOrderNo());
                //订单金额
                if (record.getPayOrderAmount() != null) {
                    rowData.add(AmountUtil.convertCent2Dollar(record.getPayOrderAmount() + ""));
                } else {
                    rowData.add("");
                }

                //时间
                rowData.add(DateUtil.format(record.getCreatedAt(), "yyyy-MM-dd HH-mm-ss"));
                excelData.add(rowData);
            }

            this.writeExcelStream(excelData);
        }
    }
}