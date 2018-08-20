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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import au.gov.ansto.bragg.nbi.server.git.GitService;
import au.gov.ansto.bragg.nbi.server.internal.AbstractUserControlRestlet;
import au.gov.ansto.bragg.nbi.server.internal.UserSessionService;
import au.gov.ansto.bragg.nbi.server.login.UserSessionObject;

/**
 * @author nxi
 *
 */
public class YamlRestlet extends AbstractUserControlRestlet implements IDisposable {

	private static final String QUERY_ENTRY_INSTRUMENT = "inst";
	private static final String QUERY_ENTRY_MESSAGE = "msg";
	
	private static final String SEG_NAME_MODEL = "model";
	private static final String SEG_NAME_SAVE = "save";
	
	private static final String PROPERTY_SICS_YAML_PATH = "gumtree.sics.yamlPath";
	
	private static String configPath;
	private static GitService gitService;
	private static SimpleDateFormat formater;
	private static String JSON_OK;

	public YamlRestlet(){
		this(null);
	}
	/**
	 * @param context
	 */
	public YamlRestlet(Context context) {
		super(context);
		configPath = System.getProperty(PROPERTY_SICS_YAML_PATH);
		if (configPath != null) {
			gitService = new GitService(configPath);
		}
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
			if (SEG_NAME_MODEL.equalsIgnoreCase(seg)){
				try {
					Form form = request.getResourceRef().getQueryAsForm();
					String instrumentId = form.getValues(QUERY_ENTRY_INSTRUMENT);
					if (instrumentId == null) {
						throw new Exception("need instrument name");
					}
//					response.setEntity("Done", MediaType.TEXT_PLAIN);
					Object model = loadConfigModel(instrumentId);
					JSONObject jsonObject = (JSONObject) _convertToJson(model);
//					JSONObject jsonObject = new JSONObject(model.toString());
					response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
				} catch (Exception e) {
					e.printStackTrace();
//					response.setEntity("{'status':'ERROR','reason':'" + e.getMessage() + "'}", MediaType.APPLICATION_JSON);
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
					return;
				}
			}  else if (SEG_NAME_SAVE.equalsIgnoreCase(seg)){
				try {
					Representation rep = request.getEntity();
					Form form = request.getResourceRef().getQueryAsForm();
					String instrumentId = form.getValues(QUERY_ENTRY_INSTRUMENT);
					String saveMessage = form.getValues(QUERY_ENTRY_MESSAGE);
					if (instrumentId == null) {
						throw new Exception("need instrument name");
					}
	//				response.setEntity("Done", MediaType.TEXT_PLAIN);
					String text = rep.getText();
					try {
						text = URLDecoder.decode(text, "UTF-8");
					} catch (Exception e) {
						throw new Exception("model can not be empty.");
					}
					saveConfigModel(instrumentId, text, saveMessage);
					response.setEntity(JSON_OK, MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
				} catch (Exception e) {
					e.printStackTrace();
					response.setEntity(makeErrorJSON(e.getMessage()), MediaType.APPLICATION_JSON);
//					response.setEntity("{'status':'ERROR','reason':'" + e.getMessage() + "'}", MediaType.APPLICATION_JSON);
//					response.setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, e);
					return;
				}
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "<span style=\"color:red\">Error: invalid user session.</span>");
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

	public static void saveConfigModel(String instrumentId, String text, String message) throws IOException, JSONException {
		if (text == null || text.trim().length() == 0) {
			throw new JSONException("model can not be empty");
		}
		File file = new File(configPath + "/" + instrumentId + "/" + instrumentId + ".yaml");
		String[] pair = text.split("&", 2);
		String path = pair[0].substring(pair[0].indexOf("=") + 1).trim();
		String modelString = pair[1].substring(pair[1].indexOf("=") + 1).trim();
		JSONObject json = new JSONObject(modelString);
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
		String[] pathSeg = path.split("/");
		Map<String, Object> device = (Map<String, Object>) model.get("GALIL_CONTROLLERS");
		for (String seg : pathSeg) {
			if (seg.length() > 0) {
				device = (Map<String, Object>) device.get(seg);
			}
		}
		updateMode(json, device);
		FileWriter writer = new FileWriter(file);
		try {
			yaml.dump(model, writer);
		} catch (Exception e) {
			// TODO: handle exception
		}finally {
			writer.close();
		}
		
		if (gitService != null) {
			try {
				gitService.applyChange();
				if (message == null || message.length() == 0) {
					message = instrumentId + ":" + formater.format(new Date());
				} else {
					message = instrumentId + ":" + message + ":" + formater.format(new Date());
				}
				gitService.commit(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void updateMode(JSONObject json, Map<String, Object> model) throws JSONException {
		for (int i = 0; i < json.names().length(); i++) {
			String key = json.names().getString(i);
			Object val = json.get(key);
			if (val instanceof JSONObject) {
				Object sub = model.get(key);
				updateMode((JSONObject) val, (Map<String, Object>) sub);
			} else {
				Object sub = model.get(key);
				if (sub instanceof Double) {
					val = Double.valueOf(val.toString());
				} else if (sub instanceof Integer) {
					val = Integer.valueOf(val.toString());
				} else if (sub instanceof String) {
					val = String.valueOf(val);
				} else if (sub instanceof LinkedHashSet) {
					continue;
				}
				if (!sub.equals(val)) {
					model.put(key, val);
				}
			}
		}
	}
	
	public static Object loadConfigModel(String instrumentId) throws IOException {
	    DumperOptions options = new DumperOptions();
	    options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
		InputStream input = new FileInputStream(new File(configPath + "/" + instrumentId + "/" + instrumentId + ".yaml"));
		try {
			Object data = yaml.load(input);
			return data;
		} finally {
			input.close();
		}
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
