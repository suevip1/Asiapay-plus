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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.entity.MchNotifyRecord;
import com.jeequan.jeepay.service.mapper.MchNotifyRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 商户通知表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-27
 */
@Slf4j
@Service
public class MchNotifyRecordService extends ServiceImpl<MchNotifyRecordMapper, MchNotifyRecord> {

    /**
     * 根据订单号和类型查询
     */
    public MchNotifyRecord findByOrderAndType(String orderId, Byte orderType) {
        return getOne(
                MchNotifyRecord.gw().eq(MchNotifyRecord::getOrderId, orderId).eq(MchNotifyRecord::getOrderType, orderType)
        );
    }

    /**
     * 查询支付订单
     */
    public MchNotifyRecord findByPayOrder(String orderId) {
        return findByOrderAndType(orderId, MchNotifyRecord.TYPE_PAY_ORDER);
    }

    public Integer updateNotifyResult(Long notifyId, Byte state, String resResult) {
        return baseMapper.updateNotifyResult(notifyId, state, resResult);
    }

    /**
     * 清理订单通知
     * @param offsetDate
     * @param pageSize
     */
    public void ClearMchNotify(Date offsetDate, int pageSize) {
        long currentPageIndex = 1;
        while (true) {
            try {
                LambdaQueryWrapper<MchNotifyRecord> lambdaNotifyRecordQueryWrapper = MchNotifyRecord.gw().le(MchNotifyRecord::getCreatedAt, offsetDate);
                IPage<MchNotifyRecord> mchNotifyRecordIPage = page(new Page(1, pageSize), lambdaNotifyRecordQueryWrapper);

                log.info("【过期通知共计】{}", mchNotifyRecordIPage.getTotal());
                log.info("【过期通知页数】{}", mchNotifyRecordIPage.getPages());

                if (mchNotifyRecordIPage == null || mchNotifyRecordIPage.getRecords().isEmpty()) { //本次查询无结果, 不再继续查询;
                    log.info("【本次查询通知无结果】{} 中止定时任务", new Date());
                    break;
                }

                List ids = new ArrayList();
                for (MchNotifyRecord mchNotifyRecord : mchNotifyRecordIPage.getRecords()) {
                    ids.add(mchNotifyRecord.getNotifyId());
                }
                boolean resultNotify = removeByIds(ids);

                log.info("【数据定时清理任务执行中】 本次清理过期通知{}条", pageSize);
                log.info("【数据定时清理任务执行中】 本次清理 MchNotifyRecord {}", resultNotify);

                currentPageIndex++;

                log.info("【数据定时清理任务执行中】 当前次数 {}", currentPageIndex);
                log.info("【过期订单页数】{}", mchNotifyRecordIPage.getPages());
            } catch (Exception e) { //出现异常，直接退出，避免死循环。
                log.info("【数据定时清理通知任务异常】error {}", e);
                break;
            }
        }
    }

}
