/**  
* @Title: SendFile.java
* @Package com.fived.welink.dataproc
* @Description: TODO
* @author CJF
* @date 2014-5-31 ÉÏÎç11:27:04
* @version V1.0  
*/
package com.fived.welink.dataproc;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;


public class SendAPK {
	private Activity activity;
	private String filePath;
	public static SendAPK sendAPK = null;
	
	public static SendAPK getInstance(Activity act){
		if(sendAPK == null)
			sendAPK = new SendAPK(act);
		return sendAPK;
	}
	
	public SendAPK(Activity act){
		this.activity = act;
	}
	
	public void sendFile(){
		List<PackageInfo> appListInfo = this.activity.getPackageManager()
				.getInstalledPackages(0);
		for (PackageInfo p : appListInfo) {
			if (p.applicationInfo.sourceDir.startsWith("/system/app/")) {
				continue;
			}
			if(p.applicationInfo.sourceDir.contains("welink")){
				this.filePath = p.applicationInfo.sourceDir;
				break;
			}
		}
		sendFile(this.filePath);
	}
	
	public void sendFile(String path) {
		PackageManager localPackageManager = activity.getPackageManager();
		Intent localIntent = null;

		HashMap<String, ActivityInfo> localHashMap = null;

		try {
			localIntent = new Intent();
			localIntent.setAction(Intent.ACTION_SEND);

			localIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(new File(path)));
			// localIntent.putExtra(Intent.EXTRA_STREAM,
			// Uri.fromFile(new File(localApplicationInfo.sourceDir)));
			localIntent.setType("*/*");
			List<ResolveInfo> localList = localPackageManager
					.queryIntentActivities(localIntent, 0);
			localHashMap = new HashMap<String, ActivityInfo>();
			Iterator<ResolveInfo> localIterator1 = localList.iterator();
			while (localIterator1.hasNext()) {
				ResolveInfo resolveInfo = (ResolveInfo) localIterator1.next();
				ActivityInfo localActivityInfo2 = resolveInfo.activityInfo;
				String str = localActivityInfo2.applicationInfo.processName;
				if (str.contains("bluetooth"))
					localHashMap.put(str, localActivityInfo2);
			}
		} catch (Exception localException) {
			// ToastHelper.showBlueToothSupportErr(activity);
		}
//		if (localHashMap.size() == 0)
//			ToastHelper.showBlueToothSupportErr(activity);
		ActivityInfo localActivityInfo1 = (ActivityInfo) localHashMap
				.get("com.android.bluetooth");
		if (localActivityInfo1 == null) {
			localActivityInfo1 = (ActivityInfo) localHashMap
					.get("com.mediatek.bluetooth");
		}
		if (localActivityInfo1 == null) {
			Iterator<ActivityInfo> localIterator2 = localHashMap.values()
					.iterator();
			if (localIterator2.hasNext())
				localActivityInfo1 = (ActivityInfo) localIterator2.next();
		}
		if (localActivityInfo1 != null) {
			localIntent.setComponent(new ComponentName(
					localActivityInfo1.packageName, localActivityInfo1.name));
			activity.startActivityForResult(localIntent, 4098);
			return;
		}
	//	ToastHelper.showBlueToothSupportErr(activity);
	} 
}
