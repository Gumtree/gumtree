package au.gov.ansto.bragg.common.dra.algolib.math;

import java.util.Vector;
import flanagan.analysis.Regression;
import flanagan.analysis.RegressionFunction;
import flanagan.analysis.Stat;
import flanagan.math.Fmath;

public class OpalDataFitter {
	
    private int nData0=0;        		// number of y data points inputted (in a singlew array if multiple y arrays)
    private int nData=0;                // number of y data points (nData0 times the number of y arrays)
    private int nXarrays=1;     		// number of x arrays
    private int nYarrays=1;     		// number of y arrays
    private int nTerms=0;       		// number of unknown parameters to be estimated	
    private boolean weightOpt =false;
    private  double[] xdata = null;
	private  double[] weight;
	private  double[] yData;
    
    public 	OpalDataFitter(double[] xxData, double[] yData, double[] weight){
    this.nData0 = yData.length;
    int n = xxData.length;
    int m = weight.length;
    double[][] xData = new double[1][n];
    for(int i=0; i<n; i++){
        xData[0][i]=xxData[i];
    }

    this.weightOpt=true;
    for(int i=0; i<m; i++){
        if(weight[i]==0.0D){
            this.weightOpt=false;
            System.out.println("a weight in Regression equals zero; all weights set to 1.0");
        }
    }
}

    // Fit data to a Gaussian (normal) probability function
	public float[][] fitGaussian(double[] xxData, double[] yData, double[] weight, boolean output ){
 
	   int lastMethod=4;
	   boolean linNonLin = false;
	   boolean zeroCheck = false;
	   float[][] fittedplot = new float [xxData.length][1];
	   int nTerms=3;
	   int n = xxData.length;
       int degreesOfFreedom=nData - nTerms;
	
       Regression rg = new Regression( xxData,  yData,  weight);
	 
       double[][] xData = new double[1][n];
       for(int i=0; i<n; i++){
           xData[0][i]=xxData[i];
       }
        // check sign of y data
	    Double tempd=null;
	    Vector<Object> retY = dataSign(yData);
	    tempd = (Double)retY.elementAt(4);
	    double yPeak = tempd.doubleValue();
	    boolean yFlag = false;
	    if(yPeak<0.0D){
	        System.out.println("Regression.fitGaussian(): This implementation of the Gaussian distribution takes only positive y values\n(noise taking low values below zero are allowed)");
	        System.out.println("All y values have been multiplied by -1 before fitting");
	        for(int i =0; i<this.nData; i++){
	                yData[i] = -yData[i];
	        }
	        retY = dataSign(yData);
	        yFlag=true;
	    }

	    // Calculate  x value at peak y (estimate of the Gaussian mean)
	    Vector<Object> ret1 = dataSign(yData);
	 	Integer tempi = null;
	    tempi = (Integer)ret1.elementAt(5);
	 	int peaki = tempi.intValue();
	    double mean = xxData[peaki];

	    // Calculate an estimate of the sd
	    double sd = Math.sqrt(2.0D)*halfWidth(xxData, yData);

	    // Calculate estimate of y scale
	    tempd = (Double)ret1.elementAt(4);
	    double ym = tempd.doubleValue();
	    ym=ym*sd*Math.sqrt(2.0D*Math.PI);

        // Fill arrays needed by the Simplex
        double[] start = new double[this.nTerms];
        double[] step = new double[this.nTerms];
        start[0] = mean;
        start[1] = ym;

        step[0] = 0.1D*sd;
        step[1] = 0.1D*start[1];
        if(step[1]==0.0D){
            Vector<Object> ret0 = dataSign(xData[0]);
	 	    Double tempdd = null;
	        tempdd = (Double)ret0.elementAt(2);
	 	    double xmax = tempdd.doubleValue();
	 	    if(xmax==0.0D){
	 	        tempdd = (Double)ret0.elementAt(0);
	 	        xmax = tempdd.doubleValue();
	 	    }
	        step[0]=xmax*0.1D;
	    }

        if(yFlag){
            // restore data
            for(int i=0; i<this.nData-1; i++){
                this.yData[i]=-this.yData[i];
            }
        }
  return fittedplot;
	}	
	
    // check data arrays for sign, max, min and peak
 	private static Vector<Object> dataSign(double[] data){

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
	
    // returns estimate of half-height width
    private static double halfWidth(double[] xData, double[] yData){
        double ymax = yData[0];
        int imax = 0;
        int n = xData.length;

        for(int i=1; i<n; i++){
            if(yData[i]>ymax){
                ymax=yData[i];
                imax=i;
            }
        }
        ymax /= 2.0D;

        double halflow=-1.0D;
        double temp = -1.0D;
        int ihl=-1;
        if(imax>0){
            ihl=imax-1;
            halflow=Math.abs(ymax-yData[ihl]);
            for(int i=imax-2; i>=0; i--){
                temp=Math.abs(ymax-yData[i]);
                if(temp<halflow){
                    halflow=temp;
                    ihl=i;
                }
            }
            halflow=Math.abs(xData[ihl]-xData[imax]);
        }

        double halfhigh=-1.0D;
        temp = -1.0D;
        int ihh=-1;
        if(imax<n-1){
            ihh=imax+1;
            halfhigh=Math.abs(ymax-yData[ihh]);
            for(int i=imax+2; i<n; i++){
                temp=Math.abs(ymax-yData[i]);
                if(temp<halfhigh){
                    halfhigh=temp;
                    ihh=i;
                }
            }
            halfhigh=Math.abs(xData[ihh]-xData[imax]);
        }

        double halfw = 0.0D;
        int nd = 0;
        if(ihl!=-1){
            halfw += halflow;
            nd++;
        }
        if(ihh!=-1){
            halfw += halfhigh;
            nd++;
        }
        halfw /= nd;

        return halfw;
}	
//  CLASSES TO EVALUATE THE SPECIAL FUNCTIONS


//	 Class to evaluate the Gausian function y = (yscale/sd.sqrt(2.pi)).exp(-[(x - xmean)/(2.sd)]^2).
	class GaussianFunction implements RegressionFunction{
	    public boolean scaleOption = true;
	    public double scaleFactor = 1.0D;
	    public double function(double[] p, double[] x){
	        double yScale = scaleFactor;
	        if(scaleOption)yScale = p[2];
	        double y = (yScale/(p[1]*Math.sqrt(2.0D*Math.PI)))*Math.exp(-0.5D*Fmath.square((x[0]-p[0])/p[1]));
	        return y;
	    }
	}

//	 Class to evaluate the Lorentzian function
//	 y = (yscale/pi).(gamma/2)/((x - mu)^2+(gamma/2)^2).
	class LorentzianFunction implements RegressionFunction{
	    public boolean scaleOption = true;
	    public double scaleFactor = 1.0D;

	    public double function(double[] p, double[] x){
	        double yScale = scaleFactor;
	        if(scaleOption)yScale = p[2];
	        double y = (yScale/Math.PI)*(p[1]/2.0D)/(Fmath.square(x[0]-p[0])+Fmath.square(p[1]/2.0D));
	        return y;
	    }
	}

//	 Class to evaluate the Poisson function
//	 y = yscale.(mu^k).exp(-mu)/k!.
	class PoissonFunction implements RegressionFunction{
	    public boolean scaleOption = true;
	    public double scaleFactor = 1.0D;

	    public double function(double[] p, double[] x){
	        double yScale = scaleFactor;
	        if(scaleOption)yScale = p[1];
	        double y = yScale*Math.pow(p[0],x[0])*Math.exp(-p[0])/Stat.factorial(x[0]);
	        return y;
	    }
	}

//	 Class to evaluate the Gumbel function
	class GumbelFunction implements RegressionFunction{
	    public boolean scaleOption = true;
	    public double scaleFactor = 1.0D;
	    public int typeFlag = 0; // set to 0 -> Minimum Mode Gumbel
	                            // reset to 1 -> Maximum Mode Gumbel
	                            // reset to 2 -> one parameter Minimum Mode Gumbel
	                            // reset to 3 -> one parameter Maximum Mode Gumbel
	                            // reset to 4 -> standard Minimum Mode Gumbel
	                            // reset to 5 -> standard Maximum Mode Gumbel

	    public double function(double[] p, double[] x){
	        double y=0.0D;
	        double arg=0.0D;
	        double yScale = scaleFactor;

	        switch(this.typeFlag){
	            case 0:
	            // y = yscale*(1/gamma)*exp((x-mu)/gamma)*exp(-exp((x-mu)/gamma))
	                arg = (x[0]-p[0])/p[1];
	                if(scaleOption)yScale = p[2];
	                y = (yScale/p[1])*Math.exp(arg)*Math.exp(-(Math.exp(arg)));
	                break;
	            case 1:
	            // y = yscale*(1/gamma)*exp((mu-x)/gamma)*exp(-exp((mu-x)/gamma))
	                arg = (p[0]-x[0])/p[1];
	                if(scaleOption)yScale = p[2];
	                y = (yScale/p[1])*Math.exp(arg)*Math.exp(-(Math.exp(arg)));
	                break;
	             case 2:
	            // y = yscale*(1/gamma)*exp((x)/gamma)*exp(-exp((x)/gamma))
	                arg = x[0]/p[0];
	                if(scaleOption)yScale = p[1];
	                y = (yScale/p[0])*Math.exp(arg)*Math.exp(-(Math.exp(arg)));
	                break;
	            case 3:
	            // y = yscale*(1/gamma)*exp((-x)/gamma)*exp(-exp((-x)/gamma))
	                arg = -x[0]/p[0];
	                if(scaleOption)yScale = p[1];
	                y = (yScale/p[0])*Math.exp(arg)*Math.exp(-(Math.exp(arg)));
	                break;
	            case 4:
	            // y = yscale*exp(x)*exp(-exp(x))
	                if(scaleOption)yScale = p[0];
	                y = yScale*Math.exp(x[0])*Math.exp(-(Math.exp(x[0])));
	                break;
	            case 5:
	            // y = yscale*exp(-x)*exp(-exp(-x))
	                if(scaleOption)yScale = p[0];
	                y = yScale*Math.exp(-x[0])*Math.exp(-(Math.exp(-x[0])));
	                break;
	        }
	        return y;
	    }
	}

//	 Class to evaluate the Frechet function
//	 y = yscale.(gamma/sigma)*((x - mu)/sigma)^(-gamma-1)*exp(-((x-mu)/sigma)^-gamma
	class FrechetFunctionOne implements RegressionFunction{
	    public boolean scaleOption = true;
	    public double scaleFactor = 1.0D;
	    public int typeFlag = 0; // set to 0 -> Three Parameter Frechet
	                            // reset to 1 -> Two Parameter Frechet
	                            // reset to 2 -> Standard Frechet

	    public double function(double[] p, double[] x){
	        double y = 0.0D;
	        boolean test = false;
	        double yScale = scaleFactor;

	        switch(typeFlag){
	            case 0: if(x[0]>=p[0]){
	                        double arg = (x[0] - p[0])/p[1];
	                        if(scaleOption)yScale = p[3];
	                        y = yScale*(p[2]/p[1])*Math.pow(arg,-p[2]-1.0D)*Math.exp(-Math.pow(arg,-p[2]));
	                    }
	                    break;
	            case 1: if(x[0]>=0.0D){
	                        double arg = x[0]/p[0];
	                        if(scaleOption)yScale = p[2];
	                        y = yScale*(p[1]/p[0])*Math.pow(arg,-p[1]-1.0D)*Math.exp(-Math.pow(arg,-p[1]));
	                    }
	                    break;
	            case 2: if(x[0]>=0.0D){
	                        double arg = x[0];
	                        if(scaleOption)yScale = p[1];
	                        y = yScale*p[0]*Math.pow(arg,-p[0]-1.0D)*Math.exp(-Math.pow(arg,-p[0]));
	                    }
	                    break;
	        }
	        return y;
	    }
	}

//	 Class to evaluate the semi-linearised Frechet function
//	 log(log(1/(1-Cumulative y) = gamma*log((x-mu)/sigma)
	class FrechetFunctionTwo implements RegressionFunction{

	    public int typeFlag = 0; // set to 0 -> Three Parameter Frechet
	                            // reset to 1 -> Two Parameter Frechet
	                            // reset to 2 -> Standard Frechet

	    public double function(double[] p, double[] x){
	        double y=0.0D;
	        switch(typeFlag){
	            case 0: y = -p[2]*Math.log(Math.abs(x[0]-p[0])/p[1]);
	                    break;
	            case 1: y = -p[1]*Math.log(Math.abs(x[0])/p[0]);
	                    break;
	            case 2: y = -p[0]*Math.log(Math.abs(x[0]));
	                    break;
	        }

	        return y;
	    }
	}

//	 Class to evaluate the Weibull function
//	 y = yscale.(gamma/sigma)*((x - mu)/sigma)^(gamma-1)*exp(-((x-mu)/sigma)^gamma
	class WeibullFunctionOne implements RegressionFunction{
	    public boolean scaleOption = true;
	    public double scaleFactor = 1.0D;
	    public int typeFlag = 0; // set to 0 -> Three Parameter Weibull
	                            // reset to 1 -> Two Parameter Weibull
	                            // reset to 2 -> Standard Weibull

	    public double function(double[] p, double[] x){
	        double y = 0.0D;
	        boolean test = false;
	        double yScale = scaleFactor;

	        switch(typeFlag){
	            case 0: if(x[0]>=p[0]){
	                        double arg = (x[0] - p[0])/p[1];
	                        if(scaleOption)yScale = p[3];
	                        y = yScale*(p[2]/p[1])*Math.pow(arg,p[2]-1.0D)*Math.exp(-Math.pow(arg,p[2]));
	                    }
	                    break;
	            case 1: if(x[0]>=0.0D){
	                        double arg = x[0]/p[0];
	                        if(scaleOption)yScale = p[2];
	                        y = yScale*(p[1]/p[0])*Math.pow(arg,p[1]-1.0D)*Math.exp(-Math.pow(arg,p[1]));
	                    }
	                    break;
	            case 2: if(x[0]>=0.0D){
	                        double arg = x[0];
	                        if(scaleOption)yScale = p[1];
	                        y = yScale*p[0]*Math.pow(arg,p[0]-1.0D)*Math.exp(-Math.pow(arg,p[0]));
	                    }
	                    break;
	        }
	        return y;
	    }
	}

//	 Class to evaluate the semi-linearised Weibull function
//	 log(log(1/(1-Cumulative y) = gamma*log((x-mu)/sigma)
	class WeibullFunctionTwo implements RegressionFunction{

	    public int typeFlag = 0; // set to 0 -> Three Parameter Weibull
	                            // reset to 1 -> Two Parameter Weibull
	                            // reset to 2 -> Standard Weibull

	    public double function(double[] p, double[] x){
	        double y=0.0D;
	        switch(typeFlag){
	            case 0: y = p[2]*Math.log(Math.abs(x[0]-p[0])/p[1]);
	                    break;
	            case 1: y = p[1]*Math.log(Math.abs(x[0])/p[0]);
	                    break;
	            case 2: y = p[0]*Math.log(Math.abs(x[0]));
	            break;
	        }

	        return y;
	    }
	}

//	 Class to evaluate the Rayleigh function
//	 y = (yscale/sigma)*(x/sigma)*exp(-0.5((x-mu)/sigma)^2
	class RayleighFunctionOne implements RegressionFunction{
	    public boolean scaleOption = true;
	    public double scaleFactor = 1.0D;

	    public double function(double[] p, double[] x){
	        double y = 0.0D;
	        boolean test = false;
	        double yScale = scaleFactor;
	        if(scaleOption)yScale = p[1];
	        if(x[0]>=0.0D){
	            double arg = x[0]/p[0];
	            y = (yScale/p[0])*arg*Math.exp(-0.5D*Math.pow(arg,2));
	        }
	        return y;
	    }
	}


//	 Class to evaluate the semi-linearised Rayleigh function
//	 log(1/(1-Cumulative y) = 0.5*(x/sigma)^2
	class RayleighFunctionTwo implements RegressionFunction{

	    public double function(double[] p, double[] x){
	        double y = 0.5D*Math.pow(x[0]/p[0],2);
	        return y;
	    }
	}

	class ExponentialFunction implements RegressionFunction{
	    public boolean scaleOption = true;
	    public double scaleFactor = 1.0D;
	    public int typeFlag = 0; // set to 0 -> Two Parameter Exponential
	                            // reset to 1 -> One Parameter Exponential
	                            // reset to 2 -> Standard Exponential

	    public double function(double[] p, double[] x){
	        double y = 0.0D;
	        boolean test = false;
	        double yScale = scaleFactor;

	        switch(typeFlag){
	            case 0: if(x[0]>=p[0]){
	                        if(scaleOption)yScale = p[2];
	                        double arg = (x[0] - p[0])/p[1];
	                        y = (yScale/p[1])*Math.exp(-arg);
	                    }
	                    break;
	            case 1: if(x[0]>=0.0D){
	                        double arg = x[0]/p[0];
	                        if(scaleOption)yScale = p[1];
	                        y = (yScale/p[0])*Math.exp(-arg);
	                    }
	                    break;
	            case 2: if(x[0]>=0.0D){
	                        double arg = x[0];
	                        if(scaleOption)yScale = p[0];
	                        y = yScale*Math.exp(-arg);
	                    }
	                    break;
	        }
	        return y;
	    }
	}

//	 class to evaluate a Pareto scaled pdf
	class ParetoFunctionOne implements RegressionFunction{
	    public boolean scaleOption = true;
	    public double scaleFactor = 1.0D;
	    public int typeFlag = 0; // set to 0 -> Two Parameter Pareto
	                            // reset to 1 -> One Parameter Pareto

	    public double function(double[] p, double[] x){
	        double y = 0.0D;
	        boolean test = false;
	        double yScale = scaleFactor;

	        switch(typeFlag){
	            case 0: if(x[0]>=p[1]){
	                        if(scaleOption)yScale = p[2];
	                        y = yScale*p[0]*Math.pow(p[1],p[0])/Math.pow(x[0],p[0]+1.0D);
	                    }
	                    break;
	            case 1: if(x[0]>=1.0D){
	                        double arg = x[0]/p[0];
	                        if(scaleOption)yScale = p[1];
	                        y = yScale*p[0]/Math.pow(x[0],p[0]+1.0D);
	                    }
	                    break;
	        }
	        return y;
	    }
	}

//	 class to evaluate a Pareto cdf
	class ParetoFunctionTwo implements RegressionFunction{

	    public int typeFlag = 0; // set to 0 -> Two Parameter Pareto
	                            // reset to 1 -> One Parameter Pareto

	    public double function(double[] p, double[] x){
	        double y = 0.0D;
	        switch(typeFlag){
	            case 0: y = Math.pow(p[1]/x[0],p[0]);
	                    break;
	            case 1: y = Math.pow(1.0D/x[0],p[0]);
	                    break;
	         }
	        return y;
	    }
	}

//	 class to evaluate a Sigmoidal threshold function
	class SigmoidThresholdFunction implements RegressionFunction{
	    public boolean scaleOption = true;
	    public double scaleFactor = 1.0D;

	    public double function(double[] p, double[] x){
	        double yScale = scaleFactor;
	        if(scaleOption)yScale = p[2];
	        double y = yScale/(1.0D + Math.exp(-p[0]*(x[0] - p[1])));
	        return y;
	     }
	}

//	 class to evaluate a Rectangular Hyberbola
	class RectangularHyperbolaFunction implements RegressionFunction{
	    public boolean scaleOption = true;
	    public double scaleFactor = 1.0D;

	    public double function(double[] p, double[] x){
	        double yScale = scaleFactor;
	        if(scaleOption)yScale = p[2];
	        double y = yScale*x[0]/(p[0] + x[0]);
	        return y;
	     }

	}

//	 class to evaluate a scaled Heaviside Step Function
	class StepFunctionFunction implements RegressionFunction{
	    public boolean scaleOption = true;
	    public double scaleFactor = 1.0D;

	    public double function(double[] p, double[] x){
	        double yScale = scaleFactor;
	        if(scaleOption)yScale = p[1];
	        double y = 0.0D;
	        if(x[0]>p[0])y = yScale;
	        return y;
	     }
	}

//	 class to evaluate a Hill or Sips sigmoidal function
	class sigmoidHillSipsFunction implements RegressionFunction{
	    public boolean scaleOption = true;
	    public double scaleFactor = 1.0D;

	    public double function(double[] p, double[] x){
	        double yScale = scaleFactor;
	        if(scaleOption)yScale = p[2];
	        double xterm = Math.pow(x[0],p[1]);
	        double y = yScale*xterm/(Math.pow(p[0], p[1]) + xterm);
	        return y;
	     }
	}

}
