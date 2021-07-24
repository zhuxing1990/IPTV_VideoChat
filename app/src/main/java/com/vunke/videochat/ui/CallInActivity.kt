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
import com.vunke.videochat.tools.AudioUtil
import com.vunke.videochat.tools.CallRecordUtil
import com.vunke.videochat.tools.FocusUtil
import com.vunke.videochat.tools.TimeUtil
import kotlinx.android.synthetic.main.activity_audio.*
import kotlinx.android.synthetic.main.activity_call_in.*
import org.linphone.core.Call

/**
 * Created by zhuxi on 2019/11/20.
 */
class  CallInActivity :AppCompatActivity(), View.OnClickListener{
    var TAG = "CallInActivity"
    var instance: LinphoneService?=null
    var  firstCallTime :Long?=0
    var message:String=""
    var callRecord:CallRecord = CallRecord()
    private var mReceiver: MainActivityReceiver? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_in)
        firstCallTime = System.currentTimeMillis()
        initLinstener()
        instance = LinphoneService.getInstance()
        initData()
        registerBroad()
        call_in_answer.requestFocus()
    }
    var dialog:NotCameraDialog?=null
    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume: ")
        val rootView = findViewById<View>(android.R.id.content)
        BackgroundManage.setBackground(this, rootView)

    }
    private fun initLinstener() {
        call_in_hang_up.setOnClickListener(this)
        call_in_answer.setOnClickListener(this)
        call_in_hang_up.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                FocusUtil.setFocus(hasFocus, v, applicationContext)
            }
        })
        call_in_answer.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                FocusUtil.setFocus(hasFocus, v, applicationContext)
            }
        })
    }

    private fun registerBroad() {
        mReceiver = MainActivityReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(BaseConfig.RECEIVE_UPB_CHANGE)
        intentFilter.addAction(BaseConfig.RECEIVE_MAIN_ACTIVITY)
        registerReceiver(mReceiver, intentFilter)
    }
    fun initData(){
        Log.i(TAG, "initData: ")
        var intent = intent
        if (intent.hasExtra("message")){
            message = intent.getStringExtra("message")
            Log.i(TAG,"message:$message")
            try {
                if (!TextUtils.isEmpty(message)){
                    if (message.contains("tel")){
                        val data = message.split("tel:")
                        val number = data[1].substring(0, data[1].indexOf(";"))
                        audio_in_phone.setText(number)
                        callRecord.call_phone = number
                        callRecord.call_name = number
                        val contactsList = ContactsDao.getInstance(this).queryPhone(number)
                        if (contactsList!=null&&contactsList.size!=0){
                            callRecord.call_name = contactsList.get(0).user_name
                            audio_in_phone.setText(callRecord.call_name)
                        }
                    } else {
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
        }
        if(intent.hasExtra(CallRecordTable.CALL_STATUS)){
            var call_status = intent.getStringExtra(CallRecordTable.CALL_STATUS)
            if (!TextUtils.isEmpty(call_status)){
                callRecord.call_status = call_status
            }else{
                callRecord.call_status = CallInfo.CALL_IN
            }
        }
    }
    private fun initDisplayName(currentCall: Call) {
        val remoteAddress = currentCall.remoteAddress
        if (remoteAddress != null) {
            var displayName = remoteAddress.displayName
            if (displayName == null || displayName.isEmpty()) {
                displayName = remoteAddress.username
                Log.i(TAG, "initDisplayName: getUsername:$displayName")
            }
            if (displayName == null || displayName.isEmpty()) {
                displayName = remoteAddress.asStringUriOnly()
                Log.i(TAG, "initDisplayName: asStringUriOnly:$displayName")
            }
            audio_in_phone.setText(displayName)
            try {
                if (displayName.contains("tel:")){
                    val data = displayName.split("tel:")
                    val number = data[1].substring(0, data[1].indexOf(";"))
                    audio_in_phone.setText(number)
                    callRecord.call_name = number
                    callRecord.call_phone = number
                    var contactsList = ContactsDao.getInstance(this).queryPhone(number)
                    if (contactsList!=null&&contactsList.size!=0){
                        callRecord.call_name = contactsList.get(0).user_name
                        audio_in_phone.setText(callRecord.call_name)
                    }
                }else{
                    callRecord.call_name = displayName
                    callRecord.call_phone = ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            audio_in_phone.setText("Anonymous")
            callRecord.call_name = "Anonymous"
            callRecord.call_phone = ""
        }
    }
    override fun onClick(v: View) {
        when (v.id){
            R.id.call_in_hang_up ->{
//                Toast.makeText(this,"挂断",Toast.LENGTH_SHORT).show()
                callRecord.call_status = CallInfo.CALL_MISSED
                if (instance!=null){
                    instance!!.hangUp()
                }
                finish()
            }
            R.id.call_in_answer ->{
//                Toast.makeText(this,"接听",Toast.LENGTH_SHORT).show()
//                callRecord.call_status = CallInfo.CALL_ANSWER
                try {
                    var numberOfCameras = Camera.getNumberOfCameras()
                    Log.i(TAG, "get camera number:" + numberOfCameras)
//                    numberOfCameras= 1;
                    val hasMicroPhone = AudioUtil.hasMicroPhone(this)
                    if (numberOfCameras == 0&&hasMicroPhone == false) {
//                        NotCameraDialog(this).Builder().show()
                        Log.i(TAG,"not camera and not microphone")
                        dialog = NotCameraDialog(this)
                        dialog!!.Builder(this).show()
                    }else if (numberOfCameras==0 && hasMicroPhone == true){
                        instance!!.lilin_jie(false)
                        Log.i("call_in_answer","获取摄像头失败，自动转语音")
                        var intent = Intent(this@CallInActivity, AudioActivity::class.java)
                        if (!TextUtils.isEmpty(message)){
                            intent.putExtra("message",message)
                        }
                        intent.putExtra(CallRecordTable.CALL_STATUS,CallInfo.CALL_IN)
                        startActivity(intent)
                        finish()
                    }else{
                        instance!!.lilin_jie()
                        if (instance!!.lilin_getVideoEnabled()) {//启动视频
                            Log.i("call_in_answer","接视频")
                            var intent = Intent(this@CallInActivity, VideoActivity::class.java)
                            if (!TextUtils.isEmpty(message)){
                                intent.putExtra("message",message)
                            }
                            intent.putExtra(CallRecordTable.CALL_STATUS,CallInfo.CALL_IN)
                            startActivity(intent)
                            finish()
                        }else{
                            Log.i("call_in_answer","接语音")
                            var intent = Intent(this@CallInActivity, AudioActivity::class.java)
                            if (!TextUtils.isEmpty(message)){
                                intent.putExtra("message",message)
                            }
                            intent.putExtra(CallRecordTable.CALL_STATUS,CallInfo.CALL_IN)
                            startActivity(intent)
                            finish()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.message)
                }catch (e:NullPointerException){
                    Log.e(TAG, e.message)
                }catch (e:Exception){
                    Log.e(TAG, e.message)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG,"onDestroy")
        callRecord.call_time = System.currentTimeMillis().toString()
        if(callRecord.call_status != CallInfo.CALL_IN){
            CallRecordUtil.updateCallRecord(this,callRecord)
        }
        if (null!=dialog&&dialog!!.isShow()){
            dialog!!.cancel()
        }
        if(callRecord.call_status == CallInfo.CALL_IN||callRecord.call_status == CallInfo.CALL_MISSED){
            var userInfoUtil = UserInfoUtil.getInstance(this)
            val userId= userInfoUtil.getUserId()
            var talkbean = TalkBean()
            talkbean.userId=userId
            talkbean.talkDuration= (0)
            talkbean.call_phone = callRecord.call_phone
            talkbean.call_status = callRecord.call_status
            talkbean.talkTime = TimeUtil.getDateTime(TimeUtil.dateFormatYMDHMS,firstCallTime!!)
            TalkManage.addConversationLog(talkbean,object: TalkCallBack {
                override fun onSuccess() {

                }

                override fun OnFailed() {

                }
            })
        }
//        CallRecordDao.getInstance(this).insertData(callRecord)
        unregisterReceiver(mReceiver)
    }
    inner class MainActivityReceiver : BroadcastReceiver() {
        private val TAG = "LoginReceiver"
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.getStringExtra("action")
            if (!TextUtils.isEmpty(action)) {
                android.util.Log.i(TAG, "onReceive: get action:" + action)
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
                            callRecord.call_status = CallInfo.CALL_MISSED
                            finish()
                        }else if(data.contains("Call released")){
                            callRecord.call_status = CallInfo.CALL_MISSED
                            finish()
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
                                    dialog = NotCameraDialog(context)
                                    dialog!!.Builder(context).show()
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
                }
                    else -> {

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