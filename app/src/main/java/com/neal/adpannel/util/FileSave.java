package com.neal.adpannel.util;

import android.app.Activity;
import android.content.Context;

import com.neal.adpannel.interf.onDownLoadListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


/**
 * 文件存储工具类
 * Created by lichao on 17/5/10.
 */

public class FileSave {
    private static final String TAG = "FileSave";

    private Activity mActivity;


    public FileSave(Activity activity){
        mActivity = activity;
    }

    /**
     * 根据url存储文件
     *
     * @param path 存储路径
     * @param name 存储名称
     * @param url  资源地址
     * @return 已存储文件
     */
    public File saveFile(String path, String name, final String url, final onDownLoadListener onDownLoadListener) {
        //路径
        File dir = new File(path);
        //文件
        final File file = new File(path, name);
        try {
            //判断路径和文件状态
            if (!dir.exists()) {
                dir.mkdir();
            } else {
                if (file.exists()) {
                    file.delete();
                    file.createNewFile();
                } else {
                    file.createNewFile();
                }
            }

            //下载文件
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 构造URL
                    URL sourceUrl = null;
                    try {
                        sourceUrl = new URL(url);

                        // 打开连接
                        URLConnection con = sourceUrl.openConnection();
                        //获得文件的长度
                        int contentLength = con.getContentLength();
                        Logs.i(TAG, "长度 :" + contentLength);
                        // 输入流
                        InputStream is = con.getInputStream();
                        // 1K的数据缓冲
                        byte[] bs = new byte[1024];
                        // 读取到的数据长度
                        int len;
                        // 输出的文件流
                        OutputStream os = new FileOutputStream(file);
                        // 开始读取
                        while ((len = is.read(bs)) != -1) {
                            os.write(bs, 0, len);
                        }
                        // 完毕，关闭所有链接
                        os.close();
                        //下载成功,切换到主线程
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onDownLoadListener.success(file);
                            }
                        });
                    } catch (Exception e) {
                        //下载失败
                        onDownLoadListener.fail();
                        e.printStackTrace();
                    }
                }
            }).start();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }

}
