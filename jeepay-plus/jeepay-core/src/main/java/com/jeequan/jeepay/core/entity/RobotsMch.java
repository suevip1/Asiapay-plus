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
@TableName("t_robots_mch")
public class RobotsMch extends BaseModel {

    private static final long serialVersionUID = 1L;

    public static final LambdaQueryWrapper<RobotsMch> gw() {
        return new LambdaQueryWrapper<>();
    }

    /**
     * 商户号
     */
    private String mchNo;

    /**
     * chat id
     */
    @TableId(value = "chat_id")
    private Long chatId;

    /**
     * 群记账余额
     */
    private Long balance;


}
