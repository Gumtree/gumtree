package au.gov.ansto.bragg.quokka.msw.schedule;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.gumtree.msw.schedule.execution.AcquisitionSummary;
import org.gumtree.msw.schedule.execution.ParameterChangeSummary;
import org.gumtree.msw.schedule.execution.Summary;

import au.gov.ansto.bragg.quokka.msw.report.LogbookReportGenerator.TableInfo;

public class InstrumentActionExecuter {
	// static fields
	private static IInstrumentExecuter defaultExecuter = new DummyInstrumentActionExecuter();
	
	// construction
	private InstrumentActionExecuter() {
	}
	
	// static
	public static IInstrumentExecuter getDefault() {
		return defaultExecuter;
	}
	public static void setDefault(IInstrumentExecuter value) {
		defaultExecuter = value;
	}

	// dummy
	private static class DummyInstrumentActionExecuter implements IInstrumentExecuter {
		// fields
		private int runId;
		
		// construction
		public DummyInstrumentActionExecuter() {
			runId = 0;
		}
		
		// helper
		private void sleep() {
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// methods
		@Override
		public Summary initiate() {
			return new Summary(Collections.<String, Object>emptyMap(), 100, false, null);
		}
		@Override
		public Summary cleanUp() {
			return new Summary(Collections.<String, Object>emptyMap(), 100, false, null);
		}
		// steps
		@Override
		public ParameterChangeSummary setParameters(String name, Map<String, Object> parameters) {
			sleep();
			return new ParameterChangeSummary(name, parameters, 100, false, null);
		}
		@Override
		public Summary preAcquisition() {
			sleep();
			return new Summary(Collections.<String, Object>emptyMap(), 100, false, null);
		}
		@Override
		public AcquisitionSummary doAcquisition(Map<String, Object> parameters) {
			sleep();
			return new AcquisitionSummary(
					parameters,
					String.format("QKK%07d", ++runId),
					0,
					0,
					0,
					100,
					false,
					null);
		}
		@Override
		public Summary postAcquisition() {
			sleep();
			return new Summary(Collections.<String, Object>emptyMap(), 100, false, null);
		}
		@Override
		@SuppressWarnings("unchecked")
		public Summary customAction(String action, Map<String, Object> parameters) {
			if (Objects.equals(action, CustomInstrumentAction.GET_SAMPLE_POSITIONS))
				parameters.put("SamplePositions", 20);
			else if (Objects.equals(action, CustomInstrumentAction.PUBLISH_FINISH_TIME)) {
				System.out.println("Time: " + String.valueOf(parameters.get("Time")));
			}
			else if (Objects.equals(action, CustomInstrumentAction.PUBLISH_TABLES)) {
				Object tables = parameters.get("Tables");
				if (tables instanceof Iterable<?>)
					for (TableInfo table : (Iterable<TableInfo>)tables) {
						System.out.println(table.getName());
						System.out.println(table.getContent());
					}
			}
			else
				sleep();

			return new Summary(parameters, 100, false, null);
		}
	}
}
