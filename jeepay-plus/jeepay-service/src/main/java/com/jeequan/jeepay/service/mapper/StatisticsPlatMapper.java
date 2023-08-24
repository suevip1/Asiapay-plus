package com.jeequan.jeepay.service.mapper;

import com.jeequan.jeepay.core.entity.StatisticsMch;
import com.jeequan.jeepay.core.entity.StatisticsPlat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 平台日总统计表 Mapper 接口
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
public interface StatisticsPlatMapper extends BaseMapper<StatisticsPlat> {
    int updateStatistics(StatisticsPlat statisticsPlat);
}
