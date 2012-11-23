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

public class WriteDemerit extends Activity {
	private DemeritClient client;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_demerit);
        client = new DemeritClient(this, "jjs");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_write_demerit, menu);
        final ListView listView = (ListView) findViewById(R.id.noteList);
     // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        progressBar.setIndeterminate(true);
        listView.setEmptyView(progressBar);
        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);
        
        new Thread(new Runnable() {
			@Override
			public void run() {
				List<Note> notes = client.getNotes();
				for (Note note : notes) {
					System.err.println("Note: " + note.getText() + 
							" " + "from: " + note.getFrom() + " to: " + note.getTo());
				}
				updateList(notes);
			}
        	
        }).start();
        return true;
    }
    
    
    private void updateList(final List<Note> notes) {
    	System.err.println("Updating list.");
    	runOnUiThread(new Runnable() {
			@Override
			public void run() {
		        final ListView listView = (ListView) findViewById(R.id.noteList);
		        ArrayAdapter<String> adapter = new ArrayAdapter<String>(WriteDemerit.this, R.layout.message_list_view);
		        for (Note note : notes) {
		        	System.err.println("Adding list note: " + note.getText());
		        	adapter.add(note.getText() + " to: " + note.getTo());
		        }
		    	listView.setAdapter(adapter);
			}    		
    	});
    }
}
