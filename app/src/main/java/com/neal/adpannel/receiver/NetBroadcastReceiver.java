package com.neal.adpannel.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.neal.adpannel.activity.BaseActivity;
import com.neal.adpannel.util.NetUtil;


/**
 * Created by lichao on 2017/6/3.
 */
public class NetBroadcastReceiver extends BroadcastReceiver {
    public NetEvent event = BaseActivity.event;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            int netWorkState = NetUtil.getNetWorkState(context);
            // 接口回调传过去状态的类型
            event.onNetChange(netWorkState);
        }
    }


    // 自定义接口
    public interface NetEvent {
        void onNetChange(int netMobile);
    }
}
