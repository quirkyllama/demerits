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

import com.jjs.demerits.shared.DemeritsProto;
import com.jjs.demerits.shared.GcmRegistration;

public class UpdateGcmId extends HttpServlet {
	public static final String API_KEY = "AIzaSyA8pJoPVU8b-wQNYAnwzrNOFrDlDbpZTLk";

  @Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		DemeritsProto.UpdateGcmId update = 
				DemeritsProto.UpdateGcmId.parseFrom(req.getInputStream());
		System.err.println("Update: " + update);

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
