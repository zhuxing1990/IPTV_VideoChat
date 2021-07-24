package com.vunke.videochat.base;

/**
 * Created by zhuxi on 2020/2/27.
 */

public class BaseConfig {
   public static final String  BASE_URL = "http://10.255.26.3:8083/VideoPhone/";//旧基础URL
//   public static final String  BASE_URL = "http://10.255.26.4:8083/VideoPhone/";//新基础URL
//   public static final String  BASE_URL = "http://10.255.26.3:8084/VideoPhone/";//基础URL
//   public static final String  BASE_URL = "http://134.175.229.3:8083/VideoPhone/";//基础URL
//   public static final String  BASE_URL = "http://124.232.136.236:8082/VideoPhone/";//test
    /**
     *  获取联系人列表
     **/
    public static final String GET_USER_CONTACTS_LIST = "addressInfo";
    public static final String ADD_USER_CONTACTS = "addressAdd";
    public static final String DEL_USER_CONTACTS = "addressDel";
    public static final String LOGIN = "login.do";//旧登录接口
//    public static final String LOGIN = "videophone/login";//新登录接口

    public static final String GET_OPTIONAL_ACCOUNT ="oai/getOptionalAccount";

    public static final String FIXE_LINE_BINDING ="oai/fixedLineBinding";

    public static final String QUY_BY_CUSTOMER_DATA = "oai/qryByCustomerData";

    public static final String ADD_CONVERSTION_LOG = "addConversationLog";

    public static final String ADD_ACCESS_LOG = "addAccessLog";

//    public static final  String ipaddr = "10.255.25.48";
//    public static final  String domain = "hu.ctcims.cn";
//    public static final  String port = "5060";
//    public static final  String areaCode = "+86";
    public static final  String lastCallNumber = "LAST_CALL_NUMBER";
//    public static final  String NINE = "9";
    public static final  String RECEIVE_MAIN_ACTIVITY = "receive_main_activity";
    public static final  String RECEIVE_VIDEO_ACTIVITY = "receive_video_activity";
    public static final  String RECEIVE_UPB_CHANGE = "receive_usb_change";
    public static final  String RECEVIE_OPEN_OVER = "receive_open_over";
    public static final String ADD_CONTACTS_QRCODE_URL ="http://web.leso114.com/mailList/mailList.html";
    public static final String CONTACTS_CONTENT_URL = "content://com.vunke.videochat.attn/attn";

}
