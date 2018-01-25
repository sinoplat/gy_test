package com.sinosoft.mobileshop.activity;

import java.io.File;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sinosoft.common.log.Log;
import com.sinosoft.getPhoneNumberUtils.SIMCardInfo;
import com.sinosoft.gyicPlat.MainActivity;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.bean.AppVersionInfo;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.Des;
import com.sinosoft.mobileshop.util.DownLoadManager;
import com.sinosoft.mobileshop.util.PermissionUtils;
import com.sinosoft.mobileshop.util.TDevice;
import com.sinosoft.mobileshop.util.VolleyUtil;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.progressdialog.RollProgressbar;
import com.sinosoft.util.Utils;

/**
 * 应用启动界面
 * 
 */
public class WelcomeActivity extends Activity {
	private final int UPDATA_NONEED = 0;
	private final int UPDATA_CLIENT = 1;
	private final int GET_UNDATAINFO_ERROR = 2;
	private final int SDCARD_NOMOUNTED = 3;
	private final int DOWN_ERROR = 4;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final View view = View.inflate(this, R.layout.activity_welcome, null);
		setContentView(view);
		
		// 渐变展示启动屏
		AlphaAnimation aa = new AlphaAnimation(0.5f, 1.0f);
		aa.setDuration(800);
		view.startAnimation(aa);
		aa.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				if(!Utils.isNetConnect()) {
					AlertDialog.Builder builder=new AlertDialog.Builder(WelcomeActivity.this);  //先得到构造器
			        builder.setTitle("提示"); //设置标题
			        builder.setMessage("当前设备没有开启任何网络，请开启后重新启动移动应用平台。"); //设置内容
			        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
			            @Override
			            public void onClick(DialogInterface dialog, int which) {
			            	finish();
			            }
			        });
			        builder.create().show();				
				} else {
//						checkNewVersion();
					if(Build.VERSION.SDK_INT>=23){
						PermissionUtils.requestMultiPermissions(WelcomeActivity.this, mPermissionGrant);
						redirectTo();
					}else {
						redirectTo();
					}
//						startAsyncUninstallAPKTask();
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		});
		
		
		
	}
	
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATA_NONEED:
				Toast.makeText(getApplicationContext(), "当前版本为最新版本",
						Toast.LENGTH_SHORT).show();
				redirectTo();
			case GET_UNDATAINFO_ERROR:
				//服务器超时   
	            Toast.makeText(getApplicationContext(), "获取服务器更新信息失败", 1).show(); 
	            redirectTo();
				break;
			case DOWN_ERROR:
				//下载apk失败  
	            Toast.makeText(getApplicationContext(), "下载新版本失败", 1).show(); 
	            redirectTo();
				break;
			}
		}
	};
	private AsyncUninstallAPKTask mAsyncUninstallAPKTask;
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	
	/**
	 * 检测新的版本
	 * @param applicationNo 
	 */
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
								String fileName =response.getString("FileName");
								String filePath =response.getString("FilePath");
								showDownload(filePath, fileName);
							}else {
								Toast.makeText(getApplicationContext(), "当前版本为最新版本",Toast.LENGTH_SHORT).show();
								redirectTo();
							}
						}else{
							CommonUtil.showToast(WelcomeActivity.this, "获取版本信息失败，无版本信息");
							redirectTo();
						}
					}else {
						CommonUtil.showToast(WelcomeActivity.this, "获取版本信息失败，"+response.getString("Desc"));
						redirectTo();
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
				redirectTo();
				CommonUtil.showToast(WelcomeActivity.this, "获取版本信息失败，网络或服务器异常，请稍后重试");
			}
		});
		requestQueue.add(jsonObjectRequest);
	}
	
	
	
	private void showDownload(String filePath, String fileName) {
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
	                WelcomeActivity.this.finish();
	            } catch (Exception e) {  
	                Message msg = new Message();  
	                msg.what = DOWN_ERROR;  
	                handler.sendMessage(msg);  
	                e.printStackTrace();  
	            }  
	        }}.start(); 
		
	};
	
	private PermissionUtils.PermissionGrant mPermissionGrant = new PermissionUtils.PermissionGrant() {
        @Override
        public void onPermissionGranted(int requestCode) {
        		redirectTo();
        }
    };

	/**
	 * 跳转到...
	 */
	private void redirectTo() {
		String readTxtFile = CommonUtil.ReadTxtFile();
		if(TextUtils.isEmpty(readTxtFile)){
			Intent intent = new Intent(this, RegisterActivity.class);
			startActivity(intent);
			finish();
		}else {
			userLogin();
		}
	}

	/**
	 * 用户登录
	 */
	@SuppressWarnings("unused")
	private void userLogin() {
		if (!Utils.isNetConnect()) {
			CommonUtil.showToast(this, "手机网络异常，请稍后再试");
			return;
		}
		try {
			JSONObject jsonObject = new JSONObject(CommonUtil.ReadTxtFile());
			if(jsonObject == null ){
				Intent intent = new Intent(this, RegisterActivity.class);
				startActivity(intent);
				finish();
			}
			final String userCode = jsonObject.getString("userCode");
			final String userPassWord = jsonObject.getString("userPassWord");
			String nativePhoneNumber = new SIMCardInfo(this).getNativePhoneNumber();
			String phoneID = TDevice.getIMEI();
			
		String jsonStr = "jsonstr={\"UserCode\":\"" + userCode 
				+ "\","+ "\"UserPassWord\":\"" + userPassWord 
				+ "\","+ "\"PhoneID\":\"" + phoneID+ "\""
				+ "}";
		String url = Constant.LOGIN + jsonStr;
		final RollProgressbar rollProgressbar = CommonUtil.showProgressbarDialog(this,
				"正在登录，请稍后......", true);
		RequestQueue requestQueue = VolleyUtil.getVolleySingleton(this).getRequestQueue();
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
				Request.Method.GET, url, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							String code = response.getString("ResultCode");
							final String string = response.getString("Desc");
							if(code.equals("1")){//成功
								new CommonUtil().saveUserInfoToFile(userCode, userPassWord);
								if(userPassWord.equals("0000")){
									Intent intent = new Intent(WelcomeActivity.this, ReActivationAndCancelActivity.class);
									startActivity(intent);
									finish();
								}else {
									Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
									startActivity(intent);
									finish();
								}
							}else {
									AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
									builder.setTitle("提示");
									builder.setMessage("登录失败，"+string);
									builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											if(string.equals("该设备已注销，请先进行注册。")){
												Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
												startActivity(intent);
												finish();
											}else{
												finish();
											}
										}
									});
									builder.create().show();
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
						CommonUtil.showToast(WelcomeActivity.this,"登录失败，网络或服务器异常，请稍后重试");
						finish();
					}
				});
				jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(1000*60, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,  DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
				requestQueue.add(jsonObjectRequest);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 开始异步任务
	 */
	private void startAsyncUninstallAPKTask() {

		// stop task
		stopAsyncUninstallAPKTask();
		// start task
		if (mAsyncUninstallAPKTask == null) {
			mAsyncUninstallAPKTask = new AsyncUninstallAPKTask();
		}
		mAsyncUninstallAPKTask.execute();

	}
	
	
	/**
	 * 结束异步任务
	 */
	private void stopAsyncUninstallAPKTask() {

		if (mAsyncUninstallAPKTask != null) {
			if (mAsyncUninstallAPKTask.isCancelled() == false) {
				mAsyncUninstallAPKTask.cancel(true);
			}
			mAsyncUninstallAPKTask = null;
		}
	}
	
	
	/**
	 * 卸载app
	 * @author dell
	 *
	 */
	private class AsyncUninstallAPKTask extends AsyncTask<String, Void, String>{
		private RollProgressbar showProgressbarDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgressbarDialog = CommonUtil.showProgressbarDialog(WelcomeActivity.this, "正在卸载app,请稍候......", false);
		}

		@Override
		protected String doInBackground(String... params) {
			List<AppVersionInfo> installApp = CommonUtil.getInstallApp(WelcomeActivity.this);
			if (installApp == null || installApp.size() == 0  ) {
				TDevice.uninstallDataAPPBySilent(Constant.GYICPACKAGE);
				return "";
			}
			
			if(installApp != null && installApp.size()>0){
        		for (int i = 0; i < installApp.size(); i++) {
        			AppVersionInfo appVersionInfo = installApp.get(i);
//        			TDevice.uninstallApk(WelcomeActivity.this, appVersionInfo.getPackageName());
        			TDevice.uninstallDataAPPBySilent(appVersionInfo.getPackageName());
				}
        	}
//        	TDevice.uninstallApk(WelcomeActivity.this, Constant.GYICPACKAGE);
			TDevice.uninstallDataAPPBySilent(Constant.GYICPACKAGE);
			return "aaa";
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (TextUtils.isEmpty(result)) {
				if(showProgressbarDialog!=null && showProgressbarDialog.ISSHOW){
					showProgressbarDialog.disProgressBar();
				}
				WelcomeActivity.this.finish();
				return;
			}

			Toast.makeText(getApplicationContext(), "卸载完成", 2000).show();
			if(showProgressbarDialog!=null && showProgressbarDialog.ISSHOW){
				showProgressbarDialog.disProgressBar();
			}

		}
		
	}
}
