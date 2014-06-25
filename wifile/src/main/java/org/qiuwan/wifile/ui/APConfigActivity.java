package org.qiuwan.wifile.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.qiuwan.wifile.R;
import org.qiuwan.wifile.ap.Wifi;
import org.qiuwan.wifile.utils.DialogsUtil;
import org.qiuwan.wifile.utils.LogUtil;

import java.util.List;

/**
 * 设置AP的Activity.
 * Created by qiuwan.zheng on 14-3-3.
 */
public class APConfigActivity extends Activity {


    private Wifi mWifi;
    private int wifiAPStatus;

    private boolean stopUpdateUI = false;

    private ToggleButton apToggle;
    private TextView hintText;
    private ImageButton settingBtn;
    private String password;
    private String SSID;

    //用于更新UI的Handler,因为创建好了热点之后没有回调方法,所以只能通过不断地发送Handler来更新了.
    Handler UIUpdateHandler = new Handler();
    int updateTimes = 30;
    Runnable UIUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateWifiAPStatus();
            if (updateTimes > 0 && !stopUpdateUI) {
                updateTimes--;
                UIUpdateHandler.postDelayed(UIUpdateRunnable, 2000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap);

        mWifi = Wifi.getWifi(this.getApplicationContext());

        findView();
        updateWifiAPStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSSIDPassword();
    }

    private void updateSSIDPassword() {
        //这里要注意,使用的是全局的Context.因为要和后面的同步
        SharedPreferences sp = getApplicationContext().getSharedPreferences(SettingDialogActivity.SETTING_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        SSID = sp.getString("SSID", "QiuWan.Zheng");
        password = sp.getString("password", "12345678");
        LogUtil.i("Recovery. SSID = " + SSID + ", password = " + password);
    }

    private void logTheWifiConfigure() {
        List<WifiConfiguration> configurations = mWifi.getConfiguredNetworks();
        for (WifiConfiguration wifiConfiguration : configurations) {
            LogUtil.i("SSID = " + wifiConfiguration.SSID + ", password = " + wifiConfiguration.preSharedKey);
        }
    }

    /**
     * 获取View的引用,及设置View的响应事件.
     */
    private void findView() {
        apToggle = (ToggleButton) findViewById(R.id.ap_toggle_btn);
        hintText = (TextView) findViewById(R.id.ap_hint);
        settingBtn = (ImageButton) findViewById(R.id.ap_setting);
        apToggle.setOnCheckedChangeListener(new APOnCheckChangeListener());
        settingBtn.setOnClickListener(new SettingOnClickListener());
    }

    class SettingOnClickListener implements View.OnClickListener {

        private EditText SSIDEditText;
        private EditText passwordEditText;

        @Override
        public void onClick(View v) {
            startActivity(new Intent(APConfigActivity.this, SettingDialogActivity.class));
        }
    }


    /**
     * 当点击创建Toggle的时候会执行这里
     */
    class APOnCheckChangeListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                // 先轮询一遍 WIFI状态 和 WIFI AP 状态
                if (mWifi.getWifiApState() == Wifi.WIFI_AP_STATE_DISABLED) {
                    // 有当AP状态为Diabled的时候才可以创建.
                    final int wifiStatus = mWifi.getWifiStatus();
                    if (wifiStatus == Wifi.WIFI_STATE_ENABLED || wifiStatus == Wifi.WIFI_STATE_ENABLING) {
                        // WIFI已连接或正在连接,提示是否要断掉WIFI
                        DialogsUtil.getAlertDialog(APConfigActivity.this,
                                "WIFI已连接,创建热点需要断开当前WIFI连接",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        createAP();
                                    }
                                }, null
                        ).show();
                    } else {
                        createAP();
                    }
                }
            } else {
                if (mWifi.getWifiApState() == Wifi.WIFI_AP_STATE_ENABLED) {
                    disabledAP();
                }
            }
        }
    }

    private void disabledAP() {
        new AsyncSetAPTask().execute(false);
    }

    private void createAP() {
        new AsyncSetAPTask().execute(true);
    }


    /**
     * 刚进入Activity的时候,要获取AP状态,显示提示信息和设置按钮的状态.
     */
    private void updateWifiAPStatus() {
        wifiAPStatus = mWifi.getWifiApState();
        updateUI(wifiAPStatus);
    }

    /**
     * 更新提示信息
     *
     * @param status
     */
    private void updateUI(int status) {
        LogUtil.i("Update UI, status = " + status);
        String hint = getString(R.string.wifi_click_to_create);

        switch (status) {
            case Wifi.WIFI_AP_STATE_FAILED:
                //错误
                hint = getString(R.string.wifi_unknown_error);
                apToggle.setClickable(false);
                stopUpdateUI = true;
                break;
            case Wifi.WIFI_AP_STATE_ENABLED:
                //已开启
                updateSSIDPassword();
                hint = getString(R.string.wifi_created, SSID, password);
                apToggle.setChecked(true);
                stopUpdateUI = true;
                break;
            case Wifi.WIFI_STATE_DISABLED:
                //已关闭
                hint = getString(R.string.wifi_click_to_create);
                apToggle.setChecked(false);
                stopUpdateUI = true;
                break;
            case Wifi.WIFI_AP_STATE_DISABLING:
                //正在关闭
                hint = getString(R.string.wifi_disabling);
                break;
            case Wifi.WIFI_AP_STATE_ENABLING:
                //正在创建
                hint = getString(R.string.wifi_creating);
                break;
            default:
                break;
        }

        hintText.setText(hint);
    }

    /**
     * 按返回键,需要设置退出的动画.
     */
    @Override
    public void onBackPressed() {
        finish();
        //设置Activity切换动画
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * 在后台设置AP.因为创建AP的时间比较久,不能在UI线程,否则会阻塞UI线程,造成界面卡住.
     */
    public class AsyncSetAPTask extends AsyncTask<Boolean, Void, Void> {

        @Override
        protected Void doInBackground(Boolean... params) {
            if (mWifi == null) {
                mWifi = Wifi.getWifi(APConfigActivity.this.getApplicationContext());
            }
            if (params[0]) {
                mWifi.setAP(true, SSID, password);
            } else {
                mWifi.setAP(false, SSID, password);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            LogUtil.i("set ap ok. now, update UI");
            updateTimes = 15;
            stopUpdateUI = false;
            UIUpdateHandler.postDelayed(UIUpdateRunnable, 300);
        }
    }
}
