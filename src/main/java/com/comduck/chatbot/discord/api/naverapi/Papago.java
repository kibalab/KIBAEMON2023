package com.comduck.chatbot.discord.api.naverapi;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URLEncoder;
import java.util.LinkedHashMap;

public class Papago {

        public Papago() {
        }

        public String[] manager(String str) {
            String[] dataLaw = str.split(" ", 2);
            String sourceLng = checkSourceLng(dataLaw[1]).replace("{\"langCode\":\"", "").replace("\"}", "");
            String targetLng = contryCodeFix(dataLaw[0]);
            String jsonResult = translate(dataLaw[1], sourceLng, targetLng);
            String r = jsonGetMessage(jsonResult);
            dataLaw[0] = contryCodeFixForDISCORD(dataLaw[0]);
            sourceLng = contryCodeFixForDISCORD(sourceLng);
            String[] data = {r, sourceLng, dataLaw[0]};
            return data;
        }

        private String jsonGetMessage(String str) {
            try {
                JSONParser p = new JSONParser();
                JSONObject obj = (JSONObject) p.parse(str);
                JSONObject obj1 =  (JSONObject)p.parse(obj.get("message").toString());
                JSONObject obj2 = (JSONObject) p.parse(obj1.get("result").toString());
                System.out.println(obj2.toString());
                return obj2.get("translatedText").toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return str;
        }

    private String contryCodeFix(String code) {
        code = code.toLowerCase();
        if(code.equals("jp")){
            code = "ja";
        } else if(code.equals("zh") || code.equals("cn")){
            code = "zh-CN";
        }
        return code;
    }

    private String contryCodeFixForDISCORD(String code) {
        code = code.toLowerCase();
        if(code.equals("ja")){
            code = "jp";
        } else if(code.equals("ko")){
            code = "kr";
        } else if(code.equals("zh") || code.equals("cn")){
            code = "cn";
        } else if(code.equals("en")){
            code = "us";
        }
        return code;
    }

    private String checkSourceLng(String str) {
        String r = "No Data";
        String apiURL = "https://openapi.naver.com/v1/papago/detectLangs";
        try {
            LinkedHashMap<String, String> parms = new LinkedHashMap<String, String>();
            String text = URLEncoder.encode(str, "UTF-8");
            parms.put("query", text);
            PostNaver postNaver = new PostNaver();
            r = postNaver.post(apiURL, parms);
        } catch (Exception e){
            e.printStackTrace();
        }
        return r;
    }

    private String translate(String str, String sourceLng, String targetLang) {
        String r = "No Data";
        String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
        try {
            LinkedHashMap<String, String> parms = new LinkedHashMap<String, String>();
            String text = URLEncoder.encode(str, "UTF-8");
            parms.put("source", sourceLng);
            parms.put("target", targetLang);
            parms.put("text", text);
            PostNaver postNaver = new PostNaver();
            r = postNaver.post(apiURL, parms);
        } catch (Exception e){
            e.printStackTrace();
        }
        return r;
    }
}