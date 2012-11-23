package com.jjs.demerits.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import com.jjs.demerits.shared.Note;

import android.app.Activity;

public class DemeritClient {
	private final String HOST = 
			"http://demeritsx.appspot.com";
	private final HttpClient client;
	private final Activity activity;
	private String email;
	public DemeritClient(Activity activity, String email) {
		this.activity = activity;
		this.email = email;
		client = new DefaultHttpClient();
	}
	
	public void sendNote(String note, String to) {
		
	}
	
	public List<Note> getNotes() {
		HttpPost post = new HttpPost();
		post.setURI(URI.create(
				String.format("%s/getNotes?user=%s",
						HOST, email)));
		System.err.println("post: " + post.getRequestLine());
		HttpResponse response;
		try {
			response = client.execute(post);
		} catch (ClientProtocolException e) {
			System.err.println("Error in http X request: " + e.getMessage());
			e.printStackTrace();
			return new ArrayList<Note>();
		} catch (IOException e) {
			System.err.println("Error in http request: " + e.getMessage());
			e.printStackTrace();
			return new ArrayList<Note>();
		}
		HttpEntity resEntity = response.getEntity();
		try {
			ObjectInputStream in = new ObjectInputStream(resEntity.getContent());
			return (List<Note>) in.readObject();
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<Note>();
	}
}
