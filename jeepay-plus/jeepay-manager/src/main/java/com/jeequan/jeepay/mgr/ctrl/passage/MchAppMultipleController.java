package com.jeequan.jeepay.mgr.ctrl.passage;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.aop.LimitRequest;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.GoogleAuthUtil;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/mchAppsMultipleSet")
public class MchAppMultipleController extends CommonCtrl {
    @Autowired
    private PayPassageService payPassageService;

    @Autowired
    private SysUserAuthService sysUserAuthService;

    @Autowired
    private PassageTransactionHistoryService passageTransactionHistoryService;

    @Autowired
    private RobotsPassageService robotsPassageService;

    @Autowired
    private MchPayPassageService mchPayPassageService;

    private static final String RECENTLY_OPEN = "recently_open";


    @PreAuthorize("hasAuthority('ENT_MCH_APP_EDIT')")
    @MethodLog(remark = "关闭全部通道")
    @RequestMapping(value = "/closeAll", method = RequestMethod.POST)
    public ApiRes closeAll() {
        //校验谷歌
        String googleCodeStr = getValString("googleCode");

        //检查当前用户是否绑定谷歌
        String account = getCurrentUser().getSysUser().getLoginUsername();
        SysUserAuth sysUserAuth = sysUserAuthService.selectByLogin(account, CS.AUTH_TYPE.LOGIN_USER_NAME, CS.SYS_TYPE.MGR);
        if (sysUserAuth == null || sysUserAuth.getGoogleAuthStatus() == CS.NO) {
            return ApiRes.customFail("请先绑定谷歌验证码");
        }

        boolean isVerify = false;
        try {
            int googleCode = Integer.parseInt(googleCodeStr);
            isVerify = GoogleAuthUtil.CheckCode(sysUserAuth.getGoogleAuthKey(), googleCode);
        } catch (Exception e) {
            throw new BizException("谷歌验证码有误！");
        }
        if (!isVerify) {
            throw new BizException("谷歌验证码有误！");
        }
        try {
            List<PayPassage> passageList = payPassageService.list(PayPassage.gw().select(PayPassage::getPayPassageId, PayPassage::getState, PayPassage::getTimeLimit));
            RedisUtil.set(RECENTLY_OPEN, JSONObject.toJSONString(passageList), 3, TimeUnit.HOURS);
            List<PayPassage> updatePassageList = new ArrayList<>();
            for (int i = 0; i < passageList.size(); i++) {
                PayPassage passage = passageList.get(i);
                if (passage.getState() == CS.YES) {
                    passage.setState(CS.NO);
                }
                passage.setTimeLimit(CS.NO);
                updatePassageList.add(passage);
            }
            //提高这个方法的事务级别
            payPassageService.saveOrUpdateBatch(updatePassageList);
            return ApiRes.ok();
        } catch (Exception e) {
            RedisUtil.set(RECENTLY_OPEN, "", 7200);
            log.error(e.getMessage(), e);
            return ApiRes.customFail("关闭失败,请联系管理员");
        }
    }

    @PreAuthorize("hasAuthority('ENT_MCH_APP_EDIT')")
    @MethodLog(remark = "启用最近关闭通道")
    @RequestMapping(value = "/openRecently", method = RequestMethod.POST)
    public ApiRes openRecently() {
        try {
            JSONArray jsonArray = JSONArray.parseArray(RedisUtil.getObject(RECENTLY_OPEN, String.class));
            if (jsonArray == null || jsonArray.size() == 0) {
                return ApiRes.customFail("没有最近关闭全部通道的记录或距离上次操作已超过三小时!");
            }
            List<PayPassage> updatePassageList = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                PayPassage passage = JSONObject.parseObject(jsonArray.get(i).toString(), PayPassage.class);
                updatePassageList.add(passage);
            }
            payPassageService.saveOrUpdateBatch(updatePassageList);
            RedisUtil.del(RECENTLY_OPEN);
            return ApiRes.ok();
        } catch (Exception e) {
            RedisUtil.del(RECENTLY_OPEN);
            log.error(e.getMessage(), e);
            return ApiRes.customFail("关闭失败,请联系管理员");
        }
    }

    @PreAuthorize("hasAuthority('ENT_MCH_APP_EDIT')")
    @MethodLog(remark = "批量开关通道")
    @RequestMapping(value = "/multipleSetState", method = RequestMethod.POST)
    @LimitRequest
    public ApiRes multipleSetState() {
        try {
            JSONObject reqJson = getReqParamJSON();

            Byte state = reqJson.getByte("state");
            JSONArray selectedIds = reqJson.getJSONArray("selectedIds");

            if (selectedIds == null || selectedIds.isEmpty()) {
                return ApiRes.customFail("请先选中需要批量操作的通道");
            }

            //检查当前用户是否绑定谷歌
            String account = getCurrentUser().getSysUser().getLoginUsername();
            SysUserAuth sysUserAuth = sysUserAuthService.selectByLogin(account, CS.AUTH_TYPE.LOGIN_USER_NAME, CS.SYS_TYPE.MGR);
            if (sysUserAuth == null || sysUserAuth.getGoogleAuthStatus() == CS.NO) {
                return ApiRes.customFail("敏感操作，请先绑定谷歌验证码");
            }

            List<PayPassage> updatePassageList = new ArrayList<>();
            Map<Long, PayPassage> payPassageMap = payPassageService.getPayPassageMap();
            for (int i = 0; i < selectedIds.size(); i++) {
                PayPassage passage = new PayPassage();
                Long passageId = selectedIds.getLongValue(i);
                passage.setPayPassageId(passageId);
                passage.setState(state);
                //打开
                if (state == CS.YES) {
                    if (StringUtils.isNotEmpty(payPassageMap.get(passageId).getTimeRules())) {
                        passage.setTimeLimit(CS.YES);
                    }
                } else {
                    //关闭
                    passage.setTimeLimit(CS.NO);
                }
                updatePassageList.add(passage);
            }
            boolean isSuccess = payPassageService.saveOrUpdateBatch(updatePassageList);
            if (!isSuccess) {
                return ApiRes.customFail("批量操作失败，请稍后再试");
            }
            return ApiRes.ok();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ApiRes.customFail("操作失败,请联系管理员");
        }
    }

    @PreAuthorize("hasAuthority('ENT_MCH_APP_EDIT')")
    @MethodLog(remark = "批量设置通道产品")
    @RequestMapping(value = "/multipleSetProduct", method = RequestMethod.POST)
    @LimitRequest
    public ApiRes multipleSetProduct() {
        try {
            JSONObject reqJson = getReqParamJSON();

            Long productId = reqJson.getLong("productId");
            JSONArray selectedIds = reqJson.getJSONArray("selectedIds");

            if (selectedIds == null || selectedIds.isEmpty()) {
                return ApiRes.customFail("请先选中需要批量操作的通道");
            }

            //检查当前用户是否绑定谷歌
            String account = getCurrentUser().getSysUser().getLoginUsername();
            SysUserAuth sysUserAuth = sysUserAuthService.selectByLogin(account, CS.AUTH_TYPE.LOGIN_USER_NAME, CS.SYS_TYPE.MGR);
            if (sysUserAuth == null || sysUserAuth.getGoogleAuthStatus() == CS.NO) {
                return ApiRes.customFail("敏感操作，请先绑定谷歌验证码");
            }

            List<PayPassage> updatePassageList = new ArrayList<>();
            for (int i = 0; i < selectedIds.size(); i++) {
                PayPassage passage = new PayPassage();
                Long passageId = selectedIds.getLongValue(i);
                passage.setPayPassageId(passageId);
                passage.setProductId(productId);
                updatePassageList.add(passage);
            }
            boolean isSuccess = payPassageService.saveOrUpdateBatch(updatePassageList);
            if (!isSuccess) {
                return ApiRes.customFail("批量操作失败，请稍后再试");
            }
            return ApiRes.ok();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ApiRes.customFail("操作失败,请联系管理员");
        }
    }

    @PreAuthorize("hasAuthority('ENT_MCH_APP_EDIT')")
    @MethodLog(remark = "批量清空通道余额")
    @RequestMapping(value = "/multipleSetBalanceZero", method = RequestMethod.POST)
    @LimitRequest
    public ApiRes multipleSetBalanceZero() {
        try {
            JSONObject reqJson = getReqParamJSON();
            JSONArray selectedIds = reqJson.getJSONArray("selectedIds");

            if (selectedIds == null || selectedIds.isEmpty()) {
                return ApiRes.customFail("请先选中需要批量操作的通道");
            }

            //检查当前用户是否绑定谷歌
            String account = getCurrentUser().getSysUser().getLoginUsername();
            SysUserAuth sysUserAuth = sysUserAuthService.selectByLogin(account, CS.AUTH_TYPE.LOGIN_USER_NAME, CS.SYS_TYPE.MGR);
            if (sysUserAuth == null || sysUserAuth.getGoogleAuthStatus() == CS.NO) {
                return ApiRes.customFail("敏感操作，请先绑定谷歌验证码");
            }

            Map<Long, PayPassage> payPassageMap = payPassageService.getPayPassageMap();

            for (int i = 0; i < selectedIds.size(); i++) {
                Long passageId = selectedIds.getLongValue(i);
                PayPassage passage = payPassageMap.get(passageId);

                Long amount = -passage.getBalance();
                Long beforeBalance = passage.getBalance();
                Long afterBalance = passage.getBalance() + amount;
                if (beforeBalance != 0) {

                    //插入通道流水记录
                    PassageTransactionHistory passageTransactionHistory = new PassageTransactionHistory();
                    passageTransactionHistory.setAmount(amount);
                    passageTransactionHistory.setPayPassageId(passage.getPayPassageId());
                    passageTransactionHistory.setPayPassageName(passage.getPayPassageName());

                    passageTransactionHistory.setBeforeBalance(beforeBalance);
                    passageTransactionHistory.setAfterBalance(afterBalance);
                    passageTransactionHistory.setFundDirection(amount >= 0 ? CS.FUND_DIRECTION_INCREASE : CS.FUND_DIRECTION_REDUCE);
                    passageTransactionHistory.setBizType(PassageTransactionHistory.BIZ_TYPE_CHANGE);
                    SysUser sysUser = getCurrentUser().getSysUser();
                    passageTransactionHistory.setCreatedUid(sysUser.getSysUserId());
                    passageTransactionHistory.setCreatedLoginName(sysUser.getLoginUsername());
                    passageTransactionHistory.setRemark("批量清空");
                    passageTransactionHistoryService.save(passageTransactionHistory);

                    payPassageService.updateBalance(passage.getPayPassageId(), amount);
                }
            }
            return ApiRes.ok();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ApiRes.customFail("操作失败,请联系管理员");
        }
    }

    @PreAuthorize("hasAuthority('ENT_MCH_APP_EDIT')")
    @MethodLog(remark = "批量设置通道费率")
    @RequestMapping(value = "/multipleSetRate", method = RequestMethod.POST)
    @LimitRequest
    public ApiRes multipleSetRate() {
        try {
            JSONObject reqJson = getReqParamJSON();
            PayPassage payPassage = getObject(PayPassage.class);

            JSONArray selectedIds = reqJson.getJSONArray("selectedIds");

            if (selectedIds == null || selectedIds.isEmpty()) {
                return ApiRes.customFail("请先选中需要批量操作的通道");
            }

            if (payPassage.getRate() == null) {
                return ApiRes.customFail("请先设置费率");
            }

            //检查当前用户是否绑定谷歌
            String account = getCurrentUser().getSysUser().getLoginUsername();
            SysUserAuth sysUserAuth = sysUserAuthService.selectByLogin(account, CS.AUTH_TYPE.LOGIN_USER_NAME, CS.SYS_TYPE.MGR);
            if (sysUserAuth == null || sysUserAuth.getGoogleAuthStatus() == CS.NO) {
                return ApiRes.customFail("敏感操作，请先绑定谷歌验证码");
            }

            List<PayPassage> updatePassageList = new ArrayList<>();
            for (int i = 0; i < selectedIds.size(); i++) {
                PayPassage passage = new PayPassage();
                Long passageId = selectedIds.getLongValue(i);

                passage.setPayPassageId(passageId);
                passage.setRate(payPassage.getRate());
                updatePassageList.add(passage);
            }
            boolean isSuccess = payPassageService.saveOrUpdateBatch(updatePassageList);
            if (!isSuccess) {
                return ApiRes.customFail("批量操作失败，请稍后再试");
            }
            return ApiRes.ok();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ApiRes.customFail("操作失败,请联系管理员");
        }
    }

    @PreAuthorize("hasAuthority('ENT_MCH_APP_EDIT')")
    @MethodLog(remark = "批量设置下单网关")
    @RequestMapping(value = "/multipleSetGate", method = RequestMethod.POST)
    @LimitRequest
    public ApiRes multipleSetGate() {
        try {
            JSONObject reqJson = getReqParamJSON();

            String payGate = reqJson.getString("payGate");
            JSONArray selectedIds = reqJson.getJSONArray("selectedIds");

            if (selectedIds == null || selectedIds.isEmpty()) {
                return ApiRes.customFail("请先选中需要批量操作的通道");
            }

            //检查当前用户是否绑定谷歌
            String account = getCurrentUser().getSysUser().getLoginUsername();
            SysUserAuth sysUserAuth = sysUserAuthService.selectByLogin(account, CS.AUTH_TYPE.LOGIN_USER_NAME, CS.SYS_TYPE.MGR);
            if (sysUserAuth == null || sysUserAuth.getGoogleAuthStatus() == CS.NO) {
                return ApiRes.customFail("敏感操作，请先绑定谷歌验证码");
            }

            Map<Long, PayPassage> payPassageMap = payPassageService.getPayPassageMap();
            List<PayPassage> updatePassageList = new ArrayList<>();

            for (int i = 0; i < selectedIds.size(); i++) {
                Long passageId = selectedIds.getLongValue(i);
                PayPassage passage = payPassageMap.get(passageId);
                if (StringUtils.isEmpty(passage.getPayInterfaceConfig())) {
                    return ApiRes.customFail("通道[" + passageId + "] " + passage.getPayPassageName() + " 未配置[支付参数]，请先配置后再批量修改");
                }
                NormalMchParams normalMchParams = JSONObject.parseObject(passage.getPayInterfaceConfig(), NormalMchParams.class);
                normalMchParams.setPayGateway(payGate);

                passage.setPayPassageId(passageId);
                passage.setPayInterfaceConfig(JSONObject.toJSONString(normalMchParams));
                updatePassageList.add(passage);
            }
            boolean isSuccess = payPassageService.saveOrUpdateBatch(updatePassageList);
            if (!isSuccess) {
                return ApiRes.customFail("批量操作失败，请稍后再试");
            }
            return ApiRes.ok();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ApiRes.customFail("操作失败,请联系管理员");
        }
    }

    @PreAuthorize("hasAuthority('ENT_MCH_APP_EDIT')")
    @MethodLog(remark = "批量设置回调IP")
    @RequestMapping(value = "/multipleSetIP", method = RequestMethod.POST)
    @LimitRequest
    public ApiRes multipleSetIP() {
        try {
            JSONObject reqJson = getReqParamJSON();

            String ip = reqJson.getString("ip");
            JSONArray selectedIds = reqJson.getJSONArray("selectedIds");

            if (selectedIds == null || selectedIds.isEmpty()) {
                return ApiRes.customFail("请先选中需要批量操作的通道");
            }

            //检查当前用户是否绑定谷歌
            String account = getCurrentUser().getSysUser().getLoginUsername();
            SysUserAuth sysUserAuth = sysUserAuthService.selectByLogin(account, CS.AUTH_TYPE.LOGIN_USER_NAME, CS.SYS_TYPE.MGR);
            if (sysUserAuth == null || sysUserAuth.getGoogleAuthStatus() == CS.NO) {
                return ApiRes.customFail("敏感操作，请先绑定谷歌验证码");
            }

            Map<Long, PayPassage> payPassageMap = payPassageService.getPayPassageMap();
            List<PayPassage> updatePassageList = new ArrayList<>();

            for (int i = 0; i < selectedIds.size(); i++) {
                Long passageId = selectedIds.getLongValue(i);
                PayPassage passage = payPassageMap.get(passageId);
                if (StringUtils.isEmpty(passage.getPayInterfaceConfig())) {
                    return ApiRes.customFail("通道[" + passageId + "] " + passage.getPayPassageName() + " 未配置[支付参数]，请先配置后再批量修改");
                }
                NormalMchParams normalMchParams = JSONObject.parseObject(passage.getPayInterfaceConfig(), NormalMchParams.class);
                normalMchParams.setWhiteList(ip);

                passage.setPayPassageId(passageId);
                passage.setPayInterfaceConfig(JSONObject.toJSONString(normalMchParams));
                updatePassageList.add(passage);
            }
            boolean isSuccess = payPassageService.saveOrUpdateBatch(updatePassageList);
            if (!isSuccess) {
                return ApiRes.customFail("批量操作失败，请稍后再试");
            }
            return ApiRes.ok();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ApiRes.customFail("操作失败,请联系管理员");
        }
    }

    @PreAuthorize("hasAuthority('ENT_MCH_APP_EDIT')")
    @MethodLog(remark = "批量删除通道")
    @RequestMapping(value = "/multipleSetDelete", method = RequestMethod.POST)
    @LimitRequest
    public ApiRes multipleSetDelete() {
        try {
            JSONObject reqJson = getReqParamJSON();
            JSONArray selectedIds = reqJson.getJSONArray("selectedIds");

            if (selectedIds == null || selectedIds.isEmpty()) {
                return ApiRes.customFail("请先选中需要批量操作的通道");
            }

            //检查当前用户是否绑定谷歌
            String account = getCurrentUser().getSysUser().getLoginUsername();
            SysUserAuth sysUserAuth = sysUserAuthService.selectByLogin(account, CS.AUTH_TYPE.LOGIN_USER_NAME, CS.SYS_TYPE.MGR);
            if (sysUserAuth == null || sysUserAuth.getGoogleAuthStatus() == CS.NO) {
                return ApiRes.customFail("敏感操作，请先绑定谷歌验证码");
            }


            List<Long> ids = selectedIds.toJavaList(Long.class);
            //余额为零且在所选ID里面的
            List<PayPassage> passageList = payPassageService.list(PayPassage.gw().in(PayPassage::getPayPassageId, ids).eq(PayPassage::getBalance, 0L));
            if (passageList.size() != ids.size()) {
                return ApiRes.customFail("所选通道中存在余额不为零的通道，请检查");
            }

            for (int i = 0; i < passageList.size(); i++) {
                PayPassage payPassage = new PayPassage();
                Long payPassageId = passageList.get(i).getPayPassageId();

                payPassage.setPayPassageId(payPassageId);
                payPassage.setState(CS.HIDE);

                boolean isSuccess = payPassageService.updateById(payPassage);
                if (isSuccess) {
                    //移除机器人绑定的通道
                    robotsPassageService.remove(RobotsPassage.gw().eq(RobotsPassage::getPassageId, payPassageId));
                    //移除绑定
                    mchPayPassageService.remove(MchPayPassage.gw().eq(MchPayPassage::getPayPassageId, payPassageId));
                } else {
                    logger.error("[批量操作]隐藏通道失败: " + payPassageId + " " + passageList.get(i).getPayPassageName());
                }
            }
            return ApiRes.ok();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ApiRes.customFail("操作失败,请联系管理员");
        }
    }

}
