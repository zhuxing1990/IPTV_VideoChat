package com.vunke.videochat.login;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.vunke.videochat.base.BaseConfig;
import com.vunke.videochat.model.LoginInfo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zhuxi on 2020/2/27.
 */

public class LoginManage {
    private static final String TAG = "LoginManage";
    public void startLogin(String userId, LoginCallBack loginCallBack){
        login(userId,loginCallBack);
    }

    private void login(String userId,final LoginCallBack loginCallBack) {
        try {
            JSONObject json = new JSONObject();
            json.put("userId",userId);
            OkGo.<String>post(BaseConfig.BASE_URL+ BaseConfig.LOGIN)
//                    .upJson(json.toString())
                    .upString(json.toString())
//                    .params(json.toString())
                    .tag(TAG)
            .execute(new StringCallback() {
                @Override
                public void onSuccess(Response<String> response) {
                    try {
                        if (!TextUtils.isEmpty(response.body())){
                            Log.i(TAG, "login onSuccess: "+response.body());
                            LoginInfo loginInfo = new Gson().fromJson(response.body(),LoginInfo.class);
                            if (loginInfo!=null){
                                int code = loginInfo.getCode();
                                if (code !=200){
                                    if (loginCallBack!=null){
                                        loginCallBack.onFailed(loginInfo);
                                    }
                                }else{
                                    if (loginCallBack!=null){
                                        loginCallBack.onSuccess(loginInfo);
                                    }
                                }
                            }else{
                                if (loginCallBack!=null){
                                    loginCallBack.onFailed(loginInfo);
                                }
                            }
                        }
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                @Override
                public void onError(Response<String> response) {
                    super.onError(response);
                    Log.i(TAG, "onError: ");
                    if (loginCallBack!=null){
                        loginCallBack.onError();
                    }
                }
            });
        }catch (JSONException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void upLoginStatus(Context context,int loginStatus){
        UserInfoUtil userInfoUtil = UserInfoUtil.getInstance(context);
        String userId = userInfoUtil.getUserId();
        try {
            JSONObject json = new JSONObject();
            json.put("userAccount",userId);
            json.put("loginStatus",loginStatus);
            OkGo.<String>post(BaseConfig.BASE_URL+BaseConfig.ADD_ACCESS_LOG).upString(json.toString()).tag(TAG).retryCount(2)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            Log.i(TAG, "onSuccess: ");
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            Log.i(TAG, "onError: ");
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

