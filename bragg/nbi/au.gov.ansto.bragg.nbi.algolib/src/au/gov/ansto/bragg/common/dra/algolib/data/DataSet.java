package au.gov.ansto.bragg.common.dra.algolib.data;

//import org.gumtree.data.gtd.GTDGroup;
//import org.gumtree.data.gtd.GTDItem;
/**
 * An abstract superclass of the data sets to be used internally
 * by the DRA.
 * @author hrz
 *
 */
public abstract class DataSet {

	public double[][] sample;
	/**
	 * Adds this data set to a GTD. This will involve some
	 * conversion.
	 * @param parent The group to put the data in.
	 * @return The item that was created.
	 */
//	public abstract GTDItem toGTD(GTDGroup parent);
	public double beamX;
	public double beamY;
}
