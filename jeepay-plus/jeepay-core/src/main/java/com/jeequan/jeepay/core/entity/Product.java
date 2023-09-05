package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeequan.jeepay.core.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 支付产品表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_product")
public class Product extends BaseModel {

    public static final LambdaQueryWrapper<Product> gw() {
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID = 1L;

    /**
     * 产品ID
     */
    @TableId(value = "product_id", type = IdType.INPUT)
    private Long productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;

    /**
     * 状态: 0-停用, 1-正常
     */
    private Byte state;

    /**
     * 允许低于成本价格拉起状态 0-停用, 1-启用
     */
    private Byte limitState;
}
