package org.gumtree.cs.sics.core;

import static org.junit.Assert.assertTrue;

import org.gumtree.service.persistence.IObjectContainerManager;
import org.gumtree.service.persistence.support.ObjectContainerManager;
import org.gumtree.sics.core.ISicsPersistenceManager;
import org.gumtree.sics.core.support.SicsPersistenceManager;
import org.gumtree.sics.io.ISicsData;
import org.gumtree.sics.io.SicsData;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

public class SicsPersistenceManagerTest {

	@Test
	public void testStore() throws JSONException {
//		ISicsPersistenceManager persistenceManager = new SicsPersistenceManager();
//		IObjectContainerManager containerManager = new ObjectContainerManager(); 
//		persistenceManager.setContainerManager(containerManager);
//		
//		// Create ans store data
//		JSONObject data = new JSONObject().putOnce("data", "123");
//		ISicsData replyData = new SicsData(data);
//		persistenceManager.store(replyData);
//		
//		// Find data
//		ObjectContainer container = containerManager.getObjectContainer("sics_new");
//		ObjectSet<ISicsData> objectSet = container.query(ISicsData.class);
//		assertTrue(objectSet.contains(replyData));
//		
//		containerManager.disposeObject();
//		persistenceManager.disposeObject();
	}
	
}
