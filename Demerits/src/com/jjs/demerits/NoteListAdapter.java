package com.jjs.demerits;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.jjs.demerits.shared.DemeritsProto;
import com.jjs.demerits.shared.DemeritsProto.NoteList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NoteListAdapter extends ArrayAdapter<DemeritsProto.Note> {
	private final List<DemeritsProto.Note> notes;
	private final Context context;
	private final String email;
	private static final DateFormat dateFormat = SimpleDateFormat.getDateInstance();
	
	public NoteListAdapter(Context context,
			List<DemeritsProto.Note> notes, String email) {
		super(context, R.layout.message_in_list, notes);
		this.context = context;
		this.notes = notes;
		this.email = email;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			row = inflater.inflate(R.layout.message_in_list, parent, false);
		}
		
		TextView from = (TextView) row.findViewById(R.id.note_to_from);
		DemeritsProto.Note note = notes.get(position);
		if (email.equals(note.getFrom())) {
			from.setText("TO: " + note.getTo());
		} else {
			from.setText("FROM: " + note.getFrom());			
		}
		TextView text = (TextView) row.findViewById(R.id.note_text);
		text.setText(note.getText());
		text.setTextColor(parent.getResources().getColor(
		    note.getDemerit() ? R.color.DemeritColor : R.color.KudoColor));
		final TextView date = (TextView) row.findViewById(R.id.note_date);
		String dateString;
		long agoMinutes = 
				(System.currentTimeMillis() - note.getDate()) / (60 * 1000);
		if (agoMinutes < 90) {
			if (agoMinutes == 0) {
		      agoMinutes = 1;
			}
			if (agoMinutes < 3 && position == 0) {
			  AlphaAnimation animation = new AlphaAnimation(1, 0.25f);
			  animation.setRepeatCount(3);
			  animation.setDuration(900);
			  animation.setRepeatMode(AlphaAnimation.REVERSE);
			  row.startAnimation(animation);
			}
			dateString = String.format("%d minute%s ago", agoMinutes, agoMinutes > 1 ? "s" : "");
		} else if (agoMinutes / 60 < 16) {
			long hours = agoMinutes / 60;
			dateString = String.format("%d hour%s ago", hours, hours > 1 ? "s" : "");
		} else {
			long daysAgo = agoMinutes / (60 * 24);
			if (daysAgo == 0) {
				daysAgo = 1;
			}
			dateString = String.format("%d day%s ago", daysAgo, daysAgo > 1 ? "s" : "");
		}
		
		date.setText(dateString);

		return row;
	}
}
