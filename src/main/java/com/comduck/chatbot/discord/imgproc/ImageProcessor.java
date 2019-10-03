package com.comduck.chatbot.discord.imgproc;

import javax.imageio.ImageIO;
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
        return src.getSubimage(rect.x, rect.y, rect.width, rect.height);
    }

    public File processImage(File canvasFile, String id, URL requesterIconFile, URL uploaderIconFile, String title, String uploader, String requester) {
        try {


            //Load Image
            BufferedImage image = ImageIO.read(canvasFile);
            BufferedImage thum = null;
            try {
                URL thumbnailFile = new URL("http://img.youtube.com/vi/" + id + "/maxresdefault.jpg");
                thum = ImageIO.read(thumbnailFile.openStream());
            } catch (Exception e1) {
                try {
                    URL thumbnailFile = new URL("http://i.ytimg.com/vi/" + id + "/maxresdefault.jpg");
                    thum = ImageIO.read(thumbnailFile.openStream());
                } catch (Exception e2) {
                    try {
                        URL thumbnailFile = new URL("http://i.ytimg.com/vi/" + id + "/sddefault.jpg");
                        thum = ImageIO.read(thumbnailFile.openStream());
                    } catch (Exception e3) {
                        e1.printStackTrace();
                        e2.printStackTrace();
                        e3.printStackTrace();
                    }

                }
            }
            BufferedImage uicon = ImageIO.read(uploaderIconFile.openStream());


            URLConnection uc = requesterIconFile.openConnection();
            uc.addRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            BufferedImage ricon = ImageIO.read(uc.getInputStream());

            //섬네일 이미지 자름
            thum = cropImage(thum, new Rectangle(30, 60, 1210, 469));

            //그래픽 생성
            Graphics g = image.getGraphics();
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            //Uploader Name TEXT
            //폰트 설정
            g.setFont(g.getFont().deriveFont(43f));
            //문자열 그림
            g.drawString(uploader, 120, 613);

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

            //Requester Profile Image
            g.setClip(new Ellipse2D.Float(600, 545, 80, 80));
            g.drawImage(ricon, 600, 545, 80, 80, null);

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
            System.out.println(ex);
        }
        return canvasFile;
    }
}
