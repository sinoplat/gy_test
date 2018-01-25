package com.sinosoft.mobileshop.util;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sinosoft.common.log.Log;
import com.sinosoft.mobileshop.activity.AdvicesActivity;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.progressdialog.RollProgressbar;

import android.content.Context;


public class CommonNetWorkUtil {

	private Context context;
	private RequestQueue requestQueue;
	
	public CommonNetWorkUtil(Context context) {
		this.context = context;
		requestQueue = VolleyUtil.getVolleySingleton(context).getRequestQueue();
	}

	
	/**
	 * 获取验证码
	 * @param userCode  员工工号
	 * @param phoneNo   员工电话
	 */
	public void getVerificationNo(String userCode,String phoneNo) {
		String jsonStr = "jsonstr={\"UserCode\":\"" + userCode 
				+ "\","+ "\"UserPhoneNo\":\"" + phoneNo 
				+ "\","+"\"PhoneID\":\"" + TDevice.getIMEI()+ "\""
				+ "}";
		String url = Constant.GET_VERIFICATIONNO + jsonStr;
		Log.i("syso", "url---------------:"+url);
		final RollProgressbar rollProgressbar = CommonUtil.showProgressbarDialog(context, "正在获取验证码", true);
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, 
				new Response.Listener<JSONObject>(){

				@Override
				public void onResponse(JSONObject response) {
					try {
						String Code = response.getString("ResultCode");
						Log.i("syso", "response----------:"+response.toString());
						if(Code.equals("1")){
							CommonUtil.showToast(context, "获取成功，请稍候");
						}else {
							CommonUtil.showToast(context, "获取验证码失败，"+response.getString("Desc"));
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
					CommonUtil.showToast(context, "获取验证码失败，网络或服务器异常，请稍后重试");
				}
			});
			requestQueue.add(jsonObjectRequest);
	}
	
	
}
