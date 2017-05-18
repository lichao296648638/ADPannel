package com.neal.adpannel.interf;

import java.io.File;

public interface onDownLoadListener {

    /**
     * 下载成功
     * @param data 下载好的文件对象
     */
    void success(File data);

    /**
     * 下载失败
     */
    void fail();

}