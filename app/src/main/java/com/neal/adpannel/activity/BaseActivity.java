package com.neal.adpannel.activity;
/**
 * 活动基类
 * Created by lichao on 17/5/2.
 */

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


public abstract class BaseActivity extends Activity {

    protected Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //去掉虚拟按键全屏显示
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        context = getApplicationContext();
        setContentView(setContent());
        initViews();
        initResource();
        initTitleBar();
        setTitleBar();
        setViews();
    }

    /**
     * 设置布局文件
     *
     * @return 布局ID
     */
    protected abstract int setContent();


    /**
     * 初始化资源
     */
    protected abstract void initResource();

    /**
     * 初始化控件
     */
    protected abstract void initViews();

    /**
     * 设置控件
     */
    protected abstract void setViews();


    /**
     * 初始化标题
     */
    protected abstract void initTitleBar();

    /**
     * 设置标题
     */
    protected abstract void setTitleBar();


}
