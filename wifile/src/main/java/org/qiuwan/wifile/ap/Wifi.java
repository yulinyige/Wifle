package org.qiuwan.wifile.ap;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Wifi管理类.单例模式.
 * Wifi有两种状态,1.正常的无线网卡模式,即可以连接其他设备.2.AP(Access Point)模式,可以让别人连接.相当于一个小型的路由器.
 * Created by qiuwan.zheng on 14-3-3.
 */
public class Wifi {

    /**
     * Wi-Fi is currently being disabled. The state will change to {@link #WIFI_STATE_DISABLED} if
     * it finishes successfully.
     */
    public static final int WIFI_STATE_DISABLING = 0;
    /**
     * Wi-Fi is disabled.
     */
    public static final int WIFI_STATE_DISABLED = 1;
    /**
     * Wi-Fi is currently being enabled. The state will change to {@link #WIFI_STATE_ENABLED} if
     * it finishes successfully.
     */
    public static final int WIFI_STATE_ENABLING = 2;
    /**
     * Wi-Fi is enabled.
     */
    public static final int WIFI_STATE_ENABLED = 3;
    /**
     * Wi-Fi is in an unknown state. This state will occur when an error happens while enabling
     * or disabling.
     */
    public static final int WIFI_STATE_UNKNOWN = 4;
    /**
     * Wi-Fi AP is currently being disabled. The state will change to
     * WIFI_AP_STATE_DISABLED if it finishes successfully.
     */
    public static final int WIFI_AP_STATE_DISABLING = 10;
    /**
     * Wi-Fi AP is disabled.
     */
    public static final int WIFI_AP_STATE_DISABLED = 11;
    /**
     * Wi-Fi AP is currently being enabled. The state will change to
     * WIFI_AP_STATE_ENABLED if it finishes successfully.
     */
    public static final int WIFI_AP_STATE_ENABLING = 12;
    /**
     * Wi-Fi AP is enabled.
     */
    public static final int WIFI_AP_STATE_ENABLED = 13;
    /**
     * Wi-Fi AP is in a failed state. This state will occur when an error occurs during
     * enabling or disabling
     */
    public static final int WIFI_AP_STATE_FAILED = 14;

    private static Wifi mWifi;
    private WifiManager mWifiManager;
    private Context mContext;

    private Wifi(Context mContext) {
        this.mContext = mContext;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    public static Wifi getWifi(Context mContext) {
        if (mWifi == null) {
            mWifi = new Wifi(mContext);
        }
        return mWifi;
    }

    /**
     * 通过java反射获取到Wifi的AP状态.注意是AP模式下的状态.
     *
     * @return
     */
    public int getWifiApState() {
        //默认设置错误.
        int i = WIFI_AP_STATE_FAILED;
        try {
            i = (Integer) mWifiManager.getClass().getMethod("getWifiApState", new Class[0]).invoke(this.mWifiManager);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return i;
    }

    public int getWifiStatus() {
        return mWifiManager.getWifiState();
    }

    public List<WifiConfiguration> getConfiguredNetworks() {
        return mWifiManager.getConfiguredNetworks();
    }

    // ------------- 分割线 ---------------------------------

    /**
     * 设置AP的状态,关闭或打开.
     */
    public void setAP(boolean enable, String SSID, String password) {

        Boolean state = getApState(mWifiManager);
        //控制状态.当设置的状态和现在的状态比较,如果一样就不用设置了.
        if (enable == state) {
            return;
        }

        //AP详细配置.
        WifiConfiguration apConfig = new WifiConfiguration();
        apConfig.SSID = SSID;
        apConfig.preSharedKey = password;
        apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        apConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

        Method method = null;
        try {
            method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        boolean enabled = !state;

        mWifiManager.setWifiEnabled(!enabled);
        try {
            method.invoke(mWifiManager, apConfig, enabled);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取AP是否打开
     *
     * @param wifi
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Boolean getApState(WifiManager wifi) {
        Method method = null;
        try {
            method = wifi.getClass().getMethod("isWifiApEnabled");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            return (Boolean) method.invoke(wifi);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }


}
