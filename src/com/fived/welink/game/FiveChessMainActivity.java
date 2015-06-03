package com.fived.welink.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class FiveChessMainActivity extends Activity {
	FiveChessView gameView = null;
	public boolean isRun = true;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		String role = this.getIntent().getStringExtra("role");

		DisplayMetrics dm = new DisplayMetrics();
		// ��ȡ��������
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		// ���ڿ��
		int screenWidth = dm.widthPixels;
		// ���ڸ߶�
		int screenHeight = dm.heightPixels;

		gameView = new FiveChessView(this, screenWidth, screenHeight, role);
		setContentView(gameView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("���¿�ʼ").setIcon(android.R.drawable.ic_menu_myplaces);
		menu.add("�˳�");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals("���¿�ʼ")) {
			gameView.canPlay = true;
			gameView.chess = new int[gameView.row][gameView.col];
			gameView.invalidate();
		} else if (item.getTitle().equals("�˳�")) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		isRun = false;
		unbindService(gameView.conn);
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// �����˳��Ի���
			AlertDialog.Builder isExit = new AlertDialog.Builder(this);
			// ���öԻ������
			isExit.setTitle("��ʾ");
			// ���öԻ�����Ϣ
			isExit.setMessage("������Ϸ��,ȷ��Ҫ�˳���?");
			// ���ѡ��ť��ע�����
			isExit.setPositiveButton("ȷ��", listener);
			isExit.setNegativeButton("ȡ��", listener);
			// ��ʾ�Ի���
			isExit.show();

		}

		return false;
	}

	/* �����Ի��������button����¼� */
	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case AlertDialog.BUTTON_POSITIVE:// "ȷ��"��ť�˳�����
				String arg = -1 + "=" + -1;
				gameView.binder.sendGameInfo("running", arg);
				finish();
				break;
			case AlertDialog.BUTTON_NEGATIVE:// "ȡ��"�ڶ�����ťȡ���Ի���
				break;
			default:
				break;
			}
		}
	};

}