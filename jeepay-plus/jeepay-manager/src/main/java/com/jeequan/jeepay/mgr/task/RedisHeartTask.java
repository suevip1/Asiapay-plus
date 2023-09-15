package com.jeequan.jeepay.mgr.task;

import com.jeequan.jeepay.core.cache.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisHeartTask {

    private static final String HEART = "redis_heart";

    @Scheduled(fixedRate = 5000) // 每5秒执行一次 5000
    public void start() {
        RedisUtil.getString(HEART);
    }
}
