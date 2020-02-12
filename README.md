# spring-boot-qrcode

#### 介绍
SpringBoot整合二维码,使用Google提供的二维码依赖架包.实现二维码文件的生成和前端的显示

#### 安装教程

1.  导入依赖
```xml
      <!--导入二维码依赖-->
    <!-- https://mvnrepository.com/artifact/com.google.zxing/core -->
    <dependency>
        <groupId>com.google.zxing</groupId>
        <artifactId>core</artifactId>
        <version>3.3.3</version>
    </dependency>
    <!-- https://mvnrepository.com/artifact/com.google.zxing/javase -->
    <dependency>
        <groupId>com.google.zxing</groupId>
        <artifactId>javase</artifactId>
        <version>3.3.3</version>
    </dependency>

```
2.  编写二维码工具类

```java
    package com.moti.springbootqrcode.utils;
    
    
    import com.google.zxing.BarcodeFormat;
    import com.google.zxing.EncodeHintType;
    import com.google.zxing.MultiFormatWriter;
    import com.google.zxing.common.BitMatrix;
    import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
    
    import javax.imageio.ImageIO;
    import java.awt.*;
    import java.awt.geom.RoundRectangle2D;
    import java.awt.image.BufferedImage;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.OutputStream;
    import java.util.Hashtable;
    
    /**
     * @Description 二维码生成工具类
     * @Author xw
     * @Date 12:14 2020/2/12
     * @Param  * @param null
     * @return
     **/
    public class QRCodeUtil {
    
        //编码
        private static final String CHARSET = "utf-8";
        //文件格式
        private static final String FORMAT = "JPG";
        // 二维码尺寸
        private static final int QRCODE_SIZE = 300;
        // LOGO宽度
        private static final int LOGO_WIDTH = 60;
        // LOGO高度
        private static final int LOGO_HEIGHT = 60;
    
        /**
         * @Description 生成二维码
         * @Author xw
         * @Date 12:14 2020/2/12
         * @Param [content, logoPath, needCompress] 内容,logo路径,是否压缩
         * @return java.awt.image.BufferedImage
         **/
        public static BufferedImage createImage(String content, String logoPath, boolean needCompress) throws Exception {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, QRCODE_SIZE, QRCODE_SIZE,
                    hints);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            if (logoPath == null || "".equals(logoPath)) {
                return image;
            }
            // 插入图片
            QRCodeUtil.insertImage(image, logoPath, needCompress);
            return image;
        }
    
        /**
         * @Description 插入logo
         * @Author xw
         * @Date 12:15 2020/2/12
         * @Param [source, logoPath, needCompress] 二维码图片,logo路径,是否压缩
         * @return void
         **/
        private static void insertImage(BufferedImage source, String logoPath, boolean needCompress) throws IOException {
            InputStream inputStream = null;
            try {
                inputStream = QRCodeUtil.getResourceAsStream(logoPath);
                Image src = ImageIO.read(inputStream);
                int width = src.getWidth(null);
                int height = src.getHeight(null);
                if (needCompress) {
                    // 压缩LOGO
                    width = width>LOGO_WIDTH?LOGO_WIDTH:width;
                    height = height>LOGO_HEIGHT?LOGO_HEIGHT:height;
                    Image image = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    Graphics g = tag.getGraphics();
                    g.drawImage(image, 0, 0, null);
                    // 绘制缩小后的图
                    g.dispose();
                    src = image;
                }
                // 插入LOGO
                Graphics2D graph = source.createGraphics();
                int x = (QRCODE_SIZE - width) / 2;
                int y = (QRCODE_SIZE - height) / 2;
                graph.drawImage(src, x, y, width, height, null);
                Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
                graph.setStroke(new BasicStroke(3f));
                graph.draw(shape);
                graph.dispose();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
    
        /**
         * @Description 生成二维码,获得到输出流 ,logo内嵌
         * @Author xw
         * @Date 12:16 2020/2/12
         * @Param [content, logoPath, output, needCompress] 内容,logo路径,输出流,是否压缩
         * @return void
         **/
        public static void encode(String content, String logoPath, OutputStream output, boolean needCompress)
                throws Exception {
            BufferedImage image = QRCodeUtil.createImage(content, logoPath, needCompress);
            ImageIO.write(image, FORMAT, output);
        }
    
        /**
         * @Description 获取指定文件的输入流，获取logo
         * @Author xw
         * @Date 12:17 2020/2/12
         * @Param [logoPath] logo路径
         * @return java.io.InputStream
         **/
        public static InputStream getResourceAsStream(String logoPath) {
            return QRCodeUtil.class.getResourceAsStream(logoPath);
        }
    }
```

3.  编写Controller

```java
    package com.moti.springbootqrcode.controller;
    
    import com.moti.springbootqrcode.utils.QRCodeUtil;
    import org.springframework.stereotype.Controller;
    import org.springframework.web.bind.annotation.RequestMapping;
    
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import java.io.File;
    import java.io.FileOutputStream;
    import java.io.OutputStream;
    import java.util.Map;
    
    /**
     * @ClassName: HelloController
     * @Description: TODO
     * @author: xw
     * @date 2020/2/12 11:25
     * @Version: 1.0
     **/
    @Controller
    public class HelloController {
    
        @RequestMapping("/hello")
        public String createQrCode(HttpServletRequest request, HttpServletResponse response, Map<String,Object> map) {
            //分享的文件ID
            Integer myFileId = 1;
            try {
                String path = request.getSession().getServletContext().getRealPath("/user_img/");
                File targetFile = new File(path, "");
                if (!targetFile.exists()) {
                    targetFile.mkdirs();
                }
                File file = new File(path, myFileId + ".jpg");
                if (!file.exists()){
                    //文件不存在,开始生成二维码并保存文件
                    OutputStream os = new FileOutputStream(file);
                    QRCodeUtil.encode("http://xuewei.world", "/static/images/head.png", os, true);
                    os.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                map.put("path","https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=2654852821,3851565636&fm=26&gp=0.jpg");
            }
            map.put("path","user_img/"+myFileId+".jpg");
            return "index";
        }
    }
    
```
