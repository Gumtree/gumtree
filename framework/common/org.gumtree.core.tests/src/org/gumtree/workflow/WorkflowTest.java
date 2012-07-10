package org.gumtree.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.gumtree.workflow.support.Workflow;
import org.gumtree.workflow.tasks.StringProcessorTask;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

public class WorkflowTest {

	@Test
	public void testWorkflowStructure() {
		IWorkflow workflow = new Workflow();
		assertEquals(0, workflow.getTasks().size());

		workflow.appendTask(new StringProcessorTask());
		assertEquals(1, workflow.getTasks().size());
		assertNotNull(workflow.getTask(0));

		workflow.appendTask(new StringProcessorTask());
		assertEquals(2, workflow.getTasks().size());
		assertNotNull(workflow.getTask(1));

		workflow.removeAllTasks();
		assertEquals(0, workflow.getTasks().size());
	}
	
	@Test
	public void testXStream() {
		XStream xStream = new XStream();
		xStream.autodetectAnnotations(true);
		IWorkflow workflow = new Workflow();
		workflow.appendTask(new StringProcessorTask());
		workflow.appendTask(new StringProcessorTask());
		String xml = xStream.toXML(workflow);
		System.out.println(xml);
		
		xStream = new XStream();
		workflow = (IWorkflow) xStream.fromXML(xml);
		System.out.println(workflow);
	}

}
