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
package com.jeequan.jeepay.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.service.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * <p>
 * 支付订单表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-27
 */
@Slf4j
@Service
public class PayOrderService extends ServiceImpl<PayOrderMapper, PayOrder> {

    /**
     * 更新订单状态  【订单生成】 --》 【支付中】
     **/
    public boolean updateInit2Ing(PayOrder payOrder) {
        payOrder.setState(PayOrder.STATE_ING);
        return update(payOrder, new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getPayOrderId, payOrder.getPayOrderId()).eq(PayOrder::getState, PayOrder.STATE_INIT));
    }

    /**
     * 更新到出码失败
     *
     * @param payOrder
     * @return
     */
    public boolean updateInit2Error(PayOrder payOrder) {
        payOrder.setState(PayOrder.STATE_ERROR);
        return update(payOrder, new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getPayOrderId, payOrder.getPayOrderId()).eq(PayOrder::getState, PayOrder.STATE_INIT));
    }

    /**
     * 更新订单状态  【支付中/关闭状态】 --》 【支付成功】
     **/
    public boolean updateIng2Success(PayOrder payOrder) {
        payOrder.setState(PayOrder.STATE_SUCCESS);
        payOrder.setSuccessTime(new Date());
        LambdaUpdateWrapper wrapper = new LambdaUpdateWrapper<PayOrder>().eq(PayOrder::getPayOrderId, payOrder.getPayOrderId()).and(wr -> {
            wr.eq(PayOrder::getState, PayOrder.STATE_CLOSED).or().eq(PayOrder::getState, PayOrder.STATE_ING);
        });
        return update(payOrder, wrapper);
    }

    /**
     * 强制补单
     *
     * @param payOrderId
     * @param loginName
     * @param beforeState
     * @return
     */
    public boolean updateOrderSuccessForce(String payOrderId, String loginName, Byte beforeState, Date successTime) {
        PayOrder updateRecord = new PayOrder();
        updateRecord.setState(PayOrder.STATE_SUCCESS);
        updateRecord.setSuccessTime(successTime);
        updateRecord.setForceChangeState(CS.YES);
        updateRecord.setForceChangeLoginName(loginName);
        updateRecord.setForceChangeBeforeState(beforeState);
        return update(updateRecord, new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getPayOrderId, payOrderId));
    }

    /**
     * 将成功状态的订单修改为测试冲正
     *
     * @param payOrderId
     * @return
     */
    public boolean updateOrderSuccessToRedo(String payOrderId) {
        PayOrder updateRecord = new PayOrder();
        updateRecord.setState(PayOrder.STATE_REFUND);
        updateRecord.setUpdatedAt(new Date());
//        updateRecord.setForceChangeState(CS.YES);
//        updateRecord.setForceChangeLoginName(loginName);
//        updateRecord.setForceChangeBeforeState(beforeState);
        return update(updateRecord, new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getPayOrderId, payOrderId));
    }

    /**
     * 更新订单状态  【支付中】 --》 【订单关闭】
     **/
    public boolean updateIng2Close(String payOrderId) {

        PayOrder updateRecord = new PayOrder();
        updateRecord.setState(PayOrder.STATE_CLOSED);

        return update(updateRecord, new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getPayOrderId, payOrderId).eq(PayOrder::getState, PayOrder.STATE_ING));
    }

    /**
     * 更新订单状态  【订单生成】 --》 【订单关闭】
     **/
    public boolean updateInit2Close(String payOrderId) {

        PayOrder updateRecord = new PayOrder();
        updateRecord.setState(PayOrder.STATE_CLOSED);

        return update(updateRecord, new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getPayOrderId, payOrderId).eq(PayOrder::getState, PayOrder.STATE_INIT));
    }


    /**
     * 更新订单状态  【支付中】 --》 【支付失败】
     **/
    public boolean updateIng2Fail(PayOrder payOrder) {
        payOrder.setState(PayOrder.STATE_FAIL);
        return update(payOrder, new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getPayOrderId, payOrder.getPayOrderId()).eq(PayOrder::getState, PayOrder.STATE_ING));
    }

    /**
     * 查询商户订单
     **/
    public PayOrder queryMchOrder(String mchNo, String payOrderId, String mchOrderNo) {

        if (StringUtils.isNotEmpty(payOrderId)) {
            return getOne(PayOrder.gw().eq(PayOrder::getMchNo, mchNo).eq(PayOrder::getPayOrderId, payOrderId));
        } else if (StringUtils.isNotEmpty(mchOrderNo)) {
            return getOne(PayOrder.gw().eq(PayOrder::getMchNo, mchNo).eq(PayOrder::getMchOrderNo, mchOrderNo));
        } else {
            return null;
        }
    }

    /**
     * 更新订单为 超时状态
     **/
    public Integer updateOrderExpired() {

        PayOrder payOrder = new PayOrder();
        payOrder.setState(PayOrder.STATE_CLOSED);

        return baseMapper.update(payOrder,
                PayOrder.gw()
                        .in(PayOrder::getState, Arrays.asList(PayOrder.STATE_INIT, PayOrder.STATE_ING))
                        .le(PayOrder::getExpiredTime, new Date())
        );
    }

    /**
     * 更新订单 通知状态 --> 已发送
     **/
    public int updateNotifySent(String payOrderId) {
        PayOrder payOrder = new PayOrder();
        payOrder.setNotifyState(CS.YES);
        payOrder.setPayOrderId(payOrderId);
        return baseMapper.updateById(payOrder);
    }

    /**
     * 通用列表查询条件
     *
     * @param iPage
     * @param payOrder
     * @param paramJSON
     * @param wrapper
     * @return
     */
    public IPage<PayOrder> listByPage(IPage iPage, PayOrder payOrder, JSONObject paramJSON, LambdaQueryWrapper<PayOrder> wrapper) {

        if (StringUtils.isNotEmpty(payOrder.getPayOrderId())) {
            wrapper.eq(PayOrder::getPayOrderId, payOrder.getPayOrderId().trim());
        }
        //商户号
        if (StringUtils.isNotEmpty(payOrder.getMchNo())) {
            wrapper.like(PayOrder::getMchNo, payOrder.getMchNo().trim());
        }
        //商户名
        if (StringUtils.isNotEmpty(payOrder.getMchName())) {
            wrapper.like(PayOrder::getMchName, payOrder.getMchName().trim());
        }
        //商户代理
        if (StringUtils.isNotEmpty(payOrder.getAgentNo())) {
            wrapper.eq(PayOrder::getAgentNo, payOrder.getAgentNo().trim());
        }
        //通道代理
        if (StringUtils.isNotEmpty(payOrder.getAgentNoPassage())) {
            wrapper.eq(PayOrder::getAgentNoPassage, payOrder.getAgentNoPassage().trim());
        }
        //通道ID
        if (payOrder.getPassageId() != null) {
            wrapper.eq(PayOrder::getPassageId, payOrder.getPassageId());
        }
        //商户订单号
        if (StringUtils.isNotEmpty(payOrder.getMchOrderNo())) {
            wrapper.eq(PayOrder::getMchOrderNo, payOrder.getMchOrderNo().trim());
        }
        if (payOrder.getState() != null) {
            wrapper.eq(PayOrder::getState, payOrder.getState());
        }
        if (payOrder.getNotifyState() != null) {
            wrapper.eq(PayOrder::getNotifyState, payOrder.getNotifyState());
        }
        if (payOrder.getProductId() != null) {
            wrapper.eq(PayOrder::getProductId, payOrder.getProductId());
        }

        if (payOrder.getForceChangeState() != null) {
            wrapper.eq(PayOrder::getForceChangeState, payOrder.getForceChangeState());
        }

        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                wrapper.ge(PayOrder::getCreatedAt, paramJSON.getString("createdStart"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                wrapper.le(PayOrder::getCreatedAt, paramJSON.getString("createdEnd"));
            }
        }
        // 三合一订单
        if (paramJSON != null && StringUtils.isNotEmpty(paramJSON.getString("unionOrderId"))) {
            wrapper.and(wr -> {
                wr.eq(PayOrder::getPayOrderId, paramJSON.getString("unionOrderId").trim())
                        .or().eq(PayOrder::getMchOrderNo, paramJSON.getString("unionOrderId").trim())
                        .or().eq(PayOrder::getPassageOrderNo, paramJSON.getString("unionOrderId").trim());
            });
        }

        wrapper.orderByDesc(PayOrder::getCreatedAt);

        return page(iPage, wrapper);
    }

    public IPage<PayOrder> listByPageUpdatedAt(IPage iPage, PayOrder payOrder, JSONObject paramJSON, LambdaQueryWrapper<PayOrder> wrapper) {

        if (StringUtils.isNotEmpty(payOrder.getPayOrderId())) {
            wrapper.eq(PayOrder::getPayOrderId, payOrder.getPayOrderId().trim());
        }
        //商户号
        if (StringUtils.isNotEmpty(payOrder.getMchNo())) {
            wrapper.like(PayOrder::getMchNo, payOrder.getMchNo().trim());
        }
        //商户名
        if (StringUtils.isNotEmpty(payOrder.getMchName())) {
            wrapper.like(PayOrder::getMchName, payOrder.getMchName().trim());
        }
        //商户代理
        if (StringUtils.isNotEmpty(payOrder.getAgentNo())) {
            wrapper.eq(PayOrder::getAgentNo, payOrder.getAgentNo().trim());
        }
        //通道代理
        if (StringUtils.isNotEmpty(payOrder.getAgentNoPassage())) {
            wrapper.eq(PayOrder::getAgentNoPassage, payOrder.getAgentNoPassage().trim());
        }
        //通道ID
        if (payOrder.getPassageId() != null) {
            wrapper.eq(PayOrder::getPassageId, payOrder.getPassageId());
        }
        //商户订单号
        if (StringUtils.isNotEmpty(payOrder.getMchOrderNo())) {
            wrapper.eq(PayOrder::getMchOrderNo, payOrder.getMchOrderNo().trim());
        }
        if (payOrder.getState() != null) {
            wrapper.eq(PayOrder::getState, payOrder.getState());
        }
        if (payOrder.getNotifyState() != null) {
            wrapper.eq(PayOrder::getNotifyState, payOrder.getNotifyState());
        }
        if (payOrder.getProductId() != null) {
            wrapper.eq(PayOrder::getProductId, payOrder.getProductId());
        }

        if (payOrder.getForceChangeState() != null) {
            wrapper.eq(PayOrder::getForceChangeState, payOrder.getForceChangeState());
        }

        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                wrapper.ge(PayOrder::getUpdatedAt, paramJSON.getString("createdStart"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                wrapper.le(PayOrder::getUpdatedAt, paramJSON.getString("createdEnd"));
            }
        }
        // 三合一订单
        if (paramJSON != null && StringUtils.isNotEmpty(paramJSON.getString("unionOrderId"))) {
            wrapper.and(wr -> {
                wr.eq(PayOrder::getPayOrderId, paramJSON.getString("unionOrderId").trim())
                        .or().eq(PayOrder::getMchOrderNo, paramJSON.getString("unionOrderId").trim())
                        .or().eq(PayOrder::getPassageOrderNo, paramJSON.getString("unionOrderId").trim());
            });
        }

        wrapper.orderByDesc(PayOrder::getUpdatedAt);

        return page(iPage, wrapper);
    }

    public List<PayOrder> listByQueryUpdatedAt(PayOrder payOrder, JSONObject paramJSON, LambdaQueryWrapper<PayOrder> wrapper) {

        if (StringUtils.isNotEmpty(payOrder.getPayOrderId())) {
            wrapper.eq(PayOrder::getPayOrderId, payOrder.getPayOrderId().trim());
        }
        //商户号
        if (StringUtils.isNotEmpty(payOrder.getMchNo())) {
            wrapper.like(PayOrder::getMchNo, payOrder.getMchNo().trim());
        }
        //商户名
        if (StringUtils.isNotEmpty(payOrder.getMchName())) {
            wrapper.like(PayOrder::getMchName, payOrder.getMchName().trim());
        }
        //商户代理
        if (StringUtils.isNotEmpty(payOrder.getAgentNo())) {
            wrapper.eq(PayOrder::getAgentNo, payOrder.getAgentNo().trim());
        }
        //通道代理
        if (StringUtils.isNotEmpty(payOrder.getAgentNoPassage())) {
            wrapper.eq(PayOrder::getAgentNoPassage, payOrder.getAgentNoPassage().trim());
        }
        //通道ID
        if (payOrder.getPassageId() != null) {
            wrapper.eq(PayOrder::getPassageId, payOrder.getPassageId());
        }
        //商户订单号
        if (StringUtils.isNotEmpty(payOrder.getMchOrderNo())) {
            wrapper.eq(PayOrder::getMchOrderNo, payOrder.getMchOrderNo().trim());
        }
        if (payOrder.getState() != null) {
            wrapper.eq(PayOrder::getState, payOrder.getState());
        }
        if (payOrder.getNotifyState() != null) {
            wrapper.eq(PayOrder::getNotifyState, payOrder.getNotifyState());
        }
        if (payOrder.getProductId() != null) {
            wrapper.eq(PayOrder::getProductId, payOrder.getProductId());
        }

        if (payOrder.getForceChangeState() != null) {
            wrapper.eq(PayOrder::getForceChangeState, payOrder.getForceChangeState());
        }

        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                wrapper.ge(PayOrder::getUpdatedAt, paramJSON.getString("createdStart"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                wrapper.le(PayOrder::getUpdatedAt, paramJSON.getString("createdEnd"));
            }
        }
        // 三合一订单
        if (paramJSON != null && StringUtils.isNotEmpty(paramJSON.getString("unionOrderId"))) {
            wrapper.and(wr -> {
                wr.eq(PayOrder::getPayOrderId, paramJSON.getString("unionOrderId").trim())
                        .or().eq(PayOrder::getMchOrderNo, paramJSON.getString("unionOrderId").trim())
                        .or().eq(PayOrder::getPassageOrderNo, paramJSON.getString("unionOrderId").trim());
            });
        }

        wrapper.orderByDesc(PayOrder::getUpdatedAt);

        return list(wrapper);
    }

    public List<PayOrder> listByQuery(PayOrder payOrder, JSONObject paramJSON, LambdaQueryWrapper<PayOrder> wrapper) {

        if (StringUtils.isNotEmpty(payOrder.getPayOrderId())) {
            wrapper.eq(PayOrder::getPayOrderId, payOrder.getPayOrderId());
        }
        //商户号
        if (StringUtils.isNotEmpty(payOrder.getMchNo())) {
            wrapper.like(PayOrder::getMchNo, payOrder.getMchNo());
        }
        //商户名
        if (StringUtils.isNotEmpty(payOrder.getMchName())) {
            wrapper.like(PayOrder::getMchName, payOrder.getMchName());
        }
        //商户代理
        if (StringUtils.isNotEmpty(payOrder.getAgentNo())) {
            wrapper.eq(PayOrder::getAgentNo, payOrder.getAgentNo());
        }
        //通道代理
        if (StringUtils.isNotEmpty(payOrder.getAgentNoPassage())) {
            wrapper.eq(PayOrder::getAgentNoPassage, payOrder.getAgentNoPassage());
        }
        //通道ID
        if (payOrder.getPassageId() != null) {
            wrapper.eq(PayOrder::getPassageId, payOrder.getPassageId());
        }
        //商户订单号
        if (StringUtils.isNotEmpty(payOrder.getMchOrderNo())) {
            wrapper.eq(PayOrder::getMchOrderNo, payOrder.getMchOrderNo());
        }
        if (payOrder.getState() != null) {
            wrapper.eq(PayOrder::getState, payOrder.getState());
        }
        if (payOrder.getNotifyState() != null) {
            wrapper.eq(PayOrder::getNotifyState, payOrder.getNotifyState());
        }
        if (payOrder.getProductId() != null) {
            wrapper.eq(PayOrder::getProductId, payOrder.getProductId());
        }

        if (payOrder.getForceChangeState() != null) {
            wrapper.eq(PayOrder::getForceChangeState, payOrder.getForceChangeState());
        }

        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                wrapper.ge(PayOrder::getCreatedAt, paramJSON.getString("createdStart"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                wrapper.le(PayOrder::getCreatedAt, paramJSON.getString("createdEnd"));
            }
        }
        // 三合一订单
        if (paramJSON != null && StringUtils.isNotEmpty(paramJSON.getString("unionOrderId"))) {
            wrapper.and(wr -> {
                wr.eq(PayOrder::getPayOrderId, paramJSON.getString("unionOrderId"))
                        .or().eq(PayOrder::getMchOrderNo, paramJSON.getString("unionOrderId"))
                        .or().eq(PayOrder::getPassageOrderNo, paramJSON.getString("unionOrderId"));
            });
        }

        wrapper.orderByDesc(PayOrder::getCreatedAt);

        return list(wrapper);
    }

    /**
     * 过期订单清理
     *
     * @param offsetDate
     * @param pageSize
     */
    public void ClearPayOrder(Date offsetDate, int pageSize) {
        long currentPageIndex = 1;

        log.info("【数据定时清理任务开始执行】{}", new Date());
        LambdaQueryWrapper<PayOrder> lambdaQueryWrapper = PayOrder.gw().le(PayOrder::getCreatedAt, offsetDate);

        while (true) {

            try {
                IPage<PayOrder> payOrderIPage = page(new Page(1, pageSize), lambdaQueryWrapper);

                log.info("【过期订单共计】{}", payOrderIPage.getTotal());
                log.info("【过期订单页数】{}", payOrderIPage.getPages());

                if (payOrderIPage == null || payOrderIPage.getRecords().isEmpty()) { //本次查询无结果, 不再继续查询;
                    log.info("【本次查询无结果】{} 中止定时任务", new Date());
                    break;
                }

                List ids = new ArrayList();
                for (PayOrder payOrder : payOrderIPage.getRecords()) {
                    ids.add(payOrder.getPayOrderId());
                }
                boolean resultOrder = removeByIds(ids);

                log.info("【数据定时清理任务执行中】 本次清理过期订单{}条", pageSize);
                log.info("【数据定时清理任务执行中】 本次清理 order {}", resultOrder);

                currentPageIndex++;

                log.info("【数据定时清理任务执行中】 当前次数 {}", currentPageIndex);
                log.info("【过期订单页数】{}", payOrderIPage.getPages());
            } catch (Exception e) { //出现异常，直接退出，避免死循环。
                log.info("【数据定时清理任务异常】error {}", e);
                break;
            }
        }
    }
}
