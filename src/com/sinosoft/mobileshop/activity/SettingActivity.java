package com.sinosoft.mobileshop.activity;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.sinosoft.contact.UpdataContactAndSms;
import com.sinosoft.getPhoneNumberUtils.SIMCardInfo;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.appwidget.dialog.HomeIconCountDialog;
import com.sinosoft.mobileshop.appwidget.floatwindow.MyWindowManager;
import com.sinosoft.mobileshop.base.BaseActivity;
import com.sinosoft.mobileshop.bean.AppVersionInfo;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.DownLoadManager;
import com.sinosoft.mobileshop.util.LiteOrmUtil;
import com.sinosoft.mobileshop.util.TDevice;
import com.sinosoft.mobileshop.util.VolleyUtil;
import com.sinosoft.phoneGapPlugins.download.MainActivity;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.progressdialog.RollProgressbar;
import com.sinosoft.util.Utils;
import com.way.pattern.App;

/**
 * 设置页面
 */
public class SettingActivity extends BaseActivity implements
		OnSharedPreferenceChangeListener {

	private LinearLayout settingDownload;
	private LinearLayout settingCheckNew;
	private LinearLayout settingModifyPass;
	private LinearLayout settingGesturePass;
	private LinearLayout settingGestureLock;
	private LinearLayout settingLinkerBackup;
	private LinearLayout settingLinkerRecover;
	private LinearLayout settingHelp;
	private LinearLayout settingQuit;
	private TextView versionNo;
	private ImageView downloadSwtich;
	private ImageView gestureLockSwtich;
	private LinearLayout is_open_float_ll;
	private ImageView is_open_float_iv;
//	private LinearLayout maintain_PhoneNo;
	private LinearLayout setting_Reactivation;
//	private LinearLayout setting_Reactivation_cancel;
	private LinearLayout setting_icon_count;

	private String downloadSet;
	private String gestureSet;

	private boolean isBackup = true;

	@Override
	protected int getLayoutId() {
		return R.layout.activity_setting;
	}

	@Override
	protected boolean hasBackButton() {
		return true;
	}

	@Override
	public void initView() {
		setTitleBar("设置", true);
		settingDownload = (LinearLayout) findViewById(R.id.setting_download);
		settingCheckNew = (LinearLayout) findViewById(R.id.setting_checknew);
		settingModifyPass = (LinearLayout) findViewById(R.id.setting_modify_passord);
		settingGesturePass = (LinearLayout) findViewById(R.id.setting_gesture_pass);
		settingGestureLock = (LinearLayout) findViewById(R.id.setting_gesture_pass_lock);
		settingLinkerBackup = (LinearLayout) findViewById(R.id.setting_linker_backup);
		settingLinkerRecover = (LinearLayout) findViewById(R.id.setting_linker_recover);
		settingHelp = (LinearLayout) findViewById(R.id.setting_help);
		settingQuit = (LinearLayout) findViewById(R.id.setting_quit);
		is_open_float_ll = (LinearLayout) findViewById(R.id.setting_is_open_float_ll);
//		maintain_PhoneNo = (LinearLayout) findViewById(R.id.setting_maintain_PhoneNo);
		setting_Reactivation = (LinearLayout) findViewById(R.id.setting_Reactivation);
//		setting_Reactivation_cancel = (LinearLayout) findViewById(R.id.setting_Reactivation_cancel);
		setting_icon_count = (LinearLayout) findViewById(R.id.setting_icon_count);
		
		setting_device_management = (LinearLayout) findViewById(R.id.setting_device_management);

		versionNo = (TextView) findViewById(R.id.versionno);
		downloadSwtich = (ImageView) findViewById(R.id.setting_download_switch);
		gestureLockSwtich = (ImageView) findViewById(R.id.setting_guesture_lockswitch);
		is_open_float_iv = (ImageView) findViewById(R.id.setting_is_open_float_iv);

		settingDownload.setOnClickListener(this);
		settingCheckNew.setOnClickListener(this);
		settingModifyPass.setOnClickListener(this);
		settingGesturePass.setOnClickListener(this);
		settingGestureLock.setOnClickListener(this);
		settingLinkerBackup.setOnClickListener(this);
		settingLinkerRecover.setOnClickListener(this);
		settingHelp.setOnClickListener(this);
		settingQuit.setOnClickListener(this);
		is_open_float_ll.setOnClickListener(this);
//		maintain_PhoneNo.setOnClickListener(this);
		setting_Reactivation.setOnClickListener(this);
//		setting_Reactivation_cancel.setOnClickListener(this);
		setting_icon_count.setOnClickListener(this);
		setting_device_management.setOnClickListener(this);
		getSharedPreferences("Config", Context.MODE_PRIVATE)
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setFloatImage(MyWindowManager.getIsOpenFloat());
	}

	@Override
	public void initData() {
		versionNo.setText(TDevice.getVersionName() + "");
		downloadSet = App.get("DownloadSetvalue", "1");
		gestureSet = App.get("GestureSetvalue", "0");

		setDownloadImage();
		setGustureLockImage();
	}

	@Override
	public void onClick(View view) {
		Intent intent = new Intent();
		switch (view.getId()) {
		case R.id.setting_download:
			if ("0".equals(downloadSet)) {
				App.set("DownloadSetvalue", "1");
			} else if ("1".equals(downloadSet)) {
				App.set("DownloadSetvalue", "0");
			}
			downloadSet = App.get("DownloadSetvalue", "0");
			setDownloadImage();
			break;
		case R.id.setting_device_management:
			intent.setClass(this, DeviceManagementActivity.class);
			startActivity(intent);
			break;
			
		case R.id.setting_checknew:
//			checkNewVersion();
			doGetVersion();
			break;
		case R.id.setting_modify_passord:
			showToast("功能暂关闭");
			break;
		case R.id.setting_icon_count:
			showIconCountDialog2();
			break;
		case R.id.setting_gesture_pass:
			showToast("功能暂关闭");
			// gestureSet = App.get("GestureSetvalue", "0");
			// if("0".equals(gestureSet)) {
			// Intent intent = new Intent(SettingActivity.this,
			// GuideGesturePasswordActivity.class);
			// startActivity(intent);
			// }
			break;
		case R.id.setting_gesture_pass_lock:
			showToast("功能暂关闭");
			// if("0".equals(gestureSet)) {
			// App.set("GestureSetvalue", "1");
			// } else if("1".equals(gestureSet)) {
			// App.set("GestureSetvalue", "0");
			// }
			// gestureSet = App.get("GestureSetvalue", "0");
			// setGustureLockImage();
			break;
		case R.id.setting_linker_backup:
			backup();
			break;
		case R.id.setting_linker_recover:
			restore();
			break;
		case R.id.setting_help:
			
			break;
		case R.id.setting_quit:
			showDialog(SettingActivity.this,
					"移动应用平台退出之后，其他接入应用将不能继续访问，您确定要退出吗？");
			break;

		case R.id.setting_is_open_float_ll:
			isOpenFloat();
			break;

//		case R.id.setting_maintain_PhoneNo:
//			showTwoButtonDialog();
//			break;

		case R.id.setting_Reactivation:
			String nativePhoneNumber = new SIMCardInfo(this).getNativePhoneNumber();
			if (TextUtils.isEmpty(nativePhoneNumber)) {
				intent.putExtra("type", "1");
				intent.setClass(this, ReActivationAndCancelActivity.class);
				startActivity(intent);
			} else {
				CommonUtil.showDialog(this, "该手机号码已经明确，不能重新更换注册信息").show();
			}
			break;

//		case R.id.setting_Reactivation_cancel:
//			intent.putExtra("type", "2");
//			intent.setClass(this, ReActivationAndCancelActivity.class);
//			startActivity(intent);
//			break;
		default:
			break;
		}
	}

	
	private void showIconCountDialog2() {
		final HomeIconCountDialog.Builder customBuilder = new
				HomeIconCountDialog.Builder(this);
            customBuilder.setTitle("设置主页图标个数")
                .setMessage("提示:每行最少1个图标，最多5个图标")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	String trim = customBuilder.getIconCount().getText().toString().trim();
                    	if(!TextUtils.isEmpty(trim)){
                    		int iconCount = Integer.valueOf(trim);
                    		if(iconCount <1 || iconCount>5){
                    			CommonUtil.showDialog(SettingActivity.this, "每行最少1个图标，最多5个图标").show();
                    		}else {
                    			getSharedPreferences("Config", Context.MODE_PRIVATE)
								.edit().putInt("numColumns", iconCount)
								.commit();
                    			CommonUtil.showToast(SettingActivity.this, "主页图标修改成功");
                    		}
                    	}else {
                    		CommonUtil.showDialog(SettingActivity.this, "请输入主页图标个数").show();
                    	}
                    	
                        dialog.dismiss();
                    }
                });
            HomeIconCountDialog homeIconCountDialog = customBuilder.create();
            Window dialogWindow = homeIconCountDialog.getWindow();
            WindowManager m = getWindowManager();
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高度
            WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
                    p.height = (int) (d.getHeight() * 0.35); // 高度设置为屏幕的0.6，根据实际情况调整
            p.width = (int) (d.getWidth() * 0.85); // 宽度设置为屏幕的0.65，根据实际情况调整
            homeIconCountDialog.show();
           
	}

	/**
     * 
     */
	private void showTwoButtonDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				SettingActivity.this);
		builder.setTitle("提示");
		builder.setMessage("请选择你要进行的操作");
		// 第一个按钮
		builder.setPositiveButton("维护自己手机号", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Intent intent = new Intent(SettingActivity.this, MaintainPhoneNoActivity.class);
				intent.putExtra("Type", "1");//维护自己手机号
				startActivity(intent);
			}
		});
		// 中间的按钮
		builder.setNeutralButton("维护他人手机号", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
						String AdminCode = getSharedPreferences("Config",Context.MODE_PRIVATE).getString("AdminCode",null);
						if (!TextUtils.isEmpty(AdminCode)
								&& AdminCode.equals("1")) {
							Intent intent = new Intent(SettingActivity.this, MaintainPhoneNoActivity.class);
							intent.putExtra("Type", "0");//维护他人手机号
							startActivity(intent);
						} else {
							CommonUtil.showDialog(SettingActivity.this, "该工号不具有管理员权限，无法使用此功能")
									.show();
						}
			}
		});
		// 第三个按钮
		builder.setNegativeButton("取消", null);
		// Diglog的显示
		builder.create().show();
	}

	/**
	 * 展示设置主页图标显示个数对话框
	 */
	private void showIconCountDialog() {
		final EditText inputServer = new EditText(this);
		inputServer.setFocusable(true);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("设置主页图标个数").setMessage("提示：每行至少一个图标，最多5个图标")
				.setView(inputServer).setNegativeButton("取消", null)
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						String inputName = inputServer.getText().toString();
						int parseInt = Integer.parseInt(inputName);
						if (parseInt < 1) {
							CommonUtil.showToast(SettingActivity.this,
									"图表个数不能少余1个");
						} else if (parseInt > 5) {
							CommonUtil.showToast(SettingActivity.this,
									"图表个数不能多余5个");
						} else {
							getSharedPreferences("Config", Context.MODE_PRIVATE)
									.edit().putInt("numColumns", parseInt)
									.commit();
						}
					}
				});
		builder.show();
	}

	/**
	 * 设置是否打开悬浮窗
	 */
	private void isOpenFloat() {
		boolean isOpenFloat = MyWindowManager.getIsOpenFloat();
		if (isOpenFloat) {
			MyWindowManager.closeFloat(getApplicationContext());
			MyWindowManager.setIsOpenFloat(false);
		} else {
			MyWindowManager.createSmallWindow(getApplicationContext());
		}
		setFloatImage(!isOpenFloat);
	}

	public void showDialog(Context context, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context); // 先得到构造器
		builder.setTitle("提示"); // 设置标题
		builder.setMessage(msg); // 设置内容
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { // 设置确定按钮
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Editor edit = getSharedPreferences("Config", Context.MODE_PRIVATE).edit();
						edit.putBoolean("isQuit", true);
						edit.commit();
						App.set("set_quit", "1");
						finish();
						Intent intent = new Intent();
						intent.setAction("com.sinosoft.msg.quit");
						MyWindowManager.closeFloat(getApplicationContext());
						SettingActivity.this.sendBroadcast(intent);
					}
				});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { // 设置取消按钮
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		// 参数都设置完成了，创建并显示出来
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void setFloatImage(boolean isOpenFloat) {
		if (isOpenFloat) {
			is_open_float_iv.setImageResource(R.drawable.on);
		} else {
			is_open_float_iv.setImageResource(R.drawable.off);
		}
	}

	private void setDownloadImage() {
		if ("0".equals(downloadSet)) {
			downloadSwtich.setImageResource(R.drawable.off);
		} else if ("1".equals(downloadSet)) {
			downloadSwtich.setImageResource(R.drawable.on);
		}
	}

	private void setGustureLockImage() {
		if ("0".equals(gestureSet)) {
			gestureLockSwtich.setImageResource(R.drawable.off);
		} else if ("1".equals(gestureSet)) {
			gestureLockSwtich.setImageResource(R.drawable.on);
		}
	}

	
	private void checkNewVersion() {
		if(!Utils.isNetConnect()) {
			CommonUtil.showToast(this, "手机网络异常，请检测或重新开启");
			return;
		}
		String jsonStr = "jsonstr={\"ApplicationNo\":\"" + Constant.GYICPACKAGE 
				+ "\","+ "\"PhoneNo\":\"" + "18831614093"
				+ "\","+ "\"OS\":\"" + "1" 
				+ "\","+ "\"CurrentVersion\":\"" + TDevice.getVersionName()
				+ "\","+ "\"OptPackageName\":\"" +  Constant.GYICPACKAGE 
				+ "\","+ "\"OptUserCode\":\"" + "0000000000" 
				+ "\","+ "\"UserCode\":\"" + "0000000000"+ "\""
				+ "}";
		String url = Constant.GETVERSIONS+jsonStr;
		final RollProgressbar rollProgressbar = CommonUtil.showProgressbarDialog(this, "获取版本信息，请稍后......", true);
		RequestQueue requestQueue = VolleyUtil.getVolleySingleton(this).getRequestQueue();
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, 
			new Response.Listener<JSONObject>(){
			
			@Override
			public void onResponse(JSONObject response) {
				String Code;
				try {
					Code = response.getString("ResultCode");
					if(Code.equals("1")){
						String applicationNewVersion =response.getString("NewCurrentVersion");
						if(!TextUtils.isEmpty(applicationNewVersion)){
							if(!applicationNewVersion.equals(TDevice.getVersionName())){
//								getNewVersion(applicationNewVersion,appVersionInfo.getApplicationNo());
								String fileName =response.getString("FileName");
								String filePath =response.getString("FilePath");
								String downLoadUrl =response.getString("DownLoadUrl");
								showDownload(filePath, fileName,downLoadUrl);
							}else {
								Toast.makeText(SettingActivity.this, "当前版本为最新版本",Toast.LENGTH_SHORT).show();
							}
						}
					}else {
						CommonUtil.showToast(SettingActivity.this, "获取版本信息失败，"+response.getString("Desc"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}finally{
					if (rollProgressbar != null) {
						rollProgressbar.disProgressBar();
					}
				}
			}


		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				if (rollProgressbar != null) {
					rollProgressbar.disProgressBar();
				}
				CommonUtil.showToast(SettingActivity.this, "获取版本信息失败，网络或服务器异常，请稍后重试");
			}
		});
		requestQueue.add(jsonObjectRequest);
		
	}
	
	private void showDownload(String filePath, String fileName,
			String downLoadUrl) {
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		String localFile = "";
		File sdDir = null;
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
		}
		if(sdDir != null) {
			localFile = sdDir.getPath() + "/" + fileName + ".apk";
		} else {
			localFile = "/" + fileName + ".apk";
		}
		
		final ProgressDialog pd;    //进度条对话框  
	    pd = new  ProgressDialog(this);  
	    pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
	    pd.setMessage("正在下载更新");  
	    pd.show();  
	    pd.setCancelable(false);
	    new Thread(){  
	        @Override  
	        public void run() {  
	            try {  
	                File file = DownLoadManager.getFileFromServer(Constant.GETAPK, pd); 
	                Utils.installApk(file);
	                pd.dismiss(); //结束掉进度条对话框  
	            } catch (Exception e) {  
	                e.printStackTrace();  
					CommonUtil.showToast(SettingActivity.this, "获取版本信息失败，网络或服务器异常，请稍后重试");
	            }  
	        }}.start(); 
	}

	/**
	 * 获取版本
	 * 
	 * @param context
	 * @param appVersionInfo
	 */
	private void doGetVersion() {
		QueryBuilder<AppVersionInfo> builder = new QueryBuilder<AppVersionInfo>(
				AppVersionInfo.class);
		builder.whereEquals("PackageName", Constant.GYICPACKAGE);
		ArrayList<AppVersionInfo> appInfos = LiteOrmUtil.getLiteOrm(
				getApplicationContext()).query(builder);
		AppVersionInfo appVersionInfo = null;
		if (appInfos != null && appInfos.size() > 0) {
			appVersionInfo = appInfos.get(0);
		}
		if (appVersionInfo == null) {
			return;
		}
		String newVersion = appVersionInfo.getApplicationNewVersion();
		String applicationNo = appVersionInfo.getApplicationNo();
		if (newVersion != null && !"".equals(newVersion)
				&& !newVersion.equals(TDevice.getVersionName())) {
			final RollProgressbar rollProgressbar = CommonUtil.showProgressbarDialog(
					SettingActivity.this, "移动应用平台有新版本，正在获取下载资源", true);
			String jsonStr = "jsonstr={\"UserCode\":\""+"0000000000"+"\",\"OptUserCode\":\""+"0000000000"+"\",\"OptPackageName\":\"\","
					+ "\"OS\":\"1\","
					+ "\"CurrentVersion\":\""
					+ newVersion
					+ "\","
					+ "\"ApplicationNo\":\""
					+ applicationNo
					+ "\""
					+ "}";
			String url = Constant.GETVERSIONS + jsonStr;
			RequestQueue mRequestQueue = VolleyUtil.getVolleySingleton(
					getApplicationContext()).getRequestQueue();
			JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
					Request.Method.GET, url, null,
					new Response.Listener<JSONObject>() {
						public void onResponse(JSONObject response) {
							try {
								String filePath = response
										.getString("FilePath");
								String fileName = response
										.getString("FileName");
								showDownload(filePath, fileName);
							} catch (JSONException e) {
								e.printStackTrace();
							} finally {
								if (rollProgressbar != null) {
									rollProgressbar.disProgressBar();
								}
							}
						};
					}, new Response.ErrorListener() {
						public void onErrorResponse(VolleyError error) {
							if (rollProgressbar != null) {
								rollProgressbar.disProgressBar();
							}
							CommonUtil.showToast(SettingActivity.this,
									"网络或服务器异常，请检查");
						};
					});

			mRequestQueue.add(jsonObjectRequest);
		} else {
			CommonUtil.showToast(SettingActivity.this, "当前已是最新版本");
		}

	}

	/**
	 * 获取远程文件
	 * 
	 * @param remoteFile
	 */
	private void showDownload(String remoteFile, String fileName) {
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		String localFile = "";
		File sdDir = null;
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
		}
		if (sdDir != null) {
			localFile = sdDir.getPath() + "/" + fileName + ".apk";
		} else {
			localFile = "/" + fileName + ".apk";
		}
		Intent i = new Intent();
		i.putExtra("remoteFile", remoteFile);
		i.putExtra("localFile", localFile);
		i.setClass(SettingActivity.this, MainActivity.class);
		startActivity(i);

		App.set("localFile", localFile);
	}

	private void backup() {
		isBackup = true;
		UpdataContactAndSms ucas = new UpdataContactAndSms();
		try {
			ucas.execute("updataContact", getApplicationContext(), pHandler);
			ucas.execute("UpdataSms", getApplicationContext(), pHandler);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void restore() {
		isBackup = false;
		UpdataContactAndSms ucas = new UpdataContactAndSms();
		try {
			ucas.execute("restoreContact", getApplicationContext(), pHandler);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private Handler pHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Bundle b = msg.getData();
			String resultCode = b.getString("result");
			if (isBackup) {
				if ("1".equals(resultCode)) {
					showToast("备份成功");
				} else {
					showToast("备份失败");
				}
			} else {
				if ("1".equals(resultCode)) {
					showToast("恢复成功");
				} else {
					showToast("恢复失败");
				}
			}
		};
	};
	private LinearLayout setting_device_management;

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (!TextUtils.isEmpty(key) && key.equals("isOpenFloat")) {
			boolean boolean1 = sharedPreferences.getBoolean(key, true);
			setFloatImage(boolean1);
		}
	}

}
