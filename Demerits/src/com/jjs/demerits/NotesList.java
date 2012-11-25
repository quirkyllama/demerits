package com.jjs.demerits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

import com.jjs.demerits.client.DemeritClient;
import com.jjs.demerits.shared.DemeritsProto;
import com.jjs.demerits.shared.DemeritsProto.NoteList;

public class NotesList extends Activity {
  public static final String PREF_NAME = "DemeritsPref";
  
  private DemeritClient client;
  private LoginScreen login;
  private MenuItem composeButton;
  private boolean paused = false;
  private boolean loggedIn = false;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    client = new DemeritClient(this);
    login = new LoginScreen(this, client);
    login.init(new LoginScreen.Callback() {
      @Override
      public void gotCredentials() {
        updateNoteList(false);
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.activity_note_list, menu);
    composeButton = menu.findItem(R.id.menu_compose);
    composeButton.setOnMenuItemClickListener(
            new OnMenuItemClickListener() {					
              @Override
              public boolean onMenuItemClick(MenuItem item) {
                System.err.println("Got click");
                Intent intent = new Intent(NotesList.this, ComposeActivity.class);
                startActivity(intent);      
                return true;
              }
            });
    composeButton.setEnabled(loggedIn);
    return true;
  }

  private void setupMessageList(NoteList notes) {
    final ListView listView = (ListView) findViewById(R.id.noteList);
    ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
    updateList(notes);
  }

  private void updateList(final NoteList noteList) {
    //System.err.println("Updating list: " + noteList.toString());
    final ListView listView = (ListView) findViewById(R.id.noteList);
    List<DemeritsProto.Note> notes = new ArrayList<DemeritsProto.Note>();
    notes.addAll(noteList.getFromUserList());
    notes.addAll(noteList.getToUserList());
    Collections.sort(notes, new Comparator<DemeritsProto.Note>() {
      @Override
      public int compare(DemeritsProto.Note n1,
              DemeritsProto.Note n2) {
        return (int) (n2.getDate() - n1.getDate());
      }
    });
    NoteListAdapter adapter = new NoteListAdapter(NotesList.this, notes, client.getEmail());
    listView.setAdapter(adapter);
  }

  @Override
  protected void onPause() {
    super.onPause();
    login.onPause();
    paused = true;
  }
  
  @Override
  protected void onResume() {
    super.onResume();
    if (paused) {
      paused = false;
      updateNoteList(true);      
    }
  }

  private void updateNoteList(final boolean delay) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (delay) {
          try {
            Thread.sleep(2000);
          } catch (InterruptedException e) {
          }
        }
        final NoteList notes = client.getNotes();
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            if (notes == null) {
              login.loginFailed();
            } else {
              loggedIn = true;
              if (composeButton != null) {
                composeButton.setEnabled(true);
              }
              setContentView(R.layout.note_list);
              setupMessageList(notes);
            }
          }
        });
      }					
    }).start();
  }
}
