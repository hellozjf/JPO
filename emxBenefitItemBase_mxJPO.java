/* emxBenefitItemBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.11.2.2 Thu Dec  4 07:55:01 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.11.2.1 Thu Dec  4 01:53:11 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.11 Wed Oct 22 15:49:41 2008 przemek Experimental przemek $
 */

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import matrix.db.AccessConstants;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
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
import com.matrixone.apps.program.Currency;
import com.matrixone.apps.program.FinancialItem;
import com.matrixone.apps.program.FinancialTemplateCategory;
import com.matrixone.apps.program.Financials;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;

/**
 * The <code>emxFinancialItemBase</code> class represents the Financial Item
 * types functionality for the AEF.
 *
 * @version AEF 9.5.1.3 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxBenefitItemBase_mxJPO extends emxFinancialItemBase_mxJPO
{

	static final String SELECT_PROJECT_BENEFIT="from["+RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"].to.id";

	static final String SELECT_PROJECT_BENEFIT_INTERVAL=(new StringBuilder()).append("from[").append(RELATIONSHIP_PROJECT_FINANCIAL_ITEM).append("].to.attribute[").append(ATTRIBUTE_BENEFIT_INTERVAL).append("]").toString();

	static final String SELECT_BENEFIT_INTERVAL_INTERVAL_DATE=(new StringBuilder()).append("from[").append(RELATIONSHIP_BENEFIT_ITEM_INTERVAL).append("].attribute[").append(BenefitItemIntervalRelationship.ATTRIBUTE_INTERVAL_DATE).append("]").toString();

	static final String SELECT_BENEFIT_PLANNED_VALUE="from["+ RELATIONSHIP_FINANCIAL_ITEMS+"].to."+BenefitItem.SELECT_PLANNED_BENEFIT;

	/**
	 * Constructs a new emxBenefitItem JPO object.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String id
	 * @throws Exception if the operation fails
	 * @since AEF 9.5.1.3
	 */
	public emxBenefitItemBase_mxJPO (Context context, String[] args)
	throws Exception
	{
		// Call the super constructor
		super(context, args);

		if (args != null && args.length > 0)
		{
			setId(args[0]);
		}
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
	public boolean hasBenefitItem(Context context,DomainObject projectSpace)
	throws Exception
	{

		MapList mlBenefit = projectSpace.getRelatedObjects(
				context,
				RELATIONSHIP_PROJECT_FINANCIAL_ITEM,
				TYPE_BENEFIT,
				null,
				null,
				false,
				true,
				(short)1,
				null,
				DomainConstants.EMPTY_STRING);

		boolean hasBenefitItem = false;
		if(mlBenefit != null && !mlBenefit.isEmpty()){
			hasBenefitItem = true;
		}
		return hasBenefitItem;
	}


	/* This function verifies the benefit has been created and state is plan frozen
	 * for different commands in action toolbar
	 * @param context the eMatrix <code>Context</code> object
	 * @param projectspace holds the following input arguments:
	 * @return boolean true if access check passed else returns false
	 * @throws Exception if the operation fails
	 */
	public boolean hasBenefitItem(Context context,String args[])
	throws Exception
	{
		boolean hasBenefitItem = false;
		HashMap programMap = (HashMap) JPO.unpackArgs(args);

		String strRMBMenu=(String)programMap.get("RMBMenu");
		String viewForRMB="";
		if(strRMBMenu==null || strRMBMenu.length()==0)
			viewForRMB= (String) programMap.get(BENEFITVIEWFILTER);
		String objectId = (String) programMap.get("objectId");
		String view= (String) programMap.get(BENEFITVIEWFILTER);

		StringList objectSelects=new StringList();
		objectSelects.add(DomainConstants.SELECT_CURRENT);
		DomainObject dmoProjectSpace=DomainObject.newInstance(context, objectId);
		MapList mlBenefit = dmoProjectSpace.getRelatedObjects(
				context,
				RELATIONSHIP_PROJECT_FINANCIAL_ITEM,
				TYPE_BENEFIT,
				objectSelects,
				null,
				false,
				true,
				(short)1,
				null,
				DomainConstants.EMPTY_STRING,
				0);


		if(mlBenefit != null && !mlBenefit.isEmpty()){
			String strLanguage=context.getSession().getLanguage();
			String sPolicyFinancial = PropertyUtil.getSchemaProperty(context,"policy_FinancialItems");
			String sPlanFrozenStateName = PropertyUtil.getSchemaProperty(context,"policy",sPolicyFinancial,"state_PlanFrozen");
			String current=(String)((Map)mlBenefit.get(0)).get(DomainConstants.SELECT_CURRENT);

			if(current.equals(sPlanFrozenStateName) &&(viewForRMB==null || viewForRMB.equals(planView)))
				hasBenefitItem = false;
			else
				hasBenefitItem = true;

		}
		return hasBenefitItem;
	}

	/* This function verifies that user can edit the benefit note field or not
	 * @param context the eMatrix <code>Context</code> object
	 * @param projectspace holds the following input arguments:
	 * @return boolean true if access check passed else returns false
	 * @throws Exception if the operation fails
	 */

	public boolean hasEditBenefitAccess(Context context,String args[])
	throws Exception
	{
		boolean hasBenefitItem = false;
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) programMap.get("objectId");
		String view= (String) programMap.get(BENEFITVIEWFILTER);

		DomainObject dmoProjectSpace=DomainObject.newInstance(context, objectId);
		MapList mlBenefit = dmoProjectSpace.getRelatedObjects(
				context,
				RELATIONSHIP_PROJECT_FINANCIAL_ITEM,
				TYPE_BENEFIT,
				null,
				null,
				false,
				true,
				(short)1,
				null,
				DomainConstants.EMPTY_STRING);

		//Modified:Di7:17-08-2011:IR-078253V6R2012x:Start
		if(mlBenefit != null && !mlBenefit.isEmpty())
		{
			String strLanguage=context.getSession().getLanguage();
			hasBenefitItem = true;	       	   
		}
		//Modified:Di7:17-08-2011:IR-078253V6R2012x:End
		return hasBenefitItem;
	}


	/* Checks if "Create Benefit" command is available to context user.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args the String array with vital request parameters.
	 * @return a boolean variable. The true value indicates the availability of the create benefit command 
	 * for the context user.
	 * @throws Exception if the operation fails.
	 */
	public boolean checkBenefitCreateAccess(Context context, String args[])
	{
			ProjectSpace projectSpace = null;
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String) programMap.get("objectId");
				projectSpace = (ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
			projectSpace.setId(objectId);
			}catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			boolean access = false;
	          try{
				if(projectSpace.isKindOf(context, TYPE_PROJECT_SPACE) ){
					emxProgramCentralUtilBase_mxJPO.getUserPreferenceCurrency(context, "emxProjectBudget.Error.NoDefaultCurrencyDefined");
					StringList select = new StringList(1);
					select.add(ProgramCentralConstants.SELECT_ID);

					MapList benefitInfoList  = new MapList();
					try{
						ProgramCentralUtil.pushUserContext(context);
						benefitInfoList = FinancialItem.getFinancialBudgetOrBenefit(context, projectSpace, select, ProgramCentralConstants.TYPE_BENEFIT);					
					}finally{
						ProgramCentralUtil.popUserContext(context);
					}
					//If Project has budget, Create Budget command must not be allowed.
					if(null != benefitInfoList && benefitInfoList.size()!=0){
						return false;
	          }

					//If User has modify access on Project, Create command is allowed.
					String state = projectSpace.getInfo(context, ProgramCentralConstants.SELECT_CURRENT);
					if(ProgramCentralConstants.STATE_PROJECT_SPACE_CREATE.equals(state)
							|| ProgramCentralConstants.STATE_PROJECT_SPACE_ASSIGN.equals(state)
							|| ProgramCentralConstants.STATE_PROJECT_SPACE_ACTIVE.equals(state)){
						access = projectSpace.checkAccess(context, (short)AccessConstants.cModify);	
	            }
			}
				return access;
		}catch(Exception e){
				return access;
		}
	}


	/* This method shows the calendar for Time Line From date in the Create New Project Benefit Web form
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @return String 
	 * @throws Exception if the operation fails
	 */
	public String getTimeLineFromDate(Context context, String args[]) throws Exception
	{
		Map programMap = (Map)JPO.unpackArgs(args);
		Map requestMap = (Map) programMap.get("requestMap");
		String strObjId = "";

		strObjId = (String) requestMap.get("objectId");


		DomainObject dmoProject = DomainObject.newInstance(context, strObjId);

		strObjId = (String) requestMap.get("objectId");



		StringList slBusSelect = new StringList();
		slBusSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);

		Map mapObjInfo = dmoProject.getInfo(context, slBusSelect);


		String strStartDate = (String) mapObjInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
		Date dtStartDate = new Date(strStartDate);

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat
				.getEMatrixDateFormat(), Locale.US);
		String StartDate           = sdf.format(dtStartDate);
		String timezone                = (String) requestMap.get("timeZone");
		double dbTimeZone              = Double.parseDouble(timezone);

		Locale strLocale = (Locale)requestMap.get("localeObj");        
		// strStartDate     = eMatrixDateFormat.getFormattedDisplayDate(StartDate, dbTimeZone,Locale.getDefault()); 
		strStartDate     = eMatrixDateFormat.getFormattedDisplayDate(StartDate, dbTimeZone,strLocale); 

		final String ROW_NAME = "TimeLineFrom";
		StringBuffer strHTMLBuffer = new StringBuffer(64);
		// No xss encoding required for date
		strHTMLBuffer.append("<input type='text' readonly='readonly' size='' name='").append(XSSUtil.encodeForHTML(context,ROW_NAME)).append("' value=\'"
				+ strStartDate +"'/>");
		strHTMLBuffer.append("<a href=\"javascript:showCalendar('emxCreateForm', '").append(XSSUtil.encodeForHTML(context,ROW_NAME)).append("', '')\">");
		strHTMLBuffer.append("<img src='../common/images/iconSmallCalendar.gif' border='0' valign='absmiddle'/>");
		strHTMLBuffer.append("</a>");

		return strHTMLBuffer.toString();
	}


	/*
	 * This method shows the calendar for Time Line To date in the Create New Project Benefit Web form
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @return String 
	 * @throws Exception if the operation fails
	 */
	public String getTimeLineToDate(Context context, String args[]) throws Exception
	{

		Map programMap = (Map)JPO.unpackArgs(args);
		Map requestMap = (Map) programMap.get("requestMap");
		String strObjId = "";

		strObjId = (String) requestMap.get("objectId");


		DomainObject dmoProject = DomainObject.newInstance(context, strObjId);



		StringList slBusSelect = new StringList();
		slBusSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);

		Map mapObjInfo = dmoProject.getInfo(context, slBusSelect);
		String strEndDate = (String) mapObjInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE); 
		Date dtEndDate = new Date(strEndDate);

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat
				.getEMatrixDateFormat(), Locale.US);
		String EndDate           = sdf.format(dtEndDate);
		String timezone                = (String) requestMap.get("timeZone");
		double dbTimeZone              = Double.parseDouble(timezone);
		Locale strLocale = (Locale)requestMap.get("localeObj");  
		strEndDate   = eMatrixDateFormat.getFormattedDisplayDate(EndDate, dbTimeZone,strLocale);       

		final String ROW_NAME = "TimeLineTo";
		StringBuffer strHTMLBuffer = new StringBuffer(64);
		strHTMLBuffer.append("<input type='text' readonly='readonly' size='' name='").append(XSSUtil.encodeForHTML(context,ROW_NAME)).append("' value=\'"
				+ strEndDate +"'/>");
		strHTMLBuffer.append("<a href=\"javascript:showCalendar('emxCreateForm', '").append(XSSUtil.encodeForHTML(context,ROW_NAME)).append("', '')\">");
		strHTMLBuffer.append("<img src='../common/images/iconSmallCalendar.gif' border='0' valign='absmiddle'/>");
		strHTMLBuffer.append("</a>");

		return strHTMLBuffer.toString();
	}

	/*
	 * This method shows the 3 intervals monthly/weekly/quarterly in the Create New Project Benefit Web form
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @return String 
	 * @throws Exception if the operation fails
	 */

	public String getTimeLineInterval(Context context, String args[]) throws Exception
	{
		StringBuffer strHTMLBuffer = new StringBuffer();

		i18nNow loc = new i18nNow();
		String strLanguage=context.getSession().getLanguage();

		String intervalValue = (String)loc.GetString("emxProgramCentralStringResource", strLanguage, "emxProgramCentral.ProjectBenefit.Interval.Weekly");

		strHTMLBuffer.append("<input type='radio' name='TimeLineInterval' value='Weekly' />");
		strHTMLBuffer.append(XSSUtil.encodeForHTML(context,intervalValue));
		intervalValue = (String)loc.GetString("emxProgramCentralStringResource", strLanguage, "emxProgramCentral.ProjectBenefit.Interval.Monthly");
		strHTMLBuffer.append("<input type='radio' name='TimeLineInterval'  checked='true' value='Monthly' />");
		strHTMLBuffer.append(XSSUtil.encodeForHTML(context,intervalValue));
		intervalValue = (String)loc.GetString("emxProgramCentralStringResource", strLanguage, "emxProgramCentral.ProjectBenefit.Interval.Quarterly");
		strHTMLBuffer.append("<input type='radio' name='TimeLineInterval' value='Quarterly'  />");
		strHTMLBuffer.append(XSSUtil.encodeForHTML(context,intervalValue));




		return strHTMLBuffer.toString();

	}
	/**
	 * returns 1 of the timeline interval value -->monthly,weekly or quarterly in add period or delete period forms
	 * @param context
	 * @param args
	 * @return String value of benefit interval attribute of benefit
	 * @throws Exception
	 */
	public String getInterval(Context context, String args[]) throws Exception
	{

		StringBuffer strHTMLBuffer = new StringBuffer();
		Map programMap   = (HashMap) JPO.unpackArgs(args);
		Map requestMap = (Map)programMap.get("requestMap");
		String strTimeLineInterval="";
		i18nNow loc = new i18nNow();
		String strLanguage=context.getSession().getLanguage();
		String strBenefitInterval=SELECT_BENEFIT_INTERVAL;
		String strPSObjId=(String) requestMap.get("objectId");

		DomainObject projectSpaceDom = DomainObject.newInstance(context, strPSObjId);
		String relPattern=Financials.RELATIONSHIP_PROJECT_FINANCIAL_ITEM;
		String typePattern=TYPE_BENEFIT;
		StringList objectSelects=new StringList();
		objectSelects.add(strBenefitInterval);

		MapList financialItems=projectSpaceDom.getRelatedObjects(context,relPattern,typePattern,objectSelects,null,false,true,(short)1,null,null,0);


		Map financialItem=null;
		for (Iterator itrFI = financialItems.iterator(); itrFI.hasNext();)
		{
			financialItem = (Map) itrFI.next();
			strTimeLineInterval=(String)financialItem.get(strBenefitInterval);

		}
		if(strTimeLineInterval.equals(MONTHLY))
			strTimeLineInterval=(String)loc.GetString("emxProgramCentralStringResource", strLanguage, "emxProgramCentral.ProjectBenefit.Interval.Monthly");
		else if(strTimeLineInterval.equals(WEEKLY))
			strTimeLineInterval=(String)loc.GetString("emxProgramCentralStringResource", strLanguage, "emxProgramCentral.ProjectBenefit.Interval.Weekly");
		else if(strTimeLineInterval.equals(QUARTERLY))
			strTimeLineInterval=(String)loc.GetString("emxProgramCentralStringResource", strLanguage, "emxProgramCentral.ProjectBenefit.Interval.Quarterly");

		final String ROW_NAME = "TimeLineInterval";      
		strHTMLBuffer.append("<input type='text' readonly='readonly' size='' name='").append(ROW_NAME).append("' value=\'"
				+ strTimeLineInterval +"'/>");
		return strHTMLBuffer.toString();
	}
	/**
	 * Returns the max number of months/weeks/quarters for addperiod
	 * @param context
	 * @param args
	 * @return String value specified in properties file as add period limit
	 * @throws Exception
	 */

	public String getIntervalPeriods(Context context, String args[]) throws Exception
	{
		StringBuffer strHTMLBuffer = new StringBuffer();

		i18nNow loc = new i18nNow();
		String strLanguage=context.getSession().getLanguage();
		int addIntervalLimit = Integer.parseInt(EnoviaResourceBundle.getProperty(context,"emxProgramCentral.ProjectBenefit.IntervalPeriodsLimit"));

		strHTMLBuffer.append("<select name='addinterval'>");
		for (int j = 1; j <= addIntervalLimit; j++)
		{

			strHTMLBuffer.append("<option name='addinterval'  value=\'"+j+"'>"+j);
			strHTMLBuffer.append("</option>");

		}
		strHTMLBuffer.append("</select>");

		return strHTMLBuffer.toString();

	}

	/**
	 * Gets all the interval number in an HTML choice box depending upon the 
	 * 1. start and end date of the Benefit object and
	 * 2. benefit object interval type (monthly/weekly .. )  
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args used to store request parameters.
	 * @return an HTML string containing maximum number of intervals that a user 
	 * is allowed to delete in a project benefit.
	 * @throws Exception if Domain object operations fail.
	 */
	public String getIntervalPeriodsForDelete(Context context, String args[]) throws Exception
	{
		Map mapProgram = (Map) JPO.unpackArgs(args);
		Map requestMap = (Map)mapProgram.get("requestMap");
		String objectId = (String)requestMap.get("objectId");
		DomainObject dmoProjectSpace=DomainObject.newInstance(context, objectId);
		StringList busSelect = new StringList();
		busSelect.add(SELECT_ID);
		busSelect.add(SELECT_BENEFIT_INTERVAL_START_DATE);
		busSelect.add(SELECT_BENEFIT_INTERVAL_END_DATE);
		busSelect.add(SELECT_BENEFIT_INTERVAL);		 
		StringList relSelect = new StringList();
		MapList mlBenefit = dmoProjectSpace.getRelatedObjects(
				context,
				RELATIONSHIP_PROJECT_FINANCIAL_ITEM,
				TYPE_BENEFIT,
				busSelect,
				relSelect,
				false,
				true,
				(short)1,
				"",
				DomainConstants.EMPTY_STRING);
		Map benefitMap = (Map)mlBenefit.get(0);
		Date dtStartDate = eMatrixDateFormat.getJavaDate((String)benefitMap.get(SELECT_BENEFIT_INTERVAL_START_DATE));
		Date dtFinishDate = eMatrixDateFormat.getJavaDate((String)benefitMap.get(SELECT_BENEFIT_INTERVAL_END_DATE));
		String strTimeLineInterval=(String)benefitMap.get(SELECT_BENEFIT_INTERVAL);
		ArrayList dateList =  getIntervalDateList(dtStartDate,dtFinishDate,strTimeLineInterval);
		StringBuffer strHTMLBuffer = new StringBuffer();
		strHTMLBuffer.append("<select name='addinterval'>");
		for (int j = 1; j < dateList.size(); j++){
			strHTMLBuffer.append("<option name='addinterval'  value=\'"+j+"'>"+j);
			strHTMLBuffer.append("</option>");
		}
		strHTMLBuffer.append("</select>");
		return strHTMLBuffer.toString();
	}
	/**
	 * Start or End option for add and delete period
	 * @param context
	 * @param args
	 * @return String HTML code
	 * @throws Exception
	 */

	public String getPeriodTo(Context context, String args[]) throws Exception
	{
		StringBuffer strHTMLBuffer = new StringBuffer();

		i18nNow loc = new i18nNow();
		String strLanguage=context.getSession().getLanguage();
		String tlPeriodValuesFromStart = (String)loc.GetString("emxProgramCentralStringResource", strLanguage, "emxProgramCentral.Benefit.IntervalPeriodsFromStart");
		String tlPeriodValuesFromEnd = (String)loc.GetString("emxProgramCentralStringResource", strLanguage, "emxProgramCentral.Benefit.IntervalPeriodsFromEnd");
		String strStart = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.Budget.IntervalPeriodsFromStart");
		String strEnd = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.Budget.IntervalPeriodsFromEnd");
		strHTMLBuffer.append("<input type='radio' name='PeriodTo'  value=\'"+strStart+"' checked='true' />");
		strHTMLBuffer.append(XSSUtil.encodeForHTML(context,tlPeriodValuesFromStart));
		strHTMLBuffer.append("<input type='radio' name='PeriodTo'  value=\'"+strEnd+"'/>");
		strHTMLBuffer.append(XSSUtil.encodeForHTML(context,tlPeriodValuesFromEnd));

		return strHTMLBuffer.toString();
	}
	/**
	 * Generates required Benefit columns dynamically
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains requestMap 
	 * @return The MapList object containing definitions about new columns for showing Benefit Intervals
	 * @throws Exception if operation fails
	 */
	public MapList getDynamicBenefitColumn (Context context, String[] args) throws Exception
	{
		Map mapProgram = (Map) JPO.unpackArgs(args);

		Map requestMap = (Map)mapProgram.get("requestMap");

		// 
		// Following code gets business objects information 
		//
		String strObjectId = (String) requestMap.get("objectId");
		DomainObject dmoProject = DomainObject.newInstance(context, strObjectId);

		if(!dmoProject.isKindOf(context, TYPE_PROJECT_SPACE) && dmoProject.isKindOf(context, TYPE_FINANCIAL_ITEM)){
			strObjectId = (String)dmoProject.getInfo(context,"to["+RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"].from.id");
			dmoProject = DomainObject.newInstance(context, strObjectId);
		}

		Map mapDate = getMinMaxBenefitDates(context,strObjectId);
		String years = (String)requestMap.get(BENEFITINTERVALFILTER);
		String selecteView = (String)requestMap.get(BENEFITVIEWFILTER);
		String tableName=(String)requestMap.get("selectedTable");
		String strInterval = (String)requestMap.get(BENEFITINTERVALFILTER);
		int index = 0;
		int interval = 0;
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
		else{
			index = strInterval.lastIndexOf(" Year");
			strInterval = strInterval.substring(0,index);
			interval = Integer.parseInt(strInterval);
		}

		boolean istypeProjectSpace = false;
		istypeProjectSpace = dmoProject.isKindOf(context, TYPE_PROJECT_SPACE);
		String strType = dmoProject.getInfo(context, SELECT_TYPE);
		String strState = dmoProject.getInfo(context, SELECT_CURRENT);

		Calendar calStartDate = Calendar.getInstance();
		Calendar calFinishDate = Calendar.getInstance();  

		Map mapColumn = null;
		Map mapSettings = null;

		String strMonth = "";
		String strTypePattern=TYPE_BENEFIT;

		String sCommandStatement = "print bus $1 select $2 dump";
		strState =  MqlUtil.mqlCommand(context, sCommandStatement,strObjectId, "from["+RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"| to.type.kindof["+strTypePattern+"]==TRUE].to.current"); 

		sCommandStatement = "print bus $1 select $2 dump";
		String strTimeLineInterval =  MqlUtil.mqlCommand(context, sCommandStatement,strObjectId, "from["+RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"| to.type.kindof["+strTypePattern+"]==TRUE].to."+SELECT_BENEFIT_INTERVAL); 

		Date dtStartDate = null;
		Date dtFinishDate = null;

		Date dtChkStartDate = null;
		Date dtChkEndDate = null;

		MapList mlColumns = new MapList();

		dtStartDate = (Date)mapDate.get("TimeLineFromDate");
		dtFinishDate = (Date)mapDate.get("TimeLineToDate");

		String sPolicyFinancial = PropertyUtil.getSchemaProperty(context,"policy_FinancialItems");
		String sPlanFrozenStateName = PropertyUtil.getSchemaProperty(context,"policy",sPolicyFinancial,"state_PlanFrozen");
		int nyear=1;
		String strLanguage = ProgramCentralConstants.EMPTY_STRING;
		strLanguage = (String)requestMap.get("languageStr");

		if(ProgramCentralUtil.isNullString(strLanguage) || strLanguage.equals(ProgramCentralConstants.EMPTY_STRING))
			strLanguage=context.getSession().getLanguage();

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
		try{
			if(null != dtStartDate && null != dtFinishDate)
			{    		
				if(years!=null)
				{
					years=years.substring(0,years.indexOf("Year")-1);
					nyear=Integer.parseInt(years);
				}
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dtStartDate);

				ArrayList dateList =  getIntervalDateList(dtStartDate,dtFinishDate,strTimeLineInterval);


				int nTimeframe  = 0 ;
				int nFiscalDisplayYear = 0;
				int nYear = 0;
				Set<Integer> intervals = new HashSet<Integer>();
				for (Iterator itrTableRows = dateList.iterator(); itrTableRows.hasNext();)
				{
					Date dtIntervalDate=(Date)itrTableRows.next();
					Calendar intervalCalendar=Calendar.getInstance();
					intervalCalendar.setTime(dtIntervalDate);

					int day = intervalCalendar.get(Calendar.DAY_OF_MONTH);
					int month=intervalCalendar.get(Calendar.MONTH)+1;
					nFiscalDisplayYear = Financials.getFiscalYear(dtIntervalDate);
					intervals.add(nFiscalDisplayYear);
					nYear = intervalCalendar.get(Calendar.YEAR);
					if(intervals.size() > interval)
						break;
					mapColumn = new HashMap();

					nTimeframe = Financials.getFiscalMonthNumber(dtIntervalDate);
					strMonth = getMonthName(context,(dtIntervalDate.getMonth()+1));
					String strLabelDate = ProgramCentralConstants.EMPTY_STRING + dtIntervalDate.getDate()+ProgramCentralConstants.HYPHEN +
					strMonth+ ProgramCentralConstants.HYPHEN + (dtIntervalDate.getYear()+1900);

					if(strTimeLineInterval.equals(CostItem.MONTHLY))
					{
						mapColumn.put("label", strM + ProgramCentralConstants.HYPHEN + nTimeframe +" ("+strLabelDate+")");
					}
					else if ((CostItem.WEEKLY).equals(strTimeLineInterval))
					{
						nTimeframe = Financials.getFiscalWeekNumber(dtIntervalDate);
						mapColumn.put("label", strWK+ProgramCentralConstants.HYPHEN+nTimeframe +" ("+strLabelDate+")");
					}
					else if ((CostItem.QUARTERLY).equals(strTimeLineInterval))
					{
						nTimeframe = Financials.getFiscalQuarterNumber(dtIntervalDate);
						mapColumn.put("label", strQ+ProgramCentralConstants.HYPHEN+nTimeframe +" ("+strLabelDate+")");
					}
					else
					{
						nTimeframe= month;
						mapColumn.put("label", strFY+ProgramCentralConstants.HYPHEN+nFiscalDisplayYear);
					}
					mapColumn.put("name", month+ProgramCentralConstants.FSLASH+day+ProgramCentralConstants.FSLASH+nYear);	
					//Added:PRG:MS9:R212:26-Aug-2011:IR-088614V6R2012x End    					

					mapSettings = new HashMap();
					mapSettings.put("Registered Suite","ProgramCentral");
					mapSettings.put("program","emxBenefitItem");
					mapSettings.put("function","getColumnBenefitData");
					mapSettings.put("Column Type","program");

					//ADDED:06-July-2011:Di7:R212:PRG:IR-080929V6R2012x:START
					if(( planView.equalsIgnoreCase(selecteView)) && strState.equals(sPlanFrozenStateName))
						mapSettings.put("Editable","false");
					else{
						mapSettings.put("Editable","true");
					}
					mapSettings.put("Validate","validateCost");
					mapSettings.put("Export","true");
					mapSettings.put("Style Program","emxFinancialItem");
					mapSettings.put("Style Function","getBudgetRightAllignedStyleInfo");
					mapSettings.put("Sortable","false");
					mapSettings.put("Edit Access Function","getEditAccessToBenefitRows"); //Modified:DI7:PRG:R212:IR-080929V6R2012x:06-July-2011
					mapSettings.put("Edit Access Program","emxBenefitItem");//Modified:DI7:PRG:R212:IR-080929V6R2012x:06-July-2011
					mapSettings.put("Field Type","string");
					mapSettings.put("Update Program","emxBenefitItem");
					mapSettings.put("Update Function","updateDynamicColumnData");
					mapSettings.put("Group Header",strFY+ProgramCentralConstants.SPACE+nFiscalDisplayYear+ProgramCentralConstants.EMPTY_STRING);				
					mapColumn.put("settings", mapSettings);
					mlColumns.add(mapColumn);

				}
				mapColumn = new HashMap();
				mapColumn.put("name", "Total");
				//Modified:29-Mar-2011:hp5
				String strTotalLabel= EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.Common.Total", strLanguage);
				mapColumn.put("label",strTotalLabel );
				//Modified:29-Mar-2011:hp5
				mapSettings = new HashMap();

				mapSettings = new HashMap();
				mapSettings.put("Registered Suite","ProgramCentral");
				mapSettings.put("program","emxBenefitItem");
				mapSettings.put("function","getColumnBenefitData");
				mapSettings.put("Column Type","program");
				mapSettings.put("Style Program","emxFinancialItem");
				mapSettings.put("Style Function","getBudgetRightAllignedStyleInfo");
				mapSettings.put("Editable","false");
				mapSettings.put("Export","true");
				mapSettings.put("Field Type","string");
				mapSettings.put("Sortable","false");				
				mapColumn.put("settings", mapSettings);
				mlColumns.add(mapColumn);

				if((strState.equals(sPlanFrozenStateName) && !planView.equalsIgnoreCase(selecteView)))
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
					mapSettings.put("Style Program","emxFinancialItem");
					mapSettings.put("Style Function","getBudgetRightAllignedStyleInfo");
					String strAmount= EnoviaResourceBundle.getProperty(context, ProgramCentralConstants.PROGRAMCENTRAL, 
							"emxProgramCentral.Financial.VarianceAmount", strLanguage);
					mapColumn.put("label",strAmount);
					mapColumn.put("settings", addEstimateTableColumns(context,"Amount"));
					mlColumns.add(mapColumn);
					mapColumn=new HashMap();
					mapColumn.put("name","VariancePercent");
					mapColumn.put("label","%");
					mapColumn.put("settings", addEstimateTableColumns(context,"%"));
					mlColumns.add(mapColumn);		
				}				
			}   		
		}catch(Exception exp){
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
	 * Added:DI7:PRG:R212:06-July-2011:IR-080929V6R2012x:START
	 */
	public StringList getEditAccessToBenefitRows(Context context,String[] args) throws MatrixException{
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
	// Added:DI7:PRG:R212:06-July-2011:IR-080929V6R2012x:END
	protected Map addEstimateTableColumns(Context context,String columnLabel)throws Exception
	{
		Map mapSettings=new HashMap();
		mapSettings.put("Registered Suite","ProgramCentral");
		mapSettings.put("Column Type","program");
		mapSettings.put("Editable","false");
		mapSettings.put("Export","true");
		mapSettings.put("Field Type","string");
		mapSettings.put("Sortable","false");
		mapSettings.put("program","emxBenefitItem");
		mapSettings.put("function","getColumnBenefitData");
		if(columnLabel.equalsIgnoreCase("Amount") || columnLabel.equalsIgnoreCase("%") )
		{
			String strVariance= EnoviaResourceBundle.getProperty(context, ProgramCentralConstants.PROGRAMCENTRAL, 
					"emxProgramCentral.Financial.Variance", context.getSession().getLanguage());
			mapSettings.put("Group Header",strVariance);
		}

		return mapSettings;
	}


	protected Map getMinMaxBenefitDates(Context context,String strProjectId) throws Exception {
		try {
			Map mapDate = new HashMap();
			DomainObject dmoProject = DomainObject.newInstance(context, strProjectId);
			String busTypeSelect = SELECT_TYPE ;


			String strRelationshipType = "";
			String strType = "";
			boolean strFrom = false;
			boolean strTo = false;


			strRelationshipType = FinancialItem.RELATIONSHIP_PROJECT_FINANCIAL_ITEM;
			strType = FinancialItem.TYPE_BENEFIT;
			strTo = true;


			final String SELECT_ATTRIBUTE_BENEFIT_INTERVAL_START_DATE = FinancialItem.SELECT_BENEFIT_INTERVAL_START_DATE;
			final String SELECT_ATTRIBUTE_BENEFIT_INTERVAL_END_DATE  =  FinancialItem.SELECT_BENEFIT_INTERVAL_END_DATE;

			StringList busSelect = new StringList();
			busSelect.add(DomainConstants.SELECT_ID);

			busSelect.add(SELECT_ATTRIBUTE_BENEFIT_INTERVAL_START_DATE);
			busSelect.add(SELECT_ATTRIBUTE_BENEFIT_INTERVAL_END_DATE);


			StringList relSelect = new StringList();
			String whereClause = "" ;



			MapList mlRequests = dmoProject.getRelatedObjects(
					context,
					strRelationshipType,
					strType,
					busSelect,
					relSelect,
					strFrom,
					strTo,
					(short)1,
					whereClause,
					DomainConstants.EMPTY_STRING,
					0);


			Map mapRequestInfo = null;
			String strBenefitId = "";
			String strBenefitStartDate = "";
			String strBenefitEndDate = "";
			String strchkBenStartDate = "";
			String strchkBenEndDate = "";

			Date dtStartDate = null;
			Date dtEndDate = null;

			Date dtChkStartDate = null;
			Date dtChkEndDate = null;

			for (Iterator iterRequest = mlRequests.iterator(); iterRequest .hasNext();)
			{
				mapRequestInfo = (Map) iterRequest.next();
				strBenefitId = (String) mapRequestInfo.get(SELECT_ID);
				strBenefitStartDate = (String) mapRequestInfo.get(SELECT_ATTRIBUTE_BENEFIT_INTERVAL_START_DATE);
				strBenefitEndDate = (String) mapRequestInfo.get(SELECT_ATTRIBUTE_BENEFIT_INTERVAL_END_DATE);
				if(null == strchkBenStartDate  || "null" .equals(strchkBenStartDate) || "".equals(strchkBenStartDate)){
					strchkBenStartDate = strBenefitStartDate;
					dtChkStartDate =  eMatrixDateFormat.getJavaDate(strchkBenStartDate);
				}
				if(null == strchkBenEndDate  || "null" .equals(strchkBenEndDate) || "".equals(strchkBenEndDate)){
					strchkBenEndDate = strBenefitEndDate;
					dtChkEndDate =  eMatrixDateFormat.getJavaDate(strchkBenEndDate);
				}

				if(null != strchkBenStartDate  && !"null" .equals(strchkBenStartDate) && !"".equals(strchkBenStartDate)){

					dtStartDate = eMatrixDateFormat.getJavaDate(strBenefitStartDate);


					if(dtStartDate.before(dtChkStartDate)){
						dtChkStartDate = dtStartDate;
					}
				}

				if(null != strchkBenEndDate  && !"null" .equals(strchkBenEndDate) && !"".equals(strchkBenEndDate)){
					dtEndDate = eMatrixDateFormat.getJavaDate(strBenefitEndDate);

					if(dtEndDate.after(dtChkEndDate)){
						dtChkEndDate = dtEndDate;
					}
				}
			}
			mapDate.put("TimeLineFromDate",dtChkStartDate);
			mapDate.put("TimeLineToDate",dtChkEndDate);


			return mapDate;
		} 
		catch (Exception exp)
		{
			exp.printStackTrace();
			throw exp;
		}

	}

	/**
	 * Creates Financial Item
	 * Added:26-Apr-2010:s4e:R209 PRG:IR-031392
	 * End:26-Apr-2010:s4e:R209 PRG:IR-031392
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return void
	 * @throws Exception if operation fails
	 */

	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void createProjectBenefit(Context context, String[] args)  throws Exception 
	{
		try 
		{

			Map programMap = (Map) JPO.unpackArgs(args);
			Map requestMap = (Map) programMap.get("requestMap");
			Map paramMap = (Map) programMap.get("paramMap");

			String strProjectSpaceId = (String)requestMap.get("objectId");
			if(isBackgroundTaskActive(context, strProjectSpaceId)){
				getCurrencyModifyBackgroundTaskMessage(context, true);
				return;
			}
			String strBenefitStartDate = (String)requestMap.get("TimeLineFrom");
			String strBenefitEndDate = (String)requestMap.get("TimeLineTo");

			String strautoNameCheck = (String)requestMap.get("autoNameCheck");
			String strBenefitName = (String)requestMap.get("Name");
			String strLanguage=context.getSession().getLanguage();

			String strTimeInterval = (String)requestMap.get("TimeLineInterval");

			String strTimeZone = (String)requestMap.get("timeZone");
			double clientTimeZone = Double.parseDouble(strTimeZone);

			String strNotes = (String)requestMap.get("Notes");


			DomainObject dmoProjectSpace = DomainObject.newInstance(context,strProjectSpaceId);


			strBenefitStartDate = eMatrixDateFormat.getFormattedInputDate(context, strBenefitStartDate, clientTimeZone, (Locale)requestMap.get("localeObj"));
			strBenefitEndDate = eMatrixDateFormat.getFormattedInputDate(context, strBenefitEndDate, clientTimeZone, (Locale)requestMap.get("localeObj"));

			Date dtBenefitStartDate = eMatrixDateFormat.getJavaDate(strBenefitStartDate);
			Date dtBenefitFinishDate = eMatrixDateFormat.getJavaDate(strBenefitEndDate);

			Calendar calBenefitStartDate = Calendar.getInstance();
			Calendar calBenefitFinishDate = Calendar.getInstance();

			calBenefitStartDate.setTime(dtBenefitStartDate);
			calBenefitFinishDate.setTime(dtBenefitFinishDate);



			StringList slBusSelect = new StringList();
			slBusSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
			slBusSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);

			String strProjectStartDate = "";
			String strProjectFinishtDate = "";



			Map mapObjInfo = dmoProjectSpace.getInfo(context, slBusSelect);
			strProjectStartDate = (String) mapObjInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
			strProjectFinishtDate = (String) mapObjInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);

			Date dtProjectStartDate = eMatrixDateFormat.getJavaDate(strProjectStartDate);
			Date dtProjectFinishDate = eMatrixDateFormat.getJavaDate(strProjectFinishtDate);

			//Modified:26-Mar-2010:vf2:R209 PRG:IR-046754   
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

			calBenefitStartDate.set(Calendar.HOUR,0);
			calBenefitStartDate.set(Calendar.HOUR_OF_DAY,0);
			calBenefitStartDate.set(Calendar.MINUTE,0);
			calBenefitStartDate.set(Calendar.SECOND,0);

			calBenefitFinishDate.set(Calendar.HOUR,0);
			calBenefitFinishDate.set(Calendar.HOUR_OF_DAY,0);
			calBenefitFinishDate.set(Calendar.MINUTE,0);
			calBenefitFinishDate.set(Calendar.SECOND,0);            	

			if(dtBenefitFinishDate.before(dtBenefitStartDate)){
				strLanguage = context.getSession().getLanguage();
				String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.ProjectBenefit.TimeLineToDateGreaterThanTimeLineFromDate", strLanguage);
				MqlUtil.mqlCommand(context, "notice " + sErrMsg);
				throw new Exception(sErrMsg);            		
			}
			else if(((calBenefitStartDate.before(calStartDate))&&(calBenefitFinishDate.before(calStartDate))))            	
			{             		
				strLanguage = context.getSession().getLanguage();
				String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.ProjectBenefit.TimeLineFromDateOrToDateShouldBeWithinProjectPeriod", strLanguage);
				MqlUtil.mqlCommand(context, "notice " + sErrMsg);
				throw new Exception(sErrMsg);            		
			}            	           	
			String strBenefitId = (String)paramMap.get("newObjectId");
			DomainObject dmoProjectBenefit = DomainObject.newInstance(context,strBenefitId);

			String strPreferredCurrency = Currency.getBaseCurrency(context,strProjectSpaceId);
			HashMap mpBenefitAttributes=new HashMap();
			mpBenefitAttributes.put(ATTRIBUTE_BENEFIT_INTERVAL_START_DATE,strBenefitStartDate);
			mpBenefitAttributes.put(ATTRIBUTE_BENEFIT_INTERVAL_END_DATE,strBenefitEndDate);
			mpBenefitAttributes.put(ATTRIBUTE_BENEFIT_INTERVAL,strTimeInterval);
			mpBenefitAttributes.put(ATTRIBUTE_NOTES,strNotes);

			// Set benefit with default values and base currency.
			mpBenefitAttributes.put(ProgramCentralConstants.ATTRIBUTE_ACTUAL_BENEFIT,"0.0" + ProgramCentralConstants.SPACE + strPreferredCurrency);
			mpBenefitAttributes.put(ProgramCentralConstants.ATTRIBUTE_ESTIMATED_BENEFIT,"0.0" + ProgramCentralConstants.SPACE + strPreferredCurrency);
			mpBenefitAttributes.put(ProgramCentralConstants.ATTRIBUTE_PLANNED_BENEFIT,"0.0" + ProgramCentralConstants.SPACE + strPreferredCurrency);

			dmoProjectBenefit.setAttributeValues(context,mpBenefitAttributes);

			// set SOV before the connect possibly re-stamps the POV and we don't have access to do SOV
			DomainAccess.createObjectOwnership(context, strBenefitId, "", context.getUser()+"_PRJ", ProgramCentralConstants.PROJECT_ROLE_PROJECT_LEAD, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);

			DomainRelationship.connect(context,dmoProjectSpace,RELATIONSHIP_PROJECT_FINANCIAL_ITEM,dmoProjectBenefit);

			String SELECT_ACCESS_OBJECT = "to["+RELATIONSHIP_PROJECT_ACCESS_LIST + "].from.id";
			String strProjectAccessObject = (String)dmoProjectSpace.getInfo(context, SELECT_ACCESS_OBJECT);
			DomainObject dmoAccessObject =  DomainObject.newInstance(context, strProjectAccessObject);
			String strRelationship = RELATIONSHIP_PROJECT_ACCESS_KEY;
			DomainRelationship.connect(context,dmoAccessObject,strRelationship,dmoProjectBenefit);

			String relId = dmoProjectBenefit.getInfo(context, 
					"to[" + ProgramCentralConstants.RELATIONSHIP_PROJECT_FINANCIAL_ITEM + "].id");
			
		} 
		catch (Exception exp)
		{
			exp.printStackTrace();
		}
	}



	/**
	 * Returns the name of Benefit & related benefit category items to be displayed in table. 
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args String array holding request parameters.
	 * @return a collection of HTML formated names of Benefit and related Benefit category items.
	 * @throws Exception if operation fails.
	 */
	public Vector getColumnBenefitNameData(Context context,String[] args) throws Exception{
		return getNameColumnDataForFinancialItems(context, args, false);// false parameter denotes benefit
	}


	/**
	 * In resource requests table, for each expand operation the data of people associated with requests are returned.
	 * 
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTableExpandChildProjectBenefitData(Context context,String[] args) throws Exception
	{
		Map programMap = (HashMap) JPO.unpackArgs(args);
		String strObjecttId = (String) programMap.get("objectId");
		String selectedView = (String)programMap.get(BUDGETVIEWFILTER);      	
		MapList mlBenefit_Cost_ItemList = null;
		MapList mlCost_ItemList = null;
		DomainObject dmoObject = DomainObject.newInstance(context, strObjecttId);

		if(dmoObject.isKindOf(context, "Financials")){
			mlBenefit_Cost_ItemList = getTableExpandChildProjectBudgetBenefitData(context,args,false);
		}else{
			mlBenefit_Cost_ItemList = getTableProjectBudgetOrBenefitData(context,args,false);//This will return benefit connected to project

			if(mlBenefit_Cost_ItemList.size()>0)
			{
				Map mpBenefitData = (Map)mlBenefit_Cost_ItemList.get(0);
				programMap.put("objectId",(String)(mpBenefitData.get(DomainConstants.SELECT_ID)));
				String[] arrJPOArguments    = new String[1];
				arrJPOArguments = JPO.packArgs(programMap);
				mlCost_ItemList = getTableExpandChildProjectBudgetBenefitData(context,arrJPOArguments,false);// To get benefit items
				mlBenefit_Cost_ItemList.addAll(mlCost_ItemList);
			}
		}
		return mlBenefit_Cost_ItemList;
	}

	/**
	 * Gets Project benefit table data for Project Benefits
	 * 
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTableProjectBenefitData(Context context, String[] args) throws Exception
	{

		return getTableProjectBudgetOrBenefitData(context,args,false);
	}


	/**
	 * Gets the data for the dynamic columns for table "PMCProjectBenefitSummaryTable"
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception if operation fails
	 */
	public Vector getColumnBenefitData(Context context, String[] args)  throws Exception {
		Vector vecResult = new Vector();
		try {
			// Create result vector
			// Get object list information from packed arguments
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map columnMap = (Map) programMap.get("columnMap");
			Map paramList = (Map) programMap.get("paramList");
			String strCurrency = (String)paramList.get("PMCProjectBenefitCurrencyFilter");
			Locale locale = (Locale)paramList.get("localeObj");
			String strColumnName = (String) columnMap.get(SELECT_NAME);

			String currency="";

			currency= (String)paramList.get(BENEFITCURRENCYFILTER);

			Map mapObjectInfo = null;
			String strColumnValues = null;
			DomainObject dmoObj = null;
			for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
			{
				mapObjectInfo = (Map) itrTableRows.next();
				String strId = (String)mapObjectInfo.get(SELECT_ID);


				boolean isProject = false;
				if(null!=strId && !"null".equalsIgnoreCase(strId) && !"".equals(strId)){
					dmoObj = DomainObject.newInstance(context, strId);
					isProject = dmoObj.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE);
				}
				strColumnValues=(String)mapObjectInfo.get(strColumnName);
				if(null != currency && !"null".equals(currency) && !"".equals(currency) ){
					String strColUnit = "";
					strColUnit = (String)mapObjectInfo.get(strColumnName+"_Unit");
					if(null != strColUnit && !currency.equals(strColUnit)){
						double dblColumnValues = Double.parseDouble(strColumnValues);
						dblColumnValues = convertAmount(context, dblColumnValues, strColUnit, currency, new Date());
						strColumnValues = String.valueOf(dblColumnValues);
					}
				}
				Map mapDate = new HashMap();
				String strBudgetTimelineInterval = null;

				if(strColumnValues!=null && strColumnValues.equals("NA"))
					strColumnValues = strColumnValues;
				//vecResult.add(strColumnValues);
				else if(strColumnValues!=null)				
					strColumnValues = strColumnValues;
				//vecResult.add(strColumnValues);
				else
					strColumnValues = "0.0";
				//vecResult.add("0.0");

				//Get user preferred currency
				String strPrefferedCurrency = PersonUtil.getCurrency(context);
				if(null==strCurrency){
					strCurrency = strPrefferedCurrency;
				}
				//To avoid Currency Formatting for Variance.
				if(!"VariancePercent".equals(strColumnName)){
					double dblValue = Double.parseDouble(strColumnValues);
					strColumnValues = Currency.format (context, strCurrency, dblValue);
				}

				if(isProject){
					vecResult.add("");
				}else{
					vecResult.add(strColumnValues);
				}
			}

			return vecResult;


		} catch (Exception exp) {

			exp.printStackTrace();
			throw exp;
		}
	}

	/**
	 * This method updates the cost/benefit value for Budget/Benefit Table 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @throws Exception if operation fails
	 */  

	public void updateDynamicColumnData(Context context,String[]args) throws Exception 
	{
		updateDynamicColumnData(context,args,false);		
	}

	/**
	 * This method refreshes the benefit table after any action command operation
	 * @param context The matrix context object
	 * @param args The arguments
	 * @throws Exception if operation fails
	 */  

	@com.matrixone.apps.framework.ui.PostProcessCallable
	public HashMap postProcessRefresh (Context context, String[] args) throws Exception
	{
		// unpack the incoming arguments
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);


		HashMap returnMap = new HashMap(1);
		returnMap.put("Action","refresh");
		return returnMap;

	}
	/**
	 * This method updates the cost/benefit value for Budget/Benefit Table 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @throws Exception if operation fails
	 */  
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void addPeriod (Context context, String[] args) throws Exception
	{
		addPeriod(context,args,false);
	}

	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void deletePeriod (Context context, String[] args) throws Exception
	{
		deletePeriod(context,args,false);
	}


	public void addBenefitItem(matrix.db.Context context, String[] args) throws Exception
	{
		HashMap methodargs = (HashMap)JPO.unpackArgs(args);
		String[] selectedCategories=(String[])methodargs.get("benefitNames");
		FinancialTemplateCategory financialTemplateCategory=new FinancialTemplateCategory();

		DomainObject dmoProjectSpace=DomainObject.newInstance(context, (String)methodargs.get("objectId"));


		//String[] strArr1=benefitNames.split(";");

		HashMap intervalMap = new HashMap();
		HashMap map = new HashMap();
		BenefitItem benefitItem=new BenefitItem();
		FinancialItem financialItem=new FinancialItem();

		financialItem.setId(dmoProjectSpace.getInfo(context,SELECT_PROJECT_BENEFIT));

		//benefitItem.create(context,"Incremental Sales", "Financial Items", financialItem, "", map, intervalMap);

		String strBenefitNames="";
		for(int i = 0; i < selectedCategories.length; i++) {
			if(selectedCategories[i].indexOf('|') == -1){ // this indicates that the item is a category, not a benefit item
				// in which case you want to ensure that its items are added
				MapList financialTemplateList = new MapList();
				StringList busSelects = new StringList();
				String parentCategoryId = null;
				java.util.HashMap benefitCategoriesMap = new java.util.HashMap();
				java.util.HashMap benefitCategoriesMapID = new java.util.HashMap();

				//Get the Benefit Categories
				busSelects.add(financialTemplateCategory.SELECT_ID);
				busSelects.add(financialTemplateCategory.SELECT_NAME);
				financialTemplateList = financialTemplateCategory.getBenefitCategories(context, 1, busSelects, null);
				Iterator mainCategoryItr = financialTemplateList.iterator();
				while(mainCategoryItr.hasNext())
				{
					Map current = (Map) mainCategoryItr.next();
					if(((String)current.get(financialTemplateCategory.SELECT_NAME)).equals(selectedCategories[i])){
						financialTemplateCategory.setId((String)current.get(financialTemplateCategory.SELECT_ID));
						Iterator subCategoryItr = financialTemplateCategory.getSubCategories(context,1,busSelects,null).iterator();
						StringList subCategoryList = new StringList();
						while(subCategoryItr.hasNext()){
							Map subCategoryMap = (Map) subCategoryItr.next();
							String benefitName=(String)subCategoryMap.get(financialTemplateCategory.SELECT_NAME);
							strBenefitNames+=benefitName+"|"+selectedCategories[i]+";";

							benefitItem.create(context,benefitName,POLICY_FINANCIAL_ITEMS , financialItem, "", map, intervalMap);

						}
						// eliminate duplicate possibilities by incrementing to the next category or item
						int comparator = i;
						while(i<(selectedCategories.length-1) && selectedCategories[i+1].indexOf("|"+selectedCategories[comparator])!=-1 ){
							i++;
						}
						break; // end looping through categories once you've found the one you're looking for
					}
				}
			} else {


				strBenefitNames=selectedCategories[i].substring(0,selectedCategories[i].indexOf("|"));
				benefitItem.create(context,strBenefitNames,POLICY_FINANCIAL_ITEMS, financialItem, "", map, intervalMap);

				// strBenefitNames += selectedCategories[i] + ";";

			} // end else
		} // end for

	}


	/**
	 * Gets the month name for displaying in the Benefit Table column for dynamic column value
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


	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTableBenefitCategoryItemData(Context context,String[] args) throws Exception
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
			mlBasicCategories = FTC.getBenefitCategories(context,1, busSelects, null);
			return mlBasicCategories;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new MatrixException(ex);
		}

	}



	/**
    /* This method returns the benefit items of the benefit categories for Add Benefit Item command
	 * when user expands the benefit category in the budget summary table.  
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getExpandTableBenefitCategoryItemData(Context context,String[] args) throws Exception
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
			String strTypePattern = TYPE_BENEFIT;
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
					TYPE_BENEFIT_ITEM, // type filter.
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
					TYPE_FINANCIAL_BENEFIT_CATEGORY, // type filter.
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
					int index = SelectedCostRev.indexOf(ProgramCentralConstants.HYPHEN);
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


	/**
     /* Check whether Delete Period command will be shown to user or not 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */	
	public boolean isRemovePeriodAvailable (Context context, String[] args) throws Exception
	{
		Map mapProgram = (Map) JPO.unpackArgs(args);
		String  selecteView = (String)mapProgram.get(BENEFITVIEWFILTER);

		//    	if(!actualView.equals(selecteView)){
		//    		if(isCurrentStateFrozen(context,args,false)){
		//    			return isRemovePeriodAvailable(context,args,false);
		//    		}else 
		//    			if(!isCurrentStateFrozen(context,args,false) && !planView.equals(selecteView)) //isCurrentStateFrozen returns false if state frozen
		//    			{
		//    				return isRemovePeriodAvailable(context,args,false);
		//    			}
		//    			else
		//    				return false;
		//    	}else{
		//    		return false;
		//    	}

		boolean isFrozen = isCurrentStateFrozen(context,args,false); // if returned true then the state is not frozen
		if(isFrozen){
			return isRemovePeriodAvailable(context,args,false);
		}else{
			return false;
		}
	}


	/**
	 * Checks if Add Period command is available to the user on the basis of 
	 * state of the benefit object and the view of the table(estimate/actual/plan). 
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args the array string with all requested data.
	 * @return a boolean value. If true, then command Add Period Item can be accessible.
	 * @throws Exception if operation fails.
	 */	
	public boolean isAddPeriodAvailable (Context context, String[] args) throws Exception
	{
		return isAddBenefitItemAvailable(context, args);
	}


	/**
	 * Checks if Add Benefit Item command is available to the user on the basis of 
	 * state of the benefit object and the view of the table(estimate/actual/plan). 
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args the array string with all requested data.
	 * @return a boolean value. If true, then command Add Benefit Item can be accessible.
	 * @throws Exception if operation fails.
	 */	
	public boolean isAddBenefitItemAvailable (Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) programMap.get("objectId");
		String isRMB = (String) programMap.get("isRMB");
		if(null != isRMB && "true".equalsIgnoreCase(isRMB.trim())){
			String sRmbTableRowId = (String) programMap.get("rmbTableRowId");
			Map sBenefitItemRowId = ProgramCentralUtil.parseTableRowId(context, sRmbTableRowId);
			objectId = (String)sBenefitItemRowId.get("objectId");
		}
		String view= (String) programMap.get(BENEFITVIEWFILTER);
		boolean isBenefit = false;
		boolean isFrozen = isCurrentStateFrozen(context,args,false); // false value indicates Benefit is in Frozen state.
		if (ProgramCentralUtil.isNullString(view)){
			if(!isFrozen)
				view = estimateView;
			else
				view = planView;
		}
		DomainObject dmoProjectSpace=DomainObject.newInstance(context, objectId);
		if(null != isRMB && "true".equalsIgnoreCase(isRMB.trim())){
			StringList slSelectable = new StringList();  			
			slSelectable.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			slSelectable.addElement(ProgramCentralConstants.SELECT_IS_BENEFIT);
			slSelectable.addElement(ProgramCentralConstants.SELECT_IS_FINANCIAL_BENEFIT_CATEGORY);
			Map mObjectInfo = dmoProjectSpace.getInfo(context, slSelectable);
			String sIsProjectSpace = (String)mObjectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			String sIsBenefit = (String)mObjectInfo.get(ProgramCentralConstants.SELECT_IS_BENEFIT);
			String sIsFinancialBenefitCategory = (String)mObjectInfo.get(ProgramCentralConstants.SELECT_IS_FINANCIAL_BENEFIT_CATEGORY);
  			if("TRUE".equalsIgnoreCase(sIsProjectSpace))
  				return false;
  			else if("TRUE".equalsIgnoreCase(sIsBenefit) || "TRUE".equalsIgnoreCase(sIsFinancialBenefitCategory))
  				return true;
		}
		// If benefit financial item is present then only Add Benefit Item should must comes into picture. 
		if(hasBenefitItem(context, dmoProjectSpace)){
			isBenefit = true;
			if(!isFrozen){
				// In freeze state command must be available only in Estimate view
				if(estimateView.equalsIgnoreCase(view))
					isBenefit = true;
				else
					isBenefit = false;
			}
		}
		return isBenefit;
	}                

	/**
	 * This method is used to get Range values for Views Filter
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 	 *        
	 * @returns HashMap containing Range values for Assignee role 
	 * @throws Exception if the operation fails	 * 
	 */
	public Map getBenefitOrBudgetViewsFilterRange(Context context, String[] args) throws Exception
	{
		return getBenefitOrBudgetViewsFilterRange(context,args,false);
	}

	/**
	 * This method is used to get Range values for Interval Filter
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 	 *        
	 * @returns HashMap containing Range values for Assignee role 
	 * @throws Exception if the operation fails	 * 
	 */
	public Map getBenefitOrBudgetYearRange(Context context, String[] args) throws Exception
	{
		return getBenefitOrBudgetYearRange(context,args,false);

	}

	/**
	 * Checks the state of Benefit object and decides the visibility of the command Freeze Benefit.
	 * If state is Frozen then command will not available to user.  
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args the string array that stores request data.
	 * @return a boolean value. True value denotes that state is not frozen and command remains avaliable
	 * to the user.
	 * @throws Exception if operations fail.
	 */
	public boolean isCurrentStateFrozen(Context context,String args[]) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) programMap.get("objectId");
		String isRMB = (String)programMap.get("isRMB");
		String rmbId = (Boolean.valueOf(isRMB))?(String)programMap.get("RMBID") : objectId;
		DomainObject dmoObject = DomainObject.newInstance(context, rmbId);
		String type = dmoObject.getInfo(context, SELECT_TYPE);

		if(!(TYPE_PROJECT_SPACE.equals(type) || TYPE_BENEFIT_ITEM.equals(type))){
			return false;
		}

		DomainObject dmoProject = DomainObject.newInstance(context, objectId);
		if(hasBenefitItem(context, dmoProject)){
			return isCurrentStateFrozen(context,args,false);
		}
		else
			return false;    
	}

	/**
	 * Checks if the Delete Benefit command is available for the user. This is checked on the basis of 
	 * user access and Benefit state. If state is frozen or user does not have Lead or Owner access then 
	 * command will not be shown on the page.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args the String array that stores request data.
	 * @return a boolean value. True value indicates that Delete Benefit command is visible to the users.  
	 * @throws Exception if operation fails.
	 */
	public boolean checkFinDeleteAccess(Context context,String args[]) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) programMap.get("objectId");
		String isRMB = (String)programMap.get("isRMB");
		String rmbId = (Boolean.valueOf(isRMB))?(String)programMap.get("RMBID") : objectId;
		DomainObject dmoObject = DomainObject.newInstance(context, rmbId);
		String type = dmoObject.getInfo(context, SELECT_TYPE);

		if(!(TYPE_PROJECT_SPACE.equals(type) || TYPE_BENEFIT.equals(type))){
			return false;
		}

		if(!isCurrentStateFrozen(context, args, false))
			return false;
		return checkFinDeleteAccess(context,args,false);
	}

	/**
	 * Checks if the command Benefit in the Financial channel is available for context user.
	 * @param context the <code>ENOVIA</code> context.
	 * @param args the Array of strings with vital request data.
	 * @return a boolean value. The true value indicates the availability of the benefit command in 
	 * Project Financial Channel for the context user.
	 */
	public boolean isProjectBenefitCommandAccessible(Context context,String[] args) throws MatrixException{
		boolean isCommandAccessible = false;
		HashMap programMap;
		try {
			programMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String) programMap.get("objectId");
			isCommandAccessible = FinancialItem.isProjectVisible(context, objectId);
		} catch (Exception e) {
			throw new MatrixException(e);
		}
		return isCommandAccessible;
	}

	/**
	 * Checks if the context user has enough access to project to deal with different benefit operations.
	 * To see or perform operations like Create benefit, edit benefit, add benefit item,
	 * the context user must be a Project owner or, a Project Lead or Financial Reviewer of the project. 
	 * @param context the <code>ENOVIA</code> context.
	 * @param args the Array of strings with vital request data. 
	 * @return a boolean value. If true, the different operations like create benefit or edit benefit
	 * can be accessible by the context user.  
	 * @throws MatrixException 
	 */
	boolean isContextAllowedForBenefitOperations(Context context, String[] args) throws MatrixException {
		return FinancialItem.isContextAllowedForFinancialOperations(context, args);
	}

	/**
	 * Access check on Enable Edit feature on Benefit table
	 * @param context the ENOVIA Context object
	 * @param args 
	 * @return 
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.PreProcessCallable
	public HashMap preProcessCheckForEdit (Context context, String[] args) throws Exception{
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
			StringList financialIds =  object.getInfoList(context, SELECT_PROJECT_BENEFIT);
			for (Iterator iterator = financialIds.iterator(); iterator.hasNext();) {
				String financialId = (String) iterator.next();
				object = DomainObject.newInstance(context, financialId);
				if(object.isKindOf(context, ProgramCentralConstants.TYPE_BENEFIT)){
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
     * hasAccess - This method verifies the user's permission based on context with benefit.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * objectId   - String containing the objectId
     * @throws Exception if the operation fails
     */
    public boolean hasAccess(Context context, String[] args)
    throws Exception
    {
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
    	String objectId = (String) programMap.get("objectId");
    	boolean access = false;
    	try{
    		if (ProgramCentralUtil.isNotNullString(objectId)){
    			DomainObject dmoObject = DomainObject.newInstance(context, objectId);
    			DomainObject dmoBenefit = DomainObject.newInstance(context);
    			boolean hasBenefit = false;
    			StringList financialIds =  dmoObject.getInfoList(context, SELECT_PROJECT_FINANCIAL_ITEM_ID);
    			for (Iterator iterator = financialIds.iterator(); iterator.hasNext();) {
    				String financialId = (String) iterator.next();
    				dmoBenefit.setId(financialId);
    				if(dmoBenefit.isKindOf(context, ProgramCentralConstants.TYPE_BENEFIT)){
    					hasBenefit = true;
    					access = dmoBenefit.checkAccess(context, (short) AccessConstants.cModify);
    					break;
    				}
    			}
    			if(!hasBenefit){
    				access = dmoObject.checkAccess(context, (short) AccessConstants.cModify);
    			}
    		}
    	}
    	catch (Exception ex){
    		throw ex;
    	}
    	return access;
    }
}
