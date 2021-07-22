package com.vunke.videochat.tools;

import android.text.TextUtils;
import android.util.Log;

import com.vunke.videochat.config.BaseConfig;

import org.linphone.core.AVPFMode;
import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.Core;
import org.linphone.core.CoreException;
import org.linphone.core.Factory;
import org.linphone.core.ProxyConfig;
import org.linphone.core.TransportType;

public   class AccountBuilder {
    	private Core lc;
		private String tempUsername;
		private String tempDisplayName;
		private String tempUserId;
		private String tempPassword;
		private String tempHa1;
		private String tempDomain;
		private String tempProxy;
		private String tempRealm;
		private String tempPrefix;
		private boolean tempOutboundProxy;
		private String tempContactsParams;
		private String tempExpire;
		private TransportType tempTransport;
		private boolean tempAvpfEnabled = false;
		private int tempAvpfRRInterval = 0;
		private String tempQualityReportingCollector;
		private boolean tempQualityReportingEnabled = false;
		private int tempQualityReportingInterval = 0;
		private boolean tempEnabled = true;
		private boolean tempNoDefault = false;


		public AccountBuilder(Core lc) {
			this.lc = lc;
		}

		public AccountBuilder setTransport(TransportType transport) {
			tempTransport = transport;
			return this;
		}

		public AccountBuilder setUsername(String username) {
			tempUsername = username;
			return this;
		}

		public AccountBuilder setDisplayName(String displayName) {
			tempDisplayName = displayName;
			return this;
		}

		public AccountBuilder setPassword(String password) {
			tempPassword = password;
			return this;
		}

		public AccountBuilder setHa1(String ha1) {
			tempHa1 = ha1;
			return this;
		}

		public AccountBuilder setDomain(String domain) {
			tempDomain = domain;
			return this;
		}

		public AccountBuilder setProxy(String proxy) {
			tempProxy = proxy;
			return this;
		}

		public AccountBuilder setOutboundProxyEnabled(boolean enabled) {
			tempOutboundProxy = enabled;
			return this;
		}

		public AccountBuilder setContactParameters(String contactParams) {
			tempContactsParams = contactParams;
			return this;
		}

		public AccountBuilder setExpires(String expire) {
			tempExpire = expire;
			return this;
		}

		public AccountBuilder setUserId(String userId) {
			tempUserId = userId;
			return this;
		}

		public AccountBuilder setAvpfEnabled(boolean enable) {
			tempAvpfEnabled = enable;
			return this;
		}

		public AccountBuilder setAvpfRRInterval(int interval) {
			tempAvpfRRInterval = interval;
			return this;
		}

		public AccountBuilder setRealm(String realm) {
			tempRealm = realm;
			return this;
		}

		public AccountBuilder setQualityReportingCollector(String collector) {
			tempQualityReportingCollector = collector;
			return this;
		}

		public AccountBuilder setPrefix(String prefix) {
			tempPrefix = prefix;
			return this;
		}

		public AccountBuilder setQualityReportingEnabled(boolean enable) {
			tempQualityReportingEnabled = enable;
			return this;
		}

		public AccountBuilder setQualityReportingInterval(int interval) {
			tempQualityReportingInterval = interval;
			return this;
		}

		public AccountBuilder setEnabled(boolean enable) {
			tempEnabled = enable;
			return this;
		}

		public AccountBuilder setNoDefault(boolean yesno) {
			tempNoDefault = yesno;
			return this;
		}

		/**
		 * Creates a new account
		 * @throws CoreException
		 */
		public void saveNewAccount(Factory lcFactory) throws CoreException {

			if (TextUtils.isEmpty(tempUsername) ||  TextUtils.isEmpty(tempDomain)) {
				Log.i("提示","Skipping account save: username or domain not provided");
				return;
			}
//			String identity = "sip:" + tempUsername + "@" + tempDomain;//测试
//			String identity = "sip:"+ tempUsername;
			String identity = "sip:" + tempUsername + "@"+ BaseConfig.INSTANCE.getDomain();//正式

//			String proxy = "sip:";
			String proxy = "<sip:"+BaseConfig.INSTANCE.getIpaddr()+";transport=udp>";//正式


			if (!TextUtils.isEmpty(tempDomain)) {
				proxy += tempDomain;
			} else {
				if (!tempProxy.startsWith("sip:") && !tempProxy.startsWith("<sip:")
						&& !tempProxy.startsWith("sips:") && !tempProxy.startsWith("<sips:")) {
					proxy += tempProxy;
				} else {
					proxy = tempProxy;
				}

			}
			Log.i("saveNewAccount", "saveNewAccount: proxy:"+proxy);
			Address proxyAddr = lcFactory.createAddress(proxy);

			Log.i("saveNewAccount", "saveNewAccount: identity:"+identity);
			Address identityAddr = lcFactory.createAddress(identity);

			if (tempDisplayName != null) {
				identityAddr.setDisplayName(tempDisplayName);
			}

			if (tempTransport != null) {
				proxyAddr.setTransport(tempTransport);
			}

			String route = tempOutboundProxy ? proxyAddr.asStringUriOnly() : null;

			ProxyConfig prxCfg = lc.createProxyConfig();
			prxCfg.setServerAddr(proxyAddr.asString());
			prxCfg.setIdentityAddress(identityAddr);
			prxCfg.setRoute(route);

			if (tempContactsParams != null)
				prxCfg.setContactUriParameters(tempContactsParams);
			if (tempExpire != null) {
				try {
					prxCfg.setExpires(Integer.parseInt(tempExpire));
				} catch (NumberFormatException nfe) {
					throw new CoreException(nfe);
				}
			}

			prxCfg.setAvpfMode(tempAvpfEnabled? AVPFMode.Enabled : AVPFMode.Disabled);
			prxCfg.setAvpfRrInterval(tempAvpfRRInterval);
			prxCfg.enableQualityReporting(tempQualityReportingEnabled);
			prxCfg.setQualityReportingCollector(tempQualityReportingCollector);
			prxCfg.setQualityReportingInterval(tempQualityReportingInterval);


			if(tempPrefix != null){
				prxCfg.setDialPrefix(tempPrefix);
			}
			if(tempRealm != null)
				prxCfg.setRealm(tempRealm);
			AuthInfo authInfo =lcFactory .createAuthInfo(tempUsername, tempUserId, tempPassword, tempHa1, tempRealm, tempDomain);
			authInfo.setUserid(tempUserId+ "@"+ BaseConfig.INSTANCE.getDomain());//正式
			lc.addProxyConfig(prxCfg);
			lc.addAuthInfo(authInfo);
			lc.setDefaultProxyConfig(prxCfg);
			if (!tempNoDefault)
				lc.setDefaultProxyConfig(prxCfg);
		}

	}