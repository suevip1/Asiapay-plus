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
import com.jeequan.jeepay.core.entity.SysLog;
import com.jeequan.jeepay.core.entity.SysLog;
import com.jeequan.jeepay.service.mapper.SysLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 系统操作日志表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-27
 */
@Slf4j
@Service
public class SysLogService extends ServiceImpl<SysLogMapper, SysLog> {

    /**
     * 清理系统日志
     * @param offsetDate
     * @param pageSize
     */
    public void ClearSysLog(Date offsetDate, int pageSize) {
        long currentPageIndex = 1;
        while (true) {
            try {
                LambdaQueryWrapper<SysLog> lambdaQueryWrapper = SysLog.gw().le(SysLog::getCreatedAt, offsetDate);
                IPage<SysLog> iPage = page(new Page(1, pageSize), lambdaQueryWrapper);

                log.info("【过期系统日志共计】{}", iPage.getTotal());
                log.info("【过期系统日志页数】{}", iPage.getPages());

                if (iPage == null || iPage.getRecords().isEmpty()) { //本次查询无结果, 不再继续查询;
                    log.info("【本次查询系统日志无结果】{} 中止定时任务", new Date());
                    break;
                }

                List ids = new ArrayList();
                for (SysLog record : iPage.getRecords()) {
                    ids.add(record.getSysLogId());
                }
                boolean result = removeByIds(ids);

                log.info("【数据定时清理任务执行中】 本次清理系统日志{}条", pageSize);
                log.info("【数据定时清理任务执行中】 本次清理 SysLog {}", result);

                currentPageIndex++;

                log.info("【数据定时清理任务执行中】 当前次数 {}", currentPageIndex);
                log.info("【过期系统日志页数】{}", iPage.getPages());
            } catch (Exception e) { //出现异常，直接退出，避免死循环。
                log.info("【数据定时清理系统日志任务异常】error {}", e);
                break;
            }
        }
    }
}
