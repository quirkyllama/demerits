package com.jjs.demerits.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.jjs.demerits.shared.DemeritsProto;
import com.jjs.demerits.shared.DemeritsProto.Note;
import com.jjs.demerits.shared.DemeritsProto.NoteList;

public class DemeritClient {
	private final String HOST = 
			"http://demeritsx.appspot.com";
	private final HttpClient client;
	private String email;
	private String password;
	
	public DemeritClient() {
		client = new DefaultHttpClient();
	}
	
	public synchronized NoteList getNotes() {
	    System.err.println("Get Notes");
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
            resEntity.consumeContent();
            System.err.println("Get Notes- content consumed");

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

  public synchronized boolean sendNote(Note note) {
    System.err.println("Sending note");
    HttpPost post = new HttpPost();
    post.setURI(URI.create(String.format("%s/postNote", HOST)));
    try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        note.writeTo(baos);
        post.setEntity(new ByteArrayEntity(baos.toByteArray()));
        HttpResponse response;
        response = client.execute(post);
        response.getEntity().consumeContent();
        if (response.getStatusLine().getStatusCode() != 200) {
          System.err.println("Error: " + response.getStatusLine().getStatusCode() );
          return false;
        }
        return true;
    } catch (ClientProtocolException e) {
        System.err.println("Error in http X request: " + e.getMessage());
        e.printStackTrace();
    } catch (IOException e) {
        System.err.println("Error in http request: " + e.getMessage());
        e.printStackTrace();
    }
    return false;
  }
  
  public synchronized boolean updateGcmId(String gcmId, boolean register) {
    System.err.println("Updating GCM: " + register + " gcmId: " + gcmId);
    DemeritsProto.UpdateGcmId update = DemeritsProto.UpdateGcmId.newBuilder()
        .setEmail(email)
        .setGcmId(gcmId)
        .setRegister(register)
        .build();
    HttpPost post = new HttpPost();
    post.setURI(URI.create(String.format("%s/updateGcmId", HOST)));
    try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        update.writeTo(baos);
        post.setEntity(new ByteArrayEntity(baos.toByteArray()));
        HttpResponse response;
        response = client.execute(post);
        response.getEntity().consumeContent();
        if (response.getStatusLine().getStatusCode() != 200) {
          System.err.println("Error: " + response.getStatusLine().getStatusCode() );
          return false;
        }
        return true;
    } catch (ClientProtocolException e) {
        System.err.println("Error in http X request: " + e.getMessage());
        e.printStackTrace();
    } catch (IOException e) {
        System.err.println("Error in http request: " + e.getMessage());
        e.printStackTrace();
    }
    return false;
  }
}
