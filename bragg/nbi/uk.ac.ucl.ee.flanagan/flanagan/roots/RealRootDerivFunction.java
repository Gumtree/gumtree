/*
*   Interface RealRootDerivFunction
*
*   The function whose root is to be determined by a class
*   RealRoots method, and its first derivative, is supplied
*   by means of this interface, RealRootDerivFunction
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:   18 May 2003
*   UPDATE: 22 June 2003
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

// Interface for RealRoot class
// returns value of function and of the first derivative of the function whose root is required
public interface RealRootDerivFunction{
    double[] function(double x);
}
