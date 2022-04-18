package com.dream.team.ourcoolrepo.client;

import com.roxstudio.utils.CUrl;

import java.util.Arrays;

public class CoolClient {
    public static void main(String[] args) {
        CUrl cUrl = new CUrl("localhost:8080");
        cUrl.output("http://localhost:8080/");
        System.out.println(Arrays.toString(cUrl.exec()));
    }
}
