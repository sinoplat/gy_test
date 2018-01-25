package com.sinosoft.mobileshop.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sinosoft.gyicPlat.MainActivity;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.appwidget.TimeButton;
import com.sinosoft.mobileshop.base.BaseActivity;
import com.sinosoft.mobileshop.util.CommonNetWorkUtil;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.TDevice;
import com.sinosoft.mobileshop.util.VolleyUtil;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.progressdialog.RollProgressbar;
import com.sinosoft.util.Utils;

public class MaintainPhoneNoActivity extends BaseActivity {

	private EditText manager_employeeNo;
	private EditText adjuster_employeeCode;
	private EditText adjuster_employeePhoneNo;
	private EditText employeePhoneNo_new;
	private TimeButton getVerificationNo;
	private Button adjustPhoneNo;
	private Button addPhoneNo;
	private EditText verification_No;
	private String type;
	private LinearLayout maintain_verificationNo_ll;
	private LinearLayout maintain_adjuster_ll;
	private EditText maintain_employeePhoneNo;
	private TextView employeeNo_tv;
	private TextView adjuster_employeePhoneNo_tv;
	private TextView employeePhoneNo_new_tv;
	private TextView adjuster_employeeCode_tv;
	private TextView manage_employeePhoneNo_tv;

	@Override
	protected int getLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_maintainphoneno;
	}

	@Override
	protected boolean hasBackButton() {
		return true;
	}
	
	@Override
	protected void init(Bundle savedInstanceState) {
		super.init(savedInstanceState);
		getVerificationNo = (TimeButton) findViewById(R.id.maintain_getVerificationNo);
		getVerificationNo.onCreate(savedInstanceState);
		getVerificationNo.setTextAfter("秒后重新获取").setTextBefore("点击获取验证码").setLenght(30 * 1000);
	}
	

	@Override
	public void initView() {
		setTitleBar("手机号维护", true);
		type = getIntent().getStringExtra("Type");
		manager_employeeNo = (EditText) findViewById(R.id.maintain_employeeNo);
		adjuster_employeePhoneNo = (EditText) findViewById(R.id.maintain_adjuster_employeePhoneNo);
		employeePhoneNo_new = (EditText) findViewById(R.id.maintain_employeePhoneNo_new);
		adjuster_employeeCode = (EditText) findViewById(R.id.maintain_adjuster_employeeCode);
		verification_No = (EditText) findViewById(R.id.maintain_verificationNo);
		maintain_employeePhoneNo = (EditText) findViewById(R.id.maintain_manage_employeePhoneNo);
		employeeNo_tv = (TextView) findViewById(R.id.maintain_employeeNo_tv);
		adjuster_employeePhoneNo_tv = (TextView) findViewById(R.id.maintain_adjuster_employeePhoneNo_tv);
		employeePhoneNo_new_tv = (TextView) findViewById(R.id.maintain_employeePhoneNo_new_tv);
		adjuster_employeeCode_tv = (TextView) findViewById(R.id.maintain_adjuster_employeeCode_tv);
		manage_employeePhoneNo_tv = (TextView) findViewById(R.id.maintain_manage_employeePhoneNo_tv);
		
		
		adjustPhoneNo = (Button) findViewById(R.id.maintain_adjustPhoneNo);
		addPhoneNo = (Button) findViewById(R.id.maintain_addPhoneNo);
		maintain_adjuster_ll = (LinearLayout) findViewById(R.id.maintain_adjuster_ll);
		maintain_verificationNo_ll = (LinearLayout) findViewById(R.id.maintain_verificationNo_ll);
		if(!TextUtils.isEmpty(type)){
			if(type.equals("1")){//维护自己手机号
				maintain_verificationNo_ll.setVisibility(View.VISIBLE);
				maintain_adjuster_ll.setVisibility(View.GONE);
				maintain_employeePhoneNo.setVisibility(View.GONE);
				manage_employeePhoneNo_tv.setVisibility(View.GONE);
				employeeNo_tv.setVisibility(View.VISIBLE);
				adjuster_employeePhoneNo_tv.setVisibility(View.VISIBLE);
				employeePhoneNo_new_tv.setVisibility(View.VISIBLE);
//				String userCode = CommonUtil.getUserinfo("userCode", this);
//				String userPhoneNo = CommonUtil.getUserinfo("userPhoneNo", this);
				employeeNo_tv.setText("员工工号:");
				adjuster_employeePhoneNo_tv.setText("手机号:");
				employeePhoneNo_new_tv.setText("新手机号:");
//				manager_employeeNo.setText(userCode);
//				adjuster_employeePhoneNo.setText(userPhoneNo);
			}else if(type.equals("0")){//维护他人手机号
				maintain_verificationNo_ll.setVisibility(View.GONE);
				maintain_adjuster_ll.setVisibility(View.VISIBLE);
				maintain_employeePhoneNo.setVisibility(View.VISIBLE);
				employeeNo_tv.setText("管理工号:");
				manage_employeePhoneNo_tv.setText("管理电话:");
				adjuster_employeeCode_tv.setText("员工工号:");
				adjuster_employeePhoneNo_tv.setText("员工电话:");
				employeePhoneNo_new_tv.setText("新电话:");
			}
		}
	
	}
	
	@Override
	protected void onDestroy() {
		getVerificationNo.onDestroy();
		super.onDestroy();
	}

	@Override
	public void initData() {
		getVerificationNo.setOnClickListener(this);
		adjustPhoneNo.setOnClickListener(this);
		addPhoneNo.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.maintain_getVerificationNo:
			getVerificationNo();
			break;

		case R.id.maintain_adjustPhoneNo:
			String managerEmployeeNo1 = manager_employeeNo.getText().toString().trim();
			String adjusterEmployeeCode1 = adjuster_employeeCode.getText().toString().trim();
			String adjusterEmployeePhoneNo1 = adjuster_employeePhoneNo.getText().toString().trim();
			String employeePhoneNoNew1 = employeePhoneNo_new.getText().toString().trim();
			String verificationNo1 =verification_No.getText().toString().trim();
			String managerPhoneNo1= maintain_employeePhoneNo.getText().toString().trim();
			if(type.equals("1")){//维护自己手机号
				if( TextUtils.isEmpty(managerEmployeeNo1)
						|| TextUtils.isEmpty(adjusterEmployeePhoneNo1)
						|| TextUtils.isEmpty(employeePhoneNoNew1)
						|| TextUtils.isEmpty(verificationNo1)){
					CommonUtil.showDialog(this, "请将相关信息填写完整").show();
				}else {
					showAdjustPhoneNoDialog();
				}
			}else if(type.equals("0")){
				if(TextUtils.isEmpty(managerEmployeeNo1) 
						|| TextUtils.isEmpty(adjusterEmployeeCode1)
						|| TextUtils.isEmpty(adjusterEmployeePhoneNo1)
						|| TextUtils.isEmpty(employeePhoneNoNew1)
						|| TextUtils.isEmpty(managerPhoneNo1)
						){
					CommonUtil.showDialog(this, "请将相关信息填写完整").show();
				}else {
					showAdjustPhoneNoDialog();
				}
			}
			
			break;

		case R.id.maintain_addPhoneNo:
			String managerEmployeeNo = manager_employeeNo.getText().toString().trim();
			String adjusterEmployeeCode = adjuster_employeeCode.getText().toString().trim();
			String adjusterEmployeePhoneNo = adjuster_employeePhoneNo.getText().toString().trim();
			String employeePhoneNoNew = employeePhoneNo_new.getText().toString().trim();
			String verificationNo =verification_No.getText().toString().trim();
			String managerPhoneNo= maintain_employeePhoneNo.getText().toString().trim();
			if(type.equals("1")){//维护自己手机号
				if( TextUtils.isEmpty(managerEmployeeNo)
						|| TextUtils.isEmpty(adjusterEmployeePhoneNo)
						|| TextUtils.isEmpty(employeePhoneNoNew)
						|| TextUtils.isEmpty(verificationNo)){
					CommonUtil.showDialog(this, "请将相关信息填写完整").show();
				}else {
					showAddPhoneNoDialog();
				}
			}else if(type.equals("0")){
				if(TextUtils.isEmpty(managerEmployeeNo) 
						|| TextUtils.isEmpty(adjusterEmployeeCode)
						|| TextUtils.isEmpty(adjusterEmployeePhoneNo)
						|| TextUtils.isEmpty(employeePhoneNoNew)
						|| TextUtils.isEmpty(managerPhoneNo)
						){
					CommonUtil.showDialog(this, "请将相关信息填写完整").show();
				}else {
					showAddPhoneNoDialog();
				}
			}
			break;

		default:
			break;
		}
	}

	/**
	 * 获取验证码
	 */
	private void getVerificationNo() {
		
		if(!Utils.isNetConnect()) {
			CommonUtil.showToast(this, "手机网络异常，请检测或重新开启");
			return;
		}
		
		String managerEmployeeNo = manager_employeeNo.getText().toString().trim();
		String adjusterEmployeeCode = adjuster_employeeCode.getText().toString().trim();
		String adjusterEmployeePhoneNo = adjuster_employeePhoneNo.getText().toString().trim();
		String employeePhoneNoNew = employeePhoneNo_new.getText().toString().trim();
		if(type.equals("1")){//维护自己手机号
			if(TextUtils.isEmpty(managerEmployeeNo) || TextUtils.isEmpty(adjusterEmployeePhoneNo)||TextUtils.isEmpty(employeePhoneNoNew)){
				CommonUtil.showDialog(this, "请将相关信息填写完整").show();
			}else{
				getVerificationNo.setIsOk(true);
				new CommonNetWorkUtil(this).getVerificationNo(managerEmployeeNo, adjusterEmployeePhoneNo);
			}
		}
	}

	private void showAddPhoneNoDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("提示");
		String message = "您确定将新手机号码添加为该员工工号的绑定号码？";
		builder.setMessage(message);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				addNewPhone();
			}
			
		});
		builder.setNegativeButton("取消", null);
		builder.create().show();
	}
	
	/**
	 * 添加新的手机号
	 */
	private void addNewPhone() {
		if (!Utils.isNetConnect()) {
			CommonUtil.showToast(this, "手机网络异常，无法添加新的手机号");
			return;
		}
		String managerEmployeeNo = manager_employeeNo.getText().toString().trim();
		String adjusterEmployeeCode = adjuster_employeeCode.getText().toString().trim();
		String adjusterEmployeePhoneNo = adjuster_employeePhoneNo.getText().toString().trim();
		String employeePhoneNoNew = employeePhoneNo_new.getText().toString().trim();
		String verificationNo =verification_No.getText().toString().trim();
		String managerPhoneNo= maintain_employeePhoneNo.getText().toString().trim();
		String jsonStr;
		String url = null;
		if(type.equals("1")){//维护自己手机号
			if( TextUtils.isEmpty(managerEmployeeNo)
					|| TextUtils.isEmpty(adjusterEmployeePhoneNo)
					|| TextUtils.isEmpty(employeePhoneNoNew)
					|| TextUtils.isEmpty(verificationNo)){
				CommonUtil.showDialog(this, "请将相关信息填写完整").show();
				return;
			}
			jsonStr =  "jsonstr={\"UserCode\":\"" + managerEmployeeNo 
					+ "\","+ "\"UserPhoneNo\":\"" + adjusterEmployeePhoneNo 
					+ "\","+ "\"UserNewPhoneNo\":\"" + employeePhoneNoNew
					+ "\","+ "\"IdentificationNo\":\"" + verificationNo
					+ "\","+ "\"PhoneID\":\"" + TDevice.getIMEI()
					+ "\","+ "\"PhoneModel\":\"" + TDevice.getPhoneType()
					+ "\","+"\"Type\":\"1\""
					+ "}";
			url = Constant.MAINTAINPHONENOSELF + jsonStr;
		}else if(type.equals("0")){
			if(TextUtils.isEmpty(managerEmployeeNo) 
					|| TextUtils.isEmpty(adjusterEmployeeCode)
					|| TextUtils.isEmpty(adjusterEmployeePhoneNo)
					|| TextUtils.isEmpty(employeePhoneNoNew)
					|| TextUtils.isEmpty(managerPhoneNo)
					){
				CommonUtil.showDialog(this, "请将相关信息填写完整").show();
				return;
			}
			jsonStr =  "jsonstr={\"AdminCode\":\"" + managerEmployeeNo 
					+ "\","+ "\"AdminPhone\":\"" + managerPhoneNo 
					+ "\","+ "\"UserCode\":\"" + adjusterEmployeeCode 
					+ "\","+ "\"UserPhoneNo\":\"" + adjusterEmployeePhoneNo
					+ "\","+ "\"NewPhoneNo\":\"" + employeePhoneNoNew
					+ "\","+ "\"PhoneID\":\"" + TDevice.getIMEI()
					+ "\","+ "\"PhoneModel\":\"" + TDevice.getPhoneType()
					+ "\","+"\"Type\":\"1\""
					+ "}";
			url = Constant.MAINTAINPHONENOMANAGER + jsonStr;
		}
		String replace = url.replace(" ", "");
		final RollProgressbar rollProgressbar = CommonUtil.showProgressbarDialog(this,
				"添加新的手机号，请稍后......", true);
		RequestQueue requestQueue = VolleyUtil.getVolleySingleton(this).getRequestQueue();
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
				Request.Method.GET, replace, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						try {
							String code = response.getString("ResultCode");
							if(code.equals("1")){//成功
								CommonUtil.showToast(MaintainPhoneNoActivity.this, "添加新的手机号成功");
								//更新本地信息
							
							}else {//
								CommonUtil.showToast(MaintainPhoneNoActivity.this, "添加新的手机号失败，"+response.getString("Desc"));
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
						CommonUtil.showToast(MaintainPhoneNoActivity.this,
								"添加新的手机号失败，网络或服务器异常，请稍后重试");

					}
				});
		requestQueue.add(jsonObjectRequest);
	}

	private void showAdjustPhoneNoDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("提示");
		String message = "您确定将新手机号码调整为该员工工号的绑定号码？";
		builder.setMessage(message);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				adjustPhone();
			}

		});
		builder.setNegativeButton("取消", null);
		builder.create().show();
	}
	
	/**
	 * 调整手机号
	 */
	private void adjustPhone() {
		if (!Utils.isNetConnect()) {
			CommonUtil.showToast(this, "手机网络异常，无法调整手机号");
			return;
		}
		String managerEmployeeNo = manager_employeeNo.getText().toString().trim();
		String adjusterEmployeeCode = adjuster_employeeCode.getText().toString().trim();
		final String adjusterEmployeePhoneNo = adjuster_employeePhoneNo.getText().toString().trim();
		final String employeePhoneNoNew = employeePhoneNo_new.getText().toString().trim();
		String verificationNo =verification_No.getText().toString().trim();
		String managerPhoneNo= maintain_employeePhoneNo.getText().toString().trim();
		String jsonStr;
		String url = null;
		if(type.equals("1")){//维护自己手机号
			if( TextUtils.isEmpty(managerEmployeeNo)
					|| TextUtils.isEmpty(adjusterEmployeePhoneNo)
					|| TextUtils.isEmpty(employeePhoneNoNew)
					|| TextUtils.isEmpty(verificationNo)){
				CommonUtil.showDialog(this, "请将相关信息填写完整").show();
				return;
			}
			jsonStr = "jsonstr={\"UserCode\":\"" + managerEmployeeNo + "\","
					+ "\"UserPhoneNo\":\"" + adjusterEmployeePhoneNo + "\","
					+ "\"UserNewPhoneNo\":\"" + employeePhoneNoNew + "\","
					+ "\"IdentificationNo\":\"" + verificationNo + "\","
					+ "\"PhoneID\":\"" + TDevice.getIMEI() + "\","
					+ "\"PhoneModel\":\"" + TDevice.getPhoneType() + "\","
					+ "\"Type\":\"0\"" + "}";
			url = Constant.MAINTAINPHONENOSELF + jsonStr;
		}else if(type.equals("0")){
			if(TextUtils.isEmpty(managerEmployeeNo) 
					|| TextUtils.isEmpty(adjusterEmployeeCode)
					|| TextUtils.isEmpty(adjusterEmployeePhoneNo)
					|| TextUtils.isEmpty(employeePhoneNoNew)
					|| TextUtils.isEmpty(managerPhoneNo)
					){
				CommonUtil.showDialog(this, "请将相关信息填写完整").show();
				return;
			}
			jsonStr =  "jsonstr={\"AdminCode\":\"" + managerEmployeeNo 
					+ "\","+ "\"AdminPhone\":\"" + managerPhoneNo 
					+ "\","+ "\"UserCode\":\"" + adjusterEmployeeCode
					+ "\","+ "\"UserPhoneNo\":\"" + adjusterEmployeePhoneNo
					+ "\","+ "\"NewPhoneNo\":\"" + employeePhoneNoNew
					+ "\","+"\"Type\":\"0\""
					+ "}";
			url = Constant.MAINTAINPHONENOMANAGER + jsonStr;
		}
		String replace = url.replace(" ", "");
		final RollProgressbar rollProgressbar = CommonUtil.showProgressbarDialog(this,
				"调整手机号，请稍后......", true);
		RequestQueue requestQueue = VolleyUtil.getVolleySingleton(this).getRequestQueue();
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
				Request.Method.GET, replace, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						try {
							String code = response.getString("ResultCode");
							if(code.equals("1")){//成功
								CommonUtil.showToast(MaintainPhoneNoActivity.this, "调整手机号成功");
								if(type.equals("1")){
									JSONObject jsonObject = new JSONObject(CommonUtil.ReadTxtFile());
									String string = jsonObject.getString("userName");
									//更新本地信息
									Intent intent = new Intent(MaintainPhoneNoActivity.this, RegisterActivity.class);
									startActivity(intent);
								}
							}else {//
								CommonUtil.showToast(MaintainPhoneNoActivity.this, "调整手机号失败，"+response.getString("Desc"));
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
						CommonUtil.showToast(MaintainPhoneNoActivity.this,
								"调整手机号失败，网络或服务器异常，请稍后重试");
					}
				});
		requestQueue.add(jsonObjectRequest);
	}
}
