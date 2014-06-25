package org.qiuwan.wifile.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import org.qiuwan.wifile.R;

/**
 * 弹出的对话框类.也是封装系统的Dialog类,可以不管.
 * Created by qiuwan.zheng on 14-2-20.
 */
public class DialogsUtil {

    public static final AlertDialog getAlertDialog(Context mContext, String mTitle, String mMessage,
                                                   String positiveString, DialogInterface.OnClickListener positiveCallback,
                                                   String negativeString, DialogInterface.OnClickListener negativeCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(mMessage).setTitle(mTitle);
        builder.setPositiveButton(positiveString, positiveCallback);
        builder.setNegativeButton(negativeString, negativeCallback);
        AlertDialog dialog = builder.create();
        return dialog;
    }

    public static final AlertDialog getAlertDialog(
            Context mContext, String mMessage,
            DialogInterface.OnClickListener positiveCallback,
            DialogInterface.OnClickListener negativeCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(mMessage)
                .setPositiveButton(mContext.getString(R.string.ok), positiveCallback)
                .setNegativeButton(mContext.getString(R.string.cancel), negativeCallback);
        return builder.create();
    }


}
