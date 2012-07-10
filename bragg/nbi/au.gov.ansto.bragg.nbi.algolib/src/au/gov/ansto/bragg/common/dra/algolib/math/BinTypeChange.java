package au.gov.ansto.bragg.common.dra.algolib.math;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.ISliceIterator;

/** This is a utility routine to change from values expressed as bin boundaries
 * to values expressed as bin centres.  The assumption is that the most rapidly
 * varying index corresponds to the bin data, and the higher-order indices are
 * looped over.
 * 
 * @author jrh
 * 
 */
public class BinTypeChange {
	public static IArray ToCentres(IArray inarray) throws InvalidRangeException, ShapeNotMatchException {
		int[] centre_shape = inarray.getShape();  //final shape
		int centre_rank = inarray.getRank();      //final rank
		centre_shape[centre_rank-1]--;
		int iter_rank = centre_rank;
		if(centre_rank>1) iter_rank--;
		IArray centre_array = Factory.createArray(double.class, centre_shape);
		//Create an iterator over the higher dimensions
		ISliceIterator high_dim_iter = inarray.getSliceIterator(iter_rank);
		ISliceIterator c_scan_iter = centre_array.getSliceIterator(iter_rank);
		while(high_dim_iter.hasNext()) {
			IArray c_scan = high_dim_iter.getArrayNext();
			IArray new_scan = c_scan_iter.getArrayNext();
			IArrayIterator c_iter = new_scan.getIterator();
			IArrayIterator o_iter = c_scan.getIterator();
			double bin_lowedge = o_iter.getDoubleNext();    //We get o_iter one extra time
			//note we rely on canonical behaviour, that is, that the iterator will loop over the fastest index first
			double bin_highedge = 0;
			while(c_iter.hasNext()) {
				bin_highedge = o_iter.getDoubleNext();
				c_iter.next().setDoubleCurrent(bin_lowedge + (bin_highedge-bin_lowedge)/2.0);
				bin_lowedge = bin_highedge;                 //set ready for next time through the loop
			}
		}
		return centre_array;
	}

}
