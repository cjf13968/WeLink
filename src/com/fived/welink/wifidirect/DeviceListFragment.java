/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fived.welink.wifidirect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fived.welink.R;
import com.fived.welink.bgservice.SocketService;
import com.fived.welink.dataproc.DBHelper;
import com.fived.welink.dataproc.DataStoreManager;

public class DeviceListFragment extends ListFragment implements
		PeerListListener, OnItemClickListener {

	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
	private List<WifiP2pDevice> peers_tmp = new ArrayList<WifiP2pDevice>();
	private int[] images = { R.drawable.nima, R.drawable.image1,
			R.drawable.image2, R.drawable.image3, R.drawable.image4,
			R.drawable.image5, R.drawable.image6, R.drawable.image7,
			R.drawable.image8 };
	private Gallery gallery;
	private ImageButton ibtnhead = null;
	private Button btname = null;
	ProgressDialog progressDialog = null;
	View mContentView = null;
	public Button btnCancel = null;
	private ImageView ivperson = null;
	private WifiP2pDevice device;
	private DBHelper db = null;
	private Dialog alertDialog = null;
	private EditText etchangename = null;
	private Toast toast = null;
	private SQLiteDatabase sqlite_write = null;
	private SQLiteDatabase sqlite_read = null;
	private ListView lv = null;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		lv = getListView();
		SetLvListener();
		db = new DBHelper(getActivity());
		sqlite_write = db.getWritableDatabase();
		sqlite_read = db.getReadableDatabase();
		getDatabasePeers();
		this.setListAdapter(new WiFiPeerListAdapter(getActivity(),
				R.layout.link_row_devices, peers));

	}

	private void getDatabasePeers(List<WifiP2pDevice> peers_tmp) {
		String sql = "select * from deviceinfo";
		String address, name;
		int status = 0;
		int online = 0;
		Cursor cursor = sqlite_read.rawQuery(sql, null);
		peers_tmp.clear();
		// cursor.moveToFirst();
		while (cursor.moveToNext()) {
			WifiP2pDevice device_tmp = new WifiP2pDevice();
			address = cursor.getString(0);
			name = cursor.getString(1);
			status = cursor.getInt(2);
			online = cursor.getInt(3);
			device_tmp.deviceAddress = address;
			device_tmp.deviceName = name;
			device_tmp.status = status;
			peers_tmp.add(device_tmp);
			Log.i("database", name);
		}
		cursor.close();
	}

	private void getDatabasePeers() {
		String sql = "select * from deviceinfo";
		String address, name;
		int status = 0;
		int online = 0;
		Cursor cursor = sqlite_read.rawQuery(sql, null);
		peers.clear();
		// cursor.moveToFirst();
		cursor.moveToLast();
		while (cursor.getPosition() >= 0) {
			WifiP2pDevice device_tmp = new WifiP2pDevice();
			address = cursor.getString(0);
			name = cursor.getString(1);
			status = cursor.getInt(2);
			online = cursor.getInt(3);
			device_tmp.deviceAddress = address;
			device_tmp.deviceName = name;
			device_tmp.status = status;
			peers.add(device_tmp);
			Log.i("database", name);
			cursor.moveToPrevious();
		}
		cursor.close();
	}

	// 检查是否存在
	private boolean Check(String address) {
		String sql = "select * from deviceinfo where address = ?";
		Cursor cursor = sqlite_read.rawQuery(sql, new String[] { address });
		System.out.println(cursor.getCount());
		if (cursor.moveToFirst())
			return true;
		else
			return false;

	}

	// 检查是否在线
	private boolean IsOnline(String address) {
		int online = 0;
		String sql = "select online from deviceinfo where address = ?";
		Cursor cursor = sqlite_read.rawQuery(sql, new String[] { address });
		if (cursor.moveToFirst()) {
			online = cursor.getInt(0);
			System.out.println(online);
			if (online == 0)
				return false;
			else {
				return true;
			}
		} else {
			Log.i("IsOnline", "为找到该设备");
			return false;
		}
	}

	private void DeleteSomeone(String address) {
		String sql = "delete from deviceinfo where address=?";
		sqlite_write.execSQL(sql, new String[] { address });
	}

	private void SetImagenum(int imagenum) {
		String sql = "update mydevice set imagenum=?";
		sqlite_write.execSQL(sql, new Object[] { imagenum });
	}

	private void SetName(String name) {
		String sql = "update mydevice set name=?";
		sqlite_write.execSQL(sql, new String[] { name });
	}

	private String GetNameInfo(String address) {
		String sql = "select name from deviceinfo where address=?";
		Cursor cursor = sqlite_read.rawQuery(sql, new String[] { address });
		if (cursor.moveToFirst()) {
			String name = cursor.getString(0);
			return name;
		} else {
			return device.deviceName;
		}
	}

	private int GetImgInfo(String address) {
		String sql = "select imagenum from deviceinfo where address=?";
		Cursor cursor = sqlite_read.rawQuery(sql, new String[] { address });
		if (cursor.moveToFirst()) {
			int imginfo = cursor.getInt(0);
			return imginfo;
		} else {
			return R.drawable.m_person;
		}
	}

	public String GetMyName() {
		String sql = "select name from mydevice";
		Cursor cursor = sqlite_read.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			String name = cursor.getString(0);
			return name;
		} else {
			return device.deviceName;
		}
	}

	public int GetMyImg() {
		String sql = "select imagenum from mydevice";
		Cursor cursor = sqlite_read.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			int img = cursor.getInt(0);
			return img;
		} else {
			return R.drawable.nima;
		}
	}

	public String GetMyAddress() {
		String sql = "select address from mydevice";
		Cursor cursor = sqlite_read.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			String address = cursor.getString(0);
			return address;
		} else {
			return "gg";
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContentView = inflater.inflate(R.layout.link_device_list, null);
		ibtnhead = (ImageButton) mContentView.findViewById(R.id.link_icon);
		btname = (Button) mContentView.findViewById(R.id.my_name);
		// btname.setText(GetMyName());
		// ibtnhead.setImageResource(GetMyImg());
		CreateDialog();
		ibtnhead.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				LayoutInflater inflater = LayoutInflater.from(getActivity());
				final View dialogView = inflater.inflate(R.layout.link_dialog,
						null);
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				gallery = (Gallery) dialogView.findViewById(R.id.gallery);
				gallery.setAdapter(new ImageAdapter());
				gallery.setSelection(images.length / 2);
				gallery.setOnItemClickListener(DeviceListFragment.this);
				builder.setIcon(R.drawable.dialog_dialer)
						.setTitle("更改头像")
						.setView(dialogView)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {

									}
								}).create().show();
				// .setNegativeButton("取消",
				// new DialogInterface.OnClickListener() {
				//
				// public void onClick(DialogInterface dialog,
				// int which) {
				//
				// }
				// }).create().show();
			}
		});
		btname.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				etchangename.setText(btname.getText());
				alertDialog.show();
			}
		});
		return mContentView;
	}

	private void SetLvListener() {
		lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo info) {
				// TODO Auto-generated method stub
				menu.add(0, 0, 0, "删除该条记录");
			}
		});
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case 0:
			Toast.makeText(getActivity(), "已删除该条记录", Toast.LENGTH_SHORT).show();
			System.out.println(info.position);
			WifiP2pDevice device_delete = peers.get(info.position);
			String address_delete = device_delete.deviceAddress;
			DeleteSomeone(address_delete);
			getDatabasePeers();
			((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();

			break;

		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	private void CreateDialog() {
		LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
		View ChangenameView = layoutInflater.inflate(R.layout.link_change_name,
				null);
		etchangename = (EditText) ChangenameView
				.findViewById(R.id.link_change_name);
		alertDialog = new AlertDialog.Builder(getActivity())
				.setTitle("				 编辑微连昵称")
				.setIcon(R.drawable.link_change_name)
				.setView(ChangenameView)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (etchangename.getText().toString().equals("")) {
							etchangename.setError("不能为空!");
							try {
								Field field = dialog.getClass().getSuperclass()
										.getDeclaredField("mShowing");
								field.setAccessible(true);
								field.set(dialog, false);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							btname.setText(etchangename.getText());// 设置自定义
							SetName(etchangename.getText().toString());
							try {
								Field field = dialog.getClass().getSuperclass()
										.getDeclaredField("mShowing");
								field.setAccessible(true);
								field.set(dialog, true);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							Field field = dialog.getClass().getSuperclass()
									.getDeclaredField("mShowing");
							field.setAccessible(true);
							field.set(dialog, true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).create();
	}

	private class ImageAdapter extends BaseAdapter {

		private int mGalleryItemBackground;

		public ImageAdapter() {
			TypedArray typedArray = getActivity().obtainStyledAttributes(
					R.styleable.HelloGallery);
			mGalleryItemBackground = typedArray.getResourceId(
					R.styleable.HelloGallery_android_galleryItemBackground, 0);
			typedArray.recycle();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return images.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return images[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ImageView imageView = new ImageView(getActivity());
			imageView.setImageResource(images[position]);
			imageView.setBackgroundResource(mGalleryItemBackground);
			return imageView;
		}

	}

	/**
	 * @return this device
	 */
	public WifiP2pDevice getDevice() {
		return device;
	}

	private static String getDeviceStatus(int deviceStatus) {
		Log.d(WiFiDirectActivity.TAG, "Peer status :" + deviceStatus);
		switch (deviceStatus) {
		case WifiP2pDevice.AVAILABLE:
			return "空闲";
		case WifiP2pDevice.INVITED:
			return "受邀";
		case WifiP2pDevice.CONNECTED:
			return "已连接";
		case WifiP2pDevice.FAILED:
			return "失败";
		case WifiP2pDevice.UNAVAILABLE:
			return "不可用";
		default:
			return "未知";

		}
	}

	/**
	 * Initiate a connection with the peer. 列表的监听
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(
				position);
		if (IsOnline(device.deviceAddress))
			((DeviceActionListener) getActivity()).showDetails(device);
	}

	/**
	 * WifiP2pDevice list.
	 */
	private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

		private List<WifiP2pDevice> items;

		public WiFiPeerListAdapter(Context context, int textViewResourceId,
				List<WifiP2pDevice> objects) {
			super(context, textViewResourceId, objects);
			items = objects;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.link_row_devices, null);
			}
			WifiP2pDevice device = items.get(position);
			if (device != null) {
				TextView top = (TextView) v.findViewById(R.id.device_name);
				TextView bottom = (TextView) v
						.findViewById(R.id.device_details);
				btnCancel = (Button) v.findViewById(R.id.link_cancel);
				ivperson = (ImageView) v.findViewById(R.id.icon);
				if (ivperson != null && !IsOnline(device.deviceAddress)) {// 不在线
					Drawable mDrawable = getResources().getDrawable(
							GetImgInfo(device.deviceAddress));
					mDrawable.mutate();
					ColorMatrix cm = new ColorMatrix();
					cm.setSaturation(0);
					ColorMatrixColorFilter cf = new ColorMatrixColorFilter(cm);
					mDrawable.setColorFilter(cf);
					ivperson.setImageDrawable(mDrawable);

					Log.i("changeimage", "true");
				} else if (ivperson != null && IsOnline(device.deviceAddress)) {// 在线
					ivperson.setImageResource(GetImgInfo(device.deviceAddress));
				}
				if (top != null) {
					top.setText(GetNameInfo(device.deviceAddress));
				}
				if (bottom != null) {
					bottom.setText(getDeviceStatus(device.status));
				}
				if (device.status == WifiP2pDevice.CONNECTED) {
					btnCancel.setVisibility(View.VISIBLE);
					btnCancel.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							((DeviceActionListener) getActivity()).disconnect();
							((DeviceActionListener) getActivity())
									.cancelDisconnect();
							Intent it = new Intent(getActivity(),
									SocketService.class);
							getActivity().stopService(it);
							clearPeers();
						}
					});
				} else {
					btnCancel.setVisibility(View.INVISIBLE);
				}
			}

			return v;

		}

		@Override
		public void insert(WifiP2pDevice object, int index) {
			// TODO Auto-generated method stub
			super.insert(object, index);
		}

		@Override
		public void remove(WifiP2pDevice object) {
			// TODO Auto-generated method stub
			super.remove(object);
		}
	}

	public void updateThisDevice(WifiP2pDevice device) {
		this.device = device;
		TextView view = (TextView) mContentView.findViewById(R.id.my_status);
		view.setText(getDeviceStatus(device.status));
		boolean isWrite = DataStoreManager.getDefaultInstance(getActivity())
				.getWriteStatus();
		if (isWrite) {
			String sql = "insert into mydevice(address, name, status, online, imagenum) values(?,?,?,?,?)";
			view = (TextView) mContentView.findViewById(R.id.my_name);
			view.setText(device.deviceName);
			sqlite_write.execSQL(sql, new Object[] { device.deviceAddress,
					device.deviceName, device.status, 1, R.drawable.nima });
			DataStoreManager.getDefaultInstance(getActivity()).setWriteStatus();
		} else {
			String sql = "update mydevice set status=?";
			sqlite_write.execSQL(sql, new Object[] { device.status });
			btname.setText(GetMyName());
			ibtnhead.setImageResource(GetMyImg());
		}
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		clearPeers();
		db.close();
		// Log.i("onDestroyView", "close_db");
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList peerList) {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		// peers.clear();
		// peers.addAll(peerList.getDeviceList());
		peers_tmp.clear();
		peers_tmp.addAll(peerList.getDeviceList());

		String address, name, currenname;
		int status, online, currenimagenum;
		String sql = "insert into deviceinfo(address, name, status, online, imagenum) values (?,?,?,?,?)";
		String sql_delete = "delete from deviceinfo where address=?";
		for (int i = 0; i < peers_tmp.size(); i++) {
			address = peers_tmp.get(i).deviceAddress;
			name = peers_tmp.get(i).deviceName;
			status = peers_tmp.get(i).status;
			online = 1;
			if (Check(address) == false) {// 不存在
				sqlite_write.execSQL(sql, new Object[] { address, name, status,
						online, R.drawable.m_person });
			} else {// 存在
				currenname = GetNameInfo(address);
				currenimagenum = GetImgInfo(address);
				sqlite_write.execSQL(sql_delete, new Object[] { address });
				sqlite_write.execSQL(sql, new Object[] { address, currenname,
						status, online, currenimagenum });
			}
		}

		getDatabasePeers();
		peers_tmp.clear();
		((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
		if (peers.size() == 0) {
			Log.d(WiFiDirectActivity.TAG, "No devices found");
			return;
		}

	}

	public void clearPeers() {
		// peers.clear();
		String address;
		int status, online;
		String sql_update = "update deviceinfo set status=3, online=0 where address=?";
		getDatabasePeers(peers_tmp);
		for (int i = 0; i < peers_tmp.size(); i++) {
			address = peers_tmp.get(i).deviceAddress;
			sqlite_write.execSQL(sql_update, new String[] { address });
			Log.i("peers_tmp", address);
		}
		peers_tmp.clear();
		getDatabasePeers();
		((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
	}

	public void onInitiateDiscovery() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		progressDialog = ProgressDialog.show(getActivity(), "按返回键撤销",
				"正在搜索您附近的好友......", true, true,
				new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						if (toast == null) {
							toast = Toast.makeText(getActivity(), "已撤销！",
									Toast.LENGTH_SHORT);
							toast.show();
						} else {
							toast.setText("已撤销！");
							toast.setDuration(Toast.LENGTH_SHORT);
							toast.show();
						}
					}
				});
	}

	/**
	 * An interface-callback for the activity to listen to fragment interaction
	 * events.
	 */
	public interface DeviceActionListener {

		void showDetails(WifiP2pDevice device);

		void cancelDisconnect();

		void connect(WifiP2pConfig config);

		void disconnect();
	}

	@Override
	public void onItemClick(AdapterView<?> adapterview, View view, int postion,
			long id) {
		// TODO Auto-generated method stub
		ibtnhead.setImageResource(images[postion]);
		SetImagenum(images[postion]);
	}

}
