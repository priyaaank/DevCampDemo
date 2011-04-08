package com.test.devcampdemo.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

import com.test.devcampdemo.models.Question;

public class QuestionTable {
	
	private static final String TABLE_NAME = "questions";
	private static final String LOG_TAG= "QuestionTable";
	private SQLiteOpenHelper puneDemoDatabase;
	
	public QuestionTable(SQLiteOpenHelper database) {
		this.puneDemoDatabase = database;
	}
	
	//A cursor is a nice wrapper to the data structure that carries data from database to program.
	//There are better implementations for an object cursor, however for a simplistic view, I have
	//gone ahead with a private class and a rudimentary implementation.
	private static class QuestionCursor extends SQLiteCursor {

		public static final String ID_QUERY = "Select * from questions where id = ? ";
		public static final String ALL_QUERY_WITHOUT_LOCATION = "Select * from questions where latitude is null or longitude is null ";
		public static final String ALL_QUERY_WITH_LOCATION = "Select * from questions where latitude is not null or longitude is not null ";

		public QuestionCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
			super(db, driver, editTable, query);
		}

		//Factory used by android to obtain an instance of the cursor object. This is needed.
		private static class Factory implements SQLiteDatabase.CursorFactory {
			@Override
			public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
				return new QuestionCursor(db, driver, editTable, query);
			}
		}
		
		private String getQuestionText() {
			return getString(getColumnIndexOrThrow("text"));
		}
		
		private String getQuestionTitle() {
			return getString(getColumnIndexOrThrow("title"));
		}
		
		private long getId() {
			return getLong(getColumnIndexOrThrow("id"));
		}

		private String getLatitude() {
			return getString(getColumnIndexOrThrow("latitude"));
		}
		
		private String getLongitude() {
			return getString(getColumnIndexOrThrow("longitude"));
		}
		
		//This is the method, that returns a populated object
		public Question getQuestion() {
			return new Question(getId(), getQuestionText(), getQuestionTitle(), getLatitude(), getLongitude());
		}
	}
	
	public List<Question> findAllWithoutLocation() {
		return findQuestions(QuestionCursor.ALL_QUERY_WITHOUT_LOCATION, null);
	}

	public List<Question> findAllWithLocation() {
		return findQuestions(QuestionCursor.ALL_QUERY_WITH_LOCATION, null);
	}
	
	public Question findById(long id) {
		String id_string = Long.toString(id);
		List<Question> questionList = findQuestions(QuestionCursor.ID_QUERY, new String[] {id_string});
		return (questionList == null || questionList.isEmpty()) ? null : questionList.get(0);
	}

	public Question create(Question newQuestion) {
		if (newQuestion != null) {
			
			//Writes to database can be made transactional as in this example.
			puneDemoDatabase.getWritableDatabase().beginTransaction();
			try {
				ContentValues dbValues = new ContentValues();
				dbValues.put("text", newQuestion.getText());
				dbValues.put("title", newQuestion.getTitle());
				dbValues.put("latitude", newQuestion.getLatitude());
				dbValues.put("longitude", newQuestion.getLongitude());
				
				//The second parameter supplied in insertOrThrow method is to cover for null hack, where no fields/values
				//are supplied to be inserted in database. It's a field name, which android can substitue in that case to
				//be null explicitly. So that eventually it will be able to say, "insert into questions (text) values (null)"
				long id = puneDemoDatabase.getWritableDatabase().insertOrThrow(TABLE_NAME, "text", dbValues);
				newQuestion.setId(id);
				puneDemoDatabase.getWritableDatabase().setTransactionSuccessful();
			} catch (SQLException sqle) {
				Log.e(LOG_TAG, "Could not create new review. Exception is :" + sqle.getMessage());
			} finally {
				puneDemoDatabase.getWritableDatabase().endTransaction();
			}
		}
		return newQuestion;
	}
	
	protected List<Question> findQuestions(String query, String[] params) {
		Cursor questionCursor = null;
		List<Question> questionList = new ArrayList<Question>();
		try {
			questionCursor = puneDemoDatabase.getReadableDatabase().rawQueryWithFactory(new QuestionCursor.Factory(), query, params, null);
			if(questionCursor != null && questionCursor.moveToFirst()) {
				do {
					questionList.add(((QuestionCursor)questionCursor).getQuestion());
				} while(questionCursor.moveToNext());
			}
		} catch(SQLException sqle) {
			Log.e(LOG_TAG, "Could not look up the reviews with params "+ params +". The error is: "+ sqle.getMessage());
		}
		finally {
			if(questionCursor != null && !questionCursor.isClosed()) {
				questionCursor.close();
			}
		}
		return questionList;
	}

	public void updateQuestion(Question eachQuestion) {
		if (eachQuestion != null) {
			long id = eachQuestion.getId();
			Log.i("QuestionsTable", "Updating records for question with id :"+ id);
			Log.i("QuestionsTable", "Updating records for question with id :"+ eachQuestion.getLongitude() + "         " + eachQuestion.getLatitude());
			if(id > 0) {
				ContentValues valuesToUpdate = new ContentValues();
				puneDemoDatabase.getWritableDatabase().beginTransaction();
				try {
					valuesToUpdate.put("title", eachQuestion.getTitle());
					valuesToUpdate.put("text", eachQuestion.getText());
					valuesToUpdate.put("longitude", eachQuestion.getLongitude());
					valuesToUpdate.put("latitude", eachQuestion.getLatitude());
					//Use ? parameterized sql queries, else you'll be exposed to sql injection here, if you make direct string substitution
					puneDemoDatabase.getWritableDatabase().update(TABLE_NAME, valuesToUpdate, " ID = ?", new String[]{Long.toString(id)});
					puneDemoDatabase.getWritableDatabase().setTransactionSuccessful();
				} catch (SQLException sqle) {
					Log.e("Question Table", "Error while updating the field for the table. Error is :" + sqle.getMessage());
				} finally {
					puneDemoDatabase.getWritableDatabase().endTransaction();
				}
			}
		}
	}
}
