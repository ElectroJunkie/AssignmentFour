package com.example.assignmentthree;

import android.app.Application;

import com.box.boxandroidlibv2.BoxAndroidClient;

public class BoxApplication extends Application{
	
	public static final String CLIENT_ID = "dbny43rs8lzrz3e6j5y7c8h23lfwstis";
	public static final String CLIENT_SECRET = "xUfokz84FdFnJiPPvQ2LXQvYGKaMdjaY";
	public static final String REDIRECT_URL = "https://www.seanrose.co";
	
	private BoxAndroidClient boxClient;
	
	// Set Box client
	public void setClient(BoxAndroidClient client){
		this.boxClient = client;
	}
	// Get Box client
	public BoxAndroidClient getClient(){
		return boxClient;
	}
}