/* emxCommonPersonSearch.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxCommonPersonSearch.java.rca 1.6 Wed Oct 22 16:21:23 2008 przemek Experimental przemek $
*/

import matrix.db.*;

/**
 * The <code>emxTask</code> class represents the Checklist JPO
 * functionality for the PRG type.
 * @since PRG 2011x
 * @version PRG 2011x
 * @author VM3.
 */
public class emxCommonPersonSearch_mxJPO extends emxCommonPersonSearchBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since PRG 2011x
     * @version PRG 2011x
     * @author VM3.
     * @grade 0
     */
    public emxCommonPersonSearch_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
}
