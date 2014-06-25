package org.qiuwan.wifile;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

/**
 * 全局的Application.
 * 现在只是用来获取Context上下文.可以不管的.
 * Created by qiuwan.zheng on 14-3-2.
 */
public class WifileApplication extends Application {
    private static Context mContext;

    public static Context getContext(){
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }
}
