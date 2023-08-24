package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * @since 2023-08-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_robots_passage")
public class RobotsPassage extends BaseModel {

    private static final long serialVersionUID=1L;

    public static final LambdaQueryWrapper<RobotsPassage> gw() {
        return new LambdaQueryWrapper<>();
    }

    /**
     * 通道ID/通道群组ID
     */
    @TableId(value = "passage_id")
    private Long passageId;

    /**
     * 群组ID
     */
    private Long chatId;
}
