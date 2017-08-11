import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.common.MemberRelationship;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.ResourceLoading;
import com.matrixone.apps.program.Task;
import com.matrixone.json.JSONArray;
import com.matrixone.json.JSONObject;

public class emxProgramUIBase_mxJPO {
    
	private final static String ATTRIBUTE_PRIORITY = PropertyUtil.getSchemaProperty("attribute_Priority");
	private final static String SELECT_ATTRIBUTE_PRIORITY ="attribute["+ATTRIBUTE_PRIORITY+"]";
	private final static String SELECT_TASK_DELIVERABLE_CURRENT ="to["+DomainConstants.RELATIONSHIP_TASK_DELIVERABLE+"]+.from.current]";
	private final static String SELECT_RELATIONSHIP_TASK_DELIVERABLE = "to["+DomainConstants.RELATIONSHIP_TASK_DELIVERABLE+"]";
	private final static String SELECT_RELATIONSHIP_ASSIGNED_TASKS ="to["+DomainConstants.RELATIONSHIP_ASSIGNED_TASKS+"]";
	private final static String SELECT_RELATIONSHIP_ASSIGNED_TASKS_FROM_NAME ="to["+DomainConstants.RELATIONSHIP_ASSIGNED_TASKS+"]+.from.name]";
	private final static String SELECT_ASSIGNED_TASKS_PERCENT_ALLOCATION  = "to["+DomainRelationship.RELATIONSHIP_ASSIGNED_TASKS+"].attribute["+ProgramCentralConstants.ATTRIBUTE_PERCENT_ALLOCATION+"]";
	private final static String SELECT_TASK_DELIVERABLE_TASK_ESTIMATED_FINISH_DATA  = "to["+DomainConstants.RELATIONSHIP_TASK_DELIVERABLE+"].from.attribute["+DomainRelationship.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE+"]";
	private final static String SELECT_FROM_SUBTASK = "from["+DomainConstants.RELATIONSHIP_SUBTASK+"]";
	private final static String SELECT_FROM_ATTRIBUTE_TASK_EXTIMATED_FINISH_DATE = "from.attribute["+DomainRelationship.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE+"]";
    private final static String SELECT_ESTIMATED_END_DATE = "attribute[" + DomainConstants.ATTRIBUTE_ESTIMATED_END_DATE + "]";
    private final static String SELECT_ATTRIBUTE_ACTUAL_END_DATE = "attribute[" + DomainConstants.ATTRIBUTE_ACTUAL_END_DATE + "]";
    private final static String ATTRIBUTE_ASSESSMENT_STATUS = PropertyUtil.getSchemaProperty("attribute_AssessmentStatus");
    private final static String SELECT_ATTRIBUTE_ASSESSMENT_STATUS ="attribute["+ATTRIBUTE_ASSESSMENT_STATUS+"]";
    private final static String ATTRIBUTE_ASSESSMENT_COMMENTS = PropertyUtil.getSchemaProperty("attribute_AssessmentComments");
    private final static String SELECT_ATTRIBUTE_ASSESSMENT_COMMENTS ="attribute["+ATTRIBUTE_ASSESSMENT_COMMENTS+"]";
    private final static String ATTRIBUTE_RESOURCE_STATUS = PropertyUtil.getSchemaProperty("attribute_ResourceStatus");
    private final static String SELECT_ATTRIBUTE_RESOURCE_STATUS ="attribute["+ATTRIBUTE_RESOURCE_STATUS+"]";
    private final static String ATTRIBUTE_RESOURCE_COMMENTS = PropertyUtil.getSchemaProperty("attribute_ResourceComments");
    private final static String SELECT_ATTRIBUTE_RESOURCE_COMMENTS ="attribute["+ATTRIBUTE_RESOURCE_COMMENTS+"]";
    private final static String ATTRIBUTE_SCHEDULE_STATUS = PropertyUtil.getSchemaProperty("attribute_ScheduleStatus");
    private final static String SELECT_ATTRIBUTE_SCHEDULE_STATUS ="attribute["+ATTRIBUTE_SCHEDULE_STATUS+"]";
    private final static String ATTRIBUTE_SCHEDULE_COMMENTS = PropertyUtil.getSchemaProperty("attribute_ScheduleComments");
    private final static String SELECT_ATTRIBUTE_SCHEDULE_COMMENTS ="attribute["+ATTRIBUTE_SCHEDULE_COMMENTS+"]";
    private final static String ATTRIBUTE_FINANCE_STATUS = PropertyUtil.getSchemaProperty("attribute_FinanceStatus");
    private final static String SELECT_ATTRIBUTE_FINANCE_STATUS ="attribute["+ATTRIBUTE_FINANCE_STATUS+"]";
    private final static String ATTRIBUTE_FINANCE_COMMENTS = PropertyUtil.getSchemaProperty("attribute_FinanceComments");
    private final static String SELECT_ATTRIBUTE_FINANCE_COMMENTS ="attribute["+ATTRIBUTE_FINANCE_COMMENTS+"]";
    private final static String ATTRIBUTE_RISK_STATUS = PropertyUtil.getSchemaProperty("attribute_RiskStatus");
    private final static String SELECT_ATTRIBUTE_RISK_STATUS ="attribute["+ATTRIBUTE_RISK_STATUS+"]";
    private final static String ATTRIBUTE_RISK_COMMENTS = PropertyUtil.getSchemaProperty("attribute_RiskComments");
    private final static String SELECT_ATTRIBUTE_RISK_COMMENTS ="attribute["+ATTRIBUTE_RISK_COMMENTS+"]";
	private final static String SELECT_FROM_ATTRIBUTE_TASK_ACTUAL_FINISH_DATE = "from.attribute["+DomainRelationship.ATTRIBUTE_TASK_ACTUAL_FINISH_DATE+"]";
    private final static String SELECT_DELEVERABLE_NAME = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.name";
    private final static String ATTRIBUTE_WORK_PHONE_NUMBER = PropertyUtil.getSchemaProperty( "attribute_WorkPhoneNumber" );    
    private final static String SELECT_ATTRIBUTE_WORK_PHONE_NUMBER ="attribute["+ATTRIBUTE_WORK_PHONE_NUMBER+"]";
    private final static String RELATIONSHIP_HAS_EFFORTS = PropertyUtil.getSchemaProperty("relationship_hasEfforts");
    private final static String TYPE_DOCUMENTS = PropertyUtil.getSchemaProperty("type_DOCUMENTS");
    private final static String TYPE_EFFORT = PropertyUtil.getSchemaProperty("type_Effort");
    private final static String RELATIONSHIP_ISSUE = PropertyUtil.getSchemaProperty("relationship_Issue" );
    private final static String TYPE_ISSUE = PropertyUtil.getSchemaProperty("type_Issue" );
    private final static String RELATIONSHIP_PROJECT_ASSESSMENT = PropertyUtil.getSchemaProperty("relationship_ProjectAssessment");
    private final static String TYPE_ASSESSMENT	= PropertyUtil.getSchemaProperty("type_Assessment");
    private final static String SELECT_ATTRIBUTE_TASK_ESTIMATED_END_DATE = "attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE  + "]";
 
	String sColorRed        = "cc0000";
    String sColorGreen      = "009c00";
    String sColorOrange     = "ff7f00";
    String sColorGray       = "97999B";
    String sColor025        = "cedfea";
    String sColor050        = "88b1cc";
    String sColor075        = "508cb4";
    String sColor099        = "2a4b62";
    String sColor100        = "0c151c";      
    SimpleDateFormat sdf;
    String[] sColors        = { "00b2a9", "329cee", "f6bd0f","8BBA00","ec0c41","752fc3","AFD8F8","fad46c","c9ff0d","F984A1","A66EDD"};
    
    public emxProgramUIBase_mxJPO(Context context, String[] args) throws Exception {
    	String dateTimeFormat =EnoviaResourceBundle.getProperty(context, "eServiceSuites.eMatrixDateFormat");
    	if(ProgramCentralUtil.isNullString(dateTimeFormat))
    	{
    		dateTimeFormat = "MM/dd/yyyy hh:mm:ss aaa";
    	}
    	sdf = new SimpleDateFormat(dateTimeFormat,Locale.US);
    }
         
    
    // Projects Dashboard
    public String[] getProjectsDashboardData(Context context, String[] args) throws Exception {
  
                
        String[] aResults           = new String[50];
        String[] initargs           = new String[] {};
        HashMap params              = new HashMap();
        HashMap paramMap            = (HashMap) JPO.unpackArgs(args);
        String sOID                 = (String) paramMap.get("objectId");
        String sLanguage            = (String)paramMap.get("languageStr");
        MapList mlProjects;
        MapList mlProjectEntries    = new MapList();
        MapList mlTasks             = new MapList();
        MapList mlDeliverables      = new MapList();
        MapList mlIssues            = new MapList();
        MapList mlAssessments       = new MapList();
        String sOIDParam            = "";
             
        StringBuilder sbProjects                = new StringBuilder();  
        StringBuilder sbTasksWeek               = new StringBuilder();  
        StringBuilder sbTasksMonth              = new StringBuilder();  
        StringBuilder sbTasksSoon               = new StringBuilder();  
        StringBuilder sbTasksOverdue            = new StringBuilder();  
        StringBuilder sbTasksCategories         = new StringBuilder();  
        StringBuilder sbTasksSeries             = new StringBuilder();  
        StringBuilder sbDeliverablesWeek        = new StringBuilder();  
        StringBuilder sbDeliverablesMonth       = new StringBuilder();  
        StringBuilder sbDeliverablesSoon        = new StringBuilder();  
        StringBuilder sbDeliverablesOverdue     = new StringBuilder();  
        StringBuilder sbDeliverablesCategories  = new StringBuilder();  
        StringBuilder sbDeliverablesSeries      = new StringBuilder(); 
        StringBuilder sbIssuesWeek              = new StringBuilder();  
        StringBuilder sbIssuesMonth             = new StringBuilder();  
        StringBuilder sbIssuesSoon              = new StringBuilder();  
        StringBuilder sbIssuesOverdue           = new StringBuilder();  
        StringBuilder sbIssuesCategories        = new StringBuilder();  
        StringBuilder sbIssuesSeries            = new StringBuilder();          
        StringBuilder sbCategoriesAssessmentX   = new StringBuilder();          
        StringBuilder sbCategoriesAssessmentY   = new StringBuilder();          
        StringBuilder sbDataAssessmentR         = new StringBuilder();          
        StringBuilder sbDataAssessmentG         = new StringBuilder();          
        StringBuilder sbDataAssessmentY         = new StringBuilder();          
        StringBuilder sbDataAssessmentN         = new StringBuilder();          
        StringBuilder sbCategoriesEffort        = new StringBuilder();          
        StringBuilder sbDataEffortPlan          = new StringBuilder();          
        StringBuilder sbDataEffortActual        = new StringBuilder();          
        StringBuilder sbDataEffortProgress      = new StringBuilder();          
        
        if(sOID.equals("")) {        
            mlProjects = (MapList)JPO.invoke(context, "emxProjectSpace", initargs, "getActiveProjects", JPO.packArgs(params), MapList.class);		
        } else {
            params.put("objectId", sOID);
            sOIDParam = "&portalMode=true&objectId=" + sOID;
            mlProjects = (MapList)JPO.invoke(context, "emxProgramUI", initargs, "getProjectsOfProgram", JPO.packArgs(params), MapList.class);			
        }

        
        // Panel Header
        String  sSuffix = ProgramCentralConstants.EMPTY_STRING;
        if(ProgramCentralConstants.EMPTY_STRING.equals(sOID)) {
        	sSuffix = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.ActiveProjects", sLanguage);
        } else {
        	sSuffix = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.RelatedProjects", sLanguage);
        }
        sbProjects.append(" <span style='font-weight:bold;color:#7f7f7f;'>").append(mlProjects.size()).append("</span> ");
        sbProjects.append(sSuffix);
        
        // Panel Chart Headers
        String sHeaderTasks         = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.PendingTasks", sLanguage); 
        String sHeaderDeliverables  = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.PendingDeliverables", sLanguage); 
        String sHeaderIssues        = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.PendingIssues", sLanguage); 
        String sHeaderAssessments   = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxFramework.Command.Assessment", sLanguage); 
        String sHeaderEfforts       = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Effort", sLanguage); 
        String sLabelThisWeek       = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.ThisWeek", sLanguage); 
        String sLabelThisMonth      = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.ThisMonth", sLanguage); 
        String sLabelSoon           = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.Soon", sLanguage); 
        String sLabelOverdue        = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.Overdue", sLanguage); 
        String sLabelPlanned        = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Planned", sLanguage); 
        String sLabelActual         = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Actual", sLanguage); 
        String sLabelProgress       = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.WeeklyTimesheet.Progress", sLanguage); 
        
        sbCategoriesAssessmentX.append("\"").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.People.ProjectAssignmentFilterSummary", sLanguage)).append("\",");
        sbCategoriesAssessmentX.append("\"").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Resource", sLanguage)).append("\",");
        sbCategoriesAssessmentX.append("\"").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Schedule", sLanguage)).append("\",");
        sbCategoriesAssessmentX.append("\"").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Finance", sLanguage)).append("\",");
        sbCategoriesAssessmentX.append("\"").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Risk", sLanguage)).append("\"");
        
	if (mlProjects.size() > 0) {
	
            StringList busSelects = new StringList();
            StringList relSelects = new StringList();
            busSelects.add(DomainObject.SELECT_ID);
            busSelects.add(DomainObject.SELECT_TYPE);
            busSelects.add(DomainObject.SELECT_NAME);
            busSelects.add(DomainObject.SELECT_CURRENT);
            busSelects.add(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
            busSelects.add(Task.SELECT_TASK_ESTIMATED_DURATION);
            busSelects.add(ProgramCentralConstants.SELECT_ATTRIBUTE_TOTAL_EFFORT);
            busSelects.add(SELECT_ATTRIBUTE_PRIORITY);
            busSelects.add("type.kindof[DOCUMENTS]");
            busSelects.add(SELECT_RELATIONSHIP_TASK_DELIVERABLE);            
            busSelects.add(SELECT_TASK_DELIVERABLE_CURRENT);		
            busSelects.add(SELECT_RELATIONSHIP_ASSIGNED_TASKS);
            busSelects.add(SELECT_ASSIGNED_TASKS_PERCENT_ALLOCATION);
            busSelects.add(SELECT_FROM_SUBTASK);
            busSelects.add(DomainObject.SELECT_PERCENTCOMPLETE);	
            relSelects.add(SELECT_FROM_ATTRIBUTE_TASK_EXTIMATED_FINISH_DATE);		
            relSelects.add("from.current");	
		
            StringList busSelectsIssues = new StringList();
            busSelectsIssues.add(SELECT_ESTIMATED_END_DATE);
            busSelectsIssues.add(DomainObject.SELECT_CURRENT);
            busSelectsIssues.add(DomainObject.SELECT_ID);
            
            StringList busSelectsAssessments = new StringList();
            busSelectsAssessments.add(DomainObject.SELECT_ORIGINATED);            
            busSelectsAssessments.add(DomainObject.SELECT_MODIFIED);            
            busSelectsAssessments.add(DomainObject.SELECT_ORIGINATOR);            
            busSelectsAssessments.add(SELECT_ATTRIBUTE_ASSESSMENT_STATUS);            
            busSelectsAssessments.add(SELECT_ATTRIBUTE_ASSESSMENT_COMMENTS);            
            busSelectsAssessments.add(SELECT_ATTRIBUTE_RESOURCE_STATUS);            
            busSelectsAssessments.add(SELECT_ATTRIBUTE_RESOURCE_COMMENTS);            
            busSelectsAssessments.add(SELECT_ATTRIBUTE_SCHEDULE_STATUS);            
            busSelectsAssessments.add(SELECT_ATTRIBUTE_SCHEDULE_COMMENTS);            
            busSelectsAssessments.add(SELECT_ATTRIBUTE_FINANCE_STATUS);            
            busSelectsAssessments.add(SELECT_ATTRIBUTE_FINANCE_COMMENTS);            
            busSelectsAssessments.add(SELECT_ATTRIBUTE_RISK_STATUS);            
            busSelectsAssessments.add(SELECT_ATTRIBUTE_RISK_COMMENTS);            
	

	        StringList selectList = new StringList(4);
	        selectList.addElement(DomainObject.SELECT_ID);
	        selectList.addElement(DomainObject.SELECT_NAME);
	        selectList.addElement(DomainObject.SELECT_PERCENTCOMPLETE);
	        selectList.addElement(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
	    
	        // sort the projects by the ones that are due sooner
            mlProjects.sort(SELECT_ATTRIBUTE_TASK_ESTIMATED_END_DATE, "ascending", "date");
    	    
            // get the number of charts we want to display and only display that number of charts
            String strNumberOfTasks =  EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ProjectSummary.ChartsToDisplay");
            int numberOfProjects = Integer.parseInt(strNumberOfTasks);
            if ( mlProjects.size() < numberOfProjects ) {
            	numberOfProjects =  mlProjects.size();
            }
            
            String []relIdArr = new String[numberOfProjects];
    	    for (int i=0; i<numberOfProjects;i++) { 
			    Map passedMap = (Map) mlProjects.get(i);
			    relIdArr[i] = (String) passedMap.get(DomainObject.SELECT_ID);
    	    }
	    
	        MapList objectMapList = DomainObject.getInfo(context, relIdArr, selectList);
            int i = -1;
	        Iterator mapItr = objectMapList.iterator();
	        Map objectMap = null;
	        while(mapItr.hasNext()) 
	        {
	        	i++;
	        	objectMap = (Map)mapItr.next();
		        String sOIDProject = (String)objectMap.get(DomainObject.SELECT_ID);
		        String sProjectName = (String)objectMap.get(DomainObject.SELECT_NAME);
		        String sPercentComplete = (String)objectMap.get(DomainObject.SELECT_PERCENTCOMPLETE);
		        String sProjectsEstimeatedFinishDate = (String)objectMap.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);

                Map mProjectEntry           = new HashMap();
                DomainObject doProject      = new DomainObject(sOIDProject);
                BigDecimal bdEffortPlanned  = new BigDecimal(0.0);
                BigDecimal bdEffortActual   = new BigDecimal(0.0);
                BigDecimal bdTotalProgress  = new BigDecimal(0.0);
                
                
                mProjectEntry.put(DomainObject.SELECT_ID, sOIDProject   );
                mProjectEntry.put(DomainObject.SELECT_NAME, sProjectName  );
                mProjectEntry.put("PercentComplete" , sPercentComplete  );
                mProjectEntry.put("openTasks"       , "0"           );
                mProjectEntry.put("openDeliverables", "0"           );
                mProjectEntry.put("openIssues"      , "0"           );
                mProjectEntry.put("color"           , sColors[i%sColors.length] );
                mProjectEntry.put("TaskEstimatedFinishDate", sProjectsEstimeatedFinishDate );
			
        		String relPattern = DomainConstants.RELATIONSHIP_SUBTASK + "," + 
        							DomainConstants.RELATIONSHIP_TASK_DELIVERABLE + "," + 
        							RELATIONSHIP_HAS_EFFORTS;
        		String typePattern = TYPE_DOCUMENTS + "," + 
        							 DomainConstants.TYPE_TASK_MANAGEMENT + "," +
        							 DomainConstants.TYPE_PROJECT_SPACE + "," + //To consider subproject tasks.
        							 TYPE_EFFORT;

        		MapList mlTemp = doProject.getRelatedObjects(context, relPattern, typePattern, busSelects, relSelects, false, true, (short) 0, "", "", 10000);
			
                
                for (int j = 0; j < mlTemp.size(); j++) {
                
                    Map mTemp               = (Map)mlTemp.get(j);
                    String sIsDeliverable   = (String)mTemp.get(SELECT_RELATIONSHIP_TASK_DELIVERABLE);
                    String sType            = (String)mTemp.get(DomainObject.SELECT_TYPE);
                                        
                    if (sIsDeliverable.equalsIgnoreCase("TRUE")) {					
                        mTemp.put("project", sProjectName);
                        if(!mlDeliverables.contains(mTemp)) { mlDeliverables.add(mTemp); }						
                    } else if((mxType.isOfParentType(context, sType, DomainConstants.TYPE_EFFORT)))  {
                        bdEffortActual = bdEffortActual.add(new BigDecimal((String)mTemp.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TOTAL_EFFORT)));
                    } else {					

                        String sHasAssignees    = (String)mTemp.get(SELECT_RELATIONSHIP_ASSIGNED_TASKS);
                        String sDuration        = (String)mTemp.get(Task.SELECT_TASK_ESTIMATED_DURATION);
                        BigDecimal bdDuration   = new BigDecimal(sDuration);
                        
                        if (mxType.isOfParentType(context, sType, DomainConstants.TYPE_TASK)) {
                            String sIsLeaf = (String)mTemp.get(SELECT_FROM_SUBTASK);
                            if (sIsLeaf.equalsIgnoreCase("FALSE")) {
                                mTemp.put("project", sProjectName);
                                mlTasks.add(mTemp);
                            }
                        }

                        if(sHasAssignees.equalsIgnoreCase("TRUE")) {
                            BigDecimal bdAssignment = new BigDecimal(0.0);
                            if (mTemp.get(SELECT_ASSIGNED_TASKS_PERCENT_ALLOCATION) instanceof StringList) {
                                StringList slPercentages = (StringList)mTemp.get(SELECT_ASSIGNED_TASKS_PERCENT_ALLOCATION);
                                for(int k = 0; k < slPercentages.size(); k++) {
                                    String sPercentage = (String)slPercentages.get(k);
                                    bdAssignment = bdAssignment.add(new BigDecimal(sPercentage));
                                }
                            } else {
                                String sPercentage = (String)mTemp.get(SELECT_ASSIGNED_TASKS_PERCENT_ALLOCATION);
                                bdAssignment = bdAssignment.add(new BigDecimal(sPercentage));
                            }
                            String sTaskPercentComplete    = (String)mTemp.get(DomainObject.SELECT_PERCENTCOMPLETE);
                            BigDecimal bdTaskPercentCompleten   = new BigDecimal(sTaskPercentComplete);
                            BigDecimal bdEffort = bdDuration.multiply(bdAssignment);
                            bdEffort = bdEffort.divide(new BigDecimal(100.0));
                            bdEffortPlanned = bdEffortPlanned.add(bdEffort);
                            BigDecimal bdTaskProgress = bdTaskPercentCompleten.multiply(bdEffort);
                            bdTotalProgress = bdTotalProgress.add(bdTaskProgress);
                        }
                    }   
                }
                
                bdEffortActual = bdEffortActual.divide(new BigDecimal(8.0));
                mProjectEntry.put("EffortPlan", bdEffortPlanned.toString());
                mProjectEntry.put("EffortActual", bdEffortActual.toString());
                bdTotalProgress = bdTotalProgress.divide(new BigDecimal(100.0));
                mProjectEntry.put("EffortProgress", bdTotalProgress.toString());
                
                mlProjectEntries.add(mProjectEntry);                
                
                MapList mlIssuesConnected = doProject.getRelatedObjects(context, RELATIONSHIP_ISSUE, TYPE_ISSUE, busSelectsIssues, relSelects, true, false, (short) 1, "current != 'Closed'", "", 10000);
			
                for (int j = 0; j < mlIssuesConnected.size(); j++) {
                    Map mIssue = (Map)mlIssuesConnected.get(j);
                    mIssue.put("project", sProjectName);
                    if(!mlIssues.contains(mIssue)) {
                        mlIssues.add(mIssue);
                    }
                }
                
                MapList mlAssessmentsConnected = doProject.getRelatedObjects(context, RELATIONSHIP_PROJECT_ASSESSMENT, TYPE_ASSESSMENT, busSelectsAssessments, relSelects, false, true, (short) 1, "", "", 10000);
                if(mlAssessmentsConnected.size() > 0) {
                    mlAssessmentsConnected.sort("originated", "descending", "date");
                    Map mAssessment = (Map)mlAssessmentsConnected.get(0);
                    mAssessment.put("project", sProjectName);
                    mAssessment.put("projectid", sOIDProject);
                    mlAssessments.add(mAssessment);
                }                          
            }
	}
        
	int iCountTasksThisWeek 		= 0;
	int iCountTasksThisMonth 		= 0;
	int iCountTasksSoon 			= 0;
	int iCountTasksOverdue			= 0;	
	int iCountDeliverablesThisWeek          = 0;
	int iCountDeliverablesThisMonth         = 0;
	int iCountDeliverablesSoon 		= 0;
	int iCountDeliverablesOverdue           = 0;		
	int iCountIssuesThisWeek 		= 0;
	int iCountIssuesThisMonth 		= 0;
	int iCountIssuesSoon 			= 0;
	int iCountIssuesOverdue 		= 0;	
    int iCountEfforts               = 0;
    
	Calendar cCurrent 	= Calendar.getInstance();
	int iYearCurrent 	= cCurrent.get(Calendar.YEAR);
	int iMonthCurrent 	= cCurrent.get(Calendar.MONTH);
	int iWeekCurrent 	= cCurrent.get(Calendar.WEEK_OF_YEAR);
	long lCurrent           = cCurrent.getTimeInMillis();
	long lDiff              = 2592000000L;	
	
		StringList tempTaskIdList = new StringList();
        for (int i = 0; i < mlTasks.size(); i++) {
                
            Map mTask       = (Map)mlTasks.get(i);
            String sCurrent = (String)mTask.get(DomainObject.SELECT_CURRENT);

            boolean isTaskRepeated = false;
            String taskId = (String) mTask.get(DomainObject.SELECT_ID);
            if(!tempTaskIdList.contains(taskId)) tempTaskIdList.add(taskId); 
    		else isTaskRepeated = true;

            if (!sCurrent.equals("Complete")) {

            	if(!isTaskRepeated){ //When Project has sub-project, Tasks may be repetitive in the "mlTasks" because of both the project. 
                String sTargetDate = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);				
                Calendar cTarget = Calendar.getInstance();
                cTarget.setTime(sdf.parse(sTargetDate));

                int iYearTarget     = cTarget.get(Calendar.YEAR);
                int iMonthTarget    = cTarget.get(Calendar.MONTH);
                int iWeekTarget     = cTarget.get(Calendar.WEEK_OF_YEAR);
                long lTarget        = cTarget.getTimeInMillis();

                if (iYearCurrent == iYearTarget) {
                    if (iWeekCurrent == iWeekTarget) {
                        iCountTasksThisWeek++;
                    }
                    if (iMonthCurrent == iMonthTarget) {
                        iCountTasksThisMonth++;
                    }
                }				
                if ((lTarget - lCurrent) < lDiff) {
                    if ((lTarget - lCurrent) > 0) {
                        iCountTasksSoon++;
                    }	                    
                }	
                if (cCurrent.after(cTarget)) {
                    iCountTasksOverdue++;
                }
            	}

                String sProject = (String)mTask.get("project");			
                for (int j = 0; j < mlProjectEntries.size(); j++) {
        
                    Map mProjectEntry = (Map)mlProjectEntries.get(j);
                    String sProjectName = (String)mProjectEntry.get(DomainObject.SELECT_NAME);
                    if(sProjectName.equals(sProject)) {
                        String sOpenTasks = (String)mProjectEntry.get("openTasks");
                        int iOpenTasks = Integer.parseInt(sOpenTasks);
                        iOpenTasks++;
                        mProjectEntry.put("openTasks", String.valueOf(iOpenTasks));
                        break;
                    }
                }
            }                
        }


        for (int i = 0; i < mlDeliverables.size(); i++) {
               
            Map mDeliverable = (Map)mlDeliverables.get(i);
            String sCurrent = (String)mDeliverable.get("from.current");

            if (!sCurrent.equals("Complete")) {

                String sTargetDate = (String)mDeliverable.get(SELECT_FROM_ATTRIBUTE_TASK_EXTIMATED_FINISH_DATE);   				
                Calendar cTarget = Calendar.getInstance();
                cTarget.setTime(sdf.parse(sTargetDate));
                
                long lTarget        = cTarget.getTimeInMillis();
                int iYearTarget     = cTarget.get(Calendar.YEAR);
                int iMonthTarget    = cTarget.get(Calendar.MONTH);
                int iWeekTarget     = cTarget.get(Calendar.WEEK_OF_YEAR);

                if (iYearCurrent == iYearTarget) {
                    if (iWeekCurrent == iWeekTarget)    { iCountDeliverablesThisWeek++;     }
                    if (iMonthCurrent == iMonthTarget)  { iCountDeliverablesThisMonth++;    }
                }				
                if ((lTarget - lCurrent) < lDiff) {
                    if ((lTarget - lCurrent) > 0) { iCountDeliverablesSoon++; }	                    
                }	
                if (cCurrent.after(cTarget)) {  iCountDeliverablesOverdue++; }

                String sProject = (String)mDeliverable.get("project");			
                
                for (int j = 0; j < mlProjectEntries.size(); j++) {
                    Map mProjectEntry = (Map)mlProjectEntries.get(j);
                    String sProjectName = (String)mProjectEntry.get(DomainObject.SELECT_NAME);
                    if(sProjectName.equals(sProject)) {
                        String sOpenDeliverables = (String)mProjectEntry.get("openDeliverables");
                        int iOpenDeliverables = Integer.parseInt(sOpenDeliverables);
                        iOpenDeliverables++;
                        mProjectEntry.put("openDeliverables", String.valueOf(iOpenDeliverables));
                        break;
                    }
                }

            }
        }
	
        StringList issueList = new StringList(mlIssues.size());
        for (int i = 0; i < mlIssues.size(); i++) {
            
            Map mIssue = (Map)mlIssues.get(i);
            String sTargetDate = (String)mIssue.get(SELECT_ESTIMATED_END_DATE);
            String IsuueId = (String)mIssue.get(DomainConstants.SELECT_ID);

            if(sTargetDate != null && !"".equals(sTargetDate) && !issueList.contains(IsuueId)) {
            	issueList.add(IsuueId);

                Calendar cTarget = Calendar.getInstance();
                cTarget.setTime(sdf.parse(sTargetDate));
            
                long lTarget        = cTarget.getTimeInMillis();
                int iYearTarget     = cTarget.get(Calendar.YEAR);
                int iMonthTarget    = cTarget.get(Calendar.MONTH);
                int iWeekTarget     = cTarget.get(Calendar.WEEK_OF_YEAR);

                if (iYearCurrent == iYearTarget) {
                    if (iWeekCurrent == iWeekTarget) { iCountIssuesThisWeek++; }
                    if (iMonthCurrent == iMonthTarget) { iCountIssuesThisMonth++; }
                }				
		
                if ((lTarget - lCurrent) < lDiff) {
                    if ((lTarget - lCurrent) > 0) { iCountIssuesSoon++; }	                    
                }	
                if (cCurrent.after(cTarget)) { iCountIssuesOverdue++; }
            }

            String sProject = (String)mIssue.get("project");			
            for (int j = 0; j < mlProjectEntries.size(); j++) {
                Map mProjectEntry = (Map)mlProjectEntries.get(j);
                String sProjectName = (String)mProjectEntry.get(DomainObject.SELECT_NAME);
                if(sProjectName.equals(sProject)) {
                    String sOpenIssues = (String)mProjectEntry.get("openIssues");
                    int iOpenIssues = Integer.parseInt(sOpenIssues);
                    iOpenIssues++;
                    mProjectEntry.put("openIssues", String.valueOf(iOpenIssues));
                    break;
                }
            }
        }

        String sPrefixTasks         = "<a onclick='openURLInDetails(\"../common/emxIndentedTable.jsp?suiteKey=ProgramCentral&table=PMCAssignedWBSTaskSummary";
        String sSuffixTasks         = "&editLink=true&hideWeeklyEfforts=true&selection=multiple&freezePane=WBSTaskName,Status,Delivarable,NewWindow&program=emxProgramUI:";
        String sPrefixDeliverables  = " <a onclick='openURLInDetails(\"../common/emxIndentedTable.jsp?suiteKey=ProgramCentral&table=PMCPendingDeliverableSummary";
        String sSuffixDeliverables  = "&sortColumnName=EstEndDate&sortDirection=ascending&editLink=true&parentRelName=relationship_TaskDeliverable&selection=multiple&freezePane=Name&program=emxProgramUI:";        
        String sPrefixIssues        = " <a onclick='openURLInDetails(\"../common/emxIndentedTable.jsp?suiteKey=ProgramCentral&table=IssueList&freezePane=Name,Files,NewWindow&editLink=true";
        String sSuffixIssues        = "&selection=multiple&program=emxProgramUI:";                               
        
        sbTasksWeek.append("<b>").append(iCountTasksThisWeek).append("</b> ").append(sPrefixTasks).append(sOIDParam).append(sSuffixTasks).append("getPendingTasks&mode=This Week&header=emxProgramCentral.String.OpenTasksThisWeek\")'>").append(sLabelThisWeek).append("</a>");
        sbTasksMonth.append("<b>").append(iCountTasksThisMonth).append("</b> ").append(sPrefixTasks).append(sOIDParam).append(sSuffixTasks).append("getPendingTasks&mode=This Month&header=emxProgramCentral.String.OpenTasksThisMonth\")'>").append(sLabelThisMonth).append("</a>");
        sbTasksSoon.append("<b>").append(iCountTasksSoon).append("</b> ").append(sPrefixTasks).append(sOIDParam).append(sSuffixTasks).append("getPendingTasks&mode=Soon&header=emxProgramCentral.String.OpenTasksSoon\")'>").append(sLabelSoon).append("</a>");
        sbTasksOverdue.append("<b>").append(iCountTasksOverdue).append("</b> ").append(sPrefixTasks).append(sOIDParam).append(sSuffixTasks).append("getPendingTasks&mode=Overdue&header=emxProgramCentral.String.OpenTasksOverdue\")'>").append(sLabelOverdue).append("</a>");
        
        sbDeliverablesWeek.append("<b>").append(iCountDeliverablesThisWeek).append("</b> ").append(sPrefixDeliverables).append(sOIDParam).append(sSuffixDeliverables).append("getDeliverablesByTask&mode=This Week&header=emxProgramCentral.String.OpenDeliverablesThisWeek\")'>").append(sLabelThisWeek).append("</a>");
        sbDeliverablesMonth.append("<b>").append(iCountDeliverablesThisMonth).append("</b> ").append(sPrefixDeliverables).append(sOIDParam).append(sSuffixDeliverables).append("getDeliverablesByTask&mode=This Month&header=emxProgramCentral.String.OpenDeliverablesThisMonth\")'>").append(sLabelThisMonth).append("</a>");
        sbDeliverablesSoon.append("<b>").append(iCountDeliverablesSoon).append("</b> ").append(sPrefixDeliverables).append(sOIDParam).append(sSuffixDeliverables).append("getDeliverablesByTask&mode=Soon&header=emxProgramCentral.String.OpenDeliverablesSoon\")'>").append(sLabelSoon).append("</a>");
        sbDeliverablesOverdue.append("<b>").append(iCountDeliverablesOverdue).append("</b> ").append(sPrefixDeliverables).append(sOIDParam).append(sSuffixDeliverables).append("getDeliverablesByTask&mode=Overdue&header=emxProgramCentral.String.OpenDeliverablesOverdue\")'>").append(sLabelOverdue).append("</a>");        
        
        sbIssuesWeek.append("<b>").append(iCountIssuesThisWeek).append("</b> ").append(sPrefixIssues).append(sOIDParam).append(sSuffixIssues).append("getPendingIssues&mode=This Week&header=emxProgramCentral.String.OpenIssuesThisWeek\")'>").append(sLabelThisWeek).append("</a>");
        sbIssuesMonth.append("<b>").append(iCountIssuesThisMonth).append("</b> ").append(sPrefixIssues).append(sOIDParam).append(sSuffixIssues).append("getPendingIssues&mode=This Month&header=emxProgramCentral.String.OpenIssuesThisMonth\")'>").append(sLabelThisMonth).append("</a>");
        sbIssuesSoon.append("<b>").append(iCountIssuesSoon).append("</b> ").append(sPrefixIssues).append(sOIDParam).append(sSuffixIssues).append("getPendingIssues&mode=Soon&header=emxProgramCentral.String.OpenIssuesSoon\")'>").append(sLabelSoon).append("</a>");
        sbIssuesOverdue.append("<b>").append(iCountIssuesOverdue).append("</b> ").append(sPrefixIssues).append(sOIDParam).append(sSuffixIssues).append("getPendingIssues&mode=Overdue&header=emxProgramCentral.String.OpenIssuesOverdue\")'>").append(sLabelOverdue).append("</a>");
       
	int iCountTasks 	= 0;
	int iCountDeliverables  = 0;
	int iCountIssues 	= 0;

	mlProjectEntries.sort("TaskEstimatedFinishDate", "ascending", "date");
	for (int i = 0; i < mlProjectEntries.size(); i++) {
            Map mProject 	= (Map)mlProjectEntries.get(i);
            String sName 	= (String) mProject.get(DomainObject.SELECT_NAME);			
            String sID 		= (String) mProject.get(DomainObject.SELECT_ID);
            String sCount	= (String) mProject.get("openTasks");	
            String sColor	= (String) mProject.get("color");	
            if(!sCount.equals("0")) { 
                iCountTasks++; 		
                sbTasksCategories.append("'").append(sName).append("',");
                sbTasksSeries.append("{name:'").append(sName).append("', y:").append(sCount).append(", id:'").append(sID).append("', color:'#").append(sColor).append("'},");
            }
	}
	if(sbTasksCategories.length() > 0) { sbTasksCategories.setLength(sbTasksCategories.length() - 1); }
	if(sbTasksSeries.length() > 0) { sbTasksSeries.setLength(sbTasksSeries.length() - 1); }

	// mlProjectEntries.sort(DomainObject.SELECT_NAME, "ascending", "String");
	// mlProjectEntries.sort("openDeliverables", "decending", "integer");
    mlProjectEntries.sort("TaskEstimatedFinishDate", "ascending", "date");
	for (int i = 0; i < mlProjectEntries.size(); i++) {
		Map mProject 	= (Map)mlProjectEntries.get(i);
		String sName 	= (String)mProject.get(DomainObject.SELECT_NAME);			
		String sID 	= (String) mProject.get(DomainObject.SELECT_ID);
		String sCount	= (String) mProject.get("openDeliverables");	
		String sColor	= (String) mProject.get("color");	
		if(!sCount.equals("0")) { 
			iCountDeliverables++; 		
			sbDeliverablesCategories.append("'").append(sName).append("',");
			sbDeliverablesSeries.append("{name:'").append(sName).append("', y:").append(sCount).append(", id:'").append(sID).append("', color:'#").append(sColor).append("'},");
		}
	}
	if(sbDeliverablesCategories.length() > 0) { sbDeliverablesCategories.setLength(sbDeliverablesCategories.length() - 1); }
	if(sbDeliverablesSeries.length() > 0) { sbDeliverablesSeries.setLength(sbDeliverablesSeries.length() - 1); }
	
    mlProjectEntries.sort("TaskEstimatedFinishDate", "ascending", "date");
	// mlProjectEntries.sort(DomainObject.SELECT_NAME, "ascending", "String");
        
        
    sbDataEffortPlan.append("{ name: \"").append(sLabelPlanned).append("\", color:'#254256', data: [");
    sbDataEffortActual.append("{ name: \"").append(sLabelActual).append("\", color:'#ff7f00', data: [");
    sbDataEffortProgress.append("{ name: \"").append(sLabelProgress).append("\", color:'#88b1cc', data: [");
  
    for (int i = 0; i < mlProjectEntries.size(); i++) {
            
        Map mProject            = (Map)mlProjectEntries.get(i);
        String sID              = (String) mProject.get(DomainObject.SELECT_ID);
        String sPercentComplete = (String)mProject.get("PercentComplete");
        String sEffortPlan      = (String)mProject.get("EffortPlan");            
        BigDecimal bdProgress   = new BigDecimal(sPercentComplete).multiply(new BigDecimal(sEffortPlan));
        bdProgress              = bdProgress.divide(new BigDecimal(100.0));
            
        // don't show the project if there is no planned effort
        BigDecimal bdZero  = new BigDecimal(0.0);
        BigDecimal bdEffortPlanned  = new BigDecimal(sEffortPlan);
        if (bdEffortPlanned.compareTo(bdZero) == 1) {
            sbCategoriesEffort.append("'").append((String)mProject.get(DomainObject.SELECT_NAME)).append("',");
            sbDataEffortPlan.append("{ id:'").append(sID).append("', y:").append((String)mProject.get("EffortPlan")).append("},");
            sbDataEffortActual.append("{ id:'").append(sID).append("', y:").append((String)mProject.get("EffortActual")).append("},");
            sbDataEffortProgress.append("{ id:'").append(sID).append("', y:").append((String)mProject.get("EffortProgress")).append("},");
            iCountEfforts++;
        }
    }
        
      
    sbDataEffortPlan.append("] }");
    sbDataEffortActual.append("] }");
    sbDataEffortProgress.append("] }");        
        
    mlProjectEntries.sort("TaskEstimatedFinishDate", "ascending", "date");
	// mlProjectEntries.sort("openIssues", "decending", "integer");
	for (int i = 0; i < mlProjectEntries.size(); i++) {
            Map mProject 	= (Map)mlProjectEntries.get(i);
            String sName 	= (String)mProject.get(DomainObject.SELECT_NAME);			
            String sID          = (String) mProject.get(DomainObject.SELECT_ID);
            String sCount	= (String) mProject.get("openIssues");	
            String sColor	= (String) mProject.get("color");	
            if(!sCount.equals("0")) { 
                iCountIssues++; 		
                sbIssuesCategories.append("'").append(sName).append("',");
                sbIssuesSeries.append("{name:'").append(sName).append("', y:").append(sCount).append(", id:'").append(sID).append("', color:'#").append(sColor).append("'},");
            }
	}
        
        // In order for the Projects to be displayed in the right order we need to go in the reverse order
		int yCoordinate = 0;
		for(int i = mlAssessments.size()-1 ; i >= 0 ; i--) {
            Map mAssessment             = (Map)mlAssessments.get(i);                        
            String sProject             = (String)mAssessment.get("project");
            String sProjectId           = (String)mAssessment.get("projectid");
            String sModified            = (String)mAssessment.get("modified");
            String sOriginator          = (String)mAssessment.get(DomainObject.SELECT_ORIGINATOR);
            String sStatusAssessment    = (String)mAssessment.get(SELECT_ATTRIBUTE_ASSESSMENT_STATUS);
            String sCommentAssessment   = (String)mAssessment.get(SELECT_ATTRIBUTE_ASSESSMENT_COMMENTS);
            String sStatusResource      = (String)mAssessment.get(SELECT_ATTRIBUTE_RESOURCE_STATUS);
            String sCommentResource     = (String)mAssessment.get(SELECT_ATTRIBUTE_RESOURCE_COMMENTS);
            String sStatusSchedule      = (String)mAssessment.get(SELECT_ATTRIBUTE_SCHEDULE_STATUS);
            String sCommentSchedule     = (String)mAssessment.get(SELECT_ATTRIBUTE_SCHEDULE_COMMENTS);
            String sStatusFinance       = (String)mAssessment.get(SELECT_ATTRIBUTE_FINANCE_STATUS);
            String sCommentFinance      = (String)mAssessment.get(SELECT_ATTRIBUTE_FINANCE_COMMENTS);
            String sStatusRisk          = (String)mAssessment.get(SELECT_ATTRIBUTE_RISK_STATUS);
            String sCommentRisk         = (String)mAssessment.get(SELECT_ATTRIBUTE_RISK_COMMENTS);
            
            if("".equals(sCommentAssessment))  { sCommentAssessment    = "-"; }
            if("".equals(sCommentResource) )   { sCommentResource      = "-"; }
            if("".equals(sCommentSchedule))    { sCommentSchedule      = "-"; }
            if("".equals(sCommentFinance))     { sCommentFinance       = "-"; }
            if("".equals(sCommentRisk))        { sCommentRisk          = "-"; }
            if(sModified.contains(" "))        { sModified = "<br/>" + sOriginator + " (" + sModified.substring(0, sModified.indexOf(" ")) + ")" ; }
                        
            appendAssessmentData(sProjectId, yCoordinate, sModified, "0", sbDataAssessmentR, sbDataAssessmentG, sbDataAssessmentY, sbDataAssessmentN, sStatusAssessment   , sCommentAssessment);
            appendAssessmentData(sProjectId, yCoordinate, sModified, "1", sbDataAssessmentR, sbDataAssessmentG, sbDataAssessmentY, sbDataAssessmentN, sStatusResource     , sCommentResource  );
            appendAssessmentData(sProjectId, yCoordinate, sModified, "2", sbDataAssessmentR, sbDataAssessmentG, sbDataAssessmentY, sbDataAssessmentN, sStatusSchedule     , sCommentSchedule  );
            appendAssessmentData(sProjectId, yCoordinate, sModified, "3", sbDataAssessmentR, sbDataAssessmentG, sbDataAssessmentY, sbDataAssessmentN, sStatusFinance      , sCommentFinance   );
            appendAssessmentData(sProjectId, yCoordinate, sModified, "4", sbDataAssessmentR, sbDataAssessmentG, sbDataAssessmentY, sbDataAssessmentN, sStatusRisk         , sCommentRisk      );
            
            yCoordinate++;
            
            sbCategoriesAssessmentY.append("'").append(sProject).append("',");
        }
        
	if(sbIssuesCategories.length() > 0) { sbIssuesCategories.setLength(sbIssuesCategories.length() - 1); }
	if(sbIssuesSeries.length() > 0) { sbIssuesSeries.setLength(sbIssuesSeries.length() - 1); }
	if(sbCategoriesAssessmentY.length() > 0) { sbCategoriesAssessmentY.setLength(sbCategoriesAssessmentY.length() - 1); }
	if(sbDataAssessmentR.length() > 0) { sbDataAssessmentR.setLength(sbDataAssessmentR.length() - 1); }
	if(sbDataAssessmentG.length() > 0) { sbDataAssessmentG.setLength(sbDataAssessmentG.length() - 1); }
	if(sbDataAssessmentY.length() > 0) { sbDataAssessmentY.setLength(sbDataAssessmentY.length() - 1); }
	if(sbDataAssessmentN.length() > 0) { sbDataAssessmentN.setLength(sbDataAssessmentN.length() - 1); }

	
	int iHeightTasks 	= 35 + (iCountTasks * 20);
	int iHeightDeliverables = 35 + (iCountDeliverables * 20);
	int iHeightIssues	= 35 + (iCountIssues * 20);
	int iHeightAssessments	= 38 + (mlAssessments.size() * 28);    
	int iHeightEffort	= 70 + (iCountEfforts * 40);   
        
      if(iHeightAssessments < 100) { iHeightAssessments = 100; }
        
        
        aResults[0] = sbProjects.toString();
        aResults[10] = sHeaderTasks;
        aResults[11] = String.valueOf(iHeightTasks);
        aResults[12] = sbTasksWeek.toString();
        aResults[13] = sbTasksMonth.toString();
        aResults[14] = sbTasksSoon.toString();
        aResults[15] = sbTasksOverdue.toString();
        aResults[16] = sbTasksCategories.toString();
        aResults[17] = sbTasksSeries.toString();
        aResults[20] = sHeaderDeliverables;
        aResults[21] = String.valueOf(iHeightDeliverables);
        aResults[22] = sbDeliverablesWeek.toString();
        aResults[23] = sbDeliverablesMonth.toString();
        aResults[24] = sbDeliverablesSoon.toString();
        aResults[25] = sbDeliverablesOverdue.toString();
        aResults[26] = sbDeliverablesCategories.toString();
        aResults[27] = sbDeliverablesSeries.toString();        
        aResults[30] = sHeaderIssues;
        aResults[31] = String.valueOf(iHeightIssues);
        aResults[32] = sbIssuesWeek.toString();
        aResults[33] = sbIssuesMonth.toString();
        aResults[34] = sbIssuesSoon.toString();
        aResults[35] = sbIssuesOverdue.toString();
        aResults[36] = sbIssuesCategories.toString();
        aResults[37] = sbIssuesSeries.toString();                  
        aResults[38] = sHeaderAssessments;                  
        aResults[39] = sbCategoriesAssessmentX.toString();                  
        aResults[40] = sbCategoriesAssessmentY.toString();                  
        aResults[41] = String.valueOf(mlAssessments.size() - 0.5);
        aResults[42] = sbDataAssessmentR.toString();                  
        aResults[43] = sbDataAssessmentY.toString();                  
        aResults[44] = sbDataAssessmentG.toString();                  
        aResults[45] = sbDataAssessmentN.toString();
        aResults[46] = String.valueOf(iHeightAssessments);
        aResults[47] = sHeaderEfforts;
        aResults[1] = sbCategoriesEffort.toString();
        aResults[2] = sbDataEffortPlan.toString();
        aResults[3] = sbDataEffortActual.toString();
        aResults[4] = sbDataEffortProgress.toString();
        aResults[5] = String.valueOf(iHeightEffort);
        
        
        return aResults;
        
    }

    
    public void appendAssessmentData(String sProjectId, int i, String sModified, String sCategory, StringBuilder sbDataAssessmentR, StringBuilder sbDataAssessmentG, StringBuilder sbDataAssessmentY, StringBuilder sbDataAssessmentN, String sStatus, String sComment) {
        
        
        StringBuilder sbToAppend = new StringBuilder();        
        sComment = sComment.replaceAll("\n", "<br/>");
            
        sbToAppend.append("{ id:'").append(sProjectId).append("',");
        sbToAppend.append(" name:'").append(sComment).append(sModified).append("',");
        sbToAppend.append(" x:").append(sCategory).append(", y:").append(i).append("},");
        
        if(sStatus.equals("Red"))         { sbDataAssessmentR.append(sbToAppend.toString()); }
        else if(sStatus.equals("Green"))  { sbDataAssessmentG.append(sbToAppend.toString()); }
        else if(sStatus.equals("Yellow")) { sbDataAssessmentY.append(sbToAppend.toString()); }
        else                              { sbDataAssessmentN.append(sbToAppend.toString()); }                    
        
    }
    public Vector columnProjectFolders(Context context, String[] args) throws Exception {


        Vector vResult      = new Vector();
        Map paramMap        = (Map) JPO.unpackArgs(args);
        MapList mlObjects   = (MapList) paramMap.get("objectList");        
        
        StringList busSelects = new StringList();
        busSelects.add(DomainObject.SELECT_TYPE);
        busSelects.add("from[Data Vaults]");
        
        for (int i = 0; i < mlObjects.size(); i++) {
            
            StringBuilder sbResult  = new StringBuilder();
            Map mObject             = (Map) mlObjects.get(i);
            String sOID             = (String)mObject.get(DomainObject.SELECT_ID);
            DomainObject dObject    = new DomainObject(sOID);
            Map mData               = dObject.getInfo(context, busSelects);

            String sIcon        = "../gnv/images/iconSmallFolderEmpty.gif";
            String sTitle       = "There are no folders";
            String sHasFolders  = (String)mData.get("from[Data Vaults]");
            if(sHasFolders.equalsIgnoreCase("TRUE")) { sIcon = "../common/images/iconSmallFolder.gif"; sTitle = "Open Folders view"; }

            sbResult.append("<a href='");
            sbResult.append("../common/emxTree.jsp?objectId=").append(sOID).append("&amp;DefaultCategory=PMCFolder'");
            sbResult.append(" target='content'><img src='").append(sIcon).append("' border='0' TITLE='").append(sTitle).append("'/></a>");    
            vResult.add(sbResult.toString());   
            
        }        

        return vResult;
    }    
         
    public Vector columnProgressBar(Context context, String[] args) throws Exception {

    	return getTasksProgressBar(context, args);

    }
    
    
    public Vector columnCurrentPhaseGate(Context context, String[] args) throws Exception {

        HashMap programMap  = (HashMap) JPO.unpackArgs(args);
        MapList mlObjects   = (MapList) programMap.get("objectList");
        Vector resultVector = new Vector(mlObjects.size());

        for (int i = 0; i < mlObjects.size(); i++) {
            Map mObject = (Map) mlObjects.get(i);
            String sOID = (String) mObject.get(DomainObject.SELECT_ID);
            resultVector.addElement(retrieveCurrentPhaseGate(context, sOID));
        }

        return resultVector;
    }
    private String retrieveCurrentPhaseGate(Context context, String sOID) throws Exception {

        String sResult          = "";
        DomainObject dObject    = new DomainObject(sOID);

        // Get active Gate
        StringList busSelects = new StringList(5);
        StringList relSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_NAME);
        busSelects.add(DomainConstants.SELECT_CURRENT);
        busSelects.add(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
        relSelects.add(ProgramCentralConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER);

        MapList mlItems = dObject.getRelatedObjects(context, "Subtask", "Phase,Gate", busSelects, relSelects, false, true, (short) 1, "current != Complete", "", 0);

        if (mlItems.size() > 0) {

            mlItems.sort(ProgramCentralConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER, "ascending", "Integer");
            
            Map mItem           = (Map) mlItems.get(0);
            String sOIDPhase    = (String) mItem.get(DomainObject.SELECT_ID);
            String sName        = (String) mItem.get(DomainObject.SELECT_NAME);
            String sTarget      = (String) mItem.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
            
            if (!sTarget.equals("") && !sTarget.equals("")) {
                
                Date dTarget    = sdf.parse(sTarget);
                Date dCurrent   = new Date();

                int iOTD = (int) ((dTarget.getTime() - dCurrent.getTime()) / (1000 * 60 * 60 * 24));
                if (iOTD < 0) {
                    sResult = "<img src='../common/images/iconStatusRed.gif' />";
                } else if (iOTD < 6) {
                    sResult = "<img src='../common/images/iconStatusYellow.gif' />";
                }

            }

            sResult = "<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=" + sOIDPhase + "', '950', '650', 'false', 'popup', '')\">" + sName + "</a> " + sResult;

        }

        return sResult;
    }
    public Vector columnPercentComplete(Context context, String[] args) throws Exception {

        
        Vector vResult      = new Vector();

        Map programMap      = (Map) JPO.unpackArgs(args);     
        MapList mlObjects   = (MapList) programMap.get("objectList");
        HashMap paramList   = (HashMap) programMap.get("paramList"); 
        HashMap columnMap   = (HashMap) programMap.get("columnMap");
        HashMap settings    = (HashMap) columnMap.get("settings");      
        String sLanguage    = (String) paramList.get("languageStr"); 
        String sClickToSet  = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.ClickToSetProgressTo", sLanguage);
        String sAttribute   = "Percent Complete";   
        String sTarget      = "parent.frames[\"listHidden\"].document.location.href";
        String[] sStates    = new String[0];
        String[] sColors    = new String[0];
        Boolean bIsIndented = false;
        
        if(settings.containsKey("Attribute")) {
            sAttribute = (String) settings.get("Attribute"); 
        }
        if(settings.containsKey("States")) {
            if(settings.containsKey("Colors")) {
                String sParameterStates = (String) settings.get("States");
                String sParameterColors = (String) settings.get("Colors");
                sStates = sParameterStates.split(","); 
                sColors = sParameterColors.split(","); 
            }
        }
        if(paramList.containsKey("isIndentedView")) { 
            bIsIndented = true;
            sTarget = "self.frames[\"listHidden\"].document.location.href";
        }        
        


        if (mlObjects.size() > 0) {

            for (int i = 0; i < mlObjects.size(); i++) {

                Map mObject             = (Map) mlObjects.get(i);
                String sOID             = (String) mObject.get(DomainObject.SELECT_ID);
                String sRID             = (String) mObject.get("id[level]");
                DomainObject dObject    = new DomainObject(sOID);

                StringList busSelects = new StringList();
                busSelects.add("attribute[" + sAttribute + "]");
                busSelects.add(DomainObject.SELECT_CURRENT);

                Map mData               = dObject.getInfo(context, busSelects);
                String sValue           = (String)mData.get("attribute[" + sAttribute + "]");
                String sCurrent         = (String)mData.get(DomainObject.SELECT_CURRENT);                
                Double dValue           = Task.parseToDouble(sValue);
                StringBuilder sbResult  = new StringBuilder();               
                String sColor           = "5F747D";
                
                if(sStates.length > 0) {
                    for (int j = 0; j < sStates.length; j++) {
                        if(sStates[j].equals(sCurrent)) {
                            sColor = sColors[j];
                            continue;
                        }
                    }                        
                }                

                StringBuilder sbStyleCommon = new StringBuilder();
                sbStyleCommon.append("border-top:1px solid #");
                sbStyleCommon.append(sColor);
                sbStyleCommon.append(";border-bottom:1px solid #");
                sbStyleCommon.append(sColor);
                sbStyleCommon.append(";font-size:6pt;float:left;text-align:center;margin:0px;padding:0px;width:18px;height:16px;line-height:15px;vertical-align:middle;text-Shadow:1px 1px 1px #111;");
                
                String sStyleActive         = " style='color:#FFFFFF;background:#" + sColor + ";" + sbStyleCommon.toString() + "'";                
                String sStyleInactive       = " style='color:transparent;background:transparent;" + sbStyleCommon.toString() + "'";                
                String sOnMouseOutActive    = " onmouseout='this.style.color=\"#FFFFFF\";this.style.background=\"#" + sColor + "\";this.style.textShadow=\"1px 1px 1px #111\";this.innerHTML=\"\";'";
                String sOnMouseOutInactive  = " onmouseout='this.style.color=\"#transparent\";this.style.background=\"transparent\";this.style.textShadow=\"none\";this.innerHTML=\"\";'";
                String sOnMouseOutCurrent   = " onmouseout='this.style.color=\"#FFFFFF\";this.style.background=\"#" + sColor + "\";this.style.textShadow=\"1px 1px 1px #111\";'";
                String sOnMouseOver         = " onmouseover='this.style.cursor=\"pointer\";this.style.color=\"#FFF\";this.style.background=\"#cc0000\";this.style.textShadow=\"1px 1px 2px #333\";this.title=\"" + XSSUtil.encodeForXML(context, sClickToSet) + " : ";
                
                String sOnMouseOver10       = sOnMouseOver + " 10%\";this.innerHTML=\"10\";'";
                String sOnMouseOver20       = sOnMouseOver + " 20%\";this.innerHTML=\"20\";'";
                String sOnMouseOver30       = sOnMouseOver + " 30%\";this.innerHTML=\"30\";'";
                String sOnMouseOver40       = sOnMouseOver + " 40%\";this.innerHTML=\"40\";'";
                String sOnMouseOver50       = sOnMouseOver + " 50%\";this.innerHTML=\"50\";'";
                String sOnMouseOver60       = sOnMouseOver + " 60%\";this.innerHTML=\"60\";'";
                String sOnMouseOver70       = sOnMouseOver + " 70%\";this.innerHTML=\"70\";'";
                String sOnMouseOver80       = sOnMouseOver + " 80%\";this.innerHTML=\"80\";'";
                String sOnMouseOver90       = sOnMouseOver + " 90%\";this.innerHTML=\"90\";'";
                String sOnMouseOver100      = sOnMouseOver + " 100%\";this.innerHTML=\"100\";'";
                
                StringBuilder sbMouseDown = new StringBuilder();
                sbMouseDown.append(" onmousedown='");
                sbMouseDown.append(sTarget);
                sbMouseDown.append("=\"../programcentral/emxProgramCentralUtil.jsp?mode=updateTaskPercentageComplete");
                sbMouseDown.append("&amp;objectId=").append(sOID);
                sbMouseDown.append("&amp;attribute=");
                sbMouseDown.append(sAttribute);
                sbMouseDown.append("&amp;rowId=");                
                sbMouseDown.append(sRID);
                sbMouseDown.append("&amp;isIndented=");
                sbMouseDown.append(bIsIndented);
                sbMouseDown.append("&amp;newValue=");

                String sOnMouseDown10   = sbMouseDown.toString() + "10.0\";'";
                String sOnMouseDown20   = sbMouseDown.toString() + "20.0\";'";
                String sOnMouseDown30   = sbMouseDown.toString() + "30.0\";'";
                String sOnMouseDown40   = sbMouseDown.toString() + "40.0\";'";
                String sOnMouseDown50   = sbMouseDown.toString() + "50.0\";'";
                String sOnMouseDown60   = sbMouseDown.toString() + "60.0\";'";
                String sOnMouseDown70   = sbMouseDown.toString() + "70.0\";'";
                String sOnMouseDown80   = sbMouseDown.toString() + "80.0\";'";
                String sOnMouseDown90   = sbMouseDown.toString() + "90.0\";'";
                String sOnMouseDown100  = sbMouseDown.toString()+ "100.0\";'";
                
                String sValue10 = "";
                String sValue20 = "";
                String sValue30 = "";
                String sValue40 = "";
                String sValue50 = "";
                String sValue60 = "";
                String sValue70 = "";
                String sValue80 = "";
                String sValue90 = "";
                String sValue100= "";
                
                String sStyle10 = sStyleInactive + sOnMouseOutInactive; 
                String sStyle20 = sStyleInactive + sOnMouseOutInactive; 
                String sStyle30 = sStyleInactive + sOnMouseOutInactive; 
                String sStyle40 = sStyleInactive + sOnMouseOutInactive; 
                String sStyle50 = sStyleInactive + sOnMouseOutInactive; 
                String sStyle60 = sStyleInactive + sOnMouseOutInactive; 
                String sStyle70 = sStyleInactive + sOnMouseOutInactive; 
                String sStyle80 = sStyleInactive + sOnMouseOutInactive; 
                String sStyle90 = sStyleInactive + sOnMouseOutInactive; 
                String sStyle100= sStyleInactive + sOnMouseOutInactive;                 

                if(dValue >= 10.0) {sStyle10 = sStyleActive + sOnMouseOutActive; if(dValue < 20.0) { sValue10 = "10"; sStyle10 = sStyleActive + sOnMouseOutCurrent;}}
                if(dValue >= 20.0) {sStyle20 = sStyleActive + sOnMouseOutActive; if(dValue < 30.0) { sValue20 = "20"; sStyle20 = sStyleActive + sOnMouseOutCurrent;}}
                if(dValue >= 30.0) {sStyle30 = sStyleActive + sOnMouseOutActive; if(dValue < 40.0) { sValue30 = "30"; sStyle30 = sStyleActive + sOnMouseOutCurrent;}}
                if(dValue >= 40.0) {sStyle40 = sStyleActive + sOnMouseOutActive; if(dValue < 50.0) { sValue40 = "40"; sStyle40 = sStyleActive + sOnMouseOutCurrent;}}
                if(dValue >= 50.0) {sStyle50 = sStyleActive + sOnMouseOutActive; if(dValue < 60.0) { sValue50 = "50"; sStyle50 = sStyleActive + sOnMouseOutCurrent;}}
                if(dValue >= 60.0) {sStyle60 = sStyleActive + sOnMouseOutActive; if(dValue < 70.0) { sValue60 = "60"; sStyle60 = sStyleActive + sOnMouseOutCurrent;}}
                if(dValue >= 70.0) {sStyle70 = sStyleActive + sOnMouseOutActive; if(dValue < 80.0) { sValue70 = "70"; sStyle70 = sStyleActive + sOnMouseOutCurrent;}}
                if(dValue >= 80.0) {sStyle80 = sStyleActive + sOnMouseOutActive; if(dValue < 90.0) { sValue80 = "80"; sStyle80 = sStyleActive + sOnMouseOutCurrent;}}
                if(dValue >= 90.0) {sStyle90 = sStyleActive + sOnMouseOutActive; 
                    if(dValue < 100.0){ sValue90 = "90"; sStyle90 = sStyleActive + sOnMouseOutCurrent;}
                    else { sValue100 = "100"; sStyle100 = sStyleActive + sOnMouseOutCurrent;}
                }
                
                sStyle10   = sStyle10.replace("style='", "style='border-left:1px  solid #" + sColor + ";");
                sStyle100 = sStyle100.replace("style='", "style='border-right:1px solid #" + sColor + ";");
                
                
                sbResult.append("<div style='visibility:hidden;display:none;'>");
                if(dValue < 100.0) { sbResult.append("0"); }
                sbResult.append(sValue).append("</div>");
                
                sbResult.append("<div").append(sStyle10).append(sOnMouseOver10).append(sOnMouseDown10).append(" >").append(sValue10).append("</div>");
                sbResult.append("<div").append(sStyle20).append(sOnMouseOver20).append(sOnMouseDown20).append(" >").append(sValue20).append("</div>");
                sbResult.append("<div").append(sStyle30).append(sOnMouseOver30).append(sOnMouseDown30).append(" >").append(sValue30).append("</div>");
                sbResult.append("<div").append(sStyle40).append(sOnMouseOver40).append(sOnMouseDown40).append(" >").append(sValue40).append("</div>");
                sbResult.append("<div").append(sStyle50).append(sOnMouseOver50).append(sOnMouseDown50).append(" >").append(sValue50).append("</div>");
                sbResult.append("<div").append(sStyle60).append(sOnMouseOver60).append(sOnMouseDown60).append(" >").append(sValue60).append("</div>");
                sbResult.append("<div").append(sStyle70).append(sOnMouseOver70).append(sOnMouseDown70).append(" >").append(sValue70).append("</div>");
                sbResult.append("<div").append(sStyle80).append(sOnMouseOver80).append(sOnMouseDown80).append(" >").append(sValue80).append("</div>");
                sbResult.append("<div").append(sStyle90).append(sOnMouseOver90).append(sOnMouseDown90).append(" >").append(sValue90).append("</div>");
                sbResult.append("<div").append(sStyle100).append(sOnMouseOver100).append(sOnMouseDown100).append(" >").append(sValue100).append("</div>");              
                sbResult.append("<div style='width:185px;'></div>");    
                
                vResult.addElement(sbResult.toString());
                
            }
        }
        
        return vResult;

    }               
    
    
    // Project Status Report Data Retrieval
    public Integer[][] getDataStatusReportProject(Context context, String[] args) throws Exception {
		        
		        
        HashMap paramMap        = (HashMap) JPO.unpackArgs(args);
        String sOID             = (String) paramMap.get("objectId");
        DomainObject dObject    = new DomainObject(sOID);
        Calendar cCurrent       = Calendar.getInstance();
        int iYearCurrent        = cCurrent.get(Calendar.YEAR);
        int iMonthCurrent       = cCurrent.get(Calendar.MONTH);
        int iWeekCurrent        = cCurrent.get(Calendar.WEEK_OF_YEAR);    
        long lCurrent           = cCurrent.getTimeInMillis();
        long lDiff              = 2592000000L;

        Integer[][] aResult     = new Integer[3][12];    
        // [0][X] = Tasks, [1][x] = deliverables, [2][X] = Issues
        // [X][0] = This Week
        // [X][1] = This Month
        // [X][2] = Soon
        // [X][3] = Overdue
        // [X][4] = State Assign
        // [X][5] = State Active
        // [X][6] = State Review
        // [X][7] = State Complete
        for (int i = 0; i < 3; i++) { for (int j = 0; j < 12; j++) { aResult[i][j] = 0; } }        
        
        StringList slTasks = new StringList();
        slTasks.add(DomainObject.SELECT_TYPE);
        slTasks.add(DomainObject.SELECT_CURRENT);
        slTasks.add(SELECT_FROM_SUBTASK);
        slTasks.add(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
        slTasks.add(Task.SELECT_TASK_ACTUAL_FINISH_DATE);
        
        
        Pattern pTypes  = new Pattern("Task");        
        MapList mlTasks = dObject.getRelatedObjects(context, "Subtask", "Task Management", slTasks, null, false, true, (short)0, "", "", 0, pTypes, null, null);

        for(int i = 0; i < mlTasks.size(); i++) {

            Map mTask           = (Map)mlTasks.get(i);
            String sCurrent     = (String)mTask.get(DomainObject.SELECT_CURRENT);
            String sHasSubtask  = (String)mTask.get(SELECT_FROM_SUBTASK);            
            
            if(sHasSubtask.equalsIgnoreCase("FALSE")) {
                
                if(sCurrent.equals("Create")) 		{ aResult[0][4]++; }
                else if(sCurrent.equals("Assign")) 	{ aResult[0][4]++; }
                else if(sCurrent.equals("Active")) 	{ aResult[0][5]++; }
                else if(sCurrent.equals("Review")) 	{ aResult[0][6]++; }
                else if(sCurrent.equals("Complete"))    { aResult[0][7]++; }	
                            
                Calendar cTarget    = Calendar.getInstance();
                String sTargetDate  = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                cTarget.setTime(sdf.parse(sTargetDate));
			
                if (!sCurrent.equals("Complete")) {
								
                    int iYearTarget     = cTarget.get(Calendar.YEAR);
                    int iMonthTarget    = cTarget.get(Calendar.MONTH);
                    int iWeekTarget     = cTarget.get(Calendar.WEEK_OF_YEAR);
                    long lTarget        = cTarget.getTimeInMillis();

                    if (iYearCurrent == iYearTarget) {
                        if (iWeekCurrent == iWeekTarget) {
                            aResult[0][0]++;
                        }
                    }				
                    if (iYearCurrent == iYearTarget) {
                        if (iMonthCurrent == iMonthTarget) {
                            aResult[0][1]++;
                        }
                    }		
                    if ((lTarget - lCurrent) < lDiff) {
                        if ((lTarget - lCurrent) > 0) {
                            aResult[0][2]++;
                        }	                    
                    }	                    
                    if (cCurrent.after(cTarget)) {
                        aResult[0][3]++;
                        aResult[0][9]++;
                    } else {
                        aResult[0][8]++;
                    }
                    
                } else {				
                    
                    String sActualDate = (String)mTask.get(Task.SELECT_TASK_ACTUAL_FINISH_DATE);
                    if (sActualDate != null && !"".equals(sActualDate)) {
                        Calendar cActual = Calendar.getInstance();
                        cActual.setTime(sdf.parse(sActualDate));	
                        if (cActual.after(cTarget)) {
                            aResult[0][10]++;
                        } else {
                            aResult[0][11]++;
                        }
                    }
                }
                
            }         
        }
        
        MapList mlDeliverables = retrieveDeliverablesOfProject(context, args, "All", "Create,Assign,Active,Review,Complete", sOID);
        
        for(int i = 0; i < mlDeliverables.size(); i++) {

            Map mDeliverable    = (Map)mlDeliverables.get(i);
            String sCurrent     = (String)mDeliverable.get("from.current");
                
            if(sCurrent.equals("Create"))           { aResult[1][4]++; }
            else if(sCurrent.equals("Assign"))      { aResult[1][4]++; }
            else if(sCurrent.equals("Active"))      { aResult[1][5]++; }
            else if(sCurrent.equals("Review"))      { aResult[1][6]++; }
            else if(sCurrent.equals("Complete"))    { aResult[1][7]++; }	
                            
            Calendar cTarget    = Calendar.getInstance();
            String sTargetDate  = (String)mDeliverable.get(SELECT_FROM_ATTRIBUTE_TASK_EXTIMATED_FINISH_DATE);
            cTarget.setTime(sdf.parse(sTargetDate));
			
            if (!sCurrent.equals("Complete")) {

                int iYearTarget     = cTarget.get(Calendar.YEAR);
                int iMonthTarget    = cTarget.get(Calendar.MONTH);
                int iWeekTarget     = cTarget.get(Calendar.WEEK_OF_YEAR);
                long lTarget        = cTarget.getTimeInMillis();                

                if (iYearCurrent == iYearTarget) {
                    if (iWeekCurrent == iWeekTarget) {
                        aResult[1][0]++;
                    }
                }				
                if (iYearCurrent == iYearTarget) {
                    if (iMonthCurrent == iMonthTarget) {
                        aResult[1][1]++;
                    }
                }	
                if ((lTarget - lCurrent) < lDiff) {
                    if ((lTarget - lCurrent) > 0) {                    
                        aResult[1][2]++;
                    }
                }	                
                if (cCurrent.after(cTarget)) {
                    aResult[1][3]++;
                    aResult[1][9]++;
                } else {
                    aResult[1][8]++;
                }

            } else {				

                String sActualDate = (String)mDeliverable.get(SELECT_FROM_ATTRIBUTE_TASK_ACTUAL_FINISH_DATE);					
                if (sActualDate != null && !"".equals(sActualDate)) {
                    Calendar cActual = Calendar.getInstance();
                    cActual.setTime(sdf.parse(sActualDate));					
                    if (cActual.after(cTarget)) {
                        aResult[1][10]++;
                    } else {
                        aResult[1][11]++;
                    }
                }
            }                
                     
        }   
        
        
        StringList slIssues = new StringList();
        slIssues.add(DomainObject.SELECT_CURRENT);
        slIssues.add(SELECT_ESTIMATED_END_DATE);
        
       	MapList mlIssues = dObject.getRelatedObjects(context, "Issue", "Issue", slIssues, null, true, false, (short) 1, "", "", 0);
	                
        for (int i = 0; i < mlIssues.size(); i++) {
        
            Map mIssue          = (Map)mlIssues.get(i);			
            String sCurrent     = (String)mIssue.get(DomainObject.SELECT_CURRENT);
            String sTargetDate  = (String)mIssue.get(SELECT_ESTIMATED_END_DATE);
			
            if(sCurrent.equals("Create")) 	{ aResult[2][4]++; }
            else if(sCurrent.equals("Assign")) 	{ aResult[2][4]++; }
            else if(sCurrent.equals("Active")) 	{ aResult[2][5]++; }
            else if(sCurrent.equals("Review")) 	{ aResult[2][6]++; }
            else if(sCurrent.equals("Closed"))	{ aResult[2][7]++; }				
		            			
            if (sTargetDate != null && !"".equals(sTargetDate)) {

                Calendar cTarget = Calendar.getInstance();
                cTarget.setTime(sdf.parse(sTargetDate));
					
                if (!sCurrent.equals("Closed") ) {
					
                    int iYearTarget     = cTarget.get(Calendar.YEAR);
                    int iMonthTarget    = cTarget.get(Calendar.MONTH);
                    int iWeekTarget     = cTarget.get(Calendar.WEEK_OF_YEAR);
                    long lTarget        = cTarget.getTimeInMillis();                    
					
                    if (iYearCurrent == iYearTarget) {
                        if (iWeekCurrent == iWeekTarget) {
                            aResult[2][0]++;
                        }
                    }				
                    if (iYearCurrent == iYearTarget) {
                        if (iMonthCurrent == iMonthTarget) {
                            aResult[2][1]++;
                        }
                    }		
                    if ((lTarget - lCurrent) < lDiff) {
                        if ((lTarget - lCurrent) > 0) {
                            aResult[2][2]++;
                        }
                    }	                    
                    if (cCurrent.after(cTarget)) {
                        aResult[2][3]++;
                        aResult[2][9]++;
                    } else {
                        aResult[2][8]++;
                    }
					
                } else {				
                    String sActualDate = (String)mIssue.get(SELECT_ATTRIBUTE_ACTUAL_END_DATE);					
                    if (sActualDate != null && !"".equals(sActualDate)) {
                        Calendar cActual = Calendar.getInstance();
                        cActual.setTime(sdf.parse(sActualDate));					
                        if (cActual.after(cTarget)) {
                            aResult[2][10]++;
                        } else {
                            aResult[2][11]++;
                        }
                    }
                }
            }
        
	}
           
        return aResult;
		        

    }  
    public StringBuffer[] getDataGanttChart(Context context, String[] args) throws Exception {
        
        
        HashMap paramMap            = (HashMap) JPO.unpackArgs(args);
        String sOID                 = (String) paramMap.get("objectId");           
        String sExpandLevels        = (String) paramMap.get("expandLevels");           
        Short levels                = Short.parseShort(sExpandLevels);
        StringBuffer[] aData        = new StringBuffer[8];        
        StringBuffer sbCategories   = new StringBuffer();
        StringBuffer sbTarget       = new StringBuffer();
        StringBuffer sbActual       = new StringBuffer();
        StringBuffer sbGates        = new StringBuffer();        
        StringBuffer sbEvents       = new StringBuffer();        
        StringBuffer sbPopups       = new StringBuffer();        
        StringBuffer sbTaskDetails  = new StringBuffer();        
        StringBuffer sbHeight       = new StringBuffer();
        
        DomainObject doProject = new DomainObject(sOID);
	
	StringList busSelects = new StringList();
	StringList relSelects = new StringList();
	busSelects.add(DomainObject.SELECT_TYPE);
	busSelects.add(DomainObject.SELECT_ID);
	busSelects.add(DomainObject.SELECT_NAME);
	busSelects.add(DomainObject.SELECT_POLICY);
	busSelects.add(DomainObject.SELECT_CURRENT);
	busSelects.add(DomainObject.SELECT_OWNER);
	busSelects.add(Task.SELECT_SCHEDULE_DURATION_UNITS);
	busSelects.add(Task.SELECT_TASK_ESTIMATED_DURATION);
	busSelects.add(Task.SELECT_TASK_ESTIMATED_START_DATE);
	busSelects.add(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
	busSelects.add(Task.SELECT_TASK_ACTUAL_DURATION);
	busSelects.add(Task.SELECT_TASK_ACTUAL_START_DATE);
	busSelects.add(Task.SELECT_TASK_ACTUAL_FINISH_DATE);
	busSelects.add(DomainObject.SELECT_PERCENTCOMPLETE);
	busSelects.add(SELECT_DELEVERABLE_NAME);
    relSelects.add(ProgramCentralConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER);		
        
    StringList slSelectsPerson = new StringList();
    slSelectsPerson.add(DomainObject.SELECT_ATTRIBUTE_FIRSTNAME);
    slSelectsPerson.add(DomainObject.SELECT_ATTRIBUTE_LASTNAME);
    slSelectsPerson.add(SELECT_ATTRIBUTE_WORK_PHONE_NUMBER);
	
	MapList mlTasks = doProject.getRelatedObjects(context, "Subtask", "Task Management", busSelects, relSelects, false, true, levels, "", "", 10000);
	mlTasks.sort(ProgramCentralConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER, "ascending", "integer");
	
	
	// Append current date
	Calendar cDate = Calendar.getInstance();		
	sbGates.append("{ color: '#333',");
	sbGates.append("width: 3,");
	sbGates.append("value: Date.UTC(").append(cDate.get(Calendar.YEAR)).append(",").append(cDate.get(Calendar.MONTH)).append(",").append(cDate.get(Calendar.DAY_OF_MONTH)).append("),");
	sbGates.append("label: {");
	sbGates.append("text: 'Today',");
	sbGates.append("style: {");
	sbGates.append("color: '#333',");
	sbGates.append("fontWeight: 'normal'");
	sbGates.append("}}}");
        
	if(mlTasks.size() > 0) {
	
            for (int i = 0; i < mlTasks.size(); i++) {
		
                Map mTask 	= (Map)mlTasks.get(i);                
                String sType 	= (String)mTask.get(DomainObject.SELECT_TYPE);
                String sName 	= (String)mTask.get(DomainObject.SELECT_NAME);
                
                if(sType.equals("Gate")) { 
                
                    String sDate 	= (String)mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                    cDate.setTime(sdf.parse(sDate));
                    if(sbGates.length() > 0) { sbGates.append(","); }
                    sbGates.append("{ color: '#").append(sColorRed).append("',");
                    sbGates.append("width: 3,");
                    sbGates.append("value: Date.UTC(").append(cDate.get(Calendar.YEAR)).append(",").append(cDate.get(Calendar.MONTH)).append(",").append(cDate.get(Calendar.DAY_OF_MONTH)).append("),");
                    sbGates.append("label: {");
                    sbGates.append("text: '").append(sName).append("',");
                    sbGates.append("style: {");
                    sbGates.append("color: '#").append(sColorRed).append("',");
                    sbGates.append("fontWeight: 'normal'");
                    sbGates.append("}}}");
				
                    mlTasks.remove(i);
				
                } else {

                    String sOIDTask         = (String)mTask.get(DomainObject.SELECT_ID);
                    String sCurrent         = (String)mTask.get(DomainObject.SELECT_CURRENT);
                    String sOwner           = (String)mTask.get(DomainObject.SELECT_OWNER);
                    String sTargetStart     = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_START_DATE);
                    String sTargetEnd       = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);    
                    String sTargetDuration  = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_DURATION);
                    String sActualStart     = (String)mTask.get(Task.SELECT_TASK_ACTUAL_START_DATE);
                    String sActualEnd       = (String)mTask.get(Task.SELECT_TASK_ACTUAL_FINISH_DATE);
                    String sActualDuration  = (String)mTask.get(Task.SELECT_TASK_ACTUAL_DURATION);
                    String sUnits           = (String)mTask.get(Task.SELECT_SCHEDULE_DURATION_UNITS);
                    String sPercent         = (String)mTask.get(DomainObject.SELECT_PERCENTCOMPLETE);
                    
                    Calendar cTargetStart   = Calendar.getInstance();
                    Calendar cTargetEnd     = Calendar.getInstance();
                    Calendar cActualStart   = Calendar.getInstance();
                    Calendar cActualEnd     = Calendar.getInstance();
                    cTargetStart.setTime(sdf.parse(sTargetStart));	
                    cTargetEnd.setTime(sdf.parse(sTargetEnd));	
                    
                                        
                    
                    // List of Categories                    
                    if(sbCategories.length() > 0) { sbCategories.append(","); }
                    sbCategories.append("'").append(sName).append("'");
                    
                    // Target Bars
                    String sTargetColor = sColor025;                                       
//                    double dPercent = Task.parseToDouble(sPercent);
//                    if(dPercent <= 25.0)        { sTargetColor = sColor025; }
//                    else if(dPercent <= 50.0)   { sTargetColor = sColor050; }
//                    else if(dPercent <= 75.0)   { sTargetColor = sColor075; }
//                    else if(dPercent < 100.0)   { sTargetColor = sColor099; } 
                    
                    if(sCurrent.equals("Active"))           { sTargetColor = sColor050; }
                    else if(sCurrent.equals("Review"))      { sTargetColor = sColor099; }
                    else if(sCurrent.equals("Complete"))    { sTargetColor = sColor100; }
                    
                    if(sbTarget.length() > 0) { sbTarget.append(","); }
                    sbTarget.append("{");
                    sbTarget.append("   id : '").append(sOIDTask).append("',");
                    sbTarget.append("   name : '").append(sName).append("',");                    
                    sbTarget.append("   type : '").append(sType).append("',");                    
                    sbTarget.append("   info : '").append(sCurrent).append(" (").append(sPercent).append("%)',");                    
                    sbTarget.append("   color : '#").append(sTargetColor).append("',");
                    sbTarget.append("   low:Date.UTC(").append(cTargetStart.get(Calendar.YEAR)).append(",").append(cTargetStart.get(Calendar.MONTH)).append(",").append(cTargetStart.get(Calendar.DAY_OF_MONTH)).append("),");
                    sbTarget.append("   high:Date.UTC(").append(cTargetEnd.get(Calendar.YEAR)).append(",").append(cTargetEnd.get(Calendar.MONTH)).append(",").append(cTargetEnd.get(Calendar.DAY_OF_MONTH)).append(")");
                    sbTarget.append("}");
 
                    // Actual Bars
                    String sActualColor = sColorGray; 
                    if(sbActual.length() > 0) { sbActual.append(","); }
                    sbActual.append("{");
                    sbActual.append("   id : '").append(sOIDTask).append("',");
                    sbActual.append("   name : '").append(sName).append("',");
                    sbActual.append("   type : '").append(sType).append("',");
                    sbActual.append("   info : '").append(sCurrent).append(" (").append(sPercent).append("%)',");
                    if(null != sActualStart) {
                        if(!"".equals(sActualStart)) {
                            cActualStart.setTime(sdf.parse(sActualStart));
                            if(sCurrent.equals("Complete")) {
                                if(null != sActualEnd) {
                                    if(!"".equals(sActualEnd)) {
                                        cActualEnd.setTime(sdf.parse(sActualEnd));
                                        if(cActualEnd.after(cTargetEnd)) { sActualColor = sColorRed; }
                                        else { sActualColor = sColorGreen; }
                                    }
                                }
                            } else {
                                if((null != sActualEnd) && (!"".equals(sActualEnd))) {
                                    cActualEnd.setTime(sdf.parse(sActualEnd));                                    
                                } else {
                                    cActualEnd.setTime(sdf.parse(sTargetEnd));
                                    Long lTarget = cTargetStart.getTimeInMillis();
                                    Long lActual = cActualStart.getTimeInMillis();
                                    Long lDiff = lActual - lTarget;
                                    Long lDiffDays = lDiff / (24*60*60*1000);
                                    Integer iDiff = lDiffDays.intValue();
                                    cActualEnd.add(java.util.GregorianCalendar.DAY_OF_YEAR, iDiff);
                                }
                                if(cActualStart.after(cTargetStart)) { sActualColor = sColorOrange; }
                                else if(cActualStart.before(cDate)) { sActualColor = sColorOrange; }
                                else { sActualColor = sColorGray; }
                            }
                            
                            sbActual.append("   color : '#").append(sActualColor).append("',");
                            sbActual.append("   low:Date.UTC(").append(cActualStart.get(Calendar.YEAR)).append(",").append(cActualStart.get(Calendar.MONTH)).append(",").append(cActualStart.get(Calendar.DAY_OF_MONTH)).append("),");
                            sbActual.append("   high:Date.UTC(").append(cActualEnd.get(Calendar.YEAR)).append(",").append(cActualEnd.get(Calendar.MONTH)).append(",").append(cActualEnd.get(Calendar.DAY_OF_MONTH)).append(")");
                            
                        }
                    }                    
                    sbActual.append("}");
                    
                    // List of Events and Popups                 
                    sbEvents.append("if(this.id == '").append(sOIDTask).append("') { App.popUp").append(i).append(".open(); App.popUp").append(i).append(".positionTo(document.body, 50, 50);}");
                    sbPopups.append("App.popUp").append(i).append("= new PopUpWindow('").append(sName).append("', { contentDiv: '").append(sOIDTask).append("', isResizable: true, width: 300 });");
                    
                    
                    // Task Details                  
                    if(!sUnits.equals("")) { sUnits = " " + sUnits; }
                    if(sTargetStart.contains(" ")) { sTargetStart = sTargetStart.substring(0, sTargetStart.indexOf(" ")); }
                    if(sTargetEnd.contains(" ")) { sTargetEnd = sTargetEnd.substring(0, sTargetEnd.indexOf(" ")); }                
                    if(null == sActualDuration) { sActualDuration = ""; }
                    if(null != sActualStart) {if(sActualStart.contains(" ")) { sActualStart = sActualStart.substring(0, sActualStart.indexOf(" ")); }} else { sActualStart = ""; }
                    if(null != sActualEnd) {if(sActualEnd.contains(" ")) { sActualEnd = sActualEnd.substring(0, sActualEnd.indexOf(" ")); }} else { sActualEnd = ""; sActualDuration = "";}                    
                                        
                    String sActual  = sActualStart + " - " + sActualEnd + " (" + sActualDuration + sUnits + ")";
                    String sTarget  = sTargetStart + " - " + sTargetEnd + " (" + sTargetDuration + sUnits + ")";                    
                    String sMail    = com.matrixone.apps.domain.util.PersonUtil.getEmail(context, sOwner);
                    
                    String sOIDPerson = com.matrixone.apps.domain.util.PersonUtil.getPersonObjectID(context, sOwner);
                    DomainObject doPerson = new DomainObject(sOIDPerson);
                    
                    Map mDataPerson = doPerson.getInfo(context, slSelectsPerson);
                    
                    String sPhone       = (String)mDataPerson.get(SELECT_ATTRIBUTE_WORK_PHONE_NUMBER);    
                    String sFirstName   = (String)mDataPerson.get(DomainObject.SELECT_ATTRIBUTE_FIRSTNAME);    
                    String sLastName    = (String)mDataPerson.get(DomainObject.SELECT_ATTRIBUTE_LASTNAME);    
                    String sPerson      = sFirstName + " " + sLastName;

                    String sDeliverables = "1";
                    if(mTask.get(SELECT_DELEVERABLE_NAME) == null) { sDeliverables = "none"; }
                    else  if(mTask.get(SELECT_DELEVERABLE_NAME) instanceof StringList) {	
                        StringList slDeliverables = (StringList)mTask.get(SELECT_DELEVERABLE_NAME);
                        sDeliverables = String.valueOf(slDeliverables.size());
                    } 
                    
                    sDeliverables = "<a href='#' onClick=\"javascript:window.open('../common/emxTree.jsp?DefaultCategory=PMCDeliverableCommandPowerView&objectId=" + sOIDTask + "','', 'height=650,width=950,toolbar=no,directories=no,status=no,menubar=no;return false;')\" >" + sDeliverables + "</a>";

 			
                    sbTaskDetails.append("<div id='").append(sOIDTask).append("'>");
                    sbTaskDetails.append("<table>");
                    sbTaskDetails.append("<tr><td style='text-align:right'><strong>Activity</strong></td><td width='3px'></td><td>").append(sType).append(" <a href='#' onClick=\"javascript:window.open('../common/emxTree.jsp?objectId=").append(sOIDTask).append("','', 'height=650,width=950,toolbar=no,directories=no,status=no,menubar=no;return false;')\">").append(sName).append("</a>").append("</td></tr>");
                    sbTaskDetails.append("<tr><td style='text-align:right'><strong>Owner</strong></td><td width='3px'></td><td>").append(sPerson).append("</td></tr>");
                    sbTaskDetails.append("<tr><td style='text-align:right'><strong>Mail</strong></td><td width='3px'></td><td><a href='mailto:").append(sMail).append("?subject=(Your task) ").append(sName).append("'>").append(sMail).append("</a>").append("</td></tr>");
                    sbTaskDetails.append("<tr><td style='text-align:right'><strong>Phone</strong></td><td width='3px'></td><td>").append(sPhone).append("</td></tr>");
                    sbTaskDetails.append("<tr><td style='text-align:right'><strong>Plan</strong></td><td width='3px'></td><td>").append(sTarget).append("</td></tr>");
                    sbTaskDetails.append("<tr><td style='text-align:right'><strong>Actual</strong></td><td width='3px'></td><td>").append(sActual).append("</td></tr>");                    
                    sbTaskDetails.append("<tr><td style='text-align:right'><strong>Status</strong></td><td width='3px'></td><td>").append(sCurrent).append(" (").append(sPercent).append("%)</td></tr>");
                    sbTaskDetails.append("<tr><td style='text-align:right'><strong>Deliverables</strong></td><td width='3px'></td><td>").append(sDeliverables).append("</td></tr>");
                    sbTaskDetails.append("</table>");
                    sbTaskDetails.append("</div>");
               
                }			
                
                
            }
	}	
	
        // Determine Chart Height
	int iHeightGantt = 40;	
	iHeightGantt += mlTasks.size() * 30;
        sbHeight.append(String.valueOf(iHeightGantt));

        aData[0] = sbCategories;
        aData[1] = sbTarget;
        aData[2] = sbActual;
        aData[3] = sbGates;
        aData[4] = sbEvents;
        aData[5] = sbTaskDetails;
        aData[6] = sbPopups;
        aData[7] = sbHeight;
        
        return aData;
        
    }        
    
    // - Retrieve Tasks         
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getPendingTasks(Context context, String[] args) throws Exception {
        
        
        HashMap paramMap    = (HashMap) JPO.unpackArgs(args);
        String sOID         = (String) paramMap.get("objectId");        
        String sMode        = (String) paramMap.get("mode");        
        MapList mlResult    = new MapList();
        String[] init       = new String[]{};        
        HashMap argsMap     = new HashMap();        
        String[] methodargs;
        MapList mlProjects;       

        if(null == sOID || "".equals(sOID)) {
            argsMap.put("objectId", "");
            methodargs = JPO.packArgs(argsMap);
            mlProjects = (com.matrixone.apps.domain.util.MapList) JPO.invoke(context, "emxProjectSpace", init, "getActiveProjects", methodargs, com.matrixone.apps.domain.util.MapList.class);
        }  else {
            argsMap.put("objectId", sOID);
            methodargs = JPO.packArgs(argsMap);
            mlProjects = (com.matrixone.apps.domain.util.MapList)JPO.invoke(context, "emxProgramUI", init, "getProjectsOfProgram", methodargs, com.matrixone.apps.domain.util.MapList.class);	            
        }
     
        // sort the projects by the ones that are due sooner
        mlProjects.sort(SELECT_ATTRIBUTE_TASK_ESTIMATED_END_DATE, "ascending", "date");
	    
        // get the number of charts we want to display and only display data for that number of charts
        String strNumberOfProjects =  EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ProjectSummary.ChartsToDisplay");
        int numberOfProjects = Integer.parseInt(strNumberOfProjects);
        if ( mlProjects.size() < numberOfProjects ) {
        	numberOfProjects =  mlProjects.size();
        }
        
        MapList tempMapList = new MapList();;
	    for (int i=0; i<numberOfProjects;i++) { 
		    Map projectMap = (Map) mlProjects.get(i);
		    tempMapList.add(projectMap);
	    }
    
        // set the project list to number of projects set by ChartsToDisplay
	    mlProjects = tempMapList;
	    
	    //Check for sub-project IDs starts : To avoid repetitive tasks.
	    String[] arrProjectId = new String[numberOfProjects]; 
	    for(int i = 0; i < numberOfProjects; i++){
                Map mProject = (Map) mlProjects.get(i);
                String sOIDProject = (String) mProject.get(DomainObject.SELECT_ID);
            arrProjectId[i] = sOIDProject;
	    }
	    
	    String SELECT_SUB_PROJECT_ID = "from["+ DomainConstants.RELATIONSHIP_SUBTASK + "].to[" + DomainConstants.TYPE_PROJECT_SPACE +"].id";
	    MapList mlSubProjectsId = DomainObject.getInfo(context, arrProjectId, new StringList(SELECT_SUB_PROJECT_ID));
	    StringList subProjectIdList = new StringList();
	    for(int i = 0; i < mlSubProjectsId.size(); i++ ){
	    	Map projectMap =  (Map) mlSubProjectsId.get(i);
	    	String subProjectId = (String) projectMap.get(SELECT_SUB_PROJECT_ID);
	    	subProjectIdList.addAll(ProgramCentralUtil.getAsStringList(subProjectId));
	    }
	    //Check for sub-project IDs ends
        
        if (numberOfProjects > 0) {
            for (int i = 0; i < numberOfProjects; i++) {
                Map mProject = (Map) mlProjects.get(i);
                String sOIDProject = (String) mProject.get(DomainObject.SELECT_ID);
                if(subProjectIdList.contains(sOIDProject)){
                	continue;
                }
                MapList mlTemp = retrieveOpenTasksOfProject(context, args, sMode, sOIDProject);  
                mlResult.addAll(mlTemp);              
            }
        }
        
        return mlResult;                
                
    }
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getPendingTasksOfProject(Context context, String[] args) throws Exception {

        HashMap paramMap    = (HashMap) JPO.unpackArgs(args);
        String sOID         = (String) paramMap.get("objectId");
        String sMode        = (String) paramMap.get("mode");

        return retrieveOpenTasksOfProject(context, args, sMode, sOID);          
    } 
    public MapList retrieveOpenTasksOfProject(Context context, String[] args, String sMode, String sOIDProject) throws Exception {

        
        MapList mlResult        = new MapList();
        DomainObject doProject  = new DomainObject(sOIDProject);
        Calendar cal            = Calendar.getInstance(TimeZone.getDefault());
        Calendar cCurrent       = Calendar.getInstance();
        int iYearCurrent        = cCurrent.get(Calendar.YEAR);
        int iMonthCurrent       = cCurrent.get(Calendar.MONTH);
        int iWeekCurrent        = cCurrent.get(Calendar.WEEK_OF_YEAR);        
        long lCurrent           = cCurrent.getTimeInMillis();
        long lDiff              = 2592000000L;   
        
        if(null == sMode) { sMode = "All"; }        
        
        StringList busSelects = new StringList();
        StringList relSelects = new StringList();
        busSelects.add(DomainObject.SELECT_ID);
        busSelects.add(DomainObject.SELECT_TYPE);
        busSelects.add(DomainObject.SELECT_NAME);
        busSelects.add(DomainObject.SELECT_POLICY);
        busSelects.add(DomainObject.SELECT_CURRENT);
        busSelects.add(DomainObject.SELECT_OWNER);
        busSelects.add(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
        busSelects.add(SELECT_FROM_SUBTASK);                           
                
        MapList mlTasks = doProject.getRelatedObjects(context, "Program Project,Subtask", "Project Space,Task Management", busSelects, relSelects, false, true, (short) 0, "", "", 0);

        if (mlTasks.size() > 0) {

            for (int j = 0; j < mlTasks.size(); j++) {

                Map mTask = (Map) mlTasks.get(j);
                String sCurrent     = (String) mTask.get(DomainObject.SELECT_CURRENT);
                String sType        = (String) mTask.get(DomainObject.SELECT_TYPE);
                String sIsLeaf      = (String) mTask.get(SELECT_FROM_SUBTASK);
                String sOIDResult   = (String) mTask.get(DomainObject.SELECT_ID);
                String sOwner       = (String) mTask.get(DomainObject.SELECT_OWNER);
                        
                if (mxType.isOfParentType(context, sType, DomainConstants.TYPE_TASK)) {
                    if (!sCurrent.equals("Complete")) {
                        if (sIsLeaf.equalsIgnoreCase("FALSE")) {
                                    
                            Map mResult = new HashMap();
                            mResult.put(DomainObject.SELECT_ID        , sOIDResult    );
                            mResult.put(DomainObject.SELECT_OWNER     , sOwner        );
                            mResult.put(DomainObject.SELECT_CURRENT   , sCurrent      );

                            if (sMode.equals("All")) {
                                mlResult.add(mResult);
                            } else if (sMode.equals("Overdue")) {
                                String sTargetDate = (String) mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                                if (sTargetDate != null && !"".equals(sTargetDate)) {

                                    Calendar cTarget = Calendar.getInstance();
                                    cTarget.setTime(sdf.parse(sTargetDate));

                                    if (cTarget.before(cal)) {
                                        mlResult.add(mResult);
                                    }
                                }                                
                            } else if (sMode.equals("This Week")) {
                                String sTargetDate = (String) mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                                if (sTargetDate != null && !"".equals(sTargetDate)) {

                                    Calendar cTarget = Calendar.getInstance();
                                    cTarget.setTime(sdf.parse(sTargetDate));

                                    int iYearTarget = cTarget.get(Calendar.YEAR);
                                    int iWeekTarget = cTarget.get(Calendar.WEEK_OF_YEAR);

                                    if (iYearCurrent == iYearTarget) {
                                        if (iWeekCurrent == iWeekTarget) {
                                            mlResult.add(mResult);
                                        }
                                    }
                                }
                            } else if (sMode.equals("This Month")) {
                                String sTargetDate = (String) mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                                if (sTargetDate != null && !"".equals(sTargetDate)) {

                                    Calendar cTarget = Calendar.getInstance();
                                    cTarget.setTime(sdf.parse(sTargetDate));

                                    int iYearTarget = cTarget.get(Calendar.YEAR);
                                    int iMonthTarget = cTarget.get(Calendar.MONTH);

                                    if (iYearCurrent == iYearTarget) {
                                        if (iMonthCurrent == iMonthTarget) {
                                            mlResult.add(mResult);
                                        }
                                    }
                                }
                            } else if (sMode.equals("Soon")) {
                                String sTargetDate = (String) mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                                if (sTargetDate != null && !"".equals(sTargetDate)) {

                                    Calendar cTarget = Calendar.getInstance();
                                    cTarget.setTime(sdf.parse(sTargetDate));
                                    long lTarget        = cTarget.getTimeInMillis();	
                                    
                                    if ((lTarget - lCurrent) < lDiff) {
                                        if ((lTarget - lCurrent) > 0) {
                                            mlResult.add(mResult);
                                        }	                    
                                    }	    
                                }
                            }                             
                        }
                    }
                }
            }
        }
  
        return mlResult;

    }

    // - Deliverables      
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getDeliverablesByTask(Context context, String[] args) throws Exception {
        
        HashMap paramMap    = (HashMap) JPO.unpackArgs(args);
        String sOID         = (String) paramMap.get("objectId"); 
        String sMode        = (String) paramMap.get("mode");
        MapList mlResult    = new MapList();
        String[] init       = new String[]{};
        HashMap argsMap     = new HashMap();
        String[] methodargs ;        
        MapList mlProjects;
        
        if(null == sOID || "".equals(sOID)) {
            argsMap.put("objectId", "");
            methodargs = JPO.packArgs(argsMap);
            mlProjects = (com.matrixone.apps.domain.util.MapList) JPO.invoke(context, "emxProjectSpace", init, "getActiveProjects", methodargs, com.matrixone.apps.domain.util.MapList.class);
        }  else {
            argsMap.put("objectId", sOID);
            methodargs = JPO.packArgs(argsMap);
            mlProjects = (com.matrixone.apps.domain.util.MapList)JPO.invoke(context, "emxProgramUI", init, "getProjectsOfProgram", methodargs, com.matrixone.apps.domain.util.MapList.class);	            
        }        
        
        // sort the projects by the ones that are due sooner
        mlProjects.sort(SELECT_ATTRIBUTE_TASK_ESTIMATED_END_DATE, "ascending", "date");
	    
        // get the number of charts we want to display and only display data for that number of charts
        String strNumberOfProjects =  EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ProjectSummary.ChartsToDisplay");
        int numberOfProjects = Integer.parseInt(strNumberOfProjects);
        if ( mlProjects.size() < numberOfProjects ) {
        	numberOfProjects =  mlProjects.size();
        }
        
        MapList tempMapList = new MapList();;
	    for (int i=0; i<numberOfProjects;i++) { 
		    Map projectMap = (Map) mlProjects.get(i);
		    tempMapList.add(projectMap);
	    }
    
        // set the project list to number of projects set by ChartsToDisplay
	    mlProjects = tempMapList;

	    if (mlProjects.size() > 0) {        
            for (int i = 0; i < mlProjects.size(); i++) {
                Map mProject = (Map) mlProjects.get(i);
                String sOIDProject = (String) mProject.get(DomainObject.SELECT_ID);
                
                MapList mlTemp   = retrieveDeliverablesOfProject(context, args, sMode, "Create,Assign,Active,Review", sOIDProject);
                
                
                StringBuilder sbResults = new StringBuilder();
                for(int xxx = mlTemp.size() - 1; xxx >=0; xxx--) {
                    Map mResult = (Map)mlTemp.get(xxx);
                    String sOIDResult = (String)mResult.get("to.id");
                    HashMap deliverablesMap     = new HashMap();
                    deliverablesMap.put("id", sOIDResult);
                    mlResult.add(deliverablesMap);
                }
                
    
            }
        }
        return mlResult;

    }
    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getDeliverablesOfProjectByTask(Context context, String[] args) throws Exception {
        
        HashMap paramMap    = (HashMap) JPO.unpackArgs(args);
        String sOID         = (String) paramMap.get("objectId"); 
        String sMode        = (String) paramMap.get("mode");
        MapList mlTemp   = retrieveDeliverablesOfProject(context, args, sMode, "Create,Assign,Active,Review", sOID);
        MapList mlResult    = new MapList();
        
        StringBuilder sbResults = new StringBuilder();
        for(int i = mlTemp.size() - 1; i >=0; i--) {
            Map mResult = (Map)mlTemp.get(i);
            String sOIDResult = (String)mResult.get("to.id");
            HashMap deliverablesMap     = new HashMap();
            deliverablesMap.put("id", sOIDResult);
            mlResult.add(deliverablesMap);
        }
        return mlResult;
    }
    public MapList retrieveDeliverablesOfProject(Context context, String[] args, String sMode, String sStates, String sOIDProject) throws Exception {

        MapList mlResult        = new MapList();
        DomainObject doProject  = new DomainObject(sOIDProject);
        Calendar cal            = Calendar.getInstance(TimeZone.getDefault());
        Calendar cCurrent       = Calendar.getInstance();
        int iYearCurrent        = cCurrent.get(Calendar.YEAR);
        int iMonthCurrent       = cCurrent.get(Calendar.MONTH);
        int iWeekCurrent        = cCurrent.get(Calendar.WEEK_OF_YEAR);   
        long lCurrent           = cCurrent.getTimeInMillis();
        long lDiff              = 2592000000L;           
        
        Pattern pTypes          = new Pattern("Task Deliverable");
        StringList busSelects   = new StringList();
        StringList relSelects   = new StringList();
        busSelects.add(DomainObject.SELECT_ID);
        busSelects.add(DomainObject.SELECT_TYPE);
        busSelects.add(DomainObject.SELECT_NAME);
        relSelects.add(DomainObject.SELECT_RELATIONSHIP_ID);
        relSelects.add("from.current");
        relSelects.add(SELECT_FROM_ATTRIBUTE_TASK_EXTIMATED_FINISH_DATE);
        relSelects.add(SELECT_FROM_ATTRIBUTE_TASK_ACTUAL_FINISH_DATE);          // Retrieve more details for dashboards using this function
        relSelects.add(DomainObject.SELECT_FROM_ID);
        relSelects.add("to.id");
        
        if(null == sMode) { sMode = "All"; }
                
        MapList mlDeliverables = doProject.getRelatedObjects(context, "Subtask,Task Deliverable", "Task Management,DOCUMENTS", busSelects, relSelects, false, true, (short) 0, "", "", 0, null, pTypes, null);		
        
        if (mlDeliverables.size() > 0) {

            for (int j = 0; j < mlDeliverables.size(); j++) {

                Map mDeliverable    = (Map) mlDeliverables.get(j);
                String sResultOID   = (String) mDeliverable.get(DomainObject.SELECT_FROM_ID);
                String sCurrent     = (String) mDeliverable.get("from.current");
                String sTargetDate  = (String) mDeliverable.get(SELECT_FROM_ATTRIBUTE_TASK_EXTIMATED_FINISH_DATE);

                mDeliverable.put(DomainObject.SELECT_ID, sResultOID);
                mDeliverable.remove("level");
                mDeliverable.remove("Level");
                               
                Calendar cTarget    = Calendar.getInstance();
                cTarget.setTime(sdf.parse(sTargetDate));
                int iYearTarget     = cTarget.get(Calendar.YEAR);
                int iWeekTarget     = cTarget.get(Calendar.WEEK_OF_YEAR);
                int iMonthTarget    = cTarget.get(Calendar.MONTH);
                         
                
                if (sStates.contains(sCurrent)) {
                    if (sMode.equals("All")) {
                        mlResult.add(mDeliverable);
                    } else if (sMode.equals("Overdue")) {
                            if (sTargetDate != null && !"".equals(sTargetDate)) {
                                if (cTarget.before(cal)) {
                                    mlResult.add(mDeliverable);
                                }
                            }                                 
                    } else if (sMode.equals("This Week")) {
                        if (iYearCurrent == iYearTarget) {
                            if (iWeekCurrent == iWeekTarget) {
                                mlResult.add(mDeliverable);
                            }
                        }
                    } else if (sMode.equals("This Month")) {
                        if (iYearCurrent == iYearTarget) {
                            if (iMonthCurrent == iMonthTarget) {
                                mlResult.add(mDeliverable);
                            }
                        }
                    } else if (sMode.equals("Soon")) {
                        long lTarget        = cTarget.getTimeInMillis();
                        if ((lTarget - lCurrent) < lDiff) {
                            if ((lTarget - lCurrent) > 0) {
                                mlResult.add(mDeliverable);
                            }	                    
                        }	    
                    } 
                    
                }
            }
        }
        
        return mlResult;

    }    

    // - Issues   
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getPendingIssues(Context context, String[] args) throws Exception {


        HashMap paramMap    = (HashMap) JPO.unpackArgs(args);
        String sOID         = (String) paramMap.get("objectId"); 
        String sMode        = (String) paramMap.get("mode"); 
        MapList mlResult    = new MapList();
        String[] init       = new String[]{};        
        HashMap argsMap     = new HashMap();
        String[] methodargs;
        MapList mlProjects;       
        Set resultSet = new HashSet();
        
        if(null == sOID || "".equals(sOID)) {
            argsMap.put("objectId", "");
            methodargs = JPO.packArgs(argsMap);
            mlProjects = (com.matrixone.apps.domain.util.MapList) JPO.invoke(context, "emxProjectSpace", init, "getActiveProjects", methodargs, com.matrixone.apps.domain.util.MapList.class);
        }  else {
            argsMap.put("objectId", sOID);
            methodargs = JPO.packArgs(argsMap);
            mlProjects = (com.matrixone.apps.domain.util.MapList)JPO.invoke(context, "emxProgramUI", init, "getProjectsOfProgram", methodargs, com.matrixone.apps.domain.util.MapList.class);	            
        }
 
        // sort the projects by the ones that are due sooner
        mlProjects.sort(SELECT_ATTRIBUTE_TASK_ESTIMATED_END_DATE, "ascending", "date");
	    
        // get the number of charts we want to display and only display data for that number of charts
        String strNumberOfProjects =  EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ProjectSummary.ChartsToDisplay");
        int numberOfProjects = Integer.parseInt(strNumberOfProjects);
        if ( mlProjects.size() < numberOfProjects ) {
        	numberOfProjects =  mlProjects.size();
        }
        
        MapList tempMapList = new MapList();;
	    for (int i=0; i<numberOfProjects;i++) { 
		    Map projectMap = (Map) mlProjects.get(i);
		    tempMapList.add(projectMap);
	    }
    
        // set the project list to number of projects set by ChartsToDisplay
	    mlProjects = tempMapList;
        if (mlProjects.size() > 0) {
            for (int i = 0; i < mlProjects.size(); i++) {
                Map mProject = (Map) mlProjects.get(i);
                String sOIDProject = (String) mProject.get(DomainObject.SELECT_ID);                
                MapList mlTemp = retrievePendingIssuesOfProject(context, args, sMode, sOIDProject);
                resultSet.addAll(mlTemp);      
            }
        }
        mlResult.addAll(resultSet);

        return mlResult;

    }    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getPendingIssuesOfProject(Context context, String[] args) throws Exception {
        HashMap paramMap    = (HashMap) JPO.unpackArgs(args);
        String sOID         = (String) paramMap.get("objectId");
        String sMode        = (String) paramMap.get("mode");
        return retrievePendingIssuesOfProject(context, args, sMode, sOID);          
    }     
    public MapList retrievePendingIssuesOfProject(Context context, String[] args, String sMode, String sOIDProject) throws Exception {

        
        MapList mlResult        = new MapList();
        Calendar cal            = Calendar.getInstance(TimeZone.getDefault());
        Calendar cCurrent       = Calendar.getInstance();
        int iYearCurrent        = cCurrent.get(Calendar.YEAR);
        int iMonthCurrent       = cCurrent.get(Calendar.MONTH);
        int iWeekCurrent        = cCurrent.get(Calendar.WEEK_OF_YEAR);   
        long lCurrent           = cCurrent.getTimeInMillis();
        long lDiff              = 2592000000L;            
        
        StringList busSelectsIssues = new StringList();
        busSelectsIssues.add(SELECT_ESTIMATED_END_DATE);
        busSelectsIssues.add(SELECT_ATTRIBUTE_ACTUAL_END_DATE);
        busSelectsIssues.add(DomainObject.SELECT_CURRENT);
        busSelectsIssues.add(DomainObject.SELECT_ID);     
        
        DomainObject doProject = new DomainObject(sOIDProject);		
        MapList mlIssuesConnected = doProject.getRelatedObjects(context, "Issue", "Issue", busSelectsIssues, null, true, false, (short) 1, "", "", 0 );
                
        if (mlIssuesConnected.size() > 0) {

            for (int j = 0; j < mlIssuesConnected.size(); j++) {

                Map mIssue = (Map) mlIssuesConnected.get(j);
                String sCurrent = (String) mIssue.get(DomainObject.SELECT_CURRENT);

                if (!sCurrent.equals("Closed")) {
                    String sTargetDate = (String) mIssue.get(SELECT_ESTIMATED_END_DATE);
                    if(sMode.equals("All")) {
                                                        mlResult.add(mIssue);
                    } else if (sTargetDate != null && !"".equals(sTargetDate)) {
                        Calendar cTarget = Calendar.getInstance();
                        cTarget.setTime(sdf.parse(sTargetDate));
                        if (sMode.equals("Overdue")) {
                            if (cTarget.before(cal)) {
                                mlResult.add(mIssue);
                            }
                        } else if (sMode.equals("All")) {
                            mlResult.add(mIssue);
                        } else if (sMode.equals("This Week")) {
                            int iYearTarget = cTarget.get(Calendar.YEAR);
                            int iWeekTarget = cTarget.get(Calendar.WEEK_OF_YEAR);
                            if (iYearCurrent == iYearTarget) {
                                if (iWeekCurrent == iWeekTarget) {
                                    mlResult.add(mIssue);
                                }
                            }
                        } else if (sMode.equals("This Month")) {
                            int iYearTarget = cTarget.get(Calendar.YEAR);
                            int iMonthTarget = cTarget.get(Calendar.MONTH);
                            if (iYearCurrent == iYearTarget) {
                                if (iMonthCurrent == iMonthTarget) {
                                    mlResult.add(mIssue);
                                }
                            }
                        } else if (sMode.equals("Soon")) {
                            long lTarget        = cTarget.getTimeInMillis();
                            if ((lTarget - lCurrent) < lDiff) {
                                if ((lTarget - lCurrent) > 0) {
                                    mlResult.add(mIssue);
                                }	                    
                            }	    
                        } 
                    }
                }
            }
        }
                
                
        return mlResult;
    }

    
    // Program Browser
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getProjectsOfProgram(Context context, String[] args) throws Exception {
        
        HashMap paramMap        = (HashMap) JPO.unpackArgs(args);
        String sOID             = (String) paramMap.get("objectId");
        DomainObject dObject    = new DomainObject(sOID);
        StringList busSelects   = new StringList();
        StringList relSelects   = new StringList();
    	String busWhere ="current!=" + ProjectSpace.STATE_PROJECT_COMPLETE + " && current!=" + ProjectSpace.STATE_PROJECT_ARCHIVE +
    			" && current!=" + ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD + " && current!=" + ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL;
          
        busSelects.add(DomainObject.SELECT_ID);
        busSelects.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_END_DATE);
        relSelects.add(DomainObject.SELECT_RELATIONSHIP_ID);
        
        String relPattern = DomainConstants.RELATIONSHIP_PROGRAM_PROJECT;
        String typePattern = DomainConstants.TYPE_PROJECT_SPACE + "," + DomainConstants.TYPE_PROJECT_CONCEPT;
        
    	return dObject.getRelatedObjects(context, relPattern, typePattern, busSelects, relSelects, false, true, (short)1, busWhere, "", 0);

    }      
    
    // My Dashboard
    public JSONArray getUserDashboardData(Context context, String[] args) throws Exception {
 
        HashMap paramMap        = (HashMap) JPO.unpackArgs(args);
        String sOID             = (String)paramMap.get("objectId");
        String sLanguage        = (String)paramMap.get("languageStr"); 
        Integer[] iCounters     = new Integer[7];
        Integer[] iCountPercent = new Integer[5];
        Integer[] iCountWeek    = new Integer[3];
        int iCountMRU           = 0;
        
        for(int i = 0; i < iCounters.length; i++)       { iCounters[i]      = 0; }        
        for(int i = 0; i < iCountPercent.length; i++)   { iCountPercent[i]  = 0; }        
        for(int i = 0; i < iCountWeek.length; i++)      { iCountWeek[i]     = 0; }        
    
        StringBuilder sbAxisProjects    = new StringBuilder();
        StringBuilder sbDataProjects    = new StringBuilder();
        StringBuilder sbInfo1           = new StringBuilder();
        StringBuilder sbInfo2           = new StringBuilder();
        StringBuilder sbInfo3           = new StringBuilder();
        StringBuilder sbInfo4           = new StringBuilder();        
        StringBuilder sbCountWeek1      = new StringBuilder();
        StringBuilder sbCountWeek2      = new StringBuilder();
        StringBuilder sbCountWeek3      = new StringBuilder();
        StringBuilder sbTimeline1       = new StringBuilder();
        StringBuilder sbTimeline2       = new StringBuilder();
        StringBuilder sbTimeline3       = new StringBuilder();
        
        JSONArray timeLine1Array = new JSONArray();
        JSONArray timeLine2Array = new JSONArray();
        JSONArray timeLine3Array = new JSONArray();
        
        Calendar cNow   = Calendar.getInstance();
        int iNowWeek 	= cNow.get(Calendar.WEEK_OF_YEAR);
        int iNowMonth 	= cNow.get(Calendar.MONTH);
        int iNowYear 	= cNow.get(Calendar.YEAR); 
        Calendar cSoon  = Calendar.getInstance();
        Calendar cMRU   = Calendar.getInstance();
    
        cSoon.add(java.util.GregorianCalendar.DAY_OF_YEAR, +30);
        cMRU.add(java.util.GregorianCalendar.DAY_OF_YEAR, -1);
        
        MapList mlTasksPending = retrieveMyTasksPending(context, args, ProgramCentralConstants.EMPTY_STRING, true);
               
        for(int i = 0; i < mlTasksPending.size(); i++) {
            
            Map mTask           = (Map)mlTasksPending.get(i);            
            String sDate        = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
            String sPercent     = (String)mTask.get(DomainObject.SELECT_PERCENTCOMPLETE);
            String sModified     = (String)mTask.get("modified");
            
            Calendar cModified    = Calendar.getInstance();            
            cModified.setTime(sdf.parse(sModified));  
            if(cModified.after(cMRU)) { iCountMRU++; }
            
            Double dPercent = Task.parseToDouble(sPercent);
            
            if(dPercent      < 25.0)    { iCountPercent[0]++; }
            else if(dPercent < 50.0)    { iCountPercent[1]++; }
            else if(dPercent < 75.0)    { iCountPercent[2]++; }
            else if(dPercent < 100.0)   { iCountPercent[3]++; }
            else                        { iCountPercent[4]++; }       
            
            if(UIUtil.isNullOrEmpty(sDate)){
				continue;
			}
            
            Calendar cTarget    = Calendar.getInstance();
            cTarget.setTime(sdf.parse(sDate));             

            int iWeek 	= cTarget.get(Calendar.WEEK_OF_YEAR);
            int iMonth 	= cTarget.get(Calendar.MONTH);
            int iYear 	= cTarget.get(Calendar.YEAR); 
            
            if(iYear == iNowYear) {
                if(iMonth == iNowMonth) { iCounters[1]++;}
                if(iWeek == iNowWeek) { iCounters[0]++;}
            }
            if(cTarget.before(cNow)) { iCounters[3]++; }
            else if(cTarget.before(cSoon)) { iCounters[2]++;}
            
                 
        }
        
		JSONArray projectsStatusArray = new JSONArray();
        JSONObject range1dataObject = new JSONObject();
        range1dataObject.put("name", "25%");
        range1dataObject.put("y", iCountPercent[0]);
        range1dataObject.put("color", "#CEDFEA");
        range1dataObject.put("value", "0-25");
        
        JSONObject range2dataObject = new JSONObject();
        range2dataObject.put("name", "25-50%");
        range2dataObject.put("y", iCountPercent[1]);
        range2dataObject.put("color", "#88B1CC");
        range2dataObject.put("value", "25-50");
        
        JSONObject range3dataObject = new JSONObject();
        range3dataObject.put("name", "50-75%");
        range3dataObject.put("y", iCountPercent[2]);
        range3dataObject.put("color", "#508CB4");
        range3dataObject.put("value", "50-75");
        
        JSONObject range4dataObject = new JSONObject();
        range4dataObject.put("name", "75-99%");
        range4dataObject.put("y", iCountPercent[3]);
        range4dataObject.put("color", "#2A4B62");
        range4dataObject.put("value", "75-99");
        
        JSONObject range5dataObject = new JSONObject();
        range5dataObject.put("name", "100%");
        range5dataObject.put("y", iCountPercent[4]);
        range5dataObject.put("color", "#ff7f00");
        range5dataObject.put("value", "100");
        
        projectsStatusArray.put(range1dataObject);
        projectsStatusArray.put(range2dataObject);
        projectsStatusArray.put(range3dataObject);
        projectsStatusArray.put(range4dataObject);
        projectsStatusArray.put(range5dataObject);
                
        MapList mlDataProjects              = new MapList();        
        java.util.List<String> lProjects    = new ArrayList<String>();        
        StringList slProject = new StringList();
        slProject.add(DomainObject.SELECT_ID);
        slProject.add(DomainObject.SELECT_NAME);

        for(int i = 0; i < mlTasksPending.size(); i++) {
            
            Map mTask           = (Map)mlTasksPending.get(i);
            //String sOIDTask     = (String)mTask.get(DomainObject.SELECT_ID);
            //Task task           = new Task(sOIDTask);

            try { // The method getpProject may fail if there is no project for a task (happens during import)
            
                //Map mProject        = task.getProject(context, slProject);
    		String sProjectName = (String)mTask.get(ProgramCentralConstants.SELECT_TASK_PROJECT_NAME);       
    		if(ProgramCentralUtil.isNullString(sProjectName)){
            	continue;	
            }    
                if(!lProjects.contains(sProjectName)) {
                    lProjects.add(sProjectName);
    	 	    String sProjectOID = (String)mTask.get(ProgramCentralConstants.SELECT_TASK_PROJECT_ID);   
                    Map mData = new HashMap();
                    mData.put(DomainObject.SELECT_ID, sProjectOID);
                    mData.put(DomainObject.SELECT_NAME, sProjectName);
                    mData.put("count", "1");
                    mlDataProjects.add(mData);
                } else {
                    int iIndex = lProjects.indexOf(sProjectName);
                    Map mData = (Map)mlDataProjects.get(iIndex);
                    String sValue = (String)mData.get("count");
                    int iValue = Integer.parseInt(sValue);
                    iValue++;
                    mData.put("count", String.valueOf(iValue));
                }
            } catch (Exception e) {}
            
        }
 
        mlDataProjects.sort("count", "descending", "integer");        
        
        JSONArray projectsObjectArray = new JSONArray();
        JSONArray labelProjectsArray = new JSONArray();
        for(int i = 0; i < mlDataProjects.size(); i++) {
            Map mData = (Map)mlDataProjects.get(i);
            String sID = (String)mData.get(DomainObject.SELECT_ID);
            String sName = (String)mData.get(DomainObject.SELECT_NAME);
            int iCount = Integer.parseInt((String)mData.get("count"));
            labelProjectsArray.put(sName);
            JSONObject dataObject = new JSONObject();
            dataObject.put("name", sName);
            dataObject.put("y", iCount);
            dataObject.put("color", "#"+sColors[i%sColors.length]);
            dataObject.put("id", sID);
            projectsObjectArray.put(dataObject);
        }        
        
        // Task Timeline data        
        for(int i = 0; i < mlTasksPending.size(); i++) {
        	
            Map mTask               = (Map)mlTasksPending.get(i);            
            String sDateModified    = (String)mTask.get("modified");
            String sDateAssigned    = (String)mTask.get("state[Assign].start");
            String sDateInReview    = (String)mTask.get("state[Review].start");
            Calendar cModified      = Calendar.getInstance();            
            cModified.setTime(sdf.parse(sDateModified)); 
            if(cModified.get(Calendar.YEAR) == iNowYear) {
                if(cModified.get(Calendar.WEEK_OF_YEAR) == iNowWeek) {    
                    iCountWeek[1]++;
                }
            }            
                        
            if(!"".equals(sDateAssigned)) {
                Calendar cAssigned = Calendar.getInstance();    
                cAssigned.setTime(sdf.parse(sDateAssigned));  
                if(cAssigned.get(Calendar.YEAR) == iNowYear) {
                    if(cAssigned.get(Calendar.WEEK_OF_YEAR) == iNowWeek) {    
                        iCountWeek[0]++;
                    }
                }
            }            
                        
            if(ProgramCentralUtil.isNotNullString(sDateInReview)) {
                Calendar cInReview = Calendar.getInstance();
                cInReview.setTime(sdf.parse(sDateInReview));
                if(cInReview.get(Calendar.YEAR) == iNowYear) {
                    if(cInReview.get(Calendar.WEEK_OF_YEAR) == iNowWeek) {    
                        iCountWeek[2]++;
                    }
                }                 
            }            
        }        
                
        mlTasksPending.sort(Task.SELECT_TASK_ESTIMATED_FINISH_DATE, "ascending", "date");
        
        if(mlTasksPending.size() > 0) {
        
            Map mTask                   = (Map)mlTasksPending.get(0);
            String sCurrent             = (String)mTask.get(DomainObject.SELECT_CURRENT);
            String sDateTimePrevious    = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
            String sDatePrevious        = ProgramCentralConstants.EMPTY_STRING;
            if(UIUtil.isNotNullAndNotEmpty(sDateTimePrevious)){
            	sDatePrevious        = sDateTimePrevious.substring(0, sDateTimePrevious.indexOf(" "));
			}
            
            int iCountAssign            = 0;
            int iCountActive            = 0;
            int iCountReview            = 0;
            
            for(int i = 0; i < mlTasksPending.size(); i++) {
                mTask = (Map)mlTasksPending.get(i);
                sCurrent = (String)mTask.get(DomainObject.SELECT_CURRENT);
                String sDateTime = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                
                if(UIUtil.isNullOrEmpty(sDateTime)){
					continue;
				}
                
                if(UIUtil.isNullOrEmpty(sDatePrevious)){
                	sDateTimePrevious = sDateTime;
                	sDatePrevious = sDateTime.substring(0, sDateTime.indexOf(" "));
				}
                
                String sDate = sDateTime.substring(0, sDateTime.indexOf(" "));
                if(sDate.equals(sDatePrevious)) { 
                    if(sCurrent.equals("Assign"))       { iCountAssign++; }
                    else if(sCurrent.equals("Active"))  { iCountActive++; }
                    else if(sCurrent.equals("Review"))  { iCountReview++; }
                } else {
                    Calendar cCreation = Calendar.getInstance();
                    cCreation.setTime(sdf.parse(sDateTimePrevious)); 
                  
                    JSONArray assignedObjArr = new JSONArray();
                    assignedObjArr.put(cCreation.getTimeInMillis());
                    assignedObjArr.put(iCountAssign);
                    timeLine1Array.put(assignedObjArr);
                    
                    JSONArray activeObjArr = new JSONArray();
                    activeObjArr.put(cCreation.getTimeInMillis());
                    activeObjArr.put(iCountActive);
                    timeLine2Array.put(activeObjArr);
                    
                    JSONArray reviewObjArr = new JSONArray();
                    reviewObjArr.put(cCreation.getTimeInMillis());
                    reviewObjArr.put(iCountReview);
                    timeLine3Array.put(reviewObjArr);

                    iCountAssign = 0;  iCountActive = 0; iCountReview = 0;
            
                    if(sCurrent.equals("Assign"))       { iCountAssign++; }
                    else if(sCurrent.equals("Active"))  { iCountActive++; }
                    else if(sCurrent.equals("Review"))  { iCountReview++; }
            
                    sDateTimePrevious = sDateTime;
                    sDatePrevious = sDate;
                }
                if (i == (mlTasksPending.size() - 1)) { 			
                    Calendar cCreation = Calendar.getInstance();
                    cCreation.setTime(sdf.parse(sDateTime)); 
				
                    JSONArray assignedObjArr = new JSONArray();
                    assignedObjArr.put(cCreation.getTimeInMillis());
                    assignedObjArr.put(iCountAssign);
                    timeLine1Array.put(assignedObjArr);
                    
                    JSONArray activeObjArr = new JSONArray();
                    activeObjArr.put(cCreation.getTimeInMillis());
                    activeObjArr.put(iCountActive);
                    timeLine2Array.put(activeObjArr);
                    
                    JSONArray reviewObjArr = new JSONArray();
                    reviewObjArr.put(cCreation.getTimeInMillis());
                    reviewObjArr.put(iCountReview);
                    timeLine3Array.put(reviewObjArr);		
                }                    
            }            
        }

        String assignLabel = EnoviaResourceBundle.getStateI18NString(context, "Project Task", "Assign", sLanguage);
        String activeLabel = EnoviaResourceBundle.getStateI18NString(context, "Project Task", "Active", sLanguage);
        String reviewLabel = EnoviaResourceBundle.getStateI18NString(context, "Project Task", "Review", sLanguage);
        
        JSONObject timeLineAssigObj = new JSONObject();
        timeLineAssigObj.put("color", "#cc0000");
        timeLineAssigObj.put("name", assignLabel);
        timeLineAssigObj.put("data", timeLine1Array);
        
        JSONObject timeLineActiveObject = new JSONObject();
        timeLineActiveObject.put("color", "#009c00");
        timeLineActiveObject.put("name", activeLabel);
        timeLineActiveObject.put("data", timeLine2Array);
        
        JSONObject timeLineReviewObject = new JSONObject();
        timeLineReviewObject.put("color", "#ff7f00");
        timeLineReviewObject.put("name", reviewLabel);
        timeLineReviewObject.put("data", timeLine3Array);
        
        JSONArray seriesMyPendingTasks = new JSONArray();
        seriesMyPendingTasks.put(timeLineAssigObj);
        seriesMyPendingTasks.put(timeLineActiveObject);
        seriesMyPendingTasks.put(timeLineReviewObject);
        
        int iHeightProjects	= 35 + (mlDataProjects.size() * 20);  
        
        // Info Links for My Pending Tasks By Project
        String sInfoPrefix      = " <a onclick='openURLInDetails(\"../common/emxIndentedTable.jsp?suiteKey=ProgramCentral&table=PMCAssignedWBSTaskSummary&hideWeeklyEfforts=true&freezePane=Status,WBSTaskName,Delivarable,NewWindow&editLink=true&selection=multiple&sortColumnName=Modified&sortDirection=decending&program=emxProgramUI:";
        sbInfo1.append("<b>").append(iCounters[0]).append("</b>").append(sInfoPrefix).append("getAssignedTasksPending&mode=Week&header=emxProgramCentral.String.MyPendingTasksDueThisWeek\")'>").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.ThisWeek" , sLanguage)).append("</a>");
        sbInfo2.append("<b>").append(iCounters[1]).append("</b>").append(sInfoPrefix).append("getAssignedTasksPending&mode=Month&header=emxProgramCentral.String.MyPendingTasksDueThisMonth\")'>").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.ThisMonth" , sLanguage)).append("</a>");
        sbInfo3.append("<b>").append(iCounters[2]).append("</b>").append(sInfoPrefix).append("getAssignedTasksPending&mode=Soon&header=emxProgramCentral.String.MyPendingTasksDueSoon\")'>").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.Soon" , sLanguage)).append("</a>");
        sbInfo4.append("<b>").append(iCounters[3]).append("</b>").append(sInfoPrefix).append("getAssignedTasksPending&mode=Overdue&header=emxProgramCentral.String.MyPendingTasksOverdue\")'>").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.Overdue" , sLanguage)).append("</a>");
        
        // Info Links for My Pending Tasks By Date
        sbCountWeek1.append("<b>").append(iCountWeek[0]).append("</b>").append(sInfoPrefix).append("getAssignedTasksPending&mode=NEW&header=emxProgramCentral.String.MyPendingTasksCreatedThisWeek\")'>").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.NEW", sLanguage)).append("</a>");
        sbCountWeek2.append("<b>").append(iCountWeek[1]).append("</b>").append(sInfoPrefix).append("getAssignedTasksPending&mode=MOD&header=emxProgramCentral.String.MyPendingTasksModifiedThisWeek\")'>").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.MODIFIED", sLanguage)).append("</a>");
        sbCountWeek3.append("<b>").append(iCountWeek[2]).append("</b>").append(sInfoPrefix).append("getAssignedTasksPending&mode=REV&header=emxProgramCentral.String.MyPendingTasksToReviewThisWeek\")'>").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.SetToRREVIEW", sLanguage)).append("</a>");
        
        // Dashboard Counters
        StringBuilder sbCounter = new StringBuilder();        
        sbCounter.append("<td onclick='openURLInDetails(\"../common/emxIndentedTable.jsp?table=PMCAssignedWBSTaskSummary&hideWeeklyEfforts=true&program=emxProgramUI:getAssignedTasksPending&header=emxProgramCentral.String.MyPendingTasks&freezePane=Status,WBSTaskName,Delivarable,NewWindow&suiteKey=ProgramCentral\")'");
        sbCounter.append(" class='counterCell ");
        if(mlTasksPending.size() == 0)  { sbCounter.append("grayBright"); }
        else                            { sbCounter.append("greenBright");  }
        sbCounter.append("'><span class='counterText greenBright'>").append(mlTasksPending.size()).append("</span><br/>");
        sbCounter.append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.ProjectTasks", sLanguage)).append("</td>");         
        
        StringBuilder sbUpdates = new StringBuilder();
        sbUpdates.append("<td ");
        if(iCountMRU > 0) {
            sbUpdates.append(" onclick='openURLInDetails(\"../common/emxIndentedTable.jsp?table=PMCAssignedWBSTaskSummary&hideWeeklyEfforts=true&program=emxProgramUI:getAssignedTasksPending&mode=MRU&header=emxProgramCentral.String.MRUTasks&freezePane=Status,WBSTaskName,Delivarable,NewWindow&suiteKey=ProgramCentral\")' ");
            sbUpdates.append(" class='mruCell'><span style='color:#000000;font-weight:bold;'>").append(iCountMRU).append("</span> <span class='counterTextMRU'>");            
            if(iCountMRU == 1) { sbUpdates.append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.MostRecentUpdates"  , sLanguage)); }
            else               { sbUpdates.append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.MostRecentUpdates" , sLanguage)); }
            sbUpdates.append("</span>");            
        } else {
            sbUpdates.append(">");    
        }
        sbUpdates.append("</td>");
       
        JSONObject taskPendingObjectLink = new JSONObject();
        taskPendingObjectLink.put("taskPendingThisWeek", sbInfo1.toString());
      	taskPendingObjectLink.put("taskPendingThisMonth", sbInfo2.toString());
      	taskPendingObjectLink.put("taskPendingSoon", sbInfo3.toString());
      	taskPendingObjectLink.put("taskPendingOverDue", sbInfo4.toString());
      	 
        String sLabelMyPendingTasks = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.MyPendingTasksByProject", sLanguage);
 	   	JSONObject projectItemObj = new JSONObject();       
 	    projectItemObj.put("name", sLabelMyPendingTasks);
 	    projectItemObj.put("data", projectsObjectArray);
 	   	JSONArray seriesProjectsFinal = new JSONArray();
 	   	seriesProjectsFinal.put(projectItemObj);
        
        JSONObject widgetItem1 = new JSONObject();        
        widgetItem1.put("label", sLabelMyPendingTasks);
        widgetItem1.put("series", seriesProjectsFinal);
        widgetItem1.put("name", "Projects");
        widgetItem1.put("type", "bar");
        widgetItem1.put("height", String.valueOf(iHeightProjects));        
        widgetItem1.put("xAxisCategories", labelProjectsArray);        
        widgetItem1.put("view", "expanded");
        widgetItem1.put("bottomLineData", taskPendingObjectLink);
        widgetItem1.put("filterable", false);     
       
        String sLabelStatus = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Attribute.Percent_Complete", sLanguage);

        JSONObject statusItemObj = new JSONObject();       
        statusItemObj.put("name", sLabelStatus);
        statusItemObj.put("data", projectsStatusArray);
 	   	JSONArray seriesStatusFinal = new JSONArray();
 	    seriesStatusFinal.put(statusItemObj);
 	   	
        JSONObject widgetItem2 = new JSONObject();        
        widgetItem2.put("label", sLabelStatus);
        widgetItem2.put("series", seriesStatusFinal);
        widgetItem2.put("name", "Status");
        widgetItem2.put("type", "pie");    
        widgetItem2.put("view", "expanded");
        widgetItem2.put("filterable", true);
        widgetItem2.put("height",190);
        // add url
        String sLabelPendingTasksByDate = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.MyPendingTasksByDate", sLanguage);
        String sLabelThisWeek = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.ThisWeek", sLanguage);
        
        JSONObject myPendingTaskByDateObjectLink = new JSONObject();
        myPendingTaskByDateObjectLink.put("taskPendingThisWeek", sLabelThisWeek);
        myPendingTaskByDateObjectLink.put("taskPendingThisMonth", sbCountWeek1.toString());
        myPendingTaskByDateObjectLink.put("taskPendingSoon", sbCountWeek2.toString());
        myPendingTaskByDateObjectLink.put("taskPendingOverDue", sbCountWeek3.toString());
      	
        JSONObject widgetItem3 = new JSONObject();        
        widgetItem3.put("label", sLabelPendingTasksByDate);
        widgetItem3.put("series", seriesMyPendingTasks);
        widgetItem3.put("name", "Tasks");
        widgetItem3.put("type", "spline");
        widgetItem3.put("view", "expanded");
        widgetItem3.put("bottomLineData", myPendingTaskByDateObjectLink);
        widgetItem3.put("filterable", true);
        // I am addin links to widget3, we can add this is any of widget but have to keep track.
        widgetItem3.put("counterLink", sbCounter.toString());
        widgetItem3.put("updateLink", sbUpdates.toString());
        
        JSONArray widgetArr = new JSONArray();
        widgetArr.put(widgetItem1);
        widgetArr.put(widgetItem2);
        widgetArr.put(widgetItem3);
        return widgetArr;
    }
    public Vector columnFolderContext(Context context, String[] args) throws Exception {


        HashMap programMap      = (HashMap) JPO.unpackArgs (args);
        MapList mlObjects       = (MapList) programMap.get ("objectList");
        Vector vResult          = new Vector ();
        String sDimensions      = EnoviaResourceBundle.getProperty(context, "emxFramework.PopupSize.Large");            
        String[] aDimensions    = sDimensions.split("x");

        StringList busSelects = new StringList();
        busSelects.add(DomainObject.SELECT_ID);
        busSelects.add(DomainObject.SELECT_TYPE);   
        busSelects.add(DomainObject.SELECT_NAME);   
        busSelects.add("originated");   
        
        Pattern pTypes = new Pattern("Project Space");
        pTypes.addPattern("Workspace");        
        
        for (int i = 0; i < mlObjects.size (); i++) {            
            
            Map mObject             = (Map) mlObjects.get (i);
            String sOID             = (String) mObject.get (DomainObject.SELECT_ID);
            DomainObject dObject    = new DomainObject (sOID);  
            StringBuilder sbResult  = new StringBuilder();
            
            MapList mlProjects = dObject.getRelatedObjects (context, "Sub Vaults,Data Vaults,Vaulted Objects,Vaulted Documents Rev2", "Project Space,Workspace Vault,Workspace", busSelects, null, true, false, (short)0, "", "", 0, pTypes, null, null);
            
            if(mlProjects.size() > 0) {
                
                mlProjects.sort("originated", "ascending", "date");
                
                Map mProject    = (Map)mlProjects.get(0);
                String sId      = (String)mProject.get(DomainObject.SELECT_ID);
                String sName    = (String)mProject.get(DomainObject.SELECT_NAME);
                String sType    = (String)mProject.get(DomainObject.SELECT_TYPE);
                String sIcon    = UINavigatorUtil.getTypeIconProperty(context, sType);
                
                sbResult.append("<span style='white-space:nowrap;'><img src=\"../common/images/").append(sIcon).append("\" /> <a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=").append(sId).append("', '950', '650', 'false', 'popup', '')\" class=\"object\">").append(sName).append("</a></span>");
                
            }
            if(mlProjects.size() > 1) {
                sbResult.append(" ");              
                sbResult.append("<a style='font-weight:normal !important;' href='#' onClick=\"");
                sbResult.append("emxTableColumnLinkClick('../common/emxTree.jsp?DefaultCategory=APPWhereUsed&amp;objectId=").append(sOID);
                sbResult.append("', 'popup', '', '").append(aDimensions[0]).append("', '").append(aDimensions[1]).append("', '')\">");                    
                sbResult.append("(more)");
                sbResult.append("</a>");
            }
            
            vResult.add(sbResult.toString());
                   
        }       
        
        return vResult;
    }  
    public Vector columnDocumentPath(Context context, String[] args) throws Exception {


        HashMap programMap      = (HashMap) JPO.unpackArgs (args);
        MapList mlObjects       = (MapList) programMap.get ("objectList");
        Vector vResult          = new Vector (mlObjects.size ());
        String sOID             = "";
        String sDimensions      = EnoviaResourceBundle.getProperty(context, "emxFramework.PopupSize.Large");            
        String[] aDimensions    = sDimensions.split("x");

        StringList busSelects = new StringList();
        StringList relSelects = new StringList();
        busSelects.add(DomainObject.SELECT_ID);
        busSelects.add(DomainObject.SELECT_NAME);
        relSelects.add("originated");
        
        for (int i = 0; i < mlObjects.size (); i++) {            
            
            StringBuilder sbResult  = new StringBuilder();
            Map mObject             = (Map) mlObjects.get (i);
            sOID                    = (String) mObject.get (DomainObject.SELECT_ID);
            DomainObject doDocument = new DomainObject(sOID);
            MapList mlFolders       = doDocument.getRelatedObjects(context, "Sub Vaults,Vaulted Objects,Vaulted Documents Rev2", "Workspace Vault", busSelects, relSelects, true, false, (short)0, "", "", 0);
            Boolean bMore           = false;
            
            if(mlFolders.size() > 0) {
            
                mlFolders.sortStructure("originated", "ascending", "date");
                int iLevelPrev = 0;
                
                for(int j = 0; j < mlFolders.size(); j++) {
                    
                    Map mFolder         = (Map)mlFolders.get(j);
                    String sLevel       = (String)mFolder.get("level");
                    String sOIDFolder   = (String)mFolder.get(DomainObject.SELECT_ID);
                    String sName        = (String)mFolder.get(DomainObject.SELECT_NAME);
                    int iLevel          = Integer.parseInt(sLevel);
                    
                    if(iLevel <= iLevelPrev) {
                        bMore = true;
                        break;                        
                    } 
                    if(j > 0) { sbResult.insert(0, " \\ "); }
                                        
                    sbResult.insert(0, "</a>");
                    sbResult.insert(0, sName);
                    sbResult.insert(0, "<img src = '../common/images/iconSmallFolder.gif' /> ");
                    sbResult.insert(0, "', 'popup', '', '" + aDimensions[0] + "', '" + aDimensions[1] + "', '');\">");
                    sbResult.insert(0, sOIDFolder);
                    sbResult.insert(0, "<a href='#' onClick=\"emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=programcentral&amp;suiteKey=ProgramCentral&amp;objectId=");
                    
                    iLevelPrev = iLevel;
                }
                         
                if(bMore) {
                    sbResult.append(" ");              
                    sbResult.append("<a style='font-weight:normal !important;' href='#' onClick=\"");
                    sbResult.append("emxTableColumnLinkClick('../common/emxTree.jsp?DefaultCategory=APPWhereUsed&amp;objectId=").append(sOID).append("', 'popup', '', '" + aDimensions[0] + "', '" + aDimensions[1] + "', '')\">");                    
                    sbResult.append("(more)");
                    sbResult.append("</a>");
                }
                
            }
            
            vResult.addElement (sbResult.toString());
        }
        return vResult;
    } 
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getMyOpenTasks(Context context, String[] args) throws Exception {   // My Pending Tasks Tab
        return retrieveMyTasksPending(context, args, "", false);
    }        
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAssignedTasksPending(Context context, String[] args) throws Exception {
        
        MapList mlResults       = new MapList();
        HashMap paramMap        = (HashMap) JPO.unpackArgs(args);
        String sOID             = (String) paramMap.get("objectId");
        String sMode            = (String) paramMap.get("mode");
        StringBuilder sbWhere   = new StringBuilder();
        Calendar cal            = Calendar.getInstance(TimeZone.getDefault());
        
        if(null == sMode) { sMode = ""; }
        
        if(sMode.equals("MRU")) {      
            
            cal.add(java.util.GregorianCalendar.DAY_OF_YEAR, -1);

            String sMinute  = String.valueOf(cal.get(Calendar.MINUTE));
            String sSecond  = String.valueOf(cal.get(Calendar.SECOND));
            String sAMPM    = (cal.get(Calendar.AM_PM) == 0 ) ? "AM" : "PM";

            if(sMinute.length() == 1) { sMinute = "0" + sMinute; }
            if(sSecond.length() == 1) { sSecond = "0" + sSecond; }        

            sbWhere.append(" && (modified >= \"");
            sbWhere.append(cal.get(Calendar.MONTH) + 1).append("/").append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(cal.get(Calendar.YEAR));
            sbWhere.append(" ").append(cal.get(Calendar.HOUR)).append(":").append(sMinute).append(":").append(sSecond).append(" ").append(sAMPM);        
            sbWhere.append("\")");   

            return retrieveMyTasksPending(context, args, sbWhere.toString(), true);                    
            
        } else if(sMode.equals("Week")) { 
            
            Calendar cNow       = Calendar.getInstance();
            int iNowWeek        = cNow.get(Calendar.WEEK_OF_YEAR);
            int iNowYear        = cNow.get(Calendar.YEAR);               
            MapList mlTasks     = retrieveMyTasksPending(context, args, "", true);

            for(int i = 0; i < mlTasks.size(); i++) {

                Map mTask           = (Map)mlTasks.get(i);
                String sDate        = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                Calendar cTarget    = Calendar.getInstance();

                cTarget.setTime(sdf.parse(sDate)); 

                int iWeek 	= cTarget.get(Calendar.WEEK_OF_YEAR);
                int iYear 	= cTarget.get(Calendar.YEAR); 

                if(iYear == iNowYear) {
                    if(iWeek == iNowWeek) {
                        mlResults.add(mTask);
                    }
                }

            }             
            
        } else if(sMode.equals("Month")) {     
            
            Calendar cNow = Calendar.getInstance();
            int iNowMonth 	= cNow.get(Calendar.MONTH);
            int iNowYear 	= cNow.get(Calendar.YEAR); 

            MapList mlTasks = retrieveMyTasksPending(context, args, "", true);

            for(int i = 0; i < mlTasks.size(); i++) {
                Map mTask = (Map)mlTasks.get(i);
                String sDate = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                Calendar cTarget = Calendar.getInstance();
                cTarget.setTime(sdf.parse(sDate)); 
                int iMonth 	= cTarget.get(Calendar.MONTH);
                int iYear 	= cTarget.get(Calendar.YEAR); 

                if(iYear == iNowYear) {
                    if(iMonth == iNowMonth) {
                        mlResults.add(mTask);
                    }
                }

            }            
            
        } else if(sMode.equals("Soon")) {  
            
            Calendar cTomorrow   = Calendar.getInstance();
            Calendar cSoon  = Calendar.getInstance();

            cSoon.add(java.util.GregorianCalendar.DAY_OF_YEAR, +30);

            sbWhere.append(" && ");
            sbWhere.append("(attribute[Task Estimated Finish Date] >= '").append(cTomorrow.get(Calendar.MONTH) + 1).append("/").append(cTomorrow.get(Calendar.DAY_OF_MONTH)).append("/").append(cTomorrow.get(Calendar.YEAR)).append("')");
            sbWhere.append(" && ");
            sbWhere.append("(attribute[Task Estimated Finish Date] <= '").append(cSoon.get(Calendar.MONTH) + 1).append("/").append(cSoon.get(Calendar.DAY_OF_MONTH)).append("/").append(cSoon.get(Calendar.YEAR)).append("')");

            mlResults = retrieveMyTasksPending(context, args, sbWhere.toString(), true);            
            
        } else if(sMode.equals("Overdue")) {  
            
            sbWhere.append(" && (attribute[Task Estimated Finish Date] < '");
            sbWhere.append(cal.get(Calendar.MONTH) + 1).append("/").append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(cal.get(Calendar.YEAR)).append(" 00:00:00 AM").append("')");
        
            mlResults = retrieveMyTasksPending(context, args, sbWhere.toString(), true);            
            
        } else if(sMode.equals("By Project")) {           
            
            String sUser        = context.getUser();  
            MapList mlTasks     = retrieveOpenTasksOfProject(context, args, "All", sOID);          

            StringList busSelects = new StringList();
            busSelects.add(DomainObject.SELECT_NAME);

            for (int i = 0; i < mlTasks.size(); i++) {
                Map mTask = (Map)mlTasks.get(i);
                    String sOIDTask = (String)mTask.get(DomainObject.SELECT_ID);
                    DomainObject doTask = new DomainObject(sOIDTask);
                    MapList mlAssignees = doTask.getRelatedObjects(context, DomainConstants.RELATIONSHIP_ASSIGNED_TASKS, "Person", busSelects, null, true, false, (short)1, "name == '" + sUser + "'", "", 0);
                    if(mlAssignees.size() > 0) {
                        mlResults.add(mTask);    
                    }
            }
            
        } else if(sMode.equals("By Percent Complete")) {
            
            String sPercent     = (String) paramMap.get("percent");        
            String sValueMin    = "100.0";
            String sValueMax    = "1000.0";

            if(sPercent.equals("0-25"))       { sValueMin =  "0.0"; sValueMax =  "25.0"; }
            else if(sPercent.equals("25-50")) { sValueMin = "25.0"; sValueMax =  "50.0"; }
            else if(sPercent.equals("50-75")) { sValueMin = "50.0"; sValueMax =  "75.0"; }
            else if(sPercent.equals("75-99")) { sValueMin = "75.0"; sValueMax = "100.0"; }
            
            sbWhere.append(" && ").append("(attribute[Percent Complete] >= '").append(sValueMin).append("')");
            sbWhere.append(" && ").append("(attribute[Percent Complete] <  '").append(sValueMax).append("')");

            mlResults = retrieveMyTasksPending(context, args, sbWhere.toString(), true);
        
        } else if(sMode.equals("By Date")) {            
            
            String sDate        = (String) paramMap.get("date");        
            Calendar cStart     = Calendar.getInstance();
            Calendar cEnd       = Calendar.getInstance();       

            long lDate = Long.parseLong(sDate);
            cStart.setTimeInMillis(lDate);
            cEnd.setTimeInMillis(lDate);
            cEnd.add(java.util.GregorianCalendar.DAY_OF_YEAR, 1);

            sbWhere.append(" && ");        
            sbWhere.append("(current != 'Create')");
            sbWhere.append(" && ");
            sbWhere.append("(attribute[Task Estimated Finish Date] >= '").append(cStart.get(Calendar.MONTH) + 1).append("/").append(cStart.get(Calendar.DAY_OF_MONTH)).append("/").append(cStart.get(Calendar.YEAR)).append("')");
            sbWhere.append(" && ");
            sbWhere.append("(attribute[Task Estimated Finish Date] <= '").append(cEnd.get(Calendar.MONTH) + 1).append("/").append(cEnd.get(Calendar.DAY_OF_MONTH)).append("/").append(cEnd.get(Calendar.YEAR)).append("')");        

            mlResults = retrieveMyTasksPending(context, args, sbWhere.toString(), true);
        
        } else if(sMode.equals("NEW")) {  
            
            Calendar cNow       = Calendar.getInstance();
            int iNowYear        = cNow.get(Calendar.YEAR);
            int iNowWeek        = cNow.get(Calendar.WEEK_OF_YEAR); 

            cNow.add(java.util.GregorianCalendar.DAY_OF_YEAR,-8);
            sbWhere.append(" && (state[Assign].start > '");
            sbWhere.append(cNow.get(Calendar.MONTH) + 1).append("/").append(cNow.get(Calendar.DAY_OF_MONTH)).append("/").append(cNow.get(Calendar.YEAR)).append("')");

            MapList mlTasks = retrieveMyTasksPending(context, args, sbWhere.toString(), true);

            for(int i = 0; i < mlTasks.size(); i++) {

                Map mTask           = (Map)mlTasks.get(i);
                String sDate        = (String)mTask.get("state[Assign].start");
                Calendar cTarget    = Calendar.getInstance();
                        
                cTarget.setTime(sdf.parse(sDate)); 
                
                int iWeek 	= cTarget.get(Calendar.WEEK_OF_YEAR);
                int iYear 	= cTarget.get(Calendar.YEAR); 

                if(iYear == iNowYear) {
                    if(iWeek == iNowWeek) {
                        mlResults.add(mTask);
                    }
                }

            }                  
            
        } else if(sMode.equals("MOD")) { 
            
            Calendar cNow   = Calendar.getInstance();
            int iNowYear    = cNow.get(Calendar.YEAR);
            int iNowWeek    = cNow.get(Calendar.WEEK_OF_YEAR); 

            cNow.add(java.util.GregorianCalendar.DAY_OF_YEAR,-8);
            sbWhere.append(" && (modified > '");
            sbWhere.append(cNow.get(Calendar.MONTH) + 1).append("/").append(cNow.get(Calendar.DAY_OF_MONTH)).append("/").append(cNow.get(Calendar.YEAR)).append("')");

            MapList mlTasks = retrieveMyTasksPending(context, args, sbWhere.toString(), true);                      

            for(int i = 0; i < mlTasks.size(); i++) {
                Map mTask = (Map)mlTasks.get(i);
                String sDate = (String)mTask.get("modified");
                Calendar cTarget = Calendar.getInstance();
                cTarget.setTime(sdf.parse(sDate)); 
                int iWeek 	= cTarget.get(Calendar.WEEK_OF_YEAR);
                int iYear 	= cTarget.get(Calendar.YEAR); 

                if(iYear == iNowYear) {
                    if(iWeek == iNowWeek) {
                        mlResults.add(mTask);
                    }
                }

            }                           
            
        } else if(sMode.equals("REV")) {  

            Calendar cNow   = Calendar.getInstance();
            int iNowYear    = cNow.get(Calendar.YEAR);
            int iNowWeek    = cNow.get(Calendar.WEEK_OF_YEAR); 

            cNow.add(java.util.GregorianCalendar.DAY_OF_YEAR,-8);            
            sbWhere.append(" && (state[Review].start > '");
            sbWhere.append(cNow.get(Calendar.MONTH) + 1).append("/").append(cNow.get(Calendar.DAY_OF_MONTH)).append("/").append(cNow.get(Calendar.YEAR)).append("')");

            MapList mlTasks = retrieveMyTasksPending(context, args, sbWhere.toString(), true);                      

            for(int i = 0; i < mlTasks.size(); i++) {
                Map mTask = (Map)mlTasks.get(i);
                String sDate = (String)mTask.get("state[Review].start");
                Calendar cTarget = Calendar.getInstance();
                cTarget.setTime(sdf.parse(sDate)); 
                int iWeek 	= cTarget.get(Calendar.WEEK_OF_YEAR);
                int iYear 	= cTarget.get(Calendar.YEAR); 

                if(iYear == iNowYear) {
                    if(iWeek == iNowWeek) {
                        mlResults.add(mTask);
                    }
                }

            }                    
            
        } else {
            mlResults = retrieveMyTasksPending(context, args, "", true);
        }
        
        return mlResults;
        
    }        
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getMyOpenTasksOfProject(Context context, String[] args) throws Exception {
        
        
        MapList mlResults   = new MapList();
        String sUser        = context.getUser();
        HashMap programMap  = (HashMap) JPO.unpackArgs(args);
        String sOID         = (String) programMap.get("objectId");          
        MapList mlTasks     = retrieveOpenTasksOfProject(context, args, "All", sOID);        
        StringList selBUS   = new StringList();
        
        selBUS.add(DomainObject.SELECT_NAME);

        for (int i = 0; i < mlTasks.size(); i++) {
            
            Map mTask       = (Map)mlTasks.get(i);
            String sOIDTask = (String)mTask.get(DomainObject.SELECT_ID);
            String sCurrent = (String)mTask.get(DomainObject.SELECT_CURRENT);
                
            if(!"Create".equals(sCurrent)) {                
                DomainObject doTask = new DomainObject(sOIDTask);
                MapList mlAssignees = doTask.getRelatedObjects(context, DomainConstants.RELATIONSHIP_ASSIGNED_TASKS, "Person", selBUS, null, true, false, (short)1, "name == '" + sUser + "'", "", 0);
                if(mlAssignees.size() > 0) { mlResults.add(mTask); }
            }
        }
        
        return mlResults;
        
    }          
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getMyOpenTasksByPercentComplete(Context context, String[] args) throws Exception {

        HashMap paramMap    = (HashMap) JPO.unpackArgs(args);
        String sPercent     = (String) paramMap.get("percent");        
        String sValueMin    = "100.0";
        String sValueMax    = "1000.0";
        
        if(sPercent.equals("0-25"))       { sValueMin =  "0.0"; sValueMax =  "25.0"; }
        else if(sPercent.equals("25-50")) { sValueMin = "25.0"; sValueMax =  "50.0"; }
        else if(sPercent.equals("50-75")) { sValueMin = "50.0"; sValueMax =  "75.0"; }
        else if(sPercent.equals("75-99")) { sValueMin = "75.0"; sValueMax = "100.0"; }
        
        StringBuilder sbWhere = new StringBuilder();
        sbWhere.append(" && ").append("(attribute[Percent Complete] >= '").append(sValueMin).append("')");
        sbWhere.append(" && ").append("(attribute[Percent Complete] <  '").append(sValueMax).append("')");
        
        return retrieveMyTasksPending(context, args, sbWhere.toString(), true);
                
    }       
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getMyOpenTasksOfDate(Context context, String[] args) throws Exception {
        
        
        HashMap paramMap    = (HashMap) JPO.unpackArgs(args);       
        String sDate        = (String) paramMap.get("date");        
        Calendar cStart     = Calendar.getInstance();
        Calendar cEnd       = Calendar.getInstance();
        
        
        long lDate = Long.parseLong(sDate);
        cStart.setTimeInMillis(lDate);
        cEnd.setTimeInMillis(lDate);
        cEnd.add(java.util.GregorianCalendar.DAY_OF_YEAR, 1);
                
        StringBuilder sbWhere = new StringBuilder();
        sbWhere.append(" && ");        
        sbWhere.append("(current != 'Create')");
        sbWhere.append(" && ");
        sbWhere.append("(attribute[Task Estimated Finish Date] >= '").append(cStart.get(Calendar.MONTH) + 1).append("/").append(cStart.get(Calendar.DAY_OF_MONTH)).append("/").append(cStart.get(Calendar.YEAR)).append("')");
        sbWhere.append(" && ");
        sbWhere.append("(attribute[Task Estimated Finish Date] <= '").append(cEnd.get(Calendar.MONTH) + 1).append("/").append(cEnd.get(Calendar.DAY_OF_MONTH)).append("/").append(cEnd.get(Calendar.YEAR)).append("')");        
        
        return retrieveMyTasksPending(context, args, sbWhere.toString(), true);
        
    }       
    public MapList retrieveMyTasksPending(Context context, String[] args, String sWhereAppend, Boolean bAssignedOnly) throws Exception {
        
        com.matrixone.apps.common.Person pUser = com.matrixone.apps.common.Person.getPerson(context);
        
        MapList mlResults = new MapList();        
        StringList selTasks = new StringList();
	selTasks.add(DomainObject.SELECT_ID);
	selTasks.add(DomainObject.SELECT_NAME);
	selTasks.add(DomainObject.SELECT_OWNER);
	selTasks.add(DomainObject.SELECT_CURRENT);
	selTasks.add("modified");
	selTasks.add("originated");
	selTasks.add(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
	selTasks.add(DomainObject.SELECT_PERCENTCOMPLETE);
	selTasks.add("state[Assign].start");
	selTasks.add("state[Active].start");
	selTasks.add("state[Review].start");
	selTasks.add(SELECT_FROM_SUBTASK);
    	selTasks.add(ProgramCentralConstants.SELECT_TASK_PROJECT_ID);
    	selTasks.add(ProgramCentralConstants.SELECT_TASK_PROJECT_NAME);

    	 String parentPolicy = "to[" + ProgramCentralConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + ProgramCentralConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.policy";
    	 selTasks.add(parentPolicy);
        
    	String sBusWhere = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "]==True";
		if(UIUtil.isNotNullAndNotEmpty(sWhereAppend)){
			sWhereAppend += "  && " + sBusWhere;
		}else{
			sWhereAppend = "  && " + sBusWhere;
		}

        if(bAssignedOnly) {
            mlResults = pUser.getRelatedObjects(context, DomainConstants.RELATIONSHIP_ASSIGNED_TASKS, "Task", selTasks, null, false, true, (short)1, "(from[Subtask] == 'False') && (current == 'Assign' || current == 'Active' || current == 'Review') " + sWhereAppend, "", 0);            
        } else {       
        	String vaultPattern = ProgramCentralConstants.QUERY_WILDCARD;
            MapList mlTasksOwned = DomainObject.findObjects(context, "Task", vaultPattern, "(from[Subtask] == 'False') && (current == 'Assign' || current == 'Active' || current == 'Review') && (owner == '" + context.getUser() + "')" + sWhereAppend, selTasks);
            MapList mlTasksAssigned = pUser.getRelatedObjects(context, DomainConstants.RELATIONSHIP_ASSIGNED_TASKS, "Task", selTasks, null, false, true, (short)1, "(from[Subtask] == 'False') && (current == 'Assign' || current == 'Active' || current == 'Review') && (owner != '" + context.getUser() + "')" + sWhereAppend, "", 0);            
            mlResults.addAll(mlTasksOwned);
            mlResults.addAll(mlTasksAssigned);        
        }
        
        int size = mlResults.size();
        for(int i=0; i<size; i++){
        	Map taskInfo = (Map)mlResults.get(i);
        	if(taskInfo.containsKey(parentPolicy)){
        		String pPolicy = (String)taskInfo.get(parentPolicy);
        		if(pPolicy.equalsIgnoreCase(ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL)){
            		mlResults.remove(taskInfo);
        	}
        	}
        }
        return mlResults;     
    }     
    

    // Task Assignment and Allocation in WBS View
    public List dynamicColumnsMembersDetails(Context context, String[] args) throws Exception {

    	return dynamicAssignmentViewMembersColumn(context, args);

    }   
    public List dynamicColumnsMembersDetailsAllocation(Context context, String[] args) throws Exception {

    	return dynamicAllocationViewMembersColumn(context, args);

    }    
    public Vector columnTaskAssignment(Context context, String[] args) throws Exception {


        Vector vResult          = new Vector();
        Map paramMap            = (Map) JPO.unpackArgs(args);
        Map paramList           = (Map)paramMap.get("paramList");
        MapList mlObjects       = (MapList) paramMap.get("objectList");        
        HashMap columnMap       = (HashMap) paramMap.get("columnMap");
        HashMap settings        = (HashMap) columnMap.get("settings");
        String sOIDPerson       = (String) settings.get("personId");
        String sLanguage        = (String)paramList.get("languageStr");
        String sCompleted       = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Completed", sLanguage);
        String sLabelAssign     = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.Assign", sLanguage);
        String sLabelUnassign   = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.Unassign", sLanguage);
        String sLabelAssigned   = EnoviaResourceBundle.getProperty(context, "Components", "emxComponents.Common.Assigned", sLanguage);
        
                
        StringList busSelects = new StringList();
        StringList relSelects = new StringList();
        busSelects.add(DomainObject.SELECT_ID);
        relSelects.add(DomainObject.SELECT_RELATIONSHIP_ID);
                
        for (int i = 0; i < mlObjects.size(); i++) {
            
            String sResult          = "";
            Boolean bIsAssigned     = false;
            Map mObject             = (Map) mlObjects.get(i);
            String sOID             = (String)mObject.get(DomainObject.SELECT_ID);            
            String sRowID           = (String)mObject.get("id[level]");            
            DomainObject dObject    = new DomainObject(sOID);


            StringList busSelects2 = new StringList();
            busSelects2.add(DomainObject.SELECT_CURRENT);
            busSelects2.add("type.kindof[Project Space]");

            Map mData               = dObject.getInfo(context, busSelects2);
            String sCurrent           = (String)mData.get(DomainObject.SELECT_CURRENT);
            String sIsProject         = (String)mData.get("type.kindof[Project Space]");                
   
            if(sIsProject.equalsIgnoreCase("FALSE")) {
            
                MapList mlAssignees     = dObject.getRelatedObjects(context, DomainConstants.RELATIONSHIP_ASSIGNED_TASKS, "Person", busSelects, relSelects, true, false, (short)1, "id == '" + sOIDPerson + "'", "", 0);
                String sStyleComplete   = "style='color:#FFF;background-color:#ABB8BD;font-weight:normal;min-width:90px;width=100%;text-align:center;vertical-align:middle;height:20px;line-height:20px;padding:0px;margin:0px;font-style:oblique'";
                String sStyleAssigned   = "style='color:#FFF;background-color:#5F747D;font-weight:normal;min-width:90px;width=100%;text-align:center;vertical-align:middle;height:20px;line-height:20px;padding:0px;margin:0px;'";
                String sStyleUnassigned = "style='font-weight:normal;min-width:90px;width=100%;text-align:center;vertical-align:middle;height:20px;line-height:20px;padding:0px;margin:0px;color:transparent;'";
                StringBuilder sbResult  = new StringBuilder();
                
                if(mlAssignees.size() > 0) {
                    bIsAssigned = true;
                    sResult     = "Assigned" + sResult;
                }      
                
                if(!sCurrent.equals("Complete")) {  
                    
                    if(bIsAssigned) {
                        
                        Map mAssignee   = (Map)mlAssignees.get(0);
                        String sRID     = (String)mAssignee.get(DomainObject.SELECT_RELATIONSHIP_ID);

                        sbResult.append("<div ");
                        sbResult.append(sStyleAssigned);
                        sbResult.append(" onclick='window.open(\"../common/emxColumnAssignmentProcess.jsp?relationship="+DomainConstants.RELATIONSHIP_ASSIGNED_TASKS+"&amp;from=false&amp;mode=remove&amp;objectId=" + sOID + "&amp;rowId=" + sRowID + "&amp;relId=" + sRID + "&amp;personId=" + sOIDPerson + "\", \"listHidden\", \"\", true);'");
                        sbResult.append(" onmouseout='this.style.background=\"#5F747D\";this.style.color=\"#FFF\";this.style.fontWeight=\"normal\"; this.innerHTML=\"").append(sLabelAssigned).append("\"'");
                        sbResult.append(" onmouseover='this.style.background=\"#cc0000\"; this.style.color=\"#FFF\";this.style.fontWeight=\"normal\"; this.style.cursor=\"pointer\"; this.innerHTML=\"- ").append(sLabelUnassign).append("\"'");
                        sbResult.append(">").append(sLabelAssigned).append("</div>");

                    } else {
                        
                        sbResult.append("<div ");
                        sbResult.append(sStyleUnassigned);
                        sbResult.append("  onclick='window.open(\"../common/emxColumnAssignmentProcess.jsp?relationship="+DomainConstants.RELATIONSHIP_ASSIGNED_TASKS+"&amp;from=false&amp;mode=add&amp;objectId=" + sOID + "&amp;rowId=" + sRowID + "&amp;personId=" + sOIDPerson + "\", \"listHidden\", \"\", true);'");                    
                        sbResult.append("  onmouseout='this.style.background=\"transparent\";this.style.color=\"transparent\";this.style.fontWeight=\"normal\"; this.innerHTML=\"-\"'");
                        sbResult.append(" onmouseover='this.style.background=\"#009c00\";    this.style.color=\"#FFF\";this.style.fontWeight=\"normal\"; this.style.cursor=\"pointer\"; this.innerHTML=\"+ ").append(sLabelAssign).append("\"'");                   
                        sbResult.append(">-</div>");
                        
                    }
                    
                } else {
                    
                    sbResult.append("<div ").append(sStyleComplete).append(">");
                    sbResult.append(sCompleted).append("</div>");
                        
                }
                
                sResult = sbResult.toString();

            } 
            
            vResult.add(sResult);
            
        }
        
        return vResult;
        
   }    
    public Vector columnTaskAllocation(Context context, String[] args) throws Exception {


        Vector vResult      = new Vector();
        Map paramMap        = (Map) JPO.unpackArgs(args);
        MapList mlObjects   = (MapList) paramMap.get("objectList");        
        HashMap columnMap   = (HashMap) paramMap.get("columnMap");
        HashMap settings    = (HashMap) columnMap.get("settings");
        String sOIDPerson   = (String) settings.get("personId");
        
        StringList busSelects = new StringList();
        StringList relSelects = new StringList();
        busSelects.add(DomainObject.SELECT_ID);
        relSelects.add(DomainObject.SELECT_RELATIONSHIP_ID);
        relSelects.add("attribute[Percent Allocation]");
        
        for (int i = 0; i < mlObjects.size(); i++) {
            
            String sResult          = "";
            Map mObject             = (Map) mlObjects.get(i);
            String sOID             = (String)mObject.get(DomainObject.SELECT_ID);  
            String sRowID           = (String)mObject.get("id[level]");
            String sRID             = "";
            DomainObject dObject    = new DomainObject(sOID);

            StringList busSelects2 = new StringList();
            busSelects2.add(DomainObject.SELECT_CURRENT);
            busSelects2.add("type.kindof[Project Space]");

            Map mData               = dObject.getInfo(context, busSelects2);
            String sCurrent           = (String)mData.get(DomainObject.SELECT_CURRENT);
            String sIsProject         = (String)mData.get("type.kindof[Project Space]");                
            String sText            = "";
            double dAllocation      = 0.0;
            String sPercentage      = "";
            String[] aContents      = {"0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};
            
            if(sIsProject.equalsIgnoreCase("FALSE")) {
            
                MapList mlAssignees     = dObject.getRelatedObjects(context, DomainConstants.RELATIONSHIP_ASSIGNED_TASKS, "Person", busSelects, relSelects, true, false, (short)1, "id == '" + sOIDPerson + "'", "", 0);                               
                String sStyleTable      = "width:100%;";
                StringBuilder sbStyle   = new StringBuilder();
//                String sStyleLabel      = "";
                String sScriptTable     = " onmouseover='this.lastChild.lastChild.style.visibility=\"visible\";this.firstChild.firstChild.lastChild.firstChild.firstChild.style.visibility=\"visible\";' onmouseout='this.lastChild.lastChild.style.visibility=\"hidden\";this.firstChild.firstChild.lastChild.firstChild.firstChild.style.visibility=\"hidden\";' ";
                String sLabel           = "";
                
                if(mlAssignees.size() > 0) {
    
                    Map mAssignee   = (Map)mlAssignees.get(0);
                    sRID            = (String)mAssignee.get(DomainObject.SELECT_RELATIONSHIP_ID);
                    String sPercent = (String)mAssignee.get("attribute[Percent Allocation]");
                    
                    double dValue   = Task.parseToDouble(sPercent) / 10;                    
                    dValue          = Math.round( dValue ) / 1d;                    
//                    dAllocation     = 0.5 + (dValue / 20);
                    dAllocation     = (dValue / 10);
                    sPercentage     = String.valueOf(dValue);
                    if(sPercentage.contains(".")) {
                        sPercentage = sPercentage.substring(0, sPercentage.indexOf("."));
                    }
                    
                    sbStyle.append("padding-right:5px;");
                    sbStyle.append("font-size:7pt;");
                    sbStyle.append("color:#FFF;");
                    sbStyle.append("text-shadow: 1px 1px 1px #111;");
                    sbStyle.append("border:1px solid #5f747d;");
                    sbStyle.append("background:-ms-linear-gradient(left,  #5f747d 0%, #5f747d ").append(dAllocation * 100).append("%, #abb8bd ").append(dAllocation).append("%);");
                    sbStyle.append("background:-moz-linear-gradient(left,  #5f747d 0%, #5f747d ").append(dAllocation * 100).append("%, #abb8bd ").append(dAllocation).append("%);");
                    sbStyle.append("background:-webkit-linear-gradient(left,  #5f747d 0%, #5f747d ").append(dAllocation * 100).append("%, #abb8bd ").append(dAllocation).append("%);");

                    sLabel = sPercentage + "0%";
                    
                    
                }            
                
                String sURLPrefix       = "<a href='../gnv/GNVTaskAssignment.jsp?mode=update&amp;objectId=" + sOID + "&amp;rowId=" + sRowID + "&amp;personId=" + sOIDPerson + "&amp;relId=" + sRID + "&amp;percent=";
                String sURLSuffix       = "' target='listHidden' style='font-size:6pt;'>";
                String sStylePercentage = "style='text-align:center;font-size:6pt;' width='17px'";
                String sStyleSelected   = "style='color:#FFF;text-align:center;font-size:6pt;background:#5f747d;' width='17px'";
                
                if(!sCurrent.equals("Complete")) { 
                    if(!sPercentage.equals("0"))  {  aContents[0] = sURLPrefix +   "0.0' style='color:#cc0000 !important;" + sURLSuffix +  "X</a>";}
                    if(!sPercentage.equals("1"))  {  aContents[1] = sURLPrefix +  "10.0" + sURLSuffix +  "10</a>";}
                    if(!sPercentage.equals("2"))  {  aContents[2] = sURLPrefix +  "20.0" + sURLSuffix +  "20</a>";}
                    if(!sPercentage.equals("3"))  {  aContents[3] = sURLPrefix +  "30.0" + sURLSuffix +  "30</a>";}
                    if(!sPercentage.equals("4"))  {  aContents[4] = sURLPrefix +  "40.0" + sURLSuffix +  "40</a>";}
                    if(!sPercentage.equals("5"))  {  aContents[5] = sURLPrefix +  "50.0" + sURLSuffix +  "50</a>";}
                    if(!sPercentage.equals("6"))  {  aContents[6] = sURLPrefix +  "60.0" + sURLSuffix +  "60</a>";}
                    if(!sPercentage.equals("7"))  {  aContents[7] = sURLPrefix +  "70.0" + sURLSuffix +  "70</a>";}
                    if(!sPercentage.equals("8"))  {  aContents[8] = sURLPrefix +  "80.0" + sURLSuffix +  "80</a>";}
                    if(!sPercentage.equals("9"))  {  aContents[9] = sURLPrefix +  "90.0" + sURLSuffix +  "90</a>";}
                    if(!sPercentage.equals("10")) { aContents[10] = sURLPrefix + "100.0" + sURLSuffix + "100</a>";}                    
                    if(sPercentage.equals(""))    {  aContents[0] = "";} 
                    if(mlAssignees.size() > 0) {
                        sText  = sURLPrefix + "0.0" +sURLSuffix + "<img style='margin-right:4px;visibility:hidden;' border='0' src='../common/images/buttonMiniCancel.gif' /></a>";
                        sLabel = sPercentage + "0%";
                    } 
                } else {
                    if(!sPercentage.equals("0"))  {  aContents[0] = "";}
                    if(!sPercentage.equals("1"))  {  aContents[1] = "";}
                    if(!sPercentage.equals("2"))  {  aContents[2] = "";}
                    if(!sPercentage.equals("3"))  {  aContents[3] = "";}
                    if(!sPercentage.equals("4"))  {  aContents[4] = "";}
                    if(!sPercentage.equals("5"))  {  aContents[5] = "";}
                    if(!sPercentage.equals("6"))  {  aContents[6] = "";}
                    if(!sPercentage.equals("7"))  {  aContents[7] = "";}
                    if(!sPercentage.equals("8"))  {  aContents[8] = "";}
                    if(!sPercentage.equals("9"))  {  aContents[9] = "";}
                    if(!sPercentage.equals("10")) { aContents[10] = "";}  
                    
                }
            
                StringBuilder sbResult = new StringBuilder();
                
                sbResult.append("<table style='" + sStyleTable + "'" + sScriptTable + ">");
    //            sbResult.append("<tr><td colspan='11' style='text-align:center;background:rgba(37, 66, 86, " + (dAllocation / 100) + ");line-height:18xp;height:18px;vertical-align:middle;font-weight:bold;'>" + sText + "</td></tr>");
//                sbResult.append("<tr><td colspan='11' style='text-align:center;background:rgba(118, 136, 148, " + dAllocation + ");line-height:18xp;height:18px;vertical-align:middle;font-weight:bold;'>" + sText + "</td></tr>");
//                sbResult.append("<tr onmouseover2='this.nextSibling.style.visibility=\"visible\";'><td colspan='11' style='text-align:center;background:rgba(118, 136, 148, " + dAllocation + ");line-height:18xp;height:18px;vertical-align:middle;font-weight:bold;'>" + sText + "</td></tr>");
//                sbResult.append("<tr onmouseover2='this.nextSibling.style.visibility=\"visible\";'><td colspan='11' style='text-align:center;background:rgba(24, 45, 58, " + dAllocation + ");line-height:18xp;height:18px;vertical-align:middle;font-weight:bold;'>" + sText + "</td></tr>");
//                sbResult.append("<tr><td colspan='11' style='text-align:center;background:rgba(43, 136, 217, " + dAllocation + ");line-height:18xp;height:18px;vertical-align:middle;font-weight:bold;'>" + sText + "</td></tr>");
    //            sbResult.append("<tr><td colspan='11' style='text-align:center;background:-moz-linear-gradient(left,  #2b88d9 0%, #207cca " + dAllocation + "%, #4fa0e2 100%);line-height:18xp;height:18px;vertical-align:middle;font-weight:bold;'>" + sText + "</td></tr>");
                sbResult.append("<tr style='border:1px solid transparent;'><td colspan='10' style='text-align:right;" + sbStyle.toString() + "line-height:18xp;height:18px;vertical-align:middle;font-weight:bold;'>" + sText  + sLabel + "</td></tr>");
                sbResult.append("<tr style='visibility:hidden;'>");
//                if(sPercentage.equals("0"))  { sbResult.append("<td " + sStyleSelected + "> 0</td>"); } else { sbResult.append("<td " + sStylePercentage + ">" + aContents[0] + "</td>"); }
                if(sPercentage.equals("1")) { sbResult.append("<td " + sStyleSelected + ">10</td>"); } else { sbResult.append("<td " + sStylePercentage + ">" + aContents[1] + "</td>"); }
                if(sPercentage.equals("2")) { sbResult.append("<td " + sStyleSelected + ">20</td>"); } else { sbResult.append("<td " + sStylePercentage + ">" + aContents[2] + "</td>"); }
                if(sPercentage.equals("3")) { sbResult.append("<td " + sStyleSelected + ">30</td>"); } else { sbResult.append("<td " + sStylePercentage + ">" + aContents[3] + "</td>"); }
                if(sPercentage.equals("4")) { sbResult.append("<td " + sStyleSelected + ">40</td>"); } else { sbResult.append("<td " + sStylePercentage + ">" + aContents[4] + "</td>"); }
                if(sPercentage.equals("5")) { sbResult.append("<td " + sStyleSelected + ">50</td>"); } else { sbResult.append("<td " + sStylePercentage + ">" + aContents[5] + "</td>"); }
                if(sPercentage.equals("6")) { sbResult.append("<td " + sStyleSelected + ">60</td>"); } else { sbResult.append("<td " + sStylePercentage + ">" + aContents[6] + "</td>"); }
                if(sPercentage.equals("7")) { sbResult.append("<td " + sStyleSelected + ">70</td>"); } else { sbResult.append("<td " + sStylePercentage + ">" + aContents[7] + "</td>"); }
                if(sPercentage.equals("8")) { sbResult.append("<td " + sStyleSelected + ">80</td>"); } else { sbResult.append("<td " + sStylePercentage + ">" + aContents[8] + "</td>"); }
                if(sPercentage.equals("9")) { sbResult.append("<td " + sStyleSelected + ">90</td>"); } else { sbResult.append("<td " + sStylePercentage + ">" + aContents[9] + "</td>"); }
                if(sPercentage.equals("10")){ sbResult.append("<td " + sStyleSelected + ">100</td>"); } else { sbResult.append("<td " + sStylePercentage + ">" + aContents[10] + "</td>"); }

                sbResult.append("</tr>");
                sbResult.append("</table>");
                sResult = sbResult.toString();
            
            }
            
            vResult.add(sResult);
        }
        
        return vResult;
        
    }    
                           
    public Vector columnTaskAllocationTotal(Context context, String[] args) throws Exception {
    	
    	return getTaskTotalAllocation(context, args);
    }   
    
    
    public MapList getMembers(Context context, String[] args, boolean bKeepContextUser) throws Exception {    
        
        MapList mlResults       = new MapList();
        HashMap paramMap        = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap      = (HashMap) paramMap.get("requestMap");     
        String sOID             = (String) requestMap.get("objectId");
        DomainObject dObject    = new DomainObject(sOID);
        String sType            = dObject.getInfo(context, DomainObject.SELECT_TYPE);
              
        String sSelectableName          = DomainObject.SELECT_NAME;
        String sSelectableID            = DomainObject.SELECT_ID;
        String sSelectableType          = "Person";
        String sSelectableRelationship  = "Member";
        
        if(sType.equals("Workspace")) {
            sSelectableName         = "to[Project Membership].from.name";
            sSelectableID           = "to[Project Membership].from.id";   
            sSelectableType         = "Project Member";
            sSelectableRelationship = "Project Members";
        } else if(sType.equals("Workspace Vault")) {
            Pattern pTypes = new Pattern("Project Space");
            StringList slFolderTree = new StringList();
            slFolderTree.add(DomainObject.SELECT_ID);
            slFolderTree.add(DomainObject.SELECT_TYPE);
            MapList mlProjects = dObject.getRelatedObjects(context, "Data Vaults,Sub Vaults", "Workspace Vault,Project Space", slFolderTree, null, true, false, (short)0, "", "", 0, pTypes, null, null);
            if(mlProjects.size() > 0) {
                Map mProject = (Map)mlProjects.get(0);
                sOID = (String)mProject.get(DomainObject.SELECT_ID);
                dObject = new DomainObject(sOID);
            }            
        }
        
        StringList busSelects = new StringList();
        busSelects.add(sSelectableName);
        busSelects.add(sSelectableID);        
        busSelects.add(DomainObject.SELECT_ATTRIBUTE_FIRSTNAME);        
        busSelects.add(DomainObject.SELECT_ATTRIBUTE_LASTNAME);     
        StringList relSelects = new StringList();
        relSelects.add(Task.SELECT_PROJECT_ROLE);  
        
        MapList mlProjectMembers = dObject.getRelatedObjects(context, sSelectableRelationship, sSelectableType, busSelects, relSelects, false, true, (short)1, "", "", 0);
               
        if(mlProjectMembers.size() > 0) {
            for (int i = 0; i < mlProjectMembers.size(); i++) {
                
                Map mProjectMember  = (Map)mlProjectMembers.get(i);
                String sPersonName  = (String)mProjectMember.get(sSelectableName);
                String sPersonOID   = (String)mProjectMember.get(sSelectableID);
                Map mPerson         = new HashMap();
                
                mPerson.put(DomainObject.SELECT_NAME, sPersonName);
                mPerson.put(DomainObject.SELECT_ID, sPersonOID);
                mPerson.put(DomainObject.SELECT_ATTRIBUTE_FIRSTNAME,    (String)mProjectMember.get(DomainObject.SELECT_ATTRIBUTE_FIRSTNAME));
                mPerson.put(DomainObject.SELECT_ATTRIBUTE_LASTNAME,     (String)mProjectMember.get(DomainObject.SELECT_ATTRIBUTE_LASTNAME));
                mPerson.put(Task.SELECT_PROJECT_ROLE,  (String)mProjectMember.get(Task.SELECT_PROJECT_ROLE));
                
                if(bKeepContextUser == false) {
                    if(!sPersonName.equals(context.getUser())){
                        mlResults.add(mPerson);
                    }                    
                } else{
                    mlResults.add(mPerson);
                }
                
            }
        }      
        
        mlResults.sort(DomainObject.SELECT_ATTRIBUTE_FIRSTNAME, "ascending", "String");
        mlResults.sort(DomainObject.SELECT_ATTRIBUTE_LASTNAME, "ascending", "String");  
        
        return mlResults;
        
    }    
   
    
    // Enhanced WBS View / Apply State Filter to WBS expand 
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList expandWBSInWork(Context context, String[] args) throws Exception {
        
        
        HashMap arguMap         = (HashMap) JPO.unpackArgs(args);
        String sOID             = (String) arguMap.get("objectId");
        DomainObject dObject    = new DomainObject(sOID);
        StringList busSelects   = new StringList();
        StringList relSelects   = new StringList();
        
        busSelects.add(DomainObject.SELECT_ID);
        busSelects.add(DomainObject.SELECT_CURRENT);
        busSelects.add(Task.SELECT_TASK_ESTIMATED_START_DATE);
        busSelects.add(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
        relSelects.add(DomainObject.SELECT_RELATIONSHIP_ID);
        
        MapList mapList =  dObject.getRelatedObjects(context, "Subtask", "Task Management,Project Space", busSelects, relSelects, false, true, (short) 1, "current != 'Complete'", "", 0);

        return applyDateFilter(context, args, mapList);
        
    }       
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList expandWBSStateReview(Context context, String[] args) throws Exception {
        
        HashMap arguMap         = (HashMap) JPO.unpackArgs(args);
        String sOID             = (String) arguMap.get("objectId");
        DomainObject dObject    = new DomainObject(sOID);
        StringList busSelects   = new StringList();
        StringList relSelects   = new StringList();
        
        busSelects.add(DomainObject.SELECT_ID);
        busSelects.add(DomainObject.SELECT_CURRENT);
        busSelects.add(Task.SELECT_TASK_ESTIMATED_START_DATE);
        busSelects.add(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
        relSelects.add(DomainObject.SELECT_RELATIONSHIP_ID);
        
        MapList mapList =  dObject.getRelatedObjects(context, "Subtask", "Task Management,Project Space", busSelects, relSelects, false, true, (short) 1, "current == 'Active' || current == 'Review'", "");
        
        if(mapList.size() > 0) {
            for (int i = mapList.size() - 1; i >= 0; i--) {
                Map map = (Map)mapList.get(i);
                String sCurrent = (String)map.get(DomainObject.SELECT_CURRENT);                
                if(sCurrent.equals("Active")) {
                    String sOIDSubtask = (String)map.get(DomainObject.SELECT_ID);
                    DomainObject doSubtask = new DomainObject(sOIDSubtask);
                    MapList mlSubtasks =  doSubtask.getRelatedObjects(context, "Subtask", "Task Management,Project Space", busSelects, relSelects, false, true, (short) 0, "current == 'Active' || current == 'Review'", "");
                    if(mlSubtasks.size() > 0) {
                        mlSubtasks.sort(DomainObject.SELECT_CURRENT, "descending", "String");
                        Map mSubtask = (Map)mlSubtasks.get(0);
                        String sCurrentSubtask = (String)mSubtask.get(DomainObject.SELECT_CURRENT);
                        if(!sCurrentSubtask.equals("Review")) {        
                            mapList.remove(i);
                        }
                    } else { 
                        mapList.remove(i);
                    }
                }
            }
        }
 
        return applyDateFilter(context, args, mapList);
    }
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList expandWBSOwned(Context context, String[] args) throws Exception {
                
        
        HashMap arguMap         = (HashMap) JPO.unpackArgs(args);
        String sUser            = context.getUser();
        String sOID             = (String) arguMap.get("objectId");
        DomainObject dObject    = new DomainObject(sOID);
        StringList busSelects   = new StringList();
        StringList relSelects   = new StringList();
        
        busSelects.add(DomainObject.SELECT_ID);
        busSelects.add(DomainObject.SELECT_OWNER);
        busSelects.add(Task.SELECT_TASK_ESTIMATED_START_DATE);
        busSelects.add(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);        
        relSelects.add(DomainObject.SELECT_RELATIONSHIP_ID);
        relSelects.add(ProgramCentralConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER);
        
        MapList mapList = dObject.getRelatedObjects(context, "Subtask", "Task Management,Project Space", busSelects, relSelects, false, true, (short) 1, "", "", 0);
        mapList.sort(ProgramCentralConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER, "ascending", "integer");
        
        for (int i = mapList.size() - 1; i >= 0; i--) {
        
            Map map                 = (Map)mapList.get(i);
            String sOwnerSubtask    = (String)map.get(DomainObject.SELECT_OWNER);

            if(!sUser.equals(sOwnerSubtask)) {
            
                Boolean bRemove         = true;
                String sOIDSubtask      = (String)map.get(DomainObject.SELECT_ID);
                DomainObject doSubtask  = new DomainObject(sOIDSubtask);
                MapList mlSubtasks      =  doSubtask.getRelatedObjects(context, "Subtask", "Task Management,Project Space", busSelects, null, false, true, (short) 0, "", "", 0);
                    
                for (int j = 0; j < mlSubtasks.size(); j++) {
                    Map mSubtask    = (Map)mlSubtasks.get(j);
                    sOwnerSubtask   = (String)mSubtask.get(DomainObject.SELECT_OWNER);
                    if(sUser.equals(sOwnerSubtask)) {
                        bRemove = false;
                        break;
                    }
                }
                
                if(bRemove) {
                    mapList.remove(i);
                }
                                        
            }           

        }
        
 
        return applyDateFilter(context, args, mapList);
    }    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList expandWBSOwnedUnassigned(Context context, String[] args) throws Exception {
        
        HashMap arguMap         = (HashMap) JPO.unpackArgs(args);
        String sUser            = context.getUser();
        String sOID             = (String) arguMap.get("objectId");
        DomainObject dObject    = new DomainObject(sOID);
        StringList busSelects   = new StringList();
        StringList relSelects   = new StringList();
        
        busSelects.add(DomainObject.SELECT_ID);
        busSelects.add(DomainObject.SELECT_NAME);
        busSelects.add(DomainObject.SELECT_OWNER);
        busSelects.add(SELECT_RELATIONSHIP_ASSIGNED_TASKS);
        relSelects.add(DomainObject.SELECT_RELATIONSHIP_ID);
        relSelects.add(ProgramCentralConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER);
        
        MapList mapList = dObject.getRelatedObjects(context, "Subtask", "Task Management,Project Space", busSelects, relSelects, false, true, (short) 1, "", "", 0);
        mapList.sort(ProgramCentralConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER, "ascending", "integer");
        
        for (int i = mapList.size() - 1; i >= 0; i--) {
        
            Map map                 = (Map)mapList.get(i);
            String sOwnerSubtask    = (String)map.get(DomainObject.SELECT_OWNER);
            String sIsAssigned      = (String)map.get(SELECT_RELATIONSHIP_ASSIGNED_TASKS);
            
            if(sUser.equals(sOwnerSubtask) && sIsAssigned.equalsIgnoreCase("FALSE")) {
            } else {
            
                Boolean bRemove         = true;
                String sOIDSubtask      = (String)map.get(DomainObject.SELECT_ID);
                DomainObject doSubtask  = new DomainObject(sOIDSubtask);
                MapList mlSubtasks      =  doSubtask.getRelatedObjects(context, "Subtask", "Task Management,Project Space", busSelects, null, false, true, (short) 0, "", "", 0);
                    
                for (int j = 0; j < mlSubtasks.size(); j++) {
                    Map mSubtask    = (Map)mlSubtasks.get(j);
                    sOwnerSubtask   = (String)mSubtask.get(DomainObject.SELECT_OWNER);
                    sIsAssigned     = (String)mSubtask.get(SELECT_RELATIONSHIP_ASSIGNED_TASKS);
                    if(sUser.equals(sOwnerSubtask)) {
                        if(sIsAssigned.equalsIgnoreCase("FALSE")) {                        
                            bRemove = false;
                            break;
                        }
                    }
                }
                
                if(bRemove) {
                    mapList.remove(i);
                }
                
            }           
        }

        return applyDateFilter(context, args, mapList);
    }    
    public MapList applyDateFilter(Context context, String[] args, MapList mapList) throws Exception {

        
        HashMap arguMap         = (HashMap) JPO.unpackArgs(args);        
        String sStartFrom 	= (String) arguMap.get("GNVWBSFilterStartFrom");
        String sStartTo		= (String) arguMap.get("GNVWBSFilterStartTo");
        String sFinishFrom 	= (String) arguMap.get("GNVWBSFilterFinishFrom");
        String sFinishTo 	= (String) arguMap.get("GNVWBSFilterFinishTo");	
        String sLanguage        = (String) arguMap.get("languageStr");	

        if(null == sStartFrom)  { sStartFrom = ""; }
        if(null == sStartTo)    { sStartTo = ""; }
        if(null == sFinishFrom) { sFinishFrom = ""; }
        if(null == sFinishTo)   { sFinishTo = ""; }                
          
        sLanguage = sLanguage.substring(0, 2);
        Locale locale = new Locale(sLanguage);    
        DateFormat dfFilter = DateFormat.getDateInstance(2, locale);    
        
       if(sFinishTo.equals("")) {
            if(!sStartFrom.equals("")) {
                if(mapList.size() > 0) {                                
                    for (int i = mapList.size() - 1; i >= 0; i--) {
                        Map map = (Map)mapList.get(i);          
                        Calendar cTaskStart = Calendar.getInstance();
                        String sTaskStart = (String)map.get(Task.SELECT_TASK_ESTIMATED_START_DATE);
                        cTaskStart.setTime(sdf.parse(sTaskStart));
                        Calendar cStartFrom = Calendar.getInstance();
                        cStartFrom.setTime(dfFilter.parse(sStartFrom));
                        if(cStartFrom.after(cTaskStart)) {
                            String sOIDTask = (String)map.get(DomainObject.SELECT_ID);
                            DomainObject doTask = new DomainObject(sOIDTask);
                            StringList slTask = new StringList();
                            slTask.add(Task.SELECT_TASK_ESTIMATED_START_DATE);
                            MapList mlTaskChildren = doTask.getRelatedObjects(context, "Subtask", "Project Space,Task Management", slTask, null, false, true, (short)0, "", "", 0);
                            if(mlTaskChildren.size() > 0) {
                                mlTaskChildren.sort(Task.SELECT_TASK_ESTIMATED_START_DATE, "decending", "date");
                                Map mTaskChild = (Map)mlTaskChildren.get(0);
                                sTaskStart = (String)mTaskChild.get(Task.SELECT_TASK_ESTIMATED_START_DATE);
                                cTaskStart.setTime(sdf.parse(sTaskStart));
                                if(cStartFrom.after(cTaskStart)) {
                                    mapList.remove(i);
                                }
                            } else {
                                mapList.remove(i);
                            }
                        }
                    }           
                }
            } 
        }

        if(!sStartTo.equals("")) {
            if(mapList.size() > 0) {
                for (int i = mapList.size() - 1; i >= 0; i--) {
                    Map map = (Map)mapList.get(i);
                    Calendar cStart = Calendar.getInstance();
                    String sTaskStart = (String)map.get(Task.SELECT_TASK_ESTIMATED_START_DATE);
                    cStart.setTime(sdf.parse(sTaskStart));
                    Calendar cStartTo = Calendar.getInstance();
                    cStartTo.setTime(dfFilter.parse(sStartTo));
                    cStartTo.add(java.util.GregorianCalendar.DAY_OF_YEAR, +1);
                    if(cStartTo.before(cStart)) {
                        mapList.remove(i);
                    }						
                }
            }
        }	
        if(!sFinishFrom.equals("")) {
            if(mapList.size() > 0) {
                for (int i = mapList.size() - 1; i >= 0; i--) {
                    Map map = (Map)mapList.get(i);
                    Calendar cFinish = Calendar.getInstance();
                    String sTaskFinish = (String)map.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                    cFinish.setTime(sdf.parse(sTaskFinish));
                    Calendar cFinishFrom = Calendar.getInstance();
                    cFinishFrom.setTime(dfFilter.parse(sFinishFrom));
                    if(cFinishFrom.after(cFinish)) {
                        mapList.remove(i);
                    }
                }
            }
        }
        if(!sFinishTo.equals("")) {
            if(mapList.size() > 0) {                                
                for (int i = mapList.size() - 1; i >= 0; i--) {
                    Map map = (Map)mapList.get(i);          
                    Calendar cTaskFinish = Calendar.getInstance();
                    String sTaskFinish = (String)map.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                    cTaskFinish.setTime(sdf.parse(sTaskFinish));
                    Calendar cFinishTo = Calendar.getInstance();
                    cFinishTo.setTime(dfFilter.parse(sFinishTo));
                    cFinishTo.add(java.util.GregorianCalendar.DAY_OF_YEAR, +1);
                    if(cFinishTo.before(cTaskFinish)) {									
                        String sOIDTask = (String)map.get(DomainObject.SELECT_ID);
                        DomainObject doTask = new DomainObject(sOIDTask);
                        StringList slTask = new StringList();
                        slTask.add(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                        slTask.add(Task.SELECT_TASK_ESTIMATED_START_DATE);
                        MapList mlTaskChildren = doTask.getRelatedObjects(context, "Subtask", "Project Space,Task Management", slTask, null, false, true, (short)0, "", "", 0);
                        if(!sStartFrom.equals("")) {
						
                            Calendar cStartFrom = Calendar.getInstance();
                            cStartFrom.setTime(dfFilter.parse(sStartFrom));						
                            Calendar cTaskStart = Calendar.getInstance();
                            Boolean bKeep = false;
							
                            for(int j = 0; j < mlTaskChildren.size(); j++) {
                                Map mTaskChild = (Map)mlTaskChildren.get(j);
                                sTaskFinish = (String)mTaskChild.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                                String sTaskStart = (String)mTaskChild.get(Task.SELECT_TASK_ESTIMATED_START_DATE);

                                cTaskFinish.setTime(sdf.parse(sTaskFinish));
                                cTaskStart.setTime(sdf.parse(sTaskStart));

                                if(cTaskFinish.before(cFinishTo)) {
                                    if(cTaskStart.after(cStartFrom)) {
                                        bKeep = true;
                                        break;
                                    }								
                                }																
                            }
							
                            if(!bKeep) {
                                mapList.remove(i);
                            }
						
                        } else {
                            if(mlTaskChildren.size() > 0) {
                                mlTaskChildren.sort(Task.SELECT_TASK_ESTIMATED_FINISH_DATE, "ascending", "date");
                                Map mTaskChild = (Map)mlTaskChildren.get(0);
                                sTaskFinish = (String)mTaskChild.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                                cTaskFinish.setTime(sdf.parse(sTaskFinish));
                                if(cTaskFinish.after(cFinishTo)) {
                                    mapList.remove(i);
                                }
                            } else {
                                mapList.remove(i);
                            }
                        }						
                    } else {
                        if(!sStartFrom.equals("")) {
		    			
                            Calendar cStartFrom = Calendar.getInstance();
                            Calendar cTaskStart = Calendar.getInstance();
                            cStartFrom.setTime(dfFilter.parse(sStartFrom));

                            String sTaskStart = (String)map.get(Task.SELECT_TASK_ESTIMATED_START_DATE);
                            cTaskStart.setTime(sdf.parse(sTaskStart));
                            if(cTaskStart.before(cStartFrom)) {
                                mapList.remove(i);
                            }
							
                        }							
                    }
                }           
            }
        }    

        
        return mapList ;
    }        
         
    
    // Project Assessment Tuning
    public  String getLatestAssessmentName(Context context, String[] args) throws Exception {
    
        StringBuilder sbResult  = new StringBuilder();
        HashMap programMap      = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap        = (HashMap) programMap.get("paramMap");
        String sOID             = (String) paramMap.get("objectId");         
        DomainObject dObject    = new DomainObject(sOID);
        String sDimensions      = EnoviaResourceBundle.getProperty(context, "emxFramework.PopupSize.Large");            
        String[] aDimensions    = sDimensions.split("x");
        
        StringList busSelects = new StringList();
        busSelects.add(DomainObject.SELECT_ID);
        busSelects.add(DomainObject.SELECT_NAME);
        busSelects.add("modified");
        
        MapList mlAssessments = dObject.getRelatedObjects(context, "Project Assessment", "Assessment", busSelects, null, false, true, (short)1, "", "", 0);
        
        if(mlAssessments.size() > 0) {        
            mlAssessments.sort("modified", "descending", "date");            
            String sIcon = UINavigatorUtil.getTypeIconProperty(context, "Assessment");           
            Map mAssessment = (Map)mlAssessments.get(0);
            sbResult.append("<a onClick=\"emxFormLinkClick('../common/emxTree.jsp?objectId=").append((String)mAssessment.get(DomainObject.SELECT_ID));
            sbResult.append("', 'popup', '', '").append(aDimensions[0]).append("', '").append(aDimensions[1]).append("', '')\">");
            sbResult.append("<img src='images/").append(sIcon).append("' /> ");
            sbResult.append((String)mAssessment.get(DomainObject.SELECT_NAME));        
            sbResult.append("</a>");        
        }
        
        return sbResult.toString();
        
    }
    public  String getLatestAssessmentModified(Context context, String[] args) throws Exception {
        
        StringBuilder sbResult  = new StringBuilder();
        HashMap programMap      = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap        = (HashMap) programMap.get("paramMap");
        String sOID             = (String) paramMap.get("objectId");         
        DomainObject dObject    = new DomainObject(sOID);
        
        StringList busSelects = new StringList();
        busSelects.add("modified");
        
        MapList mlAssessments = dObject.getRelatedObjects(context, "Project Assessment", "Assessment", busSelects, null, false, true, (short)1, "", "", 0);
        
        if(mlAssessments.size() > 0) {        
            mlAssessments.sort("modified", "descending", "date");
            Map mAssessment = (Map)mlAssessments.get(0);
            sbResult.append((String)mAssessment.get("modified"));        
        }
        
        return sbResult.toString();
        
    }      
    public  String getLatestAssessmentAssessor(Context context, String[] args) throws Exception {
        
        StringBuilder sbResult  = new StringBuilder();
        HashMap programMap      = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap        = (HashMap) programMap.get("paramMap");
        String sOID             = (String) paramMap.get("objectId");         
        DomainObject dObject    = new DomainObject(sOID);
        
        StringList busSelects = new StringList();
        busSelects.add("modified");
        busSelects.add("attribute[Originator]");
        
        MapList mlAssessments = dObject.getRelatedObjects(context, "Project Assessment", "Assessment", busSelects, null, false, true, (short)1, "", "", 0);
        
        if(mlAssessments.size() > 0) {        
            mlAssessments.sort("modified", "descending", "date");
            Map mAssessment = (Map)mlAssessments.get(0);
            sbResult.append((String)mAssessment.get("attribute[Originator]"));        
        }
        
        return sbResult.toString();
        
    }      
    public  String getLatestAssessmentSummary(Context context, String[] args) throws Exception {        
        return retrieveLatestAssessmentComments(context, args, "attribute[Assessment Status]", "attribute[Assessment Comments]");        
    }    
    public  String getLatestAssessmentResource(Context context, String[] args) throws Exception {        
        return retrieveLatestAssessmentComments(context, args, "attribute[Resource Status]", "attribute[Resource Comments]");        
    }  
    public  String getLatestAssessmentSchedule(Context context, String[] args) throws Exception {        
        return retrieveLatestAssessmentComments(context, args, "attribute[Schedule Status]", "attribute[Schedule Comments]");        
    }
    public  String getLatestAssessmentRisk(Context context, String[] args) throws Exception {        
        return retrieveLatestAssessmentComments(context, args, "attribute[Risk Status]", "attribute[Risk Comments]");        
    }
    public  String getLatestAssessmentFinance(Context context, String[] args) throws Exception {        
        return retrieveLatestAssessmentComments(context, args, "attribute[Finance Status]", "attribute[Finance Comments]");        
    }    
    public  String retrieveLatestAssessmentComments(Context context, String[] args, String selectStatus, String selectComments) throws Exception {
        
        StringBuilder sbResult  = new StringBuilder();
        HashMap programMap      = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap        = (HashMap) programMap.get("paramMap");
        String sOID             = (String) paramMap.get("objectId");         
        DomainObject dObject    = new DomainObject(sOID);
        
        StringList busSelects = new StringList();
        busSelects.add("modified");
        busSelects.add(selectStatus);
        busSelects.add(selectComments);
        
        MapList mlAssessments = dObject.getRelatedObjects(context, "Project Assessment", "Assessment", busSelects, null, false, true, (short)1, "", "", 0);
        
        if(mlAssessments.size() > 0) {        
            mlAssessments.sort("modified", "descending", "date");
            Map mAssessment = (Map)mlAssessments.get(0);
            String sStatus = (String)mAssessment.get(selectStatus);            
            String sComments = (String)mAssessment.get(selectComments);            
            
            sComments = sComments.replaceAll("\r\n", "<br/>");
            sComments = sComments.replaceAll("\r", "<br/>");
            sComments = sComments.replaceAll("\n", "<br/>");
            String sImage = "iconStatusGrayMedium.png";
            sbResult.append("<table><tr><td style='padding-right:10px;width:28px;'>");
            
            if(sStatus.equals("Red")) { sImage = "iconStatusRedMedium.png"; }
            else if(sStatus.equals("Yellow")) { sImage = "iconStatusYellowMedium.png"; }
            else if(sStatus.equals("Green")) { sImage = "iconStatusGreenMedium.png"; }
            
            sbResult.append("<img src='../gnv/images/").append(sImage).append("' />");
            sbResult.append("</td><td>");
            sbResult.append(sComments);
            sbResult.append("<td></tr></table>");
            
        }
        
        return sbResult.toString();
    }          
    public String[] getProjectAssessmentHistoryData(Context context, String[] args) throws Exception {
        
        
        String[] aResult            = new String[5];                
        HashMap paramMap            = (HashMap) JPO.unpackArgs(args);
        String sOID                 = (String) paramMap.get("objectId");
        String sLanguage            = (String) paramMap.get("languageStr");
        String sRandomize           = (String) paramMap.get("randomize");
        StringBuilder sbCategories  = new StringBuilder();
        StringBuilder sbData1       = new StringBuilder();
        StringBuilder sbData2       = new StringBuilder();
        StringBuilder sbData3       = new StringBuilder();
        StringBuilder sbData4       = new StringBuilder();
        DomainObject doProject      = new DomainObject(sOID);
        
        
        if(null == sRandomize) { sRandomize = ""; }
        
        StringList busSelects = new StringList();
        busSelects.add(DomainObject.SELECT_ORIGINATED);
// fzs        busSelects.add("modified");            
        busSelects.add(DomainObject.SELECT_ORIGINATOR);            
        busSelects.add(SELECT_ATTRIBUTE_ASSESSMENT_STATUS);            
        busSelects.add(SELECT_ATTRIBUTE_ASSESSMENT_COMMENTS);            
        busSelects.add(SELECT_ATTRIBUTE_RESOURCE_STATUS);            
        busSelects.add(SELECT_ATTRIBUTE_RESOURCE_COMMENTS);            
        busSelects.add(SELECT_ATTRIBUTE_SCHEDULE_STATUS);            
        busSelects.add(SELECT_ATTRIBUTE_SCHEDULE_COMMENTS);            
        busSelects.add(SELECT_ATTRIBUTE_FINANCE_STATUS);            
        busSelects.add(SELECT_ATTRIBUTE_FINANCE_COMMENTS);            
        busSelects.add(SELECT_ATTRIBUTE_RISK_STATUS);            
        busSelects.add(SELECT_ATTRIBUTE_RISK_COMMENTS);            
                
        MapList mlAssessments = doProject.getRelatedObjects(context, "Project Assessment", "Assessment", busSelects, null, false, true, (short)1, "", "", 0);
        
        mlAssessments.sort(DomainObject.SELECT_ORIGINATED, "descending", "date");
        
        if(sRandomize.contains("AssessmentHistory")) {
            for(int i = mlAssessments.size() - 1; i >= 1; i --) {    
                mlAssessments.remove(i);
            }
        } 
        
        for(int i = 0; i < mlAssessments.size(); i ++) {

            Map mAssessment             = (Map)mlAssessments.get(i);
            String sOriginated          = (String)mAssessment.get(DomainObject.SELECT_ORIGINATED);
            String sOriginator          = (String)mAssessment.get(DomainObject.SELECT_ORIGINATOR);
            String sStatusAssessment    = (String)mAssessment.get(SELECT_ATTRIBUTE_ASSESSMENT_STATUS);
            String sCommentAssessment   = (String)mAssessment.get(SELECT_ATTRIBUTE_ASSESSMENT_COMMENTS);
            String sStatusResource      = (String)mAssessment.get(SELECT_ATTRIBUTE_RESOURCE_STATUS);
            String sCommentResource     = (String)mAssessment.get(SELECT_ATTRIBUTE_RESOURCE_COMMENTS);
            String sStatusSchedule      = (String)mAssessment.get(SELECT_ATTRIBUTE_SCHEDULE_STATUS);
            String sCommentSchedule     = (String)mAssessment.get(SELECT_ATTRIBUTE_SCHEDULE_COMMENTS);
            String sStatusFinance       = (String)mAssessment.get(SELECT_ATTRIBUTE_FINANCE_STATUS);
            String sCommentFinance      = (String)mAssessment.get(SELECT_ATTRIBUTE_FINANCE_COMMENTS);
            String sStatusRisk          = (String)mAssessment.get(SELECT_ATTRIBUTE_RISK_STATUS);
            String sCommentRisk         = (String)mAssessment.get(SELECT_ATTRIBUTE_RISK_COMMENTS);
            
            if("".equals(sCommentAssessment))  { sCommentAssessment    = "-"; }
            if("".equals(sCommentResource) )   { sCommentResource      = "-"; }
            if("".equals(sCommentSchedule))    { sCommentSchedule      = "-"; }
            if("".equals(sCommentFinance))     { sCommentFinance       = "-"; }
            if("".equals(sCommentRisk))        { sCommentRisk          = "-"; }
                        
            appendAssessmentData(sOriginated, "4", sbData1, sbData3, sbData2, sbData4, sOriginator, sStatusAssessment   , sCommentAssessment);
            appendAssessmentData(sOriginated, "3", sbData1, sbData3, sbData2, sbData4, sOriginator, sStatusResource     , sCommentResource  );
            appendAssessmentData(sOriginated, "2", sbData1, sbData3, sbData2, sbData4, sOriginator, sStatusSchedule     , sCommentSchedule  );
            appendAssessmentData(sOriginated, "1", sbData1, sbData3, sbData2, sbData4, sOriginator, sStatusFinance      , sCommentFinance   );
            appendAssessmentData(sOriginated, "0", sbData1, sbData3, sbData2, sbData4, sOriginator, sStatusRisk         , sCommentRisk      );
            
        }
        
        if(sRandomize.equalsIgnoreCase("AssessmentHistory")) {
            
            Calendar cDate = Calendar.getInstance();            
            Random generator    = new Random();

            cDate.add(java.util.GregorianCalendar.MONTH, -1);
            
            for(int i = 0; i < 9; i++) {
                
                cDate.add(java.util.GregorianCalendar.MONTH, -1);
              
                int iAssessment = generator.nextInt(3);
                int iResource   = generator.nextInt(3);
                int iSchedule   = generator.nextInt(3);
                int iFinance    = generator.nextInt(3);
                int iRisk       = generator.nextInt(3);
                
                String sStatusAssessment = "-";
                String sStatusResource = "-";
                String sStatusSchedule = "-";
                String sStatusFinance = "-";
                String sStatusRisk = "-";
                
                if(iAssessment == 2) { sStatusAssessment = "Red"; } else if(iAssessment == 1) { sStatusAssessment = "Yellow"; } else if(iAssessment == 0) { sStatusAssessment = "Green"; }                
                if(iResource   == 2) { sStatusResource   = "Red"; } else if(iResource   == 1) { sStatusResource   = "Yellow"; } else if(iResource   == 0) { sStatusResource   = "Green"; }
                if(iSchedule   == 2) { sStatusSchedule   = "Red"; } else if(iSchedule   == 1) { sStatusSchedule   = "Yellow"; } else if(iSchedule   == 0) { sStatusSchedule   = "Green"; }
                if(iFinance    == 2) { sStatusFinance    = "Red"; } else if(iFinance    == 1) { sStatusFinance    = "Yellow"; } else if(iFinance    == 0) { sStatusFinance    = "Green"; }
                if(iRisk       == 2) { sStatusRisk       = "Red"; } else if(iRisk       == 1) { sStatusRisk       = "Yellow"; } else if(iRisk       == 0) { sStatusRisk       = "Green"; }
                
                String sDate = sdf.format(cDate.getTime());

                appendAssessmentData(sDate, "4", sbData1, sbData3, sbData2, sbData4, "", sStatusAssessment   , "-");   
                appendAssessmentData(sDate, "3", sbData1, sbData3, sbData2, sbData4, "", sStatusResource     , "-"  );
                appendAssessmentData(sDate, "2", sbData1, sbData3, sbData2, sbData4, "", sStatusSchedule     , "-"  );
                appendAssessmentData(sDate, "1", sbData1, sbData3, sbData2, sbData4, "", sStatusFinance      , "-"   );
                appendAssessmentData(sDate, "0", sbData1, sbData3, sbData2, sbData4, "", sStatusRisk         , "-"      );                
                
                
            }
            
        }        


        if(sbData1.length() > 0) { sbData1.setLength(sbData1.length() - 1); }
        if(sbData2.length() > 0) { sbData2.setLength(sbData2.length() - 1); }
        if(sbData3.length() > 0) { sbData3.setLength(sbData3.length() - 1); }
        if(sbData4.length() > 0) { sbData4.setLength(sbData4.length() - 1); }                
        
        sbCategories.append("'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Risk"     , sLanguage)).append("',");
        sbCategories.append("'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Finance"     , sLanguage)).append("',");
        sbCategories.append("'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Schedule"     , sLanguage)).append("',");
        sbCategories.append("'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Resource"     , sLanguage)).append("',");
        sbCategories.append("'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.People.ProjectAssignmentFilterSummary"     , sLanguage)).append("'");

        aResult[0] = sbCategories.toString();
        aResult[1] = sbData1.toString();
        aResult[2] = sbData2.toString();
        aResult[3] = sbData3.toString();
        aResult[4] = sbData4.toString();
        
        return aResult;
        
    }
    public void appendAssessmentData(String sDate, String sCategory, StringBuilder sbData1, StringBuilder sbData2, StringBuilder sbData3, StringBuilder sbData4, String sOriginator, String sStatus, String sComment) throws ParseException  {
              

        Calendar cDate = Calendar.getInstance();
        cDate.setTime(sdf.parse(sDate));

        
        StringBuilder sbToAppend = new StringBuilder();        
        sComment = sComment.replaceAll("\n", "<br/>");
        
        if(!"".equals(sOriginator)) { sOriginator += ", "; }
            
        sbToAppend.append("{ name:'").append(sComment).append("<br/>(").append(sOriginator).append(sDate.substring(0, sDate.indexOf(" "))).append(")',");
        sbToAppend.append(" x:Date.UTC(").append(cDate.get(Calendar.YEAR)).append(",").append(cDate.get(Calendar.MONTH)).append(",").append(cDate.get(Calendar.DAY_OF_MONTH)).append("), ");
        sbToAppend.append(" y:").append(sCategory).append("},");
        
        if(sStatus.equals("Red"))         { sbData1.append(sbToAppend.toString()); }
        else if(sStatus.equals("Green"))  { sbData2.append(sbToAppend.toString()); }
        else if(sStatus.equals("Yellow")) { sbData3.append(sbToAppend.toString()); }
        else                              { sbData4.append(sbToAppend.toString()); }                    
        
    }
    
  
    // Task Assignment Dashboards 
    public MapList getTaskOwners(Context context, String[] args) throws Exception {

        
        MapList mlResult    = new MapList();
        MapList mlObjects   = new MapList();
        HashMap paramMap    = (HashMap) JPO.unpackArgs(args);
        String sOID         = (String) paramMap.get("objectId");
               
        if(null == sOID || "".equals(sOID)) {
            mlObjects = getActiveProjects(context);
        } else {
            Map mObject = new HashMap();
            mObject.put(DomainObject.SELECT_ID, sOID);
            mlObjects.add(mObject);
        }
                
        if (mlObjects.size() > 0) {        
            for (int i = 0; i < mlObjects.size(); i++) {
                Map mProject = (Map) mlObjects.get(i);
                String sOIDProject = (String) mProject.get(DomainObject.SELECT_ID);
                MapList mlTasks = retrieveTasksOfProgramOrProject(context, sOIDProject, "", "", "");
                if (mlTasks.size() > 0) {        
                    for (int j = 0; j < mlTasks.size(); j++) {
                        Map mTask       = (Map) mlTasks.get(j);
                        String sOwner   = (String)mTask.get(DomainObject.SELECT_OWNER);
                        Map mOwner      = new HashMap();
                        mOwner.put(DomainObject.SELECT_OWNER, sOwner);
                        if(!mlResult.contains(mOwner)) {
                            mlResult.add(mOwner);
                        }
                    }
                }
            }
        }
        
        if (mlResult.size() > 0) {        
            for (int i = 0; i < mlResult.size(); i++) {  
                Map mResult = (Map)mlResult.get(i);
                String sOwner = (String)mResult.get(DomainObject.SELECT_OWNER);
                com.matrixone.apps.common.Person pOwner = com.matrixone.apps.common.Person.getPerson(context, sOwner);
                mResult.put(DomainObject.SELECT_ID, pOwner.getObjectId());
            }      
        }
        
        return mlResult;

    }      
    public MapList getTaskAssignees(Context context, String[] args) throws Exception {

        
        MapList mlResult    = new MapList();
        MapList mlObjects   = new MapList();
        HashMap paramMap    = (HashMap) JPO.unpackArgs(args);
        String sOID         = (String) paramMap.get("objectId");
        String sOwner       = context.getUser();
               
        if(null == sOID || "".equals(sOID)) {
            mlObjects = getActiveProjects(context);
        } else {
            Map mObject = new HashMap();
            mObject.put(DomainObject.SELECT_ID, sOID);
            mlObjects.add(mObject);
        }
                
 
        for (int i = 0; i < mlObjects.size(); i++) {
                
            Map mProject        = (Map) mlObjects.get(i);
            String sOIDProject  = (String) mProject.get(DomainObject.SELECT_ID);
            MapList mlTasks     = retrieveTasksOfProgramOrProject(context, sOIDProject, "", sOwner, "");
                  
            for (int j = 0; j < mlTasks.size(); j++) {

                Map mTask           = (Map) mlTasks.get(j);
                String sIsAssigned  = (String)mTask.get(SELECT_RELATIONSHIP_ASSIGNED_TASKS);
                                                                                                                   
                if(sIsAssigned.equalsIgnoreCase("TRUE")) {
                            
                    if (mTask.get(SELECT_RELATIONSHIP_ASSIGNED_TASKS_FROM_NAME) instanceof StringList) {
		
                        StringList slAssignees = (StringList)mTask.get(SELECT_RELATIONSHIP_ASSIGNED_TASKS_FROM_NAME);
                        for(int k = 0; k < slAssignees.size(); k++) {
                            String sAssignee = (String)slAssignees.get(k);
                            Map mAssignee      = new HashMap();
                            mAssignee.put("assignee", sAssignee);
                            if(!mlResult.contains(mAssignee)) {
                                mlResult.add(mAssignee);
                            }  
                        }
                    
                    } else {
                        
                        String sAssignee = (String)mTask.get(SELECT_RELATIONSHIP_ASSIGNED_TASKS_FROM_NAME);
                        Map mAssignee      = new HashMap();
                        mAssignee.put("assignee", sAssignee);
                        if(!mlResult.contains(mAssignee)) {
                            mlResult.add(mAssignee);
                        }
                        
                    }
                }
            }
        }        
        
        if (mlResult.size() > 0) {        
            for (int i = 0; i < mlResult.size(); i++) {  
                Map mResult = (Map)mlResult.get(i);
                String sAssignee = (String)mResult.get("assignee");
                com.matrixone.apps.common.Person pOwner = com.matrixone.apps.common.Person.getPerson(context, sAssignee);
                mResult.put(DomainObject.SELECT_ID, pOwner.getObjectId());
            }      
        }
        
        return mlResult;

    }       
    public MapList getActiveProjects(Context context) throws Exception {
        
        String[] init       = new String[]{};
        String[] methodargs = new String[2];
        HashMap argsMap     = new HashMap();
        
        argsMap.put("objectId", "");
        methodargs = JPO.packArgs(argsMap); 
        
        return (com.matrixone.apps.domain.util.MapList) JPO.invoke(context, "emxProjectSpace", init, "getActiveProjects", methodargs, com.matrixone.apps.domain.util.MapList.class);		
    }    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getUsersTasksPending(Context context, String[] args) throws Exception {
   
        
        MapList mlResult                        = new MapList();
        MapList mlProjects                      = new MapList();
        HashMap programMap                      = (HashMap) JPO.unpackArgs(args);
        String sOID                             = (String) programMap.get("objectId");
        String sParentOID                       = (String) programMap.get("rootOID");
        String sMode                            = (String) programMap.get("mode");
        com.matrixone.apps.common.Person pOwner = new com.matrixone.apps.common.Person(sOID);
        String sOwner                           = pOwner.getInfo(context, DomainObject.SELECT_NAME);        
        
        if(null == sParentOID || "".equals(sParentOID)) {                        
            mlProjects = getActiveProjects(context);
        } else {
            Map mObject = new HashMap();
            mObject.put(DomainObject.SELECT_ID, sParentOID);
            mlProjects.add(mObject);
        }       
        
        for (int i = 0; i < mlProjects.size(); i++) {
            Map mProject        = (Map) mlProjects.get(i);
            String sOIDProject  = (String) mProject.get(DomainObject.SELECT_ID);                
            MapList mlTasks     = retrieveTasksOfProgramOrProject(context, sOIDProject, sMode, sOwner, "");
            mlResult.addAll(mlTasks);
        }               
                
        return mlResult;

    }    
    public MapList retrieveTasksOfProgramOrProject(Context context, String sOID, String sSchedule, String sOwner, String sAssignee) throws Exception {
        
        
        MapList mlResult        = new MapList();
        DomainObject dObject    = new DomainObject(sOID);
        Pattern pTypes          = new Pattern("Task");
        StringList busSelects   = new StringList();
        StringList relSelects   = new StringList();
        Calendar cal            = Calendar.getInstance(TimeZone.getDefault());
        Calendar cCurrent       = Calendar.getInstance();
        int iYearCurrent        = cCurrent.get(Calendar.YEAR);
        int iMonthCurrent       = cCurrent.get(Calendar.MONTH);
        int iWeekCurrent        = cCurrent.get(Calendar.WEEK_OF_YEAR);        
        long lCurrent           = cCurrent.getTimeInMillis();
        long lDiff              = 2592000000L;  
        
        busSelects.add(DomainObject.SELECT_TYPE);
        busSelects.add(DomainObject.SELECT_OWNER);
        busSelects.add(DomainObject.SELECT_CURRENT);
        busSelects.add(DomainObject.SELECT_ID);
        busSelects.add(Task.SELECT_TASK_ESTIMATED_DURATION);
        busSelects.add(Task.SELECT_TASK_ESTIMATED_START_DATE);
        busSelects.add(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
        busSelects.add(Task.SELECT_TASK_ACTUAL_DURATION);
        busSelects.add(Task.SELECT_TASK_ACTUAL_START_DATE);
        busSelects.add(Task.SELECT_TASK_ACTUAL_FINISH_DATE);        
        busSelects.add(DomainObject.SELECT_PERCENTCOMPLETE);
        busSelects.add(SELECT_RELATIONSHIP_ASSIGNED_TASKS);
        busSelects.add(SELECT_RELATIONSHIP_ASSIGNED_TASKS_FROM_NAME);
        busSelects.add(SELECT_FROM_SUBTASK);
        relSelects.add(DomainObject.SELECT_RELATIONSHIP_ID);
        
        MapList mlTasks = dObject.getRelatedObjects(context, "Program Project,Subtask", "Project Space,Task Management", busSelects, relSelects, false, true, (short)0, "" ,"", 0, pTypes, null, null);
        
        if (mlTasks.size() > 0) {
            
            for (int i = 0; i < mlTasks.size(); i++) {
                
                Map mTask           = (Map) mlTasks.get(i);
                String sHasSubtasks = (String) mTask.get(SELECT_FROM_SUBTASK);
                
                if(sHasSubtasks.equalsIgnoreCase("FALSE")) {
   
                    Boolean bAdd = true;
                                        
                    // Filter if owner has been defined
                    if(!sOwner.equals("")) {
                        String sTaskOwner = (String)mTask.get(DomainObject.SELECT_OWNER); 
                        if(!sTaskOwner.equals(sOwner)) {
                            bAdd = false;
                        }
                    }
                    
                    // Filter for time schedule
                    if(bAdd) {
                        if(!sSchedule.equals("")) {
                            String sCurrent = (String)mTask.get(DomainObject.SELECT_CURRENT);                             
                            if (sCurrent.equals("Complete")) {
                                bAdd = false;
                            } else {
                                
                                if (sSchedule.equals("Overdue")) {
                                    String sTargetDate = (String) mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                                    if (sTargetDate != null && !"".equals(sTargetDate)) {
                                        Calendar cTarget = Calendar.getInstance();
                                        cTarget.setTime(sdf.parse(sTargetDate));
                                        if (cTarget.after(cal)) {
                                            bAdd = false;
                                        }
                                    }                                
                                } else if (sSchedule.equals("This Week")) {
                                    String sTargetDate = (String) mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                                    if (sTargetDate != null && !"".equals(sTargetDate)) {

                                        Calendar cTarget = Calendar.getInstance();
                                        cTarget.setTime(sdf.parse(sTargetDate));

                                        int iYearTarget = cTarget.get(Calendar.YEAR);
                                        int iWeekTarget = cTarget.get(Calendar.WEEK_OF_YEAR);

                                        bAdd = false;
                                        if (iYearCurrent == iYearTarget) {
                                            if (iWeekCurrent == iWeekTarget) {
                                                bAdd = true;
                                            }
                                        }
                                    }
                                } else if (sSchedule.equals("This Month")) {
                                    String sTargetDate = (String) mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                                    if (sTargetDate != null && !"".equals(sTargetDate)) {

                                        Calendar cTarget = Calendar.getInstance();
                                        cTarget.setTime(sdf.parse(sTargetDate));

                                        int iYearTarget = cTarget.get(Calendar.YEAR);
                                        int iMonthTarget = cTarget.get(Calendar.MONTH);

                                        bAdd = false;
                                        if (iYearCurrent == iYearTarget) {
                                            if (iMonthCurrent == iMonthTarget) {
                                                bAdd = true;
                                            }
                                        }
                                    }
                                } else if (sSchedule.equals("Soon")) {
                                    String sTargetDate = (String) mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                                    if (sTargetDate != null && !"".equals(sTargetDate)) {

                                        Calendar cTarget = Calendar.getInstance();
                                        cTarget.setTime(sdf.parse(sTargetDate));
                                        long lTarget        = cTarget.getTimeInMillis();	
                                        bAdd = false;

                                        if ((lTarget - lCurrent) < lDiff) {
                                            if ((lTarget - lCurrent) > 0) {
                                                 bAdd = true;
                                            }	                    
                                        }	    
                                    }
                                }                             
                            }
                        }                                                             
                    }
                    
                    // Filter for task assignment
                    if(bAdd) {
                        if(!sAssignee.equals("")) {      
                            String sIsAssigned = (String)mTask.get(SELECT_RELATIONSHIP_ASSIGNED_TASKS);                             
                            bAdd = false;
                            
                            if (sIsAssigned.equalsIgnoreCase("TRUE")) {
                                if (mTask.get(SELECT_RELATIONSHIP_ASSIGNED_TASKS_FROM_NAME) instanceof StringList) {
		
                                    StringList slAssignees = (StringList)mTask.get(SELECT_RELATIONSHIP_ASSIGNED_TASKS_FROM_NAME);
                                    for(int k = 0; k < slAssignees.size(); k++) {
                                        
                                        String sTaskAssignee = (String)slAssignees.get(k);
                                        if(sTaskAssignee.equals(sAssignee)) {
                                            bAdd = true;
                                            break;
                                        }                                        
                                    }
                    
                                } else {
                        
                                    String sTaskAssignee = (String)mTask.get(SELECT_RELATIONSHIP_ASSIGNED_TASKS_FROM_NAME);
                                    if(sTaskAssignee.equals(sAssignee)) {
                                        bAdd = true;
                                    }                               
                                }
                            }
                        }
                    }
                    
                    if(bAdd) {
                        mTask.remove("level");
                        mlResult.add(mTask);
                    }
                }                
            }
        }
        
        return mlResult;
    }        
    public StringBuffer getDataUsersTasksOwnedDueDate (Context context, String[] args) throws Exception {
        
        
        HashMap paramMap                        = (HashMap) JPO.unpackArgs(args);
        String sOID                             = (String) paramMap.get("objectId");
        String sParentOID                       = (String) paramMap.get("parentOID");
        String sLanguage                            = (String) paramMap.get("languageStr");
        com.matrixone.apps.common.Person pOwner = new com.matrixone.apps.common.Person(sOID);
        String sOwner                           = pOwner.getInfo(context, DomainObject.SELECT_NAME);
          
        StringBuffer sbResult       = new StringBuffer();
        StringBuffer sbResultCreate = new StringBuffer();
        StringBuffer sbResultAssign = new StringBuffer();
        StringBuffer sbResultActive = new StringBuffer();
        StringBuffer sbResultReview = new StringBuffer();        
        
        sbResultCreate.append("name: '").append(EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.State.Project_Task.Create", sLanguage)).append("',   color:'#D6DBDE', data: [");
        sbResultAssign.append("name: '").append(EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.State.Project_Task.Assign", sLanguage)).append("',  color:'#cc0000', data: [");
        sbResultActive.append("name: '").append(EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.State.Project_Task.Active", sLanguage)).append("',    color:'#ff7f00', data: [");
        sbResultReview.append("name: '").append(EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.State.Project_Task.Review", sLanguage)).append("', color:'#009c00', data: [");
        
        MapList mlTasks = new MapList();
        if(null == sParentOID || "".equals(sParentOID)) {
            MapList mlProjects = getActiveProjects(context);
            for(int i = 0; i < mlProjects.size(); i++) {
                Map mProject = (Map)mlProjects.get(i);
                String sOIDProject = (String)mProject.get(DomainObject.SELECT_ID);
                MapList mlTasksProject = retrieveTasksOfProgramOrProject(context, sOIDProject, "All", sOwner, "");
                mlTasks.addAll(mlTasksProject);
            }
        } else {
            mlTasks = retrieveTasksOfProgramOrProject(context, sParentOID, "All", sOwner, "");
        }
        
        mlTasks.sort(Task.SELECT_TASK_ESTIMATED_FINISH_DATE, "ascending", "date");
        
        if(mlTasks.size() > 0) {
        
            Map mTask                   = (Map)mlTasks.get(0);
            String sCurrent             = (String)mTask.get(DomainObject.SELECT_CURRENT);
            String sDateTimePrevious    = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
            String sDatePrevious        = sDateTimePrevious.substring(0, sDateTimePrevious.indexOf(" "));
            
            int iCountCreate = 0;
            int iCountAssign = 0;
            int iCountActive = 0;
            int iCountReview = 0;
            
            if(sCurrent.equals("Create"))       { iCountCreate++; }
            else if(sCurrent.equals("Assign"))  { iCountAssign++; }
            else if(sCurrent.equals("Active"))  { iCountActive++; }
            else if(sCurrent.equals("Review"))  { iCountReview++; }
        
            for(int i = 1; i < mlTasks.size(); i++) {
                mTask = (Map)mlTasks.get(i);
                sCurrent = (String)mTask.get(DomainObject.SELECT_CURRENT);
                String sDateTime = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                String sDate = sDateTime.substring(0, sDateTime.indexOf(" "));
                if(sDate.equals(sDatePrevious)) { 
                    if(sCurrent.equals("Create"))       { iCountCreate++; }
                    else if(sCurrent.equals("Assign"))  { iCountAssign++; }
                    else if(sCurrent.equals("Active"))  { iCountActive++; }
                    else if(sCurrent.equals("Review"))  { iCountReview++; }
                } else {
                    Calendar cCreation = Calendar.getInstance();
                    cCreation.setTime(sdf.parse(sDateTimePrevious)); 
                    int iDay 	= cCreation.get(Calendar.DAY_OF_MONTH);
                    int iMonth 	= cCreation.get(Calendar.MONTH);
                    int iYear 	= cCreation.get(Calendar.YEAR);
                    
                    sbResultCreate.append("[Date.UTC(").append(iYear).append(",").append(iMonth).append(",").append(iDay).append("), ").append(iCountCreate).append("],");
                    sbResultAssign.append("[Date.UTC(").append(iYear).append(",").append(iMonth).append(",").append(iDay).append("), ").append(iCountAssign).append("],");
                    sbResultActive.append("[Date.UTC(").append(iYear).append(",").append(iMonth).append(",").append(iDay).append("), ").append(iCountActive).append("],");
                    sbResultReview.append("[Date.UTC(").append(iYear).append(",").append(iMonth).append(",").append(iDay).append("), ").append(iCountReview).append("],");                    

                    iCountCreate = 0;
                    iCountAssign = 0;
                    iCountActive = 0;
                    iCountReview = 0;
            
                    if(sCurrent.equals("Create"))       { iCountCreate++; }
                    else if(sCurrent.equals("Assign"))  { iCountAssign++; }
                    else if(sCurrent.equals("Active"))  { iCountActive++; }
                    else if(sCurrent.equals("Review"))  { iCountReview++; }
            
                    sDateTimePrevious = sDateTime;
                    sDatePrevious = sDate;
                }
		if (i == (mlTasks.size() - 1)) { 			
                    Calendar cCreation = Calendar.getInstance();
                    cCreation.setTime(sdf.parse(sDateTime)); 
                    int iDay 	= cCreation.get(Calendar.DAY_OF_MONTH);
                    int iMonth 	= cCreation.get(Calendar.MONTH);
                    int iYear 	= cCreation.get(Calendar.YEAR); 
				
                    sbResultCreate.append("[Date.UTC(").append(iYear).append(",").append(iMonth).append(",").append(iDay).append("), ").append(iCountCreate).append("]");
                    sbResultAssign.append("[Date.UTC(").append(iYear).append(",").append(iMonth).append(",").append(iDay).append("), ").append(iCountAssign).append("]");
                    sbResultActive.append("[Date.UTC(").append(iYear).append(",").append(iMonth).append(",").append(iDay).append("), ").append(iCountActive).append("]");
                    sbResultReview.append("[Date.UTC(").append(iYear).append(",").append(iMonth).append(",").append(iDay).append("), ").append(iCountReview).append("]"); 
		
		}                     
            }            
        }

        sbResultCreate.append("]");     
        sbResultAssign.append("]");     
        sbResultActive.append("]");     
        sbResultReview.append("]");     
        
        sbResult.append(sbResultCreate);
        sbResult.append("},{");        
        sbResult.append(sbResultAssign);
        sbResult.append("},{");
        sbResult.append(sbResultActive);
        sbResult.append("},{");
        sbResult.append(sbResultReview);
        
        return sbResult;
        
    }     
    public StringBuffer getDataUsersTasksOwnedSchedule(Context context, String[] args) throws Exception {
        
        
        HashMap paramMap                        = (HashMap) JPO.unpackArgs(args);
        String sOID                             = (String) paramMap.get("objectId");
        String sParentOID                       = (String) paramMap.get("parentOID");
        com.matrixone.apps.common.Person pOwner = new com.matrixone.apps.common.Person(sOID);
        String sOwner                           = pOwner.getInfo(context, DomainObject.SELECT_NAME);
        
        int[][] aData = new int[4][5];
        for (int i = 0; i < 4; i++) { for(int j = 0; j < 5; j++) { aData[i][j] = 0; } }
        
        
        MapList mlProjects = new MapList();
        if(null == sParentOID || "".equals(sParentOID)) {
            mlProjects = getActiveProjects(context);
        } else {
            Map mProject = new HashMap();
            mProject.put(DomainObject.SELECT_ID, sParentOID);
            mlProjects.add(mProject);
        }
        for(int i = 0; i < mlProjects.size(); i++) {
            Map mProject                = (Map)mlProjects.get(i);
            String sOIDProject          = (String)mProject.get(DomainObject.SELECT_ID);
            MapList mlTasksThisWeek     = retrieveTasksOfProgramOrProject(context, sOIDProject, "This Week" , sOwner, "");
            MapList mlTasksThisMonth    = retrieveTasksOfProgramOrProject(context, sOIDProject, "This Month", sOwner, "");
            MapList mlTasksSoon         = retrieveTasksOfProgramOrProject(context, sOIDProject, "Soon"      , sOwner, "");
            MapList mlTasksOverdue      = retrieveTasksOfProgramOrProject(context, sOIDProject, "Overdue"   , sOwner, "");
            
            for(int j = 0; j < mlTasksThisWeek.size(); j++) {
                Map mTask = (Map)mlTasksThisWeek.get(j);
                String sPercentComplete = (String)mTask.get(DomainObject.SELECT_PERCENTCOMPLETE);
                double dPercent = Task.parseToDouble(sPercentComplete);
                if(dPercent <= 25.0)        { aData[0][0]++; }
                else if(dPercent <= 50.0)   { aData[0][1]++; }
                else if(dPercent <= 75.0)   { aData[0][2]++; }
                else if(dPercent < 100.0)   { aData[0][3]++; }
                else                        { aData[0][4]++; }
            }
            for(int j = 0; j < mlTasksThisMonth.size(); j++) {
                Map mTask = (Map)mlTasksThisMonth.get(j);
                String sPercentComplete = (String)mTask.get(DomainObject.SELECT_PERCENTCOMPLETE);
                double dPercent = Task.parseToDouble(sPercentComplete);
                if(dPercent <= 25.0)        { aData[1][0]++; }
                else if(dPercent <= 50.0)   { aData[1][1]++; }
                else if(dPercent <= 75.0)   { aData[1][2]++; }
                else if(dPercent < 100.0)   { aData[1][3]++; }
                else                        { aData[1][4]++; }
            }
            for(int j = 0; j < mlTasksSoon.size(); j++) {
                Map mTask = (Map)mlTasksSoon.get(j);
                String sPercentComplete = (String)mTask.get(DomainObject.SELECT_PERCENTCOMPLETE);
                double dPercent = Task.parseToDouble(sPercentComplete);
                if(dPercent <= 25.0)        { aData[2][0]++; }
                else if(dPercent <= 50.0)   { aData[2][1]++; }
                else if(dPercent <= 75.0)   { aData[2][2]++; }
                else if(dPercent < 100.0)   { aData[2][3]++; }
                else                        { aData[2][4]++; }
            }
            for(int j = 0; j < mlTasksOverdue.size(); j++) {
                Map mTask = (Map)mlTasksOverdue.get(j);
                String sPercentComplete = (String)mTask.get(DomainObject.SELECT_PERCENTCOMPLETE);
                double dPercent = Task.parseToDouble(sPercentComplete);
                if(dPercent <= 25.0)        { aData[3][0]++; }
                else if(dPercent <= 50.0)   { aData[3][1]++; }
                else if(dPercent <= 75.0)   { aData[3][2]++; }
                else if(dPercent < 100.0)   { aData[3][3]++; }
                else                        { aData[3][4]++; }
            }            
            
        }    

        StringBuffer sbResult     = new StringBuffer();
        StringBuffer sbResult0025 = new StringBuffer();
        StringBuffer sbResult2550 = new StringBuffer();
        StringBuffer sbResult5075 = new StringBuffer();
        StringBuffer sbResult7599 = new StringBuffer();
        StringBuffer sbResult9900 = new StringBuffer();
        
        sbResult0025.append("color: '#67a7cd', name: '< 25%',     data: [");
        sbResult2550.append("color: '#316e93', name: '25-50%',    data: [");
        sbResult5075.append("color: '#214a63', name: '50-75%',    data: [");
        sbResult7599.append("color: '#182d3a', name: '75-99%',    data: [");
        sbResult9900.append("color: '#ff7f00', name: 'In Review', data: [");
        
        sbResult0025.append(aData[0][0]).append(",").append(aData[1][0]).append(",").append(aData[2][0]).append(",").append(aData[3][0]).append("]");     
        sbResult2550.append(aData[0][1]).append(",").append(aData[1][1]).append(",").append(aData[2][1]).append(",").append(aData[3][1]).append("]");     
        sbResult5075.append(aData[0][2]).append(",").append(aData[1][2]).append(",").append(aData[2][2]).append(",").append(aData[3][2]).append("]");     
        sbResult7599.append(aData[0][3]).append(",").append(aData[1][3]).append(",").append(aData[2][3]).append(",").append(aData[3][3]).append("]");     
        sbResult9900.append(aData[0][4]).append(",").append(aData[1][4]).append(",").append(aData[2][4]).append(",").append(aData[3][4]).append("]");     
          
        sbResult.append(sbResult9900);
        sbResult.append("},{");   
        sbResult.append(sbResult7599);
        sbResult.append("},{");        
        sbResult.append(sbResult5075);
        sbResult.append("},{");
        sbResult.append(sbResult2550);
        sbResult.append("},{");
        sbResult.append(sbResult0025);
        
        return sbResult;
        
    }     
    public StringBuffer getDataUsersTasksOwnedStart   (Context context, String[] args) throws Exception {
        

        HashMap paramMap                        = (HashMap) JPO.unpackArgs(args);
        String sOID                             = (String) paramMap.get("objectId");
        String sParentOID                       = (String) paramMap.get("parentOID");
        String sLanguage                        = (String) paramMap.get("languageStr");
        long lDiff                              = 172800000L;
        com.matrixone.apps.common.Person pOwner = new com.matrixone.apps.common.Person(sOID);
        String sOwner                           = pOwner.getInfo(context, DomainObject.SELECT_NAME);
        
        int[] aData = new int[4];
        for (int i = 0; i < 4; i++) { aData[i] = 0; }
        
        MapList mlTasks = new MapList();
        if(null == sParentOID || "".equals(sParentOID)) {
            MapList mlProjects = getActiveProjects(context);
            for(int i = 0; i < mlProjects.size(); i++) {
                Map mProject = (Map)mlProjects.get(i);
                String sOIDProject = (String)mProject.get(DomainObject.SELECT_ID);
                MapList mlTasksProject = retrieveTasksOfProgramOrProject(context, sOIDProject, "", sOwner, "");
                mlTasks.addAll(mlTasksProject);
            }
        } else {
            mlTasks = retrieveTasksOfProgramOrProject(context, sParentOID, "", sOwner, "");
        }        
        
        for(int i = 0; i < mlTasks.size(); i++) {
            
            Map mTask           = (Map)mlTasks.get(i);
            String sTarget      = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_START_DATE);
            String sActual      = (String)mTask.get(Task.SELECT_TASK_ACTUAL_START_DATE);
            Calendar cTarget    = Calendar.getInstance();
            
            cTarget.setTime(sdf.parse(sTarget));
            
            if(null == sActual || "".equals(sActual)) {
                
                Calendar cCurrent = Calendar.getInstance();
                if(cCurrent.after(cTarget)) {
                    aData[3]++;
                } else {
                    aData[0]++;
                }
                
            } else {
                
                Calendar cActual = Calendar.getInstance();                
                cActual.setTime(sdf.parse(sActual));
                long lTarget        = cTarget.getTimeInMillis();
                long lActual        = cActual.getTimeInMillis();
                if(cTarget.after(cActual)) {
                    if((lTarget - lActual) < lDiff) {
                        aData[2]++;
                    } else {
                        aData[1]++;
                    }
                } else {
                    if((lActual - lTarget) < lDiff) {
                        aData[2]++;
                    } else {
                        aData[3]++;
                    }                    
                }                  
            }            
        }

        StringBuffer sbResult = new StringBuffer();           
        
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.NotStarted", sLanguage)).append("', y:").append(aData[0]).append(", header:'OwnedTasksNotStarted'    , filter: 'Not Started'   },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.Earlier", sLanguage)).append("', y:").append(aData[1]).append(", header:'OwnedTasksStartedEarly'  , filter: 'Early'         },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.OnTime", sLanguage)).append("', y:").append(aData[2]).append(", header:'OwnedTasksStartedOnTime' , filter: 'On Time'       },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Late", sLanguage)).append("', y:").append(aData[3]).append(", header:'OwnedTasksStartedLate'   , filter: 'Late'          } ");        
        
        
        return sbResult;
        
    }         
    public StringBuffer getDataUsersTasksOwnedFinish  (Context context, String[] args) throws Exception {
        
        
        HashMap programMap                        = (HashMap) JPO.unpackArgs(args);
        String sOID                             = (String) programMap.get("objectId");
        String sParentOID                       = (String) programMap.get("parentOID");
        String sLanguage                        = (String) programMap.get("languageStr");        
        long lDiff                              = 172800000L;
        com.matrixone.apps.common.Person pOwner = new com.matrixone.apps.common.Person(sOID);
        String sOwner                           = pOwner.getInfo(context, DomainObject.SELECT_NAME);
        MapList mlTasks                         = new MapList();

        int[] aData = new int[4];
        for (int i = 0; i < 4; i++) { aData[i] = 0; }
                
        if(null == sParentOID || "".equals(sParentOID)) {
            MapList mlProjects = getActiveProjects(context);
            for(int i = 0; i < mlProjects.size(); i++) {
                Map mProject = (Map)mlProjects.get(i);
                String sOIDProject = (String)mProject.get(DomainObject.SELECT_ID);
                MapList mlTasksProject = retrieveTasksOfProgramOrProject(context, sOIDProject, "", sOwner, "");
                mlTasks.addAll(mlTasksProject);
            }
        } else {
            mlTasks = retrieveTasksOfProgramOrProject(context, sParentOID, "", sOwner, "");
        }        
 
        for(int i = 0; i < mlTasks.size(); i++) {
           
            Map mTask           = (Map)mlTasks.get(i);
            String sTarget      = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
            String sActual      = (String)mTask.get(Task.SELECT_TASK_ACTUAL_FINISH_DATE);
            Calendar cTarget    = Calendar.getInstance();
  
            cTarget.setTime(sdf.parse(sTarget));
            
            if(null == sActual || "".equals(sActual)) {
                
                Calendar cCurrent = Calendar.getInstance();
                if(cCurrent.after(cTarget)) {
                    aData[3]++;
                } else {
                    aData[0]++;
                }
                
            } else {
                
                Calendar cActual = Calendar.getInstance();                
                cActual.setTime(sdf.parse(sActual));            
                long lTarget    = cTarget.getTimeInMillis();
                long lActual    = cActual.getTimeInMillis();
                
                if(cTarget.after(cActual)) {                  
                    if((lTarget - lActual) <= lDiff) { aData[2]++;
                    } else { aData[1]++; }
                } else {
                    if((lActual - lTarget) <= lDiff) { aData[2]++;
                    } else { aData[3]++; }                    
                }  
                
            }            
        }

        StringBuffer sbResult = new StringBuffer();                 
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.NotFinished", sLanguage)).append("', y:").append(aData[0]).append(", header:'OwnedTasksNotFinished'    , filter: 'Not Finished'  },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.Earlier", sLanguage)).append("', y:").append(aData[1]).append(", header:'OwnedTasksFinishedEarly'  , filter: 'Early'         },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.OnTime", sLanguage)).append("', y:").append(aData[2]).append(", header:'OwnedTasksFinishedOnTime' , filter: 'On Time'       },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Late", sLanguage)).append("', y:").append(aData[3]).append(", header:'OwnedTasksFinishedLate'   , filter: 'Late'          } ");           
        
        return sbResult;
        
    }    
    public StringBuffer getDataUsersTasksOwnedDuration(Context context, String[] args) throws Exception {
        
        
        HashMap paramMap                        = (HashMap) JPO.unpackArgs(args);
        String sOID                             = (String) paramMap.get("objectId");
        String sParentOID                       = (String) paramMap.get("parentOID");
        String sLanguage                        = (String) paramMap.get("languageStr");
        com.matrixone.apps.common.Person pOwner = new com.matrixone.apps.common.Person(sOID);
        String sOwner                           = pOwner.getInfo(context, DomainObject.SELECT_NAME);
        MapList mlTasks                         = new MapList();
        
        int[] aData = new int[5];
        for (int i = 0; i < 5; i++) { aData[i] = 0; }
                
        if(null == sParentOID || "".equals(sParentOID)) {
            MapList mlProjects = getActiveProjects(context);
            for(int i = 0; i < mlProjects.size(); i++) {
                Map mProject = (Map)mlProjects.get(i);
                String sOIDProject = (String)mProject.get(DomainObject.SELECT_ID);
                MapList mlTasksProject = retrieveTasksOfProgramOrProject(context, sOIDProject, "", sOwner, "");
                mlTasks.addAll(mlTasksProject);
            }
        } else {
            mlTasks = retrieveTasksOfProgramOrProject(context, sParentOID, "", sOwner, "");
        }        
        
        for(int i = 0; i < mlTasks.size(); i++) {
           
            Map mTask       = (Map)mlTasks.get(i);
            String sTarget  = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_DURATION);
            String sActual  = (String)mTask.get(Task.SELECT_TASK_ACTUAL_DURATION);
            String sFinish  = (String)mTask.get(Task.SELECT_TASK_ACTUAL_FINISH_DATE);
            
            if(null != sFinish && !"".equals(sFinish)) {
                
                double dTarget  = Task.parseToDouble(sTarget);
                double dActual  = Task.parseToDouble(sActual);
                double dDev     = dTarget - dActual;
                
                if(dDev > 3.0) { aData[0]++; }
                else if(dDev > 1.0) { aData[1]++; }
                else if(dDev > -1.0) { aData[2]++; }
                else if(dDev > -3.0) { aData[3]++; }
                else { aData[4]++; }
            }            
        }        

        StringBuffer sbResult = new StringBuffer();       
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.MuchLess", sLanguage)).append("', y:").append(aData[0]).append(", header:'OwnedTasksDurationMuchLess'  , filter: 'Much Less'   },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.3DaysLess", sLanguage)).append("', y:").append(aData[1]).append(", header:'OwnedTasksDuration3DaysLess' , filter: '3 Days Less' },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.AsPlanned", sLanguage)).append("', y:").append(aData[2]).append(", header:'OwnedTasksDurationAsPlanned' , filter: 'As Planned'  },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.3DaysMore", sLanguage)).append("', y:").append(aData[3]).append(", header:'OwnedTasksDuration3DaysMore' , filter: '3 Days More' },");            
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.MuchMore", sLanguage)).append("', y:").append(aData[4]).append(", header:'OwnedTasksDurationMuchMore'  , filter: 'Much More'   } ");            
        
        return sbResult;
        
    }  
    public StringBuffer getDataUsersTasksAssignedResourceLoad(Context context, String[] args) throws Exception {
        
        
        HashMap paramMap                        = (HashMap) JPO.unpackArgs(args);
        String sOID                             = (String) paramMap.get("objectId");
        String sParentOID                       = (String) paramMap.get("parentOID");        
        com.matrixone.apps.common.Person pUser  = new com.matrixone.apps.common.Person(sOID);
        String sAssignee                        = pUser.getInfo(context, DomainObject.SELECT_NAME);
        ResourceLoading resourceLoading         = new ResourceLoading (context);
        
        StringBuffer sbBlu      = new StringBuffer();        
        StringBuffer sbOra      = new StringBuffer();        
        StringBuffer sbRed      = new StringBuffer();        
        StringBuffer sbResult   = new StringBuffer();        
        sbBlu.append("{ name: 'Safe',     color:'#336699', data: [");
        sbOra.append("{ name: 'Limit',    color:'#ff7f00', data: [");
        sbRed.append("{ name: 'Exceeded', color:'#cc0000', data: [");
        
        MapList mlTasks = new MapList();
        if(null == sParentOID || "".equals(sParentOID)) {
            MapList mlProjects = getActiveProjects(context);
            for(int i = 0; i < mlProjects.size(); i++) {
                Map mProject = (Map)mlProjects.get(i);
                String sOIDProject = (String)mProject.get(DomainObject.SELECT_ID);
                MapList mlTasksProject = retrieveTasksOfProgramOrProject(context, sOIDProject, "All", "", sAssignee);
                mlTasks.addAll(mlTasksProject);
            }
        } else {
            mlTasks = retrieveTasksOfProgramOrProject(context, sParentOID, "All", "", sAssignee);
        }
        
        mlTasks.sort(Task.SELECT_TASK_ESTIMATED_FINISH_DATE, "ascending", "date");        
        
        if(mlTasks.size() > 1) {
            
            ArrayList al        = new ArrayList();
            Vector vec          = new Vector();
            Map mTaskFirst      = (Map)mlTasks.get(0);
            Map mTaskLast       = (Map)mlTasks.get(mlTasks.size() - 1);            
            String sDateFirst   = (String)mTaskFirst.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
            String sDateLast    = (String)mTaskLast.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);            
            Date dStart         = sdf.parse(sDateFirst);
            Date dEnd           = sdf.parse(sDateLast);           
            Date dStartAdjusted = resourceLoading.adjustWeeklyStartDate(dStart);
            Date dEndAdjusted   = resourceLoading.adjustWeeklyStartDate(dEnd);            
            al                  = resourceLoading.getWeeklyData(dStartAdjusted, dEndAdjusted); 
             
            if(al.size() == 0) { return sbResult; }
              
            com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
            SimpleDateFormat sdfValue  = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
            
            for(int i = 0; i < mlTasks.size(); i++) {

                Map mTask               = (Map)mlTasks.get(i);
                String sTaskOID         = (String)mTask.get(DomainObject.SELECT_ID);
                String sTaskStart       = (String)mTask.get(task.SELECT_TASK_ESTIMATED_START_DATE);
                String sTaskEnd         = (String)mTask.get(task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                String sTaskDuration    = (String)mTask.get(task.SELECT_TASK_ESTIMATED_DURATION);

                Vector taskVec = resourceLoading.getAllocation(context, sTaskOID, DomainConstants.RELATIONSHIP_ASSIGNED_TASKS, "Person", true, false, (short)1, true);

                task.setId(sTaskOID);

                StringList busTaskSelects = new StringList(1);
                busTaskSelects.add(task.SELECT_ID);

                String attribute_ProjectVisibility="Project Visibility";
                StringList busProjSelects = new StringList();
                busProjSelects.addElement(DomainObject.SELECT_ID);
                busProjSelects.addElement("attribute[" + attribute_ProjectVisibility + "]");

                Map projMap             = (Map) task.getProject(context, busProjSelects);
                String projID           = (String)projMap.get(task.SELECT_ID);
                String projVisibility   = (String)projMap.get("attribute[" + attribute_ProjectVisibility + "]");

                //if visibility is set to members and the person is not a member of the project
                //then do not include this task in the report.

                boolean checkmembership = false;
                MapList ml              = (MapList)resourceLoading.getAssigneeInfo(context, sTaskOID, true);
                String pid              = null;
                Iterator mListItr       = ml.iterator();
                
                while(mListItr.hasNext()){
                    pid = (String) ( ( (Map)mListItr.next() ).get(DomainConstants.SELECT_ID) );
                    if(sOID.equals(pid) && pid != null && pid.length() != 0){
                        checkmembership = true;
                        break;
                    } else {
                        checkmembership = resourceLoading.isPersonProjectMember(context,projID,sOID);
                    }
                }
                
                if(projVisibility != null && projVisibility.trim().length()>0 && projVisibility.equalsIgnoreCase("Members") && !checkmembership) {
                    continue;
                }

                String totalAssingees   = "" + ml.size();
                String userCalendarId   = resourceLoading.getUserCalendar(context,sOID);
                String alloc_value      = resourceLoading.getAllocValue(taskVec,sOID);
                ArrayList hmInfo        = new ArrayList();
                
                if (userCalendarId != null && !"".equals(userCalendarId) && !"null".equals(userCalendarId)) {
                    Date sttDate = new Date(sTaskStart);
                    Date eddDate = new Date(sTaskEnd);
                    hmInfo = resourceLoading.getDaysInRange(context, userCalendarId, sttDate,eddDate,1);
                 }
                 else{
                    hmInfo = resourceLoading.getDateRangeData(sTaskStart, sTaskEnd, true);
                 }

                Map mData = new HashMap();
                mData.put("taskid", sTaskOID);
                mData.put("taskDays", hmInfo);
                mData.put("duration", sTaskDuration);
                mData.put("assignees", totalAssingees);
                mData.put("allocation", alloc_value);
                vec.add(mData);
              
            }
            
            HashMap top = new HashMap();
            top.put("personid", sOID);
            top.put("fullname", sAssignee);
            top.put("data",vec);            
            
            for(int w=0; w<al.size(); w++){

                Date weekItem           = (Date)al.get(w);
                int MILLIS_IN_DAY       = 1000 * 60 * 60 * 24;
                Date week_date_end      = new Date( weekItem.getTime() + (6*MILLIS_IN_DAY));
                ArrayList daysofweek    = resourceLoading.getAvailableDates(context, weekItem, week_date_end, sOID, 1);                
                String sWeeklyLoad      = "";
                
                try {
                sWeeklyLoad      = (String) resourceLoading.getWeeklyResourceLoading(top,daysofweek,false);
                } catch (Exception e) { return sbResult; }                                                
                
                if(!sWeeklyLoad.equals("")) {
                                
                    Double dBlu             = 0.0;
                    Double dOra             = 0.0;
                    Double dRed             = 0.0;

                    String sValue = sWeeklyLoad.replace("</font>", "");

                    if(sValue.contains(">")) {
                        sValue = sValue.substring(sValue.indexOf(">") + 1);
                    }

                    Double dValue   = Task.parseToDouble(sValue);
                    dValue          = dValue * 100;

                    if(sWeeklyLoad.contains("red"))         { dRed = dValue; }
                    else if(sWeeklyLoad.contains("orange")) { dOra = dValue; } 
                    else                                    { dBlu = dValue; }

                    Calendar cValue = Calendar.getInstance();
                    cValue.setTime(sdfValue.parse(al.get(w).toString()));

                    int iDay 	= cValue.get(Calendar.DAY_OF_MONTH);
                    int iMonth 	= cValue.get(Calendar.MONTH);
                    int iYear 	= cValue.get(Calendar.YEAR);  

                    sbBlu.append("[Date.UTC(" + iYear + "," + iMonth + "," + iDay); 
                    sbBlu.append("), "); 
                    sbBlu.append(dBlu); 
                    sbBlu.append("],"); 

                    sbOra.append("[Date.UTC(" + iYear + "," + iMonth + "," + iDay); 
                    sbOra.append("), "); 
                    sbOra.append(dOra); 
                    sbOra.append("],"); 

                    sbRed.append("[Date.UTC(" + iYear + "," + iMonth + "," + iDay); 
                    sbRed.append("), "); 
                    sbRed.append(dRed); 
                    sbRed.append("],");  
                
                }
                
            }
        } else {
            return sbResult;
        }
        
        if(sbBlu.length() > 0) { sbBlu.setLength(sbBlu.length() - 1); }
        if(sbOra.length() > 0) { sbOra.setLength(sbOra.length() - 1); }
        if(sbRed.length() > 0) { sbRed.setLength(sbRed.length() - 1); }
        
        sbBlu.append("]}");
        sbOra.append("]}");
        sbRed.append("]}");
        
        sbResult = sbResult.append(sbBlu + "," + sbOra + "," + sbRed);
               
        return sbResult;
        
    } 
    public StringBuffer getDataUsersTasksAssignedDueDate(Context context, String[] args) throws Exception {
        
        
        HashMap paramMap                        = (HashMap) JPO.unpackArgs(args);
        String sOID                             = (String) paramMap.get("objectId");
        String sParentOID                       = (String) paramMap.get("parentOID");
        com.matrixone.apps.common.Person pUser  = new com.matrixone.apps.common.Person(sOID);
        String sAssignee                        = pUser.getInfo(context, DomainObject.SELECT_NAME);
        
        StringBuffer sbResult       = new StringBuffer();
        StringBuffer sbResultCreate = new StringBuffer();
        StringBuffer sbResultAssign = new StringBuffer();
        StringBuffer sbResultActive = new StringBuffer();
        StringBuffer sbResultReview = new StringBuffer();
        
        sbResultCreate.append("name: 'Planned Tasks',   color:'#D6DBDE', data: [");
        sbResultAssign.append("name: 'Assigned Tasks',  color:'#cc0000', data: [");
        sbResultActive.append("name: 'Active Tasks',    color:'#ff7f00', data: [");
        sbResultReview.append("name: 'Tasks In Review', color:'#009c00', data: [");
        
        MapList mlTasks = new MapList();
        if(null == sParentOID || "".equals(sParentOID)) {
            MapList mlProjects = getActiveProjects(context);
            for(int i = 0; i < mlProjects.size(); i++) {
                Map mProject = (Map)mlProjects.get(i);
                String sOIDProject = (String)mProject.get(DomainObject.SELECT_ID);
                MapList mlTasksProject = retrieveTasksOfProgramOrProject(context, sOIDProject, "All", "", sAssignee);
                mlTasks.addAll(mlTasksProject);
            }
        } else {
            mlTasks = retrieveTasksOfProgramOrProject(context, sParentOID, "All", "", sAssignee);
        }

        mlTasks.sort(Task.SELECT_TASK_ESTIMATED_FINISH_DATE, "ascending", "date");
        
        if(mlTasks.size() > 1) {
        
            Map mTask                   = (Map)mlTasks.get(0);
            String sCurrent             = (String)mTask.get(DomainObject.SELECT_CURRENT);
            String sDateTimePrevious    = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
            String sDatePrevious        = sDateTimePrevious.substring(0, sDateTimePrevious.indexOf(" "));
            
            int iCountCreate = 0;
            int iCountAssign = 0;
            int iCountActive = 0;
            int iCountReview = 0;
            
            if(sCurrent.equals("Create"))       { iCountCreate++; }
            else if(sCurrent.equals("Assign"))  { iCountAssign++; }
            else if(sCurrent.equals("Active"))  { iCountActive++; }
            else if(sCurrent.equals("Review"))  { iCountReview++; }
        
            for(int i = 1; i < mlTasks.size(); i++) {
                mTask = (Map)mlTasks.get(i);
                
                
                sCurrent = (String)mTask.get(DomainObject.SELECT_CURRENT);
                String sDateTime = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                String sDate = sDateTime.substring(0, sDateTime.indexOf(" "));
                if(sDate.equals(sDatePrevious)) { 
                    if(sCurrent.equals("Create"))       { iCountCreate++; }
                    else if(sCurrent.equals("Assign"))  { iCountAssign++; }
                    else if(sCurrent.equals("Active"))  { iCountActive++; }
                    else if(sCurrent.equals("Review"))  { iCountReview++; }
                } else {
                    Calendar cCreation = Calendar.getInstance();
                    cCreation.setTime(sdf.parse(sDateTimePrevious)); 
                    int iDay 	= cCreation.get(Calendar.DAY_OF_MONTH);
                    int iMonth 	= cCreation.get(Calendar.MONTH);
                    int iYear 	= cCreation.get(Calendar.YEAR);
                    
                    sbResultCreate.append("[Date.UTC(").append(iYear).append(",").append(iMonth).append(",").append(iDay).append("), ").append(iCountCreate).append("],");
                    sbResultAssign.append("[Date.UTC(").append(iYear).append(",").append(iMonth).append(",").append(iDay).append("), ").append(iCountAssign).append("],");
                    sbResultActive.append("[Date.UTC(").append(iYear).append(",").append(iMonth).append(",").append(iDay).append("), ").append(iCountActive).append("],");
                    sbResultReview.append("[Date.UTC(").append(iYear).append(",").append(iMonth).append(",").append(iDay).append("), ").append(iCountReview).append("],");                    

                    iCountCreate = 0;
                    iCountAssign = 0;
                    iCountActive = 0;
                    iCountReview = 0;
            
                    if(sCurrent.equals("Create"))       { iCountCreate++; }
                    else if(sCurrent.equals("Assign"))  { iCountAssign++; }
                    else if(sCurrent.equals("Active"))  { iCountActive++; }
                    else if(sCurrent.equals("Review"))  { iCountReview++; }
            
                    sDateTimePrevious = sDateTime;
                    sDatePrevious = sDate;
                }
		if (i == (mlTasks.size() - 1)) { 			
                    Calendar cCreation = Calendar.getInstance();
                    cCreation.setTime(sdf.parse(sDateTime)); 
                    int iDay 	= cCreation.get(Calendar.DAY_OF_MONTH);
                    int iMonth 	= cCreation.get(Calendar.MONTH);
                    int iYear 	= cCreation.get(Calendar.YEAR); 
				
                    sbResultCreate.append("[Date.UTC(").append(iYear).append(",").append(iMonth).append(",").append(iDay).append("), ").append(iCountCreate).append("]");
                    sbResultAssign.append("[Date.UTC(").append(iYear).append(",").append(iMonth).append(",").append(iDay).append("), ").append(iCountAssign).append("]");
                    sbResultActive.append("[Date.UTC(").append(iYear).append(",").append(iMonth).append(",").append(iDay).append("), ").append(iCountActive).append("]");
                    sbResultReview.append("[Date.UTC(").append(iYear).append(",").append(iMonth).append(",").append(iDay).append("), ").append(iCountReview).append("]"); 
		
		}                     
            }                 
        } else { return sbResult; }

        sbResultCreate.append("]");     
        sbResultAssign.append("]");     
        sbResultActive.append("]");     
        sbResultReview.append("]");     
        
        sbResult.append(sbResultCreate);
        sbResult.append("},{");        
        sbResult.append(sbResultAssign);
        sbResult.append("},{");
        sbResult.append(sbResultActive);
        sbResult.append("},{");
        sbResult.append(sbResultReview);
               
        return sbResult;
        
    }     
    public StringBuffer getDataUsersTasksAssignedSchedule(Context context, String[] args) throws Exception {
        
        
        HashMap paramMap                        = (HashMap) JPO.unpackArgs(args);
        String sOID                             = (String) paramMap.get("objectId");
        String sParentOID                       = (String) paramMap.get("parentOID");
        com.matrixone.apps.common.Person pUser  = new com.matrixone.apps.common.Person(sOID);
        String sAssignee                        = pUser.getInfo(context, DomainObject.SELECT_NAME);
        
        int[][] aData = new int[4][5];
        for (int i = 0; i < 4; i++) { for(int j = 0; j < 5; j++) { aData[i][j] = 0; } }
        
        
        MapList mlProjects = new MapList();
        if(null == sParentOID || "".equals(sParentOID)) {
            mlProjects = getActiveProjects(context);
        } else {
            Map mProject = new HashMap();
            mProject.put(DomainObject.SELECT_ID, sParentOID);
            mlProjects.add(mProject);
        }
        for(int i = 0; i < mlProjects.size(); i++) {
            Map mProject                = (Map)mlProjects.get(i);
            String sOIDProject          = (String)mProject.get(DomainObject.SELECT_ID);
            MapList mlTasksThisWeek     = retrieveTasksOfProgramOrProject(context, sOIDProject, "This Week", "", sAssignee);
            MapList mlTasksThisMonth    = retrieveTasksOfProgramOrProject(context, sOIDProject, "This Month", "", sAssignee);
            MapList mlTasksSoon         = retrieveTasksOfProgramOrProject(context, sOIDProject, "Soon", "", sAssignee);
            MapList mlTasksOverdue      = retrieveTasksOfProgramOrProject(context, sOIDProject, "Overdue", "", sAssignee);
            
            for(int j = 0; j < mlTasksThisWeek.size(); j++) {
                Map mTask = (Map)mlTasksThisWeek.get(j);
                String sPercentComplete = (String)mTask.get(DomainObject.SELECT_PERCENTCOMPLETE);
                double dPercent = Task.parseToDouble(sPercentComplete);
                if(dPercent <= 25.0)        { aData[0][0]++; }
                else if(dPercent <= 50.0)   { aData[0][1]++; }
                else if(dPercent <= 75.0)   { aData[0][2]++; }
                else if(dPercent < 100.0)   { aData[0][3]++; }
                else                        { aData[0][4]++; }
            }
            for(int j = 0; j < mlTasksThisMonth.size(); j++) {
                Map mTask = (Map)mlTasksThisMonth.get(j);
                String sPercentComplete = (String)mTask.get(DomainObject.SELECT_PERCENTCOMPLETE);
                double dPercent = Task.parseToDouble(sPercentComplete);
                if(dPercent <= 25.0)        { aData[1][0]++; }
                else if(dPercent <= 50.0)   { aData[1][1]++; }
                else if(dPercent <= 75.0)   { aData[1][2]++; }
                else if(dPercent < 100.0)   { aData[1][3]++; }
                else                        { aData[1][4]++; }
            }
            for(int j = 0; j < mlTasksSoon.size(); j++) {
                Map mTask = (Map)mlTasksSoon.get(j);
                String sPercentComplete = (String)mTask.get(DomainObject.SELECT_PERCENTCOMPLETE);
                double dPercent = Task.parseToDouble(sPercentComplete);
                if(dPercent <= 25.0)        { aData[2][0]++; }
                else if(dPercent <= 50.0)   { aData[2][1]++; }
                else if(dPercent <= 75.0)   { aData[2][2]++; }
                else if(dPercent < 100.0)   { aData[2][3]++; }
                else                        { aData[2][4]++; }
            }
            for(int j = 0; j < mlTasksOverdue.size(); j++) {
                Map mTask = (Map)mlTasksOverdue.get(j);
                String sPercentComplete = (String)mTask.get(DomainObject.SELECT_PERCENTCOMPLETE);
                double dPercent = Task.parseToDouble(sPercentComplete);
                if(dPercent <= 25.0)        { aData[3][0]++; }
                else if(dPercent <= 50.0)   { aData[3][1]++; }
                else if(dPercent <= 75.0)   { aData[3][2]++; }
                else if(dPercent < 100.0)   { aData[3][3]++; }
                else                        { aData[3][4]++; }
            }            
            
        }    

        StringBuffer sbResult     = new StringBuffer();
        StringBuffer sbResult0025 = new StringBuffer();
        StringBuffer sbResult2550 = new StringBuffer();
        StringBuffer sbResult5075 = new StringBuffer();
        StringBuffer sbResult7599 = new StringBuffer();
        StringBuffer sbResult9900 = new StringBuffer();
        
        sbResult0025.append("color: '#").append(sColor025).append("', name: '< 25%',     data: [");
        sbResult2550.append("color: '#").append(sColor050).append("', name: '25-50%',    data: [");
        sbResult5075.append("color: '#").append(sColor075).append("', name: '50-75%',    data: [");
        sbResult7599.append("color: '#").append(sColor099).append("', name: '75-99%',    data: [");
        sbResult9900.append("color: '#").append(sColorOrange).append("', name: 'In Review', data: [");
        
        sbResult0025.append(aData[0][0]).append(",").append(aData[1][0]).append(",").append(aData[2][0]).append(",").append(aData[3][0]).append("]");     
        sbResult2550.append(aData[0][1]).append(",").append(aData[1][1]).append(",").append(aData[2][1]).append(",").append(aData[3][1]).append("]");     
        sbResult5075.append(aData[0][2]).append(",").append(aData[1][2]).append(",").append(aData[2][2]).append(",").append(aData[3][2]).append("]");     
        sbResult7599.append(aData[0][3]).append(",").append(aData[1][3]).append(",").append(aData[2][3]).append(",").append(aData[3][3]).append("]");     
        sbResult9900.append(aData[0][4]).append(",").append(aData[1][4]).append(",").append(aData[2][4]).append(",").append(aData[3][4]).append("]");     
          
        sbResult.append(sbResult9900);
        sbResult.append("},{");   
        sbResult.append(sbResult7599);
        sbResult.append("},{");        
        sbResult.append(sbResult5075);
        sbResult.append("},{");
        sbResult.append(sbResult2550);
        sbResult.append("},{");
        sbResult.append(sbResult0025);
        
        return sbResult;
        
    }     
    public StringBuffer getDataUsersTasksAssignedStart(Context context, String[] args) throws Exception {
        
        
        HashMap paramMap                        = (HashMap) JPO.unpackArgs(args);
        String sOID                             = (String) paramMap.get("objectId");
        String sParentOID                       = (String) paramMap.get("parentOID");
        String sLanguage                        = (String) paramMap.get("languageStr");        
        long lDiff                              = 172800000L;
        com.matrixone.apps.common.Person pUser  = new com.matrixone.apps.common.Person(sOID);
        String sAssignee                        = pUser.getInfo(context, DomainObject.SELECT_NAME);
        
        int[] aData = new int[4];
        for (int i = 0; i < 4; i++) { aData[i] = 0; }
        
        MapList mlTasks = new MapList();
        if(null == sParentOID || "".equals(sParentOID)) {
            MapList mlProjects = getActiveProjects(context);
            for(int i = 0; i < mlProjects.size(); i++) {
                Map mProject = (Map)mlProjects.get(i);
                String sOIDProject = (String)mProject.get(DomainObject.SELECT_ID);
                MapList mlTasksProject = retrieveTasksOfProgramOrProject(context, sOIDProject, "", "", sAssignee);
                mlTasks.addAll(mlTasksProject);
            }
        } else {
            mlTasks = retrieveTasksOfProgramOrProject(context, sParentOID, "", "", sAssignee);
        }        
        
        for(int i = 0; i < mlTasks.size(); i++) {
            
            Map mTask           = (Map)mlTasks.get(i);
            String sTarget      = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_START_DATE);
            String sActual      = (String)mTask.get(Task.SELECT_TASK_ACTUAL_START_DATE);
            Calendar cTarget    = Calendar.getInstance();
            
            cTarget.setTime(sdf.parse(sTarget));
            
            if(null == sActual || "".equals(sActual)) {
                
                Calendar cCurrent = Calendar.getInstance();
                if(cCurrent.after(cTarget)) {
                    aData[3]++;
                } else {
                    aData[0]++;
                }
                
            } else {
                
                Calendar cActual = Calendar.getInstance();                
                cActual.setTime(sdf.parse(sActual));   
                cActual.setTime(sdf.parse(sActual));
                long lTarget        = cTarget.getTimeInMillis();
                long lActual        = cActual.getTimeInMillis();
                
                if(cTarget.after(cActual)) {
                    if((lTarget - lActual) < lDiff) {
                        aData[2]++;
                    } else {
                        aData[1]++;
                    }
                } else {
                    if((lActual - lTarget) < lDiff) {
                        aData[2]++;
                    } else {
                        aData[3]++;
                    }                    
                }                  
            }            
        }

        StringBuffer sbResult = new StringBuffer();         
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.NotStarted", sLanguage)).append("', y:").append(aData[0]).append(", header:'AssignedTasksNotStarted'    , filter: 'Not Started'   },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.Earlier", sLanguage)).append("', y:").append(aData[1]).append(", header:'AssignedTasksStartedEarly'  , filter: 'Early'         },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.OnTime", sLanguage)).append("', y:").append(aData[2]).append(", header:'AssignedTasksStartedOnTime' , filter: 'On Time'       },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Late", sLanguage)).append("', y:").append(aData[3]).append(", header:'AssignedTasksStartedLate'   , filter: 'Late'          } ");        
        
        return sbResult;
        
    }         
    public StringBuffer getDataUsersTasksAssignedFinish(Context context, String[] args) throws Exception {
        
        
        HashMap paramMap                        = (HashMap) JPO.unpackArgs(args);
        String sOID                             = (String) paramMap.get("objectId");
        String sParentOID                       = (String) paramMap.get("parentOID");
        String sLanguage                            = (String) paramMap.get("languageStr");
        long lDiff                              = 172800000L;
        com.matrixone.apps.common.Person pUser  = new com.matrixone.apps.common.Person(sOID);
        String sAssignee                        = pUser.getInfo(context, DomainObject.SELECT_NAME);
        MapList mlTasks                         = new MapList();
        
        int[] aData = new int[4];
        for (int i = 0; i < 4; i++) { aData[i] = 0; }
                
        if(null == sParentOID || "".equals(sParentOID)) {
            MapList mlProjects = getActiveProjects(context);
            for(int i = 0; i < mlProjects.size(); i++) {
                Map mProject = (Map)mlProjects.get(i);
                String sOIDProject = (String)mProject.get(DomainObject.SELECT_ID);
                MapList mlTasksProject = retrieveTasksOfProgramOrProject(context, sOIDProject, "", "", sAssignee);
                mlTasks.addAll(mlTasksProject);
            }
        } else {
            mlTasks = retrieveTasksOfProgramOrProject(context, sParentOID, "", "", sAssignee);
        }        
        
        for(int i = 0; i < mlTasks.size(); i++) {
           
            Map mTask           = (Map)mlTasks.get(i);
            String sTarget      = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
            String sActual      = (String)mTask.get(Task.SELECT_TASK_ACTUAL_FINISH_DATE);
            Calendar cTarget    = Calendar.getInstance();
            
            cTarget.setTime(sdf.parse(sTarget));
            
            if(null == sActual || "".equals(sActual)) {
                
                Calendar cCurrent = Calendar.getInstance();
                if(cCurrent.after(cTarget)) {
                    aData[3]++;
                } else {
                    aData[0]++;
                }
                
            } else {
                
                Calendar cActual = Calendar.getInstance();                
                cActual.setTime(sdf.parse(sActual));   
                long lTarget    = cTarget.getTimeInMillis();
                long lActual    = cActual.getTimeInMillis();
                
                if(cTarget.after(cActual)) {                  
                    if((lTarget - lActual) <= lDiff) { aData[2]++;
                    } else { aData[1]++; }
                } else {
                    if((lActual - lTarget) <= lDiff) { aData[2]++;
                    } else { aData[3]++; }                    
                }                  
            }            
        }
        
        StringBuffer sbResult = new StringBuffer();         
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.NotFinished", sLanguage)).append("', y:").append(aData[0]).append(", header:'AssignedTasksNotFinished'    , filter: 'Not Finished'  },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.Earlier", sLanguage)).append("', y:").append(aData[1]).append(", header:'AssignedTasksFinishedEarly'  , filter: 'Early'         },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.OnTime", sLanguage)).append("', y:").append(aData[2]).append(", header:'AssignedTasksFinishedOnTime' , filter: 'On Time'       },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Late", sLanguage)).append("', y:").append(aData[3]).append(", header:'AssignedTasksFinishedLate'   , filter: 'Late'          } ");           
        
        return sbResult;
        
    }    
    public StringBuffer getDataUsersTasksAssignedDuration(Context context, String[] args) throws Exception {
        
        
        HashMap paramMap                        = (HashMap) JPO.unpackArgs(args);
        String sOID                             = (String) paramMap.get("objectId");
        String sParentOID                       = (String) paramMap.get("parentOID");
        String sLanguage                            = (String) paramMap.get("languageStr");
        com.matrixone.apps.common.Person pUser  = new com.matrixone.apps.common.Person(sOID);
        String sAssignee                        = pUser.getInfo(context, DomainObject.SELECT_NAME);
        MapList mlTasks                         = new MapList();

        int[] aData = new int[5];
        for (int i = 0; i < 5; i++) { aData[i] = 0; }
                
        if(null == sParentOID || "".equals(sParentOID)) {
            MapList mlProjects = getActiveProjects(context);
            for(int i = 0; i < mlProjects.size(); i++) {
                Map mProject = (Map)mlProjects.get(i);
                String sOIDProject = (String)mProject.get(DomainObject.SELECT_ID);
                MapList mlTasksProject = retrieveTasksOfProgramOrProject(context, sOIDProject, "", "", sAssignee);
                mlTasks.addAll(mlTasksProject);
            }
        } else {
            mlTasks = retrieveTasksOfProgramOrProject(context, sParentOID, "", "", sAssignee);
        }        
        
        for(int i = 0; i < mlTasks.size(); i++) {
           
            Map mTask       = (Map)mlTasks.get(i);
            String sTarget  = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_DURATION);
            String sActual  = (String)mTask.get(Task.SELECT_TASK_ACTUAL_DURATION);
            String sFinish  = (String)mTask.get(Task.SELECT_TASK_ACTUAL_FINISH_DATE);
            
            if(null != sFinish && !"".equals(sFinish)) {
                
                double dTarget  = Task.parseToDouble(sTarget);
                double dActual  = Task.parseToDouble(sActual);
                double dDev     = dTarget - dActual;
                
                if(dDev > 3.0) { aData[0]++; }
                else if(dDev > 1.0) { aData[1]++; }
                else if(dDev > -1.0) { aData[2]++; }
                else if(dDev > -3.0) { aData[3]++; }
                else { aData[4]++; }
            }            
        }

        StringBuffer sbResult = new StringBuffer(); 
        
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.MuchLess" , sLanguage)).append("', y:").append(aData[0]).append(", header:'AssignedTasksDurationMuchLess'  , filter: 'Much Less'   },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.3DaysLess", sLanguage)).append("', y:").append(aData[1]).append(", header:'AssignedTasksDuration3DaysLess' , filter: '3 Days Less' },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.AsPlanned", sLanguage)).append("', y:").append(aData[2]).append(", header:'AssignedTasksDurationAsPlanned' , filter: 'As Planned'  },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.3DaysMore", sLanguage)).append("', y:").append(aData[3]).append(", header:'AssignedTasksDuration3DaysMore' , filter: '3 Days More' },");
        sbResult.append("{ name:'").append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.String.MuchMore" , sLanguage)).append("', y:").append(aData[4]).append(", header:'AssignedTasksDurationMuchMore'  , filter: 'Much More'   } ");
        
        return sbResult;
        
    }            
    public static Boolean checkAssignmentCapabilities(Context context, String[] args) throws Exception {

        HashMap requestMap  = (HashMap) JPO.unpackArgs(args);
        String sShow        = (String)requestMap.get("showAssignmentColumn");
        
        if(null != sShow) {
            if(sShow.equalsIgnoreCase("TRUE")) {
               return true;
            }
        }

        return false;
    }        
    public List dynamicColumnsTaskAssignment(Context context, String[] args) throws Exception {

        
        Map paramMap        = (Map) JPO.unpackArgs(args); 
        HashMap requestMap  = (HashMap) paramMap.get("requestMap");
        String sOIDPerson   = (String)requestMap.get("parentOID");          
        MapList mlResult    = new MapList();     
        Map mColumn         = new HashMap();
        HashMap settingsMap = new HashMap();
            
        settingsMap.put("Column Type", "programHTMLOutput");
        settingsMap.put("program", "emxProgramUI");
        settingsMap.put("function", "columnTaskAssignment");
        settingsMap.put("personId", sOIDPerson);
        settingsMap.put("Sortable", "false");
        settingsMap.put("Registered Suite", "ProgramCentral");
            
        mColumn.put(DomainObject.SELECT_NAME, sOIDPerson);                    
        mColumn.put("label", "emxProgramCentral.String.Assignment");
        mColumn.put("expression", DomainObject.SELECT_ID);
        mColumn.put("select", DomainObject.SELECT_ID);
        mColumn.put("settings", settingsMap);
                    
        mlResult.add(mColumn);
                                      
        return mlResult;

    }      
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getsUsersAssignedTasksPending(Context context, String[] args) throws Exception {

        
        MapList mlResult                        = new MapList();
        MapList mlProjects                      = new MapList();
        HashMap paramMap                        = (HashMap) JPO.unpackArgs(args);
        String sOID                             = (String) paramMap.get("objectId");
        String sParentOID                       = (String) paramMap.get("rootOID");
        String sMode                            = (String) paramMap.get("mode");
        com.matrixone.apps.common.Person pUser  = new com.matrixone.apps.common.Person(sOID);
        String sAssignee                        = pUser.getInfo(context, DomainObject.SELECT_NAME);                
        
        if(null == sParentOID || "".equals(sParentOID)) {                        
            mlProjects = getActiveProjects(context);
        } else {
            Map mObject = new HashMap();
            mObject.put(DomainObject.SELECT_ID, sParentOID);
            mlProjects.add(mObject);
        }       
        
        if (mlProjects.size() > 0) {             
            for (int i = 0; i < mlProjects.size(); i++) {                
                Map mProject        = (Map) mlProjects.get(i);
                String sOIDProject  = (String) mProject.get(DomainObject.SELECT_ID);                
                MapList mlTasks     = retrieveTasksOfProgramOrProject(context, sOIDProject, sMode, "", sAssignee);
                mlResult.addAll(mlTasks);
            }
        }
                
        return mlResult;

    }          
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getUsersTasksNotAssigned(Context context, String[] args) throws Exception {
        
        MapList mlResult    = new MapList();
        MapList mlTasksAll  = new MapList();
        MapList mlProjects  = new MapList();
        HashMap paramMap    = (HashMap) JPO.unpackArgs(args);
        String sParentOID   = (String) paramMap.get("rootOID");

        if(null == sParentOID || "".equals(sParentOID)) {                        
            mlProjects = getActiveProjects(context);
        } else {
            Map mObject = new HashMap();
            mObject.put(DomainObject.SELECT_ID, sParentOID);
            mlProjects.add(mObject);
        }       
        
        MapList mlTasks = new MapList();
        if (mlProjects.size() > 0) {             
            for (int i = 0; i < mlProjects.size(); i++) {                
                Map mProject        = (Map) mlProjects.get(i);
                String sOIDProject  = (String) mProject.get(DomainObject.SELECT_ID);                
                mlTasks     = retrieveTasksOfProgramOrProject(context, sOIDProject, "All", context.getUser(), "");
                mlTasksAll.addAll(mlTasks);
            }
        }        

        if(mlTasksAll.size() > 0) {
            for(int i = 0; i < mlTasksAll.size(); i++) {
                Map mResult = (Map)mlTasksAll.get(i);
                String sIsAssigned = (String)mResult.get(SELECT_RELATIONSHIP_ASSIGNED_TASKS);
                if(sIsAssigned.equalsIgnoreCase("FALSE")) {
                    mlResult.add(mResult);
                }
            }
        }
        
        return mlResult;
        
    }    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getUsersTasksAllOwned(Context context, String[] args) throws Exception {
        
        MapList mlResult    = new MapList();
        MapList mlProjects  = new MapList();
        HashMap paramMap    = (HashMap) JPO.unpackArgs(args);
        String sParentOID   = (String) paramMap.get("rootOID");

        if(null == sParentOID || "".equals(sParentOID)) {                        
            mlProjects = getActiveProjects(context);
        } else {
            Map mObject = new HashMap();
            mObject.put(DomainObject.SELECT_ID, sParentOID);
            mlProjects.add(mObject);
        }       
        
        MapList mlTasks = new MapList();
        if (mlProjects.size() > 0) {             
            for (int i = 0; i < mlProjects.size(); i++) {                
                Map mProject        = (Map) mlProjects.get(i);
                String sOIDProject  = (String) mProject.get(DomainObject.SELECT_ID);                
                mlTasks     = retrieveTasksOfProgramOrProject(context, sOIDProject, "All", context.getUser(), "");
                mlResult.addAll(mlTasks);
            }
        }
        
        return mlResult;
        
    }     
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getUsersTasksDetails(Context context, String[] args) throws Exception {

        
        HashMap paramMap    = (HashMap) JPO.unpackArgs(args);
        String sOID         = (String) paramMap.get("personId");
        String sParentOID   = (String) paramMap.get("parentOID");
        String sMode        = (String) paramMap.get("mode");
        String sSource      = (String) paramMap.get("source");        
        
        MapList mlResults = new MapList();
        
        if(sMode.equals("Duration|Much Less"))              {mlResults = retrieveUsersOwnedTasksOfDuration(context, sOID, sParentOID, sSource, 0); }
        else if(sMode.equals("Duration|3 Days Less"))       {mlResults = retrieveUsersOwnedTasksOfDuration(context, sOID, sParentOID, sSource, 1); }
        else if(sMode.equals("Duration|As Planned"))        {mlResults = retrieveUsersOwnedTasksOfDuration(context, sOID, sParentOID, sSource, 2); }
        else if(sMode.equals("Duration|3 Days More"))       {mlResults = retrieveUsersOwnedTasksOfDuration(context, sOID, sParentOID, sSource, 3); }
        else if(sMode.equals("Duration|Much More"))         {mlResults = retrieveUsersOwnedTasksOfDuration(context, sOID, sParentOID, sSource, 4); }
        else if(sMode.equals("Finish|Not Finished"))        {mlResults = retrieveUsersOwnedTasksOfFinishDate(context, sOID, sParentOID, sSource, 0); }
        else if(sMode.equals("Finish|Early"))               {mlResults = retrieveUsersOwnedTasksOfFinishDate(context, sOID, sParentOID, sSource, 1); }
        else if(sMode.equals("Finish|On Time"))             {mlResults = retrieveUsersOwnedTasksOfFinishDate(context, sOID, sParentOID, sSource, 2); }
        else if(sMode.equals("Finish|Late"))                {mlResults = retrieveUsersOwnedTasksOfFinishDate(context, sOID, sParentOID, sSource, 3); }
        else if(sMode.equals("Start|Not Started"))          {mlResults = retrieveUsersOwnedTasksOfStartDate(context, sOID, sParentOID, sSource, 0); }
        else if(sMode.equals("Start|Early"))                {mlResults = retrieveUsersOwnedTasksOfStartDate(context, sOID, sParentOID, sSource, 1); }
        else if(sMode.equals("Start|On Time"))              {mlResults = retrieveUsersOwnedTasksOfStartDate(context, sOID, sParentOID, sSource, 2); }
        else if(sMode.equals("Start|Late"))                 {mlResults = retrieveUsersOwnedTasksOfStartDate(context, sOID, sParentOID, sSource, 3); }        
        
        return mlResults;
    }    
    public MapList retrieveUsersOwnedTasksOfStartDate(Context context, String sOID, String sParentOID, String sSource, int iMode) throws Exception {
        
        
        com.matrixone.apps.common.Person pOwner = new com.matrixone.apps.common.Person(sOID);
        MapList mlTasks                         = new MapList();
        MapList mlResults                       = new MapList();
        String sOwner                           = "";
        String sAssignee                        = "";       
        
        if(sSource.equals(DomainObject.SELECT_OWNER)) {
            sOwner = pOwner.getInfo(context, DomainObject.SELECT_NAME);
        } else {
            sAssignee = pOwner.getInfo(context, DomainObject.SELECT_NAME);
        }
                
        if(null == sParentOID || "".equals(sParentOID)) {
            MapList mlProjects = getActiveProjects(context);
            for(int i = 0; i < mlProjects.size(); i++) {
                Map mProject = (Map)mlProjects.get(i);
                String sOIDProject = (String)mProject.get(DomainObject.SELECT_ID);
                MapList mlTasksProject = retrieveTasksOfProgramOrProject(context, sOIDProject, "", sOwner, sAssignee);
                mlTasks.addAll(mlTasksProject);
            }
        } else {
            mlTasks = retrieveTasksOfProgramOrProject(context, sParentOID, "", sOwner, sAssignee);
        }        

        for(int i = 0; i < mlTasks.size(); i++) {
            
            Map mTask           = (Map)mlTasks.get(i);
            String sTarget      = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_START_DATE);
            String sActual      = (String)mTask.get(Task.SELECT_TASK_ACTUAL_START_DATE);
            Calendar cTarget    = Calendar.getInstance();
            int iResult         = 10;
            
            cTarget.setTime(sdf.parse(sTarget));
            
            if(null == sActual || "".equals(sActual)) {
                
                Calendar cCurrent = Calendar.getInstance();
                if(cCurrent.after(cTarget)) {
                    iResult = 3;
                } else {
                    iResult = 0;                    
                }
                
            } else {
                
                Calendar cActual = Calendar.getInstance();                
                cActual.setTime(sdf.parse(sActual));
                
                if(cActual.after(cTarget)) {
                    iResult = 3;
                } else {
                    
                    iResult = 1;
                    
                    int iYear = cActual.get(Calendar.YEAR);
                    int iWeek = cActual.get(Calendar.DAY_OF_WEEK);
                    int iDay = cActual.get(Calendar.DAY_OF_MONTH);                   
                    if(iDay == cActual.get(Calendar.DAY_OF_MONTH)) {
                        if(iWeek == cActual.get(Calendar.DAY_OF_WEEK)) {
                            if(iYear == cActual.get(Calendar.YEAR)) {
                                iResult = 2;
                            }
                        }
                    }
                }
                  
            }
            
            if(iResult == iMode) {
                mlResults.add(mTask);
            }            
            
        }
        
        return mlResults;
        
    }        
    public MapList retrieveUsersOwnedTasksOfFinishDate(Context context, String sOID, String sParentOID, String sSource, int iMode) throws Exception {
        
        
        long lDiff                              = 172800000L;        
        com.matrixone.apps.common.Person pOwner = new com.matrixone.apps.common.Person(sOID);
        MapList mlTasks                         = new MapList();
        MapList mlResults                       = new MapList();
        String sOwner                           = "";
        String sAssignee                        = "";       
        
        if(sSource.equals(DomainObject.SELECT_OWNER)) {
            sOwner = pOwner.getInfo(context, DomainObject.SELECT_NAME);
        } else {
            sAssignee = pOwner.getInfo(context, DomainObject.SELECT_NAME);
        }        

                
        if(null == sParentOID || "".equals(sParentOID)) {
            MapList mlProjects = getActiveProjects(context);
            for(int i = 0; i < mlProjects.size(); i++) {
                Map mProject = (Map)mlProjects.get(i);
                String sOIDProject = (String)mProject.get(DomainObject.SELECT_ID);
                MapList mlTasksProject = retrieveTasksOfProgramOrProject(context, sOIDProject, "", sOwner, sAssignee);
                mlTasks.addAll(mlTasksProject);
            }
        } else {
            mlTasks = retrieveTasksOfProgramOrProject(context, sParentOID, "", sOwner, sAssignee);
        }        

        for(int i = 0; i < mlTasks.size(); i++) {
            
            Map mTask           = (Map)mlTasks.get(i);
            String sTarget      = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
            String sActual      = (String)mTask.get(Task.SELECT_TASK_ACTUAL_FINISH_DATE);
            Calendar cTarget    = Calendar.getInstance();
            int iResult         = 10;
            
            cTarget.setTime(sdf.parse(sTarget));
            
            if(null == sActual || "".equals(sActual)) {
                
                Calendar cCurrent = Calendar.getInstance();
                if(cCurrent.after(cTarget)) {
                    iResult = 3;
                } else {
                    iResult = 0;
                }
                
            } else {
                
                Calendar cActual = Calendar.getInstance();                
                cActual.setTime(sdf.parse(sActual));
                long lTarget        = cTarget.getTimeInMillis();
                long lActual        = cActual.getTimeInMillis();
                if(cTarget.after(cActual)) {
                    if((lTarget - lActual) < lDiff) {
                        iResult = 2;
                    } else {
                        iResult = 1;
                    }
                } else {
                    if((lActual - lTarget) < lDiff) {
                        iResult = 2;
                    } else {
                        iResult = 3;
                    }                    
                }                  
            }
            
            if(iResult == iMode) {
                mlResults.add(mTask);
            }            
            
        }
        
        return mlResults;
        
    }     
    public MapList retrieveUsersOwnedTasksOfDuration(Context context, String sOID, String sParentOID, String sSource, int iMode) throws Exception {
        
        
        com.matrixone.apps.common.Person pOwner = new com.matrixone.apps.common.Person(sOID);
        MapList mlTasks                         = new MapList();
        MapList mlResults                       = new MapList();
        String sOwner                           = "";
        String sAssignee                        = "";       
        
        if(sSource.equals(DomainObject.SELECT_OWNER)) {
            sOwner = pOwner.getInfo(context, DomainObject.SELECT_NAME);
        } else {
            sAssignee = pOwner.getInfo(context, DomainObject.SELECT_NAME);
        }           

                
        if(null == sParentOID || "".equals(sParentOID)) {
            MapList mlProjects = getActiveProjects(context);
            for(int i = 0; i < mlProjects.size(); i++) {
                Map mProject = (Map)mlProjects.get(i);
                String sOIDProject = (String)mProject.get(DomainObject.SELECT_ID);
                MapList mlTasksProject = retrieveTasksOfProgramOrProject(context, sOIDProject, "", sOwner, sAssignee);
                mlTasks.addAll(mlTasksProject);
            }
        } else {
            mlTasks = retrieveTasksOfProgramOrProject(context, sParentOID, "", sOwner, sAssignee);
        }        
        
        for(int i = 0; i < mlTasks.size(); i++) {
           
            Map mTask       = (Map)mlTasks.get(i);
            String sTarget  = (String)mTask.get(Task.SELECT_TASK_ESTIMATED_DURATION);
            String sActual  = (String)mTask.get(Task.SELECT_TASK_ACTUAL_DURATION);
            String sFinish  = (String)mTask.get(Task.SELECT_TASK_ACTUAL_FINISH_DATE);
            int iResult     = 10;
            
            if(null != sFinish && !"".equals(sFinish)) {
                
                double dTarget  = Task.parseToDouble(sTarget);
                double dActual  = Task.parseToDouble(sActual);
                double dDev     = dTarget - dActual;
                
                if(dDev > 3.0)      { iResult = 0; }
                else if(dDev > 1.0) { iResult = 1; }
                else if(dDev > -1.0){ iResult = 2; }
                else if(dDev > -3.0){ iResult = 3; }
                else                { iResult = 4; }
            }                   

            if(iMode == iResult) {
                mlResults.add(mTask);                    
            }
        }
        
        return mlResults;
        
    }                      

    
    /**
     * It gives Task deliverables in order of their latest modification.
     * 
     * @param context the eMatrix <code>Context</code> object 
     * @param args
     * @return Task deliverables list
     * @throws Exception if operation fails
     */
    public Vector getTasksDeliverables(Context context, String[] args) throws Exception {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList mlObjects = (MapList) programMap.get("objectList");
        HashMap paramList = (HashMap) programMap.get("paramList");
        HashMap columnMap = (HashMap) programMap.get("columnMap");
        HashMap settings = (HashMap) columnMap.get("settings");
        String sMaxItems = (String)settings.get("Max Items");
        int iMaxItems = Integer.parseInt(sMaxItems);
        String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Deliverable.CannotOpenPhysicalProduct", context.getSession().getLanguage());        
        Vector vResult = new Vector(mlObjects.size());
        
        StringList slTaskSubTypes = ProgramCentralUtil.getTaskSubTypesList(context);
        slTaskSubTypes.remove(ProgramCentralConstants.TYPE_MILESTONE);
        StringList mileStoneSubtypeList = ProgramCentralUtil.getSubTypesList(context,ProgramCentralConstants.TYPE_MILESTONE);
        slTaskSubTypes.removeAll(mileStoneSubtypeList);
        
        final String SELECT_DELIVERABLE_ID = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.id";
        final String SELECT_DELIVERABLE_TYPE = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.type";
        final String SELECT_DELIVERABLE_NAME = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.name";
        final String SELECT_DELIVERABLE_REVISION = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.revision";
        final String SELECT_DELIVERABLE_MODIFIED_DATE = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.modified";
        
        StringList busSelects = new StringList();
        busSelects.add(ProgramCentralConstants.SELECT_TYPE);
        busSelects.add(ProgramCentralConstants.SELECT_POLICY);
        busSelects.add(SELECT_DELIVERABLE_ID);
        busSelects.add(SELECT_DELIVERABLE_TYPE);
        busSelects.add(SELECT_DELIVERABLE_NAME);
        busSelects.add(SELECT_DELIVERABLE_REVISION);
        busSelects.add(SELECT_DELIVERABLE_MODIFIED_DATE); //Document sort key

        try {
        	String[] sObjIdArr = new String[mlObjects.size()]; 
            for (int i = 0; i < mlObjects.size(); i++) {
    			Map objectMap = (Map) mlObjects.get(i);
            	sObjIdArr[i] = (String) objectMap.get(ProgramCentralConstants.SELECT_ID);
    		}
            
            MapList deliverablesInfoMapList = DomainObject.getInfo(context, sObjIdArr, busSelects);

            for (int i = 0; i < deliverablesInfoMapList.size(); i++) {
               
                StringBuilder sbResult = new StringBuilder();
                
                Map mObject = (Map) mlObjects.get(i);
                String sOID = (String) mObject.get(ProgramCentralConstants.SELECT_ID);
                
                Map taskInfoMap = (Map) deliverablesInfoMapList.get(i);
                String sTaskType = (String) taskInfoMap.get(ProgramCentralConstants.SELECT_TYPE);
                String sTaskPolicy = (String) taskInfoMap.get(ProgramCentralConstants.SELECT_POLICY);
                
                if(slTaskSubTypes.contains(sTaskType) || mileStoneSubtypeList.contains(sTaskType)&& ProgramCentralConstants.POLICY_PROJECT_TASK.equals(sTaskPolicy)) {
                	
                	StringList slDeliverablesIdList =  ProgramCentralUtil.getAsStringList(taskInfoMap.get(SELECT_DELIVERABLE_ID));
                    StringList slDeliverablesTypeList = ProgramCentralUtil.getAsStringList(taskInfoMap.get(SELECT_DELIVERABLE_TYPE));
                    StringList slDeliverablesNameList = ProgramCentralUtil.getAsStringList(taskInfoMap.get(SELECT_DELIVERABLE_NAME));
                    StringList slDeliverablesRevisionList = ProgramCentralUtil.getAsStringList(taskInfoMap.get(SELECT_DELIVERABLE_REVISION));
                    StringList slDeliverablesModifiedDateList = ProgramCentralUtil.getAsStringList(taskInfoMap.get(SELECT_DELIVERABLE_MODIFIED_DATE));
                    
                    int iNoOfDeliverables = slDeliverablesIdList.size();
                  //Convert taskInfoMap to a MapList of all the deliverables.
                    MapList taskDeliverablesMapList = new MapList(iNoOfDeliverables);

                    for (int j = 0; j < iNoOfDeliverables; j++) {
                    
                    	Map taskDeliverableMap = new HashMap();
                    	
                    	taskDeliverableMap.put(SELECT_DELIVERABLE_ID, slDeliverablesIdList.get(j));
        				taskDeliverableMap.put(SELECT_DELIVERABLE_TYPE, slDeliverablesTypeList.get(j));
        				taskDeliverableMap.put(SELECT_DELIVERABLE_NAME, slDeliverablesNameList.get(j));
        				taskDeliverableMap.put(SELECT_DELIVERABLE_REVISION, slDeliverablesRevisionList.get(j));
        				taskDeliverableMap.put(SELECT_DELIVERABLE_MODIFIED_DATE, slDeliverablesModifiedDateList.get(j));
        				
        				taskDeliverablesMapList.add(taskDeliverableMap);
        			}
                   
                    //Sort Deliverables
                    taskDeliverablesMapList.sort(SELECT_DELIVERABLE_MODIFIED_DATE, 
                    							 ProgramCentralConstants.DESCENDING_SORT, 
                    							 ProgramCentralConstants.SORTTYPE_DATE);
                    
                    // Apply limit
                    int iTotalNoOfDeliverables = taskDeliverablesMapList.size();
                    int iDeliverablesDisplayLimit = (iTotalNoOfDeliverables > iMaxItems) ? iMaxItems : iTotalNoOfDeliverables ;
                    
                    sbResult.append("<table");  
                    sbResult.append("><tr>");

                    //Show Counter Link   
                    sbResult.append("<td style='vertical-align:middle;padding-right:5px;width:0px'>");                
                    sbResult.append("<div ");            
                    sbResult.append(" style='text-align:right;font-weight:bold;");
                    
                    sbResult.append("cursor: pointer;' ");
                    sbResult.append(" onmouseover='$(this).css(\"color\",\"#04A3CF\");$(this).css(\"text-decoration\",\"underline\");' onmouseout='$(this).css(\"color\",\"#333333\");$(this).css(\"text-decoration\",\"none\");' ");
                    
                    sbResult.append("onClick=\"emxTableColumnLinkClick('");
                    sbResult.append("../common/emxTree.jsp?DefaultCategory=PMCDeliverableCommandPowerView&amp;objectId=").append(sOID);
                    sbResult.append("', '', '', false, 'content', '', '', '', '')\">");
                                    
                    sbResult.append(iTotalNoOfDeliverables);
                    sbResult.append("</div>"); 
                    sbResult.append("</td>");  

                    //Show Type-Icon Link         
                    for(int j = 0; j < iDeliverablesDisplayLimit; j++) {
                        
                        Map mRelatedObject = (Map)taskDeliverablesMapList.get(j);
                        String sObjectId = (String)mRelatedObject.get(SELECT_DELIVERABLE_ID);
                        String sType = (String)mRelatedObject.get(SELECT_DELIVERABLE_TYPE);
                        String i18Type = EnoviaResourceBundle.getTypeI18NString(context, sType, context.getSession().getLanguage());
                        String sName = (String)mRelatedObject.get(SELECT_DELIVERABLE_NAME);
                        sName = XSSUtil.encodeForXML(context, sName);
                        String sRevision = (String)mRelatedObject.get(SELECT_DELIVERABLE_REVISION);
                        
                        String sIcon = UINavigatorUtil.getTypeIconProperty(context, sType);
                        
                        sbResult.append("<td style='vertical-align:middle;padding-left:1px;cursor:pointer;' ");
                        if ("VPMReference".equalsIgnoreCase(sType)) {
                        sbResult.append("onClick=\"javascript:alert('").append(sErrMsg).append("')\">");	
                        }else{
                        sbResult.append("onClick=\"javascript:callCheckout('").append(sObjectId).append("',");
                        sbResult.append("'download', '', '', 'null', 'null', 'structureBrowser', 'PMCPendingDeliverableSummary', 'null')\">");
                        }
                        sbResult.append("<img style='vertical-align:middle;' src='../common/images/").append(sIcon).append("'");
                        sbResult.append(" title=\"");
                        sbResult.append(i18Type).append(" - ").append(sName).append(" - ").append(sRevision);
                        sbResult.append("\" />");

                        sbResult.append("</td>");
                       
                    }
                    sbResult.append("</tr></table>");
                    
                    vResult.add(sbResult.toString());
                } else {
                	vResult.add(ProgramCentralConstants.EMPTY_STRING);
                }
            }
            
        } catch(Exception ex) {
        	ex.printStackTrace();
        }
        
        return vResult;        
    }
    
    /**
     * It gives Documents deliverables in order of their latest modification.
     * 
     * @param context the eMatrix <code>Context</code> object 
     * @param args
     * @return Task deliverables list
     * @throws Exception if operation fails
     */
    public Vector getDocumentsDeliverables(Context context, String[] args) throws Exception {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList mlObjects = (MapList) programMap.get("objectList");
        HashMap paramList = (HashMap) programMap.get("paramList");
        HashMap columnMap = (HashMap) programMap.get("columnMap");
        HashMap settings = (HashMap) columnMap.get("settings");
        String sMaxItems = (String)settings.get("Max Items");
        int iMaxItems = Integer.parseInt(sMaxItems);
        
        Vector vResult = new Vector(mlObjects.size());
        
        StringList slTaskSubTypes = ProgramCentralUtil.getTaskSubTypesList(context);
        slTaskSubTypes.remove(ProgramCentralConstants.TYPE_MILESTONE);
        StringList mileStoneSubtypeList = ProgramCentralUtil.getSubTypesList(context,ProgramCentralConstants.TYPE_MILESTONE);
        slTaskSubTypes.removeAll(mileStoneSubtypeList);
        
        String sDimensions = EnoviaResourceBundle.getProperty(context, "emxFramework.PopupSize.Large");
        String[] aDimensions = sDimensions.split("x");
        String sWindowHeight = aDimensions[1];
        String sWindowWidth = aDimensions[0];
        
        String sLinkPrefix = "onClick=\"emxTableColumnLinkClick('../common/";
        String sLinkSuffix = "', 'popup', '', '" + sWindowWidth + "', '" + sWindowHeight + "', '')\"";
        
        final String SELECT_DELIVERABLE_ID = DomainObject.SELECT_ID;
        final String SELECT_DELIVERABLE_TYPE = DomainObject.SELECT_TYPE;
        final String SELECT_DELIVERABLE_NAME = DomainObject.SELECT_NAME;
        final String SELECT_DELIVERABLE_REVISION = DomainObject.SELECT_REVISION;
        final String SELECT_DELIVERABLE_MODIFIED_DATE = DomainObject.SELECT_MODIFIED;

        StringList busSelects = new StringList();
        busSelects.add(ProgramCentralConstants.SELECT_POLICY);
        busSelects.add(SELECT_DELIVERABLE_ID);
        busSelects.add(SELECT_DELIVERABLE_TYPE);
        busSelects.add(SELECT_DELIVERABLE_NAME);
        busSelects.add(SELECT_DELIVERABLE_REVISION);
        busSelects.add(SELECT_DELIVERABLE_MODIFIED_DATE); //Document sort key

        try {
        	String[] sObjIdArr = new String[mlObjects.size()]; 
            for (int i = 0; i < mlObjects.size(); i++) {
    			Map objectMap = (Map) mlObjects.get(i);
            	sObjIdArr[i] = (String) objectMap.get(ProgramCentralConstants.SELECT_ID);
    		}
            
            MapList deliverablesInfoMapList = DomainObject.getInfo(context, sObjIdArr, busSelects);

            for (int i = 0; i < deliverablesInfoMapList.size(); i++) {
               
                StringBuilder sbResult = new StringBuilder();
                
                Map mObject = (Map) mlObjects.get(i);
                String sOID = (String) mObject.get(ProgramCentralConstants.SELECT_ID);
                
                Map taskInfoMap = (Map) deliverablesInfoMapList.get(i);
                String sTaskType = (String) taskInfoMap.get(ProgramCentralConstants.SELECT_TYPE);
                String sTaskPolicy = (String) taskInfoMap.get(ProgramCentralConstants.SELECT_POLICY);
                
                	
              	StringList slDeliverablesIdList =  ProgramCentralUtil.getAsStringList(taskInfoMap.get(SELECT_DELIVERABLE_ID));
                StringList slDeliverablesTypeList = ProgramCentralUtil.getAsStringList(taskInfoMap.get(SELECT_DELIVERABLE_TYPE));
                StringList slDeliverablesNameList = ProgramCentralUtil.getAsStringList(taskInfoMap.get(SELECT_DELIVERABLE_NAME));
                StringList slDeliverablesRevisionList = ProgramCentralUtil.getAsStringList(taskInfoMap.get(SELECT_DELIVERABLE_REVISION));
                StringList slDeliverablesModifiedDateList = ProgramCentralUtil.getAsStringList(taskInfoMap.get(SELECT_DELIVERABLE_MODIFIED_DATE));
                    
                int iNoOfDeliverables = slDeliverablesIdList.size();
                MapList taskDeliverablesMapList = new MapList(iNoOfDeliverables);

                for (int j = 0; j < iNoOfDeliverables; j++) {
                 
                  	Map taskDeliverableMap = new HashMap();
                   	String sDeliverableType        = (String) slDeliverablesTypeList.get(j);

                   	if (mxType.isOfParentType(context, sDeliverableType, DomainConstants.TYPE_DOCUMENT)) {
                   		taskDeliverableMap.put(SELECT_DELIVERABLE_ID, slDeliverablesIdList.get(j));
                       	taskDeliverableMap.put(SELECT_DELIVERABLE_TYPE, slDeliverablesTypeList.get(j));
           				taskDeliverableMap.put(SELECT_DELIVERABLE_NAME, slDeliverablesNameList.get(j));
           				taskDeliverableMap.put(SELECT_DELIVERABLE_REVISION, slDeliverablesRevisionList.get(j));
           				taskDeliverableMap.put(SELECT_DELIVERABLE_MODIFIED_DATE, slDeliverablesModifiedDateList.get(j));
           				
           				taskDeliverablesMapList.add(taskDeliverableMap);
                   	}
        				
       			}
                   
                //Sort Deliverables
                taskDeliverablesMapList.sort(SELECT_DELIVERABLE_MODIFIED_DATE, 
                    							 ProgramCentralConstants.DESCENDING_SORT, 
                    							 ProgramCentralConstants.SORTTYPE_DATE);
                    
                // Apply limit
                int iTotalNoOfDeliverables = taskDeliverablesMapList.size();
                int iDeliverablesDisplayLimit = (iTotalNoOfDeliverables > iMaxItems) ? iMaxItems : iTotalNoOfDeliverables ;
                   
                sbResult.append("<table");  
                sbResult.append("><tr>");
                //Show Type-Icon Link         
                for(int j = 0; j < iDeliverablesDisplayLimit; j++) {
                        
                    Map mRelatedObject = (Map)taskDeliverablesMapList.get(j);
                    String sObjectId = (String)mRelatedObject.get(SELECT_DELIVERABLE_ID);
                    String sType = (String)mRelatedObject.get(SELECT_DELIVERABLE_TYPE);
                    String sName = (String)mRelatedObject.get(SELECT_DELIVERABLE_NAME);
                    String sRevision = (String)mRelatedObject.get(SELECT_DELIVERABLE_REVISION);
                        
                    String sIcon = UINavigatorUtil.getTypeIconProperty(context, sType);

                    sbResult.append("<td style='vertical-align:middle;padding-left:1px;cursor:pointer;' ");
                    sbResult.append("onClick=\"javascript:callCheckout('").append(sObjectId).append("',");
                    sbResult.append("'download', '', '', 'null', 'null', 'structureBrowser', 'PMCPendingDeliverableSummary', 'null')\">");
                    sbResult.append("<img style='vertical-align:middle;' src='../common/images/").append(sIcon).append("'");
                    sbResult.append(" title='");
                    sbResult.append(sType).append(" - ").append(sName).append(" - ").append(sRevision);
                    sbResult.append("' />");

                    sbResult.append("</td>");
                       
                }
                sbResult.append("</tr></table>");
                    
                vResult.add(sbResult.toString());
            }
            
        } catch(Exception ex) {
        	ex.printStackTrace();
        }
        
        return vResult;        
    }
    
    /**
     * It gives percent complete info of a tasks in the form of graphical bar. 
     * 
     * @param context the eMatrix <code>Context</code> object
     * @param args
     * @return percent complete in the form of graphical bar
     * @throws Exception if operation fails
     */
    public Vector getTasksProgressBar(Context context, String[] args) throws Exception {
    	
        Vector vResult = new Vector();
        
        Map programMap = (Map) JPO.unpackArgs(args);
        MapList objectsMapList = (MapList) programMap.get("objectList");
        
        StringList objSelects = new StringList();
        objSelects.add(Task.SELECT_CURRENT);
        objSelects.add(Task.SELECT_PERCENT_COMPLETE);
        objSelects.add(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
        objSelects.add(Task.SELECT_BASELINE_CURRENT_END_DATE);
        
        try {
        	int iThresholdValue = Integer.parseInt(EnoviaResourceBundle.getProperty(context,
        										   "eServiceApplicationProgramCentral.SlipThresholdYellowRed"));
        	String[] objIdArr = new String[objectsMapList.size()];
            for (int i = 0; i < objectsMapList.size(); i++) {
            	Map objectMap = (Map) objectsMapList.get(i);
            	objIdArr[i] = (String) objectMap.get(ProgramCentralConstants.SELECT_ID);
    		}
            
            MapList taskInfoMapList = (MapList) DomainObject.getInfo(context, objIdArr, objSelects);
            
            for (int i = 0; i < taskInfoMapList.size(); i++) {
    			Map taskInfoMap = (Map) taskInfoMapList.get(i);
    			String strCurrent = (String)taskInfoMap.get(Task.SELECT_CURRENT);
                String strPercentComplete = (String)taskInfoMap.get(Task.SELECT_PERCENT_COMPLETE);                
                String strTaskEstFinishDate = (String)taskInfoMap.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                String strBaselineCurrentEndDate = (String)taskInfoMap.get(Task.SELECT_BASELINE_CURRENT_END_DATE);
                
                Double dPercent = 0.0;
                if(ProgramCentralUtil.isNotNullString(strPercentComplete))
                	dPercent = Task.parseToDouble(strPercentComplete);
                String sColor = "";                

                if (ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE.equals(strCurrent) || "100.0".equals(strPercentComplete) ){
                    sColor = "Green";
                } else {
                    Calendar cNow = Calendar.getInstance(TimeZone.getDefault());
                    
                    String strTaskFinishDate = (ProgramCentralUtil.isNotNullString(strBaselineCurrentEndDate)) ? strBaselineCurrentEndDate : strTaskEstFinishDate;
                	if(ProgramCentralUtil.isNotNullString(strPercentComplete)){
                		Date date = sdf.parse(strTaskFinishDate);

                		Calendar cTaskFinishDate = Calendar.getInstance();
                		cTaskFinishDate.setTime(date);

                		if (cTaskFinishDate.before(cNow)) {
                			sColor = "Red";
                		} else {
                			cNow.add(GregorianCalendar.DAY_OF_YEAR, +iThresholdValue);
                			if (cTaskFinishDate.before(cNow)) { 
                				sColor = "Yellow"; 
                			}else { 
                				sColor = "Green"; 
                			}
                		}
                	}
                }

                String barImage = ("Red".equalsIgnoreCase(sColor)) ? "WBSProgressBar" : "progressBar";
                
                if (dPercent < 9.5) {
                	strPercentComplete = "<img src='../common/images/progressBar000.gif'/>"; 
                } else if (dPercent < 15.0) {
                	strPercentComplete = "<img src='../common/images/"+ barImage + "010" + sColor + ".gif'/>"; 
                } else if (dPercent < 25.0) { 
                	strPercentComplete = "<img src='../common/images/"+ barImage + "020" + sColor + ".gif'/>"; 
                } else if (dPercent < 35.0) { 
                	strPercentComplete = "<img src='../common/images/"+ barImage + "030" + sColor + ".gif'/>"; 
                } else if (dPercent < 45.0) { 
                	strPercentComplete = "<img src='../common/images/"+ barImage + "040" + sColor + ".gif'/>"; 
                } else if (dPercent < 55.0) { 
                	strPercentComplete = "<img src='../common/images/"+ barImage + "050" + sColor + ".gif'/>"; 
                } else if (dPercent < 65.0) { 
                	strPercentComplete = "<img src='../common/images/"+ barImage + "060" + sColor + ".gif'/>"; 
                } else if (dPercent < 75.0) { 
                	strPercentComplete = "<img src='../common/images/"+ barImage + "070" + sColor + ".gif'/>"; 
                } else if (dPercent < 85.0) { 
                	strPercentComplete = "<img src='../common/images/"+ barImage + "080" + sColor + ".gif'/>";
                } else if (dPercent < 95.0) { 
                	strPercentComplete = "<img src='../common/images/"+ barImage + "090" + sColor + ".gif'/>"; 
                } else { 
                	strPercentComplete = "<img src='../common/images/progressBar100" + sColor + ".gif'/>"; 
                }
                vResult.addElement(strPercentComplete);
    		}
        } catch (Exception exception) {
        	exception.printStackTrace();
        	throw exception;
        }
        
        return vResult;
    }
    
    
    /**
     * It returns the Project member list details for Assignee column of Assignment View.  
     * 
     * @param context the eMatrix <code>Context</code> object
     * @param args
     * @return Project member list details
     * @throws Exception if operation fails
     */
    public List dynamicAssignmentViewMembersColumn(Context context, String[] args) throws Exception {

    	MapList mlResult = new MapList();
        
        Map programMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map)programMap.get("requestMap");
        Map imageData = (Map)requestMap.get("ImageData");
        String sMCSURL = (String)imageData.get("MCSURL");
        String objectId = (String) requestMap.get("objectId");
        
        String strI18nMember = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
                "emxProgramCentral.TaskAssignment.Member", context.getSession().getLanguage());
        String strI18nNonMember = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
                "emxProgramCentral.TaskAssignment.NonMember", context.getSession().getLanguage());
        
        try{
            HashMap paramMap = new HashMap();
            paramMap.put("objectId", objectId);
            String[] argss = JPO.packArgs(paramMap);
            
            //Finding Project Members.
            emxProjectMember_mxJPO jpo = new emxProjectMember_mxJPO(context, argss);
            MapList mlMembers = jpo.getMembers(context, argss);

            mlMembers.sort(Person.SELECT_FIRST_NAME, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
            mlMembers.sort(Person.SELECT_LAST_NAME, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
            
            StringList slProjectMembersIdList = new StringList();
            for (int i = 0; i < mlMembers.size(); i++) {
            	Map projectMemberMap = (Map) mlMembers.get(i);
            	String sName = (String) projectMemberMap.get(DomainConstants.SELECT_NAME);
            	String sPersonOID = (String) projectMemberMap.get(DomainConstants.SELECT_ID);
            	sPersonOID = sPersonOID.contains("personid_")?sPersonOID.replace("personid_", "") : sPersonOID;
            	String sFirstName = (String) projectMemberMap.get(Person.SELECT_FIRST_NAME);
            	String sLastName = (String) projectMemberMap.get(Person.SELECT_LAST_NAME);
            	
            	String sImage = emxUtilBase_mxJPO.getPrimaryImageURL(context, args, sPersonOID, "mxThumbnail Image",
            													  sMCSURL, "../common/images/noPicture.gif");
            	StringBuffer sbLabel = new StringBuffer();
            	sbLabel.append(sFirstName).append(" ").append(sLastName.toUpperCase());

            	if(ProgramCentralUtil.isNotNullString(sImage)) {
            		sbLabel = new StringBuffer();
            		sbLabel.append("<img style='height:40px;border:1px solid #bababa;' src=\"").append(sImage).append("\"/><br />");
            		sbLabel.append(sFirstName).append("<br/>");
            		sbLabel.append(sLastName.toUpperCase());
            	}  

            	HashMap settingsMap = new HashMap();
            	settingsMap.put("Auto Filter", "false");
            	settingsMap.put("Column Type", "programHTMLOutput");
            	settingsMap.put("program", "emxProgramUI");
            	settingsMap.put("function", "getAssignmentViewMembersColumnData");
            	settingsMap.put("personId", sPersonOID);
            	settingsMap.put("Width", "100"); 
            	settingsMap.put("Group Header", strI18nMember);

            	Map mColumn = new HashMap();
            	mColumn.put("name", sFirstName + " " + sLastName);                    
            	mColumn.put("label", sbLabel.toString());
            	mColumn.put("expression", "id");
            	mColumn.put("select", "id");
            	mColumn.put("settings", settingsMap);

            	mlResult.add(mColumn);
            	
            	slProjectMembersIdList.add(sPersonOID);
            }
            
            //Finding External Project Members.
            ProjectSpace project = new ProjectSpace();
            MapList mlExternalMembers = project.getProjectTaskAssignees(context, objectId);
            
            //Removing project Members from the list and keeps only external members. 
            int tempSize = mlExternalMembers.size();
            for (int i = 0; i < tempSize; i++) {
            	Map externalMemberMap = (Map) mlExternalMembers.get(i);
            	String externalMemberId = (String) externalMemberMap.get(ProgramCentralConstants.SELECT_ID);
            	if (slProjectMembersIdList.contains(externalMemberId)) {
            		mlExternalMembers.remove(externalMemberMap);
            		i--;
            		tempSize--;
            	}
            }
            
            mlExternalMembers.sort(Person.SELECT_FIRST_NAME, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
            mlExternalMembers.sort(Person.SELECT_LAST_NAME, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
            
            for (int i = 0; i < mlExternalMembers.size(); i++) {
            	Map externalMemberMap = (Map) mlExternalMembers.get(i);
            	String sName = (String) externalMemberMap.get(DomainConstants.SELECT_NAME);
            	String sPersonOID = (String) externalMemberMap.get(DomainConstants.SELECT_ID);
            	sPersonOID = sPersonOID.contains("personid_")?sPersonOID.replace("personid_", "") : sPersonOID;
            	String sFirstName = (String) externalMemberMap.get(Person.SELECT_FIRST_NAME);
            	String sLastName = (String) externalMemberMap.get(Person.SELECT_LAST_NAME);
            	
            	String sImage = emxUtilBase_mxJPO.getPrimaryImageURL(context, args, sPersonOID, "mxThumbnail Image",
            													  sMCSURL, "../common/images/noPicture.gif");
            	StringBuffer sbLabel = new StringBuffer();
            	sbLabel.append(sFirstName).append(" ").append(sLastName.toUpperCase());

            	if(ProgramCentralUtil.isNotNullString(sImage)) {
            		sbLabel = new StringBuffer();
            		sbLabel.append("<img style='height:40px;border:1px solid #bababa;' src=\"").append(sImage).append("\"/><br />");
            		sbLabel.append(sFirstName).append("<br/>");
            		sbLabel.append(sLastName.toUpperCase());
            	}  
            	
            	HashMap settingsMap = new HashMap();
            	settingsMap.put("Auto Filter", "false");
            	settingsMap.put("Column Type", "programHTMLOutput");
            	settingsMap.put("program", "emxProgramUI");
            	settingsMap.put("function", "getAssignmentViewMembersColumnData");
            	settingsMap.put("personId", sPersonOID);
            	settingsMap.put("Width", "100"); 
            	settingsMap.put("Group Header", strI18nNonMember);
            	
            	Map mColumn = new HashMap();
            	mColumn.put("name", sFirstName + " " + sLastName);       
            	mColumn.put("label", sbLabel.toString());
            	mColumn.put("expression", "id");
            	mColumn.put("select", "id");
            	mColumn.put("settings", settingsMap);

            	mlResult.add(mColumn);
            }
            
        }catch(Exception exception){
        	exception.printStackTrace();
        	throw exception;
        }

        return mlResult;
    } 
    
    
    /**
     * It returns the Project member list details for Assignee column of Allocation View.
     * 
     * @param context the eMatrix <code>Context</code> object
     * @param args
     * @return Project member list details
     * @throws Exception if operation fails
     */
    public List dynamicAllocationViewMembersColumn(Context context, String[] args) throws Exception {

    	MapList mlResult = new MapList();
        
    	Map programMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map)programMap.get("requestMap");
        String sLang = (String)requestMap.get("languageStr");
        Map imageData = imageData= (Map)requestMap.get("ImageData");
        String sMCSURL = (String)imageData.get("MCSURL");
        String objectId = (String) requestMap.get("objectId");
        
        String strI18nMember = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
                "emxProgramCentral.TaskAllocation.Member", context.getSession().getLanguage());
        String strI18nNonMember = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
                "emxProgramCentral.TaskAllocation.NonMember", context.getSession().getLanguage());
        
        try{
            HashMap paramMap = new HashMap();
            paramMap.put("objectId", objectId);
            String[] argss = JPO.packArgs(paramMap);
            
            emxProjectMember_mxJPO jpo = new emxProjectMember_mxJPO(context, argss);
            MapList mlMembers = jpo.getMembers(context, argss);
            
            mlMembers.sort(Person.SELECT_FIRST_NAME, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
            mlMembers.sort(Person.SELECT_LAST_NAME, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
            
            StringList slProjectMembersIdList = new StringList();
            for (int j = 0; j < mlMembers.size(); j++) {
            	Map projectMemberMap = (Map) mlMembers.get(j);
                String sName = (String) projectMemberMap.get(DomainConstants.SELECT_NAME);
                String sPersonOID = (String) projectMemberMap.get(DomainConstants.SELECT_ID);
                sPersonOID = sPersonOID.contains("personid_")?sPersonOID.replace("personid_", "") : sPersonOID;
                String sFirstName = (String) projectMemberMap.get(Person.SELECT_FIRST_NAME);
                String sLastName = (String) projectMemberMap.get(Person.SELECT_LAST_NAME);
                String sRole = (String) projectMemberMap.get(MemberRelationship.SELECT_PROJECT_ROLE);
                
                String sImage = emxUtilBase_mxJPO.getPrimaryImageURL(context, args, sPersonOID, "mxThumbnail Image", 
                												  sMCSURL, "../common/images/noPicture.gif");
                           
                if(ProgramCentralUtil.isNotNullString(sRole)) {
                    sRole = EnoviaResourceBundle.getRangeI18NString(context, ProgramCentralConstants.ATTRIBUTE_PROJECT_ROLE, sRole, sLang) + " ";            
                } else {
                	sRole = DomainConstants.EMPTY_STRING;
                }
                
                StringBuffer sbLabel = new StringBuffer();
                sbLabel.append("<img style='height:42px;float:left;margin-right:3px;border:1px solid #bababa;' src='").append(sImage).append("'/>");
                sbLabel.append(sLastName.toUpperCase()).append(" ").append(sFirstName).append("<br/>");
                sbLabel.append(sRole);
                
                HashMap settingsMap = new HashMap();
                settingsMap.put("Column Type","programHTMLOutput");
                settingsMap.put("program","emxProgramUI");
                settingsMap.put("function","getAllocationViewMembersColumnData");
                settingsMap.put("personId",sPersonOID);
                settingsMap.put("Sortable","false");
                settingsMap.put("Width","200");
                settingsMap.put("Group Header", strI18nMember);
                
                Map mColumn = new HashMap();
                mColumn.put("name","PMC" + sName);                  
                mColumn.put("label",sbLabel.toString());
                mColumn.put("expression","id");
                mColumn.put("select","id");
                mColumn.put("settings",settingsMap);
                
                mlResult.add(mColumn);
                
                slProjectMembersIdList.add(sPersonOID);
            }
            
          //Finding External Project Members.
            ProjectSpace project = new ProjectSpace();
            MapList mlExternalMembers = project.getProjectTaskAssignees(context, objectId);
            
            //Removing project Members from the list and keeps only external members. 
            int tempSize = mlExternalMembers.size();
            for (int i = 0; i < tempSize; i++) {
            	Map externalMemberMap = (Map) mlExternalMembers.get(i);
            	String externalMemberId = (String) externalMemberMap.get(ProgramCentralConstants.SELECT_ID);
            	if (slProjectMembersIdList.contains(externalMemberId)) {
            		mlExternalMembers.remove(externalMemberMap);
            		i--;
            		tempSize--;
            	}
            }
            
            mlExternalMembers.sort(Person.SELECT_FIRST_NAME, ProgramCentralConstants.DESCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
            mlExternalMembers.sort(Person.SELECT_LAST_NAME, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
            
            for (int j = 0; j < mlExternalMembers.size(); j++) {
            	Map externalMemberMap = (Map) mlExternalMembers.get(j);
                String sName = (String) externalMemberMap.get(DomainConstants.SELECT_NAME);
                String sPersonOID = (String) externalMemberMap.get(DomainConstants.SELECT_ID);
                sPersonOID = sPersonOID.contains("personid_")?sPersonOID.replace("personid_", "") : sPersonOID;
                String sFirstName = (String) externalMemberMap.get(Person.SELECT_FIRST_NAME);
                String sLastName = (String) externalMemberMap.get(Person.SELECT_LAST_NAME);
                String sRole = (String) externalMemberMap.get(MemberRelationship.SELECT_PROJECT_ROLE);
                
                String sImage = emxUtilBase_mxJPO.getPrimaryImageURL(context, args, sPersonOID, "mxThumbnail Image", 
                												  sMCSURL, "../common/images/noPicture.gif");
                           
                if(ProgramCentralUtil.isNotNullString(sRole)) {
                    sRole = EnoviaResourceBundle.getRangeI18NString(context, ProgramCentralConstants.ATTRIBUTE_PROJECT_ROLE, sRole, sLang) + " ";            
                } else {
                	sRole = DomainConstants.EMPTY_STRING;
                }
                
                StringBuffer sbLabel = new StringBuffer();
                sbLabel.append("<img style='height:42px;float:left;margin-right:3px;border:1px solid #bababa;' src='").append(sImage).append("'/>");
                sbLabel.append(sLastName.toUpperCase()).append(" ").append(sFirstName).append("<br/>");
                sbLabel.append(sRole);
                
                HashMap settingsMap = new HashMap();
                settingsMap.put("Column Type","programHTMLOutput");
                settingsMap.put("program","emxProgramUI");
                settingsMap.put("function","getAllocationViewMembersColumnData");
                settingsMap.put("personId",sPersonOID);
                settingsMap.put("Sortable","false");
                settingsMap.put("Width","200");
                settingsMap.put("Group Header", strI18nNonMember);
                
                Map mColumn = new HashMap();
                mColumn.put("name","PMC" + sName);                  
                mColumn.put("label",sbLabel.toString());
                mColumn.put("expression","id");
                mColumn.put("select","id");
                mColumn.put("settings",settingsMap);
                
                mlResult.add(mColumn);
            }
        } catch(Exception exception){
        	exception.printStackTrace();
        	throw exception;
        }

        return mlResult;
    }    

    
    /**
     * It gives assignment status details for each member of the Project. 
     * 
     * @param context  the eMatrix <code>Context</code> object
     * @param args
     * @return assignment status details of all project members.
     * @throws Exception if operation fails
     */
    public Vector getAssignmentViewMembersColumnData(Context context, String[] args) throws Exception {

        Vector vResult = new Vector();
    
        Map paramMap = (Map) JPO.unpackArgs(args);
        Map paramList = (Map)paramMap.get("paramList");
        String exportFormat = (String)paramList.get("exportFormat");
        MapList mlObjects = (MapList) paramMap.get("objectList");        
        HashMap columnMap = (HashMap) paramMap.get("columnMap");
        HashMap settings = (HashMap) columnMap.get("settings");
        String sOIDPerson = (String) settings.get("personId");
        String sLanguage = (String)paramList.get("languageStr");
        
		String parentPrj=(String)paramList.get("parentOID");
        DomainObject parentObj=new DomainObject(parentPrj);
	 	String projectState=(String)parentObj.getInfo(context, DomainConstants.SELECT_CURRENT);
		
        String sCompleted = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Completed", sLanguage);
        String sLabelAssign = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Assign", sLanguage);
        String sLabelUnassign = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Unassign", sLanguage);
        String sLabelAssigned = EnoviaResourceBundle.getProperty(context, "Components", "emxComponents.Common.Assigned", sLanguage);
        
        //StringList slStyle = getAllocationStyle(context, args, sOIDPerson);
        final String SELECT_RELATIONSHIP_ID = "to["+ProgramCentralConstants.RELATIONSHIP_ASSIGNED_TASKS+"].id";
        final String SELECT_ASSIGNEE_ID = "to["+ProgramCentralConstants.RELATIONSHIP_ASSIGNED_TASKS+"].from.id";
        
        StringList objSelects = new StringList();
        objSelects.add(ProgramCentralConstants.SELECT_ID);
        objSelects.add(ProgramCentralConstants.SELECT_CURRENT);
        objSelects.add(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
        objSelects.add(SELECT_RELATIONSHIP_ID);
        objSelects.add(SELECT_ASSIGNEE_ID);
        
        try{
        	String[] objIdArr = new String[mlObjects.size()];
        	for (int i = 0; i < mlObjects.size(); i++) {
        		Map mObject = (Map) mlObjects.get(i);
                objIdArr[i] = (String)mObject.get(ProgramCentralConstants.SELECT_ID);
        	}
        	
        	MapList taskInfoMapList = DomainObject.getInfo(context, objIdArr, objSelects);
        	
        	for (int i = 0; i < taskInfoMapList.size(); i++){
        		
        		String sResult = "";
        		String sRelId = "";
                Boolean bIsAssigned = false;
                
                Map mObject = (Map) mlObjects.get(i);
                String sRowID = (String)mObject.get("id[level]");
                String sOID = (String)mObject.get(ProgramCentralConstants.SELECT_ID);
        		
        		Map taskInfoMap = (Map) taskInfoMapList.get(i);
        		
        		StringList slAssigneeList = ProgramCentralUtil.getAsStringList(taskInfoMap.get(SELECT_ASSIGNEE_ID));
        		StringList slRelIdList = ProgramCentralUtil.getAsStringList(taskInfoMap.get(SELECT_RELATIONSHIP_ID));
        		
                String sCurrent = (String) taskInfoMap.get(ProgramCentralConstants.SELECT_CURRENT);
        		String sIsTaskMgmtType = (String) taskInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
                
                if( "true".equalsIgnoreCase(sIsTaskMgmtType) ) {
                    
                    String sStyleComplete = "style='color:#FFF;background-color:#ABB8BD;font-weight:normal;min-width:90px;width=100%;text-align:center;vertical-align:middle;height:20px;line-height:20px;padding:0px;margin:0px;font-style:oblique'";
                    String sStyleAssigned = "style='color:#FFF;background-color:#5F747D;font-weight:normal;min-width:90px;width=100%;text-align:center;vertical-align:middle;height:20px;line-height:20px;padding:0px;margin:0px;'";
                    String sStyleUnassigned = "style='font-weight:normal;min-width:90px;width=100%;text-align:center;vertical-align:middle;height:20px;line-height:20px;padding:0px;margin:0px;color:transparent;'";
                    StringBuilder sbResult = new StringBuilder();
                    
                    if(slAssigneeList.size() > 0 && slAssigneeList.contains(sOIDPerson)) {
                        bIsAssigned = true;
                        sResult = "Assigned" + sResult;
                        int index = slAssigneeList.indexOf(sOIDPerson);
                    	sRelId = (String) slRelIdList.get(index);
                    }      
                    
                    if( !ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE.equals(sCurrent) ) {  
                        
                        if(bIsAssigned) {

                    		if("CSV".equalsIgnoreCase(exportFormat) || "HTML".equalsIgnoreCase(exportFormat)){
                    			sbResult.append(sLabelAssigned);
                    		} else {
                            sbResult.append("<div ");
                            sbResult.append(sStyleAssigned);
                            sbResult.append(" onmouseout='this.style.background=\"#5F747D\";this.style.color=\"#FFF\";this.style.fontWeight=\"normal\"; this.innerHTML=\"").append(sLabelAssigned).append("\"'");
                            if(!projectState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL)){
                            sbResult.append(" onclick='window.open(\"../programcentral/emxProgramCentralUtil.jsp?mode=wbsAssignmentView&amp;subMode=unassign&amp;relationship=" + ProgramCentralConstants.RELATIONSHIP_ASSIGNED_TASKS + "&amp;from=false&amp;objectId=" + sOID + "&amp;rowId=" + sRowID + "&amp;relId=" + sRelId + "&amp;personId=" + sOIDPerson + "\", \"listHidden\", \"\", true);'");
                            sbResult.append(" onmouseover='this.style.background=\"#cc0000\"; this.style.color=\"#FFF\";this.style.fontWeight=\"normal\"; this.style.cursor=\"pointer\"; this.innerHTML=\"").append(sLabelUnassign).append("\"'");
                            }
                            sbResult.append(">").append(sLabelAssigned).append("</div> ");
                    		}
                        } else {
                    		if(!("CSV".equalsIgnoreCase(exportFormat) || "HTML".equalsIgnoreCase(exportFormat))){
                            sbResult.append("<div ");
                            sbResult.append(sStyleUnassigned);
                            sbResult.append("  onmouseout='this.style.background=\"transparent\";this.style.color=\"transparent\";this.style.fontWeight=\"normal\"; this.innerHTML=\"-\"'");
                            if(!projectState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL)){
                            sbResult.append("  onclick='window.open(\"../programcentral/emxProgramCentralUtil.jsp?mode=wbsAssignmentView&amp;subMode=assign&amp;relationship="+ ProgramCentralConstants.RELATIONSHIP_ASSIGNED_TASKS + "&amp;from=false&amp;objectId=" + sOID + "&amp;rowId=" + sRowID + "&amp;personId=" + sOIDPerson + "\", \"listHidden\", \"\", true);'");
                            sbResult.append(" onmouseover='this.style.background=\"#009c00\";    this.style.color=\"#FFF\";this.style.fontWeight=\"normal\"; this.style.cursor=\"pointer\"; this.innerHTML=\"").append(sLabelAssign).append("\"'");                   
                            }                  
                            sbResult.append(">-</div>");
                        }
                    	}
                    } else {
                        
                    	sbResult.append("<div ").append(sStyleComplete).append(">");
                        sbResult.append(sCompleted).append("</div>");
                    }
                    sResult = sbResult.toString();
                } 
                vResult.add(sResult);
        	}
        } catch(Exception exception){
        	exception.printStackTrace();
        }
        
        return vResult;
    }    
    
    
    /**
     * It gives allocation status details of each member of the Project.
     * 
     * @param context the eMatrix <code>Context</code> object
     * @param args
     * @return allocation status details of all the members.
     * @throws Exception if operation fails
     */
    public Vector getAllocationViewMembersColumnData(Context context, String[] args) throws Exception {

        Vector vResult = new Vector();
        
        Map paramMap = (Map) JPO.unpackArgs(args);
        MapList mlObjects = (MapList) paramMap.get("objectList");        
        HashMap columnMap = (HashMap) paramMap.get("columnMap");
        HashMap settings = (HashMap) columnMap.get("settings");
        String sOIDPerson = (String) settings.get("personId");
        Map paramList = (Map) paramMap.get("paramList");        
        String projectID=(String)paramList.get("parentOID");
        DomainObject project=new DomainObject(projectID);
        String projectState=(String)project.getInfo(context, ProgramCentralConstants.SELECT_CURRENT);
        
		
        final String SELECT_RELATIONSHIP_ID = "to["+ProgramCentralConstants.RELATIONSHIP_ASSIGNED_TASKS+"].id";
        final String SELECT_ASSIGNEE_ID = "to["+ProgramCentralConstants.RELATIONSHIP_ASSIGNED_TASKS+"].from.id";
        final String SELECT_PERCENT_ALLOCATION = "to["+ProgramCentralConstants.RELATIONSHIP_ASSIGNED_TASKS+"].attribute["+
												 ProgramCentralConstants.ATTRIBUTE_PERCENT_ALLOCATION+"].value";
        
        StringList objSelects = new StringList();
        objSelects.add(ProgramCentralConstants.SELECT_ID);
        objSelects.add(ProgramCentralConstants.SELECT_CURRENT);
        objSelects.add(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
        objSelects.add(SELECT_RELATIONSHIP_ID);
        objSelects.add(SELECT_ASSIGNEE_ID);
        objSelects.add(SELECT_PERCENT_ALLOCATION); 
        
        try{
        	String[] objIdArr = new String[mlObjects.size()];
        	for (int i = 0; i < mlObjects.size(); i++) {
        		Map mObject = (Map) mlObjects.get(i);
                objIdArr[i] = (String)mObject.get(ProgramCentralConstants.SELECT_ID);
        	}
        	
        	MapList taskInfoMapList = DomainObject.getInfo(context, objIdArr, objSelects);
        	
        	for (int i = 0; i < taskInfoMapList.size(); i++) {
        		
        		String sResult = "";
        		
        		String sRelId = "";
                String sText = "";
                double dAllocation = 0.0;
                String sPercentage = "";
                String[] aContents = {"", "", "", "", "", "", "", "", "", "", ""}; // for {"0%", "10%", "20%", "30%", "40%", "50%", "60%", "70%", "80%", "90%", "100%"}; 
                
                Map taskInfoMap = (Map) taskInfoMapList.get(i);
                
                StringList slAssigneeList = ProgramCentralUtil.getAsStringList(taskInfoMap.get(SELECT_ASSIGNEE_ID));
        		StringList slRelIdList = ProgramCentralUtil.getAsStringList(taskInfoMap.get(SELECT_RELATIONSHIP_ID));
        		StringList slPercentAllocationList = ProgramCentralUtil.getAsStringList(taskInfoMap.get(SELECT_PERCENT_ALLOCATION)); 
        		
        		Map mObject = (Map) mlObjects.get(i);

                String sRowID = (String)mObject.get("id[level]");
                String sOID = (String)mObject.get(ProgramCentralConstants.SELECT_ID);
                String sCurrent = (String) taskInfoMap.get(ProgramCentralConstants.SELECT_CURRENT);
                String sIsTaskMgmtType = (String) taskInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
                
                String elementId = sOIDPerson.replace(".", "") + sRowID.replace(",", "");
                
                if("true".equalsIgnoreCase(sIsTaskMgmtType)) {
                    
                    String sStyleTable = "width:100%;";
                    StringBuilder sbStyle = new StringBuilder();
                    StringBuilder sbScriptTable = new StringBuilder();
                    sbScriptTable.append(" onmouseover='if(document.getElementById(\"cancelMarker" + elementId + "\")) document.getElementById(\"cancelMarker" + elementId + "\").style.visibility=\"visible\";");
                    sbScriptTable.append(" document.getElementById(\"percentageMarker" + elementId + "\").style.visibility=\"visible\";'");
                    sbScriptTable.append(" onmouseout='if(document.getElementById(\"cancelMarker" + elementId + "\")) document.getElementById(\"cancelMarker" + elementId + "\").style.visibility=\"hidden\";");
                    sbScriptTable.append(" document.getElementById(\"percentageMarker" + elementId + "\").style.visibility=\"hidden\";'");
                    String sLabel = "";
                    
                    if(slAssigneeList.size() > 0 && slAssigneeList.contains(sOIDPerson)) {
        
                    	int index = slAssigneeList.indexOf(sOIDPerson);
                    	sRelId = (String)slRelIdList.get(index);
                        String sPercent = (String)slPercentAllocationList.get(index);
                        
                        double dValue = Task.parseToDouble(sPercent)/10;                    
                        dValue = Math.round(dValue)/ 1d;                    
                        dAllocation = (dValue/10);
                        sPercentage = String.valueOf(dValue);
                        if(sPercentage.contains(".")) {
                            sPercentage = sPercentage.substring(0, sPercentage.indexOf("."));
                        }
                        
                        sbStyle.append("padding-right:5px;");
                        sbStyle.append("font-size:7pt;");
                        sbStyle.append("color:#FFF;");
                        sbStyle.append("text-shadow: 1px 1px 1px #111;");
                        sbStyle.append("border:1px solid #5f747d;");
                        sbStyle.append("background:-ms-linear-gradient(left,  #5f747d 0%, #5f747d ").append(dAllocation * 100).append("%, #abb8bd ").append(dAllocation).append("%);");
                        sbStyle.append("background:-moz-linear-gradient(left,  #5f747d 0%, #5f747d ").append(dAllocation * 100).append("%, #abb8bd ").append(dAllocation).append("%);");
                        sbStyle.append("background:-webkit-linear-gradient(left,  #5f747d 0%, #5f747d ").append(dAllocation * 100).append("%, #abb8bd ").append(dAllocation).append("%);");

                        sLabel = sPercentage + "0%";
                        
                    }            
                    
                    String sURLPrefix = "<a href='../programcentral/emxProgramCentralUtil.jsp?mode=wbsAllocationView&amp;subMode=allocate&amp;objectId=" + sOID + "&amp;rowId=" + sRowID + "&amp;personId=" + sOIDPerson + "&amp;relId=" + sRelId + "&amp;relationship="+ ProgramCentralConstants.RELATIONSHIP_ASSIGNED_TASKS +"&amp;percent=";
                    String sURLSuffix = "' target='listHidden' style='font-size:6pt;'>";
                    String sStylePercentage = "style='text-align:center;font-size:6pt;' width='17px'";
                    String sStyleSelected = "style='color:#FFF;text-align:center;font-size:6pt;background:#5f747d;' width='17px'";
                    
                    if( !projectState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL) && !ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE.equals(sCurrent) ) { 
                        
                    	if(!sPercentage.equals("0")) {  
                    		aContents[0] = sURLPrefix +   "0.0' style='color:#cc0000 !important;" + sURLSuffix +  "X</a>";
                    	}
                        if(!sPercentage.equals("1")) {  
                        	aContents[1] = sURLPrefix +  "10.0" + sURLSuffix +  "10</a>";
                        }
                        if(!sPercentage.equals("2")) {  
                        	aContents[2] = sURLPrefix +  "20.0" + sURLSuffix +  "20</a>";
                        }
                        if(!sPercentage.equals("3")) {  
                        	aContents[3] = sURLPrefix +  "30.0" + sURLSuffix +  "30</a>";
                        }
                        if(!sPercentage.equals("4")) {  
                        	aContents[4] = sURLPrefix +  "40.0" + sURLSuffix +  "40</a>";
                        }
                        if(!sPercentage.equals("5")) {  
                        	aContents[5] = sURLPrefix +  "50.0" + sURLSuffix +  "50</a>";
                        }
                        if(!sPercentage.equals("6")) {  
                        	aContents[6] = sURLPrefix +  "60.0" + sURLSuffix +  "60</a>";
                        }
                        if(!sPercentage.equals("7")) {  
                        	aContents[7] = sURLPrefix +  "70.0" + sURLSuffix +  "70</a>";
                        }
                        if(!sPercentage.equals("8")) {  
                        	aContents[8] = sURLPrefix +  "80.0" + sURLSuffix +  "80</a>";
                        }
                        if(!sPercentage.equals("9")) {  
                        	aContents[9] = sURLPrefix +  "90.0" + sURLSuffix +  "90</a>";
                        }
                        if(!sPercentage.equals("10")){ 
                        	aContents[10] = sURLPrefix + "100.0" + sURLSuffix + "100</a>";
                        }                    
                        if(sPercentage.equals("")) {  
                        	aContents[0] = "";
                        }
                        
                        if(slAssigneeList.size() > 0 && slAssigneeList.contains(sOIDPerson)) {
                            sText  = sURLPrefix + "0.0" +sURLSuffix + "<img id='cancelMarker"+ elementId +"' style='margin-right:4px;visibility:hidden;' border='0' src='../common/images/buttonMiniCancel.gif' /></a>";
                            sLabel = sPercentage + "0%";
                        }  
                    }
                
                    StringBuilder sbResult = new StringBuilder();
                    sbResult.append("<table style='" + sStyleTable + "'" + sbScriptTable + ">");
                    sbResult.append("<tr style='border:1px solid transparent;'><td colspan='10' style='text-align:right;" + sbStyle.toString() + "line-height:18xp;height:18px;vertical-align:middle;font-weight:bold;'>" + sText  + sLabel + "</td></tr>");
                    sbResult.append("<tr style='visibility:hidden;' id='percentageMarker" + elementId +"'>");
                    
                    if(sPercentage.equals("1")) { 
                    	sbResult.append("<td " + sStyleSelected + ">10</td>"); 
                    } else { 
                    	sbResult.append("<td " + sStylePercentage + ">" + aContents[1] + "</td>"); 
                    }

                    if(sPercentage.equals("2")) { 
                    	sbResult.append("<td " + sStyleSelected + ">20</td>"); 
                    } else { 
                    	sbResult.append("<td " + sStylePercentage + ">" + aContents[2] + "</td>"); 
                    }
                    
                    if(sPercentage.equals("3")) { 
                    	sbResult.append("<td " + sStyleSelected + ">30</td>"); 
                    } else { 
                    	sbResult.append("<td " + sStylePercentage + ">" + aContents[3] + "</td>"); 
                    }
                    
                    if(sPercentage.equals("4")) { 
                    	sbResult.append("<td " + sStyleSelected + ">40</td>"); 
                    } else { 
                    	sbResult.append("<td " + sStylePercentage + ">" + aContents[4] + "</td>"); 
                    }
                    
                    if(sPercentage.equals("5")) { 
                    	sbResult.append("<td " + sStyleSelected + ">50</td>"); 
                    } else { 
                    	sbResult.append("<td " + sStylePercentage + ">" + aContents[5] + "</td>"); 
                    }
                    
                    if(sPercentage.equals("6")) { 
                    	sbResult.append("<td " + sStyleSelected + ">60</td>"); 
                    } else { 
                    	sbResult.append("<td " + sStylePercentage + ">" + aContents[6] + "</td>"); 
                    }
                    
                    if(sPercentage.equals("7")) { 
                    	sbResult.append("<td " + sStyleSelected + ">70</td>"); 
                    } else { 
                    	sbResult.append("<td " + sStylePercentage + ">" + aContents[7] + "</td>"); 
                    }
                    
                    if(sPercentage.equals("8")) { 
                    	sbResult.append("<td " + sStyleSelected + ">80</td>"); 
                    } else { 
                    	sbResult.append("<td " + sStylePercentage + ">" + aContents[8] + "</td>"); 
                    }
                    
                    if(sPercentage.equals("9")) { 
                    	sbResult.append("<td " + sStyleSelected + ">90</td>"); 
                    } else { 
                    	sbResult.append("<td " + sStylePercentage + ">" + aContents[9] + "</td>"); 
                    }
                    
                    if(sPercentage.equals("10")) { 
                    	sbResult.append("<td " + sStyleSelected + ">100</td>"); 
                    } else { 
                    	sbResult.append("<td " + sStylePercentage + ">" + aContents[10] + "</td>"); 
                    }

                    sbResult.append("</tr>");
                    sbResult.append("</table>");
                    sResult = sbResult.toString();
                }
                vResult.add(sResult);
        	}
        } catch(Exception exception){
        	exception.printStackTrace();
        }
        
        return vResult;
    }
    
    
    /**
     * It returns sum of the percent allocation of all the assigned members of the project.
     * 
     * @param context the eMatrix <code>Context</code> object
     * @param args
     * @return
     * @throws Exception if operation fails
     */
    public Vector getTaskTotalAllocation(Context context, String[] args) throws Exception {

        Vector vResult = new Vector();
        Map paramMap = (Map) JPO.unpackArgs(args);
        MapList mlObjects = (MapList) paramMap.get("objectList");        
        
        final String SELECT_PERCENT_ALLOCATION = "to["+ProgramCentralConstants.RELATIONSHIP_ASSIGNED_TASKS+"].attribute["+
												 ProgramCentralConstants.ATTRIBUTE_PERCENT_ALLOCATION+"].value";
        
        StringList objSelects = new StringList();
        objSelects.add(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
        objSelects.add(SELECT_PERCENT_ALLOCATION); 
        try {
        	String[] objIdArr = new String[mlObjects.size()];
            for (int i = 0; i < mlObjects.size(); i++) {
            	Map mObject = (Map) mlObjects.get(i);
                objIdArr[i] = (String)mObject.get(ProgramCentralConstants.SELECT_ID);
            }
            
            MapList taskInfoMapList = DomainObject.getInfo(context, objIdArr, objSelects);
            
            for (int i = 0; i < taskInfoMapList.size(); i++) {
            	
                String sResult          = "";           
                double dTotal           = 0.0;
                
                Map taskInfoMap = (Map) taskInfoMapList.get(i);
                String isTaskMgmtType = (String) taskInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
                
                if("true".equalsIgnoreCase(isTaskMgmtType)) {

                	StringList slPercentAllocationList = ProgramCentralUtil.getAsStringList(taskInfoMap.get(SELECT_PERCENT_ALLOCATION));
                	
                	for (int j = 0; j < slPercentAllocationList.size(); j++) {
                        String sPercent = (String) slPercentAllocationList.get(j);
                        double dPercent = Task.parseToDouble(sPercent);
                        dTotal += dPercent;
                	}
                        sResult = String.valueOf(dTotal) + " %";    
                        sResult = "<span style='text-align:right;'>" + sResult + "</span>";
                }             
                vResult.add(sResult);
            }
        } catch (Exception exception) {
        	exception.printStackTrace();
        	throw exception;
        }
        
        return vResult;
    }
    
    
}
