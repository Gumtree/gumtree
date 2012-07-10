package org.gumtree.service.notification;

import static org.junit.Assert.assertNotSame;

import org.gumtree.service.notification.support.LogNotification;
import org.junit.Test;

public class LogNotificationTest {

	@Test
	public void testEqual() throws InterruptedException {
		LogNotification notification1 = new LogNotification();
		notification1.setMessage("123");
	
		LogNotification notification2 = new LogNotification();		
		notification2.setMessage("abc");
		
		// Should not be equal (message is different)
		assertNotSame(notification1, notification2);
		
		Thread.sleep(50);
		
		LogNotification notification3 = new LogNotification();
		notification3.setMessage("123");
		
		// Should not be equal (timestamp is different)
		assertNotSame(notification1, notification3);		
	}
	
}
