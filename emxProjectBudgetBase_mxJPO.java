/*
 *  emxProjectTemplateBudgetBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: emxProjectFolderBase.java.rca 1.10.2.1 Wed Dec 24 10:59:14 2008 ds-ksuryawanshi Experimental $
 */
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.AccessConstants;
import matrix.db.AttributeType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Relationship;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Task;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
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
import com.matrixone.apps.framework.ui.UITableIndented;
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
import com.matrixone.apps.program.fiscal.Interval;
import com.matrixone.apps.program.fiscal.IntervalType;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.IllegalDataException;


public class emxProjectBudgetBase_mxJPO extends emxFinancialItemBase_mxJPO
{

	public static final String SYMBOLIC_ATTRIBUTE_TRANSACTION_AMOUNT = "attribute_TransactionAmount";
	public static final String SYMBOLIC_ATTRIBUTE_TRANSACTION_PON = "attribute_TransactionPON";
	public static final String SYMBOLIC_ATTRIBUTE_TRANSACTION_DATE = "attribute_TransactionDate";
	public static final String SYMBOLIC_TYPE_ACTUAL_TRANSACTION = "type_ActualTransaction";
	public static final String SYMBOLIC_TYPE_COST_ITEM = "type_CostItem";
	public static final String SYMBOLIC_TYPE_PHASE = "type_Phase";
	public static final String SYMBOLIC_TYPE_Budget = "type_Budget";

	public static final String SYMBOLIC_POLICY_ACTUAL_TRANSACTION = "policy_ActualTransaction";
	public static final String SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_ITEM = "relationship_ActualTransactionItem";
	public static final String SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_PHASE = "relationship_ActualTransactionPhase";
	public static final String SYMBOLIC_RELATIONSHIP_FINANCIAL_ITEMS = "relationship_FinancialItems";
	public static final String SYMBOLIC_RELATIONSHIP_SUBTASK = "relationship_Subtask";
	public static final String SYMBOLIC_RELATIONSHIP_PROJECT_FINANCIAL_ITEM = "relationship_ProjectFinancialItem";
	public static final String SELECT_BUDGET_ID = "from["+ RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"].to.id";
	public static final String SELECT_BUDGETS_PROJECT_ID = "to["+ RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"].from.id";
	static final String SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE = "attribute[" + ATTRIBUTE_TASK_ESTIMATED_START_DATE  + "]";
	static final String SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE= "attribute[" + ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE  + "]";
	public static final String STATE_BUDGET_PLAN_FROZEN = "Plan Frozen";
	public static final String STATE_BUDGET_PLAN = "Plan";
	static final String PLAN_TABLE = "PMCProjectBudgetPlanTable";
	static final String ESTIMATED_TABLE = "PMCProjectBudgetEstimatedTable";
	static final String ACTUAL_TABLE = "PMCProjectBudgetActualTable";


	/**
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	public emxProjectBudgetBase_mxJPO (Context context, String[] args)
			throws Exception
			{
		super(context,args);
			}

	/**
	 * This method is executed if a specific method is not specified.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return nothing
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	public int mxMain(Context context, String[] args)
			throws Exception
			{
		if (!context.isConnected())
			throw new Exception("not supported on desktop client");
		return 0;
			}


	/**
	 * Creates project template budget
	 * Added:14-April-2010
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return void
	 * @throws Exception if operation fails
	 */

	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void createTemplateBudget(Context context, String[] args)  throws Exception
	{
		try{
			Map programMap = (Map) JPO.unpackArgs(args);
			Map requestMap = (Map) programMap.get("requestMap");
			Map paramMap = (Map) programMap.get("paramMap");
			String budgetName =(String)requestMap.get("Name");

			String strProjectSpaceId = (String)requestMap.get("objectId");
			if(isBackgroundTaskActive(context, strProjectSpaceId)){
				getCurrencyModifyBackgroundTaskMessage(context, true);
				return;
			}
			String strCostInterval = (String)requestMap.get("CostInterval");
			String strTemplateEnforcement = (String)requestMap.get("TemplateEnforcement");
			if(null == strTemplateEnforcement){
				String strTempEnforce = EnoviaResourceBundle.getProperty(context, "emxFramework.Budget.TemplateEnforcement") ;
				strTemplateEnforcement= strTempEnforce;
			}
			String strNotes = (String)requestMap.get("Notes");

			DomainObject dmoProjectSpace = DomainObject.newInstance(context,strProjectSpaceId);
			String SELECT_ACCESS_OBJECT = "to["+RELATIONSHIP_PROJECT_ACCESS_LIST + "].from.id";
			String strProjectAccessObject = (String)dmoProjectSpace.getInfo(context, SELECT_ACCESS_OBJECT);

			String strTimeLineIntervalFrom = dmoProjectSpace.getAttributeValue(context, DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE);
			String strTimeLineIntervalTo = dmoProjectSpace.getAttributeValue(context, DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);

			String strTemplateBudgetId = "";
			strTemplateBudgetId = (String)paramMap.get("newObjectId");
			DomainObject dmoTemplateBudget =  DomainObject.newInstance(context,strTemplateBudgetId);
			String revision = dmoTemplateBudget.getUniqueName(EMPTY_STRING);
			if (ProgramCentralUtil.isNullString(budgetName))
			{
				budgetName = FrameworkUtil.autoName(context,AUTONAME_TYPE_BUDGET,null,AUTONAME_POLICY_FINANCIALITEM,null,null,true,false);
			}
			//Updating revision of newly created Budget object.
			String sCommandStatement = " modify bus $1 name $2 revision $3";
			MqlUtil.mqlCommand(context, sCommandStatement,strTemplateBudgetId, budgetName,revision); 


			String strPreferredCurrency = Currency.getBaseCurrency(context,strProjectSpaceId);
			Map attributes = new HashMap();
			attributes.put(Financials.ATTRIBUTE_COST_INTERVAL,strCostInterval);
			attributes.put(Financials.ATTRIBUTE_TEMPLATE_ENFORCEMENT,strTemplateEnforcement);
			attributes.put(Financials.ATTRIBUTE_NOTES,strNotes);
			attributes.put(Financials.ATTRIBUTE_COST_INTERVAL_START_DATE,strTimeLineIntervalFrom);
			attributes.put(Financials.ATTRIBUTE_COST_INTERVAL_END_DATE,strTimeLineIntervalTo);
			// Set budget with default values and base currency.
			attributes.put(ProgramCentralConstants.ATTRIBUTE_ACTUAL_COST,"0.0" + ProgramCentralConstants.SPACE + strPreferredCurrency);
			attributes.put(ProgramCentralConstants.ATTRIBUTE_ESTIMATED_COST,"0.0" + ProgramCentralConstants.SPACE + strPreferredCurrency);
			attributes.put(ProgramCentralConstants.ATTRIBUTE_PLANNED_COST,"0.0" + ProgramCentralConstants.SPACE + strPreferredCurrency);

			//Setting attribute
			dmoTemplateBudget.setAttributeValues(context,attributes);

			DomainObject dmoAccessObject =  DomainObject.newInstance(context, strProjectAccessObject);
			String strRelationship = RELATIONSHIP_PROJECT_ACCESS_KEY;
			DomainRelationship.connect(context,dmoAccessObject,strRelationship,dmoTemplateBudget);

			//Code added for remove ownership on template budget object
			if(dmoProjectSpace.isKindOf(context, DomainObject.TYPE_PROJECT_TEMPLATE)){
				String defaultOrg=PersonUtil.getDefaultOrganization(context, context.getUser());
				String defaultProj=PersonUtil.getDefaultProject(context, context.getUser());
				DomainAccess.deleteObjectOwnership(context, 
						strTemplateBudgetId, 
						defaultOrg,  
						defaultProj,
						DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);

				if ("GLOBAL".equalsIgnoreCase(defaultProj)){
					dmoTemplateBudget.removePrimaryOwnership(context);
				}

				// FZS commented out to fix IR-358081, the onwership is now cleard when creating the project from the PT
                // DomainAccess.clearMultipleOwnership(context,strTemplateBudgetId);

				if(!DomainAccess.hasObjectOwnership(context, strTemplateBudgetId, strProjectSpaceId, "")){
					DomainAccess.createObjectOwnership(context, strTemplateBudgetId, strProjectSpaceId, "");
				}

			}//End

		}catch (Exception exp) {
			exp.printStackTrace();
			throw exp;
		}
	}

	/**
	 * Creates project budget
	 * Added:14-April-2010
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return void
	 * @throws Exception if operation fails
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void createProjectBudget(Context context, String[] args)  throws Exception
	{
		try
		{
			Map programMap = (Map) JPO.unpackArgs(args);
			Map requestMap = (Map) programMap.get("requestMap");
			Map paramMap = (Map) programMap.get("paramMap");

			String strProjectSpaceId = (String)requestMap.get("objectId");

			String budgetName = (String) requestMap.get("Name");
			String budgetId = (String) paramMap.get("newObjectId");



			if(isBackgroundTaskActive(context, strProjectSpaceId)){
				getCurrencyModifyBackgroundTaskMessage(context, true);
				return;
			}
			String strCostInterval = (String)requestMap.get("CostInterval");
			String strTimeLineIntervalFrom = (String)requestMap.get("TimeLineIntervalFrom");
			String strTimeLineIntervalTo = (String)requestMap.get("TimeLineIntervalTo");
			String strNotes = (String)requestMap.get("Notes");

			String strTimeZone = (String)requestMap.get("timeZone");
			double clientTimeZone = Task.parseToDouble(strTimeZone);
			String strLanguage=context.getSession().getLanguage();

			Locale locale = (Locale)requestMap.get("locale");
			if(null==locale||"".equals(locale)||"Null".equals(locale))
			{
				locale = (Locale)requestMap.get("localeObj");
			}

			DomainObject dmoProjectBudget = DomainObject.newInstance(context,budgetId);
			String revision = dmoProjectBudget.getUniqueName(EMPTY_STRING);
			if (ProgramCentralUtil.isNullString(budgetName))
			{
				budgetName = FrameworkUtil.autoName(context,AUTONAME_TYPE_BUDGET,null,AUTONAME_POLICY_FINANCIALITEM,null,null,true,false);
			}
			//Updating revision of newly created Budget object.
			String sCommandStatement = " modify bus $1 name $2 revision $3";
			MqlUtil.mqlCommand(context, sCommandStatement,budgetId, budgetName,revision); 

			DomainObject dmoProjectSpace = DomainObject.newInstance(context,strProjectSpaceId);
			StringList slBusSelect = new StringList();
			slBusSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
			slBusSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);

			if(! PROJECT_PHASE .equals(strCostInterval)){
				strTimeLineIntervalFrom = eMatrixDateFormat.getFormattedInputDate(context, strTimeLineIntervalFrom, clientTimeZone,locale);
				strTimeLineIntervalTo = eMatrixDateFormat.getFormattedInputDate(context, strTimeLineIntervalTo, clientTimeZone,locale);
			}else{
				String SELECT_PROJECT_PHASE = SELECT_ID;;
				slBusSelect.add(SELECT_PROJECT_PHASE);
				MapList mlPhaseList = dmoProjectSpace.getRelatedObjects(
						context,
						RELATIONSHIP_SUBTASK,
						TYPE_PHASE,
						slBusSelect,
						null,
						false,
						true,
						(short)1,
						null,
						DomainConstants.EMPTY_STRING);

				Date calStartDate = new Date();
				Date calEndDate = new Date();
				if(null != mlPhaseList && mlPhaseList.size() != 0){
					Map mapPhaseDates = getPhaseMinMaxDates(mlPhaseList);
					calStartDate = (Date)mapPhaseDates.get("calPhaseStartDate");
					calEndDate = (Date)mapPhaseDates.get("calPhaseEndDate");
					DateFormat dateFormat = DateFormat.getDateInstance(eMatrixDateFormat.getEMatrixDisplayDateFormat(),locale);
					//DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", locale);
					strTimeLineIntervalFrom = dateFormat.format(calStartDate);
					strTimeLineIntervalTo = dateFormat.format(calEndDate);

					strTimeLineIntervalFrom = eMatrixDateFormat.getFormattedInputDate(context, strTimeLineIntervalFrom, clientTimeZone,locale);
					strTimeLineIntervalTo = eMatrixDateFormat.getFormattedInputDate(context, strTimeLineIntervalTo, clientTimeZone,locale);
				}else{
					Map mapDates = dmoProjectSpace.getInfo(context, slBusSelect);
					strTimeLineIntervalFrom = (String)mapDates.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
					strTimeLineIntervalTo = (String)mapDates.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
				}
			}

			Date dtBudgetStartDate = eMatrixDateFormat.getJavaDate(strTimeLineIntervalFrom);
			Date dtBudgetFinishDate = eMatrixDateFormat.getJavaDate(strTimeLineIntervalTo);

			Calendar calBudgetStartDate = Calendar.getInstance();
			Calendar calBudgetFinishDate = Calendar.getInstance();

			calBudgetStartDate.setTime(dtBudgetStartDate);
			calBudgetFinishDate.setTime(dtBudgetFinishDate);



			String strProjectStartDate = "";
			String strProjectFinishtDate = "";

			Map mapObjInfo = dmoProjectSpace.getInfo(context, slBusSelect);
			strProjectStartDate = (String) mapObjInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
			strProjectFinishtDate = (String) mapObjInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);

			Date dtProjectStartDate = eMatrixDateFormat.getJavaDate(strProjectStartDate);
			Date dtProjectFinishDate = eMatrixDateFormat.getJavaDate(strProjectFinishtDate);

			Calendar calStartDate = Calendar.getInstance();
			Calendar calFinishDate = Calendar.getInstance();

			calStartDate.setTime(dtProjectStartDate);
			calFinishDate.setTime(dtProjectFinishDate);

			calStartDate.set(Calendar.HOUR,0);
			calStartDate.set(Calendar.HOUR_OF_DAY,0);
			calStartDate.set(Calendar.MINUTE,0);
			calStartDate.set(Calendar.SECOND,0);

			calFinishDate.set(Calendar.HOUR,0);
			calFinishDate.set(Calendar.HOUR_OF_DAY,0);
			calFinishDate.set(Calendar.MINUTE,0);
			calFinishDate.set(Calendar.SECOND,0);

			calBudgetStartDate.set(Calendar.HOUR,0);
			calBudgetStartDate.set(Calendar.HOUR_OF_DAY,0);
			calBudgetStartDate.set(Calendar.MINUTE,0);
			calBudgetStartDate.set(Calendar.SECOND,0);

			calBudgetFinishDate.set(Calendar.HOUR,0);
			calBudgetFinishDate.set(Calendar.HOUR_OF_DAY,0);
			calBudgetFinishDate.set(Calendar.MINUTE,0);
			calBudgetFinishDate.set(Calendar.SECOND,0);

			if(dtBudgetFinishDate.before(dtBudgetStartDate)){
				strLanguage = context.getSession().getLanguage();
				String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.ProjectBenefit.TimeLineToDateGreaterThanTimeLineFromDate", strLanguage);
				throw new Exception(sErrMsg);
			}

			//IR-224806: Allowing to create  Budget, where timeline is not within Project timeline. 
			/*if(((calBudgetStartDate.before(calStartDate))&&(calBudgetFinishDate.before(calStartDate)))
				||((calBudgetStartDate.after(calFinishDate))))
			{
				strLanguage = context.getSession().getLanguage();
				String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.ProjectBenefit.TimeLineFromDateOrToDateShouldBeWithinProjectPeriod ", strLanguage);
				throw new Exception(sErrMsg);
			}*/
			String SELECT_ACCESS_OBJECT = "to["+RELATIONSHIP_PROJECT_ACCESS_LIST + "].from.id";
			String strProjectAccessObject = (String)dmoProjectSpace.getInfo(context, SELECT_ACCESS_OBJECT);

			String strPreferredCurrency = Currency.getBaseCurrency(context,strProjectSpaceId);
			Map attributes = new HashMap();
			attributes.put(Financials.ATTRIBUTE_COST_INTERVAL,strCostInterval);
			attributes.put(DomainConstants.ATTRIBUTE_NOTES,strNotes);
			attributes.put(Financials.ATTRIBUTE_COST_INTERVAL_START_DATE,strTimeLineIntervalFrom);
			attributes.put(Financials.ATTRIBUTE_COST_INTERVAL_END_DATE,strTimeLineIntervalTo);
			// Set budget with default values and base currency.
			attributes.put(ProgramCentralConstants.ATTRIBUTE_ACTUAL_COST,"0.0" + ProgramCentralConstants.SPACE + strPreferredCurrency);
			attributes.put(ProgramCentralConstants.ATTRIBUTE_ESTIMATED_COST,"0.0" + ProgramCentralConstants.SPACE + strPreferredCurrency);
			attributes.put(ProgramCentralConstants.ATTRIBUTE_PLANNED_COST,"0.0" + ProgramCentralConstants.SPACE + strPreferredCurrency);

			//Setting attribute
			dmoProjectBudget.setAttributeValues(context,attributes);
			DomainObject dmoAccessObject =  DomainObject.newInstance(context, strProjectAccessObject);
			String strRelationship = RELATIONSHIP_PROJECT_ACCESS_KEY;
			DomainRelationship.connect(context,dmoAccessObject,strRelationship,dmoProjectBudget);
		}
		catch (Exception exp)
		{
			exp.printStackTrace();
			throw exp;
		}
	}



	/**
	 * Gets template budget table data for budget
	 *
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTableTemplateBudgetData(Context context, String[] args) throws Exception
	{
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strObjectId = (String) programMap.get("objectId");
			String strLanguage = (String)programMap.get("languageStr");
			String strRelationshipType = Financials.RELATIONSHIP_PROJECT_FINANCIAL_ITEM;
			String strType = DomainConstants.TYPE_FINANCIAL_ITEM;
			String whereClause = "" ;
			StringList busSelect = new StringList();
			busSelect.add(DomainConstants.SELECT_ID);
			busSelect.add(DomainConstants.SELECT_NAME);

			StringList relSelect = new StringList();
			DomainObject dom = DomainObject.newInstance(context, strObjectId);
			MapList mlRequests = dom.getRelatedObjects(
					context,
					strRelationshipType,
					strType,
					busSelect,
					relSelect,
					false,
					true,
					(short)1,
					whereClause,
					DomainConstants.EMPTY_STRING);
			return mlRequests ;
		}
		catch (Exception exp)
		{
			exp.printStackTrace();
			throw new MatrixException(exp);
		}
	}

	/**
	 * Returns the table data for the Budget summary page.
	 *
	 * @param context Matrix Context object
	 * @param args String array
	 * @return vector holding mentioned values
	 * @throws Exception if operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTableProjectBudgetData(Context context, String[] args) throws MatrixException
	{
		try
		{
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList mlRequests =  getTableProjectBudgetOrBenefitData(context,args,true);
			Map mapRowData = new HashMap();
			for (Iterator itrObjects = mlRequests.iterator(); itrObjects.hasNext();)
			{
				mapRowData = (Map) itrObjects.next();
				String strBudgetState = (String)mapRowData.get(SELECT_CURRENT);
				if(STATE_BUDGET_PLAN_FROZEN.equals(strBudgetState)){
					mapRowData.put("RowEditable","readonly");
				}
			}
			return  mlRequests;
		}
		catch (Exception exp)
		{
			exp.printStackTrace();
			throw new MatrixException(exp);
		}
	}

	/**
	 * This method gives data for expand of budget object in project budget structure browser table.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTableExpandChildBudgetData(Context context,String[] args) throws MatrixException
	{
		MapList mlCostCategoryList = null;
		MapList mlCostItemCategoryList = null;
		try{
			Map programMap = (HashMap) JPO.unpackArgs(args);
			String strObjecttId = (String) programMap.get("objectId");
			String selectedView = (String)programMap.get(BUDGETVIEWFILTER);
			DomainObject dmoObject = DomainObject.newInstance(context, strObjecttId);
			//Added:PRG:MS9:R212:26-Aug-2011:IR-088614V6R2012x
			if(dmoObject.isKindOf(context, ProgramCentralConstants.TYPE_FINANCIALS)){
				//Added:PRG:MS9:R212:26-Aug-2011:IR-088614V6R2012x end
				mlCostCategoryList =  getTableExpandChildProjectBudgetBenefitData(context,args,true);
				if(planView.equals(selectedView)){
					String strBudgetState = dmoObject.getInfo(context, SELECT_CURRENT);
					Map mapRowData = null;
					for (Iterator itrObjects = mlCostCategoryList.iterator(); itrObjects.hasNext();)
					{
						mapRowData = (Map) itrObjects.next();
						if(STATE_BUDGET_PLAN_FROZEN.equals(strBudgetState)){
							mapRowData.put("RowEditable","readonly");
						}
					}
				}
			}
			else {
				mlCostCategoryList = getTableProjectBudgetData(context,args);//This will return Budget connected to Project

				if(mlCostCategoryList.size()>0){
					Map mpBudget = (Map)mlCostCategoryList.get(0);
					programMap.put("objectId",(String)(mpBudget.get(DomainConstants.SELECT_ID)));
					String[] arrJPOArguments    = new String[1];
					arrJPOArguments = JPO.packArgs(programMap);

					mlCostItemCategoryList =  getTableExpandChildProjectBudgetBenefitData(context,arrJPOArguments,true); //To get cost Items
					if(planView.equals(selectedView)){
						String strBudgetState = dmoObject.getInfo(context, SELECT_CURRENT);
						Map mapRowData = null;
						for (Iterator itrObjects = mlCostItemCategoryList.iterator(); itrObjects.hasNext();)
						{
							mapRowData = (Map) itrObjects.next();
							if(STATE_BUDGET_PLAN_FROZEN.equals(strBudgetState)){
								mapRowData.put("RowEditable","readonly");
							}
						}
					}
					mlCostCategoryList.addAll(mlCostItemCategoryList);
				}
			}
		}catch(Exception exp){
			exp.printStackTrace();
			throw new MatrixException(exp);
		}
		return mlCostCategoryList;
	}





	/**
	 * This method gives data for expand of budget object in project template budget structure browser table.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTableExpandChildTemplateBudgetData(Context context,String[] args) throws Exception
	{
		MapList mlCostCategoryList = null;
		mlCostCategoryList =  getTableExpandChildProjectBudgetBenefitData(context,args,true);
		return mlCostCategoryList;
	}


	/** Decides if the Create Budget command to be displayed or not in Template tree category
	 *
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @return true if column is to be shown
	 * @throws Exception if operation fails
	 */
	public boolean isBudgetCreated (Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);

		String objectId = (String) programMap.get("objectId");
		String strTableName = (String) programMap.get("table");
		String parentID = (String)programMap.get("parentOID");
		String strParentId = ProgramCentralUtil.getParentFromRMBTableRowId(programMap);

		if(ProgramCentralUtil.isNotNullString(strParentId)){
			objectId = strParentId;
		}

		if("PMCProjectTemplateBudgetTable".equals(strTableName)){
			ProjectTemplate projectTemplate = (ProjectTemplate)DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_TEMPLATE, DomainObject.PROGRAM);
	 		boolean isCtxUserOwnerOrCoOwner = projectTemplate.isOwnerOrCoOwner(context, objectId);
	 		if(!isCtxUserOwnerOrCoOwner)
	 			return false;
		}
		
		StringList objectSelects=new StringList();
		objectSelects.add(DomainConstants.SELECT_CURRENT);
		objectSelects.addElement(ProgramCentralConstants.SELECT_IS_BUDGET);
		objectSelects.addElement(ProgramCentralConstants.SELECT_IS_COST_ITEM);
		objectSelects.addElement(ProgramCentralConstants.SELECT_IS_FINANCIAL_COST_CATEGORY);
		objectSelects.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
		objectSelects.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);

		DomainObject dmoProjectSpace=DomainObject.newInstance(context, objectId);
		Map mObjectSelectsInfo = dmoProjectSpace.getInfo(context, objectSelects);
		boolean isProjectTemplate = "true".equalsIgnoreCase((String)mObjectSelectsInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE));
		boolean isProjectSpace = "true".equalsIgnoreCase((String)mObjectSelectsInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE));
		boolean isProjectBudget = "true".equalsIgnoreCase((String)mObjectSelectsInfo.get(ProgramCentralConstants.SELECT_IS_BUDGET));
		boolean isFinancialCostItem = "true".equalsIgnoreCase((String)mObjectSelectsInfo.get(ProgramCentralConstants.SELECT_IS_FINANCIAL_COST_CATEGORY));

		boolean hasBudgetItem = false;
		DomainObject budget = DomainObject.newInstance(context);
		if(isProjectTemplate || isProjectSpace){
			objectSelects.add(ProgramCentralConstants.SELECT_ID);
			MapList mlBenefit = dmoProjectSpace.getRelatedObjects(
					context,
					RELATIONSHIP_PROJECT_FINANCIAL_ITEM,
					TYPE_BUDGET,
					objectSelects,
					null,
					false,
					true,
					(short)1,
					null,
					DomainConstants.EMPTY_STRING);

			if(mlBenefit != null && !mlBenefit.isEmpty()){
				return true;
			}
		}else if(isProjectBudget){
			return true;
		}else if(isFinancialCostItem && ProgramCentralUtil.isOfGivenTypeObject(context, DomainObject.TYPE_PROJECT_TEMPLATE, parentID)){
			return true;
		}
		return false;

		//Old Security Impl
		//      	 HashMap programMap = (HashMap) JPO.unpackArgs(args);
		//
		//         String objectId = (String) programMap.get("objectId");
		//         String view= (String) programMap.get(BUDGETVIEWFILTER);
		//
		//         // [ADDED::PRG:RG6:Jan 4, 2011:IR-077536V6R2012 :R211::Start]
		//         String strParentId = ProgramCentralUtil.getParentFromRMBTableRowId(programMap);
		//         
		//         if(ProgramCentralUtil.isNotNullString(strParentId)){
		//      			 objectId = strParentId;
		//      		   }
		//         
		//        // [::PRG:RG6:Jan 4, 2011:IR-Number :R211::End]
		//         StringList objectSelects=new StringList();
		//        objectSelects.add(DomainConstants.SELECT_CURRENT);
		//         objectSelects.addElement(ProgramCentralConstants.SELECT_IS_BUDGET);
		//         objectSelects.addElement(ProgramCentralConstants.SELECT_IS_COST_ITEM);
		//         objectSelects.addElement(ProgramCentralConstants.SELECT_IS_FINANCIAL_COST_CATEGORY);
		//         objectSelects.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
		//         objectSelects.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
		//         
		//         DomainObject dmoProjectSpace=DomainObject.newInstance(context, objectId);
		//         Map mObjectSelectsInfo = dmoProjectSpace.getInfo(context, objectSelects);
		//         boolean isProjectTemplate = "true".equalsIgnoreCase((String)mObjectSelectsInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE));
		//         boolean isProjectSpace = "true".equalsIgnoreCase((String)mObjectSelectsInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE));
		//         boolean isProjectBudget = "true".equalsIgnoreCase((String)mObjectSelectsInfo.get(ProgramCentralConstants.SELECT_IS_BUDGET));
		//         boolean hasBudgetItem = false;
		//         if(isProjectTemplate || isProjectSpace){
		//       	 MapList mlBenefit = dmoProjectSpace.getRelatedObjects(
		//    					context,
		//    					RELATIONSHIP_PROJECT_FINANCIAL_ITEM,
		//    					TYPE_BUDGET,
		//    					objectSelects,
		//    						null,
		//    						false,
		//    						true,
		//    						(short)1,
		//    						null,
		//    						DomainConstants.EMPTY_STRING);
		//
		//            if(isProjectTemplate){
		//            	 if(mlBenefit == null || mlBenefit.isEmpty()){//Added:di7
		//                		  hasBudgetItem = false;
		//            	 }
		//                	  else
		//            		 hasBudgetItem = true;
		//            	 //End:di7
		//            		 
		//            }else{
		//              if(mlBenefit != null && !mlBenefit.isEmpty()){
		//            		  hasBudgetItem = true;
		//              }
		//            }
		//         }else if(isProjectBudget)
		//        	 hasBudgetItem = true;
		//         
		//            return hasBudgetItem;
	}

	/**
	 *  Decides if the Delete Budget command to be displayed or not in Budget Actions
	 * Menu
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @return true if column is to be shown
	 * @throws Exception if operation fails
	 */
	public boolean isBudgetDeleted (Context context, String[] args) throws Exception {
		Map programMap = (Map)JPO.unpackArgs(args);
		String strProjTempObjId = (String) programMap.get("objectId");
		boolean isCommandEnabled= false;
		DomainObject dmoObject = DomainObject.newInstance(context,strProjTempObjId);
		String strBudgetId = dmoObject.getInfo(context, SELECT_BUDGET_ID);
		if(null == strBudgetId && "".equals(strBudgetId) && "null".equals(strBudgetId)){
			DomainObject dmoBudgetObject = DomainObject.newInstance(context,strBudgetId);
			if(dmoBudgetObject.isKindOf(context,DomainConstants.TYPE_FINANCIAL_ITEM)){
				isCommandEnabled = true;
			}
		}
		return isCommandEnabled;
	}

	/**
	 *  Decides if the Estimated View command to be displayed or not in budget summery table
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @return true if column is to be shown
	 * @throws Exception if operation fails
	 */
	public boolean isBudgetFrozen(Context context, String[] args) throws Exception {
		Map programMap = (Map)JPO.unpackArgs(args);
		String strProjTempObjId = (String) programMap.get("objectId");
		boolean isCommandEnabled= true;
		DomainObject dmoObject = DomainObject.newInstance(context,strProjTempObjId);
		String strBudgetId = dmoObject.getInfo(context, SELECT_BUDGET_ID);
		DomainObject dmoBudgetObject = DomainObject.newInstance(context,strBudgetId);
		String strBudgetState = dmoBudgetObject.getInfo(context, SELECT_CURRENT);
		if(strBudgetState.equals(STATE_BUDGET_PLAN_FROZEN)){
			isCommandEnabled = false;
		}
		return isCommandEnabled;
	}


	/** This method returns the cost interval field range values for the create budget webform.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String getCostIntervalRangeValues(Context context,String[]args) throws Exception
	{
		try
		{
			HashMap arguMap = (HashMap)JPO.unpackArgs(args);
			Map paramMap = (Map) arguMap.get("paramMap");
			Map requestMap = (Map) arguMap.get("requestMap");

			String strTableName = (String) requestMap.get("form");
			if(!"PMCProjectTemplateBudgetCreateForm".equals(strTableName)){
				emxProgramCentralUtilBase_mxJPO.getUserPreferenceCurrency(context, "emxProjectBudget.Error.NoDefaultCurrencyDefined");
			}
			String strObjectId = (String) requestMap.get("objectId");
			DomainObject dmoProject = DomainObject.newInstance(context, strObjectId);

			String SELECT_PROJECT_PHASE = SELECT_ID;
			StringList            	slBusSelect = new StringList();
			slBusSelect.add(SELECT_PROJECT_PHASE);
			MapList mlPhaseList = dmoProject.getRelatedObjects(
					context,
					RELATIONSHIP_SUBTASK,
					TYPE_PHASE,
					slBusSelect,
					null,
					false,
					true,
					(short)1,
					null,
					DomainConstants.EMPTY_STRING);

			AttributeType atrCostInterval = new AttributeType(
					Financials.ATTRIBUTE_COST_INTERVAL);
			atrCostInterval.open(context);
			StringList strList = atrCostInterval.getChoices(context);
			atrCostInterval.close(context);
			String sCostInteval = null;

			StringBuffer output = new StringBuffer();
			String strLanguage = (String)paramMap.get("languageStr");
			String key = "emxProgramCentral.ProjectBudget.Interval.";
			String sCostIntervalKey = "";
			for (int i = 0; i < strList.size(); i++) {
				sCostInteval = (String) strList.get(i);
				//Modified:04-Mar-2011:hp5:R211:PRG:IR-098667V6R2012
				// [MODIFIED::Apr 8, 2011:S4E:R212:IR-103070V6R2012x::Start] 
				if(sCostInteval.contains(" "))
				{
					sCostIntervalKey = sCostInteval.replace(" ", "_");
				}
				else{
					sCostIntervalKey=sCostInteval;    				   
				}
				String convertedCostInterval = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						key+sCostIntervalKey, strLanguage);
				if (convertedCostInterval != null) {
					if(mlPhaseList.size() != 0 ){
						if(PROJECT_PHASE.equals(sCostInteval)){
							output.append("<input type='radio' name='CostInterval'  value=\'"+XSSUtil.encodeForHTML(context,sCostInteval)+"' checked='true' />");
							output.append(convertedCostInterval);
						}
						else{
							output.append("<input type='radio' name='CostInterval'  value=\'"+XSSUtil.encodeForHTML(context,sCostInteval)+"'/>");
							output.append(convertedCostInterval);
						}
					}
					else{
						if(PROJECT_PHASE.equals(sCostInteval)){
							output.append("<input type='radio' name='CostInterval'  value=\'"+XSSUtil.encodeForHTML(context,sCostInteval)+"' disabled='true' />");
							output.append(convertedCostInterval);
						}else{
							if(MONTHLY.equals(sCostInteval)){
								output.append("<input type='radio' name='CostInterval'  value=\'"+XSSUtil.encodeForHTML(context,sCostInteval)+"' checked='true' />");
								output.append(convertedCostInterval);
							}
							else{
								output.append("<input type='radio' name='CostInterval'  value=\'"+XSSUtil.encodeForHTML(context,sCostInteval)+"'/>");
								output.append(convertedCostInterval);
								//End:04-Mar-2011:hp5:R211:PRG:IR-098667V6R2012
							}
						}
					}
				}
			}
			String strOuput = output.toString();
			return strOuput;
		}
		catch (Exception exp)
		{
			exp.printStackTrace();
			throw exp;
		}
	}
	/** This is not used as cost interval field will not be editable.
	 * This method displays the selected cost interval in the edit mode of budget
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String getCostIntervalValues(Context context,String[]args) throws Exception
	{
		String strReturnVal = "";
		try
		{
			HashMap arguMap = (HashMap)JPO.unpackArgs(args);
			String sLanguage = context.getSession().getLanguage();
			AttributeType atrDefaultInterval = new AttributeType(Financials.ATTRIBUTE_COST_INTERVAL);
			atrDefaultInterval.open(context);
			StringList strList = atrDefaultInterval.getChoices(context);
			atrDefaultInterval.close(context);
			final String SELECT_COST_INTERVAL = "attribute["+Financials.ATTRIBUTE_COST_INTERVAL+"]";
			String resKey = "emxProgramCentral.ProjectBudget.Interval.";
			Map paramMap = (Map) arguMap.get("paramMap");
			Map requestMap = (Map) arguMap.get("requestMap");
			String strMode = (String) requestMap.get("mode");
			String strObjectId = (String) paramMap.get("objectId");
			DomainObject dmoBudget = DomainObject.newInstance(context, strObjectId);
			String strIntervalRange = "";
			String strIntervalRangeTranslated = "";
			String strInterval = dmoBudget.getInfo(context,SELECT_COST_INTERVAL);
			//Modified:NZF:30-Jul-2011:IR-113014V6R2012x
			String strProjectId = getProjectConnectedToBudget(context, strObjectId);
			boolean isPhasePresentInProject = isHavingPhases(context, strProjectId);
			StringList slIntervalRanges = new StringList();
			StringList slIntervalRangesTranslated = new StringList();
			StringBuffer strDefaultIntervalHTML =new StringBuffer();
			for(int i=0; i<strList.size();i++){
				strIntervalRange = (String)strList.get(i);
				if(strIntervalRange.contains(" "))
					strIntervalRange = strIntervalRange.replace(ProgramCentralConstants.SPACE, "_");
				strIntervalRangeTranslated = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						resKey + strIntervalRange, sLanguage);
				String strReadOnly = "";
				if(strIntervalRange.contains(ProgramCentralConstants.TYPE_PHASE) && !isPhasePresentInProject){
					strReadOnly = "disabled='disabled'";
				}
				if(strIntervalRange.contains("_"))
					strIntervalRange = strIntervalRange.replace("_",ProgramCentralConstants.SPACE);
				if(strInterval.equals(strIntervalRange)){
					strDefaultIntervalHTML.append("<input type='radio' name=\""+Financials.ATTRIBUTE_COST_INTERVAL+"\"  value=\'"+XSSUtil.encodeForHTML(context,strIntervalRange)+"' checked='true' "+strReadOnly+"/>");
					strDefaultIntervalHTML.append(strIntervalRangeTranslated);
				}else{
					strDefaultIntervalHTML.append("<input type='radio' name=\""+Financials.ATTRIBUTE_COST_INTERVAL+"\"  value=\'"+XSSUtil.encodeForHTML(context,strIntervalRange)+"' "+strReadOnly+"/>");
					strDefaultIntervalHTML.append(strIntervalRangeTranslated);
				}
			}
			//Modified:NZF:30-Jul-2011:IR-113014V6R2012x
			strReturnVal = strDefaultIntervalHTML.toString();
			return  strReturnVal;
		}
		catch (Exception exp)
		{
			exp.printStackTrace();
			throw exp;
		}
	}
	/**
	 * Check if there is a Top level Phase in a Project
	 * @param context
	 * @param strObjectId (Project or Template or Concept or their sub type ids)
	 * @return true if phase present false if not
	 * @author NZF
	 * @since 2012x for IR-113014V6R2012x
	 */

	private boolean isHavingPhases(Context context, String strObjectId) throws Exception{    	   
		try {
			boolean isPhasePresent = false;
			DomainObject dmoBudget = DomainObject.newInstance(context,strObjectId);
			MapList mlPhaseList = dmoBudget.getRelatedObjects(context,
					RELATIONSHIP_SUBTASK,
					ProgramCentralConstants.TYPE_PHASE,
					null,	   // bus selects
					null,       // relationshipSelects
					false,      // getTo
					true,       // getFrom
					(short) 1,  // recurseToLevel
					"",		   // objectWhere
					null,	   // relationshipWhere
					0);      	

			if(mlPhaseList.size()>0){
				isPhasePresent = true;
			}
			return isPhasePresent;
		} catch (FrameworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}

	}

	/**
	 * Get Project Connected to a Budget
	 * @param context
	 * @param strBudgetId
	 * @return strProjectId;
	 * @author NZF
	 * @since 2012x for IR-113014V6R2012x
	 */

	private String getProjectConnectedToBudget(Context context, String strBudgetId) throws Exception{

		try {
			String strProjectId = "";
			DomainObject dmoBudget = DomainObject.newInstance(context,strBudgetId);
			strProjectId = dmoBudget.getInfo(context, SELECT_BUDGETS_PROJECT_ID);
			return strProjectId;
		} catch (FrameworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}

	}

	/**
	 * Updates the cost interval of Budget item with a new value.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args Array of strings useful in getting request parameters.
	 * @throws MatrixException if <code>DomainObject</code> operations fail.
	 */
	public void updateCostInterval(Context context,String[]args) throws MatrixException{
		try{
			Map programMap = (Map) JPO.unpackArgs(args);
			Map paramMap = (Map) programMap.get("paramMap");
			Map requestMap = (Map)programMap.get("requestMap");
			String[] arrCostInterval =  (String[])requestMap.get(Financials.ATTRIBUTE_COST_INTERVAL);
			String newCostInterval = DomainObject.EMPTY_STRING;
			if(null != arrCostInterval){
				newCostInterval = arrCostInterval[0];
			}else{
				newCostInterval = (String)paramMap.get("New Value");
			}
			if(newCostInterval.contains("_")){
				newCostInterval = newCostInterval.replace("_",ProgramCentralConstants.SPACE);
			}
			String fItemId = (String)paramMap.get("objectId");
			if(ProgramCentralUtil.isNotNullString(newCostInterval) &&
					ProgramCentralUtil.isNotNullString(fItemId)){
				new FinancialItem().updateCostInterval(context, fItemId, newCostInterval);
			}
		}catch (Exception exp){
			throw new MatrixException(exp);
		}
	}

	/**
	 * This method returns the template enforcement field range values for the create budget template webform.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */


	public String getTempalteEnforcementRangeValues(Context context,String[]args) throws Exception
	{
		try
		{
			String strTempEnforce = EnoviaResourceBundle.getProperty(context, "emxFramework.Budget.TemplateEnforcement") ;

			String strAttrTemplateEnforcement = Financials.ATTRIBUTE_TEMPLATE_ENFORCEMENT;
			AttributeType strTemplateEnforcement = new AttributeType(strAttrTemplateEnforcement);

			strTemplateEnforcement.open(context);
			StringList strList = strTemplateEnforcement.getChoices(context);
			strTemplateEnforcement.close(context);
			String sTempEnforce = null;

			StringBuffer output = new StringBuffer();

			for (int i = 0; i < strList.size(); i++) {
				sTempEnforce = XSSUtil.encodeForHTML(context,(String) strList.get(i));
				if (sTempEnforce != null ) {
					if("False".equalsIgnoreCase(strTempEnforce)){
						if("TRUE".equalsIgnoreCase(sTempEnforce) ){
							output.append("<input type='radio' name='TemplateEnforcement'  value=\'"+sTempEnforce+"' checked='true' />");
							output.append(sTempEnforce);
						}else{
							output.append("<input type='radio' name='TemplateEnforcement'  value=\'"+sTempEnforce+"'/>");
							output.append(sTempEnforce);
						}
					}else{
						if("TRUE".equalsIgnoreCase(sTempEnforce) ){
							output.append("<input type='radio' disabled='true' name='TemplateEnforcementdisable'  value=\'"+sTempEnforce+"' checked='true' />");
							output.append(sTempEnforce);
						}else{
							output.append("<input type='radio' disabled='true' name='TemplateEnforcementdisable'  value=\'"+sTempEnforce+"'/>");
							output.append(sTempEnforce);
						}
					}
				}
			}
			String strOuput = output.toString();
			return strOuput;
		}
		catch (Exception exp)
		{
			exp.printStackTrace();
			throw exp;
		}
	}


	/**
	 * This method displays the selected template enforcement value in the edit mode of budget
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String getTemplateEnforcementValues(Context context,String[]args) throws Exception
	{
		String strReturnVal = "";
		try
		{
			String strTempEnforcePropertiesValue = EnoviaResourceBundle.getProperty(context, "emxFramework.Budget.TemplateEnforcement") ;
			HashMap arguMap = (HashMap)JPO.unpackArgs(args);

			String sLanguage = context.getSession().getLanguage();

			AttributeType attrDefaultTempEnforcement = new AttributeType(Financials.ATTRIBUTE_TEMPLATE_ENFORCEMENT);
			attrDefaultTempEnforcement.open(context);
			StringList strList = attrDefaultTempEnforcement.getChoices(context);
			attrDefaultTempEnforcement.close(context);

			final String ATTR_TEMP_ENFORCE_VAL = "attribute["+Financials.ATTRIBUTE_TEMPLATE_ENFORCEMENT+"]";

			Map paramMap = (Map) arguMap.get("paramMap");
			Map requestMap = (Map) arguMap.get("requestMap");
			String strMode = (String) requestMap.get("mode");
			String strObjectId = (String) paramMap.get("objectId");
			DomainObject dmoBudget = DomainObject.newInstance(context, strObjectId);
			String strTempEnforce = "";
			String strTempEnforceTranslated = "";

			String strEnforcement = dmoBudget.getInfo(context,ATTR_TEMP_ENFORCE_VAL);

			/*
			 * If Mode = edit.
			 */
			StringList slTempEnforce = new StringList();
			StringList slTempEnforceRangesTranslated = new StringList();
			StringBuffer strDefaultEnforceHTML = new StringBuffer();


			for(int i=0; i<strList.size();i++){
				strTempEnforce = (String)strList.get(i);
				strTempEnforce = XSSUtil.encodeForHTML(context, strTempEnforce);
				strTempEnforceTranslated = i18nNow.getRangeI18NString(ATTR_TEMP_ENFORCE_VAL, strTempEnforce, sLanguage);
				if("False".equals(strTempEnforcePropertiesValue)){
					if(strEnforcement.equals(strTempEnforce)){
						//strDefaultEnforceHTML+="<option selected='true' value=\""+strTempEnforce+"\">"+strTempEnforceTranslated+"</option>";
						strDefaultEnforceHTML.append("<input type='radio' name=\""+Financials.ATTRIBUTE_TEMPLATE_ENFORCEMENT+"\"  value=\'"+strTempEnforce+"' checked='true' />");
						strDefaultEnforceHTML.append(strTempEnforceTranslated);
					}else{
						//strDefaultEnforceHTML+="<option value=\""+strTempEnforce+"\">"+strTempEnforceTranslated+"</option>";
						strDefaultEnforceHTML.append("<input type='radio' name=\""+Financials.ATTRIBUTE_TEMPLATE_ENFORCEMENT+"\"  value=\'"+strTempEnforce+"'/>");
						strDefaultEnforceHTML.append(strTempEnforceTranslated);
					}
				}else{
					if(strEnforcement.equals(strTempEnforce) ){
						strDefaultEnforceHTML.append("<input type='radio' disabled='true' name=\""+Financials.ATTRIBUTE_TEMPLATE_ENFORCEMENT+"\"  value=\'"+strTempEnforce+"' checked='true' />");
						strDefaultEnforceHTML.append(strTempEnforceTranslated);
					}else{
						strDefaultEnforceHTML.append("<input type='radio' disabled='true' name=\""+Financials.ATTRIBUTE_TEMPLATE_ENFORCEMENT+"\"  value=\'"+strTempEnforce+"'/>");
						strDefaultEnforceHTML.append(strTempEnforceTranslated);
					}
				}
			}
			// strDefaultEnforceHTML += "</select>";
			strReturnVal = strDefaultEnforceHTML.toString();

			return  strReturnVal;

		}
		catch (Exception exp)
		{
			exp.printStackTrace();
			throw exp;
		}
	}

	/**
	 * This method updates the template enforcement attribute in the edit mode of budget
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public void updateTemplateEnforcement(Context context,String[]args) throws Exception
	{
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String TemplateEnforcement[] = (String[]) requestMap.get(Financials.ATTRIBUTE_TEMPLATE_ENFORCEMENT);
			String strSelectedTempEnforcement = "";
			if(null == TemplateEnforcement){
				strSelectedTempEnforcement = "True";
			}else{
				strSelectedTempEnforcement = TemplateEnforcement[0];
			}
			String objectId = (String) paramMap.get("objectId");
			DomainObject dmoBudget = DomainObject.newInstance(context, objectId);
			//Update the attribute
			dmoBudget.setAttributeValue(context, Financials.ATTRIBUTE_TEMPLATE_ENFORCEMENT, strSelectedTempEnforcement);
		}
		catch (Exception exp)
		{
			exp.printStackTrace();
			throw exp;
		}
	}




	/**
	 * This method returns the date field for the create budget webform.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */

	public String getTimeLineIntervalFrom(Context context, String args[]) throws Exception
	{
		return getTimeLineInterval(context,args,true);
	}



	/**
	 * This method returns the date field for the create budget webform.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */

	public String getTimeLineIntervalTo(Context context, String args[]) throws Exception
	{
		return getTimeLineInterval(context,args,false);
	}

	/**
	 * This method returns the date field for the create budget webform.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String getTimeLineInterval(Context context, String args[],boolean istimeLineIntervalFrom) throws Exception
	{

		Map programMap = (Map)JPO.unpackArgs(args);
		Map requestMap = (Map) programMap.get("requestMap");
		String strObjId = "";

		strObjId = (String) requestMap.get("objectId");


		DomainObject dmoProject = DomainObject.newInstance(context, strObjId);
		String SELECT_ATTRIBUTE_TASK_ESTIMATED_DATE = "";
		String strTimeLineInterval = "";
		final String SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE = "attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE  + "]";
		final String SELECT_ATTRIBUTE_TASK_ESTIMATED_END_DATE = "attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE  + "]";
		if(istimeLineIntervalFrom){
			SELECT_ATTRIBUTE_TASK_ESTIMATED_DATE = SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE ;
			strTimeLineInterval ="TimeLineIntervalFrom" ;
		}else{
			SELECT_ATTRIBUTE_TASK_ESTIMATED_DATE =  SELECT_ATTRIBUTE_TASK_ESTIMATED_END_DATE;
			strTimeLineInterval = "TimeLineIntervalTo";
		}

		StringList slBusSelect = new StringList();
		slBusSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_DATE);

		Map mapObjInfo = dmoProject.getInfo(context, slBusSelect);
		String strStartDate = (String) mapObjInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_DATE);
		Date dtStartDate = new Date(strStartDate);

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat
				.getEMatrixDateFormat(), Locale.US);
		String StartDate           = sdf.format(dtStartDate);
		String timezone                = (String) requestMap.get("timeZone");
		double dbTimeZone              = Task.parseToDouble(timezone);

		Locale strLocale = (Locale)requestMap.get("localeObj");
		strStartDate     = eMatrixDateFormat.getFormattedDisplayDate(StartDate, dbTimeZone,strLocale);

		final String ROW_NAME = strTimeLineInterval;
		StringBuffer strHTMLBuffer = new StringBuffer(64);
		strHTMLBuffer.append("<input type='text' readonly='readonly' size='' name='").append(XSSUtil.encodeForHTML(context,ROW_NAME)).append("' value=\'"
				+ XSSUtil.encodeForXML(context,strStartDate) +"'/>");
		strHTMLBuffer.append("<a href=\"javascript:showCalendar('emxCreateForm', '").append(XSSUtil.encodeForHTML(context,ROW_NAME)).append("', '')\">");
		strHTMLBuffer.append("<img src='../common/images/iconSmallCalendar.gif' border='0' valign='absmiddle'/>");
		strHTMLBuffer.append("</a>");

		return strHTMLBuffer.toString();

	}



	/**
	 * This method returns the main cost categories connected to the company for Add Cost Item command
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTableCostCategoryItemData(Context context,String[] args) throws Exception
	{
		try
		{
			MapList mlBasicCategories = new MapList();
			//Create result vector
			String strFinancialParentCostCategory = "";
			// Get object list information from packed arguments
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			String strObjectId = "";
			DomainObject dmoObject;
			Map mapTableData = null;

			strObjectId =(String) programMap.get("objectId");
			FinancialTemplateCategory FTC = new FinancialTemplateCategory();
			StringList busSelects = new StringList();
			busSelects.add(FTC.SELECT_ID);
			busSelects.add(FTC.SELECT_NAME);
			mlBasicCategories = FTC.getCostCategories(context,1, busSelects, null);
			return mlBasicCategories;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new MatrixException(ex);
		}

	}


	//     /**
	/* This method returns the cost items of the cost categories for Add Cost Item command
	 * when user expands the cost category in the budget summary table.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getExpandTableCostCategoryItemData(Context context,String[] args) throws MatrixException
	{
		try
		{
			MapList mlBasicCategories = new MapList();
			//Create result vector
			String strFinancialParentCostCategory = "";
			// Get object list information from packed arguments
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			String strCategoryId = "";
			String strCategoryName = "";
			Map mapTableData = null;

			StringList busSelects = new StringList();
			busSelects.add(DomainConstants.SELECT_ID);
			busSelects.add(DomainConstants.SELECT_NAME);
			busSelects.add(DomainConstants.SELECT_REVISION);
			String busWhere = "";
			String strProjectID = (String) programMap.get("ProjectId");
			DomainObject dmoProject = DomainObject.newInstance(context, strProjectID);
			StringList busSelects1 = new StringList();
			StringList relSelect = new StringList();
			String strRelationshipType = RELATIONSHIP_PROJECT_FINANCIAL_ITEM;
			busSelects1.add(DomainConstants.SELECT_ID);
			busSelects1.add(DomainConstants.SELECT_NAME);
			busSelects1.add(DomainConstants.SELECT_TYPE);
			String strTypePattern = TYPE_BUDGET;
			MapList mlBudget = dmoProject.getRelatedObjects(
					context,
					strRelationshipType,
					strTypePattern,
					busSelects1,
					relSelect,
					false,
					true,
					(short)1,
					null,
					DomainConstants.EMPTY_STRING,
					0);
			Map mapBudget = null;
			DomainObject dmoBudget = null;
			for (Iterator iterBudget = mlBudget.iterator(); iterBudget .hasNext();)
			{
				mapBudget = (Map) iterBudget.next();
				String strBudgetId = (String)mapBudget.get(SELECT_ID);
				dmoBudget = DomainObject.newInstance(context, strBudgetId);
			}
			MapList mlBudgetCostItems = dmoBudget.getRelatedObjects(
					context,        // context
					Financials.RELATIONSHIP_FINANCIAL_ITEMS,
					TYPE_COST_ITEM, // type filter.
					busSelects,     // object selectables.
					null,           // relationship selectables
					false,          // expand to direction.
					true,           // expand from direction
					(short) 1,  // level
					busWhere,       // object where clause
					null);

			strCategoryId =(String) programMap.get("objectId");
			DomainObject dmoObject = DomainObject.newInstance(context, strCategoryId);
			strCategoryName = dmoObject.getName(context);
			MapList mapList = dmoObject.getRelatedObjects(
					context,        // context
					Financials.RELATIONSHIP_FINANCIAL_SUB_CATEGORIES,
					TYPE_FINANCIAL_COST_CATEGORY, // type filter.
					busSelects,     // object selectables.
					null,           // relationship selectables
					false,          // expand to direction.
					true,           // expand from direction
					(short) 1,  // level
					busWhere,       // object where clause
					null);

			Map mapBudgetInfo = null;
			for (Iterator iterBudget = mapList.iterator(); iterBudget .hasNext();)
			{
				mapBudgetInfo = (Map) iterBudget.next();
				String CostItem = (String)mapBudgetInfo.get(DomainConstants.SELECT_NAME);
				Iterator budgetCostItem = mlBudgetCostItems.iterator();
				while (budgetCostItem.hasNext()) {
					Map CostItemMap = (Map) budgetCostItem.next();
					String SelectedCostItem = (String)CostItemMap.get(DomainConstants.SELECT_NAME);
					String SelectedCostRev = (String)CostItemMap.get(DomainConstants.SELECT_REVISION);
					String strFinItemParentName = null;
					int index = SelectedCostRev.indexOf("-");
					if (index != -1) {
						strFinItemParentName = SelectedCostRev.substring(index+1, SelectedCostRev.length());
					}
					if(CostItem.equals(SelectedCostItem)){
						if(strCategoryName.equals(strFinItemParentName)){
							iterBudget.remove();
						}
					}
				}

			}

			return mapList;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new MatrixException(ex);
		}

	}

	public Vector getColumnCostCategoryItemData(Context context,String[] args) throws Exception
	{
		try
		{
			//Create result vector
			Vector vecResult = new Vector();
			String strFinancialParentCostCategory = "";
			// Get object list information from packed arguments
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map mapRowData = null;
			String strObjectId = "";
			String strObjectName = "";
			DomainObject dmoObject;
			String strFullName ="";
			String strLanguage=context.getSession().getLanguage();
			Iterator mainCategoryItr = objectList.iterator();
			while (mainCategoryItr.hasNext()) {
				Map mainCategoryMap = (Map) mainCategoryItr.next();
				String strParentCostCategoryID = (String) mainCategoryMap.get(DomainConstants.SELECT_ID);
				String strParentCostCategoryName = (String) mainCategoryMap.get(DomainConstants.SELECT_NAME);
				String strFormedName = FrameworkUtil.findAndReplace(strParentCostCategoryName.trim()," ", "_");
				String strKey = "emxProgramCentral.Common."+strFormedName;
				String stri18nName = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						strKey, strLanguage);
				vecResult.add(stri18nName);
			}
			return vecResult;
		}
		catch (Exception ex){
			ex.printStackTrace();
			throw new MatrixException(ex);
		}
	}

	/**
	 * hasProjectPhaseForBudget
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds objectId of Type Budget.
	 * @throws Exception if the operation fails
	 * @since PMC R210
	 * @author KP2
	 * Used as Access Function for Phase Column in PMCBudgetActualTransactionSummary table
	 */
	public boolean hasProjectPhaseForBudget(Context context,String[] args) throws Exception{
		boolean bReturnValue = false;
		HashMap requestMap          = (HashMap) JPO.unpackArgs(args);
		String strObjId = (String)requestMap.get("objectId");
		if(strObjId != null && !"".equals(strObjId)){
			DomainObject objBudget = DomainObject.newInstance(context, strObjId);
			if(objBudget.isKindOf(context, TYPE_BUDGET)){
				String strCostInterval = objBudget.getAttributeValue(context, Financials.ATTRIBUTE_COST_INTERVAL);
				if(CostItem.PROJECT_PHASE.equals(strCostInterval)){
					bReturnValue = true;
				}
			}
		}
		return bReturnValue;
	}



	/**
	 * getActualTransactionSummaryList
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds objectId of Type Budget.
	 * @throws Exception if the operation fails
	 * @since PMC R210
	 * @author KP2
	 *
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getBudgetActualTransactionSummaryList(Context context,String[] args) throws Exception
	{
		HashMap requestMap          = (HashMap) JPO.unpackArgs(args);
		String strObjId = (String)requestMap.get("objectId");
		MapList ActualTransactionList = new MapList();
		if(strObjId != null && !"".equals(strObjId)){
			String strRelFinancialItems = PropertyUtil.getSchemaProperty(context, SYMBOLIC_RELATIONSHIP_FINANCIAL_ITEMS);
			String strRelActualTransactionItem = PropertyUtil.getSchemaProperty(context, SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_ITEM);
			String sCommandStatement = "print bus $1 select $2 dump $3";
			String result  = "";
			try{
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				result =  MqlUtil.mqlCommand(context, sCommandStatement,strObjId, "from["+ strRelFinancialItems +"].businessobject.to["+ strRelActualTransactionItem +"].from.id", "|");
			}finally{
				ContextUtil.popContext(context);
			}
			StringTokenizer st = new StringTokenizer(result,"|");
			while(st.hasMoreTokens()){
				String strId = st.nextToken();
				Map map = new HashMap();
				map.put(DomainConstants.SELECT_ID,strId);
				ActualTransactionList.add(map);
			}
		}
		return ActualTransactionList;
	}

	/**
	 * Creates a new actual transaction object for a cost item.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Argument string array containing program map details.
	 * @return A map with action status that indicates the success or failure of 
	 * actual transaction creation.
	 * @throws MatrixException if preferred currency retrieval operation fails, or
	 * if operations on DomainObject fail.
	 */
	@com.matrixone.apps.framework.ui.ConnectionProgramCallable
	public Map addNewActualTransaction(Context context,String[] args) throws MatrixException
	{
		String strRelActualTransactionItem = PropertyUtil.getSchemaProperty(context,SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_ITEM);
		String strRelActualTransactionPhase = PropertyUtil.getSchemaProperty(context,SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_PHASE);
		String strATPONo = PropertyUtil.getSchemaProperty(context, SYMBOLIC_ATTRIBUTE_TRANSACTION_PON);
		String strATDate = PropertyUtil.getSchemaProperty(context, SYMBOLIC_ATTRIBUTE_TRANSACTION_DATE);
		String strATAmount = PropertyUtil.getSchemaProperty(context, SYMBOLIC_ATTRIBUTE_TRANSACTION_AMOUNT);
		HashMap doc = new HashMap();
		MapList mlItems = new MapList();
		boolean showCurrencyConversionWarning = false;
		String strCurrencyConversionWarning = "";
		try{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");
			String projectId = (String) paramMap.get("projectId");
			double clientTZOffset  = Task.parseToDouble((String)paramMap.get("timeZone"));
			Element elm = (Element) programMap.get("contextData");
			MapList chgRowsMapList = UITableIndented.getChangedRowsMapFromElement(context, elm);
			String rowFormat = "";
			Locale locale = (Locale)programMap.get("localeObj");

			for (int i = 0; i < chgRowsMapList.size(); i++) {
				try {
					HashMap changedRowMap = (HashMap) chgRowsMapList.get(i);
					String childObjectId = (String) changedRowMap.get("childObjectId");
					String sRelId = (String) changedRowMap.get("relId");
					String sRowId = (String) changedRowMap.get("rowId");
					rowFormat = "[rowId:" + sRowId + "]";
					String sRelTypeSymb = (String) changedRowMap.get("relType");
					String markup = (String) changedRowMap.get("markup");
					HashMap columnsMap = (HashMap) changedRowMap.get("columns");
					String strDate = (String) columnsMap.get("Transaction Date");
					String strFormattedInputDate = eMatrixDateFormat.getFormattedInputDate(context, strDate,clientTZOffset, locale);
					Date transactionDate = eMatrixDateFormat.getJavaDate(strFormattedInputDate,locale);
					String strDescription = (String) columnsMap.get("Description");
					String strPONo = (String) columnsMap.get("PO No");
					String strCostItemId = (String) columnsMap.get("Cost Item");
					String strPhaseId = (String) columnsMap.get("Phase");
					String strTransactionAmount = (String) columnsMap.get("Amount");
					String strTransactionName = (String) columnsMap.get("Name");
					String strPrefferedCurrency = Currency.getBaseCurrency(context,projectId);
					try{
						if(ProgramCentralUtil.isNullString(strTransactionAmount)) strTransactionAmount = "0";
						strTransactionAmount = Currency.toBaseCurrency(context,projectId,strTransactionAmount,true);
					}catch(MatrixException e){
						showCurrencyConversionWarning = true;
						strCurrencyConversionWarning = e.getMessage();
					}
					strTransactionAmount = strTransactionAmount + " " + strPrefferedCurrency;
					DomainObject objActualTransaction = DomainObject.newInstance(context);
					String strType = PropertyUtil.getSchemaProperty(context,SYMBOLIC_TYPE_ACTUAL_TRANSACTION);
					String strPolicy = PropertyUtil.getSchemaProperty(context, SYMBOLIC_POLICY_ACTUAL_TRANSACTION);
					String autoName = "";

					if(null == strTransactionName || "null".equals(strTransactionName) || "".equals(strTransactionName)){
						autoName = "AT-" + FrameworkUtil.autoName(context,		//Context context
								FrameworkUtil.getAliasForAdmin(context,					//String type
										DomainConstants.SELECT_TYPE,
										strType,
										true),
										DomainConstants.EMPTY_STRING,	     	//String revision
										DomainConstants.EMPTY_STRING,			//String policy
										DomainConstants.EMPTY_STRING,			//String vault
										DomainConstants.EMPTY_STRING,			//String customRev
										true,									//boolean uniqueNameOnly
										false);								//boolean useSuperUser

					}else{
						autoName = strTransactionName;
					}

					objActualTransaction.createObject(context,strType , autoName, "", strPolicy, context.getVault().getName());
					String newATId = objActualTransaction.getId();


					if(strCostItemId != null && !"".equals(strCostItemId))
					{	// Connecting to Cost Item
						objActualTransaction.addToObject(context, new RelationshipType(strRelActualTransactionItem), strCostItemId);
					}else{

					}
					if(strPhaseId != null && !"".equals(strPhaseId) && !"null".equalsIgnoreCase(strPhaseId)){
						//Connecting to Phase
						objActualTransaction.addToObject(context, new RelationshipType(strRelActualTransactionPhase), strPhaseId);
					}else{

					}

					if(strDescription != null && !"".equals(strDescription)){
						//Updating Description
						objActualTransaction.setDescription(context,strDescription);
					}
					// Cost Item null check is not done here as it is mandatory on table
					DomainObject objCostItem = DomainObject.newInstance(context, strCostItemId);
					String strSelectCostIntervalStartDate = "to["+Financials.RELATIONSHIP_FINANCIAL_ITEMS+"].from.attribute["+Financials.ATTRIBUTE_COST_INTERVAL_START_DATE +"].value";
					String strSelectCostIntervalEndDate = "to["+Financials.RELATIONSHIP_FINANCIAL_ITEMS+"].from.attribute["+Financials.ATTRIBUTE_COST_INTERVAL_END_DATE +"].value";

					String strCostIntervalStartDate = objCostItem.getInfo(context,strSelectCostIntervalStartDate);
					String strCostIntervalEndDate = objCostItem.getInfo(context,strSelectCostIntervalEndDate);

					Date costIntervalStartDate = eMatrixDateFormat.getJavaDate(strCostIntervalStartDate);
					Date costIntervalEndDate = eMatrixDateFormat.getJavaDate(strCostIntervalEndDate);
					//Date transDate = eMatrixDateFormat.getJavaDate(strDate);

					Calendar startCal = Calendar.getInstance();
					startCal.setTime(costIntervalStartDate);
					Calendar endCal = Calendar.getInstance();
					endCal.setTime(costIntervalEndDate);
					Calendar transCal = Calendar.getInstance();
					transCal.setTime(transactionDate);
					transCal.set(Calendar.HOUR, startCal.get(Calendar.HOUR));
					transCal.set(Calendar.MINUTE, startCal.get(Calendar.MINUTE));
					transCal.set(Calendar.SECOND, startCal.get(Calendar.SECOND));
					transCal.set(Calendar.AM_PM, startCal.get(Calendar.AM_PM));

					if(transCal.before(startCal) || transCal.after(endCal)){

						SimpleDateFormat sdf = new SimpleDateFormat("EEE d MMM yyyy");
						String startDate = sdf.format(costIntervalStartDate);
						String endDate = sdf.format(costIntervalEndDate);

						i18nNow loc = new i18nNow();
						String strLanguage=context.getSession().getLanguage();
						final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
						String errMessage = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Budget.ActualTransaction.Error.InvalidDate");
						throw new IllegalDataException(errMessage+"\n"+ startDate+" - "+endDate);
					}

					//Updating the attributes
					HashMap attributeMap = new HashMap();
					attributeMap.put(strATPONo, strPONo);
					attributeMap.put(strATDate, strFormattedInputDate);
					attributeMap.put(strATAmount, strTransactionAmount);
					objActualTransaction.setAttributeValues(context, attributeMap);

					String [] argList = new String [2];
					argList[0] = newATId;
					argList[1] = Financials.ATTRIBUTE_TRANSACTION_AMOUNT;
					transactionAmountModifyPostProcess(context, argList);


					// creating a returnMap having all the details about the changed row.
					HashMap returnMap = new HashMap();
					returnMap.put("oid", newATId);
					returnMap.put("rowId", sRowId);
					returnMap.put("pid", "");
					returnMap.put("relId", sRelId);
					returnMap.put("markup", markup);
					returnMap.put("childObjectId",childObjectId);
					returnMap.put("relType",sRelTypeSymb);
					columnsMap.put("Name",autoName);
					returnMap.put("columns", columnsMap);
					mlItems.add(returnMap);
					if(showCurrencyConversionWarning)
						emxContextUtilBase_mxJPO.mqlNotice(context, strCurrencyConversionWarning);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					throw e;
				}
			}
			doc.put("Action", "success");
			doc.put("changedRows", mlItems);

		}catch(Exception ex){

			throw new MatrixException(ex.getMessage());
		}

		return doc;
	}


	/**
	 * actualTransactionModifyAction
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds map with objectId of Type Actual Transaction.
	 * @throws Exception if the operation fails
	 * @since PMC R210
	 * @author KP2
	 *
	 * This method is a trigger program
	 */

	public int actualTransactionModifyAction(Context context,String[] args) throws Exception{
		if (args == null || args.length < 2) {
			throw (new IllegalArgumentException());
		}
		int returnValue = 0;
		int index = 0;
		String objectId = args[index];
		String attrName = args[++index];
		if(attrName != null && attrName.equals(Financials.ATTRIBUTE_TRANSACTION_AMOUNT) ){
			returnValue =  transactionAmountModifyPostProcess(context, args);
		}
		return returnValue;
	}


	/**
	 * transactionAmountModifyPostProcess
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds map with objectId of Type Actual Transaction and the attribute name.
	 * @throws Exception if the operation fails
	 * @since PMC R210
	 * @author KP2
	 *
	 */

	public int transactionAmountModifyPostProcess(Context context,String[] args)throws Exception{
		if (args == null || args.length < 2) {
			throw (new IllegalArgumentException());
		}
		String strRelActualTransactionItem = PropertyUtil.getSchemaProperty(context,SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_ITEM);
		String strAttributeTransactionDate = PropertyUtil.getSchemaProperty(context,SYMBOLIC_ATTRIBUTE_TRANSACTION_DATE);
		String strTypeActualTransaction = PropertyUtil.getSchemaProperty(context,SYMBOLIC_TYPE_ACTUAL_TRANSACTION);
		int index = 0;
		String objectId = args[index];
		String attrName = args[++index];
		if(objectId != null && ! "".equals(objectId)){
			DomainObject objActualTransaction =  DomainObject.newInstance(context,objectId);
			if(objActualTransaction.isKindOf(context, strTypeActualTransaction) && attrName.equals(Financials.ATTRIBUTE_TRANSACTION_AMOUNT)){
				String strActualTransDate = objActualTransaction.getAttributeValue(context, strAttributeTransactionDate);
				String strCostItemId = objActualTransaction.getInfo(context,"from[" + strRelActualTransactionItem + "].to.id");
				if(strCostItemId != null && !"".equals(strCostItemId) && strActualTransDate != null && !"".equals(strActualTransDate)){
					DomainObject objCostItem = DomainObject.newInstance(context,strCostItemId);
					String strBudgetId = objCostItem.getInfo(context, "to["+ RELATIONSHIP_FINANCIAL_ITEMS +"].from.id");
					DomainObject budget = DomainObject.newInstance(context, strBudgetId);
					String strProjectId = budget.getInfo(context, "to["+ RELATIONSHIP_PROJECT_FINANCIAL_ITEM +"].from.id");
					Double dblCostItemIntervalActualCost = new Double(0.0);
					Double dblCostItemActualCost = new Double(0.0);
					String strPrefferedCurrency = Currency.getBaseCurrency(context,strProjectId );
					Map mapActualTransactionAmount = getActualTransactionAmountSumMap(context,objectId);
					dblCostItemIntervalActualCost = (Double)mapActualTransactionAmount.get("ATForIntervalAmount");
					dblCostItemActualCost = (Double)mapActualTransactionAmount.get("AllATAmount");
					String strCostItemIntervalActualCost = dblCostItemIntervalActualCost + ProgramCentralConstants.SPACE + strPrefferedCurrency; 
					String strCostItemActualCost = dblCostItemActualCost + ProgramCentralConstants.SPACE + strPrefferedCurrency;
					MapList relList = getCostItemIntervalRelMap(context, objectId);
					if(relList != null && relList.size() > 0){
						Map relMap = (Map) relList.get(0);
						String relId = (String) relMap.get(DomainRelationship.SELECT_ID);
						if(relId != null){
							DomainRelationship domRel = DomainRelationship.newInstance(context, relId);
							//set the Actual Cost Attribute of Cost Item Interval relationship with sum of Transaction Amount of transaction for that interval.
							domRel.setAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST, strCostItemIntervalActualCost);
						}
					}else{
						String strRelActualTransactionPhase = PropertyUtil.getSchemaProperty(context,SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_PHASE);
						String strPhaseName = objActualTransaction.getInfo(context, "from["+ strRelActualTransactionPhase +"].to.name");
						String strPhaseId = objActualTransaction.getInfo(context, "from["+ strRelActualTransactionPhase +"].to.id");
						String[]strPhase = new String[1];
						strPhase[0]= strPhaseId;
						Map mapRelData = DomainRelationship.connect(context, objCostItem, RELATIONSHIP_COST_ITEM_INTERVAL, true,strPhase);
						String RelID = (String)mapRelData.get(strPhaseId);
						DomainRelationship.setAttributeValue(context,RelID,ATTRIBUTE_ACTUAL_COST,strCostItemIntervalActualCost);
					}

					//set the Actual Cost Attribute of Cost Item object with sum of Transaction Amount of all connected Transactions
					objCostItem.setAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST, strCostItemActualCost);
					String[] arguments = new String[2];
					arguments[0] = strBudgetId;
					arguments[1] = "1";
					double dBudgetRolledUpCost = getBudgetRollUpCost(context, arguments);
					String strBudgetRolledUpCost = dBudgetRolledUpCost  + ProgramCentralConstants.SPACE + strPrefferedCurrency; 
					if(strBudgetId != null && !"".equals(strBudgetId)){
						//set the Actual Cost Attribute of Budget object with sum of Actual Cost on all Cost Items connected to Budget
						budget.setAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST, strBudgetRolledUpCost);
					}
				}
			}
		}
		else{
			throw (new IllegalArgumentException());
		}
		return 0;
	}

	/**
	 * getActualTransactionAmountSumForPhase
	 * @param context the eMatrix <code>Context</code> object
	 * @param costItemId holds map with objectId of Cost Item.
	 * @param fromDate holds start date of Budget
	 * @param toDate holds end date of Budget
	 * @throws Exception if the operation fails
	 * @return Map with one double value of sum for All Actual Transaction Amounts for Phase
	 * 			and second with  sum for All Actual Transaction Amounts connected to Cost Item.
	 * Keys are 1. "ATForPhaseAmount" and 2. "AllATAmount"
	 * @since PMC R210
	 * @author KP2
	 *
	 */

	public Map getActualTransactionAmountSumForPhase(Context context, String costItemId, String strPhase)throws Exception{
		double returnValueAllAT = 0.0;
		double returnValueATForPhase = 0.0;
		try{
			if (costItemId != null && !"".equals(costItemId) ) {
				String strRelActualTransactionItem = PropertyUtil.getSchemaProperty(context,SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_ITEM);
				String strTypeActualTransaction = PropertyUtil.getSchemaProperty(context,SYMBOLIC_TYPE_ACTUAL_TRANSACTION);
				String strATAmount = PropertyUtil.getSchemaProperty(context, SYMBOLIC_ATTRIBUTE_TRANSACTION_AMOUNT);
				String strRelActualTransactionPhase = PropertyUtil.getSchemaProperty(context,SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_PHASE);

				DomainObject objCostItem = DomainObject.newInstance(context, costItemId);
				String objWhere = null;
				StringList objSelect = new StringList(3);
				objSelect.add(DomainObject.SELECT_ID);
				objSelect.add("from["+ strRelActualTransactionPhase +"].to.name");
				objSelect.add("attribute[" + strATAmount + "].value");
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),
						DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				MapList objATList = objCostItem.getRelatedObjects(context,
						strRelActualTransactionItem, 		//String relPattern
						strTypeActualTransaction, 		//String typePattern
						objSelect,          	//StringList objectSelects,
						null,            		//StringList relationshipSelects,
						true,                  //boolean getTo,
						false,                   //boolean getFrom,
						(short)1,               //short recurseToLevel,
						objWhere,          		//String objectWhere,
						null,     		        //String relationshipWhere,
						0,						//int limit
						null,                   //Pattern includeType,
						null,                   //Pattern includeRelationship,
						null);
				if(objATList != null){
					for (Iterator itr=objATList.iterator();itr.hasNext();) {
						Map objectMap = (Map) itr.next();
						String strPhaseName = (String) objectMap.get("from["+ strRelActualTransactionPhase +"].to.name");
						if(strPhaseName != null && strPhaseName.equals(strPhase)){
							returnValueATForPhase += Task.parseToDouble((String)objectMap.get("attribute[" + strATAmount + "].value"));
						}
						returnValueAllAT += Task.parseToDouble((String)objectMap.get("attribute[" + strATAmount + "].value"));
					}
				}

			}else{
				throw (new IllegalArgumentException());
			}
			Map returnMap = new HashMap<String, Double>();
			returnMap.put("AllATAmount",new Double(returnValueAllAT));
			returnMap.put("ATForIntervalAmount",new Double(returnValueATForPhase));
			return returnMap;
		}catch(Exception e){
			throw e;
		}finally{
			ContextUtil.popContext(context);
		}
	}

	/**
	 * Calculates the sum of all Actual Transaction costs lying in an interval.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param costItemId the String id of Cost item for which total actual transaction cost 
	 * @param strCostInterval the fiscal interval type string. MONTHLY/WEEKLY/QUARTERLY
	 * @param intervalDate Start date of fiscal interval on which newly created Actual transaction 
	 * is lying.
	 * @return a Map of 1. Total cost of all Actual Transactions connected to a cost item(costItemId), and
	 * 2. Total cost of all Actual Transactions lying on passed interval date(intervalDate).  
	 * @throws Exception if operation fails.
	 */
	public Map getActualTransactionAmountSum(Context context,String costItemId,String strCostInterval,Date intervalDate)throws Exception{
		double returnValueAllAT = 0.0;
		double returnValueATForInterval = 0.0;
		try{
			if (costItemId != null && !"".equals(costItemId) ) {
				String strRelActualTransactionItem = PropertyUtil.getSchemaProperty(context,SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_ITEM);
				String strTypeActualTransaction = PropertyUtil.getSchemaProperty(context,SYMBOLIC_TYPE_ACTUAL_TRANSACTION);
				String strATAmount = PropertyUtil.getSchemaProperty(context, SYMBOLIC_ATTRIBUTE_TRANSACTION_AMOUNT);
				String strATDate = PropertyUtil.getSchemaProperty(context, SYMBOLIC_ATTRIBUTE_TRANSACTION_DATE);
				SimpleDateFormat sdf=new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat());
				DomainObject objCostItem = DomainObject.newInstance(context, costItemId);
				StringList objSelect = new StringList(3);
				objSelect.add(DomainObject.SELECT_ID);
				objSelect.add("attribute[" + strATDate + "].value");
				objSelect.add("attribute[" + strATAmount + "].value");
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				MapList objATList = objCostItem.getRelatedObjects(context,
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
						if(MONTHLY.equalsIgnoreCase(strCostInterval))
							atDate = Financials.getFiscalMonthIntervalStartDate(atDate);
						else if(WEEKLY.equalsIgnoreCase(strCostInterval))
							atDate = Financials.getFiscalWeekIntervalStartDate(atDate);
						else if(QUARTERLY.equalsIgnoreCase(strCostInterval))
							atDate = Financials.getFiscalQuarterIntervalStartDate(atDate);
						atStart.setTime(atDate);

						if(atStart.equals(intervalStart)){
							returnValueATForInterval += Task.parseToDouble((String)objectMap.get("attribute[" + strATAmount + "].value"));
						}
						returnValueAllAT += Task.parseToDouble((String)objectMap.get("attribute[" + strATAmount + "].value"));
					}
				}
			}else{
				throw (new IllegalArgumentException());
			}
			Map returnMap = new HashMap<String, Double>();
			returnMap.put("AllATAmount",new Double(returnValueAllAT));
			returnMap.put("ATForIntervalAmount",new Double(returnValueATForInterval));
			return returnMap;
		}catch(Exception e){
			throw e;			
		}finally{
			ContextUtil.popContext(context);
		}
	}

	/**
	 * getBudgetRollUpCost
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds map with objectId of Budget and the level (optional).
	 * @throws Exception if the operation fails
	 * @since PMC R210
	 * @author KP2
	 *
	 */
	public double getBudgetRollUpCost(Context context, String[] args)throws Exception{
		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}
		int index = 0;
		String objectId = null;
		int level = 1;
		objectId = args[index];
		try {
			level = Integer.parseInt(args[++index]);
		}catch(ArrayIndexOutOfBoundsException e){
			level = 1;
		}catch(NumberFormatException e){
			level = 1;
		}

		double dReturnValue = 0.0;
		String strTypeBudget = PropertyUtil.getSchemaProperty(context, SYMBOLIC_TYPE_Budget);

		if(objectId != null && !"".equals(objectId)){
			DomainObject domBudget = DomainObject.newInstance(context,objectId);
			if(domBudget.isKindOf(context, strTypeBudget )){

				StringList objSelect = new StringList(2);
				objSelect.add(DomainObject.SELECT_ID);
				objSelect.add("attribute[" + Financials.ATTRIBUTE_ACTUAL_COST + "].value");

				MapList objCostItemList = domBudget.getRelatedObjects(context,
						Financials.RELATIONSHIP_FINANCIAL_ITEMS, 	//String relPattern
						Financials.TYPE_COST_ITEM,						//String typePattern
						objSelect,					//StringList objectSelects,
						null,   				//StringList relationshipSelects,
						false,					//boolean getTo,
						true,                   //boolean getFrom,
						(short)1,               //short recurseToLevel,
						null,          		    //String objectWhere,
						null,     		        //String relationshipWhere,
						0,						//int limit
						null,                   //Pattern includeType,
						null,                   //Pattern includeRelationship,
						null);
				if(objCostItemList != null && !"".equals(objCostItemList)){
					for (Iterator itr=objCostItemList.iterator();itr.hasNext();) {
						Map objectMap = (Map) itr.next();
						String strCost = (String) objectMap.get("attribute[" + Financials.ATTRIBUTE_ACTUAL_COST + "].value");
						dReturnValue += Task.parseToDouble(strCost);
					}
				}
			}
		}
		return dReturnValue;
	}


	/**
	 * Updates the date of existing Actual Transaction object. 
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args holds map with objectId of Type Actual Transaction and the new value for the Transaction Date.
	 * @throws Exception if operation fails.
	 */
	public void updateActualTransactionDate(Context context,String[] args) throws Exception
	{
		try{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");
			HashMap requestMap = (HashMap)programMap.get("requestMap");
			String projectId = (String)requestMap.get("projectId");
			if(isBackgroundTaskActive(context, projectId)){
				getCurrencyModifyBackgroundTaskMessage(context, true);
				return;
			}			
			String strNewDate = (String)paramMap.get("New Value");
			String strATId = (String)paramMap.get("objectId");
			double clientTZOffset  = Task.parseToDouble((String)requestMap.get("timeZone"));
			String strATDate = PropertyUtil.getSchemaProperty(context, SYMBOLIC_ATTRIBUTE_TRANSACTION_DATE);
			DomainObject objActualTransaction = DomainObject.newInstance(context,strATId);
			strNewDate = eMatrixDateFormat.getFormattedInputDate(context, strNewDate, clientTZOffset, ProgramCentralUtil.getLocale(context));
			String strOldDate =  objActualTransaction.getAttributeValue(context, strATDate);
			//set new date
			objActualTransaction.setAttributeValue(context, strATDate, strNewDate);

			String[] argList = new String[4];
			argList[0] = strATId;
			argList[1] = strOldDate;
			argList[2] = strNewDate;
			argList[3] = projectId;

			actualTransactionModifyTransactionDateProcess(context, argList);
		}catch(Exception e){
			throw new Exception(e);
		}
	}

	/**
	 * Updates the actual transaction cost of the intervals on which old and new transaction dates are lying.
	 * In old interval, the method reduces the actual cost by the cost of modified actual transaction
	 * and in new interval, the method adds up the actual cost  by the cost of modified actual transaction. 
	 * @param context the ENOVIA <code>Context</code> user.
	 * @param args the String array holding request parameters like object id of modified Actual transaction.
	 * @return 0 if modification succeed.
	 * @throws Exception if operation fails.
	 */
	public int actualTransactionModifyTransactionDateProcess(Context context, String[] args) throws Exception {
		if (args == null || args.length < 3) {
			throw (new IllegalArgumentException());
		}
		int cnt = 0;
		String strATId = args[cnt];
		String strOldDate = args[++cnt];
		String strNewDate = args[++cnt];
		String projectId = args[++cnt];
		if(ProgramCentralUtil.isNotNullString(strATId)){
			DomainObject objActualTransaction = DomainObject.newInstance(context, strATId);
			String strActualTransactionAmount = objActualTransaction.getAttributeValue(context, Financials.ATTRIBUTE_TRANSACTION_AMOUNT);
			double dActualTransactionAmount = 0.0;
			if(ProgramCentralUtil.isNotNullString(strActualTransactionAmount)){
				dActualTransactionAmount = Task.parseToDouble(strActualTransactionAmount);
			}
			String strRelActualTransactionItem = PropertyUtil.getSchemaProperty(context,SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_ITEM);
			String strCostItemId = objActualTransaction.getInfo(context,"from[" + strRelActualTransactionItem + "].to.id");
			String baseCurrency = Currency.getBaseCurrency(context,projectId); 
			if(strOldDate != null && !"".equals(strOldDate)){
				if(strCostItemId != null && !"".equals(strCostItemId)){
					DomainObject objCostItem = DomainObject.newInstance(context,strCostItemId);
					String strCostInterval = objCostItem.getInfo(context, "to["+Financials.RELATIONSHIP_FINANCIAL_ITEMS+"].from.attribute["+Financials.ATTRIBUTE_COST_INTERVAL +"].value" );
					Date oldTransactionDate = eMatrixDateFormat.getJavaDate(strOldDate);
					String relWhere = null;
					boolean isBudgetByPhase = false;
					//On the basis of interval type, get the start date of interval on which old date was lying.
					//The interval start date then be used to identify the Cost Item Interval relationship which has to be 
					//updated with actual cost.
					if(CostItem.QUARTERLY.equals(strCostInterval)){
						oldTransactionDate = Financials.getFiscalQuarterIntervalStartDate(oldTransactionDate);
					}else if(CostItem.MONTHLY.equals(strCostInterval)){
						oldTransactionDate = Financials.getFiscalMonthIntervalStartDate(oldTransactionDate);
					}else if (CostItem.WEEKLY.equals(strCostInterval)){
						oldTransactionDate = Financials.getFiscalWeekIntervalStartDate(oldTransactionDate);
					}else if(CostItem.PROJECT_PHASE.equals(strCostInterval)){
						isBudgetByPhase = true;
						String strRelActualTransactionPhase = PropertyUtil.getSchemaProperty(context,SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_PHASE);
						String strPhaseName = objActualTransaction.getInfo(context, "from["+ strRelActualTransactionPhase +"].to.name");
						relWhere = CostItemIntervalRelationship.SELECT_TO_TYPE + " == \"" + Financials.TYPE_PHASE + "\" && "
								+ CostItemIntervalRelationship.SELECT_TO_NAME + " == \"" + strPhaseName +"\"";
					}else{
						i18nNow loc = new i18nNow();
						String strLanguage=context.getSession().getLanguage();
						final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
						String errMessage = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Budget.CostItem.Error.InvalidCostInterval");
						throw new IllegalDataException(errMessage);
					}
					if(!isBudgetByPhase){
						Calendar intrevalStartCal = Calendar.getInstance();
						intrevalStartCal.setTime(oldTransactionDate);
						SimpleDateFormat sdf = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat());
						String strDate = sdf.format(intrevalStartCal.getTime());
						relWhere = CostItemIntervalRelationship.SELECT_INTERVAL_DATE + " == \"" + strDate+"\"";
					}
					String strRelCostItemInterval = Financials.RELATIONSHIP_COST_ITEM_INTERVAL;
					StringList relSelects = new StringList();
					relSelects.add(DomainRelationship.SELECT_ID);
					relSelects.add(CostItemIntervalRelationship.SELECT_INTERVAL_DATE);
					relSelects.add(CostItemIntervalRelationship.SELECT_ACTUAL_COST);
					String typePattern            = TYPE_INTERVAL_ITEM_DATA +","+ TYPE_PHASE;

					MapList relList = objCostItem.getRelatedObjects(context,
							strRelCostItemInterval, //String relPattern
							typePattern, 		//String typePattern
							null,          	//StringList objectSelects,
							relSelects,            //StringList relationshipSelects,
							false,                  //boolean getTo,
							true,                   //boolean getFrom,
							(short)1,               //short recurseToLevel,
							"",          			//String objectWhere,
							relWhere,               //String relationshipWhere,
							0,						//int limit
							null,                   //Pattern includeType,
							null,                   //Pattern includeRelationship,
							null);

					if(relList != null && relList.size() > 0){
						Map relMap = (Map) relList.get(0);
						String relId = (String) relMap.get(DomainRelationship.SELECT_ID);
						if(relId != null){
							DomainRelationship domRel = DomainRelationship.newInstance(context, relId);
							double dActualCost = Task.parseToDouble((String) domRel.getAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST));
							//Sets the Actual Cost Attribute of Cost Item Interval relationship by subtracting the transaction amount.
							domRel.setAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST, Double.toString(dActualCost - dActualTransactionAmount) + ProgramCentralConstants.SPACE + baseCurrency);
						}
					}
				}
			}
			String[] argList = new String[2];
			argList[0] = strATId;
			argList[1] = Financials.ATTRIBUTE_TRANSACTION_AMOUNT;
			//this method call is to update the Actual cost of Cost Item Interval relationship 
			//created with new modified date.
			transactionAmountModifyPostProcess(context, argList);
		}
		return 0;
	}

	/**
	 * Updates the cost item of existing Actual Transaction object. 
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args holds map with objectId of Type Actual Transaction and the new value for the Cost item.
	 * @throws Exception if operation fails.
	 */
	public void updateActualTransactionCostItem(Context context,String[] args) throws Exception
	{
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap)programMap.get("paramMap");
		Map requestMap = (HashMap)programMap.get("requestMap");
		String projectId = (String)requestMap.get("projectId");
		if(isBackgroundTaskActive(context, projectId)){
			getCurrencyModifyBackgroundTaskMessage(context, true);
			return;
		}		
		String strNewCostItemId = (String)paramMap.get("New Value");
		String strATId = (String)paramMap.get("objectId");
		String strRelActualTransactionItem = PropertyUtil.getSchemaProperty(context,SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_ITEM);
		DomainObject objActualTransaction = DomainObject.newInstance(context,strATId);
		String strRelId = objActualTransaction.getInfo(context,"from[" + strRelActualTransactionItem + "].id");
		String strOldCostItemId = objActualTransaction.getInfo(context,"from[" + strRelActualTransactionItem + "].to.id");

		if(strRelId != null){
			//Replacing with new Cost Item object
			DomainRelationship.setToObject(context, strRelId,DomainObject.newInstance(context,strNewCostItemId));
		}else{
			//Connecting new Cost Item object
			objActualTransaction.addToObject(context, new RelationshipType(strRelActualTransactionItem), strNewCostItemId);
		}
		String[] argList = new String[4];
		argList[0] = strATId;
		argList[1] = strOldCostItemId;
		argList[2] = strNewCostItemId;
		argList[3] = projectId;

		actualTransactionModifyCostItemProcess(context, argList);
	}

	/**
	 * Updates the actual transaction object's Cost Item. 
	 * @param context the ENOVIA <code>Context</code> user.
	 * @param args the String array holding request parameters like object id of modified Actual transaction.
	 * @return 0 if modification succeeds.
	 * @throws Exception if operation fails.
	 */
	public int actualTransactionModifyCostItemProcess(Context context, String[] args )throws Exception{
		if (args == null || args.length < 3) {
			throw (new IllegalArgumentException());
		}
		int cnt = 0;
		String strATId = args[cnt];
		String strOldCostItemId = args[++cnt];
		String strNewCostItemId = args[++cnt];
		String projectId = args[++cnt];

		if(ProgramCentralUtil.isNotNullString(strATId)){
			DomainObject objActualTransaction = DomainObject.newInstance(context, strATId);
			String strAttributeTransactionDate = PropertyUtil.getSchemaProperty(context,SYMBOLIC_ATTRIBUTE_TRANSACTION_DATE);
			String strActualTransDate = objActualTransaction.getAttributeValue(context, strAttributeTransactionDate);
			String strActualTransactionAmount = objActualTransaction.getAttributeValue(context, Financials.ATTRIBUTE_TRANSACTION_AMOUNT);
			double dActualTransactionAmount = 0.0;
			if(ProgramCentralUtil.isNotNullString(strActualTransactionAmount)){
				dActualTransactionAmount = Task.parseToDouble(strActualTransactionAmount);
			}
			if(ProgramCentralUtil.isNotNullString(strOldCostItemId)){
				DomainObject objCostItem = DomainObject.newInstance(context,strOldCostItemId);
				String strSelectCostInterval = "to["+Financials.RELATIONSHIP_FINANCIAL_ITEMS+"].from.attribute["+Financials.ATTRIBUTE_COST_INTERVAL+"].value";
				String strCostInterval = objCostItem.getInfo(context,strSelectCostInterval);
				Date transDate = eMatrixDateFormat.getJavaDate(strActualTransDate);
				String relWhere = null;
				String baseCurrency = Currency.getBaseCurrency(context,projectId);
				boolean isBudgetByPhase = false;
				if(CostItem.QUARTERLY.equals(strCostInterval)){
					transDate = Financials.getFiscalQuarterIntervalStartDate(transDate);
				}else if(CostItem.MONTHLY.equals(strCostInterval)){
					transDate = Financials.getFiscalMonthIntervalStartDate(transDate);
				}else if (CostItem.WEEKLY.equals(strCostInterval)){
					transDate = Financials.getFiscalWeekIntervalStartDate(transDate);
				}else if(CostItem.PROJECT_PHASE.equals(strCostInterval)){
					isBudgetByPhase = true;
					String strRelActualTransactionPhase = PropertyUtil.getSchemaProperty(context,SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_PHASE);
					String strPhaseName = objActualTransaction.getInfo(context, "from["+ strRelActualTransactionPhase +"].to.name");
					relWhere = CostItemIntervalRelationship.SELECT_TO_TYPE + " == \"" + Financials.TYPE_PHASE + "\" && "
							+ CostItemIntervalRelationship.SELECT_TO_NAME + " == \"" + strPhaseName +"\"";
				}else{
					i18nNow loc = new i18nNow();
					String strLanguage=context.getSession().getLanguage();
					final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
					String errMessage = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Budget.CostItem.Error.InvalidCostInterval");
					throw new IllegalDataException(errMessage);
				}
				if(!isBudgetByPhase){
					Calendar intrevalStartCal = Calendar.getInstance();
					intrevalStartCal.setTime(transDate);
					SimpleDateFormat sdf = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat());
					String strDate = sdf.format(intrevalStartCal.getTime());
					relWhere = CostItemIntervalRelationship.SELECT_INTERVAL_DATE + " == \"" + strDate+"\"";
				}

				String strRelCostItemInterval = Financials.RELATIONSHIP_COST_ITEM_INTERVAL;
				StringList relSelects = new StringList();
				relSelects.add(DomainRelationship.SELECT_ID);
				relSelects.add(CostItemIntervalRelationship.SELECT_INTERVAL_DATE);
				relSelects.add(CostItemIntervalRelationship.SELECT_ACTUAL_COST);
	    		String typePattern            = TYPE_INTERVAL_ITEM_DATA +","+ TYPE_PHASE;

				MapList relList = objCostItem.getRelatedObjects(context,
						strRelCostItemInterval, //String relPattern
	    			 	        typePattern, 		//String typePattern
						null,          	//StringList objectSelects,
						relSelects,            //StringList relationshipSelects,
						false,                  //boolean getTo,
						true,                   //boolean getFrom,
						(short)1,               //short recurseToLevel,
						"",          			//String objectWhere,
						relWhere,               //String relationshipWhere,
						0,						//int limit
						null,                   //Pattern includeType,
						null,                   //Pattern includeRelationship,
						null);

				if(relList != null && relList.size() > 0){
					Map relMap = (Map) relList.get(0);
					String relId = (String) relMap.get(DomainRelationship.SELECT_ID);
					if(relId != null){
						DomainRelationship domRel = DomainRelationship.newInstance(context, relId);
						double dActualCost = Task.parseToDouble((String) domRel.getAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST));
						//set the Actual Cost Attribute of Cost Item Interval relationship with subtracting the transaction amount.
						domRel.setAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST, Double.toString(dActualCost - dActualTransactionAmount) + ProgramCentralConstants.SPACE + baseCurrency);
					}
				}
				//set the Actual Cost Attribute of Cost Item object with subtracting the Transaction Amount of disconnected Transactions
				double dActualCost = Task.parseToDouble((String) objCostItem.getAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST));
				objCostItem.setAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST, Double.toString(dActualCost - dActualTransactionAmount) + ProgramCentralConstants.SPACE + baseCurrency);
			}
			String[] argList = new String[2];
			argList[0] = strATId;
			argList[1] = Financials.ATTRIBUTE_TRANSACTION_AMOUNT;
			transactionAmountModifyPostProcess(context, argList);
		}
		return 0;
	}

	/**
	 * updateActualTransactionPhase
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds map with objectId of Type Actual Transaction and the new value for the Phase id.
	 * @throws Exception if the operation fails
	 * @since PMC R210
	 * @author KP2
	 *
	 */
	public void updateActualTransactionPhase(Context context,String[] args) throws Exception
	{
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap)programMap.get("paramMap");
		Map requestMap = (Map)programMap.get("requestMap");
		String strNewPhaseId = (String)paramMap.get("New Value");
		String strATId = (String)paramMap.get("objectId");
		String strProjectId = (String)requestMap.get("projectId");
		if(isBackgroundTaskActive(context, strProjectId)){
			getCurrencyModifyBackgroundTaskMessage(context, true);		
			return;
		}
		String strRelActualTransactionPhase = PropertyUtil.getSchemaProperty(context,SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_PHASE);
		DomainObject objActualTransaction = DomainObject.newInstance(context,strATId);
		String strRelId = objActualTransaction.getInfo(context,"from[" + strRelActualTransactionPhase + "].id");
		String strOldPhaseId = objActualTransaction.getInfo(context,"from[" + strRelActualTransactionPhase + "].to.id");
		if(strRelId != null){
			//Replacing with new Phase object
			DomainRelationship.setToObject(context, strRelId,DomainObject.newInstance(context,strNewPhaseId));
		}else{
			//Connecting new Phase object
			objActualTransaction.addToObject(context, new RelationshipType(strRelActualTransactionPhase), strNewPhaseId);
		}

		String[] argList = new String[4];
		argList[0] = strATId;
		argList[1] = strOldPhaseId;
		argList[2] = strNewPhaseId;
		argList[3] = strProjectId;
		actualTransactionModifyPhaseProcess(context, argList);
	}

	/**
	 * actualTransactionModifyPhaseProcess
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds map with objectId of Type Actual Transaction and old & the new value for the Phase id.
	 * @throws Exception if the operation fails
	 * @since PMC R210
	 * @author KP2
	 *
	 */
	public int actualTransactionModifyPhaseProcess(Context context, String[] args)throws Exception {
		if (args == null || args.length < 3) {
			throw (new IllegalArgumentException());
		}
		int cnt = 0;
		String strATId = args[cnt];
		String strOldPhaseId = args[++cnt];
		String strNewPhaseId = args[++cnt];
		String strProjectId = args[++cnt];
		if(strATId != null && !"".equals(strATId)){
			DomainObject objActualTransaction = DomainObject.newInstance(context, strATId);
			DomainObject objOldPhase = null;
			DomainObject objNewPhase = null;
			String strActualTransactionAmount = objActualTransaction.getAttributeValue(context, Financials.ATTRIBUTE_TRANSACTION_AMOUNT);
			String strRelActualTransactionItem = PropertyUtil.getSchemaProperty(context,SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_ITEM);
			String strCostItemName =  objActualTransaction.getInfo(context,"from[" + strRelActualTransactionItem + "].to.name");
   			String typePattern     = TYPE_INTERVAL_ITEM_DATA +","+ TYPE_PHASE;
			double dActualTransactionAmount = 0.0;

			String strBaseCurrency = Currency.getBaseCurrency(context,strProjectId); 

			if(strActualTransactionAmount != null && !"".equals(strActualTransactionAmount)){
				dActualTransactionAmount = Task.parseToDouble(strActualTransactionAmount);
			}
			if(strOldPhaseId != null && !"".equals(strOldPhaseId)){
				objOldPhase = DomainObject.newInstance(context, strOldPhaseId);

				String strRelCostItemInterval = Financials.RELATIONSHIP_COST_ITEM_INTERVAL;
				StringList relSelects = new StringList();
				relSelects.add(DomainRelationship.SELECT_ID);
				relSelects.add(CostItemIntervalRelationship.SELECT_ACTUAL_COST);
				String relWhere =  CostItemIntervalRelationship.SELECT_FROM_TYPE + " == \"" + Financials.TYPE_COST_ITEM + "\" && "
						+ CostItemIntervalRelationship.SELECT_FROM_NAME + " == \"" + strCostItemName +"\"";;

						MapList relList = objOldPhase.getRelatedObjects(context,
								strRelCostItemInterval, //String relPattern
   								typePattern, 		//String typePattern
								null,          	//StringList objectSelects,
								relSelects,            //StringList relationshipSelects,
								true,                  //boolean getTo,
								false,                   //boolean getFrom,
								(short)1,               //short recurseToLevel,
								"",          			//String objectWhere,
								relWhere,               //String relationshipWhere,
								0,						//int limit
								null,                   //Pattern includeType,
								null,                   //Pattern includeRelationship,
								null);
						if(relList != null && relList.size() > 0){
							Map relMap = (Map) relList.get(0);
							String relId = (String) relMap.get(DomainRelationship.SELECT_ID);
							if(relId != null){
								DomainRelationship domRel = DomainRelationship.newInstance(context, relId);
								double dActualCost = Task.parseToDouble((String) domRel.getAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST));
								//set the Actual Cost Attribute of Cost Item Interval relationship with subtracting the transaction amount.
								domRel.setAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST, Double.toString(dActualCost - dActualTransactionAmount) + 
										ProgramCentralConstants.SPACE + strBaseCurrency);
							}
						}
			}

			if(strNewPhaseId != null && !"".equals(strNewPhaseId)){
				objNewPhase = DomainObject.newInstance(context, strNewPhaseId);

				String strRelCostItemInterval = Financials.RELATIONSHIP_COST_ITEM_INTERVAL;
				StringList relSelects = new StringList();
				relSelects.add(DomainRelationship.SELECT_ID);
				relSelects.add(CostItemIntervalRelationship.SELECT_ACTUAL_COST);
				String relWhere =  CostItemIntervalRelationship.SELECT_FROM_TYPE + " == \"" + Financials.TYPE_COST_ITEM + "\" && "
						+ CostItemIntervalRelationship.SELECT_FROM_NAME + " == \"" + strCostItemName +"\"";;
						MapList relList = objNewPhase.getRelatedObjects(context,
								strRelCostItemInterval, //String relPattern
   								typePattern, 		//String typePattern
								null,          	//StringList objectSelects,
								relSelects,            //StringList relationshipSelects,
								true,                  //boolean getTo,
								false,                   //boolean getFrom,
								(short)1,               //short recurseToLevel,
								"",          			//String objectWhere,
								relWhere,               //String relationshipWhere,
								0,						//int limit
								null,                   //Pattern includeType,
								null,                   //Pattern includeRelationship,
								null);
						if(relList != null && relList.size() > 0){
							Map relMap = (Map) relList.get(0);
							String relId = (String) relMap.get(DomainRelationship.SELECT_ID);
							if(relId != null){
								DomainRelationship domRel = DomainRelationship.newInstance(context, relId);
								double dActualCost = Task.parseToDouble((String) domRel.getAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST));
								//set the Actual Cost Attribute of Cost Item Interval relationship with adding the transaction amount.
								domRel.setAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST, Double.toString(dActualCost + dActualTransactionAmount) +
										ProgramCentralConstants.SPACE + strBaseCurrency);
							}
						}
			}
		}
		return 0;
	}

	/**
	 * getBudgetCostItemsRange
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object Id of Budget object.
	 * @throws Exception if the operation fails
	 * @return MAP containing values for combo-box
	 * @since PMC R210
	 * @author KP2
	 *
	 */
	public Object getBudgetCostItemsRange(Context context,String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String objectId = (String) paramMap.get("objectId");
		//Added:18-Feb-2011:hp5:R210:PRG
		String sLanguage = context.getSession().getLanguage();
		//End:18-Feb-2011:hp5:R210:PRG
		HashMap tempMap = new HashMap();
		if(objectId != null && !"".equals(objectId)){
			StringList selectList = new StringList(2);
			selectList.add(DomainObject.SELECT_ID);
			selectList.add(DomainObject.SELECT_NAME);
			String strRelFinancialItem = PropertyUtil.getSchemaProperty(context, SYMBOLIC_RELATIONSHIP_FINANCIAL_ITEMS);
			String strTypeCostItem = PropertyUtil.getSchemaProperty(context, SYMBOLIC_TYPE_COST_ITEM);
			DomainObject budgetObj = DomainObject.newInstance(context,objectId);

			MapList costItemList = budgetObj.getRelatedObjects(context,
					strRelFinancialItem,  	//String relPattern
					strTypeCostItem, 		//String typePattern
					selectList,          	//StringList objectSelects,
					null,                   //StringList relationshipSelects,
					false,                  //boolean getTo,
					true,                   //boolean getFrom,
					(short)1,               //short recurseToLevel,
					"",          			//String objectWhere,
					"",                     //String relationshipWhere,
					0,						//int limit
					null,                   //Pattern includeType,
					null,                   //Pattern includeRelationship,
					null);

			//Modified:18-Feb-2011:hp5:R210:PRG
			StringList fieldRangeValues = new StringList();
			StringList convertedfieldDisplayRangeValues = new StringList();
			String key = "emxProgramCentral.Common.";
			String convertedfieldDisplayRangeValue = "";

			for (Iterator itr=costItemList.iterator();itr.hasNext();) 
			{
				Map objectMap = (Map) itr.next();
				fieldRangeValues.addElement(objectMap.get(DomainObject.SELECT_ID));
				String fieldValue = (String) objectMap.get(DomainObject.SELECT_NAME);
				if(fieldValue.contains(" "))
				{
					fieldValue = fieldValue.replace(" ", "_");
				}
				convertedfieldDisplayRangeValue = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						key+fieldValue, sLanguage);
				convertedfieldDisplayRangeValues.add(convertedfieldDisplayRangeValue);
			}
			tempMap.put("field_choices", fieldRangeValues);
			tempMap.put("field_display_choices", convertedfieldDisplayRangeValues);
			//End:18-Feb-2011:hp5:R210:PRG
		}
		return tempMap;
	}

	/**
	 * getActualTransactionPhaseRange
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds object Id of Budget object.
	 * @throws Exception if the operation fails
	 * @return MAP containing values for combo-box
	 * @since PMC R210
	 * @author KP2
	 *
	 */
	public Object getActualTransactionPhaseRange(Context context,String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String objectId = (String) paramMap.get("objectId");
		HashMap tempMap = new HashMap();
		if(objectId != null && !"".equals(objectId)){
			StringList selectList = new StringList(2);
			selectList.add(DomainObject.SELECT_ID);
			selectList.add(DomainObject.SELECT_NAME);
			String strRelSubTask = PropertyUtil.getSchemaProperty(context, SYMBOLIC_RELATIONSHIP_SUBTASK);
			String strTypePhase = PropertyUtil.getSchemaProperty(context, SYMBOLIC_TYPE_PHASE);
			String strRelProjectFinancialItem = PropertyUtil.getSchemaProperty(context, SYMBOLIC_RELATIONSHIP_PROJECT_FINANCIAL_ITEM);
			DomainObject budgetObj = DomainObject.newInstance(context,objectId);
			String strProjectId = budgetObj.getInfo(context, "to[" + strRelProjectFinancialItem + "].from.id");
			DomainObject projectObject = DomainObject.newInstance(context,strProjectId);
			MapList costItemList = projectObject.getRelatedObjects(context,
					strRelSubTask,		//String relPattern
					strTypePhase, 		//String typePattern
					selectList,         //StringList objectSelects,
					null,               //StringList relationshipSelects,
					false,              //boolean getTo,
					true,               //boolean getFrom,
					(short)1,           //short recurseToLevel,
					"",          		//String objectWhere,
					"",                 //String relationshipWhere,
					0,					//int limit
					null,               //Pattern includeType,
					null,               //Pattern includeRelationship,
					null);

			StringList fieldRangeValues = new StringList();
			StringList fieldDisplayRangeValues = new StringList();

			for (Iterator itr=costItemList.iterator();itr.hasNext();) {
				Map objectMap = (Map) itr.next();
				fieldRangeValues.addElement(objectMap.get(DomainObject.SELECT_ID));
				fieldDisplayRangeValues.addElement(objectMap.get(DomainObject.SELECT_NAME));
			}
			tempMap.put("field_choices", fieldRangeValues);
			tempMap.put("field_display_choices", fieldDisplayRangeValues);
		}
		return tempMap;
	}



	/**
	 * Generates required Cost Interval columns dynamically
	 *
	 * @param context The matrix context object
	 * @param args The arguments, it contains requestMap
	 * @return The MapList object containing definitions about new columns for showing Benefit Intervals
	 * @throws Exception if operation fails
	 */
	public MapList getDynamicCostIntervalColumn (Context context, String[] args) throws Exception
	{
		final String strDateFormat=eMatrixDateFormat.getEMatrixDateFormat();
		Map mapProgram = (Map) JPO.unpackArgs(args);
		Map requestMap = (Map)mapProgram.get("requestMap");

		// Following code gets business objects information
		String strObjectId = (String) requestMap.get("objectId");
		String selectedOption = (String) requestMap.get("selectedOption");
		selectedOption = ProgramCentralUtil.isNullString(selectedOption)?"option2":selectedOption;


		// check if budget is present.
		DomainObject dmoProject = DomainObject.newInstance(context, strObjectId);
		String SELECT_PROJECT_COST_INTERVAL = "attribute[" + ATTRIBUTE_COST_INTERVAL  + "]";
		StringList busSelect1 = new StringList(2);
		busSelect1.add(SELECT_PROJECT_COST_INTERVAL);
		busSelect1.add(SELECT_CURRENT);
		StringList relSelect1 = new StringList();
		MapList mlBudget = dmoProject.getRelatedObjects(
				context,
				RELATIONSHIP_PROJECT_FINANCIAL_ITEM,
				TYPE_BUDGET,
				busSelect1,
				relSelect1,
				false,
				true,
				(short)1,
				"",
				DomainConstants.EMPTY_STRING);

		if(mlBudget == null || mlBudget.size()<1)
			return mlBudget;

		String strLanguage=context.getSession().getLanguage();
		String strFY= EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Financials.FiscalYear", strLanguage);
		String strM= EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Financials.Month", strLanguage);
		String strQ= EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Financials.Quarter", strLanguage);
		String strWK= EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Financials.Week", strLanguage);
		String strTotal= EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Financials.Total", strLanguage);
		Date dtFiscalStartDate = null;
		Date dtFiscalEndDate = null;
		Map fiscalYearMap = null;
		Map mapColumn = null;
		Map mapSettings = null;
		Date dtStartDate = null;
		Date dtFinishDate = null;

		String tableName = (String)requestMap.get("selectedTable");
		String displayView = (String)requestMap.get(BUDGETDISPLAYVIEWFILTER);
		if("PMCProjectBudgetReportTable".equals(tableName)){
			displayView = (String)requestMap.get("PMCProjectBudgetReportDisplayViewFilter");
		}
		String strBudgetCostInterval = "";
		String strTimeLineInterval = "";
		String strMonth = "";
		String strBudgetState = "";
		String  selecteView= "";
		String strInterval = "";
		if("PMCProjectBudgetReportTable".equals(tableName)){
			selecteView = actualView;
			strInterval =  (String) requestMap.get("PMCProjectBudgetReportIntervalFilter");
		}else {
			selecteView = (String)requestMap.get(BUDGETVIEWFILTER);
			strInterval = (String)requestMap.get(BUDGETINTERVALFILTER);
		}
		MapList mlColumns = new MapList();
		int interval = 0;		
		int index = 0;
		//This is default year interval 
		if(ProgramCentralUtil.isNullString(strInterval)){
			Map intervalMap = getBenefitOrBudgetYearRange(context, args);	// to get the list of year intervals. 
			StringList slIntervals = (StringList)intervalMap.get("field_choices");
			if(slIntervals.size() != 0){
				strInterval = (String)slIntervals.get(0);	// The very first interval in the list will be taken as default interval.
				index = strInterval.lastIndexOf(" Year");
				strInterval = strInterval.substring(0,index);	
				interval = Integer.parseInt(strInterval);
			}
		}
		//User's choice interval
		else{
			index = strInterval.lastIndexOf(" Year");
			strInterval = strInterval.substring(0,index);
			interval = Integer.parseInt(strInterval);
		}
		for (Iterator itrTableRows = mlBudget.iterator(); itrTableRows.hasNext();){
			Map mapDataColumn = new HashMap();
			mapDataColumn= (Map) itrTableRows.next();
			strTimeLineInterval = (String)mapDataColumn.get(SELECT_PROJECT_COST_INTERVAL);
			strBudgetCostInterval= (String)mapDataColumn.get(SELECT_PROJECT_COST_INTERVAL);
			strBudgetState = (String)mapDataColumn.get(SELECT_CURRENT);
		}

		Map mapDate = getMinMaxCostIntervalDates(context,strObjectId);
		String selecteReportView = (String)requestMap.get(BUDGETTIMELINEREPORTFILTER);
		if(null != selecteReportView){
			strTimeLineInterval = selecteReportView;
		}else{
			//Set the interval to weekly only when everything is null.
			if(null==strTimeLineInterval){
				strTimeLineInterval = WEEKLY;	
			}
		}

		if(null != mapDate){
			dtStartDate = (Date)mapDate.get("TimeLineIntervalFrom");
			dtFinishDate = (Date)mapDate.get("TimeLineIntervalTo");
		}
		if("FiscalYear".equals(displayView)){
			String strSelectedYear = "";
			strSelectedYear = (String)requestMap.get(BUDGETFISCALYEARFILTER);

			if(null == strSelectedYear || "null".equals(strSelectedYear) || "".equals(strSelectedYear)){
				strSelectedYear = (String)requestMap.get(BUDGETREPORTFISCALYEARFILTER);
			}
			//Added:PRG:MS9:R212:26-Aug-2011:IR-088614V6R2012x
			if(null != strSelectedYear && !"".equals(strSelectedYear) && !"null".equals(strSelectedYear)){
				int year = Integer.parseInt(strSelectedYear);
				fiscalYearMap = Financials.getFiscalInterval(interval, year);
				dtFiscalStartDate = (Date)fiscalYearMap.get("fStartDate");
				dtFiscalEndDate = (Date)fiscalYearMap.get("fEndDate");

			}else{
				Calendar calendar1 = Calendar.getInstance();
				if(null!= dtStartDate){
					calendar1.setTime(dtStartDate);
					int nYear = calendar1.get(Calendar.YEAR);
					fiscalYearMap = Financials.getFiscalInterval(interval, nYear);
					dtFiscalStartDate = (Date)fiscalYearMap.get("fStartDate");
					dtFiscalEndDate = (Date)fiscalYearMap.get("fEndDate");
				}
			}
		}else{
			dtFiscalStartDate = (Date)mapDate.get("TimeLineIntervalFrom");
			dtFiscalEndDate = (Date)mapDate.get("TimeLineIntervalTo");
		}

		try{
			int nTimeframe  = 0 ;
			int nYear = 0;
			//Calculation of Fiscal Start Month  and year to display in the groupheader
			int intFiscalYearStartYear = 0;
			int intFiscalYearEndYear = 0;
			String strFiscalStartMonth = "";
			if(null != dtFiscalStartDate){
				Calendar calFiscStartYear =  Calendar.getInstance();
				calFiscStartYear.setTime(dtFiscalStartDate);
				Calendar calFiscEndYear =  Calendar.getInstance();
				calFiscEndYear.setTime(dtFiscalEndDate);
				int intFiscalYearStartMonth = calFiscStartYear.get(Calendar.MONTH)+1;
				strFiscalStartMonth =  getMonthName(context,intFiscalYearStartMonth);
				intFiscalYearStartYear = calFiscStartYear.get(Calendar.YEAR);
				intFiscalYearEndYear = calFiscEndYear.get(Calendar.YEAR);
			}
			mapColumn = new HashMap();
			if(!PROJECT_PHASE.equals(strBudgetCostInterval)){
				if(null != dtStartDate && null != dtFinishDate){

					Calendar calendar = Calendar.getInstance();
					calendar.setTime(dtStartDate);

					ArrayList dateList =  getIntervalDateList(dtStartDate,dtFinishDate,strTimeLineInterval);

					if(null != dtFiscalStartDate && null != dtFiscalEndDate){
						dateList =  getIntervalDateList(dtFiscalStartDate,dtFiscalEndDate,strTimeLineInterval);
					}
					String strIntervalDate = null;
					java.util.Set<Integer> intervals = new HashSet<Integer>();
					for (Iterator itrTableRows = dateList.iterator(); itrTableRows.hasNext();)
					{
						Date dtIntervalDate=(Date)itrTableRows.next();
						Calendar intervalCalendar=Calendar.getInstance();
						intervalCalendar.setTime(dtIntervalDate);
						int day = intervalCalendar.get(Calendar.DAY_OF_MONTH);
						int month=intervalCalendar.get(Calendar.MONTH)+1;
						int nFiscalDisplayYear = Financials.getFiscalYear(dtIntervalDate);
						intervals.add(nFiscalDisplayYear);
						nYear = intervalCalendar.get(Calendar.YEAR);
						if(intervals.size() > interval)
							break;
						mapColumn = new HashMap();
						nTimeframe = Financials.getFiscalMonthNumber(dtIntervalDate);
						strMonth = getMonthName(context,(dtIntervalDate.getMonth()+1));
						String strLabelDate = ""+dtIntervalDate.getDate()+"-"+strMonth+"-"+(dtIntervalDate.getYear()+1900);

						if(strTimeLineInterval.equals(CostItem.MONTHLY)){
							mapColumn.put("label", strM+"-"+nTimeframe +" ("+strLabelDate+")");
						}
						else if ((CostItem.WEEKLY).equals(strTimeLineInterval)){
							nTimeframe = Financials.getFiscalWeekNumber(dtIntervalDate);
							mapColumn.put("label", strWK+"-"+nTimeframe +" ("+strLabelDate+")");
						}
						else if ((CostItem.QUARTERLY).equals(strTimeLineInterval)){
							nTimeframe = Financials.getFiscalQuarterNumber(dtIntervalDate);
							mapColumn.put("label", strQ+"-"+nTimeframe +" ("+strLabelDate+")");
						}
						else{
							nTimeframe= month;
							mapColumn.put("label", strFY+"-"+nFiscalDisplayYear);
						}
						String columnName = ProgramCentralConstants.EMPTY_STRING;
						String dateFormat = strDateFormat.toLowerCase();
						if(dateFormat.contains("yyyy/mm/dd"))
						{
							columnName = nYear + "/" + month + "/" + day;
						}
						else if(dateFormat.contains("dd/mm/yyyy"))
						{
							columnName = day + "/" + month + "/" + nYear;
						}
						else
						{
							columnName = month + "/" + day + "/" + nYear;
						}
						mapColumn.put("name", columnName);
						mapSettings = new HashMap();
						mapSettings.put("Registered Suite","ProgramCentral");
						mapSettings.put("program","emxProjectBudget");
						mapSettings.put("function","getColumnBudgetData");
						mapSettings.put("Column Type","program");
						mapSettings.put("Editable","true");
						mapSettings.put("Export","true");
						mapSettings.put("Field Type","attribute");
						mapSettings.put("Sortable","false");
						if(!"PMCProjectBudgetReportTable".equals(tableName)){
							mapSettings.put("Update Program","emxProjectBudget");
							mapSettings.put("Update Function","updateDynamicColumnData");
							mapSettings.put("Edit Access Program","emxProjectBudget");
							mapSettings.put("Edit Access Function","isColumnEditable");
						}
						mapSettings.put("Style Program","emxFinancialItem");
						mapSettings.put("Style Function","getBudgetRightAllignedStyleInfo");
						if("FiscalYear".equals(displayView) || strTimeLineInterval.equals(CostItem.MONTHLY))
						{
							mapSettings.put("Group Header",strFY+" "+nFiscalDisplayYear+"");
						}else{
							mapSettings.put("Group Header",strFY+" "+nFiscalDisplayYear+"");
						}						
						mapSettings.put("Validate","validateCost");
						mapColumn.put("settings", mapSettings);
						mlColumns.add(mapColumn);
					}//Added:PRG:MS9:R212:26-Aug-2011:IR-088614V6R2012x end	
					mapColumn = new HashMap();
					mapColumn.put("name", "Total");
					String strTotalLabel= EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.Budget.Total", strLanguage);
					mapColumn.put("label",strTotalLabel );
					mapSettings = new HashMap();

					mapSettings = new HashMap();
					mapSettings.put("Registered Suite","ProgramCentral");
					mapSettings.put("program","emxProjectBudget");
					mapSettings.put("function","getColumnBudgetData");
					mapSettings.put("Column Type","program");
					mapSettings.put("Editable","false");
					mapSettings.put("Export","true");
					mapSettings.put("Field Type","string");
					mapSettings.put("Sortable","false");
					mapSettings.put("Style Program","emxFinancialItem");
					mapSettings.put("Style Function","getBudgetRightAllignedStyleInfo");
					mapColumn.put("settings", mapSettings);
					mlColumns.add(mapColumn);
				}
			}else{
				StringList busSelect = new StringList();
				StringList relSelect = new StringList();
				String whereClause = "";
				String SELECT_PROJECT_PHASE_ID =SELECT_ID;
				String SELECT_PROJECT_PHASE_NAME =SELECT_NAME;
				String SELECT_PROJECT_PHASE_START_DATE = SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE;
				String SELECT_PROJECT_PHASE_END_DATE = SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE;
				String attrTaskWBSId =  "to["+DomainConstants.RELATIONSHIP_SUBTASK+"].attribute["+DomainConstants.ATTRIBUTE_TASK_WBS+"]";
				busSelect.add(SELECT_PROJECT_PHASE_ID);
				busSelect.add(SELECT_PROJECT_PHASE_NAME);
				busSelect.add(SELECT_PROJECT_PHASE_START_DATE);
				busSelect.add(SELECT_PROJECT_PHASE_END_DATE);
				busSelect.addElement(attrTaskWBSId);
				MapList mlProjectPhase = new MapList();
				mlProjectPhase = getProjectPhases(context,strObjectId,busSelect,relSelect,whereClause);
				if("option2".equals(selectedOption)){
					mlProjectPhase.sortStructure(context, attrTaskWBSId, "ascending", "emxWBSColumnComparator");
				}else{
					mlProjectPhase.sortStructure(context, attrTaskWBSId, "ascending", "emxWBSIDComparator");
				}
				String PhaseStartDate ="";
				String PhaseFinishDate ="";
				Map mapDataColumn1 = new HashMap();
				for (Iterator itrTableRows = mlProjectPhase.iterator(); itrTableRows.hasNext();)
				{
					mapDataColumn1= (Map) itrTableRows.next();
					PhaseStartDate = (String)mapDataColumn1.get(SELECT_PROJECT_PHASE_START_DATE);
					PhaseFinishDate = (String)mapDataColumn1.get(SELECT_PROJECT_PHASE_END_DATE);
				}
				//if(null == selecteView && tableName.equals("PMCProjectBudgetReportTable")){
				if("PMCProjectBudgetReportTable".equals(tableName)){
					mlProjectPhase.clear();
					ArrayList dateList = new ArrayList();
					if(ProgramCentralUtil.isNotNullString(strTimeLineInterval) && strTimeLineInterval.contains("Phase")){
						strTimeLineInterval = WEEKLY;
					}
					dateList =  getIntervalDateList(dtStartDate,dtFinishDate,strTimeLineInterval);
					for(int i=0;i<dateList.size();i++){
						mlProjectPhase.add(dateList.get(i));
					}
				}
				String strIntervalDate = null;
				Map mapDataColumn = new HashMap();
				int nFiscalDisplayYear = 0;
				for (Iterator itrTableRows = mlProjectPhase.iterator(); itrTableRows.hasNext();)
				{    				
					if( "PMCProjectBudgetReportTable".equals(tableName)){
						Date dtIntervalDate=(Date)itrTableRows.next();
						Calendar intervalCalendar=Calendar.getInstance();
						intervalCalendar.setTime(dtIntervalDate);

						SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
						strIntervalDate = sdf.format(dtIntervalDate);
						dtIntervalDate = sdf.parse (strIntervalDate);
						int date = intervalCalendar.get(Calendar.DATE);
						nYear=intervalCalendar.get(Calendar.YEAR);
						int month = intervalCalendar.get(Calendar.MONTH);
						month++;
						strMonth = getMonthName(context, month);
						nFiscalDisplayYear = Financials.getFiscalYear(dtIntervalDate);
						String strLabelDate = "" + date + "-" + strMonth + "-" + nYear;
						mapColumn = new HashMap();
						if(strTimeLineInterval.equals(CostItem.MONTHLY)){
							mapColumn.put("label", strM + "-" + month + " (" + strLabelDate + ")");
						}
						else if ((CostItem.WEEKLY).equals(strTimeLineInterval)){
							nTimeframe = Financials.getFiscalWeekNumber(dtIntervalDate);
							mapColumn.put("label", strWK + nTimeframe + " (" + strLabelDate + ")");
						}
						else if ((CostItem.QUARTERLY).equals(strTimeLineInterval)){
							nTimeframe = Financials.getFiscalQuarterNumber(dtIntervalDate);
							mapColumn.put("label", strQ + nTimeframe + " (" + strLabelDate + ")");
						}else if ((CostItem.ANNUALLY).equals(strTimeLineInterval)){
							nTimeframe = Financials.getFiscalYear(dtIntervalDate);
							mapColumn.put("label", strFY + nTimeframe + " (" + strLabelDate + ")");
						}else{
							nTimeframe= intervalCalendar.get(Calendar.WEEK_OF_YEAR);
							mapColumn.put("label", strWK + nTimeframe + " (" + strLabelDate + ")");
						}
						mapColumn.put("name", month + "/" + date + "/" + nYear);
					}
					else{
						mapDataColumn= (Map) itrTableRows.next();
						String PhaseName = (String)mapDataColumn.get(SELECT_NAME);
						String PhaseId = (String)mapDataColumn.get(SELECT_ID);
						PhaseStartDate = (String)mapDataColumn.get(SELECT_PROJECT_PHASE_START_DATE);
						PhaseFinishDate = (String)mapDataColumn.get(SELECT_PROJECT_PHASE_END_DATE);
						mapColumn = new HashMap();
						mapColumn.put("isGrid","true");
						mapColumn.put("label",PhaseName);
						mapColumn.put("name", PhaseId);

					}
					Locale locale = (Locale)requestMap.get("localeObj");
					String strTimeZone = (String)requestMap.get("timeZone");
					double clientTZOffset = Task.parseToDouble(strTimeZone);
					String strPhaseStartDate = eMatrixDateFormat.getFormattedDisplayDate(PhaseStartDate, clientTZOffset, locale);
					String strPhaseFinishDate = eMatrixDateFormat.getFormattedDisplayDate(PhaseFinishDate, clientTZOffset, locale);
					mapSettings = new HashMap();
					mapSettings.put("Registered Suite","ProgramCentral");
					mapSettings.put("program","emxProjectBudget");
					mapSettings.put("function","getColumnBudgetData");
					mapSettings.put("Column Type","program");
					mapSettings.put("Editable","true");
					mapSettings.put("Export","true");
					mapSettings.put("Field Type","attribute");
					mapSettings.put("Sortable","false");
					if("PMCProjectBudgetReportTable".equals(tableName)){
						mapSettings.put("Group Header",strFY+" "+nFiscalDisplayYear+"");
					}else{
						mapSettings.put("Edit Access Function","getEditAccessToBudgetRows");
						mapSettings.put("Edit Access Program","emxProjectBudget"); 
						mapSettings.put("Update Program","emxProjectBudget");
						mapSettings.put("Update Function","updateDynamicColumnPhaseData");
						mapSettings.put("Group Header",strPhaseStartDate+" - "+strPhaseFinishDate);
					}
					mapSettings.put("Style Program","emxFinancialItem");
					mapSettings.put("Style Function","getBudgetRightAllignedStyleInfo");
					mapSettings.put("Validate","validateCost");
					mapColumn.put("settings", mapSettings);
					mlColumns.add(mapColumn);
				}
				mapColumn = new HashMap();
				mapColumn.put("name", "Total");
				mapColumn.put("label", strTotal);
				//Added:PRG:MS9:R212:26-Aug-2011:IR-088614V6R2012x end
				mapSettings = new HashMap();

				mapSettings = new HashMap();
				mapSettings.put("Registered Suite","ProgramCentral");
				mapSettings.put("program","emxProjectBudget");
				mapSettings.put("function","getColumnBudgetData");
				mapSettings.put("Column Type","program");
				mapSettings.put("Editable","false");
				mapSettings.put("Export","true");
				mapSettings.put("Field Type","string");
				mapSettings.put("Sortable","false");
				mapSettings.put("Style Program","emxFinancialItem");
				mapSettings.put("Style Function","getBudgetRightAllignedStyleInfo");
				mapColumn.put("settings", mapSettings);
				mlColumns.add(mapColumn);

			}

			if((strBudgetState.equals(STATE_BUDGET_PLAN_FROZEN) && !planView.equalsIgnoreCase(selecteView)) && !"PMCProjectBudgetReportTable".equals(tableName))
			{
				mapColumn=new HashMap();
				String planORactual="";
				if(actualView.equalsIgnoreCase(selecteView))
					planORactual="emxProgramCentral.Financial.Plan";
				else
					planORactual="emxProgramCentral.Financial.Actual";

				mapColumn.put("name","PlanOrActual");
				mapColumn.put("label",planORactual);
				mapColumn.put("settings", addEstimateTableColumns(context,planORactual));
				mlColumns.add(mapColumn);

				mapColumn=new HashMap();
				mapColumn.put("name","VarianceAmount");
				mapColumn.put("label","emxProgramCentral.Financial.VarianceAmount");
				mapColumn.put("settings", addEstimateTableColumns(context,"Amount"));
				mlColumns.add(mapColumn);
				mapColumn=new HashMap();
				mapColumn.put("name","VariancePercent");
				mapColumn.put("label","emxProgramCentral.Financial.VariancePercent");
				mapColumn.put("settings", addEstimateTableColumns(context,"%"));
				mlColumns.add(mapColumn);
			}
		}catch(Exception exp){
			exp.printStackTrace();
			throw exp;
		}
		return mlColumns;
	}

	/*
	 * This method returns the Edit Access List for rows in Budget Table
	 * @param context Context Object
	 * @param args String[] containing row Map List
	 * 
	 * @throws MatrixException
	 * @returns StringList of row access flags
	 * Added:nr2:PRG:R212:11 May 2011:IR-106422V6R2012x
	 */
	public StringList getEditAccessToBudgetRows(Context context,String[] args) throws MatrixException{
		try{
			StringList slEditAccess = new StringList();
			Map programMap =   (Map)JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");

			for(Iterator itr=objectList.iterator();itr.hasNext();){
				Map m = (Map)itr.next();
				String objId = (String) m.get(SELECT_ID);

				if(ProgramCentralUtil.isNullString(objId))
					throw new MatrixException();

				if(!DomainObject.newInstance(context,objId).isKindOf(context,TYPE_PROJECT_SPACE))
					slEditAccess.add(true);
				else
					slEditAccess.add(false);
			}
			return slEditAccess;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}
	//Added:nr2:PRG:R212:11 May 2011:IR-106422V6R2012x

	/*
	 * This method returns the Edit Access List for Template Enforcement column in PMCProjectTemplateBudgetTable
	 * @param context Context Object
	 * @param args String[] containing row Map List
	 * 
	 * @throws MatrixException
	 * @returns StringList of row access flags
	 * Added:nr2:PRG:R212:16 May 2011:IR-073697V6R2012x 	
	 */
	public StringList getTemplateEnforcementEditAccess(Context context, String[] args) throws MatrixException{
		try{
			StringList templateEnfAccessList = new StringList();
			String templateEnfStr = EnoviaResourceBundle.getProperty(context,"emxFramework.Budget.TemplateEnforcement") ;

			//Added:NZF:22-Jun-2011:IR-111812V6R2012xWIM
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			int i =0;
			Map mapObjectInfo = null;
			for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
			{
				mapObjectInfo = (Map) itrTableRows.next();
				if(i==0 && (templateEnfStr==null || "".equals(templateEnfStr) ||"false".equalsIgnoreCase(templateEnfStr)))
					templateEnfAccessList.add(true);
				else
					templateEnfAccessList.add(false);
				i++;
			}
			//End:NZF:22-Jun-2011:IR-111812V6R2012xWIM
			return templateEnfAccessList;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}
	//End:nr2:PRG:R212:16 May 2011:IR-073697V6R2012x 	
	public static StringList isColumnEditable(Context context, String[] args) throws Exception{
		try{
			StringList slResult= new StringList();
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map columnMap = (Map) programMap.get("columnMap");
			Map paramList = (Map) programMap.get("paramList");

			String strColumnName = (String) columnMap.get(SELECT_NAME);

			Map mapObjectInfo = null;

			//Modified:NZF:18-Jul-2011:IR-074121V6R2012x
			Map requestMap = (Map) programMap.get("requestMap");
			String strProjecttId = (String) requestMap.get("parentOID");
			DomainObject dmoProjectSpace = DomainObject.newInstance(context, strProjecttId);

			//Find budget Id from a Project to prevent unnecessary null pointer exceptions.

			String strBudgetId = "";
			StringList busSelect1 = new StringList();
			StringList relSelect = new StringList();

			String strRelationshipType = ProgramCentralConstants.RELATIONSHIP_PROJECT_FINANCIAL_ITEM;
			String projFinancialItemRelId  = "to["+ProgramCentralConstants.RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"].id";

			busSelect1.add(ProgramCentralConstants.SELECT_ID);
			busSelect1.add(projFinancialItemRelId);

			MapList mlBudget = null;
			Map mapBudget = null;

			String strTypePattern = ProgramCentralConstants.TYPE_BUDGET;
			mlBudget = dmoProjectSpace.getRelatedObjects(
					context,
					strRelationshipType,
					strTypePattern,
					busSelect1,
					relSelect,
					false,
					true,
					(short)1,
					null,
					DomainConstants.EMPTY_STRING,
					0);

			for (Iterator iterBudget = mlBudget.iterator(); iterBudget .hasNext();)
			{
				mapBudget = (Map) iterBudget.next();
				strBudgetId = (String)mapBudget.get(ProgramCentralConstants.SELECT_ID);
			}


			DomainObject dmoBudget = DomainObject.newInstance(context, strBudgetId);
			String strBudgetStartDate = (String)dmoBudget.getInfo(context,SELECT_COST_INTERVAL_START_DATE);
			String strBudgetEndDate = (String)dmoBudget.getInfo(context,SELECT_COST_INTERVAL_END_DATE);
			String strBudgetInterval = (String)dmoBudget.getInfo(context,SELECT_COST_INTERVAL);
			//Added:PRG:MS9:R212:26-Aug-2011:IR-088614V6R2012x			
			Date dtStartDate = null;
			if(ProgramCentralUtil.isNotNullString(strBudgetStartDate))
			{
				dtStartDate =  eMatrixDateFormat.getJavaDate(strBudgetStartDate);
				dtStartDate.setHours(0);
				dtStartDate.setMinutes(0);
				dtStartDate.setSeconds(0);
			}

			if("Monthly".equalsIgnoreCase(strBudgetInterval)){
				dtStartDate = Financials.getFiscalMonthIntervalStartDate(dtStartDate);
			}else if("Quarterly".equalsIgnoreCase(strBudgetInterval)){
				dtStartDate = Financials.getFiscalQuarterIntervalStartDate(dtStartDate);
			}else if("Weekly".equalsIgnoreCase(strBudgetInterval)){
				dtStartDate = Financials.getFiscalWeekIntervalStartDate(dtStartDate);
			}

			Date dtEndDate = null;
			if(ProgramCentralUtil.isNotNullString(strBudgetEndDate))
			{
				dtEndDate =  eMatrixDateFormat.getJavaDate(strBudgetEndDate);
				dtEndDate.setHours(0);
				dtEndDate.setMinutes(0);
				dtEndDate.setSeconds(0);
			}

			int i = 0;
			for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
			{
				//Make Root non editable..
				if(i == 0){
					slResult.add(Boolean.FALSE);
					i++;
				}else{
					mapObjectInfo = (Map) itrTableRows.next();

					Date dtColumnDate = eMatrixDateFormat.getJavaDate(strColumnName);

					//Use Calendar Objects instead of Date.
					//Budget Start Date
					Calendar calBudgetStartDate = Calendar.getInstance();
					calBudgetStartDate.set(dtStartDate.getYear(),dtStartDate.getMonth(),dtStartDate.getDate());
					calBudgetStartDate.set(Calendar.HOUR_OF_DAY,0);
					calBudgetStartDate.set(Calendar.MINUTE,0);
					calBudgetStartDate.set(Calendar.SECOND,0);
					calBudgetStartDate.set(Calendar.MILLISECOND,0);

					//Budget End Date
					Calendar calBudgetEndDate = Calendar.getInstance();
					calBudgetEndDate.set(dtEndDate.getYear(),dtEndDate.getMonth(),dtEndDate.getDate());
					calBudgetEndDate.set(Calendar.HOUR_OF_DAY,0);
					calBudgetEndDate.set(Calendar.MINUTE,0);
					calBudgetEndDate.set(Calendar.SECOND,0);
					calBudgetEndDate.set(Calendar.MILLISECOND,0);

					//Budget Cost Interval Date
					Calendar calBudgetIntervalDate = Calendar.getInstance();
					calBudgetIntervalDate.set(dtColumnDate.getYear(),dtColumnDate.getMonth(),dtColumnDate.getDate());
					calBudgetIntervalDate.set(Calendar.HOUR_OF_DAY,0);
					calBudgetIntervalDate.set(Calendar.MINUTE,0);
					calBudgetIntervalDate.set(Calendar.SECOND,0);
					calBudgetIntervalDate.set(Calendar.MILLISECOND,0);
					if((null != calBudgetIntervalDate && null!=calBudgetStartDate && null != calBudgetEndDate) && (!calBudgetIntervalDate.before(calBudgetStartDate) && !calBudgetIntervalDate.after(calBudgetEndDate))){
						slResult.add(Boolean.TRUE);
					}else{
						slResult.add(Boolean.FALSE);
					}
					//Added:PRG:MS9:R212:26-Aug-2011:IR-088614V6R2012x end
					i++;
				}
			}
			//End:NZF:18-Jul-2011:IR-074121V6R2012x
			return slResult;
		}catch(Exception exp){
			exp.printStackTrace();
			throw exp;
		}
	}
	public Map getMinMaxCostIntervalDates(Context context,String strProjectId) throws Exception {
		try {
			Map mapDate = new HashMap();
			DomainObject dmoProject = DomainObject.newInstance(context, strProjectId);
			String busTypeSelect = SELECT_TYPE ;


			String strRelationshipType = "";
			String strType = "";
			boolean strFrom = false;
			boolean strTo = false;


			strRelationshipType = FinancialItem.RELATIONSHIP_PROJECT_FINANCIAL_ITEM;
			strType = FinancialItem.TYPE_FINANCIAL_ITEM;
			strTo = true;


			final String SELECT_ATTRIBUTE_COST_INTERVAL_START_DATE = FinancialItem.SELECT_COST_INTERVAL_START_DATE;
			final String SELECT_ATTRIBUTE_COST_INTERVAL_END_DATE  =  FinancialItem.SELECT_COST_INTERVAL_END_DATE;

			StringList busSelect = new StringList();
			busSelect.add(DomainConstants.SELECT_ID);
			busSelect.add(SELECT_ATTRIBUTE_COST_INTERVAL_START_DATE);
			busSelect.add(SELECT_ATTRIBUTE_COST_INTERVAL_END_DATE);


			StringList relSelect = new StringList();
			String whereClause = "" ;



			MapList mlBudget = dmoProject.getRelatedObjects(
					context,
					strRelationshipType,
					TYPE_BUDGET,
					busSelect,
					relSelect,
					strFrom,
					strTo,
					(short)1,
					whereClause,
					DomainConstants.EMPTY_STRING);


			Map mapBudgetInfo = null;
			String strBudgetId = "";
			String strBudgetStartDate = "";
			String strBudgetEndDate = "";
			String strchkBudStartDate = "";
			String strchkBudEndDate = "";

			Date dtStartDate = null;
			Date dtEndDate = null;

			Date dtChkStartDate = null;
			Date dtChkEndDate = null;

			for (Iterator iterBudget = mlBudget.iterator(); iterBudget .hasNext();)
			{
				mapBudgetInfo = (Map) iterBudget.next();
				strBudgetId = (String) mapBudgetInfo.get(SELECT_ID);
				strBudgetStartDate = (String) mapBudgetInfo.get(SELECT_ATTRIBUTE_COST_INTERVAL_START_DATE);
				strBudgetEndDate = (String) mapBudgetInfo.get(SELECT_ATTRIBUTE_COST_INTERVAL_END_DATE);
				if(null == strchkBudStartDate  || "null" .equals(strchkBudStartDate) || "".equals(strchkBudStartDate)){
					strchkBudStartDate = strBudgetStartDate;
					dtChkStartDate =  eMatrixDateFormat.getJavaDate(strchkBudStartDate);
				}
				if(null == strchkBudEndDate  || "null" .equals(strchkBudEndDate) || "".equals(strchkBudEndDate)){
					strchkBudEndDate = strBudgetEndDate;
					dtChkEndDate =  eMatrixDateFormat.getJavaDate(strchkBudEndDate);
				}

				if(null != strchkBudStartDate  && !"null" .equals(strchkBudStartDate) && !"".equals(strchkBudStartDate)){

					dtStartDate = eMatrixDateFormat.getJavaDate(strBudgetStartDate);


					if(dtStartDate.before(dtChkStartDate)){
						dtChkStartDate = dtStartDate;
					}
				}

				if(null != strchkBudEndDate  && !"null" .equals(strchkBudEndDate) && !"".equals(strchkBudEndDate)){
					dtEndDate = eMatrixDateFormat.getJavaDate(strBudgetEndDate);

					if(dtEndDate.after(dtChkEndDate)){
						dtChkEndDate = dtEndDate;
					}
				}
			}
			mapDate.put("TimeLineIntervalFrom",dtChkStartDate);
			mapDate.put("TimeLineIntervalTo",dtChkEndDate);


			return mapDate;
		}
		catch (Exception exp)
		{
			exp.printStackTrace();
			throw exp;
		}

	}

	//Moved this method to Financial Item.java
	//    public ArrayList getIntervalDateList(Date startDate,Date endDate, String timeLineInterval)throws Exception {
	//
	// 	   ArrayList datelist=null;
	//
	// 	   if((CostItem.MONTHLY).equals(timeLineInterval))
	// 		{
	// 		   datelist=CostItem.getMonthlyDateList(startDate,endDate);
	// 		}
	// 	   else if((CostItem.WEEKLY).equals(timeLineInterval))
	// 		{
	// 		   datelist=CostItem.getWeeklyDateList(startDate,endDate);
	// 		}
	// 	   else
	// 		{
	// 		   datelist=CostItem.getQuarterlyDateList(startDate,endDate);
	// 		}
	//
	// 	   return datelist;
	//
	//    }



	/* Gets the month name for displaying in the Benefit Table column for dynamic column value
	 *
	 * @param nMonth The month number
	 * @return String containing month name
	 * @throws Exception if operation fails
	 */
	public String getMonthName(Context context,int nCurrentMonthValue)  throws Exception {
		try {

			String strI18MonthName = "";
			final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
			i18nNow loc = new i18nNow();
			String strLanguage=context.getSession().getLanguage();
			if(nCurrentMonthValue == 1){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Jan");
			}
			if(nCurrentMonthValue == 2){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Feb");
			}
			if(nCurrentMonthValue == 3){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Mar");
			}
			if(nCurrentMonthValue == 4){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Apr");
			}
			if(nCurrentMonthValue == 5){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.May");
			}
			if(nCurrentMonthValue == 6){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Jun");
			}
			if(nCurrentMonthValue == 7){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Jul");
			}
			if(nCurrentMonthValue == 8){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Aug");
			}
			if(nCurrentMonthValue == 9){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Sep");
			}
			if(nCurrentMonthValue == 10){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Oct");
			}
			if(nCurrentMonthValue == 11){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Nov");
			}
			if(nCurrentMonthValue == 12){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Dec");
			}


			return strI18MonthName;

		}
		catch (Exception exp) {
			exp.printStackTrace();
			throw exp;
		}
	}





	/**
	 * Gets the data for the dynamic columns for table "PMCProjectBenefitSummaryTable"
	 *
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception if operation fails
	 */
	public Vector getColumnBudgetData(Context context, String[] args)  
			throws Exception 
			{
		Vector vecResult = new Vector();
		try {
			// Create result vector
			// Get object list information from packed arguments
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map columnMap = (Map) programMap.get("columnMap");
			Map paramList = (Map) programMap.get("paramList");
			Locale locale = (Locale)paramList.get("localeObj");
			String tableName = (String)paramList.get("selectedTable");
			String strPrefferedCurrency = PersonUtil.getCurrency(context);
			String currency = ""; 
			if(paramList.containsKey(BUDGETREPORTCURRENCYFILTER)){
				currency = (String)paramList.get(BUDGETREPORTCURRENCYFILTER);
			}else{
				currency = (String)paramList.get(BUDGETCURRENCYFILTER);
			}
			if(ProgramCentralUtil.isNullString(currency)){
				currency = strPrefferedCurrency;
			}
			String strColumnName = (String) columnMap.get(SELECT_NAME);

			String strReportTImelineFilter = null;
			String SELECT_ATTRIBUTE_COST_INTERVAL = "attribute["+ATTRIBUTE_COST_INTERVAL+"]";
			strReportTImelineFilter = (String)paramList.get(BUDGETTIMELINEREPORTFILTER);
			String strReprotViewFilter=(String)paramList.get(BUDGETVIEWREPORTFILTER);
			Map mapObjectInfo = null;
			String strColumnValues = null;

			NumberFormat numberFormat = ProgramCentralUtil.getNumberFormatInstance(2, false);
			String interval = "";
			for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
			{
				mapObjectInfo = (Map) itrTableRows.next();
				interval = (String)mapObjectInfo.get(SELECT_ATTRIBUTE_COST_INTERVAL);
				String strParentId = (String)mapObjectInfo.get("id[parent]");
				String strId = (String)mapObjectInfo.get(SELECT_ID);

				DomainObject dmoObj = null;
				boolean isProject = false;
				if(null!=strId && !"null".equalsIgnoreCase(strId) && !"".equals(strId)){
					dmoObj = DomainObject.newInstance(context, strId);
					isProject = dmoObj.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE);
				}
				if(isProject){
					vecResult.add("");
					return vecResult;
				}
				if (!"Total".equals(strColumnName)) {
					strColumnValues = (String) mapObjectInfo.get(strColumnName);
					if (null != currency && !"null".equals(currency)
							&& !"".equals(currency)) {
						String strColUnit = "";
						strColUnit = (String) mapObjectInfo.get(strColumnName
								+ "_Unit");
						if (null != strColUnit && !currency.equals(strColUnit)) {
							double dblColumnValues = Double
									.parseDouble(strColumnValues);
							dblColumnValues = convertAmount(context,
									dblColumnValues, strColUnit, currency,
									new Date());
							strColumnValues = String.valueOf(numberFormat
									.format(dblColumnValues));
						}
					}
					String strBudgetTimelineInterval = null;
					Map mapDate = new HashMap();
					if (null != strReportTImelineFilter) {
						for (Iterator iterator = mapObjectInfo.keySet()
								.iterator(); iterator.hasNext();) {
							String strkey = (String) iterator.next();
							if ("DateMap".equals(strkey)) {
								mapDate = (Map) mapObjectInfo.get("DateMap");
							}
							if ("TimeLine".equals(strkey)) {
								strBudgetTimelineInterval = (String) mapObjectInfo
										.get("TimeLine");
							}
						}

						if(actualView.equals(strReprotViewFilter) || 
								("PMCProjectBudgetReportTable".equals(tableName) && ProgramCentralConstants.PROJECT_PHASE.equals(interval))
								){
							String strType = (String)mapObjectInfo.get(SELECT_TYPE);
							if(TYPE_BUDGET.equals(strType)){
								Map trial = (Map) mapObjectInfo.get("mapBudget");
								mapDate = (Map) mapObjectInfo.get("mapBudget");
							}
						}

						if (null != strReportTImelineFilter) {
							if (null != strBudgetTimelineInterval && !strBudgetTimelineInterval.equals(strReportTImelineFilter)) {
								if (strColumnName.contains("/")) {
									strColumnValues = getConsolidatedColumnData(context, strBudgetTimelineInterval,
											strReportTImelineFilter, mapDate,
											strColumnName);
								}
							}
						}
					}
					if (ProgramCentralUtil.isNullString(strColumnValues)) {
						strColumnValues = "0.0";
					}

					Double dblTotal = (Double) mapObjectInfo.get("Fiscal_Total");
					if (dblTotal == null) {
						dblTotal = 0D;
					}
					if(!"NA".equals(strColumnValues)){
						dblTotal += Task.parseToDouble(strColumnValues);
						mapObjectInfo.put("Fiscal_Total", dblTotal);
					}
				}
				else {
					strColumnValues = String.valueOf( mapObjectInfo.get("Total"));
					//strColumnValues = String.valueOf( mapObjectInfo.get("Fiscal_Total"));
					mapObjectInfo.put("Fiscal_Total",0.0);
				}
				//To avoid Currency Formatting for Variance.
				double dblValue; 
				if(!"VariancePercent".equals(strColumnName)){
					dblValue = Task.parseToDouble(strColumnValues);
					strColumnValues = Currency.format (context, currency, dblValue);
				}else{
					if (!"NA".equals(strColumnValues)){
						dblValue = Task.parseToDouble(strColumnValues);
						Locale currencyLocale = Currency.getCurrencyLocale(context, strPrefferedCurrency);
						NumberFormat formatter = NumberFormat.getNumberInstance(currencyLocale);
						strColumnValues = formatter.format(dblValue);
					}
				}
				vecResult.add(strColumnValues);
			}
			return vecResult;

		} catch (Exception exp) {

			exp.printStackTrace();
			throw exp;
		}
			}


	private String getConsolidatedColumnData(Context context, 
			String strBudgetTimelineInterval, String strReportTImelineFilter, 
			Map mapObjectInfo,String strColumnName) 
					throws MatrixException 
					{
		if(null == mapObjectInfo)
			return "0.0";
		String strColumnValue = null;
		Calendar calStartDate = Calendar.getInstance();
		Calendar calEndDate = Calendar.getInstance();
		int nMonth = 0;
		int nDate = 0;
		int nYear = 0;
		StringList slDate = FrameworkUtil.split(strColumnName, "/");
		nMonth = Integer.parseInt((String)slDate.get(0));
		nDate = Integer.parseInt((String)slDate.get(1));
		nYear = Integer.parseInt((String)slDate.get(2));
		int nFinishDate = 0;
		nMonth = (nMonth-1);
		calStartDate.set(nYear,nMonth,nDate);
		Date startDate = calStartDate.getTime();
		Date endDate = startDate;
		Interval interval = null;
		if(MONTHLY.equals(strReportTImelineFilter)){
			interval = Financials.getNextFiscalInterval(calStartDate.getTime(),IntervalType.MONTHLY);
		}else if(QUARTERLY.equals(strReportTImelineFilter)){
			interval = Financials.getNextFiscalInterval(calStartDate.getTime(),IntervalType.QUARTERLY);
		}else if(ANNUALLY.equals(strReportTImelineFilter)){
			interval = Financials.getNextFiscalInterval(calStartDate.getTime(),IntervalType.YEARLY);
		}else{
			interval = Financials.getNextFiscalInterval(calStartDate.getTime(),IntervalType.WEEKLY);
		}
		endDate = interval.getStartDate();
		long endDateMs = endDate.getTime();
		endDateMs = endDateMs - 86400000;
		calEndDate.setTimeInMillis(endDateMs);
		calEndDate.set(Calendar.HOUR_OF_DAY,0);
		calEndDate.set(Calendar.MINUTE,0);
		calEndDate.set(Calendar.SECOND,0);
		calEndDate.set(Calendar.MILLISECOND,0);

		calStartDate.setTimeInMillis(startDate.getTime());
		calStartDate.set(Calendar.HOUR_OF_DAY,0);
		calStartDate.set(Calendar.MINUTE,0);
		calStartDate.set(Calendar.SECOND,0);
		calStartDate.set(Calendar.MILLISECOND,0);

		double dblValue = 0d;
		double dblResultValue = 0d;
		for (Iterator iterator = mapObjectInfo.keySet().iterator(); iterator.hasNext();)
		{
			int nKeyMonth = 0;
			int nKeyDate = 0;
			int nKeyYear = 0;

			String strkey = (String) iterator.next();
			if(!strkey.contains("_Unit")){
				StringList slKeyDate = FrameworkUtil.split(strkey, "/");

				nKeyMonth = Integer.parseInt((String)slKeyDate.get(0));
				nKeyDate = Integer.parseInt((String)slKeyDate.get(1));
				nKeyYear = Integer.parseInt((String)slKeyDate.get(2));
			}
			Calendar calKeyDate = Calendar.getInstance();
			calKeyDate.clear(); 
			calKeyDate.set(nKeyYear, nKeyMonth-1,nKeyDate);
			calKeyDate.set(Calendar.HOUR_OF_DAY,0);
			calKeyDate.set(Calendar.MINUTE,0);
			calKeyDate.set(Calendar.SECOND,0);
			calKeyDate.set(Calendar.MILLISECOND,0);
			if((calKeyDate.after(calStartDate)&& calKeyDate.before(calEndDate)) || (calKeyDate.equals(calStartDate)) || (calKeyDate.equals(calEndDate))  ){
				String strValue = (String)mapObjectInfo.get(strkey);
				dblValue = Task.parseToDouble(strValue);
				dblResultValue += dblValue;
			}
		}
		NumberFormat numberFormat = NumberFormat.getInstance();
		numberFormat.setGroupingUsed(false);

		strColumnValue = String.valueOf(numberFormat.format(dblResultValue));
		return strColumnValue;
					}

	/**
	 * This method updates the cost/benefit value for Budget/Benefit Table
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @throws Exception if operation fails
	 * @author IXE
	 */

	public void updateDynamicColumnData(Context context,String[]args) throws Exception
	{
		updateDynamicColumnData(context,args,true);
	}

	/** addPeriod
	 * @param context Matrix Context Object
	 * @param args
	 * @return String
	 * @throws Exception if operation fails
	 * @author IXE
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void addPeriod (Context context, String[] args) throws Exception
	{
		try{
			addPeriod(context,args,true);
		}catch(Exception exp){
			exp.printStackTrace();
			throw exp;
		}
	}

	/** getInterval
	 * @param context Matrix Context Object
	 * @param args
	 * @return String
	 * @throws Exception if operation fails
	 * author IXE
	 */
	public String getInterval(Context context, String args[]) throws Exception
	{
		StringBuffer strHTMLBuffer = new StringBuffer();
		Map programMap   = (HashMap) JPO.unpackArgs(args);
		Map budgetMap = (Map)programMap.get("requestMap");
		String strTimeLineInterval="";
		String strBI=FinancialItem.SELECT_COST_INTERVAL;
		String strPSObjId=(String) budgetMap.get("objectId");
		DomainObject projectSpaceDom = DomainObject.newInstance(context, strPSObjId);
		String relPattern=Financials.RELATIONSHIP_PROJECT_FINANCIAL_ITEM;
		String typePattern=PropertyUtil.getSchemaProperty(context,"type_Budget");
		StringList objectSelects=new StringList();
		objectSelects.add(strBI);
		String strPeriod=ProgramCentralConstants.EMPTY_STRING;
		MapList financialItems=projectSpaceDom.getRelatedObjects(context,relPattern,typePattern,objectSelects,null,false,true,(short)1,null,null);

		Map financialItem=null;
		for (Iterator itrFI = financialItems.iterator(); itrFI.hasNext();)
		{
			financialItem = (Map) itrFI.next();
			strTimeLineInterval=(String)financialItem.get(strBI);
			if("Monthly".equals(strTimeLineInterval))
			{
				strPeriod = EnoviaResourceBundle.getProperty(context, "Framework", 			
						"emxFramework.Range.Interval.Monthly", context.getSession().getLanguage());
			}else if(("Quarterly").equals(strTimeLineInterval))
			{
				strPeriod = EnoviaResourceBundle.getProperty(context, "Framework", 			
						"emxFramework.Range.Interval.Quarterly", context.getSession().getLanguage());
			}else if(("Weekly").equals(strTimeLineInterval))
			{
				strPeriod = EnoviaResourceBundle.getProperty(context, "Framework", 			
						"emxFramework.Range.Interval.Weekly", context.getSession().getLanguage());
			}
		}
		final String ROW_NAME = "TimeLineInterval";
		strHTMLBuffer.append("<input type='text' readonly='readonly' size='' name='").append(XSSUtil.encodeForHTML(context,ROW_NAME)).append("' value=\'"
				+ XSSUtil.encodeForHTML(context,strPeriod) +"'/>");
		return strHTMLBuffer.toString();
	}


	/** getIntervalPeriodsForAdd
	 * @param context Matrix Context Object
	 * @param args
	 * @return String
	 * @throws Exception if operation fails
	 * author IXE
	 */
	public String getIntervalPeriodsForAdd(Context context, String args[]) throws Exception
	{
		StringBuffer strHTMLBuffer = new StringBuffer();

		i18nNow loc = new i18nNow();
		String strLanguage=context.getSession().getLanguage();

		//int addIntervalLimit = Integer.parseInt((String)loc.GetString("emxProgramCentral", strLanguage, "emxProgramCentral.Budget.IntervalPeriodsLimit"));

		String strIntervalLimit = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.Budget.IntervalPeriodsLimit") ;
		int addIntervalLimit = Integer.parseInt(strIntervalLimit);
		strHTMLBuffer.append("<select name='addinterval'>");
		for (int j = 1; j <= addIntervalLimit; j++)
		{

			strHTMLBuffer.append("<option name='addinterval'  value=\'"+j+"'>"+j);
			strHTMLBuffer.append("</option>");

		}
		strHTMLBuffer.append("</select>");

		return strHTMLBuffer.toString();

	}

	/** getIntervalPeriodsForDelete
	 * @param context Matrix Context Object
	 * @param args
	 * @return String
	 * @throws Exception if operation fails
	 * author IXE
	 */
	public String getIntervalPeriodsForDelete(Context context, String args[]) throws Exception
	{
		Map mapProgram = (Map) JPO.unpackArgs(args);
		Map requestMap = (Map)mapProgram.get("requestMap");
		DomainObject dmoProjectSpace=DomainObject.newInstance(context, (String)requestMap.get("objectId"));
		CostItem costItem = new CostItem();
		String strFIId=costItem.getBudgetorBenefitCreated(context,TYPE_BUDGET,dmoProjectSpace);
		DomainObject dmoFinItm=DomainObject.newInstance(context, strFIId);
		StringList objSelect=new StringList();

		objSelect.add(Financials.ATTRIBUTE_COST_INTERVAL_START_DATE);
		objSelect.add(Financials.ATTRIBUTE_COST_INTERVAL_END_DATE);
		objSelect.add(Financials.ATTRIBUTE_COST_INTERVAL);

		Map mpFinItm=dmoFinItm.getAttributeMap(context);
		Date dtStartDate = new Date((String)mpFinItm.get(Financials.ATTRIBUTE_COST_INTERVAL_START_DATE));
		Date dtFinishDate = new Date((String)mpFinItm.get(Financials.ATTRIBUTE_COST_INTERVAL_END_DATE));
		String strTimeLineInterval=(String)mpFinItm.get(Financials.ATTRIBUTE_COST_INTERVAL);


		ArrayList dateList =  getIntervalDateList(dtStartDate,dtFinishDate,strTimeLineInterval);


		StringBuffer strHTMLBuffer = new StringBuffer();

		i18nNow loc = new i18nNow();
		String strLanguage=context.getSession().getLanguage();

		int deleteIntervalLimit = dateList.size();

		strHTMLBuffer.append("<select name='addinterval'>");
		for (int j = 1; j < deleteIntervalLimit; j++)
		{

			strHTMLBuffer.append("<option name='addinterval'  value=\'"+j+"'>"+j);
			strHTMLBuffer.append("</option>");

		}
		strHTMLBuffer.append("</select>");

		return strHTMLBuffer.toString();

	}

	/** getPeriodTo
	 * @param context Matrix Context Object
	 * @param args
	 * @return String
	 * @throws Exception if operation fails
	 * author IXE
	 */
	public String getPeriodTo(Context context, String args[]) throws Exception
	{
		StringBuffer strHTMLBuffer = new StringBuffer();

		i18nNow loc = new i18nNow();
		String strLanguage=context.getSession().getLanguage();
		String tlPeriodValuesFromStart = (String)loc.GetString("emxProgramCentralStringResource", strLanguage, "emxProgramCentral.Budget.IntervalPeriodsFromStart");
		String tlPeriodValuesFromEnd = (String)loc.GetString("emxProgramCentralStringResource", strLanguage, "emxProgramCentral.Budget.IntervalPeriodsFromEnd");

		String strStart = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.Budget.IntervalPeriodsFromStart");
		String strEnd = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.Budget.IntervalPeriodsFromEnd");

		strHTMLBuffer.append("<input type='radio' name='PeriodTo'  value=\'"+XSSUtil.encodeForHTML(context,strStart)+"' checked='true' />");
		strHTMLBuffer.append(XSSUtil.encodeForHTML(context,tlPeriodValuesFromStart));
		strHTMLBuffer.append("<input type='radio' name='PeriodTo'  value=\'"+XSSUtil.encodeForHTML(context,strEnd)+"'/>");
		strHTMLBuffer.append(tlPeriodValuesFromEnd);

		return strHTMLBuffer.toString();

	}
	/** removePeriod
	 * @param context Matrix Context Object
	 * @param args
	 * @return void
	 * @throws Exception if operation fails
	 * author IXE
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void removePeriod (Context context, String[] args) throws Exception
	{
		try{
			deletePeriod(context,args,true);
		}catch(Exception exp){
			exp.printStackTrace();
			throw exp;
		}
	}


	/*
	 * This method connects the cost item object to the Phase and
	 *  updates the planned cost attribute on the relationship Cost Interval Item
	 * @param context the eMatrix <code>Context</code> object
	 * @param args
	 * @throws Exception if the operation fails
	 * @since PMC R210
	 * @author IXE
	 */
	public void updateDynamicColumnPhaseData(Context context,String[]args) throws Exception {
		try
		{

			//Code to get Company Id of the person.
			//String strQuery = "temp query bus "+TYPE_PERSON+ " \""+ context.getUser()+ "\" * select id dump |";
			String strQuery = "temp query bus $1 $2 $3 select $4 dump $5";
			String strPersonId = MqlUtil.mqlCommand(context,strQuery,TYPE_PERSON,context.getUser(),"*","id","|");

			PersonUtil person = new PersonUtil();

			Map programMap	= (Map) JPO.unpackArgs(args);
			Map mpParamMap  = (Map)programMap.get("paramMap");
			Map mpColumnMap                     = (HashMap)programMap.get("columnMap");
			Map mpRequestMap                    = (HashMap)programMap.get("requestMap");
			Locale locale 						= (Locale)mpRequestMap.get("locale");
			String strProjectId = (String)mpRequestMap.get("projectID");
			if(ProgramCentralUtil.isNullString(strProjectId)){
				strProjectId = (String)mpRequestMap.get("parentOID");
			}
			if(isBackgroundTaskActive(context, strProjectId)){
				getCurrencyModifyBackgroundTaskMessage(context, true);
				return;
			}
			String strColumnName                = QUERY_WILDCARD+(String)mpColumnMap.get("name");
			String strPhaseName                 = (String)mpColumnMap.get("label");
			String strPhaseId                 = (String)mpColumnMap.get("name");
			String strItemId                    = (String)mpParamMap.get("objectId");

			String strNewValue               = "";
			double nNewValue                 = 0;
			StringList objectSelects=new StringList();
			StringList relSelects=new StringList();

			String SELECT_PLANNED_ATTRIBUTE=null;
			String SELECT_INTERVAL_DATE=null;
			String RELATIONSHIP_ITEM_INTERVAL=null;
			String SELECT_INTERVAL_PLANNED_COST=null;
			String SELECT_COST_PLANNED_VALUE=null;			 
			String selecteView = "";
			String ATTRIBUTE = "";
			selecteView = (String)mpRequestMap.get(BUDGETVIEWFILTER);
			if(selecteView.contains("%20"))
				selecteView = selecteView.replace("%20", ProgramCentralConstants.SPACE);
			SELECT_INTERVAL_DATE=CostItemIntervalRelationship.SELECT_INTERVAL_DATE;
			RELATIONSHIP_ITEM_INTERVAL=RELATIONSHIP_COST_ITEM_INTERVAL;
			String strPrefferedCurrency = Currency.getBaseCurrency(context,strProjectId);

			if(selecteView.equalsIgnoreCase(planView))
			{
				SELECT_PLANNED_ATTRIBUTE=CostItem.SELECT_PLANNED_COST;
				ATTRIBUTE = ATTRIBUTE_PLANNED_COST;
				SELECT_COST_PLANNED_VALUE="from["+ RELATIONSHIP_FINANCIAL_ITEMS+"].to."+SELECT_PLANNED_ATTRIBUTE;
				SELECT_INTERVAL_PLANNED_COST=SELECT_COST_INTERVAL_PLANNED_COST;
			}
			if(selecteView.equalsIgnoreCase(estimateView))
			{
				SELECT_PLANNED_ATTRIBUTE=CostItem.SELECT_ESTIMATED_COST;
				ATTRIBUTE = ATTRIBUTE_ESTIMATED_COST;
				SELECT_COST_PLANNED_VALUE="from["+ RELATIONSHIP_FINANCIAL_ITEMS+"].to."+SELECT_PLANNED_ATTRIBUTE;
				SELECT_INTERVAL_PLANNED_COST=SELECT_COST_INTERVAL_ESTIMATED_COST;
			}
			if(selecteView.equalsIgnoreCase(actualView))
			{
				SELECT_PLANNED_ATTRIBUTE=CostItem.SELECT_ACTUAL_COST;
				ATTRIBUTE = ATTRIBUTE_ACTUAL_COST;
				SELECT_COST_PLANNED_VALUE="from["+ RELATIONSHIP_FINANCIAL_ITEMS+"].to."+SELECT_PLANNED_ATTRIBUTE;
				SELECT_INTERVAL_PLANNED_COST=SELECT_COST_INTERVAL_ACTUAL_COST;
			}

			//	SELECT_PLANNED_ATTRIBUTE=CostItem.SELECT_PLANNED_COST;
			//	SELECT_INTERVAL_DATE=CostItemIntervalRelationship.SELECT_INTERVAL_DATE;
			//	RELATIONSHIP_ITEM_INTERVAL=RELATIONSHIP_COST_ITEM_INTERVAL;
			//	SELECT_INTERVAL_PLANNED_COST=SELECT_COST_INTERVAL_PLANNED_COST;
			//SELECT_COST_PLANNED_VALUE="from["+ RELATIONSHIP_FINANCIAL_ITEMS+"].to."+CostItem.SELECT_PLANNED_COST;


			objectSelects.add(SELECT_PLANNED_ATTRIBUTE);
			relSelects.add(DomainRelationship.SELECT_ID);


			String relWhere=SELECT_INTERVAL_DATE+" ~~ \""+strColumnName+"\"";

			try
			{
				boolean showCurrencyConversionWarning = false;
				String strCurrencyConversionWarning = "";
				strNewValue=(String)mpParamMap.get("New Value");
				try{
					strNewValue = Currency.toBaseCurrency(context,strProjectId,strNewValue,true);
				}catch(MatrixException e){
					showCurrencyConversionWarning = true;
					strCurrencyConversionWarning = e.getMessage();
				}
				nNewValue = Task.parseToDouble(strNewValue);
				DomainObject dmoCostItem=DomainObject.newInstance(context, strItemId);
				String[]strPhase = new String[1];
				strPhase[0]= strPhaseId;
				StringList busSelect = new StringList();
				busSelect.add(SELECT_ID);
				StringList relSelect = new StringList();
				relSelect.add(DomainRelationship.SELECT_ID);
				String whereClause = null;
				String relWhere1 = null;
				whereClause = DomainConstants.SELECT_ID+" ~~ \""+strPhaseId+"\"";
				MapList mlCostItem = dmoCostItem.getRelatedObjects(
						context,
						RELATIONSHIP_ITEM_INTERVAL,
						TYPE_PHASE,
						busSelect,
						relSelect,
						false,
						true,
						(short)1,
						whereClause,
						relWhere1,
						0);
				Map mapCostIntervalItem = null;
				if(mlCostItem.size() != 0){
					for(Iterator itr=mlCostItem.iterator();itr.hasNext();) {
						Map objectMap = (Map) itr.next();
						String strPhaseid = (String)objectMap.get(SELECT_ID);
						String relId = (String)objectMap.get(DomainRelationship.SELECT_ID);
						DomainRelationship.setAttributeValue(context,relId,ATTRIBUTE,strNewValue+" "+strPrefferedCurrency);
					}
				}else{
					mapCostIntervalItem = DomainRelationship.connect(context, dmoCostItem, RELATIONSHIP_ITEM_INTERVAL, true,strPhase);
					String strBenIntervalRelId=(String)mapCostIntervalItem.get(strPhaseId);
					DomainRelationship.setAttributeValue(context,strBenIntervalRelId,ATTRIBUTE,strNewValue+" "+strPrefferedCurrency);
				}


				//============================================================================================================================================

				StringList slPlannedBenefitValues=dmoCostItem.getInfoList(context,SELECT_INTERVAL_PLANNED_COST);

				double nPlannedBenefit=0D;

				int tokens=slPlannedBenefitValues.size();
				for(int i=0;i<tokens;i++)
				{
					String tmp=(String)slPlannedBenefitValues.get(i);
					nPlannedBenefit=nPlannedBenefit+Task.parseToDouble(tmp);

				}

				dmoCostItem.setAttributeValue(context,ATTRIBUTE,nPlannedBenefit+" "+strPrefferedCurrency);

				//============================================================================================================================================


				DomainRelationship domRelFinItm=new DomainRelationship((String)mpParamMap.get("relId"));


				Relationship RelFinItm=new Relationship((String)mpParamMap.get("relId"));


				String strRelIdArray[]={(String)mpParamMap.get("relId")};
				relSelects.clear();
				relSelects.add(DomainRelationship.SELECT_FROM_ID);

				MapList mlRelFinItem=DomainRelationship.getInfo(context, strRelIdArray, relSelects);



				DomainObject financialDom=DomainObject.newInstance(context, (String)((Map)mlRelFinItem.get(0)).get(DomainRelationship.SELECT_FROM_ID));


				StringList benefitItemsPlannedBenefitValue= financialDom.getInfoList(context,SELECT_COST_PLANNED_VALUE);

				nPlannedBenefit=0;
				for(int i=0;i<benefitItemsPlannedBenefitValue.size();i++)
				{
					nPlannedBenefit=nPlannedBenefit+Task.parseToDouble((String)benefitItemsPlannedBenefitValue.get(i));

				}
				financialDom.setAttributeValue(context,ATTRIBUTE,nPlannedBenefit+" "+strPrefferedCurrency);
				if(showCurrencyConversionWarning)
					emxContextUtilBase_mxJPO.mqlNotice(context, strCurrencyConversionWarning);

			}
			catch (NumberFormatException e) {
				String strLanguage = context.getSession()
						.getLanguage();

				String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.ProjectBudget.SelectNumericValue", strLanguage);
				throw new Exception(sErrMsg + " " + strPhaseName);
			}

		}

		catch (Exception exp)
		{
			exp.printStackTrace();
			throw exp;
		}


	}

	/**
	 * This method decides whether to show the Remove Period.
	 * Remove Period will not available in case only one month is present.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds map with objectId of Relationship Actual Transaction Item.
	 * @throws Exception if the operation fails
	 * @since PMC R210
	 * * @author IXE
	 */
	public boolean isRemovePeriodAvailable (Context context, String[] args) throws Exception {

		Map programMap = (Map) JPO.unpackArgs(args);
		HashMap settingsMap = (HashMap) programMap.get("SETTINGS");
		String objectId = (String) programMap.get("objectId");
		DomainObject dObjProject = DomainObject.newInstance(context, objectId);
		
		String state = (String)dObjProject.getInfo(context, SELECT_CURRENT);
		
		String commandName = (settingsMap != null) ?(String)settingsMap.get("CmdName") : DomainConstants.EMPTY_STRING;
		if("PMCProjectBudgetRemovePeriod".equalsIgnoreCase(commandName)){
			if(!(STATE_PROJECT_SPACE_CREATE.equals(state) || STATE_PROJECT_SPACE_ASSIGN.equals(state) ||
				 STATE_PROJECT_SPACE_ACTIVE.equals(state) || STATE_PROJECT_SPACE_REVIEW.equals(state) ||
				 STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equals(state))){
				return false; 
			}
		}
		
		try{
			String strPreferredCurrency = emxProgramCentralUtilBase_mxJPO.getUserPreferenceCurrency(context, "emxProjectBudget.Error.NoDefaultCurrencyDefined");
		}catch(Exception e){
			return false;
		}
		boolean isAddCostItemAvailable = isAddCostItemAvailable(context,args);
		boolean isByPhase = isBudgetByPhase(context, args);
		boolean isFrozen = isCurrentStateFrozen(context,args,true); // if returned true then the state is not frozen
		if(isByPhase  == false){
			return false;
		}else{
			if(isFrozen == true){
				return isRemovePeriodAvailable(context,args,true);
			}else{
				return false;
			}
		}

		//	Map mapProgram = (Map) JPO.unpackArgs(args);
		//	String  selecteView = (String)mapProgram.get(BUDGETVIEWFILTER);
		//
		//	if(!actualView.equals(selecteView)){
		//		if(!isCurrentStateFrozen(context,args,true) && !planView.equals(selecteView))
		//		{
		//			return isRemovePeriodAvailable(context,args,true);
		//		}else{
		//			return false;
		//		}
		//	}
		//    else
		//    	return false;
	}


	/**
	 * This method decides whether to display Actual Transaction command or not. Tab will be displayed if the budget is frozen.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean isActualTransactionEnabled (Context context, String[] args) throws Exception {

		Map programMap = (Map)JPO.unpackArgs(args);
		String strProjectObjId = (String) programMap.get("objectId");
		String strType= TYPE_BUDGET;
		String strRelationshipType = RELATIONSHIP_PROJECT_FINANCIAL_ITEM;
		String strState = "";
		String whereClause = "" ;
		String strCostInterval = "";
		StringList busSelect = new StringList();
		busSelect.add(DomainConstants.SELECT_ID);
		busSelect.add(DomainConstants.SELECT_NAME);
		busSelect.add(SELECT_CURRENT);
		StringList relSelect = new StringList();
		DomainObject dom = DomainObject.newInstance(context, strProjectObjId);
		MapList mlBudget = dom.getRelatedObjects(
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

		Map mapBudgetInfo = null;
		for (Iterator iterBudget = mlBudget.iterator(); iterBudget .hasNext();)
		{
			mapBudgetInfo = (Map) iterBudget.next();
			strState = (String)mapBudgetInfo.get(SELECT_CURRENT);
		}
		String sPolicyFinancial = PropertyUtil.getSchemaProperty(context,"policy_FinancialItems");
		String sPlanFrozenStateName = PropertyUtil.getSchemaProperty(context,"policy",sPolicyFinancial,"state_PlanFrozen");
		if(sPlanFrozenStateName.equals(strState) && !isBackgroundTaskActive(context, strProjectObjId))
			return true;
		else
			return false;
	}




	/**
	 * relationshipActualTransactionItemDeleteOverride
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds map with objectId of Relationship Actual Transaction Item.
	 * @throws Exception if the operation fails
	 * @since PMC R210
	 * @author KP2
	 * This method is a trigger program
	 */

	public int relationshipActualTransactionItemDeleteOverride(Context context,String[] args) throws Exception{
		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}
		int returnValue = 0;
		int index = 0;
		String relId = args[index];
		if(relId != null && !"".equals(relId)){
			DomainRelationship domRel = DomainRelationship.newInstance(context, relId);
			domRel.open(context);
			String fromObjId = domRel.getFrom().getObjectId();
			String toObjId = domRel.getTo().getObjectId();
			DomainObject objActualTransaction = null;
			DomainObject objCostItem =null;
			double dActualTransactionAmount = 0.0;
			String strTransDate = null;
			String strCostInterval = "";
			Currency currency = new Currency();
			StringList currencies = currency.getCurrencyAttributeRange(context);
			String strBaseCurrency = (String)currencies.get(0);
			String projectId, strBudgetId;
			String connectedBudgetId = "to["+ Financials.RELATIONSHIP_FINANCIAL_ITEMS +"].from.id";
			String connectedProjectId = "to["+ Financials.RELATIONSHIP_PROJECT_FINANCIAL_ITEM +"].from.id";
			if(fromObjId != null && !"".equals(fromObjId)){
				objActualTransaction = DomainObject.newInstance(context, fromObjId);
				String strTransAmount = objActualTransaction.getAttributeValue(context, Financials.ATTRIBUTE_TRANSACTION_AMOUNT);
				strTransDate = objActualTransaction.getAttributeValue(context, PropertyUtil.getSchemaProperty(context,SYMBOLIC_ATTRIBUTE_TRANSACTION_DATE));
				if(strTransAmount != null && !"".equals(strTransAmount) ){
					dActualTransactionAmount = Task.parseToDouble(strTransAmount);
				}
				if(toObjId != null && !"".equals(toObjId) && strTransDate != null && !"".equals(strTransDate)){
					objCostItem = DomainObject.newInstance(context, toObjId);
					strBudgetId = (String)objCostItem.getInfo(context, connectedBudgetId);
					DomainObject budget = DomainObject.newInstance(context, strBudgetId);
					projectId = budget.getInfo(context, connectedProjectId);
					if(ProgramCentralUtil.isNotNullString(projectId)){
						strBaseCurrency = Currency.getBaseCurrency(context,projectId);
					}
					MapList relList = getCostItemIntervalRelMap(context, fromObjId);
					if(relList != null && relList.size() > 0){
						Map relMap = (Map) relList.get(0);
						String strRelId = (String) relMap.get(DomainRelationship.SELECT_ID);
						if(strRelId != null){
							DomainRelationship domRelCII = DomainRelationship.newInstance(context, strRelId);
							double dActualCost = Task.parseToDouble((String) domRelCII.getAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST));
							//set the Actual Cost Attribute of Cost Item Interval relationship with subtracting the transaction amount.
							domRelCII.setAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST, Double.toString(dActualCost - dActualTransactionAmount) + ProgramCentralConstants.SPACE + strBaseCurrency);
							dActualCost = Task.parseToDouble((String) objCostItem.getAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST));
							objCostItem.setAttributeValue(context,Financials.ATTRIBUTE_ACTUAL_COST , Double.toString(dActualCost - dActualTransactionAmount) + ProgramCentralConstants.SPACE + strBaseCurrency);
							String[] arguments = new String[2];
							arguments[0] = strBudgetId;
							arguments[1] = "1";
							double dBudgetRolledUpCost = getBudgetRollUpCost(context, arguments);
							if(strBudgetId != null && !"".equals(strBudgetId)){
								//set the Actual Cost Attribute of Budget object with sum of Actual Cost on all Cost Items connected to Budget
								budget.setAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST, Double.toString(dBudgetRolledUpCost) + ProgramCentralConstants.SPACE + strBaseCurrency);
							}
						}
					}
				}else{ returnValue = 1; }
			}else{	returnValue = 1; }
		}else{ 	returnValue = 1;  }

		return returnValue;
	}

	/**
	 * getCostItemIntervalRelMap
	 * @param context
	 * @param strTransactionId
	 * @return MapList with single map containing relId , Actual cost
	 * @throws Exception
	 * @author KP2
	 */
	public MapList getCostItemIntervalRelMap(Context context, String strTransactionId)throws Exception{
		MapList relList = null;

		if(strTransactionId != null && !"".equals(strTransactionId)){

			DomainObject objActualTransaction = DomainObject.newInstance(context, strTransactionId);
			String strAttributeTransactionDate = PropertyUtil.getSchemaProperty(context,SYMBOLIC_ATTRIBUTE_TRANSACTION_DATE);
			String strActualTransDate = objActualTransaction.getAttributeValue(context, strAttributeTransactionDate);
			String strRelActualTransactionItem = PropertyUtil.getSchemaProperty(context,SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_ITEM);
			String strCostItemId = objActualTransaction.getInfo(context,"from[" + strRelActualTransactionItem + "].to.id");
			if(strCostItemId != null && !"".equals(strCostItemId) && !"null".equals(strCostItemId)){
				DomainObject objCostItem = DomainObject.newInstance(context,strCostItemId);
				String strSelectCostInterval = "to["+Financials.RELATIONSHIP_FINANCIAL_ITEMS+"].from.attribute["+Financials.ATTRIBUTE_COST_INTERVAL+"].value";
				String strSelectCostIntervalStartDate = "to["+Financials.RELATIONSHIP_FINANCIAL_ITEMS+"].from.attribute["+Financials.ATTRIBUTE_COST_INTERVAL_START_DATE +"].value";
				String strCostInterval = objCostItem.getInfo(context,strSelectCostInterval);
				String strCostIntervalStartDate = objCostItem.getInfo(context,strSelectCostIntervalStartDate);
				Date costIntervalStartDate = eMatrixDateFormat.getJavaDate(strCostIntervalStartDate);
				Date transDate = eMatrixDateFormat.getJavaDate(strActualTransDate);
				String strFormattedDisplayDate = "";
				DateFormat dateFormat = DateFormat.getDateInstance(eMatrixDateFormat.getEMatrixDisplayDateFormat(), Locale.US);

				Calendar startCal = Calendar.getInstance();
				startCal.setTime(costIntervalStartDate);
				Calendar transCal = Calendar.getInstance();
				transCal.setTime(transDate);
				Calendar cal = Calendar.getInstance();
				cal.setTime(costIntervalStartDate);
				String relWhere = null;

				int transMonth  = transCal.get(Calendar.MONTH);
				int transYear = transCal.get(Calendar.YEAR);	    		
				if(CostItem.QUARTERLY.equals(strCostInterval)){
					int iQuarter = (int)transMonth/3;
					boolean iterateFlag = true;
					while (iterateFlag){
						int month = cal.get(Calendar.MONTH);
						if(iQuarter == (int)(month / 3)){
							iterateFlag = false;
							cal.set(Calendar.MONTH,month);
							cal.set(Calendar.YEAR, transYear);
							cal.setTime(Financials.getFiscalQuarterIntervalStartDate(cal.getTime()));
							strFormattedDisplayDate = dateFormat.format(cal.getTime());
							relWhere = CostItemIntervalRelationship.SELECT_INTERVAL_DATE + "== \""+ strFormattedDisplayDate + "\"";
						}else{
							cal.add(Calendar.MONTH, 3);
						}
					}
				}else if(CostItem.MONTHLY.equals(strCostInterval)){
					cal.set(Calendar.MONTH,transMonth);
					cal.set(Calendar.YEAR, transYear);
					cal.setTime(Financials.getFiscalMonthIntervalStartDate(transDate));
					strFormattedDisplayDate = dateFormat.format(cal.getTime());
					relWhere = CostItemIntervalRelationship.SELECT_INTERVAL_DATE + "== \""+ strFormattedDisplayDate + "\"";
				}else if (CostItem.WEEKLY.equals(strCostInterval)){
					int iWeekOfYear = transCal.get(Calendar.WEEK_OF_YEAR);
					int iStartWeek = startCal.get(Calendar.WEEK_OF_YEAR);
					int iWeekDiff = (iWeekOfYear - iStartWeek);
					if(iWeekDiff < 0 ){
						iWeekDiff = iWeekDiff + startCal.getActualMaximum(Calendar.WEEK_OF_YEAR);
					}
					cal.add(Calendar.WEEK_OF_YEAR, iWeekDiff);
					cal.setTime(Financials.getFiscalWeekIntervalStartDate(cal.getTime()));
					strFormattedDisplayDate = dateFormat.format(cal.getTime());
					relWhere = CostItemIntervalRelationship.SELECT_INTERVAL_DATE + "== \""+ strFormattedDisplayDate + "\"";

				}else if(CostItem.PROJECT_PHASE.equals(strCostInterval)){
					String strRelActualTransactionPhase = PropertyUtil.getSchemaProperty(context,SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_PHASE);
					String strPhaseName = objActualTransaction.getInfo(context, "from["+ strRelActualTransactionPhase +"].to.name");
					relWhere = CostItemIntervalRelationship.SELECT_TO_TYPE + " == \"" + Financials.TYPE_PHASE + "\" && "
							+ CostItemIntervalRelationship.SELECT_TO_NAME + " == \"" + strPhaseName +"\"";
				}else{
					i18nNow loc = new i18nNow();
					String strLanguage=context.getSession().getLanguage();
					final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
					String errMessage = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Budget.CostItem.Error.InvalidCostInterval");
					throw new IllegalDataException(errMessage);
				}
				String strRelCostItemInterval = Financials.RELATIONSHIP_COST_ITEM_INTERVAL;
				String typePattern            = TYPE_INTERVAL_ITEM_DATA +","+ TYPE_PHASE;
				StringList relSelects = new StringList();
				relSelects.add(DomainRelationship.SELECT_ID);
				relSelects.add(CostItemIntervalRelationship.SELECT_ACTUAL_COST);

				relList = objCostItem.getRelatedObjects(context,
						strRelCostItemInterval, //String relPattern
						typePattern, 		//String typePattern
						new StringList(),          	//StringList objectSelects,
						relSelects,            //StringList relationshipSelects,
						false,                  //boolean getTo,
						true,                   //boolean getFrom,
						(short)1,               //short recurseToLevel,
						"",          			//String objectWhere,
						relWhere,               //String relationshipWhere,
						0,						//int limit
						null,                   //Pattern includeType,
						null,                   //Pattern includeRelationship,
						null);
			}
		}
		return relList;
	}

	/**
	 * Generates a map containing 
	 * 1. Total Actual Transaction cost so far in a fiscal interval on which newly created 
	 * Actual transaction(strActualTransactionId) is lying and 
	 * 2. Total Actual cost of transaction objects connected to a cost item. 
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param strActualTransactionId String id of newly created Actual Transaction object.
	 * @return a Map of Total Actual cost.
	 * @throws Exception if operation fails.
	 */
	public Map getActualTransactionAmountSumMap(Context context, String strActualTransactionId )throws Exception {
		Double returnValueAllAT = new Double(0.0);
		Double returnValueATForInterval = new Double(0.0);
		if(strActualTransactionId != null && !"".equals(strActualTransactionId)){
			DomainObject objActualTransaction = DomainObject.newInstance(context, strActualTransactionId);
			String strAttributeTransactionDate = PropertyUtil.getSchemaProperty(context,SYMBOLIC_ATTRIBUTE_TRANSACTION_DATE);
			String strActualTransDate = objActualTransaction.getAttributeValue(context, strAttributeTransactionDate);
			String strRelActualTransactionItem = PropertyUtil.getSchemaProperty(context,SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_ITEM);
			String strCostItemId = objActualTransaction.getInfo(context,"from[" + strRelActualTransactionItem + "].to.id");
			if(strCostItemId != null && !"".equals(strCostItemId)){
				DomainObject objCostItem = DomainObject.newInstance(context,strCostItemId);
				String strSelectCostInterval = "to["+ Financials.RELATIONSHIP_FINANCIAL_ITEMS +"].from.attribute["+Financials.ATTRIBUTE_COST_INTERVAL+"].value";
				String strSelectCostIntervalStartDate = "to["+ Financials.RELATIONSHIP_FINANCIAL_ITEMS +"].from.attribute["+Financials.ATTRIBUTE_COST_INTERVAL_START_DATE +"].value";
				String strSelectCostIntervalEndDate = "to["+ Financials.RELATIONSHIP_FINANCIAL_ITEMS +"].from.attribute["+Financials.ATTRIBUTE_COST_INTERVAL_END_DATE +"].value";
				String strCostInterval = objCostItem.getInfo(context,strSelectCostInterval);
				Map mapActualTransactionAmount = null;

				if(ProgramCentralConstants.PROJECT_PHASE.equals(strCostInterval)) {
					String strRelActualTransactionPhase = PropertyUtil.getSchemaProperty(context,SYMBOLIC_RELATIONSHIP_ACTUAL_TRANSACTION_PHASE);
					String strPhaseName = objActualTransaction.getInfo(context, "from["+ strRelActualTransactionPhase +"].to.name");
					mapActualTransactionAmount = getActualTransactionAmountSumForPhase(context,strCostItemId,strPhaseName);
				}else{
					String strCostIntervalStartDate = objCostItem.getInfo(context,strSelectCostIntervalStartDate);
					String strCostIntervalEndDate = objCostItem.getInfo(context,strSelectCostIntervalEndDate);
					Date costIntervalStartDate = eMatrixDateFormat.getJavaDate(strCostIntervalStartDate);
					Date costIntervalEndDate = eMatrixDateFormat.getJavaDate(strCostIntervalEndDate);
					Date transDate = eMatrixDateFormat.getJavaDate(strActualTransDate);

					Calendar startCal = Calendar.getInstance();
					startCal.setTime(costIntervalStartDate);
					Calendar endCal = Calendar.getInstance();
					endCal.setTime(costIntervalEndDate);
					Calendar transCal = Calendar.getInstance();
					transCal.setTime(transDate);
					transCal.set(Calendar.HOUR, startCal.get(Calendar.HOUR));
					transCal.set(Calendar.MINUTE, startCal.get(Calendar.MINUTE));
					transCal.set(Calendar.SECOND, startCal.get(Calendar.SECOND));
					transCal.set(Calendar.AM_PM, startCal.get(Calendar.AM_PM));
					if(transCal.before(startCal) || transCal.after(endCal)){
						SimpleDateFormat dateFormat = new SimpleDateFormat("EEE d MMM yyyy");
						String startDate = dateFormat.format(costIntervalStartDate);
						String endDate = dateFormat.format(costIntervalEndDate);
						i18nNow loc = new i18nNow();
						String strLanguage=context.getSession().getLanguage();
						final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
						String errMessage = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Budget.ActualTransaction.Error.InvalidDate");
						throw new IllegalDataException(errMessage+"\n"+ startDate+" - "+endDate);
					}
					if(CostItem.QUARTERLY.equals(strCostInterval)){
						transDate = Financials.getFiscalQuarterIntervalStartDate(transDate);
					}else if(CostItem.MONTHLY.equals(strCostInterval)){
						transDate = Financials.getFiscalMonthIntervalStartDate(transDate);
					}else if(CostItem.WEEKLY.equals(strCostInterval)){
						transDate = Financials.getFiscalWeekIntervalStartDate(transDate);
					}else{
						i18nNow loc = new i18nNow();
						String strLanguage=context.getSession().getLanguage();
						final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
						String errMessage = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Budget.CostItem.Error.InvalidCostInterval");
						throw new IllegalDataException(errMessage);
					}
					mapActualTransactionAmount = getActualTransactionAmountSum(context,strCostItemId,strCostInterval,transDate);
				}
				returnValueATForInterval = (Double)mapActualTransactionAmount.get("ATForIntervalAmount");
				returnValueAllAT = (Double)mapActualTransactionAmount.get("AllATAmount");
			}
		}
		Map returnMap = new HashMap<String, Double>();
		returnMap.put("AllATAmount",new Double(returnValueAllAT));
		returnMap.put("ATForIntervalAmount",new Double(returnValueATForInterval));
		return returnMap;
	}

	/**
	 * rollUpBudgetActualCost
	 * @param context
	 * @param args
	 * @return int success = 0 or error = 1
	 * @throws Exception
	 * @author KP2
	 * @deprecated Cannot be used.
	 */
	public int rollUpBudgetActualCost(Context context, String[] args) throws Exception{
		int iReturnValue = 0;
		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}
		int returnValue = 0;
		int index = 0;
		String strBudgetId = args[index];
		if(strBudgetId != null && "".equals(strBudgetId)){
			DomainObject domBudget = DomainObject.newInstance(context,strBudgetId);
			if(domBudget.isKindOf(context, Financials.TYPE_BUDGET )){

				StringList objSelect = new StringList(2);
				objSelect.add(DomainObject.SELECT_ID);
				objSelect.add(Financials.ATTRIBUTE_ACTUAL_COST);
				//double dBudgetCost = 0.0;
				MapList objCostItemList = domBudget.getRelatedObjects(context,
						Financials.RELATIONSHIP_FINANCIAL_ITEMS, 	//String relPattern
						Financials.TYPE_COST_ITEM,						//String typePattern
						objSelect,					//StringList objectSelects,
						null,   				//StringList relationshipSelects,
						false,					//boolean getTo,
						true,                   //boolean getFrom,
						(short)1,               //short recurseToLevel,
						null,          		    //String objectWhere,
						null,     		        //String relationshipWhere,
						0,						//int limit
						null,                   //Pattern includeType,
						null,                   //Pattern includeRelationship,
						null);
				if(objCostItemList != null && !"".equals(objCostItemList)){
					for (Iterator itr=objCostItemList.iterator();itr.hasNext();) {
						Map objectMap = (Map) itr.next();
						String strCostItemId = (String) objectMap.get(DomainObject.SELECT_ID);
						DomainObject objCostItem = newInstance(context,strCostItemId);
						String[] arg = new String[1];
						arg[0] = strCostItemId;
						iReturnValue = rollUpCostItemActualCost(context, arg);
						//String strCost = objCostItem.getAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST);
						//dBudgetCost += Task.parseToDouble(strCost);
					}
				}
				//domBudget.setAttributeValue(context, Financials.ATTRIBUTE_ACTUAL_COST, Double.toString(dBudgetCost));
			}

		}
		else{
			iReturnValue = 1;
		}
		return iReturnValue;
	}

	/**
	 * rollUpCostItemActualCost
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @author KP2
	 * @deprecated Cannot be used.
	 */
	public int rollUpCostItemActualCost(Context context, String[] args) throws Exception{
		int iReturnValue = 0;
		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}
		int returnValue = 0;
		int index = 0;

		String strCostItemId = args[index];
		if(strCostItemId != null && "".equals(strCostItemId)){
			DomainObject objCostItem = DomainObject.newInstance(context,strCostItemId);
			if(objCostItem.isKindOf(context, Financials.TYPE_COST_ITEM )){
				StringList objSelect = new StringList(2);
				objSelect.add(DomainObject.SELECT_ID);
				objSelect.add(Financials.ATTRIBUTE_ACTUAL_COST);
				double dBudgetCost = 0.0;
				MapList objATList = objCostItem.getRelatedObjects(context,
						RELATIONSHIP_ACTUAL_TRANSACTION_ITEM, 	    //String relPattern
						TYPE_ACTUAL_TRANSACTION,						//String typePattern
						objSelect,					//StringList objectSelects,
						null,   				//StringList relationshipSelects,
						false,					//boolean getTo,
						true,                   //boolean getFrom,
						(short)1,               //short recurseToLevel,
						null,          		    //String objectWhere,
						null,     		        //String relationshipWhere,
						0,						//int limit
						null,                   //Pattern includeType,
						null,                   //Pattern includeRelationship,
						null);
				if(objATList != null && !"".equals(objATList)){
					for (Iterator itr=objATList.iterator();itr.hasNext();) {
						Map objectMap = (Map) itr.next();
						String strATId = (String) objectMap.get(DomainObject.SELECT_ID);
						String[] argList = new String[2];
						argList[0] = strATId;
						argList[1] = Financials.ATTRIBUTE_TRANSACTION_AMOUNT;
						transactionAmountModifyPostProcess(context, argList);
					}
				}
			}else{ iReturnValue = 1;}
		}else{ iReturnValue = 1;}

		return iReturnValue;
	}


	/**
	 * This method is used to get Range values for Views Filter
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 	 *
	 * @returns HashMap containing Range values for Assignee role
	 * @throws Exception if the operation fails	 *
	 * @author IXE
	 */
	public Map getBenefitOrBudgetViewsFilterRange(Context context, String[] args) throws Exception
	{
		return getBenefitOrBudgetViewsFilterRange(context,args,true);
	}


	public Map getBenefitOrBudgetDisplayViewsFilterRange(Context context, String[] args) throws Exception
	{
		Map programMap = (Map)JPO.unpackArgs(args);
		Map mpRequestMap = (HashMap)programMap.get("requestMap");
		Map rangeMap = new HashMap();
		i18nNow loc = new i18nNow();
		String strLanguage=context.getSession().getLanguage();
		String views="";
		StringList slDisplayList = new StringList();
		StringList slOriginalList = new StringList();
		final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";

		views = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.ProjectBenefitOrBudget.DisplayTimelineView", strLanguage);
		slDisplayList.add(views);
		slOriginalList.add("Timeline");

		views = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.ProjectBenefitOrBudget.DisplayFiscalView", strLanguage);		 
		slDisplayList.add(views);
		slOriginalList.add("FiscalYear");

		rangeMap.put("field_choices",slOriginalList);
		rangeMap.put("field_display_choices",slDisplayList);
		return  rangeMap;
	}

	/**
	 * This method is used to get Range values for Year Filter
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 	 *
	 * @returns HashMap containing Range values for Assignee role
	 * @throws Exception if the operation fails	 *
	 * @author IXE
	 */
	public Map getBenefitOrBudgetYearRange(Context context, String[] args) throws Exception
	{
		return getBenefitOrBudgetYearRange(context,args,true);

	}
	/** Check whether "Freeze Benefit" and "Delete Benefit Item" command will be
	 * visible to user or not
	 * If benefit/budget has already been frozen then command will not display to user
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @return true if column is to be shown
	 * @throws Exception if operation fails
	 * @author IXE
	 */
	public boolean isCurrentStateFrozen(Context context,String args[]) throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		HashMap settingsMap = (HashMap) programMap.get("SETTINGS");
		String objectId = (String) programMap.get("objectId");
		DomainObject dObjProject = DomainObject.newInstance(context, objectId);
		
		String state = (String)dObjProject.getInfo(context, SELECT_CURRENT);
		
		String commandName = (settingsMap != null) ?(String)settingsMap.get("CmdName") : DomainConstants.EMPTY_STRING;
		if("PMCProjectBudgetFreeze".equalsIgnoreCase(commandName)){
			if(!(STATE_PROJECT_SPACE_CREATE.equals(state) || STATE_PROJECT_SPACE_ASSIGN.equals(state)||
				 STATE_PROJECT_SPACE_ACTIVE.equals(state) || STATE_PROJECT_SPACE_REVIEW.equals(state)||
				 STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equals(state))){
				return false; 
			}
		}
		
		boolean bBudget = isBudgetCreated(context, args);
		if(bBudget){
			try{
				String strPreferredCurrency = emxProgramCentralUtilBase_mxJPO.getUserPreferenceCurrency(context, "emxProjectBudget.Error.NoDefaultCurrencyDefined");
			}catch(Exception e){
				return false;
			}
			return isCurrentStateFrozen(context,args,true);
		}else{
			return false;
		}
	}

	/**This method will be called to decide whether to display the Delete budget command or not.
	 * @param Context the eMatrix <code>Context</code> object
	 * @ param args *
	 * @return true if column is to be shown
	 * @throws Exception if operation fails
	 * @author IXE
	 */
	public boolean checkFinDeleteAccess(Context context, String args[])throws Exception {
		Map programMap = (Map)JPO.unpackArgs(args);
		String strTableName = (String) programMap.get("table");
		String strProjectId = (String)programMap.get("objectId");

		String strPreferredCurrency= EMPTY_STRING;
		StringList busSelects = new StringList();
		busSelects.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
		busSelects.add(SELECT_CURRENT);
		
		DomainObject  dmoProject = DomainObject.newInstance(context, strProjectId);
		Map projectInfoMap = dmoProject.getInfo(context, busSelects);
		if(!"PMCProjectTemplateBudgetTable".equals(strTableName)){
			try{
				String isKindOfProjectSpace = (String) projectInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
				if(Boolean.valueOf(isKindOfProjectSpace))
					strPreferredCurrency = emxProgramCentralUtilBase_mxJPO.getUserPreferenceCurrency(context, "emxProjectBudget.Error.NoDefaultCurrencyDefined");
			}catch(Exception e){
				return false;
			}
		}
		
		StringList tempStateList = new StringList();
		tempStateList.add(STATE_PROJECT_TASK_CREATE);
		tempStateList.add(STATE_PROJECT_TASK_ASSIGN);
		tempStateList.add(STATE_PROJECT_TASK_ACTIVE);
		tempStateList.add(STATE_PROJECT_TASK_REVIEW);
		tempStateList.add(STATE_BUDGET_PLAN_FROZEN);
		tempStateList.add(STATE_BUDGET_PLAN);
		tempStateList.add(STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD);
		
		String currState = (String) projectInfoMap.get(SELECT_CURRENT);
		boolean isInRightState = tempStateList.contains(currState);

		boolean isFrozen = isCurrentStateFrozen(context,args,true); // if isFrozen = true then budget is not frozen
		if(isFrozen == true){
			boolean hasDelAccess = isInRightState && checkFinDeleteAccess(context,args,true);
			if("PMCProjectTemplateBudgetTable".equals(strTableName)){
				ProjectTemplate projectTemplate = (ProjectTemplate)DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_TEMPLATE, DomainObject.PROGRAM);
		 		boolean isCtxUserOwnerOrCoOwner = projectTemplate.isOwnerOrCoOwner(context, strProjectId);
		 		hasDelAccess = (hasDelAccess && isCtxUserOwnerOrCoOwner);
			}
			return hasDelAccess;
		}
		else {
			return false;
	}
	}

	/** Check whether "Freeze udget" and "Delete Cost Item" command will be
	 * visible to user or not
	 * If benefit/budget has already been frozen then command will not display to user
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @return true if column is to be shown
	 * @throws Exception if operation fails
	 * @author IXE
	 */
	public boolean isBudgetByPhase(Context context,String args[]) throws Exception
	{
		String SELECT_ATTRIBUTE = "attribute["+ATTRIBUTE_COST_INTERVAL+"]";
		String strCostInterval =  isBudgetByPhaseOrTemplateEnforced(context,args,SELECT_ATTRIBUTE);
		if(PROJECT_PHASE.equals(strCostInterval))
			return false;
		else
			return true;
	}


	public boolean isBudgetFiscalYearFilterAvailable(Context context,String args[]) throws Exception
	{
		Map programMap = (Map)JPO.unpackArgs(args);
		String strDisplayFilter = (String) programMap.get("PMCProjectBudgetDisplayViewFilter");
		if("FiscalYear".equals(strDisplayFilter)){
			String SELECT_ATTRIBUTE = "attribute["+ATTRIBUTE_COST_INTERVAL+"]";
			String strCostInterval =  isBudgetByPhaseOrTemplateEnforced(context,args,SELECT_ATTRIBUTE);
			if(PROJECT_PHASE.equals(strCostInterval))
				return false;
			else
				return true;
		}else{
			return false;
		}
	}

	/**
	 * Checks if Fiscal year filter is accessible on Expense report page.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args request parameters.
	 * @return true if Fiscal is selected in the display view filter of Expense report page, plus,
	 * Budget is created by timeline.
	 * @throws Exception if operation fails.
	 */
	public boolean isExpenseReportFiscalYearFilterAccessible(Context context,String args[]) throws Exception{
		Map programMap = (Map)JPO.unpackArgs(args);
		String strDisplayFilter = (String) programMap.get("PMCProjectBudgetReportDisplayViewFilter");
		if("FiscalYear".equals(strDisplayFilter)){
			String SELECT_ATTRIBUTE = "attribute["+ATTRIBUTE_COST_INTERVAL+"]";
			String strCostInterval =  isBudgetByPhaseOrTemplateEnforced(context,args,SELECT_ATTRIBUTE);
			if(PROJECT_PHASE.equals(strCostInterval)) return false;
			else return true;
		}
		return false;
	}


	/** Check whether "Freeze Benefit" and "Delete Benefit Item" command will be
	 * visible to user or not
	 * If benefit/budget has already been frozen then command will not display to user
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @return true if column is to be shown
	 * @throws Exception if operation fails
	 * @author IXE
	 */
	public String isBudgetByPhaseOrTemplateEnforced(Context context,String args[],String SELECT_ATTRIBUTE) throws Exception
	{
		Map programMap = (Map)JPO.unpackArgs(args);
		String strProjectObjId = (String) programMap.get("objectId");
		String strType= TYPE_BUDGET;
		String strRelationshipType = RELATIONSHIP_PROJECT_FINANCIAL_ITEM;

		String whereClause = "" ;
		String strCostInterval = "";
		StringList busSelect = new StringList();
		busSelect.add(DomainConstants.SELECT_ID);
		busSelect.add(DomainConstants.SELECT_NAME);
		busSelect.add(SELECT_ATTRIBUTE);
		StringList relSelect = new StringList();
		DomainObject dom = DomainObject.newInstance(context, strProjectObjId);
		MapList mlBudget = dom.getRelatedObjects(
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

		Map mapBudgetInfo = null;
		for (Iterator iterBudget = mlBudget.iterator(); iterBudget .hasNext();)
		{
			mapBudgetInfo = (Map) iterBudget.next();
			strCostInterval = (String)mapBudgetInfo.get(SELECT_ATTRIBUTE);
		}

		return strCostInterval;

	}

	/**
	 * Decides whether template is enforced or not. <br>
	 * Returns TRUE : Template is not enforced, FALSE : Template is enforced. 
	 * 
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return boolean
	 * @throws Exception if operation fails
	 * @author IXE
	 */
	public boolean isTemplateEnforced(Context context,String args[]) throws Exception
	{
		String SELECT_ATTRIBUTE = "attribute["+ATTRIBUTE_TEMPLATE_ENFORCEMENT+"]";
		String strTemplateEnforce = isBudgetByPhaseOrTemplateEnforced(context,args,SELECT_ATTRIBUTE);
		if("TRUE".equalsIgnoreCase(strTemplateEnforce)) 	
			return false;
		else
			return true;
	}

	/**
	 * Decides whether to show Delete Cost Item Command or not
	 * Added:17-June-2010
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return boolean
	 * @throws Exception if operation fails
	 * @author IXE
	 */
	public boolean isDeleteCostItemAvailable(Context context,String args[]) throws Exception
	{
		boolean result = false;
		boolean bBudget = false;

		DomainObject dObj = DomainObject.newInstance(context);
		Map mProgram = (Map) JPO.unpackArgs(args);
		String isRMB = (String) mProgram.get("isRMB");
		String sTempObjId = (String) mProgram.get("objectId");
		String sRmbTableRowId = (String) mProgram.get("rmbTableRowId");

		if(null != isRMB && "true".equalsIgnoreCase(isRMB.trim())){
			Map sCostItemRowId = ProgramCentralUtil.parseTableRowId(context, sRmbTableRowId);
			sTempObjId = (String)sCostItemRowId.get("objectId");
			if(ProgramCentralUtil.isNotNullString(sTempObjId)){
				String sBudgetObjId = getBudgetFromCostItem(context,sTempObjId);

				if(ProgramCentralUtil.isNotNullString(sBudgetObjId)){	
					dObj.setId(sBudgetObjId);
					final String SELECT_IS_BUDGET =   "type.kindof["+TYPE_BUDGET+"]";
					final String SELECT_PROJECT ="to["+RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"].from.id";

					StringList slBudgetSelects = new StringList();
					slBudgetSelects.add(SELECT_IS_BUDGET);
					slBudgetSelects.add(SELECT_PROJECT);
					Map mapBudgetInfo = dObj.getInfo(context, slBudgetSelects);
					if(null != mapBudgetInfo){
						String isBudget = (String)mapBudgetInfo.get(SELECT_IS_BUDGET);
						if(null != isBudget && "true".equalsIgnoreCase(isBudget.trim())){
							String strProjectId = (String)mapBudgetInfo.get(SELECT_PROJECT);
							bBudget = true;

							if(ProgramCentralUtil.isNotNullString(strProjectId)){
								mProgram.put("objectId", strProjectId.trim());  // change the object id to project id in arg
								args = JPO.packArgs(mProgram);
							}
						}
					}

				}
			}
		}
		else{
			bBudget = isBudgetCreated(context, args);
		}

		Map mapProgram = (Map) JPO.unpackArgs(args);
		HashMap settingsMap = (HashMap) mapProgram.get("SETTINGS");
		String commandName = (settingsMap != null) ?(String)settingsMap.get("CmdName") : DomainConstants.EMPTY_STRING;

		DomainObject dObjProject = DomainObject.newInstance(context, (String) mapProgram.get("objectId"));
		String state = (String)dObjProject.getInfo(context, SELECT_CURRENT);
		
		if("PMCDeleteCostCategoryItem".equalsIgnoreCase(commandName)){
			String policyFinancialItems = PropertyUtil.getSchemaProperty(context,"policy_FinancialItems");
			String planStateName = PropertyUtil.getSchemaProperty(context,"policy",policyFinancialItems,"state_Plan");
			String planFrozenStateName = PropertyUtil.getSchemaProperty(context, "policy", policyFinancialItems, "state_PlanFrozen");
			if(!(STATE_PROJECT_SPACE_CREATE.equals(state) || STATE_PROJECT_SPACE_ASSIGN.equals(state)||
				 STATE_PROJECT_SPACE_ACTIVE.equals(state) || STATE_PROJECT_SPACE_REVIEW.equals(state) ||
				 STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equals(state) || planStateName.equals(state) || planFrozenStateName.equals(state))){
				return false; 
			}
		}

		try{
			if(bBudget){

				boolean isFrozen = false;
				boolean isTempEnforced = false;

				DomainObject dmoProject = DomainObject.newInstance(context, sTempObjId);
				try{
					if(dmoProject.isKindOf(context, TYPE_PROJECT_SPACE)){
						String strPreferredCurrency = emxProgramCentralUtilBase_mxJPO.getUserPreferenceCurrency(context, "emxProjectBudget.Error.NoDefaultCurrencyDefined");
					}
				}catch(Exception e){
					return result; 
				}

				String  selecteView = (String)mapProgram.get(BUDGETVIEWFILTER);

				isFrozen = isCurrentStateFrozen(context,args,true); // if isFrozen = true then budget is not frozen
				isTempEnforced = isTemplateEnforced(context,args); // if isTempEnforced = true then template is not enforced

				if(isTempEnforced == false){
					result = false;
				}else{

					if(isFrozen == false){
						result =  false;
					}else{
						result =  true;
					}
				}
			}
		}
		catch(Exception e){
			throw new MatrixException(e);
		}finally{
			mProgram.put("objectId", sTempObjId);
			args = JPO.packArgs(mProgram);   // retain the original object id
		}
		return result;
	}

	/**
	 * This method is used to get Budget object id from the Cost Item Id
	 * @param context the eMatrix <code>Context</code> object
	 * @param String strCostItemId cost Item Id        
	 * @returns String  Budget Id 
	 * @throws Exception if the operation fails	 * 
	 */
	private String getBudgetFromCostItem(Context context, String strCostItemId) throws MatrixException{
		String strBudgetId = "";

		if(strCostItemId == null || "Null".equalsIgnoreCase(strCostItemId) || "".equalsIgnoreCase(strCostItemId)){
			throw new IllegalArgumentException("Cost Item object id is null");
		}

		final String SELECT_IS_COST_ITEM =   "type.kindof["+TYPE_COST_ITEM+"]";
		final String SELECT_BUDGET ="to["+RELATIONSHIP_FINANCIAL_ITEMS+"].from.id";

		DomainObject dObj = DomainObject.newInstance(context);
		dObj.setId(strCostItemId);
		StringList slCheckListSelects = new StringList();
		slCheckListSelects.add(SELECT_IS_COST_ITEM);
		slCheckListSelects.add(SELECT_BUDGET);

		Map mapCheckListInfo = dObj.getInfo(context, slCheckListSelects);

		if(null != mapCheckListInfo){
			String isCostItemType = (String)mapCheckListInfo.get(SELECT_IS_COST_ITEM);
			if(null != isCostItemType && "true".equalsIgnoreCase(isCostItemType)){
				String strTempId = (String)mapCheckListInfo.get(SELECT_BUDGET);
				if(null != strTempId && !"Null".equalsIgnoreCase(strTempId.trim()) && !"".equalsIgnoreCase(strTempId.trim()))
					return strTempId;
			}
		}

		return strBudgetId;
	}

	/**
	 * Decides whether to show Add Cost Item Command or not
	 * Added:17-June-2010
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return boolean
	 * @throws Exception if operation fails
	 * @author IXE
	 */
	public boolean isAddCostItemAvailable(Context context,String args[]) throws Exception
	{
		boolean result = false;
		boolean bBudget = isBudgetCreated(context,args);
		if(bBudget){
			try{
				String strPreferredCurrency = emxProgramCentralUtilBase_mxJPO.getUserPreferenceCurrency(context, "emxProjectBudget.Error.NoDefaultCurrencyDefined");
			}catch(Exception e){
				return result;
			}
			Map mapProgram = (Map) JPO.unpackArgs(args);
			HashMap settingsMap = (HashMap) mapProgram.get("SETTINGS");
			String commandName = (settingsMap != null) ?(String)settingsMap.get("CmdName") : DomainConstants.EMPTY_STRING;
			String  selecteView = (String)mapProgram.get(BUDGETVIEWFILTER);
			String objectId = (String) mapProgram.get("objectId");
			DomainObject dObjProject = DomainObject.newInstance(context, objectId);
			boolean isFrozen = false;
			boolean isTempEnforced = false;

			isFrozen = isCurrentStateFrozen(context,args,true); // if returned true then the state is not frozen
			isTempEnforced = isTemplateEnforced(context,args); // if returned true then template is not enforced
			
			StringList selectList = new StringList();
			selectList.add(SELECT_CURRENT);
			selectList.add(SELECT_IS_PROJECT_SPACE);
			
			Map infoMap = dObjProject.getInfo(context, selectList);
			String isKindOfProjectSpace = (String) infoMap.get(SELECT_IS_PROJECT_SPACE);
			String state = (String) infoMap.get(SELECT_CURRENT);
			
			if("PMCProjectBudgetAddCostCategoryItem".equalsIgnoreCase(commandName)){
				String policyFinancialItems = PropertyUtil.getSchemaProperty(context, "policy_FinancialItems");
				String planStateName = PropertyUtil.getSchemaProperty(context,"policy", policyFinancialItems,"state_Plan");
				String planFrozenStateName = PropertyUtil.getSchemaProperty(context,"policy", policyFinancialItems,"state_PlanFrozen");
				if(!(STATE_PROJECT_SPACE_CREATE.equals(state) || STATE_PROJECT_SPACE_ASSIGN.equals(state)||
					 STATE_PROJECT_SPACE_ACTIVE.equals(state) || STATE_PROJECT_SPACE_REVIEW.equals(state) ||
					 STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equals(state) || planStateName.equals(state)|| planFrozenStateName.equals(state))){
					return false; 
				}
			}
			
			if(Boolean.valueOf(isKindOfProjectSpace))
			{
				if(isTempEnforced == false){
					result = false;
				}else{
					if(null == selecteView || selecteView.equals(planView)){
						if(isFrozen == true ){
							result = true;
						}else if(null == selecteView && false==isFrozen) {
							result= true;
						}else
							result = false;
					}else if (actualView.equals(selecteView)){
						result = false;
					}else{
						result= true;
					}
				}
			}
		}else{
			return result;
		}
		return result;
	}

	/**
	 * Checks if Add Cost Item command is available in Project Template to the context user.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args Holds important request parameters.
	 * @return true if Cost Item command is available to the context user.
	 * @throws Exception if operation fails.
	 */
	public boolean isAddCostItemAvailableTemplate(Context context,String args[]) throws Exception
	{
		Map mapProgram = (Map) JPO.unpackArgs(args);
		String rmbTableRowId = (String) mapProgram.get("rmbTableRowId");
		String strProjectTemplateId=null;

		if (rmbTableRowId != null){
			StringList objectList = FrameworkUtil.split(rmbTableRowId,"|");
			strProjectTemplateId = (String)objectList.elementAt(1) ;
		}else{
			strProjectTemplateId = (String) mapProgram.get("objectId");
		}     

		DomainObject dObjTemplate = DomainObject.newInstance(context, strProjectTemplateId);
		//Following is Added for to not show RMBMenu command Add Cost item  two times in project budget.
		boolean isProjectTemplate = dObjTemplate.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_TEMPLATE);
		boolean hasEditAccess = dObjTemplate.checkAccess(context, (short) AccessConstants.cModify);
		
		ProjectTemplate projectTemplate = (ProjectTemplate)DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_TEMPLATE, DomainObject.PROGRAM);
 		boolean isCtxUserOwnerOrCoOwner = projectTemplate.isOwnerOrCoOwner(context, strProjectTemplateId);
		
 		boolean hasAccess = isProjectTemplate && isBudgetCreated(context,args) && hasEditAccess && isCtxUserOwnerOrCoOwner;
		return hasAccess;
	}

	public boolean isDeleteCostItemAvailableTemplate(Context context,String args[]) throws Exception
	{
		Map mapProgram = (Map) JPO.unpackArgs(args);
		String strProjectTemplateId = (String) mapProgram.get("objectId");
				
		ProjectTemplate projectTemplate = (ProjectTemplate)DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_TEMPLATE, DomainObject.PROGRAM);
 		boolean isCtxUserOwnerOrCoOwner = projectTemplate.isOwnerOrCoOwner(context, strProjectTemplateId);
 		
		boolean bBudget = isBudgetCreated(context,args);
		boolean isTemplateEnforced = isTemplateEnforced(context,args);
		
		boolean hasAccess = isBudgetCreated(context,args) &&
							!(isTemplateEnforced(context,args)) && 
							isCtxUserOwnerOrCoOwner;
		
		return hasAccess;
	}

	public StringList isTemplateEnforcementEditable(Context context,String args[]) throws Exception
	{
		Map programMap = (Map)JPO.unpackArgs(args);

		MapList objectList = (MapList) programMap.get("objectList");
		StringList slHasAccess = new StringList();

		String strKey = EnoviaResourceBundle.getProperty(context, "emxFramework.Budget.TemplateEnforcement");
		DomainObject dmoObject = DomainObject.newInstance(context);
		for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
			Map object = (Map) iterator.next();
			String strObjectId = (String)object.get(SELECT_ID);
			dmoObject.setId(strObjectId);
			if(dmoObject.isKindOf(context, TYPE_BUDGET)){
				if(strKey.equalsIgnoreCase("True"))
					slHasAccess.add(false);
				else
					slHasAccess.add(true);
			}else{
				slHasAccess.add(true);
			}
		}
		return slHasAccess;
	}

	/**
	 * Decides whether to show Add Period Command or not
	 * Added:17-June-2010
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return boolean
	 * @throws Exception if operation fails
	 * @author IXE
	 */
	public boolean isAddPeriodAvailable(Context context,String args[]) throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		HashMap settingsMap = (HashMap) programMap.get("SETTINGS");
		String objectId = (String) programMap.get("objectId");
		DomainObject dObjProject = DomainObject.newInstance(context, objectId);
		
		String state = (String)dObjProject.getInfo(context, SELECT_CURRENT);
		
		String commandName = (settingsMap != null) ?(String)settingsMap.get("CmdName") : DomainConstants.EMPTY_STRING;
		if("PMCProjectBudgetAddPeriod".equalsIgnoreCase(commandName)){
			if(!(STATE_PROJECT_SPACE_CREATE.equals(state) || STATE_PROJECT_SPACE_ASSIGN.equals(state)||
				 STATE_PROJECT_SPACE_ACTIVE.equals(state) || STATE_PROJECT_SPACE_REVIEW.equals(state) ||
				 STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equals(state))){
				return false; 
			}
		}
		
		boolean bBudget = isBudgetCreated(context, args);
		if(bBudget){
			try{
				String strPreferredCurrency = emxProgramCentralUtilBase_mxJPO.getUserPreferenceCurrency(context, "emxProjectBudget.Error.NoDefaultCurrencyDefined");
			}catch(Exception e){
				return false;
			}
			String  selecteView = (String)programMap.get(BUDGETVIEWFILTER);

			boolean isAddCostItemAvailable = isAddCostItemAvailable(context,args);
			boolean isByPhase = isBudgetByPhase(context, args);
			boolean isFrozen = isCurrentStateFrozen(context,args,true); // if returned true then the state is not frozen

			if(isByPhase  == false){
				return false;
			}else{
				if(isFrozen == true){
					return true;
				}else{
					if(!planView.equals(selecteView) && !actualView.equals(selecteView) ){
						return true;
					}else
						return false;
				}
			}
		}else{
			return false;
		}
	}



	/**
	 * This method is used to get Range values for Report Filter
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 	 *
	 * @returns HashMap containing Range values for Assignee role
	 * @throws Exception if the operation fails
	 * @author IXE
	 */
	public Map getBudgetReportFilterRange(Context context, String[] args) throws Exception
	{
		Map programMap = (Map)JPO.unpackArgs(args);
		Map mpRequestMap = (HashMap)programMap.get("requestMap");
		String strProjObjId = (String) mpRequestMap.get("objectId");
		Map rangeMap = new HashMap();
		i18nNow loc = new i18nNow();
		String strLanguage=context.getSession().getLanguage();

		String SELECT_ATTRIBUTE = "attribute["+ATTRIBUTE_COST_INTERVAL+"]";
		String strType= TYPE_BUDGET;
		String strRelationshipType = RELATIONSHIP_PROJECT_FINANCIAL_ITEM;

		String whereClause = "" ;
		String strCostInterval = "";
		StringList busSelect = new StringList();
		busSelect.add(DomainConstants.SELECT_ID);
		busSelect.add(DomainConstants.SELECT_NAME);
		busSelect.add(SELECT_ATTRIBUTE);
		StringList relSelect = new StringList();
		DomainObject dom = DomainObject.newInstance(context, strProjObjId);
		MapList mlBudget = dom.getRelatedObjects(
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

		Map mapBudgetInfo = null;
		for (Iterator iterBudget = mlBudget.iterator(); iterBudget .hasNext();)
		{
			mapBudgetInfo = (Map) iterBudget.next();
			strCostInterval = (String)mapBudgetInfo.get(SELECT_ATTRIBUTE);
		}

		String views="";
		String sPolicyFinancial = PropertyUtil.getSchemaProperty(context,"policy_FinancialItems");
		String sPlanFrozenStateName = PropertyUtil.getSchemaProperty(context,"policy",sPolicyFinancial,"state_PlanFrozen");
		views = (String)loc.GetString("emxProgramCentralStringResource", strLanguage, "emxProgramCentral.Budget.Report");

		//Added:15-DEC-10:hp5:R211:PRG: IR-071964V6R2012:No Pull-down Menu in Expense Report-Ja
		String Weekly = (String)loc.GetString("emxProgramCentralStringResource", strLanguage, "emxProgramCentral.ProjectBenefit.Interval.Weekly");
		String Monthly = (String)loc.GetString("emxProgramCentralStringResource", strLanguage, "emxProgramCentral.ProjectBenefit.Interval.Monthly");
		String Quarterly = (String)loc.GetString("emxProgramCentralStringResource", strLanguage, "emxProgramCentral.ProjectBenefit.Interval.Quarterly");
		String Annually = (String)loc.GetString("emxProgramCentralStringResource", strLanguage, "emxProgramCentral.ProjectBenefit.Interval.Annually");


		//String viewValues[]=views.split(",");
		String viewValues[] = {Weekly,Monthly,Quarterly,Annually};
		//End:hp5

		StringList slDisplayList = new StringList();
		StringList slOriginalList = new StringList();
		int listSize=viewValues.length;

		if(MONTHLY.equals(strCostInterval)){
			for(int i = 0;i<listSize; i++) {
				//Modified:26-May-11:ms9:R212:PRG: IR-104173V6R2012x
				if(!Weekly.equals(viewValues[i])){
					//End:26-May-11:ms9:R212:PRG: IR-104173V6R2012x
					slDisplayList.add(viewValues[i]);
				}
			}
			slOriginalList.add("Monthly");
			slOriginalList.add("Quarterly");
			slOriginalList.add("Annually");
		}else if(QUARTERLY.equals(strCostInterval)){
			for(int i = 0;i<listSize; i++) {
				//Modified:26-May-11:ms9:R212:PRG: IR-104173V6R2012x
				if(!Weekly.equals(viewValues[i]) && !Monthly.equals(viewValues[i])){
					//End:26-May-11:ms9:R212:PRG: IR-104173V6R2012x
					slDisplayList.add(viewValues[i]);
				}
			}
			slOriginalList.add("Quarterly");
			slOriginalList.add("Annually");
		}
		else{
			for(int i = 0;i<listSize; i++) {
				slDisplayList.add(viewValues[i]);
			}
			slOriginalList.add("Weekly");
			slOriginalList.add("Monthly");
			slOriginalList.add("Quarterly");
			slOriginalList.add("Annually");
		}
		rangeMap.put("field_choices",slOriginalList);
		rangeMap.put("field_display_choices",slDisplayList);
		return  rangeMap;
	}


	public Map getBudgetFiscalReportFilterRange(Context context, String[] args) throws Exception
	{
		Map programMap = (Map)JPO.unpackArgs(args);
		Map mpRequestMap = (HashMap)programMap.get("requestMap");
		String strProjObjId = (String) mpRequestMap.get("objectId");
		Map rangeMap = new HashMap();
		i18nNow loc = new i18nNow();
		String strLanguage=context.getSession().getLanguage();

		String SELECT_ATTRIBUTE_START_DATE = "attribute["+ATTRIBUTE_COST_INTERVAL_START_DATE+"]";
		String SELECT_ATTRIBUTE_FINISH_DATE = "attribute["+ATTRIBUTE_COST_INTERVAL_END_DATE+"]";
		String strType= TYPE_BUDGET;
		String strRelationshipType = RELATIONSHIP_PROJECT_FINANCIAL_ITEM;
		String strTableName = ProgramCentralConstants.EMPTY_STRING;
		strTableName = (String)mpRequestMap.get("table");
		String strSelectedYear = ProgramCentralConstants.EMPTY_STRING;
		if("PMCProjectBudgetPlanTable".equals(strTableName)){
			strSelectedYear = (String)mpRequestMap.get("PMCProjectBudgetFiscalYearFilter");
		}
		if("PMCProjectBudgetReportTable".equals(strTableName)){
			strSelectedYear = (String)mpRequestMap.get("PMCProjectBudgetReportFiscalYearFilter");
		}

		String whereClause = ProgramCentralConstants.EMPTY_STRING;
		String strStartDate = ProgramCentralConstants.EMPTY_STRING;
		String strEndDate = ProgramCentralConstants.EMPTY_STRING;
		StringList busSelect = new StringList();
		busSelect.add(DomainConstants.SELECT_ID);
		busSelect.add(DomainConstants.SELECT_NAME);
		busSelect.add(SELECT_ATTRIBUTE_START_DATE);
		busSelect.add(SELECT_ATTRIBUTE_FINISH_DATE);
		StringList relSelect = new StringList();
		DomainObject dom = DomainObject.newInstance(context, strProjObjId);
		MapList mlBudget = dom.getRelatedObjects(
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

		Map mapBudgetInfo = null;
		StringList slDisplayList = new StringList();

		for (Iterator iterBudget = mlBudget.iterator(); iterBudget .hasNext();)
		{
			mapBudgetInfo = (Map) iterBudget.next();
			strStartDate = (String)mapBudgetInfo.get(SELECT_ATTRIBUTE_START_DATE);
			strEndDate = (String)mapBudgetInfo.get(SELECT_ATTRIBUTE_FINISH_DATE);

			Date dtBudgetStartDate = eMatrixDateFormat.getJavaDate(strStartDate);
			Date dtBudgetFinishDate = eMatrixDateFormat.getJavaDate(strEndDate);
			int nStartYear = Financials.getFiscalYear(dtBudgetStartDate);

			if(ProgramCentralUtil.isNotNullString(strSelectedYear)){
				slDisplayList.add(strSelectedYear);
			}else{
				slDisplayList.add(String.valueOf(nStartYear));
			}

			int nFiscalStartYear = Financials.getFiscalYear(dtBudgetStartDate);
			int nFiscalEndYear = Financials.getFiscalYear(dtBudgetFinishDate);

			while(nFiscalStartYear<=nFiscalEndYear){
				String strYear = String.valueOf(nFiscalStartYear);
				if(!slDisplayList.contains(strYear)){
					slDisplayList.add(strYear);
				}
				nFiscalStartYear++;
			}
		}
		rangeMap.put("field_choices",slDisplayList);
		rangeMap.put("field_display_choices",slDisplayList);

		return  rangeMap;
	}



	/**
	 * Returns the table data for the Budget Report page.
	 *
	 * @param context Matrix Context object
	 * @param args String array
	 * @return vector holding mentioned values
	 * @throws Exception if operation fails
	 * @author IXE
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTableProjectBudgetReportData(Context context, String[] args) throws Exception
	{
		try
		{
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList mlRequests =  getTableProjectBudgetReportData(context,args,true);
			return mlRequests ;
		}
		catch (Exception exp)
		{
			exp.printStackTrace();
			throw new MatrixException(exp);
		}
	}

	/**
	 * This method is used to get Range values for Views Filter
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 	 *
	 * @returns HashMap containing Range values for Assignee role
	 * @throws Exception if the operation fails	 *
	 * @author IXE
	 */
	public Map getBudgetViewsReportFilterRange(Context context, String[] args) throws Exception
	{
		Map programMap = (Map)JPO.unpackArgs(args);
		Map mpRequestMap = (HashMap)programMap.get("requestMap");
		String strProjObjId = (String) mpRequestMap.get("objectId");
		Map rangeMap = new HashMap();
		i18nNow loc = new i18nNow();
		String strLanguage=context.getSession().getLanguage();
		String strTypePattern=TYPE_BUDGET;
		//String strQuery="print bus "+strProjObjId+" select from["+RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"| to.type.kindof["+strTypePattern+"]==TRUE].to.current dump";
		String strQuery="print bus $1 select $2 dump";
		String strCurrent=MqlUtil.mqlCommand(context, strQuery,strProjObjId,"from["+RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"| to.type.kindof["+strTypePattern+"]==TRUE].to.current");
		String views="";
		String sPolicyFinancial = PropertyUtil.getSchemaProperty(context,"policy_FinancialItems");
		String sPlanFrozenStateName = PropertyUtil.getSchemaProperty(context,"policy",sPolicyFinancial,"state_PlanFrozen");
		//      views = (String)loc.GetString("emxProgramCentralStringResource", strLanguage, "emxProgramCentral.ProjectBenefitOrBudget.Views");
		//
		//  	String viewValues[]=views.split(",");
		//
		StringList slDisplayList = new StringList();
		StringList slOriginalList = new StringList();
		//  	int listSize=viewValues.length;
		//  	for(int i = 0;i<listSize; i++) {
		//  			slDisplayList.add(viewValues[i]);
		//  			slOriginalList.add(viewValues[i]);
		//  	}

		String selectedView = (String) mpRequestMap.get(BUDGETVIEWREPORTFILTER);

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
			if(!(selectedView!=null && selectedView.equals(views)))
			{
				slDisplayList.add(views);
				slOriginalList.add("Actual View");
			}
		}
		else
		{
			views = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.ProjectBenefitOrBudget.ViewPlan", strLanguage);
			slDisplayList.add(views);
			slOriginalList.add("Plan View");
		}

		rangeMap.put("field_choices",slOriginalList);
		rangeMap.put("field_display_choices",slDisplayList);
		return  rangeMap;

	}

	public boolean isTimeLineReportFilterAvailable(Context context,String args[]) throws Exception
	{
		if(isBudgetByPhase(context,args)){
			return false;
		}else
			return true;
	}

	/**
	 * Checks if the context user has access to Budget command in Project Categories toolbar.
	 * @param context the ENOVIA <code>Context</code> user.
	 * @param args the arrays that holds request parameters.
	 * @return true if context user has access to the command
	 * @throws Exception if operations fails.
	 */
	public boolean hasAccessToCommand(Context context, String[] args)
			throws MatrixException
			{
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String) programMap.get("objectId");
			boolean isProjectVisible = isProjectVisible(context, objectId);
			boolean isBackgroundTaskActive = isBackgroundTaskActive(context, objectId);
			if(isProjectVisible && !isBackgroundTaskActive) 
				return true;
			return false;
		}catch(Exception e){
			throw new MatrixException(e);
		}
			}

	/**
	 * Checks if the user has access to Budget command in Project template category. 
	 * @param context the  Enovia <code>context</code> object
	 * @param args request arguments
	 * @return true is context user has access to view Budget command in Project Template category
	 * @throws Exception if operation fails.
	 */
	public boolean hasAccessToBudgetCommand(Context context, String[] args)
			throws Exception
			{
		boolean access = false;
		try {
			Person person = new Person(PersonUtil
					.getPersonObjectID(context));
			StringList slRole =person.getRoleAssignments(context);
			if(slRole!=null) {
				for(int i=0;i<slRole.size();i++) {
					String role = (String)slRole.get(i);
					if("role_ProjectLead".equals(role) || "role_ProjectAdministrator".equals(role)){
						access = true;
						break;
					}
				}
			}
			boolean isLoggedInAsAdmin =  ProgramCentralUtil.checkContextRole(context, ProgramCentralConstants.ROLE_PROJECT_ADMINISTRATOR);
			boolean isLoggedInAsLead  =  ProgramCentralUtil.checkContextRole(context, ProgramCentralConstants.ROLE_PROJECT_LEAD);
			if(isLoggedInAsAdmin || isLoggedInAsLead ){
				access = true;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return access;
			}

	/**
	 * When a Phase is deleted, delete all phase references from budget &
	 * Cost Items.
	 * Note: OBJECTID is required as method argument.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the Object Id
	 * @return 0 so that Matrix core actually deletes the phase
	 * @throws Exception if operation fails
	 */
	public int triggerDeletePhaseBudgetReferences(Context context, String[] args)
			throws Exception
			{
		// get values from args.
		String PhaseId = args[0];
		DomainObject dmoPhase = DomainObject.newInstance(context, PhaseId);
		if(dmoPhase.isKindOf(context, TYPE_PHASE)){
			String SELECT_BUDGET_ID = "to["+RELATIONSHIP_FINANCIAL_ITEMS+"].from.id";
			String SELECT_BUDGET_NAME = "to["+RELATIONSHIP_FINANCIAL_ITEMS+"].from.name";
			String SELECT_BUDGET_PLANNED_COST = "to["+RELATIONSHIP_FINANCIAL_ITEMS+"].from.attribute["+ATTRIBUTE_PLANNED_COST+"]";
			String SELECT_BUDGET_ESTIMATED_COST = "to["+RELATIONSHIP_FINANCIAL_ITEMS+"].from.attribute["+ATTRIBUTE_ESTIMATED_COST+"]";
			String SELECT_BUDGET_ACTUAL_COST = "to["+RELATIONSHIP_FINANCIAL_ITEMS+"].from.attribute["+ATTRIBUTE_ACTUAL_COST+"]";
			String SELECT_COST_ITEM_PLANNED_COST = "attribute[" + ATTRIBUTE_PLANNED_COST  + "]";
			String SELECT_COST_ITEM_ESTIMATED_COST = "attribute[" + ATTRIBUTE_ESTIMATED_COST  + "]";
			String SELECT_COST_ITEM_ACTUAL_COST = "attribute[" + ATTRIBUTE_ACTUAL_COST  + "]";

			StringList slBusSelect = new StringList();
			slBusSelect.add(SELECT_ID);
			slBusSelect.add(SELECT_NAME);
			slBusSelect.add(SELECT_BUDGET_ID);
			slBusSelect.add(SELECT_BUDGET_NAME);
			slBusSelect.add(SELECT_BUDGET_PLANNED_COST);
			slBusSelect.add(SELECT_BUDGET_ESTIMATED_COST);
			slBusSelect.add(SELECT_BUDGET_ACTUAL_COST);
			slBusSelect.add(SELECT_COST_ITEM_PLANNED_COST);
			slBusSelect.add(SELECT_COST_ITEM_ESTIMATED_COST);
			slBusSelect.add(SELECT_COST_ITEM_ACTUAL_COST);

			String strRelationship = RELATIONSHIP_COST_ITEM_INTERVAL;
			String strType = TYPE_COST_ITEM;
			StringList slRelSelect = new StringList();
			String SELECT_PLANNED_COST =  DomainRelationship.getAttributeSelect(ATTRIBUTE_PLANNED_COST);
			String SELECT_ESTIMATED_COST =DomainRelationship.getAttributeSelect(ATTRIBUTE_ESTIMATED_COST);
			String SELECT_ACTUAL_COST = DomainRelationship.getAttributeSelect(ATTRIBUTE_ACTUAL_COST);

			slRelSelect.add(SELECT_PLANNED_COST);
			slRelSelect.add(SELECT_ESTIMATED_COST);
			slRelSelect.add(SELECT_ACTUAL_COST);

			MapList mlData = dmoPhase.getRelatedObjects(
					context,
					strRelationship,
					strType,
					slBusSelect,
					slRelSelect,
					true,
					false,
					(short)1,
					null,
					DomainConstants.EMPTY_STRING,
					0);
			DomainObject domBudget = null;
			DomainObject domCostItem = null;

			Double dblBudgetPlannedCost = 0d;
			Double dblBudgetEstimatedCost = 0d;
			Double dblBudgetActualCost =0d;

			Double dblCostItemPlannedCost = 0d;
			Double dblCostItemEstimatedCost = 0d;
			Double dblCostItemActualCost = 0d;

			Double dblCostItemIntervalPlannedCost = 0d;
			Double dblCostItemIntervalEstimatedCost =0d;
			Double dblCostItemIntervalActualCost = 0d;


			if(null != mlData){
				for (Iterator itr=mlData.iterator();itr.hasNext();) {
					Map objectMap = (Map) itr.next();
					String strBudgetID = (String)objectMap.get(SELECT_BUDGET_ID);
					String strCostItemID = (String)objectMap.get(SELECT_ID);

					domBudget = DomainObject.newInstance(context, strBudgetID);
					domCostItem = DomainObject.newInstance(context, strCostItemID);

					dblBudgetPlannedCost = Task.parseToDouble((String)objectMap.get(SELECT_BUDGET_PLANNED_COST));
					dblBudgetEstimatedCost = Task.parseToDouble((String)objectMap.get(SELECT_BUDGET_ESTIMATED_COST));
					dblBudgetActualCost = Task.parseToDouble((String)objectMap.get(SELECT_BUDGET_ACTUAL_COST));

					dblCostItemPlannedCost = Task.parseToDouble((String)objectMap.get(SELECT_COST_ITEM_PLANNED_COST));
					dblCostItemEstimatedCost = Task.parseToDouble((String)objectMap.get(SELECT_COST_ITEM_ESTIMATED_COST));
					dblCostItemActualCost = Task.parseToDouble((String)objectMap.get(SELECT_COST_ITEM_ACTUAL_COST));


					double dbltempCostItemIntervalPlannedCost = 0;
					dbltempCostItemIntervalPlannedCost = Task.parseToDouble((String)objectMap.get(SELECT_PLANNED_COST));
					double dbltempCostItemIntervalEstimatedCost  = 0;
					dbltempCostItemIntervalEstimatedCost = Task.parseToDouble((String)objectMap.get(SELECT_ESTIMATED_COST));
					double dbltempCostItemIntervalActualCost = 0;
					dbltempCostItemIntervalActualCost = Task.parseToDouble((String)objectMap.get(SELECT_ACTUAL_COST));

					dblCostItemIntervalPlannedCost = dblCostItemIntervalPlannedCost + dbltempCostItemIntervalPlannedCost ;
					dblCostItemIntervalEstimatedCost = dblCostItemIntervalEstimatedCost + dbltempCostItemIntervalEstimatedCost ;
					dblCostItemIntervalActualCost = dblCostItemIntervalActualCost + dbltempCostItemIntervalActualCost ;

					Map attributes = new HashMap();
					attributes.put(ATTRIBUTE_PLANNED_COST,String.valueOf(dblCostItemPlannedCost-dbltempCostItemIntervalPlannedCost));
					attributes.put(ATTRIBUTE_ESTIMATED_COST,String.valueOf(dblCostItemEstimatedCost-dbltempCostItemIntervalEstimatedCost));
					attributes.put(ATTRIBUTE_ACTUAL_COST,String.valueOf(dblCostItemActualCost-dbltempCostItemIntervalActualCost));

					domCostItem.setAttributeValues(context, attributes);
				}
			}


			if(null != domBudget && !"null".equals(domBudget) && !"".equals(domBudget)){
				Map Budgetattributes = new HashMap();
				Budgetattributes.put(ATTRIBUTE_PLANNED_COST,String.valueOf(dblBudgetPlannedCost-dblCostItemIntervalPlannedCost));
				Budgetattributes.put(ATTRIBUTE_ESTIMATED_COST,String.valueOf(dblBudgetEstimatedCost-dblCostItemIntervalEstimatedCost));
				Budgetattributes.put(ATTRIBUTE_ACTUAL_COST,String.valueOf(dblBudgetActualCost-dblCostItemIntervalActualCost));

				domBudget.setAttributeValues(context, Budgetattributes);
			}
		}

		return 0;
			}

	/**
	 * Returns the name of Budget & related Cost category items to be displayed in table. 
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args String array holding request parameters.
	 * @return a collection of HTML formated names of Budget and related Cost category items.
	 * @throws Exception if operation fails.
	 */
	public Vector getColumnBudgetNameData(Context context,String[] args) throws Exception
	{
		return getNameColumnDataForFinancialItems(context, args, true); //true parameters denotes budget object	
	}


	//Actual Transactions: Update Transaction Description 
	/**
	 * Updates actual transaction description.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args The request arguments, 
	 * @throws MatrixException if operation fails
	 */
	public void updateTransactionDescription(Context context, String[] args) throws MatrixException{
		try{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");
			HashMap requestMap = (HashMap)programMap.get("requestMap");
			String description = (String)paramMap.get("New Value");
			String transactionId = (String)paramMap.get("objectId");
			DomainObject objActualTransaction = DomainObject.newInstance(context, transactionId);
			objActualTransaction.setDescription(context, description);
		}
		catch(Exception e){
			throw new MatrixException(e);
		}
	}

	//Actual Transactions: Update Transaction PON 
	/**
	 * Updates actual transaction purchase order number.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args The request arguments, 
	 * @throws MatrixException if operation fails
	 */
	public void updatePurchaseOrderNumber(Context context, String[] args) throws MatrixException{
		try{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");
			HashMap requestMap = (HashMap)programMap.get("requestMap");
			String strNewPON = (String)paramMap.get("New Value");
			String transactionId = (String)paramMap.get("objectId");
			DomainObject objActualTransaction = DomainObject.newInstance(context, transactionId);
			objActualTransaction.setAttributeValue(context, ProgramCentralConstants.ATTRIBUTE_TRANSACTION_PON, strNewPON);
		}
		catch(Exception e){
			throw new MatrixException(e);
		}
	}

	//Actual Transactions Summary Table Edit Access Function
	/**
	 * Checks if Actual Transaction is editable.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args The request arguments, 
	 * @return List of boolean values, a true indicates transaction row is editable.
	 * @throws MatrixException if operation fails
	 */
	public StringList isActualTransactionEditable(Context context, String[] args) throws MatrixException{ 
		StringList rowEditChecks  = new StringList();
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			rowEditChecks = new StringList(objectList.size());
			StringList slTransactionIds = new StringList(objectList.size());
			String transactionId = new String();
			for(int index=0; index<objectList.size() ; index++){
				Map objectMap = (Map)objectList.get(index);
				transactionId = (String)objectMap.get(DomainConstants.SELECT_ID);
				slTransactionIds.add(transactionId); 
			}
			String[] arrTransactionIds = new String[slTransactionIds.size()];
			slTransactionIds.toArray(arrTransactionIds);

			String ctxUser = context.getUser();
			StringList selectables = new StringList();
			selectables.add(ProgramCentralConstants.SELECT_OWNER);
			MapList transactionOwnerInfo = new MapList();
			try{
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				transactionOwnerInfo = DomainObject.getInfo(context, arrTransactionIds, selectables);
			}finally{
				ContextUtil.popContext(context);
			}

			for (Iterator iterator = transactionOwnerInfo.iterator(); iterator.hasNext();) {
				Map transactionOwnerInfoMap = (Map) iterator.next();
				String strTransactionOwner = (String) transactionOwnerInfoMap.get(ProgramCentralConstants.SELECT_OWNER);
				if(ctxUser.equals(strTransactionOwner)) 
					rowEditChecks.add(true); 
				else rowEditChecks.add(false);
			}
		} catch (Exception e) {
			throw new MatrixException(e);
		}
		return rowEditChecks;
	}
	//Actual Transactions summary getters
	//Transaction Name column getter
	/**
	 * Returns transaction names for the given transaction ids.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args The request arguments, 
	 * @return List of transaction names for given transactions
	 * @throws MatrixException if operation fails
	 */      
	public StringList getTransactionName(Context context, String[] args) throws MatrixException{ 
		StringList slTransactionNames = new StringList();
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			slTransactionNames = new StringList(objectList.size());
			StringList slTransactionIds = new StringList(objectList.size());
			String transactionId = new String();
			for(int index=0; index<objectList.size() ; index++){
				Map objectMap = (Map)objectList.get(index);
				transactionId = (String)objectMap.get(DomainConstants.SELECT_ID);
				slTransactionIds.add(transactionId); 
			}
			String[] arrTransactionIds = new String[slTransactionIds.size()];
			slTransactionIds.toArray(arrTransactionIds);

			StringList selectables = new StringList();
			selectables.add(ProgramCentralConstants.SELECT_NAME);
			MapList transactionNameInfo = new MapList();
			try{
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				transactionNameInfo = DomainObject.getInfo(context, arrTransactionIds, selectables);
			}finally{
				ContextUtil.popContext(context);
			}

			for (Iterator iterator = transactionNameInfo.iterator(); iterator.hasNext();) {
				Map transactionNameInfoMap = (Map) iterator.next();
				String strTransactionName = (String) transactionNameInfoMap.get(ProgramCentralConstants.SELECT_NAME);
				slTransactionNames.add(strTransactionName);
			}
		} catch (Exception e) {
			throw new MatrixException(e);
		}
		return slTransactionNames;
	}

	//Transaction Date column getter
	/**
	 * Returns date of transaction for the given transaction ids.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args The request arguments, 
	 * @return List of transaction dates for given transactions
	 * @throws MatrixException if operation fails
	 */      
	public StringList getTransactionDate(Context context, String[] args) throws MatrixException{
		StringList slTransactionDates = new StringList();
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			slTransactionDates = new StringList(objectList.size());
			StringList slTransactionIds = new StringList(objectList.size());
			String transactionId = new String();
			for(int index=0; index<objectList.size() ; index++){
				Map objectMap = (Map)objectList.get(index);
				transactionId = (String)objectMap.get(DomainConstants.SELECT_ID);
				slTransactionIds.add(transactionId); 
			}

			String[] arrTransactionIds = new String[slTransactionIds.size()];
			slTransactionIds.toArray(arrTransactionIds);

			StringList selectables = new StringList();
			selectables.add(ProgramCentralConstants.SELECT_TRANSACTION_DATE);
			MapList transactionDateInfo = new MapList();
			try{
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				transactionDateInfo = DomainObject.getInfo(context, arrTransactionIds, selectables);
			}finally{
				ContextUtil.popContext(context);
			}

			for (Iterator iterator = transactionDateInfo.iterator(); iterator.hasNext();) {
				Map transactionDateInfoMap = (Map) iterator.next();
				String strTransactionDate = (String) transactionDateInfoMap.get(ProgramCentralConstants.SELECT_TRANSACTION_DATE);
				slTransactionDates.add(strTransactionDate);
			}
		} catch (Exception e) {
			throw new MatrixException(e);
		}
		return slTransactionDates;
	}

	//Transaction Descrition column getter
	/**
	 * Returns transaction description for the given transaction ids.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args The request arguments, 
	 * @return List of transaction description for given transactions
	 * @throws MatrixException if operation fails
	 */      
	public StringList getTransactionDescription(Context context, String[] args) throws MatrixException{ 
		StringList slTransactionDesc = new StringList();
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			slTransactionDesc = new StringList(objectList.size());
			StringList slTransactionIds = new StringList(objectList.size());
			String transactionId = new String();
			for(int index=0; index<objectList.size() ; index++){
				Map objectMap = (Map)objectList.get(index);
				transactionId = (String)objectMap.get(DomainConstants.SELECT_ID);
				slTransactionIds.add(transactionId); 
			}

			String[] arrTransactionIds = new String[slTransactionIds.size()];
			slTransactionIds.toArray(arrTransactionIds);

			StringList selectables = new StringList();
			selectables.add(ProgramCentralConstants.SELECT_DESCRIPTION);
			MapList transactionDescInfo = new MapList();
			try{
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				transactionDescInfo = DomainObject.getInfo(context, arrTransactionIds, selectables);
			}finally{
				ContextUtil.popContext(context);
			}

			for (Iterator iterator = transactionDescInfo.iterator(); iterator.hasNext();) {
				Map transactionDescInfoMap = (Map) iterator.next();
				String strTransactionDesc = (String) transactionDescInfoMap.get(ProgramCentralConstants.SELECT_DESCRIPTION);
				slTransactionDesc.add(strTransactionDesc);
			}
		} catch (Exception e) {
			throw new MatrixException(e);
		}
		return slTransactionDesc;
	}

	//Transaction Phase column getter
	/**
	 * Returns transaction phase for the given transaction ids.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args The request arguments, 
	 * @return List of transaction phase for given transactions
	 * @throws MatrixException if operation fails
	 */      
	public StringList getPhaseConnectedToTransaction(Context context, String[] args) throws MatrixException{  
		StringList slTransactionPhase = new StringList();
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			slTransactionPhase = new StringList(objectList.size());
			StringList slTransactionIds = new StringList(objectList.size());
			String transactionId = new String();
			for(int index=0; index<objectList.size() ; index++){
				Map objectMap = (Map)objectList.get(index);
				transactionId = (String)objectMap.get(DomainConstants.SELECT_ID);
				slTransactionIds.add(transactionId); 
			}

			String[] arrTransactionIds = new String[slTransactionIds.size()];
			slTransactionIds.toArray(arrTransactionIds);

			StringList selectables = new StringList();
			selectables.add("from[" + ProgramCentralConstants.RELATIONSHIP_ACTUAL_TRANSACTION_PHASE + "].to.name");
			MapList transactionPhaseInfo = new MapList();
			try{
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				transactionPhaseInfo = DomainObject.getInfo(context, arrTransactionIds, selectables);
			}finally{
				ContextUtil.popContext(context);
			}

			for (Iterator iterator = transactionPhaseInfo.iterator(); iterator.hasNext();) {
				Map transactionPhaseInfoMap = (Map) iterator.next();
				String strTransactionPhase = (String) transactionPhaseInfoMap.get("from[" + ProgramCentralConstants.RELATIONSHIP_ACTUAL_TRANSACTION_PHASE + "].to.name");
				slTransactionPhase.add(strTransactionPhase);
			}
		} catch (Exception e) {
			throw new MatrixException(e);
		}
		return slTransactionPhase;
	}

	//Transaction Cost Item  column getter
	/**
	 * Returns cost items connected to for the given transaction ids.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args The request arguments, 
	 * @return List of cost items connected to given transactions.
	 * @throws MatrixException if operation fails
	 */      
	public StringList getCostItemConnecteToTransaction(Context context, String[] args) throws MatrixException{  
		StringList slTransactionItem = new StringList();
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			slTransactionItem = new StringList(objectList.size());
			StringList slTransactionIds = new StringList(objectList.size());
			String transactionId = new String();
			for(int index=0; index<objectList.size() ; index++){
				Map objectMap = (Map)objectList.get(index);
				transactionId = (String)objectMap.get(DomainConstants.SELECT_ID);
				slTransactionIds.add(transactionId); 
			}

			String[] arrTransactionIds = new String[slTransactionIds.size()];
			slTransactionIds.toArray(arrTransactionIds);

			StringList selectables = new StringList();
			selectables.add("from[" + ProgramCentralConstants.RELATIONSHIP_ACTUAL_TRANSACTION_ITEM + "].to.name");
			MapList transactionItemInfo = new MapList();
			try{
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				transactionItemInfo = DomainObject.getInfo(context, arrTransactionIds, selectables);
			}finally{
				ContextUtil.popContext(context);
			}

			for (Iterator iterator = transactionItemInfo.iterator(); iterator.hasNext();) {
				Map transactionItemInfoMap = (Map) iterator.next();
				String strTransactionPhase = (String) transactionItemInfoMap.get("from[" + ProgramCentralConstants.RELATIONSHIP_ACTUAL_TRANSACTION_ITEM + "].to.name");
				if(strTransactionPhase.contains(" "))
					strTransactionPhase = strTransactionPhase.replaceAll(" ", "_");
				String strTransactionPhaseKey = "emxProgramCentral.Common.".concat(strTransactionPhase);
				strTransactionPhase = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
						strTransactionPhaseKey, context.getSession().getLanguage()); 
				slTransactionItem.add(strTransactionPhase);
			}
		} catch (Exception e) {
			throw new MatrixException(e);
		}
		return slTransactionItem;
	}

	//Transaction Purchased Order number column getter
	/**
	 * Returns transaction purchased order number for the given transaction ids.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args The request arguments, 
	 * @return List of transaction PONs for given transactions
	 * @throws MatrixException if operation fails
	 */      
	public StringList getTransactionPON(Context context, String[] args) throws MatrixException{  
		StringList slTransactionPON = new StringList();
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			slTransactionPON = new StringList(objectList.size());
			StringList slTransactionIds = new StringList(objectList.size());
			String transactionId = new String();
			for(int index=0; index<objectList.size() ; index++){
				Map objectMap = (Map)objectList.get(index);
				transactionId = (String)objectMap.get(DomainConstants.SELECT_ID);
				slTransactionIds.add(transactionId); 
			}

			String[] arrTransactionIds = new String[slTransactionIds.size()];
			slTransactionIds.toArray(arrTransactionIds);

			StringList selectables = new StringList();
			selectables.add(ProgramCentralConstants.SELECT_TRANSACTION_PON);
			MapList transactionPONInfo = new MapList();
			try{
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				transactionPONInfo = DomainObject.getInfo(context, arrTransactionIds, selectables);
			}finally{
				ContextUtil.popContext(context);
			}

			for (Iterator iterator = transactionPONInfo.iterator(); iterator.hasNext();) {
				Map transactionPONInfoMap = (Map) iterator.next();
				String strTransactionPON = (String) transactionPONInfoMap.get(ProgramCentralConstants.SELECT_TRANSACTION_PON);
				slTransactionPON.add(strTransactionPON);
			}
		} catch (Exception e) {
			throw new MatrixException(e);
		}
		return slTransactionPON;
	}

	/**
	 * Returns transaction amount for the given transaction ids.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args The request arguments, 
	 * @return List of transaction amount for given transactions
	 * @throws MatrixException if operation fails
	 */      
	public StringList getTransactionValue(Context context, String[] args) throws MatrixException
	{
		StringList vTransactionList = new StringList();
		boolean isNoticeRequired = false;
		try {
			String STRING_NO_EXCHANGE_RATE_ASSUMING_1_RATE = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.CurrencyConvrsion.NoExchangeRateAssuming1Rate", context.getSession().getLanguage());
			Map programMap = (Map) JPO.unpackArgs(args);
			Map paramMap = (Map) programMap.get("paramList");
			MapList objectList = (MapList) programMap.get("objectList");
			String strSelectedCurrency = (String) paramMap.get("PMCActualTransactionCurrencyFilter");
			String strPreferredCurrency = PersonUtil.getCurrency(context);
			if(ProgramCentralUtil.isNullString(strSelectedCurrency)){
				strSelectedCurrency = strPreferredCurrency;
			}
			vTransactionList = new StringList(objectList.size());
			StringList slTransactionIds = new StringList(objectList.size());
			String transactionId = new String();
			for(int i=0; i<objectList.size() ; i++){
				Map objectMap = (Map)objectList.get(i);
				transactionId = (String)objectMap.get(DomainConstants.SELECT_ID);
				slTransactionIds.add(transactionId); 
			}
			String[] arrTransactionIds = new String[slTransactionIds.size()];
			slTransactionIds.toArray(arrTransactionIds);
			StringList selectables = new StringList();
			selectables.add(ProgramCentralConstants.SELECT_TRANSACTION_AMOUNT);
			selectables.add("attribute["+Financials.ATTRIBUTE_TRANSACTION_AMOUNT+"].inputunit");
			MapList transactionAmountInfo = new MapList();
			try{
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				transactionAmountInfo = DomainObject.getInfo(context, arrTransactionIds, selectables);
			}finally{
				ContextUtil.popContext(context);
			}
			for (Iterator iterator = transactionAmountInfo.iterator(); iterator.hasNext();) {
				Map transactionAmountInfoMap = (Map) iterator.next();
				String strTransactionAmount = (String) transactionAmountInfoMap.get(ProgramCentralConstants.SELECT_TRANSACTION_AMOUNT);
				String strTransactionAmountUnit = (String) transactionAmountInfoMap.get("attribute["+Financials.ATTRIBUTE_TRANSACTION_AMOUNT+"].inputunit");
				double dblTransactionAmount = Task.parseToDouble(strTransactionAmount);
				try{
					dblTransactionAmount = convertAmount(context, dblTransactionAmount, strTransactionAmountUnit, strSelectedCurrency, new Date(),true);
				}catch(Exception e){
					isNoticeRequired = true;
				}
				strTransactionAmount = Currency.format(context, strSelectedCurrency, dblTransactionAmount);
				vTransactionList.add(strTransactionAmount);
			}
			if(isNoticeRequired)
				emxContextUtilBase_mxJPO.mqlNotice(context, STRING_NO_EXCHANGE_RATE_ASSUMING_1_RATE);

		} catch (Exception e) {
			throw new MatrixException(e);
		}
		return vTransactionList;
	}

	/**
	 * Updates amount for an actual transaction
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args Array of required string parameters.
	 * @throws Exception if operation fails.
	 */      
	public void updateTransactionAmount(Context context,String[] args) throws Exception {
		try {   		  
			Map programMap = (Map) JPO.unpackArgs(args);
			Map paramMap = (Map) programMap.get("paramMap");
			Map requestMap = (Map) programMap.get("requestMap");
			String strProjectId = (String) requestMap.get("projectId");
			if(isBackgroundTaskActive(context, strProjectId)){
				getCurrencyModifyBackgroundTaskMessage(context, true);
				return;
			}
			String strObjectId = (String) paramMap.get("objectId");
			String strNewTransactionValue = (String) paramMap.get("New Value");
			//start IR-353060
			String currency = (String) requestMap.get("PMCActualTransactionCurrencyFilter");

			String sTemp = ProgramCentralConstants.EMPTY_STRING;
			for (int i = 0; i < strNewTransactionValue.length(); i++) {
				char c = strNewTransactionValue.charAt(i);
				if(Character.isDigit(c)||'.'== c ||','==c){
					sTemp = sTemp + c;
				}
			}
			strNewTransactionValue = sTemp.trim();
			strNewTransactionValue = strNewTransactionValue.replace(" ","");
			Locale currencyLocale = Currency.getCurrencyLocale(context,currency);
			NumberFormat nFormater = NumberFormat.getInstance(currencyLocale);
			Number numericValue =  nFormater.parse(strNewTransactionValue);
			Double num = numericValue.doubleValue();
			strNewTransactionValue = Double.toString(num);
			//end
			boolean showCurrencyConversionWarning = false;
			String strCurrencyConversionWarning = "";
			String strPreferredCurrency = Currency.getBaseCurrency(context,strProjectId);
			try{
				if(ProgramCentralUtil.isNullString(strNewTransactionValue)) strNewTransactionValue = "0";
				strNewTransactionValue = Currency.toBaseCurrency(context, strProjectId,strNewTransactionValue,true);
			}catch(MatrixException e){
				showCurrencyConversionWarning = true;
				strCurrencyConversionWarning = e.getMessage();
			}	
			//strNewTransactionValue = ""+${CLASS:emxProgramCentralUtilBase}.getNormalizedCurrencyValue(context,locale, strNewTransactionValue);
			DomainObject dmoObject = DomainObject.newInstance(context, strObjectId);
			dmoObject.setAttributeValue(context, Financials.ATTRIBUTE_TRANSACTION_AMOUNT, strNewTransactionValue+" "+strPreferredCurrency);
			if(showCurrencyConversionWarning)
				emxContextUtilBase_mxJPO.mqlNotice(context,strCurrencyConversionWarning);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Returns the internationalized name values for cost items object
	 * @param context Matrix Context object
	 * @param args String array
	 * @return vector holding mentioned values
	 * @throws Exception if operation fails
	 */
	public Vector getColumnCostItemIntName(Context context,String[] args) throws Exception
	{
		try
		{
			Vector vecResult = new Vector();
			String sLanguage = context.getSession().getLanguage();
			String key = "emxProgramCentral.Common.";

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");

			int i=0;
			StringList slSelectableList = new StringList();
			slSelectableList.add("from["+ProgramCentralConstants.RELATIONSHIP_ACTUAL_TRANSACTION_ITEM+"].to.name");

			if(!objectList.isEmpty())
			{
				String[] strId = new String[objectList.size()];
				for (Iterator iterator = objectList.iterator(); iterator.hasNext();)
				{
					Map mpObjectList =  (Map)iterator.next();
					strId[i] = (String)mpObjectList.get(SELECT_ID);
					i++;
				}
				MapList mlCostItem = DomainObject.getInfo(context, strId, slSelectableList);
				for (Iterator iterator = mlCostItem.iterator(); iterator.hasNext();) 
				{
					Map mpCostItem = (Map)iterator.next();
					String strCostItemName = (String)mpCostItem.get("from["+ProgramCentralConstants.RELATIONSHIP_ACTUAL_TRANSACTION_ITEM+"].to.name");
					if(strCostItemName.contains(" "))
					{
						strCostItemName = strCostItemName.replace(" ", "_");
					}
					String strConvertedCostItemName = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							key+strCostItemName, sLanguage);
					vecResult.add(strConvertedCostItemName);
				}
			}
			return vecResult;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new MatrixException(ex);
		}
	}
	//End 01-June-2011:PRG:MS9:IR-090276V6R2012x
	/**
	 * getRangeForBudgetIntervals - This Method will generate a list of valid Budget Cost Intervals intervals
	 *
	 * @param context the eMatrix
	 * @param args holds the following input arguments:
	 * @return HashMap of "interval" values.
	 * @throws Exception if the operation fails
	 * @since 2012x for IR-082851V6R2012x
	 * @author NZF
	 */

	public HashMap getRangeForBudgetIntervals(Context context, String[] args)
			throws Exception
			{
		try 
		{

			Map programMap = (Map) JPO.unpackArgs(args);
			Map paramMap = (Map) programMap.get("paramMap");

			String strLanguage = context.getLocale().getLanguage();
			StringList slValue = new StringList();
			StringList slDisplay = new StringList();
			HashMap map = new HashMap();

			String strObjectId = (String) paramMap.get("objectId");
			boolean isPhasePresentInProject = isHavingPhases(context, strObjectId);

			AttributeType atrInterval = new AttributeType(ProgramCentralConstants.ATTRIBUTE_COST_INTERVAL);
			atrInterval.open(context);
			StringList strList = atrInterval.getChoices(context);    
			atrInterval.close(context);

			for(int i=0; i<strList.size();i++)
			{
				String strIntervalRange = (String)strList.get(i);
				if(isPhasePresentInProject){
					slValue.add(strIntervalRange);
					slDisplay.add(i18nNow.getRangeI18NString(ProgramCentralConstants.ATTRIBUTE_COST_INTERVAL, strIntervalRange, strLanguage));	
				}else if(!strIntervalRange.contains(ProgramCentralConstants.TYPE_PHASE)){
					slValue.add(strIntervalRange);
					slDisplay.add(i18nNow.getRangeI18NString(ProgramCentralConstants.ATTRIBUTE_COST_INTERVAL, strIntervalRange, strLanguage));
				}
			}

			map.put("field_choices", slValue);
			map.put("field_display_choices", slDisplay);

			return  map;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			throw e;           
		}
			}

	/**
	 * Access check on Enable Edit feature on Budget table
	 * @param context the ENOVIA Context object
	 * @param args 
	 * @return 
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.PreProcessCallable
	public Map preProcessCheckForEdit (Context context, String[] args) throws Exception{
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) inputMap.get("paramMap");
		HashMap tableData = (HashMap) inputMap.get("tableData");
		MapList objectList = (MapList) tableData.get("ObjectList");
		String strObjectId = (String) paramMap.get("objectId");
		HashMap returnMap = null;

		if(ProgramCentralUtil.isNullString(strObjectId))
			return null;

		DomainObject object = DomainObject.newInstance(context, strObjectId);
		if(object.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_SPACE)){
			StringList financialIds =  object.getInfoList(context, SELECT_BUDGET_ID);
			for (Iterator iterator = financialIds.iterator(); iterator.hasNext();) {
				String financialId = (String) iterator.next();
				object = DomainObject.newInstance(context, financialId);
				if(object.isKindOf(context, ProgramCentralConstants.TYPE_BUDGET)){
					break;
				}
			}
		}
		boolean isEditingAllowed = object.checkAccess(context, (short) AccessConstants.cModify);
		if(isEditingAllowed){
			returnMap = new HashMap(2);
			returnMap.put("Action","Continue");
			returnMap.put("ObjectList",objectList);
		} else {
			returnMap = new HashMap(3);
			returnMap.put("Action","Stop");
			returnMap.put("Message","emxProgramCentral.WBS.NoEdit");
			returnMap.put("ObjectList",objectList);
		}
		return returnMap;
	}


	/**
	 * hasAccess - This method verifies the user's permission based on context with Budget.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * objectId   - String containing the objectId
	 * @throws Exception if the operation fails
	 */
	public boolean hasAccess(Context context, String[] args)throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) programMap.get("objectId");
		boolean access = false;
		try{
			if (ProgramCentralUtil.isNotNullString(objectId)){
				DomainObject dmoObject = DomainObject.newInstance(context, objectId);
				DomainObject dmoBudget = DomainObject.newInstance(context);
				boolean hasBudget = false;
				StringList financialIds =  dmoObject.getInfoList(context, SELECT_BUDGET_ID);
				for (Iterator iterator = financialIds.iterator(); iterator.hasNext();) {
					String financialId = (String) iterator.next();
					dmoBudget.setId(financialId);
					if(dmoBudget.isKindOf(context, ProgramCentralConstants.TYPE_BUDGET)){
						hasBudget = true;
						access = dmoBudget.checkAccess(context, (short) AccessConstants.cModify);
						break;
					}
				}
				if(!hasBudget){
					access = dmoObject.checkAccess(context, (short) AccessConstants.cModify);
				}
			}
		}
		catch (Exception ex){
			throw ex;
		}
		return access;
			}

	/**
	 * Check for context user has access to edit budget object. 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the information of object.
	 * @return boolean value.
	 * @throws Exception if operation fails.
	 */
	public boolean hasAccessToEditBudget(Context context,String[]args)throws Exception
	{
		boolean hasAccess = false;
		Map programMap = (Map) JPO.unpackArgs(args);
		String objectId = (String) programMap.get("objectId");
		DomainObject object = DomainObject.newInstance(context, objectId);

		if(object.isKindOf(context, TYPE_PROJECT_SPACE) || object.isKindOf(context, TYPE_PROJECT_TEMPLATE) ){
			ProjectSpace project = new ProjectSpace(objectId);
			StringList select = new StringList(1);
			select.add(ProgramCentralConstants.SELECT_ID);

			MapList budgetInfoList  = new MapList();
			try{
				ProgramCentralUtil.pushUserContext(context);
				budgetInfoList = FinancialItem.getFinancialBudgetOrBenefit(context, 
						project, 
						select, 
						ProgramCentralConstants.TYPE_BUDGET);					
			}finally{
				ProgramCentralUtil.popUserContext(context);
			}

			if(budgetInfoList != null && !budgetInfoList.isEmpty()){
				hasAccess = true;
			}
		}

		return hasAccess;
	}
}

