package com.jeequan.jeepay.service.impl;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.RobotsMch;
import com.jeequan.jeepay.service.mapper.RobotsMchMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
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

    public void updateManageMch(Long chatId) {
        RobotsMch robotsAdmin = getManageMch();
        if (robotsAdmin == null) {
            robotsAdmin = new RobotsMch();
            robotsAdmin.setMchNo(CS.ROBOTS_MGR_MCH);
            robotsAdmin.setChatId(chatId);
            save(robotsAdmin);
        } else {
            removeById(robotsAdmin);
            robotsAdmin = new RobotsMch();
            robotsAdmin.setMchNo(CS.ROBOTS_MGR_MCH);
            robotsAdmin.setChatId(chatId);
            save(robotsAdmin);
        }
    }


    /**
     * 获取商户群
     *
     * @param chatId
     * @return
     */
    public RobotsMch getMch(Long chatId) {
        RobotsMch robotsMch = getOne(RobotsMch.gw().eq(RobotsMch::getChatId, chatId).ne(RobotsMch::getMchNo, CS.ROBOTS_MGR_MCH));
        if(robotsMch != null && StringUtils.isNotEmpty(robotsMch.getMchNo())){
            return robotsMch;
        }
        return null;
    }

    /**
     * 解绑商户
     *
     * @param chatId
     * @return
     */
    public boolean unBlindMch(Long chatId) {
        RobotsMch robotsMch = new RobotsMch();
        robotsMch.setChatId(chatId);
        robotsMch.setMchNo("");
        return updateById(robotsMch);
    }

    public void updateBlindMch(Long chatId, String mchNo) {
        RobotsMch robotsMchOld = getOne(RobotsMch.gw().eq(RobotsMch::getMchNo, mchNo));
        if (robotsMchOld != null) {
            robotsMchOld.setMchNo("");
            updateById(robotsMchOld);
        }

        RobotsMch robotsMch = new RobotsMch();
        robotsMch.setChatId(chatId);
        robotsMch.setMchNo(mchNo);
        saveOrUpdate(robotsMch);
    }

}
