package com.neal.adpannel.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.IBinder;

import com.google.gson.Gson;
import com.neal.adpannel.MyApplication;
import com.neal.adpannel.activity.MainActivity;
import com.neal.adpannel.entity.StatusEntity;
import com.neal.adpannel.interf.onStatusChangedListener;
import com.neal.adpannel.util.InetAddressUtil;
import com.neal.adpannel.util.Logs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class UDPService extends Service {
    /**
     * 状态更新监听
     */
    private onStatusChangedListener onStatusChangedListener = MainActivity.onStatusChangedListener;

    /**
     * 解析单片机数据
     */
    Gson gson = new Gson();

    public UDPService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //以太网IPv4地址
                    String ipaddress = "";
                    try {
                        for (Enumeration<NetworkInterface> en = NetworkInterface
                                .getNetworkInterfaces(); en.hasMoreElements();) {
                            NetworkInterface intf = en.nextElement();
                            if (intf.getName().toLowerCase().equals("eth0") || intf.getName().toLowerCase().equals("wlan0")) {
                                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                                    InetAddress inetAddress = enumIpAddr.nextElement();
                                    if (!inetAddress.isLoopbackAddress()) {
                                        ipaddress = inetAddress.getHostAddress().toString();
                                        if(!ipaddress.contains("::")){//ipV6的地址
                                            Logs.i("UDP地址", ipaddress);
                                        }
                                    }
                                }
                            } else {
                                continue;
                            }
                        }
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }

                    InetAddress inetAddress = InetAddress.getByName(ipaddress);
                    DatagramSocket server = new DatagramSocket(5050, inetAddress);

                    byte[] recvBuf = new byte[1024];
                    DatagramPacket recvPacket
                            = new DatagramPacket(recvBuf, recvBuf.length);
                    while (true) {
                        //接收数据
                        server.receive(recvPacket);
                        String recvStr = new String(recvPacket.getData(), 0, recvPacket.getLength());
                        //通知更新状态
                        StatusEntity statusEntity = gson.fromJson(recvStr, StatusEntity.class);
                        onStatusChangedListener.update(statusEntity);
                        Logs.i("UDP", recvStr);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();




        return super.onStartCommand(intent, flags, startId);
    }
}
