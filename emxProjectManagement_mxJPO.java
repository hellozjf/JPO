/* emxProjectManagement.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.19.2.1 Thu Dec  4 07:56:05 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.19 Wed Oct 22 15:50:33 2008 przemek Experimental przemek $
*/

import java.io.*;
import java.util.*;
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;

/**
 * The <code>emxProjectManagement</code> class represents the Project Management
 * JPO functionality for the AEF type.
 *
 * @version AEF 9.5.2.0 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxProjectManagement_mxJPO extends emxProjectManagementBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since PMC 10.0.0.0
     */
    public emxProjectManagement_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

    /**
     * Constructs a new emxProjectManagement JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param String the business object id
     * @throws Exception if the operation fails
     * @since 10.0.0.0
     */
    public emxProjectManagement_mxJPO (String id)
        throws Exception
    {
        // Call the super constructor
        super(id);
    }
}
