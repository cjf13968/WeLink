package com.fived.welink.appshare;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.fived.welink.R;
import com.fived.welink.bgservice.SocketService;
import com.fived.welink.bgservice.SocketService.ServiceBinder;
import com.fived.welink.dataproc.DataStoreManager;
import com.fived.welink.dataproc.OpenFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SearchViewCompat.OnCloseListenerCompat;
import android.util.Log;
import android.view.*;

import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import android.widget.Toast;

/**
 * 应用共享及下载Activity
 * */

public class AppShareMainActivity extends Activity {
	private TextView myAppLoading = null;
	private TextView otherAppLoading = null;
	private Button btnMyApp = null;
	private Button btnOtherApp = null;
	private Button btnRefresh = null;
	private Button btnBack = null;
	private ViewPager viewPager = null;
	private MyPagerAdapter myPagerAdapter = null;
	private int switchFlag = 0;
	private boolean isDownloag = false;
	private boolean isFresh = false;
	private DownloadAsyncTask dlt = null;
	private ServiceBinder binder = null;
	private Toast myToast = null;
	private boolean isStop = false;

	private ArrayList<AppInfo> myAppInfoList = null;
	private MyAsyncTask myAsyncTask = null;
	private MyAdapter myAdapter = null;

	private ArrayList<AppInfo> otherAppInfoList = null;
	private OtherAsyncTask otherAsyncTask = null;
	private OtherAdapter otherAdapter = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_activity);
		myAppLoading = (TextView) findViewById(R.id.myapploading);
		otherAppLoading = (TextView) findViewById(R.id.otherapploading);
		btnMyApp = (Button) findViewById(R.id.myappbtn);
		btnOtherApp = (Button) findViewById(R.id.otherappbtn);
		btnRefresh = (Button) findViewById(R.id.refresh);
		btnBack = (Button) findViewById(R.id.back_button);
		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AppShareMainActivity.this.finish();
				overridePendingTransition(R.anim.back_top_in,
						R.anim.back_bottom_out);

			}
		});
		this.SlideView();
		btnMyApp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (switchFlag == 1) {
					viewPager.setCurrentItem(0);
					switchFlag = 0;
				}
			}
		});

		btnOtherApp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (switchFlag == 0) {
					switchFlag = 1;
					viewPager.setCurrentItem(1);
				}
			}
		});

		btnRefresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isDownloag) {
					if (myToast == null) {
						myToast = Toast.makeText(getApplicationContext(),
								"下载任务正在进行中，请稍后刷新", 2);
						myToast.setGravity(Gravity.BOTTOM, 0, 0);
					} else {
						myToast.setText("下载任务正在进行中，请稍后刷新");
						myToast.setDuration(2);
					}
					myToast.show();
					return;
				}
				if (isFresh)
					return;
				myPagerAdapter = new MyPagerAdapter();
				viewPager.setAdapter(myPagerAdapter);
				viewPager.setCurrentItem(1);
			}
		});

		Intent intent = new Intent(AppShareMainActivity.this,
				SocketService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}

	// 处理后退键的情况
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			AppShareMainActivity.this.finish();
			overridePendingTransition(R.anim.back_top_in,
					R.anim.back_bottom_out);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onPause() {
		if (myAppInfoList != null) {
			DataStoreManager.getDefaultInstance(this).setAppData(myAppInfoList);
		}
		super.onPause();
	}

	class MyAdapter extends BaseAdapter {
		private ArrayList<AppInfo> appList = null;
		private Context context = null;

		public MyAdapter(ArrayList<AppInfo> list, Context tx) {
			this.appList = list;
			this.context = tx;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return appList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method s;tub
			myViewHolder holder = null;
			if (convertView == null) {
				holder = new myViewHolder();
				convertView = LayoutInflater.from(context).inflate(
						R.layout.app_myapp_item, null);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.myappImage);
				holder.nameView = (TextView) convertView
						.findViewById(R.id.myappName);
				holder.infoView = (TextView) convertView
						.findViewById(R.id.myappInfo);
				holder.checkBox = (CheckBox) convertView
						.findViewById(R.id.myappCheck);
				convertView.setTag(holder);
			} else {
				holder = (myViewHolder) convertView.getTag();
			}

			AppInfo appInfo = appList.get(position);
			//
			holder.checkBox
					.setOnCheckedChangeListener(new MyOnCheckedChangeListener(
							holder, position));
			if (appInfo.sharenable) {
				holder.nameView.setTextColor(Color.rgb(0, 0, 0));
				holder.infoView.setTextColor(Color.rgb(0, 0, 0));
				holder.checkBox.setChecked(true);
				holder.imageView.setImageAlpha(255);
			} else {
				holder.nameView.setTextColor(Color.rgb(200, 200, 200));
				holder.infoView.setTextColor(Color.rgb(200, 200, 200));
				holder.checkBox.setChecked(false);
				holder.imageView.setImageAlpha(100);
			}
			//
			holder.nameView.setText(appInfo.appName);
			//
			holder.imageView.setBackgroundColor(Color.TRANSPARENT);
			Bitmap bp = BytesBitmap.getBitmap(appInfo.appIcon.getBitmapBytes());
			holder.imageView.setImageBitmap(bp);
			//
			holder.infoView.setText("版本:" + appInfo.versionName);
			return convertView;
		}

		class MyOnCheckedChangeListener implements OnCheckedChangeListener {

			myViewHolder holder = null;
			int pos;

			MyOnCheckedChangeListener(myViewHolder holder, int pos) {
				this.holder = holder;
				this.pos = pos;
			}

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					holder.nameView.setTextColor(Color.rgb(0, 0, 0));
					holder.infoView.setTextColor(Color.rgb(0, 0, 0));
					holder.imageView.setImageAlpha(255);
					myAppInfoList.get(pos).sharenable = true;
				} else {
					holder.nameView.setTextColor(Color.rgb(200, 200, 200));
					holder.infoView.setTextColor(Color.rgb(200, 200, 200));
					holder.imageView.setImageAlpha(100);
					myAppInfoList.get(pos).sharenable = false;
				}
				// TODO Auto-generated method stub

			}
		}

		class myViewHolder {
			ImageView imageView = null;
			TextView nameView = null;
			TextView infoView = null;
			CheckBox checkBox = null;
		}
	}

	//
	class OtherAdapter extends BaseAdapter {
		private ArrayList<AppInfo> appList = null;
		private Context context = null;

		public OtherAdapter(ArrayList<AppInfo> list, Context tx) {
			this.appList = list;
			this.context = tx;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return appList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			OtherViewHolder holder = null;
			AppInfo appInfo = appList.get(position);

			if (convertView != null) {
				holder = (OtherViewHolder) convertView.getTag();
			} else {
				holder = new OtherViewHolder();
				convertView = LayoutInflater.from(context).inflate(
						R.layout.app_otherapp_item, null);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.otherappImage);
				holder.nameView = (TextView) convertView
						.findViewById(R.id.otherappName);
				holder.infoView = (TextView) convertView
						.findViewById(R.id.otherappInfo);
				holder.button = (Button) convertView
						.findViewById(R.id.otherappButton);
				holder.progressbar = (ProgressBar) convertView
						.findViewById(R.id.otherappprogressBar);
				convertView.setTag(holder);
			}

			//
			if (appInfo.sharenable) {
				holder.button.setVisibility(View.VISIBLE);

			} else {
				holder.button.setVisibility(View.INVISIBLE);
			}

			//
			if (appInfo.isDownloading) {
				holder.progressbar.setVisibility(View.VISIBLE);

			} else {
				holder.progressbar.setVisibility(View.INVISIBLE);
			}
			//
			holder.nameView.setText(appInfo.appName);
			//
			holder.imageView.setBackgroundColor(Color.TRANSPARENT);
			Bitmap bp = BytesBitmap.getBitmap(appInfo.appIcon.getBitmapBytes());
			holder.imageView.setImageBitmap(bp);
			//
			double appSize = (double) appInfo.appSize / 1024.0 / 1024.0;
			if (appSize > 1.0)
				holder.infoView.setText(String.format("%.2fMB", appSize));
			else {
				holder.infoView.setText(String.format("%dKB",
						(int) (appSize * 1024.0)));
			}
			//
			holder.progressbar.setProgress(appInfo.progress);
			//
			holder.button.setText(appInfo.btnText);
			holder.button.setTextColor(appInfo.btnTextColor);
			holder.button.setOnClickListener(new DownLoadOnClickListener(
					position));
			return convertView;
		}

		class DownLoadOnClickListener implements OnClickListener {
			private int pos = 0;

			public DownLoadOnClickListener(int pos) {
				this.pos = pos;
			}

			@Override
			public void onClick(View v) {
				AppInfo appInfo = otherAppInfoList.get(pos);
				if ((appInfo.btnText.equals("下载") || appInfo.btnText
						.equals("失败")) && !isDownloag) {
					otherAdapter.notifyDataSetChanged();
					dlt = new DownloadAsyncTask(pos);
					dlt.execute();
				} else {
					if (myToast == null) {
						myToast = Toast.makeText(getApplicationContext(),
								"下载任务正在进行中，请稍等……", 2);
						myToast.setGravity(Gravity.BOTTOM, 0, 0);
					} else {
						myToast.setText("下载任务正在进行中，请稍等……");
						myToast.setDuration(2);
					}
					myToast.show();
				}
			}
		}

		class OtherViewHolder {
			ImageView imageView = null;
			TextView nameView = null;
			TextView infoView = null;
			Button button = null;
			ProgressBar progressbar = null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}
	}

	// 我的可下载列表异步加载
	class MyAsyncTask extends AsyncTask<Integer, Integer, String> {
		private Context context;
		private ListView listView;

		public MyAsyncTask(Context tx, ListView lv) {
			this.context = tx;
			this.listView = lv;
		}

		@Override
		protected String doInBackground(Integer... params) {
			while (binder.isUpdateMyAppList()) {
				try {
					Thread.sleep(500);
				} catch (Exception e) {
				}
				if (isStop)
					return null;
			}
			myAppInfoList = binder.getMyAppList();
			return null;
		}

		@Override
		protected void onPreExecute() {
			myAppLoading.setText("加载中……");
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String result) {
			if (isStop)
				return;
			myAppLoading.setText("");
			myAdapter = new MyAdapter(myAppInfoList, context);
			listView.setAdapter(myAdapter);
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

	}

	// 好友下载列表异步
	class OtherAsyncTask extends AsyncTask<Integer, Integer, String> {
		private Context context;
		private ListView listView;

		public OtherAsyncTask(Context tx, ListView lv) {
			this.context = tx;
			this.listView = lv;
		}

		@Override
		protected String doInBackground(Integer... params) {
			int flag = 0;
			binder.requestOtherAppList();
			while (binder.isUpdateOtherAppList()) {
				try {
					Thread.sleep(500);
				} catch (Exception e) {
				}
				if (isStop)
					return null;
				flag++;
				System.out.println("flag" + flag);
				if (flag > 20)
					break;
			}
			otherAppInfoList = binder.getOtherAppList();
			return null;
		}

		@Override
		protected void onPreExecute() {
			isFresh = true;
			otherAppLoading.setText("加载中……");
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String result) {
			if (isStop)
				return;
			isFresh = false;
			if (otherAppInfoList == null) {
				otherAppLoading.setText("……载入失败╮(╯▽╰)╭");
			} else {
				otherAppLoading.setText("");
				otherAdapter = new OtherAdapter(otherAppInfoList, context);
				listView.setAdapter(otherAdapter);
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {

			super.onProgressUpdate(values);
		}

	}

	// 异步下载
	class DownloadAsyncTask extends AsyncTask<Integer, Double, Integer> {
		private int pos = 0;
		private AppInfo appInfo = null;

		public DownloadAsyncTask(int pos) {
			this.pos = pos;
			this.appInfo = otherAppInfoList.get(pos);
		}

		@Override
		protected void onPreExecute() {
			System.out.println("onPreExecute");
			appInfo.isDownloading = true;
			appInfo.btnTextColor = Color.rgb(0, 0, 0);
			appInfo.btnText = "等待";
			isDownloag = true;
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Integer result) {
			System.out.println("onPostExecute" + result);
			isDownloag = false;
			binder.setProgress(0.0f);
			if (isStop) {
				appInfo.isDownloading = false;
				appInfo.btnTextColor = Color.rgb(0, 0, 0);
				appInfo.btnText = "下载";
				appInfo.progress = 0;
				otherAdapter.notifyDataSetChanged();
				return;
			}
			if (result == -1) {
				appInfo.isDownloading = false;
				appInfo.btnTextColor = Color.rgb(255, 0, 0);
				appInfo.btnText = "失败";
			} else {
				otherAppInfoList.remove(pos);
				// String filePath = SocketService.DST_PATH + appInfo.appName
				// + ".apk";
				// OpenFile openFile = new OpenFile(filePath,
				// AppShareMainActivity.this);
				// openFile.Open();
			}
			otherAdapter.notifyDataSetChanged();
			super.onPostExecute(result);
		}

		@Override
		protected Integer doInBackground(Integer... params) {
			System.out.println("onDonbingbGstart");
			double temp = 0;
			double pg = 0;
			binder.setDownloadApp(true);
			binder.DownloadApp(appInfo.sourceDir, appInfo.appName + ".apk");
			while (binder.isDownloadApp()) {
				if (isStop) {
					System.out.println("isStop");
					return null;
				}
				pg = binder.getProgress();
				System.out.println(pg);
				if (pg - temp > 0.05) {
					System.out.println(pg - temp);
					temp = pg;
					publishProgress(pg);
				}
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
			}
			System.out.println("onDonbingbGEnd");
			if (!binder.isDownloadSuccessful())
				return -1;
			return 2;
		}

		@Override
		protected void onProgressUpdate(Double... values) {
			System.out.println("onProgressUpdate");
			appInfo.btnText = String.format("%.1f%%", values[0] * 100);
			appInfo.progress = (int) (values[0] * 100);
			otherAdapter.notifyDataSetChanged();
			super.onProgressUpdate(values);
		}
	}

	// conn
	ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			System.out.println("App-->onServiceConnected");
			binder = (ServiceBinder) service;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub

		}

	};

	// 左右滑动view，切换界面
	public void SlideView() {

		myPagerAdapter = new MyPagerAdapter();
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		viewPager.setAdapter(myPagerAdapter);

		// 初始化当前显示的view
		viewPager.setCurrentItem(0);
		// 设置预加载页面数
		viewPager.setOffscreenPageLimit(0);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				if (position == 0) {
					myAppLoading.setVisibility(View.VISIBLE);
					otherAppLoading.setVisibility(View.INVISIBLE);
					btnMyApp.setBackgroundResource(R.drawable.app_left_selected);
					btnOtherApp
							.setBackgroundResource(R.drawable.app_right_select_btn);
					switchFlag = 0;
					btnRefresh.setEnabled(false);
					btnRefresh.setText("分享");
					btnRefresh.setBackgroundColor(Color.TRANSPARENT);
				} else {
					myAppLoading.setVisibility(View.INVISIBLE);
					otherAppLoading.setVisibility(View.VISIBLE);
					btnRefresh.setEnabled(true);
					btnRefresh.setText("");
					btnRefresh
							.setBackgroundResource(R.drawable.app_refresh_selector);

					btnMyApp.setBackgroundResource(R.drawable.app_left_select_btn);
					btnOtherApp
							.setBackgroundResource(R.drawable.app_right_selected);
					switchFlag = 1;
				}

				// setTitleName(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

	}

	private class MyPagerAdapter extends PagerAdapter {

		public MyPagerAdapter() {
			super();
		}

		// 销毁arg1位置的界面
		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			// Log.i("desrotyitem", "" + arg1+arg1);
			((ViewPager) arg0).removeView((View) arg2);

		}

		// 获取当前窗体界面数
		@Override
		public int getCount() {
			return 2;
		}

		// 初始化arg0位置的界面
		@Override
		public Object instantiateItem(View view, int position) {
			LayoutInflater inflater = (LayoutInflater) view.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			int layoutId = 0;
			View layout = null;
			switch (position) {
			case 0:
				layoutId = R.layout.app_listview;
				layout = inflater.inflate(layoutId, null);
				ListView listview = (ListView) layout
						.findViewById(R.id.app_listview);
				myAsyncTask = new MyAsyncTask(AppShareMainActivity.this,
						listview);
				myAsyncTask.execute(1000);
				// 显示天气信息
				// showContent(layout);
				break;
			case 1:
				Log.i("instantiate item", "1");
				layoutId = R.layout.app_listview;
				layout = inflater.inflate(layoutId, null);
				listview = (ListView) layout.findViewById(R.id.app_listview);
				otherAsyncTask = new OtherAsyncTask(AppShareMainActivity.this,
						listview);
				otherAsyncTask.execute(1000);
				break;
			default:
				break;
			}

			((ViewPager) view).addView(layout, 0);

			return layout;
		}

		// 判断是否由对象生成界面
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

	}

	@Override
	protected void onDestroy() {
		isStop = true;
		System.out.println("AppShare onDeatroy");
		unbindService(conn);
		super.onDestroy();
	}

	// // 添加点击
	// list.setOnItemClickListener(new OnItemClickListener() {
	//
	// @Override
	// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
	// long arg3) {
	// setTitle("点击第" + arg2 + "个项目");
	// }
	// });

	// // 添加长按点击
	// list.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
	//
	// @Override
	// public void onCreateContextMenu(ContextMenu menu, View v,
	// ContextMenuInfo menuInfo) {
	// menu.setHeaderTitle("长按菜单-ContextMenu");
	// menu.add(0, 0, 0, "弹出长按菜单0");
	// menu.add(0, 1, 0, "弹出长按菜单1");
	// }
	// });
	// }
	//
	// // 长按菜单响应函数
	// @Override
	// public boolean onContextItemSelected(MenuItem item) {
	// setTitle("点击了长按菜单里面的第" + item.getItemId() + "个项目");
	// return super.onContextItemSelected(item);
	// }
}
