package com.jeequan.jeepay.com.jeequan.service;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.service.CommonService.StatisticsService;
import com.jeequan.jeepay.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class RobotsService extends TelegramLongPollingBot {

    private static final String LOG_TAG = "ROBOTS_ERROR";
    private static final String REDIS_SOURCE_SUFFIX = "REDIS_SOURCE_";

    /**
     * 商户群的消息
     */
    private static final String REDIS_MCH_SOURCE_SUFFIX = "REDIS_MCH_SOURCE_";
    private static final String GROUP_ID = "\\b频道(?:id|ID)\\b";

    /**
     * 设置操作员
     */
    private static final String SET_OP = "设置操作员 @\\w+";

    private static final String DEL_OP = "删除操作员 @\\w+";

    private static final String LIST_OP = "操作员名单";

    /**
     * 绑定商户
     */
    private static final String BLIND_MCH = "绑定商户 M\\w+";

    private static final String UN_BLIND_MCH = "解绑商户 M\\w+";
    private static final String BLIND_DEL_MCH = "解绑全部商户";

    private static final String CURRENT_MCH = "全部商户";

    private static final String TODAY_BILL = "今日跑量";

    private static final String YESTERDAY_BILL = "昨日跑量";

    private static final String QUERY_BALANCE = "查询余额";

    private static final String FORWARD_QUERY = "[Zz]{2}\\s.*";


    /**
     * 绑定通道
     */
    private static final String BLIND_PASSAGE = "绑定通道\\s+([\\d,]+)";
    private static final String BLIND_PASSAGE_REMOVE = "解绑通道\\s+([\\d,]+)";
    private static final String BLIND_PASSAGE_CLEAR_ALL = "解绑全部通道";
    private static final String BLIND_PASSAGE_ALL = "全部通道";

    /**
     * 下发功能
     */
    private static final String ADD_RECORD = "[+-]\\d+(\\.\\d+)?";
    private static final String REVOKE_RECORD = "撤销下发";
    private static final String CLEAR_RECORD = "清除下发";
    /**
     * 记账
     */
    private static final String ADD_RECORD_TOTAL = "记账 [+-]?\\d+(\\.\\d+)?";
    private static final String REVOKE_RECORD_TOTAL = "撤销记账";
    private static final String CLEAR_RECORD_TOTAL = "清除记账";

    private static final String TODAY_RECORD = "今日账单";

    private static final String YESTERDAY_RECORD = "昨日账单";


    private static final String ROBOT_QUIT = "机器人退群";

    private static final String GET_USDT_TEXT = "[Zz]{1}0";

    private static final String GET_USDT = "/usdt";

    //    群发全部 -- 私发机器人内容，再回复该内容：群发全部
//    群发商户 -- 私发机器人内容，再回复该内容：群发商户
//    群发通道 -- 私发机器人内容，再回复该内容：群发通道
    private static final String SEND_ALL = "群发全部";
    private static final String SEND_ALL_MCH = "群发商户";
    private static final String SEND_ALL_PASSAGE = "群发通道";

    private static final String CAL = "计算.*";
    /**
     * 绑定管理群
     */
    private static final String BLIND_MGR = "绑定管理群";

    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private RobotsUserService robotsUserService;

    @Autowired
    private RobotsPassageService robotsPassageService;

    @Autowired
    private RobotsMchService robotsMchService;

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private RobotsMchRecordsService robotsMchRecordsService;

    @Autowired
    private PayOrderService payOrderService;

    @Autowired
    private PayPassageService payPassageService;

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()) {
            //检测权限，命令
            if (update.getMessage().isCommand() && !update.getMessage().getFrom().getIsBot()) {
                handleCommand(update);
            } else if (update.getMessage().isGroupMessage() || update.getMessage().isSuperGroupMessage()) {
                handleGroupCommand(update);
            } else if (update.getMessage().isUserMessage() && !update.getMessage().getFrom().getIsBot()) {
                handlePrivateCommand(update);
            }

        } else if (update.hasCallbackQuery()) {
            //用户点击按钮的回复
            handleCallbackQuery(update);
        }
    }


    /**
     * 私聊命令
     *
     * @param update
     */
    private void handlePrivateCommand(Update update) {

        if (update.hasMessage() && update.getMessage().getReplyToMessage() != null && update.getMessage().hasText()) {
            String userName = update.getMessage().getFrom().getUserName();

            if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                String text = update.getMessage().getText();

                //群发全部
                if (text.equals(SEND_ALL)) {
                    //获取全部
                    List<RobotsMch> list = findNonMchDuplicateChatIds();
                    for (int i = 0; i < list.size(); i++) {
                        sendForward(list.get(i).getChatId(), update.getMessage().getReplyToMessage());
                    }
                    List<RobotsPassage> listPassage = findNonPassageDuplicateChatIds();
                    for (int i = 0; i < listPassage.size(); i++) {
                        sendForward(listPassage.get(i).getChatId(), update.getMessage().getReplyToMessage());
                    }
                    return;
                }
                //群发商户
                if (text.equals(SEND_ALL_MCH)) {
                    List<RobotsMch> list = findNonMchDuplicateChatIds();
                    for (int i = 0; i < list.size(); i++) {
                        sendForward(list.get(i).getChatId(), update.getMessage().getReplyToMessage());
                    }
                    return;
                }
                //群发通道
                if (text.equals(SEND_ALL_PASSAGE)) {
                    List<RobotsPassage> list = findNonPassageDuplicateChatIds();
                    for (int i = 0; i < list.size(); i++) {
                        sendForward(list.get(i).getChatId(), update.getMessage().getReplyToMessage());
                    }
                    return;
                }
            }
        }


        if (update.hasMessage() && update.getMessage().getReplyToMessage() != null && update.getMessage().getReplyToMessage().hasText()) {
            String userName = update.getMessage().getFrom().getUserName();

            if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                String replyText = update.getMessage().getReplyToMessage().getText();

                //群发全部
                if (replyText.equals(SEND_ALL)) {
                    //获取全部
                    List<RobotsMch> list = findNonMchDuplicateChatIds();
                    for (int i = 0; i < list.size(); i++) {
                        sendForward(list.get(i).getChatId(), update.getMessage());
                    }
                    List<RobotsPassage> listPassage = findNonPassageDuplicateChatIds();
                    for (int i = 0; i < listPassage.size(); i++) {
                        sendForward(listPassage.get(i).getChatId(), update.getMessage());
                    }
                    return;
                }
                //群发商户
                if (replyText.equals(SEND_ALL_MCH)) {
                    List<RobotsMch> list = findNonMchDuplicateChatIds();
                    for (int i = 0; i < list.size(); i++) {
                        sendForward(list.get(i).getChatId(), update.getMessage());
                    }
                    return;
                }
                //群发通道
                if (replyText.equals(SEND_ALL_PASSAGE)) {
                    List<RobotsPassage> list = findNonPassageDuplicateChatIds();
                    for (int i = 0; i < list.size(); i++) {
                        sendForward(list.get(i).getChatId(), update.getMessage());
                    }
                    return;
                }
            }
        }

        if (update.getMessage().hasText() && update.getMessage().getText().equals("test123")) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("\uD83D\uDCE2\uD83D\uDCE2\uD83D\uDCE2\uD83D\uDCE2<b>亚洲科技四方系统出租</b>\uD83D\uDCE2\uD83D\uDCE2\uD83D\uDCE2\uD83D\uDCE2" + System.lineSeparator());
            stringBuffer.append("\uD83D\uDD38<b>优点</b>：独立海外服务器，极速响应，后台丝滑不卡顿，实测每分钟15000+订单依然流畅，UI简洁美观，精细化的权限管理，严谨的细节设计防止误操作，技术客服24小时在线" + System.lineSeparator());
            stringBuffer.append("\uD83D\uDD38<b>独家特色功能</b>：通道配置修改警报，手动补单数过多警报，订单异常警报，一键日切，一键复制通道，通道定时开关等等..及全功能机器人\uD83E\uDD16免费送。" + System.lineSeparator());
            stringBuffer.append("\uD83D\uDCCC<b>我方系统问题导致被撸回调，我方全额赔付！！</b>" + System.lineSeparator());
            stringBuffer.append("\uD83D\uDD38平台技术熟练对接MG,JDB,BBIN,FG,KY,Leg,VG,MT,FG,PG,AG,OB,DG GPK GT等各大包网" + System.lineSeparator());
            stringBuffer.append("\uD83D\uDD38同一支付通道对接后，仅需更换下单网关 回调IP  通道编码等 即可使用其同系统其余通道" + System.lineSeparator());
            stringBuffer.append("\uD83D\uDD38费用：" + System.lineSeparator());
            stringBuffer.append("搭建费免费最低需首充3000（费用充值到四方后台余额，系统跑量费用）" + System.lineSeparator());
            stringBuffer.append("系统跑量费率万5\uFE0F⃣" + System.lineSeparator());
            stringBuffer.append("对接通道限时免费" + System.lineSeparator());
            stringBuffer.append("预充值模式，充值赠送活动" + System.lineSeparator());
            stringBuffer.append("充值10000   赠送1188" + System.lineSeparator());
            stringBuffer.append("充值20000   赠送2288" + System.lineSeparator());
            stringBuffer.append("充值30000   赠送3388" + System.lineSeparator());
            stringBuffer.append("充值40000   赠送5588" + System.lineSeparator());
            stringBuffer.append("充值50000   赠送8888" + System.lineSeparator());
            stringBuffer.append("推出<b>系统包月服务</b>、<b>防御攻击服务</b>，详情可咨询系统客服，欢迎各位老板莅临光临！" + System.lineSeparator());
            stringBuffer.append("❤\uFE0F❤\uFE0F本系统❤\uFE0F❤\uFE0F" + System.lineSeparator());
            stringBuffer.append("基于springboot+mysql开发，集成activeMQ、redis、zookeeper等最大限度保证高性能、抗高并发" + System.lineSeparator());
            stringBuffer.append("技术团队全新开发，从根本上杜绝了市面上流传源码的漏洞，功能俱全、服务优质、行业顶流" + System.lineSeparator());
            stringBuffer.append("<b>四方管理后台演示</b>" + System.lineSeparator());
            stringBuffer.append("地址：http://mgr.ausapay.com" + System.lineSeparator());
            stringBuffer.append("账号：master" + System.lineSeparator());
            stringBuffer.append("密码：123456" + System.lineSeparator());
            stringBuffer.append("唯一联系人：@D999999" + System.lineSeparator());
            sendSingleMessage(update.getMessage().getChatId(), stringBuffer.toString());
            return;
        }
    }

    public List<RobotsPassage> findNonPassageDuplicateChatIds() {
        QueryWrapper<RobotsPassage> wrapper = new QueryWrapper<>();
        // 使用GROUP BY查询chat_id并且HAVING COUNT(chat_id) = 1来找到不重复的chat_id
        wrapper.select("chat_id").groupBy("chat_id").having("COUNT(chat_id) != 0");
        return robotsPassageService.list(wrapper);
    }

    public List<RobotsMch> findNonMchDuplicateChatIds() {
        return robotsMchService.list(RobotsMch.gw().ne(RobotsMch::getMchNo, CS.ROBOTS_MGR_MCH));
    }

    /**
     * /开头的命令
     *
     * @param update
     */
    private void handleCommand(Update update) {
        //发送机器人使用说明
        if (update.getMessage().getText().startsWith("/help")) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("欢迎使用亚洲科技四方系统机器人，以下是机器人使用说明:" + System.lineSeparator());
            stringBuffer.append("======================================" + System.lineSeparator());
            stringBuffer.append("<b>记账功能</b>:（记账数据每个群分开，无需绑定商户，敏感操作需管理员以及操作员权限）" + System.lineSeparator());
            stringBuffer.append("+xxx -- 添加下发金额，xxx为金额，例如：+1000" + System.lineSeparator());
            stringBuffer.append("-xxx -- 扣减下发金额，xxx为金额，例如：-1000" + System.lineSeparator());
            stringBuffer.append("撤销下发 -- 删除最后一笔下发记录" + System.lineSeparator());
            stringBuffer.append("清除下发 -- 删除当天的全部下发记录" + System.lineSeparator());
            stringBuffer.append("记账 xxx -- 添加记账金额，xxx为金额，例如：记账 1000" + System.lineSeparator());
            stringBuffer.append("记账 -xxx -- 扣减记账金额，xxx为金额，例如：记账 -1000" + System.lineSeparator());
            stringBuffer.append("撤销记账 -- 删除最后一笔记账记录" + System.lineSeparator());
            stringBuffer.append("清除记账 -- 删除当天的全部记账记录" + System.lineSeparator());
            stringBuffer.append("今日账单 -- 查看今日完整账单" + System.lineSeparator());
            stringBuffer.append("昨日账单 -- 查看昨日完整账单" + System.lineSeparator());
            stringBuffer.append("======================================" + System.lineSeparator());
            stringBuffer.append("<b>商户功能</b>:（需先绑定商户才能使用）" + System.lineSeparator());
            stringBuffer.append("查询余额 -- 查询商户或通道的余额" + System.lineSeparator());
            stringBuffer.append("XXXXXXX -- 直接发送平台订单号或商户订单号<b>并带图</b>进行<b>查单</b>操作" + System.lineSeparator());
            stringBuffer.append("XXXXXXX 换行 XXXXXXX -- 多单查询每个单号间请换行<b>并带图</b>进行<b>查单</b>操作" + System.lineSeparator());
            stringBuffer.append("zz xxx-- 回复商户发单消息进行转发，例如：zz 加急加急" + System.lineSeparator());
            stringBuffer.append("今日跑量 -- 查看今日商户或通道完整跑量统计" + System.lineSeparator());
            stringBuffer.append("昨日跑量 -- 查看昨日商户或通道完整跑量统计" + System.lineSeparator());
            stringBuffer.append("======================================" + System.lineSeparator());
            stringBuffer.append("<b>通用功能</b>:" + System.lineSeparator());
            stringBuffer.append("z0 -- 查询今日U价" + System.lineSeparator());
            stringBuffer.append("计算 -- 进行四则运算，例如：计算 (1+2)*3" + System.lineSeparator());

            sendSingleMessage(update.getMessage().getChatId(), stringBuffer.toString());

            return;
        }


        if (update.getMessage().getText().startsWith(GET_USDT)) {
            sendUSDT(update.getMessage().getChatId());
            return;
        }
    }


    /**
     * 命令管理
     *
     * @param update
     */
    private void handleGroupCommand(Update update) {
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String userName = message.getFrom().getUserName();
        Message messageReply = message.getReplyToMessage();

        if (messageReply != null) {
            //检测是否查单转发信息
            //REDIS_SOURCE_SUFFIX  存储的是 转发到 通道群的suffix+id 商户群 message
            Message messageSource = RedisUtil.getObject(REDIS_SOURCE_SUFFIX + messageReply.getMessageId(), Message.class);
            if (messageSource != null) {
                sendQueryMessage(message, messageSource);
                return;
            }

            //检测是否催单信息  FORWARD_QUERY
            //REDIS_MCH_SOURCE_SUFFIX 存储的是 商户群suffix+id 通道群message
            Message messageForwardSource = RedisUtil.getObject(REDIS_MCH_SOURCE_SUFFIX + messageReply.getMessageId(), Message.class);
            if (messageForwardSource != null && message.hasText()) {
                Pattern regex = Pattern.compile(FORWARD_QUERY);
                Matcher matcher = regex.matcher(message.getText());
                if (matcher.matches()) {
                    String forwardText = message.getText().substring(3);
                    sendReplyMessage(messageForwardSource.getChatId(), messageForwardSource.getMessageId(), forwardText);
                }
                return;
            }
        }
        if (message.hasPhoto() || message.hasVideo()) {
            //todo 通过mediaGroupId 判断是否同一条group消息
            String text = message.getCaption();
            if (StringUtils.isNotEmpty(text)) {
                if (text.contains("\n")) {
                    String[] texts = text.split("\n");
                    for (int i = 0; i < texts.length; i++) {
                        sendSingleQuery(texts[i].trim(), message);
                    }
                } else {
                    sendSingleQuery(text, message);
                }
            }
            return;
        }

        if (!message.hasText()) {
            return;
        }

        if (update.getMessage().getFrom().getIsBot()) {
            return;
        }
        //==================================匹配文字命令==========================================================
        String text = message.getText().trim();
        //绑定管理群
        if (text.equals(BLIND_MGR)) {
            //是否admin
            if (robotsUserService.checkIsAdmin(userName)) {
                //不是商户群就覆盖  chatId下有商户号
                RobotsMch robotsMch = robotsMchService.getMch(chatId);
                if (checkIsMchChat(robotsMch)) {
                    sendSingleMessage(chatId, "当前群已绑定为商户群,不可绑定为四方管理群!");
                    return;
                }
                if (checkIsPassageChat(chatId)) {
                    sendSingleMessage(chatId, "当前群已绑定为通道群,不可绑定为四方管理群!");
                    return;
                }
                robotsMchService.updateManageMch(chatId);
                sendSingleMessage(chatId, "当前群绑定四方管理群成功!");
            }
            return;
        }

        //机器人退群
        if (text.equals(ROBOT_QUIT)) {
            //是否admin
            if (robotsUserService.checkIsAdmin(userName)) {
                sendSingleMessage(chatId, "收到退群命令");
                LeaveChat leaveChat = new LeaveChat();
                leaveChat.setChatId(chatId);
                try {
                    execute(leaveChat);
                    log.info("Bot left the chat...");
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        //计算
        Pattern patternCal = Pattern.compile(CAL);
        Matcher matcherCal = patternCal.matcher(text);
        if (matcherCal.matches()) {
            log.error("匹配到计算");
            try {
                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine engine = mgr.getEngineByName("JavaScript");
                String temp = text.replaceAll("计算", "").trim();
                // 计算结果
                Object result = engine.eval(temp);

                // 格式化结果为三位小数
                DecimalFormat df = new DecimalFormat("#.###");
                String formattedResult = df.format(result);

                sendReplyMessage(chatId, message.getMessageId(), formattedResult);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return;
        }

        //GET_USDT_TEXT
        Pattern patternUSDT = Pattern.compile(GET_USDT_TEXT);
        Matcher matcherUSDT = patternUSDT.matcher(text);
        if (matcherUSDT.matches()) {
            sendUSDT(chatId);
            return;
        }

        //绑定商户-管理员
        Pattern patternBlindMch = Pattern.compile(BLIND_MCH);
        Matcher matcherBlindMch = patternBlindMch.matcher(text);
        if (matcherBlindMch.matches()) {
            if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                RobotsMch robotsMchAdmin = robotsMchService.getManageMch();
                if (robotsMchAdmin != null && robotsMchAdmin.getChatId().longValue() == chatId.longValue()) {
                    sendSingleMessage(chatId, "当前群已绑定为四方管理群，不可重复绑定商户");
                    return;
                }

                if (checkIsPassageChat(chatId)) {
                    sendSingleMessage(chatId, "当前群已绑定为通道群，不可重复绑定商户");
                    return;
                }

                String mchNo = text.substring(5);
                MchInfo mchInfo = mchInfoService.getById(mchNo);

                if (mchInfo == null) {
                    sendSingleMessage(chatId, "未查询到该商户 [" + mchNo + "] 请检查！");
                } else {
                    robotsMchService.updateBlindMch(chatId, mchNo);
                    sendSingleMessage(chatId, "绑定商户成功! [ " + mchNo + " ] " + mchInfo.getMchName());
                    sendCurrentMch(chatId);
                }
                return;
            }
            return;
        }

        /**
         * 商户解绑
         */
        Pattern patternUnBlindMch = Pattern.compile(UN_BLIND_MCH);
        Matcher matcherUbBlindMch = patternUnBlindMch.matcher(text);
        if (matcherUbBlindMch.matches()) {
//          是否admin
            if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                //是否已绑定商户
                String mchNo = text.substring(5);
                if (robotsMchService.unBlindMch(chatId, mchNo)) {
                    sendSingleMessage(chatId, "商户解绑成功!");
                    sendCurrentMch(chatId);
                } else {
                    sendSingleMessage(chatId, "当前群未绑定商户");
                }
            }
            return;
        }

        /**
         * 解绑全部商户
         */
        if (text.trim().equals(BLIND_DEL_MCH)) {
//          是否admin
            if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                RobotsMch robotsMchAdmin = robotsMchService.getManageMch();
                if (robotsMchAdmin != null && robotsMchAdmin.getChatId().longValue() == chatId.longValue()) {
                    sendSingleMessage(chatId, "当前群已绑定为四方管理群，不可重复绑定商户");
                    return;
                }
                //是否已绑定商户
                if (robotsMchService.unBlindAllMch(chatId)) {
                    sendSingleMessage(chatId, "全部商户解绑成功!");
                    sendCurrentMch(chatId);
                } else {
                    sendSingleMessage(chatId, "当前群未绑定商户");
                }
            }
            return;
        }

        //全部商户
        if (text.trim().equals(CURRENT_MCH)) {
            if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                sendCurrentMch(chatId);
            }
            return;
        }


        //设置操作员  无关那个群，针对每个商户群-通道群
        Pattern patternSetOP = Pattern.compile(SET_OP);
        Matcher matcherSetOP = patternSetOP.matcher(text);
        if (matcherSetOP.matches()) {
            //是否admin
            if (robotsUserService.checkIsAdmin(userName)) {
                String opUserName = text.substring(7);
                RobotsUser robotsUser = robotsUserService.getById(opUserName);
                if (robotsUser == null) {
                    robotsUser = new RobotsUser();
                    robotsUser.setUserName(opUserName);
                    robotsUserService.save(robotsUser);
                    sendSingleMessage(chatId, "用户 [ " + opUserName + " ] 添加操作员权限成功");
                } else {
                    sendSingleMessage(chatId, "用户 [ " + opUserName + " ] 已添加过!");
                }
            }
            return;
        }

        //删除操作员
        Pattern patternDelOP = Pattern.compile(DEL_OP);
        Matcher matcherDelOP = patternDelOP.matcher(text);
        if (matcherDelOP.matches()) {
            //是否admin
            if (robotsUserService.checkIsAdmin(userName)) {
                String opUserName = text.substring(7);
                boolean isSuccess = robotsUserService.removeById(opUserName);
                if (isSuccess) {
                    sendSingleMessage(chatId, "用户 [ " + opUserName + " ] 删除操作员成功");
                } else {
                    sendSingleMessage(chatId, "用户 [ " + opUserName + " ] 未查询到操作员记录");
                }
            }
            return;
        }

        //操作员名单
        if (text.equals(LIST_OP)) {
            //是否admin 或操作员
            if (robotsUserService.checkIsAdmin(userName)) {
                //操作员名单
                List<RobotsUser> list = robotsUserService.list();
                if (list.size() > 0) {
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append("当前所有操作员:" + System.lineSeparator());
                    list.forEach(item -> {
                        stringBuffer.append(" [ " + item.getUserName() + " ] " + System.lineSeparator());
                    });
                    sendSingleMessage(chatId, stringBuffer.toString());
                } else {
                    sendSingleMessage(chatId, "当前没有操作员记录!");
                }
            }
            return;
        }

        //今日跑量
        if (text.equals(TODAY_BILL)) {
            RobotsMch robotsMch = robotsMchService.getMch(chatId);
            List<RobotsPassage> robotsPassageList = robotsPassageService.list(RobotsPassage.gw().eq(RobotsPassage::getChatId, chatId));
            //是否已绑定商户或通道
            if (robotsMch != null && StringUtils.isNotEmpty(robotsMch.getMchNo())) {
                String mchNoStr = robotsMch.getMchNo();
                JSONArray jsonArray = JSONArray.parseArray(mchNoStr);

                //查今日账单
                Date today = DateUtil.parse(DateUtil.today());

                for (int i = 0; i < jsonArray.size(); i++) {
                    String mchNo = jsonArray.getString(i); // 假设你要根据某个键来查找
                    MchInfo mchInfo = mchInfoService.getById(mchNo);
                    String mchInfoStr = "[" + mchInfo.getMchNo() + "] <b>" + mchInfo.getMchName() + "</b>";

                    StatisticsMch todayStatisticsMch = statisticsService.QueryStatisticsMchByDate(mchNo, today);
                    List<StatisticsMchProduct> statisticsMchProductList = statisticsService.QueryStatMchProduct(mchNo, today);

                    if (todayStatisticsMch == null) {
                        sendSingleMessage(chatId, mchInfoStr + " 没有今日跑量记录");
                    } else {
                        sendMchProduct(statisticsMchProductList, chatId, mchInfoStr + " 今日");
                        sendDayStat(todayStatisticsMch, chatId, mchInfoStr + " ");
                    }
                }
                return;
            }
            if (robotsPassageList.size() > 0) {
                Date today = DateUtil.parse(DateUtil.today());

                List<PayPassage> passageList = getPassageInfoList(robotsPassageList);

                StringBuffer stringBuffer = new StringBuffer();
                Long totalBalance = 0L;
                stringBuffer.append("统计日期：<b>" + DateUtil.today() + "</b>" + System.lineSeparator());
                for (int i = 0; i < passageList.size(); i++) {
                    PayPassage payPassage = passageList.get(i);
                    StatisticsPassage statisticsPassage = statisticsService.QueryStatisticsPassageByDate(payPassage.getPayPassageId(), today);
                    String passageInfoStr = "通道：[" + payPassage.getPayPassageId() + "] <b>" + payPassage.getPayPassageName() + "</b>";
                    if (statisticsPassage == null) {
                        stringBuffer.append(passageInfoStr + " 没有今日跑量记录" + System.lineSeparator());
                    } else {
                        stringBuffer.append(passageInfoStr + " 今日统计：" + AmountUtil.convertCent2Dollar(statisticsPassage.getTotalSuccessAmount()) + System.lineSeparator());
                        totalBalance += statisticsPassage.getTotalSuccessAmount();
                    }
                }
                stringBuffer.append("跑量汇总：" + AmountUtil.convertCent2Dollar(totalBalance) + System.lineSeparator());
                sendSingleMessage(chatId, stringBuffer.toString());
                return;
            }
            sendSingleMessage(chatId, "未绑定商户或通道");
            return;
        }

        //昨日跑量
        if (text.equals(YESTERDAY_BILL)) {
            RobotsMch robotsMch = robotsMchService.getMch(chatId);
            List<RobotsPassage> robotsPassageList = robotsPassageService.list(RobotsPassage.gw().eq(RobotsPassage::getChatId, chatId));

            Date todayTemp = DateUtil.parse(DateUtil.today());
            DateTime yesterday = DateUtil.offsetDay(todayTemp, -1);

            //是否已绑定商户或通道
            if (robotsMch != null && StringUtils.isNotEmpty(robotsMch.getMchNo())) {
                String mchNoStr = robotsMch.getMchNo();
                JSONArray jsonArray = JSONArray.parseArray(mchNoStr);


                for (int i = 0; i < jsonArray.size(); i++) {
                    String mchNo = jsonArray.getString(i); // 假设你要根据某个键来查找
                    MchInfo mchInfo = mchInfoService.getById(mchNo);
                    String mchInfoStr = "[" + mchInfo.getMchNo() + "] <b>" + mchInfo.getMchName() + "</b>";

                    StatisticsMch todayStatisticsMch = statisticsService.QueryStatisticsMchByDate(mchNo, yesterday);
                    List<StatisticsMchProduct> statisticsMchProductList = statisticsService.QueryStatMchProduct(mchNo, yesterday);

                    if (todayStatisticsMch == null) {
                        sendSingleMessage(chatId, mchInfoStr + " 没有昨日跑量记录");
                    } else {
                        sendMchProduct(statisticsMchProductList, chatId, mchInfoStr + " 昨日");
                        sendDayStat(todayStatisticsMch, chatId, mchInfoStr + " ");
                    }
                }
                return;
            }
            if (robotsPassageList.size() > 0) {

                List<PayPassage> passageList = getPassageInfoList(robotsPassageList);

                StringBuffer stringBuffer = new StringBuffer();
                Long totalBalance = 0L;
                stringBuffer.append("统计日期：<b>" + DateUtil.format(yesterday, "yyyy-MM-dd") + "</b>" + System.lineSeparator());
                for (int i = 0; i < passageList.size(); i++) {
                    PayPassage payPassage = passageList.get(i);
                    StatisticsPassage statisticsPassage = statisticsService.QueryStatisticsPassageByDate(payPassage.getPayPassageId(), yesterday);
                    String passageInfoStr = "通道：[" + payPassage.getPayPassageId() + "] <b>" + payPassage.getPayPassageName() + "</b>";
                    if (statisticsPassage == null) {
                        stringBuffer.append(passageInfoStr + " 没有昨日跑量记录" + System.lineSeparator());
                    } else {
                        stringBuffer.append(passageInfoStr + " 昨日统计：" + AmountUtil.convertCent2Dollar(statisticsPassage.getTotalSuccessAmount()) + System.lineSeparator());
                        totalBalance += statisticsPassage.getTotalSuccessAmount();
                    }
                }
                stringBuffer.append("跑量汇总：" + AmountUtil.convertCent2Dollar(totalBalance) + System.lineSeparator());
                sendSingleMessage(chatId, stringBuffer.toString());
                return;
            }
            sendSingleMessage(chatId, "未绑定商户或通道");
            return;
        }

        //查询余额
        if (text.equals(QUERY_BALANCE)) {
            //查询是商户群还是通道群
            RobotsMch robotsMch = robotsMchService.getMch(chatId);
            List<RobotsPassage> robotsPassageList = robotsPassageService.list(RobotsPassage.gw().eq(RobotsPassage::getChatId, chatId));

            //是否已绑定商户
            if (robotsMch != null && StringUtils.isNotEmpty(robotsMch.getMchNo())) {
                String mchStr = robotsMch.getMchNo();
                if (StringUtils.isEmpty(mchStr)) {
                    sendSingleMessage(chatId, "未绑定商户或没有记录");
                    return;
                }

                JSONArray jsonArray = JSONArray.parseArray(mchStr);
                Long totalBalance = 0L;
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0; i < jsonArray.size(); i++) {
                    String mchNo = jsonArray.getString(i);
                    MchInfo mchInfo = mchInfoService.queryMchInfo(mchNo);
                    if (mchInfo == null) {
                        stringBuffer.append("没有查询到商户 [" + mchNo + "] 的记录");
                    } else {
                        totalBalance += mchInfo.getBalance();
                        stringBuffer.append("[ " + mchNo + " ] <b>" + mchInfo.getMchName() + "</b> -- 余额为：" + AmountUtil.convertCent2Dollar(mchInfo.getBalance()) + System.lineSeparator());
                    }
                }
                stringBuffer.append("余额汇总：" + AmountUtil.convertCent2Dollar(totalBalance) + System.lineSeparator());
                sendSingleMessage(chatId, stringBuffer.toString());
                return;
            }
            if (robotsPassageList.size() > 0) {
                List<PayPassage> passageList = getPassageInfoList(robotsPassageList);
                //根据名字排序
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("当前群已绑定通道列表：" + System.lineSeparator());
                Long totalBalance = 0L;
                for (int i = 0; i < passageList.size(); i++) {
                    PayPassage payPassage = passageList.get(i);
                    stringBuffer.append("通道：[" + payPassage.getPayPassageId() + "] <b>" + payPassage.getPayPassageName() + "</b> -- 余额为：" + AmountUtil.convertCent2Dollar(payPassage.getBalance()) + System.lineSeparator());
                    totalBalance += payPassage.getBalance();
                }
                stringBuffer.append("余额汇总：" + AmountUtil.convertCent2Dollar(totalBalance) + System.lineSeparator());
                sendSingleMessage(chatId, stringBuffer.toString());
                return;
            }
            sendSingleMessage(chatId, "未绑定商户或通道");
            return;
        }

        //下发-账单记录跟群走
        Pattern patternAddRecord = Pattern.compile(ADD_RECORD);
        Matcher matcherAddRecord = patternAddRecord.matcher(text);
        if (matcherAddRecord.matches()) {
            if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                try {
                    String amountStr = text;
                    Long amount = Long.parseLong(AmountUtil.convertDollar2Cent(amountStr));
                    if (amount.longValue() == 0) {
                        return;
                    }
                    String userNickname = "";
                    if (StringUtils.isNotEmpty(message.getFrom().getFirstName())) {
                        userNickname += message.getFrom().getFirstName();
                    }
                    if (StringUtils.isNotEmpty(message.getFrom().getLastName())) {
                        userNickname += message.getFrom().getLastName();
                    }
                    robotsMchRecordsService.AddDayRecord(chatId, amount, userNickname);

                    Date today = DateUtil.parse(DateUtil.today());
                    sendBillStat(chatId, today, message.getMessageId(), "今日");
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            return;
        }
        //撤销下发
        if (text.equals(REVOKE_RECORD)) {
            if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                try {
                    String userNickname = "";
                    if (StringUtils.isNotEmpty(message.getFrom().getFirstName())) {
                        userNickname += message.getFrom().getFirstName();
                    }
                    if (StringUtils.isNotEmpty(message.getFrom().getLastName())) {
                        userNickname += message.getFrom().getLastName();
                    }
                    Date today = DateUtil.parse(DateUtil.today());
                    robotsMchRecordsService.RemoveRecentlyRecord(chatId, userNickname, today);
                    sendBillStat(chatId, today, message.getMessageId(), "今日");
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            return;
        }

        //清除下发  清除当天全部
        if (text.equals(CLEAR_RECORD)) {
            if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                try {
                    String userNickname = "";
                    if (StringUtils.isNotEmpty(message.getFrom().getFirstName())) {
                        userNickname += message.getFrom().getFirstName();
                    }
                    if (StringUtils.isNotEmpty(message.getFrom().getLastName())) {
                        userNickname += message.getFrom().getLastName();
                    }
                    //清除全部
                    Date today = DateUtil.parse(DateUtil.today());
                    robotsMchRecordsService.RemoveAllRecordByDate(chatId, userNickname, today);
                    sendBillStat(chatId, today, message.getMessageId(), "今日");
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            return;
        }


        //记账
        Pattern patternAddRecordTotal = Pattern.compile(ADD_RECORD_TOTAL);
        Matcher matcherAddRecordTotal = patternAddRecordTotal.matcher(text);
        if (matcherAddRecordTotal.matches()) {
            if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                try {
                    //没有就添加记账群记录
                    RobotsMch robotsMch = robotsMchService.getById(chatId);
                    if (robotsMch == null) {
                        robotsMch = new RobotsMch();
                        robotsMch.setChatId(chatId);
                        robotsMch.setMchNo("");
                        robotsMch.setBalance(0L);
                        robotsMchService.save(robotsMch);
                    }

                    String amountStr = text.substring(3);
                    Long amount = Long.parseLong(AmountUtil.convertDollar2Cent(amountStr));
                    if (amount.longValue() == 0) {
                        return;
                    }
                    String userNickname = "";
                    if (StringUtils.isNotEmpty(message.getFrom().getFirstName())) {
                        userNickname += message.getFrom().getFirstName();
                    }
                    if (StringUtils.isNotEmpty(message.getFrom().getLastName())) {
                        userNickname += message.getFrom().getLastName();
                    }
                    robotsMchRecordsService.AddTotalRecord(chatId, amount, userNickname);
                    robotsMch.setBalance(robotsMch.getBalance() + amount);
                    robotsMchService.saveOrUpdate(robotsMch);

                    Date today = DateUtil.parse(DateUtil.today());
                    sendBillStat(chatId, today, message.getMessageId(), "今日");
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

            }
            return;
        }

        //撤销记账
        if (text.equals(REVOKE_RECORD_TOTAL)) {
            if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                try {
                    String userNickname = "";
                    if (StringUtils.isNotEmpty(message.getFrom().getFirstName())) {
                        userNickname += message.getFrom().getFirstName();
                    }
                    if (StringUtils.isNotEmpty(message.getFrom().getLastName())) {
                        userNickname += message.getFrom().getLastName();
                    }

                    //没有就添加记账群记录
                    RobotsMch robotsMch = robotsMchService.getById(chatId);
                    if (robotsMch == null) {
                        robotsMch = new RobotsMch();
                        robotsMch.setChatId(chatId);
                        robotsMch.setMchNo("");
                        robotsMch.setBalance(0L);
                        robotsMchService.save(robotsMch);
                    }
                    Date today = DateUtil.parse(DateUtil.today());

                    Long amount = robotsMchRecordsService.RemoveRecentlyRecordTotal(chatId, userNickname, today);
                    robotsMch.setBalance(robotsMch.getBalance() - amount);
                    robotsMchService.saveOrUpdate(robotsMch);

                    sendBillStat(chatId, today, message.getMessageId(), "今日");
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            return;
        }

        //清除记账  清除当天全部
        if (text.equals(CLEAR_RECORD_TOTAL)) {
            if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                try {
                    String userNickname = "";
                    if (StringUtils.isNotEmpty(message.getFrom().getFirstName())) {
                        userNickname += message.getFrom().getFirstName();
                    }
                    if (StringUtils.isNotEmpty(message.getFrom().getLastName())) {
                        userNickname += message.getFrom().getLastName();
                    }
                    //清除全部
                    Date today = DateUtil.parse(DateUtil.today());
                    Long amount = robotsMchRecordsService.RemoveAllRecordTotalByDate(chatId, userNickname, today);

                    //没有就添加记账群记录
                    RobotsMch robotsMch = robotsMchService.getById(chatId);
                    if (robotsMch == null) {
                        robotsMch = new RobotsMch();
                        robotsMch.setChatId(chatId);
                        robotsMch.setMchNo("");
                        robotsMch.setBalance(0L);
                        robotsMchService.save(robotsMch);
                    }
                    robotsMch.setBalance(robotsMch.getBalance() - amount);
                    robotsMchService.saveOrUpdate(robotsMch);

                    sendBillStat(chatId, today, message.getMessageId(), "今日");
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            return;
        }


        //今日账单
        if (text.equals(TODAY_RECORD)) {
            try {
                Date today = DateUtil.parse(DateUtil.today());
                sendBillStat(chatId, today, message.getMessageId(), "今日");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return;
        }

        //昨日账单
        if (text.equals(YESTERDAY_RECORD)) {
            try {
                sendBillStat(chatId, DateUtil.yesterday(), message.getMessageId(), "昨日");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return;
        }

        //绑定通道  BLIND_PASSAGE
        Pattern patternBlindPassage = Pattern.compile(BLIND_PASSAGE);
        Matcher matcherBlindPassage = patternBlindPassage.matcher(text);
        if (matcherBlindPassage.matches()) {
            try {
                //是否空群  是否有权限
                if (checkBlindPassage(chatId, userName)) {
                    String passageStr = text.substring(5);
                    String[] passageIds = passageStr.replaceAll(" ", "").split(",");

                    List<Long> noFindList = new ArrayList<>();

                    Map<Long, PayPassage> payPassageMap = payPassageService.getPayPassageMap();
                    for (int index = 0; index < passageIds.length; index++) {
                        //检查格式以及是否存在通道
                        Long passageId = Long.parseLong(passageIds[index]);
                        if (payPassageMap.containsKey(passageId)) {
                            RobotsPassage robotsPassage = new RobotsPassage();
                            robotsPassage.setChatId(chatId);
                            robotsPassage.setPassageId(passageId);
                            robotsPassageService.saveOrUpdate(robotsPassage);
                        } else {
                            noFindList.add(passageId);
                        }
                    }

                    //发送当前绑定列表
                    SendPassageList(chatId);

                    if (noFindList.size() > 0) {
                        //根据名字排序
                        StringBuffer stringBufferNo = new StringBuffer();
                        stringBufferNo.append("绑定失败的ID：" + System.lineSeparator());
                        for (int i = 0; i < noFindList.size(); i++) {
                            stringBufferNo.append("通道ID：[" + noFindList.get(i) + "] 不存在,请检查ID" + System.lineSeparator());
                        }
                        sendSingleMessage(chatId, stringBufferNo.toString());
                    }
                }
            } catch (Exception e) {
                log.error("聊天原文:" + text);
                log.error(e.getMessage(), e);
                sendSingleMessage(chatId, "命令格式有误请检查,格式为[绑定通道 1000]或[绑定通道 1000,2000,3000]");
            }
            return;
        }

        //解绑通道  BLIND_PASSAGE
        Pattern patternBlindPassageRemove = Pattern.compile(BLIND_PASSAGE_REMOVE);
        Matcher matcherBlindPassageRemove = patternBlindPassageRemove.matcher(text);
        if (matcherBlindPassageRemove.matches()) {
            try {
                if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                    String passageStr = text.substring(5);
                    String[] passageIds = passageStr.replaceAll(" ", "").split(",");

                    for (int index = 0; index < passageIds.length; index++) {
                        //移除
                        Long passageId = Long.parseLong(passageIds[index]);
                        robotsPassageService.removeById(passageId);
                    }
                    sendSingleMessage(chatId, "解绑成功!");
                    //发送当前绑定列表
                    SendPassageList(chatId);
                }
            } catch (Exception e) {
                log.error("聊天原文:" + text);
                log.error(e.getMessage(), e);
                sendSingleMessage(chatId, "命令格式有误请检查,格式为[解绑通道 1000]或[解绑通道 1000,2000,3000]");
            }
            return;
        }

        //解绑全部通道
        if (text.trim().equals(BLIND_PASSAGE_CLEAR_ALL)) {
            if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                robotsPassageService.remove(RobotsPassage.gw().eq(RobotsPassage::getChatId, chatId));
                sendSingleMessage(chatId, "解绑全部通道执行成功");
                //发送当前绑定列表
                SendPassageList(chatId);
            }
            return;
        }

        //全部通道
        if (text.trim().equals(BLIND_PASSAGE_ALL)) {
            if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                //发送当前绑定列表
                SendPassageList(chatId);
            }
            return;
        }
    }


    /**
     * 发送消息
     *
     * @param chatId
     * @param messageId
     * @param messageStr
     */
    private void sendReplyMessage(Long chatId, Integer messageId, String messageStr) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(false);
        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(messageId);
        sendMessage.setText(messageStr);
        sendMessage.setParseMode(ParseMode.HTML);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("{} {}", LOG_TAG, e);
        }
    }

    private void sendReplyMessageWithMenu(Long chatId, Integer messageId, String messageStr) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(false);
        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(messageId);
        sendMessage.setText(messageStr);
        sendMessage.setParseMode(ParseMode.HTML);
        // 創建Inline Keyboard
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        // 添加按鈕
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("昨日账单");
        inlineKeyboardButton1.setCallbackData(YESTERDAY_RECORD);

        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton2.setText("今日账单");
        inlineKeyboardButton2.setCallbackData(TODAY_RECORD);

        rowInline.add(inlineKeyboardButton1);
        rowInline.add(inlineKeyboardButton2);

        // 添加按鈕到行，然後將行添加到Inline Keyboard
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);

        // 將Inline Keyboard添加到回復消息中
        sendMessage.setReplyMarkup(markupInline);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("{} {}", LOG_TAG, e);
        }
    }

    /**
     * 发消息
     *
     * @param sendMessage
     */
    private Message sendSingleMessage(SendMessage sendMessage) {
        try {
            return execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("{} {}", LOG_TAG, e);
            log.error(e.getMessage());
        }
        return null;
    }

    private void sendForward(Long chatId, Message message) {
        try {
            ForwardMessage forwardMessage = new ForwardMessage(chatId + "", message.getChatId() + "", message.getMessageId());
            execute(forwardMessage);
        } catch (TelegramApiException e) {
            log.error("{} {}", LOG_TAG, e);
        }
    }

    /**
     * 转发到商户群
     *
     * @param message       本条消息
     * @param sourceMessage 商户群消息
     */
    private void sendQueryMessage(Message message, Message sourceMessage) {
        try {
            Long chatId = sourceMessage.getChatId();

            StringBuffer stringBuffer = new StringBuffer();


            if (message.isReply()) {
                if (message.hasPhoto()) {
                    SendPhoto sendPhoto = new SendPhoto();

                    stringBuffer.append(message.getCaption() + System.lineSeparator());
                    stringBuffer.append(System.lineSeparator());
                    stringBuffer.append(sourceMessage.getText() + System.lineSeparator());

                    sendPhoto.setChatId(chatId); // Replace with the destination chat ID
                    sendPhoto.setPhoto(new InputFile(message.getPhoto().get(0).getFileId()));
                    sendPhoto.setCaption(stringBuffer.toString());
                    sendPhoto.setReplyToMessageId(sourceMessage.getMessageId());
                    sendPhoto.setParseMode(ParseMode.HTML);
                    execute(sendPhoto);
                } else if (message.hasText()) {
                    SendMessage sendMessage = new SendMessage();

                    stringBuffer.append(message.getText() + System.lineSeparator());
                    stringBuffer.append(System.lineSeparator());
                    stringBuffer.append(sourceMessage.getText() + System.lineSeparator());

                    sendMessage.setChatId(chatId);
                    sendMessage.setText(stringBuffer.toString());
                    sendMessage.setReplyToMessageId(sourceMessage.getMessageId());
                    sendMessage.setParseMode(ParseMode.HTML);
                    execute(sendMessage);
                } else if (message.hasVideo()) {
                    SendVideo sendVideo = new SendVideo();

                    stringBuffer.append(message.getCaption() + System.lineSeparator());
                    stringBuffer.append(System.lineSeparator());
                    stringBuffer.append(sourceMessage.getText() + System.lineSeparator());

                    sendVideo.setChatId(chatId); // Replace with the destination chat ID
                    sendVideo.setVideo(new InputFile(message.getVideo().getFileId()));
                    sendVideo.setCaption(stringBuffer.toString());
                    sendVideo.setReplyToMessageId(sourceMessage.getMessageId());
                    sendVideo.setParseMode(ParseMode.HTML);
                    execute(sendVideo);
                }
            }
        } catch (Exception e) {
            log.error("{} {}", LOG_TAG, e);
        }
    }

    private void sendSingleMessageAndPin(SendMessage sendMessage) {
        try {
            Message temp = execute(sendMessage);
            PinChatMessage pinChatMessage = new PinChatMessage();
            pinChatMessage.setChatId(temp.getChatId());
            pinChatMessage.setMessageId(temp.getMessageId());
            execute(pinChatMessage);
        } catch (TelegramApiException e) {
            log.error("{} {}", LOG_TAG, e);
        }
    }

    private void sendSingleMessageAndPin(Long chatId, String messageStr) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.enableMarkdown(false);
            sendMessage.setChatId(chatId);
            sendMessage.setText(messageStr);

            Message temp = execute(sendMessage);
            PinChatMessage pinChatMessage = new PinChatMessage();
            pinChatMessage.setChatId(temp.getChatId());
            pinChatMessage.setMessageId(temp.getMessageId());
            execute(pinChatMessage);
        } catch (TelegramApiException e) {
            log.error("{} {}", LOG_TAG, e);
        }
    }


    private void sendSingleMessage(EditMessageText sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("{} {}", LOG_TAG, e);
        }
    }

    private void sendCurrentMch(Long chatId) {
        RobotsMch robotsMch = robotsMchService.getMch(chatId);
        //是否已绑定商户
        if (robotsMch != null) {
            if (StringUtils.isNotEmpty(robotsMch.getMchNo())) {
                StringBuffer stringBuffer = new StringBuffer();
                JSONArray jsonArray = JSONArray.parseArray(robotsMch.getMchNo());
                stringBuffer.append("当前群绑定的商户为: " + System.lineSeparator());
                for (int i = 0; i < jsonArray.size(); i++) {
                    MchInfo mchInfo = mchInfoService.queryMchInfo(jsonArray.getString(i));
                    if (mchInfo != null) {
                        stringBuffer.append("[" + mchInfo.getMchNo() + "] " + mchInfo.getMchName() + System.lineSeparator());
                    }
                }
                sendSingleMessage(chatId, stringBuffer.toString());
            } else {
                sendSingleMessage(chatId, "当前群未绑定商户!");
            }
        } else {
            sendSingleMessage(chatId, "当前群未绑定商户！");
        }
    }

    protected Message sendQueryOrderMessage(Long chatId, Message sourceMessage, String addOn) {
        try {
            if (sourceMessage.hasPhoto()) {
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setCaption(addOn);
                sendPhoto.setChatId(chatId);
                sendPhoto.setPhoto(new InputFile(sourceMessage.getPhoto().get(0).getFileId()));
                return execute(sendPhoto);

//                CopyMessage copyMessage = new CopyMessage(chatId + "", sourceMessage.getChatId() + "", sourceMessage.getMessageId());
//                copyMessage.setCaption(addOn);
//                MessageId messageId = execute(copyMessage);
//
//                Message message = new Message();
//                message.setMessageId(messageId.getMessageId().intValue());
//                Chat chat = new Chat();
//                chat.setId(chatId);
//                message.setChat(chat);
//                return message;
            } else if (sourceMessage.hasVideo()) {
                SendVideo sendVideo = new SendVideo();
                sendVideo.setChatId(chatId);
                sendVideo.setCaption(addOn);
                sendVideo.setVideo(new InputFile(sourceMessage.getVideo().getFileId()));
                return execute(sendVideo);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    protected Message sendSingleMessage(Long chatId, String messageStr) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(false);
        sendMessage.setChatId(chatId);
        sendMessage.setText(messageStr);
        sendMessage.setParseMode(ParseMode.HTML);
        return sendSingleMessage(sendMessage);
    }

    protected Message sendSinglePinMessage(Long chatId, String messageStr) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(false);
        sendMessage.setChatId(chatId);
        sendMessage.setText(messageStr);
        return sendSingleMessage(sendMessage);
    }

    private void sendUSDT(Long chatId) {
        String url = "https://www.okx.com/v3/c2c/tradingOrders/books?t=" + System.currentTimeMillis() + "&quoteCurrency=cny&baseCurrency=usdt&side=buy&paymentMethod=all&userType=all&showTrade=false&showFollow=false&showAlreadyTraded=false&isAbleFilter=false&receivingAds=false&urlId=0";
        //https://www.okx.com/v3/c2c/tradingOrders/books?t=1693229098440&quoteCurrency=cny&baseCurrency=usdt&side=buy&paymentMethod=all&userType=all&showTrade=false&showFollow=false&showAlreadyTraded=false&isAbleFilter=false&receivingAds=false&urlId=0
        String raw = HttpUtil.get(url);
        JSONObject result = JSONObject.parseObject(raw);
        if (result.getString("code").equals("0")) {
            JSONArray buys = result.getJSONObject("data").getJSONArray("buy");
            String price = buys.getJSONObject(0).getString("price");
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("今日汇率 USDT-CNY：<b>" + price + "</b>" + System.lineSeparator());
            for (int i = 0; i < buys.size(); i++) {
                if (i < 4) {
                    stringBuffer.append(" [" + buys.getJSONObject(i).getString("nickName") + "] " + buys.getJSONObject(i).getString("price") + System.lineSeparator());
                } else {
                    break;
                }
            }
            sendSingleMessage(chatId, stringBuffer.toString());
        } else {
            sendSingleMessage(chatId, "网络异常，请稍后再试");
            log.error(raw);
        }
    }


    private boolean checkBlindPassage(Long chatId, String userName) {
        if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
            //是否为其他群
            RobotsMch robotsMchAdmin = robotsMchService.getManageMch();
            if (robotsMchAdmin != null && robotsMchAdmin.getChatId().longValue() == chatId.longValue()) {
                sendSingleMessage(chatId, "当前群已绑定四方管理群,不支持绑定通道");
                return false;
            }
            RobotsMch robotsMch = robotsMchService.getMch(chatId);
            if (checkIsMchChat(robotsMch)) {
                sendSingleMessage(chatId, "当前群已绑定商户,不支持绑定通道");
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 发送当前群绑定的通道列表
     *
     * @param chatId
     */
    private void SendPassageList(Long chatId) {
        Map<Long, PayPassage> payPassageMap = payPassageService.getPayPassageMap();
        //发送当前绑定列表
        List<RobotsPassage> robotsPassageList = robotsPassageService.list(RobotsPassage.gw().eq(RobotsPassage::getChatId, chatId));
        if (robotsPassageList.size() > 0) {
            List<PayPassage> passageList = getPassageInfoList(robotsPassageList);
            //根据名字排序
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("当前群已绑定通道列表：" + System.lineSeparator());
            for (int i = 0; i < passageList.size(); i++) {
                stringBuffer.append("通道：[" + passageList.get(i).getPayPassageId() + "] " + passageList.get(i).getPayPassageName() + System.lineSeparator());
            }
            sendSingleMessage(chatId, stringBuffer.toString());
        } else {
            sendSingleMessage(chatId, "当前群没有已绑定的通道!");
        }
    }

    private void sendMchProduct(List<StatisticsMchProduct> list, Long chatId, String dayTitle) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(dayTitle + "跑量明细：" + System.lineSeparator());
        stringBuffer.append("===================================" + System.lineSeparator());
        for (int i = 0; i < list.size(); i++) {
            stringBuffer.append("[" + list.get(i).getProductId() + "] " + list.get(i).getExt().getString("productName") + "     " + AmountUtil.convertCent2Dollar(list.get(i).getTotalSuccessAmount()) + System.lineSeparator());
        }
        sendSingleMessage(chatId, stringBuffer.toString());
    }

    private void sendDayStat(StatisticsMch statisticsMch, Long chatId, String title) {

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        float rate = (statisticsMch.getOrderSuccessCount().floatValue() / statisticsMch.getTotalOrderCount().floatValue()) * 100;
        String rateStr = decimalFormat.format(rate);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(title + DateUtil.format(statisticsMch.getStatisticsDate(), "yyyy-MM-dd") + " 汇总:" + System.lineSeparator());
        stringBuffer.append("成交金额: " + AmountUtil.convertCent2Dollar(statisticsMch.getTotalSuccessAmount()) + System.lineSeparator());
        stringBuffer.append("成交订单数: " + statisticsMch.getOrderSuccessCount() + System.lineSeparator());
        stringBuffer.append("总订单数: " + statisticsMch.getTotalOrderCount() + System.lineSeparator());
        stringBuffer.append("成功率: " + rateStr + "%");
        sendSingleMessage(chatId, stringBuffer.toString());
    }

    private void sendBillStat(Long chatId, Date date, Integer messageId, String dayTitle) {
        //默认发送今日统计
        StringBuffer stringBuffer = new StringBuffer();
        List<RobotsMchRecords> dayList = getDayStatByDate(chatId, date);
        List<RobotsMchRecords> totalList = getTotalStatByDate(chatId, date);

        //没有就添加记账群记录
        RobotsMch robotsMch = robotsMchService.getById(chatId);
        if (robotsMch == null) {
            robotsMch = new RobotsMch();
            robotsMch.setChatId(chatId);
            robotsMch.setMchNo("");
            robotsMch.setBalance(0L);
            robotsMchService.save(robotsMch);
        }
        Long totalAmount = robotsMch.getBalance();
        Long dayAmount = 0L;
        StringBuffer dayStatStr = new StringBuffer();
        for (int i = 0; i < dayList.size(); i++) {
            if (dayList.get(i).getState() == CS.YES) {
                dayAmount += dayList.get(i).getAmount();
                dayStatStr.append(DateUtil.format(dayList.get(i).getCreatedAt(), "HH:mm:ss") + "  <b>" + AmountUtil.convertCent2Dollar(dayList.get(i).getAmount()) + "</b>  " + dayList.get(i).getUserName() + System.lineSeparator());
            } else {
                dayStatStr.append("<s>" + DateUtil.format(dayList.get(i).getCreatedAt(), "HH:mm:ss") + "  " + AmountUtil.convertCent2Dollar(dayList.get(i).getAmount()) + "  " + dayList.get(i).getUserName() + "</s>  " + dayList.get(i).getRemark() + System.lineSeparator());
            }
        }

        StringBuffer totalStatStr = new StringBuffer();
        for (int i = 0; i < totalList.size(); i++) {
            if (totalList.get(i).getState() == CS.YES) {
                totalStatStr.append(DateUtil.format(totalList.get(i).getCreatedAt(), "HH:mm:ss") + "  <b>" + AmountUtil.convertCent2Dollar(totalList.get(i).getAmount()) + "</b>  " + totalList.get(i).getUserName() + System.lineSeparator());
            } else {
                totalStatStr.append("<s>" + DateUtil.format(totalList.get(i).getCreatedAt(), "HH:mm:ss") + "  " + AmountUtil.convertCent2Dollar(totalList.get(i).getAmount()) + "  " + totalList.get(i).getUserName() + "</s>  " + totalList.get(i).getRemark() + System.lineSeparator());
            }
        }
        stringBuffer.append("<b>" + DateUtil.format(date, "yyyy-MM-dd") + "</b>" + System.lineSeparator());
        stringBuffer.append("当日下发总额: <b>" + AmountUtil.convertCent2Dollar(dayAmount) + "</b>" + System.lineSeparator());
        stringBuffer.append("记账总额: <b>" + AmountUtil.convertCent2Dollar(totalAmount) + "</b>" + System.lineSeparator());
        stringBuffer.append("================" + System.lineSeparator());
        stringBuffer.append(dayTitle + "下发: (" + dayList.size() + "笔)" + System.lineSeparator());
        stringBuffer.append(dayStatStr);
        stringBuffer.append("================" + System.lineSeparator());
        stringBuffer.append(dayTitle + "记账: (" + totalList.size() + "笔)" + System.lineSeparator());
        stringBuffer.append(totalStatStr);

        sendReplyMessageWithMenu(robotsMch.getChatId(), messageId, stringBuffer.toString());
    }

    private List<RobotsMchRecords> getDayStatByDate(Long chatId, Date date) {
        LambdaQueryWrapper<RobotsMchRecords> wrapper = RobotsMchRecords.gw();

        wrapper.eq(RobotsMchRecords::getChatId, chatId);
        wrapper.eq(RobotsMchRecords::getType, RobotsMchRecords.DAY_TYPE);

        //一天的开始，结果：2017-03-01 00:00:00
        Date beginOfDay = DateUtil.beginOfDay(date);

        //一天的结束，结果：2017-03-01 23:59:59
        Date endOfDay = DateUtil.endOfDay(date);

        wrapper.ge(RobotsMchRecords::getCreatedAt, DateUtil.format(beginOfDay, "yyyy-MM-dd HH:mm:ss"));
        wrapper.le(RobotsMchRecords::getCreatedAt, DateUtil.format(endOfDay, "yyyy-MM-dd HH:mm:ss"));
        wrapper.orderByAsc(RobotsMchRecords::getCreatedAt);

        return robotsMchRecordsService.list(wrapper);

    }

    private List<RobotsMchRecords> getTotalStatByDate(Long chatId, Date date) {
        LambdaQueryWrapper<RobotsMchRecords> wrapper = RobotsMchRecords.gw();

        wrapper.eq(RobotsMchRecords::getChatId, chatId);
        wrapper.eq(RobotsMchRecords::getType, RobotsMchRecords.TOTAL_TYPE);

        //一天的开始，结果：2017-03-01 00:00:00
        Date beginOfDay = DateUtil.beginOfDay(date);

        //一天的结束，结果：2017-03-01 23:59:59
        Date endOfDay = DateUtil.endOfDay(date);

        wrapper.ge(RobotsMchRecords::getCreatedAt, DateUtil.format(beginOfDay, "yyyy-MM-dd HH:mm:ss"));
        wrapper.le(RobotsMchRecords::getCreatedAt, DateUtil.format(endOfDay, "yyyy-MM-dd HH:mm:ss"));
        wrapper.orderByAsc(RobotsMchRecords::getCreatedAt);
        return robotsMchRecordsService.list(wrapper);
    }

    /**
     * 判断是否商户群
     *
     * @param robotsMch
     * @return
     */
    private boolean checkIsMchChat(RobotsMch robotsMch) {
        return robotsMch != null && !robotsMch.getMchNo().equals(CS.ROBOTS_MGR_MCH);
    }

    /**
     * 判断是否通道群
     *
     * @param chatId
     * @return
     */
    private boolean checkIsPassageChat(Long chatId) {
        return robotsPassageService.count(RobotsPassage.gw().eq(RobotsPassage::getChatId, chatId)) != 0;
    }

    /**
     * 点击按钮反馈
     *
     * @param update
     */
    private void handleCallbackQuery(Update update) {
        // 當用戶點擊按鈕後，處理回調查詢
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();

        switch (data) {
            case TODAY_RECORD://今日账单
                Date today = DateUtil.parse(DateUtil.today());
                sendBillStat(chatId, today, callbackQuery.getMessage().getMessageId(), "今日");
                break;
            case YESTERDAY_RECORD://昨日账单
                sendBillStat(chatId, DateUtil.yesterday(), callbackQuery.getMessage().getMessageId(), "昨日");
                break;
        }

        try {
            // 用戶點擊按鈕後，回覆一個空的回調查詢，以清除按鈕的選中狀態
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackQuery.getId());
            execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化
     *
     * @throws Exception
     */
    @PostConstruct
    public void Init() throws Exception {
        String botUserName = getBotUsername();
        String botToken = getBotToken();
        if (StringUtils.isNotEmpty(botUserName) && StringUtils.isNotEmpty(botToken)) {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
            log.info("==========================================");
            log.info("========RobotsService Initializing========");
            log.info("==========================================");
        }
    }

    @Override
    public void onRegister() {
        // Define the commands to set
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("help", "亚洲科技机器人帮助说明"));
        commands.add(new BotCommand("usdt", "今日U价格查询"));
        // Create the SetMyCommands request
        SetMyCommands setMyCommands = new SetMyCommands();
        setMyCommands.setCommands(commands);

//        RobotsMch robotsAdmin = robotsMchService.getManageMch();
//        if (robotsAdmin != null) {
//            sendSingleMessage(robotsAdmin.getChatId(), "四方机器人初始化成功");
//        }

        try {
            execute(setMyCommands);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getBotUsername() {
        // Return your bot's username
        return sysConfigService.getRobotsConfig().getRobotsUserName();
    }

    @Override
    public String getBotToken() {
        // Return your bot's token
        return sysConfigService.getRobotsConfig().getRobotsToken();
    }


    /**
     * 强制补单查询
     */
    @Scheduled(fixedRate = 60000) // 每60秒执行一次
    public void forceOrderCheck() {

        try {
            Date nowTime = new Date();
            Date offsetDate = DateUtil.offsetMinute(nowTime, -1);

            //发警报
            RobotsMch robotsMch = robotsMchService.getManageMch();
            if (robotsMch != null) {
                int warnCount = Integer.parseInt(sysConfigService.getRobotsConfig().getForceOrderWarnConfig());
                if (warnCount > 0) {
                    LambdaQueryWrapper<PayOrder> lambdaQueryWrapper = PayOrder.gw().eq(PayOrder::getForceChangeState, CS.YES).le(PayOrder::getSuccessTime, nowTime).ge(PayOrder::getSuccessTime, offsetDate);
                    List<PayOrder> list = payOrderService.list(lambdaQueryWrapper);
                    int count = list.size();
                    if (count >= warnCount) {
                        log.error("过去一分钟手动补单为[ " + count + " ]条，触发预警请检查❗");
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append("检测时间点：[ " + DateUtil.format(nowTime, "yyyy-MM-dd HH:mm:ss") + " ]" + System.lineSeparator());
                        for (int i = 0; i < count; i++) {
                            stringBuffer.append("强补-平台订单号：[ " + list.get(i).getPayOrderId() + " ] 操作员:" + list.get(i).getForceChangeLoginName() + System.lineSeparator());
                        }
                        stringBuffer.append("过去一分钟手动补单为[ " + count + " ]条，触发预警请检查❗" + System.lineSeparator());
                        sendSingleMessage(robotsMch.getChatId(), stringBuffer.toString());
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    /**
     * 异常订单查询
     */
    @Scheduled(fixedRate = 60000) // 每60秒执行一次
    public void errorOrderCheck() {
        Date nowTime = new Date();
        Date offsetDate = DateUtil.offsetMinute(nowTime, -1);
        try {
            //发警报
            RobotsMch robotsMch = robotsMchService.getManageMch();
            if (robotsMch != null) {
                int warnCount = Integer.parseInt(sysConfigService.getRobotsConfig().getErrorOrderWarnConfig());
                if (warnCount > 0) {
                    LambdaQueryWrapper<PayOrder> lambdaQueryWrapper = PayOrder.gw().eq(PayOrder::getState, PayOrder.STATE_ERROR).le(PayOrder::getCreatedAt, nowTime).ge(PayOrder::getCreatedAt, offsetDate);
                    List<PayOrder> list = payOrderService.list(lambdaQueryWrapper);
                    int count = list.size();

                    if (count >= warnCount) {
                        log.error("过去一分钟异常订单(出码失败)数为[ " + count + " ]条，触发预警请检查❗");
                        Map<Long, PayPassage> payPassageMap = payPassageService.getPayPassageMap();
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append("检测时间点：[ " + DateUtil.format(nowTime, "yyyy-MM-dd HH:mm:ss") + " ]" + System.lineSeparator());
                        for (int i = 0; i < count; i++) {
                            PayOrder payOrder = list.get(i);
                            stringBuffer.append("订单号：[ " + payOrder.getPayOrderId() + " ]--通道:<b>[" + payOrder.getPassageId() + "] " + payPassageMap.get(payOrder.getPassageId()).getPayPassageName() + "</b>" + System.lineSeparator());
                        }
                        stringBuffer.append("过去一分钟异常订单数为[ <b>" + count + "</b> ]条，触发预警请检查❗" + System.lineSeparator());
                        sendSingleMessage(robotsMch.getChatId(), stringBuffer.toString());
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 发送单条订单消息
     *
     * @param unionOrderId
     * @param message
     */
    public void sendSingleQuery(String unionOrderId, Message message) {
        if (StringUtils.isNotEmpty(unionOrderId)) {
            Long chatId = message.getChatId();
            RobotsMch robotsMch = robotsMchService.getMch(chatId);

            String regex = "^[a-zA-Z0-9]{6,}$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(unionOrderId);

            if (!matcher.matches()) {
                return;
            }
            //不是通道群
            if (robotsPassageService.count(RobotsPassage.gw().eq(RobotsPassage::getChatId, chatId)) > 0) {
                return;
            }
            //是否已绑定商户
            if (robotsMch != null) {
                String mchStr = robotsMch.getMchNo();
                if (StringUtils.isEmpty(mchStr)) {
                    return;
                }

                LambdaQueryWrapper<PayOrder> wrapper = PayOrder.gw();
                wrapper.and(wr -> {
                    wr.eq(PayOrder::getPayOrderId, unionOrderId).or().eq(PayOrder::getMchOrderNo, unionOrderId);
                });

                PayOrder payOrder = null;
                try {
                    payOrder = payOrderService.getOne(wrapper);
                } catch (Exception e) {
                    log.error("机器人查单异常:" + unionOrderId);
                    log.error(e.getMessage(), e);
                    sendSingleMessage(chatId, "未查询到该群所有绑定商户下，订单号为[" + unionOrderId + "]的记录");
                    return;
                }
                if (payOrder == null) {
                    sendSingleMessage(chatId, "未查询到该群所有绑定商户下，订单号为[" + unionOrderId + "]的记录");
                } else {
                    if (mchStr.indexOf(payOrder.getMchNo()) == -1) {
                        sendSingleMessage(chatId, "未查询到该群所有绑定商户下，订单号为[" + unionOrderId + "]的记录");
                        return;
                    }
                    //1、将这条消息转发到通道群,带图或视频
                    Long passageId = payOrder.getPassageId();
                    RobotsPassage robotsPassage = robotsPassageService.getById(passageId);
                    if (robotsPassage != null) {
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append("请核实订单是否支付。如支付，烦请补单。如有异常，请回复此条消息进行反馈！(两小时内回复有效):" + System.lineSeparator());
                        stringBuffer.append("支付订单号 [ " + payOrder.getPayOrderId() + " ] " + System.lineSeparator());
                        if (StringUtils.isNotEmpty(payOrder.getPassageOrderNo())) {
                            stringBuffer.append("通道订单号为 [ " + payOrder.getPassageOrderNo() + " ] " + System.lineSeparator());
                        }

                        Message messageTemp = sendQueryOrderMessage(robotsPassage.getChatId(), message, stringBuffer.toString());
                        if (messageTemp != null) {
                            //保持2小时缓存
                            RedisUtil.set(REDIS_MCH_SOURCE_SUFFIX + message.getMessageId(), messageTemp, 2, TimeUnit.HOURS);
                            StringBuffer stringBufferOrderInfo = new StringBuffer();
                            stringBufferOrderInfo.append("机器人补充信息：" + System.lineSeparator());
                            stringBufferOrderInfo.append("支付订单号 [ " + payOrder.getPayOrderId() + " ] " + System.lineSeparator());
                            stringBufferOrderInfo.append("商户订单号 [ <b>" + payOrder.getMchOrderNo() + "</b> ] " + System.lineSeparator());
                            message.setText(stringBufferOrderInfo.toString());
                            RedisUtil.set(REDIS_SOURCE_SUFFIX + messageTemp.getMessageId(), message, 2, TimeUnit.HOURS);
                            sendReplyMessage(chatId, message.getMessageId(), "订单已传达，请稍等!");
                        } else {
                            sendReplyMessage(chatId, message.getMessageId(), "该订单对应通道群已失效,请联系四方工作人员检查");
                        }
                    } else {
                        Map<Long, PayPassage> payPassageMap = payPassageService.getPayPassageMap();
                        PayPassage passage = payPassageMap.get(passageId);

                        //发送到管理群并提醒
                        RobotsMch robotsMchAdmin = robotsMchService.getManageMch();
                        if (robotsMchAdmin != null) {
                            StringBuffer stringBuffer = new StringBuffer();
                            stringBuffer.append("商户[" + robotsMch.getMchNo() + "] 查单:" + System.lineSeparator());
                            stringBuffer.append("商户订单号 [ " + payOrder.getMchOrderNo() + " ] " + System.lineSeparator());
                            stringBuffer.append("支付订单号 [ " + payOrder.getPayOrderId() + " ] " + System.lineSeparator());
                            stringBuffer.append("未检测到已绑定的通道群,请先绑定后再查单!" + System.lineSeparator());
                            stringBuffer.append("通道信息：[ " + passage.getPayPassageId() + " ] " + passage.getPayPassageName() + System.lineSeparator());
                            sendSingleMessage(robotsMchAdmin.getChatId(), stringBuffer.toString());
                        } else {
                            StringBuffer stringBuffer = new StringBuffer();
                            stringBuffer.append("商户[" + robotsMch.getMchNo() + "] 查单:" + System.lineSeparator());
                            stringBuffer.append("商户订单号 [ " + payOrder.getMchOrderNo() + " ] " + System.lineSeparator());
                            stringBuffer.append("支付订单号 [ " + payOrder.getPayOrderId() + " ] " + System.lineSeparator());
                            stringBuffer.append("未检测到已绑定的通道群,请先绑定后再查单!" + System.lineSeparator());
                            stringBuffer.append("该订单未查询到已绑定的通道群,请通知四方工作人员先绑定通道群!" + System.lineSeparator());
                            sendSingleMessage(chatId, stringBuffer.toString());
                        }
                    }
                }
            } else {
                sendSingleMessage(chatId, "未绑定商户,请先绑定商户");
            }
        }
    }

    /**
     * 查询通道实时信息
     *
     * @param robotsPassageList
     * @return
     */
    private List<PayPassage> getPassageInfoList(List<RobotsPassage> robotsPassageList) {
        List<PayPassage> passageList = new ArrayList<>();
        Map<Long, PayPassage> payPassageMap = payPassageService.getPayPassageMap();
        for (int i = 0; i < robotsPassageList.size(); i++) {
            Long passageId = robotsPassageList.get(i).getPassageId();
            if (payPassageMap.containsKey(passageId)) {
                passageList.add(payPassageMap.get(passageId));
            }
        }
        Collections.sort(passageList, (o1, o2) -> Collator.getInstance(Locale.CHINA).compare(o1.getPayPassageName(), o2.getPayPassageName()));
        return passageList;
    }

    /**
     * 通道配置修改检测
     */
    @Scheduled(fixedRate = 10000) // 每60秒执行一次
    public void passageConfigCheck() {

        String REDIS_SUFFIX = "Passage_Pay_Config";
        List<PayPassage> list = new ArrayList<>();
        Long cacheSize = RedisUtil.getQueueLength(REDIS_SUFFIX);
        if (cacheSize.intValue() == 0) {
            return;
        }
        //通道配置预警(0关闭，1打开)
        if (!sysConfigService.getRobotsConfig().getPassageConfig().trim().equals("1")) {
            return;
        }

        int count = cacheSize.intValue();
        for (int index = 0; index < count; index++) {
            list.add(RedisUtil.removeFromQueue(REDIS_SUFFIX, PayPassage.class));
        }
        RobotsMch robotsMch = robotsMchService.getManageMch();
        if (robotsMch != null) {
            Date nowTime = new Date();

            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("检测时间点：[ " + DateUtil.format(nowTime, "yyyy-MM-dd HH:mm:ss") + " ]" + System.lineSeparator());
            for (int i = 0; i < count; i++) {
                stringBuffer.append("通道三方商户配置被修改：[" + list.get(i).getPayPassageId() + "] " + list.get(i).getPayPassageName() + System.lineSeparator());
            }
            stringBuffer.append("如非工作人员操作请注意风险❗" + System.lineSeparator());
            sendSingleMessage(robotsMch.getChatId(), stringBuffer.toString());
        }
    }

    //    @Async
    @Scheduled(cron = "0 0 04 * * ?") // 每天凌晨四点执行
    public void clearRecordCheck() {
        int dayOffset = -2;
        Date date = DateUtil.parse(DateUtil.today());
        Date offsetDate = DateUtil.offsetDay(date, dayOffset);
        //过期订单
        robotsMchRecordsService.ClearRecord(offsetDate, 500);
    }
}