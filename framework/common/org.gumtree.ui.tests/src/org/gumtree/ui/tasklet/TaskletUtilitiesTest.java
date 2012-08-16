package org.gumtree.ui.tasklet;

import static org.junit.Assert.assertEquals;

import org.gumtree.ui.tasklet.support.Tasklet;
import org.gumtree.ui.tasklet.support.TaskletUtilities;
import org.junit.Test;

public class TaskletUtilitiesTest {

	@Test
	public void testSerialisation() {
		ITasklet tasklet = new Tasklet();
		tasklet.setLabel("Histogram Memory");
		tasklet.setTags("Control, Status");
		tasklet.setContributionURI("bundle://org.gumtree.ui/scripts/hm_custom.py");
		tasklet.setProperty("defaultUri", "bundle://org.gumtree.ui/scripts/hm.py");
		
		String text = TaskletUtilities.serialiseTasklet(tasklet);
	
		ITasklet newTasklet = TaskletUtilities.deserialiseTasklet(text);
		assertEquals(tasklet.getLabel(), newTasklet.getLabel());
		assertEquals(tasklet.getTags(), newTasklet.getTags());
		assertEquals(tasklet.getContributionURI(), newTasklet.getContributionURI());
		assertEquals(tasklet.getProperties().size(), newTasklet.getProperties().size());
		for (String key : newTasklet.getProperties().keySet()) {
			assertEquals(tasklet.getProperty(key), newTasklet.getProperty(key));
		}
	}

}
