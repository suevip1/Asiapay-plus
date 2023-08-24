package com.jeequan.jeepay.agent.ctrl.merchant;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.agent.ctrl.CommonCtrl;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.StatisticsAgentMchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


@Slf4j
@RestController
@RequestMapping("/api/mchInfo")
public class AgentMchInfoController extends CommonCtrl {

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private StatisticsAgentMchService statisticsAgentMchService;

    /**
     * 查看代理商资金流水
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        try {
            MchInfo mchInfo = getObject(MchInfo.class);
            JSONObject jsonObject = getReqParamJSON();
            Date date = DateUtil.parse(jsonObject.getString("date"));


            LambdaQueryWrapper<MchInfo> wrapper = MchInfo.gw();

            wrapper.eq(MchInfo::getAgentNo, getCurrentAgentNo());

            if (StringUtils.isNotEmpty(mchInfo.getMchNo())) {
                wrapper.eq(MchInfo::getMchNo, mchInfo.getMchNo());
            }
            if (StringUtils.isNotEmpty(mchInfo.getMchName())) {
                wrapper.like(MchInfo::getMchName, mchInfo.getMchName());
            }

            wrapper.orderByDesc(MchInfo::getBalance);

            IPage<MchInfo> pages = mchInfoService.page(getIPage(), wrapper.select(MchInfo::getMchNo, MchInfo::getMchName, MchInfo::getState, MchInfo::getBalance));

            List<MchInfo> list = pages.getRecords();

            for (int i = 0; i < list.size(); i++) {
                StatisticsAgentMch statisticsAgentMch = statisticsAgentMchService.getOne(StatisticsAgentMch.gw().eq(StatisticsAgentMch::getMchNo, list.get(i).getMchNo()).eq(StatisticsAgentMch::getStatisticsDate, date).eq(StatisticsAgentMch::getAgentNo, getCurrentAgentNo()));
                if (statisticsAgentMch == null) {
                    statisticsAgentMch = new StatisticsAgentMch();
                    statisticsAgentMch.setTotalAgentIncome(0L);
                    statisticsAgentMch.setStatisticsDate(date);
                    statisticsAgentMch.setOrderSuccessCount(0);
                    statisticsAgentMch.setOrderSuccessCount(0);

                    statisticsAgentMch.setTotalAmount(0L);
                    statisticsAgentMch.setTotalOrderCount(0);
                }
                list.get(i).addExt("stat", statisticsAgentMch);
            }

            return ApiRes.page(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }

//    @PostMapping(value = "/exportExcel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
//    public void exportExcel() throws Exception {
//        MchHistory mchHistory = getObject(MchHistory.class);
//        JSONObject paramJSON = getReqParamJSON();
//        if (mchHistory == null) {
//            mchHistory = new MchHistory();
//        }
//        LambdaQueryWrapper<MchHistory> wrapper = MchHistory.gw();
//
//        //商号
//        wrapper.eq(MchHistory::getMchNo, getCurrentMchNo());
//
//        //订单
//        if (StringUtils.isNotEmpty(mchHistory.getPayOrderId())) {
//            wrapper.eq(MchHistory::getPayOrderId, mchHistory.getPayOrderId());
//        }
//        //资金方向
//        if (mchHistory.getFundDirection() != null && mchHistory.getFundDirection() != 0) {
//            wrapper.eq(MchHistory::getFundDirection, mchHistory.getFundDirection());
//        }
//        //业务类型
//        if (mchHistory.getBizType() != null && mchHistory.getBizType() != 0) {
//            wrapper.eq(MchHistory::getBizType, mchHistory.getBizType());
//        }
//        if (paramJSON != null) {
//            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
//                wrapper.ge(MchHistory::getCreatedAt, paramJSON.getString("createdStart"));
//            }
//            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
//                wrapper.le(MchHistory::getCreatedAt, paramJSON.getString("createdEnd"));
//            }
//        }
//        wrapper.orderByDesc(MchHistory::getCreatedAt);
//
//        IPage<MchHistory> pages = mchHistoryService.page(getIPage(true), wrapper);
//
//        int count = pages.getRecords().size();
//        if (count > 65535) {
//            throw new BizException("导出最大数据不能超过65535行！");
//        } else {
//            List<List> excelData = new ArrayList();
//            List<String> header = Arrays.asList("记录ID", "变更前余额(元)", "变更金额(元)", "变更后余额(元)", "业务类型", "业务订单号", "商户订单号", "订单金额", "时间");
//            excelData.add(header);
//            Iterator iteratorRecord = pages.getRecords().iterator();
//
//            while (iteratorRecord.hasNext()) {
//                MchHistory record = (MchHistory) iteratorRecord.next();
//                List<String> rowData = new ArrayList();
//                //id
//                rowData.add(String.valueOf(record.getMchHistoryId()));
//                //变更前余额
//                rowData.add(AmountUtil.convertCent2Dollar(record.getBeforeBalance() + ""));
//                //变更金额
//                String prefix = record.getFundDirection() == CS.FUND_DIRECTION_INCREASE ? "+" : "";
//                rowData.add(prefix + AmountUtil.convertCent2Dollar(record.getAmount() + ""));
//                //变更后余额
//                rowData.add(AmountUtil.convertCent2Dollar(record.getAfterBalance() + ""));
//                //业务类型
//                if (record.getBizType() == CS.BIZ_TYPE_CHANGE) {
//                    rowData.add(CS.GetMchBizTypeString(record.getBizType()) + "-(操作员用户名)-" + record.getCreatedLoginName());
//                } else {
//                    rowData.add(CS.GetMchBizTypeString(record.getBizType()));
//                }
//
//                //业务订单号
//                rowData.add(record.getPayOrderId());
//                //商户订单号
//                rowData.add(record.getMchOrderNo());
//                //订单金额
//                if (record.getPayOrderAmount() != null) {
//                    rowData.add(AmountUtil.convertCent2Dollar(record.getPayOrderAmount() + ""));
//                } else {
//                    rowData.add("");
//                }
//
//                //时间
//                rowData.add(DateUtil.format(record.getCreatedAt(), "yyyy-MM-dd HH-mm-ss"));
//                excelData.add(rowData);
//            }
//
//            this.writeExcelStream(excelData);
//        }
//    }
}