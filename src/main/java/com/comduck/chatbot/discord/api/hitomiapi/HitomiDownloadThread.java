package com.comduck.chatbot.discord.api.hitomiapi;

import com.alibaba.fastjson.JSONObject;
import com.zakgof.webp4j.Webp4j;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class HitomiDownloadThread extends Thread {
    public int index;
    public JSONObject file;
    public String galleryPage;
    public String galleryId;
    public AtomicReference<ArrayList<byte[]>> imageCollection;

    private char gg_subdomain = 'a';
    private String format = "avif";
    private String hashId;
    private String hashId_key;
    private Integer key;

    public HitomiDownloadThread(int i, JSONObject file, String galleryPage, String galleryId, AtomicReference<ArrayList<byte[]>> imageCollection)
    {
        this.index = i;
        this.file = file;
        this.galleryPage = galleryPage;
        this.imageCollection = imageCollection;
        this.galleryId = galleryId;
    }

    @Override
    public void run() {
        var imageName = file.getString("name");
        hashId = file.getString("hash");
        format = file.getIntValue("haswebp") > 0 ? "webp" : "avif";
        hashId_key = hashId.substring(hashId.length() - 3, hashId.length());

        hashId_key = String.format("%c%c%c", hashId_key.charAt(2), hashId_key.charAt(0), hashId_key.charAt(1));
        key = Integer.parseInt(hashId_key, 16); // 라

        gg_subdomain = HitomiLoader.gg_condition ? 'a' : 'b';
        if (HitomiLoader.conditionList.contains(key)) {
            gg_subdomain = !HitomiLoader.gg_condition ? 'a' : 'b';
        }

        while(HitomiLoader.isUpdating)
        {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        String url = String.format("https://%ca.hitomi.la/%s/%s/%s/%s.%s", gg_subdomain, format, HitomiLoader.gg_uuid, key, hashId, format);


        HttpURLConnection imageCon = ConnectImage(url, galleryPage);

        try {

            InputStream in = GetImageStream(imageCon, url);

            BufferedImage imageByte = Webp4j.decode(in.readAllBytes());

            var bytes = toByteArray(imageByte, "jpg");

            System.out.println(imageName);
            imageCollection.get().set(index, bytes);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public InputStream GetImageStream(HttpURLConnection imageCon, String url) throws InterruptedException {

        try {
            InputStream in = null;
            switch (imageCon.getResponseCode()) {
                case HttpURLConnection.HTTP_OK:
                    in = imageCon.getInputStream();
                    break;
                case HttpURLConnection.HTTP_UNAVAILABLE:
                    if (HitomiLoader.isUpdating) {
                        while (HitomiLoader.isUpdating) {
                            try {
                                sleep(100);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } else HitomiLoader.UpdateKey(galleryId);
                    url = String.format("https://%ca.hitomi.la/%s/%s/%s/%s.%s", gg_subdomain, format, HitomiLoader.gg_uuid, key, hashId, format);
                    imageCon = ConnectImage(url, galleryPage);
                    if(imageCon.getResponseCode() == HttpURLConnection.HTTP_UNAVAILABLE) {
                        if (format.equals("webp"))
                            format = "avif";
                        else format = "webp";
                        url = String.format("https://%ca.hitomi.la/%s/%s/%s/%s.%s", gg_subdomain, format, HitomiLoader.gg_uuid, key, hashId, format);
                        imageCon = ConnectImage(url, galleryPage);
                        return GetImageStream(imageCon, url);
                    }
                    else{
                        in = imageCon.getInputStream();
                        break;
                    }
                case HttpURLConnection.HTTP_NOT_FOUND:
                    if (gg_subdomain == 'a')
                        gg_subdomain = 'b';
                    else gg_subdomain = 'a';
                    url = String.format("https://%ca.hitomi.la/%s/%s/%s/%s.%s", gg_subdomain, format, HitomiLoader.gg_uuid, key, hashId, format);
                    imageCon = ConnectImage(url, galleryPage);
                    in = imageCon.getInputStream();
                    break;
                case HttpURLConnection.HTTP_BAD_GATEWAY:
                    for (int i = 0; i < 5; i++) {
                        if (in != null) break;
                        in = imageCon.getInputStream();
                        sleep(1000);
                    }
                    break;
                default:
                    System.out.println("[ERROR!]" + imageCon.getResponseCode());
                    break;
            }
            return in;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public HttpURLConnection ConnectImage(String url, String galleryPage)
    {
        HttpURLConnection imageCon = null;
        try {
            imageCon = (HttpURLConnection) new URL(url).openConnection();
            imageCon.setRequestProperty("User-Agent", HitomiLoader.USER_AGENT);
            imageCon.setRequestProperty("Sec-Fetch-Mode", "navigate");
            imageCon.setRequestProperty("Referer", galleryPage);
            imageCon.setRequestProperty("Referrer-Policy", "no-referrer-when-downgrade");
            imageCon.setRequestMethod("GET");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return imageCon;
    }


    public static byte[] toByteArray(BufferedImage bi, String format)
            throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, format, baos);
        byte[] bytes = baos.toByteArray();
        return bytes;

    }
}