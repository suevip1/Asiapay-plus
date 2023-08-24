package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.util.Base64;
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
 * 通道余额调额记录
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_passage_transaction_history")
public class PassageTransactionHistory extends BaseModel {

    private static final long serialVersionUID = 1L;

    public static final byte FUND_INCREASE = 1; //加款
    public static final byte FUND_REDUCE = 2; //减款

    /**
     * 订单
     */
    public static final byte BIZ_TYPE_ORDER = 4;
    /**
     * 调额
     */
    public static final byte BIZ_TYPE_CHANGE = 5;

    /**
     * 通道余额清零
     */
    public static final byte BIZ_TYPE_RESET = 6;


    public static final LambdaQueryWrapper<PassageTransactionHistory> gw() {
        return new LambdaQueryWrapper<>();
    }

    /**
     * ID
     */
    @TableId(value = "passage_transaction_history_id", type = IdType.AUTO)
    private Long passageTransactionHistoryId;

    /**
     * 通道ID
     */
    private Long payPassageId;

    /**
     * 通道名
     */
    private String payPassageName;

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
     * 业务类型,4-订单,5-通道调账 6-通道余额清零
     */
    private Byte bizType;

    /**
     * 操作者ID
     */
    private Long createdUid;

    /**
     * 操作者 用户名
     */
    private String createdLoginName;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 备注
     */
    private String remark;

    /**
     * 订单号
     */
    private String payOrderId;


}
