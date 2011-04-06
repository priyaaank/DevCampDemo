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
    protected static final int GET_LOCATION = 1;
	protected static final int TAKE_PICTURE = 2;
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
				(new Thread() {
					public void run() {
						String questionText = ((EditText)NewQuestion.this.findViewById(R.id.ask_question_box)).getText().toString();
						new QuestionTable(database).create(new Question(-1, questionText, questionText, null, null));
						QuestionUploadService.acquireStaticLock(getApplicationContext());
						startService(new Intent(getApplicationContext(), QuestionUploadService.class));
					}
				}).start();
				
				finish();
			}
		});
	}
}