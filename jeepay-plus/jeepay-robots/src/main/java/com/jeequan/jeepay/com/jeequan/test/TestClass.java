package com.jeequan.jeepay.com.jeequan.test;

import com.jeequan.jeepay.core.entity.RobotsMch;
import com.jeequan.jeepay.core.utils.AmountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class TestClass {

    public static void main(String[] args) {
        Pattern regex = Pattern.compile("[Zz]{2}\\s.*");
        Matcher matcher = regex.matcher("ZZ测试加急");
        log.info(matcher.matches() + "");

    }

}
