
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.dassault_systemes.requirements.ReqSchemaUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.requirements.RequirementsUtil;
import com.matrixone.json.JSONArray;
import com.matrixone.json.JSONObject;

public class emxRMTDashBoardBase_mxJPO{  
	public emxRMTDashBoardBase_mxJPO (Context context, String[] args) throws Exception{
	}
    
    // Requirements Dashboard
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList retrieveRequirementsDashboardItems(Context context, String[] args, StringList busSelects, String sRelationships, String sTypes, String sFilter, Map mFilter) throws Exception {    
        HashMap paramMap        = (HashMap) JPO.unpackArgs(args);
        String sOID             = (String) paramMap.get("objectId"); 
        MapList mlResults       = new MapList();
        if(!sFilter.equals("")) { sFilter = " && " + sFilter; }
        sFilter = "(revision == last) && (current != 'Obsolete')" + sFilter;
        if(sOID.equals("")) {
            mlResults = DomainObject.findObjects(context, "Requirement", "*", "(owner == '" + context.getUser() + "') && " + sFilter, busSelects);            
            com.matrixone.apps.common.Person pUser = com.matrixone.apps.common.Person.getPerson( context );
            MapList mlRequirementsAssigned = pUser.getRelatedObjects(context, "Assigned Requirement", "Requirement", busSelects, null, false, true, (short)1, "(owner != '" + context.getUser() + "') && " + sFilter, "", 0);            
            mlResults.addAll(mlRequirementsAssigned);
        } else {
            Pattern pTypes = new Pattern("Requirement");
            busSelects.add("type");
            DomainObject dObject = new DomainObject(sOID);
            mlResults = dObject.getRelatedObjects(context, sRelationships, sTypes, busSelects, null, false, true, (short)0, "", "", 0, pTypes, null, mFilter);                
        }
        return mlResults;
    }
	
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRequirementsDashboardItems(Context context, String[] args) throws Exception {  
        HashMap paramMap                = (HashMap)JPO.unpackArgs(args);
        String sOID                     = (String) paramMap.get("objectId"); 
        String sTypes                   = (String) paramMap.get("types");   
        String sRelationships           = (String) paramMap.get("relationships");       
        String filterEC                 = (String) paramMap.get("filterEC"); 
        String sFilterResponsibility    = (String) paramMap.get("filterResponsibility");  
        if(sTypes.equals(""))           { sTypes = "*"; }
        if(sRelationships.equals(""))   { sRelationships = "Design Responsibility,Sub Requirement,Specification Structure"; }   
        if(null == filterEC) { filterEC = ""; }
        StringBuilder sbWhere   = new StringBuilder();
        Map mFilter             = new HashMap();
        StringList busSelects   = new StringList();
        busSelects.add("id");                  
        if(!filterEC.equals("")) {
            busSelects.add("to[EC Affected Item]");
            busSelects.add("to[EC Affected Item].from.current");
        }
        if(!sFilterResponsibility.equals("undefined")) {
            if(!sFilterResponsibility.equals("-1")) {
                if(sFilterResponsibility.equals("-")) {
                    busSelects.add("to[Design Responsibility]");
                    sbWhere.append("(to[Design Responsibility] == False) && ");
                    mFilter.put("to[Design Responsibility]", "FALSE");
                } else {                
                    busSelects.add("to[Design Responsibility]");
                    busSelects.add("to[Design Responsibility].from.name");
                    sbWhere.append("(to[Design Responsibility] == TRUE && to[Design Responsibility].from.name == '").append(sFilterResponsibility).append("') && ");
                    mFilter.put("to[Design Responsibility]", "TRUE");
                    mFilter.put("to[Design Responsibility].from.name", sFilterResponsibility);
                }
            }     
        }           
        if (sbWhere.length() > 4 ) { sbWhere.setLength(sbWhere.length() - 4); }        
        MapList mlResults = retrieveRequirementsDashboardItems(context, args, busSelects, sRelationships, sTypes, sbWhere.toString(), mFilter);        
        if(!filterEC.equals("")) { 
            String sStatesECComplete = "Complete,Close,Reject";
            for(int i = mlResults.size() - 1; i >= 0; i--) {
                Map mResult             = (Map)mlResults.get(i);
                String sHasEC           = (String)mResult.get("to[EC Affected Item]");
                Boolean bRemove         = false;                
                Boolean bHasPendingEC   = false;
                if(sHasEC.equalsIgnoreCase("TRUE")) {
                    if (mResult.get("to[EC Affected Item].from.current") instanceof StringList) {
                        StringList slData = (StringList)mResult.get("to[EC Affected Item].from.current");
                       for(int j = 0; j < slData.size(); j++) {
                           String sStatus = (String)slData.get(j);
                           if(!sStatesECComplete.contains(sStatus)) {
                               bHasPendingEC = true;
                               break;
                           }
                       }                  
                    } else {
                        String sStatus = (String)mResult.get("to[EC Affected Item].from.current");
                        if(!sStatesECComplete.contains(sStatus)) {
                            bHasPendingEC = true;
                            break;
                        }                  
                    }
                }
                if(filterEC.equals("0")) { // No pending EC
                    if(bHasPendingEC) { bRemove = true; }
                } else if (filterEC.equals("1")) { // WITH pending EC
                    if(!bHasPendingEC) { bRemove = true; }
                }  
                if(bRemove) { mlResults.remove(i); }
                
            }           
        }
        if(!sOID.equals("")) {
            for(int i = mlResults.size() - 1; i >= 0; i--) {
                Map mResult = (Map)mlResults.get(i);
                mResult.put("level", "1");
            }            
        } 
        return mlResults;		
    }
    
	private JSONArray getTestCaseInformation(Context context, String objectId) throws FrameworkException{
		String sTypes           = "*";   
        String sRelationships   = ReqSchemaUtil.getSubRequirementRelationship(context) + ","
        						+ ReqSchemaUtil.getSpecStructureRelationship(context) + ","
        						+ ReqSchemaUtil.getRequirementValidationRelationship(context);  
        StringList busSelects = new StringList();
        StringList relSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_TYPE);  
        busSelects.add(DomainConstants.SELECT_CURRENT);
        busSelects.add("from[Requirement Validation].to.current");        
        busSelects.add("from[Requirement Validation].to.attribute[Validation Status]");
        busSelects.add("from[Requirement Validation]");
        busSelects.add("attribute[Validation Status]");
        JSONArray response = new JSONArray();
        relSelects.add("from.id[connection]");
        DomainObject dObject = DomainObject.newInstance(context, objectId);
        MapList mlRequirements = dObject.getRelatedObjects(context, sRelationships, sTypes, busSelects, relSelects, false, true, (short)0, "", "", 0, null, null, null);
        for(int i=0; i<mlRequirements.size();i++){
        	Map object = (Map)mlRequirements.get(i);
        	String type = (String)object.get(DomainConstants.SELECT_TYPE);
        	if(type.equalsIgnoreCase(ReqSchemaUtil.getTestCaseType(context))){
        		response.put(new JSONObject(object));
        	}
        }
		return response;
	}
	
	private JSONArray getECInformation(Context context, String objectId) throws FrameworkException{
		String sTypes           = "*";   
        String sRelationships   = ReqSchemaUtil.getSubRequirementRelationship(context) + ","
        						+ ReqSchemaUtil.getSpecStructureRelationship(context);
        StringList busSelects = new StringList();
        StringList relSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_TYPE);  
        busSelects.add(DomainConstants.SELECT_CURRENT);
        busSelects.add("to[EC Affected Item]");
        busSelects.add("to[EC Affected Item].from.current");
        JSONArray response = new JSONArray();
        relSelects.add("from.id[connection]");
        DomainObject dObject = DomainObject.newInstance(context, objectId);
        MapList mlRequirements = dObject.getRelatedObjects(context, sRelationships, sTypes, busSelects, relSelects, false, true, (short)0, "", "", 0, null, null, null);
        for(int i=0; i<mlRequirements.size();i++){
        	Map object = (Map)mlRequirements.get(i);
        	response.put(new JSONObject(object));
        }
		return response;
	}
	
	private JSONArray getDesignResponsibilityInformation(Context context, String objectId) throws FrameworkException{
		String sTypes           = "*";   
        String sRelationships   = RequirementsUtil.getDesignResponsibilityRelationship(context)+"," 
        						+ ReqSchemaUtil.getSubRequirementRelationship(context) + ","
        						+ ReqSchemaUtil.getSpecStructureRelationship(context);
        StringList busSelects = new StringList();
        StringList relSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_TYPE);  
        busSelects.add(DomainConstants.SELECT_CURRENT);
        busSelects.add("to[Design Responsibility].from.name");
        busSelects.add("to[Design Responsibility]");
        JSONArray response = new JSONArray();
        relSelects.add("from.id[connection]");
        DomainObject dObject = DomainObject.newInstance(context, objectId);
        MapList mlRequirements = dObject.getRelatedObjects(context, sRelationships, sTypes, busSelects, relSelects, false, true, (short)0, "", "", 0, null, null, null);
        for(int i=0; i<mlRequirements.size();i++){
        	Map object = (Map)mlRequirements.get(i);
        	response.put(new JSONObject(object));
        }
		return response;
	}
	
	private JSONArray getTimeLineInformation(Context context, String objectId) throws ParseException, MatrixException{
		StringBuilder sbCategoriesTimeline          = new StringBuilder();
        java.util.List<String> lTimeline            = new ArrayList<String>();
        java.util.List<Integer> lWeeksMonths        = new ArrayList<Integer>();
        java.util.List<Integer> lYears              = new ArrayList<Integer>();  
        StringBuilder sbDataTimeline0               = new StringBuilder();
        StringBuilder sbDataTimeline1               = new StringBuilder();
        StringBuilder sbDataTimeline2               = new StringBuilder();
        StringBuilder sbDataTimeline3               = new StringBuilder();
        StringBuilder sbDataTimeline4               = new StringBuilder();
		String sTypes           = "*"; 
        String sRelationships   = ReqSchemaUtil.getSubRequirementRelationship(context) + ","
        						+ ReqSchemaUtil.getSpecStructureRelationship(context);
        StringList busSelects = new StringList();
        StringList relSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_TYPE);  
        busSelects.add(DomainConstants.SELECT_CURRENT);
        busSelects.add("state[Private].start");
        busSelects.add("state[InWork].start");
        busSelects.add("state[Validate].start");
        busSelects.add("state[Frozen].start");
        busSelects.add("state[Release].start");
        busSelects.add("originated");
        MapList   outList = new MapList();
        JSONArray response = new JSONArray();
        relSelects.add("from.id[connection]");
        DomainObject dObject = DomainObject.newInstance(context, objectId);
        MapList mlRequirements = dObject.getRelatedObjects(context, sRelationships, sTypes, busSelects, relSelects, false, true, (short)0, "", "", 0, null, null, null);
        if(mlRequirements.size() > 0) {
            mlRequirements.sort("originated", "ascending", "date");
            Map mFirst              = (Map)mlRequirements.get(0);  
            Boolean bUseMonths      = false;
            String sDateStart       = (String)mFirst.get("originated");
            SimpleDateFormat sdf    = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aaa");

            Calendar cNow           = Calendar.getInstance();
            Calendar cStart         = Calendar.getInstance();
            Calendar cDate          = Calendar.getInstance(); 

            cDate.setTime(sdf.parse(sDateStart));
            cStart.setTime(sdf.parse(sDateStart));     
            do  {
                int iWeek = cDate.get(Calendar.WEEK_OF_YEAR);
                int iYear = cDate.get(Calendar.YEAR);
                lWeeksMonths.add(iWeek);
                lYears.add(iYear);//          
                String sTemp = iWeek + "/" + iYear;
                lTimeline.add(sTemp);
                cDate.add(Calendar.DATE, 7);
            } while (cDate.before(cNow));                
            if(lTimeline.size() > 18) {
                bUseMonths      = true;
                lTimeline       = new ArrayList<String>();
                lWeeksMonths    = new ArrayList<Integer>();
                lYears          = new ArrayList<Integer>();
                cDate.setTime(sdf.parse(sDateStart));            
                do  {           
                    int iMonth = cDate.get(Calendar.MONTH) + 1;
                    int iYear = cDate.get(Calendar.YEAR);         
                    String sTemp = iMonth + "/" + iYear;
                    lTimeline.add(sTemp);
                    lWeeksMonths.add(iMonth);
                    lYears.add(iYear);
                    cDate.add(Calendar.MONTH, 1);
                } while (cDate.before(cNow));            
            }  
            for(int i=0; i<mlRequirements.size();i++){
            	Map mlRequirement = (Map)mlRequirements.get(i);
            	String sStart0              = (String) mlRequirement.get("state[Private].start");
                String sStart1              = (String)mlRequirement.get("state[InWork].start");
                String sStart2              = (String) mlRequirement.get("state[Validate].start");
                String sStart3              = (String) mlRequirement.get("state[Frozen].start");
                String sStart4              = (String) mlRequirement.get("state[Release].start");  
            	String sTimeline0 = getWeekOfTimeline(lTimeline, lYears, lWeeksMonths, sStart0, sdf, bUseMonths);
                String sTimeline1 = getWeekOfTimeline(lTimeline, lYears, lWeeksMonths, sStart1, sdf, bUseMonths);
                String sTimeline2 = getWeekOfTimeline(lTimeline, lYears, lWeeksMonths, sStart2, sdf, bUseMonths);
                String sTimeline3 = getWeekOfTimeline(lTimeline, lYears, lWeeksMonths, sStart3, sdf, bUseMonths);
                String sTimeline4 = getWeekOfTimeline(lTimeline, lYears, lWeeksMonths, sStart4, sdf, bUseMonths);

                sbDataTimeline0.append(sTimeline0).append(",");
                sbDataTimeline1.append(sTimeline1).append(",");
                sbDataTimeline2.append(sTimeline2).append(",");
                sbDataTimeline3.append(sTimeline3).append(",");
                sbDataTimeline4.append(sTimeline4).append(",");
            }
        }
        for(int i = 0; i < lTimeline.size(); i++) {           
            sbCategoriesTimeline.append("'").append(lTimeline.get(i)).append("',");
        }
        if (sbCategoriesTimeline.length() > 0 ) { 
        	sbCategoriesTimeline.setLength(sbCategoriesTimeline.length() - 1); 
        }
        Map data = new HashMap<String,String>();
        data.put("CategoriesTimeline", sbCategoriesTimeline.toString());
        data.put("Timeline0", sbDataTimeline0.toString());
        data.put("Timeline1", sbDataTimeline1.toString());
        data.put("Timeline2", sbDataTimeline2.toString());
        data.put("Timeline3", sbDataTimeline3.toString());
        data.put("Timeline4", sbDataTimeline4.toString());
        response.put(new JSONObject(data));
		return response;
	}
	
	private JSONArray getDerivedRequirementsStatus(Context context, String objectIdList,String direction) throws NumberFormatException, MatrixException{
		int[] result = new int[2]; 
		String has="";
		String hasNot="";
		
		result[0] = 0;
		result[1] = 0;
		String sTypes           = "*";   
        String sRelationships   = ReqSchemaUtil.getDerivedRequirementRelationship(context);  
        StringList busSelects = new StringList();
        StringList relSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_TYPE);  
        busSelects.add(DomainConstants.SELECT_CURRENT);
        //JSONArray response = new JSONArray();
        String[] idList = objectIdList.split(",");
        boolean toDirection;
        boolean fromDirection;
        if(direction.equalsIgnoreCase("to")){
        	has = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.dashboard.refinedRequirement"); 
        	hasNot = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.dashboard.notrefinedRequirement"); 
        	toDirection = true;
            fromDirection = false;
        }else{
        	//covered Requirements
        	has = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.dashboard.coveredRequirement");
        	hasNot = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.dashboard.notCoveredRequirement"); 
        	toDirection = false;
            fromDirection = true;
        }
        JSONObject jsonObject = new JSONObject();
        //JSONArray reqWithDerived = new JSONArray();
        JSONArray returnedValues = new JSONArray();
        JSONObject objectInfo = new JSONObject();
        for(int i=0;i<idList.length;i++){
        	String objectId = idList[i];
        	DomainObject dObject = DomainObject.newInstance(context, objectId);
            MapList mlRequirements = dObject.getRelatedObjects(context, sRelationships, sTypes, busSelects, relSelects, fromDirection, toDirection, (short)0, "", "", 0, null, null, null);
            if(mlRequirements.size()>0){
            	result[0]+=1;
            	//reqWithDerived.put(objectId);
            	objectInfo.put(objectId,has);
            }else{
            	result[1]+=1;
            	objectInfo.put(objectId,hasNot);
            }
            
            /*for(int j=0; j<mlRequirements.size();j++){
            	Map object = (Map)mlRequirements.get(j);
            	String state = (String)object.get(DomainConstants.SELECT_CURRENT);
            	if(jsonObject.contains(state)){
            		int value = (Integer) jsonObject.get(state);
            		value++;
            		jsonObject.put(state, value);
            	}else{
            		jsonObject.put(state, 1);
            	}
            }*/
        }
        //response.put(jsonObject);
        jsonObject.put("0", result[0]);
        jsonObject.put("1", result[1]);
        returnedValues.put(jsonObject);
        //returnedValues.put(reqWithDerived);
        returnedValues.put(objectInfo);
		return returnedValues;
	}
	
	
	private JSONObject getSubRequirements(Context context, String objectIdList) throws NumberFormatException, MatrixException{
		int[] result = new int[2]; 
		String habSub = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.dashboard.hasSubReq");
		String habNoSub = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.dashboard.hasNoSubReq"	);

		String sTypes           = ReqSchemaUtil.getRequirementType(context);   
		String sRelationships   = ReqSchemaUtil.getSubRequirementRelationship(context);  
        StringList busSelects = new StringList();
        StringList relSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_TYPE);  
        busSelects.add(DomainConstants.SELECT_CURRENT);
        String[] idList = objectIdList.split(",");
        JSONObject jsonObject = new JSONObject();
        for(int i=0;i<idList.length;i++){
        	String objectId = idList[i];
        	DomainObject dObject = DomainObject.newInstance(context, objectId);
            MapList mlRequirements = dObject.getRelatedObjects(context, sRelationships, sTypes, busSelects, relSelects, false, true, (short)1, "", "", 0, null, null, null);
            if(mlRequirements.size()>0){
            	jsonObject.put(objectId, habSub);
            }else{
            	jsonObject.put(objectId, habNoSub);
            }
        }
		return jsonObject;
	}
	
	private JSONArray getTestCaseValues(Context context, String objectList) throws MatrixException{
		int[] result = new int[2]; 
		String withTestCases = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.dashboard.hasTestCases"); 
		String noTestCases = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.dashboard.hasNoTestCases"); 
		String sTypes           = "*";   
        String sRelationships   = ReqSchemaUtil.getRequirementValidationRelationship(context);  
        StringList busSelects = new StringList();
        StringList relSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_TYPE);  
        busSelects.add(DomainConstants.SELECT_CURRENT);
        busSelects.add("from[Requirement Validation].to.current");        
        busSelects.add("from[Requirement Validation].to.attribute[Validation Status]");
        busSelects.add("from[Requirement Validation]");
        busSelects.add("attribute[Validation Status]");
        JSONArray response = new JSONArray();
        relSelects.add("from.id[connection]");
        
        String[] idList = objectList.split(",");
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonReqWithTestCases = new JSONObject();
        JSONArray returnedValues = new JSONArray();
        for(int i=0;i<idList.length;i++){
        	String objectId = idList[i];
        	DomainObject dObject = DomainObject.newInstance(context, objectId);
            MapList mlRequirements = dObject.getRelatedObjects(context, sRelationships, sTypes, busSelects, relSelects, false, true, (short)1, "", "", 0, null, null, null);
            if(mlRequirements.size()>0){
            	result[0]+=1;
            	jsonReqWithTestCases.put(objectId,withTestCases);
            }else{
            	result[1]+=1;
            	jsonReqWithTestCases.put(objectId,noTestCases);
            }
        }
        jsonObject.put("0", result[0]);
        jsonObject.put("1", result[1]);
        returnedValues.put(jsonObject);
        returnedValues.put(jsonReqWithTestCases);
        
		return returnedValues;
        //return jsonReqWithTestCases;
	}
	
	
	
	private JSONArray getPLMParameterValues(Context context, String objectList) throws MatrixException{
		int[] result = new int[2]; 
		String withParams = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.dashboard.hasParameters");  
		String noParams = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.dashboard.hasNoParameters");
		String sTypes           = "*";   
        String sRelationships   = ReqSchemaUtil.getParameterUsageRelationship(context)+',';
        sRelationships   = ReqSchemaUtil.getParameterAggregationRelationship(context);
        StringList busSelects = new StringList();
        StringList relSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        //busSelects.add(DomainConstants.SELECT_TYPE);  
        //busSelects.add(DomainConstants.SELECT_CURRENT);
        relSelects.add("from.id[connection]");
        
        String[] idList = objectList.split(",");
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonReqWithParam = new JSONObject();
        JSONArray jsonReturnedValues = new JSONArray();
        for(int i=0;i<idList.length;i++){
        	String objectId = idList[i];
        	DomainObject dObject = DomainObject.newInstance(context, objectId);
            MapList mlRequirements = dObject.getRelatedObjects(context, sRelationships, sTypes, busSelects, relSelects, false, true, (short)1, "", "", 0, null, null, null);
            if(mlRequirements.size()>0){
            	result[0]+=1;
            	jsonReqWithParam.put(objectId,withParams);
            }else{
            	result[1]+=1;
            	jsonReqWithParam.put(objectId,noParams);
            }
        }
        jsonObject.put("0", result[0]);
        jsonObject.put("1", result[1]);
        jsonReturnedValues.put(jsonObject);
        jsonReturnedValues.put(jsonReqWithParam);
        
		//return jsonObject;
        return jsonReturnedValues;
	}
	
	
	@com.matrixone.apps.framework.ui.ProgramCallable
	public JSONArray getRequirementsDashboardData(Context context, String[] args) throws Exception {
		JSONArray response = new JSONArray();
		HashMap paramMap        = (HashMap) JPO.unpackArgs(args);
        String dashboardRequest = (String) paramMap.get("requestType");
        String sObjectId = (String) paramMap.get("objectId");
        JSONObject filter = new JSONObject();
        filter.put("filter", dashboardRequest);
        response.put(filter);
        if(dashboardRequest.equalsIgnoreCase("divChartTestCase")){
        	//response.put( getTestCaseValues(context, sObjectId));
        	JSONArray result = getTestCaseValues(context, sObjectId) ;
        	response.put(result.get(0));
        	response.put(result.get(1));
        }else if(dashboardRequest.equalsIgnoreCase("divChartValidation")){
        	response.put(getTestCaseInformation(context, sObjectId));
        }else if(dashboardRequest.equalsIgnoreCase("divChartEC")){
        	response.put(getECInformation(context, sObjectId));
        }else if(dashboardRequest.equalsIgnoreCase("divChartResponsibility")){
        	response.put(getDesignResponsibilityInformation(context, sObjectId));
        }else if(dashboardRequest.equalsIgnoreCase("divChartTimeline")){
        	response.put(getTimeLineInformation(context, sObjectId));
        }else if(dashboardRequest.equalsIgnoreCase("coveredRequirements")){
        	JSONArray result = getDerivedRequirementsStatus(context, sObjectId,"from") ;
        	response.put(result.get(0));
        	response.put(result.get(1));
        	//response.put(getDerivedRequirementsStatus(context, sObjectId,"from"));
        }else if(dashboardRequest.equalsIgnoreCase("refinedRequirements")){
        	JSONArray result = getDerivedRequirementsStatus(context, sObjectId,"to") ;
        	response.put(result.get(0));
        	response.put(result.get(1));
        	//response.put(getDerivedRequirementsStatus(context, sObjectId,"to"));
        }else if(dashboardRequest.equalsIgnoreCase("subRequirements")){
        	response.put(getSubRequirements(context, sObjectId));
        }else if(dashboardRequest.equalsIgnoreCase("divChartPLMParameter")){
        	JSONArray result = getPLMParameterValues(context, sObjectId) ;
        	response.put(result.get(0));
        	response.put(result.get(1));
        	//response.put(getPLMParameterValues(context, sObjectId));
        }
		return response;
	} 
	
    @com.matrixone.apps.framework.ui.ProgramCallable
    public String getWeekOfTimeline(java.util.List<String> lTimeline, java.util.List<Integer> lYears, java.util.List<Integer> lWeeksMonths, String sStart, SimpleDateFormat sdf, Boolean bUseMonths) throws ParseException {
        String sResult = "0";
        if(null != sStart) {
            if(!"".equals(sStart)) {

                Calendar cStart        = Calendar.getInstance();
                cStart.setTime(sdf.parse(sStart));

                for(int j = 0; j < lTimeline.size(); j++) {

                    int iYearReference          = lYears.get(j);
                    int iWeekMonthReference     = lWeeksMonths.get(j);
                    int iYearStart              = cStart.get(Calendar.YEAR);
                    int iMonthStart             = cStart.get(Calendar.WEEK_OF_YEAR);
                    if(bUseMonths) { iMonthStart = cStart.get(Calendar.MONTH) + 1; }

                    if(iYearStart == iYearReference) {
                        if(iMonthStart <= iWeekMonthReference) {
                            sResult = String.valueOf(j+1);
                            break;
                        }
                    } else if(iYearStart <= iYearReference) {
                            sResult = String.valueOf(j+1); 
                            break;                        
                    }
                }
            }
        }
        return sResult;
    }
}


