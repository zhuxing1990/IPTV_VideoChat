package com.vunke.videochat.manage;

import android.util.Log;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.vunke.videochat.base.BaseConfig;
import com.vunke.videochat.callback.TalkCallBack;
import com.vunke.videochat.model.TalkBean;

public class TalkManage {
    private static final String TAG = "TalkManage";
    public static void addConversationLog(TalkBean talkBean, final TalkCallBack talkCallBack){
        try {
            String json = new Gson().toJson(talkBean, TalkBean.class);
            Log.i(TAG, "addConversationLog: json:"+json);
            OkGo.<String>post(BaseConfig.BASE_URL+BaseConfig.ADD_CONVERSTION_LOG)
                    .tag(TAG).upJson(json)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            Log.i(TAG, "onSuccess: response:"+response.body());
                            if (talkCallBack!=null){
                                talkCallBack.onSuccess();
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            if (talkCallBack!=null){
                                talkCallBack.OnFailed();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
