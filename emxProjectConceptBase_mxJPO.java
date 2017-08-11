/*
 ** emxProjectConceptBase.java
 ** Copyright (c) 2002-2016 Dassault Systemes.
 ** All Rights Reserved
 */

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import matrix.db.Access;
import matrix.db.AccessConstants;
import matrix.db.AttributeType;
import matrix.db.AttributeTypeList;
import matrix.db.BusinessInterface;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.TaskDateRollup;
import com.matrixone.apps.common.util.ComponentsUtil;
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
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectConcept;
import com.matrixone.apps.program.ProjectSpace;

/**
 * The <code>emxProjectConceptBase</code> class represents the Project Concept JPO
 * functionality for the AEF type.
 *
 * @version AEF 9.5.1.1 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxProjectConceptBase_mxJPO extends emxProjectSpace_mxJPO
{

    //Added:8-Sep-2010:S2E:R210 IR-067656V6R2012
    public static final String SELECT_ATTRIBUTE_TASK_ACTUAL_DURATION = "attribute[" + ATTRIBUTE_TASK_ACTUAL_DURATION + "]";

    /**
     * Constructs a new emxProjectConcept JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the id
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.1
     */
    public emxProjectConceptBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        // Call the super constructor
        super(context, args);
        if (args != null && args.length > 0)
        {
            setId(args[0]);
        }
    }


    // To get the originator of the object---- Added for Configurable
    // Program Central
    /**
     * This method is used to get the originator of the object
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: objectList - MapList
     *            containing the objects list
     * @return List String get Originator
     * @throws Exception
     *             if the operation fails
     * @since PMC V6R2008-1
     */
    public String getOriginator(Context context, String[] args)
    throws Exception
    {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        com.matrixone.apps.common.Person person =
            (com.matrixone.apps.common.Person) DomainObject.newInstance(context,
                    DomainConstants.TYPE_PERSON);
        String personId="";
        if(personId == null || personId.equals("") || personId.equals("null")) {
            personId = person.getPerson(context).getId();
        }
        person.setId(personId);

        StringList nameSelects = new StringList(2);
        nameSelects.add(person.SELECT_LAST_NAME);
        nameSelects.add(person.SELECT_FIRST_NAME);

        Map contextInfoMap = person.getInfo(context, nameSelects);
        String contextName = contextInfoMap.get(person.SELECT_LAST_NAME) + ", " + contextInfoMap.get(person.SELECT_FIRST_NAME);
        return contextName;
    }



    // To get the Company to which the Person is connected---- Added for
    // Configurable
    // Program Central
    /**
     * This method is used to get the Company to which the Person is connected
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: objectList - MapList
     *            containing the objects list
     * @return List String Company Name
     * @throws Exception
     *             if the operation fails
     * @since PMC V6R2008-1
     */
    public String getCompany (Context context,String[] args)throws Exception
    {
        String companyName="";

        com.matrixone.apps.common.Company company =
            (com.matrixone.apps.common.Company) DomainObject.newInstance(context,
                    DomainConstants.TYPE_COMPANY);

        com.matrixone.apps.common.Person person =
            (com.matrixone.apps.common.Person) DomainObject.newInstance(context,
                    DomainConstants.TYPE_PERSON);

        String personId=null;
        if(personId == null || personId.equals("") || personId.equals("null")) {
            personId = person.getPerson(context).getId();
        }
        person.setId(personId);

        // find the person's company
        StringList busSelects = new StringList(1);
        busSelects.add(company.SELECT_ID);
        busSelects.add(company.SELECT_NAME);
        company = person.getCompany(context);

        companyName = company.getInfo(context, company.SELECT_NAME);
        return companyName;
    }



//  to show the Business Unit Select ---- Added for Configurable
    // Program Central
    /**
     * This method is used to get Business Unitt of the project
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: objectList - MapList
     *            containing the objects list
     * @return List containing Business Unit connected to the Company
     * @throws Exception
     *             if the operation fails
     * @since PMC V6R2008-1
     */


    public String selectBusUnit (Context context,String[] args)throws Exception
    {
        StringBuffer output = new StringBuffer();
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            String strMode = (String) requestMap.get("mode");
            String projectConceptId = (String) requestMap.get("objectId");
            String languageStr = (String) requestMap.get("languageStr");
            DomainObject domProjectConcept=DomainObject.newInstance(context);
            String strOuput ="";
            StringBuffer busUnitName=new StringBuffer();
            StringBuffer busUnitId=new StringBuffer();

            if(ProgramCentralUtil.isNotNullString(projectConceptId))
            {
                domProjectConcept = DomainObject.newInstance(context, projectConceptId);
                StringList selectStmts  = new StringList(2);
                selectStmts.addElement(SELECT_ID);
                selectStmts.addElement(SELECT_NAME);

                MapList mapList = domProjectConcept.getRelatedObjects( context,
                        DomainConstants.RELATIONSHIP_BUSINESS_UNIT_PROJECT,  	// relationship pattern
                        DomainConstants.TYPE_BUSINESS_UNIT,						// object pattern
                        selectStmts,                 							// object selects
                        null,              										// relationship selects
                        true,                       							// to direction
                        false,                        							// from direction
                        (short) 1,                   							// recursion level
                        null,                        							// object where clause
                        null);                       // relationship

                if(mapList != null && mapList.size() > 0)
                {
                    // construct array of ids
                    int mapListSize = mapList.size();
                    for(int i = 0; i < mapListSize; i++)
                    {
                        Map dataMap = (Map)mapList.get(i);
                        String type = (String)dataMap.get(SELECT_TYPE);
                        String name = (String)dataMap.get(SELECT_NAME);
                        String objectId =(String)dataMap.get(SELECT_ID);

                        busUnitName.append(name+",");
                        busUnitId.append(objectId+",");

                    }
                }
            }
            String strBusUnitName = "";
            String strBusUnitId = "";


            if(busUnitName.length()>0)
            {
                strBusUnitName = busUnitName.toString();
                strBusUnitName =strBusUnitName.substring(0,strBusUnitName.length()-1);
                strBusUnitId = busUnitId.toString();
                strBusUnitId =strBusUnitId.substring(0,strBusUnitId.length()-1);
            }
            com.matrixone.apps.common.Company company =
                (com.matrixone.apps.common.Company) DomainObject.newInstance(context,
                        DomainConstants.TYPE_COMPANY);

            com.matrixone.apps.common.Person person =
                (com.matrixone.apps.common.Person) DomainObject.newInstance(context,
                        DomainConstants.TYPE_PERSON);

            String personId=null;
            if(personId == null || personId.equals("") || personId.equals("null")) {
                personId = person.getPerson(context).getId();
            }
            person.setId(personId);

            if(strMode.equalsIgnoreCase("edit")|| strMode.equalsIgnoreCase("create"))
            {
                String clearLabel = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
                        "emxProgramCentral.Common.Clear",languageStr);
                // find the person's company
                StringList busSelects = new StringList(1);
                busSelects.add(company.SELECT_ID);
                busSelects.add(company.SELECT_NAME);
                company = person.getCompany(context);
                String companyId = company.getInfo(context, company.SELECT_ID);
                output.append("<input type='text' name='BusinessUnitName' value='"+XSSUtil.encodeForHTML(context, strBusUnitName)+"' readonly=\"readonly\"/>");
                output.append("<input type='hidden' name='BusinessUnitId' value='"+strBusUnitId+"'/>");
                output.append("<input type='button' name='business' value='...' onClick=\"");
                //output.append("javascript:showChooser('../programcentral/emxProgramCentralOrganizationSelectDialogFS.jsp?form=emxCreateForm&amp;fieldName=BusinessUnitName&amp;fieldId=BusinessUnitId&amp;");
                //output.append("objectId="+companyId+"&amp;suiteKey=ProgramCentral','600','600')\"/>");
                output.append("javascript:showChooser('");
                String strURL = ProgramCentralConstants.EMPTY_STRING;      			
        	strURL="../common/emxFullSearch.jsp?field=TYPES=type_BusinessUnit&amp;table=PMCOrganizationSummary&amp;selection=single&amp;submitURL=../programcentral/emxProgramCentralResourceRequestAutonomySearchSelect.jsp&amp;fieldNameActual=BusinessUnitId&amp;fieldNameDisplay=BusinessUnitName&amp;suiteKey=ProgramCentral"; 
                output.append(strURL);
        	output.append("','600','600')\"/>");
                output.append("<input type='checkbox' name='removeBU' onClick='removeBusinessUnit();'/>");
                output.append(clearLabel);
                strOuput =output.toString();
            }
            else if(ProgramCentralUtil.isNotNullString(strBusUnitId)){
            	strBusUnitId =  XSSUtil.encodeForURL(context, strBusUnitId);
            	output.append("<a href='../common/emxTree.jsp?objectId=").append(strBusUnitId);
            	output.append("&amp;relId=null&amp;suiteKey=ProgramCentral'>");
            	output.append("<img src='../common/images/iconSmallBusinessUnit.gif' border='0' valign='absmiddle'/>");
            	output.append(XSSUtil.encodeForHTML(context, strBusUnitName));
            	output.append("</a>");
            	strOuput =output.toString();
            }
            return strOuput ;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }



    // to show the Program Select ---- Added for Configurable
    // Program Central
    /**
     * This method is used to get Program of the Company
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: objectList - MapList
     *            containing the objects list
     * @return List containing Program connected to the Company
     * @throws Exception
     *             if the operation fails
     * @since PMC V6R2008-1
     */


    public String selectProgram (Context context,String[] args)throws Exception
    {
        StringBuffer output = new StringBuffer();
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            String strMode = (String) requestMap.get("mode");
            String projectConceptId = (String) requestMap.get("objectId");
            String strProgramName = DomainConstants.EMPTY_STRING;
            String strProgramId = DomainConstants.EMPTY_STRING;
            String objName = DomainConstants.EMPTY_STRING;
            StringBuffer programName = new StringBuffer();
            StringBuffer programId = new StringBuffer();
            String languageStr = (String) requestMap.get("languageStr");
            String strOuput =DomainConstants.EMPTY_STRING;
            DomainObject object = null;
            //Following condition is added for accessing project from program categories.
            boolean isComingFromProgram = false;
            if(ProgramCentralUtil.isNotNullString(projectConceptId)){
                object = DomainObject.newInstance(context,projectConceptId);
                objName = object.getInfo(context, SELECT_NAME);
            if(object.isKindOf(context, TYPE_PROGRAM)){
                isComingFromProgram = true;
            }
            else if(object.isKindOf(context, TYPE_PROJECT_MANAGEMENT)){
                isComingFromProgram = false;

                    StringList selectStmts  = new StringList(2);
                    selectStmts.addElement(SELECT_ID);
                    selectStmts.addElement(SELECT_NAME);
                    //Get connected Programs list
                    MapList mapList = object.getRelatedObjects(context,
                            DomainConstants.RELATIONSHIP_PROGRAM_PROJECT,  // relationship pattern
                            DomainConstants.TYPE_PROGRAM,                    // object pattern
                            selectStmts,                 // objectselects
                            null,              // relationship selects
                            true,                       // to direction
                            false,                        // from direction
                            (short) 1,                   // recursion level
                            null,                        // object where clause
                            null);                       // relationship where clause
                    if(mapList != null && mapList.size() > 0){
                        // construct array of ids
                        int mapListSize = mapList.size();
                        for(int i = 0; i < mapListSize; i++){
                            Map dataMap = (Map)mapList.get(i);
                            String name = (String)dataMap.get(SELECT_NAME);
                            String objectId =(String)dataMap.get(SELECT_ID);
                            programName.append(name + ",");
                            programId.append(objectId + ",");
                        }
                    }
                }
                if(programName.length()>0){
                    strProgramName = programName.toString();
                    strProgramName = strProgramName.substring(0,strProgramName.length()-1);
                    strProgramId = programId.toString();
                    strProgramId = strProgramId.substring(0,strProgramId.length()-1);
                }
            }

            if(strMode.equalsIgnoreCase("edit")|| strMode.equalsIgnoreCase("create"))
            {
            	String clearLabel = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
            			"emxProgramCentral.Common.Clear",languageStr);
            	if(isComingFromProgram){
            		output.append("<input type='text' name='ProgramName' value='"+ objName +"'/>");
            		output.append("<input type='hidden' name='ProgramId' value='" + projectConceptId + "'/>");
            	}
            	else{
            		// find the person's company
            		String personId = PersonUtil.getPersonObjectID(context);
            		Person person = new Person();
            		person.setId(personId);
            		Company company = person.getCompany(context);
            		String companyId = company.getInfo(context, company.SELECT_ID);
            		boolean isECHInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionEnterpriseChange",false,null,null);
            		//Added for special character.
            		output.append("<input type='text' name='ProgramName' value='"+XSSUtil.encodeForHTML(context,strProgramName)+"' readonly=\"readonly\"/>");
            		output.append("<input type='hidden' name='ProgramId' value='" + strProgramId + "'/>");
            		output.append("<input type='button' name='program' value='...' onClick=\"");
            		output.append("javascript:showChooser('");
            		String strURLl= "";
            		if(isECHInstalled && object!=null  && object.isKindOf(context, TYPE_CHANGE_PROJECT) && !"".equals(strProgramName)) {
            			
            			strURLl="../common/emxFullSearch.jsp?field=TYPES=type_Program&amp;table=PMCProgramAutonomySearch&amp;selection=single&amp;includeOIDprogram=emxProjectSpace:getIncludeOIDforProgramSearch&amp;submitURL=../programcentral/emxProgramCentralAutonomySearchUtil.jsp&amp;fieldNameActual=ProgramId&amp;fieldNameDisplay=ProgramName&amp;companyId="+companyId+"&amp;projectId="+projectConceptId+"&amp;suiteKey=ProgramCentral";
            				            			
            		}else{

            			strURLl="../common/emxFullSearch.jsp?field=TYPES=type_Program&amp;table=PMCProgramAutonomySearch&amp;selection=multiple&amp;includeOIDprogram=emxProjectSpace:getIncludeOIDforProgramSearch&amp;submitURL=../programcentral/emxProgramCentralAutonomySearchUtil.jsp&amp;fieldNameActual=ProgramId&amp;fieldNameDisplay=ProgramName&amp;companyId="+companyId+"&amp;projectId="+projectConceptId+"&amp;suiteKey=ProgramCentral";
            		}
            		output.append(strURLl);
            		output.append("','600','600')\"/>");

            		boolean canRemoveProgram = true;
            		if(isECHInstalled) {
            			if(object!=null ){
            				if(object.isKindOf(context, TYPE_CHANGE_PROJECT) && !"".equals(strProgramName)) {
            					canRemoveProgram = false;
            				}
            			}
            		}
            		if(canRemoveProgram) {
            			output.append("<input type='checkbox' name='removeAll' onClick='removeProgram();'/>");
            			output.append(clearLabel);
            		}
            		//End:R207:ECH:Bug:373247
            	}
            	strOuput =output.toString();
            }
            else{
                StringList slProgramNameList = FrameworkUtil.split(strProgramName, ",");
                StringList slProgramIdList = FrameworkUtil.split(strProgramId, ",");
                String strPrgId = "";
                String strPrgName= "";
                String pfMode=(String)requestMap.get("PFmode");
                if(!slProgramIdList.isEmpty()){
                    for(int nCount=0;nCount<slProgramIdList.size();nCount++){
                        strPrgId = (String)slProgramIdList.get(nCount);
                        strPrgId = XSSUtil.encodeForURL(context, strPrgId);
                        strPrgName=(String)slProgramNameList.get(nCount);
                        strPrgName  =   XSSUtil.encodeForHTML(context, strPrgName);
                        if(UIUtil.isNullOrEmpty(pfMode) || "false".equalsIgnoreCase(pfMode)){
                        	output.append("<a href='../common/emxTree.jsp?objectId=");           
	                        output.append(strPrgId);
	                        output.append("&amp;relId=null&amp;suiteKey=ProgramCentral'>");
	                        output.append("<img src='../common/images/iconSmallProgram.gif' border='0' valign='absmiddle'/>");
	                        output.append(strPrgName);
	                        output.append("</a>,");
                        }else{
                        	// in case of PFMode=true, just show the program(s) name(s).
                        	output.append("<img src='../common/images/iconSmallProgram.gif' border='0' valign='absmiddle'/>");
                        	output.append(strPrgName);
                        	output.append(",");
                        }
                    }
                    strOuput = output.toString().substring(0,output.toString().length()-1);
                }
            }
            return strOuput ;
        }
        catch (Exception ex){
            throw ex;
        }
    }

    /**
     * Displays Default Duration Unit for Task Actual Duration as it is based days.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the input arguments 'String' containing the object id
     * @return The value of Task Duration with Default Duration Unit 'd'
     * @since R210
     * @author S2E
     * @throws Exception if operation fails
     */
    public String showDurationUnit(Context context, String[] args) throws MatrixException
    {
        try {
            String strTaskDuration = null;
            Map programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            String languageStr = (String) requestMap.get("languageStr");
            Map mpParamMap = (HashMap)programMap.get("paramMap");
            String objId = (String)mpParamMap.get("objectId");
            DomainObject dmo = DomainObject.newInstance(context, objId);
            strTaskDuration = dmo.getInfo(context, SELECT_ATTRIBUTE_TASK_ACTUAL_DURATION);
            //The actual duration is always calculated in days
            return strTaskDuration + " " + EnoviaResourceBundle.getProperty(context, "ProgramCentral",
                    "emxProgramCentral.DurationUnits.Days",languageStr);
        } catch (Exception e) {
            throw new MatrixException(e);
        }
    }


//  To Perform Post Process Actions after creating a concept---- Added for
//  Configurable
    // Program Central
    /**
     * This method is used to Post Process Actions after creating a concept
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: objectList - MapList
     *            containing the objects list
     * @return List Nothing
     * @throws Exception
     *             if the operation fails
     * @since PMC V6R2008-1
     */

    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void performPostProcessActions (Context context,String[] args)throws Exception
    {
        // Check license while creating Project Concept, if license check fails here
        // the projects will not be created.
        //
        ComponentsUtil.checkLicenseReserved(context,ProgramCentralConstants.PGE_PRG_LICENSE_ARRAY);


        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String businessUnitId= (String)requestMap.get("BusinessUnitId");
        String programId= (String)requestMap.get("ProgramId");
        String defaultVault = (String)requestMap.get("defaultVault");
        String strProjName = (String)requestMap.get("Name");
        String fromRelatedProjects = (String)requestMap.get("fromRelatedProjects");
        String strDescription = (String)requestMap.get("ProjectDescription");
        String fromProgram = (String)requestMap.get("fromProgram");
        String strOwner=(String)requestMap.get("Owner");
        String objectId = (String)paramMap.get("objectId");
        String strProjectDate = (String)requestMap.get("ProjectDate");
        String strScheduleFrom = (String)requestMap.get("Schedule From");
        String strTimeZone = (String)requestMap.get("timeZone");
        double clientTimeZone = Double.parseDouble(strTimeZone);
        String calendarId 			= (String)requestMap.get("Calendar");
  	StringList calendarIds = FrameworkUtil.split(calendarId, ProgramCentralConstants.COMMA);
  	

        com.matrixone.apps.program.Program program =
            (com.matrixone.apps.program.Program) DomainObject.newInstance(context,
                    DomainConstants.TYPE_PROGRAM,DomainConstants.PROGRAM);
        com.matrixone.apps.program.ProjectConcept projectConcept =
            (com.matrixone.apps.program.ProjectConcept) DomainObject.newInstance(context,
                    DomainConstants.TYPE_PROJECT_CONCEPT,DomainConstants.PROGRAM);
        String revision=projectConcept.getUniqueName(DomainConstants.EMPTY_STRING);

        try{
			DomainObject pc = DomainObject.newInstance(context, objectId);
			System.out.println(pc.getInfo(context, "name"));
            projectConcept.startTransaction(context, true);
            projectConcept.setId(objectId);
          //PRG:RG6:R213:Mql Injection:parameterized Mql:17-Oct-2011:start
            String sCommandStatement = "modify bus $1 $2 $3 name $4 revision $5";
            MqlUtil.mqlCommand(context, sCommandStatement,DomainConstants.TYPE_PROJECT_CONCEPT,strProjName, "",strProjName,revision);
          //PRG:RG6:R213:Mql Injection:parameterized Mql:17-Oct-2011:End

            if(programId!= null && !programId.equals("") && !programId.equals("null")) {
                projectConcept.setProgram(context, programId);
            }

            if(businessUnitId!= null && !businessUnitId.equals("") && !businessUnitId.equals("null")) {
                projectConcept.setOrganization(context, businessUnitId);
            }

            DomainObject _accessListObject=DomainObject.newInstance(context);
            // create project access list object and connect to project
            _accessListObject.createAndConnect(context,
                    DomainConstants.TYPE_PROJECT_ACCESS_LIST,
                    projectConcept.getUniqueName("PAL-"),
                    DomainConstants.EMPTY_STRING,
                    DomainConstants.POLICY_PROJECT_ACCESS_LIST,
                    defaultVault,
                    DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST,
                    projectConcept,
                    false);
            // get person id
            com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);

            // get the company id for this context
            com.matrixone.apps.common.Company company = person.getCompany(context);
            StringList objectSelects = new StringList(2);
            objectSelects.add(DomainConstants.SELECT_TYPE);
            objectSelects.add(ProgramCentralConstants.SELECT_PHYSICALID);
            Map projectConceptInfo = projectConcept.getInfo(context, objectSelects);
            
            String type = (String)projectConceptInfo.get(DomainConstants.SELECT_TYPE);
            String sourceId = (String)projectConceptInfo.get(ProgramCentralConstants.SELECT_PHYSICALID);

            if ((DomainConstants.TYPE_PROJECT_TEMPLATE).equals(type))
            {
              //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:start
                sCommandStatement = "connect bus $1 preserve relationship $2 to $3";
                MqlUtil.mqlCommand(context, sCommandStatement,company.getObjectId(),DomainConstants.RELATIONSHIP_COMPANY_PROJECT_TEMPLATES,objectId);
              //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:End

             }
            else
            {
              //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:start
                sCommandStatement = "connect bus $1 preserve relationship $2 to $3";
                MqlUtil.mqlCommand(context, sCommandStatement,company.getObjectId(),DomainConstants.RELATIONSHIP_COMPANY_PROJECT,objectId);
              //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:End
            }

            String personId = person.getObjectId();
            // projectConcept.addMember(context, personId);
          //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:start
            sCommandStatement = "connect bus $1 preserve relationship $2 to $3 $4 $5";
            MqlUtil.mqlCommand(context, sCommandStatement,objectId,DomainConstants.RELATIONSHIP_MEMBER,personId,DomainConstants.ATTRIBUTE_PROJECT_ACCESS,"Project Owner");
          //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:End

            if(strOwner!= null && !strOwner.equals("") && !strOwner.equals("null")) {
              projectConcept.setOwner(context, strOwner);

            }
            // add the originator as a default "Project Owner" member
            // of this project


            final String ATTRIBUTE_SCHEDULE_FROM = PropertyUtil.getSchemaProperty(context, "attribute_ScheduleFrom");
            strProjectDate = eMatrixDateFormat.getFormattedInputDate(context, strProjectDate, clientTimeZone, (Locale)requestMap.get("localeObj"));


            //
            // TaskDateRollUp bean algorithm takes into account the time of the the start and finish dates
            // to account for that login following adjustments is done.
            //

            Date dtProjectDate = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(strProjectDate);
            Calendar calendar = new GregorianCalendar();
            calendar.setTime (dtProjectDate);

            if ("Project Start Date".equals(strScheduleFrom)) {
                calendar.set (Calendar.HOUR_OF_DAY, 8);
                calendar.set (Calendar.MINUTE, 0);
                calendar.set (Calendar.SECOND, 0);
                calendar.set (Calendar.MILLISECOND, 0);
            }
            else if ("Project Finish Date".equals(strScheduleFrom)) {
                calendar.set (Calendar.HOUR_OF_DAY, 17);
                calendar.set (Calendar.MINUTE, 0);
                calendar.set (Calendar.SECOND, 0);
                calendar.set (Calendar.MILLISECOND, 0);
            }

            dtProjectDate = calendar.getTime();
            //Added:02-June-2010:vf2:R210 PRG:IR-031079
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat (com.matrixone.apps.domain.util.eMatrixDateFormat.getEMatrixDateFormat(),Locale.US);
            //End:02-June-2010:vf2:R210 PRG:IR-031079
            strProjectDate = simpleDateFormat.format (dtProjectDate);

            HashMap attributes = new HashMap(4);
            attributes.put(DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE, strProjectDate);
            attributes.put(DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE, strProjectDate);
            attributes.put(ATTRIBUTE_SCHEDULE_FROM, strScheduleFrom);
            attributes.put(DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION, "0");
            attributes.put(DomainConstants.ATTRIBUTE_ORIGINATOR, context.getUser());
            attributes.put(ProgramCentralConstants.ATTRIBUTE_SOURCE_ID, sourceId);

            projectConcept.setAttributeValues(context, attributes);

            if (strDescription != null)
            {
                projectConcept.setDescription(context, strDescription);
            }

            if(fromRelatedProjects!=null && fromRelatedProjects.equalsIgnoreCase("true"))
            {
                String relProjectId = (String)requestMap.get("objectId");
                String relatedProjectRelationship = PropertyUtil.getSchemaProperty(context, DomainObject.SYMBOLIC_relationship_RelatedProjects);
                projectConcept.setRelatedObject(context, relatedProjectRelationship,false, relProjectId);
            }
            if(fromProgram!=null && fromProgram.length()!=0)
            {

                String programProjectRelationship = PropertyUtil.getSchemaProperty(context, DomainObject.SYMBOLIC_relationship_ProgramProject);
                projectConcept.setRelatedObject(context, programProjectRelationship,false, fromProgram);
            }

			//This is a rework done for storing primary ownership on Project Concept to fix IR-272065V6R2014x.
			StringList pcSelects = new StringList();
			pcSelects.add(ProgramCentralConstants.SELECT_COLLABORATIVE_SPACE);
			pcSelects.add(ProgramCentralConstants.SELECT_ORGANIZATION);
			pcSelects.add(ProjectSpace.SELECT_PROJECT_VISIBILITY);

			Map conceptInfo = projectConcept.getInfo(context, pcSelects);
			String visibility = (String)conceptInfo.get(ProjectSpace.SELECT_PROJECT_VISIBILITY);
			String project = (String)conceptInfo.get(ProgramCentralConstants.SELECT_COLLABORATIVE_SPACE);
			String org = (String)conceptInfo.get(ProgramCentralConstants.SELECT_ORGANIZATION);

			if("Company".equals(visibility) && 
					(ProgramCentralUtil.isNullString(project) || (ProgramCentralUtil.isNullString(org)))){
				String defaultOrg = PersonUtil.getDefaultOrganization(context, context.getUser());
				String defaultProj = PersonUtil.getDefaultProject(context, context.getUser());
				projectConcept.setPrimaryOwnership(context,defaultProj,defaultOrg);
			}
			
			projectConcept.addCalendars(context, calendarIds);

            ContextUtil.commitTransaction(context);
        }
		catch(Exception ee){
			throw new Exception(ee);
        }
    }

    // To Perform Post Process Actions after Editing a Project Concept/Project
    // Space. ---- Added for Configurable
    // Program Central
    /**
     * This method is used to Perform Post Process Actions after Editing a
     * Project Concept/Project Space
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: objectList - MapList
     *            containing the objects list
     * @return void
     * @throws Exception
     *             if the operation fails
     * @since PMC V6R2008-1
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void performPostProcessActionsEdit(Context context, String[] args)
    throws Exception
    {
        try{
            com.matrixone.apps.program.ProjectConcept projectConcept =
                (com.matrixone.apps.program.ProjectConcept) DomainObject.newInstance(context,
                        DomainConstants.TYPE_PROJECT_CONCEPT,DomainConstants.PROGRAM);

            com.matrixone.apps.common.Person person =
                (com.matrixone.apps.common.Person) DomainObject.newInstance(context,
                        DomainConstants.TYPE_PERSON);

            final String ATTRIBUTE_SCHEDULE_FROM = PropertyUtil.getSchemaProperty(context, "attribute_ScheduleFrom");
            final String SELECT_ATTRIBUTE_SCHEDULE_FROM = "attribute[" + ATTRIBUTE_SCHEDULE_FROM + "]";
            
            final String RELATIONSHIP_CONTRIBUTES_TO = PropertyUtil.getSchemaProperty("relationship_ContributesTo");
            final String SELECT_CONTRIBUTES_TO_RELATIONSHIP_ID = "from[" + RELATIONSHIP_CONTRIBUTES_TO + "].id";

            StringList busSelects = new StringList(2);
            busSelects.add(ProjectConcept.SELECT_OWNER);
            busSelects.add(SELECT_PROJECT_ACCESS_LIST_ID);

            busSelects.add(SELECT_TYPE);
            busSelects.add(SELECT_ATTRIBUTE_SCHEDULE_FROM);
            busSelects.add(SELECT_CONTRIBUTES_TO_RELATIONSHIP_ID);

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            HashMap paramMap = (HashMap) programMap.get("paramMap");
            String objectId = (String)paramMap.get("objectId");
            String businessUnitId= (String)requestMap.get("BusinessUnitId");
            String programId= (String)requestMap.get("ProgramId");
            String deliverableId= (String)requestMap.get("DeliverableEditable");
            //Modified for Bug # 340636 on 9/7/2007 - Start
            String ProjectName= (String)requestMap.get("ProjectName");

            projectConcept.setId(objectId);
            Map projectInfo = projectConcept.getInfo(context,busSelects);

            //
            // Following code is written for coping with TaskDateRollUp algorithm. This code should be moved to Schedule From attribute
            // modify Action trigger. Its late in X+4 to change the schema, hence the code is accomodated here.
            // When a project lead changes Schedule From attribute value for the existing project, following code
            // resets the time part of the project's estimated start/finish date appropriately, so that roll up process
            // algorithm still functions correctly.
            //
            String strScheduleFrom = (String)projectInfo.get(SELECT_ATTRIBUTE_SCHEDULE_FROM);
            String existingProjectScheduleFromVal = (String)requestMap.get("Schedule FromfieldValue");
            
            boolean isScheduledChanged = true;
            if(ProgramCentralUtil.isNullString(existingProjectScheduleFromVal) ||strScheduleFrom.equalsIgnoreCase(existingProjectScheduleFromVal)){
            	isScheduledChanged = false;
            }
           
            //Added:NZF:22-Dec-2011:IR-091218V6R2012
            if(isScheduledChanged){
            	com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject
            			.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
            	task.setId(objectId);
            	task.rollupAndSave(context);
            }

            //End:NZF:22-Dec-2011:IR-091218V6R2012

            String strType = (String)projectInfo.get(SELECT_TYPE);
            StringList prjSpaceSubTypes = getAllProjectSubTypeNames(context,DomainConstants.TYPE_PROJECT_SPACE);
            StringList prjConceptSubTypes = getAllProjectSubTypeNames(context,DomainConstants.TYPE_PROJECT_CONCEPT);
            String strProjType = "";
            // [MODIFIED::Jan 20, 2011:S4E:R211:TypeAhead::Start]
            String strFieldName="";
            if(prjSpaceSubTypes.contains(strType)){
                strProjType = DomainConstants.TYPE_PROJECT_SPACE;
                strFieldName ="ProjectSpaceOwner";
            } else if(prjConceptSubTypes.contains(strType)){
                strProjType = DomainConstants.TYPE_PROJECT_CONCEPT;
                strFieldName ="ProjectConceptOwner";
            }
            String ownerName =(String) requestMap.get(strFieldName);
            // [MODIFIED::Jan 20, 2011:S4E:R211:TypeAhead::End]
            BusinessType busType = new BusinessType(strProjType,context.getVault());
            if(busType.hasChildren(context))
            {
                String ActualType = (String) requestMap.get("ActualType");
                boolean isContextPushed = false;
                if (!(projectConcept.checkAccess(context,(short) AccessConstants.cModify)))
                {
                    ContextUtil.pushContext(context);
                    isContextPushed = true;
                }
                if(ActualType!=null && !ActualType.isEmpty()){
                    projectConcept.setType(context,ActualType,objectId,projectConcept.getPolicy(context).toString());
                }
                if(isContextPushed)
                    ContextUtil.popContext(context);
            }

            //Added for Change Discipline
            boolean isECHInstalled = com.matrixone.apps.domain.util.FrameworkUtil.isSuiteRegistered(context,"appVersionEnterpriseChange",false,null,null);
            if(isECHInstalled){
                if(projectConcept.isKindOf(context, PropertyUtil.getSchemaProperty(context,"type_ChangeProject"))){
                    String strInterfaceName = PropertyUtil.getSchemaProperty(context,"interface_ChangeDiscipline");
                    //Check if an the change discipline interface has been already connected
                    //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:start
                    String sCommandStatement = "print bus $1 select $2 dump";
                    String sIsInterFacePresent = MqlUtil.mqlCommand(context, sCommandStatement,objectId,"interface[" + strInterfaceName + "]");
                  //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:End

                    //If no interface --> add one
                    if("false".equalsIgnoreCase(sIsInterFacePresent)){
                        //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:start
                        sCommandStatement = "modify bus $1 add interface $2";
                        MqlUtil.mqlCommand(context, sCommandStatement,objectId,strInterfaceName);
                      //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:End
                    }

                    BusinessInterface busInterface = new BusinessInterface(strInterfaceName, context.getVault());
                    AttributeTypeList listInterfaceAttributes = busInterface.getAttributeTypes(context);

                    Iterator listInterfaceAttributesItr = listInterfaceAttributes.iterator();
                    while (listInterfaceAttributesItr.hasNext()){
                        String attrName = ((AttributeType)listInterfaceAttributesItr.next()).getName();
                        String attrNameSmall = attrName.replaceAll(" ", "");
                        String attrNameSmallHidden = attrNameSmall + "Hidden";
                        String attrNameValue = (String)requestMap.get(attrNameSmallHidden);

                        if(attrNameValue!=null && !attrNameValue.equalsIgnoreCase("")){
                            projectConcept.setAttributeValue(context, attrName, attrNameValue);
                        }else{
                            projectConcept.setAttributeValue(context, attrName, "No");
                        }
                    }
                }
            }
            //End Added for Change Discipline

            //Modified for Bug # 340636 on 9/7/2007 - End
            /*Map projectInfo = projectConcept.getInfo(context,busSelects);*/
            String oldOwner = (String) projectInfo.get(ProjectConcept.SELECT_OWNER);
            String accessObjectId = (String) projectInfo.get(SELECT_PROJECT_ACCESS_LIST_ID);
            DomainObject accessObject = DomainObject.newInstance(context,accessObjectId);
            if(ownerName!= null && !ownerName.equals("") && !ownerName.equals("null"))
            {
               projectConcept.setOwner(context,ownerName);
               accessObject.setOwner(context, ownerName);
            }

            person = com.matrixone.apps.common.Person.getPerson(context);

            // get the company id for this context
            com.matrixone.apps.common.Company company = person.getCompany(context);
            if(businessUnitId!= null && !businessUnitId.equals("") && !businessUnitId.equals("null"))
            {
                projectConcept.setBusinessUnit(context, businessUnitId);
            }
            else
            {
                projectConcept.setBusinessUnit(context, null);
            }
            // Connect Program
            if(programId!= null && !programId.equals("") && !programId.equals("null")) {
                projectConcept.setProgram(context, programId);
            } else {
                projectConcept.setProgram(context, null);
            }

            // Connect Deliverable
            String strContributesToId = (String)projectInfo.get(SELECT_CONTRIBUTES_TO_RELATIONSHIP_ID);
            if(ProgramCentralUtil.isNotNullString(deliverableId)){
    			DomainObject deliverable = DomainObject.newInstance(context,deliverableId);
    			
    			if (ProgramCentralUtil.isNotNullString(strContributesToId))
    				DomainRelationship.setToObject( context, strContributesToId, deliverable);
    			else
    				DomainRelationship.connect( context, projectConcept, RELATIONSHIP_CONTRIBUTES_TO, deliverable);
    		}
            else
            {
            	if (ProgramCentralUtil.isNotNullString(strContributesToId))
            		DomainRelationship.disconnect( context, strContributesToId);
            }

           if(ownerName!= null && !ownerName.equals("") && !ownerName.equals("null") && (!ownerName.equals(oldOwner))){
            Access accessMask = new Access();
            //accessMask.setReadAccess(true);
            //accessMask.setShowAccess(true);
            //Added 337605_1
            accessMask.setModifyAccess(true);
            accessMask.setExecuteAccess(true);
            accessMask.setUser(oldOwner);

            StringList userList = new StringList(1);
            userList.add(oldOwner);

            BusinessObjectList objects = new BusinessObjectList(1);
            objects.add(accessObject);

            ContextUtil.pushContext(context,
                                    DomainConstants.PERSON_WORKSPACE_ACCESS_GRANTOR,
                                    null,
                                    null);
               //revoke all access on the PAL object for the Old Owner
               BusinessObject.revokeAccessRights(context,objects,userList);
              //Grant the required access on the PAL object to the Old Owner
               BusinessObject.grantAccessRights(context,objects,accessMask);
           }
        }catch(Exception ee){
        	String strMsg = EnoviaResourceBundle.getProperty(context, ProgramCentralConstants.PROGRAMCENTRAL, 
					"emxProgramCentral.Experiment.ModifyAsscess", context.getSession().getLanguage());
        	
        	MqlUtil.mqlCommand(context, "warning $1", strMsg);
        	ee.printStackTrace();
        }


    }



    // To check if EPM is Installed---- Added for Configurable
    // Program Central
    /**
     * This method is used to check if EPM is Installed
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: objectList - MapList
     *            containing the objects list
     * @return List String get Originator
     * @throws Exception
     *             if the operation fails
     * @since PMC V6R2008-1
     */

    public boolean checkIfEPMInstalled(Context context, String[] args) throws Exception
    {

        if(ProjectSpace.isEPMInstalled(context))
        {
            return true;
        }
        else{
            return false;
        }
    }





    // To check the Viewer Format Access---- Added for Configurable
    // Program Central
    /**
     * This method is used to check the Viewer Format Access
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: objectList - MapList
     *            containing the objects list
     * @return boolean
     * @throws Exception
     *             if the operation fails
     * @since PMC V6R2008-1
     */

    public boolean checkViewerFormatAccess(Context context, String[] args) throws Exception
    {

        if(ProjectSpace.isEPMInstalled(context))
        {

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String mode = (String)programMap.get("mode");
            if(mode.equals("edit"))
            {
                return true;
            }
        }

        return false;

    }


    // To get the DSS Selector
    // Program Central
    /**
     * This method is used To get the DSS Selector
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: objectList - MapList
     *            containing the objects list
     * @return String
     * @throws Exception
     *             if the operation fails
     * @since PMC V6R2008-1
     */

    public String getDSSSelector(Context context, String[] args) throws Exception
    {

        StringBuffer output = new StringBuffer();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String objectId = (String)paramMap.get("objectId");
        DomainObject domainObject=DomainObject.newInstance(context, objectId);
        domainObject.setId(objectId);
        String selector = domainObject.getInfo(context,"vcfile[1].specifier");
        String value="";
        if (selector==null)
        {
            value="Trunk:Latest";
        }
        else
        {
            value=selector;
        }

        output.append("<input type='text' name='selector' value='"+XSSUtil.encodeForHTMLAttribute(context, value)+"' size='30' readonly='readonly'/>");


        return output.toString();
    }







    // To get the DSS Server Name---- Added for Configurable
    // Program Central
    /**
     * This method is used to get the DSS Server Name
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: objectList - MapList
     *            containing the objects list
     * @return String DSS Servers
     * @throws Exception
     *             if the operation fails
     * @since PMC V6R2008-1
     */

    public String getDSServer(Context context, String[] args) throws Exception
    {



        StringBuffer output = new StringBuffer();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String objectId = (String)paramMap.get("objectId");
        String mode = (String)requestMap.get("mode");
        String populateDefaults=(String)requestMap.get("populateDefaults");
        String   noStore = EnoviaResourceBundle.getProperty(context, "Components",
                "emxComponents.Common.None", context.getSession().getLanguage());
        String localPath = "";
        String localServer = "";
        String populateLocalServerData = "false";
        String strPath = "";
        String strServer = "";
        try{
            localPath = EnoviaResourceBundle.getProperty(context, "emxComponents.LocalDSPath");
            localServer = EnoviaResourceBundle.getProperty(context, "emxComponents.LocalDSServer");
            populateLocalServerData = EnoviaResourceBundle.getProperty(context, "emxComponents.PopulateLocalServerData");
        } catch(Exception ex){
            // Skip because properties might not be defined
        }
        if( populateDefaults == null || "".equals(populateDefaults) || "null".equals(populateDefaults) )
        {
            populateDefaults = populateLocalServerData;
        }


        if( populateDefaults != null && "true".equalsIgnoreCase(populateDefaults) )
        {
            if( strServer == null || "".equals(strServer) )
            {
                strServer = localServer;
            }
            if( strPath == null || "".equals(strPath))
            {
                strPath = localPath;
            }
        }


        String stores = MqlUtil.mqlCommand(context, "list $1","store"); //PRG:RG6:R213:Mql Injection:Static Mql:20-Oct-2011
        StringList storeList = FrameworkUtil.split(stores, "\n");
        Iterator storeItr = storeList.iterator();
        StringList servers = new StringList(1);
        String storeType;
        String storeName;
        while(storeItr.hasNext())
        {
            storeName = (String) storeItr.next();
          //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:start
            String sCommandStatement = "print store $1 select $2 dump";
            storeType = MqlUtil.mqlCommand(context, sCommandStatement,storeName,"type");
          //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:End

            if( "designsync".equalsIgnoreCase(storeType) )
            {
                servers.add(storeName);
            }
        }

        if(mode.equalsIgnoreCase("view"))
        {

            String defaultDocumentPolicyName= PropertyUtil.getSchemaProperty(context,"policy_ProjectSpace");

             if ( strServer == null || "".equals(strServer) || "null".equals(strServer) )
            {
                String vcSelect = "vcfile";
              //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:start
                String sCommandStatement = "print bus $1 select $2 $3 dump $4";
                String storeInfo = MqlUtil.mqlCommand(context, sCommandStatement,objectId,vcSelect+".store",vcSelect+".path","|");
              //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:End

                int index = storeInfo.indexOf("|");
                if (index < 0)
                {
                    strServer = noStore;
                    strPath = "/";
                }
                else
                {
                    strServer = storeInfo.substring(0, index);
                    if ((strServer == null) || (strServer.equals("ADMINISTRATION")) || (strServer.equals("")))
                        strServer = noStore;

                    strPath = storeInfo.substring(index + 1);
                }

            }
            output.append("<input type='text' name='Server' value='"+XSSUtil.encodeForHTMLAttribute(context, strServer)+"' size='30' readonly='readonly'/>");

        }
        else{
            servers.add(noStore);
            output.append("<select id='Server' name='Server'>");
            Iterator serverItr = servers.iterator();
            while(serverItr.hasNext())
            {
                String serverName = (String)serverItr.next();
                if (serverName.equals(strServer))
                {
                    output.append("<option value='"+XSSUtil.encodeForHTMLAttribute(context, strServer)+"' Selected='Selected'>"+serverName+"</option>");
                }
                else
                {
                    output.append("<option value='"+XSSUtil.encodeForHTMLAttribute(context, strServer)+"'>"+serverName+"</option>");
                }
            }
            output.append("</select>");
        }


        return output.toString();
    }





    // To get the DS String ---- Added for Configurable
    // Program Central
    /**
     * This method is used to get the DS Viewer Format
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: objectList - MapList
     *            containing the objects list
     * @return String View Formt list in combobox
     * @throws Exception
     *             if the operation fails
     * @since PMC V6R2008-1
     */

    public String getDSViewerFormat(Context context, String[] args) throws Exception
    {


        StringBuffer output = new StringBuffer();
        String defaultDocumentPolicyName= PropertyUtil.getSchemaProperty(context,"policy_ProjectSpace");
        //String formats = MqlUtil.mqlCommand(context, "print policy '" + defaultDocumentPolicyName + "' select format dump |" ); //PRG:RG6:R213:Mql Injection:Static Mql:20-Oct-2011
        String mqlQueryString = "print policy $1 select $2 dump $3";
        String formats = MqlUtil.mqlCommand(context,mqlQueryString,defaultDocumentPolicyName,"format","|");
        StringList formatList = new StringList();
        formatList = FrameworkUtil.split(formats, "|");
        String strLanguage=context.getSession().getLanguage();
        //String defaultFormat =MqlUtil.mqlCommand(context, "print policy '" + defaultDocumentPolicyName + "' select defaultformat dump |" ); //PRG:RG6:R213:Mql Injection:Static Mql:20-Oct-2011
        mqlQueryString = "print policy $1 select $2 dump $3";
        String defaultFormat =MqlUtil.mqlCommand(context,mqlQueryString,defaultDocumentPolicyName,"defaultformat","|");

        output.append("<select id='format' name='format'>");

        Iterator formatItr = formatList.iterator();
        while(formatItr.hasNext())
        {
            String format = (String)formatItr.next();


            String i18nFormat = i18nNow.getMXI18NString(format, "",strLanguage , "Format");
            if(format.equals(defaultFormat))
            {
                output.append("<option value='"+XSSUtil.encodeForHTMLAttribute(context, format)+"' Selected='Selected' >"+i18nFormat+"</option>");
            }
            else
            {
                output.append("<option value='"+XSSUtil.encodeForHTMLAttribute(context, format)+"'>"+i18nFormat+"</option>");
            }

        }

        output.append("</select>");

        return output.toString();

    }




    // To get the DSS Path ---- Added for Configurable
    // Program Central
    /**
     * This method is used to get the associated DSS Path
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: objectList - MapList
     *            containing the objects list
     * @return String
     * @throws Exception
     *             if the operation fails
     * @since PMC V6R2008-1
     */

    public String getDSPath(Context context, String[] args) throws Exception
    {

        StringBuffer output = new StringBuffer();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String objectId = (String)paramMap.get("objectId");

        String populateDefaults=(String)requestMap.get("populateDefaults");
        String  noStore = EnoviaResourceBundle.getProperty(context, "Components",
                "emxComponents.Common.None", context.getSession().getLanguage());
        String localPath = "";
        String localServer = "";
        String populateLocalServerData = "false";

        String strPath = "";
        String strServer = "";


        try
        {
            localPath = EnoviaResourceBundle.getProperty(context, "emxComponents.LocalDSPath");
            localServer = EnoviaResourceBundle.getProperty(context, "emxComponents.LocalDSServer");
            populateLocalServerData = EnoviaResourceBundle.getProperty(context, "emxComponents.PopulateLocalServerData");
        } catch(Exception ex)
        {
            // Skip because properties might not be defined
        }


        if( populateDefaults == null || "".equals(populateDefaults) || "null".equals(populateDefaults) )
        {
            populateDefaults = populateLocalServerData;
        }


        if( populateDefaults != null && "true".equalsIgnoreCase(populateDefaults) )
        {
            if( strServer == null || "".equals(strServer) )
            {
                strServer = localServer;
            }
            if( strPath == null || "".equals(strPath))
            {
                strPath = localPath;
            }
        }


        String stores = MqlUtil.mqlCommand(context, "list $1","store"); //PRG:RG6:R213:Mql Injection:Static Mql:20-Oct-2011
        StringList storeList = FrameworkUtil.split(stores, "\n");
        Iterator storeItr = storeList.iterator();
        StringList servers = new StringList(1);
        String storeType;
        String storeName;
        while(storeItr.hasNext())
        {
            storeName = (String) storeItr.next();
          //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:start
            String sCommandStatement = "print store $1 select $2 dump";
            storeType = MqlUtil.mqlCommand(context, sCommandStatement,storeName,"type");
          //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:End

            if( "designsync".equalsIgnoreCase(storeType) )
            {
                servers.add(storeName);
            }
        }

        if ( strServer == null || "".equals(strServer) || "null".equals(strServer) )
        {
            String vcSelect = "vcfile";
          //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:start
            String sCommandStatement = "print bus $1 select $2 $3 dump $4";
            String storeInfo = MqlUtil.mqlCommand(context, sCommandStatement, objectId, vcSelect+".store", vcSelect+".path", "|");
          //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:End

            int index = storeInfo.indexOf("|");
            if (index < 0)
            {
                strServer = noStore;
                strPath = "/";
            }
            else
            {
                strServer = storeInfo.substring(0, index);
                if ((strServer == null) || (strServer.equals("ADMINISTRATION")) || (strServer.equals("")))
                    strServer = noStore;

                strPath = storeInfo.substring(index + 1);
            }
        }

        output.append("<input type='text' name='Path' value='"+XSSUtil.encodeForHTMLAttribute(context, strPath)+"' size='30' readonly='readonly'/>");
        return output.toString();

    }



    // To get the Owner Name ---- Added for Configurable
    // Program Central
    /**
     * This method is used to get the Owner Name
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: objectList - MapList
     *            containing the objects list
     * @return String
     * @throws Exception
     *             if the operation fails
     * @since PMC V6R2008-1
     */
    public String getOwnerName(Context context, String args[]) throws Exception
    {
        String output = "";
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String objectId = (String) paramMap.get("objectId");
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strMode = (String) requestMap.get("mode");
        String jsTreeID = (String) requestMap.get("jsTreeID");
        com.matrixone.apps.common.Person person =
            (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
        com.matrixone.apps.program.ProjectConcept projectConcept =
            (com.matrixone.apps.program.ProjectConcept) DomainObject.newInstance(context,
                    DomainConstants.TYPE_PROJECT_CONCEPT,DomainConstants.PROGRAM);
        projectConcept.setId(objectId);
        StringList busSelects = new StringList(2);
        busSelects.add(projectConcept.SELECT_OWNER);
        Map projectMap = projectConcept.getInfo(context, busSelects);
        String projectOwner = (String) projectMap.get(projectConcept.SELECT_OWNER);
        String ownerId = person.getPerson(context, projectOwner).getId();
        StringBuffer sb = new StringBuffer();

        String userName = projectConcept.getInfo(context, projectConcept.SELECT_OWNER);
        person = person.getPerson(context, userName);
        String personId = PersonUtil.getPersonObjectID(context,userName);
        busSelects.clear();
        busSelects.add(person.SELECT_LAST_NAME);
        busSelects.add(person.SELECT_FIRST_NAME);
        Map personFullNameMap = person.getInfo(context, busSelects);
        String strLastName = (String) personFullNameMap.get(person.SELECT_LAST_NAME);
        String strFirstName = (String) personFullNameMap.get(person.SELECT_FIRST_NAME);
        String personName = strLastName + ", " + strFirstName;
        String strURLl="";
        if(strMode.equals("edit"))
        {
            sb.append("<input type=\"text\" name=\"PersonName\" size=\"30\" value=\""+personName+"\" readonly=\"readonly\"/>");
            sb.append("<input type=\"hidden\" name=\"Owner\" value=\""+personId+"\"/>");
            //sb.append("<input type=\"button\" name=\"bType\" size=\"200\" value=\"...\" alt=\"\" onClick=\"performProjectLeadsSearch();\"");
            //sb.append("<input type='button' name='program' value='...' onClick=\"performProjectLeadsSearch()");
            sb.append("<input type=\"button\" name=\"bType\" size=\"200\" value=\"...\" alt=\"\" onClick=\"");
            //output.append("<input type='button' name='program' value='...' onClick=\"");
            sb.append("javascript:showChooser('");
            //output.append("javascript:showChooser('");
            //String strURLl="../common/emxFullSearch.jsp?field=TYPES=type_Program&amp;table=PMCProgramAutonomySearch&amp;selection=multiple&amp;includeOIDprogram=emxProjectSpace:getIncludeOIDforProgramSearch&amp;submitURL=../programcentral/emxProgramCentralAutonomySearchUtil.jsp&amp;fieldNameActual=ProgramId&amp;fieldNameDisplay=ProgramName&amp;companyId="+companyId+"&amp;projectId="+projectConceptId+"&amp;suiteKey=ProgramCentral";
            //String strURLl="../common/emxFullSearch.jsp?field=TYPES=type_Person&table=PMCProgramAutonomySearch&selection=multiple&includeOIDprogram=emxProjectSpace:getIncludeOIDforPersonSearch";
            //Modified:03-June-2010:VM3:R210 PRG:2011x
            //String strURLl="../common/emxFullSearch.jsp?table=PMCCommonPersonSearchTable&field=TYPES=type_Person&form=PMCCommonPersonSearchForm&includeOIDprogram=emxProjectSpace:getIncludeOIDforPersonSearch&searchMode=GeneralPeopleTypeMode&selection=single&objectId="+objectId+"&submitURL=../programcentral/emxProgramCentralAutonomySearchUtil.jsp&fieldNameActual=Owner&fieldNameDisplay=PersonName&projectId="+objectId+"&suiteKey=ProgramCentral";
            //End - Modified:03-June-2010:VM3:R210 PRG:2011x

            //Modified:09-Sept-2010:S4E:R210 PRG:IR-071030V6R2011x/IR-054465V6R2011x
            //Ownership of ProjectSpace can be transferred only to Project Lead and
            //Ownership of Project concept can be transferred to any user with valid PRG role from same company.
            if(projectConcept.isKindOf(context, TYPE_PROJECT_CONCEPT))
            {
                strURLl="../common/emxFullSearch.jsp?field=TYPES=type_Person:USERROLE=Project User&table=PMCCommonPersonSearchTable&form=PMCCommonPersonSearchForm&searchMode=GeneralPeopleTypeMode&selection=single&objectId="+objectId+"&submitURL=../programcentral/emxProgramCentralAutonomySearchUtil.jsp&fieldNameActual=Owner&fieldNameDisplay=PersonName&projectId="+objectId+"&suiteKey=ProgramCentral";
            }
            else{
                strURLl="../common/emxFullSearch.jsp?field=TYPES=type_Person:USERROLE=Project Lead&table=PMCCommonPersonSearchTable&form=PMCCommonPersonSearchForm&searchMode=GeneralPeopleTypeMode&selection=single&objectId="+objectId+"&submitURL=../programcentral/emxProgramCentralAutonomySearchUtil.jsp&fieldNameActual=Owner&fieldNameDisplay=PersonName&projectId="+objectId+"&suiteKey=ProgramCentral";
            }
            //End:Modified:09-Sept-2010:S4E:R210 PRG:IR-071030V6R2011x/IR-054465V6R2011x

            sb.append(strURLl);
            sb.append("','600','600')\"/>");


        }
        else
        {
            sb.append(personName);
        }

        output = sb.toString();
        return output;
    }

    // To get the Type Name ---- Added for Configurable
    // Program Central
    /**
     * This method is used to get the Type Name
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: objectList - MapList
     *            containing the objects list
     * @return String
     * @throws Exception
     *             if the operation fails
     * @since PMC V6R2008-1
     */
     //Added for Bug # 340636 on 9/7/2007 - Begin
    public String getType(Context context, String args[]) throws Exception
    {
        //Added for Change Discipline
        boolean isECHInstalled = com.matrixone.apps.domain.util.FrameworkUtil.isSuiteRegistered(context,"appVersionEnterpriseChange",false,null,null);
        //End added for Change Discipline
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String languageStr = (String) requestMap.get("languageStr");
        String strMode = (String) requestMap.get("mode");
        String objectId = (String) paramMap.get("objectId");
        DomainObject project = DomainObject.newInstance(context, objectId);
        StringList objectSelects = new StringList(1);
        objectSelects.add(DomainConstants.SELECT_TYPE);
        Map projectMap = project.getInfo(context,objectSelects);
        String actualType = (String) projectMap.get(DomainConstants.SELECT_TYPE);
        String i18nType = i18nNow.getTypeI18NString(actualType,languageStr);
        StringList prjSpaceSubTypes = getAllProjectSubTypeNames(context,DomainConstants.TYPE_PROJECT_SPACE);
        StringList prjConceptSubTypes = getAllProjectSubTypeNames(context,DomainConstants.TYPE_PROJECT_CONCEPT);
        String inclusionType = "";
        //Added for Change Discipline
        String exclusionType = "type_Experiment";
        //End added for Change Discipline

        if(prjSpaceSubTypes.contains(actualType)){
            inclusionType = "type_ProjectSpace";
            //Added for Change Discipline
            if(isECHInstalled){
                exclusionType = exclusionType + "," + "type_ChangeProject";
            }
            //End added for Change Discipline
        } else if(prjConceptSubTypes.contains(actualType)){
            inclusionType = "type_ProjectConcept";
        }

        //Added for Change Discipline
        StringList changeProjectSubTypes = new StringList();
        if(isECHInstalled){
        changeProjectSubTypes = getAllProjectSubTypeNames(context,PropertyUtil.getSchemaProperty(context,"type_ChangeProject"));
            if(mxType.isOfParentType(context, actualType, PropertyUtil.getSchemaProperty(context,"type_ChangeProject"))){
                inclusionType = "type_ChangeProject";
                exclusionType = "type_Experiment";
            }
        }
        //End added for Change Discipline

        String strReturnResult = i18nType;
        if(strMode.equals("edit"))
        {
            //One condition added to support ECH Change Disciplines
            if((inclusionType.equals("type_ProjectSpace") && prjSpaceSubTypes.size()>1) || (inclusionType.equals("type_ProjectConcept") && prjConceptSubTypes.size()>1) || (inclusionType.equals("type_ChangeProject") && changeProjectSubTypes.size()>1))
            {
            StringBuffer sb = new StringBuffer();
                sb.append("<input type=\"text\" name=\"Type\" size=\"20\" value=\""+i18nType+"\" readonly=\"readonly\"/>");
            sb.append("<input type=\"hidden\" name=\"ActualType\" value=\""+XSSUtil.encodeForHTML(context,actualType)+"\"/>");
            sb.append("<input type=\"hidden\" name=\"previousType\" value=\""+XSSUtil.encodeForHTML(context,actualType)+"\"/>");
            //Start Exclusion List added to support ECH Change Disciplines
            sb.append("<input type=\"button\" name=\"bType\" size=\"200\" value=\"...\" alt=\"\" onClick=\"javascript:showChooser('../common/emxTypeChooser.jsp?fieldNameDisplay=Type&fieldNameActual=ActualType&formName=editDataForm&SelectType=single&SelectAbstractTypes=true&InclusionList="+inclusionType+"&ExclusionList="+exclusionType+"&ObserveHidden=true&SuiteKey=ProgramCentral&ShowIcons=true',500,400);\"");
            //End Exclusion List added to support ECH Change Disciplines
                strReturnResult = sb.toString();
            }
        }
        return strReturnResult;
    }
     //Added for Bug # 340636 on 9/7/2007 - End

        /**
    * get the Subtypes of the passed aparemeter of Project.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 - objectId - task OID
    * @returns Object
    * @throws Exception if the operation fails
    * @since Program Central V6R2008-1
    * @grade 0
    */

    private StringList getAllProjectSubTypeNames(Context context, String type) throws FrameworkException
    {
        StringList subTypeList = new StringList();
      //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:start
        String sCommandStatement = "print type $1 select $2 dump $3";
        String subTypes = MqlUtil.mqlCommand(context, sCommandStatement,type,"derivative","|");
      //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:End
        if("".equalsIgnoreCase(subTypes)){
            subTypeList.addElement(type);
            return subTypeList;
        } else {
            subTypes = subTypes+"|"+type;
        }
        subTypeList = FrameworkUtil.split(subTypes, "|");
        return subTypeList;
    }

//  To get the Owner Name ---- Added for Configurable
    // Program Central
    /**
     * This method is used to get the Owner Name
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: objectList - MapList
     *            containing the objects list
     * @return String
     * @throws Exception
     *             if the operation fails
     * @since PMC V6R2008-1
     */
    public String getProjectDate(Context context, String args[]) throws Exception
    {
        final String ROW_NAME = "ProjectDate";
        StringBuffer strHTMLBuffer = new StringBuffer(64);
        strHTMLBuffer.append("<input type='text' readonly='readonly' size='' name='").append(XSSUtil.encodeForHTML(context,ROW_NAME)).append("' value=''/>");
        strHTMLBuffer.append("<a href=\"javascript:showCalendar('emxCreateForm', '").append(XSSUtil.encodeForHTML(context,ROW_NAME)).append("', '')\">");
        strHTMLBuffer.append("<img src='../common/images/iconSmallCalendar.gif' border='0' valign='absmiddle'/>");
        strHTMLBuffer.append("</a>");

        return strHTMLBuffer.toString();
    }

}

