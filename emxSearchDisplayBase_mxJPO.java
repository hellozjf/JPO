/*
 *  emxSearchDisplayBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 * static const RCSID [] = "$Id: ${CLASSNAME}.java.rca 1.8 Wed Oct 22 16:02:45 2008 przemek Experimental przemek $";
 */
import java.util.HashMap;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MqlUtil;

/**
 * @exclude
 */
public class emxSearchDisplayBase_mxJPO extends emxDomainObject_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public emxSearchDisplayBase_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public int mxMain(Context context, String[] args)
        throws FrameworkException
    {
        if (!context.isConnected())
            throw new FrameworkException("not supported on desktop client");
        return 0;
    }

    /**
     * get folder list for the Workspace.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * @return Maplist of Workspace Folder names
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */

    public static String getEndItemsCount(Context context, String[] args) throws
        Exception
    {
        try{
            HashMap paramMap    = (HashMap) JPO.unpackArgs(args);
            String objectId     = (String)paramMap.get("objectId");
            String strLimit     = EnoviaResourceBundle.getProperty(context, "emxLibraryCentral.Search.ThresholdLimit");
            int iLimit          = Integer.parseInt(strLimit);
            iLimit+=1;
            strLimit = String.valueOf(iLimit);
            String strMQL       = "eval expression $1 on expand bus $2 rel $3 limit $4";
            return MqlUtil.mqlCommand(context, strMQL, "count(true)",
                                        objectId,
                                        DomainConstants.RELATIONSHIP_CLASSIFIED_ITEM,
                                        strLimit
                                      );

        }catch(Exception e) {
            e.printStackTrace();
            return "0.0";
        }

    }
}
