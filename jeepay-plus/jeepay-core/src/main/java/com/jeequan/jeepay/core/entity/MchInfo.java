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
 * 商户信息表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_info")
public class MchInfo extends BaseModel{

    private static final long serialVersionUID=1L;

    //gw
    public static final LambdaQueryWrapper<MchInfo> gw(){
        return new LambdaQueryWrapper<>();
    }


    public static final byte STATE_CLOSE = 0;//关闭
    public static final byte STATE_OPEN = 1; //打开

    /**
     * 商户号
     */
    @TableId(value = "mch_no", type = IdType.INPUT)
    private String mchNo;

    /**
     * 商户名称
     */
    private String mchName;

    /**
     * 商户状态: 0-停用, 1-正常
     */
    private Byte state;

    /**
     * 商户备注
     */
    private String remark;

    /**
     * 初始用户ID（创建商户时，初始用户ID）
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
     * 商户密钥
     */
    private String secret;

    /**
     * 上级代理商户号,为空则无
     */
    private String agentNo;

    /**
     * 商户分组ID
     */
    private Long mchGroupId;

    /**
     * 商户余额
     */
    private Long balance;

    /**
     * 冻结金额
     */
    private Long freezeBalance;

}
