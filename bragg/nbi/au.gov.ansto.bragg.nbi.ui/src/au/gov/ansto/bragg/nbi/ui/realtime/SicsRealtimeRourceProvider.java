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
package au.gov.ansto.bragg.nbi.ui.realtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.core.SicsUtils;

/**
 * @author nxi
 *
 */
public class SicsRealtimeRourceProvider implements IRealtimeResourceProvider {

	private static final String PROP_NXALIAS = "nxalias";
	private static final String PROP_NICKNAME = "nick";
	private static final String PROP_UNITS = "units";
	
//	private String[] resourceNames;
	private List<String> nameFilter;
//	private List<IRealtimeResource> resourceToManageList = new ArrayList<IRealtimeResource>();
	private Vector<IRealtimeResource> resourceToManageList = new Vector<IRealtimeResource>();
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.nbi.ui.realtime.IRealtimeResourceProvider#getResourceList()
	 */
	
	public SicsRealtimeRourceProvider() {
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
			IComponentController controller = SicsRealtimeResource.findDevice(name);
			if (controller != null) {
				SicsRealtimeResource resource = new SicsRealtimeResource(
						SicsRealtimeResource.getSimpleName(name));
				resource.setFullName(name);
				resourceList.add(resource);
			}
		}
		List<IComponentController> environmentControllers = SicsRealtimeResource.getEnvironmentControllers();
		for (IComponentController controller : environmentControllers) {
			String id;
			IComponentController nickController = SicsUtils.getNicknameController(controller);
			if (nickController != null) {
				
			}
			List<String> nxalias = controller.getPropertyValue(PROP_NXALIAS);
			if (nxalias.size() > 0) {
				id = nxalias.get(0);
			} else {
				id = controller.getId();
				if (id.contains("SP")) {
					id = id.replace("SP", "_Setpoint");
				} else {
					id = id.replace("S", "_Sensor");
				}
			}
			List<String> nickname = controller.getPropertyValue(PROP_NICKNAME);
			if (nickname.size() > 0) {
				id += " - " + nickname.get(0);
			}
			List<String> units = controller.getPropertyValue(PROP_UNITS);
			if (units.size() > 0) {
				id += "(" + units.get(0) + ")";
			}
			final SicsRealtimeResource resource = new SicsRealtimeResource(id);
			resource.setFullName(controller.getPath());
			resourceList.add(resource);
		}
		return resourceList;
	}

	public SicsRealtimeResource getResource(String name) {
		IComponentController controller = SicsRealtimeResource.findDevice(name);
		if (controller != null) {
//			String id = controller.getId();
//			if (id.contains("SP")) {
//				id = id.replace("SP", "_Setpoint");
//			} else {
//				id = id.replace("S", "_Sensor");
//			}
			SicsRealtimeResource resource = new SicsRealtimeResource(name);
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
