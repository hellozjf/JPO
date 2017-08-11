// emxProjectAccessListBase.java
//
// Copyright (c) 2002-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//
// static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.10.2.2 Thu Dec  4 07:55:01 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.10.2.1 Thu Dec  4 01:53:11 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.10 Wed Oct 22 15:49:23 2008 przemek Experimental przemek $
//

import java.io.*;
import java.util.*;
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.program.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.common.util.*;
import com.matrixone.apps.common.Person;

/**
 * The <code>emxProjectAccessListBase</code> class represents the Project Access
 * List JPO functionality for the AEF type.
 *
 * @version AEF 9.5.1.1 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxProjectAccessListBase_mxJPO extends DomainObject
{
    /** The project type relative to this object. */
    static protected final String SELECT_PROJECT_TYPE = "from[" +
        RELATIONSHIP_PROJECT_ACCESS_LIST + "].to." + SELECT_TYPE;
    /** The project visibility attribute relative to this object. */
    static protected final String SELECT_PROJECT_VISIBILITY = "from[" +
        RELATIONSHIP_PROJECT_ACCESS_LIST + "].to." +ProjectSpace.SELECT_PROJECT_VISIBILITY;
    /** The project company id relative to this object. */
    static protected final String SELECT_PROJECT_COMPANY_ID = "from[" +
        RELATIONSHIP_PROJECT_ACCESS_LIST + "].to." + ProjectSpace.SELECT_COMPANY_ID;
    /** The project template company id relative to this object. */
    static protected final String SELECT_TEMPLATE_COMPANY_ID = "from[" +
        RELATIONSHIP_PROJECT_ACCESS_LIST + "].to." + ProjectTemplate.SELECT_COMPANY_ID;

    protected boolean accessChecked = false;
    protected boolean access = false;

    /**
     * Constructs a new emxProjectAccessList JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the id
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.1
     */
    public emxProjectAccessListBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        // Call the super constructor
        super();
        if (args != null && args.length > 0)
        {
            setId(args[0]);
        }
    }

    /**
     * This function verifies the user's permission for the given program.
     * This check is made by verifying the user's company matches the
     * program's company.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return boolean based on if access is available or not.
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */
    public boolean hasAccess(Context context, String args[])
        throws Exception
    {
        //program[emxProjectAccessList PROJECT_READ_ACCESS -method hasAccess
        //      -construct ${OBJECTID}] == true

        if (accessChecked)
        {
            //this is required when checkshowaccess is on to avoid infinite loop
            return access;
        }

        access = false;
        // do a quick check as a grantee.
        //System.out.println("Is granted check..." + (new Date().getTime()));
        String isGranted = getInfo(context,
                                   "grantee[" + context.getUser() + "]");
        if ("TRUE".equalsIgnoreCase(isGranted))
        {
            //System.out.println("User has proper grants");
            access = true;
        } else {
            MqlUtil.mqlCommand(context, "set env global IGNORE_ACCESS TRUE"); //PRG:RG6:R213:Mql Injection:Static Mql:18-Oct-2011

            // traverse to project and get the visibility
            StringList busSelects = new StringList(4);
            busSelects.add(SELECT_PROJECT_TYPE);
            busSelects.add(SELECT_PROJECT_VISIBILITY);
            busSelects.add(SELECT_PROJECT_COMPANY_ID);
            busSelects.add(SELECT_TEMPLATE_COMPANY_ID);

            Map info = getInfo(context, busSelects);
            String visibility = (String) info.get(SELECT_PROJECT_VISIBILITY);
            String type = (String) info.get(SELECT_PROJECT_TYPE);

            if ("Company".equals(visibility) ||
                type.equals(TYPE_PROJECT_TEMPLATE))
            {
                String personCompanyId = MqlUtil.mqlCommand(context,
                        "get env PERSONCOMPANYID"); //PRG:RG6:R213:Mql Injection:Static Mql:18-Oct-2011
                if ("".equals(personCompanyId))
                {
                    Person person = null;
                    personCompanyId =
                            person.getPerson(context).getCompanyId(context);
                  //PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:start
                	String sCommandStatement = "set env PERSONCOMPANYID $1";
                	MqlUtil.mqlCommand(context, sCommandStatement,personCompanyId); 
                 //PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:End
                }

                String projectCompanyId = (String) info.get(
                                            SELECT_PROJECT_COMPANY_ID);

                String templateCompanyId = (String) info.get(
                                            SELECT_TEMPLATE_COMPANY_ID);

                if (personCompanyId.equals(projectCompanyId) ||
                    personCompanyId.equals(templateCompanyId))
                {
                    access = true;
                }
            }

            MqlUtil.mqlCommand(context, "unset env global IGNORE_ACCESS");
        }

        accessChecked = true;

        return access;
    }
}
