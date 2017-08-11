/* emxTask.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: /java/JPOsrc/custom/${CLASSNAME}.java 1.1 Fri Dec 19 16:45:25 2008 GMT ds-panem Experimental$
*/

import matrix.db.Context;

/**
 * The <code>emxTask</code> class represents the Task JPO
 * functionality for the AEF type.
 *
 * @version AEF 10.0.SP4 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxChangeTask_mxJPO extends emxChangeTaskBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     * @grade 0
     */
    public emxChangeTask_mxJPO (Context context, String[] args) throws Exception {
      super(context, args);
    }

    /**
     * Constructor with no arguments
     */
    public emxChangeTask_mxJPO() {
        super();
    }
}
