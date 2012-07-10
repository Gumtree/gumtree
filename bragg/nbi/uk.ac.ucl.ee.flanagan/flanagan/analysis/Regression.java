/*
*   Class Regression
*
*   Contains methods for simple linear regression
*   (straight line), for multiple linear regression,
*   for fitting data to a polynomial, for non-linear
*   regression (Nelder and Mead Simplex method),
*   and for fitting data to a Gaussian distribution
*   a Lorentzian distribution, a Poisson distribution,
*   a Gumbel distribution, a Frechet distrubution,
*   a Weibull distribution, an Exponential distribution,
*   a Rayleigh distribution,a Pareto distribution.
*   a rectangular hyberbola, a sigmoid threshold function
*   a x^n/(theta^n + x^n) sigmoid function, and a scaled
*   Heaviside step function.
*
*   The sum of squares function needed by the
*   non-linear regression methods is supplied by
*   means of the interface, RegressionFunction
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	    February 2002
*   MODIFIED:   7 January 2006,  28 July 2006, 9 August 2006
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   Regression.html
*
*   Copyright (c) February 2002, August 2006
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package flanagan.analysis;

import java.util.*;
import javax.swing.JOptionPane;
import flanagan.math.*;
import flanagan.io.FileOutput;
import flanagan.plot.Plot;
import flanagan.plot.PlotGraph;
import flanagan.integration.Integration;
import flanagan.integration.IntegralFunction;
import flanagan.analysis.*;

// Regression class
public class Regression{

    private int nData0=0;        		// number of y data points inputted (in a singlew array if multiple y arrays)
    private int nData=0;                // number of y data points (nData0 times the number of y arrays)
    private int nXarrays=1;     		// number of x arrays
    private int nYarrays=1;     		// number of y arrays
    private int nTerms=0;       		// number of unknown parameters to be estimated
                                    		//  multiple linear (a + b.x1 +c.x2 + . . ., = nXarrays + 1
                                    		//  polynomial fitting; = polynomial degree + 1
                                    		//  generalised linear; = nXarrays
                                    		//  simplex = no. of parameters to be estimated
    private int degreesOfFreedom=0; 		// degrees of freedom = nData - nTerms
    private double[][]  xData=null;      	// x  data values
    private double[]    yData=null;      	// y  data values
    private double[]    yCalc=null;      	// calculated y values using the regrssion coefficients
    private double[]    weight=null;     	// weighting factors
    private double[]    residual=null;   	// residuals
    private double[]    residualW=null;  	// weighted residuals
    private boolean     weightOpt=false;  	// weighting factor option
                                            	// = true; weights supplied
                                            	// = false; weigths set to unity in regression
                                            	//          average error used in statistacal methods
                                            	// if any weight[i] = zero,
                                            	//                    weighOpt is set to false and
                                            	//                    all weights set to unity
    private Vector<Double>  best = new Vector<Double>();    // best estimates vector of the unknown parameters
    private Vector<Double>  bestSd = new Vector<Double>(); 	// standard deviation estimates of the best estimates of the unknown parameters
	private double[]pseudoSd = null;        // Pseodo-nonlinear sd
    private double  chiSquare=-10.0D;       // chi  square (observed-calculated)^2/variance
    private double  reducedChiSquare=-10.0D;// reduced chi square
    private double  sumOfSquares=-10.0D;    // Sum of the squares of the residuals
    private double  lastSSnoConstraint=0.0D;// Last sum of the squares of the residuals with no constraint penalty
	private double[][]  covar=null;         // Covariance matrix
	private double[][]  corrCoeff=null;     // Correlation coefficient matrix
	private double sampleR = -10.0D;        // Sample linear correlation coefficient
	                                        // if nXarrays = 1 it is the linear or 'linear approximation'  correlation coefficient
	                                        // if nXarrays > 1 it is the multiple correlation coefficient
	private double sampleR2 = -10.0D;       // Sample coefficient of determination
	                                        // sampleR2 = sampleR*sampleR
	private double multipleF = -10.0D;      // Multiple correlation coefficient F ratio

	private String[] paraName = null;   	// names of parameters, eg, mean, sd; c[0], c[1], c[2] . . .
	private int prec = 4;               	// number of places to which double variables are truncated on output to text files
	private int field = 13;             	// field width on output to text files

    private int lastMethod=-1;          	// code indicating the last regression procedure attempted
                                            	// = 0 multiple linear regression, y = a + b.x1 +c.x2 . . .
                                            	// = 1 polynomial fitting, y = a +b.x +c.x^2 . . .
                                            	// = 2 generalised multiple linear y = a.f1(x) + b.f2(x) . . .
                                            	// = 3 Nelder and Mead simplex
                                            	// = 4 Fit to a Gaussian distribution
                                             	// = 5 Fit to a Lorentzian distribution
                                                // = 6 Fit to a Poisson distribution
                                            	// = 7 Fit to a Two Parameter Gumbel distribution (minimum order statistic)
                                            	// = 8 Fit to a Two Parameter Gumbel distribution (maximum order statistic)
                                            	// = 9 Fit to a One Parameter Gumbel distribution (minimum order statistic)
                                            	// = 10 Fit to One Parameter Gumbel distribution (maximum order statistic)
                                            	// = 11 Fit to a Standard Gumbel distribution (minimum order statistic)
                                           	    // = 12 Fit to a Standard Gumbel distribution (maximum order statistic)
                                                // = 13 Fit to a Three parameter Frechet distribution
                                                // = 14 Fit to a Two Parameter Frechet distribution
                                                // = 15 Fit to a Standard Frechet distribution
                                                // = 16 Fit to a Three parameter Weibull distribution
                                                // = 17 Fit to a Two Parameter Weibull distribution
                                                // = 18 Fit to a Standard Weibull distribution
                                                // = 19 Fit to a Two Parameter Exponential distribution
                                                // = 20 Fit to a One Parameter Exponential distribution
                                                // = 21 Fit to a Standard Parameter Exponential distribution
                                                // = 22 Fit to a Rayleigh distribution
                                                // = 23 Fit to a Two Parameter Pareto distribution
                                                // = 24 Fit to a One Parameter Pareto distribution
                                                // = 25 Fit to a Sigmoidal Threshold Function
                                                // = 26 Fit to a rectangular Hyperbola
                                                // = 27 Fit to a scaled Heaviside Step Function
                                                // = 28 Fit to a Hills/Sips Sigmoid

    private boolean frechetWeibull = true;      // Frechet Weibull switch - if true Frechet, if false Weibull
    private boolean linNonLin = true;           // if true linear method, if false non-linear method
    private boolean trueFreq = false;   	    // true if xData values are true frequencies, e.g. in a fit to Gaussian
                                        	    // false if not
                                        	    // if true chiSquarePoisson (see above) is also calculated
    private String xLegend = "x axis values";   // x axis legend in X-Y plot
    private String yLegend = "y axis values";   // y axis legend in X-Y plot
    private String graphTitle = " ";            // user supplied graph title
    private boolean legendCheck = false;        // = true if above legends overwritten by user supplied legends
    private boolean supressPrint = false;       // = true if print results is to be supressed
    private boolean supressYYplot= false;       // = true if plot of experimental versus calculated is to be supressed


    // Non-linear members
    private boolean nlrStatus=true; 	// Status of non-linear regression on exiting regression method
                                		// = true  -  convergence criterion was met
                                		// = false -  convergence criterion not met - current estimates returned
    private int scaleOpt=0;     		//  if = 0; no scaling of initial estimates
                                		//  if = 1; initial simplex estimates scaled to unity
                                		//  if = 2; initial estimates scaled by user provided values in scale[]
                                		//  (default = 0)
    private double[] scale = null;  	// values to scale initial estimate (see scaleOpt above)
    private boolean zeroCheck = false; 	// true if any best estimate value is zero
                                       		// if true the scale factor replaces the best estimate in numerical differentiation
    private boolean penalty = false; 	    // true if single parameter penalty function is included
    private boolean sumPenalty = false; 	// true if multiple parameter penalty function is included
    private int nConstraints = 0; 		    // number of single parameter constraints
    private int nSumConstraints = 0; 		// number of multiple parameter constraints
    private Vector<Object> penalties = new Vector<Object>();// 3 method index,
                                                            // number of single parameter constraints,
                                                            // then repeated for each constraint:
                                                            //  penalty parameter index,
                                                            //  below or above constraint flag,
                                                            //  constraint boundary value
    private Vector<Object> sumPenalties = new Vector<Object>();// constraint method index,
                                                            // number of multiple parameter constraints,
                                                            // then repeated for each constraint:
                                                            //  number of parameters in summation
                                                            //  penalty parameter indices,
                                                            //  summation signs
                                                            //  below or above constraint flag,
                                                            //  constraint boundary value
    private int[] penaltyCheck = null;  	// = -1 values below the single constraint boundary not allowed
                                        	// = +1 values above the single constraint boundary not allowed
    private int[] sumPenaltyCheck = null;  	// = -1 values below the multiple constraint boundary not allowed
                                        	// = +1 values above the multiple constraint boundary not allowed
    private double penaltyWeight = 1.0e30;  // weight for the penalty functions
    private int[] penaltyParam = null;   	// indices of paramaters subject to single parameter constraint
    private int[][] sumPenaltyParam = null; // indices of paramaters subject to multiple parameter constraint
    private int[][] sumPlusOrMinus = null;  // sign before each parameter in multiple parameter summation
    private int[] sumPenaltyNumber = null;  // number of paramaters in each multiple parameter constraint

    private double[] constraints = null; 	// single parameter constraint values
    private double[] sumConstraints = null; // multiple parameter constraint values
    private int constraintMethod = 0;       // constraint method number
                                            // =0: cliff to the power two (only method at present)

    private boolean scaleFlag = true;   //  if true ordinate scale factor, Ao, included as unknown in fitting to special functions
                                        //  if false Ao set to unity (default value) or user provided value (in yScaleFactor)
    private double yScaleFactor = 1.0D; //  y axis factor - set if scaleFlag (above) = false
    private int nMax = 3000;    		//  Nelder and Mead simplex maximum number of iterations
    private int nIter = 0;      		//  Nelder and Mead simplex number of iterations performed
    private int konvge = 3;     		//  Nelder and Mead simplex number of restarts allowed
    private int kRestart = 0;       	//  Nelder and Mead simplex number of restarts taken
    private double fMin = -1.0D;    	//  Nelder and Mead simplex minimum value
    private double fTol = 1e-9;     	//  Nelder and Mead simplex convergence tolerance
    private double rCoeff = 1.0D;   	//  Nelder and Mead simplex reflection coefficient
    private double eCoeff = 2.0D;   	//  Nelder and Mead simplex extension coefficient
    private double cCoeff = 0.5D;   	//  Nelder and Mead simplex contraction coefficient
    private double[] startH = null; 	//  Nelder and Mead simplex initial estimates
    private double[] step = null;   	//  Nelder and Mead simplex step values
    private double dStep = 0.5D;    	// Nelder and Mead simplex default step value
    private double[][] grad = null; 	// Non-linear regression gradients
	private double delta = 1e-4;    	// Fractional step in numerical differentiation
	private boolean invertFlag=true; 	// Hessian Matrix ('linear' non-linear statistics) check
	                                 	// true matrix successfully inverted, false inversion failed
	private boolean posVarFlag=true; 	// Hessian Matrix ('linear' non-linear statistics) check
	                                 	// true - all variances are positive; false - at least one is negative
    private int minTest = 0;    		// Nelder and Mead minimum test
                                		//  = 0; tests simplex sd < fTol
                                		//  = 1; tests reduced chi suare or sum of squares < mean of abs(y values)*fTol
    private double simplexSd = 0.0D;    	// simplex standard deviation
    private boolean statFlag = true;    	// if true - statistical method called
                                        	// if false - no statistical analysis
    private boolean plotOpt = true;     // if true - plot of calculated values is cubic spline interpolation between the calculated values
                                            // if false - calculated values linked by straight lines (accomodates Poiwsson distribution plots)
    private boolean multipleY = false;  // = true if y variable consists of more than set of data each needing a different calculation in RegressionFunction
                                        // when set to true - the index of the y value is passed to the function in Regression function


    //Constructors
    // Constructor with data with x as 2D array and weights provided
    public Regression(double[][] xData, double[] yData, double[] weight){

        int n=weight.length;
        this.nData0 = yData.length;
        this.weightOpt=true;
        for(int i=0; i<n; i++){
            if(weight[i]==0.0D){
                this.weightOpt=false;
                System.out.println("a weight in Regression equals zero; all weights set to 1.0");
            }
        }
        setDefaultValues(xData, yData, weight);
	}

	// Constructor with data with x and y as 2D arrays and weights provided
    public Regression(double[][] xxData, double[][] yyData, double[][] wWeight){
        this.multipleY = true;
        int nY1 = yyData.length;
        this.nYarrays = nY1;
        int nY2 = yyData[0].length;
        this.nData0 = nY2;
        int nX1 = xxData.length;
        int nX2 = xxData[0].length;
        double[] yData = new double[nY1*nY2];
        double[] weight = new double[nY1*nY2];
        double[][] xData = new double[nY1*nY2][nX1];
        int ii=0;
        for(int i=0; i<nY1; i++){
            int nY = yyData[i].length;
            if(nY!=nY2)throw new IllegalArgumentException("multiple y arrays must be of the same length");
            int nX = xxData[i].length;
            if(nY!=nX)throw new IllegalArgumentException("multiple y arrays must be of the same length as the x array length");
            for(int j=0; j<nY2; j++){
                yData[ii] = yyData[i][j];
                xData[ii][i] = xxData[i][j];
                weight[ii] = wWeight[i][j];
                ii++;
            }
        }

        int n=weight.length;
        this.weightOpt=true;
        for(int i=0; i<n; i++){
            if(weight[i]==0.0D){
                this.weightOpt=false;
                System.out.println("a weight in Regression equals zero; all weights set to 1.0");
            }
        }
        setDefaultValues(xData, yData, weight);
	}

	// Constructor with data with x as 1D array and weights provided
    public Regression(double[] xxData, double[] yData, double[] weight){
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
        setDefaultValues(xData, yData, weight);
	}

	// Constructor with data with x as 1D array and y as 2D array and weights provided
    public Regression(double[] xxData, double[][] yyData, double[][] wWeight){

        this.multipleY = true;
        int nY1 = yyData.length;
        this.nYarrays = nY1;
        int nY2= yyData[0].length;
        this.nData0 = nY2;
        double[] yData = new double[nY1*nY2];
        double[] weight = new double[nY1*nY2];
        int ii=0;
        for(int i=0; i<nY1; i++){
            int nY = yyData[i].length;
            if(nY!=nY2)throw new IllegalArgumentException("multiple y arrays must be of the same length");
            for(int j=0; j<nY2; j++){
                yData[ii] = yyData[i][j];
                weight[ii] = wWeight[i][j];
                ii++;
            }
        }
        int n = xxData.length;
        if(n!=nY2)throw new IllegalArgumentException("x and y data lengths must be the same");
        double[][] xData = new double[1][nY1*n];
        ii=0;
        for(int j=0; j<nY1; j++){
            for(int i=0; i<n; i++){
                xData[0][ii]=xxData[i];
                ii++;
            }
        }

        this.weightOpt=true;
        int m = weight.length;
        for(int i=0; i<m; i++){
            if(weight[i]==0.0D){
                this.weightOpt=false;
                System.out.println("a weight in Regression equals zero; all weights set to 1.0");
            }
        }
        setDefaultValues(xData, yData, weight);
	}

    // Constructor with data with x as 2D array and no weights provided
    public Regression(double[][] xData, double[] yData){
        this.nData0 = yData.length;
        int n = yData.length;
        double[] weight = new double[n];

        this.weightOpt=false;
        for(int i=0; i<n; i++)weight[i]=1.0D;

        setDefaultValues(xData, yData, weight);
	}

    // Constructor with data with x and y as 2D arrays and no weights provided
    public Regression(double[][] xxData, double[][] yyData){
        this.multipleY = true;
        int nY1 = yyData.length;
        this.nYarrays = nY1;
        int nY2 = yyData[0].length;
        this.nData0 = nY2;
        int nX1 = xxData.length;
        int nX2 = xxData[0].length;
        double[] yData = new double[nY1*nY2];
        double[][] xData = new double[nY1*nY2][nX1];
        int ii=0;
        for(int i=0; i<nY1; i++){
            int nY = yyData[i].length;
            if(nY!=nY2)throw new IllegalArgumentException("multiple y arrays must be of the same length");
            int nX = xxData[i].length;
            if(nY!=nX)throw new IllegalArgumentException("multiple y arrays must be of the same length as the x array length");
            for(int j=0; j<nY2; j++){
                yData[ii] = yyData[i][j];
                xData[ii][i] = xxData[i][j];
                ii++;
            }
        }

        int n = yData.length;
        double[] weight = new double[n];

        this.weightOpt=false;
        for(int i=0; i<n; i++)weight[i]=1.0D;

        setDefaultValues(xData, yData, weight);
	}

    // Constructor with data with x as 1D array and no weights provided
    public Regression(double[] xxData, double[] yData){
        this.nData0 = yData.length;
        int n = xxData.length;
        double[][] xData = new double[1][n];
        double[] weight = new double[n];

        for(int i=0; i<n; i++)xData[0][i]=xxData[i];

        this.weightOpt=false;
        for(int i=0; i<n; i++)weight[i]=1.0D;

        setDefaultValues(xData, yData, weight);
	}

	// Constructor with data with x as 1D array and y as a 2D array and no weights provided
    public Regression(double[] xxData, double[][] yyData){
        this.multipleY = true;
        int nY1 = yyData.length;
        this.nYarrays = nY1;
        int nY2= yyData[0].length;
        this.nData0 = nY2;
        double[] yData = new double[nY1*nY2];
        int ii=0;
        for(int i=0; i<nY1; i++){
            int nY = yyData[i].length;
            if(nY!=nY2)throw new IllegalArgumentException("multiple y arrays must be of the same length");
            for(int j=0; j<nY2; j++){
                yData[ii] = yyData[i][j];
                ii++;
            }
        }

        double[][] xData = new double[1][nY1*nY2];
        double[] weight = new double[nY1*nY2];

        ii=0;
        int n = xxData.length;
        for(int j=0; j<nY1; j++){
            for(int i=0; i<n; i++){
                xData[0][ii]=xxData[i];
                weight[ii]=1.0D;
                ii++;
            }
        }
        this.weightOpt=false;

        setDefaultValues(xData, yData, weight);
	}

	// Constructor with data as a single array that has to be binned
	// bin width and value of the low point of the first bin provided
    public Regression(double[] xxData, double binWidth, double binZero){
        double[][] data = Stat.histogramBins(xxData, binWidth, binZero);
        int n = data[0].length;
        this.nData0 = n;
        double[][] xData = new double[1][n];
        double[] yData = new double[n];
        double[] weight = new double[n];
        for(int i=0; i<n; i++){
            xData[0][i]=data[0][i];
            yData[i]=data[1][i];
        }
        boolean flag = setTrueFreqWeights(yData, weight);
        if(flag){
            this.trueFreq=true;
            this.weightOpt=true;
        }
        else{
            this.trueFreq=false;
            this.weightOpt=false;
        }
        this.nData0 = xData.length;
        setDefaultValues(xData, yData, weight);
	}

	// Constructor with data as a single array that has to be binned
	// bin width provided
    public Regression(double[] xxData, double binWidth){
        double[][] data = Stat.histogramBins(xxData, binWidth);
        int n = data[0].length;
        this.nData0 = n;
        double[][] xData = new double[1][n];
        double[] yData = new double[n];
        double[] weight = new double[n];
        for(int i=0; i<n; i++){
            xData[0][i]=data[0][i];
            yData[i]=data[1][i];
        }
        boolean flag = setTrueFreqWeights(yData, weight);
        if(flag){
            this.trueFreq=true;
            this.weightOpt=true;
        }
        else{
            this.trueFreq=false;
            this.weightOpt=false;
        }
        this.nData0 = xData.length;
        setDefaultValues(xData, yData, weight);
	}

    private static boolean setTrueFreqWeights(double[] yData, double[] weight){
        int nData=yData.length;
        boolean flag = true;
        boolean unityWeight=false;
        for(int ii=0; ii<nData; ii++){
            weight[ii]=Math.sqrt(Math.abs(yData[ii]));
        }
        int i = 0;
        while(!unityWeight){
            if(weight[i]==0.0D){
                // find next non-zero weight
                boolean test = true;
                int ilast = i-1;
                int inext = i;
                while(test){
                    inext++;
                    if(inext>nData){
                        if(ilast<0){
                            unityWeight=true;
                        }
                        test=false;
                    }
                    else{
                        if(weight[inext]!=0.0D)test=false;
                    }
                }
                if(unityWeight){
                    for(int k=0; k<nData; k++){
                        weight[k]=1.0D;
                    }
                    flag=false;
                }
                else{
                    if(ilast<0){
                        weight[i]=weight[inext]/2.0D;
                    }
                    else{
                        if(inext>=nData){
                            weight[i]=weight[ilast]/2.0D;
                        }
                        else{
                            weight[i]=(weight[ilast]+weight[inext])/2.0D;
                        }
                    }
                }
            }
            i++;
            if(i>=nData)unityWeight=true;
        }
        return flag;
    }

    // Set data and default values
    private void setDefaultValues(double[][] xData, double[] yData, double[] weight){
        this.nData = yData.length;
        this.nXarrays = xData.length;
        this.nTerms = this.nXarrays;
        this.yData = new double[nData];
        this.yCalc = new double[nData];
        this.weight = new double[nData];
        this.residual = new double[nData];
        this.residualW = new double[nData];
        this.xData = new double[nXarrays][nData];
        int n=weight.length;
        if(n!=this.nData)throw new IllegalArgumentException("The weight and the y data lengths do not agree");
        for(int i=0; i<this.nData; i++){
            this.yData[i]=yData[i];
            this.weight[i]=weight[i];
        }
        for(int j=0; j<this.nXarrays; j++){
            n=xData[j].length;
            if(n!=this.nData)throw new IllegalArgumentException("An x and the y data length do not agree");
            for(int i=0; i<this.nData; i++){
                this.xData[j][i]=xData[j][i];
            }
        }
	}

	// Supress printing of results
	public void supressPrint(){
	    this.supressPrint = true;
	}

	// Supress plot of calculated versus experimental values
	public void supressYYplot(){
	    this.supressYYplot = true;
	}


    // Reset the ordinate scale factor option
    // true - Ao is unkown to be found by regression procedure
    // false - Ao set to unity
    public void setYscaleOption(boolean flag){
        this.scaleFlag=flag;
        if(flag==false)this.yScaleFactor = 1.0D;
    }

    // Reset the ordinate scale factor option
    // true - Ao is unkown to be found by regression procedure
    // false - Ao set to unity
    // retained for backward compatibility
    public void setYscale(boolean flag){
        this.scaleFlag=flag;
        if(flag==false)this.yScaleFactor = 1.0D;
    }

    // Reset the ordinate scale factor option
    // true - Ao is unkown to be found by regression procedure
    // false - Ao set to given value
    public void setYscaleFactor(double scale){
        this.scaleFlag=false;
        this.yScaleFactor = scale;
    }

    // Get the ordinate scale factor option
    // true - Ao is unkown
    // false - Ao set to unity
    public boolean getYscaleOption(){
        return this.scaleFlag;
    }

    // Get the ordinate scale factor option
    // true - Ao is unkown
    // false - Ao set to unity
    // retained to ensure backward compatibility
    public boolean getYscale(){
        return this.scaleFlag;
    }

    // Reset the true frequency test, trueFreq
    // true if yData values are true frequencies, e.g. in a fit to Gaussian; false if not
    // if true chiSquarePoisson (see above) is also calculated
    public void setTrueFreq(boolean trFr){
        boolean trFrOld = this.trueFreq;
        this.trueFreq = trFr;
        if(trFr){
            boolean flag = setTrueFreqWeights(this.yData, this.weight);
            if(flag){
                this.trueFreq=true;
                this.weightOpt=true;
            }
            else{
                this.trueFreq=false;
                this.weightOpt=false;
            }
        }
        else{
            if(trFrOld){
                for(int i=0; i<this.weight.length; i++){
                    weight[i]=1.0D;
                }
                this.weightOpt=false;
            }
        }
    }

    // Get the true frequency test, trueFreq
    public boolean getTrueFreq(){
        return this.trueFreq;
    }

    // Reset the x axis legend
    public void setXlegend(String legend){
        this.xLegend = legend;
        this.legendCheck=true;
    }

    // Reset the y axis legend
    public void setYlegend(String legend){
        this.yLegend = legend;
        this.legendCheck=true;
    }

     // Set the title
    public void setTitle(String title){
        this.graphTitle = title;
    }

    // Multiple linear regression with intercept (including y = ax + b)
    // y = a + b.x1 + c.x2 + d.x3 + . . .
    public void linear(){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        this.lastMethod = 0;
        this.linNonLin = true;
        this.nTerms = this.nXarrays+1;
        this.degreesOfFreedom = this.nData - this.nTerms;
	    if(this.degreesOfFreedom<1)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
        double[][] aa = new double[this.nTerms][this.nData];

        for(int j=0; j<nData; j++)aa[0][j]=1.0D;
        for(int i=1; i<nTerms; i++){
            for(int j=0; j<nData; j++){
                aa[i][j]=this.xData[i-1][j];
            }
        }
        this.generalLinear(aa);
        this.generalLinearStats(aa);
    }

    // Multiple linear regression with intercept (including y = ax + b)
    // plus plot and output file
    // y = a + b.x1 + c.x2 + d.x3 + . . .
    // legends provided
    public void linearPlot(String xLegend, String yLegend){
        this.xLegend = xLegend;
        this.yLegend = yLegend;
        this.legendCheck = true;
        this.linear();
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY();
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }
    // Multiple linear regression with intercept (including y = ax + b)
    // plus plot and output file
    // y = a + b.x1 + c.x2 + d.x3 + . . .
    // no legends provided
    public void linearPlot(){
        this.linear();
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY();
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

    // Polynomial fitting
    // y = a + b.x + c.x^2 + d.x^3 + . . .
    public void polynomial(int deg){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        if(this.nXarrays>1)throw new IllegalArgumentException("This class will only perform a polynomial regression on a single x array");
        if(deg<1)throw new IllegalArgumentException("Polynomial degree must be greater than zero");
        this.lastMethod = 1;
        this.linNonLin = true;
        this.nTerms =  deg+1;
        this.degreesOfFreedom = this.nData - this.nTerms;
	    if(this.degreesOfFreedom<1)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
        double[][] aa = new double[this.nTerms][this.nData];

        for(int j=0; j<nData; j++)aa[0][j]=1.0D;
        for(int j=0; j<nData; j++)aa[1][j]=this.xData[0][j];

        for(int i=2; i<nTerms; i++){
            for(int j=0; j<nData; j++){
                aa[i][j]=Math.pow(this.xData[0][j],i);
            }
        }
        this.generalLinear(aa);
        this.generalLinearStats(aa);
    }


    // Polynomial fitting plus plot and output file
    // y = a + b.x + c.x^2 + d.x^3 + . . .
    // legends provided
    public void polynomialPlot(int n, String xLegend, String yLegend){
        this.xLegend = xLegend;
        this.yLegend = yLegend;
        this.legendCheck = true;
        this.polynomial(n);
        if(!this.supressPrint)this.print();
        int flag = this.plotXY();
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

    // Polynomial fitting plus plot and output file
    // y = a + b.x + c.x^2 + d.x^3 + . . .
    // No legends provided
    public void polynomialPlot(int n){
        this.polynomial(n);
        if(!this.supressPrint)this.print();
        int flag = this.plotXY();
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

    // Generalised linear regression
    // y = a.f1(x) + b.f2(x) + c.f3(x) + . . .
    public void linearGeneral(){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        this.lastMethod = 2;

        this.linNonLin = true;
        this.nTerms = this.nXarrays;
        this.degreesOfFreedom = this.nData - this.nTerms;
	    if(this.degreesOfFreedom<1)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
        this.generalLinear(this.xData);
        this.generalLinearStats(this.xData);
    }

    // Generalised linear regression plus plot and output file
    // y = a.f1(x) + b.f2(x) + c.f3(x) + . . .
    // legends provided
    public void linearGeneralPlot(String xLegend, String yLegend){
        this.xLegend = xLegend;
        this.yLegend = yLegend;
        this.legendCheck = true;
        this.linearGeneral();
        if(!this.supressPrint)this.print();
        if(!this.supressYYplot)this.plotYY();
    }

    // Generalised linear regression plus plot and output file
    // y = a.f1(x) + b.f2(x) + c.f3(x) + . . .
    // No legends provided
    public void linearGeneralPlot(){
        this.linearGeneral();
        if(!this.supressPrint)this.print();
        if(!this.supressYYplot)this.plotYY();
    }

	// Generalised linear regression (private method called by linear(), linearGeneral() and polynomial())
    private void generalLinear(double[][] xd){
        if(this.nData<=this.nTerms)throw new IllegalArgumentException("Number of unknown parameters is greater than or equal to the number of data points");
	    double sde, sum=0.0D, yCalctemp=0.0D;
        double[][] a = new double[this.nTerms][this.nTerms];
        double[][] h = new double[this.nTerms][this.nTerms];

        double[]b = new double[this.nTerms];
        double[]coeff = new double[this.nTerms];

		for (int i=0; i<nTerms; ++i){
			sum=0.0D ;
			for (int j=0; j<nData; ++j){
				sum += this.yData[j]*xd[i][j]/Fmath.square(this.weight[j]);
			}
			b[i]=sum;
		}
		for (int i=0; i<nTerms; ++i){
			for (int j=0; j<nTerms; ++j){
				sum=0.0;
				for (int k=0; k<nData; ++k){
					sum += xd[i][k]*xd[j][k]/Fmath.square(this.weight[k]);
				}
				a[j][i]=sum;
			}
		}
		Matrix aa = new Matrix(a);
		coeff = aa.solveLinearSet(b);

		if(!this.best.isEmpty()){
		    int m=this.best.size();
		    for(int i=m-1; i>=0; i--){
		        this.best.removeElementAt(i);
		    }
		}
	    for(int i=0; i<this.nTerms; i++){
		    this.best.addElement(new Double(coeff[i]));
        }
	}

    // Generalised linear regression statistics (private method called by linear(), linearGeneral() and polynomial())
    private void generalLinearStats(double[][] xd){
	    double sde, sum=0.0D, yCalctemp=0.0D;
        double[][] a = new double[this.nTerms][this.nTerms];
        double[][] h = new double[this.nTerms][this.nTerms];
        double[][] stat = new double[this.nTerms][this.nTerms];
        double[][] cov = new double[this.nTerms][this.nTerms];
        this.covar = new double[this.nTerms][this.nTerms];
        this.corrCoeff = new double[this.nTerms][this.nTerms];
        double[]coeffSd = new double[this.nTerms];
        double[]coeff = new double[this.nTerms];

        for(int i=0; i<this.nTerms; i++){
            coeff[i] = ((Double) best.elementAt(i)).doubleValue();
        }

		if(this.weightOpt)this.chiSquare=0.0D;
		this.sumOfSquares=0.0D;
		for (int i=0; i< nData; ++i){
			yCalctemp=0.0;
			for (int j=0; j<nTerms; ++j){
				yCalctemp += coeff[j]*xd[j][i];
			}
			this.yCalc[i] = yCalctemp;
			yCalctemp -= this.yData[i];
			this.residual[i]=yCalctemp;
			this.residualW[i]=yCalctemp/weight[i];
			if(weightOpt)this.chiSquare += Fmath.square(yCalctemp/this.weight[i]);
			this.sumOfSquares += Fmath.square(yCalctemp);
		}
		if(this.weightOpt || this.trueFreq)this.reducedChiSquare = this.chiSquare/(this.degreesOfFreedom);
		double varY = this.sumOfSquares/(this.degreesOfFreedom);
		double sdY = Math.sqrt(varY);

        if(this.sumOfSquares==0.0D){
             for(int i=0; i<this.nTerms;i++){
                coeffSd[i]=0.0D;
		        for(int j=0; j<this.nTerms;j++){
		            this.covar[i][j]=0.0D;
		            if(i==j){
		                this.corrCoeff[i][j]=1.0D;
		            }
		            else{
		                this.corrCoeff[i][j]=0.0D;
		            }
		        }
		    }
        }
        else{
	        for (int i=0; i<this.nTerms; ++i){
	    	    for (int j=0; j<this.nTerms; ++j){
	    		    sum=0.0;
	    		    for (int k=0; k<this.nData; ++k){
	    		        if (weightOpt){
	    		            sde = weight[k];
	    	            }
	                    else{
	    		            sde = sdY;
	                    }
                        sum += xd[i][k]*xd[j][k]/Fmath.square(sde);
                    }
	                h[j][i]=sum;
	    	    }
	        }
		    Matrix hh = new Matrix(h);
		    hh = hh.inverse();
		    stat = hh.getArrayCopy();
		    for (int j=0; j<nTerms; ++j){
		        coeffSd[j] = Math.sqrt(stat[j][j]);
		    }

	        for(int i=0; i<this.nTerms;i++){
		        for(int j=0; j<this.nTerms;j++){
		            this.covar[i][j]=stat[i][j];
		        }
		    }

		    for(int i=0; i<this.nTerms;i++){
		        for(int j=0; j<this.nTerms;j++){
		            if(i==j){
		                this.corrCoeff[i][j] = 1.0D;
		            }
		            else{
		                this.corrCoeff[i][j]=covar[i][j]/(coeffSd[i]*coeffSd[j]);
                    }
                }
		    }
		}

		if(!this.bestSd.isEmpty()){
		    int m=this.best.size();
		    for(int i=m-1; i>=0; i--){
		        this.bestSd.removeElementAt(i);
		    }
		}
	    for(int i=0; i<this.nTerms; i++){
		    this.bestSd.addElement(new Double(coeffSd[i]));
        }

        if(this.nXarrays==1 && nYarrays==1){
            this.sampleR = Stat.corrCoeff(this.xData[0], this.yData, this.weight);
            this.sampleR2 = this.sampleR*this.sampleR;

        }
        else{
            this.multCorrelCoeff(this.yData, this.yCalc, this.weight);
        }
	}


    // Nelder and Mead Simplex Simplex Non-linear Regression
    private void nelderMead(Vector vec, double[] start, double[] step, double fTol, int nMax){
        boolean testContract=false; // test whether a simplex contraction has been performed
        int np = start.length;  // number of unknown parameters;
        this.nlrStatus = true;
        this.nTerms = np;
        int nnp = np+1; // Number of simplex apices
        this.lastSSnoConstraint=0.0D;

        if(this.scaleOpt<2)this.scale = new double[np];
        if(scaleOpt==2 && scale.length!=start.length)throw new IllegalArgumentException("scale array and initial estimate array are of different lengths");
        if(step.length!=start.length)throw new IllegalArgumentException("step array length " + step.length + " and initial estimate array length " + start.length + " are of different");

        // check for zero step sizes
        for(int i=0; i<np; i++)if(step[i]==0.0D)throw new IllegalArgumentException("step " + i+ " size is zero");

	    // set up arrays
	    this.startH = new double[np];
	    this.step = new double[np];
	    double[]pmin = new double[np];   //Nelder and Mead Pmin

	    double[][] pp = new double[nnp][nnp];   //Nelder and Mead P
	    double[] yy = new double[nnp];          //Nelder and Mead y
	    double[] pbar = new double[nnp];        //Nelder and Mead P with bar superscript
	    double[] pstar = new double[nnp];       //Nelder and Mead P*
	    double[] p2star = new double[nnp];      //Nelder and Mead P**

        // mean of abs values of yData (for testing for minimum)
        double yabsmean=0.0D;
        for(int i=0; i<this.nData; i++)yabsmean += Math.abs(yData[i]);
        yabsmean /= this.nData;
        // degrees of freedom
        double degfree = (double)(this.degreesOfFreedom);

        // Set any single parameter constraint parameters
        if(this.penalty){
            Integer itemp = (Integer)this.penalties.elementAt(1);
            this.nConstraints = itemp.intValue();
            this.penaltyParam = new int[this.nConstraints];
            this.penaltyCheck = new int[this.nConstraints];
            this.constraints = new double[this.nConstraints];
            Double dtemp = null;
            int j=2;
            for(int i=0;i<this.nConstraints;i++){
                itemp = (Integer)this.penalties.elementAt(j);
                this.penaltyParam[i] = itemp.intValue();
                j++;
                itemp = (Integer)this.penalties.elementAt(j);
                this.penaltyCheck[i] = itemp.intValue();
                j++;
                dtemp = (Double)this.penalties.elementAt(j);
                this.constraints[i] = dtemp.doubleValue();
                j++;
            }
        }

        // Set any multiple parameter constraint parameters
        if(this.sumPenalty){
            Integer itemp = (Integer)this.sumPenalties.elementAt(1);
            this.nSumConstraints = itemp.intValue();
            this.sumPenaltyParam = new int[this.nSumConstraints][];
            this.sumPlusOrMinus = new int[this.nSumConstraints][];
            this.sumPenaltyCheck = new int[this.nSumConstraints];
            this.sumPenaltyNumber = new int[this.nSumConstraints];
            this.sumConstraints = new double[this.nSumConstraints];
            int[] itempArray = null;
            Double dtemp = null;
            int j=2;
            for(int i=0;i<this.nSumConstraints;i++){
                itemp = (Integer)this.sumPenalties.elementAt(j);
                this.sumPenaltyNumber[i] = itemp.intValue();
                j++;
                itempArray = (int[])this.sumPenalties.elementAt(j);
                this.sumPenaltyParam[i] = itempArray;
                j++;
                itempArray = (int[])this.sumPenalties.elementAt(j);
                this.sumPlusOrMinus[i] = itempArray;
                j++;
                itemp = (Integer)this.sumPenalties.elementAt(j);
                this.sumPenaltyCheck[i] = itemp.intValue();
                j++;
                dtemp = (Double)this.sumPenalties.elementAt(j);
                this.sumConstraints[i] = dtemp.doubleValue();
                j++;
            }
        }

        // Store unscaled start values
        for(int i=0; i<np; i++)this.startH[i]=start[i];

        // scale initial estimates and step sizes
        if(this.scaleOpt>0){
            boolean testzero=false;
            for(int i=0; i<np; i++)if(start[i]==0.0D)testzero=true;
            if(testzero){
                System.out.println("Neler and Mead Simplex: a start value of zero precludes scaling");
                System.out.println("Regression performed without scaling");
                this.scaleOpt=0;
            }
        }
        switch(this.scaleOpt){
            case 0: for(int i=0; i<np; i++)scale[i]=1.0D;
                    break;
            case 1: for(int i=0; i<np; i++){
                        scale[i]=1.0/start[i];
                        step[i]=step[i]/start[i];
                        start[i]=1.0D;
                    }
                    break;
            case 2: for(int i=0; i<np; i++){
                        step[i]*=scale[i];
                        start[i]*= scale[i];
                    }
                    break;
        }

        // set class member values
        this.fTol=fTol;
        this.nMax=nMax;
        this.nIter=0;
        for(int i=0; i<np; i++){
            this.step[i]=step[i];
            this.scale[i]=scale[i];
        }

	    // initial simplex
	    double sho=0.0D;
	    for (int i=0; i<np; ++i){
 	        sho=start[i];
	 	    pstar[i]=sho;
		    p2star[i]=sho;
		    pmin[i]=sho;
	    }

	    int jcount=this.konvge;  // count of number of restarts still available

	    for (int i=0; i<np; ++i){
	        pp[i][nnp-1]=start[i];
	    }
	    yy[nnp-1]=this.sumSquares(vec, start);
	    for (int j=0; j<np; ++j){
		    start[j]=start[j]+step[j];

		    for (int i=0; i<np; ++i)pp[i][j]=start[i];
		    yy[j]=this.sumSquares(vec, start);
		    start[j]=start[j]-step[j];
	    }

	    // loop over allowed iterations
        double  ynewlo=0.0D;    // current value lowest y
	    double 	ystar = 0.0D;   // Nelder and Mead y*
	    double  y2star = 0.0D;  // Nelder and Mead y**
	    double  ylo = 0.0D;     // Nelder and Mead y(low)
	    double  fMin;   // function value at minimum
	    // variables used in calculating the variance of the simplex at a putative minimum
	    double 	curMin = 00D, sumnm = 0.0D, summnm = 0.0D, zn = 0.0D;
	    int ilo=0;  // index of low apex
	    int ihi=0;  // index of high apex
	    int ln=0;   // counter for a check on low and high apices
	    boolean test = true;    // test becomes false on reaching minimum

	    while(test){
	        // Determine h
	        ylo=yy[0];
	        ynewlo=ylo;
    	    ilo=0;
	        ihi=0;
	        for (int i=1; i<nnp; ++i){
		        if (yy[i]<ylo){
			        ylo=yy[i];
			        ilo=i;
		        }
		        if (yy[i]>ynewlo){
			        ynewlo=yy[i];
			        ihi=i;
		        }
	        }
	        // Calculate pbar
	        for (int i=0; i<np; ++i){
		        zn=0.0D;
		        for (int j=0; j<nnp; ++j){
			        zn += pp[i][j];
		        }
		        zn -= pp[i][ihi];
		        pbar[i] = zn/np;
	        }

	        // Calculate p=(1+alpha).pbar-alpha.ph {Reflection}
	        for (int i=0; i<np; ++i)pstar[i]=(1.0 + this.rCoeff)*pbar[i]-this.rCoeff*pp[i][ihi];

	        // Calculate y*
	        ystar=this.sumSquares(vec, pstar);

	        ++this.nIter;

	        // check for y*<yi
	        if(ystar < ylo){
                // Form p**=(1+gamma).p*-gamma.pbar {Extension}
	            for (int i=0; i<np; ++i)p2star[i]=pstar[i]*(1.0D + this.eCoeff)-this.eCoeff*pbar[i];
	            // Calculate y**
	            y2star=this.sumSquares(vec, p2star);
	            ++this.nIter;
                if(y2star < ylo){
                    // Replace ph by p**
		            for (int i=0; i<np; ++i)pp[i][ihi] = p2star[i];
	                yy[ihi] = y2star;
	            }
	            else{
	                //Replace ph by p*
	                for (int i=0; i<np; ++i)pp[i][ihi]=pstar[i];
	                yy[ihi]=ystar;
	            }
	        }
	        else{
	            // Check y*>yi, i!=h
		        ln=0;
	            for (int i=0; i<nnp; ++i)if (i!=ihi && ystar > yy[i]) ++ln;
	            if (ln==np ){
	                // y*>= all yi; Check if y*>yh
                    if(ystar<=yy[ihi]){
                        // Replace ph by p*
	                    for (int i=0; i<np; ++i)pp[i][ihi]=pstar[i];
	                    yy[ihi]=ystar;
	                }
	                // Calculate p** =beta.ph+(1-beta)pbar  {Contraction}
	                for (int i=0; i<np; ++i)p2star[i]=this.cCoeff*pp[i][ihi] + (1.0 - this.cCoeff)*pbar[i];
	                // Calculate y**
	                y2star=this.sumSquares(vec, p2star);
	                ++this.nIter;
	                // Check if y**>yh
	                if(y2star>yy[ihi]){
	                    //Replace all pi by (pi+pl)/2

	                    for (int j=0; j<nnp; ++j){
		                    for (int i=0; i<np; ++i){
			                    pp[i][j]=0.5*(pp[i][j] + pp[i][ilo]);
			                    pmin[i]=pp[i][j];
		                    }
		                    yy[j]=this.sumSquares(vec, pmin);
	                    }
	                    this.nIter += nnp;
	                }
	                else{
	                    // Replace ph by p**
		                for (int i=0; i<np; ++i)pp[i][ihi] = p2star[i];
	                    yy[ihi] = y2star;
	                }
	            }
	            else{
	                // replace ph by p*
	                for (int i=0; i<np; ++i)pp[i][ihi]=pstar[i];
	                yy[ihi]=ystar;
	            }
	        }

            // test for convergence
            // calculte sd of simplex and minimum point
            sumnm=0.0;
	        ynewlo=yy[0];
	        ilo=0;
	        for (int i=0; i<nnp; ++i){
	            sumnm += yy[i];
	            if(ynewlo>yy[i]){
	                ynewlo=yy[i];
	                ilo=i;
	            }
	        }
	        sumnm /= (double)(nnp);
	        summnm=0.0;
	        for (int i=0; i<nnp; ++i){
		        zn=yy[i]-sumnm;
	            summnm += zn*zn;
	        }
	        curMin=Math.sqrt(summnm/np);

	        // test simplex sd
	        switch(this.minTest){
	            case 0:
                    if(curMin<fTol)test=false;
                    break;
	            case 1:
                    if(Math.sqrt(ynewlo/degfree)<yabsmean*fTol)test=false;
                    break;
		    }
            this.sumOfSquares=ynewlo;
	        if(!test){
	            // store best estimates
	            for (int i=0; i<np; ++i)pmin[i]=pp[i][ilo];
	            yy[nnp-1]=ynewlo;
	            // store simplex sd
	            this.simplexSd = curMin;
	            // test for restart
	            --jcount;
	            if(jcount>0){
	                test=true;
	   	            for (int j=0; j<np; ++j){
		                pmin[j]=pmin[j]+step[j];
		                for (int i=0; i<np; ++i)pp[i][j]=pmin[i];
		                yy[j]=this.sumSquares(vec, pmin);
		                pmin[j]=pmin[j]-step[j];
	                 }
	            }
	        }

	        if(test && this.nIter>this.nMax){
	            System.out.println("Maximum iteration number reached, in Regression.simplex(...)");
	            System.out.println("without the convergence criterion being satisfied");
	            System.out.println("Current parameter estimates and sum of squares values returned");
	            this.nlrStatus = false;
	            // store current estimates
	            for (int i=0; i<np; ++i)pmin[i]=pp[i][ilo];
	            yy[nnp-1]=ynewlo;
                test=false;
            }

        }

	    if(!this.best.isEmpty()){
    		int m=this.best.size();
		    for(int i=m-1; i>=0; i--){
		        this.best.removeElementAt(i);
		        this.bestSd.removeElementAt(i);
		    }
	    }
	    for (int i=0; i<np; ++i){
            pmin[i] = pp[i][ihi];
            this.best.addElement(new Double(pmin[i]/this.scale[i]));
            this.scale[i]=1.0D; // unscale for statistical methods
        }
    	this.fMin=ynewlo;
    	this.kRestart=this.konvge-jcount;

        if(statFlag){
            pseudoLinearStats(vec);
        }
        else{
            for (int i=0; i<np; ++i){
                this.bestSd.addElement(new Double(Double.NaN));
            }
        }
	}

    // Nelder and Mead Simplex Simplex Non-linear Regression
    public void simplex(RegressionFunction g, double[] start, double[] step, double fTol, int nMax){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplex2 should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(vec, start, step, fTol, nMax);
    }


    // Nelder and Mead Simplex Simplex Non-linear Regression
    // plus plot and output file
    public void simplexPlot(RegressionFunction g, double[] start, double[] step, double fTol, int nMax){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplexPlot2 should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(vec, start, step, fTol, nMax);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default  maximum iterations
    public void simplex(RegressionFunction g, double[] start, double[] step, double fTol){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplex2 should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        int nMaxx = this.nMax;
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(vec, start, step, fTol, nMaxx);
    }

    // Nelder and Mead Simplex Simplex Non-linear Regression
    // plus plot and output file
	// Default  maximum iterations
    public void simplexPlot(RegressionFunction g, double[] start, double[] step, double fTol){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplexPlot2 should have been called");
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex(g, start, step, fTol);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default  tolerance
    public void simplex(RegressionFunction g, double[] start, double[] step, int nMax){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplex2 should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        double fToll = this.fTol;
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(vec, start, step, fToll, nMax);
    }

    // Nelder and Mead Simplex Simplex Non-linear Regression
    // plus plot and output file
	// Default  tolerance
    public void simplexPlot(RegressionFunction g, double[] start, double[] step, int nMax){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplexPlot2 should have been called");
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex(g, start, step, nMax);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default  tolerance
	// Default  maximum iterations
    public void simplex(RegressionFunction g, double[] start, double[] step){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplex2 should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        double fToll = this.fTol;
        int nMaxx = this.nMax;
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(vec, start, step, fToll, nMaxx);
    }

    // Nelder and Mead Simplex Simplex Non-linear Regression
    // plus plot and output file
	// Default  tolerance
	// Default  maximum iterations
    public void simplexPlot(RegressionFunction g, double[] start, double[] step){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplexPlot2 should have been called");
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex(g, start, step);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default step option - all step[i] = dStep
    public void simplex(RegressionFunction g, double[] start, double fTol, int nMax){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplex2 should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        int n=start.length;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(vec, start, stepp, fTol, nMax);
    }

    // Nelder and Mead Simplex Simplex Non-linear Regression
    // plus plot and output file
	// Default step option - all step[i] = dStep
    public void simplexPlot(RegressionFunction g, double[] start, double fTol, int nMax){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplexPlot2 should have been called");
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex(g, start, fTol, nMax);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default  maximum iterations
	// Default step option - all step[i] = dStep
    public void simplex(RegressionFunction g, double[] start, double fTol){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplex2 should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        int n=start.length;
        int nMaxx = this.nMax;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(vec, start, stepp, fTol, nMaxx);
    }

    // Nelder and Mead Simplex Simplex Non-linear Regression
    // plus plot and output file
	// Default  maximum iterations
	// Default step option - all step[i] = dStep
    public void simplexPlot(RegressionFunction g, double[] start, double fTol){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplexPlot2 should have been called");
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex(g, start, fTol);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
    // Default  tolerance
	// Default step option - all step[i] = dStep
    public void simplex(RegressionFunction g, double[] start, int nMax){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplex2 should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        int n=start.length;
        double fToll = this.fTol;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.lastMethod=3;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(vec, start, stepp, fToll, nMax);
    }

    // Nelder and Mead Simplex Simplex Non-linear Regression
    // plus plot and output file
    // Default  tolerance
	// Default step option - all step[i] = dStep
    public void simplexPlot(RegressionFunction g, double[] start, int nMax){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplexPlot2 should have been called");
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex(g, start, nMax);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
    // Default  tolerance
    // Default  maximum iterations
	// Default step option - all step[i] = dStep
    public void simplex(RegressionFunction g, double[] start){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplex2 should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        int n=start.length;
        int nMaxx = this.nMax;
        double fToll = this.fTol;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(vec, start, stepp, fToll, nMaxx);
    }

    // Nelder and Mead Simplex Simplex Non-linear Regression
    // plus plot and output file
    // Default  tolerance
    // Default  maximum iterations
	// Default step option - all step[i] = dStep
    public void simplexPlot(RegressionFunction g, double[] start){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplexPlot2 should have been called");
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex(g, start);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }



    // Nelder and Mead Simplex Simplex2 Non-linear Regression
    public void simplex2(RegressionFunction2 g, double[] start, double[] step, double fTol, int nMax){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(vec, start, step, fTol, nMax);
    }


    // Nelder and Mead Simplex Simplex2 Non-linear Regression
    // plus plot and output file
    public void simplexPlot2(RegressionFunction2 g, double[] start, double[] step, double fTol, int nMax){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(vec, start, step, fTol, nMax);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY2(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default  maximum iterations
    public void simplex2(RegressionFunction2 g, double[] start, double[] step, double fTol){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        int nMaxx = this.nMax;
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(vec, start, step, fTol, nMaxx);
    }

    // Nelder and Mead Simplex Simplex2 Non-linear Regression
    // plus plot and output file
	// Default  maximum iterations
    public void simplexPlot2(RegressionFunction2 g, double[] start, double[] step, double fTol){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex2(g, start, step, fTol);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY2(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default  tolerance
    public void simplex2(RegressionFunction2 g, double[] start, double[] step, int nMax){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        double fToll = this.fTol;
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(vec, start, step, fToll, nMax);
    }

    // Nelder and Mead Simplex Simplex2 Non-linear Regression
    // plus plot and output file
	// Default  tolerance
    public void simplexPlot2(RegressionFunction2 g, double[] start, double[] step, int nMax){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex2(g, start, step, nMax);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY2(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default  tolerance
	// Default  maximum iterations
    public void simplex2(RegressionFunction2 g, double[] start, double[] step){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        double fToll = this.fTol;
        int nMaxx = this.nMax;
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(vec, start, step, fToll, nMaxx);
    }

    // Nelder and Mead Simplex Simplex2 Non-linear Regression
    // plus plot and output file
	// Default  tolerance
	// Default  maximum iterations
    public void simplexPlot2(RegressionFunction2 g, double[] start, double[] step){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex2(g, start, step);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY2(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default step option - all step[i] = dStep
    public void simplex2(RegressionFunction2 g, double[] start, double fTol, int nMax){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        int n=start.length;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(vec, start, stepp, fTol, nMax);
    }

    // Nelder and Mead Simplex Simplex2 Non-linear Regression
    // plus plot and output file
	// Default step option - all step[i] = dStep
    public void simplexPlot2(RegressionFunction2 g, double[] start, double fTol, int nMax){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex2(g, start, fTol, nMax);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY2(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default  maximum iterations
	// Default step option - all step[i] = dStep
    public void simplex2(RegressionFunction2 g, double[] start, double fTol){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        int n=start.length;
        int nMaxx = this.nMax;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(vec, start, stepp, fTol, nMaxx);
    }

    // Nelder and Mead Simplex Simplex2 Non-linear Regression
    // plus plot and output file
	// Default  maximum iterations
	// Default step option - all step[i] = dStep
    public void simplexPlot2(RegressionFunction2 g, double[] start, double fTol){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex2(g, start, fTol);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY2(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
    // Default  tolerance
	// Default step option - all step[i] = dStep
    public void simplex2(RegressionFunction2 g, double[] start, int nMax){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        int n=start.length;
        double fToll = this.fTol;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.lastMethod=3;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(vec, start, stepp, fToll, nMax);
    }

    // Nelder and Mead Simplex Simplex2 Non-linear Regression
    // plus plot and output file
    // Default  tolerance
	// Default step option - all step[i] = dStep
    public void simplexPlot2(RegressionFunction2 g, double[] start, int nMax){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex2(g, start, nMax);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY2(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
    // Default  tolerance
    // Default  maximum iterations
	// Default step option - all step[i] = dStep
    public void simplex2(RegressionFunction2 g, double[] start){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        int n=start.length;
        int nMaxx = this.nMax;
        double fToll = this.fTol;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(vec, start, stepp, fToll, nMaxx);
    }

    // Nelder and Mead Simplex Simplex2 Non-linear Regression
    // plus plot and output file
    // Default  tolerance
    // Default  maximum iterations
	// Default step option - all step[i] = dStep
    public void simplexPlot2(RegressionFunction2 g, double[] start){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        this.lastMethod=3;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex2(g, start);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY2(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

    // Calculate the sum of squares of the residuals for non-linear regression
	private double sumSquares(Vector vec, double[] x){
	    RegressionFunction g1 = null;
	    RegressionFunction2 g2 = null;
	    if(this.multipleY){
            g2 = (RegressionFunction2)vec.elementAt(0);
        }
        else{
            g1 = (RegressionFunction)vec.elementAt(0);
        }

	    double ss = -3.0D;
	    double[] param = new double[this.nTerms];
	    double[] xd = new double[this.nXarrays];
	    // rescale
	    for(int i=0; i<this.nTerms; i++)param[i]=x[i]/scale[i];

        // single parameter penalty functions
        double tempFunctVal = this.lastSSnoConstraint;
        boolean test=true;
        if(this.penalty){
            int k=0;
            for(int i=0; i<this.nConstraints; i++){
                k = this.penaltyParam[i];
                if(this.penaltyCheck[i]==-1){
                    if(param[k]<constraints[i]){
                        ss = tempFunctVal + this.penaltyWeight*Fmath.square(param[k]-constraints[i]);
                        test=false;
                     }
                }
                if(this.penaltyCheck[i]==1){
                    if(param[k]>constraints[i]){
                        ss = tempFunctVal + this.penaltyWeight*Fmath.square(param[k]-constraints[i]);
	                    test=false;
                    }
                }
            }
        }

        // multiple parameter penalty functions
        if(this.sumPenalty){
            int kk = 0;
            int pSign = 0;
            double sumPenaltySum = 0.0D;
            for(int i=0; i<this.nSumConstraints; i++){
                for(int j=0; j<this.sumPenaltyNumber[i]; j++){
                    kk = this.sumPenaltyParam[i][j];
                    pSign = this.sumPlusOrMinus[i][j];
                    sumPenaltySum += param[kk]*pSign;
                }
                if(this.sumPenaltyCheck[i]==-1){
                    if(sumPenaltySum<sumConstraints[i]){
                        ss = tempFunctVal + this.penaltyWeight*Fmath.square(sumPenaltySum-sumConstraints[i]);
                        test=false;
                     }
                }
                if(this.sumPenaltyCheck[i]==1){
                    if(sumPenaltySum>sumConstraints[i]){
                        ss = tempFunctVal + this.penaltyWeight*Fmath.square(sumPenaltySum-sumConstraints[i]);
	                    test=false;
                    }
                }
            }
        }

        if(test){
            ss = 0.0D;
            for(int i=0; i<this.nData; i++){
                for(int j=0; j<nXarrays; j++)xd[j]=this.xData[j][i];
                if(!this.multipleY){
                    ss += Fmath.square((this.yData[i] - g1.function(param, xd))/this.weight[i]);
                }
                else{
                    ss += Fmath.square((this.yData[i] - g2.function(param, xd, i))/this.weight[i]);
                }

            }
            this.lastSSnoConstraint = ss;

        }


	    return ss;

	}

	// add a single parameter constraint boundary for the non-linear regression
	public void addConstraint(int paramIndex, int conDir, double constraint){
	    this.penalty=true;

        // First element reserved for method number if other methods than 'cliff' are added later
		if(this.penalties.isEmpty())this.penalties.addElement(new Integer(this.constraintMethod));

		// add constraint
	    if(penalties.size()==1){
		    this.penalties.addElement(new Integer(1));
		}
		else{
		    int nPC = ((Integer)this.penalties.elementAt(1)).intValue();
            nPC++;
            this.penalties.setElementAt(new Integer(nPC), 1);
		}
		this.penalties.addElement(new Integer(paramIndex));
 	    this.penalties.addElement(new Integer(conDir));
 	    this.penalties.addElement(new Double(constraint));
 	}

    // add a multiple parameter constraint boundary for the non-linear regression
	public void addConstraint(int[] paramIndices, int[] plusOrMinus, int conDir, double constraint){
	    int nCon = paramIndices.length;
	    int nPorM = plusOrMinus.length;
	    if(nCon!=nPorM)throw new IllegalArgumentException("num of parameters, " + nCon + ", does not equal number of parameter signs, " + nPorM);
	    this.sumPenalty=true;

        // First element reserved for method number if other methods than 'cliff' are added later
		if(this.sumPenalties.isEmpty())this.sumPenalties.addElement(new Integer(this.constraintMethod));

    	// add constraint
		if(sumPenalties.size()==1){
		    this.sumPenalties.addElement(new Integer(1));
		}
		else{
		    int nPC = ((Integer)this.sumPenalties.elementAt(1)).intValue();
            nPC++;
            this.sumPenalties.setElementAt(new Integer(nPC), 1);
		}
		this.sumPenalties.addElement(new Integer(nCon));
		this.sumPenalties.addElement(paramIndices);
		this.sumPenalties.addElement(plusOrMinus);
 	    this.sumPenalties.addElement(new Integer(conDir));
 	    this.sumPenalties.addElement(new Double(constraint));
 	}

	// remove all constraint boundaries for the non-linear regression
	public void removeConstraints(){

	    // check if single parameter constraints already set
	    if(!this.penalties.isEmpty()){
		    int m=this.penalties.size();

		    // remove single parameter constraints
    		for(int i=m-1; i>=0; i--){
		        this.penalties.removeElementAt(i);
		    }
		}
		this.penalty = false;
		this.nConstraints = 0;

	    // check if mutiple parameter constraints already set
	    if(!this.sumPenalties.isEmpty()){
		    int m=this.sumPenalties.size();

		    // remove multiple parameter constraints
    		for(int i=m-1; i>=0; i--){
		        this.sumPenalties.removeElementAt(i);
		    }
		}
		this.sumPenalty = false;
		this.nSumConstraints = 0;
	}

	//  linear statistics applied to a non-linear regression
    private int pseudoLinearStats(Vector vec){
	    double	f1 = 0.0D, f2 = 0.0D, f3 = 0.0D, f4 = 0.0D; // intermdiate values in numerical differentiation
	    int	flag = 0;       // returned as 0 if method fully successful;
	                        // negative if partially successful or unsuccessful: check posVarFlag and invertFlag
	                        //  -1  posVarFlag or invertFlag is false;
	                        //  -2  posVarFlag and invertFlag are false
	    int np = this.nTerms;

	    double[] f = new double[np];
    	double[] pmin = new double[np];
    	double[] coeffSd = new double[np];
    	double[] xd = new double[this.nXarrays];
	    double[][]stat = new double[np][np];
	    pseudoSd = new double[np];

	    Double temp = null;

	    this.grad = new double[np][2];
	    this.covar = new double[np][np];
        this.corrCoeff = new double[np][np];

        // get best estimates
	    for (int i=0;i<np; ++i){
	        temp = (Double)this.best.elementAt(i);
	        pmin[i]=temp.doubleValue();
	    }

        // gradient both sides of the minimum
        double hold0 = 1.0D;
        double hold1 = 1.0D;
	    for (int i=0;i<np; ++i){
		    for (int k=0;k<np; ++k){
			    f[k]=pmin[k];
		    }
		    hold0=pmin[i];
            if(hold0==0.0D){
                hold0=this.step[i];
                this.zeroCheck=true;
            }
		    f[i]=hold0*(1.0D - this.delta);
	        this.lastSSnoConstraint=this.sumOfSquares;
		    f1=sumSquares(vec, f);
		    f[i]=hold0*(1.0 + this.delta);
	        this.lastSSnoConstraint=this.sumOfSquares;
		    f2=sumSquares(vec, f);
		    this.grad[i][0]=(this.fMin-f1)/Math.abs(this.delta*hold0);
		    this.grad[i][1]=(f2-this.fMin)/Math.abs(this.delta*hold0);
	    }

        // second patial derivatives at the minimum
	    this.lastSSnoConstraint=this.sumOfSquares;
	    for (int i=0;i<np; ++i){
		    for (int j=0;j<np; ++j){
			    for (int k=0;k<np; ++k){
				    f[k]=pmin[k];
			    }
			    hold0=f[i];
                if(hold0==0.0D){
                    hold0=this.step[i];
                    this.zeroCheck=true;
                }
			    f[i]=hold0*(1.0 + this.delta/2.0D);
			    hold0=f[j];
                if(hold0==0.0D){
                    hold0=this.step[j];
                    this.zeroCheck=true;
                }
			    f[j]=hold0*(1.0 + this.delta/2.0D);
        	    this.lastSSnoConstraint=this.sumOfSquares;
			    f1=sumSquares(vec, f);
			    f[i]=pmin[i];
			    f[j]=pmin[j];
			    hold0=f[i];
                if(hold0==0.0D){
                    hold0=this.step[i];
                    this.zeroCheck=true;
                }
 			    f[i]=hold0*(1.0 - this.delta/2.0D);
			    hold0=f[j];
                if(hold0==0.0D){
                    hold0=this.step[j];
                    this.zeroCheck=true;
                }
 		        f[j]=hold0*(1.0 + this.delta/2.0D);
	            this.lastSSnoConstraint=this.sumOfSquares;
			    f2=sumSquares(vec, f);
			    f[i]=pmin[i];
			    f[j]=pmin[j];
			    hold0=f[i];
                if(hold0==0.0D){
                    hold0=this.step[i];
                    this.zeroCheck=true;
                }
    		    f[i]=hold0*(1.0 + this.delta/2.0D);
    		    hold0=f[j];
                if(hold0==0.0D){
                    hold0=this.step[j];
                    this.zeroCheck=true;
                }
			    f[j]=hold0*(1.0 - this.delta/2.0D);
	            this.lastSSnoConstraint=this.sumOfSquares;
			    f3=sumSquares(vec, f);
			    f[i]=pmin[i];
			    f[j]=pmin[j];
			    hold0=f[i];
                if(hold0==0.0D){
                    hold0=this.step[i];
                    this.zeroCheck=true;
                }
			    f[i]=hold0*(1.0 - this.delta/2.0D);
			    hold0=f[j];
                if(hold0==0.0D){
                    hold0=this.step[j];
                    this.zeroCheck=true;
                }
			    f[j]=hold0*(1.0 - this.delta/2.0D);
	            this.lastSSnoConstraint=this.sumOfSquares;
			    f4=sumSquares(vec, f);
			    stat[i][j]=(f1-f2-f3+f4)/(this.delta*this.delta);
		    }
	    }

        double ss=0.0D;
        double sc=0.0D;
	    for(int i=0; i<this.nData; i++){
            for(int j=0; j<nXarrays; j++)xd[j]=this.xData[j][i];
            if(this.multipleY){
	            this.yCalc[i] = ((RegressionFunction2)vec.elementAt(0)).function(pmin, xd, i);
	        }
	        else{
	            this.yCalc[i] = ((RegressionFunction)vec.elementAt(0)).function(pmin, xd);
	        }
	        this.residual[i] = this.yCalc[i]-this.yData[i];
	        ss += Fmath.square(this.residual[i]);
	        this.residualW[i] = this.residual[i]/this.weight[i];
	        sc += Fmath.square(this.residualW[i]);
	    }
	    this.sumOfSquares = ss;
	    double varY = ss/(this.nData-np);
	    double sdY = Math.sqrt(varY);
	    if(this.weightOpt || this.trueFreq){
	        this.chiSquare=sc;
	        this.reducedChiSquare=sc/(this.nData-np);
	    }

        // calculate reduced sum of squares
        double red=1.0D;
        if(!this.weightOpt && !this.trueFreq)red=this.sumOfSquares/(this.nData-np);

        // calculate pseudo errors  -  reduced sum of squares over second partial derivative
        for(int i=0; i<np; i++){
            pseudoSd[i] = (2.0D*this.delta*red*Math.abs(pmin[i]))/(grad[i][1]-grad[i][0]);
            if(pseudoSd[i]>=0.0D){
                pseudoSd[i] = Math.sqrt(pseudoSd[i]);
            }
            else{
                pseudoSd[i] = Double.NaN;
            }
        }

        // calculate covariance matrix
	    if(np==1){
	        hold0=pmin[0];
            if(hold0==0.0D)hold0=this.step[0];
	        stat[0][0]=1.0D/stat[0][0];
		    this.covar[0][0] = stat[0][0]*red*hold0*hold0;
		    if(covar[0][0]>=0.0D){
			    coeffSd[0]=Math.sqrt(this.covar[0][0]);
			    corrCoeff[0][0]=1.0D;
			}
	        else{
			    coeffSd[0]=Double.NaN;
			    corrCoeff[0][0]=Double.NaN;
			    this.posVarFlag=false;
			}
		}
		else{
            Matrix cov = new Matrix(stat);
            cov = cov.inverse();
            this.invertFlag = cov.getMatrixCheck();
            if(this.invertFlag==false)flag--;
            stat = cov.getArrayCopy();

	        this.posVarFlag=true;
	        if (this.invertFlag){
		        for (int i=0; i<np; ++i){
		            hold0=pmin[i];
                    if(hold0==0.0D)hold0=this.step[i];
			        for (int j=i; j<np;++j){
			            hold1=pmin[j];
                        if(hold1==0.0D)hold1=this.step[j];
				        this.covar[i][j] = 2.0D*stat[i][j]*red*hold0*hold1;
				        this.covar[j][i] = this.covar[i][j];
			        }
			        if(covar[i][i]>=0.0D){
			            coeffSd[i]=Math.sqrt(this.covar[i][i]);
			        }
			        else{
			            coeffSd[i]=Double.NaN;
			            this.posVarFlag=false;
			        }
		        }

		        for (int i=0; i<np; ++i){
			        for (int j=0; j<np; ++j){
			            if((coeffSd[i]!= Double.NaN) && (coeffSd[j]!= Double.NaN)){
			                this.corrCoeff[i][j] = this.covar[i][j]/(coeffSd[i]*coeffSd[j]);
			            }
			            else{
			                this.corrCoeff[i][j]= Double.NaN;
			            }
			        }
		        }
 	        }
 	        else{
		        for (int i=0; i<np; ++i){
			        for (int j=0; j<np;++j){
			            this.covar[i][j] = Double.NaN;
			            this.corrCoeff[i][j] = Double.NaN;
			        }
			        coeffSd[i]=Double.NaN;
			        this.posVarFlag=false;
		        }
		    }
		}
	    if(this.posVarFlag==false)flag--;

	    for(int i=0; i<this.nTerms; i++){
		    this.bestSd.addElement(new Double(coeffSd[i]));
        }

        this.multCorrelCoeff(this.yData, this.yCalc, this.weight);

        return flag;

	}

	// Print the results of the regression
	// File name provided
	// prec = truncation precision
	public void print(String filename, int prec){
	    this.prec = prec;
	    this.print(filename);
	}

	// Print the results of the regression
	// No file name provided
	// prec = truncation precision
	public void print(int prec){
	    this.prec = prec;
		String filename="RegressionOutput.txt";
        this.print(filename);
	}

    // Print the results of the regression
	// File name provided
	// default value for truncation precision
	public void print(String filename){
	    if(filename.indexOf('.')==-1)filename = filename+".txt";
	    FileOutput fout = new FileOutput(filename, 'n');
	    fout.dateAndTimeln(filename);
	    fout.println(this.graphTitle);
	    paraName = new String[this.nTerms];
        if(weightOpt){
            fout.println("Weighted Least Squares Minimisation");
        }
        else{
            fout.println("Unweighted Least Squares Minimisation");
        }
        switch(this.lastMethod){
	        case 0: fout.println("Linear Regression with intercept");
                    fout.println("y = c[0] + c[1]*x1 + c[2]*x2 +c[3]*x3 + . . .");
                    for(int i=0;i<this.nTerms;i++)this.paraName[i]="c["+i+"]";
                    this.linearPrint(fout);
	                break;
	        case 1: fout.println("Polynomial (with degree = " + (nTerms-1) + ") Fitting Linear Regression");
	                fout.println("y = c[0] + c[1]*x + c[2]*x^2 +c[3]*x^3 + . . .");
	                for(int i=0;i<this.nTerms;i++)this.paraName[i]="c["+i+"]";
                    this.linearPrint(fout);
	                break;
	        case 2: fout.println("Generalised linear regression");
	                fout.println("y = c[0]*f1(x) + c[1]*f2(x) + c[2]*f3(x) + . . .");
	                for(int i=0;i<this.nTerms;i++)this.paraName[i]="c["+i+"]";
                    this.linearPrint(fout);
	                break;
	        case 3: fout.println("Nelder and Mead Simplex Non-linear Regression");
	                fout.println("y = f(x1, x2, x3 . . ., c[0], c[1], c[2] . . .");
	                fout.println("y is non-linear with respect to the c[i]");
	                for(int i=0;i<this.nTerms;i++)this.paraName[i]="c["+i+"]";
                    this.nonLinearPrint(fout);
	                break;
	        case 4: fout.println("Fitting to a Gaussian");
	                fout.println("y = (yscale/(sd.sqrt(2.sd)).exp(0.5.square((x-mean)/sd))");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="mean";
	                paraName[1]="sd";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 5: fout.println("Fitting to a Lorentzian - Output");
	                fout.println("y = (yscale/pi).(gamma/2)/((x-mean)^2+(gamma/2)^2)");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="mean";
	                paraName[1]="gamma";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
	                break;
            case 6: fout.println("Fitting to a Poisson distribution");
	                fout.println("y = yscale.mu^k.exp(-mu)/mu!");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="mean";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 7: fout.println("Fitting to a Two Parameter Minimum Order Statistic Gumbel [Type 1 Extreme Value] Distribution");
	                fout.println("y = (yscale/sigma)*exp((x - mu)/sigma))*exp(-exp((x-mu)/sigma))");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="mu";
	                paraName[1]="sigma";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 8: fout.println("Fitting to a Two Parameter Maximum Order Statistic Gumbel [Type 1 Extreme Value] Distribution");
	                fout.println("y = (yscale/sigma)*exp(-(x - mu)/sigma))*exp(-exp(-(x-mu)/sigma))");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="mu";
	                paraName[1]="sigma";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 9: fout.println("Fitting to a One Parameter Minimum Order Statistic Gumbel [Type 1 Extreme Value] Distribution");
	                fout.println("y = (yscale)*exp(x/sigma))*exp(-exp(x/sigma))");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
		            paraName[0]="sigma";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 10: fout.println("Fitting to a One Parameter Maximum Order Statistic Gumbel [Type 1 Extreme Value] Distribution");
	                fout.println("y = (yscale)*exp(-x/sigma))*exp(-exp(-x/sigma))");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
		            paraName[0]="sigma";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 11: fout.println("Fitting to a Standard Minimum Order Statistic Gumbel [Type 1 Extreme Value] Distribution");
	                fout.println("y = (yscale)*exp(x))*exp(-exp(x))");
	                fout.println("Linear regression used to fit y = yscale*z where z = exp(x))*exp(-exp(x)))");
	                if(this.scaleFlag)paraName[0]="y scale";
                    this.linearPrint(fout);
                    break;
            case 12: fout.println("Fitting to a Standard Maximum Order Statistic Gumbel [Type 1 Extreme Value] Distribution");
	                fout.println("y = (yscale)*exp(-x))*exp(-exp(-x))");
	                fout.println("Linear regression used to fit y = yscale*z where z = exp(-x))*exp(-exp(-x)))");
	                if(this.scaleFlag)paraName[0]="y scale";
                    this.linearPrint(fout);
                    break;
	        case 13: fout.println("Fitting to a Three Parameter Frechet [Type 2 Extreme Value] Distribution");
	                fout.println("y = yscale.(gamma/sigma)*((x - mu)/sigma)^(-gamma-1)*exp(-((x-mu)/sigma)^-gamma");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="mu";
	                paraName[1]="sigma";
	                paraName[2]="gamma";
	                if(this.scaleFlag)paraName[3]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 14: fout.println("Fitting to a Two parameter Frechet [Type2  Extreme Value] Distribution");
	                fout.println("y = yscale.(gamma/sigma)*(x/sigma)^(-gamma-1)*exp(-(x/sigma)^-gamma");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="sigma";
	                paraName[1]="gamma";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
  	        case 15: fout.println("Fitting to a Standard Frechet [Type 2 Extreme Value] Distribution");
	                fout.println("y = yscale.gamma*(x)^(-gamma-1)*exp(-(x)^-gamma");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="gamma";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
	        case 16: fout.println("Fitting to a Three parameter Weibull [Type 3 Extreme Value] Distribution");
	                fout.println("y = yscale.(gamma/sigma)*((x - mu)/sigma)^(gamma-1)*exp(-((x-mu)/sigma)^gamma");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="mu";
	                paraName[1]="sigma";
	                paraName[2]="gamma";
	                if(this.scaleFlag)paraName[3]="y scale";
                    this.nonLinearPrint(fout);
                    break;
  	        case 17: fout.println("Fitting to a Two parameter Weibull [Type 3 Extreme Value] Distribution");
	                fout.println("y = yscale.(gamma/sigma)*(x/sigma)^(gamma-1)*exp(-(x/sigma)^gamma");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="sigma";
	                paraName[1]="gamma";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
  	        case 18: fout.println("Fitting to a Standard Weibull [Type 3 Extreme Value] Distribution");
	                fout.println("y = yscale.gamma*(x)^(gamma-1)*exp(-(x)^gamma");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="gamma";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
		    case 19: fout.println("Fitting to a Two parameter Exponential Distribution");
	                fout.println("y = (yscale/sigma)*exp(-(x-mu)/sigma)");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="mu";
	                paraName[1]="sigma";
		            if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
  	        case 20: fout.println("Fitting to a One parameter Exponential Distribution");
	                fout.println("y = (yscale/sigma)*exp(-x/sigma)");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="sigma";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
  	        case 21: fout.println("Fitting to a Standard Exponential Distribution");
	                fout.println("y = yscale*exp(-x)");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                if(this.scaleFlag)paraName[0]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 22: fout.println("Fitting to a Rayleigh Distribution");
	                fout.println("y = (yscale/sigma)*(x/sigma)*exp(-0.5*(x/sigma)^2)");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="sigma";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 23: fout.println("Fitting to a Two Parameter Pareto Distribution");
	                fout.println("y = yscale*(alpha*beta^alpha)/(x^(alpha+1))");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="alpha";
	                paraName[1]="beta";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
             case 24: fout.println("Fitting to a One Parameter Pareto Distribution");
	                fout.println("y = yscale*(alpha)/(x^(alpha+1))");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="alpha";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
             case 25: fout.println("Fitting to a Sigmoidal Threshold Function");
	                fout.println("y = yscale/(1 + exp(-slopeTerm(x - theta)))");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="slope term";
	                paraName[1]="theta";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
             case 26: fout.println("Fitting to a Rectangular Hyperbola");
	                fout.println("y = yscale.x/(theta + x)");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="theta";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 27: fout.println("Fitting to a Scaled Heaviside Step Function");
	                fout.println("y = yscale.H(x - theta)");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="theta";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 28: fout.println("Fitting to a Hill/Sips Sigmoid");
	                fout.println("y = yscale.x^n/(theta^n + x^n)");
	                fout.println("Nelder and Mead Simplex used to fit the data)");
	                paraName[0]="theta";
	                paraName[1]="n";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;


             default: throw new IllegalArgumentException("Method number (this.lastMethod) not found");

		    }

		fout.close();
	}

	// Print the results of the regression
	// No file name provided
	public void print(){
		    String filename="RegressOutput.txt";
		    this.print(filename);
	}

	// Private method - print linear regression output
	private void linearPrint(FileOutput fout){

	    Double temp = null;
	    double[] coeff = new double[this.nTerms];
	    double[] coeffSd = new double[this.nTerms];

	    for(int i=0; i<this.nTerms; i++){
    	    temp = (Double) this.best.elementAt(i);
    	    coeff[i] = temp.doubleValue();
    	    temp = (Double) this.bestSd.elementAt(i);
    	    coeffSd[i] = temp.doubleValue();
	    }

	    if(this.legendCheck){
            fout.println();
            fout.println("x1 = " + this.xLegend);
            fout.println("y  = " + this.yLegend);
 	    }

        fout.println();
        fout.printtab(" ", this.field);
        fout.printtab("Best", this.field);
        fout.printtab("Standard", this.field);
        fout.println("Coefficient of");

        fout.printtab(" ", this.field);
        fout.printtab("Estimate");
        fout.printtab("Deviation", this.field);
        fout.println("variation (%)");

        for(int i=0; i<this.nTerms; i++){
            fout.printtab(this.paraName[i], this.field);
            fout.printtab(Fmath.truncate(coeff[i],this.prec), this.field);
            fout.printtab(Fmath.truncate(coeffSd[i],this.prec), this.field);
            fout.println(Fmath.truncate(coeffSd[i]*100.0D/coeff[i],this.prec));
        }
        fout.println();

        int ii=0;
        if(this.lastMethod<2)ii=1;
        for(int i=0; i<this.nXarrays; i++){
            fout.printtab("x"+String.valueOf(i+ii), this.field);
        }
        fout.printtab("y(expl)", this.field);
        fout.printtab("y(calc)", this.field);
        fout.printtab("weight", this.field);
        fout.printtab("residual", this.field);
        fout.println("residual");

        for(int i=0; i<this.nXarrays; i++){
            fout.printtab(" ", this.field);
        }
        fout.printtab(" ", this.field);
        fout.printtab(" ", this.field);
        fout.printtab(" ", this.field);
        fout.printtab("(unweighted)", this.field);
        fout.println("(weighted)");


        for(int i=0; i<this.nData; i++){
            for(int j=0; j<this.nXarrays; j++){
                fout.printtab(Fmath.truncate(this.xData[j][i],this.prec), this.field);
            }
            fout.printtab(Fmath.truncate(this.yData[i],this.prec), this.field);
            fout.printtab(Fmath.truncate(this.yCalc[i],this.prec), this.field);
            fout.printtab(Fmath.truncate(this.weight[i],this.prec), this.field);
            fout.printtab(Fmath.truncate(this.residual[i],this.prec), this.field);
            fout.println(Fmath.truncate(this.residualW[i],this.prec));
         }
        fout.println();
        fout.println("Sum of squares " + Fmath.truncate(this.sumOfSquares, this.prec));
		if(this.trueFreq){
		    fout.printtab("Chi Square (Poissonian bins)");
		    fout.println(Fmath.truncate(this.chiSquare,this.prec));
            fout.printtab("Reduced Chi Square (Poissonian bins)");
		    fout.println(Fmath.truncate(this.reducedChiSquare,this.prec));
            fout.printtab("Chi Square (Poissonian bins) Probability");
		    fout.println(Fmath.truncate((1.0D-Stat.chiSquareProb(this.chiSquare, this.nData-this.nXarrays)),this.prec));
		}
		else{
		    if(weightOpt){
	            fout.printtab("Chi Square");
		        fout.println(Fmath.truncate(this.chiSquare,this.prec));
                fout.printtab("Reduced Chi Square");
		        fout.println(Fmath.truncate(this.reducedChiSquare,this.prec));
                fout.printtab("Chi Square Probability");
		        fout.println(Fmath.truncate(this.getchiSquareProb(),this.prec));
		    }
		}
	    fout.println(" ");
	    fout.println("Correlation: x - y data");
	    if(this.nXarrays>1){
	        fout.printtab("Multiple Correlation Coefficient");
	        fout.println(Fmath.truncate(this.sampleR,this.prec));
	        if(this.sampleR2<=1.0D){
		        fout.printtab("Multiple Correlation Coefficient F-test ratio");
		        fout.println(Fmath.truncate(this.multipleF,this.prec));
		        fout.printtab("Multiple Correlation Coefficient F-test probability");
		        fout.println(Fmath.truncate(Stat.fTestProb(this.multipleF, this.nXarrays-1, this.nData-this.nXarrays),this.prec));
		    }
		}
	    else{
		    fout.printtab("Linear Correlation Coefficient");
	        fout.println(Fmath.truncate(this.sampleR,this.prec));
	        if(this.sampleR2<=1.0D){
		        fout.printtab("Linear Correlation Coefficient Probability");
		        fout.println(Fmath.truncate(Stat.linearCorrCoeffProb(this.sampleR, this.nData-this.nTerms),this.prec));
            }
        }

    	fout.println(" ");
	    fout.println("Correlation: y(experimental) - y(calculated");
        fout.printtab("Linear Correlation Coefficient");
        double ccyy = Stat.corrCoeff(this.yData, this.yCalc);

	    fout.println(Fmath.truncate(ccyy, this.prec));
		fout.printtab("Linear Correlation Coefficient Probability");
		fout.println(Fmath.truncate(Stat.linearCorrCoeffProb(ccyy, this.nData-1),this.prec));


        fout.println(" ");
        fout.printtab("Degrees of freedom");
		fout.println(this.nData - this.nTerms);
        fout.printtab("Number of data points");
		fout.println(this.nData);
        fout.printtab("Number of estimated paramaters");
		fout.println(this.nTerms);

        fout.println();
        if(this.chiSquare!=0.0D){
            fout.println("Correlation coefficients");
            fout.printtab(" ", this.field);
            for(int i=0; i<this.nTerms;i++){
                fout.printtab(paraName[i], this.field);
            }
            fout.println();

            for(int j=0; j<this.nTerms;j++){
                fout.printtab(paraName[j], this.field);
                for(int i=0; i<this.nTerms;i++){
                    fout.printtab(Fmath.truncate(this.corrCoeff[i][j], this.prec), this.field);
                }
                fout.println();
            }
        }

        fout.println();
        fout.println("End of file");

		fout.close();
	}

	// Private method - print non-linear regression output
	private void nonLinearPrint(FileOutput fout){

	    Double temp = null;
	    double[] coeff = new double[this.nTerms];
	    double[] coeffSd = new double[this.nTerms];

	    for(int i=0; i<this.nTerms; i++){
    	    temp = (Double) this.best.elementAt(i);
    	    coeff[i] = temp.doubleValue();
    	    temp = (Double) this.bestSd.elementAt(i);
    	    coeffSd[i] = temp.doubleValue();
	    }

        if(this.legendCheck){
            fout.println();
            fout.println("x1 = " + this.xLegend);
            fout.println("y  = " + this.yLegend);
 	    }

        fout.println();
        if(!this.nlrStatus){
            fout.println("Convergence criterion was not satisfied");
            fout.println("The following results are, or a derived from, the current estimates on exiting the regression method");
            fout.println();
        }

        fout.println("Estimated parameters");
        fout.println("The statistics are obtained assuming that the model behaves as a linear model about the minimum.");
        fout.println("The Hessian matrix is calculated as the numerically derived second derivatives of chi square with respect to all pairs of parameters.");
        if(this.zeroCheck)fout.println("The best estimate/s equal to zero were replaced by the step size in the numerical differentiation!!!");
        fout.println("Consequentlty treat the statistics with great caution");
        if(!this.posVarFlag){
            fout.println("Covariance matrix contains at least one negative diagonal element");
            fout.println(" - all variances are dubious");
            fout.println(" - may not be at a minimum");
        }
        if(!this.invertFlag){
            fout.println("Hessian matrix is singular");
            fout.println(" - variances cannot be calculated");
            fout.println(" - may not be at a minimum");
        }

        fout.println(" ");
        if(!this.scaleFlag){
            fout.println("The ordinate scaling factor [yscale, Ao] has been set equal to " + this.yScaleFactor);
            fout.println(" ");
        }
        fout.printtab(" ", this.field);
        fout.printtab("Best", this.field);
        if(this.invertFlag){
            fout.printtab("Estimate", this.field);
            fout.printtab("Coefficient", this.field);
        }
        fout.printtab("Pre-min", this.field);
        fout.printtab("Post-min", this.field);
        fout.printtab("Initial", this.field);
        fout.println("Fractional");

        fout.printtab(" ", this.field);
        fout.printtab("estimate", this.field);
        if(this.invertFlag){
            fout.printtab("of the sd", this.field);
            fout.printtab("of", this.field);
        }
        fout.printtab("gradient", this.field);
        fout.printtab("gradient", this.field);
        fout.printtab("estimate", this.field);
        fout.println("step");
        if(this.invertFlag){
            fout.printtab(" ", this.field);
            fout.printtab(" ", this.field);
            fout.printtab(" ", this.field);
            fout.println("variation (%)");
        }

        for(int i=0; i<this.nTerms; i++){
            fout.printtab(this.paraName[i], this.field);
            fout.printtab(Fmath.truncate(coeff[i],this.prec), this.field);
            if(invertFlag){
                fout.printtab(Fmath.truncate(coeffSd[i],this.prec), this.field);
                fout.printtab(Fmath.truncate(coeffSd[i]*100.0D/coeff[i],this.prec), this.field);
            }
            fout.printtab(Fmath.truncate(this.grad[i][0],this.prec), this.field);
            fout.printtab(Fmath.truncate(this.grad[i][1],this.prec), this.field);
            fout.printtab(Fmath.truncate(this.startH[i],this.prec), this.field);
            fout.println(Fmath.truncate(this.step[i],this.prec));
        }
        fout.println();

        ErrorProp ePeak = null;
        ErrorProp eYscale = null;
        if(this.scaleFlag){
            switch(this.lastMethod){
            case 4: ErrorProp eSigma = new ErrorProp(coeff[1], coeffSd[1]);
                    eYscale = new ErrorProp(coeff[2]/Math.sqrt(2.0D*Math.PI), coeffSd[2]/Math.sqrt(2.0D*Math.PI));
                    ePeak = eYscale.over(eSigma);
                    fout.printsp("Calculated estimate of the peak value = ");
                    fout.println(ErrorProp.truncate(ePeak, prec));
                    break;
            case 5: ErrorProp eGamma = new ErrorProp(coeff[1], coeffSd[1]);
                    eYscale = new ErrorProp(2.0D*coeff[2]/Math.PI, 2.0D*coeffSd[2]/Math.PI);
                    ePeak = eYscale.over(eGamma);
                    fout.printsp("Calculated estimate of the peak value = ");
                    fout.println(ErrorProp.truncate(ePeak, prec));
                    break;

            }
        }
        if(this.lastMethod==25){
            fout.printsp("Calculated estimate of the maximum gradient = ");
            if(this.scaleFlag){
                fout.println(Fmath.truncate(coeff[0]*coeff[2]/4.0D, prec));
            }
            else{
                fout.println(Fmath.truncate(coeff[0]*this.yScaleFactor/4.0D, prec));
            }

        }
        if(this.lastMethod==28){
            fout.printsp("Calculated estimate of the maximum gradient = ");
            if(this.scaleFlag){
                fout.println(Fmath.truncate(coeff[1]*coeff[2]/(4.0D*coeff[0]), prec));
            }
            else{
                fout.println(Fmath.truncate(coeff[1]*this.yScaleFactor/(4.0D*coeff[0]), prec));
            }
            fout.printsp("Calculated estimate of the Ka, i.e. theta raised to the power n = ");
            fout.println(Fmath.truncate(Math.pow(coeff[0], coeff[1]), prec));
        }
        fout.println();

        int kk=0;
        for(int j=0; j<nYarrays; j++){
            if(this.multipleY)fout.println("Y array " + j);

            for(int i=0; i<this.nXarrays; i++){
                fout.printtab("x"+String.valueOf(i), this.field);
            }

            fout.printtab("y(expl)", this.field);
            fout.printtab("y(calc)", this.field);
            fout.printtab("weight", this.field);
            fout.printtab("residual", this.field);
            fout.println("residual");

            for(int i=0; i<this.nXarrays; i++){
                fout.printtab(" ", this.field);
            }
            fout.printtab(" ", this.field);
            fout.printtab(" ", this.field);
            fout.printtab(" ", this.field);
            fout.printtab("(unweighted)", this.field);
            fout.println("(weighted)");
            for(int i=0; i<this.nData0; i++){
                for(int jj=0; jj<this.nXarrays; jj++){
                    fout.printtab(Fmath.truncate(this.xData[jj][kk],this.prec), this.field);
                }
                fout.printtab(Fmath.truncate(this.yData[kk],this.prec), this.field);
                fout.printtab(Fmath.truncate(this.yCalc[kk],this.prec), this.field);
                fout.printtab(Fmath.truncate(this.weight[kk],this.prec), this.field);
                fout.printtab(Fmath.truncate(this.residual[kk],this.prec), this.field);
                fout.println(Fmath.truncate(this.residualW[kk],this.prec));
                kk++;
            }
            fout.println();
        }

	    fout.printtab("Sum of squares of the unweighted residuals");
		fout.println(Fmath.truncate(this.sumOfSquares,this.prec));
	    if(this.trueFreq){
		    fout.printtab("Chi Square (Poissonian bins)");
		    fout.println(Fmath.truncate(this.chiSquare,this.prec));
            fout.printtab("Reduced Chi Square (Poissonian bins)");
		    fout.println(Fmath.truncate(this.reducedChiSquare,this.prec));
            fout.printtab("Chi Square (Poissonian bins) Probability");
		    fout.println(Fmath.truncate(1.0D-Stat.chiSquareProb(this.reducedChiSquare,this.degreesOfFreedom),this.prec));
		}
		else{
		    if(weightOpt){
	            fout.printtab("Chi Square");
		        fout.println(Fmath.truncate(this.chiSquare,this.prec));
                fout.printtab("Reduced Chi Square");
		        fout.println(Fmath.truncate(this.reducedChiSquare,this.prec));
                fout.printtab("Chi Square Probability");
		        fout.println(Fmath.truncate(this.getchiSquareProb(),this.prec));
		    }
		}

        fout.println(" ");
	    fout.println("Correlation: x - y data");
	    if(this.nXarrays>1){
	        fout.printtab("Multiple Correlation Coefficient");
	        fout.println(Fmath.truncate(this.sampleR, this.prec));
            if(this.sampleR2<=1.0D){
		        fout.printtab("Multiple Correlation Coefficient F-test ratio");
		        fout.println(Fmath.truncate(this.multipleF, this.prec));
		        fout.printtab("Multiple Correlation Coefficient F-test probability");
		        fout.println(Stat.fTestProb(this.multipleF, this.nXarrays-1, this.nData-this.nXarrays));
		    }
		}
		else{
		    fout.printtab("Linear Correlation Coefficient");
	        fout.println(Fmath.truncate(this.sampleR, this.prec));
            if(this.sampleR2<=1.0D){
		        fout.printtab("Linear Correlation Coefficient Probability");
		        fout.println(Fmath.truncate(Stat.linearCorrCoeffProb(this.sampleR, this.nData-this.nTerms),this.prec));
            }
        }

    	fout.println(" ");
	    fout.println("Correlation: y(experimental) - y(calculated)");
        fout.printtab("Linear Correlation Coefficient");
        double ccyy = Stat.corrCoeff(this.yData, this.yCalc);
	    fout.println(Fmath.truncate(ccyy, this.prec));
		fout.printtab("Linear Correlation Coefficient Probability");
		fout.println(Fmath.truncate(Stat.linearCorrCoeffProb(ccyy, this.nData-1),this.prec));

    	fout.println(" ");
        fout.printtab("Degrees of freedom");
		fout.println(this.degreesOfFreedom);
        fout.printtab("Number of data points");
		fout.println(this.nData);
        fout.printtab("Number of estimated paramaters");
		fout.println(this.nTerms);

        fout.println();

        if(this.posVarFlag && this.invertFlag && this.chiSquare!=0.0D){
            fout.println("Parameter - parameter correlation coefficients");
            fout.printtab(" ", this.field);
            for(int i=0; i<this.nTerms;i++){
                fout.printtab(paraName[i], this.field);
            }
            fout.println();

            for(int j=0; j<this.nTerms;j++){
                fout.printtab(paraName[j], this.field);
                for(int i=0; i<this.nTerms;i++){
                    fout.printtab(Fmath.truncate(this.corrCoeff[i][j], this.prec), this.field);
                }
                fout.println();
            }
            fout.println();
        }

        fout.println();
        fout.printtab("Number of iterations taken");
        fout.println(this.nIter);
        fout.printtab("Maximum number of iterations allowed");
        fout.println(this.nMax);
        fout.printtab("Number of restarts taken");
        fout.println(this.kRestart);
        fout.printtab("Maximum number of restarts allowed");
        fout.println(this.konvge);
        fout.printtab("Standard deviation of the simplex at the minimum");
        fout.println(Fmath.truncate(this.simplexSd, this.prec));
        fout.printtab("Convergence tolerance");
        fout.println(this.fTol);
        switch(minTest){
            case 0: fout.println("simplex sd < the tolerance times the mean of the absolute values of the y values");
                    break;
            case 1: fout.println("simplex sd < the tolerance");
                    break;
            case 2: fout.println("simplex sd < the tolerance times the square root(sum of squares/degrees of freedom");
                    break;
        }
        fout.println("Step used in numerical differentiation to obtain Hessian matrix");
        fout.println("d(parameter) = parameter*"+this.delta);

        fout.println();
        fout.println("End of file");
		fout.close();
	}

	// plot calculated y against experimental y
	// title provided
    public void plotYY(String title){
        this.graphTitle = title;
        int ncurves = 2;
        int npoints = this.nData0;
        double[][] data = PlotGraph.data(ncurves, npoints);

        int kk = 0;
        for(int jj=0; jj<this.nYarrays; jj++){

            // fill first curve with experimental versus best fit values
            for(int i=0; i<nData0; i++){
                data[0][i]=this.yData[kk];
                data[1][i]=this.yCalc[kk];
                kk++;
            }

            // Create a title
            String title0 = this.setGandPtitle(this.graphTitle);
            if(this.multipleY)title = title0 + "y array " + jj;

            // Calculate best fit straight line between experimental and best fit values
            Regression yyRegr = new Regression(this.yData, this.yCalc, this.weight);
            yyRegr.linear();
            double[] coef = yyRegr.getCoeff();
            data[2][0]=Fmath.minimum(this.yData);
            data[3][0]=coef[0]+coef[1]*data[2][0];
            data[2][1]=Fmath.maximum(this.yData);
            data[3][1]=coef[0]+coef[1]*data[2][1];

            PlotGraph pg = new PlotGraph(data);

            pg.setGraphTitle(title);
            pg.setXaxisLegend("Experimental y value");
            pg.setYaxisLegend("Calculated y value");
            int[] popt = {1, 0};
            pg.setPoint(popt);
            int[] lopt = {0, 3};
            pg.setLine(lopt);

            pg.plot();
        }
    }

    //Creates a title
    private String setGandPtitle(String title){
        String title1 = "";
        switch(this.lastMethod){
	        case 0: title1 = "Linear regression (with intercept): "+title;
	                break;
	        case 1: title1 = "Linear(polynomial with degree = " + (nTerms-1) + ") regression: "+title;
	                break;
	        case 2: title1 = "General linear regression: "+title;
	                break;
	        case 3: title1 = "Non-linear (simplex) regression: "+title;
	                break;
	        case 4: title1 = "Fit to a Gaussian distribution: "+title;
	                break;
	        case 5: title1 = "Fit to a Lorentzian distribution: "+title;
	                break;
	        case 6:title1 = "Fit to a Poisson distribution: "+title;
	                break;
		    case 7: title1 = "Fit to a Two Parameter Minimum Order Statistic Gumbel distribution: "+title;
	                break;
            case 8: title1 = "Fit to a two Parameter Maximum Order Statistic Gumbel distribution: "+title;
	                break;
	        case 9: title1 = "Fit to a One Parameter Minimum Order Statistic Gumbel distribution: "+title;
	                break;
	        case 10: title1 = "Fit to a One Parameter Maximum Order Statistic Gumbel distribution: "+title;
	                break;
            case 11: title1 = "Fit to a Standard Minimum Order Statistic Gumbel distribution: "+title;
	                break;
            case 12: title1 = "Fit to a Standard Maximum Order Statistic Gumbel distribution: "+title;
	                break;
	        case 13:title1 = "Fit to a Three Parameter Frechet distribution: "+title;
	                break;
	        case 14:title1 = "Fit to a Two Parameter Frechet distribution: "+title;
	                break;
	        case 15:title1 = "Fit to a Standard Frechet distribution: "+title;
	                break;
	        case 16:title1 = "Fit to a Three Parameter Weibull distribution: "+title;
	                break;
	        case 17:title1 = "Fit to a Two Parameter Weibull distribution: "+title;
	                break;
	        case 18:title1 = "Fit to a Standard Weibull distribution: "+title;
	                break;
	        case 19:title1 = "Fit to a Two Parameter Exponential distribution: "+title;
	                break;
	        case 20:title1 = "Fit to a One Parameter Exponential distribution: "+title;
	                break;
	        case 21:title1 = "Fit to a Standard exponential distribution: "+title;
	                break;
	        case 22:title1 = "Fit to a Rayleigh distribution: "+title;
	                break;
	        case 23:title1 = "Fit to a General Pareto distribution: "+title;
	                break;
	        case 24:title1 = "Fit to a General Pareto distribution: "+title;
	                break;
	        case 25:title1 = "Fit to a Sigmoid Threshold Function: "+title;
	                break;
	        case 26:title1 = "Fit to a Rectangular Hyperbola: "+title;
	                break;
	        case 27:title1 = "Fit to a Scaled Heaviside Step Function: "+title;
	                break;
	        case 28:title1 = "Fit to a Hill/Sips Sigmoid: "+title;
	                break;

	        default: title1 = " "+title;
	    }
	    return title1;
    }

	// plot calculated y against experimental y
	// no title provided
    public void plotYY(){
        plotYY(this.graphTitle);
    }

    // plot experimental x against experimental y and against calculated y
    // linear regression data
	// title provided
    private int plotXY(String title){
        this.graphTitle = title;
        int flag=0;
        if(!this.linNonLin && this.nTerms>0){
            System.out.println("You attempted to use Regression.plotXY() for a non-linear regression without providing the function reference (pointer) in the plotXY argument list");
            System.out.println("No plot attempted");
            flag=-1;
            return flag;
        }
        flag = this.plotXYlinear(title);
        return flag;
    }

    // plot experimental x against experimental y and against calculated y
    // Linear regression data
	// no title provided
    public int plotXY(){
        int flag = plotXY(this.graphTitle);
        return flag;
    }

    // plot experimental x against experimental y and against calculated y
    // non-linear regression data
	// title provided
	// matching simplex
    private int plotXY(RegressionFunction g, String title){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y array\nplotXY2 should have been called");
        this.graphTitle = title;
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        int flag = this.plotXYnonlinear(vec, title);
        return flag;
    }

    // plot experimental x against experimental y and against calculated y
    // non-linear regression data
	// title provided
	// matching simplex2
    private int plotXY2(RegressionFunction2 g, String title){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        this.graphTitle = title;
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        int flag = this.plotXYnonlinear(vec, title);
        return flag;
    }

    // plot experimental x against experimental y and against calculated y
    // non-linear regression data
	// no title provided
	// matches simplex
    private int plotXY(RegressionFunction g){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y array\nplotXY2 should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        int flag = this.plotXYnonlinear(vec, this.graphTitle);
        return flag;
    }

    // plot experimental x against experimental y and against calculated y
    // non-linear regression data
	// no title provided
	// matches simplex2
    private int plotXY2(RegressionFunction2 g){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nplotXY should have been called");
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(g);
        int flag = this.plotXYnonlinear(vec, this.graphTitle);
        return flag;
    }

    // Add legends option
    public void addLegends(){
        int ans = JOptionPane.showConfirmDialog(null, "Do you wish to add your own legends to the x and y axes", "Axis Legends", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if(ans==0){
            this.xLegend = JOptionPane.showInputDialog("Type the legend for the abscissae (x-axis) [first data set]" );
            this.yLegend = JOptionPane.showInputDialog("Type the legend for the ordinates (y-axis) [second data set]" );
            this.legendCheck = true;
        }
    }

    // private method for plotting experimental x against experimental y and against calculated y
	// Linear regression
	// title provided
    private int plotXYlinear(String title){
        this.graphTitle = title;
        int flag=0;  //Returned as 0 if plot data can be plotted, -1 if not, -2 if tried multiple regression plot
        if(this.nXarrays>1){
            System.out.println("You attempted to use Regression.plotXY() for a multiple regression");
            System.out.println("No plot attempted");
            flag=-2;
            return flag;
        }

	    double[] coeff = new double[this.nTerms];
	    Double temp = null;

        for(int i=0; i<this.nTerms; i++){
    	    temp = (Double) this.best.elementAt(i);
    	    coeff[i] = temp.doubleValue();
        }

        int ncurves = 2;
        int npoints = 200;
        if(npoints<this.nData0)npoints=this.nData0;
        if(this.lastMethod==11 || this.lastMethod==12 || this.lastMethod==21)npoints=this.nData0;
        double[][] data = PlotGraph.data(ncurves, npoints);
        double xmin =Fmath.minimum(xData[0]);
        double xmax =Fmath.maximum(xData[0]);
        double inc = (xmax - xmin)/(double)(npoints - 1);
        String title1 = " ";
        String title2 = " ";

        for(int i=0; i<nData0; i++){
            data[0][i] = this.xData[0][i];
            data[1][i] = this.yData[i];
        }

        data[2][0]=xmin;
        for(int i=1; i<npoints; i++)data[2][i] = data[2][i-1] + inc;
        if(this.nTerms==0){
            switch(this.lastMethod){
	        case 11: title1 = "No regression: Minimum Order Statistic Standard Gumbel (y = exp(x)exp(-exp(x))): "+this.graphTitle;
                    title2 = " points - experimental values;   line - theoretical curve;   no parameters to be estimated";
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                for(int i=0; i<npoints; i++)data[3][i] = this.yCalc[i];
	                break;
	        case 12: title1 = "No regression:  Maximum Order Statistic Standard Gumbel (y = exp(-x)exp(-exp(-x))): "+this.graphTitle;
                    title2 = " points - experimental values;   line - theoretical curve;   no parameters to be estimated";
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                for(int i=0; i<npoints; i++)data[3][i] = this.yCalc[i];
	                break;
	        case 21: title1 = "No regression:  Standard Exponential (y = exp(-x)): "+this.graphTitle;
                    title2 = " points - experimental values;   line - theoretical curve;   no parameters to be estimated";
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                for(int i=0; i<npoints; i++)data[3][i] = this.yCalc[i];
	                break;
	        }

        }
        else{
	        switch(this.lastMethod){
	        case 0: title1 = "Linear regression  (y = a + b.x): "+this.graphTitle;
                    title2 = " points - experimental values;   line - best fit curve";
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                for(int i=0; i<npoints; i++)data[3][i] = coeff[0] + coeff[1]*data[2][i];
	                break;
	        case 1: title1 = "Linear (polynomial with degree = " + (nTerms-1) + ") regression: "+this.graphTitle;
	                title2 = " points - experimental values;   line - best fit curve";
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                for(int i=0; i<npoints; i++){
                        double sum=coeff[0];
                        for(int j=1; j<this.nTerms; j++)sum+=coeff[j]*Math.pow(data[2][i],j);
                        data[3][i] = sum;
                    }
	                break;
	        case 2: title1 = "Linear regression  (y = a.x): "+this.graphTitle;
                    title2 = " points - experimental values;   line - best fit curve";
	                if(this.nXarrays==1){
	                    if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                    for(int i=0; i<npoints; i++)data[3][i] = coeff[0]*data[2][i];
	                }
	                else{
	                    System.out.println("Regression.plotXY(linear): lastMethod, "+lastMethod+",cannot be plotted in two dimensions");
	                    System.out.println("No plot attempted");
	                    flag=-1;
	                }
	                break;
	        case 11: title1 = "Linear regression: Minimum Order Statistic Standard Gumbel (y = a.z where z = exp(x)exp(-exp(x))): "+this.graphTitle;
                    title2 = " points - experimental values;   line - best fit curve";
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                for(int i=0; i<npoints; i++)data[3][i] = coeff[0]*Math.exp(data[2][i])*Math.exp(-Math.exp(data[2][i]));
	                break;
	        case 12: title1 = "Linear regression:  Maximum Order Statistic Standard Gumbel (y = a.z where z=exp(-x)exp(-exp(-x))): "+this.graphTitle;
                    title2 = " points - experimental values;   line - best fit curve";
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                for(int i=0; i<npoints; i++)data[3][i] = coeff[0]*Math.exp(-data[2][i])*Math.exp(-Math.exp(-data[2][i]));
	                break;
	        default: System.out.println("Regression.plotXY(linear): lastMethod, "+lastMethod+", either not recognised or cannot be plotted in two dimensions");
	                System.out.println("No plot attempted");
	                flag=-1;
	                return flag;
	        }
	    }

        PlotGraph pg = new PlotGraph(data);

        pg.setGraphTitle(title1);
        pg.setGraphTitle2(title2);
        pg.setXaxisLegend(this.xLegend);
        pg.setYaxisLegend(this.yLegend);
        int[] popt = {1,0};
        pg.setPoint(popt);
        int[] lopt = {0,3};
        pg.setLine(lopt);
        if(weightOpt)pg.setErrorBars(0,this.weight);
        pg.plot();

        return flag;
	}

    // private method for plotting experimental x against experimental y and against calculated y
	// Non-linear regression
	// title provided
    public int plotXYnonlinear(Vector vec, String title){
        this.graphTitle = title;
        RegressionFunction g1 = null;
	    RegressionFunction2 g2 = null;
	    if(this.multipleY){
            g2 = (RegressionFunction2)vec.elementAt(0);
        }
        else{
            g1 = (RegressionFunction)vec.elementAt(0);
        }

        int flag=0;  //Returned as 0 if plot data can be plotted, -1 if not
	    double[] coeff = new double[this.nTerms];
	    Double temp = null;

        if(this.lastMethod<3){
	        System.out.println("Regression.plotXY(non-linear): lastMethod, "+lastMethod+", either not recognised or cannot be plotted in two dimensions");
	        System.out.println("No plot attempted");
	        flag=-1;
	        return flag;
	    }


	    for(int i=0; i<this.nTerms; i++){
    	    temp = (Double) this.best.elementAt(i);
    	    coeff[i] = temp.doubleValue();
        }

	    if(this.nXarrays>1){
	        System.out.println("Multiple Linear Regression with more than one independent variable cannot be plotted in two dimensions");
            System.out.println("plotYY() called instead of plotXY()");
            this.plotYY(title);
            flag=-2;
        }
	    else{
	        if(this.multipleY){
	            int ncurves = 2;
                int npoints = 200;
                if(npoints<this.nData0)npoints=this.nData0;
                String title1, title2;
                int kk=0;
                double[] wWeight = new double[this.nData0];
                for(int jj=0; jj<this.nYarrays; jj++){
                    double[][] data = PlotGraph.data(ncurves, npoints);
                    for(int i=0; i<this.nData0; i++){
                        data[0][i] = this.xData[0][kk];
                        data[1][i] = this.yData[kk];
                        wWeight[i] = this.weight[kk];
                        kk++;
                    }
                    double xmin =Fmath.minimum(xData[0]);
                    double xmax =Fmath.maximum(xData[0]);
                    double inc = (xmax - xmin)/(double)(npoints - 1);
                    data[2][0]=xmin;
                    for(int i=1; i<npoints; i++)data[2][i] = data[2][i-1] + inc;
                    double[] xd = new double[this.nXarrays];
                    for(int i=0; i<npoints; i++){
                        xd[0] = data[2][i];
                        data[3][i] = g2.function(coeff, xd, jj*this.nData0);
                    }

                    // Create a title
 	                title1 = this.setGandPtitle(title);
    	            title2 = " points - experimental values;   line - best fit curve;  y data array " + jj;
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";

                    PlotGraph pg = new PlotGraph(data);

                    pg.setGraphTitle(title1);
                    pg.setGraphTitle2(title2);
                    pg.setXaxisLegend(this.xLegend);
                    pg.setYaxisLegend(this.yLegend);
                    int[] popt = {1,0};
                    pg.setPoint(popt);
                    int[] lopt = {0,3};
                    pg.setLine(lopt);
                    if(weightOpt)pg.setErrorBars(0,wWeight);

                    pg.plot();
                }
	        }
	        else{
                int ncurves = 2;
                int npoints = 200;
                if(npoints<this.nData0)npoints=this.nData0;
                if(this.lastMethod==6)npoints=this.nData0;
                String title1, title2;
                double[][] data = PlotGraph.data(ncurves, npoints);
                for(int i=0; i<this.nData0; i++){
                    data[0][i] = this.xData[0][i];
                    data[1][i] = this.yData[i];
                }
                if(this.lastMethod==6){
                    double[] xd = new double[this.nXarrays];
                    for(int i=0; i<npoints; i++){
                        data[2][i]=data[0][i];
                        xd[0] = data[2][i];
                        data[3][i] = g1.function(coeff, xd);
                    }
                }
                else{
                    double xmin =Fmath.minimum(xData[0]);
                    double xmax =Fmath.maximum(xData[0]);
                    double inc = (xmax - xmin)/(double)(npoints - 1);
                    data[2][0]=xmin;
                    for(int i=1; i<npoints; i++)data[2][i] = data[2][i-1] + inc;
                    double[] xd = new double[this.nXarrays];
                    for(int i=0; i<npoints; i++){
                        xd[0] = data[2][i];
                        data[3][i] = g1.function(coeff, xd);
                    }
                }

                // Create a title
 	            title1 = this.setGandPtitle(title);
    	        title2 = " points - experimental values;   line - best fit curve";
	            if(weightOpt)title2 = title2 +";   error bars - weighting factors";

                PlotGraph pg = new PlotGraph(data);

                pg.setGraphTitle(title1);
                pg.setGraphTitle2(title2);
                pg.setXaxisLegend(this.xLegend);
                pg.setYaxisLegend(this.yLegend);
                int[] popt = {1,0};
                pg.setPoint(popt);
                int[] lopt = {0,3};
                pg.setLine(lopt);
                if(weightOpt)pg.setErrorBars(0,this.weight);

                pg.plot();
	        }
	    }
        return flag;
	}

    // Get the non-linear regression status
    // true if convergence was achieved
    // false if convergence not achieved before maximum number of iterations
    //  current values then returned
    public boolean getNlrStatus(){
        return this.nlrStatus;
    }

    // Reset scaling factors (scaleOpt 0 and 1, see below for scaleOpt 2)
    public void setScale(int n){
        if(n<0 || n>1)throw new IllegalArgumentException("The argument must be 0 (no scaling) 1(initial estimates all scaled to unity) or the array of scaling factors");
        this.scaleOpt=n;
    }

    // Reset scaling factors (scaleOpt 2, see above for scaleOpt 0 and 1)
    public void setScale(double[] sc){
        this.scale=sc;
        this.scaleOpt=2;
    }

    // Get scaling factors
    public double[] getScale(){
        return this.scale;
    }

	// Reset the non-linear regression convergence test option
	public void setMinTest(int n){
	    if(n<0 || n>1)throw new IllegalArgumentException("minTest must be 0 or 1");
	    this.minTest=n;
	}

    // Get the non-linear regression convergence test option
	public int getMinTest(){
	    return this.minTest;
	}

	// Get the simplex sd at the minimum
	public double getSimplexSd(){
	    return this.simplexSd;
	}

	// Get the best estimates of the unknown parameters
	public double[] getCoeff(){
	    double[] coeff = new double[this.nTerms];
	    Double temp = null;
	    for(int i=0; i<this.nTerms; i++){
    	    temp = (Double)this.best.elementAt(i);
    	    coeff[i] = temp.doubleValue();
 	    }
	    return coeff;
	}

	// Get the estimates of the standard deviations of the best estimates of the unknown parameters
	public double[] getCoeffSd(){
	    double[] coeffSd = new double[this.nTerms];
	    Double temp = null;
	    for(int i=0; i<this.nTerms; i++){
    	    temp = (Double) this.bestSd.elementAt(i);
    	    coeffSd[i] = temp.doubleValue();
 	    }
	    return coeffSd;
	}

	// Get the cofficients of variations of the best estimates of the unknown parameters
	public double[] getCoeffVar(){
	    double[] coeffVar = new double[this.nTerms];
	    double coeff = 0.0D;
	    double coeffSd = 0.0D;

	    Double temp = null;
	    for(int i=0; i<this.nTerms; i++){
    	    temp = (Double) this.best.elementAt(i);
    	    coeff = temp.doubleValue();
    	    temp = (Double) this.bestSd.elementAt(i);
    	    coeffSd = temp.doubleValue();
    	    coeffVar[i]=coeffSd*100.0D/coeff;
 	    }
	    return coeffVar;
	}

	// Get the pseudo-estimates of the standard deviations of the best estimates of the unknown parameters
	public double[] getPseudoSd(){

	    return (double[])pseudoSd.clone();
	}


	// Get the inputted x values
	public double[][] getXdata(){
	    return (double[][])xData.clone();
	}

    // Get the inputted y values
	public double[] getYdata(){
	    return (double[])yData.clone();
	}

	// Get the calculated y values
	public double[] getYcalc(){
	    double[] temp = new double[this.nData];
	    for(int i=0; i<this.nData; i++)temp[i]=this.yCalc[i];
	    return temp;
	}

	// Get the unweighted residuals, y(experimental) - y(calculated)
	public double[] getResiduals(){
	    double[] temp = new double[this.nData];
	    for(int i=0; i<this.nData; i++)temp[i]=this.yData[i]-this.yCalc[i];
	    return temp;
	}

	// Get the weighted residuals, (y(experimental) - y(calculated))/weight
	public double[] getWeightedResiduals(){

	    double[] temp = new double[this.nData];
	    for(int i=0; i<this.nData; i++)temp[i]=(this.yData[i]-this.yCalc[i])/weight[i];
	    return temp;
	}

	// Get the unweighted sum of squares of the residuals
	public double getSumOfSquares(){
	    return this.sumOfSquares;
	}

	// Get the chi square estimate
	public double getChiSquare(){
	    double ret=0.0D;
	    if(weightOpt){
		    ret = this.chiSquare;
	    }
	    else{
	        System.out.println("Chi Square cannot be calculated as data are neither true frequencies nor weighted");
	        System.out.println("A value of -1 is returned as Chi Square");
	        ret = -1.0D;
	    }
	    return ret;
	}

	// Get the reduced chi square estimate
	public double getReducedChiSquare(){
	    double ret=0.0D;
	    if(weightOpt){
	        ret = this.reducedChiSquare;
		}
	    else{
	        System.out.println("A Reduced Chi Square cannot be calculated as data are neither true frequencies nor weighted");
	        System.out.println("A value of -1 is returned as Reduced Chi Square");
	        ret = -1.0D;
	    }
	    return ret;
	}

	// Get the chi square probablity
	public double getchiSquareProb(){
	    double ret=0.0D;
	    if(weightOpt){
	        ret = 1.0D-Stat.chiSquareProb(this.chiSquare, this.nData-this.nXarrays);
	    }
		else{
	        System.out.println("A Chi Square probablity cannot be calculated as data are neither true frequencies nor weighted");
	        System.out.println("A value of -1 is returned as Reduced Chi Square");
	        ret = -1.0D;
	    }
	    return ret;
	}

	// Get the covariance matrix
	public double[][] getCovMatrix(){
	    return this.covar;
	}

	// Get the correlation coefficient matrix
	public double[][] getCorrCoeffMatrix(){
	    return this.corrCoeff;
	}

	// Get the number of iterations in nonlinear regression
	public int getNiter(){
	    return this.nIter;
	}


	// Set the maximum number of iterations allowed in nonlinear regression
	public void setNmax(int nmax){
	    this.nMax = nmax;
	}

	// Get the maximum number of iterations allowed in nonlinear regression
	public int getNmax(){
	    return this.nMax;
	}

	// Get the number of restarts in nonlinear regression
	public int getNrestarts(){
	    return this.kRestart;
	}

    // Set the maximum number of restarts allowed in nonlinear regression
	public void setNrestartsMax(int nrs){
	    this.konvge = nrs;
	}

	// Get the maximum number of restarts allowed in nonlinear regression
	public int getNrestartsMax(){
	    return this.konvge;
	}

	// Get the degrees of freedom
	public double getDegFree(){
	    return (this.degreesOfFreedom);
	}

	// Reset the Nelder and Mead reflection coefficient [alpha]
	public void setNMreflect(double refl){
	    this.rCoeff = refl;
	}

	// Get the Nelder and Mead reflection coefficient [alpha]
	public double getNMreflect(){
	    return this.rCoeff;
	}

    // Reset the Nelder and Mead extension coefficient [beta]
	public void setNMextend(double ext){
	    this.eCoeff = ext;
	}
	// Get the Nelder and Mead extension coefficient [beta]
	public double getNMextend(){
	    return this.eCoeff;
	}

	// Reset the Nelder and Mead contraction coefficient [gamma]
	public void setNMcontract(double con){
	    this.cCoeff = con;
	}

	// Get the Nelder and Mead contraction coefficient [gamma]
	public double getNMcontract(){
	    return cCoeff;
	}

	// Set the non-linear regression tolerance
	public void setTolerance(double tol){
	    this.fTol = tol;
	}


	// Get the non-linear regression tolerance
	public double getTolerance(){
	    return this.fTol;
	}

	// Get the non-linear regression pre and post minimum gradients
	public double[][] getGrad(){
	    return this.grad;
	}

	// Set the non-linear regression fractional step size used in numerical differencing
	public void setDelta(double delta){
	    this.delta = delta;
	}

	// Get the non-linear regression fractional step size used in numerical differencing
	public double getDelta(){
	    return this.delta;
	}

	// Get the non-linear regression statistics Hessian matrix inversion status flag
	public boolean getInversionCheck(){
	    return this.invertFlag;
	}

	// Get the non-linear regression statistics Hessian matrix inverse diagonal status flag
	public boolean getPosVarCheck(){
	    return this.posVarFlag;
	}

    // Test of an additional terms  {extra sum of squares]
    // return F-ratio, probability, order check and values provided in order used
    public static Vector<Object> testOfAdditionalTerms(double chiSquareR, int nParametersR, double chiSquareF, int nParametersF, int nPoints){
        int degFreedomR = nPoints - nParametersR;
        int degFreedomF = nPoints - nParametersF;

        // Check that model 2 has the lowest degrees of freedom
        boolean reversed = false;
        if(degFreedomR<degFreedomF){
            reversed = true;
            double holdD = chiSquareR;
            chiSquareR = chiSquareF;
            chiSquareF = holdD;
            int holdI = nParametersR;
            nParametersR = nParametersF;
            nParametersF = holdI;
            degFreedomR = nPoints - nParametersR;
            degFreedomF = nPoints - nParametersF;
            System.out.println("package flanagan.analysis; class Regression; method testAdditionalTerms");
            System.out.println("the order of the chi-squares has been reversed to give a second chi- square with the lowest degrees of freedom");
        }
        int degFreedomD = degFreedomR - degFreedomF;

        // F ratio
        double numer = (chiSquareR - chiSquareF)/degFreedomD;
        double denom = chiSquareF/degFreedomF;
        double fRatio = numer/denom;

        // Probability
        double fProb = 1.0D;
        if(chiSquareR>chiSquareF){
            fProb = Stat.fTestProb(fRatio, degFreedomD, degFreedomF);
        }

        // Return vector
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(new Double(fRatio));
        vec.addElement(new Double(fProb));
        vec.addElement(new Boolean(reversed));
        vec.addElement(new Double(chiSquareR));
        vec.addElement(new Integer(nParametersR));
        vec.addElement(new Double(chiSquareF));
        vec.addElement(new Integer(nParametersF));
        vec.addElement(new Integer(nPoints));

        return vec;
    }

    // Test of an additional terms  {extra sum of squares]
    // return F-ratio only
    public double testOfAdditionalTermsFratio(double chiSquareR, int nParametersR, double chiSquareF, int nParametersF, int nPoints){
        int degFreedomR = nPoints - nParametersR;
        int degFreedomF = nPoints - nParametersF;

        // Check that model 2 has the lowest degrees of freedom
        boolean reversed = false;
        if(degFreedomR<degFreedomF){
            reversed = true;
            double holdD = chiSquareR;
            chiSquareR = chiSquareF;
            chiSquareF = holdD;
            int holdI = nParametersR;
            nParametersR = nParametersF;
            nParametersF = holdI;
            degFreedomR = nPoints - nParametersR;
            degFreedomF = nPoints - nParametersF;
            System.out.println("package flanagan.analysis; class Regression; method testAdditionalTermsFratio");
            System.out.println("the order of the chi-squares has been reversed to give a second chi- square with the lowest degrees of freedom");
        }
        int degFreedomD = degFreedomR - degFreedomF;

        // F ratio
        double numer = (chiSquareR - chiSquareF)/degFreedomD;
        double denom = chiSquareF/degFreedomF;
        double fRatio = numer/denom;

        return fRatio;
    }

    // Test of an additional terms  {extra sum of squares]
    // return F-distribution probablity only
    public double testOfAdditionalTermsFprobabilty(double chiSquareR, int nParametersR, double chiSquareF, int nParametersF, int nPoints){
        int degFreedomR = nPoints - nParametersR;
        int degFreedomF = nPoints - nParametersF;

        // Check that model 2 has the lowest degrees of freedom
        boolean reversed = false;
        if(degFreedomR<degFreedomF){
            reversed = true;
            double holdD = chiSquareR;
            chiSquareR = chiSquareF;
            chiSquareF = holdD;
            int holdI = nParametersR;
            nParametersR = nParametersF;
            nParametersF = holdI;
            degFreedomR = nPoints - nParametersR;
            degFreedomF = nPoints - nParametersF;
            System.out.println("package flanagan.analysis; class Regression; method testAdditionalTermsFprobability");
            System.out.println("the order of the chi-squares has been reversed to give a second chi- square with the lowest degrees of freedom");
        }
        int degFreedomD = degFreedomR - degFreedomF;

        // F ratio
        double numer = (chiSquareR - chiSquareF)/degFreedomD;
        double denom = chiSquareF/degFreedomF;
        double fRatio = numer/denom;

        // Probability
        double fProb = 1.0D;
        if(chiSquareR>chiSquareF){
            fProb = Stat.fTestProb(fRatio, degFreedomD, degFreedomF);
        }

        return fProb;
    }



    // FIT TO SPECIAL FUNCTIONS
	// Fit to a Poisson distribution
	public void poisson(){
	    this.fitPoisson(0);
	}

	// Fit to a Poisson distribution
	public void poissonPlot(){
	    this.fitPoisson(1);
	}

	private void fitPoisson(int plotFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=6;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nTerms=2;
	    if(!this.scaleFlag)this.nTerms=2;
	    this.degreesOfFreedom=this.nData - this.nTerms;
	    if(this.degreesOfFreedom<1)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // Check all abscissae are integers
	    for(int i=0; i<this.nData; i++){
	        if(xData[0][i]-Math.floor(xData[0][i])!=0.0D)throw new IllegalArgumentException("all abscissae must be, mathematically, integer values");
	    }

	    // Calculate  x value at peak y (estimate of the distribution mean)
	    Vector<Object> ret1 = Regression.dataSign(yData);
	 	Double tempd = null;
	 	Integer tempi = null;
	    tempi = (Integer)ret1.elementAt(5);
	 	int peaki = tempi.intValue();
	    double mean = xData[0][peaki];

	    // Calculate peak value
	    tempd = (Double)ret1.elementAt(4);
	    double peak = tempd.doubleValue();

	    // Fill arrays needed by the Simplex
        double[] start = new double[this.nTerms];
        double[] step = new double[this.nTerms];
        start[0] = mean;
        if(this.scaleFlag){
            start[1] = peak/(Math.exp(mean*Math.log(mean)-Stat.logFactorial(mean))*Math.exp(-mean));
        }
        step[0] = 0.1D*start[0];
        if(step[0]==0.0D){
            Vector<Object> ret0 = Regression.dataSign(xData[0]);
	 	    Double tempdd = null;
	        tempdd = (Double)ret0.elementAt(2);
	 	    double xmax = tempdd.doubleValue();
	 	    if(xmax==0.0D){
	 	        tempdd = (Double)ret0.elementAt(0);
	 	        xmax = tempdd.doubleValue();
	 	    }
	        step[0]=xmax*0.1D;
	    }
        if(this.scaleFlag)step[1] = 0.1D*start[1];

	    // Nelder and Mead Simplex Regression
        PoissonFunction f = new PoissonFunction();
        this.addConstraint(1,-1,0.0D);
        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;
        Vector<Object> vec2 = new Vector<Object>();
        vec2.addElement(f);
        this.nelderMead(vec2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();
            // Plot results
            this.plotOpt=false;
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
	}

	// Fit to a Gaussian
	public void gaussian(){
	    this.fitGaussian(0);
	}

	// Fit to a Gaussian
	public void gaussianPlot(){
	    this.fitGaussian(1);
	}

    // Fit data to a Gaussian (normal) probability function
	private void fitGaussian(int plotFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=4;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nTerms=3;
	    if(!this.scaleFlag)this.nTerms=2;
	    this.degreesOfFreedom=this.nData - this.nTerms;
	    if(this.degreesOfFreedom<1)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

        // check sign of y data
	    Double tempd=null;
	    Vector<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.elementAt(4);
	    double yPeak = tempd.doubleValue();
	    boolean yFlag = false;
	    if(yPeak<0.0D){
	        System.out.println("Regression.fitGaussian(): This implementation of the Gaussian distribution takes only positive y values\n(noise taking low values below zero are allowed)");
	        System.out.println("All y values have been multiplied by -1 before fitting");
	        for(int i =0; i<this.nData; i++){
	                yData[i] = -yData[i];
	        }
	        retY = Regression.dataSign(yData);
	        yFlag=true;
	    }

	    // Calculate  x value at peak y (estimate of the Gaussian mean)
	    Vector<Object> ret1 = Regression.dataSign(yData);
	 	Integer tempi = null;
	    tempi = (Integer)ret1.elementAt(5);
	 	int peaki = tempi.intValue();
	    double mean = xData[0][peaki];

	    // Calculate an estimate of the sd
	    double sd = Math.sqrt(2.0D)*halfWidth(xData[0], yData);

	    // Calculate estimate of y scale
	    tempd = (Double)ret1.elementAt(4);
	    double ym = tempd.doubleValue();
	    ym=ym*sd*Math.sqrt(2.0D*Math.PI);

        // Fill arrays needed by the Simplex
        double[] start = new double[this.nTerms];
        double[] step = new double[this.nTerms];
        start[0] = mean;
        start[1] = ym;
        if(this.scaleFlag){
            start[2] = ym;
        }
        step[0] = 0.1D*sd;
        step[1] = 0.1D*start[1];
        if(step[1]==0.0D){
            Vector<Object> ret0 = Regression.dataSign(xData[0]);
	 	    Double tempdd = null;
	        tempdd = (Double)ret0.elementAt(2);
	 	    double xmax = tempdd.doubleValue();
	 	    if(xmax==0.0D){
	 	        tempdd = (Double)ret0.elementAt(0);
	 	        xmax = tempdd.doubleValue();
	 	    }
	        step[0]=xmax*0.1D;
	    }
        if(this.scaleFlag)step[2] = 0.1D*start[1];

	    // Nelder and Mead Simplex Regression
        GaussianFunction f = new GaussianFunction();
        this.addConstraint(1,-1,0.0D);
        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;
        Vector<Object> vec2 = new Vector<Object>();
        vec2.addElement(f);
        this.nelderMead(vec2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }

        if(yFlag){
            // restore data
            for(int i=0; i<this.nData-1; i++){
                this.yData[i]=-this.yData[i];
            }
        }

	}

    // Fit data to a lorentzian
	public void lorentzian(){
	    this.fitLorentzian(0);
	}

	public void lorentzianPlot(){
	    this.fitLorentzian(1);
	}

	private void fitLorentzian(int allTest){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=5;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nTerms=3;
	    if(!this.scaleFlag)this.nTerms=2;
	    this.degreesOfFreedom=this.nData - this.nTerms;
	    if(this.degreesOfFreedom<1)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

        // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

        // check sign of y data
	    Double tempd=null;
	    Vector<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.elementAt(4);
	    double yPeak = tempd.doubleValue();
	    boolean yFlag = false;
	    if(yPeak<0.0D){
	        System.out.println("Regression.fitLorentzian(): This implementation of the Lorentzian distribution takes only positive y values\n(noise taking low values below zero are allowed)");
	        System.out.println("All y values have been multiplied by -1 before fitting");
	        for(int i =0; i<this.nData; i++){
	                yData[i] = -yData[i];
	        }
	        retY = Regression.dataSign(yData);
	        yFlag=true;
	    }

	    // Calculate  x value at peak y (estimate of the distribution mean)
	    Vector ret1 = Regression.dataSign(yData);
	 	Integer tempi = null;
	    tempi = (Integer)ret1.elementAt(5);
	 	int peaki = tempi.intValue();
	    double mean = xData[0][peaki];

	    // Calculate an estimate of the half-height width
	    double sd = halfWidth(xData[0], yData);

	    // Calculate estimate of y scale
	    tempd = (Double)ret1.elementAt(4);
	    double ym = tempd.doubleValue();
	    ym=ym*sd*Math.PI/2.0D;

        // Fill arrays needed by the Simplex
        double[] start = new double[this.nTerms];
        double[] step = new double[this.nTerms];
        start[0] = mean;
        start[1] = sd*0.9D;
        if(this.scaleFlag){
            start[2] = ym;
         }
        step[0] = 0.2D*sd;
        if(step[0]==0.0D){
            Vector<Object> ret0 = Regression.dataSign(xData[0]);
	 	    Double tempdd = null;
	        tempdd = (Double)ret0.elementAt(2);
	 	    double xmax = tempdd.doubleValue();
	 	    if(xmax==0.0D){
	 	        tempdd = (Double)ret0.elementAt(0);
	 	        xmax = tempdd.doubleValue();
	 	    }
	        step[0]=xmax*0.1D;
	    }
        step[1] = 0.2D*start[1];
        if(this.scaleFlag)step[2] = 0.2D*start[2];

	    // Nelder and Mead Simplex Regression
        LorentzianFunction f = new LorentzianFunction();
        this.addConstraint(1,-1,0.0D);
        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;
        Vector<Object> vec2 = new Vector<Object>();
        vec2.addElement(f);
        this.nelderMead(vec2, start, step, this.fTol, this.nMax);

        if(allTest==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }

        if(yFlag){
            // restore data
            for(int i=0; i<this.nData-1; i++){
                this.yData[i]=-this.yData[i];
            }
        }

	}

    // Calculate the multiple correlation coefficient
    private void multCorrelCoeff(double[] yy, double[] yyCalc, double[] ww){

        // sum of reciprocal weights squared
        double sumRecipW = 0.0D;
        for(int i=0; i<this.nData; i++){
            sumRecipW += 1.0D/Fmath.square(ww[i]);
        }

        // weighted mean of yy
        double my = 0.0D;
        for(int j=0; j<this.nData; j++){
            my += yy[j]/Fmath.square(ww[j]);
        }
        my /= sumRecipW;


        // weighted mean of residuals
        double mr = 0.0D;
        double[] residuals = new double[this.nData];
        for(int j=0; j<this.nData; j++){
            residuals[j] = yy[j] - yyCalc[j];
            mr += residuals[j]/Fmath.square(ww[j]);
        }
        mr /= sumRecipW;

        // calculate yy weighted sum of squares
        double s2yy = 0.0D;
        for(int k=0; k<this.nData; k++){
            s2yy += Fmath.square((yy[k]-my)/ww[k]);
        }

        // calculate residual weighted sum of squares
        double s2r = 0.0D;
        for(int k=0; k<this.nData; k++){
            s2r += Fmath.square((residuals[k]-mr)/ww[k]);
        }

        // calculate multiple coefficient of determination
        this.sampleR2 = 1.0D - s2r/s2yy;
        // calculate multiple correlation coefficient
        this.sampleR = Math.sqrt(this.sampleR2);

        if(this.nXarrays>1){
            this.multipleF = this.sampleR2*(this.nData-this.nXarrays)/((1.0D-this.sampleR2)*(this.nXarrays-1));
        }
    }

    // Get the Sample Correlation Coefficient
    public double getSampleR(){
        return this.sampleR;
    }

    // Get the Coefficient of Determination
    public double getSampleR2(){
        return this.sampleR2;
    }

    // Get the Multiple Correlation Coefficient F ratio
    public double getMultipleF(){
        if(this.nXarrays==1){
            System.out.println("Regression.getMultipleF - The rgression is not amultple regession: -10 returnrd");
        }
        return this.multipleF;
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

    public void frechet(){
	    this.fitFrechet(0, 0);
	}

	public void frechetPlot(){
	    this.fitFrechet(1, 0);
	}

	public void frechetTwoPar(){
	    this.fitFrechet(0, 1);
	}

	public void frechetTwoParPlot(){
	    this.fitFrechet(1, 1);
	}

	public void frechetStandard(){
	    this.fitFrechet(0, 2);
	}

	public void frechetStandardPlot(){
	    this.fitFrechet(1, 2);
	}

    private void fitFrechet(int allTest, int typeFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    switch(typeFlag){
    	    case 0: this.lastMethod=13;
	                this.nTerms=4;
	                break;
	        case 1: this.lastMethod=14;
	                this.nTerms=3;
	                break;
	        case 2: this.lastMethod=15;
	                this.nTerms=2;
	                break;
        }
	    if(!this.scaleFlag)this.nTerms=this.nTerms-1;
        this.frechetWeibull=true;
        this.fitFrechetWeibull(allTest, typeFlag);
    }

    // method for fitting data to either a Frechet or a Weibull distribution
    private void fitFrechetWeibull(int allTest, int typeFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.degreesOfFreedom=this.nData - this.nTerms;
	    if(this.degreesOfFreedom<1)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

        // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // check y data
	    Double tempd=null;
	    Vector<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.elementAt(4);
	    double yPeak = tempd.doubleValue();
	    Integer tempi = null;
	    tempi = (Integer)retY.elementAt(5);
	 	int peaki = tempi.intValue();

	 	// check for infinity
	 	if(this.infinityCheck(yPeak, peaki)){
 	        retY = Regression.dataSign(yData);
	        tempd = (Double)retY.elementAt(4);
	        yPeak = tempd.doubleValue();
	        tempi = null;
	        tempi = (Integer)retY.elementAt(5);
	 	    peaki = tempi.intValue();
	 	}

 	    // check sign of y data
 	    String ss = "Weibull";
	    if(this.frechetWeibull)ss = "Frechet";
 	    boolean ySignFlag = false;
	    if(yPeak<0.0D){
	        this.reverseYsign(ss);
	        retY = Regression.dataSign(this.yData);
	        yPeak = -yPeak;
	        ySignFlag = true;
	    }

        // check y values for all very small values
        boolean magCheck=false;
        double magScale = this.checkYallSmall(yPeak, ss);
        if(magScale!=1.0D){
            magCheck=true;
            yPeak=1.0D;
        }

	    // minimum value of x
	    Vector<Object> retX = Regression.dataSign(this.xData[0]);
        tempd = (Double)retX.elementAt(0);
	    double xMin = tempd.doubleValue();

	    // maximum value of x
        tempd = (Double)retX.elementAt(2);
	    double xMax = tempd.doubleValue();

        // Calculate  x value at peak y (estimate of the 'distribution mean')
		double distribMean = xData[0][peaki];

	    // Calculate an estimate of the half-height width
	    double sd = Math.log(2.0D)*halfWidth(xData[0], yData);

	    // Save x-y-w data
	    double[] xx = new double[this.nData];
	    double[] yy = new double[this.nData];
	    double[] ww = new double[this.nData];

	    for(int i=0; i<this.nData; i++){
	        xx[i]=this.xData[0][i];
	        yy[i]=this.yData[i];
	        ww[i]=this.weight[i];
	    }

	    // Calculate the cumulative probability and return ordinate scaling factor estimate
	    double[] cumX = new double[this.nData];
	    double[] cumY = new double[this.nData];
	    double[] cumW = new double[this.nData];
	    ErrorProp[] cumYe = ErrorProp.oneDarray(this.nData);
        double yScale = this.calculateCumulativeValues(cumX, cumY, cumW, cumYe, peaki, yPeak, distribMean, ss);

	    //Calculate loglog v log transforms
	    if(this.frechetWeibull){
	        for(int i=0; i<this.nData; i++){
	            cumYe[i] = ErrorProp.over(1.0D, cumYe[i]);
	            cumYe[i] = ErrorProp.log(cumYe[i]);
	            cumYe[i] = ErrorProp.log(cumYe[i]);
	            cumY[i] = cumYe[i].getValue();
	            cumW[i] = cumYe[i].getError();
	        }
	    }
	    else{
	        for(int i=0; i<this.nData; i++){
	            cumYe[i] = ErrorProp.minus(1.0D,cumYe[i]);
	            cumYe[i] = ErrorProp.over(1.0D, cumYe[i]);
	            cumYe[i] = ErrorProp.log(cumYe[i]);
	            cumYe[i] = ErrorProp.log(cumYe[i]);
	            cumY[i] = cumYe[i].getValue();
	            cumW[i] = cumYe[i].getError();
	        }
        }

        // Fill data arrays with transformed data
        for(int i =0; i<this.nData; i++){
	                xData[0][i] = cumX[i];
	                yData[i] = cumY[i];
	                weight[i] = cumW[i];
	    }
	    boolean weightOptHold = this.weightOpt;
	    this.weightOpt=true;

		// Nelder and Mead Simplex Regression for semi-linearised Frechet or Weibull
		// disable statistical analysis
		this.statFlag=false;

        // Fill arrays needed by the Simplex
        double[] start = new double[this.nTerms];
        double[] step = new double[this.nTerms];
        for(int i=0; i<this.nTerms; i++){
            start[i]=1.0D;
            step[i]=0.2D;
        }
        switch(typeFlag){
    	    case 0:
                    start[0] = xMin*0.9D;         //mu
                    start[1] = sd;                //sigma
                    start[2] = 4.0D;              //gamma
                    step[0] = 0.2D*start[0];
                    if(step[0]==0.0D){
                        Vector<Object> ret0 = Regression.dataSign(xData[0]);
	 	                Double tempdd = null;
	                    tempdd = (Double)ret0.elementAt(2);
	 	                double xmax = tempdd.doubleValue();
	 	                if(xmax==0.0D){
	 	                    tempdd = (Double)ret0.elementAt(0);
	 	                    xmax = tempdd.doubleValue();
	 	                }
	                    step[0]=xmax*0.1D;
	                }
                    step[1] = 0.2D*start[1];
                    step[2] = 0.5D*start[2];
                    this.addConstraint(0,+1,xMin);
                    this.addConstraint(1,-1,0.0D);
                    this.addConstraint(2,-1,0.0D);
                    break;
    	    case 1: start[0] = sd;                //sigma
                    start[1] = 4.0D;              //gamma
                    step[0] = 0.2D*start[0];
                    step[1] = 0.5D*start[1];
                    this.addConstraint(0,-1,0.0D);
                    this.addConstraint(1,-1,0.0D);
                    break;
    	    case 2: start[0] = 4.0D;              //gamma
                    step[0] = 0.5D*start[0];
                    this.addConstraint(0,-1,0.0D);
                    break;
        }

        // Create instance of loglog function and perform regression
        if(this.frechetWeibull){
            FrechetFunctionTwo f = new FrechetFunctionTwo();
            f.typeFlag = typeFlag;
            Vector<Object> vec2 = new Vector<Object>();
            vec2.addElement(f);
            this.nelderMead(vec2, start, step, this.fTol, this.nMax);
        }
        else{
            WeibullFunctionTwo f = new WeibullFunctionTwo();
            f.typeFlag = typeFlag;
            Vector<Object> vec2 = new Vector<Object>();
            vec2.addElement(f);
            this.nelderMead(vec2, start, step, this.fTol, this.nMax);
        }

	    // Get best estimates of loglog regression
	    double[] ests = new double[this.nTerms];
	    for (int i=0;i<this.nTerms; ++i){
	        tempd = (Double)this.best.elementAt(i);
	        ests[i]=tempd.doubleValue();
	    }

	    // Nelder and Mead Simplex Regression for Frechet or Weibull
	    // using best estimates from loglog regression as initial estimates

		// enable statistical analysis
		this.statFlag=true;

	    // restore data reversing the loglog transform but maintaining any sign reversals
	    this.weightOpt=weightOptHold;
	    for(int i =0; i<this.nData; i++){
	        xData[0][i] = xx[i];
	        yData[i] = yy[i];
	        weight[i] = ww[i];
	    }

        // Fill arrays needed by the Simplex
        switch(typeFlag){
            case 0: start[0] = ests[0];         //mu
                    start[1] = ests[1];         //sigma
                    start[2] = ests[2];         //gamma
                    if(this.scaleFlag){
                        start[3] = 1.0/yScale;      //y axis scaling factor
                     }
                    step[0] = 0.1D*start[0];
                    if(step[0]==0.0D){
                        Vector<Object> ret0 = Regression.dataSign(xData[0]);
	 	                Double tempdd = null;
	                    tempdd = (Double)ret0.elementAt(2);
	 	                double xmax = tempdd.doubleValue();
	 	                if(xmax==0.0D){
	 	                    tempdd = (Double)ret0.elementAt(0);
	 	                    xmax = tempdd.doubleValue();
	 	                }
	                    step[0]=xmax*0.1D;
	                }
                    step[1] = 0.1D*start[1];
                    step[2] = 0.1D*start[2];
                    if(this.scaleFlag){
                        step[3] = 0.1D*start[3];
                    }
                   break;
            case 1: start[0] = ests[0];         //sigma
                    start[1] = ests[1];         //gamma
                    if(this.scaleFlag){
                        start[2] = 1.0/yScale;      //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    step[1] = 0.1D*start[1];
                    if(this.scaleFlag)step[2] = 0.1D*start[2];
                    break;
            case 2: start[0] = ests[0];         //gamma
                    if(this.scaleFlag){
                        start[1] = 1.0/yScale;      //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    if(this.scaleFlag)step[1] = 0.1D*start[1];
                    break;
        }

        // Create instance of Frechet function and perform regression
        if(this.frechetWeibull){
            FrechetFunctionOne ff = new FrechetFunctionOne();
            ff.typeFlag = typeFlag;
            ff.scaleOption = this.scaleFlag;
            ff.scaleFactor = this.yScaleFactor;
            Vector<Object> vec2 = new Vector<Object>();
            vec2.addElement(ff);
            this.nelderMead(vec2, start, step, this.fTol, this.nMax);
            if(allTest==1){
                // Print results
                if(!this.supressPrint)this.print();
                // Plot results
                int flag = this.plotXY(ff);
                if(flag!=-2 && !this.supressYYplot)this.plotYY();
            }
        }
        else{
            WeibullFunctionOne ff = new WeibullFunctionOne();
            ff.typeFlag = typeFlag;
            ff.scaleOption = this.scaleFlag;
            ff.scaleFactor = this.yScaleFactor;
            Vector<Object> vec2 = new Vector<Object>();
            vec2.addElement(ff);
            this.nelderMead(vec2, start, step, this.fTol, this.nMax);
            if(allTest==1){
                // Print results
                if(!this.supressPrint)this.print();
                // Plot results
                int flag = this.plotXY(ff);
                if(flag!=-2 && !this.supressYYplot)this.plotYY();
            }
        }

        // restore data
        this.weightOpt = weightOptHold;
	    if(magCheck){
	        for(int i =0; i<this.nData; i++){
	            this.yData[i] = yy[i]/magScale;
	            if(this.weightOpt)this.weight[i] = ww[i]/magScale;
	        }
	    }
	    if(ySignFlag){
	        for(int i =0; i<this.nData; i++){
	            this.yData[i]=-this.yData[i];
	        }
	    }
	}

	// Check for y value = infinity
	public boolean infinityCheck(double yPeak, int peaki){
	    boolean flag=false;
	 	if(yPeak == 1.0D/0.0D || yPeak == -1.0D/0.0D){
	 	    int ii = peaki+1;
	 	    if(peaki==this.nData-1)ii = peaki-1;
	 	    this.xData[0][peaki]=this.xData[0][ii];
	 	    this.yData[peaki]=this.yData[ii];
 	        this.weight[peaki]=this.weight[ii];
 	        System.out.println("An infinty has been removed at point "+peaki);
	 	    flag = true;
 	    }
 	    return flag;
    }

    // reverse sign of y values if negative
    public void reverseYsign(String ss){
	        System.out.println("This implementation of the " + ss + " distributions takes only positive y values\n(noise taking low values below zero are allowed)");
	        System.out.println("All y values have been multiplied by -1 before fitting");
	        for(int i =0; i<this.nData; i++){
	                this.yData[i] = -this.yData[i];
	        }
	}

    // check y values for all y are very small value
    public double checkYallSmall(double yPeak, String ss){
	    double magScale = 1.0D;
	    double recipYpeak = Fmath.truncate(1.0/yPeak, 4);
        if(yPeak<1e-4){
            System.out.println(ss + " fitting: The ordinate axis (y axis) has been rescaled by "+recipYpeak+" to reduce rounding errors");
            for(int i=0; i<this.nData; i++){
                this.yData[i]*=recipYpeak;
                if(this.weightOpt)this.weight[i]*=recipYpeak;
            }
            magScale=recipYpeak;
        }
        return magScale;
    }

    // Calculate cumulative values
    public double calculateCumulativeValues(double[] cumX, double[] cumY, double[] cumW, ErrorProp[] cumYe, int peaki, double yPeak, double distribMean, String ss){
        cumX[0]= this.xData[0][0];
	    for(int i=1; i<this.nData; i++){
            cumX[i] = this.xData[0][i];
	    }

	    ErrorProp[] yE = ErrorProp.oneDarray(this.nData);
	    for(int i=0; i<this.nData; i++){
            yE[i].reset(this.yData[i], this.weight[i]);
	    }

	    // check on shape of data for first step of cumulative calculation
	    if(peaki!=0){
	        if(peaki==this.nData-1){
	            System.out.println("The data does not cover a wide enough range of x values to fit to a " + ss + " distribution with any accuracy");
	            System.out.println("The regression will be attempted but you should treat any result with great caution");
	        }
	        if(this.yData[0]<this.yData[1]*0.5D && this.yData[0]>distribMean*0.02D){
	            ErrorProp x0 = new ErrorProp(0.0D, 0.0D);
	            x0 = yE[0].times(this.xData[0][1]-this.xData[0][0]);
	            x0 = x0.over(yE[1].minus(yE[0]));
	            x0 = ErrorProp.minus(this.xData[0][0],x0);
	            if(this.yData[0]>=0.9D*yPeak)x0=(x0.plus(this.xData[0][0])).over(2.0D);
		        if(x0.getValue()<0.0D)x0.reset(0.0D, 0.0D);
	            cumYe[0] = yE[0].over(2.0D);
	            cumYe[0] = cumYe[0].times(ErrorProp.minus(this.xData[0][0], x0));
	        }
	        else{
	            cumYe[0].reset(0.0D, this.weight[0]);
	        }
	    }
	    else{
	        cumYe[0].reset(0.0D, this.weight[0]);

	    }

	    // cumulative calculation for rest of the points (trapezium approximation)
	    for(int i=1; i<this.nData; i++){
	        cumYe[i] = yE[i].plus(yE[i-1]);
            cumYe[i] = cumYe[i].over(2.0D);
	        cumYe[i] = cumYe[i].times(this.xData[0][i]-this.xData[0][i-1]);
	        cumYe[i] = cumYe[i].plus(cumYe[i-1]);
		    }
	    // check on shape of data for final step of cumulative calculation
	    ErrorProp cumYtotal = cumYe[this.nData-1].copy();
	    if(peaki==this.nData-1){
	        cumYtotal = cumYtotal.times(2.0D);
	    }
	    else{
	        if(this.yData[this.nData-1]<yData[this.nData-2]*0.5D && yData[this.nData-1]>distribMean*0.02D){
	            ErrorProp xn = new ErrorProp();
	            xn = yE[this.nData-1].times(this.xData[0][this.nData-2]-this.xData[0][this.nData-1]);
	            xn = xn.over(yE[this.nData-2].minus(yE[this.nData-1]));
	            xn = ErrorProp.minus(this.xData[0][this.nData-1], xn);
	            if(this.yData[0]>=0.9D*yPeak)xn=(xn.plus(this.xData[0][this.nData-1])).over(2.0D);
	            cumYtotal =  cumYtotal.plus(ErrorProp.times(0.5D,(yE[this.nData-1].times(xn.minus(this.xData[0][this.nData-1])))));
	        }
	    }
	    // estimate y scaling factor
	    double yScale = 1.0D/cumYtotal.getValue();
	    for(int i=0; i<this.nData; i++){
	        cumYe[i]=cumYe[i].over(cumYtotal);
	    }

	    // check for zero and negative  values
	    int jj = 0;
	    boolean test = true;
	    for(int i=0; i<this.nData; i++){
	        if(cumYe[i].getValue()<=0.0D){
	            if(i<=jj){
	                test=true;
	                jj = i;
	                while(test){
	                    jj++;
	                    if(jj>=this.nData)throw new ArithmeticException("all zero cumulative data!!");
	                    if(cumYe[jj].getValue()>0.0D){
	                        cumYe[i]=cumYe[jj].copy();
	                        cumX[i]=cumX[jj];
	                        test=false;
	                    }
	                }
	            }
	            else{
	                if(i==this.nData-1){
	                    cumYe[i]=cumYe[i-1].copy();
	                    cumX[i]=cumX[i-1];
	                }
	                else{
	                    cumYe[i]=cumYe[i-1].plus(cumYe[i+1]);
	                    cumYe[i]=cumYe[i].over(2.0D);
	                    cumX[i]=(cumX[i-1]+cumX[i+1])/2.0D;
	                }
	            }
	        }
	    }

	    // check for unity value
		jj = this.nData-1;
	    for(int i=this.nData-1; i>=0; i--){
	        if(cumYe[i].getValue()>=1.0D){
	            if(i>=jj){
	                test=true;
	                jj = this.nData-1;
	                while(test){
	                    jj--;
	                    if(jj<0)throw new ArithmeticException("all unity cumulative data!!");
	                    if(cumYe[jj].getValue()<1.0D){
	                        cumYe[i]=cumYe[jj].copy();
	                        cumX[i]=cumX[jj];
	                        test=false;
	                    }
	                }
	            }
	            else{
	                if(i==0){
	                    cumYe[i]=cumYe[i+1].copy();
	                    cumX[i]=cumX[i+1];
	                }
	                else{
	                    cumYe[i]=cumYe[i-1].plus(cumYe[i+1]);
	                    cumYe[i]=cumYe[i].over(2.0D);
	                    cumX[i]=(cumX[i-1]+cumX[i+1])/2.0D;
	                }
	            }
	        }
	    }
	    return yScale;
	}

    public void weibull(){
	    this.fitWeibull(0, 0);
	}

	public void weibullPlot(){
	    this.fitWeibull(1, 0);
	}

	public void weibullTwoPar(){
	    this.fitWeibull(0, 1);
	}

	public void weibullTwoParPlot(){
	    this.fitWeibull(1, 1);
	}

	public void weibullStandard(){
	    this.fitWeibull(0, 2);
	}

	public void weibullStandardPlot(){
	    this.fitWeibull(1, 2);
	}

    private void fitWeibull(int allTest, int typeFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    switch(typeFlag){
    	    case 0: this.lastMethod=16;
	                this.nTerms=4;
	                break;
	        case 1: this.lastMethod=17;
	                this.nTerms=3;
	                break;
	        case 2: this.lastMethod=18;
	                this.nTerms=2;
	                break;
        }
	    if(!this.scaleFlag)this.nTerms=this.nTerms-1;
        this.frechetWeibull=false;
        this.fitFrechetWeibull(allTest, typeFlag);
    }

	public void gumbelMin(){
	    this.fitGumbel(0, 0);
	}

	public void gumbelMinPlot(){
	    this.fitGumbel(1, 0);
	}

	public void gumbelMax(){
	    this.fitGumbel(0, 1);
	}
	public void gumbelMaxPlot(){
	    this.fitGumbel(1, 1);
	}

	public void gumbelMinOnePar(){
	    this.fitGumbel(0, 2);
	}

	public void gumbelMinOneParPlot(){
	    this.fitGumbel(1, 2);
	}

    public void gumbelMaxOnePar(){
	    this.fitGumbel(0, 3);
	}

	public void gumbelMaxOneParPlot(){
	    this.fitGumbel(1, 3);
	}

	public void gumbelMinStandard(){
	    this.fitGumbel(0, 4);
	}

	public void gumbelMinStandardPlot(){
	    this.fitGumbel(1, 4);
	}

	public void gumbelMaxStandard(){
	    this.fitGumbel(0, 5);
	}

	public void gumbelMaxStandardPlot(){
	    this.fitGumbel(1, 5);
	}

    // No parameters set for estimation
    // Correlation coefficient and plot
    private void noParameters(String ss){
        System.out.println(ss+" Regression");
        System.out.println("No parameters set for estimation");
        System.out.println("Theoretical curve obtained");
	    String filename1="RegressOutput.txt";
	    String filename2="RegressOutputN.txt";
	    FileOutput fout = new FileOutput(filename1, 'n');
	    System.out.println("Results printed to the file "+filename2);
	    fout.dateAndTimeln(filename1);
        fout.println("No parameters set for estimation");
        switch(this.lastMethod){
            case 11:     fout.println("Minimal Standard Gumbel p(x) = exp(x)exp(-exp(x))");
                        for(int i=0; i<this.nData; i++)this.yCalc[i]=Math.exp(this.xData[0][i])*Math.exp(-Math.exp(this.xData[0][i]));
                        break;
            case 12:    fout.println("Maximal Standard Gumbel p(x) = exp(-x)exp(-exp(-x))");
                        for(int i=0; i<this.nData; i++)this.yCalc[i]=Math.exp(-this.xData[0][i])*Math.exp(-Math.exp(-this.xData[0][i]));
                        break;
            case 21:    fout.println("Standard Exponential p(x) = exp(-x)");
                        for(int i=0; i<this.nData; i++)this.yCalc[i]=Math.exp(-this.xData[0][i]);
                        break;
        }
        this.sumOfSquares = 0.0D;
        this.chiSquare = 0.0D;
        double temp = 0.0D;
         for(int i=0; i<this.nData; i++){
            temp = Fmath.square(this.yData[i]-this.yCalc[i]);
            this.sumOfSquares += temp;
            this.chiSquare += temp/Fmath.square(this.weight[i]);
        }
        double corrCoeff = Stat.corrCoeff(this.yData, this.yCalc);
        fout.printtab("Correlation Coefficient");
        fout.println(Fmath.truncate(corrCoeff, this.prec));
        fout.printtab("Correlation Coefficient Probability");
        fout.println(Fmath.truncate(1.0D-Stat.linearCorrCoeffProb(corrCoeff, this.degreesOfFreedom-1), this.prec));

        fout.printtab("Sum of Squares");
        fout.println(Fmath.truncate(this.sumOfSquares, this.prec));
        if(this.weightOpt || this.trueFreq){
            fout.printtab("Chi Square");
            fout.println(Fmath.truncate(this.chiSquare, this.prec));
            fout.printtab("chi square probability");
            fout.println(Fmath.truncate(Stat.chiSquareProb(this.chiSquare, this.degreesOfFreedom-1), this.prec));
        }
        fout.println(" ");

        fout.printtab("x", this.field);
        fout.printtab("p(x) [expl]", this.field);
        fout.printtab("p(x) [calc]", this.field);
        fout.println("residual");

        for(int i=0; i<this.nData; i++){
            fout.printtab(Fmath.truncate(this.xData[0][i], this.prec), this.field);
            fout.printtab(Fmath.truncate(this.yData[i], this.prec), this.field);
            fout.printtab(Fmath.truncate(this.yCalc[i], this.prec), this.field);
            fout.println(Fmath.truncate(this.yData[i]-this.yCalc[i], this.prec));
       }
       fout.close();
       this.plotXY();
       if(!this.supressYYplot)this.plotYY();
    }

	private void fitGumbel(int allTest, int typeFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    switch(typeFlag){
    	    case 0: this.lastMethod=7;
	                this.nTerms=3;
	                break;
	        case 1: this.lastMethod=8;
	                this.nTerms=3;
	                break;
	        case 2: this.lastMethod=9;
	                this.nTerms=2;
	                break;
	        case 3: this.lastMethod=10;
	                this.nTerms=2;
	                break;
	        case 4: this.lastMethod=11;
	                this.nTerms=1;
	                break;
            case 5: this.lastMethod=12;
	                this.nTerms=1;
	                break;
	    }
	    if(!this.scaleFlag)this.nTerms=this.nTerms-1;
	    this.zeroCheck = false;
		this.degreesOfFreedom=this.nData - this.nTerms;
	    if(this.degreesOfFreedom<1)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
	    if(this.nTerms==0){
	        this.noParameters("Gumbel");
	    }
	    else{


	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // check sign of y data
	    Double tempd=null;
	    Vector<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.elementAt(4);
	    double yPeak = tempd.doubleValue();
	    boolean yFlag = false;
	    if(yPeak<0.0D){
	        System.out.println("Regression.fitGumbel(): This implementation of the Gumbel distribution takes only positive y values\n(noise taking low values below zero are allowed)");
	        System.out.println("All y values have been multiplied by -1 before fitting");
	        for(int i =0; i<this.nData; i++){
	                yData[i] = -yData[i];
	        }
	        retY = Regression.dataSign(yData);
	        yFlag=true;
	    }

	    // check  x data
	    Vector<Object> retX = Regression.dataSign(xData[0]);
	 	Integer tempi = null;

        // Calculate  x value at peak y (estimate of the 'distribution mean')
	    tempi = (Integer)retY.elementAt(5);
	 	int peaki = tempi.intValue();
	    double distribMean = xData[0][peaki];

	    // Calculate an estimate of the half-height width
	    double sd = halfWidth(xData[0], yData);

	    // Nelder and Mead Simplex Regression for Gumbel
        // Fill arrays needed by the Simplex
        double[] start = new double[this.nTerms];
        double[] step = new double[this.nTerms];
        switch(typeFlag){
            case 0:
            case 1:
                    start[0] = distribMean;                     //mu
                    start[1] = sd*Math.sqrt(6.0D)/Math.PI;      //sigma
                    if(this.scaleFlag){
                        start[2] = yPeak*start[1]*Math.exp(1);      //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    if(step[0]==0.0D){
                        Vector<Object> ret0 = Regression.dataSign(xData[0]);
	 	                Double tempdd = null;
	                    tempdd = (Double)ret0.elementAt(2);
	 	                double xmax = tempdd.doubleValue();
	 	                if(xmax==0.0D){
	 	                    tempdd = (Double)ret0.elementAt(0);
	 	                    xmax = tempdd.doubleValue();
	 	                }
	                    step[0]=xmax*0.1D;
	                }
                    step[1] = 0.1D*start[1];
                    if(this.scaleFlag)step[2] = 0.1D*start[2];

	                // Add constraints
                    this.addConstraint(1,-1,0.0D);
                    break;
            case 2:
            case 3:
                    start[0] = sd*Math.sqrt(6.0D)/Math.PI;      //sigma
                    if(this.scaleFlag){
                        start[1] = yPeak*start[0]*Math.exp(1);      //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    if(this.scaleFlag)step[1] = 0.1D*start[1];
	                // Add constraints
                    this.addConstraint(0,-1,0.0D);
                    break;
            case 4:
            case 5:
                    if(this.scaleFlag){
                        start[0] = yPeak*Math.exp(1);               //y axis scaling factor
                        step[0] = 0.1D*start[0];
                    }
                    break;
        }

        // Create instance of Gumbel function
        GumbelFunction ff = new GumbelFunction();

        // Set minimum type / maximum type option
        ff.typeFlag = typeFlag;

        // Set ordinate scaling option
        ff.scaleOption = this.scaleFlag;
        ff.scaleFactor = this.yScaleFactor;

        if(typeFlag<4){

            // Perform simplex regression
            Vector<Object> vec2 = new Vector<Object>();
            vec2.addElement(ff);
            this.nelderMead(vec2, start, step, this.fTol, this.nMax);

            if(allTest==1){
                // Print results
                if(!this.supressPrint)this.print();

                // Plot results
                int flag = this.plotXY(ff);
                if(flag!=-2 && !this.supressYYplot)this.plotYY();
            }
        }
        else{
            // calculate exp exp term
            double[][] xxx = new double[1][this.nData];
            double aa=1.0D;
            if(typeFlag==5)aa=-1.0D;
            for(int i=0; i<this.nData; i++){
                xxx[0][i]=Math.exp(aa*this.xData[0][i])*Math.exp(-Math.exp(aa*this.xData[0][i]));
            }

            // perform linear regression
            this.linNonLin = true;
            this.generalLinear(xxx);

            if(!this.supressPrint)this.print();
            if(!this.supressYYplot)this.plotYY();
            this.plotXY();

            this.linNonLin = false;

        }

        if(yFlag){
            // restore data
            for(int i=0; i<this.nData-1; i++){
                this.yData[i]=-this.yData[i];
            }
        }
        }
	}

	// sort elements x, y and w arrays of doubles into ascending order of the x array
    // using selection sort method
    private static void sort(double[] x, double[] y, double[] w){
            int index = 0;
            int lastIndex = -1;
            int n = x.length;
            double holdx = 0.0D;
            double holdy = 0.0D;
            double holdw = 0.0D;

            while(lastIndex < n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(x[i]<x[index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdx=x[index];
                x[index]=x[lastIndex];
                x[lastIndex]=holdx;
                holdy=y[index];
                y[index]=y[lastIndex];
                y[lastIndex]=holdy;
                holdw=w[index];
                w[index]=w[lastIndex];
                w[lastIndex]=holdw;
            }
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

    public void exponential(){
	    this.fitExponential(0, 0);
	}

	public void exponentialPlot(){
	    this.fitExponential(1, 0);
	}

	public void exponentialOnePar(){
	    this.fitExponential(0, 1);
	}

	public void exponentialOneParPlot(){
	    this.fitExponential(1, 1);
	}

	public void exponentialStandard(){
	    this.fitExponential(0, 2);
	}

	public void exponentialStandardPlot(){
	    this.fitExponential(1, 2);
	}

    private void fitExponential(int allTest, int typeFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    switch(typeFlag){
    	    case 0: this.lastMethod=19;
	                this.nTerms=3;
	                break;
	        case 1: this.lastMethod=20;
	                this.nTerms=2;
	                break;
	        case 2: this.lastMethod=21;
	                this.nTerms=1;
	                break;
        }
	    if(!this.scaleFlag)this.nTerms=this.nTerms-1;
   	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.degreesOfFreedom=this.nData - this.nTerms;
	    if(this.degreesOfFreedom<1)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
	    if(this.nTerms==0){
	        this.noParameters("Exponential");
	    }
	    else{

	    // Save x-y-w data
	    double[] xx = new double[this.nData];
	    double[] yy = new double[this.nData];
	    double[] ww = new double[this.nData];

	    for(int i=0; i<this.nData; i++){
	        xx[i]=this.xData[0][i];
	        yy[i]=this.yData[i];
	        ww[i]=this.weight[i];
	    }

        // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // check y data
	    Double tempd=null;
	    Vector<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.elementAt(4);
	    double yPeak = tempd.doubleValue();
	    Integer tempi = null;
	    tempi = (Integer)retY.elementAt(5);
	 	int peaki = tempi.intValue();

 	    // check sign of y data
 	    String ss = "Exponential";
 	    boolean ySignFlag = false;
	    if(yPeak<0.0D){
	        this.reverseYsign(ss);
	        retY = Regression.dataSign(this.yData);
	        yPeak = -yPeak;
	        ySignFlag = true;
	    }

        // check y values for all very small values
        boolean magCheck=false;
        double magScale = this.checkYallSmall(yPeak, ss);
        if(magScale!=1.0D){
            magCheck=true;
            yPeak=1.0D;
        }

	    // minimum value of x
	    Vector<Object> retX = Regression.dataSign(this.xData[0]);
        tempd = (Double)retX.elementAt(0);
	    double xMin = tempd.doubleValue();

        // estimate of sigma
        double yE = yPeak/Math.exp(1.0D);
        if(this.yData[0]<yPeak)yE = (yPeak+yData[0])/(2.0D*Math.exp(1.0D));
        double yDiff = Math.abs(yData[0]-yE);
        double yTest = 0.0D;
        int iE = 0;
        for(int i=1; i<this.nData; i++){
            yTest=Math.abs(this.yData[i]-yE);
            if(yTest<yDiff){
                yDiff=yTest;
                iE=i;
            }
        }
        double sigma = this.xData[0][iE]-this.xData[0][0];

	    // Nelder and Mead Simplex Regression
	    double[] start = new double[this.nTerms];
	    double[] step = new double[this.nTerms];

        // Fill arrays needed by the Simplex
        switch(typeFlag){
            case 0: start[0] = xMin*0.9;    //mu
                    start[1] = sigma;       //sigma
                    if(this.scaleFlag){
                        start[2] = yPeak*sigma; //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    if(step[0]==0.0D){
                        Vector<Object> ret0 = Regression.dataSign(xData[0]);
	 	                Double tempdd = null;
	                    tempdd = (Double)ret0.elementAt(2);
	 	                double xmax = tempdd.doubleValue();
	 	                if(xmax==0.0D){
	 	                    tempdd = (Double)ret0.elementAt(0);
	 	                    xmax = tempdd.doubleValue();
	 	                }
	                    step[0]=xmax*0.1D;
	                }
                    step[1] = 0.1D*start[1];
                    if(this.scaleFlag)step[2] = 0.1D*start[2];
                    break;
            case 1: start[0] = sigma;       //sigma
                    if(this.scaleFlag){
                        start[1] = yPeak*sigma; //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    if(this.scaleFlag)step[1] = 0.1D*start[1];
                    break;
            case 2: if(this.scaleFlag){
                        start[0] = yPeak;       //y axis scaling factor
                        step[0] = 0.1D*start[0];
                    }
                    break;
        }

        // Create instance of Exponential function and perform regression
        ExponentialFunction ff = new ExponentialFunction();
        ff.typeFlag = typeFlag;
        ff.scaleOption = this.scaleFlag;
        ff.scaleFactor = this.yScaleFactor;
        Vector<Object> vec2 = new Vector<Object>();
        vec2.addElement(ff);
        this.nelderMead(vec2, start, step, this.fTol, this.nMax);

        if(allTest==1){
            // Print results
            if(!this.supressPrint)this.print();
            // Plot results
            int flag = this.plotXY(ff);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }

        // restore data
	    if(magCheck){
	        for(int i =0; i<this.nData; i++){
	            this.yData[i] = yy[i]/magScale;
	            if(this.weightOpt)this.weight[i] = ww[i]/magScale;
	        }
	    }
	    if(ySignFlag){
	        for(int i =0; i<this.nData; i++){
	            this.yData[i]=-this.yData[i];
	        }
	    }
	    }
	}

    // check for zero and negative  values
    public void checkZeroNeg(double [] xx, double[] yy, double[] ww){
	    int jj = 0;
	    boolean test = true;
	    for(int i=0; i<this.nData; i++){
	        if(yy[i]<=0.0D){
	            if(i<=jj){
	                test=true;
	                jj = i;
	                while(test){
	                    jj++;
	                    if(jj>=this.nData)throw new ArithmeticException("all zero cumulative data!!");
	                    if(yy[jj]>0.0D){
	                        yy[i]=yy[jj];
	                        xx[i]=xx[jj];
	                        ww[i]=ww[jj];
	                        test=false;
	                    }
	                }
	            }
	            else{
	                if(i==this.nData-1){
	                    yy[i]=yy[i-1];
	                    xx[i]=xx[i-1];
	                    ww[i]=ww[i-1];
	                }
	                else{
	                    yy[i]=(yy[i-1] + yy[i+1])/2.0D;
	                    xx[i]=(xx[i-1] + xx[i+1])/2.0D;
	                    ww[i]=(ww[i-1] + ww[i+1])/2.0D;
	                }
	            }
	        }
	    }
	}

	public void rayleigh(){
	    this.fitRayleigh(0, 0);
	}

	public void rayleighPlot(){
	    this.fitRayleigh(1, 0);
	}

    private void fitRayleigh(int allTest, int typeFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
    	this.lastMethod=22;
	    this.nTerms=2;
	    if(!this.scaleFlag)this.nTerms=this.nTerms-1;
   	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.degreesOfFreedom=this.nData - this.nTerms;
	    if(this.degreesOfFreedom<1)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");


        // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // check y data
	    Double tempd=null;
	    Vector<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.elementAt(4);
	    double yPeak = tempd.doubleValue();
	    Integer tempi = null;
	    tempi = (Integer)retY.elementAt(5);
	 	int peaki = tempi.intValue();

 	    // check sign of y data
 	    String ss = "Rayleigh";
 	    boolean ySignFlag = false;
	    if(yPeak<0.0D){
	        this.reverseYsign(ss);
	        retY = Regression.dataSign(this.yData);
	        yPeak = -yPeak;
	        ySignFlag = true;
	    }

        // check y values for all very small values
        boolean magCheck=false;
        double magScale = this.checkYallSmall(yPeak, ss);
        if(magScale!=1.0D){
            magCheck=true;
            yPeak=1.0D;
        }

	    // Save x-y-w data
	    double[] xx = new double[this.nData];
	    double[] yy = new double[this.nData];
	    double[] ww = new double[this.nData];

	    for(int i=0; i<this.nData; i++){
	        xx[i]=this.xData[0][i];
	        yy[i]=this.yData[i];
	        ww[i]=this.weight[i];
	    }

	    // minimum value of x
	    Vector<Object> retX = Regression.dataSign(this.xData[0]);
        tempd = (Double)retX.elementAt(0);
	    double xMin = tempd.doubleValue();

	    // maximum value of x
        tempd = (Double)retX.elementAt(2);
	    double xMax = tempd.doubleValue();

        // Calculate  x value at peak y (estimate of the 'distribution mean')
		double distribMean = xData[0][peaki];

	    // Calculate an estimate of the half-height width
	    double sd = Math.log(2.0D)*halfWidth(xData[0], yData);

	    // Calculate the cumulative probability and return ordinate scaling factor estimate
	    double[] cumX = new double[this.nData];
	    double[] cumY = new double[this.nData];
	    double[] cumW = new double[this.nData];
	    ErrorProp[] cumYe = ErrorProp.oneDarray(this.nData);
        double yScale = this.calculateCumulativeValues(cumX, cumY, cumW, cumYe, peaki, yPeak, distribMean, ss);

	    //Calculate log  transform
	    for(int i=0; i<this.nData; i++){
	        cumYe[i] = ErrorProp.minus(1.0D,cumYe[i]);
	        cumYe[i] = ErrorProp.over(1.0D, cumYe[i]);
	        cumYe[i] = ErrorProp.log(cumYe[i]);
	        cumY[i] = cumYe[i].getValue();
	        cumW[i] = cumYe[i].getError();
        }

        // Fill data arrays with transformed data
        for(int i =0; i<this.nData; i++){
	        xData[0][i] = cumX[i];
	        yData[i] = cumY[i];
	        weight[i] = cumW[i];
	    }
	    boolean weightOptHold = this.weightOpt;
	    this.weightOpt=true;

		// Nelder and Mead Simplex Regression for semi-linearised Rayleigh
		// disable statistical analysis
		this.statFlag=false;

        // Fill arrays needed by the Simplex
        double[] start = new double[this.nTerms];
        double[] step = new double[this.nTerms];
        for(int i=0; i<this.nTerms; i++){
            start[i]=1.0D;
            step[i]=0.2D;
        }
        start[0] = sd;                //sigma
        step[0] = 0.2D;
        this.addConstraint(0,-1,0.0D);

        // Create instance of log function and perform regression
        RayleighFunctionTwo f = new RayleighFunctionTwo();
        Vector<Object> vec2 = new Vector<Object>();
        vec2.addElement(f);
        this.nelderMead(vec2, start, step, this.fTol, this.nMax);

	    // Get best estimates of log regression
	    double[] ests = new double[this.nTerms];
	    for (int i=0;i<this.nTerms; ++i){
	        tempd = (Double)this.best.elementAt(i);
	        ests[i]=tempd.doubleValue();
	    }

		// enable statistical analysis
		this.statFlag=true;

	    // restore data reversing the loglog transform but maintaining any sign reversals
	    this.weightOpt=weightOptHold;
	    for(int i =0; i<this.nData; i++){
	        xData[0][i] = xx[i];
	        yData[i] = yy[i];
	        weight[i] = ww[i];
	    }

        // Fill arrays needed by the Simplex
        start[0] = ests[0];         //sigma
        if(this.scaleFlag){
            start[1] = 1.0/yScale;      //y axis scaling factor
        }
        step[0] = 0.1D*start[0];
        if(this.scaleFlag)step[1] = 0.1D*start[1];


        // Create instance of Rayleigh function and perform regression
        RayleighFunctionOne ff = new RayleighFunctionOne();
        ff.scaleOption = this.scaleFlag;
        ff.scaleFactor = this.yScaleFactor;
        Vector<Object> vec3 = new Vector<Object>();
        vec3.addElement(ff);
        this.nelderMead(vec2, start, step, this.fTol, this.nMax);

        if(allTest==1){
            // Print results
            if(!this.supressPrint)this.print();
            // Plot results
            int flag = this.plotXY(ff);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }

        // restore data
	    if(magCheck){
	        for(int i =0; i<this.nData; i++){
	            this.yData[i] = yy[i]/magScale;
	            if(this.weightOpt)this.weight[i] = ww[i]/magScale;
	        }
	    }
	    if(ySignFlag){
	        for(int i =0; i<this.nData; i++){
	            this.yData[i]=-this.yData[i];
	        }
	    }
	}

	// Two Parameter Pareto
    public void pareto(){
	    this.fitPareto(0, 0);
	}

	public void paretoPlot(){
	    this.fitPareto(1, 0);
	}

    // One Parameter Pareto
	public void paretoOnePar(){
	    this.fitPareto(0, 1);
	}

	public void paretoOneParPlot(){
	    this.fitPareto(1, 1);
	}

    // method for fitting data to a Pareto distribution
    private void fitPareto(int allTest, int typeFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    switch(typeFlag){
    	    case 0: this.lastMethod=23;
	                this.nTerms=3;
	                break;
	        case 1: this.lastMethod=24;
	                this.nTerms=2;
	                break;
	    }

	    if(!this.scaleFlag)this.nTerms=this.nTerms-1;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.degreesOfFreedom=this.nData - this.nTerms;
	    if(this.degreesOfFreedom<1)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
 	    String ss = "Pareto";

        // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // check y data
	    Double tempd=null;
	    Vector<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.elementAt(4);
	    double yPeak = tempd.doubleValue();
	    Integer tempi = null;
	    tempi = (Integer)retY.elementAt(5);
	 	int peaki = tempi.intValue();

	 	// check for infinity
	 	if(this.infinityCheck(yPeak, peaki)){
 	        retY = Regression.dataSign(yData);
	        tempd = (Double)retY.elementAt(4);
	        yPeak = tempd.doubleValue();
	        tempi = null;
	        tempi = (Integer)retY.elementAt(5);
	 	    peaki = tempi.intValue();
	 	}

 	    // check sign of y data
 	    boolean ySignFlag = false;
	    if(yPeak<0.0D){
	        this.reverseYsign(ss);
	        retY = Regression.dataSign(this.yData);
	        yPeak = -yPeak;
	        ySignFlag = true;
	    }

        // check y values for all very small values
        boolean magCheck=false;
        double magScale = this.checkYallSmall(yPeak, ss);
        if(magScale!=1.0D){
            magCheck=true;
            yPeak=1.0D;
        }

	    // minimum value of x
	    Vector<Object> retX = Regression.dataSign(this.xData[0]);
        tempd = (Double)retX.elementAt(0);
	    double xMin = tempd.doubleValue();

	    // maximum value of x
        tempd = (Double)retX.elementAt(2);
	    double xMax = tempd.doubleValue();

        // Calculate  x value at peak y (estimate of the 'distribution mean')
		double distribMean = xData[0][peaki];

	    // Calculate an estimate of the half-height width
	    double sd = Math.log(2.0D)*halfWidth(xData[0], yData);

	    // Save x-y-w data
	    double[] xx = new double[this.nData];
	    double[] yy = new double[this.nData];
	    double[] ww = new double[this.nData];

	    for(int i=0; i<this.nData; i++){
	        xx[i]=this.xData[0][i];
	        yy[i]=this.yData[i];
	        ww[i]=this.weight[i];
	    }

	    // Calculate the cumulative probability and return ordinate scaling factor estimate
	    double[] cumX = new double[this.nData];
	    double[] cumY = new double[this.nData];
	    double[] cumW = new double[this.nData];
	    ErrorProp[] cumYe = ErrorProp.oneDarray(this.nData);
        double yScale = this.calculateCumulativeValues(cumX, cumY, cumW, cumYe, peaki, yPeak, distribMean, ss);

	    //Calculate l - cumlative probability
	    for(int i=0; i<this.nData; i++){
	        cumYe[i] = ErrorProp.minus(1.0D,cumYe[i]);
	        cumY[i] = cumYe[i].getValue();
	        cumW[i] = cumYe[i].getError();
        }

        // Fill data arrays with transformed data
        for(int i =0; i<this.nData; i++){
	                xData[0][i] = cumX[i];
	                yData[i] = cumY[i];
	                weight[i] = cumW[i];
	    }
	    boolean weightOptHold = this.weightOpt;
	    this.weightOpt=true;

		// Nelder and Mead Simplex Regression for Pareto estimated cdf
		// disable statistical analysis
		this.statFlag=false;

        // Fill arrays needed by the Simplex
        double[] start = new double[this.nTerms];
        double[] step = new double[this.nTerms];
        for(int i=0; i<this.nTerms; i++){
            start[i]=1.0D;
            step[i]=0.2D;
        }
        switch(typeFlag){
    	    case 0:
                    start[0] = 2;           //alpha
                    start[1] = xMin*0.9D;   //beta
                    step[0] = 0.2D*start[0];
                    step[1] = 0.2D*start[1];
                    if(step[1]==0.0D){
                        double xmax = xMax;
	 	                if(xmax==0.0D){
	 	                    xmax = xMin;
	 	                }
	                    step[1]=xmax*0.1D;
	                }
	                this.addConstraint(0,-1,0.0D);
                    this.addConstraint(1,+1,xMin);
                    break;
    	    case 1: start[0] = 2;                //alpha
                    step[0] = 0.2D*start[0];
                    this.addConstraint(0,-1,0.0D);
                    break;
        }

        // Create instance of cdf function and perform regression
        ParetoFunctionTwo f = new ParetoFunctionTwo();
        f.typeFlag = typeFlag;
        Vector<Object> vec2 = new Vector<Object>();
        vec2.addElement(f);
        this.nelderMead(vec2, start, step, this.fTol, this.nMax);

	    // Get best estimates of cdf regression
	    double[] ests = new double[this.nTerms];
	    for (int i=0;i<this.nTerms; ++i){
	        tempd = (Double)this.best.elementAt(i);
	        ests[i]=tempd.doubleValue();
	    }

	    // Nelder and Mead Simplex Regression for Pareto
	    // using best estimates from cdf regression as initial estimates

		// enable statistical analysis
		this.statFlag=true;

	    // restore data reversing the cdf transform but maintaining any sign reversals
	    this.weightOpt=weightOptHold;
	    for(int i =0; i<this.nData; i++){
	        xData[0][i] = xx[i];
	        yData[i] = yy[i];
	        weight[i] = ww[i];
	    }

        // Fill arrays needed by the Simplex
        switch(typeFlag){
            case 0: start[0] = ests[0];                         //alpha
                    start[1] = ests[1];                         //beta
                    if(this.scaleFlag){
                        start[2] = 1.0/yScale;    //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    step[1] = 0.1D*start[1];
                    if(step[1]==0.0D){
                        double xmax = xMax;
	 	                if(xmax==0.0D){
	 	                    xmax = xMin;
	 	                }
	                    step[1]=xmax*0.1D;
	                }
                    if(this.scaleFlag)step[2] = 0.1D*start[2];
                    break;
            case 1: start[0] = ests[0];                         //alpha
                    if(this.scaleFlag){
                        start[1] = 1.0/yScale;    //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    if(this.scaleFlag)step[1] = 0.1D*start[1];
                    break;
         }

        // Create instance of Pareto function and perform regression
        ParetoFunctionOne ff = new ParetoFunctionOne();
        ff.typeFlag = typeFlag;
        ff.scaleOption = this.scaleFlag;
        ff.scaleFactor = this.yScaleFactor;
        Vector<Object> vec3 = new Vector<Object>();
        vec3.addElement(ff);
        this.nelderMead(vec3, start, step, this.fTol, this.nMax);

        if(allTest==1){
            // Print results
            if(!this.supressPrint)this.print();
            // Plot results
            int flag = this.plotXY(ff);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }

        // restore data
        this.weightOpt = weightOptHold;
	    if(magCheck){
	        for(int i =0; i<this.nData; i++){
	            this.yData[i] = yy[i]/magScale;
	            if(this.weightOpt)this.weight[i] = ww[i]/magScale;
	        }
	    }
	    if(ySignFlag){
	        for(int i =0; i<this.nData; i++){
	            this.yData[i]=-this.yData[i];
	        }
	    }
	}


	// method for fitting data to a sigmoid threshold function
    public void sigmoidThreshold(){
        fitSigmoidThreshold(0);
    }

    // method for fitting data to a sigmoid threshold function with plot and print out
    public void sigmoidThresholdPlot(){
        fitSigmoidThreshold(1);
    }


    // method for fitting data to a sigmoid threshold function
    private void fitSigmoidThreshold(int plotFlag){

        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=25;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nTerms=3;
	    if(!this.scaleFlag)this.nTerms=2;
	    this.degreesOfFreedom=this.nData - this.nTerms;
	    if(this.degreesOfFreedom<1)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // Estimate  of theta
	    double yymin = Fmath.minimum(this.yData);
	    double yymax = Fmath.maximum(this.yData);
	    int dirFlag = 1;
	    if(yymin<0)dirFlag=-1;
	    double yyymid = (yymax - yymin)/2.0D;
	    double yyxmidl = xData[0][0];
	    int ii = 1;
	    int nLen = this.yData.length;
	    boolean test = true;
	    while(test){
	        if(this.yData[ii]>=dirFlag*yyymid){
	            yyxmidl = xData[0][ii];
	            test = false;
	        }
	        else{
	            ii++;
	            if(ii>=nLen){
	                yyxmidl = Stat.mean(this.xData[0]);
	                ii=nLen-1;
                    test = false;
                }
	        }
	    }
	    double yyxmidh = xData[0][nLen-1];
	    int jj = nLen-1;
	    test = true;
	    while(test){
	        if(this.yData[jj]<=dirFlag*yyymid){
	            yyxmidh = xData[0][jj];
	            test = false;
	        }
	        else{
	            jj--;
	            if(jj<0){
	                yyxmidh = Stat.mean(this.xData[0]);
	                jj=1;
                    test = false;
                }
	        }
	    }
	    int thetaPos = (ii+jj)/2;
        double theta0 = xData[0][thetaPos];

	    // estimate of slope
	    double thetaSlope1 = 2.0D*(yData[nLen-1] - theta0)/(xData[0][nLen-1] - xData[0][thetaPos]);
	    double thetaSlope2 = 2.0D*theta0/(xData[0][thetaPos] - xData[0][nLen-1]);
	    double thetaSlope = Math.max(thetaSlope1, thetaSlope2);

        // initial estimates
        double[] start = new double[nTerms];
        start[0] = 4.0D*thetaSlope;
        if(dirFlag==1){
            start[0] /= yymax;
        }
        else{
            start[0] /= yymin;
        }
        start[1] = theta0;
        if(this.scaleFlag){
            if(dirFlag==1){
                start[2] = yymax;
            }
            else{
                start[2] = yymin;
            }
        }

        // initial step sizes
        double[] step = new double[nTerms];
        for(int i=0; i<nTerms; i++)step[i] = 0.1*start[i];
        if(step[0]==0.0D)step[1] = 0.1*(xData[0][nLen-1] - xData[0][0])/(yData[nLen-1] - yData[0]);
        if(step[1]==0.0D)step[1] = (xData[0][nLen-1] - xData[0][0])/20.0D;
        if(this.scaleFlag)if(step[2]==0.0D)step[2] = 0.1*(yData[nLen-1] - yData[0]);

        // Nelder and Mead Simplex Regression
        SigmoidThresholdFunction f = new SigmoidThresholdFunction();
        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(f);
        this.nelderMead(vec, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
    }
    // method for fitting data to a Hill/Sips Sigmoid
    public void sigmoidHillSips(){
        fitsigmoidHillSips(0);
    }

    // method for fitting data to a Hill/Sips Sigmoid with plot and print out
    public void sigmoidHillSipsPlot(){
        fitsigmoidHillSips(1);
    }

    // method for fitting data to a Hill/Sips Sigmoid
    private void fitsigmoidHillSips(int plotFlag){

        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=28;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nTerms=3;
	    if(!this.scaleFlag)this.nTerms=2;
	    this.degreesOfFreedom=this.nData - this.nTerms;
	    if(this.degreesOfFreedom<1)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // Estimate  of theta
	    double yymin = Fmath.minimum(this.yData);
	    double yymax = Fmath.maximum(this.yData);
	    int dirFlag = 1;
	    if(yymin<0)dirFlag=-1;
	    double yyymid = (yymax - yymin)/2.0D;
	    double yyxmidl = xData[0][0];
	    int ii = 1;
	    int nLen = this.yData.length;
	    boolean test = true;
	    while(test){
	        if(this.yData[ii]>=dirFlag*yyymid){
	            yyxmidl = xData[0][ii];
	            test = false;
	        }
	        else{
	            ii++;
	            if(ii>=nLen){
	                yyxmidl = Stat.mean(this.xData[0]);
	                ii=nLen-1;
                    test = false;
                }
	        }
	    }
	    double yyxmidh = xData[0][nLen-1];
	    int jj = nLen-1;
	    test = true;
	    while(test){
	        if(this.yData[jj]<=dirFlag*yyymid){
	            yyxmidh = xData[0][jj];
	            test = false;
	        }
	        else{
	            jj--;
	            if(jj<0){
	                yyxmidh = Stat.mean(this.xData[0]);
	                jj=1;
                    test = false;
                }
	        }
	    }
	    int thetaPos = (ii+jj)/2;
	    double theta0 = xData[0][thetaPos];

        // initial estimates
        double[] start = new double[nTerms];
        start[0] = theta0;
        start[1] = 1;
        if(this.scaleFlag){
            if(dirFlag==1){
                start[2] = yymax;
            }
            else{
                start[2] = yymin;
            }
        }

        // initial step sizes
        double[] step = new double[nTerms];
        for(int i=0; i<nTerms; i++)step[i] = 0.1*start[i];
        if(step[0]==0.0D)step[0] = (xData[0][nLen-1] - xData[0][0])/20.0D;
        if(this.scaleFlag)if(step[2]==0.0D)step[2] = 0.1*(yData[nLen-1] - yData[0]);

        // Nelder and Mead Simplex Regression
        sigmoidHillSipsFunction f = new sigmoidHillSipsFunction();
        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(f);
        this.nelderMead(vec, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
    }


	// method for fitting data to a rectangular hyberbola
    public void rectangularHyperbola(){
        fitRectangularHyperbola(0);
    }

    // method for fitting data to a rectangular hyberbola with plot and print out
    public void rectangularHyperbolaPlot(){
        fitRectangularHyperbola(1);
    }

    // method for fitting data to a rectangular hyperbola
    private void fitRectangularHyperbola(int plotFlag){

        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=26;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nTerms=2;
	    if(!this.scaleFlag)this.nTerms=1;
	    this.degreesOfFreedom=this.nData - this.nTerms;
	    if(this.degreesOfFreedom<1)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // Estimate  of theta
	    double yymin = Fmath.minimum(this.yData);
	    double yymax = Fmath.maximum(this.yData);
	    int dirFlag = 1;
	    if(yymin<0)dirFlag=-1;
	    double yyymid = (yymax - yymin)/2.0D;
	    double yyxmidl = xData[0][0];
	    int ii = 1;
	    int nLen = this.yData.length;
	    boolean test = true;
	    while(test){
	        if(this.yData[ii]>=dirFlag*yyymid){
	            yyxmidl = xData[0][ii];
	            test = false;
	        }
	        else{
	            ii++;
	            if(ii>=nLen){
	                yyxmidl = Stat.mean(this.xData[0]);
	                ii=nLen-1;
                    test = false;
                }
	        }
	    }
	    double yyxmidh = xData[0][nLen-1];
	    int jj = nLen-1;
	    test = true;
	    while(test){
	        if(this.yData[jj]<=dirFlag*yyymid){
	            yyxmidh = xData[0][jj];
	            test = false;
	        }
	        else{
	            jj--;
	            if(jj<0){
	                yyxmidh = Stat.mean(this.xData[0]);
	                jj=1;
                    test = false;
                }
	        }
	    }
	    int thetaPos = (ii+jj)/2;
	    double theta0 = xData[0][thetaPos];

        // initial estimates
        double[] start = new double[nTerms];
        start[0] = theta0;
        if(this.scaleFlag){
            if(dirFlag==1){
                start[1] = yymax;
            }
            else{
                start[1] = yymin;
            }
        }

        // initial step sizes
        double[] step = new double[nTerms];
        for(int i=0; i<nTerms; i++)step[i] = 0.1*start[i];
        if(step[0]==0.0D)step[0] = (xData[0][nLen-1] - xData[0][0])/20.0D;
        if(this.scaleFlag)if(step[1]==0.0D)step[1] = 0.1*(yData[nLen-1] - yData[0]);

        // Nelder and Mead Simplex Regression
        RectangularHyperbolaFunction f = new RectangularHyperbolaFunction();
        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(f);
        this.nelderMead(vec, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
    }

// method for fitting data to a scaled Heaviside Step Function
    public void stepFunction(){
        fitStepFunction(0);
    }

    // method for fitting data to a scaled Heaviside Step Function with plot and print out
    public void stepFunctionPlot(){
        fitStepFunction(1);
    }

    // method for fitting data to a scaled Heaviside Step Function
    private void fitStepFunction(int plotFlag){

        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=27;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nTerms=2;
	    if(!this.scaleFlag)this.nTerms=1;
	    this.degreesOfFreedom=this.nData - this.nTerms;
	    if(this.degreesOfFreedom<1)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // Estimate  of theta
	    double yymin = Fmath.minimum(this.yData);
	    double yymax = Fmath.maximum(this.yData);
	    int dirFlag = 1;
	    if(yymin<0)dirFlag=-1;
	    double yyymid = (yymax - yymin)/2.0D;
	    double yyxmidl = xData[0][0];
	    int ii = 1;
	    int nLen = this.yData.length;
	    boolean test = true;
	    while(test){
	        if(this.yData[ii]>=dirFlag*yyymid){
	            yyxmidl = xData[0][ii];
	            test = false;
	        }
	        else{
	            ii++;
	            if(ii>=nLen){
	                yyxmidl = Stat.mean(this.xData[0]);
	                ii=nLen-1;
                    test = false;
                }
	        }
	    }
	    double yyxmidh = xData[0][nLen-1];
	    int jj = nLen-1;
	    test = true;
	    while(test){
	        if(this.yData[jj]<=dirFlag*yyymid){
	            yyxmidh = xData[0][jj];
	            test = false;
	        }
	        else{
	            jj--;
	            if(jj<0){
	                yyxmidh = Stat.mean(this.xData[0]);
	                jj=1;
                    test = false;
                }
	        }
	    }
	    int thetaPos = (ii+jj)/2;
	    double theta0 = xData[0][thetaPos];

        // initial estimates
        double[] start = new double[nTerms];
        start[0] = theta0;
        if(this.scaleFlag){
            if(dirFlag==1){
                start[1] = yymax;
            }
            else{
                start[1] = yymin;
            }
        }

        // initial step sizes
        double[] step = new double[nTerms];
        for(int i=0; i<nTerms; i++)step[i] = 0.1*start[i];
        if(step[0]==0.0D)step[0] = (xData[0][nLen-1] - xData[0][0])/20.0D;
        if(this.scaleFlag)if(step[1]==0.0D)step[1] = 0.1*(yData[nLen-1] - yData[0]);

        // Nelder and Mead Simplex Regression
        StepFunctionFunction f = new StepFunctionFunction();
        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;
        Vector<Object> vec = new Vector<Object>();
        vec.addElement(f);
        this.nelderMead(vec, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
    }
}

//  CLASSES TO EVALUATE THE SPECIAL FUNCTIONS


// Class to evaluate the Gausian function y = (yscale/sd.sqrt(2.pi)).exp(-[(x - xmean)/(2.sd)]^2).
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

// Class to evaluate the Lorentzian function
// y = (yscale/pi).(gamma/2)/((x - mu)^2+(gamma/2)^2).
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

// Class to evaluate the Poisson function
// y = yscale.(mu^k).exp(-mu)/k!.
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

// Class to evaluate the Gumbel function
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

// Class to evaluate the Frechet function
// y = yscale.(gamma/sigma)*((x - mu)/sigma)^(-gamma-1)*exp(-((x-mu)/sigma)^-gamma
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

// Class to evaluate the semi-linearised Frechet function
// log(log(1/(1-Cumulative y) = gamma*log((x-mu)/sigma)
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

// Class to evaluate the Weibull function
// y = yscale.(gamma/sigma)*((x - mu)/sigma)^(gamma-1)*exp(-((x-mu)/sigma)^gamma
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

// Class to evaluate the semi-linearised Weibull function
// log(log(1/(1-Cumulative y) = gamma*log((x-mu)/sigma)
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

// Class to evaluate the Rayleigh function
// y = (yscale/sigma)*(x/sigma)*exp(-0.5((x-mu)/sigma)^2
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


// Class to evaluate the semi-linearised Rayleigh function
// log(1/(1-Cumulative y) = 0.5*(x/sigma)^2
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

// class to evaluate a Pareto scaled pdf
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

// class to evaluate a Pareto cdf
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

// class to evaluate a Sigmoidal threshold function
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

// class to evaluate a Rectangular Hyberbola
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

// class to evaluate a scaled Heaviside Step Function
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

// class to evaluate a Hill or Sips sigmoidal function
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
