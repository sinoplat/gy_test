package com.sinosoft.mobileshop.service;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.androidpn.client.ConnectivityReceiver;
import org.androidpn.client.Constants;
import org.androidpn.client.NotificationReceiver;
import org.androidpn.client.PhoneStateChangeListener;
import org.androidpn.client.XmppManager;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources.NotFoundException;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sangfor.ssl.SangforAuth;
import com.sinosoft.gyicPlat.MainActivity;
import com.sinosoft.mobileshop.activity.RegisterActivity;
import com.sinosoft.mobileshop.appwidget.floatwindow.MyWindowManager;
import com.sinosoft.mobileshop.bean.AppVersionInfo;
import com.sinosoft.mobileshop.service.receiver.WakeUpReceiver;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.LiteOrmUtil;
import com.sinosoft.mobileshop.util.TDevice;
import com.sinosoft.mobileshop.util.VolleyUtil;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.traffic.HttpLocation;
import com.sinosoft.traffic.TextFormater;
import com.sinosoft.traffic.TrafficHttp;

public class VpnWorkService extends Service implements OnSharedPreferenceChangeListener{

    private static final int sHashCode = VpnWorkService.class.getName().hashCode();

    public static boolean startFlag = false;
    
    private boolean isStop = false;
    private boolean isSure = false;
    private boolean isWait = false;
    public static final int SHOW_RECONNECTION = 1;//展示重连的对话框
    public static final int SHOW_SUCCESS = 2;//展示成功的对话框
    public static final int SHOW_FAIL = 3;//展示失败的对话框
    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private String packageNames = "";
    private int nineCount = 0;
    private AlertDialog waitDialog;//等待连接对话框
    private AlertDialog reconnectionDialog;//是否重连对话框
	private SharedPreferences preferences;
	private static HomeWatcherReceiver mHomeKeyReceiver = null;
	private double lat;
	private double lon;
	public String latStr;
	public String lonStr;
	// 用于格式化日期,作为日志文件名的一部分
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private String location;
	private int i = 1;
	private String networkaddress;
	private LocationManager locationManager;
	private DecimalFormat format;
	private String provider;
	private Timer uploadGPSTimer;
	private TimerTask uploadGPSTimerTask;
	public static final String SERVICE_NAME = "org.androidpn.client.NotificationService";
	private TelephonyManager telephonyManager;
	private BroadcastReceiver notificationReceiver;
	private BroadcastReceiver connectivityReceiver;
	private PhoneStateListener phoneStateListener;
	private ExecutorService executorService;
	private TaskSubmitter taskSubmitter;
	private TaskTracker taskTracker;
	private XmppManager xmppManager;
	private SharedPreferences sharedPrefs;
	private String deviceId;
	AlarmManager mAlarmManager = null;
	PendingIntent mPendingIntent = null;
	
	
	// 移植推送
	public VpnWorkService() {
		// 用于接收推送广播并用NotificationManager通知用户(系统通知栏的通知)
		notificationReceiver = new NotificationReceiver();
		// 接收手机网络状态的广播,来管理xmppManager与服务端的连接与断开
		connectivityReceiver = new ConnectivityReceiver(this);
		// 集成于android.telephony.PhoneStateListener,同上,用于监听数据链接的状态
		phoneStateListener = new PhoneStateChangeListener(this);
		// 线程池
		executorService = Executors.newSingleThreadExecutor();
		// 向线程池提交一个task任务
		taskSubmitter = new TaskSubmitter(this);
		// 任务计数器,用以维护当前工作的task任务
		taskTracker = new TaskTracker(this);
	}
	
	@Override
	public void onCreate() {
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		// wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		// connectivityManager = (ConnectivityManager)
		// getSystemService(Context.CONNECTIVITY_SERVICE);

		sharedPrefs = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME,
				Context.MODE_PRIVATE);

		// 获取设备ID,放入sharedPrefs中
		deviceId = telephonyManager.getDeviceId();
		// Log.d(LOGTAG, "deviceId=" + deviceId);
		Editor editor = sharedPrefs.edit();
		editor.putString(Constants.DEVICE_ID, deviceId);
		editor.commit();
		Intent intent = new Intent(getApplicationContext(),
				VpnWorkService.class);
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		mPendingIntent = PendingIntent.getService(this, 0, intent,
				Intent.FLAG_ACTIVITY_NEW_TASK);
		long now = System.currentTimeMillis();
		mAlarmManager.setInexactRepeating(AlarmManager.RTC, now, 6000,
				mPendingIntent);

		// 如果设备运行在模拟器,将模拟器放到sharedPrefs中
		if (deviceId == null || deviceId.trim().length() == 0
				|| deviceId.matches("0+")) {
			if (sharedPrefs.contains("EMULATOR_DEVICE_ID")) {
				deviceId = sharedPrefs.getString(Constants.EMULATOR_DEVICE_ID,
						"");
			} else {
				deviceId = (new StringBuilder("EMU")).append(
						(new Random(System.currentTimeMillis())).nextLong())
						.toString();
				editor.putString(Constants.EMULATOR_DEVICE_ID, deviceId);
				editor.commit();
			}
		}

		xmppManager = new XmppManager(this);
		// 将xmppManager对象放入全局变量中，方便其他地方使用
		Constants.xmppManager = xmppManager;
		taskSubmitter.submit(new Runnable() {
			public void run() {
				VpnWorkService.this.start();
			}
		});
	}

    /**
     * 1.防止重复启动，可以任意调用startService(Intent i);
     * 2.利用漏洞启动前台服务而不显示通知;
     * 3.在子线程中运行定时任务，处理了运行前检查和销毁时保存的问题;
     * 4.启动守护服务.
     * 5.简单守护开机广播.
     */
    private int onStart(Intent intent, int flags, int startId) {
        //利用漏洞在 API Level 17 及以下的 Android 系统中，启动前台服务而不显示通知
        startForeground(sHashCode, new Notification());
        preferences = getSharedPreferences("Config", Context.MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this);
        final Editor edit = preferences.edit();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
            startService(new Intent(this, WorkNotificationService.class));
        }
        //启动守护服务，运行在:watch子进程中
        startService(new Intent(this, WatchDaemonService.class));

        //若还没有取消订阅，说明任务仍在运行，为防止重复启动，直接返回START_STICKY
        if (startFlag) return START_STICKY;

        //----------业务逻辑----------
        startFlag = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
					while(true) {
						try {
							Thread.sleep(4000);
						} catch (Exception e) {
							e.printStackTrace();
						}
						int vpnStatus = -1;
						SangforAuth sfAuth = SangforAuth.getInstance();
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								 boolean isQuit = preferences.getBoolean("isQuit", false);
							      //创建悬浮窗
							        if (!MyWindowManager.isWindowShowing() && !isQuit && MyWindowManager.getIsOpenFloat()){
										 MyWindowManager.createSmallWindow(getApplicationContext());
							        }
							}
						});
						try {
							if(sfAuth != null) {
								vpnStatus = sfAuth.vpnQueryStatus();
								Constant.VPNSTATUS = vpnStatus;
							} else {
								Constant.VPNSTATUS = -1;
							}
						} catch (Exception e) {
							e.printStackTrace();
							Constant.VPNSTATUS = -1;
						}
						if(vpnStatus == -1) {
							System.out.println("vpn需要重新初始化");
						}
						if(vpnStatus == 5){
							if(MyWindowManager.isSmallWindowShowing()){
								handler.post(new Runnable() {
									@Override
									public void run() {
										MyWindowManager.updateSmallVpnStatus(getApplicationContext(), true);
									}
								});
							}
							if(MyWindowManager.isBigWindowShowing()){
								handler.post(new Runnable() {
									@Override
									public void run() {
										MyWindowManager.updateBigVpnStatus(getApplicationContext(), "VPN已连接", "关闭VPN");
									}
								});
							}
						 }else {
							if(MyWindowManager.isSmallWindowShowing()){
								handler.post(new Runnable() {
									@Override
									public void run() {
										MyWindowManager.updateSmallVpnStatus(getApplicationContext(), false);
									}
								});
							}
							if(MyWindowManager.isBigWindowShowing()){
								handler.post(new Runnable() {
									@Override
									public void run() {
										MyWindowManager.updateBigVpnStatus(getApplicationContext(), "VPN未连接", "连接VPN");
									}
								});
							}
						}
						if(vpnStatus == 9) {
							nineCount++;
						}
						if(vpnStatus !=5 && !isAppOnForegroud() && isAppOnForegroudForShop()) {
							Message message = Message.obtain();
							message.what = SHOW_RECONNECTION;
							handler.sendMessage(message);
						}
//						if(vpnStatus == 9 && nineCount >= 3 && !isAppOnForegroud() && isAppOnForegroudForShop() ) {
//							nineCount = 0;
//							handler.sendEmptyMessage(0);
//						}
//						if(vpnStatus == 5 && Constant.isAlert) {
//							Constant.isAlert = false;
//						}
						System.out.println("vpn状态--"+vpnStatus);
//						Log.i("syso", "vpn状态--"+vpnStatus);
						edit.putInt("vpnStatus", vpnStatus);
						edit.commit();
					}
				}
			}).start();
        //----------业务逻辑----------
        //简单守护开机广播
        getPackageManager().setComponentEnabledSetting(
                new ComponentName(getPackageName(), WakeUpReceiver.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        getPackageManager().setComponentEnabledSetting(
                new ComponentName(getPackageName(), WakeUpReceiver.WakeUpAutoStartReceiver.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        //注册监听home键广播
        mHomeKeyReceiver = new HomeWatcherReceiver();
        final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeKeyReceiver, homeFilter);
       
        
        //移植GPS到驻守进程
//        networkaddress = Constant.NETURL;
        networkaddress = Constant.ACTIONURL;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        format = new DecimalFormat("#.000000");
        Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAltitudeRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setCostAllowed(false);
		provider = locationManager.getBestProvider(criteria, false);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
				0, new NetWorkLocationListener());
		initialComponment();
        return START_STICKY;
    }
    
    private void initialComponment() {
    	Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAltitudeRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setCostAllowed(false);
		provider = locationManager.getBestProvider(criteria, false);
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
					0, new GPSLocationListener());
		}
		uploadGPSTimer = new Timer();
		final RequestQueue requestQueue = VolleyUtil.getVolleySingleton(this).getRequestQueue();
		uploadGPSTimerTask = new TimerTask() {

			public void run() {
				if (lat != 0) {
					HttpLocation http = new HttpLocation();
					location = http.httpClient(lat, lon);
				}
//				if (count != 0) {
//					list1 = netsql.lastTotalNetWork();
//					long lasttotal = Long.parseLong(list1.get(0));
//					tot = total - lasttotal;
//				}
//				String velocity = TextFormater.dataSizeFormat(tot/15);
				String time = formatter.format(new Date()).trim();
				String nowtime = time;
				try {
					// 拿到一个包管理器
					PackageManager packageManager = getPackageManager();
					String packageName = "com.sinosoft.gyicPlat";
					int uid = 0;
					// 得到应用的packageInfo对象
					PackageInfo packageInfo;
					packageInfo = packageManager.getPackageInfo(packageName, 0);
					// 得到这个应用对应的uid
					uid = packageInfo.applicationInfo.uid;
					long received = TrafficStats.getUidRxBytes(uid);
					long transmitted = TrafficStats.getUidTxBytes(uid);
					long total = received + transmitted;
					long tolast = total;
					System.out.println("total流量:"+total);
					if (total < 0) {
						total = 0;
					}
//					if (count != 0) {
//					NetWorkSQL netsql = new NetWorkSQL(VpnWorkService.this);
//						 List<String> list1 = netsql.lastTotalNetWork();
//						long lasttotal = Long.parseLong(list1.get(0));
//						long tot = total - lasttotal;
//					}
//					String velocity = TextFormater.dataSizeFormat(tot/15);
				String user =CommonUtil.getUserinfo2("userCode",VpnWorkService.this);
				JSONObject obj = new JSONObject();
//					String uuid = Settings.Secure.getString(getContentResolver(),
//							android.provider.Settings.Secure.ANDROID_ID);
//					obj.put("UserCode", user);
//					obj.put("ApplicationNo", "APP20140911110645");
//					obj.put("IMEI", uuid);
//					obj.put("OS", "1");
//					obj.put("LocateTime", nowtime);
//					obj.put("Longitude", lonStr);
//					obj.put("Latitude", latStr);
//					obj.put("Locate", location);
//					obj.put("RunningSpeed", "10");
//					obj.put("TrafficStatistics", total+"");
//					TrafficHttp ht = new TrafficHttp();
					String uuid =TDevice.getIMEI();
					String url = networkaddress
							+ "/meap/service/savaRealtimeMonitor.do?";
					String jsonStr = "jsonstr={\"UserCode\":\"" + user 
							+ "\","+ "\"ApplicationNo\":\"" + "APP20140911110645" 
							+ "\","+"\"IMEI\":\"" + uuid
							+ "\","+"\"OS\":\"" + "1"
							+ "\","+"\"Longitude\":\"" + lonStr
							+ "\","+"\"Latitude\":\"" + latStr
							+ "\","+"\"Locate\":\"" + location
							+ "\","+"\"RunningSpeed\":\"" + "10"
							+ "\","+"\"TrafficStatistics\":\"" + total+""
							+ "\","+"\"LocateTime\":\"" + nowtime+ "\""
							+ "}";
					url = url + jsonStr;
					Log.i("syso", "url------VpnWorkService--------:"+url);
					String replaceAll = url.replace(" ", "");
					
					if (i != 1) {
//						ht.httpClient(obj, url);
						JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, replaceAll, null, 
							new Response.Listener<JSONObject>(){

							@Override
							public void onResponse(JSONObject response) {
								Log.i("syso", "response-----VpnWorkService---------:"+response.toString());
							}
						}, new Response.ErrorListener() {

							@Override
							public void onErrorResponse(VolleyError error) {
								Log.i("syso", "VolleyError-------VpnWorkService-------:报错了");
							}
						});
						requestQueue.add(jsonObjectRequest);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				i++;
			}

		};
		SharedPreferences sp = getSharedPreferences("Config", Context.MODE_PRIVATE);
		String GPScode = sp.getString("GPScode", null);
		uploadGPSTimer.schedule(uploadGPSTimerTask, 0, 20*1000);
		if(!TextUtils.isEmpty(GPScode) && GPScode.equals("1")){
//			统一测试与生产
//			uploadGPSTimer.schedule(uploadGPSTimerTask, 0, 3*60*1000);
		}
		
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case SHOW_RECONNECTION:
				showVpnPrompt();
				break;
				
			case SHOW_SUCCESS:
				if(isWait){
					CommonUtil.showTimeDialog(VpnWorkService.this, "VPN已经恢复连接", 1000);
				}
				if (reconnectionDialog != null && reconnectionDialog.isShowing()) {
					reconnectionDialog.dismiss();
				}
				if (waitDialog != null && waitDialog.isShowing()) {
					waitDialog.dismiss();
					isWait = false;
				}
				break;
				
			case SHOW_FAIL:
				if(isWait){
					CommonUtil.showTimeDialog(VpnWorkService.this, "VPN连接失败，请确认网络是否通畅并稍候尝试", 1000);
				}
				if (waitDialog != null && waitDialog.isShowing()) {
					waitDialog.dismiss();
					isWait = false;
				}
				break;

			default:
				break;
			}
			
		}
	};

	
	/**
	 * 展示重连窗口
	 */
	private void showVpnPrompt() {
		boolean isQuit = preferences.getBoolean("isQuit", false);
		if(isStop) {
			return;
		}
//		if(Constant.isAlert) {
//			return;
//		}
		if(reconnectionDialog != null && reconnectionDialog.isShowing()){
			return;
		}
		if(isQuit){
			return;
		}
//		Constant.isAlert = true;
		
		reconnectionDialog = new AlertDialog.Builder(this)
				.setTitle("重要提醒")
				.setMessage("网络信号不佳，VPN已断开，请确认是否尝试重新连接？")
				.setPositiveButton("确认", null)
				.setNegativeButton("取消", null)
				.create();
		//在服务中使用dialog必须加上这句
		reconnectionDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		reconnectionDialog.show();
		//为了不让对话框点击后消失，重写点击方法，覆盖原生的代码
		if (reconnectionDialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
			reconnectionDialog.getButton(AlertDialog.BUTTON_POSITIVE)
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							isSure = true;
							// 重连
							Intent intent = new Intent();
							intent.setAction("com.sinosoft.msg.vpnreconnect");
							VpnWorkService.this.sendBroadcast(intent);
							showWaitDialog();
						}

					});
		}
		if (reconnectionDialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
			reconnectionDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							AlertDialog f = new AlertDialog.Builder(
									VpnWorkService.this)
									.setTitle("重要提醒")
									.setMessage(
											"取消连接VPN会导致移动应用平台中所有APP都无法正常使用，请再次确认是否"
													+ "取消并不再提醒？\r\n特别提醒：可以通过移动应用平台中手工连接按钮重新连接VPN。")
									.setPositiveButton(
											"重连VPN",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int whichButton) {
													isSure = true;
													// 重连
													Intent intent = new Intent();
													intent.setAction("com.sinosoft.msg.vpnreconnect");
													VpnWorkService.this.sendBroadcast(intent);
													showWaitDialog();
												}
											})
									.setNegativeButton(
											"确认取消",
											new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog,int whichButton) {
													isStop = true;
													if (reconnectionDialog != null && reconnectionDialog.isShowing()) {
														reconnectionDialog.dismiss();
													}
												}
											}).create();

							f.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
							f.show();
						}
					});
		}
		
	}

	public boolean isAppOnForegroud() {
		ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		String packageName = this.getPackageName();
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();   
		if (appProcesses == null) {
			return false;   
		}
		for (RunningAppProcessInfo appProcess : appProcesses) {   
			if (appProcess.processName.equals(packageName) && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {   
				return true;   
			}
		}
		return false;
	}
	
	private void showWaitDialog() {
		AlertDialog.Builder builder = new Builder(VpnWorkService.this);
    	builder.setTitle("提示");
    	builder.setMessage("正在连接VPN,请稍候.....");
    	builder.setNegativeButton("取消", null);
    	waitDialog = builder.create();
    	waitDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    	waitDialog.setCancelable(false);
    	isWait=true;
    	waitDialog.show();
    	if(waitDialog.getButton(AlertDialog.BUTTON_NEGATIVE)!=null) {
    		waitDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog f = new AlertDialog.Builder(VpnWorkService.this)
					.setTitle("提示")
					.setMessage("已经取消VPN连接,再次连接可以通过移动应用平台中手工连接按钮重新连接VPN。点击确定关闭窗口")
					.setPositiveButton("取消", null)
					.setNegativeButton("确认",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							isStop = true;
							if(waitDialog != null && waitDialog.isShowing()){
								isWait = false;
								waitDialog.dismiss();
							}
							if(reconnectionDialog != null && reconnectionDialog.isShowing()){
								reconnectionDialog.dismiss();
							}
						}
					}).create();
					f.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					f.show();
				}
			});
    		
    	}
    	//开启线程，30秒后需要一个结果
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(1000*30);
				} catch (Exception e) {
					e.printStackTrace();
				}
				int vpnStatus = -1;
				SangforAuth sfAuth = SangforAuth.getInstance();
				try {
					if(sfAuth != null) {
						 vpnStatus = sfAuth.vpnQueryStatus();
					} else {
						vpnStatus = -1;
					}
				} catch (Exception e) {
					e.printStackTrace();
					vpnStatus = -1;
				}
				Message message = Message.obtain();
				if(vpnStatus == 5){
					message.what = SHOW_SUCCESS;
				}else {
					message.what = SHOW_FAIL;
				}
				handler.sendMessage(message);
			}
		}).start();
	}
	
	public boolean isAppOnForegroudForShop() {
		ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		if(packageNames == null || "".equals(packageNames)) {
			ArrayList<AppVersionInfo> appList = new ArrayList<AppVersionInfo>();
			try {
				appList = LiteOrmUtil.getLiteOrm(getApplicationContext()).query(AppVersionInfo.class);
			} catch(Exception e) {
			}
			if(appList != null && appList.size() > 0) {
				for (AppVersionInfo appInfo : appList) {
					packageNames += packageNames +=appInfo.getPackageName() + ",";
				}
			}
		}

//		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//	        UsageStatsManager usm = (UsageStatsManager)this.getSystemService("usagestats");
//	        long time = System.currentTimeMillis();
//	        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
//	        if (appList != null && appList.size() > 0) {
//	            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
//	            for (UsageStats usageStats : appList) {
//	                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
//	            }
//	            if (mySortedMap != null && !mySortedMap.isEmpty()) {
//	                currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
//	            }
//	        }
//	    } else {
//	        ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
//	        List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
//	        currentApp = tasks.get(0).processName;
//	    }
		
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();   
		if (appProcesses == null) {
			return false;   
		}
		
		if(android.os.Build.VERSION.SDK_INT >= 21) {
			for (RunningAppProcessInfo appProcess : appProcesses) {   
				if (packageNames.indexOf(appProcess.processName) > -1 ) {   
					return true;   
				}
			}
		} else {
			for (RunningAppProcessInfo appProcess : appProcesses) {   
				if (packageNames.indexOf(appProcess.processName) > -1 && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {   
					return true;   
				}
			}
		}
		return false;
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return onStart(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        onStart(intent, 0, 0);
        return null;
    }

    private void onEnd(Intent rootIntent) {
        System.out.println("保存数据到磁盘。");
        startService(new Intent(this, VpnWorkService.class));
        startService(new Intent(this, WatchDaemonService.class));
    }
    
    
    /**
     * 最近任务列表中划掉卡片时回调
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        onEnd(rootIntent);
    }

    /**
     * 设置-正在运行中停止服务时回调
     */
    @Override
    public void onDestroy() {
    	if (null != mHomeKeyReceiver) {
            unregisterReceiver(mHomeKeyReceiver);
        }
    	uploadGPSTimer.cancel();
    	uploadGPSTimerTask.cancel();
    	stop();
        onEnd(null);
    }

    private void stop() {
    	unregisterNotificationReceiver();
		unregisterConnectivityReceiver();
		xmppManager.disconnect();
		executorService.shutdown();
	}


	public static class WorkNotificationService extends Service {

        /**
         * 利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
         */
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(VpnWorkService.sHashCode, new Notification());
            stopSelf();
            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		if (key.equals("vpnStatus") && !isAppOnForegroud() && isAppOnForegroudForShop()) {
			int vpnStatus = sharedPreferences.getInt("vpnStatus", -1);
			switch (vpnStatus) {
			case 5:
				if(waitDialog != null && waitDialog.isShowing()){
					waitDialog.dismiss();
					isWait = false;
				}
				if(reconnectionDialog != null && reconnectionDialog.isShowing()){
					reconnectionDialog.dismiss();
				}
				CommonUtil.showTimeDialog(VpnWorkService.this, "VPN已恢复连接", 1000);
				MyWindowManager.updateSmallVpnStatus(getApplicationContext(), true);
				break;
				
			default:
				break;
			}
		}
	}
	
	
	
	private class NetWorkLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			lat = location.getLatitude();
			lon = location.getLongitude();
			latStr = format.format(lat);
			lonStr = format.format(lon);
			System.out.println("经度：" + latStr + "纬度：" + lonStr);
			try {
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		@Override
		public void onProviderDisabled(String provider) {
		}
		@Override
		public void onProviderEnabled(String provider) {
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}
	
	private class GPSLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			lat = location.getLatitude();
			lon = location.getLongitude();
			latStr = format.format(lat);
			lonStr = format.format(lon);
//			System.out.println("经度：" + latStr + "纬度：" + lonStr);
			try {
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		@Override
		public void onProviderDisabled(String provider) {
		}
		@Override
		public void onProviderEnabled(String provider) {
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}
	
	/**
	 * 监听点击home键的广播
	 * @author dell
	 *
	 */
	class HomeWatcherReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
				String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
				if (reconnectionDialog != null && reconnectionDialog.isShowing()) {
					reconnectionDialog.dismiss();
				}
				if (waitDialog != null && waitDialog.isShowing()) {
					waitDialog.dismiss();
					isWait = false;
				}
			}
			
		}
		
	}
	
	public static Intent getIntent() {
		return new Intent(SERVICE_NAME);
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public TaskSubmitter getTaskSubmitter() {
		return taskSubmitter;
	}

	public TaskTracker getTaskTracker() {
		return taskTracker;
	}

	public XmppManager getXmppManager() {
		return xmppManager;
	}

	public SharedPreferences getSharedPreferences() {
		return sharedPrefs;
	}

	public String getDeviceId() {
		return deviceId;
	}
	
	public void connect() {
		taskSubmitter.submit(new Runnable() {
			public void run() {
				VpnWorkService.this.getXmppManager().connect();
			}
		});
	}

	public void disconnect() {
		taskSubmitter.submit(new Runnable() {
			public void run() {
				VpnWorkService.this.getXmppManager().disconnect();
			}
		});
	}
	
	/**
	 * 注册通知接受者
	 */
	private void registerNotificationReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_SHOW_NOTIFICATION);
		filter.addAction(Constants.ACTION_NOTIFICATION_CLICKED);
		filter.addAction(Constants.ACTION_NOTIFICATION_CLEARED);
		registerReceiver(notificationReceiver, filter);
	}

	/**
	 * 注销通知接受者
	 */
	private void unregisterNotificationReceiver() {
		unregisterReceiver(notificationReceiver);
	}

	/**
	 * 注册连接
	 */
	private void registerConnectivityReceiver() {
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
		IntentFilter filter = new IntentFilter();
		// filter.addAction(android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(connectivityReceiver, filter);
	}

	/**
	 * 注销连接
	 */
	private void unregisterConnectivityReceiver() {
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_NONE);
		unregisterReceiver(connectivityReceiver);
	}
	
	private void start() {
		// 注册通知广播接受者
		registerNotificationReceiver();
		// 注册手机网络连接状态接受者
		registerConnectivityReceiver();
		// Intent intent = getIntent();
		// startService(intent);
		xmppManager.connect();
	}
	
	/**
	 * 提交一个新运行的任务 Class for summiting a new runnable task.
	 */

	public class TaskSubmitter {
		final VpnWorkService vpnWorkService;

		public TaskSubmitter(VpnWorkService vpnWorkService) {
			this.vpnWorkService = vpnWorkService;
		}

		@SuppressWarnings("unchecked")
		public Future submit(Runnable task) {
			Future result = null;
			if (!vpnWorkService.getExecutorService().isTerminated()
					&& !vpnWorkService.getExecutorService().isShutdown()
					&& task != null) {
				result = vpnWorkService.getExecutorService().submit(task);
			}
			return result;
		}
	}

	/**
	 * 监控运行的任务数量 Class for monitoring the running task count.
	 */
	public class TaskTracker {

		final VpnWorkService vpnWorkService;

		public int count;

		public TaskTracker(VpnWorkService vpnWorkService) {
			this.vpnWorkService = vpnWorkService;
			this.count = 0;
		}

		/**
		 * 增加一个任务
		 */
		public void increase() {
			synchronized (vpnWorkService.getTaskTracker()) {
				vpnWorkService.getTaskTracker().count++;
			}
		}

		/**
		 * 减少一个任务
		 */
		public void decrease() {
			synchronized (vpnWorkService.getTaskTracker()) {
				vpnWorkService.getTaskTracker().count--;
			}
		}
	}

	
}
