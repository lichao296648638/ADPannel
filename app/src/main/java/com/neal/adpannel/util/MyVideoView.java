package com.neal.adpannel.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;


/**
 * Created by lichao on 17/5/9.
 */

public class MyVideoView extends VideoView {
//    /**
//     * 视频控制器
//     */
//    VideoController mVideoController;
//
//    /**
//     * 设置视频控制器
//     */
//    public void setVideoController(VideoController videoController) {
//        mVideoController = videoController;
//    }
    public MyVideoView(Context context) {
        super(context);
    }

    public MyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //我们重新计算高度
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}
