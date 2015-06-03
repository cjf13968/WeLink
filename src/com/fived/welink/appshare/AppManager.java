package com.fived.welink.appshare;

import com.fived.welink.dataproc.DataStoreManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.content.Context;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;


/**
 * 获取系统已经安装的应用信息
 */
public class AppManager {

	private PackageManager packageManager = null;
	public static AppManager appManager = null;
	public Context tx = null;

	public static AppManager getInstance(Context ct) {
		if (appManager == null)
			appManager = new AppManager(ct);
		return appManager;
	}

	public AppManager(Context context) {
		tx = context;
		packageManager = context.getPackageManager();
	}

	public ArrayList<AppInfo> installPackagesInfo() {
		ArrayList<AppInfo> appList = new ArrayList<AppInfo>(); // 用来存储获取的应用信息数据
		List<PackageInfo> packages = packageManager.getInstalledPackages(0);
		HashSet<String> shildAppSet = DataStoreManager.getDefaultInstance(tx)
				.getAppData();

		for (int i = 0; i < packages.size(); i++) {
			PackageInfo packageInfo = packages.get(i);
			AppInfo tmpInfo = new AppInfo();
			tmpInfo.appName = packageInfo.applicationInfo.loadLabel(
					packageManager).toString();
			tmpInfo.sourceDir = packageInfo.applicationInfo.sourceDir;
			tmpInfo.packageName = packageInfo.packageName;
			tmpInfo.versionName = packageInfo.versionName;
			tmpInfo.versionCode = packageInfo.versionCode;
			//
			BitmapDrawable bitmapDrawable = (BitmapDrawable) packageInfo.applicationInfo
					.loadIcon(packageManager);
			tmpInfo.appIcon = new MyBitmap(BytesBitmap.getBytes(bitmapDrawable
					.getBitmap()));

			tmpInfo.appSize = (long) (new File(tmpInfo.sourceDir).length());
			for (int j = 0; j < shildAppSet.size(); ++j) {
				if (shildAppSet.toArray()[j].toString().equals(
						packageInfo.packageName)) {
					tmpInfo.sharenable = false;
				}
			}
			// Only display the non-system app info
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
				appList.add(tmpInfo);// 如果非系统应用，则添加至appList
		}
		return appList;
	}
}
