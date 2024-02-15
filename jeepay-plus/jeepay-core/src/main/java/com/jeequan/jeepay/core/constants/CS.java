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
package com.jeequan.jeepay.core.constants;

import com.jeequan.jeepay.core.entity.PayOrder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Author terrfly
 * @Date 2019/11/16 15:09
 * @Description Constants 常量对象
 **/
public class CS {

    //登录图形验证码缓存时间，单位：s
    public static final int VERCODE_CACHE_TIME = 60;

    /**
     * 订单过期时间
     */
    public static final int ORDER_EXPIRED_TIME = 240;


    /**
     * Redis key 检查是否能下单-租户余额是否充足
     */
    public static final String CHECK_AVAILABLE = "CHECK_AVAILABLE_KEY";

    /**
     * 四方管理平台检查可用接口地址
     */
//    public static final String CHECK_AVAILABLE_MANAGE_API = "http://127.0.0.1:8216/api/checkAvailable/check";
    public static final String CHECK_AVAILABLE_MANAGE_API = "https://pay-api.aisa-pay.com/api/checkAvailable/check";

    /**
     * 系统类型定义
     **/
    public interface SYS_TYPE {
        String MCH = "MCH";
        String MGR = "MGR";
        String AGENT = "AGENT";
        Map<String, String> SYS_TYPE_MAP = new HashMap<>();
    }

    static {
        SYS_TYPE.SYS_TYPE_MAP.put(SYS_TYPE.MCH, "商户系统");
        SYS_TYPE.SYS_TYPE_MAP.put(SYS_TYPE.MGR, "运营平台");
        SYS_TYPE.SYS_TYPE_MAP.put(SYS_TYPE.AGENT, "代理系统");
    }

    /**
     * 测试商户商户号
     */
    public static final String TEST_MCH_NO = "M1691231056";


    /**
     * yes or no
     **/
    public static final byte NO = 0;
    public static final byte YES = 1;

    /**
     * 实时统计总订单
     */
    public static final String REAL_TIME_STAT = "Real_time_stat";

    /**
     * 实时统计成功订单
     */
    public static final String REAL_TIME_SUCCESS_STAT = "Real_time_success_stat";

    /**
     * 资金变动方向 加
     */
    public static final byte FUND_DIRECTION_INCREASE = 1;
    /**
     * 资金变动方向 减
     */
    public static final byte FUND_DIRECTION_REDUCE = 2;

    //业务类型,1-支付,2-提现,3-调账,4-提现驳回解冻
    /**
     * 业务类型 支付(商户、通道)/分润(代理)
     */
    public static final byte BIZ_TYPE_PAY_OR_INCOME = 1;
    /**
     * 业务类型 提现
     */
    public static final byte BIZ_TYPE_WITHDRAW = 2;
    /**
     * 业务类型 调账
     */
    public static final byte BIZ_TYPE_CHANGE = 3;

    /**
     * 提现驳回解冻
     */
    public static final byte BIZ_TYPE_UNFREEZE = 4;

    /**
     * 提现冻结
     */
    public static final byte BIZ_TYPE_FREEZE = 5;

    /**
     * 订单冲正
     */
    public static final byte BIZ_TYPE_REDO = 6;

    public static final byte CHANGE_BALANCE_TYPE_ORDER = 1;

    public static final byte CHANGE_BALANCE_TYPE_MANUAL = 2;

    public static final String CHANGE_BALANCE_REDIS_SUFFIX = "BalanceChange";

    public static final String ROBOTS_CONFIG_GROUP = "robotsConfigGroup";

    /**
     * 机器人相关，管理群商户号
     */
    public static final String ROBOTS_MGR_MCH = "M_ADMIN";

    public static String GetMchBizTypeString(byte type) {
        switch (type) {
            case BIZ_TYPE_PAY_OR_INCOME:
                return "支付";
            case BIZ_TYPE_WITHDRAW:
                return "提现";
            case BIZ_TYPE_CHANGE:
                return "调账";
            case BIZ_TYPE_UNFREEZE:
                return "驳回解冻";
            case BIZ_TYPE_REDO:
                return "测试冲正";
        }
        return "";
    }

    public static String GetAgentBizTypeString(byte type) {
        switch (type) {
            case BIZ_TYPE_PAY_OR_INCOME:
                return "分润";
            case BIZ_TYPE_WITHDRAW:
                return "提现";
            case BIZ_TYPE_CHANGE:
                return "调账";
            case BIZ_TYPE_UNFREEZE:
                return "驳回解冻";
            case BIZ_TYPE_REDO:
                return "测试冲正";
        }
        return "";
    }

    /**
     * 订单状态转字符串
     *
     * @param type
     * @return
     */
    public static String GetPayOrderTypeString(byte type) {
        switch (type) {
            case PayOrder.STATE_INIT:
                return "订单生成";
            case PayOrder.STATE_ING:
                return "支付中";
            case PayOrder.STATE_SUCCESS:
                return "支付成功";
            case PayOrder.STATE_FAIL:
                return "支付失败";
            case PayOrder.STATE_CANCEL:
                return "已撤销";
            case PayOrder.STATE_REFUND:
                return "测试冲正";
            case PayOrder.STATE_CLOSED:
                return "订单关闭";
            case PayOrder.STATE_ERROR:
                return "出码失败";
        }
        return "";
    }

    public static String GetPayOrderNotifyTypeString(byte type) {
        switch (type) {
            case 0:
                return "未发送";
            case 1:
                return "已发送";
        }
        return "";
    }

    /**
     * 通用 可用 / 禁用
     **/
    public static final int PUB_USABLE = 1;
    public static final int PUB_DISABLE = 0;

    public static final Map<Integer, String> PUB_USABLE_MAP = new HashMap<>();

    static {
        PUB_USABLE_MAP.put(PUB_USABLE, "正常");
        PUB_USABLE_MAP.put(PUB_DISABLE, "停用");
    }

    /**
     * 默认密码
     */
    public static final String DEFAULT_PWD = "123456";


    /**
     * 允许上传的的图片文件格式，需要与 WebSecurityConfig对应
     */
    public static final Set<String> ALLOW_UPLOAD_IMG_SUFFIX = new HashSet<>();

    static {
        ALLOW_UPLOAD_IMG_SUFFIX.add("jpg");
        ALLOW_UPLOAD_IMG_SUFFIX.add("png");
        ALLOW_UPLOAD_IMG_SUFFIX.add("jpeg");
        ALLOW_UPLOAD_IMG_SUFFIX.add("gif");
        ALLOW_UPLOAD_IMG_SUFFIX.add("mp4");
    }


    public static final long TOKEN_TIME = 60 * 60 * 2; //单位：s,  两小时


    //access_token 名称
    public static final String ACCESS_TOKEN_NAME = "iToken";

    /** ！！不同系统请放置不同的redis库 ！！ **/
    /**
     * 缓存key: 当前用户所有用户的token集合  example: TOKEN_1001_HcNheNDqHzhTIrT0lUXikm7xU5XY4Q
     */
    public static final String CACHE_KEY_TOKEN = "TOKEN_%s_%s";

    public static String getCacheKeyToken(Long sysUserId, String uuid) {
        return String.format(CACHE_KEY_TOKEN, sysUserId, uuid);
    }

    /**
     * 图片验证码 缓存key
     **/
    public static final String CACHE_KEY_IMG_CODE = "img_code_%s";

    public static String getCacheKeyImgCode(String imgToken) {
        return String.format(CACHE_KEY_IMG_CODE, imgToken);
    }

    /**
     * 回调URL的格前缀
     */
    public static final String PAY_RETURNURL_FIX_ONLY_JUMP_PREFIX = "ONLYJUMP_";

    /**
     * 登录认证类型
     **/
    public interface AUTH_TYPE {

        byte LOGIN_USER_NAME = 1; //登录用户名
    }


    //菜单类型
    public interface ENT_TYPE {

        String MENU_LEFT = "ML";  //左侧显示菜单
        String MENU_OTHER = "MO";  //其他菜单
        String PAGE_OR_BTN = "PB";  //页面 or 按钮

    }

    //接口类型
    public interface IF_CODE {
        String TESTPAY = "testpay";

        String ASIAPAY = "asiapay";

        String ASIAPAYMOBILE = "asiapaymobile";

        /**
         * 卡密
         */
        String CARDPAY = "cardpay";
        /**
         * 亿付支付
         */
        String YIFUPAY = "yifupay";
        /**
         * 七天支付
         */
        String QIPAY = "qipay";

        String JOMALONGPAY = "jomalongpay";

        String LANGUIFANG = "languifang";

        String RIXINPAY = "rixinpay";

        String RIXINPAY2 = "rixinpay2";

        String RIXINPAY3 = "rixinpay3";
        String RIXINPAY4 = "rixinpay4";

        String XXPAY = "xxpay";

        String SHAYUPAY = "shayupay";
        String SHAYUPAY2 = "shayupay2";

        String XXPAY2 = "xxpay2";

        String XXPAY3 = "xxpay3";

        String XXPAY4 = "xxpay4";
        String XXPAY5 = "xxpay5";
        String XXPAY6 = "xxpay6";

        String CHANGSHENG = "changsheng";
        String CHANGSHENG2 = "changsheng2";
        String CHANGSHENG3 = "changsheng3";

        String XIAOBAWANG = "xiaobawang";

        String TIANHEPAY = "tianhepay";

        String SHENGYANG = "shengyang";

        String GAWASY = "gawasy";

        String PAY731 = "pay731";

        String YONGHENG = "yongheng";

        String CHUANGXIN = "chuangxin";

        String XIAOJI = "xiaoji";

        String BENCHI = "benchi";

        String NAICHA = "naicha";

        String KAMIPAY = "kamipay";

        String DAFU = "dafu";

        String YONGHANG = "yonghang";

        String WANGTING = "wangting";

        String HONGYUN = "hongyun";

        String YEZIPAY = "yezipay";

        String STARPAY = "starpay";

        String JIUYIPAY = "jiuyipay";

        String CANGQIONG = "cangqiong";

        String MUFENG = "mufeng";

        String BAIHUI = "baihui";

        String DINGXIN = "dingxin";

        String CHAOREN = "chaoren";

        String YICHUANG = "yichuang";

        String BOXIN = "boxin";

        String SHUNXIN = "shunxin";

        String WEILAN = "weilan";

        String XUANJIE = "xuanjie";

        String GFPAY = "gfpay";

        String YUNYIN = "yunyin";

        String PANGPANG = "pangpang";

        String JUDING = "juding";

        String HAIFU = "haifu";

        String GANPAY = "ganpay";

        String RONGFU = "rongfu";

        String RONGFU2 = "rongfu2";

        String RONGFU3 = "rongfu3";

        String PPPAY = "pppay";

        String JIBA = "jiba";

        String DASHI = "dashi";

        String JULIANG = "juliang";

        String ANSHUNFA = "anshunfa";

        String YIFENG = "yifeng";

        String YONGSHENG = "yongsheng";

        String JIEDA = "jieda";

        String MINGFA = "mingfa";

        String MAYI = "mayi";

        String LANTIAN = "lantian";

        String BOLIN = "bolin";

        String G63A = "g63a";

        String ANGELPAY = "angelpay";

        String XIAMI = "xiami";

        String SHANDIAN = "shandian";

        String CHUANGYUAN = "chuangyuan";

        String HUOJIAN = "huojian";

        String XINGWANG = "xingwang";

        String FENGYE = "fengye";

        String DOUFU = "doufu";

        String XIAPI = "xiapi";

        String ZHAOCAIMAO = "zhaocaimao";

        String TESILA = "tesila";

        String DASHENG = "dasheng";
        String WWGOPAY = "wwgopay";
        String HENGSHENG = "hengsheng";
        String LIYUPAY = "liyupay";
        String HUOYAN = "huoyan";
        String TENGCHENG = "tengcheng";
        String XIAOSAN = "xiaosan";
        String XIONGMAO = "xiongmao";
        String XIONGMAO2 = "xiongmao2";
        String TENGHUI = "tenghui";
        String POHAO = "pohao";
        String FEIFAN = "feifan";
        String FEIFAN2 = "feifan2";
        String HUAYUE = "huayue";
        String FUSHENG = "fusheng";
        String JINGDONG = "jingdong";
        String XIQUE = "xique";
        String JIUYE = "jiuye";
        String HUABO = "huabo";
        String GUANGNIAN = "guangnian";
        String CAICAI = "caicai";
        String HUANLEDOU = "huanledou";
        String JINLI = "jinli";
        String ZHOUYI = "zhouyi";
        String SGPAY = "sgpay";
        String GEWASI = "gewasi";
        String XINGCHEN = "xingchen";
        String XXPAY7 = "xxpay7";

        String YIFUXINPAY = "yifuxinpay";

        String FEICUI = "feicui";

        String XXPAY8 = "xxpay8";

        String YILIAN = "yilian";

        String UIDPAY = "uidpay";

        String YAOGUAI = "yaoguai";

        String JIADE = "jiade";

        String SHANGGU = "shanggu";

        String YONGCAI = "yongcai";

        String XMPAY = "xmpay";

        String JIUZHOU = "jiuzhou";

        String XIGUA = "xigua";

        String HIPAY = "hipay";

        String XIANGGOU = "xianggou";

        String KUNPENG = "kunpeng";

        String DINGSHENG = "dingsheng";

        String QINGXIU = "qingxiu";

        String FUTEDUO = "futeduo";

        String GALIPAY = "galipay";

        String LANSHA = "lansha";

        String XXPAY9 = "xxpay9";

        String TIANYANG = "tianyang";

        String DIGUA = "digua";

        String QXPAY = "qxpay";

        String OPEN = "open";

        String WLAN = "wlan";
        String XIAOHONG = "xiaohong";
        String FEIYUE = "feiyue";

        String MOBILEPAY = "mobilepay";

        String ZHIFUPAY = "zhifupay";
        String QIANXI = "qianxi";
        String QIANGSHENG = "qiangsheng";
        String JINFAN = "jinfan";
        String JQKPAY = "jqkpay";

        String DEALPAY = "dealpay";

        String AOGUPAY = "aogupay";

        String LJPAY = "ljpay";
        String FENGYUN = "fengyun";
        String RUIZE = "ruize";
        String HONGMENG = "hongmeng";

        String XXPAY10 = "xxpay10";

        String XXPAY11 = "xxpay11";

        String SHPAY = "shpay";

        String RZPAY = "rzpay";

        String CYPAY = "cypay";

        String APPAY = "appay";

        String DOPAY = "dopay";

        String ODPAY = "odpay";

        String KXPAY = "kxpay";

        String MACKPAY = "mackpay";

        String MOQUEPAY = "moquepay";

        String WEIZHIFU = "weizhifu";

        String BAOFENG = "baofeng";

        String TANGBANG = "tangbang";

        String GOODPAY = "goodpay";

        String BYPAY = "bypay";

        String JCPAY = "jcpay";

        String YSPAY = "yspay";

        String STOREPAY = "storepay";
    }



    //支付方式代码
    public interface PAY_WAY_CODE {
        String ALI_BAR = "ALI_BAR";  //支付宝条码支付
        String YSF_BAR = "YSF_BAR";  //云闪付条码支付
        String WX_BAR = "WX_BAR";  //微信条码支付
    }

    //支付数据包 类型
    public interface PAY_DATA_TYPE {
        String PAY_URL = "payUrl";  //跳转链接的方式  redirectUrl
        String FORM = "form";  //表单提交
        String CODE_URL = "codeUrl";  //二维码URL
        String CODE_IMG_URL = "codeImgUrl";  //二维码图片显示URL
        String NONE = "none";  //无参数
    }
}
