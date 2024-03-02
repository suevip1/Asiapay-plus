package com.jeequan.jeepay.com.jeequan.service;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeequan.jeepay.components.mq.model.RobotListenPayOrderSuccessMQ;
import com.jeequan.jeepay.components.mq.model.RobotWarnMQ;
import com.jeequan.jeepay.components.mq.model.RobotWarnNotify;
import com.jeequan.jeepay.components.mq.model.RobotWarnPassage;
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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class RobotsService extends TelegramLongPollingBot implements RobotListenPayOrderSuccessMQ.IMQReceiver, RobotWarnMQ.IMQReceiver {

    private static final String LOG_TAG = "ROBOTS_ERROR";

    /**
     * 机器人发到通道群的转发的消息,key是通道群转发的消息ID，值是存储的商户群原消息
     */
    private static final String REDIS_SOURCE_SUFFIX = "REDIS_SOURCE_";

    /**
     * 商户群查单-原消息  key messageId,value 通道群的转发message
     */
    private static final String REDIS_MCH_SOURCE_SUFFIX = "REDIS_MCH_SOURCE_";

    /**
     * （用于处理查单过程中变成成功的订单）商户已经识别成功的查单消息,通过订单号存储，方便识别  value message
     */
    private static final String REDIS_MCH_SOURCE_ORDER_SUFFIX = "REDIS_MCH_SOURCE_ORDER_";

    /**
     * （用于催单命令）存储已转发的查单信息  订单号 商户订单号 value 转发到通道群的message ID
     */
    private static final String REDIS_ORDER_FORWARD_SUFFIX = "REDIS_ORDER_FORWARD_";

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

    private static final String QUERY_PRODUCT = "产品费率";

    private static final String QUERY_PRODUCT_SINGLE = "产品费率\\s+([\\d,]+)";


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
    private static final String ADD_RECORD_TOTAL = "[+-]\\d+(\\.\\d+)?";
    private static final String REVOKE_RECORD_TOTAL = "撤销下发";
    private static final String CLEAR_RECORD_TOTAL = "清除下发";
    /**
     * 记账
     */
    private static final String ADD_RECORD = "记账 [+-]?\\d+(\\.\\d+)?";
    private static final String REVOKE_RECORD = "撤销记账";
    private static final String CLEAR_RECORD = "清除记账";
    private static final String TODAY_RECORD = "今日账单";
    private static final String YESTERDAY_RECORD = "昨日账单";

    private static final String TODAY_SETTLE = "今日结算";
    private static final String YESTERDAY_SETTLE = "昨日结算";


    private static final String ROBOT_QUIT = "机器人退群";

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

    private static final String DELETE_MSG = "删除";

    private static final String ORDER_REGEX = "^[a-zA-Z0-9-_\\$%]{7,}$";

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

    @Autowired
    private MchHistoryService mchHistoryService;

    @Autowired
    private StatisticsMchService statisticsMchService;


    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()) {
            int timestamp = update.getMessage().getDate();
            int now = (int) (System.currentTimeMillis() / 1000);
            if (Math.abs(now - timestamp) > 180) {
                log.error("消息已超过三分钟,消息不处理,聊天原文: -- {}", update.getMessage().getText());
                return;
            }
            //检测权限，命令
            if (update.getMessage().isCommand() && !update.getMessage().getFrom().getIsBot()) {
                handleCommand(update);
            } else if (update.getMessage().isGroupMessage() || update.getMessage().isSuperGroupMessage()) {
                handleGroupCommand(update);
            } else if (update.getMessage().isUserMessage()) {
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
                if (text.trim().equals(SEND_ALL)) {
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
                if (text.trim().equals(SEND_ALL_MCH)) {
                    List<RobotsMch> list = findNonMchDuplicateChatIds();
                    for (int i = 0; i < list.size(); i++) {
                        sendForward(list.get(i).getChatId(), update.getMessage().getReplyToMessage());
                    }
                    return;
                }
                //群发通道
                if (text.trim().equals(SEND_ALL_PASSAGE)) {
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
                if (replyText.trim().equals(SEND_ALL)) {
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
                if (replyText.trim().equals(SEND_ALL_MCH)) {
                    List<RobotsMch> list = findNonMchDuplicateChatIds();
                    for (int i = 0; i < list.size(); i++) {
                        sendForward(list.get(i).getChatId(), update.getMessage());
                    }
                    return;
                }
                //群发通道
                if (replyText.trim().equals(SEND_ALL_PASSAGE)) {
                    List<RobotsPassage> list = findNonPassageDuplicateChatIds();
                    for (int i = 0; i < list.size(); i++) {
                        sendForward(list.get(i).getChatId(), update.getMessage());
                    }
                    return;
                }
            }
        }

        if (update.getMessage().hasText() && update.getMessage().isReply()) {
            String text = update.getMessage().getText().trim();
            String userName = update.getMessage().getFrom().getUserName();

            String textReply = update.getMessage().getReplyToMessage().getText().trim();

            if ((textReply.equals(TODAY_SETTLE) || textReply.equals(YESTERDAY_SETTLE)) && text.equals("确认执行")) {
                if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                    //全部商户
                    List<RobotsMch> list = findNonMchDuplicateChatIds();

                    Date date = new Date();
                    Date today = DateUtil.parse(DateUtil.today());
                    if (textReply.equals(TODAY_SETTLE)) {
                        date = today;
                    } else {
                        date = DateUtil.offsetDay(today, -1);
                    }

                    for (int i = 0; i < list.size(); i++) {
                        RobotsMch robotsMch = list.get(i);
                        sendSettleInfo(robotsMch, date);
                    }
                }
                return;
            }
            if (text.equals(TODAY_SETTLE) || text.equals(YESTERDAY_SETTLE)) {
                if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                    sendReplyMessage(update.getMessage().getChatId(), update.getMessage().getMessageId(), "请回复你发送的这条消息，发送[确认执行]发送商户结算");
                }
            }
            return;
        }

        if (update.getMessage().hasText() && !update.getMessage().isReply()) {
            String text = update.getMessage().getText().trim();
            String userName = update.getMessage().getFrom().getUserName();

            if (text.equals(TODAY_SETTLE) || text.equals(YESTERDAY_SETTLE)) {
                if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                    sendReplyMessage(update.getMessage().getChatId(), update.getMessage().getMessageId(), "请回复你发送的这条消息，发送[确认执行]发送商户结算");
                }
            }
            return;
        }

    }

    /**
     * 查找所有通道群
     *
     * @return
     */
    public List<RobotsPassage> findNonPassageDuplicateChatIds() {
        QueryWrapper<RobotsPassage> wrapper = new QueryWrapper<>();
        // 使用GROUP BY查询chat_id并且HAVING COUNT(chat_id) = 1来找到不重复的chat_id
        wrapper.select("chat_id").groupBy("chat_id").having("COUNT(chat_id) != 0");
        return robotsPassageService.list(wrapper);
    }

    /**
     * 查找所有绑定了商户的群
     *
     * @return
     */
    public List<RobotsMch> findNonMchDuplicateChatIds() {
        List<RobotsMch> temp = robotsMchService.list(RobotsMch.gw().ne(RobotsMch::getMchNo, CS.ROBOTS_MGR_MCH));
        List<RobotsMch> result = new ArrayList<>();
        for (int i = 0; i < temp.size(); i++) {
            if (StringUtils.isNotEmpty(temp.get(i).getMchNo().trim())) {
                result.add(temp.get(i));
            }
        }
        return result;
    }

    /**
     * /开头的命令
     *
     * @param update
     */
    private void handleCommand(Update update) {
        //发送机器人使用说明
        if (update.getMessage().getText().startsWith("/help")) {
            StringBuilder stringBuffer = new StringBuilder();
            stringBuffer.append("欢迎使用亚洲科技四方系统机器人，以下是机器人使用说明:").append(System.lineSeparator());
            stringBuffer.append("======================================").append(System.lineSeparator());
            stringBuffer.append("<b>记账功能</b>:（记账数据每个群分开，无需绑定商户，敏感操作需管理员以及操作员权限）").append(System.lineSeparator());
            stringBuffer.append("+xxx -- (下发金额累加不清空)添加下发金额，xxx为金额，例如：+1000").append(System.lineSeparator());
            stringBuffer.append("-xxx -- (下发金额累加不清空)扣减下发金额，xxx为金额，例如：-1000").append(System.lineSeparator());
            stringBuffer.append("撤销下发 -- 删除最后一笔下发记录").append(System.lineSeparator());
            stringBuffer.append("清除下发 -- 删除当天的全部下发记录").append(System.lineSeparator());
            stringBuffer.append("记账 xxx -- (记账仅保留当日记录)添加记账金额，xxx为金额，例如：记账 1000").append(System.lineSeparator());
            stringBuffer.append("记账 -xxx -- (记账仅保留当日记录)扣减记账金额，xxx为金额，例如：记账 -1000").append(System.lineSeparator());
            stringBuffer.append("撤销记账 -- 删除最后一笔记账记录").append(System.lineSeparator());
            stringBuffer.append("清除记账 -- 删除当天的全部记账记录").append(System.lineSeparator());
            stringBuffer.append("今日账单 -- 查看今日完整账单").append(System.lineSeparator());
            stringBuffer.append("昨日账单 -- 查看昨日完整账单").append(System.lineSeparator());
            stringBuffer.append("======================================").append(System.lineSeparator());
            stringBuffer.append("<b>商户功能</b>:（需先绑定商户才能使用）").append(System.lineSeparator());
            stringBuffer.append("查询余额 -- 查询商户或通道的余额").append(System.lineSeparator());
            stringBuffer.append("产品费率 -- 查询商户已开通产品实时费率").append(System.lineSeparator());
            stringBuffer.append("产品费率 ID-- 查询商户已开通单个产品实时费率").append(System.lineSeparator());
            stringBuffer.append("XXXXXXX -- 直接发送平台订单号或商户订单号<b>并带图</b>进行<b>查单</b>操作").append(System.lineSeparator());
            stringBuffer.append("XXXXXXX 备注XXX-- 直接发送平台订单号或商户订单号<b>并带图</b>进行<b>查单</b>操作").append(System.lineSeparator());
            stringBuffer.append("XXXXXXX 换行 XXXXXXX -- 多单查询每个单号间请换行<b>并带图</b>进行<b>查单</b>操作").append(System.lineSeparator());
            stringBuffer.append("zz -- 催单：回复商户发单消息进行转发，例如：zz 加急加急").append(System.lineSeparator());
            stringBuffer.append("zz 订单号/商户订单号 xxxx-- 催单：直接发送订单号以及备注信息进行催单，例如：zz P1234567890 加急加急").append(System.lineSeparator());
            stringBuffer.append("今日跑量 -- 查看今日商户或通道完整跑量统计").append(System.lineSeparator());
            stringBuffer.append("昨日跑量 -- 查看昨日商户或通道完整跑量统计").append(System.lineSeparator());
            stringBuffer.append("今日结算 -- 查看今日结算信息").append(System.lineSeparator());
            stringBuffer.append("昨日结算 -- 查看昨日结算信息").append(System.lineSeparator());
            stringBuffer.append("======================================").append(System.lineSeparator());
            stringBuffer.append("<b>通用功能</b>:").append(System.lineSeparator());
            stringBuffer.append("uj -- 查询今日U价(C2C全部支付方式)").append(System.lineSeparator());
            stringBuffer.append("ub -- 查询今日U价(C2C银行卡)").append(System.lineSeparator());
            stringBuffer.append("ua -- 查询今日U价(C2C支付宝)").append(System.lineSeparator());
            stringBuffer.append("uw -- 查询今日U价(C2C微信支付)").append(System.lineSeparator());
            stringBuffer.append("kj -- 查询今日U价(大宗全部支付方式)").append(System.lineSeparator());
            stringBuffer.append("kb -- 查询今日U价(大宗银行卡)").append(System.lineSeparator());
            stringBuffer.append("ka -- 查询今日U价(大宗支付宝)").append(System.lineSeparator());
            stringBuffer.append("kw -- 查询今日U价(大宗微信支付)").append(System.lineSeparator());
            stringBuffer.append("计算 -- 进行四则运算，例如：计算 (1+2)*3").append(System.lineSeparator());

            sendSingleMessage(update.getMessage().getChatId(), stringBuffer.toString());
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


        if (message.isReply() && messageReply != null) {
            //检测是否查单转发信息

            if (message.hasText() && message.getText().trim().equals(DELETE_MSG)) {
                //是否admin
                if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                    //引用的是机器人自己的消息
                    if (messageReply.getFrom().getUserName().equals(getBotUsername())) {
                        //删除本条消息以及引用的消息
                        sendDeleteMessage(chatId, message.getMessageId());
                        //删除本条消息以及引用的消息
                        sendDeleteMessage(chatId, messageReply.getMessageId());
                        log.info("收到删除命令 原文: " + messageReply.getText());
                    }
                }
                return;
            }

            //REDIS_SOURCE_SUFFIX  存储的是 转发到 通道群的suffix+id 商户群 message
            Message messageSource = RedisUtil.getObject(REDIS_SOURCE_SUFFIX + messageReply.getChatId() + messageReply.getMessageId(), Message.class);
            //有缓存
            if (messageSource != null) {
                //是本机器人发的消息
                if (messageReply.getFrom().getUserName().equals(getBotUsername())) {
                    //是通道群发的消息
                    List<RobotsPassage> robotsPassageList = robotsPassageService.list(RobotsPassage.gw().eq(RobotsPassage::getChatId, chatId));
                    if (!robotsPassageList.isEmpty()) {
                        sendQueryMessage(message, messageSource);
                    }
                }
                return;
            }

            //检测是否催单信息  回复的形式
            //REDIS_MCH_SOURCE_SUFFIX 存储的是 商户群suffix+id 通道群message
            Message messageForwardSource = RedisUtil.getObject(REDIS_MCH_SOURCE_SUFFIX + messageReply.getChatId() + messageReply.getMessageId(), Message.class);
            if (messageForwardSource != null && message.hasText()) {
                if (message.getText().indexOf("zz ") == 0) {
                    if (messageForwardSource.getFrom().getUserName().equals(getBotUsername())) {
                        String queryStr = message.getText().replaceAll("zz ", "");
                        sendReplyMessage(messageForwardSource.getChatId(), messageForwardSource.getMessageId(), queryStr);
                    }
                }
                return;
            }
        }
        //包含图或视频、且不是回复信息
        if ((message.hasPhoto() || message.hasVideo()) && !message.isReply()) {
            String text = message.getCaption();
            if (StringUtils.isNotEmpty(text)) {
                if (text.contains("\n")) {
                    String[] texts = text.trim().split("\n");
                    for (int i = 0; i < texts.length; i++) {
                        sendSingleQuery(texts[i].trim(), message);
                    }
                } else {
                    sendSingleQuery(text.trim(), message);
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

        if (text.indexOf("zz ") == 0) {
            String tempStr = text.replaceAll("zz ", "").trim();

            //分割备注
            int firstSpaceIndex = tempStr.indexOf(" ");
            String unionOrderId = "";
            String remark = "";
            // 截取第一个空格之后的所有内容
            if (firstSpaceIndex != -1) {
                remark = tempStr.substring(firstSpaceIndex + 1).trim();
                unionOrderId = tempStr.substring(0, firstSpaceIndex).trim();
            } else {
                //没有备注
                unionOrderId = tempStr.trim();
            }

            //看是否催单信息 （直接发送订单号催单的）
            Pattern patternReminders = Pattern.compile(ORDER_REGEX);
            Matcher matcherReminders = patternReminders.matcher(unionOrderId);
            if (matcherReminders.matches()) {
                Message messagePaySource = RedisUtil.getObject(REDIS_ORDER_FORWARD_SUFFIX + message.getChatId() + unionOrderId, Message.class);
                if (messagePaySource != null) {
                    if (messagePaySource.getFrom().getUserName().equals(getBotUsername())) {
                        if (!StringUtils.isNotEmpty(remark)) {
                            remark = "<b>烦请加急处理!</b>";
                        }
                        sendReplyMessage(messagePaySource.getChatId(), messagePaySource.getMessageId(), remark);
                    }
                    return;
                }
                return;
            }
            return;
        }

        //绑定管理群
        if (text.trim().equals(BLIND_MGR)) {
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
        if (text.trim().equals(ROBOT_QUIT)) {
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

        //查U价格

        //uj -- 查询市场USTD买入价，c2c全部支付方式;
        if (text.trim().equals("uj")) {
            sendUSDT(chatId, "all", "all", "C2C全部支付方式");
            return;
        }
        //ub -- 查询市场USTD买入价，c2c银行卡;
        if (text.trim().equals("ub")) {
            sendUSDT(chatId, "all", "bank", "C2C银行卡");
            return;
        }
        //ua 查询市场USTD买入价，c2c支付宝;
        if (text.trim().equals("ua")) {
            sendUSDT(chatId, "all", "aliPay", "C2C支付宝");
            return;
        }
        //uw -- 查询市场USTD买入价，c2c微信支付;
        if (text.trim().equals("uw")) {
            sendUSDT(chatId, "all", "wxPay", "C2C微信支付");
            return;
        }
        //kj -- 查询市场USTD买入价，大宗全部支付方式;
        if (text.trim().equals("kj")) {
            sendUSDT(chatId, "blockTrade", "all", "大宗全部支付方式");
            return;
        }
        //kb -- 查询市场USTD买入价，大宗银行卡;
        if (text.trim().equals("kb")) {
            sendUSDT(chatId, "blockTrade", "bank", "大宗银行卡");
            return;
        }
        //ka -- 查询市场USTD买入价，大宗支付宝;
        if (text.trim().equals("ka")) {
            sendUSDT(chatId, "blockTrade", "aliPay", "大宗支付宝");
            return;
        }
        //kw -- 查询市场USTD买入价，大宗微信支付;
        if (text.trim().equals("kw")) {
            sendUSDT(chatId, "blockTrade", "wxPay", "大宗微信支付");
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

                if (mchInfo == null && mchInfo.getState() != CS.HIDE) {
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
        if (text.trim().equals(LIST_OP)) {
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


        // 查产品费率
        if (text.trim().equals(QUERY_PRODUCT)) {
            RobotsMch robotsMch = robotsMchService.getMch(chatId);
            //是否已绑定商户或通道
            if (robotsMch != null && StringUtils.isNotEmpty(robotsMch.getMchNo())) {

                String mchNoStr = robotsMch.getMchNo();
                JSONArray jsonArray = JSONArray.parseArray(mchNoStr);

                Map<Long, Product> productMap = statisticsService.productService.getProductMap();

                for (int i = 0; i < jsonArray.size(); i++) {
                    String mchNo = jsonArray.getString(i); // 假设你要根据某个键来查找
                    MchInfo mchInfo = mchInfoService.getById(mchNo);
                    String mchInfoStr = "[" + mchInfo.getMchNo() + "] <b>" + mchInfo.getMchName() + "</b>";

                    List<MchProduct> records = statisticsService.mchProductService.list(MchProduct.gw().select(MchProduct::getMchNo, MchProduct::getCreatedAt, MchProduct::getMchRate, MchProduct::getProductId).eq(MchProduct::getMchNo, mchNo).eq(MchProduct::getState, CS.YES));

                    if (records.isEmpty()) {
                        sendSingleMessage(chatId, mchInfoStr + " 没有已绑定的产品记录");
                    } else {
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append(mchInfoStr + " 产品列表:" + System.lineSeparator());
                        for (int x = 0; x < records.size(); x++) {
                            MchProduct record = records.get(x);
                            String productInfo = "[" + record.getProductId() + "] " + productMap.get(record.getProductId()).getProductName();
                            stringBuffer.append(productInfo + "   " + GetRateStr(record.getMchRate()) + System.lineSeparator());
                        }
                        sendSingleMessage(chatId, stringBuffer.toString());
                    }
                }
                return;
            }
            return;
        }

        //查单个产品费率
        Pattern patternProductRate = Pattern.compile(QUERY_PRODUCT_SINGLE);
        Matcher matcherProductRate = patternProductRate.matcher(text);
        if (matcherProductRate.matches()) {
            String productIdStr = text.substring(5);
            try {
                RobotsMch robotsMch = robotsMchService.getMch(chatId);
                if (robotsMch != null && StringUtils.isNotEmpty(robotsMch.getMchNo())) {
                    Long productId = 0L;
                    try {
                        productId = Long.parseLong(productIdStr);
                    } catch (Exception e) {
                        sendSingleMessage(chatId, "未查询到商户下ID [" + productIdStr + "] 产品");
                    }

                    String mchNoStr = robotsMch.getMchNo();
                    JSONArray jsonArray = JSONArray.parseArray(mchNoStr);

                    Map<Long, Product> productMap = statisticsService.productService.getProductMap();

                    for (int i = 0; i < jsonArray.size(); i++) {
                        String mchNo = jsonArray.getString(i); // 假设你要根据某个键来查找
                        MchInfo mchInfo = mchInfoService.getById(mchNo);
                        String mchInfoStr = "[" + mchInfo.getMchNo() + "] <b>" + mchInfo.getMchName() + "</b>";

                        List<MchProduct> records = statisticsService.mchProductService.list(MchProduct.gw().select(MchProduct::getMchNo, MchProduct::getMchRate, MchProduct::getProductId).eq(MchProduct::getMchNo, mchNo).eq(MchProduct::getState, CS.YES).eq(MchProduct::getProductId, productId));

                        if (!records.isEmpty()) {

                            if (records.size() == 1) {
                                StringBuilder stringBuffer = new StringBuilder();
                                stringBuffer.append(mchInfoStr).append(" 产品列表:").append(System.lineSeparator());

                                MchProduct record = records.get(0);
                                String productInfo = "[" + record.getProductId() + "] " + productMap.get(record.getProductId()).getProductName();
                                stringBuffer.append(productInfo).append("   ").append(GetRateStr(record.getMchRate())).append(System.lineSeparator());

                                sendSingleMessage(chatId, stringBuffer.toString());
                            } else {
                                log.error("检查记录，产品-商户绑定费率重复" + mchInfoStr + " " + productId);
                            }
                        } else {
                            sendSingleMessage(chatId, mchInfoStr + " 没有[" + productId + "]的产品");
                        }
                    }
                    return;
                } else {
                    sendSingleMessage(chatId, "未绑定商户,请先绑定商户");
                }
            } catch (Exception e) {
                log.error("聊天原文:" + text);
                log.error(e.getMessage(), e);
                sendSingleMessage(chatId, "查询异常,请联系管理员");
            }
            return;
        }

        //今日跑量
        if (text.trim().equals(TODAY_BILL)) {
            RobotsMch robotsMch = robotsMchService.getMch(chatId);
            List<RobotsPassage> robotsPassageList = robotsPassageService.list(RobotsPassage.gw().eq(RobotsPassage::getChatId, chatId));
            //是否已绑定商户或通道
            if (robotsMch != null && StringUtils.isNotEmpty(robotsMch.getMchNo())) {
                String mchNoStr = robotsMch.getMchNo();
                JSONArray jsonArray = JSONArray.parseArray(mchNoStr);

                //查今日跑量
                Date today = DateUtil.parse(DateUtil.today());

                Long totalAmount = 0L;
                Long amount = 0L;
                Long totalCost = 0L;
                for (int i = 0; i < jsonArray.size(); i++) {
                    String mchNo = jsonArray.getString(i); // 假设你要根据某个键来查找
                    MchInfo mchInfo = mchInfoService.getById(mchNo);
                    String mchInfoStr = "[" + mchInfo.getMchNo() + "] <b>" + mchInfo.getMchName() + "</b>";

                    StatisticsMch todayStatisticsMch = statisticsService.QueryStatisticsMchByDate(mchNo, today);
                    List<StatisticsMchProduct> statisticsMchProductList = statisticsService.QueryStatMchProduct(mchNo, today);

                    if (todayStatisticsMch == null) {
                        sendSingleMessage(chatId, mchInfoStr + " 没有今日跑量记录");
                    } else {
                        StringBuffer sbTemp = sendMchProduct(statisticsMchProductList, chatId, mchInfoStr + " 今日");
                        sbTemp.append(System.lineSeparator());
                        sbTemp.append(sendDayStat(todayStatisticsMch, chatId, mchInfoStr + " "));
                        sendSingleMessage(chatId, sbTemp.toString());
                        totalAmount += todayStatisticsMch.getTotalSuccessAmount();
                        amount += (todayStatisticsMch.getTotalSuccessAmount() - todayStatisticsMch.getTotalMchCost());
                        totalCost += todayStatisticsMch.getTotalMchCost();
                    }
                }
                if (jsonArray.size() > 1) {
                    StringBuffer stringBuffer1 = new StringBuffer();
                    stringBuffer1.append("跑量汇总: <b>" + AmountUtil.convertCent2Dollar(totalAmount) + "</b>" + System.lineSeparator());
                    stringBuffer1.append("手续费: <b>" + AmountUtil.convertCent2Dollar(totalCost) + "</b>" + System.lineSeparator());
                    stringBuffer1.append("入账汇总: <b>" + AmountUtil.convertCent2Dollar(amount) + "</b>");
                    sendSingleMessage(chatId, stringBuffer1.toString());
                }
                return;
            }
            if (robotsPassageList.size() > 0) {
                Date today = DateUtil.parse(DateUtil.today());

                List<PayPassage> passageList = getPassageInfoList(robotsPassageList);

                StringBuffer stringBuffer = new StringBuffer();
                Long totalBalance = 0L;
                Long balance = 0L;
                Long totalCost = 0L;
                stringBuffer.append("统计日期：<b>" + DateUtil.today() + "</b>" + System.lineSeparator());
                stringBuffer.append("-----------------------------------------------" + System.lineSeparator());
                stringBuffer.append("通道|跑量金额|实时费率|入账金额" + System.lineSeparator());
                stringBuffer.append("-----------------------------------------------" + System.lineSeparator());
                for (int i = 0; i < passageList.size(); i++) {
                    PayPassage payPassage = passageList.get(i);
                    StatisticsPassage statisticsPassage = statisticsService.QueryStatisticsPassageByDate(payPassage.getPayPassageId(), today);
                    String passageInfoStr = "[" + payPassage.getPayPassageId() + "] <b>" + payPassage.getPayPassageName() + "</b>";
                    if (statisticsPassage == null) {
                        stringBuffer.append(passageInfoStr + " | " + GetRateStr(payPassage.getRate()) + " | " + "  没有今日跑量记录" + System.lineSeparator());
                    } else {
                        Long amount = statisticsPassage.getTotalSuccessAmount() - statisticsPassage.getTotalPassageCost();
                        stringBuffer.append(passageInfoStr + " | " + AmountUtil.convertCent2Dollar(statisticsPassage.getTotalSuccessAmount()) + " | " + GetRateStr(payPassage.getRate()) + " | " + AmountUtil.convertCent2Dollar(amount) + System.lineSeparator());
                        balance += (amount);
                        totalBalance += statisticsPassage.getTotalSuccessAmount();
                        totalCost += statisticsPassage.getTotalPassageCost();
                    }
                }
                stringBuffer.append("-----------------------------------------------" + System.lineSeparator());
                stringBuffer.append("跑量汇总：<b>" + AmountUtil.convertCent2Dollar(totalBalance) + "</b>" + System.lineSeparator());
                stringBuffer.append("手续费: <b>" + AmountUtil.convertCent2Dollar(totalCost) + "</b>" + System.lineSeparator());
                stringBuffer.append("入账汇总：<b>" + AmountUtil.convertCent2Dollar(balance) + "</b>" + System.lineSeparator());
                sendSingleMessage(chatId, stringBuffer.toString());
                return;
            }
            sendSingleMessage(chatId, "未绑定商户或通道");
            return;
        }

        //昨日跑量
        if (text.trim().equals(YESTERDAY_BILL)) {
            RobotsMch robotsMch = robotsMchService.getMch(chatId);
            List<RobotsPassage> robotsPassageList = robotsPassageService.list(RobotsPassage.gw().eq(RobotsPassage::getChatId, chatId));

            Date todayTemp = DateUtil.parse(DateUtil.today());
            DateTime yesterday = DateUtil.offsetDay(todayTemp, -1);

            //是否已绑定商户或通道
            if (robotsMch != null && StringUtils.isNotEmpty(robotsMch.getMchNo())) {
                String mchNoStr = robotsMch.getMchNo();
                JSONArray jsonArray = JSONArray.parseArray(mchNoStr);

                Long totalAmount = 0L;
                Long amount = 0L;
                Long totalCost = 0L;
                for (int i = 0; i < jsonArray.size(); i++) {
                    String mchNo = jsonArray.getString(i); // 假设你要根据某个键来查找
                    MchInfo mchInfo = mchInfoService.getById(mchNo);
                    String mchInfoStr = "[" + mchInfo.getMchNo() + "] <b>" + mchInfo.getMchName() + "</b>";

                    StatisticsMch todayStatisticsMch = statisticsService.QueryStatisticsMchByDate(mchNo, yesterday);
                    List<StatisticsMchProduct> statisticsMchProductList = statisticsService.QueryStatMchProduct(mchNo, yesterday);

                    if (todayStatisticsMch == null) {
                        sendSingleMessage(chatId, mchInfoStr + " 没有昨日跑量记录");
                    } else {
                        StringBuffer sbTemp = sendMchProduct(statisticsMchProductList, chatId, mchInfoStr + " 昨日");
                        sbTemp.append(System.lineSeparator());
                        sbTemp.append(sendDayStat(todayStatisticsMch, chatId, mchInfoStr + " "));
                        sendSingleMessage(chatId, sbTemp.toString());

                        totalAmount += todayStatisticsMch.getTotalSuccessAmount();
                        amount += (todayStatisticsMch.getTotalSuccessAmount() - todayStatisticsMch.getTotalMchCost());
                        totalCost += todayStatisticsMch.getTotalMchCost();
                    }
                }
                if (jsonArray.size() > 1) {
                    StringBuffer stringBuffer1 = new StringBuffer();
                    stringBuffer1.append("跑量汇总: <b>" + AmountUtil.convertCent2Dollar(totalAmount) + "</b>" + System.lineSeparator());
                    stringBuffer1.append("手续费: <b>" + AmountUtil.convertCent2Dollar(totalCost) + "</b>" + System.lineSeparator());
                    stringBuffer1.append("入账汇总: <b>" + AmountUtil.convertCent2Dollar(amount) + "</b>");
                    sendSingleMessage(chatId, stringBuffer1.toString());
                }
                return;
            }
            if (robotsPassageList.size() > 0) {
                List<PayPassage> passageList = getPassageInfoList(robotsPassageList);

                StringBuffer stringBuffer = new StringBuffer();

                Long totalBalance = 0L;
                Long balance = 0L;
                Long totalCost = 0L;
                stringBuffer.append("统计日期：<b>" + DateUtil.format(yesterday, "yyyy-MM-dd") + "</b>" + System.lineSeparator());
                stringBuffer.append("-----------------------------------------------" + System.lineSeparator());
                stringBuffer.append("通道|跑量金额|实时费率|入账金额" + System.lineSeparator());
                stringBuffer.append("-----------------------------------------------" + System.lineSeparator());

                for (int i = 0; i < passageList.size(); i++) {
                    PayPassage payPassage = passageList.get(i);
                    StatisticsPassage statisticsPassage = statisticsService.QueryStatisticsPassageByDate(payPassage.getPayPassageId(), yesterday);
                    String passageInfoStr = "[" + payPassage.getPayPassageId() + "] <b>" + payPassage.getPayPassageName() + "</b>";
                    if (statisticsPassage == null) {
                        stringBuffer.append(passageInfoStr + " | " + GetRateStr(payPassage.getRate()) + " | " + "  没有昨日跑量记录" + System.lineSeparator());
                    } else {
                        Long amount = statisticsPassage.getTotalSuccessAmount() - statisticsPassage.getTotalPassageCost();
                        stringBuffer.append(passageInfoStr + " | " + AmountUtil.convertCent2Dollar(statisticsPassage.getTotalSuccessAmount()) + " | " + GetRateStr(payPassage.getRate()) + " | " + AmountUtil.convertCent2Dollar(amount) + System.lineSeparator());
                        balance += (amount);
                        totalBalance += statisticsPassage.getTotalSuccessAmount();
                        totalCost += statisticsPassage.getTotalPassageCost();
                    }
                }
                stringBuffer.append("-----------------------------------------------" + System.lineSeparator());
                stringBuffer.append("跑量汇总：<b>" + AmountUtil.convertCent2Dollar(totalBalance) + "</b>" + System.lineSeparator());
                stringBuffer.append("手续费: <b>" + AmountUtil.convertCent2Dollar(totalCost) + "</b>" + System.lineSeparator());
                stringBuffer.append("入账汇总：<b>" + AmountUtil.convertCent2Dollar(balance) + "</b>" + System.lineSeparator());
                sendSingleMessage(chatId, stringBuffer.toString());
                return;
            }
            sendSingleMessage(chatId, "未绑定商户或通道");
            return;
        }

        //查询余额
        if (text.trim().equals(QUERY_BALANCE)) {
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

                    String amountStr = text.substring(3);
                    Long amount = Long.parseLong(AmountUtil.convertDollar2Cent(amountStr));
                    Date today = DateUtil.parse(DateUtil.today());


                    if (amount.longValue() == 0) {
                        //发送今日账单
                        sendBillStat(chatId, today, message.getMessageId(), "今日", true);
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

                    sendBillStat(chatId, today, message.getMessageId(), "今日", true);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            return;
        }
        //撤销下发
        if (text.trim().equals(REVOKE_RECORD)) {
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
                    sendBillStat(chatId, today, message.getMessageId(), "今日", true);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            return;
        }

        //清除下发  清除当天全部
        if (text.trim().equals(CLEAR_RECORD)) {
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
                    sendBillStat(chatId, today, message.getMessageId(), "今日", true);
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

                    String amountStr = text;

                    Date today = DateUtil.parse(DateUtil.today());
                    Long amount = Long.parseLong(AmountUtil.convertDollar2Cent(amountStr));

                    if (amount.longValue() == 0) {
                        sendBillStat(chatId, today, message.getMessageId(), "今日", true);
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


                    sendBillStat(chatId, today, message.getMessageId(), "今日", true);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

            }
            return;
        }

        //撤销记账
        if (text.trim().equals(REVOKE_RECORD_TOTAL)) {
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

                    sendBillStat(chatId, today, message.getMessageId(), "今日", true);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            return;
        }

        //清除记账  清除当天全部
        if (text.trim().equals(CLEAR_RECORD_TOTAL)) {
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

                    sendBillStat(chatId, today, message.getMessageId(), "今日", true);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            return;
        }


        //今日账单
        if (text.trim().equals(TODAY_RECORD)) {
            try {
                Date today = DateUtil.parse(DateUtil.today());
                sendBillStat(chatId, today, message.getMessageId(), "今日", false);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return;
        }

        //昨日账单
        if (text.trim().equals(YESTERDAY_RECORD)) {
            try {
                sendBillStat(chatId, DateUtil.yesterday(), message.getMessageId(), "昨日", false);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return;
        }

        //今日结算
        if (text.trim().equals(TODAY_SETTLE)) {
            try {
                if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                    RobotsMch robotsMch = robotsMchService.getById(chatId);
                    Date today = DateUtil.parse(DateUtil.today());
                    sendSettleInfo(robotsMch, today);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return;

        }
        //昨日结算
        if (text.trim().equals(YESTERDAY_SETTLE)) {
            try {
                if (robotsUserService.checkIsAdmin(userName) || robotsUserService.checkIsOp(userName)) {
                    RobotsMch robotsMch = robotsMchService.getById(chatId);
                    Date today = DateUtil.parse(DateUtil.today());
                    DateTime yesterday = DateUtil.offsetDay(today, -1);
                    sendSettleInfo(robotsMch, yesterday);
                }
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
                        if (payPassageMap.containsKey(passageId) && payPassageMap.get(passageId).getState() != CS.HIDE) {
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

    private String GetRateStr(BigDecimal rate) {
        return rate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) + "%";
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

    private void sendReplyAndPinMessage(Long chatId, Integer messageId, String messageStr) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(false);
        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(messageId);
        sendMessage.setText(messageStr);
        sendMessage.setParseMode(ParseMode.HTML);
        try {
            Message message = execute(sendMessage);
            sendSinglePinMessage(chatId, message.getMessageId());
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
            Message message = execute(sendMessage);
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

    private boolean sendDeleteMessage(DeleteMessage deleteMessage) {
        try {
            return execute(deleteMessage);
        } catch (Exception e) {
            log.error("{} {}", LOG_TAG, e);
            log.error(e.getMessage());
        }
        return false;
    }

    private boolean sendPinMessage(PinChatMessage pinChatMessage) {
        try {
            return execute(pinChatMessage);
        } catch (TelegramApiException e) {
            log.error("{} {}", LOG_TAG, e);
            log.error(e.getMessage());
        }
        return false;
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

                    if (StringUtils.isNotEmpty(message.getCaption()) && message.getCaption().contains("@")) {
                        return;
                    }

                    SendPhoto sendPhoto = new SendPhoto();
                    if (StringUtils.isNotEmpty(message.getCaption())) {
                        stringBuffer.append(message.getCaption() + System.lineSeparator());
                    }
                    stringBuffer.append(System.lineSeparator());
                    if (sourceMessage.hasText() && StringUtils.isNotEmpty(sourceMessage.getText())) {
                        stringBuffer.append(sourceMessage.getText() + System.lineSeparator());
                    }


                    sendPhoto.setChatId(chatId); // Replace with the destination chat ID
                    sendPhoto.setPhoto(new InputFile(message.getPhoto().get(0).getFileId()));
                    sendPhoto.setCaption(stringBuffer.toString());
                    sendPhoto.setReplyToMessageId(sourceMessage.getMessageId());
                    sendPhoto.setParseMode(ParseMode.HTML);
                    execute(sendPhoto);
                } else if (message.hasText()) {
                    if (StringUtils.isNotEmpty(message.getText()) && message.getText().contains("@")) {
                        return;
                    }
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
                    if (StringUtils.isNotEmpty(message.getCaption()) && message.getCaption().contains("@")) {
                        return;
                    }
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
            sendMessage.setParseMode(ParseMode.HTML);
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

    protected void sendDeleteMessage(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        sendDeleteMessage(deleteMessage);
    }

    protected boolean sendSinglePinMessage(Long chatId, Integer messageId) {
        PinChatMessage sendMessage = new PinChatMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setMessageId(messageId);
        return sendPinMessage(sendMessage);
    }


    /**
     * 查U价格
     *
     * @param chatId
     * @param userType all,blockTrade(大宗)
     * @param payType  all,bank,aliPay,wxPay
     * @param title    自定义标题
     */
    private void sendUSDT(Long chatId, String userType, String payType, String title) {
//        uj -- 查询市场USTD买入价，c2c全部支付方式;
//        ub -- 查询市场USTD买入价，c2c银行卡;
//        ua -- 查询市场USTD买入价，c2c支付宝;
//        uw -- 查询市场USTD买入价，c2c微信支付;
//
//        kj -- 查询市场USTD买入价，大宗全部支付方式;
//        kb -- 查询市场USTD买入价，大宗银行卡;
//        ka -- 查询市场USTD买入价，大宗支付宝;
//        kw -- 查询市场USTD买入价，大宗微信支付;


//        String url = "https://www.okx.com/v3/c2c/tradingOrders/books?t=" + System.currentTimeMillis() + "&quoteCurrency=cny&baseCurrency=usdt&side=buy&paymentMethod=all&userType=all&showTrade=false&showFollow=false&showAlreadyTraded=false&isAbleFilter=false&receivingAds=false&urlId=0";
        //https://www.okx.com/v3/c2c/tradingOrders/books?t=1693229098440&quoteCurrency=cny&baseCurrency=usdt&side=buy&paymentMethod=all&userType=all&showTrade=false&showFollow=false&showAlreadyTraded=false&isAbleFilter=false&receivingAds=false&urlId=0
        //https://www.okx.com/v3/c2c/tradingOrders/books?quoteCurrency=CNY&baseCurrency=USDT&side=sell&paymentMethod=wxPay&userType=all&t=1707206863366

        String url = "https://www.okx.com/v3/c2c/tradingOrders/books?quoteCurrency=CNY&baseCurrency=USDT&side=sell&showTrade=false&showFollow=false&showAlreadyTraded=false&isAbleFilter=false&receivingAds=false&urlId=0&paymentMethod=" + payType + "&userType=" + userType + "&t=" + System.currentTimeMillis();
        String raw = HttpUtil.get(url);
        JSONObject result = JSONObject.parseObject(raw);
        if (result.getString("code").equals("0")) {
            JSONArray buys = result.getJSONObject("data").getJSONArray("sell");
            String price = buys.getJSONObject(0).getString("price");
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("<b>" + title + "</b>" + System.lineSeparator());
            stringBuffer.append("今日汇率 USDT-CNY：<b>" + price + "</b>" + System.lineSeparator());
            for (int i = 0; i < buys.size(); i++) {
                if (i < 5) {
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

    private StringBuffer sendMchProduct(List<StatisticsMchProduct> list, Long chatId, String dayTitle) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(dayTitle + "跑量明细：" + System.lineSeparator());
        stringBuffer.append("----------------------------------" + System.lineSeparator());
        stringBuffer.append("产品|跑量|实时费率|应结算" + System.lineSeparator());
        stringBuffer.append("----------------------------------" + System.lineSeparator());
        for (int i = 0; i < list.size(); i++) {
            stringBuffer.append("[" + list.get(i).getProductId() + "] "
                    + list.get(i).getExt().getString("productName") + " | "
                    + AmountUtil.convertCent2Dollar(list.get(i).getTotalSuccessAmount()) + " | "
                    + list.get(i).getExt().getString("rate") + " | "
                    + AmountUtil.convertCent2Dollar(list.get(i).getTotalSuccessAmount() - list.get(i).getTotalCost()) + System.lineSeparator());
        }
        return stringBuffer;
    }

    private StringBuffer sendDayStat(StatisticsMch statisticsMch, Long chatId, String title) {

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        float rate = (statisticsMch.getOrderSuccessCount().floatValue() / statisticsMch.getTotalOrderCount().floatValue()) * 100;
        String rateStr = decimalFormat.format(rate);
        Long amount = statisticsMch.getTotalSuccessAmount() - statisticsMch.getTotalMchCost();

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(title + DateUtil.format(statisticsMch.getStatisticsDate(), "yyyy-MM-dd") + " 汇总:" + System.lineSeparator());
        stringBuffer.append("跑量总金额: " + AmountUtil.convertCent2Dollar(statisticsMch.getTotalSuccessAmount()) + System.lineSeparator());
        stringBuffer.append("入账总金额: " + AmountUtil.convertCent2Dollar(amount) + System.lineSeparator());
        stringBuffer.append("成交订单数: " + statisticsMch.getOrderSuccessCount() + System.lineSeparator());
        stringBuffer.append("总订单数: " + statisticsMch.getTotalOrderCount() + System.lineSeparator());
        stringBuffer.append("成功率: " + rateStr + "%");
        return stringBuffer;
    }

    private void sendBillStat(Long chatId, Date date, Integer messageId, String dayTitle, boolean isPin) {
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
        Long totalDayAmount = 0L;
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
                totalDayAmount += totalList.get(i).getAmount();
                totalStatStr.append(DateUtil.format(totalList.get(i).getCreatedAt(), "HH:mm:ss") + "  <b>" + AmountUtil.convertCent2Dollar(totalList.get(i).getAmount()) + "</b>  " + totalList.get(i).getUserName() + System.lineSeparator());
            } else {
                totalStatStr.append("<s>" + DateUtil.format(totalList.get(i).getCreatedAt(), "HH:mm:ss") + "  " + AmountUtil.convertCent2Dollar(totalList.get(i).getAmount()) + "  " + totalList.get(i).getUserName() + "</s>  " + totalList.get(i).getRemark() + System.lineSeparator());
            }
        }
        stringBuffer.append("<b>" + DateUtil.format(date, "yyyy-MM-dd") + "</b>" + System.lineSeparator());
        stringBuffer.append("[<b>下发总额</b>]: <b>" + AmountUtil.convertCent2Dollar(totalAmount) + "</b>" + System.lineSeparator());
        stringBuffer.append("当日记账累计: <b>" + AmountUtil.convertCent2Dollar(dayAmount) + "</b>" + System.lineSeparator());
        stringBuffer.append("================" + System.lineSeparator());
        stringBuffer.append(dayTitle + "下发: (" + totalList.size() + "笔)" + System.lineSeparator());
        stringBuffer.append(totalStatStr);
        stringBuffer.append("----------------" + System.lineSeparator());
        stringBuffer.append("当日下发累计: <b>" + AmountUtil.convertCent2Dollar(totalDayAmount) + "</b>" + System.lineSeparator());
        stringBuffer.append("================" + System.lineSeparator());
        stringBuffer.append(dayTitle + "记账: (" + dayList.size() + "笔)" + System.lineSeparator());
        stringBuffer.append(dayStatStr);
        stringBuffer.append("================" + System.lineSeparator());
        stringBuffer.append("(<b>下发累积，记账日清</b>)" + System.lineSeparator());

        if (isPin) {
            sendReplyAndPinMessage(chatId, messageId, stringBuffer.toString());
        } else {
            sendReplyMessage(chatId, messageId, stringBuffer.toString());
        }
    }

    private void sendMchSettle(JSONArray mchNos, Long chatId, Date date) {
        try {
            if (mchNos.isEmpty()) {
                return;
            }

            Long totalAmount = 0L;
            Long amount = 0L;

            //单个商户
            for (int i = 0; i < mchNos.size(); i++) {
                String mchNo = mchNos.getString(i);
                MchInfo mchInfo = mchInfoService.getById(mchNo.trim());
                if (mchInfo != null) {
                    StatisticsMch statisticsMch = statisticsService.QueryStatisticsMchByDate(mchNo, date);
                    String timeStrWithEnter = "<b>" + DateUtil.format(date, "yyyy-MM-dd") + "</b>" + System.lineSeparator();
                    String mchInfoStr = "[" + mchInfo.getMchNo() + "] <b>" + mchInfo.getMchName() + "</b>";
                    if (statisticsMch == null) {
                        sendSingleMessage(chatId, timeStrWithEnter + mchInfoStr + " 没有当日跑量记录");
                    } else {
                        StringBuffer stringBuffer = new StringBuffer();

                        stringBuffer.append(timeStrWithEnter + mchInfoStr + " 当日结算:" + System.lineSeparator());
                        stringBuffer.append("-----------------------" + System.lineSeparator());
                        stringBuffer.append("跑量汇总: <b>" + AmountUtil.convertCent2Dollar(statisticsMch.getTotalSuccessAmount()) + "</b>" + System.lineSeparator());
                        stringBuffer.append("入账汇总: <b>" + AmountUtil.convertCent2Dollar(statisticsMch.getTotalSuccessAmount() - statisticsMch.getTotalMchCost()) + "</b>" + System.lineSeparator());
                        stringBuffer.append("-----------------------" + System.lineSeparator());

                        totalAmount += statisticsMch.getTotalSuccessAmount();
                        amount += (statisticsMch.getTotalSuccessAmount() - statisticsMch.getTotalMchCost());

                        //查调账、提现 记录 当日
                        List<MchHistory> mchHistories = getMchHistoryByDate(mchNo, date);
                        if (!mchHistories.isEmpty()) {
                            stringBuffer.append("<b>资金变动摘要:</b>" + System.lineSeparator());
                            for (int x = 0; x < mchHistories.size(); x++) {
                                MchHistory mchHistory = mchHistories.get(x);
                                String remark = "";
                                if (StringUtils.isNotEmpty(mchHistory.getRemark().trim())) {
                                    remark = "| 备注: " + mchHistory.getRemark().trim();
                                }
                                stringBuffer.append("[" + CS.GetMchBizTypeString(mchHistory.getBizType()) + "]: <b>" + AmountUtil.convertCent2Dollar(mchHistory.getAmount()) + "</b> | " + DateUtil.formatDateTime(mchHistory.getCreatedAt()) + remark + System.lineSeparator());
                            }
                            stringBuffer.append("-----------------------" + System.lineSeparator());
                            stringBuffer.append("手续费总额：" + AmountUtil.convertCent2DollarShort(statisticsMch.getTotalMchCost()) + System.lineSeparator());
                            stringBuffer.append("-----------------------" + System.lineSeparator());
                        }

                        //当日 第一笔流水 最后一笔流水
                        List<MchHistory> mchHistoryTotal = getTotalMchHistoryByDate(mchNo, date);
                        if (!mchHistoryTotal.isEmpty()) {

                            MchHistory first = mchHistoryTotal.get(0);
                            stringBuffer.append("日初余额: " + AmountUtil.convertCent2Dollar(first.getBeforeBalance()) + System.lineSeparator());

                            MchHistory last = mchHistoryTotal.get(mchHistoryTotal.size() - 1);
                            stringBuffer.append("日终余额: " + AmountUtil.convertCent2Dollar(last.getAfterBalance()) + System.lineSeparator());
                            stringBuffer.append("-----------------------" + System.lineSeparator());
                        }


                        //查当日手续费

                        stringBuffer.append("当前商户余额: <b>" + AmountUtil.convertCent2DollarShort(mchInfo.getBalance()) + "</b>" + System.lineSeparator());
                        stringBuffer.append("<b>请注意是否还有支付中的订单,可能导致结算数据有少许出入</b>" + System.lineSeparator());
                        sendSingleMessageAndPin(chatId, stringBuffer.toString());
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

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
     * 查商户当天的手动流水
     *
     * @param mchNo
     * @param date
     * @return
     */
    private List<MchHistory> getMchHistoryByDate(String mchNo, Date date) {
        LambdaQueryWrapper<MchHistory> wrapper = MchHistory.gw();

        wrapper.eq(MchHistory::getMchNo, mchNo);
        wrapper.in(MchHistory::getBizType, Arrays.asList(CS.BIZ_TYPE_CHANGE, CS.BIZ_TYPE_WITHDRAW, CS.BIZ_TYPE_UNFREEZE));

        //一天的开始，结果：2017-03-01 00:00:00
        Date beginOfDay = DateUtil.beginOfDay(date);

        //一天的结束，结果：2017-03-01 23:59:59
        Date endOfDay = DateUtil.endOfDay(date);

        wrapper.ge(MchHistory::getCreatedAt, DateUtil.format(beginOfDay, "yyyy-MM-dd HH:mm:ss"));
        wrapper.le(MchHistory::getCreatedAt, DateUtil.format(endOfDay, "yyyy-MM-dd HH:mm:ss"));
        wrapper.orderByAsc(MchHistory::getCreatedAt);
        return mchHistoryService.list(wrapper);
    }

    private List<MchHistory> getTotalMchHistoryByDate(String mchNo, Date date) {
        LambdaQueryWrapper<MchHistory> wrapper = MchHistory.gw();
        wrapper.eq(MchHistory::getMchNo, mchNo);

        //一天的开始，结果：2017-03-01 00:00:00
        Date beginOfDay = DateUtil.beginOfDay(date);

        //一天的结束，结果：2017-03-01 23:59:59
        Date endOfDay = DateUtil.endOfDay(date);

        wrapper.ge(MchHistory::getCreatedAt, DateUtil.format(beginOfDay, "yyyy-MM-dd HH:mm:ss"));
        wrapper.le(MchHistory::getCreatedAt, DateUtil.format(endOfDay, "yyyy-MM-dd HH:mm:ss"));
        wrapper.orderByAsc(MchHistory::getCreatedAt);

        return mchHistoryService.list(wrapper);
    }


    /**
     * 判断是否商户群
     *
     * @param robotsMch
     * @return
     */
    private boolean checkIsMchChat(RobotsMch robotsMch) {
        if (robotsMch == null) {
            return false;
        }
        if (robotsMch.getMchNo().equals(CS.ROBOTS_MGR_MCH)) {
            return false;
        }
        String mchStr = robotsMch.getMchNo();
        if (StringUtils.isNotEmpty(mchStr)) {
            JSONArray jsonArray = JSONArray.parseArray(mchStr);
            //是空的移除
            if (jsonArray.isEmpty()) {
                robotsMch.setMchNo("");
                robotsMchService.saveOrUpdate(robotsMch);
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }

//        return robotsMch != null && !robotsMch.getMchNo().equals(CS.ROBOTS_MGR_MCH);
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
                sendBillStat(chatId, today, callbackQuery.getMessage().getMessageId(), "今日", false);
                break;
            case YESTERDAY_RECORD://昨日账单
                sendBillStat(chatId, DateUtil.yesterday(), callbackQuery.getMessage().getMessageId(), "昨日", false);
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
//        commands.add(new BotCommand("usdt", "今日U价格查询"));
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
                        stringBuffer.append("<b>异常订单预警❗</b>" + System.lineSeparator());
                        stringBuffer.append("检测时间点：[ " + DateUtil.format(nowTime, "yyyy-MM-dd HH:mm:ss") + " ]" + System.lineSeparator());
                        for (int i = 0; i < count; i++) {
                            PayOrder payOrder = list.get(i);
                            stringBuffer.append("订单号：[ " + payOrder.getPayOrderId() + " ]--通道:<b>[" + payOrder.getPassageId() + "] " + payPassageMap.get(payOrder.getPassageId()).getPayPassageName() + "</b>" + System.lineSeparator());
                        }
                        stringBuffer.append("过去一分钟异常订单数为[ <b>" + count + "</b> ]条，触发预警请检查❗" + System.lineSeparator());
                        stringBuffer.append("（如需关闭此预警，请到四方后台调整[系统管理]-[机器人配置]-[异常订单预警]）" + System.lineSeparator());
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
     * @param messageStr
     * @param message
     */
    public void sendSingleQuery(String messageStr, Message message) {

        if (StringUtils.isNotEmpty(messageStr)) {
            Long chatId = message.getChatId();
            RobotsMch robotsMch = robotsMchService.getMch(chatId);

            int firstSpaceIndex = messageStr.indexOf(" ");
            String unionOrderId = "";
            String remark = "";
            // 截取第一个空格之后的所有内容
            if (firstSpaceIndex != -1) {
                remark = messageStr.substring(firstSpaceIndex + 1);
                unionOrderId = messageStr.substring(0, firstSpaceIndex);
            } else {
                unionOrderId = messageStr;
            }

            Pattern pattern = Pattern.compile(ORDER_REGEX);
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
                JSONArray mchArray = JSONArray.parseArray(mchStr);
                if (mchArray.isEmpty()) {
                    return;
                }
                List<String> mchNos = new ArrayList<>();
                for (int i = 0; i < mchArray.size(); i++) {
                    mchNos.add(mchArray.getString(i));
                }

                LambdaQueryWrapper<PayOrder> wrapper = PayOrder.gw();
                if (StringUtils.isNotEmpty(remark)) {
                    String orderStr = messageStr.substring(0, firstSpaceIndex);
                    wrapper.and(wr -> {
                        wr.eq(PayOrder::getPayOrderId, orderStr).or().eq(PayOrder::getMchOrderNo, orderStr);
                        wr.in(PayOrder::getMchNo, mchNos);
                    });
                } else {
                    wrapper.and(wr -> {
                        wr.eq(PayOrder::getPayOrderId, messageStr.trim()).or().eq(PayOrder::getMchOrderNo, messageStr.trim());
                        wr.in(PayOrder::getMchNo, mchNos);
                    });
                }


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

                    if (!mchStr.contains(payOrder.getMchNo())) {
                        sendSingleMessage(chatId, "未查询到该群所有绑定商户下，订单号为[" + unionOrderId + "]的记录");
                        return;
                    }
                    //检查订单是否成功状态
                    if (payOrder.getState() == PayOrder.STATE_SUCCESS) {
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append("订单已支付成功！" + System.lineSeparator());
                        stringBuffer.append("商户单号：[ <b>" + payOrder.getMchOrderNo() + "</b> ]" + System.lineSeparator());
                        stringBuffer.append("支付单号：[ " + payOrder.getPayOrderId() + " ]" + System.lineSeparator());
                        sendReplyMessage(chatId, message.getMessageId(), stringBuffer.toString());
                        return;
                    }

                    //1、将这条消息转发到通道群,带图或视频
                    Long passageId = payOrder.getPassageId();
                    RobotsPassage robotsPassage = robotsPassageService.getById(passageId);
                    if (robotsPassage != null) {

                        String addOnInfo = payOrder.getPayOrderId();
                        if (StringUtils.isNotEmpty(remark)) {
                            addOnInfo = addOnInfo + System.lineSeparator() + "备注:" + remark;
                        }

                        Message messageTemp = null;
                        try {
                            //转发到通道群的message
                            messageTemp = sendQueryOrderMessage(robotsPassage.getChatId(), message, addOnInfo);
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                        if (messageTemp != null) {

                            //商户群查单 chatId+messageId -原消息  用于识别回复方式的催单
                            RedisUtil.set(REDIS_MCH_SOURCE_SUFFIX + message.getChatId() + message.getMessageId(), messageTemp, 24, TimeUnit.HOURS);

                            //商户已经识别成功的查单消息,通过订单号存储，方便识别
                            RedisUtil.set(REDIS_MCH_SOURCE_ORDER_SUFFIX + payOrder.getPayOrderId(), message, 24, TimeUnit.HOURS);

                            //用于识别是否催单信息
                            RedisUtil.set(REDIS_ORDER_FORWARD_SUFFIX + message.getChatId() + unionOrderId, messageTemp, 24, TimeUnit.HOURS);

                            StringBuffer stringBufferOrderInfo = new StringBuffer();
                            stringBufferOrderInfo.append("机器人补充信息：" + System.lineSeparator());
                            stringBufferOrderInfo.append("商户订单号 [ <b>" + payOrder.getMchOrderNo() + "</b> ] " + System.lineSeparator());
                            message.setText(stringBufferOrderInfo.toString());

                            //机器人发到通道群的转发的消息,key是新消息ID，值是存储的商户群原消息
                            RedisUtil.set(REDIS_SOURCE_SUFFIX + messageTemp.getChatId() + messageTemp.getMessageId(), message, 24, TimeUnit.HOURS);

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
                            stringBuffer.append("商户 " + robotsMch.getMchNo() + " 查单:" + System.lineSeparator());
                            stringBuffer.append("商户订单号 [ " + payOrder.getMchOrderNo() + " ] " + System.lineSeparator());
                            stringBuffer.append("支付订单号 [ " + payOrder.getPayOrderId() + " ] " + System.lineSeparator());
                            stringBuffer.append("未检测到已绑定的通道群,请先绑定后再查单!" + System.lineSeparator());
                            stringBuffer.append("通道信息：[ " + passage.getPayPassageId() + " ] " + passage.getPayPassageName() + System.lineSeparator());
                            sendSingleMessage(robotsMchAdmin.getChatId(), stringBuffer.toString());
                        } else {
                            StringBuffer stringBuffer = new StringBuffer();
                            stringBuffer.append("商户 " + robotsMch.getMchNo() + " 查单:" + System.lineSeparator());
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

    private void sendSettleInfo(RobotsMch robotsMch, Date date) {
        Long chatId = robotsMch.getChatId();
        if (robotsMch == null || StringUtils.isEmpty(robotsMch.getMchNo())) {
            sendSingleMessage(chatId, "请先绑定商户");
            return;
        }
        JSONArray jsonArray = JSONArray.parseArray(robotsMch.getMchNo());
        if (jsonArray.isEmpty()) {
            sendSingleMessage(chatId, "请先绑定商户");
            return;
        }
        sendMchSettle(jsonArray, chatId, date);
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

    /**
     * 异常通道检测
     */
    @Scheduled(fixedRate = 60000) // 每60秒执行一次
    public void checkErrorPassage() {

        int errorOrderWarnCount = Integer.parseInt(sysConfigService.getRobotsConfig().getErrorOrderWarnConfig().trim());

        //异常通道检测-同异常订单(0关闭，1打开)
        if (errorOrderWarnCount <= 0) {
            return;
        }

        Map<Long, Integer> errorInfoMap = RedisUtil.checkAndCleanPassageErrorInfo(errorOrderWarnCount);
        if (!errorInfoMap.isEmpty()) {
            RobotsMch robotsMch = robotsMchService.getManageMch();
            if (robotsMch != null) {
                Date nowTime = new Date();

                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("<b>通道异常预警❗</b>" + System.lineSeparator());
                stringBuffer.append("检测时间点：[ " + DateUtil.format(nowTime, "yyyy-MM-dd HH:mm:ss") + " ]" + System.lineSeparator());
                stringBuffer.append("过去一分钟拉起通道失败次数过多" + System.lineSeparator());
                Map<Long, PayPassage> payPassageMap = payPassageService.getPayPassageMap();
                for (Map.Entry<Long, Integer> entry : errorInfoMap.entrySet()) {
                    Long passageId = entry.getKey();
                    //发预警消息
                    stringBuffer.append("通道：[<b>" + passageId + "</b>] <b>" + payPassageMap.get(passageId).getPayPassageName() + "</b> 拉起失败次数: " + entry.getValue() + System.lineSeparator());
                }
                stringBuffer.append("请及时调整通道!" + System.lineSeparator());
                stringBuffer.append("（如需关闭此预警，请到四方后台调整[系统管理]-[机器人配置]-[异常订单预警]）" + System.lineSeparator());
                sendSingleMessage(robotsMch.getChatId(), stringBuffer.toString());
            }
        }
    }

    @Override
    public void receive(RobotListenPayOrderSuccessMQ.MsgPayload payload) {
        try {
            String payOrderId = payload.getPayOrderId();
            //商户查单消息
            Message messageSource = RedisUtil.getObject(REDIS_MCH_SOURCE_ORDER_SUFFIX + payOrderId, Message.class);
            if (messageSource != null) {
                log.info("收到成功订单并有查单缓存" + payOrderId);
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("订单已回调！" + System.lineSeparator());
                stringBuffer.append("商户单号：[ <b>" + payload.getMchOrderNo() + "</b> ]" + System.lineSeparator());

                sendReplyMessage(messageSource.getChatId(), messageSource.getMessageId(), stringBuffer.toString());
                RedisUtil.del(REDIS_MCH_SOURCE_ORDER_SUFFIX + payOrderId);
            }


        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void receive(RobotWarnMQ.MsgPayload robotWarn) {
        try {
            Byte warnType = robotWarn.getWarnType();
            if (Objects.equals(warnType, CS.ROBOT_WARN_TYPE.PASSAGE_ERROR)) {
                RobotWarnPassage robotWarnPassage = JSONObject.parseObject(robotWarn.getData(), RobotWarnPassage.class);
                if (robotWarnPassage != null) {
                    //异常信息存入redis
                    RedisUtil.savePassageErrorInfo(robotWarnPassage.getPassageId(), robotWarnPassage.getTimestamp());
                } else {
                    log.error("转换 RobotWarnPassage 为空，检查代码");
                }
            } else if (Objects.equals(warnType, CS.ROBOT_WARN_TYPE.NOTIFY_ERROR)) {
                RobotWarnNotify robotWarnNotify = JSONObject.parseObject(robotWarn.getData(), RobotWarnNotify.class);
                MchInfo mchInfo = mchInfoService.getById(robotWarnNotify.getMchNo());

                //检查是否有商户群
                RobotsMch robotsMch = robotsMchService.getOne(RobotsMch.gw().like(RobotsMch::getMchNo, mchInfo.getMchNo()).ne(RobotsMch::getMchNo, CS.ROBOTS_MGR_MCH));
                if (robotsMch != null) {
                    String messageStr = "商户 [" + robotWarnNotify.getMchNo() + "]" + mchInfo.getMchName() + System.lineSeparator() + "订单 [<b>" + robotWarnNotify.getPayOrderId() + "</b>]" + System.lineSeparator() + "发送通知失败，请及时处理！";
                    sendSingleMessage(robotsMch.getChatId(), messageStr);
                } else {
                    //没有则发管理群
                    RobotsMch robotsMchManage = robotsMchService.getManageMch();
                    if (robotsMchManage != null) {
                        String messageStr = "商户 [" + robotWarnNotify.getMchNo() + "]" + mchInfo.getMchName() + System.lineSeparator() + "订单 [<b>" + robotWarnNotify.getPayOrderId() + "</b>]" + System.lineSeparator() + "发送通知失败，请及时处理！" + System.lineSeparator() + "[建议及时绑定商户群，机器人将自动发送通知失败提醒到商户群]";
                        sendSingleMessage(robotsMchManage.getChatId(), messageStr);
                    } else {
                        log.error("转换 RobotWarnNotify 为空，检查代码");
                    }
                }


            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        String test = " 2114980136573839 \n 测下备注";
        log.info("|" + test.trim() + "|");
    }

}