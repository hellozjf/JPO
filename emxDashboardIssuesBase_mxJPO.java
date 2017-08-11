
import com.matrixone.apps.common.Issue;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.json.JSONArray;
import com.matrixone.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class emxDashboardIssuesBase_mxJPO {
    
    public static String sColorState1 = "#6699bb";
    public static String sColorState2 = "#cc0000";
    public static String sColorState3 = "#ff7f00";
    public static String sColorState4 = "#009c00";
    public static String sColorState5 = "#AAB8BE";
    public static String sColorLink     = emxUtil_mxJPO.sColorLink;
    
    SimpleDateFormat sdf = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat());
    String[] sColors     = emxUtil_mxJPO.sColorsCharts;
    
    public emxDashboardIssuesBase_mxJPO(Context context, String[] args) throws Exception {}
    // My Dashboard
    public JSONObject getUserDashboardData(Context context, String[] args) throws Exception {
        
        HashMap paramMap        = (HashMap) JPO.unpackArgs(args);
        //String sOID             = (String)paramMap.get("objectId");
        String sLanguage        = (String)paramMap.get("languageStr"); 
        int iCountDate          = 0;
        int iCountMRU           = 0;
        Calendar cMRU           = Calendar.getInstance();
        long sNow = Calendar.getInstance().getTimeInMillis();
        cMRU.add(java.util.GregorianCalendar.DAY_OF_YEAR,-1);

        com.matrixone.apps.common.Person pUser = com.matrixone.apps.common.Person.getPerson( context );
        
        StringList busSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);        
        busSelects.add(DomainConstants.SELECT_NAME);
        busSelects.add(DomainConstants.SELECT_OWNER);
        busSelects.add(DomainConstants.SELECT_CURRENT);
        busSelects.add(DomainConstants.SELECT_ORIGINATED);  
        busSelects.add(DomainConstants.SELECT_MODIFIED);        
        busSelects.add(DomainConstants.SELECT_DESCRIPTION);
        busSelects.add("attribute["+ DomainConstants.ATTRIBUTE_ESTIMATED_START_DATE +"]");
        busSelects.add("attribute["+ DomainConstants.ATTRIBUTE_ESTIMATED_END_DATE +"]");
        
        String strType = PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_type_Issue);
        String strRelationAssignedIssue = PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_relationship_AssignedIssue);  
        
        MapList mlIssues = pUser.getRelatedObjects(context, strRelationAssignedIssue, strType, busSelects, null, false, true, (short)1, "current != 'Closed'", "", 0);

        mlIssues.sort("attribute["+ DomainConstants.ATTRIBUTE_ESTIMATED_END_DATE +"]", "ascending", "date");       
        
        JSONArray yAxisCategoreis = new JSONArray();
        JSONArray issueDetailsOjbectArray = new JSONArray();
        for(int i = 0; i < mlIssues.size(); i++) {
            Map mIssue = (Map)mlIssues.get(i);
            String sId          = (String)mIssue.get(DomainConstants.SELECT_ID);
            String sName        = (String)mIssue.get(DomainConstants.SELECT_NAME);
            String sCurrent     = (String)mIssue.get(DomainConstants.SELECT_CURRENT);
            String sModified    = (String)mIssue.get(DomainConstants.SELECT_MODIFIED);
            String sOwner       = (String)mIssue.get(DomainConstants.SELECT_OWNER);
            String sDescription = (String)mIssue.get(DomainConstants.SELECT_DESCRIPTION);
            String sStart       = (String)mIssue.get("attribute["+ DomainConstants.ATTRIBUTE_ESTIMATED_START_DATE +"]");
            String sEnd         = (String)mIssue.get("attribute["+ DomainConstants.ATTRIBUTE_ESTIMATED_END_DATE +"]");
            String sColor       = sColorState1;
            
            Calendar cModified    = Calendar.getInstance();            
            cModified.setTime(sdf.parse(sModified));  
            if(cModified.after(cMRU)) { iCountMRU++; }            
            

            if(UIUtil.isNotNullAndNotEmpty(sStart) && UIUtil.isNotNullAndNotEmpty(sEnd)) {
                Calendar sStartDate = Calendar.getInstance();            
                Calendar sEndDate = Calendar.getInstance();
            	sStartDate.setTime(sdf.parse(sStart));
            	sEndDate.setTime(sdf.parse(sEnd));
                            
                if(sCurrent.equals("Assign")) {
                	sColor = sColorState2; 
                }else if(sCurrent.equals("Active")) {
                	sColor = sColorState3; 
                }else if(sCurrent.equals("Review")) {
                	sColor = sColorState4; 
                }                    
                iCountDate++;            
                yAxisCategoreis.put(sName);                    
                JSONObject issueDetailsOjbect = new JSONObject();
                issueDetailsOjbect.put("low", sStartDate.getTimeInMillis());
                issueDetailsOjbect.put("high", sEndDate.getTimeInMillis());
                issueDetailsOjbect.put("id", sId);
                issueDetailsOjbect.put("desc", sDescription);
                issueDetailsOjbect.put("owner", sOwner);
                issueDetailsOjbect.put("color", sColor);
                issueDetailsOjbectArray.put(issueDetailsOjbect);
                }
            }
            
        StringBuilder sbCounter = new StringBuilder();        
        sbCounter.append("<td onclick='openURLInDetails(\"../common/emxIndentedTable.jsp?table=IssueListDetails&program=emxDashboardIssues:getIssuesAssignedPending&header=emxFramework.String.AssignedPendingIssues&freezePane=Name,Edit&suiteKey=Framework&selection=multiple\")'");
        sbCounter.append(" class='counterCell ");
        if(mlIssues.size() == 0){ sbCounter.append("grayBright");   }
        else                    { sbCounter.append("yellow");       }
        sbCounter.append("'><span class='counterText ");
        if(mlIssues.size() == 0){ sbCounter.append("grayBright");   }
        else                    { sbCounter.append("yellow");       }        
        sbCounter.append("'>").append(mlIssues.size()).append("</span><br/>");
        sbCounter.append(EnoviaResourceBundle.getProperty(context, "Components", "emxComponents.MyDesk.Issues", sLanguage)).append("</td>");         
        
        StringBuilder sbUpdates = new StringBuilder();
        sbUpdates.append("<td ");
        if(iCountMRU > 0) {           
            sbUpdates.append(" onclick='openURLInDetails(\"../common/emxIndentedTable.jsp?table=IssueListDetails&program=emxDashboardIssues:getIssuesAssignedPending&mode=MRU&header=emxFramework.String.MRUIssues&freezePane=Name,Edit&suiteKey=Framework&selection=multiple\")' ");
            sbUpdates.append(" class='mruCell'><span style='color:#000000;font-weight:bold;'>").append(iCountMRU).append("</span> <span class='counterTextMRU'>");            
            if(iCountMRU == 1) { sbUpdates.append(EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.MostRecentUpdate"  , sLanguage)); }
            else               { sbUpdates.append(EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.MostRecentUpdates" , sLanguage)); }
            sbUpdates.append("</span>");          
            
        } else {
            sbUpdates.append(">");    
        }
        sbUpdates.append("</td>");        
        
        int iHeightDate	= 35 + (iCountDate * 20);              
        if(iHeightDate < 120) { iHeightDate = 120; }
        
        String labelPlannedIssues = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.AssignedPendingIssues", sLanguage);
        
        JSONObject issueItemObj = new JSONObject();       
        issueItemObj.put("name", labelPlannedIssues);
        issueItemObj.put("data", issueDetailsOjbectArray);
 	   	JSONArray seriesIssuesFinal = new JSONArray();
 	   	seriesIssuesFinal.put(issueItemObj);
        
 	   	String sHasIssuesAssigned = pUser.getInfo(context, "from["+ PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_relationship_AssignedIssue)+ "]");
 	   
        JSONObject issueWidget = new JSONObject();
        issueWidget.put("label", labelPlannedIssues);
        issueWidget.put("series", seriesIssuesFinal);        
        issueWidget.put("name", "PlannedIssues" );
        issueWidget.put("type", "columnrange");
        issueWidget.put("height", iHeightDate);
        issueWidget.put("view", "expanded");
        issueWidget.put("filterable", true);
        issueWidget.put("zoomType", "xy");
        issueWidget.put("sNow", sNow);
        issueWidget.put("filterURL", "../common/emxForm.jsp?hideExtendedHeader=true&form=type_Issue&toolbar=IssuePropertiesToolBar&editLink=true&suiteKey=Components&StringResourceFileId=emxComponentsStringResource&SuiteDirectory=components&emxSuiteDirectory=components");       
        issueWidget.put("xAxisCategories", yAxisCategoreis);
        issueWidget.put("counterLink", sbCounter.toString());
        issueWidget.put("updateLink", sbUpdates.toString());
        issueWidget.put("hasIssuesAssigned",sHasIssuesAssigned);
        return issueWidget;    
    }
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getIssuesAssignedPending(Context context, String[] args) throws Exception {
		
                        
        Map programMap          = (Map) JPO.unpackArgs(args);
        String sMode            = (String) programMap.get("mode");  
        StringBuilder sbWhere   = new StringBuilder();

        if(null == sMode) { sMode = ""; }
        com.matrixone.apps.common.Person pUser = com.matrixone.apps.common.Person.getPerson( context );
        
        StringList busSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_MODIFIED);         
            
        sbWhere.append("(current != 'Closed')");
        
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
            
        }
        String strType = PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_type_Issue);
        String strRelationAssignedIssue = PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_relationship_AssignedIssue); 
        
        return pUser.getRelatedObjects(context, strRelationAssignedIssue, strType, busSelects, null, false, true, (short)1, sbWhere.toString(), "", 0);
		
    }

}
