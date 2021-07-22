package com.vunke.videochat.model;

public class TalkBean {
    private String talkTime;
    private long talkDuration;
    private String userId;
    private String call_phone;
    private String user_number;
    private String call_status;
    public String getTalkTime() {
        return talkTime;
    }

    public void setTalkTime(String talkTime) {
        this.talkTime = talkTime;
    }

    public long getTalkDuration() {
        return talkDuration;
    }

    public void setTalkDuration(long talkDuration) {
        this.talkDuration = talkDuration;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCall_phone() {
        return call_phone;
    }

    public void setCall_phone(String call_phone) {
        this.call_phone = call_phone;
    }

    public String getUser_number() {
        return user_number;
    }

    public void setUser_number(String user_number) {
        this.user_number = user_number;
    }

    public String getCall_status() {
        return call_status;
    }

    public void setCall_status(String call_status) {
        this.call_status = call_status;
    }

    @Override
    public String toString() {
        return "TalkBean{" +
                "talkTime='" + talkTime + '\'' +
                ", talkDuration=" + talkDuration +
                ", userId='" + userId + '\'' +
                ", call_phone='" + call_phone + '\'' +
                ", user_number='" + user_number + '\'' +
                ", call_status='" + call_status + '\'' +
                '}';
    }
}
