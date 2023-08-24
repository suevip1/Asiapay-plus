package com.jeequan.jeepay.core.utils;

import java.util.Comparator;

class MapKeyComparator implements Comparator<String> {
    public int compare(String str1, String str2) {
        return str1.compareTo(str2);
    }
}

class MapKeyDescComparator implements Comparator<String> {
    public int compare(String str1, String str2) {
        return str2.compareTo(str1);
    }
}
