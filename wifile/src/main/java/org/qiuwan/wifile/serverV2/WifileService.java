package org.qiuwan.wifile.serverV2;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import com.sun.net.httpserver.HttpServer;

import org.qiuwan.wifile.ap.Wifi;
import org.qiuwan.wifile.utils.LogUtil;
import org.qiuwan.wifile.utils.NetUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * 启动Web服务.
 * Created by qiuwan.zheng on 2014/4/16.
 */
public class WifileService extends Service {

    public static final String WEB_SERVICE = "org.qiuwan.wifile.serverV2.WifileService";
    public static IntentFilter WifiIntentFilter;
    private WifiChangeReceiver wifiChangeReceiver;

    private static boolean sRunning = false;
    private int mPort = 1024;
    private HttpServer server;

    @Override
    public void onCreate() {
        super.onCreate();
        WifiIntentFilter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            mPort = intent.getIntExtra("port", 1024);
        }
        startServer();

        wifiChangeReceiver = new WifiChangeReceiver();
        registerReceiver(wifiChangeReceiver, WifiIntentFilter);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(wifiChangeReceiver);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        stopServer();
        LogUtil.i("Webservice Stop!");
        super.onDestroy();
    }

    public static boolean isRunning() {
        return isRunning();
    }

    private boolean startServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(mPort), 0);
            server.createContext("/", new WifileHandler());
            server.setExecutor(null);
            server.start();
            sRunning = true;
            LogUtil.i("web 服务启动成功.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sRunning;
    }

    /**
     * 停止Web服务器.
     */
    private void stopServer() {
        if (server != null) {
            try {
                server.stop(0);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            server = null;
            sRunning = false;
        }
    }

    /**
     * Web服务是否已经启动.
     *
     * @param mContext 上下文.
     * @return
     */
    public static boolean isStart(Context mContext) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceInfos = manager.getRunningServices(50);
        if (serviceInfos.isEmpty()) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo info : serviceInfos) {
            if (WEB_SERVICE.equals(info.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 监听Wifi的变化.当IP地址变化的情况下,决定是否要关闭Web服务.
     */
    public class WifiChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiIntentFilter.getAction(0))) {
                int networkStatus = NetUtils.getWifiStatus(WifileService.this);
                int APStatus = NetUtils.getWifiApState(WifileService.this);
                // 只有在两种情况下关闭服务.1.WifiAP is enable
                if (networkStatus == WifiManager.WIFI_STATE_DISABLED && APStatus == Wifi.WIFI_AP_STATE_DISABLED) {
                    stopSelf();
                    LogUtil.i("Wifi status change to disable. stop the webservice");
                }
            }
        }
    }

}
