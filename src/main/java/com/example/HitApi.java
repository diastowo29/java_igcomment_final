package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.model.ErrorLogs;
import com.example.repo.ErrorLogsRepository;

public class HitApi {
	public JSONObject hitAuth(String newUrl, String method, ErrorLogsRepository errorRepo, String accountId) {
		JSONObject response = new JSONObject();
		try {
			System.out.println("CALLING " + method);
			URL url = new URL(newUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(method);
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				System.out.println("HTTP ERROR:");
				System.out.println(conn.getResponseMessage());
				response = null;
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
				LocalDateTime now = LocalDateTime.now();

				errorRepo.save(new ErrorLogs(0, accountId,
						conn.getResponseMessage() + " - " + conn.getResponseCode() + " - " + dtf.format(now).toString(),
						method));

				response = new JSONObject().put("failed_status", "error")
						.put("code", String.valueOf(conn.getResponseCode())).put("message", conn.getResponseMessage());
			}
//			System.out.println("Output from Server .... \n");
			BufferedReader rd = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));
			String jsonText = readUser(rd);
			response = new JSONObject(jsonText);

			conn.disconnect();

		} catch (MalformedURLException e) {

		} catch (IOException e) {

		} catch (JSONException e) {

		}

		return response;
	}

	public JSONObject hit(String newUrl, String method, ErrorLogsRepository errorRepo, String accountId,
			ErrorLogs errLog) {
		JSONObject response = new JSONObject();
		try {
			System.out.println("CALLING " + method);
			URL url = new URL(newUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(method);
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				System.out.println("HTTP ERROR:");
				System.out.println(conn.getResponseMessage());
				response = null;
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
				LocalDateTime now = LocalDateTime.now();

				if (errLog == null) {
					errorRepo.save(new ErrorLogs(0, accountId, conn.getResponseMessage() + " - "
							+ conn.getResponseCode() + " - " + dtf.format(now).toString(), method));
				} else {
					errLog.setCause(conn.getResponseMessage() + " - " + conn.getResponseCode() + " - "
							+ dtf.format(now).toString());
					errLog.setStacktrace(method);
					errorRepo.save(errLog);
				}

				response = new JSONObject().put("failed_status", "error")
						.put("code", String.valueOf(conn.getResponseCode())).put("message", conn.getResponseMessage());
			}
//			System.out.println("Output from Server .... \n");
			BufferedReader rd = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));
			String jsonText = readUser(rd);
			response = new JSONObject(jsonText);

			conn.disconnect();

		} catch (MalformedURLException e) {

		} catch (IOException e) {

		} catch (JSONException e) {

		}

		return response;
	}

	private static String readUser(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}
}
