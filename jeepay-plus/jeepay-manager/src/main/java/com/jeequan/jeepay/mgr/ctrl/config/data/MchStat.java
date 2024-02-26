package com.jeequan.jeepay.mgr.ctrl.config.data;

import lombok.Data;

@Data
public class MchStat {
    private String mchName;
    private int allCount;
    private int perMinCount;
}