package com.fived.welink.wifidirect;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

import com.baidu.cloudsdk.BaiduException;
import com.baidu.cloudsdk.IBaiduListener;
import com.baidu.cloudsdk.social.core.MediaType;
import com.baidu.cloudsdk.social.oauth.SocialConfig;
import com.baidu.cloudsdk.social.share.ShareContent;
import com.baidu.cloudsdk.social.share.SocialShare;
import com.baidu.cloudsdk.social.share.SocialShare.UIWidgetStyle;
import com.fived.welink.R;
import com.fived.welink.bgservice.SocketService;
import com.fived.welink.chart.ChartMainActivity;
import com.fived.welink.dataproc.SendAPK;
import com.fived.welink.welcome.HelpActivity;
import com.fived.welink.wifidirect.DeviceListFragment.DeviceActionListener;

public class WiFiDirectActivity extends Activity implements ChannelListener,
		DeviceActionListener, ConnectionInfoListener, OnMenuItemClickListener {

	public static final String TAG = "WeLink";
	private WifiP2pManager manager;
	private WifiP2pInfo info;
	WifiP2pDevice device;
	private boolean isWifiP2pEnabled = false;
	private boolean retryChannel = false;
	ProgressDialog progressDialog = null;

	private final IntentFilter intentFilter = new IntentFilter();
	private Channel channel;
	private BroadcastReceiver receiver = null;
	private Button btnSearch = null;
	private Toast toast = null;

	//
	private String mClientId;
	private SocialShare share;
	private ShareContent mPageContent = new ShareContent(" ",
			"我正在使用“微连直传”，这是款很不错的安卓应用，你也可试试哦！", " ");

	public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
		this.isWifiP2pEnabled = isWifiP2pEnabled;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.link_main);

		// add necessary intent values to be matched.
		//
		mClientId = SocialConfig.getInstance(this).getClientId(MediaType.BAIDU);

		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(this, getMainLooper(), null);
		receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
		registerReceiver(receiver, intentFilter);

		btnSearch = (Button) findViewById(R.id.link_search);
		btnSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isWifiP2pEnabled) {
					if (toast == null) {
						toast = Toast.makeText(WiFiDirectActivity.this,
								"请打开无线", Toast.LENGTH_SHORT);
						toast.show();
					} else {
						toast.setDuration(Toast.LENGTH_SHORT);
						toast.setText("请打开无线");
						toast.show();
					}
					return;
				}
				final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
						.findFragmentById(R.id.frag_list);
				resetData();
				fragment.onInitiateDiscovery();
				manager.discoverPeers(channel,
						new WifiP2pManager.ActionListener() {

							@Override
							public void onSuccess() {
								if (toast == null) {
									toast = Toast.makeText(
											WiFiDirectActivity.this,
											"正在初始化......", Toast.LENGTH_SHORT);
									toast.show();
								} else {
									toast.setDuration(Toast.LENGTH_SHORT);
									toast.setText("正在初始化......");
									toast.show();
								}
							}

							@Override
							public void onFailure(int reasonCode) {
								if (toast == null) {
									toast = Toast.makeText(
											WiFiDirectActivity.this, "搜索失败",
											Toast.LENGTH_SHORT);
									toast.show();
								} else {
									toast.setDuration(Toast.LENGTH_SHORT);
									toast.setText("搜索失败");
									toast.show();
								}
							}
						});
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		if (progressDialog != null)
			progressDialog.dismiss();
		super.onPause();
	}

	public void showPopup(View v) {
		PopupMenu popup = new PopupMenu(this, v);
		popup.setOnMenuItemClickListener(this);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.main_menu, popup.getMenu());
		popup.show();
	}

	/**
	 * Remove all peers and clear all fields. This is called on
	 * BroadcastReceiver receiving a state change event.
	 */
	public void resetData() {
		DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
				.findFragmentById(R.id.frag_list);
		if (fragmentList != null) {
			fragmentList.clearPeers();
		}
	}

	@Override
	public void showDetails(WifiP2pDevice device) {
		this.device = device;
		if (device.status == WifiP2pDevice.CONNECTED) {
			Intent intent = new Intent(WiFiDirectActivity.this,
					ChartMainActivity.class);
			intent.putExtra("device", device);
			startActivity(intent);
		} else {
			WifiP2pConfig config = new WifiP2pConfig();
			config.deviceAddress = device.deviceAddress;
			config.wps.setup = WpsInfo.PBC;
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			progressDialog = ProgressDialog.show(this, "按返回键撤销", "正在连接到 :"
					+ device.deviceName, true, true);
			connect(config);
		}

	}

	@Override
	public void connect(WifiP2pConfig config) {
		manager.connect(channel, config, new ActionListener() {

			@Override
			public void onSuccess() {
				Log.i("my", "开启握手");

			}

			@Override
			public void onFailure(int reason) {
				if (toast == null) {
					toast = Toast.makeText(WiFiDirectActivity.this,
							"连接失败，请重试！", Toast.LENGTH_SHORT);
					toast.show();
				} else {
					toast.setDuration(Toast.LENGTH_SHORT);
					toast.setText("连接失败，请重试！");
					toast.show();
				}
			}
		});
	}

	@Override
	public void disconnect() {
		manager.removeGroup(channel, new ActionListener() {

			@Override
			public void onFailure(int reasonCode) {
				Log.d(TAG, "断开失败码 :" + reasonCode);

			}

			@Override
			public void onSuccess() {
			}

		});
	}

	@Override
	public void onChannelDisconnected() {
		// we will try once more
		if (manager != null && !retryChannel) {
			if (toast == null) {
				toast = Toast.makeText(this, "通道丢失，请重新连接！", Toast.LENGTH_LONG);
				toast.show();
			} else {
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.setText("通道丢失，请重新连接！");
				toast.show();
			}
			resetData();
			retryChannel = true;
			manager.initialize(this, getMainLooper(), this);
		} else {
			Toast.makeText(
					this,
					"严重!通道可能失去,尝试禁用/启用P2P。",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void cancelDisconnect() {

		if (manager != null) {
			final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
					.findFragmentById(R.id.frag_list);
			if (fragment.getDevice() == null
					|| fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
				disconnect();
			} else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
					|| fragment.getDevice().status == WifiP2pDevice.INVITED) {

				manager.cancelConnect(channel, new ActionListener() {

					@Override
					public void onSuccess() {
						Toast.makeText(WiFiDirectActivity.this, "连接异常终止",
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onFailure(int reasonCode) {
//						Toast.makeText(WiFiDirectActivity.this,
//								"连接异常代码: " + reasonCode, Toast.LENGTH_SHORT)
//								.show();
					}
				});
			}
		}

	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		// TODO Auto-generated method stub
		DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
				.findFragmentById(R.id.frag_list);
		String name = null;
		String address = null;
		int imgnum = 0;
		if (fragmentList != null) {
			name = fragmentList.GetMyName();
			imgnum = fragmentList.GetMyImg();
			address = fragmentList.GetMyAddress();
		}
		this.info = info;
		// 开启服务
		Intent serviceIntent = new Intent();
		serviceIntent.setClass(WiFiDirectActivity.this, SocketService.class);
		serviceIntent.setAction(SocketService.ACTION_SEND_FILE);
		serviceIntent.putExtra(SocketService.EXTRAS_GROUP_OWNER_ADDRESS,
				info.groupOwnerAddress.getHostAddress());
		Log.i("ip", info.groupOwnerAddress.getHostAddress());
		serviceIntent.putExtra(SocketService.EXTRAS_GROUP_OWNER_PORT, 8988);
		serviceIntent.putExtra(SocketService.GROUP_FORMED, info.groupFormed);
		serviceIntent.putExtra(SocketService.GROUP_OWNER, info.isGroupOwner);
		serviceIntent.putExtra("name", name);
		serviceIntent.putExtra("imgnum", Integer.toString(imgnum));
		serviceIntent.putExtra("address", address);
		startService(serviceIntent);
		// 跳转页面
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		this.disconnect();
		if (progressDialog != null)
			progressDialog.dismiss();
		WifiManager wifiManager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(false);
		Intent it = new Intent(this, SocketService.class);
		stopService(it);
		super.onDestroy();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.link_send:
			SendAPK.getInstance(WiFiDirectActivity.this).sendFile();
			return true;
		case R.id.link_share:
			share.getInstance(WiFiDirectActivity.this, mClientId).show(
					getWindow().getDecorView(), mPageContent,
					UIWidgetStyle.IOS_LIKE, new IBaiduListener() {

						@Override
						public void onError(BaiduException arg0) {
							// TODO Auto-generated method stub
							Toast.makeText(WiFiDirectActivity.this, "分享成功", 1)
									.show();
						}

						@Override
						public void onComplete(JSONArray arg0) {
							// TODO Auto-generated method stub
							Toast.makeText(WiFiDirectActivity.this, "分享成功", 1)
									.show();
						}

						@Override
						public void onComplete(JSONObject arg0) {
							// TODO Auto-generated method stub
							Toast.makeText(WiFiDirectActivity.this, "分享成功", 1)
									.show();
						}

						@Override
						public void onComplete() {
							// TODO Auto-generated method stub
							Toast.makeText(WiFiDirectActivity.this, "分享失败", 1)
									.show();
						}

						@Override
						public void onCancel() {
							// TODO Auto-generated method stub
							Toast.makeText(WiFiDirectActivity.this, "分享失败", 1)
									.show();
						}
					});
			return true;
		case R.id.link_help:
			Log.i("Menu", "help");
			Intent it = new Intent(WiFiDirectActivity.this, HelpActivity.class);
			startActivity(it);
			return true;

		default:
			return false;
		}
	}
}
