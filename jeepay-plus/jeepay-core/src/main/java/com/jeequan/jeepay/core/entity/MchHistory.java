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
 * 商户资金账户流水表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_history")
public class MchHistory extends BaseModel {

    private static final long serialVersionUID=1L;

    public static final byte FUND_INCREASE = 1; //加款
    public static final byte FUND_REDUCE = 2; //减款

    public static final LambdaQueryWrapper<MchHistory> gw() {
        return new LambdaQueryWrapper<>();
    }


    /**
     * ID
     */
    @TableId(value = "mch_history_id", type = IdType.AUTO)
    private Long mchHistoryId;

    /**
     * 商户号
     */
    private String mchNo;

    /**
     * 商户名
     */
    private String mchName;

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
     * 单笔手续费
     */
    private Long mchRateAmount;

    /**
     * 资金变动方向,1-加款,2-减款
     */
    private Byte fundDirection;

    /**
     * 业务类型,1-支付,2-提现,3-调账
     */
    private Byte bizType;

    /**
     * 创建者id(2-提现,3-调账 操作时不为空)
     */
    private Long createdUid;

    /**
     * 创建者姓名(2-提现,3-调账 操作时不为空)
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
     * 通道订单号
     */
    private String passageOrderId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 平台收入
     */
    private Long platIncome;

    /**
     * 商户代理收入
     */
    private Long agentIncome;


    /**
     * 代理商户号
     */
    private String agentNo;

    /**
     * 代理商名字
     */
    private String agentName;

    /**
     * 商户订单号
     */
    private String mchOrderNo;


}
