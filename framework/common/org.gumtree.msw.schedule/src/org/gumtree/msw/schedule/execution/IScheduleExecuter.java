package org.gumtree.msw.schedule.execution;

import java.util.Map;

public interface IScheduleExecuter {
	// methods
	public boolean initiate();
	public void release();
	// steps
	public ParameterChangeSummary setParameters(String name, Map<String, Object> parameters);
	public Summary preAcquisition();
	public AcquisitionSummary doAcquisition();
	public Summary postAcquisition();
}
