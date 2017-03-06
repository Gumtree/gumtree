package org.gumtree.msw.schedule.execution;

public interface IScheduleWalkerListener {
	// methods
	public void onBeginSchedule();
	public void onEndSchedule();
	// initialization
	public void onInitialized(Summary summary);
	public void onCleanedUp(Summary summary);
	// step
	public void onBeginStep(ScheduleStep step);
	public void onEndStep(ScheduleStep step);
	// parameters
	public void onBeginChangeParameter(ScheduleStep step);
	public void onEndChangeParameters(ScheduleStep step, ParameterChangeSummary summary);
	// acquisition
	public void onBeginPreAcquisition(ScheduleStep step);
	public void onEndPreAcquisition(ScheduleStep step, Summary summary);
	public void onBeginDoAcquisition(ScheduleStep step);
	public void onEndDoAcquisition(ScheduleStep step, AcquisitionSummary summary);
	public void onBeginPostAcquisition(ScheduleStep step);
	public void onEndPostAcquisition(ScheduleStep step, Summary summary);
}
