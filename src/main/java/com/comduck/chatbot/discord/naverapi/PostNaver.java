package com.comduck.chatbot.discord.naverapi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

    public class PostNaver {

        private String APIclientId;//애플리케이션 클라이언트 아이디값";
        private String APIclientSecret;//애플리케이션 클라이언트 시크릿값";

        public PostNaver() {
            this.APIclientId = "aEDY5kTcPgBZelyX9f9W";//애플리케이션 클라이언트 아이디값";
            this.APIclientSecret = "GW1G088Juf";//애플리케이션 클라이언트 시크릿값";
        }

        public String post (String apiURL, LinkedHashMap<String, String> parms) {
            String postParams = "";
            Object[] keys = parms.keySet().toArray();
            for(Object key : keys){
                postParams += key.toString() + "=" + parms.get(key);
                if(!key.equals(keys[keys.length-1])){
                    postParams += "&";
                }
            }
            String r = postData(apiURL, postParams);
            return r;
        }

        private String postData (String apiURL, String parms) {

            try {
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", APIclientId);
                con.setRequestProperty("X-Naver-Client-Secret", APIclientSecret);
                // post request
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(parms);
                wr.flush();
                wr.close();
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
