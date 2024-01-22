/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClientError;
import org.gumtree.core.object.IDisposable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import au.gov.ansto.bragg.nbi.server.git.GitException;
import au.gov.ansto.bragg.nbi.server.git.GitService;
import au.gov.ansto.bragg.nbi.server.git.GitService.GitCommit;
import au.gov.ansto.bragg.nbi.server.internal.AbstractUserControlRestlet;
import au.gov.ansto.bragg.nbi.server.internal.LinkedJSONObject;
import au.gov.ansto.bragg.nbi.server.internal.UserSessionService;
import au.gov.ansto.bragg.nbi.server.login.UserSessionObject;

/**
 * @author nxi
 *
 */
public class SEYamlRestlet extends AbstractUserControlRestlet implements IDisposable {

	private static final String PROPERTY_SERVER_SEDB_NAME = "gumtree.server.SEDBName";
	private static final String PROPERTY_SERVER_SEDB_PATH = "gumtree.server.SEDBPath";
	private static final String PROPERTY_SERVER_SECONFIG_NAME = "gumtree.server.SEConfigName";
	private static final String PROPERTY_SERVER_SECONFIG_PATH = "gumtree.server.SEConfigPath";

	private static final String QUERY_ENTRY_INSTRUMENT = "inst";
	private static final String QUERY_ENTRY_DEVICEID = "did";
	private static final String QUERY_ENTRY_MESSAGE = "msg";
	private static final String QUERY_ENTRY_OLDDID = "oldDid";
	private static final String QUERY_ENTRY_NEWDID = "newDid";
	private static final String QUERY_ENTRY_VERSION_ID = "version";
	private static final String QUERY_ENTRY_TIMESTAMP = "timestamp";

	private static final String SEG_NAME_SEDB = "sedb";
	private static final String SEG_NAME_SECONFIG = "seconfig";
	private static final String SEG_NAME_DBHISTORY = "dbhistory";
	private static final String SEG_NAME_CONFIGHISTORY = "confighistory";
	private static final String SEG_NAME_DBSAVE = "dbsave";
	private static final String SEG_NAME_DBREMOVE = "dbremove";
	private static final String SEG_NAME_DBLOAD = "dbload";
	private static final String SEG_NAME_CONFIGSAVE = "configsave";
	private static final String SEG_NAME_CHANGENAME = "changeName";
	
	private static final String QUERY_ENTRY_PATH = "path";

	private static String dbPath;
	private static String dbName;
	private static String configPath;
	private static String configName;

	private static String JSON_OK;

	private static Map<String, GitService> gitServiceMap;
	private static SimpleDateFormat formater;

	private static final Logger logger = LoggerFactory.getLogger(SEYamlRestlet.class);

	public SEYamlRestlet(){
		this(null);
	}
	/**
	 * @param context
	 */
	public SEYamlRestlet(Context context) {
		super(context);
		dbPath = System.getProperty(PROPERTY_SERVER_SEDB_PATH);
		dbName = System.getProperty(PROPERTY_SERVER_SEDB_NAME);
		configPath = System.getProperty(PROPERTY_SERVER_SECONFIG_PATH);
		configName = System.getProperty(PROPERTY_SERVER_SECONFIG_NAME);
		
		gitServiceMap = new HashMap<String, GitService>();

		if (formater == null) {
			formater = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
		}
		if (JSON_OK == null) {
			JSONObject json = new JSONObject();
			try {
				json.append("status", "OK");
			} catch (JSONException e) {
			}
			JSON_OK = json.toString();
		}
	}

	@Override
	public void handle(Request request, Response response) {
		String seg = request.getResourceRef().getLastSegment();

		UserSessionObject session = null;
		
		try {
			session = UserSessionService.getSession(request, response);
		} catch (Exception e1) {
			response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, e1.toString());
			return;
		}
		
		if (session == null || !session.isValid()) {
			try {
				session = checkDavSession(request);
			} catch (Exception e) {
			}
		}

		if (session != null && session.isValid()) {
			if (SEG_NAME_SEDB.equalsIgnoreCase(seg)){
				try {
//					Form form = request.getResourceRef().getQueryAsForm();
//					response.setEntity("Done", MediaType.TEXT_PLAIN);
//					copyFromRemote(instrumentId, session);
					Object model = loadSEDB();
					JSONObject jsonObject = (JSONObject) _convertToJson(model);
//					JSONObject jsonObject = new JSONObject(model.toString());
					response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("failed to load db model", e);
//					response.setEntity("{'status':'ERROR','reason':'" + e.getMessage() + "'}", MediaType.APPLICATION_JSON);
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
					return;
				}					

			} else if (SEG_NAME_SECONFIG.equalsIgnoreCase(seg)){
				try {
					Form form = request.getResourceRef().getQueryAsForm();
//					response.setEntity("Done", MediaType.TEXT_PLAIN);
//					copyFromRemote(instrumentId, session);
					String instrumentId = form.getValues(QUERY_ENTRY_INSTRUMENT);
					Object model = loadSEConfig(instrumentId);
					JSONObject jsonObject = (JSONObject) _convertToJson(model);
//					JSONObject jsonObject = new JSONObject(model.toString());
					response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("failed to load config model", e);
//					response.setEntity("{'status':'ERROR','reason':'" + e.getMessage() + "'}", MediaType.APPLICATION_JSON);
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
					return;
				}

			} else if (SEG_NAME_DBHISTORY.equalsIgnoreCase(seg)) {
				try {
					Form form = request.getResourceRef().getQueryAsForm();
//					String did = form.getValues(QUERY_ENTRY_DEVICEID);
					String did = form.getValues(QUERY_ENTRY_DEVICEID);
					List<GitCommit> commits = getSEDBGitService().getCommits(dbName);
//					JSONObject jsonObject = new JSONObject(new LinkedHashMap<String, Object>());
					JSONArray jsonArray = new JSONArray();
//					int i = 0;
					for (GitCommit commit : commits) {
						if (did != null && did.trim().length() > 0) {
							String message = commit.getMessage();
							if (!message.contains(did)) {
								if (!message.contains("fetch remote version")) {
									continue;
								}
							}
						}
						JSONObject obj = new JSONObject();
						obj.put("id", commit.getId());
						obj.put("name", commit.getName());
						obj.put("short message", commit.getShortMessage());
						obj.put("message", commit.getMessage());
						obj.put("timestamp", commit.getTimestamp());
//						jsonObject.put(String.valueOf(i), obj);
						jsonArray.put(obj);
//						i ++;
					}
//					JSONObject jsonObject = new JSONObject(model.toString());
					response.setEntity(jsonArray.toString(), MediaType.TEXT_PLAIN);
					response.setStatus(Status.SUCCESS_OK);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("failed to load history", e);
//					response.setEntity("{'status':'ERROR','reason':'" + e.getMessage() + "'}", MediaType.APPLICATION_JSON);
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
					return;
				}
			}  else if (SEG_NAME_CONFIGHISTORY.equalsIgnoreCase(seg)) {
				try {
					Form form = request.getResourceRef().getQueryAsForm();
//					String did = form.getValues(QUERY_ENTRY_DEVICEID);
					String path = form.getValues(QUERY_ENTRY_PATH);
					String instrumentId = form.getValues(QUERY_ENTRY_INSTRUMENT);
					List<GitCommit> commits = getSEConfigGitService(instrumentId).getCommits(configName);
//					JSONObject jsonObject = new JSONObject(new LinkedHashMap<String, Object>());
					JSONArray jsonArray = new JSONArray();
//					int i = 0;
					for (GitCommit commit : commits) {
						if (path != null && path.trim().length() > 0) {
							String message = commit.getMessage();
							if (!message.contains(path)) {
								if (!message.contains("fetch remote version")) {
									continue;
								}
							}
						}
						JSONObject obj = new JSONObject();
						obj.put("id", commit.getId());
						obj.put("name", commit.getName());
						obj.put("short message", commit.getShortMessage());
						obj.put("message", commit.getMessage());
						obj.put("timestamp", commit.getTimestamp());
//						jsonObject.put(String.valueOf(i), obj);
						jsonArray.put(obj);
//						i ++;
					}
//					JSONObject jsonObject = new JSONObject(model.toString());
					response.setEntity(jsonArray.toString(), MediaType.TEXT_PLAIN);
					response.setStatus(Status.SUCCESS_OK);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("failed to load history", e);
//					response.setEntity("{'status':'ERROR','reason':'" + e.getMessage() + "'}", MediaType.APPLICATION_JSON);
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
					return;
				}
			} else if (SEG_NAME_DBSAVE.equalsIgnoreCase(seg)){
				try {
					if (!hasSEConfigurePrivilege(session)) {
						throw new Exception("user not allowed to change motor configuration.");
					}
					Representation rep = request.getEntity();
					Form form = request.getResourceRef().getQueryAsForm();
					String saveMessage = form.getValues(QUERY_ENTRY_MESSAGE);
//					String versionId = form.getValues(QUERY_ENTRY_VERSION_ID);
					String text = rep.getText();
					try {
						text = URLDecoder.decode(text, "UTF-8");
					} catch (Exception e) {
						throw new Exception("model can not be empty.");
					}
//					if (versionId != null && versionId.length() > 0) {
//						saveTempConfigModel(versionId, text, saveMessage, session);
//					} else {
					saveDBConfigModel(text, saveMessage, session);
//					}
//					copyToRemote();
					response.setEntity(JSON_OK, MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
				} catch (Exception e) {
					logger.error("failed to do save", e);
					response.setEntity(makeErrorJSON("failed to save, " + e.getLocalizedMessage()), MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
					return;
				}
			} else if (SEG_NAME_DBLOAD.equalsIgnoreCase(seg)){
				try {
					Form form = request.getResourceRef().getQueryAsForm();
					String objectId = form.getValues(QUERY_ENTRY_VERSION_ID);
					String text = getSEDBGitService().readRevisionOfFile(dbName, objectId);
					YamlRestlet.saveRevisionToTempFile(objectId, text);
					Object model = YamlRestlet.loadConfigModelFromText(text);
					JSONObject jsonObject = (JSONObject) _convertToJson(model);
					response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
				} catch (Exception e) {
					logger.error("failed to load yaml.", e);
					e.printStackTrace();
//					response.setEntity("{'status':'ERROR','reason':'" + e.getMessage() + "'}", MediaType.APPLICATION_JSON);
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
					return;
				}
			} else if (SEG_NAME_CHANGENAME.equalsIgnoreCase(seg)){
				try {
					if (!hasSEConfigurePrivilege(session)) {
						throw new Exception("user not allowed to change motor configuration.");
					}
					Representation rep = request.getEntity();
					Form query = request.getResourceRef().getQueryAsForm();
					String saveMessage = query.getValues(QUERY_ENTRY_MESSAGE);
					Form form = new Form(rep);
					String oldDid = form.getValues(QUERY_ENTRY_OLDDID);
					String newDid = form.getValues(QUERY_ENTRY_NEWDID);
//					String versionId = form.getValues(QUERY_ENTRY_VERSION_ID);
//					String text = rep.getText();
//					try {
//						text = URLDecoder.decode(text, "UTF-8");
//					} catch (Exception e) {
//						throw new Exception("model can not be empty.");
//					}
//					if (versionId != null && versionId.length() > 0) {
//						saveTempConfigModel(versionId, text, saveMessage, session);
//					} else {
					changeDBDeviceName(oldDid, newDid, saveMessage, session);
//					}
//					copyToRemote();
					response.setEntity(JSON_OK, MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
				} catch (Exception e) {
					logger.error("failed to change device name", e);
					response.setEntity(makeErrorJSON("failed to change, " + e.getLocalizedMessage()), MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
					return;
				}
			} else if (SEG_NAME_DBREMOVE.equalsIgnoreCase(seg)){
				try {
					if (!hasSEConfigurePrivilege(session)) {
						throw new Exception("user not allowed to change configuration.");
					}
					Representation rep = request.getEntity();
					Form form = request.getResourceRef().getQueryAsForm();
					String did = form.getValues(QUERY_ENTRY_DEVICEID);
					String saveMessage = form.getValues(QUERY_ENTRY_MESSAGE);
//					if (versionId != null && versionId.length() > 0) {
//						saveTempConfigModel(versionId, text, saveMessage, session);
//					} else {
					deleteDBConfigModel(did, saveMessage, session);
//					}
//					copyToRemote();
					response.setEntity(JSON_OK, MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
				} catch (Exception e) {
					logger.error("failed to delete", e);
					response.setEntity(makeErrorJSON("failed to delete, " + e.getLocalizedMessage()), MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
					return;
				}
			} else if (SEG_NAME_CONFIGSAVE.equalsIgnoreCase(seg)){
				try {
					if (!hasSEConfigurePrivilege(session)) {
						throw new Exception("user not allowed to change motor configuration.");
					}
					Representation rep = request.getEntity();
					Form form = request.getResourceRef().getQueryAsForm();
					String instrumentId = form.getValues(QUERY_ENTRY_INSTRUMENT);
					String saveMessage = form.getValues(QUERY_ENTRY_MESSAGE);
//					String versionId = form.getValues(QUERY_ENTRY_VERSION_ID);
					String text = rep.getText();
					try {
						text = URLDecoder.decode(text, "UTF-8");
					} catch (Exception e) {
						throw new Exception("model can not be empty.");
					}
//					if (versionId != null && versionId.length() > 0) {
//						saveTempConfigModel(versionId, text, saveMessage, session);
//					} else {
					saveSEConfigModel(instrumentId, text, saveMessage, session);
//					}
//					copyToRemote();
					response.setEntity(JSON_OK, MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
				} catch (Exception e) {
					logger.error("failed to do save", e);
					response.setEntity(makeErrorJSON("failed to save, " + e.getLocalizedMessage()), MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
					return;
				}
			} else {
				JSONObject obj = new JSONObject();
				try {
					obj.put("status", "ERROR");
					obj.put("reason", "unimplemented");
				} catch (JSONException e) {
				}
				response.setEntity(obj.toString(), MediaType.APPLICATION_JSON);
				response.setStatus(Status.SUCCESS_OK);
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "<span style=\"color:red\">Error: invalid user session.</span>");
		}
	}

	public static void changeDBDeviceName(String oldDid, String newDid, String message, UserSessionObject session) 
			throws IOException, JSONException, GitException {
		String userName = session.getUserName().toUpperCase();
		String filePath = getSEDBPath();
		GitService git = getSEDBGitService();
//		saveModel(filePath, text, message, userName, git);
		if (oldDid == null || oldDid.trim().length() == 0 || newDid == null || newDid.trim().length() == 0) {
			throw new JSONException("device name can not be empty");
		}
		File file = new File(filePath);

		DumperOptions options = new DumperOptions();
	    options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
		Map<String, Object> model = null;
		InputStream input = new FileInputStream(file);
		try {
			model = yaml.loadAs(input, Map.class);
		} finally {
			input.close();
		}
		Map<String, Object> device = (Map<String, Object>) model.get(oldDid);
		if (device == null) {
			throw new JSONException("device with the old name not found");
		}
		model.remove(oldDid);
		model.put(newDid, device);
		FileWriter writer = new FileWriter(file);
		try {
			yaml.dump(model, writer);
		} catch (Exception e) {
			// TODO: handle exception
		}finally {
			writer.close();
		}
		
//		GitService git = getSEDBGitService();
		if (git != null) {
			git.applyChange();
			if (message == null || message.length() == 0) {
				message = userName + " updated " + newDid + ": " + formater.format(new Date());
			} else {
				message = userName + " updated " + newDid + ": " + message;
			}
			git.commit(message);
		}

	}

	public static void deleteDBConfigModel(String did, String message, UserSessionObject session) 
			throws IOException, JSONException, GitException {
		String userName = session.getUserName().toUpperCase();
		String filePath = getSEDBPath();
		GitService git = getSEDBGitService();
//		saveModel(filePath, text, message, userName, git);
		if (did == null || did.trim().length() == 0 ) {
			throw new JSONException("device name can not be empty");
		}
		File file = new File(filePath);

		DumperOptions options = new DumperOptions();
	    options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
		Map<String, Object> model = null;
		InputStream input = new FileInputStream(file);
		try {
			model = yaml.loadAs(input, Map.class);
		} finally {
			input.close();
		}
		Map<String, Object> device = (Map<String, Object>) model.get(did);
		if (device == null) {
			throw new JSONException("device with the name not found");
		}
		model.remove(did);
		FileWriter writer = new FileWriter(file);
		try {
			yaml.dump(model, writer);
		} catch (Exception e) {
			// TODO: handle exception
		}finally {
			writer.close();
		}
		
//		GitService git = getSEDBGitService();
		if (git != null) {
			git.applyChange();
			if (message == null || message.length() == 0) {
				message = userName + " deleted " + did + ": " + formater.format(new Date());
			} else {
				message = userName + " deleted " + did + ": " + message;
			}
			git.commit(message);
		}

	}
	
	public static void saveDBConfigModel(String text, String message, UserSessionObject session) 
			throws IOException, JSONException, GitException {
		String userName = session.getUserName().toUpperCase();
		String filePath = getSEDBPath();
		GitService git = getSEDBGitService();
		saveModel(filePath, text, message, userName, git);
	}
	
	public static void saveSEConfigModel(String instrumentId, String text, String message, UserSessionObject session) 
			throws IOException, JSONException, GitException {
		String userName = session.getUserName().toUpperCase();
		String filePath = getSEConfigFilename(instrumentId);
		GitService git = getSEConfigGitService(instrumentId);
		saveModel(filePath, text, message, userName, git);
	}
		
	public static void saveModel(String yamlFilePath, String text, String message, String userName, GitService git) 
			throws IOException, JSONException, GitException {
		if (text == null || text.trim().length() == 0) {
			throw new JSONException("model can not be empty");
		}
		File file = new File(yamlFilePath);
		String[] pair = text.split("&", 2);
		String did = pair[0].substring(pair[0].indexOf("=") + 1).trim();
//		String idx = pair[1].substring(pair[1].indexOf("=") + 1).trim();
		String modelString = pair[1].substring(pair[1].indexOf("=") + 1).trim();
		JSONObject json = new LinkedJSONObject(modelString);
//		System.out.println(json.get("path"));
//		System.out.println(json.get("model"));
	    DumperOptions options = new DumperOptions();
	    options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
		Map<String, Object> model = null;
		InputStream input = new FileInputStream(file);
		try {
			model = yaml.loadAs(input, Map.class);
		} finally {
			input.close();
		}
//		String[] pathSeg = path.split("/");
//		Map<String, Object> device = (Map<String, Object>) model.get("GALIL_CONTROLLERS");
		Map<String, Object> device = (Map<String, Object>) model.get(did);
//		for (String seg : pathSeg) {
//			if (seg.length() > 0) {
//				device = (Map<String, Object>) device.get(seg);
//			}
//		}
		
//		Object item = device.get("sics_motor");
//		int index = Integer.valueOf(idx);
//		item = ((ArrayList<Object>) item).get(index);
//		Object item = device.get(idx);
		if (device == null) {
			Map<String, Object> map = YamlRestlet.toMap(json);
			model.put(did, map);
		} else {
//			updateModel(json, (Map<String, Object>) device);
			Map<String, Object> map = YamlRestlet.toMap(json);
			model.put(did, map);
		}
		FileWriter writer = new FileWriter(file);
		try {
			yaml.dump(model, writer);
		} catch (Exception e) {
			// TODO: handle exception
		}finally {
			writer.close();
		}
		
//		GitService git = getSEDBGitService();
		if (git != null) {
			git.applyChange();
			if (message == null || message.length() == 0) {
				message = userName + " updated " + did + ": " + formater.format(new Date());
			} else {
				message = userName + " updated " + did + ": " + message;
			}
			git.commit(message);
		}
	}

	private static void updateModel(JSONObject json, Map<String, Object> model) throws JSONException {
		for (int i = 0; i < json.names().length(); i++) {
			String key = json.names().getString(i);
			Object val = json.get(key);
			if (val instanceof JSONObject) {
//				Object sub = model.get(key);
//				if (sub == null || !(sub instanceof Map)) {
				Map<String, Object> sub = new LinkedHashMap<String, Object>();
//				} else {
//					((Map<String, Object>) sub).clear();
//				}
				updateModel((JSONObject) val, (Map<String, Object>) sub);
				model.put(key, sub);
			} else if (val instanceof JSONArray) {
				JSONArray array = (JSONArray) val;
				String[] vals = new String[array.length()];
				for (int idx = 0; idx < vals.length; idx ++) {
					vals[idx] = array.getString(idx);
				}
				model.put(key, vals);
			} else {
				Object sub = model.get(key);
				if (sub instanceof Double) {
					try {
						val = Double.valueOf(val.toString());
					} catch (Exception e) {
						val = String.valueOf(val);
					}
				} else if (sub instanceof Integer) {
					try {
						val = Integer.valueOf(val.toString());
					} catch (Exception e) {
						val = String.valueOf(val);
					}
				} else if (sub instanceof String) {
					val = String.valueOf(val);
				} else if (sub instanceof LinkedHashSet) {
					continue;
				} else if (sub instanceof JSONArray) {
					JSONArray array = (JSONArray) sub;
					String[] vals = new String[array.length()];
					for (int idx = 0; idx < vals.length; idx ++) {
						vals[idx] = array.getString(idx);
					}
					sub = vals;
				}
				if (sub == null) {
					try {
						val = Float.valueOf(val.toString());
					} catch (Exception e) {
						val = String.valueOf(val);
					}
				}
				if (sub == null || !sub.equals(val)) {
					model.put(key, val);
				}
			}
		}
	}
	
	public static Object loadSEDB() throws IOException {
	    DumperOptions options = new DumperOptions();
	    options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
		InputStream input = new FileInputStream(new File(getSEDBPath()));
		try {
			Object data = yaml.load(input);
			return data;
		} finally {
			input.close();
		}
	}

	public static Object loadSEConfig(String instrumentId) throws IOException {
	    DumperOptions options = new DumperOptions();
	    options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
		InputStream input = new FileInputStream(new File(getSEConfigFilename(instrumentId)));
		try {
			Object data = yaml.load(input);
			return data;
		} finally {
			input.close();
		}
	}

	private static String getSEDBPath() {
		return dbPath + "/" + dbName;
	}

	private static String getSEConfigPath(String instrumentId) {
		return configPath + "/" + instrumentId;
	}

	private static String getSEConfigFilename(String instrumentId) {
		return configPath + "/" + instrumentId + "/" + configName;
	}
	
	private synchronized static GitService getSEDBGitService() {
		if (gitServiceMap.containsKey(SEG_NAME_SEDB)) {
			return gitServiceMap.get(SEG_NAME_SEDB);
		} else {
			GitService git = new GitService(dbPath);
			gitServiceMap.put(SEG_NAME_SEDB, git);
			return git;
		}
	}
	
	private synchronized static GitService getSEConfigGitService(String instrumentId) {
		if (gitServiceMap.containsKey(instrumentId)) {
			return gitServiceMap.get(instrumentId);
		} else {
			GitService git = new GitService(getSEConfigPath(instrumentId));
			gitServiceMap.put(instrumentId, git);
			return git;
		}
	}
	
	private static String makeErrorJSON(String errorMessage) {
		JSONObject json = new JSONObject();
		try {
			json.append("status", "ERROR");
			json.append("reason", errorMessage);
		} catch (JSONException e) {
		}
		return json.toString();
	}
	/* (non-Javadoc)
	 * @see org.gumtree.core.object.IDisposable#disposeObject()
	 */
	@Override
	public void disposeObject() {
		// TODO Auto-generated method stub

	}

	private static Object _convertToJson(Object o) throws JSONException {
		if (o instanceof Map) {
			Map<Object, Object> map = (Map<Object, Object>) o;

			JSONObject result = new JSONObject(new LinkedHashMap<String, Object>());

			for (Map.Entry<Object, Object> stringObjectEntry : map.entrySet()) {
				String key = stringObjectEntry.getKey().toString();

				result.put(key, _convertToJson(stringObjectEntry.getValue()));
			}

			return result;
		} else if (o instanceof ArrayList) {
			ArrayList arrayList = (ArrayList) o;
			JSONArray result = new JSONArray();

			for (Object arrayObject : arrayList) {
				result.put(_convertToJson(arrayObject));
			}

			return result;
		} else if (o instanceof String) {
			return o;
		} else if (o instanceof Boolean) {
			return o;
		} else if (o instanceof Double) {
			return o;
		} else if (o instanceof Integer) {
			return o;
		} else if (o instanceof LinkedHashSet) {
			LinkedHashSet set = (LinkedHashSet) o;
			JSONArray result = new JSONArray();

			for (Object obj : set) {
				result.put(_convertToJson(obj));
			}

			return result;
		} else if (o == null) {
			return "null";
		} else {
			throw new HttpClientError("can not convert to json");
		}
	}
	
}
