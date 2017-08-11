/*
**  emxTemplateDrivenDashboardBase.java
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
*/
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.SetList;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.TaskHolder;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.apps.program.Assessment;
import com.matrixone.apps.program.CostItem;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.ProjectTemplate;
import com.matrixone.apps.program.Task;

/*****************************************************************************************
*       New JPO for Config Table Conversion Task
*******************************************************************************************/
/**
 * The <code>emxTemplateDrivenDashboardBase</code> class represents the Dashboard JPO
 * functionality for the AEF type.
 *
 * @version PMC 10-6 - Copyright (c) 2002, MatrixOne, Inc.
 * TODO - should mention X5 
 */
public class emxTemplateDrivenDashboardBase_mxJPO
{
    private static final String SELECT_TEMPLATE_DEFAULTNAME   = "ProgramCentral.Dashboards.TemplateDefaultName";
    private static final String STR_TEMPLATE_CMD_NAME               = PropertyUtil.getSchemaProperty( "command_PMCProjectTemplatesCommand" );
    private static final String STR_INCLUDELEVEL_CMD_NAME           = PropertyUtil.getSchemaProperty( "command_PMCIncludeLevelCommand" );
    private static final String STR_WBSTASKS_CMD_NAME               = PropertyUtil.getSchemaProperty( "command_PMCShowWBSTasksCommand" );
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public emxTemplateDrivenDashboardBase_mxJPO(Context context, String[] args)
    throws Exception
    {
        // Call the super constructor
        super();     
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     */
    public int mxMain(Context context, String[] args)
      throws Exception
    {
        if (!context.isConnected())
            throw new Exception("not supported on desktop client");
        return 0;
    }

    /**
     * This method gets the List of Dashboards depending on the context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return MapList contains list of Dashboards
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public MapList getDashboards(Context context, String[] args)
      throws Exception
    {
        MapList dashboardMapList = new MapList();
        String dashboardName = "";
        long dashboardCount = 0L;
        int i = 0;
        try
        {
            SetList dashboardList = matrix.db.Set.getSets(context, true);
            String command = "";

            if (!dashboardList.isEmpty())
            {
                HashMap dashboardMap = null;
                String dbDesc = "";
                command  = "list property on set $1"; //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011
                Iterator dbList = dashboardList.iterator();
                while (dbList.hasNext())// loop through each set owned by the user
                {
                    matrix.db.Set thisSet = (matrix.db.Set) dbList.next();
                    dashboardName = thisSet.toString();
                    if (dashboardName.startsWith(".dashboard-"))
                    {
                      //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:start
  				String output =  MqlUtil.mqlCommand(context, command,dashboardName); 
                        dashboardName = dashboardName.substring(11);
                        dashboardMap = new HashMap();
                        dashboardCount = thisSet.count(context);
                        Long countLong = new java.lang.Long(dashboardCount);
                        i = 0;
                        i = output.indexOf(".dashboard-", i);
                        dbDesc = "";
                        if (i != -1)
                        {//if Description is present
                            i += 11;
                            int endNameIndex = output.indexOf("value", i);
                            int descBeginIndex = endNameIndex + 6;
                            dbDesc = output.substring(descBeginIndex,
                                    output.length());
                        }
                        else
                        {//if Description is not present
                            dbDesc = "";
                        }
                        dashboardMap.put("id", dashboardName);
                        dashboardMap.put("dashboardCount", countLong.toString());
                        dashboardMap.put("dashboardDesc", dbDesc);
                        dashboardMapList.add((Map) dashboardMap);
                    }
                    //End if
                }
                // End while
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        
        return dashboardMapList;
      
    }

    /**
     * This method gets the Dashboard Names.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectList contains a MapList.
     * @return Vector contains list of Dashboard names
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public Vector getName(Context context, String[] args)
      throws Exception
    {
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        Map paramList               = (Map) programMap.get("paramList");
        String strSuiteDir          = (String) paramList.get("SuiteDirectory");
        MapList relBusObjPageList   = (MapList) programMap.get("objectList");
        Vector vecName              = new Vector(relBusObjPageList.size());
        
        String strURL               = "../programcentral/emxProgramCentralDashboardsDetailsFS.jsp?dashboardName=";
        
        String imageStr             = "../common/images/iconSmallDashboard.gif";

        String strDashBoardName     = "";
        boolean isPrinterFriendly = false;
        String strPrinterFriendly = (String)paramList.get("reportFormat");
        if ( strPrinterFriendly != null ) {
            isPrinterFriendly = true;
        }
        try
        {
            StringBuffer sbOutput = null;
            Map relBusObjMap = null;
            String strName = null;
            for (int i = 0; i < relBusObjPageList.size(); i++)
            {
                relBusObjMap = (Map) relBusObjPageList.get(i);
                strName = (String) relBusObjMap.get("id");
                sbOutput = new StringBuffer();
                sbOutput.append("<img src=\"" + imageStr + "\" border=\"0\">");
                if(!isPrinterFriendly)
                {
                  strDashBoardName      = FrameworkUtil.findAndReplace(strName,"'","*");
                  sbOutput.append("<a href=\"javascript:emxTableColumnLinkClick('" + strURL);
                                sbOutput.append(XSSUtil.encodeForURL(context,strDashBoardName));
                                sbOutput.append("&emxSuiteDirectory=");
                                sbOutput.append(strSuiteDir);
                                sbOutput.append("', '600', '600', 'false', 'content','')\"  class='object'>");
                                sbOutput.append(strName);
                                sbOutput.append("</a>");
                }
                else
                {
                  sbOutput.append(strName);
                }
                vecName.addElement(sbOutput.toString());
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        return vecName;
       
    }

    /**
     * This method gets the Description of Dashboards.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectList contains a MapList
     * @return Vector containing list of Dashboards description
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public ArrayList getDescription(Context context, String[] args)
      throws Exception
    {
        HashMap programMap        = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList) programMap.get("objectList");
        ArrayList descList         = new ArrayList(relBusObjPageList.size());
        try
        {
            Map relBusObjMap = null;
            for (int i = 0; i < relBusObjPageList.size(); i++)
            {
                relBusObjMap = (Map) relBusObjPageList.get(i);               
                descList.add((String) relBusObjMap.get("dashboardDesc"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
           
        }        
        return descList;
        
    }

    /**
     * This method gets the Count of Dashboards.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectList contains a MapList
     * @return Vector containing list of Dashboards Count
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public Vector getCount(Context context, String[] args)
      throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList) programMap.get("objectList");
        Vector vecCount = new Vector(relBusObjPageList.size());
        try
        {
            Map relBusObjMap = null;
            for (int i = 0; i < relBusObjPageList.size(); i++)
            {
                relBusObjMap = (Map) relBusObjPageList.get(i);                
                vecCount.addElement((String) relBusObjMap.get("dashboardCount"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;           
        }       
         return vecCount;       
    }
 
    /**
     * This function returns the HashMap with field_display_choices and field_choices of Templates to be shown in the PMCProjectTemplatesCommand command
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.lang.Object
     * @throws Exception java.lang.Exception
     */
    public static Object getAllProjectTemplates(Context context, String[] args )
    throws Exception
    {
        HashMap hmRangeMap  = new HashMap();
        try
        {     
            HashMap programMap   = (HashMap) JPO.unpackArgs(args);
            String showTemplates = (String)programMap.get("showTemplates");
            StringList sTemplateIds            = new StringList();
            StringList sTemplateNameDisplay    = new StringList();
         
            emxProjectTemplate_mxJPO base      = new emxProjectTemplate_mxJPO(context, null);
            MapList projTemplatesList          = null;
                     
			if(EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"ProgramCentral.Dashboards.TemplateOptions_Active", "en").equalsIgnoreCase(showTemplates)){
                projTemplatesList = base.getActiveProjectTemplates(context,args);    
            }
			else if(EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"ProgramCentral.Dashboards.TemplateOptions_All", "en").equalsIgnoreCase(showTemplates)){
                projTemplatesList = base.getAllProjectTemplates(context,args);   
            }
			else{
                projTemplatesList = base.getActiveProjectTemplates(context,args);            
            }
          
                        
            Iterator templateListIterator      = projTemplatesList.iterator();
            Map templateMap                    = null;        
            String templateId                  = null; 
            ProjectTemplate projectTemplate    = null;
            
            String languageStr                 = context.getSession().getLanguage();
			String defaultTempName = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					SELECT_TEMPLATE_DEFAULTNAME, languageStr);
			String defaultTempValue = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					SELECT_TEMPLATE_DEFAULTNAME, "en");   

            sTemplateNameDisplay.add(defaultTempName);
            sTemplateIds.add(defaultTempValue);
            
            while(templateListIterator.hasNext())
            {
                templateMap     = (Map) templateListIterator.next();
                templateId      = (String) templateMap.get(DomainConstants.SELECT_ID);
                projectTemplate = (ProjectTemplate) DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_TEMPLATE,DomainConstants.PROGRAM);
                
                projectTemplate.setId(templateId);             
                
                sTemplateNameDisplay.add(projectTemplate.getName(context));
                sTemplateIds.add(templateId);
            }     
            hmRangeMap.put("field_display_choices",sTemplateNameDisplay);
            hmRangeMap.put("field_choices", sTemplateIds );
        }
        catch (Exception ex)
        {         
            throw new FrameworkException((String) ex.getMessage());
        }
        if(hmRangeMap != null && !"".equals( hmRangeMap ))
        {
            return hmRangeMap;
        }
        else
        {
            return Integer.valueOf(0);
        }
    }
    
    
    /**
     * 
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.util.Vector
     * @throws Exception java.lang.Exception
     */
    public Vector getDashboardName(Context context, String[] args)
    throws Exception
    {
    	HashMap programMap          = (HashMap) JPO.unpackArgs(args);
    	Map paramList               = (Map) programMap.get("paramList");
    	String strSuiteDir          = (String) paramList.get("SuiteDirectory");
    	MapList relBusObjPageList   = (MapList) programMap.get("objectList");
    	Vector vecName              = new Vector(relBusObjPageList.size());     
    	String imageStr             = "../common/images/iconSmallDashboard.gif";
    	boolean isPrinterFriendly   = false;
    	String strPrinterFriendly   = (String)paramList.get("reportFormat");

    	if ( strPrinterFriendly != null ) {
    		isPrinterFriendly = true;
    	}
    	try
    	{
    		Map relBusObjMap      = null;
    		String strName        = null;
    		StringBuffer sbOutput = null;

    		StringBuffer strURL = new StringBuffer(50);

    		strURL.append("../common/emxIndentedTable.jsp?")
    		.append("toolbar=PMCDashboardToolbarMenu&amp;")
    		.append("program=emxTemplateDrivenDashboard:getRelatedProjects&amp;") 
    		.append("table=PMCDashboardDetailsTable&amp;")
    		.append("header=emxProgramCentral.ProgramTop.DashboardDetails&amp;") 
    		.append("selection=multiple&amp;")                                                                                
    		.append("suiteKey=ProgramCentral&amp;HelpMarker=emxhelpdashboardsdetails&amp;")  
    		.append("isFromDashboard=true&amp;")
    		.append("showRMB=false&amp;")
    		.append("dashboardName=");

    		for (int i = 0; i < relBusObjPageList.size(); i++)
    		{
    			relBusObjMap = (Map) relBusObjPageList.get(i);
    			strName      = (String) relBusObjMap.get("name");
    			sbOutput     = new StringBuffer(20);
    			sbOutput.append("<img src=\"" + imageStr + "\" border=\"0\"/>");
    			if(!isPrinterFriendly)
    			{
    				String strDashBoardName = FrameworkUtil.findAndReplace(strName, "&", "|*|");
    				sbOutput.append("<a href=\"javascript:emxTableColumnLinkClick('" + strURL.toString() );
    				sbOutput.append(XSSUtil.encodeForJavaScript(context,strDashBoardName));
    				sbOutput.append("&amp;emxSuiteDirectory=");
    				sbOutput.append(strSuiteDir);
    				sbOutput.append("', '600', '600', 'false', 'content','')\"  class='object'>");
    				sbOutput.append(XSSUtil.encodeForXML(context,strName));
    				sbOutput.append("</a>");
    			}
    			else
    			{
    				sbOutput.append(XSSUtil.encodeForHTML(context,strName));
    			}             
    			vecName.addElement(sbOutput.toString());              
    		}
    	}
    	catch (Exception ex)
    	{
    		throw ex;
    	}
    	return vecName;

    }
    
   
   /**
    * This function returns the Maplist containing related objects using the objectId and recursionLevel parameters given.
    * @param context matrix.db.Context
    * @param objectId java.lang.String
    * @param recursionLevel short
    * @return com.matrixone.apps.domain.util.MapList
    * @throws Exception java.lang.Exception
    */    
    private static MapList getWBSTasks(Context context, String objectId, short recursionLevel) throws Exception{
        MapList mapList = new MapList();        
       
        DomainObject rootNodeObj       = DomainObject.newInstance(context, objectId);        
        StringList objectSelects       = new StringList(7);
        StringList relSelect = new StringList(1);
        objectSelects.addElement(DomainConstants.SELECT_ID);
        objectSelects.addElement(DomainConstants.SELECT_NAME);            
        objectSelects.addElement(DomainConstants.SELECT_TYPE);
        objectSelects.addElement(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
        objectSelects.addElement(Task.SELECT_BASELINE_CURRENT_END_DATE);        
        objectSelects.addElement(Task.SELECT_PERCENT_COMPLETE);
        objectSelects.addElement(DomainConstants.SELECT_CURRENT);
        
        relSelect.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER);
        
        mapList = rootNodeObj.getRelatedObjects(context,
                                                DomainConstants.RELATIONSHIP_SUBTASK,
                                                DomainConstants.QUERY_WILDCARD,
                                                false,
                                                true,
                                                recursionLevel,
                                                objectSelects,
                                                relSelect, //null,//relationshipSelects
                                                null,
                                                null,
                                                null,
                                                null,
                                                null) ;
          
        
        return mapList;
     }

    

    
    /**
     * 
     * @return int
     * @throws Exception
     */
    private int[] getThresholdValues(Context context)
    throws Exception
    {       
        int slipValue1=0;
        int slipValue2=0;
        int riskValue1=0;
        int riskValue2=0;
        int costratioValue1=100;
        int costratioValue2=110;      
        String ascendText1 ="eServiceApplicationProgramCentral.SlipThresholdGreenYellow";
        String ascendText2 ="eServiceApplicationProgramCentral.SlipThresholdYellowRed";
        String ascendText3 ="eServiceApplicationProgramCentralRPNThreshold.Yellow";
        String ascendText4 ="eServiceApplicationProgramCentralRPNThreshold.Red";
        String ascendText5 ="eServiceApplicationProgramCentral.CostRatioThresholdGreenYellow";
        String ascendText6 ="eServiceApplicationProgramCentral.CostRatioThresholdYellowRed";
       
        String s1 = EnoviaResourceBundle.getProperty(context,ascendText1);
        s1=s1.trim();
        slipValue1 = Integer.parseInt(s1);
        String s2  = EnoviaResourceBundle.getProperty(context,ascendText2);
        s2=s2.trim();
        slipValue2 = Integer.parseInt(s2);
        String s5  = EnoviaResourceBundle.getProperty(context,ascendText5);
        if ( s5 != null )
        {
          s5=s5.trim();
          costratioValue1 = Integer.parseInt(s5);
        }
        String s6 = EnoviaResourceBundle.getProperty(context,ascendText6);
        if ( s6 != null )
        {
        s6=s6.trim();
          costratioValue2 = Integer.parseInt(s6);
       }

          String s3 = EnoviaResourceBundle.getProperty(context,ascendText3);
          s3=s3.trim();
          riskValue1 = Integer.parseInt(s3);
          String s4  = EnoviaResourceBundle.getProperty(context,ascendText4);
          s4=s4.trim();
          riskValue2 = Integer.parseInt(s4);


        int values[]={slipValue1,slipValue2,riskValue1,riskValue2,costratioValue1,costratioValue2};
       return values;
       
    }
    
    /**
     * 
     * @return
     * @throws Exception
     */
    private String[] getColorCodeValues(Context context) throws Exception
    {
      String slipColor1="008000";
      String slipColor2="FFCC00";
      String slipColor3="FF0000";
      String riskColor1="008000";
      String riskColor2="FFCC00";
      String riskColor3="FF0000";
      String costRatioColor1="008000";
      String costRatioColor2="FFCC00";
      String costRatioColor3="FF0000";
      String ascendText1 ="eServiceApplicationProgramCentral.SlipThreshholdColor1";
      String ascendText2 ="eServiceApplicationProgramCentral.SlipThreshholdColor2";
      String ascendText3 ="eServiceApplicationProgramCentral.SlipThreshholdColor3";
      String ascendText4 ="eServiceApplicationProgramCentral.RiskThreshholdColor1";
      String ascendText5 ="eServiceApplicationProgramCentral.RiskThreshholdColor2";
      String ascendText6 ="eServiceApplicationProgramCentral.RiskThreshholdColor3";
      String ascendText7 ="eServiceApplicationProgramCentral.CostRatioThreshholdColor1";
      String ascendText8 ="eServiceApplicationProgramCentral.CostRatioThreshholdColor2";
      String ascendText9 ="eServiceApplicationProgramCentral.CostRatioThreshholdColor3";


      String s1 = EnoviaResourceBundle.getProperty(context,ascendText1);
      slipColor1=s1.trim();
      String s2 = EnoviaResourceBundle.getProperty(context,ascendText2);
      slipColor2=s2.trim();
      String s3 = EnoviaResourceBundle.getProperty(context,ascendText3);
      slipColor3=s3.trim();
      String s4 = EnoviaResourceBundle.getProperty(context,ascendText4);
      riskColor1=s4.trim();
      String s5 = EnoviaResourceBundle.getProperty(context,ascendText5);
      riskColor2=s5.trim();
      String s6 = EnoviaResourceBundle.getProperty(context,ascendText6);
      riskColor3=s6.trim();
      String s7 = EnoviaResourceBundle.getProperty(context,ascendText7);
      costRatioColor1=s7.trim();
      String s8 = EnoviaResourceBundle.getProperty(context,ascendText8);
      costRatioColor2=s8.trim();
      String s9 = EnoviaResourceBundle.getProperty(context,ascendText9);
      costRatioColor3=s9.trim();
      String values[]={slipColor1,slipColor2,slipColor3,riskColor1,riskColor2,riskColor3,costRatioColor1,costRatioColor2,costRatioColor3};

      return values;
     }
    
    /**
     * 
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.util.Vector
     * @throws Exception java.lang.Exception
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRelatedProjects(Context context, String[] args)
    throws Exception
    {
    	MapList returnList          = new MapList(); 
    	try{
    		//Added for : Context user has preference currency or not
    		emxProgramCentralUtilBase_mxJPO.getUserPreferenceCurrency(context, "emxProjectBudget.Error.NoDefaultCurrencyDefined");

    		HashMap programMap          = (HashMap) JPO.unpackArgs(args);
    		String busId                = (String)programMap.get("objectId");
    		String dashboardName        = (String)programMap.get("dashboardName");
    		dashboardName				= FrameworkUtil.findAndReplace(dashboardName, "|*|", "&");
    		String selectState          = (String)programMap.get("mx.page.filter");       
    		String jsTreeID             = (String)programMap.get("jsTreeID");

    		String selectedProjectTemplateId = (String) programMap.get("PMCProjectTemplatesCommand");

    		com.matrixone.apps.program.ProjectSpace projectSpace =
    				(com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
    		com.matrixone.apps.program.Task task =
    				(com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, DomainConstants.PROGRAM);
    		com.matrixone.apps.program.Risk risk =
    				(com.matrixone.apps.program.Risk) DomainObject.newInstance(context, DomainConstants.TYPE_RISK, DomainConstants.PROGRAM);
    		com.matrixone.apps.common.Person person =
    				(com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
    		com.matrixone.apps.program.Program program =
    				(com.matrixone.apps.program.Program) DomainObject.newInstance(context, DomainConstants.TYPE_PROGRAM, DomainConstants.PROGRAM);

    		StringList busSelects =  new StringList(7);
    		busSelects.add(projectSpace.SELECT_ID);
    		busSelects.add(projectSpace.SELECT_NAME);
    		busSelects.add(projectSpace.SELECT_TYPE);
    		busSelects.add(projectSpace.SELECT_BUSINESS_UNIT_NAME);
    		busSelects.add(projectSpace.SELECT_CURRENT);
    		//busSelects.add(projectSpace.SELECT_OWNER);
    		busSelects.add(projectSpace.SELECT_PROGRAM_NAME);
    		busSelects.add(projectSpace.SELECT_PROJECT_TEMPLATE_ID);

    		StringList relSelects = new StringList(1);
    		relSelects.add(com.matrixone.apps.common.SubtaskRelationship.SELECT_TASK_WBS);

    		MapList setProjects      = new MapList();
    		String sArchiveStateName = PropertyUtil.getSchemaProperty(context,"policy",projectSpace.POLICY_PROJECT,"state_Archive");

    		String busWhere = DomainConstants.EMPTY_STRING;

    		String preferredCurrency = PersonUtil.getCurrency(context);

    		String RiskURLPrefix = UIMenu.getHRef(UIMenu.getCommand(context, "PMCRisk"));
    		RiskURLPrefix = RiskURLPrefix.replace("${SUITE_DIR}","../programcentral");

    		String assessmentURLPreFix = UIMenu.getHRef(UIMenu.getCommand(context, "PMCAssessment"));
    		assessmentURLPreFix = assessmentURLPreFix.replace("${COMMON_DIR}","../common");

    		String qualityURLPrefix = UIMenu.getHRef(UIMenu.getCommand(context, "PMCQuality"));
    		qualityURLPrefix = qualityURLPrefix.replace("${COMMON_DIR}","../common");
    		// String relWhere = null;
    		if (selectState == null || "".equals(selectState) || selectState.equalsIgnoreCase("---"))
    		{
    			selectState = "All";
    		}

    		// If selectState is All, then display all archived objects except concept projects
    		if (selectState.equals("All") || selectState.equals("---") )
    		{
    			busWhere = "current!='" + sArchiveStateName + "'&&type!='" + projectSpace.TYPE_PROJECT_CONCEPT + "'";
    		}
    		else
    		{
    			// Else, only display objects described by selectState filter (do not include concept projects)
    			busWhere = "current=='" + selectState + "'&&type!='" + projectSpace.TYPE_PROJECT_CONCEPT + "'" +
    					"&&current!='" + sArchiveStateName + "'";
    		}

    		if (busId != null && !"null".equals(busId) && !"".equals(busId))
    		{
    			com.matrixone.apps.domain.DomainObject domain=new com.matrixone.apps.domain.DomainObject(busId);
    			StringList objectSelects = new StringList(1);

    			objectSelects.add("type.kindof");
    			Map objectDetails = domain.getInfo(context,objectSelects);
    			if(!((objectDetails.get("type.kindof")).equals(domain.TYPE_PROGRAM)))
    			{
    				projectSpace.setId(busId);
    				Map theProjectMap = (Map) projectSpace.getInfo(context, busSelects);
    				setProjects.add( theProjectMap );            
    			}
    			else
    			{
    				program.setId( busId );
    				setProjects = program.getProjects( context, busSelects, busWhere );
    			}
    		}
    		//Take the Set name contained in dashboardName and retrieve the contents of the set.
    		else
    		{
    			if (dashboardName != null && !dashboardName.equals(""))
    			{
    				setProjects = getDashboardList(context, dashboardName, busSelects );
    			}
    		}

    		if(ProgramCentralUtil.isNotNullString(selectedProjectTemplateId)
    				&& !"Default Template".equalsIgnoreCase(selectedProjectTemplateId)) {

    			for(int i = 0; i < setProjects.size(); i++) {

    				Map ProjectInfoMap = (Map) setProjects.get(i);
    				String projectTemplateId = 
    						(String)ProjectInfoMap.get(ProjectSpace.SELECT_PROJECT_TEMPLATE_ID);

    				if(ProgramCentralUtil.isNullString(projectTemplateId) || 
    						!projectTemplateId.equalsIgnoreCase(selectedProjectTemplateId)) {
    					setProjects.remove(ProjectInfoMap);
    					--i;
    				}
    			}
    		}
    		boolean slipFlag=true;

    		int slipValue1 = 0;
    		int slipValue2 = 0;
    		int riskValue1 = 0;
    		int riskValue2 = 0;
    		int costratioValue1 = 0;
    		int costratioValue2 = 0;

    		String slipColor1 = DomainConstants.EMPTY_STRING;
    		String slipColor2 = DomainConstants.EMPTY_STRING;
    		String slipColor3 = DomainConstants.EMPTY_STRING;
    		String riskColor1 = DomainConstants.EMPTY_STRING;
    		String riskColor2 = DomainConstants.EMPTY_STRING;
    		String riskColor3 = DomainConstants.EMPTY_STRING;
    		String costRatioColor1 = DomainConstants.EMPTY_STRING;
    		String costRatioColor2 = DomainConstants.EMPTY_STRING;
    		String costRatioColor3 = DomainConstants.EMPTY_STRING;

    		if (slipFlag)
    		{
    			int thresholdValues[]    = {5,10,5,10,5,10};
    			String colorCodeValues[] = {"008000","FFCC00","FF0000","008000","FFCC00","FF0000","008000","FFCC00","FF0000"};
    			try
    			{
    				thresholdValues = getThresholdValues(context);
    				colorCodeValues = getColorCodeValues(context);                  
    			}
    			catch(Exception e)
    			{
    				//System.out.println(e);
    			}

    			slipValue1 = thresholdValues[0];
    			slipValue2 = thresholdValues[1];
    			riskValue1 = thresholdValues[2];
    			riskValue2 = thresholdValues[3];

    			costratioValue1 = thresholdValues[4];
    			costratioValue2 = thresholdValues[5];

    			slipColor1 = colorCodeValues[0];
    			slipColor2 = colorCodeValues[1];
    			slipColor3 = colorCodeValues[2];

    			riskColor1 = colorCodeValues[3];
    			riskColor2 = colorCodeValues[4];
    			riskColor3 = colorCodeValues[5];

    			costRatioColor1 = colorCodeValues[6];
    			costRatioColor2 = colorCodeValues[7];
    			costRatioColor3 = colorCodeValues[8];

    			slipFlag=false;
    		}

    		// If no filter is given when the page is called, then all objects will be displayed    
    		if (selectState == null || "".equals(selectState) || selectState.equalsIgnoreCase("---"))
    		{
    			selectState = "All";
    		}

    		// If selectState is All, then display all archived objects except concept projects
    		if (selectState.equals("All") || selectState.equals("---") )
    		{
    			busWhere = "current!='" + sArchiveStateName + "'&&type!='" + projectSpace.TYPE_PROJECT_CONCEPT + "'";
    		}
    		else
    		{
    			// Else, only display objects described by selectState filter (do not include concept projects)
    			busWhere = "current=='" + selectState + "'&&type!='" + projectSpace.TYPE_PROJECT_CONCEPT + "'" +
    					"&&current!='" + sArchiveStateName + "'";
    		}
    		ListIterator listItr = setProjects.listIterator();

    		StringList task_busSelects =  new StringList(6);
    		task_busSelects.add(task.SELECT_ID);
    		task_busSelects.add(task.SELECT_NAME);
    		task_busSelects.add(task.SELECT_TASK_ACTUAL_FINISH_DATE);
    		task_busSelects.add(task.SELECT_TASK_ESTIMATED_FINISH_DATE);
    		task_busSelects.add(task.SELECT_CURRENT);
    		task_busSelects.add(task.SELECT_BASELINE_CURRENT_END_DATE);

    		// to get RPN values
    		StringList risk_relSelects =  new StringList(3);
    		risk_relSelects.add(com.matrixone.apps.program.RiskRPNRelationship.SELECT_EFFECTIVE_DATE);
    		risk_relSelects.add(com.matrixone.apps.program.RiskRPNRelationship.SELECT_RISK_RPN_VALUE);
    		risk_relSelects.add(com.matrixone.apps.program.RiskRPNRelationship.SELECT_RISK_IMPACT);

    		//Financial selectables
    		StringList BudgetbusSelects = new StringList(7);
    		BudgetbusSelects.add(ProgramCentralConstants.SELECT_ID);
    		BudgetbusSelects.add(com.matrixone.apps.program.FinancialItem.SELECT_NAME);
    		BudgetbusSelects.add(com.matrixone.apps.program.FinancialItem.SELECT_PLANNED_COST);
    		BudgetbusSelects.add(ProgramCentralConstants.SELECT_ATTRIBUTE_PLANNED_COST_UNIT);
    		BudgetbusSelects.add(com.matrixone.apps.program.FinancialItem.SELECT_ACTUAL_COST);
    		BudgetbusSelects.add(ProgramCentralConstants.SELECT_ATTRIBUTE_ACTUAL_COST_UNIT);
    		BudgetbusSelects.add(com.matrixone.apps.program.FinancialItem.SELECT_ESTIMATED_COST);
    		BudgetbusSelects.add(ProgramCentralConstants.SELECT_ATTRIBUTE_ESTIMATED_COST_UNIT);
    		BudgetbusSelects.add(ProgramCentralConstants.SELECT_CURRENT);

    		//Financial selectables
    		StringList BenefitbusSelects = new StringList(7);
    		BenefitbusSelects.add(com.matrixone.apps.program.FinancialItem.SELECT_ID);
    		BenefitbusSelects.add(com.matrixone.apps.program.FinancialItem.SELECT_NAME);
    		BenefitbusSelects.add(com.matrixone.apps.program.FinancialItem.SELECT_PLANNED_BENEFIT);
    		BenefitbusSelects.add(ProgramCentralConstants.SELECT_ATTRIBUTE_PLANNED_BENEFIT_UNIT);
    		BenefitbusSelects.add(com.matrixone.apps.program.FinancialItem.SELECT_ACTUAL_BENEFIT);
    		BenefitbusSelects.add(ProgramCentralConstants.SELECT_ATTRIBUTE_ACTUAL_BENEFIT_UNIT);

    		java.util.Date date1              =  new java.util.Date();
    		java.util.HashMap incompleteTask  = new java.util.HashMap();
    		java.util.HashMap completeTask    = new java.util.HashMap();
    		String projId                     = "";        
    		long slipDay                      = 0;
    		long slipDayab                    = 0;
    		String col                        = "";
    		String currentPhase               = "";

    		Map projectMap = null;
    		while (listItr.hasNext())
    		{
    			slipDay         = 0;
    			slipDayab       = 0;
    			col             = "";
    			currentPhase    = "";

    			projectMap = (Map) listItr.next();
    			projId         = (String) projectMap.get(projectSpace.SELECT_ID);
    			projectSpace.setId(projId);
    			projectMap.put("projectId", projId);

    			//MapList projectTasks = projectSpace.getTasks(context, 1, task_busSelects, relSelects,false); // newly added
    			MapList projectTasks = Task.getTasks(context, (TaskHolder)projectSpace, 1, task_busSelects, relSelects);

    			projectTasks.addSortKey(com.matrixone.apps.common.SubtaskRelationship.SELECT_TASK_WBS,"descending", "integer");
    			projectTasks.sort();
    			incompleteTask.clear();
    			completeTask.clear();
    			if (! projectTasks.isEmpty())
    			{
    				Iterator taskItr = projectTasks.iterator();
    				while(taskItr.hasNext())
    				{
    					Map curState = (Map) taskItr.next();

    					String currstate = (String) curState.get(task.SELECT_CURRENT);
    					task.setId((String) curState.get(task.SELECT_ID));
    					if ("Complete".equals(currstate))
    					{
    						String finishDate = "";
    						String baselineCurrentFinishDateStr = (String) curState.get(task.SELECT_BASELINE_CURRENT_END_DATE);
    						if (null == baselineCurrentFinishDateStr || "".equals(baselineCurrentFinishDateStr))
    						{
    							finishDate = (String) curState.get(task.SELECT_TASK_ACTUAL_FINISH_DATE);
    						}

    						else
    						{
    							finishDate = baselineCurrentFinishDateStr;
    						}
    						completeTask.put(finishDate,curState);
    					}
    					else
    					{
    						String finishDate = "";
    						String baselineCurrentFinishDateStr = (String) curState.get(task.SELECT_BASELINE_CURRENT_END_DATE);
    						if (null == baselineCurrentFinishDateStr || "".equals(baselineCurrentFinishDateStr)){
    							finishDate = (String) curState.get(task.SELECT_TASK_ESTIMATED_FINISH_DATE);
    						}else{
    							finishDate = baselineCurrentFinishDateStr;
    						}    
    						incompleteTask.put(finishDate,curState);
    					}
    				} // end while
    			} //end if task list is not empty

    			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
    			java.util.Date min = new java.util.Date();
    			Map disply         = null;
    			String  nextURL1   = "";
    			String minStr = "";

    			if ( !incompleteTask.isEmpty() || !completeTask.isEmpty() )
    			{
    				if(incompleteTask.isEmpty())
    				{
    					if(! completeTask.isEmpty())
    					{
    						Object keys[] = (completeTask.keySet()).toArray();
    						java.util.Date test;
    						java.util.Date max = sdf.parse(((String)((Map)completeTask.get((String)keys[0])).get(task.SELECT_TASK_ACTUAL_FINISH_DATE)));
    						minStr = (String)keys[0];

    						for (int i =0; i < keys.length; i++)
    						{
    							test = sdf.parse(((String)((Map)completeTask.get((String)keys[i])).get(task.SELECT_TASK_ACTUAL_FINISH_DATE)));
    							if(test.after(max))
    							{
    								minStr = (String)keys[i];
    								max = sdf.parse(((String)((Map)completeTask.get((String)keys[i])).get(task.SELECT_TASK_ACTUAL_FINISH_DATE)));
    							}

    						}//end for keys
    					}//end if completed list is not empty

    					disply = (Map) completeTask.get(minStr);
    					//if the task is complete use the Actual Finish date for slip days calculations
    					date1  = sdf.parse((String)disply.get(task.SELECT_TASK_ACTUAL_FINISH_DATE));
    					//if the task is complete use the estimated finish date
    					min    = sdf.parse((String)disply.get(task.SELECT_TASK_ESTIMATED_FINISH_DATE));
    				}
    				else
    				{
    					if(! incompleteTask.isEmpty())
    					{
    						Object keys[] = (incompleteTask.keySet()).toArray();

    						java.util.Date minDate = sdf.parse(((String)((Map)incompleteTask.get((String)keys[0])).get(task.SELECT_TASK_ESTIMATED_FINISH_DATE)));
    						minStr = (String)keys[0];
    						java.util.Date test;
    						for (int i =0; i <keys.length; i++)
    						{
    							test      = sdf.parse(((String)((Map)incompleteTask.get((String)keys[i])).get(task.SELECT_TASK_ESTIMATED_FINISH_DATE)));
    							if(test.before(minDate))
    							{
    								minStr = (String)keys[i];
    								minDate = sdf.parse(((String)((Map)incompleteTask.get((String)keys[i])).get(task.SELECT_TASK_ESTIMATED_FINISH_DATE)));
    							}               
    						}
    					}//end if
    					disply = (Map) incompleteTask.get(minStr);
    					//if the task is incomplete use the Estimated Finish date for slip days calculations
    					date1  = sdf.parse((String)disply.get(task.SELECT_TASK_ESTIMATED_FINISH_DATE));
    				}
    			}

    			if(minStr != null) {
    				projectMap.put("CurrentPhaseDate", minStr);

    			}
    			else {
    				projectMap.put("CurrentPhaseDate", "");
    			}
    			date1.setHours(0);
    			date1.setMinutes(0);
    			date1.setSeconds(0);
    			min.setHours(0);
    			min.setMinutes(0);
    			min.setSeconds(0);
    			if(disply!=null) {

    				//determine slip days and color of slip days
    				if ( date1.after(min) ) {
    					//if the (Estimated/Actual) date is after the current date (min) then
    					//the milestone (task) is currently on time, so show the slip days as
    					//the amount of time until (Estimated/Actual) Finish date ( always green)
    					int dayOfWeek = min.getDay();
    					if (dayOfWeek  == 0 || dayOfWeek == 6) {
    						//don't remove day since start day is not a week day
    						slipDay = task.computeDuration(min,date1);
    					} else {
    						//week day, so take out the starting day
    						slipDay = task.computeDuration(min,date1) - 1;
    					}
    					//slip day color should always be green when it is before the (Estimated/Actual) Finish date
    					slipDayab = java.lang.Math.abs(slipDay);
    					col = "#"+slipColor1;
    				} else {
    					//calculate the slip days and change color according to the amount of days
    					//the milestone (task) has slipped
    					slipDay = task.computeDuration(date1,min) - 1;//take out the starting day
    					//determine color of slip days
    					if ( slipDay >= slipValue2 ) {
    						slipDayab = java.lang.Math.abs(slipDay);
    						col       = "#" + slipColor3;
    					} else if ( slipDay > slipValue1 && slipDay < slipValue2 ) {
    						slipDayab = java.lang.Math.abs(slipDay);
    						col       = "#" + slipColor2;
    					} else {
    						slipDayab = java.lang.Math.abs(slipDay);
    						col = "#"+slipColor1;
    					}//ends else

    					// set the color to green if project is complete
    					if(incompleteTask.isEmpty())
    					{
    						col = "#"+slipColor1;
    					}
    				}//ends else
    				//Start:372640
    				if ((jsTreeID == null) || ("null".equals(jsTreeID))) {
    					nextURL1 = "../common/emxTree.jsp?objectId=" + disply.get(task.SELECT_ID);
    				}
    				else {
    					nextURL1 = "../common/emxTree.jsp?objectId="+  disply.get(task.SELECT_ID) +
    							"&mode=insert&jsTreeID=" + jsTreeID + "&AppendParameters=false";
    				}
    				nextURL1 = nextURL1.replaceAll("&","&amp;");
    				//End:372640
    				currentPhase = (String) disply.get(task.SELECT_NAME);
    				projectMap.put("slipDays", String.valueOf(slipDayab));
    			}
    			else {
    				projectMap.put("slipDays", "");
    			}

    			projectMap.put("col", col);
    			projectMap.put("currentPhase", currentPhase);
    			projectMap.put("nextURL1", nextURL1);

    			int yellowRisks=0;
    			int redRisks=0;
    			int greenRisks=0;
    			int displayRisks=0;
    			double rpnNumber=0;
    			String riskCol ="";

    			StringList rbusSelects = new StringList(2);
    			rbusSelects.add(risk.SELECT_ID);
    			rbusSelects.add(risk.SELECT_NAME);
    			MapList projectRisks=risk.getRisks(context,projectSpace,rbusSelects,risk_relSelects,null);

    			Map currentRisk = null;
    			MapList RPNList = null;
    			String RPN ="0";
    			if (! projectRisks.isEmpty())
    			{
    				ListIterator listItr2 = projectRisks.listIterator();

    				while(listItr2.hasNext())
    				{
    					currentRisk=(Map)listItr2.next();
    					risk.setId((String)currentRisk.get(risk.SELECT_ID));
    					RPNList = risk.getRPNs(context, risk_relSelects, null);
    					RPN = "0.0";
    					if(!RPNList.isEmpty())
    					{
    						Map RPNMap = (Map) RPNList.get(0);
    						RPN = (String) RPNMap.get(com.matrixone.apps.program.RiskRPNRelationship.SELECT_RISK_RPN_VALUE);
    					}
    					try {
    						rpnNumber=Task.parseToDouble(RPN);
    					}
    					catch(Exception e) {
    						//do nothing
    					}

    					if ( rpnNumber >= riskValue2 )
    					{
    						redRisks++;
    					}
    					else if ( rpnNumber > riskValue1 && rpnNumber < riskValue2 )
    					{
    						yellowRisks++;
    					}
    					else
    					{
    						greenRisks++;
    					}
    				} // end of while loop
    			}

    			if(redRisks != 0)
    			{
    				displayRisks=redRisks;
    				riskCol ="#"+riskColor3;
    			}
    			else if(yellowRisks != 0)
    			{
    				displayRisks=yellowRisks;
    				riskCol = "#"+riskColor2;
    			}
    			else
    			{
    				displayRisks=greenRisks;
    				riskCol = "#"+riskColor1;
    			}

    			projectMap.put("displayRisks", String.valueOf(displayRisks));
    			projectMap.put("riskCol", riskCol);

    			// assessment selectables

    			StringList AssessmentbusSelects = new StringList(2);
    			AssessmentbusSelects.add(com.matrixone.apps.program.Assessment.SELECT_ORIGINATED);
    			AssessmentbusSelects.add(com.matrixone.apps.program.Assessment.SELECT_ASSESSMENT_STATUS );
    			MapList assessmentList = new MapList();

    			assessmentList=Assessment.getAssessments(context,projectSpace,AssessmentbusSelects,null,null,null);    			

    			Map LatestAssessment=null;
    			String status = "";
    			String assessmentURL = "";

    			if (! assessmentList.isEmpty())
    			{
    				Iterator assessmentListItr = assessmentList.iterator();
    				String dtstr1="";
    				String dtstr2="";
    				while(assessmentListItr.hasNext())
    				{
    					Map current       = (Map) assessmentListItr.next();
    					if(LatestAssessment==null)
    					{
    						LatestAssessment = current;
    					}
    					else
    					{
    						dtstr1 = (String) LatestAssessment.get(Assessment.SELECT_ORIGINATED);
    						dtstr2 = (String) current.get(Assessment.SELECT_ORIGINATED);
    						java.util.Date max  = sdf.parse(dtstr1);
    						java.util.Date min1 = sdf.parse(dtstr2);
    						if(max.compareTo(min1)<0)
    						{
    							LatestAssessment = current;
    						}
    					}
    				}
    			}

    			if(LatestAssessment!=null)
    			{
    				assessmentURL=assessmentURLPreFix+"&objectId=" +XSSUtil.encodeForURL(context, (String)projectMap.get(projectSpace.SELECT_ID)) + "&fromDashboard=true&suiteKey=ProgramCentral";
    				assessmentURL = assessmentURL.replaceAll("&","&amp;");
    				status = (String)LatestAssessment.get(Assessment.SELECT_ASSESSMENT_STATUS);
    			}
    			projectMap.put("status", status);
    			projectMap.put("assessmentURL", assessmentURL);

    			// for getting the values for Totalcost and Totalbenefit columns
    			//MapList financeItemList = null;
    			Map budget              = null;
    			Map benefit             = null;
    			double totalbenift      = 0.0;
    			double totalcost        = 0.0;
    			int costratio           = 0;
    			String budgetId           = "";
    			String benefitId           = "";
    			// financeItemList = com.matrixone.apps.program.FinancialItem.getFinancialItems(context,projectSpace,FinancialbusSelects);
    			String strBudget = DomainConstants.EMPTY_STRING;
    			String strBenefit = DomainConstants.EMPTY_STRING;
    			StringList relSelect = new StringList();
    			String whereClause = "" ;
    			DomainObject dmoProject =  DomainObject.newInstance(context, projId);
    			CostItem costItem = new CostItem();
    			MapList mlBudget =  dmoProject.getRelatedObjects(
    					context,
    					ProgramCentralConstants.RELATIONSHIP_PROJECT_FINANCIAL_ITEM,
    					ProgramCentralConstants.TYPE_BUDGET,
    					BudgetbusSelects,
    					relSelect,
    					false,
    					true,
    					(short)1,
    					whereClause,
    					DomainConstants.EMPTY_STRING,
    					0);

    			MapList mlBenefit =  dmoProject.getRelatedObjects(
    					context,
    					ProgramCentralConstants.RELATIONSHIP_PROJECT_FINANCIAL_ITEM,
    					ProgramCentralConstants.TYPE_BENEFIT,
    					BenefitbusSelects,
    					relSelect,
    					false,
    					true,
    					(short)1,
    					whereClause,
    					DomainConstants.EMPTY_STRING,
    					0);

    			double actualVal = 0.0d;
    			double plannedVal = 0.0d;
    			double estimatedVal = 0.0d;
    			String storedCurrency = "";

    			emxFinancialItem_mxJPO financial = new emxFinancialItem_mxJPO(context, args);
    			if (! mlBudget.isEmpty()){
    				Iterator budgetItr = mlBudget.iterator();
    				if(budgetItr.hasNext()){
    					budget   = (Map) budgetItr.next();
    					budgetId   = (String)budget.get(com.matrixone.apps.program.FinancialItem.SELECT_ID);
    					String Budgetstate = (String)budget.get(ProgramCentralConstants.SELECT_CURRENT);
    					actualVal = Task.parseToDouble((String)budget.get(com.matrixone.apps.program.FinancialItem.SELECT_ACTUAL_COST) );
    					storedCurrency = (String)budget.get(ProgramCentralConstants.SELECT_ATTRIBUTE_ACTUAL_COST_UNIT);  	
    					actualVal = financial.convertAmount(context, actualVal, storedCurrency, preferredCurrency, new Date(), false);

    					plannedVal = Task.parseToDouble((String)budget.get(com.matrixone.apps.program.FinancialItem.SELECT_PLANNED_COST));
    					storedCurrency = (String)budget.get(ProgramCentralConstants.SELECT_ATTRIBUTE_PLANNED_COST_UNIT);  	
    					plannedVal = financial.convertAmount(context, plannedVal, storedCurrency, preferredCurrency, new Date(), false);

    					estimatedVal = Task.parseToDouble( (String)budget.get(com.matrixone.apps.program.FinancialItem.SELECT_ESTIMATED_COST));
    					storedCurrency = (String)budget.get(ProgramCentralConstants.SELECT_ATTRIBUTE_ESTIMATED_COST_UNIT);  	
    					estimatedVal = financial.convertAmount(context, estimatedVal, storedCurrency, preferredCurrency, new Date(), false);
						boolean isFrozen = true;
						if (!"Plan Frozen".equalsIgnoreCase(Budgetstate)) {
    						totalcost = totalcost + plannedVal;
							costratio = 0;
							isFrozen = false;
						} else if (actualVal > 0) {
    						totalcost = totalcost + actualVal;

						} else {
							totalcost = totalcost + estimatedVal;
						}

						if ((isFrozen == true) && (estimatedVal != 0)) {
							costratio = (int) (((actualVal) * 100) / estimatedVal);
    					}
    				} //End of If statement

    				projectMap.put("costRatio", String.valueOf(costratio)+"%");
    				projectMap.put("totalCost", emxProgramCentralUtilBase_mxJPO.getFormattedCurrencyValue (context, null, preferredCurrency,totalcost));
    			}
    			else {
    				projectMap.put("costRatio", "");
    				projectMap.put("totalCost", "");
    			}

    			actualVal = 0.0d;
    			plannedVal = 0.0d;
    			if (! mlBenefit.isEmpty()){
    				Iterator benefitItr = mlBenefit.iterator();
    				if(benefitItr.hasNext()){
    					benefit   = (Map) benefitItr.next();
    					benefitId   = (String)benefit.get(com.matrixone.apps.program.FinancialItem.SELECT_ID);

    					actualVal = Task.parseToDouble((String)benefit.get(com.matrixone.apps.program.FinancialItem.SELECT_ACTUAL_BENEFIT)); 
    					storedCurrency = (String)benefit.get(ProgramCentralConstants.SELECT_ATTRIBUTE_ACTUAL_BENEFIT_UNIT);  	
    					actualVal = financial.convertAmount(context, actualVal, storedCurrency, preferredCurrency, new Date(), false);

    					plannedVal = Task.parseToDouble((String)benefit.get(com.matrixone.apps.program.FinancialItem.SELECT_PLANNED_BENEFIT)); 
    					storedCurrency = (String)benefit.get(ProgramCentralConstants.SELECT_ATTRIBUTE_PLANNED_BENEFIT_UNIT);  	
    					plannedVal = financial.convertAmount(context, plannedVal, storedCurrency, preferredCurrency, new Date(), false);

    					if(null != (String)benefit.get(com.matrixone.apps.program.FinancialItem.SELECT_ACTUAL_BENEFIT)){
    						if(actualVal == 0.0)
    							totalbenift = totalbenift + plannedVal;
    						else
    							totalbenift = totalbenift + actualVal;
    					}
    				} //End of If statement
    				projectMap.put("totalBenefit", emxProgramCentralUtilBase_mxJPO.getFormattedCurrencyValue (context, null, preferredCurrency,totalbenift));
    			}
    			else {
    				projectMap.put("totalBenefit", "");
    			}
    			String costColor = "";
    			if ( costratio > costratioValue2 ){
    				costColor = "#" + costRatioColor3;
    			}
    			else if ( costratio> costratioValue1 && costratio <= costratioValue2 ){
    				costColor = "#" + costRatioColor2;
    			}
    			else{
    				costColor= "#"+costRatioColor1;
    			}
    			projectMap.put("costColor", costColor);

    			StringList MemberbusSelects = new StringList(2);
    			MemberbusSelects.add(person.SELECT_FIRST_NAME);
    			MemberbusSelects.add(person.SELECT_LAST_NAME);

    			// Build busSelects and busWhere for retrieving owner information
    			//String owner = (String) projectMap.get(projectSpace.SELECT_OWNER);
    			//person = person.getPerson(context, owner);

    			// Create a map of the owner's information.
    			//Map ownerMap        = person.getInfo(context, MemberbusSelects);
    			//String Project_lead = ownerMap.get(person.SELECT_LAST_NAME) + ", " + ownerMap.get(person.SELECT_FIRST_NAME);
    			//projectMap.put(projectSpace.SELECT_OWNER, Project_lead);

    			// String newURL1 = "emxProgramCentralRiskSummaryFS.jsp?objectId=" + projectMap.get(projectSpace.SELECT_ID) + "&fromDashboard=true";

    			String newURL1 = RiskURLPrefix+"&objectId=" + projectMap.get(projectSpace.SELECT_ID) + "&fromDashboard=true&suiteKey=ProgramCentral";
    			newURL1 = newURL1.replaceAll("&","&amp;");

    			projectMap.put("newURL1", newURL1);
    			String newURL2 = "";
    			/*if(isKava)
            {
                newURL2 = "../programcentral/emxProgramCentralDashboardsMetricsCharts.jsp?objectId=" + projectMap.get(projectSpace.SELECT_ID) + 
                        "&objectName=" + projectMap.get(projectSpace.SELECT_NAME) +
                        "&treeNodeKey=node.Projects&suiteKey=eServiceSuiteProgramCentral"
                     +   "&objectUrl=";            

                newURL2 = com.matrixone.apps.domain.util.XSSUtil.encodeForURL(newURL2);

            }
            projectMap.put("newURL2", newURL2);*/

    			String  nextURL="";
    			if ((jsTreeID == null) || ("null".equals(jsTreeID)))
    			{       
    				nextURL =  "../common/emxTree.jsp?objectId=" + projectMap.get(projectSpace.SELECT_ID) +
    						"&mode=replace" + "&DefaultCategory=PMCGateDashboardCommandPowerView" + "&AppendParameters=false"; //Added:nr2:PRG:R210:Added for Project Gate
    			}
    			else
    			{
    				nextURL = "../common/emxTree.jsp?objectId="+  projectMap.get(projectSpace.SELECT_ID) +
    						"&mode=insert&jsTreeID=" + jsTreeID + "&DefaultCategory=PMCGateDashboardCommandPowerView" + "&AppendParameters=false"; //Added:nr2:PRG:R210:Added for Project Gate
    			}
    			nextURL = nextURL.replaceAll("&","&amp;");

    			String costURL="";
    			if ((jsTreeID == null) || ("null".equals(jsTreeID)))
    			{
    				costURL = "../common/emxTree.jsp?objectId=" + budgetId +
    						"&mode=replace" + "&AppendParameters=false";                
    			}
    			else{
    				costURL = "../common/emxTree.jsp?objectId=" + budgetId +
    						"&mode=insert&jsTreeID=" + jsTreeID + "&AppendParameters=false";
    			}
    			costURL = costURL.replaceAll("&","&amp;");

    			projectMap.put("costURL", costURL);

    			String qualityURL = qualityURLPrefix + "&objectId=" +XSSUtil.encodeForURL(context, (String)projectMap.get(projectSpace.SELECT_ID))+"&suiteKey=ProgramCentral";
    			qualityURL = qualityURL.replaceAll("&","&amp;");

    			String financeURL1 = "../common/emxPortal.jsp?portal=PMCProjectBudgetPortal&suiteKey=ProgramCentral&header=emxProgramCentral.ObjectPortal.Header&HelpMarker=emxhelpprojectpowerview&showPageHeader=false&objectId="+projId;    
    			String financeURL2 = "../common/emxPortal.jsp?portal=PMCProjectFinancialPortal&showPageHeader=false&objectId=" + projId;

    			financeURL1 = financeURL1.replaceAll("&","&amp;");
    			financeURL2 = financeURL2.replaceAll("&","&amp;");

    			projectMap.put("qualityURL", qualityURL);
    			projectMap.put("financeURL1", financeURL1);
    			projectMap.put("financeURL2", financeURL2);
    			projectMap.put("nextURL", nextURL);

    			//Project relationship with template
    			//get the projTemplateName and Id add it in projectMap
    			//next time on click when control comes then add selected template name in projectMap...        
    			returnList.add(projectMap);
    		}       
    	}catch(Exception ex){
    		ex.printStackTrace();
    		throw ex;
    	}
    	return returnList;
    }
    
    
    /**
     * 
     * @param context
     * @param dashboardName
     * @param objectSelects
     * @return
     * @throws FrameworkException
     */
    public MapList getDashboardList( Context context, String dashboardName, StringList objectSelects )
    throws FrameworkException
    {
    	MapList maplist = new MapList();
    	matrix.db.Set s = getSet( context, ".dashboard-" + dashboardName );
    	if ( s != null )
    	{
    		try
    		{
    			BusinessObjectWithSelectList bowsl = s.select( context, objectSelects );
    			maplist = FrameworkUtil.toMapList(bowsl);
    		}
    		catch ( MatrixException me )
    		{
    			throw new FrameworkException(""+maplist);
    		}
    	}
    	return maplist;
    }
    
    /**
     * 
     * @param context
     * @param getName
     * @return
     */
    public matrix.db.Set getSet( Context context, String getName )
    {
      try
      {
        SetList l = matrix.db.Set.getSets( context, true );
        for ( int i = 0; i < l.size(); i++ )
        {
          matrix.db.Set s = ( matrix.db.Set )l.getElement( i );
          if ( s.getName().equals( getName ) )
          {
              return s;
          }
        }
      }
      catch ( MatrixException me )
      {
        me.printStackTrace();
        return null;
      }
      return null;
    }
    
    /**
     * 
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.util.Vector
     * @throws Exception java.lang.Exception
     */
    public StringList getCurrentPhase(Context context, String[] args) 
    throws Exception
    {       
    	HashMap programMap          = (HashMap) JPO.unpackArgs(args);       
    	MapList objectList          = (MapList) programMap.get("objectList");
    	Map paramList         = (Map) programMap.get("paramList");        
    	String reportFormat = (String) paramList.get("reportFormat");
    	StringList currentPhaseList = new StringList(objectList.size());
    	StringBuffer sbOutput = null;
    	Map relBusObjMap      = null;
    	String currentPhase = "";
    	boolean bCSVReportFormat = (reportFormat!=null && "CSV".equalsIgnoreCase(reportFormat));
    	for (int i = 0; i < objectList.size(); i++)
    	{
    		relBusObjMap = (Map) objectList.get(i);            
    		sbOutput = new StringBuffer(30);
    		//Added for special character.
    		//currentPhase	= XSSUtil.encodeForHTML(context,(String) relBusObjMap.get("currentPhase"));
    		if(bCSVReportFormat){
    			currentPhase	= XSSUtil.encodeForHTML(context,(String) relBusObjMap.get("currentPhase"));
    			currentPhase = currentPhase.replaceAll("&amp;", "&");
    			currentPhaseList.add(currentPhase);
    		}
    		else{
    			currentPhase	= XSSUtil.encodeForXML(context,(String) relBusObjMap.get("currentPhase"));
    			currentPhase = currentPhase.replaceAll("&amp;", "&");
    			sbOutput.append("<a href ='").append((String) relBusObjMap.get("nextURL1")).append("'>");
    			//Added for special character.
    			sbOutput.append("<b>"+currentPhase+"</b></a>");           
    			currentPhaseList.add(sbOutput.toString());
    			//currentPhaseList.add(currentPhase.toString());
    		}       
    	}
    	return currentPhaseList;  
    }
    
    
    /**
     * 
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.util.Vector
     * @throws Exception java.lang.Exception
     */
    public StringList getCurrentPhaseDate(Context context, String[] args) 
    throws Exception
    {      
    	HashMap programMap         = (HashMap) JPO.unpackArgs(args);       
    	MapList objectList         = (MapList) programMap.get("objectList");
    	StringList currentDateList = new StringList(objectList.size());
    	Map relBusObjMap           = null;
    	for (int i = 0; i < objectList.size(); i++)
    	{
    		relBusObjMap = (Map) objectList.get(i);            

    		currentDateList.addElement((String) relBusObjMap.get("CurrentPhaseDate"));
    	}       
    	return currentDateList;  
    }
    
 
   
    /**
     * 
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.util.Vector
     * @throws Exception java.lang.Exception
     */
    public StringList getRisks(Context context, String[] args) 
    throws Exception
    {        
    	HashMap programMap    = (HashMap) JPO.unpackArgs(args);
    	MapList objectList    = (MapList) programMap.get("objectList");
    	Map paramList         = (Map) programMap.get("paramList");        
    	String reportFormat = (String) paramList.get("reportFormat");
    	StringList risksList = new StringList(objectList.size());

    	StringBuffer sbOutput = null;
    	Map relBusObjMap      = null;
    	boolean bCSVReportFormat = true;
    	bCSVReportFormat = (reportFormat!=null && "CSV".equalsIgnoreCase(reportFormat));
    	for (int i = 0; i < objectList.size(); i++)
    	{
    		sbOutput = new StringBuffer(30);
    		relBusObjMap = (Map) objectList.get(i); 
    		//Added:09-June-2010:ak4:R210:PRG:Bug:054680
    		String riskName =XSSUtil.encodeForHTML(context,(String) relBusObjMap.get("displayRisks"));
    		if(bCSVReportFormat){
    			risksList.add(riskName);
    		}else{
    			sbOutput.append("<input type =\"hidden\" name=\'" + riskName + "\'/>");
    			//End:09-June-2010:ak4:R210:PRG:Bug:054680
    			sbOutput.append("<a href=\"javascript:showModalDialog('" + (String) relBusObjMap.get("newURL1"));         
    			sbOutput.append("', '600', '600')\"  class='object'>");
    			sbOutput.append("<font color='"+(String) relBusObjMap.get("riskCol")+"'><b>");
    			sbOutput.append((String) relBusObjMap.get("displayRisks"));
    			sbOutput.append("</b></font></a>");
    			risksList.add(sbOutput.toString());
    		}       
    	}       
    	return risksList;      
    }
    
    /**
     * 
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.util.Vector
     * @throws Exception java.lang.Exception
     */
    public StringList getQualityURL(Context context, String[] args) 
    throws Exception
    {        
    	HashMap programMap        = (HashMap) JPO.unpackArgs(args);
    	MapList objectList        = (MapList) programMap.get("objectList");
    	Map paramList         = (Map) programMap.get("paramList");        
    	String reportFormat = (String) paramList.get("reportFormat");
    	StringList qualityUrlList = new StringList(objectList.size());
    	StringBuffer sbOutput     = null;
    	Map relBusObjMap          = null;
    	boolean bCSVReportFormat = reportFormat!=null && "CSV".equalsIgnoreCase(reportFormat);
    	for (int i = 0; i < objectList.size(); i++)
    	{
    		sbOutput     = new StringBuffer(30);
    		relBusObjMap = (Map) objectList.get(i);
    		if(bCSVReportFormat){
    			qualityUrlList.add(ProgramCentralConstants.EMPTY_STRING); 
    		}
    		else{
    			sbOutput.append("<a href=\"javascript:showModalDialog('" + (String) relBusObjMap.get("qualityURL")+"&amp;StringResourceFileId=emxProgramCentralStringResource");         
    			sbOutput.append("', '600', '600', 'false', 'content','')\"  class='object'>");
    			sbOutput.append("<img src=\"../common/images/iconQualityMeasure.gif\" border=\"0\" />");
    			sbOutput.append("</a>");

    			qualityUrlList.add(sbOutput.toString());
    		}       
    	}       
    	return qualityUrlList;      
    }
    
    /**
     * 
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.util.Vector
     * @throws Exception java.lang.Exception
     */
    public StringList getSlipDays(Context context, String[] args) 
    throws Exception
    {       
    	HashMap programMap        = (HashMap) JPO.unpackArgs(args);
    	MapList objectList        = (MapList) programMap.get("objectList");
    	StringList slipDaysList   = new StringList(objectList.size());
    	Map paramList         = (Map) programMap.get("paramList");        
    	String reportFormat = (String) paramList.get("reportFormat");
    	StringBuffer buff = null;

    	Map relBusObjMap          = null;
    	for (int i = 0; i < objectList.size(); i++)
    	{
    		buff = new StringBuffer();
    		relBusObjMap = (Map) objectList.get(i);
    		String slipDays=(String)relBusObjMap.get("slipDays");
    		if(reportFormat!=null && "CSV".equalsIgnoreCase(reportFormat)){
    			slipDaysList.add(XSSUtil.encodeForHTML(context,slipDays));
    		}
    		else{
    			buff.append("<font color='"+(String) relBusObjMap.get("col")+"'><b>")
    			.append(XSSUtil.encodeForHTML(context,(String) relBusObjMap.get("slipDays")))
    			.append("</b></font>");          
    			slipDaysList.add(buff.toString());       
    		}       
    	}       
    	return slipDaysList;      
    }
    
   /**
    * 
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.util.Vector
     * @throws Exception java.lang.Exception
    */
    public StringList getAssessmentURL(Context context, String[] args) 
    throws Exception
    {       
    	HashMap programMap        = (HashMap) JPO.unpackArgs(args);
    	MapList objectList        = (MapList) programMap.get("objectList");
    	Map paramList         = (Map) programMap.get("paramList");        
    	String reportFormat = (String) paramList.get("reportFormat");
    	StringList assesmentURLList = new StringList(objectList.size());

    	StringBuffer sbOutput     = null;
    	Map relBusObjMap          = null;
        String status             = DomainConstants.EMPTY_STRING;

    	String red    = "Red";
    	String green  = "Green";
    	String yellow = "Yellow";
    	String other  = "---";
    	boolean bCSVReportFormat = reportFormat!=null && "CSV".equalsIgnoreCase(reportFormat);

    	for (int i = 0; i < objectList.size(); i++)
    	{
    		sbOutput     = new StringBuffer(30);
    		relBusObjMap = (Map) objectList.get(i);
    		status       = XSSUtil.encodeForHTML(context,(String) relBusObjMap.get("status"));
    		if(bCSVReportFormat){
    			assesmentURLList.add(status);
    		}else{
    			sbOutput.append("<a href=\"javascript:showModalDialog('" + (String) relBusObjMap.get("assessmentURL"));         
    			sbOutput.append("', '600', '600')\"  class='object'>");
    			if (red.equalsIgnoreCase(status))
    			{              
    				sbOutput.append("<img src=\"../common/images/iconStatusRed.gif\" border=\"0\" />");
    			}
    			else if (green.equalsIgnoreCase(status))
    			{
    				sbOutput.append("<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\" />");                
    			}
    			else if (yellow.equalsIgnoreCase(status))
    			{
    				sbOutput.append("<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\" />");                
    			}
    			if (other.equalsIgnoreCase(status))
    			{
    				sbOutput.append("---");
    			}      
    			sbOutput.append("</a>");

    			assesmentURLList.add(sbOutput.toString());
    		}       
    	}
    	return assesmentURLList;      
    }
    
    /**
     * 
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.util.Vector
     * @throws Exception java.lang.Exception
     */
    public StringList getCostRatio(Context context, String[] args) 
    throws Exception
    {        
    	HashMap programMap        = (HashMap) JPO.unpackArgs(args);
    	MapList objectList        = (MapList) programMap.get("objectList");
    	Map paramList         = (Map) programMap.get("paramList");        
    	String reportFormat = (String) paramList.get("reportFormat");
    	StringList costRatioList  = new StringList(objectList.size());
    	Map relBusObjMap          = null;
    	// Start : 372640
    	StringBuffer sbOutput     = null;
    	boolean bCSVReportFromat = (reportFormat!=null && "CSV".equalsIgnoreCase(reportFormat));
    	for (int i = 0; i < objectList.size(); i++)
    	{
    		relBusObjMap = (Map) objectList.get(i);
    		sbOutput = new StringBuffer(30);
    		String costRatio=XSSUtil.encodeForHTML(context, (String) relBusObjMap.get("costRatio"));
    		if(bCSVReportFromat){
    			costRatioList.add(costRatio);
    		}
    		else{
    			
    			sbOutput.append("<font color='"+(String) relBusObjMap.get("costColor")+"'><b>")
    			.append(XSSUtil.encodeForHTML(context,(String) relBusObjMap.get("costRatio")))
    			.append("</b></font>");          
    			costRatioList.add(sbOutput.toString());       
    			/*sbOutput.append("<a href=\"javascript:showModalDialog('" + (String) relBusObjMap.get("costURL"));
    			sbOutput.append("', '600', '600')\"  class='object'>");
    			sbOutput.append("<font color='"+(String) relBusObjMap.get("costColor")+"'><b>");
    			sbOutput.append((String) (String) relBusObjMap.get("costRatio"));
    			sbOutput.append("</b></font></a>");
    			costRatioList.add(sbOutput.toString());*/

    			// buff = new StringBuffer();
    			// buff.append("<font color='"+(String) relBusObjMap.get("costColor")+"'><b>")
    			//.append((String) relBusObjMap.get("costRatio"))
    			//.append("</b></font>");
    			//costRatioList.add(buff.toString());
    			// End : 372640
    		}       
    	}
    	return costRatioList;      
    }
    
    /**
     * 
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.util.Vector
     * @throws Exception java.lang.Exception
     */
    public StringList getTotalCost(Context context, String[] args) 
    throws Exception
    {        
    	HashMap programMap        = (HashMap) JPO.unpackArgs(args);
    	MapList objectList        = (MapList) programMap.get("objectList");
    	Map paramList         = (Map) programMap.get("paramList");        
    	String reportFormat = (String) paramList.get("reportFormat");
    	StringList totalCostList  = new StringList(objectList.size());
    	// Start : 372640
    	StringBuffer sbOutput     = null;
    	Map relBusObjMap          = null;
    	boolean bCSVReportFromat = (reportFormat!=null && "CSV".equalsIgnoreCase(reportFormat));

    	for (int i = 0; i < objectList.size(); i++)
    	{
    		relBusObjMap = (Map) objectList.get(i);
    		sbOutput = new StringBuffer(30);
    		String totalCost=(String) relBusObjMap.get("totalCost");
    		if(bCSVReportFromat){
    			totalCostList.add(XSSUtil.encodeForHTML(context,totalCost));
    		}
    		else{
    			sbOutput.append("<a href=\"javascript:showModalDialog('" + (String) relBusObjMap.get("financeURL1"));
    			sbOutput.append("', '600', '600')\"  class='object'>");
    			sbOutput.append("<b>"+XSSUtil.encodeForHTML(context,(String) relBusObjMap.get("totalCost"))+"</b></a>");                    
    			totalCostList.add(sbOutput.toString());
    			//totalCostList.add((String) relBusObjMap.get("totalCost"));
    			// End : 372640
    		}       
    	}
    	return totalCostList;      
    }
    
    /**
     * 
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.util.Vector
     * @throws Exception java.lang.Exception
     */
    public StringList getTotalBenefit(Context context, String[] args) 
    throws Exception
    {        
    	HashMap programMap          = (HashMap) JPO.unpackArgs(args);
    	MapList objectList          = (MapList) programMap.get("objectList");
    	Map paramList         = (Map) programMap.get("paramList");        
    	String reportFormat = (String) paramList.get("reportFormat");
    	StringList totalBenefitList = new StringList(objectList.size());
    	// Start : 372640
    	StringBuffer sbOutput       = null;
    	Map relBusObjMap            = null;
    	String sLink = DomainObject.EMPTY_STRING;
    	boolean bCSVReportFormat = (reportFormat!=null && "CSV".equalsIgnoreCase(reportFormat));
    	for (int i = 0; i < objectList.size(); i++)
    	{
    		sbOutput     = new StringBuffer(30);
    		relBusObjMap = (Map) objectList.get(i);          
    		String projectId = (String)relBusObjMap.get("projectId");
    		sLink = "../common/emxIndentedTable.jsp?table=PMCProjectBenefitSummaryTable&amp;toolbar=PMCProjectBenefitFilter&amp;freezePane=Name&amp;selection=none&amp;expandProgram=emxBenefitItem:getTableExpandChildProjectBenefitData&amp;editLink=false&amp;showTabHeader=false&amp;emxExpandFilter=3&amp;customize=false&amp;HelpMarker= emxhelpfinancialitemsummary&amp;objectId=" +XSSUtil.encodeForURL(context, projectId);
    		String totalBenefit=XSSUtil.encodeForHTML(context,(String)relBusObjMap.get("totalBenefit"));
    		if(bCSVReportFormat){
    			totalBenefitList.add(totalBenefit);
    		}
    		else{
    			sbOutput.append("<a href=\"javascript:showModalDialog('" + sLink);
    			sbOutput.append("', '600', '600')\"  class='object'>");
    			sbOutput.append("<b>"+XSSUtil.encodeForHTML(context,(String) relBusObjMap.get("totalBenefit"))+"</b></a>");           
    			totalBenefitList.add(sbOutput.toString());
    			//sbOutput.append((String) relBusObjMap.get("totalBenefit"));
    			// End : 372640
    		} 
    	}
    	return totalBenefitList;      
    }
    
    /**
     * 
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.util.Vector
     * @throws Exception java.lang.Exception
     */
    public StringList getMetrics(Context context, String[] args) 
    throws Exception
    {    
        HashMap programMap        = (HashMap) JPO.unpackArgs(args);
        MapList objectList        = (MapList) programMap.get("objectList");
        Map paramList         = (Map) programMap.get("paramList");        
        String reportFormat = (String) paramList.get("reportFormat");
        StringList metricUrlList  = new StringList(objectList.size());
        StringBuffer sbOutput     = null;
        Map relBusObjMap          = null;      
        for (int i = 0; i < objectList.size(); i++)
        {
            sbOutput     = new StringBuffer(30);
            relBusObjMap = (Map) objectList.get(i);
            if(reportFormat!=null && "CSV".equalsIgnoreCase(reportFormat)){
         	   metricUrlList.add(ProgramCentralConstants.EMPTY_STRING); 
              }
            else{
            sbOutput.append("<a href=\"javascript:showModalDialog('" + (String) relBusObjMap.get("newURL2"));         
            sbOutput.append("', '600', '600', 'true', 'content','')\"  class='object'>");
            sbOutput.append("<img src=\"../common/images/iconMetricMeasure.gif\" border=\"0\" />");
            sbOutput.append("</a>");
            
            metricUrlList.add(sbOutput.toString());
        }      
       }     
      return metricUrlList;      
    }
    
    /**
     * 
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.util.Vector
     * @throws Exception java.lang.Exception
     */
    public StringList getBusinessUnit(Context context, String[] args) 
    throws Exception
    {        
        HashMap programMap           = (HashMap) JPO.unpackArgs(args);
        MapList objectList           = (MapList) programMap.get("objectList");
        StringList businessUnitList  = new StringList(objectList.size());
        StringBuffer sbOutput        = null;
        Map relBusObjMap             = null;
        String strBUnit              = null;
        for (int i = 0; i < objectList.size(); i++)
        {
            sbOutput     = new StringBuffer(30);
            relBusObjMap = (Map) objectList.get(i);
            strBUnit     = (String) relBusObjMap.get(ProjectSpace.SELECT_BUSINESS_UNIT_NAME);
            if(strBUnit!=null && !"null".equals(strBUnit))
            {
                sbOutput.append(strBUnit);   
            }
            else
            {
                sbOutput.append("");    
            }
            businessUnitList.add(sbOutput.toString());
        }       
     
       return businessUnitList;      
    }
    
    /**
     * 
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.util.Vector
     * @throws Exception java.lang.Exception
     */
    public StringList getProjectName(Context context, String[] args) 
    throws Exception
    {         
    	HashMap programMap         = (HashMap) JPO.unpackArgs(args);
    	MapList objectList         = (MapList) programMap.get("objectList");
    	Map paramList         = (Map) programMap.get("paramList");        
    	String reportFormat = (String) paramList.get("reportFormat");
    	StringList projNameUrlList = new StringList(objectList.size());
    	StringBuffer sbOutput      = null;
    	Map relBusObjMap           = null;
    	boolean bCSVReportFormat = reportFormat!=null && "CSV".equalsIgnoreCase(reportFormat);

    	for (int i = 0; i < objectList.size(); i++)
    	{
    		sbOutput     = new StringBuffer(30);
    		relBusObjMap = (Map) objectList.get(i);
    		//Added:09-June-2010:ak4:R210:PRG:Bug:054680
    		String pSpaceName =(String) relBusObjMap.get(ProjectSpace.SELECT_NAME);
    		if(bCSVReportFormat){
    			projNameUrlList.add(pSpaceName);
    		}
    		else{
    			//Added for special character.
    			pSpaceName	=	XSSUtil.encodeForXML(context, pSpaceName);
    			sbOutput.append("<input type =\"hidden\" name=\'" + pSpaceName + "\'/>");
    			sbOutput.append("<a href='").append((String) relBusObjMap.get("nextURL")).append("'>");         
    			sbOutput.append("<img src=\"../common/images/iconSmallProject.gif\" border=\"0\" />");
    			sbOutput.append(pSpaceName);
    			sbOutput.append("</a>");

    			projNameUrlList.add(sbOutput.toString());
    		}   
    	}      
    	return projNameUrlList;      
    }
    
   
       
    /**
     * 
     * @param context
     * @return
     * @throws Exception
     */
    public static double getTimezone(Context context)throws Exception
    {
        int iRawOffset=0;
        double dOffset = 0.0;
    try{
        //getting timezone from context
        String sTimezone = context.getTimezone();
        //getting offset from timezone
        iRawOffset = (TimeZone.getTimeZone(sTimezone)).getRawOffset();
        if(iRawOffset >0)
            iRawOffset = 0-iRawOffset;
        else if(iRawOffset < 0)
            iRawOffset = 0-iRawOffset;
        //converting it from millisec to hrs.
        dOffset = iRawOffset/3600000D ;
    }catch(Exception e){System.out.println(" exception in getTimezone method in MBOMutil "+e);
        dOffset = 0.0;
    }
    return dOffset;

   }
    
    /**
     * 
     * @param strDate
     * @param locale
     * @param timeZone
     * @return
     * @throws Exception
     */
    public static String getUIDateFromEmxDate(String strDate, Locale locale,String timeZone)
    throws Exception
    {
        eMatrixDateFormat.setEMatrixDateFormat((Context)null);
       
        double timeZoneOffset= Task.parseToDouble(timeZone);
        return eMatrixDateFormat.getFormattedDisplayDate(strDate, timeZoneOffset, locale);
        
    }
    
    /**
     * 
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.util.Vector
     * @throws Exception java.lang.Exception
     */
    public StringList getNewWindow(Context context, String[] args) 
    throws Exception
    {        
        HashMap programMap        = (HashMap) JPO.unpackArgs(args);
        MapList objectList        = (MapList) programMap.get("objectList");
        StringList newWindowList  = new StringList(objectList.size());
        StringBuffer sbOutput     = null;
        Map relBusObjMap          = null;
        for (int i = 0; i < objectList.size(); i++)
        {
            sbOutput = new StringBuffer(30);
            relBusObjMap = (Map) objectList.get(i);            
           
                sbOutput.append("<a href=\"javascript:showDetailsPopup('" + (String) relBusObjMap.get("nextURL"));         
                    sbOutput.append("', '600', '600', 'false', 'content','')\"  class='object'>");
                    sbOutput.append("<img src=\"../common/images/iconNewWindow.gif\" border=\"0\" />");
                sbOutput.append("</a>");
            newWindowList.add(sbOutput.toString());
        }       
       return newWindowList;      
    }
   
    /**
     * 
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.util.Vector
     * @throws Exception java.lang.Exception
     */
    public StringList getNewWindowForDashboard(Context context, String[] args) 
    throws Exception
    {        
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        Map paramList               = (Map) programMap.get("paramList");
        MapList relBusObjPageList   = (MapList) programMap.get("objectList");
        StringList newWindowList    = new StringList(relBusObjPageList.size());
        StringBuffer strURL         = new StringBuffer(50);
        strURL.append("../common/emxIndentedTable.jsp?")
                      .append("toolbar=PMCDashboardToolbarMenu&")
                      .append("program=emxTemplateDrivenDashboard:getRelatedProjects&")
                      .append("table=PMCDashboardDetailsTable&")                 
                      .append("header=emxProgramCentral.ProgramTop.DashboardDetails&")
                      .append("selection=multiple&")
                      .append("suiteKey=ProgramCentral&HelpMarker=emxhelpdashboardsdetails&")
                      .append("isFromDashboard=true&")
                      .append("showRMB=false&")                      
                      .append("dashboardName=");
        
      
        boolean isPrinterFriendly = false;
        String strPrinterFriendly = (String)paramList.get("reportFormat");
        if ( strPrinterFriendly != null ) {
            isPrinterFriendly = true;
        }
       
        try
        {
            Map relBusObjMap      = null;
            String strName        = null;
            StringBuffer sbOutput = null;
            for (int i = 0; i < relBusObjPageList.size(); i++)
            {
                relBusObjMap = (Map) relBusObjPageList.get(i);
                strName      = (String) relBusObjMap.get("id");
                sbOutput     = new StringBuffer();
                if(!isPrinterFriendly)
                {
                    sbOutput.append("<a href=\"javascript:showDetailsPopup('" + strURL.toString()+strName);         
                        sbOutput.append("', '600', '600', 'false', 'content','')\"  class='object'>");
                        sbOutput.append("<img src=\"../common/images/iconNewWindow.gif\" border=\"0\" />");
                    sbOutput.append("</a>");
                }
                
                newWindowList.addElement(sbOutput.toString());              
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
       
            return newWindowList;
           
    }
    
    /**
     * This Access function is requaird to check if Template driven dashboard WBS tasks needs to be shown in the table. If Template from combo box 
     * is selected then this function returns false meaning hide all other columns except dynamically added WBS Tasks. And function returns true if all the columns nedds to be shown.
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.util.Vector
     * @throws Exception java.lang.Exception
     */
    public boolean isShowColumn(Context context, String[] args) throws Exception {
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      boolean access = true;
    
      String selectedTemplateId = (String)programMap.get(STR_TEMPLATE_CMD_NAME);
      String defaultTempValue = EnoviaResourceBundle.getProperty(context,
										ProgramCentralConstants.PROGRAMCENTRAL,
										SELECT_TEMPLATE_DEFAULTNAME, "en");
      
      if((selectedTemplateId != null && !defaultTempValue.equals(selectedTemplateId))) {
        access = false;
      }      
      
      return access;
  }
    
    
   
    /**
     * This Access Function is called to check if Actions menu has to be shown on Dashboard details page or no. 
     * On Dashborads command from Projects/Program Actions menu is not requaired.   
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return java.util.Vector
     * @throws Exception java.lang.Exception
     */
   public boolean isShowActionsMenu(Context context, String[] args)
    throws Exception
    {
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
    //  MapList objectList         = (MapList) programMap.get("objectList");
      String isShowActionsMenu = (String) programMap.get("showActionsMenu");   
      boolean access = true;
      if(isShowActionsMenu!=null && isShowActionsMenu.equals("false"))
      {
        access = false;
      }    
      return access;
    }
    
 
    /**
     * 
     * @param context
     * @param inputType
     * @return
     * @throws FrameworkException
     */
    private static Set getListOfSubTypes(Context context,
            String inputType) throws FrameworkException {
        Set setValues = new HashSet();
                
      //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:start
        String sCommandStatement = "print type $1 select $2 dump $3";
        String result  =  MqlUtil.mqlCommand(context, sCommandStatement,inputType,"derivative", "|"); 
      //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:End
        
        if(result != null && !"".equals(result)){
            StringList slDerivatives = FrameworkUtil.split(result, "|");
            String eachSubType = null;
            Set setItsSubTypes = null;
            for(int i=0;i<slDerivatives.size();i++){
                eachSubType = (String) slDerivatives.get(i);
                setItsSubTypes = getListOfSubTypes(context, eachSubType);
                if(setItsSubTypes.size() > 0){
                    setValues.addAll(setItsSubTypes);
                } else {
                    setValues.add(eachSubType);
                }
            }
        } else {
//          System.out.println("---comehere--->");
            setValues.add(inputType);
        }
//      System.out.println("---setValues--->"+setValues);
        return setValues;
    }
    
    //Logic related to Template Driven Dashboards - starts
    
    /**
     * This function returns the Maplist of dynamic column details.
     * @param context matrix.db.Context
     * @param args java.lang.String
     * @return com.matrixone.apps.domain.util.MapList 
     * @throws Exception java.lang.Exception
     */
        public static MapList getWBSDynamicColumns(Context context, String[] args) throws Exception
        {
        	/** 
        	 * Table "PMCDashboardDetailsTable" defines a column for dynamic columns after selection of Template name from the combobox provided on the toolbar "PMCDashboardToolbarMenu"
        	 * This function gets all the related objects as per Include recursion level mentioned(if Include Level checkbox is selected on the UI) in the command "PMCIncludeLevelCommand" and
        	 * dynamic columns are created based on Template WBS tasks( If Include level is selected then header is given as immediate parent level WBS Task)
        	 * Values for column are shown if Template WBS Task name, type, level and Project WBS Task name, type, level matches.
        	 **/
        	HashMap programMap = (HashMap) JPO.unpackArgs(args);
        	HashMap paramMap   = (HashMap) programMap.get("requestMap");        

        	String selectedTemplateId        = (String)paramMap.get(STR_TEMPLATE_CMD_NAME);
        	String isIncludeLevelToSelected  = DomainConstants.EMPTY_STRING;
        	String strShowWBSTasks           = DomainConstants.EMPTY_STRING;

        	if(selectedTemplateId != null && !"null".equals(selectedTemplateId))
        	{
        		isIncludeLevelToSelected     = (String)paramMap.get(STR_INCLUDELEVEL_CMD_NAME);                
        		strShowWBSTasks              = (String)paramMap.get(STR_WBSTASKS_CMD_NAME);
        	}

        	Set setSubTypesTask = new HashSet();
        	Set selectedSubTypes = getListOfSubTypes(context, DomainConstants.TYPE_TASK_MANAGEMENT);

        	if(strShowWBSTasks!=null && "true".equals(strShowWBSTasks))
        	{                
        		String strWBSTask = (String)paramMap.get("wbsTask");
        		if(strWBSTask!=null)
        		{
        			StringTokenizer strTokens = new StringTokenizer(strWBSTask, "|");
        			String strSymbWBSTaskName = DomainConstants.EMPTY_STRING;
        			String strActWBSTaskName  = DomainConstants.EMPTY_STRING;
        			while ( strTokens.hasMoreTokens() ) {
        				strSymbWBSTaskName = strTokens.nextToken();
        				strActWBSTaskName  = PropertyUtil.getSchemaProperty( context, strSymbWBSTaskName.trim() );

        				if(selectedSubTypes.contains(strActWBSTaskName))
        				{                      
        					setSubTypesTask.add(strActWBSTaskName);
        				}                    
        			}
        		}
        	}
        	else
        	{
        		setSubTypesTask = selectedSubTypes;             
        	}
        	MapList columnMapList = new MapList();
        	String defaultTempValue = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
        			SELECT_TEMPLATE_DEFAULTNAME, "en");

        	if(selectedTemplateId!=null && !"null".equals(selectedTemplateId) && !defaultTempValue.equals(selectedTemplateId) )
        	{
        		Map colMap          = null;
        		Map settingsMap = null;

        		String includeLevel     = (String)paramMap.get("includeLevel");
        		String immdParentLevel  = "1";                
        		int intIncludeLevel     = 0;
        		int intImmdParentLevel  = 1;

        		if(includeLevel!=null && !"null".equals(includeLevel) && !"0".equals(includeLevel))
        		{
        			immdParentLevel     = ((Integer.parseInt(includeLevel)-1))+"";                    
        			intIncludeLevel     = Integer.parseInt(includeLevel);
        			intImmdParentLevel  = Integer.parseInt(immdParentLevel);
        		}

        		MapList listOfTasks              = getWBSTasks(context,selectedTemplateId,(short)intImmdParentLevel);
        		Iterator taskIterator            = listOfTasks.iterator();
        		Map taskMap                      = null;
        		String taskId                    = DomainConstants.EMPTY_STRING;
        		String taskName                  = DomainConstants.EMPTY_STRING;
        		String taskType                  = DomainConstants.EMPTY_STRING;
        		String taskTypeForChildWBS       = DomainConstants.EMPTY_STRING;
        		MapList listOfSubTasks           = null;
        		Iterator taskIteratorForChildWBS = null;
        		boolean showHeader               = false;                
        		StringList templateWBSTasksList  = new StringList();     
        		Map taskMapForTemplate           = null;
        		String taskIdFromTemplate        = DomainConstants.EMPTY_STRING;
        		MapList subTaskList              = null;
        		Iterator subtaskIter             = null;
        		while(taskIterator.hasNext())
        		{          
        			taskMapForTemplate = (Map) taskIterator.next();    
        			//When include level is not selected, intImmdParentLevel will be equal to 1 and 
        			// following listing of objects for level 1 should happen so added this condition "intImmdParentLevel==1"
        			// And when include level is selected then we need to show only selected level objects hence, comapred with current level 
        			if(intImmdParentLevel==1 || immdParentLevel.equals(taskMapForTemplate.get(DomainConstants.SELECT_LEVEL)))
        			{
        				taskIdFromTemplate = (String)taskMapForTemplate.get(DomainConstants.SELECT_ID);

        				templateWBSTasksList.add(taskMapForTemplate.get(DomainConstants.SELECT_NAME));

        				subTaskList  = getWBSTasks(context,taskIdFromTemplate,(short)1);//each level Immediate child list 
        				subtaskIter  = subTaskList.iterator();
        				if(subtaskIter.hasNext())
        				{
        					taskMapForTemplate = (Map) subtaskIter.next();                          
        					templateWBSTasksList.add(taskMapForTemplate.get(DomainConstants.SELECT_NAME));                           
        				}
        			}
        		}
        		listOfTasks.sort(ProgramCentralConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER, "ascending", "integer");
        		taskIterator    = listOfTasks.iterator();
        		boolean hasType = false;
        		while(taskIterator.hasNext())
        		{          
        			taskMap    = (Map) taskIterator.next();                      
        			taskType   = (String)taskMap.get(DomainConstants.SELECT_TYPE); 

        			taskId      = (String)taskMap.get(DomainConstants.SELECT_ID);
        			hasType = false;                     
        			if((isIncludeLevelToSelected!=null && "true".equals(isIncludeLevelToSelected)) && strShowWBSTasks!=null && "true".equals(strShowWBSTasks)){
        				if(!setSubTypesTask.contains(taskType)){
        					listOfSubTasks = (MapList) getWBSTasks(context,taskId,(short)1);//iterator for each child
        					if(listOfSubTasks!=null)
        					{
        						taskIteratorForChildWBS = listOfSubTasks.iterator();
        						while(taskIteratorForChildWBS.hasNext())
        						{
        							Map taskMapForSubTask = (Map) taskIteratorForChildWBS.next();
        							taskTypeForChildWBS   = (String)taskMapForSubTask.get(DomainConstants.SELECT_TYPE);

        							if(setSubTypesTask.contains(taskTypeForChildWBS))
        							{
        								hasType = true;
        								break;
        							}
        						}
        					}
        				}
        			}
        			if(setSubTypesTask.contains(taskType)|| (hasType))
        			{                     
        				showHeader  = false;
        				taskName    = (String)taskMap.get(DomainConstants.SELECT_NAME);
        				taskId      = (String)taskMap.get(DomainConstants.SELECT_ID);  
        				colMap      = new HashMap();
        				settingsMap = new HashMap();                     

        				//When include level is not selected, intImmdParentLevel will be equal to 1 and 
        				// following listing of objects for level 1 should happen so added this condition "intImmdParentLevel==1"
        				// And when include level is selected then we need to show only selected level objects hence, comapred with current level                        
        				if((isIncludeLevelToSelected!=null && "true".equals(isIncludeLevelToSelected))
        						&&  (intImmdParentLevel==1 || immdParentLevel.equals(taskMap.get(DomainConstants.SELECT_LEVEL))))
        				{
        					listOfSubTasks = (MapList) getWBSTasks(context,taskId,(short)1);//iterator for each child
        					if(listOfSubTasks!=null && listOfSubTasks.size()>0)
        					{
        						settingsMap.put("Group Header",taskName);                                
        						showHeader = true;
        					}
        				}
        				String dynamicColumnWidth ="";
        				try {
        					dynamicColumnWidth = EnoviaResourceBundle.getProperty(context, "ProgramCentral.Dashboards.DynamicColmnWidth") ;
        				} catch(Exception ex) {
        					dynamicColumnWidth = "10";
        				}
        				if(!showHeader && setSubTypesTask.contains(taskType) && (intImmdParentLevel==1 || immdParentLevel.equals(taskMap.get(DomainConstants.SELECT_LEVEL)) ))
        				{
        					//Set information of Column Settings in settingsMap
        					settingsMap.put("Column Type","programHTMLOutput");
        					settingsMap.put("program","emxTemplateDrivenDashboard");

        					settingsMap.put("function","getColumnValues");
        					settingsMap.put("Width", dynamicColumnWidth); 
        					settingsMap.put("Export", "true");
        					settingsMap.put("Registered Suite", "ProgramCentral");
        					colMap.put("name", "DynamicColumn" );
        					colMap.put("label", taskName);                        
        					colMap.put("expression_businessobject", "empty" );
        					colMap.put("settings",(settingsMap));
        					colMap.put("customSetting_TaskId", taskId);
        					colMap.put("customSetting_taskType", (setSubTypesTask));
        					colMap.put("customSetting_ColumnTaskName", taskName);
        					colMap.put("customSetting_CustomLevel", "0");//0 indicates Include Level is not selected 
        					colMap.put("customSetting_ParentTask", new HashMap());
        					colMap.put("customSetting_TemplateWBSTasks", templateWBSTasksList);
        					columnMapList.add(colMap);                             
        				}
        				else
        				{
        					if(listOfSubTasks!=null)
        					{
        						taskIteratorForChildWBS = listOfSubTasks.iterator();
        						while(taskIteratorForChildWBS.hasNext())
        						{                                  
        							Map taskMapForSubTask = (Map) taskIteratorForChildWBS.next();
        							taskTypeForChildWBS   = (String)taskMapForSubTask.get(DomainConstants.SELECT_TYPE);
        							if(setSubTypesTask.contains(taskTypeForChildWBS))
        							{                               
        								colMap      = new HashMap();
        								settingsMap = new HashMap();

        								settingsMap.put("Column Type","programHTMLOutput");
        								settingsMap.put("program","emxTemplateDrivenDashboard");
        								settingsMap.put("function","getColumnValues");     
        								settingsMap.put("Export", "true");
        								settingsMap.put("Registered Suite","ProgramCentral");    
        								settingsMap.put("Width", dynamicColumnWidth);  
        								settingsMap.put("Group Header",taskName);
        								colMap.put("name", "DynamicColumn" );                                  
        								colMap.put("label", taskMapForSubTask.get(DomainConstants.SELECT_NAME));                                 
        								colMap.put("expression_businessobject", "empty" );                         
        								colMap.put("settings",(settingsMap));

        								colMap.put("customSetting_taskId", taskMapForSubTask.get(DomainConstants.SELECT_ID));
        								colMap.put("customSetting_taskType", (setSubTypesTask));
        								colMap.put("customSetting_ColumnTaskName", taskMapForSubTask.get(DomainConstants.SELECT_NAME));
        								colMap.put("customSetting_CustomLevel", includeLevel);
        								colMap.put("customSetting_ParentTask", (taskMap));
        								colMap.put("customSetting_TemplateWBSTasks", templateWBSTasksList);
        								columnMapList.add(colMap);
        							}

        						}
        					}
        				}
        			}
        		}        
        	}      
        	return columnMapList;
        }
        
        /**
         * This functions gets called for Dynamic column values.It returns the dynamic column values.
         * @param context matrix.db.Context
         * @param args java.lang.String
         * @return matrix.util.StringList
         * @throws Exception java.lang.Exception
         */
        public StringList getColumnValues(Context context, String[] args) 
        throws Exception
        {
            StringList projNameUrlList = new StringList();
         
            HashMap programMap         = (HashMap) JPO.unpackArgs(args);
            Map paramList              = (Map) programMap.get("paramList");
            Map columnMap              = (Map) programMap.get("columnMap");
            String taskId              = (String)columnMap.get("customSetting_taskId");
            String taskName            = null;  
           
            MapList objectList         = (MapList) programMap.get("objectList");
            int noOfProjects           = objectList.size();
           
            //Get the Template Tasks
            StringList projWBSList    = (StringList)columnMap.get("customSetting_TemplateWBSTasks");      
            HashSet thisColumnType        = (HashSet)columnMap.get("customSetting_taskType");
            String thisColumnName        = (String)columnMap.get("customSetting_ColumnTaskName");
            
            String includeLevel = (String)columnMap.get("customSetting_CustomLevel");            
            String immdParentLevel = "1";                
            int intIncludeLevel     = 0;
            int intImmdParentLevel  = 1;
            
            if(includeLevel!=null && !"null".equals(includeLevel)&& !"0".equals(includeLevel))
            {
                immdParentLevel     = ((Integer.parseInt(includeLevel)-1))+"";                    
                intIncludeLevel     = Integer.parseInt(includeLevel);
                intImmdParentLevel  = Integer.parseInt(immdParentLevel);
            }
            
            
            Map mpParenTask                 = (Map)columnMap.get("customSetting_ParentTask");
            String nameOfParentTask       = null;
            String parentTaskLvl             = null;
	      String parentTaskType        = null; 
            if(mpParenTask != null && mpParenTask.size() > 0){
               nameOfParentTask = (String) mpParenTask.get(DomainConstants.SELECT_NAME);     
	        parentTaskType   = (String) mpParenTask.get(DomainConstants.SELECT_TYPE);   
            }
              
            Map relBusObjMap       = null;
            String projectId       = null; 
            MapList resultMap      = null;
            Map taskMapForProject  = null;
            Map validMap           = null;
            int iCntr              = 0;
            Iterator taskIterator1 = null;
            boolean foundValidPair = false;
            Map taskMapForProject1 = null;
            
            Map eachMap            = null;
            String eachLvl         = null;
            String eachLvlName     = null;
            String eachLvlType     = null;
            String thisLvl         = null;
            String thisTaskName    = null;
            String thisTaskType    = null;
            boolean foundParent    = false;
            String strURL          = null;
            boolean found          = false;
            Iterator taskIterator  = null;
            for (int i = 0; i < noOfProjects; i++)
            {
                relBusObjMap = (Map) objectList.get(i);    
                projectId    = (String)relBusObjMap.get("projectId");     
              
                //Validation while displaying Level 2 objects
                //When Include level 2 is selected, then While constructing level 2 column settings "customSetting_IsLevelTwo" for level 2 objects is set to true.
                //"thisLvl" indicates the current object level and "eachLvl" indicates the objects level in the map.
                //When template WBS task name and type matches with projects WBS task name and type then "foundParent" is set to true. 
                if(intIncludeLevel > 0){                    
                    resultMap = getWBSTasks(context,projectId,(short)intIncludeLevel);
                   
                    validMap       = null;
                    iCntr          = 0;
                    taskIterator1  = resultMap.iterator();
                    foundValidPair = false;                    
                   
                    while(taskIterator1.hasNext())
                    {
                        taskMapForProject1 = (Map) taskIterator1.next();
                        thisLvl            = (String)taskMapForProject1.get(DomainConstants.SELECT_LEVEL);
                        thisTaskName       = (String)taskMapForProject1.get(DomainConstants.SELECT_NAME);
                        thisTaskType       = (String)taskMapForProject1.get(DomainConstants.SELECT_TYPE);
                        
                        if(includeLevel.equals(thisLvl) && thisColumnName.equals(thisTaskName)&& thisColumnType.contains(thisTaskType)){
                            foundParent = false;
                            for(int k=iCntr;k>=0;k--)
                            {
                                eachMap = (Map) resultMap.get(k);
                                eachLvl = (String) eachMap.get(DomainConstants.SELECT_LEVEL);
                                
                                if(immdParentLevel.equals(eachLvl)){
                                    eachLvlName = (String) eachMap.get(DomainConstants.SELECT_NAME);                           
                                    eachLvlType = (String) eachMap.get(DomainConstants.SELECT_TYPE);
                                    
                                    if(eachLvlName.equals(nameOfParentTask) && eachLvlType.equals(parentTaskType)){
                                        foundParent = true;                                    
                                    }
                                    break;    
                                }
                            }
                            if(foundParent){
                                validMap       = taskMapForProject1;
                                foundValidPair = true;
                                break;
                            }
                        }
                        iCntr++;
                    }
                    //This is when multiple same name WBS task subtask. 
                    //When correct parent and child is found,that is when include level 2 is selected then the current child name has tobe shown under respective parent WBS task column.foundValidPair is used for this validation                    
                    if(foundValidPair){
                        strURL = getTaskNameColumn(context, args, validMap);    
                        projNameUrlList.add(strURL);                  
                    } else {
                        projNameUrlList.add("");                  
                    }
                }//Till here 
                //else part is for first level WBS tasks.
                else {                    
                    includeLevel = (String)paramList.get("includeLevel");//This includeLevel is the mentioned Include level in the command href
                    if(includeLevel!=null && !"null".equals(includeLevel)&& !"0".equals(includeLevel) && (Integer.parseInt(includeLevel))>2)
                    {
                        immdParentLevel     = ((Integer.parseInt(includeLevel)-1))+"";               
                        intImmdParentLevel  = Integer.parseInt(immdParentLevel);
                    }
                  
                    resultMap = getWBSTasks(context,projectId,(short)intImmdParentLevel);
                    
                    found = false;
                    taskIterator = resultMap.iterator();
                    while(taskIterator.hasNext())
                    {
                        taskMapForProject = (Map) taskIterator.next();
                        
                        taskId   = (String)taskMapForProject.get(DomainConstants.SELECT_ID);
                        taskName = (String)taskMapForProject.get(DomainConstants.SELECT_NAME);
                        thisTaskType = (String)taskMapForProject.get(DomainConstants.SELECT_TYPE);
                       
                        if(projWBSList.size()>0 && thisColumnName.equals(taskName)&& thisColumnType.contains(thisTaskType))
                        {
                            strURL = getTaskNameColumn(context, args, taskMapForProject);    
                            projNameUrlList.add(strURL);                     
                            found = true;
                            break;
                        }
                    }
                    if(!found){
                        projNameUrlList.add(""); 
                    }                
                }          
           }      
            return projNameUrlList;     
        }
        
        /**
         * This function returns the name of WBS task column as a combination of "Estimated Finish date" and "percentage complete". 
         * And the percentage complete and estimated finish date is from project WBS Task.
         * @param context matrix.db.Context
         * @param args java.lang.String 
         * @param taskObjMap java.util.Map
         * @return java.lang.String
         * @throws Exception
         */
        public String getTaskNameColumn (Context context, String[] args,  Map taskObjMap) throws Exception
        {
            StringBuffer sBuff        = new StringBuffer();
            HashMap programMap        = (HashMap) JPO.unpackArgs(args);
            HashMap paramList         = (HashMap) programMap.get("paramList");
            boolean isPrinterFriendly = false;
            String strPrinterFriendly = (String)paramList.get("reportFormat"); 
            
            if ( strPrinterFriendly != null ) {
                isPrinterFriendly = true;
            }
            String taskId = (String) taskObjMap.get(DomainConstants.SELECT_ID);
            boolean blDeletedTask = false;
        
            double tz         = getTimezone(context);
            String sTimezone  = String.valueOf(tz);          
            String percentage = "%";
            
            String attribDateVal      = getUIDateFromEmxDate((String)taskObjMap.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE), Locale.US, sTimezone);
            String colorStatus        = getTaskColorStatus(context, taskObjMap);    
            String strPercentComplete = (String) taskObjMap.get(Task.SELECT_PERCENT_COMPLETE);
            String strVal             = (attribDateVal+"("+strPercentComplete + percentage+")");
            DomainObject taskObj      = DomainObject.newInstance(context, taskId);
            
            
            if(taskObj.hasRelatedObjects(context,DomainConstants.RELATIONSHIP_DELETED_SUBTASK,false)) {
                blDeletedTask = true;
            }
          
            //  Display critical path only for not completed
            if (blDeletedTask){
                sBuff.append("<font color='"+colorStatus+"'>");
                sBuff.append(strVal);
                sBuff.append("</font>");
            }else 
            {
                if(!isPrinterFriendly){                    
                    sBuff.append("<a href ='javascript:showModalDialog(\"");
                    sBuff.append("../common/emxTree.jsp?objectId=");
                    sBuff.append(taskId);                   
                    sBuff.append("\", \"875\", \"550\", \"false\", \"popup\")' title=\""+taskObjMap.get(DomainConstants.SELECT_CURRENT)+"\" " +
                            "style=\"color:"+colorStatus+"\">");
                    sBuff.append(strVal);
                    sBuff.append("</a>");
                } 
                else {               
                    //sBuff.append("<font color='"+colorStatus+"'>");
                    sBuff.append(strVal);
                    //sBuff.append("</font>");
                }              
            }          
            return sBuff.toString();
        }
        
        /**
         * This function returns the color status. 
         *              ?   If the task is late, it will be displayed in red font
         *              ?   If the task is at risk, it will be displayed in orange font
         *              ?   If the task is not complete but not late or at risk, it will be displayed in green font 
         * @param context matrix.db.Context
         * @param objectMap java.util.Map
         * @return java.lang.String
         * @throws Exception
         */
        public String getTaskColorStatus(Context context, Map objectMap)
        throws Exception
        {
           String colorStatus         = ProgramCentralConstants.EMPTY_STRING;
           String percentageComplete  = "100";                    
           String COLOR_STATUS_RED    = "#"+EnoviaResourceBundle.getProperty(context,"eServiceApplicationProgramCentral.SlipThreshholdColor3");
           String COLOR_STATUS_GREEN  = "#"+EnoviaResourceBundle.getProperty(context,"eServiceApplicationProgramCentral.SlipThreshholdColor1");
           String COLOR_STATUS_ORANGE = "#"+EnoviaResourceBundle.getProperty(context,"eServiceApplicationProgramCentral.SlipThreshholdColor2");
           
           com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
                    DomainConstants.TYPE_TASK,DomainConstants.TYPE_PROGRAM);
          
           String COMPLETE_STATE = PropertyUtil.getSchemaProperty(context, "policy", task.getDefaultPolicy(context), "state_Complete");
            try
            {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);       
                int yellowRedThreshold         = Integer.parseInt(EnoviaResourceBundle.getProperty(context,"eServiceApplicationProgramCentral.SlipThresholdYellowRed"));
                Date tempDate                  = new Date();
                Date sysDate                   = new Date(tempDate.getYear(), tempDate.getMonth(), tempDate.getDate());
                String strObjectType           = (String) objectMap.get(DomainConstants.SELECT_TYPE);
                   
                    Date baselineCurrentEndDate         = null;
                    String baselineCurrentEndDateString = (String) objectMap.get(task.SELECT_BASELINE_CURRENT_END_DATE);
                    Date estFinishDate                  = sdf.parse((String) objectMap.get(task.SELECT_TASK_ESTIMATED_FINISH_DATE));
                    if (!"".equals(baselineCurrentEndDateString))
                    {
                        baselineCurrentEndDate = sdf.parse(baselineCurrentEndDateString);
                    }
                    long daysRemaining;
                    if (null == baselineCurrentEndDate)
                    {
                        daysRemaining = (long) task.computeDuration(sysDate,
                                estFinishDate);
                    }
                    else
                    {
                        daysRemaining = (long) task.computeDuration(sysDate,
                                baselineCurrentEndDate);
                    }
                    String strState = (String)objectMap.get(task.SELECT_CURRENT);
                    if (null == baselineCurrentEndDate)
                    {                      
                        if (strState.equals(COMPLETE_STATE) ||
                              ((String) objectMap.get(task.SELECT_PERCENT_COMPLETE)).equals(
                                      percentageComplete))
                        {
                            colorStatus = COLOR_STATUS_GREEN;                      
                        }
                        else if (!strState.equals(COMPLETE_STATE) &&
                              sysDate.after(estFinishDate))
                        {
                            colorStatus = COLOR_STATUS_RED; 
                        }
                        else if (!strState.equals(COMPLETE_STATE) &&
                              daysRemaining <= yellowRedThreshold)
                        {
                            colorStatus = COLOR_STATUS_ORANGE; 
                        }
                        else
                        {
                            colorStatus = ProgramCentralConstants.EMPTY_STRING;
                        }
                    }
                    else
                    {
                        if (strState.equals(COMPLETE_STATE) ||
                              ((String) objectMap.get(task.SELECT_PERCENT_COMPLETE)).equals(
                                      percentageComplete))
                        {
                            colorStatus = COLOR_STATUS_GREEN; 
                        }
                        else if (!strState.equals(COMPLETE_STATE) &&
                              sysDate.after(baselineCurrentEndDate))
                        {
                            colorStatus = COLOR_STATUS_RED; 
                        }
                        else if (!strState.equals(COMPLETE_STATE) &&
                              (daysRemaining <= yellowRedThreshold))
                        {
                            colorStatus = COLOR_STATUS_ORANGE; 
                        }
                        else
                        {
                            colorStatus = ProgramCentralConstants.EMPTY_STRING;
                        }
                    }
            }
            catch (Exception ex)
            {
                throw ex;
            }            
            return colorStatus;            
        
        }
        
    /**
     * This method decides weather to show Project Template drop-down or not.
     * If 'objectId' present and of type Project Space or it's subtype, then it 
     * returns false for not to show Project Template drop-down.
     * 
     * @param 	context 
     * 			the eMatrix <code>Context</code> object.
     * @param 	argumentArray 
     * 			holds arguments which will be used in function.
     * 
     * @return 	isShowTemplateDropdown
     * 			true of call is not for Project or it's subtype, else false.
     * 
     * @throws 	Exception
     * 			Throw Exception when any abnormal condition occur while executing
     * 			the function.
     */
    public boolean isShowTemplatesCommand(Context context, String[] argumentArray) throws Exception {
    	
    	HashMap programMap 	= JPO.unpackArgs(argumentArray);
    	String objectId 	= (String)programMap.get("objectId");
    	boolean isShowTemplateDropdown  = true;
    	
    	if( ProgramCentralUtil.isNotNullString(objectId) && 
    			(new DomainObject(objectId).isKindOf(context,ProgramCentralConstants.TYPE_PROJECT_SPACE))) {
    		isShowTemplateDropdown = false;
    	}
    	
    	return isShowTemplateDropdown;
    }
}
