package com.sinosoft.mobileshop.util;

import java.util.Stack;

import android.app.Activity;


public class AppManager {
	private static Stack<Activity> activityStack;
	private static Stack<Activity> activityBaseStack;
	private static AppManager instance;
	
	private AppManager() {
	}

	public static AppManager getAppManager() {
		if (instance == null) {
			instance = new AppManager();
		}
		return instance;
	}
	
	/**
	 * 添加Activity到堆栈
	 */
	public void addActivity(Activity activity) {
		if (activityStack == null) {
			activityStack = new Stack<Activity>();
		}
		activityStack.add(activity);
	}
	
	/**
	 * 添加Activity到堆栈
	 */
	public void addBaseActivity(Activity activity) {
		if (activityBaseStack == null) {
			activityBaseStack = new Stack<Activity>();
		}
		activityBaseStack.add(activity);
	}

	
	/**
	 * 获取当前Activity（堆栈中最后一个压入的）
	 */
	public Activity currentActivity() {
		if (activityStack != null) {
			return activityStack.lastElement();
		}
		return null;
	}

	/**
	 * 获取指定类名的Activity
	 */
	public static Activity getActivity(Class<?> cls) {
		if (activityStack != null) {
			for (Activity activity : activityStack) {
				if (activity.getClass().equals(cls)) {
					return activity;
				}
			}
		}
		return null;
	}

	/**
	 * 结束当前Activity（堆栈中最后一个压入的）
	 */
	public void finishActivity() {
		if (activityStack != null) {
			Activity activity = activityStack.lastElement();
			finishActivity(activity);
		}
	}

	/**
	 * 结束指定的Activity
	 */
	public void finishActivity(Activity activity) {
		if (activity != null && !activity.isFinishing()) {
			activity.finish();
			activity = null;
		}
	}

	/**
	 * 结束指定类名的Activity
	 */
	public void finishActivity(Class<?> cls) {
		if (activityStack != null) {
			for (Activity activity : activityStack) {
				if (activity.getClass().equals(cls)) {
					finishActivity(activity);
					break;
				}
			}
		}
	}

	/**
	 * 结束所有的Activity
	 */
	public void finishAllActivity() {
		for (Activity activity : activityStack) {
			finishActivity(activity);
		}
		activityStack.clear();
	}
	
	/**
	 * 结束所有非登录的activity
	 * @param class1 登录的activity
	 */
	public void finishSubActivity(Class<Activity> class1) {
		for (Activity activity : activityBaseStack) {
			if (!activity.getClass().equals(class1)) {
				finishActivity(activity);
			}
		}
		activityBaseStack.clear();
	}

	/**
	 * 退出应用程序
	 */
	public void AppExit() {
		try {
			finishAllActivity();
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
