package com.neal.adpannel.activity;
/**
 * 登陆页
 * Created by lichao on 17/5/2.
 */

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.neal.adpannel.R;
import com.neal.adpannel.service.WakeService;
import com.neal.adpannel.util.DeviceUtil;
import com.neal.adpannel.util.Logs;
import com.neal.adpannel.util.Toasts;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class LoginActivity extends BaseActivity {

    /**
     * 全局待唤醒Acitivity，应用里保持就一个这种页面
     */
    public static LoginActivity loginActivity;

    private static final String TAG = "LoginActivity";

    /**
     * 登陆
     */
    Button bt_login;

    /**
     * 锁屏
     */
    Button bt_scree_lock;

    /**
     * 用户名
     */
    EditText et_user;

    /**
     * 密码
     */
    EditText et_password;


    /**
     * 是否具有管理权限
     */
    private boolean isAdminActive;


    /**
     * 设备管理器
     */
    private DevicePolicyManager mDPM;

    /**
     * 唤醒屏幕意图
     */
    Intent wakeInent;

    @Override
    protected int setContent() {
        return R.layout.activity_login;
    }

    @Override
    protected void initResource() {
        requestPermissions();
        loginActivity = this;
        wakeInent = new Intent(this, WakeService.class);

    }

    @Override
    protected void initViews() {
        bt_login = (Button) findViewById(R.id.bt_login);
        bt_scree_lock = (Button) findViewById(R.id.bt_scree_lock);
        et_password = (EditText) findViewById(R.id.et_password);
        et_user = (EditText) findViewById(R.id.et_user);

    }

    @Override
    protected void setViews() {
        //登陆逻辑
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EMClient.getInstance().login(et_user.getText().toString(), et_password.getText().toString(), new EMCallBack() {//回调
                    @Override
                    public void onSuccess() {
                        //进入主页
                        EMClient.getInstance().groupManager().loadAllGroups();
                        EMClient.getInstance().chatManager().loadAllConversations();
                        Logs.d(TAG, "登录聊天服务器成功！");
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }

                    @Override
                    public void onError(int code, String message) {
                        Logs.d(TAG, "登录聊天服务器失败！");
                    }
                });
            }
        });

        //锁屏逻辑
        bt_scree_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                try {
//                    createSuProcess("reboot -p").waitFor(); //关机命令
//                    //createSuProcess("reboot").waitFor(); //这个部分代码是用来重启的
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

//                String cmd = "su -c reboot";
//                try {
//                    Runtime.getRuntime().exec(cmd);
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    new AlertDialog.Builder(LoginActivity.this).setTitle("Error").setMessage(
//                            e.getMessage()).setPositiveButton("OK", null).show();
//                }

//                shutDown();

                //开启定时解锁服务
                startService(wakeInent);
                //锁屏
                DeviceUtil.lockScreen(LoginActivity.this);
            }
        });


    }

    @Override
    protected void initTitleBar() {

    }

    @Override
    protected void setTitleBar() {

    }



    @TargetApi(23)
    private void requestPermissions() {
        // 申请管理权限
        DeviceUtil.initLockScreen(this);


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Toasts.makeText("您按下的按键键值为:" + keyCode);
        Logs.i(TAG,"onKeyDown event.getRepeatCount() "+event.getRepeatCount());
        return true;
    }


    private void shutDown() {
        Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    static Process createSuProcess() throws IOException {
        File rootUser = new File("/system/xbin/ru");
        if (rootUser.exists()) {
            return Runtime.getRuntime().exec(rootUser.getAbsolutePath());
        } else {
            return Runtime.getRuntime().exec("su");
        }
    }

    /**
     * 关机
     */
    static Process createSuProcess(String cmd) throws IOException {

        DataOutputStream os = null;
        Process process = createSuProcess();

        try {
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit $?\n");
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }

        return process;
    }


}
