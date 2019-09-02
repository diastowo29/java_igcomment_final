package com.example.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cif_errorlogs")
public class ErrorLogs implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8787334070059750938L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "cif_account_id")
	private String cifAccountId;

	@Column(name = "cause")
	private String cause;

	@Column(name = "stacktrace")
	private String stacktrace;

	public long getId() {
		return id;
	}

	public String getCifAccountId() {
		return cifAccountId;
	}

	public String getCause() {
		return cause;
	}

	public String getStacktrace() {
		return stacktrace;
	}
	
	public void setCifAccountId(String cifAccountId) {
		this.cifAccountId = cifAccountId;
	}
	
	public void setCause(String cause) {
		this.cause = cause;
	}
	
	public void setStacktrace(String stacktrace) {
		this.stacktrace = stacktrace;
	}

	protected ErrorLogs() {

	}

	public ErrorLogs(long id, String cifAccountId, String cause, String stacktrace) {
		if (id != 0) {
			this.id = id;
		}
		this.cifAccountId = cifAccountId;
		this.cause = cause;
		this.stacktrace = stacktrace;
	}
}
