/*
 * ${CLASSNAME}
 *
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 *
 *
 */


import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Attribute;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.dassault_systemes.enovia.changeaction.factory.ChangeActionFactory;
import com.dassault_systemes.enovia.changeaction.interfaces.IChangeAction;
import com.dassault_systemes.enovia.changeaction.interfaces.IChangeActionServices;
import com.dassault_systemes.enovia.changeaction.interfaces.IProposedChanges;
import com.dassault_systemes.enovia.enterprisechangemgt.admin.ECMAdmin;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeAction;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeManagement;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeOrder;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeOrderUI;
import com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil;
import com.dassault_systemes.enovia.enterprisechangemgt.util.EffectivityUtil;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.RouteTemplate;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.StringUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * The <code>enoECMChangeOrderBase</code> class contains methods for executing JPO operations related
 * to objects of the admin type  Change.
 * @author/R3D
 * @version Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 */
public class enoECMChangeOrderBase_mxJPO extends emxDomainObject_mxJPO {


	/********************************* MAP SELECTABLES/*********************************
    /** A string constant with the value "field_Choices". */
	private static String FIELD_CHOICES = "field_choices";
	/** A string constant with the value "field_display_choices". */
	private static String FIELD_DISPLAY_CHOICES = "field_display_choices";
	private static final String INFO_TYPE_ACTIVATED_TASK  = "activatedTask";
	public static final String SUITE_KEY = "EnterpriseChangeMgt";

	private ChangeUtil changeUtil       =  null;
	private ChangeOrderUI changeOrderUI =  null;
	private ChangeOrder changeOrder     =  null;



	/**
	 * Default Constructor.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds no arguments
	 * @throws        Exception if the operation fails
	 * @since         Ecm R211
	 **
	 */
	public enoECMChangeOrderBase_mxJPO (Context context, String[] args) throws Exception {

		super(context, args);
		changeUtil    = new ChangeUtil();
		changeOrderUI = new ChangeOrderUI();
		changeOrder   = new ChangeOrder ();
	}

	/**
	 * Main entry point.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds no arguments
	 * @return        an integer status code (0 = success)
	 * @throws        Exception when problems occurred in the Common Components
	 * @since         Common X3
	 **
	 */
	public int mxMain (Context context, String[] args) throws Exception {
		if (!context.isConnected()) {
			i18nNow i18nnow = new i18nNow();
			String strContentLabel = EnoviaResourceBundle.getProperty(context,
					ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(), "EnterpriseChangeMgt.Error.UnsupportedClient");
			throw  new Exception(strContentLabel);
		}
		return  0;
	}


	/**
	 * @author
	 * Updates the Reported Against field in CO WebForm.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a MapList with the following as input arguments or entries:
	 * objectId holds the context ECR object Id
	 * New Value holds the newly selected Reported Against Object Id
	 * @throws Exception if the operations fails
	 * @since ECM-R211

	 */
	public DomainRelationship connectReportedAgainstChange (Context context, String[] args) throws Exception {

		try{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap   = (HashMap)programMap.get(ChangeConstants.PARAM_MAP);
			return connect(context,paramMap,ChangeConstants.RELATIONSHIP_REPORTED_AGAINST_CHANGE);

		}catch(Exception ex){
			ex.printStackTrace();
			throw  new FrameworkException((String)ex.getMessage());
		}
	}

	/**
	 * @author
	 * Updates the Responsible Organisation in CO WebForm.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a MapList with the following as input arguments or entries:
	 * objectId holds the context CO object Id
	 * @throws Exception if the operations fails
	 * @since ECM-R211
	 */	
	public void connectResponsibleOrganisation(Context context, String[] args) throws Exception {

		try {
			//unpacking the Arguments from variable args
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap   = (HashMap)programMap.get(ChangeConstants.PARAM_MAP);
			String objectId    = (String)paramMap.get(ChangeConstants.OBJECT_ID);
			String strROName     = (String)paramMap.get("New Value");
			changeOrder.setId(objectId);
			if(UIUtil.isNotNullAndNotEmpty(strROName)) {
				changeOrder.setPrimaryOwnership(context, ChangeUtil.getDefaultProject(context), strROName);
	  		}
		}

		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
	}
	
	/**
	 * @author
	 * Updates the Responsible Organisation in CO WebForm.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a MapList with the following as input arguments or entries:
	 * objectId holds the context CO object Id
	 * @throws Exception if the operations fails
	 * @since ECM-R211
	 */
	public DomainRelationship connectChangeCoordinator(Context context, String[] args) throws Exception {

		try {
			//unpacking the Arguments from variable args
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap   = (HashMap)programMap.get("paramMap");

			return connect(context,paramMap,ChangeConstants.RELATIONSHIP_CHANGE_COORDINATOR);
		}

		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
	}

	/**
	 * @author
	 * Updates the Responsible Organisation in CO WebForm.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a MapList with the following as input arguments or entries:
	 * objectId holds the context CO object Id
	 * @throws Exception if the operations fails
	 * @since ECM-R211
	 */
	private DomainRelationship connect(Context context, HashMap paramMap,String targetRelName) throws Exception {

		try {
			String objectId    = (String)paramMap.get(ChangeConstants.OBJECT_ID);
			changeOrder.setId(objectId);
			return changeOrder.connect(context,paramMap,targetRelName, true);
		}

		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
	}
	/**
	 * @author
	 * Generates dynamic query for Change Coordinator field
	 * @param context
	 * @param args
	 * @return String
	 * @throws Exception
	 */
	public String getChangeCoordinatorDynamicSearchQuery(Context context, String[] args) throws Exception {
		//unpacking the Arguments from variable args
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		Map fieldValuesMap = (HashMap)programMap.get(ChangeConstants.FIELD_VALUES);
		Map requestMap     = (HashMap)programMap.get(ChangeConstants.REQUEST_MAP);

		String orgId      = (String)fieldValuesMap.get(ChangeConstants.RESPONSIBLE_ORGANISATION_OID);
		if(UIUtil.isNullOrEmpty(orgId)){
		String changeObjID = (String)requestMap.get(ChangeConstants.OBJECT_ID);
			if(UIUtil.isNotNullAndNotEmpty(changeObjID)){
				orgId = ChangeUtil.getRtoIdFromName(context, DomainObject.newInstance(context, changeObjID).getInfo(context, SELECT_ORGANIZATION));
			}
		}
		return "MEMBER_ID="+orgId+":USERROLE=role_ChangeCoordinator,role_VPLMProjectLeader";

	}

	/**
	 * @author
	 * Method for including Review Route templates owned by context user
	 * @param context
	 * @param args
	 * @return String
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public Object includeReviewRouteTemplates(Context context, String[] args) throws Exception {
		String objWhere = "attribute[" + ATTRIBUTE_ROUTE_BASE_PURPOSE + "]== Review && current == Active";
		MapList routeTemplateList = (MapList)changeOrderUI.getRouteTemplates(context,objWhere);
		return  changeUtil.getStringListFromMapList(routeTemplateList,SELECT_ID);
	}
	/**
	 * @author
	 * Method for including Approval Route templates
	 * @param context
	 * @param args
	 * @return String
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public Object includeApprovalRouteTemplates(Context context, String[] args) throws Exception {
		String objWhere = "attribute[" + ATTRIBUTE_ROUTE_BASE_PURPOSE + "]== Approval && current == Active";
		MapList routeTemplateList = (MapList)changeOrderUI.getRouteTemplates(context,objWhere);
		return  changeUtil.getStringListFromMapList(routeTemplateList,SELECT_ID);
	}

	/**
	 * @author
	 * Generates dynamic query for Tech/Sr Tech Assignee Chooser fields from CO affected items table.
	 * @param context
	 * @param args
	 * @return String
	 * @throws Exception
	 */
	public String getRoleDynamicSearchQuery(Context context, Map programMap,boolean isTechRole) throws Exception {
		Map requestMap     = (HashMap)programMap.get(ChangeConstants.REQUEST_MAP);
		String changeObjID = (String)requestMap.get(ChangeConstants.OBJECT_ID);
		Map fieldValuesMap = (HashMap)programMap.get("typeAheadMap");
		String RTOId = "";
		String techRole  = "";
		if(fieldValuesMap != null & fieldValuesMap.size() > 0) {
			String caID = (String)fieldValuesMap.get(ChangeConstants.ROW_OBJECT_ID);
			RTOId = ChangeUtil.getRtoIdFromName(context, DomainObject.newInstance(context, caID).getInfo(context, SELECT_ORGANIZATION));
		}
		if(changeUtil.isNullOrEmpty(RTOId)) {
			RTOId = ChangeUtil.getRtoIdFromName(context, DomainObject.newInstance(context, changeObjID).getInfo(context, SELECT_ORGANIZATION));
		}
		return "TYPES=type_Person:CURRENT=policy_Person.state_Active:MEMBER_ID="+RTOId+":USERROLE="+techRole;
	}

	/**
	 * @author
	 * Generates dynamic query for Tech Role Column
	 * @param context
	 * @param args
	 * @return String
	 * @throws Exception
	 */
	public String getTechAssigneeRoleDynamicSearchQuery(Context context, String []args) throws Exception {
		//unpacking the Arguments from variable args
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		return getRoleDynamicSearchQuery(context, programMap,true);
	}

	/**
	 * @author
	 * Generates dynamic query for Senior Tech Role Column
	 * @param context
	 * @param args
	 * @return String
	 * @throws Exception
	 */
	public String getSrTechAssigneeoleDynamicSearchQuery(Context context, String []args) throws Exception {
		//unpacking the Arguments from variable args
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		return getRoleDynamicSearchQuery(context, programMap,false);
	}

	/**
	 * @author
	 * Generates dynamic search types for Reported Against field
	 * @param context
	 * @param args
	 * @return String
	 * @throws Exception
	 */
	public String getFieldSearchTypes(Context context, String[] args) throws Exception {
		
		String searchTypes = (String)changeUtil.getRelationshipTypes(context,ChangeConstants.RELATIONSHIP_REPORTED_AGAINST_CHANGE,true,false,null);
		return "TYPES="+searchTypes;
	}

	/**
	 * Function to get the policies from the type passed in.
	 * @param context
	 *                  eMatrix code context object
	 * @param args
	 *                  packed HashMap of request parameters
	 * @param acceptLanguage
	 *                  The Language in which the values are to be displayed.
	 * @return HashMap
	 *                   containing the actual and display values of policies
	 * @throws Exception
	 *                   if the operation fails
	 */



    public HashMap getPolicies(Context context,String []args) throws Exception {

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap)programMap.get(ChangeConstants.REQUEST_MAP);
            String type        = (String)requestMap.get(ChangeConstants.TYPE);
            String functionality = (String)requestMap.get("functionality");
            boolean  isconnectedtoCR = (UIUtil.isNotNullAndNotEmpty((String)requestMap.get("isconnectedtoCR")));
            return changeOrderUI.getPolicies(context, type, ChangeConstants.CHANGE_POLICY_DEFAULT,(ChangeConstants.FOR_RELEASE.equalsIgnoreCase(functionality)||ChangeConstants.FOR_OBSOLETE.equalsIgnoreCase(functionality) || isconnectedtoCR));
           }
    /**
     * @author
     * Generates dynamic query for new owner field in transfer ownership form
     * @param context
     * @param args
     * @return String
     * @throws Exception
     */
    public String getSearchQueryForTransferOwnership(Context context, String[] args) throws Exception
    {
    	HashMap programMap = (HashMap)JPO.unpackArgs(args);
    	Map requestMap     = (HashMap)programMap.get(ChangeConstants.REQUEST_MAP);
    	String objectId    = (String)requestMap.get(ChangeConstants.OBJECT_ID);

		if(!UIUtil.isNullOrEmpty(objectId)){
			setId(objectId);
		}
		else{
			objectId = 	(String)requestMap.get("objectId");
			StringTokenizer strTok = new StringTokenizer(objectId,",");
			if (strTok.hasMoreElements()){
				objectId = (String)strTok.nextToken();
			}
			setId(objectId);
		}
		StringList slOrgSelect = new StringList(SELECT_TYPE);
		slOrgSelect.add(SELECT_ORGANIZATION);
    	Map mapOrgId = getInfo(context, slOrgSelect);

    	String strResponsibleOrg = (String) ChangeUtil.getRtoIdFromName(context, (String)mapOrgId.get(SELECT_ORGANIZATION));
    	String strObjectType = (String) mapOrgId.get(SELECT_TYPE);

    	if(ChangeUtil.isNullOrEmpty(strResponsibleOrg) && !ChangeUtil.isNullOrEmpty(strObjectType)&& ChangeConstants.TYPE_CHANGE_ACTION.equals(strObjectType))
    	{
    		strResponsibleOrg = ChangeUtil.getRtoIdFromName(context, (String)getInfo(context, "to["+ ChangeConstants.RELATIONSHIP_CHANGE_ACTION +"].from."+SELECT_ORGANIZATION));
    	}

    	return "TYPES=type_Person:CURRENT=policy_Person.state_Active:MEMBER_ID=" + strResponsibleOrg +":USERROLE=role_ChangeCoordinator,role_VPLMProjectLeader";
    }


    /**
     * @author
     * Generates dynamic query for new owner field in transfer ownership form
     * @param context
     * @param args
     * @return String
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList excludeCurrentOwner(Context context, String[] args) throws Exception {

		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String objectId    = (String)programMap.get(ChangeConstants.OBJECT_ID);
		if(UIUtil.isNullOrEmpty(objectId)){
			objectId = 	(String)programMap.get("parentOID");
		}

		StringList excludeList = new StringList(1);
		StringList objSelects  = new StringList(2);
		objSelects.addElement(SELECT_OWNER);
		objSelects.addElement("from["+ChangeConstants.RELATIONSHIP_CHANGE_COORDINATOR+"].to.id");
		Map objInfo = (Map)DomainObject.newInstance(context, objectId).getInfo(context, objSelects);
		String owner = (String)objInfo.get(SELECT_OWNER);
		String changeCoordinator = (String)objInfo.get("from["+ChangeConstants.RELATIONSHIP_CHANGE_COORDINATOR+"].to.id");
		if(!changeUtil.isNullOrEmpty(owner) && !changeUtil.isNullOrEmpty(changeCoordinator) && owner.equals(changeCoordinator))
			excludeList   = new StringList(PersonUtil.getPersonObjectID(context, owner));

        return excludeList;
    }

    /**
     * @author
     * Generates dynamic query for new owner field in transfer ownership form
     * @param context
     * @param args
     * @return String
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
    public StringList includeOIDPerson(Context context, String[] args) throws Exception {

    	System.out.println("In: includeOIDPerson method");
    	HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String strObjectId    = (String)programMap.get(ChangeConstants.OBJECT_ID);
		if(UIUtil.isNullOrEmpty(strObjectId)){
			strObjectId = 	(String)programMap.get("parentOID");
		}
		StringList slIncludePersonOID = new StringList();
		StringList slObjSelects  = new StringList(2);
		slObjSelects.addElement(DomainConstants.SELECT_ORGANIZATION);
		slObjSelects.addElement("project");
		Map objInfo = (Map)DomainObject.newInstance(context, strObjectId).getInfo(context, slObjSelects);
		String strOrganiztion = (String)objInfo.get(DomainConstants.SELECT_ORGANIZATION);
		String strProject = (String)objInfo.get("project");
		strProject = "*"+strProject+"*"; 
		String strName = "*";
		String strWhere1 = "(to[Member].from.name=='"+strOrganiztion+"') && from[Assigned Security Context].to.name ~='"+strProject+"'";
		String sResult = EMPTY_STRING;
		if(!changeUtil.isNullOrEmpty(strOrganiztion) && !changeUtil.isNullOrEmpty(strProject))
		{
			sResult  = MqlUtil.mqlCommand(context,"temp query bus $1 $2 $3 where $4 select $5 dump $6","Person",strName,"-",strWhere1,"id","|");
		}else{
			sResult  = MqlUtil.mqlCommand(context,"temp query bus $1 $2 $3 select $4 dump $5","Person",strName,"-","id","|");
		}
		StringList slPersons = FrameworkUtil.split(sResult, "\n");
		for (int i=0;i<slPersons.size();i++) {
			String strIterVal = (String) slPersons.get(i);
				StringList slPersonDetails = FrameworkUtil.split(strIterVal, "|");
				slIncludePersonOID.add(slPersonDetails.get(3));
		
		    }
		System.out.println("out: includeOIDPerson method");
        return slIncludePersonOID;
    }
    /**
     * @author
     * This method is called from update program of CO, Create/Edit, Reviewer/Approval List fields.
     * @param context ematrix context.
     * @param args holds a Map with the following input arguments.
     * @throws Exception if any operation fails.
     */

    public void updateReviewRouteObject (Context context, String[] args) throws Exception {

         HashMap programMap = (HashMap) JPO.unpackArgs(args);
         HashMap fieldMap   = (HashMap) programMap.get(ChangeConstants.FIELD_MAP);
         HashMap paramMap   = (HashMap) programMap.get(ChangeConstants.PARAM_MAP);

		 String strNewToTypeObjId = (String)paramMap.get(ChangeConstants.NEW_OID);
		 String strOldToTypeObjId = (String)paramMap.get(ChangeConstants.OLD_OID);

         String objectId           = (String)paramMap.get(ChangeConstants.OBJECT_ID);

		 strNewToTypeObjId = (ChangeUtil.isNullOrEmpty(strNewToTypeObjId)) ?
							 (String)paramMap.get(ChangeConstants.NEW_VALUE) : strNewToTypeObjId ;

		 strOldToTypeObjId = (ChangeUtil.isNullOrEmpty(strOldToTypeObjId)) ?
							 (String)paramMap.get(ChangeConstants.OLD_VALUE) : strOldToTypeObjId ;

		new ChangeOrder(objectId).updateRouteObject(context, strNewToTypeObjId, strOldToTypeObjId, "Review");
     }
    /**
     * @author
     * This method is called from update program of CO, Create/Edit, Reviewer/Approval List fields.
     * @param context ematrix context.
     * @param args holds a Map with the following input arguments.
     * @throws Exception if any operation fails.
     */

    public void updateApprovalRouteObject (Context context, String[] args) throws Exception {

         HashMap programMap = (HashMap) JPO.unpackArgs(args);
         HashMap fieldMap   = (HashMap) programMap.get(ChangeConstants.FIELD_MAP);
         HashMap paramMap   = (HashMap) programMap.get(ChangeConstants.PARAM_MAP);

		 String strNewToTypeObjId = (String)paramMap.get(ChangeConstants.NEW_OID);
		 String strOldToTypeObjId = (String)paramMap.get(ChangeConstants.OLD_OID);

         String objectId           = (String)paramMap.get(ChangeConstants.OBJECT_ID);

		 strNewToTypeObjId = (ChangeUtil.isNullOrEmpty(strNewToTypeObjId)) ?
							 (String)paramMap.get(ChangeConstants.NEW_VALUE) : strNewToTypeObjId ;

		 strOldToTypeObjId = (ChangeUtil.isNullOrEmpty(strOldToTypeObjId)) ?
							 (String)paramMap.get(ChangeConstants.OLD_VALUE) : strOldToTypeObjId ;

		new ChangeOrder(objectId).updateRouteObject(context, strNewToTypeObjId, strOldToTypeObjId, "Approval");
     }



    /**
     * @author
     * Updates the Distribution List field in CO WebForm.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * object Id object Id of context CO.
     * New Value object Id of updated Distribution List Object
     * @throws Exception if the operations fails
     * @since ECM-R211
  */
     public DomainRelationship connectDistributionList (Context context, String[] args) throws Exception {
         //unpacking the Arguments from variable args
         HashMap programMap = (HashMap) JPO.unpackArgs(args);
         HashMap paramMap   = (HashMap) programMap.get(ChangeConstants.PARAM_MAP);
         return connect(context,paramMap,RELATIONSHIP_EC_DISTRIBUTION_LIST);
     }

     /**
      * Get the list of all Objects(CAs) which are connected to the Change object and From CAs retrieve all the affected items and send the list
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args    holds the following input arguments:
      *            0 - HashMap containing one String entry for key "objectId"
      * @return        a eMatrix <code>MapList</code> object having the list of Affected
      * @throws        Exception if the operation fails
      * @since         ECM R211
      **/

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAffectedItems(Context context, String[] args) throws Exception {


             HashMap programMap = (HashMap)JPO.unpackArgs(args);
             Map paramMap       = (HashMap)programMap.get(ChangeConstants.PARAM_MAP);
             String strParentId = (String) programMap.get("parentOID");

             changeOrder.setId(strParentId);
             return changeOrder.getProposedItems(context);
     }
    /**
     * Get the list of all Objects(CAs) which are connected to the Change object and From CAs retrieve all the affected items and send the list
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *            0 - HashMap containing one String entry for key "objectId"
     * @return        a eMatrix <code>MapList</code> object having the list of Affected
     * @throws        Exception if the operation fails
     * @since         ECM R211
     **/

   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getAffectedChangeActions(Context context, String[] args) throws Exception {


            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            Map paramMap       = (HashMap)programMap.get(ChangeConstants.PARAM_MAP);
            String strParentId = (String) programMap.get("parentOID");

            changeOrder.setId(strParentId);
            return changeOrder.getAffectedChangeActions(context);
    }



    /**
     * @author
     * this method performs the cancel process of change - The
     * Affected CAs,Affected Items, Routes,Reference Documents,Prerequisites Connected to this Particular CO are
     * Disconnected and finally change promoted to cancel state.
     *
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds the following input arguments: - The ObjectID of the
     *            Change Process
     * @throws Exception
     *             if the operation fails.
     * @since ECM R211.
     */

    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void cancelChange(Context context, String[] args) throws Exception

    {
        HashMap paramMap   = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) paramMap.get(ChangeConstants.REQUEST_MAP);
		String objectId    = changeUtil.isNullOrEmpty((String)paramMap.get(ChangeConstants.OBJECT_ID))? (String)requestMap.get(ChangeConstants.OBJECT_ID) : (String)paramMap.get(ChangeConstants.OBJECT_ID);
		String cancelReason  = changeUtil.isNullOrEmpty((String)paramMap.get("cancelReason"))? (String)requestMap.get("Reason") : (String)paramMap.get("cancelReason");



		ChangeOrder changeOrder = new ChangeOrder(objectId);
		changeOrder.cancel(context,cancelReason);
    }

    private void sendNotification(Context context,String objectId,String subjectKey,String messageKey, String propertyKey, String notificationName) throws Exception {

    	StringList ccList		  = new StringList();
		StringList bccList		  = new StringList();
		StringList lstAttachments = new StringList();

		changeOrder.setId(objectId);
		StringList toList = changeOrder.getToListForChangeProcess(context);

		emxNotificationUtilBase_mxJPO.sendNotification(context,objectId,toList, ccList, bccList, subjectKey,messageKey, lstAttachments, propertyKey, null,null,null);

		if(!UIUtil.isNullOrEmpty(notificationName)) {
			String argmail[] = {objectId,notificationName};
			JPO.invoke(context, "emxNotificationUtil", null,"objectNotification", argmail);

		}
	}
  
    /**
     * @author
     * this method performs the hold process of change.Moves all associated CAs to hold state.
     *
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds the following input arguments: - The ObjectID of the
     *            Change Process
     * @throws Exception
     *             if the operation fails.
     * @since ECM R211.
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void holdChange(Context context, String[] args)throws Exception {

        HashMap paramMap   = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap)paramMap.get(ChangeConstants.REQUEST_MAP);
		String objectId    = changeUtil.isNullOrEmpty((String)paramMap.get(ChangeConstants.OBJECT_ID))? (String)requestMap.get(ChangeConstants.OBJECT_ID) : (String)paramMap.get(ChangeConstants.OBJECT_ID);
		String holdReason  = changeUtil.isNullOrEmpty((String)paramMap.get("holdReason"))? (String)requestMap.get("Reason") : (String)paramMap.get("holdReason");
		ChangeOrder changeOrder = new ChangeOrder(objectId);
		changeOrder.hold(context,holdReason);
    }


    /**@author
	 * Resumes the Hold Changes and sends notification and updates the history
	 * @param context
	 * @throws Exception
	 */
	public void resumeChange(Context context,String[] args)throws Exception {

        HashMap paramMap   = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) paramMap.get(ChangeConstants.REQUEST_MAP);
		String objectId    = changeUtil.isNullOrEmpty((String)paramMap.get(ChangeConstants.OBJECT_ID))? (String)requestMap.get(ChangeConstants.OBJECT_ID) : (String)paramMap.get(ChangeConstants.OBJECT_ID);
		ChangeOrder changeOrder = new ChangeOrder(objectId);
		changeOrder.resume(context);
	}

	
    /**
     * @author
     * Method to return prerequisites for Change
     * @param context
     * @param args
     * @return
     * @throws Exception
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getPrerequisites(Context context, String[] args)throws Exception {

        Map programMap     = (HashMap)JPO.unpackArgs(args);
        String changeObjId = (String)programMap.get(ChangeConstants.OBJECT_ID);
        return new ChangeManagement(changeObjId).getPrerequisites(context,ChangeConstants.TYPE_CHANGE_ORDER);
    }

    /**
     * Gets all the persons with which this Change object is connected with
     * Assignee relationship
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing one String entry for key "objectId"
     * @return        a <code>MapList</code> object having the list of Assignees,their relIds and rel names for this Change Object
     * @throws        Exception if the operation fails
     * @since         ECM R211
     **
     */

    public MapList getAssignees(Context context, String[] args)
        throws Exception
        {
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            //getting parent object Id from args
            String changeObjId = (String)programMap.get(ChangeConstants.OBJECT_ID);

            return new ChangeManagement(changeObjId).getAssignees(context);

        }

    /**
     * @author
     * Gets Approval tasks and shows on Properties page of Change.
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public String getApprovalTasksOnChange(Context context, String []args) throws Exception {

    	//XSSOK
    	HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap    = (HashMap)programMap.get(ChangeConstants.PARAM_MAP);
        HashMap requestMap  = (HashMap)programMap.get(ChangeConstants.REQUEST_MAP);
        String  objectId    = (String)requestMap.get(ChangeConstants.OBJECT_ID);
        changeOrderUI.setId(objectId);
        MapList taskMapList = changeOrderUI.getCurrentAssignedTasksOnObject(context);

        boolean isExporting = (paramMap.get("reportFormat") != null);


    	// For export to CSV
    	String exportFormat = null;
    	boolean exportToExcel = false;
    	if(requestMap!=null && requestMap.containsKey("reportFormat")){
    		exportFormat = (String)requestMap.get("reportFormat");
    	}
    	if("CSV".equals(exportFormat)){
    		exportToExcel = true;
    	}

        String taskTreeActualLink     = getTaskTreeHref(context, requestMap);
        String taskApprovalActualLink = getTaskApprovalHref(context);

        String taskTreeTranslatedLink = "";
        String taskApprovalTranslatedLink = "";

        Map mapObjectInfo;String strName;String strInfoType; String taskObjectId;
        StringBuffer returnHTMLBuffer = new StringBuffer(100);
        if (taskMapList.size() > 0) {
    		if(!exportToExcel)
    		{
            returnHTMLBuffer.append("<div><table><tr><td class=\"object\">");
            returnHTMLBuffer.append(EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Message.ApprovalRequired"));
            returnHTMLBuffer.append("</td></tr><br/><tr><td>");
            returnHTMLBuffer.append(EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Message.ApprovalMessage"));
            returnHTMLBuffer.append("</td></tr></table></div>");
    		}else{
    			returnHTMLBuffer.append(EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Message.ApprovalRequired"));
    			returnHTMLBuffer.append("\n");
    			returnHTMLBuffer.append(EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Message.ApprovalMessage"));
    			returnHTMLBuffer.append("\n\n");

    		}
        }
        // Do for each object
        for (Iterator itrObjects = taskMapList.iterator(); itrObjects.hasNext();) {
            mapObjectInfo = (Map) itrObjects.next();
            strName = (String)mapObjectInfo.get("name");
            strInfoType = (String)mapObjectInfo.get("infoType");

                if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType)) {

    			if(!exportToExcel)
    			{
                    taskObjectId = (String)mapObjectInfo.get(ChangeConstants.ID);
                    taskTreeTranslatedLink = FrameworkUtil.findAndReplace(taskTreeActualLink, "${OBJECT_ID}", taskObjectId);
                    taskTreeTranslatedLink = FrameworkUtil.findAndReplace(taskTreeTranslatedLink,"${NAME}", strName);
                    returnHTMLBuffer.append("<div><table><tr><td>");

                    returnHTMLBuffer.append(EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Label.TaskAssigned"))
                                    .append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;").append(taskTreeTranslatedLink);
                    returnHTMLBuffer.append("</td></tr><br/>\n\n\n\n\n<td>");

                    taskApprovalTranslatedLink = FrameworkUtil.findAndReplace(taskApprovalActualLink, "${TASK_ID}", taskObjectId);
                    taskApprovalTranslatedLink = FrameworkUtil.findAndReplace(taskApprovalTranslatedLink, "${OBJECT_ID}",(String)mapObjectInfo.get("parentObjectId"));
                    taskApprovalTranslatedLink = FrameworkUtil.findAndReplace(taskApprovalTranslatedLink, "${STATE}",(String)mapObjectInfo.get("parentObjectState"));

                    returnHTMLBuffer.append(EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Label.ApprovalStatus"))
                                    .append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;").append(taskApprovalTranslatedLink);
                    returnHTMLBuffer.append("</td></tr></table></div>");
    			}
    			else{
    				returnHTMLBuffer.append(EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Label.TaskAssigned"))
    				.append("      ").append(strName);
    				returnHTMLBuffer.append("\n");
    				returnHTMLBuffer.append(EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Label.ApprovalStatus"))
    				.append("      ").append(EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Command.AwaitingApproval"));



                }

            }

        }
        return returnHTMLBuffer.toString();
    }


/**
 * @author
 * Prepare Task Tree HREF for the give map values
 * @param context
 * @param paramMap
 * @return
 * @throws Exception
 */
private String getTaskTreeHref(Context context,Map paramMap)throws Exception {
    StringBuffer strTreeLink = new StringBuffer();
    strTreeLink.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?relId=");
    strTreeLink.append((String)paramMap.get("relId"));
    strTreeLink.append("&parentOID=");
    strTreeLink.append((String)paramMap.get("parentOID"));
    strTreeLink.append("&jsTreeID=");
    strTreeLink.append((String)paramMap.get("jsTreeID"));
    strTreeLink.append("&suiteKey=Framework");
    strTreeLink.append("&emxSuiteDirectory=common");
    strTreeLink.append("&objectId=${OBJECT_ID}&taskName=${NAME}");
    strTreeLink.append("', '', '', 'true', 'popup', '')\"  class=\"object\">");
    strTreeLink.append("<img border=\"0\" src=\"images/iconSmallTask.gif\">${NAME}</a>");
    return strTreeLink.toString();
 }

/**
 * @author
 * Prepare Awaiting Approval HREF for the give map values
 * @param context
 * @param paramMap
 * @return
 * @throws Exception
 */
private String getTaskApprovalHref(Context context)throws Exception {
    // Form the Approve link template
    StringBuffer strTaskApproveLink = new StringBuffer(64);
    strTaskApproveLink.append("<a target=\"hiddenFrame\" class=\"object\" href=\"../common/emxLifecycleApproveRejectPreProcess.jsp?emxTableRowId=${OBJECT_ID}^${STATE}^^${TASK_ID}&objectId=${OBJECT_ID}&suiteKey=Framework");
    strTaskApproveLink.append("\"><img border='0' src='../common/images/iconActionApprove.gif' />");

    strTaskApproveLink.append(EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Command.AwaitingApproval"));
    strTaskApproveLink.append("</a>");

    return strTaskApproveLink.toString();

 }

/**
 * To create the Change Object from Create Component
 *
 * @author
 * @param context the eMatrix code context object
 * @param args packed hashMap of request parameter
 * @return Map contains change object id
 * @throws Exception if the operation fails
 * @Since ECM R211
 */
@com.matrixone.apps.framework.ui.CreateProcessCallable
public Map createChange(Context context, String[] args) throws Exception {

    HashMap programMap   = (HashMap) JPO.unpackArgs(args);
    HashMap requestValue = (HashMap) programMap.get(ChangeConstants.REQUEST_VALUES_MAP);
    HashMap requestMap   = (HashMap) programMap.get(ChangeConstants.REQUEST_MAP);

    String sType   = getStringFromArr((String[])requestValue.get("TypeActual"),0);
    String sPolicy = getStringFromArr((String[])requestValue.get("Policy"),0);
    String sVault  = getStringFromArr((String[])requestValue.get("Vault"),0);
    String sOwner  = getStringFromArr((String[])requestValue.get("Owner"),0);
    String sDescription = getStringFromArr((String[])requestValue.get("Description"),0);
    String sChangeTemplateID  = getStringFromArr((String[])requestValue.get("ChangeTemplateOID"),0);
    String effectivityExpr =  getStringFromArr((String[])requestValue.get("ChangeEffectivityOID"),0);
    String selectedObjId =  ((String)programMap.get("selectedObjIdList"));
    String fromConfiguredBOMView = "false";
    
    if (UIUtil.isNullOrEmpty(selectedObjId)) {
    	selectedObjId =  ((String) programMap.get("selectedPartsList")); // for XCE use case selected objectIds will be passed.
    	fromConfiguredBOMView = "true";
    }
       
    sType   = UIUtil.isNotNullAndNotEmpty(sType)  ? (String) programMap.get("TypeActual") : EMPTY_STRING;
    sPolicy = UIUtil.isNotNullAndNotEmpty(sPolicy)? (String) programMap.get("Policy") : EMPTY_STRING;
    sVault  = UIUtil.isNotNullAndNotEmpty(sVault) ? (String) programMap.get("Vault") : EMPTY_STRING;
    sOwner  = UIUtil.isNotNullAndNotEmpty(sOwner) ? (String) programMap.get("Owner") : EMPTY_STRING;
    String changeId   = "";
    String sInterfaceName = "";
    String[] sourceAffectedItemRowIds= null;

    Map returnMap     = new HashMap();
    boolean bAutoName = false;

    try {
        ChangeOrder change = new ChangeOrder();
        changeId = change.create(context,sType,sPolicy,sVault,sOwner);
                DomainObject coDomObj = DomainObject.newInstance(context, changeId);
        coDomObj.setDescription(context, sDescription);
        //Logic to apply Interface of Template to CO
		if(!UIUtil.isNullOrEmpty(sChangeTemplateID)){
                        sInterfaceName = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3",sChangeTemplateID,"interface","|");
			if(!UIUtil.isNullOrEmpty(sInterfaceName)){
				Iterator intrItr         = FrameworkUtil.split(sInterfaceName, "|").iterator();
				//Add all Change Template Interfaces to CO
				for(int i=0; intrItr.hasNext();i++){
					MqlUtil.mqlCommand(context, "modify bus $1 add interface $2",changeId,(String)intrItr.next());
				}
			}
		}
		
		//CUSTOMCHANGE is no longer supported
		/*
		if (ChangeUtil.isCFFInstalled(context)) {
			String createJPO = ECMAdmin.getCustomChangeCreateJPO(context, "XCE");
			
			if (UIUtil.isNotNullAndNotEmpty(selectedObjId)) {
				sourceAffectedItemRowIds = selectedObjId.split("~");
			}
		
			if (UIUtil.isNotNullAndNotEmpty(createJPO)) {
				String programName = createJPO.replaceAll(":.*$", "").trim();
				String methodName = createJPO.replaceAll("^.*:", "").trim();
			
			
				HashMap paramMap = new HashMap();
				paramMap.put("newObjectId", changeId);
			
				HashMap request = new HashMap();
				request.put("mode", "create");
				request.put("ChangeEffectivityOID", effectivityExpr);
				request.put("sourceAffectedItemRowIds", sourceAffectedItemRowIds);
				request.put("fromConfiguredBOMView", fromConfiguredBOMView);
			
				HashMap jpoMap = new HashMap();
				jpoMap.put("paramMap", paramMap);
				jpoMap.put("requestMap", request);
				String[] args1 = (String[])JPO.packArgs(jpoMap);
				
				HashMap resultMap = JPO.invoke(context, programName, null, methodName, args1, HashMap.class);
				
				if ("error".equals((String) resultMap.get("Action"))) {					
					throw new FrameworkException((String) resultMap.get("Message"));
				}
			}
		}
		*/
		
		//Added for displaying effectivity field on CO properties page for Mobile Mode
        if(UINavigatorUtil.isMobile(context)){
        	EffectivityUtil.setEffectivityOnChange(context,changeId,"");
		}
		
        returnMap.put(ChangeConstants.ID, changeId);

    } catch (Exception e) {
        e.printStackTrace();
        throw new FrameworkException(e);
    }

    return returnMap;
}

private String getStringFromArr(String[] StringArr, int intArrIndex) {
	return (StringArr != null) ? (String)StringArr[intArrIndex] : EMPTY_STRING;
}


    ///////////////////////////////////////My Changes View/////////////////////////////////////////



    /**
         * Program get all the CO (assigned via Route, Route Template, Owned)
         * @param context the eMatrix <code>Context</code> object
         * @param args    holds the following input arguments:
         *           0 -  MapList containing "objectId"
         * @return        a <code>MapList</code> object having the list of Assignees,their relIds and rel names for this Change Object
         * @throws        Exception if the operation fails
         * @since         ECM R211
         **
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getMyChangeOrders(Context context,String args[]) throws Exception{
        MapList objList = new MapList();
		try{
			String objectId 			= PersonUtil.getPersonObjectID(context);

			StringList strCOOwned 		= (StringList)getOwnedCO(context, args);
            StringList sRouteCO 		= getRouteTaskAssignedCOs(context, objectId);
            StringList sRouteTemplateCO = getRouteTemplateAssignedCOs(context, objectId);

            Set hs = new HashSet();
            hs.addAll(strCOOwned);
            hs.addAll(sRouteCO);
            hs.addAll(sRouteTemplateCO);

            Iterator itr = hs.iterator();
            String id = "";
            while(itr.hasNext()){
                id = (String)itr.next();
                Map map = new HashMap();
                map.put("id", id);
                objList.add(map);
            }
            if(objList.size()!=0)
                return objList;
           else
                return new MapList();
        }catch (Exception e) {

            throw e;
        }
    }


    /**
     * Retrieves  Change Order assigned to person via Route Task
     * @author R3D
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public StringList getRouteTaskAssignedCOs(Context context, String personObjId) throws Exception {

    	 String objSelect   = "to["+RELATIONSHIP_PROJECT_TASK+"].from."+
     			"from["+RELATIONSHIP_ROUTE_TASK+"].to."+
     			"to["+RELATIONSHIP_OBJECT_ROUTE+"|from.type=='"+ChangeConstants.TYPE_CHANGE_ORDER+"'].from.id";

         String sCO = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",personObjId,objSelect);
    	return FrameworkUtil.split(sCO, ChangeConstants.COMMA_SEPERATOR);

       }

    /**
     * Retrieves  Change Order assigned to person via Route Template where Route Base Purpose is Approval/Review
     * @author R3D
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public StringList getRouteTemplateAssignedCOs(Context context,String personObjId) throws Exception {


    	String objSelect   = "to["+RELATIONSHIP_ROUTE_NODE+"|from.type=='"+TYPE_ROUTE_TEMPLATE+"']."+
   			 "from.to["+RELATIONSHIP_INITIATING_ROUTE_TEMPLATE+"].from."+
   			 "to["+RELATIONSHIP_OBJECT_ROUTE+"|from.type=='"+ChangeConstants.TYPE_CHANGE_ORDER+"'].from.id";

      	String sCO = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",personObjId,objSelect);
    	return FrameworkUtil.split(sCO, ChangeConstants.COMMA_SEPERATOR);

       }


    /**
       * Program returns StringList of CO Object IDs if the context user is Owner/Change Initiator(Originator)/Change Coordinator.
       * @param context the eMatrix <code>Context</code> object
       * @param args    holds the following input arguments:
       *           0 -  Object
       * @return        a <code>MapList</code> object having the list of Assignees,their relIds and rel names for this Change Object
       * @throws        Exception if the operation fails
       * @since         ECM R211
       **
     */
    public Object getOwnedCO(Context context,String args[]) throws Exception{
        StringList returnList = new StringList();
        try{
            StringList objectSelects = new StringList(6);
            objectSelects.add(SELECT_ID);
            String objectWhere = "from[Change Coordinator].to.name == \""+context.getUser()+"\" || owner == \""+ context.getUser() +"\" || attribute[Originator]==\""+context.getUser()+"\"";
            MapList ownedCO = DomainObject.findObjects(context,
            		ChangeConstants.TYPE_CHANGE_ORDER,                                 // type filter
                    QUERY_WILDCARD,         // vault filter
                    objectWhere,                            // where clause
                    objectSelects);                         // object selects
            return new ChangeUtil().getStringListFromMapList(ownedCO, "id");
        }catch (Exception e) {

            throw e;
        }

    }

    /**
                             * Method to list all the "Change Actions"
                             * @param context the eMatrix <code>Context</code> object
                             * @param args    holds the following input arguments:
                             *           0 -  MapList ontaining objectID
                             * @return        a <code>MapList</code> object having the list of Assignees,their relIds and rel names for this Change Object
                             * @throws        Exception if the operation fails
                             * @since         ECM R211
                             **
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllChangeActions(Context context,String args[]) throws Exception
    {
        MapList listCA = new MapList();
        try{
            String objectId 			= PersonUtil.getPersonObjectID(context);

			StringList sOwnedCA 		= (StringList)getOwnedCAs(context, args);
            StringList sRouteCA 		= getRouteTaskAssignedCAs(context, objectId);
            StringList sRouteTemplateCA = getRouteTemplateAssignedCAs(context, objectId);
            //StringList sAssigneeCA		= getAssigneeCAs(context, objectId); //Assigned to Technical Assignee or Senior Technical Assignee

            Set hs = new HashSet();
            hs.addAll(sOwnedCA);
            hs.addAll(sRouteCA);
            hs.addAll(sRouteTemplateCA);
            //hs.addAll(sAssigneeCA);

            Iterator itr = hs.iterator();
            String id = "";
            while(itr.hasNext()){
                id = (String)itr.next();
                Map map = new HashMap();
                map.put("id", id);
                listCA.add(map);
            }
        }catch (Exception e) {
            throw e;
        }
        return listCA;
    }



    /**
     * Retrieves  Change Action assigned to person via Route Task
     * @author R3D
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public StringList getRouteTaskAssignedCAs(Context context, String personObjId) throws Exception {

    	 String objSelect   = "to["+RELATIONSHIP_PROJECT_TASK+"].from."+
     			"from["+RELATIONSHIP_ROUTE_TASK+"].to."+
     			"to["+RELATIONSHIP_OBJECT_ROUTE+"|from.type=='"+ChangeConstants.TYPE_CHANGE_ACTION+"'].from.id";

         String sCA = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",personObjId,objSelect);
    	return FrameworkUtil.split(sCA, ChangeConstants.COMMA_SEPERATOR);

       }

    /**
     * Retrieves  Change Action assigned to person via Route Template where Route Base Purpose is Approval/Review
     * @author R3D
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public StringList getRouteTemplateAssignedCAs(Context context,String personObjId) throws Exception {

    	String objSelect   = "to["+RELATIONSHIP_ROUTE_NODE+"|from.type=='"+TYPE_ROUTE_TEMPLATE+"']."+
      			 "from.to["+RELATIONSHIP_INITIATING_ROUTE_TEMPLATE+"].from."+
      			 "to["+RELATIONSHIP_OBJECT_ROUTE+"|from.type=='"+ChangeConstants.TYPE_CHANGE_ACTION+"'].from.id";


        String sCA = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",personObjId,objSelect);
    	return FrameworkUtil.split(sCA, ChangeConstants.COMMA_SEPERATOR);

       }


    /**
     * Program returns StringList of CA Object IDs if the context user is Owner/Technical Assignee/Senior Technical Assignee.
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  Object
     * @return        a <code>MapList</code> object having the list of Assignees,their relIds and rel names for this Change Object
     * @throws        Exception if the operation fails
     * @since         ECM R211
     **
   */
  public Object getOwnedCAs(Context context,String args[]) throws Exception{
      StringList returnList = new StringList();
      try{
          StringList objectSelects = new StringList(6);
          objectSelects.add(SELECT_ID);
          String objectWhere = "from[Technical Assignee].to.name == \""+context.getUser()+"\" || owner == \""+ context.getUser() +"\" || from[Senior Technical Assignee].to.name==\""+context.getUser()+"\"";

          MapList ownedCO = DomainObject.findObjects(context,
        		  ChangeConstants.TYPE_CHANGE_ACTION,                                 // type filter
                  QUERY_WILDCARD,         // vault filter
                  objectWhere,                            // where clause
                  objectSelects);                         // object selects
          return new ChangeUtil().getStringListFromMapList(ownedCO, "id");
      }catch (Exception e) {

          throw e;
      }

  }

  /**
   * Retrieves  Change Action assigned to Technical Assignee or Senior Technical Assignee
   * @author R3D
   * @param context
   * @param args
   * @return
   * @throws Exception
   */
  /*public StringList getAssigneeCAs(Context context,String personObjectId) throws Exception
  {
      MapList listCA = new MapList();
      try{
          StringList busSelects = new StringList();
          busSelects.add(SELECT_ID);
          DomainObject dmObj = DomainObject.newInstance(context);
          dmObj.setId(personObjectId);
          listCA = dmObj.getRelatedObjects(context,
					RELATIONSHIP_TECHNICAL_ASSIGNEE+","
					+RELATIONSHIP_SENIOR_TECHNICAL_ASSIGNEE,
					TYPE_CHANGE_ACTION,
					busSelects,
					null,
					true,
					false,
					(short)1,
					EMPTY_STRING,
					EMPTY_STRING);

          return new ChangeUtil().getStringListFromMapList(listCA, "id");
      }catch (Exception e) {
          throw e;
      }

  }*/


	/**
	 * Program to get CO Edit Icon in structure browser
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 *           0 -  Object
	 * @return        a <code>MapList</code> object having the list of Assignees,their relIds and rel names for this Change Object
	 * @throws        Exception if the operation fails
	 * @since         ECM R211
	 **
	 */
	public Vector showEditIconforStructureBrowser(Context context, String args[])throws FrameworkException{
		//XSSOK
		Vector columnVals = null;
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			StringList objSelects = new StringList(2);
			objSelects.addElement(SELECT_TYPE);
			objSelects.addElement(SELECT_CURRENT);
			objSelects.addElement(SELECT_ID);
			objSelects.addElement(SELECT_OWNER);
			objSelects.addElement(ChangeConstants.SELECT_TYPE_KINDOF);

			String type ="";
			String objectId ="";
			String current = "";
			String owner = "";
			String strTypeKinfOf = EMPTY_STRING;
			String strEditCO= EnoviaResourceBundle.getProperty(context,SUITE_KEY, 
					"EnterpriseChangeMgt.Label.EditCO", context.getSession().getLanguage());
			String strEditCR= EnoviaResourceBundle.getProperty(context,SUITE_KEY, 
					"EnterpriseChangeMgt.Label.EditCR", context.getSession().getLanguage());
			String strEditCA= EnoviaResourceBundle.getProperty(context,SUITE_KEY, 
					"EnterpriseChangeMgt.Label.EditCA", context.getSession().getLanguage());
			Map mapObjectInfo = null;
			StringBuffer sbEditIcon = null;

			MapList objectList = (MapList) programMap.get(ChangeConstants.OBJECT_LIST);
			StringList sObjectIDList = changeUtil.getStringListFromMapList(objectList, ChangeConstants.ID);

			if (objectList == null || objectList.size() == 0)
				return columnVals;
			else

				columnVals = new Vector(sObjectIDList.size());

			MapList COInfoList = DomainObject.getInfo(context, (String[])sObjectIDList.toArray(new String[sObjectIDList.size()]), objSelects);

			if(!COInfoList.isEmpty()){
				Iterator sItr = COInfoList.iterator();
				while(sItr.hasNext()){
					mapObjectInfo = (Map)sItr.next();
					type = (String)mapObjectInfo.get(SELECT_TYPE);
					objectId = (String)mapObjectInfo.get(SELECT_ID);
					current =(String)mapObjectInfo.get(SELECT_CURRENT);
					owner =(String)mapObjectInfo.get(SELECT_OWNER);
					strTypeKinfOf = (String)mapObjectInfo.get(ChangeConstants.SELECT_TYPE_KINDOF);

					sbEditIcon = new StringBuffer();
					if (type.equalsIgnoreCase(ChangeConstants.TYPE_CHANGE_ORDER)||new DomainObject(objectId).isKindOf(context, ChangeConstants.TYPE_CHANGE_ORDER)){
						if((ChangeConstants.STATE_FORMALCHANGE_PROPOSE.equalsIgnoreCase(current) || ChangeConstants.STATE_FORMALCHANGE_PREPARE.equalsIgnoreCase(current)  || ChangeConstants.STATE_FORMALCHANGE_HOLD.equalsIgnoreCase(current)) && (context.getUser().equalsIgnoreCase(owner))){
						sbEditIcon.append("<a href=\"JavaScript:emxTableColumnLinkClick('");
						sbEditIcon.append("../common/emxForm.jsp?formHeader=EnterpriseChangeMgt.Heading.EditCO&amp;mode=edit");
						sbEditIcon.append("&amp;HelpMarker=emxhelpecoeditdetails&amp;submitAction=refreshCaller&amp;suiteKey=EnterpriseChangeMgt&amp;objectId=");
						sbEditIcon.append(XSSUtil.encodeForHTMLAttribute(context, objectId)); //common code

						sbEditIcon.append("&amp;form=type_ChangeOrderSlidein'"); //give CO edit form here.
						sbEditIcon.append(", '700', '600', 'true', 'slidein', '')\">");
						sbEditIcon.append("<img border=\"0\" src=\"../common/images/iconActionEdit.gif\" title=");
						sbEditIcon.append("\""+ XSSUtil.encodeForHTMLAttribute(context, strEditCO)+"\"");
						sbEditIcon.append("/></a>");
						}else{
							sbEditIcon.append("-");
						}
						columnVals.add(sbEditIcon.toString());

					}
					if (type.equalsIgnoreCase(ChangeConstants.TYPE_CHANGE_REQUEST)||new DomainObject(objectId).isKindOf(context, ChangeConstants.TYPE_CHANGE_REQUEST)){
						if((ChangeConstants.STATE_CHANGEREQUEST_CREATE.equalsIgnoreCase(current) || ChangeConstants.STATE_CHANGEREQUEST_EVALUATE.equalsIgnoreCase(current) || ChangeConstants.STATE_CHANGEREQUEST_HOLD.equalsIgnoreCase(current)) && (context.getUser().equalsIgnoreCase(owner))){
							sbEditIcon.append("<a href=\"JavaScript:emxTableColumnLinkClick('");
							sbEditIcon.append("../common/emxForm.jsp?formHeader=EnterpriseChangeMgt.Heading.EditCR&amp;mode=edit");
							sbEditIcon.append("&amp;HelpMarker=emxhelpeCReditdetails&amp;submitAction=refreshCaller&amp;suiteKey=EnterpriseChangeMgt&amp;objectId=");
							sbEditIcon.append(XSSUtil.encodeForHTMLAttribute(context, objectId)); //common code

							sbEditIcon.append("&amp;form=type_ChangeRequestSlidein'"); //give CR edit form here.
							sbEditIcon.append(", '700', '600', 'true', 'slidein', '')\">");
							sbEditIcon.append("<img border=\"0\" src=\"../common/images/iconActionEdit.gif\" title=");
							sbEditIcon.append("\""+ XSSUtil.encodeForHTMLAttribute(context, strEditCR)+"\"");
							sbEditIcon.append("/></a>");
						} else{
							sbEditIcon.append("-");
						}
						columnVals.add(sbEditIcon.toString());

					}
					if (type.equalsIgnoreCase(ChangeConstants.TYPE_CHANGE_ACTION)||new DomainObject(objectId).isKindOf(context, ChangeConstants.TYPE_CHANGE_ACTION)){
						if((ChangeConstants.STATE_CHANGE_ACTION_PREPARE.equalsIgnoreCase(current) || ChangeConstants.STATE_CHANGE_ACTION_INWORK.equalsIgnoreCase(current) || ChangeConstants.STATE_CHANGE_ACTION_HOLD.equalsIgnoreCase(current)) && (context.getUser().equalsIgnoreCase(owner))){
							sbEditIcon.append("<a href=\"JavaScript:emxTableColumnLinkClick('");
							sbEditIcon.append("../common/emxForm.jsp?formHeader=EnterpriseChangeMgt.Heading.EditCA&amp;mode=edit");
							sbEditIcon.append("&amp;HelpMarker=emxhelpeCAeditdetails&amp;submitAction=refreshCaller&amp;suiteKey=EnterpriseChangeMgt&amp;objectId=");
							sbEditIcon.append(XSSUtil.encodeForHTMLAttribute(context, objectId)); //common code
							sbEditIcon.append("&amp;form=type_ChangeActionSlidein'"); //give CR edit form here.
							sbEditIcon.append(", '700', '600', 'true', 'slidein', '')\">");
							sbEditIcon.append("<img border=\"0\" src=\"../common/images/iconActionEdit.gif\" title=");
							sbEditIcon.append("\""+ XSSUtil.encodeForHTMLAttribute(context, strEditCA)+"\"");
							sbEditIcon.append("/></a>");
						} else{
						sbEditIcon.append("-");
						}
						columnVals.add(sbEditIcon.toString());

					}

					if(!type.equalsIgnoreCase(ChangeConstants.TYPE_CHANGE_ORDER) && !type.equalsIgnoreCase(ChangeConstants.TYPE_CHANGE_REQUEST) && !type.equalsIgnoreCase(ChangeConstants.TYPE_CHANGE_ACTION) && !strTypeKinfOf.equalsIgnoreCase("Change")) {
						sbEditIcon.append("-");
						columnVals.add(sbEditIcon.toString());
					}

				}//end of while

			}

			return columnVals;
		} catch (Exception e) {
			throw new FrameworkException(e);
		}
	}

	/**
	 * Program to Show Policies
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 *           0 -  Object
	 * @return        a <code>MapList</code> object having the list of Assignees,their relIds and rel names for this Change Object
	 * @throws        Exception if the operation fails
	 * @since         ECM R211
	 **
	 */

	public HashMap showPolicies(Context context,String []args) throws Exception {

		if (args.length == 0 )
		{
			throw new IllegalArgumentException();
		}




		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		HashMap requestMap = (HashMap)paramMap.get("requestMap");
		HashMap fieldMap = (HashMap)paramMap.get("fieldMap");

		String language     = (String)requestMap.get("languageStr");
		String propertyFile = (String)requestMap.get("StringResourceField");
		
		String sFormalPolicy = PropertyUtil.getSchemaProperty(context,"policy_FormalChange");
		String sFasttrackPolicy = PropertyUtil.getSchemaProperty(context,"policy_FasttrackChange");
		
		String localsFormalProcess = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Policy."+sFormalPolicy.replaceAll(" ", "_"));

		String localsFasttrack = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Policy."+sFasttrackPolicy.replaceAll(" ", "_"));

		StringList fieldRangeValues = new StringList();
		StringList fieldDisplayRangeValues = new StringList();
		HashMap tempMap = new HashMap();

		String policy = "";
		String il18NPolicyName;
		HashMap fieldValues = (HashMap)paramMap.get("fieldValues");

		String selectType = "Change Order";
		String keyRangeValue = "field_choices";
		String keyDisplayRangeValue = "field_display_choices";
		
		if(fieldValues!=null)
		{

			selectType = (String)fieldValues.get("DefaultType");
			keyRangeValue = "RangeValues";
			keyDisplayRangeValue = "RangeDisplayValues";

		}
			Map sPolicyMap = mxType.getDefaultPolicy(context,selectType, false);

			String languageStr = context.getSession().getLanguage();
			String sDefaultPolicy = (String)sPolicyMap.get("name");

			MapList policyList = mxType.getPolicies(context, selectType, false);

			Iterator itr = policyList.iterator();
			while(itr.hasNext())
			{
				Map newMap = (Map)itr.next();
				String policyName = (String)newMap.get("name");
				il18NPolicyName=i18nNow.getAdminI18NString(ChangeConstants.POLICY, policyName,languageStr);
				fieldRangeValues.add(policyName);
				fieldDisplayRangeValues.add(il18NPolicyName);

			}

			tempMap.put(keyRangeValue, fieldRangeValues);
			tempMap.put(keyDisplayRangeValue, fieldDisplayRangeValues);


			return tempMap;
	}//end of method

	/**
	 * Shows History Date column values on table in CO Properties page
	 * @author
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Vector getHistoryDate(Context context, String[] args) throws Exception {
		return getHistoryColumn(context, args, ChangeConstants.DATE);
	}

	/**
	 * Shows History Person column values on table in CO Properties page
	 * @author
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Vector getHistoryPerson(Context context, String[] args) throws Exception {
		return getHistoryColumn(context, args, ChangeConstants.PERSON);
	}

	/**
	 * Shows History Action column values on table in CO Properties page
	 * @author
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Vector getHistoryAction(Context context, String[] args) throws Exception {
		return getHistoryColumn(context, args, ChangeConstants.ACTION);
	}

	/**
	 * Shows History State column values on table in CO Properties page
	 * @author
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Vector getHistoryState(Context context, String[] args) throws Exception {
		return getHistoryColumn(context, args, ChangeConstants.STATE);
	}
	/**
	 * Shows History Description column values on table in CO Properties page
	 * @author
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Vector getHistoryDescription(Context context, String[] args) throws Exception {
		return getHistoryColumn(context, args, ChangeConstants.DESCRIPTION);
	}

	/**
	 * Shows History column values on table in CO Properties page
	 * @author
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	private Vector getHistoryColumn(Context context, String[] args, String colName) throws Exception {
		HashMap paramMap   = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) paramMap.get(ChangeConstants.OBJECT_LIST);
		Vector valVactor = new Vector();
		if (objectList != null) {
			for (int i = 0; i < objectList.size(); i++) {
				Map obj      = (Map) objectList.get(i);
				String value = (String) obj.get(colName);
				if (ChangeUtil.isNullOrEmpty(value))
					valVactor.add("");
				else
					valVactor.add(value);
			}
		}
		return valVactor;
	}

	/**
	 * This method gets ownership history on a Change object for the table to display on change properties page.
	 *
	 * @author
	 * @param context
	 *                the eMatrix <code>Context</code> object
	 * @param args
	 *                holds the following input arguments: 0 - String containing
	 *                Service object id.
	 * @return MapList holds a list of history records.
	 * @throws Exception
	 *                 if the operation fails
	 * @since ECM R211
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getOwnershipHistory(Context context,String []args)throws Exception
	{
		HashMap programMap  = (HashMap)JPO.unpackArgs(args);
		String  objectId    = (String)programMap.get(ChangeConstants.OBJECT_ID);
		String timeZone     = (String) programMap.get("timeZone");

		changeOrderUI = new ChangeOrderUI(objectId);
		StringList customHistoryList = changeOrderUI.getCustomHistory(context);
		return changeOrderUI.getFilteredHistoryBasedOnAction(context,timeZone,customHistoryList,ChangeConstants.OWNERSHIP_HISTORY);

	}
	/**
	 * This method gets hold and resume history on a Change object for the table to display on change properties page.
	 * @author
	 * @param context
	 *                the eMatrix <code>Context</code> object
	 * @param args
	 *                holds the following input arguments: 0 - String containing
	 *                Service object id.
	 * @return MapList holds a list of history records.
	 * @throws Exception
	 *                 if the operation fails
	 * @since ECM R211
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getHoldAndResumeHistory(Context context,String []args)throws Exception
	{
		HashMap programMap  = (HashMap)JPO.unpackArgs(args);
		String  objectId    = (String)programMap.get(ChangeConstants.OBJECT_ID);
		String timeZone     = (String) programMap.get("timeZone");
		MapList historyMapList = new MapList();

		changeOrderUI = new ChangeOrderUI(objectId);
		StringList customHistoryList = changeOrderUI.getCustomHistory(context);
		historyMapList = changeOrderUI.getFilteredHistoryBasedOnAction(context,timeZone,customHistoryList,ChangeConstants.HOLD_HISTORY);
		historyMapList.addAll(changeOrderUI.getFilteredHistoryBasedOnAction(context,timeZone,customHistoryList,ChangeConstants.RESUME_HISTORY));

		return historyMapList;

	}

	/**
	 * This method gets cancel history on a Change object for the table to display on change properties page.
	 *
	 * @author
	 * @param context
	 *                the eMatrix <code>Context</code> object
	 * @param args
	 *                holds the following input arguments: 0 - String containing
	 *                Service object id.
	 * @return MapList holds a list of history records.
	 * @throws Exception
	 *                 if the operation fails
	 * @since ECM R211
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable 
	public MapList getCancelHistory(Context context,String []args)throws Exception
	{
		HashMap programMap  = (HashMap)JPO.unpackArgs(args);
		String  objectId    = (String)programMap.get(ChangeConstants.OBJECT_ID);
		String timeZone     = (String) programMap.get("timeZone");

		changeOrderUI = new ChangeOrderUI(objectId);
		StringList customHistoryList = changeOrderUI.getCustomHistory(context);
		return changeOrderUI.getFilteredHistoryBasedOnAction(context,timeZone,customHistoryList,ChangeConstants.CANCEL_HISTORY);

	}

	/**
	 * Trigger Method to send notification after functionality Change Owner.
	 * @author
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args - Object Id of Change
	 * @return int - Returns integer status code
            0 - in case the trigger is successful
	 * @throws Exception if the operation fails
	 * @since ECM R211
	 */
	public int notifyOwner(Context context, String[] args) throws Exception {

		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}

		try {

			String strObjectId     = args[0];
			String subjectKey      = args[1];
			String messageKey      = args[2];
			String propertyKey     = args[3];
			String kindOfOwner     = args[4];

			DomainObject domChange = newInstance(context, strObjectId);
			StringList objSelects  = new StringList(SELECT_OWNER);;
			objSelects.addElement(SELECT_ORIGINATOR);

			Map objMap      = domChange.getInfo(context, objSelects);
			String strOwner = (String)objMap.get(SELECT_OWNER);
			String strOriginator = (String)objMap.get(SELECT_ORIGINATOR);

			String strLanguage = context.getSession().getLanguage();

			StringList ccList  = new StringList();
			StringList bccList = new StringList();
			StringList toList  = new StringList();
			StringList lstAttachments = new StringList();

			toList.add(strOwner);
			lstAttachments.add(strObjectId);

			//Do not send a message if the current owner is person_UserAgent
			if (ChangeConstants.USER_AGENT.equalsIgnoreCase(strOwner)||kindOfOwner.equalsIgnoreCase(DomainConstants.TYPE_ORGANIZATION)||kindOfOwner.equalsIgnoreCase("Project"))
				return 0;

			//Sending mail to the owner
			emxNotificationUtilBase_mxJPO.sendNotification( context,strObjectId,toList, ccList, bccList, subjectKey,messageKey, lstAttachments, propertyKey, null,null,"enoECMChangeOrder:getTransferComments");

		} catch (Exception ex) {
			ex.printStackTrace(System.out);
			throw new FrameworkException((String) ex.getMessage());
		}
		return 0;
	}
	/**
	 * This method checks whether Change Coordinator is assigned to change or not.
	 * @author
	 * @param context
	 *            the eMatrix <code>Context</code> object.
	 * @param args
	 *            holds objectId.
	 * @param args
	 *            holds relationship name
	 * @return integer 0 if Change Coordinator is assigned else 1
	 * @throws Exception if the operation fails.
	 * @since ECM R211
	 */
	public int checkForChangeCoordinator(Context context, String args[]) throws Exception {
		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}

		int retValue = 0;
		try {
			String objectId = args[0];
			setId(objectId);
			String relationshipName =  PropertyUtil.getSchemaProperty(context,args[1]);
			String resourceFieldId  =  args[2];
			String propertyKey      =  args[3];

			String changeCoordinator = getInfo(context,"from["+relationshipName+"].to.id");

			String Message           = EnoviaResourceBundle.getProperty(context, resourceFieldId, context.getLocale(),propertyKey);

			if (ChangeUtil.isNullOrEmpty(changeCoordinator)){
				emxContextUtilBase_mxJPO.mqlNotice(context, Message);
				return 1;
			}
		}catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
		return retValue;
	}

	/**
	 * This method checks whether atleast one primary affected item connected to Change Order before moving to prepare state
	 * @author
	 * @param context
	 *            the eMatrix <code>Context</code> object.
	 * @param args
	 *            holds objectId.
	 * @param args
	 *            holds relationship name
	 * @return Integer 0 if primary affected item is connected else 1.
	 * @throws Exception if the operation fails.
	 * @since ECM R211
	 */
	public int checkForAffectedItemConnected(Context context, String args[]) throws Exception {

		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}

		int retValue = 0;
		try {
			String objectId = args[0];
			setId(objectId);
			String relationshipName =  PropertyUtil.getSchemaProperty(context,args[1]);
			String typeName         =  PropertyUtil.getSchemaProperty(context,args[2]);
			String resourceFieldId  =  args[3];
			String propertyKey      =  args[4];

			StringList objectSelect = new StringList(1);
			objectSelect.addElement(SELECT_TYPE);
			objectSelect.addElement(SELECT_ID);
			objectSelect.addElement("physicalid");
			String Message       =EnoviaResourceBundle.getProperty(context, resourceFieldId, context.getLocale(),propertyKey);
			MapList affectedList = getRelatedObjects(context,
					relationshipName,
					DomainConstants.QUERY_WILDCARD,
					objectSelect,
					null,
					false,
					true,
					(short) 1,
					null,null,
					(short)0);


			if (affectedList != null && affectedList.size() > 0) {
				Iterator iter = affectedList.iterator();
				retValue = 1;
				while( iter.hasNext()){
					Map caMap = (Map)iter.next();


					String type = (String) caMap.get(SELECT_TYPE);

					if (type.equals(ChangeConstants.TYPE_CCA)) {
						String ccaId = (String) caMap.get(SELECT_ID);
						retValue = 0;
						String affectedItemExists = DomainObject.newInstance(context, ccaId).getInfo(context, "from[" + DomainConstants.RELATIONSHIP_AFFECTED_ITEM + "]");
						
						if ("False".equalsIgnoreCase(affectedItemExists)) {
							retValue = 1;							
						}
					}else{
						String sCAId = (String) caMap.get("physicalid");
						//check if proposed change is present
						IChangeAction changeActionObj = new ChangeAction().getChangeAction(context, sCAId);
						List<IProposedChanges> proposedChangesList = changeActionObj.getProposedChanges(context);						

						if(proposedChangesList !=null && proposedChangesList.size()>0){

							retValue = 0;
							break;

						}
					}
				}
				if(retValue==1){
					emxContextUtilBase_mxJPO.mqlNotice(context, Message);
				}
			} else {
				emxContextUtilBase_mxJPO.mqlNotice(context, Message);
				retValue = 1;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
		
		return retValue;
	}
   /**
	 * This trigger function determines if there are any connected objects to CO/CA
	 * @author S4Y
	 * @param context
	 *            the eMatrix <code>Context</code> object.
	 * @param args
	 *            holds Trigger Params
	 * @return Integer 0 if primary affected item is connected else 1.
	 * @throws Exception if the operation fails.
	 * @since ECM R216
	 */
	public int checkIfAffectedItemConnected(Context context, String args[]) throws Exception {

		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}

		int retValue = 0;
		try {
			String objectId = args[0];
			setId(objectId);

			String relationshipName =  PropertyUtil.getSchemaProperty(context,args[1]);
			String typeName         =  PropertyUtil.getSchemaProperty(context,args[2]);
			if(UIUtil.isNullOrEmpty(typeName))
				typeName = args[2];
			String resourceFieldId  =  args[3];
			String propertyKey      =  args[4];
			String sCurrent 		=  args[5];
			String sOwner			=  args[6];
			
			StringList objectSelects = new StringList(3);
			objectSelects.addElement("physicalid");
			objectSelects.addElement("from["+ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM+"].to.id");
		    objectSelects.addElement("from["+DomainConstants.RELATIONSHIP_AFFECTED_ITEM+"].to.id");
		    
		    StringList relSelects = new StringList(SELECT_ID);
		    StringList strAffectedItemId = null;
		    
		    String Message       =EnoviaResourceBundle.getProperty(context, resourceFieldId, context.getLocale(),propertyKey);
			MapList affectedList = getRelatedObjects(context,
													relationshipName,
													"*",
													objectSelects,
													null,
													false,
													true,
													(short) 1,
													null,null,
													(short)0);
			if(ChangeUtil.isLegacyEnable(context)){
				if ((affectedList != null && affectedList.size() > 0) || (!sOwner.equalsIgnoreCase(context.getUser()) && (ChangeConstants.TYPE_CHANGE_ORDER.equalsIgnoreCase(typeName) || ChangeConstants.TYPE_CHANGE_REQUEST.equalsIgnoreCase(typeName))) || (ChangeConstants.TYPE_CHANGE_REQUEST.equalsIgnoreCase(typeName) && !ChangeConstants.STATE_CHANGEREQUEST_CREATE.equalsIgnoreCase(sCurrent))) {
					Iterator iter = affectedList.iterator();
					while( iter.hasNext() )
					{
						Map map = (Map) iter.next();
						Object obj = (Object)map.get("from["+ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM+"].to.id");
						strAffectedItemId = ChangeUtil.convertObjToStringList(context, obj);					
						if(strAffectedItemId == null || strAffectedItemId.size() == 0){
							obj = (Object)map.get("from["+ChangeConstants.RELATIONSHIP_AFFECTED_ITEM+"].to.id");
							strAffectedItemId = ChangeUtil.convertObjToStringList(context, obj);
						}
						if(strAffectedItemId != null && strAffectedItemId.size() > 0){
							emxContextUtilBase_mxJPO.mqlNotice(context, Message);
							return 1;
						}
					}				
				}
			} else {
				if (affectedList != null && affectedList.size() > 0){
					Iterator iter = affectedList.iterator();
					while( iter.hasNext()){
						String sCAId = (String)((Map)iter.next()).get("physicalid");
						//check if proposed \ realized change is present
						IChangeAction changeActionObj = new ChangeAction().getChangeAction(context, sCAId);
						List<IProposedChanges> proposedChangesList = changeActionObj.getProposedChanges(context);						
						List realizedChangeList=changeActionObj.getRealizedChanges(context);
						
						if((proposedChangesList != null && proposedChangesList.size() > 0) ||
						   (realizedChangeList != null && realizedChangeList.size() > 0)){
							emxContextUtilBase_mxJPO.mqlNotice(context, Message);
							return 1;
						}
					}
				}
			}
			

		}catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
		return retValue;
	}

	/**
	 * this method retrieves the Change Coordinator and assigns as the owner of the Change
	 * @author
	 * @param context
	 *            the eMatrix <code>Context</code> object.
	 * @param args1
	 *            holds objectId.
	 * @param args2
	 *            holds Relationship Name.
	 * @return 0 if success else 1 for failure
	 * @throws Exception if the operation fails.
	 * @since ECM R211
	 */
	public int RouteToChangeCoordinator(Context context, String args[]) throws Exception {

		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}

		int success = 0;

		String objectId = args[0];
		setId(objectId);
		String relationName      = PropertyUtil.getSchemaProperty(context,args[1]);
		String coordinatorSelect = "from["+relationName+"].to.name";
		String owner = SELECT_OWNER;

		StringList objSelects = new StringList(2); objSelects.addElement(coordinatorSelect);
		objSelects.addElement(owner);
		try {
			Map objMap = getInfo(context, objSelects);
			String currentOwner      = (String)objMap.get(owner);
			String changeCoordinator = (String)objMap.get(coordinatorSelect);

			//If Change Coordinator is not empty and if he is not the owner then set Change Coordinator as owner
			if (!ChangeUtil.isNullOrEmpty(changeCoordinator) && !currentOwner.equalsIgnoreCase(changeCoordinator))
				setOwner(context, changeCoordinator);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
		return success;
	}

	/**
	 * This method is used check promote trigger on different states of the policies. checks if the RouteTemplate is attached
	 * for context change object or not
	 * @author
	 * @param context
	 * @param args
	 *            0 - String containing object id.
	 *            1 - String Approval Or Reviewer RouteTemplate.
	 *            2 - String Resource file name
	 *            3 - String Resource field key
	 * @return int 0 for success or 1 for failure
	 * @throws Exception if the operation fails. * *
	 * @since  ECM R211
	 */
	public int checkRouteTemplateForState(Context context, String[] args) throws Exception {

		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}

		String objectId = args[0];// Change Object Id
		String routeBasePurpose  = args[1];
		String relationName      = RELATIONSHIP_OBJECT_ROUTE;
		String typeName          = TYPE_ROUTE_TEMPLATE;
		String resourceFieldId   = args[2];
		String propertyKey       = args[3];

		MapList mapRouteTemplate = new MapList();

		try {

			// create change object with the context Object Id
			setId(objectId);
			StringList selectStmts = new StringList(1);
			selectStmts.addElement("attribute["+ATTRIBUTE_ROUTE_BASE_PURPOSE+"]");

			String whrClause = "attribute["+ ATTRIBUTE_ROUTE_BASE_PURPOSE + "] match '"
					+ routeBasePurpose + "' && current == Active";

			// get route template objects from change object
			mapRouteTemplate = getRelatedObjects(context,
					relationName,
					typeName,
					selectStmts,
					null,
					false,
					true,
					(short) 1,
					whrClause,
					null,
					(short) 0);

			if(mapRouteTemplate != null && mapRouteTemplate.size() > 0) {
				return 0; // returns true if there is any route template objects connected with change object
			} else {
				emxContextUtil_mxJPO.mqlNotice(context,EnoviaResourceBundle.getProperty(context, resourceFieldId, context.getLocale(),propertyKey));
				return 1;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			return 1;// return false
		}

	}

	/**
	 *
	 * This method retrieves all the routes from Change Object, if no routes exists calls createRoute method
	 * which creates route from route template and attaches to Change object and if routes exists means, in
	 * further process retrieves all Route Templates from Change Object and for each Route Template retrieves
	 * initiated routes, if any of these initiated routes already present in original route list , makes
	 * booleanRoutePresent variable to True and skips method call of Create Route
	 *
	 * @author
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args:
	 *            0 - OBJECTID change id
	 *            1 - Route Base Purpose The kind of Route (Route Base Purpose) can be Approval,Review,Standard
	 *            2 - Relationship Object Route
	 *            3 - Relationship Initiating Route Template
	 *            4 - type Route
	 *            5 - type Route Template
	 *            6 - state In-Review/In-Approve
	 *            7 - policy of the change object
	 *            8 - target State (In-Review/In-Approval)
	 *            object state (from state ie that is the state on which the route will be started)
	 *
	 * @returns true for sucess and false for trigger failure
	 * @throws Exception if the operation fails
	 * @since ECM R211
	 */
	public int createRouteFromRouteTemplate(Context context, String[] args) throws Exception {

		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}

		String changeObjId       = args[0]; // Change Object
		String routeBasePurpose  = args[1];
		String relObjectRoute    = RELATIONSHIP_OBJECT_ROUTE;
		String relInitiatedRoute = RELATIONSHIP_INITIATING_ROUTE_TEMPLATE;
		String typeRoute         = TYPE_ROUTE;
		String typeRouteTemplate = TYPE_ROUTE_TEMPLATE;

		try {
			StringList sListRoutes         = new StringList();
			StringList sListRouteTemplates = new StringList();
			StringList initiatedRoutesList = new StringList();

			boolean boolRoutePresent = false;
			String routeTemplateID   = "";
			String initiatedRouteID  = "";
			DomainObject objTemplate = null;

			setId(changeObjId);// change object

			String routeSelect            = "from[" + relObjectRoute + "].to["+typeRoute+"]";

			//get Route Templates connected to Change Object where clause to filter out Approval or Review Routes
			String routeTemplateSelect    = "from[" + relObjectRoute + "|attribute[" + ATTRIBUTE_ROUTE_BASE_PURPOSE + "] " +
					"== "+routeBasePurpose+"].to["+typeRouteTemplate+"].id";

			String initiatedRouteTemplateSelect = "to[" + relInitiatedRoute + "].from["+typeRoute+"]";

			// get Routes Connected to change object
			sListRoutes = getInfoList(context, routeSelect);

			// if route present already go for further process else call create Route method
			if (sListRoutes != null && sListRoutes.size() > 0) {

				sListRouteTemplates = getInfoList(context, routeTemplateSelect);

				if (sListRouteTemplates != null && sListRouteTemplates.size() > 0) {

					for (Iterator itrTemplates = sListRouteTemplates.iterator(); itrTemplates.hasNext();) {

						routeTemplateID = (String) itrTemplates.next();
						int index       =  routeTemplateID.indexOf("=");
						routeTemplateID = (index > 0) ? routeTemplateID.substring(index+2,routeTemplateID.length()) : routeTemplateID;
						objTemplate     = (DomainObject) DomainObject.newInstance(context,routeTemplateID);

						// get list of routes connected to Route Template
						initiatedRoutesList = objTemplate.getInfoList(context, initiatedRouteTemplateSelect);

						if (initiatedRoutesList.size() > 0 && !boolRoutePresent) {
							for (Iterator itrRoute = initiatedRoutesList.iterator(); itrRoute.hasNext();) {
								initiatedRouteID = (String) itrRoute.next();

								// check if this initiated Route ID is already exists in that original Route List
								if (sListRoutes.contains(initiatedRouteID)) {
									boolRoutePresent = true;
									break;
								}
							}
						}
					}
				}
			}

			if (!boolRoutePresent) {
				return (createRoute(context, args));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());

		}
		return 0;
	}


	/**
	 * this method creates route from route template and attaches to change object
	 *
	 * @author
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args:
	 *            0 - OBJECTID change id
	 *            1 - change object policy
	 *            2 - change object state (from state ie that is the state
	 *            on which the route will be started)
	 *            3 - the The kind of Route Template( Route Base
	 *            Purpose ) can be Approval,Review,Standard
	 * @returns true for success and false for trigger failure
	 * @throws Exception
	 *             if the operation fails
	 * @since Common X3
	 */
	public int createRoute(Context context, String[] args) throws Exception {

		String changeObjId = args[0]; // change Object

		String changeObjectPolicy = args[2];
		String changeState        = args[3];
		String routeBasePurpose   = args[1];

		String templateId    = "";   // routeTemplate ID
		String sTemplateDesc = "";   // template description
		String sTemplateBasePurpose = "";   // template Base Purpuse
		String templateState = "";   // current state
		Route  routeObj      = (Route) DomainObject.newInstance(context,TYPE_ROUTE);; // object route

		String strAutoStopOnRejection ="";
		String strRouteName = "";

		// get Alias Names
		String RoutePolicyAdminAlias = FrameworkUtil.getAliasForAdmin(context,SELECT_POLICY, POLICY_ROUTE, true);
		String RouteTypeAdminAlias   = FrameworkUtil.getAliasForAdmin(context,SELECT_TYPE, TYPE_ROUTE, true);
		String sAttrRouteBasePurpose = PropertyUtil.getSchemaProperty(context,"attribute_RouteBasePurpose");

		String sAttrRestartUponTaskRejection         = PropertyUtil.getSchemaProperty(context, "attribute_AutoStopOnRejection" );
		String SELECT_ATTRIBUTE_AUTO_STOP_REJECTION  = "attribute[" + sAttrRestartUponTaskRejection + "]";
		String sAttrRouteCompletionAction            = PropertyUtil.getSchemaProperty(context,"attribute_RouteCompletionAction");

		// add object selects
		SelectList objectSelects = new SelectList(5);
		objectSelects.add(SELECT_ID);
		objectSelects.add(RouteTemplate.SELECT_DESCRIPTION);
		objectSelects.add(RouteTemplate.SELECT_CURRENT);
		objectSelects.add(SELECT_ATTRIBUTE_AUTO_STOP_REJECTION);
		objectSelects.add("attribute["+ ATTRIBUTE_ROUTE_BASE_PURPOSE + "]");
		// add relationship selects
		SelectList relSelects = new SelectList(1);
		relSelects.add(SELECT_RELATIONSHIP_ID);

		// where clause to filter out Aproval or Reviewal Route
		String whrClause    = "attribute["+ ATTRIBUTE_ROUTE_BASE_PURPOSE + "] match '"+ routeBasePurpose + "'";
		String stateActive  = STATE_ROUTE_TEMPLATE_ACTIVE;
		whrClause = whrClause + " && (latest == 'true') && (current == '"+stateActive+"')";

		MapList templateList = null; // Map List of Templates attached to change object
		Map routeNodeMap     = null;
		HashMap routeObjectRelAttrMap = null;
		HashMap routeAttrMap = null;

		DomainObject changeObj = (DomainObject) DomainObject.newInstance(context,changeObjId);
		StringList objSelects  = new StringList(SELECT_NAME);objSelects.addElement(SELECT_OWNER);

		Map changeObjectInfo     = changeObj.getInfo(context,objSelects);
		String changeObjectName  = (String)changeObjectInfo.get(SELECT_NAME);
		String changeObjectOwner = (String)changeObjectInfo.get(SELECT_OWNER);

		try {
			// get list of Templates attached to change object
			templateList = changeObj.getRelatedObjects(context,
					RELATIONSHIP_OBJECT_ROUTE,
					TYPE_ROUTE_TEMPLATE,
					objectSelects,
					relSelects,
					false,
					true,
					(short) 1,
					whrClause,
					null,
					(short) 0);

			Iterator itr = templateList.iterator();
			while (itr.hasNext()) {

				Map mapTemplate = (Map) itr.next();// Template Map
				templateId      = (String) mapTemplate.get(SELECT_ID);
				sTemplateDesc   = (String) mapTemplate.get(SELECT_DESCRIPTION);
				sTemplateBasePurpose =(String) mapTemplate.get("attribute["+ ATTRIBUTE_ROUTE_BASE_PURPOSE + "]");
				templateState   = (String) mapTemplate.get(SELECT_CURRENT);
				strAutoStopOnRejection =(String) mapTemplate.get(SELECT_ATTRIBUTE_AUTO_STOP_REJECTION);

				if (!ChangeUtil.isNullOrEmpty(templateId) && "Active".equals(templateState)) {

					// create Route Object id
					String sRouteId = FrameworkUtil.autoName(context,RouteTypeAdminAlias, "", RoutePolicyAdminAlias,PersonUtil.getDefaultVault(context));
					// create Route object
					routeObj.setId(sRouteId);
					// rename change object object name
					strRouteName = routeObj.getInfo(context,SELECT_NAME);
					//Change the Route Name by appending Context Change Object Name
					strRouteName = "Route_"+ strRouteName + "_" + changeObjectName + "_"+ changeState;
					// set new name to Route object
					routeObj.setName(context, strRouteName);

					// HashMap to carry all the attribute values to be set
					routeObjectRelAttrMap = new HashMap();

					routeObjectRelAttrMap.put(ATTRIBUTE_ROUTE_BASE_STATE,FrameworkUtil.reverseLookupStateName(context, args[2], args[3]));
					routeObjectRelAttrMap.put(ATTRIBUTE_ROUTE_BASE_POLICY,FrameworkUtil.getAliasForAdmin(context, "Policy", args[2], false));
					routeObjectRelAttrMap.put(sAttrRouteBasePurpose, "Standard");
					routeObj.open(context);
					routeObj.setDescription(context, sTemplateDesc);
					routeObj.setAttributeValue(context, sAttrRouteBasePurpose,sTemplateBasePurpose);
					//set Route Completion Action
					routeObj.setAttributeValue(context,sAttrRouteCompletionAction,"Promote Connected Object");
					routeObj.setAttributeValue(context,sAttrRestartUponTaskRejection,strAutoStopOnRejection); //IR-118894

					//set the all the relationships from created route object
					RelationshipType relationshipType = new RelationshipType(RELATIONSHIP_OBJECT_ROUTE);
					DomainRelationship newRel = routeObj.addFromObject(context,relationshipType, changeObjId);
					// connect to Route Template
					routeObj.connectTemplate(context, templateId);
					// add member list from template to route
					routeObj.addMembersFromTemplate(context, templateId);
					// UPDATE OBJECT ROUTE ATTRIBUTES.
					newRel.setAttributeValues(context, routeObjectRelAttrMap);
					// Change Route Action Attribute to Approve
					StringList relProductSelects = new StringList(1);
					relProductSelects.add(SELECT_RELATIONSHIP_ID);
					String nodeTypePattern = TYPE_PERSON + ChangeConstants.COMMA_SEPERATOR + TYPE_ROUTE_TASK_USER;
					// get all the tasks in the route
					MapList routeNodeList = routeObj.getRelatedObjects(context,
							RELATIONSHIP_ROUTE_NODE,
							nodeTypePattern, null,
							relProductSelects, false, true, (short) 1, null,null,(short) 0);
					Iterator itrRouteNodeList = routeNodeList.iterator();
					while (itrRouteNodeList.hasNext()) {
						routeNodeMap = (Map) itrRouteNodeList.next();
						DomainRelationship routeNodeId = new DomainRelationship((String) routeNodeMap.get(SELECT_RELATIONSHIP_ID));
						//attribute map for Route node relationship
						routeAttrMap = new HashMap();
						routeAttrMap.put(ATTRIBUTE_ROUTE_ACTION,"Approve");
						routeNodeId.setAttributeValues(context, routeAttrMap);
					}

					startRoute(context, sRouteId);// auto start route
					if (ChangeConstants.USER_AGENT.equals(routeObj.getOwner(context).getName()) && !ChangeUtil.isNullOrEmpty(changeObjectOwner))
					{
						routeObj.setId(sRouteId);
						routeObj.setOwner(context, changeObjectOwner);
					}
					
					routeObj.addToObject(context,new RelationshipType(DomainConstants.RELATIONSHIP_PROJECT_ROUTE),DomainObject.newInstance(context, PersonUtil.getPersonObjectID(context, changeObjectOwner)).getId(context)); //Added For IR-337468
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
		return 0;
	}

	/**
	 * Starts the route
	 * @author
	 * @param context the eMatrix <code>Context</code> object
	 * @param strRouteId route object id which has to be satrted
	 * @returns nothing
	 * @throws FrameworkException if the operation fails
	 * @since ECM R211
	 */
	public void startRoute (Context context, String strRouteId) throws Exception
	{
		String OFFSET_FROM_ROUTE_START_DATE  = "Route Start Date";
		String OFFSET_FROM_TASK_CREATE_DATE  = "Task Create Date";

		String attrDueDateOffset      = PropertyUtil.getSchemaProperty(context, "attribute_DueDateOffset");
		String attrDueDateOffsetFrom  = PropertyUtil.getSchemaProperty(context, "attribute_DateOffsetFrom");
		String attrAssigneeDueDate    = PropertyUtil.getSchemaProperty(context, "attribute_AssigneeSetDueDate");

		String selDueDateOffset       = "attribute["+attrDueDateOffset+"]";;
		String selDueDateOffsetFrom   = "attribute["+attrDueDateOffsetFrom+"]";
		String selSequence            = "attribute["+ATTRIBUTE_ROUTE_SEQUENCE+"]";
		String selRouteNodeRelId      = SELECT_RELATIONSHIP_ID;

		StringList relSelects = new StringList(4);
		relSelects.addElement(selDueDateOffset);
		relSelects.addElement(selDueDateOffsetFrom);
		relSelects.addElement(selRouteNodeRelId);
		relSelects.addElement(selSequence);

		StringBuffer sWhereExp = new StringBuffer();

		DomainObject doRoute = new DomainObject(strRouteId);

		// where clause filters to all route tasks with due offset from this Route Start
		sWhereExp.append("("+selDueDateOffset+ " !~~ \"\")");
		sWhereExp.append(" && (" +selDueDateOffsetFrom + " ~~ \""+OFFSET_FROM_ROUTE_START_DATE+"\")");

		MapList routeStartOffsetList = doRoute.getRelatedObjects(context,
				Route.RELATIONSHIP_ROUTE_NODE, //String relPattern
				"*",                          //String typePattern
				null,                          //StringList objectSelects,
				relSelects,                    //StringList relationshipSelects,
				false,                         //boolean getTo,
				true,                          //boolean getFrom,
				(short)1,                      //short recurseToLevel,
				"",                            //String objectWhere,
				sWhereExp.toString(),          //String relationshipWhere,
				null,                          //Pattern includeType,
				null,                          //Pattern includeRelationship,
				null);                         //Map includeMap

		// set Scheduled Due Date attribute for all delta offset Route Nodes
		setDueDatesFromOffset(context, routeStartOffsetList);

		sWhereExp.setLength(0);

		// where clause filters to First order tasks offset from their creation (i.e. this route start)
		sWhereExp.setLength(0);
		sWhereExp.append("("+selDueDateOffset+ " !~~ \"\")");
		sWhereExp.append(" && (" +selDueDateOffsetFrom + " ~~ \""+OFFSET_FROM_TASK_CREATE_DATE+"\")");
		sWhereExp.append(" && (" +selSequence + " == \"1\")");

		MapList routeFirstOrderOffsetList = doRoute.getRelatedObjects(context,
				Route.RELATIONSHIP_ROUTE_NODE, //String relPattern
				"*",                          //String typePattern
				null,                          //StringList objectSelects,
				relSelects,                    //StringList relationshipSelects,
				false,                         //boolean getTo,
				true,                          //boolean getFrom,
				(short)1,                      //short recurseToLevel,
				"",                            //String objectWhere,
				sWhereExp.toString(),          //String relationshipWhere,
				null,                         //Pattern includeType,
				null,                         //Pattern includeRelationship,
				null);                       //Map includeMap
		// set Scheduled Due Date attribute for all delta offset ORDER 1 Route Nodes offset From Task create which is same as Route start
		setDueDatesFromOffset(context, routeFirstOrderOffsetList);

		doRoute.promote(context);


	}

	/**
	 * @author
	 * Set Scheduled Complete Date attribute for all RouteNodes constructed from the maplist
	 * The Completion Due Date is got by adding Offset days attribute to current System date-time
	 * @param context the eMatrix <code>Context</code> object
	 * @param MapList Task list for which offset date has to be set
	 * @returns nothing
	 * @throws FrameworkException if the operation fails
	 * @since Common X3
	 */
	public void setDueDatesFromOffset(Context context, MapList offsetList) throws Exception
	{

		String attrDueDateOffset      = PropertyUtil.getSchemaProperty(context, "attribute_DueDateOffset");
		String selDueDateOffset       = "attribute["+attrDueDateOffset+"]";

		Map rNodeMap                     = null;
		DomainRelationship relObjRouteNode     = null;
		Attribute scheduledDateAttribute = null;
		AttributeList timeAttrList       = new AttributeList();
		GregorianCalendar cal            = new GregorianCalendar();
		GregorianCalendar offSetCal      = new GregorianCalendar();
		SimpleDateFormat formatterTest   = new SimpleDateFormat (eMatrixDateFormat.getInputDateFormat(),Locale.US);

		Iterator nextOrderOffsetItr      = offsetList.iterator();

		// get the equivalent server time with required timezone

		cal.setTime(new Date(cal.getTime().getTime())); //modified on 8th March
		String routeTaskScheduledDateStr  = null;
		String rNodeId                    = null;
		String duedateOffset              = null;

		try
		{
			while(nextOrderOffsetItr.hasNext())
			{
				// use separate calendar objects and reset offSetCal to master calendar to ensure
				// all delta tasks are offset from same Route Start Time.
				offSetCal      = (GregorianCalendar)cal.clone();
				rNodeMap       = (Map) nextOrderOffsetItr.next();
				rNodeId        = (String)rNodeMap.get(DomainObject.SELECT_RELATIONSHIP_ID);
				duedateOffset  = (String)rNodeMap.get(selDueDateOffset);
				// construct corresponding RouteNode relationships and now set correct due-date
				// by adding delta offset to Current time (Route Start) time
				relObjRouteNode             = new DomainRelationship(rNodeId);
				offSetCal.add(Calendar.DATE, Integer.parseInt(duedateOffset));
				routeTaskScheduledDateStr   = formatterTest.format(offSetCal.getTime());
				scheduledDateAttribute      = new Attribute(new AttributeType(ATTRIBUTE_SCHEDULED_COMPLETION_DATE) ,routeTaskScheduledDateStr);
				timeAttrList.add(scheduledDateAttribute);

				// set Scheduled Completion date attribute
				relObjRouteNode.setAttributes(context,timeAttrList);
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}
	/**
	 * Reset Owner on demote of ChangeObject
	 * @author
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * 0 - String holding the object id.
	 * @returns void.
	 * @throws Exception if the operation fails
	 * @since ECM R211
	 */
	public void resetOwner(Context context, String[] args)
			throws Exception
			{
		try
		{
			String objectId = args[0];                              //changeObject ID
			setObjectId(objectId);
			String strCurrentState = args[1];                       //current state of ChangeObject
			
			StringList select	   = new StringList(SELECT_OWNER);
			select.add(SELECT_ORIGINATOR);
			select.add(SELECT_POLICY);
			
			Map resultList 		   = getInfo(context, select);
			String currentOwner    = (String) resultList.get(SELECT_OWNER);
			String sOriginator     = (String) resultList.get(SELECT_ORIGINATOR);
			String sPolicy		   = (String) resultList.get(SELECT_POLICY);
			
			if(ChangeConstants.POLICY_FORMAL_CHANGE.equalsIgnoreCase(sPolicy)){
				String promotedBy      = "";                            //previous state owner
				String historyData     = ChangeUtil.getHistory(context,objectId,"history.promote");
				StringList historyList = FrameworkUtil.splitString(historyData, ChangeConstants.COMMA_SEPERATOR);

				for(int i=historyList.size()-1;i>=0;i--){
					String historyRecord = ((String)historyList.elementAt(i)).trim();
					if(historyRecord.startsWith("promote")){
						promotedBy = historyRecord.substring(historyRecord.indexOf("user:")+6, historyRecord.indexOf("time:")-2).trim();
						if(promotedBy.indexOf("User Agent") > -1)continue;
						break;
					}
				}

				if(!ChangeUtil.isNullOrEmpty(promotedBy) && !currentOwner.equalsIgnoreCase(promotedBy)){
					setOwner(context, promotedBy); //reset owner on previous state
				}
			} else{
				if(!ChangeUtil.isNullOrEmpty(sOriginator) && !currentOwner.equalsIgnoreCase(sOriginator))
					setOwner(context, sOriginator); //reset owner as Originator while Demoting
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
			}

	/**
	 * this method checks the Related object state Returns Boolean determines whether the connected
	 * objects are in appropriate state.
	 *
	 * @author
	 * @param context
	 *            the eMatrix <code>Context</code> object.
	 * @param args
	 *            holds objectId.
	 * @param args
	 *            holds relationship name.
	 * @param args
	 *            holds type name.
	 * @param args
	 *            holds policy name.
	 * @param args
	 *            holds State.
	 * @param args
	 *            holds TO/FROM.
	 * @param args
	 *            holds String Resource file name
	 * @param args
	 *            holds String resource filed key name.
	 * @return Boolean determines whether the connected objects are in
	 *         appropriate state.
	 * @throws Exception if the operation fails.
	 * @since ECM R211
	 */
	public int checkRelatedObjectsInProperState(Context context,String args[]) throws Exception {

		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}
		String objectId = args[0];
		setId(objectId);
		String strRelationshipName = PropertyUtil.getSchemaProperty(context,args[1]);
		String strTypeName         = PropertyUtil.getSchemaProperty(context,args[2]);
		String strPolicyName       = PropertyUtil.getSchemaProperty(context,args[3]);

		String strStates = args[4];
		boolean boolTo   = args[5].equalsIgnoreCase("TO")?true:false;
		boolean boolFrom = args[5].equalsIgnoreCase("FROM")?true:false;

		String strResourceFieldId = args[6];
		String strStringId        = args[7];

		String strMessage         = EnoviaResourceBundle.getProperty(context, strResourceFieldId, context.getLocale(),strStringId);
		String strCurrentState    = args[8];
		String strPolicy          = args[9];


		StringList stateList = new StringList ();
		String state = "";
		String strRelnWhereClause = "";
		String strSymbolicCurrentPolicy = FrameworkUtil.getAliasForAdmin(context, "policy", strPolicy, true);
		String strSymbolicCurrentState  = FrameworkUtil.reverseLookupStateName(context,strPolicy,strCurrentState);

		String RouteBasePolicy = PropertyUtil.getSchemaProperty(context,"attribute_RouteBasePolicy");
		String RouteBaseState  = PropertyUtil.getSchemaProperty(context,"attribute_RouteBaseState");
		
		String INTERFACE_CHANGEONHOLD = PropertyUtil.getSchemaProperty(context, "interface_ChangeOnHold");
		String SELECT_INTERFACE_CHANGE_ON_HOLD = "interface[" + INTERFACE_CHANGEONHOLD + "]";

		int ichkvalue = 0;
		if (strStates.indexOf(" ")>-1){
			stateList = FrameworkUtil.split(strStates, EMPTY_STRING);
		}
		else if (strStates.indexOf(",")>-1){
			stateList = FrameworkUtil.split(strStates, ChangeConstants.COMMA_SEPERATOR);
		}
		else if(strStates.indexOf("~")>-1){
			stateList = FrameworkUtil.split(strStates, ChangeConstants.TILDE_DELIMITER);
		}
		else{
			stateList = FrameworkUtil.split(strStates, "");
		}

		StringList actualStatelist = new StringList();
		for (Iterator stateItr = stateList.iterator();stateItr.hasNext();){
			state = (String)stateItr.next();
			actualStatelist.addElement(PropertyUtil.getSchemaProperty(context, "policy", strPolicyName , state));
		}

		if(RELATIONSHIP_OBJECT_ROUTE.equalsIgnoreCase(strRelationshipName)){
			strRelnWhereClause = "attribute["+RouteBasePolicy+"] == "+strSymbolicCurrentPolicy+" && attribute["+RouteBaseState+"] == "+strSymbolicCurrentState;
		}
		
		
		StringList busSelects = new StringList(3);
		busSelects.add(SELECT_ID);
		busSelects.add(SELECT_CURRENT);
		busSelects.add(SELECT_INTERFACE_CHANGE_ON_HOLD);
		StringList relSelects = new StringList(2);
		relSelects.add(SELECT_ID);

		MapList maplistObjects = new MapList();

		try
		{
			maplistObjects = getRelatedObjects(context,
					strRelationshipName,
					strTypeName,
					busSelects,          // object Select
					relSelects,          // rel Select
					boolFrom,            // to
					boolTo,              // from
					(short)1,
					null,                // ob where
					strRelnWhereClause,  // rel where
					(short)0
					);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
		if (maplistObjects != null && (maplistObjects.size() > 0)) {
			Iterator itr = maplistObjects.iterator();
			while (itr.hasNext() && ichkvalue != 1) {
				Map mapObject = (Map) itr.next();
				//commented to handle both the conditions below
				//ichkvalue     = actualStatelist.contains(mapObject.get("current")) ? 0 : 1;
				
				String strChangeOnHold = (String) mapObject.get(SELECT_INTERFACE_CHANGE_ON_HOLD);
				ichkvalue = actualStatelist.contains(mapObject.get("current")) && "FALSE".equalsIgnoreCase(strChangeOnHold)? 0 : 1;
				if(ichkvalue==1){
					break;
				}
			}

		}
		if((maplistObjects == null || maplistObjects.size()==0) && ChangeConstants.TYPE_CHANGE_ORDER.equals(strTypeName)){
			strMessage = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(), "EnterpriseChangeMgt.Notice.CRIsNotConnectedToCO");
			ichkvalue = 1;
		}
		if(ichkvalue == 1) {
			emxContextUtil_mxJPO.mqlNotice(context,strMessage);
		}

		return ichkvalue;
	}
	/**
	 * This method notifies CA assignees regarding CA assignment
	 * @author yoq
	 * @param context matrix code context object
	 * @param args trigger parameters
	 * @param args[0]-------change object id
	 * @param args[1]-------Change Action relationship helps to retrieve all CAs from CO
	 * @param args[2]-------Assignee relationship helps to retrieve all Assignees from CA
	 * @param args[3]-------Subject key to pass in the notification
	 * @param args[4]-------message key to pass in the notification
	 * @param args[5]-------Suite key to pass in the notification
	 * @throws Exception if the operation fails
	 */
	public int transferOwnershipAndNotifyAssignees(Context context,String []args) throws Exception {

		String changeObjId     = args[0];
		String relChangeAction = PropertyUtil.getSchemaProperty(context, args[1]);
		String relTechAssignee = PropertyUtil.getSchemaProperty(context, args[2]);
		String subjectKey      = args[3];
		String messageKey      = args[4];
		String suiteKey        = args[5];
		String changeActionId  = "";
		Map changeActionMap    = null;
		String techAssignee    = "";
		String owner           = "";

		HashSet assigneeSet = new HashSet();
		try {
			setId(changeObjId);
			String TechAssigeeSelect = "from["+relTechAssignee+"].to.name";
			StringList personSelects = new StringList(TechAssigeeSelect);
			personSelects.add(DomainConstants.SELECT_OWNER);

			personSelects.addElement(SELECT_ID);

			MapList mapList =  getRelatedObjects(context,				           // matrix context
					ChangeConstants.RELATIONSHIP_CHANGE_ACTION,		   // relationship pattern
					ChangeConstants.TYPE_CHANGE_ACTION,  					   	   // object pattern
					personSelects,                      // object selects
					null,            			       // relationship selects
					false,                              // to direction
					true,                        	   // from direction
					(short) 1,                          // recursion level
					null,                               // object where clause
					null,                               // relationship where clause
					(short) 0);

			for (Iterator changeActionIterator = mapList.iterator();changeActionIterator.hasNext();) {
				changeActionMap = (Map)changeActionIterator.next();
				changeActionId  = (String)changeActionMap.get(SELECT_ID);
				techAssignee    = (String)changeActionMap.get(TechAssigeeSelect);
				owner           = (String)changeActionMap.get(DomainConstants.SELECT_OWNER);
				setId(changeActionId);
				if(!ChangeUtil.isNullOrEmpty(techAssignee) &&(!techAssignee.equalsIgnoreCase(owner))) {
					setOwner(context, techAssignee);}
				else{
					return 0;
				}
			}//Notifications will be called through owner change trigger.
		}

		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
		return 0;
	}

	/**
	 *This method performs a check for prerequisites state before moving Change to Complete state
	 * if any of prerequisite state is not complete , then raise a notice
	 * @author
	 * @param context
	 * @param args
	 *           0 - object id
	 *           1 - name of state
	 *           2 - Policy of the object
	 *           3 - typeName
	 *           4 - Property key for Notice
	 *           5 - Suite key
	 * @return integer 0 for success or 1 for failure
	 * @throws Exception  if the operation fails
	 */
	public int checkPrerequisitesBeforeComplete(Context context,String[] args) throws Exception
	{

		String objectId     = args[0];
		String stateName    = args[1];
		String typeName     = args[2];
		String relName      = args[3];
		String propertyKey  = args[4];
		String suiteKey     = args[5];

		MapList mListPrerequisites  = null;
		boolean hasPrerequisites    = false ;
		String currentStateOfPrereq = "";
		String relWhereClause 		= "(attribute[" + ChangeConstants.ATTRIBUTE_PREREQUISITE_TYPE + "]== Mandatory)" ;
		

		try{

                    String sResult = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3",objectId,"state","|");
			StringList stateList = FrameworkUtil.split(sResult, "|");
			setId(objectId);
			StringList sList = new StringList(2);
			sList.addElement(SELECT_ID);
			sList.addElement(SELECT_CURRENT);
			//get the related Prerequisites in the MapList
			mListPrerequisites = getRelatedObjects(context,
					PropertyUtil.getSchemaProperty(context, relName),//IR-248902V6R2014x
					PropertyUtil.getSchemaProperty(context, typeName),
					sList,
					null,
					false,
					true,
					(short)1,
					EMPTY_STRING,
					relWhereClause,(short) 0);
			for (Iterator prereqIterator = mListPrerequisites.iterator(); prereqIterator.hasNext();)
			{
				currentStateOfPrereq =  ((Map) prereqIterator.next()).get(SELECT_CURRENT).toString();;
				//If prerequisite change state is less than current change state means raise notice
				if(changeUtil.checkObjState(context, stateList, currentStateOfPrereq, stateName, ChangeConstants.LT) == 0)
				{
					hasPrerequisites =  true ;
					break ;
				}
			}
		}
		catch(Exception d){
			d.printStackTrace();
		}

		if(hasPrerequisites)
		{
			String strAlertMessage = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(),propertyKey);
			emxContextUtilBase_mxJPO.mqlNotice(context, strAlertMessage);
			return 1 ;
		}
		else
			return 0;
	}
	/**
	 * @author
	 * triggers the promotion process of Change to Complete
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public int ProcessChange (Context context, String []args) throws Exception {
		String changeObjectId = args[0];
		String nextState      = args[1];
		//String sChangActionType = "";
		String changeAction   = "";
		//String changeActionPolicy = "";
		String stateCompleteMapping = "";
		Map<String,String> changeActionMap = null;
        int retCode = 0;
		try {
			//get the related Change Actions from change
			setId(changeObjectId);
			StringList objectSelects = new StringList(3);
			//objectSelects.addElement(SELECT_POLICY);
			//objectSelects.addElement(SELECT_TYPE);			
			objectSelects.addElement(SELECT_ID);
			objectSelects.addElement("policy.property[state_Complete].value");

			StringBuffer sbRelPatterns = new StringBuffer(50);
			sbRelPatterns.append(ChangeConstants.TYPE_CHANGE_ACTION);
			
			//IR-457279-3DEXPERIENCER2017x: as the support for Configured ECO is removed, this code also needs to be removed
			
			//to support release Configured ECO
			/*
			String sCusXCEType = ECMAdmin.getCustomChange(context, "XCE").getType();
			if(sCusXCEType != null && sCusXCEType.length() > 0){
				sbRelPatterns.append(",");
				sbRelPatterns.append(sCusXCEType);
			}
			*/
			
			//StringBuffer sbXCEChgActions = new StringBuffer();
			MapList mapList=  getRelatedObjects(context,                     // matrix context
					ChangeConstants.RELATIONSHIP_CHANGE_ACTION,  // relationship pattern
					//ChangeConstants.TYPE_CHANGE_ACTION,  					 // object pattern
					sbRelPatterns.toString(),
					objectSelects,               // object selects
					null,            			 // relationship selects
					false,                       // to direction
					true,                        // from direction
					(short) 0,                   // recursion level
					null,                        // object where clause
					null,                        // relationship where clause
					(short) 0);
			for(Iterator changeActionItr = mapList.iterator();changeActionItr.hasNext();) {
				changeActionMap = (Map<String,String>)changeActionItr.next();
				//sChangActionType = changeActionMap.get(SELECT_TYPE);
				changeAction    = changeActionMap.get(SELECT_ID);
				/*
				if(sChangActionType.equals(sCusXCEType)){
					if(sbXCEChgActions.length() > 0){
						sbXCEChgActions.append(",");
					}
					
					sbXCEChgActions.append(changeAction);
				} else {
				*/				
					//changeActionPolicy   = changeActionMap.get(SELECT_POLICY);
					stateCompleteMapping = changeActionMap.get("policy.property[state_Complete].value");
					if(!ChangeUtil.isNullOrEmpty(stateCompleteMapping)) {
						setId(changeAction);
						setState(context, stateCompleteMapping);
					}
				//}
			}
            
            //IR 397076, 403119-3DEXPERIENCER2016x-FD02
            IChangeActionServices iCaServices = ChangeActionFactory.CreateChangeActionFactory();
            retCode = iCaServices.automatizeCaFromCo(context, changeObjectId);    
            
            //this should be executed last for configured ECO supports
			//		IR-426964-3DEXPERIENCER2016x            
            /*
			if(retCode == 0 && sbXCEChgActions.length() > 0){	    			
				String releaseJPO = ECMAdmin.getCustomChangeReleaseJPO(context, "XCE");
    			if (UIUtil.isNotNullAndNotEmpty(releaseJPO)) {
    				String programName = releaseJPO.replaceAll(":.*$", "").trim();
    				String methodName = releaseJPO.replaceAll("^.*:", "").trim();
    				//XCE Change Action objects is separated by ',' if more than one
    				//mkmk JDK has not yet supported String.join
    				//String sXCEChgActions = (slXCEChgActions.size() == 1)?slXCEChgActions.get(0):String.join(",", slXCEChgActions);
    				
    				String[] saXCEArgs = new String[2];
    				saXCEArgs[0] = changeObjectId;
   					saXCEArgs[1] = sbXCEChgActions.toString();
    				
    				//sXCEErrMsg = EnoviaResourceBundle.getProperty(context,
					//		ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(), "EnterpriseChangeMgt.Error.JPOProgramMethodError");
    				//sXCEErrMsg += releaseJPO;
    				retCode = JPO.invoke(context,programName, null, methodName, saXCEArgs);
    			}else{
    				String strError = EnoviaResourceBundle.getProperty(context,
							ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(), "EnterpriseChangeMgt.Error.JPOProgramMethodDoesNotExist");
    				retCode = 1;
					throw new FrameworkException(strError);
    			}
			}
			*/   
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
		return retCode;
	}

	/**
	 * This trigger method notifies the owner/originator/Distribution List members regarding change Completion.
	 *
	 * @author
	 * @param context The ematrix context of the request.
	 * @param args The string array containing the following arguments
	 *          0 - The object id of the context Engineering Change Object
	 *          1 - The attribute person selectables
	 *          2 - The relationship person selectables
	 * @throws Exception
	 * @throws FrameworkException
	 * @since ECM R211
	 */
	public int notifyOnComplete(Context context, String[] args)
			throws Exception, FrameworkException {
		//Get the Object Id and owner of the context Engineering Change object.
		String strObjectId     = args[0];
		String subjectKey      = args[1];
		String messageKey      = args[2];
		String propertyKey     = args[3];

		StringList ccList  = new StringList();
		StringList bccList = new StringList();
		StringList toList  = new StringList();
		StringList lstAttachments = new StringList();

		try {
			lstAttachments.add(strObjectId);
			changeOrder = new ChangeOrder(strObjectId);
			toList = changeOrder.getToListForChangeProcess(context);
			
			String type = changeOrder.getTypeName();
			String name = changeOrder.getName();
			String revision = changeOrder.getRevision();

			
			String subject = EnoviaResourceBundle.getProperty(context, propertyKey,context.getLocale(),  subjectKey);
			String description = EnoviaResourceBundle.getProperty(context, propertyKey,context.getLocale(),  messageKey);
			
					MailUtil.sendNotification(context,
					toList,
					ccList,
					bccList,
					subject,
						new String[]{"type","name","revision"},
						new String[]{type,name,revision},
					description, 
						new String[]{"type","name","revision"},
						new String[]{type,name,revision},
					new StringList(strObjectId),
					context.getRole(),
					propertyKey); 

		}
		catch (Exception ex) {
			ex.printStackTrace();
			return 1;
		}
		return 0;

	}
	/**
	 * Displays Reviewer list members of Route Template in CO Properties page
	 * @author
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String getReviewerListMembers(Context context,String []args) throws Exception {

		//XSSOK
		HashMap paramMap   = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) paramMap.get(ChangeConstants.REQUEST_MAP);
		String objectId    = (String)requestMap.get(ChangeConstants.OBJECT_ID);

		// For export to CSV
		String exportFormat = null;
		boolean exportToExcel = false;
		if(requestMap!=null && requestMap.containsKey("reportFormat")){
			exportFormat = (String)requestMap.get("reportFormat");
		}
		if("CSV".equals(exportFormat)){
			exportToExcel = true;
		}

		changeOrderUI.setId(objectId);
		return changeOrderUI.getMembersBasedOnPurpose(context,"Review",exportToExcel);
	}

	/**
	 * Displays Approver list members of Route Template in CO Properties page
	 * @author
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String getApprovalListMembers(Context context,String []args) throws Exception {

		//XSSOK
		HashMap paramMap   = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) paramMap.get(ChangeConstants.REQUEST_MAP);
		String objectId    = (String)requestMap.get(ChangeConstants.OBJECT_ID);
		// For export to CSV
		String exportFormat = null;
		boolean exportToExcel = false;
		if(requestMap!=null && requestMap.containsKey("reportFormat")){
			exportFormat = (String)requestMap.get("reportFormat");
		}
		if("CSV".equals(exportFormat)){
			exportToExcel = true;
		}

		changeOrderUI.setId(objectId);
		return changeOrderUI.getMembersBasedOnPurpose(context,"Approval",exportToExcel);

	}
	/**This method includes Responsible Organisation OIDs for Create CO RO field search.
	 * @author
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList includeROs(Context context, String []args) throws Exception {

		HashMap paramMap   = (HashMap) JPO.unpackArgs(args);
		StringList buList  = new StringList();
		MapList mapList    = new MapList();
		String changeTemplateOID  = (String)paramMap.get(ChangeConstants.CHANGE_TEMPLATE_OID);
		String companyOID         = "";
		StringList objSelects = new StringList(SELECT_ID);
		objSelects.add(SELECT_NAME);
		companyOID = !changeUtil.isNullOrEmpty(changeTemplateOID) ? ChangeUtil.getRtoIdFromName(context, (String)DomainObject.newInstance(context, changeTemplateOID).getInfo(context, SELECT_ORGANIZATION))
				: Person.getPerson(context).getCompanyId(context);


				if(!changeUtil.isNullOrEmpty(companyOID)) {
					StringBuffer relPattern = new StringBuffer(RELATIONSHIP_DIVISION).append(",").append(RELATIONSHIP_COMPANY_DEPARTMENT);
					setId(companyOID);
					String where = "current=='Active'";
					mapList      =  getRelatedObjects(  context,                   // matrix context
							relPattern.toString(),     // relationship pattern
							"*",                       // object pattern
							objSelects,                      // object selects
							null,                      // relationship selects
							true,                      // to direction
							true,                      // from direction
							(short) 0,                 // recursion level
							where,                     // object where clause
							null,                      // relationship where clause
							(short) 0);
				}
				if (mapList.size() > 0) {
					return changeUtil.getStringListFromMapList(mapList, SELECT_ID);
				}
				else {
					buList.addElement(Company.getHostCompany(context));
					return buList;
				}
	}
	/**
	 * excludeAffectedItems() method returns OIDs of Affect Items
	 * which are already connected to context change object
	 * @param context Context : User's Context.
	 * @param args String array
	 * @return The StringList value of OIDs
	 * @throws Exception if searching Parts object fails.
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeAffectedItems(Context context, String args[])throws Exception
	{
		
		HashMap programMap = (HashMap) JPO.unpackArgs(args);		 
		String  strChangeId = (String) programMap.get("objectId");
		changeOrder = new ChangeOrder(strChangeId);
	
		StringList strlAffItemList = changeOrder.excludeProposedItems(context);
		return strlAffItemList;
		/*	
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		 
		String  strChangeId = (String) programMap.get("objectId");
		StringList strlAffItemList = new StringList();

		if (ChangeUtil.isNullOrEmpty(strChangeId))
			return strlAffItemList;

		try
		{
			setId(strChangeId);
			StringList changeActionList = getInfoList(context, "from["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].to.id");

			String relPattern =  new StringBuffer(ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM).append(",")
			.append(StringUtil.join(ECMAdmin.getAllCustomChangeRels(context), ","))
			.toString();
			MapList resultList = null;
			Map map = null;

			for (int i=0;i < changeActionList.size() ; i++)
			{
				setId((String)changeActionList.get(i));
				resultList = getRelatedObjects(context, relPattern,
						  "*",
						  new StringList(DomainObject.SELECT_ID),
						  null,
						  false,
						  true,
						  (short) 2,
						  "",
						  "");
				
				Iterator itr = resultList.iterator();
				while(itr.hasNext()) {
					map = (Map)itr.next();
					strlAffItemList.addElement((String)map.get(DomainObject.SELECT_ID));
				}
			}

		}catch (Exception e) {
			e.printStackTrace();
		}
		return strlAffItemList;
		*/
	}

	/**
	 * excludeCandidateItems() method returns OIDs of Candidate Items
	 * which are already connected to context change object
	 * @param context Context : User's Context.
	 * @param args String array
	 * @return The StringList value of OIDs
	 * @throws Exception if searching Parts object fails.
	 */
@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
public StringList excludeCandidateItems(Context context, String args[])throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String  strChangeId = (String) programMap.get("objectId");
		StringList strlAffItemList = new StringList();

		if (ChangeUtil.isNullOrEmpty(strChangeId))
			return strlAffItemList;

		try
		{
			setId(strChangeId);
			strlAffItemList.addAll(getInfoList(context, "from["+ChangeConstants.RELATIONSHIP_CANDIDATE_AFFECTED_ITEM+"].to.id"));
			StringBuffer relPattern = new StringBuffer(ChangeConstants.RELATIONSHIP_CHANGE_ACTION).append(",").append(ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM);
			StringList objSelects = new StringList(SELECT_ID);
			Map objMap;
			MapList mapList      =  getRelatedObjects(  context,                   // matrix context
					relPattern.toString(),     // relationship pattern
					"*",                       // object pattern
					objSelects,                      // object selects
					null,                      // relationship selects
					false,                      // to direction
					true,                      // from direction
					(short) 2,                 // recursion level
					null,                     // object where clause
					null,                      // relationship where clause
					(short) 0);

			for(Iterator mapListItr = mapList.iterator();mapListItr.hasNext(); ) {
				objMap = (Map)mapListItr.next();
				if(objMap.get("relationship").equals(ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM))
					strlAffItemList.add(objMap.get("id"));
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return strlAffItemList;
	}

	/**
	 * @author R3D
	 * Updates the Change Template in CO WebForm.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a MapList with the following as input arguments or entries:
	 * objectId holds the context CO object Id
	 * @throws Exception if the operations fails
	 * @since ECM-R215
	 */
	public DomainRelationship connectChangeTemplate(Context context, String[] args) throws Exception {

		try {
			//unpacking the Arguments from variable args
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap   = (HashMap)programMap.get("paramMap");
			HashMap requestMap = (HashMap)programMap.get("requestMap");
			String objectId    = (String)paramMap.get(ChangeConstants.OBJECT_ID);
			changeOrder.setId(objectId);

			String[] sChangeTemplateOID = (String[])requestMap.get("ChangeTemplateOID");
			String ssChangeTemplateID = sChangeTemplateOID[0];
			String sInterfaceName = "";
			String sReferenceDoc = "";
			Iterator intrItr;
			String sCOType = changeOrder.getInfo(context, SELECT_TYPE);
			StringList intrList;
			String[] arrRefDoc;

			//Connecting Reference Document of Template to CO
			DomainObject dmObj = DomainObject.newInstance(context);
			if(!UIUtil.isNullOrEmpty(ssChangeTemplateID)){
				dmObj.setId(ssChangeTemplateID);
				//IR-248788V6R2014x
				intrList = dmObj.getInfoList(context, "from["+RELATIONSHIP_REFERENCE_DOCUMENT+"].to.id");
				if(!UIUtil.isNullOrEmpty(intrList.toString().trim())){
					arrRefDoc = (String[])intrList.toArray(new String[0]);
					changeOrder.addRelatedObjects(context, new RelationshipType(RELATIONSHIP_REFERENCE_DOCUMENT) , true, arrRefDoc);
				}
			}
			//This Logic is moved to Create Jpo method (createChange)
			//Logic to apply Interface of Template to CO
			/*if(!UIUtil.isNullOrEmpty(ssChangeTemplateID)){
                            sInterfaceName = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3",ssChangeTemplateID,"interface","|");
				if(!UIUtil.isNullOrEmpty(sInterfaceName)){
					intrItr         = FrameworkUtil.split(sInterfaceName, "|").iterator();
					//Add all Change Template Interfaces to CO
					for(int i=0; intrItr.hasNext();i++){
						MqlUtil.mqlCommand(context, "modify bus $1 add interface $2",objectId,(String)intrItr.next());
					}

				}
			}*/


			return changeOrder.connect(context,paramMap,ChangeConstants.RELATIONSHIP_CHANGE_INSTANCE, false);
		}

		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
	}//end of method



	/**
	 * @author R3D
	 * Updates the Change Template in CO Clone WebForm.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a MapList with the following as input arguments or entries:
	 * objectId holds the context CO object Id
	 * @throws Exception if the operations fails
	 * @since ECM-R215
	 */
	public DomainRelationship connectTemplateToCloneCO(Context context, String[] args) throws Exception {

		try {
			//unpacking the Arguments from variable args
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap   = (HashMap)programMap.get("paramMap");
			HashMap requestMap = (HashMap)programMap.get("requestMap");
			String objectId    = (String)paramMap.get(ChangeConstants.OBJECT_ID);
			changeOrder.setId(objectId);

			String[] sChangeTemplateOID = (String[])requestMap.get("ChangeTemplate2OID");
			String ssChangeTemplateID = sChangeTemplateOID[0];
			String sInterfaceName = "";
			String sReferenceDoc = "";
			Iterator intrItr;
			StringList intrList;
			String[] arrRefDoc;
			String sCOType = changeOrder.getInfo(context, SELECT_TYPE);

			//Connecting Reference Document of Template to CO
			DomainObject dmObj = DomainObject.newInstance(context);
			if(!UIUtil.isNullOrEmpty(ssChangeTemplateID)){
				dmObj.setId(ssChangeTemplateID);
				//IR-248788V6R2014x modification
				/*sReferenceDoc = dmObj.getInfo(context, "from["+RELATIONSHIP_REFERENCE_DOCUMENT+"].to.id");
				if(!UIUtil.isNullOrEmpty(sReferenceDoc)){
					changeOrder.addRelatedObject(context, new RelationshipType(RELATIONSHIP_REFERENCE_DOCUMENT) , false, sReferenceDoc);
				}*/

				intrList = dmObj.getInfoList(context, "from["+RELATIONSHIP_REFERENCE_DOCUMENT+"].to.id");
				if(!UIUtil.isNullOrEmpty(intrList.toString().trim())){
					arrRefDoc = (String[])intrList.toArray(new String[0]);
					changeOrder.addRelatedObjects(context, new RelationshipType(RELATIONSHIP_REFERENCE_DOCUMENT) , true, arrRefDoc);
				}
			}

			//Logic to apply Interface of Template to CO
			if(!UIUtil.isNullOrEmpty(ssChangeTemplateID)){
                            sInterfaceName = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3",ssChangeTemplateID,"interface","|");
				if(!UIUtil.isNullOrEmpty(sInterfaceName)){
					intrItr         = FrameworkUtil.split(sInterfaceName, "|").iterator();
					//Add all Change Template Interfaces to CO
					for(int i=0; intrItr.hasNext();i++){
						MqlUtil.mqlCommand(context, "modify bus $1 add interface $2",objectId,(String)intrItr.next());
					}
				}
			}


			return changeOrder.connect(context,paramMap,ChangeConstants.RELATIONSHIP_CHANGE_INSTANCE, false);
		}

		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
	}//end of method

	/**
	 * Display Quick Actions for each CA Object under CO Affected Items Table
	 * @param context
	 * @param args
	 * @return Vector containing list of Quick Actions
	 * @throws Exception
	 * @since R211 ECM
	 */
	public Vector showQuickActions(Context context, String[] args) throws Exception
	{
		//XSSOK
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get(ChangeConstants.OBJECT_LIST);
		String languageStr = context.getSession().getLanguage();
		Vector vecReturn   = null;
		StringBuffer sb    = null;
		try
		{
			String transferOwnership = EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Message.TransferOwnershipOfCA");
			String approvalTasks     = EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Message.ApprovalTasksOfCA");
			String ImpactAnalysis    = EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.alt.ImpactAnalysis");

			vecReturn = new Vector(objectList.size());

			String strCAId				= "";
			String strType				= "";
			String strTransferOwnership	= "";
			String strApproveTasks		= "";
			String strImpactAnalysis    = "";
			Map changeActionMap = null;

			Iterator changeActionItr = objectList.iterator();

			while (changeActionItr.hasNext()) {
				changeActionMap = (Map)changeActionItr.next();
				strCAId = (String)changeActionMap.get(ChangeConstants.ID);
				strType = (String)changeActionMap.get(ChangeConstants.TYPE);

				sb = new StringBuffer(500);

				if(mxType.isOfParentType(context, strType, ChangeConstants.TYPE_CHANGE_ACTION)){
				strTransferOwnership = "<a href=\"javascript:getTopWindow().showSlideInDialog('../common/emxForm.jsp?form=type_TransferOwnership&amp;formHeader=TransferOwnership&amp;mode=edit&amp;submitAction=refreshCaller&amp;postProcessJPO=enoECMChangeUtil:transferOwnership&amp;objectId=" + XSSUtil.encodeForHTMLAttribute(context, strCAId) + "', 'true')\"><img border='0' src='../common/images/iconSmallPerson.gif' name='person' id='person' alt=\""+transferOwnership+"\" title=\""+transferOwnership+"\"/></a>";
				sb.append(strTransferOwnership);

				strApproveTasks = "<a href=\"javascript:showModalDialog('../common/emxTableEdit.jsp?program=enoECMChangeOrder:getTasks&amp;table=AEFMyTaskMassApprovalSummary&amp;selection=multiple&amp;header=emxComponents.Common.TaskMassApproval&amp;postProcessURL=../common/emxLifecycleTasksMassApprovalProcess.jsp&amp;HelpMarker=emxhelpmytaskmassapprove&amp;suiteKey=Components&amp;StringResourceFileId=emxComponentsStringResource&amp;SuiteDirectory=component&amp;objectId=" + XSSUtil.encodeForHTMLAttribute(context, strCAId) + "', '800', '575')\"><img border='0' src='../common/images/iconSmallSignature.gif' name='person' id='person' alt=\""+approvalTasks+"\" title=\""+approvalTasks+"\"/></a>";
				sb.append(strApproveTasks);

				strImpactAnalysis = "<a href=\"javascript:showModalDialog('../common/emxTree.jsp?objectId=" + XSSUtil.encodeForHTMLAttribute(context, strCAId) + "&amp;DefaultCategory="+XSSUtil.encodeForURL("Impact Analysis")+"', '800', '575')\"><img border='0' src='../common/images/iconSmallImpactAnalysis.gif' name='IA' id='IA' alt=\""+ImpactAnalysis+"\" title=\""+ImpactAnalysis+"\"/></a>";				
				sb.append("&#160;&#160;"+strImpactAnalysis);
				}

				vecReturn.addElement(sb.toString());

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e);
		}

		return vecReturn;

	}
	/**
	 * Display Affected Item names for each CA Object under CO Affected Items Table
	 * @param context
	 * @param args
	 * @return Vector containing list of Quick Actions
	 * @throws Exception
	 * @since R211 ECM
	 */
	public Vector showAffectedItemNames(Context context, String[] args) throws Exception
	{
		//XSSOK
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get(ChangeConstants.OBJECT_LIST);
		HashMap paramList = (HashMap)programMap.get("paramList");
		String strReportFormat = (String) paramList.get("reportFormat");
		Vector vecReturn   = null;
		StringBuffer sb    = null;
		try
		{
			vecReturn = new Vector(objectList.size());
			Map map = null;
			String strAffectedObjID     = "";
			String strAffectedObjType   = "";
			String strAffectedObjName   = "";
			String strApproveTasks		= "";
			StringBuffer objectIcon		= new StringBuffer();

			Iterator objectItr = objectList.iterator();

			while (objectItr.hasNext()) {
				map = (Map)objectItr.next();
				sb = new StringBuffer(500);
				strAffectedObjID = (String)map.get(ChangeConstants.AFFECTED_ITEM_ID);
				if(!ChangeUtil.isNullOrEmpty(strAffectedObjID)) {
					strAffectedObjType 	 = (String)map.get(SELECT_TYPE);
					strAffectedObjName   = (String)map.get(SELECT_NAME);
					objectIcon.append(UINavigatorUtil.getTypeIconProperty(context, strAffectedObjType));
					if(strReportFormat!=null&&strReportFormat.equals("null")==false&&strReportFormat.equals("")==false){
						sb.append(strAffectedObjName);
					}
					else{
						sb.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=" + XSSUtil.encodeForHTMLAttribute(context, strAffectedObjID) + "', '800', '575')\"><img border='0' src='../common/images/"+XSSUtil.encodeForHTMLAttribute(context, objectIcon.toString())+"'/>"+XSSUtil.encodeForHTML(context, strAffectedObjName)+"</a>");
                    objectIcon.setLength(0);
					}
				}
				else
					sb.append("");

				vecReturn.addElement(sb.toString());

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e);
		}

		return vecReturn;

	}
	/**
	 * Display Affected Item names for each CA Object under CO Affected Items Table
	 * @param context
	 * @param args
	 * @return Vector containing list of Quick Actions
	 * @throws Exception
	 * @since R211 ECM
	 */
	public Vector showRelatedCANames(Context context, String[] args) throws Exception
	{
		//XSSOK
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get(ChangeConstants.OBJECT_LIST);
		Vector vecReturn   = null;
		StringBuffer sb    = null;
		try
		{
			vecReturn = new Vector(objectList.size());
			Map map = null;
			String strAffectedObjID     = "";
			String strAffectedObjType   = "";
			String strAffectedObjName   = "";
			String strTreeLink			= "";
			String strApproveTasks		= "";
			StringBuffer objectIcon		= new StringBuffer();

			Iterator objectItr = objectList.iterator();

			while (objectItr.hasNext()) {
				map = (Map)objectItr.next();
				sb = new StringBuffer(500);
				strAffectedObjID = (String)map.get(ChangeConstants.RELATED_CA_ID);
				if(!ChangeUtil.isNullOrEmpty(strAffectedObjID)) {
					strAffectedObjType 	 = (String)map.get(ChangeConstants.RELATED_CA_TYPE);
					strAffectedObjName   = (String)map.get(ChangeConstants.RELATED_CA_NAME);

					objectIcon.append(UINavigatorUtil.getTypeIconProperty(context, strAffectedObjType));
					strTreeLink = "<a class=\"object\" href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=" + XSSUtil.encodeForHTMLAttribute(context, strAffectedObjID) + "&amp;DefaultCategory=ECMCAAffectedItems', '800', '575','true','content')\"><img border='0' src='../common/images/"+XSSUtil.encodeForHTMLAttribute(context, objectIcon.toString())+"'/>"+XSSUtil.encodeForHTML(context, strAffectedObjName)+"</a>";
					sb.append(strTreeLink);
                    objectIcon.setLength(0);

				}
				else
					sb.append("");

				vecReturn.addElement(sb.toString());

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e);
		}

		return vecReturn;

	}

	/**
	 * Update Technical Assignee under CO Affected Items Table
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 * @since R211 ECM
	 */
	public Boolean updateTechnicalAssignee(Context context, String[] args)throws Exception {

		try
		{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap   = (HashMap)programMap.get(ChangeConstants.PARAM_MAP);
			String  objectId   = (String)paramMap.get(ChangeConstants.OBJECT_ID);
			ChangeAction changeAction = new ChangeAction(objectId);
			return changeAction.updateAssignee(context, programMap, ChangeConstants.RELATIONSHIP_TECHNICAL_ASSIGNEE);

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e);
		}
	}

	/**
	 * Update Change Owner under CO summary Table
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 */
	public Boolean updateOwner(Context context, String[] args)throws Exception {

		try
		{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap   = (HashMap)programMap.get(ChangeConstants.PARAM_MAP);
			String  objectId   = (String)paramMap.get(ChangeConstants.OBJECT_ID);
			ChangeOrder changeOrderobj = new ChangeOrder(objectId);
			
	   		String strNewOwner	= (String)paramMap.get(ChangeConstants.NEW_VALUE);
	   		changeOrderobj.transferOwnership(context, EMPTY_STRING, strNewOwner);
	   		return true;

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e);
		}
	}
	
	
	/**
	 * Update Change Coordinator under CO summary Table
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 */
	public Boolean updateChangeCoordinator(Context context, String[] args)throws Exception {

		try
		{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap   = (HashMap)programMap.get(ChangeConstants.PARAM_MAP);
			String  objectId   = (String)paramMap.get(ChangeConstants.OBJECT_ID);
			ChangeOrder changeOrderobj = new ChangeOrder(objectId);
			
	   		String strNewOwner	= (String)paramMap.get(ChangeConstants.NEW_VALUE);
	   		changeOrderobj.updateChangeCoordinator(context, strNewOwner);
	   		return true;

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e);
		}
	}

	/**
	 * Update Technical Assignee under CO Affected Items Table
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 * @since R211 ECM
	 */
	public Boolean updateSeniorTechnicalAssignee(Context context, String[] args)throws Exception {

		try
		{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap   = (HashMap)programMap.get(ChangeConstants.PARAM_MAP);
			String  objectId   = (String)paramMap.get(ChangeConstants.OBJECT_ID);
			ChangeAction changeAction = new ChangeAction(objectId);
			return changeAction.updateAssignee(context, programMap, ChangeConstants.RELATIONSHIP_SENIOR_TECHNICAL_ASSIGNEE);

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e);
		}
	}
	/**
	 * Update Planned End Date of the context CA under CO Affected Items Table
	 * @param context
	 * @param args
	 * @return int
	 * @throws Exception
	 * @since R211 ECM
	 */
	public int updatePlannedEndDate(Context context, String[] args) throws Exception
	{
		try {

			HashMap programMap	= (HashMap)JPO.unpackArgs(args);
			HashMap paramMap 	= (HashMap)programMap.get(ChangeConstants.PARAM_MAP);

			String strEstCompletionDate	= ChangeConstants.ATTRIBUTE_ESTIMATED_COMPLETION_DATE;
			String CAObjectID 		 	= (String)paramMap.get(ChangeConstants.OBJECT_ID);
			String newDateValue  		= (String)paramMap.get(ChangeConstants.NEW_VALUE);

			HashMap columnMap   = (HashMap)programMap.get("columnMap");
			HashMap requestMap  = (HashMap)programMap.get(ChangeConstants.REQUEST_MAP);
			HashMap settingsMap = (HashMap)columnMap.get("settings");
			String strFormat    = (String) settingsMap.get("format");

			if ("date".equalsIgnoreCase(strFormat) && !ChangeUtil.isNullOrEmpty(newDateValue)) {

				double iClientTimeOffset = (new Double((String) requestMap.get("timeZone"))).doubleValue();
				String strTempStartDate= eMatrixDateFormat.getFormattedInputDate(newDateValue,iClientTimeOffset,(java.util.Locale)(requestMap.get("locale")));
				DomainObject.newInstance(context, CAObjectID).setAttributeValue(context, strEstCompletionDate, strTempStartDate);

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e);
		}

		return 0;
	}



	/**
	 * Fetch all the Inbox Tasks related to context CA under CO Affected Items Table
	 * @param context
	 * @param args
	 * @return MapList
	 * @throws Exception
	 * @since R211 ECM
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTasks(Context context, String args[]) throws Exception {

		HashMap progMap    = (HashMap) JPO.unpackArgs(args);
		String strObjectId = (String) progMap.get(ChangeConstants.OBJECT_ID);
		MapList mapList    = new MapList();
		try {
			if(!ChangeUtil.isNullOrEmpty(strObjectId)) {
				changeOrderUI = new ChangeOrderUI(strObjectId);
				mapList = changeOrderUI.getCurrentAssignedTasksOnObject(context);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
		return mapList;
	}

	/**
	 * This method checks whether TechAssignees for all CAs under CO is assigned or not.
	 * @author
	 * @param context
	 *            the eMatrix <code>Context</code> object.
	 * @param args
	 *            holds objectId.
	 * @param args
	 *            holds relationship name
	 * @return integer 0 if Change Coordinator is assigned else 1
	 * @throws Exception if the operation fails.
	 * @since ECM R211
	 */
	public int CheckForAssignees(Context context, String args[]) throws Exception {
		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}

		try {
			String objectId = args[0];
			String relationshipChangeAction 		  = PropertyUtil.getSchemaProperty(context,args[1]);
			String relationshipTechnicalAssignee   = PropertyUtil.getSchemaProperty(context,args[2]);
			String relationshipSrTechnicalAssignee = PropertyUtil.getSchemaProperty(context,args[3]);
			String resourceKey = args[4];
			String propertyKey = args[5];

			setId(objectId);
			StringList  changeActionList = getInfoList(context,"from["+relationshipChangeAction+"].to[" + ChangeConstants.TYPE_CHANGE_ACTION + "].id");
			
			if (changeActionList.size() > 0) {
				String Message               = EnoviaResourceBundle.getProperty(context, resourceKey, context.getLocale(),propertyKey);
	
				String techAssigneeSelect   = "from["+relationshipTechnicalAssignee+"].to.id";
				//String SrtechAssigneeSelect = "from["+relationshipSrTechnicalAssignee+"].to.id";
	
				StringList objSelects = new StringList(2);
				objSelects.addElement(techAssigneeSelect);
				//objSelects.addElement(SrtechAssigneeSelect);
				objSelects.addElement(DomainObject.SELECT_CURRENT);
	
				MapList mapList = DomainObject.getInfo(context, (String[])changeActionList.toArray(new String[changeActionList.size()]), objSelects);
	
				Map objMap;String techAssignee;String SrTechAssignee;String currentState;
				for(Iterator mapListItr = mapList.iterator();mapListItr.hasNext(); ) {
					objMap = (Map)mapListItr.next();
					techAssignee   = (String)objMap.get(techAssigneeSelect);
					//SrTechAssignee = (String)objMap.get(SrtechAssigneeSelect);
					currentState=(String)objMap.get(DomainObject.SELECT_CURRENT);
					if(changeUtil.isNullOrEmpty(techAssignee) && !ChangeConstants.STATE_CHANGE_ACTION_CANCEL.equalsIgnoreCase(currentState)) {
						//Sends a warning message like Techassignees should be assigned before moving to next state.
						emxContextUtilBase_mxJPO.mqlNotice(context, Message);
						return 1;
					}
				}
			}

		}catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
		return 0;
	}




	/**
	 * This method updates attribute Estimated Completion Date on CO by retrieving longer estimated completion date of all related CAs.
	 * @author
	 * @param context
	 *            the eMatrix <code>Context</code> object.
	 * @param args
	 *            holds objectId.
	 * @param args
	 *            holds relationship name
	 * @return integer 0 if Change Coordinator is assigned else 1
	 * @throws Exception if the operation fails.
	 * @since ECM R211
	 */
	public int updateCAHighestEstimationCompletionDateOnCO(Context context, String args[]) throws Exception {
		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}
		String changeOrderId 	   = "";
		DomainObject changeOrderObj = null;
		MapList changeActionList    = null;
		StringList objectSelects    = new StringList(1);
		StringList relSelects    = new StringList(1);
		
		Map changeActionMap     = null;
		String estimationDate   = "";
		Date estimationJavaDate = null;
		long []arr = null;
		long timeInMilliSeconds;


		try {
			String objectId      = args[0];
			String attributeName = args[1];

			if(!ChangeConstants.ATTRIBUTE_ESTIMATED_COMPLETION_DATE.equalsIgnoreCase(attributeName))
				return 0;

			setId(objectId);
			String changeOrderSelect  = "to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].from["+ChangeConstants.TYPE_CHANGE_ORDER+"].id";
			changeOrderId =  MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",objectId,changeOrderSelect);
				
			if(!ChangeUtil.isNullOrEmpty(changeOrderId)){
					changeOrderObj = DomainObject.newInstance(context, changeOrderId);
					objectSelects.addElement(ChangeConstants.SELECT_ATTRIBUTE_ESTIMATED_COMPLETION_DATE);
					relSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
					
					changeActionList =  changeOrderObj.getRelatedObjects(context,						   // matrix context
																		ChangeConstants.RELATIONSHIP_CHANGE_ACTION,		   // relationship pattern
																		ChangeConstants.TYPE_CHANGE_ACTION,  			   // object pattern
																		objectSelects,                     // object selects
																		relSelects,            			       // relationship selects
																		false,                             // to direction
																		true,                        	   // from direction
																		(short) 1,                         // recursion level
																		null,                              // object where clause
																		null,                              // relationship where clause
																		(short) 0);						   // object limit


				int size = changeActionList.size();
				arr = new long[size];
				for(int i=0;i<size;i++) {
					changeActionMap = (Map)changeActionList.get(i);
					estimationDate  = (String)changeActionMap.get(ChangeConstants.SELECT_ATTRIBUTE_ESTIMATED_COMPLETION_DATE);
					if(ChangeUtil.isNullOrEmpty(estimationDate))continue;
					timeInMilliSeconds = eMatrixDateFormat.getJavaDate(estimationDate).getTime();
					arr[i]  = timeInMilliSeconds;
				}
				Arrays.sort(arr);
				long highestDate = arr[arr.length-1];
				SimpleDateFormat _mxDateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
				DomainObject.newInstance(context,changeOrderId).setAttributeValue(context, ChangeConstants.ATTRIBUTE_ESTIMATED_COMPLETION_DATE, _mxDateFormat.format(new Date(highestDate)));
			}		 

		}catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
		return 0;
	}

	/**
	 * This method displays Hold Affected CAs Warning field of Hold CO form with radio button
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args[] packed hashMap of request parameters
	 * @return String containing html data to construct with radio button.
	 * @throws Exception if the operation fails.
	 * @since R212
	 */

	public Object displayHoldCAsWarning(Context context, String[] args) throws Exception
	{
		//XSSOK
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap   = (HashMap)programMap.get("paramMap");
		String languageStr = (String) paramMap.get("languageStr");

		String CAConnections  = EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Label.holdWarning");

		StringBuffer strBuf = new StringBuffer();
		strBuf.append("<table><tr><td align=left>");
		strBuf.append("<input type=radio checked name=\"CAHoldItemsWarning\" value=\"true\">");
		strBuf.append("</td><td align=left>");
		strBuf.append(CAConnections);
		strBuf.append("</td></tr></table>");

		return strBuf.toString();
	}
	public void sendNotification(Context context,String []args) throws Exception {


		String objectId		   = args[0];
		String subjectKey      = args[1];
		String messageKey      = args[2];
		String propertyKey     = args[3];

		StringList ccList		  = new StringList();
		StringList bccList		  = new StringList();
		StringList lstAttachments = new StringList();
		lstAttachments.add(objectId);

		changeOrder.setId(objectId);
		StringList toList = changeOrder.getToListForChangeProcess(context);

		emxNotificationUtilBase_mxJPO.sendNotification(context,objectId,toList, ccList, bccList, subjectKey,messageKey, lstAttachments, propertyKey, null,null,null);

	}

	/**
	 * Used to get Tasks of CO/CA for Mass Approval
	 * @param context
	 * @param objectId
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getChangeTasks(Context context, String[] args) throws Exception {
		try {
			HashMap programMap              = (HashMap)JPO.unpackArgs(args);
			HashMap requestMap              = (HashMap)programMap.get("requestMap");
			MapList mlTableData 			= new MapList();
			String objectIDs 				= (String)programMap.get("objectIdToApprove");
			ChangeOrderUI changeOrderUI 	= null;
			StringList objectList 			= FrameworkUtil.split(objectIDs, ",");
			String sObjectId 				= "";
			Map mapTemp = new HashMap();

			Iterator itr = objectList.iterator();
			while(itr.hasNext()){
				sObjectId 		= (String)itr.next();
				changeOrderUI 	= new ChangeOrderUI(sObjectId);
				mlTableData.addAll(changeOrderUI.getCurrentAssignedTasksOnObject(context));
			}
			int nSerialNumber = 0;
            for (Iterator objectListItr = mlTableData.iterator(); objectListItr.hasNext(); nSerialNumber++) {
                mapTemp = (Map) objectListItr.next();

                // Add level value else sorting will give problem
                mapTemp.put("serialNumber", String.valueOf(nSerialNumber));
            }
			return mlTableData;
		}
		catch(Exception exp) {
			exp.printStackTrace();
			throw new FrameworkException(exp.getMessage());
		}
	}


	/**
	 * Show Mass Approval Link in SB Table
	 * @param context
	 * @param objectId
	 * @return
	 * @throws Exception
	 */
	public Vector showQuickMassApproval(Context context, String[] args) throws Exception
	{
		//XSSOK
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get(ChangeConstants.OBJECT_LIST);
		String languageStr = context.getSession().getLanguage();
		Vector vecReturn   = null;
		StringBuffer sb    = null;
		try
		{
			StringList changeOrderIDList = changeUtil.getStringListFromMapList(objectList, ChangeConstants.ID);

			String transferOwnership =EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Message.TransferOwnershipOfCA");

			String approvalTasks     = EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Message.ApprovalTasksOfCA");

			String approvalRequired = EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Label.ApprovalStatus");

			vecReturn = new Vector(changeOrderIDList.size());
			String strCOId				= "";
			String strTransferOwnership	= "";
			String strApproveTasks		= "";

			ChangeOrderUI changeOrderUI 	= null;
			MapList mlTableData 			= new MapList();

			Iterator changeOrderItr = changeOrderIDList.iterator();
			while (changeOrderItr.hasNext()) {
				strCOId = changeOrderItr.next().toString();
				changeOrderUI 	= new ChangeOrderUI(strCOId);
				String type = changeOrderUI.getInfo(context, SELECT_TYPE);

				if(changeOrderUI.isKindOf(context, ChangeConstants.TYPE_CHANGE_ORDER) || changeOrderUI.isKindOf(context, ChangeConstants.TYPE_CHANGE_REQUEST) || changeOrderUI.isKindOf(context, ChangeConstants.TYPE_CHANGE_ACTION)){
					mlTableData = changeOrderUI.getCurrentAssignedTasksOnObject(context);

					if(!mlTableData.isEmpty()){
						sb = new StringBuffer(500);

						strApproveTasks = "<a href=\"javascript:showModalDialog('../enterprisechangemgt/ECMUtil.jsp?objectId=" + XSSUtil.encodeForHTMLAttribute(context, strCOId) + "&amp;functionality=MassTaskApproval', '400', '400')\"><img border='0' src='../common/images/iconActionApprove.gif' name='person' id='person' alt='+approvalTasks+' title='+approvalTasks+'/>"+EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Command.AwaitingApproval")+"</a>";
						sb.append(strApproveTasks);
						vecReturn.addElement(sb.toString());
					}
					else{
						vecReturn.addElement("-");
					}
				}
				
				if(!changeOrderUI.isKindOf(context, ChangeConstants.TYPE_CHANGE_ORDER) && !changeOrderUI.isKindOf(context, ChangeConstants.TYPE_CHANGE_REQUEST) && !changeOrderUI.isKindOf(context, ChangeConstants.TYPE_CHANGE_ACTION))
				{
					vecReturn.addElement("-");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e);
		}

		return vecReturn;

	}


	/**
	 * Show Mass Approval Link in SB Table
	 * @param context
	 * @param objectId
	 * @return
	 * @throws Exception
	 */
	public Vector showCAQuickMassApproval(Context context, String[] args) throws Exception
	{
		//XSSOK
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get(ChangeConstants.OBJECT_LIST);
		String languageStr = context.getSession().getLanguage();
		Vector vecReturn   = null;
		StringBuffer sb    = null;
		try
		{
			StringList changeOrderIDList = changeUtil.getStringListFromMapList(objectList, ChangeConstants.ID);

			String transferOwnership = EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Message.TransferOwnershipOfCA");

			String approvalTasks     = EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Message.ApprovalTasksOfCA");

			String approvalRequired = EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Label.ApprovalStatus");

			vecReturn = new Vector(changeOrderIDList.size());
			String strCOId				= "";
			String strTransferOwnership	= "";
			String strApproveTasks		= "";

			ChangeOrderUI changeOrderUI 	= null;
			MapList mlTableData 			= new MapList();

			Iterator changeOrderItr = changeOrderIDList.iterator();
			while (changeOrderItr.hasNext()) {
				strCOId = changeOrderItr.next().toString();
				changeOrderUI 	= new ChangeOrderUI(strCOId);
				String type = changeOrderUI.getInfo(context, SELECT_TYPE);

				if(changeOrderUI.isKindOf(context, ChangeConstants.TYPE_CHANGE_ACTION)){
					mlTableData = changeOrderUI.getCurrentAssignedTasksOnObject(context);

					if(!mlTableData.isEmpty()){
						sb = new StringBuffer(500);

						//Multitenant
						//strApproveTasks = "<a href=\"javascript:showModalDialog('../common/emxTableEdit.jsp?program=enoECMChangeOrder:getTasks&amp;table=AEFMyTaskMassApprovalSummary&amp;selection=multiple&amp;header=emxComponents.Common.TaskMassApproval&amp;postProcessURL=../common/emxLifecycleTasksMassApprovalProcess.jsp&amp;HelpMarker=emxhelpmytaskmassapprove&amp;suiteKey=Components&amp;StringResourceFileId=emxComponentsStringResource&amp;SuiteDirectory=component&amp;objectId=" + strCOId + "', '800', '575')\"><img border='0' src='../common/images/iconActionApprove.gif' name='person' id='person' alt='"+approvalTasks+"' title='"+approvalTasks+"'/>"+i18nNow.getI18nString("EnterpriseChangeMgt.Command.AwaitingApproval", ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getSession().getLanguage())+"</a>";
						strApproveTasks = "<a href=\"javascript:showModalDialog('../enterprisechangemgt/ECMUtil.jsp?objectId=" + XSSUtil.encodeForHTMLAttribute(context, strCOId) + "&amp;functionality=MassTaskApproval', '400', '400')\"><img border='0' src='../common/images/iconActionApprove.gif' name='person' id='person' alt='+approvalTasks+' title='+approvalTasks+'/>"+EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Command.AwaitingApproval")+"</a>";
						sb.append(strApproveTasks);
						vecReturn.addElement(sb.toString());
					}
					else{
						vecReturn.addElement("-");
					}
				}


				if(!changeOrderUI.isKindOf(context, ChangeConstants.TYPE_CHANGE_ACTION))
				{
					vecReturn.addElement("-");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e);
		}

		return vecReturn;

	}


	/**
	 * Displays Cloned CO Name on Copy Selected page
	 * @author
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String getClonedCOName(Context context,String []args) throws Exception {

		HashMap paramMap   = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) paramMap.get(ChangeConstants.REQUEST_MAP);
		String objectId    = (String)requestMap.get("copyObjectId");
		DomainObject dmObj = DomainObject.newInstance(context);
		if(!UIUtil.isNullOrEmpty(objectId)){
			dmObj.setId(objectId);
			return dmObj.getName(context);}
		else
                    return "";
	}

	/**
	 * Displays Cloned CO Name on Copy Selected page
	 * @author
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean showFieldInClone(Context context,String []args) throws Exception {

		boolean sReturn = false;
		HashMap paramMap   = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) paramMap.get(ChangeConstants.REQUEST_MAP);
		String sCeateMode    = (String)paramMap.get("CreateMode");
		if("CloneCO".equals(sCeateMode))
		{
			sReturn = true;
		}
		return sReturn;
	}

	/**
	 * Displays Cloned CO Name on Copy Selected page
	 * @author
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean showFieldInCreate(Context context,String []args) throws Exception {

		boolean sReturn = false;
		HashMap paramMap   = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) paramMap.get(ChangeConstants.REQUEST_MAP);
		String sCeateMode    = (String)paramMap.get("CreateMode");
		if("CreateCO".equals(sCeateMode)){
			sReturn = true;
		}

		return sReturn;
	}

	/**
	 * @author R3D
	 * this method performs the Mass hold process of change.Moves all associated CAs to hold state.
	 *
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object.
	 * @param args
	 *            holds the following input arguments: - The ObjectID of the
	 *            Change Process
	 * @throws Exception
	 *             if the operation fails.
	 * @since ECM R211.
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void massHoldCO(Context context, String[] args)throws Exception {

		HashMap programMap   = (HashMap) JPO.unpackArgs(args);
		HashMap requestValuesMap = (HashMap) programMap.get("requestValuesMap");
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String strObjectIds    = (String)paramMap.get("objectsToHold");
		String sReason     = (String)paramMap.get("Reason");

		String objectId = "";
		StringTokenizer strIds = new StringTokenizer(strObjectIds,",");
		while(strIds.hasMoreTokens())
		{
			objectId = (String)strIds.nextToken();
			ChangeOrder changeOrder = new ChangeOrder(objectId);
			changeOrder.hold(context,sReason);
			//hold(context,objectId,sReason);
		}

	}

	/**
	 * @author R3D
	 * this method performs the Mass Cancel process of change.Moves all associated CAs to hold state.
	 * @param context
	 *            the eMatrix <code>Context</code> object.
	 * @param args
	 *            holds the following input arguments: - The ObjectID of the
	 *            Change Process
	 * @throws Exception
	 *             if the operation fails.
	 * @since ECM R211.
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void massHoldCancelCO(Context context, String[] args)throws Exception {

		HashMap programMap   = (HashMap) JPO.unpackArgs(args);
		HashMap requestValuesMap = (HashMap) programMap.get("requestValuesMap");
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String strObjectIds    = (String)paramMap.get("objectsToCancel");
		String sReason     = (String)paramMap.get("Reason");

		String objectId = "";
		StringTokenizer strIds = new StringTokenizer(strObjectIds,",");
		while(strIds.hasMoreTokens())
		{
			objectId = (String)strIds.nextToken();
			ChangeOrder changeOrder = new ChangeOrder(objectId);
			changeOrder.cancel(context,sReason);
			//cancel(context,objectId,sReason);
		}

	}



	/**
	 * @author R3D
	 * this method performs the Mass Transfer Ownership
	 * @param context
	 *            the eMatrix <code>Context</code> object.
	 * @param args
	 *            holds the following input arguments: - The ObjectID of the
	 *            Change Process
	 * @throws Exception
	 *             if the operation fails.
	 * @since ECM R211.
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void massTransferOwnershipToChangeCoordinator(Context context, String[] args)throws Exception {

		HashMap programMap   = (HashMap) JPO.unpackArgs(args);
		HashMap requestValuesMap = (HashMap) programMap.get("requestValuesMap");
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String strObjectIds    = (String)paramMap.get("objectId");
		String sReason     = (String)paramMap.get("TransferReason");


		String newOwner 		 = (String)paramMap.get(ChangeConstants.NEW_OWNER);
		String []params 	     = {sReason,newOwner};

		String objectId = "";
		StringTokenizer strIds = new StringTokenizer(strObjectIds,",");
		while(strIds.hasMoreTokens()){
			objectId = (String)strIds.nextToken();
			changeOrder.setId(objectId);
			changeOrder.transferOwnership(context, sReason,newOwner);
		}
	}

	/**
	 * @author R3D
	 * this method performs the Mass Transfer Ownership
	 * @param context
	 *            the eMatrix <code>Context</code> object.
	 * @param args
	 *            holds the following input arguments: - The ObjectID of the
	 *            Change Process
	 * @throws Exception
	 *             if the operation fails.
	 * @since ECM R211.
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void massTransferOwnershipToTechnicalAssignee(Context context, String[] args)throws Exception {

		HashMap programMap   = (HashMap) JPO.unpackArgs(args);
		HashMap requestValuesMap = (HashMap) programMap.get("requestValuesMap");
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String strObjectIds    = (String)paramMap.get("objectId");
		String sReason     = (String)paramMap.get("TransferReason");

		String newOwner 		 = (String)paramMap.get(ChangeConstants.NEW_OWNER);
		String []params 	     = {sReason,newOwner};

		String objectId = "";
		StringTokenizer strIds = new StringTokenizer(strObjectIds,",");
		while(strIds.hasMoreTokens()){
			objectId = (String)strIds.nextToken();
			changeOrder.setId(objectId);
			changeOrder.transferOwnership(context, sReason,newOwner);
		}
	}


	/**
	 * Range Program to display Legacy Change Filter Options
	 * @param context   the eMatrix <code>Context</code> object
	 * @param           String[] of ObjectIds.
	 * @return          Object containing CO objects
	 * @throws          Exception if the operation fails
	 * @since           ECM R212
	 */
	public HashMap getLegacyChangeFilters(Context context, String args[])throws Exception {
		HashMap ViewMap 					= new HashMap();
		HashMap programMap 					= (HashMap) JPO.unpackArgs(args);
		HashMap requestMap 					= (HashMap) programMap.get("requestMap");
		HashMap menuMap 					= UICache.getMenu(context, "ECMChangeLegacyMenu");
		MapList commandMap 					= (MapList)menuMap.get("children");

		String cmdName 						= "";
		String cmdLabel 					= "";
		String sDisplayValue 				= "";
		String sRegisteredSuite 			= "";
		String strStringResourceFile 		= "";
		StringList fieldRangeValues 		= new StringList();
		StringList fieldDisplayRangeValues 	= new StringList();

		if(commandMap!=null){
			Iterator cmdItr = commandMap.iterator();
			while(cmdItr.hasNext())
			{
				Map tempMap 				= (Map)cmdItr.next();
				cmdName 					= (String)tempMap.get("name");
				HashMap cmdMap 				= UICache.getCommand(context, cmdName);
				HashMap settingMap 			= (HashMap)cmdMap.get("settings");
				cmdLabel 					= (String)cmdMap.get("label");
				sRegisteredSuite 			= (String)settingMap.get("Registered Suite");

				StringBuffer strBuf = new StringBuffer("emx");
				strBuf.append(sRegisteredSuite);
				strBuf.append("StringResource");

				//strStringResourceFile 		= UINavigatorUtil.getStringResourceFileId(sRegisteredSuite);
				//sDisplayValue 				= i18nNow.getI18nString (cmdLabel, strStringResourceFile, context.getSession().getLanguage());

				sDisplayValue = EnoviaResourceBundle.getProperty(context, strBuf.toString(), context.getLocale(),cmdLabel);

				fieldRangeValues.addElement(sDisplayValue);
				fieldDisplayRangeValues.addElement(sDisplayValue);
			}
			ViewMap.put("field_choices", fieldRangeValues);
			ViewMap.put("field_display_choices", fieldDisplayRangeValues);
		}
		if(commandMap!=null)
			return ViewMap;
		else
			return new HashMap();
	}

	/**
	 * Method to get Legacy Changes
	 * @param context   the eMatrix <code>Context</code> object
	 * @param           String[] of ObjectIds.
	 * @return          Object containing CO objects
	 * @throws          Exception if the operation fails
	 * @since           ECM R212
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getLegacyChangeForSearchType(Context context, String args[]) throws Exception
	{

		MapList sTableData = new MapList();
		HashMap programMap              = (HashMap)JPO.unpackArgs(args);

		String searchType = (String)programMap.get("searchType");
		String strObjectId = (String)programMap.get("objectId");

		StringBuffer sb = new StringBuffer();
		StringList sTypeList = FrameworkUtil.split(searchType, ",");
		String type ="";
		for(int i= 0; i<sTypeList.size(); i++){
			String single = (String)sTypeList.get(i);
			type = PropertyUtil.getSchemaProperty(context,single);
			sb.append(type);
			if(i!=sTypeList.size()-1)
				sb.append(",");
		}


		StringList strList = new StringList();
		strList.add(SELECT_NAME);
		strList.add(SELECT_TYPE);
		strList.add(SELECT_ID);

		try{
		//String wherClause = SELECT_CURRENT + "== 'Active' ";
			if(UIUtil.isNullOrEmpty(strObjectId))
		sTableData = DomainObject.findObjects(context, sb.toString(), "*", null, strList);
			else{
				DomainObject partObject=new DomainObject(strObjectId);
			   sTableData= partObject.getRelatedObjects(context, 
					  											QUERY_WILDCARD, 
					  											sb.toString(), 
					  											strList, 
															  	null, 
															  	true, 
															  	false, 
															  	(short)0, 
															  	null, 
													  			null,
													  			0);
			}
		}

		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}

		return sTableData;

	}//end of method

	/**
	 * Method to check if the Affected Item is already connected to any Change Object which is not Completed.
	 * If it is connected, block the addition and throw a Notice to the user.
	 * @param context   the eMatrix <code>Context</code> object
	 * @param           String[] args.
	 * @return          true/false
	 * @throws          Exception if the operation fails
	 * @since           ECM R211
	 */
	public int checkIfAffectedItemAlreadyConnectedToChangeObject(Context context, String args[]) throws Exception {

		if(args==null || args.length<10)
		{
			throw new IllegalArgumentException();
		}

		int isError = 0;

		try {

			String strRelType  			= args[0];
			String strToObjectId     	= args[1];
			String strNewRCValue     	= args[2];
			DomainObject domObj			= DomainObject.newInstance(context, strToObjectId);
			Map tempMap 				= null;
			Map mapTemp 				= new HashMap();
			MapList mlAffetcedItemList	= new MapList();
			String strRCAttrValue		= "";
			String strObjState			= "";
			String strChangeObjState 	= "";
			String strType 				= "";
			String strPolicy 			= "";
			String strReleasedState		= "";
			String strCompleteState 	= "";
			String strCancelledState	= "";

			StringList slObjSelect	= new StringList();
			slObjSelect.add(SELECT_CURRENT);
			slObjSelect.add(SELECT_TYPE);
			slObjSelect.add(SELECT_POLICY);

			mapTemp 	= domObj.getInfo(context,slObjSelect);
			strObjState = (String) mapTemp.get(SELECT_CURRENT);
			strType 	= (String) mapTemp.get(SELECT_TYPE);
			strPolicy 	= (String) mapTemp.get(SELECT_POLICY);

			strReleasedState = ECMAdmin.getReleaseStateValue(context, strType, strPolicy);


			if (ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM.equalsIgnoreCase(strRelType)) {

				if (strNewRCValue.equalsIgnoreCase(ChangeConstants.FOR_NONE) || strNewRCValue.equalsIgnoreCase(ChangeConstants.FOR_UPDATE)) {

					return isError;

				} else if (strNewRCValue.equalsIgnoreCase(ChangeConstants.FOR_RELEASE) || strNewRCValue.equalsIgnoreCase(ChangeConstants.FOR_OBSOLESCENCE)) {


					StringList slRelSelect	= new StringList();
					slRelSelect.add(ChangeConstants.SELECT_ATTRIBUTE_REQUESTED_CHANGE);

					String strCAPolicyName = ChangeConstants.POLICY_CHANGE_ACTION;
					strCompleteState = PropertyUtil.getSchemaProperty(context, ChangeConstants.POLICY, strCAPolicyName, ChangeConstants.STATE_SYMBOLIC_COMPLETE);
					strCancelledState = PropertyUtil.getSchemaProperty(context, ChangeConstants.POLICY, strCAPolicyName, ChangeConstants.STATE_SYMBOLIC_CANCELLED);

					mlAffetcedItemList = domObj.getRelatedObjects(context,
								ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM+","+ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM,
								ChangeConstants.TYPE_CHANGE_ACTION,
								slObjSelect,
								slRelSelect,
								true,
								false,
							   (short) 1,
								ChangeConstants.EMPTY_STRING,
								ChangeConstants.EMPTY_STRING,
								0);

					Iterator itr = mlAffetcedItemList.iterator();

					while (itr.hasNext()) {

						tempMap 			= (Map) itr.next();
						strRCAttrValue 		= (String) tempMap.get(ChangeConstants.SELECT_ATTRIBUTE_REQUESTED_CHANGE);
						strChangeObjState 	= (String) tempMap.get(SELECT_CURRENT);

						if(!strChangeObjState.equalsIgnoreCase(strCompleteState) && !strChangeObjState.equalsIgnoreCase(strCancelledState)) {

							if (strNewRCValue.equalsIgnoreCase(ChangeConstants.FOR_RELEASE) && strRCAttrValue.equalsIgnoreCase(ChangeConstants.FOR_RELEASE)) {

								emxContextUtil_mxJPO.mqlNotice(context,EnoviaResourceBundle.getProperty(context, args[3], context.getLocale(),args[4]));
								isError = 1;
								return isError;

							} else if (strNewRCValue.equalsIgnoreCase(ChangeConstants.FOR_OBSOLESCENCE) && strRCAttrValue.equalsIgnoreCase(ChangeConstants.FOR_OBSOLESCENCE)) {

								emxContextUtil_mxJPO.mqlNotice(context,EnoviaResourceBundle.getProperty(context, args[3], context.getLocale(),args[5]));

								isError = 1;
								return isError;

							}

						}

					}

				}

				 StringList strListChildStates = domObj.getInfoList(context,SELECT_STATES);
				 int indexTargetState = strListChildStates.indexOf(strReleasedState);
				 int indexObjectState = strListChildStates.indexOf(strObjState);

				if(indexTargetState != -1 && indexObjectState != -1) {

					if (strNewRCValue.equalsIgnoreCase(ChangeConstants.FOR_RELEASE) && indexObjectState >= indexTargetState) {

						emxContextUtil_mxJPO.mqlNotice(context,EnoviaResourceBundle.getProperty(context, args[3], context.getLocale(),args[6]));
						isError = 1;
						return isError;

					}

					if ((strNewRCValue.equalsIgnoreCase(ChangeConstants.FOR_OBSOLESCENCE) || strNewRCValue.equalsIgnoreCase(ChangeConstants.FOR_REVISE) || strNewRCValue.equalsIgnoreCase(ChangeConstants.FOR_MAJOR_REVISE)) && indexObjectState < indexTargetState) {

						emxContextUtil_mxJPO.mqlNotice(context,EnoviaResourceBundle.getProperty(context, args[3], context.getLocale(),args[7]));
						isError = 1;
						return isError;

					}
				}
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return isError;

	}

	/**
		 * Display Evaluation Reviewer List person name in CA Display Table
		 * @author R3D
		 * @param context
		 * @param args
		 * @return
		 * @throws Exception
		 */
		public Vector getCAReviewerListMembers(Context context,String []args) throws Exception {

			//XSSOK
			Vector sReviewMemberList = new Vector();
			HashMap programMap 		  = (HashMap) JPO.unpackArgs(args);
			MapList objectList 		  = (MapList) programMap.get("objectList");
			HashMap paramMap          = (HashMap) programMap.get("paramList");
			
			// For export to CSV
			String exportFormat = null;
			boolean exportToExcel = false;
			if(paramMap!=null && paramMap.containsKey("reportFormat")){
				exportFormat = (String)paramMap.get("reportFormat");
			}
			if("CSV".equals(exportFormat)){
				exportToExcel = true;
			}
			
			String objectId ="";

			sReviewMemberList = new Vector(objectList.size());
			Iterator itr = objectList.iterator();
			while(itr.hasNext()){
				Map objectMap = (Map) itr.next();
				objectId = (String)objectMap.get("id");
				changeOrderUI.setId(objectId);
				String name = changeOrderUI.getMembersBasedOnPurpose(context,"Approval",exportToExcel);
				sReviewMemberList.add(name);
			}
			if(!sReviewMemberList.isEmpty())
			return sReviewMemberList;
			else
				return new Vector();
	}


	/**
	 * Method invoked from the Delete Trigger of rel "Change Affected Item".
	 * This method is Used to delete the CA Object, if the Affected Item is the Last one to be removed.
	 * @param context
	 * @param args CA Object and Affected Item Object
	 * @return integer
	 * @throws Exception
	 */
	public int deleteCAOnLastAffectedItem(Context context, String args[])throws Exception
	{
		try
		{
			String strCAObjectId = args[0];
			String strAffectedItemId = args[1];

			StringList objSelects = new StringList();
			String strRelselect = "from["+ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM+"].to.id";
	        objSelects.addElement(SELECT_CURRENT);
	        objSelects.addElement(strRelselect);
			DomainObject doObj = DomainObject.newInstance(context, strCAObjectId);
	        Map map = doObj.getInfo(context, objSelects);
	        String strRemainingAffItems  = (String)map.get(strRelselect);
			if(changeUtil.isNullOrEmpty(strRemainingAffItems))
			{
				String current = (String)map.get(SELECT_CURRENT);
				String strCAPolicyName = ChangeConstants.POLICY_CHANGE_ACTION;
		        String strCancelledState = PropertyUtil.getSchemaProperty(context, ChangeConstants.POLICY, strCAPolicyName, ChangeConstants.STATE_SYMBOLIC_CANCELLED);
				if(!strCancelledState.equalsIgnoreCase(current))
					doObj.deleteObject(context);
			}
		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw Ex;
		}
		return 0;
	}

	/**
	 * Method invoked from the Owner Notification Trigger.
	 * This method is Used to get transfer reason while transferring the owner of the context change
	 * @param context
	 * @param args Object ID
	 * @return String
	 * @throws Exception
	 */
	public static String getTransferComments(Context context, StringList args, Locale locale, String string1,String string2, String string3)
	throws Exception
	{
		try
		{
			String objectId = (String)args.firstElement();
			String person = context.getUser();
			String transferReason      = "";
			String historyData     = ChangeUtil.getHistory(context,objectId,"history.custom");
			StringList historyList = FrameworkUtil.splitString(historyData, ChangeConstants.COMMA_SEPERATOR);
			if(UIUtil.isNotNullAndNotEmpty(historyData))
			{
				transferReason = historyData.substring(historyData.lastIndexOf("Transfer Comments:"), historyData.length()-1);
			}
			return transferReason;

		}
			catch (Exception ex) {
				ex.printStackTrace();
				throw new FrameworkException(ex.getMessage());
			}
		}

	/**
	 * This method is Used to refresh the whole affected item page after editing any values in the column.
	 * @param context
	 * @param args
	 * @return HashMap
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public HashMap refreshAffectedItems (Context context, String args[]) throws Exception
    {
		HashMap doc     = new HashMap();
		doc.put("Action", "refresh");
		return doc;
    }
	/**
	 * This method is Used to get the locale specific values for given list of properties.
	 * @param context
	 * @param Props  Array of Properties.
	 * @param stringResourceFileID String Resource File ID
	 * @param languageStr Language String
	 * @return List of Values for given Properties
	 * @throws Exception
	 */
	public StringList getValuesforProperties(Context context, String[] Props, String stringResourceFileID, String languageStr){
		StringList slPropValue = new StringList(Props.length);
		for(int i=0;i<Props.length;i++){
			slPropValue.add(EnoviaResourceBundle.getProperty(context, stringResourceFileID, new Locale(languageStr), Props[i]));
		}
		return slPropValue;
	}
	/**
	 * This method is used to get Range Values for Fast track Process options.
	 * @param context
	 * @param args.
	 * @return Map of Display and Actual values for the options
	 * @throws Exception
	 */
	public HashMap FastTrackProcessOptionsRangeValues(Context context,String[] args){
		// This is intentional change to take off immediate options
		String [] rangeOptionsStrings = {"EnterpriseChangeMgt.Range.FastTrackProcess.Deferred"};
		HashMap rangeMap              = new HashMap(2);
		rangeMap.put("field_choices", getValuesforProperties(context,rangeOptionsStrings, "emxEnterpriseChangeMgtStringResource", "en"));
		rangeMap.put("field_display_choices", getValuesforProperties(context,rangeOptionsStrings, "emxEnterpriseChangeMgtStringResource", context.getSession().getLanguage()));
		return rangeMap;
	}
	/**
	 * This method is used for restricting the access of field limited to Mass Release or Mass Obsolete operations.
	 * @param context
	 * @param args to get Functionality (Mass Release or Mass Obsolete).
	 * @return True or False
	 * @throws Exception
	 */
	public boolean ShowFieldInMassReleaseOrObsolete(Context context,String []args) throws Exception {
		boolean sReturn          = false;
		HashMap paramMap         = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap       = (HashMap) paramMap.get(ChangeConstants.REQUEST_MAP);
		String sFunctionality    = (String)paramMap.get("functionality");
		if((ChangeConstants.FOR_OBSOLETE.equals(sFunctionality))||(ChangeConstants.FOR_RELEASE.equals(sFunctionality)))
		{
			sReturn = true;
		}
		return sReturn;
	}

	/**
	 * Trigger Method to set context user as Change Coordinator if CO policy is Fast track Change.
	 * @author M24
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args - Object Id and Policy of Change Order
	 * @return int - Returns integer status code
            0 - in case the trigger is successful
	 * @throws Exception if the operation fails
	 * @since ECM R211
	 */
	public int setCOChangeCoordinator(Context context,String []args) throws Exception {
		String strObjId                  = args[0];
		String strPolicy                 = args[1];
		String strRelChangeCoordinator   = PropertyUtil.getSchemaProperty(context,args[2]);
		if(ChangeConstants.POLICY_FASTTRACK_CHANGE.equalsIgnoreCase(strPolicy)){
			try{
				ChangeOrder co           = new ChangeOrder(strObjId);
				DomainObject loginUser   = new DomainObject(PersonUtil.getPersonObjectID(context));
				co.connect(context, strRelChangeCoordinator, loginUser, false);
			}catch(Exception e){
				e.printStackTrace();
				emxContextUtil_mxJPO.mqlNotice(context,EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(),"EnterpriseChangeMgt.Notice.NotConnectedAsChangeCoordinator"));
				return 1;
			}
		}
		return 0;
	}
	
	/**
	 * Post Process of CO create. If nonempty effectivity is set on CO, 
	 * then auto generate a PUE ECO and add same effectivity to it.
	 *  
	 * @author D2E
	 * @param context - the eMatrix <code>Context</code> object
	 * @param <code>String[]</code> args - requestMap  
	 * @return void  
	 * @throws Exception if the operation fails
	 * @since ECM R211
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public HashMap coPostProcessJPO(Context context, String args[]) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);		
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		
		HashMap resultMap = new HashMap();
		
		//CUSTOMCHANGE is no longer supported
		/*
		if (ChangeUtil.isCFFInstalled(context)) {
			String createJPO = ECMAdmin.getCustomChangeCreateJPO(context, "XCE");
			
			if (UIUtil.isNotNullAndNotEmpty(createJPO)) {
				String programName = createJPO.replaceAll(":.*$", "").trim();
				String methodName = createJPO.replaceAll("^.*:", "").trim();
				
				resultMap = JPO.invoke(context, programName, null, methodName, args, HashMap.class);
			}
		}
		*/
		
		return resultMap;
	}
	
	public MapList getEffectivity(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		
		String mode                = (String) requestMap.get("mode");		
		String changeId            = (String) requestMap.get(ChangeConstants.OBJECT_ID);
		String effectivityRequired = (String) requestMap.get("effectivityRequired");
		
		/* If user selects the type through type chooser in CO create form then type key of requestMap contains value _selectedType:Change Order,type_ChangeOrder 
		 * which is causing issue in Effectivity field of CO to fix the issue requestMap is modified */
		String type = (String) requestMap.get("type");
		if (UIUtil.isNotNullAndNotEmpty(type) && type.startsWith("_selectedType")) {
			requestMap.put("type", type.replaceAll("^.*,", ""));
			requestMap.put("selectedType", type);
		}
		
		Map formFieldSetting = new HashMap();
		
		if (!changeUtil.isCFFInstalled(context)) {
			formFieldSetting.put("Access Expression", "false");
		} else {
			String methodName = "create".equals(mode) ? "getEffectivityOnChangeCreate" : "getEffectivityOnChangeDisplay";
			//String effectivityMand = "true".equals(effectivityRequired) ? "true" : "false";
			
			if (!"true".equals(effectivityRequired)) {				
				formFieldSetting.put("Access Function", "showMand");
				formFieldSetting.put("Access Program", "emxEffectivityFramework");
			}
			
			formFieldSetting.put("Field Type", "programHTMLOutput");
			formFieldSetting.put("Registered Suite", "Effectivity");
			//formFieldSetting.put("Required", effectivityMand);
			formFieldSetting.put("TypeAhead", "false");
			formFieldSetting.put("Update Function", "updateEffectivityOnChange");
			formFieldSetting.put("Update Program", "emxEffectivityFramework");
			formFieldSetting.put("program", "emxEffectivityFramework");
			formFieldSetting.put("function", methodName);
			
			if (!"create".equals(mode) && UIUtil.isNotNullAndNotEmpty(changeId)) {
				String SELECT_NAMED_EFFECTIVITY_EXISTS = "from[" + PropertyUtil.getSchemaProperty(context, "relationship_NamedEffectivity") + "]";
				StringList objectSelect = new StringList(2);
				objectSelect.add(DomainConstants.SELECT_TYPE);
				objectSelect.add(SELECT_NAMED_EFFECTIVITY_EXISTS);
				
				Map infoMap = DomainObject.newInstance(context, changeId).getInfo(context, objectSelect);
				
				String namedEffectivity = (String) infoMap.get(SELECT_NAMED_EFFECTIVITY_EXISTS);
				String changeType = (String) infoMap.get(DomainConstants.SELECT_TYPE);
				
				if ("false".equalsIgnoreCase(namedEffectivity)) {
					formFieldSetting = new HashMap();
					formFieldSetting.put("Access Expression", "false");
				} else if (changeType.equals(ChangeConstants.TYPE_CCA)) {
					formFieldSetting = new HashMap();
					formFieldSetting.put("Editable", "false");
					formFieldSetting.put("Field Type", "program");
					formFieldSetting.put("Registered Suite", "Effectivity");			
					formFieldSetting.put("program", "enoECMChangeOrder");
					formFieldSetting.put("function", "getEffectivityOnChange");
				} else if (!changeType.equals(ChangeConstants.TYPE_CHANGE_ORDER)) {
					formFieldSetting.put("Required", "true");
				}
			}
		}
		
		Map formFieldMap = new HashMap();
		
		formFieldMap.put("label", "Effectivity.Form.Label.Effectivity");
		formFieldMap.put("name", "ChangeEffectivity");
		formFieldMap.put("settings", formFieldSetting);
		
		MapList formFieldList = new MapList(1);
		formFieldList.add(formFieldMap);
		
		return formFieldList;
	}
	
	public String getEffectivityOnChange(Context context, String args[]) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		
		return (String) ((Map) EffectivityUtil.getEffectivityOnChange(context, (String) requestMap.get(ChangeConstants.OBJECT_ID))).get("displayValue");
	}
	
	/**
	 * Method to get dynamic search string for Change Template search in Mass Change Functionality
	 *  (Mass Release or Mass Obsolete)
	 * @author M24
	 * @param context - the eMatrix <code>Context</code> object
	 * @param <code>String[]</code> args - requestMap  
	 * @return String StringPattern
	 * @throws Exception if the operation fails
	 * @since ECM R216
	 */
	public String getCTDynamicSearchQuery(Context context, String [] args) throws Exception {
		StringBuffer returnString  = new StringBuffer(); 
		returnString.append("TYPES=type_ChangeTemplate:CURRENT=policy_ChangeTemplate.state_Active");
		HashMap programMap   = (HashMap)JPO.unpackArgs(args); 
		Map requestMap       = (HashMap)programMap.get(ChangeConstants.REQUEST_MAP);
		String functionality = (String)requestMap.get("functionality");
		
		if((UIUtil.isNotNullAndNotEmpty(functionality)&&(ChangeConstants.FOR_RELEASE.equals(functionality)||ChangeConstants.FOR_OBSOLETE.equals(functionality))))
			returnString.append(":DEFAULT_POLICY=").append(ChangeConstants.FASTTRACK_CHANGE);
		return returnString.toString();
	}
	
	/**
     * Program to get ApprovalList For CA from Co/CR Context
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  Object
     * @return        a <code>MapList</code> object having the list of ApprovalList names for this Change Object
     * @throws        Exception if the operation fails
     * @since         ECM R216
     **
   */
	public  Vector getApprovalListForChangeAction(Context context, String[] args) throws Exception {
		// Create result vector
        Vector vecResult = new Vector();
        Map mapObjectInfo = null;
        String sApprovalListName = "";

        // Get object list information from packed arguments
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList)programMap.get("objectList");
        for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
            mapObjectInfo = (Map) itrObjects.next();
            sApprovalListName = (String)mapObjectInfo.get("from[Object Route].to.name");
            vecResult.add(sApprovalListName);
        }
        return vecResult;
	}
	/**
	 * Method to get dynamic search string for Change TEmplate search in Mass Change Functionality
	 *  
	 * @author M24
	 * @param context - the eMatrix <code>Context</code> object
	 * @param <code>String[]</code> args - MapList argsList  
	 * @throws Exception if the operation fails
	 * @since ECM R216
	 */
	public void moveAffectdItemsToNewCA(Context context, String [] args) throws Exception {
		MapList argsList = (MapList)JPO.unpackArgs(args);
		ChangeOrder changeOrder = new ChangeOrder(argsList.get(0).toString());
		changeOrder.moveToCA(context, argsList);
	}
	/**
	 * Action Trigger on ( Prepare/In Review --> In work) to check whether the context Change Order related  Change Action to be completed or not.
	 * If so then Promote the Change Order to "In Approval" state and notify CO Owner.
	 * @param context
	 * @param args (Change Order Id and Notice)
	 * @throws Exception
	 */	
	public void checkForCAsState(Context context, String [] args) throws Exception{

		try{
			String strCAId;
			String strCAState;
			String strCAPolicy;
			StringList listChangeActionAllStates;
			boolean pendingChangeExists=false;
			String strCCAId=null;
			Map tempMap = null;
			String strChangeOrderPolicy = null;
			StringList strRouteList = new StringList();
			String strRoutetemplate = null;
			String strChangeOrderId=args[0];
			String relPattern= ChangeConstants.RELATIONSHIP_CHANGE_ACTION;
			relPattern=relPattern.concat(",");
			relPattern=relPattern.concat(ChangeConstants.RELATIONSHIP_OBJECT_ROUTE);
			String typePattern=ChangeConstants.TYPE_ROUTE_TEMPLATE;
			typePattern=typePattern.concat(",");
			typePattern=typePattern.concat(ChangeConstants.TYPE_CHANGE_ACTION);
			typePattern=typePattern.concat(",");
			typePattern=typePattern.concat(ChangeConstants.TYPE_CCA);
			StringList slObjectSelect = new StringList(4);
			slObjectSelect.add(SELECT_ID);
			slObjectSelect.add(SELECT_NAME);
			slObjectSelect.add(SELECT_CURRENT);
			slObjectSelect.add(SELECT_TYPE);
			slObjectSelect.add(SELECT_POLICY);
			slObjectSelect.add("attribute["+ATTRIBUTE_ROUTE_BASE_PURPOSE+"]");
			StringList slRelSelect = null;
			setId(strChangeOrderId);
			MapList resultList= getRelatedObjects(context,
					relPattern,
					typePattern,
					slObjectSelect,
					slRelSelect,
					false,
					true,
					(short) 1,
					"",
					EMPTY_STRING,
					0);
			if(resultList!=null || !resultList.isEmpty()){

				HashMap releaseStateMap = ChangeUtil.getReleasePolicyStates(context);
				Map mapTemp;
				for(Object var : resultList)
				{
					mapTemp = (Map) var;
					strCAId = (String) mapTemp.get(SELECT_ID);
					strCAState = (String) mapTemp.get(SELECT_CURRENT);					
					strCAPolicy = (String) mapTemp.get(SELECT_POLICY);
					if(strCAPolicy.equalsIgnoreCase(ChangeConstants.POLICY_CHANGE_ACTION)||strCAPolicy.equalsIgnoreCase(ChangeConstants.POLICY_PUEECO))
					{
						listChangeActionAllStates = ChangeUtil.getAllStates(context, strCAPolicy);
						if (new ChangeUtil().checkObjState(context, listChangeActionAllStates, strCAState, (String) releaseStateMap.get(strCAPolicy), ChangeConstants.LT) == 0) {
							if (ChangeConstants.TYPE_CCA.equals((String) mapTemp.get(SELECT_TYPE))) {
								String affectedItemExits = DomainObject.newInstance(context, strCAId).getInfo(context, "from[" + DomainConstants.RELATIONSHIP_AFFECTED_ITEM + "]");
								if ("True".equalsIgnoreCase(affectedItemExits)) {
									pendingChangeExists = true;
									break;
								} else {
									strCCAId = strCAId;
								}
							} else {
								pendingChangeExists = true;
								break;
							}
						}
					}
					else{
						String routeBasePurpose=(String)mapTemp.get("attribute["+ATTRIBUTE_ROUTE_BASE_PURPOSE+"]");
						if(routeBasePurpose.equalsIgnoreCase("Approval"))
						{
							strRoutetemplate=(String)mapTemp.get(SELECT_NAME);
						}

					}


				}
			}
			if (!pendingChangeExists) {
				setId(strChangeOrderId);
				strChangeOrderPolicy= getInfo(context,SELECT_POLICY);
				if(UIUtil.isNotNullAndNotEmpty(strRoutetemplate)){
					setState(context, PropertyUtil.getSchemaProperty(context, "policy", strChangeOrderPolicy, "state_InApproval"));
				}else{
					setState(context, PropertyUtil.getSchemaProperty(context, "policy", strChangeOrderPolicy, "state_Complete"));
				}
				emxNotificationUtilBase_mxJPO.sendNotification(context, 
						strChangeOrderId, 
						new StringList(getOwner(context).getName()), 
						new StringList(), 
						new StringList(), 
						args[1], 
						args[2], 
						new StringList(), 
						args[3], 
						null, null, null);
				if (strCCAId != null) {
					DomainObject.deleteObjects(context, new String[] {strCCAId});
				}
			}
		}catch(Exception Ex) {
			Ex.printStackTrace();
			throw Ex;
		}
	}
	/**
	 * Program to get Column(Drop Zone) value For CO Summary table
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 *           0 -  Object
	 * @return        Vector of column value
	 * @throws        Exception if the operation fails
	 **
	 */
	public Vector showDropZoneColumn(Context context, String args[])throws Exception
	{
		//XSSOK
		Vector columnVals = new Vector();
		String drop= EnoviaResourceBundle.getProperty(context,SUITE_KEY, 
				"EnterpriseChangeMgt.Label.Drop", context.getSession().getLanguage()); 
		String changeAction= EnoviaResourceBundle.getProperty(context,SUITE_KEY, 
				"EnterpriseChangeMgt.Label.ChangeAction", context.getSession().getLanguage()); 
		StringBuilder sb = new StringBuilder();
		sb.append("<div style=\"border-radius:4px;font-size:9px;text-align:center;border:1px dashed #aaa;color:#aaa;\">");
		//XSSOK
		sb.append(drop);
		sb.append("<br/>");
		//XSSOK
		sb.append(changeAction);
		sb.append("</div>");
		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get(ChangeConstants.OBJECT_LIST);
			ChangeUtil changeUtil=new ChangeUtil();
			StringList strObjectIdList = changeUtil.getStringListFromMapList(objectList,DomainObject.SELECT_ID);

			if (strObjectIdList == null || strObjectIdList.size() == 0){
				return columnVals;
			} else{
				columnVals = new Vector(strObjectIdList.size());
			}
			for(int i=0;i<strObjectIdList.size();i++){
				String 	strChangeActionId = (String) strObjectIdList.get(i);
				columnVals.add(sb.toString());
			}
			return columnVals;

		} catch (Exception e) {
			throw new FrameworkException(e);
		}
	}//end of method

	/**
	 * Checks if the being connected Change Action to context Change Order \ Request belongs to the parent company \ subsidiary or not.
	 * If so, it will block that connection establishment as CA at parent company \ subsidiary can not be attached to CO from the child organization. 
	 *  
	 * @param context - ENOVIA <code>Context</code> object
	 * @param args
	 * @return int the value 0 is a succeed, else is a failure  
	 * @throws Exception
	 */	
	public int checkForCAInParentCompany(Context context, String [] args) throws Exception{
		int iRetCode = 0;		
		String sCOId = args[0];
		String sCAId = args[1];
		
		String[] saIds = new String[]{sCOId,sCAId};
		MapList mlCOCA = DomainObject.getInfo(context, saIds, new StringList(DomainObject.SELECT_ORGANIZATION));

		String sCOOrgName = (String)((Map)mlCOCA.get(0)).get(DomainObject.SELECT_ORGANIZATION);
		String sCAOrgName = (String)((Map)mlCOCA.get(1)).get(DomainObject.SELECT_ORGANIZATION);
		
		//perform the check if CO and CA are not from the same organization
		if(!sCAOrgName.equals(sCOOrgName)){
			//get CA's hierarchy and check
			String sValues = MqlUtil.mqlCommand(context, "print role $1 select $2 dump $3", sCAOrgName, "ancestor", "|");
			
			StringList slRoleHierarchy  =  FrameworkUtil.split(sValues, "|");
			if(!slRoleHierarchy.contains(sCOOrgName)){
				iRetCode = 1;
				String sError  =  EnoviaResourceBundle.getProperty(context ,
											"emxEnterpriseChangeMgtStringResource",
											context.getLocale(),
											"EnterpriseChangeMgt.Error.COCAConnection.NotSameOrg");
				throw new FrameworkException(sError);
			}			
		}
		
		return iRetCode;
	}	
	
}//end of class

