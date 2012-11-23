package com.jjs.demerits;

import java.util.List;

import com.jjs.demerits.client.DemeritClient;
import com.jjs.demerits.shared.Note;

import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

public class NotesList extends Activity {
	private DemeritClient client;
	private LoginScreen login;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = new DemeritClient(this);
        login = new LoginScreen(this, client);
        login.init(new LoginScreen.Callback() {
			@Override
			public void gotCredentials() {
				new Thread(new Runnable() {

					@Override
					public void run() {
						final List<Note> notes = client.getNotes();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (notes == null) {
									login.loginFailed();
								} else {
							        setContentView(R.layout.activity_write_demerit);
									setupMessageList(notes);
								}
							}
						});
					}					
				}).start();
			}
		});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_write_demerit, menu);
        return true;
    }

	private void setupMessageList(List<Note> notes) {
		final ListView listView = (ListView) findViewById(R.id.noteList);
//        ProgressBar progressBar = new ProgressBar(this);
//        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT));
//        progressBar.setIndeterminate(true);
//        listView.setEmptyView(progressBar);
        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
 //       root.addView(progressBar);
        updateList(notes);
	}
    
	private void updateList(final List<Note> notes) {
		System.err.println("Updating list: " + notes.size());
		final ListView listView = (ListView) findViewById(R.id.noteList);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(NotesList.this, R.layout.message_list_view);
		for (Note note : notes) {
			System.err.println("Adding list note: " + note.getText());
			adapter.add(note.getText() + " to: " + note.getTo());
		}
		listView.setAdapter(adapter);
	}
}
