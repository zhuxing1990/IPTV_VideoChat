package com.vunke.videochat.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.vunke.videochat.R;
import com.vunke.videochat.adaper.ContactsAdaper;
import com.vunke.videochat.adaper.RecyclerViewSpacesItemDecoration;
import com.vunke.videochat.base.BaseConfig;
import com.vunke.videochat.callback.ContactsCallBack;
import com.vunke.videochat.dao.ContactsDao;
import com.vunke.videochat.db.Contacts;
import com.vunke.videochat.login.UserInfoUtil;
import com.vunke.videochat.model.ContactsList;
import com.vunke.videochat.receiver.ContactsReceiver;
import com.vunke.videochat.ui.AddContactActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by zhuxi on 2020/2/27.
 */

public class ContactsFragment extends Fragment implements View.OnClickListener,View.OnKeyListener{
    private static final String TAG = "ContactsFragment";
    @Override
    public LayoutInflater onGetLayoutInflater(Bundle savedInstanceState) {
        return super.onGetLayoutInflater(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    private ContactsAdaper contactsAdapter;
    private RelativeLayout contacts_rl1;
    private List<Contacts> contactsList;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x3840:
//                    contactsList = ContactsDao.Companion.getInstance(getActivity()).queryAll();//------------------数据库更新
                    contactsList = ContactsDao.Companion.getInstance(getActivity()).queryAll(getActivity());
                    if (contactsList!=null&& contactsList.size()!=0){
                        if (contactsAdapter !=null){
                            contactsAdapter.update(contactsList);
                            showRecycler();
//                            contacts_rl1.requestFocus();
                        }else{
                            contactsAdapter = new ContactsAdaper(getActivity(),contactsList);
                            contacts_recycler.setAdapter(contactsAdapter);
                            showRecycler();
//                            contacts_rl1.requestFocus();
                        }
                    }else{
                        hindReccycler();
//                        contacts_rl1.requestFocus();
                    }
                    contacts_rl1.requestFocus();
                    break;
                case 0x3841:
                    updateContentsList();
                    break;
                default:
                    break;
            }
        }
    };

    private ContactsReceiver contactsReceiver;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts,null);
        initView(view);
        initData();
        updateContentsList();
        registerReceiver();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
    }

    private void registerReceiver() {
        contactsReceiver = new ContactsReceiver(new ContactsCallBack() {
            @Override
            public void onUpdate() {
                Log.i(TAG, "Contacts onUpdate: ");
                handler.sendEmptyMessage(0x3840);
                updateContentsList();
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ContactsReceiver.CONTACTS_ADD_ACTION);
        getActivity().registerReceiver(contactsReceiver,intentFilter);
    }

    private void updateContentsList() {
        try {
            UserInfoUtil userInfoUtil = UserInfoUtil.getInstance(getActivity());
            String userId = userInfoUtil.getUserId();
            JSONObject json = new JSONObject();
            json.put("userId",userId);
            OkGo.<String>post(BaseConfig.BASE_URL+ BaseConfig.GET_USER_CONTACTS_LIST)
                    .tag(this)
                    .upString(json.toString())
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            Log.i(TAG, "onSuccess: s:"+response.body());
                            try {
                                ContactsList contactsList = new Gson().fromJson(response.body(), ContactsList.class);
                                int code = contactsList.getCode();
                                if (code==200){
                                    List<ContactsList.UserData.ContactData> contactDataList = contactsList.getObj().getData();
                                    if (contactDataList!=null&&contactDataList.size()!=0){
                                        for (ContactsList.UserData.ContactData contactData:contactDataList) {
//                                        ContactsDao.Companion.getInstance(getActivity()).updateName(contactData);//------------------数据库更新
                                            ContactsDao.Companion.getInstance(getActivity()).saveContacts(getActivity(),contactData);
                                            handler.sendEmptyMessage(0x3840);
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
                            Log.e(TAG, "onError: ",response.getException() );
                            handler.sendEmptyMessageDelayed(0x3841,10000L);
                        }

                        @Override
                        public void onFinish() {
                            super.onFinish();
                            Log.i(TAG, "onFinish: ");
                        }
                    });
        }catch (JSONException e){
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    private RecyclerView contacts_recycler;
    private void initView(View view) {
        contacts_rl1 = view.findViewById(R.id.contacts_rl1);
        contacts_rl1.setOnClickListener(this);
        contacts_recycler = view.findViewById(R.id.contacts_recycler);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),2);
        contacts_recycler.setLayoutManager(gridLayoutManager);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.TOP_DECORATION,20);//top间距
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.BOTTOM_DECORATION,20);//底部间距
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.LEFT_DECORATION,30);//左间距
        stringIntegerHashMap.put(RecyclerViewSpacesItemDecoration.RIGHT_DECORATION,30);//右间距
        contacts_recycler.addItemDecoration(new RecyclerViewSpacesItemDecoration(stringIntegerHashMap));

    }
    private void initData() {
//        contactsList = ContactsDao.Companion.getInstance(getActivity()).queryAll();//------------------数据库更新
        contactsList = ContactsDao.Companion.getInstance(getActivity()).queryAll(getActivity());
        if (contactsList!=null&& contactsList.size()!=0){
//            Log.i(TAG, "initData: contactsList:"+contactsList.toString());
            contactsAdapter = new ContactsAdaper(getActivity(),contactsList);
            contacts_recycler.setAdapter(contactsAdapter);
            showRecycler();
        }
    }

    private void showRecycler() {
        if (contacts_recycler.getVisibility() == View.INVISIBLE){
            contacts_recycler.setVisibility(View.VISIBLE);
        }
    }
    private void hindReccycler(){
        if (contacts_recycler.getVisibility() == View.VISIBLE){
            contacts_recycler.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.contacts_rl1:
                AddContact();
                break;
        }
    }
//    private AddContactsDialog dialog;
    private void AddContact() {
        Intent intent = new Intent(getActivity(), AddContactActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
//        if (dialog!=null&&dialog.isShowing()){
//            dialog.cancel();
//        }
//        dialog = new AddContactsDialog(getActivity()).builder();
//        dialog.setPositiveButton("", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String name = dialog.getNameEdit().toString();
//                String phone = dialog.getPhoenEdit().toString();
//                if (TextUtils.isEmpty(name)){
//                    Toast.makeText(getActivity(),"请输入姓名",Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (TextUtils.isEmpty(phone)){
//                    Toast.makeText(getActivity(),"请输入号码",Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (!Utils.isNumeric(phone)){
//                    Toast.makeText(getActivity(),"号码请输入数字",Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                Contacts contacts = new Contacts();
//                contacts.setUser_name(name);
//                contacts.setPhone(phone);
//                contacts.set_id(System.currentTimeMillis());
//                Long i = ContactsDao.Companion.getInstance(getActivity()).saveData(contacts);
//                if (i!=-1L){
//                    Toast.makeText(getActivity(),"保存成功",Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent();
//                    intent.setAction(CallRecordReceiver.CALL_RECORD_ACTION);
//                    getActivity().sendBroadcast(intent);
//                    dialog.cancel();
//                    handler.sendEmptyMessage(0x3840);
//                }else {
//                    Toast.makeText(getActivity(),"保存失败",Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });
//        dialog.setNeutralButton("", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.cancel();
//            }
//        });
//        dialog.setCancelable(false);
//        dialog.show();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (v.getId()){
            case R.id.contacts_rl1:
                if (keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
                    return true;
                }
                break;

        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        try {
            if (null!=contactsReceiver){
                getActivity().unregisterReceiver(contactsReceiver);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}