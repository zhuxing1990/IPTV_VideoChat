package com.vunke.videochat.adaper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vunke.videochat.R;
import com.vunke.videochat.base.BaseConfig;
import com.vunke.videochat.config.CallInfo;
import com.vunke.videochat.dao.CallRecordDao;
import com.vunke.videochat.db.CallRecord;
import com.vunke.videochat.db.ContactsTable;
import com.vunke.videochat.dialog.CallRecordDialog;
import com.vunke.videochat.dialog.NotCameraDialog;
import com.vunke.videochat.manage.CallManage;
import com.vunke.videochat.tools.SPUtils;
import com.vunke.videochat.tools.TimeUtil;
import com.vunke.videochat.tools.Utils;
import com.vunke.videochat.ui.AddContactActivity;

import java.util.List;

/**
 * Created by zhuxi on 2020/4/2.
 */

public class AttnAdapter extends RecyclerView.Adapter<AttnAdapter.AttnHolder> {
    private static final String TAG = "AttnAdapter";
    private List<CallRecord> list;
    private Activity context;
    private CallRecordDialog callRecordDialog;
    public AttnAdapter(Activity context, List<CallRecord> list){
        this.list = list;
        this.context = context;
    }

    public void update(List<CallRecord> newList) {
        list.clear();
        list.addAll(newList);
        this.notifyDataSetChanged();
    }
    public void clear(){
        list.clear();
        notifyDataSetChanged();
    }
    @Override
    public AttnHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_attn,parent,false);
        AttnHolder holder = new AttnHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(AttnHolder holder, final int position) {
        CallRecord callRecord = list.get(position);
        Log.i(TAG, "onBindViewHolder: callRecord:"+callRecord.toString());
        String call_phone = list.get(position).call_phone;
        String call_name = list.get(position).call_name;
        if (!TextUtils.isEmpty(call_name)){
            holder.recycler_contacts_phone.setText(call_name);
        }else{
            holder.recycler_contacts_phone.setText(call_phone);
        }
        String time = list.get(position).call_time;
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TimeUtil.dateFormatYMDHM2);
        String callTime = "";
        boolean isToday = TimeUtil.isToday(Long.valueOf(time));
        if (isToday){
            callTime = TimeUtil.getDateTime(TimeUtil.dateFormatMDHM,Long.valueOf(time));
        }else{
            callTime = Utils.getDate(context,Long.valueOf(time));
        }
//        String callTime = TimeUtil.formatDateStr2Desc(time, TimeUtil.dateFormatYMDHM2);
//        holder.recycler_contacts_time.setText(Utils.getDate(context,Long.valueOf(time)));
        holder.recycler_contacts_time.setText(callTime);
            String status = list.get(position).getCall_status();
            if (!TextUtils.isEmpty(status)){
                if (status.equals(CallInfo.INSTANCE.getCALL_ANSWER())){
                    holder.recycler_contacts_status.setImageResource(R.mipmap.record_callin);
                    holder.recycler_contacts_status2.setText("已接");
                    holder.recycler_contacts_status2.setTextColor(Color.parseColor("#5494f4"));
                }else  if (status.equals(CallInfo.INSTANCE.getCALL_HANGUP())){
                    holder.recycler_contacts_status.setImageResource(R.mipmap.record_callout);
                    holder.recycler_contacts_status2.setText("呼出");
                    holder.recycler_contacts_status2.setTextColor(Color.parseColor("#5494f4"));
                }else  if (status.equals(CallInfo.INSTANCE.getCALL_MISSED())){
                    holder.recycler_contacts_status.setImageResource(R.mipmap.record_callmissed);
                    holder.recycler_contacts_status2.setText("未接");
                    holder.recycler_contacts_status2.setTextColor(Color.parseColor("#f45454"));
                }else  if (status.equals(CallInfo.INSTANCE.getCALL_RINGING())){
                    holder.recycler_contacts_status.setImageResource(R.mipmap.record_callin);
                    holder.recycler_contacts_status2.setText("已接");
                    holder.recycler_contacts_status2.setTextColor(Color.parseColor("#43f87a"));
                }else if (status.equals(CallInfo.INSTANCE.getCALL_OUT())){
                    holder.recycler_contacts_status.setImageResource(R.mipmap.record_callout);
                    holder.recycler_contacts_status2.setText("呼出");
                    holder.recycler_contacts_status2.setTextColor(Color.parseColor("#5494f4"));
                }else if(status.equals(CallInfo.INSTANCE.getCALL_IN())){
                    holder.recycler_contacts_status.setImageResource(R.mipmap.record_callin);
                    holder.recycler_contacts_status2.setText("已接");
                    holder.recycler_contacts_status2.setTextColor(Color.parseColor("#43f87a"));
                }
            }
            holder.recycler_contacts_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setAttn(context,position);
                }
            });

    }

    private void setAttn(final Activity mcontext, final int position) {
        if (callRecordDialog!=null&& callRecordDialog.isShowing()){
            callRecordDialog.cancel();
        }
        callRecordDialog = new CallRecordDialog(mcontext).builder();
        callRecordDialog.setCallAudioOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int numberOfCameras = Camera.getNumberOfCameras();
                Log.i(TAG,"get camera number:"+numberOfCameras);
                if (numberOfCameras==0){
                    callRecordDialog.cancel();
//                    new NotCameraDialog(context).Builder().show();
                    new NotCameraDialog(context).Builder(context).show();
                }else{
                    String phone = list.get(position).getCall_phone();
                    SPUtils.putString(mcontext, BaseConfig.lastCallNumber,phone);
                    CallManage.CallAudio(mcontext,phone);
                    callRecordDialog.cancel();
                }
            }
        });
        callRecordDialog.setCallVideoOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int numberOfCameras = Camera.getNumberOfCameras();
                Log.i(TAG,"get camera number:"+numberOfCameras);
                if (numberOfCameras==0){
                    new NotCameraDialog(context).Builder(context).show();
                    callRecordDialog.cancel();
                }else{
                    String phone = list.get(position).getCall_phone();
                    SPUtils.putString(mcontext, BaseConfig.lastCallNumber,phone);
                    CallManage.CallVideo(mcontext,phone);
                    callRecordDialog.cancel();
                }
            }
        });
        callRecordDialog.setDelOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CallRecord callRecord = list.get(position);
                int deleteData = CallRecordDao.Companion.getInstance(mcontext).deleteData(callRecord);
                if (deleteData!=-1){
                    Log.i(TAG, "onClick: 删除成功");
                }else{
                    Log.i(TAG, "onClick: 删除失败");
                }
                List<CallRecord> callRecordList = CallRecordDao.Companion.getInstance(mcontext).queryAll();
                update(callRecordList);
                callRecordDialog.cancel();
            }
        });
        callRecordDialog.setBackOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callRecordDialog.cancel();
            }
        });
        callRecordDialog.setAddOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = list.get(position).getCall_phone();
                String name = list.get(position).getCall_name();
                long id = list.get(position).getCall_id();
                AddContact(phone,name,id);
                callRecordDialog.cancel();
            }
        });
    }
//    private AddContactsDialog dialog;
    private void AddContact(String phone,String name,Long id) {
        Intent intent = new Intent(context, AddContactActivity.class);
        if (!name.equals(phone)){
            intent.putExtra(ContactsTable.INSTANCE.getUSER_NAME(),name);
        }
        if (0!=id){
            intent.putExtra(ContactsTable.INSTANCE.get_ID(),id);
        }
        intent.putExtra(ContactsTable.INSTANCE.getPHONE(),phone);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
//        if (dialog!=null&&dialog.isShowing()){
//            dialog.cancel();
//        }
//        dialog = new AddContactsDialog(context).builder();
//        dialog.setPhoenEdit(phone);
//        dialog.setNameEdit(name);
//        dialog.setPositiveButton("", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String name = dialog.getNameEdit().toString();
//                String phone = dialog.getPhoenEdit().toString();
//                if (TextUtils.isEmpty(name)){
//                    Toast.makeText(context,"请输入姓名",Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (TextUtils.isEmpty(phone)){
//                    Toast.makeText(context,"请输入号码",Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (!Utils.isNumeric(phone)){
//                    Toast.makeText(context,"号码请输入数字",Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                Contacts contacts = new Contacts();
//                contacts.setUser_name(name);
//                contacts.setPhone(phone);
//                Long i = ContactsDao.Companion.getInstance(context).saveData(contacts);
//                if (i!=-1L){
//                    Toast.makeText(context,"保存成功",Toast.LENGTH_SHORT).show();
//                    dialog.cancel();
//                    try {
//                        Intent intent = new Intent();
//                        intent.setAction(ContactsReceiver.CONTACTS_ADD_ACTION);
//                        context.sendBroadcast(intent);
//                        Intent intent2 = new Intent();
//                        intent2.setAction(CallRecordReceiver.CALL_RECORD_ACTION);
//                        context.sendBroadcast(intent2);
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }else {
//                    Toast.makeText(context,"保存失败",Toast.LENGTH_SHORT).show();
//                }
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
    public int getItemCount() {
        return list.size();
    }


    public class AttnHolder extends RecyclerView.ViewHolder{
        private ImageView recycler_contacts_img;
        private ImageView recycler_contacts_status;
        private TextView recycler_contacts_status2;
        private TextView recycler_contacts_phone;
        private TextView recycler_contacts_time;
        private RelativeLayout recycler_contacts_content;
        public AttnHolder(View itemView) {
            super(itemView);
            recycler_contacts_content = itemView.findViewById(R.id.recycler_contacts_content);
            recycler_contacts_img = itemView.findViewById(R.id.recycler_contacts_img);
            recycler_contacts_status = itemView.findViewById(R.id.recycler_contacts_status);
            recycler_contacts_status2 = itemView.findViewById(R.id.recycler_contacts_status2);
            recycler_contacts_phone = itemView.findViewById(R.id.recycler_contacts_phone);
            recycler_contacts_time = itemView.findViewById(R.id.recycler_contacts_time);
        }
    }
}
