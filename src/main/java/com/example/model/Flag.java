package com.example.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.example.others.FlagStatus;

@Entity
@Table(name = "cif_flag")
public class Flag implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8787334070059750938L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "cif_account_id")
	private String cifAccountId;

	@Column(name = "cif_status")
	private String cifStatus;

	@Column(name = "cif_interval")
	private int cifInterval;

	@Column(name = "cif_daylimit")
	private int cifDayLimit;

	@Column(name = "cif_waitcounter")
	private int cifWaitCounter;
	
	@Column(name = "cif_mailcounter")
	private int cifMailCounter;

	public long getId() {
		return id;
	}

	public String getCifAccountId() {
		return cifAccountId;
	}

	public String getCifStatus() {
		return cifStatus;
	}

	public int getCifInterval() {
		return cifInterval;
	}

	public int getCifDayLimit() {
		return cifDayLimit;
	}

	public int getCifWaitCounter() {
		return cifWaitCounter;
	}
	
	public int getCifMailCounter() {
		return cifMailCounter;
	}

	public void setCifAccountId(String cifAccountId) {
		this.cifAccountId = cifAccountId;
	}

	public void setCifStatus(FlagStatus cifStatus) {
		this.cifStatus = cifStatus.toString();
	}

	public void setCifInterval(int cifInterval) {
		this.cifInterval = cifInterval;
	}

	public void setCifDayLimit(int cifDayLimit) {
		this.cifDayLimit = cifDayLimit;
	}

	public void setCifWaitCounter(int cifWaitCounter) {
		this.cifWaitCounter = cifWaitCounter;
	}
	
	public void setCifMailCounter(int cifMailCounter) {
		this.cifMailCounter = cifMailCounter;
	}

	protected Flag() {

	}

	public Flag(long id, String cifAccountId, FlagStatus cifStatus, int cifInterval, int cifDayLimit,
			int cifWaitCounter, int cifMailCounter) {
		if (id != 0) {
			this.id = id;
		}
		this.cifAccountId = cifAccountId;
		this.cifStatus = cifStatus.toString();
		this.cifInterval = cifInterval;
		this.cifDayLimit = cifDayLimit;
		this.cifWaitCounter = cifWaitCounter;
		this.cifMailCounter = cifMailCounter;
	}
}
