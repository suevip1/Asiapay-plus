package com.jeequan.jeepay.service.mapper;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.entity.AgentAccountInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Map;

/**
 * <p>
 * 代理商账户表 Mapper 接口
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
public interface AgentAccountInfoMapper extends BaseMapper<AgentAccountInfo> {

    int updateBalance(Map param);

    JSONObject sumAgentInfo();
}
