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
package com.jeequan.jeepay.mgr.ctrl.merchant;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.components.mq.model.CleanMchLoginAuthCacheMQ;
import com.jeequan.jeepay.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

/**
 * 商户管理类
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021-06-07 07:15
 */
@RestController
@RequestMapping("/api/mchInfo")
public class MchInfoController extends CommonCtrl {

    @Autowired
    private MchInfoService mchInfoService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysUserAuthService sysUserAuthService;
    @Autowired
    private AgentAccountInfoService agentAccountInfoService;

    @Autowired
    private PayOrderService payOrderService;

    @Autowired
    private IMQSender mqSender;

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 商户信息列表
     */
    @PreAuthorize("hasAuthority('ENT_MCH_LIST')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        try {
            MchInfo mchInfo = getObject(MchInfo.class);

            LambdaQueryWrapper<MchInfo> wrapper = MchInfo.gw();
            if (StringUtils.isNotEmpty(mchInfo.getMchNo())) {
                wrapper.like(MchInfo::getMchNo, mchInfo.getMchNo());
            }
            if (StringUtils.isNotEmpty(mchInfo.getAgentNo())) {
                wrapper.like(MchInfo::getAgentNo, mchInfo.getAgentNo());
            }
            if (StringUtils.isNotEmpty(mchInfo.getMchName())) {
                wrapper.like(MchInfo::getMchName, mchInfo.getMchName());
            }

            if (mchInfo.getState() != null) {
                wrapper.eq(MchInfo::getState, mchInfo.getState());
            }
            wrapper.orderByAsc(MchInfo::getCreatedAt);

            List<AgentAccountInfo> agentAccountInfos = agentAccountInfoService.list();


            IPage<MchInfo> pages = mchInfoService.page(getIPage(), wrapper);

            List<MchInfo> mchInfoList = pages.getRecords();
            for (int i = 0; i < mchInfoList.size(); i++) {
                for (int j = 0; j < agentAccountInfos.size(); j++) {
                    if (mchInfoList.get(i).getAgentNo().equals(agentAccountInfos.get(j).getAgentNo())) {
                        mchInfoList.get(i).addExt("agentName", agentAccountInfos.get(j).getAgentName());
                    }
                }
            }
            pages.setRecords(mchInfoList);
            return ApiRes.page(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 新增商户信息
     */
    @PreAuthorize("hasAuthority('ENT_MCH_INFO_ADD')")
    @MethodLog(remark = "新增商户")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ApiRes add() {
        MchInfo mchInfo = getObject(MchInfo.class);
        // 获取传入的商户登录名
        String loginUserName = getValStringRequired("loginUserName");
        String agentNo = getValString("agentNo");
        mchInfo.setMchNo("M" + DateUtil.currentSeconds());
        // 当前登录用户信息
        SysUser sysUser = getCurrentUser().getSysUser();
        mchInfo.setCreatedUid(sysUser.getSysUserId());

        mchInfoService.addMch(mchInfo, loginUserName);
        return ApiRes.ok();
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 删除商户信息
     */
    @PreAuthorize("hasAuthority('ENT_MCH_INFO_DEL')")
    @MethodLog(remark = "删除商户")
    @RequestMapping(value = "/{mchNo}", method = RequestMethod.DELETE)
    public ApiRes delete(@PathVariable("mchNo") String mchNo) {
        if (payOrderService.count(PayOrder.gw().eq(PayOrder::getMchNo, mchNo)) > 0) {
            throw new BizException("该商户已发生交易，无法删除！");
        }

        List<Long> userIdList = mchInfoService.removeByMchNo(mchNo);
        // 推送mq删除redis用户缓存
        mqSender.send(CleanMchLoginAuthCacheMQ.build(userIdList));
        return ApiRes.ok();
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 更新商户信息
     */
    @PreAuthorize("hasAuthority('ENT_MCH_INFO_EDIT')")
    @MethodLog(remark = "更新商户信息")
    @RequestMapping(value = "/{mchNo}", method = RequestMethod.PUT)
    public ApiRes update(@PathVariable("mchNo") String mchNo) {
        //获取查询条件
        MchInfo mchInfo = getObject(MchInfo.class);
        mchInfo.setMchNo(mchNo); //设置商户号主键

        // 待删除用户登录信息的ID list
        Set<Long> removeCacheUserIdList = new HashSet<>();

        // 如果商户状态为禁用状态，清除该商户用户登录信息
        if (mchInfo.getState() == CS.NO) {
            sysUserService.list(SysUser.gw().select(SysUser::getSysUserId).eq(SysUser::getBelongInfoId, mchNo).eq(SysUser::getSysType, CS.SYS_TYPE.MCH))
                    .stream().forEach(u -> removeCacheUserIdList.add(u.getSysUserId()));
        }

        //判断是否重置密码
        if (getReqParamJSON().getBooleanValue("resetPass")) {
            // 待更新的密码
            String updatePwd = getReqParamJSON().getBoolean("defaultPass") ? CS.DEFAULT_PWD : Base64.decodeStr(getValStringRequired("confirmPwd"));
            // 获取商户超管
            Long mchAdminUserId = sysUserService.findMchAdminUserId(mchNo);

            //重置超管密码
            sysUserAuthService.resetAuthInfoAndGoogle(mchAdminUserId, updatePwd, CS.SYS_TYPE.MCH);

            //删除超管登录信息
            removeCacheUserIdList.add(mchAdminUserId);
        }

        // 推送mq删除redis用户认证信息
        if (!removeCacheUserIdList.isEmpty()) {
            mqSender.send(CleanMchLoginAuthCacheMQ.build(new ArrayList<>(removeCacheUserIdList)));
        }

//        //更新商户信息
//        if (!mchInfoService.updateById(mchInfo)) {
//            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
//        }
        mchInfoService.updateMchInfo(mchInfo);
        return ApiRes.ok();
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 查询商户信息
     */
    @PreAuthorize("hasAnyAuthority('ENT_MCH_INFO_VIEW', 'ENT_MCH_INFO_EDIT')")
    @RequestMapping(value = "/{mchNo}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("mchNo") String mchNo) {
        MchInfo mchInfo = mchInfoService.queryMchInfo(mchNo);
        if (mchInfo == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        SysUser sysUser = sysUserService.getById(mchInfo.getInitUserId());
        if (sysUser != null) {
            mchInfo.addExt("loginUserName", sysUser.getLoginUsername());
        }
        return ApiRes.ok(mchInfo);
    }

}
