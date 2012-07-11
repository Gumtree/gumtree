package au.gov.ansto.bragg.kowari.exp.task;

import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.util.WorkflowFactory;

public class KowariExperimentWorkflow {

	public static IWorkflow createEmptyWorkflow(){
		IWorkflow workflow = WorkflowFactory.createEmptyWorkflow();
		workflow.addTask(new HeaderInformationBlockTask());
//		workflow.addTask(new ScanNDTask());
		return workflow;
	}
}
