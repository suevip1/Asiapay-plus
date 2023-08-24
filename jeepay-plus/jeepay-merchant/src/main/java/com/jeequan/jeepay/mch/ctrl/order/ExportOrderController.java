package com.jeequan.jeepay.mch.ctrl.order;


import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.Product;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.PayPassageService;
import com.jeequan.jeepay.service.impl.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

@RestController
@RequestMapping("/api/payOrderExport")
public class ExportOrderController extends CommonCtrl {

    @Autowired
    public PayOrderService payOrderService;

    @Autowired
    public ProductService productService;


    @PostMapping(value = "/exportMchExcel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportMchExcel() throws Exception {
        PayOrder payOrder = getObject(PayOrder.class);
        JSONObject paramJSON = getReqParamJSON();
        if (payOrder == null) {
            payOrder = new PayOrder();
        }
        payOrder.setMchNo(getCurrentMchNo());

        List<PayOrder> records = payOrderService.listByQuery(payOrder, paramJSON, PayOrder.gw());

        int count = records.size();
        if (count > 65535) {
            throw new BizException("导出最大数据不能超过65535行！");
        } else {
            List<List> excelData = new ArrayList();
            Map<Long, Product> productMap = productService.getProductMap();

            List<String> header = Arrays.asList("商户号", "商户名称", "支付金额", "订单号", "商户订单号", "商户服务费", "产品类型", "支付状态", "创建时间", "成功时间");
            excelData.add(header);
            Iterator iteratorRecord = records.iterator();

            while (iteratorRecord.hasNext()) {
                PayOrder record = (PayOrder) iteratorRecord.next();
                List<String> rowData = new ArrayList();
                //商户号
                rowData.add(String.valueOf(record.getMchNo()));
                //商户名称
                rowData.add(record.getMchName());
                //支付金额
                rowData.add(AmountUtil.convertCent2Dollar(record.getAmount()));

                //订单号
                rowData.add(record.getPayOrderId());
                //商户订单号
                rowData.add(record.getMchOrderNo());

                //商户手续费
                rowData.add(AmountUtil.convertCent2Dollar(record.getMchFeeAmount()));
                //产品类型
                String productStr = "[" + record.getProductId() + "]" + productMap.get(record.getProductId()).getProductName();
                rowData.add(productStr);

                //支付状态
                rowData.add(CS.GetPayOrderTypeString(record.getState()));
                //创建时间
                rowData.add(DateUtil.format(record.getCreatedAt(), "yyyy-MM-dd HH:mm:ss"));
                //成功时间
                if (record.getSuccessTime() != null) {
                    rowData.add(DateUtil.format(record.getSuccessTime(), "yyyy-MM-dd HH:mm:ss"));
                } else {
                    rowData.add("");
                }
                excelData.add(rowData);
            }

            this.writeExcelStream(excelData);
        }
    }

    private String convertToPercentage(BigDecimal value) {
        // 将 BigDecimal 转换为百分比形式并保留两位小数
        DecimalFormat decimalFormat = new DecimalFormat("0.00%");
        return decimalFormat.format(value);
    }
}