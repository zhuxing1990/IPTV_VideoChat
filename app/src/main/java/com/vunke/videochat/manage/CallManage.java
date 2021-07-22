package com.vunke.videochat.manage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.vunke.videochat.config.BaseConfig;
import com.vunke.videochat.config.CallInfo;
import com.vunke.videochat.db.CallRecordTable;
import com.vunke.videochat.linphone.LinphoneService;
import com.vunke.videochat.ui.AudioActivity;
import com.vunke.videochat.ui.VideoActivity;

import org.linphone.core.Address;
import org.linphone.core.CallParams;

/**
 * Created by zhuxi on 2019/11/20.
 */

public class CallManage {
    private static final String TAG = "CallManage";
    public static  void CallVideo(Activity activity, String str) {
        try {
            LinphoneService instance = LinphoneService.getInstance();
            Log.i(TAG, "CallVideo: "+str);
            Address addressToCall = instance.getCore().interpretUrl(str+ "@" +  BaseConfig.INSTANCE.getIpaddr());
            CallParams params = instance.getCore().createCallParams(null);
//            boolean highBandwidthConnection = isHighBandwidthConnection(activity);
//            if (highBandwidthConnection){
//                params.enableLowBandwidth(true);
//            }
            params.enableVideo(true);
            if (addressToCall != null) {
                instance.getCore().inviteAddressWithParams(addressToCall, params);
            }
//            instance.lilin_call(BaseConfig.INSTANCE.getNINE()+str,BaseConfig.INSTANCE.getIpaddr(),true);
            Intent intent =new Intent(activity, VideoActivity.class);
            intent.putExtra("number",str);
            intent.putExtra(CallRecordTable.INSTANCE.getCALL_STATUS(), CallInfo.INSTANCE.getCALL_OUT());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e1) {
            org.linphone.mediastream.Log.e(TAG, e1.getMessage());
        }
    }
    public static  void CallAudio(Activity activity, String str) {
        try {
            LinphoneService instance = LinphoneService.getInstance();
            Log.i(TAG, "CallVideo: "+str);
            Address addressToCall = instance.getCore().interpretUrl(str+ "@" + BaseConfig.INSTANCE.getIpaddr());
            CallParams params = instance.getCore().createCallParams(null);
//            boolean highBandwidthConnection = isHighBandwidthConnection(activity);
//            if (highBandwidthConnection){
//                params.enableLowBandwidth(true);
//            }
            params.enableVideo(false);
            if (addressToCall != null) {
                instance.getCore().inviteAddressWithParams(addressToCall, params);
            }
//            instance.lilin_call(BaseConfig.INSTANCE.getNINE()+str,BaseConfig.INSTANCE.getIpaddr(),true);
            Intent intent =new Intent(activity, AudioActivity.class);
            intent.putExtra("number",str);
            intent.putExtra(CallRecordTable.INSTANCE.getCALL_STATUS(), CallInfo.INSTANCE.getCALL_OUT());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e1) {
            org.linphone.mediastream.Log.e(TAG, e1.getMessage());
        }
    }
    public static boolean isHighBandwidthConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null
                && info.isConnected()
                && isConnectionFast(info.getType(), info.getSubtype()));
    }
    private static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return false;
            }
        }
        // in doubt, assume connection is good.
        return true;
    }
}
