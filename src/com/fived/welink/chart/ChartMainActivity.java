package com.fived.welink.chart;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;

import com.fived.welink.R;
import com.fived.welink.appshare.AppShareMainActivity;
import com.fived.welink.bgservice.SocketService;
import com.fived.welink.bgservice.SocketService.ServiceBinder;
import com.fived.welink.dataproc.ChartInfo;
import com.fived.welink.dataproc.Utils;
import com.fived.welink.fileshare.FileShareMainActivity;
import com.fived.welink.game.FiveChessMainActivity;

import java.io.File;
import java.util.*;

/**
 * 聊天Activity
 */
public class ChartMainActivity extends Activity {
	private static final int REQUEST_EX = 1;

	ArrayList<HashMap<String, Object>> chatList = null;
	String[] from = { "image", "text" };
	int[] to = { R.id.chatlist_image_me, R.id.chatlist_text_me,
			R.id.chatlist_image_other, R.id.chatlist_text_other };
	int[] layout = { R.layout.chat_listitem_me, R.layout.chat_listitem_other };
	String contactName = " ";
	/**
	 * 这里两个布局文件使用了同一个id，测试一下是否管用 TT事实证明这回产生id的匹配异常！所以还是要分开。。
	 * 
	 * contactName用于接收Intent传递的contactName，进而用来调用数据库中的相关的联系人信息先暂时使用一个头像
	 */

	public final static int OTHER = 1;
	public final static int ME = 0;

	protected ListView chatListView = null;
	protected Button chatSendButton = null;
	protected Button appShareButton = null;
	protected Button contactInfoButton = null;
	protected Button exitButton = null;
	protected EditText editText = null;
	protected TextView gameView = null;

	protected ServiceBinder binder = null;
	protected MyChatAdapter adapter = null;
	protected GetMsgHandler getMsgHandler = null;
	protected Toast toast = null;
	
	//
	protected boolean isRun = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chart_activity);

		Intent intent = new Intent(ChartMainActivity.this, SocketService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		//
		// Intent it = this.getIntent();
		// Bundle bl = it.getExtras();
		//
		chatList = new ArrayList<HashMap<String, Object>>();
		addTextToList("歪脖子姐姐，点击右上角的“文件夹”可以传文件哦！ ", OTHER);
		addTextToList("小胖，那下载的文件保存在哪里啊？", ME);
		addTextToList("保存在存储卡的WeLinkDownload文件下 ", OTHER);
		addTextToList("好的，知道了！", ME);
		addTextToList("点击正上方的“一起玩游戏”，还可以玩互动游戏哦！ ", OTHER);

		contactName = this.getIntent().getStringExtra("device");

		chatSendButton = (Button) findViewById(R.id.chat_buttom_send);
		exitButton = (Button) findViewById(R.id.chart_top_exit);
		appShareButton = (Button) findViewById(R.id.chat_bottom_tran);
		contactInfoButton = (Button) findViewById(R.id.chat_top_contactinfo);
		editText = (EditText) findViewById(R.id.chat_bottom_edittext);
		chatListView = (ListView) findViewById(R.id.chat_list);
		gameView = (TextView) findViewById(R.id.chat_contact_name);
		gameView.setText("一起玩游戏");

		adapter = new MyChatAdapter(this, chatList, layout, from, to);

		appShareButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ChartMainActivity.this,
						AppShareMainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);

				overridePendingTransition(R.anim.push_bottom_in,
						R.anim.push_top_out);

			}
		});

		gameView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (toast == null) {
					toast = Toast.makeText(ChartMainActivity.this,
							"已向好友发出邀请，等待确认……", Toast.LENGTH_LONG);
					toast.show();
				} else {
					toast.setDuration(Toast.LENGTH_LONG);
					toast.setText("已向好友发出邀请，等待确认……");
					toast.show();
				}
				if (Utils.isFastDoubleClick(2000))
					return;
				binder.sendGameInfo("invite");
			}
		});

		exitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ChartMainActivity.this.finish();
			}
		});
		contactInfoButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("explorer_title", "选择待发送文件");
				intent.setDataAndType(
						Uri.fromFile(new File(SocketService.SDCARD_PATH)),
						"*/*");
				intent.setClass(ChartMainActivity.this,
						FileShareMainActivity.class);
				startActivityForResult(intent, REQUEST_EX);
			}
		});

		chatSendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String myWord = null;

				/**
				 * 这是一个发送消息的监听器，注意如果文本框中没有内容，那么getText()的返回值可能为
				 * null，这时调用toString()会有异常！所以这里必须在后面加上一个""隐式转换成String实例
				 * ，并且不能发送空消息。
				 */

				myWord = (editText.getText() + "").toString();
				if (myWord.length() == 0)
					return;
				editText.setText("");
				addTextToList(myWord, ME);
				/**
				 * 更新数据列表，并且通过setSelection方法使ListView始终滚动在最底端
				 */
				adapter.notifyDataSetChanged();
				chatListView.setSelection(chatList.size() - 1);
				binder.sendChartMsg(myWord);

			}
		});
		chatListView.setAdapter(adapter);
		//
	}

	@Override
	protected void onStart() {
		getMsgHandler = new GetMsgHandler();
		Thread t = new GetMsgThread();
		t.start();
		super.onStart();
	}

	//
	class GetMsgHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			ArrayList<ChartInfo> newMsgList = (ArrayList<ChartInfo>) (msg.obj);
			for (int i = 0; i < newMsgList.size(); ++i) {
				ChartInfo info = newMsgList.get(i);
				addTextToList(info.content, info.flag);
			}
			adapter.notifyDataSetChanged();
			chatListView.setSelection(chatList.size() - 1);
			super.handleMessage(msg);
		}

	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_EX) {
				// Uri uri = intent.getData();
				// addTextToList(uri.toString(), ME);
				String path = intent.getStringExtra("path");
				String name = path.substring(path.lastIndexOf("/") + 1);
				binder.sendSelectedApp(path, name);
				adapter.notifyDataSetChanged();
				chatListView.setSelection(chatList.size() - 1);
			}
		}
	}

	//
	class GetMsgThread extends Thread {
		@Override
		public void run() {
			while (isRun) {
				try {
					Thread.sleep(500);
				} catch (Exception e) {
				}
				// if (binder != null) {
				// if (!binder.isConnected()) {
				// if (toast == null) {
				// toast = Toast.makeText(ChartMainActivity.this,
				// "连接异常……", Toast.LENGTH_LONG);
				// toast.show();
				// } else {
				// toast.setDuration(Toast.LENGTH_LONG);
				// toast.setText("连接异常");
				// toast.show();
				// }
				// }
				// }
				if (binder != null) {
					ArrayList<ChartInfo> getChartList = binder.getMsg();
					if (getChartList != null)
						getMsgHandler.obtainMessage(1, getChartList)
								.sendToTarget();
				}
			}
		}
	}

	//
	protected void addTextToList(String text, int who) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("person", who);
		map.put("image", who == ME ? R.drawable.contact_2
				: R.drawable.contact_3);
		map.put("text", text);
		chatList.add(map);
	}

	// conn
	ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			System.out.println("Chart-->onServiceConnected");
			binder = (ServiceBinder) service;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub

		}

	};

	private class MyChatAdapter extends BaseAdapter {

		Context context = null;
		ArrayList<HashMap<String, Object>> chatList = null;
		int[] layout;
		String[] from;
		int[] to;

		public MyChatAdapter(Context context,
				ArrayList<HashMap<String, Object>> chatList, int[] layout,
				String[] from, int[] to) {
			super();
			this.context = context;
			this.chatList = chatList;
			this.layout = layout;
			this.from = from;
			this.to = to;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return chatList.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		class ViewHolder {
			public ImageView imageView = null;
			public TextView textView = null;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			int who = (Integer) chatList.get(position).get("person");

			convertView = LayoutInflater.from(context).inflate(
					layout[who == ME ? 0 : 1], null);
			holder = new ViewHolder();
			holder.imageView = (ImageView) convertView
					.findViewById(to[who * 2 + 0]);
			holder.textView = (TextView) convertView
					.findViewById(to[who * 2 + 1]);

			System.out.println(holder);
			System.out.println("WHYWHYWHYWHYW");
			System.out.println(holder.imageView);
			holder.imageView.setBackgroundResource((Integer) chatList.get(
					position).get(from[0]));
			holder.textView.setText(chatList.get(position).get(from[1])
					.toString());
			return convertView;
		}
	}

	@Override
	protected void onDestroy() {
		isRun = false;
		unbindService(conn);
		super.onDestroy();
	}
}
