package fr.soleil.nexus;

// Tools lib
import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;

public class NexusFileInstance {
    // / Definitions
    protected static final String PATH_SEPARATOR = "/"; // Node separator in
    // path when having a
    // string representation
    protected final static int RANK_MAX = 32; // Maximum dimension rank

    // / Member attributes
    private String m_sFilePath; // Path to current file
    private NexusFileHandler m_nfFile; // Current file
    private int m_iAccessMode; // The current access mode to the file: read /
    // write
    private static ReentrantLock g_mutex; // Mutex for thread safety
    public static String g_curFile;

    // / Constructors
    protected NexusFileInstance() {
	m_sFilePath = "";
	synchronized (NexusFileInstance.class) {
	    if (g_mutex == null) {
		g_mutex = new ReentrantLock();
		g_curFile = "";
	    }
	}
    }

    protected NexusFileInstance(String sFilePath) {
	m_sFilePath = sFilePath;
	synchronized (NexusFileInstance.class) {
	    if (g_mutex == null) {
		g_mutex = new ReentrantLock();
		g_curFile = "";
	    }
	}
    }

    // / Accessors
    public String getFilePath() {
	return m_sFilePath;
    }

    public void setFile(String sFilePath) {
	m_sFilePath = sFilePath;
	try {
	    closeFile();
	} catch (Throwable t) {
	}
    }

    protected NexusFileHandler getNexusFile() throws NexusException {
	if (m_nfFile == null)
	    throw new NexusException("No file currently opened!");
	return m_nfFile;
    }

    public boolean isFileOpened() {
	return m_nfFile != null;
    }

    /**
     * getFileAccessMode
     * 
     * @return the currently access mode for the file
     * @throws NexusException
     *             if no file opened
     */
    public int getFileAccessMode() throws NexusException {
	if (isFileOpened())
	    return m_iAccessMode;
	else
	    throw new NexusException("No file currently opened!");
    }

    /**
     * finalize Free ressources
     */
    @Override
    protected void finalize() throws Throwable {
	closeFile();
	m_sFilePath = null;
	m_nfFile = null;
    }

    // ---------------------------------------------------------
    // / Protected methods

    // ---------------------------------------------------------
    // / File manipulation
    // ---------------------------------------------------------
    /**
     * openFile
     * 
     * @param sFilePath the full path to reach the NeXus file (including its name) (optionnal)
     * @param iAccesMode the requested acces mode (read and/or write) (optionnal)
     * @note if path isn't specified, the default will be the AcquisitionData
     *       specified one
     * @note if access mode isn't, the default will be read-only
     * @throws NexusException
     */
    public void openFile() throws NexusException {
	openFile(m_sFilePath, NexusFile.NXACC_READ);
    }

    protected void openFile(int iAccessMode) throws NexusException {
	openFile(m_sFilePath, iAccessMode);
    }

    protected void openFile(String sFilePath, int iAccessMode)
	    throws NexusException {
	boolean openFile = false;

	// Try to open file
	m_sFilePath = sFilePath;
	g_mutex.lock();
	g_curFile = m_sFilePath;

	try {
	    // No file yet opened
	    if (null == m_nfFile) {
		openFile = true;
	    } else {
		// Check file isn't opened yet
		String sCurFile = "";
		try {
		    sCurFile = m_nfFile.inquirefile();
		} catch (NexusException ne) {
		    openFile = true;
		}

		// Check file opened is the good one and have correct write
		// access
		if (!sFilePath.equals(sCurFile) || iAccessMode != m_iAccessMode) {
		    m_nfFile.close();
		    openFile = true;
		}
	    }

	    // open the file if needed
	    if (openFile) {
		open(sFilePath, iAccessMode);
		m_iAccessMode = iAccessMode;
	    }

	} catch (NexusException e) {
	    m_nfFile = null;
	    m_sFilePath = "";
	    g_curFile = "";
	    System.out.println("hihihi");
	    e.printStackTrace();
	    g_mutex.unlock();
	    throw e;
	}
    }

    private void open(String sFilePath, int iAccessMode) throws NexusException {
	// Transform path into absolute
	File file = new File(sFilePath);
	sFilePath = file.getAbsolutePath();

	if (NexusFile.NXACC_RDWR == iAccessMode) {

	    if (file.exists()) {
		m_nfFile = new NexusFileHandler(sFilePath, iAccessMode);
	    } else {
		m_nfFile = new NexusFileHandler(sFilePath,
			NexusFile.NXACC_CREATE5);
	    }
	} else if (file.exists()) {
	    m_nfFile = new NexusFileHandler(sFilePath, iAccessMode);
	} else {
	    throw new NexusException("Can't open file for read: " + sFilePath
		    + " doesn't exist!");
	}
	m_iAccessMode = iAccessMode;
    }

    /**
     * closeFile Close the current file, but keep its path so we can easily open
     * it again.
     */
    public void closeFile() throws NexusException {
	try {
	    if (m_nfFile != null) {
		m_nfFile.close();
		m_nfFile.finalize();
	    }

	    m_nfFile = null;

	} catch (Throwable t) {
	    if (g_mutex.isLocked() && g_curFile.equals(m_sFilePath)) {
		if (g_mutex.getHoldCount() - 1 == 0) {
		    g_curFile = "";
		}
		g_mutex.unlock();
	    }
	    if (t instanceof NexusException) {
		throw (NexusException) t;
	    } else {
		t.printStackTrace();
		return;
	    }
	}
	if (g_mutex.isLocked() && g_curFile.equals(m_sFilePath)) {
	    if (g_mutex.getHoldCount() - 1 == 0) {
		g_curFile = "";
	    }

	    g_mutex.unlock();
	}
    }

    // ---------------------------------------------------------
    // / Debugging methods
    // ---------------------------------------------------------
    public static void ShowFreeMem(String lab) {
	System.out.println("Free mem " + Runtime.getRuntime().freeMemory()
		+ " (octet) -> " + lab);
    }
}
