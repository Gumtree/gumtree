/*
*   Class PlotPoleZero
*
*   Plots, in a window, the poles and zeros of a transfer function,
*   of the form of a polynomial over a polynomial, in either the s- or
*   z-plane given the coefficients of the polynomials either as two arrays
*   or as two types ComplexPolynom()
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:       July 2002
*   REVISED:    22 June 2003, 14 August 2004, 16 May 2005
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   PlotPoleZero.html
*
*   Copyright (c) June 2003, May 2005
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


package flanagan.plot;

import java.awt.*;
import flanagan.math.Fmath;
import flanagan.complex.Complex;
import flanagan.complex.ComplexPoly;
import flanagan.io.FileOutput;

public class PlotPoleZero{
        private ComplexPoly numerCoeff = null;      // ComplexPoly instance of the numerator polynomial
        private ComplexPoly denomCoeff = null;      // ComplexPoly instance of the denominator polynomial
        private Complex[] numerRoots = null;        // Roots of the numerator polynomial
        private Complex[] denomRoots = null;        // Roots of the denominator polynomial
        private double[][] data = null;             // Data for PlotGraph
        private int nDeg = 0;                       // degree of numerator polynomial
        private int dDeg = 0;                       // degree of denominator polynomial
        private int mDeg = 0;                       // maximum of the two polynomial degrees
        private int sORz = 0;                       // if 0 s or z plot, =1 s plane plot, =2 z plane plot
        private boolean zCircle = false;            // if true - a unit radius circle is plotted
        private boolean noImag = true;              // if true - no imaginary non-zero values
        private boolean noReal = true;              // if true - no real non-zero values
        private boolean noZeros = true;             // = true if no zeros, false if there are
        private boolean noPoles = true;             // = true if no poles, false if there are


        // Constructors
        // no poles or zeros set
        public PlotPoleZero(){
        }

        // numer    Array of coefficients of the numerator polynomial
        // denom    Array of coefficients of the denominator polynomial
        // ComplexPoly coefficients
        public PlotPoleZero(ComplexPoly numer, ComplexPoly denom){

                if(numer!=null){
                    this.nDeg = numer.getDeg();
                    if(this.nDeg>0){
                        this.numerCoeff = ComplexPoly.copy(numer);
                        this.numerRoots = Complex.oneDarray(nDeg);
                        this.mDeg = nDeg;
                        this.noZeros = false;
                    }
                }

                if(denom!=null){
                    this.dDeg = denom.getDeg();
                    if(this.dDeg>0){
                        this.denomCoeff = ComplexPoly.copy(denom);
                        this.denomRoots = Complex.oneDarray(dDeg);
                        if(!this.noZeros){
                            this.mDeg = (int)Math.max(nDeg, dDeg);
                        }
                        else{
                            this.mDeg = dDeg;
                        }
                        this.noPoles = false;
                    }
                }
                if(this.noZeros && this.noPoles)throw new IllegalArgumentException("No poles or zeros entered");
        }

        // Two arrays of Complex coefficients
        public PlotPoleZero(Complex[] numer, Complex[] denom){

            if(numer!=null){
                this.nDeg = numer.length-1;
                if(this.nDeg>0){
                    this.numerCoeff = new ComplexPoly(numer);;
                    this.numerRoots = Complex.oneDarray(nDeg);
                    this.mDeg = nDeg;
                    this.noZeros = false;
                }
            }

            if(denom!=null){
                this.dDeg = denom.length-1;
                if(this.dDeg>0){
                    this.denomCoeff = new ComplexPoly(denom);;
                    this.denomRoots = Complex.oneDarray(dDeg);
                    if(!this.noZeros){
                        this.mDeg = (int)Math.max(nDeg, dDeg);
                    }
                    else{
                        this.mDeg = dDeg;
                    }
                    this.noPoles = false;
                }
                if(this.noZeros && this.noPoles)throw new IllegalArgumentException("No poles or zeros entered");
            }
        }


        // Two arrays of double coefficients
        public PlotPoleZero(double[] numer, double[] denom){

            if(numer!=null){
                this.nDeg = numer.length-1;
                if(this.nDeg>0){
                    this.numerCoeff = new ComplexPoly(numer);;
                    this.numerRoots = Complex.oneDarray(nDeg);
                    this.mDeg = nDeg;
                    this.noZeros = false;
                }
            }

            if(denom!=null){
                this.dDeg = denom.length-1;
                if(this.dDeg>0){
                    this.denomCoeff = new ComplexPoly(denom);;
                    this.denomRoots = Complex.oneDarray(dDeg);
                    if(!this.noZeros){
                        this.mDeg = (int)Math.max(nDeg, dDeg);
                    }
                    else{
                        this.mDeg = dDeg;
                    }
                    this.noPoles = false;
                }
                if(this.noZeros && this.noPoles)throw new IllegalArgumentException("No poles or zeros entered");
            }
        }


        // Enter zeros as ComplexPoly
        public void setNumerator(ComplexPoly numer){
                if(numer!=null){
                    this.nDeg = numer.getDeg();
                    if(this.nDeg>0){
                        this.numerCoeff = ComplexPoly.copy(numer);
                        this.numerRoots = Complex.oneDarray(nDeg);
                        if(!this.noPoles){
                            this.mDeg = (int)Math.max(nDeg, dDeg);
                        }
                        else{
                            this.mDeg = nDeg;
                        }
                        this.noZeros = false;
                    }
                }
                if(this.noZeros)throw new IllegalArgumentException("No zeros entered");
        }

        // Enter poles as ComplexPoly
        public void setDenominator(ComplexPoly denom){
                if(denom!=null){
                    this.dDeg = denom.getDeg();
                    if(this.dDeg>0){
                        this.denomCoeff = ComplexPoly.copy(denom);
                        this.denomRoots = Complex.oneDarray(dDeg);
                        if(!this.noZeros){
                            this.mDeg = (int)Math.max(nDeg, dDeg);
                        }
                        else{
                            this.mDeg = dDeg;
                        }
                        this.noPoles = false;
                    }
                }
                if(this.noPoles)throw new IllegalArgumentException("No poles entered");
        }

        // Enter zeros: array of Complex coefficients
        public void setNumerator(Complex[] numer){
            if(numer!=null){
                this.nDeg = numer.length-1;
                if(this.nDeg>0){
                    this.numerCoeff = new ComplexPoly(numer);;
                    this.numerRoots = Complex.oneDarray(nDeg);
                    if(!this.noPoles){
                        this.mDeg = (int)Math.max(nDeg, dDeg);
                    }
                    else{
                        this.mDeg = nDeg;
                    }
                    this.noZeros = false;
                }
                 if(this.noZeros)throw new IllegalArgumentException("No zeros entered");
            }
        }

        // Enter poles: array of Complex coefficients
        public void setDenominator(Complex[] denom){
            if(denom!=null){
                this.dDeg = denom.length-1;
                if(this.dDeg>0){
                    this.denomCoeff = new ComplexPoly(denom);;
                    this.denomRoots = Complex.oneDarray(dDeg);
                    if(!this.noZeros){
                        this.mDeg = (int)Math.max(nDeg, dDeg);
                    }
                    else{
                        this.mDeg = dDeg;
                    }
                    this.noPoles = false;
                }
                if(this.noPoles)throw new IllegalArgumentException("No poles entered");
            }
        }

        // Enter zeros: array of double coefficients
        public void setNumerator(double[] numer){
            if(numer!=null){
                this.nDeg = numer.length-1;
                if(this.nDeg>0){
                    this.numerCoeff = new ComplexPoly(numer);;
                    this.numerRoots = Complex.oneDarray(nDeg);
                    if(!this.noPoles){
                        this.mDeg = (int)Math.max(nDeg, dDeg);
                    }
                    else{
                        this.mDeg = nDeg;
                    }
                    this.noZeros = false;
                }
                if(this.noZeros)throw new IllegalArgumentException("No zeros entered");
            }
        }

        // Enter poles: array of double coefficients
        public void setDenominator(double[] denom){
            if(denom!=null){
                this.dDeg = denom.length-1;
                if(this.dDeg>0){
                    this.denomCoeff = new ComplexPoly(denom);;
                    this.denomRoots = Complex.oneDarray(dDeg);
                    if(!this.noZeros){
                        this.mDeg = (int)Math.max(nDeg, dDeg);
                    }
                    else{
                        this.mDeg = dDeg;
                    }
                    this.noPoles = false;
                }
                if(this.noPoles)throw new IllegalArgumentException("No poles entered");
            }
        }


        // Sets plot to s-plane plot
        public void setS(){
                this.sORz=1;
        }

        // Sets plot to z-plane plot
        public void setZ(){
                this.sORz=2;
                this.zCircle=true;
        }

        // Sets plot a unit radius circle.
        public void setCircle(){
                this.zCircle=true;
                if(this.sORz!=2)sORz=2;
        }

        // Unsets plot a unit radius circle.
        public void unsetCircle(){
                this.zCircle=false;
        }

        // Calculate roots and plot and write to text file
        // Plot title given
        public void pzPlot(String title){
                double[] zerosReal = null;
                double[] zerosImag = null;
                double[] polesReal = null;
                double[] polesImag = null;
                double[] xAxisIfRealZero = null;
                double[] yAxisIfRealZero = null;
                double[] xAxisIfImagZero = null;
                double[] yAxisIfImagZero = null;
                double[] xAxisCircle1 = null;
                double[] yAxisCircle1 = null;
                double[] xAxisCircle2 = null;
                double[] yAxisCircle2 = null;
                double absReal = 0.0D;
                double absImag = 0.0D;
                double zeroLimit = 1e-5;
                int ncirc = 600;
                double stp = 2.0/(double)(ncirc-1);
                int maxPoints = 0;

                int mm=0;
                if(this.nDeg>0){
                    mm++;
                    zerosReal = new double[this.nDeg];
                    zerosImag = new double[this.nDeg];
                    this.numerRoots = this.numerCoeff.roots();
                    for(int i=0; i<this.nDeg; i++){
                        zerosReal[i] = this.numerRoots[i].getReal();
                        zerosImag[i] = this.numerRoots[i].getImag();
                        if(!numerRoots[i].isZero()){
                            absReal = Math.abs(zerosReal[i]);
                            absImag = Math.abs(zerosImag[i]);
                            if(absReal>absImag){
                                if(absImag<zeroLimit*absReal)zerosImag[i]=0.0D;
                            }
                            else{
                                if(absReal<zeroLimit*absImag)zerosReal[i]=0.0D;
                            }
                        }
                        if(zerosReal[i]!=0.0D)this.noReal=false;
                        if(zerosImag[i]!=0.0D)this.noImag=false;
                    }
                    maxPoints = nDeg;
                }

                if(this.dDeg>0){
                    mm++;
                    polesReal = new double[this.dDeg];
                    polesImag = new double[this.dDeg];
                    this.denomRoots = this.denomCoeff.roots();
                      for(int i=0; i<this.dDeg; i++){
                        polesReal[i] = this.denomRoots[i].getReal();
                        polesImag[i] = this.denomRoots[i].getImag();
                        if(!denomRoots[i].isZero()){
                            absReal = Math.abs(polesReal[i]);
                            absImag = Math.abs(polesImag[i]);
                            if(absReal>absImag){
                                if(absImag<zeroLimit*absReal)polesImag[i]=0.0D;
                            }
                            else{
                                if(absReal<zeroLimit*absImag)polesReal[i]=0.0D;
                            }
                        }
                        if(polesReal[i]!=0.0D)this.noReal=false;
                        if(polesImag[i]!=0.0D)this.noImag=false;
                    }
                    if(dDeg>maxPoints)maxPoints=dDeg;
                }

                if(this.noReal){
                    mm++;
                    xAxisIfRealZero = new double[2];
                    xAxisIfRealZero[0]=1.D;
                    xAxisIfRealZero[1]=-1.0D;
                    yAxisIfRealZero = new double[2];
                    yAxisIfRealZero[0]=0.0D;
                    yAxisIfRealZero[1]=0.0D;
                    if(2>maxPoints)maxPoints=2;
                }

                if(this.noImag){
                    mm++;
                    xAxisIfImagZero = new double[2];
                    xAxisIfImagZero[0]=0.0D;
                    xAxisIfImagZero[1]=0.0D;
                    yAxisIfImagZero = new double[2];
                    yAxisIfImagZero[0]=1.0D;
                    yAxisIfImagZero[1]=-1.0D;
                    if(2>maxPoints)maxPoints=2;
                }

                if(this.zCircle){
                    mm+=2;
                    xAxisCircle1[0]=-1.0;
                    yAxisCircle1[0]=0.0;
                    xAxisCircle2[0]=-1.0;
                    yAxisCircle2[0]=0.0;
                    for(int i=1; i<ncirc; i++){
                        xAxisCircle1[i]=xAxisCircle1[i-1]+stp;
                        yAxisCircle1[i]=Math.sqrt(1.0-xAxisCircle1[i]*xAxisCircle1[i]);
                        xAxisCircle2[i]=xAxisCircle2[i-1]+stp;
                        yAxisCircle2[i]=-yAxisCircle1[i];
                    }
                    if(ncirc>maxPoints)maxPoints=ncirc;
                }
                int ii = 0;

                // Create array for data to be plotted
                double[][] data = PlotGraph.data(mm, maxPoints);
                boolean[] trim = new  boolean[mm];
                boolean[] minmax = new  boolean[mm];
                int[] line = new int[mm];
                int[] point = new int[mm];

                // Fill above array with data to be plotted
                ii=0;
                if(this.nDeg>0){
                        line[ii]=0;
                        point[ii]=1;
                        trim[ii]=false;
                        minmax[ii]=true;
                        for(int i=0; i<nDeg; i++){
                            data[2*ii][i]=zerosReal[i];
                            data[2*ii+1][i]=zerosImag[i];
                        }
                        ii++;
                }
                if(this.dDeg>0){
                        line[ii]=0;
                        point[ii]=7;
                        trim[ii]=false;
                        minmax[ii]=true;
                        for(int i=0; i<dDeg; i++){
                            data[2*ii][i]=polesReal[i];
                            data[2*ii+1][i]=polesImag[i];
                        }
                        ii++;
                }
                if(this.zCircle){
                        line[ii]=2;
                        point[ii]=0;
                        trim[ii]=true;
                        minmax[ii]=false;
                        for(int i=0; i<ncirc; i++){
                            data[2*ii][i]=xAxisCircle1[i];
                            data[2*ii+1][i]=yAxisCircle1[i];
                        }
                        ii++;
                        line[ii]=2;
                        point[ii]=0;
                        trim[ii]=true;
                        minmax[ii]=false;
                        for(int i=0; i<ncirc; i++){
                            data[2*ii][i]=xAxisCircle2[i];
                            data[2*ii+1][i]=yAxisCircle2[i];
                        }
                        ii++;
                }
                if(this.noReal){
                        line[ii]=0;
                        point[ii]=0;
                        trim[ii]=false;
                        minmax[ii]=true;
                        for(int i=0; i<2; i++){
                            data[2*ii][i]=xAxisIfRealZero[i];
                            data[2*ii+1][i]=yAxisIfRealZero[i];
                        }
                        ii++;
                }
                if(this.noImag){
                        line[ii]=0;
                        point[ii]=0;
                        trim[ii]=false;
                        minmax[ii]=true;

                        for(int i=0; i<2; i++){
                            data[2*ii][i]=xAxisIfImagZero[i];
                            data[2*ii+1][i]=yAxisIfImagZero[i];
                        }
                        ii++;
                }

                // Create an instance of PlotGraph with above data
                PlotGraph pg = new PlotGraph(data);
                pg.setLine(line);
                pg.setPoint(point);
                pg.setTrimOpt(trim);
                pg.setMinMaxOpt(minmax);
                pg.setXlowFac(0.0D);
                pg.setYlowFac(0.0D);
                pg.setNoOffset(true);

                switch(sORz){
                        case 0:
                                pg.setGraphTitle("Pole Zero Plot: "+title);
                                pg.setXaxisLegend("Real part of s or z");
                                pg.setYaxisLegend("Imaginary part of s or z");
                                break;
                        case 1:
                                pg.setGraphTitle("Pole Zero Plot (s-plane): "+title);
                                pg.setXaxisLegend("Real part of s");
                                pg.setYaxisLegend("Imaginary part of s");
                                break;
                        case 2:
                                pg.setGraphTitle("Pole Zero Plot (z-plane): "+title);
                                pg.setXaxisLegend("Real part of z");
                                pg.setYaxisLegend("Imaginary part of z");
                                break;
                }

                // Plot poles and zeros
                pg.plot();

                // Open and write an output file

                Complex[] numval = null;
                Complex[] denval = null;

                FileOutput fout = new FileOutput("PoleZeroOutput.txt");

                fout.println("Output File for Program PlotPoleZero");
                if(this.sORz==1)fout.println("An s-plane plot");
                if(this.sORz==2)fout.println("A z-plane plot");
                fout.dateAndTimeln(title);
                fout.println();

                if(!this.noZeros){
                    numval = numerCoeff.polyNomCopy();
                    fout.println("Numerator polynomial coefficients");
                    for(int i=0;i<=nDeg;i++){
                        fout.print(numval[i].toString());
                        if(i<nDeg){
                            fout.printcomma();
                            fout.printsp();
                        }
                    }
                    fout.println();
                    fout.println();
                }

                if(!this.noPoles){
                    denval = denomCoeff.polyNomCopy();
                    fout.println("Denominator polynomial coefficients");
                    for(int i=0;i<=dDeg;i++){
                        fout.print(denval[i].toString());
                        if(i<dDeg){
                            fout.printcomma();
                            fout.printsp();
                        }
                    }
                    fout.println();
                    fout.println();
                }

                fout.println("Numerator roots (zeros)");
                    if(nDeg<1){
                        fout.println("No zeros");
                    }
                    else{
                        for(int i=0;i<nDeg;i++){
                            fout.print(Complex.truncate(numerRoots[i],6));
                            if(i<nDeg-1){
                                fout.printcomma();
                                fout.printsp();
                            }
                        }
                        fout.println();
                        fout.println();
                    }

                    fout.println("Denominator roots (poles)");
                    if(dDeg<1){
                        fout.println("No poles");
                    }
                    else{
                        for(int i=0;i<dDeg;i++){
                            fout.print(Complex.truncate(denomRoots[i],6));
                            if(i<dDeg-1){
                                fout.printcomma();
                                fout.printsp();
                            }
                        }
                        fout.println();
                        fout.println();
                    }

                    if(this.sORz==2){
                        fout.println("Denominator pole radial distances on the z-plane");
                        if(dDeg<1){
                            fout.println("No poles");
                        }
                        else{
                            for(int i=0;i<dDeg;i++){
                                fout.print(Fmath.truncate(denomRoots[i].abs(),6));
                                if(i<dDeg-1){
                                    fout.printcomma();
                                    fout.printsp();
                                }
                            }
                        }
                        fout.println();
                        fout.println();
                    }

                    boolean testroots=true;
                    if(this.sORz==1){
                        for(int i=0;i<dDeg;i++){
                            if(denomRoots[i].getReal()>0)testroots=false;
                        }
                        if(testroots){
                                fout.println("All pole real parts are less than or equal to zero - stable system");
                        }
                        else{
                                fout.println("At least one pole real part is greater than zero - unstable system");
                        }
                    }

                    if(this.sORz==2){
                        for(int i=0;i<dDeg;i++){
                                if(Fmath.truncate(denomRoots[i].abs(),6)>1.0)testroots=false;
                        }
                        if(testroots){
                                fout.println("All pole distances from the z-plane zero are less than or equal to one - stable system");
                        }
                        else{
                                fout.println("At least one pole distance from the z-plane zero is greater than one - unstable system");
                        }
                }

                fout.println();
                fout.println("End of file");
                fout.close();

        }

        // Calculate roots and plot and write to text file
        // No plot title given
        public void pzPlot(){
                String title = "no file title provided";
                pzPlot(title);
        }

}
