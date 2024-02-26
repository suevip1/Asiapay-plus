/*
 * Copyright (c) 2021-2031
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeequan.jeepay.core.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 支付订单表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_pay_order")
public class PayOrder extends BaseModel {

    public static final LambdaQueryWrapper<PayOrder> gw() {
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID = 1L;
    //支付状态: 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-测试冲正, 6-订单关闭 ,7-出码失败，
    /**
     * 订单生成
     */
    public static final byte STATE_INIT = 0;
    /**
     * 支付中
     */
    public static final byte STATE_ING = 1;
    /**
     * 支付成功
     */
    public static final byte STATE_SUCCESS = 2;
    /**
     * 支付失败
     */
    public static final byte STATE_FAIL = 3;
    /**
     * 已撤销
     */
    public static final byte STATE_CANCEL = 4;
    /**
     * 测试冲正(退款)
     */
    public static final byte STATE_REFUND = 5;
    /**
     * 订单关闭
     */
    public static final byte STATE_CLOSED = 6;

    /**
     * 出码失败- 拉起通道时异常
     */
    public static final byte STATE_ERROR = 7;

    /**
     * 调额入账（部分金额入账）
     */
    public static final byte STATE_CHANGE = 8;

    public static final byte DIVISION_MODE_FORBID = 0; //该笔订单不允许分账
    public static final byte DIVISION_MODE_AUTO = 1; //支付成功按配置自动完成分账
    public static final byte DIVISION_MODE_MANUAL = 2; //商户手动分账(解冻商户金额)

    public static final byte DIVISION_STATE_UNHAPPEN = 0; //未发生分账
    public static final byte DIVISION_STATE_WAIT_TASK = 1; //等待分账任务处理
    public static final byte DIVISION_STATE_ING = 2; //分账处理中
    public static final byte DIVISION_STATE_FINISH = 3; //分账任务已结束(不体现状态)


    /**
     * 支付订单号
     */
    @TableId(value = "pay_order_id", type = IdType.INPUT)
    private String payOrderId;

    /**
     * 商户号
     */
    private String mchNo;

    /**
     * 代理商号
     */
    private String agentNo;

    /**
     * 通道ID
     */
    private Long passageId;

    /**
     * 商户名称
     */
    private String mchName;

    /**
     * 通道订单号
     */
    private String passageOrderNo;

    /**
     * 支付接口代码
     */
    private String ifCode;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 支付金额,单位分
     */
    private Long amount;

    /**
     * 商户手续费费率快照
     */
    private BigDecimal mchFeeRate;

    /**
     * 商户手续费,单位分
     */
    private Long mchFeeAmount;

    /**
     * 代理商费率快照
     */
    private BigDecimal agentRate;

    /**
     * 代理商分润,单位分
     */
    private Long agentFeeAmount;

    /**
     * 支付状态: 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-已退款, 6-订单关闭
     */
    private Byte state;

    /**
     * 向下游回调状态, 0-未发送,  1-已发送
     */
    private Byte notifyState;

    /**
     * 回调参数(三方回调我方接口)
     */
    private String notifyParams;

    /**
     * 回调响应(我方给三方的响应)
     */
    private String notifyResp;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 下单请求返回
     */
    private String passageResp;

    /**
     * 渠道支付错误码
     */
    private String errCode;

    /**
     * 渠道支付错误描述
     */
    private String errMsg;

    /**
     * 订单扩展参数(暂时保留)
     */
    private String extParamJson;

    /**
     * 异步通知地址
     */
    private String notifyUrl;

    /**
     * 页面跳转地址
     */
    private String returnUrl;

    /**
     * 订单失效时间
     */
    private Date expiredTime;

    /**
     * 订单支付成功时间
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

    /**
     * 商户订单号
     */
    private String mchOrderNo;

    /**
     * 通道费率快照
     */
    private BigDecimal passageRate;

    /**
     * 通道费用快照
     */
    private Long passageFeeAmount;

    /**
     * 三方通道用户标识
     */
    private String passageUserId;

    /**
     * 通道代理商手续费,单位分
     */
    private Long agentPassageFee;

    /**
     * 通道代理商费率
     */
    private BigDecimal agentPassageRate;

    /**
     * 通道的代理商户
     */
    private String agentNoPassage;

    /**
     * 是否强制补单, 0-否,  1-是
     */
    private Byte forceChangeState;

    /**
     * 手动补单时的操作员
     */
    private String forceChangeLoginName;

    /**
     * 补单前订单状态
     */
    private Byte forceChangeBeforeState;
}
