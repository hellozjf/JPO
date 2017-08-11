/* emxChecklist.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxTask.java.rca 1.6 Wed Oct 22 16:21:23 2008 przemek Experimental przemek $
*/

import matrix.db.*;

/**
 * The <code>emxChecklist</code> class represents the Checklist JPO
 * functionality for the PRG type.
 * @since PRG 2011x
 * @version PRG 2011x
 * @author VM3.
 */

public class emxChecklist_mxJPO extends emxChecklistBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @grade 0
     * @since PRG 2011x
     * @author VM3
     */
    public emxChecklist_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
}
