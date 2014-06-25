package org.qiuwan.wifile.service;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import org.qiuwan.wifile.ap.Wifi;
import org.qiuwan.wifile.server.WebServer;
import org.qiuwan.wifile.utils.FileUtil;
import org.qiuwan.wifile.utils.LogUtil;
import org.qiuwan.wifile.utils.NetUtils;

import java.util.List;

/**
 * 已不用
 * WebServer的服务.
 * 如果需要Web服务,先在后台启动一个Service,再在这个Service上启动服务器WebServer.保证当HomeActivity退出之后,Web服务不会中断.
 * Created by qiuwan.zheng on 14-3-3.
 */
public class WebService extends Service {

    public static final String WEB_SERVICE = "org.qiuwan.wifile.service.WebService";

    // Default WebServer port is 1024.
    public static final int PORT = 1024;
    // WebRoot根目录
    public static final String WEBROOT = "/";

    public static String htmlFilePath = "/storage/sdcard0/Android/data/org.qiuwan.wifile/files/wifile";

    // 需要的服务
    private WebServer webServer;

    private IntentFilter wifiIntentFileter;
    private WifiChangeReciver wifiChangeReciver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 服务启动的时候,new一个WebServer
     */
    @Override
    public void onCreate() {
        super.onCreate();
        //需要初始化Html目录
        htmlFilePath = FileUtil.getHtmlFilePath(WebService.this);
        wifiIntentFileter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
        webServer = new WebServer(PORT, WEBROOT);
    }

    /**
     * 启动WebServer
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        webServer.setDaemon(true);
        webServer.start();
        // 监听Wifi变化
        wifiChangeReciver = new WifiChangeReciver();
        registerReceiver(wifiChangeReciver, wifiIntentFileter);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        webServer.close();
        try {
            unregisterReceiver(wifiChangeReciver);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        LogUtil.i("Webservice Stop!");
        super.onDestroy();
    }

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

    // 监听Wifi的变化
    public class WifiChangeReciver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(wifiIntentFileter.getAction(0))) {
                int networkStatus = NetUtils.getWifiStatus(WebService.this);
                int APStatus = NetUtils.getWifiApState(WebService.this);
                // 只有在两种情况下关闭服务.1.WifiAP is enable
                if (networkStatus == WifiManager.WIFI_STATE_DISABLED && APStatus == Wifi.WIFI_AP_STATE_DISABLED) {
                    stopService(new Intent(WebService.this, WebService.class));
                    LogUtil.i("Wifi status change to disable. stop the webservice");
                }
            }
        }
    }
}
