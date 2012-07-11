package au.gov.ansto.bragg.echidna.dra.algolib.processes;

public class EchidnafileProcess {

	
	/**
	 * This is element wise file addition process. When user applies for mergering  files, one should consider about element weight.
	 *  This algorithm is designed to use as flexiable as possible 
	 * @param datSam1   Two D array (file) is going to merge.
	 * @param datSam2   Two D array (file) is going to merge.
	 * @param weight1     Element weight for array1. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @param weight2     Eelement weight for array2. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @return
	 */
	public double[][] echidnafileAddition (double[][] datSam1, double[][] datSam2, double[][] weight1, double[][] weight2 ){
	
	       if (datSam1.length != datSam2.length
	                || datSam1[0].length != datSam2[0].length) {
	            return null;
	        }
    	   int i, j;
	       int vPixels =datSam1.length;
	       int hPixels =datSam1[0].length;

	       if (weight1 == null) {
	    	   weight1 = new double[vPixels][hPixels];
	    	   for (i = 0; i < vPixels; i++) 
		            for (j = 0; j < hPixels; j++) weight1[i][j] = 1.0;
	       }
	       
	       if (weight2 == null) {
	    	   weight2 = new double[vPixels][hPixels];	
	    	   for (i = 0; i < vPixels; i++) 
		            for (j = 0; j < hPixels; j++) weight2[i][j] = 1.0;
	       }
	       
	        double[][] out = new double[vPixels][hPixels];
	        
	
	        for (i = 0; i < vPixels; i++) {
	            for (j = 0; j < hPixels; j++) {
	            	String stdat1 = String.valueOf(datSam1[i][j]);
	            	String stdat2 = String.valueOf(datSam2[i][j]);
	            	if( stdat1.equals("NaN") || stdat2.equals("NaN")) {
	            		if( stdat1.equals("NaN") && stdat2.equals("NaN"))out[i][j] = Double.NaN;
	            		  else{
	            		    if( stdat1.equals("NaN")) out[i][j] =datSam2[i][j];
	            		    if( stdat2.equals("NaN")) out[i][j] =datSam1[i][j];
	            		  }
	            	}
	            	else   out[i][j] = datSam1[i][j]*weight1[i][j] +  datSam2[i][j]*weight2[i][j];
	            }
	        }
		
		return out;
		
	}
	/**
	 * This is element wise file addition process. When user applies for mergering  files, one should consider about element weight.
	 *  This algorithm is designed to use as flexiable as possible 
	 * @param datSam1   Two D array (file) is going to merge.
	 * @param datSam2   Two D array (file) is going to merge.
	 * @param weight1     Element weight for array1. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @param weight2     Eelement weight for array2. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @return
	 */
	public double[][] echidnafileSubtraction (double[][] datSam1, double[][] datSam2, double[][] weight1, double[][] weight2 ){
	
	       if (datSam1.length != datSam2.length
	                || datSam1[0].length != datSam2[0].length) {
	            return null;
	        }
	       int vPixels =datSam1.length;
	       int hPixels =datSam1[0].length;
	       
	        double[][] out = new double[vPixels][hPixels];
	        
	        int i, j;
	        
		       if (weight1 == null) {
		    	   weight1 = new double[vPixels][hPixels];
		    	   for (i = 0; i < vPixels; i++) 
			            for (j = 0; j < hPixels; j++) weight1[i][j] = 1.0;
		       }
		       
		       if (weight2 == null) {
		    	   weight2 = new double[vPixels][hPixels];
		    	   for (i = 0; i < vPixels; i++) 
			            for (j = 0; j < hPixels; j++) weight2[i][j] = 1.0;
		       }
		       
		       
	        for (i = 0; i < vPixels; i++) {
	            for (j = 0; j < hPixels; j++) {
	            	String stdat1 = String.valueOf(datSam1[i][j]);
	            	String stdat2 = String.valueOf(datSam2[i][j]);
	            	if( stdat1.equals("NaN") || stdat2.equals("NaN")) {
	            		if( stdat1.equals("NaN") && stdat2.equals("NaN"))out[i][j] = Double.NaN;
	            		  else{
	            		    if( stdat1.equals("NaN")) out[i][j] =Double.NaN;
	            		    if( stdat2.equals("NaN")) out[i][j] =datSam1[i][j];
	            		  }
	            	}
	            	else if( datSam1[i][j]*weight1[i][j] < datSam2[i][j]*weight2[i][j]) out[i][j] = 0.0;
	            	        else   out[i][j] = datSam1[i][j]*weight1[i][j] -  datSam2[i][j]*weight2[i][j];
	            }
	        }
		
		
		return out;
		
	}
	/**
	 * This is column or row wise file addition process. When user applies for mergering  files, one should consider about element weight.
	 *  This algorithm is designed to use as flexiable as possible 
	 * @param datSam1   Two D array (file) is going to merge.
	 * @param datSam2   Two D array (file) is going to merge.
	 * @param weight1     Element weight for array1. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @param weight2     Eelement weight for array2. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 *  @param nWise      Column wise or row wise controller   nWise = 0 is column wise and nWise = 1 is row wise merge.                         
	 *                              
	 * @return
	 */
	public double[][] echidnafileAdditioncrw (double[][] datSam1, double[][] datSam2, double[] weight1, double[] weight2, int nWise ){
	       if (datSam1.length != datSam2.length
	                || datSam1[0].length != datSam2[0].length) {
	            return null;
	        }
 
	       int i, j;
	       int vPixels =datSam1.length;
	       int hPixels =datSam1[0].length;

	 if(nWise ==0) {
	       if (weight1 == null) {
	    	   weight1 = new double[hPixels];	 
		            for (j = 0; j < hPixels; j++) weight1[j] = 1.0;
	       }
	       
	       if (weight2 == null) {
	    	   weight2 = new double[hPixels];
	    	   for (j = 0; j < hPixels; j++) 
		             weight2[j]= 1.0;
		    }
	 }
	   else {	       
	    	   if (weight1 == null) {
		    	   weight1 = new double[vPixels];
	    	   for (i = 0; i < vPixels; i++) 
		             weight1[i] = 1.0;
	            }
	       
	       if (weight2 == null) {
	    	   weight2 = new double[vPixels];
	    	   for (i = 0; i < vPixels; i++) 
		             weight2[i]= 1.0;	                   
	       }
        
	   }
	        double[][] out = new double[vPixels][hPixels];
	        
	
	        for (i = 0; i < vPixels; i++) {
	            for (j = 0; j < hPixels; j++) {
	            	String stdat1 = String.valueOf(datSam1[i][j]);
	            	String stdat2 = String.valueOf(datSam2[i][j]);
	            	if( stdat1.equals("NaN") || stdat2.equals("NaN")) {
	            		if( stdat1.equals("NaN") && stdat2.equals("NaN"))out[i][j] = Double.NaN;
	            		  else{
	            		    if( stdat1.equals("NaN")) out[i][j] =datSam2[i][j];
	            		    if( stdat2.equals("NaN")) out[i][j] =datSam1[i][j];
	            		  }
	            	}
	            	else  
	            		if (nWise ==0) out[i][j] = datSam1[i][j]*weight1[i] +  datSam2[i][j]*weight2[i];
	            		else out[i][j] = datSam1[i][j]*weight1[i] +  datSam2[i][j]*weight2[j];
	            }
	        }
		
		
		return out;
		
	}
	/**
	 * This is collumn or row wise file addition process. When user applies for mergering  files, one should consider about element weight.
	 *  This algorithm is designed to use as flexiable as possible 
	 * @param datSam1   Two D array (file) is going to merge.
	 * @param datSam2   Two D array (file) is going to merge.
	 * @param weight1     Element weight for array1. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @param weight2     Eelement weight for array2. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @param nWise      Column wise or row wise controller   nWise = 0 is column wise and nWise = 1 is row wise merge
	 * @return
	 */
	public double[][] echidnafileSubtractioncrw (double[][] datSam1, double[][] datSam2, double[] weight1,
			                                                   double[] weight2, int nWise ){
	       if (datSam1.length != datSam2.length
	                || datSam1[0].length != datSam2[0].length) {
	            return null;
	        }
  	   int i, j;
	       int vPixels =datSam1.length;
	       int hPixels =datSam1[0].length;

	 if(nWise ==0) {
	       if (weight1 == null) {
	    	   weight1 = new double[hPixels];
		            for (j = 0; j < hPixels; j++) weight1[j] = 1.0;
	       }
	       
	       if (weight2 == null) {
	    	   weight2 = new double[hPixels];
		            for (j = 0; j < hPixels; j++) weight2[j]= 1.0;
	       }
	    }
	   else {	       
	    	   if (weight1 == null) {
		    	   weight1 = new double[vPixels];
	    	   for (i = 0; i < vPixels; i++) 
		             weight1[i] = 1.0;
	            }
	       
	       if (weight2 == null) {
	    	   weight2 = new double[vPixels];
	    	   for (i = 0; i < vPixels; i++) 
		            for (j = 0; j < hPixels; j++) weight2[i]= 1.0;
	            }
	       
	       }
       
	       
	        double[][] out = new double[vPixels][hPixels];
	        
	
	        for (i = 0; i < vPixels; i++) {
	            for (j = 0; j < hPixels; j++) {
	            	String stdat1 = String.valueOf(datSam1[i][j]);
	            	String stdat2 = String.valueOf(datSam2[i][j]);
	            	if( stdat1.equals("NaN") || stdat2.equals("NaN")) {
	            		if( stdat1.equals("NaN") && stdat2.equals("NaN"))out[i][j] = Double.NaN;
	            		  else{
	            		    if( stdat1.equals("NaN")) out[i][j] =Double.NaN;
	            		    if( stdat2.equals("NaN")) out[i][j] =datSam1[i][j];
	            		  }
	            	}
	            	else  
	            		if (nWise ==0) out[i][j] = datSam1[i][j]*weight1[i] -  datSam2[i][j]*weight2[i];
	            		else out[i][j] = datSam1[i][j]*weight1[i] -  datSam2[i][j]*weight2[j];
	            }
	        }
		
		
		
		return out;
		
	}
	/**
	 * This is element wise file addition process. When user applies for mergering  files, one should consider about element weight.
	 *  This algorithm is designed to use as flexiable as possible 
	 * @param datSam1   Two D array (file) is going to merge.
	 * @param datSam2   Two D array (file) is going to merge.
	 * @param weight1     Element weight for array1. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @param weight2     Eelement weight for array2. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @return
	 */
	public double[][] echidnafileMultiplication (double[][] datSam1, double[][] datSam2, double[][] weight1, double[][] weight2 ){
	
	       if (datSam1.length != datSam2.length
	                || datSam1[0].length != datSam2[0].length) {
	            return null;
	        }
	       int vPixels =datSam1.length;
	       int hPixels =datSam1[0].length;
	       
	        double[][] out = new double[vPixels][hPixels];
	        
	        int i, j;
	        
// To avoid data set null input, set all elements  as 1.0	        
		       if (datSam1 == null) {
		    	   datSam1 = new double[vPixels][hPixels];
		    	   for (i = 0; i < vPixels; i++) 
			            for (j = 0; j < hPixels; j++) datSam1[i][j] = 1.0;
		       }
		       
		       if (datSam2 == null) {
		    	   datSam2 = new double[vPixels][hPixels];
		    	   for (i = 0; i < vPixels; i++) 
			            for (j = 0; j < hPixels; j++) datSam2[i][j] = 1.0;
		       }	
		       
 // To avoid weight array null input, set all elements  as 1.0	  		       
		       if (weight1 == null) {
		    	   weight1 = new double[vPixels][hPixels];
		    	   for (i = 0; i < vPixels; i++) 
			            for (j = 0; j < hPixels; j++) weight1[i][j] = 1.0;
		       }
		       
		       if (weight2 == null) {
		    	   weight2 = new double[vPixels][hPixels];	
		    	   for (i = 0; i < vPixels; i++) 
			            for (j = 0; j < hPixels; j++) weight2[i][j] = 1.0;
		       }
		       
		       
	        for (i = 0; i < vPixels; i++) {
	            for (j = 0; j < hPixels; j++) {
	            	String stdat1 = String.valueOf(datSam1[i][j]);
	            	String stdat2 = String.valueOf(datSam2[i][j]);
	            	if( stdat1.equals("NaN") || stdat2.equals("NaN")) {
	            		out[i][j] = Double.NaN;	 
	            	}
	            	else   out[i][j] = datSam1[i][j]*weight1[i][j] *  datSam2[i][j]*weight2[i][j];
	            }
	        }
		
		
		return out;
		
	}
	/**
	 * This is collumn or row wise file addition process. When user applies for mergering  files, one should consider about element weight.
	 *  This algorithm is designed to use as flexiable as possible 
	 * @param datSam1   Two D array (file) is going to merge.
	 * @param datSam2   Two D array (file) is going to merge.
	 * @param weight1     Element weight for array1. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @param weight2     Eelement weight for array2. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 *@param nWise      Column wise or row wise controller   nWise = 0 is column wise and nWise = 1 is row wise merge
	 * @return
	 */
	public double[][] echidnafileMultiplicationcrw (double[][] datSam1, double[][] datSam2, double[] weight1, 
			                                                    double[] weight2, int nWise ){
	
	       if (datSam1.length != datSam2.length
	                || datSam1[0].length != datSam2[0].length) {
	            return null;
	        }
 	   int i, j;
	       int vPixels =datSam1.length;
	       int hPixels =datSam1[0].length;

	       // To avoid data set null input, set all elements  as 1.0	        
	       if (datSam1 == null) {
	    	   datSam1 = new double[vPixels][hPixels];
	    	   for (i = 0; i < vPixels; i++) 
		            for (j = 0; j < hPixels; j++) datSam1[i][j] = 1.0;
	       }
	       
	       if (datSam2 == null) {
	    	   datSam2 = new double[vPixels][hPixels];
	    	   for (i = 0; i < vPixels; i++) 
		            for (j = 0; j < hPixels; j++) datSam2[i][j] = 1.0;
	       }

	  	 if(nWise ==0) {
		       if (weight1 == null) {
		    	   weight1 = new double[hPixels];	 
			            for (j = 0; j < hPixels; j++) weight1[j] = 1.0;
		       }
		       
		       if (weight2 == null) {
		    	   weight2 = new double[hPixels];
		    	   for (j = 0; j < hPixels; j++) 
			             weight2[j]= 1.0;
			    }
		 }
		   else {	       
		    	   if (weight1 == null) {
			    	   weight1 = new double[vPixels];
		    	   for (i = 0; i < vPixels; i++) 
			             weight1[i] = 1.0;
		            }
		       
		       if (weight2 == null) {
		    	   weight2 = new double[vPixels];
		    	   for (i = 0; i < vPixels; i++) 
			             weight2[i]= 1.0;	                   
		       }
	        
		   }
      
	       
	        double[][] out = new double[vPixels][hPixels];
	        
	
	        for (i = 0; i < vPixels; i++) {
	            for (j = 0; j < hPixels; j++) {
	            	String stdat1 = String.valueOf(datSam1[i][j]);
	            	String stdat2 = String.valueOf(datSam2[i][j]);
	            	if( stdat1.equals("NaN") || stdat2.equals("NaN")) {
	            		out[i][j] = Double.NaN;
	            	}
	            	else    out[i][j] = datSam1[i][j]*weight1[i] *  datSam2[i][j]*weight2[j];
	            }
	        }
		
		
		
		return out;
		
		
	}
	
	
	/**
	 * This is element wise file addition process. When user applies for mergering  files, one should consider about element weight.
	 *  This algorithm is designed to use as flexiable as possible 
	 * @param datSam1   Two D array (file) is going to merge.
	 * @param retio         Element weight for datSam. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @return
	 */
	public double[][] echidnDataReNorm (double[][] datSam, double ratio ){
	
	       if (datSam == null)   return null;
	 
    	   int i, j;
	       int vPixels =datSam.length;
	       int hPixels =datSam[0].length;

	
	       
	        double[][] out = new double[vPixels][hPixels];
	        
	
	        for (i = 0; i < vPixels; i++) {
	            for (j = 0; j < hPixels; j++) {
	            	String stdat1 = String.valueOf(datSam[i][j]);


	            		    if( stdat1.equals("NaN")) out[i][j] =Double.NaN;
	
	            	else   out[i][j] = datSam[i][j]*ratio;
	            }
	        }
		
		return out;
		
	}
}
