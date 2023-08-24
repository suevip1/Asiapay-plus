package com.jeequan.jeepay.mgr.ctrl.passagetest;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.util.JeepayKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/*
 * 支付测试 - 回调函数
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/6/22 14:22
 */
@RestController
@RequestMapping("/api/anon/passageTestNotify")
public class PassageTestNotifyController extends CommonCtrl {

    @Autowired
    private MchInfoService mchInfoService;

    @RequestMapping("/payOrder")
    public void payOrderNotify() throws IOException {

        //请求参数
        JSONObject params = getReqParamJSON();

        String mchNo = params.getString("mchNo");
        String sign = params.getString("sign");
        MchInfo mchInfo = mchInfoService.queryMchInfo(mchNo);
        if (mchInfo == null) {
            response.getWriter().print("商户不存在");
            return;
        }
        params.remove("sign");
        if (!JeepayKit.getSign(params, mchInfo.getSecret()).equalsIgnoreCase(sign)) {
            response.getWriter().print("签名验证失败");
            return;
        }

//        JSONObject msg = new JSONObject();
//        msg.put("state", params.getIntValue("state"));
//        msg.put("errCode", params.getString("errCode"));
//        msg.put("errMsg", params.getString("errMsg"));
//
//        //推送到前端
//        WsPayOrderServer.sendMsgByOrderId(params.getString("payOrderId"), msg.toJSONString());

        response.getWriter().print("SUCCESS");
    }

}