package au.gov.ansto.bragg.freehep.jas3;

import java.util.Vector;

public class OneDPeakfind {
	

	// check data arrays for sign, max, min and peak
 	private static Vector<Object> dataSign(double[] data, double[] axisVect, double[] resoVect, String fitFunc){

        Vector<Object> ret = new Vector<Object>();
        int n = data.length;

        //
	    double peak=0.0D;       // peak: larger of maximum and any abs(negative minimum)
	    int peaki=-1;// index of above
	    double shift=0.0D;      // shift to make all positive if a mixture of positive and negative
	    double max=data[0];     // maximum
	    int maxi=0;// index of above
	    double min=data[0];     // minimum
	    int mini=0;// index of above
	    int signCheckPos=0;     // number of negative values
	    int signCheckNeg=0;     // number of positive values
	    int signCheckZero=0;    // number of zero values
	    int signFlag=-1;         // 0 all positive; 1 all negative; 2 positive and negative
	    for(int i=0; i<n; i++){
	        if(data[i]>max){
	            max=data[i];
	            maxi=i;
	        }
	        if(data[i]<min){
	            min=data[i];
	            mini=i;
	        }
	        if(data[i]==0.0D)signCheckZero++;
	        if(data[i]>0.0D)signCheckPos++;
	        if(data[i]<0.0D)signCheckNeg++;
	    }
	    if((signCheckZero+signCheckPos)==n){
	        peak=max;
	        peaki=maxi;
	        signFlag=0;
	    }
	    else{
	        if((signCheckZero+signCheckNeg)==n){
	            peak=min;
	            peaki=mini;
	            signFlag=1;
	        }
	        else{
	            peak=max;
	            peaki=maxi;
	            if(-min>max){
	                peak=min;
	                peak=mini;
	            }
	            signFlag=2;
	            shift=-min;
	        }
	    }

	    // transfer results to the Vector
	    ret.addElement(new Double(min));
	    ret.addElement(new Integer(mini));
	    ret.addElement(new Double(max));
	    ret.addElement(new Integer(maxi));
	    ret.addElement(new Double(peak));
	    ret.addElement(new Integer(peaki));
	    ret.addElement(new Integer(signFlag));
	    ret.addElement(new Double(shift));

	    return ret;
	}

}
