package com.fived.welink.dataproc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.widget.Toast;

import java.util.*;

import com.fived.welink.appshare.AppInfo;

/**
 * ÎÄ¼þ´æ´¢
 */
public class DataStoreManager {

	public static final String UN_SHARED_APP_KEY = "appShildList";
	public static final String START_STATUS = "start_status";
	public static final String WRITE_DATABASE = "write_database";

	private SharedPreferences share = null;
	private Editor editor = null;
	private Context t;
	public static DataStoreManager dataStoreManager = null;

	public static DataStoreManager getDefaultInstance(Context context) {
		if (dataStoreManager == null)
			dataStoreManager = new DataStoreManager(context);
		return dataStoreManager;
	}

	public DataStoreManager(Context context) {
		t = context;
		share = context
				.getSharedPreferences("preference", Context.MODE_PRIVATE);
	}

	@SuppressLint("CommitPrefEdits")
	public Editor getEditor() {
		if (share != null)
			editor = share.edit();
		return editor;
	}

	public void setStartStatus() {
		if (getEditor() != null) {
			editor.putBoolean(DataStoreManager.START_STATUS, false);
			editor.commit();
		}
	}
	
	public void setWriteStatus(){
		if(getEditor() != null){
			editor.putBoolean(DataStoreManager.WRITE_DATABASE, false);
			editor.commit();
		}
	}

	public boolean getStartStauts() {
		boolean isFirstStart = false;
		if (share != null) {
			isFirstStart = share.getBoolean(START_STATUS, true);
		}
		return isFirstStart;
	}
	
	public boolean getWriteStatus(){
		boolean isWrite = false;
		if(share != null){
			isWrite = share.getBoolean(WRITE_DATABASE, true);
		}
		return isWrite;
	}

	public void setAppData(ArrayList<AppInfo> appInfoList) {
		Set<String> siteno = null;
		if (appInfoList != null) {
			siteno = new HashSet<String>();
			for (int i = 0; i < appInfoList.size(); ++i) {
				AppInfo appInfo = appInfoList.get(i);
				if (!appInfo.sharenable) {
					siteno.add(appInfo.packageName);
				}
			}

			if (siteno != null && getEditor() != null) {
				editor.putStringSet(DataStoreManager.UN_SHARED_APP_KEY, siteno);
				editor.commit();
			}
		}
	}

	public HashSet<String> getAppData() {
		HashSet<String> packName = null;
		System.out.println("jj");
		if (share != null) {
			packName = (HashSet<String>) share.getStringSet(
					DataStoreManager.UN_SHARED_APP_KEY, new HashSet<String>());
			if (packName == null)
				System.out.println();
		}
		System.out.println("pp");
		return packName;
	}
}
