//
// emxCommonProjectAccessListBase.java
//
// Copyright (c) 2002-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//
//Revision 1.2  2004/01/20  16:30:31  rcheluva
//Fixed Bug 281580
//Uncommentd the Company Visibilty checks.
//
//Revision 1.1  2003/12/01  19:37:26  rmuralidhar
//Initial revision
//
//

import java.io.*;
import java.util.*;
import matrix.db.*;
import matrix.util.*;

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
public class emxCommonProjectAccessListBase_mxJPO extends DomainObject
{
    /** The project type relative to this object. */
    static protected final String SELECT_PROJECT_TYPE = "from[" +
        RELATIONSHIP_PROJECT_ACCESS_LIST + "].to." + SELECT_TYPE;
    /** The project visibility attribute relative to this object. */
    static protected final String SELECT_PROJECT_VISIBILITY = "from[" +
        RELATIONSHIP_PROJECT_ACCESS_LIST + "].to." +getAttributeSelect(ATTRIBUTE_PROJECT_VISIBILITY);
    /** The project company id relative to this object. */
    static protected final String SELECT_PROJECT_COMPANY_ID = "from[" +
        RELATIONSHIP_PROJECT_ACCESS_LIST + "].to." +
        "to[" + RELATIONSHIP_COMPANY_PROJECT + "].from.id"; // ProjectSpace.SELECT_COMPANY_ID;
    /** The project template company id relative to this object. */
    static protected final String SELECT_TEMPLATE_COMPANY_ID = "from[" +
        RELATIONSHIP_PROJECT_ACCESS_LIST + "].to." +
        "to[" + RELATIONSHIP_COMPANY_PROJECT_TEMPLATES + "].from.id"; // ProjectTemplate.SELECT_COMPANY_ID;

    protected boolean accessChecked = false;
    protected boolean access = false;

    /**
     * Constructs a new emxProjectAccessList JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args an array of String arguments for this method
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.1
     */
    public emxCommonProjectAccessListBase_mxJPO (Context context, String[] args)
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
     * @param args - not used
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     * @grade 0
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
            MqlUtil.mqlCommand(context, "set env $1 $2 $3", "global", "IGNORE_ACCESS", "TRUE");

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
                type.equals(TYPE_PROJECT_TEMPLATE) ||
                mxType.isOfParentType(context,type,DomainConstants.TYPE_PROJECT_TEMPLATE) ||
                type.equals(TYPE_PART_QUALITY_PLAN_TEMPLATE) ||
                type.equals(TYPE_PART_QUALITY_PLAN))
            {
                String personCompanyId = MqlUtil.mqlCommand(context, "get env $1","PERSONCOMPANYID");
                
                if ("".equals(personCompanyId))
                {
                    Person person = null;
                    personCompanyId = person.getPerson(context).getCompanyId(context);
                    MqlUtil.mqlCommand(context, "set env $1 $2", "PERSONCOMPANYID", personCompanyId);
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

            MqlUtil.mqlCommand(context, "unset env $1 $2", "global", "IGNORE_ACCESS");
        }

        accessChecked = true;

        return access;
    }
}
