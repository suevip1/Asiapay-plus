package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.math.BigDecimal;
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
 * 商户-产品关系表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_product")
public class MchProduct extends BaseModel {

    private static final long serialVersionUID=1L;

    public static final LambdaQueryWrapper<MchProduct> gw(){
        return new LambdaQueryWrapper<>();
    }


    public static final byte STATE_CLOSE = 0;//关闭
    public static final byte STATE_OPEN = 1; //打开

    /**
     * ID
     */
    @TableId(value = "mch_product_id", type = IdType.AUTO)
    private Long mchProductId;

    /**
     * 商户号
     */
    private String mchNo;

    /**
     * 通道ID
     */
    private Long productId;

    /**
     * 状态: 0-停用, 1-正常
     */
    private Byte state;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 商户产品费率
     */
    private BigDecimal mchRate;


    /**
     * 商户代理费率
     */
    private BigDecimal agentRate;


}
