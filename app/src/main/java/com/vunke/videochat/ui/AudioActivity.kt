package com.vunke.videochat.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Camera
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.vunke.videochat.R
import com.vunke.videochat.base.BaseConfig
import com.vunke.videochat.callback.TalkCallBack
import com.vunke.videochat.config.CallInfo
import com.vunke.videochat.dao.ContactsDao
import com.vunke.videochat.db.CallRecord
import com.vunke.videochat.db.CallRecordTable
import com.vunke.videochat.dialog.NotCameraDialog
import com.vunke.videochat.linphone.LinphoneService
import com.vunke.videochat.login.UserInfoUtil
import com.vunke.videochat.manage.BackgroundManage
import com.vunke.videochat.manage.TalkManage
import com.vunke.videochat.model.TalkBean
import com.vunke.videochat.tools.CallRecordUtil
import com.vunke.videochat.tools.FocusUtil
import com.vunke.videochat.tools.TimeUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_audio.*
import kotlinx.android.synthetic.main.activity_call_in.*
import org.linphone.core.Call
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by zhuxi on 2019/11/20.
 */
class AudioActivity:AppCompatActivity(), View.OnClickListener{
    private val TAG = "AudioActivity"
    var instance:LinphoneService?=null
    var message:String=""
    private var mReceiver: MainActivityReceiver? = null
    var  firstCallTime :Long?=0
    var dialog:NotCameraDialog?=null
    var callRecord: CallRecord = CallRecord()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)
        instance = LinphoneService.getInstance()
        initData()
        registerBroad()
        initLinstener()
        val numberOfCameras = Camera.getNumberOfCameras()
        Log.i(TAG, "get camera number:" + numberOfCameras)
        if (numberOfCameras == 0) {
//            NotCameraDialog(this).Builder().show()
            dialog = NotCameraDialog(this).Builder(this)
            dialog!!.show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume: ")
        val rootView = findViewById<View>(android.R.id.content)
        BackgroundManage.setBackground(this, rootView)
    }
    var timeOb:DisposableObserver<Long>?=null
    private fun startCallTimeTask() {
        timeOb = object :DisposableObserver<Long>(){
            override fun onComplete() {
                dispose()
            }

            override fun onNext(t: Long) {
                var callTime = System.currentTimeMillis() - firstCallTime!!
                var getTime = getDateTimes(callTime)
                audio_calltime.setText(getTime)
            }

            override fun onError(e: Throwable) {
                dispose()
            }

        }
        Observable.interval(0,1,TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(timeOb!!)

    }

    /**
     * long 转时间
     * @param time
     * @back
     */
    fun getDateTimes(time: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss")
        sdf.setTimeZone(TimeZone.getTimeZone("GMT00:00"))
        return sdf.format(Date(time))
    }
    private fun stopCallTimeTask() {
        if (timeOb!=null){
            timeOb!!.dispose()
            timeOb=null
        }
    }
    private fun registerBroad() {
        mReceiver = MainActivityReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(BaseConfig.RECEIVE_UPB_CHANGE)
        intentFilter.addAction(BaseConfig.RECEIVE_MAIN_ACTIVITY)
        registerReceiver(mReceiver, intentFilter)
    }
    private fun initData() {
        Log.i(TAG, "initData: ")
        var intent = intent
        if (intent.hasExtra("message")){
            message = intent.getStringExtra("message")
            Log.i(TAG,"message:$message")
            try {
                if (!TextUtils.isEmpty(message)){
                    if (message.contains("tel:")){
                        val data = message.split("tel:")
//                        for (i in data.indices) {
//                            println(data[i])
//                        }
                        val number = data[1].substring(0, data[1].indexOf(";"))
                        audio_phone.setText(number)
                        callRecord.call_phone = number
                        callRecord.call_name = number
                        var contactsList = ContactsDao.getInstance(this).queryPhone(number)
                        if (contactsList!=null&&contactsList.size!=0){
                            callRecord.call_name = contactsList.get(0).user_name
                            audio_phone.setText(callRecord.call_name)
                        }
                    }else{
                        val currentCall = instance!!.getmCore().currentCall
                        if (currentCall != null) {
                            Log.i(TAG, "initData: 0")
                            initDisplayName(currentCall)
                        } else {
                            for (call in LinphoneService.getCore().calls) {
                                if (call != null && call.conference != null) {
                                    if (LinphoneService.getCore().isInConference) {
                                        Log.i(TAG, "initData: 1")
                                        initDisplayName(currentCall!!)
                                    }
                                } else if (call != null && call !== currentCall) {
                                    val state = call.state
                                    if (state == Call.State.Paused || state == Call.State.PausedByRemote || state == Call.State.Pausing) {
                                        Log.i(TAG, "initData: 2")
                                        initDisplayName(currentCall!!)
                                    }
                                }
                            }
                        }
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }else if (intent.hasExtra("number")){
            var number = intent.getStringExtra("number")
            audio_phone.setText(number)
            callRecord.call_phone = number
            callRecord.call_name = number
            var contactsList = ContactsDao.getInstance(this).queryPhone(number)
            if (contactsList!=null&&contactsList.size!=0){
                callRecord.call_name = contactsList.get(0).user_name
                audio_phone.setText(callRecord.call_name)
            }
        }
        if(intent.hasExtra(CallRecordTable.CALL_STATUS)){
            var call_status = intent.getStringExtra(CallRecordTable.CALL_STATUS)
            if (!TextUtils.isEmpty(call_status)){
                callRecord.call_status = call_status
                firstCallTime = System.currentTimeMillis()
                startCallTimeTask()
            }else{
                callRecord.call_status = CallInfo.CALL_OUT
            }
        }
    }
    private fun initDisplayName(currentCall: Call) {
        val remoteAddress = currentCall.remoteAddress
        if (remoteAddress != null) {
            var displayName = remoteAddress.displayName
            if (TextUtils.isEmpty(displayName)) {
                displayName = remoteAddress.username
                Log.i(TAG, "initDisplayName: getUsername:$displayName")
            }
            if (TextUtils.isEmpty(displayName)) {
                Log.i(TAG, "initDisplayName: asStringUriOnly:$displayName")
                displayName = remoteAddress.asStringUriOnly()
            }
            callRecord.call_phone = displayName
            callRecord.call_name = displayName
            if (!TextUtils.isEmpty(displayName)){
                audio_in_phone.setText(displayName)
            }
            try {
                if (displayName.contains("tel:")){
                    val data = displayName.split("tel:")
                    val number = data[1].substring(0, data[1].indexOf(";"))
                    if (!TextUtils.isEmpty(number)){
                        audio_in_phone.setText(number)
                    }
                    callRecord.call_name = number
                    callRecord.call_phone = number
                    var contactsList = ContactsDao.getInstance(this).queryPhone(number)
                    if (contactsList!=null&&contactsList.size!=0){
                        callRecord.call_name = contactsList.get(0).user_name
                        audio_phone.setText(callRecord.call_name)
                    }
                }else{
                    callRecord.call_name = displayName
                    callRecord.call_phone = ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            audio_phone.setText("Anonymous")
            callRecord.call_name = "Anonymous"
            callRecord.call_phone = ""
        }
    }
    private fun initLinstener(){
        audio_hang_up.setOnClickListener(this)
        audio_mute.setOnClickListener(this)
        audio_hang_up.setOnFocusChangeListener(object :View.OnFocusChangeListener{
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                FocusUtil.setFocus(hasFocus,v,applicationContext)
            }
        })
        audio_mute.setOnFocusChangeListener(object :View.OnFocusChangeListener{
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                FocusUtil.setFocus(hasFocus,v,applicationContext)
            }
        })
        audio_hang_up.requestFocus()
    }

    private var isMute = false
    override fun onClick(v: View) {
        when(v.id){
            R.id.audio_hang_up->{
//                Toast.makeText(this,"挂断", Toast.LENGTH_SHORT).show()
                if (!callRecord.call_status.equals(CallInfo.CALL_IN)) {
                    callRecord.call_status = CallInfo.CALL_OUT
                }
                instance!!.hangUp()
                finish()
            }
            R.id.audio_mute->{
                isMute =!isMute
                var lc = instance!!.getmCore()
                lc.enableMic(!isMute)
                if (isMute){
                    Toast.makeText(this,"已静音",Toast.LENGTH_SHORT).show()
                    audio_mute.setBackgroundResource(R.mipmap.mute2)
                }else{
                    Toast.makeText(this,"已恢复",Toast.LENGTH_SHORT).show()
                    audio_mute.setBackgroundResource(R.mipmap.mute)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCallTimeTask()
        unregisterReceiver(mReceiver)
        callRecord.call_time = System.currentTimeMillis().toString()
//        CallRecordDao.getInstance(this).insertData(callRecord)
        CallRecordUtil.updateCallRecord(this,callRecord)
        if ( null!= dialog  && dialog!!.isShow()){
            dialog!!.cancel()
        }
        var userInfoUtil = UserInfoUtil.getInstance(this)
        val userId= userInfoUtil.getUserId()
        var talkbean = TalkBean()
        talkbean.userId=userId
        talkbean.talkDuration= (System.currentTimeMillis()-firstCallTime!!)/1000
        talkbean.call_phone = callRecord.call_phone
        talkbean.call_status = callRecord.call_status
        talkbean.talkTime = TimeUtil.getDateTime(TimeUtil.dateFormatYMDHMS,firstCallTime!!)
        TalkManage.addConversationLog(talkbean,object:TalkCallBack{
            override fun onSuccess() {

            }

            override fun OnFailed() {

            }
        })
    }

    inner class MainActivityReceiver : BroadcastReceiver() {
        private val TAG = "LoginReceiver"
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.getStringExtra("action")
            if (!TextUtils.isEmpty(action)) {
                Log.i(TAG, "onReceive: get action:" + action)
                when (action) {
                    "reg_state" ->{
                        Log.i(TAG, "onReceive: reg_state:" + intent.getStringExtra("data"))

                    }
                    "show_code" -> {
                        Log.i(TAG, "onReceive  show_code: " + intent.getStringExtra("data"))
                    }
                    "show_version" ->{
                        Log.i(TAG, "onReceive: show_version:" + intent.getStringExtra("data"))
                    }
                    "show_status" -> {
                        Log.i(TAG, "onReceive: show_status:" + intent.getStringExtra("data"))
                        var data = intent.getStringExtra("data")
                        if (data.contains("Call terminated")){
//                            callRecord.call_status = CallInfo.CALL_RINGING
                            finish()
                        }else if (data.contains("You have missed 1 call.")){
                            if (callRecord.call_status!=CallInfo.CALL_IN){
                                callRecord.call_status = CallInfo.CALL_MISSED
                            }
                            finish()
                        }else if (data.contains("Request timeout.")){
                            if (callRecord.call_status!=CallInfo.CALL_IN) {
                                callRecord.call_status = CallInfo.CALL_MISSED
                            }
                            finish()
                        }else if(data.contains("Call released")){
//                            callRecord.call_status = CallInfo.CALL_ANSWER
                            finish()
                        }else if (data.equals("Connected")){
                            firstCallTime = System.currentTimeMillis()
                            startCallTimeTask();
                        }
                    }"usb_change" -> {
                    val status = intent.getStringExtra("status")
                    val numberOfCameras = Camera.getNumberOfCameras()
                    if(!TextUtils.isEmpty(status)){
                        when(status){
                            Intent.ACTION_MEDIA_CHECKING ->{
                                Log.e(TAG, "ACTION_MEDIA_CHECKING")
                            }
                            Intent.ACTION_MEDIA_MOUNTED ->{
                                Log.e(TAG, "ACTION_MEDIA_MOUNTED")
                                if (numberOfCameras > 0) {

                                }
                            }
                            Intent.ACTION_MEDIA_UNMOUNTED ->{
                                Log.e(TAG, "ACTION_MEDIA_UNMOUNTED")
                                Log.i(TAG, "get camera number:" + numberOfCameras)

                            }
                            UsbManager.ACTION_USB_DEVICE_DETACHED ->{
                                Log.e(TAG, "ACTION_USB_DEVICE_DETACHED")
                                if (numberOfCameras == 0) {
//                                        NotCameraDialog(context).Builder().show()
                                    dialog = NotCameraDialog(context).Builder(context)
                                    dialog!!.show()
                                }
                            }
                            UsbManager.ACTION_USB_DEVICE_ATTACHED ->{
                                Log.e(TAG, "ACTION_USB_DEVICE_ATTACHED")
                                val device_add = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                                if (device_add != null) {
                                    if (numberOfCameras > 0) {

                                    }
                                }
                            }
                            Intent.ACTION_MEDIA_EJECT ->{
                                Log.e(TAG, "ACTION_MEDIA_EJECT")
                            }else ->{

                        }
                        }
                    }
                }else -> {

                }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return false
        }
        return super.onKeyDown(keyCode, event)
    }
}