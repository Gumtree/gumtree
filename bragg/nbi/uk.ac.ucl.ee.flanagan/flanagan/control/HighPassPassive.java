/*      Class HighPassPassive
*
*       This class contains the constructor to create an instance of
*       a low pass filter:
*           V(out) = V(in)RCjomega/(1 +RCjomega)
*       and the methods needed to use this process in simulation
*       of control loops.
*
*       This class is a subclass of the superclass BlackBox.
*
*       Author:  Michael Thomas Flanagan.
*
*       Created: 21 May 2005
*       Updated:  2 July 2006
*
*
*       DOCUMENTATION:
*       See Michael T Flanagan's JAVA library on-line web page:
*       HighPassPassive.html
*
*   Copyright (c) July 2006  Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/


package flanagan.control;
import flanagan.complex.Complex;
import flanagan.complex.ComplexPoly;

public class HighPassPassive extends BlackBox{

    private double resistance = 0.0D;       // Resistance value, R ohms
    private double capacitance = 0.0D;      // Capacitance value, C farads
    private double timeConstant = 0.0D;     // Time constant, RC seconds
    private boolean setR = false;           // = true when resistance set
    private boolean setC = false;           // = true when capacitance set

    // Constructor
    // Sets time constant and order to unity
    public HighPassPassive(){
        super("Passive High Pass Filter");
        super.setSnumer(new ComplexPoly(0.0D, 1.0D));
        super.setSdenom(new ComplexPoly(1.0D, 1.0D));
        super.setZtransformMethod(1);
        super.addDeadTimeExtras();
        this.timeConstant = 1.0D;
    }


    public void setResistance(double res){
        this.resistance = res;
        this.timeConstant = res*this.capacitance;
        this.calcPolesZerosS();
        super.sNumer = ComplexPoly.rootsToPoly(this.sZeros);
        for(int i=0; i<=super.sNumerDeg;i++)super.sNumer.resetCoeff(i, super.sNumer.coeffCopy(i).times(Math.pow(this.timeConstant, i)));
        super.sDenom = ComplexPoly.rootsToPoly(this.sPoles);
        super.addDeadTimeExtras();
        this.setR = true;
    }

    public void setCapacitance(double cap){
        this.capacitance = cap;
        this.timeConstant = cap*this.resistance;
        this.calcPolesZerosS();
        super.sNumer = ComplexPoly.rootsToPoly(this.sZeros);
        for(int i=0; i<=super.sNumerDeg;i++)super.sNumer.resetCoeff(i, super.sNumer.coeffCopy(i).times(Math.pow(this.timeConstant, i)));
        super.sDenom = ComplexPoly.rootsToPoly(this.sPoles);
        super.addDeadTimeExtras();
        this.setC = true;
    }

    public void setTimeConstant(double tau){
        this.timeConstant = tau;
        this.calcPolesZerosS();
        super.sNumer = ComplexPoly.rootsToPoly(this.sZeros);
        for(int i=0; i<=super.sNumerDeg;i++)super.sNumer.resetCoeff(i, super.sNumer.coeffCopy(i).times(Math.pow(this.timeConstant, i)));
        super.sDenom = ComplexPoly.rootsToPoly(this.sPoles);
        super.addDeadTimeExtras();
    }

    public double getResistance(){
        if(this.setR){
            return this.resistance;
        }
        else{
            System.out.println("Class; HighPassPassive, method: getResistance");
            System.out.println("No resistance has been entered; zero returned");
            return 0.0D;
        }
    }

    public double getCapacitance(){
        if(this.setC){
            return this.capacitance;
        }
        else{
            System.out.println("Class; HighPassPassive, method: getCapacitance");
            System.out.println("No capacitance has been entered; zero returned");
            return 0.0D;
        }
    }

    public double getTimeConstant(){
        return this.timeConstant;
    }

    // Calculate the zeros and poles in the s-domain
    protected void calcPolesZerosS(){
            super.sZeros[0].setReal(0.0D);
            super.sPoles[0].setReal(-this.timeConstant);
    }
}

