package com.jjs.demerits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.jjs.demerits.client.DemeritClient;
import com.jjs.demerits.shared.DemeritUtils;
import com.jjs.demerits.shared.DemeritsProto;
import com.jjs.demerits.shared.DemeritsProto.Note;
import com.jjs.demerits.shared.DemeritsProto.NoteList;

public class NotesList extends Activity {
  public static final String PREF_NAME = "DemeritsPref";
  private static final String GCM_SENDER_ID = "691074005527";
  private static final String RECENT = "Most Recent";  
  private static final String OLDEST = "Oldest First";

  private String filter;
  private DemeritClient client;
  private LoginScreen login;
  private MenuItem composeButton;
  private boolean paused = false;
  private boolean loggedIn = false;
  private NoteList notes;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GCMRegistrar.checkDevice(this);
    GCMRegistrar.checkManifest(this);
    String regId = GCMRegistrar.getRegistrationId(this);
    filter = RECENT;
    if (regId.equals("")) {
      GCMRegistrar.register(this, GCM_SENDER_ID);
    } else {
      System.err.println("Already registered: " + regId);
    }
    client = new DemeritClient(this);
    login = new LoginScreen(this, client);
    login.init(new LoginScreen.Callback() {
      @Override
      public void gotCredentials() {
        updateNoteList(false);
        new Thread(new Runnable() {
          @Override
          public void run() {
            String regId = GCMRegistrar.getRegistrationId(NotesList.this);
            if (regId.isEmpty()) {
              System.err.println("Not registered yet!");
            } else {
              client.updateGcmId(regId, true);
            }
          }          
        }).start();
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

  @Override
  @Deprecated
  protected Dialog onCreateDialog(int id, Bundle bundle) {
    if (id == LoginScreen.ACCOUNT_DIALOG) {
      return login.createAccountDialog();
    }
    return null;
  }
  
  private void setupMessageList() {
    final ListView listView = (ListView) findViewById(R.id.noteList);
    ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
    updateList();
  }

  private void updateList() {
    final ListView listView = (ListView) findViewById(R.id.noteList);
    List<DemeritsProto.Note> noteList = new ArrayList<DemeritsProto.Note>();
    final boolean recentFirst = !filter.equals(OLDEST);
    noteList.addAll(notes.getFromUserList());
    noteList.addAll(notes.getToUserList());
    if (!filter.equals(OLDEST) && !filter.equals(RECENT)) {
      System.err.println("Filtering: " + filter);
      List<DemeritsProto.Note> filteredList = new ArrayList<DemeritsProto.Note>();
      for (Note note : noteList) {
        if (note.getFrom().startsWith(filter) ||
            note.getTo().startsWith(filter)) {
          filteredList.add(note);
        }
      }
      noteList = filteredList;
    }
    
    Collections.sort(noteList, new Comparator<DemeritsProto.Note>() {
      @Override
      public int compare(DemeritsProto.Note n1,
              DemeritsProto.Note n2) {
        int val = (int) (n2.getDate() - n1.getDate());
        return recentFirst ? val : -val;
      }
    });
    NoteListAdapter adapter = new NoteListAdapter(NotesList.this, noteList, client.getEmail());
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
    if (!client.isNetworkAvailable()) {
      Toast.makeText(this, "Sorry, network not available",Toast.LENGTH_SHORT).show();
      return;
    }
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (delay) {
          try {
            Thread.sleep(2000);
          } catch (InterruptedException e) {
          }
        }
        notes = client.getNotes();
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
              Button filterButton = (Button) findViewById(R.id.list_filter);
              filterButton.setText("Filter By: " + filter);
              filterButton.setOnClickListener(new OnClickListener() {            
                @Override
                public void onClick(View v) {
                  AlertDialog.Builder builder = new AlertDialog.Builder(NotesList.this);
                  final List<String> filterList = getFilterList(notes);
                  builder.setItems(filterList.toArray(new String[filterList.size()]), 
                      new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                      updateFilter(filterList.get(item));
                      dialog.cancel();
                    }
                  });
                  builder.create().show();
                }
              });
              setupMessageList();
            }
          }
        });
      }					
    }).start();
  }

  private void updateFilter(String filter) {
    if (this.filter.equals(filter)) {
      return;
    }
    this.filter = filter;
    Button filterButton = (Button) findViewById(R.id.list_filter);
    filterButton.setText("Filter By: " + filter);
    setupMessageList();
  }                    

  private List<String> getFilterList(NoteList notes) {
    SortedSet<String> emails = new TreeSet<String>();
    List<String> list = new ArrayList<String>();
    list.add(RECENT);
    list.add(OLDEST);
    for (Note note : notes.getToUserList()) {
      emails.add(DemeritUtils.getShortEmail(note.getFrom()));
    }
    for (Note note : notes.getFromUserList()) {
      emails.add(DemeritUtils.getShortEmail(note.getTo()));
    }
    list.addAll(emails);
    return list;
  }
}
