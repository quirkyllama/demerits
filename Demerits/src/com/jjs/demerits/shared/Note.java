package com.jjs.demerits.shared;

import java.io.Serializable;

public class Note implements Serializable {
	private static final long serialVersionUID = 91819760001L;

	private Long key;

	private String from;

	private String to;

	private String text;

	private long date;
	
	private boolean demerit;
	
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Long getKey() {
		return key;
	}

	public boolean isDemerit() {
		return demerit;
	}

	public void setDemerit(boolean demerit) {
		this.demerit = demerit;
	}
}
