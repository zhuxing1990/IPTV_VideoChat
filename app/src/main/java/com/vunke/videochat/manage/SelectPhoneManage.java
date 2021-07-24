package com.vunke.videochat.manage;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.vunke.videochat.base.BaseConfig;
import com.vunke.videochat.dialog.MyDialog;
import com.vunke.videochat.dialog.OpenProgessDialog;
import com.vunke.videochat.dialog.SuccessfulOpenDialog;
import com.vunke.videochat.login.LoginCallBack;
import com.vunke.videochat.login.LoginManage;
import com.vunke.videochat.login.UserInfoUtil;
import com.vunke.videochat.model.LoginInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zhuxi on 2020/9/15.
 */

public class SelectPhoneManage {
    private static final String TAG = "SelectPhoneManage";
//    private static ProgressDialog progressDialog;
    private static OpenProgessDialog progessDialog;
    public static void fixedLineNumber(final Activity context,final String PhoneNumber){
//        progressDialog =ProgressDialog.show(context,null,"正在开通中，请稍候……");
        progessDialog = new OpenProgessDialog(context);
        progessDialog.setMessage("正在开通中，请稍候……").show();
        final UserInfoUtil userInfoUtil = UserInfoUtil.getInstance(context);
        try {
            JSONObject json = new JSONObject();
            json.put("userId",userInfoUtil.getUserId())
                .put("fixedLineNumber",PhoneNumber)
                .put("userToken",userInfoUtil.getUserToken());
            OkGo.<String>post(BaseConfig.BASE_URL+BaseConfig.FIXE_LINE_BINDING)
                    .tag(TAG).upString(json.toString())
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            Log.i(TAG, "onSuccess: "+response.body());
                            try {
                                if (!TextUtils.isEmpty(response.body())){
                                    JSONObject js = new JSONObject(response.body());
                                    if (js.has("code")){
                                        int code = js.getInt("code");
                                        if (200==code){
                                            OpenManage.saveOpenInfo(context,userInfoUtil.getUserId(),PhoneNumber);
                                            initTimeOut(context);
                                        }else{
//                                            progressDialog.dismiss();
                                            progessDialog.cancel();
                                            if (js.has("message")){
                                                String message = js.getString("message");
                                                if (!TextUtils.isEmpty(message)){
                                                    initDialog(context,message);
                                                }
                                            }
                                        }
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            Log.i(TAG, "onError: ");
//                            progressDialog.dismiss();
                            progessDialog.cancel();
                            initDialog(context,"网络异常，请稍候再试!");
                        }
                    });
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    public static DisposableObserver<Long> disposableObserver;
    public static void initTimeOut(final Activity context) {
        if (disposableObserver!=null){
            if (!disposableObserver.isDisposed()){
                disposableObserver.dispose();
            }
        }
        final int stopTime = 60;
        disposableObserver = new DisposableObserver<Long>() {
            @Override
            public void onNext(Long aLong) {
                if (aLong>0){
//                    if (progressDialog!=null&& progressDialog.isShowing()){
//                        progressDialog.setMessage("正在开通中，请稍等:"+aLong+"秒");
//                    }else{
//                        progressDialog =ProgressDialog.show(context,null,"正在开通中，请稍等:"+aLong+"秒");
//                    }
                    if (progessDialog!=null&&progessDialog.isShowing()){
                        progessDialog.setMessage("正在开通中，请稍等:"+aLong+"秒");
                    }else{
                        progessDialog = new OpenProgessDialog(context);
                        progessDialog.setMessage("正在开通中，请稍等:"+aLong+"秒");
                        progessDialog.show();
                    }
                }else{
                    UserInfoUtil userInfoUtil = UserInfoUtil.getInstance(context);
                    LoginManage loginManage = new LoginManage();
                    loginManage.startLogin(userInfoUtil.getUserId(), new LoginCallBack() {
                        @Override
                        public void onSuccess(LoginInfo loginInfo) {
//                            progressDialog.dismiss();
                            progessDialog.cancel();
                            final SuccessfulOpenDialog successfulOpenDialog = new SuccessfulOpenDialog(context);
                            successfulOpenDialog.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(BaseConfig.RECEVIE_OPEN_OVER);
                                    context.sendBroadcast(intent);
                                    successfulOpenDialog.cancel();
                                    context.finish();
                                }
                            });
                            successfulOpenDialog.show();
                        }

                        @Override
                        public void onFailed(LoginInfo loginInfo) {
//                            progressDialog.dismiss();
                            progessDialog.cancel();
                            initDialog(context, "开通失败，请拨打4009900901,人工开通.");
                        }

                        @Override
                        public void onError() {
//                            progressDialog.dismiss();
                            progessDialog.cancel();
                            initDialog(context, "开通失败，请稍后再试.");
                        }
                    });
                    onComplete();
                }
            }

            @Override
            public void onError(Throwable e) {
                this.dispose();
            }

            @Override
            public void onComplete() {
                this.dispose();
            }
        };
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .filter(new Predicate<Long>() {
                    @Override
                    public boolean test(Long t) throws Exception {
                        return t <= stopTime;
                    }
                }).map(new Function<Long, Long>() {
            @Override
            public Long apply(Long t) throws Exception {
                return -(t-stopTime);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(disposableObserver);
//        Observable.interval(60, TimeUnit.SECONDS)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(disposableObserver);
    }

    private static MyDialog dialog;
    private static void initDialog(final Activity context, String message) {
        CancelDialog();
        dialog = new MyDialog(context);
        dialog.setMessage(message);
        dialog.setCommitOnClickLintener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                context.finish();
            }
        });
        dialog.show();
    }
    private static void CancelDialog(){
        if (dialog!=null){
            dialog.cancel();
        }
    }
}
