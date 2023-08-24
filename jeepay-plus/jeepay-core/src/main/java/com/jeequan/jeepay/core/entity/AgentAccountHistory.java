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
 * 代理商资金账户流水表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_agent_account_history")
public class AgentAccountHistory extends BaseModel {

    private static final long serialVersionUID = 1L;

    public static final LambdaQueryWrapper<AgentAccountHistory> gw() {
        return new LambdaQueryWrapper<>();
    }

    /**
     * ID
     */
    @TableId(value = "agent_account_history_id", type = IdType.AUTO)
    private Long agentAccountHistoryId;

    /**
     * 代理商商户号
     */
    private String agentNo;

    /**
     * 代理商名称快照
     */
    private String agentName;

    /**
     * 变动金额
     */
    private Long amount;

    /**
     * 变更前账户余额
     */
    private Long beforeBalance;

    /**
     * 变更后账户余额
     */
    private Long afterBalance;

    /**
     * 资金变动方向,1-加款,2-减款
     */
    private Byte fundDirection;

    /**
     * 业务类型,1-分润,2-提现,3-调账
     */
    private Byte bizType;

    /**
     * 创建者ID(2-提现,3-调账 操作时不为空)
     */
    private Long createdUid;

    /**
     * 创建者登录名(2-提现,3-调账 操作时不为空)
     */
    private String createdLoginName;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 平台订单号
     */
    private String payOrderId;

    /**
     * 订单金额
     */
    private Long payOrderAmount;

    /**
     * 备注
     */
    private String remark;


}
