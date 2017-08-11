/*
** emxRMTCATIAIntegrationBase
**
** Copyright (c) 1992-2016 Dassault Systemes.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
**
*/

/*
 
Change History:
Date       Change By  Release   Bug/Functionality        Details
-----------------------------------------------------------------------------------------------------------------------------
09/2010 NZR		R211	Creation
 */

import java.util.HashMap;

import matrix.db.Context;
import matrix.db.JPO;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;

/*
* This JPO contains the methods required by CATIA RFLP Navigator.  
 * @author: Nicolas Dintzner
 * @version RequirementManagement V6R2012 - Copyright (c) 2010-2016, Dassault Systemes.
 */
public class emxRMTCATIAIntegrationBase_mxJPO extends emxDomainObject_mxJPO
{

 /**
    * Create a new emxRMTCATIAIntegrationBase object.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return a emxRMTCATIAIntegrationBase object.
    * @throws Exception if the operation fails
    * @since RequirementManagement V6R2012
    * @grade 0
    */
   public emxRMTCATIAIntegrationBase_mxJPO(Context context, String[] args) throws Exception
   {
      super(context, args);
   }


   /**
    * Main entry point.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return integer status code (0 = success)
    * @throws Exception if the operation fails
    * @since RequirementManagement V6R2012
    * @grade 0
    */
   public int mxMain (Context context, String[] args) throws Exception
   {
      if (!context.isConnected())
      {
         String language = context.getSession().getLanguage();
         String strContentLabel = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Alert.FeaturesCheckFailed");
         throw  new Exception(strContentLabel);
      }
      return(0);
   }
   
	/**
	* This method is used to display objects selected in CATIA search panel in an ENOVIA structure browser table display. 
	* @param context ENOVIA MatrixOne context object
	* @param args ENOVIA MatrixOne packed arguments. Requires at least "idList" stored in one of the maps. It must contains a comma separated list of physical ID of the object to display
	* @return MapList containing the MetrixOne object ids to be displayed in the table.
	* @throws Exception if the operation fails or if the idList parameter cannot be found.
	*
	*/
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList includeCATIASelectionOIDs(Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args); 

		MapList returnedIds = new MapList();
		String physicalIds = null;

		if(programMap != null)
		{
			physicalIds = (String)programMap.get("idList");
		}	
		
		if(physicalIds == null || "".equals(physicalIds))
		{
			 throw new Exception("unable to find the idList parameter");
		}

		String[] physicalIdsArray = physicalIds.split(",");
		for(int i = 0; i < physicalIdsArray.length; i++)
		{
			if(physicalIdsArray[i] != null && !"".equals(physicalIdsArray[i] ) )
			{
				 String data = MqlUtil.mqlCommand(context, "PRINT BUS $1 SELECT $2  DUMP ", physicalIdsArray[i], "id");
				 if(data == null || "".equals(data) )
				 {
					continue;
				 }
				 HashMap<String,String> info = new HashMap<String,String>();
				 info.put("id",data);
				 info.put("level","1" );
				 returnedIds.add(info);
			}
		}
		return returnedIds;
	}	
}
