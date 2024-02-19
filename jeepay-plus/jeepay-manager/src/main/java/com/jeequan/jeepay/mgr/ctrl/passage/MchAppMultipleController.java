package com.jeequan.jeepay.mgr.ctrl.passage;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PassageTransactionHistory;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.entity.SysUserAuth;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.GoogleAuthUtil;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.PassageTransactionHistoryService;
import com.jeequan.jeepay.service.impl.PayPassageService;
import com.jeequan.jeepay.service.impl.SysUserAuthService;
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
    @MethodLog(remark = "批量操作通道")
    @RequestMapping(value = "/multiple", method = RequestMethod.POST)
    public ApiRes multiple() {
        try {
            JSONObject reqJson = getReqParamJSON();

            Byte state = reqJson.getByte("state");
            JSONArray selectedIds = reqJson.getJSONArray("selectedIds");

            if (selectedIds == null || selectedIds.isEmpty()) {
                return ApiRes.customFail("请先选中需要批量操作的通道");
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
            payPassageService.saveOrUpdateBatch(updatePassageList);
            return ApiRes.ok();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ApiRes.customFail("操作失败,请联系管理员");
        }
    }

}
