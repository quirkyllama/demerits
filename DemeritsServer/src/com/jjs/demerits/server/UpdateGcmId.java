package com.jjs.demerits.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;

import javax.jdo.JDONullIdentityException;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.jjs.demerits.shared.DemeritsProto;
import com.jjs.demerits.shared.GcmRegistration;
import com.jjs.demerits.shared.Note;

public class UpdateGcmId extends HttpServlet {
	public static final String API_KEY = "AIzaSyA8pJoPVU8b-wQNYAnwzrNOFrDlDbpZTLk";

  @Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		DemeritsProto.UpdateGcmId update = 
				DemeritsProto.UpdateGcmId.parseFrom(req.getInputStream());
		System.err.println("Update: " + update);
//		Sender sender = new Sender(API_KEY);
//		Message message = new Message.Builder().build();
//		Result result = sender.send(message, update.getGcmId(), 5);

		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
		  System.err.println("GCM: " + update.getEmail());
		  try {
		    GcmRegistration gcm = pm.getObjectById(GcmRegistration.class, update.getEmail());
            gcm.setRegistrationDate(System.currentTimeMillis());
            gcm.setRegistrationId(update.getGcmId());	
		  } catch (JDOObjectNotFoundException e) {
		    System.err.println("New GCM for: " + update.getEmail());
		    GcmRegistration gcm = new GcmRegistration();
	        gcm.setEmail(update.getEmail());
	        gcm.setRegistrationDate(System.currentTimeMillis());
	        gcm.setRegistrationId(update.getGcmId());
	        pm.makePersistent(gcm);
		  }
		}
		finally {
		  pm.close();
		}

		PrintWriter output = res.getWriter();
		//output.write("Update GCM: " + update + "\nResult: " + result.getMessageId());
	}
}
