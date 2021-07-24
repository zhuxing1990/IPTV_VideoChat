package com.vunke.videochat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.vunke.videochat.login.LoginCallBack;
import com.vunke.videochat.login.LoginManage;
import com.vunke.videochat.login.UserInfoUtil;
import com.vunke.videochat.manage.RegisterManage;
import com.vunke.videochat.model.LoginInfo;
import com.vunke.videochat.tools.LinphoneMiniUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zhuxi on 2020/8/24.
 */

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "onReceive: is boot receiver");
        initTime(context);
    }

    private void initTime(final Context context) {
        Observable.interval(30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        LinphoneMiniUtils.initLinphoneService(context);
                        initLogin(context);
                        LoginManage.upLoginStatus(context,0);
                        onComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dispose();
                        initTime(context);
                    }

                    @Override
                    public void onComplete() {
                        dispose();
                    }
                });
    }

    private UserInfoUtil userInfoUtil;
    private LoginManage loginManage;
    private void initLogin(final Context context) {
        Log.i(TAG, "initLogin: ");
        userInfoUtil = UserInfoUtil.getInstance(context);
        String userId = userInfoUtil.getUserId();
        if (!TextUtils.isEmpty(userId)){
//            login_userId.setText("用户账号:"+userInfoUtil.getUserId());
            loginManage = new LoginManage();
            loginManage.startLogin(userId, new LoginCallBack() {
                @Override
                public void onSuccess(LoginInfo loginInfo) {
                    Log.i(TAG, "onSuccess: get loginInfo success:"+loginInfo.toString());
                    //用户已经注册，可以正常登录
                   String userName = loginInfo.getData().getUserName();
                    String passWord = loginInfo.getData().getPassword();
                    String domain = loginInfo.getData().getDomain();
                    try {
                        RegisterManage.Login(context,userName,passWord,domain);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailed(LoginInfo loginInfo) {
                    // 获取用户信息失败，无法登录
                    Log.i(TAG, "onSuccess: get loginInfo failed");
                }

                @Override
                public void onError() {
                    //网络异常
                    Log.i(TAG, "onSuccess: get loginInfo error");
                }
            });
        }
    }
}
