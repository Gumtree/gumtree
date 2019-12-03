/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClientError;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.JschSession;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.gumtree.core.object.IDisposable;
import org.gumtree.security.EncryptionUtils;
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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import au.gov.ansto.bragg.nbi.server.git.GitException;
import au.gov.ansto.bragg.nbi.server.git.GitService;
import au.gov.ansto.bragg.nbi.server.git.GitService.GitCommit;
import au.gov.ansto.bragg.nbi.server.internal.AbstractUserControlRestlet;
import au.gov.ansto.bragg.nbi.server.internal.UserSessionService;
import au.gov.ansto.bragg.nbi.server.login.UserSessionObject;

/**
 * @author nxi
 *
 */
public class YamlRestlet extends AbstractUserControlRestlet implements IDisposable {

	private static final String QUERY_ENTRY_INSTRUMENT = "inst";
	private static final String QUERY_ENTRY_PATH = "path";
	private static final String QUERY_ENTRY_MESSAGE = "msg";
	private static final String QUERY_ENTRY_VERSION_ID = "version";
	private static final String QUERY_ENTRY_TIMESTAMP = "timestamp";
	
	private static final String SEG_NAME_MODEL = "model";
	private static final String SEG_NAME_SAVE = "save";
	private static final String SEG_NAME_LOAD = "load";
	private static final String SEG_NAME_HISTORY = "history";
	private static final String SEG_NAME_APPLY = "apply";
	
	private static final String PROPERTY_SERVER_TEMP_PATH = "gumtree.server.temp";
	private static final String PROPERTY_SERVER_YAML_PATH = "gumtree.server.yamlPath";
	private static final String PROPERTY_YAML_FOLDER = "yaml";
	private static final String PROPERTY_YAML_FILENAME = "gumtree.sics.yamlfile";
	private static final String PROPERTY_YAML_REMOTEPATH = "gumtree.sics.yamlPath";
	private static final String PROPERTY_SERVER_SHARE = "gumtree.server.sharePath";
	
	private static final String PROPERTY_SSH_USER = "gumtree.ssh.username";
	private static final String PROPERTY_SSH_HOST = "gumtree.ssh.host";
	private static final String PROPERTY_SSH_KEYPATH = "gumtree.ssh.keypath";
	private static final String PROPERTY_SSH_PASSPHRASE = "gumtree.ssh.passphrase";
	
	private static final Logger logger = LoggerFactory.getLogger(YamlRestlet.class);
	
	private static String configPath;
	private static String tempPath;
	private static Map<String, GitService> gitServiceMap;
	private static SimpleDateFormat formater;
	private static String JSON_OK;
	private static String user;
	private static String host;
	private static String keyPath;
	private static String passphrase;
	private static String remotePath;
	private static String localPath;
	private static String yamlName;
	

	public YamlRestlet(){
		this(null);
	}
	/**
	 * @param context
	 */
	public YamlRestlet(Context context) {
		super(context);
		configPath = System.getProperty(PROPERTY_SERVER_YAML_PATH);
		tempPath = System.getProperty(PROPERTY_SERVER_TEMP_PATH);
		user = System.getProperty(PROPERTY_SSH_USER);
		host = System.getProperty(PROPERTY_SSH_HOST);
		keyPath = System.getProperty(PROPERTY_SSH_KEYPATH);
		try {
			passphrase = EncryptionUtils.decryptBase64(System.getProperty(PROPERTY_SSH_PASSPHRASE));
			System.err.println(passphrase);
		} catch (Exception e1) {
		}
		remotePath = System.getProperty(PROPERTY_YAML_REMOTEPATH);
		yamlName = System.getProperty(PROPERTY_YAML_FILENAME);
		localPath = System.getProperty(PROPERTY_SERVER_SHARE);
		gitServiceMap = new HashMap<String, GitService>();
//		if (configPath != null) {
//			gitService = new GitService(configPath);
//		}
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

	private synchronized static GitService getGitService(String instrumentId) {
		if (gitServiceMap.containsKey(instrumentId)) {
			return gitServiceMap.get(instrumentId);
		} else {
			GitService git = new GitService(configPath + "/" + instrumentId);
			gitServiceMap.put(instrumentId, git);
			return git;
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
					copyFromRemote(instrumentId, session);
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
			}  else if (SEG_NAME_HISTORY.equalsIgnoreCase(seg)){
				try {
					Form form = request.getResourceRef().getQueryAsForm();
					String instrumentId = form.getValues(QUERY_ENTRY_INSTRUMENT);
					String path = form.getValues(QUERY_ENTRY_PATH);
					List<GitCommit> commits = getGitService(instrumentId).getCommits(PROPERTY_YAML_FOLDER + '/' + yamlName);
//					JSONObject jsonObject = new JSONObject(new LinkedHashMap<String, Object>());
					JSONArray jsonArray = new JSONArray();
					int i = 0;
					for (GitCommit commit : commits) {
						if (path != null && path.trim().length() > 0) {
							String message = commit.getMessage();
							if (!message.contains(path)) {
								continue;
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
						i ++;
					}
//					JSONObject jsonObject = new JSONObject(model.toString());
					response.setEntity(jsonArray.toString(), MediaType.TEXT_PLAIN);
					response.setStatus(Status.SUCCESS_OK);
				} catch (Exception e) {
					e.printStackTrace();
//					response.setEntity("{'status':'ERROR','reason':'" + e.getMessage() + "'}", MediaType.APPLICATION_JSON);
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
					return;
				}
			}  else if (SEG_NAME_LOAD.equalsIgnoreCase(seg)){
				try {
					Form form = request.getResourceRef().getQueryAsForm();
					String instrumentId = form.getValues(QUERY_ENTRY_INSTRUMENT);
					String objectId = form.getValues(QUERY_ENTRY_VERSION_ID);
					String text = getGitService(instrumentId).readRevisionOfFile(PROPERTY_YAML_FOLDER + "/" + yamlName, objectId);
					saveRevisionToTempFile(objectId, text);
					Object model = loadConfigModelFromText(text);
					JSONObject jsonObject = (JSONObject) _convertToJson(model);
					response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
				} catch (Exception e) {
					e.printStackTrace();
//					response.setEntity("{'status':'ERROR','reason':'" + e.getMessage() + "'}", MediaType.APPLICATION_JSON);
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
					return;
				}
			} else if (SEG_NAME_APPLY.equalsIgnoreCase(seg)){
				try {
					Form form = request.getResourceRef().getQueryAsForm();
					String instrumentId = form.getValues(QUERY_ENTRY_INSTRUMENT);
					String versionId = form.getValues(QUERY_ENTRY_VERSION_ID);
					String ts = form.getValues(QUERY_ENTRY_TIMESTAMP);
					if (instrumentId == null) {
						throw new Exception("need instrument name");
					}
					if (versionId == null) {
						throw new Exception("need version id");
					}
					applyHistoryVersion(instrumentId, versionId, ts, session);
					response.setEntity(JSON_OK, MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
				} catch (Exception e) {
					e.printStackTrace();
					response.setEntity(makeErrorJSON(e.getMessage()), MediaType.APPLICATION_JSON);
					return;
				}
			} else if (SEG_NAME_SAVE.equalsIgnoreCase(seg)){
				try {
					Representation rep = request.getEntity();
					Form form = request.getResourceRef().getQueryAsForm();
					String instrumentId = form.getValues(QUERY_ENTRY_INSTRUMENT);
					String saveMessage = form.getValues(QUERY_ENTRY_MESSAGE);
					String versionId = form.getValues(QUERY_ENTRY_VERSION_ID);
					if (instrumentId == null) {
						throw new Exception("need instrument name");
					}
					String text = rep.getText();
					try {
						text = URLDecoder.decode(text, "UTF-8");
					} catch (Exception e) {
						throw new Exception("model can not be empty.");
					}
					if (versionId != null && versionId.length() > 0) {
						saveTempConfigModel(instrumentId, versionId, text, saveMessage, session);
					} else {
						saveConfigModel(instrumentId, text, saveMessage, session);
					}
					copyToRemote(instrumentId);
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

	public static void saveConfigModel(String instrumentId, String text, String message, UserSessionObject session) 
			throws IOException, JSONException, GitException {
		if (text == null || text.trim().length() == 0) {
			throw new JSONException("model can not be empty");
		}
		File file = new File(getYamlFilePath(instrumentId));
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
		
		GitService git = getGitService(instrumentId);
		if (git != null) {
			git.applyChange();
			if (message == null || message.length() == 0) {
				message = session.getUserName().toUpperCase() + " updated " + path + ": " + formater.format(new Date());
			} else {
				message = session.getUserName().toUpperCase() + " updated " + path + ": " + message;
			}
			git.commit(message);
		}
	}

	public static void saveTempConfigModel(String instrumentId, String versionId, String text, String message, UserSessionObject session) 
			throws IOException, JSONException {
		if (text == null || text.trim().length() == 0) {
			throw new JSONException("model can not be empty");
		}
		File iFile = new File(tempPath + "/" + versionId);
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
		InputStream input = new FileInputStream(iFile);
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
		File oFile = new File(getYamlFilePath(instrumentId));
		FileWriter writer = new FileWriter(oFile);
		try {
			yaml.dump(model, writer);
		} catch (Exception e) {
			// TODO: handle exception
		}finally {
			writer.close();
		}
		
		GitService git = getGitService(instrumentId);
		if (git != null) {
			try {
				git.applyChange();
				if (message == null || message.length() == 0) {
					message = session.getUserName().toUpperCase() + " updated " + path + ": " + formater.format(new Date());
				} else {
					message = session.getUserName().toUpperCase() + " updated " + path + ": " + message;
				}
				git.commit(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void applyHistoryVersion(String instrumentId, String versionId, String timestamp, UserSessionObject session) 
			throws IOException, JSONException {
//		File iFile = new File(tempPath + "/" + versionId);
		
//		File oFile = new File(getYamlFilePath(instrumentId));
		
		Files.copy(Paths.get(tempPath + "/" + versionId), 
					Paths.get(getYamlFilePath(instrumentId)),
					StandardCopyOption.REPLACE_EXISTING);
		
		GitService git = getGitService(instrumentId);
		if (git != null) {
			try {
				git.applyChange();
				String message = session.getUserName().toUpperCase() + " reversed version to" + ": " + timestamp;
				git.commit(message);
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
	
	private static String getYamlFilePath(String instrumentId) {
		return configPath + "/" + instrumentId + "/" + PROPERTY_YAML_FOLDER + "/" + yamlName;
	}

	private static String getRemoteYamelPath(String instrumentId) {
		return (remotePath + "/" + yamlName).replace("$INSTRUMENT", instrumentId);
	}

	private static String getTempFilePath(String instrumentId) {
//		return configPath + "/temp/" + instrumentId + "/" + yamlName;
		return localPath + "/" + instrumentId + "/" + yamlName;
	}

	private static String getHostName(String instrumentId) {
		return host.replace("$INSTRUMENT", instrumentId);
	}
	
	public static void copyFromRemote(String instrumentId, UserSessionObject session) {
		FileOutputStream fos=null;
		try{

			String rfile = getRemoteYamelPath(instrumentId); 
			String lfile = getTempFilePath(instrumentId);

//			String prefix = null;
//			if(new File(lfile).isDirectory()){
//				prefix = lfile + File.separator;
//			}

			logger.debug(keyPath);
			logger.debug(passphrase);
			logger.debug(getHostName(instrumentId));
			logger.debug(user);
			JSch jsch = new JSch();
			jsch.addIdentity(keyPath, passphrase);
//			System.err.println(EncryptionUtils.encryptBase64(passphrase));
			Session jschSession = jsch.getSession(user, getHostName(instrumentId), 22);
			jschSession.setConfig("StrictHostKeyChecking", "no"); 

			// username and password will be given via UserInfo interface.
//			UserInfo ui=new MyUserInfo();
//			session.setUserInfo(ui);
			jschSession.connect();

			// exec 'scp -f rfile' remotely
//			rfile=rfile.replace("'", "'\"'\"'");
//			rfile="'"+rfile+"'";
			String command = "scp -f " + rfile;
			Channel channel = jschSession.openChannel("exec");
			((ChannelExec)channel).setCommand(command);

			// get I/O streams for remote scp
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			byte[] buf=new byte[1024];

			// send '\0'
			buf[0] = 0; 
			out.write(buf, 0, 1); 
			out.flush();

			while(true){
				int c = checkAck(in);
				if (c!='C') {
					break;
				}

				// read '0644 '
				in.read(buf, 0, 5);

				long filesize=0L;
				while(true){
					if(in.read(buf, 0, 1)<0){
						// error
						break; 
					}
					if(buf[0]==' ')break;
					filesize=filesize*10L+(long)(buf[0]-'0');
				}

				String file = null;
				for(int i=0;;i++){
					in.read(buf, i, 1);
					if(buf[i]==(byte)0x0a){
						file = new String(buf, 0, i);
						break;
					}
				}

				//System.out.println("filesize="+filesize+", file="+file);

				// send '\0'
				buf[0]=0; out.write(buf, 0, 1); out.flush();

				// read a content of lfile
				fos = new FileOutputStream(lfile);
				int foo;
				while(true){
					if(buf.length<filesize) foo=buf.length;
					else foo=(int)filesize;
					foo=in.read(buf, 0, foo);
					if(foo<0){
						// error 
						break;
					}
					fos.write(buf, 0, foo);
					filesize-=foo;
					if(filesize==0L) break;
				}
				fos.close();
				fos=null;

				if(checkAck(in)!=0){
					break;
				}

				// send '\0'
				buf[0]=0; out.write(buf, 0, 1); out.flush();
			}

			jschSession.disconnect();
			
			File file1 = new File(getYamlFilePath(instrumentId));
			File file2 = new File(getTempFilePath(instrumentId));
			boolean isIdentical = FileUtils.contentEquals(file1, file2);
			logger.debug("isIdentical=" + isIdentical);
			if (!isIdentical) {
				FileUtils.copyFile(file2, file1);
				GitService git = getGitService(instrumentId);
				if (git != null) {
					try {
						git.applyChange();
						String message = session.getUserName().toUpperCase() + " fetch remote version" + ": " + formater.format(new Date());
						git.commit(message);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());
			try{
				if (fos!=null) {
					fos.close();
				}
			} catch(Exception ee){
			}
		}
	}
	
	public static void copyToRemote(String instrumentId) throws Exception {
		FileInputStream fis=null;
		try{

			String rfile = getRemoteYamelPath(instrumentId); 
			String lfile = getYamlFilePath(instrumentId);

			logger.debug(rfile);
			logger.debug(lfile);
			
//			String prefix = null;
//			if(new File(lfile).isDirectory()){
//				prefix = lfile + File.separator;
//			}

			JSch jsch = new JSch();
			jsch.addIdentity(keyPath, passphrase);
			Session jschSession = jsch.getSession(user, getHostName(instrumentId), 22);
			jschSession.setConfig("StrictHostKeyChecking", "no"); 

			// username and password will be given via UserInfo interface.
//			UserInfo ui=new MyUserInfo();
//			session.setUserInfo(ui);
			jschSession.connect();

			// exec 'scp -f rfile' remotely
			String command="scp -t " + rfile;
			Channel channel = jschSession.openChannel("exec");
			((ChannelExec)channel).setCommand(command);

			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			if(checkAck(in)!=0){
				throw new IOException("failed to read yaml file");
			}

			File _lfile = new File(lfile);

			// send "C0644 filesize filename", where filename should not include '/'
			long filesize = _lfile.length();
			command = "C0644 " + filesize + " ";
			if(lfile.lastIndexOf('/') > 0) {
				command += lfile.substring(lfile.lastIndexOf('/') + 1);
			}
			else{
				command += lfile;
			}
			command += "\n";
			out.write(command.getBytes()); 
			out.flush();
			if(checkAck(in) != 0){
				throw new IOException("failed to read file size");
			}

			// send a content of lfile
			fis = new FileInputStream(lfile);
			byte[] buf = new byte[1024];
			while(true){
				int len = fis.read(buf, 0, buf.length);
				if(len <= 0) {
					break;
				}
				out.write(buf, 0, len); //out.flush();
			}
			fis.close();
			fis=null;
			// send '\0'
			buf[0] = 0; 
			out.write(buf, 0, 1); 
			out.flush();
			
			if(checkAck(in)!=0){
				throw new IOException("failed to send file to SICS");
			}
			out.close();

			channel.disconnect();
			jschSession.disconnect();
			
		} catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());
			try{
				if (fis!=null) {
					fis.close();
				}
			} catch(Exception ee){
				
			}
			throw e;
		}
	}
	
	private static int checkAck(InputStream in) throws IOException{
		int b=in.read();
		// b may be 0 for success,
		//          1 for error,
		//          2 for fatal error,
		//          -1
		if(b==0) return b;
		if(b==-1) return b;

		if(b==1 || b==2){
			StringBuffer sb=new StringBuffer();
			int c;
			do {
				c=in.read();
				sb.append((char)c);
			}
			while(c!='\n');
//				if(b==1){ // error
//					System.out.print(sb.toString());
//				}
//				if(b==2){ // fatal error
//					System.out.print(sb.toString());
//				}
		}
		return b;
	}

	public static Object loadConfigModel(String instrumentId) throws IOException {
	    DumperOptions options = new DumperOptions();
	    options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
		InputStream input = new FileInputStream(new File(getYamlFilePath(instrumentId)));
		try {
			Object data = yaml.load(input);
			return data;
		} finally {
			input.close();
		}
	}
	
	public static Object loadConfigModelFromText(String text) throws IOException {
	    DumperOptions options = new DumperOptions();
	    options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
		try {
			Object data = yaml.load(text);
			return data;
		} finally {
		}
	}
	
	public static void saveRevisionToTempFile(String id, String text) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(tempPath + "/" + id, "UTF-8");
		writer.write(text);
		writer.close();
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
