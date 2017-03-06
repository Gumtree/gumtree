package org.gumtree.msw.schedule.execution;

import java.util.Map;

public interface IScheduleExecuter {
	// methods
	public Summary initiate();
	public Summary cleanUp();
	// steps
	public ParameterChangeSummary setParameters(String name, Map<String, Object> parameters);
	public Summary preAcquisition();
	public AcquisitionSummary doAcquisition(Map<String, Object> parameters);
	public Summary postAcquisition();
}
