package org.gumtree.data.util;

import java.util.concurrent.Semaphore;

/**
 * This class is used to manage access to CDMA in order not to have JVM crashes with not thread-safe
 * plugins.
 * 
 * @author GIRARDOT
 */
public class AccessController {

    private final static Semaphore accessControl = new Semaphore(1, true);

    /**
     * Asks for access
     * 
     * @throws InterruptedException if the current thread is interrupted while waiting for permit
     */
    public static void takeAccess() throws InterruptedException {
        accessControl.acquire();
    }

    /**
     * Releases access
     */
    public static void releaseAccess() {
        accessControl.release();
    }

}
