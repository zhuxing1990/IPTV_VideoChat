package com.vunke.videochat.dialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vunke.videochat.R;
import com.vunke.videochat.config.CallInfo;
import com.vunke.videochat.dao.ContactsDao;
import com.vunke.videochat.db.CallRecord;
import com.vunke.videochat.db.CallRecordTable;
import com.vunke.videochat.db.Contacts;
import com.vunke.videochat.linphone.LinphoneService;
import com.vunke.videochat.tools.CallRecordUtil;
import com.vunke.videochat.ui.AudioActivity;
import com.vunke.videochat.ui.VideoActivity;

import org.linphone.core.Address;
import org.linphone.core.Call;

import java.util.List;

/**
 * Created by zhuxi on 2020/3/3.
 */

public class CallInDialog implements View.OnKeyListener {
    private static final String TAG = "CallInDialog";
    private Context context;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private View view;
    private RelativeLayout dialog_callin_view;
//    private LinphoneMiniManager instance;
    private LinphoneService instance;
    private String message;
    public CallInDialog(Context context,LinphoneService instance, String message){
        this.context = context;
        this.instance = instance;
        this.message = message;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        layoutParams.format = PixelFormat.RGBA_8888;
        // 设置浮动窗口不可触摸 聚焦
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        //设置窗口权重
        layoutParams.gravity = Gravity.RIGHT;
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.width =  WindowManager.LayoutParams.MATCH_PARENT;;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
    }

    public CallInDialog builder(){
        view = LayoutInflater.from(context).inflate(R.layout.dialog_callin,null);
        dismiss();
        dialog_callin_view = view.findViewById(R.id.dialog_callin_view);
        ImageView dialog_call_answwer = view.findViewById(R.id.dialog_call_answwer);
        dialog_call_answwer.requestFocus();
        TextView dialog_call_phone = view.findViewById(R.id.dialog_call_phone);
        dialog_call_answwer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int numberOfCameras = Camera.getNumberOfCameras();
                    Log.i(TAG, "get camera number:" + numberOfCameras);
                    if (numberOfCameras == 0) {
//                       new NotCameraDialog(context).Builder().show();
//                        NotCameraDialog.getInstance(context).show();
                        Toast.makeText(context,"摄像头未连接",Toast.LENGTH_SHORT).show();
                    }else{
                        instance.lilin_jie();
                        if (instance.lilin_getVideoEnabled()){
                            Intent intent = new Intent(context, VideoActivity.class);
                            if (!TextUtils.isEmpty(message)){
                                intent.putExtra("message",message);
                            }
                            intent.putExtra(CallRecordTable.INSTANCE.getCALL_STATUS(), CallInfo.INSTANCE.getCALL_IN());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                            dismiss();
                        }else{
                            Intent intent = new Intent(context, AudioActivity.class);
                            if (!TextUtils.isEmpty(message)){
                                intent.putExtra("message",message);
                            }
                            intent.putExtra(CallRecordTable.INSTANCE.getCALL_STATUS(), CallInfo.INSTANCE.getCALL_IN());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                            dismiss();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        ImageView dialog_call_hang_up = view.findViewById(R.id.dialog_call_hang_up);
        dialog_call_hang_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instance.hangUp();
                dismiss();
                CallRecord callRecord = new CallRecord();
                try {
                    if (!TextUtils.isEmpty(message)){
                        if (message.contains("<tel")){
                            String[] data = message.split("<tel:");
//                for (i in data.indices) {
//                    println(data[i])
//                }
                            String number = data[1].substring(0, data[1].indexOf(";"));
                            callRecord.call_phone=number;
                            callRecord.call_name=number;
                            List<Contacts> contactsList = ContactsDao.Companion.getInstance(context).queryPhone(number);
                            if (contactsList!=null&&contactsList.size()!=0){
                                callRecord.call_name = contactsList.get(0).user_name;
                            }
                            callRecord.call_time = System.currentTimeMillis()+"".trim();
                            callRecord.call_status = CallInfo.INSTANCE.getCALL_MISSED();
                            CallRecordUtil.updateCallRecord(context,callRecord);
                        }
                    }else{
//                        LinphoneAddress remoteAddress = instance.getmLinphoneCore().getRemoteAddress();
//                        Log.i(TAG, "initData: remoteAddress:"+remoteAddress);
//                        String userName = remoteAddress.getUserName();
//                        String getDisplayName = remoteAddress.getDisplayName();
//                        callRecord.call_phone = userName;
//                        callRecord.call_name = getDisplayName;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
//        dialog_call_answwer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                FocusUtil.INSTANCE.setFocus(hasFocus, v, context);
//            }
//        });
//        dialog_call_hang_up.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                FocusUtil.INSTANCE.setFocus(hasFocus, v, context);
//            }
//        });
        initNumber(dialog_call_phone);
        windowManager.addView(view,layoutParams);
        isShow = true;
        return this;
    }

    private void initNumber(TextView textView) {
        try {
            if (!TextUtils.isEmpty(message)){
                if (message.contains("<tel")){
                    String[] data = message.split("<tel:");
//                for (i in data.indices) {
//                    println(data[i])
//                }
                    String number = data[1].substring(0, data[1].indexOf(";"));
                    textView.setText(number);
                }else{
                    Call currentCall = instance.getCore().getCurrentCall();
                    if (currentCall!=null){
                        Log.i(TAG, "initData: 0");
                        initDisplayName(currentCall,textView);
                    }else{
                        for (Call call : instance.getCore().getCalls()) {
                            if (call != null && call.getConference() != null) {
                                if (instance.getCore().isInConference()) {
                                    Log.i(TAG, "initData: 1");
                                    initDisplayName(currentCall,textView);
                                }
                            } else if (call != null && call != currentCall) {
                                Call.State state = call.getState();
                                if (state == Call.State.Paused
                                        || state == Call.State.PausedByRemote
                                        || state == Call.State.Pausing) {
                                    Log.i(TAG, "initData: 2");
                                    initDisplayName(currentCall,textView);
                                }
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void initDisplayName(Call currentCall,TextView textView) {
        Address remoteAddress = currentCall.getRemoteAddress();
        if (remoteAddress!=null){
            String displayName = remoteAddress.getDisplayName();
            if (TextUtils.isEmpty(displayName)) {
                displayName = remoteAddress.getUsername();
                Log.i(TAG, "initDisplayName: getUsername:"+displayName);
            }
            if (TextUtils.isEmpty(displayName)) {
                Log.i(TAG, "initDisplayName: asStringUriOnly:"+displayName);
                displayName = remoteAddress.asStringUriOnly();
            }
            textView.setText(displayName);
            try {
                if (displayName.contains("tel:")){
                    String[] data = displayName.split("tel:");
                    String number = data[1].substring(0, data[1].indexOf(";"));
                    List<Contacts> contactsList = ContactsDao.Companion.getInstance(context).queryPhone(number);
                    if (contactsList!=null&&contactsList.size()!=0){
                        textView.setText(contactsList.get(0).getUser_name());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            textView.setText("Anonymous");
        }
    }
    private boolean isShow;
    public boolean isShow(){
        return isShow;
    }
    public CallInDialog dismiss() {
        Log.i(TAG, "CallInDialog dismiss: ");
        isShow= false;
        try {
            if (dialog_callin_view!=null){
                if (dialog_callin_view.getVisibility()==View.VISIBLE){
                    windowManager.removeView(dialog_callin_view);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            instance.hangUp();
            dismiss();
            return true;
        }
        return false;
    }
}
