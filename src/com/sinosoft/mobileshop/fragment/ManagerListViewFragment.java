package com.sinosoft.mobileshop.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.adapter.AppInfoAdapter;
import com.sinosoft.mobileshop.appwidget.popupwindow.MyPopupWindow;
import com.sinosoft.mobileshop.appwidget.recycler.RefreshRecyclerView;
import com.sinosoft.mobileshop.base.BaseFragment;
import com.sinosoft.mobileshop.bean.AppVersionInfo;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.JsonUtil;
import com.sinosoft.mobileshop.util.LiteOrmQueryUtil;
import com.sinosoft.mobileshop.util.LiteOrmUtil;
import com.sinosoft.mobileshop.util.VolleyUtil;
import com.sinosoft.phoneGapPlugins.util.Constant;

public class ManagerListViewFragment extends BaseFragment {

	private RefreshRecyclerView mRecyclerView;
	private AppInfoAdapter mAdapter;
	private Handler handler;
	private Context context;
	private EditText managerlist_query_et;
	private Button managerlist_query;
	private Button managerlist_down_iv;
	private MyPopupWindow myPopupWindow;
	private CheckBox rBupdateApp;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_managerlist, container,false);
		initView(view);
		initData();
		
		IntentFilter install = new IntentFilter();
		install.addAction("com.sinosoft.msg.install");//添加动态广播的Action
		view.getContext().registerReceiver(broadcastReceiver, install);
		
		return view;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initView(View view) {
		super.initView(view);
		handler = new Handler();
		context = view.getContext();
		mAdapter = new AppInfoAdapter(view.getContext());
		mRecyclerView = (RefreshRecyclerView) view.findViewById(R.id.appinfo_recycler_view);
		managerlist_query_et = (EditText) view.findViewById(R.id.managerlist_query_et);
		managerlist_query = (Button) view.findViewById(R.id.managerlist_query_iv);
		managerlist_down_iv = (Button) view.findViewById(R.id.managerlist_down_iv);
		mRecyclerView.setSwipeRefreshColors(0xFF437845, 0xFFE44F98, 0xFF2FAC21);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
		mRecyclerView.setAdapter(mAdapter);
		managerlist_down_iv.setOnClickListener(this);
		managerlist_query.setOnClickListener(this);
		myPopupWindow = new MyPopupWindow(getActivity());
		managerlist_query_et.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(TextUtils.isEmpty(s)){
					mRecyclerView.showSwipeRefresh();
					if(rBupdateApp !=null){
						boolean checked = rBupdateApp.isChecked();
						if(checked){
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									mAdapter.clear();
									List<AppVersionInfo> appInfoList = LiteOrmUtil.getLiteOrm(context).query(AppVersionInfo.class);
									List<AppVersionInfo> appList = new ArrayList<AppVersionInfo>();
									for (AppVersionInfo appVersionInfo : appInfoList) {
					        			if(Constant.GYICPACKAGE.equals(appVersionInfo.getPackageName())) {
					        				continue;
					        			}
					        			final int status = CommonUtil.getVersionStatus(appVersionInfo.getApplicationNewVersion(), appVersionInfo.getPackageName());
					        			if(status ==1){
					        				appList.add(appVersionInfo);
					        			}
									}
									mAdapter.addAll(appList);
									mAdapter.isLoadEnd = true;
									mAdapter.showNoMoreAndHidden();
									mRecyclerView.dismissSwipeRefresh();
								}
							}, 1000);
						}else {
							getData();
						}
					}else {
						getData();
					}
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		if(myPopupWindow != null){
			rBupdateApp = myPopupWindow.getRBupdateApp();
			if(rBupdateApp != null){
				rBupdateApp.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						mRecyclerView.showSwipeRefresh();
						if(isChecked){
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									mAdapter.clear();
									String trim = managerlist_query_et.getText().toString().trim();
									List<AppVersionInfo> appInfoList = null;
									List<AppVersionInfo> appList =  new ArrayList<AppVersionInfo>();
									if(TextUtils.isEmpty(trim)){
										appInfoList = LiteOrmUtil.getLiteOrm(context).query(AppVersionInfo.class);
									}else {
										String [] value = {trim};
										LiteOrmQueryUtil liteOrmQueryUtil = new LiteOrmQueryUtil(LiteOrmUtil.getLiteOrm(getActivity()));
										appInfoList = (List<AppVersionInfo>) liteOrmQueryUtil.getQueryByWhere(AppVersionInfo.class, "ApplicationName", value);
									}
									if(appInfoList!= null && appInfoList.size()>0){
										for (AppVersionInfo appVersionInfo : appInfoList) {
											if(Constant.GYICPACKAGE.equals(appVersionInfo.getPackageName())) {
												continue;
											}
											final int status = CommonUtil.getVersionStatus(appVersionInfo.getApplicationNewVersion(), appVersionInfo.getPackageName());
											if(status ==1){
												appList.add(appVersionInfo);
											}
										}
										if(appList!= null && appList.size()>0){
											mAdapter.clear();
											mAdapter.addAll(appList);
										}else{
											mAdapter.clear();
											CommonUtil.showToast(getActivity(), "暂无该应用");
										}
									}else {
										mAdapter.clear();
										CommonUtil.showToast(getActivity(), "暂无该应用");
									}
									mAdapter.isLoadEnd = true;
									mAdapter.showNoMoreAndHidden();
									mRecyclerView.dismissSwipeRefresh();
								}
							}, 1000);
						}else {
							String trim = managerlist_query_et.getText().toString().trim();
							if(TextUtils.isEmpty(trim)){
								getData();
							}else {
								String [] value = {trim};
								queryByCondition(AppVersionInfo.class, "ApplicationName", value);
							}
						}
					}
				});
			}
		}
//		mRecyclerView.setRefreshAction(new Action() {
//			@Override
//			public void onAction() {
//				doPost();
//			}
//		});
		

//		mRecyclerView.setLoadMoreAction(new Action() {
//			@Override
//			public void onAction() {
//				getData(false);
//				page++;
//			}
//		});
//
		mRecyclerView.post(new Runnable() {
			@Override
			public void run() {
				mRecyclerView.showSwipeRefresh();
				doPost();
			}
		});
	}
	
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.managerlist_query_iv:
			String appName = managerlist_query_et.getText().toString().trim();
			if(TextUtils.isEmpty(appName)){
				CommonUtil.showToast(context, "应用名称不能为空");
			}else {
				String [] value = {appName};
				queryByCondition(AppVersionInfo.class, "ApplicationName", value);
			}
			break;

		case R.id.managerlist_down_iv:
			showPopupwindow();
			break;

		default:
			break;
		}
	}
	
	
	/**
	 * 展示下拉框
	 */
	@SuppressLint("NewApi")
	private void showPopupwindow() {
		if(!myPopupWindow.isShowing()){
			managerlist_down_iv.setBackground(CommonUtil.getDrawable(R.drawable.bg_up, context));
			myPopupWindow.showAsDropDown(managerlist_down_iv);
		}else {
			managerlist_down_iv.setBackground(CommonUtil.getDrawable(R.drawable.bg_down, context));
			myPopupWindow.dismiss();
		}
	}

	/**
	 * 通过条件查找app
	 * @param <T>
	 */
	private <T> void queryByCondition(final Class<T> cla, final String field,final String[] value) {
		mRecyclerView.post(new Runnable() {
			@Override
			public void run() {
				mRecyclerView.showSwipeRefresh();
				if(value == null || value.length == 0){
					CommonUtil.showDialog(getActivity(), "查询条件不能为空").show();
					mRecyclerView.dismissSwipeRefresh();
					return;
				}
				LiteOrmQueryUtil liteOrmQueryUtil = new LiteOrmQueryUtil(LiteOrmUtil.getLiteOrm(getActivity()));
				final List<AppVersionInfo> queryByWhere = (List<AppVersionInfo>) liteOrmQueryUtil.getQueryByWhere(cla, field, value);
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if(queryByWhere != null && queryByWhere.size()>0){
							mAdapter.clear();
							mAdapter.addAll(queryByWhere);
						}else {
							mAdapter.clear();
							CommonUtil.showToast(getActivity(), "暂无该应用");
						}
						mAdapter.isLoadEnd = true;
						mAdapter.showNoMoreAndHidden();
						mRecyclerView.dismissSwipeRefresh();
					}
				}, 1000);
			}
		});
	}

	@Override
	public void initData() {
		super.initData();
		
	}
	
	

	private void getData() {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mAdapter.clear();
				List<AppVersionInfo> appInfoList = LiteOrmUtil.getLiteOrm(context).query(AppVersionInfo.class);
				List<AppVersionInfo> appList = new ArrayList<AppVersionInfo>();
				for (AppVersionInfo appVersionInfo : appInfoList) {
        			if(Constant.GYICPACKAGE.equals(appVersionInfo.getPackageName())) {
        				continue;
        			}
        			appList.add(appVersionInfo);
				}
				mAdapter.addAll(appList);
				mAdapter.isLoadEnd = true;
				mAdapter.showNoMoreAndHidden();
				mRecyclerView.dismissSwipeRefresh();
			}
		}, 1000);
	}

	private void doPost() {
//		String url = Constant.GETAPPLIST + 
//				"jsonstr={\"UserCode\":\""+CommonUtil.getUserinfo("userCode",context)+"\",\"OptUserCode\":\""+CommonUtil.getUserinfo("userCode",context)+"\",\"OptPackageName\":\"\",\"OS\":\"1\"}";
		String url = Constant.GETAPPLIST + 
				"jsonstr={\"UserCode\":\"0000000000\",\"OptUserCode\":\"0000000000\",\"OptPackageName\":\"\",\"OS\":\"1\"}";
		RequestQueue mRequestQueue = VolleyUtil.getVolleySingleton(context).getRequestQueue();
		JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,  new Response.Listener<JSONArray>(){
            private List<AppVersionInfo> appVersionList;
            private List<AppVersionInfo> appList = new ArrayList<AppVersionInfo>();

			@Override
            public void onResponse(JSONArray response) {
            	if(response != null && response.length() > 0) {
            		appVersionList = JsonUtil.jsonToBeanList(response, AppVersionInfo.class);
            		LiteOrmUtil.getLiteOrm(context).deleteAll(AppVersionInfo.class);
            		LiteOrmUtil.getLiteOrm(context).save(appVersionList);
            	}
            	getData();
			}
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                CommonUtil.showToast(context, "网络或服务器异常，请检查");
                getData();
            }
        });
		mRequestQueue.add(jsonArrayRequest);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().unregisterReceiver(broadcastReceiver);
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if(myPopupWindow != null && myPopupWindow.isShowing()){
			myPopupWindow.dismiss();
		}
	}
	
	// 消息接收器
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, android.content.Intent intent) {
			getData();
		};
	};

}
