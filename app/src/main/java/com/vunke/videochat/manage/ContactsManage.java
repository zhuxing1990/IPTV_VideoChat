package com.vunke.videochat.manage;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.vunke.videochat.base.BaseConfig;
import com.vunke.videochat.db.Contacts;
import com.vunke.videochat.login.UserInfoUtil;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by zhuxi on 2020/12/8.
 */

public class ContactsManage {
    private static final String TAG = "ContactsManage";
    public static  void UploadContacts(Context context, Contacts contacts) {
        try {
            UserInfoUtil userInfoUtil = UserInfoUtil.getInstance(context);
            String userId = userInfoUtil.getUserId();
            JSONObject json = new JSONObject();
            json.put("userId",userId);
            JSONArray jsonArray = new JSONArray();
            JSONObject contactJson = new JSONObject();
            contactJson.put("friendsName",contacts.getUser_name());
            contactJson.put("friendsNumber",contacts.getPhone());
            contactJson.put("friendsId",contacts.get_id());
            jsonArray.put(contactJson);
            json.put("data",jsonArray);

            OkGo.<String>post(BaseConfig.BASE_URL+BaseConfig.ADD_USER_CONTACTS).tag(TAG)
                    .upString(json.toString())
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            String body = response.body();
                            if (!TextUtils.isEmpty(body)){
                                Log.i(TAG, "onSuccess: upload request success:"+body);
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            Log.i(TAG, "onError: Upload connects failed ");
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void DelContacts(Activity context, Contacts contacts) {
        try {
            UserInfoUtil userInfoUtil = UserInfoUtil.getInstance(context);
            String userId = userInfoUtil.getUserId();
            JSONObject json = new JSONObject();
            json.put("userId",userId);
            json.put("id",contacts.get_id());
            json.put("friendsName",contacts.getUser_name());
            json.put("friendsNumber",contacts.getPhone());
            OkGo.<String>post(BaseConfig.BASE_URL+BaseConfig.DEL_USER_CONTACTS).tag(TAG)
                    .upString(json.toString())
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            String body = response.body();
                            if (!TextUtils.isEmpty(body)){
                                Log.i(TAG, "onSuccess: del conects request success:"+body);
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            Log.i(TAG, "onError: del conects failed ");
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
