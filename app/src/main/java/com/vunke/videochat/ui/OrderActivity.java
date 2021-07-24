package com.vunke.videochat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.vunke.videochat.R;
import com.vunke.videochat.base.BaseConfig;
import com.vunke.videochat.dialog.MyDialog;
import com.vunke.videochat.login.UserInfoUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class OrderActivity extends AppCompatActivity {
    private static final String TAG = "OrderActivity";
    RelativeLayout order_commit,order_back;;
    TextView order_username;
    TextView order_usercard;
    EditText order_edit;
    private String endNumber = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        initView();
        initListener();
        initData();
    }
    private UserInfoUtil userInfoUtil;
    private MyDialog dialog;
    private void initData() {
        try {
            userInfoUtil = UserInfoUtil.getInstance(this);
            JSONObject json = new JSONObject();
//            userInfoUtil.setUserId("CS009624668@VOD");
//            userInfoUtil.setUserId("IY037652913@VOD");
//            userInfoUtil.setUserToken("08162013615809948600820601102125");
            json.put("userId",userInfoUtil.getUserId())
                    .put("userToken",userInfoUtil.getUserToken());
            OkGo.<String>post(BaseConfig.BASE_URL+BaseConfig.QUY_BY_CUSTOMER_DATA).tag(this)
                    .upString(json.toString())
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            try {
                                if (!TextUtils.isEmpty(response.body())){
                                    JSONObject js = new JSONObject(response.body());
                                    if (js.has("code")){
                                        int code = js.getInt("code");
                                        if (code==200){
                                            String certNum = js.getString("certNum");
                                            String custName = js.getString("custName");
                                            order_username.setText(custName);
                                           if (!TextUtils.isEmpty(certNum)){
                                               try {
                                                   if (certNum.length()==18){
                                                       String substring = certNum.substring(certNum.length() - 8,certNum.length() - 4);
                                                       String str = certNum.replace(substring,"****");
                                                       endNumber = certNum.substring(certNum.length() - 4);
                                                       String number = str.substring(0,certNum.length()-4);
                                                       order_usercard.setText(number);
                                                   }
                                               }catch (Exception e){
                                                   e.printStackTrace();
                                                   order_usercard.setText(certNum);
                                               }
                                           }
                                        }else{
                                            if (js.has("message")){
                                                String message = js.getString("message");
                                                if (!TextUtils.isEmpty(message)){
//                                                    Toast.makeText(OrderActivity.this,message,Toast.LENGTH_SHORT).show();
                                                    initDialog(message);
                                                }
                                            }
                                        }
                                    }
                                }
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            Log.i(TAG, "onError: ");
                            initDialog("网络异常，请稍候再试!");
                        }
                    });

        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private void initDialog(String message) {
        CancelDialog();
        dialog = new MyDialog(OrderActivity.this);
        dialog.setMessage(message);
        dialog.setCommitOnClickLintener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                finish();
            }
        });
        dialog.show();
    }
    private void CancelDialog(){
        if (dialog!=null){
            dialog.cancel();
        }
    }
    private void initListener() {
        order_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(endNumber)){
                    Toast.makeText(OrderActivity.this,"获取用户信息失败,请稍候!",Toast.LENGTH_SHORT).show();
                    initData();
                }else{
                    String num4 = order_edit.getText().toString();
                    if (TextUtils.isEmpty(num4)){
                        Toast.makeText(OrderActivity.this,"请将身份证后4位补充完成",Toast.LENGTH_SHORT).show();
                    }else{
                        if (num4.equals(endNumber)){
                            Intent intent = new Intent(OrderActivity.this, SelectPhoneActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(OrderActivity.this,"验证失败,请重新输入!",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        order_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        order_username = findViewById(R.id.order_username);
        order_usercard = findViewById(R.id.order_usercard);
        order_commit = findViewById(R.id.order_commit);
        order_back = findViewById(R.id.order_back);
        order_edit = findViewById(R.id.order_edit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CancelDialog();
    }
}
