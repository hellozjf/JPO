/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import java.util.*;
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;

public class emxEngRDOMigrationBase_mxJPO extends emxCommonMigration_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @grade 0
     */
    public emxEngRDOMigrationBase_mxJPO (Context context, String[] args)
        throws Exception
    {    	
     super(context, args);    
    }

       
    public String writeObjectId(Context context, String[] args) throws Exception
    {
    	String oid = args[0];
    	String sRetVal = "";
    	String sHostComp = PropertyUtil.getSchemaProperty(context, "role_CompanyName");
    	String sProj = DomainAccess.getDefaultProject(context);	
    	String RELATIONSHIP_CHANGE_RESPONSIBILITY = PropertyUtil.getSchemaProperty(context,"relationship_ChangeResponsibility");
    	    	
    	try
        {  

    		 StringList objectSelects = new StringList();
             objectSelects.add("to[" + DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.name");    
             objectSelects.add("to["+RELATIONSHIP_CHANGE_RESPONSIBILITY+"].from.name"); 
             objectSelects.add(DomainConstants.SELECT_TYPE);
             DomainObject domObject = new DomainObject(oid);
             Map strObjSelect = (Map)domObject.getInfo(context, objectSelects);
             String strRDOName = "";
             if(domObject.isKindOf(context, TYPE_ECR)) 
               strRDOName = (String)strObjSelect.get("to["+RELATIONSHIP_CHANGE_RESPONSIBILITY+"].from.name");
             else
               strRDOName = (String)strObjSelect.get("to[" + DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.name");
                  
           	if (!strRDOName.equals(sHostComp) )
        	{
        		  	sRetVal = oid+"|"+sProj+"|"+strRDOName;
        	} 
        }
        catch(Exception me)
        {
            throw me;
        }

        return sRetVal;
    }  
    //End of Method 
    
}
