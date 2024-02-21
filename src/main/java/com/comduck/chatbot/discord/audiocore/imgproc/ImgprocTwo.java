package com.comduck.chatbot.discord.audiocore.imgproc;

import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImgprocTwo {
    public ImgprocTwo() {

    }

    public File processImage(JSONObject details, File canvasFile, URL requesterIconFile, URL uploaderIconFile, String requester) {
        try {
            String id = (String) details.get("id");
            String uploader = (String) details.get("author_name");
            String title = (String) details.get("title");
            String thumbUrl = (String) details.get("thumbnail_url");


            //Load Image

            BufferedImage image = ImageIO.read(canvasFile);
            boolean thumbIsSd = false;
            BufferedImage thum = null;

            BufferedImage uicon = null;
            for(int i=0; i<5; i++) { // 최대 5번 시도
                if (uicon == null) { // 가끔 이미지를 한번에 못가져 오는경우가 있어서 만듬
                    System.out.println("[ImageProcess2] UploaderIcon : " + uploaderIconFile.toString());
                    uicon = ImageIO.read(uploaderIconFile.openConnection().getInputStream());
                } else { break; }
            }
            URLConnection uc = requesterIconFile.openConnection();
            uc.addRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            BufferedImage ricon = ImageIO.read(uc.getInputStream());

            BufferedImage overlay = ImageIO.read(new File(
                    "shadow.png"));

            BufferedImage info = ImageIO.read(new File(
                    "info.png"));

            try {
                if (thum == null) {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "maxresdefault"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());

                }
                if (thum == null) {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "maxresdefault").replace("i.ytimg", "i1.ytimg"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());

                }
                if (thum == null) {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "maxresdefault").replace("i.ytimg", "i2.ytimg"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());

                }
                if (thum == null) {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "maxresdefault").replace("i.ytimg", "i3.ytimg"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                }
                if (thum == null) {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "maxresdefault").replace("i.ytimg", "i4.ytimg"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                }
                if (thum == null) {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "maxresdefault").replace("i.ytimg", "img.youtube"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                }
                if (thum == null) {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "sddefault"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                    thumbIsSd = true;
                }
                if (thum == null) {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "sddefault").replace("i.ytimg", "i1.ytimg"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                    thumbIsSd = true;
                }
                if (thum == null) {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "sddefault").replace("i.ytimg", "i2.ytimg"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                    thumbIsSd = true;
                }
                if (thum == null) {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "sddefault").replace("i.ytimg", "i3.ytimg"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                    thumbIsSd = true;
                }
                if (thum == null) {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "sddefault").replace("i.ytimg", "i4.ytimg"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                    thumbIsSd = true;
                }
                if (thum == null) {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "sddefault").replace("i.ytimg", "img.youtube"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                    thumbIsSd = true;
                }
                if (thum == null) {
                    File thumbnailFile = new File("./NotFound.png");
                    thum = ImageIO.read(thumbnailFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            //그래픽 생성
            Graphics2D g = (Graphics2D) image.getGraphics();

            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            //Thumnail Image
            g.setClip(new RoundRectangle2D.Float(14, 16, 1256, 184, 90, 90));
            g.drawImage(image, 14, 16, 1256, 184, null);
            //g.drawImage(thum, 14, 16, 1256, 1256/thum.getWidth() * thum.getHeight(), null);
            g.drawImage(thum, 14, -250, 1256, 706, null);
            g.drawImage(overlay, 14, 16, 1256, 184, null);
            g.drawImage(info, 14+25, 16+75, 838, 81, null);

            //Uploader Name TEXT
            //폰트 설정
            g.setFont(g.getFont().deriveFont(43f));
            //문자열 그림
            g.drawString(uploader, 14+120, 16+140);

            //Requster Name TEXT
            g.setFont(g.getFont().deriveFont(43f));
            g.drawString(requester, 14+695, 16+140);

            //Video Title TEXT
            g.setFont(g.getFont().deriveFont(54f));
            g.drawString(title, 14+38, 16+60);

            //Uploader Profile Image
            //클립생성(마스크 모양 생성)
            g.setClip(new Ellipse2D.Float(40, 92, 82, 82));
            //클립에 이미지 그림
            g.drawImage(uicon, 40, 92, 82, 82, null);

            //Requester Profile Image
            g.setClip(new Ellipse2D.Float(612, 91, 82, 82));
            g.drawImage(ricon, 612, 91, 82, 82, null);


            //Edit close
            g.dispose();

            //Save Image
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssZ");
            File outFile = new File(String.format("%s.png", dateFormat.format(new Date())));

            ImageIO.write(image, "png", outFile);

            return outFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return canvasFile;
    }
}
