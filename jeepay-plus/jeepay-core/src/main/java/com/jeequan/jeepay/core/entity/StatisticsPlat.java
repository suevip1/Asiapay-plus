package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeequan.jeepay.core.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 平台日总统计表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_statistics_plat")
public class StatisticsPlat extends BaseModel {

    public static StatisticsPlat Empty(){
        StatisticsPlat statisticsPlat = new StatisticsPlat();
        statisticsPlat.setOrderSuccessCount(0);
        statisticsPlat.setTotalOrderCount(0);

        statisticsPlat.setPlatTotalIncome(0L);
        statisticsPlat.setTotalAmount(0L);
        statisticsPlat.setTotalSuccessAmount(0L);
        return statisticsPlat;
    }

    private static final long serialVersionUID = 1L;

    public static final LambdaQueryWrapper<StatisticsPlat> gw() {
        return new LambdaQueryWrapper<>();
    }

    /**
     * 统计日期
     */
    @TableId(value = "statistics_date", type = IdType.INPUT)
    private Date statisticsDate;

    /**
     * 订单总数
     */
    private Integer totalOrderCount;

    /**
     * 成功订单数
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
     * 平台总收款利润，单位分
     */
    private Long platTotalIncome;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;


}
