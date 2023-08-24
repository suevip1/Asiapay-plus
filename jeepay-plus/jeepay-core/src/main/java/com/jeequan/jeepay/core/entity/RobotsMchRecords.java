package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
@TableName("t_robots_mch_records")
public class RobotsMchRecords implements Serializable {

    public static final LambdaQueryWrapper<RobotsMchRecords> gw() {
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
    @TableId(value = "robot_mch_record_id", type = IdType.AUTO)
    private Long robotMchRecordId;

    /**
     * 商户号
     */
    private String mchNo;

    /**
     * 金额
     */
    private Long amount;

    /**
     * 操作员用户名
     */
    private String userName;

    /**
     * 创建时间
     */
    private Date createdAt;


}
