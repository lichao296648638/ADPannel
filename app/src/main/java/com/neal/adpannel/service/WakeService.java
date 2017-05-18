package com.neal.adpannel.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;

import com.neal.adpannel.activity.LoginActivity;
import com.neal.adpannel.util.DeviceUtil;


public class WakeService extends Service {
    /**
     * 唤醒屏幕接收器action
     */
    private final String WAKE_ACTION = "com.neal.rems.WAKE";


    /**
     * 系统闹钟
     */
    AlarmManager am;

    /**
     * 解锁意图
     */
    Intent wakeIntent;

    /**
     * 延迟解锁意图
     */
    PendingIntent pi;

    /**
     * 唤醒广播接受者
     */
    WakeReceiver wr;


    public WakeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化系统闹钟
        am = (AlarmManager) getSystemService(ALARM_SERVICE);
        //设置解锁action
        wakeIntent = new Intent(WAKE_ACTION);
        //设置五秒后发送唤醒广播
        pi = PendingIntent.getBroadcast(this, 0, wakeIntent, 0);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //注册唤醒屏幕接收器
        registReceiver();
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 5 * 1000, pi);
        return super.onStartCommand(intent, flags, startId);
    }


    public class WakeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //解锁
            DeviceUtil.wakeScreen(LoginActivity.loginActivity);
            //取消闹钟服务
            am.cancel(pi);
            //注销接受者
            unregisterReceiver(wr);
            //停止服务
            stopSelf();
        }

    }

    /**
     * 注册动态唤醒广播
     */
    public void registReceiver() {
         wr = new WakeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WAKE_ACTION);
        registerReceiver(wr, filter);
    }
}
