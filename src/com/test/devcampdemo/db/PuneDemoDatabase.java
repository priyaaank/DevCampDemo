package com.test.devcampdemo.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.test.devcampdemo.R;

public class PuneDemoDatabase extends SQLiteOpenHelper {

	//This is the database name that we defined
	public static final String DB_NAME = "PUNE_DEMO";
	public static final int version = 1;
	private static final String LOG_TAG = "PuneDemoDatabase";
	private Context context;
	
	public PuneDemoDatabase(Context context) {
		super(context, DB_NAME, null, version);
		this.context = context;
	}

	/**
	 * This method is called "once" by android framework, when app tries to access db related stuff for first time. It is a place holder to create
	 * database tables and any seed data. It is not called with upgrades. 
	 * 
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.beginTransaction();
		//Just fetching all the migration sqls from the string.xml 
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

	//Run multiple sql statements in db
	private void executeMultipleSqls(String[] sqls, SQLiteDatabase db) {
		for(String eachSql : sqls) {
			if(eachSql.trim().length() > 0) {
				db.execSQL(eachSql);
			}
		}
	}

	/**
	 * This is called by the android framework when app is upgraded from an older version to newer version.
	 * We should do stuff here, which will upgrade our database. And other relevant changes to data.
	 * 
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}
