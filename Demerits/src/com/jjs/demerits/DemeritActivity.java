package com.jjs.demerits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.jjs.demerits.client.DemeritClient;
import com.jjs.demerits.shared.DemeritUtils;
import com.jjs.demerits.shared.DemeritsProto;
import com.jjs.demerits.shared.DemeritsProto.Note;
import com.jjs.demerits.shared.DemeritsProto.NoteList;

public class DemeritActivity extends Activity {
  public static final String PREF_NAME = "DemeritsPref";
  private static final String GCM_SENDER_ID = "691074005527";
  private static final String RECENT = "Most Recent";  
  private static final String OLDEST = "Oldest First";
  private static final String SAVED_MODE = "SavedMode";
  private static final String LAST_FILTER = "LastFilter";

  private String filter;
  private DemeritClient client;
  private LoginScreen login;
  private MenuItem composeButton;
  private boolean paused = false;
  private boolean loggedIn = false;
  private NoteList notes;
  private DisplayMode mode = null;
  private enum DisplayMode {
    LIST, COMPOSE
  }
  private ComposeScreen composeScreen;
  
  public DemeritActivity() {
    super();
  }
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    System.err.println("OnCreate");
    SharedPreferences preferences = 
        getBaseContext().getSharedPreferences(
            DemeritActivity.PREF_NAME, Activity.MODE_PRIVATE);
    String modeString =
        preferences.getString(SAVED_MODE, DisplayMode.LIST.toString());
    filter = preferences.getString(LAST_FILTER, RECENT);
    final DisplayMode nextMode = DisplayMode.valueOf(modeString);

    GCMRegistrar.checkDevice(this);
    GCMRegistrar.checkManifest(this);
    String regId = GCMRegistrar.getRegistrationId(this);
    if (regId.equals("")) {
      GCMRegistrar.register(this, GCM_SENDER_ID);
    } else {
      System.err.println("Already registered: " + regId);
    }
    client = new DemeritClient(this);
    login = new LoginScreen(this, client);
    composeScreen = new ComposeScreen(this, client, login);
    login.init(new LoginScreen.Callback() {
      @Override
      public void gotCredentials() {
        if (nextMode == DisplayMode.LIST) {
          switchToNoteList(false);
        } else {
          goToComposeScreen();
        }
        new Thread(new Runnable() {
          @Override
          public void run() {
            String regId = GCMRegistrar.getRegistrationId(DemeritActivity.this);
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
  public void onBackPressed() {
    if (mode == DisplayMode.COMPOSE) {
      switchToNoteList(false);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    System.err.println("OnPrepareOptionMenus!!!!!");
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.activity_note_list, menu);
    composeButton = menu.findItem(R.id.menu_compose);
    composeButton.setOnMenuItemClickListener(
        new OnMenuItemClickListener() {					
          @Override
          public boolean onMenuItemClick(MenuItem item) {
            System.err.println("Got click");
            goToComposeScreen();
            return true;
          }
        });
    
    boolean enabled = mode == DisplayMode.LIST;
    System.err.println("Enabled: " + enabled + " " + mode);
    composeButton.setVisible(enabled);
    composeButton.setEnabled(enabled);
    composeScreen.onCreateOptionsMenu(menu, mode == DisplayMode.COMPOSE);
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
    NoteListAdapter adapter = new NoteListAdapter(DemeritActivity.this, noteList, client.getEmail());
    listView.setAdapter(adapter);
  }

  @Override
  protected void onPause() {
    super.onPause();
    System.err.println("OnPause");
    login.onPause();
    composeScreen.onPause();
    
    SharedPreferences.Editor preferences = 
        getBaseContext().getSharedPreferences(
            DemeritActivity.PREF_NAME, Activity.MODE_PRIVATE).edit();
    preferences.putString(SAVED_MODE,  mode.toString());    
    preferences.commit();
    paused = true;
  }
  
  @Override
  protected void onResume() {
    super.onResume();
    System.err.println("OnResume");
    if (paused) {
      paused = false;
    }
  }

  public void switchToNoteList(final boolean delay) {
    if (mode != DisplayMode.LIST) {
      mode = DisplayMode.LIST;
      setContentView(R.layout.note_list);
      invalidateOptionsMenu();
      Button filterButton = (Button) findViewById(R.id.list_filter);
      filterButton.setText("Filter By: " + filter);
      filterButton.setOnClickListener(new OnClickListener() {            
        @Override
        public void onClick(View v) {
          AlertDialog.Builder builder = new AlertDialog.Builder(DemeritActivity.this);
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
    }
    
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
//              setContentView(R.layout.note_list);
              updateList();
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
    updateList();
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

  private void goToComposeScreen() {
    composeScreen.show();
    mode = DisplayMode.COMPOSE;
    invalidateOptionsMenu();
  }
}
