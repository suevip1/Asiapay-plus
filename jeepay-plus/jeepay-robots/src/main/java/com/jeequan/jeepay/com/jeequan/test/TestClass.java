package com.jeequan.jeepay.com.jeequan.test;

import cn.hutool.core.date.DateUtil;
import com.jeequan.jeepay.core.entity.RobotsMch;
import com.jeequan.jeepay.core.utils.AmountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class TestClass {

    public static void main(String[] args) {
        String input = "绑定通道 1002";
//        String input = "绑定通道 1002,1003,1004";

        String regex = "绑定通道\\s+([\\d,]+)";  // 正则表达式

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        log.info(matcher.matches() + "");
        log.info(matcher.find() + "");
    }

}
