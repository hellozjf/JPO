/* emxGantt.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxTask.java.rca 1.6 Wed Oct 22 16:21:23 2008 przemek Experimental przemek $
*/

import matrix.db.*;

/**
 * The <code>emxTask</code> class represents the Gantt JPO functionality.
 */
public class emxGantt_mxJPO extends emxGanttBase_mxJPO {

    /**
     *Parameterized constructor.
     * 
	 * @param	context			
	 * 			Context object which is used while fetching data related application.
     * @param 	argumentArray
     * 			Array of parameters which are needed by JPO.
     * 
	 * @throws 	Exception		
	 * 			Exception can be thrown in case of constructor fail to execute.
     */
    public emxGantt_mxJPO (Context context, String[] argumentArray) throws Exception {
      super(context,argumentArray);
    }
}
