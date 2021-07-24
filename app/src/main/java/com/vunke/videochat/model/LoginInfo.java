package com.vunke.videochat.model;

/**
 * Created by zhuxi on 2020/2/27.
 */

public class LoginInfo {

    private DataBean data;
    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public class DataBean{
    private String userName;
    private String password;
    private String domain;
        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "userName='" + userName + '\'' +
                    ", password='" + password + '\'' +
                    ", domain='" + domain + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "LoginInfo{" +
                "data=" + data +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
