package org.qiuwan.wifile.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.qiuwan.wifile.R;

/**
 * Toast工具,更方便的显示Toast.
 * Created by qiuwan.zheng on 14-2-16.
 */
public class ToastUtil {

    private static Toast sToast = null;
    private static TextView toastText;

    private ToastUtil() {
    }

    public static void setToast(Toast toast) {
        if (sToast != null)
            sToast.cancel();
        sToast = toast;
    }

    public static void cancelToast() {
        if (sToast != null)
            sToast.cancel();
        sToast = null;
    }

    public static void show(Context context, String text, int duration) {
        cancelToast();
        View view = LayoutInflater.from(context).inflate(R.layout.custom_toast, null);
        toastText = (TextView) view.findViewById(R.id.toast_text);
        sToast = new Toast(context);
        toastText.setText(text);
        sToast.setView(view);
        sToast.setDuration(duration);
        sToast.show();
    }

    public static void showShort(Context context, String text) {
        show(context, text, Toast.LENGTH_SHORT);
    }

}
