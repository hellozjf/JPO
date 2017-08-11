/*
 *  emxTestExecutionBase.java
 *
 * Copyright (c) 2005-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENOProductLine/CNext/Modules/ENOProductLine/JPOsrc/base/${CLASSNAME}.java 1.4.2.1.1.1 Wed Oct 29 22:17:06 2008 GMT przemek Experimental$
 */
/*
 * @quickreview T25 DJH 13:07:31 : HL "Test Execution under Test Case without an EC". Modified getRelatedTestExecutions(). Added support for relationship TEST EXECUTION TEST CASE.
 * @quickreview T25 DJH 13:08:16 : IR IR-248763V6R2014x : Test Execution command disappear when Test case promoted to Completed state.Modified method showTestExecutionLinksToAssignedQE to show Action menu when Test Case is in Complete State.
 * @quickreview ZUD DJH 13:11:12 : IR IR-261269V6R2014x : STP: Actual Completion Date is not getting updated for Test case even when user promoted the test case to  complete state. Added New Function SetTestCaseCompletionDate. 
 * @quickreview ZUD DJH 13:11:13 : IR IR-261269V6R2014x : Putting PopContext in finally block. 
 * @quickreview JX5 QYG 15:02:12 : HL Widgetization - method displayCopyParameterCheckBox & copyParameter
 * @quickreview HAT1 ZUD 16:08:09 : IR-439318-3DEXPERIENCER2017x: R419-STP: Test Case is accepting "Estimated Completion Date" as past date & Test Execution is accepting  "Estimated Start Date" & "Estimated End Date" as past date.
 */

import  java.util.Calendar;
import  java.util.Date;
import  java.util.HashMap;
import  java.util.Hashtable;
import  java.util.Iterator;
import  java.util.List;
import  java.util.Locale;
import  java.util.Map;
import  java.util.Vector;
import java.text.DateFormat;
import  java.text.SimpleDateFormat;
import  matrix.db.Context;
import  matrix.db.Person;
import  matrix.db.JPO;

import  matrix.util.StringList;

import  com.matrixone.apps.domain.DomainObject;
import  com.matrixone.apps.domain.DomainConstants;
import  com.matrixone.apps.domain.DomainRelationship;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import  com.matrixone.apps.domain.util.eMatrixDateFormat;
import  com.matrixone.apps.domain.util.FrameworkException;
import  com.matrixone.apps.domain.util.FrameworkUtil;
import  com.matrixone.apps.domain.util.MapList;
import  com.matrixone.apps.domain.util.MessageUtil;
import  com.matrixone.apps.domain.util.PropertyUtil;
import  com.matrixone.apps.domain.util.ContextUtil;

import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;
import com.matrixone.apps.productline.TestExecution;

/**
 * The <code>emxTestExecutionBase</code> JPO class that holds methods for executing JPO
 * operations related to objects of the type Test Execution
 * @author  Enovia MatrixOne
 * @version ProductCentral 10-6 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxTestExecutionBase_mxJPO extends emxDomainObject_mxJPO {

    public static final String SUITE_KEY = "ProductLine";

    /**
     * Alias for the symbolic name of the relationship Assigned Test Execution.
     */
    protected static final String RELATIONSHIP_ASSIGNED_TESTEXECUTION = "relationship_AssignedTestExecution";

	// Begin of add by Enovia MatrixOne for bug no. 300085 on 04/13/05

	/** A string constant with the value state_FormalApproval */
    public static final String EC_STATE_FORMAL_APPROVAL  = "state_FormalApproval";
    /** A string constant with the value state_Complete */
    public static final String EC_STATE_COMPLETE         = "state_Complete";
	/** A string constant with the value state_Complete */
    public static final String EC_STATE_REJECT         = "state_Reject";
	/** A string constant with the value state_Complete */
    public static final String EC_STATE_CLOSE         = "state_Close";

	// End of add by Enovia MatrixOne for bug no. 300085 on 04/13/05

    /**
     * Default Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the packed arguments
     * @throws        Exception if the operation fails
     * @since         ProductCentral 10-6
     **
     */

    public emxTestExecutionBase_mxJPO (Context context, String[] args)
        throws Exception {
      super(context, args);
    }


    /**
     * Main entry point
     *
     * @param context context for this request
     * @param args    holds no arguments
     * @return        an integer status code (0 = success)
     * @throws        Exception when problems occurred in the AEF
     * @since         ProductCentral 10-6
     */
    public int mxMain(Context context, String[] args)
        throws Exception {
        if (!context.isConnected()) {
            String strLanguage = context.getSession().getLanguage();
            String strContentLabel = EnoviaResourceBundle.getProperty(context, "ProductLine","emxProduct.Error.UnsupportedClient",strLanguage);
            throw new Exception(strContentLabel);
       }
       return 0;
    }

    /**
     * Gets the Slip Days field in Test Execution Properties page.
     *
     * @param context  the eMatrix <code>Context</code> object
     * @param args     holds the following input arguments:
     *             0 - HashMap containing one String entry for key "objectId"
     * @return         a <code>String</code> object having HTML output for the
     *                 Slip Days field in Test Execution Properties page
     * @throws         Exception if the operation fails
     * @since          Product Central 10-6
     **
     */
    public String getSlipDaysField(Context context, String[] args) throws Exception {
    	//XSSOK
        int nDuration = 0;
        //unpacking the Arguments from variable args
        HashMap programMap    = (HashMap)JPO.unpackArgs(args);
        //getting parent object Id from args
        Map paramMap          = (Map)programMap.get("paramMap");
        String strTEId       = (String)paramMap.get("objectId");
        setId(strTEId);
        String strEstimatedEndDt = getAttributeValue(context,DomainConstants.ATTRIBUTE_ESTIMATED_END_DATE);
        String strActualEndDt    = getAttributeValue(context,DomainConstants.ATTRIBUTE_ACTUAL_END_DATE);
        //If the Estimated end date and Actual End date attributes are present the slip is calculated
        if(!(strEstimatedEndDt == null || strEstimatedEndDt.equalsIgnoreCase(DomainConstants.EMPTY_STRING) || strEstimatedEndDt.equalsIgnoreCase("null") )
           && !(strActualEndDt == null || strActualEndDt.equalsIgnoreCase(DomainConstants.EMPTY_STRING) || strActualEndDt.equalsIgnoreCase("null") ) ) {
            nDuration = ProductLineUtil.daysBetween(strActualEndDt,strEstimatedEndDt);
            if(nDuration<0){
                nDuration = 0;
            }
            return Integer.toString(nDuration);
        } else {
            return DomainConstants.EMPTY_STRING;
        }
    }

    /**
     * Get related Test Execution of the parent object
     *
     * @param context  the eMatrix <code>Context</code> object
     * @param args     holds the following input arguments:
     *             0 - HashMap containing one String entry for key "objectId"
     * @return         a <code>MapList</code> object having the list of all related Test
     *                 Executions for the context Object.
     * @throws         Exception if the operation fails
     * @since          Product Central 10-6
     **
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRelatedTestExecutions(Context context, String[] args) throws Exception
    {
        //unpacking the Arguments from variable args
        HashMap programMap       = (HashMap)JPO.unpackArgs(args);
        //getting parent object Id from args
        String strParentId       = (String)programMap.get("objectId");
        //initializing return type
        MapList returnMapList    = new MapList();
        // the relationship for searching related Test Executions
        String strParentRelName           = null;
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        //Relationships are selected by its Ids
        StringList relSelects    = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //the number of levels to expand, 1 equals expand one level
        short recurseToLevel     = 1;

        try{
              setId(strParentId);
              String strParentObjType = getInfo(context, ProductLineConstants.SELECT_TYPE);

              //setting the relationship name to searh for connected TE.
              if(strParentObjType.equals(DomainConstants.TYPE_ENGINEERING_CHANGE))
              {
                strParentRelName = DomainConstants.RELATIONSHIP_EC_TEST_EXECUTION;

                returnMapList        = getRelatedObjects(context,
                                                         strParentRelName,                            // Relationship name
                                                         DomainConstants.TYPE_TEST_EXECUTION,        // get type Test Execution
                                                         objectSelects,
                                                         relSelects,
                                                         false,
                                                         true,                                        //Get the to end of the rel
                                                         recurseToLevel,
                                                         DomainConstants.EMPTY_STRING,
                                                         DomainConstants.EMPTY_STRING);
              }
              else if (strParentObjType.equals(DomainConstants.TYPE_TEST_CASE)) // added condition for HL Test Execution under Test Case without an EC
              {       
            	  //this relationship will hold between (From)TE - (To)TC, when Test Case is created directly under Requirement
                strParentRelName = DomainConstants.RELATIONSHIP_TEST_EXECUTION_TEST_CASE;
        
        	      returnMapList        = getRelatedObjects(context,
                strParentRelName,                            // Relationship name
                DomainConstants.TYPE_TEST_EXECUTION,        // get type Test Execution
                objectSelects,
                relSelects,
                true,                                      // get To relationships for the Test Case
                false,                                         
                recurseToLevel,
                DomainConstants.EMPTY_STRING,
                DomainConstants.EMPTY_STRING);

             }
          }
          catch (Exception ex)
          {
            throw ex;
          }
          return returnMapList;
    }

    /**
     * The method is used to fill the Slip Days column in Test Execution Summary Table .
     * Returns the Slip Days for each Test Execution in its summary page.
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id list
     * @return        a <code>Vector</code> object to return Slip Days for each Test Execution in the summary page.
     * @throws        Exception if the operation fails
     * @since         Product Central 10-6
     **
     */
    public Vector getSlipDaysColumn(Context context, String[] args) throws Exception {
        Vector returnVector   = new Vector();
        HashMap programMap    = (HashMap)JPO.unpackArgs(args);
        MapList objectList    = (MapList)programMap.get("objectList");
        int objectListSize    = 0 ;

        if(objectList != null) {
            objectListSize = objectList.size();
        }

        String oidsArray[]    = new String[objectListSize];
        for (int i = 0; i < objectListSize; i++) {
            try {
                   oidsArray[i] = (String)((HashMap)objectList.get(i)).get(DomainConstants.SELECT_ID);
            } catch (Exception ex) {
                   oidsArray[i] = (String)((Hashtable)objectList.get(i)).get(DomainConstants.SELECT_ID);
            }
        }

        String selectAttrEstEndDt = "attribute["+DomainConstants.ATTRIBUTE_ESTIMATED_END_DATE+"]";
        String selectAttrActualEndDt = "attribute["+DomainConstants.ATTRIBUTE_ACTUAL_END_DATE+"]";
        StringList listSelect = new StringList(selectAttrEstEndDt);
        listSelect.add(selectAttrActualEndDt);
        MapList attributeMapList = DomainObject.getInfo(context,oidsArray,listSelect);
        Iterator attLstItr = attributeMapList.iterator();
        while(attLstItr.hasNext()){
            int nDuration = 0;
            Map tmpAtrMap = (Map)attLstItr.next();
            String estimatedEndDt = (String)( tmpAtrMap.get(selectAttrEstEndDt) );
            String actualEndDt = (String)( tmpAtrMap.get(selectAttrActualEndDt) );
            if(!(estimatedEndDt == null || estimatedEndDt.equalsIgnoreCase(DomainConstants.EMPTY_STRING) || estimatedEndDt.equalsIgnoreCase("null") )
                        && !(actualEndDt == null || actualEndDt.equalsIgnoreCase(DomainConstants.EMPTY_STRING) || actualEndDt.equalsIgnoreCase("null") ) ) {
                nDuration = ProductLineUtil.daysBetween(actualEndDt,estimatedEndDt);
                if(nDuration<0){
                    nDuration = 0;
                }
                returnVector.add(Integer.toString(nDuration));

            } else {
                returnVector.add(DomainConstants.EMPTY_STRING);
            }
        }

        return returnVector;
    }

    /**
     * The method is used display the WBS number indicating the hierarchical levels of Test Cases connected to the Test Execution
     * object. This information is available as the 'Task WBS' attribute on the relationship between the Test Execution and
     * connected Test Case children. e.g. If a parent Engineering Change object 'EC1' has 2 Test Case siblings TC1, TC2 and TC1 has
     * 2 next level Test Case children 'TC11' and 'TC111', the WBS numbers set for the Test Execution Test Cases relationship are
     * 1, 1.1, 1.1.1 and 2 respectively for 'TC1', 'TC11', 'TC111', 'TC2' objects.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id list
     * @return        a <code>Vector</code> object to return the WBS number for each Test Case listed in the summary page
     * @throws        Exception if the operation fails
     * @since         Product Central 10-6
     **
     */
    public Vector getWBSColumn(Context context, String[] args) throws Exception {
        Vector returnVector   = new Vector();
        HashMap programMap    = (HashMap)JPO.unpackArgs(args);
        MapList objectList    = (MapList)programMap.get("objectList");
        int objectListSize    = 0 ;

        if(objectList != null) {
            objectListSize = objectList.size();
        }

        String oidsArray[]    = new String[objectListSize];
        for (int i = 0; i < objectListSize; i++) {
            try {
                   oidsArray[i] = (String)((HashMap)objectList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
            } catch (Exception ex) {
                   oidsArray[i] = (String)((Hashtable)objectList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
            }
        }
        // 'Task WBS' attribute select
        StringBuffer sbAttrTaskWBSSelect  = new StringBuffer("attribute[");
        sbAttrTaskWBSSelect.append(TestExecution.ATTRIBUTE_TASK_WBS).append("]");
        StringList relSelects = new StringList(sbAttrTaskWBSSelect.toString());

        MapList mlist            = DomainRelationship.getInfo(context, oidsArray, relSelects);
        Iterator itr             = mlist.iterator();

        String sTaskWBS         = null;
        Map objectMap           = null;

        while( itr.hasNext()) {
            objectMap = (Map)itr.next();
            sTaskWBS  = (String) objectMap.get(sbAttrTaskWBSSelect.toString());
            returnVector.add(sTaskWBS);
        }
        return returnVector;
    }

    /**
     * Access program for showing 'Create New'/'Edit All'/'Delete' Test Execution action
     * links to Parent Object's owner or assigned 'Software Quality Engineer' or
     * 'Quality Manager' or 'Senior Quality Engineer' of the parent object.
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *            0 - HashMap containing one String entry for key "objectId"
     * @return        a boolean value to display 'Create New'/'Edit All'/'Delete' links in Test Execution Summary page
     * @throws        Exception if the operations fails
     * @since         Product Central 10-6
     */
    public boolean showTestExecutionLinksToAssignedQE (Context context, String[] args) throws Exception {

        //Return boolean value to show or hide Test Execution commands
        boolean showCommands    = false;
        // the parent assignee relationship name
        String strParentRelName           = null;
        //unpacking the Arguments from variable args
        HashMap programMap           = (HashMap)JPO.unpackArgs(args);
        //getting parent object Id from args
        String strParentId           = (String)programMap.get("objectId");
        //Getting the context user
        String strContextUser        = context.getUser();

        try{
            DomainObject parentDomObj    = DomainObject.newInstance(context,strParentId);
            String strParentObjOwner     = parentDomObj.getInfo(context,DomainConstants.SELECT_OWNER);

			// Begin of add by Enovia MatrixOne for bug no. 300085 on 04/13/05

			// Getting the current state and the policy of the context EC object
			String strParentObjState     = parentDomObj.getInfo(context,DomainConstants.SELECT_CURRENT);
			String strParentObjPolicy     = parentDomObj.getInfo(context,DomainConstants.SELECT_POLICY);

			String strFormalApproval = FrameworkUtil.lookupStateName(context,
																	strParentObjPolicy,
																	EC_STATE_FORMAL_APPROVAL);
			String strClose = FrameworkUtil.lookupStateName(context,
															strParentObjPolicy,
															EC_STATE_CLOSE);
			String strReject = FrameworkUtil.lookupStateName(context,
															strParentObjPolicy,
															EC_STATE_REJECT);
			String strComplete = FrameworkUtil.lookupStateName(context,
															strParentObjPolicy,
															EC_STATE_COMPLETE);

			// If the current state of the EC object is Close, Reject,Complete or Formal Approval ,
			// the commands will not be shown
			if( strParentObjState.equals(strFormalApproval)
				||strParentObjState.equals(strClose)
				||strParentObjState.equals(strReject)
				||strParentObjState.equals(strComplete))
			{
				//Start: T25 DJH 2013:08:16:IR IR-248763V6R2014x
				 if(parentDomObj.isKindOf(context, PropertyUtil.getSchemaProperty(context, "type_TestCase"))) //When TC under Requirement is created
				{
					showCommands=true;
				}
				else //When TC under EC is created
				{
					showCommands = false;
				}
				//End: T25 DJH
			}else {

			// End of add by Enovia MatrixOne for bug no. 300085 on 4/13/05

				//Checking if the context user is the parent object owner
				if( strContextUser.equals(strParentObjOwner) ) {
					showCommands = true;
				} else {
					//Checking if the context user is the Assigned 'Software Quality Engineer' or 'Quality Manager' or 'Senior Quality Engineer' of parent object

					//Map to hold the parent object's 'Assignees'
					MapList parentObjAssigneeMapList = null;
					StringList objectSelects     = new StringList(DomainConstants.SELECT_NAME);
					objectSelects.add(DomainConstants.SELECT_ID);
					short recurseToLevel         = 1;
					String strParentObjType      = null;

					strParentObjType  = parentDomObj.getInfo(context, ProductLineConstants.SELECT_TYPE);
					//setting the relationship name to get related Assignees.
					if(strParentObjType.equals(DomainConstants.TYPE_ENGINEERING_CHANGE)){
						strParentRelName = DomainConstants.RELATIONSHIP_ASSIGNED_EC;
					}
					parentObjAssigneeMapList        = parentDomObj.getRelatedObjects(context,
																 strParentRelName,             // Relationship name
																 DomainConstants.TYPE_PERSON,  // get type Persom
																 objectSelects,
																 new StringList(),
																 true,                         //Get the from end of the rel
																 false,
																 recurseToLevel,
																 DomainConstants.EMPTY_STRING,
																 DomainConstants.EMPTY_STRING);
					Iterator mListItr      = parentObjAssigneeMapList.iterator();
					String tmpAssigneeName = null;
					Person tmpAssigneeObj = null;
					while(mListItr.hasNext()){
						tmpAssigneeName     = (String) ( ( (Map)mListItr.next() ).get(DomainConstants.SELECT_NAME) );
						if( strContextUser.equals(tmpAssigneeName) ) {
							tmpAssigneeObj      = new Person(strContextUser);
							if( tmpAssigneeObj.isAssigned(context, ProductLineConstants.ROLE_QUALITY_MANAGER) ||
								tmpAssigneeObj.isAssigned(context, ProductLineConstants.ROLE_SENIOR_QUALITY_ENGINEER) ||
								tmpAssigneeObj.isAssigned(context, ProductLineConstants.ROLE_SOFTWARE_QUALITY_ENGINEER) ) {
								showCommands = true;
								break;
							}
						}
					}//end of while
				}//end of else
			}// end of else
        } catch (Exception ex){
            throw ex;
        }
        return showCommands;
    }

    /**
     * The method is used to display Status gif column in Test Case Summary table of Test Execution.
     * Green indicates and red indicates
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id list
     * @return        a <code>Vector</code> object to return the status gif corresponding to each Test Case result.
     * @throws        Exception if the operation fails
     * @since         Product Central 10-6
     **
     */
    public Vector showTestCaseStatusGif(Context context, String[] args) throws Exception {
        Vector returnVector   = new Vector();
        HashMap programMap    = (HashMap)JPO.unpackArgs(args);
        MapList objectList    = (MapList)programMap.get("objectList");
        int objectListSize    = 0 ;

        if(objectList != null) {
            objectListSize = objectList.size();
        }

        String oidsArray[]    = new String[objectListSize];
        for (int i = 0; i < objectListSize; i++) {
            returnVector.add(Integer.toString(i));
            try {
                oidsArray[i] = (String)((HashMap)objectList.get(i)).get(DomainConstants.SELECT_ID);
            } catch (Exception ex) {
                oidsArray[i] = (String)((Hashtable)objectList.get(i)).get(DomainConstants.SELECT_ID);
            }
        }
        return returnVector;
    }

    /**
     * This trigger method notifies all the Person objects connected to the Test
     * Execution by "Assigned Test Execution" relationship when the Test
     * Execution is promoted from the Create state. The notification
     * message contains the <TYPE><NAME><REVISION>of the Test Execution.
     *
     * @param context The ematrix context of the request
     * @param args String Array with following arguments: 0 - The Object Id of
     *            the context Test Execution.
     * @throws Exception
     * @throws FrameworkException
     * @since ProductCentral 10.6
     */
    public void notifyAssignees(Context context, String[] args)
            throws Exception, FrameworkException {

        //Get the Object Id of the context Engineering Change object.
        String strObjectId = (String) args[0];

        //Get the actual name of the relationship Assigned Test Execution from
        // the symbolic name
        String strRelAssignedTestExecution = PropertyUtil.getSchemaProperty(
                context, RELATIONSHIP_ASSIGNED_TESTEXECUTION);

        //Create a new domain object representing the Test Execution
        DomainObject domTest = DomainObject.newInstance(
                context, strObjectId);

        //Get all the objects of type "Person" connected to Test Execution
        //by "Assigned Test Execution" relationship
        StringList slObjectSelects = new StringList(DomainConstants.SELECT_NAME);
        List assigneeList = (MapList) domTest.getRelatedObjects(
                context, strRelAssignedTestExecution,
                DomainConstants.TYPE_PERSON, slObjectSelects, null, true,
                false, (short) 1, null, null);

        //Form the stringlist of all the assignee names to send the message
        StringList slAssigneeList = new StringList();
        for (int i = 0; i < assigneeList.size(); i++) {
            slAssigneeList.addElement((String) ((Map) assigneeList.get(i))
                    .get(DomainConstants.SELECT_NAME));
        }

        //Form the subject and the message body
        String strLanguage = context.getSession().getLanguage();
        String strSubjectKey = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Message.Subject.TestExecutionAssigned",strLanguage);
        String strMessageKey = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Message.Description.TestExecutionAssigned",strLanguage);
        strMessageKey = MessageUtil.substituteValues(
                    context, strMessageKey, strObjectId, strLanguage);
        String[] subjectKeys = {};
        String[] subjectValues = {};
        String[] messageKeys = {};
        String[] messageValues = {};

        //Form the message attachment
        StringList slAttachments = new StringList();
        slAttachments.add(strObjectId);

        //Send the notification to all the Assignees

         emxMailUtilBase_mxJPO.sendNotification( context, slAssigneeList,
         null, null, strSubjectKey, subjectKeys, subjectValues, strMessageKey,
         messageKeys, messageValues, slAttachments, null);

    }

    /**
     * This trigger method notifies the Assignee about the Test Execution
     * assignment when a new relationship "Assigned Test Execution" is created
     * between Test Execution and Person object. The notification is sent
     * only if Test Execution is not in Create state.
     *
     * @param context The ematrix context of the request
     * @param args The string array containing following elements: 0 - The
     *            object id of Person 1 - The object id of the context EC
     * @throws Exception
     * @throws FrameworkException
     */
    public void notifyAssignee(Context context, String[] args)
            throws Exception, FrameworkException {
        //Get the objectId of the Test Execution and the Person object
        String strPersonId = args[0];
        String strObjId = args[1];

        //Get the current state of the Test Execution and if it is
        // in create then do not notify the Assignee
        DomainObject domEC = DomainObject.newInstance(
                context, strObjId);
        List lstObjectSelects = new StringList(DomainConstants.SELECT_CURRENT);
        lstObjectSelects.add(DomainConstants.SELECT_POLICY);
        lstObjectSelects.add(DomainConstants.SELECT_STATES);
        lstObjectSelects.add(DomainConstants.SELECT_NAME);

        Map mapECInfo = domEC.getInfo(
                context, (StringList) lstObjectSelects);
        String strStateSubmit = FrameworkUtil.lookupStateName(
                context, (String) mapECInfo.get(DomainConstants.SELECT_POLICY),
                "state_Create");

        List lstStateList = (StringList) mapECInfo
                .get(DomainConstants.SELECT_STATES);
        String strCurrentState = (String) mapECInfo
                .get(DomainConstants.SELECT_CURRENT);

        if (lstStateList.indexOf(strCurrentState) > lstStateList
                .indexOf(strStateSubmit)) {
            DomainObject domPerson = DomainObject.newInstance(
                    context, strPersonId);
            Map mapPersonInfo = domPerson.getInfo(
                    context, (StringList) lstObjectSelects);

            //Form the reciepient list to send the message
            List lstAssigneeList = new StringList((String) mapPersonInfo
                    .get(DomainConstants.SELECT_NAME));

            //Form the subject and the message body
            String strLanguage = context.getSession().getLanguage();
            String strSubjectKey = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
                    "emxProduct.Message.Subject.TestExecutionAssigned",strLanguage);
            String strMessageKey = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
                    "emxProduct.Message.Description.TestExecutionAssigned",strLanguage);
            strMessageKey = MessageUtil.substituteValues(
                    context, strMessageKey, strObjId, strLanguage);
            String[] subjectKeys = {};
            String[] subjectValues = {};
            String[] messageKeys = {};
            String[] messageValues = {};

            //Form the message attachment
            List lstAttachments = new StringList();
            lstAttachments.add(strObjId);

            //Send the notification to all the Assignees

             emxMailUtilBase_mxJPO.sendNotification( context,
             (StringList)lstAssigneeList, null, null, strSubjectKey,
             subjectKeys, subjectValues, strMessageKey, messageKeys,
             messageValues, (StringList)lstAttachments, null);
        }
    }

    /**
     * This trigger method sets the value of the attribute "Actual Start Date"
     * to the current system date when the Test Execution is promoted to
     * In Progress state.
     *
     * @param context The ematrix context of the request.
     * @param args The string array containing the following elements: 0 - The
     *            object id of the context Test Execution
     * @throws Exception
     * @throws FrameworkException
     */
    public void populateActualStartDate(Context context, String[] args)
            throws Exception, FrameworkException {

        //Call the setToCurrentDate method passing the object id and attribute
        // name
        setToCurrentDate(
                context, args[0], DomainConstants.ATTRIBUTE_ACTUAL_START_DATE);
    }

    /**
     * This trigger method sets the value of the attribute "Actual End Date" to
     * the current system date when the Test Execution is promoted to
     * Complete state.
     *
     * @param context The ematrix context of the request.
     * @param args The string array containing the following elements: 0 - The
     *            object id of the context Test Execution
     * @throws Exception
     * @throws FrameworkException
     */
    public void populateActualEndDate(Context context, String[] args)
            throws Exception, FrameworkException {

        //Call the setToCurrentDate method passing the object id and attribute
        // name
        setToCurrentDate(
                context, args[0], DomainConstants.ATTRIBUTE_ACTUAL_END_DATE);
    }

    /**
     * This method sets the attribute of type date to the current system date.
     * It accepts the object id and the actual name of the attribute as
     * arguments.
     *
     * @param context The ematrix context of the request.
     * @param strObjectId The object id
     * @param strAttribName The actual name of the attribute
     * @throws Exception
     * @throws FrameworkException
     */
    protected void setToCurrentDate(Context context, String strObjectId,
            String strAttribName) throws Exception, FrameworkException {
        //Get the current system date
        Calendar cal = Calendar.getInstance();
        cal.add(
                Calendar.SECOND, -1);
        Date date = cal.getTime();
        SimpleDateFormat mxDateFormat = new SimpleDateFormat(eMatrixDateFormat
                .getEMatrixDateFormat(), Locale.US);
        String strDate = mxDateFormat.format(date);

        //Set the attribute to the system date
        DomainObject domTest = DomainObject.newInstance(
                context, strObjectId);
        domTest.setAttributeValue(
                context, strAttribName, strDate);
    }
    /**
     * This method  is pre process JPO sets the row Read only if that Test Execution is in "Complete" State.
     *
     * @param context The ematrix context of the request.
     * @param args The string array containing the following elements: 0 - The
     *            object id of the context Test Execution
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.PreProcessCallable
    public HashMap setReadOnlyForTE(Context context,String[]args)throws Exception
    {
        HashMap objectMap = new HashMap();
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String contextID = (String) requestMap.get ("objectId") ;
        HashMap tableData = (HashMap) programMap.get("tableData");
        String strSelectedTable = (String) requestMap.get("selectedTable");
        MapList lstObjects = (MapList) tableData.get("ObjectList");

        Map tempObjMap;
        
        String strRowEditable = "RowEditable";
        String strReadOnly = "readonly";
        for(int i=0,n=lstObjects.size();i<n;i++)
        {
            tempObjMap = (Map) lstObjects.get(i);
            Map tempMap = new HashMap();
            String strObjId = (String) tempObjMap.get (DomainConstants.SELECT_ID);
            DomainObject domTEObj = new DomainObject(strObjId);
            String strState = domTEObj.getInfo(context,ProductLineConstants.SELECT_CURRENT);
            System.out.println("strState>>"+strState);
            if (strState.equals("Complete"))            
                tempObjMap.put(strRowEditable,strReadOnly);
            
        }
        objectMap.put("Action","CONTINUE");
        objectMap.put("ObjectList",lstObjects);
        return objectMap;
    }

	 /**
     * This trigger method sets the value of the attribute "Actual Completion Date"
     * to the current system date when the Test case is promoted to
     * complete  state.
     *
     * @param context The ematrix context of the request.
     * @param args The string array containing the following elements: 0 - The
     *            object id of the context Test Execution
     * @throws Exception
     * @throws FrameworkException
     */
    public void SetTestCaseCompletionDate(Context context, String[] args)
            throws Exception, FrameworkException
	{
		try
    	  {
    		  //use super user to overcome access issue
    		  ContextUtil.pushContext(context);
   		  
    		  //Call the setToCurrentDate method passing the object id and attribute name
    		  setToCurrentDate(context, args[0], ProductLineConstants.ATTRIBUTE_ACTUAL_COMPLETION_DATE);
    	  }
    	  catch (Exception e)
    	  {
    		  e.printStackTrace();
    	  }
    	  finally
    	  {
    		  // Set the context back to the context user
    		  ContextUtil.popContext(context);
    	  }
    }

    public String displayCopyParameterCheckBox(Context context, String[] args) throws Exception{
    	return  EnoviaResourceBundle.getProperty(context, "emxProductLineStringResource", context.getLocale(),"emxProduct.Form.Label.CopyParameter")+ "<input type=\"checkbox\" name=\"CopyParameter\"/>";
    }

 // ++ HAT1 ZUD: IR-439318-3DEXPERIENCER2017x fix
    
    /**
     * Method is used to validate the Estimated Start and End Date field
     * 
     * @param context the eMatrix <code>Context</code> object 
     * @param args hold the hashMap containing the following argument
     * @returns void 
     * @throws Exception if operation fails.
     * @since R212
     */
    public void validatePropertiesEstimateDate(Context context, String[] args) throws Exception
    {
    	try{
    	  HashMap hashMap = (HashMap)JPO.unpackArgs(args);
          HashMap paramMap = (HashMap) hashMap.get("paramMap");
          String strObjectId = (String) paramMap.get("objectId");
          String strNewValue = (String) paramMap.get("New Value");
          HashMap requestMap = (HashMap) hashMap.get("requestMap");
          
          HashMap fieldMap = (HashMap) hashMap.get("fieldMap");
          String fieldName = (String) fieldMap.get("name");
          
          DateFormat df = new SimpleDateFormat();
          Calendar calendar =  Calendar.getInstance();
          
          java.util.Date strEstimatedDate =null;
          
    	  double iClientTimeOffset = (new Double((String) requestMap.get("timeZone"))).doubleValue();
    	  Locale locale = (Locale)requestMap.get("localeObj");
    	  strNewValue = eMatrixDateFormat.getFormattedInputDate(context, strNewValue,
                  iClientTimeOffset,
                  locale);
    	  strEstimatedDate = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(strNewValue);
          java.util.Date strOldEstimatedDate =null;
          String strOldValue = (String) paramMap.get("Old value");
          strOldEstimatedDate = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(strOldValue);
          if(!strOldEstimatedDate.equals(strEstimatedDate)){
        	  calendar.setTime(strEstimatedDate);
              Date newEstimatedDate = calendar.getTime();
              int day_of_month = calendar.get(Calendar.DAY_OF_MONTH);
              
              Date dtTodaysDate = df.parse(df.format(new Date()));
              calendar.setTime(dtTodaysDate);
              int tday_of_month = calendar.get(Calendar.DAY_OF_MONTH);
              
              if(newEstimatedDate.before(dtTodaysDate) && day_of_month != tday_of_month)
              {
                  String language = context.getSession().getLanguage();
                  if(fieldName.equalsIgnoreCase("EstimatedStartDate"))
                  {
                      String strInvalidEstimateStartDate = EnoviaResourceBundle.getProperty(context,"ProductLine",
                    		  "emxProduct.TestExecution.Alert.InvalidEstimatedStartDate",language);
                    
                      throw new Exception(strInvalidEstimateStartDate);            	  
                  }
                  else if(fieldName.equalsIgnoreCase("EstimatedEndDate"))
                  {
                      String strInvalidEstimateEndDate = EnoviaResourceBundle.getProperty(context,"ProductLine",
                    		  "emxProduct.TestExecution.Alert.InvalidEstimatedEndDate",language);
                    
                      throw new Exception(strInvalidEstimateEndDate); 
                  }
              }
              else
              {
            	  DomainObject dom = new DomainObject(strObjectId);
                  if(fieldName.equalsIgnoreCase("EstimatedStartDate"))
                  {
                	  dom.setAttributeValue(context, ProductLineConstants.ATTRIBUTE_ESTIMATED_START_DATE , strNewValue);
                  }
            	  else if(fieldName.equalsIgnoreCase("EstimatedEndDate"))
                  {
                	  dom.setAttributeValue(context, ProductLineConstants.ATTRIBUTE_ESTIMATED_END_DATE , strNewValue);
                  }              }  
          }
         
    	}catch(Exception ex)
    	{
    		throw new FrameworkException(ex.getMessage());
    	}
    }
    
    /**
     * Method is used to validate the Estimated Start and End Date field
     * 
     * @param context the eMatrix <code>Context</code> object 
     * @param args hold the hashMap containing the following argument
     * @returns void 
     * @throws Exception if operation fails.
     * @since R419
     */
    public void validateCreateFormEstimateDate(Context context, String[] args) throws Exception
    {
    	try{
    	  HashMap hashMap = (HashMap)JPO.unpackArgs(args);
          HashMap paramMap = (HashMap) hashMap.get("paramMap");
          HashMap fieldMap = (HashMap) hashMap.get("fieldMap");
          
          String fieldName = (String) fieldMap.get("name");
          
          String strObjectId = (String) paramMap.get("objectId");
          String strNewValue = (String) paramMap.get("New Value");
          DateFormat df = new SimpleDateFormat();
          Calendar calendar =  Calendar.getInstance();
          
          java.util.Date strEstimatedDate =null;

          strEstimatedDate = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(strNewValue);

    	  calendar.setTime(strEstimatedDate);
          Date newEstimatedDate = calendar.getTime();
          int day_of_month = calendar.get(Calendar.DAY_OF_MONTH);
          
          Date dtTodaysDate = df.parse(df.format(new Date()));
          calendar.setTime(dtTodaysDate);
          int tday_of_month = calendar.get(Calendar.DAY_OF_MONTH);
          
          if(newEstimatedDate.before(dtTodaysDate) && day_of_month != tday_of_month)
          {
              String language = context.getSession().getLanguage();
              if(fieldName.equalsIgnoreCase("EstimatedStartDate"))
              {
                  String strInvalidEstimateStartDate = EnoviaResourceBundle.getProperty(context,"ProductLine",
                		  "emxProduct.TestExecution.Alert.InvalidEstimatedStartDate",language);
                
                  throw new Exception(strInvalidEstimateStartDate);            	  
              }
              else if(fieldName.equalsIgnoreCase("EstimatedEndDate"))
              {
                  String strInvalidEstimateEndDate = EnoviaResourceBundle.getProperty(context,"ProductLine",
                		  "emxProduct.TestExecution.Alert.InvalidEstimatedEndDate",language);
                
                  throw new Exception(strInvalidEstimateEndDate); 
              }
          }
          else
          {
        	  DomainObject dom = new DomainObject(strObjectId);
              if(fieldName.equalsIgnoreCase("EstimatedStartDate"))
              {
            	  dom.setAttributeValue(context, ProductLineConstants.ATTRIBUTE_ESTIMATED_START_DATE , strNewValue);
              }
        	  else if(fieldName.equalsIgnoreCase("EstimatedEndDate"))
              {
            	  dom.setAttributeValue(context, ProductLineConstants.ATTRIBUTE_ESTIMATED_END_DATE , strNewValue);
              }
          }  
         
    	}catch(Exception ex)
    	{
    		throw new FrameworkException(ex.getMessage());
    	}
    }
    // -- HAT1 ZUD: IR-439318-3DEXPERIENCER2017x fix
    
}
