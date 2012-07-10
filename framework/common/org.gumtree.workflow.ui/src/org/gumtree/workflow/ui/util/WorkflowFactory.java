/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package org.gumtree.workflow.ui.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.gumtree.core.object.ObjectConfigException;
import org.gumtree.util.collection.IParameters;
import org.gumtree.util.collection.Parameters;
import org.gumtree.util.xml.CustomisedMapConverter;
import org.gumtree.util.xml.ParameterMapConverter;
import org.gumtree.util.xml.ParametersConverter;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.config.TaskConfig;
import org.gumtree.workflow.ui.config.WorkflowConfig;
import org.gumtree.workflow.ui.internal.Workflow;

import com.thoughtworks.xstream.XStream;

/**
 * WorkflowUtils contains a collection of utility methods for the workflow API.
 * 
 * @since 1.0
 */
public final class WorkflowFactory {

	private static volatile XStream xStream;
	
	public static XStream getXStream() {
		if (xStream == null) {
			synchronized (WorkflowFactory.class) {
				if (xStream == null) {
					xStream = new XStream();
					xStream.alias("workflow", WorkflowConfig.class);
					xStream.alias("task", TaskConfig.class);
					xStream.alias("parameters", IParameters.class, Parameters.class);
					xStream.useAttributeFor(TaskConfig.class, "classname");
					xStream.addImplicitCollection(WorkflowConfig.class, "taskConfigs", TaskConfig.class);
					xStream.registerConverter(new ParametersConverter(xStream.getMapper()));
					xStream.registerConverter(new ParameterMapConverter(xStream.getMapper()));
					xStream.registerConverter(new CustomisedMapConverter(xStream.getMapper()));
					// TODO: should avoid using this
					xStream.autodetectAnnotations(true);
				}
			}
		}
		return xStream;
	}
	
	public static IWorkflow createEmptyWorkflow() {
		return createWorkflow(new WorkflowConfig());
	}
	
	public static IWorkflow createWorkflow(InputStream input) throws ObjectConfigException {
		return createWorkflow(input, new HashMap<String, Object>());
	}
	
	public static IWorkflow createWorkflow(Reader reader) throws ObjectConfigException {
		return createWorkflow(reader, new HashMap<String, Object>());
	}
	
	public static IWorkflow createWorkflow(InputStream input, Map<String, Object> initialContext) throws ObjectConfigException {
		Object configObject = null;
		
		// Load file
		try {
			configObject = getXStream().fromXML(input);
		} catch (Throwable t) {
			throw new ObjectConfigException("Invalid config file input.", t);
		}
		
		// Use loaded file to create workflow
		if (configObject instanceof WorkflowConfig) {
			return createWorkflow((WorkflowConfig) configObject, initialContext);
		}
		throw new ObjectConfigException("Invalid config file.");
	}
	
	public static IWorkflow createWorkflow(Reader reader, Map<String, Object> initialContext) throws ObjectConfigException {
		Object configObject = null;
		
		// Load file
		try {
			configObject = getXStream().fromXML(reader);
		} catch (Throwable t) {
			throw new ObjectConfigException("Invalid config file input.", t);
		}
		
		// Use loaded file to create workflow
		if (configObject instanceof WorkflowConfig) {
			return createWorkflow((WorkflowConfig) configObject, initialContext);
		}
		throw new ObjectConfigException("Invalid config file.");
	}
	
	public static IWorkflow createWorkflow(WorkflowConfig config) throws ObjectConfigException {
		return createWorkflow(config, new HashMap<String, Object>());
	}
	
	public static IWorkflow createWorkflow(WorkflowConfig config, Map<String, Object> initialContext) throws ObjectConfigException {
		Workflow workflow = new Workflow();
		workflow.getContext().putAll(initialContext);
		workflow.configure(config);
		return workflow;
	}
	
	public static IWorkflow createWorkflow(IWorkflow originalWorkflow) {
		StringWriter writer = new StringWriter();
		saveWorkflow(originalWorkflow, writer);
		IWorkflow newWorkflow = createWorkflow(new StringReader(writer.getBuffer().toString()));
		return newWorkflow;
	}
	
	public static void saveWorkflow(IWorkflow workflow, OutputStream out) {
		WorkflowFactory.getXStream().toXML(createWorkflowConfig(workflow), out);
	}
	
	public static void saveWorkflow(IWorkflow workflow, Writer writer) {
		WorkflowFactory.getXStream().toXML(createWorkflowConfig(workflow), writer);
	}
	
	private static WorkflowConfig createWorkflowConfig(IWorkflow workflow) {
		WorkflowConfig config = new WorkflowConfig();
		
		// Persists context		
		for (Entry<String, Object> entry : workflow.getContext().entrySet()) {
			if (workflow.getContext().isPersistable(entry.getKey())) {
				config.getContext().put(entry.getKey(), entry.getValue());
			}
		}
		
		// Persists parameters
		config.setParameters(workflow.getParameters());
		
		// Persists tasks
		for (int i = 0; i < workflow.getTasks().size(); i++) {
			ITask task = workflow.getTasks().get(i);
			TaskConfig taskConfig = new TaskConfig();
			
			// Persists class name
			taskConfig.setClassname(task.getClass().getName());
			// Persists data model
			taskConfig.setDataModel(task.getDataModel());
			// Persists parameters
			task.persist(task.getParameters());
			taskConfig.setParameters(task.getParameters());
			
			config.getTaskConfigs().add(taskConfig);
		}
		return config;
	}

	private WorkflowFactory() {
		super();
	}
	
}
