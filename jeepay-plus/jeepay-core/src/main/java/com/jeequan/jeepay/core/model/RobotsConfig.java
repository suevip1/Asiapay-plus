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
}