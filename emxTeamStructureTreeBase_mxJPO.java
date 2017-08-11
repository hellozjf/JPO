/*
 *  emxTeamStructureTreeBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */
import matrix.db.*;
import matrix.util.*;
import java.io.*;
import java.util.*;
import java.lang.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.common.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.*;
import com.matrixone.apps.common.util.*;

/**
 * @version AEF Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxTeamStructureTreeBase_mxJPO extends emxDomainObject_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public emxTeamStructureTreeBase_mxJPO (Context context, String[] args)
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
            throw new FrameworkException(ComponentsUtil.i18nStringNow("emxTeamCentral.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
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

    public static MapList getWorkSpaceFolderList(Context context, String[] args) throws
        Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap   = (HashMap)programMap.get("paramMap");
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String objectId    = (String)paramMap.get("objectId");
        DomainObject domObj = DomainObject.newInstance(context,objectId);

        StringList strSel  = new StringList();
        strSel.add(DomainConstants.SELECT_TYPE);

        Map objMap     = domObj.getInfo(context, strSel);

        String objType = null;
        MapList result = new MapList();
        if(objMap!=null)
        {
            objType = (String)objMap.get(DomainConstants.SELECT_TYPE);
        }

        StringList sSelects = new StringList();
		String RELATIONSHIP_LINKED_FOLDERS = PropertyUtil.getSchemaProperty(context, "relationship_LinkedFolders");
        String MENU_PROJECTVAULT = PropertyUtil.getSchemaProperty(context,"menu_TMCtypeProjectVault");
        if( objType != null && objType.equals(DomainObject.TYPE_WORKSPACE))
        {
            try
            {
                sSelects.add(DomainObject.SELECT_ID)  ;
                sSelects.add(DomainObject.SELECT_NAME);
				StringList selectRelStmts  = new StringList();
				selectRelStmts.add(DomainObject.SELECT_RELATIONSHIP_ID);
                result= (MapList)domObj.getRelatedObjects(context,
                                                        DomainObject.RELATIONSHIP_WORKSPACE_VAULTS+","+ RELATIONSHIP_LINKED_FOLDERS,
                                                        DomainObject.TYPE_PROJECT_VAULT,
                                                        sSelects,
                                                        selectRelStmts,
                                                        false,
                                                        true,
                                                        (short)1,
                                                        null,
                                                        null);
            } catch ( FrameworkException e){
                throw new FrameworkException(e);
            }
        }
        else
        {
            DomainObject subFolder = DomainObject.newInstance(context , objectId);
            StringList selectRelStmts  = new StringList();
			selectRelStmts.add(subFolder.SELECT_RELATIONSHIP_ID);
            StringList selectTypeStmts = new StringList();
            selectTypeStmts.add(subFolder.SELECT_NAME);
            selectTypeStmts.add(subFolder.SELECT_ID);
            try
            {
                result= (MapList)subFolder.getRelatedObjects(context,
                                                            DomainObject.RELATIONSHIP_SUBVAULTS+","+ RELATIONSHIP_LINKED_FOLDERS,
                                                            DomainObject.TYPE_PROJECT_VAULT,
                                                            selectTypeStmts,
                                                            selectRelStmts,
                                                            false,
                                                            true,
                                                            (short)1,
                                                            null,
                                                            null);
            } catch ( FrameworkException e){
                throw new FrameworkException(e);
            }
        }
        /* Added to display the proper menu for workspace vault in advance search
         * In general advance search will look for type_MENUNAME. But in our case it will be TMCtype_MENUNAME
         * For that we need to pass treeMenu with the required menu to be displayed.
         */
        for(int i=0; i<result.size(); i++)
        {
            Hashtable tempTable = (Hashtable) result.get(i);
            tempTable.put("treeMenu",MENU_PROJECTVAULT);
        }
        return result;
    }

}
