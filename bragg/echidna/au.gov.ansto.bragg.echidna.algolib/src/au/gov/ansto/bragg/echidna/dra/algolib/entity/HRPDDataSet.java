package au.gov.ansto.bragg.echidna.dra.algolib.entity;

import au.gov.ansto.bragg.common.dra.algolib.model.Scan;

public class HRPDDataSet extends Scan {
	public double[][][] multisampl;
	public double[][] sample;
	public double monSample;
	public double transmissionSample;
	public double[][] emptyCell;
	public double monEmptyCell;
	public double transmissionEmpty;
	public double[][] blocked;
	public double monBlocked;
	public String name;
	public double lambda;
	public double beamX, beamY;
	public double[][] corrected;
	public HRPDDetector detector;
	public double[][] geometry;
	public double[][] stiched;
}
