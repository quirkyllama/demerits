package com.jjs.demerits.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.jjs.demerits.shared.DemeritsProto;
import com.jjs.demerits.shared.DemeritsProto.NoteList;
import com.jjs.demerits.shared.Note;

import android.app.Activity;

public class DemeritClient {
	private final String HOST = 
			"http://demeritsx.appspot.com";
	private final HttpClient client;
	private final Activity activity;
	private String email;
	private String password;
	
	public DemeritClient(Activity activity) {
		this.activity = activity;
		client = new DefaultHttpClient();
	}
	
	public void sendNote(String note, String to) {
		
	}
	
	public NoteList getNotes() {
		HttpPost post = new HttpPost();
		post.setURI(URI.create(String.format("%s/getNotes", HOST)));
		List<NameValuePair> nameValuePairs = 
				new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("user", email));
		nameValuePairs.add(new BasicNameValuePair("password", password));
		System.err.println("user: '" + email + "' password: " + password);
		DemeritsProto.NoteList.Builder builder =
			DemeritsProto.NoteList.newBuilder();
		try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response;
			response = client.execute(post);
			HttpEntity resEntity = response.getEntity();
			builder.mergeFrom(resEntity.getContent());
		} catch (ClientProtocolException e) {
			System.err.println("Error in http X request: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error in http request: " + e.getMessage());
			e.printStackTrace();
		}
		return builder.build();
	}

	public void setCredentials(String email, String password) {
		this.email = email;
		this.password = password;	
	}

	public String getEmail() {
		return email;
	}
}
