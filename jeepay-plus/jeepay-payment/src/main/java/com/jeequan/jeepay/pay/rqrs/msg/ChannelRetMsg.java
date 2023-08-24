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
package com.jeequan.jeepay.pay.rqrs.msg;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

/*
* 上游渠道侧响应信息包装类
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:31
*/
@Slf4j
@Data
public class ChannelRetMsg implements Serializable {

    /**
     * 上游渠道返回状态
     */
    private ChannelState channelState;

    /**
     * 渠道订单号
     */
    private String channelOrderId;

    /**
     * 渠道用户标识,一般为三方通道用户号
     */
    private String channelUserId;

    /**
     * 渠道错误码
     */
    private String channelErrCode;

    /**
     * 渠道错误描述
     */
    private String channelErrMsg;

    /**
     * 渠道支付数据包, 一般用于支付订单的继续支付操作
     */
    private String channelAttach;

    /**
     * 上游渠道返回的原始报文, 一般用于[运营平台的查询上游结果]功能
     */
    private String channelOriginResponse;

    /**
     * 是否需要轮询查单（比如微信条码支付） 默认不查询订单
     */
    private boolean isNeedQuery = false;

    /**
     * 响应结果（一般用于回调接口返回给上游数据 ）
     */
    private ResponseEntity responseEntity;

    /**
     * 渠道状态枚举值
     */
    public enum ChannelState {
        /**
         * 接口正确返回： 业务状态已经明确成功
         */
        CONFIRM_SUCCESS,
        /**
         * 拉起订单成功,等待支付
         */
        WAITING,
        /**
         * 接口正确返回： 业务状态已经明确失败
         */
        CONFIRM_FAIL,
        /**
         * 异常: 超时等
         */
        SYS_ERROR
    }

    //静态初始函数
    public ChannelRetMsg(){}
    public ChannelRetMsg(ChannelState channelState, String channelOrderId, String channelErrCode, String channelErrMsg) {
        this.channelState = channelState;
        this.channelOrderId = channelOrderId;
        this.channelErrCode = channelErrCode;
        this.channelErrMsg = channelErrMsg;
    }

    /**
     * 明确成功
     * @param channelOrderId
     * @return
     */
    public static ChannelRetMsg confirmSuccess(String channelOrderId){
        return new ChannelRetMsg(ChannelState.CONFIRM_SUCCESS, channelOrderId, null, null);
    }

    /**
     * 接口返回成功，等待支付
     * @param channelOrderId
     * @return
     */
    public static ChannelRetMsg confirmWaiting(String channelOrderId){
        return new ChannelRetMsg(ChannelState.CONFIRM_SUCCESS, channelOrderId, null, null);
    }

    /**
     * 明确失败
     * @param channelErrCode
     * @param channelErrMsg
     * @return
     */
    public static ChannelRetMsg confirmFail(String channelErrCode, String channelErrMsg){
        return new ChannelRetMsg(ChannelState.CONFIRM_FAIL, null, channelErrCode, channelErrMsg);
    }

    /**
     * 明确失败
     * @param channelOrderId
     * @param channelErrCode
     * @param channelErrMsg
     * @return
     */
    public static ChannelRetMsg confirmFail(String channelOrderId, String channelErrCode, String channelErrMsg){
        return new ChannelRetMsg(ChannelState.CONFIRM_FAIL, channelOrderId, channelErrCode, channelErrMsg);
    }

    /**
     * 明确失败
     * @param channelOrderId
     * @return
     */
    public static ChannelRetMsg confirmFail(String channelOrderId){
        return new ChannelRetMsg(ChannelState.CONFIRM_FAIL, channelOrderId, null, null);
    }

    /**
     * 明确失败
     * @return
     */
    public static ChannelRetMsg confirmFail(){
        return new ChannelRetMsg(ChannelState.CONFIRM_FAIL, null, null, null);
    }

    /**
     * 处理中
     * @return
     */
    public static ChannelRetMsg waiting(){
        return new ChannelRetMsg(ChannelState.WAITING, null, null, null);
    }

    /**
     * 异常的情况
     * @param channelErrMsg
     * @return
     */
    public static ChannelRetMsg sysError(String channelErrMsg){
        return new ChannelRetMsg(ChannelState.SYS_ERROR, null, null, "系统：" + channelErrMsg);
    }
}





