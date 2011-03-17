package com.test.punedemo.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public class RemoteServiceWrapper {
	
	private static final String LOG_TAG = "RemoteServiceWrapper";
	private String remoteServiceUrl; 
	HttpClient httpClient = new DefaultHttpClient();
	
	public RemoteServiceWrapper(String remoteServiceUrl) {
		this.remoteServiceUrl = remoteServiceUrl;
	}
	
	public void postToRemoteService(String subUrl, Map<String, String> paramsMap) {
		Log.i(LOG_TAG, "Received a request to post remote to url" + (this.remoteServiceUrl + subUrl));
		if(paramsMap != null  && paramsMap.size() > 0) {
			List<NameValuePair> payload;
			HttpPost postRequest = new HttpPost((this.remoteServiceUrl + subUrl));
			payload = new ArrayList<NameValuePair>(paramsMap.size());
			for(String paramsName : paramsMap.keySet()) {
				payload.add(new BasicNameValuePair(paramsName, paramsMap.get(paramsName)));
			}
			
			try {
				postRequest.setEntity(new UrlEncodedFormEntity(payload));
				Log.i(LOG_TAG, "posting data: " + payload.toString());
				HttpResponse response;
				response = httpClient.execute(postRequest);			
				
				//successfully uploaded to mark pending review as complete
				if(response.getStatusLine().getStatusCode() == 201) {
					Log.i(LOG_TAG, "Uploaded successfully!");
				}
			} catch (UnsupportedEncodingException e) {
				Log.e(LOG_TAG, e.getMessage());
			} catch (ClientProtocolException e) {
				Log.e(LOG_TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
			}		
		}
	}

}
