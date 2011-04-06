package com.test.devcampdemo;

import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		database = new PuneDemoDatabase(getApplicationContext());
		reviewTable = new QuestionTable(database);
		setContentView(R.layout.question_list);
		
		new QuestionsFetchAsyncTask().execute();
		bindTitleBarButtons();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(database != null)
			database.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(0, QUIT, 0, getString(R.string.quit)).setIcon(R.drawable.cancel);
	    return true;
	}
	
	private void updateListingContentsWith(List<Question> questions) {
		if(questions != null  && questions.size() > 0) {
			this.questions = questions;
			String[] questionTitles = new String[questions.size()];
			int index = 0;
			for(Question eachQuestion : questions) {
				questionTitles[index++] = eachQuestion.getTitle();
			}
			messageListAdapter = new ArrayAdapter<String>(this, R.layout.question, questionTitles);
			setListAdapter(messageListAdapter);
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if(questions != null && questions.size() >= position) {
			String uri = "geo:"+ questions.get(position).getLatitude() +","+ questions.get(position).getLongitude() +"?z=15";
			startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
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
				startActivity(newReviewIntent);
			}
		});
		
		ImageView onDemandRefresh = (ImageView) findViewById(R.id.refresh_icon);
		onDemandRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new QuestionsFetchAsyncTask().execute();
			}
		});
	}
	
	private class QuestionsFetchAsyncTask extends AsyncTask<Void, Void, List<Question>> {
		private final ProgressDialog dialog = new ProgressDialog(QuestionListing.this);

		@Override
		protected void onPreExecute() {
			this.dialog.setMessage("Fetching...");
			this.dialog.show();
		}
		
		@Override
		protected List<Question> doInBackground(Void... params) {
			return QuestionListing.this.populateReviewList();
		}
			
		@Override
		protected void onPostExecute(List<Question> result) {
			
			QuestionListing.this.updateListingContentsWith(result);

			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
		}
	}
}
