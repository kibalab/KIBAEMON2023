package com.comduck.chatbot.discord.api.hitomiapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zakgof.webp4j.Webp4j;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HitomiLoader extends Thread{
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.0.0";

    public static boolean isUpdating = false;

    public static boolean gg_condition;
    public static String gg_uuid;
    public static ArrayList conditionList;

    public final String GalleryId;
    public final Consumer<Integer> Callback;
    public final Consumer<List<byte[]>> EndCallback;

    public HitomiLoader(String galleryId, Consumer<Integer> callback, Consumer<List<byte[]>> endCallback)
    {
        GalleryId = galleryId;
        Callback = callback;
        EndCallback = endCallback;
    }

    public static void UpdateKey(String GalleryId) throws IOException {
        System.out.println("Update Key Data");
        isUpdating = true;
        var gg = RequestHttpContent(String.format("https://ltn.hitomi.la/gg.js", GalleryId));

        var lines = gg.lines().toList();

        var ggPattern = Pattern.compile("(case)\\s([0-9]*)");
        Matcher matcher = ggPattern.matcher(gg);
        var results = matcher.results().toList();
        conditionList = new ArrayList<Integer>();
        for (int i = 0; i < results.size(); i++) {
            conditionList.add(Integer.parseInt(results.get(i).group().replace("case ", "")));
        }
        gg_condition = lines.get(3).replace("var o = ", "").equals("1");

        gg_uuid = lines.get(lines.size() - 2).replace("b: '", "").replace("/'", "");
        isUpdating = false;
    }

    public static String RequestHttpContent(String Url) throws IOException {
        URL url_gg = new URL(Url);

        URLConnection connection_gg = url_gg.openConnection();
        connection_gg.setRequestProperty("User-Agent", USER_AGENT);
        var istream = connection_gg.getInputStream();
        var ireader = new InputStreamReader(istream, "UTF-8");
        var ibuffer = new BufferedReader(ireader);

        String line = "";
        StringBuffer sbuf = new StringBuffer();
        while((line=ibuffer.readLine()) != null) {
            sbuf.append(line + "\r\n");
        }

        return sbuf.toString();
    }

    @Override
    public void run() {

        /*
        https://aa.hitomi.la/webp/1712570401/3978/aa938ead2942d91052592471ebe6016311462de17342f026c536fae7b5eb18af.webp

        가 = 라의 16진수를 https://ltn.hitomi.la/gg.js 의 m 함수에 전달 및 실행해서 o + 97을 아스키코드로 문자화 (o와 m함수의 case는 주기적으로 변동됨)
        나 = 확장자 (avif, webp)
        다 = https://ltn.hitomi.la/gg.js 의 변수 b (b는 주기적으로 변동됨) // 1712570401
        라 = 마의 마지막 3문자 {..}{.}의 순서를 뒤집어 {.}{..}로 한뒤 16진수를 10진수로 변환 // 3978
        마 = https://ltn.hitomi.la/galleries/갤러리ID.js 의 이미지 Hash ID // aa938ead2942d91052592471ebe6016311462de17342f026c536fae7b5eb18af
         */

        try {
            var galleryPage = String.format("https://hitomi.la/reader/%s.html", GalleryId);

            var galleryInfo = (JSONObject) JSON.parse(RequestHttpContent(String.format("https://ltn.hitomi.la/galleries/%s.js", GalleryId)).replace("var galleryinfo = ", ""));

            UpdateKey(GalleryId);

            var files = galleryInfo.getJSONArray("files");

            var imageCollection = new AtomicReference<ArrayList<byte[]>>(new ArrayList<byte[]>());

            var threadSlot = new ArrayList<Thread>();

            for (int i = 0; i < files.size(); i++) {
                imageCollection.get().add(null);
                HitomiDownloadThread myRunnable = new HitomiDownloadThread(i, (JSONObject) files.get(i), galleryPage, GalleryId, imageCollection);
                myRunnable.start();
                threadSlot.add(myRunnable);

                for (int j = 0; j < threadSlot.size();) {
                    var t = threadSlot.get(j);
                    if (!t.isAlive()) {
                        threadSlot.remove(t);
                    }
                    else j++;
                }
                if(i % 7 == 0) Callback.accept(i);

                if(threadSlot.size() > 3) sleep(1000);
            }

            while(true)
            {
                for (int j = 0; j < threadSlot.size();) {
                    var t = threadSlot.get(j);
                    if (!t.isAlive()) {
                        threadSlot.remove(t);
                    }
                    else j++;
                }

                if(threadSlot.isEmpty()) break;
            }

            EndCallback.accept(imageCollection.get());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
