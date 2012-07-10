/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
*/
package org.gumtree.data.nexus.fitting.jas3;

import hep.aida.IAnalysisFactory;
import hep.aida.IFitFactory;
import hep.aida.IFitResult;
import hep.aida.IFitter;
import hep.aida.IFunction;
import hep.aida.IFunctionFactory;
import hep.aida.IHistogram1D;
import hep.aida.IHistogramFactory;
import hep.aida.IPlotter;
import hep.aida.ITree;
import hep.aida.ITreeFactory;

import java.util.Random;

/**
 * @author jgw
 * Tel: +61 2 9717 7062  Fax: +61 2 9717 9799
 * Data Analysis Team, Bragg Institute,Bld.82
 * ANSTO PMB 1 Menai NSW 2234 AUSTRALIA

 */
public class GenericDataFitting {
	
	double[][] fittedScanVect = null;
	double meanOfScan = 0.0;
	double sigma = 0.0;
	double heightOfScan = 0.0;
	double chisq = 0.0;
	double bg  = 0.0;

      /**
       * Method is developed for detector scan data fitting.
       *  For developer, If you try to modify objects in this class, you'd better understand CERN HBOOK package and PAW package
       *  Script languge is defined use PAW. All script inpiut should be consistant with PAW and HBOOK
       * @param scanDat   One D detector scan data array
       * @param axisVect   One D axis array.  there must  be scanDat.length = axisVect.length.
       * @param  resoVect  User required resolution vector that is used to make return fiittedVect
       * @param fitFunc       Fitting function name, such as Gaussian, Lorenzian or Polynomial function
       * @param fittingParam  this designed fot user defined fitting function. If you do not use user defined function, just input "null"
       *                                     regarding to polynomial function, n order of polynomial should be written as Poly(2) or Poly(n)
       * @return  fitted one D detector scan array.
       */	      
      public GenericDataFitting (double[] scanDat, double[] axisVect, double[] resoVect, String fitFunc, String fittingParam) {
	      // Create factories
//          public  double[] detectorScanProcess (double[] scanDat, double[] axisVect) {    		
	      IAnalysisFactory  analysisFactory = IAnalysisFactory.create();
	      ITreeFactory      treeFactory = analysisFactory.createTreeFactory();
	      ITree             tree = treeFactory.create();
//	      IPlotter          plotter = analysisFactory.createPlotterFactory().create("SimpleFittingJas3.java Plot");
	      IHistogramFactory histogramFactory = analysisFactory.createHistogramFactory(tree);
	      IFunctionFactory  functionFactory = analysisFactory.createFunctionFactory(tree);
	      IFitFactory       fitFactory = analysisFactory.createFitFactory();
	    
	      int nScan =scanDat.length;
	      double xMin = Double.MAX_VALUE;
	      double xMax =Double.MIN_VALUE;
	      if (resoVect == null) resoVect = axisVect;
	      
	      for (int n = 0; n < nScan; n++) {
	    	  if (axisVect[n] < xMin) xMin = axisVect[n];
	    	  if (axisVect[n] >xMax) xMax = axisVect[n];
	      }
	  
	      String graphLabel = null;
	  
	      if (fitFunc.equals("Guassian") ) graphLabel = "Scan Data with Gaussian Fitting";
	                              else                         graphLabel = "Scan Data with Polynomial Fitting";
	      IHistogram1D histDet = histogramFactory.createHistogram1D("Monitor Scan",nScan,xMin, xMax);
		
	    	  


	      for (int i=0; i<nScan; i++) {
		    	double xValue = axisVect[i];
		    	int         yValue =  (int) (scanDat[i] + 0.5);

		    	for (int k = 0; k < yValue; k++ )
		    	histDet.fill(xValue);
	      }


	   // The AIDA Factories

      if ( fitFunc.equals("Gaussian") ){
    	  
	      IFunction gauss = functionFactory.createFunctionFromScript("gauss", 1,
          "background+a*exp(-(x[0]-mean)*(x[0]-mean)/sigma/sigma)","a,mean,sigma,background","A Gaussian");   
	      gauss.setParameter("a",histDet.maxBinHeight());
	      gauss.setParameter("mean",histDet.mean());
	      gauss.setParameter("sigma",histDet.rms());

//	      plotter.region(0).plot(histDet);
//	      plotter.region(0).plot(gauss);


	      IFitter jminuit = fitFactory.createFitter("Chi2","jminuit");

	      IFitResult jminuitResult = jminuit.fit(histDet,gauss);

//	      plotter.region(0).plot(jminuitResult.fittedFunction());
//	      plotter.show();

	      functionFactory.cloneFunction("fitted gauss (jminuit)",jminuitResult.fittedFunction());
	      
	      System.out.println("jminuit Chi2="+jminuitResult.quality());
	      chisq = jminuitResult.quality();
	      meanOfScan  = new Double(jminuitResult.fittedParameter("mean"));		
	      sigma     = new Double(jminuitResult.fittedParameter("sigma"));		
          bg   = jminuitResult.fittedParameter("background");
	      double norm = jminuitResult.fittedParameter("a") ;
	      heightOfScan =  new Double(norm + bg );

	      // Set fitted scanVector values. background+a*exp(-(x[0]-mean)*(x[0]-mean)/sigma/sigma
	      int nResol = resoVect.length;
	      fittedScanVect = new  double[3][nResol];

	      for (int j = 0; j < nResol; j++)
	    	  fittedScanVect[0][j] =  bg + norm*Math.exp(-(resoVect[j]-meanOfScan)
	    			                                                                  *(resoVect[j]-meanOfScan)/(sigma*sigma));

      }  
      
      else if ( fitFunc.equals("Poly(2)") ){
    	  
	      IFunction parabola =  functionFactory.createFunctionFromScript(
	    		                                 "parabola",1,"background + (a*x[0]*x[0]+b*x[0]+c)","a,b,c,background","A Parabola");
	      parabola.setParameter("c",histDet.maxBinHeight());
	      parabola.setParameter("b",histDet.mean());
	      parabola.setParameter("a",histDet.rms());
	      parabola.setParameter("background",histDet.minBinHeight());
//	      plotter.region(0).plot(histDet);
//	      plotter.region(0).plot(parabola);

	      IFitter jminuit = fitFactory.createFitter("Chi2","jminuit");

	      IFitResult jminuitResult = jminuit.fit(histDet,parabola);

//	      plotter.region(0).plot(jminuitResult.fittedFunction());
//	      plotter.show();

	      functionFactory.cloneFunction("fitted parabola (jminuit)",jminuitResult.fittedFunction());	      
// Constant calculation
	      double aValue = jminuitResult.fittedParameter("a");
	      double bValue = jminuitResult.fittedParameter("b");
	      double cValue = jminuitResult.fittedParameter("c");
	      double bg         = jminuitResult.fittedParameter("background");
//Set fitted scanVector values. a*x^2 + b*x + c
	      int nResol = resoVect.length;
	      fittedScanVect = new  double[3][nResol];
	      for (int j = 0; j < nResol; j++)
	    	  fittedScanVect[0][j] = aValue*resoVect[j]*resoVect[j] +  bValue*resoVect[j] +  cValue + bg;

//	    	  fittedScanVect[j] = aValue*axisVect[j]*axisVect[j] +  bValue*axisVect[j] +  cValue + bg;
//        Function f(x) = a*( x + b/2*a )^2 - b*b/4*a + c	
	      
	      chisq = jminuitResult.quality();
	      meanOfScan  = new Double(- bValue /(2*aValue));
	      heightOfScan =  new Double( cValue - bValue*bValue/(4*aValue) + bg);
     
	      System.out.println("jminuit Chi2= "+jminuitResult.quality());
      }
      else if (fitFunc.equals("Kowari")) {
    	  
    	  kowariSpecialFitting(scanDat, axisVect, resoVect,  fitFunc) ;
      }
      
      else {
    	  
    	  userDefinedFitting(scanDat, axisVect, resoVect,  fitFunc, fittingParam);
      }
      
	      System.out.println("PeakPos = "+ meanOfScan );
	      System.out.println("Height     = "+ heightOfScan);

//    	  return fittedScanVect;
      }
      

      public double[][] getFittedScanVect(){
    	  return fittedScanVect;
      }
      
      
      /**
       * 
       * @return   Fitted  {peakPosition ,heightOfScanPeak, ChiSqare }
       *  peakPosition:     Mean value of  axisVect, This value is peak position of detector scan data vector.
       *  heightOfScanPeak: Peak value of detector scan vector
       *  fitSigma:                    Sigma if fitting with Gaussian otherwise  return is 0.0;
       *  chisq:     fitting quality chi square
       */
	public double[] getFittedParams() {
		return new double[] {meanOfScan, heightOfScan, sigma,  chisq };
	}


	/**
	 * 
	 * @param scanDat
	 * @param axisVect
	 * @param resoVect
	 * @param fitFunc
	 * @return
	 */
	
	private  void kowariSpecialFitting(double[] scanDat, double[] axisVect, double[] resoVect, String fitFunc) 
	{
	      // Create factories
//          public  double[] detectorScanProcess (double[] scanDat, double[] axisVect) {    		
	      IAnalysisFactory  analysisFactory = IAnalysisFactory.create();
	      ITreeFactory      treeFactory = analysisFactory.createTreeFactory();
	      ITree             tree = treeFactory.create();
//	      IPlotter          plotter = analysisFactory.createPlotterFactory().create("SimpleFittingJas3.java Plot");
	      IHistogramFactory histogramFactory = analysisFactory.createHistogramFactory(tree);
	      IFunctionFactory  functionFactory = analysisFactory.createFunctionFactory(tree);
	      IFitFactory       fitFactory = analysisFactory.createFitFactory();
	    
	      int nScan =scanDat.length;
	      double xMin = Double.MAX_VALUE;
	      double xMax =Double.MIN_VALUE;
	      if (resoVect == null) resoVect = axisVect;
	      
	      for (int n = 0; n < nScan; n++) {
	    	  if (axisVect[n] < xMin) xMin = axisVect[n];
	    	  if (axisVect[n] >xMax) xMax = axisVect[n];
	      }
	      String graphLabel = null;
	      if (fitFunc.equals("Guassian") ) graphLabel = "Scan Data with Gaussian Fitting";
	                              else                         graphLabel = "Scan Data with Polynomial Fitting";
	      IHistogram1D histDet = histogramFactory.createHistogram1D("Monitor Scan",nScan,xMin, xMax);
		
	    	  


	      for (int i=0; i<nScan; i++) {
		    	double xValue = axisVect[i];
		    	int         yValue =  (int) (scanDat[i] + 0.5);

		    	for (int k = 0; k < yValue; k++ )
		    	histDet.fill(xValue);
	      }


	   // The AIDA Factories


    	  
	      IFunction gauss = functionFactory.createFunctionFromScript("gauss", 1,
        "background+norm*(a*exp(-(x[0]-mean)*(x[0]-mean)/sigma/sigma) + (1-a)*b/((x[0]-mean)*(x[0]-mean)-b*b)) "
	    ,"a,mean,sigma,background","A Gaussian");   
	      gauss.setParameter("a",histDet.maxBinHeight());
	      gauss.setParameter("mean",histDet.mean());
	      gauss.setParameter("sigma",histDet.rms());

//	      plotter.region(0).plot(histDet);
//	      plotter.region(0).plot(gauss);


	      IFitter jminuit = fitFactory.createFitter("Chi2","jminuit");

	      IFitResult jminuitResult = jminuit.fit(histDet,gauss);

//	      plotter.region(0).plot(jminuitResult.fittedFunction());
//	      plotter.show();

	      functionFactory.cloneFunction("fitted gauss (jminuit)",jminuitResult.fittedFunction());
	      
	      System.out.println("jminuit Chi2="+jminuitResult.quality());
	      chisq = jminuitResult.quality();
	      meanOfScan  = new Double(jminuitResult.fittedParameter("mean"));		
	      sigma     = new Double(jminuitResult.fittedParameter("sigma"));		
           bg   = jminuitResult.fittedParameter("background");
	      double norm = jminuitResult.fittedParameter("a") ;
	      heightOfScan =  new Double(norm + bg );

	      // Set fitted scanVector values. background+a*exp(-(x[0]-mean)*(x[0]-mean)/sigma/sigma
	      int nResol = resoVect.length;
	      fittedScanVect = new  double[3][nResol];

	      for (int j = 0; j < nResol; j++)
	    	  fittedScanVect[0][j] =  bg + norm*Math.exp(-(resoVect[j]-meanOfScan)
	    			                                                                  *(resoVect[j]-meanOfScan)/(sigma*sigma));


      

      
	      System.out.println("PeakPos = "+ meanOfScan );
	      System.out.println("Height     = "+ heightOfScan);

		
	}
	
	/**
	 * 
	 * @param scanDat
	 * @param axisVect
	 * @param resoVect
	 * @param fitFunc         input use script language such as 
	 *               "background+norm*(a*exp(-(x[0]-mean)*(x[0]-mean)/sigma/sigma) + (1-a)*b/((x[0]-mean)*(x[0]-mean)-b*b)) "
	 * @param fittingParam  input such as "a,mean,sigma,background"
	 * @return
	 */
	
	private  void userDefinedFitting(double[] scanDat, double[] axisVect, double[] resoVect, String fitFunc, String fittingParam) 
	{
	      // Create factories
//          public  double[] detectorScanProcess (double[] scanDat, double[] axisVect) {    		
	      IAnalysisFactory  analysisFactory = IAnalysisFactory.create();
	      ITreeFactory      treeFactory = analysisFactory.createTreeFactory();
	      ITree             tree = treeFactory.create();
	      IPlotter          plotter = analysisFactory.createPlotterFactory().create("SimpleFittingJas3.java Plot");
	      IHistogramFactory histogramFactory = analysisFactory.createHistogramFactory(tree);
	      IFunctionFactory  functionFactory = analysisFactory.createFunctionFactory(tree);
	      IFitFactory       fitFactory = analysisFactory.createFitFactory();
	    
	      int nScan =scanDat.length;
	      double xMin = Double.MAX_VALUE;
	      double xMax =Double.MIN_VALUE;
	      if (resoVect == null) resoVect = axisVect;
	      
	      for (int n = 0; n < nScan; n++) {
	    	  if (axisVect[n] < xMin) xMin = axisVect[n];
	    	  if (axisVect[n] >xMax) xMax = axisVect[n];
	      }
	      String graphLabel = null;
	     
              graphLabel = "User Defined Function Fitting";
	      IHistogram1D histDet = histogramFactory.createHistogram1D("Monitor Scan",nScan,xMin, xMax);
		
	    	  


	      for (int i=0; i<nScan; i++) {
		    	double xValue = axisVect[i];
		    	int         yValue =  (int) (scanDat[i] + 0.5);

		    	for (int k = 0; k < yValue; k++ )
		    	histDet.fill(xValue);
	      }


	   // The AIDA Factories


    	  
	      IFunction gauss = functionFactory.createFunctionFromScript("gauss", 1,
                       fitFunc , fittingParam,"A Gaussian");   
	      gauss.setParameter("a",histDet.maxBinHeight());
	      gauss.setParameter("mean",histDet.mean());
	      gauss.setParameter("sigma",histDet.rms());

	      plotter.region(0).plot(histDet);
	      plotter.region(0).plot(gauss);


	      IFitter jminuit = fitFactory.createFitter("Chi2","jminuit");

	      IFitResult jminuitResult = jminuit.fit(histDet,gauss);

//	      plotter.region(0).plot(jminuitResult.fittedFunction());
//	      plotter.show();

	      functionFactory.cloneFunction("fitted gauss (jminuit)",jminuitResult.fittedFunction());
	      
	      System.out.println("jminuit Chi2="+jminuitResult.quality());
	      chisq = jminuitResult.quality();
	      meanOfScan  = new Double(jminuitResult.fittedParameter("mean"));		
	      sigma     = new Double(jminuitResult.fittedParameter("sigma"));		
        bg   = jminuitResult.fittedParameter("background");
	      double norm = jminuitResult.fittedParameter("a") ;
	      heightOfScan =  new Double(norm + bg );

	      // Set fitted scanVector values. background+a*exp(-(x[0]-mean)*(x[0]-mean)/sigma/sigma
	      int nResol = resoVect.length;
	      fittedScanVect = new  double[3][nResol];

//	      for (int j = 0; j < nResol; j++)
//	    	  fittedScanVect[j] =  bg + norm*Math.exp(-(resoVect[j]-meanOfScan)
//	    			                                                                  *(resoVect[j]-meanOfScan)/(sigma*sigma));

    	  fittedScanVect[0] = jminuitResult.fittedFunction().gradient(resoVect);
      

      
	      System.out.println("PeakPos = "+ meanOfScan );
	      System.out.println("Height     = "+ heightOfScan);

		
	}
	/**
	 * Main method for test only 
	 * 
	 * @param args
	 */
	
	
	public static void main(String[] args)
	   {
    	  
      // Create factories

      IAnalysisFactory  analysisFactory = IAnalysisFactory.create();
      ITreeFactory      treeFactory = analysisFactory.createTreeFactory();
      ITree             tree = treeFactory.create();
//      IPlotter          plotter = analysisFactory.createPlotterFactory().create("SimpleFittingJas3.java Plot");
      IHistogramFactory histogramFactory = analysisFactory.createHistogramFactory(tree);
      IFunctionFactory  functionFactory = analysisFactory.createFunctionFactory(tree);
      IFitFactory       fitFactory = analysisFactory.createFitFactory();
    
      IHistogram1D h1 = histogramFactory.createHistogram1D("Histogram 1D",200,-5,5);
	
    	  
      Random r = new Random();

      for (int i=0; i<100000; i++) {
	    	double x = ((double )i - 50000.0)/10000.0;
	    	double y = 25 - x*x;
	    	int inty = (int) y;
	    	for (int k = 0; k < inty; k++ )
	    	h1.fill(x);
//	          h1.fill(x);
//	      h1.fill(r.nextGaussian());
//          h1.fill(r.nextDouble()*10-5);
      }

//      IFunction gauss = functionFactory.createFunctionFromScript("gauss", 1,
//    		                           "background+a*exp(-(x[0]-mean)*(x[0]-mean)/sigma/sigma)","a,mean,sigma,background","A Gaussian");
   // The AIDA Factories

     
      IFunction parabola =  functionFactory.createFunctionFromScript("parabola",
    		                                 1,"background + (a*x[0]*x[0]+b*x[0]+c)","a,b,c,background","A Garabola");
//      gauss.setParameter("a",h1.maxBinHeight());
//      gauss.setParameter("mean",h1.mean());
//      gauss.setParameter("sigma",h1.rms());
      parabola.setParameter("c",h1.maxBinHeight());
      parabola.setParameter("b",h1.mean());
      parabola.setParameter("a",h1.rms());
//      plotter.region(0).plot(h1);
//      plotter.region(0).plot(parabola);
//      plotter.region(0).plot(gauss);

      IFitter jminuit = fitFactory.createFitter("Chi2","jminuit");

      IFitResult jminuitResult = jminuit.fit(h1,parabola);
//      IFitResult jminuitResult = jminuit.fit(h1,gauss);
//      plotter.region(0).plot(jminuitResult.fittedFunction());
//      plotter.show();

//      functionFactory.cloneFunction("fitted gauss (jminuit)",jminuitResult.fittedFunction());
      functionFactory.cloneFunction("fitted parabola (jminuit)",jminuitResult.fittedFunction());	      
//Constant calculation
      double aValue = jminuitResult.fittedParameter("a");
      double bValue = jminuitResult.fittedParameter("b");
      double cValue = jminuitResult.fittedParameter("c");
//Function f(x) = a*( x + b/2*a )^2 - b*b/4*a + c	 
      double fitMean = new Double(-bValue / 2*aValue);
      double fitHeight = new Double(cValue - bValue*bValue/(4*aValue));
      
      System.out.println("jminuit Chi2= "+jminuitResult.quality());

      System.out.println("Mean = "+ fitMean );
      System.out.println("Height = "+ fitHeight);
   }


}




