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
 * 异常订单表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2024-02-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_error_order")
public class ErrorOrder extends BaseModel {

    private static final long serialVersionUID = 1L;

    public static final LambdaQueryWrapper<ErrorOrder> gw() {
        return new LambdaQueryWrapper<>();
    }

    /**
     * ID
     */
    @TableId(value = "error_order_id", type = IdType.AUTO)
    private Long errorOrderId;

    /**
     * 商户号
     */
    private String mchNo;

    /**
     * 商户名称
     */
    private String mchName;

    /**
     * 支付金额,单位分
     */
    private Long amount;

    /**
     * 商户请求参数
     */
    private String mchReq;

    /**
     * 回调响应(我方给商户的响应)
     */
    private String mchResp;

    /**
     * 商户订单号
     */
    private String mchOrderNo;

    /**
     * 创建时间
     */
    private Date createdAt;


}
