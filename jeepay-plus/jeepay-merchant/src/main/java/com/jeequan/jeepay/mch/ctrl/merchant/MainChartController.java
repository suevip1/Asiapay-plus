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
package com.jeequan.jeepay.mch.ctrl.merchant;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.CommonService.StatisticsService;
import com.jeequan.jeepay.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 主页数据类
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
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
    private MchProductService mchProductService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StatisticsMchProductService statisticsMchProductService;

    @Autowired
    private SysUserService sysUserService;

    @RequestMapping(value = "/twoDayCount", method = RequestMethod.GET)
    public ApiRes twoDayCount() {
        JSONObject jsonObject = new JSONObject();
        Date today = DateUtil.parse(DateUtil.today());
        StatisticsMch todayStatisticsMch = statisticsService.QueryStatisticsMchByDate(getCurrentMchNo(), today);
        if (todayStatisticsMch == null) {
            todayStatisticsMch = todayStatisticsMch.Empty();
        }
        DateTime yesterday = DateUtil.offsetDay(today, -1);

        StatisticsMch yesterdayStatisticsMch = statisticsService.QueryStatisticsMchByDate(getCurrentMchNo(), yesterday);
        if (yesterdayStatisticsMch == null) {
            yesterdayStatisticsMch = yesterdayStatisticsMch.Empty();
        }
        jsonObject.put("todayCount", todayStatisticsMch);
        jsonObject.put("yesterdayCount", yesterdayStatisticsMch);
        float todaySuccessRate = 0;
        if (todayStatisticsMch.getTotalOrderCount() != 0) {
            todaySuccessRate = todayStatisticsMch.getOrderSuccessCount().floatValue() / (todayStatisticsMch.getTotalOrderCount().floatValue());
        }
        float yesterdaySuccessRate = 0;
        if (yesterdayStatisticsMch.getTotalOrderCount() != 0) {
            yesterdaySuccessRate = yesterdayStatisticsMch.getOrderSuccessCount().floatValue() / (yesterdayStatisticsMch.getTotalOrderCount().floatValue());
        }
        jsonObject.put("todaySuccessRate", todaySuccessRate);
        jsonObject.put("yesterdaySuccessRate", yesterdaySuccessRate);
        //返回数据
        return ApiRes.ok(jsonObject);
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        JSONObject jsonObject = getReqParamJSON();
        Date date = DateUtil.parse(jsonObject.getString("date"));
        Map<Long, Product> productMap = productService.getProductMap();

        IPage<StatisticsMchProduct> pages = statisticsMchProductService.page(getIPage(true), StatisticsMchProduct.gw().eq(StatisticsMchProduct::getMchNo, getCurrentMchNo()).eq(StatisticsMchProduct::getStatisticsDate, date));
        List<StatisticsMchProduct> statisticsMchProductList = pages.getRecords();

        for (int i = 0; i < statisticsMchProductList.size(); i++) {
            statisticsMchProductList.get(i).addExt("productName", productMap.get(statisticsMchProductList.get(i).getProductId()).getProductName());
        }
        pages.setRecords(statisticsMchProductList);
        //返回数据
        return ApiRes.ok(pages);
    }

    @RequestMapping(value = "/mchInfo", method = RequestMethod.GET)
    public ApiRes mchInfo() {
        JSONObject json = new JSONObject();
        MchInfo mchInfo = mchInfoService.queryMchInfo(getCurrentMchNo());


        Map<Long, Product> productMap = productService.getProductMap();

        List<MchProduct> records = mchProductService.list(MchProduct.gw().select(MchProduct::getMchNo, MchProduct::getCreatedAt, MchProduct::getUpdatedAt, MchProduct::getMchRate, MchProduct::getProductId).eq(MchProduct::getMchNo, getCurrentMchNo()).eq(MchProduct::getState, CS.YES));

        for (int i = 0; i < records.size(); i++) {
            Long productId = records.get(i).getProductId();
            String productName = productMap.get(productId).getProductName();
            records.get(i).addExt("productName", productName);
        }
        json.put("products", records);

        SysUser sysUser = sysUserService.getOne(SysUser.gw().eq(SysUser::getBelongInfoId, getCurrentMchNo()).eq(SysUser::getSysType, CS.SYS_TYPE.MCH));
        mchInfo.addExt("loginUsername", sysUser.getLoginUsername());
        json.put("mchInfo", mchInfo);
        //返回数据
        return ApiRes.ok(json);
    }
}
