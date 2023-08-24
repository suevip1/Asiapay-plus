package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeequan.jeepay.core.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-07-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_robots_user")
public class RobotsUser extends BaseModel {

    private static final long serialVersionUID=1L;
    public static final LambdaQueryWrapper<RobotsUser> gw() {
        return new LambdaQueryWrapper<>();
    }

    /**
     * 机器人操作员用户名
     */
    @TableId(value = "user_name")
    private String userName;
}
