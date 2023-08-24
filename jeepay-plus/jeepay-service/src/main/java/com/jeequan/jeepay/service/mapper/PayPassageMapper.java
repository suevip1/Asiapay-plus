package com.jeequan.jeepay.service.mapper;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Map;

/**
 * <p>
 * 支付通道表 Mapper 接口
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
public interface PayPassageMapper extends BaseMapper<PayPassage> {
    int updateBalance(Map param);
    int updateQuota(Map param);

    JSONObject sumPassageInfo();
}
