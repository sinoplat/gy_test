package com.sinosoft.mobileshop.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Paint.Join;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.sinosoft.getPhoneNumberUtils.SIMCardInfo;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.bean.AppOrderRel;
import com.sinosoft.mobileshop.bean.AppUploadFile;
import com.sinosoft.mobileshop.bean.AppVersionInfo;
import com.sinosoft.mobileshop.bean.AssistApp;
import com.sinosoft.phoneGapPlugins.pgsqliteplugin.DatabaseHelper;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.progressdialog.RollProgressbar;
import com.squareup.okhttp.internal.Util;

public class CommonUtil {

	public static boolean hasKitKat() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	}

	public static boolean hasLollipop() {
		return Build.VERSION.SDK_INT >= 21;
	}

	public static String getImageUrl(AppUploadFile appUploadFile) {
		String pre = "prpmuploadfileId.applicationNo="
				+ appUploadFile.getApplicationNo()
				+ "&prpmuploadfileId.applicationversion="
				+ appUploadFile.getApplicationVersion()
				+ "&prpmuploadfileId.serialNo=" + appUploadFile.getSerialNo();

		// String pre =
		// "prpmuploadfileId.applicationNo=APP20150120075906&prpmuploadfileId.applicationversion=0.0.1&prpmuploadfileId.serialNo=1";
		
		return Constant.GETIMAGEURL + pre;
	}

	public static DisplayImageOptions getImageConfig() {
		DisplayImageOptions options = null;
		if (options == null) {
			options = new DisplayImageOptions.Builder().cacheInMemory(true)
					.cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565)
					.build();
		}
		
		return options;
	}


	/**
	 * 0 未安装 1-更新 2-打开
	 * 
	 * @param newVersionNo
	 * @param packageName
	 * @return
	 */
	public static int getVersionStatus(String newVersionNo, String packageName) {
		boolean isExist = TDevice.isPackageExist(packageName);
		if (!isExist) {
			return 0;
		}
		String oldVersion = TDevice.getVersionName(packageName);
		if (oldVersion != null && !TextUtils.isEmpty(newVersionNo) && !oldVersion.equals(newVersionNo)) {
			return 1;
		} else {
			return 2;
		}
	}

	public static RollProgressbar showProgressbarDialog(Context context, String text,
			boolean isShow) {
		RollProgressbar rollProgressbar = new RollProgressbar(context);
		rollProgressbar.showProgressBar(text, isShow);
		return rollProgressbar;
	}

	public static void showToast(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
	}

	public static AlertDialog showDialog(Context context, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context); // 先得到构造器
		builder.setTitle("提示"); // 设置标题
		builder.setMessage(msg); // 设置内容
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { // 设置确定按钮
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss(); // 关闭dialog
					}
				});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { // 设置取消按钮
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		// 参数都设置完成了，创建并显示出来
		AlertDialog dialog = builder.create();
		return dialog;
	}

	public static AlertDialog showOneBtnDialog(Context context, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context); // 先得到构造器
		builder.setTitle("提示"); // 设置标题
		builder.setMessage(msg); // 设置内容
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { // 设置确定按钮
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss(); // 关闭dialog
					}
				});

		// 参数都设置完成了，创建并显示出来
		AlertDialog dialog = builder.create();
		return dialog;
	}

	/**
	 * 展示一个规定时间后自动关闭的 对话框
	 * 
	 * @param context
	 *            上下文
	 * @param msg
	 *            提示信息
	 * @param time
	 *            规定时间
	 */
	public static void showTimeDialog(Context context, String msg,
			final long time) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context); // 先得到构造器
		builder.setTitle("提示"); // 设置标题
		builder.setMessage(msg); // 设置内容
		// 参数都设置完成了，创建并显示出来
		final AlertDialog dialog = builder.create();
		dialog.setCancelable(false);
		dialog.getWindow()
				.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(time);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dialog.dismiss();
			}
		}).start();
	}
	
	
	/**
	 * 把用户信息保存到文件中
	 * @param employeeNo
	 * @param employeePhoneNo
	 * @param imei
	 */
	public static void saveUserInfoToFile(String userCode,String userPassWord) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("userCode", userCode);
			jsonObject.put("userPassWord", userPassWord);
			writeFileToSD(jsonObject.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	/**
	 * 写数据到文件
	 * @param strcontent
	 * @param strFilePath
	 */
	public static void WriteTxtFile(String strcontent) {
		// 每次写入时，都换行写
		String strContent = strcontent + "\n";
		try {
			File file = new File(Constant.USERINFO_PATH+Constant.USERINFO);
			if (!file.exists()) {
				file.createNewFile();
			}
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			raf.seek(file.length());
			raf.write(strContent.getBytes());
			raf.close();
		} catch (Exception e) {
			Log.e("TestFile", "Error on write File.");
		}
	}

	/**
	 * 写数据到文件
	 * @param strcontent
	 * @param strFilePath
	 */
	public static void writeFileToSD( String content) {
		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
			return;
		}
		try {
			File path = new File(Constant.USERINFO_PATH);
			File file = new File(Constant.USERINFO_PATH+Constant.USERINFO);
			Log.i("syso", "文件路径："+Constant.USERINFO_PATH+Constant.USERINFO);
			if (!path.exists()) {
				path.mkdir();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream stream = new FileOutputStream(file);
			byte[] buf = content.getBytes();
			stream.write(buf);
			stream.close();

		} catch (Exception e) {
			Log.e("TestFile", "Error on writeFilToSD.");
			e.printStackTrace();
		}
	}

	/**
	 * 获取当前账户的信息
	 * @return
	 */
	public static String getUserinfo2(String key,Context context) {
		String readTxtFile = ReadTxtFile();
		String value = null;
		if (!TextUtils.isEmpty(readTxtFile)) {
			try {
				if (key.equals("userPhoneNo")) {
					String nativePhoneNumber = new SIMCardInfo(context).getNativePhoneNumber();
					if (TextUtils.isEmpty(nativePhoneNumber)) {
						JSONObject jsonObject = new JSONObject(readTxtFile);
						value = jsonObject.getString(key);
					} else {
						value = nativePhoneNumber;
					}
				} else {
					JSONObject jsonObject = new JSONObject(readTxtFile);
					value = jsonObject.getString(key);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return value;
	}

	/**
	 * 读取文件中的数据
	 * @param strFilePath
	 * @return
	 */
	public static String ReadTxtFile() {
		String content = ""; // 文件内容字符串
		// 打开文件
		File file = new File(Constant.USERINFO_PATH+Constant.USERINFO);
		// 如果path是传递过来的参数，可以做一个非目录的判断
		if (file.isDirectory()) {
			Log.d("TestFile", "The File doesn't not exist.");
		} else {
			try {
				InputStream instream = new FileInputStream(file);
				if (instream != null) {
					InputStreamReader inputreader = new InputStreamReader(
							instream);
					BufferedReader buffreader = new BufferedReader(inputreader);
					String line;
					// 分行读取
					while ((line = buffreader.readLine()) != null) {
						content += line + "\n";
					}
					instream.close();
				}
			} catch (java.io.FileNotFoundException e) {
				Log.d("TestFile", "The File doesn't not exist.");
			} catch (IOException e) {
				Log.d("TestFile", e.getMessage());
			}
		}
		return content;
	}
	
	/**
	 * 获取安装的信息
	 * @return
	 */
	public static List<AppVersionInfo> getInstallApp(Context context) {
		List<AppVersionInfo> appInfoNewList = LiteOrmUtil.getLiteOrm(context).query(AppVersionInfo.class);
		List<AppOrderRel> appOrderRelList = LiteOrmUtil.getLiteOrm(context).query(AppOrderRel.class);
		List<AssistApp> assistAppList = LiteOrmUtil.getLiteOrm(context).query(AssistApp.class);
		List<AppVersionInfo> appInfoList = new ArrayList<AppVersionInfo>();
		List<AppVersionInfo> dataSourceList = new ArrayList<AppVersionInfo>();
		Map<String, Integer> assAppMap = new HashMap<String, Integer>();
		if(assistAppList != null && assistAppList.size() > 0) {
			for (AssistApp assistApp : assistAppList) {
				assAppMap.put(assistApp.getPackageName(), assistApp.getId());
			}
		}
		
		Map<String, String> orderAppMap = new HashMap<String, String>();
		if(appOrderRelList != null && appOrderRelList.size() > 0) {
			for (AppOrderRel appOrderRel : appOrderRelList) {
				orderAppMap.put(appOrderRel.getApplicationNo(), appOrderRel.getSerialNo());
			}
		}
		
		if(appInfoNewList != null && appInfoNewList.size() > 0) {
        	for (AppVersionInfo appVersionInfo : appInfoNewList) {
        		if(orderAppMap.containsKey(appVersionInfo.getApplicationNo())) {
        			appVersionInfo.setResultCode(orderAppMap.get(appVersionInfo.getApplicationNo()));
        		} else {
        			appVersionInfo.setResultCode("100");
        		}
        		appInfoList.add(appVersionInfo);
			}
        }
		
		if(appInfoList != null && appInfoList.size() > 0) {
			for (AppVersionInfo appVersionInfo : appInfoList) {
				int status = CommonUtil.getVersionStatus(appVersionInfo.getApplicationNewVersion(), 
						appVersionInfo.getPackageName());
				if((status == 1 || status == 2) && !assAppMap.containsKey(appVersionInfo.getPackageName())) { //1 更新   2打开
					dataSourceList.add(appVersionInfo);
				}
			}
		}
		return dataSourceList;
	}
	
	

	public static Drawable getDrawable(int i,Context context) {
		Resources resources = context.getResources();
		Drawable drawable = resources.getDrawable(i); 
		return drawable;
	}

	
	

	// 初始化数据
	public static void initData(Context context) {
		DatabaseHelper dBHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dBHelper.getWritableDatabase();
		db.delete("prpmRegistorUser", null, null);
		ContentValues _values = new ContentValues();
		_values.put("userCode", "");
		_values.put("password", "");
		_values.put("userName", "");
		_values.put("comCode", "");
		db.insert("prpmRegistorUser", null, _values);
	}
	
	/** 
	 * 删除目录（文件夹）以及目录下的文件 
	 * @param   sPath 被删除目录的文件路径 
	 * @return  目录删除成功返回true，否则返回false 
	 */  
	public static boolean deleteDirectory(String sPath) {  
		boolean flag = false;
	    //如果sPath不以文件分隔符结尾，自动添加文件分隔符  
	    if (!sPath.endsWith(File.separator)) {  
	        sPath = sPath + File.separator;  
	    }  
	    File dirFile = new File(sPath);  
	    //如果dir对应的文件不存在，或者不是一个目录，则退出  
	    if (!dirFile.exists() || !dirFile.isDirectory()) {  
	        return false;  
	    }  
	    flag = true;  
	    //删除文件夹下的所有文件(包括子目录)  
	    File[] files = dirFile.listFiles();  
	    for (int i = 0; i < files.length; i++) {  
	        //删除子文件  
	        if (files[i].isFile()) {  
	            flag = deleteFile(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        } //删除子目录  
	        else {  
	            flag = deleteDirectory(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        }  
	    }  
	    if (!flag) return false;  
	    //删除当前目录  
	    if (dirFile.delete()) {  
	        return true;  
	    } else {  
	        return false;  
	    }  
	}

	/** 
	 * 删除单个文件 
	 * @param   sPath    被删除文件的文件名 
	 * @return 单个文件删除成功返回true，否则返回false 
	 */  
	public static boolean deleteFile(String sPath) {  
		boolean flag = false;
	    File file = new File(sPath);  
	    // 路径为文件且不为空则进行删除  
	    if (file.isFile() && file.exists()) {  
	        file.delete();  
	        flag = true;  
	    }  
	    return flag;  
	}   
}
