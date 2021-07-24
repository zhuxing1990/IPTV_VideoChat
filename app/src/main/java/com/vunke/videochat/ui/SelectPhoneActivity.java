package com.vunke.videochat.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.vunke.videochat.R;
import com.vunke.videochat.adaper.RecyclerViewSpacesItemDecoration;
import com.vunke.videochat.adaper.SelectPhoneAdapter;
import com.vunke.videochat.base.BaseConfig;
import com.vunke.videochat.dialog.MyDialog;
import com.vunke.videochat.login.UserInfoUtil;
import com.vunke.videochat.model.AccountBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by zhuxi on 2020/9/11.
 */

public class SelectPhoneActivity extends AppCompatActivity {
    private static final String TAG = "SelectPhoneActivity";
    private UserInfoUtil userInfoUtil;
    private RecyclerView select_num_recvcler;
    private SelectPhoneAdapter adapter;
    private List<String> phoneList;
    private Button selectphone_changebatch;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectphone);
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        View rootView = findViewById(android.R.id.content);
//        BackgroundManage.setBackground(this, rootView);
    }

    private void initData() {
        userInfoUtil = UserInfoUtil.getInstance(this);
        try {
            JSONObject json = new JSONObject();
            json.put("userId",userInfoUtil.getUserId())
                .put("userToken",userInfoUtil.getUserToken());
            OkGo.<String>post(BaseConfig.BASE_URL+BaseConfig.GET_OPTIONAL_ACCOUNT)
                    .tag(this).upString(json.toString())
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            Log.i(TAG, "onSuccess: "+response);
                            if (!TextUtils.isEmpty(response.body())){
                                try {
                                    AccountBean accountBean = new Gson().fromJson(response.body(), AccountBean.class);
                                    if (accountBean!=null){
                                        if (200==accountBean.getCode()){
                                            initReceiverData(accountBean);
                                        }else{
                                            if (!TextUtils.isEmpty(accountBean.getMessage())){
                                                String message = accountBean.getMessage();
                                                initDialog(message);
                                            }
                                        }
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            Log.i(TAG, "onError: ");
                            initDialog("网络异常，请稍候再试!");
                        }
                    });
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    private MyDialog dialog;
    private void initDialog(String message) {
        CancelDialog();
        dialog = new MyDialog(SelectPhoneActivity.this);
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
    private int page = 0;
    private int maxpage= 0;
    private void initReceiverData(AccountBean accountBean) {
        phoneList = accountBean.getOptionalFixedLine();
        if (phoneList!=null&&phoneList.size()!=0){
            int a =phoneList.size() % 9;
            int b = phoneList.size()/9;
            if (a==0){
                maxpage = b;
            }else{
                maxpage = b+1;
            }
            Log.i(TAG, "initReceiverData: get max page:"+maxpage);
            if (phoneList.size()<=9){
                adapter = new SelectPhoneAdapter(SelectPhoneActivity.this,phoneList);
                select_num_recvcler.setAdapter(adapter);
                selectphone_changebatch.setVisibility(View.VISIBLE);
            }else{
                setPageList(phoneList,0);
            }
        }
    }
    private void setPageList(List<String> list,int page){
        try {
            Log.i(TAG, "setPageList: page:"+page);
            List<String> pageList = null;
            int i = list.size() - page * 10;
            Log.i(TAG, "setPageList: i:"+i);
            if (i>0){
                int a = page*9;
                int b = (page+1)*9;
                if (a>list.size()){
                    Log.i(TAG, "setPageList: a:"+a);
                    pageList = list.subList((page-1)*9,list.size());
                }else if (b>list.size()){
                    Log.i(TAG, "setPageList: b:"+b);
                    pageList = list.subList(page*9,list.size());
                }else{
                    pageList = list.subList(page*9,(page+1)*9);
                }
            }else if (i<=0){
                if (page*9>list.size()){
                    pageList = list.subList((page-1)*9,list.size());
                }else{
                    pageList = list.subList(page*9,list.size());
                }
            }
            adapter = new SelectPhoneAdapter(SelectPhoneActivity.this,pageList);
            select_num_recvcler.setAdapter(adapter);
            if (selectphone_changebatch.getVisibility()!=View.VISIBLE){
                selectphone_changebatch.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void initView() {
        select_num_recvcler = findViewById(R.id.selectphone_recycler);
        selectphone_changebatch = findViewById(R.id.selectphone_changebatch);
        selectphone_changebatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneList!=null&&phoneList.size()!=0){
                    if (page+1<maxpage){
                        page++;
                        setPageList(phoneList,page);
                    }else{
                        page = 0;
                        setPageList(phoneList,page);
                    }
                }
            }
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3);
        select_num_recvcler.setLayoutManager(gridLayoutManager);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.TOP_DECORATION,20);//top间距
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.BOTTOM_DECORATION,20);//底部间距
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.LEFT_DECORATION,30);//左间距
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.RIGHT_DECORATION,30);//右间距
        select_num_recvcler.addItemDecoration(new RecyclerViewSpacesItemDecoration(stringIntegerHashMap));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CancelDialog();
    }
}
