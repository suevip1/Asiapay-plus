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
package com.jeequan.jeepay.mch.ctrl.paytest;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.mch.websocket.server.WsPayOrderServer;
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
@RequestMapping("/api/anon/paytestNotify")
public class PaytestNotifyController extends CommonCtrl {
    // todo PaytestNotifyController
    @Autowired private MchInfoService mchInfoService;

    @RequestMapping("/payOrder")
    public void payOrderNotify() throws IOException {

        //请求参数
        JSONObject params = getReqParamJSON();

        String mchNo = params.getString("mchNo");
        String sign = params.getString("sign");
        MchInfo mchInfo = mchInfoService.queryMchInfo(mchNo);
        if(mchInfo == null){
            response.getWriter().print("商户不存在");
            return;
        }
        params.remove("sign");
        if(!JeepayKit.getSign(params, mchInfo.getSecret()).equalsIgnoreCase(sign)){
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
