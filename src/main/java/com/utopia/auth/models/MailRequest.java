package com.utopia.auth.models;

public class MailRequest {

	private String name;
	private String to;
	private String from;
	private String subject;
	
	public MailRequest(String to) {
		this.name = "Utopia Airlines";
		this.to = to;
		this.from = "utopia.ss.airlines@gmail.com";
		this.subject = "Password Recovery";
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	

}

