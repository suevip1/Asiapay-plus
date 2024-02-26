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
package com.jeequan.jeepay.mgr.ctrl.config;

import cn.hutool.core.date.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.mgr.ctrl.config.data.MchStat;
import com.jeequan.jeepay.mgr.ctrl.config.data.PassageStat;
import com.jeequan.jeepay.service.CommonService.StatisticsService;
import com.jeequan.jeepay.service.impl.AgentAccountInfoService;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.PayPassageService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 首页统计类
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021-06-07 07:15
 */
@Slf4j
@RestController
@RequestMapping("api/mainChart")
public class MainChartController extends CommonCtrl {
    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private AgentAccountInfoService agentAccountInfoService;

    @Autowired
    private PayPassageService payPassageService;

    @PreAuthorize("hasAuthority('ENT_C_MAIN_PAY_COUNT')")
    @RequestMapping(value = "/twoDayCount", method = RequestMethod.GET)
    public ApiRes twoDayCount() {
        JSONObject jsonObject = new JSONObject();
        Date today = DateUtil.parse(DateUtil.today());
        StatisticsPlat todayStatisticsPlat = statisticsService.QueryStatisticsPlatByDate(today);
        if (todayStatisticsPlat == null) {
            todayStatisticsPlat = StatisticsPlat.Empty();
        }
        DateTime yesterday = DateUtil.offsetDay(today, -1);

        StatisticsPlat yesterdayStatisticsPlat = statisticsService.QueryStatisticsPlatByDate(yesterday);
        if (yesterdayStatisticsPlat == null) {
            yesterdayStatisticsPlat = StatisticsPlat.Empty();
        }
        int mchNum = mchInfoService.count(MchInfo.gw().ne(MchInfo::getState, CS.HIDE));
        int agentNum = agentAccountInfoService.count(AgentAccountInfo.gw().ne(AgentAccountInfo::getState, CS.HIDE));
        jsonObject.put("todayCount", todayStatisticsPlat);
        jsonObject.put("yesterdayCount", yesterdayStatisticsPlat);
        jsonObject.put("mchNum", mchNum);
        jsonObject.put("agentNum", agentNum);
        float todaySuccessRate = 0;
        if (todayStatisticsPlat.getTotalOrderCount() != 0) {
            todaySuccessRate = todayStatisticsPlat.getOrderSuccessCount().floatValue() / (todayStatisticsPlat.getTotalOrderCount().floatValue());
        }
        float yesterdaySuccessRate = 0;
        if (yesterdayStatisticsPlat.getTotalOrderCount() != 0) {
            yesterdaySuccessRate = yesterdayStatisticsPlat.getOrderSuccessCount().floatValue() / (yesterdayStatisticsPlat.getTotalOrderCount().floatValue());
        }
        jsonObject.put("todaySuccessRate", todaySuccessRate);
        jsonObject.put("yesterdaySuccessRate", yesterdaySuccessRate);
        //返回数据
        return ApiRes.ok(jsonObject);
    }

    @PreAuthorize("hasAuthority('ENT_C_MAIN_PAY_COUNT')")
    @RequestMapping(value = "/realTimeCount", method = RequestMethod.GET)
    public ApiRes realTimeCount() {
        JSONObject json;
        Date now = DateUtil.parse(DateUtil.now());
        Map<String, Object> payOrderAllMap = RedisUtil.retrieveCollection(CS.REAL_TIME_STAT);
        Map<String, Object> payOrderSuccessMap = RedisUtil.retrieveCollection(CS.REAL_TIME_SUCCESS_STAT);
        List<PayPassage> passageList = payPassageService.list();

        Map<String, PassageStat> passageMap = new HashMap<>();
        for (int index = 0; index < passageList.size(); index++) {
            PassageStat passageStat = new PassageStat();
            passageStat.setPassageName("[" + passageList.get(index).getPayPassageId() + "]" + passageList.get(index).getPayPassageName());
            passageStat.setSuccessAmount(0L);
            passageMap.put(passageList.get(index).getPayPassageId().toString(), passageStat);
        }

        for (Map.Entry<String, Object> entry : payOrderSuccessMap.entrySet()) {
            PayOrder payOrder = JSON.parseObject((String) entry.getValue(), PayOrder.class);
            long betweenMin = DateUtil.between(now, payOrder.getCreatedAt(), DateUnit.MINUTE);
            if (betweenMin >= 61) {
                RedisUtil.deleteFromHash(CS.REAL_TIME_SUCCESS_STAT, payOrder.getPayOrderId());
            }
        }

        List<PayOrder> listMerge = new ArrayList<>();
        //比较重复项-成功的订单以创建时间为准
        if (payOrderAllMap.size() != 0) {
            for (Map.Entry<String, Object> entry : payOrderAllMap.entrySet()) {
                PayOrder payOrder = JSON.parseObject((String) entry.getValue(), PayOrder.class);

                long betweenMin = DateUtil.between(now, payOrder.getCreatedAt(), DateUnit.MINUTE);
                if (betweenMin >= 30) {
                    RedisUtil.deleteFromHash(CS.REAL_TIME_STAT, payOrder.getPayOrderId());
                } else {
                    Long passageId = payOrder.getPassageId();
                    PassageStat passageStat = (PassageStat) passageMap.get(passageId.toString());
                    if (passageStat != null) {
                        int count = passageStat.getAllCount() + 1;
                        passageStat.setAllCount(count);
                        passageMap.replace(passageId.toString(), passageStat);
                        if (payOrderSuccessMap.containsKey(payOrder.getPayOrderId())) {
                            listMerge.add(payOrder);
                        }
                    }
                }
            }
        }

        if (listMerge.size() != 0) {
            //成功单统计
            for (int i = 0; i < listMerge.size(); i++) {
                PayOrder payOrder = listMerge.get(i);
                PassageStat passageStat = passageMap.get(payOrder.getPassageId().toString());
                passageStat.setSuccessCount(passageStat.getSuccessCount() + 1);
                passageStat.setSuccessAmount(passageStat.getSuccessAmount() + payOrder.getAmount());
                passageMap.replace(payOrder.getPassageId().toString(), passageStat);
            }

            // 移除 allCount 为 0 的对象
            passageMap.values().removeIf(stat -> (stat).getAllCount() == 0);

            LinkedHashMap<String, PassageStat> sortedMap = passageMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, PassageStat>comparingByValue(
                            Comparator.comparingDouble(stat -> (double) ((PassageStat) stat).getSuccessCount() / ((PassageStat) stat).getAllCount())
                                    .reversed()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();
            for (Map.Entry<String, PassageStat> entry : sortedMap.entrySet()) {
                String key = entry.getKey();
                PassageStat value = entry.getValue();
                resultMap.put(key, value);
            }
            if (resultMap.size() <= 15) {
                json = new JSONObject(resultMap);
                return ApiRes.ok(json);
            } else {
                int i = 0;
                LinkedHashMap<String, Object> tempMap = new LinkedHashMap<>();
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    i++;
                    String key = entry.getKey();
                    tempMap.put(key, entry.getValue());
                    if (i >= 15) {
                        json = new JSONObject(tempMap);
                        return ApiRes.ok(json);
                    }
                }
            }
        }

        //返回数据
        return ApiRes.ok();
    }

    /**
     * 商户实时并发计算
     *
     * @return
     */
//    @PreAuthorize("hasAuthority('ENT_C_MAIN_PAY_COUNT')")
//    @RequestMapping(value = "/realTimeConcurrent/{time}", method = RequestMethod.GET)
//    public ApiRes realTimeConcurrent(@PathVariable("time") Integer time) {
    @PreAuthorize("hasAuthority('ENT_C_MAIN_PAY_COUNT')")
    @RequestMapping(value = "/realTimeConcurrent", method = RequestMethod.GET)
    public ApiRes realTimeConcurrent() {
        JSONObject reqJson = getReqParamJSON();

        Integer time = reqJson.getInteger("time");
        Integer pageNumber = reqJson.getInteger("pageNumber");
        Integer pageSize = reqJson.getInteger("pageSize");
        IPage<MchStat> pages = new Page<>();

        if (time == null || pageNumber == null) {
            pages.setRecords(new ArrayList<>());
            pages.setTotal(0);
            pages.setSize(10);
            pages.setCurrent(1);
            return ApiRes.ok(pages);
        }
        pages.setSize(pageSize);
        pages.setCurrent(pageNumber);
        //time 5 20 60
        Date now = DateUtil.parse(DateUtil.now());

        Map<String, Object> payOrderAllMap = RedisUtil.retrieveCollection(CS.REAL_TIME_STAT);
        if (!payOrderAllMap.isEmpty()) {
            List<MchInfo> mchInfoList = mchInfoService.list();

            //初始化商户统计集合
            Map<String, MchStat> mchStatMap = new HashMap<>();
            for (int index = 0; index < mchInfoList.size(); index++) {
                MchStat mchStat = new MchStat();
                mchStat.setMchName("[" + mchInfoList.get(index).getMchNo() + "]" + mchInfoList.get(index).getMchName());
                mchStat.setAllCount(0);
                mchStat.setPerMinCount(0);

                mchStatMap.put(mchInfoList.get(index).getMchNo(), mchStat);
            }

            //统计并发数
            for (Map.Entry<String, Object> entry : payOrderAllMap.entrySet()) {
                PayOrder payOrder = JSON.parseObject((String) entry.getValue(), PayOrder.class);
                long betweenMin = DateUtil.between(now, payOrder.getCreatedAt(), DateUnit.MINUTE);
                //超时的移除
                if (betweenMin >= 61) {
                    RedisUtil.deleteFromHash(CS.REAL_TIME_STAT, payOrder.getPayOrderId());
                } else {
                    String mchNo = payOrder.getMchNo();
                    MchStat mchStat = mchStatMap.get(mchNo);
                    if (betweenMin <= time) {
                        mchStat.setAllCount(mchStat.getAllCount() + 1);
                        mchStatMap.replace(mchNo, mchStat);
                    }
                }
            }

            // 移除 allCount 为 0 的对象
            mchStatMap.values().removeIf(stat -> (stat).getAllCount() == 0);

            //计算每分钟并发
            for (Map.Entry<String, MchStat> entry : mchStatMap.entrySet()) {
                MchStat mchStat = entry.getValue();
                Integer allCount = mchStat.getAllCount();
                mchStat.setPerMinCount(allCount / time);
                mchStatMap.replace(entry.getKey(), mchStat);
            }

            //排序
            LinkedHashMap<String, MchStat> sortedMap = mchStatMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(
                            Comparator.comparingInt(stat -> ((MchStat) stat).getAllCount())
                                    .reversed()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            List<MchStat> records = new ArrayList<>();
            int i = 0;
            // 1-10
            int minIndex = (pageNumber - 1) * pageSize; // 0-9[1] 10-19[2]
            int maxIndex = pageNumber * pageSize - 1;
            for (Map.Entry<String, MchStat> entry : sortedMap.entrySet()) {
                if (i >= minIndex && i <= maxIndex) {
                    records.add(entry.getValue());
                }
                i++;
            }
            pages.setRecords(records);
            pages.setTotal(sortedMap.size());
            //排序后的页码
            return ApiRes.ok(pages);
        }

        //返回数据
        return ApiRes.ok();
    }

    public static void main(String[] args) {

    }
}



