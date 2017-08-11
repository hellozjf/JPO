/* emxFinancialTemplateCategoryBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.9.2.2 Thu Dec  4 07:55:00 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.9.2.1 Thu Dec  4 01:53:09 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.9 Wed Oct 22 15:49:12 2008 przemek Experimental przemek $
*/

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
 * The <code>emxFinancialTemplateCategoryBase</code> class represents the
 * Financial Benefit Category and Financial Cost Category JPO
 * functionality for the AEF type.
 *
 * @version AEF 9.5.1.3 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxFinancialTemplateCategoryBase_mxJPO
       extends com.matrixone.apps.program.FinancialTemplateCategory
{
    /** Company Id for this category. */
    protected String _companyId = null;

    /**
     * Constructs a new emxFinancialTemplateCategory JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String id
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.3
     */
    public emxFinancialTemplateCategoryBase_mxJPO (Context context, String[] args)
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
     * This method get the company id for this category.
     *
     * @param context the eMatrix <code>Context</code> object
     * @return String company id
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.3
     */
    protected String getCompanyId(Context context)
        throws Exception
    {
        if (_companyId == null)
        {
            StringList busSelects = new StringList(1);
            busSelects.add(SELECT_ID);
            MapList mapList = getRelatedObjects(
                               context,         // context.
                               RELATIONSHIP_COMPANY_FINANCIAL_CATEGORIES + "," +
                               RELATIONSHIP_FINANCIAL_SUB_CATEGORIES,
                               QUERY_WILDCARD,  // type filter.
                               busSelects,      // business object selectables
                               null,            // relationship selectables
                               true,            // expand to direction
                               false,           // expand from direction
                               (short)0,        // level
                               null,            // object where clause
                               null);           // relationship where clause
            int size = mapList.size();
            if (size > 0)
            {
                size--;
                Map map = (Map) mapList.get(size);
                _companyId = (String) map.get(SELECT_ID);
            }
        }
        return _companyId;
    }

    /**
     * This function verifies the user's permission for the given
     * template category by checking the user's company against the
     * category template.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return boolean true or false
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.3
     */
    public boolean hasAccess(Context context, String args[])
        throws Exception
    {
        //System.out.println("Start FTC Access: " + new Date().getTime());

        //program[emxFinancialTemplateCategory -method hasAccess
        //            -construct ${OBJECTID}] == true

        boolean access = false;

        String personCompanyId = MqlUtil.mqlCommand(context,
                                    "get env global PERSONCOMPANYID");
        if ("".equals(personCompanyId))
        {
            //System.out.println("Looking up user company id...");
            com.matrixone.apps.common.Person person = null;
            personCompanyId = person.getPerson(context).getCompanyId(context);
            String sCommandStatement = "set env global $1 $2";
            MqlUtil.mqlCommand(context, sCommandStatement,"PERSONCOMPANYID",personCompanyId); 
        }
        String companyId = getCompanyId(context);

        access = personCompanyId.equals(companyId) ? true : false;

        //System.out.println("End FTC Access: " + new Date().getTime() +
        //                   " :: " + access);
        return access;
    }
}
