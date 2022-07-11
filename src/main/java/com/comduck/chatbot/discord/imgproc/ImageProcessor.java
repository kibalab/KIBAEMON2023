package com.comduck.chatbot.discord.imgproc;

import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ImageProcessor {

    // 예시 코드
    // ImageProcessor processor = new ImageProcessor();
    // File outFile = processor.processImage(new File("testImage.png"), new File("testThum.png"), new File("iconTest.png"));
    // outFile 전송 후 파일 삭제
    // outFile.delete();

   public ImageProcessor() {

   }

    public File processImage(JSONObject details, File canvasFile, URL requesterIconFile, URL uploaderIconFile, String requester, long x_duration, String[] tags) {
        try {

            String id = (String) details.get("id");
            String uploader = (String) details.get("author_name");
            String title = (String) details.get("title");
            String thumbUrl = (String) details.get("thumbnail_url");
            Date duration = new Date(x_duration);

            //Load Image
            BufferedImage image = ImageIO.read(canvasFile);
            BufferedImage thum = null;
            BufferedImage uicon = null;
            boolean thumbIsSd = false;

            try {
                for (int i = 0; i < 5; i++) { // 최대 5번 시도
                    if (uicon == null) { // 가끔 이미지를 한번에 못가져 오는경우가 있어서 만듬
                        uicon = ImageIO.read(uploaderIconFile.openConnection().getInputStream());
                    } else {
                        break;
                    }
                }
            }catch (Exception e){
                System.out.println("[ImageProcessor] Failed Read Uploader Profile Image");
            }


            URLConnection uc = requesterIconFile.openConnection();
            uc.addRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            BufferedImage ricon = ImageIO.read(uc.getInputStream());

            Font SansMedium = Font.createFont(Font.TRUETYPE_FONT, new File("NotoSansCJKkr-Medium.otf"));
            Font SansBold = Font.createFont(Font.TRUETYPE_FONT, new File("NotoSansCJKkr-Bold.otf"));


            //그래픽 생성
            Graphics g = image.getGraphics();
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setFont(SansMedium);

            //Requster Name TEXT
            g2d.setFont(g.getFont().deriveFont(43f));
            g2d.drawString(requester, 788, 636);

            //Video Title TEXT
            g2d.setFont(SansBold);
            g2d.setFont(g2d.getFont().deriveFont(52f));
            float title_width = CalculateStringWidth(g2d, title);
            float title_height = CalculateStringHeight(g2d, title);
            if(title_width > 1230) g2d.setFont(g2d.getFont().deriveFont((1230 / title_width ) * 52f));
            float fit_Upper = title_width / CalculateStringHeight(g2d, title) / 4;
            //Title Text Shadow
            Color origin = g2d.getColor();
            g2d.setColor(Color.BLACK);
            g2d.drawString(title, 21, 552 - fit_Upper);
            g2d.setColor(origin);
            //Title Text Body
            g2d.drawString(title, 18, 552 - fit_Upper);

            //Duration Text
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            g2d.setFont(g.getFont().deriveFont(20f));
            g2d.drawString(formatter.format(duration), 34, 489);

            DrawTags(g2d, tags);

            //Uploader Profile Image
            g2d.setFont(SansMedium);
            //클립생성(마스크 모양 생성)
            g2d.setClip(new Ellipse2D.Float(26, 569, 82, 82));
            //클립에 이미지 그림
            if (uicon != null) g.drawImage(uicon, 26, 569, 82, 82, null);

            //Uploader Name TEXT
            //폰트 설정
            g2d.setFont(g.getFont().deriveFont(43f));
            //문자열 그림
            g2d.setClip(new Rectangle(116, 556, 440, 120));
            g2d.drawString(uploader, 116, 636);

            //Requester Profile Image
            g2d.setClip(new Ellipse2D.Float(693, 569, 79.13f, 79.13f));
            if (ricon != null) g2d.drawImage(ricon, 693, 569, 79, 79, null);


            String[] sizes = {"maxresdefault", "sddefault"};
            String[] servers = {"i.ytimg", "i1.ytimg", "i2.ytimg", "i3.ytimg", "i4.ytimg", "img.youtube"};

            for(String size : sizes) {
                for(String server : servers) {
                    URL thumbnailFile = null;
                    try {
                        thumbnailFile = new URL(thumbUrl.replace("hqdefault", size).replace("i.ytimg", server));
                        thum = ImageIO.read(thumbnailFile.openConnection().getInputStream());
                        System.out.println("[ImageProcessor] Succeeded Load Image - " + thumbnailFile.toString());
                    } catch (Exception e) {
                        System.out.println("[ImageProcessor] Failed Load Image - " + thumbnailFile.toString());
                    }
                    if(thum != null) break;
                } if(thum != null) break;
            }


            //Thumnail Image
            g2d.setClip(new RoundRectangle2D.Float(11, 12, 1260, 446, 90, 90));

            System.out.println("[ImageProcessor] Tumbnail Size :" + thum.getWidth() + "/" + thum.getHeight());
            float sizeFit = Math.round(1260.0f/thum.getWidth() * 10) / 10;
            System.out.println("[ImageProcessor] Tumbnail Size ratio :" + sizeFit);
            //g2d.setPaint(new TexturePaint(thum, new Rectangle2D.Double(11, 12, 1260, 446)));
            if (thum != null) g2d.drawImage(thum, 0, 0, (int)(thum.getWidth() * sizeFit), (int)(thum.getHeight() * sizeFit),null);

            //Edit close
            g2d.dispose();
            g.dispose();

            //Save Image
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssZ");
            File outFile = new File(String.format("%s.png", dateFormat.format(new Date())));

            //image = blur(image);

            ImageIO.write(image, "png", outFile);

            return outFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return canvasFile;
    }

    public Graphics2D DrawTags(Graphics2D g2d, String[] tags){

        BufferedImage Tag_L = null, Tag_M = null, Tag_R = null;
       try {
           Tag_L = ImageIO.read(new File("Tag_L.png"));
           Tag_M = ImageIO.read(new File("Tag_M.png"));
           Tag_R = ImageIO.read(new File("Tag_R.png"));
       }catch (Exception e){
           System.out.println("[ImageProcessor] Can not load Tag Sprite");
       }

        int tag_posX = 129;
        int padding = 7;
        for(int i = 0; i < tags.length ; i++){
            int middle_size = (int)(CalculateStringWidth(g2d, tags[i]));

            if(tag_posX + Tag_L.getWidth() + middle_size + Tag_R.getWidth() > 1150){
                middle_size = (int)(CalculateStringWidth(g2d, "..."));

                g2d.drawImage(Tag_L, tag_posX, 438, Tag_L.getWidth(), Tag_L.getHeight(),null);
                tag_posX += Tag_L.getWidth();
                g2d.drawImage(Tag_M, tag_posX, 438, middle_size, Tag_M.getHeight(),null);
                g2d.drawString("...", tag_posX, 490);
                tag_posX += middle_size;
                g2d.drawImage(Tag_R, tag_posX, 438, Tag_R.getWidth(), Tag_R.getHeight(),null);
                break;
            }


            g2d.drawImage(Tag_L, tag_posX, 438, Tag_L.getWidth(), Tag_L.getHeight(),null);
            tag_posX += Tag_L.getWidth();
            g2d.drawImage(Tag_M, tag_posX, 438, middle_size, Tag_M.getHeight(),null);
            g2d.drawString(tags[i], tag_posX, 490);
            tag_posX += middle_size;
            g2d.drawImage(Tag_R, tag_posX, 438, Tag_R.getWidth(), Tag_R.getHeight(),null);
            tag_posX += padding;
        }
        return g2d;
    }

    public float CalculateStringWidth(Graphics2D g2d, String text){
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
        return (float)(g2d.getFont().getStringBounds(text, frc).getWidth());
    }
    public float CalculateStringHeight(Graphics2D g2d, String text){
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
        return (float)(g2d.getFont().getStringBounds(text, frc).getHeight());
    }

    public BufferedImage blur(BufferedImage target) {
        // TODO Auto-generated method stub

        int radius = 3;
        int size = radius * 2 + 1;

        float[] data = new float[size * size];

        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;

        for (int i = -radius; i <= radius; i++) {
            float distance = i * i;
            int index = i + radius;
            data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
            total += data[index];
        }

        for (int i = 0; i < data.length; i++) {
            data[i] /= total;
        }
        Kernel kernel = new Kernel(1, size, data);

        ConvolveOp convolveOp = new ConvolveOp(kernel,ConvolveOp.EDGE_NO_OP,null);

        target = convolveOp.filter(target, null);

        kernel = new Kernel(size, 1, data);

        convolveOp = new ConvolveOp(kernel,ConvolveOp.EDGE_NO_OP,null);

        target = convolveOp.filter(target, null);

        return target;

    }
}

class Pixel {
    public float R, G, B, A;

    public Pixel(float r, float g, float b, float a) {
        this.R = r;
        this.G = g;
        this.B = b;
    }
}