package com.comduck.chatbot.discord.api.naverapi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;

public class GetNaver {
    private String APIclientId;//애플리케이션 클라이언트 아이디값";
    private String APIclientSecret;//애플리케이션 클라이언트 시크릿값";

    public GetNaver() {
        this.APIclientId = "aEDY5kTcPgBZelyX9f9W";//애플리케이션 클라이언트 아이디값";
        this.APIclientSecret = "GW1G088Juf";//애플리케이션 클라이언트 시크릿값";
    }

    public String get (String apiURL, LinkedHashMap<String, String> parms) {
        String postParams = "";
        Object[] keys = parms.keySet().toArray();
        for(Object key : keys){
            postParams += key.toString() + "=" + parms.get(key);
            if(!key.equals(keys[keys.length-1])){
                postParams += "&";
            }
        }
        String r = getData(apiURL, postParams);

        System.out.println(r);
        return r;
    }

    private String getData (String apiURL, String parms) {

        try {
            //String apiURL = "https://openapi.naver.com/v1/search/blog.xml?query="+ text; // xml 결과
            URL url = new URL(apiURL+parms);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", APIclientId);
            con.setRequestProperty("X-Naver-Client-Secret", APIclientSecret);
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            return response.toString();
        } catch (Exception e) {
            System.out.println(e);
        }
        return "";
    }
}
