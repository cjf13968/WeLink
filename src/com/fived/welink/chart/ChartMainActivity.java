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
 * ����Activity
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
	 * �������������ļ�ʹ����ͬһ��id������һ���Ƿ���� TT��ʵ֤����ز���id��ƥ���쳣�����Ի���Ҫ�ֿ�����
	 * 
	 * contactName���ڽ���Intent���ݵ�contactName�����������������ݿ��е���ص���ϵ����Ϣ����ʱʹ��һ��ͷ��
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
		addTextToList("�Ჱ�ӽ�㣬������Ͻǵġ��ļ��С����Դ��ļ�Ŷ�� ", OTHER);
		addTextToList("С�֣������ص��ļ����������ﰡ��", ME);
		addTextToList("�����ڴ洢����WeLinkDownload�ļ��� ", OTHER);
		addTextToList("�õģ�֪���ˣ�", ME);
		addTextToList("������Ϸ��ġ�һ������Ϸ�����������滥����ϷŶ�� ", OTHER);

		contactName = this.getIntent().getStringExtra("device");

		chatSendButton = (Button) findViewById(R.id.chat_buttom_send);
		exitButton = (Button) findViewById(R.id.chart_top_exit);
		appShareButton = (Button) findViewById(R.id.chat_bottom_tran);
		contactInfoButton = (Button) findViewById(R.id.chat_top_contactinfo);
		editText = (EditText) findViewById(R.id.chat_bottom_edittext);
		chatListView = (ListView) findViewById(R.id.chat_list);
		gameView = (TextView) findViewById(R.id.chat_contact_name);
		gameView.setText("һ������Ϸ");

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
							"������ѷ������룬�ȴ�ȷ�ϡ���", Toast.LENGTH_LONG);
					toast.show();
				} else {
					toast.setDuration(Toast.LENGTH_LONG);
					toast.setText("������ѷ������룬�ȴ�ȷ�ϡ���");
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
				intent.putExtra("explorer_title", "ѡ��������ļ�");
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
				 * ����һ��������Ϣ�ļ�������ע������ı�����û�����ݣ���ôgetText()�ķ���ֵ����Ϊ
				 * null����ʱ����toString()�����쳣��������������ں������һ��""��ʽת����Stringʵ��
				 * �����Ҳ��ܷ��Ϳ���Ϣ��
				 */

				myWord = (editText.getText() + "").toString();
				if (myWord.length() == 0)
					return;
				editText.setText("");
				addTextToList(myWord, ME);
				/**
				 * ���������б�����ͨ��setSelection����ʹListViewʼ�չ�������׶�
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
				// "�����쳣����", Toast.LENGTH_LONG);
				// toast.show();
				// } else {
				// toast.setDuration(Toast.LENGTH_LONG);
				// toast.setText("�����쳣");
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
