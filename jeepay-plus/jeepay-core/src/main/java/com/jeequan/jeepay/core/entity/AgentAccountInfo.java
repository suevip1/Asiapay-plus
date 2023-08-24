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
 * 代理商账户表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_agent_account_info")
public class AgentAccountInfo extends BaseModel {

    private static final long serialVersionUID=1L;
    public static final LambdaQueryWrapper<AgentAccountInfo> gw(){
        return new LambdaQueryWrapper<>();
    }

    /**
     * 代理商商户号
     */
    @TableId(value = "agent_no", type = IdType.INPUT)
    private String agentNo;

    /**
     * 代理商名称
     */
    private String agentName;

    /**
     * 账户余额
     */
    private Long balance;

    /**
     * 账户状态,1-可用,0-停止使用
     */
    private Byte state;

    /**
     * 商户备注
     */
    private String remark;

    /**
     * 初始用户ID（创建时分配的用户ID）
     */
    private Long initUserId;

    /**
     * 创建者用户ID
     */
    private Long createdUid;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 冻结金额
     */
    private Long freezeBalance;

}
