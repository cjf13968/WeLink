package com.fived.welink.game;

import com.fived.welink.bgservice.SocketService;
import com.fived.welink.bgservice.SocketService.ServiceBinder;
import com.fived.welink.game.FiveChessInfo;


import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class FiveChessView extends View {

	FiveChessMainActivity context = null;
	public ServiceBinder binder = null;

	int nowR = -1, nowC = -1; // ��ǰ��������
	int screenWidth, screenHeight;
	String message = "";// ��ʾ�ֵ��ĸ����
	String role = FiveChessInfo.ROLE_BLACK;// ��ɫ��white,black
	int row = 19, col = 12; // ���ߵ�����������
	int stepLength = 60;// ����ÿ����
	int chessSize = 23; // ���Ӱ뾶
	int startX = 65;
	int startY = 80;
	int[][] chess = null;// 0����û�����ӣ�1�����Ǻ��壬2�������
	boolean isBlack = true;
	boolean canPlay = true;

	public FiveChessView(Context context, int screenWidth, int screenHeight,
			String role) {
		super(context);
		this.context = (FiveChessMainActivity) context;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.stepLength = screenWidth / 13;
		this.chessSize = this.stepLength / 2 - 5;
		this.role = role;
		this.message = "��������";
		// row = (screenHeight - startY) / stepLength;
		// col = (screenWidth - startX) / stepLength + 1;

		chess = new int[row][col];
		canPlay = (role.equals(FiveChessInfo.ROLE_BLACK) ? true : false);
		InfogetThread.start();
		Intent intent = new Intent(this.context, SocketService.class);
		this.context.bindService(intent, conn, Context.BIND_AUTO_CREATE);

	}

	// ��Service��
	public ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (ServiceBinder) service;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	};
	//
	Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			FiveChessInfo fv = (FiveChessInfo) (msg.obj);
			if (fv.c < 0) {
				Toast.makeText(context, "�������˳���Ϸ��", Toast.LENGTH_SHORT).show();
				context.finish();
				return;
			}
			chess[fv.r][fv.c] = (role.equals(FiveChessInfo.ROLE_BLACK) ? 2 : 1);
			canPlay = true;
			update(fv.r, fv.c);
		}

	};
	// ��ú��ѵ�������Ϣ
	Thread InfogetThread = new Thread() {
		@Override
		public void run() {
			super.run();
			while (context.isRun) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
				if (binder.getGameInfo().isEmpty()) {
					continue;
				} else {
					FiveChessInfo fv1 = (FiveChessInfo) binder.getGameInfo()
							.get(0);
					FiveChessInfo fv2 = new FiveChessInfo(fv1.r, fv1.c);
					binder.getGameInfo().clear();
					Message msg = new Message();
					msg.obj = fv2;
					myHandler.sendMessage(msg);
				}
			}
		}
	};

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		canvas.drawRect(0, 0, screenWidth, screenHeight, paint);// ������
		paint.setColor(Color.BLUE);
		paint.setTextSize(40);
		canvas.drawText(message, (screenWidth - 150) / 2, 40, paint);// ��������
		paint.setColor(Color.RED);
		canvas.drawText("���� ", screenWidth / 3 * 2 + 40, 55, paint);
		//
		if (this.role.equals(FiveChessInfo.ROLE_BLACK)) {
			paint.setColor(Color.BLACK);
			paint.setStyle(Style.FILL);
			canvas.drawCircle(screenWidth / 5 * 4 + 50, 40, chessSize / 3 * 2,
					paint);
		} else {
			paint.setColor(Color.BLACK);
			paint.setStyle(Style.STROKE);
			canvas.drawCircle(screenWidth / 5 * 4 + 50, 40, chessSize / 3 * 2,
					paint);
		}
		//
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(3.0f);
		// ������
		for (int i = 0; i < row; i++) {
			canvas.drawLine(startX, startY + i * stepLength, startX + (col - 1)
					* stepLength, startY + i * stepLength, paint);
		}
		for (int i = 0; i < col; i++) {
			canvas.drawLine(startX + i * stepLength, startY, startX + i
					* stepLength, startY + (row - 1) * stepLength, paint);
		}

		for (int r = 0; r < row; r++) {
			for (int c = 0; c < col; c++) {
				if (chess[r][c] == 1) {
					// ������
					paint.setColor(Color.BLACK);
					paint.setStyle(Style.FILL);
					canvas.drawCircle(startX + c * stepLength, startY + r
							* stepLength, chessSize, paint);
				} else if (chess[r][c] == 2) {
					// ������
					paint.setColor(Color.WHITE);
					paint.setStyle(Style.FILL);
					canvas.drawCircle(startX + c * stepLength, startY + r
							* stepLength, chessSize, paint);

					paint.setColor(Color.BLACK);
					paint.setStyle(Style.STROKE);
					canvas.drawCircle(startX + c * stepLength, startY + r
							* stepLength, chessSize, paint);
				}
			}
		}
		// ������ӵ�
		if (nowC != -1) {
			if (canPlay)
				paint.setColor(Color.RED);
			else
				paint.setColor(Color.GREEN);
			paint.setStrokeWidth(5.0f);
			int x1 = startX + nowC * stepLength - chessSize - 2;
			int y1 = startY + nowR * stepLength - chessSize - 2;
			int x2 = startX + nowC * stepLength + chessSize + 2;
			int y2 = startY + nowR * stepLength + chessSize + 2;
			paint.setStyle(Style.STROKE);
			canvas.drawLine(x1, y1, x1, y1 + chessSize / 2, paint);
			canvas.drawLine(x1, y1, x1 + chessSize / 2, y1, paint);

			canvas.drawLine(x1, y2, x1, y2 - chessSize / 2, paint);
			canvas.drawLine(x1, y2, x1 + chessSize / 2, y2, paint);

			canvas.drawLine(x2, y1, x2, y1 + chessSize / 2, paint);
			canvas.drawLine(x2, y1, x2 - chessSize / 2, y1, paint);

			canvas.drawLine(x2, y2, x2, y2 - chessSize / 2, paint);
			canvas.drawLine(x2, y2, x2 - chessSize / 2, y2, paint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!canPlay) {
			return false;
		}
		float x = event.getX();
		float y = event.getY();
		int r = Math.round((y - startY) / stepLength);
		int c = Math.round((x - startX) / stepLength);
		if (r < 0 || r > row - 1 || c < 0 || c > col - 1) {
			return false;
		}
		if (chess[r][c] != 0) {
			return false;
		}// �����������ٻ�������
		if (this.role.equals(FiveChessInfo.ROLE_BLACK)) {
			chess[r][c] = 1;

		} else {
			chess[r][c] = 2;
		}
		String arg = r + "=" + c;
		binder.sendGameInfo("running", arg);
		canPlay = false;
		update(r, c);
		return super.onTouchEvent(event);
	}

	private void update(int r, int c) {
		nowR = r;
		nowC = c;
		message = this.message.equals("�ֵ�����") ? "�ֵ�����" : "�ֵ�����";
		invalidate();
		if (judge(r, c, 0, 1))
			return;
		if (judge(r, c, 1, 0))
			return;
		if (judge(r, c, 1, 1))
			return;
		if (judge(r, c, 1, -1))
			return;
	}

	private boolean judge(int r, int c, int x, int y) {// r��c��ʾ�к��У�x��ʾ��y�����ϵ�ƫ�ƣ�y��ʾ��x�����ϵ�ƫ��
		int count = 1;
		int a = r;
		int b = c;
		while (r >= 0 && r < row && c >= 0 && c < col && r + x >= 0
				&& r + x < row && c + y >= 0 && c + y < col
				&& chess[r][c] == chess[r + x][c + y]) {
			count++;
			if (y > 0) {
				c++;
			} else if (y < 0) {
				c--;
			}
			if (x > 0) {
				r++;
			} else if (x < 0) {
				r--;
			}
		}
		while (a >= 0 && a < row && b >= 0 && b < col && a - x >= 0
				&& a - x < row && b - y >= 0 && b - y < col
				&& chess[a][b] == chess[a - x][b - y]) {
			count++;
			if (y > 0) {
				b--;
			} else if (y < 0) {
				b++;
			}
			if (x > 0) {
				a--;
			} else if (x < 0) {
				a++;
			}
		}
		if (count >= 5) {
			String str = "";
			if (canPlay) {
				str = "�r(�s���t)�q�����ˣ�";
			} else {
				str = "O(��_��)O������Ӯ�ˣ�";
			}
			new AlertDialog.Builder(context)
					.setTitle("��Ϸ����")
					.setMessage(str)
					.setPositiveButton("���¿�ʼ",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									chess = new int[row][col];
									nowC = -1;
									// FiveChessView.this.role =
									// (FiveChessView.this.role
									// .equals(FiveChessInfo.ROLE_BLACK) ?
									// FiveChessInfo.ROLE_White
									// : FiveChessInfo.ROLE_BLACK);
									invalidate();
								}
							}).show();
			return true;
		}

		return false;
	}
}
