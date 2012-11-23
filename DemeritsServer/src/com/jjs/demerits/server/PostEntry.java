package com.jjs.demerits.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jjs.demerits.shared.Note;

public class PostEntry extends HttpServlet {
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String demerit = req.getParameter("text");
		String from = req.getParameter("from");
		String to = req.getParameter("to");
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Note tester = new Note();
			tester.setFrom(from);
			tester.setTo(to);
			tester.setText(demerit);
			tester.setDate(System.currentTimeMillis());
			pm.makePersistent(tester);
		}
		finally {
			pm.close();
		}
		
		System.err.println("Demerit from client: " + demerit);
		PrintWriter output = res.getWriter();
		output.write("Saved your demerit: " + demerit);
	}
}
