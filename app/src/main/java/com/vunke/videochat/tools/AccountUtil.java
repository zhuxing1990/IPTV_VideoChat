package com.vunke.videochat.tools;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.vunke.videochat.config.BaseConfig;
import com.vunke.videochat.linphone.LinphoneService;

import org.linphone.core.AccountCreator;
import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.Core;
import org.linphone.core.DialPlan;
import org.linphone.core.Factory;
import org.linphone.core.NatPolicy;
import org.linphone.core.ProxyConfig;
import org.linphone.core.TransportType;
import org.linphone.core.tools.Log;

import java.util.Locale;

public class AccountUtil {
    private static final String TAG = "AccountUtil";
    public static AccountCreator getAccountCreator() {
        AccountCreator mAccountCreator = LinphoneService.getInstance().getmCore().createAccountCreator("https://subscribe.linphone.org:444/wizard.php");
        return mAccountCreator;
    }

    public static void reloadAccountCreatorConfig(String path) {
        Log.i(TAG,"reloadDefaultAccountCreatorConfig: 创建默认的代理配置和离线助手");
        Log.i(TAG,"reloadAccountCreatorConfig: path:" + path);
        Core core = LinphoneService.getInstance().getCore();
        if (core != null) {
            core.loadConfigFromXml(path);
            AccountCreator accountCreator = getAccountCreator();
            accountCreator.reset();
            accountCreator.setLanguage(Locale.getDefault().getLanguage());
        }
    }

    public static void createProxyConfigAndLeaveAssistant(Context context, AccountCreator  accountCreator) {
        Core core = LinphoneService.getCore();
        ProxyConfig proxyConfig = accountCreator.createProxyConfig();
        if (proxyConfig == null) {
            Log.e("[Assistant] Account creator couldn't create proxy config");
            Log.e("createProxyConfigAndLeaveAssistant: 帐户创建者无法创建代理配置");
            // TODO: display error message
        } else {
            if (proxyConfig.getDialPrefix() == null) {
                DialPlan dialPlan = getDialPlanForCurrentCountry(context);
                if (dialPlan != null) {
                    proxyConfig.setDialPrefix(dialPlan.getCountryCallingCode());
                }
            }
        }
    }

    public static DialPlan getDialPlanForCurrentCountry(Context context) {
        try {
            TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String countryIso = tm.getNetworkCountryIso();
            return getDialPlanFromCountryCode(countryIso);
        } catch (Exception e) {
            Log.e("[Assistant] " + e);
        }
        return null;
    }

    public static DialPlan getDialPlanFromCountryCode(String countryCode) {
        if (countryCode == null || countryCode.isEmpty()) return null;

        for (DialPlan c : Factory.instance().getDialPlans()) {
            if (countryCode.equalsIgnoreCase(c.getIsoCountryCode())) return c;
        }
        return null;
    }
    public static void startLogin(Context context,String userName, String passWord) {
        Log.i(TAG, "startLogin: ");
        ProxyConfig mProxyConfig = null;
        AuthInfo mAuthInfo = null;
        Core core = LinphoneService.getCore();
        if (core != null) {
            ProxyConfig[] proxyConfigs = core.getProxyConfigList();
            for (ProxyConfig proxyConfig : proxyConfigs) {
                Log.i(TAG, "startLogin: get proxyConfig");
                mProxyConfig = proxyConfig;
            }
            if (mProxyConfig == null) {
                // Ensure the default configuration is loaded first
                Log.i(TAG, "startLogin: mProxyConfig is null ,create default config");
                String defaultConfig = context.getFilesDir().getAbsolutePath()+"/default_assistant_create.rc";
                core.loadConfigFromXml(defaultConfig);
                mProxyConfig = core.createProxyConfig();
            }
            if (mProxyConfig != null) {
                Log.i(TAG, "startLogin: mProxyConfig start edit");
                mAuthInfo = mProxyConfig.findAuthInfo();
                if (mAuthInfo == null) {
                    Log.i(TAG, "startLogin: find authinfo failed,create new auth info");
                    mAuthInfo =
                            Factory.instance().createAuthInfo(null, null, null, null, null, null);
                }
                Address identity = mProxyConfig.getIdentityAddress();
                if (identity != null) {
                    Log.i(TAG, "startLogin: init identity");
                    identity.setUsername(BaseConfig.INSTANCE.getAreaCode() + userName);//正式
//                    identity.setUsername( userName);//测试
                    identity.setDomain(BaseConfig.INSTANCE.getDomain());
                }
                NatPolicy natPolicy = mProxyConfig.getNatPolicy();
                if (natPolicy == null) {
                    Log.i(TAG, "startLogin: init Natpplicy");
                    natPolicy = core.createNatPolicy();
                    core.setNatPolicy(natPolicy);
                    mProxyConfig.setNatPolicy(natPolicy);
                }
                mProxyConfig.setIdentityAddress(identity);

                if (mAuthInfo != null) {
                    Log.i(TAG, "startLogin: start add authInfo");
                    mAuthInfo.setUsername(BaseConfig.INSTANCE.getAreaCode() + userName);//正式
//                    mAuthInfo.setUsername(userName);//测试
                    mAuthInfo.setUserid(
                            BaseConfig.INSTANCE.getAreaCode()+    //正式
                                     userName
                                    + "@"
                                    + BaseConfig.INSTANCE.getDomain());
                    mAuthInfo.setHa1(null);
                    mAuthInfo.setPassword(passWord);
                    // Reset algorithm to generate correct hash depending on
                    // algorithm set in next to come 401
                    mAuthInfo.setAlgorithm(null);
                    mAuthInfo.setDomain(BaseConfig.INSTANCE.getDomain());
                }
                String proxystr =
                        "<sip:" + BaseConfig.INSTANCE.getIpaddr() + ";transport=udp>"; // 正式
                Address proxy = Factory.instance().createAddress(proxystr);
                mProxyConfig.setServerAddr(proxy.asString());
                mProxyConfig.setExpires(3600);
                mProxyConfig.setRoute(proxy.asStringUriOnly());
                String server = mProxyConfig.getServerAddr();
                Address serverAddr = Factory.instance().createAddress(server);
                if (serverAddr != null) {
                    try {
                        Log.i(TAG, "startLogin: init transport and server addr");
                        serverAddr.setTransport(TransportType.Udp);
                        server = serverAddr.asString();
                        mProxyConfig.setServerAddr(server);
                        mProxyConfig.setRoute(server);
                    } catch (NumberFormatException nfe) {
                        Log.e(nfe);
                    }
                }
                if (core != null && mProxyConfig != null && mAuthInfo != null) {
                    core.addAuthInfo(mAuthInfo);
                    core.addProxyConfig(mProxyConfig);
                    //                    core.setDefaultProxyConfig(mProxyConfig);
                    core.refreshRegisters();
                    Log.i(TAG, "startLogin: start refresh registers");
                }
            }
        }
    }

}
