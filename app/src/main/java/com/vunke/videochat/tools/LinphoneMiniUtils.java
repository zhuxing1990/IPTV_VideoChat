package com.vunke.videochat.tools;

/*
LinphoneMiniUtils.java
Copyright (C) 2017  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.github.dfqin.grantor.PermissionListener;
import com.github.dfqin.grantor.PermissionsUtil;
import com.vunke.videochat.linphone.LinphoneService;

import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.EcCalibratorStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.media.AudioManager.STREAM_VOICE_CALL;

public class LinphoneMiniUtils {
	public static void copyIfNotExist(Context context, int ressourceId, String target) throws IOException {
		File lFileToCopy = new File(target);
		if (!lFileToCopy.exists()) {
			copyFromPackage(context, ressourceId, lFileToCopy.getName());
		}
	}

	public static void copyFromPackage(Context context, int ressourceId, String target) throws IOException {
		FileOutputStream lOutputStream = context.openFileOutput (target, 0);
		InputStream lInputStream = context.getResources().openRawResource(ressourceId);
		int readByte;
		byte[] buff = new byte[8048];
		while (( readByte = lInputStream.read(buff)) != -1) {
			lOutputStream.write(buff,0, readByte);
		}
		lOutputStream.flush();
		lOutputStream.close();
		lInputStream.close();
	}

	public static void initEchoCancellation(final Context context) {
		Log.i("提示", "initEchoCancellation: ");
		if (LinphoneService.isReady()){
			//回声消除
			LinphoneService.getInstance().getCore()
					.addListener(
							new CoreListenerStub() {
								@Override
								public void onEcCalibrationResult(
										Core core, EcCalibratorStatus status, int delayMs) {
									if (status == EcCalibratorStatus.InProgress) return;
									core.removeListener(this);
									AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
									mAudioManager.setSpeakerphoneOn(false);
									mAudioManager.setMode(AudioManager.MODE_NORMAL);
								}
							});
			startEcCalibration(context);
		}
	}

	private static boolean mAudioFocused = false;
	public static void startEcCalibration(Context context) {
		Core core = LinphoneService.getCore();
		if (core == null) {
			return;
		}
		AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setSpeakerphoneOn(true);
		if (mAudioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
			org.linphone.core.tools.Log.w("[Audio Manager] already in MODE_IN_COMMUNICATION, skipping...");
			return;
		}
		org.linphone.core.tools.Log.d("[Audio Manager] Mode: MODE_IN_COMMUNICATION");

		mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		org.linphone.core.tools.Log.i("[Audio Manager] Set audio mode on 'Voice Communication'");
		if (!mAudioFocused) {
			int res =
					mAudioManager.requestAudioFocus(
							null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
			org.linphone.core.tools.Log.d(
					"[Audio Manager] Audio focus requested: "
							+ (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
							? "Granted"
							: "Denied"));
			if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true;
		}
		int oldVolume = mAudioManager.getStreamVolume(STREAM_VOICE_CALL);
		int maxVolume = mAudioManager.getStreamMaxVolume(STREAM_VOICE_CALL);
		mAudioManager.setStreamVolume(STREAM_VOICE_CALL, maxVolume, 0);
		core.startEchoCancellerCalibration();
		mAudioManager.setStreamVolume(STREAM_VOICE_CALL, oldVolume, 0);
	}

	public static void initLinphoneService(final Context context) {
		if (PermissionsUtil.hasPermission(context, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA})) {
			//有
			if (!LinphoneService.isReady()){
				Intent intentOne = new Intent(context, LinphoneService.class);
				context.startService(intentOne);
			}
		} else {
			PermissionsUtil.requestPermission(context, new PermissionListener() {

				public void permissionGranted(@NonNull String[] permissions) {
					//用户授予了
					Intent intentOne = new Intent(context, LinphoneService.class);
					context.startService(intentOne);
				}

				public void permissionDenied(@NonNull String[] permissions) {
					//用户拒绝了访问摄像头的申请
					Toast.makeText(context, "您没有授权将无法启用网络电话!", Toast.LENGTH_LONG).show();
				}
			}, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA});
		}
	}
}
