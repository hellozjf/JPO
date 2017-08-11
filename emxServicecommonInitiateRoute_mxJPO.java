/*
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
*/

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.util.*;
import java.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;


/**
 * The <code>emxServicecommonInitiateRoute</code> class contains methods to remove the process from the tcl.
 *
 * @version AEF 9.5.0.0 - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxServicecommonInitiateRoute_mxJPO
{
    /** String contains Route Node Relationship. */
    public static final String  sRelRouteNode                =  DomainConstants.RELATIONSHIP_ROUTE_NODE;
    /** String contains Route Task Relationship. */
    public static final String  sRelRouteTask                =  PropertyUtil.getSchemaProperty("relationship_RouteTask");
    /** String contains Project Task Relationship. */
    public static final String  sRelProjectTask              =  PropertyUtil.getSchemaProperty("relationship_ProjectTask");
    /** String contains Object Route Relationship. */
    public static final String  sRelObjectRoute              =  PropertyUtil.getSchemaProperty("relationship_ObjectRoute");
    /** String contains Route Sequence Attribute. */
    public static final String  sAttRouteSequence            =  PropertyUtil.getSchemaProperty("attribute_RouteSequence");
    /** String contains Route Action Attribute. */
    public static final String  sAttRouteAction              =  PropertyUtil.getSchemaProperty("attribute_RouteAction");
    /** String contains Route Instructions Attribute. */
    public static final String  sAttRouteInstructions        =  PropertyUtil.getSchemaProperty("attribute_RouteInstructions");
    /** String contains Approval Status Attribute. */
    public static final String  sAttApprovalStatus           =  PropertyUtil.getSchemaProperty("attribute_ApprovalStatus");
    /** String contains ScheduledCompletionDate Attribute. */
    public static final String  sAttScheduledCompletionDate  =  PropertyUtil.getSchemaProperty("attribute_ScheduledCompletionDate");
    /** String contains ApproversResponsiblility Attribute. */
    public static final String  sAttApproversResponsibility  =  PropertyUtil.getSchemaProperty("attribute_ApproversResponsibility");
    /** String contains RouteNodeID Attribute. */
    public static final String  sAttRouteNodeID              =  PropertyUtil.getSchemaProperty("attribute_RouteNodeID");
    /** String contains CurrentRouteNode Attribute. */
    public static final String  sAttCurrentRouteNode         =  PropertyUtil.getSchemaProperty("attribute_CurrentRouteNode");
    /** String contains RouteStatus Attribute. */
    public static final String  sAttRouteStatus              =  PropertyUtil.getSchemaProperty("attribute_RouteStatus");
    /** String contains Title Attribute. */
    public static final String  sAttTitle                    =  PropertyUtil.getSchemaProperty("attribute_Title");
    /** String contains AllowDelegation Attribute. */
    public static final String  sAttAllowDelegation          =  PropertyUtil.getSchemaProperty("attribute_AllowDelegation");
    /** String contains ReviewTask Attribute. */
    public static final String  sAttReviewTask               =  PropertyUtil.getSchemaProperty("attribute_ReviewTask");
    /** String contains AssigneeSetDueDate Attribute. */
    public static final String  sAttAssigneeDueDateOpt       =  PropertyUtil.getSchemaProperty("attribute_AssigneeSetDueDate");
    /** String contains DueDateOffset Attribute. */
    public static final String  sAttDueDateOffset            =  PropertyUtil.getSchemaProperty("attribute_DueDateOffset");
    /** String contains DateOffsetFrom Attribute. */
    public static final String  sAttDueDateOffsetFrom        =  PropertyUtil.getSchemaProperty("attribute_DateOffsetFrom");
    /** String contains TemplateTask Attribute. */
    public static final String  sAttTemplateTask             =  PropertyUtil.getSchemaProperty("attribute_TemplateTask");
    /** String contains FirstName Attribute. */
    public static final String  sAttFirstName                =  PropertyUtil.getSchemaProperty("attribute_FirstName");
    /** String contains LastName Attribute. */
    public static final String  sAttLastName                 =  PropertyUtil.getSchemaProperty("attribute_LastName");
    /** String contains AbsenceStartDate Attribute. */
    public static final String  sAttAbsenceStartDate         =  PropertyUtil.getSchemaProperty("attribute_AbsenceStartDate");
    /** String contains AbsenceEndDate Attribute. */
    public static final String  sAttAbsenceEndDate           =  PropertyUtil.getSchemaProperty("attribute_AbsenceEndDate");
    /** String contains AbsenceDelegate Attribute. */
    public static final String  sAttAbsenceDelegate          =  PropertyUtil.getSchemaProperty("attribute_AbsenceDelegate");
    /** String contains RouteTaskUser Attribute. */
    public static final String  sAttRouteTaskUser            =  PropertyUtil.getSchemaProperty("attribute_RouteTaskUser");
    /** String contains RouteTaskUserCompany Attribute. */
    public static final String  sAttRouteTaskUserCompany     =  PropertyUtil.getSchemaProperty("attribute_RouteTaskUserCompany");
    /** String contains Person Type. */
    public static final String  sTypePerson                  =  PropertyUtil.getSchemaProperty("type_Person");
    /** String contains RouteTaskUser Type. */
    public static final String  sTypeRouteTaskUser           =  PropertyUtil.getSchemaProperty("type_RouteTaskUser");
    /** String contains person WorkspaceAccessGrantor. */
    public static final String  sWorkspaceAccessGrantor      =  PropertyUtil.getSchemaProperty("person_WorkspaceAccessGrantor");
    /** String contains person RouteDelegationGrantor. */
    public static final String  sRouteDelegationGrantor      =  PropertyUtil.getSchemaProperty("person_RouteDelegationGrantor");
    /** String contains InboxTask Type. */
    public static final String  typeTask                     = PropertyUtil.getSchemaProperty("type_InboxTask");
    /** String contains InboxTask Policy. */
    public static final String  policy                       = PropertyUtil.getSchemaProperty("policy_InboxTask");


    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */

    public emxServicecommonInitiateRoute_mxJPO (Context context, String[] args)
        throws Exception
    {
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int 0, status code
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */

    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            String languageStr = context.getSession().getLanguage();
            String exMsg = i18nNow.getI18nString("emxFramework.Message.Invocation","emxFrameworkStringResource",languageStr);
            exMsg += i18nNow.getI18nString("emxFramework.InitiateRoute","emxFrameworkStringResource",languageStr);
            throw new Exception(exMsg);
        }
        return 0;
    }

    /**
     * eServicecommonInitiateRoute method is to remove the process from the tcl.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    0 - String which contains Type of object
     *    1 - String which contains Name of object
     *    2 - String which contains Revision of object
     *    3 - int which contains RouteSequence value
     *    4 - int which contains ErrorOnNoConnection value
     * @return int 0 for success otherwise 1
     * @since AEF 10 minor 1
     */

     public static int eServicecommonInitiateRoute (Context context,String args[])
     {
        //varibales Declaration
        String sRouteNodeId             = "";
        String sRouteAction             = "";
        String sApprovalStatus          = "";
        String sScheduledCompletionDate = "";
        String sApproversResponsibility = "";
        String sTitle                   = "";
        String sAllowDelegation         = "";
        String sReviewTask              = "";
        String sAssigneeDueDateOpt      = "";
        String sDueDateOffString        = "";
        String sDueDateOffsetFrom       = "";
        String sTemplateTask            = "";
        String sRouteTaskUser           = "";
        String sRouteTaskUserCompany    = "";
        String sRouteNodeIdAttr         = "";
        String sPersonId                = "";
        String sPersonName              = "";
        String sPersonVault             = "";
        String sAbsenceDelegate         = "";
        String sPersonFirstName         = "";
        String sPersonLastName          = "";
        String sRouteInstructions = "";
        String sRPEType = "";
        String sRPEName  = "";
        String sRPERev  = "";
        String strTreeMenu="";

        StringBuffer strMQL = null;
        String sDelegatorName = "";
        String sDelegatorId   = "";
        String sDelegatorFullName ="";
        String sGroupOrRole = "";
        String sGroupOrRoleTitle = "";
        String sGRName ="";
        String sRPEOwner = "";
        String sRouteTaskUserValue ="";
        String sObjectId= "";
        String lGrantor ="";
        String lGrantee = "";
        String lGranteeAccess ="";
        String sInboxTaskId   ="";
        String sInboxTaskType ="";
        String sInboxTaskName  ="";
        String sInboxTaskRev  ="";
        String sRouteObjects= "";
        int bPersonFound=0;
        int fGranteeLookup = 0;
        String Message = "";
        // eof Variables Declaration
        try
        {
             ContextUtil.startTransaction(context, true);
            // getting the type from arguments[0]
            String sType= args[0];
            // getting the Name from arguments[1]
            String sName = args[1];
            // getting the Revision from arguments[2]
            String sRev = args[2];
            // The Task Sequence to Execute
            int sRouteSequenceValue =Integer.parseInt(args[3]);
            int bErrorOnNoConnection =Integer.parseInt(args[4]);

            // declaring String array because emxMailUtil JPO needs treeMenu as a String array
            String sTreeMenu[]  = new String[1];
            //Building the Task Object
            DomainObject inboxTask = new DomainObject();

            //Building the Object of the Mail Util to be used Later
            emxMailUtil_mxJPO mailUtil = new emxMailUtil_mxJPO(context, null);
            //Building the Object of the emxContextUtil to be used Later
            emxContextUtil_mxJPO emxContextUtilObj = new emxContextUtil_mxJPO(context, null);

            //to get the Route Id and the Route Owner
           String mqlret = MqlUtil.mqlCommand(context,"print bus $1 $2 $3 select $4 $5 dump $6", sType, sName,sRev, "id", "owner", "|");
            // getting the Route Id from the output
            String sRouteId    =  mqlret.substring(0,mqlret.indexOf("|"));
            //getting the Route Owner
            String sRouteOwner =  mqlret.substring((mqlret.indexOf("|")+1),mqlret.length());
            // building the Route Object for getting the Related Objects using getRelatedObjects method
            DomainObject Route = new DomainObject(sRouteId);
            // objects Selects
            StringList objectSelects = new StringList();
            objectSelects.addElement(DomainConstants.SELECT_ID);
            objectSelects.addElement(DomainConstants.SELECT_TYPE);
            objectSelects.addElement(DomainConstants.SELECT_NAME);
            objectSelects.addElement(DomainConstants.SELECT_REVISION);
            // relationship Selects
            StringList relationshipSelects = new StringList();
            // String Buffer to append the type name Revision of the Objects which are connected to Route
            // by Object Route Relationship
            StringBuffer sBufRouteObjects = new StringBuffer(50);
            //getting Objects which are Connected with object Route Relationship
            MapList sRouteObjectsMaplist = Route.getRelatedObjects(context,
                                                                   sRelObjectRoute,
                                                                   "*",
                                                                   objectSelects,
                                                                   relationshipSelects,
                                                                   true,
                                                                   false,
                                                                   (short)1,
                                                                   "",
                                                                   "");
            for(int i=0; i< sRouteObjectsMaplist.size(); i++)
            {
              // Map of the Objects
              Map mapRouteObjects= (Map)sRouteObjectsMaplist.get(i);
             //appending the Type name and revision to the String buffer
             sBufRouteObjects.append(mapRouteObjects.get(DomainConstants.SELECT_TYPE)+" "+mapRouteObjects.get(DomainConstants.SELECT_NAME)+""+mapRouteObjects.get(DomainConstants.SELECT_REVISION)+"\n");
            }
            //Converting the StringBuffer content to String
            sRouteObjects = sBufRouteObjects.toString();
           //object Selects
           SelectList routeNodeObjectSelects = new SelectList();
           routeNodeObjectSelects.addElement(DomainConstants.SELECT_ID);
           routeNodeObjectSelects.addElement(DomainConstants.SELECT_NAME);
           routeNodeObjectSelects.addElement(DomainConstants.SELECT_VAULT);
           routeNodeObjectSelects.addElement("attribute["+sAttAbsenceDelegate+"]");
           routeNodeObjectSelects.addElement("attribute["+sAttFirstName+"]");
           routeNodeObjectSelects.addElement("attribute["+sAttLastName+"]");
           //relationship selects
           SelectList routeNodeRelationshipSelects = new SelectList();
           routeNodeRelationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
           routeNodeRelationshipSelects.addElement("attribute["+sAttRouteSequence+"]");
           routeNodeRelationshipSelects.addElement("attribute["+sAttRouteAction+"]");
           routeNodeRelationshipSelects.addElement("attribute["+sAttApprovalStatus+"]");
           routeNodeRelationshipSelects.addElement("attribute["+sAttScheduledCompletionDate+"]");
           routeNodeRelationshipSelects.addElement("attribute["+sAttApproversResponsibility+"]");
           routeNodeRelationshipSelects.addElement("attribute["+sAttTitle+"]");
           routeNodeRelationshipSelects.addElement("attribute["+sAttAllowDelegation+"]");
           routeNodeRelationshipSelects.addElement("attribute["+sAttReviewTask+"]");
           routeNodeRelationshipSelects.addElement("attribute["+sAttAssigneeDueDateOpt+"]");
           routeNodeRelationshipSelects.addElement("attribute["+sAttDueDateOffset+"]");
           routeNodeRelationshipSelects.addElement("attribute["+sAttDueDateOffsetFrom+"]");
           routeNodeRelationshipSelects.addElement("attribute["+sAttTemplateTask+"]");
           routeNodeRelationshipSelects.addElement("attribute["+sAttRouteTaskUser+"]");
           routeNodeRelationshipSelects.addElement("attribute["+sAttRouteTaskUserCompany+"]");
           //routeNodeRelationshipSelects.addElement("attribute["+sAttRouteNodeID+"]);
           routeNodeRelationshipSelects.addElement("attribute["+sAttRouteInstructions+"]");

           MapList routedNodeObjects = Route.getRelatedObjects(context,
                                                               sRelRouteNode,
                                                               "*",
                                                               routeNodeObjectSelects,
                                                               routeNodeRelationshipSelects,
                                                               false,
                                                               true,
                                                               (short)1,
                                                               "",
                                                               "");
         //Sorting the Maplist Based on the Route Sequence.
         routedNodeObjects.sort("attribute["+sAttRouteSequence+"]","ascending","String");
         // routedNodeObjects.add(RoutedItems);
         //checking whether there are any objects Connected to Route using Route Node Relationship
         if (routedNodeObjects.size() == 0) {
            String [] mailArguments = new String [33];
            mailArguments[0] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.NoObjectError";
            mailArguments[1] = "4";
            mailArguments[2] = "Person";
            mailArguments[3] = sTypePerson;
            mailArguments[4] = "Type";
            mailArguments[5] = sType;
            mailArguments[6] = "Name";
            mailArguments[7] = sName;
            mailArguments[8] = "Rev";
            mailArguments[9] = sRev;
            Message = emxMailUtil_mxJPO.getMessage(context,mailArguments);
            if(Message.length() >0)
            {
            	MqlUtil.mqlCommand(context, "notice $1", Message);
                return 1;
             }
          //  throw new MatrixException(exceptionMessage);
            return 1;
          }
          /* Check if Person or Group/Role with required sequence order is present
           * If not then increament sequence order till Person or Group/Role found
           * If no person or group/role found then error out.
           */
          int bGreaterSeqNoFound =1;
          while (bGreaterSeqNoFound == 1)
          {
              bGreaterSeqNoFound = 0;
              for(int i=0; i< routedNodeObjects.size(); i++)
              {
                  Map objectMap= (Map)routedNodeObjects.get(i);
                  // getting the Sequence no of the Task
                  String sRouteSequence = (String) objectMap.get("attribute["+sAttRouteSequence+"]");
                  int routeSequence =Integer.parseInt(sRouteSequence);
                  if (routeSequence > sRouteSequenceValue) {
                      bGreaterSeqNoFound=1;
                  }

                  // Checking whether the passed sequence and the object sequence are same
                  // if same only do the below operation
                  if (routeSequence == sRouteSequenceValue) {
                      bPersonFound = 1;
                      // getting all the attribute Values from the Map
                      sRouteNodeId             = (String)objectMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                      sRouteAction             = (String)objectMap.get("attribute["+sAttRouteAction+"]");
                      sApprovalStatus          = (String)objectMap.get("attribute["+sAttApprovalStatus+"]");
                      sScheduledCompletionDate = (String)objectMap.get("attribute["+sAttScheduledCompletionDate+"]");
                      sApproversResponsibility = (String)objectMap.get("attribute["+sAttApproversResponsibility+"]");
                      sTitle                   = (String)objectMap.get("attribute["+sAttTitle+"]");
                      sAllowDelegation         = (String)objectMap.get("attribute["+sAttAllowDelegation+"]");
                      sReviewTask              = (String)objectMap.get("attribute["+sAttReviewTask+"]");
                      sAssigneeDueDateOpt      = (String)objectMap.get("attribute["+sAttAssigneeDueDateOpt+"]");
                      sDueDateOffString        = (String)objectMap.get("attribute["+sAttDueDateOffset+"]");
                      sDueDateOffsetFrom       = (String)objectMap.get("attribute["+sAttDueDateOffsetFrom+"]");
                      sTemplateTask            = (String)objectMap.get("attribute["+sAttTemplateTask+"]");
                      sRouteTaskUser           = (String)objectMap.get("attribute["+sAttRouteTaskUser+"]");
                      sRouteTaskUserCompany    = (String)objectMap.get("attribute["+sAttRouteTaskUserCompany+"]");
                      sRouteNodeIdAttr         = (String)objectMap.get("attribute["+sAttRouteNodeID+"]");
                      sPersonId                = (String)objectMap.get(DomainConstants.SELECT_ID);
                      sPersonName              = (String)objectMap.get(DomainConstants.SELECT_NAME);
                      sPersonVault             = (String)objectMap.get(DomainConstants.SELECT_VAULT);
                      sAbsenceDelegate         = (String)objectMap.get("attribute["+sAttAbsenceDelegate+"]");
                      sPersonFirstName         = (String)objectMap.get("attribute["+sAttFirstName+"]");
                      sPersonLastName          = (String)objectMap.get("attribute["+sAttLastName+"]");
                      sRouteInstructions       = (String)objectMap.get("attribute["+sAttRouteInstructions+"]");

                      // Get multiline attribute Route Instuction from connection id
                      sRPEType = MqlUtil.mqlCommand(context, "get env $1", "TYPE");
                      sRPEName  = MqlUtil.mqlCommand(context,"get env $1", "NAME");
                      sRPERev  = MqlUtil.mqlCommand(context,"get env $1", "REVISION");
                      strTreeMenu= MqlUtil.mqlCommand(context, "get env $1 $2","global", "MX_TREE_MENU");
                      sTreeMenu[0] = strTreeMenu;
                      if (!strTreeMenu.equals("")) {
                         MqlUtil.mqlCommand(context, "set env $1 $2 $3", "global", "MX_TREE_MENU", strTreeMenu);
                      }
                      // Create a Inbox Task
                      MQLCommand commandInbox = new MQLCommand();
                      commandInbox.open(context);
                      strMQL = new StringBuffer(80);
                      strMQL.append("execute program $1 $2 $3 $4 $5");
                      commandInbox.executeCommand(context,strMQL.toString(), "eServicecommonNumberGenerator.tcl", typeTask ,"", policy, "Null");
                      String errinbox = commandInbox.getError();
                      String strInboxRes = commandInbox.getResult().trim();
                      commandInbox.close(context);
                      StringTokenizer token = new StringTokenizer(strInboxRes,"|",false);
                      String sInboxErrorCode = (token.nextToken()).trim();
                      if(errinbox.length() != 0){
                          return 1;
                      }
                      // getting the Task id,type,name,Rev from tokenizing the output of the mql.
                      sInboxTaskId   =(token.nextToken()).trim();
                      sInboxTaskType =(token.nextToken()).trim();
                      sInboxTaskName  =(token.nextToken()).trim();
                      sInboxTaskRev  =(token.nextToken()).trim();
                      // setting the id of the task.
                      inboxTask.setId(sInboxTaskId);
                     // Before connecting the task to the person, check to see if
                     // delegation is required:
                     sDelegatorId = "";
                     // checking if the absenceDelegate attribute value not equals "" and
                     // allow delegation equals to true. if both conditiond are met do the belw Operation
                     if (!sAbsenceDelegate.equals("") && sAllowDelegation.equals("TRUE"))
                     {

                        // Rather than dealing with date/time formats, get the current
                        //time from the server.  The RPE timestamp available in triggers
                        //is not necessarily the same format of the server.
                        SelectList taskSelectList = new SelectList(1);
                        taskSelectList.add("originated");
                        Map taskMap = inboxTask.getInfo(context,taskSelectList);
                        String sCurrentTime =  (String)taskMap.get("originated");
                        SelectList objectSelects1= new SelectList();
                        objectSelects1.addElement(DomainConstants.SELECT_ID);
                        StringBuffer strWhere = new StringBuffer(80);
                        strWhere.append("attribute[");
                        strWhere.append(sAttAbsenceStartDate);
                        strWhere.append("] <='");
                        strWhere.append(sCurrentTime);
                        strWhere.append("' && attribute[");
                        strWhere.append(sAttAbsenceEndDate);
                        strWhere.append("] >='");
                        strWhere.append(sCurrentTime);
                        strWhere.append("'");
                        // find the person object
                        MapList absenceMap=  DomainObject.findObjects(context,
                                                                      sTypePerson,
                                                                      sPersonName,
                                                                      "*",
                                                                     "*",
                                                                     sPersonVault,
                                                                     strWhere.toString(),
                                                                     false,
                                                                     objectSelects1);
                        // if the person has absence set start and end date set the map size will be greater than 0
                        if (absenceMap.size()!= 0) {
                         /* The person is absent; delegate the person's task
                          * Just re-set sPersonId & sPersonName to the new person
                          * Look up this new person
                          */
                          StringList objectSelect = new StringList();
                          objectSelect.addElement(DomainConstants.SELECT_ID);
                          objectSelect.addElement("attribute["+sAttFirstName+"]");
                          objectSelect.addElement("attribute["+sAttLastName+"]");
                          // find the details of the Delegate
                          MapList personMapList=  DomainObject.findObjects(context,
                                                                           sTypePerson,
                                                                           sAbsenceDelegate,
                                                                           "*",
                                                                           "*",
                                                                           "*",
                                                                           "",
                                                                           false,
                                                                           objectSelect);
                          sDelegatorName =sAbsenceDelegate;
                          Map personMap= (Map)personMapList.get(0);
                          sDelegatorId   = (String)personMap.get(DomainConstants.SELECT_ID);
                          sDelegatorFullName = (String)personMap.get("attribute["+sAttFirstName+"]")+(String)personMap.get("attribute["+sAttLastName+"]");
                          // Change route node relationship to reflect new delegator
                          MQLCommand updateCommand = new MQLCommand();
                          updateCommand.open(context);
                          String strCommand ="modify connection $1 to $2";
                          updateCommand.executeCommand(context, strCommand, sRouteNodeId, sDelegatorId);
                          String errUpdate = updateCommand.getError();
                          String strResUpdate = updateCommand.getResult().trim();
                          updateCommand.close(context);
                          if(errUpdate.length() != 0){
                          //throw new MatrixException("Error connecting Delegator ");
                            return 1;
                          }
                          /* Grant access to the route and route objects to new person
                           * based on access to original assignee.  Do this by looking
                           * up grants from 'Workspace Access Grantor' and granting
                           * to new user using 'Route Delegation Grantor'.
                           */
                          MapList lGrants = new MapList();
                          if (fGranteeLookup == 0) {
                             fGranteeLookup = 1;
                             for(int i1=0; i1< sRouteObjectsMaplist.size(); i1++)
                             {
                               Map mapRouteObjects= (Map)sRouteObjectsMaplist.get(i1);
                               sObjectId = (String)mapRouteObjects.get(DomainConstants.SELECT_ID);
                               if(!UIUtil.isNullOrEmpty(sObjectId)){
                                 lGrantor = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3",sObjectId, "grantor","|");
                               }
                               if(lGrantor.indexOf("|") !=-1)
                               {
                                 lGrantor = lGrantor.substring(0,lGrantor.indexOf("|"));
                               }
                              if (UIUtil.isNullOrEmpty(lGrantor)) {continue;}
                              StringTokenizer stk = null;
                              String output = "";
                              if(!UIUtil.isNullOrEmpty(sObjectId)){
                                 output = MqlUtil.mqlCommand(context,"print bus $1 select $2 $3 dump $4",sObjectId, "grantee","granteeaccess", "|");
                              } 
                              if(!UIUtil.isNullOrEmpty(output)){
                              stk = new StringTokenizer(output,"|");
                              if(stk.hasMoreTokens())
                              {
                                 lGrantee = stk.nextToken();
                                 lGranteeAccess =  stk.nextToken();
                              }
                              HashMap grantMap= new HashMap();
                              grantMap.put("sObjectId",sObjectId);
                              grantMap.put("lGrantor",lGrantor);
                              grantMap.put("lGrantee",lGrantee);
                              grantMap.put("lGranteeAccess",lGranteeAccess);
                              lGrants.add(grantMap);
                              }
                            }//eof for loop
                         }//. eof if loop





                         for(int i2=0; i2< lGrants.size(); i2++)
                         {
                           HashMap grantMap= (HashMap) lGrants.get(i2);
                           sObjectId = (String)grantMap.get("sObjectId");
                           lGrantor = (String)grantMap.get("lGrantor");
                           lGrantee = (String)grantMap.get("lGrantee");
                           lGranteeAccess = (String)grantMap.get("lGranteeAccess");
                           if (lGrantor == sWorkspaceAccessGrantor && lGrantee == sPersonName) {
                             StringBuffer mqlCmd = new StringBuffer(50);
                             MqlUtil.mqlCommand(context,"mql modify bus $1 grant $2 access $3",sObjectId, sDelegatorName,lGranteeAccess);
                           }
                         }
                         if (lGrants.size() != 0) {
                            emxContextUtilObj.popContext(context,null);// popShadowAgent
                         }
                         // # Override old assignee id and name to new delegator
                         sPersonId  = sDelegatorId;
                         sPersonName  =sDelegatorName;
                      }// eof absence Map .size(0
                    }
                   // Connect Inbox Task via Project Task relationship to the Person or RTU the Route is attached to
                   DomainObject personObject = new DomainObject(sPersonId);
                   personObject.open(context);
                   personObject.close(context);
                   DomainRelationship.connect(context, inboxTask, sRelProjectTask,personObject);
                   //# set person variable to group/role if applicable
                   if (!sRouteTaskUser.equals("")) {
                     sGroupOrRole = sRouteTaskUser.substring(sRouteTaskUser.indexOf("_"),sRouteTaskUser.length());
                    // sRouteTaskUserValue    = Framework.getPropertyValue(sRouteTaskUser);
                     sPersonName =sRouteTaskUserValue;
                     sGroupOrRoleTitle = sGroupOrRole.toUpperCase();
                     sGRName =sGroupOrRoleTitle +"-"+sPersonName;
                   }
                  // Updating the Owner
                  inboxTask.setOwner(context,sPersonName);
                  /*
                   * Copy 'Route Action' 'Route Instructions' 'Approval Status' 'Scheduled Completion Date'
                   *      'Route Task User' 'Route Task User Company'
                   *      'Review Task'  'Assignee Set Due Date'
                   *      'Due Date Offset' 'Date Offset From'
                   *      'Approvers Responsibility' attributes from relationship Route Node to Inbox Tasxk
                   * Set 'Route Node Id' attribute of Inbox Task to 'Route Node' Relationship ID
                   */
                  HashMap attributesMap  = new HashMap();
                  attributesMap.put(sAttRouteAction,sRouteAction);
                  attributesMap.put(sAttRouteInstructions,sRouteInstructions);
                  attributesMap.put(sAttApprovalStatus,sApprovalStatus);
                  attributesMap.put(sAttScheduledCompletionDate,sScheduledCompletionDate);
                  attributesMap.put(sAttApproversResponsibility,sApproversResponsibility);
                  attributesMap.put(sAttRouteNodeID,sRouteNodeIdAttr);
                  attributesMap.put(sAttTitle,sTitle);
                  attributesMap.put(sAttAllowDelegation,sAllowDelegation);
                  attributesMap.put(sAttReviewTask,sReviewTask);
                  attributesMap.put(sAttAssigneeDueDateOpt,sAssigneeDueDateOpt);
                  attributesMap.put(sAttDueDateOffset,sDueDateOffString);
                  attributesMap.put(sAttDueDateOffsetFrom,sDueDateOffsetFrom);
                  attributesMap.put(sAttTemplateTask,sTemplateTask);
                  attributesMap.put(sAttRouteTaskUser,sRouteTaskUser);
                  attributesMap.put(sAttRouteTaskUserCompany,sRouteTaskUserCompany);
                  // updating the attibutes
                  inboxTask.setAttributeValues(context, attributesMap);
                  // connecting the inbox task object to Route
                  DomainRelationship.connect(context,inboxTask ,sRelRouteTask,Route);
                  // Send Icon mail to the Group or Role if applicable.
                 if ((sRPEType.equals(sInboxTaskType)) && UIUtil.isNotNullAndNotEmpty(sRouteTaskUser))
                 {
                   String mqlReturn = MqlUtil.mqlCommand(context,"expand bus $1 $2 $3 from relationship $4 dump $5",sRPEType, sRPEName, sRPERev,sRelProjectTask, "|");
				   if (!UIUtil.isNullOrEmpty(mqlReturn)) {
					StringTokenizer ownerString = new StringTokenizer(mqlReturn, "|");
					ownerString.nextToken();
					ownerString.nextToken();
					ownerString.nextToken();
					ownerString.nextToken();
					sRPEOwner = ownerString.nextToken();
				  }
                  if (UIUtil.isNullOrEmpty(sRouteObjects))
                  {
                   //Construct String array to send mail notification
                    String [] mailArguments = new String [33];
                    mailArguments[0] = sPersonName;
                    mailArguments[1] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.SubjectNotice3";
                    mailArguments[2] = "2";
                    mailArguments[3] = "Name";
                    mailArguments[4] = sPersonName;
                    mailArguments[5] = "GroupOrRole";
                    mailArguments[6] = sGroupOrRoleTitle;
                    mailArguments[7] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.MessageNotice2";
                    mailArguments[8] = "12";
                    mailArguments[9] = "IBType";
                    mailArguments[10] = sInboxTaskType;
                    mailArguments[11] = "IBName";
                    mailArguments[12] = sInboxTaskName;
                    mailArguments[13] = "IBRev";
                    mailArguments[14]= sInboxTaskRev;
                    mailArguments[13] = "GRName";
                    mailArguments[14]= sGRName;
                    mailArguments[15] = "RPEType";
                    mailArguments[16]=sRPEType;
                    mailArguments[17] = "RPEName";
                    mailArguments[18]=sRPEName;
                    mailArguments[19] = "RPERev";
                    mailArguments[20]=sRPERev;
                    mailArguments[21] = "RPEOwner";
                    mailArguments[22]=sRPEOwner;
                    mailArguments[23] = "Type";
                    mailArguments[24]=sType;
                    mailArguments[25] = "Name";
                    mailArguments[26]=sName;
                    mailArguments[27] = "Rev";
                    mailArguments[28]=sRev;
                    mailArguments[29] = "ROwner";
                    mailArguments[30]=sRouteOwner;
                    mailArguments[31] = sInboxTaskId;
                    mailArguments[32] = "";
                    //To set Tree Menu
                    emxMailUtil_mxJPO.setTreeMenuName(context, sTreeMenu);
                    //Calling method to send Mail
                    emxMailUtil_mxJPO.sendNotificationToUser(context,mailArguments);
                  } else {
                           String [] mailArguments = new String [35];
                           mailArguments[0] = sPersonName;
                           mailArguments[1] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.SubjectNotice3";
                           mailArguments[2] = "2";
                           mailArguments[3] = "Name";
                           mailArguments[4] = sPersonName;
                           mailArguments[5] = "GroupOrRole";
                           mailArguments[6] = sGroupOrRoleTitle;
                           mailArguments[7] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.MessageNotice2";
                           mailArguments[8] = "13";
                           mailArguments[9] = "IBType";
                           mailArguments[10] = sInboxTaskType;
                           mailArguments[11] = "IBName";
                           mailArguments[12] = sInboxTaskName;
                           mailArguments[13] = "IBRev";
                           mailArguments[14]= sInboxTaskRev;
                           mailArguments[13] = "GRName";
                           mailArguments[14]= sGRName;
                           mailArguments[15] = "RPEType";
                           mailArguments[16]=sRPEType;
                           mailArguments[17] = "RPEName";
                           mailArguments[18]=sRPEName;
                           mailArguments[19] = "RPERev";
                           mailArguments[20]=sRPERev;
                           mailArguments[21] = "RPEOwner";
                           mailArguments[22]=sRPEOwner;
                           mailArguments[23] = "Type";
                           mailArguments[24]=sType;
                           mailArguments[25] = "Name";
                           mailArguments[26]=sName;
                           mailArguments[27] = "Rev";
                           mailArguments[28]=sRev;
                           mailArguments[29] = "ROwner";
                           mailArguments[30]=sRouteOwner;
                           mailArguments[31] = "RObjects";
                           mailArguments[32] = sRouteObjects;
                           mailArguments[33] = sInboxTaskId;
                           mailArguments[34] = "";
                           //To set Tree Menu
                           emxMailUtil_mxJPO.setTreeMenuName(context, sTreeMenu);
                           //Calling method to send Mail
                           emxMailUtil_mxJPO.sendNotificationToUser(context,mailArguments);
                        }
              } else if (!(sRPEType.equals(sInboxTaskType)) && UIUtil.isNotNullAndNotEmpty(sRouteTaskUser))  {
                  if (UIUtil.isNullOrEmpty(sRouteObjects))
                  {
                        String [] mailArguments = new String[26];
                        mailArguments[0]= sPersonName;
                        mailArguments[1]="emxFramework.ProgramObject.eServicecommonInitiateRoute.SubjectNotice3";
                        mailArguments[2]="2";
                        mailArguments[3]="Name";
                        mailArguments[4]=sPersonName;
                        mailArguments[5]="GroupOrRole";
                        mailArguments[6]=sGroupOrRoleTitle;
                        mailArguments[7]="emxFramework.ProgramObject.eServicecommonInitiateRoute.FirstMessage2";
                        mailArguments[8]="8";
                        mailArguments[9]="IBType";
                        mailArguments[10]=sInboxTaskType;
                        mailArguments[11]="IBName";
                        mailArguments[12]=sInboxTaskName;
                        mailArguments[13]="IBRev";
                        mailArguments[14]=sInboxTaskRev;
                        mailArguments[15]="GRName";
                        mailArguments[16]=sGRName;
                        mailArguments[17]="Type";
                        mailArguments[18]= sType;
                        mailArguments[19]="Name";
                        mailArguments[20]= sName;
                        mailArguments[21]="Rev";
                        mailArguments[22]=sRev;
                        mailArguments[23]="ROwner";
                        mailArguments[23]=sRouteOwner;
                        mailArguments[24]=sInboxTaskId;
                        mailArguments[25]="";
                        emxMailUtil_mxJPO.setTreeMenuName(context,sTreeMenu);
                        emxMailUtil_mxJPO.sendNotificationToUser(context,mailArguments);
                  } else {
                           String [] mailArguments = new String [27];
                           mailArguments[0] = sPersonName;
                           mailArguments[1] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.SubjectNotice3";
                           mailArguments[2] = "2";
                           mailArguments[3] = "Name";
                           mailArguments[4] = sPersonName;
                           mailArguments[5] = "GroupOrRole";
                           mailArguments[6] = sGroupOrRoleTitle;
                           mailArguments[7] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.FirstMessageWithObjects2";
                           mailArguments[8] = "9";
                           mailArguments[9] = "IBType";
                           mailArguments[10] = sInboxTaskType;
                           mailArguments[11] = "IBName";
                           mailArguments[12] = sInboxTaskName;
                           mailArguments[13] = "IBRev";
                           mailArguments[14]= sInboxTaskRev;
                           mailArguments[13] = "GRName";
                           mailArguments[14]= sGRName;
                           mailArguments[15] = "Type";
                           mailArguments[16]=sType;
                           mailArguments[17] = "Name";
                           mailArguments[18]=sName;
                           mailArguments[19] = "Rev";
                           mailArguments[20]=sRev;
                           mailArguments[21] = "ROwner";
                           mailArguments[22]=sRouteOwner;
                           mailArguments[23] = "RObjects";
                           mailArguments[24] = sRouteObjects;
                           mailArguments[25] = sInboxTaskId;
                           mailArguments[26] = "";
                           emxMailUtil_mxJPO.setTreeMenuName(context,sTreeMenu);
                           emxMailUtil_mxJPO.sendNotificationToUser(context,mailArguments);
                    }
              }

              //Send Icon mail to the Person if applicable.

               if (sRPEType == sInboxTaskType && UIUtil.isNullOrEmpty(sRouteTaskUser)) {
            	  String mqlReturn = MqlUtil.mqlCommand(context,"expand bus $1 $2 $3 from relationship $4 dump $5",sRPEType, sRPEName, sRPERev,sRelProjectTask, "|");
			      if (!UIUtil.isNullOrEmpty(mqlReturn)) {
				    StringTokenizer ownerString = new StringTokenizer(mqlReturn, "|");
				    ownerString.nextToken();
				    ownerString.nextToken();
				    ownerString.nextToken();
				    ownerString.nextToken();
				    sRPEOwner = ownerString.nextToken();
			      }
                  if (UIUtil.isNullOrEmpty(sRouteObjects)) {
                      if (UIUtil.isNullOrEmpty(sDelegatorId)) {
                           String [] mailArguments = new String [29];
                    mailArguments[0] = sPersonName;
                    mailArguments[1] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.SubjectNotice";
                    mailArguments[2] = "0";
                    mailArguments[3] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.MessageNotice";
                    mailArguments[4] = "11";
                    mailArguments[5] = "IBType";
                    mailArguments[6] = sInboxTaskType;
                    mailArguments[7] = "IBName";
                    mailArguments[8] = sInboxTaskName;
                    mailArguments[9] = "IBRev";
                    mailArguments[10]= sInboxTaskRev;
                    mailArguments[11] = "RPEType";
                    mailArguments[12]=sRPEType;
                    mailArguments[13] = "RPEName";
                    mailArguments[14]=sRPEName;
                    mailArguments[15] = "RPERev";
                    mailArguments[16]=sRPERev;
                    mailArguments[17] = "RPEOwner";
                    mailArguments[18]=sRPEOwner;
                    mailArguments[19] = "Type";
                    mailArguments[20]=sType;
                    mailArguments[21] = "Name";
                    mailArguments[22]=sName;
                    mailArguments[23] = "Rev";
                    mailArguments[24]=sRev;
                    mailArguments[25] = "ROwner";
                    mailArguments[26]=sRouteOwner;
                    mailArguments[27] = sInboxTaskId;
                    mailArguments[32] = "";
                    //To set Tree Menu
                    emxMailUtil_mxJPO.setTreeMenuName(context, sTreeMenu);
                    //Calling method to send Mail
                    emxMailUtil_mxJPO.sendNotificationToUser(context,mailArguments);
                   } else {
                    String [] mailArguments = new String [29];
                    mailArguments[0] = sPersonName;
                    mailArguments[1] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.SubjectNotice";
                    mailArguments[2] = "0";
                    mailArguments[3] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.DelegatorMessageNotice";
                    mailArguments[4] = "12";
                    mailArguments[5] = "IBType";
                    mailArguments[6] = sInboxTaskType;
                    mailArguments[7] = "IBName";
                    mailArguments[8] = sInboxTaskName;
                    mailArguments[9] = "IBRev";
                    mailArguments[10]= sInboxTaskRev;
                    mailArguments[11] = "RPEType";
                    mailArguments[12]=sRPEType;
                    mailArguments[13] = "RPEName";
                    mailArguments[14]=sRPEName;
                    mailArguments[15] = "RPERev";
                    mailArguments[16]=sRPERev;
                    mailArguments[17] = "RPEOwner";
                    mailArguments[18]=sRPEOwner;
                    mailArguments[19] = "Type";
                    mailArguments[20]=sType;
                    mailArguments[21] = "Name";
                    mailArguments[22]=sName;
                    mailArguments[23] = "Rev";
                    mailArguments[24]=sRev;
                    mailArguments[25] = "ROwner";
                    mailArguments[26]=sRouteOwner;
                    mailArguments[27] ="Delegator";
                    mailArguments[28]= sPersonFirstName+" "+sPersonLastName;
                    mailArguments[27] = sInboxTaskId;
                    mailArguments[28] = "";
                    //To set Tree Menu
                    emxMailUtil_mxJPO.setTreeMenuName(context, sTreeMenu);
                    //Calling method to send Mail
                    emxMailUtil_mxJPO.sendNotificationToUser(context,mailArguments);
                    }
                  } //eof  if {sRouteObjects == ""}
                  else {
                     if (UIUtil.isNullOrEmpty(sDelegatorId)) {
                         String [] mailArguments = new String [29];
                    mailArguments[0] = sPersonName;
                    mailArguments[1] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.SubjectNotice";
                    mailArguments[2] = "0";
                    mailArguments[3] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.MessageNoticeWithObjects";
                    mailArguments[4] = "12";
                    mailArguments[5] = "IBType";
                    mailArguments[6] = sInboxTaskType;
                    mailArguments[7] = "IBName";
                    mailArguments[8] = sInboxTaskName;
                    mailArguments[9] = "IBRev";
                    mailArguments[10]= sInboxTaskRev;
                    mailArguments[11] = "RPEType";
                    mailArguments[12]=sRPEType;
                    mailArguments[13] = "RPEName";
                    mailArguments[14]=sRPEName;
                    mailArguments[15] = "RPERev";
                    mailArguments[16]=sRPERev;
                    mailArguments[17] = "RPEOwner";
                    mailArguments[18]=sRPEOwner;
                    mailArguments[19] = "Type";
                    mailArguments[20]=sType;
                    mailArguments[21] = "Name";
                    mailArguments[22]=sName;
                    mailArguments[23] = "Rev";
                    mailArguments[24]=sRev;
                    mailArguments[25] = "ROwner";
                    mailArguments[26]=sRouteOwner;
                    mailArguments[27] ="RObjects";
                    mailArguments[28]= sRouteObjects;
                    mailArguments[27] = sInboxTaskId;
                    mailArguments[28] = "";
                    //To set Tree Menu
                    emxMailUtil_mxJPO.setTreeMenuName(context, sTreeMenu);
                    //Calling method to send Mail
                    emxMailUtil_mxJPO.sendNotificationToUser(context,mailArguments);
                   } else {
                    String [] mailArguments = new String [29];
                    mailArguments[0] = sPersonName;
                    mailArguments[1] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.SubjectNotice";
                    mailArguments[2] = "0";
                    mailArguments[3] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.DelegatorMessageNoticeWithObjects";
                    mailArguments[4] = "13";
                    mailArguments[5] = "IBType";
                    mailArguments[6] = sInboxTaskType;
                    mailArguments[7] = "IBName";
                    mailArguments[8] = sInboxTaskName;
                    mailArguments[9] = "IBRev";
                    mailArguments[10]= sInboxTaskRev;
                    mailArguments[11] = "RPEType";
                    mailArguments[12]=sRPEType;
                    mailArguments[13] = "RPEName";
                    mailArguments[14]=sRPEName;
                    mailArguments[15] = "RPERev";
                    mailArguments[16]=sRPERev;
                    mailArguments[17] = "RPEOwner";
                    mailArguments[18]=sRPEOwner;
                    mailArguments[19] = "Type";
                    mailArguments[20]=sType;
                    mailArguments[21] = "Name";
                    mailArguments[22]=sName;
                    mailArguments[23] = "Rev";
                    mailArguments[24]=sRev;
                    mailArguments[25] = "ROwner";
                    mailArguments[26]=sRouteOwner;
                    mailArguments[27] ="Delegator";
                    mailArguments[28]= sPersonFirstName+" "+sPersonLastName;
                    mailArguments[27] = sInboxTaskId;
                    mailArguments[28] = "";
                    //To set Tree Menu
                    emxMailUtil_mxJPO.setTreeMenuName(context, sTreeMenu);
                    //Calling method to send Mail
                    emxMailUtil_mxJPO.sendNotificationToUser(context,mailArguments);
                    }
                  }
              } else if (!sRPEType.equals(sInboxTaskType) && "".equals(sRouteTaskUser)) {
                  if (UIUtil.isNullOrEmpty(sRouteObjects)) {
                      if (UIUtil.isNullOrEmpty(sDelegatorId)) {
                        String [] mailArguments = new String [21];
                    mailArguments[0] = sPersonName;
                    mailArguments[1] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.SubjectNotice2";
                    mailArguments[2] = "0";
                    mailArguments[3] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.FirstMessage";
                    mailArguments[4] = "7";
                    mailArguments[5] = "IBType";
                    mailArguments[6] = sInboxTaskType;
                    mailArguments[7] = "IBName";
                    mailArguments[8] = sInboxTaskName;
                    mailArguments[9] = "IBRev";
                    mailArguments[10]= sInboxTaskRev;
                    mailArguments[11] = "Type";
                    mailArguments[12]=sType;
                    mailArguments[13] = "Name";
                    mailArguments[14]=sName;
                    mailArguments[15] = "Rev";
                    mailArguments[16]=sRev;
                    mailArguments[17] = "ROwner";
                    mailArguments[18]=sRouteOwner;
                     mailArguments[19] = sInboxTaskId;
                    mailArguments[20] = "";
                    //To set Tree Menu
                    emxMailUtil_mxJPO.setTreeMenuName(context, sTreeMenu);
                    //Calling method to send Mail
                    emxMailUtil_mxJPO.sendNotificationToUser(context,mailArguments);

                      } else {
                    String [] mailArguments = new String [23];
                    mailArguments[0] = sPersonName;
                    mailArguments[1] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.SubjectNotice2";
                    mailArguments[2] = "0";
                    mailArguments[3] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.FirstDelegatorMessage";
                    mailArguments[4] = "8";
                    mailArguments[5] = "IBType";
                    mailArguments[6] = sInboxTaskType;
                    mailArguments[7] = "IBName";
                    mailArguments[8] = sInboxTaskName;
                    mailArguments[9] = "IBRev";
                    mailArguments[10]= sInboxTaskRev;
                    mailArguments[11] = "Type";
                    mailArguments[12]=sType;
                    mailArguments[13] = "Name";
                    mailArguments[14]=sName;
                    mailArguments[15] = "Rev";
                    mailArguments[16]=sRev;
                    mailArguments[17] = "ROwner";
                    mailArguments[18]=sRouteOwner;
                    mailArguments[19] ="Delegator";
                    mailArguments[20]= sPersonFirstName+" "+sPersonLastName;
                    mailArguments[21] = sInboxTaskId;
                    mailArguments[22] = "";
                    //To set Tree Menu
                    emxMailUtil_mxJPO.setTreeMenuName(context, sTreeMenu);
                    //Calling method to send Mail
                    emxMailUtil_mxJPO.sendNotificationToUser(context,mailArguments);
                    }
                  } else {
                      if (UIUtil.isNullOrEmpty(sDelegatorId)) {
                            String [] mailArguments = new String [23];
                    mailArguments[0] = sPersonName;
                    mailArguments[1] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.SubjectNotice2";
                    mailArguments[2] = "0";
                    mailArguments[3] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.FirstMessageWithObjects";
                    mailArguments[4] = "8";
                    mailArguments[5] = "IBType";
                    mailArguments[6] = sInboxTaskType;
                    mailArguments[7] = "IBName";
                    mailArguments[8] = sInboxTaskName;
                    mailArguments[9] = "IBRev";
                    mailArguments[10]= sInboxTaskRev;
                    mailArguments[11] = "Type";
                    mailArguments[12]=sType;
                    mailArguments[13] = "Name";
                    mailArguments[14]=sName;
                    mailArguments[15] = "Rev";
                    mailArguments[16]=sRev;
                    mailArguments[17] = "ROwner";
                    mailArguments[18]=sRouteOwner;
                    mailArguments[19] ="RObjects";
                    mailArguments[20]= sRouteObjects;
                    mailArguments[21] = sInboxTaskId;
                    mailArguments[22] = "";
                    //To set Tree Menu
                    emxMailUtil_mxJPO.setTreeMenuName(context, sTreeMenu);
                    //Calling method to send Mail
                    emxMailUtil_mxJPO.sendNotificationToUser(context,mailArguments);

                      } else {
                        String [] mailArguments = new String [25];
                    mailArguments[0] = sPersonName;
                    mailArguments[1] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.SubjectNotice2";
                    mailArguments[2] = "0";
                    mailArguments[3] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.FirstDelegatorMessageWithObjects";
                    mailArguments[4] = "9";
                    mailArguments[5] = "IBType";
                    mailArguments[6] = sInboxTaskType;
                    mailArguments[7] = "IBName";
                    mailArguments[8] = sInboxTaskName;
                    mailArguments[9] = "IBRev";
                    mailArguments[10]= sInboxTaskRev;
                    mailArguments[11] = "Type";
                    mailArguments[12]=sType;
                    mailArguments[13] = "Name";
                    mailArguments[14]=sName;
                    mailArguments[15] = "Rev";
                    mailArguments[16]=sRev;
                    mailArguments[17] = "ROwner";
                    mailArguments[18]=sRouteOwner;
                    mailArguments[19] ="RObjects";
                    mailArguments[20]= sRouteObjects;
                    mailArguments[21] ="Delegator";
                    mailArguments[22] =sPersonFirstName+" "+sPersonLastName;
                    mailArguments[23] = sInboxTaskId;
                    mailArguments[24] = "";
                    //To set Tree Menu
                    emxMailUtil_mxJPO.setTreeMenuName(context, sTreeMenu);
                    //Calling method to send Mail
                    emxMailUtil_mxJPO.sendNotificationToUser(context,mailArguments);
                      }
                  }
              }//

              emxContextUtilObj.pushContext(context,null);
              //Inform route owner that task was delegated to someone else
              if (!sDelegatorId.equals("")) {
                  String [] mailArguments = new String [17];
                    mailArguments[0] = sPersonName;
                    mailArguments[1] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.SubjectNotice";
                    mailArguments[2] = "0";
                    mailArguments[3] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.MessageDelegator";
                    mailArguments[4] = "4";
                    mailArguments[7] = "IBName";
                    mailArguments[8] = sInboxTaskName;
                    mailArguments[9] = "Name";
                    mailArguments[10] = sName;
                    mailArguments[11] ="Delegator";
                    mailArguments[12]= sDelegatorFullName;
                    mailArguments[13] ="FullName";
                    mailArguments[14]= sPersonFirstName+" "+sPersonLastName;
                    mailArguments[15] = "";
                    mailArguments[16] = "";
                    //To set Tree Menu
                    //Calling method to send Mail
                    emxMailUtil_mxJPO.sendNotificationToUser(context,mailArguments);


              }
          }
          // Set Route Status attribute on Route to equal Started
          // Set Current Route Node attribute on Route to equal 1
          Route.setAttributeValue(context, ""+sAttCurrentRouteNode, ""+sRouteSequenceValue);
          Route.setAttributeValue(context, sAttRouteStatus, "Started");
       }//eof for loop
      if (bPersonFound == 1) {
          break;
      }
      if (bGreaterSeqNoFound == 1) {
          sRouteSequenceValue++;
      }
  }//eof while loop


// Error if no Person object found
  if (bPersonFound == 0 && bErrorOnNoConnection == 1) {
      if (UIUtil.isNullOrEmpty(sRouteTaskUser)) {
                    String [] mailArguments = new String [17];
                    mailArguments[1] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.NoObjectError";
                    mailArguments[2] = "4";
                    mailArguments[3] = "Person";
                    mailArguments[4] = sTypePerson;
                    mailArguments[7] = "Type";
                    mailArguments[8] = sType;
                    mailArguments[9] = "Name";
                    mailArguments[10]=sName;
                    mailArguments[11] ="Rev";
                    mailArguments[12]= sRev;
                    mailArguments[13] ="FullName";
                    emxMailUtil_mxJPO.setTreeMenuName(context, sTreeMenu);
                    //Calling method to send Mail
                    Message= emxMailUtil_mxJPO.getMessage(context,mailArguments);
         } else {
                    String [] mailArguments = new String [17];
                    mailArguments[1] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.NoObjectError";
                    mailArguments[2] = "4";
                    mailArguments[3] = "Person";
                    mailArguments[4] = sTypeRouteTaskUser;
                    mailArguments[7] = "Type";
                    mailArguments[8] = sType;
                    mailArguments[9] = "Name";
                    mailArguments[10]=sName;
                    mailArguments[11] ="Rev";
                    mailArguments[12]= sRev;
                    emxMailUtil_mxJPO.setTreeMenuName(context, sTreeMenu);
                    //Calling method to send Mail
                    Message = emxMailUtil_mxJPO.getMessage(context,mailArguments);
              }//eof else
       if(Message.length() >0)
       {
        MqlUtil.mqlCommand(context,"notice $1",Message);
        return 1;
       }
     }//eof if (bPersonFound == 0 && bErrorOnNoConnection == 1)

   ContextUtil.commitTransaction(context);
   return 0;
   }catch(Exception e){
      ContextUtil.abortTransaction(context);
      return 1;
   }
}
}
