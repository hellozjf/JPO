/* emxFinancialItemBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.11.2.2 Thu Dec  4 07:55:01 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.11.2.1 Thu Dec  4 01:53:11 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.11 Wed Oct 22 15:49:41 2008 przemek Experimental przemek $
*/

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import matrix.db.AccessConstants;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Relationship;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Task;
import com.matrixone.apps.domain.CurrencyConversion;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DebugUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.program.BenefitItem;
import com.matrixone.apps.program.BenefitItemIntervalRelationship;
import com.matrixone.apps.program.CostItem;
import com.matrixone.apps.program.CostItemIntervalRelationship;
import com.matrixone.apps.program.Currency;
import com.matrixone.apps.program.FinancialItem;
import com.matrixone.apps.program.FinancialTemplateCategory;
import com.matrixone.apps.program.Financials;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.ProjectTemplate;
import com.matrixone.apps.program.fiscal.CalendarType;
import com.matrixone.apps.program.fiscal.Interval;
import com.matrixone.apps.program.fiscal.IntervalType;
/**
 * The <code>emxFinancialItemBase</code> class represents the Financial Item
 * types functionality for the AEF.
 *
 * @version AEF 9.5.1.3 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxFinancialItemBase_mxJPO extends com.matrixone.apps.program.FinancialItem
{
	/** Id of the Access List Object for this Assessment. */
	protected DomainObject _accessListObject = null;

	/** The project access list id relative to this object. */
	static protected final String SELECT_PROJECT_ACCESS_LIST_ID =
			"to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.id";
	static final String SELECT_PROJECT_BENEFIT_BUDGET=(new StringBuilder()).append("from[").append(Financials.RELATIONSHIP_PROJECT_FINANCIAL_ITEM).append("].to.id").toString();
	static final String SELECT_BENEFIT_INTERVAL_PLANNED_BENEFIT=(new StringBuilder()).append("from[").append(RELATIONSHIP_BENEFIT_ITEM_INTERVAL).append("].attribute[").append(ATTRIBUTE_PLANNED_BENEFIT).append("]").toString();
	static final String SELECT_BENEFIT_INTERVAL_INTERVAL_DATE=(new StringBuilder()).append("from[").append(RELATIONSHIP_BENEFIT_ITEM_INTERVAL).append("].attribute[").append(BenefitItemIntervalRelationship.ATTRIBUTE_INTERVAL_DATE).append("]").toString();
	static final String SELECT_COST_INTERVAL_INTERVAL_DATE=(new StringBuilder()).append("from[").append(RELATIONSHIP_COST_ITEM_INTERVAL).append("].attribute[").append(CostItemIntervalRelationship.ATTRIBUTE_INTERVAL_DATE).append("]").toString();
	static final String SELECT_COST_INTERVAL_PLANNED_COST=(new StringBuilder()).append("from[").append(RELATIONSHIP_COST_ITEM_INTERVAL).append("].attribute[").append(ATTRIBUTE_PLANNED_COST).append("]").toString();
	static final String SELECT_BENEFIT_INTERVAL_ESTIMATED_BENEFIT=(new StringBuilder()).append("from[").append(RELATIONSHIP_BENEFIT_ITEM_INTERVAL).append("].attribute[").append(ATTRIBUTE_ESTIMATED_BENEFIT).append("]").toString();
	static final String SELECT_BENEFIT_INTERVAL_ACTUAL_BENEFIT=(new StringBuilder()).append("from[").append(RELATIONSHIP_BENEFIT_ITEM_INTERVAL).append("].attribute[").append(ATTRIBUTE_ACTUAL_BENEFIT).append("]").toString();
	static final String SELECT_COST_INTERVAL_ESTIMATED_COST="from["+RELATIONSHIP_COST_ITEM_INTERVAL+"].attribute["+ATTRIBUTE_ESTIMATED_COST+"]";
	static final String SELECT_COST_INTERVAL_ACTUAL_COST="from["+RELATIONSHIP_COST_ITEM_INTERVAL+"].attribute["+ATTRIBUTE_ACTUAL_COST+"]";
	public static final String SELECT_BUDGET_ID = "from["+Financials.RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"].to.id";
	static final String SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE = "attribute[" + ATTRIBUTE_TASK_ESTIMATED_START_DATE  + "]";
	static final String SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE= "attribute[" + ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE  + "]";
	static final String BUDGET_PLAN_TABLE = "PMCProjectBudgetPlanTable";
	static final String BUDGET_ESTIMATED_TABLE = "PMCProjectBudgetEstimatedTable";
	static final String BUDGET_ACTUAL_TABLE = "PMCProjectBudgetActualTable";
	static final String PROJECTTEMPLATETABLE = "PMCProjectTemplateBudgetTable";
	static final String PROJECTBUDGETREPORTTABLE = "PMCProjectBudgetReportTable";
	static final String BENEFITCURRENCYFILTER=
			PropertyUtil.getSchemaProperty("command_PMCProjectBenefitCurrencyFilter");
	static final String BENEFITINTERVALFILTER=
			PropertyUtil.getSchemaProperty("command_PMCProjectBenefitIntervalFilter");
	static final String BENEFITVIEWFILTER=
			PropertyUtil.getSchemaProperty("command_PMCProjectBenefitViewsFilter");

	static final String BUDGETCURRENCYFILTER=
			PropertyUtil.getSchemaProperty("command_PMCProjectBudgetCurrencyFilter");
	static final String BUDGETINTERVALFILTER=
			PropertyUtil.getSchemaProperty("command_PMCProjectBudgetIntervalFilter");
	static final String BUDGETVIEWFILTER=
			PropertyUtil.getSchemaProperty("command_PMCProjectBudgetViewsFilter");
	protected static String frmCurrency=null;
	static final String BUDGETVIEWREPORTFILTER=
			PropertyUtil.getSchemaProperty("command_PMCProjectBudgetViewsReportFilter");

	static final String BUDGETTIMELINEREPORTFILTER=
			PropertyUtil.getSchemaProperty("command_PMCProjectBudgetReportFilterCommand");

	static final String BUDGETFISCALYEARFILTER=
			PropertyUtil.getSchemaProperty("command_PMCProjectBudgetFiscalYearFilter");

	static final String BUDGETREPORTFISCALYEARFILTER=
			PropertyUtil.getSchemaProperty("command_PMCProjectBudgetReportFiscalYearFilter");

	static final String BUDGETREPORTCURRENCYFILTER=
			PropertyUtil.getSchemaProperty("command_PMCExpenseReportCurrencyFilter");

	static final String BUDGETDISPLAYVIEWFILTER=
			PropertyUtil.getSchemaProperty("command_PMCProjectBudgetDisplayViewFilter");

	protected final static String planView="Plan View";
	protected final static String estimateView="Estimate View";
	protected final static String actualView= "Actual View";

	/**
	 * Constructs a new emxFinancialItem JPO object.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String id
	 * @throws Exception if the operation fails
	 * @since AEF 9.5.1.3
	 */
	public emxFinancialItemBase_mxJPO (Context context, String[] args)
			throws Exception
			{
		// Call the super constructor
		super();
		if (args != null && args.length > 0)
		{
			setId(args[0]);
		}
			}

	/**
	 * Get the access list object for this Financial Item.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @throws Exception if the operation fails
	 * @return DomainObject of Access list Object
	 * @since AEF 9.5.1.3
	 */
	protected DomainObject getAccessListObject(Context context)
			throws Exception
			{
		if (_accessListObject == null)
		{
			String accessListID = getInfo(context,
					SELECT_PROJECT_ACCESS_LIST_ID);
			if (accessListID != null && ! "".equals(accessListID))
			{
				_accessListObject = DomainObject.newInstance(context, accessListID);
			}
		}
		return _accessListObject;
			}

	/**
	 * This function verifies the user's permission for the given
	 * Financial Item.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *   [PROJECT_MEMBER|FINANCIAL_REVIEWER|
	 *      PROJECT_LEAD|PROJECT_OWNER|PROJECT_USER] <BR>
	 *   PROJECT_MEMBER to see if the context user is a project member, <BR>
	 *   PROJECT_ASSESSOR to see if the context user is a project assessor, <BR>
	 *   PROJECT_LEAD to see if the context user is a project lead, <BR>
	 *   PROJECT_OWNER to see if the context user is a project owner.
	 *   PROJECT_USER to see if the context user is a project user.
	 * @return boolean true if access check passed else returns false
	 * @throws Exception if the operation fails
	 * @since AEF 9.5.1.3
	 */
	public boolean hasAccess(Context context, String args[])
			throws Exception
			{
		boolean access = false;
		DomainObject accessListObject = getAccessListObject(context);

		if (accessListObject != null)
		{
			for (int i = 0; i < args.length; i++)
			{
				String accessType = args[i];
				int iAccess;
				if ("PROJECT_MEMBER".equals(accessType) ||
						"PROJECT_USER".equals(accessType))
				{
					iAccess = AccessConstants.cExecute;
				}
				else if ("PROJECT_LEAD".equals(accessType))
				{
					iAccess = AccessConstants.cModify;
				}
				else if ("FINANCIAL_REVIEWER".equals(accessType))
				{
					iAccess = AccessConstants.cModifyForm;
				}
				else if ("PROJECT_OWNER".equals(accessType))
				{
					iAccess = AccessConstants.cOverride;
				}
				else
				{
					continue;
				}
				if (accessListObject.checkAccess(context, (short) iAccess))
				{
					access = true;
					break;
				}
			}
		}
		return access;
			}

	/**
	 * When a Financial Item is deleted, delete all associated Benefit &
	 * Cost Items.
	 * Note: OBJECTID is required as method argument.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the Object Id
	 * @return 0 so that Matrix core actually deletes the Financial Item
	 * @throws Exception if operation fails
	 * @since AEF 9.5.2.0
	 */
	public int triggerDeleteOverride(Context context, String[] args)
			throws Exception
			{
		DebugUtil.debug("Entering emxFinancialItem triggerDeleteOverride");
		com.matrixone.apps.program.BenefitItem benefitItem = null;
		com.matrixone.apps.program.CostItem costItem = null;

		// get values from args.
		String objectId = args[0];
		setId(objectId);

		StringList objectSelects = new StringList(1);
		objectSelects.add(SELECT_ID);
		objectSelects.add(SELECT_BENEFIT_INTERVAL_ITEM_DATA_ID);

		MapList mapList = benefitItem.getBenefitItems(
				context, this, objectSelects, null);

		DomainObject domainObject = DomainObject.newInstance(context);

		Iterator itr = mapList.iterator();
		while (itr.hasNext())
		{
			Map map = (Map) itr.next();
			String childId = (String) map.get(SELECT_ID);
			Object interval = map.get(
					SELECT_BENEFIT_INTERVAL_ITEM_DATA_ID);
			if (interval != null)
			{
				String intervalId;
				if (interval instanceof java.util.List)
				{
					intervalId = (String) ((java.util.List) interval).get(0);
				}
				else
				{
					intervalId = (String) interval;
				}
				domainObject.setId(intervalId);
				domainObject.remove(context);
			}
			domainObject.setId(childId);
			domainObject.remove(context);
		}

		objectSelects.remove(SELECT_BENEFIT_INTERVAL_ITEM_DATA_ID);
		objectSelects.add(costItem.SELECT_INTERVAL_ITEM_DATA_ID);
		mapList = costItem.getCostItems(context, this, objectSelects, null);

		itr = mapList.iterator();
		while (itr.hasNext())
		{
			Map map = (Map) itr.next();
			String childId = (String) map.get(SELECT_ID);
			Object interval = map.get(
					costItem.SELECT_INTERVAL_ITEM_DATA_ID);
			if (interval != null)
			{
				String intervalId;
				if (interval instanceof java.util.List)
				{
					intervalId = (String) ((java.util.List) interval).get(0);
				}
				else
				{
					intervalId = (String) interval;
				}
				domainObject.setId(intervalId);
				domainObject.remove(context);
			}
			domainObject.setId(childId);
			domainObject.remove(context);
		}

		DebugUtil.debug("Exiting FinancialItem triggerDeleteOverride");
		return 0;
			}
	/* This function verifies the financial item has been created
	 * for the project or not.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param projectspace holds the following input arguments:
	 * @return boolean true if access check passed else returns false
	 * @throws Exception if the operation fails
	 * @since PMC X+2
	 */
	private boolean hasFinancialItem(Context context,ProjectSpace projectSpace)throws Exception{
		String strProjectId = projectSpace.getId(context);
		DomainObject dmoProjectId = DomainObject.newInstance(context, strProjectId);
		CostItem costItem = new CostItem();
		String StrBudgetID = costItem.getBudgetorBenefitCreated(context, TYPE_BUDGET, dmoProjectId);
		boolean hasFinancialItem = false;
		if(StrBudgetID != null && !"".equals(StrBudgetID) && !"null".equals(StrBudgetID) ){
			hasFinancialItem = true;
		}
		return hasFinancialItem;
	}

	/**
	 * Verifies whether the create and import links need to be shown or not.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @return boolean if true commands are accessible.
	 * @throws Exception if the operation fails
	 */
	public boolean checkFinCreateImportAccess(Context context, String args[])
			throws Exception{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strTableName = (String) programMap.get("table");
		String objectId = (String) programMap.get("objectId");
		DomainObject object = DomainObject.newInstance(context, objectId);
		boolean access = false;
		try{
			if("PMCProjectTemplateBudgetTable".equals(strTableName)){
				ProjectTemplate projectTemplate = (ProjectTemplate)DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_TEMPLATE, DomainObject.PROGRAM);
		 		boolean isCtxUserOwnerOrCoOwner = projectTemplate.isOwnerOrCoOwner(context, objectId);
		 		if(!isCtxUserOwnerOrCoOwner)
		 			return false;
			}

			if(object.isKindOf(context, TYPE_PROJECT_SPACE) || object.isKindOf(context, TYPE_PROJECT_TEMPLATE) ){
				emxProgramCentralUtilBase_mxJPO.getUserPreferenceCurrency(context, "emxProjectBudget.Error.NoDefaultCurrencyDefined");
				ProjectSpace project = new ProjectSpace(objectId);
				StringList select = new StringList(1);
				select.add(ProgramCentralConstants.SELECT_ID);

				MapList budgetInfoList  = new MapList();
				try{
					ProgramCentralUtil.pushUserContext(context);
					budgetInfoList = FinancialItem.getFinancialBudgetOrBenefit(context, project, select, ProgramCentralConstants.TYPE_BUDGET);
				}finally{
					ProgramCentralUtil.popUserContext(context);
				}
				//If Project has budget, Create Budget command must not be allowed.
				if(null != budgetInfoList && budgetInfoList.size()!=0){
					return false;
				}

				String policyFinancialItems = PropertyUtil.getSchemaProperty(context, "policy_FinancialItems");
				String planFrozenStateName = PropertyUtil.getSchemaProperty(context,"policy", policyFinancialItems,"state_PlanFrozen");
				String planStateName = PropertyUtil.getSchemaProperty(context,"policy", policyFinancialItems,"state_Plan");

				//If User has modify access on Project, Create command is allowed.
				String state = project.getInfo(context, ProgramCentralConstants.SELECT_CURRENT);
				if(STATE_PROJECT_SPACE_CREATE.equals(state) || STATE_PROJECT_SPACE_ASSIGN.equals(state)||
				   STATE_PROJECT_SPACE_ACTIVE.equals(state) || STATE_PROJECT_SPACE_REVIEW.equals(state) ||
				   STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equals(state) || planStateName.equals(state) || planFrozenStateName.equals(state) ){
					access = project.checkAccess(context, (short)AccessConstants.cModify);
				}
			}
			return access;
		}catch(Exception e){
			return access;
		}

		//{Old Security Impl}
		//          HashMap programMap = (HashMap) JPO.unpackArgs(args);
		//          String objectId = (String) programMap.get("objectId");
		//          String projectLead  = "Project Lead";
		//          String projectOwner = "Project Owner";
		//          String financialReviewer = "Financial Reviewer";
		//          boolean check = false;
		//    	//Added:di7
		//    	DomainObject dmoProject = DomainObject.newInstance(context,objectId);
		//    	String strPreferredCurrency ="";
		//    	//End:di7
		//        //Added 27-Sep-2010:PRG:RG6:IR-070662V6R2011x
		//          try{
		//    		//Added:di7:08-sept-2011
		//    		if(dmoProject.isKindOf(context, TYPE_PROJECT_SPACE))
		//    			strPreferredCurrency = ${CLASS:emxProgramCentralUtilBase}.getUserPreferenceCurrency(context, "emxProjectBudget.Error.NoDefaultCurrencyDefined");
		//    		//End:di7:08-sept-2011
		//          }catch(Exception e){
		//        	 return check;
		//          }
		//        //End Added 27-Sep-2010:PRG:RG6:IR-070662V6R2011x
		//           com.matrixone.apps.program.ProjectSpace projectSpace =
		//                       (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
		//           projectSpace.setId(objectId);
		//           String access = projectSpace.getAccess(context);
		//           if(!hasFinancialItem(context,projectSpace)){
		//            if(access.equals(projectLead) || access.equals(projectOwner) || access.equals(financialReviewer)){
		//              check = true;
		//            }
		//            else{
		//              check = false;
		//            }
		//          }
		//          return check;
	}
	/* This function verifies whether the delete link
	 * need to be shown or not.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @return boolean true if access check passed else returns false
	 * @throws Exception if the operation fails
	 * @since PMC X+2
	 */
	public boolean checkFinDeleteAccess(Context context, String args[],boolean isCostItem)
			throws Exception{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) programMap.get("objectId");
		String tableName = (String) programMap.get("table");
		String strParentId = ProgramCentralUtil.getParentFromRMBTableRowId(programMap);
		if(ProgramCentralUtil.isNotNullString(strParentId)){
			objectId = strParentId;
		}
		ProjectSpace project = new ProjectSpace(objectId);

		boolean hasFinancialItem = false;
		emxBenefitItem_mxJPO benItm= new emxBenefitItem_mxJPO(context, null);
		StringList select = new StringList();
		select.add(ProgramCentralConstants.SELECT_ID);
		MapList financialInfo = null;
		if(isCostItem){
			financialInfo = FinancialItem.getFinancialBudgetOrBenefit(context, project, select,
					ProgramCentralConstants.TYPE_BUDGET);
		}else{
			financialInfo = FinancialItem.getFinancialBudgetOrBenefit(context, project, select,
					ProgramCentralConstants.TYPE_BENEFIT);
		}
		if(null == financialInfo || financialInfo.size()==0) return false;
		Map financial = (Map) financialInfo.get(0);
		String financialId = (String)financial.get(SELECT_ID);
		DomainObject object = DomainObject.newInstance(context, financialId);
		return object.checkAccess(context, (short) AccessConstants.cModify);

		//Old Security Impl
		//		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		//		String objectId = (String) programMap.get("objectId");
		//
		//		// [ADDED::PRG:RG6:Jan 4, 2011:IR-077536V6R2012 :R211::Start]
		//		String strParentId = ProgramCentralUtil.getParentFromRMBTableRowId(programMap);
		//
		//		if(ProgramCentralUtil.isNotNullString(strParentId)){
		//			objectId = strParentId;
		//		}
		//
		//		// [::PRG:RG6:Jan 4, 2011:IR-Number :R211::End]
		//
		//		String projectLead  = "Project Lead";
		//		String projectOwner = "Project Owner";
		//		String financialReviewer = "Financial Reviewer";
		//		${CLASS:emxBenefitItem} benItm= new ${CLASS:emxBenefitItem}(context, null);
		//		boolean check = false;
		//		com.matrixone.apps.program.ProjectSpace projectSpace =
		//				(com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
		//		projectSpace.setId(objectId);
		//		String access = projectSpace.getAccess(context);
		//		boolean hasFinancialItem=false;
		//		if(isCostItem)
		//			hasFinancialItem=hasFinancialItem(context,projectSpace);
		//		else
		//			hasFinancialItem=benItm.hasBenefitItem(context,projectSpace);
		//
		//		if(hasFinancialItem)
		//		{
		//			if(access.equals(projectLead) || access.equals(projectOwner) || access.equals(financialReviewer)){
		//				check = true;
		//			}
		//			else{
		//				check = false;
		//			}
		//		}
		//		return check;
	}
	/* This function gets all the financial Object connected to the Project object
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @return boolean true if access check passed else returns false
	 * @throws Exception if the operation fails
	 * @since PMC X+2
	 */
	//    public Object getProjectFinancials(Context context, String args[])
	//        throws Exception
	//    {
	//      HashMap programMap = (HashMap) JPO.unpackArgs(args);
	//      String objectId = (String) programMap.get("objectId");
	//      com.matrixone.apps.program.ProjectSpace projectSpace =
	//         (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
	//      StringList busSelects =  new StringList();
	//      busSelects.add(DomainConstants.SELECT_ID);
	//      busSelects.add(DomainConstants.SELECT_OWNER);
	//      projectSpace.setId(objectId);
	//      MapList financialItemList = com.matrixone.apps.program.FinancialItem.getFinancialItems(context, projectSpace, busSelects,strType);
	//      return financialItemList;
	//    }
	/* This function gets all the financial Objects connected to the Project object
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @return boolean true if access check passed else returns false
	 * @throws Exception if the operation fails
	 * @since PMC X+2
	 */
	public Vector getProjectFinancialCheckbox(Context context, String args[])
			throws Exception
			{
		Map programMap = (Map)JPO.unpackArgs(args);
		com.matrixone.apps.common.Person person =
				(com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
		person = person.getPerson(context);
		String personName = (String) person.getInfo(context, person.SELECT_NAME);
		MapList objectList = (MapList) programMap.get("objectList");
		Vector enableCheckbox = new Vector();
		String owner = "";
		for(int i =0; i< objectList.size(); i++)
		{
			Map objectMap = (Map) objectList.get(i);
			owner = (String)objectMap.get(DomainConstants.SELECT_OWNER);
			if(owner.equals(personName)) {
				enableCheckbox.add("true");
			}
			else {
				enableCheckbox.add("false");
			}
		}// eof for
		return enableCheckbox;
			}
	/* This function gets all the Benefit Objects connected to the Financial Object
	 * This method is used to display the Benefit Summary.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @return boolean true if access check passed else returns false
	 * @throws Exception if the operation fails
	 * @since PMC X+2
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public Object getBenefitItems(Context context, String []args) throws Exception
	{

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) programMap.get("objectId");
		MapList benefitItemList = new MapList();
		try{
			com.matrixone.apps.program.FinancialItem financialItem = (com.matrixone.apps.program.FinancialItem)
					DomainObject.newInstance(context,DomainConstants.TYPE_FINANCIAL_ITEM,"PROGRAM");
			financialItem.setId(objectId);
			StringList busSelects = new StringList(1);
			busSelects.add(DomainConstants.SELECT_ID);
			busSelects.add(DomainConstants.SELECT_NAME);
			busSelects.add(DomainConstants.SELECT_OWNER);
			benefitItemList = BenefitItem.getBenefitItems(context, financialItem, busSelects, null);
		}
		catch(Exception e)
		{
			throw (e);
		}
		finally
		{
			return benefitItemList;
		}
	}
	/* This function gets all the Cost Objects connected to the Financial Object
	 * This method is used to display the Cost Summary.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @return boolean true if access check passed else returns false
	 * @throws Exception if the operation fails
	 * @since PMC X+2
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public Object getCostItems(Context context, String []args) throws Exception
	{

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) programMap.get("objectId");
		MapList costItemList = new MapList();
		try{
			com.matrixone.apps.program.FinancialItem financialItem = (com.matrixone.apps.program.FinancialItem)
					DomainObject.newInstance(context,DomainConstants.TYPE_FINANCIAL_ITEM,"PROGRAM");
			financialItem.setId(objectId);
			StringList busSelects = new StringList(1);
			busSelects.add(DomainConstants.SELECT_ID);
			busSelects.add(DomainConstants.SELECT_NAME);
			busSelects.add(DomainConstants.SELECT_OWNER);
			costItemList = CostItem.getCostItems(context, financialItem, busSelects, null);
		}
		catch(Exception e)
		{
			throw (e);
		}
		finally
		{
			return costItemList;
		}
	}
	/**
	 * This method is used to disable Checkbox of benefit and cost
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *    objectList - objectList contains a MapList of Maps which contains objects.
	 * @return Vector containing the risk items value as String.
	 * @throws Exception if the operation fails
	 * @since PMC X+2
	 */
	public Vector showCostBenefitCheckbox(Context context, String[] args)
			throws Exception
			{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		Vector enableCheckbox = new Vector();
		com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person)
				DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
		try
		{
			String owner = "";
			String objectId = "";
			String userId   = (String)person.getPerson(context).getName();
			for(int i =0; i< objectList.size(); i++)
			{
				Map objectMap = (Map) objectList.get(i);
				owner = (String)objectMap.get(DomainConstants.SELECT_OWNER);
				objectId= (String)objectMap.get(DomainConstants.SELECT_ID);
				if (owner.equals(userId)) {
					enableCheckbox.add(true);
				}
				else {
					enableCheckbox.add(false);
				} // eof else
			}// eof for
		} // eof try
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			return enableCheckbox;
		}
			}
	/**Returns i18n Cost Item Names
	 *
	 * @param context Matrix Context Object
	 * @param args String[] arguments packed from Table method
	 * @return Vector of i18n names
	 * @throws Exception if operation fails
	 */
	public Vector getCostItemNames(Context context,String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		String strLanguage = context.getSession().getLanguage();
		Vector vcCostItems = new Vector();
		for (Iterator iterator = objectList.iterator(); iterator.hasNext();)
		{
			Map mapCostItems = (Map) iterator.next();
			String strName = (String) mapCostItems.get(SELECT_NAME);
			String strFormedName = FrameworkUtil.findAndReplace(strName.trim()," ", "_");
			String strKey = "emxProgramCentral.Common."+strFormedName;
			String stri18nName = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					strKey, strLanguage);
			vcCostItems.add(stri18nName);
		}
		return vcCostItems;
	}

	/**Returns i18n Benefit Item Names
	 *
	 * @param context Matrix Context Object
	 * @param args String[] arguments packed from Table method
	 * @return Vector of i18n names
	 * @throws Exception if operation fails
	 */
	public Vector getBenefitItemNames(Context context,String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		String strLanguage = context.getSession().getLanguage();
		Vector vcBenefitItems = new Vector();
		for (Iterator iterator = objectList.iterator(); iterator.hasNext();)
		{
			Map mapBenefitItems = (Map) iterator.next();
			String strName = (String) mapBenefitItems.get(SELECT_NAME);
			String strFormedName = FrameworkUtil.findAndReplace(strName.trim()," ", "_");
			String strKey = "emxProgramCentral.Common."+strFormedName;
			String stri18nName = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					strKey, strLanguage);
			vcBenefitItems.add(stri18nName);
		}
		return vcBenefitItems;
	}



	/**
	 * This method updates the cost/benefit value for Budget/Benefit Table
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @throws Exception if operation fails
	 */

	public void updateDynamicColumnData(Context context,String[]args, boolean isCostItem) throws Exception {
		try
		{
			String sCommandStatement = "temp query bus $1 $2 $3 select $4 dump $5";
			String strPersonId =  MqlUtil.mqlCommand(context, sCommandStatement,TYPE_PERSON, context.getUser(),"*","id","|");
			PersonUtil person = new PersonUtil();
			Map programMap                      = (HashMap) JPO.unpackArgs(args);
			HashMap mpParamMap                      = (HashMap)programMap.get("paramMap");
			Map mpColumnMap                     = (HashMap)programMap.get("columnMap");
			Map mpRequestMap                    = (HashMap)programMap.get("requestMap");
			Locale locale 						= (Locale)mpRequestMap.get("locale");
			String strColumnName                 = (String)mpColumnMap.get("name")+QUERY_WILDCARD;
			String strMonthYearName             = (String)mpColumnMap.get("label");
			String strItemId  = (String)mpParamMap.get("objectId");
			String tableName=(String)mpRequestMap.get("selectedTable");
			String strProjectId = (String)mpRequestMap.get("projectID");
			if(ProgramCentralUtil.isNullString(strProjectId)){
				strProjectId = (String)mpRequestMap.get("parentOID");
			}
			if(isBackgroundTaskActive(context, strProjectId)){
				getCurrencyModifyBackgroundTaskMessage(context, true);
				return;
			}
			String strPrefferedCurrency = Currency.getBaseCurrency(context,strProjectId);
			String currency = "";
			String selecteView = "";

			String strNewValue               = "";
			double nNewValue                 = 0;
			StringList objectSelects=new StringList();
			StringList relSelects=new StringList();

			String SELECT_PLANNED_ATTRIBUTE=null;
			String SELECT_INTERVAL_DATE=null;
			String RELATIONSHIP_ITEM_INTERVAL=null;
			String SELECT_INTERVAL_PLANNED_BENEFIT_COST=null;
			String ATTRIBUTE_PLANNED_BENEFIT_COST=null;
			String SELECT_BENEFIT_COST_PLANNED_VALUE=null;
			String relPattern=  "";
			String SELECT_INTERVAL_DATA_ID = "";
			if(isCostItem)
			{
				currency = (String)mpRequestMap.get(BUDGETCURRENCYFILTER);
				selecteView = (String)mpRequestMap.get(BUDGETVIEWFILTER);
				if(selecteView.contains("%20"))
					selecteView = selecteView.replace("%20", ProgramCentralConstants.SPACE);
				SELECT_PLANNED_ATTRIBUTE=CostItem.SELECT_PLANNED_COST;
				SELECT_INTERVAL_DATE=CostItemIntervalRelationship.SELECT_INTERVAL_DATE;
				RELATIONSHIP_ITEM_INTERVAL=RELATIONSHIP_COST_ITEM_INTERVAL;
				SELECT_INTERVAL_PLANNED_BENEFIT_COST=SELECT_COST_INTERVAL_PLANNED_COST;
				ATTRIBUTE_PLANNED_BENEFIT_COST=ATTRIBUTE_PLANNED_COST;
				SELECT_BENEFIT_COST_PLANNED_VALUE="from["+ RELATIONSHIP_FINANCIAL_ITEMS+"].to."+CostItem.SELECT_PLANNED_COST;
				relPattern=RELATIONSHIP_COST_ITEM_INTERVAL;
				SELECT_INTERVAL_DATA_ID=CostItem.SELECT_INTERVAL_ITEM_DATA_ID;
				objectSelects.add(SELECT_ID);
				if(selecteView.equalsIgnoreCase(planView))
				{
					SELECT_PLANNED_ATTRIBUTE=CostItem.SELECT_PLANNED_COST;
					SELECT_INTERVAL_PLANNED_BENEFIT_COST=SELECT_COST_INTERVAL_PLANNED_COST;
					ATTRIBUTE_PLANNED_BENEFIT_COST=ATTRIBUTE_PLANNED_COST;
					SELECT_BENEFIT_COST_PLANNED_VALUE="from["+ RELATIONSHIP_FINANCIAL_ITEMS+"].to."+CostItem.SELECT_PLANNED_COST;
				}
				if(selecteView.equalsIgnoreCase(estimateView))
				{
					SELECT_PLANNED_ATTRIBUTE=CostItem.SELECT_ESTIMATED_COST;
					SELECT_INTERVAL_PLANNED_BENEFIT_COST=SELECT_COST_INTERVAL_ESTIMATED_COST;
					ATTRIBUTE_PLANNED_BENEFIT_COST=ATTRIBUTE_ESTIMATED_COST;
					SELECT_BENEFIT_COST_PLANNED_VALUE="from["+ RELATIONSHIP_FINANCIAL_ITEMS+"].to."+CostItem.SELECT_ESTIMATED_COST;
				}
				if(selecteView.equalsIgnoreCase(actualView))
				{
					SELECT_PLANNED_ATTRIBUTE=CostItem.SELECT_ACTUAL_COST;
					SELECT_INTERVAL_PLANNED_BENEFIT_COST=SELECT_COST_INTERVAL_ACTUAL_COST;
					ATTRIBUTE_PLANNED_BENEFIT_COST=ATTRIBUTE_ACTUAL_COST;
					SELECT_BENEFIT_COST_PLANNED_VALUE="from["+ RELATIONSHIP_FINANCIAL_ITEMS+"].to."+CostItem.SELECT_ACTUAL_COST;
				}
			}
			else
			{
				SELECT_INTERVAL_DATE=BenefitItemIntervalRelationship.SELECT_INTERVAL_DATE;
				RELATIONSHIP_ITEM_INTERVAL=RELATIONSHIP_BENEFIT_ITEM_INTERVAL;
				currency = (String)mpRequestMap.get(BENEFITCURRENCYFILTER);
				selecteView = (String)mpRequestMap.get(BENEFITVIEWFILTER);
				if(selecteView.contains("%20"))
					selecteView = selecteView.replace("%20", ProgramCentralConstants.SPACE);
				if(selecteView.equalsIgnoreCase(planView))
				{
					SELECT_PLANNED_ATTRIBUTE=BenefitItem.SELECT_PLANNED_BENEFIT;
					SELECT_INTERVAL_PLANNED_BENEFIT_COST=SELECT_BENEFIT_INTERVAL_PLANNED_BENEFIT;
					ATTRIBUTE_PLANNED_BENEFIT_COST=ATTRIBUTE_PLANNED_BENEFIT;
					SELECT_BENEFIT_COST_PLANNED_VALUE="from["+ RELATIONSHIP_FINANCIAL_ITEMS+"].to."+BenefitItem.SELECT_PLANNED_BENEFIT;
				}
				if(selecteView.equalsIgnoreCase(estimateView))
				{
					SELECT_PLANNED_ATTRIBUTE=BenefitItem.SELECT_ESTIMATED_BENEFIT;
					SELECT_INTERVAL_PLANNED_BENEFIT_COST=SELECT_BENEFIT_INTERVAL_ESTIMATED_BENEFIT;
					ATTRIBUTE_PLANNED_BENEFIT_COST=ATTRIBUTE_ESTIMATED_BENEFIT;
					SELECT_BENEFIT_COST_PLANNED_VALUE="from["+ RELATIONSHIP_FINANCIAL_ITEMS+"].to."+BenefitItem.SELECT_ESTIMATED_BENEFIT;
				}

				if(selecteView.equalsIgnoreCase(actualView))
				{
					SELECT_PLANNED_ATTRIBUTE=BenefitItem.SELECT_ACTUAL_BENEFIT;
					SELECT_INTERVAL_PLANNED_BENEFIT_COST=SELECT_BENEFIT_INTERVAL_ACTUAL_BENEFIT;
					ATTRIBUTE_PLANNED_BENEFIT_COST=ATTRIBUTE_ACTUAL_BENEFIT;
					SELECT_BENEFIT_COST_PLANNED_VALUE="from["+ RELATIONSHIP_FINANCIAL_ITEMS+"].to."+BenefitItem.SELECT_ACTUAL_BENEFIT;
				}
			}

			objectSelects.add(SELECT_PLANNED_ATTRIBUTE);
			relSelects.add(DomainRelationship.SELECT_ID);


			String relWhere=SELECT_INTERVAL_DATE+" ~~ \""+strColumnName+"\"";
			String typePattern = TYPE_INTERVAL_ITEM_DATA +","+ TYPE_PHASE;

			try
			{
				boolean showCurrencyConversionWarning = false;
				String strCurrencyConversionWarning = "";
				strNewValue=(String)mpParamMap.get("New Value");
				//start IR-353060
				String sTemp = ProgramCentralConstants.EMPTY_STRING;
				for (int i = 0; i < strNewValue.length(); i++) {
					char c = strNewValue.charAt(i);
					if(Character.isDigit(c)||'.'== c ||','== c ||' '== c){
						sTemp = sTemp + c;
					}
				}
				strNewValue = sTemp.trim();
				strNewValue = strNewValue.replace(" ","");
				Locale currencyLocale = Currency.getCurrencyLocale(context,currency);
				NumberFormat nFormater = NumberFormat.getInstance(currencyLocale);
			  Number numericValue =  nFormater.parse(((Double)Task.parseToDouble(strNewValue)).toString());

				Double num = numericValue.doubleValue();
				strNewValue = Double.toString(num);
				//end
				try{
					strNewValue = Currency.toBaseCurrency(context,strProjectId,strNewValue,true);
				}catch(MatrixException e){
					showCurrencyConversionWarning = true;
					strCurrencyConversionWarning = e.getMessage();
				}
				if(strNewValue.indexOf(currency)!=-1)
					strNewValue=strNewValue.substring(0,strNewValue.indexOf(currency)-1);
				nNewValue = Task.parseToDouble(strNewValue);
				DomainObject benefitDom= DomainObject.newInstance(context, strItemId);

				MapList mlBenIntervalRelId=benefitDom.getRelatedObjects(context,
						RELATIONSHIP_ITEM_INTERVAL,
					        typePattern,
						objectSelects,
						relSelects,
						false,
						true,
						(short)1,
						null,
						relWhere);
				if(mlBenIntervalRelId.size()!=0){
					String strBenIntervalRelId=(String)((Map)mlBenIntervalRelId.get(0)).get(DomainRelationship.SELECT_ID);

					DomainRelationship.setAttributeValue(context,strBenIntervalRelId,ATTRIBUTE_PLANNED_BENEFIT_COST,strNewValue +" "+strPrefferedCurrency);
				}
				else{
					if(isCostItem){
						objectSelects.add(SELECT_ID);
						MapList mlBenIntervalRelId1=benefitDom.getRelatedObjects(context,
								RELATIONSHIP_ITEM_INTERVAL,
								typePattern,
								objectSelects,
								relSelects,
								false,
								true,
								(short)1,
								null,
								null);
						Object slIntervalItmDataId=((Map)mlBenIntervalRelId1.get(0)).get(SELECT_ID);
						String strIntervalItmDataId=null;
						if(slIntervalItmDataId instanceof StringList)
							strIntervalItmDataId=(String)((StringList)slIntervalItmDataId).get(0);
						else
							strIntervalItmDataId=(String)slIntervalItmDataId;

						DomainRelationship beneItmIntervalId=DomainRelationship.connect(context,benefitDom,relPattern,DomainObject.newInstance(context, strIntervalItmDataId));
						Date dtBIDate = eMatrixDateFormat.getJavaDate((String)mpColumnMap.get("name"));
						Calendar calendar =Calendar.getInstance();
						calendar.setTime(dtBIDate);
						SimpleDateFormat sdf=null;
						final String strDateFormat=eMatrixDateFormat.getEMatrixDateFormat();
						sdf = new SimpleDateFormat(strDateFormat);
						String date = sdf.format(calendar.getTime());
						String ATTRIBUTE_INTERVAL_COST_BENEFIT_DATE=CostItemIntervalRelationship.ATTRIBUTE_INTERVAL_DATE;
						beneItmIntervalId.setAttributeValue(context,ATTRIBUTE_INTERVAL_COST_BENEFIT_DATE,date);
						beneItmIntervalId.setAttributeValue(context,ATTRIBUTE_PLANNED_BENEFIT_COST,strNewValue +" "+strPrefferedCurrency);
					}
				}


				//============================================================================================================================================

				StringList slPlannedBenefitValues=benefitDom.getInfoList(context,SELECT_INTERVAL_PLANNED_BENEFIT_COST);

				double nPlannedBenefit=0D;

				int tokens=slPlannedBenefitValues.size();
				for(int i=0;i<tokens;i++)
				{
					String tmp=(String)slPlannedBenefitValues.get(i);
					nPlannedBenefit=nPlannedBenefit+Task.parseToDouble(tmp);

				}

				benefitDom.setAttributeValue(context,ATTRIBUTE_PLANNED_BENEFIT_COST,nPlannedBenefit+" "+strPrefferedCurrency);

				//============================================================================================================================================


				DomainRelationship domRelFinItm=new DomainRelationship((String)mpParamMap.get("relId"));


				Relationship RelFinItm=new Relationship((String)mpParamMap.get("relId"));


				String strRelIdArray[]={(String)mpParamMap.get("relId")};
				relSelects.clear();
				relSelects.add(DomainRelationship.SELECT_FROM_ID);

				MapList mlRelFinItem=DomainRelationship.getInfo(context, strRelIdArray, relSelects);



				DomainObject financialDom= DomainObject.newInstance(context, (String)((Map)mlRelFinItem.get(0)).get(DomainRelationship.SELECT_FROM_ID));


				StringList benefitItemsPlannedBenefitValue= financialDom.getInfoList(context,SELECT_BENEFIT_COST_PLANNED_VALUE);

				nPlannedBenefit=0;
				for(int i=0;i<benefitItemsPlannedBenefitValue.size();i++)
				{
					nPlannedBenefit=nPlannedBenefit+Task.parseToDouble((String)benefitItemsPlannedBenefitValue.get(i));

				}
				financialDom.setAttributeValue(context,ATTRIBUTE_PLANNED_BENEFIT_COST,nPlannedBenefit+" "+strPrefferedCurrency);

				if(showCurrencyConversionWarning)
					MqlUtil.mqlCommand(context, "notice " + strCurrencyConversionWarning);
			}
			catch (NumberFormatException e) {
				String strLanguage = context.getSession().getLanguage();
				String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
						"emxProgramCentral.ProjectBenefit.SelectNumericBenefit", strLanguage);
				throw new Exception(sErrMsg + " " + strMonthYearName);
			}
		}
		catch (Exception exp){
			exp.printStackTrace();
			throw exp;
		}
	}

	/**
	 * In Budget/Benefit table, for each expand operation the data of people associated with requests are returned.
	 *
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */
	public MapList getTableExpandChildProjectBudgetBenefitData(Context context,String[] args, boolean isCostItem) throws MatrixException
	{
		MapList mlItemList = new MapList();
		try{
			Map programMap = (Map)JPO.unpackArgs(args);
			String strFinancialItemId = (String) programMap.get("objectId");
			String strRelationshipPattern = Financials.RELATIONSHIP_FINANCIAL_ITEMS;
			StringList slBusSelect = new StringList();
			String strTypePattern =null;
			Object relPlannedBenefit_CostValues=null;
			Object intervalDateValues=null;
			StringList slRelPlannedBenefit_CostValues=new StringList();
			StringList slIntervalDateValues=new StringList();
			String strPlanned_Benefit_Cost=null;
			String strIntervalDate=null;
			String SELECT_ATTRIBUTE_COST_INTERVAL =null;
			String SELECT_PLANNED_BENEFIT_COST=null;
			String SELECT_PLANNED_BENEFIT_COST_UNIT = null;
			String SELECT_INTERVAL_PLANNED_AMOUNT=null;
			String SELECT_INTERVAL_DATE=null;
			String SELECT_PHASE_ID = null;
			String strViewTableType = "";
			strViewTableType = (String)programMap.get("selectedTable");
			String SELECT_ACTUAL_AMOUNT = "";
			String years = "";
			String selectedView = "";
			String strSelectedCurrency ="";
			String SELECT_ACTUAL_AMOUNT_UNIT = null;
			String SELECT_INTERVAL_PLANNED_AMOUNT_UNIT = null;
			//Code to get Company Id of the person.
			String sCommandStatement = "temp query bus $1 $2 $3 select $4 dump $5";
			String strPersonId =  MqlUtil.mqlCommand(context, sCommandStatement,TYPE_PERSON, context.getUser(),"*","id","|");

			PersonUtil person = new PersonUtil();
			String strPrefferedCurrency = PersonUtil.getCurrency(context);


			if(isCostItem){
				selectedView = (String)programMap.get(BUDGETVIEWFILTER);
				if(ProgramCentralUtil.isNullString(selectedView)){
					selectedView = (String)programMap.get(BUDGETVIEWREPORTFILTER);
				}
				if(ProgramCentralUtil.isNullString(selectedView)){
					selectedView = (String)programMap.get(BUDGETVIEWREPORTFILTER);
				}
				if(programMap.containsKey(BUDGETREPORTCURRENCYFILTER)){
					strSelectedCurrency = (String)programMap.get(BUDGETREPORTCURRENCYFILTER);
					selectedView = actualView;
				}else{
					strSelectedCurrency = (String)programMap.get(BUDGETCURRENCYFILTER);
				}

				strTypePattern = Financials.TYPE_COST_ITEM;
				if(null != selectedView){
					if(planView.equals(selectedView)){
						SELECT_INTERVAL_PLANNED_AMOUNT=SELECT_COST_INTERVAL_PLANNED_COST;
						SELECT_INTERVAL_PLANNED_AMOUNT_UNIT= "from["+RELATIONSHIP_COST_ITEM_INTERVAL+"].attribute["+ATTRIBUTE_PLANNED_COST+"].inputunit";
						SELECT_PLANNED_BENEFIT_COST=CostItem.SELECT_PLANNED_COST;
						SELECT_PLANNED_BENEFIT_COST_UNIT =  "attribute[" + ATTRIBUTE_PLANNED_COST + "].inputunit";
					}
					if(estimateView.equals(selectedView)){
						SELECT_INTERVAL_PLANNED_AMOUNT=SELECT_COST_INTERVAL_ESTIMATED_COST;
						SELECT_INTERVAL_PLANNED_AMOUNT_UNIT= "from["+RELATIONSHIP_COST_ITEM_INTERVAL+"].attribute["+ATTRIBUTE_ESTIMATED_COST+"].inputunit";
						SELECT_PLANNED_BENEFIT_COST=CostItem.SELECT_ESTIMATED_COST;
						SELECT_PLANNED_BENEFIT_COST_UNIT =  "attribute[" + ATTRIBUTE_ESTIMATED_COST + "].inputunit";
						SELECT_ACTUAL_AMOUNT=CostItem.SELECT_ACTUAL_COST;
						SELECT_ACTUAL_AMOUNT_UNIT =  "attribute[" + ATTRIBUTE_ACTUAL_COST + "].inputunit";
						slBusSelect.add(SELECT_ACTUAL_AMOUNT);
						slBusSelect.add(SELECT_ACTUAL_AMOUNT_UNIT);
					}
					if(actualView.equals(selectedView)){
						SELECT_INTERVAL_PLANNED_AMOUNT=SELECT_COST_INTERVAL_ACTUAL_COST;
						SELECT_INTERVAL_PLANNED_AMOUNT_UNIT= "from["+RELATIONSHIP_COST_ITEM_INTERVAL+"].attribute["+ATTRIBUTE_ACTUAL_COST+"].inputunit";
						SELECT_PLANNED_BENEFIT_COST=CostItem.SELECT_ACTUAL_COST;
						SELECT_PLANNED_BENEFIT_COST_UNIT =  "attribute[" + ATTRIBUTE_ACTUAL_COST + "].inputunit";
						SELECT_ACTUAL_AMOUNT=CostItem.SELECT_PLANNED_COST;
						SELECT_ACTUAL_AMOUNT_UNIT =  "attribute[" + ATTRIBUTE_PLANNED_COST + "].inputunit";
						slBusSelect.add(SELECT_ACTUAL_AMOUNT);
						slBusSelect.add(SELECT_ACTUAL_AMOUNT_UNIT);
					}
				}

				if(PROJECTTEMPLATETABLE.equals(strViewTableType)){
					SELECT_INTERVAL_PLANNED_AMOUNT=SELECT_COST_INTERVAL_ACTUAL_COST;
					SELECT_PLANNED_BENEFIT_COST=CostItem.SELECT_ACTUAL_COST;
				}

				SELECT_INTERVAL_DATE=SELECT_COST_INTERVAL_INTERVAL_DATE;
				SELECT_ATTRIBUTE_COST_INTERVAL = "attribute["+ATTRIBUTE_COST_INTERVAL+"]";
				SELECT_PHASE_ID = "from["+RELATIONSHIP_COST_ITEM_INTERVAL+"].to.id";
			}
			else
			{
				strTypePattern = Financials.TYPE_BENEFIT_ITEM;
				SELECT_INTERVAL_DATE=SELECT_BENEFIT_INTERVAL_INTERVAL_DATE;
				// years = (String)programMap.get(BENEFITINTERVALFILTER);
				selectedView = (String)programMap.get(BENEFITVIEWFILTER);
				strSelectedCurrency = (String)programMap.get(BENEFITCURRENCYFILTER);
				SELECT_ATTRIBUTE_COST_INTERVAL = "attribute["+ATTRIBUTE_BENEFIT_INTERVAL+"]";

				if(planView.equals(selectedView)){
					SELECT_PLANNED_BENEFIT_COST=BenefitItem.SELECT_PLANNED_BENEFIT;
					SELECT_PLANNED_BENEFIT_COST_UNIT = "attribute[" + ATTRIBUTE_PLANNED_BENEFIT + "].inputunit";
					SELECT_INTERVAL_PLANNED_AMOUNT=SELECT_BENEFIT_INTERVAL_PLANNED_BENEFIT;
					SELECT_INTERVAL_PLANNED_AMOUNT_UNIT= "from["+RELATIONSHIP_BENEFIT_ITEM_INTERVAL+"].attribute["+ATTRIBUTE_PLANNED_BENEFIT+"].inputunit";
				}
				if(estimateView.equals(selectedView)){
					SELECT_PLANNED_BENEFIT_COST=BenefitItem.SELECT_ESTIMATED_BENEFIT;
					SELECT_PLANNED_BENEFIT_COST_UNIT = "attribute[" + ATTRIBUTE_ESTIMATED_BENEFIT + "].inputunit";
					SELECT_INTERVAL_PLANNED_AMOUNT=SELECT_BENEFIT_INTERVAL_ESTIMATED_BENEFIT;
					SELECT_INTERVAL_PLANNED_AMOUNT_UNIT= "from["+RELATIONSHIP_BENEFIT_ITEM_INTERVAL+"].attribute["+ATTRIBUTE_ESTIMATED_BENEFIT+"].inputunit";
					SELECT_ACTUAL_AMOUNT=BenefitItem.SELECT_ACTUAL_BENEFIT;
					SELECT_ACTUAL_AMOUNT_UNIT =  "attribute[" + ATTRIBUTE_ACTUAL_BENEFIT + "].inputunit";
					slBusSelect.add(SELECT_ACTUAL_AMOUNT);
					slBusSelect.add(SELECT_ACTUAL_AMOUNT_UNIT);
				}

				if(actualView.equals(selectedView)){
					SELECT_PLANNED_BENEFIT_COST=BenefitItem.SELECT_ACTUAL_BENEFIT;
					SELECT_PLANNED_BENEFIT_COST_UNIT = "attribute[" + ATTRIBUTE_ACTUAL_BENEFIT + "].inputunit";
					SELECT_INTERVAL_PLANNED_AMOUNT=SELECT_BENEFIT_INTERVAL_ACTUAL_BENEFIT;
					SELECT_INTERVAL_PLANNED_AMOUNT_UNIT= "from["+RELATIONSHIP_BENEFIT_ITEM_INTERVAL+"].attribute["+ATTRIBUTE_ACTUAL_BENEFIT+"].inputunit";
					SELECT_ACTUAL_AMOUNT=BenefitItem.SELECT_PLANNED_BENEFIT;
					SELECT_ACTUAL_AMOUNT_UNIT =  "attribute[" + ATTRIBUTE_PLANNED_BENEFIT + "].inputunit";
					slBusSelect.add(SELECT_ACTUAL_AMOUNT);
					slBusSelect.add(SELECT_ACTUAL_AMOUNT_UNIT);
				}
			}

			DomainObject finItem = DomainObject.newInstance(context, strFinancialItemId);
			StringList slFinItemInfo = new StringList(3);
			slFinItemInfo.add(SELECT_CURRENT);
			slFinItemInfo.add(SELECT_COST_INTERVAL_START_DATE);
			slFinItemInfo.add(SELECT_COST_INTERVAL_END_DATE);
			slFinItemInfo.add(SELECT_ATTRIBUTE_COST_INTERVAL);
			Map mFinItemInfo = finItem.getInfo(context,slFinItemInfo);
			String strInterval = (String) mFinItemInfo.get(SELECT_ATTRIBUTE_COST_INTERVAL);
			String current = (String)mFinItemInfo.get(SELECT_CURRENT);
			String strBudgetStartDate = (String)mFinItemInfo.get(SELECT_COST_INTERVAL_START_DATE);
			String strBudgetEndDate = (String)mFinItemInfo.get(SELECT_COST_INTERVAL_END_DATE);

			slBusSelect.add(SELECT_ID);
			slBusSelect.add(SELECT_CURRENT);
			slBusSelect.add(SELECT_TYPE);
			slBusSelect.add(SELECT_NAME);
			slBusSelect.add(SELECT_REVISION);
			if(isCostItem){
				slBusSelect.add(SELECT_PHASE_ID);

				///TEST
				slBusSelect.add("to[Actual Transaction Item].from.attribute[Transaction Date]");
				slBusSelect.add("to[Actual Transaction Item].from.attribute[Transaction Amount]");
				slBusSelect.add("to[Actual Transaction Item].from.attribute[Transaction Amount].inputunit");
			}
			slBusSelect.add(SELECT_PLANNED_BENEFIT_COST);
			if(isCostItem && actualView.equals(selectedView) && !"Project Phase".equals(strInterval)){
				SELECT_INTERVAL_PLANNED_AMOUNT = SELECT_COST_INTERVAL_PLANNED_COST;
				SELECT_INTERVAL_PLANNED_AMOUNT_UNIT = "from["+RELATIONSHIP_COST_ITEM_INTERVAL+"].attribute["+ATTRIBUTE_PLANNED_COST+"].inputunit";
			}

			if(!PROJECTTEMPLATETABLE.equals(strViewTableType)){
				slBusSelect.add(SELECT_PLANNED_BENEFIT_COST_UNIT);
				slBusSelect.add(SELECT_INTERVAL_PLANNED_AMOUNT_UNIT);
			}
			slBusSelect.add(SELECT_INTERVAL_PLANNED_AMOUNT);
			slBusSelect.add(SELECT_INTERVAL_DATE);
			StringList slRelSelect = new StringList();
			slRelSelect.add(DomainRelationship.SELECT_ID);

			boolean getTo = false;
			boolean getFrom = true;
			short recurseToLevel = 1;
			Map mapBenefit_Cost_Info=null;
			Map benefit_cost_CategoryMap = new HashMap();
			try{
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				mlItemList = finItem.getRelatedObjects(context,
						strRelationshipPattern, //pattern to match relationships
						strTypePattern, //pattern to match types
						slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
						slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
						getTo, //get To relationships
						getFrom, //get From relationships
						recurseToLevel, //the number of levels to expand, 0 equals expand all.
						DomainConstants.EMPTY_STRING, //where clause to apply to objects, can be empty ""
						DomainConstants.EMPTY_STRING, //where clause to apply to relationship, can be empty ""
						0);
			}catch(MatrixException e){
				throw new Exception(e);
			}finally{
				ContextUtil.popContext(context);
			}
			//
			// To separate the Total and Actual values
			//

			for (Iterator itrItems = mlItemList.iterator(); itrItems.hasNext();) {
				Map mapItem = (Map) itrItems.next();

				if("PMCProjectBudgetReportTable".equals(strViewTableType)){
					String baseUnit;
					String baseValue;
					double dblValue;
					if(mapItem.containsKey(SELECT_ACTUAL_AMOUNT)){
						baseValue = (String)mapItem.get(SELECT_ACTUAL_AMOUNT);
						baseUnit = (String)mapItem.get(SELECT_ACTUAL_AMOUNT_UNIT);
						dblValue = Task.parseToDouble(baseValue);
						dblValue = convertAmount(context, dblValue, baseUnit, strSelectedCurrency, new Date());
						baseValue = String.valueOf(dblValue);
						mapItem.put(SELECT_ACTUAL_AMOUNT, baseValue);
						mapItem.put(SELECT_ACTUAL_AMOUNT_UNIT, strSelectedCurrency);
					}
					if(mapItem.containsKey(SELECT_PLANNED_BENEFIT_COST)){
						baseValue = (String)mapItem.get(SELECT_PLANNED_BENEFIT_COST);
						baseUnit = (String)mapItem.get(SELECT_PLANNED_BENEFIT_COST_UNIT);
						dblValue = Task.parseToDouble(baseValue);
						dblValue = convertAmount(context, dblValue, baseUnit, strSelectedCurrency, new Date());
						baseValue = String.valueOf(dblValue);
						mapItem.put(SELECT_PLANNED_BENEFIT_COST, baseValue);
						mapItem.put(SELECT_PLANNED_BENEFIT_COST_UNIT, strSelectedCurrency);
					}
					if(mapItem.containsKey(SELECT_INTERVAL_PLANNED_AMOUNT)){
						StringList baseValues = new StringList();
						StringList baseUnits = new StringList();

						Object amountObject = mapItem.get(SELECT_INTERVAL_PLANNED_AMOUNT);
						Object unitObject = mapItem.get(SELECT_INTERVAL_PLANNED_AMOUNT_UNIT);
						if(amountObject instanceof StringList && unitObject instanceof StringList){
							baseValues = (StringList) amountObject;
							baseUnits = (StringList) unitObject;
							for(int index=0;index<baseValues.size();index++){
								baseValue = (String)baseValues.get(index);
								baseUnit = (String)baseUnits.get(index);
								dblValue = Task.parseToDouble(baseValue);
								dblValue =  convertAmount(context, dblValue, baseUnit, strSelectedCurrency, new Date());
								baseValue = String.valueOf(dblValue);
								baseUnit = strSelectedCurrency;
								baseValues.remove(index);
								baseValues.add(index,baseValue);
								baseUnits.remove(index);
								baseUnits.add(index,baseUnit);
							}
						}else if (amountObject instanceof String && unitObject instanceof String) {
							baseValues.add(amountObject);
							baseUnits.add(unitObject);
						}

						mapItem.put(SELECT_INTERVAL_PLANNED_AMOUNT,baseValues);
						mapItem.put(SELECT_INTERVAL_PLANNED_AMOUNT_UNIT,baseUnits);
					}
				}


				mapItem.put("Total", (String)mapItem.get(SELECT_PLANNED_BENEFIT_COST));
				if(!PROJECTTEMPLATETABLE.equals(strViewTableType)){
					mapItem.put("Total_Unit", (String)mapItem.get(SELECT_PLANNED_BENEFIT_COST_UNIT));
				}
				if(null!=selectedView && !selectedView.equalsIgnoreCase(planView)){
					mapItem.put("PlanOrActual", (String)mapItem.get(SELECT_ACTUAL_AMOUNT));
					if(!PROJECTTEMPLATETABLE.equals(strViewTableType)){
						mapItem.put("PlanOrActual_Unit", (String)mapItem.get(SELECT_ACTUAL_AMOUNT_UNIT));
					}
				}
			}

			if(!PROJECTBUDGETREPORTTABLE.equals(strViewTableType)){
				mlItemList = getCategoryItems(context, isCostItem, mlItemList);
			}else {
				mlItemList = getCategoryItems(context, isCostItem, mlItemList,true); // if it is expense report then pass isExpenseReportPage=true
			}

			//
			//to remove last map of expandMultiLevelJPO key until further processing
			//
			int lastIndex = mlItemList.size()-1;
			Map mpLastMap = (Map)mlItemList.remove(lastIndex);
			if(!mpLastMap.containsKey("expandMultiLevelsJPO")){
				mlItemList.add(mpLastMap);
				mpLastMap = null;
			}

			Map tmp=null;
			Map hmCategoryPlannedAttribute= new HashMap();
			MapList mlCopyItemList=new MapList();
			Map mpCopyItem=null;
			mlCopyItemList.addAll(mlItemList);
			if(!PROJECTTEMPLATETABLE.equals(strViewTableType)){
				//if(isCostItem)
				if("Project Phase".equals(strInterval)) {
					rollupAmounts(context, args, mlItemList, SELECT_PHASE_ID, SELECT_INTERVAL_PLANNED_AMOUNT, strInterval,current, isCostItem,SELECT_INTERVAL_PLANNED_AMOUNT_UNIT);
				}else {
					rollupAmounts(context, args, mlItemList, SELECT_INTERVAL_DATE, SELECT_INTERVAL_PLANNED_AMOUNT, strInterval,current,isCostItem,SELECT_INTERVAL_PLANNED_AMOUNT_UNIT);
				}
			}

			//If Person's preferred currency not equal to filter selected Currency then the cost items will not be editable
			if(null != strSelectedCurrency && !strSelectedCurrency.equals(strPrefferedCurrency)){
				for (Iterator itrItems1 = mlItemList.iterator(); itrItems1.hasNext();) {
					Map mapItemInfo1 = (Map) itrItems1.next();
					mapItemInfo1.put("RowEditable", "readonly");
				}
			}

			//This is to disable the Actual View Table for Budget
			if(isCostItem)
			{
				if(actualView.equals(selectedView)){
					for (Iterator itrItems = mlItemList.iterator(); itrItems.hasNext();) {
						Map mapItemInfo = (Map) itrItems.next();
						mapItemInfo.put("RowEditable", "readonly");
					}
				}else{
					for (Iterator itrItems = mlItemList.iterator(); itrItems.hasNext();) {
						Map mapItemInfo = (Map) itrItems.next();
						mapItemInfo.put(SELECT_COST_INTERVAL_START_DATE,strBudgetStartDate);
						mapItemInfo.put(SELECT_COST_INTERVAL_END_DATE,strBudgetEndDate);
					}
				}
			}

			//
			//to restore back expandMultiLevelJPO key
			//
			if(null != mpLastMap && null!=mlItemList ){
				mlItemList.add(mpLastMap);
			}
		}
		catch(Exception exp) {
			exp.printStackTrace();
		}
		return mlItemList;
	}

	/**
	 * Rolls up the amount values for all the items
	 *
	 * @param context The Matrix Context object
	 * @param mlItemList The list of Cost/Benefit items
	 * @param strIntervalKey The key against which the rolled up amount will be stored (Phase ID/Interval Date)
	 * @param strAmountKey The key to find the corresponding amount in map
	 * @param current2
	 */
	private void rollupAmounts(Context context, String[] args, MapList mlItemList, String strIntervalKey, String strAmountKey, String strInterval,String current, boolean iscostItem, String strIntervalAmountUnitKey ) throws MatrixException
	{
		try{
			//
			// Find out the root of hierarchy and get the amount rolled up
			//
			Map programMap = (Map)JPO.unpackArgs(args);
			for (Iterator itrItems = mlItemList.iterator(); itrItems.hasNext();) {
				Map mapItemInfo = (Map) itrItems.next();
				String strLevel = (String) mapItemInfo.get(SELECT_LEVEL);
				String strRelationship = (String)mapItemInfo.get(KEY_RELATIONSHIP);

				if (!ProgramCentralConstants.RELATIONSHIP_COMPANY_FINANCIAL_CATEGORIES.equals(strRelationship)) {
					continue;
				}

				MapList mlSubHierarchy = filterSubHierarchy(mapItemInfo, mlItemList);
				rollupAmounts(context,mapItemInfo, programMap, mlSubHierarchy, strIntervalKey, strAmountKey, strInterval,current, iscostItem,strIntervalAmountUnitKey);
			}
		}
		catch(Exception exp){
			exp.printStackTrace();
		}
	}


	/**
	 * Roll-up the amount for a items hierarchy
	 *
	 * @param mapRootInfo The root of the sub-hierarchy
	 * @param mlSubHierarchy The indented list of children items
	 * @param strIntervalKey The key against which the rolled up amount will be stored (Phase ID/Interval Date)
	 * @param strAmountKey The key to find the corresponding amount in map
	 * @param strIntervalAmountUnitKey
	 */
	private void rollupAmounts(Context context, Map mapRootInfo, Map programMap, MapList mlSubHierarchy, String strIntervalKey, String strAmountKey, String strIntervalType,String current, boolean isCostItem, String strIntervalAmountUnitKey) throws MatrixException {
		try{
			String selectedView = "";
			String strPreferredCurrency = "";
			String numberOfFiscalYears = "";
			String fiscalYear  = "";

			//Set Plan/Estimated/Actual view and Preferred Currency.
			if(isCostItem){
				String tableName = (String)programMap.get("selectedTable");
				String displayViewMode = (String) programMap.get(BUDGETDISPLAYVIEWFILTER);
				numberOfFiscalYears = (String) programMap.get(BUDGETINTERVALFILTER);
				fiscalYear = (String) programMap.get(BUDGETFISCALYEARFILTER);
				if(tableName.equals("PMCProjectBudgetReportTable")){
					selectedView = actualView;
					strPreferredCurrency = (String)programMap.get(BUDGETREPORTCURRENCYFILTER);
					fiscalYear = (String) programMap.get("PMCProjectBudgetReportFiscalYearFilter");
					displayViewMode = (String) programMap.get("PMCProjectBudgetReportDisplayViewFilter");
					numberOfFiscalYears = (String) programMap.get("PMCProjectBudgetReportIntervalFilter");
				}else{
					selectedView = (String)programMap.get(BUDGETVIEWFILTER);
					strPreferredCurrency = (String)programMap.get(BUDGETCURRENCYFILTER);
				}
			} else{
				selectedView = (String)programMap.get(BENEFITVIEWFILTER);
				strPreferredCurrency = (String)programMap.get(BENEFITCURRENCYFILTER);
			}
			if(ProgramCentralUtil.isNullString(strPreferredCurrency)){
				strPreferredCurrency = emxProgramCentralUtilBase_mxJPO.getUserPreferenceCurrency(context, "emxProjectBudget.Error.NoDefaultCurrencyDefined");
			}

			if(isCostItem && (!ProgramCentralConstants.PROJECT_PHASE.equals(strIntervalType))){
				List<Integer> validFiscals = new ArrayList<Integer>();
				if(ProgramCentralUtil.isNullString(fiscalYear)){
					emxProjectBudgetBase_mxJPO budget = new emxProjectBudgetBase_mxJPO(context, null);
					Map requestMap = new HashMap();
					Map localProgramMap = new HashMap(programMap);
					String parentOID = (String) localProgramMap.get("parentOID");
					localProgramMap.put("objectId", parentOID);
					requestMap.put("requestMap", localProgramMap);
					Map fiscalMap = budget.getBudgetFiscalReportFilterRange(context, JPO.packArgs(requestMap));
					StringList slFiscals = (StringList) fiscalMap.get("field_choices");
					fiscalYear = (String) slFiscals.get(0);
				}
				//if("FiscalYear".equals(displayViewMode)){
				StringList slFiscals = FrameworkUtil.split(numberOfFiscalYears, ProgramCentralConstants.SPACE);
				String strNumberOfFiscals = (String) slFiscals.get(0);
				int numberOfFiscals = Integer.parseInt(strNumberOfFiscals);
				int validYear = Integer.parseInt(fiscalYear);
				for(int index=0; index<numberOfFiscals; index++){
					validFiscals.add(validYear);
					validYear += 1;
				}
				//}

				String sPolicyFinancial = PropertyUtil.getSchemaProperty(context,"policy_FinancialItems");
				String sPlanFrozenStateName = PropertyUtil.getSchemaProperty(context,"policy",sPolicyFinancial,"state_PlanFrozen");
				String strViewTableType = (String)programMap.get("selectedTable");
				int nRootLevel = Integer.parseInt((String)mapRootInfo.get(SELECT_LEVEL));
				int nChildrenLevel = nRootLevel + 1;
				for (Iterator itrChildrenItems = mlSubHierarchy.iterator(); itrChildrenItems.hasNext();) {
					Map mapChildItem = (Map) itrChildrenItems.next();
					String fItemId = (String)mapChildItem.get(SELECT_ID);
					int nChildLevel = Integer.parseInt((String)mapChildItem.get(SELECT_LEVEL));

					MapList mlChildSubHierarchy = filterSubHierarchy(mapChildItem, mlSubHierarchy);
					if (mlChildSubHierarchy.size() != 0) {
						rollupAmounts(context,mapChildItem, programMap, mlChildSubHierarchy, strIntervalKey, strAmountKey, strIntervalType, current, isCostItem,strIntervalAmountUnitKey);
					}
					Object objRootIntervals = mapRootInfo.get(strIntervalKey);
					Object objChildIntervals = mapChildItem.get(strIntervalKey);
					Object objChildAmounts   = mapChildItem.get(strAmountKey);
					Object objChildAmountsUnits   = mapChildItem.get(strIntervalAmountUnitKey);
					if (objChildIntervals == null) {
						continue;
					}

					StringList slChildIntervals = new StringList();
					StringList slChildAmounts = new StringList();
					StringList slChildAmountsUnits = new StringList();
					double actualTotal = 0D;
					double estimatedTotal = 0D;
					double plannedTotal = 0D;
					if (objChildIntervals instanceof String) {
						slChildIntervals.add((String)objChildIntervals);
					}else if (objChildIntervals instanceof StringList) {
						slChildIntervals = (StringList)objChildIntervals;
					}else {
						throw new MatrixException("Invalid data for processing");
					}

					//if(!actualView.equals(selectedView) && slChildAmounts!=null){
					if(objChildAmounts instanceof StringList){
						slChildAmounts = (StringList)objChildAmounts;
						slChildAmountsUnits =(StringList)objChildAmountsUnits;
					}else if (objChildAmounts instanceof String) {
						slChildAmounts.clear();
						slChildAmountsUnits.clear();
						slChildAmounts.add((String)objChildAmounts);
						slChildAmountsUnits.add((String)objChildAmountsUnits);
					}
					else {
						throw new MatrixException("Invalid data for processing");
					}
					//}

					//Filter out intervals falling in invalid fiscals.
					StringList slValidIntervals = new StringList(slChildIntervals.size());
					StringList slValidFinancialValues = new StringList(slChildAmounts.size());
					StringList slValidFinancialUnitValues = new StringList(slChildAmountsUnits.size());
					//for (Iterator iterator = slIntervalDateValues.iterator(); iterator.hasNext();) {
					for (int index = 0; index< slChildIntervals.size(); index++) {
						String strIntervalDate = (String) slChildIntervals.get(index);
						Date intervalDate = eMatrixDateFormat.getJavaDate(strIntervalDate);
						int year = Financials.getFiscalYear(intervalDate);
						if(validFiscals.contains(year)){
							slValidIntervals.add(strIntervalDate);
							//if(!(actualView.equals(selectedView))){
							slValidFinancialValues.add(slChildAmounts.get(index));
							slValidFinancialUnitValues.add(slChildAmountsUnits.get(index));
							//}
						}
					}
					//Only Valid Intervals
					if(!slValidIntervals.isEmpty()){
						slChildIntervals.clear();
						slChildIntervals = slValidIntervals;
					}
					//Only Valid Fiancial Values
					if(!slValidFinancialValues.isEmpty()){
						slChildAmounts.clear();
						slChildAmounts = slValidFinancialValues;
					}
					//Only Valid Fiancial Units
					if(!slValidFinancialUnitValues.isEmpty()){
						slChildAmountsUnits.clear();
						slChildAmountsUnits = slValidFinancialUnitValues;
					}

					if(actualView.equals(selectedView) && slChildIntervals !=null){
						String strIntervalValue;
						Date intervalDate;
						for(int cnt=0; cnt<slChildAmounts.size(); cnt++){
							strIntervalValue = (String)slChildIntervals.get(cnt);
							intervalDate = eMatrixDateFormat.getJavaDate(strIntervalValue);
							String financialvalue = (String) slChildAmounts.get(cnt);
							String financialUnit = (String) slChildAmountsUnits.get(cnt);
							plannedTotal += convertAmount(context, Task.parseToDouble(financialvalue), financialUnit, strPreferredCurrency, new Date());
						}
						slChildAmounts.clear();
						slChildAmountsUnits.clear();
						for(int cnt=0;cnt<slChildIntervals.size();cnt++){
							strIntervalValue = (String)slChildIntervals.get(cnt);
							intervalDate = eMatrixDateFormat.getJavaDate(strIntervalValue);
							Double totalIntervalActualCost = getIntervalTotalActualCost(context,fItemId,strIntervalType,intervalDate,strPreferredCurrency);
							slChildAmounts.add(totalIntervalActualCost.toString());
							slChildAmountsUnits.add(strPreferredCurrency);
						}
						actualTotal = Task.parseToDouble(FrameworkUtil.getSum(context,context.getLocale(), slChildAmounts));
					}
					else if(estimateView.equals(selectedView) && slChildIntervals !=null){
						String strIntervalValue;
						Date intervalDate;
						for(int cnt=0; cnt<slChildIntervals.size(); cnt++){
							strIntervalValue = (String)slChildIntervals.get(cnt);
							intervalDate = eMatrixDateFormat.getJavaDate(strIntervalValue);
							Double totalIntervalActualCost = getIntervalTotalActualCost(context,fItemId,strIntervalType,intervalDate,strPreferredCurrency);
							actualTotal += totalIntervalActualCost;
							String financialvalue = (String) slChildAmounts.get(cnt);
							String financialUnit = (String) slChildAmountsUnits.get(cnt);
							estimatedTotal += convertAmount(context, Task.parseToDouble(financialvalue ), financialUnit, strPreferredCurrency, new Date());
						}
					}
					else if(planView.equals(selectedView) && slChildIntervals !=null){
						String strIntervalValue;
						Date intervalDate;
						for(int cnt=0; cnt<slChildIntervals.size(); cnt++){
							strIntervalValue = (String)slChildIntervals.get(cnt);
							intervalDate = eMatrixDateFormat.getJavaDate(strIntervalValue);
							String financialvalue = (String) slChildAmounts.get(cnt);
							String financialUnit = (String) slChildAmountsUnits.get(cnt);
							plannedTotal += convertAmount(context, Task.parseToDouble(financialvalue ), financialUnit, strPreferredCurrency, new Date());
						}
					}

					int size = slChildIntervals.size();
					for (int i = 0; i < size; i++) {
						String strInterval = (String)slChildIntervals.get(i);
						String strKey = strInterval;
						if(!CostItem.PROJECT_PHASE.equals(strIntervalType)){
							strInterval=strInterval.substring(0,strInterval.indexOf(" "));//TODO
							strKey = strInterval;
						}
						else{
							if("PMCProjectBudgetReportTable".equals(strViewTableType)){
								DomainObject dmoObj = DomainObject.newInstance(context, strInterval);
								if(dmoObj.isKindOf(context, TYPE_PHASE)){
									strKey = "Phase_"+strInterval;
								}
							}else{
								strKey = strInterval;
							}
						}

						double dblChildAmount = Task.parseToDouble((String)slChildAmounts.get(i));
						String strChildAmountUnit = (String)slChildAmountsUnits.get(i);
						if(!strChildAmountUnit.equalsIgnoreCase(strPreferredCurrency)){
							dblChildAmount = convertAmount(context, dblChildAmount, strChildAmountUnit, strPreferredCurrency, new Date());
							strChildAmountUnit = strPreferredCurrency;
						}
						//
						// Update child map
						//
						mapChildItem.put(strInterval, String.valueOf(dblChildAmount));
						mapChildItem.put(strKey, String.valueOf(dblChildAmount));
						mapChildItem.put(strInterval+"_Unit",strChildAmountUnit);
						if(sPlanFrozenStateName.equals(current)&& planView.equalsIgnoreCase(selectedView)) {
							mapChildItem.put("RowEditable","readonly");
						}
						rollupAmount(context, mapRootInfo, mapChildItem, strInterval, strInterval,strIntervalAmountUnitKey,strPreferredCurrency);
					}

					if(actualView.equals(selectedView) ){
						mapChildItem.put("Total", String.valueOf(actualTotal));
						mapChildItem.put("Total_Unit",strPreferredCurrency);
						mapChildItem.put("PlanOrActual", String.valueOf(plannedTotal));
						mapChildItem.put("PlanOrActual_Unit",strPreferredCurrency);
					}
					else if(planView.equals(selectedView) ){
						mapChildItem.put("Total", String.valueOf(plannedTotal));
						mapChildItem.put("Total_Unit",strPreferredCurrency);
					}
					else if(estimateView.equals(selectedView) ){
						mapChildItem.put("Total", String.valueOf(estimatedTotal));
						mapChildItem.put("Total_Unit",strPreferredCurrency);
						mapChildItem.put("PlanOrActual", String.valueOf(actualTotal));
						mapChildItem.put("PlanOrActual_Unit",strPreferredCurrency);
					}
					rollupAmount(context, mapRootInfo, mapChildItem, "Total", "Total",strIntervalAmountUnitKey,strPreferredCurrency);
					if(!planView.equalsIgnoreCase(selectedView)){
						rollupAmount(context, mapRootInfo, mapChildItem, "PlanOrActual", "PlanOrActual",strIntervalAmountUnitKey,strPreferredCurrency);
						calculateVariance(mapChildItem, "Total", "PlanOrActual", "VarianceAmount", "VariancePercent", isCostItem, selectedView);
					}
				}//For
				calculateVariance(mapRootInfo, "Total", "PlanOrActual", "VarianceAmount", "VariancePercent", isCostItem, selectedView);
			}else{
				String sPolicyFinancial = PropertyUtil.getSchemaProperty(context,"policy_FinancialItems");
				String sPlanFrozenStateName = PropertyUtil.getSchemaProperty(context,"policy",sPolicyFinancial,"state_PlanFrozen");
				String strViewTableType = (String)programMap.get("selectedTable");
				int nRootLevel = Integer.parseInt((String)mapRootInfo.get(SELECT_LEVEL));
				int nChildrenLevel = nRootLevel + 1;
				for (Iterator itrChildrenItems = mlSubHierarchy.iterator(); itrChildrenItems.hasNext();) {
					Map mapChildItem = (Map) itrChildrenItems.next();

					int nChildLevel = Integer.parseInt((String)mapChildItem.get(SELECT_LEVEL));

					MapList mlChildSubHierarchy = filterSubHierarchy(mapChildItem, mlSubHierarchy);
					if (mlChildSubHierarchy.size() != 0) {
						rollupAmounts(context,mapChildItem, programMap, mlChildSubHierarchy, strIntervalKey, strAmountKey, strIntervalType, current, isCostItem,strIntervalAmountUnitKey);
					}

					Object objRootIntervals = mapRootInfo.get(strIntervalKey);
					Object objChildIntervals = mapChildItem.get(strIntervalKey);
					Object objChildAmounts   = mapChildItem.get(strAmountKey);
					Object objChildAmountsUnits   = mapChildItem.get(strIntervalAmountUnitKey);
					if (objChildIntervals == null) {
						continue;
					}

					StringList slChildIntervals = new StringList();
					StringList slChildAmounts = new StringList();
					StringList slChildAmountsUnits = new StringList();
					if (objChildIntervals instanceof String) {
						slChildIntervals.add((String)objChildIntervals);
						slChildAmounts.add((String)objChildAmounts);
						slChildAmountsUnits.add((String)objChildAmountsUnits);
					}
					else if (objChildIntervals instanceof StringList) {
						slChildIntervals = (StringList)objChildIntervals;
						slChildAmounts = (StringList)objChildAmounts;
						slChildAmountsUnits =(StringList)objChildAmountsUnits;
					}
					else {
						throw new MatrixException("Invalid data for processing");
					}

					int size = slChildIntervals.size();
					for (int i = 0; i < size; i++) {
						String strInterval = (String)slChildIntervals.get(i);
						String strKey = strInterval;
						if(!CostItem.PROJECT_PHASE.equals(strIntervalType)){
							strInterval=strInterval.substring(0,strInterval.indexOf(" "));//TODO
							strKey = strInterval;
						}
						else{
							if("PMCProjectBudgetReportTable".equals(strViewTableType)){
								DomainObject dmoObj = DomainObject.newInstance(context, strInterval);
								if(dmoObj.isKindOf(context, TYPE_PHASE)){
									strKey = "Phase_"+strInterval;
								}
							}else{
								strKey = strInterval;
							}
						}

						double dblChildAmount = Task.parseToDouble((String)slChildAmounts.get(i));

						//
						// Update child map
						//
						mapChildItem.put(strInterval, String.valueOf(dblChildAmount));
						mapChildItem.put(strKey, String.valueOf(dblChildAmount));
						mapChildItem.put(strInterval+"_Unit", (String)slChildAmountsUnits.get(i));

						if(sPlanFrozenStateName.equals(current)&& planView.equalsIgnoreCase(selectedView)) {
							mapChildItem.put("RowEditable","readonly");
						}
						rollupAmount(context, mapRootInfo, mapChildItem, strInterval, strInterval,strIntervalAmountUnitKey,strPreferredCurrency);
					}

					//
					// Rollup of Total
					//
					rollupAmount(context, mapRootInfo, mapChildItem, "Total", "Total",strIntervalAmountUnitKey,strPreferredCurrency);

					//
					// Rollup of Actual Amount
					//

					if(!planView.equalsIgnoreCase(selectedView))
					{
						rollupAmount(context, mapRootInfo, mapChildItem, "PlanOrActual", "PlanOrActual",strIntervalAmountUnitKey,strPreferredCurrency);
						calculateVariance(mapChildItem, "Total", "PlanOrActual", "VarianceAmount", "VariancePercent", isCostItem, selectedView);
					}
				}//For

				calculateVariance(mapRootInfo, "Total", "PlanOrActual", "VarianceAmount", "VariancePercent", isCostItem, selectedView);
			}
		}
		catch(Exception exp) {
			exp.printStackTrace();
			throw new MatrixException(exp);
		}
	}

	/**
	 * Calculates the variance amount and variance percentage using the actual and estimated values
	 *
	 * @param mapItemInfo The map of financial item information
	 * @param strTotalAmountKey The key to be used to get the total amount from map
	 * @param strActualAmountKey The key to be used to get the actual amount from map
	 * @param strVarianceAmountKey The key to be used to put the variance amount in map
	 * @param strVariancePercentKey The key to be used to put the variance amount % in map
	 */
	protected void calculateVariance(Map mapItemInfo, String strTotalAmountKey,
			String strActualAmountKey, String strVarianceAmountKey, String strVariancePercentKey, boolean iscostItem, String selectedView) {
		if (mapItemInfo == null) {
			return;
		}
		DecimalFormat twoDForm = new DecimalFormat("#.##",new DecimalFormatSymbols(Locale.US));
		double dblTotalAmount = 0;
		String strTotalAmountUnit = null;
		if (mapItemInfo.get(strTotalAmountKey) != null) {
			dblTotalAmount = Task.parseToDouble((String)mapItemInfo.get(strTotalAmountKey));
			strTotalAmountUnit = (String)mapItemInfo.get(strTotalAmountKey+"_Unit");
		}
		double dblActualAmount = 0;
		if (mapItemInfo.get(strActualAmountKey) != null) {
			dblActualAmount = Task.parseToDouble((String)mapItemInfo.get(strActualAmountKey));
		}
		double dblVarianceAmount = 0;
		double dblVariancePercent = 0;
		if(estimateView.equals(selectedView)){
			dblVarianceAmount = (dblActualAmount - dblTotalAmount);
			if(dblTotalAmount != 0){
				dblVariancePercent = dblActualAmount * 100 / dblTotalAmount;
				dblVariancePercent=(Double)Task.parseToDouble(twoDForm.format(dblVariancePercent));
			}
		}

		if(actualView.equals(selectedView)){
			dblVarianceAmount = (dblTotalAmount - dblActualAmount);
			if(dblActualAmount != 0){
				dblVariancePercent = dblTotalAmount * 100 / dblActualAmount;
				dblVariancePercent=(Double)Task.parseToDouble(twoDForm.format(dblVariancePercent));
			}
		}



		mapItemInfo.put(strVarianceAmountKey, String.valueOf(dblVarianceAmount));
		mapItemInfo.put(strVarianceAmountKey+"_Unit", strTotalAmountUnit);
		if(estimateView.equals(selectedView)){
			if(dblTotalAmount != 0){
				mapItemInfo.put(strVariancePercentKey,String.valueOf(dblVariancePercent));
			}else{
				mapItemInfo.put(strVariancePercentKey, "NA");
			}
		}

		if(actualView.equals(selectedView)){
			if(dblActualAmount != 0){
				mapItemInfo.put(strVariancePercentKey,String.valueOf(dblVariancePercent));
			}else{
				mapItemInfo.put(strVariancePercentKey, "NA");
			}
		}
	}

	/**
	 * Rolls up the amount values from child map to parent map
	 *
	 * @param mapParent The parent map
	 * @param mapChild The child map
	 * @param strInputAmountKey The key to be used to get the amount to be rolled up
	 * @param strOutputAmountKey The key to be used to put the amount & rolledup amount against
	 * @param strIntervalAmountUnitKey
	 */
	protected void rollupAmount(Context context, Map mapParent, Map mapChild, String strInputAmountKey, String strOutputAmountKey, String strIntervalAmountUnitKey, String strPreferredCurrency) throws Exception {
		String strTotalAmount = (String)mapChild.get(strInputAmountKey);
		String strChildUnit = (String)mapChild.get(strInputAmountKey+"_Unit");
		double dblChildAmount = 0D;
		if (strTotalAmount == null || strChildUnit == null) {
			strTotalAmount =  String.valueOf(dblChildAmount);
			mapChild.put(strInputAmountKey, strTotalAmount);
			mapChild.put(strInputAmountKey+"_Unit", strPreferredCurrency);
		}else{
			dblChildAmount = Task.parseToDouble(strTotalAmount);
			dblChildAmount =  convertAmount(context, dblChildAmount, strChildUnit, strPreferredCurrency, new Date());
			strTotalAmount = String.valueOf(dblChildAmount);
			mapChild.put(strOutputAmountKey, strTotalAmount);
			mapChild.put(strInputAmountKey+"_Unit", strPreferredCurrency);
		}

		String strParentTotalAmount = (String)mapParent.get(strInputAmountKey);
		String strParentTotalAmountUnit = (String)mapParent.get(strIntervalAmountUnitKey);
		double dblParentTotalAmount = 0D;
		if (strParentTotalAmount == null || strParentTotalAmountUnit == null) {
			strParentTotalAmount = "0.0";
			mapParent.put(strInputAmountKey, strParentTotalAmount);
			mapParent.put(strIntervalAmountUnitKey, strPreferredCurrency);
		}else{
			dblParentTotalAmount = Task.parseToDouble(strParentTotalAmount);
			dblParentTotalAmount =  convertAmount(context, dblParentTotalAmount, strParentTotalAmountUnit, strPreferredCurrency, new Date());
		}

		double dblTotalAmount = Task.parseToDouble(strTotalAmount);
		dblParentTotalAmount += dblTotalAmount;

		mapParent.put(strOutputAmountKey, String.valueOf(dblParentTotalAmount));
		//
		//This is to have unit of the amount
		//
		mapParent.put(strOutputAmountKey+"_Unit", strPreferredCurrency);
	}
	/**
	 * Get the selected currency converted amount
	 * @param context
	 * @param dblAmount
	 * @param strSrcUnit
	 * @param strDestUnit
	 * @param date
	 * @return
	 * @throws Exception
	 */
	protected double convertAmount(Context context, double dblAmount, String strSrcUnit, String strDestUnit, Date date) throws Exception {
		return convertAmount(context, dblAmount, strSrcUnit, strDestUnit, date, false);
	}

	/**
	 * Converts amount on the basis of preferred currency and defined exchange rated.
	 * @param context the enovia <code>Context</code> object.
	 * @param dblAmount amount in double format to be converted for the preffered currency.
	 * @param strSrcUnit Current currency unit.
	 * @param strDestUnit Currency unit in which amount is to be converted.
	 * @param date The <code>Date</code> object
	 * @param throwExpOnNoRate a boolean value. This is to check if desired exchange rate is defined.
	 * @return A converted amount in double format.
	 * @throws Exception if operation on ProgramCentralUtil fails, or
	 * if i18n operation fails.
	 */
	protected double convertAmount(Context context, double dblAmount, String strSrcUnit, String strDestUnit, Date date, boolean throwExpOnNoRate) throws Exception
	{
		//return Currency.toDisplayCurrency(context, dblAmount, strSrcUnit, strDestUnit, date, throwExpOnNoRate);
		return Currency.convert(context, dblAmount, strSrcUnit, strDestUnit, date, throwExpOnNoRate);
	}


	private MapList getCompanyCurrencyList(Context context) throws Exception{
		//Code to get Company Id of the person.
		String sCommandStatement = "temp query bus $1 $2 $3 select $4 dump $5";
		String strPersonId =  MqlUtil.mqlCommand(context, sCommandStatement,TYPE_PERSON, context.getUser(),"*","id","|");

		PersonUtil person = new PersonUtil();
		String strPrefferedCurrency = PersonUtil.getCurrency(context);
		StringList slResult = FrameworkUtil.splitString(strPersonId, "|");
		strPersonId = (String)slResult.lastElement();

		Person contextPerson = new Person();
		contextPerson.newInstance(context);
		contextPerson.setId(strPersonId);

		String SELECT_COMPANY = "to["+DomainConstants.RELATIONSHIP_EMPLOYEE+"].from.id";
		String strCompanyId = (String)contextPerson.getInfo(context, SELECT_COMPANY);
		//End
		Map mp = new HashMap();
		mp.put("objectId", strCompanyId);

		String[] arrJPOArguments = new String[1];
		arrJPOArguments = JPO.packArgs(mp);
		MapList mlCurrencies = (MapList)JPO.invoke(context,
				"emxCurrencyConversionBase", null, "getCurrencyExchangeRate",
				arrJPOArguments, MapList.class);

		return mlCurrencies;
	}

	/**
	 * To filter out the information for the items hierarchy starting from the
	 * provided root of the hierarchy
	 *
	 * @param mapRootInfo The root element of the hierarchy
	 * @param mlItemList All items in all hierarchy
	 * @return The filtered list of maps pertaining to the hierarchy of the passed root item
	 */

	private MapList filterSubHierarchy(Map mapRootInfo, MapList mlItemList)
	{
		MapList mlFilteredList = new MapList();
		String strRootLevel = (String)mapRootInfo.get(SELECT_LEVEL);

		for (Iterator itrItems = mlItemList.iterator(); itrItems.hasNext();) {
			Map mapCurrItem = (Map) itrItems.next();
			if (mapCurrItem.equals(mapRootInfo)) {

				//mlFilteredList.add(mapRootInfo);

				while (itrItems.hasNext()) {
					mapCurrItem = (Map) itrItems.next();
					String strLevel = (String) mapCurrItem.get(SELECT_LEVEL);

					if (!strRootLevel.equals(strLevel)) {
						mlFilteredList.add(mapCurrItem);
					}
					else {
						break;
					}
				}

				return mlFilteredList;
			}
		}

		return mlFilteredList;
	}

	/**
	 * This method returns the list of all category items connected to the budget.
	 * @param context
	 * @param isCostItem
	 * @param mlFinancialItemList
	 * @return
	 * @throws MatrixException
	 */
	public MapList getCategoryItems(Context context,boolean isCostItem,MapList mlFinancialItemList) throws MatrixException
	{
		try{
			return getCategoryItems(context,isCostItem,mlFinancialItemList,false);
		}
		catch(Exception exp)
		{
			throw new MatrixException(exp);
		}
	}

	/**
	 * This method returns the list of all category items connected to the budget.
	 * @param context
	 * @param isCostItem
	 * @param mlFinancialItemList
	 * @param isExpenseReportPage
	 * @return
	 * @throws MatrixException
	 */
	public MapList getCategoryItems(Context context,boolean isCostItem,MapList mlFinancialItemList,boolean isExpenseReportPage) throws MatrixException
	{
		try{
			MapList companyCategoryTree = getCompanyFinancialCategoriesTree(context, isCostItem);
			MapList mlResult = null;
			if(!isExpenseReportPage){
				mlResult = getAvailableChildrenStructure(mlFinancialItemList, companyCategoryTree);
			}else {
				mlResult = getAvailableChildrenStructure(mlFinancialItemList, companyCategoryTree,isExpenseReportPage);
			}

			Map hmTemp = new HashMap();
			hmTemp.put("expandMultiLevelsJPO","true");
			mlResult.add(hmTemp);
			return mlResult;
		}
		catch(Exception exp)
		{
			throw new MatrixException(exp);
		}
	}

	/**
	 * Updates the financial items maplist with the parent information and also returns the list of identified parents
	 *
	 * @param mlFinancialItemList The list of financial items (cost/benefit) of a project space/project template
	 * @param companyCategoryTree The Financial Cost/Benefit Categories for the company used as reference to find
	 * parents
	 * @return The list of parent categories found for the provided financial items
	 */
	protected MapList getAvailableChildrenStructure(MapList mlFinancialItemList, MapList companyCategoryTree) {
		return getAvailableChildrenStructure(mlFinancialItemList,companyCategoryTree,false);
	}

	/**
	 * Updates the financial items maplist with the parent information and also returns the list of identified parents
	 *
	 * @param mlFinancialItemList The list of financial items (cost/benefit) of a project space/project template
	 * @param companyCategoryTree The Financial Cost/Benefit Categories for the company used as reference to find
	 * parents
	 * @param isExpenseReportPage This value will be true for expense report otherwise false
	 * @return The list of parent categories found for the provided financial items
	 */
	protected MapList getAvailableChildrenStructure(MapList mlFinancialItemList, MapList companyCategoryTree,boolean isExpenseReportPage) {

		MapList mlResult = new MapList();

		for(Iterator itrCompanyCategory = companyCategoryTree.iterator(); itrCompanyCategory .hasNext();)
		{
			Map mapCategoryItem = (Map) itrCompanyCategory.next();

			MapList mlPartialChildFinItemsFound = getAvailableChildrenStructure(mlFinancialItemList, mapCategoryItem);

			if (mlPartialChildFinItemsFound != null && mlPartialChildFinItemsFound.size() > 0) {
				mlResult.addAll(mlPartialChildFinItemsFound);
			}
		}
		String strLevel = "2";
		// If it is expense report page then adjust the levels
		if(isExpenseReportPage){
			strLevel = "1";
		}
		// Adjust Levels
		for (Iterator itrResult = mlResult.iterator(); itrResult.hasNext();) {
			Map mapResult = (Map) itrResult.next();
			Map mapParent = (Map)mapResult.get("Parent");
			if (mapParent == null) {
				mapResult.put("level", strLevel);
				mapResult.put("disableSelection", "true");
				mapResult.put("RowEditable", "readonly");
			}
			else {
				int nParentLevel = Integer.parseInt((String)mapParent.get("level"));
				mapResult.put("level", String.valueOf(nParentLevel + 1));
			}
		}

		return mlResult;
	}

	/**
	 * Finds out the children (of mapCat) list if it is available in provided mlFinancialItemList
	 *
	 * @param mlFinancialItemList The financial items connected to project space / template
	 * @param mapCat The category item to be checked if its any of the children are part of mlFinancialItemList
	 * @return MapList containing children list with parent as first item in maplist (is mapCat is not item but parent)
	 */
	private MapList getAvailableChildrenStructure(MapList mlFinancialItemList, Map mapCat) {
		MapList mlResult = new MapList();
		if (mapCat == null) {
			return mlResult;
		}

		MapList mlChildren = (MapList)mapCat.get("Children");
		if (mlChildren == null || mlChildren.size() == 0) {
			String strCatName = (String)mapCat.get(DomainConstants.SELECT_NAME);
			Map mapCatParent = (Map)mapCat.get("Parent");

			for (Iterator itrFinItems = mlFinancialItemList.iterator(); itrFinItems.hasNext();) {
				Map mapFinItem = (Map) itrFinItems.next();
				String strFinItemName = (String)mapFinItem.get(DomainConstants.SELECT_NAME);

				// Try to find the name of the parent from revision
				// if this is not there that means it is legacy data and we need to handle
				// it that way.
				String strFinItemRev = (String)mapFinItem.get(DomainConstants.SELECT_REVISION);
				String strFinItemParentName = null;
				int index = strFinItemRev.indexOf("-");
				if (index != -1) {
					strFinItemParentName = strFinItemRev.substring(index+1, strFinItemRev.length());
				}

				//
				// Decide if we need to update fin item map with parent information
				//
				if (strFinItemName != null && strFinItemName.equals(strCatName)) {
					boolean updateParent = false;

					if (strFinItemParentName == null) {
						updateParent = true;
					}
					else {
						String strParentName = (String)mapCatParent.get(SELECT_NAME);
						if (strFinItemParentName.equals(strParentName)) {
							updateParent = true;
						}
					}

					if (updateParent) {
						mapFinItem.put("hasChildren", "false");
						mapFinItem.put("Parent", mapCatParent);
						mlResult.add(mapFinItem);
						break;
					}
				}
			}
		}
		else {
			boolean isChildrenFound = false;
			for (Iterator itrChildren = mlChildren.iterator(); itrChildren.hasNext();) {
				Map mapChild = (Map) itrChildren.next();
				MapList mlPartialChildFinItemsFound = getAvailableChildrenStructure(mlFinancialItemList, mapChild);

				if (mlPartialChildFinItemsFound != null && mlPartialChildFinItemsFound.size() > 0) {
					isChildrenFound = true;
					mlResult.addAll(mlPartialChildFinItemsFound);
				}
			}

			if(isChildrenFound) {
				mlResult.add(0, mapCat);
			}
		}

		return mlResult;
	}

	/**
	 * Retrives the entire cost/benefit categories for the context user's company.
	 *
	 * @param contextThe Matrix Context
	 * @param isCostItem if true the cost categories else benefit categories will be returned
	 * @return MapList of the financial categories. each map will have at least following information
	 *         Key "Parent" will point to map of the parent category
	 *         Key "Children" will point to MapList of children categories
	 *         Key DomainConstants.SELECT_NAME will have the name of the category
	 * @throws FrameworkException
	 */
	protected MapList getCompanyFinancialCategoriesTree(Context context,
			boolean isCostItem) throws FrameworkException {
		//
		// Find out the Financial Items category list defined for this company
		//
		StringList busSelects = new StringList();
		busSelects.add(FinancialTemplateCategory.SELECT_ID);
		busSelects.add(FinancialTemplateCategory.SELECT_NAME);
		busSelects.add(FinancialTemplateCategory.SELECT_TYPE);

		MapList companyCategoryList = null;
		if (isCostItem)
		{
			companyCategoryList = FinancialTemplateCategory.getCostCategories(context, 0, busSelects, null);
		}
		else
		{
			companyCategoryList = FinancialTemplateCategory.getBenefitCategories(context, 0, busSelects, null);
		}

		//
		// Arrange this MapList such that each category map will have
		// key Children to point to MapList of children category
		// key Parent to point to Map of parent category
		//
		Stack stParent = new Stack();
		for (Iterator itrCategory = companyCategoryList.iterator(); itrCategory .hasNext();)
		{
			Map mapChild = (Map) itrCategory.next();

			int nLevel = Integer.parseInt((String)mapChild.get("level"));
			if (!stParent.isEmpty())
			{
				Map mapParent = (Map)stParent.peek();
				int nParentLevel = Integer.parseInt((String)mapParent.get(DomainConstants.SELECT_LEVEL));
				if (nLevel <= nParentLevel)
				{
					while ((nLevel <= nParentLevel) && !stParent.isEmpty())
					{
						stParent.pop();

						if (stParent.isEmpty())
						{
							break;
						}
						else
						{
							mapParent = (Map)stParent.peek();
							nParentLevel = Integer.parseInt((String)mapParent.get(DomainConstants.SELECT_LEVEL));
						}
					}

					if (stParent.isEmpty())
					{
						mapParent = null;
						nParentLevel = 0;
					}
					else
					{
						mapParent = (Map)stParent.peek();
						nParentLevel = Integer.parseInt((String)mapParent.get(DomainConstants.SELECT_LEVEL));
					}
				}

				if (mapParent != null)
				{
					//
					// Make parent aware of his child category/item
					//
					MapList mlChildren = (MapList)mapParent.get("Children");
					if (mlChildren == null)
					{
						mlChildren = new MapList();
						mapParent.put("Children", mlChildren);
					}
					mlChildren.add(mapChild);

					//
					// Make child aware of his parent category
					//
					mapChild.put("Parent", mapParent);
				}
			}

			stParent.push(mapChild);
		}//For
		stParent = null;

		//
		// Convert the flat list to tree structure
		//
		MapList mlResult = new MapList();
		for (Iterator itrCats = companyCategoryList.iterator(); itrCats.hasNext();) {
			Map mapCat = (Map) itrCats.next();
			if (mapCat.get("Parent") == null) {
				mlResult.add(mapCat);
			}
		}

		return mlResult;
	}

	/**
	 * Returns summary node data of project's Budget.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args String array holding request parameters.
	 * @param isCostItem is true for getting Budget's data.
	 * @return a MapList containing summary node information of project's Budget.
	 * @throws Exception if operation fails.
	 */
	public MapList getTableProjectBudgetData(Context context, String[] args, boolean isCostItem) throws Exception{
		String strItemTypePattern=null;
		String SELECT_PLANNED_AMOUNT=null;
		String ATTRIBUTE_PLANNED_AMOUNT=null;
		String SELECT_INTERVAL_PLANNED_AMOUNT=null;
		String SELECT_INTERVAL_DATE=null;
		String SELECT_PHASE_ID = null;
		String SELECT_ATTRIBUTE_COST_INTERVAL =null;
		String SELECT_PLANNED_AMOUNT_UNIT = null;
		Map programMap = (Map) JPO.unpackArgs(args);
		String strObjectId = (String) programMap.get("objectId");
		String strViewTableType = "";
		strViewTableType = (String)programMap.get("selectedTable");
		String years = "";
		String selecteView = "";
		String SELECT_ACTUAL_AMOUNT = "";
		String SELECT_ACTUAL_AMOUNT_UNIT = "";
		StringList busSelect = new StringList();
		String strPrefferedCurrency = emxProgramCentralUtilBase_mxJPO.getUserPreferenceCurrency(context, "emxProjectBudget.Error.NoDefaultCurrencyDefined");
		String strSelectedCurrency = "";
		String SELECT_INTERVAL_PLANNED_AMOUNT_UNIT = null;
		strItemTypePattern = Financials.TYPE_COST_ITEM;
		SELECT_INTERVAL_DATE=SELECT_COST_INTERVAL_INTERVAL_DATE;
		SELECT_PHASE_ID = "from["+RELATIONSHIP_COST_ITEM_INTERVAL+"].to.id";
		SELECT_ATTRIBUTE_COST_INTERVAL = "attribute["+ATTRIBUTE_COST_INTERVAL+"]";
		strSelectedCurrency = (String)programMap.get(BUDGETCURRENCYFILTER);
		String displayViewMode = (String) programMap.get(BUDGETDISPLAYVIEWFILTER);
		String numberOfFiscalYears = (String) programMap.get(BUDGETINTERVALFILTER);
		String fiscalYear = (String) programMap.get(BUDGETFISCALYEARFILTER);
		if(programMap.containsKey(BUDGETVIEWFILTER)){
			selecteView = (String)programMap.get(BUDGETVIEWFILTER);
			if(ProgramCentralUtil.isNullString(selecteView)){
				if(isCurrentStateFrozen(context, args, true))
					selecteView = planView;
				else
					selecteView = estimateView;
			}
		}
		if(strViewTableType.equalsIgnoreCase("PMCProjectBudgetReportTable")){
			selecteView = actualView;
			strSelectedCurrency = (String)programMap.get(BUDGETREPORTCURRENCYFILTER);
			fiscalYear = (String) programMap.get("PMCProjectBudgetReportFiscalYearFilter");
			displayViewMode = (String) programMap.get("PMCProjectBudgetReportDisplayViewFilter");
			numberOfFiscalYears = (String) programMap.get("PMCProjectBudgetReportIntervalFilter");
		}
		if(!ProgramCentralUtil.isNullString(strSelectedCurrency)){
			strPrefferedCurrency = strSelectedCurrency;
		}

		if(planView.equals(selecteView)){
			SELECT_INTERVAL_PLANNED_AMOUNT=SELECT_COST_INTERVAL_PLANNED_COST;
			SELECT_INTERVAL_PLANNED_AMOUNT_UNIT ="from["+RELATIONSHIP_COST_ITEM_INTERVAL+"].attribute["+ATTRIBUTE_PLANNED_COST+"].inputunit";
			SELECT_PLANNED_AMOUNT=CostItem.SELECT_PLANNED_COST;
			SELECT_PLANNED_AMOUNT_UNIT = "attribute[" + ATTRIBUTE_PLANNED_COST + "].inputunit";
			busSelect.add(SELECT_COST_INTERVAL_START_DATE);
			busSelect.add(SELECT_COST_INTERVAL_END_DATE);
		}
		else if(estimateView.equals(selecteView)){
			SELECT_INTERVAL_PLANNED_AMOUNT=SELECT_COST_INTERVAL_ESTIMATED_COST;
			SELECT_INTERVAL_PLANNED_AMOUNT_UNIT ="from["+RELATIONSHIP_COST_ITEM_INTERVAL+"].attribute["+ATTRIBUTE_ESTIMATED_COST+"].inputunit";
			SELECT_PLANNED_AMOUNT=CostItem.SELECT_ESTIMATED_COST;
			SELECT_PLANNED_AMOUNT_UNIT = "attribute[" + ATTRIBUTE_ESTIMATED_COST + "].inputunit";
			SELECT_ACTUAL_AMOUNT=CostItem.SELECT_ACTUAL_COST;
			SELECT_ACTUAL_AMOUNT_UNIT = "attribute[" + ATTRIBUTE_ACTUAL_COST + "].inputunit";
			busSelect.add(SELECT_ACTUAL_AMOUNT);
			busSelect.add(SELECT_ACTUAL_AMOUNT_UNIT);
			busSelect.add(SELECT_COST_INTERVAL_START_DATE);
			busSelect.add(SELECT_COST_INTERVAL_END_DATE);
		}
		else if(actualView.equals(selecteView)){
			SELECT_INTERVAL_PLANNED_AMOUNT=SELECT_COST_INTERVAL_ACTUAL_COST;
			SELECT_INTERVAL_PLANNED_AMOUNT_UNIT ="from["+RELATIONSHIP_COST_ITEM_INTERVAL+"].attribute["+ATTRIBUTE_ACTUAL_COST+"].inputunit";
			SELECT_PLANNED_AMOUNT=CostItem.SELECT_ACTUAL_COST;
			SELECT_PLANNED_AMOUNT_UNIT = "attribute[" + ATTRIBUTE_ACTUAL_COST + "].inputunit";
			SELECT_ACTUAL_AMOUNT=CostItem.SELECT_PLANNED_COST;
			SELECT_ACTUAL_AMOUNT_UNIT = "attribute[" + ATTRIBUTE_PLANNED_COST + "].inputunit";
			busSelect.add(SELECT_ACTUAL_AMOUNT);
			busSelect.add(SELECT_ACTUAL_AMOUNT_UNIT);
			busSelect.add(SELECT_COST_INTERVAL_START_DATE);
			busSelect.add(SELECT_COST_INTERVAL_END_DATE);
		}

		String strRelationshipType = RELATIONSHIP_PROJECT_FINANCIAL_ITEM;
		busSelect.add(DomainConstants.SELECT_ID);
		busSelect.add(DomainConstants.SELECT_NAME);
		busSelect.add(DomainConstants.SELECT_TYPE);
		busSelect.add(SELECT_ATTRIBUTE_COST_INTERVAL);
		busSelect.add(SELECT_PLANNED_AMOUNT);
		busSelect.add(SELECT_PLANNED_AMOUNT_UNIT);

		StringList relSelect = new StringList();
		relSelect.add(DomainRelationship.SELECT_ID);

		DomainObject dmoProject = DomainObject.newInstance(context, strObjectId);

		if(!dmoProject.isKindOf(context, TYPE_PROJECT_SPACE) && dmoProject.isKindOf(context, TYPE_FINANCIAL_ITEM)){
			strObjectId= (String)dmoProject.getInfo(context,"to["+RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"].from.id");
			dmoProject = DomainObject.newInstance(context, strObjectId);
		}

		MapList mlBudgetBenefit = dmoProject.getRelatedObjects(
				context,
				strRelationshipType,
				TYPE_BUDGET,
				busSelect,
				relSelect,
				false,
				true,
				(short)1,
				null,
				DomainConstants.EMPTY_STRING,
				0);

		Map mapBudgetBenefitInfo = null;

		java.util.Set keyset=new HashSet();
		String strPlannedBenefitValue = null;
		Object relPlannedBenefitValues=null;
		Object intervalDateValues=null;
		Object phaseIdValues = null;
		Object relPlannedBenefitValuesUnits=null;
		StringList slRelPlannedBenefitValues=new StringList();
		StringList slIntervalDateValues=new StringList();

		StringList slRelPlannedBenefitValuesUnits=new StringList();
		strRelationshipType=Financials.RELATIONSHIP_FINANCIAL_ITEMS;

		boolean showNoExchRateWarning = false;
		i18nNow i18n = new i18nNow();
		final String STRING_NO_EXCHANGE_RATE_ASSUMING_1_RATE = i18n.GetString("emxProgramCentralStringResource", context.getSession().getLanguage(), "emxProgramCentral.CurrencyConvrsion.NoExchangeRateAssuming1Rate");
		for (Iterator itrBudgetBenefit = mlBudgetBenefit.iterator(); itrBudgetBenefit.hasNext();){
			mapBudgetBenefitInfo = (Map) itrBudgetBenefit.next();
			String strInterval = (String)mapBudgetBenefitInfo.get(SELECT_ATTRIBUTE_COST_INTERVAL);
			if(!"Project Phase".equals(strInterval)){
				double actualTotal = 0D;
				double estimateTotal = 0D;
				double plannedTotal = 0D;
				if(ProgramCentralUtil.isNullString(fiscalYear)){
					emxProjectBudgetBase_mxJPO budget = new emxProjectBudgetBase_mxJPO(context, args);
					Map requestMap = new HashMap();
					requestMap.put("requestMap", programMap);
					Map fiscalMap = budget.getBudgetFiscalReportFilterRange(context, JPO.packArgs(requestMap));
					StringList slFiscals = (StringList) fiscalMap.get("field_choices");
					fiscalYear = (String) slFiscals.get(0);
				}
				List<Integer> validFiscals = new ArrayList<Integer>();
				//if("FiscalYear".equals(displayViewMode)){
				StringList slFiscals = FrameworkUtil.split(numberOfFiscalYears, ProgramCentralConstants.SPACE);
				String strNumberOfFiscals = (String) slFiscals.get(0);
				int numberOfFiscals = Integer.parseInt(strNumberOfFiscals);
				int validYear = Integer.parseInt(fiscalYear);
				for(int index=0; index<numberOfFiscals; index++){
					validFiscals.add(validYear);
					validYear += 1;
				}
				//}

				//Get Cost or Benefit Items from Budget or Benefit
				DomainObject domBenefit = DomainObject.newInstance(context, (String)mapBudgetBenefitInfo.get(DomainConstants.SELECT_ID));
				busSelect.add(SELECT_ID);
				//This condition is added to get total planned cost in actual view table.
				if(actualView.equals(selecteView)){
					SELECT_INTERVAL_PLANNED_AMOUNT = SELECT_COST_INTERVAL_PLANNED_COST;
					SELECT_INTERVAL_PLANNED_AMOUNT_UNIT = "from["+RELATIONSHIP_COST_ITEM_INTERVAL+"].attribute["+ATTRIBUTE_PLANNED_COST+"].inputunit";
				}
				busSelect.add(SELECT_INTERVAL_PLANNED_AMOUNT);
				busSelect.add(SELECT_INTERVAL_PLANNED_AMOUNT_UNIT);
				busSelect.add(SELECT_INTERVAL_DATE);
				busSelect.add(SELECT_PHASE_ID);
				MapList mlBenefitItems = domBenefit.getRelatedObjects(
						context,
						strRelationshipType,
						strItemTypePattern,
						busSelect,
						relSelect,
						false,
						true,
						(short)1,
						null,
						DomainConstants.EMPTY_STRING,
						0);
				Map mapBenItemInfo=null;
				for (Iterator itrBenItem = mlBenefitItems.iterator(); itrBenItem.hasNext();){
					double benItemActualTotal = 0D;
					mapBenItemInfo = (Map) itrBenItem.next();
					String fItemId =(String)mapBenItemInfo.get(SELECT_ID);
					//If Person's preferred currency not equal to filter selected Currency then the cost items will not be editable
					if(null != strSelectedCurrency && !strSelectedCurrency.equals(strPrefferedCurrency)){
						mapBudgetBenefitInfo.put("RowEditable", "readonly");
					}

					//This will disable the Actual View table
					if(actualView.equals(selecteView)){
						mapBenItemInfo.put("RowEditable", "readonly");
					}
					relPlannedBenefitValues=mapBenItemInfo.get(SELECT_INTERVAL_PLANNED_AMOUNT);
					relPlannedBenefitValuesUnits = mapBenItemInfo.get(SELECT_INTERVAL_PLANNED_AMOUNT_UNIT);

					//if(!actualView.equals(selecteView) && relPlannedBenefitValues!=null){
					if(relPlannedBenefitValues instanceof StringList){
						slRelPlannedBenefitValues=(StringList)relPlannedBenefitValues;
						slRelPlannedBenefitValuesUnits=(StringList)relPlannedBenefitValuesUnits;
					}else {
						slRelPlannedBenefitValues.clear();
						slRelPlannedBenefitValuesUnits.clear();
						slRelPlannedBenefitValues.add((String)relPlannedBenefitValues);
						slRelPlannedBenefitValuesUnits.add((String)relPlannedBenefitValuesUnits);
					}
					//}
					intervalDateValues = mapBenItemInfo.get(SELECT_INTERVAL_DATE);
					if(intervalDateValues instanceof StringList){
						slIntervalDateValues =(StringList)intervalDateValues;
					}else{
						slIntervalDateValues.clear();
						slIntervalDateValues.add(intervalDateValues);
					}
					//Filter out intervals falling in invalid fiscals.
					StringList slValidIntervals = new StringList(slIntervalDateValues.size());
					StringList slValidFinancialValues = new StringList(slRelPlannedBenefitValues.size());
					StringList slValidFinancialUnitValues = new StringList(slRelPlannedBenefitValuesUnits.size());
					//for (Iterator iterator = slIntervalDateValues.iterator(); iterator.hasNext();) {
					for (int index = 0; index< slIntervalDateValues.size(); index++) {
						String strIntervalDate = (String) slIntervalDateValues.get(index);
						Date intervalDate = eMatrixDateFormat.getJavaDate(strIntervalDate);
						int year = Financials.getFiscalYear(intervalDate);
						if(validFiscals.contains(year)){
							slValidIntervals.add(strIntervalDate);
							//if(!(actualView.equals(selecteView))){
							slValidFinancialValues.add(slRelPlannedBenefitValues.get(index));
							slValidFinancialUnitValues.add(slRelPlannedBenefitValuesUnits.get(index));
							//}
						}
					}
					//Only Valid Intervals
					if(!slValidIntervals.isEmpty()){
						slIntervalDateValues.clear();
						slIntervalDateValues = slValidIntervals;
					}
					//Only Valid Fiancial Values
					if(!slValidFinancialValues.isEmpty()){
						slRelPlannedBenefitValues.clear();
						slRelPlannedBenefitValues = slValidFinancialValues;
					}
					//Only Valid Fiancial Units
					if(!slValidFinancialUnitValues.isEmpty()){
						slRelPlannedBenefitValuesUnits.clear();
						slRelPlannedBenefitValuesUnits = slValidFinancialUnitValues;
					}
					if((actualView.equals(selecteView) && slIntervalDateValues !=null)){
						String strIntervalValue;
						Date intervalDate;
						for(int cnt=0;cnt<slIntervalDateValues.size();cnt++){
							strIntervalValue = (String)slIntervalDateValues.get(cnt);
							intervalDate = eMatrixDateFormat.getJavaDate(strIntervalValue);
							String financialvalue = (String) slRelPlannedBenefitValues.get(cnt);
							String financialUnit = (String) slRelPlannedBenefitValuesUnits.get(cnt);
							plannedTotal += convertAmount(context, Task.parseToDouble(financialvalue), financialUnit, strPrefferedCurrency, new Date());
						}
						strIntervalValue = "";
						intervalDate = null;
						slRelPlannedBenefitValues.clear();
						slRelPlannedBenefitValuesUnits.clear();
						for(int cnt=0;cnt<slIntervalDateValues.size();cnt++){
							strIntervalValue = (String)slIntervalDateValues.get(cnt);
							intervalDate = eMatrixDateFormat.getJavaDate(strIntervalValue);
							Double totalIntervalActualCost = getIntervalTotalActualCost(context,fItemId,strInterval,intervalDate,strPrefferedCurrency);
							slRelPlannedBenefitValues.add(totalIntervalActualCost.toString());
							slRelPlannedBenefitValuesUnits.add(strPrefferedCurrency);
						}
						actualTotal += Task.parseToDouble(FrameworkUtil.getSum(context,context.getLocale(), slRelPlannedBenefitValues));
					}
					else if(estimateView.equals(selecteView) && slIntervalDateValues !=null){
						String strIntervalValue;
						Date intervalDate;
						for(int cnt=0; cnt<slIntervalDateValues.size(); cnt++){
							strIntervalValue = (String)slIntervalDateValues.get(cnt);
							intervalDate = eMatrixDateFormat.getJavaDate(strIntervalValue);
							Double totalIntervalActualCost = getIntervalTotalActualCost(context,fItemId,strInterval,intervalDate,strPrefferedCurrency);
							actualTotal += totalIntervalActualCost;
							String financialvalue = (String) slRelPlannedBenefitValues.get(cnt);
							String financialUnit = (String) slRelPlannedBenefitValuesUnits.get(cnt);
							estimateTotal += convertAmount(context, Task.parseToDouble(financialvalue ), financialUnit, strPrefferedCurrency, new Date());
						}
					}
					else if(planView.equals(selecteView) && slIntervalDateValues !=null){
						String strIntervalValue;
						Date intervalDate;
						for(int cnt=0; cnt<slIntervalDateValues.size(); cnt++){
							strIntervalValue = (String)slIntervalDateValues.get(cnt);
							intervalDate = eMatrixDateFormat.getJavaDate(strIntervalValue);
							String financialvalue = (String) slRelPlannedBenefitValues.get(cnt);
							String financialUnit = (String) slRelPlannedBenefitValuesUnits.get(cnt);
							plannedTotal += convertAmount(context, Task.parseToDouble(financialvalue ), financialUnit, strPrefferedCurrency, new Date());
						}
					}

					if(slRelPlannedBenefitValues!=null){
						//int slSize = slRelPlannedBenefitValues.size();
						int slSize = slIntervalDateValues.size();
						for(int i=0;i<slSize;i++){
							String strPB = (String)slRelPlannedBenefitValues.get(i);
							String strID = (String)slIntervalDateValues.get(i);
							String strPBUnit = (String)slRelPlannedBenefitValuesUnits.get(i);
							double dblPB =0D;
							if(!strPBUnit.equalsIgnoreCase(strPrefferedCurrency)){
								dblPB = Task.parseToDouble(strPB);
								dblPB = convertAmount(context, dblPB, strPBUnit, strPrefferedCurrency, new Date());
								strPB = String.valueOf(dblPB);
								strPBUnit = strPrefferedCurrency;
							}
							if(!"Project Phase".equals(strInterval)) {
								strID=strID.substring(0,strID.indexOf(" "));
							}else{
								if("PMCProjectBudgetReportTable".equals(strViewTableType)){
									DomainObject dmoObj = DomainObject.newInstance(context, strID);
									if(dmoObj.isKindOf(context, TYPE_PHASE)){
										strID = "Phase_"+strID;
									}
								}else{
									strID = strID;
								}
							}
							slRelPlannedBenefitValues.set(i, strPB);
							slRelPlannedBenefitValuesUnits.set(i, strPBUnit);
							double dblValueSoFar = 0D;
							if(mapBudgetBenefitInfo.containsKey(strID)){
								String strValueSoFar = (String)mapBudgetBenefitInfo.get(strID);
								String strValueSoFarUnit = (String)mapBudgetBenefitInfo.get(strID + "_Unit");
								if(ProgramCentralUtil.isNotNullString(strValueSoFar)){
									dblValueSoFar = Task.parseToDouble(strValueSoFar);
									if(!strValueSoFarUnit.equalsIgnoreCase(strPBUnit))
										dblValueSoFar = convertAmount(context, dblValueSoFar, strValueSoFarUnit, strPBUnit, new Date());
								}
								dblPB = Task.parseToDouble(strPB);
								dblPB += dblValueSoFar;
								strPB = String.valueOf(dblPB);
							}

							mapBudgetBenefitInfo.put(strID,strPB);// for each cost or benefit item
							mapBudgetBenefitInfo.put(strID+"_Unit",strPBUnit);
						}//slRelPlannedBenefitValues iteration ends here
					}// if ends
				}//mlBenefitItems iteration ends here

				//If Person's preferred currency not equal to filter selected Currency then the cost items will not be editable
				if(null != strSelectedCurrency && !strSelectedCurrency.equals(strPrefferedCurrency)){
					mapBudgetBenefitInfo.put("RowEditable", "readonly");
				}

				String strPlannedBenefitUnitValue = null;
				if(planView.equals(selecteView)){
					mapBudgetBenefitInfo.put("RowEditable","readonly");
					mapBudgetBenefitInfo.put("Total",String.valueOf(plannedTotal));
					mapBudgetBenefitInfo.put("Total_Unit",strPrefferedCurrency);
				}
				else if(estimateView.equals(selecteView)){
					//actualTotal = Task.parseToDouble(FrameworkUtil.getSum(context,context.getLocale(), slRelPlannedBenefitValues));
					//String strActualTotalUnit = (String) mapBudgetBenefitInfo.get(SELECT_ACTUAL_AMOUNT_UNIT);
					//estimateTotal = Task.parseToDouble(strPlannedBenefitValue);
					double amountTotal = 0;
					double variance = 0;
					amountTotal = actualTotal - estimateTotal;
					if(estimateTotal != 0){
						variance=((actualTotal * 100)/estimateTotal);
					}
					actualTotal = (Double)Task.parseToDouble(actualTotal);
					amountTotal=(Double)Task.parseToDouble(amountTotal);
					variance=(Double)Task.parseToDouble(variance);
					mapBudgetBenefitInfo.put("RowEditable","readonly");
					mapBudgetBenefitInfo.put("Total", String.valueOf(estimateTotal));
					mapBudgetBenefitInfo.put("Total_Unit",strPrefferedCurrency);
					mapBudgetBenefitInfo.put("PlanOrActual",actualTotal + DomainConstants.EMPTY_STRING);
					mapBudgetBenefitInfo.put("PlanOrActual_Unit",strPrefferedCurrency);
					mapBudgetBenefitInfo.put("VarianceAmount", amountTotal + DomainConstants.EMPTY_STRING);
					mapBudgetBenefitInfo.put("VarianceAmount_Unit",strPrefferedCurrency );
					if(estimateTotal != 0){
						mapBudgetBenefitInfo.put("VariancePercent", variance+DomainConstants.EMPTY_STRING);
					}else{
						mapBudgetBenefitInfo.put("VariancePercent", "NA");
					}
				}
				else if(actualView.equals(selecteView)){
					mapBudgetBenefitInfo.put("RowEditable", "readonly");
					double plannedValue;
					//					double PlanTotal = Task.parseToDouble((String) mapBudgetBenefitInfo.get(SELECT_ACTUAL_AMOUNT));
					//					String strActualTotalUnit = (String) mapBudgetBenefitInfo.get(SELECT_ACTUAL_AMOUNT_UNIT);
					//					if(!strActualTotalUnit.equalsIgnoreCase(strPrefferedCurrency)){
					//						PlanTotal = convertAmount(context, PlanTotal, strActualTotalUnit, strPrefferedCurrency, new Date());
					//						strActualTotalUnit = strPrefferedCurrency;
					//					}
					double amountTotal=0;
					double variance = 0;
					//amountTotal=actualTotal - PlanTotal;
					amountTotal = actualTotal - plannedTotal;
					if(plannedTotal != 0){
						variance=((actualTotal * 100)/plannedTotal);
					}

					amountTotal = (Double)Task.parseToDouble(amountTotal);
					variance = (Double)Task.parseToDouble(variance);

					mapBudgetBenefitInfo.put("Total",actualTotal);
					mapBudgetBenefitInfo.put("Total_Unit",strPrefferedCurrency);
					mapBudgetBenefitInfo.put("PlanOrActual",String.valueOf(plannedTotal));
					mapBudgetBenefitInfo.put("PlanOrActual_Unit",strPrefferedCurrency);
					mapBudgetBenefitInfo.put("VarianceAmount", amountTotal+DomainConstants.EMPTY_STRING);
					mapBudgetBenefitInfo.put("VarianceAmount_Unit",strPrefferedCurrency);
					if(plannedTotal != 0){
						mapBudgetBenefitInfo.put("VariancePercent", variance+DomainConstants.EMPTY_STRING);
					}else{
						mapBudgetBenefitInfo.put("VariancePercent", "NA");
					}
				}
			}else{
				//If Person's preferred currency not equal to filter selected Currency then the cost items will not be editable
				if(null != strSelectedCurrency && !strSelectedCurrency.equals(strPrefferedCurrency)){
					mapBudgetBenefitInfo.put("RowEditable", "readonly");
				}

				//This will disable the Actual View table
				if(isCostItem)
				{
					if(actualView.equals(selecteView)){
						mapBudgetBenefitInfo.put("RowEditable", "readonly");
					}
				}

				strPlannedBenefitValue=(String) mapBudgetBenefitInfo.get(SELECT_PLANNED_AMOUNT);
				String strPlannedBenefitUnitValue=(String) mapBudgetBenefitInfo.get(SELECT_PLANNED_AMOUNT_UNIT);
				double convertedAmount = Currency.convert(context, Task.parseToDouble(strPlannedBenefitValue), strPlannedBenefitUnitValue, strPrefferedCurrency, new Date(), true);
				strPlannedBenefitValue = String.valueOf(convertedAmount);
				strPlannedBenefitUnitValue = strPrefferedCurrency;
				mapBudgetBenefitInfo.put("RowEditable","readonly");
				mapBudgetBenefitInfo.put("Total",strPlannedBenefitValue);
				mapBudgetBenefitInfo.put("Total_Unit",strPlannedBenefitUnitValue);

				if(estimateView.equals(selecteView)){
					double actualTotal=Task.parseToDouble((String) mapBudgetBenefitInfo.get(SELECT_ACTUAL_AMOUNT));
					String strActualTotalUnit = (String) mapBudgetBenefitInfo.get(SELECT_ACTUAL_AMOUNT_UNIT);
					actualTotal = Currency.convert(context, actualTotal, strActualTotalUnit, strPrefferedCurrency, new Date(), true);
					strActualTotalUnit = strPrefferedCurrency;

					double estimateTotal=Task.parseToDouble(strPlannedBenefitValue);
					double amountTotal=0;
					double variance = 0;
					amountTotal=actualTotal - estimateTotal;
					if(estimateTotal != 0){
						variance=((actualTotal * 100)/estimateTotal);
					}

					actualTotal=(Double)Task.parseToDouble(actualTotal);
					amountTotal=(Double)Task.parseToDouble(amountTotal);
					variance=(Double)Task.parseToDouble(variance);

					mapBudgetBenefitInfo.put("PlanOrActual",actualTotal+DomainConstants.EMPTY_STRING);
					mapBudgetBenefitInfo.put("PlanOrActual_Unit",strActualTotalUnit);
					mapBudgetBenefitInfo.put("VarianceAmount", amountTotal+DomainConstants.EMPTY_STRING);
					mapBudgetBenefitInfo.put("VarianceAmount_Unit",strActualTotalUnit );
					if(estimateTotal != 0){
						mapBudgetBenefitInfo.put("VariancePercent", variance+DomainConstants.EMPTY_STRING);
					}else{
						mapBudgetBenefitInfo.put("VariancePercent", "NA");
					}
				}

				if(actualView.equals(selecteView)){
					double PlanTotal=Task.parseToDouble((String) mapBudgetBenefitInfo.get(SELECT_ACTUAL_AMOUNT));
					String strActualTotalUnit = (String) mapBudgetBenefitInfo.get(SELECT_ACTUAL_AMOUNT_UNIT);
					PlanTotal = Currency.convert(context, PlanTotal, strActualTotalUnit, strPrefferedCurrency, new Date(), true);
					strActualTotalUnit = strPrefferedCurrency;
					double actualTotal=Task.parseToDouble(strPlannedBenefitValue);
					double amountTotal=0;
					double variance = 0;
					amountTotal=actualTotal - PlanTotal;
					if(PlanTotal != 0){
						variance=((actualTotal * 100)/PlanTotal);
					}

					PlanTotal=(Double)Task.parseToDouble(PlanTotal);
					amountTotal=(Double)Task.parseToDouble(amountTotal);
					variance=(Double)Task.parseToDouble(variance);

					mapBudgetBenefitInfo.put("PlanOrActual",PlanTotal+DomainConstants.EMPTY_STRING);
					mapBudgetBenefitInfo.put("PlanOrActual_Unit",strActualTotalUnit);
					mapBudgetBenefitInfo.put("VarianceAmount", amountTotal+DomainConstants.EMPTY_STRING);
					mapBudgetBenefitInfo.put("VarianceAmount_Unit",strActualTotalUnit );
					if(PlanTotal != 0){
						mapBudgetBenefitInfo.put("VariancePercent", variance+DomainConstants.EMPTY_STRING);
					}else{
						mapBudgetBenefitInfo.put("VariancePercent", "NA");
					}
				}

				DomainObject domBenefit=DomainObject.newInstance(context, (String)mapBudgetBenefitInfo.get(DomainConstants.SELECT_ID));
				busSelect.add(SELECT_INTERVAL_PLANNED_AMOUNT);
				busSelect.add(SELECT_INTERVAL_PLANNED_AMOUNT_UNIT);
				busSelect.add(SELECT_INTERVAL_DATE);
				if(isCostItem){
					busSelect.add(SELECT_PHASE_ID);
				}
				MapList mlBenefitItems = domBenefit.getRelatedObjects(
						context,
						strRelationshipType,
						strItemTypePattern,
						busSelect,
						relSelect,
						false,
						true,
						(short)1,
						null,
						DomainConstants.EMPTY_STRING,
						0);

				Map mapBenItemInfo=null;
				for (Iterator itrBenItem = mlBenefitItems.iterator(); itrBenItem.hasNext();){
					mapBenItemInfo = (Map) itrBenItem.next();
					//If Person's preferred currency not equal to filter selected Currency then the cost items will not be editable
					if(null != strSelectedCurrency && !strSelectedCurrency.equals(strPrefferedCurrency)){
						mapBudgetBenefitInfo.put("RowEditable", "readonly");
					}

					//This will disable the Actual View table
					if(isCostItem)
					{
						if(actualView.equals(selecteView)){
							mapBenItemInfo.put("RowEditable", "readonly");
						}
					}

					relPlannedBenefitValues=mapBenItemInfo.get(SELECT_INTERVAL_PLANNED_AMOUNT);
					relPlannedBenefitValuesUnits = mapBenItemInfo.get(SELECT_INTERVAL_PLANNED_AMOUNT_UNIT);
					if(!"Project Phase".equals(strInterval)) {
						intervalDateValues=mapBenItemInfo.get(SELECT_INTERVAL_DATE);
					}
					else {
						intervalDateValues=mapBenItemInfo.get(SELECT_PHASE_ID);
					}

					if(relPlannedBenefitValues!=null){
						if(relPlannedBenefitValues instanceof StringList){

							slRelPlannedBenefitValues=(StringList)relPlannedBenefitValues;
							slIntervalDateValues=(StringList)intervalDateValues;

							slRelPlannedBenefitValuesUnits=(StringList)relPlannedBenefitValuesUnits;
						}
						else{
							slRelPlannedBenefitValues.clear();
							slRelPlannedBenefitValuesUnits.clear();
							slIntervalDateValues.clear();
							slRelPlannedBenefitValues.add((String)relPlannedBenefitValues);
							slRelPlannedBenefitValuesUnits.add((String)relPlannedBenefitValuesUnits);
							slIntervalDateValues.add((String)intervalDateValues);
						}
					}
					if(slRelPlannedBenefitValues!=null){
						int slSize=slRelPlannedBenefitValues.size();
						for(int i=0;i<slSize;i++){
							String strPB=(String)slRelPlannedBenefitValues.get(i);
							String strID=(String)slIntervalDateValues.get(i);
							String strPBUnit=(String)slRelPlannedBenefitValuesUnits.get(i);

							if(!"Project Phase".equals(strInterval)) {
								strID=strID.substring(0,strID.indexOf(" "));
							}
							else{
								if("PMCProjectBudgetReportTable".equals(strViewTableType)){
									DomainObject dmoObj = DomainObject.newInstance(context, strID);
									if(dmoObj.isKindOf(context, TYPE_PHASE)){
										strID = "Phase_"+strID;
									}
								}else{
									strID = strID;
								}
							}
							//Added 27-Sep-2010:PRG:RG6:IR-070662V6R2011x
							double dblItemAmntInDestUnit;
							if(mapBudgetBenefitInfo.containsKey(strID)){
								String strDestUnit = (String)mapBudgetBenefitInfo.get(strID+"_Unit");
								String strPartialAmount = (String)mapBudgetBenefitInfo.get(strID);
								///TEST
								dblItemAmntInDestUnit = 0.0;
								dblItemAmntInDestUnit = Task.parseToDouble(strPB);
								try {
									dblItemAmntInDestUnit = convertAmount(context, Task.parseToDouble(strPB), strPBUnit, strDestUnit, new Date(), true);
								} catch (Exception exp) {
									showNoExchRateWarning = true;
								}
								double plannedBenefit=Task.parseToDouble(strPartialAmount) + dblItemAmntInDestUnit;
								plannedBenefit=(Double)Task.parseToDouble(plannedBenefit);
								mapBudgetBenefitInfo.put(strID,plannedBenefit+DomainConstants.EMPTY_STRING);
							}
							else{
								dblItemAmntInDestUnit = Task.parseToDouble(strPB);
								try {
									//NZF
									if("PMCProjectBudgetReportTable".equals(strViewTableType) && null==strSelectedCurrency){
										strSelectedCurrency = strPrefferedCurrency;
									}//NZF END
									dblItemAmntInDestUnit = convertAmount(context, dblItemAmntInDestUnit, strPBUnit, strSelectedCurrency, new Date(), true);
									mapBudgetBenefitInfo.put(strID,dblItemAmntInDestUnit+DomainConstants.EMPTY_STRING);
									mapBudgetBenefitInfo.put(strID+"_Unit",strSelectedCurrency);
								}catch(Exception e){
									showNoExchRateWarning = true;
									mapBudgetBenefitInfo.put(strID,strPB);
									mapBudgetBenefitInfo.put(strID+"_Unit",strPBUnit);
								}
							}
							//End Added 27-Sep-2010:PRG:RG6:IR-070662V6R2011x
						}
					}
				}
			}
		}

		if (showNoExchRateWarning) {
			MqlUtil.mqlCommand(context, "notice $1", true,STRING_NO_EXCHANGE_RATE_ASSUMING_1_RATE);
		}

		return mlBudgetBenefit ;
	}

	/**
	 * Returns summary node data of project's Benefit.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args String array holding request parameters.
	 * @param isCostItem is false for getting Benefit data.
	 * @return a MapList containing summary node information of project's Benefit.
	 * @throws Exception if operation fails.
	 */
	public MapList getTableProjectBenefitData(Context context, String[] args,boolean isCostItem) throws Exception{

		String strTypePattern=null;
		String strItemTypePattern=null;
		String SELECT_PLANNED_AMOUNT=null;
		String ATTRIBUTE_PLANNED_AMOUNT=null;
		String SELECT_INTERVAL_PLANNED_AMOUNT=null;
		String SELECT_INTERVAL_DATE=null;
		String SELECT_PHASE_ID = null;
		String SELECT_ATTRIBUTE_COST_INTERVAL =null;
		String SELECT_PLANNED_AMOUNT_UNIT = null;
		Map programMap = (Map) JPO.unpackArgs(args);
		String strObjectId = (String) programMap.get("objectId");
		String strViewTableType = "";
		strViewTableType = (String)programMap.get("selectedTable");
		String years = "";
		String selecteView = "";
		String SELECT_ACTUAL_AMOUNT = "";
		String SELECT_ACTUAL_AMOUNT_UNIT = "";
		DecimalFormat twoDForm = new DecimalFormat("#.##",new DecimalFormatSymbols(Locale.US));
		StringList busSelect = new StringList();

		String strPrefferedCurrency = emxProgramCentralUtilBase_mxJPO.getUserPreferenceCurrency(context, "emxProjectBudget.Error.NoDefaultCurrencyDefined");
		String strSelectedCurrency = "";
		String SELECT_INTERVAL_PLANNED_AMOUNT_UNIT = null;
		strTypePattern = TYPE_BENEFIT;
		strItemTypePattern = Financials.TYPE_BENEFIT_ITEM;
		SELECT_INTERVAL_DATE=SELECT_BENEFIT_INTERVAL_INTERVAL_DATE;
		selecteView = (String)programMap.get(BENEFITVIEWFILTER);
		strSelectedCurrency = (String)programMap.get(BENEFITCURRENCYFILTER);
		if(ProgramCentralUtil.isNotNullString(strSelectedCurrency)){
			strPrefferedCurrency = strSelectedCurrency;
		}
		if(ProgramCentralUtil.isNullString(selecteView)){
			if(isCurrentStateFrozen(context, args, isCostItem))
				selecteView = planView;
			else
				selecteView = estimateView;
		}
		if(planView.equals(selecteView)){
			SELECT_PLANNED_AMOUNT=BenefitItem.SELECT_PLANNED_BENEFIT;
			SELECT_PLANNED_AMOUNT_UNIT = "attribute[" + ATTRIBUTE_PLANNED_BENEFIT + "].inputunit";
			SELECT_INTERVAL_PLANNED_AMOUNT=SELECT_BENEFIT_INTERVAL_PLANNED_BENEFIT;
			SELECT_INTERVAL_PLANNED_AMOUNT_UNIT ="from["+RELATIONSHIP_BENEFIT_ITEM_INTERVAL+"].attribute["+ATTRIBUTE_PLANNED_BENEFIT+"].inputunit";
		}
		else if(estimateView.equals(selecteView)){
			SELECT_PLANNED_AMOUNT=BenefitItem.SELECT_ESTIMATED_BENEFIT;
			SELECT_PLANNED_AMOUNT_UNIT = "attribute[" + ATTRIBUTE_ESTIMATED_BENEFIT + "].inputunit";
			SELECT_INTERVAL_PLANNED_AMOUNT=SELECT_BENEFIT_INTERVAL_ESTIMATED_BENEFIT;
			SELECT_INTERVAL_PLANNED_AMOUNT_UNIT ="from["+RELATIONSHIP_BENEFIT_ITEM_INTERVAL+"].attribute["+ATTRIBUTE_ESTIMATED_BENEFIT+"].inputunit";
			SELECT_ACTUAL_AMOUNT=BenefitItem.SELECT_ACTUAL_BENEFIT;
			SELECT_ACTUAL_AMOUNT_UNIT = "attribute[" + ATTRIBUTE_ACTUAL_BENEFIT + "].inputunit";
			busSelect.add(SELECT_ACTUAL_AMOUNT);
			busSelect.add(SELECT_ACTUAL_AMOUNT_UNIT);
		}
		else if(actualView.equals(selecteView)){
			SELECT_PLANNED_AMOUNT=BenefitItem.SELECT_ACTUAL_BENEFIT;
			SELECT_PLANNED_AMOUNT_UNIT = "attribute[" + ATTRIBUTE_ACTUAL_BENEFIT + "].inputunit";
			SELECT_INTERVAL_PLANNED_AMOUNT=SELECT_BENEFIT_INTERVAL_ACTUAL_BENEFIT;
			SELECT_INTERVAL_PLANNED_AMOUNT_UNIT ="from["+RELATIONSHIP_BENEFIT_ITEM_INTERVAL+"].attribute["+ATTRIBUTE_ACTUAL_BENEFIT+"].inputunit";
			SELECT_ACTUAL_AMOUNT=BenefitItem.SELECT_PLANNED_BENEFIT;
			SELECT_ACTUAL_AMOUNT_UNIT = "attribute[" + ATTRIBUTE_PLANNED_BENEFIT + "].inputunit";
			busSelect.add(SELECT_ACTUAL_AMOUNT);
			busSelect.add(SELECT_ACTUAL_AMOUNT_UNIT);
		}

		String strRelationshipType = RELATIONSHIP_PROJECT_FINANCIAL_ITEM;
		busSelect.add(DomainConstants.SELECT_ID);
		busSelect.add(DomainConstants.SELECT_NAME);
		busSelect.add(DomainConstants.SELECT_TYPE);
		if(isCostItem){
			busSelect.add(SELECT_ATTRIBUTE_COST_INTERVAL);
		}
		busSelect.add(SELECT_PLANNED_AMOUNT);
		busSelect.add(SELECT_PLANNED_AMOUNT_UNIT);

		StringList relSelect = new StringList();
		relSelect.add(DomainRelationship.SELECT_ID);

		DomainObject dmoProject = DomainObject.newInstance(context, strObjectId);

		if(!dmoProject.isKindOf(context, TYPE_PROJECT_SPACE) && dmoProject.isKindOf(context, TYPE_FINANCIAL_ITEM)){
			strObjectId= (String)dmoProject.getInfo(context,"to["+RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"].from.id");
			dmoProject = DomainObject.newInstance(context, strObjectId);
		}

		MapList mlBudgetBenefit = dmoProject.getRelatedObjects(
				context,
				strRelationshipType,
				strTypePattern,
				busSelect,
				relSelect,
				false,
				true,
				(short)1,
				null,
				DomainConstants.EMPTY_STRING,
				0);

		Map mapBudgetBenefitInfo = null;

		java.util.Set keyset=new HashSet();
		String strPlannedBenefitValue = null;
		Object relPlannedBenefitValues=null;
		Object intervalDateValues=null;
		Object phaseIdValues = null;
		Object relPlannedBenefitValuesUnits=null;
		StringList slRelPlannedBenefitValues=new StringList();
		StringList slIntervalDateValues=new StringList();

		StringList slRelPlannedBenefitValuesUnits=new StringList();
		StringList slIntervalDateValuesUnits=new StringList();

		strRelationshipType=Financials.RELATIONSHIP_FINANCIAL_ITEMS;

		boolean showNoExchRateWarning = false;
		String notice = "";
		i18nNow i18n = new i18nNow();
		final String STRING_NO_EXCHANGE_RATE_ASSUMING_1_RATE = i18n.GetString("emxProgramCentralStringResource", context.getSession().getLanguage(), "emxProgramCentral.CurrencyConvrsion.NoExchangeRateAssuming1Rate");

		for (Iterator itrBudgetBenefit = mlBudgetBenefit.iterator(); itrBudgetBenefit.hasNext();)
		{
			mapBudgetBenefitInfo = (Map) itrBudgetBenefit.next();
			//If Person's preferred currency not equal to filter selected Currency then the cost items will not be editable
			if(null != strSelectedCurrency && !strSelectedCurrency.equals(strPrefferedCurrency)){
				mapBudgetBenefitInfo.put("RowEditable", "readonly");
			}

			//This will disable the Actual View table
			if(isCostItem)
			{
				if(actualView.equals(selecteView)){
					mapBudgetBenefitInfo.put("RowEditable", "readonly");
				}
			}
			String strInterval ="";
			if(isCostItem){
				strInterval = (String)mapBudgetBenefitInfo.get(SELECT_ATTRIBUTE_COST_INTERVAL);
			}

			strPlannedBenefitValue=(String) mapBudgetBenefitInfo.get(SELECT_PLANNED_AMOUNT);
			String strPlannedBenefitUnitValue=(String) mapBudgetBenefitInfo.get(SELECT_PLANNED_AMOUNT_UNIT);
			mapBudgetBenefitInfo.put("RowEditable","readonly");
			mapBudgetBenefitInfo.put("Total",strPlannedBenefitValue);
			//This is to get Currency of the amount
			mapBudgetBenefitInfo.put("Total_Unit",strPlannedBenefitUnitValue);
			if(!selecteView.equalsIgnoreCase(planView))
			{
				double actualTotal=Task.parseToDouble((String) mapBudgetBenefitInfo.get(SELECT_ACTUAL_AMOUNT));
				String strActualTotalUnit = (String) mapBudgetBenefitInfo.get(SELECT_ACTUAL_AMOUNT_UNIT);
				double estimateTotal=Task.parseToDouble(strPlannedBenefitValue);
				double amountTotal=0;
				double variance = 0;
				amountTotal=actualTotal - estimateTotal;
				if(estimateTotal != 0){
					variance=((actualTotal * 100)/estimateTotal);
				}

				  actualTotal=(Double)Task.parseToDouble(twoDForm.format(actualTotal));
				  amountTotal=(Double)Task.parseToDouble(twoDForm.format(amountTotal));
				  variance=(Double)Task.parseToDouble(twoDForm.format(variance));

				mapBudgetBenefitInfo.put("PlanOrActual",actualTotal+DomainConstants.EMPTY_STRING);
				mapBudgetBenefitInfo.put("PlanOrActual_Unit",strActualTotalUnit);
				mapBudgetBenefitInfo.put("VarianceAmount", amountTotal+DomainConstants.EMPTY_STRING);
				mapBudgetBenefitInfo.put("VarianceAmount_Unit",strActualTotalUnit );
				if(estimateTotal != 0){
					mapBudgetBenefitInfo.put("VariancePercent", variance+DomainConstants.EMPTY_STRING);
				}else{
					mapBudgetBenefitInfo.put("VariancePercent", "NA");
				}
			}

			if(selecteView.equalsIgnoreCase(actualView))
			{
				double PlanTotal=Task.parseToDouble((String) mapBudgetBenefitInfo.get(SELECT_ACTUAL_AMOUNT));
				String strActualTotalUnit = (String) mapBudgetBenefitInfo.get(SELECT_ACTUAL_AMOUNT_UNIT);
				double actualTotal=Task.parseToDouble(strPlannedBenefitValue);
				double amountTotal=0;
				double variance = 0;
				amountTotal=actualTotal - PlanTotal;
				if(PlanTotal != 0){
					variance=((actualTotal * 100)/PlanTotal);
				}

				  PlanTotal=(Double)Task.parseToDouble(twoDForm.format(PlanTotal));
				  amountTotal=(Double)Task.parseToDouble(twoDForm.format(amountTotal));
				  variance=(Double)Task.parseToDouble(twoDForm.format(variance));

				mapBudgetBenefitInfo.put("PlanOrActual",PlanTotal+DomainConstants.EMPTY_STRING);
				mapBudgetBenefitInfo.put("PlanOrActual_Unit",strActualTotalUnit);
				mapBudgetBenefitInfo.put("VarianceAmount", amountTotal+DomainConstants.EMPTY_STRING);
				mapBudgetBenefitInfo.put("VarianceAmount_Unit",strActualTotalUnit );
				if(PlanTotal != 0){
					mapBudgetBenefitInfo.put("VariancePercent", variance+DomainConstants.EMPTY_STRING);
				}else{
					mapBudgetBenefitInfo.put("VariancePercent", "NA");
				}
			}

			DomainObject domBenefit=DomainObject.newInstance(context, (String)mapBudgetBenefitInfo.get(DomainConstants.SELECT_ID));
			busSelect.add(SELECT_INTERVAL_PLANNED_AMOUNT);
			busSelect.add(SELECT_INTERVAL_PLANNED_AMOUNT_UNIT);
			busSelect.add(SELECT_INTERVAL_DATE);
			if(isCostItem){
				busSelect.add(SELECT_PHASE_ID);

			}
			MapList mlBenefitItems = domBenefit.getRelatedObjects(
					context,
					strRelationshipType,
					strItemTypePattern,
					busSelect,
					relSelect,
					false,
					true,
					(short)1,
					null,
					DomainConstants.EMPTY_STRING,
					0);

			Map mapBenItemInfo=null;

			for (Iterator itrBenItem = mlBenefitItems.iterator(); itrBenItem.hasNext();)
			{
				mapBenItemInfo = (Map) itrBenItem.next();


				//If Person's preferred currency not equal to filter selected Currency then the cost items will not be editable
				if(null != strSelectedCurrency && !strSelectedCurrency.equals(strPrefferedCurrency)){
					mapBudgetBenefitInfo.put("RowEditable", "readonly");
				}

				//This will disable the Actual View table
				if(isCostItem)
				{
					if(actualView.equals(selecteView)){
						mapBenItemInfo.put("RowEditable", "readonly");
					}
				}

				relPlannedBenefitValues=mapBenItemInfo.get(SELECT_INTERVAL_PLANNED_AMOUNT);
				relPlannedBenefitValuesUnits = mapBenItemInfo.get(SELECT_INTERVAL_PLANNED_AMOUNT_UNIT);
				if(!"Project Phase".equals(strInterval)) {
					intervalDateValues=mapBenItemInfo.get(SELECT_INTERVAL_DATE);
				}
				else {
					intervalDateValues=mapBenItemInfo.get(SELECT_PHASE_ID);
				}

				if(relPlannedBenefitValues!=null)
				{
					if(relPlannedBenefitValues instanceof StringList)
					{

						slRelPlannedBenefitValues=(StringList)relPlannedBenefitValues;
						slIntervalDateValues=(StringList)intervalDateValues;

						slRelPlannedBenefitValuesUnits=(StringList)relPlannedBenefitValuesUnits;
					}
					else
					{

						slRelPlannedBenefitValues.clear();
						slRelPlannedBenefitValuesUnits.clear();
						slIntervalDateValues.clear();
						slRelPlannedBenefitValues.add((String)relPlannedBenefitValues);
						slRelPlannedBenefitValuesUnits.add((String)relPlannedBenefitValuesUnits);
						slIntervalDateValues.add((String)intervalDateValues);

					}
				}
				if(slRelPlannedBenefitValues!=null)
				{
					int slSize=slRelPlannedBenefitValues.size();

					for(int i=0;i<slSize;i++)
					{
						String strPB=(String)slRelPlannedBenefitValues.get(i);
						String strID=(String)slIntervalDateValues.get(i);
						String strPBUnit=(String)slRelPlannedBenefitValuesUnits.get(i);

						if(!"Project Phase".equals(strInterval)) {
							strID=strID.substring(0,strID.indexOf(" "));
						}
						else{
							if("PMCProjectBudgetReportTable".equals(strViewTableType)){
								DomainObject dmoObj = DomainObject.newInstance(context, strID);
								if(dmoObj.isKindOf(context, TYPE_PHASE)){
									strID = "Phase_"+strID;
								}
							}else{
								strID = strID;
							}
						}
						//Added 27-Sep-2010:PRG:RG6:IR-070662V6R2011x
						double dblItemAmntInDestUnit;
						if(mapBudgetBenefitInfo.containsKey(strID))
						{
							String strDestUnit = (String)mapBudgetBenefitInfo.get(strID+"_Unit");
							String strPartialAmount = (String)mapBudgetBenefitInfo.get(strID);
							///TEST
							dblItemAmntInDestUnit = 0.0;
							dblItemAmntInDestUnit = Task.parseToDouble(strPB);
							try {
								dblItemAmntInDestUnit = convertAmount(context, Task.parseToDouble(strPB), strPBUnit, strDestUnit, new Date(), true);
							} catch (Exception exp) {
								showNoExchRateWarning = true;
								notice = exp.getMessage();
							}

							double plannedBenefit=Task.parseToDouble(strPartialAmount) + dblItemAmntInDestUnit;
							  plannedBenefit=(Double)Task.parseToDouble(twoDForm.format(plannedBenefit));
							mapBudgetBenefitInfo.put(strID,plannedBenefit+DomainConstants.EMPTY_STRING);

						}
						else
						{
							dblItemAmntInDestUnit = Task.parseToDouble(strPB);
							try {
								//NZF
								if("PMCProjectBudgetReportTable".equals(strViewTableType) && null==strSelectedCurrency){
									strSelectedCurrency = strPrefferedCurrency;
								}//NZF END
								dblItemAmntInDestUnit = convertAmount(context, dblItemAmntInDestUnit, strPBUnit, strSelectedCurrency, new Date(), true);
								mapBudgetBenefitInfo.put(strID,dblItemAmntInDestUnit+DomainConstants.EMPTY_STRING);
								mapBudgetBenefitInfo.put(strID+"_Unit",strSelectedCurrency);
							}catch(Exception e){
								showNoExchRateWarning = true;
								notice = e.getMessage();
								mapBudgetBenefitInfo.put(strID,strPB);
								mapBudgetBenefitInfo.put(strID+"_Unit",strPBUnit);
							}
						}
						//End Added 27-Sep-2010:PRG:RG6:IR-070662V6R2011x
					}
				}
			}
		}

		if (showNoExchRateWarning) {
			MqlUtil.mqlCommand(context, "notice $1", true,notice);
		}

		return mlBudgetBenefit ;
	}

	/**
	 * Returns total actual transaction amount for a given cost item(fItemId)
	 * in a given time interval(intervalDate).
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param fItemId The object string id of cost item.
	 * @param strInterval Type of time line interval, MONTHLY/QUARTERLY,WEEKLY.
	 * @param intervalDate The interval start date on which total actual transaction for a
	 * cost item is to be calculated.
	 * @param strPreferredCurrency The user Currency string.
	 * @return Total actual transaction amount for a cost item in a given interval.
	 * @throws Exception if operation fails.
	 */
	public double getIntervalTotalActualCost(Context context,String fItemId,String strInterval,Date intervalDate,String strPreferredCurrency)
			throws Exception{
		final String SYMBOLIC_ATTRIBUTE_TRANSACTION_AMOUNT = "attribute_TransactionAmount";
		final String SYMBOLIC_ATTRIBUTE_TRANSACTION_DATE = "attribute_TransactionDate";
		final String SYMBOLIC_TYPE_ACTUAL_TRANSACTION = "type_ActualTransaction";
		final String SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_ITEM = "relationship_ActualTransactionItem";
		double returnValueAllAT = 0.0;
		double returnValueATForInterval = 0.0;
		try{
			if (fItemId != null && !"".equals(fItemId) ) {
				String strRelActualTransactionItem = PropertyUtil.getSchemaProperty(context,SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_ITEM);
				String strTypeActualTransaction = PropertyUtil.getSchemaProperty(context,SYMBOLIC_TYPE_ACTUAL_TRANSACTION);
				String strATAmount = PropertyUtil.getSchemaProperty(context, SYMBOLIC_ATTRIBUTE_TRANSACTION_AMOUNT);
				String strATDate = PropertyUtil.getSchemaProperty(context, SYMBOLIC_ATTRIBUTE_TRANSACTION_DATE);
				SimpleDateFormat sdf=new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat());
				DomainObject objCostItem = DomainObject.newInstance(context, fItemId);
				StringList objSelect = new StringList(3);
				objSelect.add(DomainObject.SELECT_ID);
				objSelect.add("attribute[" + strATDate + "].value");
				objSelect.add("attribute[" + strATAmount + "].value");
				objSelect.add("attribute[" + strATAmount + "].inputunit");
				MapList objATList = null;
				try{
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					objATList = objCostItem.getRelatedObjects(context,
							strRelActualTransactionItem, 		//String relPattern
							strTypeActualTransaction, 		//String typePattern
							objSelect,          	//StringList objectSelects,
							null,            		//StringList relationshipSelects,
							true,                  //boolean getTo,
							false,                   //boolean getFrom,
							(short)1,               //short recurseToLevel,
							null,          		//String objectWhere,
							null,     		        //String relationshipWhere,
							0,						//int limit
							null,                   //Pattern includeType,
							null,                   //Pattern includeRelationship,
							null);

				}finally{
					ContextUtil.popContext(context);
				}

				if(objATList != null){
					// Start date of interval on which newly created actual transaction date is falling.
					Calendar intervalStart = Calendar.getInstance();
					intervalStart.setTime(intervalDate);
					// Iterate each and every AT object connected to a particular cost item.
					// For each iteration check if AT object date(atDate) is falling on start date(intervalStart),
					// if falling, then add up the actual cost values.
					Calendar atStart = Calendar.getInstance();
					for (Iterator itr=objATList.iterator();itr.hasNext();) {
						Map objectMap = (Map) itr.next();
						String strDate = (String)objectMap.get("attribute[" + strATDate + "].value");
						Date atDate = eMatrixDateFormat.getJavaDate(strDate);
						if(MONTHLY.equalsIgnoreCase(strInterval))
							atDate = Financials.getFiscalMonthIntervalStartDate(atDate);
						else if(WEEKLY.equalsIgnoreCase(strInterval))
							atDate = Financials.getFiscalWeekIntervalStartDate(atDate);
						else if(QUARTERLY.equalsIgnoreCase(strInterval))
							atDate = Financials.getFiscalQuarterIntervalStartDate(atDate);
						atStart.setTime(atDate);

						if(atStart.equals(intervalStart)){
							double dblTransactionAmount = Task.parseToDouble((String)objectMap.get("attribute[" + strATAmount + "].value"));
							String strTransactionCurrency = (String)objectMap.get("attribute[" + strATAmount + "].inputunit");
							if(!strTransactionCurrency.equalsIgnoreCase(strPreferredCurrency)){
								returnValueATForInterval += convertAmount(context, dblTransactionAmount, strTransactionCurrency, strPreferredCurrency, new Date());
							}else{
								returnValueATForInterval +=dblTransactionAmount;
							}
						}
						//returnValueAllAT += Task.parseToDouble((String)objectMap.get("attribute[" + strATAmount + "].value"));
					}
				}
			}else{
				throw (new IllegalArgumentException());
			}
			Map returnMap = new HashMap<String, Double>();
			//returnMap.put("AllATAmount",new Double(returnValueAllAT));
			//returnMap.put("ATForIntervalAmount",new Double(returnValueATForInterval));
			//return returnMap;
			return returnValueATForInterval;
		}catch(Exception e){
			throw e;
		}
	}
	/**
	 * Gets Project benefit table data for Project Benefits
	 *
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */
	public MapList getTableProjectBudgetOrBenefitData(Context context, String[] args,boolean isCostItem) throws Exception
	{
		try
		{
			if(isCostItem){
				return getTableProjectBudgetData(context,args,isCostItem);
			}else{
				return getTableProjectBenefitData(context,args,isCostItem);
			}
		}
		catch (Exception exp)
		{
			exp.printStackTrace();
			throw new MatrixException(exp);
		}
	}

	public ArrayList getIntervalDateList(Date startDate,Date endDate, String timeLineInterval)
			throws Exception {
		if((Financials.MONTHLY).equals(timeLineInterval))
			return Financials.getMonthlyDateList(startDate,endDate);
		else if((Financials.WEEKLY).equals(timeLineInterval))
			return Financials.getWeeklyDateList(startDate,endDate);
		else if((Financials.QUARTERLY).equals(timeLineInterval))
			return Financials.getQuarterlyDateList(startDate,endDate);
		else
			return Financials.getAnnualDateList(startDate,endDate);
	}

	/**
	 * Deletes a period(interval) from Budget and Benefit.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args String array that contains request parameters.
	 * @param isCostItem will be true if financial item from which period is being deleted is Budget. It will be false
	 * in case of Benefit.
	 * @throws Exception if operations fail.
	 */
	public void deletePeriod (Context context, String[] args, boolean isCostItem) throws Exception
	{

		Map programMap   = (HashMap) JPO.unpackArgs(args);
		Map mpRequestMap  = (HashMap)programMap.get("requestMap");
		Calendar calStartDate = Calendar.getInstance();
		final String strDateFormat=eMatrixDateFormat.getEMatrixDateFormat();
		int intervalPeriod = Integer.parseInt((String)mpRequestMap.get("addinterval"));
		String deletePeriodFrom = (String)mpRequestMap.get("PeriodTo");
		String strTimeInterval = (String)mpRequestMap.get("TimeLineInterval");

		StringList objectSelects=new StringList();
		objectSelects.add(DomainObject.SELECT_ID);
		String strObjectId = (String)mpRequestMap.get("objectId");
		if(isBackgroundTaskActive(context, strObjectId)){
			getCurrencyModifyBackgroundTaskMessage(context, true);
			return;
		}
		DomainObject dmoProjectSpace=DomainObject.newInstance(context, strObjectId);
		String typePattern=null;
		String itemtypePattern=null;
		String SELECT_BENEFIT_COST_INTERVAL_START_DATE=null;
		String SELECT_BENEFIT_COST_INTERVAL_END_DATE=null;
		String SELECT_INTERVAL=null;
		if(isCostItem){
			typePattern=TYPE_BUDGET;
			itemtypePattern=TYPE_COST_ITEM;
			SELECT_BENEFIT_COST_INTERVAL_START_DATE=SELECT_COST_INTERVAL_START_DATE;
			SELECT_BENEFIT_COST_INTERVAL_END_DATE=SELECT_COST_INTERVAL_END_DATE;
			SELECT_INTERVAL=SELECT_COST_INTERVAL;
		}
		else{
			typePattern=TYPE_BENEFIT;
			itemtypePattern=TYPE_BENEFIT_ITEM;
			SELECT_BENEFIT_COST_INTERVAL_START_DATE=SELECT_BENEFIT_INTERVAL_START_DATE;
			SELECT_BENEFIT_COST_INTERVAL_END_DATE=SELECT_BENEFIT_INTERVAL_END_DATE;
			SELECT_INTERVAL=SELECT_BENEFIT_INTERVAL;
		}

		objectSelects.add(SELECT_BENEFIT_COST_INTERVAL_START_DATE);
		objectSelects.add(SELECT_BENEFIT_COST_INTERVAL_END_DATE);
		objectSelects.add(SELECT_INTERVAL);
		MapList mlBenefits=dmoProjectSpace.getRelatedObjects(context,RELATIONSHIP_PROJECT_FINANCIAL_ITEM,typePattern,objectSelects,null,false,true,(short)1,null,null);

		DomainObject dmoBenefit=null;

		Map mpBenefit=null;
		try
		{
			for (Iterator itrBenefit = mlBenefits.iterator(); itrBenefit.hasNext();)
			{

				mpBenefit=(Map)itrBenefit.next();
				dmoBenefit=DomainObject.newInstance(context, (String)mpBenefit.get(DomainObject.SELECT_ID));

				objectSelects.clear();
				objectSelects.add(DomainConstants.SELECT_ID);
				objectSelects.add(SELECT_BENEFIT_INTERVAL_ITEM_DATA_ID);

				MapList mlBenefitItems=dmoBenefit.getRelatedObjects(context,RELATIONSHIP_FINANCIAL_ITEMS,itemtypePattern,objectSelects,null,false,true,(short)1,null,null);
				String strBIStartDate=(String)mpBenefit.get(SELECT_BENEFIT_COST_INTERVAL_START_DATE);
				String strBIEndDate=(String)mpBenefit.get(SELECT_BENEFIT_COST_INTERVAL_END_DATE);
				String strTimeLineInterval=(String)mpBenefit.get(SELECT_INTERVAL);

				Calendar calendar = Calendar.getInstance();
				Date dtBIDate = eMatrixDateFormat.getJavaDate(strBIStartDate);
				calendar.setTime(dtBIDate);
				int startDay=calendar.get(Calendar.DATE);
				if(null==mlBenefitItems || mlBenefitItems.size()==0){
					String strNoItemMessageKey = "emxProgramCentral.Benefit.NoBenefitItemPresentDelete";
					if(isCostItem)
						strNoItemMessageKey = "emxProgramCentral.Budget.NoCostItemPresentDelete";
					String errMessage = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
							strNoItemMessageKey, context.getSession().getLanguage());
					MqlUtil.mqlCommand(context, "notice " + errMessage);
					return;
				}
				String strPreferredCurrency = Currency.getBaseCurrency(context,strObjectId);
				if(deletePeriodFrom.equalsIgnoreCase("Start")){
					deletePeriodFromStart(context,strPreferredCurrency,intervalPeriod,mlBenefitItems,strBIStartDate,strBIEndDate,strTimeLineInterval,dmoBenefit,startDay,isCostItem);
				}
				else{
					deletePeriodFromEnd(context,strPreferredCurrency,intervalPeriod,mlBenefitItems, strBIStartDate, strBIEndDate, strTimeLineInterval, dmoBenefit,startDay,isCostItem);
				}

			}//for loop end
		} catch(Exception exp) {
			throw new Exception(exp);
		}


	}

	/**
	 * Deletes a set of intervals(periods) from the financial item(Budget or Benefit) depending upon the
	 * number of <code>intervalPeriod</code> passed.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param intervalPeriod number of intervals that are marked for deletion.
	 * @param mlBenefitItems
	 * @param strBIStartDate
	 * @param strBIEndDate
	 * @param strTimeLineInterval
	 * @param dmoBenefit
	 * @param startDay
	 * @param isCostItem
	 * @throws Exception
	 */
	private void deletePeriodFromStart(Context context,String strPreferredCurrency, int intervalPeriod, MapList mlBenefitItems, String strBIStartDate,String strBIEndDate, String strTimeLineInterval, DomainObject dmoBenefit,int startDay, boolean isCostItem) throws Exception
	{
		try{
			Calendar calendar=Calendar.getInstance();
			Date dtBIDate=null;
			Date nextDate = null;
			DateFormat formatter = DateFormat.getDateInstance(eMatrixDateFormat.getEMatrixDisplayDateFormat(), Locale.US);
			Map benefitItem=null;
			String date=null;
			Interval interval = null;
			Date traversedIntervalDate = null;
			StringList relSelects=new StringList();
			String relPattern=null;
			String SELECT_PLANNED_BENEFIT_COST=null;
			String SELECT_INTERVAL_DATE=null;
			String ATTRIBUTE_PLANNED_BENEFIT_COST=null;
			String ATTRIBUTE_BENEFIT_COST_INTERVAL_END_DATE=null;
			String ATTRIBUTE_BENEFIT_COST_INTERVAL_START_DATE=null;
			if(isCostItem){
				relPattern=RELATIONSHIP_COST_ITEM_INTERVAL;
				SELECT_PLANNED_BENEFIT_COST=SELECT_PLANNED_COST;
				SELECT_INTERVAL_DATE=CostItemIntervalRelationship.SELECT_INTERVAL_DATE;
				ATTRIBUTE_PLANNED_BENEFIT_COST=ATTRIBUTE_PLANNED_COST;
				ATTRIBUTE_BENEFIT_COST_INTERVAL_START_DATE=ATTRIBUTE_COST_INTERVAL_START_DATE;
				ATTRIBUTE_BENEFIT_COST_INTERVAL_END_DATE=ATTRIBUTE_COST_INTERVAL_END_DATE;
			}else{
				relPattern=RELATIONSHIP_BENEFIT_ITEM_INTERVAL;
				SELECT_PLANNED_BENEFIT_COST=SELECT_PLANNED_BENEFIT;
				SELECT_INTERVAL_DATE=BenefitItemIntervalRelationship.SELECT_INTERVAL_DATE;
				ATTRIBUTE_PLANNED_BENEFIT_COST=ATTRIBUTE_PLANNED_BENEFIT;
				ATTRIBUTE_BENEFIT_COST_INTERVAL_START_DATE=ATTRIBUTE_BENEFIT_INTERVAL_START_DATE;
				ATTRIBUTE_BENEFIT_COST_INTERVAL_END_DATE=ATTRIBUTE_BENEFIT_INTERVAL_END_DATE;
			}
			relSelects.add(DomainRelationship.SELECT_ID);
			relSelects.add(SELECT_PLANNED_BENEFIT_COST);
			relSelects.add(SELECT_INTERVAL_DATE);
			double totalforBudOrBen=0;
			dtBIDate = eMatrixDateFormat.getJavaDate(strBIStartDate);

			for (Iterator itrBI = mlBenefitItems.iterator(); itrBI.hasNext();){
				interval = null;
				nextDate = dtBIDate; // for each cost/benefit item initialize with start date
				benefitItem = (Map) itrBI.next();
				String strBIId=(String)benefitItem.get(DomainConstants.SELECT_ID);
				DomainObject dmoItem=DomainObject.newInstance(context, strBIId);
				MapList mlIntervalItemData = dmoItem.getRelatedObjects(context,relPattern,TYPE_INTERVAL_ITEM_DATA,null,relSelects,false,true,(short)1,null,null);
				ArrayList alDisconnectRelIds=new ArrayList();
				double totalvalue=0;
				if(strTimeLineInterval.equals(MONTHLY))
					nextDate = Financials.getFiscalMonthIntervalStartDate(nextDate);
				else if(strTimeLineInterval.equals(WEEKLY))
					nextDate = Financials.getFiscalWeekIntervalStartDate(nextDate);
				else if(strTimeLineInterval.equals(QUARTERLY))
					nextDate = Financials.getFiscalQuarterIntervalStartDate(nextDate);
				for(int cnt = intervalPeriod;cnt>0;cnt--){
					Map mpItmIntervalData=null;
					String strRelIdToBeRemove=null;
					String strPlannedValue=null;
					totalvalue=0;
					traversedIntervalDate = null;
					for (Iterator itrIntervalData = mlIntervalItemData.iterator(); itrIntervalData.hasNext();)
					{
						mpItmIntervalData = (Map) itrIntervalData.next();
						String strIntervalDate=(String)mpItmIntervalData.get(SELECT_INTERVAL_DATE);
						strPlannedValue=(String)mpItmIntervalData.get(SELECT_PLANNED_BENEFIT_COST);
						traversedIntervalDate = eMatrixDateFormat.getJavaDate(strIntervalDate);
						strRelIdToBeRemove=(String)mpItmIntervalData.get(DomainRelationship.SELECT_ID);
						if(nextDate.equals(traversedIntervalDate)){
							alDisconnectRelIds.add(strRelIdToBeRemove);
						}else if(!alDisconnectRelIds.contains(strRelIdToBeRemove)){
							totalvalue=totalvalue+Task.parseToDouble(strPlannedValue);
						}
					}
					if(strTimeLineInterval.equals(MONTHLY))
						interval = Financials.getNextFiscalInterval(nextDate, IntervalType.MONTHLY);
					else if(strTimeLineInterval.equals(WEEKLY))
						interval = Financials.getNextFiscalInterval(nextDate, IntervalType.WEEKLY);
					else if(strTimeLineInterval.equals(QUARTERLY))
						interval = Financials.getNextFiscalInterval(nextDate, IntervalType.QUARTERLY);
					nextDate = interval.getStartDate();
				}
				String[] strRelIds = (String[])alDisconnectRelIds.toArray(new String[alDisconnectRelIds.size()]);
				DomainRelationship.disconnect(context,strRelIds);
				dmoItem.setAttributeValue(context,ATTRIBUTE_PLANNED_BENEFIT_COST,totalvalue + ProgramCentralConstants.SPACE + strPreferredCurrency);
				totalforBudOrBen = totalforBudOrBen + totalvalue;
			}
			dtBIDate = interval.getStartDate();
			calendar.setTime(dtBIDate);
			date = formatter.format(calendar.getTime());
			Date dtStartDate = eMatrixDateFormat.getJavaDate(strBIStartDate);
			HashMap attributeMap=new HashMap();
			attributeMap.put(ATTRIBUTE_BENEFIT_COST_INTERVAL_START_DATE,date);
			attributeMap.put(ATTRIBUTE_PLANNED_BENEFIT_COST,totalforBudOrBen + ProgramCentralConstants.SPACE + strPreferredCurrency);
			dmoBenefit.setAttributeValues(context,attributeMap);
		}catch(Exception e){
			throw new MatrixException(e);
		}
	}


	private void deletePeriodFromEnd(Context context, String strPreferredCurrency, int intervalPeriod, MapList mlBenefitItems, String strBIStartDate, String strBIEndDate, String strTimeLineInterval, DomainObject dmoBenefit,int startDay,boolean isCostItem) throws Exception
	{
		try{
			Calendar calendar=Calendar.getInstance();
			Date dtBIDate=null;
			Date previousDate = null;
			DateFormat formatter = DateFormat.getDateInstance(eMatrixDateFormat.getEMatrixDisplayDateFormat(), Locale.US);
			Map benefitItem=null;
			String date=null;
			Interval interval = null;
			Date traversedIntervalDate = null;
			StringList relSelects=new StringList();
			String relPattern=null;
			String SELECT_PLANNED_BENEFIT_COST=null;
			String SELECT_INTERVAL_DATE=null;
			String ATTRIBUTE_PLANNED_BENEFIT_COST=null;
			String ATTRIBUTE_BENEFIT_COST_INTERVAL_END_DATE=null;
			String ATTRIBUTE_BENEFIT_COST_INTERVAL_START_DATE=null;
			if(isCostItem){
				relPattern=RELATIONSHIP_COST_ITEM_INTERVAL;
				SELECT_PLANNED_BENEFIT_COST=SELECT_PLANNED_COST;
				SELECT_INTERVAL_DATE=CostItemIntervalRelationship.SELECT_INTERVAL_DATE;
				ATTRIBUTE_PLANNED_BENEFIT_COST=ATTRIBUTE_PLANNED_COST;
				ATTRIBUTE_BENEFIT_COST_INTERVAL_START_DATE=ATTRIBUTE_COST_INTERVAL_START_DATE;
				ATTRIBUTE_BENEFIT_COST_INTERVAL_END_DATE=ATTRIBUTE_COST_INTERVAL_END_DATE;
			}else{
				relPattern=RELATIONSHIP_BENEFIT_ITEM_INTERVAL;
				SELECT_PLANNED_BENEFIT_COST=SELECT_PLANNED_BENEFIT;
				SELECT_INTERVAL_DATE=BenefitItemIntervalRelationship.SELECT_INTERVAL_DATE;
				ATTRIBUTE_PLANNED_BENEFIT_COST=ATTRIBUTE_PLANNED_BENEFIT;
				ATTRIBUTE_BENEFIT_COST_INTERVAL_START_DATE=ATTRIBUTE_BENEFIT_INTERVAL_START_DATE;
				ATTRIBUTE_BENEFIT_COST_INTERVAL_END_DATE=ATTRIBUTE_BENEFIT_INTERVAL_END_DATE;
			}
			relSelects.add(DomainRelationship.SELECT_ID);
			relSelects.add(SELECT_PLANNED_BENEFIT_COST);
			relSelects.add(SELECT_INTERVAL_DATE);
			double totalforBudOrBen=0;
			dtBIDate = eMatrixDateFormat.getJavaDate(strBIEndDate);
			for (Iterator itrBI = mlBenefitItems.iterator(); itrBI.hasNext();){
				interval = null;
				previousDate = dtBIDate; // for each cost/benefit item initialize with end date
				benefitItem = (Map) itrBI.next();
				String strBIId=(String)benefitItem.get(DomainConstants.SELECT_ID);
				DomainObject dmoItem=DomainObject.newInstance(context, strBIId);
				MapList mlIntervalItemData = dmoItem.getRelatedObjects(context,relPattern,TYPE_INTERVAL_ITEM_DATA,null,relSelects,false,true,(short)1,null,null);
				ArrayList alDisconnectRelIds=new ArrayList();
				double totalvalue=0;
				if(strTimeLineInterval.equals(MONTHLY))
					previousDate = Financials.getFiscalMonthIntervalStartDate(previousDate);
				else if(strTimeLineInterval.equals(WEEKLY))
					previousDate = Financials.getFiscalWeekIntervalStartDate(previousDate);
				else if(strTimeLineInterval.equals(QUARTERLY))
					previousDate = Financials.getFiscalQuarterIntervalStartDate(previousDate);

				for(int cnt = intervalPeriod;cnt>0;cnt--){
					Map mpItmIntervalData=null;
					String strRelIdToBeRemove=null;
					String strPlannedValue=null;
					totalvalue=0;
					traversedIntervalDate = null;
					for (Iterator itrIntervalData = mlIntervalItemData.iterator(); itrIntervalData.hasNext();)
					{
						mpItmIntervalData = (Map) itrIntervalData.next();
						String strIntervalDate=(String)mpItmIntervalData.get(SELECT_INTERVAL_DATE);
						strPlannedValue=(String)mpItmIntervalData.get(SELECT_PLANNED_BENEFIT_COST);
						traversedIntervalDate = eMatrixDateFormat.getJavaDate(strIntervalDate);
						strRelIdToBeRemove=(String)mpItmIntervalData.get(DomainRelationship.SELECT_ID);
						if(previousDate.equals(traversedIntervalDate)){
							alDisconnectRelIds.add(strRelIdToBeRemove);
						}else if(!alDisconnectRelIds.contains(strRelIdToBeRemove)){
							totalvalue=totalvalue+Task.parseToDouble(strPlannedValue);
						}
					}
					if(strTimeLineInterval.equals(MONTHLY))
						interval = Financials.getPreviousFiscalInterval(previousDate, IntervalType.MONTHLY);
					else if(strTimeLineInterval.equals(WEEKLY))
						interval = Financials.getPreviousFiscalInterval(previousDate, IntervalType.WEEKLY);
					else if(strTimeLineInterval.equals(QUARTERLY))
						interval = Financials.getPreviousFiscalInterval(previousDate, IntervalType.QUARTERLY);
					previousDate = interval.getStartDate();
				}
				String[] strRelIds = (String[])alDisconnectRelIds.toArray(new String[alDisconnectRelIds.size()]);
				DomainRelationship.disconnect(context,strRelIds);
				dmoItem.setAttributeValue(context,ATTRIBUTE_PLANNED_BENEFIT_COST,totalvalue + ProgramCentralConstants.SPACE + strPreferredCurrency);
				totalforBudOrBen = totalforBudOrBen + totalvalue;
			}
			dtBIDate = interval.getEndDate();
			calendar.setTime(dtBIDate);
			date = formatter.format(calendar.getTime());
			Date dtStartDate = eMatrixDateFormat.getJavaDate(strBIStartDate);
			HashMap attributeMap=new HashMap();
			attributeMap.put(ATTRIBUTE_BENEFIT_COST_INTERVAL_END_DATE,date);
			attributeMap.put(ATTRIBUTE_PLANNED_BENEFIT_COST,totalforBudOrBen + ProgramCentralConstants.SPACE + strPreferredCurrency);
			if(dtBIDate.before(dtStartDate))
				attributeMap.put(ATTRIBUTE_BENEFIT_COST_INTERVAL_END_DATE,strBIStartDate);

			dmoBenefit.setAttributeValues(context,attributeMap);
		}catch(Exception e){
			throw new MatrixException(e);
		}
	}


	/**
	 * Adds a period(interval) to Budget and Benefit.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args String array that contains request parameters.
	 * @param isCostItem will be true if financial item to which period is being added is Budget. It will be false
	 * in case of Benefit.
	 * @throws Exception if operations fail.
	 */
	public void addPeriod (Context context, String[] args, boolean isCostItem) throws Exception
	{
		try{
			Map programMap   = (HashMap) JPO.unpackArgs(args);
			Map mpRequestMap  = (HashMap)programMap.get("requestMap");
			final String strDateFormat=eMatrixDateFormat.getEMatrixDateFormat();
			int intervalPeriod = Integer.parseInt((String)mpRequestMap.get("addinterval"));
			String addPeriodTo = (String)mpRequestMap.get("PeriodTo");
			StringList objectSelects=new StringList();
			objectSelects.add(DomainObject.SELECT_ID);
			String strObjectId = (String)mpRequestMap.get("objectId");
			if(isBackgroundTaskActive(context, strObjectId)){
				getCurrencyModifyBackgroundTaskMessage(context, true);
				return;
			}
			DomainObject dmoProjectSpace=DomainObject.newInstance(context,strObjectId);
			String typePattern=null;
			String itemtypePattern=null;
			String SELECT_BENEFIT_COST_INTERVAL_START_DATE=null;
			String SELECT_BENEFIT_COST_INTERVAL_END_DATE=null;
			String SELECT_INTERVAL=null;
			String SELECT_INTERVAL_DATA_ID=null;
			if(isCostItem)
			{
				typePattern=TYPE_BUDGET;
				itemtypePattern=TYPE_COST_ITEM;
				SELECT_BENEFIT_COST_INTERVAL_START_DATE=SELECT_COST_INTERVAL_START_DATE;
				SELECT_BENEFIT_COST_INTERVAL_END_DATE=SELECT_COST_INTERVAL_END_DATE;
				SELECT_INTERVAL=SELECT_COST_INTERVAL;
				SELECT_INTERVAL_DATA_ID=CostItem.SELECT_INTERVAL_ITEM_DATA_ID;
			}
			else
			{
				typePattern=TYPE_BENEFIT;
				itemtypePattern=TYPE_BENEFIT_ITEM;
				SELECT_BENEFIT_COST_INTERVAL_START_DATE=SELECT_BENEFIT_INTERVAL_START_DATE;
				SELECT_BENEFIT_COST_INTERVAL_END_DATE=SELECT_BENEFIT_INTERVAL_END_DATE;
				SELECT_INTERVAL=SELECT_BENEFIT_INTERVAL;
				SELECT_INTERVAL_DATA_ID=SELECT_BENEFIT_INTERVAL_ITEM_DATA_ID;
			}
			StringList slBusSelect = new StringList();
			slBusSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
			Map mapObjInfo = dmoProjectSpace.getInfo(context, slBusSelect);
			String strProjectStartDate = (String) mapObjInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
			Date dtProjectStartDate = eMatrixDateFormat.getJavaDate(strProjectStartDate);
			objectSelects.add(SELECT_BENEFIT_COST_INTERVAL_START_DATE);
			objectSelects.add(SELECT_BENEFIT_COST_INTERVAL_END_DATE);
			objectSelects.add(SELECT_INTERVAL);
			MapList mlBenefits=dmoProjectSpace.getRelatedObjects(context,RELATIONSHIP_PROJECT_FINANCIAL_ITEM, typePattern,objectSelects,null,false,true,(short)1,null,null);

			DomainObject dmoBenefit=null;
			Map mpBenefitItem=null;
			MapList mlBenefitItems=null;


			for (Iterator itrBenefit = mlBenefits.iterator(); itrBenefit.hasNext();)
			{
				mpBenefitItem = (Map) itrBenefit.next();
				dmoBenefit=DomainObject.newInstance(context, (String)mpBenefitItem.get(DomainObject.SELECT_ID));

				objectSelects.clear();
				objectSelects.add(DomainConstants.SELECT_ID);
				objectSelects.add(SELECT_INTERVAL_DATA_ID);

				mlBenefitItems=dmoBenefit.getRelatedObjects(context,RELATIONSHIP_FINANCIAL_ITEMS,itemtypePattern,objectSelects,null,false,true,(short)1,null,null);
				if(mlBenefitItems == null || mlBenefitItems.size()==0){
					String strNoItemMessageKey = "emxProgramCentral.Benefit.NoBenefitItemPresent";
					if(isCostItem)
						strNoItemMessageKey = "emxProgramCentral.Budget.NoCostItemPresent";
					String errMessage = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
							strNoItemMessageKey, context.getSession().getLanguage());
					MqlUtil.mqlCommand(context, "notice " + errMessage);
					return;
				}
				String strBIStartDate=(String)mpBenefitItem.get(SELECT_BENEFIT_COST_INTERVAL_START_DATE);
				String strBIEndDate=(String)mpBenefitItem.get(SELECT_BENEFIT_COST_INTERVAL_END_DATE);
				CalendarType caltype = CalendarType.forName("FISCAL");



				String strTimeLineInterval=(String)mpBenefitItem.get(SELECT_INTERVAL);
				Calendar calendar = Calendar.getInstance();
				Date dtBIDate=null;
				dtBIDate = eMatrixDateFormat.getJavaDate(strBIStartDate);
				calendar.setTime(dtBIDate);
				int startDay=calendar.get(Calendar.DATE);
				String strPrefferedCurrency = Currency.getBaseCurrency(context,strObjectId);
				if(addPeriodTo.equalsIgnoreCase("Start")){
					addPeriodToStart(context,strPrefferedCurrency,intervalPeriod,mlBenefitItems,dmoBenefit,strBIStartDate,strTimeLineInterval, dtProjectStartDate,startDay,isCostItem);
				}else if(addPeriodTo.equalsIgnoreCase("End")){
					addPeriodToEnd(context,strPrefferedCurrency,intervalPeriod,mlBenefitItems,dmoBenefit,strBIEndDate, strTimeLineInterval,startDay,isCostItem);
				}


			}//for loop end
		} catch(Exception exp) {

			throw new MatrixException(exp);
		}
	}

	/**
	 * Adds a period(interval) to the start of the financial item depending upon the type of interval.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param strPrefferedCurrency the user's preferred currency.
	 * @param intervalPeriod the number of periods(intervals) to be added at the start.
	 * @param mlBenefitItems a Map list of Benefit items if Financial item is Benefit
	 * or a Map list of Cost items if Financial item is Budget.
	 * @param dmoBenefit the Financial item domain object Budget or Benefit.
	 * @param strBIStartDate the String start date of Budget(or Benefit).
	 * @param strTimeLineInterval type of Interval MONTHLY/WEEKLY/QUARTERLY.
	 * @param dtProjectStartDate the String start date of project.
	 * @param startDay
	 * @param isCostItem a boolean value that decides if Financial item is Budget or Benefit.
	 * @throws Exception if operations fail.
	 */
	private void addPeriodToStart(Context context, String strPrefferedCurrency, int intervalPeriod, MapList mlBenefitItems,DomainObject dmoBenefit, String strBIStartDate,String strTimeLineInterval, Date dtProjectStartDate,int startDay, boolean isCostItem )throws Exception
	{
		Map benefitItem=null;
		Calendar modifiedCal = Calendar.getInstance();
		Date dtBIDate=null;
		Date previousDate = null;
		DateFormat formatter = DateFormat.getDateInstance(eMatrixDateFormat.getEMatrixDisplayDateFormat(), Locale.US);
		String date=null;
		Interval interval = null;
		StringList relSelects=new StringList();
		String relPattern=null;
		String ATTRIBUTE_BENEFIT_COST_INTERVAL_START_DATE=null;
		String SELECT_INTERVAL_DATA_ID=null;
		String ATTRIBUTE_INTERVAL_COST_BENEFIT_DATE=null;
		String SELECT_ATTRIBUTE_PLANNED_BENEFIT;
		String SELECT_ATTRIBUTE_ACTUAL_BENEFIT;
		String SELECT_ATTRIBUTE_ESTIMATED_BENEFIT;
		dtBIDate = eMatrixDateFormat.getJavaDate(strBIStartDate);
		if(isCostItem){
			relPattern=RELATIONSHIP_COST_ITEM_INTERVAL;
			ATTRIBUTE_BENEFIT_COST_INTERVAL_START_DATE=ATTRIBUTE_COST_INTERVAL_START_DATE;
			SELECT_INTERVAL_DATA_ID=CostItem.SELECT_INTERVAL_ITEM_DATA_ID;
			ATTRIBUTE_INTERVAL_COST_BENEFIT_DATE=CostItemIntervalRelationship.ATTRIBUTE_INTERVAL_DATE;
			SELECT_ATTRIBUTE_PLANNED_BENEFIT = ATTRIBUTE_PLANNED_COST;
			SELECT_ATTRIBUTE_ACTUAL_BENEFIT = ATTRIBUTE_ACTUAL_COST;
			SELECT_ATTRIBUTE_ESTIMATED_BENEFIT = ATTRIBUTE_ESTIMATED_COST;
		}else{
			relPattern=RELATIONSHIP_BENEFIT_ITEM_INTERVAL;
			ATTRIBUTE_BENEFIT_COST_INTERVAL_START_DATE=ATTRIBUTE_BENEFIT_INTERVAL_START_DATE;
			SELECT_INTERVAL_DATA_ID=SELECT_BENEFIT_INTERVAL_ITEM_DATA_ID;
			ATTRIBUTE_INTERVAL_COST_BENEFIT_DATE=BenefitItemIntervalRelationship.ATTRIBUTE_INTERVAL_DATE;
			SELECT_ATTRIBUTE_PLANNED_BENEFIT = ATTRIBUTE_PLANNED_BENEFIT;
			SELECT_ATTRIBUTE_ACTUAL_BENEFIT = ATTRIBUTE_ACTUAL_BENEFIT;
			SELECT_ATTRIBUTE_ESTIMATED_BENEFIT = ATTRIBUTE_ESTIMATED_BENEFIT;
		}

		Calendar calProjectStartDate = Calendar.getInstance();
		calProjectStartDate.setTime(dtProjectStartDate);
		calProjectStartDate.set(Calendar.HOUR,0);
		calProjectStartDate.set(Calendar.HOUR_OF_DAY,0);
		calProjectStartDate.set(Calendar.MINUTE,0);
		calProjectStartDate.set(Calendar.SECOND,0);

		for (Iterator itrBI = mlBenefitItems.iterator(); itrBI.hasNext();)
		{
			interval = null;
			previousDate = dtBIDate;
			benefitItem = (Map) itrBI.next();
			String strBIId=(String)benefitItem.get(DomainConstants.SELECT_ID);
			Object slIntervalItmDataId=benefitItem.get(SELECT_INTERVAL_DATA_ID);
			String strIntervalItmDataId=null;
			if(slIntervalItmDataId instanceof StringList)
				strIntervalItmDataId=(String)((StringList)slIntervalItmDataId).get(0);
			else
				strIntervalItmDataId=(String)slIntervalItmDataId;
			int i = intervalPeriod;
			double defaultVal = 0D;
			while(i>0)
			{
				Map fItemIntervalAttributes = new HashMap();
				if(strTimeLineInterval.equalsIgnoreCase("Monthly"))
					interval = Financials.getPreviousFiscalInterval(previousDate, IntervalType.MONTHLY);
				else if(strTimeLineInterval.equalsIgnoreCase("Quarterly"))
					interval = Financials.getPreviousFiscalInterval(previousDate, IntervalType.QUARTERLY);
				else if(strTimeLineInterval.equalsIgnoreCase("Weekly"))
					interval = Financials.getPreviousFiscalInterval(previousDate, IntervalType.WEEKLY);
				previousDate = interval.getStartDate();
				modifiedCal.setTime(previousDate);
				modifiedCal.set(Calendar.HOUR,0);
				modifiedCal.set(Calendar.HOUR_OF_DAY,0);
				modifiedCal.set(Calendar.MINUTE,0);
				modifiedCal.set(Calendar.SECOND,0);
				date = formatter.format(modifiedCal.getTime());
				DomainRelationship beneItmIntervalId=DomainRelationship.connect(context,DomainObject.newInstance(context, strBIId),relPattern,DomainObject.newInstance(context, strIntervalItmDataId));
				fItemIntervalAttributes.put(ATTRIBUTE_INTERVAL_COST_BENEFIT_DATE, date);
				fItemIntervalAttributes.put(SELECT_ATTRIBUTE_PLANNED_BENEFIT, defaultVal + ProgramCentralConstants.SPACE + strPrefferedCurrency);
				fItemIntervalAttributes.put(SELECT_ATTRIBUTE_ESTIMATED_BENEFIT, defaultVal + ProgramCentralConstants.SPACE + strPrefferedCurrency);
				fItemIntervalAttributes.put(SELECT_ATTRIBUTE_ACTUAL_BENEFIT, defaultVal + ProgramCentralConstants.SPACE + strPrefferedCurrency);
				beneItmIntervalId.setAttributeValues(context,fItemIntervalAttributes);
				//beneItmIntervalId.setAttributeValue(context,ATTRIBUTE_INTERVAL_COST_BENEFIT_DATE,date);
				i--;
			}//while loop end
		}//benefit item for loop end


		// No need to show notice
		/*		if((modifiedCal.getTime()).before(calProjectStartDate.getTime())){
		String strLanguage = context.getSession().getLanguage();
		String sErrMsg =null;
					if(isCostItem){
				sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
						"emxProgramCentral.Budget.CostIntervalStartDateLessThanProjectStartDate", strLanguage);
			}else{
				sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
						"emxProgramCentral.ProjectBenefit.BenefitIntervalStartDateLessThanProjectStartDate", strLanguage);
		}
			MqlUtil.mqlCommand(context, "Notice " + sErrMsg);
	  }*/
		dtBIDate = interval.getStartDate();
		modifiedCal.setTime(dtBIDate);
		date = formatter.format(modifiedCal.getTime());
		dmoBenefit.setAttributeValue(context,ATTRIBUTE_BENEFIT_COST_INTERVAL_START_DATE,date);
	}

	/**
	 * Adds a period(interval) to the end of the financial item depending upon the type of interval.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param strPrefferedCurrency the user's preferred currency.
	 * @param intervalPeriod the number of periods(intervals) to be added at the end.
	 * @param mlBenefitItems a Map list of Benefit items if Financial item is Benefit
	 * or a Map list of Cost items if Financial item is Budget.
	 * @param dmoBenefit the Financial item domain object Budget or Benefit.
	 * @param strBIEndDate the String end date of Budget(or Benefit).
	 * @param strTimeLineInterval type of Interval MONTHLY/WEEKLY/QUARTERLY.
	 * @param startDay
	 * @param isCostItem a boolean value that decides if Financial item is Budget or Benefit.
	 * @throws Exception if operations fail.
	 */
	private void addPeriodToEnd(Context context, String strPrefferedCurrency,int intervalPeriod, MapList mlBenefitItems, DomainObject dmoBenefit, String strBIEndDate,String strTimeLineInterval, int startDay, boolean isCostItem )throws Exception
	{
		Map benefitItem=null;
		Calendar calendar = Calendar.getInstance();
		Date dtBIDate=null;
		Date nextDate = null;
		DateFormat formatter = DateFormat.getDateInstance(eMatrixDateFormat.getEMatrixDisplayDateFormat(), Locale.US);
		String date=null;
		String relPattern=null;
		String ATTRIBUTE_BENEFIT_COST_INTERVAL_END_DATE=null;
		String SELECT_INTERVAL_DATA_ID=null;
		String ATTRIBUTE_INTERVAL_COST_BENEFIT_DATE=null;
		String SELECT_ATTRIBUTE_PLANNED_BENEFIT;
		String SELECT_ATTRIBUTE_ACTUAL_BENEFIT;
		String SELECT_ATTRIBUTE_ESTIMATED_BENEFIT;
		Interval interval = null;

		dtBIDate = eMatrixDateFormat.getJavaDate(strBIEndDate);
		if(isCostItem)
		{
			relPattern=RELATIONSHIP_COST_ITEM_INTERVAL;
			ATTRIBUTE_BENEFIT_COST_INTERVAL_END_DATE=ATTRIBUTE_COST_INTERVAL_END_DATE;
			SELECT_INTERVAL_DATA_ID=CostItem.SELECT_INTERVAL_ITEM_DATA_ID;
			ATTRIBUTE_INTERVAL_COST_BENEFIT_DATE=CostItemIntervalRelationship.ATTRIBUTE_INTERVAL_DATE;
			SELECT_ATTRIBUTE_PLANNED_BENEFIT = ATTRIBUTE_PLANNED_COST;
			SELECT_ATTRIBUTE_ACTUAL_BENEFIT = ATTRIBUTE_ACTUAL_COST;
			SELECT_ATTRIBUTE_ESTIMATED_BENEFIT = ATTRIBUTE_ESTIMATED_COST;
		}
		else
		{
			relPattern=RELATIONSHIP_BENEFIT_ITEM_INTERVAL;
			ATTRIBUTE_BENEFIT_COST_INTERVAL_END_DATE=ATTRIBUTE_BENEFIT_INTERVAL_END_DATE;
			SELECT_INTERVAL_DATA_ID=SELECT_BENEFIT_INTERVAL_ITEM_DATA_ID;
			ATTRIBUTE_INTERVAL_COST_BENEFIT_DATE=BenefitItemIntervalRelationship.ATTRIBUTE_INTERVAL_DATE;
			SELECT_ATTRIBUTE_PLANNED_BENEFIT = ATTRIBUTE_PLANNED_BENEFIT;
			SELECT_ATTRIBUTE_ACTUAL_BENEFIT = ATTRIBUTE_ACTUAL_BENEFIT;
			SELECT_ATTRIBUTE_ESTIMATED_BENEFIT = ATTRIBUTE_ESTIMATED_BENEFIT;
		}

		for (Iterator itrBI = mlBenefitItems.iterator(); itrBI.hasNext();)
		{
			interval = null;
			nextDate = dtBIDate;
			benefitItem = (Map) itrBI.next();			//benefitItem = (Map) mlBenefitItems.get(0);
			String strBIId=(String)benefitItem.get(DomainConstants.SELECT_ID);
			Object slIntervalItmDataId=benefitItem.get(SELECT_INTERVAL_DATA_ID);
			String strIntervalItmDataId=null;
			if(slIntervalItmDataId instanceof StringList)
				strIntervalItmDataId=(String)((StringList)slIntervalItmDataId).get(0);
			else
				strIntervalItmDataId=(String)slIntervalItmDataId;

			int i=intervalPeriod;
			double defaultVal = 0D;
			while(i>0)
			{
				Map fItemIntervalAttributes = new HashMap();
				DomainRelationship beneItmIntervalId=DomainRelationship.connect(context,DomainObject.newInstance(context, strBIId),relPattern,DomainObject.newInstance(context, strIntervalItmDataId));
				if(strTimeLineInterval.equalsIgnoreCase("Monthly"))
					interval = Financials.getNextFiscalInterval(nextDate, IntervalType.MONTHLY);
				else if(strTimeLineInterval.equalsIgnoreCase("Quarterly"))
					interval = Financials.getNextFiscalInterval(nextDate, IntervalType.QUARTERLY);
				else if(strTimeLineInterval.equalsIgnoreCase("Weekly"))
					interval = Financials.getNextFiscalInterval(nextDate, IntervalType.WEEKLY);
				nextDate = interval.getStartDate();
				calendar.setTime(nextDate);
				date = formatter.format(calendar.getTime());
				fItemIntervalAttributes.put(ATTRIBUTE_INTERVAL_COST_BENEFIT_DATE, date);
				fItemIntervalAttributes.put(SELECT_ATTRIBUTE_PLANNED_BENEFIT, defaultVal + ProgramCentralConstants.SPACE + strPrefferedCurrency);
				fItemIntervalAttributes.put(SELECT_ATTRIBUTE_ESTIMATED_BENEFIT, defaultVal + ProgramCentralConstants.SPACE + strPrefferedCurrency);
				fItemIntervalAttributes.put(SELECT_ATTRIBUTE_ACTUAL_BENEFIT, defaultVal + ProgramCentralConstants.SPACE + strPrefferedCurrency);
				//beneItmIntervalId.setAttributeValue(context,ATTRIBUTE_INTERVAL_COST_BENEFIT_DATE,date);
				beneItmIntervalId.setAttributeValues(context,fItemIntervalAttributes);
				i--;
			}
		}
		dtBIDate = interval.getEndDate();
		calendar.setTime(dtBIDate);
		date = formatter.format(calendar.getTime());
		Date dtModifiedDate=eMatrixDateFormat.getJavaDate(date);
		dmoBenefit.setAttributeValue(context,ATTRIBUTE_BENEFIT_COST_INTERVAL_END_DATE,date);
	}
	/**
	 * Checks if Delete Period  command is top be displayed on Financial Items page.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args the String array that carries request parameters data.
	 * @param isCostItem a boolean value helps in recognizing whether Financial item is a Budget
	 * or Benefit object. True denotes Budget.
	 * @return a boolean value. If True, then Delete Period command will be displayed in the page.
	 * @throws Exception if operation fails.
	 */
	public boolean isRemovePeriodAvailable (Context context, String[] args,boolean isCostItem) throws Exception {
		Map programMap = (Map)JPO.unpackArgs(args);
		String strProjObjId = (String) programMap.get("objectId");
		boolean isCommandEnabled= true;
		String attribute_benefit_cost_interval_start_date=null;
		String attribute_benefit_cost_interval_end_date=null;
		String attribute_benefit_cost_interval=null;
		String strTypePattern= "";
		if(isCostItem){
			attribute_benefit_cost_interval_start_date=ATTRIBUTE_COST_INTERVAL_START_DATE;
			attribute_benefit_cost_interval_end_date=ATTRIBUTE_COST_INTERVAL_END_DATE;
			attribute_benefit_cost_interval=ATTRIBUTE_COST_INTERVAL;
			strTypePattern = TYPE_BUDGET;
		}
		else{
			attribute_benefit_cost_interval_start_date=ATTRIBUTE_BENEFIT_INTERVAL_START_DATE;
			attribute_benefit_cost_interval_end_date=ATTRIBUTE_BENEFIT_INTERVAL_END_DATE;
			attribute_benefit_cost_interval=ATTRIBUTE_BENEFIT_INTERVAL;
			strTypePattern = TYPE_BENEFIT;
		}
		DomainObject dmoObject = DomainObject.newInstance(context,strProjObjId);
		String sCommandStatement = "print bus $1 select $2 dump";
		String strBenefitOrBudgetId =  MqlUtil.mqlCommand(context, sCommandStatement,strProjObjId, "from["+RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"| to.type.kindof["+strTypePattern+"]==TRUE].to.id");
		if(ProgramCentralUtil.isNullString(strBenefitOrBudgetId)){
			isCommandEnabled = false;
		}
		else{
			DomainObject dmoBudgetObject = DomainObject.newInstance(context,strBenefitOrBudgetId);
			StringList objSelect=new StringList();
			objSelect.add(attribute_benefit_cost_interval_start_date);
			objSelect.add(attribute_benefit_cost_interval_end_date);
			objSelect.add(attribute_benefit_cost_interval);
			Map mpFinItm=dmoBudgetObject.getAttributeMap(context);
			Date dtStartDate = new Date((String)mpFinItm.get(attribute_benefit_cost_interval_start_date));
			Date dtFinishDate = new Date((String)mpFinItm.get(attribute_benefit_cost_interval_end_date));
			String strTimeLineInterval=(String)mpFinItm.get(attribute_benefit_cost_interval);
			ArrayList dateList =  getIntervalDateList(dtStartDate,dtFinishDate,strTimeLineInterval);
			int deleteIntervalLimit = dateList.size();
			if(deleteIntervalLimit <= 1){
				isCommandEnabled = false;
			}
		}
		return isCommandEnabled;
	}


	/*
	 * This mothod will be invoked on the trigger action when the Budget or benefit
	 * is promoted to Plan Frozen State.
	 */
	public void triggerPlanToEstimatedView(Context context,String[] args) throws MatrixException
	{
		try
		{
			String strBudgetId = args[0];
			String strNextbudgetState = args[1];
			String strLanguage = context.getSession().getLanguage();
			final int TRIGGER_SUCCESS = 0;
			DomainObject dmoBudget = DomainObject.newInstance(context, strBudgetId);

			String strFinancialType = dmoBudget.getInfo(context, SELECT_TYPE);
			String SELECT_INTERVAL_ITEM_PLANNED_COST = "";
			String SELECT_INTERVAL_ITEM_REL_ID = "";
			String SELECT_ITEM_PLANNED_COST = "";
			String strRelationshipType = Financials.RELATIONSHIP_FINANCIAL_ITEMS;
			String ATTRIBUTE_ESTIMATED_AMOUNT=null;
			String strType = "";
			String SELECT_BUDGET_PLANNED_COST = "";
			String SELECT_INTERVAL_ITEM_PLANNED_COST_UNIT = "";
			String SELECT_ITEM_PLANNED_COST_UNIT = "";
			String SELECT_BUDGET_PLANNED_COST_UNIT = "";

			if(TYPE_BUDGET.equals(strFinancialType)){
				SELECT_INTERVAL_ITEM_PLANNED_COST = "from["+RELATIONSHIP_COST_ITEM_INTERVAL+"].attribute[" + ATTRIBUTE_PLANNED_COST +"]";
				SELECT_INTERVAL_ITEM_PLANNED_COST_UNIT = "from["+RELATIONSHIP_COST_ITEM_INTERVAL+"].attribute[" + ATTRIBUTE_PLANNED_COST +"].inputunit";
				SELECT_INTERVAL_ITEM_REL_ID = "from["+RELATIONSHIP_COST_ITEM_INTERVAL+"].id";
				strType = Financials.TYPE_COST_ITEM;
				SELECT_ITEM_PLANNED_COST = "attribute["+Financials.ATTRIBUTE_PLANNED_COST+"]";
				SELECT_ITEM_PLANNED_COST_UNIT = "attribute["+Financials.ATTRIBUTE_PLANNED_COST+"].inputunit";
				ATTRIBUTE_ESTIMATED_AMOUNT=ATTRIBUTE_ESTIMATED_COST;
				SELECT_BUDGET_PLANNED_COST = "attribute["+Financials.ATTRIBUTE_PLANNED_COST+"]";
				SELECT_BUDGET_PLANNED_COST_UNIT = "attribute["+Financials.ATTRIBUTE_PLANNED_COST+"].inputunit";
			}

			if(TYPE_BENEFIT.equals(strFinancialType)){
				SELECT_INTERVAL_ITEM_PLANNED_COST = "from["+RELATIONSHIP_BENEFIT_ITEM_INTERVAL+"].attribute[" + ATTRIBUTE_PLANNED_BENEFIT +"]";
				SELECT_INTERVAL_ITEM_PLANNED_COST_UNIT = "from["+RELATIONSHIP_BENEFIT_ITEM_INTERVAL+"].attribute[" + ATTRIBUTE_PLANNED_BENEFIT +"].inputunit";
				SELECT_INTERVAL_ITEM_REL_ID = "from["+RELATIONSHIP_BENEFIT_ITEM_INTERVAL+"].id";
				strType = Financials.TYPE_BENEFIT_ITEM;
				SELECT_ITEM_PLANNED_COST = "attribute["+Financials.ATTRIBUTE_PLANNED_BENEFIT+"]";
				SELECT_ITEM_PLANNED_COST_UNIT = "attribute["+Financials.ATTRIBUTE_PLANNED_BENEFIT+"].inputunit";
				ATTRIBUTE_ESTIMATED_AMOUNT=ATTRIBUTE_ESTIMATED_BENEFIT;
				SELECT_BUDGET_PLANNED_COST = "attribute["+Financials.ATTRIBUTE_PLANNED_BENEFIT+"]";
				SELECT_BUDGET_PLANNED_COST_UNIT = "attribute["+Financials.ATTRIBUTE_PLANNED_BENEFIT+"].inputunit";
			}


			String strTotalAmount = dmoBudget.getInfo(context, SELECT_BUDGET_PLANNED_COST);
			String strTotalAmountUnit = dmoBudget.getInfo(context, SELECT_BUDGET_PLANNED_COST_UNIT);
			dmoBudget.setAttributeValue(context, ATTRIBUTE_ESTIMATED_AMOUNT, strTotalAmount+" "+strTotalAmountUnit);

			StringList busSelect = new StringList();
			busSelect.add(SELECT_ID);
			busSelect.add(SELECT_NAME);
			busSelect.add(SELECT_ITEM_PLANNED_COST);
			busSelect.add(SELECT_INTERVAL_ITEM_PLANNED_COST);
			busSelect.add(SELECT_ITEM_PLANNED_COST_UNIT);
			busSelect.add(SELECT_INTERVAL_ITEM_PLANNED_COST_UNIT);
			busSelect.add(SELECT_INTERVAL_ITEM_REL_ID);
			StringList relSelect = new StringList();
			String whereClause = "";
			MapList mlCostItems = dmoBudget.getRelatedObjects(
					context,
					strRelationshipType,
					strType,
					busSelect,
					relSelect,
					false,
					true,
					(short)1,
					whereClause,
					DomainConstants.EMPTY_STRING,
					0);


			Map mapCostItemInfo = null;
			String strStateValue = null;

			for (Iterator iterRequest = mlCostItems.iterator(); iterRequest .hasNext();)
			{
				mapCostItemInfo = (Map) iterRequest.next();
				String strPlannedCost = (String)mapCostItemInfo.get(SELECT_ITEM_PLANNED_COST);
				String strPlannedCostUnit = (String)mapCostItemInfo.get(SELECT_ITEM_PLANNED_COST_UNIT);
				String strCostItemId = (String)mapCostItemInfo.get(SELECT_ID);
				DomainObject dmoCostItem = DomainObject.newInstance(context, strCostItemId);
				dmoCostItem.setAttributeValue(context, ATTRIBUTE_ESTIMATED_AMOUNT, strPlannedCost + ProgramCentralConstants.SPACE + strPlannedCostUnit);
				Object objRelId = mapCostItemInfo.get(SELECT_INTERVAL_ITEM_REL_ID);
				Object objIntervalPlannedCost = mapCostItemInfo.get(SELECT_INTERVAL_ITEM_PLANNED_COST);
				Object objIntervalPlannedCostUnit = mapCostItemInfo.get(SELECT_INTERVAL_ITEM_PLANNED_COST_UNIT);
				StringList slRelId = new StringList();
				StringList slIntervalPlannedCost = new StringList();
				StringList slIntervalPlannedCostUnits = new StringList();
				String strIntervalPlannedCost = "";
				String strRelId = "";
				if(objRelId instanceof String){
					slRelId.add((String)objRelId);
				}else{
					slRelId = (StringList)objRelId;
				}

				if(objIntervalPlannedCost instanceof String){
					slIntervalPlannedCost.add((String)objIntervalPlannedCost);
					slIntervalPlannedCostUnits.add((String)objIntervalPlannedCostUnit);
				}else{
					slIntervalPlannedCost = (StringList)objIntervalPlannedCost;
					slIntervalPlannedCostUnits = (StringList)objIntervalPlannedCostUnit;
				}
				DomainObject dmoRel = null;
				for(int i = 0 ;i<slIntervalPlannedCost.size();i++){
					strRelId = (String)slRelId.get(i);
					strIntervalPlannedCost = (String)slIntervalPlannedCost.get(i);
					String strIntervalPlannedCostUnit = (String)slIntervalPlannedCostUnits.get(i);
					DomainRelationship.setAttributeValue(context, strRelId, ATTRIBUTE_ESTIMATED_AMOUNT, strIntervalPlannedCost+" "+strIntervalPlannedCostUnit);

				}

			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new MatrixException(ex);
		}
	}

	protected Map addEstimateTableColumns(Context context,String columnLabel)throws Exception
	{
		Map mapSettings=new HashMap();
		String strLanguage = context.getSession().getLanguage();
		String strVariance = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
				"emxProgramCentral.Financial.Variance", strLanguage);
		mapSettings.put("Registered Suite","ProgramCentral");
		mapSettings.put("Column Type","program");
		mapSettings.put("Editable","false");
		mapSettings.put("Export","true");
		mapSettings.put("Field Type","real");
		mapSettings.put("program","emxProjectBudget");
		mapSettings.put("function","getColumnBudgetData");
		mapSettings.put("Style Program","emxFinancialItem");
		mapSettings.put("Style Function","getBudgetRightAllignedStyleInfo");

		if(columnLabel.equalsIgnoreCase("Amount") || columnLabel.equalsIgnoreCase("%") ){
			mapSettings.put("Group Header",strVariance);
		}

		if(columnLabel.equalsIgnoreCase("%") ){
			mapSettings.put("Style Program","emxFinancialItem");
			mapSettings.put("Style Function","getBudgetStyleInfo");
		}
		return mapSettings;
	}


	public StringList getBudgetStyleInfo(Context context, String[] args)  throws Exception
	{
		try
		{
			StringList slFTEStyles = new StringList();
			// Get object list information from packed arguments
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");

			Map paramList = (Map) programMap.get("paramList");
			Map columnMap = (Map) programMap.get("columnMap");
			int nMonth = 0;
			int nYear  =0;
			String strColumnName = (String) columnMap.get(SELECT_NAME);
			if(null != strColumnName){
				for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
				{
					Map mapObjectInfo = (Map) itrTableRows.next();
					String strColumnValues=(String)mapObjectInfo.get(strColumnName);
					if(null !=strColumnValues && !"NA".equals(strColumnValues)){
						double dblColumnValues = 0;
						dblColumnValues = Task.parseToDouble(strColumnValues);
						if(dblColumnValues > 110 || dblColumnValues < 0){
							slFTEStyles.addElement("BudgetRedBackGroundColor");
						}
						if(dblColumnValues <=100 && dblColumnValues > 0){
							slFTEStyles.addElement("BudgetGreenBackGroundColor");
						}
						if(dblColumnValues > 100 && dblColumnValues <= 110){
							slFTEStyles.addElement("BudgetYellowBackGroundColor");
						}
						if(dblColumnValues == 0){
							//slFTEStyles.addElement("");
							slFTEStyles.addElement("ColumnRightAllign");
						}

					}else{
						//slFTEStyles.addElement("");
						slFTEStyles.addElement("ColumnRightAllign");
					}
				}
			}
			return slFTEStyles;

		} catch (Exception exp) {
			exp.printStackTrace();
			throw exp;
		}
	}



	public StringList getBudgetRightAllignedStyleInfo(Context context, String[] args)  throws Exception
	{
		try
		{
			StringList slFTEStyles = new StringList();
			// Get object list information from packed arguments
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");

			Map paramList = (Map) programMap.get("paramList");
			Map columnMap = (Map) programMap.get("columnMap");
			int nMonth = 0;
			int nYear  =0;
			for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
			{
				Map mapObjectInfo = (Map) itrTableRows.next();
				slFTEStyles.addElement("ColumnRightAllign");
			}
			return slFTEStyles;

		} catch (Exception exp) {
			exp.printStackTrace();
			throw exp;
		}
	}
	/* Decides if the delete benefit command to be displayed or not in action menu
	 *
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @return true if column is to be shown
	 * @throws Exception if operation fails
	 */
	public boolean isDeleteBenefitItemAvailable (Context context, String[] args,boolean isCostItem) throws Exception {

		Map programMap = (Map)JPO.unpackArgs(args);
		String strProjObjId = (String) programMap.get("objectId");
		boolean isCommandEnabled= true;

		String tableName=(String)programMap.get("selectedTable");

		if(tableName==null || tableName.equalsIgnoreCase("PMCProjectBenefitSummaryTable") )
			return true;
		else
			return false;
	}
	/* Check whether user can freeze the benefit/budget or not.
	 * If benefit/budget has already been frozen then command will not display to user
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @return true if column is to be shown
	 * @throws Exception if operation fails
	 */
	public boolean freezeBenefitOrBudgetAccess(Context context, String[] args,boolean isCostItem) throws Exception
	{
		Map programMap = (Map)JPO.unpackArgs(args);
		String strProjObjId = (String) programMap.get("objectId");
		String strTypePattern=TYPE_BENEFIT;
		if(isCostItem)
			strTypePattern= Financials.TYPE_BUDGET;

		String sCommandStatement = "print bus $1 select $2 dump";
		String currentState =  MqlUtil.mqlCommand(context, sCommandStatement,strProjObjId, "from["+RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"| to.type.kindof["+strTypePattern+"]==TRUE].to.current");

		String sPolicyFinancial = PropertyUtil.getSchemaProperty(context,"policy_FinancialItems");
		String sPlanFrozenStateName = PropertyUtil.getSchemaProperty(context,"policy",sPolicyFinancial,"state_PlanFrozen");

		if(sPlanFrozenStateName.equalsIgnoreCase(currentState))
		{
			return false;
		}
		else
			return true;
	}
	/**
	 * This method is used to get Range values for Year Filter
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 	 *
	 * @returns HashMap containing Range values for Assignee role
	 * @throws Exception if the operation fails	 *
	 */
	public Map getBenefitOrBudgetYearRange(Context context, String[] args,boolean isCostItem) throws Exception
	{
		Map programMap = (Map)JPO.unpackArgs(args);
		Map mpRequestMap = (HashMap)programMap.get("requestMap");
		String strProjObjId = (String) mpRequestMap.get("objectId");
		boolean isCommandEnabled= true;

		String attribute_benefit_cost_interval_start_date=null;
		String attribute_benefit_cost_interval_end_date=null;
		int years=0;

		if(isCostItem)
		{
			attribute_benefit_cost_interval_start_date=ATTRIBUTE_COST_INTERVAL_START_DATE;
			attribute_benefit_cost_interval_end_date=ATTRIBUTE_COST_INTERVAL_END_DATE;
		}
		else
		{
			attribute_benefit_cost_interval_start_date=ATTRIBUTE_BENEFIT_INTERVAL_START_DATE;
			attribute_benefit_cost_interval_end_date=ATTRIBUTE_BENEFIT_INTERVAL_END_DATE;
		}

		String strTypePattern=TYPE_BENEFIT;
		if(isCostItem)
			strTypePattern= Financials.TYPE_BUDGET;

		String sCommandStatement = "print bus $1 select $2 dump";
		String strBudgetId =  MqlUtil.mqlCommand(context, sCommandStatement,strProjObjId, "from["+RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"| to.type.kindof["+strTypePattern+"]==TRUE].to.id");

		if(null != strBudgetId && !"null".equals(strBudgetId) && !"".equals(strBudgetId)){
			DomainObject dmoBudgetObject = DomainObject.newInstance(context,strBudgetId);

			StringList objSelect=new StringList();
			objSelect.add(attribute_benefit_cost_interval_start_date);
			objSelect.add(attribute_benefit_cost_interval_end_date);


			Map mpFinItm=dmoBudgetObject.getAttributeMap(context);
			Date dtStartDate = new Date((String)mpFinItm.get(attribute_benefit_cost_interval_start_date));
			Date dtFinishDate = new Date((String)mpFinItm.get(attribute_benefit_cost_interval_end_date));
			ArrayList dateList =  getIntervalDateList(dtStartDate,dtFinishDate,MONTHLY);
			int nYearTimeframe=0,tmpyear=0;
			for (Iterator itrDateList = dateList.iterator(); itrDateList.hasNext();)
			{
				Date dtIntervalDate=(Date)itrDateList.next();

				//Calendar intervalCalendar=Calendar.getInstance();
				//intervalCalendar.setTime(dtIntervalDate);
				//nYearTimeframe=intervalCalendar.get(Calendar.YEAR);
				nYearTimeframe = Financials.getFiscalYear(dtIntervalDate);
				if(nYearTimeframe!=tmpyear)
				{
					years++;
				}
				tmpyear=nYearTimeframe;
			}
		}
		Map rangeMap = new HashMap();
		StringList slDisplayList = new StringList();
		StringList slOriginalList = new StringList();
		//Modified:14-Mar-2011:hp5:R211:PRG
		String strLanguage = context.getSession().getLanguage();
		String i18nYear = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
				"emxProgramCentral.Common.Year", strLanguage);
		for(int i = years; i>=1;i--) {
			slDisplayList.add(String.valueOf(i)+" "+i18nYear);
			slOriginalList.add(String.valueOf(i)+" Year");
		}
		//End:14-Mar-2011:hp5:R211:PRG
		rangeMap.put("field_choices",slOriginalList);
		rangeMap.put("field_display_choices",slDisplayList);
		return  rangeMap;

	}

	/**
	 * Check whether "Freeze Benefit/Budget" and "Delete Benefit/Budget Item" command will be
	 * visible to user or not
	 * If benefit/budget has already been frozen then command will not display to user
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @return true if column is to be shown
	 * @throws Exception if operation fails
	 */
	public boolean isCurrentStateFrozen(Context context,String args[],boolean isCostItem) throws Exception
	{
		Map programMap = (Map)JPO.unpackArgs(args);
		String strProjectObjId = (String) programMap.get("objectId");

		// the code for RMB is modified  in order to find the project id from the object at any level in
		// SB- hierarchy (from financial item, cost item)
		String isRMB = (String) programMap.get("isRMB");
		String sTempObjId = (String) programMap.get("objectId");
		if(null != isRMB && "true".equalsIgnoreCase(isRMB.trim())){
			DomainObject dObj = DomainObject.newInstance(context);
			String sRmbTableRowId = (String) programMap.get("rmbTableRowId");
			Map sBenefitItemRowId = ProgramCentralUtil.parseTableRowId(context, sRmbTableRowId);
			sTempObjId = (String)sBenefitItemRowId.get("objectId");
			if(ProgramCentralUtil.isNotNullString(sTempObjId)){
				String sFinantialItemObjId = getFinantialItemFromBenefitCostItem(context,sTempObjId);  // method to Fin item id from the benefit item

				if(ProgramCentralUtil.isNotNullString(sFinantialItemObjId)){
					dObj.setId(sFinantialItemObjId);
					final String SELECT_PROJECT ="to["+RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"].from.id";

					StringList slFinacialItemSelects = new StringList();
					slFinacialItemSelects.add(SELECT_IS_BUDGET);
					slFinacialItemSelects.add(SELECT_IS_BENEFIT);
					slFinacialItemSelects.add(SELECT_PROJECT);

					Map mapFinancialItemInfo = dObj.getInfo(context, slFinacialItemSelects);

					if(null != mapFinancialItemInfo){
						String isBudget = (String)mapFinancialItemInfo.get(SELECT_IS_BUDGET);
						String isBenefit = (String)mapFinancialItemInfo.get(SELECT_IS_BENEFIT);

						if( (null != isBudget && "true".equalsIgnoreCase(isBudget.trim())) || (null != isBenefit && "true".equalsIgnoreCase(isBenefit.trim())) ){
							String strTempProjectId = (String)mapFinancialItemInfo.get(SELECT_PROJECT);

							if(ProgramCentralUtil.isNotNullString(strTempProjectId)){
								strProjectObjId = strTempProjectId;
							}
						}
					}

				}else{
					return false;   //Empty financial object is returned, so type is Root node. Check method getFinantialItemFromBenefitCostItem
				}
			}
		}

		String strTypePattern=TYPE_BENEFIT;
		if(isCostItem)
			strTypePattern=TYPE_BUDGET;

		String sCommandStatement = "print bus $1 select $2 dump";
		String currentState =  MqlUtil.mqlCommand(context, sCommandStatement,strProjectObjId, "from["+RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"| to.type.kindof["+strTypePattern+"]==TRUE].to.current");

		String sPolicyFinancial = PropertyUtil.getSchemaProperty(context,"policy_FinancialItems");
		String sPlanFrozenStateName = PropertyUtil.getSchemaProperty(context,"policy",sPolicyFinancial,"state_PlanFrozen");
		if(sPlanFrozenStateName.equals(currentState))
			return false;
		else
			return true;

	}

	/**
	 * This method is used to get financial Item object id from the Cost Item or Benefit Item Id
	 * @param context the eMatrix <code>Context</code> object
	 * @param String strBenefitCostItemId:Benefit/cost Item Id
	 * @returns String  financial Item Id
	 * @throws Exception if the operation fails	 *
	 */
	private String getFinantialItemFromBenefitCostItem(Context context, String strBenefitCostItemId) throws MatrixException{
		String strFinancialItemId = "";

		if(strBenefitCostItemId == null || "Null".equalsIgnoreCase(strBenefitCostItemId) || "".equalsIgnoreCase(strBenefitCostItemId)){
			throw new IllegalArgumentException("Benefit Item object id is null");
		}

		final String SELECT_FINANCIAL_ITEM ="to["+RELATIONSHIP_FINANCIAL_ITEMS+"].from.id";

		DomainObject dObj = DomainObject.newInstance(context);
		dObj.setId(strBenefitCostItemId);
		StringList slFinantialItemSelects = new StringList();
		slFinantialItemSelects.add(ProgramCentralConstants.SELECT_IS_BENEFIT_ITEM);
		slFinantialItemSelects.add(ProgramCentralConstants.SELECT_IS_COST_ITEM);
		slFinantialItemSelects.add(ProgramCentralConstants.SELECT_IS_BUDGET);
		slFinantialItemSelects.add(ProgramCentralConstants.SELECT_IS_BENEFIT);
		slFinantialItemSelects.add(SELECT_FINANCIAL_ITEM);

		Map mapFinancialItemInfo = dObj.getInfo(context, slFinantialItemSelects);

		if(null != mapFinancialItemInfo){
			String isBudget = (String)mapFinancialItemInfo.get(ProgramCentralConstants.SELECT_IS_BUDGET);
			String isBenefit = (String)mapFinancialItemInfo.get(ProgramCentralConstants.SELECT_IS_BENEFIT);

			if( (null != isBudget && "true".equalsIgnoreCase(isBudget.trim())) || (null != isBenefit && "true".equalsIgnoreCase(isBenefit.trim())) ){
				strFinancialItemId = strBenefitCostItemId;
			}
			else{
				String isBenefitItemType = (String)mapFinancialItemInfo.get(ProgramCentralConstants.SELECT_IS_BENEFIT_ITEM);
				String isCostItemType = (String)mapFinancialItemInfo.get(ProgramCentralConstants.SELECT_IS_COST_ITEM);
				if( ( null != isBenefitItemType && "true".equalsIgnoreCase(isBenefitItemType.trim()) ) || ( null != isCostItemType && "true".equalsIgnoreCase(isCostItemType.trim()) ) ){
					String strTempId = (String)mapFinancialItemInfo.get(SELECT_FINANCIAL_ITEM);
					if(null != strTempId && !"Null".equalsIgnoreCase(strTempId.trim()) && !"".equalsIgnoreCase(strTempId.trim()))
						strFinancialItemId = strTempId;
				}
			}
		}

		return strFinancialItemId;
	}


	/**
	 * Rollup of all cost items and/or benefit items.
	 * @param context the eMatrix <code>Context</code> object
	 * @param updateCost true if Cost Items be rolled up
	 * @param updateBenefit true if Benefit Items be rolled up
	 * @throws FrameworkException if operation fails.
	 * @since AEF 9.5.1.3
	 * @grade 0
	 */
	public void rollupAmountOnDelete(Context context, String args[])
			throws Exception
			{
		Map programMap = (Map)JPO.unpackArgs(args);
		String isCostItem=(String)programMap.get("isCostItem");
		String financialItemId=(String)programMap.get("objectId");
		String baseCurrency = "";
		FinancialItem financialItem = null;
		DomainObject financial = DomainObject.newInstance(context, financialItemId);
		if(financial.isKindOf(context, ProgramCentralConstants.TYPE_FINANCIAL_ITEM)){
			financialItem = new FinancialItem(financialItemId);
			StringList slSelects = new StringList(1);
			slSelects.add(ProgramCentralConstants.SELECT_ID);
			MapList mlProjectInfo = financialItem.getParentInfo(context, slSelects);
			Map mProjectInfo = (Map) mlProjectInfo.get(0);
			String projectId = (String) mProjectInfo.get(SELECT_ID);
			baseCurrency = Currency.getBaseCurrency(context, projectId);
		}
		else
			throw new Exception();

		String planAmount = null;
		String select_planned_amount=null;
		String attribute_planned_amount=null;
		StringList mapSelects = new StringList();
		MapList itemList=new MapList();
		if(isCostItem.equalsIgnoreCase("true")){
			select_planned_amount=SELECT_PLANNED_COST;
			attribute_planned_amount=ATTRIBUTE_PLANNED_COST;
			mapSelects.add (select_planned_amount);
			itemList = CostItem.getCostItems(context,financialItem, mapSelects, null);
		}else{
			select_planned_amount=SELECT_PLANNED_BENEFIT;
			attribute_planned_amount=ATTRIBUTE_PLANNED_BENEFIT;
			mapSelects.add (select_planned_amount);
			itemList = BenefitItem.getBenefitItems(context,financialItem, mapSelects, null);
		}
		Iterator itemListIterator = itemList.iterator();
		double planAmountD = 0;
		while (itemListIterator.hasNext()){
			Map itemMap = (Map) itemListIterator.next();
			planAmount = (String) itemMap.get (select_planned_amount);
			try{
				planAmountD = planAmountD + Task.parseToDouble(planAmount);
			}catch (NumberFormatException nfe){
				throw new FrameworkException (nfe.getMessage());
			}
		}
		String strFinancialValue =  planAmountD + ProgramCentralConstants.SPACE + baseCurrency;
		financialItem.setAttributeValue(context, attribute_planned_amount, strFinancialValue);
			}
	/**
	 * This method is used to get Range values for Currency Filter
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 	 *
	 * @returns HashMap containing Range values for Assignee role
	 * @throws Exception if the operation fails	 *
	 */
	public Map getBenefitOrBudgetCurrencyFilterRange(Context context, String[] args) throws Exception
	{
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			Map requestMap = (Map) programMap.get("requestMap");
			String objectId = (String)requestMap.get("objectId");
			String strSelectedCurrency = "";
			if(requestMap.containsKey("PMCProjectBudgetCurrencyFilter")){
				strSelectedCurrency = (String) requestMap.get("PMCProjectBudgetCurrencyFilter");
				StringList slCurrencyData = FrameworkUtil.splitString(strSelectedCurrency, "~");
				strSelectedCurrency = slCurrencyData.get(0).toString();
			}else if(requestMap.containsKey("PMCExpenseReportCurrencyFilter")){
				strSelectedCurrency = (String) requestMap.get("PMCExpenseReportCurrencyFilter");
				StringList slCurrencyData = FrameworkUtil.splitString(strSelectedCurrency, "~");
				strSelectedCurrency = slCurrencyData.get(0).toString();
			}else if(requestMap.containsKey("PMCProjectBenefitCurrencyFilter")){
				strSelectedCurrency = (String) requestMap.get("PMCProjectBenefitCurrencyFilter");
				StringList slCurrencyData = FrameworkUtil.splitString(strSelectedCurrency, "~");
				strSelectedCurrency = slCurrencyData.get(0).toString();
			}else if(requestMap.containsKey("PMCActualTransactionCurrencyFilter")){
				strSelectedCurrency = (String) requestMap.get("PMCActualTransactionCurrencyFilter");
				StringList slCurrencyData = FrameworkUtil.splitString(strSelectedCurrency, "~");
				strSelectedCurrency = slCurrencyData.get(0).toString();
				objectId = (String)requestMap.get("projectId");

			}
			String strLanguage = context.getSession().getLanguage();
			Currency currency = new Currency();
			String strCompanyId = PersonUtil.getUserCompanyId(context);
			Map mConversionRates = currency.getCurrencyConversionMap(context, strCompanyId);
			String strBaseCurrency = currency.getBaseCurrency(context, objectId);
			String strPreferredCurrency = PersonUtil.getCurrency(context);
			//Currency attribute range
			StringList slCurrencyRange = currency.getCurrencyAttributeRange(context);
			slCurrencyRange.remove("Unassigned");
			//Translated Currency attribute range
			StringList slCurrencyRangeTranslated = i18nNow.getAttrRangeI18NStringList(ProgramCentralConstants.ATTRIBUTE_CURRENCY, slCurrencyRange, strLanguage);

			//Currency to be displayed on filter
			String strDisplayCurrency = "";
			//I18N Currency to be displayed on filter
			String strDisplayCurrencyTranslated = "";

			StringList slCurrencyWithRate = new StringList(slCurrencyRange.size());
			StringList slCurrencyTranslated = new StringList(slCurrencyRangeTranslated.size());

			Date currentDate = new Date();
			double  fltRate = 0f;
			double fltRevereseRate = 0f;
			String strCurrencyConversionWarning = "";
			boolean showCurrencyConversionWarning = false;
			for(int index=0; index<slCurrencyRange.size(); index++){
				fltRate = 0f;
				showCurrencyConversionWarning = false;
				strCurrencyConversionWarning = "";
				strDisplayCurrency = (String)slCurrencyRange.get(index);
				strDisplayCurrencyTranslated = (String)slCurrencyRangeTranslated.get(index);
				try{
					//fltRate = Currency.toDisplayCurrency(context, 1, strBaseCurrency, strDisplayCurrency, currentDate, true);
					fltRate = Currency.convert(context, 1, strBaseCurrency, strDisplayCurrency, currentDate, true);
				}catch(Exception e){
					fltRate = 1;
					showCurrencyConversionWarning = true;
					strCurrencyConversionWarning = e.getMessage();
				}
				if(strDisplayCurrency.equals(strPreferredCurrency)){
					if((showCurrencyConversionWarning) && ("".equals(strSelectedCurrency) || strPreferredCurrency.equals(strSelectedCurrency)))
						MqlUtil.mqlCommand(context, "notice " + strCurrencyConversionWarning);
					//slCurrencyWithRate.add(0, strDisplayCurrency+"~"+fltRate+"!"+fltRevereseRate);
					slCurrencyWithRate.add(0, strDisplayCurrency);
					slCurrencyTranslated.add(0,strDisplayCurrencyTranslated);
				}else {
					//if(fltRate == 1)
					if(fltRate == 1 && !strDisplayCurrency.equals(strBaseCurrency))
						continue;
					//slCurrencyWithRate.add(strDisplayCurrency+"~"+fltRate+"!"+fltRevereseRate);
					slCurrencyWithRate.add(strDisplayCurrency);
					slCurrencyTranslated.add(strDisplayCurrencyTranslated);
				}
			}
			Map mFilterCurrencyInfo = new HashMap();
			mFilterCurrencyInfo.put("field_choices", slCurrencyWithRate);
			mFilterCurrencyInfo.put("field_display_choices", slCurrencyTranslated);

			return  mFilterCurrencyInfo;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}



	/**
	 * This method is used to get Range values for Views Filter
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 	 *
	 * @returns HashMap containing Range values for Assignee role
	 * @throws Exception if the operation fails	 *
	 */
	public Map getBenefitOrBudgetViewsFilterRange(Context context, String[] args,boolean isCostItem) throws Exception
	{

		Map programMap = (Map)JPO.unpackArgs(args);
		Map mpRequestMap = (HashMap)programMap.get("requestMap");
		String strProjObjId = (String) mpRequestMap.get("objectId");
		String selectedView = (String) mpRequestMap.get(BENEFITVIEWFILTER);
		Map rangeMap = new HashMap();
		i18nNow loc = new i18nNow();
		String strLanguage=context.getSession().getLanguage();
		String strTypePattern=TYPE_BENEFIT;
		if(isCostItem){
			strTypePattern=TYPE_BUDGET;
			selectedView = (String) mpRequestMap.get(BUDGETVIEWFILTER);
		}

		//PRG:RG6:R213:Mql Injection:parameterized Mql:24-Oct-2011:start
		String sCommandStatement = "print bus $1 select $2 dump";
		String strCurrent =  MqlUtil.mqlCommand(context, sCommandStatement,strProjObjId, "from["+RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"| to.type.kindof["+strTypePattern+"]==TRUE].to.current");
		//PRG:RG6:R213:Mql Injection:parameterized Mql:24-Oct-2011:End

		String views="";
		StringList slDisplayList = new StringList();
		StringList slOriginalList = new StringList();
		String sPolicyFinancial = PropertyUtil.getSchemaProperty(context,"policy_FinancialItems");
		String sPlanFrozenStateName = PropertyUtil.getSchemaProperty(context,"policy",sPolicyFinancial,"state_PlanFrozen");
		final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";



		if(strCurrent.equals(sPlanFrozenStateName))
		{
			views = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.ProjectBenefitOrBudget.ViewEstimate", strLanguage);
			if(!(selectedView!=null && selectedView.equals(views)))
			{
				slDisplayList.add(views);
				slOriginalList.add("Estimate View");
			}

			views = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.ProjectBenefitOrBudget.ViewPlan", strLanguage);
			slDisplayList.add(views);
			slOriginalList.add("Plan View");

			views = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.ProjectBenefitOrBudget.ViewActual", strLanguage);
			if(!(selectedView!=null && selectedView.equals(views))){
				slDisplayList.add(views);
				slOriginalList.add("Actual View");
			}
		}
		else{
			views = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.ProjectBenefitOrBudget.ViewPlan", strLanguage);
			slDisplayList.add(views);
			slOriginalList.add("Plan View");
		}
		rangeMap.put("field_choices",slOriginalList);
		rangeMap.put("field_display_choices",slDisplayList);
		return  rangeMap;
	}
	/**
	 * This method is used to get Currency Conversion map as per the company
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 	 *
	 * @returns HashMap containing Currency exchange information.
	 * @throws Exception if the operation fails	 *
	 */
	public Map getCurrencyConversionMap(Context context, String companyId) throws Exception
	{
		Map conversionMap = new HashMap();
		try{
			StringList objectSelects = new StringList();
			String SELECT_RELATIONSHIP_RATE_PERIOD_ID = "from[" + CurrencyConversion.RELATIONSHIP_RATE_PERIOD + "].id";
			CurrencyConversion currConv = new CurrencyConversion();
			CurrencyConversion.MULTI_VALUE_LIST.add(SELECT_RELATIONSHIP_RATE_PERIOD_ID);
			objectSelects = CurrencyConversion._currencyConversionSelects;
			objectSelects.add( SELECT_RELATIONSHIP_RATE_PERIOD_ID);

			MapList totalresultList = currConv.getCurrencyConversions(context,objectSelects, false,companyId);
			Map mapCurrencyConversion = null;
			String fromCurrency        = null;
			String toCurrency          = null;
			String rate                = null;
			String strFromCurrency        = null;
			String strToCurrency          = null;

			if(!totalresultList.isEmpty())
			{
				Iterator totalItr = totalresultList.iterator();
				while(totalItr.hasNext())
				{
					Map tempMap = new HashMap();
					Object oRates = null;
					Object oDates = null;
					StringList rates           = new StringList();
					StringList dates           = new StringList();
					mapCurrencyConversion = (Map)totalItr.next();
					fromCurrency        = (String)mapCurrencyConversion.get(CurrencyConversion.SELECT_NAME);
					strFromCurrency=fromCurrency.substring(fromCurrency.indexOf("~")+1);
					toCurrency          = (String)mapCurrencyConversion.get(CurrencyConversion.SELECT_REVISION);
					oRates =mapCurrencyConversion.get(CurrencyConversion.SELECT_RATE_PERIOD_RATE);

					oDates=mapCurrencyConversion.get(CurrencyConversion.SELECT_RATE_PERIOD_START_EFFECTIVITY);
					if(oRates!=null && oDates!=null)
					{
						if(oRates instanceof StringList && oDates instanceof StringList )
						{
							rates         = (StringList)mapCurrencyConversion.get(CurrencyConversion.SELECT_RATE_PERIOD_RATE);
							dates         = (StringList)mapCurrencyConversion.get(CurrencyConversion.SELECT_RATE_PERIOD_START_EFFECTIVITY);
						}
						else if(oRates instanceof String && oDates instanceof String)
						{
							rates.add((String)mapCurrencyConversion.get(CurrencyConversion.SELECT_RATE_PERIOD_RATE));
							dates.add((String)mapCurrencyConversion.get(CurrencyConversion.SELECT_RATE_PERIOD_START_EFFECTIVITY));
						}
					}
					tempMap.put(CurrencyConversion.SELECT_RATE_PERIOD_RATE, rates);
					tempMap.put(CurrencyConversion.SELECT_RATE_PERIOD_START_EFFECTIVITY, dates);
					conversionMap.put(strFromCurrency+","+toCurrency, tempMap);

				}
			}

		}catch (Exception e) {
			throw new MatrixException(e);
		}

		return  conversionMap;

	}

	/**
	 * Gets Project budget report data
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */
	public MapList getTableProjectBudgetReportData(Context context, String[] args, boolean isCostItem) throws Exception{
		try{
			Map mapProgram = (Map)JPO.unpackArgs(args);
			// Get Budget information
			MapList mlBudgets = getTableProjectBudgetOrBenefitData(context, args, true);
			if (mlBudgets == null || mlBudgets.isEmpty()) {
				return new MapList();
			}
			// Get Budget's Cost Items' information
			Map mapBudgetInfo = (Map)mlBudgets.get(0);
			String strBudgetId = (String)mapBudgetInfo.get(DomainConstants.SELECT_ID);
			String strTimeLine = (String)mapBudgetInfo.get(SELECT_COST_INTERVAL);
			String strBudgetStartDate = (String)mapBudgetInfo.get(SELECT_COST_INTERVAL_START_DATE);
			String strBudgetEndDate = (String)mapBudgetInfo.get(SELECT_COST_INTERVAL_END_DATE);

			mapProgram.put("objectId", strBudgetId);
			String strTimeZone = (String)mapProgram.get("timeZone");
			String strTimeLineInterval = "";
			String selecteReportView = (String)mapProgram.get(BUDGETTIMELINEREPORTFILTER);
			String currency = (String)mapProgram.get(BUDGETREPORTCURRENCYFILTER);
			mapProgram.put(BUDGETVIEWREPORTFILTER, actualView);
			mapProgram.put(BUDGETREPORTCURRENCYFILTER, currency);
			if(ProgramCentralUtil.isNotNullString(selecteReportView)){
				strTimeLineInterval = selecteReportView;
			}else{
				strTimeLineInterval = WEEKLY;
			}
			///TEST
			boolean isActualView = false;
			double clientTimeZone = Task.parseToDouble(strTimeZone);

			String[] argsExpand = JPO.packArgs(mapProgram);
			MapList mlCostItemStructure = getTableExpandChildProjectBudgetBenefitData(context, argsExpand, true);


			//
			// Remove categories from mlCostItemStructure
			//
			MapList mlCostItems = filterCostItemCategories(mlCostItemStructure);

			//
			// Add Budget as the first map
			//
			mlCostItems.add(0, mapBudgetInfo);

			//If timeline interval is by Phase, preparing data for weekly timeline for reports
			Map mapDate = new HashMap();
			if(PROJECT_PHASE.equals(strTimeLine)){
				mlCostItems = prepareWeeklyBudgetForPhasesInActualView(context,mlCostItems,clientTimeZone,strBudgetStartDate,strBudgetEndDate,strTimeLineInterval,currency);
				//mlCostItems = getBudgetByPhaseReport(context,mlCostItems,clientTimeZone,strBudgetStartDate,strBudgetEndDate,strTimeLineInterval);
			}
			else{
				for (Iterator itrBudgetBenefit = mlCostItems.iterator(); itrBudgetBenefit.hasNext();){
					Map mapCostItem = (Map) itrBudgetBenefit.next();
					mapDate = getConsolidatedBudgetDateMap(mapCostItem);
					mapCostItem.put("DateMap", mapDate);
					mapCostItem.put("TimeLine", strTimeLine);
				}
			}
			//printMapList(mlCostItems);

			return mlCostItems;
		}
		catch (Exception exp)
		{
			exp.printStackTrace();
			throw new MatrixException(exp);
		}
	}

	/**
	 * This method prepares a New copy of given HashMap
	 *
	 * @param mapSource HashMap containing List of Assigned Tasks
	 * @return Copy of given Assigned Task Map
	 * @throws MatrixException if the operation fails
	 * @since R210
	 */
	private Map shalowCopy(Map mapSource) {
		Map mapNew = new HashMap();
		for (Iterator itrKeys = mapSource.keySet().iterator(); itrKeys.hasNext();) {
			Object objKey = itrKeys.next();
			Object objValue = mapSource.get(objKey);

			mapNew.put(objKey, objValue);
		}

		return mapNew;
	}

	private MapList getBudgetByPhaseReport(Context context,MapList mlCostItems, double clientTimeZone, String strBudgetStartDate, String strBudgetEndDate,String timelineView)throws Exception {
		String SELECT_PHASE_START_DATE = "attribute[" + ATTRIBUTE_TASK_ESTIMATED_START_DATE  + "]";
		String SELECT_PHASE_END_DATE = "attribute[" + ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE  + "]";

		try{
			Date dtBudgetStartDate =removeTime(eMatrixDateFormat.getJavaDate(strBudgetStartDate));
			Date dtBudgetEndDate =removeTime(eMatrixDateFormat.getJavaDate(strBudgetEndDate));
			ArrayList dateList =  getIntervalDateList(dtBudgetStartDate,dtBudgetEndDate,timelineView);
			dateList = removeTime(dateList);
			Map mapDate = storeAsDateMap(dateList);

			for (Iterator itrCostItems = mlCostItems.iterator(); itrCostItems.hasNext();) {
				Map mapCostItem = (Map) itrCostItems.next();

				Map mapCostItemDate = shalowCopy(mapDate);
				mapCostItem.put("DateMap", mapCostItemDate);
				mapCostItem.put("TimeLine", PROJECT_PHASE);

				for (Iterator itrKeys = mapCostItem.keySet().iterator(); itrKeys.hasNext();) {
					ArrayList phaseDateList;
					String strKey = (String) itrKeys.next();
					if(!strKey.startsWith("Phase_")){
						continue;
					}
					String strPhaseId = "";
					String strAmount  = "";
					if(!strKey.contains("_Unit")){
						strPhaseId = FrameworkUtil.findAndReplace(strKey, "Phase_", "");
						strAmount = (String)mapCostItem.get(strKey);

						double dAmount = 0;
						if (null != strAmount && !"".equals(strAmount)) {
							dAmount = Task.parseToDouble(strAmount);
						}

						DomainObject dmoPhase = DomainObject.newInstance(context,strPhaseId);
						StringList slSelect = new StringList();
						slSelect.add(SELECT_PHASE_START_DATE);
						slSelect.add(SELECT_PHASE_END_DATE);

						Map mapPhase = dmoPhase.getInfo(context,slSelect);

						String strPhaseStartDate = (String)mapPhase.get(SELECT_PHASE_START_DATE);
						String strPhaseEndDate = (String)mapPhase.get(SELECT_PHASE_END_DATE);

						Date dtPhaseStartDate = removeTime(eMatrixDateFormat.getJavaDate(strPhaseStartDate));
						Date dtPhaseEndDate = removeTime(eMatrixDateFormat.getJavaDate(strPhaseEndDate));

						// How many weeks lie in this phase
						int nTotalWeeksInPhase = 0;
						int daysinPhase = getDays(dtPhaseStartDate, dtPhaseEndDate) + 1;
						double perDayAmount = dAmount/daysinPhase;

						phaseDateList = getIntervalDateList(dtPhaseStartDate,dtPhaseEndDate,timelineView);
						phaseDateList = removeTime(phaseDateList);

						int length = phaseDateList.size();
						int actualDays;
						double prevAmount,intervalamount = 0;
						String intervalLabel;
						//First interval date of phase.
						Date firstIntervalDate = (Date)phaseDateList.get(0);
						for(int i=0; i < length; i++){
							prevAmount = intervalamount = actualDays = 0;
							Date currIntervalDate = (Date)phaseDateList.get(i);
							//If there is only one interval, take all days of the phase as actual days.
							if(length == 1){
								actualDays = daysinPhase;
							}
							//If there is more than one interval, take days between current and next interval
							//as actual days(excluding next interval date).
							else if(i<(length-1)){
								Date nextIntervalDate = (Date)phaseDateList.get(i+1);
								//If current interval date is same as phase first interval date, then take only
								//those days as actual days that fall between phase start date and next interval.
								if(firstIntervalDate.equals(currIntervalDate)){
									//actualDays = getDays(dtBudgetStartDate,nextIntervalDate);
									actualDays = getDays(dtPhaseStartDate,nextIntervalDate);
								}
								//If interval is not first or last interval, then take days between current and
								//next interval as actual days(excluding next interval date).
								else{
									actualDays = getDays(currIntervalDate,nextIntervalDate);
								}
							}
							//If interval is last interval, then take days between last interval and
							// phase end date as actual days(including phase end date).
							else{
								actualDays = getDays(currIntervalDate,dtPhaseEndDate) + 1;
							}
							intervalamount = actualDays * perDayAmount;
							intervalLabel = getDateKey(currIntervalDate);
							if(mapCostItemDate.containsKey(intervalLabel))
								prevAmount = Task.parseToDouble((String)mapCostItemDate.get(intervalLabel));
							mapCostItemDate.put(intervalLabel, String.valueOf(prevAmount + intervalamount));
						}




						//						for(int i=0; i < weekIntervals; i++){
						//							prevAmount = intervalamount = actualDays = 0;
						//							Date intervalDate = (Date)phaseDateList.get(i);
						//							if(weekIntervals == 1){
						//								actualDays = daysinPhase;
						//							}  if(i==0){
						//								actualDays = 7 - getDays(intervalDate,dtPhaseStartDate);
						//							}else if(i==(weekIntervals-1)){
						//								actualDays = getDays(intervalDate,dtPhaseEndDate) + 1 ;
						//							}else{
						//								actualDays = 7;
						//							}
						//							intervalamount = actualDays * perDayAmount;
						//							intervalLabel = getDateKey(intervalDate);
						//							if(mapCostItemDate.containsKey(intervalLabel))
						//								prevAmount = Task.parseToDouble((String)mapCostItemDate.get(intervalLabel));
						//							mapCostItemDate.put(intervalLabel, String.valueOf(prevAmount + intervalamount));
						//						}

						//						for (Iterator itrDates = dateList.iterator(); itrDates.hasNext();) {
						//							Date date = (Date) itrDates.next();
						//
						//							if (isWithin(date, dtStartDate, dtEndDate)) {
						//								nTotalWeeksInPhase++;
						//							}
						//						}
						//						double dAmountPerWeek = dAmount / nTotalWeeksInPhase;

						// Update the phase amounts per week now
						//						for (Iterator itrDates = dateList.iterator(); itrDates.hasNext();) {
						//							Date date = (Date) itrDates.next();
						//
						//							if (isWithin(date, dtStartDate, dtEndDate)) {
						//								String strDate = getDateKey(date);
						//
						//								double dAmountPrev = Task.parseToDouble((String)mapCostItemDate.get(strDate));
						//								mapCostItemDate.put(strDate, String.valueOf(dAmountPrev + dAmountPerWeek));
						//							}
						//						}
					}

				}
			}
		}
		catch(Exception exp){
			throw exp;
		}
		return mlCostItems;
	}

	private MapList prepareWeeklyBudgetForPhasesInActualView(Context context,MapList mlCostItems,
			double clientTimeZone, String strBudgetStartDate, String strBudgetEndDate,String strTimeLineInterval, String filterCurrency)
					throws Exception {
		String SELECT_PHASE_START_DATE = "attribute[" + ATTRIBUTE_TASK_ESTIMATED_START_DATE  + "]";
		String SELECT_PHASE_END_DATE = "attribute[" + ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE  + "]";

		try{
			Date dtBudgetStartDate =removeTime(eMatrixDateFormat.getJavaDate(strBudgetStartDate));
			Date dtBudgetEndDate =removeTime(eMatrixDateFormat.getJavaDate(strBudgetEndDate));
			ArrayList dateList =  getIntervalDateList(dtBudgetStartDate,dtBudgetEndDate,strTimeLineInterval);
			dateList = removeTime(dateList);

			Map mapDate = storeAsDateMap(dateList);
			Map mapBudget = new HashMap();
			for (Iterator itrCostItems = mlCostItems.iterator(); itrCostItems.hasNext();) {
				Map mapCostItem = (Map) itrCostItems.next();

				Map mapCostItemDate = shalowCopy(mapDate);
				mapCostItem.put("DateMap", mapCostItemDate);
				mapCostItem.put("TimeLine", PROJECT_PHASE);

				String itemname = (String) mapCostItem.get("name");
				String itemid = (String) mapCostItem.get("id");
				// Get the actual transaction information
				Object objActTransDate = mapCostItem.get("to[Actual Transaction Item].from.attribute[Transaction Date]");//TODO
				Object objActTransAmount = mapCostItem.get("to[Actual Transaction Item].from.attribute[Transaction Amount]");
				Object objActTransAmountUnit = mapCostItem.get("to[Actual Transaction Item].from.attribute[Transaction Amount].inputunit");

				if(null != objActTransDate){
					StringList slActTransDate = new StringList();
					StringList slActTransAmount = new StringList();
					StringList slActTransAmountUnit = new StringList();

					if (objActTransDate instanceof String) {
						slActTransDate.add((String)objActTransDate);
						slActTransAmount.add((String)objActTransAmount);
						slActTransAmountUnit.add((String)objActTransAmountUnit);
					}
					else if (objActTransDate instanceof StringList) {
						slActTransDate.addAll((StringList)objActTransDate);
						slActTransAmount.addAll((StringList)objActTransAmount);
						slActTransAmountUnit.addAll((StringList)objActTransAmountUnit);
					}
					else {
						throw new MatrixException("Invalid selectable value type " + objActTransDate.getClass().getName());
					}
					// Consolidate the amounts now
					for (Object objStartDateOfWeek : dateList) {
						Date dtStartOfWeek = (Date)objStartDateOfWeek;
						dtStartOfWeek = removeTime(dtStartOfWeek);
						Interval interval = null;
						if(MONTHLY.equals(strTimeLineInterval)){
							interval = Financials.getNextFiscalInterval(dtStartOfWeek, IntervalType.MONTHLY);
						}else if(QUARTERLY.equals(strTimeLineInterval)){
							interval = Financials.getNextFiscalInterval(dtStartOfWeek, IntervalType.QUARTERLY);
						}else if(ANNUALLY.equals(strTimeLineInterval)){
							interval = Financials.getNextFiscalInterval(dtStartOfWeek, IntervalType.YEARLY);
						}else{
							interval = Financials.getNextFiscalInterval(dtStartOfWeek, IntervalType.WEEKLY);
						}
						Date dtEndOfWeek = interval.getStartDate();
						long endDateMs = dtEndOfWeek.getTime();
						endDateMs = endDateMs - 86400000;
						dtEndOfWeek.setTime(endDateMs);
						// Find and consolidate the transactions in this date range
						int size = slActTransAmount.size();
						for (int i = 0; i < size; i++) {
							String strTransDate = (String)slActTransDate.get(i);
							String strTransAmount = (String)slActTransAmount.get(i);
							String strTransAmountUnit = (String)slActTransAmountUnit.get(i);
							double dblTransAmount = Currency.convert(context, Task.parseToDouble(strTransAmount), strTransAmountUnit, filterCurrency, new Date(), false);
							String strWeekStartDate = getDateKey(dtStartOfWeek);
							Date dtTransDate = removeTime(eMatrixDateFormat.getJavaDate(strTransDate));
							if (isWithin(dtTransDate, dtStartOfWeek, dtEndOfWeek)) {
								//String strDate = getDateKey(dtStartOfWeek);
								String strPrevAmount = (String)mapCostItemDate.get(strWeekStartDate);
								Double dblPrevAmount = 0d;
								if(null != strPrevAmount && !"null".equals(strPrevAmount) && !"".equals(strPrevAmount)){
									dblPrevAmount = new Double(strPrevAmount);
								}
								dblPrevAmount += dblTransAmount;
								mapCostItemDate.put(strWeekStartDate, String.valueOf(dblPrevAmount));
								Double dblBudPrevAmount = 0d;
								dblBudPrevAmount += dblTransAmount;
								if(!mapBudget.containsKey(strWeekStartDate)){
									mapBudget.put(strWeekStartDate, String.valueOf(dblBudPrevAmount));
								}else{
									if(mapBudget.containsKey(strWeekStartDate))
									{
										dblBudPrevAmount += Task.parseToDouble((String)(mapBudget.get(strWeekStartDate)));
										mapBudget.put(strWeekStartDate, String.valueOf(dblBudPrevAmount));
									}
								}
							}
						}
					}
				}
				mapCostItem.put("mapBudget",mapBudget);
			}
		}
		catch(Exception exp){
			throw new MatrixException(exp);
		}
		return mlCostItems;
	}

	private boolean isWithin(Date date, Date dtStartDate, Date dtEndDate) {
		return ( date.equals(dtStartDate) || (dtStartDate.before(date) && dtEndDate.after(date)) || date.equals(dtEndDate));
	}

	private Map storeAsDateMap(ArrayList dateList) {
		Map mapResult = new HashMap();
		Calendar calc = Calendar.getInstance();

		for (Iterator itrDates = dateList.iterator(); itrDates.hasNext();) {
			Date date = (Date) itrDates.next();
			String strKey = getDateKey(date);
			mapResult.put(strKey, "0");
		}

		return mapResult;
	}

	private String getDateKey(Date date) {
		Calendar calc = Calendar.getInstance();
		calc.setTime(date);
		return (calc.get(Calendar.MONTH)+1) + "/" + calc.get(Calendar.DATE) + "/" + calc.get(Calendar.YEAR);
	}

	protected Date removeTime(Date date)throws MatrixException{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String strTempDate = simpleDateFormat.format(date);
		try {
			date = simpleDateFormat.parse(strTempDate);
		} catch (ParseException e) {
			throw new MatrixException(e);
		}
		return date;
	}

	protected ArrayList removeTime(ArrayList dateList)throws MatrixException{
		ArrayList resultDateList = new ArrayList(dateList.size());
		for (Iterator itrDates = dateList.iterator(); itrDates.hasNext();) {
			Date date = (Date) itrDates.next();
			date = removeTime(date);
			resultDateList.add(date);
		}
		return resultDateList;
	}

	private Map getConsolidatedBudgetDateMap(Map mapBudgetBenefitInfo) {
		Map mapResult = new HashMap();

		for (Iterator iterator = mapBudgetBenefitInfo.keySet().iterator(); iterator.hasNext();)
		{
			String strkey = (String) iterator.next();
			String strValue= null;
			if(strkey.contains("/")){
				strValue = (String)mapBudgetBenefitInfo.get(strkey);
				mapResult.put(strkey,strValue);
			}
		}
		return mapResult;
	}

	/**
	 *
	 *
	 * @param mlCostItemStructure
	 * @return
	 */
	private MapList filterCostItemCategories(MapList mlCostItemStructure) {

		MapList mlResult = new MapList();
		for (Iterator itrItems = mlCostItemStructure.iterator(); itrItems.hasNext();) {
			Map mapCurrItem = (Map) itrItems.next();

			String strType = (String)mapCurrItem.get(SELECT_TYPE);
			if (strType == null || TYPE_FINANCIAL_COST_CATEGORY.equals(strType)) { //BUGBUG IXE 6/7/10 Check the kindof for this type
				continue;
			}

			mlResult.add(mapCurrItem);
		}

		return mlResult;
	}

	/**
	 * Returns the name of Financial Item & related cost/benefit category items to be displayed in table.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args Array of strings holding request parameters.
	 * @param isBudget a boolean value. True indicates operation is performed over Budget and
	 * false is for Benefit.
	 * @return a collection of html formatted names of Financial Items(Budget/benefit) and
	 * related Cost/Benefit categories entries.
	 * @throws MatrixException
	 */
	Vector getNameColumnDataForFinancialItems(Context context, String[] args, boolean isBudget)
			throws MatrixException{
		try
		{
			//Create result vector
			Vector vecResult = new Vector();
			String strFinancialParentCostCategory =ProgramCentralConstants.EMPTY_STRING;;
			// Get object list information from packed arguments
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map mapRowData = null;
			String strObjectId = ProgramCentralConstants.EMPTY_STRING;
			String strObjectName = ProgramCentralConstants.EMPTY_STRING;;
			DomainObject dmoObject = null;
			String strFullName =ProgramCentralConstants.EMPTY_STRING;;
			String dmoType;
			String strLanguage = ProgramCentralConstants.EMPTY_STRING;
			Iterator mainCategoryItr = objectList.iterator();
			String strFormedName = null;
			StringBuffer sbProjectLink = null;
			Map paramMap = (Map)programMap.get("paramList");
			strObjectId = (String) paramMap.get("objectId");
			DomainObject dbBudget = DomainObject.newInstance(context,strObjectId);
			String strQuery = "relationship["+ProgramCentralConstants.RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"].from.type";
			String strParentType =  dbBudget.getInfo(context,strQuery);
			String currentTable = (String)paramMap.get("selectedTable");
			strLanguage = (String)paramMap.get("languageStr");
			if(ProgramCentralUtil.isNullString(strLanguage) || strLanguage.equals(ProgramCentralConstants.EMPTY_STRING))
				strLanguage=context.getSession().getLanguage();

			boolean isExportable = false;
			if(paramMap.containsKey("exportFormat") && ("CSV".equalsIgnoreCase((String)paramMap.get("exportFormat")))){
				isExportable = true;
			}
			// Based on boolean parameter value of these variables will be decided.
			String strFinancialItemType;
			String strCostItemtype;
			String strCostCategoryType;
			if(isBudget){
				strFinancialItemType = TYPE_BUDGET;
				strCostItemtype = TYPE_COST_ITEM;
				strCostCategoryType = TYPE_FINANCIAL_COST_CATEGORY;
			}else{
				strFinancialItemType = TYPE_BENEFIT;
				strCostItemtype = TYPE_BENEFIT_ITEM;
				strCostCategoryType = TYPE_FINANCIAL_BENEFIT_CATEGORY;
			}
			while (mainCategoryItr.hasNext()) {
				Map mainCategoryMap = (Map) mainCategoryItr.next();
				String strParentCostCategoryID = (String) mainCategoryMap.get(DomainConstants.SELECT_ID);
				String strParentCostCategoryName = (String) mainCategoryMap.get(DomainConstants.SELECT_NAME);
				String strKey = ProgramCentralConstants.EMPTY_STRING;;
				String stri18nName = ProgramCentralConstants.EMPTY_STRING;;
				dmoObject = DomainObject.newInstance(context, strParentCostCategoryID);
				dmoType = dmoObject.getInfo(context, SELECT_TYPE);
				sbProjectLink = new StringBuffer();
				if(dmoType.equals(TYPE_PROJECT_SPACE)){
					stri18nName = dmoObject.getName();
					if(isExportable)
						sbProjectLink.append(stri18nName);
					else{
						sbProjectLink.append("<a href='../common/emxTree.jsp?objectId=");
						sbProjectLink.append(XSSUtil.encodeForURL(context,strParentCostCategoryID));
						sbProjectLink.append("'>");
						sbProjectLink.append(XSSUtil.encodeForXML(context,stri18nName));
						sbProjectLink.append("</a>");
					}
				}else if(dmoType.equals(strFinancialItemType)){
					//strFormedName = FrameworkUtil.findAndReplace(strParentCostCategoryName.trim()," ", "_");
					strFormedName = strParentCostCategoryName.trim();
					if(currentTable.equals("PMCProjectBudgetReportTable")){
						// XSS encoding removed because it is breaking in french and some other
						sbProjectLink.append(strFormedName);
					}else{
						if(isExportable){
							// XSS encoding removed because it is breaking in french and some other
							sbProjectLink.append(strFormedName);
						} else {
							if(isBudget){
								if(strParentType.equalsIgnoreCase(DomainObject.TYPE_PROJECT_SPACE)){
									sbProjectLink.append("<a href=\"JavaScript:top.showSlideInDialog('../common/emxForm.jsp?form=PMCProjectBudgetEditForm&amp;mode=view&amp;toolbar=PMCProjectBudgetEditToolbar&amp;HelpMarker=emxhelpprojectbudgetproperties&amp;suiteKey=ProgramCentral&amp;objectId=");
								}
								else if(strParentType.equalsIgnoreCase(DomainObject.TYPE_PROJECT_TEMPLATE))
								{
									sbProjectLink.append("<a href=\"JavaScript:top.showSlideInDialog('../common/emxForm.jsp?form=PMCProjectTemplateBudgetViewForm&amp;mode=view&amp;toolbar=PMCProjectBudgetEditToolbar&amp;HelpMarker=emxhelpprojectbudgetproperties&amp;suiteKey=ProgramCentral&amp;objectId=");
								}
							}
							else{
								sbProjectLink.append("<a href=\"JavaScript:top.showSlideInDialog('../common/emxForm.jsp?form=PMCProjectBenefitPropertyPage&amp;mode=view&amp;toolbar=PMCProjectBenefitEditToolbar&amp;HelpMarker=emxhelpfinancialitemdetails&amp;suiteKey=ProgramCentral&amp;objectId=");
							}
							sbProjectLink.append(XSSUtil.encodeForURL(context,strParentCostCategoryID));
							sbProjectLink.append("','true')\">");
						//Added for special character.
							sbProjectLink.append(XSSUtil.encodeForXML(context,strFormedName));
							sbProjectLink.append("</a>");
						}
					}
				}else if(dmoType.equals(strCostItemtype) ||
						dmoType.equals(strCostCategoryType)){
					strFormedName = FrameworkUtil.findAndReplace(strParentCostCategoryName.trim()," ", "_");
					// XSS encoding removed because it is breaking in french and some other
					strKey = "emxProgramCentral.Common."+strFormedName;
					stri18nName = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
							strKey, strLanguage);
					sbProjectLink.append(stri18nName);
				}
				vecResult.add(sbProjectLink.toString());
			}
			return vecResult;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new MatrixException(ex);
		}
	}

	public static boolean isBackgroundTaskActive(Context context, String[] args)
			throws MatrixException{
		try{
			Map programMap = (HashMap) JPO.unpackArgs(args);
			Map requestMap = (HashMap)programMap.get("requestMap");
			String  objectId = (String) programMap.get("objectId");
			return isBackgroundTaskActive(context, objectId);
		}catch(Exception e){
			throw new MatrixException(e);
		}
	}

	/**
	 * Generates an admin message for the user trying to make transaction on financial objects, when some background task is active.
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args the request parameter string array
	 * @return HTML formatted string message to stop user from making transaction on financial objects.
	 * @throws MatrixException if operation fails.
	 */
	public String getCurrencyModifyBackgroundTaskMessage(Context context, String[] args)
			throws MatrixException{
		StringBuffer strbuff = new StringBuffer();
		strbuff.append("<img src=\"../common/images/iconStatusAlert.gif\">");
		String message = getCurrencyModifyBackgroundTaskMessage(context, false);
		strbuff.append(XSSUtil.encodeForHTML(context,message));
		return strbuff.toString();
	}


	/**
	 * When an Budget is created, grant the creator the proper
	 * permissions on the Budget object.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the assessment id
	 * @throws Exception if operation fails
	 */
	public void triggerCreateAction(Context context, String[] args)
			throws Exception
			{
		// get values from args.
		String financialItemId = args[0]; // Budget ID
		String personId = com.matrixone.apps.domain.util.PersonUtil.getPersonObjectID(context);
		DomainAccess.createObjectOwnership(context, financialItemId, personId, "Project Lead", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
			}

	/**
	 * Checks access on Multiple Ownership Access command for Budget.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean hasAccessOnMultipleOwnershipAccessCommand(Context context, String[]args )throws Exception
	{
		boolean hasAccess = false;
		try {
			Map inputMap = JPO.unpackArgs(args);
			String objectId = (String)inputMap.get("objectId");

			DomainObject object = DomainObject.newInstance(context, objectId);
			if(object.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_SPACE)){
				ProjectSpace project = new ProjectSpace(objectId);
				StringList selects = new StringList();
				selects.add(SELECT_ID);
				MapList financialInfoList = null;
				financialInfoList = FinancialItem.getFinancialBudgetOrBenefit(context, project,
						selects, ProgramCentralConstants.TYPE_BUDGET);
				if(null!=financialInfoList && financialInfoList.size()>0){
					hasAccess = true;
				}
			}
		} catch (Exception e) {
			throw new MatrixException(e);
		}
		return hasAccess;
	}

	/**
	 * Checks access on Multiple Ownership Access command for Benefit.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean hasAccessOnBenefitMultiOwnershipAccessCommand(Context context, String[]args )throws Exception
	{
		boolean hasAccess = false;
		try {
			Map inputMap = JPO.unpackArgs(args);
			String objectId = (String)inputMap.get("objectId");

			DomainObject object = DomainObject.newInstance(context, objectId);
			if(object.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_SPACE)){
				ProjectSpace project = new ProjectSpace(objectId);
				StringList selects = new StringList();
				selects.add(SELECT_ID);
				MapList financialInfoList = null;
				financialInfoList = FinancialItem.getFinancialBudgetOrBenefit(context, project,
						selects, ProgramCentralConstants.TYPE_BENEFIT);
				if(null!=financialInfoList && financialInfoList.size()>0){
					hasAccess = true;
				}
			}
		} catch (Exception e) {
			throw new MatrixException(e);
		}
		return hasAccess;
	}

	/**
	 * Inherits access inheritance from Budget to newly created Actual Transaction.
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args parameters passed with trigger invocation.
	 * @throws Exception if operation fails.
	 */
	public void triggerCreateActualTransactionItemInheritOwnership(Context context, String[] args) throws Exception{
		String actualTransactionId = args[0];
		String budgetId = args[1];
		DomainAccess.createObjectOwnership(context, actualTransactionId, budgetId, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
	}


	/**
	 * Inherits ownership to the budget from it's parent only when created in a Project Template.
	 *
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args contains a packed HashMap
	 * */
	public void triggerInheritOwnershipOnCreateBudgetInTemplate(Context context, String[] args) throws Exception
	{
		String fromId = args[0];
		String toId = args[1];

		try{
			DomainObject fromObj = DomainObject.newInstance(context, fromId);
			if(fromObj.isKindOf(context,DomainConstants.TYPE_PROJECT_TEMPLATE)){
				DomainAccess.createObjectOwnership(context, toId, fromId, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
			}

		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}

	}

}
