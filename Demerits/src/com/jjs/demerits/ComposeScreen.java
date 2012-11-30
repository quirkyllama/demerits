package com.jjs.demerits;

import java.util.ArrayList;
import java.util.List;

import com.jjs.demerits.client.DemeritClient;
import com.jjs.demerits.shared.DemeritsProto.Note;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class ComposeScreen {
  private static final String LAST_TO_EMAIL = "last_to_email";
  
  private final DemeritClient client;
  private final LoginScreen login;
  private final DemeritActivity context;
  private MenuItem composeButton;

  public ComposeScreen(DemeritActivity context, DemeritClient client, LoginScreen login) {
    this.context = context;
    this.login = login;
    this.client = client;
  }

  public void show() {
    context.setContentView(R.layout.compose_layout);
    ((Button) context.findViewById(R.id.send_new_button)).setOnClickListener(
        new OnClickListener() {          
          @Override
          public void onClick(View v) {
            if (verifyNote()) {
              sendNote();
            }
          }
        });
    setupToField();
  }
   
  private void setupToField() {
    String[] emailAddresses = getAllEmails();
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
            android.R.layout.simple_dropdown_item_1line, emailAddresses);
    AutoCompleteTextView textView =
        (AutoCompleteTextView) context.findViewById(R.id.to_field);
    textView.setAdapter(adapter);
    String toEmail = getLastEmailFromPreferences();
    System.err.println("Email from saved bundle: " + toEmail);
    if (toEmail != null && !toEmail.isEmpty()) {
      textView.setText(toEmail);
    }
  }

  private String[] getAllEmails() {
    List<String> emailAddressCollection = new ArrayList<String>();
    ContentResolver cr = context.getContentResolver();
    Cursor emailCur = 
        cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, null, null, null);

    while (emailCur.moveToNext()) {
      String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
      emailAddressCollection.add(email);
    }
    emailCur.close();

    String[] emailAddresses = new String[emailAddressCollection.size()];
    emailAddressCollection.toArray(emailAddresses);
    return emailAddresses;
  }

  private String getLastEmailFromPreferences() {
    SharedPreferences preferences = 
        context.getBaseContext().getSharedPreferences(
            DemeritActivity.PREF_NAME, Activity.MODE_PRIVATE);
    return preferences.getString(LAST_TO_EMAIL, null);
  }

  public boolean onCreateOptionsMenu(Menu menu, boolean enabled) {
    composeButton = menu.findItem(R.id.menu_send);
    composeButton.setOnMenuItemClickListener(
        new OnMenuItemClickListener() {                 
          @Override
          public boolean onMenuItemClick(MenuItem item) {
            if (verifyNote()) {
              sendNote();
            }
            return true;
          }
        });
    System.err.println("Setting enabled: "+ enabled);
    composeButton.setVisible(enabled);
    composeButton.setEnabled(enabled);
    return true;
  }

  protected void sendNote() {
    final Note note = fillNote();
    new Thread(new Runnable() {
      @Override
      public void run() {
        SharedPreferences.Editor preferences = 
            context.getBaseContext().getSharedPreferences(
                DemeritActivity.PREF_NAME, Activity.MODE_PRIVATE).edit();
        preferences.putString(LAST_TO_EMAIL,  note.getTo());

        System.err.println("Save last email: " + note.getTo());
        preferences.commit();

        final boolean success = client.sendNote(note);
        context.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            String demeritText = note.getDemerit() ?
                "Demerit" : "Kudo";
            Toast.makeText(context, 
                success ? (demeritText + " to " + note.getTo() + " posted!") :
                  "Error posting " + demeritText, Toast.LENGTH_SHORT).show(); 
          }
        });
      }
    }).start();
    System.err.println("Finishing!");
    context.switchToNoteList(true);
  }

  protected boolean verifyNote() {
    Note note = fillNote();
    if (!note.hasTo()) {
      reportError("To field must be set");
      return false;
    } 
    if (!note.hasText() || note.getText().isEmpty()) {
      reportError("Message must not be empty");
      return false;
    }
    return true;
  }

  private void reportError(String string) {
    Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
  }

  private Note fillNote() {
    return Note.newBuilder()
        .setDate(System.currentTimeMillis())
        .setTo(getTextForField(R.id.to_field).trim())
        .setFrom(client.getEmail())
        .setText(getTextForField(R.id.demerit_text).trim())
        .setDemerit(!((CheckBox) context.findViewById(R.id.kudo_checkbox)).isChecked())
        .build();
  }

  private String getTextForField(int id) {
    return ((EditText) context.findViewById(id)).getText().toString();
  }

  public void onPause() {
  }
}
