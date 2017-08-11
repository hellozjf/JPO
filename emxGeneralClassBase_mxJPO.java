/*
 *   Copyright (c) 1992-2016 Dassault Systemes.
 *   All Rights Reserved.
 *   This program contains proprietary and trade secret information of MatrixOne,
 *   Inc.  Copyright notice is precautionary only
 *   and does not evidence any actual or intended publication of such program
 *
 */

import matrix.db.Context;
import matrix.util.StringList;

import java.util.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;

import com.matrixone.apps.library.LibraryCentralConstants;


/**
 * The <code>emxGeneralClassBase</code> represents implementation of 
 * the "To Side" of "SubClass" Relationship in LC Schema
 *
 */

public class emxGeneralClassBase_mxJPO  extends emxClassification_mxJPO
{


    /**
     * Creates emxGeneralClassBase object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     *    0 - String entry for "objectId"
     * @throws Exception if the operation fails
     */

    public emxGeneralClassBase_mxJPO (Context context, String[] args) throws Exception
    {
        super (context, args);
    }


     /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int
     * @throws Exception if the operation fails
     */

    public int mxMain (Context context, String[] args) throws Exception
    {
        if (true)
        {
            throw new Exception (
                    "must specify method on emxGeneralClassBase invocation"
            );
        }

        return 0;
    }

    /**
     * Updates the 'Classification Class' and its parent objects' count as a result
     * of a classified item being revised.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     *    0 - FROMOBJECTID
     *    1 - TOOBJECTID
     *    2 - PARENTEVENT
     *    3 - NEWRELID
     * @return int the enditems count
     * @throws Exception if the operation fails
     */
    public int updateCount(Context context, String[] args) throws Exception
    {
        if(args == null || (args !=null && args.length < 3))
        {
            throw new Exception ("ERROR - Invalid number of arguments");
        }

        int ret = 0;

        //arg[0]: classification object id
        DomainObject dmObj = (DomainObject) DomainObject.newInstance(context, args[0]);
        String parentEvent = args[2];   //revise,clone,modify
        String newRelId = args[3];      //new relationship id

        //update the count as a result of a classified item being revised
        if("revise".equals(parentEvent) && newRelId != null && newRelId.length() > 0)
        {
            try
            {
                ContextUtil.startTransaction(context, true);
                String attrCountSel = "attribute[" + dmObj.ATTRIBUTE_COUNT + "]";

                String countStr = dmObj.getInfo(context, attrCountSel);
                int count = (new Integer(countStr)).intValue();
                count++;

                dmObj.setAttributeValue(context, dmObj.ATTRIBUTE_COUNT, Integer.toString(count));

                //Update other parent's count as well
                StringList busSels = new StringList();
                busSels.add(dmObj.SELECT_ID);
                busSels.add(attrCountSel);

                String rels = LibraryCentralConstants.RELATIONSHIP_SUBCLASS;
                StringBuffer types = new StringBuffer(LibraryCentralConstants.TYPE_CLASSIFICATION);
                types.append(",");
                types.append(LibraryCentralConstants.TYPE_LIBRARIES);

                MapList result = dmObj.getRelatedObjects(context,
                                          rels,
                                          types.toString(),
                                          busSels,
                                          null,
                                          true,
                                          false,
                                          (short)0,
                                          null,
                                          null);

                if(result != null && result.size() > 0)
                {
                    for(int i=0; i < result.size(); i++)
                    {
                        Map map = (Map) result.get(i);
                        String sObjId = (String) map.get(dmObj.SELECT_ID);
                        String sCount = (String) map.get(attrCountSel);
                        int objCounts = (new Integer(sCount)).intValue();
                        objCounts++;

                        DomainObject parentObj =
                          (DomainObject) DomainObject.newInstance(context,sObjId);

                        parentObj.setAttributeValue(context, dmObj.ATTRIBUTE_COUNT, Integer.toString(objCounts));
                    }
                }

                ContextUtil.commitTransaction(context);
            }catch(Exception ex)
            {
                ret = 1;
                ContextUtil.abortTransaction(context);
                ex.printStackTrace();
                throw ex;
            }
        }

        return ret;
    }

}
