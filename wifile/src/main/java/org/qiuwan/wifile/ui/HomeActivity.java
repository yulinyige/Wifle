package org.qiuwan.wifile.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.qiuwan.wifile.R;
import org.qiuwan.wifile.service.WebService;
import org.qiuwan.wifile.utils.FileUtil;
import org.qiuwan.wifile.utils.LogUtil;
import org.qiuwan.wifile.utils.NetUtils;
import org.qiuwan.wifile.utils.ToastUtil;

/**
 * 已不用
 * 开启文件分享.
 * Note:1,不支持中文路径. 2, 无权限的下载会出错.文件名中包含"."号的容易出错.
 * TODO:当打开了共享之后,记得打开WifiLock.防止Wifi休眠
 * Created by qiuwan.zheng on 14-3-2.
 */
public class HomeActivity extends Activity {

    private TextView hintText;
    private ToggleButton serviceToggle;
    private View hintContainerView;
    private ImageView apImage;
    private ImageView[] circleAnimationImageView = new ImageView[3];

    private Bitmap circleImage;

    private Context mContext;

    private String IP;
    private Intent serviceIntent;

    private Animation fadeInAnimation;
    private Animation fadeOutAnimation;
    private boolean playAnimation = false;
    private Animation[] animation = new Animation[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mContext = this;
        serviceIntent = new Intent(this, WebService.class);

        //initView
        hintText = (TextView) findViewById(R.id.home_hint);
        serviceToggle = (ToggleButton) findViewById(R.id.home_toggle_btn);
        apImage = (ImageView) findViewById(R.id.home_ap);
        hintContainerView = findViewById(R.id.home_hint_container);
        circleAnimationImageView[0] = ((ImageView) findViewById(R.id.home_circleImage_1));
        circleAnimationImageView[1] = ((ImageView) findViewById(R.id.home_circleImage_2));
        circleAnimationImageView[2] = ((ImageView) findViewById(R.id.home_circleImage_3));
        setViewListener();

        initFile();
        fadeInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.tips_fade_in);
        fadeOutAnimation = AnimationUtils.loadAnimation(mContext, R.anim.tips_fade_out);
    }

    private void setViewListener() {
        //set service toggle button listener.
        serviceToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    boolean startOK = startWebService(false);
                    updateUI(startOK);
                } else {
                    //关闭服务
                    LogUtil.i("Service stop!");
                    stopWebService();
                    updateUI(false);
                }
            }
        });

        apImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, APConfigActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    /**
     * 控制服务的启动先判断是否已经启动
     *
     * @param serviceStop 确定是否已经停止,确定true,如果不确定是否已经停止,则传false,这用于重启的服务,因为重启的时候应该已经是停止了的.
     * @return true已经启动, false启动不成功
     */
    private boolean startWebService(boolean serviceStop) {
        //如果不确定已经停止了,不要进行服务的检测,
        if (!serviceStop && WebService.isStart(mContext)) {
            return true;
        }
        IP = updateIP();
        if (TextUtils.isEmpty(IP)) {
            //空的IP地址,说明没有WIFI或者没有启动WIFI...
            LogUtil.i("Invalid IP. address = " + IP);
            ToastUtil.showShort(mContext, "启动失败,请确认打开了Wifi.");
            serviceToggle.setChecked(false);
            return false;
        }

        LogUtil.i("Start Service. IP = " + IP);
        try {
            startService(serviceIntent);
            serviceToggle.setChecked(true);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * 停止Web服务.
     *
     * @return
     */
    private boolean stopWebService() {
        return stopService(serviceIntent);
    }

    /**
     * 更新UI,根据状态.
     *
     * @param isStarted
     */
    private void updateUI(boolean isStarted) {
        if (isStarted) {
            //显示Tips
            hintText.setText("Http://" + IP + ":1024");
            hintContainerView.startAnimation(fadeInAnimation);
            //播放动画
            initCircleAnimation();
        } else {
            //隐藏Tips
            hintContainerView.startAnimation(fadeOutAnimation);
            stopCircelAnimation();
        }
    }

    /**
     * 更新当前Wifi的IP地址.这个IP地址有两种模式,一种是无线网卡模式,一种是AP模式.如果未开启Wifi将获取不到IP
     *
     * @return Wifi的IP地址.如未开启WIFI, 将返回null.
     */
    private String updateIP() {
        String ip;
        // Get IP address
        ip = NetUtils.getLocalIPAddress(mContext);
        //如果IP地址是0.0.0.0,可能是启动了AP,所以还要用另外的方法获取.
        if (ip.equals("0.0.0.0")) {
            return NetUtils.getWifiApIpAddress();
        } else {
            return ip;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (WebService.isStart(mContext)) {
            //如果已经启动了,就更新IP地址.
            IP = updateIP();
            serviceToggle.setChecked(true);
        } else {
            serviceToggle.setChecked(false);
        }
    }

    /**
     * 复制Web页面所用到的文件到指定的目录
     */
    private void initFile() {
        //先检查是否存在指定的文件,如果有,说明已经复制了,就不必要再复制一遍了.
        if (FileUtil.isExistHtmlFile(mContext)) {
            return;
        }
        boolean isOk = FileUtil.assetsCopy(this);
        if (!isOk) {
            ToastUtil.showShort(mContext, "外部存储似乎出现了点问题,请检查SD卡.");
        }

    }

    /**
     * 开始播放动画的Handler
     * TODO:优化动画的播放方法.
     */
    Handler animationHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final int i = msg.arg1;
            if (msg.what == 1 && playAnimation) {
                circleAnimationImageView[i].startAnimation(animation[i]);
            }
        }
    };

    /**
     * 停止动画播放.
     */
    private void stopCircelAnimation() {
        playAnimation = false;
    }

    /**
     * 初始化动画.
     */
    private void initCircleAnimation() {
        playAnimation = true;
        if (circleImage == null) {
            circleImage = BitmapFactory.decodeResource(getResources(), R.drawable.circle);
        }
        for (int i = 0; i < circleAnimationImageView.length; i++) {
            circleAnimationImageView[i].setImageBitmap(circleImage);
            animation[i] = getAnim(i);
            Message msg = new Message();
            msg.what = 1;
            msg.arg1 = i;
            animationHandler.sendMessageDelayed(msg, i * 1000);
        }
    }

    /**
     * 获取WIFI水波的动画.因为动画使用了三个ImageView交替变大.
     *
     * @param i 第几个ImageView
     * @return 当前的ImageView的动画.
     */
    private Animation getAnim(final int i) {
        AnimationSet animation = (AnimationSet) AnimationUtils.loadAnimation(mContext, R.anim.circle);
        if (animation != null) {
            //设置动画监听,播放完一个之后,需要发送一个消息,继续播放下一个.
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Message msg = new Message();
                    msg.what = 1;
                    msg.arg1 = i;
                    animationHandler.sendMessage(msg);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
        return animation;
    }
}
