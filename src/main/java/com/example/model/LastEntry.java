package com.example.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cif_lastentry")
public class LastEntry implements Serializable {

	private static final long serialVersionUID = 4900334629433455229L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "cif_account_id")
	private String cifAccountId;

	@Column(name = "cif_last_entry")
	private long cifLastEntry;

	public long getId() {
		return id;
	}

	public String getCifAccountId() {
		return cifAccountId;
	}

	public long getCifLastEntry() {
		return cifLastEntry;
	}

	protected LastEntry() {

	}

	public LastEntry(long id, String cifACcountId, long cifLastEntry) {
		if (id != 0) {
			this.id = id;
		}
		this.cifAccountId = cifACcountId;
		this.cifLastEntry = cifLastEntry;
	}

}
