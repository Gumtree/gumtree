/**
 * 
 */
package au.gov.ansto.bragg.nbi.dra.experiment;

import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.region.RegionUtils;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 07/05/2009
 */
public class StatisticCalculation extends ConcreteProcessor {

	Plot statisticInputPlot;
	Boolean statisticStop = false;
	Boolean statisticSkip = false;
	Double regionLeft;
	Double regionRight;
	Double statisticOutput;
	/**
	 * 
	 */
	public StatisticCalculation() {
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.processor.ConcreteProcessor#process()
	 */
	@Override
	public Boolean process() throws Exception {
		if (statisticSkip || statisticInputPlot.getShortName().matches("emptyData"))
			return statisticStop;
		IArray axisArray = statisticInputPlot.getAxisArrayList().get(0);
		IArray dataArray = null;
		dataArray = statisticInputPlot.findSignalArray();
		int[] binBoundary;
		if ((regionLeft == null && regionRight == null) || (regionLeft.isNaN() && regionRight.isNaN())){
			binBoundary = new int[2];
			binBoundary[0] = 0;
			binBoundary[1] = (int) dataArray.getSize();
		}else{
			double[] boundary = new double[2];
			if (regionLeft == null || regionLeft.isNaN()){
				boundary[0] = Double.MIN_VALUE;
				boundary[1] = regionRight;
			}else if (regionRight == null || regionRight.isNaN()){
				boundary[0] = regionLeft;
				boundary[1] = Double.MAX_VALUE;
			}else{
				if (regionLeft <= regionRight){
					boundary[0] = regionLeft;
					boundary[1] = regionRight;
				}else {
					boundary[1] = regionLeft;
					boundary[0] = regionRight;
				}
			}
			binBoundary = RegionUtils.convertBoundary(boundary, axisArray);
			binBoundary[1] = binBoundary[1] < dataArray.getSize() ? 
					binBoundary[1] : (int) dataArray.getSize() - 1;
//			dataArray = statisticInputPlot.findSignalArray().section(new int[]{binBoundary[0]}, 
//					new int[]{binBoundary[1] - binBoundary[0] + 1});
		}
		IIndex index = dataArray.getIndex();
		double maxValue = Double.MIN_VALUE;
		int maxIndex = 0;
		for (int i = binBoundary[0]; i < binBoundary[1]; i ++){
			index.set(i);
			double currentValue = dataArray.getDouble(index);
			if (currentValue > maxValue){
				maxValue = currentValue;
				maxIndex = i;
			}
		}
		statisticOutput = axisArray.getDouble(index.set(maxIndex));
		
		
		return statisticStop;
	}

	/**
	 * @return the calculationOutput
	 */
	public Double getStatisticOutput() {
		return statisticOutput;
	}

	/**
	 * @param statisticInputPlot the statisticInputPlot to set
	 */
	public void setStatisticInputPlot(Plot statisticInputPlot) {
		this.statisticInputPlot = statisticInputPlot;
	}

	/**
	 * @param statisticStop the statisticStop to set
	 */
	public void setStatisticStop(Boolean statisticStop) {
		this.statisticStop = statisticStop;
	}

	/**
	 * @param statisticSkip the statisticSkip to set
	 */
	public void setStatisticSkip(Boolean statisticSkip) {
		this.statisticSkip = statisticSkip;
	}

	/**
	 * @param regionLeft the regionLeft to set
	 */
	public void setRegionLeft(Double regionLeft) {
		this.regionLeft = regionLeft;
	}

	/**
	 * @param regionRight the regionRight to set
	 */
	public void setRegionRight(Double regionRight) {
		this.regionRight = regionRight;
	}

}