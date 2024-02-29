package com.jeequan.jeepay.mgr.ctrl.agent;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.components.mq.model.CleanAgentLoginAuthCacheMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.aop.LimitRequest;
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

import java.util.*;

/**
 * 代理商管理类
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021-06-07 07:15
 */
@RestController
@RequestMapping("/api/isvInfo")
public class AgentInfoController extends CommonCtrl {

    @Autowired
    private AgentAccountInfoService agentAccountInfoService;

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private PayPassageService payPassageService;

    @Autowired
    private SysUserAuthService sysUserAuthService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private IMQSender mqSender;

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:12
     * @describe: 查询代理商信息列表
     */
    @PreAuthorize("hasAuthority('ENT_ISV_LIST')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes list() {
        try {
            JSONObject reqJson = getReqParamJSON();

            AgentAccountInfo agentAccountInfo = getObject(AgentAccountInfo.class);
            QueryWrapper<AgentAccountInfo> wrapper = new QueryWrapper<>();
            wrapper.ne("state", CS.HIDE);
            if (StringUtils.isNotEmpty(agentAccountInfo.getAgentNo())) {
                wrapper.eq("agent_no", agentAccountInfo.getAgentNo());
            }
            if (StringUtils.isNotEmpty(agentAccountInfo.getAgentName())) {
                wrapper.like("agent_name", agentAccountInfo.getAgentName().trim());
            }
            if (agentAccountInfo.getState() != null) {
                wrapper.eq("state", agentAccountInfo.getState());
            }

            String sortField = reqJson.getString("sortField");
            String sortOrder = reqJson.getString("sortOrder");

            if (StringUtils.isNotEmpty(sortField) && sortField.equals("agentName") && StringUtils.isNotEmpty(sortOrder)) {
                wrapper.orderBy(true, !sortOrder.equals("descend"), "CONVERT(agent_name USING gbk) COLLATE gbk_chinese_ci");
            }


            if (StringUtils.isNotEmpty(sortField) && sortField.equals("balance") && StringUtils.isNotEmpty(sortOrder)) {
                wrapper.orderBy(true, !sortOrder.equals("descend"), "balance");
            }

            if (StringUtils.isNotEmpty(sortField) && sortField.equals("createdAt") && StringUtils.isNotEmpty(sortOrder)) {
                wrapper.orderBy(true, !sortOrder.equals("descend"), "created_at");
            }

            if (StringUtils.isEmpty(sortField) || StringUtils.isEmpty(sortOrder)) {
                wrapper.orderBy(true, true, "created_at");
            }

            IPage<AgentAccountInfo> pages = agentAccountInfoService.page(getIPage(true), wrapper);
            return ApiRes.page(pages);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ApiRes.fail(ApiCodeEnum.QUERY_ERROR);
        }
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:13
     * @describe: 新增代理商信息
     */
    @PreAuthorize("hasAuthority('ENT_ISV_INFO_ADD')")
    @MethodLog(remark = "新增代理商")
    @RequestMapping(value = "", method = RequestMethod.POST)
    @LimitRequest
    public ApiRes add() {
        AgentAccountInfo agentAccountInfo = getObject(AgentAccountInfo.class);
        String loginUserName = getValStringRequired("loginUserName");

        String agentNo = "A" + DateUtil.currentSeconds();
        agentAccountInfo.setAgentNo(agentNo);
        SysUser sysUser = getCurrentUser().getSysUser();
        agentAccountInfo.setCreatedUid(sysUser.getSysUserId());
        agentAccountInfoService.addAgent(agentAccountInfo, loginUserName);
        return ApiRes.ok();
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:13
     * @describe: 删除代理商信息
     */
    @PreAuthorize("hasAuthority('ENT_ISV_INFO_DEL')")
    @MethodLog(remark = "删除代理商")
    @RequestMapping(value = "/{agentNo}", method = RequestMethod.DELETE)
    @LimitRequest
    public ApiRes delete(@PathVariable("agentNo") String agentNo) {
        AgentAccountInfo agentAccountInfo = agentAccountInfoService.queryAgentInfo(agentNo);

        //余额是否清空
        if (!(agentAccountInfo.getBalance() == 0 && agentAccountInfo.getFreezeBalance() == 0)) {
            return ApiRes.fail(ApiCodeEnum.DELETE_BALANCE_NOT_ZERO);
        }
        //是否还存在下级商户
        if (mchInfoService.count(MchInfo.gw().eq(MchInfo::getAgentNo, agentNo)) > 0) {
            return ApiRes.customFail("当前代理商下还有未解绑的商户,请先解绑对应商户");
        }

        //是否还存在下级通道
        if (payPassageService.count(PayPassage.gw().eq(PayPassage::getAgentNo, agentNo)) > 0) {
            return ApiRes.customFail("当前代理商下还有未解绑的通道,请先解绑对应通道");
        }
        agentAccountInfo.setState(CS.HIDE);
        agentAccountInfoService.updateAgentInfo(agentAccountInfo);

        List<Long> userIdList = agentAccountInfoService.removeAgentDepends(agentNo);
        // 清除redis 登录用户缓存
        mqSender.send(CleanAgentLoginAuthCacheMQ.build(userIdList));
        return ApiRes.ok();
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:13
     * @describe: 更新代理商信息
     */
    @PreAuthorize("hasAuthority('ENT_ISV_INFO_EDIT')")
    @MethodLog(remark = "更新代理商信息")
    @RequestMapping(value = "/{agentNo}", method = RequestMethod.PUT)
    @LimitRequest
    public ApiRes update(@PathVariable("agentNo") String agentNo) {
        //获取查询条件
        AgentAccountInfo agentAccountInfo = getObject(AgentAccountInfo.class);
        agentAccountInfo.setAgentNo(agentNo);
        agentAccountInfo.setUpdatedAt(new Date());


        // 待删除用户登录信息的ID list
        Set<Long> removeCacheUserIdList = new HashSet<>();

        // 如果商户状态为禁用状态，清除该商户用户登录信息
        if (agentAccountInfo.getState() == CS.NO) {
            sysUserService.list(SysUser.gw().select(SysUser::getSysUserId).eq(SysUser::getBelongInfoId, agentNo).eq(SysUser::getSysType, CS.SYS_TYPE.AGENT))
                    .stream().forEach(u -> removeCacheUserIdList.add(u.getSysUserId()));
        }

        //判断是否重置密码
        if (getReqParamJSON().getBooleanValue("resetPass")) {
            // 待更新的密码
            String updatePwd = getReqParamJSON().getBoolean("defaultPass") ? CS.DEFAULT_PWD : Base64.decodeStr(getValStringRequired("confirmPwd"));
            // 获取商户超管
            Long mchAdminUserId = sysUserService.findAgentAdminUserId(agentNo);

            //重置密码
            sysUserAuthService.resetAuthInfoAndGoogle(mchAdminUserId, updatePwd, CS.SYS_TYPE.AGENT);

            //删除超管登录信息
            removeCacheUserIdList.add(mchAdminUserId);
        }

        // 推送mq删除redis用户认证信息
        if (!removeCacheUserIdList.isEmpty()) {
            mqSender.send(CleanAgentLoginAuthCacheMQ.build(new ArrayList<>(removeCacheUserIdList)));
        }

        //更新商户信息
        agentAccountInfoService.updateAgentInfo(agentAccountInfo);

        return ApiRes.ok();
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:13
     * @describe: 查看代理商信息
     */
    @PreAuthorize("hasAnyAuthority('ENT_ISV_INFO_VIEW', 'ENT_ISV_INFO_EDIT')")
    @RequestMapping(value = "/{agentNo}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("agentNo") String agentNo) {
        AgentAccountInfo agentAccountInfo = agentAccountInfoService.queryAgentInfo(agentNo);
        if (agentAccountInfo == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        SysUser sysUser = sysUserService.getById(agentAccountInfo.getInitUserId());
        if (sysUser != null) {
            agentAccountInfo.addExt("loginUserName", sysUser.getLoginUsername());
        }
        return ApiRes.ok(agentAccountInfo);
    }


}