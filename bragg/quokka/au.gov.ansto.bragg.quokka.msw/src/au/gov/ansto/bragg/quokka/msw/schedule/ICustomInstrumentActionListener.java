package au.gov.ansto.bragg.quokka.msw.schedule;

import org.gumtree.msw.schedule.execution.Summary;

public interface ICustomInstrumentActionListener {
	// methods
	public void onActionFinished(String action, Summary summary);
}
