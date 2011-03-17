package com.test.punedemo.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.test.punedemo.R;

public class PuneDemoDatabase extends SQLiteOpenHelper {

	public static final String DB_NAME = "PUNE_DEMO";
	public static final int version = 1;
	private static final String LOG_TAG = "PuneDemoDatabase";
	private Context context;
	
	public PuneDemoDatabase(Context context) {
		super(context, DB_NAME, null, version);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.beginTransaction();
		String[] sqls = context.getResources().getString(R.string.sqls).split(";");
		try {
			executeMultipleSqls(sqls, db);
			db.setTransactionSuccessful();
		} catch(SQLException sqle) {
			Log.e(LOG_TAG,sqle.getMessage());
		} finally {
			db.endTransaction();
		}
	}

	private void executeMultipleSqls(String[] sqls, SQLiteDatabase db) {
		for(String eachSql : sqls) {
			if(eachSql.trim().length() > 0) {
				db.execSQL(eachSql);
			}
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}
