package com.jeequan.jeepay.core.entity;

import java.math.BigDecimal;
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
 * 分账记录表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-07-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_division_record")
public class DivisionRecord extends BaseModel {

    public static final LambdaQueryWrapper<DivisionRecord> gw(){
        return new LambdaQueryWrapper<>();
    }

    /**
     * 手工结算
     */
    public static final byte PAY_TYPE_MANUAL = 0;//关闭
    /**
     * api结算
     */
    public static final byte PAY_TYPE_API = 1; //打开

    //1-待结算 2-结算成功, 3-结算失败(取消),4-超时取消订单
    /**
     * 待结算
     */
    public static final byte STATE_WAIT = 1;
    /**
     * 结算成功
     */
    public static final byte STATE_SUCCESS = 2;
    /**
     * 结算失败
     */
    public static final byte STATE_FAIL = 3;

    /**
     * 超时取消
     */
    public static final byte STATE_CANCEL= 4;

    //0-手动结算 1-银行卡 2-USDT 3-其他
    /**
     * 结算方式-手动结算
     */
    public static final byte ACC_TYPE_MANUAL = 0;
    /**
     * 结算方式-银行卡结算
     */
    public static final byte ACC_TYPE_BANK_CARD = 1;
    /**
     * 结算方式-U结算
     */
    public static final byte ACC_TYPE_USDT = 2;
    /**
     * 结算方式-其他方式
     */
    public static final byte ACC_TYPE_OTHER = 3;


    private static final long serialVersionUID=1L;


    /**
     * 用户类型：商户
     */
    public static final byte USER_TYPE_MCH = 1;
    /**
     * 用户类型：代理
     */
    public static final byte USER_TYPE_AGENT = 2;

    /**
     * 分账记录ID
     */
    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    /**
     * 商户号/代理商号
     */
    private String userNo;

    /**
     * 商户名称/代理商名称
     */
    private String userName;

    /**
     * 用户类型:1-商户，2-代理
     */
    private Byte userType;

    /**
     * 结算模式：0-手动结算，1-api结算
     */
    private Byte payType;

    /**
     * 结算通道ID
     */
    private Long divisionPassageId;

    /**
     * 支付订单渠道支付订单号
     */
    private String payOrderChannelOrderNo;

    /**
     * 申请金额,单位分
     */
    private Long amount;

    /**
     * 订单实际分账金额, 单位：分（订单金额 - 手续费）
     */
    private Long divisionAmount;

    /**
     * 费率
     */
    private BigDecimal divisionFeeRate;

    /**
     * 手续费, 单位：分
     */
    private Long divisionAmountFee;

    /**
     * 分账批次号
     */
    private String channelBatchOrderId;

    /**
     *  状态: 1-待结算 2-结算成功, 3-结算失败(取消)，4-超时关闭
     */
    private Byte state;

    /**
     * 上游返回数据包
     */
    private String channelRespResult;

    /**
     * 账号快照》 分账接收账号类型: 0-手动结算 1-银行卡 2-USDT 3-其他
     */
    private Byte accType;

    /**
     * 账号快照》 分账接收账号/地址
     */
    private String accNo;

    /**
     * 账号快照》 分账接收账号名称
     */
    private String accName;

    /**
     * 实际接收金额,单位分
     */
    private Long calDivisionAmount;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 备注
     */
    private String remark;

    /**
     * 申请失效时间
     */
    private Date expiredTime;

}
