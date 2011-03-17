package com.test.punedemo.models;

import java.util.HashMap;
import java.util.Map;

import com.test.punedemo.services.RemoteServiceWrapper;

import android.content.Context;

public class Question {
	
	private String title;
	private String text;
	private long id;
	private RemoteServiceWrapper remoteServiceWrapper;

	public Question(long id, String title, String text) {
		this.title = title;
		this.text = text;
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public void submitToService(Context context) {
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("question_title", getTitle());
		paramsMap.put("question_text", getText());
		String url = "http://crosstalk-ws.heroku.com";
		remoteServiceWrapper = new RemoteServiceWrapper(url);
		remoteServiceWrapper.postToRemoteService("/questions", paramsMap);
	}
}
