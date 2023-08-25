package com.jeequan.jeepay.com.jeequan.service;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.menubutton.MenuButton;
import org.telegram.telegrambots.meta.api.objects.menubutton.MenuButtonCommands;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;
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
    private static final String BLIND_DEL_MCH = "商户解绑";

    private static final String TODAY_BILL = "今日跑量";

    private static final String YESTERDAY_BILL = "昨日跑量";

    private static final String QUERY_BALANCE = "查询余额";

    private static final String QUERY_ORDER = "查单 \\w+";

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

    /**
     * 绑定管理群
     */
    private static final String BLIND_MGR = "绑定管理群";
    private static final String BLIND_MGR_CONFIRM = "BLIND_MGR_CONFIRM";
    private static final String BLIND_MGR_CANCEL = "BLIND_MGR_CANCEL";

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
        //if (update.hasMessage() && update.getMessage().hasText()) {
        if (update.hasMessage()) {
            //检测权限，命令
            handleCommand(update);
        } else if (update.hasCallbackQuery()) {
            //用户点击按钮的回复
            handleCallbackQuery(update);
        }
    }

    /**
     * 命令管理
     *
     * @param update
     */
    private void handleCommand(Update update) {
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String userName = message.getFrom().getUserName();
        Message messageReply = message.getReplyToMessage();
        if (messageReply != null) {
            //检测是否查单转发信息
            Message messageSource = RedisUtil.getObject(REDIS_SOURCE_SUFFIX + messageReply.getMessageId(), Message.class);
            if (messageSource != null) {
                sendQueryMessage(message, messageSource);
            }
        }
        if (!message.hasText() || message.hasPhoto()) {
            return;
        }

        //==================================匹配命令==========================================================
        String text = message.getText().trim();
        //绑定管理群
        if (text.equals(BLIND_MGR)) {
            //是否admin
            if (robotsUserService.checkIsAdmin(userName)) {
                //不是商户群就覆盖  chatId下有商户号
                RobotsMch robotsMch = robotsMchService.getMch(chatId);
                if (checkIsMchChat(robotsMch)) {
                    sendSingleMessage(chatId, "当前群已绑定为商户群,不可绑定为四方群!");
                    return;
                }
                if (checkIsPassageChat(chatId)) {
                    sendSingleMessage(chatId, "当前群已绑定为通道群,不可绑定为四方群!");
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


        //绑定商户-管理员
        Pattern patternBlindMch = Pattern.compile(BLIND_MCH);
        Matcher matcherBlindMch = patternBlindMch.matcher(text);
        if (matcherBlindMch.matches()) {
            if (robotsUserService.checkIsAdmin(userName)) {
                RobotsMch robotsMchAdmin = robotsMchService.getManageMch();
                if (robotsMchAdmin != null && robotsMchAdmin.getChatId().longValue() == chatId.longValue()) {
                    sendSingleMessage(chatId, "当前群已绑定为四方管理群，不可重复绑定商户");
                    return;
                }

                if (checkIsPassageChat(chatId)) {
                    sendSingleMessage(chatId, "当前群已绑定为通道群，不可重复绑定商户");
                    return;
                }

                //清空老的商户号-chatId
                String mchNo = text.substring(5);
                MchInfo mchInfo = mchInfoService.getById(mchNo);

                if (mchInfo == null) {
                    sendSingleMessage(chatId, "未查询到该商户 [" + mchNo + "] 请检查！");
                } else {
                    robotsMchService.updateBlindMch(chatId, mchNo);
                    sendSingleMessage(chatId, "绑定商户成功! [ " + mchNo + " ] " + mchInfo.getMchName());
                }
                return;
            }
            return;
        }

        /**
         * 商户解绑
         */
        if (text.trim().equals(BLIND_DEL_MCH)) {
//          是否admin
            if (robotsUserService.checkIsAdmin(userName)) {
                //是否已绑定商户
                if (robotsMchService.unBlindMch(chatId)) {
                    sendSingleMessage(chatId, "商户群解绑成功!");
                } else {
                    sendSingleMessage(chatId, "当前群未绑定商户");
                }
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
            //是否已绑定商户
            if (robotsMch != null) {
                String mchNo = robotsMch.getMchNo();
                //查今日账单
                Date today = DateUtil.parse(DateUtil.today());
                StatisticsMch todayStatisticsMch = statisticsService.QueryStatisticsMchByDate(mchNo, today);
                List<StatisticsMchProduct> statisticsMchProductList = statisticsService.QueryStatMchProduct(mchNo, today);

                if (todayStatisticsMch == null) {
                    sendSingleMessage(chatId, "没有今日跑量记录");
                } else {
                    sendMchProduct(statisticsMchProductList, chatId, "今日");
                    sendDayStat(todayStatisticsMch, chatId);
                }
            } else {
                sendSingleMessage(chatId, "未绑定商户或没有记录");
            }
            return;
        }

        //昨日跑量
        if (text.equals(YESTERDAY_BILL)) {

            Date today = DateUtil.parse(DateUtil.today());
            DateTime yesterday = DateUtil.offsetDay(today, -1);

            RobotsMch robotsMch = robotsMchService.getMch(chatId);
            //是否已绑定商户
            if (robotsMch != null) {
                String mchNo = robotsMch.getMchNo();
                //查跑量
                StatisticsMch todayStatisticsMch = statisticsService.QueryStatisticsMchByDate(mchNo, yesterday);
                List<StatisticsMchProduct> statisticsMchProductList = statisticsService.QueryStatMchProduct(mchNo, yesterday);

                if (todayStatisticsMch == null) {
                    sendSingleMessage(chatId, "没有昨日跑量记录");
                } else {
                    sendMchProduct(statisticsMchProductList, chatId, "昨日");
                    sendDayStat(todayStatisticsMch, chatId);
                }
            } else {
                sendSingleMessage(chatId, "未绑定商户或没有记录");
            }

            return;
        }

        //查询余额
        if (text.equals(QUERY_BALANCE)) {
            RobotsMch robotsMch = robotsMchService.getMch(chatId);
            //是否已绑定商户
            if (robotsMch != null) {
                String mchNo = robotsMch.getMchNo();
                MchInfo mchInfo = mchInfoService.queryMchInfo(mchNo);
                if (mchInfo == null) {
                    sendSingleMessage(chatId, "没有该商户的记录");
                } else {
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append("[ " + mchNo + " ] " + mchInfo.getMchName() + System.lineSeparator());
                    stringBuffer.append("当前账户余额为:" + System.lineSeparator());
                    stringBuffer.append(AmountUtil.convertCent2Dollar(mchInfo.getBalance()) + System.lineSeparator());
                    sendSingleMessage(chatId, stringBuffer.toString());
                }
            } else {
                sendSingleMessage(chatId, "未绑定商户或没有记录");
            }
            return;
        }

        //查单 商户订单号 平台订单号
        Pattern patternQueryOrder = Pattern.compile(QUERY_ORDER);
        Matcher matcherQueryOrder = patternQueryOrder.matcher(text);
        if (matcherQueryOrder.matches()) {
            RobotsMch robotsMch = robotsMchService.getMch(chatId);
            //是否已绑定商户
            if (robotsMch != null) {
                String unionOrderId = text.substring(3).trim();
                LambdaQueryWrapper<PayOrder> wrapper = PayOrder.gw();
                wrapper.and(wr -> {
                    wr.eq(PayOrder::getPayOrderId, unionOrderId).or().eq(PayOrder::getMchOrderNo, unionOrderId);
                });
                wrapper.eq(PayOrder::getMchNo, robotsMch.getMchNo());
                PayOrder payOrder = payOrderService.getOne(wrapper);
                if (payOrder == null) {
                    sendSingleMessage(chatId, "未查询到商户[" + robotsMch.getMchNo() + "]下，订单号为[" + unionOrderId + "]的记录");
                } else {
                    //1、将这条消息转发到通道群
                    Long passageId = payOrder.getPassageId();
                    RobotsPassage robotsPassage = robotsPassageService.getById(passageId);
                    if (robotsPassage != null) {
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append("请核实订单是否支付。如支付，烦请补单。如有异常，请回复此条消息进行反馈！(两小时内回复有效):" + System.lineSeparator());
                        stringBuffer.append("支付订单号为 [ " + payOrder.getPayOrderId() + " ] " + System.lineSeparator());
                        if (StringUtils.isNotEmpty(payOrder.getPassageOrderNo())) {
                            stringBuffer.append("通道订单号为 [ " + payOrder.getPassageOrderNo() + " ] " + System.lineSeparator());
                        }
                        Message messageTemp = sendSingleMessage(robotsPassage.getChatId(), stringBuffer.toString());
                        if (messageTemp != null) {
                            //保持2小时缓存
                            RedisUtil.set(REDIS_SOURCE_SUFFIX + messageTemp.getMessageId(), message, 2, TimeUnit.HOURS);
                        } else {
                            robotsPassageService.removeById(passageId);
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
                            stringBuffer.append("商户订单号为 [ " + payOrder.getMchOrderNo() + " ] " + System.lineSeparator());
                            stringBuffer.append("支付订单号为 [ " + payOrder.getPayOrderId() + " ] " + System.lineSeparator());
                            stringBuffer.append("未检测到已绑定的通道群,请先绑定后再查单!" + System.lineSeparator());
                            stringBuffer.append("通道信息：[ " + passage.getPayPassageId() + " ] " + passage.getPayPassageName() + System.lineSeparator());
                            sendSingleMessage(robotsMchAdmin.getChatId(), stringBuffer.toString());
                        } else {
                            StringBuffer stringBuffer = new StringBuffer();
                            stringBuffer.append("商户[" + robotsMch.getMchNo() + "] 查单:" + System.lineSeparator());
                            stringBuffer.append("商户订单号为 [ " + payOrder.getMchOrderNo() + " ] " + System.lineSeparator());
                            stringBuffer.append("支付订单号为 [ " + payOrder.getPayOrderId() + " ] " + System.lineSeparator());
                            stringBuffer.append("未检测到已绑定的通道群,请先绑定后再查单!" + System.lineSeparator());
                            stringBuffer.append("该订单未查询到已绑定的通道群,请通知四方工作人员先绑定通道群!" + System.lineSeparator());
                            sendSingleMessage(chatId, stringBuffer.toString());
                        }
                    }
                }
            } else {
                sendSingleMessage(chatId, "未绑定商户,请先绑定商户");
            }
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
                    String[] passageIds = passageStr.split(",");

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
                    String[] passageIds = passageStr.split(",");

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
        }
        return null;
    }

    private void sendForwardAndReply(ForwardMessage forwardMessage, Message replyMessage) {
        try {
            Message message = execute(forwardMessage);
            message.setReplyToMessage(replyMessage);
        } catch (TelegramApiException e) {
            log.error("{} {}", LOG_TAG, e);
        }
    }

    private void sendQueryMessage(Message message, Message sourceMessage) {
        try {
            Long chatId = sourceMessage.getChatId();

            if (message.isReply()) {
                if (message.hasPhoto()) {
                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setChatId(chatId); // Replace with the destination chat ID
                    sendPhoto.setPhoto(new InputFile(message.getPhoto().get(0).getFileId()));
                    sendPhoto.setCaption(message.getCaption());
                    sendPhoto.setReplyToMessageId(sourceMessage.getMessageId());
                    execute(sendPhoto);
                } else if (message.hasText()) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText(message.getText());
                    sendMessage.setReplyToMessageId(sourceMessage.getMessageId());
                    execute(sendMessage);
                }
//                RedisUtil.del(REDIS_SOURCE_SUFFIX + message.getReplyToMessage().getMessageId());
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


    protected Message sendSingleMessage(Long chatId, String messageStr) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(false);
        sendMessage.setChatId(chatId);
        sendMessage.setText(messageStr);
        return sendSingleMessage(sendMessage);
    }

    protected Message sendSinglePinMessage(Long chatId, String messageStr) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(false);
        sendMessage.setChatId(chatId);
        sendMessage.setText(messageStr);
        return sendSingleMessage(sendMessage);
    }

    private void sendMgrBlindMenu(Long chatId) {
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(chatId);
        replyMessage.setText("确认绑定该群为四方管理群么？");

        // 創建Inline Keyboard
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        // 添加按鈕
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("✅确认");
        inlineKeyboardButton1.setCallbackData(BLIND_MGR_CONFIRM);

        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton2.setText("❌取消");
        inlineKeyboardButton2.setCallbackData(BLIND_MGR_CANCEL);

        rowInline.add(inlineKeyboardButton1);
        rowInline.add(inlineKeyboardButton2);

        // 添加按鈕到行，然後將行添加到Inline Keyboard
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);

        // 將Inline Keyboard添加到回復消息中
        replyMessage.setReplyMarkup(markupInline);
        sendSingleMessage(replyMessage);
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
            List<PayPassage> passageList = new ArrayList<>();
            for (int i = 0; i < robotsPassageList.size(); i++) {
                passageList.add(payPassageMap.get(robotsPassageList.get(i).getPassageId()));
            }
            Collections.sort(passageList, (o1, o2) -> Collator.getInstance(Locale.CHINA).compare(o1.getPayPassageName(), o2.getPayPassageName()));
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

    private void sendDayStat(StatisticsMch statisticsMch, Long chatId) {

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        float rate = (statisticsMch.getOrderSuccessCount().floatValue() / statisticsMch.getTotalOrderCount().floatValue()) * 100;
        String rateStr = decimalFormat.format(rate);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(DateUtil.format(statisticsMch.getStatisticsDate(), "yyyy-MM-dd") + " 账单:" + System.lineSeparator());
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
        stringBuffer.append("下发总额: " + AmountUtil.convertCent2Dollar(dayAmount) + System.lineSeparator());
        stringBuffer.append("记账总额: " + AmountUtil.convertCent2Dollar(totalAmount) + System.lineSeparator());
//        stringBuffer.append(System.lineSeparator());
        stringBuffer.append("================" + System.lineSeparator());
        stringBuffer.append(dayTitle + "下发: (" + dayList.size() + "笔)" + System.lineSeparator());
        stringBuffer.append(dayStatStr);
        stringBuffer.append("================" + System.lineSeparator());
        stringBuffer.append(dayTitle + "记账: (" + totalList.size() + "笔)" + System.lineSeparator());
        stringBuffer.append(totalStatStr);

        sendReplyMessage(robotsMch.getChatId(), messageId, stringBuffer.toString());
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
        return robotsMch != null && StringUtils.isNotEmpty(robotsMch.getMchNo()) && !robotsMch.getMchNo().equals(CS.ROBOTS_MGR_MCH);
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
            case BLIND_MGR_CONFIRM:
                //商户群跟管理群不可重复绑定
                int count = robotsMchService.count(RobotsMch.gw().eq(RobotsMch::getChatId, chatId).ne(RobotsMch::getMchNo, CS.ROBOTS_MGR_MCH));
                if (count != 0) {
                    sendSingleMessage(chatId, "当前群已绑定为商户群，不可重复绑定");
                } else {
                    //blind mgr
                    RobotsMch robotsMch = new RobotsMch();
                    robotsMch.setChatId(chatId);
                    robotsMch.setMchNo(CS.ROBOTS_MGR_MCH);
                    if (robotsMchService.count(RobotsMch.gw().eq(RobotsMch::getMchNo, CS.ROBOTS_MGR_MCH)) != 0) {
                        robotsMchService.update(robotsMch, RobotsMch.gw().eq(RobotsMch::getMchNo, CS.ROBOTS_MGR_MCH));
                    } else {
                        robotsMchService.save(robotsMch);
                    }
                    EditMessageText editMessageText = new EditMessageText();
                    editMessageText.setChatId(chatId);
                    editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
                    editMessageText.setText("管理群绑定成功!"); // 将消息内容设置为空字符串，相当于删除消息
                    sendSingleMessage(editMessageText);
                }
                break;
            case BLIND_MGR_CANCEL:
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
                editMessageText.setText("取消操作"); // 将消息内容设置为空字符串，相当于删除消息
                sendSingleMessage(editMessageText);
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
            log.info("========RobotsService Initializing========");
        }
    }

    @Override
    public void onRegister() {
        // Define the commands to set
//        List<BotCommand> commands = new ArrayList<>();
//        commands.add(new BotCommand("help", "机器人帮助说明"));
//
//        // Create the SetMyCommands request
//        SetMyCommands setMyCommands = new SetMyCommands();
//        setMyCommands.setCommands(commands);
//
//        try {
//            execute(setMyCommands);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
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

    private static final int WARNING_COUNT = 5;

    /**
     * 强制补单查询
     */
    @Scheduled(fixedRate = 60000) // 每60秒执行一次
    public void forceOrderCheck() {
        Date nowTime = new Date();
        Date offsetDate = DateUtil.offsetMinute(nowTime, -1);

        //发警报
        RobotsMch robotsMch = robotsMchService.getManageMch();
        if (robotsMch != null) {

            LambdaQueryWrapper<PayOrder> lambdaQueryWrapper = PayOrder.gw().eq(PayOrder::getForceChangeState, CS.YES).le(PayOrder::getSuccessTime, nowTime).ge(PayOrder::getSuccessTime, offsetDate);
            List<PayOrder> list = payOrderService.list(lambdaQueryWrapper);
            int count = list.size();
            if (count >= WARNING_COUNT) {
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


    /**
     * 额度检查
     */
    @Scheduled(fixedRate = 5000) // 每60秒执行一次
    public void quotaCheck() {
        RobotsMch robotsMch = robotsMchService.getManageMch();
        if (robotsMch != null) {
//            sendSingleMessage(robotsMch.getChatId(), "test mgr");
        }
    }

    /**
     * 通道配置修改检测
     */
    @Scheduled(fixedRate = 60000) // 每60秒执行一次
    public void passageConfigCheck() {

        String REDIS_SUFFIX = "Passage_Pay_Config";
        List<PayPassage> list = new ArrayList<>();
        Long cacheSize = RedisUtil.getQueueLength(REDIS_SUFFIX);
        if (cacheSize.intValue() == 0) {
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
}