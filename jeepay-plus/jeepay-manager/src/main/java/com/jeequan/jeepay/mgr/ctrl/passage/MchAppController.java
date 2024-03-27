package com.jeequan.jeepay.mgr.ctrl.passage;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.LimitRequest;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.Collator;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 支付通道管理类
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-06-16 09:15
 */
@RestController
@RequestMapping("/api/mchApps")
public class MchAppController extends CommonCtrl {

    @Autowired
    private PayPassageService payPassageService;

    @Autowired
    private ProductService productService;

    @Autowired
    private RobotsPassageService robotsPassageService;

    @Autowired
    private StatisticsPassageService statisticsPassageService;

    @Autowired
    private MchPayPassageService mchPayPassageService;

    /**
     * @Author: ZhuXiao
     * @Description: 应用列表
     * @Date: 9:59 2021/6/16
     */
    @PreAuthorize("hasAuthority('ENT_MCH_APP_LIST')")
    @GetMapping
    public ApiRes list() {
        try {
            JSONObject reqJson = getReqParamJSON();
            PayPassage payPassage = getObject(PayPassage.class);

            QueryWrapper<PayPassage> wrapper = new QueryWrapper<>();
            wrapper.ne("state", CS.HIDE);
            if (StringUtils.isNotEmpty(payPassage.getPayPassageName())) {
                wrapper.like("pay_passage_name", payPassage.getPayPassageName().trim());
            }

            if (payPassage.getPayPassageId() != null) {
                wrapper.eq("pay_passage_id", payPassage.getPayPassageId());
            }

            if (payPassage.getState() != null) {
                wrapper.eq("state", payPassage.getState());
            }

            if (payPassage.getProductId() != null) {
                wrapper.eq("product_id", payPassage.getProductId());
            }

            if (StringUtils.isNotEmpty(payPassage.getPayInterfaceConfig())) {
                wrapper.like("pay_interface_config", payPassage.getPayInterfaceConfig());
            }

            List<Product> productList = productService.list();
            Map<Long, String> productMap = new HashMap<>();
            for (int i = 0; i < productList.size(); i++) {
                productMap.put(productList.get(i).getProductId(), productList.get(i).getProductName());
            }

            //全局排序+搜索可用
            wrapper.orderBy(true, false, "state");

            String sortField = reqJson.getString("sortField");
            String sortOrder = reqJson.getString("sortOrder");
            if (StringUtils.isNotEmpty(sortField) && sortField.equals("payPassageId") && StringUtils.isNotEmpty(sortOrder)) {
                wrapper.orderBy(true, !sortOrder.equals("descend"), "CONVERT(pay_passage_name USING gbk) COLLATE gbk_chinese_ci");
            }

            if (StringUtils.isNotEmpty(sortField) && sortField.equals("balance") && StringUtils.isNotEmpty(sortOrder)) {
                wrapper.orderBy(true, !sortOrder.equals("descend"), "balance");
            }

            if (StringUtils.isNotEmpty(sortField) && sortField.equals("rate") && StringUtils.isNotEmpty(sortOrder)) {
                wrapper.orderBy(true, !sortOrder.equals("descend"), "rate");
            }

            if (StringUtils.isEmpty(sortField) || StringUtils.isEmpty(sortOrder)) {
                wrapper.orderBy(true, true, "created_at");
            }

            IPage<PayPassage> pages = payPassageService.page(getIPage(true), wrapper);

            List<PayPassage> payPassageList = pages.getRecords();

            Date today = DateUtil.parse(DateUtil.today());
            LambdaQueryWrapper<StatisticsPassage> wrapperStatisticsPassage = StatisticsPassage.gw();
            wrapperStatisticsPassage.eq(StatisticsPassage::getStatisticsDate, today);

            List<StatisticsPassage> statisticsPassageList = statisticsPassageService.list(wrapperStatisticsPassage);

            Map<Long, String> statisticsPassageMap = new HashMap<>();
            DecimalFormat decimalFormat = new DecimalFormat("#.##");


            for (int i = 0; i < statisticsPassageList.size(); i++) {
                StatisticsPassage statisticsPassage = statisticsPassageList.get(i);
                if (statisticsPassage.getOrderSuccessCount() == 0 || statisticsPassage.getTotalOrderCount() == 0) {
                    statisticsPassageMap.put(statisticsPassage.getPayPassageId(), "0.00");
                } else {
                    float rate = statisticsPassage.getOrderSuccessCount().floatValue() / statisticsPassage.getTotalOrderCount().floatValue() * 100f;

                    // 使用DecimalFormat的format方法将float类型的数字保留两位小数
                    String formattedNumber = decimalFormat.format(rate);
                    statisticsPassageMap.put(statisticsPassage.getPayPassageId(), formattedNumber);
                }

            }

            for (int i = 0; i < payPassageList.size(); i++) {
                PayPassage passage = payPassageList.get(i);
                passage.addExt("productName", productMap.get(passage.getProductId()));
                if (statisticsPassageMap.containsKey(passage.getPayPassageId())) {
                    passage.addExt("successRate", statisticsPassageMap.get(passage.getPayPassageId()));
                } else {
                    passage.addExt("successRate", "0.00");
                }
            }

//            pages.setTotal(payPassageList.size());
            pages.setRecords(payPassageList);
            return ApiRes.ok(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }

    /**
     * @Author: ZhuXiao
     * @Description: 新建应用
     * @Date: 10:05 2021/6/16
     */
    @PreAuthorize("hasAuthority('ENT_MCH_APP_ADD')")
    @MethodLog(remark = "新建通道")
    @PostMapping
    @LimitRequest
    public ApiRes add() {
        PayPassage payPassage = getObject(PayPassage.class);
        String passageName = payPassage.getPayPassageName().replaceAll(" ", "");
        payPassage.setPayPassageName(passageName);
//        PayPassage resultPassage = payPassageService.getOne(PayPassage.gw().eq(PayPassage::getPayPassageName, payPassage.getPayPassageName()));
//        if (resultPassage != null && StringUtils.isNotEmpty(resultPassage.getPayPassageName())) {
//            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_REPEAT);
//        }
        //插入对应通道的空值配置
        boolean result = payPassageService.save(payPassage);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }
        return ApiRes.ok();
    }

    /**
     * @Author: ZhuXiao
     * @Description: 应用详情
     * @Date: 10:13 2021/6/16
     */
    @PreAuthorize("hasAnyAuthority('ENT_MCH_APP_VIEW', 'ENT_MCH_APP_EDIT')")
    @GetMapping("/{payPassageId}")
    public ApiRes detail(@PathVariable("payPassageId") Long payPassageId) {
        PayPassage payPassage = payPassageService.queryPassageInfo(payPassageId);
        if (payPassage == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        return ApiRes.ok(payPassage);
    }

    /**
     * @Author: ZhuXiao
     * @Description: 更新应用信息
     * @Date: 10:11 2021/6/16
     */
    @PreAuthorize("hasAuthority('ENT_MCH_APP_EDIT')")
    @MethodLog(remark = "更新通道信息")
    @PutMapping("/{payPassageId}")
    @LimitRequest
    public ApiRes update(@PathVariable("payPassageId") Long payPassageId) {

        JSONObject reqJson = getReqParamJSON();

        PayPassage payPassage = getObject(PayPassage.class);


        payPassage.setPayPassageId(payPassageId);
        payPassage.setUpdatedAt(new Date());


        String passageName = payPassage.getPayPassageName();
        if (StringUtils.isNotEmpty(passageName)) {
            passageName = payPassage.getPayPassageName().replaceAll(" ", "");
            payPassage.setPayPassageName(passageName);
        }

        if (reqJson.getBoolean("openLimit") != null && reqJson.getBoolean("openLimit")) {
            PayPassage checkPassage = payPassageService.queryPassageInfo(payPassageId);
            if (StringUtils.isNotEmpty(checkPassage.getTimeRules())) {
                payPassage.setTimeLimit(CS.YES);
            }
        }

        //去掉一些配置的空格
        //payInterfaceConfig payRules
        String payInterfaceConfig = payPassage.getPayInterfaceConfig();
        String payRules = payPassage.getPayRules();

        if (StringUtils.isNotEmpty(payInterfaceConfig)) {
            //去掉换行符
            String temp = payInterfaceConfig.replaceAll("\\\\n", "").trim();
            String temp1 = temp.replaceAll(" ", "");
            payPassage.setPayInterfaceConfig(temp1);
        }

        if (StringUtils.isNotEmpty(payRules)) {
            String temp = payRules.replaceAll("\\\\n", "").trim();
            String temp1 = temp.replaceAll(" ", "");
            payPassage.setPayRules(temp1);
        }
        payPassageService.updatePassageInfo(payPassage);
        return ApiRes.ok();
    }

    /**
     * @Author: ZhuXiao
     * @Description: 删除通道
     * @Date: 10:14 2021/6/16
     */
    @PreAuthorize("hasAuthority('ENT_MCH_APP_DEL')")
    @MethodLog(remark = "删除通道")
    @DeleteMapping("/{payPassageId}")
    @LimitRequest
    @Transactional(transactionManager = "transactionManager", rollbackFor = {Exception.class})
    public ApiRes delete(@PathVariable("payPassageId") Long payPassageId) {

        PayPassage oldPassage = payPassageService.queryPassageInfo(payPassageId);
        if (!(oldPassage.getBalance() == 0)) {
            return ApiRes.fail(ApiCodeEnum.DELETE_BALANCE_NOT_ZERO_PASSAGE);
        }
        PayPassage payPassage = new PayPassage();
        payPassage.setPayPassageId(payPassageId);
        payPassage.setState(CS.HIDE);
        boolean isSuccess = payPassageService.updateById(payPassage);
        if (isSuccess) {
            //移除机器人绑定的通道
            robotsPassageService.remove(RobotsPassage.gw().eq(RobotsPassage::getPassageId, payPassageId));
            //移除绑定
            mchPayPassageService.remove(MchPayPassage.gw().eq(MchPayPassage::getPayPassageId, payPassageId));
            return ApiRes.ok();
        } else {
            logger.error("隐藏通道失败: " + payPassageId);
            return ApiRes.customFail("删除失败,请稍后再试");
        }
    }
}
