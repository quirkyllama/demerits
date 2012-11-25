package com.jjs.demerits;

import com.jjs.demerits.client.DemeritClient;
import com.jjs.demerits.shared.DemeritsProto.Note;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class ComposeActivity extends Activity {
  private DemeritClient client;
  private LoginScreen login;
  private MenuItem composeButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    client = new DemeritClient(this);
    login = new LoginScreen(this, client);
    login.init(new LoginScreen.Callback() {
      @Override
      public void gotCredentials() {}
    });
    setContentView(R.layout.compose_layout);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.compose_note, menu);
    composeButton = menu.findItem(R.id.menu_compose);
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
    composeButton.setEnabled(true);
    return true;
  }

  protected void sendNote() {
    final Note note = fillNote();
    new Thread(new Runnable() {
      @Override
      public void run() {
        final boolean success = client.sendNote(note);
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
           Toast.makeText(ComposeActivity.this, 
                   success ? ("Demerit to " + note.getTo() + " posted!") :
                     "Error posting Demerit", Toast.LENGTH_SHORT).show(); 
          }
        });
      }
    }).start();
    System.err.println("Finishing!");
    finish();
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
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
  }

  private Note fillNote() {
    return Note.newBuilder()
      .setDate(System.currentTimeMillis())
      .setTo(getTextForField(R.id.to_field))
      .setFrom(client.getEmail())
      .setText(getTextForField(R.id.demerit_text))
      .setDemerit(!((CheckBox) findViewById(R.id.kudo_checkbox)).isChecked())
      .build();
  }

  private String getTextForField(int id) {
    return ((EditText) findViewById(id)).getText().toString();
  }

  public ComposeActivity() {
    
  }

}
