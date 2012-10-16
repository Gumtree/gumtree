package org.gumtree.workflow;

import org.gumtree.workflow.AbstractTask;
import org.gumtree.workflow.model.ElementModel;
import org.gumtree.workflow.model.TaskModel;
import org.gumtree.workflow.model.WorkflowModel;

public final class WorkflowTestUtils {

	public static WorkflowModel createSimpleModel() {
		WorkflowModel workflow = new WorkflowModel();
		workflow.setName("Test");
		return workflow;
	}

	public static WorkflowModel createLinearModel(int numberOfTask) {
		WorkflowModel workflow = new WorkflowModel();
		workflow.setName("Test");
		TaskModel previousModel = null;
		for (int i = 0; i < numberOfTask; i++) {
			TaskModel task = workflow.createTaskModel();
			task.setTaskClass(DummyTask.class.getName());
			task.setName("Task#" + i);
			if (i == 0) {
				workflow.addStartTask(task);
			}
			if (i == numberOfTask - 1) {
				workflow.addEndTask(task);
			}
			if (i != 0 && i != numberOfTask - 1) {
				previousModel.addNextTask(task);
			}
			previousModel = task;
		}
		return workflow;
	}
	
	public static WorkflowModel createParallelModel(int numberOfParallelTask, int numberOfLinearTask) {
		WorkflowModel workflow = new WorkflowModel();
		workflow.setName("Test");
		ElementModel parent = workflow;
		for (int j = 0; j < numberOfParallelTask; j++) {
			TaskModel previousModel = null;
			for (int i = 0; i < numberOfLinearTask; i++) {
				TaskModel task = parent.createTaskModel();
				task.setTaskClass(DummyTask.class.getName());
				task.setName("Task#" + j + "/" + i);
				if (i == 0) {
					parent.addStartTask(task);
				}
				if (i == numberOfLinearTask - 1) {
					parent.addEndTask(task);
				}
				if (i != 0 && i != numberOfLinearTask - 1) {
					previousModel.addNextTask(task);
				}
				previousModel = task;
			}
		}
		return workflow;
	}

	public static WorkflowModel createComplexModel() {
		WorkflowModel workflow = new WorkflowModel();
		workflow.setName("Test");

		TaskModel task1 = workflow.createTaskModel();
		task1.setTaskClass(DummyTask.class.getName());
		task1.setName("Task#01");
		workflow.addStartTask(task1);
		workflow.addEndTask(task1);

		TaskModel task2 = task1.createTaskModel();
		task2.setName("Task#02");
		task1.setTaskClass(DummyTask.class.getName());
		task1.addStartTask(task2);

		TaskModel task3 = task1.createTaskModel();
		task3.setName("Task#03");
		task3.setTaskClass(DummyTask.class.getName());
		task2.addNextTask(task3);

		TaskModel task4 = task1.createTaskModel();
		task4.setName("Task#04");
		task4.setTaskClass(DummyTask.class.getName());
		task3.addNextTask(task4);
		task1.addEndTask(task4);

		TaskModel task5 = workflow.createTaskModel();
		task5.setName("Task#05");
		task5.setTaskClass(DummyTask.class.getName());
		task3.addNextTask(task5);

		TaskModel task6 = workflow.createTaskModel();
		task6.setName("Task#06");
		task6.setTaskClass(DummyTask.class.getName());
		task5.addNextTask(task6);
		workflow.addEndTask(task6);

		TaskModel task7 = workflow.createTaskModel();
		task7.setName("Task#07");
		task7.setTaskClass(DummyTask.class.getName());
		task5.addNextTask(task7);
		workflow.addEndTask(task7);

		TaskModel task8 = workflow.createTaskModel();
		task8.setName("Task#08");
		task8.setTaskClass(DummyTask.class.getName());
		workflow.addStartTask(task8);

		TaskModel task9 = workflow.createTaskModel();
		task9.setName("Task#09");
		task9.setTaskClass(DummyTask.class.getName());
		task8.addNextTask(task9);

		TaskModel task10 = workflow.createTaskModel();
		task10.setName("Task#10");
		task10.setTaskClass(DummyTask.class.getName());
		task9.addNextTask(task10);

		TaskModel task11 = workflow.createTaskModel();
		task11.setName("Task#11");
		task11.setTaskClass(DummyTask.class.getName());
		task8.addNextTask(task11);

		TaskModel task12 = workflow.createTaskModel();
		task12.setName("Task#12");
		task11.setTaskClass(DummyTask.class.getName());
		task10.addNextTask(task12);
		task11.addNextTask(task12);
		workflow.addEndTask(task12);

		return workflow;
	}

	private WorkflowTestUtils() {
		super();
	}

	@SuppressWarnings("serial")
	public static class DummyTask extends AbstractTask<Void, Void> {

		@Override
		public void run() {
		}

	}

}
