package com.jeequan.jeepay.core.entity;

import java.math.BigDecimal;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableField;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeequan.jeepay.core.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 转账代付订单表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-09-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_trans_order")
public class TransOrder extends BaseModel {

    private static final long serialVersionUID=1L;

    public static final LambdaQueryWrapper<TransOrder> gw() {
        return new LambdaQueryWrapper<>();
    }


    /**
     * 转账订单号
     */
    @TableId(value = "trans_order_id")
    private String transOrderId;

    /**
     * 商户号/代理商号
     */
    private String userNo;

    /**
     * 商户类型:1-商户,2-代理
     */
    private Byte userType;

    /**
     * 通道ID
     */
    private Long passageId;

    /**
     * 通道账户ID
     */
    private String ifCode;

    /**
     * 转账金额,单位分
     */
    private Long amount;

    /**
     * 转账状态:0-订单生成,1-转账中,2-转账成功,3-转账失败,4-业务处理完成
     */
    private Byte state;

    /**
     * 转账结果:0-不确认结果,1-等待手动处理,2-确认成功,3-确认失败
     */
    private Byte result;

    /**
     * 备注
     */
    private String remark;

    /**
     * 账户属性:0-对私,1-对公,2-USDT
     */
    private Byte accountAttr;

    /**
     * 账户类型:1-银行卡转账,2-微信转账,3-支付宝转账
     */
    private Byte accountType;

    /**
     * 账户名
     */
    private String accountName;

    /**
     * 账户号
     */
    private String accountNo;

    /**
     * 开户行名称
     */
    private String bankName;

    /**
     * 渠道商户ID
     */
    private String channelMchId;

    /**
     * 渠道费率
     */
    private BigDecimal channelRate;

    /**
     * 渠道成本,单位分
     */
    private Long channelFee;

    /**
     * 渠道订单号
     */
    private String channelOrderNo;

    /**
     * 渠道错误码
     */
    private String channelErrorCode;

    /**
     * 渠道错误描述
     */
    private String channelErrorMsg;

    /**
     * 特定渠道发起时额外参数
     */
    private String extra;

    /**
     * 扩展参数1
     */
    private String param1;

    /**
     * 订单失效时间
     */
    @TableField("expireTime")
    private Date expireTime;

    /**
     * 订单转账成功时间
     */
    private Date successTime;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;


}
