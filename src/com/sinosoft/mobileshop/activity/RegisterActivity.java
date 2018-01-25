package com.sinosoft.mobileshop.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sinosoft.getPhoneNumberUtils.SIMCardInfo;
import com.sinosoft.gyicPlat.MainActivity;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.base.BaseActivity;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.PermissionUtils;
import com.sinosoft.mobileshop.util.TDevice;
import com.sinosoft.mobileshop.util.VolleyUtil;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.progressdialog.RollProgressbar;
import com.sinosoft.util.Utils;

public class RegisterActivity extends BaseActivity{
	
	private EditText et_employeeNo;
	private EditText et_employeeName;
//	private EditText et_employeePhoneNo;
//	private EditText et_verificationNo;
	private Button activation;
//	private TimeButton getVerificationNo;

	@Override
	protected int getLayoutId() {
		return R.layout.activity_register;
	}
	
	@Override
	protected boolean hasBackButton() {
		// TODO Auto-generated method stub
		return false;
	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.register_activation://登录
			if(Build.VERSION.SDK_INT >23){
				PermissionUtils.requestPermission(RegisterActivity.this, PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE, mPermissionGrant);
			}else {
				doActivation();
			}
			break;
			
//		case R.id.register_getVerificationNo://获取验证码
//			doGetVerificationNo();
//			break;

		default:
			break;
		}
		
	}
	
	private PermissionUtils.PermissionGrant mPermissionGrant = new PermissionUtils.PermissionGrant() {
        @Override
        public void onPermissionGranted(int requestCode) {
            switch (requestCode) {
                case PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE:
                	doActivation();
                    break;
                case PermissionUtils.CODE_READ_EXTERNAL_STORAGE:
                    break;
            }
        }
    };

	/**
	 * 登录
	 */
	private void doActivation() {
		final String employeeNo = et_employeeNo.getText().toString();
		final String employeeName = et_employeeName.getText().toString();
//		final String employeePhoneNo = et_employeePhoneNo.getText().toString();
//		final String verificationNo = et_verificationNo.getText().toString();
		if(!Utils.isNetConnect()) {
			CommonUtil.showToast(this, "手机网络异常，请检测或重新开启");
			return;
		}
		if(TextUtils.isEmpty(employeeNo) || TextUtils.isEmpty(employeeName)){
			CommonUtil.showDialog(this, "请将相关信息填写完整").show();
			return;
		}
		String jsonStr = "jsonstr={\"UserCode\":\"" + employeeNo 
				+ "\","+ "\"UserPassWord\":\"" + employeeName 
				+ "\","+"\"UserPhoneNo\":\"" + new SIMCardInfo(this).getNativePhoneNumber()
				+ "\","+"\"PhoneModel\":\"" + TDevice.getPhoneType()
				+ "\","+"\"PhoneID\":\"" + TDevice.getIMEI()+ "\""
				+ "}";
		final String url = Constant.ACTIVATION + jsonStr;
		final RollProgressbar rollProgressbar = CommonUtil.showProgressbarDialog(this, "正在登录，请稍后......", false);
		RequestQueue requestQueue = VolleyUtil.getVolleySingleton(this).
				getRequestQueue();
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, 
			new Response.Listener<JSONObject>(){

			@Override
			public void onResponse(JSONObject response) {
				try {
					String Code = response.getString("ResultCode");
					if(Code.equals("1")){
						new CommonUtil().saveUserInfoToFile(employeeNo, employeeName);
						if(employeeName.equals("0000")){
							AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
							builder.setTitle("提示");
							builder.setMessage("登录成功，用户首次登录需修改初始密码，修改后下次可直接使用");
							builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Intent intent = new Intent(RegisterActivity.this, ReActivationAndCancelActivity.class);
									startActivity(intent);
									finish();
								}
							});
							builder.setCancelable(false);
							builder.create().show();
						}else {
							Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
							startActivity(intent);
							finish();
						}
					}else {
						CommonUtil.showToast(RegisterActivity.this, "用户登录失败，"+response.getString("Desc"));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
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
				CommonUtil.showToast(RegisterActivity.this, "登录失败，网络或服务器异常，请稍后重试");
			
			}
		});
		jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(1000*60, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,  DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		requestQueue.add(jsonObjectRequest);
	}
	
	@Override
	protected void init(Bundle savedInstanceState) {
		super.init(savedInstanceState);
//		getVerificationNo = (TimeButton) findViewById(R.id.register_getVerificationNo);
//		getVerificationNo.onCreate(savedInstanceState);
//		getVerificationNo.setTextAfter("秒后重新获取").setTextBefore("获取验证码").setLenght(30 * 1000);
	}

	@Override
	public void initView() {
		setTitleBar("用户登录", false);
		et_employeeNo = (EditText) findViewById(R.id.register_employeeNo);
		et_employeeName = (EditText) findViewById(R.id.register_employeeName);
//		et_employeePhoneNo = (EditText) findViewById(R.id.register_employeePhoneNo);
//		et_verificationNo = (EditText) findViewById(R.id.register_verificationNo);
		activation = (Button) findViewById(R.id.register_activation);
		activation.setOnClickListener(this);
//		getVerificationNo.setOnClickListener(this);
//		et_employeeName.setOnFocusChangeListener(new OnFocusChangeListener() {
//			
//			@Override
//			public void onFocusChange(View v, boolean hasFocus) {
//				if(hasFocus){
//					String trim = et_employeeNo.getText().toString().trim();
//					if (!TextUtils.isEmpty(trim)) {
//						getUserName(trim);
//					}else {
//						CommonUtil.showToast(RegisterActivity.this, "输入员工工号后会自动带出姓名");
//					}
//				}
//			}
//		});
		
	}
	
	/**
	 * 获取员工姓名
	 * @param userCode
	 */
	private void getUserName(String  userCode) {
		if(!Utils.isNetConnect()) {
			CommonUtil.showToast(this, "手机网络异常，无法获取员工姓名");
			return;
		}
		String jsonStr = "jsonstr={\"UserCode\":\"" + userCode + "\""+ "}";
		String url = Constant.GET_USERNAME + jsonStr;
		final RollProgressbar rollProgressbar = CommonUtil.showProgressbarDialog(this, "正在获取员工姓名，请稍后......", false);
		RequestQueue requestQueue = VolleyUtil.getVolleySingleton(this).getRequestQueue();
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, 
			new Response.Listener<JSONObject>(){

			@Override
			public void onResponse(JSONObject response) {
				try {
					String Code = response.getString("ResultCode");
					if(Code.equals("1")){//成功
						String userName = response.getString("UserName");
						et_employeeName.setText(userName);
					}else {//失败
						CommonUtil.showToast(RegisterActivity.this, "获取员工姓名失败，"+response.getString("Desc"));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
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
				CommonUtil.showToast(RegisterActivity.this, "获取员工姓名失败，网络或服务器异常，请稍后重试");
			}
		});
		requestQueue.add(jsonObjectRequest);
		
	}
	
	@Override
	protected void onDestroy() {
//		getVerificationNo.onDestroy();
		super.onDestroy();
	}

	@Override
	public void initData() {
		String nativePhoneNumber = new SIMCardInfo(this).getNativePhoneNumber();
		if(!TextUtils.isEmpty(nativePhoneNumber)){
//			et_employeePhoneNo.setText(nativePhoneNumber);
//			et_employeePhoneNo.setEnabled(false);
		}
		
	}

}
