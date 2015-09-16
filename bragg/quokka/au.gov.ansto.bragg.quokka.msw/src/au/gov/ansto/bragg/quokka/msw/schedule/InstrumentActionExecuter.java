package au.gov.ansto.bragg.quokka.msw.schedule;

import java.util.Map;

import org.gumtree.msw.schedule.execution.AcquisitionSummary;
import org.gumtree.msw.schedule.execution.ParameterChangeSummary;
import org.gumtree.msw.schedule.execution.Summary;

public class InstrumentActionExecuter implements IInstrumentActionExecuter {
	// static fields
	private static IInstrumentActionExecuter defaultExecuter = new InstrumentActionExecuter();
	
	// construction
	private InstrumentActionExecuter() {
	}
	
	// static
	public static IInstrumentActionExecuter getDefault() {
		return defaultExecuter;
	}
	public static void setDefault(IInstrumentActionExecuter value) {
		defaultExecuter = value;
	}
	
	// methods
	@Override
	public boolean initiate() {
		return true;
	}
	@Override
	public void release() {
	}
	// steps
	@Override
	public ParameterChangeSummary setParameters(String name, Map<String, Object> parameters) {
		try {
			Thread.sleep(500);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new ParameterChangeSummary(name, parameters, 100, false);
	}
	@Override
	public Summary preAcquisition() {
		try {
			Thread.sleep(500);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new Summary(100, false);
	}
	@Override
	public AcquisitionSummary doAcquisition() {
		try {
			Thread.sleep(500);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new AcquisitionSummary(100, false);
	}
	@Override
	public Summary postAcquisition() {
		try {
			Thread.sleep(500);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new Summary(100, false);
	}
	@Override
	public Summary customAction(String action, Map<String, Object> parameters) {
		try {
			Thread.sleep(500);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new Summary(100, false);
	}
}
