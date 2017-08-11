/* emxProjectFinancialItemRelationshipBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.8.2.2 Thu Dec  4 07:55:21 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.8.2.1 Thu Dec  4 01:53:30 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.8 Wed Oct 22 15:49:31 2008 przemek Experimental przemek $
*/

import java.io.*;
import java.util.*;
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.program.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.common.util.*;

/**
 * The <code>emxProjectFinancialItemRelationshipBase</code> class represents the
 * Project Assessment relationship JPO functionality for the AEF type.
 *
 * @version AEF 9.5.2.0 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxProjectFinancialItemRelationshipBase_mxJPO
       extends DomainRelationship
{

    /**
     * Constructs a new emxProjectFinancialItemRelationship JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the name
     * @throws Exception if the operation fails
     * @since AEF 9.5.2.0
     */
    public emxProjectFinancialItemRelationshipBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        // Call the super constructor
        super();
        if (args != null && args.length > 0)
        {
            setName(args[0]);
        }
    }

    /**
     * When a Project Financial Item relationship is deleted, if the user
     * does not have proper permissions, verify that the user is a
     * project financial reviewer and delete the connection through super user.
     * Note: RELID is needed for this trigger as a constructor argument.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *     0 - String containing the object id
     * @return int based on success or failure
     * @throws Exception if operation fails
     * @since AEF 9.5.2.0
     */
    public int triggerDeleteOverride(Context context, String[] args)
        throws Exception
    {
        DebugUtil.debug("Entering Project Financial Item Relationship " +
                        "triggerDeleteOverride");

        int ret = 0;

        String fromObjectId = args[0];

        emxProjectSpace_mxJPO project =
                new emxProjectSpace_mxJPO(fromObjectId);

        // Check to see if user is Project Assessor
        String[] args2 = new String[] {"FINANCIAL_REVIEWER"};
        if (project.hasAccess(context, args2))
        {
            // change into superuser and make the connection
            ContextUtil.pushContext(context);
            try
            {
                // change return value as deletion will be taken care of.
                ret = 1;
                remove(context);
            }
            finally
            {
                ContextUtil.popContext(context);
            }
        }

        DebugUtil.debug("Exiting Project Financial Item Relationship " +
                        "triggerDeleteOverride: " + ret);

        return ret;
    }
}
