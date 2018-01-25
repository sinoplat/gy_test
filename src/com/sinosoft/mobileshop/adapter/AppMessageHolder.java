package com.sinosoft.mobileshop.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.activity.NewsDetailActivity;
import com.sinosoft.mobileshop.appwidget.recycler.adapter.BaseViewHolder;
import com.sinosoft.mobileshop.bean.AppMessage;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.TDevice;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.progressdialog.RollProgressbar;

public class AppMessageHolder extends BaseViewHolder<AppMessage> {

	private ImageView msgRead;
	private ImageView msgIcon;
	private TextView msgTitle;
	private TextView msgDate;
	private TextView msgContent;
	private Button msgToapp;
	private RollProgressbar rollProgressbar;
	
    public AppMessageHolder(ViewGroup parent) {
		super(parent, R.layout.holder_appmessage);
	}

	@Override
	public void onInitializeView() {
		super.onInitializeView();
		msgRead = findViewById(R.id.msg_read_iv);
		msgIcon = findViewById(R.id.msg_icon_iv);
		msgTitle = findViewById(R.id.msg_title_tv);
		msgDate = findViewById(R.id.msg_date_tv);
		msgContent = findViewById(R.id.msg_content_tv);
		msgToapp =findViewById(R.id.msg_toapp);
	}

	@Override
	public void setData(final AppMessage object) {
		super.setData(object);
		
		if(object != null && "0".equals(object.getReadFlag())) {
			msgRead.setVisibility(View.VISIBLE);
		} else {
			msgRead.setVisibility(View.INVISIBLE);
		}
		String remark = object.getRemark();
		if(!TextUtils.isEmpty(remark)){
			String[] split = remark.split("^");
			final String packageName = split[0];
			final String launch = split[1];
			String appName = split[2];
			if(packageName.equals(Constant.GYICPACKAGE)){
				msgToapp.setVisibility(View.GONE);
			}else {
				msgToapp.setVisibility(View.VISIBLE);
				msgToapp.setText(appName);
				msgToapp.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						int versionStatus = CommonUtil.getVersionStatus("", packageName);
						if(versionStatus == 0){//未安装此APP
							Toast.makeText(itemView.getContext(), "当前设备尚未安装此应用，请先进行下载安装。", Toast.LENGTH_LONG).show();
						}else {
							TDevice.openApp(itemView.getContext(), packageName, 
									packageName + launch);
						}
					}
				});
			}
		}else {
			msgToapp.setVisibility(View.GONE);
		}
		
		msgTitle.setText(object.getMessageTitle());
		msgDate.setText(object.getOperateDate());
		msgContent.setText(object.getMessageContent());
		
	}

	@Override
	public void onItemViewClick(AppMessage object) {
		super.onItemViewClick(object);
		
		Intent intent = new Intent(itemView.getContext(), NewsDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("appMessage", object);
		intent.putExtras(bundle);
		itemView.getContext().startActivity(intent);
	}
	
	
}