package com.example.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cif_dataentry")
public class DataEntry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2525033423449584485L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "cif_account_id")
	private String cifAccountId;

	@Column(name = "cif_post_id")
	private String cifPostId;

	@Column(name = "cif_json_data", columnDefinition = "TEXT")
	private String cifJsonData;

	public long getId() {
		return id;
	}

	public String getCifAccountId() {
		return cifAccountId;
	}

	public String getCifPostId() {
		return cifPostId;
	}

	public String getCifJsonData() {
		return cifJsonData;
	}

	protected DataEntry() {

	}

	public DataEntry(long id, String cifAccountId, String cifPostId, String cifJsonData) {
		if (id != 0) {
			this.id = id;
		}
		this.cifPostId = cifPostId;
		this.cifAccountId = cifAccountId;
		this.cifJsonData = cifJsonData;
	}
}
