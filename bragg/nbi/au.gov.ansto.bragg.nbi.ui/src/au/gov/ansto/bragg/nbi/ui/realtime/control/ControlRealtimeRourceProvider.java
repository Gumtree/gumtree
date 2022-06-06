/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package au.gov.ansto.bragg.nbi.ui.realtime.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.gumtree.control.core.ISicsController;

import au.gov.ansto.bragg.nbi.ui.realtime.IRealtimeResource;
import au.gov.ansto.bragg.nbi.ui.realtime.IRealtimeResourceProvider;

/**
 * @author nxi
 *
 */
public class ControlRealtimeRourceProvider implements IRealtimeResourceProvider {

//	private String[] resourceNames;
	private List<String> nameFilter;
//	private List<IRealtimeResource> resourceToManageList = new ArrayList<IRealtimeResource>();
	private Vector<IRealtimeResource> resourceToManageList = new Vector<IRealtimeResource>();
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.nbi.ui.realtime.IRealtimeResourceProvider#getResourceList()
	 */
	
	public ControlRealtimeRourceProvider() {
//		resourceNames = SicsUtils.getSicsDrivableIds();
	}
	@Override
	public List<IRealtimeResource> getResourceList() {
		// TODO Auto-generated method stub
//		if (resourceNames == null) {
//			return null;
//		}
		List<IRealtimeResource> resourceList = new ArrayList<IRealtimeResource>();
		if (nameFilter == null) {
			return resourceList;
		}
//		List<String> resourceNameList = Arrays.asList(resourceNames);
		for (String name : nameFilter) {
//			if (resourceNameList.contains(name)) {
//				SicsRealtimeResource resource = new SicsRealtimeResource(name);
//				resource.setFullName(name);
//				resourceList.add(resource);
//			}
//			else {
//				IComponentController controller = SicsRealtimeResource.findDevice(name);
//				if (controller != null) {
//					SicsRealtimeResource resource = new SicsRealtimeResource(name);
//					resourceList.add(resource);
//				}
//			}
			if (name == null || "null".equals(name)) {
				continue;
			}
			ISicsController controller = ControlRealtimeResource.findDevice(name);
			if (controller != null) {
				ControlRealtimeResource resource = new ControlRealtimeResource(
						ControlRealtimeResource.getSimpleName(name));
				resource.setFullName(name);
				resourceList.add(resource);
			}
		}
		List<ISicsController> environmentControllers = ControlRealtimeResource.getEnvironmentControllers();
		for (ISicsController controller : environmentControllers) {
			String id = controller.getId();
			if (id.contains("SP")) {
				id = id.replace("SP", "_Setpoint");
			} else {
				id = id.replace("S", "_Sensor");
			}
			ControlRealtimeResource resource = new ControlRealtimeResource(id);
			resource.setFullName(controller.getPath());
			resourceList.add(resource);
		}
		return resourceList;
	}

	public ControlRealtimeResource getResource(String name) {
		ISicsController controller = ControlRealtimeResource.findDevice(name);
		if (controller != null) {
//			String id = controller.getId();
//			if (id.contains("SP")) {
//				id = id.replace("SP", "_Setpoint");
//			} else {
//				id = id.replace("S", "_Sensor");
//			}
			ControlRealtimeResource resource = new ControlRealtimeResource(name);
			resource.setFullName(name);
			return resource;
		}
		return null;
	}
	
	public void setFilter(List<String> nameFilter) {
		this.nameFilter = nameFilter;
	}
	
	@Override
	public void addResourceToUpdateList(IRealtimeResource resource) {
		resourceToManageList.add(resource);
	}
	
	public void updateResource() {
		for (IRealtimeResource resource : resourceToManageList) {
			resource.update();
		}
	}

	public void clear() {
		for (IRealtimeResource resource : resourceToManageList) {
			resource.clear();
		}
		resourceToManageList.clear();
	}
	@Override
	public void removeResourceFromUpdateList(IRealtimeResource resource) {
		resourceToManageList.remove(resource);
		resource.clear();
	}
}
