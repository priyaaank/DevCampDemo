package com.test.devcampdemo;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.test.devcampdemo.db.PuneDemoDatabase;
import com.test.devcampdemo.db.QuestionTable;
import com.test.devcampdemo.models.Question;

public class QuestionListing extends ListActivity {
	
	private PuneDemoDatabase database;
	private QuestionTable reviewTable;
	private ArrayAdapter<String> messageListAdapter = null;
	private List<Question> questions;
	
	private final static int QUIT = 1;
	
	
	//Note: In this class, although we explicitly provide a button to do a manual refresh. However, what if we didn't want that? We wanted a auto refresh
	//of listing with latest content. Thats where the broadcast receivers can play a role. Once you register this class for a broadcast and once a 
	//broadcast is made, an auto refresh can be done. Which means, anytime a location is sucessfully obtained or a new question is created, a broadcast
	//can be made and auto refresh will happen here. (Only if listing is in forground, as it doesn't make sense otherwise to refresh stuff)
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		database = new PuneDemoDatabase(getApplicationContext());
		reviewTable = new QuestionTable(database);
		setContentView(R.layout.question_list);
		
		//Call the functionality specified in QuestionFetchAsyncTask into a separate thread. This implementation
		//takes care of handler and looper automatically. Every thing in Async task happens on UI thread except
		//processing done in doInBackground method; where network and blocking operations should be done. This is
		//one of the simplest implementations to handle network/blocking processing off UI thread and post 
		//results/notifications back to UI.
		new QuestionsFetchAsyncTask().execute();
		bindTitleBarButtons();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(database != null)
			database.close();
	}
	
	//Method to create a menu listing
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(0, QUIT, 0, getString(R.string.quit)).setIcon(R.drawable.cancel);
	    return true;
	}
	
	//messageListAdapter is a simple ArrayAdapter. For complex datasets it's possible to write custom adapters and they
	//are generally more suitable for most of the situations.
	private void updateListingContentsWith(List<Question> questions) {
		if(questions != null  && questions.size() > 0) {
			this.questions = questions;
			String[] questionTitles = new String[questions.size()];
			int index = 0;
			for(Question eachQuestion : questions) {
				questionTitles[index++] = eachQuestion.getTitle();
			}
			messageListAdapter = new ArrayAdapter<String>(this, R.layout.question, questionTitles);
			
			//One set, the listing view refreshes automatically with latest data.
			setListAdapter(messageListAdapter);
		}
	}
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if(questions != null && questions.size() >= position) {
			//This can be triggered into a separate thread, as I have noticed, sometimes maps take a while to start up.
			//The last variable "z" here specifies the zoom level on map, which is currently hard coded to 15.
			//Note: We do not call any intent specificall by name, we just provide the action and data (uri). Based on that
			//Google map is chosen, as it may be the only intent that can handle this action. If there were similar apps, which could
			//show a geo location and was exposed with same action and data; then android would give user an option to choose one of the
			//two possible apps. Similar to what happens when you have two browsers installed and you click on a link.
			//This feature is powerful, as you can expose your intents too and do something with a given data and action. This is declared in
			//Android manifest file.
			//TODO - Call the map intent with geo cordinates and zoom level
		}
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case QUIT:
	        this.finish();
	    }
	    return false;
	}
	
	private List<Question> populateReviewList() {
		return reviewTable.findAllWithLocation();
	}
	
	private void bindTitleBarButtons() {
		//New Review Button Binding
		ImageView newReviewImage = (ImageView) findViewById(R.id.new_review);
		newReviewImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent newReviewIntent = new Intent(QuestionListing.this, NewQuestion.class);
				//We don't finish the current activity here, which means it's not lost of closed. It's just get pushed down the stack (onPause)
				//and new spawned activity takes the foreground. Once the foreground activity ends, the control will come back to 
				//this activity (onResume). 
				startActivity(newReviewIntent);
			}
		});
		
		ImageView onDemandRefresh = (ImageView) findViewById(R.id.refresh_icon);
		onDemandRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Every time we want an on demand refresh, we do async task execution.
				new QuestionsFetchAsyncTask().execute();
			}
		});
	}
	
	//TODO - Remove this placeholder class and implement a AsyncTaskClass
	//TODO - Show progress on UI while the fetch is done from database
	private class QuestionsFetchAsyncTask {
		
		public void execute() {
			
		}
	}
}
