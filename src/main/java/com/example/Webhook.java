package com.example;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.model.Comments;
import com.example.repo.CommentRepository;

@Controller
@SpringBootApplication
@CrossOrigin
@RequestMapping("/instagram/webhook")
public class Webhook {

	@Autowired
	CommentRepository commentRepo;

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<String> getWebhook(@RequestParam(name = "hub.challenge") String hub) {
		return new ResponseEntity<String>(hub, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			MediaType.APPLICATION_JSON_UTF8_VALUE }, produces = { MediaType.APPLICATION_ATOM_XML_VALUE,
					MediaType.APPLICATION_JSON_UTF8_VALUE })
	public ResponseEntity<String> postWebhook(@RequestBody String request) {
		System.out.println("/webhook");
		JSONObject commentJson = new JSONObject();
		String ig_id = "";
		String comment = "";
		String comment_id = "";
		String media_id = "";
		try {
			commentJson = new JSONObject(request);
			if (commentJson.has("entry")) {
				for (int i = 0; i < commentJson.getJSONArray("entry").length(); i++) {
					ig_id = commentJson.getJSONArray("entry").getJSONObject(i).getString("id");
					if (commentJson.getJSONArray("entry").getJSONObject(i).has("changes")) {
						for (int j = 0; j < commentJson.getJSONArray("entry").getJSONObject(i).getJSONArray("changes")
								.length(); j++) {
							comment = commentJson.getJSONArray("entry").getJSONObject(i).getJSONArray("changes")
									.getJSONObject(j).getJSONObject("value").getString("text");
							comment_id = commentJson.getJSONArray("entry").getJSONObject(i).getJSONArray("changes")
									.getJSONObject(j).getJSONObject("value").getString("id");
							media_id = commentJson.getJSONArray("entry").getJSONObject(i).getJSONArray("changes")
									.getJSONObject(j).getJSONObject("value").getJSONObject("media").getString("id");
						}
					}
					commentRepo.save(new Comments(0, ig_id, comment_id, comment, "", media_id, "", ""));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		System.out.println("WEbHOOK Triggered");
		System.out.println(request);
		System.out.println();
		return new ResponseEntity<String>("", HttpStatus.OK);
	}

	@RequestMapping(value = "/update")
	public ResponseEntity<String> testingUpdate() {
		commentRepo.save(new Comments(3, "123455", "4567789", "cobain lagi ahhhhh", "", "123456", "", ""));
		return new ResponseEntity<String>("OK", HttpStatus.OK);
	}
}
