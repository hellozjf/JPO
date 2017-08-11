import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import matrix.db.Access;
import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.IntList;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.StringUtil;
import com.matrixone.apps.program.ProgramCentralUtil;

public class emxSecurityMigrationMigrateProgramObjectsBase_mxJPO extends
    emxProgramMigration_mxJPO {

	//private static final long serialVersionUID = -5029177381386073045L;
	static private  Map<String, Integer> _accessMasksConstMapping = new HashMap<String, Integer>(35);
    private static final String DEFAULT_COLLABORATIVE_SPACE =  "GLOBAL";//"Default";
	private static final String DEFAULT_ORGANIZATION = PropertyUtil.getSchemaProperty("role_CompanyName");//"Company Name";
	private static final String PROJECT_ACCESS_MEMBER = "Project Member";
	private static final String PROJECT_ACCESS_LEAD = "Project Lead";
	static {
		_accessMasksConstMapping.put("read", Access.cRead);
		_accessMasksConstMapping.put("modify", Access.cModify);
		_accessMasksConstMapping.put("delete", Access.cDelete);
		_accessMasksConstMapping.put("checkout", Access.cCheckout);
		_accessMasksConstMapping.put("checkin", Access.cCheckin);
		_accessMasksConstMapping.put("lock", Access.cLock);
		_accessMasksConstMapping.put("unlock", Access.cUnLock);
		_accessMasksConstMapping.put("grant", Access.cGrant);
		_accessMasksConstMapping.put("revoke", Access.cRevoke);
		_accessMasksConstMapping.put("changeowner", Access.cChangeOwner);
		_accessMasksConstMapping.put("create", Access.cCreate);
		_accessMasksConstMapping.put("promote", Access.cPromote);
		_accessMasksConstMapping.put("demote", Access.cDemote);
		_accessMasksConstMapping.put("enable", Access.cEnable);
		_accessMasksConstMapping.put("disable", Access.cDisable);
		_accessMasksConstMapping.put("override", Access.cOverride);
		_accessMasksConstMapping.put("schedule", Access.cSchedule);
		_accessMasksConstMapping.put("revise", Access.cRevise);
		_accessMasksConstMapping.put("changevault", Access.cChangeLattice);
		_accessMasksConstMapping.put("changename", Access.cChangeName);
		_accessMasksConstMapping.put("changepolicy", Access.cChangePolicy);
		_accessMasksConstMapping.put("changetype", Access.cChangeType);
		_accessMasksConstMapping.put("fromconnect", Access.cFromConnect);
		_accessMasksConstMapping.put("toconnect", Access.cToConnect);
		_accessMasksConstMapping.put("fromdisconnect", Access.cFromDisconnect);
		_accessMasksConstMapping.put("todisconnect", Access.cToDisconnect);
		_accessMasksConstMapping.put("freeze", Access.cFreeze);
		_accessMasksConstMapping.put("thaw", Access.cThaw);
		_accessMasksConstMapping.put("execute", Access.cExecute);
		_accessMasksConstMapping.put("modifyform", Access.cModifyForm);
		_accessMasksConstMapping.put("viewform", Access.cViewForm);
		_accessMasksConstMapping.put("show", Access.cShow);
		_accessMasksConstMapping.put("majorrevise", Access.cMajorRevise);
		_accessMasksConstMapping.put("all", Access.cAll);
		_accessMasksConstMapping.put("none", Access.cNone);
	}

	public emxSecurityMigrationMigrateProgramObjectsBase_mxJPO(Context context, String[] args) throws Exception {
		super(context, args);
	}
	public static StringList mxObjectSelects = new StringList(15);
	public static StringList USER_PROJECTS = new StringList();
	public static boolean unconvertable = false;
	public static String unConvertableComments = "";

	//PROJECT AND TASKS
	public static  String SELECT_OWNER 						   	= "";
	public static  String RELATIONSHIP_EMPLOYEE 				= "";
	public static String ATTRIBUTE_PROJECT_VISIBILITY 			= "";
	public static String TYPE_PROJECT_SPACE 					= "";
	public static String TYPE_TASK_MANAGEMENT 					= "";
	public static String TYPE_TASK                          	= "";
    public static String TYPE_CHANGE_TASK                       = "";
	public static String TYPE_PHASE                          	= "";             	           
	public static String TYPE_GATE  					     	= "";         	           
	public static String TYPE_MILESTONE                      	= "";            	       
	public static String TYPE_ASSESSMENT 					 	= "";                    
	public static String RELATIONSHIP_PROJECT_ASSESSMENT     	= "";    
	public static String RELATIONSHIP_SUBTASK                	= "";
	public static String RELATIONSHIP_ASSIGNED_TASK          	= "";
	public static String RELATIONSHIP_MEMBER 					= "";
	public static String RELATIONSHIP_PROJECT_ACCESS_LIST 		= "";
	public static String RELATIONSHIP_PROJECT_ACCESS_KEY 		= "";
	//BUDGET DATA
	public static String TYPE_BUDGET                          	= "";
	public static String TYPE_COST_ITEM                       	= "";
	public static String RELATIONSHIP_FINANCIAL_ITEMS         	= "";
	public static String RELATIONSHIP_PROJECT_FINANCIAL_ITEM  	= "";
	public static String RELATIONSHIP_ACTUAL_TRANSACTION_ITEM 	= "";
	public static String RELATIONSHIP_COST_ITEM_INTERVAL      	= "";
	public static String TYPE_BENEFIT							= "";
	// Risk Data
	public static String TYPE_RISK                           	= "";
	public static String POLICY_PROJECT_RISK                           			= "";
	public static String RELATIONSHIP_RISK_ITEM                 = "";
	public static String RELATIONSHIP_RISK         				= "";
	public static String RELATIONSHIP_RISK_RPN  				= "";
	public static String RELATIONSHIP_ASSIGNED_RISK  			= "";
	public static String selectRiskProjectIdFromPAL      						= "";
	public static String selectRiskAssignee      								= "";
	public static String selectRiskItems      									= "";
	public static String selectRiskHolder	      								= "";
	public static String selectRiskRPN	      									= "";

	public static String LOGICAL_ACCESS_PROJECT_LEAD     		= "";
	public static String RELATIONSHIP_REFERENCE_DOCUMENT 		= "";

	public static String selectProjectvisibilityFromPAL 						= ""; 
	public static String selectAttributeProjectVisibility 						= "";
	public static String projectAccessListIdFromProject 						= "";
	public static String projectSpaceGranteeFromProjectAccessList 				= "";
	public static String projectSpaceGrantGranteeAccessFromProjectAccessList 	= "";

	public static String prgProjectMemberIds 									= "";
	public static String prgSelectAttrbProjectVisibility 						= "";
	//assessment
	public static String assessmentProjectId 									= "";
	//Budget
	public static String budgetProjectId 										= "";
	public static String budgetGranteeFromProjectAccessList 					= "";
	public static String budgetGrantGranteeAccessFromProjectAccessList 			= "";
	public static String selectBudgetCostItemList 								= "";
	public static String selectActualTransactions 								= "";
	public static String selectCostItemInterval 								= "";
	//
	public static String isProjectTemplateParentOfTask 							= "";
	public static String taskGranteeFromProjectAccessList 						= "";
	public static String taskGrantGranteeAccessFromProjectAccessList 			= "";
	public static String selectParentProjectIdUsingPAL 							= "";
	public static String selectParentProjectOwnerUsingPAL 						= "";
	public static String selectParentProjectVisibilityUsingPAL					= "";
	public static String subTaskParentId 										= "";
	public static String deletedSubtaskParentId									= "";
	public static String granteeFromProjectAccessList            				= "";
	public static String grantGranteeAccessFromProjectAccessList 				= "";
	public static String selectTaskAssignee 									= "";
	public static String selectBudgetParentProjectId 							= "";
	//Risk
	
    //
    
    public static  String selectOrganization 									= "";
    public static  String selectOwnerOrganization 								= "";

    public static String SELECT_IS_PROJECT_SPACE 								= "";
    public static String SELECT_IS_PROJECT_CONCEPT								= "";
   	public static String SELECT_IS_TASK	 										= "";
    public static String SELECT_IS_CHANGE_TASK                                  = "";
   	public static String SELECT_IS_PHASE	 									= "";
   	public static String SELECT_IS_GATE	 										= "";
   	public static String SELECT_IS_MILESTONE	   								= "";
   	public static String SELECT_IS_ASSESSMENT	   								= "";
   	public static String SELECT_IS_RISK	   										= "";
   	public static String SELECT_IS_BUDGET	   									= "";
   	public static String selectParentProjectTypeUsingPAL						= "";
   	public static String selectParentProjectConceptTypeUsingPAL					= "";
   	public static String SELECT_POLICY 											= "";
   	//FD00
   	public static String SELECT_IS_BENEFIT 										= "";
   	public static String SELECT_IS_QUALITY 										= "";
   	public static String selectQualityHolder									= "";

	@SuppressWarnings("unchecked")
	public static void init(Context context) throws FrameworkException
	{
		//schema import
		SELECT_OWNER 							= "owner";
		LOGICAL_ACCESS_PROJECT_LEAD    			= "Project Lead";
		SELECT_POLICY 							= "policy";
		selectOrganization 						= "organization";
		RELATIONSHIP_EMPLOYEE 					= PropertyUtil.getSchemaProperty("relationship_Employee");

		//Project and Task Related
		ATTRIBUTE_PROJECT_VISIBILITY   	 		= PropertyUtil.getSchemaProperty(context, "attribute_ProjectVisibility");

		TYPE_PROJECT_SPACE 						= PropertyUtil.getSchemaProperty("type_ProjectSpace");
		TYPE_TASK_MANAGEMENT 					= PropertyUtil.getSchemaProperty("type_TaskManagement");
		TYPE_TASK  								= PropertyUtil.getSchemaProperty("type_Task");
        TYPE_CHANGE_TASK                        = PropertyUtil.getSchemaProperty("type_ChangeTask");
		TYPE_PHASE 								= PropertyUtil.getSchemaProperty("type_Phase");
		TYPE_GATE  								= PropertyUtil.getSchemaProperty("type_Gate");
		TYPE_MILESTONE  						= PropertyUtil.getSchemaProperty("type_Milestone");

		RELATIONSHIP_MEMBER 	         		= PropertyUtil.getSchemaProperty("relationship_Member");
		RELATIONSHIP_PROJECT_ACCESS_LIST 		= PropertyUtil.getSchemaProperty("relationship_ProjectAccessList");
		RELATIONSHIP_PROJECT_ACCESS_KEY 		= PropertyUtil.getSchemaProperty("relationship_ProjectAccessKey");
		RELATIONSHIP_SUBTASK 					= PropertyUtil.getSchemaProperty("relationship_Subtask");
		RELATIONSHIP_ASSIGNED_TASK 				= PropertyUtil.getSchemaProperty("relationship_AssignedTasks");

		//Assessment related
		TYPE_ASSESSMENT 						= PropertyUtil.getSchemaProperty("type_Assessment");
		RELATIONSHIP_PROJECT_ASSESSMENT 		= PropertyUtil.getSchemaProperty("relationship_ProjectAssessment");

		//Budget related
		TYPE_BUDGET 							= PropertyUtil.getSchemaProperty("type_Budget");
		RELATIONSHIP_FINANCIAL_ITEMS 			= PropertyUtil.getSchemaProperty("relationship_FinancialItems");
		RELATIONSHIP_PROJECT_FINANCIAL_ITEM 	= PropertyUtil.getSchemaProperty("relationship_ProjectFinancialItem");
		TYPE_COST_ITEM 							= PropertyUtil.getSchemaProperty("type_CostItem");
		TYPE_BENEFIT							= PropertyUtil.getSchemaProperty("type_Benefit");
		// Risk Related
		TYPE_RISK                           	= PropertyUtil.getSchemaProperty("type_Risk");
		POLICY_PROJECT_RISK						= PropertyUtil.getSchemaProperty("policy_ProjectRisk");
		RELATIONSHIP_RISK_ITEM                  = PropertyUtil.getSchemaProperty("relationship_RiskItem");
		RELATIONSHIP_RISK_RPN		 			= PropertyUtil.getSchemaProperty("relationship_RiskRPN");
		RELATIONSHIP_ASSIGNED_RISK  			= PropertyUtil.getSchemaProperty("relationship_AssignedRisk");
		RELATIONSHIP_RISK 						= PropertyUtil.getSchemaProperty("relationship_Risk");
		RELATIONSHIP_REFERENCE_DOCUMENT			= PropertyUtil.getSchemaProperty("relationship_ReferenceDocument");

   		
   		SELECT_IS_PROJECT_SPACE 				= "type.kindof[" + DomainConstants.TYPE_PROJECT_SPACE + "]";
   		SELECT_IS_PROJECT_CONCEPT 				= "type.kindof[" + DomainConstants.TYPE_PROJECT_CONCEPT + "]";
   	   	SELECT_IS_TASK	 						= "type.kindof[" + TYPE_TASK + "]";
        SELECT_IS_CHANGE_TASK                   = "type.kindof[" + TYPE_CHANGE_TASK + "]";
   	   	SELECT_IS_PHASE	 						= "type.kindof[" + TYPE_PHASE + "]";
   	   	SELECT_IS_GATE	 						= "type.kindof[" + TYPE_GATE + "]";
   	   	SELECT_IS_MILESTONE	 					= "type.kindof[" + TYPE_MILESTONE + "]";
   	   	SELECT_IS_ASSESSMENT	 				= "type.kindof[" + TYPE_ASSESSMENT + "]";
   	   	SELECT_IS_RISK	 						= "type.kindof[" + TYPE_RISK + "]";
   	   	SELECT_IS_BUDGET	 					= "type.kindof[" + TYPE_BUDGET + "]";
   	   	SELECT_IS_BENEFIT	 					= "type.kindof[" + TYPE_BENEFIT + "]";
   	   	SELECT_IS_QUALITY	 					= "type.kindof[" + TYPE_QUALITY + "]";
   	   	
   	
		// project space migration related
		selectProjectvisibilityFromPAL 		    = "from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to." +getAttributeSelect(ATTRIBUTE_PROJECT_VISIBILITY);
		selectAttributeProjectVisibility        = "attribute["+ATTRIBUTE_PROJECT_VISIBILITY+"]";
		prgSelectAttrbProjectVisibility         = "attribute["+RELATIONSHIP_MEMBER+"].to.id";
		prgProjectMemberIds                     = "from["+RELATIONSHIP_MEMBER+"].to.id";
		projectAccessListIdFromProject          = "to["+RELATIONSHIP_PROJECT_ACCESS_LIST+"].from.id";

		// access grantees from PAL
		projectSpaceGranteeFromProjectAccessList			= "to["+RELATIONSHIP_PROJECT_ACCESS_LIST+"].from.grantee";	
		projectSpaceGrantGranteeAccessFromProjectAccessList = "to["+RELATIONSHIP_PROJECT_ACCESS_LIST+"].from.grant.granteeaccess";

		granteeFromProjectAccessList 						= "to["+RELATIONSHIP_PROJECT_ACCESS_KEY+"].from.grantee";	
		grantGranteeAccessFromProjectAccessList 			= "to["+RELATIONSHIP_PROJECT_ACCESS_KEY+"].from.grant.granteeaccess";

		taskGranteeFromProjectAccessList 					= granteeFromProjectAccessList;
		taskGrantGranteeAccessFromProjectAccessList 		= grantGranteeAccessFromProjectAccessList;

		budgetGranteeFromProjectAccessList            		= granteeFromProjectAccessList; //"to["+RELATIONSHIP_PROJECT_ACCESS_KEY+"].from.grantee";	
		budgetGrantGranteeAccessFromProjectAccessList 		= grantGranteeAccessFromProjectAccessList; //"to["+RELATIONSHIP_PROJECT_ACCESS_KEY+"].from.grant.granteeaccess";

		//task migration related
		selectParentProjectIdUsingPAL = "to["+RELATIONSHIP_PROJECT_ACCESS_KEY+"].businessobject.from["+RELATIONSHIP_PROJECT_ACCESS_LIST+"].to.id";
		selectParentProjectOwnerUsingPAL = "to["+RELATIONSHIP_PROJECT_ACCESS_KEY+"].businessobject.from["+RELATIONSHIP_PROJECT_ACCESS_LIST+"].to.owner";
		selectParentProjectVisibilityUsingPAL = "to["+RELATIONSHIP_PROJECT_ACCESS_KEY+"].businessobject.from["+RELATIONSHIP_PROJECT_ACCESS_LIST+"].to."+getAttributeSelect(ATTRIBUTE_PROJECT_VISIBILITY);
		subTaskParentId = "to["+RELATIONSHIP_SUBTASK+"].from.id";
		selectTaskAssignee = "to["+RELATIONSHIP_ASSIGNED_TASK+"].from.name";
		deletedSubtaskParentId  = "to["+RELATIONSHIP_DELETED_SUBTASK+"].from.id";
		// assessment selectables
		assessmentProjectId  = "to["+RELATIONSHIP_PROJECT_ASSESSMENT+"].from.id";
		// budget selectables
		budgetProjectId                               = "to["+RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"].from.id";
		selectBudgetCostItemList  = "from["+RELATIONSHIP_FINANCIAL_ITEMS+"].to.id";
		//Risk
		selectRiskProjectIdFromPAL      					= "to["+RELATIONSHIP_PROJECT_ACCESS_KEY+"].businessobject.from["+RELATIONSHIP_PROJECT_ACCESS_LIST+"].to.id";
		selectRiskAssignee      							= "to[" + RELATIONSHIP_ASSIGNED_RISK + "].from.name";
		selectRiskItems      								= "from[" + RELATIONSHIP_RISK_ITEM + "].to.id";
		selectRiskHolder	      							= "to[" + RELATIONSHIP_RISK + "].from.id";
   	    
   	    selectOwnerOrganization = "owner.object[Person].to["+RELATIONSHIP_EMPLOYEE+"].from.name";
   	    selectParentProjectTypeUsingPAL = "to["+RELATIONSHIP_PROJECT_ACCESS_KEY+"].businessobject.from["+RELATIONSHIP_PROJECT_ACCESS_LIST+"].to.type.kindof["+TYPE_PROJECT_SPACE+"]";
   	    selectParentProjectConceptTypeUsingPAL = "to["+RELATIONSHIP_PROJECT_ACCESS_KEY+"].businessobject.from["+RELATIONSHIP_PROJECT_ACCESS_LIST+"].to.type.kindof["+TYPE_PROJECT_CONCEPT+"]";
   	    selectQualityHolder	      							= "to[" + RELATIONSHIP_QUALITY + "].from.id";
   	 
		DomainObject.MULTI_VALUE_LIST.add(prgProjectMemberIds);
		DomainObject.MULTI_VALUE_LIST.add(projectAccessListIdFromProject);
		DomainObject.MULTI_VALUE_LIST.add(projectSpaceGranteeFromProjectAccessList);
		DomainObject.MULTI_VALUE_LIST.add(projectSpaceGrantGranteeAccessFromProjectAccessList);


		DomainObject.MULTI_VALUE_LIST.add(granteeFromProjectAccessList);
		DomainObject.MULTI_VALUE_LIST.add(grantGranteeAccessFromProjectAccessList);

		DomainObject.MULTI_VALUE_LIST.add(projectSpaceGranteeFromProjectAccessList);
		DomainObject.MULTI_VALUE_LIST.add(projectSpaceGrantGranteeAccessFromProjectAccessList);


		DomainObject.MULTI_VALUE_LIST.add(selectTaskAssignee);
		DomainObject.MULTI_VALUE_LIST.add(selectBudgetCostItemList);

		DomainObject.MULTI_VALUE_LIST.add(selectRiskAssignee);
		DomainObject.MULTI_VALUE_LIST.add(selectRiskItems);

		String command = "list role *_PRJ";
		String result = MqlUtil.mqlCommand(context, command, true);
		USER_PROJECTS = StringUtil.split(result, "\n");
		
	}

	@SuppressWarnings("unchecked")
	@Override 
	public void migrateObjects(Context context, StringList objectList) throws Exception
	{
	        mqlLogRequiredInformationWriter("In emxSecurityMigrationMigrateProgramObjects 'migrateObjects' method "+"\n");
		init(context);
		mxObjectSelects.addElement("id");
		mxObjectSelects.addElement("type");
		mxObjectSelects.addElement("name");
		mxObjectSelects.addElement(SELECT_POLICY);
		/*mxObjectSelects.addElement("grantee");
        mxObjectSelects.addElement("grant.granteeaccess");*/
		mxObjectSelects.addElement(SELECT_OWNER);
		mxObjectSelects.addElement(selectParentProjectIdUsingPAL);
		mxObjectSelects.addElement(selectParentProjectOwnerUsingPAL);
		mxObjectSelects.addElement(selectParentProjectVisibilityUsingPAL);

		// for project space
		mxObjectSelects.addElement(selectAttributeProjectVisibility);
		mxObjectSelects.addElement(projectAccessListIdFromProject);
		mxObjectSelects.addElement(projectSpaceGranteeFromProjectAccessList);
		mxObjectSelects.addElement(projectSpaceGrantGranteeAccessFromProjectAccessList);
		mxObjectSelects.addElement(prgProjectMemberIds);
		mxObjectSelects.addElement(selectProjectvisibilityFromPAL);
		mxObjectSelects.addElement(prgSelectAttrbProjectVisibility);
		// for grantee accesses 
		mxObjectSelects.addElement(granteeFromProjectAccessList );
		mxObjectSelects.addElement(grantGranteeAccessFromProjectAccessList );
		// for task  
		mxObjectSelects.addElement(taskGranteeFromProjectAccessList);
		mxObjectSelects.addElement(taskGrantGranteeAccessFromProjectAccessList);
		mxObjectSelects.addElement(subTaskParentId);
		mxObjectSelects.addElement(selectTaskAssignee);
		mxObjectSelects.addElement(deletedSubtaskParentId);
		// for assessment 
		mxObjectSelects.add(assessmentProjectId);
		//for budget
		mxObjectSelects.add(budgetProjectId);
		mxObjectSelects.add(budgetGranteeFromProjectAccessList);		
		mxObjectSelects.add(budgetGrantGranteeAccessFromProjectAccessList);
		mxObjectSelects.add(selectBudgetCostItemList);
		//for risk
		mxObjectSelects.add(selectRiskProjectIdFromPAL);
		mxObjectSelects.add(selectRiskAssignee);
		mxObjectSelects.add(selectRiskItems);
		mxObjectSelects.add(selectRiskHolder);
        //

        mxObjectSelects.add(selectOrganization);
        mxObjectSelects.add(selectOwnerOrganization);
        
        mxObjectSelects.add(SELECT_IS_PROJECT_SPACE);
        mxObjectSelects.add(SELECT_IS_PROJECT_CONCEPT);
        mxObjectSelects.add(SELECT_IS_TASK);
        mxObjectSelects.add(SELECT_IS_CHANGE_TASK);
        mxObjectSelects.add(SELECT_IS_PHASE);
        mxObjectSelects.add(SELECT_IS_GATE);
        mxObjectSelects.add(SELECT_IS_MILESTONE);
        mxObjectSelects.add(SELECT_IS_ASSESSMENT);
        mxObjectSelects.add(SELECT_IS_RISK);
        mxObjectSelects.add(SELECT_IS_BUDGET);
        mxObjectSelects.add(SELECT_IS_BENEFIT);
        mxObjectSelects.add(SELECT_IS_QUALITY);
        mxObjectSelects.add(selectParentProjectTypeUsingPAL);
        mxObjectSelects.add(selectParentProjectConceptTypeUsingPAL);
        mxObjectSelects.add(selectQualityHolder);
        
		String[] oidsArray = new String[objectList.size()];
		oidsArray = (String[])objectList.toArray(oidsArray);
		MapList mapList = DomainObject.getInfo(context, oidsArray, mxObjectSelects);
   		try{
		migratePOVObjects(context, mapList);
		migrateSOVObjects(context, mapList);
   		}catch(Exception e){
   			throw (e);
   		}
	}
	
	private void migratePOVObjects(Context context, MapList mapList) throws Exception{
		mqlLogWriter("-------------------------------------------" + "\n");
		mqlLogRequiredInformationWriter("=========:: POV MIGRATION STARTED ::=========");
		Iterator<?> itr = mapList.iterator();
		while(itr.hasNext())
        {
			Map<?, ?> m = (Map<?, ?>)itr.next();
	    	String oid = (String)m.get("id");
	    	String org = (String)m.get(selectOrganization); 
	    	String ownerOrg = (String)m.get(selectOwnerOrganization);
	    	mqlLogRequiredInformationWriter("Oobject Id="+oid+", Object Org="+org+" , ownerOrg="+ownerOrg);
	    	//boolean hasMatchingOrg = ownerOrg != null ? ownerOrg.equalsIgnoreCase(org) : false;
	    	if(ProgramCentralUtil.isNotNullString(ownerOrg)){
	    		boolean hasMatchingOrg = ownerOrg.equalsIgnoreCase(org);
	    		if(!hasMatchingOrg){
		    		DomainObject object = DomainObject.newInstance(context);
		    		object.setId(oid);
		    		object.setPrimaryOwnership(context, DEFAULT_COLLABORATIVE_SPACE,ownerOrg);
		    	}
	    	}
        }
		
		mqlLogRequiredInformationWriter("=========:: POV MIGRATION COMPLETED ::=========");
	}
	
	private void migrateSOVObjects(Context context, MapList mapList) throws Exception{

		Iterator<?> itr = mapList.iterator();
		mqlLogWriter("-------------------------------------------" + "\n");
		mqlLogRequiredInformationWriter("=========:: SOV MIGRATION STARTED ::=========");
		while(itr.hasNext())
		{
			unconvertable = false;
			unConvertableComments = "";
			Map<?, ?> m = (Map<?, ?>)itr.next();
			String type = (String)m.get("type");
			String name = (String)m.get("name");
			String oid = (String)m.get("id");

        	String isProjectSpace = (String)m.get(SELECT_IS_PROJECT_SPACE);
			String isProjectConcept = (String)m.get(SELECT_IS_PROJECT_CONCEPT);
        	String isTask = (String)m.get(SELECT_IS_TASK);
            String isChangeTask = (String)m.get(SELECT_IS_CHANGE_TASK);
        	String isPhase = (String)m.get(SELECT_IS_PHASE);
        	String isGate = (String)m.get(SELECT_IS_GATE);
        	String isMilestone = (String)m.get(SELECT_IS_MILESTONE);
        	String isAssessment = (String)m.get(SELECT_IS_ASSESSMENT);
        	String isRisk = (String)m.get(SELECT_IS_RISK);
        	String isBudget = (String)m.get(SELECT_IS_BUDGET);
        	String isBenefit = (String)m.get(SELECT_IS_BENEFIT);
        	String isQuality = (String)m.get(SELECT_IS_QUALITY);
		
			if("true".equalsIgnoreCase(isProjectSpace) || "true".equalsIgnoreCase(isProjectConcept)){
				migrateProjectSpace(context, m);
            }else if ("true".equalsIgnoreCase(isTask) || "true".equalsIgnoreCase(isChangeTask) || "true".equalsIgnoreCase(isPhase) ||
        			 "true".equalsIgnoreCase(isGate) || "true".equalsIgnoreCase(isMilestone)) {
				migrateTask(context, m);
			}else if("true".equalsIgnoreCase(isAssessment)){
				migrateAssessment(context, m);
        	}else if("true".equalsIgnoreCase(isBudget)) {
				migrateBudget(context, m);
        	}else if ("true".equalsIgnoreCase(isRisk)) {
		                migrateRisk(context, m);
		}else if("true".equalsIgnoreCase(isBenefit)) {
			migrateBenefit(context, m);
    	}else if("true".equalsIgnoreCase(isQuality)) {
			migrateQuality(context, m);
    	}
        	
        	else {
				unconvertable = true;
				unConvertableComments += "TYPE: " + type + " IS NOT CONSIDERED FOR SOV MIGRATION ....  \n";
			}

			if( unconvertable )
			{
				writeUnconvertedOID(unConvertableComments, oid);
			} else {
				loadMigratedOids(oid);
			//	mqlLogRequiredInformationWriter(":: SOV CREATED FOR OBJECT OF TYPE: '" +type + "'  NAME: '"+name + "' OID: "+oid);
			}
			mqlLogWriter("-------------------------------------------" + "\n");
			mqlLogWriter(m.toString() +"\n");
		}
		mqlLogWriter("=========:: SOV MIGRATION COMPLETED ::=========" + "\n");
		}

	@Override
	public void stampDefaultProjectAndOrganization(Context context) throws Exception{
		mqlLogRequiredInformationWriter("=========:: STARTED KERNEL STAMPING ::=========");

		String defaultPrj = DEFAULT_COLLABORATIVE_SPACE;
		String defaultOrg = DEFAULT_ORGANIZATION;
		mqlLogRequiredInformationWriter(" DEFAULT_COLLABORATIVE_SPACE= "+DEFAULT_COLLABORATIVE_SPACE);
		mqlLogRequiredInformationWriter(" DEFAULT_ORGANIZATION= "+DEFAULT_ORGANIZATION);
		try{
			executeKernelStamping(context,defaultPrj,defaultOrg);
		}catch(Exception e){
			mqlLogRequiredInformationWriter(":::Exception in executing kernel stamping"); 
			throw e;
		}
		mqlLogRequiredInformationWriter("=========:: COMPLETED KERNEL STAMPING ::=========");
	}

	private void executeKernelStamping(Context context,String project, String organization ) throws Exception{
		StringBuffer policyList = new StringBuffer(100); 
		mqlLogRequiredInformationWriter("-- Creating Kernel MQL Command----");
		
		String prgPolicyList = "";
		Iterator itr = emxProgramFindObjects_mxJPO.programPolicyList.iterator();
		while(itr.hasNext()){
			String policy = (String)itr.next();
			policyList.append("\"");
			policyList.append(policy);
			policyList.append("\"");

			if(itr.hasNext()){
				policyList.append(",");
			}
		}
		prgPolicyList = policyList.toString();
   		// The MQL Command
		StringBuffer  sbCommand = new StringBuffer(100);
		sbCommand.append("transition project_org policy ");
		sbCommand.append(prgPolicyList);
		sbCommand.append(" project \"");
		sbCommand.append(project);
		sbCommand.append("\" organization \"");
		sbCommand.append(organization);
		sbCommand.append("\"");
				
		String strMQLCommand = sbCommand.toString(); 
		//"transition project_org policy "+prgPolicyList+ " project \""+project+"\" organization \""+organization+"\"";
		mqlLogRequiredInformationWriter("-- Executing Kernel MQL Command----");
		mqlLogRequiredInformationWriter(strMQLCommand);
		mqlLogRequiredInformationWriter("--------------");
		MqlUtil.mqlCommand(context, strMQLCommand);
		
		//MqlUtil.mqlCommand(context, command, true,"project_org", project, organization);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void migrateProjectSpace(Context context, Map<?, ?> m) throws Exception 
	{
		logMigrationStarted(context,m,"migrate"+m.get("type"));

		Map<String, Collection> objectAccessMap = (Map<String, Collection>)((Map<String, Map>)getTotalAccessMap(context, m)).get("PARENTACCESSMAP");
		boolean isCompanyVisibleProject = isCompanyVisibleProjectSpace(context, m);
    	if(!isCompanyVisibleProject){
    		mqlLogRequiredInformationWriter("-- 'MEMBER' VISIBILITY PROJECT --");
			removePrimaryOwnershipForProject(context, m);
    	}else{
    		mqlLogRequiredInformationWriter("-- 'COMPANY' VISIBILITY PROJECT --");
		}

		StringList projectMemberIds  = (StringList)m.get(prgProjectMemberIds);
		mqlLogRequiredInformationWriter("PROJECT MEMBER LIST: "+projectMemberIds);
		Object objList = m.get(projectSpaceGranteeFromProjectAccessList);
		
		if( objList != null )
		{
			mqlLogRequiredInformationWriter("grantee=="+m.get(projectSpaceGranteeFromProjectAccessList));
			StringList grantees = (StringList)objList;
			grantees = removeDuplicates(grantees);
			mqlLogRequiredInformationWriter("PROJECT SPACE MEMBER NAMES: "+grantees);
			if(grantees == null || grantees.size() == 0){
				mqlLogRequiredInformationWriter("GRANTEE'S ARE NULL CAN NOT CREATE OWNERSHIP FOR BUDGET: "+m.get("name")+"  ODI: "+m.get("id"));
				unconvertable = true;
				unConvertableComments += "GRANTEE'S ARE NULL CAN NOT CREATE OWNERSHIP FOR BUDGET: "+m.get("name")+"  ODI: "+m.get("id");
				return;
			}
			
			Iterator gItr = grantees.iterator();
			while(gItr.hasNext())
			{
				String grantee = (String)gItr.next();
				if(ProgramCentralUtil.isNotNullString(grantee)){
					HashSet accessSet = (HashSet)objectAccessMap.get(grantee);
					StringList accessList = new StringList();
					accessList.addAll(accessSet);
					String sLogicalAccess = PROJECT_ACCESS_MEMBER;
					if(accessList.contains("modify"))
					{
						sLogicalAccess = PROJECT_ACCESS_LEAD;
					}
					if( USER_PROJECTS.contains(grantee+"_PRJ"))
					{
					        mqlLogRequiredInformationWriter("Modify Project  " + m.get("name") + " for user "+ grantee +" with access as "+ sLogicalAccess + " with comment as " + DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
						DomainAccess.createObjectOwnership(context, (String)m.get("id"), null, grantee + "_PRJ", sLogicalAccess, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP,true);
						mqlLogRequiredInformationWriter("-----"+sLogicalAccess+" OWNERSHIP CREATED FOR PROJECT SPACE: '"+m.get("name")+"'------");
					} else {
						mqlLogRequiredInformationWriter("SOV WAS NOT CREATED FOR Project Space WITH OID: " +  (String)m.get("id") + ", for user '" + grantee +"'...." );
						unconvertable = true;
						unConvertableComments += "SOV WAS NOT CREATED FOR PROJECT: " +  (String)m.get("id") + ", for user '" + grantee +"'.... \n" ;
					}
				}
			}
		} else {
			mqlLogRequiredInformationWriter("GRANTEE'S ARE NULL CAN NOT CREATE OWNERSHIP FOR PROJECT: "+m.get("name")+"  ODI: "+m.get("id"));
			unconvertable = true;
			unConvertableComments += "GRANTEE'S ARE NULL CAN NOT CREATE OWNERSHIP FOR PROJECT: "+m.get("name")+"  ODI: "+m.get("id");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void migrateTask(Context context, Map<?, ?> m) throws Exception 
	{
		logMigrationStarted(context,m,"migrateTask");
		String sTaskId = (String)m.get("id");
		boolean isToMigrate = isToMigrateObject(context, m);
		if(isToMigrate)  // only the tasks inside the  Project Space will be migrated
		{
			handlePrimaryOwnership(context,m,sTaskId); //for primary ownership

			String sTaskParentObjectId = getTaskParentId(context, m);  // immediate parent object
			String taskId = (String) m.get(SELECT_ID);
			String strTaskName = (String) m.get(SELECT_NAME);
			String strObjType = (String)m.get("type");
			migrateObjectsWithInheritedOwnership(context, taskId, sTaskParentObjectId, strTaskName,strObjType);

			// get the task assignee and create the SOV for them with Project Lead access
			createOwnershipForObjectAssignee(context, m, selectTaskAssignee);
			// create inherited ownership for task deliverables
			//migrateReferenceDocuments(context,m); no clearity on implementation of reference documents
		}
		else
		{
			mqlLogRequiredInformationWriter("TASK- NAME: "+m.get("name")+", OID: "+sTaskId+" DOES NOT BELONGS TO PROJECT SPACE / CONCEPT. SO IT IS NOT CONSIDERED FOR SOV MIGRATION");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void migrateRisk(Context context, Map<?, ?> m) throws Exception 
	{
		logMigrationStarted(context,m,"migrateRisk");
		String sRiskId = (String)m.get("id");
		boolean isToMigrate = isToMigrateObject(context, m);
		String riskPolicy = (String)m.get(SELECT_POLICY);
		if(isToMigrate)  // only the tasks inside the  Project Space will be migrated
		{
			handlePrimaryOwnership(context,m,sRiskId); //for primary ownership
			mqlLogRequiredInformationWriter("----RISK POLICY IS: "+riskPolicy);
			String sRiskParentObjectId = getRiskParentId(context, m);  // immediate parent object
			String riskId = (String) m.get(SELECT_ID);
			String strRiskName = (String) m.get(SELECT_NAME);
			String strObjType = (String)m.get("type");
			migrateObjectsWithInheritedOwnership(context, riskId, sRiskParentObjectId, strRiskName,strObjType);
			// get the Risk assignee and create the SOV for them
			if(ProgramCentralUtil.isNotNullString(POLICY_PROJECT_RISK) && POLICY_PROJECT_RISK.equalsIgnoreCase(riskPolicy)){
			createOwnershipForObjectAssignee(context, m, selectRiskAssignee);
			}else{
				mqlLogRequiredInformationWriter("ASSIGNEE OWNERSHIP CANNOT BE CREATED FOR RISK- NAME: "+m.get("name")+", OID: "+sRiskId+" POLICY: "+riskPolicy);
			}
			// cause DomainAccess.xml contains entry for Project Risk policy and not Risk policy. 
			//migrate attachments
			//migrateReferenceDocuments(context,m);   // no clearity for implementation
		}
		else
		{
			mqlLogRequiredInformationWriter("RISK- NAME: "+m.get("name")+", OID: "+sRiskId+" DOES NOT BELONGS TO PROJECT SPACE / CONCEPT. SO IT IS NOT CONSIDERED FOR SOV MIGRATION");
			unconvertable = true;
			unConvertableComments += "RISK- NAME: "+m.get("name")+", OID: "+sRiskId+" DOES NOT BELONGS TO PROJECT SPACE / CONCEPT. SO IT IS NOT CONSIDERED FOR SOV MIGRATION" ;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void migrateAssessment(Context context, Map<?, ?> m) throws Exception 
	{
		logMigrationStarted(context,m,"migrateAssessment");
		String sAssessmentId = (String)m.get("id");
		boolean isToMigrate = isToMigrateObject(context, m);
		if(isToMigrate)  // only the tasks inside the  Project Space will be migrated
		{
			handlePrimaryOwnership(context,m,sAssessmentId); //for primary ownership
			String sAssessmentParentObjectId = getAssessmentParentId(context, m);  // parent project id

			String strAssessmentName = (String) m.get(SELECT_NAME);
			String strObjType = (String)m.get("type");
			migrateObjectsWithInheritedOwnership(context, sAssessmentId, sAssessmentParentObjectId, strAssessmentName,strObjType);
			//migrate attachments
			//	migrateReferenceDocuments(context,m);
		}
		else
		{
			mqlLogRequiredInformationWriter("Assessment- NAME: "+m.get("name")+", OID: "+sAssessmentId+" DOES NOT BELONGS TO PROJECT SPACE. SO IT IS NOT CONSIDERED FOR SOV MIGRATION");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void migrateBudget(Context context, Map<?, ?> m) throws Exception {

		logMigrationStarted(context,m,"migrateBudget");
		Map<String, Collection> objectAccessMap = (Map<String, Collection>)((Map<String, Map>)getTotalAccessMap(context, m)).get("PARENTACCESSMAP");

		String budgetParentProjectId = (String)m.get(budgetProjectId);
		String strBudgetId = (String)m.get("id");
		StringList costItemList = new StringList();
		boolean isToMigrate = isToMigrateObject(context, m);
		if(!isToMigrate){
			mqlLogRequiredInformationWriter("CANNOT CREATE SOV FOR BUDGET/BENEFIT NAME: '"+ m.get("name") +"', OID: "+strBudgetId+" AS IT DOES NOT BELONG TO PROJECT SPACE / CONCEPT.");
			unconvertable = true;
			unConvertableComments += "THE BUDGET '"+ m.get("name") +"' DOES NOT BELONG TO PROJECT SPACE / CONCEPT \n" ;
			return;// only the budgets inside the  Project Space will be migrated
		}

		handlePrimaryOwnershipForBudget(context,m,strBudgetId); //for primary ownership

		Object objList = m.get(budgetGranteeFromProjectAccessList);

		if(objList == null){
			mqlLogRequiredInformationWriter("GRANTEE'S ARE NULL CAN NOT CREATE OWNERSHIP FOR BUDGET/BENEFIT: "+m.get("name")+"  ODI: "+m.get("id"));
			unconvertable = true;
			unConvertableComments += "GRANTEE'S ARE NULL CAN NOT CREATE OWNERSHIP FOR BUDGET/BENEFIT: "+m.get("name")+"  ODI: "+m.get("id");
			return;
		}

		mqlLogRequiredInformationWriter("BUDGET/BENEFIT Grantee from PAL ::"+objList);
		StringList grantees = (StringList)objList; 
		grantees = removeDuplicates(grantees);
		if(grantees == null || grantees.size() == 0){
			mqlLogRequiredInformationWriter("GRANTEE'S ARE NULL CAN NOT CREATE OWNERSHIP FOR BUDGET: "+m.get("name")+"  ODI: "+m.get("id"));
			unconvertable = true;
			unConvertableComments += "GRANTEE'S ARE NULL CAN NOT CREATE OWNERSHIP FOR BUDGET: "+m.get("name")+"  ODI: "+m.get("id");
			return;
		}

		Iterator gItr = grantees.iterator();
		while(gItr.hasNext())
		{
			String grantee = (String)gItr.next();
			HashSet accessSet = (HashSet)objectAccessMap.get(grantee);
			StringList accessList = new StringList();
			accessList.addAll(accessSet);
			String logicalAccess = PROJECT_ACCESS_MEMBER;
			if(accessList.contains("modify")){
				logicalAccess = PROJECT_ACCESS_LEAD;
			}

			if( USER_PROJECTS.contains(grantee+"_PRJ"))
			{
				mqlLogRequiredInformationWriter("------------------------------------------------------");
				mqlLogRequiredInformationWriter("Modify Budget/Benefit  " + m.get("name") + " for user "+ grantee +" with access as "+ logicalAccess + " with comment as " + DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
				DomainAccess.createObjectOwnership(context, (String)m.get("id"), null, grantee + "_PRJ", logicalAccess, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP,true);
				mqlLogRequiredInformationWriter("-----"+logicalAccess+" OWNERSHIP CREATED FOR BUDGET/BENEFIT: '"+m.get("name")+"'------");
			} else {
				mqlLogRequiredInformationWriter("SOV WAS NOT CREATED FOR BUDGET/BENEFIT: " +  (String)m.get("id") + ", for user '" + grantee +"'.... \n" );
				unconvertable = true;
				unConvertableComments += "SOV WAS NOT CREATED FOR BUDGET/BENEFIT: " +  (String)m.get("id") + ", for user '" + grantee +"'.... \n" ;
			}
		}

		// migration of cost item
		StringList contentIds = (StringList)m.get(selectBudgetCostItemList);
		if(null != contentIds )
		{
			costItemList.addAll(contentIds);
		}
		mqlLogRequiredInformationWriter(":: STARTED MIGRATION OF Cost/Benefit Items: "+costItemList);

		migrateObjectsWithInheritedOwnership(context, costItemList, strBudgetId,m) ;

		mqlLogRequiredInformationWriter(":: COMPLETED MIGRATION OF Cost/Benefit Items");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void migrateBenefit(Context context, Map<?, ?> m) throws Exception {
		migrateBudget(context, m);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void migrateQuality(Context context, Map<?, ?> m) throws Exception 
	{
		logMigrationStarted(context,m,"migrateQuality");
		String sQualityId = (String)m.get("id");
		boolean isToMigrate = isToMigrateObject(context, m);
		if(isToMigrate)  // only the tasks inside the  Project Space will be migrated
		{
			handlePrimaryOwnership(context,m,sQualityId); //for primary ownership
			String sQualityParentObjectId = getParentId(context, m, selectQualityHolder);  // parent project id

			String strQualityName = (String) m.get(SELECT_NAME);
			String strObjType = (String)m.get("type");
			migrateObjectsWithInheritedOwnership(context, sQualityId, sQualityParentObjectId, strQualityName,strObjType);
			//migrate attachments
			//	migrateReferenceDocuments(context,m);
		}
		else
		{
			mqlLogRequiredInformationWriter("Quality- NAME: "+m.get("name")+", OID: "+sQualityId+" DOES NOT BELONGS TO PROJECT SPACE. SO IT IS NOT CONSIDERED FOR SOV MIGRATION");
		}
	}
	
	private boolean isCompanyVisibleProjectSpace(Context context, Map<?, ?> m) throws Exception{

		String  attributeProjectVisibilityValue = (String)m.get(selectAttributeProjectVisibility);
		boolean isCompanyVisibleProject = true;

		if("Members".equalsIgnoreCase(attributeProjectVisibilityValue))     
		{
			isCompanyVisibleProject = false;
		}

		return isCompanyVisibleProject;
	}

	private void removePrimaryOwnershipForProject(Context context, Map<?, ?> m) throws Exception{

		String projectIdValue = (String)m.get("id"); 
		DomainObject dmoProjectObject = DomainObject.newInstance(context, projectIdValue);
		dmoProjectObject.removePrimaryOwnership(context);
		mqlLogRequiredInformationWriter("-- PRIMARY OWNERSHIP REMOVED --");
	}

	private void removePrimaryOwnership(Context context, String projectId) throws Exception{

		DomainObject dmoProjectObject = DomainObject.newInstance(context, projectId);
		dmoProjectObject.removePrimaryOwnership(context);
	}

	private boolean isToMigrateObject(Context context,Map<?, ?> m) throws Exception{

    	Object containerType = m.get(selectParentProjectTypeUsingPAL);  // the the TOP level Project Id
    	Object containerTypeForConcept = m.get(selectParentProjectConceptTypeUsingPAL);  // the the TOP level Project Concept Id
    	String sContainerType = "";
    	String sContainerTypeForConcept = "";
    	if(containerType instanceof StringList){
    		StringList slcontainerType = (StringList)containerType;
    		sContainerType = (slcontainerType != null) ? (String)slcontainerType.get(0) : "";
			}
    	else{
    		sContainerType = (String)containerType;
		}
    	
    	if(containerTypeForConcept instanceof StringList){
    		StringList slcontainerType = (StringList)containerTypeForConcept;
    		sContainerTypeForConcept = (slcontainerType != null) ? (String)slcontainerType.get(0) : "";
    	}else{
    		sContainerTypeForConcept = (String)containerTypeForConcept;
    	}
    	
    	// if parent type is kind of project space 
    	boolean isToMigrateForProject = "TRUE".equalsIgnoreCase(sContainerType) ? true : false;

    	// if parent type is kind of project Concept 
    	boolean isToMigrateForConcept = "TRUE".equalsIgnoreCase(sContainerTypeForConcept) ? true : false;
    	
    	boolean isToMigrate = isToMigrateForProject || isToMigrateForConcept;

		return isToMigrate;
	}

	private String getTaskParentId(Context context, Map<?, ?> m) throws Exception{

		Object taskParentObjectId = m.get(subTaskParentId);
		String sTaskParentObjectId = "";
		if(taskParentObjectId instanceof StringList)
		{
			StringList slTaskParentId = (StringList)taskParentObjectId;
			sTaskParentObjectId = (slTaskParentId != null) ? (String)slTaskParentId.get(0) : "";
		}
		else
		{
			sTaskParentObjectId = (String)taskParentObjectId;
		}

		// if task is not connected by subtask 
		if(null == sTaskParentObjectId || "".equalsIgnoreCase(sTaskParentObjectId) ||  "null".equalsIgnoreCase(sTaskParentObjectId))
		{
			Object deletedTaskParentObjectId = m.get(deletedSubtaskParentId);

			if(deletedTaskParentObjectId instanceof StringList)
			{
				StringList slDeletedTaskParentId = (StringList)deletedTaskParentObjectId;
				sTaskParentObjectId = (slDeletedTaskParentId != null) ? (String)slDeletedTaskParentId.get(0) : "";
			}
			else
			{
				sTaskParentObjectId = (String)deletedTaskParentObjectId;
			}
		}
		return sTaskParentObjectId;
	}

	private String getAssessmentParentId(Context context, Map<?, ?> m) throws Exception{

		Object assessmentParentObjectId = m.get(assessmentProjectId);
		String sAssessmentParentObjectId = "";
		if(assessmentParentObjectId instanceof StringList)
		{
			StringList slAssessmentParentId = (StringList)assessmentParentObjectId;
			sAssessmentParentObjectId = (slAssessmentParentId != null) ? (String)slAssessmentParentId.get(0) : "";
		}
		else
		{
			sAssessmentParentObjectId = (String)assessmentParentObjectId;
		}

		return sAssessmentParentObjectId;
	}

	private String getRiskParentId(Context context, Map<?, ?> m) throws Exception{

		Object riskParentObjectId = m.get(selectRiskHolder);
		String sRiskParentObjectId = "";
		if(riskParentObjectId instanceof StringList)
		{
			StringList slRiskParentId = (StringList)riskParentObjectId;
			sRiskParentObjectId = (slRiskParentId != null) ? (String)slRiskParentId.get(0) : "";
		}
		else
		{
			sRiskParentObjectId = (String)riskParentObjectId;
		}

		return sRiskParentObjectId;
	}

	private String getParentId(Context context, Map<?, ?> m, String relName){
		Object parentObjectId = m.get(relName);
		String sParentObjectId = "";
		if(parentObjectId instanceof StringList)
		{
			StringList slParentId = (StringList)parentObjectId;
			sParentObjectId = (slParentId != null) ? (String)slParentId.get(0) : "";
		}
		else
		{
			sParentObjectId = (String)parentObjectId;
		}

		return sParentObjectId;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void createOwnershipForObjectAssignee(Context context, Map<?, ?> m, String selectObjectAssignee) throws Exception{

		Object objectAssignee = m.get(selectObjectAssignee);

		List<String> slObjectAssigneeList = new ArrayList<String>();
		if(objectAssignee instanceof StringList){
			slObjectAssigneeList = (StringList) objectAssignee;
		}else{
			String sObjectAssignee = (String)objectAssignee;
			if(ProgramCentralUtil.isNotNullString(sObjectAssignee)){
			slObjectAssigneeList.add(sObjectAssignee);
		}
		}

		String strObjType = (String)m.get("type");
		String strObjName = (String)m.get("name");


		for(String grantee : slObjectAssigneeList){

			if( USER_PROJECTS.contains(grantee+"_PRJ"))
			{
				mqlLogRequiredInformationWriter("Creating SOV for Assignee's for Object of TYPE: "+strObjType +", NAME: " + strObjName + " for user "+ grantee +" with access as "+ LOGICAL_ACCESS_PROJECT_LEAD + " with comment as " + DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
				DomainAccess.createObjectOwnership(context, (String)m.get("id"), null, grantee + "_PRJ", LOGICAL_ACCESS_PROJECT_LEAD, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP,true);
				mqlLogRequiredInformationWriter("-----"+LOGICAL_ACCESS_PROJECT_LEAD+" OWNERSHIP CREATED FOR: '"+m.get("name")+"'----------------------------");
			} else {
				mqlLogRequiredInformationWriter("CANNOT CREATE SOV FOR ASSIGNEE ON OBJECT OF TYPE: "+strObjType+", NAME: "+strObjName +", OID: "+(String)m.get("id") +" FOR USER: '" + grantee +"' " );
				unconvertable = true;
				unConvertableComments += "Modify "+strObjType +  (String)m.get("id") + " adding Multiple Ownership for user '" + grantee +"' is not supported due to two reason 1. The member is a Role or Group 2. The member is not Migrated to be added.... \n" ;
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void migrateObjectsWithInheritedOwnership(Context context, StringList objectItemList, String strParentObjectId, Map<?,?> m) throws Exception{

		int size = objectItemList!= null?objectItemList.size() : 0; 
		StringList objectSelects = new StringList();
		objectSelects.addElement(SELECT_ID);
		objectSelects.addElement(SELECT_NAME);

		if( size > 0 )
		{
			String[] oidsArray = new String[size];
			oidsArray = (String[])objectItemList.toArray(oidsArray);
			MapList mapList = DomainObject.getInfo(context, oidsArray, objectSelects);
			mqlLogWriter("CREATE INHERITED OWNERSHIP FOR Content MapList = "+ mapList+" FROM PARENT: "+strParentObjectId);
			Iterator<?> itr = mapList.iterator();
			while(itr.hasNext())
			{
				Map cMap = (Map)itr.next();
				String strName =  (String)cMap.get(SELECT_NAME);
				String strObjectId = (String)cMap.get(SELECT_ID);
				migrateObjectsWithInheritedOwnership(context,strObjectId,strParentObjectId, strName,m,true);
			}
		}
	}

	private boolean migrateObjectsWithInheritedOwnership(Context context, String strObjectId, String strParentId, String objName, Map<?,?> m, boolean handlePrimaryOwnership) throws Exception{

		if(handlePrimaryOwnership){
			handlePrimaryOwnership(context, m, strObjectId);
		}

		if(!DomainAccess.hasObjectOwnership(context, strObjectId, strParentId, "")){
			mqlLogRequiredInformationWriter(":: Inherited Ownership Created for Object NAME: "+objName+", OID: "+strObjectId+" FROM PARENT: "+strParentId);
			DomainAccess.createObjectOwnership(context, strObjectId, strParentId, "");
			return Boolean.TRUE;
		}else{
			mqlLogRequiredInformationWriter("::INHERITED OWNERSHIP ALREADY EXISTS FOR Object NAME: "+objName+", OID: "+strObjectId+" FROM PARENT: "+strParentId);
		}
		return Boolean.FALSE;
	}

	private boolean migrateObjectsWithInheritedOwnership(Context context, String strObjectId, String strParentId, String objName,String sObjType) throws Exception{

		if(ProgramCentralUtil.isNullString(strParentId)){
			mqlLogRequiredInformationWriter(":: CANNOT CREATE Inherited Ownership for Object TYPE: "+sObjType+", NAME: "+objName+", OID: "+strObjectId+" ,FROM PARENT: "+strParentId);
			unconvertable = true;
			unConvertableComments += "Object Name: '"+ objName +"', Type: "+sObjType+", OID: "+strObjectId+" , does not have parent" ;
			return Boolean.FALSE;
		}
		
		if(!DomainAccess.hasObjectOwnership(context, strObjectId, strParentId, "")){
			mqlLogRequiredInformationWriter(":: Inherited Ownership Created for Object TYPE: "+sObjType+", NAME: "+objName+", OID: "+strObjectId+" FROM PARENT: "+strParentId);
		        DomainAccess.createObjectOwnership(context, strObjectId, strParentId, "");
			return Boolean.TRUE;
		}else{
			mqlLogRequiredInformationWriter("::INHERITED OWNERSHIP ALREADY EXISTS FOR Object TYPE: "+sObjType+",NAME: "+objName+", OID: "+strObjectId+" FROM PARENT: "+strParentId);
		}
		return Boolean.FALSE;
	}

	private void handlePrimaryOwnership(Context context, Map<?,?> m, String objid) throws Exception{

		String strProjectSpaceId = (String)m.get(selectParentProjectIdUsingPAL);
		String strProjectSpaceOwner = (String)m.get(selectParentProjectOwnerUsingPAL);
		String strProjectSpaceAttrbProjectVisibility = (String)m.get(selectParentProjectVisibilityUsingPAL);
		String name = (String)m.get("name");
		String type = (String)m.get("type");
		String oid = (String)m.get("id");
		
		if(ProgramCentralUtil.isNotNullString(strProjectSpaceId) && 
				ProgramCentralUtil.isNotNullString(strProjectSpaceOwner))
		{
			if("Members".equalsIgnoreCase(strProjectSpaceAttrbProjectVisibility)){
				removePrimaryOwnership(context,objid);
    			mqlLogRequiredInformationWriter("::PARENT PROJECT VISIBILITY IS MEMBERS.  PRIMARY OWNERSHIP REMOVED FOR OBJECT TYPE: "+type+", NAME: "+name+", OID: "+oid);
		} else {
				mqlLogRequiredInformationWriter(" PARENT PROJECT VISIBILITY IS COMPANY ");
			}
		}
	}

	private void handlePrimaryOwnershipForBudget(Context context, Map<?,?> m, String objid) throws Exception{

		String strProjectSpaceId = (String)m.get(selectParentProjectIdUsingPAL);
		String strProjectSpaceOwner = (String)m.get(selectParentProjectOwnerUsingPAL);
		String name = (String)m.get("name");
		// for budget remove primary ownership independent of project space visibility 
		if(ProgramCentralUtil.isNotNullString(strProjectSpaceId) && 
				ProgramCentralUtil.isNotNullString(strProjectSpaceOwner))
		{
			removePrimaryOwnership(context,objid);
   			mqlLogRequiredInformationWriter("PRIMARY OWNERSHIP REMOVED FOR BUDGET NAME: " +name+", OID: "+objid);
		}
	}

	@SuppressWarnings("unchecked")
	private static StringList removeDuplicates(StringList list)
	{
		HashSet<String> hashSet = new HashSet<String>();
		for(int i = 0; i < list.size(); i++)
		{
			String str = (String) list.get(i);
			if(ProgramCentralUtil.isNotNullString(str))
				hashSet.add(str);
		}
		list.clear();
		list.addAll(hashSet);
		return list;
	}

	public void mqlLogRequiredInformationWriter(String command) throws Exception
	{
		super.mqlLogRequiredInformationWriter(command +"\n");
	}

	public void mqlLogWriter(String command) throws Exception
	{
		super.mqlLogWriter(command +"\n");
	}

	private void logMigrationStarted(Context context, Map<?, ?>m, String fromMethodName)throws Exception{
		String type = (String)m.get("type");
		String oid = (String)m.get("id");
		String name = (String)m.get("name");
		mqlLogWriter("-------------------------------------------" + "\n");
		mqlLogRequiredInformationWriter("Started migration of Object: TYPE= "+type+",  NAME= "+name+",  OID= "+oid);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, Map> getTotalAccessMap(Context context, Map<?, ?> m) throws Exception {
		Set keySet = m.keySet();
		Iterator keyItr = keySet.iterator();
		Map<String, Map> totalAccessMap = new HashMap<String, Map>();
		Map<String, Collection> objectAccessMap = new HashMap<String, Collection>();
		Map<String, Collection> parentAccessMap = new HashMap<String, Collection>();
		totalAccessMap.put("OBJECTACCESSMAP", objectAccessMap);
		totalAccessMap.put("PARENTACCESSMAP", parentAccessMap);
		//Collection accessBits = new HashSet(56);
		while(keyItr.hasNext())
		{
			String key = (String)keyItr.next();
			if( key.indexOf("grant[") >= 0)
			{
				int startBindex = key.indexOf("[", key.indexOf("grant"));
				int closeBindex = key.indexOf("]", key.indexOf("grant"));
				String gratorGrantee = key.substring(startBindex+1, closeBindex);
				if( gratorGrantee.indexOf(",") > 0)
				{
					String grantee = gratorGrantee.substring(gratorGrantee.indexOf(",")+1, gratorGrantee.length());
					String keyValue = (String)m.get(key);
					keyValue = keyValue.replace("majorrevise,", "");
					if(keyValue.contains("grant") )
					{
						keyValue = keyValue.replace("grant,", "");
						keyValue = keyValue.replace("revoke,", "changeowner,");
					}
					Map<String, Collection> localAccessMap = new HashMap<String, Collection>();
					if( key.startsWith("grant[") )                                      //TODO need to checl the way to be used for task and project
					{
						localAccessMap = totalAccessMap.get("OBJECTACCESSMAP");
					} else {
						localAccessMap = totalAccessMap.get("PARENTACCESSMAP");
					}
					StringList accessList = StringUtil.split(keyValue, ",");
					if(localAccessMap.containsKey(grantee))
					{
						Collection accessBits = (Collection)localAccessMap.get(grantee);
						accessBits.addAll(accessList);
					} else {
						Collection accessBits = new HashSet(56);
						accessBits.addAll(accessList);
						localAccessMap.put(grantee, accessBits);
					}
				}
			}
		}
		return totalAccessMap;
	}

	static protected Long getAccessFlag(StringList masks) throws Exception 
	{
		IntList intList = new IntList(masks.size());
		Access access = new Access();
		for (int i=0; i < masks.size(); i++) {
			String mask = ((String) masks.get(i)).trim().toLowerCase();
			Object maskValue = _accessMasksConstMapping.get(mask);
			if (maskValue != null) {
				int cMask = (Integer) maskValue;
				intList.addElement(cMask);
			}
		}
		access.processIntList(intList);
		return Long.valueOf(access.getLongAccessFlag());
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	private Map<String, Collection> getObjectAccessMap(Context context, Map<?, ?> m) 
	{
		Set keySet = m.keySet();
		Iterator keyItr = keySet.iterator();
		Map<String, Collection> accessMap = new HashMap<String, Collection>();
		//Collection accessBits = new HashSet(56);
		while(keyItr.hasNext())
		{
			String key = (String)keyItr.next();
			if( key.startsWith("grant["))
			{
				int startBindex = key.indexOf("[");
				int closeBindex = key.indexOf("]");
				String gratorGrantee = key.substring(startBindex+1, closeBindex);
				if( gratorGrantee.indexOf(",") > 0)
				{
					String grantee = gratorGrantee.substring(gratorGrantee.indexOf(",")+1, gratorGrantee.length());
					String keyValue = (String)m.get(key);
					keyValue = keyValue.replace("majorrevise,", "");
					if(keyValue.contains("grant") )
					{
						keyValue = keyValue.replace("grant,", "");
						keyValue = keyValue.replace("revoke,", "changeowner,");
					}
					StringList accessList = StringUtil.split(keyValue, ",");
					if(accessMap.containsKey(grantee))
					{
						Collection accessBits = (Collection)accessMap.get(grantee);
						accessBits.addAll(accessList);
					} else {
						Collection accessBits = new HashSet(56);
						accessBits.addAll(accessList);
						accessMap.put(grantee, accessBits);
					}
				}
			}
		}
		return accessMap;
	}
}

