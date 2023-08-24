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
 * 支付通道表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_pay_passage")
public class PayPassage extends BaseModel {

    private static final long serialVersionUID=1L;

    public static final byte STATE_OPEN = 1; //通道打开
    public static final byte STATE_CLOSE = 0; //通道关闭


    public static final byte PAY_TYPE_RANGE = 1; //收款规则：1 10-5000
    public static final byte PAY_TYPE_SPECIFIED = 2; //收款规则：2 指定金额 10|20|30


    public static final LambdaQueryWrapper<PayPassage> gw(){
        return new LambdaQueryWrapper<>();
    }
    /**
     * ID
     */
    @TableId(value = "pay_passage_id", type = IdType.AUTO)
    private Long payPassageId;

    /**
     * 通道名称
     */
    private String payPassageName;

    /**
     * 支付接口代码
     */
    private String ifCode;

    /**
     * 对应产品ID
     */
    private Long productId;

    /**
     * 通道费率(实际三方通道成本)
     */
    private BigDecimal rate;

    /**
     * 收款方式：1-区间范围 如:10-5000，2-固定金额 如:100|200|500 以|为分隔符
     */
    private Byte payType;

    /**
     * 收款规则
     */
    private String payRules;

    /**
     * 三方通道配置数据
     */
    private String payInterfaceConfig;

    /**
     * 通道余额
     */
    private Long balance;

    /**
     * 状态: 0-停用, 1-启用
     */
    private Byte state;

    /**
     * 轮询权重:任意正整数,设置为0则不会拉起该通道
     */
    private Integer weights;

    /**
     * 额度限制状态: 0-停用, 1-启用
     */
    private Byte quotaLimitState;

    /**
     * 通道授信额度
     */
    private Long quota;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 通道的上级代理
     */
    private String agentNo;

    /**
     * 通道的上级代理费率
     */
    private BigDecimal agentRate;

    /**
     * 通道可用时间设置:0-停用, 1-启用
     */
    private Byte timeLimit;

    /**
     * 通道执行时间规则，|分割
     */
    private String timeRules;

}
