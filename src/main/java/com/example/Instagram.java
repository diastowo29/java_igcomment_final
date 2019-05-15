package com.example;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.model.DataEntry;
import com.example.repo.ClientRepository;
import com.example.repo.DataEntryRepository;
import com.example.repo.FlagRepository;
import com.example.repo.IntervalRepository;
import com.example.repo.LastEntryRepository;
import com.example.urls.Entity;
import com.google.gson.Gson;

@Controller
@SpringBootApplication
@CrossOrigin
@RequestMapping("/instagram/")
public class Instagram {

	@Autowired
	ClientRepository clientRepo;
	@Autowired
	FlagRepository flagRepo;
	@Autowired
	LastEntryRepository lastRepo;
	@Autowired
	DataEntryRepository dataRepo;
	@Autowired
	IntervalRepository intervalRepo;

	Entity entity = new Entity();
	String RETURNURL = "";

	@RequestMapping(method = RequestMethod.GET)
	String indexGet() {
		System.out.println("/get");
		RETURNURL = "testing";
		return "preadmin";
	}

	/* HANDLE POST REQUEST FROM VIEW, see preadmin.html */
	@PostMapping(value = "/admin", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			MediaType.APPLICATION_JSON_UTF8_VALUE }, produces = { MediaType.APPLICATION_ATOM_XML_VALUE,
					MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String newIndex(@RequestParam("appId") String appId, @RequestParam("appSecret") String appSecret,
			Model model) {
		model.addAttribute("appId", appId);
		model.addAttribute("appSecret", appSecret);
		return "admin";
	}

	/*
	 * @RequestMapping(method = RequestMethod.POST, consumes = {
	 * MediaType.APPLICATION_FORM_URLENCODED_VALUE,
	 * MediaType.APPLICATION_JSON_UTF8_VALUE }, produces = {
	 * MediaType.APPLICATION_ATOM_XML_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE
	 * }) String indexPost(@RequestParam Map<String, String> paramMap) {
	 * System.out.println("/post"); RETURNURL = paramMap.get("return_url");
	 * System.out.println(RETURNURL); return "preadmin"; }
	 */

	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			MediaType.APPLICATION_JSON_UTF8_VALUE }, produces = { MediaType.APPLICATION_ATOM_XML_VALUE,
					MediaType.APPLICATION_JSON_UTF8_VALUE })
	String indexPost(@RequestParam Map<String, String> paramMap, Model model) {
		RETURNURL = paramMap.get("return_url");

		/*model.addAttribute("appId", entity.APP_ID);
		model.addAttribute("appSecret", entity.APP_SECRET);*/
		model.addAttribute("callbackUrl", entity.CALLBACKURL);
		return "preadmin";
	}

	@RequestMapping("/getToken/")
	String getToken() {
		System.out.println("/getToken/");
		return "get_token";
	}

	@RequestMapping("/submit")
	String submitToken(@RequestParam("token") String token, @RequestParam("appId") String appId,
			@RequestParam("appSecret") String appSecret, Model model) {
		System.out.println("GET SUBMIT TOKEN: " + token);
		String accToken = "";
		HitApi calling = new HitApi();

		HashMap<String, String> hashMap = new HashMap<>();
		ArrayList<HashMap<String, String>> hashList = new ArrayList<>();
		try {

			JSONObject output = calling.hit(entity.getAccTokenApi(appId, appSecret) + token, "GET");
			accToken = output.getString("access_token");

			try {

				JSONObject outputAcc = calling.hit(entity.GET_ACC_ID_API + accToken, "GET");
				JSONArray igData = outputAcc.getJSONArray("data");
				if (outputAcc != null) {
					for (int i = 0; i < igData.length(); i++) {
						hashMap = new HashMap<>();
						if (igData.getJSONObject(i).has("connected_instagram_account")) {
							hashMap.put("name", igData.getJSONObject(i).getString("name"));
							hashMap.put("id", igData.getJSONObject(i).getJSONObject("connected_instagram_account")
									.getString("id"));
							hashMap.put("token", accToken);
							hashList.add(hashMap);
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		model.addAttribute("igList", hashList);
		return "ig_account";
	}

	@RequestMapping("/submittoken")
	String finalSubmit(@RequestParam(name = "getId") String igId, @RequestParam(name = "name") String igName,
			@RequestParam(name = "token") String igToken, @RequestParam(name = "option") String option, Model model) {
		System.out.println("/submittoken");
		HashMap<String, String> hashMap = new HashMap<>();
		hashMap.put("returnUrl", RETURNURL);
		System.out.println("RETURN URL: " + RETURNURL);
		hashMap.put("igId", igId);
		System.out.println("igName: " + igName);
		try {
			if (option.equals("1")) {
				hashMap.put("name", "Instagram - " + URLDecoder.decode(igName, "UTF-8") + " - Post to Ticket");
			} else {
				hashMap.put("name", "Instagram - " + URLDecoder.decode(igName, "UTF-8") + " - Comment to Ticket");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		hashMap.put("metadata",
				"{\"igId\": \"" + igId + "\", \"token\": \"" + igToken + "\", \"option\": \"" + option + "\"}");
		hashMap.put("state", "{\"state\":\"testing\"}");

		model.addAttribute("metadata", hashMap);
		return "final_submit";
	}

	@RequestMapping("/callback")
	String callBack(@RequestParam("code") String code, @RequestParam("state") String state, Model model)
			throws JSONException {
		System.out.println("/callback");
		JSONObject stateJson = new JSONObject(state.toString());
		model.addAttribute("code", code);
		model.addAttribute("appId", stateJson.getString("appId"));
		model.addAttribute("appSecret", stateJson.getString("appSecret"));

		return "callback";
	}

	@RequestMapping("/webhook")
	String webhook(@RequestParam("code") String code, @RequestParam("state") String state, Model model)
			throws JSONException {
		return "callback";
	}

	@RequestMapping(value = "/pull", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> pullV2(@RequestParam Map<String, String> paramMap) throws JSONException {

		Gson gson = new Gson();
		HashMap<String, Object> response = new HashMap<>();
		ArrayList<Object> extResource = new ArrayList<>();
		ArrayList<Object> extResourceRest = new ArrayList<>();
		JSONObject jobject = new JSONObject(paramMap.get("metadata").toString());
		String accountId = jobject.getString("igId");
		String token = jobject.getString("token");
		String option = jobject.getString("option");
		// String accountId = "17841406514405225";
		// String token =
		// "EAAFWflehVNwBAHzKh6ahR4NfmZCLy258qmjDo067JDMoiKaO36lbAk0aD2WAgd7lxR7Hx1Mf9hWy312pyXUsMCRgXv76FziZBgy5rVphRFZBCRVelF1jb9Rqm6983xdeYMPCrZBnlawVRYdReXqyQKNFtXjUqC8ZD";
		// String option = "1";

		List<DataEntry> dataEntry = dataRepo.findByCifAccountId(accountId);
		List<DataEntry> willbeDelete = new ArrayList<>();
		boolean alreadyFull = false;
		int extCounter = 0;

		System.out.println(dataRepo.count());

		for (int i = 0; i < dataEntry.size(); i++) {

			@SuppressWarnings("unchecked")
			ArrayList<Object> cifJsonData = gson.fromJson(dataEntry.get(i).getCifJsonData(), ArrayList.class);
			if (!alreadyFull) {
				for (int j = 0; j < cifJsonData.size(); j++) {
					if (extCounter >= 199) {
						alreadyFull = true;
						extResourceRest.add(cifJsonData.get(j));
					} else {
						extResource.add(cifJsonData.get(j));
						extCounter++;
					}
				}
				if (extResourceRest.size() > 0) {
					System.out.println("===== UPDATE DB WITH ID: " + dataEntry.get(i).getId() + " =====");
					doSaveDataEntryDb(dataEntry.get(i).getId(), dataEntry.get(i).getCifAccountId(),
							dataEntry.get(i).getCifPostId(), extResourceRest);
					extResourceRest = new ArrayList<>();
				} else {
					willbeDelete.add(dataEntry.get(i));
				}
			}
		}
		response.put("external_resources", extResource);

		for (int i = 0; i < willbeDelete.size(); i++) {
			dataRepo.delete(willbeDelete.get(i));
		}

		if (dataRepo.count() <= 2) {
			ThreadingTicket ticketThread = new ThreadingTicket(accountId, token, option, flagRepo, lastRepo, dataRepo,
					intervalRepo);
			ticketThread.start();
		} else {
			System.out.println("===== Still too many rows at DB =====");
		}

		return new ResponseEntity<Object>(response, HttpStatus.OK);
	}

	private void doSaveDataEntryDb(long id, String accountId, String postId, ArrayList<Object> extResource) {
		Gson gson = new Gson();
		dataRepo.save(new DataEntry(id, accountId, postId, gson.toJson(extResource)));
	}

	@RequestMapping("/manifest")
	ResponseEntity<Object> manifest() {
		System.out.println("/manifest");
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("name", "Instagram Integration Java");
		hashMap.put("id", "zendesk-internal-instagram-integration-java");
		hashMap.put("author", "Trees Solutions");
		hashMap.put("version", "v1.0");
		HashMap<String, String> urlMap = new HashMap<>();
		urlMap.put("admin_ui", entity.HEROKUDOMAIN + "instagram/");
		urlMap.put("pull_url", entity.HEROKUDOMAIN + "instagram/pull");
		urlMap.put("channelback_url", entity.HEROKUDOMAIN + "instagram/channelback");
		urlMap.put("clickthrough_url", entity.HEROKUDOMAIN + "instagram/manifest");

		hashMap.put("urls", urlMap);
		return new ResponseEntity<Object>(hashMap, HttpStatus.OK);
	}

	/* FIXME CHANNELBACK */
	@RequestMapping("/channelback")
	public ResponseEntity<Object> channelback(@RequestParam Map<String, String> paramMap)
			throws JSONException, UnsupportedEncodingException {
		System.out.println("/channelback");
		System.out.println(paramMap);
		JSONObject postComment = new JSONObject();

		/* GET COMMENT ID */
		String commentId = paramMap.get("parent_id").split("-")[2];
		String mediaId = paramMap.get("thread_id").split("-")[2];
		// String mediaUrl = paramMap.get("thread_id").split("-")[4];
		String igId = paramMap.get("thread_id").split("-")[3];
		String message = paramMap.get("message").toString();
		JSONObject metadata = new JSONObject(paramMap.get("metadata").toString());

		HitApi call = new HitApi();
		Entity ent = new Entity();

		if (metadata.getString("option").equals("1")) {
			postComment = call.hit(
					ent.createComment(mediaId, URLEncoder.encode(message, "UTF-8"), metadata.getString("token")),
					"POST");
		} else {
			postComment = call.hit(
					ent.replyComment(commentId, URLEncoder.encode(message, "UTF-8"), metadata.getString("token")),
					"POST");
		}

		HashMap<String, Object> response = new HashMap<>();
		response.put("external_id", "cif-comment-" + postComment.getString("id") + "-" + igId /* + "-" + mediaUrl */);
		response.put("allow_channelback", true);

		return new ResponseEntity<Object>(response, HttpStatus.OK);
	}

	@RequestMapping("/saveclient")
	public ResponseEntity<String> saveClient(@RequestBody Map<String, String> parameter) {
		System.out.println("/saveclient");
		System.out.println(parameter);
		return new ResponseEntity<String>(HttpStatus.OK);
	}

	@RequestMapping("/testing")
	String testingMethod(@RequestParam(name = "name", defaultValue = "dias") String name, Model model) {
		ArrayList<HashMap<String, String>> list = new ArrayList<>();

		HashMap<String, String> hashMap = new HashMap<>();
		hashMap.put("name", "amizah");
		hashMap.put("id", "1");
		list.add(hashMap);

		hashMap = new HashMap<>();
		hashMap.put("name", "diastowo");
		hashMap.put("id", "2");
		list.add(hashMap);

		model.addAttribute("namelist", list);

		return "testing";
	}

}
