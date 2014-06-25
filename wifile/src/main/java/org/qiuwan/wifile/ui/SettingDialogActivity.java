package org.qiuwan.wifile.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.qiuwan.wifile.R;
import org.qiuwan.wifile.utils.ToastUtil;

/**
 * 设置WIfi的SSID和密码的Dialog
 * Created by qiuwan.zheng on 14-3-3.
 */
public class SettingDialogActivity extends Activity {

    public static final String SETTING_SHARED_PREFERENCE = "setting_shared_preference";
    private EditText passwordEditText;
    private EditText SSIDEditText;
    private TextView cancelBtn;
    private TextView okBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_setting);

        SSIDEditText = (EditText) findViewById(R.id.wifi_SSID);
        passwordEditText = (EditText) findViewById(R.id.wifi_password);
        cancelBtn = (TextView) findViewById(R.id.setting_cancel);
        okBtn = (TextView) findViewById(R.id.setting_ok);
        // 确定按钮的监听.
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String SSID = SSIDEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (TextUtils.isEmpty(SSID)) {
                    SSIDEditText.setHint("SSID不能为空");
                    return;
                }
                if (TextUtils.isEmpty(password) || password.length() < 8) {
                    passwordEditText.setHint("亲,密码不能少于8位");
                    passwordEditText.setText("");
                    return;
                }
                saveSharedPreference(SSID, password);
                ToastUtil.showShort(SettingDialogActivity.this, "保存设置成功!");
                onBackPressed();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void saveSharedPreference(String SSID, String password) {
        SharedPreferences sp = getApplicationContext().getSharedPreferences(SETTING_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        sp.edit().putString("SSID", SSID).putString("password", password).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        try {
            outState.putString("SSID", SSIDEditText.getText().toString());
            outState.putString("password", passwordEditText.getText().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        super.onSaveInstanceState(outState);
    }
}
