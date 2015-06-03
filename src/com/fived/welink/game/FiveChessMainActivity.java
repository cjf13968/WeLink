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
		// 获取窗口属性
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		// 窗口宽度
		int screenWidth = dm.widthPixels;
		// 窗口高度
		int screenHeight = dm.heightPixels;

		gameView = new FiveChessView(this, screenWidth, screenHeight, role);
		setContentView(gameView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("重新开始").setIcon(android.R.drawable.ic_menu_myplaces);
		menu.add("退出");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals("重新开始")) {
			gameView.canPlay = true;
			gameView.chess = new int[gameView.row][gameView.col];
			gameView.invalidate();
		} else if (item.getTitle().equals("退出")) {
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
			// 创建退出对话框
			AlertDialog.Builder isExit = new AlertDialog.Builder(this);
			// 设置对话框标题
			isExit.setTitle("提示");
			// 设置对话框消息
			isExit.setMessage("正在游戏中,确定要退出吗?");
			// 添加选择按钮并注册监听
			isExit.setPositiveButton("确定", listener);
			isExit.setNegativeButton("取消", listener);
			// 显示对话框
			isExit.show();

		}

		return false;
	}

	/* 监听对话框里面的button点击事件 */
	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
				String arg = -1 + "=" + -1;
				gameView.binder.sendGameInfo("running", arg);
				finish();
				break;
			case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
				break;
			default:
				break;
			}
		}
	};

}