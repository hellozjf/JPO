/*   emxBusinessUnitBase
**
**   Copyright (c) 2003-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   This JPO contains the implementation of emxPart
**
*/

import java.util.HashMap;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.common.BusinessUnit;
import com.matrixone.apps.common.Organization;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * The <code>emxBusinessUnitBase</code> class contains implementation code for emxBusinessUnitBase.
 *3 * @version Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxBusinessUnitBase_mxJPO extends emxDomainObject_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since EC 9.5.JCI.0
     * @grade 0
     */
    public emxBusinessUnitBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
  
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns nothing
     * @throws Exception if the operation fails
     * @since EC 9.5.JCI.0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CompanyBase.SpecifyMethodOnPartInvocation", context.getLocale().getLanguage()));
        }
        return 0;
    }

    /**
        * Gets the Departments for the Business Unit.
        *
        * @param context The Matrix Context.
        * @param selectStmts The list of selects.
        * @return maplist of Departments
        * @throws FrameworkException If the operation fails.
        * @since AEF Rossini
        * @grade 0
        */
        public MapList getDepartments (Context context,String[] args)
            throws Exception
       {
          HashMap paramMap = (HashMap)JPO.unpackArgs(args);
          
          String objectId = (String) paramMap.get("objectId");
          MapList mapList = new MapList();
        
          try {
              BusinessUnit businessUnitObj = new BusinessUnit(objectId);
              StringList selectStmts = new StringList(1);
              selectStmts.addElement(businessUnitObj.SELECT_ID);
   
              mapList = businessUnitObj.getDepartments(context, selectStmts);

          }
          
          catch (FrameworkException Ex) {
               throw Ex;
         }
         return mapList;
   
      } 

  
	 /**
   	  * Expand program for select Business Unit  
	  * If isFrom and relationshipName names are passed and if the context object is already connected
	  * to a Business Unit it will be disabled for selection.
	  */  	  
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList selectionBusinessUnitExpandProgram(Context context, String[] args) throws FrameworkException {
            try {
                Map programMap = (Map) JPO.unpackArgs(args);
                
                boolean isAddExisting = "true".equalsIgnoreCase((String)programMap.get("isAddExisting"));
                String isFrom= (String) programMap.get("isFrom");
                String relationshipName = (String) programMap.get("relationshipName");
                
                String SELECT_CURRENT_OBJECT_ID = null;
                if(!UIUtil.isNullOrEmpty(isFrom) && !UIUtil.isNullOrEmpty(relationshipName)) {
                    relationshipName = PropertyUtil.getSchemaProperty(context,relationshipName);
                    SELECT_CURRENT_OBJECT_ID = "true".equalsIgnoreCase(isFrom) ?
                                                "from["+relationshipName+"].to.id" : "to["+relationshipName+"].from.id";
                     isAddExisting = true;
                }
                
                StringList selectables = new StringList(2);
                selectables.add(SELECT_ID);
                if(SELECT_CURRENT_OBJECT_ID != null)
                    selectables.add(SELECT_CURRENT_OBJECT_ID);
                
                String orgId = (String) programMap.get("objectId");
                Organization org = new Organization(orgId);
                MapList businessUnits = org.getBusinessUnits(context, 1, selectables, false);
                
                if(isAddExisting) {
                    Map reqMap = (Map) programMap.get("RequestValuesMap");
                    String parentObjId = ((String[])reqMap.get("objectId"))[0];
                    
                    for (int i = 0; i < businessUnits.size(); i++) {
                        Map bu = (Map) businessUnits.get(i);
                        Object connectedObject = bu.get(SELECT_CURRENT_OBJECT_ID);
                        if(connectedObject == null) {
                            continue;
                        } else if (connectedObject instanceof StringList) {
                            if(((StringList)connectedObject).contains(parentObjId)) {
                                bu.put("disableSelection", "true");
                            }                            
                        } else if(connectedObject.equals(parentObjId)) {
                            bu.put("disableSelection", "true");
                        }
                    }
                }
                return businessUnits;
            } catch (Exception e) {
                throw new FrameworkException(e);
            }        
        }
  
   /**
         * Gets the Persons connected to the Company of the context Business Unit.
         *
         * @param context The Matrix Context.
         * @param args - the JPO standard args
         * @return StringList of Person
         * @throwsException If the operation fails.
         */
        @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
        public static Object includePersonsInCompany(Context context, String[] args ) throws FrameworkException
       	{
       		StringList includeOID = new StringList();
       		try
       		{
       			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
       			String objectId = (String)paramMap.get("objectId");
       			String parentOID = (String)paramMap.get("parentOID");
       			StringList sRelSelect = new StringList(1);
       			StringList selectables = new StringList(2);
                selectables.add(SELECT_ID);
       			
       			if (parentOID == null) {
       				DomainObject object = DomainObject.newInstance(context, objectId);
       				parentOID = object.getInfo(context, "to["+ DomainConstants.RELATIONSHIP_DIVISION + "].from.id");
       			}
       			DomainObject domObj = DomainObject.newInstance(context, parentOID);
       			includeOID = domObj.getInfoList(context,"from["+DomainConstants.RELATIONSHIP_MEMBER+"].to.id");
       			return includeOID;
       		}
       		catch (Exception ex)
       		{
       			throw new FrameworkException(ex);
       		}
       	}
}
