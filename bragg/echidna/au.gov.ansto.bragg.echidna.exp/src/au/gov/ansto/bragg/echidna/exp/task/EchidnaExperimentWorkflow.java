package au.gov.ansto.bragg.echidna.exp.task;

import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.util.WorkflowFactory;

public class EchidnaExperimentWorkflow {

	public static IWorkflow createEmptyWorkflow(){
		IWorkflow workflow = WorkflowFactory.createEmptyWorkflow();
		workflow.addTask(new HeaderInformationBlockTask());
		workflow.addTask(new SicsScriptBlockTask());
//		workflow.addTask(new DoRTScanTask());
		return workflow;
	}
	
	public static IWorkflow createRobotWorkflow() {
		IWorkflow workflow = WorkflowFactory.createEmptyWorkflow();
		workflow.addTask(new HeaderInformationBlockTask());
		workflow.addTask(new SicsScriptBlockTask());
		workflow.addTask(new DoRTScanTask());
		return workflow;
	}
}
