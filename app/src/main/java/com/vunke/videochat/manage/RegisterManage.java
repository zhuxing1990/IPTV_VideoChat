package com.vunke.videochat.manage;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.vunke.videochat.config.BaseConfig;
import com.vunke.videochat.linphone.LinphoneService;
import com.vunke.videochat.tools.AccountUtil;

import org.linphone.core.AccountCreator;
import org.linphone.core.AuthInfo;
import org.linphone.core.Core;
import org.linphone.core.DialPlan;
import org.linphone.core.Factory;
import org.linphone.core.ProxyConfig;
import org.linphone.core.TransportType;
import org.linphone.core.tools.Log;

/**
 * Created by zhuxi on 2019/11/20.
 */

public class RegisterManage {
    private static final String TAG = "RegisterManage";
//    public static void Login(Context context,String userName,String passWord){
//        try {
//            LinphoneService instance = LinphoneService.getInstance();
//            instance.lilin_reg(BaseConfig.INSTANCE.getIpaddr(),BaseConfig.INSTANCE.getAreaCode()+userName, passWord,BaseConfig.INSTANCE.getPort() ,TransportType.Udp);//正式
////            instance.lilin_reg(BaseConfig.INSTANCE.getIpaddr(),userName, passWord,BaseConfig.INSTANCE.getPort() ,TransportType.Udp);//测试
//        }catch (CoreException e){
//            e.printStackTrace();
//            Toast.makeText(context,"登录失败，请稍候再试!",Toast.LENGTH_SHORT).show();
//        } catch (Exception e){
//            e.printStackTrace();
//            Toast.makeText(context,"登录异常,请重试!",Toast.LENGTH_SHORT).show();
//        }
//    }
    public static void Login(Context context, String userName, String passWord,String domain) {
        LinphoneService instance = LinphoneService.getInstance();
        Core core = instance.getCore();
        for (ProxyConfig linphoneProxyConfig : core.getProxyConfigList()) {
            core.removeProxyConfig(linphoneProxyConfig);
            Log.e(TAG, "Login:  remove proxy config ");
        }
        for (AuthInfo a : core.getAuthInfoList()) {
            Log.e(TAG, "Login:  remove auth info ");
            core.removeAuthInfo(a);
        }
        if (core != null) {
            Log.i(TAG, "Login: init accountCreator");
            String basePath = context.getFilesDir().getAbsolutePath();
            AccountUtil.reloadAccountCreatorConfig(basePath + "/default_assistant_create.rc");
            AccountCreator accountCreator = core.createAccountCreator("https://subscribe.linphone.org:444/wizard.php");
            accountCreator.setDomain(BaseConfig.INSTANCE.getIpaddr());//旧版本
//            accountCreator.setDomain(domain);//新版本
            accountCreator.setTransport(TransportType.Udp);
            accountCreator.setUsername(userName);
            accountCreator.setPassword(passWord);
            AccountUtil.createProxyConfigAndLeaveAssistant(context, accountCreator);
        }
        AccountUtil.startLogin(context,userName, passWord,domain);
    }

//    public static void Login(Context context,String userName,String passWord){
//        try {
//            Core core = LinphoneService.getCore();
//            String configFile = context.getFilesDir().getAbsolutePath()+"/.linphonerc";
//            Config config = Factory.instance().createConfig(configFile);
//            AccountCreator accountCreator = core.createAccountCreator(config.getString("assistant", "xmlrpc_url", null));
//            accountCreator.setDomain(com.vunke.videochat.base.BaseConfig.domain);
//            accountCreator.setTransport(TransportType.Udp);
//            //        accountCreator.setUsername(BaseConfig.INSTANCE.getAreaCode()+userName);
////        accountCreator.setPassword(passWord);
//            ProxyConfig mProxyConfig = accountCreator.createProxyConfig();
//            if (mProxyConfig==null){
//                Log.e("[Assistant] Account creator couldn't create proxy config");
//                Log.e("createProxyConfigAndLeaveAssistant: 帐户创建者无法创建代理配置");
//                String defaultConfig = context.getFilesDir().getAbsolutePath()+"/default_assistant_create.rc";
//                core.loadConfigFromXml(defaultConfig);
//                mProxyConfig = core.createProxyConfig();
//            }else{
//                if (mProxyConfig.getDialPrefix() == null) {
//                    DialPlan dialPlan = getDialPlanForCurrentCountry(context);
//                    if (dialPlan != null) {
//                        mProxyConfig.setDialPrefix(dialPlan.getCountryCallingCode());
//                    }
//                }
//            }
//            if (mProxyConfig!=null){
//                mProxyConfig.edit();
//                Address identityAddress = mProxyConfig.getIdentityAddress();
//                if (identityAddress==null){
//                    Log.i("CreateAccount: identityAddress is null,init Address");
//                    String address = "<sip:domain;transport=udp>";
//                    identityAddress= Factory.instance().createAddress(address);
//                }
//
//                AuthInfo mAuthInfo = mProxyConfig.findAuthInfo();
//                if (mAuthInfo==null){
//                    Log.i("CreateAccount: AuthInfo is null,create AuthInfo");
//                    mAuthInfo = Factory.instance().createAuthInfo(null, null, null, null, null, null);
//                }
//
//                NatPolicy natPolicy = mProxyConfig.getNatPolicy();
//                if (natPolicy == null) {
//                    natPolicy = core.createNatPolicy();
//                    core.setNatPolicy(natPolicy);
//                }
//                if (identityAddress != null) {
//                    identityAddress.setUsername(userName);
//                    identityAddress.setDomain(com.vunke.videochat.base.BaseConfig.domain);
//                    mProxyConfig.setIdentityAddress(identityAddress);
//                    mProxyConfig.setServerAddr(identityAddress.asString());
//                    mProxyConfig.done();
//                }
//                mAuthInfo.setUsername(userName);
//                mAuthInfo.setDomain(com.vunke.videochat.base.BaseConfig.domain);
//                mAuthInfo.setUserid(userName+ "@"+com.vunke.videochat.base.BaseConfig.domain);
//                if (core != null) {
//                    core.refreshRegisters();
//                }
//                mAuthInfo.setHa1(null);
//                mAuthInfo.setPassword(passWord);
//                mAuthInfo.setAlgorithm(null);
//                core.addAuthInfo(mAuthInfo);
//                core.refreshRegisters();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
    private static DialPlan getDialPlanForCurrentCountry(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String countryIso = tm.getNetworkCountryIso();
            return getDialPlanFromCountryCode(countryIso);
        } catch (Exception e) {
            Log.e("[Assistant] " + e);
        }
        return null;
    }
    private static DialPlan getDialPlanFromCountryCode(String countryCode) {
        if (countryCode == null || countryCode.isEmpty()) return null;

        for (DialPlan c : Factory.instance().getDialPlans()) {
            if (countryCode.equalsIgnoreCase(c.getIsoCountryCode())) return c;
        }
        return null;
    }


}
