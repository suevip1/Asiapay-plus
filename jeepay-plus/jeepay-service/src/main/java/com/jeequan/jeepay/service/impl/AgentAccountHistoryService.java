package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeequan.jeepay.core.entity.AgentAccountHistory;
import com.jeequan.jeepay.service.mapper.AgentAccountHistoryMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 代理商资金账户流水表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
@Slf4j
@Service
public class AgentAccountHistoryService extends ServiceImpl<AgentAccountHistoryMapper, AgentAccountHistory> {
    /**
     * 清理代理流水
     * @param offsetDate
     * @param pageSize
     */
    public void ClearAgentHistory(Date offsetDate, int pageSize) {
        long currentPageIndex = 1;
        while (true) {
            try {
                LambdaQueryWrapper<AgentAccountHistory> lambdaQueryWrapper = AgentAccountHistory.gw().le(AgentAccountHistory::getCreatedAt, offsetDate);
                IPage<AgentAccountHistory> iPage = page(new Page(1, pageSize), lambdaQueryWrapper);

                log.info("【过期通知共计】{}", iPage.getTotal());
                log.info("【过期通知页数】{}", iPage.getPages());

                if (iPage == null || iPage.getRecords().isEmpty()) { //本次查询无结果, 不再继续查询;
                    log.info("【本次查询通知无结果】{} 中止定时任务", new Date());
                    break;
                }

                List ids = new ArrayList();
                for (AgentAccountHistory record : iPage.getRecords()) {
                    ids.add(record.getAgentAccountHistoryId());
                }
                boolean result = removeByIds(ids);

                log.info("【数据定时清理任务执行中】 本次清理过期代理流水{}条", pageSize);
                log.info("【数据定时清理任务执行中】 本次清理 AgentAccountHistory {}", result);

                currentPageIndex++;

                log.info("【数据定时清理任务执行中】 当前次数 {}", currentPageIndex);
                log.info("【过期订单页数】{}", iPage.getPages());
            } catch (Exception e) { //出现异常，直接退出，避免死循环。
                log.info("【数据定时清理通知任务异常】error {}", e);
                break;
            }
        }
    }
}
