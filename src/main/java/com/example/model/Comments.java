package com.example.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cif_comments")
public class Comments implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7157178489574254967L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "igid")
	private String igid;

	@Column(name = "cif_comment_id")
	private String cif_comment_id;

	@Column(name = "cif_comment_text")
	private String cif_comment_text;

	@Column(name = "cif_comment_user")
	private String cif_comment_user;

	@Column(name = "cif_media_id")
	private String cif_media_id;

	@Column(name = "cif_media_caption")
	private String cif_media_caption;

	@Column(name = "cif_media_url")
	private String cif_media_url;

	public long getId() {
		return id;
	}

	public String getCif_ig_id() {
		return igid;
	}

	public String getCif_comment_id() {
		return cif_comment_id;
	}

	public String getCif_comment_text() {
		return cif_comment_text;
	}

	public String getCif_comment_user() {
		return cif_comment_user;
	}

	public String getCif_media_id() {
		return cif_media_id;
	}

	public String getCif_media_caption() {
		return cif_media_caption;
	}

	public String getCif_media_url() {
		return cif_media_url;
	}

	protected Comments() {
	}

	public Comments(long id, String cif_ig_id, String cif_comment_id, String cif_comment_text, String cif_comment_user,
			String cif_media_id, String cif_media_caption, String cif_media_url) {
		if (id != 0) {
			this.id = id;
		}
		this.igid = cif_ig_id;
		this.cif_comment_id = cif_comment_id;
		this.cif_comment_text = cif_comment_text;
		this.cif_comment_user = cif_comment_user;
		this.cif_media_id = cif_media_id;
		this.cif_media_caption = cif_media_caption;
		this.cif_media_url = cif_media_url;
	}
}