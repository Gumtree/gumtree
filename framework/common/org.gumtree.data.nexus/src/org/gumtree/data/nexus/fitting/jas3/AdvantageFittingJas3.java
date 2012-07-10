package org.gumtree.data.nexus.fitting.jas3;

import java.util.Random;

//import au.gov.ansto.bragg.freehep.anstoaida.*;
import hep.aida.IAnalysisFactory;
import hep.aida.IFitData;
import hep.aida.IFitFactory;
import hep.aida.IFitResult;
import hep.aida.IFitter;
import hep.aida.IFunction;
import hep.aida.IFunctionFactory;
import hep.aida.IHistogram2D;
import hep.aida.IHistogramFactory;
import hep.aida.IPlotter;
import hep.aida.ITree;
import hep.aida.ITupleFactory;
/**
 * @author jgw
 * Tel: +61 2 9717 7062  Fax: +61 2 9717 9799
 * Data Analysis Team, Bragg Institute,Bld.82
 * ANSTO PMB 1 Menai NSW 2234 AUSTRALIA

 */
public class AdvantageFittingJas3 {
	  public static void main(String[] argv) throws java.io.IOException
	  {
	    
	    IAnalysisFactory  anFactory   = IAnalysisFactory.create();
	    ITree             tree        = anFactory.createTreeFactory().create();
	    IHistogramFactory histFactory = anFactory.createHistogramFactory( tree );
	    ITupleFactory     tuplFactory = anFactory.createTupleFactory( tree );
	    IFunctionFactory  funcFactory = anFactory.createFunctionFactory( tree );
	    IFitFactory       fitFactory  = anFactory.createFitFactory();
	    IFitter           fitter      = fitFactory.createFitter("Chi2","jminuit");
	    
	    IHistogram2D hist  = histFactory.createHistogram2D("hist","Test Histogram",100,-5,15,50,-5,5);

	    Random r = new Random();

	    for (int i=0; i<10000; i++) {
//	      double x = r.nextGaussian()+5;
//	      if ( r.nextDouble() > 0.8 ) x = 2.5*r.nextGaussian()+5;
//	      double y = r.nextGaussian();
	    	double x = (double )i;
	    	double y = 6000 - x*x  - 70.0 * r.nextGaussian()/(1.0 +x);
	      hist.fill(x,y);
	    }
	    
	    IFitData fitData = fitFactory.createFitData();
	    fitData.create2DConnection(hist);

//	    IFunction func = funcFactory.createFunctionFromScript("twoDdistr",2, "N*(a*exp( -(x[0]-mu0)*(x[0]-mu0)/(2*s0*s0) )+"
//	                                 + "(1-a)*exp( -(x[0]-mu1)*(x[0]-mu1)/(2*s1*s1) ))*exp( -(x[1]-mu2)*"
//	                                 + "(x[1]-mu2)/(2*s2*s2) )","N,a,mu0,s0,mu1,s1,mu2,s2"," ",null);
	    IFunction func = funcFactory.createFunctionFromScript("twoDdistr",2, "N*(a*exp( -(x[0]-mu0)*(x[0]-mu0)/(2*s0*s0) )+"
                + "(1-a)*exp( -(x[0]-mu1)*(x[0]-mu1)/(2*s1*s1) ))*exp( -(x[1]-mu2)*"
                + "(x[1]-mu2)/(2*s2*s2) )","N,a,mu0,s0,mu1,s1,mu2,s2"," ",null);

	    double[] initialPars = { 1, 0.8, 5, 1, 5, 2, 0, 1};
	    func.setParameters( initialPars );

	    fitter.fitParameterSettings("mu2").setFixed(true);
	    fitter.fitParameterSettings("a").setBounds(0.5,0.9);
	    fitter.fitParameterSettings("a").setStepSize(0.001);
	    fitter.fitParameterSettings("s1").setBounds(2,4);
	    fitter.fitParameterSettings("s1").setStepSize(0.1);
	    fitter.setConstraint("s0 = s2");
	    fitter.setConstraint("mu0 = mu1");


	    IFitResult fitResult = fitter.fit(fitData,func);

	    System.out.println("Chi2 = "+fitResult.quality());
	    
	    double[] fPars     = fitResult.fittedParameters();
	    double[] fParErrs  = fitResult.errors();
	    String[] fParNames = fitResult.fittedParameterNames();

	    for(int i=0; i< fitResult.fittedFunction().numberOfParameters(); i++ ) 
	      System.out.println(fParNames[i]+" : "+fPars[i]+" +- "+fParErrs[i]);


	    IPlotter plotter = anFactory.createPlotterFactory().create("AdvantageFittingJas3 plot");
	    plotter.destroyRegions();
	    plotter.createRegion(0,0,.66,1).plot(hist);
	    plotter.createRegion(.66,0,.33,.5).plot( histFactory.projectionX("projX",hist) );
	    plotter.createRegion(.66,.5,.33,.5).plot( histFactory.projectionY("projY",hist) );
	    plotter.show();

	  }
}
