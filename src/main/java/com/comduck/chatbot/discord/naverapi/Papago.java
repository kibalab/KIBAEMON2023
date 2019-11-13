package com.comduck.chatbot.discord.naverapi;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Papago {

    private String APIclientId;//애플리케이션 클라이언트 아이디값";
    private String APIclientSecret;//애플리케이션 클라이언트 시크릿값";

    public Papago() {
        this.APIclientId = "aEDY5kTcPgBZelyX9f9W";//애플리케이션 클라이언트 아이디값";
        this.APIclientSecret = "GW1G088Juf";//애플리케이션 클라이언트 시크릿값";
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
            JSONObject obj1 = (JSONObject) p.parse(obj.get("message").toString());
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
        }
        return code;
    }

    private String checkSourceLng(String str) {
        try {
            String query = URLEncoder.encode(str, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/papago/detectLangs";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Naver-Client-Id", APIclientId);
            con.setRequestProperty("X-Naver-Client-Secret", APIclientSecret);
            // post request
            String postParams = "query=" + query;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
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

    private String translate(String str, String sourceLng, String targetLang) {
        try {
            String text = URLEncoder.encode(str, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Naver-Client-Id", APIclientId);
            con.setRequestProperty("X-Naver-Client-Secret", APIclientSecret);
            // post request
            String postParams = String.format("source=%s&target=%s&text=", sourceLng, targetLang) + text;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
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