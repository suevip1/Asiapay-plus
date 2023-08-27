package com.jeequan.jeepay.core.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 机器人配置
 */
@Data
public class RobotsConfig implements Serializable {

    /**
     * 机器人管理员用户名
     */
    private String robotsAdmin;

    /**
     * 机器人用户名
     */
    private String robotsUserName;

    /**
     * 机器人token
     */
    private String robotsToken;

    /**
     * 通道配置修改预警 (0关闭，1打开)
     */
    private String passageConfig;

    /**
     * 强制补单预警(0关闭，任意正数打开)
     */
    private String forceOrderWarnConfig;

    /**
     * 异常订单预警 (0关闭，任意正数打开)
     */
    private String errorOrderWarnConfig;
}