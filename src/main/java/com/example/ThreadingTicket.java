package com.example;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.model.DataEntry;
import com.example.model.ErrorLogs;
import com.example.model.Flag;
import com.example.model.Interval;
import com.example.model.LastEntry;
import com.example.others.FlagStatus;
import com.example.others.ResponseCode;
import com.example.repo.DataEntryRepository;
import com.example.repo.ErrorLogsRepository;
import com.example.repo.FlagRepository;
import com.example.repo.IntervalRepository;
import com.example.repo.LastEntryRepository;
import com.example.urls.Entity;
import com.google.gson.Gson;

public class ThreadingTicket extends Thread {
	String accountId;
	String token;
	String option;
	boolean willExpired;
//	String mailRecipient;
	FlagRepository flagRepo;
	LastEntryRepository lastRepo;
	DataEntryRepository dataRepo;
	IntervalRepository intervalRepo;
	ErrorLogsRepository errorRepo;

	boolean tooMuchComment = false;

	public ThreadingTicket(String accountId, String token, String option, FlagRepository flagRepo,
			LastEntryRepository lastRepo, DataEntryRepository dataRepo, IntervalRepository intervalRepo,
			ErrorLogsRepository errorRepo, boolean willExpired/* , String mailRecipient */) {
		this.accountId = accountId;
		this.token = token;
		this.option = option;
		this.flagRepo = flagRepo;
		this.lastRepo = lastRepo;
		this.dataRepo = dataRepo;
		this.intervalRepo = intervalRepo;
		this.errorRepo = errorRepo;
		this.willExpired = willExpired;
//		this.mailRecipient = mailRecipient;
	}

	@Override
	public void run() {
		Entity ent = new Entity();
		long lastRun = 0;
		Flag flagging = flagRepo.findByCifAccountId(accountId);
		ErrorLogs errLog = errorRepo.findByCifAccountId(accountId);
		
		System.out.println("THREAD RUN");
		System.out.println("CIF STATUS " + flagging.getCifStatus());
//		Mailer mail = new Mailer();
		/*
		 * if (willExpired) { if (flagging.getCifMailCounter() >= 720 ||
		 * flagging.getCifMailCounter() == 0) { mail.sendEmail(); } }
		 */
		
		int intv = 0;
		if (flagging.getCifStatus().equals(FlagStatus.NEW.toString())) {
			Interval interval = intervalRepo.save(new Interval(0, accountId, ent.defaultInterval));
			lastRepo.save(new LastEntry(0, accountId, new Date().getTime()));
			try {
				gettingEntry(FlagStatus.INIT, "0", new Date().getTime(), flagging, interval, false, errLog);
				flagging.setCifAccountId(accountId);
				flagging.setCifStatus(FlagStatus.READY);
				flagging.setCifInterval(0);
				flagging.setCifMailCounter(0);
				flagRepo.save(flagging);
			} catch (Exception e) {
				flagging.setCifAccountId(accountId);
				flagging.setCifStatus(FlagStatus.READY);
				flagging.setCifInterval(0);
				flagging.setCifMailCounter(0);
				flagRepo.save(flagging);
			}
		} else {
			try {
				if (flagging.getCifStatus().equals(FlagStatus.READY.toString())) {
					Interval interval = intervalRepo.findByCifAccountId(accountId);
					if (interval == null) {
						interval = intervalRepo.save(new Interval(0, accountId, ent.defaultInterval));
						intv = ent.defaultInterval;
					} else {
						intv = interval.getCifInterval();
					}
					if (flagging.getCifInterval() == intv) {

						flagging.setCifStatus(FlagStatus.WAIT);
						flagRepo.save(flagging);

						LastEntry lastEntry = lastRepo.findByCifAccountId(accountId);
						lastRun = lastEntry.getCifLastEntry();

						boolean needReauth = gettingEntry(FlagStatus.PROCESSED, "0", lastRun, flagging, interval, false,
								errLog);

						if (needReauth) {
							System.out.println("===== cif need to be reauth ======");
							flagging.setCifStatus(FlagStatus.REAUTH);
							flagging.setCifInterval(2);
							if (willExpired) {
								if (flagging.getCifMailCounter() >= 720) {
									flagging.setCifMailCounter(0);
								} else {
									flagging.setCifMailCounter(flagging.getCifMailCounter() + 1);	
								}
							}
							flagRepo.save(flagging);
						} else {
							lastRepo.save(new LastEntry(lastEntry.getId(), accountId, new Date().getTime()));
							flagging.setCifStatus(FlagStatus.READY);
							flagging.setCifInterval(0);
							if (willExpired) {
								if (flagging.getCifMailCounter() >= 720) {
									flagging.setCifMailCounter(0);
								} else {
									flagging.setCifMailCounter(flagging.getCifMailCounter() + 1);	
								}
							}
							flagRepo.save(flagging);
						}
					} else {
						if (flagging.getCifInterval() > 2) {
							flagging.setCifStatus(FlagStatus.READY);
							flagging.setCifInterval(2);
							if (willExpired) {
								if (flagging.getCifMailCounter() >= 720) {
									flagging.setCifMailCounter(0);
								} else {
									flagging.setCifMailCounter(flagging.getCifMailCounter() + 1);	
								}
							}
							flagRepo.save(flagging);
						} else {
							flagging.setCifStatus(FlagStatus.READY);
							flagging.setCifInterval(flagging.getCifInterval() + 1);
							if (willExpired) {
								if (flagging.getCifMailCounter() >= 720) {
									flagging.setCifMailCounter(0);
								} else {
									flagging.setCifMailCounter(flagging.getCifMailCounter() + 1);	
								}
							}
							flagRepo.save(flagging);
							System.out.println("===== WAIT FOR INTERVAL: " + (flagging.getCifInterval()) + " =====");
						}
					}
				} else if (flagging.getCifStatus().equals(FlagStatus.REAUTH.toString())) {
					flagging.setCifStatus(FlagStatus.READY);
					flagging.setCifInterval(2);
					flagging.setCifMailCounter(0);
					flagRepo.save(flagging);
				} else {
					if (flagging.getCifWaitCounter() >= ent.MAXWAIT) {
						flagging.setCifWaitCounter(0);
						flagging.setCifStatus(FlagStatus.READY);
					} else {
						flagging.setCifWaitCounter(flagging.getCifWaitCounter() + 1);
					}
					if (willExpired) {
						if (flagging.getCifMailCounter() >= 720) {
							flagging.setCifMailCounter(0);
						} else {
							flagging.setCifMailCounter(flagging.getCifMailCounter() + 1);	
						}
					}
					flagRepo.save(flagging);
					System.out.println("===== PLEASE WAIT, ITS STILL RUNNING =====");
				}
			} catch (Exception e) {
				
				System.out.println("===== ERROR =====");
				e.printStackTrace();
				e.getLocalizedMessage();

				flagging.setCifStatus(FlagStatus.READY);
				flagging.setCifInterval(0);
				if (willExpired) {
					if (flagging.getCifMailCounter() >= 720) {
						flagging.setCifMailCounter(0);
					} else {
						flagging.setCifMailCounter(flagging.getCifMailCounter() + 1);	
					}
				}
				flagRepo.save(flagging);
			}
		}
		
		System.out.println("===== " + accountId + " Finished =====");
		return;
	}

	public Flag newAccountFlag(String accountId) {
		Flag flagging = flagRepo.save(new Flag(0, accountId, FlagStatus.WAIT, 0, 3, 0, 0));
		return flagging;
	}

	public boolean gettingEntry(FlagStatus flagStatus, String nextUrl, long lastRun, Flag flagging, Interval interval,
			boolean tooMuchComment, ErrorLogs errLog) throws IOException {
		HitApi calling = new HitApi();
		Entity ent = new Entity();
		JSONObject allMedia = new JSONObject();

		int commentLimit = 198;

		boolean thatsAll = false;
		boolean needReauth = false;

		String apiUrl = "";
		HashMap<String, Object> extObj = new HashMap<>();
		ArrayList<Object> extResource = new ArrayList<>();

		System.out.println("GETTING ENTRY");
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

			if (nextUrl.equals("0")) {
				apiUrl = ent.getMediaUrl(accountId, token);
			} else {
				apiUrl = nextUrl;
			}

			try {
				System.out.println(apiUrl);
				allMedia = calling.hit(apiUrl, "GET", errorRepo, accountId, errLog);
			} catch (RuntimeException e) {
				e.printStackTrace();
				flagging.setCifStatus(FlagStatus.READY);
				flagging.setCifInterval(0);
				flagging.setCifWaitCounter(0);
				flagRepo.save(flagging);
			}
			if (allMedia.has("failed_status")) {
				if (allMedia.get("code").toString().equals(ResponseCode.BAD_REQUEST.toString())) {
					needReauth = true;
				}
			} else {
				if (allMedia.has("data")) {
					for (int i = 0; i < allMedia.getJSONArray("data").length(); i++) {
						extResource = new ArrayList<>();
						Date postDate = sdf
								.parse(allMedia.getJSONArray("data").getJSONObject(i).getString("timestamp"));

						/*
						 * ZonedDateTime zdt =
						 * ZonedDateTime.parse(allMedia.getJSONArray("data").getJSONObject(i)
						 * .getString("timestamp").replace("+0000", "Z")); LocalDateTime ldt =
						 * zdt.toLocalDateTime();
						 * 
						 * System.out.println(allMedia.getJSONArray("data").getJSONObject(i).getString(
						 * "timestamp")); System.out.println(ldt);
						 */

						long diff = (lastRun) - (postDate.getTime());
						/*
						 * long diffHours = diff / (60 * 60 * 1000);
						 * 
						 * long diffSeconds = diff / 1000 % 60; long diffMinutes = diff / (60 * 1000) %
						 * 60; long diffHours = diff / (60 * 60 * 1000) % 24;
						 */
						long diffDays = diff / (24 * 60 * 60 * 1000);

						if (diffDays <= flagging.getCifDayLimit()) {
							System.out.println("===== Getting post: " + i + " =====");
							String parentMedia = allMedia.getJSONArray("data").getJSONObject(i).getString("id") + "-"
									+ accountId;
							HashMap<String, String> author = new HashMap<>();

							if (allMedia.getJSONArray("data").getJSONObject(i).getInt("comments_count") > 500) {
								tooMuchComment = true;
							}

							author.put("external_id", "cif-user-" + allMedia.getJSONArray("data").getJSONObject(i)
									.getJSONObject("owner").getString("username") + "-" + accountId);
							author.put("name", allMedia.getJSONArray("data").getJSONObject(i).getJSONObject("owner")
									.getString("username"));
							extObj = new HashMap<>();
							extObj.put("external_id", "cif-media-" + parentMedia);
							try {
								extObj.put("message",
										allMedia.getJSONArray("data").getJSONObject(i).getString("caption"));
							} catch (Exception e) {
								extObj.put("message", "Post without caption");
							}
							extObj.put("created_at", allMedia.getJSONArray("data").getJSONObject(i)
									.getString("timestamp").replace("+0000", "Z"));

							HashMap<String, String> displayObject = new HashMap<>();
							HashMap<String, Object> displayInfo = new HashMap<>();
							ArrayList<Object> displayArray = new ArrayList<>();
							ArrayList<Object> fieldsArray = new ArrayList<>();
							ArrayList<String> tagsArray = new ArrayList<>();

							displayObject.put("media_url",
									allMedia.getJSONArray("data").getJSONObject(i).getString("media_url"));
							displayInfo.put("type", "cif-media-" + parentMedia);
							displayInfo.put("data", displayObject);
							displayArray.add(displayInfo);
							displayObject = new HashMap<>();
							displayInfo = new HashMap<>();
							try {
								displayObject.put("media_caption",
										allMedia.getJSONArray("data").getJSONObject(i).getString("caption"));
							} catch (JSONException e) {
								displayObject.put("media_caption", "Post without caption");
							}
							displayInfo.put("type", "cif-caption-" + parentMedia);
							displayInfo.put("data", displayObject);
							displayArray.add(displayInfo);

							extObj.put("display_info", displayArray);
							extObj.put("author", author);
							extObj.put("allow_channelback", true);

							tagsArray.add("ig_" + allMedia.getJSONArray("data").getJSONObject(i).getString("id"));
							HashMap<String, Object> fieldsObj = new HashMap<>();
							fieldsObj.put("id", "tags");
							fieldsObj.put("value", tagsArray);
							fieldsArray.add(fieldsObj);
							fieldsObj = new HashMap<>();
							fieldsObj.put("id", "external_id");
							fieldsObj
									.put("value",
											"cif-user-"
													+ allMedia.getJSONArray("data").getJSONObject(i)
															.getJSONObject("owner").getString("username")
													+ "-" + accountId);
							fieldsArray.add(fieldsObj);
							extObj.put("fields", fieldsArray);

							extResource.add(extObj);
							if (allMedia.getJSONArray("data").getJSONObject(i).has("comments")) {
								// boolean gotAllComment = false;
								for (int j = 0; j < allMedia.getJSONArray("data").getJSONObject(i)
										.getJSONObject("comments").getJSONArray("data").length(); j++) {
									boolean continueExt = false;
									JSONObject mediaJson = allMedia.getJSONArray("data").getJSONObject(i)
											.getJSONObject("comments");

									continueExt = checkForContinue(flagStatus,
											allMedia.getJSONArray("data").getJSONObject(i).getJSONObject("comments")
													.getJSONArray("data").getJSONObject(j).getString("timestamp"),
											lastRun);

									extResource = extractData(allMedia, i, mediaJson, j, displayObject, displayInfo,
											displayArray, fieldsArray, tagsArray, author, option, accountId, extObj,
											parentMedia, extResource, continueExt);

									if (extResource.size() >= commentLimit) {
										doSaveDb(0, accountId,
												allMedia.getJSONArray("data").getJSONObject(i).getString("id"),
												extResource);
										extResource = new ArrayList<>();
									}
								}
								String pageUrl = "";
								if (allMedia.getJSONArray("data").getJSONObject(i).getJSONObject("comments")
										.has("paging")) {
									if (allMedia.getJSONArray("data").getJSONObject(i).getJSONObject("comments")
											.getJSONObject("paging").has("next")) {
										try {
											pageUrl = allMedia.getJSONArray("data").getJSONObject(i)
													.getJSONObject("comments").getJSONObject("paging")
													.getString("next");

											JSONObject mediaPaging = getPaging(pageUrl, flagging.getId(),
													flagging.getCifAccountId(), errLog);
											if (mediaPaging.has("failed_status")) {
												if (mediaPaging.get("code").toString()
														.equals(ResponseCode.BAD_REQUEST.toString())) {
													/*
													 * flagRepo.save(new Flag(flagging.getId(),
													 * flagging.getCifAccountId(), FlagStatus.REAUTH, 0,
													 * flagging.getCifDayLimit()));
													 */

													needReauth = true;
												}
											} else {
												for (int p = 0; p < mediaPaging.getJSONArray("data").length(); p++) {
													boolean continueExt = false;

													continueExt = checkForContinue(flagStatus,
															mediaPaging.getJSONArray("data").getJSONObject(p)
																	.getString("timestamp"),
															lastRun);

													extResource = extractData(allMedia, i, mediaPaging, p,
															displayObject, displayInfo, displayArray, fieldsArray,
															tagsArray, author, option, accountId, extObj, parentMedia,
															extResource, continueExt);

													if (extResource.size() >= commentLimit) {
														doSaveDb(0, accountId, allMedia.getJSONArray("data")
																.getJSONObject(i).getString("id"), extResource);
														extResource = new ArrayList<>();
													}
												}
												while (mediaPaging.has("paging")) {
													if (mediaPaging.getJSONObject("paging").has("next")) {
														try {
															mediaPaging = getPaging(
																	mediaPaging.getJSONObject("paging")
																			.getString("next"),
																	flagging.getId(), flagging.getCifAccountId(),
																	errLog);
															if (mediaPaging.has("failed_status")) {
																if (mediaPaging.get("code").toString()
																		.equals(ResponseCode.BAD_REQUEST.toString())) {
																	/*
																	 * flagRepo.save(new Flag(flagging.getId(),
																	 * flagging.getCifAccountId(), FlagStatus.REAUTH, 0,
																	 * flagging.getCifDayLimit()));
																	 */

																	needReauth = true;
																}
															} else {
																for (int p = 0; p < mediaPaging.getJSONArray("data")
																		.length(); p++) {
																	boolean continueExt = false;

																	continueExt = checkForContinue(flagStatus,
																			mediaPaging.getJSONArray("data")
																					.getJSONObject(p)
																					.getString("timestamp"),
																			lastRun);

																	extResource = extractData(allMedia, i, mediaPaging,
																			p, displayObject, displayInfo, displayArray,
																			fieldsArray, tagsArray, author, option,
																			accountId, extObj, parentMedia, extResource,
																			continueExt);

																	if (extResource.size() >= commentLimit) {
																		doSaveDb(0, accountId,
																				allMedia.getJSONArray("data")
																						.getJSONObject(i)
																						.getString("id"),
																				extResource);
																		extResource = new ArrayList<>();
																	}
																}
															}
														} catch (RuntimeException e) {
															e.printStackTrace();

															flagging.setCifStatus(FlagStatus.READY);
															flagging.setCifInterval(0);
															flagRepo.save(flagging);

														}
													}
												}
											}
										} catch (RuntimeException e) {
											e.printStackTrace();

											flagging.setCifStatus(FlagStatus.READY);
											flagging.setCifInterval(0);
											flagRepo.save(flagging);

										}
									}
								}
							}

							try {
								doSaveDb(0, accountId, allMedia.getJSONArray("data").getJSONObject(i).getString("id"),
										extResource);
								extResource = new ArrayList<>();

							} catch (NullPointerException e) {
								doSaveDb(0, accountId, allMedia.getJSONArray("data").getJSONObject(i).getString("id"),
										extResource);
								extResource = new ArrayList<>();
							}
						} else {
							thatsAll = true;
						}
					}
				}
				if (!needReauth) {
					if (!thatsAll) {
						if (allMedia.has("paging")) {
							if (allMedia.getJSONObject("paging").has("next")) {
								gettingEntry(flagStatus, allMedia.getJSONObject("paging").getString("next"), lastRun,
										flagging, interval, tooMuchComment, errLog);
							}
						}
					}
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return needReauth;
	}

	private void doSaveDb(long i, String accountId, String id, ArrayList<Object> extResource) {
		Gson gson = new Gson();
		dataRepo.save(new DataEntry(i, accountId, id, gson.toJson(extResource)));
	}

	private boolean checkForContinue(FlagStatus flagStatus, String dateValidate, long lastRun) {
		boolean continueExt = false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		try {
			if (flagStatus.equals(FlagStatus.INIT)) {
				continueExt = true;
			} else {
				Date commentDate = sdf.parse(dateValidate);
				long diffComment = (commentDate.getTime() - (lastRun));
				long diffCommentSeconds = diffComment / (1000);
				// System.out.println("DIFFERENCE SECONDS: " + diffCommentSeconds);
				if (diffCommentSeconds > -120) {
					continueExt = true;
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return continueExt;
	}

	public JSONObject getPaging(String url, long flagId, String flagAccountId, ErrorLogs errLog) {
		JSONObject mediaPaging = new JSONObject();
		HitApi api = new HitApi();
		mediaPaging = api.hit(url, "GET", errorRepo, accountId, errLog);
		return mediaPaging;
	}

	private ArrayList<Object> extractData(JSONObject allMedia, int i, JSONObject mediaPaging, int j,
			HashMap<String, String> displayObject, HashMap<String, Object> displayInfo, ArrayList<Object> displayArray,
			ArrayList<Object> fieldsArray, ArrayList<String> tagsArray, HashMap<String, String> author, String option,
			String igId, HashMap<String, Object> extObj, String parentMedia, ArrayList<Object> extResource,
			boolean continueExt) throws JSONException {

		String parentComment = mediaPaging.getJSONArray("data").getJSONObject(j).getString("id") + "-" + igId;

		author = new HashMap<>();
		author.put("external_id",
				"cif-user-" + mediaPaging.getJSONArray("data").getJSONObject(j).getString("username") + "-" + igId);
		author.put("name", mediaPaging.getJSONArray("data").getJSONObject(j).getString("username"));
		extObj = new HashMap<>();
		if (option.equals("1")) {
			extObj.put("parent_id", "cif-media-" + parentMedia);
		}

		displayObject = new HashMap<>();
		displayInfo = new HashMap<>();
		displayArray = new ArrayList<>();

		displayObject.put("media_url", allMedia.getJSONArray("data").getJSONObject(i).getString("media_url"));
		displayInfo.put("type", "cif-comment-" + parentComment);
		displayInfo.put("data", displayObject);
		displayArray.add(displayInfo);
		displayObject = new HashMap<>();
		displayInfo = new HashMap<>();
		try {
			displayObject.put("media_caption", allMedia.getJSONArray("data").getJSONObject(i).getString("caption"));
		} catch (JSONException e) {
			displayObject.put("media_caption", "Post without caption");
		}
		displayInfo.put("type", "cif-caption-" + parentMedia);
		displayInfo.put("data", displayObject);
		displayArray.add(displayInfo);

		if (continueExt) {
			extObj.put("display_info", displayArray);

			extObj.put("external_id", "cif-comment-" + parentComment);
			extObj.put("message", mediaPaging.getJSONArray("data").getJSONObject(j).getString("text"));
			extObj.put("created_at",
					mediaPaging.getJSONArray("data").getJSONObject(j).getString("timestamp").replace("+0000", "Z"));
			extObj.put("author", author);
			extObj.put("allow_channelback", true);

			extObj.put("fields", fieldsArray);

			extResource.add(extObj);
		}

		if (mediaPaging.getJSONArray("data").getJSONObject(j).has("replies")) {
			for (int k = (mediaPaging.getJSONArray("data").getJSONObject(j).getJSONObject("replies")
					.getJSONArray("data").length() - 1); k > -1; k--) {
				extResource = extractReplies(author, mediaPaging, parentMedia, extObj, igId, k, j, displayInfo,
						displayObject, option, parentComment, displayArray, allMedia, i, fieldsArray, extResource);
			}
		}
		return extResource;
	}

	private ArrayList<Object> extractReplies(HashMap<String, String> author, JSONObject mediaPaging, String parentMedia,
			HashMap<String, Object> extObj, String igId, int k, int j, HashMap<String, Object> displayInfo,
			HashMap<String, String> displayObject, String option, String parentComment, ArrayList<Object> displayArray,
			JSONObject allMedia, int i, ArrayList<Object> fieldsArray, ArrayList<Object> extResource)
			throws JSONException {
		author = new HashMap<>();
		author.put("external_id", "cif-user-" + mediaPaging.getJSONArray("data").getJSONObject(j)
				.getJSONObject("replies").getJSONArray("data").getJSONObject(k).getString("username") + "-" + igId);
		author.put("name", mediaPaging.getJSONArray("data").getJSONObject(j).getJSONObject("replies")
				.getJSONArray("data").getJSONObject(k).getString("username"));
		extObj = new HashMap<>();
		if (option.equals("1")) {
			extObj.put("parent_id", "cif-media-" + parentMedia);
		} else {
			extObj.put("parent_id", "cif-comment-" + parentComment);
		}

		displayObject = new HashMap<>();
		displayInfo = new HashMap<>();
		displayArray = new ArrayList<>();

		displayObject.put("media_url", allMedia.getJSONArray("data").getJSONObject(i).getString("media_url"));
		if (option.equals("1")) {
			displayInfo.put("type", "cif-media-" + parentMedia);
		} else {
			displayInfo.put("type", "cif-comment-" + parentComment);
		}
		displayInfo.put("data", displayObject);
		displayArray.add(displayInfo);
		displayObject = new HashMap<>();
		displayInfo = new HashMap<>();
		try {
			displayObject.put("media_caption", allMedia.getJSONArray("data").getJSONObject(i).getString("caption"));
		} catch (JSONException e) {
			displayObject.put("media_caption", "Post without caption");
		}
		displayInfo.put("type", "cif-caption-" + parentMedia);
		displayInfo.put("data", displayObject);
		displayArray.add(displayInfo);

		extObj.put("display_info", displayArray);
		extObj.put("external_id", "cif-comment-" + mediaPaging.getJSONArray("data").getJSONObject(j)
				.getJSONObject("replies").getJSONArray("data").getJSONObject(k).getString("id") + "-" + igId);
		extObj.put("message", mediaPaging.getJSONArray("data").getJSONObject(j).getJSONObject("replies")
				.getJSONArray("data").getJSONObject(k).getString("text"));
		extObj.put("created_at", mediaPaging.getJSONArray("data").getJSONObject(j).getJSONObject("replies")
				.getJSONArray("data").getJSONObject(k).getString("timestamp").replace("+0000", "Z"));
		extObj.put("author", author);
		extObj.put("allow_channelback", true);

		extObj.put("fields", fieldsArray);

		extResource.add(extObj);

		return extResource;
	}

}
