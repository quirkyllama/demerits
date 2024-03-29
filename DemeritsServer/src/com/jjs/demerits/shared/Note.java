package com.jjs.demerits.shared;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class Note {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long key;

	@Persistent
	@Index
	private String from;

	@Persistent
	@Index
	private String to;

	@Persistent
	private String text;

	@Persistent
	private long date;
	
	@Persistent
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
