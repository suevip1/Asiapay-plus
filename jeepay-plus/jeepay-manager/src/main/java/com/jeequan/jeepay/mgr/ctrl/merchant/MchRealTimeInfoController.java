package com.jeequan.jeepay.mgr.ctrl.merchant;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchHistory;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.AgentAccountInfoService;
import com.jeequan.jeepay.service.impl.MchInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/mchRealTimeInfo")
public class MchRealTimeInfoController extends CommonCtrl {

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private AgentAccountInfoService agentAccountInfoService;

    @PostMapping(value = "/exportExcel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportExcel() throws Exception {
        MchInfo mchInfo = getObject(MchInfo.class);

        LambdaQueryWrapper<MchInfo> wrapper = MchInfo.gw();
        if (StringUtils.isNotEmpty(mchInfo.getMchNo())) {
            wrapper.like(MchInfo::getMchNo, mchInfo.getMchNo().trim());
        }
        if (StringUtils.isNotEmpty(mchInfo.getAgentNo())) {
            wrapper.like(MchInfo::getAgentNo, mchInfo.getAgentNo().trim());
        }
        if (StringUtils.isNotEmpty(mchInfo.getMchName())) {
            wrapper.like(MchInfo::getMchName, mchInfo.getMchName().trim());
        }

        if (mchInfo.getState() != null) {
            wrapper.eq(MchInfo::getState, mchInfo.getState());
        }
        wrapper.orderByDesc(MchInfo::getBalance);

        List<MchInfo> records = mchInfoService.list(wrapper);

        int count = records.size();
        if (count > 65535) {
            throw new BizException("导出最大数据不能超过65535行！");
        } else {
            List<List> excelData = new ArrayList();
            List<String> header = Arrays.asList("商户号", "商户名", "余额(元)", "创建时间");
            excelData.add(header);
            Iterator iteratorRecord = records.iterator();

            while (iteratorRecord.hasNext()) {
                MchInfo record = (MchInfo) iteratorRecord.next();
                List<String> rowData = new ArrayList();

                //商户号
                rowData.add(String.valueOf(record.getMchNo()));
                //商户名
                rowData.add(String.valueOf(record.getMchName()));
                //余额
                rowData.add(AmountUtil.convertCent2Dollar(record.getBalance() + ""));
                //时间
                rowData.add(DateUtil.format(record.getCreatedAt(), "yyyy-MM-dd HH-mm-ss"));
                excelData.add(rowData);
            }

            this.writeExcelStream(excelData);
        }
    }
}
