package com.vunke.videochat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.vunke.videochat.callback.RegisterCallBack;

/**
 * Created by zhuxi on 2020/2/28.
 */

public class RegisterReceiver extends BroadcastReceiver {
    private static final String TAG = "RegisterReceiver";
    private RegisterCallBack callBack;
    public RegisterReceiver(RegisterCallBack callBack){
        this.callBack = callBack;
    }
    public RegisterCallBack getCallBack(){
        return callBack;
    }
    public void setCallBack(RegisterCallBack callBack){
        this.callBack = callBack;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("action");
        if (!TextUtils.isEmpty(action)){
            Log.i(TAG, "onReceive: get action:"+action);
            switch (action) {
                case "reg_state":
                    String loginStatus  = intent.getStringExtra("data");
                    if (!TextUtils.isEmpty(loginStatus)){
                        if (loginStatus.contains("success")){
                            if (callBack!=null){
                                callBack.onSuccess();
                            }
                        }else if (loginStatus.contains("progress")) {
                            Log.i(TAG, "onReceive: login in progress");
                        } else {
                            if (callBack!=null){
                                callBack.onFailed(loginStatus);
                            }
                        }
                    }
                    break;
                case "show_code":
                    Log.i("编码", "onReceive: "+intent.getStringExtra("data"));
                    break;
                case "show_version":

                    break;
                case "show_status":

                    break;
                default:
                    break;
            }
        }
    }
}
