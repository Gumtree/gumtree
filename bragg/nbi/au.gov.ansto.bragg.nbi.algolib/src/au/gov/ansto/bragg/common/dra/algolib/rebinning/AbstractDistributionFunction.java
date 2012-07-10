package au.gov.ansto.bragg.common.dra.algolib.rebinning;

public abstract class AbstractDistributionFunction implements DistributionFunction {

	public double getProportion(double intervalStart, double intervalEnd,
			double division) {

		// Divide area between start and division by total area to
		// get the proportion.
		
		Interval completeInterval = new Interval(intervalStart, intervalEnd);
		Interval subInterval = new Interval(intervalStart, division);
		
		return getProportion(completeInterval, subInterval);
	}
	
	public double getProportion(Interval completeInterval, Interval innerInterval) {
		// Check that the inner interval is contained within the larger 
		// interval.
		if (!completeInterval.contains(innerInterval)) {
			String errorMessage = "innerInterval must be a subset of completeInterval.";
			throw new IllegalArgumentException(errorMessage);
		}
		
		double totalArea = getArea(completeInterval);
		double subArea = getArea(innerInterval);
		
		return subArea / totalArea;
	}

}
