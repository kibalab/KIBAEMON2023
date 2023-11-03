package com.comduck.chatbot.discord.commands.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class WebUtil {


    //URL여부 확인
    static public boolean isURL(String Url) {
        try {
            new URL(Url);
            return true;

        } catch (MalformedURLException e) {
            return false;
        }
    }

    //유튜브 검색
    static public ArrayList<String> searchYoutube(String title) {
        //유튜브 검색 경로 지정, 빈 해쉬맵 생성
        String youtubeUrl = "https://www.youtube.com/results?search_query=";
        ArrayList<String> video = new ArrayList<String>();
        System.out.println("[searchYoutube] Ready : " + youtubeUrl+title);
        //1. URL + title 로 검색하여 영상 제목을 전부 가져옴
        //2. video 해쉬맵에 하나씩 담아서 반환
        try {
            Document doc = Jsoup.connect(youtubeUrl+title).get();
            Elements titleE = doc.getElementsByTag("a");
            for(int i=0, j=0; titleE.size()> i; i++) {
                Element data = titleE.get(i);
                System.out.println("\n[" + i + "]TestParse: " + data.text() + "\n" +data.attr("href"));
                if( data.id().contains("video-title") && !data.attr("href").contains("http")) {

                    video.add("https://www.youtube.com" + data.attr("href"));
                    j++;
                }
            }
            return video;
        } catch (Exception e) {
            System.out.println(e);
            return video;
        }
    }
}
