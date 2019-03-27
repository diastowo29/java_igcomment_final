package com.example.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cif_clients")
public class Client implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 537765001659643807L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "cif_client_id")
	private String cifClientId;

	@Column(name = "cof_client_secret")
	private String cifClientSecret;

	@Column(name = "cif_o_post_o_ticket")
	private String cifPostToTicket;

	public long getId() {
		return id;
	}

	public String getClientId() {
		return cifClientId;
	}

	public String getClientSecret() {
		return cifClientSecret;
	}

	public String getPostToTicket() {
		return cifPostToTicket;
	}

	public Client(long id, String cifClientId, String cifClientSecret, String cifPostToTicket) {
		if (id != 0) {
			this.id = id;
		}
		this.cifClientId = cifClientId;
		this.cifClientSecret = cifClientSecret;
		this.cifPostToTicket = cifPostToTicket;
	}
}
