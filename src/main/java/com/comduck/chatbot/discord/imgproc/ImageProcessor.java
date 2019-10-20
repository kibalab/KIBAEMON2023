package com.comduck.chatbot.discord.imgproc;

import com.github.kiulian.downloader.model.VideoDetails;
import com.luciad.imageio.webp.WebPReadParam;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ImageProcessor {

    // 예시 코드
    // ImageProcessor processor = new ImageProcessor();
    // File outFile = processor.processImage(new File("testImage.png"), new File("testThum.png"), new File("iconTest.png"));
    // outFile 전송 후 파일 삭제
    // outFile.delete();

   public ImageProcessor() {

    }

    private BufferedImage cropImage(BufferedImage src, Rectangle rect) {
        System.out.println(src.getData().getBounds());
        return src.getSubimage(rect.x, rect.y, rect.width, rect.height);
    }

    public File processImage(VideoDetails details, File canvasFile, URL requesterIconFile, URL uploaderIconFile, String requester) {
        try {

            String id = details.videoId();
            String uploader = details.author();
            String title = details.title();

            System.out.println(details.thumbnails());
            String thumbUrl = details.thumbnails().get(details.thumbnails().size()-1);




            //Load Image
            BufferedImage image = ImageIO.read(canvasFile);
            boolean thumbIsSd = false;
            BufferedImage thum = null;

            BufferedImage uicon = null;
            for(int i=0; i<5; i++) { // 최대 5번 시도
                if (uicon == null) { // 가끔 이미지를 한번에 못가져 오는경우가 있어서 만듬
                    uicon = ImageIO.read(uploaderIconFile.openConnection().getInputStream());
                } else { break; }
            }

            URLConnection uc = requesterIconFile.openConnection();
            uc.addRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            BufferedImage ricon = ImageIO.read(uc.getInputStream());

            //그래픽 생성
            Graphics g = image.getGraphics();
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            //Requster Name TEXT
            g.setFont(g.getFont().deriveFont(43f));
            g.drawString(requester, 695, 613);

            //Video Title TEXT
            g.setFont(g.getFont().deriveFont(54f));
            g.drawString(title, 20, 525);

            //Uploader Profile Image
            //클립생성(마스크 모양 생성)
            g.setClip(new Ellipse2D.Float(24, 543, 82, 82));
            //클립에 이미지 그림
            g.drawImage(uicon, 24, 543, 82, 82, null);

            //Uploader Name TEXT
            //폰트 설정
            g.setFont(g.getFont().deriveFont(43f));
            //문자열 그림
            g.setClip(new Rectangle(120, 570, 440, 80));
            g.drawString(uploader, 120, 613);

            //Requester Profile Image
            g.setClip(new Ellipse2D.Float(600, 545, 80, 80));
            g.drawImage(ricon, 600, 545, 80, 80, null);


            if(thum == null) {
                try {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "maxresdefault"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                } catch (IOException e) { }

            } if(thum == null) {
                try {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "maxresdefault").replace("i.ytimg", "i1.ytimg"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                } catch (IOException e) { }

            } if(thum == null) {
                try {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "maxresdefault").replace("i.ytimg", "i2.ytimg"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                } catch (IOException e) { }

            } if(thum == null) {
                try {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "maxresdefault").replace("i.ytimg", "i3.ytimg"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                } catch (IOException e) { }

            } if(thum == null) {
                try {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "maxresdefault").replace("i.ytimg", "i4.ytimg"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                } catch (IOException e) { }

            } if(thum == null) {
                try {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "maxresdefault").replace("i.ytimg", "img.youtube"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                } catch (IOException e) { }

            } if(thum == null) {
                try {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "sddefault"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                    thumbIsSd = true;
                } catch (IOException e) { }

            } if(thum == null) {
                try {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "sddefault").replace("i.ytimg", "i1.ytimg"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                    thumbIsSd = true;
                } catch (IOException e) { }

            } if(thum == null) {
                try {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "sddefault").replace("i.ytimg", "i2.ytimg"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                    thumbIsSd = true;
                } catch (IOException e) { }

            } if(thum == null) {
                try {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "sddefault").replace("i.ytimg", "i3.ytimg"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                    thumbIsSd = true;
                } catch (IOException e) { }

            } if(thum == null) {
                try {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "sddefault").replace("i.ytimg", "i4.ytimg"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                    thumbIsSd = true;
                } catch (IOException e) { }

            } if(thum == null) {
                try {
                    URL thumbnailFile = new URL(thumbUrl.replace("hqdefault", "sddefault").replace("i.ytimg", "img.youtube"));
                    thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                    thumbIsSd = true;
                } catch (IOException e) { }

            } if(thum == null) {
                try {
                    File thumbnailFile = new File("./NotFound.png");
                    thum = ImageIO.read(thumbnailFile);
                } catch (IOException e) { e.printStackTrace(); }

            }

            //System.out.println(thumbUrl);


            //섬네일 이미지 자름
            if(thum != null && !thumbIsSd) {
                thum = cropImage(thum, new Rectangle(30, 60, 1210, 469));
            } if(thum != null && thumbIsSd) {
                thum = cropImage(thum, new Rectangle(30, 123, 605, 234));
            }

            //Thumnail Image
            g.setClip(new RoundRectangle2D.Float(0, 0, 1280, 469, 50, 50));
            g.drawImage(thum, 0, 0, 1280, 469, null);


            //Edit close
            g2d.dispose();
            g.dispose();

            //Save Image
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssZ");
            File outFile = new File(String.format("%s.png", dateFormat.format(new Date())));

            ImageIO.write(image, "png", outFile);

            return outFile;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return canvasFile;
    }
}
