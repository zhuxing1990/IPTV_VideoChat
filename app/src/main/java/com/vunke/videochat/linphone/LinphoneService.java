package com.vunke.videochat.linphone;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.vunke.videochat.R;
import com.vunke.videochat.config.BaseConfig;
import com.vunke.videochat.config.CallInfo;
import com.vunke.videochat.db.CallRecordTable;
import com.vunke.videochat.dialog.CallInDialog;
import com.vunke.videochat.tools.AccountBuilder;
import com.vunke.videochat.tools.LinphoneMiniUtils;
import com.vunke.videochat.ui.CallInActivity;

import org.linphone.core.AuthInfo;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Config;
import org.linphone.core.Core;
import org.linphone.core.CoreException;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.LogCollectionState;
import org.linphone.core.PayloadType;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;
import org.linphone.core.VideoActivationPolicy;
import org.linphone.core.VideoDefinition;
import org.linphone.core.tools.H264Helper;
import org.linphone.mediastream.Version;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class LinphoneService extends Service {
    private static final String TAG = "LinphoneService";
    private static final String START_LINPHONE_LOGS = " ==== Device information dump ====";
    // Keep a static reference to the Service so we can access it from anywhere in the app
    private static LinphoneService sInstance;

    private Handler mHandler;
    private Timer mTimer;

    private Core mCore;
    private CoreListenerStub mCoreListener;

    public static boolean isReady() {
        return sInstance != null;
    }

    public static LinphoneService getInstance() {
        return sInstance;
    }

    public static Core getCore() {
        return sInstance.mCore;
    }

    public Core getmCore() {
        return mCore;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
        // The first call to liblinphone SDK MUST BE to a Factory method
        // So let's enable the library debug logs & log collection
        String basePath = getFilesDir().getAbsolutePath();
        Factory.instance().setLogCollectionPath(basePath);
        Factory.instance().setDebugMode(true, "linphone");
        Factory.instance().enableLogCollection(LogCollectionState.Enabled);
        // Dump some useful information about the device we're running on
        Log.i(TAG,START_LINPHONE_LOGS);
        dumpDeviceInformation();
        dumpInstalledLinphoneInformation();

        mHandler = new Handler();
        // This will be our main Core listener, it will change activities depending on events
        initListener();

        try {
            // Let's copy some RAW resources to the device
            // The default config file must only be installed once (the first time)

//            // The factory config is used to override any other setting, let's copy it each time
		LinphoneMiniUtils.copyIfNotExist(this, R.raw.linphonerc_default, basePath + "/.linphonerc");
		LinphoneMiniUtils.copyFromPackage(this, R.raw.linphonerc_factory, new File(basePath + "/linphonerc").getName());
		LinphoneMiniUtils.copyIfNotExist(this, R.raw.lpconfig, basePath + "/lpconfig.xsd");
        LinphoneMiniUtils.copyFromPackage(this,R.raw.default_assistant_create,new File(basePath + "/default_assistant_create.rc").getName());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        // Create the Core and add our listener
        mCore = Factory.instance().createCore(basePath + "/.linphonerc", basePath + "/linphonerc", this);
        mCore.addListener(mCoreListener);
        Log.i(TAG, "onCreate: Linphone Code VersionCode:"+mCore.getVersion());
        initConfig();
        // Core is ready to be configured
        configureCore();
//        setPreferredVideoSize("cif");
        mCore.setVideoPreset("default");
        mCore.setPreferredFramerate(0);
        mCore.setUploadBandwidth(0);
        mCore.setDownloadBandwidth(0);
        setPreferredVideoSize("vga");
        setInitiateVideoCall(true);
        setAutomaticallyAcceptVideoRequests(true);
        H264Helper.setH264Mode(H264Helper.MODE_AUTO, mCore);
//        Toast.makeText(this, "MODE_MEDIA_CODEC", Toast.LENGTH_SHORT).show();
//        H264Helper.setH264Mode(H264Helper.MODE_MEDIA_CODEC, mCore);
    }
    public Config initConfig() {
        String basePath = getFilesDir().getAbsolutePath();
        if (null==mCore){
            File linphonerc = new File(basePath + "/.linphonerc");
            if (linphonerc.exists()) {
                Log.i(TAG, "initConfig: file existsstart create confirg");
                return Factory.instance().createConfig(linphonerc.getAbsolutePath());
            } else {
                InputStream inputStream =getResources().openRawResource(R.raw.linphonerc_default);
                InputStreamReader inputreader = new InputStreamReader(inputStream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                StringBuilder text = new StringBuilder();
                String line;
                try {
                    while ((line = buffreader.readLine()) != null) {
                        text.append(line);
                        text.append('\n');
                    }
                } catch (IOException ioe) {
                    org.linphone.core.tools.Log.e(ioe);
                }
                try {
                    if (inputStream!=null){
                        inputStream.close();
                    }
                    if (inputreader!=null){
                        inputreader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "initConfig: start create config from String");
                return Factory.instance().createConfigFromString(text.toString());
            }
        } else {
            Log.i(TAG, "initConfig: instance not ready, start create config");
            return Factory.instance().createConfig(basePath+"/.linphonerc");
        }
//        return null;
    }
    private boolean isLogin = false;

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }
    private void initListener() {
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {
                Log.i(TAG, "onCallStateChanged: message:"+message);
                Log.i(TAG, "onCallStateChanged: state:"+state);
//                Toast.makeText(LinphoneService.this, message, Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(BaseConfig.INSTANCE.getRECEIVE_MAIN_ACTIVITY());
                intent1.putExtra("action", "show_status");intent1.putExtra("data", message );
                sendBroadcast( intent1);
                if (state == Call.State.IncomingReceived) {
                        boolean mangguoPlayer = isMangguoPlayer();
                        if (mangguoPlayer){
                            showCallInWindow(message);
                        }else{
                            Intent intent = new Intent(LinphoneService.this, CallInActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("message",message);
                            intent.putExtra(CallRecordTable.INSTANCE.getCALL_STATUS(), CallInfo.INSTANCE.getCALL_IN());
                            startActivity(intent);
                        }
//                    Toast.makeText(LinphoneService.this, "Incoming call received, answering it automatically", Toast.LENGTH_LONG).show();
//                    // For this sample we will automatically answer incoming calls
//                    CallParams params = getCore().createCallParams(call);
//                    params.enableVideo(true);
//                    call.acceptWithParams(params);
                } else if (state == Call.State.Connected) {
                    Log.i(TAG, "onCallStateChanged: ");
                    // This stats means the call has been established, let's start the call activity
//                    Intent intent = new Intent(LinphoneService.this, CallActivity.class);
                    // As it is the Service that is starting the activity, we have to give this flag
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.putExtra("message",message);
//                    intent.putExtra(CallRecordTable.INSTANCE.getCALL_STATUS(), CallInfo.INSTANCE.getCALL_IN());
//                    startActivity(intent);
                }else  if (state == Call.State.End){
                    Intent intent = new Intent(BaseConfig.INSTANCE.getRECEIVE_VIDEO_ACTIVITY());
                    intent.putExtra("action", "end");
                    sendBroadcast( intent);
                    dismissDialog();
                }else if (state == Call.State.Released){
                    Intent intent = new Intent(BaseConfig.INSTANCE.getRECEIVE_VIDEO_ACTIVITY());
                    intent.putExtra("action", "end");
                    sendBroadcast( intent);
                    dismissDialog();
                }else if (state == Call.State.UpdatedByRemote){

                }
            }

            @Override
            public void onRegistrationStateChanged(Core lc, ProxyConfig cfg, RegistrationState cstate, String smessage) {
                super.onRegistrationStateChanged(lc, cfg, cstate, smessage);
                Intent intent = new Intent(BaseConfig.INSTANCE.getRECEIVE_MAIN_ACTIVITY());
                intent.putExtra("action", "reg_state");intent.putExtra("data",smessage );
                sendBroadcast( intent);
                switch (cstate) {
                    case Ok: // This state means you are connected, to can make and receive calls & messages
                        isLogin = true;
                        StringBuffer stringBuffer = new StringBuffer();
                        PayloadType[] audioPayloadTypes = mCore.getAudioPayloadTypes();
                        for (PayloadType payloadType:audioPayloadTypes) {
                            String mimeType = payloadType.getMimeType();
                            int clockRate = payloadType.getClockRate();
                            boolean enabled = payloadType.enabled();
                            stringBuffer.append("\n audio mimeType:"+mimeType);
                            stringBuffer.append("\n audio clockRate:"+clockRate);
                            stringBuffer.append("\n audio enabled:"+enabled);
                        }
                        PayloadType[] videoPayloadTypes = mCore.getVideoPayloadTypes();
                        for (PayloadType payloadType:videoPayloadTypes) {
                            String mimeType = payloadType.getMimeType();
                            int clockRate = payloadType.getClockRate();
                            boolean enabled = payloadType.enabled();
                            stringBuffer.append("\n video mimeType:"+mimeType);
                            stringBuffer.append("\n video clockRate:"+clockRate);
                            stringBuffer.append("\n video enabled:"+enabled);
                        }
                        Log.i(TAG, "onRegistrationStateChanged: PayloadType:"+stringBuffer.toString());
                        break;
                    case None: // This state is the default state
                    case Cleared: // This state is when you disconnected
                        isLogin = false;
                        break;
                    case Failed: // This one means an error happened, for example a bad password
                        isLogin = false;
                        break;
                    case Progress: // Connection is in progress, next state will be either Ok or Failed
                        isLogin = false;
                        break;
                    default:
                        isLogin = false;
                        break;
                }
            }
        };
    }
    private void dismissDialog() {
        if (callInDialog!=null){
            callInDialog.dismiss();
        }
    }
    private CallInDialog callInDialog;
    private void showCallInWindow(final String message) {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext(message);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        callInDialog = new CallInDialog(getApplicationContext(), sInstance, s)
                                .builder();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        dispose();
                    }

                    @Override
                    public void onComplete() {
                        dispose();
                    }
                });

    }

    private boolean isMangguoPlayer() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        String  currentClassName =manager.getRunningTasks(1).get(0).topActivity.getClassName();
        Log.i(TAG, "isMangguoPlayer: get top activity:"+currentClassName);
        if (!currentClassName.contains("com.vunke.videochat")) {
            Log.i(TAG, "isMangguoPlayer: mang guo apk is playing video ");
            return true;
        }else {
            return false;
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // If our Service is already running, no need to continue
        if (sInstance != null) {
            return START_STICKY;
        }

        // Our Service has been started, we can keep our reference on it
        // From now one the Launcher will be able to call onServiceReady()
        sInstance = this;

        // Core must be started after being created and configured
        mCore.start();
        // We also MUST call the iterate() method of the Core on a regular basis
        TimerTask lTask =
                new TimerTask() {
                    @Override
                    public void run() {
                        mHandler.post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCore != null) {
                                            mCore.iterate();
                                        }
                                    }
                                });
                    }
                };
        mTimer = new Timer("Linphone scheduler");
        mTimer.schedule(lTask, 0, 20);
        Factory.instance().enableLogCollection(LogCollectionState.Enabled);
        Factory.instance().setDebugMode(true, "linphone");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mCore.removeListener(mCoreListener);
        mTimer.cancel();
        mCore.stop();
        // A stopped Core can be started again
        // To ensure resources are freed, we must ensure it will be garbage collected
        mCore = null;
        // Don't forget to free the singleton as well
        sInstance = null;

        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // For this sample we will kill the Service at the same time we kill the app
        stopSelf();

        super.onTaskRemoved(rootIntent);
    }

    private void configureCore() {
        // We will create a directory for user signed certificates if needed
        String basePath = getFilesDir().getAbsolutePath();
        String userCerts = basePath + "/user-certs";
        File f = new File(userCerts);
        if (!f.exists()) {
            if (!f.mkdir()) {
                Log.e(TAG,userCerts + " can't be created.");
            }
        }
        mCore.setUserCertificatesPath(userCerts);
    }

    private void dumpDeviceInformation() {
        StringBuilder sb = new StringBuilder();
        sb.append("DEVICE=").append(Build.DEVICE).append("\n");
        sb.append("MODEL=").append(Build.MODEL).append("\n");
        sb.append("MANUFACTURER=").append(Build.MANUFACTURER).append("\n");
        sb.append("SDK=").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("Supported ABIs=");
        for (String abi : Version.getCpuAbis()) {
            sb.append(abi).append(", ");
        }
        sb.append("\n");
        Log.i(TAG,sb.toString());
    }

    private void dumpInstalledLinphoneInformation() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException nnfe) {
            nnfe.printStackTrace();
        }

        if (info != null) {
            Log.i(TAG,"[Service] Linphone version is "+info.versionName + " (" + info.versionCode + ")");
        } else {
            Log.i(TAG,"[Service] Linphone version is unknown");
        }
    }


    public void hangUp() {
        if (mCore.getCallsNb() > 0) {
            Call call = mCore.getCurrentCall();
            if (call == null) {
                // Current call can be null if paused for example
                call = mCore.getCalls()[0];
            }
            call.terminate();
        }
    }

    	/**
	 * 再次打开不用注册了 会启用原来注册的
	 * @param
	 * @param password
	 * @throws CoreException
	 */
	public void lilin_reg(String domain, String username, String password , String port, TransportType ttype) throws CoreException {

		//
		for (ProxyConfig linphoneProxyConfig : mCore.getProxyConfigList()) {
            mCore.removeProxyConfig( linphoneProxyConfig);
			Log.e("removeProxyConfig", "lilin_reg:  remove proxy config ");
		}
		for( AuthInfo a: mCore.getAuthInfoList()){
			Log.e("removeAuthInfo", "lilin_reg:  remove auth info ");
            mCore.removeAuthInfo( a);
		}
        AccountBuilder builder = new AccountBuilder(mCore)
                .setUsername(username)
                .setDomain(domain+":"+port)
                .setHa1(null)
                .setUserId(username)
                .setDisplayName("")
                .setPassword(password);
		String forcedProxy = "";
//		String forcedProxy = "10.255.25.48:5060";
		if (!TextUtils.isEmpty(forcedProxy)) {
			builder.setProxy(forcedProxy)
					.setOutboundProxyEnabled(true)
					.setAvpfRRInterval(5);
		}

		builder.setTransport(ttype);
        builder.saveNewAccount(Factory.instance());



//		//LinphoneAddress proxyAddr = lcFactory.createLinphoneAddress("sip:"+username+"@"+domain);
//		//proxyAddr.setTransport(LinphoneAddress.TransportType.LinphoneTransportUdp );
//
//		mLinphoneCore.addAuthInfo(lcFactory.createAuthInfo(username, password,null, domain+":"+port));
//		// create proxy config
//		Log.e(">ddd>"+domain+":"+port);
//		LinphoneProxyConfig proxyCfg = mLinphoneCore.createProxyConfig("sip:"+username+"@"+domain+":"+port,   domain+":"+port, null, true);
//
//		//proxyCfg.enablePublish(false);
//		//proxyCfg.setExpires(10000);
//
//		mLinphoneCore.addProxyConfig(proxyCfg); // add it to linphone
//		mLinphoneCore.setDefaultProxyConfig(proxyCfg);


	}
    public void lilin_jie() throws CoreException  {
		Log.i(TAG,"提示:lilin_jie: ");
		//instance.getLC().setVideoPolicy(true, instance.getLC().getVideoAutoAcceptPolicy());/*设置初始话视频电话，设置了这个你拨号的时候就默认为使用视频发起通话了*/
		//getLC().setVideoPolicy(getLC().getVideoAutoInitiatePolicy(), true);/*设置自动接听视频通话的请求，也就是说只要是视频通话来了，直接就接通，不用按键确定，这是我们的业务流，不用理会*/
		/*这是允许视频通话，这个选了false就彻底不能接听或者拨打视频电话了*/
            Call currentCall = mCore.getCurrentCall();
			if (currentCall != null) {
				CallParams params = mCore.createCallParams(currentCall);
				CallParams remoteParams = mCore.getCurrentCall().getRemoteParams();
				if(  remoteParams != null && remoteParams.videoEnabled()){
					Log.i(TAG, "lilin_jie: 支持视频通话");
					params.enableVideo( true );
				}
				boolean videoSupported = params.videoEnabled();
				if (videoSupported){
					Log.i(TAG, "提示:lilin_qie_updatecall: 对方支持视频通话");
				}else{
					Log.i(TAG, "提示:lilin_qie_updatecall: 对方不支持视频通话");
				}
                currentCall.acceptWithParams(params);
			}
	}

	public void lilin_jie(boolean isVideo) throws CoreException {
		Log.i(TAG, "lilin_jie: 2");
        Call currentCall = mCore.getCurrentCall();
			if (currentCall!=null){
                CallParams params = mCore.createCallParams(currentCall);
                CallParams remoteParams = mCore.getCurrentCall().getRemoteParams();
				if (isVideo){
					if(  remoteParams != null && remoteParams.videoEnabled()){
						Log.i(TAG, "lilin_jie: 设置支持视频通话");
						params.enableVideo(true);
					}
				}else{
					Log.i(TAG, "lilin_jie: 设置不支持视频通话");
					params.enableVideo(false);
				}
                boolean videoSupported = params.videoEnabled();
				if (videoSupported){
					Log.i(TAG, "提示:lilin_qie_updatecall: 对方支持视频通话");
				}else{
					Log.i(TAG, "提示:lilin_qie_updatecall: 对方不支持视频通话");
				}
                currentCall.acceptUpdate(params);
//				getLC().updateCall(currentCall,params);
			}
	}

    	public boolean lilin_getVideoEnabled() throws CoreException{
            Call currentCall = mCore.getCurrentCall();
		if (currentCall!=null){
            CallParams remoteParams = mCore.getCurrentCall().getRemoteParams();
			return remoteParams != null && remoteParams.videoEnabled();
		}
		return false;
	}

    public void acceptCallUpdate(boolean accept) {
        Call call = mCore.getCurrentCall();
        if (call == null) {
            return;
        }

        CallParams params = mCore.createCallParams(call);
        if (accept) {
            params.enableVideo(true);
            mCore.enableVideoCapture(true);
            mCore.enableVideoDisplay(true);
        }

        call.acceptUpdate(params);
    }
    public void setPreferredVideoSize(String preferredVideoSize) {
        if (mCore == null) return;
        VideoDefinition preferredVideoDefinition =
                Factory.instance().createVideoDefinitionFromName(preferredVideoSize);
        mCore.setPreferredVideoDefinition(preferredVideoDefinition);
    }

    public void setInitiateVideoCall(boolean initiate) {
        if (mCore == null) return;
        VideoActivationPolicy vap = mCore.getVideoActivationPolicy();
        vap.setAutomaticallyInitiate(initiate);
        mCore.setVideoActivationPolicy(vap);
    }
    public void setAutomaticallyAcceptVideoRequests(boolean accept) {
        if (mCore == null) return;
        VideoActivationPolicy vap = mCore.getVideoActivationPolicy();
        vap.setAutomaticallyAccept(accept);
        mCore.setVideoActivationPolicy(vap);
    }

}
