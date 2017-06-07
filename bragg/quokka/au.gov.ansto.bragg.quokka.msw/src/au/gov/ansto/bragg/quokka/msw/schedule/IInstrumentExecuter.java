package au.gov.ansto.bragg.quokka.msw.schedule;

import java.util.Map;

import org.gumtree.msw.schedule.execution.IScheduleExecuter;
import org.gumtree.msw.schedule.execution.Summary;

public interface IInstrumentExecuter extends IScheduleExecuter {
	// methods (custom action is not part of schedule)
	public Summary customAction(String action, Map<String, Object> parameters);
}
