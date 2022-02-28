package com.up.wqs.service;

import java.io.File;

import static com.up.wqs.constant.AppConstants.FILEDIR;

public class FileUtils {

    public static Integer getFileAndDirectory() {
        String path = FILEDIR;        //要遍历的路径
        File file = new File(path);        //获取其file对象
        File[] fs = file.listFiles();
        int count = 0;
        //遍历path下的文件和目录，放在File数组中
        for (File f : fs) {                    //遍历File[]数组
            if (!f.isDirectory() && f.getName().contains(".mp4")){
                System.out.println(f);
                count++;
            }
        }
        return count;
    }

}
