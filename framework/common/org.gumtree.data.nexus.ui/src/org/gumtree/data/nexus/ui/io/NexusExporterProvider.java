/**
 * 
 */
package org.gumtree.data.nexus.ui.io;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.vis.interfaces.IExporter;
import org.gumtree.vis.interfaces.IExporterProvider;

/**
 * @author nxi
 *
 */
public class NexusExporterProvider implements IExporterProvider {

	@Override
	public List<IExporter> getExporters() {
		List<IExporter> exporters = new ArrayList<IExporter>();
		exporters.add(new BinExporter());
		exporters.add(new GsasExporter());
		exporters.add(new HdfExporter());
		exporters.add(new NakedXYSigmaExporter());
		exporters.add(new PdCIFExporter());
		exporters.add(new SansExporter());
		exporters.add(new TextExporter());
		exporters.add(new XMLExporter());
		exporters.add(new XYSigmaExporter());
		return exporters;
	}

}
