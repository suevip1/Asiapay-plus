package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeequan.jeepay.core.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 支付通道日统计表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_statistics_passage")
public class StatisticsPassage extends BaseModel {

    private static final long serialVersionUID=1L;

    public static final LambdaQueryWrapper<StatisticsPassage> gw(){
        return new LambdaQueryWrapper<>();
    }

    /**
     * ID
     */
    @TableId(value = "statistics_passage_id", type = IdType.AUTO)
    private Long statisticsPassageId;

    /**
     * 支付通道ID
     */
    private Long payPassageId;

    /**
     * 统计日期
     */
    private Date statisticsDate;

    /**
     * 收款订单总数
     */
    private Integer totalOrderCount;

    /**
     * 收款成功订单数
     */
    private Integer orderSuccessCount;

    /**
     * 收款总金额，单位分
     */
    private Long totalAmount;

    /**
     * 收款成功金额，单位分
     */
    private Long totalSuccessAmount;

    /**
     * 总通道成本，单位分
     */
    private Long totalPassageCost;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;


}
