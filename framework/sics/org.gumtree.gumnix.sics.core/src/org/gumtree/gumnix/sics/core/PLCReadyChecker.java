package org.gumtree.gumnix.sics.core;

import java.net.URI;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.service.directory.IDirectoryService;

public class PLCReadyChecker implements IInstrumentReadyCriterion {

	public IInstrumentReadyStatus checkInstrumentReady() {
		IDataAccessManager dam = ServiceUtils.getService(IDataAccessManager.class);
		try  {
			String readyStatus = dam.get(URI.create("sics://hdb/instrument/status/ready"), String.class);
			if (Boolean.valueOf(readyStatus)) {
				// PLC is ready
				return InstrumentReadyStatus.READY_STATUS;
			} else {
				// PLC is not ready
				IDirectoryService directoryService = ServiceUtils.getService(IDirectoryService.class);
				directoryService.bind("shutterStatusCheckFailed", true);
				return new InstrumentReadyStatus(false, "Instrument PLC is not ready");
			}
		} catch (Exception e) {
		}
		// Missing PLC info, assume the instrument is ready
		return InstrumentReadyStatus.READY_STATUS;
	}

}
