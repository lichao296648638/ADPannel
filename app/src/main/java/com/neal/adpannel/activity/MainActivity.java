package com.neal.adpannel.activity;
/**
 * 报警demo
 * Created by lichao on 17/5/2.
 */

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.EMServiceNotReadyException;
import com.neal.adpannel.R;
import com.neal.adpannel.ease.receiver.CallReceiver;
import com.neal.adpannel.ease.ui.VideoCallActivity;
import com.neal.adpannel.entity.AdEntity;
import com.neal.adpannel.entity.EventEntity;
import com.neal.adpannel.interf.onDownLoadListener;
import com.neal.adpannel.interf.onSwitchAd;
import com.neal.adpannel.util.FileSave;
import com.neal.adpannel.util.Logs;
import com.neal.adpannel.util.MyVideoView;
import com.neal.adpannel.util.Toasts;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends BaseActivity implements onSwitchAd {
    private static final String TAG = "MainActivity";


    /**
     * 发起视频
     */
    Button bt_connect;
//
//    /**
//     * 视频广告
//     */
//    MyVideoView mvv_ad;

    /**
     * 主容器
     */
    FrameLayout fm_container;

    /**
     * Google Gson
     */
    Gson gson = new Gson();

    /**
     * okhttp
     */
    OkHttpClient client;
    String data = null;


    /**
     * 文件下载器
     */
    FileSave fileSave;

    /**
     * 广告场景编辑事件时间戳
     */
    EventEntity eventEntity = new EventEntity();

    /**
     * 广告切换器
     */
    onSwitchAd switcher = this;

    /**
     * 当前轮播组的广告元素下标
     */
    volatile int[] indexArr;


    /**
     * 所有待下载的文件数
     */
    int allFiles = 0;

    /**
     * 呼入监听者
     */
    CallReceiver callReceiver;


    /**
     * 所有播放中的视频
     */
    List<MyVideoView> myVideoViews = new ArrayList<>();

    /**
     * 广告暂停标志
     */
    boolean isPaused = false;

    /**
     * 同步锁
     */
    Object lock = new Object();

    /**
     * 长按报警按下和抬起时间
     */
    long downTime, upTime;

    /**
     * 是否开始记录按钮按下时间
     */
    boolean isRecord = false;

    /**
     * 数据库操作对象
     */
    SQLiteDatabase db;
    String str_Update = "{\"time_stamp\":\"2017101010101\"}";
    String str_Ad = "[{\"font_bold\":false,\"font_size\":0,\"data\":\"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1494521130900&di=64c531e9e45405e07fb819346f9f8a5d&imgtype=0&src=http%3A%2F%2Fstock.591hx.com%2Fimages%2Fhnimg%2F20151208%2F18%2F6463988493360928614.jpg\",\"top\":0,\"time_stamp\":\"1\",\"height\":525,\"width\":350,\"file_name\":\"picture1.jpg\",\"font_color\":\"\",\"set_type\":0,\"type\":2,\"font_family\":0,\"left\":0,\"play_time\":3,\"banner_group\":\"333\"},{\"font_bold\":false,\"font_size\":0,\"data\":\"http://flv2.bn.netease.com/videolib3/1604/28/fVobI0704/SD/fVobI0704-mobile.mp4\",\"top\":525,\"time_stamp\":\"2\",\"height\":400,\"width\":800,\"file_name\":\"video1.mp4\",\"font_color\":\"\",\"set_type\":0,\"type\":1,\"font_family\":0,\"left\":0,\"banner_group\":\"333\"},{\"font_bold\":false,\"font_size\":0,\"data\":\"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1494521130900&di=64c531e9e45405e07fb819346f9f8a5d&imgtype=0&src=http%3A%2F%2Fstock.591hx.com%2Fimages%2Fhnimg%2F20151208%2F18%2F6463988493360928614.jpg\",\"top\":925,\"time_stamp\":\"3\",\"height\":525,\"width\":350,\"file_name\":\"picture2.jpg\",\"font_color\":\"\",\"set_type\":0,\"type\":2,\"font_family\":0,\"left\":0,\"play_time\":4,\"banner_group\":\"222\"},{\"font_bold\":false,\"font_size\":0,\"data\":\"http://flv2.bn.netease.com/videolib3/1604/28/fVobI0704/SD/fVobI0704-mobile.mp4\",\"top\":925,\"time_stamp\":\"4\",\"height\":400,\"width\":800,\"file_name\":\"video2.mp4\",\"font_color\":\"\",\"set_type\":0,\"type\":1,\"font_family\":0,\"banner_group\":\"222\",\"left\":350}]";
//    String str_Ad = "[{\"font_bold\":false,\"font_size\":0,\"data\":\"http://flv2.bn.netease.com/videolib3/1604/28/fVobI0704/SD/fVobI0704-mobile.mp4\",\"top\":525,\"time_stamp\":\"2\",\"height\":400,\"width\":800,\"file_name\":\"video1.mp4\",\"font_color\":\"\",\"set_type\":0,\"type\":1,\"font_family\":0,\"left\":0,\"banner_group\":\"333\"},{\"font_bold\":false,\"font_size\":0,\"data\":\"http://flv2.bn.netease.com/videolib3/1604/28/fVobI0704/SD/fVobI0704-mobile.mp4\",\"top\":925,\"time_stamp\":\"4\",\"height\":400,\"width\":800,\"file_name\":\"video2.mp4\",\"font_color\":\"\",\"set_type\":0,\"type\":1,\"font_family\":0,\"banner_group\":\"222\",\"left\":350}]";


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(callReceiver);
    }

    @Override
    protected int setContent() {
        return R.layout.activity_main;
    }

    @Override
    protected void initResource() {
        //初始化LitePal
        db = Connector.getDatabase();
        //初始化OKHTTP
        client = new OkHttpClient();
    }

    @Override
    protected void initViews() {
        bt_connect = (Button) findViewById(R.id.bt_connect);
//        mvv_ad = (MyVideoView) findViewById(R.id.mvv_ad);
        fm_container = (FrameLayout) findViewById(R.id.fm_container);
        //监听呼入电话
        IntentFilter callFilter = new IntentFilter(EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        callReceiver = new CallReceiver();
        registerReceiver(callReceiver, callFilter);


    }

    @Override
    protected void setViews() {
        //拨打视频电话
        bt_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startVideoCall();
            }
        });
//
//        mvv_ad.setVideoPath("/storage/emulated/0/Movies/cf752b1c12ce452b3040cab2f90bc265_h264818000nero_aac32-1.mp4");
//        //让videoView获得焦点
//        mvv_ad.requestFocus();
//        mvv_ad.start();

        //判断是否根据缓存创建节目
        if (isUpdate(str_Update)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
//                    saveElements(getJson("http://192.168.1.106:8000/api/mips/material/"));
                    saveElements(str_Ad);

                }
            }).start();
        } else {
            playElements();
        }


    }

    @Override
    protected void initTitleBar() {

    }

    @Override
    protected void setTitleBar() {

    }


    /**
     * make a video call
     */
    protected void startVideoCall() {
        if (!EMClient.getInstance().isConnected())
            Toasts.makeText(getResources().getString(R.string.not_connect_to_server));
        else {
            startActivity(new Intent(this, VideoCallActivity.class).putExtra("username", "296648637")
                    .putExtra("isComingCall", false));
        }
    }

    /**
     * 从本地数据库中播放广告节点
     */
    private void playElements() {

        //所有的节目组列表
        final List<List<AdEntity>> allAds = new ArrayList<List<AdEntity>>();
        //获取本地数据中所有节目元素
        List<AdEntity> adEntities = DataSupport.findAll(AdEntity.class);
        //装载所有节目组ID
        AdEntity adEntity = null;
        Iterator<AdEntity> adIterator = adEntities.iterator();
        Set<String> adGroup = new android.support.v4.util.ArraySet<String>();
        while (adIterator.hasNext()) {
            adEntity = adIterator.next();
            adGroup.add(adEntity.getBanner_group());
        }
        //遍历所有节目组
        Iterator<String> groupIdIterator = adGroup.iterator();
        while (groupIdIterator.hasNext()) {
            String groupId = groupIdIterator.next();
            List<AdEntity> sigleList = DataSupport.where("banner_group = ?", groupId).find(AdEntity.class);
            //添加该节目组至所有节目列表
            allAds.add(sigleList);
        }
        //初始化每个节目组的节目下标
        indexArr = new int[allAds.size()];
        //遍历所有的节目列表
        for (int i = 0; i < allAds.size(); i++) {
            //当前广告组下标
            final int currGroupIndex = i;
            final List<AdEntity> singleList = allAds.get(i);
            //广告素材时长
            final int[] AdTime = new int[singleList.size()];
            //记录时长
            for (int j = 0; j < singleList.size(); j++) {
                //获取当前广告元素实体类
                AdEntity currAd = singleList.get(j);
                //获取每个广告素材的时长，视频文件需特殊处理
                if (currAd.getType() != 1) {
                    AdTime[j] = currAd.getPlay_time();
                } else {
                    MediaPlayer player = new MediaPlayer();
                    try {
                        player.setDataSource(currAd.getFile_path());  //recordingFilePath（）为音频文件的路径
                        player.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    double duration = player.getDuration();//获取音频的时间
                    Logs.d("ACETEST", "### duration: " + duration);
                    player.release();//释放资源
                    AdTime[j] = (int) duration;

                }
            }
            //启动广告轮播线程
            excuteAdTask(singleList, currGroupIndex, AdTime);

        }


    }

    /**
     * 执行轮播广告的任务
     *
     * @param singleList     广告组
     * @param currGroupIndex 当前广告组下标
     * @param AdTime         广告播放时长
     */
    private void excuteAdTask(final List<AdEntity> singleList, final int currGroupIndex, final int[] AdTime) {

        //有死循环，开启线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                //初次执行任务
                boolean isFirst = true;
                //记录任务开始时间
                long startTime = System.currentTimeMillis();

                //开始循环
                while (true) {
                    //广告暂停，线程挂起
                    if (isPaused) {
                        synchronized (lock) {
                            try {
                                Logs.i(TAG, "广告线程挂起....");
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    //如果是初次运行则直接播放
                    if (isFirst) {
                        //切换场景
                        switcher.onSwitch(singleList, indexArr[currGroupIndex]);
                        //重新开始计算任务开始时间
                        startTime = System.currentTimeMillis();
                        //非初次了
                        isFirst = false;

                    }
                    //记录任务当前时间
                    long currentTime = System.currentTimeMillis();
                    //任务已运行时间
                    long elapsedTime = currentTime - startTime;
                    //判断是否切换广告元素，非视频
                    if (singleList.get(indexArr[currGroupIndex]).getType() != 1) {
                        if (elapsedTime >= AdTime[indexArr[currGroupIndex]] * 1000) {
                            //记录广告数组下标+1
                            indexArr[currGroupIndex]++;
                            //循环
                            if (indexArr[currGroupIndex] == AdTime.length)
                                indexArr[currGroupIndex] = 0;
                            //切换场景
                            switcher.onSwitch(singleList, indexArr[currGroupIndex]);
                            //重新开始计算任务开始时间
                            startTime = System.currentTimeMillis();

                        }
                    } else {
                        //判断是否切换广告元素，视频
                        if (elapsedTime >= AdTime[indexArr[currGroupIndex]]) {
                            //记录广告数组下标+1
                            indexArr[currGroupIndex]++;
                            //循环
                            if (indexArr[currGroupIndex] == AdTime.length)
                                indexArr[currGroupIndex] = 0;
                            //切换场景
                            switcher.onSwitch(singleList, indexArr[currGroupIndex]);
                            //重新开始计算任务开始时间
                            startTime = System.currentTimeMillis();

                        }
                    }


                }
            }
        }).start();

    }

    /**
     * 根据Json字符串动态解析布局
     *
     * @param json 服务器返回的布局json
     */
    private void saveElements(String json) {
        Logs.i(TAG, "访问的数据：= " + json);
        //使用gson将json转成实体广告元素列表
        List<AdEntity> adEntities = gson.fromJson(json,
                new TypeToken<List<AdEntity>>() {
                }.getType());

        //获取广告元素列表遍历器
        final Iterator<AdEntity> iterator = adEntities.iterator();
        //开始遍历
        while (iterator.hasNext()) {
            //获取广告元素实体类
            final AdEntity adEntity = iterator.next();
            //设置大小和位置
            final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(adEntity.getWidth(), adEntity.getHeight());
            layoutParams.leftMargin = adEntity.getLeft();
            layoutParams.topMargin = adEntity.getTop();
            //判断元素类型
            switch (adEntity.getType()) {
                //视频
                case 1:
                    //待下载文件数 + 1
                    allFiles++;
                    //设置视频下载器回调
                    fileSave = new FileSave(this);
                    //下载视频并保存，返回本地储存的文件对象
                    fileSave.saveFile(context.getFilesDir().getAbsolutePath(), adEntity.getFile_name(), adEntity.getData(), new onDownLoadListener() {
                        @Override
                        public void success(File data) {
                            //文件路径入库
                            adEntity.setFile_path(data.getAbsolutePath());
                            adEntity.save();
                            //下载成功，待下载文件数 -1
                            allFiles--;
                            //所有文件下载完毕，开始播放呀！
                            if (allFiles == 0) {
                                playElements();
                            }
                        }

                        @Override
                        public void fail() {
                            Logs.i(TAG, "视频下载失败！");

                        }
                    });
                    break;
                //图片
                case 2:
                    //待下载文件数 + 1
                    allFiles++;
                    //设置图片下载器回调
                    fileSave = new FileSave(this);
                    //下载图片并保存，返回本地储存的文件对象
                    fileSave.saveFile(context.getFilesDir().getAbsolutePath(), adEntity.getFile_name(), adEntity.getData(), new onDownLoadListener() {
                        @Override
                        public void success(File data) {
                            //文件路径入库
                            adEntity.setFile_path(data.getAbsolutePath());
                            adEntity.save();
                            //下载成功，待下载文件数 -1
                            allFiles--;
                            //所有文件下载完毕，开始播放呀！
                            if (allFiles == 0) {
                                playElements();
                            }
                        }

                        @Override
                        public void fail() {
                            Logs.i(TAG, "图片下载失败！");

                        }
                    });
                    break;
                //pdf
                case 3:
                    break;
                //word
                case 4:

                    break;
            }

            //缓存广告编辑事件时间戳
            ContentValues values = new ContentValues();
            values.put("time_stamp", "2017101010101");
            DataSupport.update(EventEntity.class, values, 1);
            //

        }


    }

    /**
     * 根据Server的Json判断是否更新广告内容
     *
     * @param json Server返回字段
     * @return
     */
    private boolean isUpdate(String json) {
        //获取启动信息
        SharedPreferences sharedPreferences = getSharedPreferences("init", MODE_PRIVATE);
        boolean launch = sharedPreferences.getBoolean("launch", false);
        if (!launch) {
            //从未更新过，添加一条空事件记录
            eventEntity.setTime_stamp("");
            eventEntity.save();
            //修改启动信息
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("launch", true);
            editor.apply();
        }
        //查询数据库中的已有广告列表数据
        eventEntity = DataSupport.find(EventEntity.class, 1);
        //找出上次的广告列表
        String str_last = eventEntity.getTime_stamp();
        //跟当前Server的版本比对
        eventEntity = gson.fromJson(json, EventEntity.class);
        String str_current = eventEntity.getTime_stamp();
        //不一致，则需要更新广告数据
        if (TextUtils.isEmpty(str_last) || !str_last.equals(str_current)) {
            return true;
        }
        return false;
    }

    /**
     * okhttp get
     *
     * @param url
     */
    private String getJson(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                //打印json
                String data = response.body().string();
                Logs.i(TAG, data);
                return data;
            } else {
                throw new IOException("Unexpected code " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void onSwitch(final List<AdEntity> singleList, int index) {

        //获取当前广告元素
        final AdEntity currAd = singleList.get(index);
        //设置大小和位置
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(currAd.getWidth(), currAd.getHeight());
        layoutParams.leftMargin = currAd.getLeft();
        layoutParams.topMargin = currAd.getTop();

        //设置要播放的元素
        switch (currAd.getType()) {
            //文本
            case 0:
                //查看缓存中是否已有该控件
                TextView tv_ad = (TextView) fm_container.findViewWithTag(currAd.getTime_stamp());
                //有则直接显示
                if (tv_ad != null) {
                    tv_ad.setVisibility(View.VISIBLE);
                    return;
                }
                //没有则新建
                tv_ad = new TextView(MainActivity.this);
                tv_ad.setText(currAd.getData());
                tv_ad.setLayoutParams(layoutParams);
                //处理字体
                Typeface typeFace = null;

                switch (currAd.getFont_family()) {
                    //宋体
                    case 1:
                        typeFace = Typeface.createFromAsset(getAssets(), "song.ttf");
                        tv_ad.setTypeface(typeFace);
                        break;
                    //楷体
                    case 2:
                        typeFace = Typeface.createFromAsset(getAssets(), "kai.ttf");
                        tv_ad.setTypeface(typeFace);
                        break;
                    //黑体
                    case 3:
                        typeFace = Typeface.createFromAsset(getAssets(), "hei.ttf");
                        tv_ad.setTypeface(typeFace);
                        break;
                    //魏体
                    case 4:
                        typeFace = Typeface.createFromAsset(getAssets(), "wei.ttf");
                        tv_ad.setTypeface(typeFace);
                        break;
                    //行体
                    case 5:
                        typeFace = Typeface.createFromAsset(getAssets(), "xing.ttf");
                        tv_ad.setTypeface(typeFace);
                        break;
                    //圆体
                    case 6:
                        typeFace = Typeface.createFromAsset(getAssets(), "yuan.ttf");
                        tv_ad.setTypeface(typeFace);
                        break;
                    //隶书
                    case 7:
                        typeFace = Typeface.createFromAsset(getAssets(), "li.ttf");
                        tv_ad.setTypeface(typeFace);
                        break;
                    //微软雅黑
                    case 8:
                        typeFace = Typeface.createFromAsset(getAssets(), "yahei.ttf");
                        tv_ad.setTypeface(typeFace);
                        break;
                }
                //判断是否为粗体
                if (currAd.isFont_bold()) {
                    TextPaint paint = tv_ad.getPaint();
                    paint.setFakeBoldText(true);
                }

                //设置字体大小
                tv_ad.setTextSize(currAd.getFont_size());
                //设置字体颜色
                tv_ad.setTextColor(Color.parseColor(currAd.getFont_color()));
                //给控件设置标记
                tv_ad.setTag(currAd.getTime_stamp());
                //设置完毕添加文字
                tv_ad.setVisibility(View.VISIBLE);
                fm_container.addView(tv_ad);
                break;
            //视频
            case 1:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //查看缓存中是否已有该控件
                        MyVideoView mvv_ad = (MyVideoView) fm_container.findViewWithTag(currAd.getTime_stamp());
                        //有则直接显示
                        if (mvv_ad != null) {
                            mvv_ad.setVisibility(View.VISIBLE);
                            mvv_ad.start();
                            return;
                        }
                        //没有则新建
                        mvv_ad = new MyVideoView(MainActivity.this);
                        mvv_ad.setLayoutParams(layoutParams);
                        Uri videoUri = Uri.fromFile(new File(currAd.getFile_path()));
                        mvv_ad.setVideoURI(videoUri);
                        //给控件设置标记
                        mvv_ad.setTag(currAd.getTime_stamp());
                        //添加视频
                        fm_container.addView(mvv_ad);
                        mvv_ad.setVisibility(View.VISIBLE);
                        mvv_ad.requestFocus();
                        mvv_ad.start();
                        //设置循环播放
                        mvv_ad.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                //设置循环播放
                                mp.start();
                                mp.setLooping(true);
                            }
                        });
                        //添加至视频列表
                        myVideoViews.add(mvv_ad);
                    }
                });

                break;
            //图片
            case 2:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //查看缓存中是否已有该控件
                        ImageView iv_ad = (ImageView) fm_container.findViewWithTag(currAd.getTime_stamp());
                        //有则直接显示
                        if (iv_ad != null) {
                            iv_ad.setVisibility(View.VISIBLE);
                            return;
                        }
                        //没有则新建
                        iv_ad = new ImageView(MainActivity.this);
                        iv_ad.setLayoutParams(layoutParams);
                        Uri imgUrl = Uri.fromFile(new File(currAd.getFile_path()));
                        iv_ad.setImageURI(imgUrl);
                        //给控件设置标记
                        iv_ad.setTag(currAd.getTime_stamp());
                        //添加图片
                        iv_ad.setVisibility(View.VISIBLE);
                        fm_container.addView(iv_ad);
                    }
                });

                break;
            //pdf
            case 3:
                break;
            //word
            case 4:

                break;
        }
        //广告组只有一个元素则无需替换
        if (singleList.size() == 1) {
            return;
        }
        //设置要取消播放的元素
        index = index - 1;
        if (index < 0) {
            index = singleList.size() - 1;
        }
        switch (singleList.get(index).getType()) {
            //文本
            case 0:
                //找到该子View，屏蔽
                final TextView tv_ad = (TextView) fm_container.findViewWithTag(singleList.get(index).getTime_stamp());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (tv_ad != null)
                            tv_ad.setVisibility(View.GONE);
                    }
                });
                break;
            //视频
            case 1:
                //找到该子View，屏蔽
                final MyVideoView mvv_ad = (MyVideoView) fm_container.findViewWithTag(singleList.get(index).getTime_stamp());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mvv_ad != null)
                            mvv_ad.setVisibility(View.GONE);
                    }
                });
                break;
            //图片
            case 2:
                //找到该子View，屏蔽
                final ImageView iv_ad = (ImageView) fm_container.findViewWithTag(singleList.get(index).getTime_stamp());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (iv_ad != null)
                            iv_ad.setVisibility(View.GONE);
                    }
                });
                break;
            //pdf
            case 3:
                break;
            //word
            case 4:

                break;
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        ADResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ADPause();

    }

    /**
     * 广告暂停
     */
    private void ADPause() {
        //所有视频暂停
        Iterator<MyVideoView> videoViewIterator = myVideoViews.iterator();
        while (videoViewIterator.hasNext()) {
            MyVideoView myVideoView = videoViewIterator.next();
            if (myVideoView.getVisibility() == View.VISIBLE && myVideoView.isPlaying())
                myVideoView.pause();
        }
        //暂停广告计时
        isPaused = true;
    }

    /**
     * 广告播放
     */
    private void ADResume() {
        //所有视频暂停
        Iterator<MyVideoView> videoViewIterator = myVideoViews.iterator();
        while (videoViewIterator.hasNext()) {
            MyVideoView myVideoView = videoViewIterator.next();
            if (myVideoView.getVisibility() == View.VISIBLE && !myVideoView.isPlaying())
                myVideoView.start();
        }
        //唤醒广告线程
        if (isPaused == true) {
            isPaused = false;
            synchronized (lock) {
                Logs.i(TAG, "广告线程唤醒....");
                lock.notifyAll();
            }

        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //广告板报警按钮按下键值
        if (keyCode == 111) {
            //记录按下时间点
            downTime = System.currentTimeMillis();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!isRecord) {
                        //记录抬起时间点
                        upTime = System.currentTimeMillis();
                        if ((upTime - downTime) / 1000 >= 3) {
                            //报警
                            startVideoCall();
                            isRecord = true;
                            break;
                        }
                    }

                }
            }).start();
        }

        //广告板报警按钮抬起键值
        if (keyCode == 61) {
            isRecord = false;
        }
        return true;
    }

}
