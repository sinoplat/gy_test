package com.sinosoft.mobileshop.activity;

import java.text.SimpleDateFormat;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sangfor.ssl.SangforAuth;
import com.sinosoft.gyicPlat.MainActivity;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.appwidget.floatwindow.MyWindowManager;
import com.sinosoft.mobileshop.base.BaseActivity;
import com.sinosoft.mobileshop.bean.AppMessage;
import com.sinosoft.mobileshop.bean.Prpmregistoruser;
import com.sinosoft.mobileshop.util.AppManager;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.DateUtil;
import com.sinosoft.mobileshop.util.JsonUtil;
import com.sinosoft.mobileshop.util.TDevice;
import com.sinosoft.mobileshop.util.VolleyUtil;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.progressdialog.RollProgressbar;

/**
 * 设备管理
 * @author qianchunzheng
 *
 */
public class DeviceManagementActivity  extends BaseActivity {
	
	
	private ListView listView;
	private List<Prpmregistoruser> prpmregistorusers;
	private Myadapter myadapter;

	@Override
	protected int getLayoutId() {
		
		return R.layout.activity_devicemanagement;
	}

	@Override
	public void onClick(View v) {
		
	}
	
	@Override
	protected boolean hasBackButton() {
		return true;
	}

	@Override
	public void initView() {
		setTitleBar("设备管理", true);
		listView = (ListView) findViewById(R.id.devicManagement_listview);
//		myadapter = new Myadapter();
//		listView.setAdapter(myadapter);
	}
	
	class Myadapter extends BaseAdapter{
		
		private List<Prpmregistoruser> prpmregistoruserList;
		
		public Myadapter(List<Prpmregistoruser> prpmregistoruserList) {
			this.prpmregistoruserList = prpmregistoruserList;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return prpmregistoruserList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return prpmregistoruserList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(DeviceManagementActivity.this);
			View inflate = inflater.inflate(R.layout.item_devicemanagement, null);
			Prpmregistoruser prpmregistoruser = prpmregistoruserList.get(position);
			TextView itme_userName =(TextView) inflate.findViewById(R.id.itme_userName);
			TextView itme_userCode =(TextView) inflate.findViewById(R.id.itme_userCode);
			TextView itme_phoneName =(TextView) inflate.findViewById(R.id.itme_phoneName);
			TextView item_checkTime =(TextView) inflate.findViewById(R.id.item_checkTime);
			final TextView item_imei = (TextView) inflate.findViewById(R.id.item_imei);
			Button item_unBind =(Button) inflate.findViewById(R.id.item_unBind);
			itme_userName.setText(prpmregistoruser.getUserName());
			itme_phoneName.setText(prpmregistoruser.getRemark());
			itme_userCode.setText(prpmregistoruser.getUserCode());
			item_checkTime.setText(prpmregistoruser.getRegisttime());
			item_imei.setText(prpmregistoruser.getImei());
			item_unBind.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
//					unbindDevice(item_imei.getText().toString().trim(),position);
					showUnbindDeviceDialog(item_imei.getText().toString().trim(),position);
				}
				
			});
			
			
			return inflate;
		}
		
	}
	
	/**
	 * 展示解绑设备时的提示信息
	 * @param trim
	 * @param position
	 */
	private void showUnbindDeviceDialog(final String trim, final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("提示");
		if(trim.equals(TDevice.getIMEI())){
			builder.setMessage("您要解绑的是当前设备，解绑后需要重新登录方可使用，确定要解绑吗？");
		}else {
			builder.setMessage("解绑后需要重新登录方可使用，确定要解绑吗？");
		}
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				unbindDevice(trim, position);
			}
		});
		
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		builder.setCancelable(false);
		builder.create().show();
	}
	
	/**
	 * 解绑设备
	 */
	private void unbindDevice(final String imei,final int position) {
		
		try {
			JSONObject jsonObject = new JSONObject(CommonUtil.ReadTxtFile());
			final String userCode = jsonObject.getString("userCode");
			String jsonStr =  "jsonstr={\"userCode\":\"" + userCode 
					+ "\","+ "\"imei\":\"" + imei+ "\""
					+ "}";
			String url = Constant.UNBINDDEVICE + jsonStr;
			final RollProgressbar rollProgressbar = CommonUtil.showProgressbarDialog(this,
					"正在进行解绑设备，请稍后......", true);
			RequestQueue requestQueue = VolleyUtil.getVolleySingleton(this).getRequestQueue();
			JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
					Request.Method.GET, url, null,
					new Response.Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							try {
								String code = response.getString("resultCode");
								if(code.equals("1")){//成功
									if(imei.equals(TDevice.getIMEI())){//如果解绑的是当前设备，需要退出系统
//											if(SangforAuth.getInstance() != null && (Constant.VPNSTATUS != 2 && Constant.VPNSTATUS !=5 )) {
												AlertDialog.Builder builder = new AlertDialog.Builder(DeviceManagementActivity.this);
												builder.setTitle("提示");
												builder.setMessage("当前设备解绑成功，将退出系统");
												builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
													
													@Override
													public void onClick(DialogInterface dialog, int which) {
														SharedPreferences preferences = getSharedPreferences("Config", Context.MODE_PRIVATE);
														preferences.edit().putBoolean("isQuit", true).commit();
														MyWindowManager.closeFloat(getApplicationContext());
														if(SangforAuth.getInstance() != null) {
											                SangforAuth.getInstance().vpnLogout();
														}
														AppManager.getAppManager().finishAllActivity();
														android.os.Process.killProcess(android.os.Process.myPid());
														System.exit(0); 
													}
												});
												builder.setCancelable(false);
												builder.create().show();
//										}
									}else {
										prpmregistorusers.remove(position);
										if(prpmregistorusers != null && prpmregistorusers.size()>0){
											myadapter.notifyDataSetChanged();
										}else {
											CommonUtil.showToast(DeviceManagementActivity.this,"该设备已无绑定账号");
										}
									}
								}else {//
									CommonUtil.showToast(DeviceManagementActivity.this,"设备解绑失败失败，"+response.getString("data"));
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
							CommonUtil.showToast(DeviceManagementActivity.this,"设备解绑失败失败，网络或服务器异常，请稍后重试");
						}
					});
			jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(1000*60*3, 
	                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 
	                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
			requestQueue.add(jsonObjectRequest);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	}

	@Override
	public void initData() {
		try {
			if(prpmregistorusers != null && prpmregistorusers.size()>0){
				prpmregistorusers.clear();
			}
			JSONObject jsonObject = new JSONObject(CommonUtil.ReadTxtFile());
			final String userCode = jsonObject.getString("userCode");
			String jsonStr = "jsonstr={\"userCode\":\"" + userCode + "\""
					+ "}";
			String url = Constant.GETDEVICELIST + jsonStr;
			final RollProgressbar rollProgressbar = CommonUtil.showProgressbarDialog(this,
					"正在获取列表，请稍后......", true);
			RequestQueue requestQueue = VolleyUtil.getVolleySingleton(this).getRequestQueue();
			JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
					Request.Method.GET, url, null,
					new Response.Listener<JSONObject>() {

						@Override
						public void onResponse(JSONObject response) {
							try {
								String code = response.getString("resultCode");
								if(code.equals("1")){//成功
									prpmregistorusers = JsonUtil.strToBeanList(response.getString("data"), Prpmregistoruser.class);
									myadapter = new Myadapter(prpmregistorusers);
									listView.setAdapter(myadapter);
									myadapter.notifyDataSetChanged();
								}else {//
									CommonUtil.showToast(DeviceManagementActivity.this,"获取设备列表失败，"+response.getString("data"));
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
							CommonUtil.showToast(DeviceManagementActivity.this,"获取设备列表失败，网络或服务器异常，请稍后重试");
						}
					});
					requestQueue.add(jsonObjectRequest);
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 

}
