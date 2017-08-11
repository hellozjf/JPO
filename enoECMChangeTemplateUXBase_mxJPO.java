import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
import com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;
import matrix.util.StringList;


public class enoECMChangeTemplateUXBase_mxJPO extends emxDomainObject_mxJPO {

	public enoECMChangeTemplateUXBase_mxJPO(Context context, String[] args)
			throws Exception {
		super(context, args);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Gets the Change Templates as per the Context User Visibility. - As per "Member" relationship
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 *           0 -  HashMap containing one String entry for key "objectId"
	 * @return        a <code>MapList</code> object having the list of Change Templates, Object Id of Change Template objects.
	 * @throws        Exception if the operation fails
	 * @since         ECM R419HF2
	 **
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getMyTemplatesView(Context context, String[] args) throws Exception
	{

		HashSet<String> sOrgSet 		= 		new HashSet<String>();
		MapList sTemplateList 			= 		new MapList();
		MapList sFinalTemplateList      = 		new MapList();
		try
		{
			String loggedInPersonId 	= 		PersonUtil.getPersonObjectID(context);
			boolean isChangeAdmin 		= 		ChangeUtil.hasChangeAdministrationAccess(context);

			DomainObject dmObj 			= 		DomainObject.newInstance(context);
			sOrgSet.add(loggedInPersonId); //To get Personal Templates, adding the person ID


			String sObjectId = "";
			String sOwner ="";
			String sMemberOrgId = "";
			String sParentOrgID = "";
			String sChildOrgID = "";


			StringBuffer selectTemplate = 		new StringBuffer("from[");
			selectTemplate.append(ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES);
			selectTemplate.append("].to.id");


			StringList sSelectList = new StringList();
			sSelectList.add(selectTemplate.substring(0));
			sSelectList.add(SELECT_ID);
			sSelectList.add(SELECT_OWNER);


			StringBuffer selectMemberOrg = 		new StringBuffer("to[");
			selectMemberOrg.append(RELATIONSHIP_MEMBER);
			selectMemberOrg.append("].from.id");

			//Getting Member Organizations object IDs
			dmObj.setId(loggedInPersonId);
			StringList sMemberOrgList = dmObj.getInfoList(context, selectMemberOrg.substring(0));



			Iterator sItr = sMemberOrgList.iterator();
			while(sItr.hasNext())
			{
				sMemberOrgId = (String)sItr.next();
				sOrgSet.add(sMemberOrgId);

				//Getting the above Parent Organizations Object IDs
				DomainObject orgObj = new DomainObject(sMemberOrgId);
				MapList sParentOrgList = orgObj.getRelatedObjects(context,
						RELATIONSHIP_DIVISION+","
						+RELATIONSHIP_COMPANY_DEPARTMENT,
						TYPE_ORGANIZATION,
						new StringList(SELECT_ID),
						null,
						true,
						false,
						(short)0,
						EMPTY_STRING,
						EMPTY_STRING,
						null,
						null,
						null);
				Iterator sParentOrgItr = sParentOrgList.iterator();
				while(sParentOrgItr.hasNext())
				{
					Map tempMap = (Map)sParentOrgItr.next();
					sParentOrgID = (String)tempMap.get(SELECT_ID);
					sOrgSet.add(sParentOrgID);
				}

				if(isChangeAdmin)
				{
					//Getting Business Units and Departments object IDs
					Company sCompanyObj = new Company(sMemberOrgId);
					MapList sOrgList = sCompanyObj.getBusinessUnitsAndDepartments(context, 0, new StringList(SELECT_ID), false);
					Iterator sOrgItr = sOrgList.iterator();
					while(sOrgItr.hasNext())
					{
						Map tempMap = (Map)sOrgItr.next();
						sChildOrgID = (String)tempMap.get(SELECT_ID);
						sOrgSet.add(sChildOrgID);
					}
				}

			}
			String[] arrObjectIDs = (String[])sOrgSet.toArray(new String[0]);

			//getting Templates connected to each organization/person
			sTemplateList = DomainObject.getInfo(context, arrObjectIDs, sSelectList);


			Iterator sTempItr = sTemplateList.iterator();
			while(sTempItr.hasNext())
			{
				Map newMap = (Map)sTempItr.next();
				sObjectId = (String)newMap.get(selectTemplate.substring(0));
				sOwner = (String)newMap.get("owner");
				if(!UIUtil.isNullOrEmpty(sObjectId))
				{
					StringList sList = FrameworkUtil.split(sObjectId,"\7");
					Iterator sListItr = sList.iterator();
					while(sListItr.hasNext())
					{
						Map sTempMap = new HashMap();
						sObjectId = (String)sListItr.next();
						sTempMap.put("id", sObjectId);
						sTempMap.put("owner", sOwner);
						sFinalTemplateList.add(sTempMap);
					}


				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new FrameworkException(e);
		}
		return sFinalTemplateList;
	}//end of method

}
