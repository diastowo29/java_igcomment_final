package com.example;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.model.DataEntry;
import com.example.model.ErrorLogs;
import com.example.model.Flag;
import com.example.others.FlagStatus;
import com.example.repo.ClientRepository;
import com.example.repo.DataEntryRepository;
import com.example.repo.ErrorLogsRepository;
import com.example.repo.FlagRepository;
import com.example.repo.IntervalRepository;
import com.example.repo.LastEntryRepository;
import com.example.urls.Entity;
import com.google.gson.Gson;

import kong.unirest.JsonNode;
import kong.unirest.Unirest;

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
	@Autowired
	ErrorLogsRepository errorRepo;

	Entity entity = new Entity();
	String RETURNURL = "";

	@RequestMapping(method = RequestMethod.GET)
	String indexGet() {
		System.out.println("/get");
		RETURNURL = "testing";
		return "preadmin";
	}

	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			MediaType.APPLICATION_JSON_UTF8_VALUE }, produces = { MediaType.APPLICATION_ATOM_XML_VALUE,
					MediaType.APPLICATION_JSON_UTF8_VALUE })
	String indexPost(@RequestParam Map<String, String> paramMap, Model model) {
		RETURNURL = paramMap.get("return_url");
		String metadata = paramMap.get("metadata");

		if (!metadata.isEmpty()) {
			JSONObject metaObject = new JSONObject(metadata);
//			model.addAttribute("email", metaObject.get("email").toString());
			if (metaObject.has("app_id")) {				
				model.addAttribute("app_id", metaObject.get("app_id").toString());
			} else {
				model.addAttribute("app_id", "");
			}
			
			if (metaObject.has("app_secret")) {				
				model.addAttribute("app_secret", metaObject.get("app_secret").toString());
			} else {
				model.addAttribute("app_secret", "");
			}
		} else {
			model.addAttribute("email", "");
			model.addAttribute("app_id", "");
			model.addAttribute("app_secret", "");
		}

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
			@RequestParam("appSecret") String appSecret, /* @RequestParam("email") String email, */ Model model) {
		System.out.println("GET SUBMIT TOKEN: " + token);
		System.out.println("GET APP ID: " + appId);
		System.out.println("GET APP SECRET: " + appSecret);
//		System.out.println("GET EMAIL: " + email);
		String accToken = "";
		HitApi calling = new HitApi();

		HashMap<String, String> hashMap = new HashMap<>();
		ArrayList<HashMap<String, String>> hashList = new ArrayList<>();
		try {

			JSONObject output = calling.hitAuth(entity.getAccTokenApi(appId, appSecret) + token, "GET", errorRepo,
					appId + " - Submit");
			accToken = output.getString("access_token");
			System.out.println("ACC TOKEN: " + accToken);

			try {
				JSONObject outputAcc = calling.hitAuth(entity.GET_ACC_ID_API + accToken, "GET", errorRepo,
						appId + " - Submit");
				JSONArray igData = outputAcc.getJSONArray("data");
				if (outputAcc != null) {
					for (int i = 0; i < igData.length(); i++) {
						hashMap = new HashMap<>();
						if (igData.getJSONObject(i).has("connected_instagram_account")) {
							hashMap.put("name", igData.getJSONObject(i).getString("name"));
							hashMap.put("app_id", appId);
							hashMap.put("app_secret", appSecret);
							hashMap.put("id", igData.getJSONObject(i).getJSONObject("connected_instagram_account")
									.getString("id"));
							hashMap.put("token", accToken);
//							hashMap.put("email", email);
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
			@RequestParam(name = "token") String igToken, @RequestParam(name = "option") String option,
			@RequestParam(name = "app_id") String app_id, @RequestParam(name = "app_secret") String app_secret,
			/* @RequestParam(name = "email") String email, */Model model) {
		HashMap<String, String> hashMap = new HashMap<>();
		hashMap.put("returnUrl", RETURNURL);
		hashMap.put("igId", igId);
		System.out.println("igName: " + igName);
//		System.out.println("email: " + email);
		try {
			if (option.equals("1")) {
				hashMap.put("name", "Instagram - " + URLDecoder.decode(igName, "UTF-8") + " - Post to Ticket");
			} else {
				hashMap.put("name", "Instagram - " + URLDecoder.decode(igName, "UTF-8") + " - Comment to Ticket");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		/* GET TOKEN EXPIRED DATE HERE */

		StringBuilder sb = new StringBuilder();

		Unirest.get(entity.getTokenExpDateApi(app_id, igToken)).asJson().ifSuccess(response -> {
			JsonNode expiredDateObj = response.getBody();
			int secondLeft = expiredDateObj.getObject().getInt("expires_in");
			int daysLeft = secondLeft / 3600 / 24;

			Date currentDate = new Date();
			Calendar c = Calendar.getInstance();
			c.setTime(currentDate);
			c.add(Calendar.DATE, daysLeft);

			sb.append(c.getTimeInMillis());
//			expiredTime += c.getTimeInMillis();

		}).ifFailure(response -> {
			System.out.println(response.getStatus());
			System.out.println(response.getStatusText());
		});

		/* GET TOKEN EXPIRED DATE HERE */
		JSONObject metadata = new JSONObject();
		metadata.put("igId", igId);
		metadata.put("app_id", app_id);
		metadata.put("app_secret", app_secret);
		metadata.put("token", igToken);
		metadata.put("option", option);
//		metadata.put("email", email);
		metadata.put("exp_date", sb.toString());

		hashMap.put("metadata", metadata.toString());
		hashMap.put("state", "{\"state\":\"testing\"}");

		Flag flagging = flagRepo.findByCifAccountId(igId);
		try {
			flagRepo.save(new Flag(flagging.getId(), igId, FlagStatus.READY, flagging.getCifInterval(),
					flagging.getCifDayLimit(), flagging.getCifWaitCounter(), 0));
		} catch (NullPointerException e) {
			System.out.println("Is it new flag ?");
		}

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

	@RequestMapping(value = "/pull", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> pullV2(@RequestParam Map<String, String> paramMap) throws JSONException {

		boolean willExpired = true;

		Gson gson = new Gson();
		HashMap<String, Object> response = new HashMap<>();
		ArrayList<Object> extResource = new ArrayList<>();
		ArrayList<Object> extResourceRest = new ArrayList<>();
		JSONObject jobject = new JSONObject(paramMap.get("metadata").toString());
		String accountId = jobject.getString("igId");
		String token = jobject.getString("token");
		String option = jobject.getString("option");
//		String mailRecipient = jobject.getString("email");

		System.out.println("IG ID: " + accountId);
		System.out.println("IG TOKEN: " + token);
//		System.out.println("EXPIRED DATE: " + jobject.getString("exp_date"));
//		System.out.println("MAIL RECIPIENT: " + mailRecipient);

//		long longDate = Long.parseLong(jobject.getString("exp_date"));
//		Date expiredDate = new Date();
//		expiredDate.setTime(longDate);

//		System.out.println(expiredDate);

//		Date currentDate = new Date();
//
//		long diff = expiredDate.getTime() - currentDate.getTime();
//		long daysLeft = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
//		System.out.println(daysLeft);
//		if (daysLeft < 14) {
//			willExpired = true;
//		}

		List<DataEntry> dataEntry = dataRepo.findByCifAccountId(accountId);

		Flag flagging = flagRepo.findByCifAccountId(accountId);

		List<DataEntry> willbeDelete = new ArrayList<>();
		boolean alreadyFull = false;
		int extCounter = 0;
		
		checkErrorLogs(accountId);
		
//		System.out.println(dataRepo.count());

		/* TEST */

		HttpStatus responseCode;

		if (flagging == null) {
			flagging = newAccountFlag(accountId);
		}

		if (flagging.getCifStatus().equals(FlagStatus.REAUTH.toString())) {
			responseCode = HttpStatus.UNAUTHORIZED;
		} else {
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
				ThreadingTicket ticketThread = new ThreadingTicket(accountId, token, option, flagRepo, lastRepo,
						dataRepo, intervalRepo, errorRepo, willExpired);
				ticketThread.start();
			} else {
				System.out.println("===== Still too many rows at DB =====");
			}
			responseCode = HttpStatus.OK;
		}

		return new ResponseEntity<Object>(response, responseCode);
	}

	public Flag newAccountFlag(String accountId) {
		Flag flagging = flagRepo.save(new Flag(0, accountId, FlagStatus.NEW, 0, 3, 0, 0));
		return flagging;
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
		urlMap.put("clickthrough_url", entity.HEROKUDOMAIN + "instagram/clickthrough");

		hashMap.put("urls", urlMap);
		return new ResponseEntity<Object>(hashMap, HttpStatus.OK);
	}

	@RequestMapping("/clickthrough")
	ResponseEntity<Object> clickthrough() {
		System.out.println("/manifest");
		HashMap<String, Object> hashMap = new HashMap<>();
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
		String igId = paramMap.get("thread_id").split("-")[3];
		String message = paramMap.get("message").toString();
		JSONObject metadata = new JSONObject(paramMap.get("metadata").toString());

		HitApi call = new HitApi();
		Entity ent = new Entity();

		HttpStatus reponseCode;

		if (metadata.getString("option").equals("1")) {
			postComment = call.hitAuth(
					ent.createComment(mediaId, URLEncoder.encode(message, "UTF-8"), metadata.getString("token")),
					"POST", errorRepo, igId + " - Channelback");
		} else {
			postComment = call.hitAuth(
					ent.replyComment(commentId, URLEncoder.encode(message, "UTF-8"), metadata.getString("token")),
					"POST", errorRepo, igId + " - Channelback");
		}

		HashMap<String, Object> response = new HashMap<>();

		if (postComment.has("failed_status")) {
			reponseCode = HttpStatus.INTERNAL_SERVER_ERROR;
		} else {
			reponseCode = HttpStatus.OK;
			response.put("external_id", "cif-comment-" + postComment.getString("id") + "-" + igId);
			response.put("allow_channelback", true);
		}

		return new ResponseEntity<Object>(response, reponseCode);
	}

	@RequestMapping("/saveclient")
	public ResponseEntity<String> saveClient(@RequestBody Map<String, String> parameter) {
		System.out.println("/saveclient");
		System.out.println(parameter);
		return new ResponseEntity<String>(HttpStatus.OK);
	}
	
	public void checkErrorLogs (String accountId) {		
		List<ErrorLogs> errorLogs = errorRepo.findAll();
//		System.out.println(errorLogs.size());
		if (errorLogs.size() > 1000) {
			errorRepo.deleteAll();
		}
	}
}
