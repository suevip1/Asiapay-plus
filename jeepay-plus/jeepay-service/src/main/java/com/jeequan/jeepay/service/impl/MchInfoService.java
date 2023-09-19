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

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.service.mapper.MchInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * <p>
 * 商户信息表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-27
 */
@Slf4j
@Service
public class MchInfoService extends ServiceImpl<MchInfoMapper, MchInfo> {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private PayOrderService payOrderService;

    @Autowired
    private MchPayPassageService mchPayPassageService;

    @Autowired
    private SysUserAuthService sysUserAuthService;

    @Autowired
    private AgentAccountInfoService agentAccountInfoService;

    @Autowired
    private MchProductService mchProductService;
    @Resource
    private MchInfoMapper mchInfoMapper;

    /**
     * 查询商户信息
     *
     * @param mchNo
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MchInfo queryMchInfo(String mchNo) {
        //查询缓存中是否有
        MchInfo mchInfo = getById(mchNo);
        if (mchInfo == null) {
            throw new BizException("没有查询到商户");
        }
        return mchInfo;
    }

    /**
     * 更新商户信息
     *
     * @param mchInfo
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMchInfo(MchInfo mchInfo) {
        try {
            boolean isSuccess = update(mchInfo, MchInfo.gw().eq(MchInfo::getMchNo, mchInfo.getMchNo()));
            if (!isSuccess) {
                throw new BizException("修改失败,更新商户失败");
            }
        } catch (Exception e) {
            log.error("数据库异常,更新商户失败");
            log.error(e.getMessage(), e);
            throw new BizException("数据库异常,更新商户失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void addMch(MchInfo mchInfo, String loginUserName) {
        // 校验特邀商户信息
        if (StringUtils.isNotEmpty(mchInfo.getAgentNo())) {
            // 当前服务商状态是否正确
            AgentAccountInfo agentAccountInfo = agentAccountInfoService.queryAgentInfo(mchInfo.getAgentNo());
            if (agentAccountInfo == null || agentAccountInfo.getState() == CS.NO) {
                throw new BizException("当前代理商不可用");
            }
        }

        // 插入商户基本信息
        boolean saveResult = save(mchInfo);
        if (!saveResult) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }

        // 插入用户信息
        SysUser sysUser = new SysUser();
        sysUser.setLoginUsername(loginUserName);
        sysUser.setBelongInfoId(mchInfo.getMchNo());
        sysUser.setIsAdmin(CS.YES);
        sysUser.setState(mchInfo.getState());
        sysUserService.addSysUser(sysUser, CS.SYS_TYPE.MCH);


        // 存入商户默认用户ID
        MchInfo updateRecord = new MchInfo();
        updateRecord.setMchNo(mchInfo.getMchNo());
        updateRecord.setInitUserId(sysUser.getSysUserId());
        saveResult = updateById(updateRecord);
        if (!saveResult) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }
    }

    /**
     * 删除商户
     **/
    @Transactional(rollbackFor = Exception.class)
    public List<Long> removeByMchNo(String mchNo) {
        try {
            // 0.当前商户是否存在
            MchInfo mchInfo = getById(mchNo);
            if (mchInfo == null) {
                throw new BizException("该商户不存在");
            }

            // 1.查看当前商户是否存在交易数据
            int payCount = payOrderService.count(PayOrder.gw().eq(PayOrder::getMchNo, mchNo));
            if (payCount > 0) {
                throw new BizException("该商户已存在交易数据，不可删除");
            }

            // 2.删除当前商户配置的支付通道
            mchPayPassageService.remove(MchPayPassage.gw().eq(MchPayPassage::getMchNo, mchNo));

            // 3.删除当前商户对应的产品表
            mchProductService.remove(MchProduct.gw().eq(MchProduct::getMchNo, mchNo));

            List<SysUser> userList = sysUserService.list(SysUser.gw()
                    .eq(SysUser::getBelongInfoId, mchNo)
                    .eq(SysUser::getSysType, CS.SYS_TYPE.MCH)
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
                    .eq(SysUser::getBelongInfoId, mchNo)
                    .eq(SysUser::getSysType, CS.SYS_TYPE.MCH)
            );

            // 7.删除当前商户
            boolean removeMchInfo = removeById(mchNo);
            if (!removeMchInfo) {
                throw new BizException("删除当前商户失败");
            }
            return userIdList;
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        }
    }

    /**
     * 更新账户余额
     *
     * @param mchNo
     * @param changeAmount
     * @return
     */
    @Transactional(transactionManager = "transactionManager", rollbackFor = {Exception.class})
    public boolean updateBalance(String mchNo, Long changeAmount) {
        try {
            MchInfo mchInfo = queryMchInfo(mchNo);
            mchInfo.setBalance(mchInfo.getBalance() + changeAmount);
            boolean isSuccess = updateById(mchInfo);
            if (!isSuccess) {
                log.error("更新余额不成功 [" + mchInfo.getMchNo() + "] " + changeAmount);
            }
            return isSuccess;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException("数据更新异常");
        }
    }

    public JSONObject sumMchInfo() {
        return mchInfoMapper.sumMchInfo();
    }


    /**
     * 获取商户map
     *
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map<String, MchInfo> getMchInfoMap() {
        List<MchInfo> mchInfoList = list();
        Map<String, MchInfo> productMap = new HashMap<>();

        for (int i = 0; i < mchInfoList.size(); i++) {
            productMap.put(mchInfoList.get(i).getMchNo(), mchInfoList.get(i));
        }
        return productMap;
    }


}
