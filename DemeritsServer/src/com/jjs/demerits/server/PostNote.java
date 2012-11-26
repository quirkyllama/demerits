package com.jjs.demerits.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.jjs.demerits.shared.Base64;
import com.jjs.demerits.shared.DemeritsProto;
import com.jjs.demerits.shared.GcmRegistration;
import com.jjs.demerits.shared.Note;

public class PostNote extends HttpServlet {
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    DemeritsProto.Note note = 
        DemeritsProto.Note.parseFrom(req.getInputStream());
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Note newNote = new Note();
      newNote.setFrom(note.getFrom());
      newNote.setTo(note.getTo());
      newNote.setText(note.getText());
      newNote.setDate(note.getDate());
      newNote.setDemerit(note.getDemerit());
      pm.makePersistent(newNote);

      try {
        GcmRegistration gcm = 
            pm.getObjectById(GcmRegistration.class, note.getTo());
        Sender sender = new Sender(UpdateGcmId.API_KEY);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        note.writeTo(baos);
        String encodedNote = Base64.encodeBytes(baos.toByteArray());
        
        Message message = new Message.Builder()
            .addData("demerit.note", encodedNote)
            .build();
        Result result = sender.send(message, gcm.getRegistrationId(), 5);
        System.err.println("Sent reminder: " + 
            result.getErrorCodeName() + " to: " + 
            gcm.getRegistrationId() + " " + note.getTo() +
            " gcm: " + encodedNote);
      } catch (JDOObjectNotFoundException e) {
        System.err.println("Not GCM for: " + note.getTo());
      }
    }
    finally {
      pm.close();
    }

    PrintWriter output = res.getWriter();
    output.write("Saved your demerit: " + note.getText());
  }
}
