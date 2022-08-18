/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author nxi
 *
 */
public class RecurrentScheduler {

	private List<IRecurrentTask> taskList;
	private Timer timer;
	private boolean isEnabled = true;
	
	/**
	 * 
	 */
	public RecurrentScheduler(long msPeriod) {
		taskList = new ArrayList<IRecurrentTask>();
		timer = new Timer(true);
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				synchronized (taskList) {
					if (isEnabled) {
						for (IRecurrentTask task : taskList) {
							task.run();
						}
					} else {
						cancel();
					}
				}
			}
		}, msPeriod, msPeriod);
	}

	public void addTask(IRecurrentTask task) {
		taskList.add(task);
	}
	
	public void removeTask(IRecurrentTask task) {
		taskList.remove(task);
	}
	
	public interface IRecurrentTask{
		void run();
	}
	
}
