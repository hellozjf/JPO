/*   emxCommonECRBase.
**
**   Copyright (c) 2004-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program.
**
**   This JPO contains the implementation of emxCommonECR.
**
*/

import matrix.db.*;
import matrix.util.*;
import java.util.*;

import com.matrixone.servlet.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;


/**
 * The <code>emxCommonECRBase</code> class contains implementation code for emxCommonECR.
 *
 * @version Common 10-5-SP1 - Copyright (c) 2004, MatrixOne, Inc.
 */

public class emxCommonECRBase_mxJPO extends emxChange_mxJPO
{
    /**
    * Constructor.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @throws Exception if the operation fails.
    * @since Common 10-5-SP1.
    */
    public emxCommonECRBase_mxJPO (Context context, String[] args)
      throws Exception
    {
        super(context, args);
    }

   /**
     * Returns whether the context user can approve signatures.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @return Boolean determines whether the context user can approve signatures.
     * @throws Exception if the operation fails.
     * @since 10-5-SP1.
    */
    public Boolean allowSignatureApproval(Context context,String args[]) throws Exception {

        if (args == null || args.length < 1) {
              throw (new IllegalArgumentException());
        }
        //Check to see if the context user is a member of the Change Board on the Product Line for this ECR
        boolean allowApproval = false;

        String objectId = args[0];
        setId(objectId);
        String selectChangeBoard = "from[" + RELATIONSHIP_ECR_MAIN_PRODUCT_AFFECTED + "].to.attribute[" + PropertyUtil.getSchemaProperty(context,"attribute_ChangeBoard") + "].value";
        String strChangeBoard = getInfo(context, selectChangeBoard);

        String sCurrentUser = context.getUser();
        if(strChangeBoard!=null && !EMPTY_STRING.equals(strChangeBoard)){
            
            String sResult = MqlUtil.mqlCommand(context, "print group $1 select $2 dump $3", strChangeBoard, "person[" + sCurrentUser + "]", "|");
    
            StringTokenizer tokens = new StringTokenizer(sResult, "|");
            while(tokens.hasMoreTokens())
            {
                String isPersoninChangeBoard = tokens.nextToken();
                if ("true".equalsIgnoreCase(isPersoninChangeBoard))
                {
                    allowApproval = true;
                    break;
                }
            }
        }
        return Boolean.valueOf(allowApproval);
    }
}
