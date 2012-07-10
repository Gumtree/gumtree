/**
 * 
 */
package au.gov.ansto.bragg.nbi.dra.source;

import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.process.exception.NullSignalException;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 *
 */
public class FrameCollection extends ConcreteProcessor {

	public enum ProcessType{
		ADDSINGLE,
		RUNSINGLE,
		RUNALL;
	};
	
	private ProcessType processType = ProcessType.RUNALL;
	private boolean isNewDataSource = true;
	
	private Integer setableFrameIndex = 0;
	private Integer currentFrameIndex = 0;
	private IGroup inputFrameSetPlot;
	private Plot outputFramePlot;
	private IGroup loopToAllOut;
	private IDataItem scanAxis;
	
	/**
	 * 
	 */
	public FrameCollection() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Boolean process() throws Exception{
		if (inputFrameSetPlot == null)
			throw new NullSignalException("Input is missing");
		
		return false;
	}

	/**
	 * @return the currentFrameIndex
	 */
	public Integer getCurrentFrameIndex() {
		return currentFrameIndex;
	}

	/**
	 * @return the outputFramePlot
	 */
	public Plot getOutputFramePlot() {
		return outputFramePlot;
	}

	/**
	 * @return the loopToAllOut
	 */
	public IGroup getLoopToAllOut() {
		return loopToAllOut;
	}

	/**
	 * @param setableFrameIndex the setableFrameIndex to set
	 */
	public void setSetableFrameIndex(Integer setableFrameIndex) {
		if (setableFrameIndex != this.setableFrameIndex){
			if (inputFrameSetPlot == null){
				processType = ProcessType.ADDSINGLE;
			}else{
				try {
					int[] shape = ((NcGroup) inputFrameSetPlot).getSignalArray().getShape();					
					if (shape.length <= 2)
						processType = ProcessType.ADDSINGLE;
					else if (shape.length == 3){
						if (shape[0] <= setableFrameIndex)
							processType = ProcessType.ADDSINGLE;
						else
							processType = ProcessType.RUNSINGLE;
					}else if (shape.length == 4){
						if (shape[0] * shape[1] <= setableFrameIndex)
							processType = ProcessType.ADDSINGLE;
						else
							processType = ProcessType.RUNSINGLE;
					}
						
				} catch (SignalNotAvailableException e) {
					e.printStackTrace();
				}
			}
			this.setableFrameIndex = setableFrameIndex;
			currentFrameIndex = setableFrameIndex;
		}
	}

	/**
	 * @param inputFrameSetPlot the inputFrameSetPlot to set
	 */
	public void setInputFrameSetPlot(IGroup inputFrameSetPlot) {
		if (this.inputFrameSetPlot != inputFrameSetPlot){
			this.inputFrameSetPlot = inputFrameSetPlot;
			processType = ProcessType.RUNALL;
			
			currentFrameIndex = 0;
		}
	}

	/**
	 * @return the scanAxis
	 */
	public IDataItem getScanAxis() {
		return scanAxis;
	}


}
