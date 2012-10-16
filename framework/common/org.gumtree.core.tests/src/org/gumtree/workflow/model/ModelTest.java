package org.gumtree.workflow.model;

import static org.junit.Assert.assertEquals;

import org.gumtree.workflow.WorkflowTestUtils.DummyTask;
import org.junit.Test;

public class ModelTest {

	@Test
	public void testEmptyModel() {
		WorkflowModel workflow = new WorkflowModel();
		workflow.setName("Test");
		assertEquals("Test", workflow.getName());
		assertEquals(0, workflow.getChildren().size());
		assertEquals(0, workflow.getStartTasks().size());
		assertEquals(0, workflow.getEndTasks().size());
	}

	@Test
	public void testSingleModel() {
		WorkflowModel workflow = new WorkflowModel();
		workflow.setName("Test");

		TaskModel task = workflow.createTaskModel();
		task.setTaskClass(DummyTask.class.getName());
		workflow.addStartTask(task);
		workflow.addEndTask(task);

		assertEquals(1, workflow.getChildren().size());
		assertEquals(1, workflow.getStartTasks().size());
		assertEquals(1, workflow.getEndTasks().size());

		assertEquals(workflow, task.getParent());
		assertEquals(0, task.getPreviousTasks().size());
		assertEquals(0, task.getNextTasks().size());
	}

	@Test
	public void testLinearModel() {
		WorkflowModel workflow = new WorkflowModel();
		workflow.setName("Test");

		TaskModel task1 = workflow.createTaskModel();
		task1.setTaskClass(DummyTask.class.getName());
		workflow.addStartTask(task1);

		TaskModel task2 = workflow.createTaskModel();
		task2.setTaskClass(DummyTask.class.getName());
		task1.addNextTask(task2);

		TaskModel task3 = workflow.createTaskModel();
		task3.setTaskClass(DummyTask.class.getName());
		task2.addNextTask(task3);
		workflow.addEndTask(task3);

		// Test workflow model
		assertEquals(3, workflow.getChildren().size());
		assertEquals(1, workflow.getStartTasks().size());
		assertEquals(1, workflow.getEndTasks().size());
		assertEquals(task1, workflow.getStartTasks().get(0));
		assertEquals(task3, workflow.getEndTasks().get(0));

		// Test task1 model
		assertEquals(workflow, task1.getParent());
		assertEquals(0, task1.getStartTasks().size());
		assertEquals(0, task1.getEndTasks().size());
		assertEquals(0, task1.getPreviousTasks().size());
		assertEquals(1, task1.getNextTasks().size());
		assertEquals(task2, task1.getNextTasks().get(0));

		// Test task2 model
		assertEquals(workflow, task2.getParent());
		assertEquals(0, task2.getStartTasks().size());
		assertEquals(0, task2.getEndTasks().size());
		assertEquals(1, task2.getPreviousTasks().size());
		assertEquals(1, task2.getNextTasks().size());
		assertEquals(task1, task2.getPreviousTasks().get(0));
		assertEquals(task3, task2.getNextTasks().get(0));

		// Test task3 model
		assertEquals(workflow, task3.getParent());
		assertEquals(0, task3.getStartTasks().size());
		assertEquals(0, task3.getEndTasks().size());
		assertEquals(1, task3.getPreviousTasks().size());
		assertEquals(0, task3.getNextTasks().size());
		assertEquals(task2, task3.getPreviousTasks().get(0));
	}

	@Test
	public void testComplexModel() {
		WorkflowModel workflow = new WorkflowModel();
		workflow.setName("Test");

		TaskModel task1 = workflow.createTaskModel();
		task1.setTaskClass(DummyTask.class.getName());
		workflow.addStartTask(task1);
		workflow.addEndTask(task1);

		TaskModel task2 = task1.createTaskModel();
		task1.setTaskClass(DummyTask.class.getName());
		task1.addStartTask(task2);

		TaskModel task3 = task1.createTaskModel();
		task3.setTaskClass(DummyTask.class.getName());
		task2.addNextTask(task3);

		TaskModel task4 = task1.createTaskModel();
		task4.setTaskClass(DummyTask.class.getName());
		task3.addNextTask(task4);
		task1.addEndTask(task4);

		TaskModel task5 = workflow.createTaskModel();
		task5.setTaskClass(DummyTask.class.getName());
		task3.addNextTask(task5);

		TaskModel task6 = workflow.createTaskModel();
		task6.setTaskClass(DummyTask.class.getName());
		task5.addNextTask(task6);
		workflow.addEndTask(task6);

		TaskModel task7 = workflow.createTaskModel();
		task7.setTaskClass(DummyTask.class.getName());
		task5.addNextTask(task7);
		workflow.addEndTask(task7);

		TaskModel task8 = workflow.createTaskModel();
		task8.setTaskClass(DummyTask.class.getName());
		workflow.addStartTask(task8);

		TaskModel task9 = workflow.createTaskModel();
		task9.setTaskClass(DummyTask.class.getName());
		task8.addNextTask(task9);

		TaskModel task10 = workflow.createTaskModel();
		task10.setTaskClass(DummyTask.class.getName());
		task9.addNextTask(task10);

		TaskModel task11 = workflow.createTaskModel();
		task11.setTaskClass(DummyTask.class.getName());
		task8.addNextTask(task11);

		TaskModel task12 = workflow.createTaskModel();
		task11.setTaskClass(DummyTask.class.getName());
		task10.addNextTask(task12);
		task11.addNextTask(task12);
		workflow.addEndTask(task12);

		assertEquals(9, workflow.getChildren().size());
		assertEquals(2, workflow.getStartTasks().size());
		assertEquals(4, workflow.getEndTasks().size());
	}

	@Test
	public void testSerialisation() {
		WorkflowModel workflow = new WorkflowModel();
		workflow.setName("Test");

		TaskModel task1 = workflow.createTaskModel();
		task1.setTaskClass(DummyTask.class.getName());
		workflow.addStartTask(task1);
		workflow.addEndTask(task1);

		TaskModel task2 = task1.createTaskModel();
		task1.setTaskClass(DummyTask.class.getName());
		task1.addStartTask(task2);

		TaskModel task3 = task1.createTaskModel();
		task3.setTaskClass(DummyTask.class.getName());
		task2.addNextTask(task3);

		TaskModel task4 = task1.createTaskModel();
		task4.setTaskClass(DummyTask.class.getName());
		task3.addNextTask(task4);
		task1.addEndTask(task4);

		TaskModel task5 = workflow.createTaskModel();
		task5.setTaskClass(DummyTask.class.getName());
		task3.addNextTask(task5);

		TaskModel task6 = workflow.createTaskModel();
		task6.setTaskClass(DummyTask.class.getName());
		task5.addNextTask(task6);
		workflow.addEndTask(task6);

		TaskModel task7 = workflow.createTaskModel();
		task7.setTaskClass(DummyTask.class.getName());
		task5.addNextTask(task7);
		workflow.addEndTask(task7);

		TaskModel task8 = workflow.createTaskModel();
		task8.setTaskClass(DummyTask.class.getName());
		workflow.addStartTask(task8);

		TaskModel task9 = workflow.createTaskModel();
		task9.setTaskClass(DummyTask.class.getName());
		task8.addNextTask(task9);

		TaskModel task10 = workflow.createTaskModel();
		task10.setTaskClass(DummyTask.class.getName());
		task9.addNextTask(task10);

		TaskModel task11 = workflow.createTaskModel();
		task11.setTaskClass(DummyTask.class.getName());
		task8.addNextTask(task11);

		TaskModel task12 = workflow.createTaskModel();
		task11.setTaskClass(DummyTask.class.getName());
		task10.addNextTask(task12);
		task11.addNextTask(task12);
		workflow.addEndTask(task12);

		String string = ModelUtils.getGson().toJson(workflow);

		WorkflowModel newModel = ModelUtils.getGson().fromJson(string,
				WorkflowModel.class);

		assertEquals(9, newModel.getChildren().size());
		assertEquals(2, workflow.getStartTasks().size());
		assertEquals(4, workflow.getEndTasks().size());
	}

}
