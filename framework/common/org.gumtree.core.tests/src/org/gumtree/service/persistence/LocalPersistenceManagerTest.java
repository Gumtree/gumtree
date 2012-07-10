package org.gumtree.service.persistence;

import static org.junit.Assert.assertNotNull;

import org.gumtree.core.service.ServiceManager;
import org.gumtree.service.persistence.support.LocalPersistenceManager;
import org.gumtree.service.persistence.support.ObjectContainerManager;
import org.junit.Test;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.osgi.Db4oServiceImpl;
import com.db4o.query.Query;

public class LocalPersistenceManagerTest {

	@Test
	public void testInitialisation() {
		ObjectContainerManager objectContainerManager = new ObjectContainerManager();
		objectContainerManager.setDb4oService(new Db4oServiceImpl());
		objectContainerManager.setServiceManager(new ServiceManager());
		LocalPersistenceManager manager = new LocalPersistenceManager();
		manager.setObjectContainerManager(objectContainerManager);
		ObjectContainer container = manager.getObjectContainer();
		assertNotNull(container);
	}
	
	@Test
	public void testDatabase() {
		ObjectContainerManager objectContainerManager = new ObjectContainerManager();
		objectContainerManager.setDb4oService(new Db4oServiceImpl());
		objectContainerManager.setServiceManager(new ServiceManager());
		LocalPersistenceManager manager = new LocalPersistenceManager();
		manager.setObjectContainerManager(objectContainerManager);
		ObjectContainer container = objectContainerManager.createObjectContainer("test", true);
//		File databaseFile = new File("test.yap");
//		ObjectContainer container = Db4o.openFile(databaseFile.getAbsolutePath());
		
		PersistentEntry entry = new PersistentEntry("123", "456");
		container.store(entry);
		container.commit();
		
		Query query = container.query();
		query.constrain(PersistentEntry.class);
		query.descend("key").constrain("123");
		ObjectSet<PersistentEntry> set = query.execute();
		for (PersistentEntry result : set) {
			System.out.println(result);
		}
		objectContainerManager.removeObjectContainer("test", true);
	}
	
}
