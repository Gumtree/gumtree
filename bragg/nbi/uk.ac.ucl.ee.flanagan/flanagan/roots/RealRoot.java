/*
*   Class RealRoot
*
*   Contains methods for finding a real root
*
*   The function whose root is to be determined is supplied
*   by means of an interface, RealRootFunction,
*   if no derivative required
*
*   The function whose root is to be determined is supplied
*   by means of an interface, RealRootDerivFunction,
*   as is the first derivative if a derivative is required
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:   18 May 2003
*   UPDATE: 22 June 2003, 10 April 2006
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   RealRoot.html
*
*   Copyright (c) June 2003    Michael Thomas Flanagan
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

package flanagan.roots;

import java.util.*;
import flanagan.math.Fmath;

// RealRoot class
public class RealRoot{

    private double root = Double.NaN;   // root to be found
    private double tol = 1e-9;          // tolerance in determining convergence upon a root
    private int iterMax = 3000;         // maximum number of iterations allowed in root search
    private int iterN = 0;              // number of iterations taken in root search
    private double upperBound = 0;      // upper bound for bisection and false position methods
    private double lowerBound = 0;      // lower bound for bisection and false position methods
    private double estimate = 0;        // estimate for Newton-Raphson method
    private int maximumBoundsExtension = 100;       // number of times that the bounds may be extended
                                                    // by the difference separating them if the root is
                                                    // found not to be bounded
    private boolean noBoundExtensions = false;      // = true if number of no extension to the  bounds allowed
    private boolean noLowerBoundExtensions = false; // = true if number of no extension to the lower bound allowed
    private boolean noUpperBoundExtensions = false; // = true if number of no extension to the upper bound allowed

    // Constructor
    public RealRoot(){
    }

    // INSTANCE METHODS

    // Set lower bound
    public void setLowerBound(double lower){
        this.lowerBound = lower;
    }

    // Set lower bound
    public void setUpperBound(double upper){
        this.upperBound = upper;
    }

    // Set estimate
    public void setEstimate(double estimate){
        this.estimate = estimate;
    }

    // Reset the default tolerance
    public void setTolerance(double tolerance){
        this.tol=tolerance;
    }

    // Get the default tolerance
    public double getTolerance(){
        return this.tol;
    }

    // Reset the maximum iterations allowed
    public void setIterMax(int imax){
        this.iterMax=imax;
    }

    // Get the maximum iterations allowed
    public int getIterMax(){
        return this.iterMax;
    }

    // Get the number of iterations taken
    public int getIterN(){
        return this.iterN;
    }

    // Reset the maximum number of bounds extensions
    public void setMaximumBoundsExtensions(int maximumBoundsExtension){
        this.maximumBoundsExtension=maximumBoundsExtension;
    }

    // Prevent extensions to the supplied bounds
    public void noBoundsExtensions(){
        this.noBoundExtensions = true;
        this.noLowerBoundExtensions = true;
        this.noUpperBoundExtensions = true;
    }

    // Prevent extension to the lower bound
    public void noLowerBoundExtension(){
        this.noLowerBoundExtensions = true;
        if(this.noUpperBoundExtensions)this.noBoundExtensions = true;
    }

    // Prevent extension to the upper bound
    public void noUpperBoundExtension(){
        this.noUpperBoundExtensions = true;
        if(this.noLowerBoundExtensions)this.noBoundExtensions = true;
    }

    // Combined bisection and Inverse Quadratic Interpolation method
    // bounds already entered
   	public double brent(RealRootFunction g){
        return this.brent(g, this.lowerBound, this.upperBound);
    }

    // Combined bisection and Inverse Quadratic Interpolation method
    // bounds supplied as arguments
   	public double brent(RealRootFunction g, double lower, double upper){
   	    this.lowerBound = lower;
   	    this.upperBound = upper;

	    // check upper>lower
	    if(upper==lower)throw new IllegalArgumentException("upper cannot equal lower");

        boolean testConv = true;    // convergence test: becomes false on convergence
        this.iterN = 0;
        double temp = 0.0D;

        if(upper<lower){
 	        temp = upper;
	        upper = lower;
	        lower = temp;
	    }

	    // calculate the function value at the estimate of the higher bound to x
	    double fu = g.function(upper);
	    // calculate the function value at the estimate of the lower bound of x
	    double fl = g.function(lower);
	    if(Fmath.isNaN(fl))throw new IllegalArgumentException("lower bound returned NaN as the function value");
	    if(Fmath.isNaN(fu))throw new IllegalArgumentException("upper bound returned NaN as the function value");

        // check that the root has been bounded and extend bounds if not and extension allowed
        boolean testBounds = true;
        int numberOfBoundsExtension = 0;
        double initialBoundsDifference = (upper - lower)/2.0D;
        while(testBounds){
            if(fu*fl<=0.0D){
                testBounds=false;
            }
            else{
                if(this.noBoundExtensions){
                    String message = "RealRoot.brent: root not bounded and no extension to bounds allowed\n";
                    message += "NaN returned";
                    System.out.println(message);
                    return Double.NaN;
                }
                else{
                    numberOfBoundsExtension++;
                    if(numberOfBoundsExtension>this.maximumBoundsExtension){
                        String message = "RealRoot.brent: root not bounded and maximum number of extension to bounds allowed, " + this.maximumBoundsExtension + ", exceeded\n";
                        message += "NaN returned";
                        System.out.println(message);
                        return Double.NaN;
                    }
                    if(!this.noLowerBoundExtensions){
                        lower -= initialBoundsDifference;
                        fl = g.function(lower);
                    }
                    if(!this.noUpperBoundExtensions){
                        upper += initialBoundsDifference;
                        fu = g.function(upper);
                    }
                }
            }
        }

	    // check initial values for true root value
	    if(fl==0.0D){
	        this.root=lower;
	        testConv = false;
	    }
	    if(fu==0.0D){
	        this.root=upper;
	        testConv = false;
	    }

	    // Function at mid-point of initial estimates
        double mid=(lower+upper)/2.0D;   // mid point (bisect) or new x estimate (Inverse Quadratic Interpolation)
        double lastMidB = mid;           // last succesful mid point
        double fm = g.function(mid);
        double diff = mid-lower; // difference between successive estimates of the root
        double fmB = fm;        // last succesful mid value function value
        double lastMid=mid;
        boolean lastMethod = true; // true; last method = Inverse Quadratic Interpolation, false; last method = bisection method
        boolean nextMethod = true; // true; next method = Inverse Quadratic Interpolation, false; next method = bisection method

	    // search
	    double rr=0.0D, ss=0.0D, tt=0.0D, pp=0.0D, qq=0.0D; // interpolation variables
	    while(testConv){
	        // test for convergence
	        if(fm==0.0D || Math.abs(diff)<this.tol){
	            testConv=false;
	            if(fm==0.0D){
	                this.root=lastMid;
	            }
	            else{
	                if(Math.abs(diff)<this.tol)this.root=mid;
	            }
	        }
	        else{
	            lastMethod=nextMethod;
	            // test for succesfull inverse quadratic interpolation
	            if(lastMethod){
	                if(mid<lower || mid>upper){
	                    // inverse quadratic interpolation failed
	                    nextMethod=false;
	                }
	                else{
	                    fmB=fm;
	                    lastMidB=mid;
	                }
	            }
	            else{
	                nextMethod=true;
	            }
		        if(nextMethod){
		            // inverse quadratic interpolation
		            fl=g.function(lower);
	                fm=g.function(mid);
	                fu=g.function(upper);
	                rr=fm/fu;
	                ss=fm/fl;
	                tt=fl/fu;
	                pp=ss*(tt*(rr-tt)*(upper-mid)-(1.0D-rr)*(mid-lower));
	                qq=(tt-1.0D)*(rr-1.0D)*(ss-1.0D);
	                lastMid=mid;
	                diff=pp/qq;
	                mid=mid+diff;
	            }
	            else{
	                // Bisection procedure
	                fm=fmB;
	                mid=lastMidB;
	                if(fm*fl>0.0D){
	                    lower=mid;
	                    fl=fm;
	                }
	                else{
	                    upper=mid;
	                    fu=fm;
	                }
	                lastMid=mid;
	                mid=(lower+upper)/2.0D;
	                fm=g.function(mid);
	                diff=mid-lastMid;
	                fmB=fm;
	                lastMidB=mid;
	            }
	        }
            this.iterN++;
            if(this.iterN>this.iterMax){
                System.out.println("this.brent: maximum number of iterations exceeded - root at this point returned");
                System.out.println("Last mid-point difference = "+diff+", tolerance = " + this.tol);
                this.root = mid;
                testConv = false;
            }
        }
        return this.root;
    }

    // bisection method
    // bounds already entered
	public double bisect(RealRootFunction g){
	    return this.bisect(g, this.lowerBound, this.upperBound);
	}

    // bisection method
	public double bisect(RealRootFunction g, double lower, double upper){
   	    this.lowerBound = lower;
   	    this.upperBound = upper;

	    // check upper>lower
	    if(upper==lower)throw new IllegalArgumentException("upper cannot equal lower");
	    if(upper<lower){
            double temp = upper;
	        upper = lower;
	        lower = temp;
	    }

        boolean testConv = true;    // convergence test: becomes false on convergence
        this.iterN = 0;             // number of iterations
        double diff = 1e300;        // abs(difference between the last two successive mid-pint x values)

	    // calculate the function value at the estimate of the higher bound to x
	    double fu = g.function(upper);
	    // calculate the function value at the estimate of the lower bound of x
	    double fl = g.function(lower);
	    if(Fmath.isNaN(fl))throw new IllegalArgumentException("lower bound returned NaN as the function value");
	    if(Fmath.isNaN(fu))throw new IllegalArgumentException("upper bound returned NaN as the function value");

        // check that the root has been bounded and extend bounds if not and extension allowed
        boolean testBounds = true;
        int numberOfBoundsExtension = 0;
        double initialBoundsDifference = (upper - lower)/2.0D;
        while(testBounds){
            if(fu*fl<=0.0D){
                testBounds=false;
            }
            else{
                if(this.noBoundExtensions){
                    String message = "RealRoot.bisect: root not bounded and no extension to bounds allowed\n";
                    message += "NaN returned";
                    System.out.println(message);
                    return Double.NaN;

                }
                else{
                    numberOfBoundsExtension++;
                    if(numberOfBoundsExtension>this.maximumBoundsExtension){
                        String message = "RealRoot.bisect: root not bounded and maximum number of extension to bounds allowed, " + this.maximumBoundsExtension + ", exceeded\n";
                        message += "NaN returned";
                        System.out.println(message);
                        return Double.NaN;
                    }
                    if(!this.noLowerBoundExtensions){
                        lower -= initialBoundsDifference;
                        fl = g.function(lower);
                    }
                    if(!this.noUpperBoundExtensions){
                        upper += initialBoundsDifference;
                        fu = g.function(upper);
                    }
                }
            }
        }

	    // check initial values for true root value
	    if(fl==0.0D){
	        this.root=lower;
	        testConv = false;
	    }
	    if(fu==0.0D){
	        this.root=upper;
	        testConv = false;
	    }

	    // start search
        double mid = (lower+upper)/2.0D;    // mid-point
        double lastMid = 1e300;             // previous mid-point
        double fm = g.function(mid);
        while(testConv){
            if(fm==0.0D || diff<this.tol){
                testConv=false;
                this.root=mid;
            }
            if(fm*fl>0.0D){
                lower = mid;
                fl=fm;
            }
            else{
                upper = mid;
                fu=fm;
            }
            lastMid = mid;
            mid = (lower+upper)/2.0D;
            fm = g.function(mid);
            diff = Math.abs(mid-lastMid);
            this.iterN++;
            if(this.iterN>this.iterMax){
                System.out.println("this.bisect: maximum number of iterations exceeded - root at this point returned");
                System.out.println("Last mid-point difference = "+diff+", tolerance = " + this.tol);
                this.root = mid;
                testConv = false;
            }
        }
        return this.root;
    }

    // false position  method
    // bounds already entered
	public double falsePosition(RealRootFunction g){
	    return this.falsePosition(g, this.lowerBound, this.upperBound);
    }

    // false position  method
	public double falsePosition(RealRootFunction g, double lower, double upper){
	    this.lowerBound = lower;
	    this.upperBound = upper;

	    // check upper>lower
	    if(upper==lower)throw new IllegalArgumentException("upper cannot equal lower");
	    if(upper<lower){
 	        double temp = upper;
	        upper = lower;
	        lower = temp;
	    }

        boolean testConv = true;    // convergence test: becomes false on convergence
        this.iterN = 0;             // number of iterations
        double diff = 1e300;   // abs(difference between the last two successive mid-pint x values)

	    // calculate the function value at the estimate of the higher bound to x
	    double fu = g.function(upper);
	    // calculate the function value at the estimate of the lower bound of x
	    double fl = g.function(lower);
	    if(Fmath.isNaN(fl))throw new IllegalArgumentException("lower bound returned NaN as the function value");
	    if(Fmath.isNaN(fu))throw new IllegalArgumentException("upper bound returned NaN as the function value");

        // check that the root has been bounded and extend bounds if not and extension allowed
        boolean testBounds = true;
        int numberOfBoundsExtension = 0;
        double initialBoundsDifference = (upper - lower)/2.0D;
        while(testBounds){
            if(fu*fl<=0.0D){
                testBounds=false;
            }
            else{
                if(this.noBoundExtensions){
                    String message = "RealRoot.falsePosition: root not bounded and no extension to bounds allowed\n";
                    message += "NaN returned";
                    System.out.println(message);
                    return Double.NaN;
                }
                else{
                    numberOfBoundsExtension++;
                    if(numberOfBoundsExtension>this.maximumBoundsExtension){
                        String message = "RealRoot.falsePosition: root not bounded and maximum number of extension to bounds allowed, " + this.maximumBoundsExtension + ", exceeded\n";
                        message += "NaN returned";
                        System.out.println(message);
                        return Double.NaN;
                    }
                    if(!this.noLowerBoundExtensions){
                        lower -= initialBoundsDifference;
                        fl = g.function(lower);
                    }
                    if(!this.noUpperBoundExtensions){
                        upper += initialBoundsDifference;
                        fu = g.function(upper);
                    }
                }
            }
        }

	    // check initial values for true root value
	    if(fl==0.0D){
	        this.root=lower;
	        testConv = false;
	    }
	    if(fu==0.0D){
	        this.root=upper;
	        testConv = false;
	    }

	    // start search
        double mid = lower+(upper-lower)*Math.abs(fl)/(Math.abs(fl)+Math.abs(fu));    // mid-point
        double lastMid = 1e300;             // previous mid-point
        double fm = g.function(mid);
        while(testConv){
            if(fm==0.0D || diff<this.tol){
                testConv=false;
                this.root=mid;
            }
            if(fm*fl>0.0D){
                lower = mid;
                fl=fm;
            }
            else{
                upper = mid;
                fu=fm;
            }
            lastMid = mid;
            mid = lower+(upper-lower)*Math.abs(fl)/(Math.abs(fl)+Math.abs(fu));    // mid-point
            fm = g.function(mid);
            diff = Math.abs(mid-lastMid);
            this.iterN++;
            if(this.iterN>this.iterMax){
                System.out.println("this.falsePosition: maximum number of iterations exceeded - root at this point returned");
                System.out.println("Last mid-point difference = "+diff+", tolerance = " + this.tol);
                this.root = mid;
                testConv = false;
            }
        }
        return this.root;
    }

    // Combined bisection and Newton Raphson method
    // bounds already entered
   	public double bisectNewtonRaphson(RealRootDerivFunction g){
   	    return this.bisectNewtonRaphson(g, this.lowerBound, this.upperBound);
   	}

    // Combined bisection and Newton Raphson method
    // default accuracy used
   	public double bisectNewtonRaphson(RealRootDerivFunction g, double lower, double upper){
	    this.lowerBound = lower;
	    this.upperBound = upper;

	    // check upper>lower
	    if(upper==lower)throw new IllegalArgumentException("upper cannot equal lower");

        boolean testConv = true;    // convergence test: becomes false on convergence
        this.iterN = 0;         // number of iterations
        double temp = 0.0D;

        if(upper<lower){
 	        temp = upper;
	        upper = lower;
	        lower = temp;
	    }

	    // calculate the function value at the estimate of the higher bound to x
	    double[] f = g.function(upper);
	    double fu=f[0];
	    // calculate the function value at the estimate of the lower bound of x
	    f = g.function(lower);
	    double fl=f[0];
	    if(Fmath.isNaN(fl))throw new IllegalArgumentException("lower bound returned NaN as the function value");
	    if(Fmath.isNaN(fu))throw new IllegalArgumentException("upper bound returned NaN as the function value");

        // check that the root has been bounded and extend bounds if not and extension allowed
        boolean testBounds = true;
        int numberOfBoundsExtension = 0;
        double initialBoundsDifference = (upper - lower)/2.0D;
        while(testBounds){
            if(fu*fl<=0.0D){
                testBounds=false;
            }
            else{
                if(this.noBoundExtensions){
                    String message = "RealRoot.bisectNewtonRaphson: root not bounded and no extension to bounds allowed\n";
                    message += "NaN returned";
                    System.out.println(message);
                    return Double.NaN;
                }
                else{
                    numberOfBoundsExtension++;
                    if(numberOfBoundsExtension>this.maximumBoundsExtension){
                        String message = "RealRoot.bisectNewtonRaphson: root not bounded and maximum number of extension to bounds allowed, " + this.maximumBoundsExtension + ", exceeded\n";
                        message += "NaN returned";
                        System.out.println(message);
                        return Double.NaN;
                    }
                    if(!this.noLowerBoundExtensions){
                        lower -= initialBoundsDifference;
                        f = g.function(lower);
                        fl = f[0];
                    }
                    if(!this.noUpperBoundExtensions){
                        upper += initialBoundsDifference;
                        f = g.function(upper);
                        fu = f[0];
                    }
                }
            }
        }

	    // check initial values for true root value
	    if(fl==0.0D){
	        this.root=lower;
	        testConv = false;
	    }
	    if(fu==0.0D){
	        this.root=upper;
	        testConv = false;
	    }

	    // Function at mid-point of initial estimates
        double mid=(lower+upper)/2.0D;   // mid point (bisect) or new x estimate (Newton-Raphson)
        double lastMidB = mid;           // last succesful mid point
        f = g.function(mid);
        double diff = f[0]/f[1]; // difference between successive estimates of the root
        double fm = f[0];
        double fmB = fm;        // last succesful mid value function value
        double lastMid=mid;
        mid = mid-diff;
        boolean lastMethod = true; // true; last method = Newton Raphson, false; last method = bisection method
        boolean nextMethod = true; // true; next method = Newton Raphson, false; next method = bisection method

	    // search
	    while(testConv){
	        // test for convergence
	        if(fm==0.0D || Math.abs(diff)<this.tol){
	            testConv=false;
	            if(fm==0.0D){
	                this.root=lastMid;
	            }
	            else{
	                if(Math.abs(diff)<this.tol)this.root=mid;
	            }
	        }
	        else{
	            lastMethod=nextMethod;
	            // test for succesfull Newton-Raphson
	            if(lastMethod){
	                if(mid<lower || mid>upper){
	                    // Newton Raphson failed
	                    nextMethod=false;
	                }
	                else{
	                    fmB=fm;
	                    lastMidB=mid;
	                }
	            }
	            else{
	                nextMethod=true;
	            }
		        if(nextMethod){
		            // Newton-Raphson procedure
	                f=g.function(mid);
	                fm=f[0];
	                diff=f[0]/f[1];
	                lastMid=mid;
	                mid=mid-diff;
	            }
	            else{
	                // Bisection procedure
	                fm=fmB;
	                mid=lastMidB;
	                if(fm*fl>0.0D){
	                    lower=mid;
	                    fl=fm;
	                }
	                else{
	                    upper=mid;
	                    fu=fm;
	                }
	                lastMid=mid;
	                mid=(lower+upper)/2.0D;
	                f=g.function(mid);
	                fm=f[0];
	                diff=mid-lastMid;
	                fmB=fm;
	                lastMidB=mid;
	            }
	        }
            this.iterN++;
            if(this.iterN>this.iterMax){
                System.out.println("this.bisectNetonRaphson: maximum number of iterations exceeded - root at this point returned");
                System.out.println("Last mid-point difference = "+diff+", tolerance = " + this.tol);
                this.root = mid;
                testConv = false;
            }
        }
        return this.root;
    }

    // Newton Raphson method
    // estimate already entered
	public double newtonRaphson(RealRootDerivFunction g){
	    return this.newtonRaphson(g, this.estimate);

	}

    // Newton Raphson method
	public double newtonRaphson(RealRootDerivFunction g, double x){
	    this.estimate = x;
        boolean testConv = true;    // convergence test: becomes false on convergence
        this.iterN = 0;             // number of iterations
        double diff = 1e300;   // difference between the last two successive mid-pint x values

	    // calculate the function and derivative value at the initial estimate  x
	    double[] f = g.function(x);
	    if(Fmath.isNaN(f[0]))throw new IllegalArgumentException("NaN returned as the function value");
	    if(Fmath.isNaN(f[1]))throw new IllegalArgumentException("NaN returned as the derivative function value");


	    // search
        while(testConv){
            diff = f[0]/f[1];
            if(f[0]==0.0D || Math.abs(diff)<this.tol){
                this.root = x;
                testConv=false;
            }
            else{
                x -= diff;
                f = g.function(x);
	            if(Fmath.isNaN(f[0]))throw new IllegalArgumentException("NaN returned as the function value");
	            if(Fmath.isNaN(f[1]))throw new IllegalArgumentException("NaN returned as the derivative function value");
            }
            this.iterN++;
            if(this.iterN>this.iterMax){
                System.out.println("this.newtonRaphson: maximum number of iterations exceeded - root at this point returned");
                System.out.println("Last mid-point difference = "+diff+", tolerance = " + this.tol);
                this.root = x;
                testConv = false;
            }
        }
        return this.root;
    }

    // STATIC METHODS

   	// Combined bisection and Inverse Quadratic Interpolation method
    // default tolerance used
   	public static double brent(RealRootFunction g, double lower, double upper, double tol){
	    // check upper>lower
	    if(upper==lower)throw new IllegalArgumentException("upper cannot equal lower");

	    double root = Double.NaN;   // variable to hold the returned root
        boolean testConv = true;    // convergence test: becomes false on convergence
        int iterN = 0;
        int iterMax = 1000;
        double temp = 0.0D;
        if(upper<lower){
 	        temp = upper;
	        upper = lower;
	        lower = temp;
	    }

	    // calculate the function value at the estimate of the higher bound to x
	    double fu = g.function(upper);
	    // calculate the function value at the estimate of the lower bound of x
	    double fl = g.function(lower);
	    if(Fmath.isNaN(fl))throw new IllegalArgumentException("lower bound returned NaN as the function value");
	    if(Fmath.isNaN(fu))throw new IllegalArgumentException("upper bound returned NaN as the function value");

        // check that the root has been bounded
        if(fu*fl<=0.0D){
            String message = "RealRoot.brent: root not bounded and no extension to bounds allowed\n";
            message += "NaN returned";
            System.out.println(message);
            return Double.NaN;
        }
	    // check initial values for true root value
	    if(fl==0.0D){
	        root=lower;
	        testConv = false;
	    }
	    if(fu==0.0D){
	        root=upper;
	        testConv = false;
	    }

	    // Function at mid-point of initial estimates
        double mid=(lower+upper)/2.0D;   // mid point (bisect) or new x estimate (Newton-Raphson)
        double lastMidB = mid;           // last succesful mid point
        double fm = g.function(mid);
        double diff = mid-lower; // difference between successive estimates of the root
        double fmB = fm;        // last succesful mid value function value
        double lastMid=mid;
        boolean lastMethod = true; // true; last method = Newton Raphson, false; last method = bisection method
        boolean nextMethod = true; // true; next method = Newton Raphson, false; next method = bisection method

	    // search
	    double rr=0.0D, ss=0.0D, tt=0.0D, pp=0.0D, qq=0.0D; // interpolation variables
	    while(testConv){
	        // test for convergence
	        if(fm==0.0D || Math.abs(diff)<tol){
	            testConv=false;
	            if(fm==0.0D){
	                root=lastMid;
	            }
	            else{
	                if(Math.abs(diff)<tol)root=mid;
	            }
	        }
	        else{
	            lastMethod=nextMethod;
	            // test for succesfull inverse quadratic interpolation
	            if(lastMethod){
	                if(mid<lower || mid>upper){
	                    // inverse quadratic interpolation failed
	                    nextMethod=false;
	                }
	                else{
	                    fmB=fm;
	                    lastMidB=mid;
	                }
	            }
	            else{
	                nextMethod=true;
	            }
		        if(nextMethod){
		            // inverse quadratic interpolation
		            fl=g.function(lower);
	                fm=g.function(mid);
	                fu=g.function(upper);
	                rr=fm/fu;
	                ss=fm/fl;
	                tt=fl/fu;
	                pp=ss*(tt*(rr-tt)*(upper-mid)-(1.0D-rr)*(mid-lower));
	                qq=(tt-1.0D)*(rr-1.0D)*(ss-1.0D);
	                lastMid=mid;
	                diff=pp/qq;
	                mid=mid+diff;
	            }
	            else{
	                // Bisection procedure
	                fm=fmB;
	                mid=lastMidB;
	                if(fm*fl>0.0D){
	                    lower=mid;
	                    fl=fm;
	                }
	                else{
	                    upper=mid;
	                    fu=fm;
	                }
	                lastMid=mid;
	                mid=(lower+upper)/2.0D;
	                fm=g.function(mid);
	                diff=mid-lastMid;
	                fmB=fm;
	                lastMidB=mid;
	            }
	        }
            iterN++;
            if(iterN>iterMax){
                System.out.println("RealRoot.brent: maximum number of iterations exceeded - root at this point returned");
                System.out.println("Last mid-point difference = "+diff+", tolerance = " + tol);
                root = mid;
                testConv = false;
            }
        }
        return root;
    }



    // bisection method
    // tolerance supplied
	public static double bisect(RealRootFunction g, double lower, double upper, double tol){
	    // check upper>lower
	    if(upper==lower)throw new IllegalArgumentException("upper cannot equal lower");
	    if(upper<lower){
            double temp = upper;
	        upper = lower;
	        lower = temp;
	    }

	    double root = Double.NaN;   // variable to hold the returned root
        boolean testConv = true;    // convergence test: becomes false on convergence
        int iterN = 0;              // number of iterations
        int iterMax = 1000;         // maximum number of iterations
        double diff = 1e300;        // abs(difference between the last two successive mid-pint x values)

	    // calculate the function value at the estimate of the higher bound to x
	    double fu = g.function(upper);
	    // calculate the function value at the estimate of the lower bound of x
	    double fl = g.function(lower);
	    if(Fmath.isNaN(fl))throw new IllegalArgumentException("lower bound returned NaN as the function value");
	    if(Fmath.isNaN(fu))throw new IllegalArgumentException("upper bound returned NaN as the function value");

        // check that the root has been bounded
        if(fu*fl<=0.0D){
            String message = "RealRoot.bisect: root not bounded and no extension to bounds allowed\n";
            message += "NaN returned";
            System.out.println(message);
            return Double.NaN;
        }
	    // check initial values for true root value
	    if(fl==0.0D){
	        root=lower;
	        testConv = false;
	    }
	    if(fu==0.0D){
	        root=upper;
	        testConv = false;
	    }

	    // start search
        double mid = (lower+upper)/2.0D;    // mid-point
        double lastMid = 1e300;             // previous mid-point
        double fm = g.function(mid);
        while(testConv){
            if(fm==0.0D || diff<tol){
                testConv=false;
                root=mid;
            }
            if(fm*fl>0.0D){
                lower = mid;
                fl=fm;
            }
            else{
                upper = mid;
                fu=fm;
            }
            lastMid = mid;
            mid = (lower+upper)/2.0D;
            fm = g.function(mid);
            diff = Math.abs(mid-lastMid);
            iterN++;
            if(iterN>iterMax){
                System.out.println("RealRoot.bisect: maximum number of iterations exceeded - root at this point returned");
                System.out.println("Last mid-point difference = "+diff+", tolerance = " + tol);
                root = mid;
                testConv = false;
            }
        }
        return root;
    }



    // false position  method
    // tolerance supplied
	public static double falsePosition(RealRootFunction g, double lower, double upper, double tol){
	    // check upper>lower
	    if(upper==lower)throw new IllegalArgumentException("upper cannot equal lower");
	    if(upper<lower){
 	        double temp = upper;
	        upper = lower;
	        lower = temp;
	    }

	    double root = Double.NaN;   // variable to hold the returned root
        boolean testConv = true;    // convergence test: becomes false on convergence
        int iterN = 0;              // number of iterations
        double diff = 1e250;        // abs(difference between the last two successive mid-pint x values)
        int iterMax = 1000;         // maximum number of iterations

	    // calculate the function value at the estimate of the higher bound to x
	    double fu = g.function(upper);
	    // calculate the function value at the estimate of the lower bound of x
	    double fl = g.function(lower);
	    if(Fmath.isNaN(fl))throw new IllegalArgumentException("lower bound returned NaN as the function value");
	    if(Fmath.isNaN(fu))throw new IllegalArgumentException("upper bound returned NaN as the function value");

        // check that the root has been bounded
        if(fu*fl<=0.0D){
            String message = "RealRoot.falsePosition: root not bounded and no extension to bounds allowed\n";
            message += "NaN returned";
            System.out.println(message);
            return Double.NaN;
        }
	    // check initial values for true root value
	    if(fl==0.0D){
	        root=lower;
	        testConv = false;
	    }
	    if(fu==0.0D){
	        root=upper;
	        testConv = false;
	    }

	    // start search
        double mid = lower+(upper-lower)*Math.abs(fl)/(Math.abs(fl)+Math.abs(fu));    // mid-point
        double lastMid = 1e300;             // previous mid-point
        double fm = g.function(mid);
        while(testConv){
            if(fm==0.0D || diff<tol){
                testConv=false;
                root=mid;
            }
            if(fm*fl>0.0D){
                lower = mid;
                fl=fm;
            }
            else{
                upper = mid;
                fu=fm;
            }
            lastMid = mid;
            mid = lower+(upper-lower)*Math.abs(fl)/(Math.abs(fl)+Math.abs(fu));    // mid-point
            fm = g.function(mid);
            diff = Math.abs(mid-lastMid);
            iterN++;
            if(iterN>iterMax){
                System.out.println("RealRoot.falsePosition: maximum number of iterations exceeded - root at this point returned");
                System.out.println("Last mid-point difference = "+diff+", tolerance = " + tol);
                root = mid;
                testConv = false;
            }
        }
        return root;
    }


    // Combined bisection and Newton Raphson method
    // tolerance supplied
   	public static double bisectNewtonRaphson(RealRootDerivFunction g, double lower, double upper, double tol){

	    // check upper>lower
	    if(upper==lower)throw new IllegalArgumentException("upper cannot equal lower");

	    double root = Double.NaN;   // variable to hold the returned root
        boolean testConv = true;    // convergence test: becomes false on convergence
        int iterN = 0;              // number of iterations
        int iterMax = 1000;         // maximum number of iterations
        double temp = 0.0D;

        if(upper<lower){
 	        temp = upper;
	        upper = lower;
	        lower = temp;
	    }

	    // calculate the function value at the estimate of the higher bound to x
	    double[] f = g.function(upper);
	    double fu=f[0];
	    // calculate the function value at the estimate of the lower bound of x
	    f = g.function(lower);
	    double fl=f[0];
	    if(Fmath.isNaN(fl))throw new IllegalArgumentException("lower bound returned NaN as the function value");
	    if(Fmath.isNaN(fu))throw new IllegalArgumentException("upper bound returned NaN as the function value");

        // check that the root has been bounded
        if(fu*fl<=0.0D){
            String message = "RealRoot.bisectNewtonRaphson: root not bounded and no extension to bounds allowed\n";
            message += "NaN returned";
            System.out.println(message);
            return Double.NaN;
        }
	    // check initial values for true root value
	    if(fl==0.0D){
	        root=lower;
	        testConv = false;
	    }
	    if(fu==0.0D){
	        root=upper;
	        testConv = false;
	    }

	    // Function at mid-point of initial estimates
        double mid=(lower+upper)/2.0D;   // mid point (bisect) or new x estimate (Newton-Raphson)
        double lastMidB = mid;           // last succesful mid point
        f = g.function(mid);
        double diff = f[0]/f[1]; // difference between successive estimates of the root
        double fm = f[0];
        double fmB = fm;        // last succesful mid value function value
        double lastMid=mid;
        mid = mid-diff;
        boolean lastMethod = true; // true; last method = Newton Raphson, false; last method = bisection method
        boolean nextMethod = true; // true; next method = Newton Raphson, false; next method = bisection method

	    // search
	    while(testConv){
	        // test for convergence
	        if(fm==0.0D || Math.abs(diff)<tol){
	            testConv=false;
	            if(fm==0.0D){
	                root=lastMid;
	            }
	            else{
	                if(Math.abs(diff)<tol)root=mid;
	            }
	        }
	        else{
	            lastMethod=nextMethod;
	            // test for succesfull Newton-Raphson
	            if(lastMethod){
	                if(mid<lower || mid>upper){
	                    // Newton Raphson failed
	                    nextMethod=false;
	                }
	                else{
	                    fmB=fm;
	                    lastMidB=mid;
	                }
	            }
	            else{
	                nextMethod=true;
	            }
		        if(nextMethod){
		            // Newton-Raphson procedure
	                f=g.function(mid);
	                fm=f[0];
	                diff=f[0]/f[1];
	                lastMid=mid;
	                mid=mid-diff;
	            }
	            else{
	                // Bisection procedure
	                fm=fmB;
	                mid=lastMidB;
	                if(fm*fl>0.0D){
	                    lower=mid;
	                    fl=fm;
	                }
	                else{
	                    upper=mid;
	                    fu=fm;
	                }
	                lastMid=mid;
	                mid=(lower+upper)/2.0D;
	                f=g.function(mid);
	                fm=f[0];
	                diff=mid-lastMid;
	                fmB=fm;
	                lastMidB=mid;
	            }
	        }
            iterN++;
            if(iterN>iterMax){
                System.out.println("RealRoot.bisectNetonRaphson: maximum number of iterations exceeded - root at this point returned");
                System.out.println("Last mid-point difference = "+diff+", tolerance = " + tol);
                root = mid;
                testConv = false;
            }
        }
        return root;
    }


    // Newton Raphson method
    // tolerance supplied
	public static double newtonRaphson(RealRootDerivFunction g, double x, double tol){
	    double root = Double.NaN;   // variable to hold the returned root
        boolean testConv = true;    // convergence test: becomes false on convergence
        int iterN = 0;              // number of iterations
        int iterMax = 1000;         // maximum number of iterations
        double diff = 1e250;        // difference between the last two successive mid-pint x values

	    // calculate the function and derivative value at the initial estimate  x
	    double[] f = g.function(x);
	    if(Fmath.isNaN(f[0]))throw new IllegalArgumentException("NaN returned as the function value");
	    if(Fmath.isNaN(f[1]))throw new IllegalArgumentException("NaN returned as the derivative function value");

	    // search
        while(testConv){
            diff = f[0]/f[1];
            if(f[0]==0.0D || Math.abs(diff)<tol){
                root = x;
                testConv=false;
            }
            else{
                x -= diff;
                f = g.function(x);
	            if(Fmath.isNaN(f[0]))throw new IllegalArgumentException("NaN returned as the function value");
	            if(Fmath.isNaN(f[1]))throw new IllegalArgumentException("NaN returned as the derivative function value");
            }
            iterN++;
            if(iterN>iterMax){
                System.out.println("RealRoot.newtonRaphson: maximum number of iterations exceeded - root at this point returned");
                System.out.println("Last mid-point difference = "+diff+", tolerance = " + tol);
                root = x;
                testConv = false;
            }
        }
        return root;
    }
}

