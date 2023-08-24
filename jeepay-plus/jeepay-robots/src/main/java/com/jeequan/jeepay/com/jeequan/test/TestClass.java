package com.jeequan.jeepay.com.jeequan.test;

import cn.hutool.core.date.DateUtil;
import com.jeequan.jeepay.core.entity.RobotsMch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class TestClass {

    public static void main(String[] args) {
        //查单 \w+
        //下发\s+([-+]?\d+)
        String ADD_BILL = "下发\\s+([-+]?\\d+)";
//        Pattern patternBlindMch = Pattern.compile(ADD_BILL);
//        Matcher matcherBlindMch = patternBlindMch.matcher("查单");
        String text = "下发 -50";
        Pattern patternAddBill = Pattern.compile(ADD_BILL);
        Matcher matcherAddBill = patternAddBill.matcher("下发 -50");

        log.info(matcherAddBill.find()+"");
        log.info(text.substring(3));


        String test="1005,5006,5005,";
        String [] tests = test.split(",");
        log.info(tests.length+"");
    }

}
