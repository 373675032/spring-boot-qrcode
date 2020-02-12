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
