package com.fived.welink.bgservice;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.fived.welink.R;
import com.fived.welink.appshare.AppInfo;
import com.fived.welink.appshare.AppManager;
import com.fived.welink.chart.ChartMainActivity;
import com.fived.welink.dataproc.ChartInfo;
import com.fived.welink.dataproc.DBHelper;
import com.fived.welink.dataproc.OpenFile;
import com.fived.welink.game.FiveChessInfo;
import com.fived.welink.game.FiveChessMainActivity;
import com.fived.welink.game.GameInfo;
import com.fived.welink.wifidirect.WiFiDirectActivity;

/**
 * ��̨service�����ڱ�����Ŀ����ӵ�SocketͨѶ��������������ݵ�Э������
 */
public class SocketService extends android.app.Service {

	public static final String CHART_HEAD = "charth:";

	public static final String RE_APP_LIST_HEAD = "reapph:";
	public static final String RE_APP_HEAD = "anameh:";

	public static final String APP_LIST_HEAD = "applih:";
	public static final String APP_HEAD = "geapph:";

	public static final String ACCEPT_FILE_HEAD = "apfile:";

	public static final String FRIEND_INFO = "frinfo:";

	// game
	public static final String GAME_HEAD = "apgame:";

	public static final int SEND_STATUS_SEND = 0;
	public static final int SEND_STATUS_NORMAL = 2;

	private static final int SOCKET_TIMEOUT = 5000;
	public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
	public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
	public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
	public static final String GROUP_FORMED = "group_formed";
	public static final String GROUP_OWNER = "group_owner";
	public static String SDCARD_PATH = "";
	public static String DST_PATH = "";

	public Socket socket;
	public ArrayList<ChartInfo> chatMsgList = new ArrayList<ChartInfo>();
	public OutputStream out;// �����
	public InputStream in;// ������
	private Lock locker = new ReentrantLock();

	private int sendStatus = SEND_STATUS_NORMAL;

	private double downLoadProgress = 0;
	private ArrayList<AppInfo> myAppInfoList = null;
	private ArrayList<AppInfo> otherAppInfoList = null;
	private boolean isDownLoadingApp = false;
	private boolean isUpdateMyAppList = false;
	private boolean isUpdateOtherAppList = false;
	private boolean isDownloadSuccessful = false;

	private String downloadAppName = "";
	private String downloadAppPath = "";
	private String sendAppName = "";

	// ����֪ͨ����ʾ
	private NotificationManager manager;
	private Notification notifDwonload = null;
	private Notification notifSend = null;
	private MyHandler handler = null;
	// private PendingIntent pIntent = null;

	// ��������Ϸ,�ݶ�
	private ArrayList<GameInfo> gameInfoList = new ArrayList<GameInfo>();

	@Override
	public void onCreate() {
		System.out.println("onCreate");
		// ֪ͨ��
		// Intent intent = new Intent(SocketService.this, TmpActivity.class);
		// // intent.putExtra("path", downloadAppPath);
		// pIntent = PendingIntent.getActivity(SocketService.this, 0, intent,
		// 0);

		notifDwonload = new Notification();
		notifSend = new Notification();
		handler = new MyHandler();
		manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		notifDwonload.icon = R.drawable.notif_download;
		notifDwonload.tickerText = "�µ���������";
		notifDwonload.contentView = new RemoteViews(getPackageName(),
				R.layout.notif_content_view);

		notifSend.icon = R.drawable.notif_send;
		notifSend.tickerText = "�µķ�������";
		notifSend.contentView = new RemoteViews(getPackageName(),
				R.layout.notif_content_view);

		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		System.out.println("onStartCommand");
		//
		SDCARD_PATH = Environment.getExternalStorageDirectory().getPath();
		DST_PATH = SDCARD_PATH + "/WeLinkDownload/";
		if (myAppInfoList == null)
			new GetMyAppListThread().start();
		if (socket == null) {
			Thread t = new SocketCreateThread(intent);
			t.start();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		System.out.println("Service onDestroy");
		if (socket != null) {
			try {
				in.close();
				out.close();
				socket.close();
			} catch (Exception e) {
			}
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		System.out.println("onBind");
		return new ServiceBinder();
	}

	//
	class SocketCreateThread extends Thread {
		private Intent intent = null;

		public SocketCreateThread(Intent it) {
			this.intent = it;
		}

		@Override
		public void run() {
			if (intent == null) {
				return;
			}
			if (intent.getAction().equals(ACTION_SEND_FILE)) {
				boolean formed = intent.getExtras().getBoolean(GROUP_FORMED);
				boolean owner = intent.getExtras().getBoolean(GROUP_OWNER);
				String deviceName = intent.getStringExtra("name");
				String imgNum = intent.getStringExtra("imgnum");
				String macAddress = intent.getStringExtra("address");
				ServerSocket serverSocket = null;
				try {
					if (formed && owner) {
						serverSocket = new ServerSocket(8988);
						socket = serverSocket.accept();
						// socket.setSoTimeout(5000);
						out = socket.getOutputStream();
						Log.i("file", "�����socket");
						new ReadThread().start();
					} else if (formed) {
						String host = intent.getExtras().getString(
								EXTRAS_GROUP_OWNER_ADDRESS);
						int port = intent.getExtras().getInt(
								EXTRAS_GROUP_OWNER_PORT);
						socket = new Socket();
						Log.d(WiFiDirectActivity.TAG,
								"Opening client socket - ");
						socket.connect((new InetSocketAddress(host, port)),
								SOCKET_TIMEOUT);
						// socket.setSoTimeout(5000);
						out = socket.getOutputStream();
						Log.i("file", "�ͻ���socket");
						new ReadThread().start();
					}

					Intent intent = new Intent();
					intent.putExtra("device", deviceName);
					intent.setClass(SocketService.this, ChartMainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);

					sendMsg(FRIEND_INFO, deviceName, imgNum + "=" + macAddress);
				} catch (IOException e) {
					Log.e(WiFiDirectActivity.TAG, e.getMessage());
				} finally {
					if (serverSocket != null) {
						try {
							serverSocket.close();
						} catch (Exception e) {
							// Give up
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public class ServiceBinder extends Binder {

		/**
		 * �ж������Ƿ����
		 */
		public boolean isDownloadSuccessful() {
			return isDownloadSuccessful;
		}

		/**
		 * ������ؽ���
		 */
		public double getProgress() {
			return downLoadProgress;
		}

		/**
		 * �������ؽ���
		 */
		public synchronized void setProgress(double arg) {
			downLoadProgress = arg;
		}

		/**
		 * ����app
		 */
		public void DownloadApp(String dir, String name) {
			new SendMsgThread(RE_APP_HEAD, dir, name).start();
			// new MyThread(sc, name).start();
		}

		/**
		 * �ж�����״̬
		 */
		public boolean isConnected() {
			if (socket.isConnected())
				return true;
			return false;
		}

		/**
		 * �������״̬
		 */
		public boolean isDownloadApp() {
			return isDownLoadingApp;
		}

		/**
		 * ��������״̬
		 */
		public synchronized void setDownloadApp(boolean b) {
			isDownLoadingApp = b;
		}

		/**
		 * ��ñ���Ӧ����Ϣ����״̬
		 */
		public boolean isUpdateMyAppList() {
			return isUpdateMyAppList;
		}

		/**
		 * ���ľĿ�����Ӧ����Ϣ����״̬
		 */
		public boolean isUpdateOtherAppList() {
			return isUpdateOtherAppList;
		}

		/**
		 * ����������Ϣ
		 */
		public synchronized void sendChartMsg(String str) {
			new SendMsgThread(CHART_HEAD, str).start();
		}

		/**
		 * ��ñ���Ӧ����Ϣ�б�
		 */
		public ArrayList<AppInfo> getMyAppList() {
			return myAppInfoList;
		}

		/**
		 * ���ͻ��Ŀ�����Ӧ���б�����
		 */
		public void requestOtherAppList() {
			System.out.println("�����ú����б�");
			isUpdateOtherAppList = true;
			new SendMsgThread(RE_APP_LIST_HEAD).start();
		}

		/**
		 * ���Ŀ�����Ӧ���б�
		 */
		public ArrayList<AppInfo> getOtherAppList() {
			return otherAppInfoList;
		}

		/**
		 * ������Ϸ��Ϣ
		 * */
		public void sendGameInfo(String arg1, String arg2) {
			new SendMsgThread(GAME_HEAD, arg1, arg2).start();
		}

		public void sendGameInfo(String arg1) {
			new SendMsgThread(GAME_HEAD, arg1).start();
		}

		/**
		 * ���������ļ�
		 */
		public void sendSelectedApp(String dir, String name) {
			// new SendAppThread(dir, name).start();
			new SendMsgThread(ACCEPT_FILE_HEAD, dir, name).start();
		}

		/**
		 * ��Ϸ���ݽ���
		 */

		public ArrayList<GameInfo> getGameInfo() {
			return gameInfoList;
		}

		/**
		 * ��ȡ������Ϣ�����е�������Ϣ��¼
		 */
		public ArrayList<ChartInfo> getMsg() {
			ArrayList<ChartInfo> chat_copy = null;
			locker.lock(); // ����
			try {
				if (chatMsgList.size() != 0) {
					chat_copy = new ArrayList<ChartInfo>();
					for (int i = 0; i < chatMsgList.size(); ++i) {
						int flag = chatMsgList.get(i).flag;
						String content = chatMsgList.get(i).content;
						chat_copy.add(new ChartInfo(flag, content));
					}
					chatMsgList.clear();
				}
			} finally {
				locker.unlock(); // ����
			}
			return chat_copy;
		}
	}

	// ����,һֱѭ���������յ��İ�ͷ�͵�ǰ״̬
	// ����Ӧ�Ĵ���
	class ReadThread extends Thread {
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			// Looper.prepare();
			try {
				in = socket.getInputStream();
				int tmp = 0;
				while (true) {
					byte buffer_in[] = new byte[1024 * 32];
					tmp = in.read(buffer_in);
					String judgeHead = new String(buffer_in, 0, 7);
					String str = "";
					String arg1 = "";
					String arg2 = "";
					System.out.println("�ļ�:" + tmp + "start" + str + ":end");
					try {
						str = new String(buffer_in, 7, tmp - 7);
						arg1 = str.substring(str.indexOf("<") + 1,
								str.lastIndexOf(">"));
						arg2 = str.substring(str.lastIndexOf("[") + 1,
								str.lastIndexOf("]"));
						System.out.println(arg1 + "   " + arg2);
					} catch (Exception e) {
						judgeHead = "gg";
					}
					// ����
					if (judgeHead.equals(CHART_HEAD)) {
						addMsgToChartList(ChartMainActivity.OTHER, arg1);
					}
					// ������Ϣ
					else if (judgeHead.equals(FRIEND_INFO)) {
						String name = arg1;
						String[] atStr = arg2.split("=");
						int imgNum = Integer.parseInt(atStr[0]);
						String macAddress = atStr[1];
						SQLiteDatabase sql_write = DBHelper.getInstance(SocketService.this).getWritableDatabase();
						String sql = "update deviceinfo set name=?,imagenum=? where address=?";
						sql_write.execSQL(sql, new Object[]{name, imgNum, macAddress});
					}
					// �����ȡӦ���б�
					else if (judgeHead.equals(RE_APP_LIST_HEAD)) {
						System.out.println("�����ȡӦ���б�");
						new SendMyAppListThread().start();
					}
					// �Ƿ�ͬ������ļ�
					else if (judgeHead.equals(ACCEPT_FILE_HEAD)) {
						String arg = arg1 + "=" + arg2;
						Message msg = new Message();
						msg.what = 10;
						msg.obj = arg;
						handler.sendMessage(msg);
					}
					// ��Ϸ
					else if (judgeHead.equals(GAME_HEAD)) {
						if (arg1.equals("invite")) {
							String arg = arg1 + "=" + arg2;
							Message msg = new Message();
							msg.what = 11;
							msg.obj = arg;
							handler.sendMessage(msg);
						} else if (arg1.equals("accept")) {
							Intent intent = new Intent();
							intent.putExtra("role", FiveChessInfo.ROLE_BLACK);
							intent.setClass(SocketService.this,
									FiveChessMainActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						} else if (arg1.equals("running")) {
							String[] arg = arg2.split("=");
							int r = Integer.parseInt(arg[0]);
							int c = Integer.parseInt(arg[1]);
							GameInfo gf = new FiveChessInfo(r, c);
							gameInfoList.add(gf);
						}

					}
					// �����ȡӦ��
					else if (judgeHead.equals(RE_APP_HEAD)) {
						System.out.println("�����ȡӦ��");
						new SendAppThread(arg1, arg2).start();
					}
					// ��ȡӦ���б�
					else if (judgeHead.equals(APP_LIST_HEAD)) {
						System.out.println("��ȡӦ���б�");
						ObjectInputStream ois = null;
						try {
							ois = new ObjectInputStream(in);
							otherAppInfoList = (ArrayList<AppInfo>) ois
									.readObject();
							System.out.println(otherAppInfoList.size());

						} catch (Exception e) {
							System.out.println("ddd" + e.toString());
						} finally {
							System.out.println("isUpdateOtherAppList:false");
							isUpdateOtherAppList = false;
						}
					}
					// ���Ӧ��
					else if (judgeHead.equals(APP_HEAD)) {
						socket.setSoTimeout(5000);
						downloadAppName = arg1;
						downloadAppPath = DST_PATH + arg1;
						int tp = 0;
						long recvSize = 0;
						long size = Long.parseLong(arg2);
						double lastP = 0;
						byte[] inputByte = null;
						DataInputStream dis = null;
						FileOutputStream fos = null;
						System.out.println("���Ӧ��");
						System.out.println("Ӧ������" + arg1);

						try {
							dis = new DataInputStream(in);
							new File(DST_PATH).mkdir();
							/*
							 * �ļ��洢λ��
							 */
							File dFile = new File(DST_PATH + arg1);
							dFile.createNewFile();// �����ڣ��½�
							fos = new FileOutputStream(dFile);
							inputByte = new byte[1024 * 32];
							System.out.println("��ʼ��������...");
							while ((tp = dis.read(inputByte, 0,
									inputByte.length)) > 0) {
								fos.write(inputByte, 0, tp);
								// fos.flush();
								recvSize += tp;
								downLoadProgress = (double) recvSize * 1.0
										/ size;
								if (downLoadProgress - lastP > 0.03) {
									lastP = downLoadProgress;
									Message msg = new Message();
									msg.what = 0;
									msg.obj = lastP;
									handler.sendMessage(msg);
								}
								if (size <= recvSize) {
									isDownloadSuccessful = true;
									handler.sendEmptyMessage(1);
									break;
								}
							}
							sendMsg(CHART_HEAD, arg1 + " �������");
							addMsgToChartList(ChartMainActivity.ME, arg1
									+ " �������");
							OpenFile openFile = new OpenFile(downloadAppPath,
									getApplicationContext());
							openFile.Open();

						} catch (Exception e) {
							isDownloadSuccessful = false;
							handler.sendEmptyMessage(4);
						} finally {
							System.out.println("����isdownloading");
							socket.setSoTimeout(1000000);
							downLoadProgress = 0;
							isDownLoadingApp = false;
						}
					}
				}// while(true)
			} catch (IOException e) {
				System.out.println("ReadException:" + e.toString());
			}
		}
	}

	// ������Ϣ�߳�
	class SendMsgThread extends Thread {
		private String chartMsg = null;
		private int status;

		public SendMsgThread(String head, String content1, String content2,
				int status) {
			chartMsg = head + "<" + content1 + ">" + "[" + content2 + "]";
			this.status = status;
		}

		public SendMsgThread(String head, String content1, int status) {
			this(head, content1, "", status);
		}

		public SendMsgThread(String head, String content1) {
			this(head, content1, "", SEND_STATUS_NORMAL);
		}

		public SendMsgThread(String head) {
			this(head, "", "", SEND_STATUS_NORMAL);
		}

		public SendMsgThread(String head, String content1, String content2) {
			this(head, content1, content2, SEND_STATUS_NORMAL);
		}

		@Override
		public void run() {
			while (sendStatus != status) {
				try {
					Thread.sleep(500);
				} catch (Exception e) {
				}
			}
			sendMsg(chartMsg);
			super.run();
		}
	}

	//
	public synchronized void sendMsg(String msg) {
		byte buffer_out[] = new byte[1024 * 32];
		buffer_out = msg.getBytes();
		int tmp = buffer_out.length;
		try {
			out.write(buffer_out, 0, tmp);
			out.flush();
		} catch (IOException e) {
			System.out.println("Exception��������Ϣ" + e.toString());
		}
	}

	//
	public synchronized void sendMsg(String head, String arg1, String arg2) {
		String msg = head + "<" + arg1 + ">" + "[" + arg2 + "]";
		sendMsg(msg);
	}

	//
	public synchronized void sendMsg(String head, String arg1) {
		String msg = head + "<" + arg1 + ">" + "[" + "" + "]";
		sendMsg(msg);
	}

	// ���myAppList
	class GetMyAppListThread extends Thread {

		@Override
		public void run() {
			isUpdateMyAppList = true;
			if (myAppInfoList == null)
				myAppInfoList = AppManager.getInstance(SocketService.this)
						.installPackagesInfo();
			isUpdateMyAppList = false;
			super.run();
		}

	}

	// ����myAppList
	class SendMyAppListThread extends Thread {
		@Override
		public void run() {
			sendStatus = SEND_STATUS_SEND;
			System.out.println("����myAppList");
			sendMsg(APP_LIST_HEAD, "", "");
			while (isUpdateMyAppList) {
				try {
					Thread.sleep(500);
				} catch (Exception e) {
				}
			}
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(out);
				oos.writeObject(myAppInfoList);
			} catch (Exception e) {
			} finally {
				sendStatus = SEND_STATUS_NORMAL;
			}
			super.run();
		}
	}

	// ����Ӧ��
	class SendAppThread extends Thread {
		String appDir = null;
		String appName = null;

		public SendAppThread(String dir, String name) {
			this.appDir = dir;
			this.appName = name;
		}

		@Override
		public void run() {
			int flag = 0;
			while (sendStatus != SEND_STATUS_NORMAL) {
				if (flag == 0) {
					Toast.makeText(getApplicationContext(),
							"�����������ڽ����У����Ժ��͡���", Toast.LENGTH_SHORT).show();
					flag = 1;
				}
				try {
					Thread.sleep(500);
				} catch (Exception e) {
				}
			}
			sendStatus = SEND_STATUS_SEND;
			sendAppName = appName;
			byte[] sendBytes = null;
			FileInputStream fis = null;
			DataOutputStream dos = null;
			int tmp = 0;
			long sendSize = 0;
			long size = 0;
			double progress;
			double lastP = 0;
			try {
				File file = new File(appDir); // Ҫ������ļ�·��
				size = file.length();
				dos = new DataOutputStream(out);
				fis = new FileInputStream(file);

				sendMsg(APP_HEAD, appName, Long.toString(size));

				sendBytes = new byte[1024 * 32];
				while ((tmp = fis.read(sendBytes, 0, sendBytes.length)) > 0) {
					System.out.println("����Ӧ��:" + tmp);
					dos.write(sendBytes, 0, tmp);
					// dos.flush();
					sendSize += tmp;

					progress = (double) sendSize / size;
					if (progress - lastP > 0.03) {
						lastP = progress;
						Message msg = new Message();
						msg.what = 2;
						msg.obj = lastP;
						handler.sendMessage(msg);
					}
				}
				//

				handler.sendEmptyMessage(3);
				//
				addMsgToChartList(ChartMainActivity.ME, appName + " �������");

			} catch (Exception e) {
				System.out.println("Exception:����Ӧ��" + e.toString());
			} finally {
				sendStatus = SEND_STATUS_NORMAL;
			}
			super.run();
		}
	}

	public void addMsgToChartList(int flag, String msg) {
		locker.lock(); // ����
		try {
			chatMsgList.add(new ChartInfo(flag, msg));
		} finally {
			locker.unlock(); // ����
		}
	}

	// ֪ͨ����Ϣ
	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			double progress = 0;
			String name = "";
			if (msg.what == 2 || msg.what == 3) {
				name = sendAppName;
			} else {
				name = downloadAppName;
			}
			if (name.length() > 12) {
				name = name.substring(0, 12) + "����";
			}
			switch (msg.what) {
			case 0:
				// notifDwonload.contentIntent = null;
				progress = (Double) (msg.obj);
				notifDwonload.contentView.setImageViewResource(
						R.id.content_view_image, R.drawable.notif_download);
				notifDwonload.contentView.setTextViewText(
						R.id.content_view_text1, name + " ������:"
								+ (int) (progress * 100) + "%");
				notifDwonload.contentView.setProgressBar(
						R.id.content_view_progress, 100,
						(int) (progress * 100), false);
				manager.notify(0, notifDwonload);

				break;
			case 1:
				// notifDwonload.contentIntent = pIntent;
				notifDwonload.contentView.setTextViewText(
						R.id.content_view_text1, name + " ������ɣ�");
				notifDwonload.contentView.setProgressBar(
						R.id.content_view_progress, 100, 100, false);
				manager.notify(0, notifDwonload);
				break;
			case 2:
				progress = (Double) (msg.obj);
				notifSend.contentView.setImageViewResource(
						R.id.content_view_image, R.drawable.notif_send);
				notifSend.contentView.setTextViewText(R.id.content_view_text1,
						name + " ������:" + (int) (progress * 100) + "%");
				notifSend.contentView.setProgressBar(
						R.id.content_view_progress, 100,
						(int) (progress * 100), false);
				manager.notify(1, notifSend);

				break;
			case 3:
				notifSend.contentView.setTextViewText(R.id.content_view_text1,
						name + " ������ɣ�");
				notifSend.contentView.setProgressBar(
						R.id.content_view_progress, 100, 100, false);
				manager.notify(1, notifSend);
				break;
			case 4:
				notifDwonload.contentView.setTextViewText(
						R.id.content_view_text1, name + " ����ʧ�ܣ�");
				manager.notify(0, notifDwonload);
				break;
			case 10:
				String argStr = (String) (msg.obj);
				String[] arg = argStr.split("=");
				AlertDialog.Builder bulider = new AlertDialog.Builder(
						SocketService.this);
				bulider.setTitle("��Ϣ");
				bulider.setMessage("�Ƿ���� " + arg[1] + "?");
				bulider.setPositiveButton("ȷ��", new FileAcDlgOnClickListener(
						arg[0], arg[1]));
				bulider.setNegativeButton("ȡ��", new FileAcDlgOnClickListener(
						arg[0], arg[1]));
				AlertDialog alert = bulider.create();
				alert.getWindow().setType(
						WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				alert.show();
				break;
			case 11:
				AlertDialog.Builder bulider1 = new AlertDialog.Builder(
						SocketService.this);
				bulider1.setTitle("��Ϣ");
				bulider1.setMessage("����������һ������Ϸ���Ƿ���� ?");
				bulider1.setPositiveButton("����", new GameAcDlgOnClickListener());
				bulider1.setNegativeButton("�ܾ�", new GameAcDlgOnClickListener());
				AlertDialog alert1 = bulider1.create();
				alert1.getWindow().setType(
						WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				alert1.show();
				break;
			default:
				break;
			}
		}
	}

	// �Ի���ȷ����ȡ��������
	class FileAcDlgOnClickListener implements DialogInterface.OnClickListener {

		private String dir;
		private String name;

		FileAcDlgOnClickListener(String arg1, String arg2) {
			this.dir = arg1;
			this.name = arg2;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case -1:
				new SendMsgThread(RE_APP_HEAD, dir, name).start();
				break;
			case -2:
				new SendMsgThread(CHART_HEAD, "ȡ������" + name).start();
				break;
			}
		}
	}

	// ��Ϸ����
	class GameAcDlgOnClickListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case -1:
				new SendMsgThread(GAME_HEAD, "accept").start();
				Intent intent = new Intent();
				intent.putExtra("role", FiveChessInfo.ROLE_White);
				intent.setClass(SocketService.this, FiveChessMainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				break;
			case -2:
				new SendMsgThread(CHART_HEAD, "�ܾ��������Ϸ����").start();
				break;
			}
		}
	}

}
