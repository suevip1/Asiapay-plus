import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.entity.PayOrder;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public class TestRun {
    private static final String REDIS_SUFFIX = "Statistics";

    public static void main(String[] args) {
        Date now = DateUtil.parse(DateUtil.now());
        Date newEndDate = DateUtil.offsetMinute(now, 30);

        long betweenDay = DateUtil.between(now, newEndDate, DateUnit.MINUTE);
        log.info(now.toString());
        log.info(betweenDay + "");
    }

}
