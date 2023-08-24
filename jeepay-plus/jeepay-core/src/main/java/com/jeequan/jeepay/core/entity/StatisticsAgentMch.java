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
 * 代理商-商户日统计表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-07-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_statistics_agent_mch")
public class StatisticsAgentMch extends BaseModel {

    private static final long serialVersionUID=1L;
    public static final LambdaQueryWrapper<StatisticsAgentMch> gw(){
        return new LambdaQueryWrapper<>();
    }
    /**
     * ID
     */
    @TableId(value = "statistics_agent_mch_id", type = IdType.AUTO)
    private Long statisticsAgentMchId;

    /**
     * 代理商号
     */
    private String agentNo;

    /**
     * 商户号
     */
    private String mchNo;

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
     * 收款手续费，单位分
     */
    private Long totalAgentIncome;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;


}
