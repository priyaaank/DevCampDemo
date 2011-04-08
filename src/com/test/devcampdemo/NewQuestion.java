package com.test.devcampdemo;

import android.app.Activity;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.test.devcampdemo.db.PuneDemoDatabase;

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
				
				//TODO - All steps below in a separate thread
				//TODO - Implement saving the question to database
				//TODO - Acquire lock to service
				//TODO - Invoke service
				
				//Close self.
				finish();
			}
		});
	}
}