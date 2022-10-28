/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.catalog;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gumtree.data.IFactory;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IDictionary;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.utils.FactoryManager;
import org.gumtree.service.db.CatalogDB;
import org.gumtree.service.db.ControlDB;
import org.gumtree.service.db.ProposalDB;
import org.gumtree.service.db.RecordsFileException;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author nxi
 *
 */
public class CatalogCollectingService {

	private static final int CATALOG_COLLECTING_INTERVAL = 10000;
	private static final String PROP_SICS_DATAFOLDER = "gumtree.sics.dataPath";
	private static final String PROP_ANALYSIS_DICTIONARY = "gumtree.analysis.dictionary";
	private static final String PROP_CATALOG_TITLES = "gumtree.catalog.columnTitles";
	private static final String PREF_DBKEY_LASTMODIFIED = "catalog_lastModified";
	private static Thread collectingThread;
	private static Logger logger = LoggerFactory.getLogger(CatalogCollectingService.class);
	
	private File dataFolder;
	private long lastModifiedTimestamp;
	private IFactory dataFactory;
	private IDictionary dataDictionary;
	private List<String> catalogColumns;
	private ProposalDB proposalDb;
	private ControlDB controlDb;
	
	/**
	 * 
	 */
	public CatalogCollectingService() {
			dataFolder = new File(System.getProperty(PROP_SICS_DATAFOLDER));
			if (!dataFolder.exists() || !dataFolder.isDirectory()) {
				dataFolder = null;
			}
			dataFactory = (new FactoryManager()).getFactory();
			String dictPath = System.getProperty(PROP_ANALYSIS_DICTIONARY);
			if (dictPath != null) {
				try {
					dataDictionary = dataFactory.openDictionary(dictPath);
				} catch (FileAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			catalogColumns = Arrays.asList(System.getProperty(PROP_CATALOG_TITLES).split(","));
			proposalDb = ProposalDB.getInstance();
			controlDb = ControlDB.getInstance();
			try {
				String savedValue = controlDb.getControlEntry(PREF_DBKEY_LASTMODIFIED);
				if (savedValue != null) {
					lastModifiedTimestamp = Long.valueOf(savedValue);
				}
			} catch (Exception e) {
			}
			startCollectingThread();
	}

	private synchronized void startCollectingThread() {
		if (collectingThread == null) {

			collectingThread = new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						try {
							Thread.sleep(CATALOG_COLLECTING_INTERVAL);
						} catch (InterruptedException e) {
							break;
						}
						try {
							collectFiles();
						} catch (Exception e) {
							logger.error("failed to collect data files: " + e.getMessage());
						}
					}
				}

			});
			collectingThread.start();
		}
	}

	private void collectFiles() throws FileAccessException, IOException {
		if (dataFolder != null) {
			File[] newFiles = dataFolder.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					if (file.isFile() && file.lastModified() > lastModifiedTimestamp) {
						String name = file.getName();
						if (name.length() == 17 && name.endsWith(".nx.hdf")) {
							return true;
						}
					}
					return false;
				}
			});
			Arrays.sort(newFiles);
			for (File file : newFiles) {
				try {
					logger.error(file.getPath());
					updateCatalog(file);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}
		
	}

	private void updateCatalog(File file) throws FileAccessException, IOException, ClassNotFoundException, RecordsFileException, JSONException {
		IDataset ds = null;
		try {
			ds = dataFactory.openDataset(file.toURI());
			IGroup root = ds.getRootGroup();
			root.setDictionary(dataDictionary);
			Map<String, String> dict = new HashMap<String, String>();
			for (String column : catalogColumns) {
				String[] pair = column.split(":");
				if (pair[0].trim() != "#") {
					IDataItem item = root.findDataItem(pair[0]);
					if (item != null) {
						IArray data = item.getData();
						String val = "";
						Class<?> type = data.getElementType();
						if (type == char.class || type == String.class) {
							val = data.toString();
						} else {
							if (data.getSize() > 1) {
								val = data.getIterator().getObjectNext().toString();
							} else {
								val = data.toString();
							}
						}
						dict.put(pair[1], val);
					} else {
						dict.put(pair[1], "");
					}
				}
			}
			String sessionId = controlDb.getCurrentSessionId();
			String currentProposal = proposalDb.findProposalId(sessionId);
			CatalogDB catalogDb = CatalogDB.getInstance(currentProposal);
			catalogDb.updateEntry(file.getName(), dict);
			lastModifiedTimestamp = file.lastModified();
			controlDb.addControlEntry(PREF_DBKEY_LASTMODIFIED, String.valueOf(lastModifiedTimestamp));
		} finally {
			if (ds != null) {
				ds.close();
			}
		}
	}

	public void dispose() {
		collectingThread.interrupt();
	}
		
}
