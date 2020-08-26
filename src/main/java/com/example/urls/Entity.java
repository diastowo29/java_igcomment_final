package com.example.urls;

public class Entity {

	public String HEROKUDOMAIN = "https://java-ig-comment-watsons.herokuapp.com/";
	public String CALLBACKURL = HEROKUDOMAIN + "instagram/callback";
	public String FB_API_DOMAIN = "https://graph.facebook.com/v3.3";
	public String GET_ACC_ID_API = FB_API_DOMAIN + "/me/accounts?fields=connected_instagram_account,name&access_token=";

	public int defaultInterval = 2;
	public int MAXWAIT = 10;

	/*
	 * public String APP_ID = ""; public String APP_SECRET = "";
	 */

	public String getMediaUrl(String accId, String token) {
		return FB_API_DOMAIN + "/" + accId
				+ "/media?fields=comments.limit(99){username,text,timestamp,replies.limit(99){username,text,timestamp}}"
				+ ",media_url,caption,comments_count,timestamp,owner{username}&limit=25&access_token=" + token;
	}

	public String getAccTokenApi(String clientId, String clientSecret) {
		return FB_API_DOMAIN + "/oauth/access_token?client_id=" + clientId + "&redirect_uri=" + CALLBACKURL
				+ "&client_secret=" + clientSecret + "&code=";
	}

	public String replyComment(String commentId, String message, String token) {
		return FB_API_DOMAIN + "/" + commentId + "/replies?message=" + message + "&access_token=" + token;
	}

	public String createComment(String mediaId, String message, String token) {
		return FB_API_DOMAIN + "/" + mediaId + "/comments?message=" + message + "&access_token=" + token;
	}

}
