package com.example.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cif_interval")
public class Interval implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2517903124293152004L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "cif_account_id")
	private String cifAccountId;

	@Column(name = "cif_interval")
	private int cifInterval;

	public long getId() {
		return id;
	}

	public String getCifAccountId() {
		return cifAccountId;
	}

	public int getCifInterval() {
		return cifInterval;
	}

	protected Interval() {

	}

	public Interval(long id, String cifAccountId, int cifInterval) {
		if (id != 0) {
			this.id = id;
		}
		this.cifAccountId = cifAccountId;
		this.cifInterval = cifInterval;
	}
}
