package com.jjs.demerits;

import java.io.IOException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gcm.GCMBaseIntentService;
import com.jjs.demerits.shared.Base64;
import com.jjs.demerits.shared.DemeritUtils;
import com.jjs.demerits.shared.DemeritsProto;

public class GCMIntentService extends GCMBaseIntentService {
  private static final String DEMERIT_NOTE_IN_BUNDLE = "demerit.note";
  
  public GCMIntentService() {
    super();
    System.err.println("GCMIntentService Created!");
  }

  public GCMIntentService(String... senderIds) {
    super(senderIds);
  }

  @Override
  protected void onError(Context context, String error) {
  }

  @Override
  protected void onMessage(Context context, Intent intent) {
    Bundle extras = intent.getExtras();
    String encodedNote = extras.getString(DEMERIT_NOTE_IN_BUNDLE);
    System.err.println("Got Message from GCM: " + 
        encodedNote);    
    byte[] rawNote;
    try {
      rawNote = Base64.decode(encodedNote);
      DemeritsProto.Note note = 
          DemeritsProto.Note.parseFrom(rawNote);
      String shortEmail = DemeritUtils.getShortEmail(note.getFrom());
      String title = note.getDemerit() ?
          "You've got Demerits!" :
            shortEmail + " sent you a Kudo!";
      String content = note.getDemerit() ?
          String.format("From: %s\n '%s'", shortEmail, note.getText()) : note.getText();
      Intent listIntent = new Intent(this, NotesList.class);
      listIntent.putExtra(DEMERIT_NOTE_IN_BUNDLE, encodedNote);
      PendingIntent pendingIntent = 
          PendingIntent.getActivity(this, 0, listIntent, PendingIntent.FLAG_UPDATE_CURRENT);
      Notification notification = new NotificationCompat.Builder(this)
        .setSmallIcon(note.getDemerit() ?
            R.drawable.new_demerit_notification :
              R.drawable.new_kudo_notification)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setTicker(title)
        .setContentTitle(title)
        .setContentText(content).build();
      NotificationManager mNotificationManager =
          (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      // mId allows you to update the notification later on.
      mNotificationManager.notify(note.getFrom().hashCode(), notification);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  protected void onRegistered(Context arg0, String register) {
    System.err.println("GCM Registered!");
  }

  @Override
  protected void onUnregistered(Context arg0, String arg1) {
    System.err.println("GCM Unregistered!");

  }

}
