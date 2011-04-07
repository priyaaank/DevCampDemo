package com.test.devcampdemo;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.test.devcampdemo.db.PuneDemoDatabase;
import com.test.devcampdemo.db.QuestionTable;
import com.test.devcampdemo.models.Question;
import com.test.devcampdemo.services.QuestionUploadService;

public class NewQuestion extends Activity {
	private SQLiteOpenHelper database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ask_question);
        database = new PuneDemoDatabase(getApplicationContext());
        setComponentListeners();
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	if(database != null)
    		database.close();
    }
    
    private void setComponentListeners() {
		findViewById(R.id.ask_question_submit_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Android has an inbuilt restriction of 5 seconds for responsiveness of any UI event. 
				//If app doesn't respond back within 5 seconds, then an ANR (app not responding) 
				//error is shown. To ensure, that our app is responsive; invocation of service can be in 
				//a separate thread. This would promise a better experience for user.
				(new Thread() {
					public void run() {
						String questionText = ((EditText)NewQuestion.this.findViewById(R.id.ask_question_box)).getText().toString();
						new QuestionTable(database).create(new Question(-1, questionText, questionText, null, null));
						
						//Before we invoke the service, acquire a lock on device, else between the 
						//time service is called and it's actually invoked, if device goes off to sleep
						//our service won't be able to run. More details on this in WakeEventService
						QuestionUploadService.acquireStaticLock(getApplicationContext());
						startService(new Intent(getApplicationContext(), QuestionUploadService.class));
					}
				}).start();
				
				//Close self.
				finish();
			}
		});
	}
}