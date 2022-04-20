package com.dream.team.ourcoolrepo.client;

import com.roxstudio.utils.CUrl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CoolClient {
    public static void main(String[] args) {
        String mobileUserAgent = "Mozilla/5.0 (Linux; U; Android 8.0.0; zh-cn; KNT-AL10 Build/HUAWEIKNT-AL10) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) MQQBrowser/7.3 Chrome/37.0.0.0 Mobile Safari/537.36";
        Map<String, String> fakeAjaxHeaders = new HashMap<String, String>();
        fakeAjaxHeaders.put("X-Requested-With", "XMLHttpRequest");
        fakeAjaxHeaders.put("Referer", "http://localhost:8081/");
        CUrl curl = new CUrl("http://localhost:8081/awesomescript.js")
                .opt("-A", mobileUserAgent) // simulate a mobile browser
                .headers(fakeAjaxHeaders)   // simulate an AJAX request
                .header("X-Auth-Token: xxxxxxx"); // other custom header, this might be calculated elsewhere
        System.out.println(Arrays.toString(curl.exec()));

    }
}
