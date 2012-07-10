/*  Program PsRandom
*
*   Class for obtaining a single decimal or binary pseudorandom number
*   or a sequence of decimal or binary pseudorandom numbers
*   Supplements the Java random class with the generation of
*   pseudorandom binary numbers and of lorentzian, poissonian, pareto,
*   exponential, gumbel, weibull, frechet, and rayleigh deviates in
*   addition to the gaussian (normal) and correlated Gaussian deviates.
*   Also offers a choice of Knuth or Park-Miller generation methods.
*
*   Binary pseudorandom number calculations are adapted from
*   the Numerical Recipes methods written in the C language
*   based on the "primitive polynomials modulo 2" method:
*   Numerical Recipes in C, The Art of Scientific Computing,
*   W.H. Press, S.A. Teukolsky, W.T. Vetterling & B.P. Flannery,
*   Cambridge University Press, 2nd Edition (1992) pp 296 - 300.
*   (http://www.nr.com/).
*
*   AUTHOR: Dr Michael Thomas Flanagan
*   DATE:   22 April 2004
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   PsRandom.html
*
*   Copyright (c) April 2004  Michael Thomas Flanagan
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

package flanagan.math;

import java.util.*;
import flanagan.analysis.Stat;

public class PsRandom{

    private long seed;          //  current seed value -   updated after each generation of a pseudorandom bit
                                //  initial seed supplied as either:
                                //              i.  as the current clock time (in milliseconds since 1970)
                                //                  (no argument given to the constructor)
                                //              ii. by the user as the constructor argument
                                //  method are available for resetting the value of the seed

    private long initialSeed;   // initial seed value

    private int methodOptionDecimal = 1;    // Method for calculating pseudorandom decimal numbers
                                 // = 1 Method -    Knuth - this class calls the Java Random class which
                                 //                 implements this method
                                 //                 See Numerical Recipes in C - W.H. Press et al. (Cambridge)
    		                     //                 1st edition 1988 p. 212, 2nd edition 1992 p283 for details
    		                     //                 This is the default option used in all methods in this class,
    		                     //                 Pseudorandom, generating a decimal random number, i.e. all but
    		                     //                 the methods to generate pseudorandom binary numbers.
                                 // = 2 Method -    Park and Miller random number generator with Bays-Durham shuffle
                                 //                 after 	ran1 	(Numerical Recipes in C - W.H. Press et al. (Cambridge)
    		                     //                 2nd edition 1992 p280.

    private Random rr = null;    // instance of java.util.Random if Knuth method (default method) used

    private int methodOptionBinary = 1;     // Method for calculating pseudorandom binary numbers
                                // = 1 Method -     Primitive polynomials modulo 2 method - version 1
                                //                  This method is the more cumbersome one in terms of coding (see method 2 below)
                                //                  but more readily lends itself to a shift register hardware implementation
                                // = 2 Method -     Primitive polynomials modulo 2 method - version 2
                                //                  This method is the more suited to software implementation (compare with method 1 above)
                                //                  but lends itself less readily to a shift register hardware implementation
                                // Method 1 is the default option

    // Park and Miller constants and variables
    private long ia     = 16807L;
    private long im     = 2147483647L;
    private double am   = 1.0D/im;
    private long iq     = 127773L;
    private long ir     = 2836L;
    private int ntab    = 32;
    private long ndiv   = (1L + (im - 1L)/ntab);
    private double eps  = 1.2e-7;
    private double rnmx = 1.0D - eps;
    private long iy     = 0L;
    private long[] iv = new long[ntab];

    // Box-Muller variables
	private int	    iset = 0;
	private double	gset = 0.0D;

    // Polynomial powers of 2 (used in calculation of psedorandom binary numbers)
    // See header reference (Numerical Recipes) above for polynomials other than (18, 5, 2, 1, 0)
    private long powTwo1    = 1;
    private long powTwo2    = 2;
    private long powTwo5    = 16;
    private long powTwo18   = 131072;
    private long mask       = powTwo1 + powTwo2 + powTwo5;

    // CONSTRUCTORS

    // Seed taken from the clock
    public PsRandom(){
        this.seed = System.currentTimeMillis();
        this.initialSeed = this.seed;
        this.rr = new Random(this.seed);
    }

    // Seed supplied by user
    public PsRandom(long seed){
        this.seed = seed;
        this.initialSeed = seed;
        this.rr = new Random(this.seed);
    }

    // METHODS

    // Resets the value of the seed
    public void setSeed(long seed){
        this.seed = seed;
        if(this.methodOptionDecimal==1)rr = new Random(this.seed);
    }

    // Returns the initial value of the seed
    public long getInitialSeed(){
        return this.initialSeed;
    }

    // Returns the current value of the seed
    public long getSeed(){
        return this.seed;
    }

    // Resets the method of calculation of a pseudorandom decimal number
    // argument = 1 -> Knuth; argument = 2 -> Parker-Miller
    // Default option = 1
    public void setMethodDecimal(int methodOpt){
        if(methodOpt<1 || methodOpt>2)throw new IllegalArgumentException("Argument to RandomBits.setMethodDecimal must 1 or 2\nValuetransferred was"+methodOpt);
        this.methodOptionDecimal = methodOpt;
        if(methodOpt==1)rr = new Random(this.seed);
    }

    // Return the binary pseudorandom number method option; 1 = Method 1, 2= Method 2
    public int getMethodDecimal(){
        return this.methodOptionDecimal;
    }
    // Resets the method of calculation of a pseudorandom binary number
    // argument = 1 -> method 1; argument = 2 -> Method 2
    // See above and Numerical Recipes reference (in program header) for method descriptions
    // Default option = 1
    public void setMethodBinary(int methodOpt){
        if(methodOpt<1 || methodOpt>2)throw new IllegalArgumentException("Argument to RandomBits.setMethodBinary must 1 or 2\nValuetransferred was"+methodOpt);
        this.methodOptionBinary = methodOpt;
    }

    // Return the binary pseudorandom number method option; 1 = Method 1, 2= Method 2
    public int getMethodBinary(){
        return this.methodOptionBinary;
    }

    // Returns a pseudorandom double between 0.0 and 1.0
    public double nextDouble(){
        if(this.methodOptionDecimal==1){
            return rr.nextDouble();
        }
        else{
            return  this.parkMiller();
        }
    }

    // Returns an array, of length arrayLength, of pseudorandom doubles between 0.0 and 1.0
    public double[] doubleArray(int arrayLength){
        double[] array = new double[arrayLength];
        if(this.methodOptionDecimal==1){
            for(int i=0; i<arrayLength; i++){
                array[i] = rr.nextDouble();
            }
        }
        else{
            for(int i=0; i<arrayLength; i++){
                array[i] = this.parkMiller();
            }
        }
        return array;
    }

    //  Park and Miller random number generator with Bays-Durham shuffle
    //  after 	ran1 	Numerical Recipes in C - W.H. Press et al. (Cambridge)
    //		            2nd edition 1992 p280.
    // return a pseudorandom number between 0.0 and 1.0
    double parkMiller(){
	    int jj  = 0;
	    long kk = 0L;
	    double temp = 0.0D;
	    this.iy = 0L;

	    if(this.seed <= 0L || iy!=0){
		    if(-this.seed < 1){
		        this.seed = 1;
		    }
		    else{
		        this.seed = -this.seed;
		    }
		    for(int j=ntab+7; j>=0; j--){
			    kk = this.seed/iq;
			    this.seed = ia*( this.seed - kk*iq)- ir*kk;
			    if(this.seed < 0L) this.seed += im;
			    if (j < ntab) iv[j] = this.seed;
		    }
		    iy = iv[0];
	    }
	    kk = this.seed/iq;
	    this.seed = ia*(this.seed - kk*iq)-ir*kk;
	    if(this.seed < 0)this.seed += im;
	    jj = (int)(iy/ndiv);
	    iy = iv[jj];
	    iv[jj] = this.seed;
	    if((temp = am*iy) > rnmx){
	        return rnmx;
	    }
	    else{
	        return temp;
	    }
	}

    // Returns a pseudorandom bit
    public int nextBit(){
        if(this.methodOptionBinary==1){
            return nextBitM1();
        }
        else{
            return  nextBitM2();
        }
    }

    // Returns an array, of length arrayLength, of pseudorandom bits
    public int[] bitArray(int arrayLength){
        int[] bitarray = new int[arrayLength];
        for(int i=0; i<arrayLength; i++){
             bitarray[i]=nextBit();
        }
        return bitarray;
     }

    // Returns a pseudorandom bit - Method 1
    // This method is the more cumbersome one in terms of coding (see method 2 below)
    // but more readily lends itself to a shift register hardware implementation
    public int nextBitM1(){
        long newBit;

	    newBit =  ((this.seed & this.powTwo18) >> 17L) ^ ((this.seed & this.powTwo5) >> 4L) ^ ((this.seed & this.powTwo2) >> 1L) ^ (this.seed & this.powTwo1);
	    this.seed=(this.seed << 1L) | newBit;
	    return (int) newBit;
    }

    // Returns a pseudorandom bit - Method 2
    // This method is the more suited to software implementation (compare with method 1 above)
    // but lends itself less readily to a shift register hardware implementation
    public int nextBitM2(){
        int randomBit = 0;
        if((this.seed & this.powTwo18)<=0L){
            this.seed = ((this.seed ^ this.mask) << 1L) | this.powTwo1;
            randomBit = 1;
        }
        else{
            this.seed <<= 1L;
            randomBit = 0;
        }

	    return randomBit;
    }

    // Returns a Gaussian (normal) random deviate
    // mean  =  the mean, sd = standard deviation
    public double nextGaussian(double mean, double sd){
        double ran = 0.0D;
        if(this.methodOptionDecimal==1){
            ran=rr.nextGaussian();
            ran = ran*sd+mean;
        }
        else{
            ran=this.boxMullerParkMiller();
            ran = ran*sd+mean;
        }
        return ran;
    }


    // Returns an array of Gaussian (normal) random deviates
    // mean  =  the mean, sd = standard deviation, n = length of array
    public double[] gaussianArray(double mean, double sd, int n){
        double[] ran = new double[n];
        if(this.methodOptionDecimal==1){
            for(int i=0; i<n; i++){
                ran[i]=rr.nextGaussian();
                ran[i] = ran[i]*sd+mean;
            }
        }
        else{
            for(int i=0; i<n; i++){
                ran[i]=this.boxMullerParkMiller();
                ran[i] = ran[i]*sd+mean;
            }
        }
        return ran;
    }

    // Returns two arrays, both of length n, of correlated Gaussian (normal) random deviates
    // of means, mean1 and mean2, and standard deviations, sd1 and sd2,
    // and a correlation coefficient, rho
    public double[][] correlatedGaussianArrays(double mean1, double mean2, double sd1, double sd2, double rho, int n){
        if(Math.abs(rho)>1.0D)throw new IllegalArgumentException("The correlation coefficient, " + rho + ", must lie between -1 and 1");
        double[][] ran = new double[2][n];
        double ranh = 0.0D;
        double rhot = Math.sqrt(1.0D - rho*rho);
        if(this.methodOptionDecimal==1){
            for(int i=0; i<n; i++){
                ranh = rr.nextGaussian();
                ran[0][i] = ranh*sd1 + mean1;
                ran[1][i] = (rho*ranh + rhot*rr.nextGaussian())*sd2 + mean2;
            }
        }
        else{
            for(int i=0; i<n; i++){
                ranh=this.boxMullerParkMiller();
                ran[0][i] = ranh*sd1 + mean1;
                ran[1][i] = (rho*ranh + rhot*rr.nextGaussian())*sd2 + mean2;
            }
        }
        return ran;
    }

    // Box-Muller normal deviate generator
    //      after 	gasdev 	(Numerical Recipes in C - W.H. Press et al. (Cambridge)
    //		2nd edition 1992 p289
    // Uses Park and Miller method for generating pseudorandom numbers
    double boxMullerParkMiller(){
	    double fac = 0.0D, rsq = 0.0D, v1 = 0.0D, v2 = 0.0D;

	    if (iset==0){
		    do {
			    v1=2.0*parkMiller()-1.0D;
			    v2=2.0*parkMiller()-1.0D;
			    rsq=v1*v1+v2*v2;
		    }while (rsq >= 1.0D || rsq == 0.0D);
		    fac=Math.sqrt(-2.0D*Math.log(rsq)/rsq);
		    gset=v1*fac;
		    iset=1;
		    return v2*fac;
	    }else{
	    	iset=0;
		    return gset;
	    }
    }

    // Returns a Lorentzian pseudorandom deviate
    // mu  =  the mean, gamma = half-height width
    public double nextLorentzian (double mu, double gamma){
        double ran = 0.0D;
        if(this.methodOptionDecimal==1){
            ran = Math.tan((rr.nextDouble()-0.5)*Math.PI);
            ran = ran*gamma/2.0D+mu;
        }
        else{
            ran = Math.tan((this.nextDouble()-0.5)*Math.PI);
            ran = ran*gamma/2.0D+mu;
        }
        return ran;
    }


    // Returns an array of Lorentzian pseudorandom deviates
    // mu  =  the mean, gamma = half-height width, n = length of array
    public double[] lorentzianArray (double mu, double gamma, int n){
        double[] ran = new double[n];
        if(this.methodOptionDecimal==1){
            for(int i=0; i<n; i++){
                ran[i]=Math.tan((rr.nextDouble()-0.5)*Math.PI);
                ran[i] = ran[i]*gamma/2.0D+mu;
            }
        }
        else{
            for(int i=0; i<n; i++){
                ran[i]=Math.tan((this.nextDouble()-0.5)*Math.PI);
                ran[i] = ran[i]*gamma/2.0D+mu;
            }
        }
        return ran;
    }

    // Returns a Poissonian pseudorandom deviate
    // follows the approach of Numerical Recipes, 2nd Edition, p 294
    public double nextPoissonian(double mean){
        double ran = 0.0D;
        double oldm = -1.0D;
        double expt = 0.0D;
        double em = 0.0D;
        double term = 0.0D;
        double sq = 0.0D;
        double lnMean = 0.0D;
        double yDev = 0.0D;

        if(mean < 12.0D){
            if(mean != oldm){
                oldm = mean;
                expt = Math.exp(-mean);
            }
            em = -1.0D;
            term = 1.0D;
            do{
                ++em;
                term *= this.nextDouble();
            }while(term>expt);
            ran = em;
        }
        else{
            if(mean != oldm){
                oldm = mean;
                sq = Math.sqrt(2.0D*mean);
                lnMean = Math.log(mean);
                expt = lnMean - Stat.logGamma(mean+1.0D);
            }
            do{
                do{
                    yDev = Math.tan(Math.PI*this.nextDouble());
                    em = sq*yDev+mean;
                }while(em<0.0D);
                em = Math.floor(em);
                term = 0.9D*(1.0D+yDev*yDev)*Math.exp(em*lnMean - Stat.logGamma(em+1.0D)-expt);
            }while(rr.nextDouble()>term);
            ran = em;
        }
        return ran;
    }

    // Returns an array of Poisson random deviates
    // follows the approach of Numerical Recipes, 2nd Edition, p 294
    public double[] poissonianArray(double mean, int n){
        double[] ran = new double[n];
        double oldm = -1.0D;
        double expt = 0.0D;
        double em = 0.0D;
        double term = 0.0D;
        double sq = 0.0D;
        double lnMean = 0.0D;
        double yDev = 0.0D;

        if(mean < 12.0D){
            for(int i=0; i<n; i++){
                if(mean != oldm){
                    oldm = mean;
                    expt = Math.exp(-mean);
                }
                em = -1.0D;
                term = 1.0D;
                do{
                    ++em;
                    term *= this.nextDouble();
                }while(term>expt);
                ran[i] = em;
            }
        }
        else{
            for(int i=0; i<n; i++){
                if(mean != oldm){
                    oldm = mean;
                    sq = Math.sqrt(2.0D*mean);
                    lnMean = Math.log(mean);
                    expt = lnMean - Stat.logGamma(mean+1.0D);
                }
                do{
                    do{
                        yDev = Math.tan(Math.PI*this.nextDouble());
                        em = sq*yDev+mean;
                    }while(em<0.0D);
                    em = Math.floor(em);
                    term = 0.9D*(1.0D+yDev*yDev)*Math.exp(em*lnMean - Stat.logGamma(em+1.0D)-expt);
                }while(rr.nextDouble()>term);
                ran[i] = em;
            }
        }
        return ran;
    }

    // Returns a Pareto pseudorandom deviate
    public double nextPareto(double alpha, double beta){
        double ran = 0.0D;
        if(this.methodOptionDecimal==1){
            ran = Math.pow(1.0D-rr.nextDouble(), -1.0D/alpha)*beta;
        }
        else{
            ran = Math.pow(1.0D-this.nextDouble(), -1.0D/alpha)*beta;
        }
        return ran;
    }

    // Returns an array, of Pareto pseudorandom deviates, of length n
    public double[] paretoArray (double alpha, double beta, int n){
        double[] ran = new double[n];
        if(this.methodOptionDecimal==1){
            for(int i=0; i<n; i++){
                ran[i] = Math.pow(1.0D-rr.nextDouble(), -1.0D/alpha)*beta;
            }
        }
        else{
            for(int i=0; i<n; i++){
                ran[i] = Math.pow(1.0D-this.nextDouble(), -1.0D/alpha)*beta;
            }
        }
        return ran;
    }

    // Returns an exponential pseudorandom deviate
    public double nextExponential(double mu, double sigma){
        double ran = 0.0D;
        if(this.methodOptionDecimal==1){
            ran = mu - Math.log(1.0D-rr.nextDouble())*sigma;
        }
        else{
            ran = mu - Math.log(1.0D-this.nextDouble())*sigma;
        }
        return ran;
    }

    // Returns an array, of exponential pseudorandom deviates, of length n
    public double[] exponentialArray (double mu, double sigma, int n){
        double[] ran = new double[n];
        if(this.methodOptionDecimal==1){
            for(int i=0; i<n; i++){
                ran[i] = mu - Math.log(1.0D-rr.nextDouble())*sigma;
            }
        }
        else{
            for(int i=0; i<n; i++){
                ran[i] = mu - Math.log(1.0D-this.nextDouble())*sigma;
            }
        }
        return ran;
    }

   // Returns a Rayleigh pseudorandom deviate
   public double nextRayleigh(double sigma){
        double ran = 0.0D;
        if(this.methodOptionDecimal==1){
            ran = Math.sqrt(-2.0D*Math.log(1.0D-rr.nextDouble()))*sigma;
        }
        else{
            ran = Math.sqrt(-2.0D*Math.log(1.0D-this.nextDouble()))*sigma;
        }
        return ran;
    }

   // Returns an array, of Rayleigh pseudorandom deviates, of length n
   public double[] rayleighArray (double sigma, int n){
        double[] ran = new double[n];
        if(this.methodOptionDecimal==1){
            for(int i=0; i<n; i++){
                ran[i] = Math.sqrt(-2.0D*Math.log(1.0D-rr.nextDouble()))*sigma;
            }
        }
        else{
            for(int i=0; i<n; i++){
                ran[i] = Math.sqrt(-2.0D*Math.log(1.0D-this.nextDouble()))*sigma;
            }
        }
        return ran;
    }

    // Returns a minimal Gumbel (Type I EVD) random deviate
    // mu  =  location parameter, sigma = scale parameter
    public double nextMinimalGumbel(double mu, double sigma){
        double ran = 0.0D;
        if(this.methodOptionDecimal==1){
            ran = Math.log(Math.log(1.0D/(1.0D-rr.nextDouble())))*sigma+mu;
        }
        else{
            ran = Math.log(Math.log(1.0D/(1.0D-rr.nextDouble())))*sigma+mu;
        }
        return ran;
    }

    // Returns an array of minimal Gumbel (Type I EVD) random deviates
    // mu  =  location parameter, sigma = scale parameter, n = length of array
    public double[] minimalGumbelArray(double mu, double sigma,  int n){
        double[] ran = new double[n];
        if(this.methodOptionDecimal==1){
            for(int i=0; i<n; i++){
                ran[i] = Math.log(Math.log(1.0D/(1.0D-rr.nextDouble())))*sigma+mu;
            }
        }
        else{
            for(int i=0; i<n; i++){
                ran[i] = Math.log(Math.log(1.0D/(1.0D-this.nextDouble())))*sigma+mu;
            }
        }
        return ran;
    }

    // Returns a maximal Gumbel (Type I EVD) random deviate
    // mu  =  location parameter, sigma = scale parameter
    public double nextMaximalGumbel(double mu, double sigma){
        double ran = 0.0D;
        if(this.methodOptionDecimal==1){
            ran = mu-Math.log(Math.log(1.0D/(1.0D-rr.nextDouble())))*sigma;
        }
        else{
            ran = mu-Math.log(Math.log(1.0D/(1.0D-this.nextDouble())))*sigma;
        }
        return ran;
    }

    // Returns an array of maximal Gumbel (Type I EVD) random deviates
    // mu  =  location parameter, sigma = scale parameter, n = length of array
    public double[] maximalGumbelArray(double mu, double sigma,  int n){
        double[] ran = new double[n];
        if(this.methodOptionDecimal==1){
            for(int i=0; i<n; i++){
                ran[i] = mu-Math.log(Math.log(1.0D/(1.0D-rr.nextDouble())))*sigma;
            }
        }
        else{
            for(int i=0; i<n; i++){
                ran[i] = mu-Math.log(Math.log(1.0D/(1.0D-this.nextDouble())))*sigma;
            }
        }
        return ran;
    }

    // Returns a Frechet (Type II EVD) random deviate
    // mu  =  location parameter, sigma = scale parameter, gamma = shape parameter
    public double nextFrechet(double mu, double sigma, double gamma){
        double ran = 0.0D;
        if(this.methodOptionDecimal==1){
            ran = Math.pow((1.0D/(Math.log(1.0D/rr.nextDouble()))),1.0D/gamma)*sigma + mu;
        }
        else{
            ran = Math.pow((1.0D/(Math.log(1.0D/this.nextDouble()))),1.0D/gamma)*sigma + mu;
        }
        return ran;
    }

    // Returns an array of Frechet (Type II EVD) random deviates
    // mu  =  location parameter, sigma = scale parameter, gamma = shape parameter, n = length of array
    public double[] frechetArray(double mu, double sigma,  double gamma, int n){
        double[] ran = new double[n];
        if(this.methodOptionDecimal==1){
            for(int i=0; i<n; i++){
                ran[i] = Math.pow((1.0D/(Math.log(1.0D/rr.nextDouble()))),1.0D/gamma)*sigma + mu;
            }
        }
        else{
            for(int i=0; i<n; i++){
                ran[i] = Math.pow((1.0D/(Math.log(1.0D/this.nextDouble()))),1.0D/gamma)*sigma + mu;
            }
        }
        return ran;
    }

    // Returns a Weibull (Type III EVD) random deviate
    // mu  =  location parameter, sigma = scale parameter, gamma = shape parameter
    public double nextWeibull(double mu, double sigma, double gamma){
        double ran = 0.0D;
        if(this.methodOptionDecimal==1){
            ran = Math.pow(-Math.log(1.0D-rr.nextDouble()),1.0D/gamma)*sigma + mu;
        }
        else{
            ran = Math.pow(-Math.log(1.0D-this.nextDouble()),1.0D/gamma)*sigma + mu;
        }
        return ran;
    }

    // Returns an array of Weibull (Type III EVD)  random deviates
    // mu  =  location parameter, sigma = scale parameter, gamma = shape parameter, n = length of array
    public double[] weibullArray(double mu, double sigma,  double gamma, int n){
        double[] ran = new double[n];
        if(this.methodOptionDecimal==1){
            for(int i=0; i<n; i++){
                ran[i] = Math.pow(-Math.log(1.0D-rr.nextDouble()),1.0D/gamma)*sigma + mu;
            }
        }
        else{
            for(int i=0; i<n; i++){
                ran[i] = Math.pow(-Math.log(1.0D-this.nextDouble()),1.0D/gamma)*sigma + mu;
            }
        }
        return ran;
    }
}



