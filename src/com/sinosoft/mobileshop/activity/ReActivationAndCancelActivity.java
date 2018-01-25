package com.sinosoft.mobileshop.activity;

import org.apache.commons.net.ftp.parser.RegexFTPFileEntryParserImpl;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.appwidget.TimeButton;
import com.sinosoft.mobileshop.base.BaseActivity;
import com.sinosoft.mobileshop.util.AppManager;
import com.sinosoft.mobileshop.util.CommonNetWorkUtil;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.TDevice;
import com.sinosoft.mobileshop.util.VolleyUtil;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.progressdialog.RollProgressbar;
import com.sinosoft.util.Utils;

public class ReActivationAndCancelActivity extends BaseActivity {

	private EditText userName;
	private EditText re_passWord_new;
	private EditText passWord_new;
	private EditText passWord_old;
	private Button reactivition;
	private String isCancel = "0";
	private LinearLayout reactivation_new_ll;

	@Override
	protected int getLayoutId() {
		return R.layout.activity_reactivation_cancel;
	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.reactivition_Reactivition:
			Reactivition();
			break;
			
		default:
			break;
		}
	}
	
	/**
	 * 取消注册和激活
	 */
	protected void cancelActivition() {
		if (!Utils.isNetConnect()) {
			CommonUtil.showToast(this, "手机网络异常，无法添加新的手机号");
			return;
		}
		
		String employeeNoOld = userName.getText().toString().trim();
		String employeeNameOld = passWord_old.getText().toString().trim();
		String phoneNoOld = passWord_new.getText().toString().trim();
		
		if (TextUtils.isEmpty(employeeNoOld)
				|| TextUtils.isEmpty(employeeNameOld)
				|| TextUtils.isEmpty(phoneNoOld))

		{
			CommonUtil.showDialog(this, "请将相关信息填写完整").show();
			return;
		}
		
		String jsonStr =  "jsonstr={\"UserCode\":\"" + employeeNoOld 
				+ "\","+ "\"UserPhoneNo\":\"" + phoneNoOld 
				+ "\","+ "\"PhoneID\":\"" + TDevice.getIMEI()+ "\""
				+ "}";
		String url = Constant.CANCELACTIVITION + jsonStr;
		String replace = url.replace(" ", "");
		final RollProgressbar rollProgressbar = CommonUtil.showProgressbarDialog(this,
				"正在取消用户，请稍后......", true);
		RequestQueue requestQueue = VolleyUtil.getVolleySingleton(this).getRequestQueue();
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
				Request.Method.GET, replace, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						try {
							String code = response.getString("ResultCode");
							if(code.equals("1")){//成功
								CommonUtil.showToast(ReActivationAndCancelActivity.this, "取消用户成功");
								//更新本地信息
								CommonUtil.deleteDirectory(Constant.USERINFO_PATH);
								AppManager.getAppManager().finishAllActivity();
								Intent intent = new Intent(ReActivationAndCancelActivity.this, RegisterActivity.class);
								startActivity(intent);
							}else {//
								CommonUtil.showToast(ReActivationAndCancelActivity.this, "取消用户失败，"+response.getString("Desc"));
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
						CommonUtil.showToast(ReActivationAndCancelActivity.this,
								"添加新的手机号失败，网络或服务器异常，请稍后重试");

					}
				});
		requestQueue.add(jsonObjectRequest);
		
	}

	/**
	 * 重新注册和激活
	 */
	private void Reactivition() {
		if (!Utils.isNetConnect()) {
			CommonUtil.showToast(this, "手机网络异常，无法添加新的手机号");
			return;
		}
		String userNamestr = userName.getText().toString().trim();
		String passWord_oldstr = passWord_old.getText().toString().trim();
		String passWord_newstr = passWord_new.getText().toString().trim();
		String re_passWord_newstr = re_passWord_new.getText().toString().trim();
		
		if (TextUtils.isEmpty(userNamestr)
				|| TextUtils.isEmpty(passWord_oldstr)
				|| TextUtils.isEmpty(passWord_newstr)
				|| TextUtils.isEmpty(re_passWord_newstr))

		{
			CommonUtil.showDialog(this, "请将相关信息填写完整").show();
			return;
		}

		if (!passWord_newstr.equals(re_passWord_newstr)) {
			CommonUtil.showDialog(this, "两次密码输入不一致").show();
			return;
		}

		if (passWord_newstr.equals("0000") || re_passWord_newstr.equals("0000")) {
			CommonUtil.showDialog(this, "新密码不能与初始密码一致").show();
			return;
		}
		
		String jsonStr =  "jsonstr={\"UserCode\":\"" + userNamestr 
				+ "\","+ "\"NewPassWord\":\"" + passWord_newstr 
				+ "\","+ "\"OldPassWord\":\"" + passWord_oldstr
				+ "\","+ "\"IMEI\":\"" + TDevice.getIMEI()
				+ "\","+ "\"OS\":\"" + "1"+ "\""
				+ "}";
		String url = Constant.UPDATEPASSWORD + jsonStr;
		String replace = url.replace(" ", "");
		final RollProgressbar rollProgressbar = CommonUtil.showProgressbarDialog(this,
				"正在修改密码，请稍后......", true);
		RequestQueue requestQueue = VolleyUtil.getVolleySingleton(this).getRequestQueue();
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
				Request.Method.GET, replace, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						try {
							String code = response.getString("ResultCode");
							if(code.equals("1")){//成功
								AlertDialog.Builder builder = new AlertDialog.Builder(ReActivationAndCancelActivity.this);
								builder.setTitle("提示");
								builder.setMessage("修改密码成功，请用新密码再次登录，下次可直接使用");
								builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Intent intent = new Intent(ReActivationAndCancelActivity.this, RegisterActivity.class);
										startActivity(intent);
										finish();
									}
								});
								builder.setCancelable(false);
								builder.create().show();
							}else {//
								AlertDialog.Builder builder = new AlertDialog.Builder(ReActivationAndCancelActivity.this);
								builder.setTitle("提示");
								builder.setMessage("修改密码失败，"+response.getString("Desc"));
								builder.setPositiveButton("确定",null);
								builder.setCancelable(false);
								builder.create().show();
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
						CommonUtil.showToast(ReActivationAndCancelActivity.this,
								"修改密码失败，网络或服务器异常，请稍后重试");
					}
				});
		requestQueue.add(jsonObjectRequest);
	}



	@Override
	protected boolean hasBackButton() {
		return true;
	}

	@Override
	public void initView() {
		setTitleBar("修改密码", false);
		reactivation_new_ll = (LinearLayout) findViewById(R.id.reactivation_new_ll);
		userName = (EditText) findViewById(R.id.reactivition_employeeNo_old);//用户名
		passWord_old = (EditText) findViewById(R.id.reactivition_employeeName_old);//原密码
		passWord_new = (EditText) findViewById(R.id.reactivition_phoneNo_old);//新密码
		re_passWord_new = (EditText) findViewById(R.id.reactivition_employeeNo_new);//新密码确认
		reactivition = (Button) findViewById(R.id.reactivition_Reactivition);//修改密码
		reactivition.setOnClickListener(this);
		userName.setText(new CommonUtil().getUserinfo2("userCode", this));
		userName.setEnabled(false);
	}
	

	@Override
	public void initData() {
		
	}

}
