package com.sinosoft.phoneGapPlugins.util;

import com.sinosoft.mobileshop.util.CommonUtil;

public class Constant {

	public static final String XMPPPPORT = "5222"; // 推送端口号
	public static final String APIKEY = "1234567890"; // 推送key

	public static final String XMPPHOST = "9.0.2.211";// 内网测试地址
	public static final String XMPPHOST_OUT  = "220.178.31.53";// 外网测试地址
//	public static final String XMPPHOST_OUT  = "9.0.2.211";// 外网测试地址
//	public static final String XMPPHOST_OUT = "192.168.1.103";// 本地测试地址
//	public static final String XMPPHOST = "192.168.1.191";// 内网测试地址
//	public static final String XMPPHOST_OUT  = "192.168.1.191";// 外网测试地址
	
	public static final String ACTIONURL = "http://" + XMPPHOST + ":7002";//内网测试地址
	
	public static final String ACTIONURL_OUT = "http://" + XMPPHOST_OUT + ":8001";//外网测试地址
	
	public static final String ACTIONURL_NEWS = "http://" + XMPPHOST_OUT + ":8011";//消息平台地址
	
//	public static final String ACTIONURL_OUT = "http://" + XMPPHOST_OUT + ":7001";//本地测试用
	
	public static final int ftpPort = 21;// FTP端口号
	public static final String ftpUserName = "weblogic";// FTP用户名
	public static final String ftpPassword = "weblogic";// FTP密码

	public static final String GETVPN = "http://220.178.31.53:8001";// 获取VPN用户名密码地址
	// 获取GPS定位地址（百度API)
	public static final String LOCATIONURL = "http://115.239.210.16/geocoder/v2/?ak=FA0d0942f513ed3a466b7b9dde0c1b6b&location=%f,%f&output=json&pois=1&coordtype=wgs84ll";
	// xmpp 地址
	public static final String XMPPURL = XMPPHOST_OUT + ":" + XMPPPPORT;
	// ftp 地址
	public static final String FTPURL = XMPPHOST + ":" + ftpPort;
	// VPN标志 1：深信服 2：天融信
	public static final int VPNFLAG = 1; 
	// FTP端口`
	public static final String FTPPORT = "21";
	// 内网端口
	public static final String NETPORT = "7002";
	
	public static int VPNSTATUS = -1;
	public static boolean isAlert = false;
	public static boolean firstLoad = true;
	
	public static String USERCODE = CommonUtil.getUserinfo2("userCode",null);
//	public static String USERCODE = "0000000000";
	
//	String path = "/sdcard/gyicmobileplat/crash/";
	public static String USERINFO_PATH = "/sdcard/gyicPlat/";
	
	public static String USERINFO = "userInfo.text";
	
	// 内网 地址
	public static final String NETURL = XMPPHOST + ":" + NETPORT;

	public static final String GYICPACKAGE = "com.sinosoft.gyicPlat";
	// 获取VPN信息   
	public static final String GETVPNURL = Constant.GETVPN + "/meap/service/getVPN.do";
	// 获取应用列表
	public static final String GETAPPLIST = ACTIONURL + "/meap/service/getAppliCations.do?";
	// 获取图片
	public static final String GETIMAGEURL = ACTIONURL + "/meap/service/appimage.do?";
	// 下载apk
	public static final String GETAPK = ACTIONURL + "/meap/service/getApk.do";
	// 获取消息列表
	public static final String GETMESSAGEURL = ACTIONURL_NEWS + "/meapMessage/service/getMessages.do?";
	// 操作消息
	public static final String OPERMSGURL = ACTIONURL_NEWS + "/meapMessage/service/operateMessage.do?";
	// 保存意见反馈信息
	public static final String SAVEADVICEINFOURL = ACTIONURL + "/meap/android/saveFeedBack.do?";
	// 获取验证码
	public static final String GET_VERIFICATIONNO = ACTIONURL + "/meap/service/getIdentificationNo.do?";
	// 获取用户姓名
	public static final String GET_USERNAME = ACTIONURL + "/meap/service/getUserName.do?";
	// 激活
	public static final String ACTIVATION = ACTIONURL_OUT + "/meap/service/userActivation.do?";
	// 登录
	public static final String LOGIN = ACTIONURL_OUT + "/meap/service/toLogin.do?";
	// 重新注册和激活
	public static final String REACTIVITION = ACTIONURL + "/meap/service/userReActivation.do?";
	// 取消注册和激活
	public static final String CANCELACTIVITION = ACTIONURL + "/meap/service/userUnregister.do?";
	// 维护自己的手机号
	public static final String MAINTAINPHONENOSELF = ACTIONURL + "/meap/service/maintainPhoneNoSelf.do?";
	// 维护他人的手机号
	public static final String MAINTAINPHONENOMANAGER = ACTIONURL + "/meap/service/maintainPhoneNoManager.do?";
	// 获取平台版本信息
	public static final String GETVERSIONS = ACTIONURL + "/meap/service/getVersions.do?";
	// 获取应用版本信息
	public static final String GETAPPVERSIONS = ACTIONURL + "/meap/service/getVersions.do?";
	// 修改密码
	public static final String UPDATEPASSWORD = ACTIONURL_OUT + "/meap/service/updatePassword.do?";
	// 获取设备列表 
	public static final String GETDEVICELIST = ACTIONURL + "/meap/service/getDeviceList.do?";
	// 解绑设备
	public static final String UNBINDDEVICE = ACTIONURL+ "/meap/service/unbindDevice.do?";
		
}
