package com.neal.adpannel.activity;
/**
 * 报警demo
 * Created by lichao on 17/5/2.
 */

import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.util.ArraySet;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.chat.EMClient;
import com.neal.adpannel.R;
import com.neal.adpannel.ease.receiver.CallReceiver;
import com.neal.adpannel.ease.ui.VideoCallActivity;
import com.neal.adpannel.entity.AdEntity;
import com.neal.adpannel.entity.EventEntity;
import com.neal.adpannel.entity.StatusEntity;
import com.neal.adpannel.interf.onDownLoadListener;
import com.neal.adpannel.interf.onStatusChangedListener;
import com.neal.adpannel.service.UDPService;
import com.neal.adpannel.util.Downloader;
import com.neal.adpannel.util.Logs;
import com.neal.adpannel.util.MyVideoView;
import com.neal.adpannel.util.NetUtil;
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


public class MainActivity extends BaseActivity implements onStatusChangedListener {

    private static final String TAG = "MainActivity";

    /**
     * 状态更新监听器
     */
    public static onStatusChangedListener onStatusChangedListener;


    /**
     * 是否需要重新下载资源
     */
    boolean isReDownload = false;

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
    Downloader downloader;

    /**
     * 广告场景编辑事件时间戳
     */
    EventEntity eventEntity = new EventEntity();

    /**
     * 当前轮播组的广告元素下标
     */
    volatile int[] indexArr;

    /**
     * 当前场景下标
     */
    int mSceneIndex;

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
     * 场景播放时间组
     */
    int[] sceneTimes;

    /**
     * 主线程
     */
    Thread mainThread;

    /**
     * 当前广告轮播线程停止标志
     */
    boolean[] sceneTags;

    /**
     * 电梯状态UI组
     */
    TextView tv_station, tv_status, tv_safety, tv_overload, tv_power, tv_brk, tv_temp_hum;

    /**
     * 左右箭头
     */
    ImageView iv_arrow_left, iv_arrow_right;

    /**
     * 上次方向状态 0停 1上 2下
     */
    int lastState;

    /**
     * 数据库操作对象
     */
    SQLiteDatabase db;
    String str_Update = "{\"time_stamp\":\"2017101010101\"}";
    String str_Ad = "[{\"type\":2,\"width\":768,\"height\":1366,\"left\":0,\"top\":0,\"data\":\"https://ooo.0o0.ooo/2017/06/27/59521bf1612a3.jpg\",\"file_name\":\"image.jpg\",\"time_stamp\":\"image\",\"banner_group\":\"2\",\"scene_group\":\"111\"},{\"type\":1,\"height\":350,\"width\":550,\"left\":110,\"top\":340,\"data\":\"http://flv2.bn.netease.com/videolib3/1604/28/fVobI0704/SD/fVobI0704-mobile.mp4\",\"file_name\":\"video.mp4\",\"time_stamp\":\"video\",\"banner_group\":\"222\",\"scene_group\":\"111\"}]";
//    String str_Ad = "[{\"font_bold\":false,\"font_size\":30,\"data\":\"测试数据\",\"top\":0,\"time_stamp\":\"1\",\"height\":60,\"width\":400,\"file_name\":\"picture1.jpg\",\"font_color\":\"#FF0000\",\"set_type\":0,\"type\":0,\"font_family\":0,\"left\":600,\"play_time\":3,\"banner_group\":\"333\",\"scene_group\":\"1\",\"scene_time\":10}]";

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
        //设置监听器
        onStatusChangedListener = this;
//        mvv_ad = (MyVideoView) findViewById(R.id.mvv_ad);
        fm_container = (FrameLayout) findViewById(R.id.fm_container);
        //监听呼入电话
        IntentFilter callFilter = new IntentFilter(EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        callReceiver = new CallReceiver();
        registerReceiver(callReceiver, callFilter);
        tv_station = (TextView) findViewById(R.id.tv_station);
        tv_status = (TextView) findViewById(R.id.tv_status);
        tv_brk = (TextView) findViewById(R.id.tv_brk);
        tv_temp_hum = (TextView) findViewById(R.id.tv_temp_hum);
        tv_overload = (TextView) findViewById(R.id.tv_overload);
        tv_power = (TextView) findViewById(R.id.tv_power);
        tv_safety = (TextView) findViewById(R.id.tv_safety);
        tv_status = (TextView) findViewById(R.id.tv_status);
        iv_arrow_left = (ImageView) findViewById(R.id.iv_arrow_left);
        iv_arrow_right = (ImageView) findViewById(R.id.iv_arrow_right);
        //开启UDP服务
        Intent intent = new Intent(this, UDPService.class);
        startService(intent);
    }

    @Override
    protected void setViews() {
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
            startActivity(new Intent(this, VideoCallActivity.class).putExtra("username", "637")
                    .putExtra("isComingCall", false));
        }
    }

    /**
     * 从本地数据库中播放广告节点
     */
    private void playElements() {
        //装载所有广告列表
        //所有的场景列表
        final List<List<AdEntity>> allScene = new ArrayList<List<AdEntity>>();
        //获取本地数据中所有节目元素
        List<AdEntity> adEntities = DataSupport.findAll(AdEntity.class);
        //装载所有场景ID
        AdEntity adEntity = null;
        Iterator<AdEntity> adIterator = adEntities.iterator();
        Set<String> sceneGroup = new ArraySet<>();
        while (adIterator.hasNext()) {
            adEntity = adIterator.next();
            sceneGroup.add(adEntity.getScene_group());
        }
        //遍历所有场景组
        Iterator<String> groupIdIterator = sceneGroup.iterator();
        while (groupIdIterator.hasNext()) {
            String sceneId = groupIdIterator.next();
            List<AdEntity> singleScene = DataSupport.where("scene_group = ?", sceneId).find(AdEntity.class);
            //添加场景至所有场景列表
            allScene.add(singleScene);
        }
        //初始化场景播放时间
        sceneTimes = new int[allScene.size()];
        //初始化场景线程标志位
        sceneTags = new boolean[allScene.size()];
        for (int i = 0; i < sceneTags.length; i++) {
            sceneTags[i] = true;
        }
        for (int i = 0; i < sceneTimes.length; i++) {
            sceneTimes[i] = allScene.get(i).get(0).getScene_time();
        }
        //开始执行场景线程
        mainThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //场景播放的开始时间
                long sceneStartTime = System.currentTimeMillis();
                //当前场景执行时间
                long sceneCurrentTime = System.currentTimeMillis();
                //单个场景的所有广告
                List<AdEntity> singleSceneAds = allScene.get(mSceneIndex);
                //单个场景的所有广告组
                List<List<AdEntity>> allList = new ArrayList<>();
                //装载所有节目组ID
                AdEntity adEntity = null;
                Iterator<AdEntity> adIterator = singleSceneAds.iterator();
                Set<String> adGroup = new ArraySet<>();
                while (adIterator.hasNext()) {
                    adEntity = adIterator.next();
                    adGroup.add(adEntity.getBanner_group());
                }
                //遍历所有节目组
                Iterator<String> groupIdIterator = adGroup.iterator();
                while (groupIdIterator.hasNext()) {
                    String groupId = groupIdIterator.next();
                    List<AdEntity> singleList = DataSupport.where("banner_group = ? and scene_group = ?", groupId, singleSceneAds.get(0).getScene_group()).find(AdEntity.class);
                    //添加该节目组至所有节目列表
                    allList.add(singleList);
                }
                //初始化每个节目组的节目下标
                indexArr = new int[allList.size()];
                //遍历所有的节目列表
                for (int i = 0; i < allList.size(); i++) {
                    //当前广告组下标
                    final int currGroupIndex = i;
                    final List<AdEntity> singleList = allList.get(i);
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
                    excuteAdTask(singleList, currGroupIndex, AdTime, mSceneIndex);
                }
                while (allScene.size() != 1) {
                    //判断是否进入下一场景
                    sceneCurrentTime = System.currentTimeMillis();
                    if ((sceneCurrentTime - sceneStartTime) / 1000 >= sceneTimes[mSceneIndex]) {
                        //切换场景
                        switchScene(allScene, mSceneIndex);
                        //重新计时
                        sceneStartTime = System.currentTimeMillis();
                    }
                }
            }
        });

        mainThread.start();
    }


    /**
     * 执行轮播广告的任务
     *
     * @param singleList     广告组
     * @param currGroupIndex 当前广告组下标
     * @param AdTime         广告播放时长
     * @param sceneIndex     广告组所属的场景
     */
    private void excuteAdTask(final List<AdEntity> singleList, final int currGroupIndex, final int[] AdTime, final int sceneIndex) {
        //开启线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                //初次执行任务
                boolean isFirst = true;
                //记录任务开始时间
                long startTime = System.currentTimeMillis();

                //开始循环
                while (sceneTags[sceneIndex]) {
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
                        //切换广告
                        switchAd(singleList, indexArr[currGroupIndex]);
                        //重新开始计算任务开始时间
                        startTime = System.currentTimeMillis();
                        //非初次了
                        isFirst = false;
                    }
                    //记录任务当前时间
                    long currentTime = System.currentTimeMillis();
                    //任务已运行时间
                    long elapsedTime = currentTime - startTime;
                    //轮播组只有一条广告，不切换
                    if (singleList.size() == 1)
                        return;
                    //判断是否切换广告元素，非视频
                    if (singleList.get(indexArr[currGroupIndex]).getType() != 1) {
                        if (elapsedTime >= AdTime[indexArr[currGroupIndex]] * 1000) {
                            //记录广告数组下标+1
                            indexArr[currGroupIndex]++;
                            //循环
                            if (indexArr[currGroupIndex] == AdTime.length)
                                indexArr[currGroupIndex] = 0;
                            //切换广告
                            switchAd(singleList, indexArr[currGroupIndex]);
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
                            switchAd(singleList, indexArr[currGroupIndex]);
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
        //总元素个数
        allFiles = adEntities.size();
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
                //文字
                case 0:
                    allFiles--;
                    adEntity.save();
                    //所有文件下载完毕，开始播放呀！
                    if (allFiles == 0) {
                        //缓存广告编辑事件时间戳
                        ContentValues values = new ContentValues();
                        values.put("time_stamp", "2017101010101");
                        DataSupport.update(EventEntity.class, values, 1);
                        playElements();
                    }
                    break;
                //视频
                case 1:
                    //文件路径入库
                    adEntity.setFile_path(context.getFilesDir().getAbsolutePath() + "/" + adEntity.getFile_name());
                    adEntity.save();
                    //设置视频下载器回调
                    downloader = new Downloader(this);
                    //下载视频并保存，返回本地储存的文件对象
                    downloader.saveFile(context.getFilesDir().getAbsolutePath(), adEntity.getFile_name(), adEntity.getData(), new onDownLoadListener() {
                        @Override
                        public void success(File data) {
                            //下载成功，待下载文件数 -1
                            allFiles--;
                            //所有文件下载完毕，开始播放呀！
                            if (allFiles == 0) {
                                //缓存广告编辑事件时间戳
                                ContentValues values = new ContentValues();
                                values.put("time_stamp", "2017101010101");
                                DataSupport.update(EventEntity.class, values, 1);
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
                    //文件路径入库
                    adEntity.setFile_path(context.getFilesDir().getAbsolutePath() + "/" + adEntity.getFile_name());
                    adEntity.save();
                    //设置图片下载器回调
                    downloader = new Downloader(this);
                    //下载图片并保存，返回本地储存的文件对象
                    downloader.saveFile(context.getFilesDir().getAbsolutePath(), adEntity.getFile_name(), adEntity.getData(), new onDownLoadListener() {
                        @Override
                        public void success(File data) {
                            //下载成功，待下载文件数 -1
                            allFiles--;
                            //所有文件下载完毕，开始播放呀！
                            if (allFiles == 0) {
                                //缓存广告编辑事件时间戳
                                ContentValues values = new ContentValues();
                                values.put("time_stamp", "2017101010101");
                                DataSupport.update(EventEntity.class, values, 1);
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

    /**
     * 切换场景
     *
     * @param allScene 所有场景的广告组
     * @param index    当前场景下标
     */
    public void switchScene(List<List<AdEntity>> allScene, int index) {
        //设置场景所属线程的标志位
        sceneTags[index] = false;
        //遍历屏蔽当前场景元素
        for (int i = 0; i < allScene.get(index).size(); i++) {
            switch (allScene.get(index).get(i).getType()) {
                //文本
                case 0:
                    //找到该子View，屏蔽
                    final TextView tv_ad = (TextView) fm_container.findViewWithTag(allScene.get(index).get(i).getTime_stamp());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (tv_ad != null)
                                fm_container.removeView(tv_ad);
                        }
                    });
                    break;
                //视频
                case 1:
                    //找到该子View，屏蔽
                    final MyVideoView mvv_ad = (MyVideoView) fm_container.findViewWithTag(allScene.get(index).get(i).getTime_stamp());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mvv_ad != null)
                                fm_container.removeView(mvv_ad);
                        }
                    });
                    break;
                //图片
                case 2:
                    //找到该子View，屏蔽
                    final ImageView iv_ad = (ImageView) fm_container.findViewWithTag(allScene.get(index).get(i).getTime_stamp());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (iv_ad != null)
                                fm_container.removeView(iv_ad);
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
        //播放下一场景
        mSceneIndex++;
        if (mSceneIndex == allScene.size()) {
            mSceneIndex = 0;
        }
        //下一场景线程标志位启动
        sceneTags[mSceneIndex] = true;
        //单个场景的所有广告
        List<AdEntity> singleSceneAds = allScene.get(mSceneIndex);
        //单个场景的所有广告组
        List<List<AdEntity>> allList = new ArrayList<>();
        //装载所有节目组ID
        AdEntity adEntity = null;
        Iterator<AdEntity> adIterator = singleSceneAds.iterator();
        Set<String> adGroup = new android.support.v4.util.ArraySet<String>();
        while (adIterator.hasNext()) {
            adEntity = adIterator.next();
            adGroup.add(adEntity.getBanner_group());
        }
        //遍历所有节目组
        Iterator<String> groupIdIterator = adGroup.iterator();
        while (groupIdIterator.hasNext()) {
            String groupId = groupIdIterator.next();
            List<AdEntity> singleList = DataSupport.where("banner_group = ? and scene_group = ?", groupId, singleSceneAds.get(0).getScene_group()).find(AdEntity.class);
            //添加该节目组至所有节目列表
            allList.add(singleList);
        }
        //初始化每个节目组的节目下标
        indexArr = new int[allList.size()];
        //遍历所有的节目列表
        for (int i = 0; i < allList.size(); i++) {
            //当前广告组下标
            final int currGroupIndex = i;
            final List<AdEntity> singleList = allList.get(i);
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
            excuteAdTask(singleList, currGroupIndex, AdTime, mSceneIndex);
        }

    }

    /**
     * 轮播一组广告
     *
     * @param singleList 广告组
     * @param index      当前广告组中的广告下标
     */
    public void switchAd(final List<AdEntity> singleList, int index) {
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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                    }
                });
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
        //去掉虚拟按键全屏显示
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
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


    @Override
    public void onNetChange(int netMobile) {
        //网络状态变化时的操作
        if (netMobile == NetUtil.NETWORK_NONE) {
            //失去连接,判断文件是否重新下载
            if (allFiles != 0) {
                isReDownload = true;
            }
        } else {
            //重新下载资源
            if (isReDownload) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                    saveElements(getJson("http://192.168.1.106:8000/api/mips/material/"));
                        saveElements(str_Ad);
                        //重置下载标志位
                        isReDownload = false;
                    }
                }).start();
            }
        }
    }

    @Override
    public void update(final StatusEntity status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (status.getSafety()) {
                    case 0:
                        tv_safety.setText("安全回路:闭合");
                        break;
                    case 1:
                        tv_safety.setText("安全回路:断开");
                        break;
                }

                String station = "0";


                if (status.getStation() <= 6) {
                    station = status.getStation() + "";
                    if (status.getStation() == 1) {
                        station = "M";
                    }

                    if (status.getStation() == 0) {
                        station = "1";
                    }

                    if (status.getStation() == 6) {
                        station = "7";
                    }
                }

                if (status.getStation() > 6) {
                    station = status.getStation() + 2 + "";
                }
                tv_station.setText(station);

                switch (status.getStatus()) {
                    case 0:
                        tv_status.setText("运行状态:正常");
                        break;
                    case 1:
                        tv_status.setText("运行状态:检修");
                        break;
                }

                switch (status.getDirection()) {
                    //停止
                    case 0:
                        if (lastState == 1) {
                            Glide.with(MainActivity.this).load(R.drawable.up_yellow).into(iv_arrow_left);
                            Glide.with(MainActivity.this).load(R.drawable.down_green).into(iv_arrow_right);
                        } else if (lastState == 2) {
                            Glide.with(MainActivity.this).load(R.drawable.up_green).into(iv_arrow_left);
                            Glide.with(MainActivity.this).load(R.drawable.down_yellow).into(iv_arrow_right);
                        }
                        lastState = 0;
                        break;
                    //上行
                    case 1:
                        if (lastState != 1) {
                            Glide.with(MainActivity.this).load(R.drawable.up_anim).into(iv_arrow_left);
                            Glide.with(MainActivity.this).load(R.drawable.down_green).into(iv_arrow_right);
                        }
                        lastState = 1;
                        break;
                    //下行
                    case 2:
                        if (lastState != 2) {
                            Glide.with(MainActivity.this).load(R.drawable.up_green).into(iv_arrow_left);
                            Glide.with(MainActivity.this).load(R.drawable.down_anim).into(iv_arrow_right);
                        }
                        lastState = 2;
                        break;
                }

                switch (status.getOverload()) {
                    case 0:
                        tv_overload.setText("载重状态:正常");
                        break;
                    case 1:
                        tv_overload.setText("载重状态:超载");
                        break;
                }

                switch (status.getPower()) {
                    case 0:
                        tv_power.setText("系统电源:正常");
                        break;
                    case 1:
                        tv_power.setText("系统电源:掉电");
                        break;
                }

                switch (status.getBrk()) {
                    case 0:
                        tv_brk.setText("制动器:断开");
                        break;
                    case 1:
                        tv_brk.setText("制动器:闭合");
                        break;
                }

                tv_temp_hum.setText("温度/湿度:" + status.getTemp() + "℃/" + status.getHum() + "%");

            }
        });

    }
}
