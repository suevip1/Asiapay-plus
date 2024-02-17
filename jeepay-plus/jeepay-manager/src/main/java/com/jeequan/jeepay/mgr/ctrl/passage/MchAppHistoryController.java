package com.jeequan.jeepay.mgr.ctrl.passage;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.AgentAccountHistory;
import com.jeequan.jeepay.core.entity.PassageTransactionHistory;
import com.jeequan.jeepay.core.entity.PassageTransactionHistory;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.PassageTransactionHistoryService;
import com.jeequan.jeepay.service.impl.PassageTransactionHistoryService;
import com.jeequan.jeepay.service.impl.PayPassageService;
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

    @PostMapping(value = "/exportExcel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportExcel() throws Exception {
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

        List<PassageTransactionHistory> records = passageTransactionHistoryService.list(wrapper);

        List<List> excelData = new ArrayList();
        List<String> header = Arrays.asList("记录ID", "通道名", "变更前余额(元)", "变更金额(元)", "变更后余额(元)", "业务类型", "订单号", "时间");
        excelData.add(header);
        Iterator iteratorRecord = records.iterator();

        while (iteratorRecord.hasNext()) {
            PassageTransactionHistory record = (PassageTransactionHistory) iteratorRecord.next();
            List<String> rowData = new ArrayList();
            //记录ID
            rowData.add(String.valueOf(record.getPassageTransactionHistoryId()));
            //通道名
            rowData.add("[" + record.getPayPassageId() + "] " + record.getPayPassageName());
            //变更前余额
            rowData.add(AmountUtil.convertCent2Dollar(record.getBeforeBalance() + ""));
            //变更金额
            String prefix = record.getFundDirection() == CS.FUND_DIRECTION_INCREASE ? "+" : "";
            rowData.add(prefix + AmountUtil.convertCent2Dollar(record.getAmount() + ""));
            //变更后余额
            rowData.add(AmountUtil.convertCent2Dollar(record.getAfterBalance() + ""));
            //业务类型
            rowData.add(CS.GetPassageBizTypeString(record.getBizType()));

            //业务订单号
            rowData.add(record.getPayOrderId());

            //时间
            rowData.add(DateUtil.format(record.getCreatedAt(), "yyyy-MM-dd HH-mm-ss"));
            excelData.add(rowData);
        }

        this.writeExcelStream(excelData);
    }
}