package com.jeequan.jeepay.service.impl;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.RobotsMch;
import com.jeequan.jeepay.service.mapper.RobotsMchMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-07-24
 */
@Service
public class RobotsMchService extends ServiceImpl<RobotsMchMapper, RobotsMch> {
    /**
     * 获取管理群
     *
     * @return
     */
    public RobotsMch getManageMch() {
        return getOne(RobotsMch.gw().eq(RobotsMch::getMchNo, CS.ROBOTS_MGR_MCH));
    }

    /**
     * 获取商户群
     *
     * @param chatId
     * @return
     */
    public RobotsMch getMch(Long chatId) {
        return getOne(RobotsMch.gw().eq(RobotsMch::getChatId, chatId).ne(RobotsMch::getMchNo, CS.ROBOTS_MGR_MCH));
    }

}
