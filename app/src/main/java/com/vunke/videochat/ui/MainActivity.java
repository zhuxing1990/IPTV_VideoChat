package com.vunke.videochat.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vunke.videochat.R;
import com.vunke.videochat.config.BaseConfig;
import com.vunke.videochat.linphone.LinphoneService;
import com.vunke.videochat.manage.CallManage;
import com.vunke.videochat.manage.RegisterManage;
import com.vunke.videochat.tools.FocusUtil;
import com.vunke.videochat.tools.LinphoneMiniUtils;
import com.vunke.videochat.tools.SPUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zhuxi on 2019/11/17.
 */

public class MainActivity extends AppCompatActivity  implements View.OnClickListener  {
    private static final String TAG = "MainActivity";

    private TextView main_login_status;
    private EditText main_edit_num;
    private Button main_relogin;
    private Button main_btn_num1,main_btn_num2,main_btn_num3,main_btn_num4,main_btn_num5,main_btn_num6,main_btn_num7,main_btn_num8,main_btn_num9,main_btn_num0,main_btn_del;
    private ImageView main_call_audio,main_call_video;

    private LinphoneService instance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinphoneMiniUtils.initLinphoneService(this);
        registerBroad();
        initView();
    }
    private MainActivityReceiver mReceiver;
    private void registerBroad() {
        mReceiver = new MainActivityReceiver();
        IntentFilter intentFilter = new IntentFilter(BaseConfig.INSTANCE.getRECEIVE_MAIN_ACTIVITY());
        registerReceiver(mReceiver,intentFilter);
    }

    private void initView() {
        main_login_status = findViewById(R.id.main_login_status);
        main_edit_num = findViewById(R.id.main_edit_num);
        main_relogin = findViewById(R.id.main_relogin);
        main_relogin.setOnClickListener(this);
        main_btn_num1 = findViewById(R.id.main_btn_num1);
        main_btn_num2 = findViewById(R.id.main_btn_num2);
        main_btn_num3 = findViewById(R.id.main_btn_num3);
        main_btn_num4 = findViewById(R.id.main_btn_num4);
        main_btn_num5 = findViewById(R.id.main_btn_num5);
        main_btn_num6 = findViewById(R.id.main_btn_num6);
        main_btn_num7 = findViewById(R.id.main_btn_num7);
        main_btn_num8 = findViewById(R.id.main_btn_num8);
        main_btn_num9 = findViewById(R.id.main_btn_num9);
        main_btn_num0 = findViewById(R.id.main_btn_num0);
        main_btn_del = findViewById(R.id.main_btn_del);
        main_call_audio = findViewById(R.id.main_call_audio);
        main_call_video = findViewById(R.id.main_call_video);
        main_btn_num1.setOnClickListener(this);
        main_btn_num2.setOnClickListener(this);
        main_btn_num3.setOnClickListener(this);
        main_btn_num4.setOnClickListener(this);
        main_btn_num5.setOnClickListener(this);
        main_btn_num6.setOnClickListener(this);
        main_btn_num7.setOnClickListener(this);
        main_btn_num8.setOnClickListener(this);
        main_btn_num9.setOnClickListener(this);
        main_btn_num0.setOnClickListener(this);
        main_btn_del.setOnClickListener(this);
        main_btn_del.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                main_edit_num.setText("");
                return false;
            }
        });
        main_call_audio.setOnClickListener(this);
        main_call_video.setOnClickListener(this);
        main_call_audio.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                FocusUtil.INSTANCE.setFocus(hasFocus,v,MainActivity.this);
            }
        });
        main_call_video.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                FocusUtil.INSTANCE.setFocus(hasFocus,v,MainActivity.this);
            }
        });
//        main_call_audio.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                startCall(false);
//                return false;
//            }
//        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_relogin:
                stopClick(main_relogin,5);
                initRegister();
                break;
            case R.id.main_btn_num1:
                main_edit_num.append("1");
                break;
            case R.id.main_btn_num2:
                main_edit_num.append("2");
                break;
            case R.id.main_btn_num3:
                main_edit_num.append("3");
                break;
            case R.id.main_btn_num4:
                main_edit_num.append("4");
                break;
            case R.id.main_btn_num5:
                main_edit_num.append("5");
                break;
            case R.id.main_btn_num6:
                main_edit_num.append("6");
                break;
            case R.id.main_btn_num7:
                main_edit_num.append("7");
                break;
            case R.id.main_btn_num8:
                main_edit_num.append("8");
                break;
            case R.id.main_btn_num9:
                main_edit_num.append("9");
                break;
            case R.id.main_btn_num0:
                main_edit_num.append("0");
                break;
            case R.id.main_btn_del:
//                int index=main_edit_num.getSelectionStart();   //获取Edittext光标所在位置
//                String str=main_edit_num.getText().toString();
//                if (!TextUtils.isEmpty(str)) {//判断输入框不为空，执行删除
//                    main_edit_num.getText().delete(index-1,index);
//                }
              String  PhoneNumber = main_edit_num.getText().toString().trim();
                DelNumber(PhoneNumber);
                break;
            case R.id.main_call_audio:
                startCall(false);
                break;
            case R.id.main_call_video:
                startCall(true);
                break;
            default:
                break;
        }
    }

    private void startCall(boolean isVideo) {
        String str=main_edit_num.getText().toString();
        if (TextUtils.isEmpty(str)){
            String lastCall = SPUtils.getString(getApplicationContext(), BaseConfig.INSTANCE.getLastCallNumber(),"");
            if (TextUtils.isEmpty(lastCall)){
                return;
            }else{
                Log.i(TAG, "startCall: lastCall:"+lastCall);
                main_edit_num.setText(lastCall);
            }
        }else{
            Log.i(TAG, "onClick: startCall:"+str);
            SPUtils.putString(getApplicationContext(), BaseConfig.INSTANCE.getLastCallNumber(),str);
            if (isVideo){
                CallManage.CallVideo(MainActivity.this,str);
            }else{
                CallManage.CallAudio(MainActivity.this,str);
            }
        }
    }

    private void initRegister() {
        if(!LinphoneService.isReady()){
            instance = LinphoneService.getInstance();
        }
        try {
//            String userName = "7318478702102";
//            String password = "11223355";

//            String userName = "73184310085";
//            String password = "y5427ln4";
//            String userName = "73257120251";
//            String password = "8HTG0BvT";
//            String userName = "7393710188";
//            String password = "u3AB63LR";
            //73152324252  V8lMAD8d
//            7354427957   rz4k6vYz
//            String userName = "7352227923";
//            String password = "UUAoe4OJ";
//            String userName = "7354427957";
//            String password = "rz4k6vYz";
//            String userName = "73188302837";
//            String password = "cT6Oel3m";
//            String userName = "73188107880";
//            String password = "QEh8UMcj";

//            String userName = "1015";
//            String password = "9999";
//            String userName = "1012";
//            String password = "1234";
//            String userName = "1006";
//            String password = "aegis2020";

            String userName = "73184065729";
            String password = "VG7yTT0B";
//            String userName = "73188107892";
//            String password = "CDRnO48N";

//            String userName = "73188300623";
//            String password = "r9KL73Cb";

            RegisterManage.Login(MainActivity.this,userName,password,BaseConfig.INSTANCE.getDomain());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void stopClick(final Button button,final long stopTime) {
        Log.i(TAG, "stopClick: ");
        button.setEnabled(false);
        Observable<Long> longObservable = Observable.interval(0, 1, TimeUnit.SECONDS)
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
                .observeOn(AndroidSchedulers.mainThread());

        DisposableObserver<Long> disposableObserver = new DisposableObserver<Long>() {

            @Override
            public void onNext(Long aLong) {
                Log.i(TAG, "onNext: "+aLong);
                if (aLong>0){
                    button.setText( aLong + "".trim());
                }else{
                    button.setText("登录");
                    onComplete();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "onError: ");
                button.setEnabled(true);
                this.dispose();
            }

            @Override
            public void onComplete() {
                Log.i(TAG, "onComplete: ");
                button.setEnabled(true);
                this.dispose();
            }
        };
        longObservable.subscribe(disposableObserver);
    }
    private boolean isLogin = false;

    public class MainActivityReceiver extends BroadcastReceiver {
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
                                isLogin = true;
                                main_login_status.setText("在线");
                                main_relogin.setVisibility(View.GONE);
                            }else {
                                isLogin = false;
                                main_login_status.setText("离线");
                                main_relogin.setVisibility(View.VISIBLE);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.i(TAG, "onKeyDown: keycode:"+keyCode);
        if (keyCode ==KeyEvent.KEYCODE_BACK){
            exit();
            return false;
        }else if (keyCode == KeyEvent.KEYCODE_1){
            main_edit_num.append("1");
            return false;
        }else if (keyCode == KeyEvent.KEYCODE_2){
            main_edit_num.append("2");
            return false;
        }else if (keyCode == KeyEvent.KEYCODE_3){
            main_edit_num.append("3");
            return false;
        }else if (keyCode == KeyEvent.KEYCODE_4){
            main_edit_num.append("4");
            return false;
        }else if (keyCode == KeyEvent.KEYCODE_5){
            main_edit_num.append("5");
            return false;
        }else if (keyCode == KeyEvent.KEYCODE_6){
            main_edit_num.append("6");
            return false;
        }else if (keyCode == KeyEvent.KEYCODE_7){
            main_edit_num.append("7");
            return false;
        }else if (keyCode == KeyEvent.KEYCODE_8){
            main_edit_num.append("8");
            return false;
        }else if (keyCode == KeyEvent.KEYCODE_9){
            main_edit_num.append("9");
            return false;
        }else if (keyCode == KeyEvent.KEYCODE_0){
            main_edit_num.append("0");
            return false;
        }else if(keyCode == KeyEvent.KEYCODE_DEL || keyCode == 1185){
            String  PhoneNumber = main_edit_num.getText().toString().trim();
            DelNumber(PhoneNumber);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void DelNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {

        } else{
            StringBuffer buffer = new StringBuffer(phoneNumber);
            buffer.deleteCharAt(buffer.length() - 1);
            main_edit_num.setText(buffer.toString().trim());
        }
    }

    private long exitTime = 0;
    public void exit() {
        if (System.currentTimeMillis() - exitTime > 2000L) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
            return;
        }
        finish();
    }


}
