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
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HitomiLoader extends Thread{
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.0.0";

    public final String GalleryId;
    public final Consumer<List<FileUpload>> Callback;

    public HitomiLoader(String galleryId, Consumer<List<FileUpload>> callback)
    {
        GalleryId = galleryId;
        Callback = callback;
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
            var gg = RequestHttpContent(String.format("https://ltn.hitomi.la/gg.js", GalleryId));

            var lines = gg.lines().toList();

            var ggPattern = Pattern.compile("(case)\\s([0-9]*)");
            Matcher matcher = ggPattern.matcher(gg);
            var results = matcher.results().toList();
            var conditionList = new ArrayList<Integer>();
            for (int i = 0; i < results.size(); i++) {
                conditionList.add(Integer.parseInt(results.get(i).group().replace("case ", "")));
            }
            var gg_condition = lines.get(3).replace("var o = ", "").equals("1");

            var gg_uuid = lines.get(lines.size() - 2).replace("b: '", "").replace("/'", "");

            var files = galleryInfo.getJSONArray("files");

            List images = new ArrayList<FileUpload>();
            for (int i = 0; i < files.size(); i++) {
                var file = (JSONObject) files.get(i);
                var imageName = file.getString("name");
                var hashId = file.getString("hash");
                var format = "webp";
                format = file.getIntValue("hasavif") > 0 ? "avif" : "";
                format = file.getIntValue("haswebp") > 0 ? "webp" : "avif";
                var hashId_key = hashId.substring(hashId.length() - 3, hashId.length());

                hashId_key = String.format("%c%c%c", hashId_key.charAt(2), hashId_key.charAt(0), hashId_key.charAt(1));
                var key = Integer.parseInt(hashId_key, 16); // 라

                System.out.println(String.format("[%d] %s", i, hashId));
                System.out.println(String.format("key : %s | %d", String.valueOf(hashId_key), key));

                var gg_subdomain = gg_condition ? 'a' : 'b';
                ;
                if (conditionList.contains(key)) {
                    gg_subdomain = !gg_condition ? 'a' : 'b';
                }

                URL url = new URL(String.format("https://%ca.hitomi.la/%s/%s/%s/%s.%s", gg_subdomain, format, gg_uuid, key, hashId, format));


                HttpURLConnection imageCon = (HttpURLConnection) url.openConnection();
                imageCon.setRequestProperty("User-Agent", USER_AGENT);
                imageCon.setRequestProperty("Sec-Fetch-Mode", "navigate");
                imageCon.setRequestProperty("Referer", galleryPage);
                imageCon.setRequestProperty("Referrer-Policy", "no-referrer-when-downgrade");
                imageCon.setRequestMethod("GET");
                InputStream in = imageCon.getInputStream();
                System.out.println(imageName);

                BufferedImage imageByte = Webp4j.decode(in.readAllBytes());

                var image = FileUpload.fromData(toByteArray(imageByte, "jpg"), imageName);
                images.add(image);

                if(images.size() >= 10)
                {
                    Callback.accept(images);
                    images.clear();
                }
            }
            Callback.accept(images);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public static byte[] toByteArray(BufferedImage bi, String format)
            throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, format, baos);
        byte[] bytes = baos.toByteArray();
        return bytes;

    }

    public String RequestHttpContent(String Url) throws IOException {
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
}
