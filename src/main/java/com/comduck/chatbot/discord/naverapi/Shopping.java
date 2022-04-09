package com.comduck.chatbot.discord.naverapi;


import net.dv8tion.jda.api.EmbedBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;

public class Shopping {
    public Shopping() {

    }

    public EmbedBuilder manager(String str) {
        String apiURL = "https://openapi.naver.com/v1/search/shop?";
        GetNaver get = new GetNaver();

        LinkedHashMap data = new LinkedHashMap();
        String text = null;
        try {
            System.out.println(str);
            text = URLEncoder.encode(str, "UTF-8");
            data.put("query", text);
            data.put("display", "100");
            data.put("start", "1");
            data.put("sort", "sim");
        } catch (Exception e) {
            e.printStackTrace();
        }


        String result = get.get(apiURL, data);


        try {
            return BuildEmbed(jsonGetMessage(result));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private JSONArray jsonGetMessage(String str) {
        try {
            JSONParser p = new JSONParser();
            JSONObject obj = (JSONObject) p.parse(str);
            JSONArray obj1 =  (JSONArray)p.parse(obj.get("items").toString());
            return obj1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private EmbedBuilder BuildEmbed(JSONArray data) throws UnsupportedEncodingException {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(new Color(0x1FFF2A));
        JSONParser p = new JSONParser();
        DecimalFormat f = new DecimalFormat("###,###,###");
        if(data.size() <= 0) {
            eb.setAuthor("네이버쇼핑 내 검색 결과가 없습니다.", "https://shopping.naver.com/", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSEyTRrEubfpkk2mYj8FIbNuDg8ktxkcrLoI-7Rb4SpsUtMrSvs&s");
            eb.addField("Notice", "정확한 검색어 인지 확인하시고 다시 검색해 주세요.", false);
        }
        JSONObject jsonData = (JSONObject) data.get(0);
        String title = jsonData.get("title").toString().replace("<b>", "").replace("</b>", "");
        String img = jsonData.get("image").toString();
        String link = jsonData.get("link").toString();
        String lowPrice = jsonData.get("lprice").toString();
        int highPrice = 0;

        for(int i=0; i<data.size(); i++) {
            jsonData = (JSONObject) data.get(i);
            int price = Integer.parseInt(jsonData.get("lprice").toString());
            if(highPrice < price){
                highPrice = price;
            }
        }

        System.out.println(String.format("[Shopping] Find Price %s~%s", lowPrice, highPrice));

        String price = String.format("%s ~ %s", lowPrice, f.format(highPrice));

        eb.setImage(img);
        eb.setAuthor(title, link, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSEyTRrEubfpkk2mYj8FIbNuDg8ktxkcrLoI-7Rb4SpsUtMrSvs&s");
        eb.addField("Price", price, false);
        return eb;
    }
}











