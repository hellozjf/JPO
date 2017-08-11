/*
 * emxResourcePoolReportsBase
 *
 * Copyright (c) 1999-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 */

/**
Change History:
Date       Change By  Release        Bug/Functionality        Details
-----------------------------------------------------------------------------------------------------------------------------
21-Aug-09   wqy        V6R2010x     IR-012043V6R2010x        Change the display logic for the Task hyperlink in 
															 ResourceLoading Report     
                       										 Change sequence to PersonName display only single time. 
*/

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import matrix.db.Access;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectItr;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Role;
import matrix.db.UserItr;
import matrix.db.UserList;
import matrix.util.MatrixException;
import matrix.util.StringItr;
import matrix.util.StringList;

import com.matrixone.apps.common.Organization;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.DateUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.program.FTE;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ResourceLoading;
import com.matrixone.apps.program.Task;
import com.matrixone.jdom.JDOMException;

/**
 * @author WQY
 *
 */
public class emxResourcePoolReportsBase_mxJPO 
{
    private static final String ROW_HEADER_NAME_LANG = "RowHeaderNameLanguage";
    private static final String ROW_HEADER_NAME = "RowHeaderName";
    private static final String COLUMN_MONTHYEAR_HEADER_NAME = "ColumnMonthYearHeaderName";
    private static final String COLUMN_MONTHYEAR_VALUE = "ColumnMonthYearValue";
    private static final String STRING_CAPACITY = "Capacity";
    private static final String STRING_OPEN_REQUESTS = "OpenRequests";
    private static final String STRING_PROPOSED = "Proposed";
    private static final String STRING_COMMITTED = "Committed";
    private static final String STRING_PEOPLE = "People";
    private static final String STRING_PROJECT_UTILIZATION = "Project Utilization";
    private static final String STRING_ACTUAL_UTILIZATION = "Actual Utilization";
    private static final String STRING_TITLE = "Title";
    private static final String STRING_I18N_TITLE = "i18n Title";
    private static final String STRING_PLAN="Plan";
    private static final String FTE_TIMELINE_MONTHLY = "Monthly";
    private static final String FTE_TIMELINE_WEEKLY = "Weekly";
    private static final String FTE_TIMELINE_QUARTERLY = "Quarterly";
    private static final String SELECT_REL_RESOURCE_POOL_REQUEST_ID = "to["+DomainConstants.RELATIONSHIP_RESOURCE_POOL+"].businessobject."+DomainConstants.SELECT_ID;
    private Context context;
    
    /**
     * Constructor 
     * 
     * @param context The Matrix Context object
     * @param args The arguments array
     * @throws Exception if operation fails
     */
    public emxResourcePoolReportsBase_mxJPO(Context context, String[] args) throws Exception{
    	this.context = context;
    }

   
    /**
     * @param context
     * @param args
     * @return
     * @throws MatrixException
     */
    public int mxMain(Context context, String[] args) throws MatrixException 
    {
        throw new MatrixException("This JPO cannot be run stand alone.");
    }
    
    /**
     * Get table data of Resource Utilization Report.
     *
     * @param context The Matrix Context object
     * @param args Packed program and request maps for the table
     * @return MapList containing all table data
     * @throws MatrixException if the operation fails
     * @since PRG R207
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTableResourceUtilizationReportData(Context context, String[] args)
            throws MatrixException 
    {      
        try 
        {            
            if (context == null) 
            {
                throw new IllegalArgumentException("context");
            }
            
            MapList mlResourceUtilDataList  = new MapList();
            Map programMap = (Map) JPO.unpackArgs(args);
            Map requestValuesMap = (Map) programMap.get("RequestValuesMap");
            
            String[] strTableRowIdValues =  (String[])requestValuesMap.get("ResourcePoolId");
            String strResourceObjectId = strTableRowIdValues[0];
            StringList slTableRowIdValuesSplit = FrameworkUtil.split(strResourceObjectId, ",");
            String[] strResourcePoolIds = new String[slTableRowIdValuesSplit.size()];
            slTableRowIdValuesSplit.copyInto(strResourcePoolIds);
            
            //Get the Data for the First Column 
                                
            i18nNow loc = new i18nNow();
            String strLanguage = (String)programMap.get("languageStr");
            final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
            
            final String STRING_CAPACITY_LANG = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourcePlanning.Report.Capacity");
            final String STRING_OPEN_REQUESTS_LANG = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourcePlanning.Report.OpenRequests");
            final String STRING_COMMITTED_LANG = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourcePlanning.Report.Committed");
            final String STRING_PROPOSED_LANG = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourcePlanning.Report.Proposed");
            final String STRING_PEOPLE_LANG = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourcePlanning.Report.People");
            final String STRING_PROJECT_UTILIZATION_LANG = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourcePlanning.Report.ProjectUtilization");
            final String STRING_ACTUAL_UTILIZATION_LANG = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourcePlanning.Report.ActualUtilization");
            
            StringList slRowHeaderNameList = new StringList();
            slRowHeaderNameList.add(STRING_CAPACITY);
            slRowHeaderNameList.add(STRING_OPEN_REQUESTS);
            slRowHeaderNameList.add(STRING_COMMITTED);
            slRowHeaderNameList.add(STRING_PROJECT_UTILIZATION);
            slRowHeaderNameList.add(STRING_ACTUAL_UTILIZATION);
            
            //
            // Create table rows
            //
            Map mapTableRow = new HashMap();
            mapTableRow.put(STRING_TITLE, STRING_CAPACITY);
            mapTableRow.put(STRING_I18N_TITLE, STRING_CAPACITY_LANG);
            mlResourceUtilDataList.add(mapTableRow);
            
            mapTableRow = new HashMap();
            mapTableRow.put(STRING_TITLE, STRING_OPEN_REQUESTS);
            mapTableRow.put(STRING_I18N_TITLE, STRING_OPEN_REQUESTS_LANG);
            mlResourceUtilDataList.add(mapTableRow);
         
            mapTableRow = new HashMap();
            mapTableRow.put(STRING_TITLE, STRING_COMMITTED);
            mapTableRow.put(STRING_I18N_TITLE, STRING_COMMITTED_LANG);
            mlResourceUtilDataList.add(mapTableRow);
            
            mapTableRow = new HashMap();
            mapTableRow.put(STRING_TITLE, STRING_PROJECT_UTILIZATION);
            mapTableRow.put(STRING_I18N_TITLE, STRING_PROJECT_UTILIZATION_LANG+(" (%)"));
            mlResourceUtilDataList.add(mapTableRow);
            
            mapTableRow = new HashMap();
            mapTableRow.put(STRING_TITLE, STRING_ACTUAL_UTILIZATION);
            mapTableRow.put(STRING_I18N_TITLE, STRING_ACTUAL_UTILIZATION_LANG+(" (%)"));
            mlResourceUtilDataList.add(mapTableRow);
            
            //
            // Create columns for each rows
            //
            String strFromDate = "";
            String strToDate = "";
            strFromDate = null!=requestValuesMap.get("PMCCustomFilterFromDate")?((String[])requestValuesMap.get("PMCCustomFilterFromDate"))[0]:"";
            strToDate   = null!=requestValuesMap.get("PMCCustomFilterToDate")?((String[])requestValuesMap.get("PMCCustomFilterToDate"))[0]:"";
            StringList slMonthYearList = getTimeframes(strFromDate, strToDate);
            
            Map mapMonthYearValues = getResourceUtilizationReportMonthData(context,slMonthYearList,strResourcePoolIds,slRowHeaderNameList);
            
            String strTitle = "";
            Map mapColumnsData = null;
            for (Iterator itrTableRow = mlResourceUtilDataList.iterator(); itrTableRow.hasNext();) 
            {
                mapTableRow = (Map) itrTableRow.next();
                
                strTitle = (String)mapTableRow.get(STRING_TITLE);
                mapColumnsData = (Map)mapMonthYearValues.get(strTitle);
                
                mapTableRow.put("Columns Data", mapColumnsData);
            }
                        
            return mlResourceUtilDataList;
        } 
        catch (IllegalArgumentException iaexp) 
        {
            iaexp.printStackTrace();
            throw new MatrixException(iaexp);
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
        
    /**
     * Get Resource Utilization data of resource pool sorted with header names as per time line.
     * @param context The Matrix Context object
     * @param slTimeLineList contains list of time line value
     * @param strResourcePoolIds contains list of resource pool id for which data is required
     * @param slRowHeaderNameList contains list of headers required to get sorted data
     * @return Map containing all data with time line for header names(<HeaderNames, Map<TimeLineValue, data for Timeline>> 
     * @throws MatrixException
     */
    private Map getResourceUtilizationReportMonthData(Context context,StringList slTimeLineList, String[] strResourcePoolIds, StringList slRowHeaderNameList) throws MatrixException 
    {
        try 
        {
            final String SELECT_REL_MEMBER_PERSON_ID = "from["+DomainConstants.RELATIONSHIP_MEMBER+"].businessobject."+DomainConstants.SELECT_ID;
            final String SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE = "to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].attribute["+DomainConstants.ATTRIBUTE_FTE+"].value";
            final String SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE = "from["+DomainConstants.RELATIONSHIP_ALLOCATED+"].attribute["+DomainConstants.ATTRIBUTE_FTE+"].value";
            final String SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE = "from["+DomainConstants.RELATIONSHIP_ALLOCATED+"].attribute["+DomainConstants.ATTRIBUTE_RESOURCE_STATE+"].value";
            StringList slRequestIdList  = new StringList();
            StringList slPersonIdList   = new StringList();
                    
            Map mapAllFTEValue = null;
                    
            int nPeopleSize = 0;
            
            StringList slSelectList = new StringList();
            slSelectList.add(SELECT_REL_RESOURCE_POOL_REQUEST_ID);
            slSelectList.add(SELECT_REL_MEMBER_PERSON_ID);
            
            Map mapQueryWithDataList = getResourcePoolsDataWithSelectList(context, strResourcePoolIds, slSelectList, true);
            slRequestIdList = (StringList)mapQueryWithDataList.get(SELECT_REL_RESOURCE_POOL_REQUEST_ID);
            slPersonIdList = (StringList)mapQueryWithDataList.get(SELECT_REL_MEMBER_PERSON_ID);
            Map mapMonthYearFTECapacity = new HashMap();
            StringList slFilterPersonIdList = new StringList();
            if(null!=slPersonIdList && slPersonIdList.size()>0)
            {
            	StringList slPMCUserList = getPMCUser(context);
            	for(int index=0; index<slPersonIdList.size(); index++)
            	{
            		String strPersonId = (String)slPersonIdList.get(index);
            		if(slPMCUserList.contains(strPersonId))
            		{
            			slFilterPersonIdList.add(strPersonId);
            		}
            	}
                nPeopleSize = slFilterPersonIdList.size();
                //mapMonthYearFTECapacity= getPersonsMonthYearFTECapacity(context, slPersonIdList, slTimeLineList);
            }
            slSelectList.clear();
            slSelectList.add(SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE);
            slSelectList.add(DomainConstants.SELECT_CURRENT);
            slSelectList.add(SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE);
            slSelectList.add(SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE);
            String[] strRequestIds = new String[0];
            
            BusinessObjectWithSelectList resourceRequestObjWithSelectList = null;
            BusinessObjectWithSelect bows = null;
            if(null!=slRequestIdList)
            {
            	strRequestIds = new String[slRequestIdList.size()];
            	slRequestIdList.copyInto(strRequestIds);
            	 //Modified:4-Jan-2010:ixe:R209:PRG:IR-019137
            	final String SELECT_REL_RESOURCE_PLAN_PROJECT_ID = "to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].from."+DomainConstants.SELECT_ID;
            	StringList slProjectSelectList = new StringList();
            	slProjectSelectList.add(SELECT_REL_RESOURCE_PLAN_PROJECT_ID);
            	slProjectSelectList.add("to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].from."+DomainConstants.SELECT_CURRENT);
            	resourceRequestObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strRequestIds,slProjectSelectList);
            	for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourceRequestObjWithSelectList); itr.next();)
            	{
            		String strCurrentId = "";
            		String strCurrentState = "";
            		bows = itr.obj();
            		strCurrentState = bows.getSelectData("to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].from."+DomainConstants.SELECT_CURRENT);
            		if(null != (strCurrentState)){
            			if(!isActiveProject(strCurrentState)){
            				slRequestIdList.remove(bows.getObjectId()) ;
            			}
            		}
            	}
            	strRequestIds = new String[slRequestIdList.size()];
            	slRequestIdList.copyInto(strRequestIds);
            	//Modification End:4-Jan-2010:ixe:R209:PRG:IR-019137
            }
          
            resourceRequestObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strRequestIds,slSelectList);
           
            Map mapRequestRelResourcePlanData = new HashMap();
            Map mapRequestRelAllocatedData = new HashMap();
            String strRelAllocatedResourceState = "";
            String strFTEPlanValue = "";
            StringList slFTEDataList = null;
            StringList slRelAllocatedResourceStateList = null;
           
                
            for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourceRequestObjWithSelectList); itr.next();)
            {
                bows = itr.obj();
                String strCurrentState = bows.getSelectData(DomainConstants.SELECT_CURRENT);
                if(null==mapRequestRelResourcePlanData.get(strCurrentState))
                {
                    mapAllFTEValue = new HashMap();
                }
                else
                {
                    mapAllFTEValue = (Map)mapRequestRelResourcePlanData.get(strCurrentState);
                }
                strFTEPlanValue = bows.getSelectData(SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE);
                mapAllFTEValue = getCalculatedMonthFTEMap(strFTEPlanValue, mapAllFTEValue);
                mapRequestRelResourcePlanData.put(strCurrentState, mapAllFTEValue);
                
                slRelAllocatedResourceStateList = bows.getSelectDataList(SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE);
                if(null!=slRelAllocatedResourceStateList)
                {
                    for(int iStateIndex=0; iStateIndex<slRelAllocatedResourceStateList.size(); iStateIndex++)
                    {
                        strRelAllocatedResourceState = (String)slRelAllocatedResourceStateList.get(iStateIndex);
                        if(null==mapRequestRelAllocatedData.get(strRelAllocatedResourceState))
                        {
                            mapAllFTEValue = new HashMap();
                        }
                        else
                        {
                            mapAllFTEValue = (Map)mapRequestRelAllocatedData.get(strRelAllocatedResourceState);
                        }
                        slFTEDataList = (StringList)bows.getSelectDataList(SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE);
                        if(null!=slFTEDataList)
                        {
                            strFTEPlanValue = (String)slFTEDataList.get(iStateIndex);
                            mapAllFTEValue = getCalculatedMonthFTEMap(strFTEPlanValue, mapAllFTEValue);
                            mapRequestRelAllocatedData.put(strRelAllocatedResourceState, mapAllFTEValue);
                        }
                    }
                }
            }
            Map mapRowNameMonthYearValue   = new HashMap(); 
            Map mapReqMonthYearValue       = null;
            
            String strRowHeaderValue        = "";
            String strColumnHeader          = "";

            Double dColumnHeaderValue     = null;
            Double dCommittedRequestValue = null;
            Double dCapacityValue         = null;
            double dOutputValue = 0; 
            boolean isCapacityValuePositive = false;
            Map mapReqCalculatedValue       = new HashMap();
            Map mapGetAllRequired           = null;
            Double dProjectUtilizationValue = null;
            Double dRequestValue            = null;
            
            String [] strOpenRequest = new String[3];
            strOpenRequest[0] = DomainConstants.STATE_RESOURCE_REQUEST_REQUESTED;
            strOpenRequest[1] = DomainConstants.STATE_RESOURCE_REQUEST_PROPOSED;
            //Commented:31-Dec-09:ixe:R209:PRG:014273
           // strOpenRequest[2] = DomainConstants.STATE_RESOURCE_REQUEST_REJECTED;
            
            Map  mapOpenRequestValue = new HashMap();
            for(int reqIndex = 0; reqIndex<strOpenRequest.length; reqIndex++)
            {
                Map mapOpenRequestMonthYearFTEValue = (Map)mapRequestRelResourcePlanData.get(strOpenRequest[reqIndex]);
                if(null!=mapOpenRequestMonthYearFTEValue)
                {
                    for (Iterator iter = mapOpenRequestMonthYearFTEValue.keySet().iterator(); iter.hasNext();)
                    {
                        String strMonthYearValue = (String)iter.next();
                        Double dFTEValue = new Double(0);
                        if(null!=mapOpenRequestValue.get(strMonthYearValue))
                        {
                            dFTEValue = (Double)mapOpenRequestValue.get(strMonthYearValue);
                        }
                        dFTEValue = new Double(((Double)mapOpenRequestMonthYearFTEValue.get(strMonthYearValue)).doubleValue()+dFTEValue.doubleValue());
                        mapOpenRequestValue.put(strMonthYearValue,dFTEValue);
                    }
                }
            }
            mapReqCalculatedValue.put(STRING_OPEN_REQUESTS,mapOpenRequestValue);
            mapReqCalculatedValue.put(STRING_COMMITTED,mapRequestRelAllocatedData.get(DomainConstants.ATTRIBUTE_RESOURCE_STATE_RANGE_COMMITTED));

            String [] strProjectUtlizationReq = new String[2];
            strProjectUtlizationReq[0] = STRING_OPEN_REQUESTS;
            strProjectUtlizationReq[1] = STRING_COMMITTED;
            String numberofPeopleUnit=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.NumberofPeopleUnit");
            double nHoursPerDay = FTE.getHoursPerDay(context);
            for (int index = 0; index < slRowHeaderNameList.size(); index++) 
            {
                strRowHeaderValue = (String)slRowHeaderNameList.get(index); 
                mapGetAllRequired = (HashMap)mapReqCalculatedValue.get(strRowHeaderValue);
                mapReqMonthYearValue = new HashMap();
                for (int i = 0; i < slTimeLineList.size(); i++) 
                {
                    dCommittedRequestValue      = new Double(0);
                    dCapacityValue              = new Double(0);
                    dColumnHeaderValue          = new Double(0);
                    dOutputValue                = 0;
                    strColumnHeader = (String)slTimeLineList.get(i);
                    //mapGetAllRequired = (HashMap)mapReqCalculatedValue.get(STRING_CAPACITY);
            
                    //dCapacityValue = null!=mapGetAllRequired && null!=mapGetAllRequired.get(strColumnHeader)?(Double)mapGetAllRequired.get(strColumnHeader):new Double(0D);
                    dCapacityValue = (double)nPeopleSize;
                	if("Hours".equalsIgnoreCase(numberofPeopleUnit))
					{
                		FTE fte = FTE.getInstance(context);
                		long duration = DateUtil.computeDuration(fte.getStartDate(strColumnHeader),fte.getEndDate(strColumnHeader));
                		dCapacityValue = (nPeopleSize*nHoursPerDay*duration);
					}
                    isCapacityValuePositive = nPeopleSize>0?true:false;
              
                    if(STRING_CAPACITY.equals(strRowHeaderValue))
                    {
                        dOutputValue = nPeopleSize;
                    	if("Hours".equalsIgnoreCase(numberofPeopleUnit))
						{
                    		FTE fte = FTE.getInstance(context);
                    		long duration = DateUtil.computeDuration(fte.getStartDate(strColumnHeader),fte.getEndDate(strColumnHeader));
                    		dOutputValue = (nPeopleSize*nHoursPerDay*duration);
						}
                    }
                    else if(STRING_PROJECT_UTILIZATION.equals(strRowHeaderValue) && isCapacityValuePositive)
                    {
                        dProjectUtilizationValue = new Double(0D);
                        for(int j=0; j<strProjectUtlizationReq.length; j++)
                        {
                            mapGetAllRequired = (HashMap)mapReqCalculatedValue.get(strProjectUtlizationReq[j]);
                            dRequestValue = null!=mapGetAllRequired && null!=mapGetAllRequired.get(strColumnHeader)?(Double)mapGetAllRequired.get(strColumnHeader):new Double(0D);
                            dProjectUtilizationValue = new Double(dProjectUtilizationValue.doubleValue()+dRequestValue.doubleValue());
                        }
                        dOutputValue = ((100*(dProjectUtilizationValue.doubleValue()))/dCapacityValue.doubleValue());
                    }
                    else if(STRING_ACTUAL_UTILIZATION.equals(strRowHeaderValue) && isCapacityValuePositive)
                    {
                        mapGetAllRequired = (HashMap)mapReqCalculatedValue.get(STRING_COMMITTED);
                        dCommittedRequestValue = null!=mapGetAllRequired && null!=mapGetAllRequired.get(strColumnHeader)?(Double)mapGetAllRequired.get(strColumnHeader):new Double(0D);
                        dOutputValue = ((dCommittedRequestValue.doubleValue()*100)/dCapacityValue.doubleValue());
                    }
                    else
                    {
                    	mapGetAllRequired = (HashMap)mapReqCalculatedValue.get(strRowHeaderValue);
                        dColumnHeaderValue = null!=mapGetAllRequired && null!=mapGetAllRequired.get(strColumnHeader)?(Double)mapGetAllRequired.get(strColumnHeader):new Double(0D);
                        dOutputValue = dColumnHeaderValue.doubleValue();
                    }
        			NumberFormat numberFormat = ProgramCentralUtil.getNumberFormatInstance(2, false);
                    mapReqMonthYearValue.put(strColumnHeader, numberFormat.format(dOutputValue));
                }
                mapRowNameMonthYearValue.put(strRowHeaderValue,mapReqMonthYearValue);
            }
            return mapRowNameMonthYearValue;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
 
    /**
     * <method description>
     *
     * @param context The Matrix Context object
     * @param args <description>
     * @return MapList <description>
     * @throws MatrixException if the operation fails
     * @since PRG R207
     */
    public MapList getDynamicMonthYearColumns(Context context, String[] args)
            throws MatrixException 
    {
        try 
        {
            if (context == null) 
            {
                throw new IllegalArgumentException("context");
            }
            Map programMap = (HashMap) JPO.unpackArgs(args);
            Map requestMap = (Map) programMap.get("requestMap");
            String strLanguage = (String)requestMap.get("languageStr");
            String strFromDate = "";
            String strToDate = "";
            strFromDate = (String)requestMap.get("PMCCustomFilterFromDate");
            strToDate   = (String)requestMap.get("PMCCustomFilterToDate");   
            String strMode = (String)requestMap.get("mode");
            StringList slMonthYearList = getTimeframes(strFromDate, strToDate);
            String strMonthYearValue = "";
            String strI18ColumnValue = "";
            MapList mlColumnsMapList = new MapList();
            Map columnMap = null;
            Map settingsMap = null;
            FTE fte = FTE.getInstance(context);
            String strFTETimeline = fte.getTimeframeConfigName();
            String strTimeLinePreFix = "";
            i18nNow loc = new i18nNow();
            final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
            String sLanguage = context.getSession().getLanguage();
            String sPlan = EnoviaResourceBundle.getProperty(context, ProgramCentralConstants.PROGRAMCENTRAL,"emxProgramCentral.Effort.Plan", 
            		sLanguage);
            String sActual = EnoviaResourceBundle.getProperty(context, ProgramCentralConstants.PROGRAMCENTRAL,"emxProgramCentral.Common.Actual", 
            		sLanguage);
            if(FTE_TIMELINE_WEEKLY.equals(strFTETimeline))
            {   
            	strTimeLinePreFix =  (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourceRequest.TimeLine.WeekPrefix");
            }
            else if(FTE_TIMELINE_QUARTERLY.equals(strFTETimeline))
            {              
               	strTimeLinePreFix =  (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourceRequest.TimeLine.QuarterPrefix");
            }
            for (int i = 0; i < slMonthYearList.size(); i++)
            {
                //Related Objects - Name column
                strMonthYearValue = (String)slMonthYearList.get(i);
                strI18ColumnValue = "";
                if(FTE_TIMELINE_MONTHLY.equals(strFTETimeline))
                {  
                	strI18ColumnValue = geti18MonthYearName(strMonthYearValue, strLanguage);
                }
                else
                {
                	strI18ColumnValue = strTimeLinePreFix+strMonthYearValue;
                }
                columnMap = new HashMap();
                settingsMap = new HashMap();
                columnMap.put("settings", settingsMap);
                columnMap.put("name", strMonthYearValue);//TODO English
                columnMap.put("label", strI18ColumnValue);//TODO Translated
                settingsMap.put("Registered Suite","ProgramCentral");
                settingsMap.put("function","getColumnMonthYearData");
                settingsMap.put("program","emxResourcePoolReports");
                settingsMap.put("Column Type","program");
                settingsMap.put("Sortable","false");
                //if(strMode.equals("PMCPeopleProjectAssignmentReport"))
                if(("PMCPeopleProjectAssignmentReport").equals(strMode))
                {
                	settingsMap.put("Style Program","emxResourcePoolReports");
                	settingsMap.put("Style Function","getFTEStyleInfo");
                	mlColumnsMapList.add(columnMap);
                }
               // else if(strMode.equals("PMCResourceUtilizationReport"))
                else if(("PMCResourceUtilizationReport").equals(strMode))
                {
                	settingsMap.put("Style Program","emxResourcePoolReports");
                	settingsMap.put("Style Function","getStyleInfoForResourceUtilizationReport");
                	mlColumnsMapList.add(columnMap);
                }
                //else if(strMode.equals("PMCResourceLoadingReport"))
                else if(("PMCResourceLoadingReport").equals(strMode))
                {
                	String strDisplayMode = (String)requestMap.get("displaymode");
                	if(strDisplayMode.equals("Summary"))
                	{
                		//settingsMap.put("Style Program","emxResourcePoolReports");
                		//settingsMap.put("Style Function","getStyleInfoForResourceLoadingReport");
                		mlColumnsMapList.add(columnMap);
                	}
                	else if(strDisplayMode.equals("Detail"))
        			{
                		columnMap = new HashMap();
                        settingsMap = new HashMap();
        				columnMap.put("settings", settingsMap);
        				columnMap.put("name", "Plan_"+strMonthYearValue);//TODO English
        				columnMap.put("label", sPlan);
        				settingsMap.put("Registered Suite","ProgramCentral");
        				settingsMap.put("function","getColumnPlanVsActualMonthYearData");
        				settingsMap.put("program","emxResourcePoolReports");
        				settingsMap.put("Column Type","program");
        				settingsMap.put("Sortable","false");
            			settingsMap.put("Group Header",strI18ColumnValue);
            			mlColumnsMapList.add(columnMap);
            			
            			columnMap = new HashMap();
                        settingsMap = new HashMap();
        				columnMap.put("settings", settingsMap);
        				columnMap.put("name", "Actual_"+strMonthYearValue);//TODO English
        				columnMap.put("label", sActual);
        				settingsMap.put("Registered Suite","ProgramCentral");
        				settingsMap.put("function","getColumnPlanVsActualMonthYearData");
        				settingsMap.put("program","emxResourcePoolReports");
        				settingsMap.put("Column Type","program");
        				settingsMap.put("Sortable","false");
            			settingsMap.put("Group Header",strI18ColumnValue);
            			settingsMap.put("TimeLine Header",strMonthYearValue);
            			settingsMap.put("Style Program","emxResourcePoolReports");
                		settingsMap.put("Style Function","getStyleInfoForPlanvsActualReport");
            			mlColumnsMapList.add(columnMap);
        			}
                }
                else
                {
                	mlColumnsMapList.add(columnMap);
                }
             }
             return mlColumnsMapList;
        }         
        catch (IllegalArgumentException iaexp) 
        {
            iaexp.printStackTrace();
            throw new MatrixException(iaexp);
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
    
    /**
     * Gets the data for the column "<name>" for table "<name>"
     * 
     * @param context The matrix context object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing
     * @throws Exception if operation fails
     */
    public Vector getColumnMonthYearData(Context context, String[] args)
            throws Exception 
    {
        try 
        {
            // Create result vector
            Vector vecResult = new Vector();
            // Get object list information from packed arguments
            Map programMap = (HashMap) JPO.unpackArgs(args);
            Map columnMap = (HashMap) programMap.get("columnMap");
            String strColumnName = (String)columnMap.get("name");
            MapList mlObjectList = (MapList) programMap.get("objectList");
            
            Map mapObjectInfo = null;
            Map mapColumnsData = null;
            for (Iterator itrTableRows = mlObjectList.iterator(); itrTableRows.hasNext();) 
            {
                mapObjectInfo = (Map) itrTableRows.next();
                if(null!=mapObjectInfo.get("Columns Data"))
                {
                	mapColumnsData = (Map)mapObjectInfo.get("Columns Data");
                	if(null!=mapColumnsData.get(strColumnName))
                	{
                		vecResult.add (mapColumnsData.get(strColumnName));
                	}
                	else
                	{
                		vecResult.add ("");
                	}
                }
                else
                {
                	vecResult.add ("");
                }
            }
            return vecResult;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw exp;
        }
    }
    
    /**
     * Gets the data for the column "<name>" for table "<name>"
     * 
     * @param context The matrix context object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing
     * @throws Exception if operation fails
     */
    public Vector getColumnRowHeaderData(Context context, String[] args) throws Exception 
    {
        try 
        {
            // Create result vector
            Vector vecResult = new Vector();
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList mlObjectList = (MapList) programMap.get("objectList");
            
            HashMap paramMap = (HashMap) programMap.get("paramList");
            String exportFormat = (String) paramMap.get("exportFormat");
            
            Map mapObjectInfo = null;
            for (Iterator itrTableRows = mlObjectList.iterator(); itrTableRows.hasNext();) {
                mapObjectInfo = (Map) itrTableRows.next();
                if("CSV".equalsIgnoreCase(exportFormat))
                {
                	vecResult.add (mapObjectInfo.get(STRING_TITLE));
                }
                else{
                	vecResult.add (mapObjectInfo.get(STRING_I18N_TITLE));
                }
            }
            return vecResult;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw exp;
        }
    }
    
    /**
     * @param strFromDate
     * @param strToDate
     * @return
     * @throws Exception
     */
    public StringList getTimeframes(String strFromDate, String strToDate) throws Exception
    {
        long lFromDate = 0;
        long lToDate = 0;
        if ( strFromDate!= null && !"".equals(strFromDate) && !"null".equals(strFromDate))  
        {
            lFromDate = (Long.parseLong(strFromDate));               
        }
        if ( strToDate!= null && !"".equals(strToDate) && !"null".equals(strToDate))
        {
            lToDate   = (Long.parseLong(strToDate));                
        }
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        if(lFromDate>0)
        {
            startDate.setTimeInMillis(lFromDate);
        }
        else
        {
            startDate.setTimeInMillis(System.currentTimeMillis());
        }
        if(lToDate>0)
        {
            endDate.setTimeInMillis(lToDate);
        }
        else
        {
            int nNoofMonths = Integer.parseInt(FrameworkProperties.getProperty("emxProgramCentral.ResourcePlanning.Report.Default.NoofMonths"))-1;
            endDate.setTimeInMillis(System.currentTimeMillis());
            endDate.set(Calendar.MONTH, (endDate.get(Calendar.MONTH)+nNoofMonths));
        }
        return getTimeframes(startDate, endDate);
    }
    
    
    public StringList getTimeframes(Calendar startDate, Calendar endDate) throws Exception
    {
    	FTE fte = FTE.getInstance(context);
    	Date fromDate = startDate.getTime();
    	Date toDate = endDate.getTime();
    	MapList mlGetTimeFrames = fte.getTimeframes(fromDate, toDate);
    	StringList slTimeFrameList = new StringList();
    	Map mapObjectInfo = null;
        Map mapColumnsData = null;
        String strTimeFrame = "";
        String strYear = "";
    	for (Iterator timeFrameItr = mlGetTimeFrames.iterator(); timeFrameItr.hasNext();) 
    	{
    		  mapObjectInfo = (Map)timeFrameItr.next();
    		  strTimeFrame = Integer.toString((Integer)mapObjectInfo.get(FTE.ATTRIBUTE_TIMEFRAME));
    	      strYear =  Integer.toString((Integer)mapObjectInfo.get(FTE.ATTRIBUTE_YEAR));
    		  slTimeFrameList.add(strTimeFrame+"-"+strYear);
		}
        return slTimeFrameList;
    }
    
    /**
     * Get i18Name of Month and Year for eg. Jan-2008 
     * @param strMonthYear 
     * @param strLanguage
     * @return
     * @throws MatrixException 
     */
    private String geti18MonthYearName(String strMonthYear, String strLanguage) throws MatrixException
    {
        try
        {
            i18nNow loc = new i18nNow();
            final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
            String strI18MonthName = "";
            int nMonth = Integer.parseInt(strMonthYear.substring(0,(strMonthYear.indexOf("-"))));
            int nYear = Integer.parseInt(strMonthYear.substring((strMonthYear.indexOf("-")+1)));
            
            switch (nMonth) 
            {
                case 1:
                    strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Jan");
                    break;
                case 2:
                    strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Feb");
                    break;
                case 3:
                    strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Mar");
                    break;
                case 4:
                    strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Apr");
                    break;
                case 5:
                    strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.May");
                    break;                
                case 6:
                    strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Jun");
                    break;
                case 7:
                    strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Jul");
                    break;
                case 8:
                    strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Aug");
                    break;
                case 9:
                    strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Sep");
                    break;
                case 10:
                    strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Oct");
                    break;
                case 11:
                    strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Nov");
                    break;
                case 12:
                    strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Dec");
                    break;
            }
            //TODO
            //Convert year in I18
            //String strI18YearName = (String)loc.GetString(strLanguage, "emxProgramCentral.Common.Dec");
            String strI18YearName = "-"+nYear;
            return strI18MonthName+strI18YearName;
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            throw new MatrixException(e);
        }
    }
    
    protected static Map getCalculatedMonthFTEMap(String strFTEValue, Map mapAllFTEValue) throws MatrixException
    {
        
        String strMonthYearValue = "";
        Double dFTEValue = null;
        
        if (strFTEValue != null && !"".equals(strFTEValue) && !"null".equals(strFTEValue)) 
        {
            FTE fteRequest = FTE.getInstance(strFTEValue);
            Map mapMonthYearFTEValue = fteRequest.getAllFTE();
            
            if(null!=mapMonthYearFTEValue)
            {
                for (Iterator iter = mapMonthYearFTEValue.keySet().iterator(); iter.hasNext();)
                {
                    strMonthYearValue = (String)iter.next();
                    dFTEValue = new Double(0);
                    if(null!=mapAllFTEValue.get(strMonthYearValue))
                    {
                        dFTEValue = (Double)mapAllFTEValue.get(strMonthYearValue);
                    }
                    dFTEValue = new Double(((Double)mapMonthYearFTEValue.get(strMonthYearValue)).doubleValue()+dFTEValue.doubleValue());
                    mapAllFTEValue.put(strMonthYearValue,dFTEValue);
                }
            }
        }
        return mapAllFTEValue;
    }
        
    protected static Map getPersonsMonthYearFTECapacity(Context context, StringList slPersonIdList, StringList slMonthYear) throws MatrixException
    {
        final String SELECT_REL_TO_ALLOCATED_ATTRIBUTE_FTE = "to["+DomainConstants.RELATIONSHIP_ALLOCATED+"].attribute["+DomainConstants.ATTRIBUTE_FTE+"].value"; 
        int nPeopleSize = slPersonIdList.size();
        String[] strPersonIds = new String[nPeopleSize];
        slPersonIdList.copyInto(strPersonIds);
        String strMonthYearValue = "";
        Double dFTEValue = null;
        Map mapAllFTEValue = null;
        
        StringList slSelectList = new StringList();
        slSelectList.add(SELECT_REL_TO_ALLOCATED_ATTRIBUTE_FTE);
        
        Map mapMonthYearFTECapacity = new HashMap();
        
        BusinessObjectWithSelectList personObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strPersonIds,slSelectList);
        BusinessObjectWithSelect bows = null;
        StringList slPersonFTEs = null;
        String strPersonFTEValue = "";
        Double dPersonFTEsValue = null;
        
        for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(personObjWithSelectList); itr.next();)
        {
            bows = itr.obj();
            slPersonFTEs = bows.getSelectDataList(SELECT_REL_TO_ALLOCATED_ATTRIBUTE_FTE);
            mapAllFTEValue = new HashMap();
            if(null!=slPersonFTEs) 
            {
                for(StringItr itrFTE = new StringItr(slPersonFTEs);itrFTE.next();)
                {
                    strPersonFTEValue = itrFTE.obj();
                    mapAllFTEValue = getCalculatedMonthFTEMap(strPersonFTEValue, mapAllFTEValue);
                }
            }
            for (int i = 0; i < slMonthYear.size(); i++) 
            {
                strMonthYearValue = (String)slMonthYear.get(i);
                dFTEValue = new Double(0d);
                dPersonFTEsValue = new Double(0D);
                if(null!=mapAllFTEValue.get(strMonthYearValue))
                {
                    dPersonFTEsValue = (Double)mapAllFTEValue.get(strMonthYearValue);
                }
                dPersonFTEsValue = new Double((1d)-dPersonFTEsValue.doubleValue());
                if(dPersonFTEsValue.doubleValue()<0)
                {
                    dPersonFTEsValue = new Double(0d);
                }
                if(null!=mapMonthYearFTECapacity.get(strMonthYearValue))
                {
                    dFTEValue = (Double)mapMonthYearFTECapacity.get(strMonthYearValue);
                }
                dFTEValue = new Double(dPersonFTEsValue.doubleValue()+dFTEValue.doubleValue());
                mapMonthYearFTECapacity.put(strMonthYearValue,dFTEValue);
            }
        }
        return mapMonthYearFTECapacity;
    } 
    
    
    
    protected static Map getResourcePoolsDataWithSelectList(Context context, String[] strObjectIds, StringList slSelectList, boolean isDistinctObjectReqd)
    throws MatrixException
    {
    	BusinessObjectWithSelectList resourcePoolObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strObjectIds,slSelectList);
        BusinessObjectWithSelect bows = null;
        StringList slSelectIdDataList = null;
        StringList slSelectIdList = null;
        String strSelectQuery = "";
        Map mapQueryWithDataList = new HashMap();
        for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourcePoolObjWithSelectList); itr.next();)
        {
            bows = itr.obj();
            for(int i=0; i<slSelectList.size(); i++)
            {                
                strSelectQuery = (String)slSelectList.get(i);
                slSelectIdDataList = bows.getSelectDataList(strSelectQuery);
                if(null!=slSelectIdDataList)
                {
                    slSelectIdList = new StringList();
                    if(null!=mapQueryWithDataList.get(strSelectQuery))
                    {
                        slSelectIdList.addAll((StringList)mapQueryWithDataList.get(strSelectQuery));
                    }
                    if(isDistinctObjectReqd)
                    {
	                    for(StringItr itrPerson = new StringItr(slSelectIdDataList); itrPerson.next();)
	                    {
	                        if(!slSelectIdList.contains(itrPerson.obj()))
	                        {
	                            slSelectIdList.add(itrPerson.obj());
	                        }
	                    }
                    }
                    else
                    {
                    	slSelectIdList.addAll(slSelectIdDataList);
                    }
                    mapQueryWithDataList.put(strSelectQuery,slSelectIdList);
                }
            }
        }
        return mapQueryWithDataList;
    }
    
    /**
     * <method description>
     *
     * @param context The Matrix Context object
     * @param args <description>
     * @return MapList <description>
     * @throws MatrixException if the operation fails
     * @since PRG R207
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTableResourceDemandVersusCapacityReportData(Context context, String[] args)
            throws MatrixException 
    {   
        MapList mlResourceUtilDataList  = new MapList();
        try 
        {            
            if (context == null) 
            {
                throw new IllegalArgumentException("context");
            }
            
            //MapList mlResourceUtilDataList  = new MapList();
            Map programMap = (Map) JPO.unpackArgs(args);
                                  
            Map requestValuesMap = (Map) programMap.get("RequestValuesMap");
            
            String[] strTableRowIdValues =  (String[])requestValuesMap.get("ResourcePoolId");
            String strResourceObjectId = strTableRowIdValues[0];
            StringList slTableRowIdValuesSplit = FrameworkUtil.split(strResourceObjectId, ",");
            String[] strResourcePoolIds = new String[slTableRowIdValuesSplit.size()];
            slTableRowIdValuesSplit.copyInto(strResourcePoolIds);
            //String[] strResourcePoolIds = new String[strTableRowIdValues.length];
            //for (int i = 0; i < strTableRowIdValues.length; i++) 
            //{
            //    slTableRowIdValuesSplit = FrameworkUtil.split(strTableRowIdValues[i],"|"); 
            //    strResourcePoolIds[i]=(String)slTableRowIdValuesSplit.get(0);
            //}
        
            //Get the Data for the First Column 
            i18nNow loc = new i18nNow();
            String strLanguage = (String)programMap.get("languageStr");
            final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
            
            final String STRING_DELTA_LANG = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourcePlanning.Report.Capacity");
            final String STRING_ASSIGNED_LANG = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourcePlanning.Report.Committed");
            final String STRING_OPEN_REQUESTS_LANG = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourcePlanning.Report.OpenRequests");
            
            StringList slRowHeaderNameLangList = new StringList();
            slRowHeaderNameLangList.add(STRING_DELTA_LANG);
            slRowHeaderNameLangList.add(STRING_ASSIGNED_LANG);
            slRowHeaderNameLangList.add(STRING_OPEN_REQUESTS_LANG);
          
            StringList slRowHeaderNameList = new StringList();
            slRowHeaderNameList.add(STRING_CAPACITY);
            slRowHeaderNameList.add(STRING_COMMITTED);
            slRowHeaderNameList.add(STRING_OPEN_REQUESTS);
            //
            // Create table rows
            //
            Map mapTableRow = new HashMap();
            mapTableRow.put(STRING_TITLE, STRING_CAPACITY);
            mapTableRow.put(STRING_I18N_TITLE, STRING_DELTA_LANG);
            mlResourceUtilDataList.add(mapTableRow);
            
            mapTableRow = new HashMap();
            mapTableRow.put(STRING_TITLE, STRING_COMMITTED);
            mapTableRow.put(STRING_I18N_TITLE, STRING_ASSIGNED_LANG);
            mlResourceUtilDataList.add(mapTableRow);
            
            mapTableRow = new HashMap();
            mapTableRow.put(STRING_TITLE, STRING_OPEN_REQUESTS);
            mapTableRow.put(STRING_I18N_TITLE, STRING_OPEN_REQUESTS_LANG);
            mlResourceUtilDataList.add(mapTableRow);
            
            //
            // Create columns for each rows
            //
            String strFromDate = "";
            String strToDate = "";
            strFromDate = null!=requestValuesMap.get("PMCCustomFilterFromDate")?((String[])requestValuesMap.get("PMCCustomFilterFromDate"))[0]:"";
            strToDate   = null!=requestValuesMap.get("PMCCustomFilterToDate")?((String[])requestValuesMap.get("PMCCustomFilterToDate"))[0]:"";
            StringList slMonthYearList = getTimeframes(strFromDate, strToDate);
            
            Map mapMonthYearValues = getResourceDemandVersusCapacityReportData(context,slMonthYearList,strResourcePoolIds,slRowHeaderNameList);
            String strTitle = "";
            Map mapColumnsData = null;
            for (Iterator itrTableRow = mlResourceUtilDataList.iterator(); itrTableRow.hasNext();) 
            {
                mapTableRow = (Map) itrTableRow.next();
                strTitle = (String)mapTableRow.get(STRING_TITLE);
                mapColumnsData = (Map)mapMonthYearValues.get(strTitle);
                mapTableRow.put("Columns Data", mapColumnsData);
            }
            return mlResourceUtilDataList;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
                   
    private Map getResourceDemandVersusCapacityReportData(Context context, StringList slMonthYear, String[] strResourcePoolIds, StringList slRowHeaderNameList) throws MatrixException 
    {
        try 
        {
            
            final String SELECT_REL_MEMBER_PERSON_ID = "from["+DomainConstants.RELATIONSHIP_MEMBER+"].businessobject."+DomainConstants.SELECT_ID;
            final String SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE = "to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].attribute["+DomainConstants.ATTRIBUTE_FTE+"].value";
            final String SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE = "from["+DomainConstants.RELATIONSHIP_ALLOCATED+"].attribute["+DomainConstants.ATTRIBUTE_FTE+"].value";
            final String SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE = "from["+DomainConstants.RELATIONSHIP_ALLOCATED+"].attribute["+DomainConstants.ATTRIBUTE_RESOURCE_STATE+"].value";
			//Added:31-May-2010:vf2:R210 PRG:IR-039216
			final String SELECT_REL_ALLOCATED_ID= "from["+DomainConstants.RELATIONSHIP_ALLOCATED+"].to.id";
			//End:31-May-2010:vf2:R210 PRG:IR-039216            
            StringList slRequestIdList  = new StringList();
            StringList slPersonIdList   = new StringList();
            
            Map mapAllFTEValue = null;
            int nPeopleSize = 0;
            
            StringList slSelectList = new StringList();
            slSelectList.add(SELECT_REL_RESOURCE_POOL_REQUEST_ID);
            slSelectList.add(SELECT_REL_MEMBER_PERSON_ID);
            
            Map mapQueryWithDataList = getResourcePoolsDataWithSelectList(context, strResourcePoolIds, slSelectList, true);
            
            slRequestIdList = (StringList)mapQueryWithDataList.get(SELECT_REL_RESOURCE_POOL_REQUEST_ID);
            slPersonIdList = (StringList)mapQueryWithDataList.get(SELECT_REL_MEMBER_PERSON_ID);
            StringList slFilterPersonIdList = new StringList();
            if(null!=slPersonIdList && slPersonIdList.size()>0)
            {
            	StringList slPMCUserList = getPMCUser(context);
            	for(int index=0; index<slPersonIdList.size(); index++)
            	{
            		String strPersonId = (String)slPersonIdList.get(index);
            		if(slPMCUserList.contains(strPersonId))
            		{
            			slFilterPersonIdList.add(strPersonId);
            		}
            	}
                nPeopleSize = slFilterPersonIdList.size();
                //mapMonthYearFTECapacity= getPersonsMonthYearFTECapacity(context, slPersonIdList, slTimeLineList);
            }
            slSelectList.clear();
            slSelectList.add(SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE);
            slSelectList.add(DomainConstants.SELECT_CURRENT);
            slSelectList.add(SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE);
            slSelectList.add(SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE);
			//Added:31-May-2010:vf2:R210 PRG:IR-039216
			slSelectList.add(SELECT_REL_ALLOCATED_ID);
			//End:31-May-2010:vf2:R210 PRG:IR-039216            
            String[] strRequestIds = new String[0];
            if(null!=slRequestIdList)
            {
            	strRequestIds = new String[slRequestIdList.size()];
            	slRequestIdList.copyInto(strRequestIds);
            }
            
            BusinessObjectWithSelectList resourceRequestObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strRequestIds,slSelectList);
            BusinessObjectWithSelect bows = null;
            /* Added:23-Sep-2010:rg6:R210 PRG:IR-072914V6R2011x
			//Added:31-May-2010:vf2:R210 PRG:IR-039216
			StringList committedList = null;
			StringList committedID = null;
			HashMap committedMap = new HashMap();
			int committedPeople = 0; 	            
        	for(BusinessObjectWithSelectItr nitr= new BusinessObjectWithSelectItr(resourceRequestObjWithSelectList); nitr.next();)
        	{
        		bows = nitr.obj();				
				committedList = bows.getSelectDataList(SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE);
				committedID = bows.getSelectDataList(SELECT_REL_ALLOCATED_ID);	
				if(committedList != null && committedID != null) {
					for(int i = 0; i<committedList.size(); i++){
						if(STRING_COMMITTED.equals(committedList.get(i))){
							committedMap.put(committedID.get(i), committedID.get(i));
						}
					}
				}
				committedPeople = committedMap.size();				     		
        	}    
        	//End:31-May-2010:vf2:R210 PRG:IR-039216         	
        	* End:23-Sep-2010:rg6:R210 PRG:IR-072914V6R2011x         	           	
        	*/
            
            final String SELECT_REL_RESOURCE_PLAN_PROJECT_ID = "to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].from."+DomainConstants.SELECT_ID;
        	StringList slProjectSelectList = new StringList();
        	slProjectSelectList.add(SELECT_REL_RESOURCE_PLAN_PROJECT_ID);
        	slProjectSelectList.add("to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].from."+DomainConstants.SELECT_CURRENT);
        	BusinessObjectWithSelectList resourceRequestObjWithSelectList1 = BusinessObject.getSelectBusinessObjectData(context,strRequestIds,slProjectSelectList); //modified:23-Sep-2010:rg6:R210 PRG:IR-072914V6R2011x
        	//Start : Modified by ixe for IR-019137V6R2011
        	for(BusinessObjectWithSelectItr nitr= new BusinessObjectWithSelectItr(resourceRequestObjWithSelectList1); nitr.next();) //modified:23-Sep-2010:rg6:R210 PRG:IR-072914V6R2011x
        	{
        		String strProjectId = "";
        		String strCurrentState = "";
        		bows = nitr.obj();
        		strProjectId = bows.getSelectData(SELECT_REL_RESOURCE_PLAN_PROJECT_ID);
        		String strProjectState = bows.getSelectData("to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].from."+DomainConstants.SELECT_CURRENT);
        			if(!isActiveProject(strProjectState)){
        				slRequestIdList.remove(bows.getObjectId()) ;
        			}
        	}
        	//End : Modified by ixe for IR-019137V6R2011
            Map mapRequestRelResourcePlanData = new HashMap();
            Map mapRequestRelAllocatedData = new HashMap();
            
            String strRelAllocatedResourceState = "";
            String strFTEPlanValue = "";
            
            StringList slFTEDataList = null;
            StringList slRelAllocatedResourceStateList = null;
                
          //  for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourceRequestObjWithSelectList); itr.next();)
          //  {
         //       bows = itr.obj();
            for(StringItr stringItr = new StringItr(slRequestIdList); stringItr.next();)
        	{
        		String strRequestID = stringItr.obj();
        		DomainObject dmoRequest =DomainObject.newInstance(context, strRequestID);
        		String strCurrentState =  dmoRequest.getInfo(context,DomainConstants.SELECT_CURRENT);
                if(strCurrentState.equals(DomainConstants.STATE_RESOURCE_REQUEST_REQUESTED)||strCurrentState.equals(DomainConstants.STATE_RESOURCE_REQUEST_PROPOSED))
                {
                    if(null==mapRequestRelResourcePlanData.get(STRING_OPEN_REQUESTS))
                    {
                        mapAllFTEValue = new HashMap();
                    }
                    else
                    {   
                        mapAllFTEValue = (Map)mapRequestRelResourcePlanData.get(STRING_OPEN_REQUESTS);
                    }
                    strFTEPlanValue = dmoRequest.getInfo(context,SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE);
                    mapAllFTEValue = getCalculatedMonthFTEMap(strFTEPlanValue, mapAllFTEValue);
                    mapRequestRelResourcePlanData.put(STRING_OPEN_REQUESTS, mapAllFTEValue);
                }
                
                //slRelAllocatedResourceStateList = bows.getSelectDataList(SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE); //Modified:23-Sep-2010:rg6:R210 PRG:IR-072914V6R2011x
                slRelAllocatedResourceStateList = dmoRequest.getInfoList(context,SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE);
                
                if(null!=slRelAllocatedResourceStateList)
                {
                    for(int iStateIndex=0; iStateIndex<slRelAllocatedResourceStateList.size(); iStateIndex++)
                    {
                        strRelAllocatedResourceState = (String)slRelAllocatedResourceStateList.get(iStateIndex);
                        if(null==mapRequestRelAllocatedData.get(strRelAllocatedResourceState))
                        {
                            mapAllFTEValue = new HashMap();
                        }
                        else
                        {
                            mapAllFTEValue = (Map)mapRequestRelAllocatedData.get(strRelAllocatedResourceState);
                        }
                        //slFTEDataList = (StringList)bows.getSelectDataList(SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE); ////Modified:23-Sep-2010:rg6:R210 PRG:IR-072914V6R2011x
                        slFTEDataList = (StringList)dmoRequest.getInfoList(context,SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE);
                        if(null!=slFTEDataList)
                        {
                            strFTEPlanValue = (String)slFTEDataList.get(iStateIndex);
                            mapAllFTEValue = getCalculatedMonthFTEMap(strFTEPlanValue, mapAllFTEValue);
                            mapRequestRelAllocatedData.put(strRelAllocatedResourceState, mapAllFTEValue);
                        }
                    }
                }
            }
            Map mapRowNameMonthYearValue   = new HashMap(); 
            Map mapReqMonthYearValue       = null;
            
            String strRowHeaderValue        = "";
            String strColumnHeader          = "";

            Double dColumnHeaderValue     = null;
            Double dOpenRequestValue      = null;
            Double dAssignedRequestValue = null;
            double dOutputValue = 0; 
            
            Map mapReqCalculatedValue = new HashMap();
            Map mapGetAllRequired = null;
            
            mapReqCalculatedValue.put(STRING_OPEN_REQUESTS,mapRequestRelResourcePlanData.get(STRING_OPEN_REQUESTS));
            mapReqCalculatedValue.put(STRING_COMMITTED,mapRequestRelAllocatedData.get(DomainConstants.ATTRIBUTE_RESOURCE_STATE_RANGE_COMMITTED));
            String numberofPeopleUnit=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.NumberofPeopleUnit");
            double nHoursPerDay = FTE.getHoursPerDay(context);
            FTE fte = FTE.getInstance(context);
            for (int index = 0; index < slRowHeaderNameList.size(); index++) 
            {
                strRowHeaderValue = (String)slRowHeaderNameList.get(index); 
                mapGetAllRequired = (HashMap)mapReqCalculatedValue.get(strRowHeaderValue);
                mapReqMonthYearValue = new HashMap();
                for (int i = 0; i < slMonthYear.size(); i++) 
                {
                    dOpenRequestValue           = new Double(0);
                    dAssignedRequestValue       = new Double(0);
                    dColumnHeaderValue          = new Double(0);
                    
                    strColumnHeader = (String)slMonthYear.get(i);
                    if(STRING_CAPACITY.equals(strRowHeaderValue))
                    {
                        /*mapGetAllRequired = (HashMap)mapReqCalculatedValue.get(STRING_OPEN_REQUESTS);
                        dOpenRequestValue = null!=mapGetAllRequired && null!=mapGetAllRequired.get(strColumnHeader)?(Double)mapGetAllRequired.get(strColumnHeader):new Double(0D);
                        
                        mapGetAllRequired = (HashMap)mapReqCalculatedValue.get(STRING_COMMITTED);
                        dAssignedRequestValue = null!=mapGetAllRequired && null!=mapGetAllRequired.get(strColumnHeader)?(Double)mapGetAllRequired.get(strColumnHeader):new Double(0D);
                        
                        dOutputValue = nPeopleSize-(dOpenRequestValue.doubleValue()+dAssignedRequestValue.doubleValue());*/
                    	dOutputValue = nPeopleSize;
                    	if("Hours".equalsIgnoreCase(numberofPeopleUnit))
                    	{
                    		long duration = DateUtil.computeDuration(fte.getStartDate(strColumnHeader),fte.getEndDate(strColumnHeader));
                    		dOutputValue = (nPeopleSize*nHoursPerDay*duration);
                    	}
                    }
                    else
                    {
                        dColumnHeaderValue = null!=mapGetAllRequired && null!=mapGetAllRequired.get(strColumnHeader)?(Double)mapGetAllRequired.get(strColumnHeader):new Double(0D);
                        dOutputValue = dColumnHeaderValue.doubleValue();
                    }
        			NumberFormat numberFormat = ProgramCentralUtil.getNumberFormatInstance(2, true);
                    mapReqMonthYearValue.put(strColumnHeader, numberFormat.format(dOutputValue));
                }
                mapRowNameMonthYearValue.put(strRowHeaderValue,mapReqMonthYearValue);
            }
            return mapRowNameMonthYearValue;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
    
    /**
     * @param context
     * @param args
     * @return
     * @throws MatrixException
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTableResourceDemandSummaryReportData(Context context, String[] args)
    throws MatrixException 
    {   
        MapList mlResourceUtilDataList  = new MapList();
        try 
        {            
            if (context == null) 
            {
                throw new IllegalArgumentException("context");
            }
            
            //MapList mlResourceUtilDataList  = new MapList();
            Map programMap = (Map) JPO.unpackArgs(args);
            
            Map requestValuesMap = (Map) programMap.get("RequestValuesMap");
            
          //Get the Data for the First Column 
            i18nNow loc = new i18nNow();
            String strLanguage = (String)programMap.get("languageStr");
            final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
            
            final String STRING_CAPACITY_LANG = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourcePlanning.Report.Capacity");
            
            String[] strTableRowIdValues =  (String[])requestValuesMap.get("ResourcePoolId");
            String strResourceObjectId = strTableRowIdValues[0];
            StringList slResourcePoolIds = FrameworkUtil.split(strResourceObjectId, ",");
            String[] strResourcePoolIds = new String[slResourcePoolIds.size()];
            slResourcePoolIds.copyInto(strResourcePoolIds);
        
            StringList slSelectList = new StringList();
            slSelectList.add(DomainConstants.SELECT_NAME);
            slSelectList.add(DomainConstants.SELECT_TYPE);
            
            Map mapQueryWithDataList = getResourcePoolsDataWithSelectList(context, strResourcePoolIds, slSelectList, false);
            
            StringList slNameList = (StringList)mapQueryWithDataList.get(DomainConstants.SELECT_NAME);
            StringList slTypeList = (StringList)mapQueryWithDataList.get(DomainConstants.SELECT_TYPE);
            
            Map mapTableRow = null;
            for(int i=0; i<slResourcePoolIds.size(); i++)
            {
                mapTableRow = new HashMap();
                String strType = (String)slTypeList.get(i);
                //Added for special character.
                String resourcePoolName	=	 (String)slNameList.get(i);
                String resPoolHyperLnk = getOrganizationHRefLink(context, strType, (String)slResourcePoolIds.get(i),XSSUtil.encodeForHTML(context, resourcePoolName));
                mapTableRow.put("ResourcePoolId", slResourcePoolIds.get(i));
                //Added for special character.
                mapTableRow.put(STRING_TITLE,resourcePoolName);
                mapTableRow.put(STRING_I18N_TITLE, resPoolHyperLnk);
                mlResourceUtilDataList.add(mapTableRow);
            }
            mapTableRow = new HashMap();
            mapTableRow.put("ResourcePoolId", STRING_CAPACITY);
            mapTableRow.put(STRING_TITLE, STRING_CAPACITY);
            mapTableRow.put(STRING_I18N_TITLE, STRING_CAPACITY_LANG);
            mlResourceUtilDataList.add(mapTableRow);
            
            String strFromDate = "";
            String strToDate = "";
            strFromDate = null!=requestValuesMap.get("PMCCustomFilterFromDate")?((String[])requestValuesMap.get("PMCCustomFilterFromDate"))[0]:"";
            strToDate   = null!=requestValuesMap.get("PMCCustomFilterToDate")?((String[])requestValuesMap.get("PMCCustomFilterToDate"))[0]:"";
            StringList slMonthYearList = getTimeframes(strFromDate, strToDate);
            
            Map mapMonthYearValues = getResourceDemandSummaryReportData(context,slMonthYearList,strResourcePoolIds,slNameList);
            String strResourcePoolId = "";
            Map mapColumnsData = null;
            for (Iterator itrTableRow = mlResourceUtilDataList.iterator(); itrTableRow.hasNext();) 
            {
                mapTableRow = (Map) itrTableRow.next();
                strResourcePoolId = (String)mapTableRow.get("ResourcePoolId");
                mapColumnsData = (Map)mapMonthYearValues.get(strResourcePoolId);
                mapTableRow.put("Columns Data", mapColumnsData);
            }
            return mlResourceUtilDataList;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
    
    /**
     * @param context
     * @param slMonthYear
     * @param strResourcePoolIds
     * @param slRowHeaderNameList
     * @return
     * @throws MatrixException
     */
    private Map getResourceDemandSummaryReportData(Context context,StringList slMonthYear, String[] strResourcePoolIds, StringList slRowHeaderNameList) throws MatrixException 
    {
        try 
        {
            final String SELECT_RESOURCE_POOL_ID = DomainConstants.SELECT_ID;
            final String SELECT_REL_TO_RESOURCE_POOL_RESOURCE_REQUEST_ID = "to["+DomainConstants.RELATIONSHIP_RESOURCE_POOL+"].businessobject."+DomainConstants.SELECT_ID;
            final String SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE = "to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].attribute["+DomainConstants.ATTRIBUTE_FTE+"].value";
            final String SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE = "from["+DomainConstants.RELATIONSHIP_ALLOCATED+"].attribute["+DomainConstants.ATTRIBUTE_FTE+"].value";
            final String SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE = "from["+DomainConstants.RELATIONSHIP_ALLOCATED+"].attribute["+DomainConstants.ATTRIBUTE_RESOURCE_STATE+"].value";
            
            Map mapRowNameMonthYearValue   = new HashMap();         
            Map mapMonthYearValue = null;
                    
            StringList slSelectList = new StringList();
            slSelectList.add(SELECT_RESOURCE_POOL_ID);
            slSelectList.add(SELECT_REL_TO_RESOURCE_POOL_RESOURCE_REQUEST_ID);
            BusinessObjectWithSelectList resourcePoolObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strResourcePoolIds,slSelectList);
            BusinessObjectWithSelectList resourceRequestsObjWithSelectList = null;
            BusinessObjectWithSelect bowsResourcePool = null;
            BusinessObjectWithSelect bowsResourceRequest = null;
            Map mapRequestRelResourcePlanData = new HashMap();
            String strFTEPlanValue = "";
            String strResourcePoolId = "";
            StringList slResourcePoolResourceRequestIdList = null;
            
            slSelectList.clear();
            slSelectList.add(SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE);
            slSelectList.add(DomainConstants.SELECT_CURRENT);
            slSelectList.add(SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE);
            slSelectList.add(SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE);
               
            StringList slFTEDataList = null;
            StringList slRelAllocatedResourceStateList = null;
            String strRelAllocatedResourceState = "";
            
            for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourcePoolObjWithSelectList); itr.next();)
            {
                bowsResourcePool = itr.obj();
                strResourcePoolId = bowsResourcePool.getSelectData(SELECT_RESOURCE_POOL_ID);
                slResourcePoolResourceRequestIdList = bowsResourcePool.getSelectDataList(SELECT_REL_TO_RESOURCE_POOL_RESOURCE_REQUEST_ID);
                if(null==slResourcePoolResourceRequestIdList || slResourcePoolResourceRequestIdList.size()==0)
                {
                    mapMonthYearValue = new HashMap();
                    strFTEPlanValue = "";
                    mapMonthYearValue = getResourceRequestsPerMonthMap(strFTEPlanValue, mapMonthYearValue,slMonthYear);
                    mapRequestRelResourcePlanData.put(strResourcePoolId, mapMonthYearValue);
                    continue;
                }
                String[] strRequestIds = new String[0];
                if(null!=slResourcePoolResourceRequestIdList)
                {
                	strRequestIds = new String[slResourcePoolResourceRequestIdList.size()];
                    slResourcePoolResourceRequestIdList.copyInto(strRequestIds);
                }
                
              //Start : Modified by ixe for IR-019137V6R2011
                BusinessObjectWithSelect bows = null;
                final String SELECT_REL_RESOURCE_PLAN_PROJECT_ID = "to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].from."+DomainConstants.SELECT_ID;
            	StringList slProjectSelectList = new StringList();
            	slProjectSelectList.add(SELECT_REL_RESOURCE_PLAN_PROJECT_ID);
            	slProjectSelectList.add("to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].from."+DomainConstants.SELECT_CURRENT);
            	resourceRequestsObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strRequestIds,slProjectSelectList);
            	for(BusinessObjectWithSelectItr nitr= new BusinessObjectWithSelectItr(resourceRequestsObjWithSelectList); nitr.next();)
            	{
            		String strProjectId = "";
            		String strCurrentState = "";
            		bows = nitr.obj();
            		strProjectId = bows.getSelectData(SELECT_REL_RESOURCE_PLAN_PROJECT_ID);
            		String strProjectState = bows.getSelectData("to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].from."+DomainConstants.SELECT_CURRENT);
            			if(!isActiveProject(strProjectState)){
            				slResourcePoolResourceRequestIdList.remove(bows.getObjectId()) ;
            			}
            	}
            	//End : Modified by ixe for IR-019137V6R2011
                             
                resourceRequestsObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strRequestIds,slSelectList);
              //  for(BusinessObjectWithSelectItr itrRequestObj= new BusinessObjectWithSelectItr(resourceRequestsObjWithSelectList); itrRequestObj.next();)
              //  {
               // 	bowsResourceRequest = itrRequestObj.obj();
                for(StringItr stringItr = new StringItr(slResourcePoolResourceRequestIdList); stringItr.next();)
            	{
            		String strRequestID = stringItr.obj();
            			DomainObject dmoRequest =DomainObject.newInstance(context, strRequestID);
            			 String strCurrentState =  dmoRequest.getInfo(context,DomainConstants.SELECT_CURRENT);
                    //String strCurrentState = bowsResourceRequest.getSelectData(DomainConstants.SELECT_CURRENT);
                    if(strCurrentState.equals(DomainConstants.STATE_RESOURCE_REQUEST_REQUESTED)||strCurrentState.equals(DomainConstants.STATE_RESOURCE_REQUEST_PROPOSED))
                    {
                    	if(null==mapRequestRelResourcePlanData.get(strResourcePoolId))
                        {
                            mapMonthYearValue = new HashMap();
                        }
                        else
                        {   
                            mapMonthYearValue = (Map)mapRequestRelResourcePlanData.get(strResourcePoolId);
                        }
                       // strFTEPlanValue = bowsResourceRequest.getSelectData(SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE);
                    	strFTEPlanValue =  dmoRequest.getInfo(context,SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE);
                        mapMonthYearValue = getResourceRequestsPerMonthMap(strFTEPlanValue, mapMonthYearValue,slMonthYear);
                        mapRequestRelResourcePlanData.put(strResourcePoolId, mapMonthYearValue);
                    }
                    
                   // slRelAllocatedResourceStateList = bowsResourceRequest.getSelectDataList(SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE);
                    slRelAllocatedResourceStateList = dmoRequest.getInfoList(context,SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE);
                    if(null!=slRelAllocatedResourceStateList)
                    {
                        for(int iStateIndex=0; iStateIndex<slRelAllocatedResourceStateList.size(); iStateIndex++)
                        {
                            strRelAllocatedResourceState = (String)slRelAllocatedResourceStateList.get(iStateIndex);
                            if(strRelAllocatedResourceState.equalsIgnoreCase(DomainConstants.ATTRIBUTE_RESOURCE_STATE_RANGE_COMMITTED))
                            {
	                            if(null==mapRequestRelResourcePlanData.get(strResourcePoolId))
	                            {
	                                mapMonthYearValue = new HashMap();
	                            }
	                            else
	                            {   
	                                mapMonthYearValue = (Map)mapRequestRelResourcePlanData.get(strResourcePoolId);
	                            }
	                            slFTEDataList = (StringList)dmoRequest.getInfoList(context,SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE);
	                            if(null!=slFTEDataList)
	                            {
	                                strFTEPlanValue = (String)slFTEDataList.get(iStateIndex);
	                                mapMonthYearValue = getResourceRequestsPerMonthMap(strFTEPlanValue, mapMonthYearValue,slMonthYear);
	                                mapRequestRelResourcePlanData.put(strResourcePoolId, mapMonthYearValue);
	                            }
                            }
                        }
                    }
                }
            }
            final String SELECT_REL_MEMBER_PERSON_ID = "from["+DomainConstants.RELATIONSHIP_MEMBER+"].businessobject."+DomainConstants.SELECT_ID;
            slSelectList.clear();
            slSelectList.add(SELECT_REL_MEMBER_PERSON_ID);
            double nPeopleSize = 0;
            Map mapQueryWithDataList = getResourcePoolsDataWithSelectList(context, strResourcePoolIds, slSelectList, true);
            StringList slPersonIdList = (StringList)mapQueryWithDataList.get(SELECT_REL_MEMBER_PERSON_ID);
            Map mapMonthYearFTECapacity = new HashMap();
            StringList slFilterPersonIdList = new StringList();
            if(null!=slPersonIdList && slPersonIdList.size()>0)
            {
            	StringList slPMCUserList = getPMCUser(context);
            	for(int index=0; index<slPersonIdList.size(); index++)
            	{
            		String strPersonId = (String)slPersonIdList.get(index);
            		if(slPMCUserList.contains(strPersonId))
            		{
            			slFilterPersonIdList.add(strPersonId);
            		}
            	}
                nPeopleSize = slFilterPersonIdList.size();
            }
            mapMonthYearValue = new HashMap();
            String numberofPeopleUnit=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.NumberofPeopleUnit");
            double nHoursPerDay = FTE.getHoursPerDay(context);
            FTE fte = FTE.getInstance(context);
            for(int i=0; i<slMonthYear.size(); i++)
            {
            	double dOutputValue = nPeopleSize;
            	if("Hours".equalsIgnoreCase(numberofPeopleUnit))
            	{
            		long duration = DateUtil.computeDuration(fte.getStartDate((String)slMonthYear.get(i)),fte.getEndDate((String)slMonthYear.get(i)));
            		dOutputValue = (nPeopleSize*nHoursPerDay*duration);
            	}
            	mapMonthYearValue.put(slMonthYear.get(i), String.valueOf(dOutputValue));
            }
            mapRequestRelResourcePlanData.put(STRING_CAPACITY, mapMonthYearValue);
            updateToRoundingValue(mapRequestRelResourcePlanData);
            return mapRequestRelResourcePlanData;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
    
    private void updateToRoundingValue(Map mapRequestRelResourcePlanData) throws MatrixException
    {
    	NumberFormat numberFormat=NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setGroupingUsed(false);
       
    	for (Iterator itr = mapRequestRelResourcePlanData.keySet().iterator(); itr.hasNext();) 
    	{
			String strKey = (String) itr.next();
			Map mapMonthYearValue = (Map)mapRequestRelResourcePlanData.get(strKey);
			for (Iterator iterator = mapMonthYearValue.keySet().iterator(); iterator.hasNext();) 
			{
				String strTimeline = (String) iterator.next();
				Double nTimeLineValue = Task.parseToDouble((String)mapMonthYearValue.get(strTimeline));
				mapMonthYearValue.put(strTimeline, numberFormat.format(nTimeLineValue));
			}
		}
    }
    
    /**
     * @param strFTEValue
     * @param mapMonthYearValue
     * @return
     * @throws MatrixException
     */
    public Map getResourceRequestsPerMonthMap(String strFTEValue, Map mapMonthYearValue, StringList slMonthYearList) throws MatrixException
    {
        try
        {
            String strMonthYearValue = "";
            Double dFTEValue = null;
            StringList slFTEMonthYearList = new StringList();
            Map mapAllFTEValue = new HashMap();
            if (strFTEValue != null && !"".equals(strFTEValue) && !"null".equals(strFTEValue)) 
            {
                FTE fteRequest = FTE.getInstance(context, strFTEValue);
                mapAllFTEValue =  getCalculatedMonthFTEMap(strFTEValue, mapAllFTEValue);
                String strStartMonthYear = fteRequest.getStartTimeframe();
                String strEndMonthYear = fteRequest.getEndTimeframe();
                if ((strStartMonthYear != null && !"".equals(strStartMonthYear) && !"null".equals(strStartMonthYear))
                        && (strEndMonthYear != null && !"".equals(strEndMonthYear) && !"null".equals(strEndMonthYear))) 
                {
                    StringList slStartMonthYearList = FrameworkUtil.split(strStartMonthYear,"-");
                    StringList slEndMonthYearList = FrameworkUtil.split(strEndMonthYear,"-");
                    
                    int nStartMonth = Integer.parseInt((String)slStartMonthYearList.get(0));
                    int nStartYear = Integer.parseInt((String)slStartMonthYearList.get(1));
                    int nEndMonth = Integer.parseInt((String)slEndMonthYearList.get(0));
                    int nEndYear = Integer.parseInt((String)slEndMonthYearList.get(1));
                    
                    Date startDate = fteRequest.getStartDate(nStartMonth+"-"+nStartYear);
                    Calendar calStartDate = Calendar.getInstance();
                    calStartDate.setTime(startDate);
                    Date endDate = fteRequest.getEndDate(nEndMonth+"-"+nEndYear);
                    Calendar calEndDate = Calendar.getInstance();
                    calEndDate.setTime(endDate);
                    slFTEMonthYearList = getTimeframes(calStartDate, calEndDate);
                }
            }
            for (int i=0; i<slMonthYearList.size(); i++)
            {
                strMonthYearValue = (String)slMonthYearList.get(i);
                dFTEValue = new Double(0);
                Double dActualFTEValue = new Double(0);
                if(null!=mapMonthYearValue.get(strMonthYearValue))
                {
                    dFTEValue = new Double(Task.parseToDouble((String)mapMonthYearValue.get(strMonthYearValue)));
                }
                if(null!=mapAllFTEValue.get(strMonthYearValue))
                {
                    dActualFTEValue = (Double)mapAllFTEValue.get(strMonthYearValue);
                }
                if(slFTEMonthYearList.contains(strMonthYearValue))
                {
                    dFTEValue = new Double(dFTEValue.doubleValue()+ dActualFTEValue.doubleValue());
                }
                mapMonthYearValue.put(strMonthYearValue,String.valueOf(dFTEValue.doubleValue()));
            }
            return mapMonthYearValue;
        }
        catch (JDOMException jdome) 
        {
            jdome.printStackTrace();
            throw new MatrixException(jdome);
        } 
        catch (IOException ioe) 
        {
            ioe.printStackTrace();
            throw new MatrixException(ioe);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            throw new MatrixException(e);
        }
    }
    public static String getQuarters(int nMonth)
    {  
        String nQuart  = "0";
        switch (nMonth) 
        {
            case 0:
            	nQuart  = "1";
                break;
            case 1:
            	nQuart  = "1";
                break;
            case 2:
            	nQuart  = "1";
                break;
            case 3:
            	nQuart  = "2";
                break;
            case 4:
            	nQuart  = "2";
                break;                
            case 5:
            	nQuart  = "2";
                break;
            case 6:
            	nQuart  = "3";
                break;
            case 7:
            	nQuart  = "3";
                break;
            case 8:
            	nQuart  = "3";
                break;
            case 9:
            	nQuart  = "4";
                break;
            case 10:
            	nQuart  = "4";
                break;
            case 11:
            	nQuart  = "4";
                break;
            default:
            	nQuart  = "0";
            	break;
        }
        return nQuart;
    }
    
    /**
     * @param context
     * @param args
     * @return
     * @throws MatrixException
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTableProjectAssignmentReportData(Context context, String[] args)
    throws MatrixException 
    {   
        MapList mlResourceUtilDataList  = new MapList();
        try 
        {            
            if (context == null) 
            {
                throw new IllegalArgumentException("context");
            }
            
            Map programMap = (Map) JPO.unpackArgs(args);
            Map requestValuesMap = (Map) programMap.get("RequestValuesMap");
            
            String[] strTableRowIdValues =  (String[])requestValuesMap.get("ResourcePoolId");
            String strResourceObjectId = strTableRowIdValues[0];
            StringList slTableRowIdValuesSplit = FrameworkUtil.split(strResourceObjectId, ",");
            String[] strResourcePoolIds = new String[slTableRowIdValuesSplit.size()];
            slTableRowIdValuesSplit.copyInto(strResourcePoolIds);
            
            StringList slSelectList = new StringList();
            final String SELECT_REL_TO_RESOURCE_POOL_RESOURCE_PLAN_PROJECT_ID = "to["+DomainConstants.RELATIONSHIP_RESOURCE_POOL+"].businessobject.to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].businessobject."+DomainConstants.SELECT_ID;
            slSelectList.add(SELECT_REL_TO_RESOURCE_POOL_RESOURCE_PLAN_PROJECT_ID);
            Map mapQueryWithDataList = getResourcePoolsDataWithSelectList(context, strResourcePoolIds, slSelectList, true);
            StringList slProjectIdList = (StringList)mapQueryWithDataList.get(SELECT_REL_TO_RESOURCE_POOL_RESOURCE_PLAN_PROJECT_ID);
            //Modified by ixe for IR-019137V6R2011
            /*for(StringItr stringItr = new StringItr(slProjectIdList); stringItr.next();)
        	{
            	String strProjectId = stringItr.obj();
        			DomainObject dmoProject =DomainObject.newInstance(context, strProjectId);
        			String strProjectState = dmoProject.getInfo(context, DomainConstants.SELECT_CURRENT);
        			if(!isActiveProject(strProjectState)){
        				slProjectIdList.remove(strProjectId) ;
        			}
        	}*/
            if (slProjectIdList != null && !slProjectIdList.isEmpty()) {
	            for(int i=0;i<slProjectIdList.size();i++) {
	            	String strProjectId = (String)slProjectIdList.get(i);
	        			DomainObject dmoProject =DomainObject.newInstance(context, strProjectId);
	        			String strProjectState = dmoProject.getInfo(context, DomainConstants.SELECT_CURRENT);
	        			if(!isActiveProject(strProjectState)){
	        				slProjectIdList.remove(strProjectId) ;
	        			}
	        	}
            }
            //modification end
            slSelectList.clear();
            slSelectList.add(DomainConstants.SELECT_NAME);
            String []strProjectIds = new String[0];
            if(null!=slProjectIdList)
            {
            	strProjectIds = new String[slProjectIdList.size()];
                slProjectIdList.copyInto(strProjectIds);
                Map mapProjectNameList = getResourcePoolsDataWithSelectList(context, strProjectIds, slSelectList, false);
                StringList slProjectNameList = (StringList)mapProjectNameList.get(DomainConstants.SELECT_NAME);
            
            	Map mapTableRow = null;
            	for(int i=0; i<slProjectNameList.size(); i++)
            	{
	                mapTableRow = new HashMap();
	                //Added for special character.
	                String projectName	=	(String)slProjectNameList.get(i);
	                String prjHyperLnk = getProgramCentralTableTreeLink(context, DomainConstants.TYPE_PROJECT_SPACE, (String)slProjectIdList.get(i),XSSUtil.encodeForHTML(context, projectName), true); 
	                mapTableRow.put(STRING_TITLE,projectName);
	                mapTableRow.put(STRING_I18N_TITLE, prjHyperLnk);
	                mapTableRow.put("Id", slProjectIdList.get(i));
	                mlResourceUtilDataList.add(mapTableRow);
	            }
	            String strFromDate = "";
	            String strToDate = "";
	            strFromDate = null!=requestValuesMap.get("PMCCustomFilterFromDate")?((String[])requestValuesMap.get("PMCCustomFilterFromDate"))[0]:"";
	            strToDate   = null!=requestValuesMap.get("PMCCustomFilterToDate")?((String[])requestValuesMap.get("PMCCustomFilterToDate"))[0]:"";
	            StringList slMonthYearList = getTimeframes(strFromDate, strToDate);
	            
	            Map mapMonthYearValues = getProjectAssignmentReportData(context,slMonthYearList,strProjectIds,slProjectNameList);
	            String strTitle = "";
	            String strId = "";
	            Map mapColumnsData = null;
	            for (Iterator itrTableRow = mlResourceUtilDataList.iterator(); itrTableRow.hasNext();) 
	            {
	                mapTableRow = (Map) itrTableRow.next();
	                strTitle = (String)mapTableRow.get(STRING_TITLE);
	                strId = (String)mapTableRow.get("Id");
	                mapColumnsData = (Map)mapMonthYearValues.get(strId);
	                mapTableRow.put("Columns Data", mapColumnsData);
	            }
            }
            return mlResourceUtilDataList;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
    
    /**
     * @param context
     * @param slMonthYear
     * @param strResourcePoolIds
     * @param slRowHeaderNameList
     * @return
     * @throws MatrixException
     */
    private Map getProjectAssignmentReportData(Context context,StringList slMonthYear, String[] strProjectIds, StringList slRowHeaderNameList) throws MatrixException 
    {
        try 
        {
            final String SELECT_PROJECT_ID = DomainConstants.SELECT_ID;
            final String SELECT_REL_TO_RESOURCE_PLAN_RESOURCE_REQUEST_ID = "from["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].businessobject."+DomainConstants.SELECT_ID;
            final String SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE = "from["+DomainConstants.RELATIONSHIP_ALLOCATED+"].attribute["+DomainConstants.ATTRIBUTE_FTE+"].value";
            final String SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE = "from["+DomainConstants.RELATIONSHIP_ALLOCATED+"].attribute["+DomainConstants.ATTRIBUTE_RESOURCE_STATE+"].value";
          
            Map mapRowNameMonthYearValue   = new HashMap();         
            Map mapMonthYearValue = null;
                    
            StringList slSelectList = new StringList();
            slSelectList.add(SELECT_PROJECT_ID);
            slSelectList.add(SELECT_REL_TO_RESOURCE_PLAN_RESOURCE_REQUEST_ID);
            BusinessObjectWithSelectList projectObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strProjectIds,slSelectList);
            BusinessObjectWithSelectList resourceRequestsObjWithSelectList = null;
            BusinessObjectWithSelect bowsProject = null;
            BusinessObjectWithSelect bowsResourceRequest = null;
            Map mapRequestRelResourcePlanData = new HashMap();
            String strFTEPlanValue = "";
            String strProjectId = "";
            String strRelAllocatedResourceState = "";
            StringList slProjectResourceRequestIdList = null;
            StringList slRelAllocatedResourceStateList = null;
            
            slSelectList.clear();
            slSelectList.add(SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE);
            slSelectList.add(SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE);
            int i1=0;
            for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(projectObjWithSelectList); itr.next();)
            {
                bowsProject = itr.obj();
                i1++;
                strProjectId = bowsProject.getSelectData(SELECT_PROJECT_ID);
                slProjectResourceRequestIdList = bowsProject.getSelectDataList(SELECT_REL_TO_RESOURCE_PLAN_RESOURCE_REQUEST_ID);
                String[] strRequestIds = new String[0];
                if(null!=slProjectResourceRequestIdList)
                {
                	strRequestIds = new String[slProjectResourceRequestIdList.size()];
                	slProjectResourceRequestIdList.copyInto(strRequestIds);
                }
                
                
                resourceRequestsObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strRequestIds,slSelectList);
                for(BusinessObjectWithSelectItr itrRequestObj= new BusinessObjectWithSelectItr(resourceRequestsObjWithSelectList); itrRequestObj.next();)
                {
                    bowsResourceRequest = itrRequestObj.obj();
                    slRelAllocatedResourceStateList = bowsResourceRequest.getSelectDataList(SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE);
                    if(null==mapRequestRelResourcePlanData.get(strProjectId))
                    {
                        mapMonthYearValue = new HashMap();
                    }
                    else
                    {   
                        mapMonthYearValue = (Map)mapRequestRelResourcePlanData.get(strProjectId);
                    }
                    if(null!=slRelAllocatedResourceStateList)
                    {
                        StringList slFTEDataList = (StringList)bowsResourceRequest.getSelectDataList(SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE);
                        for(int iStateIndex=0; iStateIndex<slRelAllocatedResourceStateList.size(); iStateIndex++)
                        {
                            strRelAllocatedResourceState = (String)slRelAllocatedResourceStateList.get(iStateIndex);
                            strFTEPlanValue = null;
                            if(null!=slFTEDataList && null!=slFTEDataList.get(iStateIndex)
                                    && strRelAllocatedResourceState.equals(DomainConstants.ATTRIBUTE_RESOURCE_STATE_RANGE_COMMITTED))
                            {
                                strFTEPlanValue = (String)slFTEDataList.get(iStateIndex);
                            }
                            mapMonthYearValue = getResourceRequestsPerMonthMap(strFTEPlanValue, mapMonthYearValue,slMonthYear);
                            mapRequestRelResourcePlanData.put(strProjectId, mapMonthYearValue);
                        }
                    }
                    else
                    {
                        strFTEPlanValue = null;
                        mapMonthYearValue = getResourceRequestsPerMonthMap(strFTEPlanValue, mapMonthYearValue,slMonthYear);
                        mapRequestRelResourcePlanData.put(strProjectId, mapMonthYearValue);
                    }
                }
            }
            return mapRequestRelResourcePlanData;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
    public boolean CheckChartingRequired(Context context, String[] args) throws MatrixException
    {
    	boolean isChartingRequired = true;
    	try 
    	{
    		if (context == null) 
    		{
    			throw new IllegalArgumentException("context");
    		}
    		Map programMap = (Map) JPO.unpackArgs(args);
    		String strChartingRequiredCheck = (String)programMap.get("chartingrequired");
    		if(null!=strChartingRequiredCheck)
    		{
    			isChartingRequired = "true".equalsIgnoreCase(strChartingRequiredCheck)?true:false;
    		}
    	} 
    	catch (Exception exp) 
    	{
    		exp.printStackTrace();
    		throw new MatrixException(exp);
    	}
    	return isChartingRequired;
    }
    
    /**
     * @param context
     * @param args
     * @return
     * @throws MatrixException
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTablePeopleProjectAssignmentReportData(Context context, String[] args)
    throws MatrixException 
    {   
        MapList mlResourceUtilDataList  = new MapList();
        try 
        {            
            if (context == null) 
            {
                throw new IllegalArgumentException("context");
            }
            Map programMap = (Map) JPO.unpackArgs(args);
            Map requestMap = (Map) programMap.get("requestMap");
            String strLanguage = (String)programMap.get("languageStr");
            Map paramList = (Map)programMap.get("paramList");
            Map requestValuesMap = (Map) programMap.get("RequestValuesMap");
            String strSelectedOverallFilter = ((String[])requestValuesMap.get("PMCReportPeopleProjectAssignementFilter"))[0];
		    
            String[] strTableRowIdValues =  (String[])requestValuesMap.get("ResourcePoolId");
            String strResourceObjectId = strTableRowIdValues[0];
            StringList slTableRowIdValuesSplit = FrameworkUtil.split(strResourceObjectId, ",");
            String[] strResourceIds = new String[slTableRowIdValuesSplit.size()];
            slTableRowIdValuesSplit.copyInto(strResourceIds);
            
            StringList slProjectIdList = null;
        	StringList slDistinctProjectIdList = new StringList();
        	Map mapQueryWithDataList = null;
        	Map mapMonthYearValues = null;
        	String strPersonId = "";

        	String strFromDate = "";
            String strToDate = "";
            strFromDate = null!=requestValuesMap.get("PMCCustomFilterFromDate")?((String[])requestValuesMap.get("PMCCustomFilterFromDate"))[0]:"";
            strToDate   = null!=requestValuesMap.get("PMCCustomFilterToDate")?((String[])requestValuesMap.get("PMCCustomFilterToDate"))[0]:"";
            StringList slMonthYearList = getTimeframes(strFromDate, strToDate);
        	
            StringList slStateList = new StringList();
            slStateList.add(DomainConstants.ATTRIBUTE_RESOURCE_STATE_RANGE_COMMITTED);
            slStateList.add(DomainConstants.ATTRIBUTE_RESOURCE_STATE_RANGE_PROPOSED);
            slStateList.add(DomainConstants.ATTRIBUTE_RESOURCE_STATE_RANGE_REQUESTED);
            StringList slSelectList = new StringList();
            
            final String SELECT_REL_TO_ALLOCATED_RESOURCE_PLAN_PROJECT_ID = "to["+DomainConstants.RELATIONSHIP_ALLOCATED+"].businessobject.to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].businessobject."+DomainConstants.SELECT_ID;
            final String SELECT_REL_TO_ALLOCATED_RESOURCE_PLAN_PROJECT_NAME = "to["+DomainConstants.RELATIONSHIP_ALLOCATED+"].businessobject.to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].businessobject."+DomainConstants.SELECT_NAME;
            
            slSelectList.add(DomainConstants.SELECT_NAME);
            slSelectList.add(SELECT_REL_TO_ALLOCATED_RESOURCE_PLAN_PROJECT_ID);
            slSelectList.add(SELECT_REL_TO_ALLOCATED_RESOURCE_PLAN_PROJECT_NAME);
            
            Map mapObjectIdWithQueryDataList = getBusinessObjectDataWithSelectList(context, strResourceIds, slSelectList);
            Map mapPersonIdWithProjectIdList = new HashMap();
            Map mapProjectMemberRelInfoList = getPersonProjectMemberAllocationData(context, strResourceIds,slMonthYearList);
            if(null!=mapObjectIdWithQueryDataList)
            {
            	slProjectIdList = null;
            	slDistinctProjectIdList = new StringList();
            	mapQueryWithDataList = null;
            	strPersonId = "";
            	for (Iterator iterator = mapObjectIdWithQueryDataList.keySet().iterator(); iterator.hasNext();) 
            	{
            		strPersonId = (String)iterator.next();
            		mapQueryWithDataList = (Map)mapObjectIdWithQueryDataList.get(strPersonId);
            		slProjectIdList = (StringList)mapQueryWithDataList.get(SELECT_REL_TO_ALLOCATED_RESOURCE_PLAN_PROJECT_ID);
            		if(null!=slProjectIdList && slProjectIdList.size()>0)
            		{
            			for(int index=0;index<slProjectIdList.size();index++)
            			{
            				if(!slDistinctProjectIdList.contains(slProjectIdList.get(index)))
            				{
            					slDistinctProjectIdList.add(slProjectIdList.get(index));
            				}
            			}
            		}
            	}
	            String[] strProjectIds = new String[slDistinctProjectIdList.size()];
	            slDistinctProjectIdList.copyInto(strProjectIds);
	            mapMonthYearValues = getPeopleProjectAssignmentReportData(context,slMonthYearList,strProjectIds, strSelectedOverallFilter);
            }
        	Map mapTableRow = null;
            Map mapColumnsData = null;
            StringList slProjectNameList = null;
            String strDataKey = "";
            String strPersonName = "";
            
            for (Iterator iterator = mapObjectIdWithQueryDataList.keySet().iterator(); iterator.hasNext();) 
        	{
        		strPersonId = (String)iterator.next();
        		mapQueryWithDataList = (Map)mapObjectIdWithQueryDataList.get(strPersonId);
        		strPersonName = (String)(((StringList)mapQueryWithDataList.get(DomainConstants.SELECT_NAME)).get(0));
        		strPersonName = PersonUtil.getFullName(context, strPersonName); 
			    boolean isPersonNameDisplayed = false;
			    String strPersonFullName = getPersonHRefLink(context, strPersonId, strPersonName);
        		slProjectIdList = new StringList();
        		if(null==strSelectedOverallFilter || "".equals(strSelectedOverallFilter) || "null".equals(strSelectedOverallFilter) || strSelectedOverallFilter.equals("Detail"))
    		    {
        			isPersonNameDisplayed = false;
        			if(null!=mapQueryWithDataList.get(SELECT_REL_TO_ALLOCATED_RESOURCE_PLAN_PROJECT_NAME))
        			{
        				slProjectNameList = (StringList)mapQueryWithDataList.get(SELECT_REL_TO_ALLOCATED_RESOURCE_PLAN_PROJECT_NAME);
        				slProjectIdList = (StringList)mapQueryWithDataList.get(SELECT_REL_TO_ALLOCATED_RESOURCE_PLAN_PROJECT_ID);
	            		if(null!=slProjectIdList && slProjectIdList.size()>0)
	            		{
	            			StringList slTempProjectIdList = new StringList();
	            			for(int i=0; i<slProjectIdList.size(); i++)
	            			{
		            			strDataKey = "";
		            			if(slTempProjectIdList.contains((String)slProjectIdList.get(i)))
		            			{
		            				continue;
		            			}
		            			slTempProjectIdList.add((String)slProjectIdList.get(i));
		                		mapColumnsData = null;
		            			for(int j=0; j<slStateList.size();j++)
		            			{
			            			strDataKey = strPersonId+"$"+slProjectIdList.get(i)+"$"+slStateList.get(j);
			            			mapColumnsData = (Map)mapMonthYearValues.get(strDataKey);
			            			if(null!=mapColumnsData)
			            			{	         
			            				mapTableRow = new HashMap();
			            				if(isPersonNameDisplayed)
			            				{
			            					mapTableRow.put("PersonId", "");
			            					mapTableRow.put("PersonName", "");
			            				}
			            				else
			            				{
			            					mapTableRow.put("PersonId", strPersonId);
			            					mapTableRow.put("PersonName", strPersonFullName);
			            					isPersonNameDisplayed = true;
			            				}
			            				String strProjectName = getProgramCentralTableTreeLink(context, DomainConstants.TYPE_PROJECT_SPACE, (String)slProjectIdList.get(i), (String)slProjectNameList.get(i), true);
			            				mapTableRow.put("ProjectTitle", slProjectNameList.get(i));
			            				mapTableRow.put("Project i18n Title", strProjectName);
			            				mapTableRow.put("State", slStateList.get(j));
			            				mapTableRow.put("ProjectId", slProjectIdList.get(i));
			        	                mapTableRow.put("Columns Data", mapColumnsData);
			            				mlResourceUtilDataList.add(mapTableRow);
			            				
			            			}
		            			}
	            		    }
	            		}
	    		    }
        			if(null!=mapProjectMemberRelInfoList.get(strPersonId))
        			{
        				Map mapProjectMemberRelProjectinfoList = (Map)mapProjectMemberRelInfoList.get(strPersonId); 
        				StringList slMemberProjectIdList = (StringList)mapProjectMemberRelProjectinfoList.get("ProjectIds");
        				StringList slMemberProjectNameList = (StringList)mapProjectMemberRelProjectinfoList.get("ProjectNames");
        				mapColumnsData = (Map)mapProjectMemberRelProjectinfoList.get("Columns Data");
        				for(int i=0; i<slMemberProjectIdList.size(); i++)
        				{
        					String strProjectId = (String)slMemberProjectIdList.get(i);
        					if(!slProjectIdList.contains(strProjectId))
        					{
	            				mapTableRow = new HashMap();
	            				if(isPersonNameDisplayed)
	            				{
	            					mapTableRow.put("PersonId", "");
	            					mapTableRow.put("PersonName", "");
	            				}
	            				else
	            				{
	            					mapTableRow.put("PersonId", strPersonId);
	            					mapTableRow.put("PersonName", strPersonFullName);
	            					isPersonNameDisplayed = true;
	            				}
	            				String strProjectName = getProgramCentralTableTreeLink(context, DomainConstants.TYPE_PROJECT_SPACE, strProjectId, (String)slMemberProjectNameList.get(i), true);
	            				mapTableRow.put("ProjectTitle", slMemberProjectNameList.get(i));
	            				mapTableRow.put("Project i18n Title", strProjectName);
	            				mapTableRow.put("State", "");
	            				mapTableRow.put("ProjectId", strProjectId);
	        	                mapTableRow.put("Columns Data", mapColumnsData);
	            				mlResourceUtilDataList.add(mapTableRow);
        					}
        				}
        			}
    		    }
	    		strDataKey = strPersonId+"$"+"Total";
	    		mapColumnsData = (Map)mapMonthYearValues.get(strDataKey);
				if(null!=mapColumnsData)
				{	         
					mapTableRow = new HashMap();
					if(isPersonNameDisplayed)
					{
						mapTableRow.put("PersonId", "");
						mapTableRow.put("PersonName", "");
					}
					else
					{
						mapTableRow.put("PersonId", strPersonId);
						mapTableRow.put("PersonName", strPersonFullName);
					}
					mapTableRow.put("ProjectTitle", "Total");
					String sTotal = EnoviaResourceBundle.getProperty(context, ProgramCentralConstants.PROGRAMCENTRAL,
							"emxProgramCentral.Common.Total",strLanguage);
					mapTableRow.put("Project i18n Title", sTotal);
					mapTableRow.put("State", "");
					mapTableRow.put("ProjectId", "");
	                mapTableRow.put("Columns Data", mapColumnsData);
					mlResourceUtilDataList.add(mapTableRow);
				}
        	}
            return mlResourceUtilDataList;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
    
    /**
     * @param context
     * @param slMonthYear
     * @param strResourcePoolIds
     * @param slRowHeaderNameList
     * @return
     * @throws MatrixException
     */
    private Map getPeopleProjectAssignmentReportData(Context context,StringList slMonthYear, String[] strProjectIds, String strSelectedOverallFilter) throws MatrixException 
    {
        try 
        {
            final String SELECT_PROJECT_ID = DomainConstants.SELECT_ID;
            final String SELECT_REL_FROM_RESOURCE_PLAN_RESOURCE_REQUEST_ID = "from["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].businessobject."+DomainConstants.SELECT_ID;
            final String SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE = "from["+DomainConstants.RELATIONSHIP_ALLOCATED+"].attribute["+DomainConstants.ATTRIBUTE_FTE+"].value";
            final String SELECT_REL_FROM_ALLOCATED_RESOURCE_ID = "from["+DomainConstants.RELATIONSHIP_ALLOCATED+"].businessobject."+DomainConstants.SELECT_ID;
            final String SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE = "from["+DomainConstants.RELATIONSHIP_ALLOCATED+"].attribute["+DomainConstants.ATTRIBUTE_RESOURCE_STATE+"].value";
          
            Map mapRowNameMonthYearValue   = new HashMap();         
            Map mapMonthYearValue = new HashMap();
                    
            StringList slSelectList = new StringList();
            slSelectList.add(SELECT_PROJECT_ID);
            slSelectList.add(SELECT_REL_FROM_RESOURCE_PLAN_RESOURCE_REQUEST_ID);
            BusinessObjectWithSelectList projectObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strProjectIds,slSelectList);
            BusinessObjectWithSelectList resourceRequestsObjWithSelectList = null;
            BusinessObjectWithSelect bowsProject = null;
            BusinessObjectWithSelect bowsResourceRequest = null;
            Map mapRequestRelAllocationData = new HashMap();
            Map mapProjectWiseAllocationData = new HashMap();
            //Map mapPersonAllocationData = new HashMap();
            String strFTEPlanValue = "";
            String strProjectId = "";
            String strRelAllocatedResourceState = "";
            StringList slProjectResourceRequestIdList = null;
            StringList slRelAllocatedResourceStateList = null;
            StringList slAllocatedResourceList = null;
            Map mapRequestRelTimeLineData = new HashMap();
            slSelectList.clear();
            slSelectList.add(SELECT_REL_FROM_ALLOCATED_RESOURCE_ID);
            slSelectList.add(SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE);
            slSelectList.add(SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE);
            int i1=0;
            String strPersonId = "";
            for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(projectObjWithSelectList); itr.next();)
            {
                bowsProject = itr.obj();
                i1++;
                strProjectId = bowsProject.getSelectData(SELECT_PROJECT_ID);
                slProjectResourceRequestIdList = bowsProject.getSelectDataList(SELECT_REL_FROM_RESOURCE_PLAN_RESOURCE_REQUEST_ID);
                String[] strRequestIds = new String[slProjectResourceRequestIdList.size()];
                slProjectResourceRequestIdList.copyInto(strRequestIds);
                resourceRequestsObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strRequestIds,slSelectList);
                for(BusinessObjectWithSelectItr itrRequestObj= new BusinessObjectWithSelectItr(resourceRequestsObjWithSelectList); itrRequestObj.next();)
                {
                    bowsResourceRequest = itrRequestObj.obj();
                    slAllocatedResourceList = bowsResourceRequest.getSelectDataList(SELECT_REL_FROM_ALLOCATED_RESOURCE_ID);
                    slRelAllocatedResourceStateList = bowsResourceRequest.getSelectDataList(SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE);
                    if(null!=slAllocatedResourceList)
                    {
                    	for(int iStateIndex=0; iStateIndex<slRelAllocatedResourceStateList.size(); iStateIndex++)
                        {
                    		strPersonId = (String)slAllocatedResourceList.get(iStateIndex);
	                    	StringList slFTEDataList = (StringList)bowsResourceRequest.getSelectDataList(SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE);
                            strRelAllocatedResourceState = (String)slRelAllocatedResourceStateList.get(iStateIndex);
                            strFTEPlanValue = null;
                            if(null!=slFTEDataList && null!=slFTEDataList.get(iStateIndex)
                                    && (strRelAllocatedResourceState.equals(DomainConstants.ATTRIBUTE_RESOURCE_STATE_RANGE_COMMITTED)  
                                    	||	strRelAllocatedResourceState.equals(DomainConstants.ATTRIBUTE_RESOURCE_STATE_RANGE_PROPOSED)))
                            {
                            	mapMonthYearValue = new HashMap();
                                strFTEPlanValue = (String)slFTEDataList.get(iStateIndex);
                                mapMonthYearValue = getCalculatedMonthFTEMap(strFTEPlanValue, mapMonthYearValue);
                                StringList slActualFTEMonths = new StringList();
                                slActualFTEMonths.addAll(mapMonthYearValue.keySet());
                                if(null!=mapRequestRelAllocationData.get(strPersonId))
                                {
                                	mapProjectWiseAllocationData = (Map)mapRequestRelAllocationData.get(strPersonId);
                                }
                                else
                                {
                                	mapProjectWiseAllocationData = new HashMap();
                                }
                               	if(null!=mapProjectWiseAllocationData.get(strProjectId))
                               	{
                               		mapRequestRelTimeLineData = (Map)mapProjectWiseAllocationData.get(strProjectId);
                               	}
                               	else
                               	{  	 
                               		mapRequestRelTimeLineData = new HashMap();
                               	}
                               	Map mapRequestRelTimeLineDataRecursive = (Map)mapRequestRelTimeLineData.get(strRelAllocatedResourceState);
                               	boolean isFTEExistInTimeline = false;
                               	for (int i=0; i<slMonthYear.size(); i++)
                               	{
                               		String strMonthYearValue = (String)slMonthYear.get(i);
                               		if(slActualFTEMonths.contains(strMonthYearValue))
                               		{
                               			isFTEExistInTimeline = true;
                               		}
                               		Double dFTEValue = new Double(0);
                               		Double dNewFTEValue = new Double(0);
                               		if(null!=mapRequestRelTimeLineDataRecursive && null!=mapRequestRelTimeLineDataRecursive.get(strMonthYearValue))
                               		{
                               			dFTEValue = new Double(Task.parseToDouble((String)mapRequestRelTimeLineDataRecursive.get(strMonthYearValue)));
                               		}
                               		if(null!=mapMonthYearValue && null!=mapMonthYearValue.get(strMonthYearValue))
                               		{
                               			dNewFTEValue = (Double)(mapMonthYearValue.get(strMonthYearValue));
                               		}
                               		dFTEValue = new Double(dFTEValue.doubleValue() + dNewFTEValue.doubleValue());
                               		mapMonthYearValue.put(strMonthYearValue,String.valueOf(dFTEValue.doubleValue()));
                                }
                                if(isFTEExistInTimeline)
                                {
                                	mapRequestRelTimeLineData.put(strRelAllocatedResourceState, mapMonthYearValue);
                                	mapProjectWiseAllocationData.put(strProjectId, mapRequestRelTimeLineData);
                                	mapRequestRelAllocationData.put(strPersonId, mapProjectWiseAllocationData);
                                }
                            }
                        }
                    }
                }
            }
            String strDataKey = "";
            Map mapRequestRelAllocationDataFilter = new HashMap();
            Map mapRequestRelTimeLineDataTemp = null;
            for (Iterator itrPersonId = mapRequestRelAllocationData.keySet().iterator(); itrPersonId.hasNext();) 
            {
            	strPersonId = (String)itrPersonId.next();
            	Map mapProjectWiseAllocationDataFilter = (Map)mapRequestRelAllocationData.get(strPersonId);
				int nProjectSize = mapProjectWiseAllocationDataFilter.size();
				boolean isTotalRowToBeAdded = false;
				Map mapMonthYearValueTemp = new HashMap();
           		for (Iterator itrProjectId = mapProjectWiseAllocationDataFilter.keySet().iterator(); itrProjectId.hasNext();) 
           		{
           			strProjectId = (String) itrProjectId.next();
           			Map mapRequestRelTimeLineDataFilter = (Map)mapProjectWiseAllocationDataFilter.get(strProjectId);
           			int nStateSize = mapRequestRelTimeLineDataFilter.size();
           			for (Iterator iterator2 = mapRequestRelTimeLineDataFilter.keySet().iterator(); iterator2.hasNext();) 
           			{
           				strRelAllocatedResourceState = (String)iterator2.next();
           				mapRequestRelTimeLineDataTemp = (Map)mapRequestRelTimeLineDataFilter.get(strRelAllocatedResourceState);
           				strDataKey = strPersonId+"$"+strProjectId;
           				mapRequestRelAllocationDataFilter.put(strDataKey+"$"+strRelAllocatedResourceState, mapRequestRelTimeLineDataTemp);
           				for (int i=0; i<slMonthYear.size(); i++)
           				{
           					String strMonthYearValue = (String)slMonthYear.get(i);
           					Double dFTEValue = new Double(0);
           					Double dNewFTEValue = new Double(0);
           					if(null!=mapRequestRelTimeLineDataTemp.get(strMonthYearValue))
           					{
           						dFTEValue = new Double(Task.parseToDouble((String)mapRequestRelTimeLineDataTemp.get(strMonthYearValue)));
           					}
           					if(null!=mapMonthYearValueTemp.get(strMonthYearValue))
           					{
           						dNewFTEValue = new Double(Task.parseToDouble((String)mapMonthYearValueTemp.get(strMonthYearValue)));
           					}
           					dFTEValue = new Double(dFTEValue.doubleValue() + dNewFTEValue.doubleValue());
           					mapMonthYearValueTemp.put(strMonthYearValue,String.valueOf(dFTEValue.doubleValue()));
           				}
           				if(nStateSize >1 || (null!=strSelectedOverallFilter && !"".equals(strSelectedOverallFilter) && !"null".equals(strSelectedOverallFilter) && strSelectedOverallFilter.equals("Summary")))
           				{
           					isTotalRowToBeAdded = true;
           				}
           			}
           		}
            	if(isTotalRowToBeAdded || (nProjectSize>1))
            	{
            		mapRequestRelAllocationDataFilter.put(strPersonId+"$"+"Total", mapMonthYearValueTemp);
            	}
            }
            return mapRequestRelAllocationDataFilter;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
        
    /**
     * @param context
     * @param strObjectIds
     * @param slSelectList
     * @return
     * @throws MatrixException
     */
    protected static Map getBusinessObjectDataWithSelectList(Context context, String[] strObjectIds, StringList slSelectList)
    throws MatrixException
    {
    	BusinessObjectWithSelectList resourcePoolObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strObjectIds,slSelectList);
        BusinessObjectWithSelect bows = null;
        StringList slSelectIdDataList = null;
        StringList slSelectIdList = null;
        String strSelectQuery = "";
        Map mapQueryWithDataList = null;
        Map mapObjectIdWithQueryDataList = new HashMap();
        for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourcePoolObjWithSelectList); itr.next();)
        {
            bows = itr.obj();
            mapQueryWithDataList = new HashMap();
            String strObjectId = bows.getObjectId();
            for(int i=0; i<slSelectList.size(); i++)
            {                
                strSelectQuery = (String)slSelectList.get(i);
                slSelectIdDataList = bows.getSelectDataList(strSelectQuery);
                mapQueryWithDataList.put(strSelectQuery,slSelectIdDataList);
            }
            mapObjectIdWithQueryDataList.put(strObjectId,mapQueryWithDataList);
        }
        return mapObjectIdWithQueryDataList;
    }    
    
    /**
     * Gets the data for the column "<name>" for table "<name>"
     * 
     * @param context The matrix context object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing
     * @throws Exception if operation fails
     */
    public Vector getColumnPersonData(Context context, String[] args) throws Exception 
    {
        try 
        {
            // Create result vector
            Vector vecResult = new Vector();
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList mlObjectList = (MapList) programMap.get("objectList");
            
            Map mapObjectInfo = null;
            for (Iterator itrTableRows = mlObjectList.iterator(); itrTableRows.hasNext();) 
            {
                mapObjectInfo = (Map) itrTableRows.next();
                vecResult.add (mapObjectInfo.get("PersonName"));
            }
            return vecResult;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw exp;
        }
    }
    
    /**
     * Gets the data for the column "<name>" for table "<name>"
     * 
     * @param context The matrix context object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing
     * @throws Exception if operation fails
     */
    public Vector getColumnProjectData(Context context, String[] args) throws Exception 
    {
        try 
        {
            // Create result vector
            Vector vecResult = new Vector();
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList mlObjectList = (MapList) programMap.get("objectList");
            
            Map mapObjectInfo = null;
            for (Iterator itrTableRows = mlObjectList.iterator(); itrTableRows.hasNext();) 
            {
                mapObjectInfo = (Map) itrTableRows.next();
                vecResult.add (mapObjectInfo.get("Project i18n Title"));
            }
            return vecResult;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw exp;
        }
    }
    
    /**
     * Gets the data for the column "<name>" for table "<name>"
     * 
     * @param context The matrix context object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing
     * @throws Exception if operation fails
     */
    public Vector getColumnStateData(Context context, String[] args) throws Exception 
    {
        try 
        {
            // Create result vector
            Vector vecResult = new Vector();
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList mlObjectList = (MapList) programMap.get("objectList");
            
            Map mapObjectInfo = null;
            for (Iterator itrTableRows = mlObjectList.iterator(); itrTableRows.hasNext();) 
            {
                mapObjectInfo = (Map) itrTableRows.next();
                vecResult.add (mapObjectInfo.get("State"));
            }
            return vecResult;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw exp;
        }
    }
    
    /**
     * @param context
     * @param args
     * @return
     * @throws MatrixException
     */
    public MapList getDynamicProjectColumn(Context context, String[] args)
    throws MatrixException 
	{
		try 
		{
		    if (context == null) 
		    {
		        throw new IllegalArgumentException("context");
		    }
		    Map programMap = (HashMap) JPO.unpackArgs(args);
		    Map requestMap = (Map) programMap.get("requestMap");
		    MapList mlColumnsMapList = new MapList();
		    Map requestValuesMap = (Map) requestMap.get("RequestValuesMap");
            String strSelectedOverallFilter = ((String[])requestValuesMap.get("PMCReportPeopleProjectAssignementFilter"))[0];
		    if(null==strSelectedOverallFilter || "".equals(strSelectedOverallFilter) || "null".equals(strSelectedOverallFilter) || strSelectedOverallFilter.equals("Detail"))
		    {
			    String strLanguage = (String)requestMap.get("languageStr");
			    Map columnMap = null;
			    Map settingsMap = null;
			    i18nNow loc = new i18nNow();
			    final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
		    	String stri18ColumnName =  (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourcePlanning.Report.Projects");
		    	String strColumnName =  "Projects";
		        columnMap = new HashMap();
		        settingsMap = new HashMap();
		        columnMap.put("settings", settingsMap);
		        columnMap.put("name", strColumnName);//TODO English
		        columnMap.put("label", stri18ColumnName);//TODO Translated
		        settingsMap.put("Registered Suite","eServiceSuiteProgramCentral");
		        settingsMap.put("function","getColumnProjectData");
		        settingsMap.put("program","emxResourcePoolReports");
		        settingsMap.put("Sortable","false");
		        settingsMap.put("Export","true");
		        settingsMap.put("Column Type","programHTMLOutput");
		        mlColumnsMapList.add(columnMap);
		    }
	        return mlColumnsMapList;
		}         
		catch (IllegalArgumentException iaexp) 
		{
		    iaexp.printStackTrace();
		    throw new MatrixException(iaexp);
		} 
		catch (Exception exp) 
		{
		    exp.printStackTrace();
		    throw new MatrixException(exp);
		}
	}
    
    /**
     * @param context
     * @param args
     * @return
     * @throws MatrixException
     */
    public MapList getDynamicStateColumn(Context context, String[] args)
    throws MatrixException 
	{
		try 
		{
		    if (context == null) 
		    {
		        throw new IllegalArgumentException("context");
		    }
		    Map programMap = (HashMap) JPO.unpackArgs(args);
		    Map requestMap = (Map) programMap.get("requestMap");
		    String strLanguage = (String)requestMap.get("languageStr");
		    MapList mlColumnsMapList = new MapList();
		    Map requestValuesMap = (Map) requestMap.get("RequestValuesMap");
            String strSelectedOverallFilter = ((String[])requestValuesMap.get("PMCReportPeopleProjectAssignementFilter"))[0];
		    if(null==strSelectedOverallFilter || "".equals(strSelectedOverallFilter) || "null".equals(strSelectedOverallFilter) || strSelectedOverallFilter.equals("Detail"))
		    {
			    Map columnMap = null;
			    Map settingsMap = null;
			    i18nNow loc = new i18nNow();
			    final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
		    	String stri18ColumnName =  (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourcePlanning.Report.State");
		    	String strColumnName =  "State";
		        columnMap = new HashMap();
		        settingsMap = new HashMap();
		        columnMap.put("settings", settingsMap);
		        columnMap.put("name", strColumnName);//TODO English
		        columnMap.put("label", stri18ColumnName);//TODO Translated
		        settingsMap.put("Registered Suite","eServiceSuiteProgramCentral");
		        settingsMap.put("function","getColumnStateData");
		        settingsMap.put("program","emxResourcePoolReports");
		        settingsMap.put("Sortable","false");
		        settingsMap.put("Column Type","program");
		        mlColumnsMapList.add(columnMap);
		    }    
	        return mlColumnsMapList;
		}         
		catch (IllegalArgumentException iaexp) 
		{
		    iaexp.printStackTrace();
		    throw new MatrixException(iaexp);
		} 
		catch (Exception exp) 
		{
		    exp.printStackTrace();
		    throw new MatrixException(exp);
		}
	}
    
    /**
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public StringList getFTEStyleInfo(Context context, String[] args)  throws Exception 
    {
    	try 
        {
            StringList slFTEStyles = new StringList();
            // Get object list information from packed arguments
            Map programMap = (HashMap) JPO.unpackArgs(args);
            Map columnMap = (HashMap) programMap.get("columnMap");
            String strColumnName = (String)columnMap.get("name");
            MapList mlObjectList = (MapList) programMap.get("objectList");
            double nMaxFTEPerPerson = Task.parseToDouble(EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourceRequest.MaximumFTEPerPerson"));
            Map mapObjectInfo = null;
            Map mapColumnsData = null;
            for (Iterator itrTableRows = mlObjectList.iterator(); itrTableRows.hasNext();) 
            {
                mapObjectInfo = (Map) itrTableRows.next();
                mapColumnsData = (Map)mapObjectInfo.get("Columns Data");
                String strFTEValue= (String)mapColumnsData.get(strColumnName);
                if(null!=strFTEValue && !"".equals(strFTEValue) && !"null".equals(strFTEValue))
                {
	                double nActualFTE = Task.parseToDouble(strFTEValue);
	                if(nActualFTE>=nMaxFTEPerPerson)
	                {
	                	slFTEStyles.addElement("ColumnBackGroundColor");
	                }
	                else
	                {
	                	slFTEStyles.addElement("CellBackGroundColor");
	                }
                }
                else
                {
                	slFTEStyles.addElement("");
                }
            }
            return slFTEStyles;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw exp;
        }                    
    }
    
    public StringList getPMCUser(Context context) throws MatrixException
    {
    	StringList slPMCUsers = new StringList();
    	try
    	{
	    	String strPMCSymbolicRoles = EnoviaResourceBundle.getProperty(context, "eServiceSuiteProgramCentral.Roles");
	        StringList slPMCSymbolicRoles = FrameworkUtil.split(strPMCSymbolicRoles, ",");
	        
	        String strRole = "";
	        Role matrixRole = null;
	        UserList assignments = null;
	        String personObjId = "";
	        for (StringItr itrRoles = new StringItr(slPMCSymbolicRoles); itrRoles.next();) 
	        {
	            strRole = PropertyUtil.getSchemaProperty(context, itrRoles.obj().trim());
	            
	            matrixRole = new Role(strRole);
	            matrixRole.open(context);
	            
	            StringList projectUsers = new StringList();
	            assignments = matrixRole.getAssignments(context);
    			if(assignments.size() > 0){
	            Organization organization = new Organization();
	            UserItr userItr = new UserItr(assignments);
	            
	            while(userItr.next())
	            {
	                if (userItr.obj() instanceof matrix.db.Person)
	                {
	                	try {
	                		personObjId = PersonUtil.getPersonObjectID(context, userItr.obj().getName());
	                	} catch(Exception exception) {
			          		//PersonUtil.getPersonObjectID() throws exception when Person Admin
			          		//object exists and Business object does not.
	                	}
	                    if (!slPMCUsers.contains(personObjId)) 
	                    {
	                        slPMCUsers.add(personObjId);
	                    }
	                }
	            }
	            matrixRole.close(context);
	        }
    			else{ // to handle OCDX users case
    				String sMqlCommand = "print role $1 select $2 dump $3;";
    				String result = MqlUtil.mqlCommand(context, sMqlCommand, strRole, "person", "|" );
    				StringList Users = FrameworkUtil.split(result, "|");
    				Iterator userIterator = Users.iterator();
    				while(userIterator.hasNext())
    				{
    					String userName = (String)userIterator.next();
    					try
    					{
    						//Creating person bean itself will throw error if Business Object does not exist
    						personObjId = PersonUtil.getPersonObjectID(context, userName);
    						if (!slPMCUsers.contains(personObjId))
    							slPMCUsers.add(personObjId);
    					}
    					catch(Exception e)
    					{

    					}
    				}
    			}
    		}
    	}
    	catch(FrameworkException fwe)
    	{
    		throw new MatrixException(fwe);
    	}
    	return slPMCUsers;
    }
    
    /**
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public StringList getStyleInfoForResourceUtilizationReport(Context context, String[] args)  throws Exception 
    {
    	try 
        {
            StringList slFTEStyles = new StringList();
            // Get object list information from packed arguments
            Map programMap = (HashMap) JPO.unpackArgs(args);
            Map columnMap = (HashMap) programMap.get("columnMap");
            String strColumnName = (String)columnMap.get("name");
            MapList mlObjectList = (MapList) programMap.get("objectList");
            Map mapObjectInfo = null;
            Map mapColumnsData = null;
            String strRowName = "";
            for (Iterator itrTableRows = mlObjectList.iterator(); itrTableRows.hasNext();) 
            {
                mapObjectInfo = (Map) itrTableRows.next();
                strRowName = (String)mapObjectInfo.get(STRING_TITLE);
                if(strRowName.equalsIgnoreCase(STRING_PROJECT_UTILIZATION) || strRowName.equalsIgnoreCase(STRING_ACTUAL_UTILIZATION))
                {
                	mapColumnsData = (Map)mapObjectInfo.get("Columns Data");
                	double nActualPercentage = Task.parseToDouble((String)mapColumnsData.get(strColumnName));
                	if(nActualPercentage>100)
                	{
                		slFTEStyles.addElement("ColumnBackGroundColor");
                	}
                	else if(nActualPercentage>=85 && nActualPercentage<=100)
                	{
                		slFTEStyles.addElement("CellYellowBackGroundColorForPMCReport");
                	}
                	else
                    {
                    	slFTEStyles.addElement("");
                    }
                }
                else
                {
                	slFTEStyles.addElement("");
                }
            }
            return slFTEStyles;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw exp;
        }                    
    }
    
    /**
     * This method is used to get if the person is allocated to project as member. 
     * This data is used for the presentation of People Project Assignment Report.
     * This Method is returning Map containing personId and Project Info related to person.
     * @param context
     * @param strPersonIds It will contains PersonIds
     * @param slTimeLineData it will contains TimeLineData Required for report.
     * @return Map
     * @throws MatrixException
     */
    private Map getPersonProjectMemberAllocationData(Context context, String[] strPersonIds, StringList slTimeLineData) throws MatrixException 
    {
        try 
        {
            Map mapProjectMemberRelInfoList = new HashMap();
        	final String SELECT_PROJECT_ID = DomainConstants.SELECT_ID;
            final String SELECT_REL_TO_MEMBER_OBJECT_ID = "to["+DomainConstants.RELATIONSHIP_MEMBER+"].businessobject."+DomainConstants.SELECT_ID;
            final String SELECT_REL_TO_MEMBER_OBJECT_TYPE = "to["+DomainConstants.RELATIONSHIP_MEMBER+"].businessobject."+DomainConstants.SELECT_TYPE;
            final String SELECT_REL_TO_MEMBER_OBJECT_NAME = "to["+DomainConstants.RELATIONSHIP_MEMBER+"].businessobject."+DomainConstants.SELECT_NAME;
            //Get Value of all the of selectable
            StringList slSelectList = new StringList();
            slSelectList.add(SELECT_REL_TO_MEMBER_OBJECT_ID);
            slSelectList.add(SELECT_REL_TO_MEMBER_OBJECT_TYPE);
            slSelectList.add(SELECT_REL_TO_MEMBER_OBJECT_NAME);
            
            Map mapColumnData = new HashMap();
            if(null!=slTimeLineData)
            {
	            for(int i=0; i<slTimeLineData.size(); i++)
	            {
	            	mapColumnData.put(slTimeLineData.get(i), "");
	            }
            }
            Map mapObjectIdWithQueryDataList = getBusinessObjectDataWithSelectList(context, strPersonIds, slSelectList);
            if(null!=mapObjectIdWithQueryDataList)
            {
            	StringList slObjectIdList 	= null;
            	StringList slObjectTypeList = null;
            	StringList slObjectNameList = null;
            	StringList slProjectIdList 	= null;
            	StringList slProjectNameList= null;
            	Map mapQueryWithDataList = null;
            	String strPersonId = "";
            	String strObjectType = "";
            	Map mapProjectMemberProjectInfoList = null;
            	for (Iterator iterator = mapObjectIdWithQueryDataList.keySet().iterator(); iterator.hasNext();) 
            	{
            		strPersonId = (String)iterator.next();
            		mapProjectMemberProjectInfoList = new HashMap();
            		mapQueryWithDataList = (Map)mapObjectIdWithQueryDataList.get(strPersonId);
            		slObjectIdList = (StringList)mapQueryWithDataList.get(SELECT_REL_TO_MEMBER_OBJECT_ID);
            		slObjectTypeList = (StringList)mapQueryWithDataList.get(SELECT_REL_TO_MEMBER_OBJECT_TYPE);
            		slObjectNameList = (StringList)mapQueryWithDataList.get(SELECT_REL_TO_MEMBER_OBJECT_NAME);
            		strObjectType = "";
            		slProjectIdList = new StringList();
            		slProjectNameList = new StringList();
            		for(int i=0; i<slObjectTypeList.size(); i++)
            		{
            			strObjectType = (String)slObjectTypeList.get(i);
            			if(strObjectType.equals(DomainConstants.TYPE_PROJECT_SPACE))
            			{
            				slProjectIdList.add(slObjectIdList.get(i));
            				slProjectNameList.add(slObjectNameList.get(i));
            			}
            		}
            		mapProjectMemberProjectInfoList.put("ProjectIds", slProjectIdList);
            		mapProjectMemberProjectInfoList.put("ProjectNames", slProjectNameList);
            		mapProjectMemberProjectInfoList.put("Columns Data", mapColumnData);
            		mapProjectMemberRelInfoList.put(strPersonId,mapProjectMemberProjectInfoList);
            	}
            }
            
            return mapProjectMemberRelInfoList;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTableResourceDemandBySkillReportData(Context context, String[] args)
    throws MatrixException 
    {   
        MapList mlResourceUtilDataList  = new MapList();
        try 
        {            
            if (context == null) 
            {
                throw new IllegalArgumentException("context");
            }
            
            //MapList mlResourceUtilDataList  = new MapList();
            Map programMap = (Map) JPO.unpackArgs(args);
            
            Map requestValuesMap = (Map) programMap.get("RequestValuesMap");
            
            //Get the Data for the First Column 
            i18nNow loc = new i18nNow();
            String strLanguage = (String)programMap.get("languageStr");
            final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
            
            final String STRING_CAPACITY_LANG = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourcePlanning.Report.Capacity");
            
            String[] strTableRowIdValues =  (String[])requestValuesMap.get("ResourcePoolId");
            String strResourceObjectId = strTableRowIdValues[0];
            StringList slResourcePoolIds = FrameworkUtil.split(strResourceObjectId, ",");
            String[] strResourcePoolIds = new String[slResourcePoolIds.size()];
            slResourcePoolIds.copyInto(strResourcePoolIds);
        
            StringList slSelectList = new StringList();
            
            final String SELECT_REL_TO_RESOURCE_POOL_RESOURCEREQSKILL_SKILL_ID = "to["+DomainConstants.RELATIONSHIP_RESOURCE_POOL+"].businessobject.from["+DomainConstants.RELATIONSHIP_RESOURCE_REQUEST_SKILL+"].businessobject."+DomainConstants.SELECT_ID;
            final String SELECT_REL_TO_RESOURCE_POOL_RESOURCEREQSKILL_SKILL_NAME = "to["+DomainConstants.RELATIONSHIP_RESOURCE_POOL+"].businessobject.from["+DomainConstants.RELATIONSHIP_RESOURCE_REQUEST_SKILL+"].businessobject."+DomainConstants.SELECT_NAME;
            slSelectList.add(SELECT_REL_TO_RESOURCE_POOL_RESOURCEREQSKILL_SKILL_ID);
            slSelectList.add(SELECT_REL_TO_RESOURCE_POOL_RESOURCEREQSKILL_SKILL_NAME);
            
            //Map mapQueryWithDataList = getResourcePoolsDataWithSelectList(context, strResourcePoolIds, slSelectList, true);
            BusinessObjectWithSelectList resourcePoolObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strResourcePoolIds,slSelectList);
            BusinessObjectWithSelect bows = null;
            StringList slSelectSkillIdList = null;
            StringList slSelectSkillNameList = null;
            StringList slSkillIdList = new StringList();
            StringList slSkillNameList = new StringList();
            StringList slFilterResourcePoolIdList = new StringList();
            
            for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourcePoolObjWithSelectList); itr.next();)
            {
                bows = itr.obj();
                slSelectSkillIdList = bows.getSelectDataList(SELECT_REL_TO_RESOURCE_POOL_RESOURCEREQSKILL_SKILL_ID);
                slSelectSkillNameList = bows.getSelectDataList(SELECT_REL_TO_RESOURCE_POOL_RESOURCEREQSKILL_SKILL_NAME);
                if(null!=slSelectSkillIdList)
                {
                	if(!slFilterResourcePoolIdList.contains(bows.getObjectId()))
                	{
                		slFilterResourcePoolIdList.add(bows.getObjectId());
                	}
                	for(int i=0; i<slSelectSkillIdList.size();i++)
                    {
                        if(!slSkillIdList.contains(slSelectSkillIdList.get(i)))
                        {
                        	slSkillIdList.add(slSelectSkillIdList.get(i));
                        	slSkillNameList.add(slSelectSkillNameList.get(i));
                        }
                    }
                }
            }
            String[] strFilterResourcePoolIds = new String[slFilterResourcePoolIdList.size()];
            slFilterResourcePoolIdList.copyInto(strFilterResourcePoolIds);
            
            Map mapTableRow = null;
            
            String strTypeIcon = "";
		    String strSymbolicType = FrameworkUtil.getAliasForAdmin(context,"Type",DomainConstants.TYPE_BUSINESS_SKILL, true);
		    strTypeIcon = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon." + strSymbolicType);
		    if(null!=slSkillIdList && slSkillIdList.size()>0)
		    {
	            for(int i=0; i<slSkillIdList.size(); i++)
	            {
	                mapTableRow = new HashMap();
	                String 	skillName	=	(String)slSkillNameList.get(i);
	                StringBuffer businessSkillHyperLnk = new StringBuffer();
	                businessSkillHyperLnk.append("<a href='../common/emxTree.jsp?");
	                businessSkillHyperLnk.append("objectId=").append((String)slSkillIdList.get(i));
	                businessSkillHyperLnk.append("'>");
	                businessSkillHyperLnk.append("<img border=\"0\" src=\"images/"+strTypeIcon+"\" />");
	              //Added for special character.
	                businessSkillHyperLnk.append(XSSUtil.encodeForHTML(context, skillName));
	                businessSkillHyperLnk.append("</a>");
	               
	                mapTableRow.put("SkillId", slSkillIdList.get(i));
	              //Added for special character.
	                mapTableRow.put(STRING_TITLE, skillName);
	                mapTableRow.put(STRING_I18N_TITLE, businessSkillHyperLnk.toString());
	                mlResourceUtilDataList.add(mapTableRow);
	            }
	            mapTableRow = new HashMap();
	            mapTableRow.put("SkillId", STRING_CAPACITY);
	            mapTableRow.put(STRING_TITLE, STRING_CAPACITY);
	            mapTableRow.put(STRING_I18N_TITLE, STRING_CAPACITY_LANG);
	            mlResourceUtilDataList.add(mapTableRow);
	            
	            String strFromDate = "";
	            String strToDate = "";
	            strFromDate = null!=requestValuesMap.get("PMCCustomFilterFromDate")?((String[])requestValuesMap.get("PMCCustomFilterFromDate"))[0]:"";
	            strToDate   = null!=requestValuesMap.get("PMCCustomFilterToDate")?((String[])requestValuesMap.get("PMCCustomFilterToDate"))[0]:"";
	            StringList slMonthYearList = getTimeframes(strFromDate, strToDate);
	            
	            Map mapMonthYearValues = getResourceDemandBySkillReportData(context,slMonthYearList,strFilterResourcePoolIds);
	            
	            final String SELECT_REL_MEMBER_PERSON_ID = "from["+DomainConstants.RELATIONSHIP_MEMBER+"].businessobject."+DomainConstants.SELECT_ID;
	            slSelectList.clear();
	            slSelectList.add(SELECT_REL_MEMBER_PERSON_ID);
	            double nPeopleSize = 0;
	            Map mapQueryWithDataList = getResourcePoolsDataWithSelectList(context, strResourcePoolIds, slSelectList, true);
	            StringList slPersonIdList = (StringList)mapQueryWithDataList.get(SELECT_REL_MEMBER_PERSON_ID);
	            Map mapMonthYearFTECapacity = new HashMap();
	            StringList slFilterPersonIdList = new StringList();
	            if(null!=slPersonIdList && slPersonIdList.size()>0)
	            {
	            	StringList slPMCUserList = getPMCUser(context);
	            	for(int index=0; index<slPersonIdList.size(); index++)
	            	{
	            		String strPersonId = (String)slPersonIdList.get(index);
	            		if(slPMCUserList.contains(strPersonId))
	            		{
	            			slFilterPersonIdList.add(strPersonId);
	            		}
	            	}
	                nPeopleSize = slFilterPersonIdList.size();
	            }
	            Map mapMonthYearValue = new HashMap();
	            String numberofPeopleUnit=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.NumberofPeopleUnit");
	            double nHoursPerDay = FTE.getHoursPerDay(context);
	            FTE fte = FTE.getInstance(context);
	            for(int i=0; i<slMonthYearList.size(); i++)
	            {
	            	double dOutputValue = nPeopleSize;
	            	if("Hours".equalsIgnoreCase(numberofPeopleUnit))
	            	{
	            		long duration = DateUtil.computeDuration(fte.getStartDate((String)slMonthYearList.get(i)),fte.getEndDate((String)slMonthYearList.get(i)));
	            		dOutputValue = (nPeopleSize*nHoursPerDay*duration);
	            	}
	            	mapMonthYearValue.put(slMonthYearList.get(i), String.valueOf(dOutputValue));
	            }
	            mapMonthYearValues.put(STRING_CAPACITY, mapMonthYearValue);
	            
	            String strResourcePoolId = "";
	            Map mapColumnsData = null;
	            for (Iterator itrTableRow = mlResourceUtilDataList.iterator(); itrTableRow.hasNext();) 
	            {
	                mapTableRow = (Map) itrTableRow.next();
	                strResourcePoolId = (String)mapTableRow.get("SkillId");
	                mapColumnsData = (Map)mapMonthYearValues.get(strResourcePoolId);
	                mapTableRow.put("Columns Data", mapColumnsData);
	            }
		    }
            return mlResourceUtilDataList;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
    
    /**
     * @param context
     * @param slMonthYear
     * @param strResourcePoolIds
     * @param slRowHeaderNameList
     * @return
     * @throws MatrixException
     */
    private Map getResourceDemandBySkillReportData(Context context,StringList slMonthYear, String[] strResourcePoolIds) throws MatrixException 
    {
        try 
        {
            final String SELECT_RESOURCE_REQUSET_SKILL_ID = "from["+DomainConstants.RELATIONSHIP_RESOURCE_REQUEST_SKILL+"].businessobject."+DomainConstants.SELECT_ID;
            final String SELECT_REL_TO_RESOURCE_POOL_RESOURCE_REQUEST_ID = "to["+DomainConstants.RELATIONSHIP_RESOURCE_POOL+"].businessobject."+DomainConstants.SELECT_ID;
            final String SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE = "to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].attribute["+DomainConstants.ATTRIBUTE_FTE+"].value";
            final String SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE = "from["+DomainConstants.RELATIONSHIP_ALLOCATED+"].attribute["+DomainConstants.ATTRIBUTE_FTE+"].value";
            final String SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE = "from["+DomainConstants.RELATIONSHIP_ALLOCATED+"].attribute["+DomainConstants.ATTRIBUTE_RESOURCE_STATE+"].value";
            
            Map mapRowNameMonthYearValue   = new HashMap();         
            Map mapMonthYearValue = null;
                    
            StringList slSelectList = new StringList();
            slSelectList.add(SELECT_REL_TO_RESOURCE_POOL_RESOURCE_REQUEST_ID);
            BusinessObjectWithSelectList resourcePoolObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strResourcePoolIds,slSelectList);
            BusinessObjectWithSelectList resourceRequestsObjWithSelectList = null;
            BusinessObjectWithSelect bowsResourcePool = null;
            BusinessObjectWithSelect bowsResourceRequest = null;
            Map mapRequestRelResourcePlanData = new HashMap();
            String strFTEPlanValue = "";
            String strResourceRequestSkillId = "";
            StringList slResourcePoolResourceRequestIdList = null;
            
            slSelectList.clear();
            slSelectList.add(SELECT_RESOURCE_REQUSET_SKILL_ID);
            slSelectList.add(SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE);
            slSelectList.add(DomainConstants.SELECT_CURRENT);
            slSelectList.add(SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE);
            slSelectList.add(SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE);
               
            StringList slFTEDataList = null;
            StringList slRelAllocatedResourceStateList = null;
            String strRelAllocatedResourceState = "";
            
            for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourcePoolObjWithSelectList); itr.next();)
            {
                bowsResourcePool = itr.obj();
                slResourcePoolResourceRequestIdList = bowsResourcePool.getSelectDataList(SELECT_REL_TO_RESOURCE_POOL_RESOURCE_REQUEST_ID);
            	if(null==slResourcePoolResourceRequestIdList || slResourcePoolResourceRequestIdList.size()==0)
            	{
            		continue;
            	}
            	String[] strRequestIds = new String[0];
            	if(null!=slResourcePoolResourceRequestIdList)
            	{
            		strRequestIds = new String[slResourcePoolResourceRequestIdList.size()];
            		slResourcePoolResourceRequestIdList.copyInto(strRequestIds);
            	}
            	BusinessObjectWithSelect bows = null;
            	final String SELECT_REL_RESOURCE_PLAN_PROJECT_ID = "to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].from."+DomainConstants.SELECT_ID;
            	StringList slProjectSelectList = new StringList();
            	slProjectSelectList.add(SELECT_REL_RESOURCE_PLAN_PROJECT_ID);
            	slProjectSelectList.add("to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].from."+DomainConstants.SELECT_CURRENT);
            	resourceRequestsObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strRequestIds,slProjectSelectList);
            	for(BusinessObjectWithSelectItr nitr= new BusinessObjectWithSelectItr(resourceRequestsObjWithSelectList); nitr.next();)
            	{
            		String strProjectId = "";
            		String strCurrentState = "";
            		bows = nitr.obj();
            		strProjectId = bows.getSelectData(SELECT_REL_RESOURCE_PLAN_PROJECT_ID);
            		String strProjectState = bows.getSelectData("to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].from."+DomainConstants.SELECT_CURRENT);
            			if(!isActiveProject(strProjectState)){
            				slResourcePoolResourceRequestIdList.remove(bows.getObjectId()) ;
            			}
            	}
            	
            	
            	//resourceRequestsObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strRequestIds,slSelectList);
            	//for(BusinessObjectWithSelectItr itrRequestObj= new BusinessObjectWithSelectItr(resourceRequestsObjWithSelectList); itrRequestObj.next();)
            	//{
            	//	bowsResourceRequest = itrRequestObj.obj();
            	for(StringItr stringItr = new StringItr(slResourcePoolResourceRequestIdList); stringItr.next();)
            	{
            		String strRequestID = stringItr.obj();
            			DomainObject dmoRequest =DomainObject.newInstance(context, strRequestID);
            		strResourceRequestSkillId = dmoRequest.getInfo(context,SELECT_RESOURCE_REQUSET_SKILL_ID);
            		if(null!=strResourceRequestSkillId && !"".equals(strResourceRequestSkillId) && !"null".equals(strResourceRequestSkillId))
            		{
	            		String strCurrentState =dmoRequest.getInfo(context,DomainConstants.SELECT_CURRENT);
	            		if(strCurrentState.equals(DomainConstants.STATE_RESOURCE_REQUEST_REQUESTED)||strCurrentState.equals(DomainConstants.STATE_RESOURCE_REQUEST_PROPOSED))
	            		{
	            			if(null==mapRequestRelResourcePlanData.get(strResourceRequestSkillId))
	            			{
	            				mapMonthYearValue = new HashMap();
	            			}
	            			else
	            			{   
	            				mapMonthYearValue = (Map)mapRequestRelResourcePlanData.get(strResourceRequestSkillId);
	            			}
	            			strFTEPlanValue = dmoRequest.getInfo(context,SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE);
	            			mapMonthYearValue = getResourceRequestsPerMonthMap(strFTEPlanValue, mapMonthYearValue,slMonthYear);
	            			mapRequestRelResourcePlanData.put(strResourceRequestSkillId, mapMonthYearValue);
	            		}
	
	            		slRelAllocatedResourceStateList =dmoRequest.getInfoList(context,SELECT_REL_ALLOCATED_ATTRIBUTE_RESOURCE_STATE);
	            		if(null!=slRelAllocatedResourceStateList)
	            		{
	            			for(int iStateIndex=0; iStateIndex<slRelAllocatedResourceStateList.size(); iStateIndex++)
	            			{
	            				strRelAllocatedResourceState = (String)slRelAllocatedResourceStateList.get(iStateIndex);
	            				if(strRelAllocatedResourceState.equalsIgnoreCase(DomainConstants.ATTRIBUTE_RESOURCE_STATE_RANGE_COMMITTED))
	            				{
	            					if(null==mapRequestRelResourcePlanData.get(strResourceRequestSkillId))
	            					{
	            						mapMonthYearValue = new HashMap();
	            					}
	            					else
	            					{   
	            						mapMonthYearValue = (Map)mapRequestRelResourcePlanData.get(strResourceRequestSkillId);
	            					}
	            					slFTEDataList = (StringList)dmoRequest.getInfoList(context,SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE);
	            					if(null!=slFTEDataList)
	            					{
	            						strFTEPlanValue = (String)slFTEDataList.get(iStateIndex);
	            						mapMonthYearValue = getResourceRequestsPerMonthMap(strFTEPlanValue, mapMonthYearValue,slMonthYear);
	            						mapRequestRelResourcePlanData.put(strResourceRequestSkillId, mapMonthYearValue);
	            					}
	            				}
	            			}
	            		}
	            	}
            	}
            }
            updateToRoundingValue(mapRequestRelResourcePlanData);
            return mapRequestRelResourcePlanData;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
    
    /**
     * <method description>
     *
     * @param context The Matrix Context object
     * @param args <description>
     * @return MapList <description>
     * @throws Exception 
     * @since PRG R208
     */
    public MapList getDynamicColumnsResourceLoadingReport(Context context, String[] args)
            throws Exception 
    {
    	if (context == null) 
        {
            throw new IllegalArgumentException("context");
        }
        Map programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");
    	MapList mlColumnsMapList = new MapList();
    	String strDisplayMode = (String)requestMap.get("displaymode");
    	if(strDisplayMode.equals("Summary"))
    	{
    		mlColumnsMapList.addAll(getDynamicPersonResourceLoadingColumn(context, args));
        	mlColumnsMapList.addAll(getDynamicProjectResourceLoadingColumn(context, args));
        	mlColumnsMapList.addAll(getDynamicWBSResourceLoadingColumn(context, args));
        	mlColumnsMapList.addAll(getDynamicMonthYearColumns(context, args));
        	
    	}
    	else if(strDisplayMode.equals("Detail"))
		{
        	mlColumnsMapList.addAll(getDynamicProjectResourceLoadingColumn(context, args));
        	mlColumnsMapList.addAll(getDynamicMonthYearColumns(context, args));
		}
    	return mlColumnsMapList;
    }
    
    /**
     * @param context
     * @param args
     * @return
     * @throws MatrixException
     */
    public MapList getDynamicPersonResourceLoadingColumn(Context context, String[] args)
    throws MatrixException 
	{
		try 
		{
		    if (context == null) 
		    {
		        throw new IllegalArgumentException("context");
		    }
		    Map programMap = (HashMap) JPO.unpackArgs(args);
		    Map requestMap = (Map) programMap.get("requestMap");
		    MapList mlColumnsMapList = new MapList();
		    Map requestValuesMap = (Map) requestMap.get("RequestValuesMap");
		    String strLanguage = (String)requestMap.get("languageStr");
		    Map columnMap = null;
		    Map settingsMap = null;
		    i18nNow loc = new i18nNow();
		    final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
	    	String stri18ColumnName =  (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourcePlanning.Report.People");
	    	String strColumnName =  "People";
	        columnMap = new HashMap();
	        settingsMap = new HashMap();
	        columnMap.put("settings", settingsMap);
	        columnMap.put("name", strColumnName);//TODO English
	        columnMap.put("label", stri18ColumnName);//TODO Translated
	        settingsMap.put("Registered Suite","eServiceSuiteProgramCentral");
	        settingsMap.put("function","getColumnPersonData");
	        settingsMap.put("program","emxResourcePoolReports");
	        settingsMap.put("Sortable","false");
	        settingsMap.put("Export","true");
	        settingsMap.put("Column Type","programHTMLOutput");
	        //settingsMap.put("Style Program","emxResourcePoolReports");
        	//settingsMap.put("Style Function","getStyleInfoForResourceLoadingReport");
	        mlColumnsMapList.add(columnMap);
	        return mlColumnsMapList;
		}         
		catch (IllegalArgumentException iaexp) 
		{
		    iaexp.printStackTrace();
		    throw new MatrixException(iaexp);
		} 
		catch (Exception exp) 
		{
		    exp.printStackTrace();
		    throw new MatrixException(exp);
		}
	}
    
    /**
     * @param context
     * @param args
     * @return
     * @throws MatrixException
     */
    public MapList getDynamicProjectResourceLoadingColumn(Context context, String[] args)
    throws MatrixException 
	{
		try 
		{
		    if (context == null) 
		    {
		        throw new IllegalArgumentException("context");
		    }
		    Map programMap = (HashMap) JPO.unpackArgs(args);
		    Map requestMap = (Map) programMap.get("requestMap");
		    MapList mlColumnsMapList = new MapList();
		    Map requestValuesMap = (Map) requestMap.get("RequestValuesMap");
		    String strLanguage = (String)requestMap.get("languageStr");
		    Map columnMap = null;
		    Map settingsMap = null;
		    i18nNow loc = new i18nNow();
		    final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
	    	String stri18ColumnName =  (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourcePlanning.Report.Projects");
	    	String strColumnName =  "Projects";
	        columnMap = new HashMap();
	        settingsMap = new HashMap();
	        columnMap.put("settings", settingsMap);
	        columnMap.put("name", strColumnName);//TODO English
	        columnMap.put("label", stri18ColumnName);//TODO Translated
	        settingsMap.put("Registered Suite","eServiceSuiteProgramCentral");
	        settingsMap.put("function","getColumnProjectData");
	        settingsMap.put("program","emxResourcePoolReports");
	        settingsMap.put("Sortable","false");
	        settingsMap.put("Export","true");
	        settingsMap.put("Column Type","programHTMLOutput");
	        //settingsMap.put("Style Program","emxResourcePoolReports");
        	//settingsMap.put("Style Function","getStyleInfoForResourceLoadingReport");
	        mlColumnsMapList.add(columnMap);
	        return mlColumnsMapList;
		}         
		catch (IllegalArgumentException iaexp) 
		{
		    iaexp.printStackTrace();
		    throw new MatrixException(iaexp);
		} 
		catch (Exception exp) 
		{
		    exp.printStackTrace();
		    throw new MatrixException(exp);
		}
	}
    
    /**
     * @param context
     * @param args
     * @return
     * @throws MatrixException
     */
    public MapList getDynamicWBSResourceLoadingColumn(Context context, String[] args)
    throws MatrixException 
	{
		try 
		{
		    if (context == null) 
		    {
		        throw new IllegalArgumentException("context");
		    }
		    Map programMap = (HashMap) JPO.unpackArgs(args);
		    Map requestMap = (Map) programMap.get("requestMap");
		    MapList mlColumnsMapList = new MapList();
		    Map requestValuesMap = (Map) requestMap.get("RequestValuesMap");
		    String strLanguage = (String)requestMap.get("languageStr");
		    Map columnMap = null;
		    Map settingsMap = null;
		    i18nNow loc = new i18nNow();
		    final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
	    	String stri18ColumnName =  (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourcePlanning.Report.WBS");
	    	String strColumnName =  "WBS";
	        columnMap = new HashMap();
	        settingsMap = new HashMap();
	        columnMap.put("settings", settingsMap);
	        columnMap.put("name", strColumnName);//TODO English
	        columnMap.put("label", stri18ColumnName);//TODO Translated
	        settingsMap.put("Registered Suite","eServiceSuiteProgramCentral");
	        settingsMap.put("function","getColumnWBSData");
	        settingsMap.put("program","emxResourcePoolReports");
	        settingsMap.put("Sortable","false");
	        settingsMap.put("Export","true");
	        settingsMap.put("Column Type","programHTMLOutput");
	        //settingsMap.put("Style Program","emxResourcePoolReports");
        	//settingsMap.put("Style Function","getStyleInfoForResourceLoadingReport");
	        mlColumnsMapList.add(columnMap);
	        return mlColumnsMapList;
		}         
		catch (IllegalArgumentException iaexp) 
		{
		    iaexp.printStackTrace();
		    throw new MatrixException(iaexp);
		} 
		catch (Exception exp) 
		{
		    exp.printStackTrace();
		    throw new MatrixException(exp);
		}
	}
    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTableResourceLoadingReportData(Context context, String[] args)
    throws MatrixException 
    {   
        MapList mlResourceUtilDataList  = new MapList();
        try 
        {            
            if (context == null) 
            {
                throw new IllegalArgumentException("context");
            }
            Map programMap = (Map) JPO.unpackArgs(args);
            Map paramList = (Map)programMap.get("paramList");
            Map requestValuesMap = (Map) programMap.get("RequestValuesMap");
		    String strLanguage = (String)programMap.get("languageStr");
		    String strSelectedProjectFilter = "";
		    boolean isProjectFilterActive = false; 
		    if(null!=requestValuesMap.get("PMCReportResourceLoadingProjectFilter"))
		    {
		    	strSelectedProjectFilter = ((String[])requestValuesMap.get("PMCReportResourceLoadingProjectFilter"))[0];
		    }
		    if((!"".equals(strSelectedProjectFilter)) && (!"All".equals(strSelectedProjectFilter)))
		    {
		    	isProjectFilterActive = true;
		    }
            String[] strTableRowIdValues =  (String[])requestValuesMap.get("ResourcePoolId");
            String strResourceObjectId = strTableRowIdValues[0];
            StringList slTableRowIdValuesSplit = FrameworkUtil.split(strResourceObjectId, ",");
            String[] strResourceIds = new String[slTableRowIdValuesSplit.size()];
            slTableRowIdValuesSplit.copyInto(strResourceIds);
            
            StringList slProjectIdList = null;
        	StringList slDistinctProjectIdList = new StringList();
        	Map mapQueryWithData = null;
        	Map mapMonthYearValues = null;
        	String strPersonId = "";

        	String strFromDate = "";
            String strToDate = "";
            strFromDate = null!=requestValuesMap.get("PMCCustomFilterFromDate")?((String[])requestValuesMap.get("PMCCustomFilterFromDate"))[0]:"";
            strToDate   = null!=requestValuesMap.get("PMCCustomFilterToDate")?((String[])requestValuesMap.get("PMCCustomFilterToDate"))[0]:"";
            StringList slMonthYearList = getTimeframes(strFromDate, strToDate);
            MapList mlPersonResLoading = getPersonResourceLoadingAllocation(context, slTableRowIdValuesSplit);
            Map mapProjectMemberRelInfoList = getResourceLoadingReportData(context, mlPersonResLoading,slMonthYearList);
            Map mapMemberTotalLoadingInfoList = new HashMap();
            if(isProjectFilterActive)
            {
            	mapMemberTotalLoadingInfoList = getTotalFilterProjectResourceLoadingReportData(context, mlPersonResLoading, slMonthYearList,strSelectedProjectFilter);
            }
            else
            {
            	mapMemberTotalLoadingInfoList = getTotalResourceLoadingReportData(context, mlPersonResLoading, slMonthYearList);
            }
            
            Map mapTableRow = null;
            Map mapColumnsData = null;
            StringList slProjectNameList = null;
            String strDataKey = "";
            String strPersonName = "";
            Map mapPersonInfoList = getPersonIdsSorter(context, slTableRowIdValuesSplit);
            StringList slPersonIDSortedList = (StringList)mapPersonInfoList.get("PERSONIDS");
            StringList slPersonNameSortedList = (StringList)mapPersonInfoList.get("PERSONNAMES");
            
            for (int pIndex =0; pIndex< slPersonIDSortedList.size(); pIndex++) 
        	{
        		strPersonId = (String)slPersonIDSortedList.get(pIndex);
        		strPersonName = (String)(slPersonNameSortedList.get(pIndex));
        		boolean isPersonNameDisplayed = false;
			    String strPersonFullName = getPersonResourceLoadingHRefLink(context, strPersonId, strLanguage, strPersonName, strFromDate, strToDate);
			    mapColumnsData = new HashMap();
        		if(null!=mapProjectMemberRelInfoList.get(strPersonId))
        		{
	        		mapQueryWithData = (Map)mapProjectMemberRelInfoList.get(strPersonId);
				    slProjectIdList = new StringList();
	    			isPersonNameDisplayed = false;
	                Vector vector = (Vector)mapQueryWithData.get("data");
	                boolean isProjectNameDispayed = false;
	                if(vector!=null && vector.size()>0)
	                {
	                	slProjectIdList = new StringList();
	                	Vector vProjectTaskSortedList = getProjectTaskSorterList(context, vector);
	                	int nPerson = 0;
	                	for(int i = 0; i<vProjectTaskSortedList.size(); i++)
	                	{
	                		HashMap hitem = (HashMap)vProjectTaskSortedList.get(i);
	                		String strProjectId = (String)hitem.get("projectid");
	                		String strTaskName = (String)hitem.get("taskname");
	                		String strTaskId = (String)hitem.get("taskid");
	                		String strProjectName = (String)hitem.get("projectName");
	                		if(isProjectFilterActive && (!strSelectedProjectFilter.equals(strProjectName)))
	                		{
	                			continue;
	                		}
	                		mapColumnsData =  (Map)hitem.get("Columns Data");
	    					isProjectNameDispayed = false;
	        				mapTableRow = new HashMap();
	        				if(!slProjectIdList.contains(strProjectId))
	    					{
	        					slProjectIdList.add(strProjectId);
	    					}
	        				else
	        				{
	        					isProjectNameDispayed = true;
	        				}
	        				if(nPerson>0)
	        				{
	        					isPersonNameDisplayed = true;
	        				}
	        				nPerson++;
	        				DomainObject dmnObject = DomainObject.newInstance(context);
	        				dmnObject.setId(strTaskId);
	        				Access accessObj = dmnObject.getAccessMask(context);
	        				boolean hasAccess = false;
	        				if(accessObj.hasReadAccess() && accessObj.hasShowAccess())
	        				{
	        					hasAccess = true;
	        				}
	        				String strHRefTaskName = getProgramCentralTableTreeLink(context, DomainConstants.TYPE_TASK, strTaskId, strTaskName, hasAccess);
	        				mapTableRow = getResourceLoadingReportObjectListData(context, strPersonFullName, strPersonId, isPersonNameDisplayed, isProjectNameDispayed, strProjectId, strProjectName, strHRefTaskName, mapColumnsData);
	        				mlResourceUtilDataList.add(mapTableRow);
	    				}
	                	if(slProjectIdList.size()>0)
	                	{
		                	mapQueryWithData = (HashMap)mapMemberTotalLoadingInfoList.get(strPersonId);
		            		mapColumnsData =  (Map)mapQueryWithData.get("Columns Data");
		            		i18nNow loc = new i18nNow();
		        		    final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
		        	    	String stri18ColumnName =  (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Total");
			                mapTableRow = getResourceLoadingReportObjectListData(context, stri18ColumnName, "Total", false, true, "", "", "", mapColumnsData);
		    				mlResourceUtilDataList.add(mapTableRow);
	                	}
	    			}
	                else
	                {
	                	if(!isProjectFilterActive)
	                	{
	                		mapTableRow = getResourceLoadingReportObjectListData(context, strPersonFullName,strPersonId, isPersonNameDisplayed, true, "", "", "", mapColumnsData);
	                		mlResourceUtilDataList.add(mapTableRow);
	                	}
	                }
	        	}
        		else
        		{
        			if(!isProjectFilterActive)
                	{
        				mapTableRow = getResourceLoadingReportObjectListData(context, strPersonFullName,strPersonId, isPersonNameDisplayed, true, "", "", "", mapColumnsData);
        				mlResourceUtilDataList.add(mapTableRow);
                	}
        		}
        	}
            return mlResourceUtilDataList;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
    
    private Vector getProjectTaskSorterList(Context context, Vector vector) 
    {
    	Vector vProjectTaskSortedList = new Vector();
    	Vector vTaskMapList = null;
    	StringList slProjectNameList = new StringList();
    	StringList slDummyProjectNameList = new StringList();
    	StringList slDummyProjectIdList = new StringList();
    	StringList slTaskIdList = null;
    	StringList slTaskNameList = null;
    	
    	Map mapProjectTaskList = new HashMap();
    	for(int i = 0; i<vector.size(); i++)
    	{
    		vTaskMapList = new StringList();
    		slTaskIdList = new StringList();
        	slTaskNameList = new StringList();
    		HashMap hitem = new HashMap();
    		hitem.putAll((HashMap)vector.get(i));
    		String strProjectId = (String)hitem.get("projectid");
    		String strTaskName = (String)hitem.get("taskname");
    		String strTaskId = (String)hitem.get("taskid");
    		String strProjectName = (String)hitem.get("projectName");
    		if(!slDummyProjectIdList.contains(strProjectId))
    		{
    			slProjectNameList.add(strProjectName);
    			slDummyProjectNameList.add(strProjectName);
    			slDummyProjectIdList.add(strProjectId);
    		}
    		if(null!=mapProjectTaskList.get(strProjectId+"_TaskName"))
    		{
    			slTaskNameList = (StringList)mapProjectTaskList.get(strProjectId+"_TaskName");
    		}
    		if(null!=mapProjectTaskList.get(strProjectId+"_TaskId"))
    		{
    			slTaskIdList = (StringList)mapProjectTaskList.get(strProjectId+"_TaskId");
    		}
    		slTaskIdList.add(strTaskId);
    		slTaskNameList.add(strTaskName);
    		mapProjectTaskList.put(strTaskId, hitem);
    		mapProjectTaskList.put(strProjectId+"_TaskName", slTaskNameList);
    		mapProjectTaskList.put(strProjectId+"_TaskId", slTaskIdList);
		}
    	Collections.sort(slProjectNameList);
    	for(int j=0; j<slProjectNameList.size();j++)
    	{
    		int nIndexOfProjectName = slDummyProjectNameList.indexOf(slProjectNameList.get(j));
    		String strProjectId = (String) slDummyProjectIdList.get(nIndexOfProjectName);
    		slDummyProjectIdList.remove(nIndexOfProjectName);
    		slDummyProjectNameList.remove(nIndexOfProjectName);
    		StringList slDummyTaskIdList = new StringList(); 
    		slDummyTaskIdList.addAll((StringList)mapProjectTaskList.get(strProjectId+"_TaskId"));
    		StringList slDummyTaskNameList = new StringList();
    		slDummyTaskNameList.addAll((StringList)mapProjectTaskList.get(strProjectId+"_TaskName"));
    		slTaskNameList = new StringList();
    		slTaskNameList.addAll((StringList)mapProjectTaskList.get(strProjectId+"_TaskName"));
    		Collections.sort(slTaskNameList);
    		for(int i=0; i<slTaskNameList.size(); i++)
    		{
    			int nIndexOfTaskName = slDummyTaskNameList.indexOf(slTaskNameList.get(i));
        		String strTaskId = (String) slDummyTaskIdList.get(nIndexOfTaskName);
        		slDummyTaskIdList.remove(nIndexOfTaskName);
        		slDummyTaskNameList.remove(nIndexOfTaskName);
        		vProjectTaskSortedList.add((Map)(mapProjectTaskList.get(strTaskId)));
    		}
    	}
    	return vProjectTaskSortedList;
    	
	}
	/**
     * @param context
     * @param slPersonIdList
     * @return
     * @throws MatrixException
     */
    public MapList getPersonResourceLoadingAllocation(Context context, StringList slPersonIdList) throws MatrixException
    {
    	try
    	{
    		Person person = null;
    		String strUserFullName = "";
    		MapList mlPersonResLoading = new MapList();
    		ResourceLoading resourceLoading = new ResourceLoading (context);
    		Task task = (Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
    		for(int i=0; i<slPersonIdList.size(); i++)
    		{
    			String strPersonId = (String)slPersonIdList.get(i);
    			DomainObject objPerson = DomainObject.newInstance(context, strPersonId);
    			objPerson.open(context);
    			String userName =objPerson.getName();
    			String personId =objPerson.getId();
    			person = person.getPerson(context,userName);
    			String fName = person.getAttributeValue(context,DomainConstants.ATTRIBUTE_FIRST_NAME);
    			String lName = person.getAttributeValue(context,DomainConstants.ATTRIBUTE_LAST_NAME);
    			strUserFullName = lName + ", " + fName;

    			StringList busSelects = new StringList(7);
    			busSelects.add(task.SELECT_ID);
    			busSelects.add(task.SELECT_NAME);
    			busSelects.add(task.SELECT_CURRENT);
    			busSelects.add(task.SELECT_PERCENT_COMPLETE);
    			busSelects.add(task.SELECT_TASK_ESTIMATED_START_DATE);
    			busSelects.add(task.SELECT_TASK_ESTIMATED_FINISH_DATE);
    			busSelects.add(task.SELECT_TASK_ACTUAL_FINISH_DATE);
    			busSelects.add(task.SELECT_TASK_ESTIMATED_DURATION);

    			String busWhere = " current!='Complete'";
    			//Added:nr2:PRG:R210:27-Aug 2010:IR-057835V6R2011x
                busWhere += " && relationship[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.relationship[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current!='" + ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD + "'";
                busWhere += " && relationship[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.relationship[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current!='" + ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL + "'";
                //End:nr2:PRG:R210:27-Aug 2010:IR-057835V6R2011x
    			String taskId = "";
    			String sStartdate = "";
    			String sEndDate = "";
    			String task_duration = "";

    			String type_Person=(String)PropertyUtil.getSchemaProperty(context,"type_Person");
    			String rel_assigned=(String)PropertyUtil.getSchemaProperty(context,"relationship_AssignedTasks");
    			HashMap dataMap = new HashMap();
    			HashMap itemData=null;
    			Vector vec = null;
    			MapList assignmentList = person.getAssignments(context, busSelects, busWhere, false, true);
    			String rowValue="";
    			String stask_name="";
    			vec = new Vector();

    			for(int g=0;g<assignmentList.size();g++)
    			{
    				itemData = new HashMap();
    				stask_name="";
    				taskId =(String)((Map)assignmentList.get(g)).get("id");
    				stask_name = (String)((Map)assignmentList.get(g)).get("name");
    				task.setId(taskId);

    				String attribute_ProjectVisibility=(String)PropertyUtil.getSchemaProperty(context,"attribute_ProjectVisibility");

    				StringList busTaskSelects = new StringList(1);
    				busTaskSelects.add(task.SELECT_ID);
    				busTaskSelects.add(task.SELECT_NAME);
    				busTaskSelects.addElement("attribute[" + attribute_ProjectVisibility + "]");

    				// Do not get the data as super user, user should not see which he does not have access to
    				Map listProj = (Map)task.getProject(context,busTaskSelects, true);

    				////Get Project data
    				//StringList busProjectSelects = new StringList();
    				//busProjectSelects.addElement(DomainObject.SELECT_ID);
    				//Map projMap = (Map) task.getProject(context,busProjectSelects);
    				if(listProj != null )
    				{
    					String projID = (String)listProj.get(task.SELECT_ID);
    					String projVisibility = (String)listProj.get("attribute[" + attribute_ProjectVisibility + "]");
    					//if visibility is set to members and the person is not a member of the project
    					//then do not include this task in the report.
    					boolean checkmembership = resourceLoading.isPersonProjectMember(context,projID,personId);
    					if(projVisibility != null && projVisibility.trim().length()>0 &&
    							projVisibility.equalsIgnoreCase("Members") && !checkmembership)
    					{
    						continue;
    					}
    				}
    				else
    				{
    					continue;
    				}

    				//check to make sure that the task does not have any children before adding it to
    				//the list of tasks
    				// Do not get the data as super user, user should not see which he does not have access to
    				MapList testMap = task.getTasks(context,task, 1, busTaskSelects, new StringList(), false, true);
    				if(testMap!=null && testMap.size()>0)
    				{
    					continue;
    				}

    				String projId = "";
    				String projName = "";
    				if(listProj != null )
    				{
    					projId = (String)listProj.get(task.SELECT_ID);
    					projName =(String)listProj.get(task.SELECT_NAME);
    					sStartdate =(String)((Map)assignmentList.get(g)).get(task.SELECT_TASK_ESTIMATED_START_DATE);
    					sEndDate =(String)((Map)assignmentList.get(g)).get(task.SELECT_TASK_ESTIMATED_FINISH_DATE);
    					task_duration =(String)((Map)assignmentList.get(g)).get(task.SELECT_TASK_ESTIMATED_DURATION);
    					// Do not get the data as super user, user should not see which he does not have access to
    					Vector taskVec = resourceLoading.getAllocation(context,taskId,rel_assigned,type_Person,true,false,(short)1, true);

    					// Do not get the data as super user, user should not see which he does not have access to
    					MapList ml = (MapList)resourceLoading.getAssigneeInfo(context,taskId, true);
    					String totalAssingees = "" + ml.size();
    					String alloc_value = resourceLoading.getAllocValue(taskVec,strPersonId);
    					String userCalendarId = ResourceLoading.getUserCalendar(context,strPersonId);
    					ArrayList hmInfo = new ArrayList();
    					if (userCalendarId != null && !"".equals(userCalendarId) && !"null".equals(userCalendarId))
    					{
    						Date sttDate = new Date(sStartdate);
    						Date eddDate = new Date(sEndDate);
    						hmInfo = ResourceLoading.getDaysInRange(context, userCalendarId, sttDate,eddDate,1);
    					}
    					else{
    						hmInfo = ResourceLoading.getDateRangeData(sStartdate,sEndDate,true);
    					}
    					itemData.put("taskid",taskId);
    					itemData.put("taskname",stask_name);
    					itemData.put("projectid",projId);
    					itemData.put("projectName",projName);
    					itemData.put("taskDays",hmInfo);
    					itemData.put("duration",task_duration);
    					itemData.put("assignees",totalAssingees);
    					itemData.put("allocation",alloc_value);
    					vec.add(itemData);
    				}
    			}
    			dataMap.put("personid",strPersonId);
    			dataMap.put("fullname",strUserFullName);
    			dataMap.put("data",vec);
    			mlPersonResLoading.add(dataMap);
    		}		
    		return mlPersonResLoading;
    	}
    	catch(Exception ex)
    	{
    		throw new MatrixException(ex);
    	}
    }
    
    public Map getResourceLoadingReportData(Context context, MapList mlPersonResLoading, StringList slTimeLineList) throws MatrixException
    {
    	Map mapPersonTimeLineFTE = new HashMap(); 
    	try 
    	{
    		Map mapTimeLineFTE = null;
    		ResourceLoading resourceLoading = new ResourceLoading (context);
    		FTE fte = FTE.getInstance(context);
            String strFTETimeline = fte.getTimeframeConfigName();
    		for(int j=0;j<mlPersonResLoading.size(); j++)
    		{
    			HashMap topMap = (HashMap)mlPersonResLoading.get(j);
    			String pid = (String)topMap.get("personid");
    			String full_n = (String)topMap.get("fullname");
                Vector vector = (Vector)topMap.get("data");
                if(vector!=null && vector.size()>0)
                {
                	for(int i = 0; i<vector.size(); i++)
                	{
                		HashMap hitem = (HashMap)vector.get(i);
                		pid = (String)topMap.get("personid");
                		mapTimeLineFTE = new HashMap();
		    			mapTimeLineFTE = getResourceLoadingFTECalculation(context, hitem, pid, slTimeLineList, strFTETimeline, false);
		    			hitem.put("Columns Data", mapTimeLineFTE);
                	}
                }
                mapPersonTimeLineFTE.put(pid, topMap);
    		}
    	}
    	catch (Exception e) 
    	{
    		throw new MatrixException(e);
    	}
    	return mapPersonTimeLineFTE;
    }
    
    public Map getProjectResourceLoadingReportData(Context context, MapList mlPersonResLoading, StringList slTimeLineList) throws MatrixException
    {
    	Map mapPersonTimeLineFTE = new HashMap(); 
    	try 
    	{
    		Map mapProjectTimeLineFTE = null;
    		Map mapTimeLineFTE = null;
    		Map mapTimeLineTaskFTE = null;
    		ResourceLoading resourceLoading = new ResourceLoading (context);
    		FTE fte = FTE.getInstance(context);
            String strFTETimeline = fte.getTimeframeConfigName();
            Map mapProjectFTEValue = null;
    		for(int j=0;j<mlPersonResLoading.size(); j++)
    		{
    			HashMap topMap = (HashMap)mlPersonResLoading.get(j);
    			String pid = (String)topMap.get("personid");
    			String full_n = (String)topMap.get("fullname");
                Vector vector = (Vector)topMap.get("data");
                StringList slProjectIdList = new StringList();
                mapProjectTimeLineFTE = new HashMap();
                mapProjectFTEValue = new HashMap();
                if(vector!=null && vector.size()>0)
                {
                	mapTimeLineFTE = null;
                	StringList slProjectNameList = new StringList();
                	StringList slProjectIdsList = new StringList();
                	for(int i = 0; i<vector.size(); i++)
                	{
                		HashMap hitem = (HashMap)vector.get(i);
                		String strProjectId = (String)hitem.get("projectid");
                		String strProjectName = (String)hitem.get("projectName");
        				if(!slProjectIdList.contains(strProjectId))
    					{
        					slProjectIdList.add(strProjectId);
        					mapProjectFTEValue.put(strProjectId, strProjectName);
        					if(null!=mapProjectFTEValue.get(pid+"_ProjectName"))
        		    		{
        						slProjectNameList = (StringList)mapProjectFTEValue.get(pid+"_ProjectName");
        		    		}
        		    		if(null!=mapProjectFTEValue.get(pid+"_ProjectId"))
        		    		{
        		    			slProjectIdsList = (StringList)mapProjectFTEValue.get(pid+"_ProjectId");
        		    		}
        		    		slProjectNameList.add(strProjectName);
        		    		slProjectIdsList.add(strProjectId);
        		    		mapProjectFTEValue.put(pid+"_ProjectId",slProjectIdsList);
        		    		mapProjectFTEValue.put(pid+"_ProjectName",slProjectNameList);
    					}
                		mapTimeLineTaskFTE = new HashMap();
		    			mapTimeLineTaskFTE = getResourceLoadingFTECalculation(context, hitem, pid, slTimeLineList, strFTETimeline, false);
		    			if(null!=mapProjectTimeLineFTE.get(strProjectId))
		    			{
		    				mapTimeLineFTE = (Map)mapProjectTimeLineFTE.get(strProjectId);
		    			}
		    			else
		    			{
		    				mapTimeLineFTE = new HashMap();
		    			}
		    			for (int nTimeLine=0; nTimeLine<slTimeLineList.size();nTimeLine++ ) 
		    			{
							String strTimeLine = (String)slTimeLineList.get(nTimeLine);
							double strFTEValue = Task.parseToDouble((String)mapTimeLineTaskFTE.get(strTimeLine));
							double strTotalFTEValue = 0D;
							if(null!=mapTimeLineFTE.get(strTimeLine))
							{
								strTotalFTEValue = Task.parseToDouble((String)mapTimeLineFTE.get(strTimeLine));
							}
							strTotalFTEValue = strFTEValue+strTotalFTEValue;
							mapTimeLineFTE.put(strTimeLine, resourceLoading.resultFormatted(strTotalFTEValue));
						}
		    			mapProjectTimeLineFTE.put(strProjectId, mapTimeLineFTE);
                	}
                }
                mapProjectFTEValue.put("Columns Data", mapProjectTimeLineFTE);
                mapProjectFTEValue.put("personid", pid);
                mapPersonTimeLineFTE.put(pid, mapProjectFTEValue);
    		}
    	}
    	catch (Exception e) 
    	{
    		throw new MatrixException(e);
    	}
    	return mapPersonTimeLineFTE;
    }
    
    public Map getTotalResourceLoadingReportData(Context context, MapList mlPersonResLoading, StringList slTimeLineList) throws MatrixException
    {
    	Map mapPersonTimeLineFTE = new HashMap(); 
    	try 
    	{
    		Map mapTimeLineFTE = null;
    		ResourceLoading resourceLoading = new ResourceLoading (context);
    		FTE fte = FTE.getInstance(context);
            String strFTETimeline = fte.getTimeframeConfigName();
    		for(int j=0;j<mlPersonResLoading.size(); j++)
    		{
    			HashMap topMap = (HashMap)mlPersonResLoading.get(j);
    			String pid = (String)topMap.get("personid");
    			String full_n = (String)topMap.get("fullname");
                mapTimeLineFTE = new HashMap();
                mapTimeLineFTE = getResourceLoadingFTECalculation(context, topMap, pid, slTimeLineList, strFTETimeline, true);
                topMap.put("Columns Data", mapTimeLineFTE);
                mapPersonTimeLineFTE.put(pid, topMap);
    		}
    	}
    	catch (Exception e) 
    	{
    		throw new MatrixException(e);
    	}
    	return mapPersonTimeLineFTE;
    }
    
    public Map getTotalFilterProjectResourceLoadingReportData(Context context, MapList mlPersonResLoading, StringList slTimeLineList, String strFilterProjectName) throws MatrixException
    {
    	Map mapPersonTimeLineFTE = new HashMap(); 
    	try 
    	{
    		Map mapProjectTimeLineFTE = null;
    		Map mapTimeLineFTE = null;
    		Map mapTimeLineTaskFTE = null;
    		ResourceLoading resourceLoading = new ResourceLoading (context);
    		FTE fte = FTE.getInstance(context);
            String strFTETimeline = fte.getTimeframeConfigName();
            Map mapProjectFTEValue = null;
    		for(int j=0;j<mlPersonResLoading.size(); j++)
    		{
    			HashMap topMap = (HashMap)mlPersonResLoading.get(j);
    			String pid = (String)topMap.get("personid");
    			String full_n = (String)topMap.get("fullname");
                Vector vector = (Vector)topMap.get("data");
                StringList slProjectIdList = new StringList();
                mapProjectTimeLineFTE = new HashMap();
                mapProjectFTEValue = new HashMap();
                if(vector!=null && vector.size()>0)
                {
                	mapTimeLineFTE = null;
                	StringList slProjectNameList = new StringList();
                	StringList slProjectIdsList = new StringList();
                	for(int i = 0; i<vector.size(); i++)
                	{
                		HashMap hitem = (HashMap)vector.get(i);
                		String strProjectId = (String)hitem.get("projectid");
                		String strProjectName = (String)hitem.get("projectName");
                		if(!strFilterProjectName.equals(strProjectName))
                		{
                			continue;
                		}
                		mapTimeLineTaskFTE = new HashMap();
		    			mapTimeLineTaskFTE = getResourceLoadingFTECalculation(context, hitem, pid, slTimeLineList, strFTETimeline, false);
		    			if(null!=mapProjectTimeLineFTE.get(pid))
		    			{
		    				mapTimeLineFTE = (Map)mapProjectTimeLineFTE.get(pid);
		    			}
		    			else
		    			{
		    				mapTimeLineFTE = new HashMap();
		    			}
		    			for (int nTimeLine=0; nTimeLine<slTimeLineList.size();nTimeLine++ ) 
		    			{
							String strTimeLine = (String)slTimeLineList.get(nTimeLine);
							double strFTEValue = Task.parseToDouble((String)mapTimeLineTaskFTE.get(strTimeLine));
							double strTotalFTEValue = 0D;
							if(null!=mapTimeLineFTE.get(strTimeLine))
							{
								strTotalFTEValue = Task.parseToDouble((String)mapTimeLineFTE.get(strTimeLine));
							}
							strTotalFTEValue = strFTEValue+strTotalFTEValue;
							mapTimeLineFTE.put(strTimeLine, resourceLoading.resultFormatted(strTotalFTEValue));
						}
		    			mapProjectTimeLineFTE.put(pid, mapTimeLineFTE);
                	}
                }
                mapTimeLineFTE = (Map)mapProjectTimeLineFTE.get(pid);
                topMap.put("Columns Data", mapTimeLineFTE);
                mapPersonTimeLineFTE.put(pid, topMap);
    		}
    	}
    	catch (Exception e) 
    	{
    		throw new MatrixException(e);
    	}
    	return mapPersonTimeLineFTE;
    }
    /**
     * Gets the data for the column "<name>" for table "<name>"
     * 
     * @param context The matrix context object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing
     * @throws Exception if operation fails
     */
    public Vector getColumnWBSData(Context context, String[] args) throws Exception 
    {
    	try 
    	{
    		// Create result vector
    		Vector vecResult = new Vector();
    		HashMap programMap = (HashMap) JPO.unpackArgs(args);
    		MapList mlObjectList = (MapList) programMap.get("objectList");

    		Map mapObjectInfo = null;
    		for (Iterator itrTableRows = mlObjectList.iterator(); itrTableRows.hasNext();) 
    		{
    			mapObjectInfo = (Map) itrTableRows.next();
    			vecResult.add (mapObjectInfo.get("WBSName"));
    		}
    		return vecResult;
    	} 
    	catch (Exception exp) 
    	{
    		exp.printStackTrace();
    		throw exp;
    	}
    }

    private String getProgramCentralTableTreeLink(Context context, String strObjectType, String strObjectId, String strObjectName, boolean hasAccess) throws FrameworkException
    {
    	StringBuffer prjHyperLnk = new StringBuffer();
    	String strProjectTypeIcon = "";
    	String strSymbolicProjectType = FrameworkUtil.getAliasForAdmin(context, "Type", strObjectType, true);
    	strProjectTypeIcon = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon." + strSymbolicProjectType);
    	if(hasAccess)
    	{
	    	prjHyperLnk.append("<a href='../common/emxTree.jsp?emxSuiteDirectory=programcentral&amp;suiteKey=ProgramCentral");
	    	prjHyperLnk.append("&amp;objectId=").append(strObjectId).append("'>");
	    	prjHyperLnk.append("<img border=\"0\" src=\"images/"+strProjectTypeIcon+"\" />");
	    	prjHyperLnk.append(strObjectName);
	    	prjHyperLnk.append("</a>");
    	}
    	else
    	{
    		prjHyperLnk.append("<img border=\"0\" src=\"images/"+strProjectTypeIcon+"\" />");
	    	prjHyperLnk.append(strObjectName);
    	}
    	String strProjectLink = prjHyperLnk.toString();
    	return strProjectLink;
    }

    private String getOrganizationHRefLink(Context context, String strType, String strOrganizationId, String strOrganizationName) throws FrameworkException
    {
    	String strTypeIcon = "";
    	StringBuffer resPoolHyperLnk = new StringBuffer();
    	String strSymbolicType = FrameworkUtil.getAliasForAdmin(context, "Type", strType, true);
    	strTypeIcon = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon." + strSymbolicType);
    	resPoolHyperLnk.append("<a href='../common/emxTree.jsp?treeMenu=PMCResourcePoolTree&amp;DefaultCategory=PMCResourcePoolCategoryResourceRequests");
    	resPoolHyperLnk.append("&amp;objectId=").append(strOrganizationId).append("'>");
    	resPoolHyperLnk.append("<img border=\"0\" src=\"images/"+strTypeIcon+"\" />");
    	resPoolHyperLnk.append(strOrganizationName);
    	resPoolHyperLnk.append("</a>");
    	return resPoolHyperLnk.toString();
    }

    private String getPersonHRefLink(Context context, String strPersonId, String strPersonName) throws FrameworkException
    {
    	StringBuffer strPersonHyperLink = new StringBuffer();
    	String strPersonTypeIcon = "";
    	String strSymbolicPersonType = FrameworkUtil.getAliasForAdmin(context, "Type", DomainConstants.TYPE_PERSON, true);
    	strPersonTypeIcon = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon." + strSymbolicPersonType);
    	strPersonHyperLink.append("<a href='../common/emxTree.jsp?");
    	strPersonHyperLink.append("objectId=").append(strPersonId).append("'>");
    	strPersonHyperLink.append("<img border=\"0\" src=\"images/"+strPersonTypeIcon+"\" />");
    	strPersonHyperLink.append(strPersonName);
    	strPersonHyperLink.append("</a>");
    	String strPersonLink = strPersonHyperLink.toString();
    	return strPersonLink;
    }
    
    private String getPersonResourceLoadingHRefLink(Context context, String strPersonId, String strLanguage, String strPersonName, String strFromDate, String strEndDate) throws FrameworkException
    {
    	StringBuffer strPersonHyperLink = new StringBuffer();
    	String strPersonTypeIcon = "";
    	i18nNow loc = new i18nNow();
	    final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
    	String stri18HeaderName =  (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.ResourcePlanning.Report.PlanVsActualResourceLoadingReport");
    	stri18HeaderName = FrameworkUtil.findAndReplace(stri18HeaderName, "$<name>", strPersonName);
    	String strSymbolicPersonType = FrameworkUtil.getAliasForAdmin(context, "Type", DomainConstants.TYPE_PERSON, true);
    	strPersonTypeIcon = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon." + strSymbolicPersonType);
    	strPersonHyperLink.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxIndentedTable.jsp?")
    			.append("program=emxResourcePoolReports:getTableResourceLoadingPlanvsActualReportData&amp;table=PMCReportResourceLoading")
    			.append("&amp;selection=none&amp;mode=PMCResourceLoadingReport&amp;header=")
    			.append(stri18HeaderName)
    			.append("&amp;helpmarker=emxhelpresourceloadingreport&amp;chartingrequired=false&amp;suiteKey=ProgramCentral&amp;sortColumnName=none")
    			.append("&amp;displaymode=Detail&amp;PMCCustomFilterFromDate=")
    			.append(strFromDate)
    			.append("&amp;PMCCustomFilterToDate=")
    			.append(strEndDate)
    			.append("&amp;");
    	strPersonHyperLink.append("ResourcePoolId=");
    	strPersonHyperLink.append(strPersonId);
    	strPersonHyperLink.append("', ");
    	strPersonHyperLink.append("'700', '600', 'false', 'popup', '')\">");
    	strPersonHyperLink.append("<img border=\"0\" src=\"images/"+strPersonTypeIcon+"\" />");
    	strPersonHyperLink.append(strPersonName);
    	strPersonHyperLink.append("</a>");
    	String strPersonLink = strPersonHyperLink.toString();
    	return strPersonLink;
    }
    
    private Map getResourceLoadingFTECalculation(Context context,HashMap hitem, String pid, StringList slTimeLineList, String strFTETimeline, boolean isTotalCalculation) throws MatrixException
    {
		Map mapTimeLineFTE = new HashMap();
		try 
		{
			FTE fte = FTE.getInstance(context);
			ResourceLoading resourceLoading = new ResourceLoading (context);
			for(int ta = 0; ta<slTimeLineList.size(); ta++)
			{
				double dFTELoading = 0d;
				String strTimeLine = (String)slTimeLineList.get(ta);
				Date startDate = fte.getStartDate(strTimeLine);
				Date endDate = fte.getEndDate(strTimeLine);
				if(FTE_TIMELINE_MONTHLY.equalsIgnoreCase(strFTETimeline))
				{
					long getnum = resourceLoading.getNumworkingDays(context,startDate,pid);
					ArrayList arr = resourceLoading.getworkingDates(context,startDate,pid,1);
					if(isTotalCalculation)
					{
						MapList mlTopMap = new MapList();
						mlTopMap.add(hitem);
						dFTELoading = Task.parseToDouble(resourceLoading.getTotalLoading(mlTopMap,getnum,arr));
					}
					else
					{
						dFTELoading = resourceLoading.get_t_loading(hitem,getnum,arr);
					}
				}
				else if(FTE_TIMELINE_QUARTERLY.equalsIgnoreCase(strFTETimeline))
				{
					ArrayList daysofweek = resourceLoading.getAvailableDates(context, startDate, endDate, pid, 1);
					if(isTotalCalculation)
					{
						MapList mlTopMap = new MapList();
						mlTopMap.add(hitem);
						dFTELoading = Task.parseToDouble(resourceLoading.getTotalQuarterlyLoading(mlTopMap,daysofweek));
					}
					else
					{
						dFTELoading = resourceLoading.getQuarterlyLoading(hitem,daysofweek);
					}
					
				}
				else if(FTE_TIMELINE_WEEKLY.equalsIgnoreCase(strFTETimeline))
				{
					ArrayList daysofweek = resourceLoading.getAvailableDates(context, startDate, endDate, pid, 1);
					if(isTotalCalculation)
					{
						MapList mlTopMap = new MapList();
						mlTopMap.add(hitem);
						dFTELoading = Task.parseToDouble(resourceLoading.getTotalWeeklyLoading(mlTopMap,daysofweek));
					}
					else
					{
						dFTELoading = resourceLoading.getWeeklyLoading(hitem,daysofweek);
					}
				}
				mapTimeLineFTE.put(strTimeLine, resourceLoading.resultFormatted(dFTELoading));
			}
		} 
		catch (NumberFormatException nfe) 
		{
			throw new MatrixException(nfe);
		} 
		catch (Exception ex) 
		{
			throw new MatrixException(ex);
		}
		return mapTimeLineFTE;
    }
    
    /**
     * This method is used for style column and rows of Resource Loading Report
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public StringList getStyleInfoForResourceLoadingReport(Context context, String[] args)  throws Exception 
    {
    	try 
        {
            StringList slFTEStyles = new StringList();
            // Get object list information from packed arguments
            Map programMap = (HashMap) JPO.unpackArgs(args);
            Map columnMap = (HashMap) programMap.get("columnMap");
            String strColumnName = (String)columnMap.get("name");
            MapList mlObjectList = (MapList) programMap.get("objectList");
            Map mapObjectInfo = null;
            Map mapColumnsData = null;
            String strRowName = "";
            for (Iterator itrTableRows = mlObjectList.iterator(); itrTableRows.hasNext();) 
            {
                mapObjectInfo = (Map) itrTableRows.next();
                strRowName = (String)mapObjectInfo.get("PersonId");
                if(strRowName.equalsIgnoreCase("Total"))
                {
                   	slFTEStyles.addElement("ColumnBackGroundColor");
                }
                else
                {
                	slFTEStyles.addElement("");
                }
            }
            return slFTEStyles;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw exp;
        }                    
    }
    
    public Map getPersonIdsSorter(Context context,StringList slPersonIdList) throws Exception
    {
    	Person person = null;
    	StringList slPersonIdSortList = new StringList();
    	StringList slPersonFullNameSortList = new StringList();
    	Map mapPersonIdName = new HashMap();
    	for(int i=0; i<slPersonIdList.size(); i++)
		{
			String strPersonId = (String)slPersonIdList.get(i);
			DomainObject objPerson = DomainObject.newInstance(context, strPersonId);
			objPerson.open(context);
			String userName =objPerson.getName();
			String personId =objPerson.getId();
			person = person.getPerson(context,userName);
			String fName = person.getAttributeValue(context,DomainConstants.ATTRIBUTE_FIRST_NAME);
			String lName = person.getAttributeValue(context,DomainConstants.ATTRIBUTE_LAST_NAME);
			String strUserFullName = lName + ", " + fName;
			mapPersonIdName.put(strUserFullName,personId);
		}
    	StringList slPersonNameList = new StringList();
    	slPersonNameList.addAll(mapPersonIdName.keySet());
    	Collections.sort(slPersonNameList);
    	for(int i=0; i<slPersonNameList.size(); i++)
    	{
    		String strPersonName = (String)slPersonNameList.get(i);
    		slPersonIdSortList.add(mapPersonIdName.get(strPersonName));
    		slPersonFullNameSortList.add(strPersonName);
    	}
    	Map mapPersonInfoList = new HashMap();
    	mapPersonInfoList.put("PERSONIDS", slPersonIdSortList);
    	mapPersonInfoList.put("PERSONNAMES", slPersonFullNameSortList);
    	return mapPersonInfoList;
    }
    
    private Map getResourceLoadingReportObjectListData (Context context, String strPersonName,String strPersonId,boolean isPersonaNameDisplayed, 
    													boolean isProjectNameDispayed,String strProjectId,String strProjectName,
    													String strHRefTaskName, Map mapColumnsData) throws FrameworkException
    {
    	Map mapTableRow = new HashMap();
		if(isPersonaNameDisplayed)
		{
			mapTableRow.put("PersonId","");
			mapTableRow.put("PersonName","");
		}
		else
		{
			mapTableRow.put("PersonName", strPersonName);
			mapTableRow.put("PersonId", strPersonId);
		}
		if(!isProjectNameDispayed)
		{
			String strHRefProjectName = getProgramCentralTableTreeLink(context, DomainConstants.TYPE_PROJECT_SPACE, strProjectId, strProjectName, true);
			mapTableRow.put("ProjectTitle", strProjectName);
			mapTableRow.put("Project i18n Title", strHRefProjectName);
		}
		else
		{
			mapTableRow.put("ProjectTitle", "");
			mapTableRow.put("Project i18n Title", "");
		}
		mapTableRow.put("ProjectId", strProjectId);
		mapTableRow.put("WBSName", strHRefTaskName);
        mapTableRow.put("Columns Data", mapColumnsData);
        return mapTableRow;
    }
    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTableResourceLoadingPlanvsActualReportData(Context context, String[] args)
    throws MatrixException 
    {   
        MapList mlResourceUtilDataList  = new MapList();
        try 
        {            
            if (context == null) 
            {
                throw new IllegalArgumentException("context");
            }
            Map programMap = (Map) JPO.unpackArgs(args);
            Map paramList = (Map)programMap.get("paramList");
            Map requestValuesMap = (Map) programMap.get("RequestValuesMap");
		    
            String[] strTableRowIdValues =  (String[])requestValuesMap.get("ResourcePoolId");
            String strResourceObjectId = strTableRowIdValues[0];
            StringList slTableRowIdValuesSplit = FrameworkUtil.split(strResourceObjectId, ",");
            String[] strResourceIds = new String[slTableRowIdValuesSplit.size()];
            slTableRowIdValuesSplit.copyInto(strResourceIds);
            
        	StringList slDistinctProjectIdList = new StringList();
        	Map mapQueryWithData = null;
        	Map mapMonthYearValues = null;

        	String strFromDate = "";
            String strToDate = "";
            strFromDate = null!=requestValuesMap.get("PMCCustomFilterFromDate")?((String[])requestValuesMap.get("PMCCustomFilterFromDate"))[0]:"";
            strToDate   = null!=requestValuesMap.get("PMCCustomFilterToDate")?((String[])requestValuesMap.get("PMCCustomFilterToDate"))[0]:"";
            StringList slMonthYearList = getTimeframes(strFromDate, strToDate);
            MapList mlPersonResLoading = getPersonResourceLoadingAllocation(context, slTableRowIdValuesSplit);
            Map mapProjectMemberRelInfoList = getProjectResourceLoadingReportData(context, mlPersonResLoading,slMonthYearList);
            mapMonthYearValues = getPeopleProjectAssignmentDataOfResourceRequest(context, strResourceIds, slMonthYearList, "Detail");
            Map mapTableRow = null;
            Map mapColumnsActualData = null;
            Map mapColumnsPlanData = null;
            Map mapColumnsProjectsData = null;
            Map mapColumnsPlanActualData = null;
            
            mapColumnsActualData = new HashMap();
            mapColumnsPlanData = new HashMap();
            mapColumnsPlanActualData = new HashMap();
            if(null!=mapProjectMemberRelInfoList)
            {
            	for (Iterator iterator = mapProjectMemberRelInfoList.keySet().iterator(); iterator.hasNext();) 
            	{
            		String strPersonId = (String)iterator.next();
            		mapColumnsPlanActualData = new HashMap();
            		mapQueryWithData = (Map)mapProjectMemberRelInfoList.get(strPersonId);
            		mapColumnsProjectsData 				= (Map)mapQueryWithData.get("Columns Data");
            		StringList slProjectIdList 			= new StringList();
            		StringList slProjectNameList 		= new StringList();
            		StringList slDummyProjectIdList 	= new StringList();
            		StringList slDummyProjectNameList 	= new StringList();
            		if(null!=mapQueryWithData.get(strPersonId+"_ProjectName"))
            		{
            			slProjectNameList.addAll((StringList)mapQueryWithData.get(strPersonId+"_ProjectName"));
            			slDummyProjectNameList.addAll((StringList)mapQueryWithData.get(strPersonId+"_ProjectName"));
            			slDummyProjectIdList.addAll((StringList)mapQueryWithData.get(strPersonId+"_ProjectId"));
            			Collections.sort(slProjectNameList);
            			for(int j=0; j<slProjectNameList.size(); j++)
            			{
            				int nIndexOfProjectName = slDummyProjectNameList.indexOf(slProjectNameList.get(j));
            				String strProjectId = (String) slDummyProjectIdList.get(nIndexOfProjectName);
            				slDummyProjectIdList.remove(nIndexOfProjectName);
            				slDummyProjectNameList.remove(nIndexOfProjectName);
            				slProjectIdList.add(strProjectId);
            			}
            			for (int i=0; i<slProjectIdList.size(); i++) 
            			{
            				mapColumnsPlanActualData = new HashMap();
            				String strProjectId = (String)slProjectIdList.get(i);
            				String strProjectName = (String)mapQueryWithData.get(strProjectId);
            				String strPlanDataInfo = strPersonId+"$"+strProjectId+"$"+DomainConstants.ATTRIBUTE_RESOURCE_STATE_RANGE_COMMITTED;
            				if(null!=mapMonthYearValues && null!=mapMonthYearValues.get(strPlanDataInfo))
            				{
            					mapColumnsPlanData = (Map)mapMonthYearValues.get(strPlanDataInfo);
            					mapColumnsPlanActualData.put(STRING_PLAN, mapColumnsPlanData);
            				}
            				mapColumnsActualData = (Map)mapColumnsProjectsData.get(strProjectId);
            				mapColumnsPlanActualData.put("Actual", mapColumnsActualData);
            				mapTableRow = new HashMap();
            				mapTableRow = getResourceLoadingReportObjectListData(context, "", strPersonId, true, false, strProjectId, strProjectName, "", mapColumnsPlanActualData);
            				mlResourceUtilDataList.add(mapTableRow);
            			}
            		}
            	}
            }
            return mlResourceUtilDataList;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
    
    
    /**
     * Gets the data for the column "<name>" for table "<name>"
     * 
     * @param context The matrix context object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing
     * @throws Exception if operation fails
     */
    public Vector getColumnPlanVsActualMonthYearData(Context context, String[] args)
            throws Exception 
    {
        try 
        {
            // Create result vector
            Vector vecResult = new Vector();
            // Get object list information from packed arguments
            Map programMap = (HashMap) JPO.unpackArgs(args);
            Map columnMap = (HashMap) programMap.get("columnMap");
            String strColumnName = (String)columnMap.get("name");
            String strTimeLineName = "";
            String strColumnHeaderName = "";
            MapList mlObjectList = (MapList) programMap.get("objectList");
            
            Map mapObjectInfo = null;
            Map mapColumnsPlanActualData = null;
            Map mapColumnsData = null;
            for (Iterator itrTableRows = mlObjectList.iterator(); itrTableRows.hasNext();) 
            {
                mapObjectInfo = (Map) itrTableRows.next();
                if(null!=mapObjectInfo.get("Columns Data"))
                {
                	mapColumnsPlanActualData = (Map)mapObjectInfo.get("Columns Data");
                	int nIndex = strColumnName.indexOf("_");
                	strColumnHeaderName = strColumnName.substring(0, nIndex);
                	strTimeLineName = strColumnName.substring(nIndex+1,strColumnName.length());
                	mapColumnsData = (Map)mapColumnsPlanActualData.get(strColumnHeaderName);
                	if(null!=mapColumnsData && null!=mapColumnsData.get(strTimeLineName))
                	{
                		vecResult.add (mapColumnsData.get(strTimeLineName));
                	}
                	else
                	{
                		vecResult.add ("");
                	}
                }
                else
                {
                	vecResult.add ("");
                }
            }
            return vecResult;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw exp;
        }
    }
    
    private Map getPeopleProjectAssignmentDataOfResourceRequest(Context context, String[] strResourceIds, StringList slMonthYearList, String strSelectedOverallFilter) throws MatrixException
    {
    	StringList slSelectList = new StringList();
    	Map mapMonthYearValues = new HashMap();
        final String SELECT_REL_TO_ALLOCATED_RESOURCE_PLAN_PROJECT_ID = "to["+DomainConstants.RELATIONSHIP_ALLOCATED+"].businessobject.to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].businessobject."+DomainConstants.SELECT_ID;
        final String SELECT_REL_TO_ALLOCATED_RESOURCE_PLAN_PROJECT_NAME = "to["+DomainConstants.RELATIONSHIP_ALLOCATED+"].businessobject.to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].businessobject."+DomainConstants.SELECT_NAME;
        
        slSelectList.add(DomainConstants.SELECT_NAME);
        slSelectList.add(SELECT_REL_TO_ALLOCATED_RESOURCE_PLAN_PROJECT_ID);
        slSelectList.add(SELECT_REL_TO_ALLOCATED_RESOURCE_PLAN_PROJECT_NAME);
        
        Map mapObjectIdWithQueryDataList = getBusinessObjectDataWithSelectList(context, strResourceIds, slSelectList);
        Map mapPersonIdWithProjectIdList = new HashMap();
        Map mapProjectMemberRelInfoList = getPersonProjectMemberAllocationData(context, strResourceIds,slMonthYearList);
        if(null!=mapObjectIdWithQueryDataList)
        {
        	StringList slProjectIdList = null;
        	StringList slDistinctProjectIdList = new StringList();
        	Map mapQueryWithDataList = null;
        	String strPersonId = "";
        	for (Iterator iterator = mapObjectIdWithQueryDataList.keySet().iterator(); iterator.hasNext();) 
        	{
        		strPersonId = (String)iterator.next();
        		mapQueryWithDataList = (Map)mapObjectIdWithQueryDataList.get(strPersonId);
        		slProjectIdList = (StringList)mapQueryWithDataList.get(SELECT_REL_TO_ALLOCATED_RESOURCE_PLAN_PROJECT_ID);
        		if(null!=slProjectIdList && slProjectIdList.size()>0)
        		{
        			for(int index=0;index<slProjectIdList.size();index++)
        			{
        				if(!slDistinctProjectIdList.contains(slProjectIdList.get(index)))
        				{
        					slDistinctProjectIdList.add(slProjectIdList.get(index));
        				}
        			}
        		}
        	}
            String[] strProjectIds = new String[slDistinctProjectIdList.size()];
            slDistinctProjectIdList.copyInto(strProjectIds);
            mapMonthYearValues = getPeopleProjectAssignmentReportData(context,slMonthYearList,strProjectIds, strSelectedOverallFilter);
        }
        return mapMonthYearValues;
    }
    
    public StringList getStyleInfoForPlanvsActualReport(Context context, String[] args)  throws Exception 
    {
    	try 
        {
            StringList slFTEStyles = new StringList();
            // Get object list information from packed arguments
            Map programMap = (HashMap) JPO.unpackArgs(args);
            Map columnMap = (HashMap) programMap.get("columnMap");
            String strColumnName = (String)columnMap.get("name");
            MapList mlObjectList = (MapList) programMap.get("objectList");
            int nIndex = strColumnName.indexOf("_");
        	String strColumnHeaderName = strColumnName.substring(0, nIndex);
        	String strColumnHeaderCheckName = STRING_PLAN;
        	String strTimeLineName = strColumnName.substring(nIndex+1,strColumnName.length());
           
            Map mapObjectInfo = null;
            Map mapColumnsPlanData = null;
            Map mapColumnsActualData = null;
            Map mapColumnsPlanActualData = null;
            
            for (Iterator itrTableRows = mlObjectList.iterator(); itrTableRows.hasNext();) 
            {
                mapObjectInfo = (Map) itrTableRows.next();
                mapColumnsPlanActualData = (Map)mapObjectInfo.get("Columns Data");
                strTimeLineName = strColumnName.substring(nIndex+1,strColumnName.length());
                String strFTEValue= "";
                if(null!=mapColumnsPlanActualData && null!=mapColumnsPlanActualData.get(strColumnHeaderName))
                {
                	mapColumnsActualData = (Map)mapColumnsPlanActualData.get(strColumnHeaderName);
                	strFTEValue= (String)mapColumnsActualData.get(strTimeLineName);
                }
                String strFTEMaxCheckValue = ""; 
                if(null!=mapColumnsPlanActualData && null!=mapColumnsPlanActualData.get(strColumnHeaderCheckName))
                {
	            	mapColumnsPlanData = (Map)mapColumnsPlanActualData.get(strColumnHeaderCheckName);
	            	strFTEMaxCheckValue= (String)mapColumnsPlanData.get(strTimeLineName);
                }
                double nActualFTE = 0D;
                double nMaxFTEPerPerson = 0D;
                try
                {
                	if(null!=strFTEValue && !"".equals(strFTEValue) && !"null".equals(strFTEValue))
                    {
                		nActualFTE = Task.parseToDouble(strFTEValue);
                    }
                	if(null!=strFTEMaxCheckValue && !"".equals(strFTEMaxCheckValue) && !"null".equals(strFTEMaxCheckValue))
                    {
                		nMaxFTEPerPerson = Task.parseToDouble(strFTEMaxCheckValue);
                    }
                }
                catch (NumberFormatException e) 
                {
                	nActualFTE = 0;
                	nMaxFTEPerPerson = 0;
				}
                
                if(nActualFTE>nMaxFTEPerPerson)
                {
                	slFTEStyles.addElement("ColumnBackGroundColor");
                }
                else
                {
                	slFTEStyles.addElement("");
                }
            }
            return slFTEStyles;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw exp;
        }                    
    }
    
    public StringList getAllResourceLoadingReport(Context context,String[] args)throws Exception
    {
    	StringList slProjectNameList = new StringList();
    	String sAll = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Common.All", context.getSession().getLanguage());
    	slProjectNameList.add(sAll);
        try 
        {
        	if (context == null) 
            {
                throw new IllegalArgumentException("context");
            }
            Map programMap = (Map) JPO.unpackArgs(args);
            Map paramList = (Map)programMap.get("paramList");
            Map requestMap = (Map) programMap.get("requestMap");
		    String strLanguage = (String)programMap.get("languageStr");
		    
            String strResourceObjectId =  (String)requestMap.get("ResourcePoolId");
            StringList slTableRowIdValuesSplit = FrameworkUtil.split(strResourceObjectId, ",");
            String[] strResourceIds = new String[slTableRowIdValuesSplit.size()];
            slTableRowIdValuesSplit.copyInto(strResourceIds);
            
            MapList mlPersonResLoading = getPersonResourceLoadingAllocation(context, slTableRowIdValuesSplit);
    		
    		for(int j=0;j<mlPersonResLoading.size(); j++)
    		{
    			HashMap topMap = (HashMap)mlPersonResLoading.get(j);
    			String pid = (String)topMap.get("personid");
    			String full_n = (String)topMap.get("fullname");
    			Vector vector = (Vector)topMap.get("data");
    			if(vector!=null && vector.size()>0)
    			{
    				for(int i = 0; i<vector.size(); i++)
    				{
    					HashMap hitem = (HashMap)vector.get(i);
    					String strProjId = (String)hitem.get("projectid");
    					String strProjName = (String)hitem.get("projectName");
    					if(!slProjectNameList.contains(strProjName))
    					{
    						slProjectNameList.add(strProjName);
    					}
    				}
    			}
    		}
       	}
        catch (Exception e) 
        {
        	throw new MatrixException(e);
        }
        
        return slProjectNameList;
    }
    
    /**
     * Check whether current state of project is Active which 
     * includes Create, Assign and Active state of project)
     * @param strPrjCurrentState Project current State
     * @return true if project is in active state.
     */
    
    private boolean isActiveProject(String strPrjCurrentState)
    {
    	boolean isActiveProject = true;
    	if(!strPrjCurrentState.equals(DomainConstants.STATE_PROJECT_SPACE_CREATE) && !strPrjCurrentState.equals(DomainConstants.STATE_PROJECT_SPACE_ASSIGN)&& !strPrjCurrentState.equals(DomainConstants.STATE_PROJECT_SPACE_ACTIVE))
    	{
    		isActiveProject = false;
    	}
    	return isActiveProject;
    }
}
