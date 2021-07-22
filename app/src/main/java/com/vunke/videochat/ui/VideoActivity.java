package com.vunke.videochat.ui;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vunke.videochat.R;
import com.vunke.videochat.base.BaseConfig;
import com.vunke.videochat.callback.CallOverCallBack;
import com.vunke.videochat.callback.TalkCallBack;
import com.vunke.videochat.config.CallInfo;
import com.vunke.videochat.dao.ContactsDao;
import com.vunke.videochat.db.CallRecord;
import com.vunke.videochat.db.CallRecordTable;
import com.vunke.videochat.db.Contacts;
import com.vunke.videochat.dialog.CallTimeDialog;
import com.vunke.videochat.dialog.NotCameraDialog;
import com.vunke.videochat.linphone.LinphoneService;
import com.vunke.videochat.login.UserInfoUtil;
import com.vunke.videochat.manage.TalkManage;
import com.vunke.videochat.model.TalkBean;
import com.vunke.videochat.tools.AudioUtil;
import com.vunke.videochat.tools.CallRecordUtil;
import com.vunke.videochat.tools.CameraUtil;
import com.vunke.videochat.tools.TimeUtil;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.VideoDefinition;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class VideoActivity extends AppCompatActivity implements View.OnClickListener{
	private static final String TAG = "VideoActivity";
	private VideoActivityReceiver mReceiver;

	private TextureView mRenderingView,mPreviewView;
	private ImageView video_hang_up,video_mute,video_qiev;
	//	private Button video_speaker;
	private LinphoneService instance;
	private TextView video_mute_text;
	private RelativeLayout call_video_r3,call_video_r4;
	private CallRecord callRecord;
	private NotCameraDialog dialog;
	private ImageView video_switch;
	private RelativeLayout callvideo_rl1,callvideo_rl2;
	private TextView video_callTime;
	private long firstCallTime=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		instance = LinphoneService.getInstance();
		init();
		initData();
		initCamera();
//		firstCallTime = System.currentTimeMillis();
//		startCallTimeTask();
		initCallTime();
		initAlphaAnimation1();
		initAlphaAnimation2();
		initTimerOut();
		registerBroadCast();
		switchTime = System.currentTimeMillis();
	}
	private void init() {
		mRenderingView = findViewById(R.id.id_video_rendering);
		mPreviewView = findViewById(R.id.id_video_preview);
		Core core = LinphoneService.getCore();
		// We need to tell the core in which to display what
		core.setNativeVideoWindowId(mRenderingView);
		core.setNativePreviewWindowId(mPreviewView);
		video_hang_up = findViewById(R.id.video_hang_up);
		video_mute= findViewById(R.id.video_mute);
		video_callTime= findViewById(R.id.video_callTime);
//		video_speaker = findViewById(R.id.video_speaker);
		video_qiev =findViewById(R.id.video_qiev);
		video_hang_up.setOnClickListener(this);
		video_mute.setOnClickListener(this);
//		video_speaker.setOnClickListener(this);
		video_qiev.setOnClickListener(this);

		callvideo_rl1 = findViewById(R.id.callvideo_rl1);
		callvideo_rl2 = findViewById(R.id.callvideo_rl2);

		video_hang_up.requestFocus();
		video_hang_up.bringToFront();
		video_mute_text = findViewById(R.id.video_mute_text);
		call_video_r3 = findViewById(R.id.call_video_r3);
		call_video_r4 = findViewById(R.id.call_video_r4);
		video_switch = findViewById(R.id.video_switch);
		video_switch.setOnClickListener(this);
	}

	DisposableObserver<Long> timeOb=null;
	private void startCallTimeTask() {
		timeOb = new DisposableObserver<Long>(){
			@Override
			public void onNext(Long aLong) {
				long callTime = System.currentTimeMillis() - firstCallTime;
				String getTime = getDateTimes(callTime);
				video_callTime.setText(getTime);
			}

			@Override
			public void onError(Throwable e) {
				dispose();
			}

			@Override
			public void onComplete() {
				dispose();

			}

		};
		Observable.interval(0,1,TimeUnit.SECONDS)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(timeOb);

	}
	private void stopCallTimeTask() {
		if (timeOb!=null){
			if (!timeOb.isDisposed()){
				timeOb.dispose();
				timeOb=null;
			}
		}
	}
	private String  getDateTimes(Long time){
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT00:00"));
		return sdf.format(new Date(time));
	}
	private void initCamera() {
		int numberOfCameras = Camera.getNumberOfCameras();
		Log.i(TAG,"get camera number:"+numberOfCameras);
		boolean hasMicroPhone = AudioUtil.hasMicroPhone(this);
		if (numberOfCameras==0 && hasMicroPhone == false){
			Log.i(TAG, "initCamera: not camera and not microphone");
			dialog = new NotCameraDialog(this);
			dialog.Builder(this).show();
		}else if (numberOfCameras==0 &&hasMicroPhone == true){
			Log.i(TAG, "initCamera: has microphone");
//			qiev();
		}else{
//			CameraUtil.initCamera(instance);
		}
	}
	private String message;
	private void initData() {
		callRecord = new CallRecord();
		Intent intent = getIntent();
		callRecord.call_phone="";
		callRecord.call_name ="";
		if (intent.hasExtra("message")){
			message = intent.getStringExtra("message");
			Log.i(TAG,"message:"+message);
			try {
				if (!TextUtils.isEmpty(message)){
					if (message.contains("<tel:")){
						String[] data = message.split("<tel:");
						String number = data[1].substring(0, data[1].indexOf(";"));
						callRecord.call_phone = number;
						callRecord.call_name = number;
						List<Contacts> contactsList = ContactsDao.Companion.getInstance(this).queryPhone(number);
						if (contactsList!=null&&contactsList.size()!=0){
							callRecord.call_name = contactsList.get(0).getUser_name();
						}
					}else{
						Call currentCall = instance.getCore().getCurrentCall();
						if (currentCall!=null){
							Log.i(TAG, "initData: 0");
							initDisplayName(currentCall);
						}else{
							for (Call call : instance.getCore().getCalls()) {
								if (call != null && call.getConference() != null) {
									if (instance.getCore().isInConference()) {
										Log.i(TAG, "initData: 1");
										initDisplayName(currentCall);
									}
								} else if (call != null && call != currentCall) {
									Call.State state = call.getState();
									if (state == Call.State.Paused
											|| state == Call.State.PausedByRemote
											|| state == Call.State.Pausing) {
										Log.i(TAG, "initData: 2");
										initDisplayName(currentCall);
									}
								}
							}
						}
					}
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}else if (intent.hasExtra("number")){
			String  number = intent.getStringExtra("number");
			callRecord.call_phone = number;
			callRecord.call_name = number;
			List<Contacts> contactsList = ContactsDao.Companion.getInstance(this).queryPhone(number);
			if (contactsList!=null&&contactsList.size()!=0){
				callRecord.call_name = contactsList.get(0).getUser_name();
			}
		}
		if(intent.hasExtra(CallRecordTable.INSTANCE.getCALL_STATUS())){
			String call_status = intent.getStringExtra(CallRecordTable.INSTANCE.getCALL_STATUS());;
			if (!TextUtils.isEmpty(call_status)){
				callRecord.call_status = call_status;
				firstCallTime = System.currentTimeMillis();
				startCallTimeTask();
			}else{
				callRecord.call_status = CallInfo.INSTANCE.getCALL_OUT();
			}
		}
	}

	private void initDisplayName(Call currentCall) {
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
			callRecord.call_phone = displayName;
			callRecord.call_name = displayName;
			try {
				if (displayName.contains("tel:")){
					String[] data = displayName.split("tel:");
					String number = data[1].substring(0, data[1].indexOf(";"));
					callRecord.call_phone = number;
					callRecord.call_name = number;
					List<Contacts> contactsList = ContactsDao.Companion.getInstance(this).queryPhone(number);
					if (contactsList!=null&&contactsList.size()!=0){
						callRecord.call_name = contactsList.get(0).getUser_name();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			callRecord.call_name = "Anonymous";
			callRecord.call_phone = "";
		}
	}



	private CallTimeDialog callTimeDialog;
	private DisposableObserver<Long> callTimeObserver;
	private void initCallTime() {
		callTimeDialog = new CallTimeDialog(this);
		callTimeDialog.setConfirmOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				HangUp();
				callTimeDialog.cancel();
			}
		});
		callTimeDialog.setCallOverCallBack(new CallOverCallBack(){

			@Override
			public void onOver() {
				HangUp();
			}
		});
		clearCallTimeOut();
		callTimeObserver = new DisposableObserver<Long>() {
			@SuppressLint("NewApi")
			@Override
			public void onNext(Long aLong) {
				if (!isDestroyed()) {
					callTimeDialog.show();
					onComplete();
				}
			}

			@Override
			public void onError(Throwable e) {
				dispose();
			}

			@Override
			public void onComplete() {
				dispose();
			}
		};
		Observable.interval(1, TimeUnit.HOURS)
//		Observable.interval(30, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(callTimeObserver);
	}



	private void registerBroadCast() {
		//广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BaseConfig.RECEIVE_VIDEO_ACTIVITY);
		intentFilter.addAction(BaseConfig.RECEIVE_MAIN_ACTIVITY);
		intentFilter.addAction(BaseConfig.RECEIVE_UPB_CHANGE);
		intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		mReceiver = new VideoActivityReceiver();
		registerReceiver(mReceiver, intentFilter);
		IntentFilter usbFilter = new IntentFilter();
		usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(UsbReceiver,usbFilter);
	}
	private BroadcastReceiver UsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (!TextUtils.isEmpty(action)){
				switch (action){
					case UsbManager.ACTION_USB_DEVICE_DETACHED://接收到U盘设设备拔出广播
						Log.e(TAG, "get action: ACTION_USB_DEVICE_DETACHED");
					Toast.makeText(context,"检测到USB设备被拔出,请确认摄像头是否正常连接",Toast.LENGTH_LONG).show();
						if (getCameras()==0){
//									new NotCameraDialog(context).Builder().show();
							dialog = new NotCameraDialog(context);
							dialog.Builder(context).show();
						}else{
							CameraUtil.initCamera(instance);
						}
						break;
					case UsbManager.ACTION_USB_DEVICE_ATTACHED://接收到U盘设备插入广播
						Log.e(TAG, "ACTION_USB_DEVICE_ATTACHED");
						Toast.makeText(context,"检测到USB设备正在接入……",Toast.LENGTH_LONG).show();
						CameraUtil.initCamera(instance);
						switchCamera(1);
						break;
						default:
						break;
				}
			}
		}
	};

	private void resizePreview() {
		Log.i(TAG, "resizePreview: ");
		Core core = LinphoneService.getCore();
		if (core.getCallsNb() > 0) {
			Call call = core.getCurrentCall();
			if (call == null) {
				call = core.getCalls()[0];
			}
			if (call == null) return;

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			int screenHeight = metrics.heightPixels;
			int maxHeight =
					screenHeight / 4; // Let's take at most 1/4 of the screen for the camera preview

			VideoDefinition videoSize =
					call.getCurrentParams()
							.getSentVideoDefinition(); // It already takes care of rotation
			if (videoSize.getWidth() == 0 || videoSize.getHeight() == 0) {
				Log.w(TAG,
						"[Video] Couldn't get sent video definition, using default video definition");
				videoSize = core.getPreferredVideoDefinition();
			}
			int width = videoSize.getWidth();
			int height = videoSize.getHeight();

			Log.d(TAG,"[Video] Video height is " + height + ", width is " + width);
			width = width * maxHeight / height;
			height = maxHeight;

			if (mPreviewView == null) {
				Log.e(TAG,"[Video] mCaptureView is null !");
				return;
			}

			RelativeLayout.LayoutParams newLp = new RelativeLayout.LayoutParams(width, height);
			newLp.addRule(
					RelativeLayout.ALIGN_PARENT_BOTTOM,
					1); // Clears the rule, as there is no removeRule until API 17.
			newLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
			mPreviewView.setLayoutParams(newLp);
			Log.d(TAG,"[Video] Video preview size set to " + width + "x" + height);
		}
	}

	private boolean isMute = false;
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.video_hang_up:
				HangUp();
				break;
			case R.id.video_mute:
				isMute =!isMute;
				Core lc = instance.getmCore();
				lc.enableMic(!isMute);
				if (isMute){
					Toast.makeText(this,"已静音",Toast.LENGTH_SHORT).show();
					video_mute.setBackgroundResource(R.mipmap.mute2);
				}else{
					Toast.makeText(this,"已恢复",Toast.LENGTH_SHORT).show();
					video_mute.setBackgroundResource(R.mipmap.mute);
				}
				break;
//			case R.id.video_speaker:
//				isSpeaker = !isSpeaker;
//				instance.lilin_qie(isSpeaker);
//				if (isSpeaker){
//
//				}else{
//
//				}
//				break;
			case R.id.video_qiev:
//				switchCamera(0);
				qiev();
				break;
			case R.id.video_switch:
				try {
//					if (System.currentTimeMillis() - switchTime>10000){
//						switchTime = System.currentTimeMillis();
//						isSwitch =!isSwitch;
//						CameraUtil.changeScreen(isSwitch,callvideo_rl2,callvideo_rl1,mPreviewView,mRenderingView);
//					}else{
//						Toast.makeText(this,"请稍后尝试",Toast.LENGTH_SHORT).show();
//					}
				}catch (Exception e){
					e.printStackTrace();
				}
				break;
			default:
				break;
		}
	}

	private void qiev() {
//		try {
//            instance.lilin_jie(false);
//            Intent intent = new Intent(this, AudioActivity.class);
//            if (!TextUtils.isEmpty(message)){
//                intent.putExtra("message",message);
//            }
//            intent.putExtra(CallRecordTable.INSTANCE.getCALL_STATUS(), CallInfo.INSTANCE.getCALL_IN());
//            startActivity(intent);
////            instance.updateCall();
//            finish();
//        }catch (LinphoneCoreException e){
//            e.printStackTrace();
//        }
	}

	private long switchTime = 0;
	private boolean isSwitch = false;

	private void HangUp() {
		if (!callRecord.call_status.equals(CallInfo.INSTANCE.getCALL_IN())){
			callRecord.call_status = CallInfo.INSTANCE.getCALL_OUT();
		}
		instance.hangUp();
		callTimeDialog.cancel();
		finish();
	}

	private boolean isSpeaker = true;


	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "onResume: ");
//		resizePreview();

	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "onPause: ");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
		if (UsbReceiver!=null){
			unregisterReceiver(UsbReceiver);
		}
		Core core = instance.getCore();
		if (core != null) {
			core.setNativeVideoWindowId(null);
			core.setNativePreviewWindowId(null);
		}
		mPreviewView = null;
		mRenderingView = null;

		if (null!=dialog&& dialog.isShow()){
			dialog.cancel();
		}
		if (null!=callTimeDialog&&callTimeDialog.isShow()){
			callTimeDialog.cancel();
		}
		callRecord.call_time = System.currentTimeMillis()+"".trim();
		CallRecordUtil.updateCallRecord(this,callRecord);
		clearCallTimeOut();
		stopCallTimeTask();
		UserInfoUtil userInfoUtil = UserInfoUtil.getInstance(this);
		String userId= userInfoUtil.getUserId();
		TalkBean talkbean = new TalkBean();
		talkbean.setUserId(userId);
		talkbean.setCall_phone(callRecord.call_phone);
		talkbean.setCall_status(callRecord.call_status);
		talkbean.setTalkDuration((System.currentTimeMillis()-firstCallTime)/1000);
		talkbean.setTalkTime(TimeUtil.getDateTime(TimeUtil.dateFormatYMDHMS,firstCallTime)) ;
		TalkManage.addConversationLog(talkbean,new TalkCallBack (){

			@Override
			public void onSuccess() {

			}

			@Override
			public void OnFailed() {

			}
		});
	}

	private void clearCallTimeOut() {
		if (callTimeObserver!=null&&!callTimeObserver.isDisposed()){
			callTimeObserver.dispose();
		}
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		Log.i(TAG, "dispatchKeyEvent: event:" + event.getAction());
		if (KeyEvent.ACTION_DOWN == event.getAction()) {
			stopTimerOut();
			initTimerOut();
			isOnTouch= true;
			initTouch(isOnTouch);
		}
		return super.dispatchKeyEvent(event);
	}



	public class VideoActivityReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getStringExtra("action");
			switch (action) {
				case "end":
					finish();
					break;
				case "show_status":
					Log.i(TAG, "onReceive: show_status");
					String data = intent.getStringExtra("data");
					if (TextUtils.isEmpty(data)){
						return;
					}
					if (data.contains("Call terminated")){
//						callRecord.call_status = CallInfo.INSTANCE.getCALL_RINGING();
						finish();
					}else if (data.contains("You have missed 1 call.")){
						callRecord.call_status = CallInfo.INSTANCE.getCALL_MISSED();
						finish();
					}else if (data.contains("Request timeout.")){
						callRecord.call_status = CallInfo.INSTANCE.getCALL_MISSED();
						finish();
					}else if(data.contains("Call released")){
//						callRecord.call_status = CallInfo.INSTANCE.getCALL_ANSWER();
						finish();
					}else if (data.contains("Call is updated by remote")){
//						instance.lilin_qie_updatecall();
//						Intent audioIntent = new Intent(context, AudioActivity.class);
//						if (!TextUtils.isEmpty(message)){
//							audioIntent.putExtra("message",message);
//						}
//						audioIntent.putExtra(CallRecordTable.INSTANCE.getCALL_STATUS(), CallInfo.INSTANCE.getCALL_IN());
//						startActivity(audioIntent);
//						finish();
					}else if (data.equals("Connected")){
						firstCallTime = System.currentTimeMillis();
						startCallTimeTask();
					}
					break;
				case "receive_usb_change":
					String status = intent.getStringExtra("status");

					if(!TextUtils.isEmpty(status)){
						switch (status){
							case Intent.ACTION_MEDIA_CHECKING:
								Log.e(TAG, "get action: ACTION_MEDIA_CHECKING");
								break;
							case Intent.ACTION_MEDIA_MOUNTED:
								Log.e(TAG, "get action: ACTION_MEDIA_MOUNTED");
								if (getCameras()>0){
									switchCamera(1);
								}
								break;
							case Intent.ACTION_MEDIA_UNMOUNTED: //U盘卸载
								Log.e(TAG, "get action:ACTION_MEDIA_UNMOUNTED");
								if (getCameras()==0){
//									new NotCameraDialog(context).Builder().show();
									dialog = new NotCameraDialog(context);
									dialog.Builder(context).show();
								}
								break;
							case UsbManager.ACTION_USB_DEVICE_DETACHED://接收到U盘设设备拔出广播
								Log.e(TAG, "get action: ACTION_USB_DEVICE_DETACHED");
								if (getCameras()==0){
//									new NotCameraDialog(context).Builder().show();
									dialog = new NotCameraDialog(context);
									dialog.Builder(context).show();
								}else{
									CameraUtil.initCamera(instance);
								}
								break;
							case UsbManager.ACTION_USB_DEVICE_ATTACHED://接收到U盘设备插入广播
								Log.e(TAG, "ACTION_USB_DEVICE_ATTACHED");
								CameraUtil.initCamera(instance);
								switchCamera(1);
								break;
							case Intent.ACTION_MEDIA_EJECT:
								Log.e(TAG, "ACTION_MEDIA_EJECT");
								break;
						}
					}
					break;
				default:
					break;
			}
		}
	}


	private int getCameras() {
		AndroidCameraConfiguration.AndroidCamera[] androidCameras = AndroidCameraConfiguration.retrieveCameras();
		Log.i(TAG, "getCameras: "+ androidCameras.length);
		return androidCameras.length;
	}
//	DisposableObserver<Long> CameraObserver;
	public void switchCamera(long nmb) {
		Log.i(TAG, "switchCamera: ");
		CameraUtil.initCamera(instance);
	}

//	private   void fixZOrder(SurfaceView rendering, SurfaceView preview) {
//		rendering.setZOrderOnTop(false);
//		preview.setZOrderOnTop(true);
//		preview.setZOrderMediaOverlay(true); // Needed to be able to display control layout over
//	}


	private AlphaAnimation alphaAnimation0To1;
	private AlphaAnimation alphaAnimation1To0;
	private void initAlphaAnimation1() {
		alphaAnimation0To1 = new AlphaAnimation(0f, 1f);
		alphaAnimation0To1.setDuration(1000);
		alphaAnimation0To1.setFillAfter(true);

	}

	private void initAlphaAnimation2() {
		alphaAnimation1To0 = new AlphaAnimation(1f, 0f);
		alphaAnimation1To0.setDuration(1000);
		alphaAnimation1To0.setFillAfter(true);
	}

	boolean isOnTouch = false;
	//	Observable<Long> longObservable;
	DisposableObserver<Long> disposableObserver;
	private void initTimerOut(){
		if (disposableObserver!=null&&!disposableObserver.isDisposed()){
			disposableObserver.dispose();
		}
		disposableObserver = new DisposableObserver<Long>() {
			@SuppressLint("NewApi")
			@Override
			public void onNext(Long aLong) {
				if (!isDestroyed()) {
					call_video_r3.clearAnimation();
					call_video_r3.setAnimation(alphaAnimation1To0);
					call_video_r4.clearAnimation();
					call_video_r4.setAnimation(alphaAnimation1To0);
					isOnTouch = false;
					initTouch(isOnTouch);
				}
				onComplete();
			}

			@Override
			public void onError(Throwable e) {
				dispose();
				call_video_r3.clearAnimation();
				call_video_r4.clearAnimation();
				initTouch(true);
			}

			@Override
			public void onComplete() {
				dispose();
				initTouch(true);
			}
		};
		Observable.interval(8, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.io()).
				observeOn(AndroidSchedulers.mainThread())
				.subscribe(disposableObserver);

	}

	private void initTouch(boolean b) {
		video_hang_up.setEnabled(b);
		video_mute.setEnabled(b);
		video_qiev.setEnabled(b);
	}

	/**
	 * 取消控件隐藏功能
	 */
	private void stopTimerOut() {
		Log.i(TAG, "stopTimerOut: ");
//		call_video_r3.clearAnimation();
		Animation animation = call_video_r3.getAnimation();
		if (alphaAnimation1To0.equals(animation)) {
			call_video_r3.clearAnimation();
			call_video_r3.startAnimation(alphaAnimation0To1);
		}
		Animation animation1 = call_video_r4.getAnimation();
		if (alphaAnimation1To0.equals(animation1)){
			call_video_r4.clearAnimation();
			call_video_r4.startAnimation(alphaAnimation0To1);
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			return  false;
		}
		return super.onKeyDown(keyCode, event);
	}
}
