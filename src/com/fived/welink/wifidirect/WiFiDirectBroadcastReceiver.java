package com.fived.welink.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;
import android.widget.Toast;

import com.fived.welink.R;
import com.fived.welink.bgservice.SocketService;
import com.fived.welink.wifidirect.DeviceListFragment.DeviceActionListener;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

	private WifiP2pDevice device = null;
	private WifiP2pManager manager;
	private Channel channel;
	private WiFiDirectActivity activity;
	private Toast toast = null;

	public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
			WiFiDirectActivity activity) {
		super();
		this.manager = manager;
		this.channel = channel;
		this.activity = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		//	当启用或禁用设备上的Wi-Fi Direct时，发出这个广播
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

			// UI update to indicate wifi p2p status.
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				// Wifi Direct mode is enabled
				activity.setIsWifiP2pEnabled(true);
				final DeviceListFragment fragment = (DeviceListFragment) activity
						.getFragmentManager().findFragmentById(R.id.frag_list);
				fragment.onInitiateDiscovery();
				manager.discoverPeers(channel,
						new WifiP2pManager.ActionListener() {

							@Override
							public void onSuccess() {
								if (toast == null) {
									toast = Toast.makeText(activity,
											"正在初始化......", Toast.LENGTH_SHORT);
									toast.show();
								} else {
									toast.setText("正在初始化......");
									toast.setDuration(Toast.LENGTH_SHORT);
									toast.show();
								}
							}

							@Override
							public void onFailure(int reasonCode) {
								if (toast == null) {
									toast = Toast.makeText(activity,
											"Discovery Failed : " + reasonCode,
											Toast.LENGTH_SHORT);
									toast.show();
								} else {
									toast.setText("搜索失败: " + reasonCode);
									toast.setDuration(Toast.LENGTH_SHORT);
									toast.show();
								}
							}
						});
			} else {
				activity.setIsWifiP2pEnabled(false);
				activity.resetData();

			}
			Log.d(WiFiDirectActivity.TAG, "P2P state changed - " + state);
			//	在调用discoverPeers()方法时，发出这个广播，如果你要在应用程序中处理这个Intent，
			//	通常是希望调用requestPeers()方法来获取对等设备的更新列表
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

			// request available peers from the wifi p2p manager. This is an
			// asynchronous call and the calling activity is notified with a
			// callback on PeerListListener.onPeersAvailable()
			if (manager != null) {
				manager.requestPeers(channel, (PeerListListener) activity
						.getFragmentManager().findFragmentById(R.id.frag_list));
			}
			Log.d(WiFiDirectActivity.TAG, "P2P peers changed");
			//	在设备的Wi-Fi连接状态变化时，发出这个广播
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
				.equals(action)) {

			if (manager == null) {
				return;
			}

			NetworkInfo networkInfo = (NetworkInfo) intent
					.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

			if (networkInfo.isConnected() && device != null) {

				// we are connected with the other device, request connection
				// info to find group owner IP
				manager.requestConnectionInfo(channel, activity);
			} else {
				// It's a disconnect
				if (toast == null) {
					toast = Toast.makeText(activity, "连接已断开，请退回主界面重新连接",
							Toast.LENGTH_LONG);
					toast.show();
				} else {
					toast.setText("连接已断开，请退回主界面重新连接");
					toast.setDuration(Toast.LENGTH_LONG);
					toast.show();
				}
				activity.resetData();
				((DeviceActionListener) activity).disconnect();
				((DeviceActionListener) activity).cancelDisconnect();
				Intent it = new Intent(activity, SocketService.class);
				activity.stopService(it);			
			}
			//	当设备的细节（如设备的名称）发生变化时，发出这个广播
		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
				.equals(action)) {
			device = (WifiP2pDevice) intent
					.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
			DeviceListFragment fragment = (DeviceListFragment) activity
					.getFragmentManager().findFragmentById(R.id.frag_list);
			fragment.updateThisDevice(device);
		}
	}
}
