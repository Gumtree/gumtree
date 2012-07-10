/*
*   Class   Stat
*
*   USAGE:  Statistical functions
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:    June 2002 as part of Fmath
*   AMENDED: 12 May 2003 Statistics separated out from Fmath as a new class
*   UPDATE:  18 June 2005, 5 January 2006, 25 April 2006
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   Stat.html
*
*   Copyright (c) April 2004, 18 June 2005, 5 January 2006
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
import flanagan.math.Fmath;
import flanagan.integration.Integration;
import flanagan.integration.IntegralFunction;
import flanagan.plot.PlotGraph;
import flanagan.complex.*;


// Class to evaluate the linear correlation coefficient probablity function
// Needed in calls to Integration.gaussQuad
class CorrCoeff implements IntegralFunction{

         public double a;

         public double function(double x){
                  double y = Math.pow((1.0D - x*x),a);
                  return y;
         }
}

//Statical methods class Stat
public class Stat{


        // A small number close to the smallest representable floating point number
        public static final double FPMIN = 1e-300;

        // PRIVATE MEMBERS FOR USE IN FOLLOWING PUBLIC METHODS
        // GAMMA FUNCTIONS
        //  Lanczos Gamma Function approximation - N (number of coefficients -1)
        private static int lgfN = 6;
        //  Lanczos Gamma Function approximation - Coefficients
        private static double[] lgfCoeff = {1.000000000190015, 76.18009172947146, -86.50532032941677, 24.01409824083091, -1.231739572450155, 0.1208650973866179E-2, -0.5395239384953E-5};
        //  Lanczos Gamma Function approximation - small gamma
        private static double lgfGamma = 5.0;
        //  Maximum number of iterations allowed in Incomplete Gamma Function calculations
        private static int igfiter = 1000;
        //  Tolerance used in terminating series in Incomplete Gamma Function calculations
        private static double igfeps = 1e-8;

        // METHODS

        // Arithmetic mean of a 1D array of doubles, aa
        public static double mean(double[] aa){
                int n = aa.length;
                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=aa[i];
                }
                return sum/((double)n);
        }

        // Weighted arithmetic mean of a 1D array of doubles, aa
        public static double mean(double[] aa, double[] ww){
                int n = aa.length;
                if(n!=ww.length)throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
                double sumx=0.0D;
                double sumw=0.0D;
                double weight = 0.0D;
                for(int i=0; i<n; i++){
                    weight = 1.0D/(ww[i]*ww[i]);
                    sumx+=aa[i]*weight;
                    sumw+=weight;
                }
                return sumx/sumw;
        }

        // Weighted arithmetic mean of a 1D array of floats, aa
        public static double mean(float[] aa, float[] ww){
                int n = aa.length;
                if(n!=ww.length)throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
                float sumx=0.0F;
                float sumw=0.0F;
                float weight = 0.0F;
                for(int i=0; i<n; i++){
                    weight = 1.0F/(ww[i]*ww[i]);
                    sumx+=aa[i]*weight;
                    sumw+=weight;
                }
                return sumx/sumw;
        }

        // Geometric mean of a 1D array of doubles, aa
        public static double geometricMean(double[] aa){
                int n = aa.length;
                double product=1.0D;
                for(int i=0; i<n; i++)product *= Math.pow(aa[i], 1.0D/((double)n));
                return product;
        }

        // Geometric mean of a 1D array of floats, aa
        public static float geometricMean(float[] aa){
                int n = aa.length;
                float product=1.0F;
                for(int i=0; i<n; i++)product *= (float)Math.pow(aa[i], 1.0F/((float)n));
                return product;
        }

        // Weighted geometric mean of a 1D array of doubles, aa
        public static double geometricMean(double[] aa, double[] ww){
                int n = aa.length;
                double sumW = 0.0D;
                double[] weight = new double[n];
                for(int i=0; i<n; i++){
                    weight[i]=1.0D/(ww[i]*ww[i]);
                    sumW += ww[i];
                }
                double product=1.0D;
                for(int i=0; i<n; i++){
                    product *= Math.pow(aa[i], weight[i]/sumW);
                }
                return product;
        }

        // Weighted geometric mean of a 1D array of floats, aa
        public static float geometricMean(float[] aa, float[] ww){
                int n = aa.length;
                float sumW = 0.0F;
                float[] weight = new float[n];
                for(int i=0; i<n; i++){
                    weight[i]=1.0F/(ww[i]*ww[i]);
                    sumW += ww[i];
                }
                float product=1.0F;
                for(int i=0; i<n; i++){
                    product *= (float)Math.pow(aa[i], weight[i]/sumW);
                }
                return product;
        }

        // Harmonic mean of a 1D array of doubles, aa
        public static double harmonicMean(double[] aa){
                int n = aa.length;
                double sum = 0.0D;
                for(int i=0; i<n; i++)sum += 1.0D/aa[i];
                return (double)n/sum;
        }

        // Harmonic mean of a 1D array of floats, aa
        public static float harmonicMean(float[] aa){
                int n = aa.length;
                float sum = 0.0F;
                for(int i=0; i<n; i++)sum += 1.0F/aa[i];
                return (float)n/sum;
        }

        // Weighted harmonic mean of a 1D array of doubles, aa
        public static double harmonicMean(double[] aa, double[] ww){
                int n = aa.length;
                double sum = 0.0D;
                double sumW = 0.0D;
                double[] weight = new double[n];
                for(int i=0; i<n; i++){
                    sumW += ww[i];
                    weight[i]=1.0D/(ww[i]*ww[i]);
                }
                for(int i=0; i<n; i++)sum += ww[i]/aa[i];
                return sumW/sum;
        }

        // Weighted harmonic mean of a 1D array of floats, aa
        public static float harmonicMean(float[] aa, float[] ww){
                int n = aa.length;
                float sum = 0.0F;
                float sumW = 0.0F;
                float[] weight = new float[n];
                for(int i=0; i<n; i++){
                    sumW += ww[i];
                    weight[i]=1.0F/(ww[i]*ww[i]);
                }
                for(int i=0; i<n; i++)sum += ww[i]/aa[i];
                return sumW/sum;
        }

        // Generalised mean of a 1D array of doubles, aa
        public static double generalisedMean(double[] aa, double m){
                int n = aa.length;
                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum += Math.pow(aa[i],m);
                }
                return Math.pow(sum/((double)n), 1.0D/m);
        }

        // Generalised mean of a 1D array of floats, aa
        public static float generalisedMean(float[] aa, float m){
                int n = aa.length;
                float sum=0.0F;
                for(int i=0; i<n; i++){
                        sum += Math.pow(aa[i],m);
                }
                return (float)Math.pow(sum/((float)n), 1.0F/m);
        }

        // Interquartile mean of a 1D array of doubles, aa
        public static double interQuartileMean(double[] aa){
                int n = aa.length;
                if(n<4)throw new IllegalArgumentException("At least 4 array elements needed");
                double[] bb = Fmath.selectionSort(aa);
                double sum = 0.0D;
                for(int i=n/4; i<3*n/4; i++)sum += bb[i];
                return 2.0*sum/(double)(n);
        }

        // Interquartile mean of a 1D array of floats, aa
        public static float interQuartileMean(float[] aa){
                int n = aa.length;
                if(n<4)throw new IllegalArgumentException("At least 4 array elements needed");
                float[] bb = Fmath.selectionSort(aa);
                float sum = 0.0F;
                for(int i=n/4; i<3*n/4; i++)sum += bb[i];
                return 2.0F*sum/(float)(n);
        }

       // Root mean square (rms) of a 1D array of doubles, aa
        public static double rms(double[] aa){
                int n = aa.length;
                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=aa[i]*aa[i];
                }
                return Math.sqrt(sum/((double)n));
        }

        // Root mean square (rms) of a 1D array of floats, aa
        public static float rms(float[] aa){
                int n = aa.length;
                float sum = 0.0F;
                for(int i=0; i<n; i++){
                        sum+=aa[i]*aa[i];
                }
                sum /= (float)n;

                return (float)Math.sqrt(sum);
        }

        // Arithmetic mean of a 1D array of floats, aa
        public static float mean(float[] aa){
                int n = aa.length;
                float sum=0.0F;
                for(int i=0; i<n; i++){
                        sum+=aa[i];
                }
                return sum/((float)n);
        }

        // Arithmetic mean of a 1D array of int, aa
        public static double mean(int[] aa){
                int n = aa.length;
                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=(double)aa[i];
                }
                return sum/((double)n);
        }

             // Arithmetic mean of a 1D array of long, aa
        public static double mean(long[] aa){
                int n = aa.length;
                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=(double)aa[i];
                }
                return sum/((double)n);
        }

        // Arithmetic mean of a 1D array of Complex, aa
        public static Complex mean(Complex[] aa){
                int n = aa.length;
                Complex sum = new Complex(0.0D, 0.0D);
                for(int i=0; i<n; i++){
                        sum = sum.plus(aa[i]);
                }
                return sum.over((double)n);
        }

        // Median of a 1D array of doubles, aa
        public static double median(double[] aa){
                int n = aa.length;
                int nOverTwo = n/2;
                double med = 0.0D;
                double[] bb = Fmath.selectionSort(aa);
                if(Fmath.isOdd(n)){
                    med = bb[nOverTwo];
                }
                else{
                    med = (bb[nOverTwo-1]+bb[nOverTwo])/2.0D;
                }

                return med;
        }

        // Median of a 1D array of floats, aa
        public static float median(float[] aa){
                int n = aa.length;
                int nOverTwo = n/2;
                float med = 0.0F;
                float[] bb = Fmath.selectionSort(aa);
                if(Fmath.isOdd(n)){
                    med = bb[nOverTwo];
                }
                else{
                    med = (bb[nOverTwo-1]+bb[nOverTwo])/2.0F;
                }

                return med;
        }

        // Median of a 1D array of int, aa
        public static double median(int[] aa){
                int n = aa.length;
                int nOverTwo = n/2;
                double med = 0.0D;
                int[] bb = Fmath.selectionSort(aa);
                if(Fmath.isOdd(n)){
                    med = (double)bb[nOverTwo];
                }
                else{
                    med = (double)(bb[nOverTwo-1]+bb[nOverTwo])/2.0D;
                }

                return med;
        }

        // Median of a 1D array of long, aa
        public static double median(long[] aa){
                int n = aa.length;
                int nOverTwo = n/2;
                double med = 0.0D;
                long[] bb = Fmath.selectionSort(aa);
                if(Fmath.isOdd(n)){
                    med = (double)bb[nOverTwo];
                }
                else{
                    med = (double)(bb[nOverTwo-1]+bb[nOverTwo])/2.0D;
                }

                return med;
        }

        // Standard deviation of a 1D array of doubles, aa
        public static double standardDeviation(double[] aa){
                return Math.sqrt(variance(aa));
        }

        // Standard deviation of a 1D array of floats, aa
        public static float standardDeviation(float[] aa){
                return (float)Math.sqrt(variance(aa));
        }

        // Standard deviation of a 1D array of int, aa
        public static double standardDeviation(int[] aa){
                return Math.sqrt(variance(aa));
        }

        // Standard deviation of a 1D array of long, aa
        public static double standardDeviation(long[] aa){
                return Math.sqrt(variance(aa));
        }

        // Weighted standard deviation of a 1D array of doubles, aa
        public static double standardDeviation(double[] aa, double[] ww){
                if(aa.length!=ww.length)throw new IllegalArgumentException("length of variable array, " + aa.length + " and length of weight array, " + ww.length + " are different");
                return Math.sqrt(variance(aa, ww));
        }

        // Weighted standard deviation of a 1D array of floats, aa
        public static float standardDeviation(float[] aa, float[] ww){
                if(aa.length!=ww.length)throw new IllegalArgumentException("length of variable array, " + aa.length + " and length of weight array, " + ww.length + " are different");
                return (float)Math.sqrt(variance(aa, ww));
        }



        // volatility   log  (doubles)
        public static double volatilityLogChange(double[] array){
            int n = array.length-1;
            double[] change = new double[n];
            for(int i=0; i<n; i++)change[i] = Math.log(array[i+1]/array[i]);
            return Stat.standardDeviation(change);
        }

        // volatility   log  (floats)
        public static float volatilityLogChange(float[] array){
            int n = array.length-1;
            float[] change = new float[n];
            for(int i=0; i<n; i++)change[i] = (float)Math.log(array[i+1]/array[i]);
            return Stat.standardDeviation(change);
        }

        // volatility   percentage (double)
        public static double volatilityPerCentChange(double[] array){
            int n = array.length-1;
            double[] change = new double[n];
            for(int i=0; i<n; i++)change[i] = (array[i+1] - array[i])*100.0D/array[i];
            return Stat.standardDeviation(change);
        }

        // volatility   percentage (float)
        public static double volatilityPerCentChange(float[] array){
            int n = array.length-1;
            float[] change = new float[n];
            for(int i=0; i<n; i++)change[i] = (array[i+1] - array[i])*100.0F/array[i];
            return Stat.standardDeviation(change);
        }

        // Coefficient of variation of an array of doubles
        public static double coefficientOfVariation(double[] array){
            return 100.0D*Stat.standardDeviation(array)/Math.abs(Stat.mean(array));
        }

        // Coefficient of variation of an array of float
        public static float coefficientOfVariation(float[] array){
            return 100.0F*Stat.standardDeviation(array)/Math.abs(Stat.mean(array));
        }

        // Subtract mean of an array from data array elements
        public static double[] subtractMean(double[] array){
            int n = array.length;
            double mean = Stat.mean(array);
            double[] arrayMinusMean = new double[n];
            for(int i=0; i<n; i++)arrayMinusMean[i] = array[i] - mean;

            return arrayMinusMean;
        }

        // Subtract mean of an array from data array elements
        public static float[] subtractMean(float[] array){
            int n = array.length;
            float mean = Stat.mean(array);
            float[] arrayMinusMean = new float[n];
            for(int i=0; i<n; i++)arrayMinusMean[i] = array[i] - mean;

            return arrayMinusMean;
        }

        // Variance of a 1D array of doubles, aa
        public static double variance(double[] aa){
                int n = aa.length;
                double sum=0.0D, mean=0.0D;
                for(int i=0; i<n; i++){
                        sum+=aa[i];
                }
                mean=sum/((double)n);
                sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=Fmath.square(aa[i]-mean);
                }
                return sum/((double)(n-1));
        }

        // Variance of a 1D array of floats, aa
        public static float variance(float[] aa){
                int n = aa.length;
                float sum=0.0F, mean=0.0F;
                for(int i=0; i<n; i++){
                        sum+=aa[i];
                }
                mean=sum/((float)n);
                sum=0.0F;
                for(int i=0; i<n; i++){
                        sum+=Fmath.square(aa[i]-mean);
                }
                return sum/((float)(n-1));
        }

       // Variance of a 1D array of int, aa
        public static double variance(int[] aa){
                int n = aa.length;
                double sum=0.0D, mean=0.0D;
                for(int i=0; i<n; i++){
                        sum+=(double)aa[i];
                }
                mean=sum/((double)n);
                sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=Fmath.square((double)aa[i]-mean);
                }
                return sum/((double)(n-1));
        }

       // Variance of a 1D array of ilong, aa
        public static double variance(long[] aa){
                int n = aa.length;
                double sum=0.0D, mean=0.0D;
                for(int i=0; i<n; i++){
                        sum+=(double)aa[i];
                }
                mean=sum/((double)n);
                sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=Fmath.square((double)aa[i]-mean);
                }
                return sum/((double)(n-1));
        }

        // Weighted variance of a 1D array of doubles, aa
        public static double variance(double[] aa, double[] ww){
                int n = aa.length;
                if(n!=ww.length)throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
                double sumx=0.0D, sumw=0.0D, mean=0.0D;
                double[] weight = new double[n];
                for(int i=0; i<n; i++){
                        sumx+=aa[i];
                        weight[i]=1.0D/(ww[i]*ww[i]);
                        sumw+=weight[i];
                }
                mean=sumx/sumw;
                sumx=0.0D;
                for(int i=0; i<n; i++){
                        sumx+=weight[i]*Fmath.square(aa[i]-mean);
                }
                return sumx*(double)(n)/((double)(n-1)*sumw);
        }

        // Weighted variance of a 1D array of floats, aa
        public static float variance(float[] aa, float[] ww){
                int n = aa.length;
                if(n!=ww.length)throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
                float sumx=0.0F, sumw=0.0F, mean=0.0F;
                float[] weight = new float[n];
                for(int i=0; i<n; i++){
                        sumx+=aa[i];
                        weight[i]=1.0F/(ww[i]*ww[i]);
                        sumw+=weight[i];
                }
                mean=sumx/sumw;
                sumx=0.0F;
                for(int i=0; i<n; i++){
                        sumx+=weight[i]*Fmath.square(aa[i]-mean);
                }
                return sumx*(float)(n)/((float)(n-1)*sumw);
        }

        // Covariance of two 1D arrays of doubles, xx and yy
        public static double covariance(double[] xx, double[] yy){
                int n = xx.length;
                if(n!=yy.length)throw new IllegalArgumentException("length of x variable array, " + n + " and length of y array, " + yy.length + " are different");

                double sumx=0.0D, meanx=0.0D;
                double sumy=0.0D, meany=0.0D;
                for(int i=0; i<n; i++){
                        sumx+=xx[i];
                        sumy+=yy[i];
                }
                meanx=sumx/((double)n);
                meany=sumy/((double)n);
                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=(xx[i]-meanx)*(yy[i]-meany);
                }
                return sum/((double)(n-1));
        }

        // Covariance of two 1D arrays of floats, xx and yy
        public static float covariance(float[] xx, float[] yy){
                int n = xx.length;
                if(n!=yy.length)throw new IllegalArgumentException("length of x variable array, " + n + " and length of y array, " + yy.length + " are different");

                float sumx=0.0F, meanx=0.0F;
                float sumy=0.0F, meany=0.0F;
                for(int i=0; i<n; i++){
                        sumx+=xx[i];
                        sumy+=yy[i];
                }
                meanx=sumx/((float)n);
                meany=sumy/((float)n);
                float sum=0.0F;
                for(int i=0; i<n; i++){
                        sum+=(xx[i]-meanx)*(yy[i]-meany);
                }
                return sum/((float)(n-1));
        }

        // Covariance of two 1D arrays of ints, xx and yy
        public static double covariance(int[] xx, int[] yy){
                int n = xx.length;
                if(n!=yy.length)throw new IllegalArgumentException("length of x variable array, " + n + " and length of y array, " + yy.length + " are different");

                double sumx=0.0D, meanx=0.0D;
                double sumy=0.0D, meany=0.0D;
                for(int i=0; i<n; i++){
                        sumx+=(double)xx[i];
                        sumy+=(double)yy[i];
                }
                meanx=sumx/((double)n);
                meany=sumy/((double)n);
                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=((double)xx[i]-meanx)*((double)yy[i]-meany);
                }
                return sum/((double)(n-1));
        }

        // Covariance of two 1D arrays of ints, xx and yy
        public static double covariance(long[] xx, long[] yy){
                int n = xx.length;
                if(n!=yy.length)throw new IllegalArgumentException("length of x variable array, " + n + " and length of y array, " + yy.length + " are different");

                double sumx=0.0D, meanx=0.0D;
                double sumy=0.0D, meany=0.0D;
                for(int i=0; i<n; i++){
                        sumx+=(double)xx[i];
                        sumy+=(double)yy[i];
                }
                meanx=sumx/((double)n);
                meany=sumy/((double)n);
                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=((double)xx[i]-meanx)*((double)yy[i]-meany);
                }
                return sum/((double)(n-1));
        }

        // Weighted covariance of two 1D arrays of doubles, xx and yy with weights ww
        public static double covariance(double[] xx, double[] yy, double[] ww){
                int n = xx.length;
                if(n!=yy.length)throw new IllegalArgumentException("length of x variable array, " + n + " and length of y array, " + yy.length + " are different");
                if(n!=ww.length)throw new IllegalArgumentException("length of x variable array, " + n + " and length of weight array, " + yy.length + " are different");
                double sumx=0.0D, sumy=0.0D, sumw=0.0D, meanx=0.0D, meany=0.0D;
                double[] weight = new double[n];
                for(int i=0; i<n; i++){
                        sumx+=xx[i];
                        sumy+=yy[i];
                        weight[i]=1.0D/(ww[i]*ww[i]);
                        sumw+=weight[i];
                }
                meanx=sumx/sumw;
                meany=sumy/sumw;

                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=weight[i]*(xx[i]-meanx)*(yy[i]-meany);
                }
                return sum*(double)(n)/((double)(n-1)*sumw);
        }

        //

        // Gamma function
        // Lanczos approximation (6 terms)
        public static double gamma(double x){

                double xcopy = x;
                double first = x + lgfGamma + 0.5;
                double second = lgfCoeff[0];
                double fg = 0.0D;

                if(x>=0.0){
                        if(x>=1.0D && x-(int)x==0.0D){
                                fg = Stat.factorial(x)/x;
                        }
                        else{
                                first = Math.pow(first, x + 0.5)*Math.exp(-first);
                                for(int i=1; i<=lgfN; i++)second += lgfCoeff[i]/++xcopy;
                                fg = first*Math.sqrt(2.0*Math.PI)*second/x;
                        }
                }
                else{
                         fg = -Math.PI/(x*Stat.gamma(-x)*Math.sin(Math.PI*x));
                }
                return fg;
        }

        // Return the Lanczos constant gamma
        public static double getLanczosGamma(){
                return Stat.lgfGamma;
        }

        // Return the Lanczos constant N (number of coeeficients + 1)
        public static int getLanczosN(){
                return Stat.lgfN;
        }

        // Return the Lanczos coeeficients
        public static double[] getLanczosCoeff(){
                int n = Stat.getLanczosN()+1;
                double[] coef = new double[n];
                for(int i=0; i<n; i++){
                        coef[i] = Stat.lgfCoeff[i];
                }
                return coef;
        }

        // Return the nearest smallest representable floating point number to zero with mantissa rounded to 1.0
        public static double getFpmin(){
                return Stat.FPMIN;
        }

        // log to base e of the Gamma function
        // Lanczos approximation (6 terms)
        public static double logGamma(double x){
                double xcopy = x;
                double fg = 0.0D;
                double first = x + lgfGamma + 0.5;
                double second = lgfCoeff[0];

                if(x>=0.0){
                        if(x>=1.0 && x-(int)x==0.0){
                                fg = Stat.logFactorial(x)-Math.log(x);
                        }
                        else{
                                first -= (x + 0.5)*Math.log(first);
                                for(int i=1; i<=lgfN; i++)second += lgfCoeff[i]/++xcopy;
                                fg = Math.log(Math.sqrt(2.0*Math.PI)*second/x) - first;
                        }
                }
                else{
                        fg = Math.PI/(Stat.gamma(1.0D-x)*Math.sin(Math.PI*x));

                        if(fg!=1.0/0.0 && fg!=-1.0/0.0){
                                if(fg<0){
                                         throw new IllegalArgumentException("\nThe gamma function is negative");
                                }
                                else{
                                        fg = Math.log(fg);
                                }
                        }
                }
                return fg;
        }

        // Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
        public static double incompleteGamma(double a, double x){
                if(a<0.0D  || x<0.0D)throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
                double igf = 0.0D;

                if(x < a+1.0D){
                        // Series representation
                        igf = incompleteGammaSer(a, x);
                }
                else{
                        // Continued fraction representation
                        igf = incompleteGammaFract(a, x);
                }
                return igf;
        }

        // Complementary Incomplete Gamma Function Q(a,x) = 1 - P(a,x) = 1 - integral from zero to x of (exp(-t)t^(a-1))dt
        // Also known as the Incomplete Gamma Function
        public static double incompleteGammaComplementary(double a, double x){
                if(a<0.0D  || x<0.0D)throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
                double igf = 0.0D;

                if(x!=0.0D){
                        if(x==1.0D/0.0D)
                        {
                                igf=1.0D;
                        }
                        else{
                                if(x < a+1.0D){
                                        // Series representation
                                        igf = 1.0D - incompleteGammaSer(a, x);
                                }
                                else{
                                        // Continued fraction representation
                                        igf = 1.0D - incompleteGammaFract(a, x);
                                }
                        }
                }
                return igf;
        }

        // Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
        // Series representation of the function - valid for x < a + 1
        public static double incompleteGammaSer(double a, double x){
                if(a<0.0D  || x<0.0D)throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
                if(x>=a+1) throw new IllegalArgumentException("\nx >= a+1   use Continued Fraction Representation");

                int i = 0;
                double igf = 0.0D;
                boolean check = true;

                double acopy = a;
                double sum = 1.0/a;
                double incr = sum;
                double loggamma = Stat.logGamma(a);

                while(check){
                        ++i;
                        ++a;
                        incr *= x/a;
                        sum += incr;
                        if(Math.abs(incr) < Math.abs(sum)*Stat.igfeps){
                                igf = sum*Math.exp(-x+acopy*Math.log(x)- loggamma);
                                check = false;
                        }
                        if(i>=Stat.igfiter){
                                check=false;
                                igf = sum*Math.exp(-x+acopy*Math.log(x)- loggamma);
                                System.out.println("\nMaximum number of iterations were exceeded in Stat.incompleteGammaSer().\nCurrent value returned.\nIncrement = "+String.valueOf(incr)+".\nSum = "+String.valueOf(sum)+".\nTolerance =  "+String.valueOf(igfeps));
                        }
                }
                return igf;
        }

        // Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
        // Continued Fraction representation of the function - valid for x >= a + 1
        // This method follows the general procedure used in Numerical Recipes for C,
        // The Art of Scientific Computing
        // by W H Press, S A Teukolsky, W T Vetterling & B P Flannery
        // Cambridge University Press,   http://www.nr.com/
        public static double incompleteGammaFract(double a, double x){
                if(a<0.0D  || x<0.0D)throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
                if(x<a+1) throw new IllegalArgumentException("\nx < a+1   Use Series Representation");

                int i = 0;
                double ii = 0;
                double igf = 0.0D;
                boolean check = true;

                double loggamma = Stat.logGamma(a);
                double numer = 0.0D;
                double incr = 0.0D;
                double denom = x - a + 1.0D;
                double first = 1.0D/denom;
                double term = 1.0D/FPMIN;
                double prod = first;

                while(check){
                        ++i;
                        ii = (double)i;
                        numer = -ii*(ii - a);
                        denom += 2.0D;
                        first = numer*first + denom;
                        if(Math.abs(first) < Stat.FPMIN){
                            first = Stat.FPMIN;
                        }
                        term = denom + numer/term;
                        if(Math.abs(term) < Stat.FPMIN){
                            term = Stat.FPMIN;
                         }
                        first = 1.0D/first;
                        incr = first*term;
                        prod *= incr;
                        if(Math.abs(incr - 1.0D) < igfeps)check = false;
                        if(i>=Stat.igfiter){
                                check=false;
                                System.out.println("\nMaximum number of iterations were exceeded in Stat.incompleteGammaFract().\nCurrent value returned.\nIncrement - 1 = "+String.valueOf(incr-1)+".\nTolerance =  "+String.valueOf(igfeps));
                        }
                }
                igf = 1.0D - Math.exp(-x+a*Math.log(x)-loggamma)*prod;
                return igf;
        }

        // Reset the maximum number of iterations allowed in the calculation of the incomplete gamma functions
        public static void setIncGammaMaxIter(int igfiter){
                Stat.igfiter=igfiter;
        }

        // Return the maximum number of iterations allowed in the calculation of the incomplete gamma functions
        public static int getIncGammaMaxIter(){
                return Stat.igfiter;
        }

        // Reset the tolerance used in the calculation of the incomplete gamma functions
        public static void setIncGammaTol(double igfeps){
                Stat.igfeps=igfeps;
        }

        // Return the tolerance used in the calculation of the incomplete gamm functions
        public static double getIncGammaTol(){
                return Stat.igfeps;
        }

        // Beta function
        public static double beta(double z, double w){
                return Math.exp(logGamma(z) + logGamma(w) - logGamma(z + w));
        }

        // Incomplete Beta function
        // Continued Fraction approximation (see Numerical recipies for details of method)
        public static double incompleteBeta(double z, double w, double x){
            if(x<0.0D || x>1.0D)throw new IllegalArgumentException("Argument x, "+x+", must be lie between 0 and 1 (inclusive)");
            double ibeta = 0.0D;
            if(x==0.0D){
                ibeta=0.0D;
            }
            else{
                if(x==1.0D){
                    ibeta=1.0D;
                }
                else{
                    // Term before continued fraction
                    ibeta = Math.exp(Stat.logGamma(z+w) - Stat.logGamma(z) - logGamma(w) + z*Math.log(x) + w*Math.log(1.0D-x));
                    // Continued fraction
                    if(x < (z+1.0D)/(z+w+2.0D)){
                        ibeta = ibeta*Stat.contFract(z, w, x)/z;
                    }
                    else{
                        // Use symmetry relationship
                        ibeta = 1.0D - ibeta*Stat.contFract(w, z, 1.0D-x)/w;
                    }
                }
            }
            return ibeta;
        }

        // Incomplete fraction summation used in the method incompleteBeta
        // modified Lentz's method
        public static double contFract(double a, double b, double x){
            int maxit = 500;
            double eps = 3.0e-7;
            double aplusb = a + b;
            double aplus1 = a + 1.0D;
            double aminus1 = a - 1.0D;
            double c = 1.0D;
            double d = 1.0D - aplusb*x/aplus1;
            if(Math.abs(d)<Stat.FPMIN)d = FPMIN;
            d = 1.0D/d;
            double h = d;
            double aa = 0.0D;
            double del = 0.0D;
            int i=1, i2=0;
            boolean test=true;
            while(test){
                i2=2*i;
                aa = i*(b-i)*x/((aminus1+i2)*(a+i2));
                d = 1.0D + aa*d;
                if(Math.abs(d)<Stat.FPMIN)d = FPMIN;
                c = 1.0D + aa/c;
                if(Math.abs(c)<Stat.FPMIN)c = FPMIN;
                d = 1.0D/d;
                h *= d*c;
                aa = -(a+i)*(aplusb+i)*x/((a+i2)*(aplus1+i2));
                d = 1.0D + aa*d;
                if(Math.abs(d)<Stat.FPMIN)d = FPMIN;
                c = 1.0D + aa/c;
                if(Math.abs(c)<Stat.FPMIN)c = FPMIN;
                d = 1.0D/d;
                del = d*c;
                h *= del;
                i++;
                if(Math.abs(del-1.0D) < eps)test=false;
                if(i>maxit){
                    test=false;
                    System.out.println("Maximum number of iterations ("+maxit+") exceeded in Stat.contFract in Stat.incomplete Beta");
                }
            }
            return h;

        }

        // Error Function
        public static double erf(double x){
                double erf = 0.0D;
                if(x!=0.0){
                        if(x==1.0D/0.0D){
                                erf = 1.0D;
                        }
                        else{
                                if(x>=0){
                                        erf = Stat.incompleteGamma(0.5, x*x);
                                }
                                else{
                                        erf = -Stat.incompleteGamma(0.5, x*x);
                                }
                        }
                }
                return erf;
        }

        // Complementary Error Function
        public static double erfc(double x){
                double erfc = 1.0D;
                if(x!=0.0){
                        if(x==1.0D/0.0D){
                                erfc = 0.0D;
                        }
                        else{
                                if(x>=0){
                                        erfc = 1.0D - Stat.incompleteGamma(0.5, x*x);
                                }
                                else{
                                        erfc = 1.0D + Stat.incompleteGamma(0.5, x*x);
                                }
                        }
                }
                return erfc;
        }

        // Gaussian (normal) cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        // mean  =  the mean, sd = standard deviation
        public static double normalProb(double mean, double sd, double upperlimit){
                double arg = (upperlimit - mean)/(sd*Math.sqrt(2.0));
                return (1.0D + Stat.erf(arg))/2.0D;
        }

        // Gaussian (normal) cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        // mean  =  the mean, sd = standard deviation
        public static double normalProb(double mean, double sd, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit - mean)/(sd*Math.sqrt(2.0));
                double arg2 = (upperlimit - mean)/(sd*Math.sqrt(2.0));

                return (Stat.erf(arg2)-Stat.erf(arg1))/2.0D;
        }

        // Gaussian (normal) probability
        // mean  =  the mean, sd = standard deviation
        public static double normal(double mean, double sd, double x){
                return Math.exp(-Fmath.square((x - mean)/sd)/2.0)/(sd*Math.sqrt(2.0D*Math.PI));
        }

        // Returns an array of Gaussian (normal) random deviates - clock seed
        // mean  =  the mean, sd = standard deviation, length of array
        public static double[] normalRand(double mean, double sd, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                    ran[i]=rr.nextGaussian();
                    ran[i] = ran[i]*sd+mean;
                }
                return ran;
        }

        // Returns an array of Gaussian (normal) random deviates - user provided seed
        // mean  =  the mean, sd = standard deviation, length of array
        public static double[] normalRand(double mean, double sd, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                    ran[i]=rr.nextGaussian();
                    ran[i] = ran[i]*sd+mean;
                }
                return ran;
        }


        // Lorentzian cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        public static double lorentzianProb(double mu, double gamma, double upperlimit){
                double arg = (upperlimit - mu)/(gamma/2.0D);
                return (1.0D/Math.PI)*(Math.atan(arg)+Math.PI/2.0);
        }

        // Lorentzian cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double lorentzianProb(double mu, double gamma, double lowerlimit, double upperlimit){
                double arg1 = (upperlimit - mu)/(gamma/2.0D);
                double arg2 = (lowerlimit - mu)/(gamma/2.0D);
                return (1.0D/Math.PI)*(Math.atan(arg1)-Math.atan(arg2));
        }

        // Lorentzian probability
        public static double lorentzian(double mu, double gamma, double x){
                double arg =gamma/2.0D;
                return (1.0D/Math.PI)*arg/(Fmath.square(mu-x)+arg*arg);
        }


        // Returns an array of Lorentzian random deviates - clock seed
        // mu  =  the mean, gamma = half-height width, length of array
        public static double[] lorentzianRand(double mu, double gamma, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                    ran[i]=Math.tan((rr.nextDouble()-0.5)*Math.PI);
                    ran[i] = ran[i]*gamma/2.0D+mu;
                }
                return ran;
        }

        // Returns an array of Lorentzian random deviates - user provided seed
        // mu  =  the mean, gamma = half-height width, length of array
        public static double[] lorentzianRand(double mu, double gamma, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                    ran[i]=Math.tan((rr.nextDouble()-0.5)*Math.PI);
                    ran[i] = ran[i]*gamma/2.0D+mu;
                }
                return ran;
        }

        // Cumulative Poisson Probability Function
        // probability that a number of Poisson random events will occur between 0 and k (inclusive)
        // k is an integer greater than equal to 1
        // mean  = mean of the Poisson distribution
        public static double poissonProb(int k, double mean){
                if(k<1)throw new IllegalArgumentException("k must be an integer greater than or equal to 1");
                return Stat.incompleteGammaComplementary((double) k, mean);
        }

        // Poisson Probability Function
        // k is an integer greater than or equal to zero
        // mean  = mean of the Poisson distribution
        public static double poisson(int k, double mean){
                if(k<0)throw new IllegalArgumentException("k must be an integer greater than or equal to 0");
                return Math.pow(mean, k)*Math.exp(-mean)/Stat.factorial((double)k);
        }

        // Returns an array of Poisson random deviates - clock seed
        // mean  =  the mean,  n = length of array
        // follows the ideas of Numerical Recipes
        public static double[] poissonRand(double mean, int n){

                Random rr = new Random();
                double[] ran = poissonRandCalc(rr, mean, n);
                return ran;
        }

        // Returns an array of Poisson random deviates - user provided seed
        // mean  =  the mean,  n = length of array
        // follows the ideas of Numerical Recipes
        public static double[] poissonRand(double mean, int n, long seed){

                Random rr = new Random(seed);
                double[] ran = poissonRandCalc(rr, mean, n);
                return ran;
        }

        // Calculates and returns an array of Poisson random deviates
        private static double[] poissonRandCalc(Random rr, double mean, int n){
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
                            term *= rr.nextDouble();
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
                                yDev = Math.tan(Math.PI*rr.nextDouble());
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


        // Chi-Square Probability Function
        // probability that an observed chi-square value for a correct model should be less than chiSquare
        // nu  =  the degrees of freedom
        public static double chiSquareProb(double chiSquare, int nu){
                return Stat.incompleteGamma((double)nu/2.0D, chiSquare/2.0D);
        }

        // Chi-Square Statistic for Poisson distribution
        public static double chiSquare(double[] observed, double[] expected, double[] variance){
            int nObs = observed.length;
            int nExp = expected.length;
            int nVar = variance.length;
            if(nObs!=nExp)throw new IllegalArgumentException("observed array length does not equal the expected array length");
            if(nObs!=nVar)throw new IllegalArgumentException("observed array length does not equal the variance array length");
            double chi = 0.0D;
            for(int i=0; i<nObs; i++){
                chi += Fmath.square(observed[i]-expected[i])/variance[i];
            }
            return chi;
        }

        // Chi-Square Statistic for Poisson distribution for frequency data
        // and Poisson distribution for each bin
        // double arguments
        public static double chiSquareFreq(double[] observedFreq, double[] expectedFreq){
            int nObs = observedFreq.length;
            int nExp = expectedFreq.length;
            if(nObs!=nExp)throw new IllegalArgumentException("observed array length does not equal the expected array length");
            double chi = 0.0D;
            for(int i=0; i<nObs; i++){
                chi += Fmath.square(observedFreq[i]-expectedFreq[i])/expectedFreq[i];
            }
            return chi;
        }

        // Chi-Square Statistic for Poisson distribution for frequency data
        // and Poisson distribution for each bin
        // int arguments
        public static double chiSquareFreq(int[] observedFreq, int[] expectedFreq){
            int nObs = observedFreq.length;
            int nExp = expectedFreq.length;
            if(nObs!=nExp)throw new IllegalArgumentException("observed array length does not equal the expected array length");
            double[] observ = new double[nObs];
            double[] expect = new double[nObs];
            for(int i=0; i<nObs; i++){
                observ[i] = (int)observedFreq[i];
                expect[i] = (int)expectedFreq[i];
            }

            return chiSquareFreq(observ, expect);
        }

        // Returns the binomial cumulative probabilty
        public static double binomialProb(double p, int n, int k){
                if(p<0.0D || p>1.0D)throw new IllegalArgumentException("\np must lie between 0 and 1");
                if(k<0 || n<0)throw new IllegalArgumentException("\nn and k must be greater than or equal to zero");
                if(k>n)throw new IllegalArgumentException("\nk is greater than n");
                return Stat.incompleteBeta(k, n-k+1, p);
        }

        // Returns a binomial Coefficient as a double
        public static double binomialCoeff(int n, int k){
                if(k<0 || n<0)throw new IllegalArgumentException("\nn and k must be greater than or equal to zero");
                if(k>n)throw new IllegalArgumentException("\nk is greater than n");
                return Math.floor(0.5D + Math.exp(Stat.logFactorial(n) - Stat.logFactorial(k) - Stat.logFactorial(n-k)));
        }

        // Returns the F-distribution probabilty for degrees of freedom df1, df2
        // F ratio provided
        public static double fTestProb(double fValue, int df1, int df2){
            double ddf1 = (double)df1;
            double ddf2 = (double)df2;
            double x = ddf2/(ddf2+ddf1*fValue);
            return Stat.incompleteBeta(df2/2.0D, df1/2.0D, x);
        }

        // Returns the F-distribution probabilty for degrees of freedom df1, df2
        // numerator and denominator variances provided
        public static double fTestProb(double var1, int df1, double var2, int df2){
            double fValue = var1/var2;
            double ddf1 = (double)df1;
            double ddf2 = (double)df2;
            double x = ddf2/(ddf2+ddf1*fValue);
            return Stat.incompleteBeta(df2/2.0D, df1/2.0D, x);
        }

        // Returns the F-test value corresponding to a F-distribution probabilty, fProb,
        //   for degrees of freedom df1, df2
        public static double fTestValueGivenFprob(double fProb, int df1, int df2){

            // Create an array F-test value array
            int fTestsNum = 100;    // length of array
            double[] fTestValues = new double[fTestsNum];
            fTestValues[0]=0.0001D;             // lowest array value
            fTestValues[fTestsNum-1]=10000.0D;  // highest array value
            // calculate array increment - log scale
            double diff = (Fmath.log10(fTestValues[fTestsNum-1])-Fmath.log10(fTestValues[0]))/(fTestsNum-1);
            // Fill array
            for(int i=1; i<fTestsNum-1; i++){
                fTestValues[i] = Math.pow(10.0D,(Fmath.log10(fTestValues[i-1])+diff));
            }

            // calculate F test probability array corresponding to F-test value array
            double[] fTestProb = new double[fTestsNum];
            for(int i=0; i<fTestsNum; i++){
                fTestProb[i] = Stat.fTestProb(fTestValues[i], df1, df2);
            }

            // calculate F-test value for provided probability
            // using bisection procedure
            double fTest0 = 0.0D;
            double fTest1 = 0.0D;
            double fTest2 = 0.0D;

            // find bracket for the F-test probabilities and calculate F-Test value from above arrays
            boolean test0 = true;
            boolean test1 = true;
            int i=0;
            int endTest=0;
            while(test0){
                if(fProb==fTestProb[i]){
                    fTest0=fTestValues[i];
                    test0=false;
                    test1=false;
                }
                else{
                    if(fProb>fTestProb[i]){
                        test0=false;
                        if(i>0){
                            fTest1=fTestValues[i-1];
                            fTest2=fTestValues[i];
                            endTest=-1;
                        }
                        else{
                            fTest1=fTestValues[i]/10.0D;
                            fTest2=fTestValues[i];
                        }
                    }
                    else{
                        i++;
                        if(i>fTestsNum-1){
                            test0=false;
                            fTest1=fTestValues[i-1];
                            fTest2=10.0D*fTestValues[i-1];
                            endTest=1;
                        }
                    }
                }
            }

            // call bisection method
            if(test1)fTest0=fTestBisect(fProb, fTest1, fTest2, df1, df2, endTest);

            return fTest0;
        }

        // Bisection procedure for calculating and F-test value corresponding
        //   to a given F-test probability
        private static double fTestBisect(double fProb, double fTestLow, double fTestHigh, int df1, int df2, int endTest){

            double funcLow = fProb - Stat.fTestProb(fTestLow, df1, df2);
            double funcHigh = fProb - Stat.fTestProb(fTestHigh, df1, df2);
            double fTestMid = 0.0D;
            double funcMid = 0.0;
            int nExtensions = 0;
            int nIter = 1000;           // iterations allowed
            double check = fProb*1e-6;  // tolerance for bisection
            boolean test0 = true;       // test for extending bracket
            boolean test1 = true;       // test for bisection procedure
            while(test0){
                if(funcLow*funcHigh>0.0D){
                    if(endTest<0){
                        nExtensions++;
                        if(nExtensions>100){
                            System.out.println("Class: Stats\nMethod: fTestBisect\nProbability higher than range covered\nF-test value is less than "+fTestLow);
                            System.out.println("This value was returned");
                            fTestMid=fTestLow;
                            test0=false;
                            test1=false;
                        }
                        fTestLow /= 10.0D;
                        funcLow = fProb - Stat.fTestProb(fTestLow, df1, df2);
                    }
                    else{
                        nExtensions++;
                        if(nExtensions>100){
                            System.out.println("Class: Stats\nMethod: fTestBisect\nProbability lower than range covered\nF-test value is greater than "+fTestHigh);
                            System.out.println("This value was returned");
                            fTestMid=fTestHigh;
                            test0=false;
                            test1=false;
                        }
                        fTestHigh *= 10.0D;
                        funcHigh = fProb - Stat.fTestProb(fTestHigh, df1, df2);
                    }
                }
                else{
                    test0=false;
                }

                int i=0;
                while(test1){
                    fTestMid = (fTestLow+fTestHigh)/2.0D;
                    funcMid = fProb - Stat.fTestProb(fTestMid, df1, df2);
                    if(Math.abs(funcMid)<check){
                        test1=false;
                    }
                    else{
                        i++;
                        if(i>nIter){
                            System.out.println("Class: Stats\nMethod: fTestBisect\nmaximum number of iterations exceeded\ncurrent value of F-test value returned");
                            test1=false;
                        }
                        if(funcMid*funcHigh>0){
                            funcHigh=funcMid;
                            fTestHigh=fTestMid;
                        }
                        else{
                            funcLow=funcMid;
                            fTestLow=fTestMid;
                        }
                    }
                }
            }
            return fTestMid;
        }

        // Returns the Student t distribution probabilty
        public static double dtudentTProb(double tValue, int df){
            double ddf = (double)df;
            double x = ddf/(ddf+tValue*tValue);
            return 1.0D - Stat.incompleteBeta(df/2.0D, 0.5D, x);
        }

        // Distribute data into bins to obtain histogram
        // zero bin position and upper limit provided
        public static double[][] histogramBins(double[] data, double binWidth, double binZero, double binUpper){
            int n = 0;              // new array length
            int m = data.length;    // old array length;
            for(int i=0; i<m; i++)if(data[i]<=binUpper)n++;
            if(n!=m){
                double[] newData = new double[n];
                int j = 0;
                for(int i=0; i<m; i++){
                    if(data[i]<=binUpper){
                        newData[j] = data[i];
                        j++;
                    }
                }
                System.out.println((m-n)+" data points, above histogram upper limit, excluded in Stat.histogramBins");
                return histogramBins(newData, binWidth, binZero);
            }
            else{
                 return histogramBins(data, binWidth, binZero);

            }
        }

        // Distribute data into bins to obtain histogram
        // zero bin position provided
        public static double[][] histogramBins(double[] data, double binWidth, double binZero){
            double dmax = Fmath.maximum(data);
            int nBins = (int) Math.ceil((dmax - binZero)/binWidth);
            if(binZero+nBins*binWidth==dmax)nBins++;
            int nPoints = data.length;
            int[] dataCheck = new int[nPoints];
            for(int i=0; i<nPoints; i++)dataCheck[i]=0;
            double[]binWall = new double[nBins+1];
            binWall[0]=binZero;
            for(int i=1; i<=nBins; i++){
                binWall[i] = binWall[i-1] + binWidth;
            }
            double[][] binFreq = new double[2][nBins];
            for(int i=0; i<nBins; i++){
                binFreq[0][i]= (binWall[i]+binWall[i+1])/2.0D;
                binFreq[1][i]= 0.0D;
            }
            boolean test = true;

            for(int i=0; i<nPoints; i++){
                test=true;
                int j=0;
                while(test){
                    if(data[i]>=binWall[j] && data[i]<binWall[j+1]){
                        binFreq[1][j]+= 1.0D;
                        dataCheck[i]=1;
                        test=false;
                    }
                    else{
                        if(j==nBins-1){
                            test=false;
                        }
                        else{
                            j++;
                        }
                    }
                }
            }
            int nMissed=0;
            for(int i=0; i<nPoints; i++)if(dataCheck[i]==0)nMissed++;
            if(nMissed>0)System.out.println(nMissed+" data points, below histogram lower limit, excluded in Stat.histogramBins");
            return binFreq;
        }

        // Distribute data into bins to obtain histogram
        // zero bin position calculated
        public static double[][] histogramBins(double[] data, double binWidth){
            double dmin = Fmath.minimum(data);
            double dmax = Fmath.maximum(data);
            double span = dmax - dmin;
            int nBins = (int) Math.ceil(span/binWidth);
            double rem = ((double)nBins)*binWidth-span;
            double binZero =dmin-rem/2.0D;
            return Stat.histogramBins(data, binWidth, binZero);
        }

        // Distribute data into bins to obtain histogram and plot histogram
        // zero bin position and upper limit provided
        public static double[][] histogramBinsPlot(double[] data, double binWidth, double binZero, double binUpper){
            String xLegend = null;
            return histogramBinsPlot(data, binWidth, binZero, binUpper, xLegend);
        }

        // Distribute data into bins to obtain histogram and plot histogram
        // zero bin position, upper limit and x-axis legend provided
        public static double[][] histogramBinsPlot(double[] data, double binWidth, double binZero, double binUpper, String xLegend){
            int n = 0;              // new array length
            int m = data.length;    // old array length;
            for(int i=0; i<m; i++)if(data[i]<=binUpper)n++;
            if(n!=m){
                double[] newData = new double[n];
                int j = 0;
                for(int i=0; i<m; i++){
                    if(data[i]<=binUpper){
                        newData[j] = data[i];
                        j++;
                    }
                }
                System.out.println((m-n)+" data points, above histogram upper limit, excluded in Stat.histogramBins");
                return histogramBinsPlot(newData, binWidth, binZero, xLegend);
            }
            else{
                 return histogramBinsPlot(data, binWidth, binZero, xLegend);

            }
        }

        // Distribute data into bins to obtain histogram and plot the histogram
        // zero bin position provided
        public static double[][] histogramBinsPlot(double[] data, double binWidth, double binZero){
            String xLegend = null;
            return histogramBinsPlot(data, binWidth, binZero, xLegend);
        }

        // Distribute data into bins to obtain histogram and plot the histogram
        // zero bin position and x-axis legend provided
        public static double[][] histogramBinsPlot(double[] data, double binWidth, double binZero, String xLegend){
            double[][] results = histogramBins(data, binWidth, binZero);
            int nBins = results[0].length;
            int nPoints = nBins*3+1;
            double[][] cdata = PlotGraph.data(1, nPoints);
            cdata[0][0]=binZero;
            cdata[1][0]=0.0D;
            int k=1;
            for(int i=0; i<nBins; i++){
                cdata[0][k]=cdata[0][k-1];
                cdata[1][k]=results[1][i];
                k++;
                cdata[0][k]=cdata[0][k-1]+binWidth;
                cdata[1][k]=results[1][i];
                k++;
                cdata[0][k]=cdata[0][k-1];
                cdata[1][k]=0.0D;
                k++;
            }

            PlotGraph pg = new PlotGraph(cdata);
            pg.setGraphTitle("Histogram:  Bin Width = "+binWidth);
            pg.setLine(3);
            pg.setPoint(0);
            pg.setYaxisLegend("Frequency");
            if(xLegend!=null)pg.setXaxisLegend(xLegend);
            pg.plot();

            return results;
        }

        // Distribute data into bins to obtain histogram and plot the histogram
        // zero bin position calculated
        public static double[][] histogramBinsPlot(double[] data, double binWidth){
            String xLegend = null;
            return Stat.histogramBinsPlot(data, binWidth, xLegend);
        }

        // Distribute data into bins to obtain histogram and plot the histogram
        // zero bin position calculated, x-axis legend provided
        public static double[][] histogramBinsPlot(double[] data, double binWidth, String xLegend){
            double dmin = Fmath.minimum(data);
            double dmax = Fmath.maximum(data);
            double span = dmax - dmin;
            int nBins = (int) Math.ceil(span/binWidth);
            double rem = ((double)nBins)*binWidth-span;
            double binZero =dmin-rem/2.0D;
            return Stat.histogramBinsPlot(data, binWidth, binZero, xLegend);
        }


        // factorial of n
        // argument and return are integer, therefore limited to 0<=n<=12
        // see below for long and double arguments
        public static int factorial(int n){
                if(n<0)throw new IllegalArgumentException("n must be a positive integer");
                if(n>12)throw new IllegalArgumentException("n must less than 13 to avoid integer overflow\nTry long or double argument");
                int f = 1;
                for(int i=1; i<=n; i++)f*=i;
                return f;
        }

        // factorial of n
        // argument and return are long, therefore limited to 0<=n<=20
        // see below for double argument
        public static long factorial(long n){
                if(n<0)throw new IllegalArgumentException("n must be a positive integer");
                if(n>20)throw new IllegalArgumentException("n must less than 21 to avoid long integer overflow\nTry double argument");
                long f = 1;
                for(int i=1; i<=n; i++)f*=i;
                return f;
        }

        // factorial of n
        // Argument is of type double but must be, numerically, an integer
        // factorial returned as double but is, numerically, should be an integer
        // numerical rounding may makes this an approximation after n = 21
        public static double factorial(double n){
                if(n<0 || (n-(int)n)!=0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
                double f = 1.0D;
                int nn = (int)n;
                for(int i=1; i<=nn; i++)f*=i;
                return f;
        }

        // log to base e of the factorial of n
        // log[e](factorial) returned as double
        // numerical rounding may makes this an approximation
        public static double logFactorial(int n){
                if(n<0 || (n-(int)n)!=0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
                double f = 0.0D;
                for(int i=2; i<=n; i++)f+=Math.log(i);
                return f;
        }

        // log to base e of the factorial of n
        // Argument is of type double but must be, numerically, an integer
        // log[e](factorial) returned as double
        // numerical rounding may makes this an approximation
        public static double logFactorial(double n){
        if(n<0 || (n-(int)n)!=0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
                double f = 0.0D;
                int nn = (int)n;
                for(int i=2; i<=nn; i++)f+=Math.log(i);
                return f;
        }

        // Calculate correlation coefficient
        // x y data as double
        public static double corrCoeff(double[] xx, double[]yy){

            double temp0 = 0.0D, temp1 = 0.0D;  // working variables
            int nData = xx.length;
            if(yy.length!=nData)throw new IllegalArgumentException("array lengths must be equal");
            int df = nData-1;
            // means
            double mx = 0.0D;
            double my = 0.0D;
            for(int i=0; i<nData; i++){
                mx += xx[i];
                my += yy[i];
            }
            mx /= nData;
            my /= nData;

            // calculate sample variances
            double s2xx = 0.0D;
            double s2yy = 0.0D;
            double s2xy = 0.0D;
            for(int i=0; i<nData; i++){
                s2xx += Fmath.square(xx[i]-mx);
                s2yy += Fmath.square(yy[i]-my);
                s2xy += (xx[i]-mx)*(yy[i]-my);
            }
            s2xx /= df;
            s2yy /= df;
            s2xy /= df;

            // calculate corelation coefficient
            double sampleR = s2xy/Math.sqrt(s2xx*s2yy);

            return sampleR;
        }

        // Calculate correlation coefficient
        // x y data as float
        public static float corrCoeff(float[] x, float[] y){
            int nData = x.length;
            if(y.length!=nData)throw new IllegalArgumentException("array lengths must be equal");
            int n = x.length;
            double[] xx = new double[n];
            double[] yy = new double[n];
            for(int i=0; i<n; i++){
                xx[i] = (double)x[i];
                yy[i] = (double)y[i];
            }
            return (float)Stat.corrCoeff(xx, yy);
        }

        // Calculate correlation coefficient
        // x y data as int
        public static double corrCoeff(int[] x, int[]y){
            int n = x.length;
            if(y.length!=n)throw new IllegalArgumentException("array lengths must be equal");

            double[] xx = new double[n];
            double[] yy = new double[n];
            for(int i=0; i<n; i++){
                xx[i] = (double)x[i];
                yy[i] = (double)y[i];
            }
            return Stat.corrCoeff(xx, yy);
        }

        // Calculate weighted correlation coefficient
        // x y data and weights w as double
        public static double corrCoeff(double[] x, double[]y, double[] w){
            int n = x.length;
            if(y.length!=n)throw new IllegalArgumentException("x and y array lengths must be equal");
            if(w.length!=n)throw new IllegalArgumentException("x and weight array lengths must be equal");

            double sxy = Stat.covariance(x, y, w);
            double sx = Stat.variance(x, w);
            double sy = Stat.variance(y, w);
            return sxy/Math.sqrt(sx*sy);
        }

        // Calculate correlation coefficient
        // Binary data x and y
        // Input is the frequency matrix, F, elements, f(i,j)
        // f(0,0) - element00 - frequency of x and y both = 1
        // f(0,1) - element01 - frequency of x = 0 and y = 1
        // f(1,0) - element10 - frequency of x = 1 and y = 0
        // f(1,1) - element11 - frequency of x and y both = 0
        public static double corrCoeff(int element00, int element01, int element10, int element11){
            return ((double)(element00*element11 - element01*element10))/Math.sqrt((double)((element00+element01)*(element10+element11)*(element00+element10)*(element01+element11)));
        }

        // Calculate correlation coefficient
        // Binary data x and y
        // Input is the frequency matrix, F
        // F(0,0) - frequency of x and y both = 1
        // F(0,1) - frequency of x = 0 and y = 1
        // F(1,0) - frequency of x = 1 and y = 0
        // F(1,1) - frequency of x and y both = 0
        public static double corrCoeff(int[][] freqMatrix){
            double element00 = (double)freqMatrix[0][0];
            double element01 = (double)freqMatrix[0][1];
            double element10 = (double)freqMatrix[1][0];
            double element11 = (double)freqMatrix[1][1];
            return ((element00*element11 - element01*element10))/Math.sqrt(((element00+element01)*(element10+element11)*(element00+element10)*(element01+element11)));
        }

        // Linear correlation coefficient cumulative probablity
        // old name calls renamed method
        public static double linearCorrCoeffProb(double rCoeff, int nu){
            return corrCoeffProb(rCoeff, nu);
        }

        // Linear correlation coefficient cumulative probablity
        public static double corrCoeffProb(double rCoeff, int nu){
            if(Math.abs(rCoeff)>1.0D)throw new IllegalArgumentException("|Correlation coefficient| > 1 :  " + rCoeff);

            // Create instances of the classes holding the function evaluation methods
            CorrCoeff cc = new CorrCoeff();

            // Assign values to constant in the function
            cc.a = ((double)nu - 2.0D)/2.0D;


            double integral = Integration.gaussQuad(cc, Math.abs(rCoeff), 1.0D, 128);

            double preterm = Math.exp(Stat.logGamma((nu+1.0D)/2.0)-Stat.logGamma(nu/2.0D))/Math.sqrt(Math.PI);

            return preterm*integral;
        }

        // Linear correlation coefficient single probablity
        // old name calls renamed method
        public static double linearCorrCoeff(double rCoeff, int nu){
            return Stat.corrCoeffPdf(rCoeff, nu);
        }

        // Linear correlation coefficient single probablity
        public static double corrCoeffPdf(double rCoeff, int nu){
            if(Math.abs(rCoeff)>1.0D)throw new IllegalArgumentException("|Correlation coefficient| > 1 :  " + rCoeff);

            double a = ((double)nu - 2.0D)/2.0D;
            double y = Math.pow((1.0D - Fmath.square(rCoeff)),a);

            double preterm = Math.exp(Stat.logGamma((nu+1.0D)/2.0)-Stat.logGamma(nu/2.0D))/Math.sqrt(Math.PI);

            return preterm*y;
        }

        // Minimum Gumbel cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        public static double gumbelMinProb(double mu, double sigma, double upperlimit){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg = -(upperlimit - mu)/sigma;
                return Math.exp(-Math.exp(arg));
        }

        // Maximum Gumbel cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        public static double gumbelMaxProb(double mu, double sigma, double upperlimit){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg = -(upperlimit - mu)/sigma;
                return 1.0D-Math.exp(-Math.exp(arg));
        }


        // Minimum Gumbel cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double gumbelMinProb(double mu, double sigma, double lowerlimit, double upperlimit){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg1 = -(lowerlimit - mu)/sigma;
                double arg2 = -(upperlimit - mu)/sigma;
                double term1 = Math.exp(-Math.exp(arg1));
                double term2 = Math.exp(-Math.exp(arg2));
                return term2-term1;
        }

        // Maximum Gumbel cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double gumbelMaxProb(double mu, double sigma, double lowerlimit, double upperlimit){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg1 = (lowerlimit - mu)/sigma;
                double arg2 = (upperlimit - mu)/sigma;
                double term1 = -Math.exp(-Math.exp(arg1));
                double term2 = -Math.exp(-Math.exp(arg2));
                return term2-term1;
        }

        // Minimum Gumbel probability
        public static double gumbelMin(double mu,double sigma, double x){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg =(x-mu)/sigma;
                return (1.0D/sigma)*Math.exp(arg)*Math.exp(-Math.exp(arg));
        }

        // Maximum Gumbel probability
        public static double gumbelMax(double mu,double sigma, double x){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg =-(x-mu)/sigma;
                return (1.0D/sigma)*Math.exp(arg)*Math.exp(-Math.exp(arg));
        }

        // Returns an array of minimal Gumbel (Type I EVD) random deviates - clock seed
        // mu  =  location parameter, sigma = scale parameter, n = length of array
        public static double[] gumbelMinRand(double mu, double sigma,  int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = Math.log(Math.log(1.0D/(1.0D-rr.nextDouble())))*sigma+mu;
                }
                return ran;
        }

        // Returns an array of minimal Gumbel (Type I EVD) random deviates - user supplied seed
        // mu  =  location parameter, sigma = scale parameter, n = length of array
        public static double[] gumbelMinRand(double mu, double sigma,  int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = Math.log(Math.log(1.0D/(1.0D-rr.nextDouble())))*sigma+mu;
                }
                return ran;
        }

        // Returns an array of maximal Gumbel (Type I EVD) random deviates - clock seed
        // mu  =  location parameter, sigma = scale parameter, n = length of array
        public static double[] gumbelMaxRand(double mu, double sigma,  int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = mu-Math.log(Math.log(1.0D/(1.0D-rr.nextDouble())))*sigma;
                }
                return ran;
        }

       // Returns an array of maximal Gumbel (Type I EVD) random deviates - user supplied seed
        // mu  =  location parameter, sigma = scale parameter, n = length of array
        public static double[] gumbelMaxRand(double mu, double sigma,  int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = mu-Math.log(Math.log(1.0D/(1.0D-rr.nextDouble())))*sigma;
                }
                return ran;
        }

        // Minimum Gumbel mean
        public static double gumbelMinMean(double mu,double sigma){
                return mu - sigma*Fmath.EULER_CONSTANT_GAMMA;
        }

        // Maximum Gumbel mean
        public static double gumbelMaxMean(double mu,double sigma){
                return mu + sigma*Fmath.EULER_CONSTANT_GAMMA;
        }

        // Minimum Gumbel standard deviation
        public static double gumbelMinStandDev(double sigma){
                return sigma*Math.PI/Math.sqrt(6.0D);
        }

        // Maximum Gumbel standard deviation
        public static double gumbelMaxStandDev(double sigma){
                return sigma*Math.PI/Math.sqrt(6.0D);
        }

        // Minimum Gumbel mode
        public static double gumbelMinMode(double mu,double sigma){
            return mu;
        }

        // Maximum Gumbel mode
        public static double gumbelMaxMode(double mu,double sigma){
            return mu;
        }

        // Minimum Gumbel median
        public static double gumbelMinMedian(double mu,double sigma){
            return mu + sigma*Math.log(Math.log(2.0D));
        }

        // Maximum Gumbel median
        public static double gumbelMaxMedian(double mu,double sigma){
            return mu - sigma*Math.log(Math.log(2.0D));
        }


        // Weibull cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double weibullProb(double mu, double sigma, double gamma, double upperlimit){
                double arg = (upperlimit - mu)/sigma;
                double y = 0.0D;
                if(arg>0.0D)y = 1.0D - Math.exp(-Math.pow(arg, gamma));
                return y;
        }


        // Weibull cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double weibullProb(double mu, double sigma, double gamma, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit - mu)/sigma;
                double arg2 = (upperlimit - mu)/sigma;
                double term1 = 0.0D, term2 = 0.0D;
                if(arg1>=0.0D)term1 = -Math.exp(-Math.pow(arg1, gamma));
                if(arg2>=0.0D)term2 = -Math.exp(-Math.pow(arg2, gamma));
                return term2-term1;
        }

        // Weibull probability
        public static double weibull(double mu,double sigma, double gamma, double x){
                double arg =(x-mu)/sigma;
                double y = 0.0D;
                if(arg>=0.0D){
                    y = (gamma/sigma)*Math.pow(arg, gamma-1.0D)*Math.exp(-Math.pow(arg, gamma));
                }
                return y;
        }

        // Weibull mean
        public static double weibullMean(double mu,double sigma, double gamma){
                return mu + sigma*Stat.gamma(1.0D/gamma+1.0D);
        }

        // Weibull standard deviation
        public static double weibullStandDev(double sigma, double gamma){
                double y = Stat.gamma(2.0D/gamma+1.0D)-Fmath.square(Stat.gamma(1.0D/gamma+1.0D));
                return sigma*Math.sqrt(y);
        }

        // Weibull mode
        public static double weibullMode(double mu,double sigma, double gamma){
            double y=mu;
            if(gamma>1.0D){
                y = mu + sigma*Math.pow((gamma-1.0D)/gamma, 1.0D/gamma);
            }
            return y;
        }

        // Weibull median
        public static double weibullMedian(double mu,double sigma, double gamma){
            return mu + sigma*Math.pow(Math.log(2.0D),1.0D/gamma);
        }

        // Returns an array of Weibull (Type III EVD) random deviates - clock seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] weibullRand(double mu, double sigma, double gamma, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = Math.pow(-Math.log(1.0D-rr.nextDouble()),1.0D/gamma)*sigma + mu;
                }
                return ran;
        }

        // Returns an array of Weibull (Type III EVD) random deviates - user supplied seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] weibullRand(double mu, double sigma, double gamma, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = Math.pow(-Math.log(1.0D-rr.nextDouble()),1.0D/gamma)*sigma + mu;
                }
                return ran;
        }

        // Frechet cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double frechetProb(double mu, double sigma, double gamma, double upperlimit){
                double arg = (upperlimit - mu)/sigma;
                double y = 0.0D;
                if(arg>0.0D)y = Math.exp(-Math.pow(arg, -gamma));
                return y;
        }


        // Frechet cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double frechetProb(double mu, double sigma, double gamma, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit - mu)/sigma;
                double arg2 = (upperlimit - mu)/sigma;
                double term1 = 0.0D, term2 = 0.0D;
                if(arg1>=0.0D)term1 = Math.exp(-Math.pow(arg1, -gamma));
                if(arg2>=0.0D)term2 = Math.exp(-Math.pow(arg2, -gamma));
                return term2-term1;
        }

        // Frechet probability
        public static double frechet(double mu,double sigma, double gamma, double x){
                double arg =(x-mu)/sigma;
                double y = 0.0D;
                if(arg>=0.0D){
                    y = (gamma/sigma)*Math.pow(arg, -gamma-1.0D)*Math.exp(-Math.pow(arg, -gamma));
                }
                return y;
        }

        // Frechet mean
        public static double frechetMean(double mu,double sigma, double gamma){
                double y = Double.NaN;
                if(gamma>1.0D){
                    y = mu + sigma*Stat.gamma(1.0D-1.0D/gamma);
                }
                return y;
        }

        // Frechet standard deviation
        public static double frechetStandDev(double sigma, double gamma){
                double y = Double.NaN;
                if(gamma>2.0D){
                    y = Stat.gamma(1.0D-2.0D/gamma)-Fmath.square(Stat.gamma(1.0D-1.0D/gamma));
                    y = sigma*Math.sqrt(y);
                }
                return y;
        }

        // Frechet mode
        public static double frechetMode(double mu,double sigma, double gamma){
                return mu + sigma*Math.pow(gamma/(1.0D+gamma), 1.0D/gamma);
         }

        // Returns an array of Frechet (Type II EVD) random deviates - clock seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] frechetRand(double mu, double sigma, double gamma, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = Math.pow((1.0D/(Math.log(1.0D/rr.nextDouble()))),1.0D/gamma)*sigma + mu;
                }
                return ran;
        }

        // Returns an array of Frechet (Type II EVD) random deviates - user supplied seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] frechetRand(double mu, double sigma, double gamma, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = Math.pow((1.0D/(Math.log(1.0D/rr.nextDouble()))),1.0D/gamma)*sigma + mu;
                }
                return ran;
        }

        // Exponential cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double exponentialProb(double mu, double sigma, double upperlimit){
                double arg = (upperlimit - mu)/sigma;
                double y = 0.0D;
                if(arg>0.0D)y = 1.0D - Math.exp(-arg);
                return y;
        }

        // Exponential cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double exponentialProb(double mu, double sigma, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit - mu)/sigma;
                double arg2 = (upperlimit - mu)/sigma;
                double term1 = 0.0D, term2 = 0.0D;
                if(arg1>=0.0D)term1 = -Math.exp(-arg1);
                if(arg2>=0.0D)term2 = -Math.exp(-arg2);
                return term2-term1;
        }

        // Exponential probability
        public static double exponential(double mu,double sigma, double x){
                double arg =(x-mu)/sigma;
                double y = 0.0D;
                if(arg>=0.0D){
                    y = Math.exp(-arg)/sigma;
                }
                return y;
        }

        // Exponential mean
        public static double exponentialMean(double mu, double sigma){
                return mu + sigma;
        }

        // Exponential standard deviation
        public static double exponentialStandDev(double sigma){
                return sigma;
        }

        // Exponential mode
        public static double exponentialMode(double mu){
            return mu;
        }

        // Exponential median
        public static double exponentialMedian(double mu,double sigma){
            return mu + sigma*Math.log(2.0D);
        }

        // Returns an array of Exponential random deviates - clock seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] exponentialRand(double mu, double sigma, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = mu - Math.log(1.0D-rr.nextDouble())*sigma;
                }
                return ran;
        }

        // Returns an array of Exponential random deviates - user supplied seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] exponentialRand(double mu, double sigma, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = mu - Math.log(1.0D-rr.nextDouble())*sigma;
                }
                return ran;
        }


        // Rayleigh cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double rayleighProb(double sigma, double upperlimit){
                double arg = (upperlimit)/sigma;
                double y = 0.0D;
                if(arg>0.0D)y = 1.0D - Math.exp(-arg*arg/2.0D);
                return y;
        }

        // Rayleigh cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double rayleighProb(double sigma, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit)/sigma;
                double arg2 = (upperlimit)/sigma;
                double term1 = 0.0D, term2 = 0.0D;
                if(arg1>=0.0D)term1 = -Math.exp(-arg1*arg1/2.0D);
                if(arg2>=0.0D)term2 = -Math.exp(-arg2*arg2/2.0D);
                return term2-term1;
        }

        // Rayleigh probability
        public static double rayleigh(double sigma, double x){
                double arg =x/sigma;
                double y = 0.0D;
                if(arg>=0.0D){
                    y = (arg/sigma)*Math.exp(-arg*arg/2.0D)/sigma;
                }
                return y;
        }

        // Rayleigh mean
        public static double rayleighMean(double sigma){
                return sigma*Math.sqrt(Math.PI/2.0D);
        }

        // Rayleigh standard deviation
        public static double rayleighStandDev(double sigma){
                return sigma*Math.sqrt(2.0D-Math.PI/2.0D);
        }

        // Rayleigh mode
        public static double rayleighMode(double sigma){
            return sigma;
        }

        // Rayleigh median
        public static double rayleighMedian(double sigma){
            return sigma*Math.sqrt(Math.log(2.0D));
        }

        // Returns an array of Rayleigh random deviates - clock seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] rayleighRand(double sigma, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = Math.sqrt(-2.0D*Math.log(1.0D-rr.nextDouble()))*sigma;
                }
                return ran;
        }

        // Returns an array of Rayleigh random deviates - user supplied seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] rayleighRand(double sigma, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = Math.sqrt(-2.0D*Math.log(1.0D-rr.nextDouble()))*sigma;
                }
                return ran;
        }

        // Pareto cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double paretoProb(double alpha, double beta, double upperlimit){
                double y = 0.0D;
                if(upperlimit>=beta)y = 1.0D - Math.pow(beta/upperlimit, alpha);
                return y;
        }

        // Pareto cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double paretoProb(double alpha, double beta, double lowerlimit, double upperlimit){
                double term1 = 0.0D, term2 = 0.0D;
                if(lowerlimit>=beta)term1 = -Math.pow(beta/lowerlimit, alpha);
                if(upperlimit>=beta)term2 = -Math.pow(beta/upperlimit, alpha);
                return term2-term1;
        }

        // Pareto probability
        public static double pareto(double alpha, double beta, double x){
                double y = 0.0D;
                if(x>=beta){
                    y = alpha*Math.pow(beta, alpha)/Math.pow(x, alpha+1.0D);
                }
                return y;
        }

        // Pareto mean
        public static double paretoMean(double alpha, double beta){
                double y = Double.NaN;
                if(alpha>1.0D)y = alpha*beta/(alpha-1);
                return y;
        }

        // Pareto standard deviation
        public static double paretoStandDev(double alpha, double beta){
                double y = Double.NaN;
                if(alpha>1.0D)y = alpha*Fmath.square(beta)/(Fmath.square(alpha-1)*(alpha-2));
                return y;
        }

        // Pareto mode
        public static double paretoMode(double beta){
            return beta;
        }

        // Returns an array of Pareto random deviates - clock seed
        public static double[] paretoRand(double alpha, double beta, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = Math.pow(1.0D-rr.nextDouble(), -1.0D/alpha)*beta;
                }
                return ran;
        }

        // Returns an array of Pareto random deviates - user supplied seed
        public static double[] paretoRand(double alpha, double beta, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = Math.pow(1.0D-rr.nextDouble(), -1.0D/alpha)*beta;
                }
                return ran;
        }

        // Anscombe test for a upper outlier
        public static Vector<Object> upperOutliersAnscombe(double[] values, double constant){
            boolean test=true;
            double[] copy0 = (double[])values.clone();
            int nValues = values.length;
            int nValues0 = nValues;
            Vector<Double> outers = new Vector<Double>();
            int nOutliers = 0;

            while(test){
                boolean copyCheck = true;
                double mean = Stat.mean(copy0);
                double standDev = Stat.standardDeviation(copy0);
                double max = Fmath.maximum(copy0);
                double statistic = (max - mean)/standDev;
                int j=0;
                if(statistic>constant){
                    outers.addElement(new Double(max));
                    nOutliers++;
                    double[] copy1 = new double[nValues-1];
                    for(int i=0; i<nValues; i++){
                        if(copy0[i]==max && copyCheck){
                            copyCheck = false;
                        }
                        else{
                            copy1[j] = copy0[i];
                            j++;
                        }
                    }
                    nValues--;
                    copy0 = (double[])copy1.clone();
                }
                else{
                    test=false;
                }
            }

            double[] outliers = null;
            int[] outIndices = null;
            if(nOutliers>0){
                outliers = new double[nOutliers];
                outIndices = new int[nOutliers];
                boolean[] indCheck = new boolean[nValues0];
                for(int i=0; i<nValues0; i++)indCheck[i]=true;
                for(int i=0; i<nOutliers; i++)outliers[i] = ((Double)outers.elementAt(i)).doubleValue();
                for(int j=0; j<nOutliers; j++){
                    boolean testO = true;
                    int i = 0;
                    while(testO){
                        if(outliers[j]==values[i] && indCheck[i]){
                            outIndices[j] = i;
                            indCheck[i]=false;
                            testO = false;
                        }
                        else{
                            i++;
                            if(i>=nValues0){
                                testO=false;
                                System.out.println("Stat.outliersUpperAnscombe");
                                System.out.println("Could not find an outlier index!!!!!!!!!");
                            }
                        }
                    }
                }
            }

            Vector<Object> ret = new Vector<Object>();
            ret.addElement(new Integer(nOutliers));
            ret.addElement(outliers);
            ret.addElement(outIndices);
            ret.addElement(copy0);

            return ret;
        }


        // Anscombe test for a lower outlier
        public static Vector<Object> lowerOutliersAnscombe(double[] values, double constant){
            boolean test=true;
            double[] copy0 = (double[])values.clone();
            int nValues = values.length;
            int nValues0 = nValues;
            Vector<Double> outers = new Vector<Double>();
            int nOutliers = 0;

            while(test){
                boolean copyCheck = true;
                double mean = Stat.mean(copy0);
                double standDev = Stat.standardDeviation(copy0);
                double min = Fmath.minimum(copy0);
                double statistic = (mean - min)/standDev;
                int j=0;
                if(statistic>constant){
                    outers.addElement(new Double(min));
                    nOutliers++;
                    double[] copy1 = new double[nValues-1];
                    for(int i=0; i<nValues; i++){
                        if(copy0[i]==min && copyCheck){
                            copyCheck = false;
                        }
                        else{
                            copy1[j] = copy0[i];
                            j++;
                        }
                    }
                    nValues--;
                    copy0 = (double[])copy1.clone();
                }
                else{
                    test=false;
                }
            }

            double[] outliers = null;
            int[] outIndices = null;
            if(nOutliers>0){
                outliers = new double[nOutliers];
                outIndices = new int[nOutliers];
                boolean[] indCheck = new boolean[nValues0];
                for(int i=0; i<nValues0; i++)indCheck[i]=true;
                for(int i=0; i<nOutliers; i++)outliers[i] = ((Double)outers.elementAt(i)).doubleValue();
                for(int j=0; j<nOutliers; j++){
                    boolean testO = true;
                    int i = 0;
                    while(testO){
                        if(outliers[j]==values[i] && indCheck[i]){
                            outIndices[j] = i;
                            indCheck[i]=false;
                            testO = false;
                        }
                        else{
                            i++;
                            if(i>=nValues0){
                                testO=false;
                                System.out.println("Stat.outliersUpperAnscombe");
                                System.out.println("Could not find an outlier index!!!!!!!!!");
                            }
                        }
                    }
                }
            }

            Vector<Object> ret = new Vector<Object>();
            ret.addElement(new Integer(nOutliers));
            ret.addElement(outliers);
            ret.addElement(outIndices);
            ret.addElement(copy0);

            return ret;
        }

 }














