package com.sinosoft.mobileshop.appwidget.popupwindow;

import com.sinosoft.gyicPlat.R;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.PopupWindow;

public class MyPopupWindow extends PopupWindow{
private Activity activity;
private CheckBox rb_updateApp;
	
	public MyPopupWindow(Activity activity) {
		super(activity);
		this.activity = activity;
		initUI();
	}

	private void initUI() {
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View inflate = inflater.inflate(R.layout.popupwindow, null);
		setContentView(inflate);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setOutsideTouchable(false);
		this.setFocusable(false);
		this.setTouchable(true);
		setBackgroundDrawable(new BitmapDrawable(activity.getResources()));
		update();
		getContentView().setFocusableInTouchMode(false);
		getContentView().setFocusable(false);
		rb_updateApp = (CheckBox) inflate.findViewById(R.id.rb_updateApp);
	}

	public CheckBox getRBupdateApp() {
		return rb_updateApp;
	}
}
