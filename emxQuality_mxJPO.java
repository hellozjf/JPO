/* emxQuality.java

   Copyright (c) 1992-2015 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxQuality.java.rca 1.6 Wed Oct 22 16:21:23 2008 przemek Experimental przemek $
*/

import matrix.db.*;

/**
 * The <code>emxQuality</code> class represents the Quality JPO
 * functionality for the AEF type.
 *
 * @version AEF 10.0.SP4 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxQuality_mxJPO extends emxQualityBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     * @grade 0
     */
    public emxQuality_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
}
