package com.example.urls;

public class Entity {
	
	public String HEROKUDOMAIN = "https://java-ig-comment-dana.herokuapp.com/";
	public String CALLBACKURL = HEROKUDOMAIN + "instagram/callback";
	public String FB_API_DOMAIN = "https://graph.facebook.com/v3.3";
	public String GET_ACC_ID_API = FB_API_DOMAIN + "/me/accounts?fields=connected_instagram_account,name&access_token=";

	public int defaultInterval = 2;
	public int MAXWAIT = 10;

	/*
	 * public String APP_ID = ""; public String APP_SECRET = "";
	 */

	public String getMediaUrl(String account_id, String token) {
		return FB_API_DOMAIN + "/" + account_id
				+ "/media?fields=comments.limit(99){username,text,timestamp,replies.limit(99){username,text,timestamp}}"
				+ ",media_url,caption,comments_count,timestamp,owner{username}&limit=25&access_token=" + token;
	}
	
	public String getTokenExpDateApi (String app_id, String token) {
		return FB_API_DOMAIN + "/oauth/access_token?grant_type=fb_attenuate_token&client_id=" + app_id + "&fb_exchange_token=" + token;
	}

	public String getAccTokenApi(String app_id, String app_secret) {
		return FB_API_DOMAIN + "/oauth/access_token?client_id=" + app_id + "&redirect_uri=" + CALLBACKURL
				+ "&client_secret=" + app_secret + "&code=";
	}

	public String replyComment(String comment_id, String message, String token) {
		return FB_API_DOMAIN + "/" + comment_id + "/replies?message=" + message + "&access_token=" + token;
	}

	public String createComment(String post_id, String message, String token) {
		return FB_API_DOMAIN + "/" + post_id + "/comments?message=" + message + "&access_token=" + token;
	}
	
}