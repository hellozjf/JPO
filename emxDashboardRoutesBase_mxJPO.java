import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.json.JSONArray;
import com.matrixone.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class emxDashboardRoutesBase_mxJPO {

	public static String[] sColors = {"329cee","f6bd0f","8BBA00","ec0c41","752fc3","AFD8F8","fad46c","c9ff0d","F984A1","A66EDD"};
	public static String colorRed = "#cc0000";
	public static String colorYellow = "#ff7f00";
	public static String colorGreen 	= "#009c00";
	public static String colorGray = "#5f747d";
	public static String relRouteTask = DomainConstants.RELATIONSHIP_ROUTE_TASK;
	public static String relObjectRoute = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;	
	 SimpleDateFormat sdf = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);

    public emxDashboardRoutesBase_mxJPO(Context context, String[] args) throws Exception {}

    public JSONObject getRouteWidgetJsonData(matrix.db.Context context, String[] args) throws Exception {

    	Collection routeAttrMultiValueList = new HashSet(10);
    	routeAttrMultiValueList.add("to["+ relRouteTask +"].from.id");
    	routeAttrMultiValueList.add("to["+ relRouteTask +"].from.current");
    	routeAttrMultiValueList.add("to["+ relRouteTask +"].from.from["+ DomainConstants.RELATIONSHIP_PROJECT_TASK + "].to.attribute["+ DomainConstants.ATTRIBUTE_FIRST_NAME +"]");
    	routeAttrMultiValueList.add("to["+ relRouteTask +"].from.from["+ DomainConstants.RELATIONSHIP_PROJECT_TASK + "].to.attribute["+ DomainConstants.ATTRIBUTE_LAST_NAME +"]");
    	routeAttrMultiValueList.add("to["+ relRouteTask +"].from.attribute["+ DomainConstants.ATTRIBUTE_SCHEDULED_COMPLETION_DATE + "]");
    	routeAttrMultiValueList.add("to["+ relRouteTask +"].from.attribute["+ DomainConstants.ATTRIBUTE_ACTUAL_COMPLETION_DATE +"]");
    	routeAttrMultiValueList.add("to["+ relRouteTask +"].from.attribute["+ DomainConstants.ATTRIBUTE_TITLE +"]");
    	routeAttrMultiValueList.add("to["+ relRouteTask +"].from.attribute["+ DomainConstants.ATTRIBUTE_ROUTE_ACTION+"]");
    	routeAttrMultiValueList.add("to["+ relRouteTask +"].from.from["+DomainConstants.RELATIONSHIP_PROJECT_TASK+"].to.name");
    	routeAttrMultiValueList.add("to["+ relObjectRoute +"].from.type.kindof");

       String sLanguage = context.getLocale().getLanguage();
       Calendar cRecent = Calendar.getInstance();
       Calendar cFuture = Calendar.getInstance();
       String sOID                 = "";
       int[] aCountPurpose     = new int[3];
        cRecent.add(java.util.GregorianCalendar.DAY_OF_YEAR, -14);
        cFuture.add(java.util.GregorianCalendar.DAY_OF_YEAR, 5);

       String sLabelHidePanel = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.HidePanel",  sLanguage);
       String sLabelRoute = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Type.Route",  sLanguage);
       String sLabelTitle= EnoviaResourceBundle.getProperty(context, "Components", "emxFramework.Attribute.Title",  sLanguage);
       String sLabelAction= EnoviaResourceBundle.getProperty(context, "Components", "emxComponents.TaskDetails.Action",  sLanguage);
       String sLabelAssignee= EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.Assignee",  sLanguage);
       String sLabelTargetDate = EnoviaResourceBundle.getProperty(context, "Components", "emxComponents.Routes.ScheduleCompDate",  sLanguage);
       
       JSONObject tooltipObject = new JSONObject();
       tooltipObject.put("labelRoute", sLabelRoute);
       tooltipObject.put("labelTitle", sLabelTitle);
       tooltipObject.put("labelAction", sLabelAction);
       tooltipObject.put("labelAssignee", sLabelAssignee);
       tooltipObject.put("labelTargetDate", sLabelTargetDate);

        java.util.List<String> lTypes       = new ArrayList<String>();
        java.util.List<String> lTemplates   = new ArrayList<String>();

        MapList mlTypes = new MapList();

        StringList busSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_NAME);
        busSelects.add(DomainConstants.SELECT_CURRENT);

        busSelects.add("attribute["+ DomainConstants.ATTRIBUTE_ROUTE_STATUS +"]");
        busSelects.add("attribute[Current Route Node]");
        busSelects.add("to["+ relRouteTask +"]");
        busSelects.add("to["+ relRouteTask +"].from.current");
        busSelects.add("to["+ relRouteTask +"].from.from["+ DomainConstants.RELATIONSHIP_PROJECT_TASK + "].to.attribute["+ DomainConstants.ATTRIBUTE_FIRST_NAME +"]");
        busSelects.add("to["+ relRouteTask +"].from.from["+ DomainConstants.RELATIONSHIP_PROJECT_TASK + "].to.attribute["+ DomainConstants.ATTRIBUTE_LAST_NAME +"]");
        busSelects.add("to["+ relRouteTask +"].from.attribute["+ DomainConstants.ATTRIBUTE_SCHEDULED_COMPLETION_DATE + "]");
        busSelects.add("to["+ relRouteTask +"].from.attribute["+ DomainConstants.ATTRIBUTE_ACTUAL_COMPLETION_DATE +"]");
        busSelects.add("to["+ relRouteTask +"].from.attribute["+ DomainConstants.ATTRIBUTE_TITLE +"]");
        busSelects.add("to["+ relRouteTask +"].from.attribute["+ DomainConstants.ATTRIBUTE_ROUTE_ACTION+"]");
        busSelects.add("to["+ relRouteTask +"].from.from["+DomainConstants.RELATIONSHIP_PROJECT_TASK+"].to.name");
        busSelects.add("to["+ relObjectRoute +"]");
        busSelects.add("to["+ relObjectRoute +"].from.type.kindof");
        busSelects.add("attribute["+ DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE +"]");
        busSelects.add("from["+ DomainConstants.RELATIONSHIP_INITIATING_ROUTE_TEMPLATE +"].to.name");

        MapList mlRoutes        = retrieveRoutesPending(context, busSelects, routeAttrMultiValueList, "");
        MapList mlTasksPending  = new MapList();
        MapList mlTasksRecent   = new MapList();

       StringBuilder strThereAreRouts = new StringBuilder();
       strThereAreRouts.append(EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.ThereAre" , sLanguage));
       strThereAreRouts.append(" <span style='font-weight:bold;color:#000;'>").append(mlRoutes.size()).append("</span> ");
       strThereAreRouts.append(EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.PendingRoutes" , sLanguage));

        for(int i = 0; i < mlRoutes.size(); i++) {

            Map mRoute          = (Map)mlRoutes.get(i);
            String sId          = (String)mRoute.get(DomainConstants.SELECT_ID);
            String sName        = (String)mRoute.get(DomainConstants.SELECT_NAME);
            String sHasTask     = (String)mRoute.get("to["+ relRouteTask +"]");
            String sHasObject   = (String)mRoute.get("to["+ relObjectRoute +"]");
            String sPurpose     = (String)mRoute.get("attribute["+ DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE +"]");
            String sTemplate    = (String)mRoute.get("from["+ DomainConstants.RELATIONSHIP_INITIATING_ROUTE_TEMPLATE +"].to.name");

           if("TRUE".equalsIgnoreCase(sHasTask)) {

           	StringList sStatus      = (StringList)mRoute.get("to["+ relRouteTask +"].from.current");
           	StringList sDateTarget  = (StringList)mRoute.get("to["+ relRouteTask +"].from.attribute["+ DomainConstants.ATTRIBUTE_SCHEDULED_COMPLETION_DATE + "]");
           	StringList sDateActual  = (StringList)mRoute.get("to["+ relRouteTask +"].from.attribute["+ DomainConstants.ATTRIBUTE_ACTUAL_COMPLETION_DATE +"]");
           	StringList sTitle       = (StringList)mRoute.get("to["+ relRouteTask +"].from.attribute["+ DomainConstants.ATTRIBUTE_TITLE +"]");
           	StringList sAction      = (StringList)mRoute.get("to["+ relRouteTask +"].from.attribute["+ DomainConstants.ATTRIBUTE_ROUTE_ACTION +"]");
           	StringList sFirstName   = (StringList)mRoute.get("to["+ relRouteTask +"].from.from["+ DomainConstants.RELATIONSHIP_PROJECT_TASK + "].to.attribute["+ DomainConstants.ATTRIBUTE_FIRST_NAME +"]");
           	StringList sLastName    = (StringList)mRoute.get("to["+ relRouteTask +"].from.from["+ DomainConstants.RELATIONSHIP_PROJECT_TASK + "].to.attribute["+ DomainConstants.ATTRIBUTE_LAST_NAME +"]");
           	StringList sFullNames 	= (StringList)mRoute.get("to["+ relRouteTask +"].from.from["+ DomainConstants.RELATIONSHIP_PROJECT_TASK + "].to.name");

               for(int k = 0; k < sStatus.size(); k++) {
            	   String sFullName = "";
            	   try {
            		   sFullName    = (String)sFullNames.get(k);
            	   } catch (Exception ex) {
            		   //donothing
            	   }
                   if(sFullName.equals(context.getUser())) {
                	   addPendingTaskToMap(context, mlTasksPending, sId, sName, (String)sStatus.get(k), (String)sDateTarget.get(k), (String)sTitle.get(k), (String)sAction.get(k), (String)sFirstName.get(k), (String)sLastName.get(k), sLanguage);
                	   addRecentTaskToMap(context, mlTasksRecent, cRecent, sId, sName, (String)sStatus.get(k), (String)sDateTarget.get(k), (String)sDateActual.get(k), (String)sTitle.get(k), (String)sAction.get(k), (String)sFirstName.get(k), (String)sLastName.get(k), sLanguage);
                   }
               }
            }

            // Route Content Type
            if(sHasObject.equalsIgnoreCase("TRUE")) {
                    StringList slData = (StringList)mRoute.get("to["+ relObjectRoute +"].from.type.kindof");
                    for(int k = 0; k < slData.size(); k++) {
                        String sData = (String)slData.get(k);
                        sData = EnoviaResourceBundle.getTypeI18NString(context, sData, sLanguage);
                        if(!lTypes.contains(sData)) {
                            lTypes.add(sData);
                            Map mType = new HashMap();
                            mType.put("name", (String)slData.get(k));
                            mType.put("label", sData);
                            mlTypes.add(mType);
                        }
                    }
                    }
            // Route Base Purpose counters
           if(sPurpose.equals("Approval")){ 
           	aCountPurpose[0]++; 
           } else if(sPurpose.equals("Review")){
           	aCountPurpose[1]++;
           }else if(sPurpose.equals("Standard")) {
           	aCountPurpose[2]++; 
           }

            // Get list of all templates in use
           if(UIUtil.isNullOrEmpty(sTemplate)) {
           	sTemplate = "-"; 
           	mRoute.put("from["+ DomainConstants.RELATIONSHIP_INITIATING_ROUTE_TEMPLATE +"].to.name", "-"); 
           }
           if(!lTemplates.contains(sTemplate)){ 
           	lTemplates.add(sTemplate); 
           }           
        }


        // Add routes that have been completed recently
        StringBuilder sbWhere = new StringBuilder();
        sbWhere.append("(owner == \"").append(context.getUser()).append("\") && (current == 'Complete')");
        sbWhere.append("&& (modified >= ").append(cRecent.get(Calendar.MONTH)).append("/").append(cRecent.get(Calendar.DAY_OF_MONTH)).append("/").append(cRecent.get(Calendar.YEAR)).append(")");
        MapList mlRoutesRecent = DomainObject.findObjects(context, DomainConstants.TYPE_ROUTE, null, null, null, context.getVault().getName(), sbWhere.toString(), null,
                true, busSelects,(short)0, null, null, routeAttrMultiValueList);
        for(int i = 0; i < mlRoutesRecent.size(); i++) {

            Map mRoute          = (Map)mlRoutesRecent.get(i);
            String sId          = (String)mRoute.get(DomainConstants.SELECT_ID);
            String sName        = (String)mRoute.get(DomainConstants.SELECT_NAME);
            String sHasTask     = (String)mRoute.get("to["+relRouteTask+"]");


            if(sHasTask.equalsIgnoreCase("TRUE")) {
           	StringList sStatus      = (StringList)mRoute.get("to["+relRouteTask+"].from.current");
           	StringList sDateTarget  = (StringList)mRoute.get("to["+relRouteTask+"].from.attribute["+ DomainConstants.ATTRIBUTE_SCHEDULED_COMPLETION_DATE + "]");
           	StringList sDateActual  = (StringList)mRoute.get("to["+relRouteTask+"].from.attribute["+ DomainConstants.ATTRIBUTE_ACTUAL_COMPLETION_DATE +"]");
           	StringList sTitle       = (StringList)mRoute.get("to["+relRouteTask+"].from.attribute["+ DomainConstants.ATTRIBUTE_TITLE +"]");
           	StringList sAction      = (StringList)mRoute.get("to["+relRouteTask+"].from.attribute["+ DomainConstants.ATTRIBUTE_ROUTE_ACTION+"]");
           	StringList sFirstName   = (StringList)mRoute.get("to["+relRouteTask+"].from.from["+ DomainConstants.RELATIONSHIP_PROJECT_TASK + "].to.attribute["+ DomainConstants.ATTRIBUTE_FIRST_NAME +"]");
           	StringList sLastName    = (StringList)mRoute.get("to["+relRouteTask+"].from.from["+ DomainConstants.RELATIONSHIP_PROJECT_TASK + "].to.attribute["+ DomainConstants.ATTRIBUTE_LAST_NAME +"]");

           	for(int k = 0; k < sStatus.size(); k++) {
                  addRecentTaskToMap(context, mlTasksRecent, cRecent, sId, sName, (String)sStatus.get(k), (String)sDateTarget.get(k), (String)sDateActual.get(k), (String)sTitle.get(k), (String)sAction.get(k), (String)sFirstName.get(k), (String)sLastName.get(k), sLanguage);
                }

            }
        }

       mlTasksPending.sort("date", "descending", "date");
       JSONObject widgetItem1 = getRoutePendingTasks(context, mlTasksPending, tooltipObject, sLanguage);
       
       mlTasksRecent.sort("actual", "descending", "date");
       JSONObject widgetItem2 = getRouteRecentTasks(context, mlTasksRecent, tooltipObject, sLanguage);

       JSONObject widgetItem4 = getRouteBasePurposeData(context, aCountPurpose, sLanguage);

       Collections.sort(lTemplates);
       JSONObject widgetItem5= getRouteTemplateData(context, lTemplates, mlRoutes, sLanguage);
      
       Collections.sort(lTypes);
       JSONObject widgetItem3 = getRouteContentTypeData(context, lTypes, mlRoutes, mlTypes, sLanguage);

       JSONObject headerData = new JSONObject();
       headerData.put("hidePanelLabel",sLabelHidePanel);
       headerData.put("headerString", strThereAreRouts.toString());
       
       JSONArray widgetArray = new JSONArray();
       widgetArray.put(widgetItem1);
       widgetArray.put(widgetItem2);
       widgetArray.put(widgetItem3);
       widgetArray.put(widgetItem4);
       widgetArray.put(widgetItem5);
       
       String detailedURL ="../common/emxIndentedTable.jsp?freezePane=Status,Name,NewWindow&toolbar=APPRouteSummaryToolBar" +
       		"&program=emxRoute:getMyDeskActiveRoutes,emxRoute:getMyDeskInActiveRoutes,emxRoute:getAllMyDeskRoutes" +
       		"&programLabel=emxComponents.Filter.Active,emxComponents.Filter.Complete,emxComponents.Filter.All&table=APPRouteSummary" +
       		"&selection=multiple&header=emxComponents.String.RoutesSummary&suiteKey=Components";
       
       JSONObject routeDataObject = new JSONObject();
       routeDataObject.put("header",headerData);       
       routeDataObject.put("widgets", widgetArray);
       routeDataObject.put("detailedURL", detailedURL);
       
       return routeDataObject;
   }
   
   private JSONObject getRoutePendingTasks(matrix.db.Context context, MapList mlTasksPending,
		   JSONObject tooltipObject, String sLanguage) throws Exception{
	   
	 String sLabelPendingTasks = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.PendingTasks", sLanguage);	   
   	 JSONArray taskOverdueArray = new JSONArray();
     JSONArray taskNowArray = new JSONArray();
     JSONArray taskSoonArray = new JSONArray();
     JSONArray seriesPendingTaskArray = new JSONArray();
     JSONArray labelPendingTaskArray = new JSONArray();
     JSONObject taskPendingObjectLink = new JSONObject(); 
       
	 StringBuilder sbPendingWeek         = new StringBuilder();
	 StringBuilder sbPendingMonth        = new StringBuilder();
	 StringBuilder sbPendingOverdue      = new StringBuilder();
	 String sPrefixTasks         = "<a onclick='clickChart(\"../common/emxIndentedTable.jsp?table=APPTaskSummary&freezePane=Name,Title&program=emxDashboardRoutes:getRouteTasksPending&mode=";
	 String sSuffixTasks         = "&suiteKey=Framework&selection=multiple&header=";
	 String sLabelThisWeek   = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.ThisWeek" , sLanguage);
	 String sLabelThisMonth  = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.ThisMonth", sLanguage);
	 String sLabelOverdue= EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.Overdue",  sLanguage);
   	 String sLabelNow= EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.Now",  sLanguage);
   	 String sLabelSoon= EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.Soon",  sLanguage);

       int[] aCountPending     = new int[3];

       Calendar cNow               = Calendar.getInstance();
       Calendar cDate              = Calendar.getInstance();
       Calendar cRecent            = Calendar.getInstance();
       Calendar cFuture            = Calendar.getInstance();

       cRecent.add(java.util.GregorianCalendar.DAY_OF_YEAR, -14);
       cFuture.add(java.util.GregorianCalendar.DAY_OF_YEAR, 5);
       int iYearNow       = cNow.get(Calendar.YEAR);
       int iMonthNow      = cNow.get(Calendar.MONTH);
       int iWeekNow       = cNow.get(Calendar.WEEK_OF_YEAR);

   		int iMaxPending = 0;
        for(int i = 0; i < mlTasksPending.size(); i++) {

            Map mTaskPending            = (Map)mlTasksPending.get(i);
            String sTitle               = (String)mTaskPending.get("title");
            String sDate                = (String)mTaskPending.get("date");
            //String sDateLabel           = (String)mTaskPending.get("date");
            String sPerson              = (String)mTaskPending.get("person");
            String sAction              = (String)mTaskPending.get("action");
            String sRoute               = (String)mTaskPending.get("name");
            String sId                  = (String)mTaskPending.get("id");

            if(sTitle.equals("")) { sTitle = "(" + sAction + ")"; }
            else if(sTitle.equals(" ")) { sTitle = "(" + sAction + ")"; }

            if(UIUtil.isNotNullAndNotEmpty(sDate)){
                iMaxPending++;
                labelPendingTaskArray.put(sTitle);
                cDate.setTime(sdf.parse(sDate));

                int iYear   = cDate.get(Calendar.YEAR);
                int iMonth  = cDate.get(Calendar.MONTH);
                int iWeek   = cDate.get(Calendar.WEEK_OF_YEAR);
                int iDay    = cDate.get(Calendar.DAY_OF_MONTH);

                JSONObject taskDataObject = new JSONObject();
                taskDataObject.put("id", sId);
                taskDataObject.put("title", sTitle);
                taskDataObject.put("route", sRoute);
                taskDataObject.put("person", sPerson);
                taskDataObject.put("action", sAction);
                taskDataObject.put("date", cDate.getTimeInMillis());
                taskDataObject.put("x", cDate.getTimeInMillis());
                taskDataObject.put("y", i);

                if(cDate.before(cNow)){
               	 taskOverdueArray.put(taskDataObject); 
               	 aCountPending[2]++; 
                }else {
                    if(cDate.after(cFuture)){
                   	 taskSoonArray.put(taskDataObject); 
                    }else{
                   	 taskNowArray.put(taskDataObject); 
                    }
                }

                if(iYear == iYearNow) {
                    if(iWeek == iWeekNow)   { aCountPending[0]++; }
                    if(iMonth == iMonthNow) { aCountPending[1]++;     }
                }

            }

        }

   	 JSONObject taskOverdueDataObject = new JSONObject();
   	 taskOverdueDataObject.put("name", sLabelOverdue);
   	 taskOverdueDataObject.put("color", colorRed);  	 
   	 taskOverdueDataObject.put("data", taskOverdueArray);   	 
	   	 JSONObject taskOverdueMarkerObject = new JSONObject();
	   	 taskOverdueMarkerObject.put("fillColor", "#ffffff");
	   	 taskOverdueMarkerObject.put("lineColor", colorRed);
	   	 taskOverdueDataObject.put("marker", taskOverdueMarkerObject);
   	 
   	 JSONObject taskNowDataObject = new JSONObject();
   	 taskNowDataObject.put("name", sLabelNow);
   	 taskNowDataObject.put("color", colorYellow);  	 
   	 taskNowDataObject.put("data", taskNowArray);
   	 	JSONObject taskNowDataMarkerObject = new JSONObject();
   	 	taskNowDataMarkerObject.put("fillColor", "#ffffff");
   	 	taskNowDataMarkerObject.put("lineColor", colorYellow);
   	 	taskNowDataObject.put("marker", taskNowDataMarkerObject);
   	 
   	 JSONObject taskSoonDataObject = new JSONObject();
   	 taskSoonDataObject.put("name", sLabelSoon);
   	 taskSoonDataObject.put("color", colorGreen);
   	 taskSoonDataObject.put("data", taskSoonArray);
   	 	JSONObject taskSoonMarkerObject = new JSONObject();
   	 	taskSoonMarkerObject.put("fillColor", "#ffffff");
   	 	taskSoonMarkerObject.put("lineColor", colorGreen);
   	 	taskSoonDataObject.put("marker", taskSoonMarkerObject);
   	 
   	 seriesPendingTaskArray.put(taskOverdueDataObject);
   	 seriesPendingTaskArray.put(taskNowDataObject);
   	 seriesPendingTaskArray.put(taskSoonDataObject);

        sbPendingWeek.append("<span style='font-weight:bold;'>").append(aCountPending[0]).append("</span> ").append(sPrefixTasks).append("week").append(sSuffixTasks).append("emxFramework.String.PendingTasksThisWeek").append("\")'>").append(sLabelThisWeek).append("</a>");
        sbPendingMonth.append("<span style='font-weight:bold;'>").append(aCountPending[1]).append("</span> ").append(sPrefixTasks).append("month").append(sSuffixTasks).append("emxFramework.String.PendingTasksThisMonth").append("\")'>").append(sLabelThisMonth).append("</a>");
        sbPendingOverdue.append("<span style='font-weight:bold;'>").append(aCountPending[2]).append("</span> ").append(sPrefixTasks).append("overdue").append(sSuffixTasks).append("emxFramework.String.PendingTasksOverdue").append("\")'>").append(sLabelOverdue).append("</a>");

     taskPendingObjectLink.put("taskPendingThisWeek", sbPendingWeek.toString());
   	 taskPendingObjectLink.put("taskPendingThisMonth", sbPendingMonth.toString());
   	 taskPendingObjectLink.put("taskPendingOverDue", sbPendingOverdue.toString());

   	    int iHeightPending = 40 + (iMaxPending * 17);
        if(iHeightPending < 160) {
       	 iHeightPending = 160; 
        }

        JSONObject widgetItem1 = new JSONObject();        
        widgetItem1.put("label", sLabelPendingTasks);
        widgetItem1.put("series", seriesPendingTaskArray);
        widgetItem1.put("name", "Pending");
        widgetItem1.put("type", "scatter");
        widgetItem1.put("height", iHeightPending);
        widgetItem1.put("yMax", iMaxPending-1);
        widgetItem1.put("yAxisCategories", labelPendingTaskArray);        
        widgetItem1.put("view", "expanded");
        widgetItem1.put("bottomLineData", taskPendingObjectLink);
        widgetItem1.put("xAxisDateValue", Calendar.getInstance().getTimeInMillis());
        widgetItem1.put("tooltipObject", tooltipObject);
        widgetItem1.put("filterable", true);
        widgetItem1.put("showLegend", true);        
        widgetItem1.put("filterURL", "../common/emxPortal.jsp?portal=APPRoutePowerView&suiteKey=Components" +
        		"&StringResourceFileId=emxComponentsStringResource&SuiteDirectory=components&emxSuiteDirectory=components");

        if(seriesPendingTaskArray.length()==0){
        	widgetItem1.put("view", "collapsed");
        }                 
        return widgetItem1;
   }
   
   private JSONObject getRouteRecentTasks(matrix.db.Context context, MapList mlTasksRecent, 
		   JSONObject tooltipObject, String sLanguage) throws Exception{

	   JSONArray seriesRecentTaskArray = new JSONArray();
       JSONArray labelRecentTaskArray = new JSONArray();        
       JSONArray taskWithTargetDateArray = new JSONArray();
       JSONArray taskDelayedArray = new JSONArray();
       JSONArray taskInTimeArray = new JSONArray();
       JSONArray taskWitoutTargetArray = new JSONArray();
       
       String sLabelForRecentTasks = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.RecentUpdates", sLanguage);
       String sLabelTarget = EnoviaResourceBundle.getProperty(context, "Components", "emxComponents.Routes.ScheduleCompDate",  sLanguage);
	   String sLabelInTime = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.InTime",  sLanguage);
	   String sLabelDelayed = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.Delayed",  sLanguage);
	   String sLabelNoTarget = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.NoTargetDate",  sLanguage);
       Calendar cDate              = Calendar.getInstance();
   	   int iMaxRecent          = 0;

        for(int i = 0; i < mlTasksRecent.size(); i++) {

            Map mTaskRecent             = (Map)mlTasksRecent.get(i);
            String sTitle               = (String)mTaskRecent.get("title");
            String sTarget              = (String)mTaskRecent.get("target");
            //String sTargetLabel         = (String)mTaskRecent.get("target");
            String sActual              = (String)mTaskRecent.get("actual");
            String sPerson              = (String)mTaskRecent.get("person");
            String sAction              = (String)mTaskRecent.get("action");
            String sRoute               = (String)mTaskRecent.get("name");
            String sId                  = (String)mTaskRecent.get("id");
            
            Calendar cTarget = Calendar.getInstance();
            cTarget .setTime(sdf.parse(sTarget));
            int iYearTarget   = cTarget.get(Calendar.YEAR);
            int iMonthTarget  = cTarget.get(Calendar.MONTH);
            int iDayTarget    = cTarget.get(Calendar.DAY_OF_MONTH);
            
            if(sTitle.equals("")) { sTitle = "(" + sAction + ")"; }
            else if(sTitle.equals(" ")) { sTitle = "(" + sAction + ")"; }

            if(!"".equals(sActual)) {

                iMaxRecent++;
                labelRecentTaskArray.put(sTitle);
                cDate.setTime(sdf.parse(sActual));

               JSONObject taskDataObject = new JSONObject();
               taskDataObject.put("id", sId);
               taskDataObject.put("title", sTitle);
               taskDataObject.put("route", sRoute);
               taskDataObject.put("person", sPerson);
               taskDataObject.put("action", sAction);
               taskDataObject.put("date", cDate.getTimeInMillis());
               taskDataObject.put("x", cDate.getTimeInMillis());               
               taskDataObject.put("y", i);

               if(UIUtil.isNullOrEmpty(sTarget)) {                	
               	taskWitoutTargetArray.put(taskDataObject);
               }else {
                   JSONObject cloneTaskDataObject = new JSONObject(taskDataObject.toString());
                   cloneTaskDataObject.put("x", cTarget.getTimeInMillis());
                   taskWithTargetDateArray.put(cloneTaskDataObject);

                    if(cDate.after(cTarget)) {
                     	taskDelayedArray.put(taskDataObject);
                    } else {
                     	taskInTimeArray.put(taskDataObject);
                    }

                }

            }

        }

       JSONObject taskWithTargetMarkerObject = new JSONObject();
       taskWithTargetMarkerObject.put("fillColor", "#ffffff");
       taskWithTargetMarkerObject.put("lineColor", colorYellow);       
       JSONObject taskWithTargetDataObject = new JSONObject();
       taskWithTargetDataObject.put("name", sLabelTarget);
       taskWithTargetDataObject.put("color", colorYellow);  	 
       taskWithTargetDataObject.put("data", taskWithTargetDateArray);
       taskWithTargetDataObject.put("marker", taskWithTargetMarkerObject);
       seriesRecentTaskArray.put(taskWithTargetDataObject);
       
       JSONObject taskDelayedMarkerObject = new JSONObject();
       taskDelayedMarkerObject.put("fillColor", "#ffffff");
       taskDelayedMarkerObject.put("lineColor", colorRed);       
       JSONObject taskDelayedDataObject = new JSONObject();
       taskDelayedDataObject.put("name", sLabelDelayed);
       taskDelayedDataObject.put("color", colorRed);  	 
       taskDelayedDataObject.put("data", taskDelayedArray);
       taskDelayedDataObject.put("marker", taskDelayedMarkerObject);
       seriesRecentTaskArray.put(taskDelayedDataObject);
       
   	   JSONObject taskInTimeMarkerObject = new JSONObject();
   	   taskInTimeMarkerObject.put("fillColor", "#ffffff");
   	   taskInTimeMarkerObject.put("lineColor", colorGreen);       
   	   JSONObject taskInTimeDataObject = new JSONObject();
       taskInTimeDataObject.put("name", sLabelInTime);
       taskInTimeDataObject.put("color", colorGreen);  	 
       taskInTimeDataObject.put("data", taskInTimeArray);
       taskInTimeDataObject.put("marker", taskInTimeMarkerObject);
       seriesRecentTaskArray.put(taskInTimeDataObject);
       	   
       JSONObject taskWithoutTargetDataMarkerObject = new JSONObject();       
   	   taskWithoutTargetDataMarkerObject.put("fillColor", "#ffffff");
   	   taskWithoutTargetDataMarkerObject.put("lineColor", colorGray);       
   	   JSONObject taskWithoutTargetDataObject = new JSONObject();
       taskWithoutTargetDataObject.put("name", sLabelNoTarget);
       taskWithoutTargetDataObject.put("color", colorGray);  	 
       taskWithoutTargetDataObject.put("data", taskWitoutTargetArray);
       taskWithoutTargetDataObject.put("marker", taskWithoutTargetDataMarkerObject);
       seriesRecentTaskArray.put(taskWithoutTargetDataObject);

       int iHeightRecent	= 78 + (iMaxRecent * 20);
       if(iHeightRecent < 150) {
       	iHeightRecent = 150; 
       }

       JSONObject widgetItem2 = new JSONObject();        
       widgetItem2.put("label", sLabelForRecentTasks);
       widgetItem2.put("series", seriesRecentTaskArray);
       widgetItem2.put("name", "Recent");
       widgetItem2.put("type", "scatter");
       widgetItem2.put("height", iHeightRecent);
       widgetItem2.put("yMax", iMaxRecent-1);
       widgetItem2.put("yAxisCategories", labelRecentTaskArray);
       widgetItem2.put("view", "expanded");
       widgetItem2.put("xAxisDateValue", Calendar.getInstance().getTimeInMillis());
       widgetItem2.put("tooltipObject", tooltipObject);
       widgetItem2.put("filterable", true);
       widgetItem2.put("showLegend", true);
       widgetItem2.put("filterURL", "../common/emxPortal.jsp?portal=APPRoutePowerView&suiteKey=Components" +
		"&StringResourceFileId=emxComponentsStringResource&SuiteDirectory=components&emxSuiteDirectory=components");
       if(seriesRecentTaskArray.length()==0){
       	widgetItem2.put("view", "collapsed");
       }
       return widgetItem2;
        }

   private JSONObject getRouteBasePurposeData(matrix.db.Context context, int[] aCountPurpose, String sLanguage) 
	throws Exception{
	  
	   String sLabelPurpose = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.RouteBasePurpose", sLanguage);
   
	   JSONArray seriesPurposeArray = new JSONArray();
	   JSONObject seriesPurposeApprove = new JSONObject();
	   seriesPurposeApprove.put("name",EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.Route_Base_Purpose.Approval", sLanguage));
	   seriesPurposeApprove.put("color","#8BBA00");
	   seriesPurposeApprove.put("y",aCountPurpose[0]);
	   seriesPurposeApprove.put("value","Approval");
	   seriesPurposeArray.put(seriesPurposeApprove);
       
       JSONObject seriesPurposeReview = new JSONObject();
       seriesPurposeReview.put("name",EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.Route_Base_Purpose.Review", sLanguage));
       seriesPurposeReview.put("color","#329CEE");        
       seriesPurposeReview.put("y",aCountPurpose[1]);
       seriesPurposeReview.put("value","Review");
       seriesPurposeArray.put(seriesPurposeReview);
       
       JSONObject seriesPurposeStandard = new JSONObject();
       seriesPurposeStandard.put("name",EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.Route_Base_Purpose.Standard", sLanguage));
       seriesPurposeStandard.put("color","#F6BD0F");
       seriesPurposeStandard.put("y",aCountPurpose[2]);
       seriesPurposeStandard.put("value","Standard");
       seriesPurposeArray.put(seriesPurposeStandard);
       
	   JSONObject dataItem4 = new JSONObject();       
       dataItem4.put("data", seriesPurposeArray);
       dataItem4.put("name", sLabelPurpose);
       JSONArray seriesRoutePurposeFinal = new JSONArray();
       seriesRoutePurposeFinal.put(dataItem4);
       
       JSONObject widgetItem4 = new JSONObject();        
       widgetItem4.put("label", sLabelPurpose);
       widgetItem4.put("series", seriesRoutePurposeFinal);
       widgetItem4.put("name", "Purpose");
       widgetItem4.put("type", "pie");
       widgetItem4.put("view", "expanded");
       widgetItem4.put("height", 140);
       widgetItem4.put("filterable", true);
       widgetItem4.put("filterURL","../common/emxIndentedTable.jsp?editLink=true&selection=multiple&program=emxDashboardRoutes:getRoutesPending" +
  		"&table=APPRouteSummary&freezePane=Status,Name,NewWindow");
       if(seriesPurposeArray.length()==0){
       	widgetItem4.put("view", "collapsed");
       }
	
       return widgetItem4;
   }
   
   private JSONObject getRouteTemplateData(matrix.db.Context context, java.util.List<String> lTemplates, MapList mlRoutes,
		   String sLanguage)throws Exception{
	   
       JSONArray seriesTemplateDataArray = new JSONArray();
       JSONArray labelTemplateArray = new JSONArray();   	
	   String sLabelForRouteTemplate = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.RouteTemplate", sLanguage);

        for(int i = 0; i < lTemplates.size(); i++) {
       	labelTemplateArray.put(lTemplates.get(i));
        }

        int[] aCountTemplate = new int[lTemplates.size()];

        for(int i = 0; i < mlRoutes.size(); i++) {

            Map mRoute          = (Map)mlRoutes.get(i);
            String sTemplate    = (String)mRoute.get("from["+ DomainConstants.RELATIONSHIP_INITIATING_ROUTE_TEMPLATE +"].to.name");

            aCountTemplate[lTemplates.indexOf(sTemplate)]++;
            }

       for(int i = 0; i < aCountTemplate.length; i++) {
       	JSONObject seriesTemplate = new JSONObject();
       	seriesTemplate.put("color", "#"+sColors[i%sColors.length]);
       	seriesTemplate.put("y", aCountTemplate[i]);
       	seriesTemplateDataArray.put(seriesTemplate);
       	//sbDataTemplate.append("{ color:'#").append(sColors[i%sColors.length]).append("', y:").append(aCountTemplate[i]).append("},"); 
        }

       int iHeightTemplate 	= 32 + (aCountTemplate.length * 28);

	   JSONObject dataObj = new JSONObject();       
       dataObj.put("data", seriesTemplateDataArray);
       dataObj.put("name", sLabelForRouteTemplate);       
       JSONArray seriesRouteTemplateFinal = new JSONArray();
       seriesRouteTemplateFinal.put(dataObj);
       
       JSONObject widgetItem5 = new JSONObject();        
       widgetItem5.put("label", sLabelForRouteTemplate);
       widgetItem5.put("series", seriesRouteTemplateFinal);
       widgetItem5.put("name", "Template");
       widgetItem5.put("type", "bar");
       widgetItem5.put("height", iHeightTemplate );
       widgetItem5.put("xAxisCategories", labelTemplateArray );        
       widgetItem5.put("view", "expanded");
       widgetItem5.put("filterable", true);
       widgetItem5.put("tooltipEnabled", false);
       widgetItem5.put("filterURL","../common/emxIndentedTable.jsp?editLink=true&selection=multiple&program=emxDashboardRoutes:getRoutesPending" +
       		"&table=APPRouteSummary&freezePane=Status,Name,NewWindow");
       if(seriesTemplateDataArray.length()==0){
       	widgetItem5.put("view", "collapsed");
       }
       
       return widgetItem5;
   }

   private JSONObject getRouteContentTypeData(matrix.db.Context context, java.util.List<String> lTypes, MapList mlRoutes, MapList mlTypes, 
   		String sLanguage) throws Exception{

	   String sLabelContentType = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.ContentType", sLanguage);
       JSONArray seriesContentTypeArray = new JSONArray();
       JSONArray labelContentTypeArray = new JSONArray();

	   	for(int i = 0; i < lTypes.size(); i++) {
	   		labelContentTypeArray.put(lTypes.get(i));
        }

	   	int[] aCountType = new int[lTypes.size()];        
        for(int i = 0; i < mlRoutes.size(); i++) {

            Map mRoute          = (Map)mlRoutes.get(i);
            String sHasObject   = (String)mRoute.get("to["+ relObjectRoute +"]");
            if(sHasObject.equalsIgnoreCase("TRUE")) {
                java.util.List<String> lPreviousTypes = new ArrayList<String>();
                    StringList slData = (StringList)mRoute.get("to["+relObjectRoute+"].from.type.kindof");

                    for(int k = 0; k < slData.size(); k++) {
                        String sData = (String)slData.get(k);
                        if(!lPreviousTypes.contains(sData)) {
                            sData = EnoviaResourceBundle.getTypeI18NString(context, sData, sLanguage);
                            aCountType[lTypes.indexOf(sData)]++;
                            lPreviousTypes.add(sData);
                        }
                    }
                    }
                }

        for(int i = 0; i < aCountType.length; i++) {
            String sFilter = "";
            for(int j = 0; j < mlTypes.size(); j++) {
                Map mType = (Map)mlTypes.get(j);
                String sTypeLabel = (String)mType.get("label");
                if(sTypeLabel.equals(lTypes.get(i))) {
                    sFilter = (String)mType.get("name");
                    break;
                }
            }

           JSONObject seriesContentType = new JSONObject();
           seriesContentType.put("filter", sFilter);
           seriesContentType.put("color", "#"+sColors[i%sColors.length]);
           seriesContentType.put("y", aCountType[i]);
           seriesContentTypeArray.put(seriesContentType);
           //sbDataType.append("{ filter:'").append(sFilter).append("', color:'#").append(sColors[i%sColors.length]).append("', y:").append(aCountType[i]).append("},");
        }

       int iHeightContentType 	= 32 + (aCountType.length * 28);

	   JSONObject dataObj = new JSONObject();       
	   dataObj.put("data", seriesContentTypeArray);
	   dataObj.put("name", sLabelContentType);       
       JSONArray seriesContentTypeFinal = new JSONArray();
       seriesContentTypeFinal.put(dataObj);
       
       JSONObject widgetItem3 = new JSONObject();        
       widgetItem3.put("label", sLabelContentType);
       widgetItem3.put("series", seriesContentTypeFinal);
       widgetItem3.put("name", "ContentType");
       widgetItem3.put("type", "bar");
       widgetItem3.put("height", iHeightContentType);
       widgetItem3.put("xAxisCategories", labelContentTypeArray);
       widgetItem3.put("view", "expanded");
       widgetItem3.put("filterable", true);       
       widgetItem3.put("tooltipEnabled", false);
       widgetItem3.put("filterURL","../common/emxIndentedTable.jsp?editLink=true&selection=multiple&program=emxDashboardRoutes:getRoutesPending" +
       		"&table=APPRouteSummary&freezePane=Status,Name,NewWindow");
       if(seriesContentTypeArray.length()==0){
       	widgetItem3.put("view", "collapsed");
        }

       return widgetItem3;
    }

   private void addPendingTaskToMap(Context context, MapList mlTasksPending, String sId, String sName, String sStatus, 
		   String sDateTarget, String sTitle, String sAction, String sFirstName, String sLastName, String sLanguage) {

        if(!sStatus.equals("Complete")) {

           if(!sDateTarget.equals("")) {
           	if(!sDateTarget.equals(" ")) {
           		try {
           			Map mPendingTask = new HashMap();
           			String sPerson = sLastName.toUpperCase() + " " + sFirstName;
           			
           			if("Approve".equals(sAction)) {
	                	sAction=EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.Route_Action.Approve", sLanguage);
	                } else if("Comment".equals(sAction)) {
	                	sAction=EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.Route_Action.Comment", sLanguage);	
	                } else if("Notify Only".equals(sAction)) {
	                	sAction=EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.Route_Action.Notify_Only", sLanguage);	
	                }

	                mPendingTask.put("id", sId);
	                mPendingTask.put("name", sName);
	                mPendingTask.put("date", sDateTarget);
	                mPendingTask.put("title", sTitle);
	                mPendingTask.put("action", sAction);
	                mPendingTask.put("person", sPerson);

	                mlTasksPending.add(mPendingTask);
           		} catch(FrameworkException e) {
           			e.printStackTrace();
           		}
           	}
          }
        }
    }

   private void addRecentTaskToMap(Context context, MapList mlTasksRecent, Calendar cRecent, String sId, String sName, String sStatus, String sDateTarget,
		   String sDateActual, String sTitle, String sAction, String sFirstName, String sLastName, String sLanguage) throws ParseException {

        if(!sDateActual.equals(" ")) {
            if(!sDateActual.equals("")) {

                Calendar cDate = Calendar.getInstance();
                cDate.setTime(sdf.parse(sDateActual));

                if(cDate.after(cRecent)) {
                	try {
                		Map mPendingTask = new HashMap();
                		String sPerson = sLastName.toUpperCase() + " " + sFirstName;
                		
                		if("Approve".equals(sAction)) {
		                	sAction=EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.Route_Action.Approve", sLanguage);
		                } else if("Comment".equals(sAction)) {
		                	sAction=EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.Route_Action.Comment", sLanguage);
		                } else if("Notify Only".equals(sAction)) {
		                	sAction=EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.Route_Action.Notify_Only", sLanguage);		
		                }

		                mPendingTask.put("id", sId);
		                mPendingTask.put("name", sName);
		                mPendingTask.put("target", sDateTarget);
		                mPendingTask.put("actual", sDateActual);
                    	mPendingTask.put("title", sTitle);
                    	mPendingTask.put("action", sAction);
                    	mPendingTask.put("person", sPerson);

                    	mlTasksRecent.add(mPendingTask);
                	} catch(FrameworkException e) {
                		e.printStackTrace();
                	}
                }
            }
       }
   }

   public MapList retrieveRoutesPending(matrix.db.Context context, StringList busSelects, String sFilter) throws FrameworkException {
       
	   String sWhere = "(to["+DomainConstants.RELATIONSHIP_ROUTE_TASK+"].from.from["+DomainConstants.RELATIONSHIP_PROJECT_TASK+"].to.name == \""+context.getUser() +"\") && (current == 'In Process') && (attribute[Route Status] == 'Started')";
       if(!sFilter.equals("")) {
           sWhere += " && " + sFilter;
       }
       return DomainObject.findObjects(context, DomainConstants.TYPE_ROUTE, context.getVault().getName(), sWhere, busSelects);
    }
    
   public MapList retrieveRoutesPending(matrix.db.Context context, StringList busSelects, Collection routeAttrMultiValueList, 
		   String sFilter) throws FrameworkException {
       
	   String sWhere = "(to["+DomainConstants.RELATIONSHIP_ROUTE_TASK+"].from.from["+DomainConstants.RELATIONSHIP_PROJECT_TASK+"].to.name == \""+context.getUser() +"\") && (current == 'In Process') && (attribute[Route Status] == 'Started')";
	   if(!sFilter.equals("")) {
           sWhere += " && " + sFilter;
       }
       return DomainObject.findObjects(context, DomainConstants.TYPE_ROUTE, null, null, null, context.getVault().getName(), sWhere, null,
               true, busSelects,(short)0, null, null, routeAttrMultiValueList);
   }
    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRoutesPending(Context context, String[] args) throws FrameworkException, Exception {


        HashMap paramMap        = (HashMap) JPO.unpackArgs(args);
        String sOID             = (String) paramMap.get("objectId");
        String sFilterContent   = (String) paramMap.get("filterContentType");
        String sFilterTemplate  = (String) paramMap.get("filterTemplate");
        String sFilterPurpose   = (String) paramMap.get("filterPurpose");
        String relObjectRoute = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;
        String relInitiatingRouteTemplate = DomainConstants.RELATIONSHIP_INITIATING_ROUTE_TEMPLATE;

        StringBuilder sbWhere = new StringBuilder();
        StringList busSelects = new StringList();
        busSelects.add("id");
		busSelects.add("attribute["+DomainConstants.ATTRIBUTE_RESTRICT_MEMBERS+"]");



        if(null != sFilterContent) {
            if(sbWhere.length() > 0) { sbWhere.append(" && "); }
            busSelects.add("to["+ relObjectRoute +"]");
            busSelects.add("to["+ relObjectRoute +"].from.type.kindof");
            sbWhere.append("(to["+ relObjectRoute +"] == True)");
            sbWhere.append("&& (to["+ relObjectRoute+"].from.type.kindof == '");
            sbWhere.append(sFilterContent);
            sbWhere.append("')");
        }
        if(null != sFilterPurpose) {
            if(sbWhere.length() > 0) { sbWhere.append(" && "); }
            sbWhere.append("(attribute["+ DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE +"] == '").append(sFilterPurpose).append("')");
        }
        if(null != sFilterTemplate) {
            if(sbWhere.length() > 0) { sbWhere.append(" && "); }
            if(sFilterTemplate.equals("-")) {
                busSelects.add("from["+ relInitiatingRouteTemplate +"]");
                sbWhere.append("(from["+ relInitiatingRouteTemplate +"] == False)");
            } else {
                busSelects.add("from["+ relInitiatingRouteTemplate +"].to.name");
                sbWhere.append("(from["+ relInitiatingRouteTemplate +"].to.name == '");
                sbWhere.append(sFilterTemplate);
                sbWhere.append("')");
            }
        }
        return retrieveRoutesPending(context, busSelects, sbWhere.toString());
        }

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRouteTasksPending(Context context, String[] args) throws FrameworkException, Exception {


        MapList mlResults   = new MapList();
        HashMap paramMap    = (HashMap) JPO.unpackArgs(args);
        String sOID         = (String) paramMap.get("objectId");
        String sMode        = (String) paramMap.get("mode");
        Calendar cNow       = Calendar.getInstance();
        int iYearNow        = cNow.get(Calendar.YEAR);
        int iWeekNow        = cNow.get(Calendar.WEEK_OF_YEAR);
        int iMonthNow       = cNow.get(Calendar.MONTH);
        Long lNow           = cNow.getTimeInMillis();

        java.util.List<String> lIds = new ArrayList<String>();

        StringList busSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_NAME);
        busSelects.add(DomainConstants.SELECT_CURRENT);
        busSelects.add("attribute["+ PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_attribute_CurrentRouteNode) +"]");

        Collection routeAttrMultiValueList = new HashSet(3);
        routeAttrMultiValueList.add("to["+ relRouteTask +"].from.id");
		routeAttrMultiValueList.add("to["+ relRouteTask +"].from.owner");
        routeAttrMultiValueList.add("to["+ relRouteTask +"].from.current");
        routeAttrMultiValueList.add("to["+ relRouteTask +"].from.attribute["+ DomainConstants.ATTRIBUTE_SCHEDULED_COMPLETION_DATE + "]");
        
        busSelects.add("to["+ relRouteTask +"]");
        busSelects.add("to["+ relRouteTask +"].from.id");
		busSelects.add("to["+ relRouteTask +"].from.owner");
        busSelects.add("to["+ relRouteTask +"].from.current");
        busSelects.add("to["+ relRouteTask +"].from.attribute["+ DomainConstants.ATTRIBUTE_SCHEDULED_COMPLETION_DATE +"]");

        MapList mlRoutes = retrieveRoutesPending(context, busSelects, routeAttrMultiValueList, "(to[Route Task] == True)");

        for(int i = 0; i < mlRoutes.size(); i++) {

            Map mRoute      = (Map)mlRoutes.get(i);
            StringList slId = (StringList)mRoute.get("to["+ relRouteTask +"].from.id");
			StringList slTaskOwner = (StringList)mRoute.get("to["+ relRouteTask +"].from.owner");
            StringList slStatus = (StringList)mRoute.get("to["+ relRouteTask +"].from.current");
            StringList slTarget = (StringList)mRoute.get("to["+ relRouteTask +"].from.attribute["+ DomainConstants.ATTRIBUTE_SCHEDULED_COMPLETION_DATE +"]");
            if(slStatus.size()>0) {
                for(int j = 0; j < slStatus.size(); j++) {
                    if(context.getUser().equals((String)slTaskOwner.get(j))) {
                		addPendingTaskByMode(lIds, sMode, iYearNow, iMonthNow, iWeekNow, lNow, (String)slId.get(j),(String) slStatus.get(j), (String)slTarget.get(j));
                	}
                }
            }
        }

        String[] aIds = new String[lIds.size()];
        for(int i = 0; i < aIds.length; i++) { 
        	aIds[i] = lIds.get(i); 
        }
        StringList slTask = new StringList();

        slTask.add(DomainConstants.SELECT_ID);
        slTask.add("attribute["+DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE+"]");

        slTask.add("attribute["+ DomainConstants.ATTRIBUTE_ROUTE_INSTRUCTIONS+"]");
        slTask.add("attribute["+ DomainConstants.ATTRIBUTE_ACTUAL_COMPLETION_DATE+"]");
        slTask.add(DomainConstants.SELECT_TYPE);
        slTask.add("attribute["+ DomainConstants.ATTRIBUTE_TASK_ACTUAL_FINISH_DATE+"]");

        slTask.add("attribute["+ PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_attribute_DueDate)+"]");
        slTask.add("attribute["+ DomainConstants.ATTRIBUTE_ROUTE_ACTION +"]");
        slTask.add("attribute["+ DomainConstants.ATTRIBUTE_SCHEDULED_COMPLETION_DATE +"]");
        slTask.add("from["+ relRouteTask +"].to.id");
        slTask.add("from["+ relRouteTask +"].to.Name");
		slTask.add("from[" + relRouteTask + "].to.to[" + DomainConstants.RELATIONSHIP_ROUTE_SCOPE + "].from.id");
		slTask.add("from[" + relRouteTask + "].to.to[" + DomainConstants.RELATIONSHIP_ROUTE_SCOPE + "].from.name");
        slTask.add(DomainConstants.SELECT_DESCRIPTION);
        slTask.add(DomainConstants.SELECT_CURRENT);

        mlResults = DomainObject.getInfo(context, aIds, slTask);

        return mlResults;
    }
    
    public void addPendingTaskByMode(java.util.List lIds, String sMode, int iYearNow, int iMonthNow, int iWeekNow, Long lNow, String sId, String sStatus, String sTarget) throws ParseException {

        if(sStatus.equals("Assigned") || sStatus.equals("Review")) {

            if(null != sTarget) { if(!sTarget.equals("")) { if(!sTarget.equals(" ")) {

                Boolean bAdd            = false;
                SimpleDateFormat sdf = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat());
                Calendar cTarget        = Calendar.getInstance();

                cTarget.setTime(sdf.parse(sTarget));

                int iYear   = cTarget.get(Calendar.YEAR);
                int iWeek   = cTarget.get(Calendar.WEEK_OF_YEAR);
                int iMonth  = cTarget.get(Calendar.MONTH);

                if(sMode.equals("week")) {
                    if(iYear == iYearNow) {
                        if(iWeek == iWeekNow) {
                            bAdd = true;
                        }
                    }
                } else if(sMode.equals("month")) {
                    if(iYear == iYearNow) {
                        if(iMonth == iMonthNow) {
                            bAdd = true;
                        }
                    }
                } else if(sMode.equals("overdue")) {
                    Long lTarget = cTarget.getTimeInMillis();
                    if(lTarget < lNow) { bAdd = true; }
                }

                if(bAdd) {
                    lIds.add(sId);
                }

            }}}
        }

    }


    // My Dashboard
    public JSONObject getUserDashboardData(Context context, String[] args) throws Exception {

        String[] sColors        = {"329cee","f6bd0f","8BBA00","ec0c41","752fc3","AFD8F8","fad46c","c9ff0d","F984A1","A66EDD"};
        String[] aResults       = new String[16];
        HashMap paramMap        = (HashMap) JPO.unpackArgs(args);
        String sOID             = (String)paramMap.get("objectId");
        String sLanguage        = (String)paramMap.get("languageStr");
        Integer[] iCounters     = new Integer[3];
        int iCountTimeline      = 0;
        int iCountMRU           = 0;

        JSONArray seriesPendingTaskArray = new JSONArray();
        JSONArray categoryLabelsArray = new JSONArray();      
        categoryLabelsArray.put(" ");
        JSONArray timeLine1Array = new JSONArray();
        JSONArray timeLine2Array = new JSONArray();
        JSONArray timeLine3Array = new JSONArray();
        
        String sLabelAssignedTasksPending = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.AssignedTasksPending", sLanguage);
    	String sLabelPast = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.Past", sLanguage);
    	String sLabelNow = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.Now", sLanguage);
    	String sLabelSoon = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.Soon", sLanguage);
    	
        String sLabelRoute = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Type.Route",  sLanguage);
        String sLabelTitle = EnoviaResourceBundle.getProperty(context, "Components", "emxComponents.Widget.Tasks",  sLanguage);
        String sLabelAction = EnoviaResourceBundle.getProperty(context, "Components", "emxComponents.TaskDetails.Action",  sLanguage);
        String sLabelTargetDate =EnoviaResourceBundle.getProperty(context, "Components", "emxComponents.Routes.ScheduleCompDate",  sLanguage);
    	
        JSONObject tooltipObject = new JSONObject();
        tooltipObject.put("labelRoute", sLabelRoute);
        tooltipObject.put("labelTitle", sLabelTitle);
        tooltipObject.put("labelAction", sLabelAction);        
        tooltipObject.put("labelTargetDate", sLabelTargetDate);

        for(int i = 0; i < 3; i++) { iCounters[i] = 0; }

        Calendar cNow   = Calendar.getInstance();
        int iWeekNow 	= cNow.get(Calendar.WEEK_OF_YEAR);
        int iMonthNow 	= cNow.get(Calendar.MONTH);
        int iYearNow 	= cNow.get(Calendar.YEAR);
        Calendar cFuture= Calendar.getInstance();
        cFuture.add(java.util.GregorianCalendar.DAY_OF_YEAR, 5);

        Calendar cMRU= Calendar.getInstance();
        cMRU.add(java.util.GregorianCalendar.DAY_OF_YEAR, -1);

        StringBuilder sbInfo1           = new StringBuilder();
        StringBuilder sbInfo2           = new StringBuilder();
        StringBuilder sbInfo3           = new StringBuilder();

        com.matrixone.apps.common.Person pUser = com.matrixone.apps.common.Person.getPerson( context );

        StringList busSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_NAME);
        busSelects.add(DomainConstants.SELECT_CURRENT);
        busSelects.add(DomainConstants.SELECT_MODIFIED);
        busSelects.add("attribute["+ DomainConstants.ATTRIBUTE_SCHEDULED_COMPLETION_DATE +"]");
        busSelects.add("attribute["+ DomainConstants.ATTRIBUTE_ROUTE_ACTION +"]");
        busSelects.add("attribute["+ DomainConstants.ATTRIBUTE_TITLE +"]");
        busSelects.add("from["+ DomainConstants.RELATIONSHIP_ROUTE_TASK+"].to.id");
        busSelects.add("from["+ DomainConstants.RELATIONSHIP_ROUTE_TASK+"].to.name");

        MapList mlInboxTasks = pUser.getRelatedObjects(context, "Project Task", "Inbox Task", busSelects, null, true, false, (short)1, "(current != 'Complete')", "", 0);

        mlInboxTasks.sort("attribute["+ DomainConstants.ATTRIBUTE_SCHEDULED_COMPLETION_DATE +"]", "descending", "date");

        for(int i = 0; i < mlInboxTasks.size(); i++) {

            Map mInboxTask  = (Map)mlInboxTasks.get(i);
            String sDate    = (String)mInboxTask.get("attribute["+ DomainConstants.ATTRIBUTE_SCHEDULED_COMPLETION_DATE +"]");
            String sTitle   = (String)mInboxTask.get("attribute["+ DomainConstants.ATTRIBUTE_TITLE +"]");
            String sAction  = (String)mInboxTask.get("attribute["+ DomainConstants.ATTRIBUTE_ROUTE_ACTION +"]");
            String sId      = (String)mInboxTask.get("from["+ DomainConstants.RELATIONSHIP_ROUTE_TASK+"].to.id");
            String sRoute   = (String)mInboxTask.get("from["+ DomainConstants.RELATIONSHIP_ROUTE_TASK+"].to.name");
            String sModified= (String)mInboxTask.get(DomainConstants.SELECT_MODIFIED);

            Calendar cModified    = Calendar.getInstance();
            cModified.setTime(sdf.parse(sModified));
            if(cModified.after(cMRU)) { iCountMRU++; }

            if(UIUtil.isNullOrEmpty(sTitle)){
            	sTitle = "(" + sAction + ")"; 
            }

            if(UIUtil.isNotNullAndNotEmpty(sDate)){
            	
                    iCountTimeline++;
                    Calendar cTarget    = Calendar.getInstance();
                    cTarget.setTime(sdf.parse(sDate));

                    int iDay 	= cTarget.get(Calendar.DAY_OF_MONTH);
                    int iWeek 	= cTarget.get(Calendar.WEEK_OF_YEAR);
                    int iMonth 	= cTarget.get(Calendar.MONTH);
                    int iYear 	= cTarget.get(Calendar.YEAR);

                    categoryLabelsArray.put(sTitle);

                    JSONObject routeDataObject = new JSONObject();
                    routeDataObject.put("id", sId);
                    routeDataObject.put("title", sTitle);
                    routeDataObject.put("route", sRoute);
                    routeDataObject.put("action", sAction);
                    routeDataObject.put("date", cTarget.getTimeInMillis());
                    routeDataObject.put("x", cTarget.getTimeInMillis());               
                    routeDataObject.put("y", iCountTimeline);
                    
                    if(cTarget.before(cNow)) {
                    	iCounters[0]++; 
                    	timeLine1Array.put(routeDataObject);
                    }else {
                        if(cTarget.before(cFuture)) {
                        	timeLine2Array.put(routeDataObject);
                        }else {
                        	timeLine3Array.put(routeDataObject);
                        }
                    }
                    if(iYear == iYearNow) {
                        if(iMonth == iMonthNow) { iCounters[1]++; }
                        if(iWeek == iWeekNow) { iCounters[2]++; }
                    }
            }

        }

        // Info Links
        String sInfoPrefix 	= " <a onclick='openURLInDetails(\"../common/emxIndentedTable.jsp?suiteKey=Framework&table=APPTaskSummary&freezePane=Name,NewWindow&editLink=true&selection=multiple&sortColumnName=DueDate&sortDirection=ascending&program=emxDashboardRoutes:";
        sbInfo1.append("<b>").append(iCounters[0]).append("</b>").append(sInfoPrefix).append("getRouteTasksAssignedPending&mode=Overdue&header=emxFramework.String.RouteTasksOverdue\")'>").append(EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.Overdue" , sLanguage)).append("</a>");
        sbInfo2.append("<b>").append(iCounters[1]).append("</b>").append(sInfoPrefix).append("getRouteTasksAssignedPending&mode=Month&header=emxFramework.String.RouteTasksDueThisMonth\")'>").append(EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.ThisMonth" , sLanguage)).append("</a>");
        sbInfo3.append("<b>").append(iCounters[2]).append("</b>").append(sInfoPrefix).append("getRouteTasksAssignedPending&mode=Week&header=emxFramework.String.RouteTasksDueThisWeek\")'>").append(EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.ThisWeek" , sLanguage)).append("</a>");


        // Dashboard Counters
        StringBuilder sbCounter = new StringBuilder();
        sbCounter.append("<td onclick='openURLInDetails(\"../common/emxIndentedTable.jsp?table=APPTaskSummary&program=emxDashboardRoutes:getRouteTasksAssignedPending&header=emxFramework.String.AssignedTasksPending&freezePane=Name,NewWindow&suiteKey=Framework&selection=multiple\")' ");
        sbCounter.append(" class='counterCell ");
        if(mlInboxTasks.size() == 0){ sbCounter.append("grayBright");   }
        else                        { sbCounter.append("purple");       }
        sbCounter.append("'><span class='counterText ");
        if(mlInboxTasks.size() == 0){ sbCounter.append("grayBright");   }
        else                        { sbCounter.append("purple");       }
        sbCounter.append("'>").append(mlInboxTasks.size()).append("</span><br/>");
        sbCounter.append(EnoviaResourceBundle.getProperty(context, "Components", "emxComponents.Common.AssignedTasks", sLanguage)).append("</td>");

        StringBuilder sbUpdates = new StringBuilder();
        sbUpdates.append("<td ");
        if(iCountMRU > 0) {
            sbUpdates.append(" onclick='openURLInDetails(\"../common/emxIndentedTable.jsp?table=APPTaskSummary&program=emxDashboardRoutes:getRouteTasksAssignedPending&mode=MRU&header=emxFramework.String.MRURouteTasks&freezePane=Name,NewWindow&suiteKey=Framework&selection=multiple\")' ");
            sbUpdates.append(" class='mruCell'><span style='color:#000000;font-weight:bold;'>").append(iCountMRU).append("</span> <span class='counterTextMRU'>");
            if(iCountMRU == 1) { sbUpdates.append(EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.MostRecentUpdate"  , sLanguage)); }
            else               { sbUpdates.append(EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.MostRecentUpdates" , sLanguage)); }
            sbUpdates.append("</span>");
        } else {
            sbUpdates.append(">");
        }
        sbUpdates.append("</td>");


        int iHeight = 40 + (iCountTimeline*17);
        if(iHeight < 160) { iHeight = 160; }

     	JSONObject taskOverdueDataObject = new JSONObject();
       	taskOverdueDataObject.put("name", sLabelPast);
       	taskOverdueDataObject.put("color", colorRed);  	 
       	taskOverdueDataObject.put("data", timeLine1Array);   	 
         
       	JSONObject taskNowDataObject = new JSONObject();
       	taskNowDataObject.put("name", sLabelNow);
       	taskNowDataObject.put("color", colorYellow);  	 
       	taskNowDataObject.put("data", timeLine2Array);
        	 
       	JSONObject taskSoonDataObject = new JSONObject();
       	taskSoonDataObject.put("name", sLabelSoon);
       	taskSoonDataObject.put("color", colorGreen);
       	taskSoonDataObject.put("data", timeLine3Array);
        	 
       	seriesPendingTaskArray.put(taskOverdueDataObject);
       	seriesPendingTaskArray.put(taskNowDataObject);
       	seriesPendingTaskArray.put(taskSoonDataObject);
        
        JSONObject taskPendingObjectLink = new JSONObject();
        taskPendingObjectLink.put("taskPendingThisWeek", sbInfo3.toString());
      	taskPendingObjectLink.put("taskPendingThisMonth", sbInfo2.toString());
      	taskPendingObjectLink.put("taskPendingOverDue", sbInfo1.toString());
        
      	String sHasInboxTaskAssigned	= pUser.getInfo(context, "to["+ DomainConstants.RELATIONSHIP_PROJECT_TASK+"]");
      	
        JSONObject routeWidget = new JSONObject();        
        routeWidget.put("label", sLabelAssignedTasksPending);
        routeWidget.put("series", seriesPendingTaskArray);
        routeWidget.put("name", "RouteTasks");
        routeWidget.put("type", "scatter");
        routeWidget.put("height", iHeight);
        routeWidget.put("yMax", iCountTimeline);
        routeWidget.put("yAxisCategories", categoryLabelsArray);        
        routeWidget.put("view", "expanded");
        routeWidget.put("bottomLineData", taskPendingObjectLink);
        routeWidget.put("xAxisDateValue", Calendar.getInstance().getTimeInMillis());
        routeWidget.put("tooltipObject", tooltipObject);
        routeWidget.put("filterable", true);
        routeWidget.put("showLegend", true);        
        routeWidget.put("filterURL", "../common/emxPortal.jsp?portal=APPRoutePowerView&suiteKey=Components" +
        		"&StringResourceFileId=emxComponentsStringResource&SuiteDirectory=components&emxSuiteDirectory=components");
        routeWidget.put("counterLink", sbCounter.toString());
        routeWidget.put("updateLink", sbUpdates.toString());
        routeWidget.put("hasInboxTaskAssigned",sHasInboxTaskAssigned);
        return routeWidget;
    }
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRouteTasksAssignedPending(Context context, String[] args) throws Exception {


        Map programMap          = (Map) JPO.unpackArgs(args);
        String sMode            = (String) programMap.get("mode");
        StringBuilder sbWhere   = new StringBuilder();

        if(null == sMode) { sMode = ""; }
        com.matrixone.apps.common.Person pUser = com.matrixone.apps.common.Person.getPerson( context );

        String attrScheduledCompletionDate = DomainConstants.ATTRIBUTE_SCHEDULED_COMPLETION_DATE;
        StringList busSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_TYPE);
		busSelects.add("attribute["+ DomainConstants.ATTRIBUTE_TITLE +"]");
        busSelects.add("attribute["+ DomainConstants.ATTRIBUTE_SCHEDULED_COMPLETION_DATE +"]");
        busSelects.add("from["+DomainConstants.RELATIONSHIP_ROUTE_TASK+"].to.id");
        busSelects.add("from["+DomainConstants.RELATIONSHIP_ROUTE_TASK+"].to.name");        
		busSelects.add(DomainConstants.SELECT_CURRENT);
        busSelects.add("attribute["+DomainConstants.ATTRIBUTE_ROUTE_ACTION +"]");		
        
        sbWhere.append("(current != 'Complete')");

        if(sMode.equals("MRU")) {

            Calendar cal = Calendar.getInstance();
            cal.add(java.util.GregorianCalendar.DAY_OF_YEAR, -1);

            String sMinute = String.valueOf(cal.get(Calendar.MINUTE));
            String sSecond = String.valueOf(cal.get(Calendar.SECOND));
            String sAMPM = (cal.get(Calendar.AM_PM) == 0 ) ? "AM" : "PM";

            if(sSecond.length() == 1) { sSecond = "0" + sSecond; }
            if(sMinute.length() == 1) { sMinute = "0" + sMinute; }


            sbWhere.append(" && (modified >= \"");
            sbWhere.append(cal.get(Calendar.MONTH) + 1).append("/").append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(cal.get(Calendar.YEAR));
            sbWhere.append(" ").append(cal.get(Calendar.HOUR) + 1).append(":").append(sMinute).append(":").append(sSecond).append(" ").append(sAMPM);
            sbWhere.append("\")");

        } else if(sMode.equals("Week")) {

            Calendar cStart = Calendar.getInstance();
            Calendar cEnd = Calendar.getInstance();

            cStart.set(Calendar.DAY_OF_WEEK, cStart.getFirstDayOfWeek());
            cEnd.set(Calendar.DAY_OF_WEEK, cEnd.getFirstDayOfWeek());
            cEnd.add(java.util.GregorianCalendar.DAY_OF_YEAR, +7);

            sbWhere.append(" && ");
            sbWhere.append("(attribute["+ attrScheduledCompletionDate +"] >= '").append(cStart.get(Calendar.MONTH) + 1).append("/").append(cStart.get(Calendar.DAY_OF_MONTH)).append("/").append(cStart.get(Calendar.YEAR)).append("')");
            sbWhere.append(" && ");
            sbWhere.append("(attribute["+ attrScheduledCompletionDate +"] < '").append(cEnd.get(Calendar.MONTH) + 1).append("/").append(cEnd.get(Calendar.DAY_OF_MONTH)).append("/").append(cEnd.get(Calendar.YEAR)).append("')");

        } else if(sMode.equals("Month")) {

            Calendar cStart = Calendar.getInstance();
            Calendar cEnd = Calendar.getInstance();

            cEnd.add(java.util.GregorianCalendar.MONTH, +1);

            sbWhere.append(" && ");
            sbWhere.append("(attribute["+ attrScheduledCompletionDate +"] >= '").append(cStart.get(Calendar.MONTH) + 1).append("/1/").append(cStart.get(Calendar.YEAR)).append("')");
            sbWhere.append(" && ");
            sbWhere.append("(attribute["+ attrScheduledCompletionDate +"] < '").append(cEnd.get(Calendar.MONTH) + 1).append("/1/").append(cEnd.get(Calendar.YEAR)).append("')");

        } else if(sMode.equals("Overdue")) {

            Calendar cNow = Calendar.getInstance();

            sbWhere.append(" && ");
            sbWhere.append("(attribute["+ attrScheduledCompletionDate +"] <= '").append(cNow.get(Calendar.MONTH) + 1).append("/").append(cNow.get(Calendar.DAY_OF_MONTH)).append("/").append(cNow.get(Calendar.YEAR)).append("')");

        }

        return pUser.getRelatedObjects(context, DomainConstants.RELATIONSHIP_PROJECT_TASK , DomainConstants.TYPE_INBOX_TASK, busSelects, null, true, false, (short)1, sbWhere.toString(), "", 0);

    }
}
