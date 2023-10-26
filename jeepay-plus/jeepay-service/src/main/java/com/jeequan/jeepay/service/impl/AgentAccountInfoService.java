package com.jeequan.jeepay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.service.mapper.AgentAccountInfoMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 代理商账户表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-06-22
 */
@Service
public class AgentAccountInfoService extends ServiceImpl<AgentAccountInfoMapper, AgentAccountInfo> {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private PayPassageService payPassageService;

    @Autowired
    private MchProductService mchProductService;

    @Resource
    private AgentAccountInfoMapper agentAccountInfoMapper;

    @Autowired
    private SysUserAuthService sysUserAuthService;

    /**
     * 通过代理商号获取
     *
     * @param agentNo
     * @return
     */
    @Transactional(transactionManager = "transactionManager", rollbackFor = {Exception.class}, isolation = Isolation.SERIALIZABLE)
    public AgentAccountInfo queryAgentInfo(String agentNo) {
        AgentAccountInfo agentAccountInfo = getById(agentNo);
        if (agentAccountInfo == null) {
            throw new BizException("没有查询到代理商户");
        }
        return agentAccountInfo;
    }

    /**
     * 更新代理信息
     *
     * @param agentAccountInfo
     */
    @Transactional(transactionManager = "transactionManager", rollbackFor = {Exception.class})
    public void updateAgentInfo(AgentAccountInfo agentAccountInfo) {
        try {
            //更新不处理余额
            agentAccountInfo.setBalance(null);

            boolean isSuccess = update(agentAccountInfo, AgentAccountInfo.gw().eq(AgentAccountInfo::getAgentNo, agentAccountInfo.getAgentNo()));
            if (!isSuccess) {
                throw new BizException("修改失败,更新代理失败");
            }
        } catch (Exception e) {
            log.error("数据库异常,代理商户失败");
            log.error(e.getMessage(), e);
            throw new BizException("修改失败,更新代理失败");
        }
    }

    /**
     * 添加代理商（自动关联相关登录信息）
     *
     * @param agentAccountInfo
     * @param loginUserName
     */
    @Transactional(transactionManager = "transactionManager", rollbackFor = {Exception.class})
    public void addAgent(AgentAccountInfo agentAccountInfo, String loginUserName) {

        // 插入商户基本信息
        boolean saveResult = save(agentAccountInfo);
        if (!saveResult) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }

        // 插入用户信息
        SysUser sysUser = new SysUser();
        sysUser.setLoginUsername(loginUserName);
        sysUser.setBelongInfoId(agentAccountInfo.getAgentNo());
        sysUser.setIsAdmin(CS.YES);
        sysUser.setState(agentAccountInfo.getState());
        sysUserService.addSysUser(sysUser, CS.SYS_TYPE.AGENT);


        // 存入商户默认用户ID
        AgentAccountInfo updateRecord = new AgentAccountInfo();
        updateRecord.setAgentNo(agentAccountInfo.getAgentNo());
        updateRecord.setInitUserId(sysUser.getSysUserId());
        saveResult = updateById(updateRecord);
        if (!saveResult) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }

    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = {Exception.class})
    public List<Long> removeByAgentNo(String agentNo) {
        try {
            // 0.当前代理是否存在
            AgentAccountInfo agentAccountInfo = getById(agentNo);
            if (agentAccountInfo == null) {
                throw new BizException("该代理不存在");
            }

            // 1.当前代理是否存在下级通道、下级商户  改为0 或提示
            List<MchInfo> mchInfoList = mchInfoService.list(MchInfo.gw().eq(MchInfo::getAgentNo, agentNo));

            for (int i = 0; i < mchInfoList.size(); i++) {
                MchInfo mchInfo = mchInfoList.get(i);
                List<MchProduct> mchProductList = mchProductService.list(MchProduct.gw().eq(MchProduct::getMchNo, mchInfo.getMchNo()).ne(MchProduct::getAgentRate, BigDecimal.ZERO));
                for (int index = 0; index < mchProductList.size(); index++) {
                    MchProduct mchProduct = mchProductList.get(index);
                    mchProduct.setAgentRate(BigDecimal.ZERO);
                    mchProductService.saveOrUpdate(mchProduct);
                }
                mchInfo.setAgentNo("");
                mchInfoService.updateMchInfo(mchInfo);
            }
            //通道代理清理
            List<PayPassage> passageList = payPassageService.list(PayPassage.gw().eq(PayPassage::getAgentNo, agentNo));
            for (int index = 0; index < passageList.size(); index++) {
                PayPassage payPassage = passageList.get(index);
                payPassage.setAgentRate(BigDecimal.ZERO);
                payPassage.setAgentNo("");
                payPassageService.updatePassageInfo(payPassage);
            }

            List<SysUser> userList = sysUserService.list(SysUser.gw()
                    .eq(SysUser::getBelongInfoId, agentNo)
                    .eq(SysUser::getSysType, CS.SYS_TYPE.AGENT)
            );

            // 返回的用户id
            List<Long> userIdList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(userList)) {
                for (SysUser user : userList) {
                    userIdList.add(user.getSysUserId());
                }
                // 5.删除当前商户用户子用户信息
                sysUserAuthService.remove(SysUserAuth.gw().in(SysUserAuth::getUserId, userIdList));
            }

            // 6.删除当前商户的登录用户
            sysUserService.remove(SysUser.gw()
                    .eq(SysUser::getBelongInfoId, agentNo)
                    .eq(SysUser::getSysType, CS.SYS_TYPE.AGENT)
            );

            // 7.删除当前商户
            boolean removeAgentInfo = removeById(agentNo);
            if (!removeAgentInfo) {
                throw new BizException("删除当前代理失败");
            }
            return userIdList;
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        }
    }


    public Map<String, AgentAccountInfo> getAgentInfoMap() {
        List<AgentAccountInfo> agentAccountInfos = list();
        Map<String, AgentAccountInfo> agentAccountInfoMap = new HashMap<>();

        for (int i = 0; i < agentAccountInfos.size(); i++) {
            agentAccountInfoMap.put(agentAccountInfos.get(i).getAgentNo(), agentAccountInfos.get(i));
        }
        return agentAccountInfoMap;
    }


    /**
     * 更新账户余额
     *
     * @param agentNo
     * @param changeAmount
     * @return
     */
    @Transactional(transactionManager = "transactionManager", rollbackFor = {Exception.class})
    public void updateBalance(String agentNo, Long changeAmount) {
        try {
            Map params = new HashMap();
            params.put("agentNo", agentNo);
            params.put("changeAmount", changeAmount);
            int isSuccess = agentAccountInfoMapper.updateBalance(params);
            if (isSuccess == 0) {
                log.error("更新代理余额不成功 [" + agentNo + "] " + changeAmount);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException("数据更新异常");
        }
    }

    public JSONObject sumAgentInfo() {
        return agentAccountInfoMapper.sumAgentInfo();
    }
}
