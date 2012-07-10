package org.gumtree.workflow.tasks;

import static org.junit.Assert.assertEquals;

import org.gumtree.workflow.tasks.StringProcessorTask.Operation;
import org.junit.Test;

public class StringProcessorTaskTest {

	@Test
	public void testTasks() {
		StringProcessorTask task = new StringProcessorTask();
		task.setOperation(Operation.TO_UPPER);
		assertEquals("ABC", task.setInput("abc").run().getOutput());
		
		task.setOperation(Operation.TO_LOWER);
		assertEquals("abc", task.setInput("aBc").run().getOutput());
	}

}
