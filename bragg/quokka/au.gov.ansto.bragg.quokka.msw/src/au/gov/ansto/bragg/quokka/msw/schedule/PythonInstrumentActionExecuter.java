package au.gov.ansto.bragg.quokka.msw.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;

import org.gumtree.msw.RefId;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.schedule.execution.AcquisitionSummary;
import org.gumtree.msw.schedule.execution.InitializationSummary;
import org.gumtree.msw.schedule.execution.ParameterChangeSummary;
import org.gumtree.msw.schedule.execution.Summary;

import au.gov.ansto.bragg.quokka.msw.ExperimentDescription;
import au.gov.ansto.bragg.quokka.msw.ModelProvider;
import au.gov.ansto.bragg.quokka.msw.User;

public class PythonInstrumentActionExecuter implements IInstrumentExecuter {
	// finals
	private static final String ID_TAG = "%%ID%%";
	private static final String PY_IMPORT_MSW = "import bragg.quokka.msw as msw";
	private static final String PY_CONTEXT = "msw.setContext(%%ID%%)";
	private static final String PY_INITIATE = "msw.initiate(%%ID%%)";
	private static final String PY_CLEAN_UP = "msw.cleanUp(%%ID%%)";
	private static final String PY_SET_PARAMETERS_SCRIPT = "msw.setParameters(%%ID%%)";
	private static final String PY_PRE_ACQUISITION_SCRIPT = "msw.preAcquisition(%%ID%%)";
	private static final String PY_DO_ACQUISITION_SCRIPT = "msw.doAcquisition(%%ID%%)";
	private static final String PY_POST_ACQUISITION_SCRIPT = "msw.postAcquisition(%%ID%%)";
	private static final String PY_CUSTOM_ACTION_SCRIPT = "msw.customAction(%%ID%%)";
	// java/python interface
	private static final Map<Long, Object> idMap = new HashMap<>();
	private static long objectId = 0;
	
	// fields
	private final ScriptEngine engine;
	private final ModelProvider modelProvider;
	
	// construction
	public PythonInstrumentActionExecuter(ScriptEngine engine, ModelProvider modelProvider) {
		this.engine = engine;
		this.modelProvider = modelProvider;

		try {
			long id = setObject(engine.getContext());
			try {
				engine.eval(PY_IMPORT_MSW);
				engine.eval(PY_CONTEXT.replaceAll(ID_TAG, Long.toString(id) + 'L'));
			}
			finally {
				removeObject(id);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// helper
	private long getPocessingTime(long startTime) {
		long nano = System.nanoTime() - startTime;
		return nano / 1000 / 1000 / 1000;
	}
	
	// methods
	@Override
	public InitializationSummary initiate() {
		PyInitiateInfo info = null;
		long startTime = System.nanoTime();
		
		try {
			List<User> users = new ArrayList<>();
			modelProvider.getUserList().fetchElements(users);
			Collections.sort(users, Element.INDEX_COMPARATOR);
			
			StringBuilder usersStr = new StringBuilder();
			StringBuilder emails = new StringBuilder();
			StringBuilder phones = new StringBuilder();
			final Procedure2<StringBuilder, String> merger = new Procedure2<StringBuilder, String>() {
				@Override
				public void apply(StringBuilder sb, String str) {
					if ((str != null) && (str.length() > 0)) {
						if (sb.length() > 0)
							sb.append("; ");
						
						sb.append(str);
					}
				}
			};
			
			for (User user : users) {
				merger.apply(usersStr, user.getName());
				merger.apply(emails, user.getEmail());
				merger.apply(phones, user.getPhone());
			}

			ExperimentDescription experimentDescription = modelProvider.getExperimentDescription();
			info = new PyInitiateInfo(
					experimentDescription.getProposalNumber(),
					experimentDescription.getExperimentTitle(),
					experimentDescription.getSampleStage(),
					usersStr.toString(),
					emails.toString(),
					phones.toString());
			
			long id = setObject(info);
			try {
				engine.eval(PY_IMPORT_MSW);
				engine.eval(PY_INITIATE.replaceAll(ID_TAG, Long.toString(id) + 'L'));
			}
			finally {
				removeObject(id);
			}
		}
		catch (Exception e) {
			info = null;
			e.printStackTrace();
		}
		
		if (info == null)
			return new InitializationSummary(
					Collections.<String, Object>emptyMap(),
					getPocessingTime(startTime),
					true,
					null);
		else
			return new InitializationSummary(
					Collections.<String, Object>emptyMap(),
					info.proposalNumber,
					info.experimentTitle,
					info.sampleStage,
					getPocessingTime(startTime),
					info.interrupted,
					info.notes);
	}
	@Override
	public Summary cleanUp() {
		PyInfo info = null;
		long startTime = System.nanoTime();
		
		try {
			info = new PyInfo();

			long id = setObject(info);
			try {
				engine.eval(PY_IMPORT_MSW);
				engine.eval(PY_CLEAN_UP.replaceAll(ID_TAG, Long.toString(id) + 'L'));
			}
			finally {
				removeObject(id);
			}
		}
		catch (Exception e) {
			info = null;
			e.printStackTrace();
		}
		
		if (info == null)
			return new Summary(
					Collections.<String, Object>emptyMap(),
					getPocessingTime(startTime),
					true,
					null);
		else
			return new Summary(
					Collections.<String, Object>emptyMap(),
					getPocessingTime(startTime),
					info.interrupted,
					info.notes);
	}
	// steps
	@Override
	public ParameterChangeSummary setParameters(String name, Map<String, Object> parameters) {
		PyParameterInfo info = null;
		long startTime = System.nanoTime();
		
		try {
			int indexId = name.indexOf(RefId.HASH);
			if (indexId != -1)
				name = name.substring(0, indexId);
			
			info = new PyParameterInfo(name, parameters);
			
			long id = setObject(info);
			try {
				engine.eval(PY_IMPORT_MSW);
				engine.eval(PY_SET_PARAMETERS_SCRIPT.replaceAll(ID_TAG, Long.toString(id) + 'L'));
			}
			finally {
				removeObject(id);
			}
		}
		catch (Exception e) {
			info = null;
			e.printStackTrace();
		}
		
		if (info == null)
			return new ParameterChangeSummary(
					name,
					parameters,
					getPocessingTime(startTime),
					true,
					null);
		else
			return new ParameterChangeSummary(
					name,
					parameters,
					getPocessingTime(startTime),
					info.interrupted,
					info.notes);
	}
	@Override
	public Summary preAcquisition() {
		PyInfo info = null;
		long startTime = System.nanoTime();
		
		try {
			info = new PyInfo();
			
			long id = setObject(info);
			try {
				engine.eval(PY_IMPORT_MSW);
				engine.eval(PY_PRE_ACQUISITION_SCRIPT.replaceAll(ID_TAG, Long.toString(id) + 'L'));
			}
			finally {
				removeObject(id);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		if (info == null)
			return new Summary(
					Collections.<String, Object>emptyMap(),
					getPocessingTime(startTime),
					true,
					null);
		else
			return new Summary(
					Collections.<String, Object>emptyMap(),
					getPocessingTime(startTime),
					info.interrupted,
					info.notes);
	}
	@Override
	public AcquisitionSummary doAcquisition(Map<String, Object> parameters) {
		PyAcquisitionInfo info = null;
		long startTime = System.nanoTime();
		
		try {
			info = new PyAcquisitionInfo(parameters);

			long id = setObject(info);
			try {
				engine.eval(PY_IMPORT_MSW);
				engine.eval(PY_DO_ACQUISITION_SCRIPT.replaceAll(ID_TAG, Long.toString(id) + 'L'));
			}
			finally {
				removeObject(id);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if (info == null)
			return new AcquisitionSummary(
					parameters,
					getPocessingTime(startTime),
					true,
					null);
		else
			return new AcquisitionSummary(
					parameters,
					info.filename,
					info.totalSeconds,
					info.totalCounts,
					info.monitorCounts,
					getPocessingTime(startTime),
					info.interrupted,
					info.notes);
	}
	@Override
	public Summary postAcquisition() {
		PyInfo info = null;
		long startTime = System.nanoTime();
		
		try {
			info = new PyInfo();
			
			long id = setObject(info);
			try {
				engine.eval(PY_IMPORT_MSW);
				engine.eval(PY_POST_ACQUISITION_SCRIPT.replaceAll(ID_TAG, Long.toString(id) + 'L'));
			}
			finally {
				removeObject(id);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		if (info == null)
			return new Summary(
					Collections.<String, Object>emptyMap(),
					getPocessingTime(startTime),
					true,
					null);
		else
			return new Summary(
					Collections.<String, Object>emptyMap(),
					getPocessingTime(startTime),
					info.interrupted,
					info.notes);
	}
	// custom
	@Override
	public Summary customAction(String action, Map<String, Object> parameters) {
		PyCustomActionInfo info = null;
		long startTime = System.nanoTime();
		
		try {
			info = new PyCustomActionInfo(action, parameters);
			
			long id = setObject(info);
			try {
				engine.eval(PY_IMPORT_MSW);
				engine.eval(PY_CUSTOM_ACTION_SCRIPT.replaceAll(ID_TAG, Long.toString(id) + 'L'));
			}
			finally {
				removeObject(id);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		if (info == null)
			return new Summary(
					parameters,
					getPocessingTime(startTime),
					true,
					null);
		else
			return new Summary(
					parameters,
					getPocessingTime(startTime),
					info.interrupted,
					info.notes);
	}
	
	// java/python interface
	public static synchronized Object getObject(long id) {
		return idMap.get(id);
	}
	private static synchronized long setObject(Object object) {
		long id = ++objectId;
		idMap.put(id, object);
		return id;
	}
	private static synchronized void removeObject(long id) {
		idMap.remove(id);
	}

	public static class PyInfo {
		// fields
		public boolean interrupted;
		public String notes;
		
		// construction
		public PyInfo() {
			this.interrupted = false;
			this.notes = null;
		}
	}

	public static class PyInitiateInfo extends PyInfo {
		// fields
		public String proposalNumber;
		public String experimentTitle;
		public String sampleStage;
		// others
		public String users;
		public String emails;
		public String phones;
		
		// construction
		public PyInitiateInfo(String proposalNumber, String experimentTitle, String sampleStage, String users, String emails, String phones) {
			this.proposalNumber = proposalNumber;
			this.experimentTitle = experimentTitle;
			this.sampleStage = sampleStage;
			this.users = users;
			this.emails = emails;
			this.phones = phones;
		}
	}
	
	public static class PyParameterInfo extends PyInfo {
		// fields
		public String name;
		public Map<String, Object> parameters;
		
		// construction
		public PyParameterInfo(String name, Map<String, Object> parameters) {
			this.name = name;
			this.parameters = parameters;
		}
	}
	
	public static class PyAcquisitionInfo extends PyInfo {
		// fields
		public Map<String, Object> parameters; // optional: MinTime, MaxTime, TargetDetectorCounts, TargetMonitorCounts
		public String filename;
		public long totalSeconds;
		public long totalCounts;
		public long monitorCounts;
		
		// construction
		public PyAcquisitionInfo(Map<String, Object> parameters) {
			this.parameters = parameters;
			this.filename = null;
			this.totalSeconds = 0;
			this.totalCounts = 0;
			this.monitorCounts = 0;
		}
	}

	public static class PyCustomActionInfo extends PyInfo {
		// fields
		public String action;
		public Map<String, Object> parameters;
		
		// construction
		public PyCustomActionInfo(String action, Map<String, Object> parameters) {
			this.action = action;
			this.parameters = parameters;
		}
	}
	
	private static interface Procedure2<A0, A1> {
		// methods
		public void apply(A0 a0, A1 a1);
	}
}
