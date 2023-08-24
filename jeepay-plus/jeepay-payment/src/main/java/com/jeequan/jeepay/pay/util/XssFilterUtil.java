package com.jeequan.jeepay.pay.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
public class XssFilterUtil {

    private static final Whitelist whitelist = Whitelist.basicWithImages();

    private static final Document.OutputSettings outputSettings = (new Document.OutputSettings()).prettyPrint(false);

    public static String clean(String content) {
        return Jsoup.clean(content, "", whitelist, outputSettings);
    }
    public static String[] excludes = new String[]{"/api/qrcode_img_get"};

    public static boolean isHandXss(String uri) {
        for (int i = 0; i < excludes.length; i++) {
            if (uri.indexOf(excludes[i]) >= 0)
                return false;
        }
        return true;
    }

    static {
        whitelist.addAttributes(":all", new String[] { "style" });
    }
}
