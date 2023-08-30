package com.jeequan.jeepay.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.RobotsMch;
import com.jeequan.jeepay.service.mapper.RobotsMchMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if (robotsMch != null && StringUtils.isNotEmpty(robotsMch.getMchNo())) {
            return robotsMch;
        }
        return null;
    }

    /**
     * 解绑全部商户
     *
     * @param chatId
     * @return
     */
    public boolean unBlindAllMch(Long chatId) {
        RobotsMch robotsMch = new RobotsMch();
        robotsMch.setChatId(chatId);
        robotsMch.setMchNo("");
        return updateById(robotsMch);
    }

    /**
     * 解绑单个商户
     *
     * @param chatId
     * @param mchNo
     * @return
     */
    public boolean unBlindMch(Long chatId, String mchNo) {
        //兼容之前的写法
        RobotsMch robotsMch = getById(chatId);
        if (robotsMch != null) {
            String mchStr = robotsMch.getMchNo();
            if (StringUtils.isNotEmpty(mchStr)) {
                JSONArray jsonArray = JSONArray.parseArray(mchStr);

                Map<String, String> indexMap = new HashMap<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    String key = jsonArray.getString(i); // 假设你要根据某个键来查找
                    indexMap.put(key, key);
                }
                if (indexMap.containsKey(mchNo)) {
                    jsonArray.remove(mchNo);
                }
                robotsMch.setMchNo(jsonArray.toJSONString());
            } else {
                return false;
            }
        } else {
            return false;
        }
        return updateById(robotsMch);
    }

    public void updateBlindMch(Long chatId, String mchNo) {
        //查找其他群有没有绑定
        List<RobotsMch> robotsMchOldList = list(RobotsMch.gw().like(RobotsMch::getMchNo, mchNo));
        if (robotsMchOldList.size() > 0) {
            for (int i = 0; i < robotsMchOldList.size(); i++) {
                unBlindMch(robotsMchOldList.get(i).getChatId(), mchNo);
            }
        }

        //添加商户
        RobotsMch robotsMch = getById(chatId);
        if (robotsMch != null) {
            String mchStr = robotsMch.getMchNo();
            if (StringUtils.isNotEmpty(mchStr)) {
                JSONArray jsonArray = JSONArray.parseArray(mchStr);

                // 构建索引，以元素的特定属性作为索引键
                Map<String, String> indexMap = new HashMap<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    String key = jsonArray.getString(i); // 假设你要根据某个键来查找
                    indexMap.put(key, key);
                }
                if (!indexMap.containsKey(mchNo)) {
                    jsonArray.add(mchNo);
                }

                robotsMch.setMchNo(jsonArray.toJSONString());
            } else {
                JSONArray jsonArray = new JSONArray();
                jsonArray.add(mchNo);
                robotsMch.setMchNo(jsonArray.toJSONString());
            }
        } else {
            robotsMch = new RobotsMch();
            robotsMch.setChatId(chatId);
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(mchNo);
            robotsMch.setMchNo(jsonArray.toJSONString());
        }
        saveOrUpdate(robotsMch);
    }

}
