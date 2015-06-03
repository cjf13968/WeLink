package com.fived.welink.appshare;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;

/**
 * ��¼Ӧ�õ���Ϣ
 */
public class AppInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	public String appName = "";
	public String packageName = "";
	public String versionName = "";
	public long appSize = 0;
	public String sourceDir = "";
	public int versionCode = 0;
	public MyBitmap appIcon = null;
	public boolean sharenable = true;
	public boolean isDownloading = false;
	public String btnText = "����";
	public int btnTextColor = Color.rgb(0, 0, 0);
	public int progress = 0;
	

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		AppInfo appInfo = (AppInfo) o;
		if (appInfo.packageName.equals(this.packageName)
				&& appInfo.versionName.equals(this.versionName))
			return true;
		else
			return false;
	}
}

class BytesBitmap {
	public static Bitmap getBitmap(byte[] data) {
		return BitmapFactory.decodeByteArray(data, 0, data.length);
	}

	public static byte[] getBytes(Bitmap bitmap) {
		ByteArrayOutputStream baops = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 0, baops);
		return baops.toByteArray();
	}
}

/**
 * 91 * MyBitmap��Ҫ�����л����� 92 * ���а�����ͨ��BytesBitmap��õ���Bitmap�����ݵ����� 93 *
 * ��һ������λͼ�����ֵ��ַ��������ڱ�ʶͼƬ 94 * @author joran 95 * 96
 */
class MyBitmap implements Serializable {
	/**
	 * serialVersionUID����:
	 * http://www.blogjava.net/invisibletank/archive/2007/11/15/160684.html
	 */
	private static final long serialVersionUID = 1L;
	private byte[] bitmapBytes = null;

	public MyBitmap(byte[] bitmapBytes) {
		// TODO Auto-generated constructor stub
		this.bitmapBytes = bitmapBytes;
	}

	public byte[] getBitmapBytes() {
		return this.bitmapBytes;
	}
}
