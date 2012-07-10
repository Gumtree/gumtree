/*      Class ClosedLoop
*
*       This class supports the creation of a path of Black Boxes
*       i.e. of instances of BlackBox and of any of its subclasses,
*       e.g. PropIntDeriv, FirstOrder, and the methods to combine
*       these into both a single instance of BlackBox and a Vector
*       of analogue segments, digital segments and converters,
*       with a feedback path from the last box on the forward path to the first box on the forward path
*
*       Author:  Michael Thomas Flanagan.
*
*       Created: August 2002
*	    Updated: 14 May 2005
*
*       DOCUMENTATION:
*       See Michael T Flanagan's JAVA library on-line web page:
*       OpenLoop.html
*
*
*   Copyright (c) May 2005   Michael Thomas Flanagan
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

package flanagan.control;

import java.util.Vector;
import flanagan.complex.Complex;
import flanagan.complex.ComplexPoly;
import flanagan.control.OpenLoop;

public class ClosedLoop extends BlackBox{
    private OpenLoop forwardPath = new OpenLoop();  // forward path boxes
    private OpenLoop closedPath = new OpenLoop();   // full closed path boxes

    private Vector<BlackBox> feedbackPath = new Vector<BlackBox>(); // feedback path boxes
    private int nFeedbackBoxes = 0;             // number of boxes in feedback path

    private boolean checkPath = false;          // true if segment has been called
    private boolean checkNoMix = true;          // true - no ADC or DAC
    private boolean checkConsolidate = false;   // true if consolidate has been called

    // Constructor
    public ClosedLoop(){
        super("Closed Loop");
    }

    // Add box to the forward path
    public void addBoxToForwardPath(BlackBox box){
        this.forwardPath.addBoxToPath(box);
    }

    // Add box to the open path
    public void addBoxToFeedbackPath(BlackBox box){
        this.feedbackPath.addElement(box);
        this.nFeedbackBoxes++;
    }

    // Consolidate all boxes into appropriate segments and
    //  combine all boxes into either on forward path box or one closed loop box
    public void consolidate(){

        // add feedback boxes to forward path boxes
        this.closedPath = this.forwardPath.copy();
        for(int i=0; i<this.nFeedbackBoxes; i++){
            this.closedPath.addBoxToPath((BlackBox)this.feedbackPath.elementAt(i));
        }

        // combine forward path boxes
        this.forwardPath.consolidate();

        // combine closed path boxes
        this.closedPath.consolidate();

        // Calculate transfer function
        ComplexPoly fpNumer = this.forwardPath.getSnumer();
        ComplexPoly fpDenom = this.forwardPath.getSdenom();
        ComplexPoly cpNumer = this.closedPath.getSnumer();
        ComplexPoly cpDenom = this.closedPath.getSdenom();
        if(fpDenom.isEqual(cpDenom)){
            super.sNumer = fpNumer.copy();
            this.sDenom = (cpNumer.plus(fpDenom)).copy();
        }
        else{
            super.sNumer = fpNumer.times(cpDenom);
            super.sDenom = cpNumer.plus(cpDenom.times(fpDenom));
        }
        this.checkConsolidate = true;
    }

    // Return number of boxes in the forward path
    public int getNumberOfBoxesInForwardPath(){
        if(!checkConsolidate)this.consolidate();
        return this.forwardPath.getNumberOfBoxes();
    }

    // Return number of boxes in the closed path
    public int getNumberOfBoxesInClosedLoop(){
        if(!checkConsolidate)this.consolidate();
        return this.closedPath.getNumberOfBoxes();
    }

    // Return segment Vector for forward path
    public Vector getForwardPathSegmentsVector(){
        if(!checkConsolidate)this.consolidate();
        return this.forwardPath.getSegmentsVector();
    }

    // Return segment Vector for closed path
    public Vector getClosedLoopSegmentsVector(){
        if(!checkConsolidate)this.consolidate();
        return this.closedPath.getSegmentsVector();
    }

   // Return number of segments in the forward path
    public int getNumberOfSegmentsInForwardPath(){
        if(!checkConsolidate)this.consolidate();
        return this.forwardPath.getNumberOfSegments();
    }

    // Return number of segments in the closed path
    public int getNumberOfSegmentsInClosedLoop(){
        if(!checkConsolidate)this.consolidate();
        return this.closedPath.getNumberOfSegments();
    }

    // Return name of all boxes in forward path
    public String getNamesOfBoxesInForwardPath(){
        if(!checkConsolidate)this.consolidate();
        return this.forwardPath.getNamesOfBoxes();
    }

    // Return name of all boxes in closed path
    public String getNamesOfBoxesInClosedLoop(){
        if(!checkConsolidate)this.consolidate();
        return this.closedPath.getNamesOfBoxes();
    }

    // Remove all boxes from the path
    public void removeAllBoxes(){
        this.forwardPath.removeAllBoxes();
        this.closedPath.removeAllBoxes();
    }

}
