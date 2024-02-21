package com.comduck.chatbot.discord.audiocore;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class YoutubeParse {


    private String youtubeURL;

    void YoutubeParse() { }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
}
        return sb.toString();
                }

    private JSONObject parse(String id) {
        String src = String.format("https://www.youtube.com/oembed?url=https://youtu.be/%s&format=json", id);

        try {
            InputStream is = new URL(src).openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            JSONObject j = (JSONObject) new JSONParser().parse(jsonText);
            j.put("id", id);
            return j;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public JSONObject getVideo(String id) {
        return parse(id);
    }

}
