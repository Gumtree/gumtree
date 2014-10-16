package au.gov.ansto.bragg.nbi.server.jython;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JythonRunnerManager {

	private static Map<UUID, JythonRunner> runnerMap;

	static {
		runnerMap = new HashMap<UUID, JythonRunner>();
	}
	
	public JythonRunnerManager() {
	}

	public JythonRunner getNewRunner() {
		JythonRunner runner = new JythonRunner();
		runnerMap.put(runner.getUuid(), runner);
		return runner;
	}
	
	public JythonRunner getNewRunner(String uuidString) {
		UUID uuid = UUID.fromString(uuidString);
		JythonRunner runner = new JythonRunner(uuid);
		runnerMap.put(uuid, runner);
		return runner;
	}
	
	public JythonRunner getJythonRunner(UUID uuid){
		return runnerMap.get(uuid);
	}

	public void deleteJythonRunner(UUID uuid) {
		runnerMap.remove(uuid);
	}
	
}
