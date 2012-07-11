/******************************************************************************* 
 * Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.quokka.exp.core.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.ui.internal.cli.beanshell.ActionListener;
import org.gumtree.ui.internal.cli.beanshell.BeanShellCommandLineView;
import org.gumtree.ui.internal.cli.beanshell.EvaluationFailedException;
import org.gumtree.ui.internal.cli.beanshell.BeanShellCommandLineView.ColorEnum;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.kakadu.dom.KakaduDOM;
import au.gov.ansto.bragg.quokka.exp.core.Command;
import au.gov.ansto.bragg.quokka.exp.core.QuokkaExperiment;
import au.gov.ansto.bragg.quokka.exp.core.exception.InitializeCommandException;
import au.gov.ansto.bragg.quokka.exp.core.lib.Util;

/**
 * @author nxi
 * Created on 31/03/2008
 */
public class Run implements Command {

	private File batchFile;
	QuokkaExperiment experiment;
	List<Thread> subThreadList;
	List<String> argNameList;
	List<String> argList;
	Thread currentRunningThread;

	public Run(){
		super();
		argNameList = new ArrayList<String>();
		argList = new ArrayList<String>();
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		String help = "RUN: Load a batch script file and run.\n\n";
		help += "Usage: run [<BatchScriptFilePath>]\n\n";
		help += "This generic command will parse a batch script file referred by "
			+ "the path argument and run the scripts.\n\n";
		help += "<BatchScriptFilePath> \t the location and the name of the batch script file, " +
		"with a extension name of 'qka'. "
		+ "It is an optional argument.\n\n";
		help += "If no device name is provided, the command will provide a UI interface to " +
		"choose a batch script file from the file system.\n";
		return help;	}

	public String getShortDescription() {
		// TODO Auto-generated method stub
		String description = "RUN: Load a batch script file and run.\n";
		description += "Usage: run [<BatchScriptFilePath>]\n";
		description += "For more information, please use 'help run'.\n";
		return description;	}

	public String run() {
		// TODO Auto-generated method stub
		if (batchFile == null){
			batchFile = KakaduDOM.getFilenameFromShell("qka", "quokka batch script file");
		}
		QuokkaExperiment.setCurrentPath(batchFile.getParent());
		List<String> commandLines = null;
		try {
			commandLines = getCommandLines();
		} catch (InitializeCommandException e1) {
			// TODO Auto-generated catch block
//			e1.printStackTrace();
			return e1.getMessage() + "\n";
		}
		experiment.printlnToShell("done loading the file, now processing the scripts ...\n\n");

		ActionListener skipListener = new ActionListener(){
			public void run(){
//				currentRunningThread.interrupt();
				skip();
			}
		};
		ActionListener stopListener = new ActionListener(){
			public void run(){
				stop();
			}
		};
		BeanShellCommandLineView.getInstance().addSkipActionListener(skipListener);
		BeanShellCommandLineView.getInstance().addStopActionListener(stopListener);
		subThreadList = new ArrayList<Thread>();
		Thread dependencyThread = null;
		try{
//			String[] temp = bufferReader.readLine().split("=");
			for (String commandLine : commandLines){
				Thread thread = new CommandThread(dependencyThread, commandLine);
				subThreadList.add(thread);
				dependencyThread = thread;
				thread.start();
			}
		}catch (Exception e) {
			// TODO: handle exception
			return "failed to run the commands\n";
		}
		while (dependencyThread != null && dependencyThread.isAlive()){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		BeanShellCommandLineView.getInstance().removeAllSkipActionListener();
		BeanShellCommandLineView.getInstance().removeAllStopActionListener();
//		Thread currentThread = Thread.currentThread();
//		while (dependencyThread.isAlive()){
//		try {
////		currentThread.wait(100);
//		wait(100);
//		} catch (InterruptedException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//		}
//		}

//		return "current thread: " + currentRunningThread.toString() + "\n" + 
		return "done running the script file: " + batchFile.getName();
	}

	private List<String> getCommandLines() throws InitializeCommandException {
		// TODO Auto-generated method stub
		List<String> rawCommandLines = new ArrayList<String>();
		List<String> commandLines = new ArrayList<String>();
		BufferedReader bufferReader = null;
		try {
			bufferReader = new BufferedReader(new FileReader(batchFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			throw new InitializeCommandException("failed to open the file, wrong format: " + e);
		}
		if (bufferReader == null)
			throw new InitializeCommandException("failed to open the file, wrong format");
		String header;
		boolean isFirstLine = true;
		try{
			while(bufferReader.ready()){
				String commandLine = bufferReader.readLine();
//				BeanShellCommandLineView beanShellView = BeanShellCommandLineView.getInstance();
//				beanShellView.evaluate(commandLine);
				if (commandLine.trim().length() == 0) continue;
				if (commandLine.trim().startsWith("#")) {
//					experiment.printlnToShell(commandLine);
					if (isFirstLine) header = commandLine;
					continue;
				}
				rawCommandLines.add(commandLine);
				argNameList.addAll(findArguments(commandLine));
				isFirstLine = false;
			}
			bufferReader.close();
		}catch (IOException e) {
			// TODO: handle exception
			throw new InitializeCommandException("failed to run the commands" + e);
		}
		while (argList.size() < argNameList.size()){
			askForArguments();
		}
		for (String commandLine : rawCommandLines){
			commandLines.add(applyArgument(commandLine));
		}
		return commandLines;
	}

	private void askForArguments() {
		// TODO Auto-generated method stub

		String message = "The required arguments are";
		for (Iterator<String> iterator = argNameList.iterator(); iterator.hasNext();) {
			String argName = iterator.next();
			message += " " + argName;
		}
		if (argList.size() > 0)
			message += "\nPlease arguments are";
		else 
			message += "\nno argument has been set.";
		for (Iterator<String> iterator = argList.iterator(); iterator.hasNext();) {
			String argValue = iterator.next();
			message += " " + argValue;
		}
		message += "\nPlease provide the rest arguments or reinput all the arguments";


//		boolean isValidReply = false;
//		while (!isValidReply){
		String argumentsLine = getBeanShellView().dialog(message).toLowerCase();
		String[] arguments = argumentsLine.split(" ");
		if (arguments.length >= argNameList.size()){
			argList = new ArrayList<String>();
		}
		for (int i = 0; i < arguments.length; i++) {
			argList.add(arguments[i]);
		}
	}

	private List<String> findArguments(String commandLine) {
		// TODO Auto-generated method stub
		List<String> args = new ArrayList<String>();
		int index = commandLine.indexOf("%");
		if (index < 0) return args;
//		commandLine.
		int argIndex = 0;
		for (int i = index + 2; i < commandLine.length() + 1; i ++){
			try {
				argIndex = Integer.valueOf(commandLine.substring(index + 1, i));
			} catch (Exception e) {
				// TODO: handle exception
				break;
			}
		}
		String arg = "%" + argIndex;
		boolean isListed = false;
		for (String argName : argNameList){
			if (argName.equals(arg))
				isListed = true;
		}
		if (! isListed)
			argNameList.add(arg);
		commandLine = commandLine.substring(index + 2);
		args.addAll(findArguments(commandLine));
		return args;
	}

	private List<String> validateArguments_bk() throws InitializeCommandException {
		// TODO Auto-generated method stub
		List<String> commandLines = new ArrayList<String>();
		BufferedReader bufferReader = null;
		try {
			bufferReader = new BufferedReader(new FileReader(batchFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			throw new InitializeCommandException("failed to open the file, wrong format: " + e);
		}
		if (bufferReader == null)
			throw new InitializeCommandException("failed to open the file, wrong format");
		String header;
		boolean isFirstLine = true;
		try{
			while(bufferReader.ready()){
				String commandLine = bufferReader.readLine();
//				BeanShellCommandLineView beanShellView = BeanShellCommandLineView.getInstance();
//				beanShellView.evaluate(commandLine);
				if (commandLine.trim().length() == 0) continue;
				if (commandLine.trim().startsWith("#")) {
//					experiment.printlnToShell(commandLine);
					if (isFirstLine) header = commandLine;
					continue;
				}
				try {
					commandLine = applyArgument(commandLine);
					commandLines.add(commandLine);
				} catch (InitializeCommandException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
					throw new InitializeCommandException(e.getMessage() + " in command: " + commandLine);
				}
				isFirstLine = false;
			}
			bufferReader.close();
		}catch (IOException e) {
			// TODO: handle exception
			throw new InitializeCommandException("failed to run the commands" + e);
		}
		return commandLines;
	}

	private String applyArgument(String commandLine) throws InitializeCommandException {
		// TODO Auto-generated method stub
		int index = commandLine.indexOf("%");
		if (index < 0) return commandLine;
//		commandLine.
		int argIndex = 0;
		for (int i = index + 2; i < commandLine.length() + 1; i ++){
			try {
				argIndex = Integer.valueOf(commandLine.substring(index + 1, i));
			} catch (Exception e) {
				// TODO: handle exception
				break;
			}
		}
//		if (argIndex == 0)
//		throw new InitializeCommandException("illegal use of '%'");
		String argValue = null;
		try {
			argValue = argList.get(argIndex - 1);
		} catch (Exception e) {
			// TODO: handle exception
//			throw new InitializeCommandException("please provide the argument '%" + argIndex + "'");
			String message = "please provide the argument '%" + argIndex + "'";
//			boolean isValidReply = false;
//			while (!isValidReply){
			String argument = getBeanShellView().dialog(message).toLowerCase();
			argValue = argument;
//			experiment.printlnToShell("get " + argument + "\n");
//			if (argument.matches("yes") || argument.matches("y")) {
//			confirmed = true;
//			isValidReply = true;
//			}
//			else if (argument.matches("no") || argument.matches("n")) {
//			confirmed = false;
//			isValidReply = true;
//			}
//			}
		}
		commandLine = commandLine.replaceFirst("%" + argIndex, argValue);
		commandLine = applyArgument(commandLine);
		return commandLine;
	}

	public void skip(){
//		currentRunningThread.interrupt();
//		currentRunningThread.stop();
		try {
			stopThread((CommandThread) currentRunningThread);			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void stop(){
		try {
			SicsCore.getSicsController().interrupt();
		} catch (SicsIOException e) {
			// TODO Auto-generated catch block
			LoggerFactory.getLogger(QuokkaExperiment.class);
		}
		for (Iterator iterator = subThreadList.iterator(); iterator.hasNext();) {
			CommandThread thread = (CommandThread) iterator.next();
			try {
				stopThread(thread);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		try {
			SicsCore.getSicsController().interrupt();
		} catch (SicsIOException e) {
			// TODO Auto-generated catch block
			LoggerFactory.getLogger(QuokkaExperiment.class);
		}

	}

	private void stopThread(CommandThread thread){
//		experiment.printlnToShell(thread.getCommandName(), ColorEnum.red);
//		thread.stop();
		if (thread == currentRunningThread && thread.getCommandName().equals("run"))
			return;
		thread.stop();
	}

	public void setExperiment(QuokkaExperiment experiment) {
		// TODO Auto-generated method stub
		this.experiment = experiment;
	}

	public void setParameter(String... params)
	throws InitializeCommandException {
		// TODO Auto-generated method stub
		if (params.length == 0) return;
//		if (params.length > 1) 
//		throw new InitializeCommandException("can not match parameters (expecting 1 parameter)");
		String filename = params[0];
		batchFile = new File(filename);
		if (!batchFile.exists()){ 
//			throw new InitializeCommandException("can not find the file: "
//			+ filename);
//			} catch (Exception e) {
			// TODO: handle exception
			if (! filename.startsWith("/"))
				filename = "/" + filename;
			if (! filename.toLowerCase().endsWith(".exp"))
				filename += ".exp";
			URI uri = null;
			try {
//				uri = Util.getLibraryFolder();
//				uri = new URI(uri.getRawPath() + filename);
//				batchFile = new File(uri);
//				if (! batchFile.exists()){
				File currentFolder = new File(".");
				System.out.println(currentFolder.getAbsolutePath());
				uri = Util.getRawPath(Util.LIBRARY_DIR + filename);
				batchFile = new File(uri);
				if (! batchFile.exists()){
					uri = Util.getRawPath(Util.LIBRARY_DIR + filename);
					batchFile = new File(uri);
				}
				if (! batchFile.exists()){
					uri = Util.getRawPath(filename);
					batchFile = new File(uri);
				}
				if (! batchFile.exists()){
					if (QuokkaExperiment.getCurrentPath() != null)
						batchFile = new File(QuokkaExperiment.getCurrentPath() + filename);
				}
				if (! batchFile.exists())
					throw new InitializeCommandException("can not find the file: " + params[0]);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				throw new InitializeCommandException("can not find the file: " + params[0]);
			}
		}
		for (int i = 1; i < params.length; i++) {
			argList.add(params[i]);
		}
	}

	private class CommandThread extends Thread{
		Thread dependencyThread; 
		String commandLine;

		public CommandThread(){
			super();
		}

		public CommandThread(Thread dependencyThread, String commandLine){
			this.dependencyThread = dependencyThread;
			this.commandLine = commandLine;
		}

		public void run(){
//			Thread thread = Thread.currentThread();
			if (dependencyThread != null){
				while (dependencyThread.isAlive()) {
					try {
//						System.out.println(commandLine + " thread sleep 100");
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}else {
				try {
//					System.out.println(commandLine + " thread sleep 100");
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			BeanShellCommandLineView beanShellView = BeanShellCommandLineView.getInstance();
			try {
				experiment.printlnToShell(">>>BATCH: " + commandLine + "\n", ColorEnum.black);
				currentRunningThread = this;
				beanShellView.evaluate(commandLine);
//				experiment.printlnToShell(">>>", ColorEnum.darkRed);
//				beanShellView.pushOut();
			} catch (EvaluationFailedException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				experiment.printlnToShell("failed to finish the commands -- user interrupt\n", ColorEnum.red);
			}
		}

		public String toString(){
			return super.toString() + " name: " + commandLine;
		}

		public String getCommandName(){
			String commandName = commandLine.substring(0, commandLine.trim().indexOf(" "));
			return commandName;
		}
	}

	BeanShellCommandLineView getBeanShellView(){
		return BeanShellCommandLineView.getInstance();
	}
}
