package au.gov.ansto.bragg.wombat.exp.task;

import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.util.WorkflowFactory;

public class WombatExperimentWorkflow {

	public static IWorkflow createEmptyWorkflow(){
		IWorkflow workflow = WorkflowFactory.createEmptyWorkflow();
//		workflow.addTask(new HeaderInformationBlockTask());
//		workflow.addTask(new ScanNDTask());
		return workflow;
	}
}
