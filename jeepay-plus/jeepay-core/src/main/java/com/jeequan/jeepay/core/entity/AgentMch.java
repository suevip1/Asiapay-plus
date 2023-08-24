package com.jeequan.jeepay.core.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 代理商-商户关系表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_agent_mch")
public class AgentMch implements Serializable {

    private static final long serialVersionUID=1L;
    public static final LambdaQueryWrapper<AgentMch> gw(){
        return new LambdaQueryWrapper<>();
    }


    /**
     * ID
     */
    @TableId(value = "agent_mch_id", type = IdType.AUTO)
    private Long agentMchId;

    /**
     * 上级代理商商户号
     */
    private String agentNo;

    /**
     * 商户号
     */
    private String mchNo;

    /**
     * 对应产品ID
     */
    private Long productId;

    /**
     * 对应产品费率
     */
    private BigDecimal rate;

    /**
     * 备注
     */
    private String remark;


}
