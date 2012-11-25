package com.jjs.demerits.server;

import java.io.IOException;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jjs.demerits.shared.DemeritsProto;
import com.jjs.demerits.shared.DemeritsProto.NoteList.Builder;
import com.jjs.demerits.shared.Note;

public class GetNotesForUser extends HttpServlet {
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String user = req.getParameter("user");
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(Note.class, "from == '" + user + "'");
			q.setOrdering("date desc");
			List<Note> notesFromUser = (List<Note>) q.execute();
			
			q = pm.newQuery(Note.class, "to == '" + user + "'");
			q.setOrdering("date desc");
			List<Note> notesToUser = (List<Note>) q.execute();
			DemeritsProto.NoteList.Builder noteListBuilder = 
					DemeritsProto.NoteList.newBuilder();
			noteListBuilder.setEmail(user);
			addToList(notesFromUser, noteListBuilder, true);
			addToList(notesToUser, noteListBuilder, false);
			
			ServletOutputStream output = res.getOutputStream();
			noteListBuilder.build().writeTo(output);
			output.close();
		}
		finally {
			pm.close();
		}
	}

	private void addToList(
			List<Note> notes, Builder noteListBuilder, boolean fromUser) {
		for (Note note : notes) {
			DemeritsProto.Note.Builder builder = DemeritsProto.Note.newBuilder();
			builder.setDate(note.getDate())
				.setText(note.getText())
				.setDemerit(note.isDemerit())
				.setFrom(note.getFrom())
				.setTo(note.getTo());
			if (fromUser) {
				noteListBuilder.addFromUser(builder);
			} else {
				noteListBuilder.addToUser(builder);
			}
		}
	}
}
