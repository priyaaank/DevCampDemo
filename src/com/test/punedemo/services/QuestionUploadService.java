package com.test.punedemo.services;

import java.util.List;

import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.IBinder;
import android.util.Log;

import com.test.punedemo.db.PuneDemoDatabase;
import com.test.punedemo.db.QuestionTable;
import com.test.punedemo.models.Question;

public class QuestionUploadService extends WakeEventService {

	private static final String LOG_TAG = "QuestionUploadService";
	private SQLiteOpenHelper database;

	@Override
	public void onCreate() {
		super.onCreate();
		database = new PuneDemoDatabase(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(database != null)
			database.close();
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }
	
	@Override
	public void doServiceTask() {
		try {
			List<Question> allQuestions = new QuestionTable(database).findAll();
			if(allQuestions != null && allQuestions.size() > 0) {
				for(Question eachQuestion : allQuestions) {
					((Question)eachQuestion).submitToService(this);
				}
			}
		} catch(Exception e) {
			Log.e(LOG_TAG, e.getMessage());
		} finally {
			this.stopSelf();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
