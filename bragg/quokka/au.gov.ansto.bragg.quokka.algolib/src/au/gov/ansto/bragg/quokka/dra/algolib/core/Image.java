package au.gov.ansto.bragg.quokka.dra.algolib.core;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.quokka.dra.algolib.core.internal.PowerCentroid;

public class Image {

	private IGroup rawData = null;
	private double[] beamCenter = null;
	private IArray rawDataArray = null;
	private Double detectorDistance = null;
	private Double lambda = null;
	private int[] dimensionSize = null;
	private Pixel[] pixelArray = null;
	private Double QCentroid;
	
	public Image(IGroup data){
		rawData = data;
		try {
			getStorageData();
		} catch (SignalNotAvailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		pixelArray = new Pixel[(int) rawDataArray.getSize()];
	}
	
	private void getStorageData() throws SignalNotAvailableException {
		// TODO Auto-generated method stub
		rawDataArray = ((NcGroup) rawData).getSignalArray();
		dimensionSize = ((NcGroup) rawData).findSignal().getShape();
		IDataItem beamCenterData = rawData.getDataItem("BCENT");
		try{
			beamCenter = (double[]) beamCenterData.getData().getArrayUtils().copyTo1DJavaArray();
			if (beamCenter.length != 2) throw new Exception();
			System.out.println("find beam center at (" + beamCenter[0] + ", " + beamCenter[1] + ")");
		}catch(Exception ex){
			System.out.println("can not find beam center in the data, use default value (96.0, 96.0)");
			beamCenter = new double[2];
			beamCenter[0] = 96.;
			beamCenter[1] = 96.;			
		}
		IDataItem detectorDistanceData = rawData.getDataItem("DET_DIST");
		try {
			detectorDistance = detectorDistanceData.getData().getArrayMath().getMaximum() * 1000;
			System.out.println("find detector distance: " + detectorDistance);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("can not find detector distance in the data, use default value 10(m)");
			detectorDistance = Double.valueOf(10000);
		}
		IDataItem lambdaData = rawData.getDataItem("LAMBDA");
		try {
			lambda = lambdaData.getData().getArrayMath().getMaximum();
			System.out.println("find lambda: " + lambda);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("can not find lambda in the data, use default value 8.4(A)");
			lambda = 8.4;
		}
	}
	
	public Pixel getPixel(int z, int x){
		if (z >= dimensionSize[0] && x >= dimensionSize[1]) return null;
		if (pixelArray[z * dimensionSize[0] + x] == null){
			pixelArray[z * dimensionSize[0] + x] = new Pixel(z, x, this);
		}
		return pixelArray[z * dimensionSize[0] + x];
	}

	public Pixel getPixel(int index){
		if (index >= pixelArray.length) return null;
		int z = 0; 
		int x = 0;
		z = index / dimensionSize[0];
		x = index - z * dimensionSize[0];
		return getPixel(z, x);
	}
	
	public IGroup getRawData() {
		return rawData;
	}

	public double[] getBeamCenter() {
		return beamCenter;
	}
	
	public double getDetectorDistance() {
		return detectorDistance;
	}

	public double getLambda() {
		return lambda;
	}

	public int[] getDimensionSize() {
		return dimensionSize;
	}

	public Pixel[] getPixelArray() {
		return pixelArray;
	}
	
	public double getQCentroid() throws DimensionNotMatchException{
		double[] position = new double[pixelArray.length];
		if (QCentroid == null){
			for(int i = 0; i < pixelArray.length; i++){
				Pixel pixel = getPixel(i);
				position[i] = pixel.getQ();
			}
			IArray positionArray = Factory.createArray(position);
			QCentroid = PowerCentroid.powerCentroid1D(positionArray, this.rawDataArray, 1);
		}
		return QCentroid.doubleValue();
	}

	public IArray getRawDataArray() {
		return rawDataArray;
	}
}
