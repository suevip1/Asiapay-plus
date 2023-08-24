package com.jeequan.jeepay.mgr.ctrl.config.data;

import lombok.Data;

@Data
public class PassageStat {
    private String passageName;
    private Long successAmount;
    private int allCount;
    private int successCount;
}