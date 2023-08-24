package com.jeequan.jeepay.core.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import java.io.Serializable;

import com.jeequan.jeepay.core.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 代理商-通道关系表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_agent_passage")
public class AgentPassage extends BaseModel {

    private static final long serialVersionUID=1L;

    /**
     * ID
     */
    @TableId(value = "agent_passage_id", type = IdType.AUTO)
    private Integer agentPassageId;

    /**
     * 代理商商户号
     */
    private String agentNo;

    /**
     * 通道ID
     */
    private Long payPassageId;

    /**
     * 代理商费率,百分比
     */
    private BigDecimal agentRate;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;


}
