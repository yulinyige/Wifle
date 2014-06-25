package org.qiuwan.wifile.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import org.qiuwan.wifile.ap.Wifi;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 网络相关的工具类.获取IP和判断网络是否可用.
 * Created by qiuwan.zheng on 14-3-2.
 */
public class NetUtils {

    /**
     * 正常获取IP地址的方式.
     * 但是在AP模式下,获取到的地址是0.0.0.0
     * 这个时候可以使用奇葩的获取方式
     *
     * @param mContext
     * @return
     */
    public static String getLocalIPAddress(Context mContext) {
        WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }

    /**
     * 判断网络是否可用
     *
     * @warning need ACCESS_NETWORK_STATE permission
     */
    public static boolean isNetworkAvailable(Context mContext) {
        ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (null == info) {
            return false;
        }
        return info.isAvailable();
    }

    /**
     * 奇葩获取IP地址的方式.
     * 遍历所有网络接口,看接口名称是否包含有wlan,如果有,就认为是Wifi.
     *
     * @return IP地址.
     */
    public static String getWifiApIpAddress() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().contains("wlan")) {
                    Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                    while (enumIpAddr.hasMoreElements()) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && (inetAddress.getAddress().length == 4)) {
                            LogUtil.i(inetAddress.getHostAddress());
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            LogUtil.i(ex.toString());
        }
        return null;
    }

    public static int getWifiStatus(Context mContext) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getWifiState();
    }

    /**
     * 通过java反射获取到Wifi的AP状态.注意是AP模式下的状态.
     *
     * @return
     */
    public static int getWifiApState(Context mContext) {
        //默认设置错误.
        int i = Wifi.WIFI_AP_STATE_FAILED;
        try {
            WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            i = (Integer) mWifiManager.getClass().getMethod("getWifiApState", new Class[0]).invoke(mWifiManager);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return i;
    }

}
