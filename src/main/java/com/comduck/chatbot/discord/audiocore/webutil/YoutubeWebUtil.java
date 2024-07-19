package com.comduck.chatbot.discord.audiocore.webutil;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.model.YoutubeVideo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeWebUtil {

    static public URL getUserImage(String channel) {
        //유튜브 검색 경로 지정, 빈 해쉬맵 생성
        Document doc2 = null;
        //1. URL + title 로 검색하여 영상 제목을 전부 가져옴
        //2. video 해쉬맵에 하나씩 담아서 반환
        try {
            doc2 = Jsoup.connect(channel).get();

            Element imageE = doc2.body().select("link[rel^=image_]").first();
            var image = imageE.attr("href");

            if (image != null) {
                return new URL(image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static public String[] getTrackTags(String videoIdentifier) {
        List<String> tags = new ArrayList<String>();
        try {
            YoutubeDownloader yd = new YoutubeDownloader();
            YoutubeVideo video = yd.getVideo(videoIdentifier);
            String dsc = video.details().description();

            Pattern p = Pattern.compile("(#(\\S)*)");
            Matcher m = p.matcher(dsc);

            while (m.find()) {
                tags.add(m.group());
            }
        } catch (Exception e) {
            System.err.println("[YoutubeWebUtil] Failed Load Tag list");
        }
        var size = tags.size();
        return tags.toArray(new String[size]);
    }
}
