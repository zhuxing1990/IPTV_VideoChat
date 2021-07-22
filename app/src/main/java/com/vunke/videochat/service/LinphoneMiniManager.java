//package com.vunke.videochat.service;
//
///*
//LinphoneMiniManager.java
//Copyright (C) 2017  Belledonne Communications, Grenoble, France
//
//This program is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2
//of the License, or (at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
//*/
//
//import android.app.ActivityManager;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager.NameNotFoundException;
//import android.media.AudioManager;
//import android.os.IBinder;
//import android.preference.CheckBoxPreference;
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.vunke.videochat.R;
//import com.vunke.videochat.config.BaseConfig;
//import com.vunke.videochat.config.CallInfo;
//import com.vunke.videochat.db.CallRecordTable;
//import com.vunke.videochat.dialog.CallInDialog;
//import com.vunke.videochat.tools.AccountBuilder;
//import com.vunke.videochat.tools.LinphoneMiniUtils;
//import com.vunke.videochat.ui.CallInActivity;
//
//import org.linphone.core.LinphoneAddress;
//import org.linphone.core.LinphoneAuthInfo;
//import org.linphone.core.LinphoneCall;
//import org.linphone.core.LinphoneCall.State;
//import org.linphone.core.LinphoneCallParams;
//import org.linphone.core.LinphoneCallStats;
//import org.linphone.core.LinphoneChatMessage;
//import org.linphone.core.LinphoneChatRoom;
//import org.linphone.core.LinphoneContent;
//import org.linphone.core.LinphoneCore;
//import org.linphone.core.LinphoneCore.AuthMethod;
//import org.linphone.core.LinphoneCore.EcCalibratorStatus;
//import org.linphone.core.LinphoneCore.GlobalState;
//import org.linphone.core.LinphoneCore.LogCollectionUploadState;
//import org.linphone.core.LinphoneCore.RegistrationState;
//import org.linphone.core.LinphoneCore.RemoteProvisioningState;
//import org.linphone.core.LinphoneCoreException;
//import org.linphone.core.LinphoneCoreFactory;
//import org.linphone.core.LinphoneCoreListener;
//import org.linphone.core.LinphoneEvent;
//import org.linphone.core.LinphoneFriend;
//import org.linphone.core.LinphoneFriendList;
//import org.linphone.core.LinphoneInfoMessage;
//import org.linphone.core.LinphoneNatPolicy;
//import org.linphone.core.LinphoneProxyConfig;
//import org.linphone.core.OpenH264DownloadHelperListener;
//import org.linphone.core.PayloadType;
//import org.linphone.core.PublishState;
//import org.linphone.core.SubscriptionState;
//import org.linphone.core.VideoSize;
//import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration;
//import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration.AndroidCamera;
//import org.linphone.tools.OpenH264DownloadHelper;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.util.Timer;
//import java.util.TimerTask;
//
//import io.reactivex.Observable;
//import io.reactivex.ObservableEmitter;
//import io.reactivex.ObservableOnSubscribe;
//import io.reactivex.android.schedulers.AndroidSchedulers;
//import io.reactivex.observers.DisposableObserver;
//import io.reactivex.schedulers.Schedulers;
//
//public class LinphoneMiniManager extends Service implements LinphoneCoreListener {
//	private static final String TAG = "Linphone";
//	private static LinphoneMiniManager mInstance;
//	private Context mContext;
//	private LinphoneCore mLinphoneCore;
//	private Timer mTimer;
//	private LinphoneCoreFactory lcFactory;
//
//	public LinphoneCore getmLinphoneCore() {
//		return mLinphoneCore;
//	}
//
//	public void setmLinphoneCore(LinphoneCore mLinphoneCore) {
//		this.mLinphoneCore = mLinphoneCore;
//	}
//
//	public LinphoneCoreFactory getLcFactory() {
//		return lcFactory;
//	}
//
//	public void setLcFactory(LinphoneCoreFactory lcFactory) {
//		this.lcFactory = lcFactory;
//	}
//
//	private OpenH264DownloadHelper mCodecDownloader;
//	public static boolean isReady() {
//		return mInstance != null;
//	}
//
//	@Override
//	public void onCreate() {
//		super.onCreate();
//
//		mContext = this;
//		lcFactory = LinphoneCoreFactory.instance();
////		lcFactory.setDebugMode(true, TAG);
////		lcFactory.setLogCollectionPath(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"vunke"+File.separator+"videochat"+File.separator);
//		try {
//			String basePath = mContext.getFilesDir().getAbsolutePath();
//			copyAssetsFromPackage(basePath);
//			mLinphoneCore = lcFactory.createLinphoneCore(this, basePath + "/.linphonerc", basePath + "/linphonerc", null, mContext);
//			initLinphoneCoreValues(basePath);
//			setUserAgent();
//			startIterate();
//			mInstance = this;
//
////			mLinphoneCore.getConfig().setInt("audio", "codec_bitrate_limit", 128);
//			float preferredFramerate = mLinphoneCore.getPreferredFramerate();
//			Log.i(TAG, "onCreate: 视频帧速率:"+preferredFramerate);
//			int uploadBandwidth = mLinphoneCore.getUploadBandwidth();
//			Log.i(TAG, "onCreate: 最大可用上传带宽:"+uploadBandwidth+"/秒");
//			int downloadBandwidth = mLinphoneCore.getDownloadBandwidth();
//			Log.i(TAG, "onCreate: 最大可用下载带宽"+downloadBandwidth+"/秒");
//			String[] supportedVideoSizes = mLinphoneCore.getSupportedVideoSizes();
//			for (String str:supportedVideoSizes){
//				Log.i(TAG, "onCreate: 支持的视频分辨率:"+str);
////				if (str.contains("720p")){
////					mLinphoneCore.setPreferredVideoSize(VideoSize.VIDEO_SIZE_720P);
////				}else if (str.contains("1020p")){
////					mLinphoneCore.setPreferredVideoSize(VideoSize.VIDEO_SIZE_1020P);
////				}
//			}
////			mLinphoneCore.setPreferredVideoSize(VideoSize.VIDEO_SIZE_QVGA);
//			VideoSize preferredVideoSize = mLinphoneCore.getPreferredVideoSize();
//			Log.i(TAG, "onCreate: 获取视频分辨率:"+preferredVideoSize);
//			// 默认的视频分辨率  320*240 不用设置
////			mLinphoneCore.setPreferredVideoSize(VideoSize.VIDEO_SIZE_QVGA);
////			mLinphoneCore.setPreferredVideoSize(VideoSize.VIDEO_SIZE_QCIF);
//			mLinphoneCore.setPreferredVideoSize(VideoSize.VIDEO_SIZE_720P);
////			mLinphoneCore.setPreferredVideoSize(VideoSize.VIDEO_SIZE_1020P);
//			// 默认的视频分辨率  320*240 不用设置
////			mLinphoneCore. enableAdaptiveRateControl(true);//启用自适应速率控制
////			mLinphoneCore.setPreferredVideoSizeByName("720p");
//
//
//			//audio 码率设置
//			//			mLinphoneCore.setPreferredFramerate(24);
//			//			mLinphoneCore.setPreferredFramerate(30);
//
//
////			mLinphoneCore.setUploadBandwidth(1536);
////			mLinphoneCore.setDownloadBandwidth(1536);
//			boolean isEchoCancellation =  true;
//			mLinphoneCore.enableEchoCancellation(isEchoCancellation);
//			mLinphoneCore.setNetworkReachable(true); // Let's assume it's true
//			mLinphoneCore.enableVideo(true, true);//视频可用
//			setSipPore(-1);
//
//			setFrontCamAsDefault();
//			//mLinphoneCore.enableDownloadOpenH264(true);
//			//    Log.e("<<<"+getApplicationInfo().nativeLibraryDir);
//		   // System.load(getApplicationInfo().nativeLibraryDir+"libmsopenh264.so");
//			//System.loadLibrary ("openh264");
//
//		//	H264Helper.setH264Mode(H264Helper.MODE_OPENH264,mLinphoneCore);
//
//			/**
//			LinphoneNatPolicy nat = getOrCreateNatPolicy();
//			nat.setStunServer("118.190.151.162:3478" );
//			nat.enableIce(true);
//			nat.enableTurn(true);
//			mLinphoneCore.setNatPolicy(nat);
//			setTurnUsernamePwd("lilin","lilinaini");**/
//
//            Intent intent1 = new Intent(BaseConfig.INSTANCE.getRECEIVE_MAIN_ACTIVITY());
//            intent1.putExtra("action", "show_version");intent1.putExtra("data", mLinphoneCore.getVersion() );
//            sendBroadcast( intent1);
//
//		} catch (LinphoneCoreException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		mCodecDownloader = LinphoneCoreFactory.instance().createOpenH264DownloadHelper();
//
//	}
//	public void setTurnUsernamePwd(String username,String password) {
//		if (getLC() == null) return;
//		LinphoneNatPolicy nat = getOrCreateNatPolicy();
//		LinphoneAuthInfo authInfo = getLC().findAuthInfo(null, nat.getStunServerUsername(), null);
//
//		if (authInfo != null) {
//			LinphoneAuthInfo cloneAuthInfo = authInfo.clone();
//			getLC().removeAuthInfo(authInfo);
//			cloneAuthInfo.setUsername(username);
//			cloneAuthInfo.setUserId( username );
//			cloneAuthInfo.setPassword(password);
//			getLC().addAuthInfo(cloneAuthInfo);
//		} else {
//			authInfo = lcFactory.instance().createAuthInfo(username, username, password, null, null, null);
//			getLC().addAuthInfo(authInfo);
//		}
//		nat.setStunServerUsername(username);
//		getLC().setNatPolicy(nat);
//	}
//
//	private LinphoneNatPolicy getOrCreateNatPolicy() {
//		LinphoneNatPolicy nat = mLinphoneCore.getNatPolicy();
//		if (nat == null) {
//			nat = mLinphoneCore.createNatPolicy();
//		}
//		return nat;
//	}
//	//取消注册
//	public void unRegisterUserAuth() {
//		mLinphoneCore.clearAuthInfos();
//	}
//
//	public static LinphoneMiniManager getInstance() {
//		return mInstance;
//	}
//	public static LinphoneCore getLC() {
//		if(null==mInstance){ return null; }
//		return mInstance.mLinphoneCore;
//	}
//
//	public void destroy() {
//		try {
//			mTimer.cancel();
//			mLinphoneCore.destroy();
//		}
//		catch (RuntimeException e) {
//		}
//		finally {
//			mLinphoneCore = null;
//
//			mInstance = null;
//		}
//	}
//
//	private void startIterate() {
//		TimerTask lTask = new TimerTask() {
//			@Override
//			public void run() {
//				mLinphoneCore.iterate();
//			}
//		};
//
//		/*use schedule instead of scheduleAtFixedRate to avoid iterate from being call in burst after cpu wake up*/
//		mTimer = new Timer("LinphoneMini scheduler");
//		mTimer.schedule(lTask, 0, 20);
//	}
//
//	private void setUserAgent() {
//		try {
//			String versionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
//			if (versionName == null) {
//				versionName = String.valueOf(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode);
//			}
//			mLinphoneCore.setUserAgent("LinphoneMiniAndroid", versionName);
//		} catch (NameNotFoundException e) {
//		}
//	}
//	public void loadh264(){
//		mLinphoneCore.enableDownloadOpenH264(true);
//		OpenH264DownloadHelper mCodecDownloader = LinphoneCoreFactory.instance().createOpenH264DownloadHelper();
//
//		OpenH264DownloadHelperListener mCodecListener = new OpenH264DownloadHelperListener() {
//
//
//			@Override
//			public void OnProgress(final int current, final int max) {
//				Log.e("loadh264",max+"---------->"+current);
//			}
//
//			@Override
//			public void OnError(final String error) {
//				Log.e("loadh264","down---------->"+error);
//			}
//		};
//		mCodecDownloader.setOpenH264HelperListener(mCodecListener);
//		mCodecDownloader.downloadCodec();
//	}
//
//	private void setFrontCamAsDefault() {
//		int camId = 0;
//		AndroidCamera[] cameras = AndroidCameraConfiguration.retrieveCameras();
//		for (AndroidCamera androidCamera : cameras) {
//			if (androidCamera.frontFacing) camId = androidCamera.id;
//		}
//		mLinphoneCore.setVideoDevice(camId);
//	}
//
//	private void copyAssetsFromPackage(String basePath) throws IOException {
//		LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.oldphone_mono, basePath + "/oldphone_mono.wav");
//		LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.ringback, basePath + "/ringback.wav");
//		LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.toy_mono, basePath + "/toy_mono.wav");
//		LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.linphonerc_default, basePath + "/.linphonerc");
//		LinphoneMiniUtils.copyFromPackage(mContext, R.raw.linphonerc_factory, new File(basePath + "/linphonerc").getName());
//		LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.lpconfig, basePath + "/lpconfig.xsd");
//		LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.rootca, basePath + "/rootca.pem");
//	}
//
//	private void initLinphoneCoreValues(String basePath) {
//		mLinphoneCore.setContext(mContext);
//		mLinphoneCore.setRootCA(basePath + "/rootca.pem");
//		mLinphoneCore.setPlayFile(basePath + "/toy_mono.wav");
//		mLinphoneCore.setChatDatabasePath(basePath + "/linphone-history.db");
//		int availableCores = Runtime.getRuntime().availableProcessors();
//		mLinphoneCore.setCpuCount(availableCores);// 设置有效的cpu数量
//	}
//
//	@Override
//	public void authInfoRequested(LinphoneCore lc, String realm, String username, String domain) {
//		Log.e(TAG,"提示:lilin authInfoRequested: "+username  );
//	}
//
//	@Override
//	public void globalState(LinphoneCore lc, GlobalState state, String message) {
//		Log.e(TAG,"提示:lilin Global state: " + state + "(" + message + ")");
//	}
//
//	@Override
//	public void callState(LinphoneCore lc, LinphoneCall call, State cstate,
//                          String message) {
//		Log.d(TAG,"提示:lilin Call state1: " + cstate + "(" + message + ")");
//		if (message.contains("Call released")){
//			Intent intent1 = new Intent(BaseConfig.INSTANCE.getRECEIVE_MAIN_ACTIVITY());
//			intent1.putExtra("action", "show_status");intent1.putExtra("data", message );
//			sendBroadcast( intent1);
////			dismissDialog();
//		}
////		LinphoneCallParams params = mLinphoneCore.createCallParams(call);
////		String header = params.getCustomHeader("Supported");
////		Log.i(TAG, "callState: header:"+header);
//	}
//
//	@Override
//	public void callStatsUpdated(LinphoneCore lc, LinphoneCall call,
//                                 LinphoneCallStats stats) {
//		//Log.e("lilin  callStatsUpdated: "+stats.toString() );
//	}
//
//	@Override
//	public void callEncryptionChanged(LinphoneCore lc, LinphoneCall call,
//                                      boolean encrypted, String authenticationToken) {
//		Log.e(TAG,"提示:lilin  callEncryptionChanged: " );
//	}
//	private boolean isLogin = false;
//
//	public boolean isLogin() {
//		return isLogin;
//	}
//
//	public void setLogin(boolean login) {
//		isLogin = login;
//	}
//
//	@Override
//	public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg,
//                                  RegistrationState cstate, String smessage) {
//		//Log.e("lilin Registration state: " + cstate + "(" + smessage + ")");
//		Intent intent = new Intent(BaseConfig.INSTANCE.getRECEIVE_MAIN_ACTIVITY());
//		intent.putExtra("action", "reg_state");intent.putExtra("data",smessage );
//		sendBroadcast( intent);
//		if (!TextUtils.isEmpty(smessage)){
//			if (smessage.contains("success")){
//				isLogin = true;
//			}else{
//				isLogin = false;
//			}
//		}else{
//			isLogin = false;
//		}
//		if( smessage.indexOf("successful")!=-1){
//			String str="";
//			PayloadType[] audioCodecs = mLinphoneCore.getAudioCodecs();
//			for (PayloadType payloadType : audioCodecs) {
//				str+=payloadType.getMime()+"-"+payloadType.getRate()+"("+mLinphoneCore.isPayloadTypeEnabled( payloadType )+")<>";
//			}
//			str+="\n";
//			for (final PayloadType payloadType : mLinphoneCore.getVideoCodecs()) {
//				Log.i(TAG, "payloadType.getMine: "+payloadType.getMime());
//				if(payloadType.getMime().equalsIgnoreCase("VP8")){
//					Log.i("getPayloadType", "payloadType:VP8");
//					try {
//						mLinphoneCore.enablePayloadType( payloadType,false );
//					} catch (LinphoneCoreException e) {
//						Log.e("","》》》》》》》》》》》》");
//					}
//				}else if (payloadType.getMime().equalsIgnoreCase("H264")){
//					Log.i(TAG, "payloadType:H264");
//					if (lc.downloadOpenH264Enabled()) {
//						final CheckBoxPreference codec = new CheckBoxPreference(mContext);
//						codec.setTitle(payloadType.getMime());
//						if (mCodecDownloader.isCodecFound()) {
//							codec.setSummary(mCodecDownloader.getLicenseMessage());
//							codec.setTitle("OpenH264");
//						}
//						codec.setChecked(lc.isPayloadTypeEnabled(payloadType));
//					}else{
//						Log.i(TAG, "lc can't open H264: ");
//					}
//				}
//				str+=payloadType.getMime()+"("+mLinphoneCore.isPayloadTypeEnabled( payloadType )+")<>";
//			}
//
//			Intent intent1 = new Intent(BaseConfig.INSTANCE.getRECEIVE_MAIN_ACTIVITY());
//			intent1.putExtra("action", "show_code");intent1.putExtra("data",str );
//			sendBroadcast( intent1);
//		}
//
//	}
//
//	@Override
//	public void newSubscriptionRequest(LinphoneCore lc, LinphoneFriend lf,
//                                       String url) {
//		Log.e(TAG,"提示:lilin  newSubscriptionRequest: " );
//	}
//
//	@Override
//	public void notifyPresenceReceived(LinphoneCore lc, LinphoneFriend lf) {
//		Log.e(TAG,"提示:lilin  notifyPresenceReceived: " );
//	}
//
//
//	@Override
//	public void messageReceived(LinphoneCore lc, LinphoneChatRoom cr,
//                                LinphoneChatMessage message) {
//		Log.e(TAG,"提示:lilin  Message received from " + cr.getPeerAddress().asString() + " : " + message.getText() + "(" + message.getExternalBodyUrl() + ")");
//	}
//
//	@Override
//	public void isComposingReceived(LinphoneCore lc, LinphoneChatRoom cr) {
//		Log.e(TAG,"提示:lilin Composing received from " + cr.getPeerAddress().asString());
//
//	}
//
//	@Override
//	public void dtmfReceived(LinphoneCore lc, LinphoneCall call, int dtmf) {
//		Log.e(TAG,"提示:lilin  dtmfReceived: " );
//	}
//
//	@Override
//	public void ecCalibrationStatus(LinphoneCore lc, EcCalibratorStatus status,
//                                    int delay_ms, Object data) {
//		Log.e(TAG,"提示:lilin  ecCalibrationStatus: " );
//		if (status == EcCalibratorStatus.InProgress) return;
//		mLinphoneCore.removeListener(this);
//		AudioManager mAudioManager = ((AudioManager)getSystemService(Context.AUDIO_SERVICE));
//		mAudioManager.setSpeakerphoneOn(false);
//		mAudioManager.setMode(AudioManager.MODE_NORMAL);
//		if (mLinphoneCore.needsEchoCalibration()){
//			//回声消除
//			boolean isEchoCancellation =  true;
//			mLinphoneCore.enableEchoCancellation(isEchoCancellation);
//			Log.i(TAG, "ecCalibrationStatus: true");
//		}
//	}
//
//	@Override
//	public void notifyReceived(LinphoneCore lc, LinphoneCall call,
//                               LinphoneAddress from, byte[] event) {
//		Log.e(TAG,"提示:lilin  notifyReceived: " );
//	}
//
//	@Override
//	public void transferState(LinphoneCore lc, LinphoneCall call,
//                              State new_call_state) {
//		Log.e(TAG,"提示:lilin  transferState: " );
//	}
//
//	@Override
//	public void infoReceived(LinphoneCore lc, LinphoneCall call,
//                             LinphoneInfoMessage info) {
//		Log.e(TAG,"提示:lilin  infoReceived: " );
//	}
//
//	@Override
//	public void subscriptionStateChanged(LinphoneCore lc, LinphoneEvent ev,
//                                         SubscriptionState state) {
//		Log.e(TAG,"提示:lilin  subscriptionStateChanged: " );
//	}
//
//	@Override
//	public void notifyReceived(LinphoneCore lc, LinphoneEvent ev,
//                               String eventName, LinphoneContent content) {
//		Log.e(TAG,"提示:lilin Notify received: " + eventName + " -> " + content.getDataAsString());
//	}
//
//	@Override
//	public void publishStateChanged(LinphoneCore lc, LinphoneEvent ev,
//                                    PublishState state) {
//		Log.e(TAG,"提示:lilin  publishStateChanged: " );
//	}
//
//	@Override
//	public void configuringStatus(LinphoneCore lc,
//                                  RemoteProvisioningState state, String message) {
//		Log.e(TAG,"提示:lilin Configuration state: " + state + "(" + message + ")");
//	}
//
//	@Override
//	public void show(LinphoneCore lc) {
//		Log.e(TAG,"提示:lilin  show: " );
//	}
//
//	@Override
//	public void displayStatus(LinphoneCore lc, String message) {
//		Log.e(TAG,"提示:lilin  displayStatus: "+message );
//		Intent intent1 = new Intent(BaseConfig.INSTANCE.getRECEIVE_MAIN_ACTIVITY());
//		intent1.putExtra("action", "show_status");intent1.putExtra("data", message );
//		sendBroadcast( intent1);
//		if (message.contains("is contacting you")){
//			boolean mangguoPlayer = isMangguoPlayer();
//			if (mangguoPlayer){
//				showCallInWindow(message);
//			}else{
//				Intent intent = new Intent(this, CallInActivity.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				intent.putExtra("message",message);
//				intent.putExtra(CallRecordTable.INSTANCE.getCALL_STATUS(), CallInfo.INSTANCE.getCALL_IN());
//				startActivity(intent);
//			}
//		}
//		if(message.indexOf("Call terminated")!=-1){
//			Intent intent = new Intent(BaseConfig.INSTANCE.getRECEIVE_VIDEO_ACTIVITY());
//			intent.putExtra("action", "end");
//			sendBroadcast( intent);
//			dismissDialog();
//		}
//		if (message.indexOf("You have missed 1 call.")!=-1){
//			Intent intent = new Intent(BaseConfig.INSTANCE.getRECEIVE_VIDEO_ACTIVITY());
//			intent.putExtra("action", "end");
//			sendBroadcast( intent);
//			dismissDialog();
//		}
//
//	}
//	private void dismissDialog() {
//		if (callInDialog!=null){
//			callInDialog.dismiss();
//		}
//	}
//	private CallInDialog callInDialog;
//	private void showCallInWindow(final String message) {
//		Observable.create(new ObservableOnSubscribe<String>() {
//			@Override
//			public void subscribe(ObservableEmitter<String> emitter) throws Exception {
//				emitter.onNext(message);
//				emitter.onComplete();
//			}
//		}).subscribeOn(Schedulers.io())
//				.observeOn(AndroidSchedulers.mainThread())
//				.subscribe(new DisposableObserver<String>() {
//					@Override
//					public void onNext(String s) {
//						callInDialog = new CallInDialog(getApplicationContext(), mInstance, s)
//								.builder();
//					}
//
//					@Override
//					public void onError(Throwable e) {
//						e.printStackTrace();
//						dispose();
//					}
//
//					@Override
//					public void onComplete() {
//						dispose();
//					}
//				});
//
//	}
//
//	private boolean isMangguoPlayer() {
//		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//		String  currentClassName =manager.getRunningTasks(1).get(0).topActivity.getClassName();
//		Log.i(TAG, "isMangguoPlayer: get top activity:"+currentClassName);
//		if (!currentClassName.contains("com.vunke.videochat")) {
//			Log.i(TAG, "isMangguoPlayer: mang guo apk is playing video ");
//			return true;
//		}else {
//			return false;
//		}
//	}
//	@Override
//	public void displayMessage(LinphoneCore lc, String message) {
//		Log.e(TAG,"提示:lilin  displayMessage: " );
//	}
//
//	@Override
//	public void displayWarning(LinphoneCore lc, String message) {
//		Log.e(TAG,"提示:lilin  displayWarning: "+message );
//	}
//
//	@Override
//	public void authenticationRequested(LinphoneCore arg0,
//                                        LinphoneAuthInfo arg1, AuthMethod arg2) {
//		// TODO Auto-generated method stub
//		Log.e(TAG,"提示:lilin  authenticationRequested: " );
//	}
//
//	@Override
//	public void fileTransferProgressIndication(LinphoneCore arg0,
//                                               LinphoneChatMessage arg1, LinphoneContent arg2, int arg3) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void fileTransferRecv(LinphoneCore arg0, LinphoneChatMessage arg1,
//                                 LinphoneContent arg2, byte[] arg3, int arg4) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public int fileTransferSend(LinphoneCore arg0, LinphoneChatMessage arg1,
//                                LinphoneContent arg2, ByteBuffer arg3, int arg4) {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public void friendListCreated(LinphoneCore arg0, LinphoneFriendList arg1) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void friendListRemoved(LinphoneCore arg0, LinphoneFriendList arg1) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void messageReceivedUnableToDecrypted(LinphoneCore arg0,
//                                                 LinphoneChatRoom arg1, LinphoneChatMessage arg2) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void networkReachableChanged(LinphoneCore arg0, boolean arg1) {
//		// TODO Auto-generated method stub
//		Log.e(TAG,"提示:lilin  networkReachableChanged: " );
//	}
//
//	@Override
//	public void uploadProgressIndication(LinphoneCore arg0, int arg1, int arg2) {
//		// TODO Auto-generated method stub
//		Log.e(TAG,"提示:lilin  uploadProgressIndication: " );
//	}
//
//	@Override
//	public void uploadStateChanged(LinphoneCore arg0,
//                                   LogCollectionUploadState arg1, String arg2) {
//		// TODO Auto-generated method stub
//		Log.e(TAG,"提示:lilin  uploadStateChanged: " );
//	}
//
//
//
//	/**
//	 * 再次打开不用注册了 会启用原来注册的
//	 * @param
//	 * @param password
//	 * @throws LinphoneCoreException
//	 */
//	public void lilin_reg(String domain,String username,String password ,String port,LinphoneAddress.TransportType ttype) throws LinphoneCoreException {
//
//		//
//		for (LinphoneProxyConfig linphoneProxyConfig : mLinphoneCore.getProxyConfigList()) {
//			mLinphoneCore.removeProxyConfig( linphoneProxyConfig);
//			Log.e("removeProxyConfig", "lilin_reg:  remove proxy config ");
//		}
//		for( LinphoneAuthInfo a: mLinphoneCore.getAuthInfosList()){
//			Log.e("removeAuthInfo", "lilin_reg:  remove auth info ");
//			mLinphoneCore.removeAuthInfo( a);
//		}
//        AccountBuilder builder = new AccountBuilder(getLC())
//                .setUsername(username)
//                .setDomain(domain+":"+port)
//                .setHa1(null)
//                .setUserId(username)
//                .setDisplayName("")
//                .setPassword(password);
//		String forcedProxy = "";
////		String forcedProxy = "10.255.25.48:5060";
//		if (!TextUtils.isEmpty(forcedProxy)) {
//			builder.setProxy(forcedProxy)
//					.setOutboundProxyEnabled(true)
//					.setAvpfRRInterval(5);
//		}
//
//		builder.setTransport(ttype);
//        builder.saveNewAccount(lcFactory);
//
//
//
////		//LinphoneAddress proxyAddr = lcFactory.createLinphoneAddress("sip:"+username+"@"+domain);
////		//proxyAddr.setTransport(LinphoneAddress.TransportType.LinphoneTransportUdp );
////
////		mLinphoneCore.addAuthInfo(lcFactory.createAuthInfo(username, password,null, domain+":"+port));
////		// create proxy config
////		Log.e(">ddd>"+domain+":"+port);
////		LinphoneProxyConfig proxyCfg = mLinphoneCore.createProxyConfig("sip:"+username+"@"+domain+":"+port,   domain+":"+port, null, true);
////
////		//proxyCfg.enablePublish(false);
////		//proxyCfg.setExpires(10000);
////
////		mLinphoneCore.addProxyConfig(proxyCfg); // add it to linphone
////		mLinphoneCore.setDefaultProxyConfig(proxyCfg);
//
//
//	}
//
//	public void setSipPore(int port) {
//		LinphoneCore.Transports transportPorts = mLinphoneCore.getSignalingTransportPorts();//端口
//		transportPorts.udp = port;
//		transportPorts.tcp = port;
//		transportPorts.tls = -1;
//		mLinphoneCore.setSignalingTransportPorts(transportPorts);
//	}
//
//	public void 	lilin_call(String username,String host,boolean isVideoCall) throws LinphoneCoreException {
//		LinphoneAddress address = mLinphoneCore.interpretUrl(username+ "@" + host);
//		address.setDisplayName(username);
//		LinphoneCallParams params = mLinphoneCore.createCallParams(null);
//		if (isVideoCall) {
//			params.setVideoEnabled(true);
//			//params.enableLowBandwidth(false);
//		} else {
//			params.setVideoEnabled(false);
//		}
//		//replaces,outbound,100rel,timer,precondition
////		params.addCustomHeader("Supported","replaces,outbound,100rel");
//		LinphoneCall call = mLinphoneCore.inviteAddressWithParams(address, params);
//		if (call == null) {
//			Log.e("lilin error", "Could not place call to " + username);
//			return;
//		}
//	}
//	public void hangUp() {
//		LinphoneCall currentCall = mLinphoneCore.getCurrentCall();
//		if (currentCall != null) {
//			mLinphoneCore.terminateCall(currentCall);
//		} else if (mLinphoneCore.isInConference()) {
//			mLinphoneCore.terminateConference();
//		} else {
//			mLinphoneCore.terminateAllCalls();
//		}
//	}
//	public void lilin_jie() throws LinphoneCoreException  {
//		Log.i(TAG,"提示:lilin_jie: ");
//		//instance.getLC().setVideoPolicy(true, instance.getLC().getVideoAutoAcceptPolicy());/*设置初始话视频电话，设置了这个你拨号的时候就默认为使用视频发起通话了*/
//		//getLC().setVideoPolicy(getLC().getVideoAutoInitiatePolicy(), true);/*设置自动接听视频通话的请求，也就是说只要是视频通话来了，直接就接通，不用按键确定，这是我们的业务流，不用理会*/
//		/*这是允许视频通话，这个选了false就彻底不能接听或者拨打视频电话了*/
//			LinphoneCall currentCall = getLC().getCurrentCall();
//			if (currentCall != null) {
//				LinphoneCallParams params = getLC().createCallParams(currentCall);
//				LinphoneCallParams remoteParams = getLC().getCurrentCall().getRemoteParams();
//				if(  remoteParams != null && remoteParams.getVideoEnabled()){
//					Log.i(TAG, "lilin_jie: 支持视频通话");
//					params.setVideoEnabled( true );
//				}
//				boolean videoSupported = getLC().isVideoSupported();
//				if (videoSupported){
//					Log.i(TAG, "提示:lilin_qie_updatecall: 对方支持视频通话");
//				}else{
//					Log.i(TAG, "提示:lilin_qie_updatecall: 对方不支持视频通话");
//				}
//				getLC().acceptCallWithParams(currentCall,params);
//			}
//	}
//	public void lilin_jie(boolean isVideo) throws LinphoneCoreException {
//		Log.i(TAG, "lilin_jie: 2");
//			LinphoneCall currentCall = getLC().getCurrentCall();
//			if (currentCall!=null){
//				LinphoneCallParams params = getLC().createCallParams(currentCall);
//				LinphoneCallParams remoteParams = getLC().getCurrentCall().getRemoteParams();
//				if (isVideo){
//					if(  remoteParams != null && remoteParams.getVideoEnabled()){
//						Log.i(TAG, "lilin_jie: 设置支持视频通话");
//						params.setVideoEnabled( true );
//					}
//				}else{
//					Log.i(TAG, "lilin_jie: 设置不支持视频通话");
//					params.setVideoEnabled(false);
//				}
//				boolean videoSupported = getLC().isVideoSupported();
//				if (videoSupported){
//					Log.i(TAG, "提示:lilin_qie_updatecall: 对方支持视频通话");
//				}else{
//					Log.i(TAG, "提示:lilin_qie_updatecall: 对方不支持视频通话");
//				}
//				getLC().updateCall(currentCall,params);
//			}
//	}
//	public boolean lilin_qie_updatecall(){
//		Log.i(TAG, "lilin_qie_updatecall: ");
//		boolean isVideo = false;
//		LinphoneCall currentCall = mLinphoneCore.getCurrentCall();
//		if (currentCall!=null){
//			LinphoneCallParams params = getLC().createCallParams(currentCall);
//			LinphoneCallParams remoteParams = currentCall.getRemoteParams();
//			try {
//				Log.i(TAG, "lilin_qie_updatecall: 接受另一端发起的呼叫修改 ");
//				mLinphoneCore.acceptCallUpdate(currentCall,params);
//				if (remoteParams!=null){
//					isVideo =remoteParams.getVideoEnabled();
//					Log.i(TAG,"提示:lilin_qie_updatecall:获取另一端设置是否支持视频："+isVideo);
//				}else{
//					Log.i(TAG,"提示:lilin_qie_updatecall:获取另一端发起的呼叫修改为空");
//				}
//				boolean videoSupported = getLC().isVideoSupported();
//				if (videoSupported){
//					Log.i(TAG, "提示:lilin_qie_updatecall: 对方支持视频通话");
//				}else{
//					Log.i(TAG, "提示:lilin_qie_updatecall: 对方不支持视频通话");
//				}
//			}catch (LinphoneCoreException e){
//				e.printStackTrace();
//			}
//		}
//		return isVideo;
//	}
//	public boolean lilin_getVideoEnabled() {
//		LinphoneCall currentCall = mLinphoneCore.getCurrentCall();
//		if (currentCall!=null){
//			LinphoneCallParams remoteParams = currentCall.getRemoteParams();
//			return remoteParams != null && remoteParams.getVideoEnabled();
//		}
//		return false;
//	}
//	public void updateCall() {
//		LinphoneCall lCall = mLinphoneCore.getCurrentCall();
//		if (lCall == null) {
//			Log.e(TAG,"提示:Trying to updateCall while not in call: doing nothing");
//			return;
//		}
//		mLinphoneCore.updateCall(lCall,null);
//	}
//
//	public void lilin_qie(boolean speakerOn){
//		mLinphoneCore.enableSpeaker( speakerOn );
//	}
//
//	@Override
//	public IBinder onBind(Intent intent) {
//		return null;
//	}
//
//
//
//}
