package au.gov.ansto.bragg.pelican.exp.config;

import org.gumtree.scripting.ScriptExecutor;

public class CommandHandler {

	private static ScriptExecutor Jython_Executor;
	
	public CommandHandler() {
		// TODO Auto-generated constructor stub
	}

	public ScriptExecutor getScriptExecutor(){
		if (Jython_Executor == null) {
			 Jython_Executor = new ScriptExecutor("jython");
			 try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		return Jython_Executor;
	}
	
	public void runCommand(String command){
		
	}
}
