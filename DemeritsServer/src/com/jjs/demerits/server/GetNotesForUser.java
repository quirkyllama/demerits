package com.jjs.demerits.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
			List<Note> allNotes = new ArrayList<Note>();
			allNotes.addAll(pm.detachCopyAll(notesFromUser));
			allNotes.addAll(pm.detachCopyAll(notesToUser));
			
			System.err.println("Demerit Notes from: " + user + ": " + allNotes.size());
			ServletOutputStream output = res.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(output);
			oos.writeObject(allNotes);
			oos.close();
		}
		finally {
			pm.close();
		}
	}
}
