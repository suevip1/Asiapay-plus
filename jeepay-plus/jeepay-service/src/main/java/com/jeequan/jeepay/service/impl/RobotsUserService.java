package com.jeequan.jeepay.service.impl;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.RobotsMch;
import com.jeequan.jeepay.core.entity.RobotsUser;
import com.jeequan.jeepay.service.mapper.RobotsUserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
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
public class RobotsUserService extends ServiceImpl<RobotsUserMapper, RobotsUser> {

    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private RobotsMchService robotsMchService;


    /**
     * 是否管理员
     *
     * @param userName
     * @return
     */
    public boolean checkIsAdmin(String userName) {
        return sysConfigService.getRobotsConfig().getRobotsAdmin().equals(userName);
    }

    /**
     * 是否操作员
     * @param userName
     * @return
     */
    public boolean checkIsOp(String userName) {
        return getById(userName) != null;
    }

    public boolean saveMch(RobotsMch robotsMch) {
        return robotsMchService.saveOrUpdate(robotsMch);
    }
}