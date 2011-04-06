package com.test.devcampdemo.services;

import java.util.List;

import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.test.devcampdemo.db.PuneDemoDatabase;
import com.test.devcampdemo.db.QuestionTable;
import com.test.devcampdemo.helpers.LocationUpdateTrigger;
import com.test.devcampdemo.helpers.LocationUpdateTrigger.LocationResultExecutor;
import com.test.devcampdemo.models.Question;

public class QuestionUploadService extends WakeEventService {

	private static final String LOG_TAG = "QuestionUploadService";
	private SQLiteOpenHelper database;

	@Override
	public void onCreate() {
		super.onCreate();
		database = new PuneDemoDatabase(getApplicationContext());
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
			(new LocationUpdateTrigger(getApplicationContext(), new LocationResultExecutor() {
				@Override
				public void executeWithUpdatedLocation(Location location) {
					List<Question> allQuestions = new QuestionTable(database).findAllWithoutLocation();
					if(allQuestions != null && allQuestions.size() > 0) {
						for(Question eachQuestion : allQuestions) {
							eachQuestion.setLatitude(Double.toString(location.getLatitude()));
							eachQuestion.setLongitude(Double.toString(location.getLongitude()));
							
							//Instead of looping on individual questions I can do a batch update
							//as long as I have all the ids from the database. However for demo
							//purpose, I'll ignore the effort for that and just do an individual 
							//entity update.
							new QuestionTable(database).updateQuestion(eachQuestion);
						}
					}
				}
			})).fetchLatestLocation();
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
