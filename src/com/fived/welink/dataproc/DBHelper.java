package com.fived.welink.dataproc;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "WeLink.db";
	private static final int DATABASE_VERSION = 1;

	public static DBHelper dbHelper = null;

	public static DBHelper getInstance(Context ct) {
		if (dbHelper == null)
			dbHelper = new DBHelper(ct);
		return dbHelper;
	}

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql = "create table if not exists deviceinfo (address varchar(20), name varchar(12), status integer, online integer, imagenum integer)";
		String sql_my = "create table if not exists mydevice (address varchar(20), name varchar(12), status integer, online integer, imagenum integer)";
		db.execSQL(sql);
		db.execSQL(sql_my);
		Log.i("sqlite", "onCreate");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}
	

}
