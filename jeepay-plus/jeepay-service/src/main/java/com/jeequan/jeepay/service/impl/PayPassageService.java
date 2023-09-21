package com.jeequan.jeepay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.entity.AgentAccountInfo;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.service.mapper.PayPassageMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 支付通道表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
@Slf4j
@Service
public class PayPassageService extends ServiceImpl<PayPassageMapper, PayPassage> {


    private final static String REDIS_SUFFIX = "Passage_Pay_Config";
    @Resource
    private PayPassageMapper payPassageMapper;

    public PayPassage queryPassageInfo(Long payPassageId) {
        //查询缓存中是否有
        PayPassage payPassage = getById(payPassageId);
        if (payPassage == null) {
            throw new BizException("没有查询到通道");
        }
        return payPassage;
    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = {Exception.class})
    public void updatePassageInfo(PayPassage payPassage) {
        try {
            PayPassage passageOld = getById(payPassage.getPayPassageId());
            String oldConfig = passageOld.getPayInterfaceConfig();
            String newConfig = payPassage.getPayInterfaceConfig();
            //同步最新余额
            payPassage.setBalance(passageOld.getBalance());

            if (StringUtils.isNotEmpty(oldConfig) && StringUtils.isNotEmpty(newConfig)) {
                if (!oldConfig.equals(newConfig)) {
                    RedisUtil.addToQueue(REDIS_SUFFIX, passageOld);
                    log.info("通道三方配置被修改,[{}]{}", payPassage.getPayPassageId(), payPassage.getPayPassageName());
                }
            }
            boolean isSuccess = update(payPassage, PayPassage.gw().eq(PayPassage::getPayPassageId, payPassage.getPayPassageId()));
            if (!isSuccess) {
                throw new BizException("修改失败,更新通道失败,通道名不能重复");
            }
        } catch (Exception e) {
            log.error("数据库异常,更新通道失败");
            log.error(e.getMessage(), e);
            throw new BizException("修改失败,更新通道失败,通道名不能重复");
        }
    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = {Exception.class})
    public boolean removePayPassage(Long payPassageId) {
        boolean removePayPassage = removeById(payPassageId);
        if (!removePayPassage) {
            throw new BizException("删除当前通道失败");
        }
        return true;
    }


    public Map<Long, PayPassage> getPayPassageMap() {
        List<PayPassage> passageList = list();
        Map<Long, PayPassage> payPassageMap = new HashMap<>();

        for (int i = 0; i < passageList.size(); i++) {
            payPassageMap.put(passageList.get(i).getPayPassageId(), passageList.get(i));
        }
        return payPassageMap;
    }

    /**
     * 更新账户余额
     *
     * @param payPassageId
     * @param changeAmount
     * @return
     */
    @Transactional(transactionManager = "transactionManager", rollbackFor = {Exception.class})
    public boolean updateBalance(Long payPassageId, Long changeAmount) {
        try {
            PayPassage payPassage = queryPassageInfo(payPassageId);
            payPassage.setBalance(payPassage.getBalance() + changeAmount);
            boolean isSuccess = updateById(payPassage);
            if (!isSuccess) {
                log.error("更新余额不成功 [" + payPassage.getPayPassageId() + "] " + changeAmount);
            }
            return isSuccess;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException("数据更新异常");
        }
    }

    /**
     * 更新授信余额
     *
     * @param payPassageId
     * @param changeAmount
     * @return
     */
    @Transactional(transactionManager = "transactionManager", rollbackFor = {Exception.class})
    public boolean updateQuota(Long payPassageId, Long changeAmount) {
        try {
            PayPassage payPassage = queryPassageInfo(payPassageId);
            payPassage.setQuota(payPassage.getQuota() + changeAmount);
            boolean isSuccess = updateById(payPassage);
            if (!isSuccess) {
                log.error("更新授信不成功 [" + payPassage.getPayPassageId() + "] " + changeAmount);
            }
            return isSuccess;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public JSONObject sumPassageInfo() {
        return payPassageMapper.sumPassageInfo();
    }
}
