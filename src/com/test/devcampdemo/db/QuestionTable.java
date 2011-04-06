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
	
	private static class QuestionCursor extends SQLiteCursor {

		public static final String ID_QUERY = "Select * from questions where id = ? ";
		public static final String ALL_QUERY_WITHOUT_LOCATION = "Select * from questions where latitude is null or longitude is null ";

		public QuestionCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
			super(db, driver, editTable, query);
		}
		
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
		
		public Question getQuestion() {
			return new Question(getId(), getQuestionText(), getQuestionTitle(), getLatitude(), getLongitude());
		}
	}
	
	public List<Question> findAllWithoutLocation() {
		return findQuestions(QuestionCursor.ALL_QUERY_WITHOUT_LOCATION, null);
	}

	public Question findById(long id) {
		String id_string = Long.toString(id);
		List<Question> questionList = findQuestions(QuestionCursor.ID_QUERY, new String[] {id_string});
		return (questionList == null || questionList.isEmpty()) ? null : questionList.get(0);
	}

	public Question create(Question newQuestion) {
		if (newQuestion != null) {
			
			puneDemoDatabase.getWritableDatabase().beginTransaction();
			try {
				ContentValues dbValues = new ContentValues();
				dbValues.put("text", newQuestion.getText());
				dbValues.put("title", newQuestion.getTitle());
				dbValues.put("latitude", newQuestion.getLatitude());
				dbValues.put("longitude", newQuestion.getLongitude());
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
			ContentValues valuesToUpdate = new ContentValues();
			if(id > 0) {
				puneDemoDatabase.getWritableDatabase().beginTransaction();
				try {
					valuesToUpdate.put("title", eachQuestion.getTitle());
					valuesToUpdate.put("text", eachQuestion.getText());
					valuesToUpdate.put("longitude", eachQuestion.getLongitude());
					valuesToUpdate.put("latitude", eachQuestion.getLatitude());
					puneDemoDatabase.getWritableDatabase().update(TABLE_NAME, valuesToUpdate, " ID = ?", new String[]{Long.toString(id)});
					puneDemoDatabase.getWritableDatabase().setTransactionSuccessful();
				} catch (SQLException sqle) {
					Log.e("Pending Reviews Table", "Error while updating the field for the table. Error is :" + sqle.getMessage());
				} finally {
					puneDemoDatabase.getWritableDatabase().endTransaction();
				}
			}
		}
	}
}
