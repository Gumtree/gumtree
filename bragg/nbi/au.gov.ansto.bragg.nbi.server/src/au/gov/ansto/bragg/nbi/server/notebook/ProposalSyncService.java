package au.gov.ansto.bragg.nbi.server.notebook;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.gumtree.service.db.ControlDB;
import org.gumtree.service.db.ProposalDB;
import org.gumtree.service.db.RecordsFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProposalSyncService {

	private static final String PROP_PROPOSALSYNC_ENABLED = "gumtree.proposalSync.enabled";
	private static final String PROP_PROPOSALSYNC_TIME = "gumtree.proposalSync.timeInHour";
	private static final String PROP_INSTRUMENT_ID = "gumtree.instrument.id";
	private static final String HOST = "http://tideland.nbi.ansto.gov.au";
	private static final String PATH = "/shim/proposal.php?instrument_name=";
	private static Logger logger = LoggerFactory.getLogger(ProposalSyncService.class);
	private boolean proposalSyncEnabled = false;
	private int syncHour;
	private HttpClient client;
	
	public ProposalSyncService() {
		try {
			proposalSyncEnabled = Boolean.valueOf(System.getProperty(PROP_PROPOSALSYNC_ENABLED));
			if (proposalSyncEnabled) {
				syncHour = Integer.valueOf(System.getProperty(PROP_PROPOSALSYNC_TIME));
			}
		} catch (Exception e) {
		}
		if (proposalSyncEnabled) {
			Calendar timeOfDay = Calendar.getInstance();
			timeOfDay.set(Calendar.HOUR_OF_DAY, syncHour);
			timeOfDay.set(Calendar.MINUTE, 0);
			timeOfDay.set(Calendar.SECOND, 0);

			new DailyRunnerDaemon(timeOfDay, new Runnable() {
				@Override
				public void run()
				{
					try {
						checkProposalFromServer();
					} catch(Exception e) {
						logger.error("An error occurred performing proposal sync", e);
					}
				}
			}, "Sync proposal ID").start();
			try {
//				checkProposalFromServer();
			} catch(Exception e) {
				logger.error("An error occurred performing proposal sync", e);
				e.printStackTrace();
			}
		}
	}

	public void checkProposalFromServer() throws HttpException, IOException, RecordsFileException{
		String newProposalId = getProposalId();
		if (newProposalId != null) {
			String oldSession = "";
			String oldProposal = "";
			try {
				oldSession = ControlDB.getInstance().getCurrentSessionId();
//				oldName = SessionDB.getInstance().getSessionValue(oldSession);
				oldProposal = ProposalDB.getInstance().findProposalId(oldSession);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!newProposalId.equals(oldProposal)) {
				NotebookRestlet.switchToNewProposal(newProposalId);
			}
		}
	}
	
	public String getProposalId() throws HttpException, IOException {
		String instrumentName = System.getProperty(PROP_INSTRUMENT_ID);
		GetMethod getMethod = new GetMethod(HOST);
		getMethod.setDoAuthentication(true);
		getMethod.setPath(PATH + instrumentName);
		String resp = null;
		try {
			int statusCode = getClient().executeMethod(getMethod);
			if (statusCode != HttpStatus.SC_OK) {
				return null;
			} else {
				resp = getMethod.getResponseBodyAsString();
			}			
		} finally {
			getMethod.releaseConnection();
		}
		if (resp != null) {
			Pattern pattern = Pattern.compile("PROPOSAL_ID: \\d+");
			Matcher m = pattern.matcher(resp);
			if (m.find()) {
			    String s = m.group(0);
			    pattern = Pattern.compile("\\d+");
			    m = pattern.matcher(s);
			    if (m.find()) {
			    	return m.group(0);
			    }
			}
		} 
		return null;
	}
	
	private synchronized HttpClient getClient()  {
		if (client == null) {
			client = new HttpClient();
		}
		return client;
	}
	
	class DailyRunnerDaemon	{
		private final Runnable dailyTask;
		private final int hour;
		private final int minute;
		private final int second;
		private final String runThreadName;

		public DailyRunnerDaemon(Calendar timeOfDay, Runnable dailyTask, String runThreadName)
		{
			this.dailyTask = dailyTask;
			this.hour = timeOfDay.get(Calendar.HOUR_OF_DAY);
			this.minute = timeOfDay.get(Calendar.MINUTE);
			this.second = timeOfDay.get(Calendar.SECOND);
			this.runThreadName = runThreadName;
		}

		public void start() {
			startTimer();
		}

		private void startTimer() {
			new Timer(runThreadName, true).schedule(new TimerTask() {
				@Override
				public void run()
				{
					dailyTask.run();
					startTimer();
				}
			}, getNextRunTime());
		}


		private Date getNextRunTime()
		{
			Calendar startTime = Calendar.getInstance();
			Calendar now = Calendar.getInstance();
			startTime.set(Calendar.HOUR_OF_DAY, hour);
			startTime.set(Calendar.MINUTE, minute);
			startTime.set(Calendar.SECOND, second);
			startTime.set(Calendar.MILLISECOND, 0);

			if(startTime.before(now) || startTime.equals(now))
			{
				startTime.add(Calendar.DATE, 1);
			}

			return startTime.getTime();
		}
	}
}
