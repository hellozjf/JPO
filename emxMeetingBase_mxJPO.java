/*
 *  emxMeetingBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */
import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import matrix.db.Attribute;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.Context;
import matrix.db.ExpansionIterator;
import matrix.db.JPO;
import matrix.db.Relationship;
import matrix.db.RelationshipItr;
import matrix.db.RelationshipType;
import matrix.util.List;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Document;
import com.matrixone.apps.common.Meeting;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Search;
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
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UITableIndented;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.jdom.Element;

/**
 * @version AEF Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxMeetingBase_mxJPO extends emxDomainObject_mxJPO
{
    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */


    /** A string constant with the value && . */
    public static final String SYMB_AND = " && ";
    /** A string constant with the value ~~. */
    public static final String SYMB_MATCH = " ~~ ";
    /** A string constant with the value *. */
    public static final String SYMB_WILD = "*";
    /** A string constant with the value (. */
    public static final String SYMB_OPEN_PARAN = "(";
    /** A string constant with the value ). */
    public static final String SYMB_CLOSE_PARAN = ")";
    /** A string constant with the value "'". */
    public static final String SYMB_QUOTE = "'";
    /** A string constant with the value vaultOption. */
    public static final String VAULT_OPTION = "vaultOption";
    /** A string constant with the value true. */
    public static final String TRUE = "true";
    /** A string constant with the value !. */
    public static final String SYMB_NOT = "!";
    /** A string constant with the value "to". */
    public static final String SYMB_TO = "to";
    /** A string constant with the value "from". */
    public static final String SYMB_FROM = "from";
    /** A string constant with the value [. */
    public static final String SYMB_OPEN_BRACKET = "[";
    /** A string constant with the value ]. */
    public static final String SYMB_CLOSE_BRACKET = "]";
    /** A string constant with the value ".". */
    public static final String SYMB_DOT = ".";
    /** A string constant with the value ==. */
    public static final String SYMB_EQUAL = " == ";
   /** A string constant with the value !=. */
    public static final String SYMB_NOT_EQUAL = " != ";
    /** A string constant with the value Relationship Agenda Item. */
    public static final String RELATIONSHIP_AGENDA_ITEM = PropertyUtil.getSchemaProperty("relationship_AgendaItem");//"Agenda Item";
    public static final String RELATIONSHIP_AGENDA_RESPONSIBILITY = PropertyUtil.getSchemaProperty("relationship_AgendaResponsibility");
    /** A string constant with the value Type Agenda Item. */
   // public static final String TYPE_AGENDA_ITEM = "Agenda Item";
    /** A string constant with the value Attribute Meeting Site Name. */
   // public static final String ATTRIBUTE_MEETING_SITE_NAME = PropertyUtil.getSchemaProperty("attribute_MeetingSiteName");//"Meeting Site Name";
    /** A string constant with the value Attribute Meeting Site Id. */
   // public static final String ATTRIBUTE_MEETING_SITE_ID = PropertyUtil.getSchemaProperty("attribute_MeetingSiteID");//"Meeting Site ID";
    /** A string constant with the Project Member. */
    public static final String STR_PROJECT_MEMBER = DomainConstants.TYPE_PROJECT_MEMBER; //"Project Member";
    /** A string constant with the Relationship Project Members. */
    public static final String RELATIONSHIP_PROJECT_MEMBERS = PropertyUtil.getSchemaProperty("relationship_ProjectMembers");//"Project Members";
    /** A string constant with the value Attribute Host Meeting. */
    public static final String ATTRIBUTE_HOST_MEETING = PropertyUtil.getSchemaProperty("attribute_HostMeetings");//"Host Meetings";
    /** A string constant with the value State Scheduled. */
    public static final String STATE_SCHEDULED = "state_Scheduled";
    /** A string constant with the value State In Progress. */
    public static final String STATE_IN_PROGRESS = "state_InProgress";
    /** A string constant with the value State Complete. */
    public static final String STATE_COMPLETE = "state_Complete";
    /** A string constant with the value Attribute Meeting Type. */
    public static final String ATTRIBUTE_MEETING_TYPE = PropertyUtil.getSchemaProperty("attribute_MeetingType");//"Meeting Type";
    /** A string constant with the value Attribute Meeting Context. */
    public static final String ATTRIBUTE_MEETING_CONTEXT = PropertyUtil.getSchemaProperty("attribute_MeetingContext");//"Meeting Context";
    //Sequence
    /** A string constant with the value Attribute Sequence. */
    public static final String ATTRIBUTE_SEQUENCE = PropertyUtil.getSchemaProperty("attribute_Sequence");//"Sequence";
    //"Topic Duration"
    /** A string constant with the value Attribute Topic Duration. */
    public static final String ATTRIBUTE_TOPIC_DURATION = PropertyUtil.getSchemaProperty("attribute_TopicDuration");//"Topic Duration";
    /** A string constant with the value Attribute Topic . */
    public static final String ATTRIBUTE_TOPIC = PropertyUtil.getSchemaProperty("attribute_Topic");//"Topic";

    public static final String SYMB_ATTRIBUTE = "attribute";

    private static final String STATE_MEETING_CREATE = PropertyUtil.getSchemaProperty("policy", POLICY_MEETING, "state_Create");
    private static final String STATE_MEETING_SCHEDULED = PropertyUtil.getSchemaProperty("policy", POLICY_MEETING, "state_Scheduled");
    private static final String STATE_MEETING_IN_PROGRESS = PropertyUtil.getSchemaProperty("policy", POLICY_MEETING, "state_InProgress");
    private static final String STATE_MEETING_COMPLETE = PropertyUtil.getSchemaProperty("policy", POLICY_MEETING, "state_Complete");


    public emxMeetingBase_mxJPO(Context context, String[] args) throws Exception
    {
        super(context, args);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns nothing
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (!context.isConnected())
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
        return 0;
    }

    /**
     * Sends Notification to Meeting Attendees.
     *
     * @param context the eMatrix Context object
     * @param String array contains Meetingid, flag to send mail to Existing Attendees
     * @return void
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public void meetingNotification(matrix.db.Context context, String sMeetingId, String strNotification)
        throws Exception
    {

        DomainObject busMeeting     = DomainObject.newInstance(context,sMeetingId);
        //To get Details of Meeting Owner,Start Date and Note
        StringList selects = new StringList();
        selects.addElement(busMeeting.SELECT_DESCRIPTION);
        selects.addElement(busMeeting.SELECT_OWNER);
        selects.addElement("attribute["+busMeeting.ATTRIBUTE_MEETING_STARTDATETIME+"]");

        String sOwner               = busMeeting.getInfo(context,busMeeting.SELECT_OWNER);
        String sMeetingDate         = busMeeting.getInfo(context,"attribute["+busMeeting.ATTRIBUTE_MEETING_STARTDATETIME+"]");
        String sMeetingNote         = busMeeting.getInfo(context,busMeeting.SELECT_DESCRIPTION);

        //Setting Type and Relationship pattern objects and select statements
        Pattern patternRelationship = new Pattern(busMeeting.RELATIONSHIP_ASSIGNED_MEETINGS);
        Pattern patternType         = new Pattern(busMeeting.TYPE_PERSON);
        StringList relSelects       = new StringList();
        relSelects.addElement(busMeeting.SELECT_RELATIONSHIP_ID);
        StringList typeSelects      = new StringList();
        typeSelects.addElement(busMeeting.SELECT_NAME);

        // To get all members connected to the Meeting
        MapList mapPerson = busMeeting.getRelatedObjects(context,
                                                       patternRelationship.getPattern(),  //String relPattern
                                                       patternType.getPattern(),          //String typePattern
                                                       typeSelects,                       //StringList objectSelects,
                                                       relSelects,                        //StringList relationshipSelects,
                                                       true,                              //boolean getTo,
                                                       false,                             //boolean getFrom,
                                                       (short)1,                          //short recurseToLevel,
                                                       "",                                //expandTypeWhere,
                                                       "",                                //String relationshipWhere,
                                                       null,                              //Pattern includeType,
                                                       null,                              //Pattern includeRelationship,
                                                       null);

        //Iterating the maplist to get the Attendee's detail
        try {
            ContextUtil.startTransaction(context, true);
            Iterator mapItr = mapPerson.iterator();
            while (mapItr.hasNext())
            {
                Map map                     = (Map)mapItr.next();
                String sRelationshipId         = (String) map.get(busMeeting.SELECT_RELATIONSHIP_ID);
                String sAttendeeName           = (String) map.get(busMeeting.SELECT_NAME);
                DomainRelationship Obj  = DomainRelationship.newInstance(context,sRelationshipId);
                String sAttributeValue         = Obj.getAttributeString(context, busMeeting.ATTRIBUTE_MEETING_ATTENDEE);

                //To send mail to newly Added Attendee, Existing Attendee and Remove Attendee
                emxMailUtil_mxJPO mailUtil = new emxMailUtil_mxJPO(context, null);

                //To set value to Meeting note if it is empty
                if(sMeetingNote == null || sMeetingNote.equals(""))
                {
                    String [] noteArguments = new String [3];
                    noteArguments[0]="emxFramework.ProgramObject.eServicecommonTrigaNotifyMeetingInvitation_if.None";
                    noteArguments[1]="0";
                    noteArguments[2]="";
                    sMeetingNote = mailUtil.getMessage(context,noteArguments);
                }
                //To send mail to newly Added Attendee
                if ("Add".equals(sAttributeValue))
                {
                    //Construct String array to send mail notification
                    String [] mailArguments = new String [12];
                    mailArguments[0] = sAttendeeName;
                    mailArguments[1] = "emxFramework.ProgramObject.eServicecommonTrigaNotifyMeetingInvitation_if.Subject";
                    mailArguments[2] = "0";
                    mailArguments[3] = "emxFramework.ProgramObject.eServicecommonTrigaNotifyMeetingInvitation_if.Message";
                    mailArguments[4] = "3";
                    mailArguments[5] = "host";
                    mailArguments[6] = sOwner;
                    mailArguments[7] = "date";
                    mailArguments[8] = sMeetingDate;
                    mailArguments[9] = "note";
                    mailArguments[10]= sMeetingNote;
                    mailArguments[11]= sMeetingId;
                    //Calling method to send Mail
                    mailUtil.sendNotificationToUser(context,mailArguments);
                    //Setting Meeting Attendee Attribute as Notified for the newly added Attendees
                    try {
                        ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), null, context.getVault().getName());
                        Obj.setAttributeValue(context,busMeeting.ATTRIBUTE_MEETING_ATTENDEE ,"Notified");
                    } catch (Exception e) {
                        throw e;
                    } finally {
                        ContextUtil.popContext(context);
                    }
                }
                  //To send mail to Removed Attendee
                  else if ("Remove".equals(sAttributeValue) )
                {
                    //Construct String array to send mail notification
                    String [] mailArguments = new String [11];
                    mailArguments[0] = sAttendeeName;
                    mailArguments[1] = "emxFramework.ProgramObject.eServicecommonTrigaNotifyMeetingAttendeeRemoval_if.Subject";
                    mailArguments[2] = "0";
                    mailArguments[3] = "emxFramework.ProgramObject.eServicecommonTrigaNotifyMeetingAttendeeRemoval_if.Message";
                    mailArguments[4] = "3";
                    mailArguments[5] = "host";
                    mailArguments[6] = sOwner;
                    mailArguments[7] = "date";
                    mailArguments[8] = sMeetingDate;
                    mailArguments[9] = "note";
                    mailArguments[10]= sMeetingNote;
                    //Calling method to send Mail
                    mailUtil.sendNotificationToUser(context,mailArguments);
                }
                  //To send mail to Existing Attendee
                  else if ("Notified".equals(sAttributeValue))
                {
                    //Construct String array to send mail notification
                    String [] mailArguments=new String [12];
                    mailArguments[0] = sAttendeeName;
                    mailArguments[1] = "emxFramework.ProgramObject.eServicecommonTrigaNotifyMeetingUpdated_if.Subject";
                    mailArguments[2] = "0";
                    mailArguments[3] = "emxFramework.ProgramObject.eServicecommonTrigaNotifyMeetingUpdated_if.Message";
                    mailArguments[4] = "3";
                    mailArguments[5] = "host";
                    mailArguments[6] = sOwner;
                    mailArguments[7] = "date";
                    mailArguments[8] = sMeetingDate;
                    mailArguments[9] = "note";
                    mailArguments[10]= sMeetingNote;
                    mailArguments[11]= sMeetingId;
                    //Calling method to send Mail
                    mailUtil.sendNotificationToUser(context,mailArguments);
                }
            }
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw e;
        }
    }

    /**
     * Gets the list Meetings for Meeting Summary.
     *
     * @param context the eMatrix Context object
     * @param String array contains Context of Meetings Id
     * @return MapList
     * @throws Exception if the operation fails
     * @since V6 R207 Author : Louis M
     * @grade 0
     */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getMeetings(matrix.db.Context context, String[] args) throws Exception
   {

       HashMap requestMap          = (HashMap) JPO.unpackArgs(args);
       String strContextId = (String)requestMap.get("objectId");

       matrix.util.Pattern typePattern = new matrix.util.Pattern(DomainObject.TYPE_MEETING);
       matrix.util.Pattern relPattern  = new matrix.util.Pattern(DomainObject.RELATIONSHIP_MEETING_CONTEXT);

       com.matrixone.apps.common.Person busPerson = com.matrixone.apps.common.Person.getPerson(context);
       DomainObject meetingContextObj = DomainObject.newInstance(context);
       meetingContextObj.setId(strContextId);

       StringList selectTypeStmts = new StringList();

       selectTypeStmts.add(DomainObject.SELECT_ID);
       selectTypeStmts.add(DomainObject.SELECT_TYPE);
       selectTypeStmts.add(DomainObject.SELECT_NAME);
       selectTypeStmts.add(DomainObject.SELECT_DESCRIPTION);
       selectTypeStmts.add(DomainObject.SELECT_OWNER);
       selectTypeStmts.add(DomainObject.SELECT_CURRENT);
       selectTypeStmts.add(DomainObject.SELECT_POLICY);

       selectTypeStmts.add("attribute["+DomainObject.ATTRIBUTE_MEETING_STARTDATETIME+"]");
       selectTypeStmts.add("attribute["+DomainObject.ATTRIBUTE_MEETING_DURATION+"]");
       selectTypeStmts.add("attribute["+DomainObject.ATTRIBUTE_MEETING_KEY+"]");

       StringBuffer expandTypeWhere = new StringBuffer("");
       expandTypeWhere.append("('").append(DomainObject.SELECT_OWNER).append("' == '").append(busPerson.getName());
       expandTypeWhere.append("' || '");
       expandTypeWhere.append(DomainObject.SELECT_CURRENT).append("' != 'Create')");

       MapList totalresultList = new MapList();

       totalresultList = meetingContextObj.getRelatedObjects(context,
               relPattern.getPattern(),  //String relPattern
               typePattern.getPattern(), //String typePattern
               selectTypeStmts,          //StringList objectSelects,
               null,                     //StringList relationshipSelects,
               false,                     //boolean getTo,
               true,                     //boolean getFrom,
               (short)1,                 //short recurseToLevel,
               expandTypeWhere.toString(),          //String objectWhere,
               "",                       //String relationshipWhere,
               null,                     //Pattern includeType,
               null,                     //Pattern includeRelationship,
               null);
       return totalresultList;

   }
  @com.matrixone.apps.framework.ui.PostProcessCallable
  public HashMap postProcessRefresh (Context context, String[] args) throws Exception
    {
            HashMap returnMap = new HashMap(1);
            returnMap.put("Action","refresh");
            return returnMap;
    }
  /**
     * Gets the  Meeting Context as program HTML Output.
     *
     * @param context the eMatrix Context object
     * @param String array contains Meetings Id
     * @return String
     * @throws Exception if the operation fails
     * @since V6 R207 Author : Louis M
     * @grade 0
     */
  public String getMeetingContext(Context context,String[] args) throws Exception {
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap         = (HashMap)programMap.get("requestMap");
        String reportFormat=(String) requestMap.get("reportFormat");

        String strMode = (String)requestMap.get("Mode");
        String ObjectId = (String)requestMap.get("objectId");

        //Modified:30-August-2010:s4e:R210 PRG:IR-067229V6R2011x
      //While exporting mode is empty and reportFormat ="CSV".
        if((isEmpty(ObjectId) || isEmpty(strMode)) && !"CSV".equalsIgnoreCase(reportFormat))
            return "";
        //End:Modified:30-August-2010:s4e:R210 PRG:IR-067229V6R2011x
        DomainObject ObjectDom = DomainObject.newInstance(context, ObjectId);

        if("create".equals(strMode)){
            return ObjectDom.getInfo(context,DomainConstants.SELECT_NAME);
        }else {
               //If the login user is not having access to the object we will get null with the relation, so we are trying to push the context
               //to get the meeting context object. This is just to dispaly the meeting context object name.
            Map contextObj = getMeetingContextObject(context, ObjectDom, new StringList(DomainConstants.SELECT_NAME), true);
            return  contextObj == null ? "" : (String)contextObj.get(DomainConstants.SELECT_NAME);
        }
    }
    /**
     * Gets the  Meeting Start Time combobox in Create Meeting Page (program HTML Output).
     *
     * @param context the eMatrix Context object
     * @param String array contains Meetings Id
     * @return String
     * @throws Exception if the operation fails
     * @since V6 R207 Author : Louis M
     * @grade 0
     */

  public String getMeetingStartTime(Context context,String[] args) throws Exception {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      HashMap requestMap = (HashMap) programMap.get("requestMap");
      String strMeetingId = (String)requestMap.get("objectId");
      String strMode = (String)requestMap.get("Mode");
      StringBuffer strMeetingStartTime = new StringBuffer();
      if(!UIUtil.isNullOrEmpty(strMode) && strMode.equals("create")){
            strMeetingStartTime.append("8:30:00 AM");
      } else if(!UIUtil.isNullOrEmpty(strMeetingId)){
          double clientTZOffset  = Double.parseDouble((String)requestMap.get("timeZone"));
          strMeetingStartTime.append(getFormattedMeetingStartTime(context, new Meeting(strMeetingId), clientTZOffset, false));
      }
      return strMeetingStartTime.toString();
  }

    /**
     * This method returns the meeting date and time in 'MMM DD, YYYY HH:MM:SS a' from
     * e.g. 5/8/2009 18:30:00 will be return as 'May 8, 2009 6:30:00 PM'
     * @param context
     * @param meetingObj
     * @param clientTZOffset
     * @return
     * @throws FrameworkException
     */

    private String getFormattedDisplayMeetingStartDateTime(Context context, Meeting meetingObj, double clientTZOffset) throws FrameworkException {
        String strDate = meetingObj.getInfo(context, "attribute["+DomainConstants.ATTRIBUTE_MEETING_START_DATETIME+"]");
        //Here we are passing locale=Locale.ENGLISH and dateformat = DateFormat.MEDIUM
        //This is to get the date time string allways in same format. i.e. in  'MMM DD, YYYY HH:MM:SS a' format.
        //Otherwise it will return different format for each locale.
        String strFormattedDisplayDateTime = eMatrixDateFormat.getFormattedDisplayDateTime(context, strDate, true, DateFormat.MEDIUM, clientTZOffset, Locale.ENGLISH);
        //Formatted Meeting date will be in the form of 'MMM DD, YYYY HH:MM:SS a' take only time part from this
        //e.g if the date is 'May 8, 2009 6:30:00 PM', take only 6:30:00 PM from this.
        return strFormattedDisplayDateTime;
    }

    /**
     * This will return the meeting starttime in the user preference time zone.
     * @param context
     * @return meeting starttime in the user preference time zone.
     * @throws FrameworkException
     */

    private String getFormattedMeetingStartTime(Context context, Meeting meetingObj, double clientTZOffset, boolean trimSeconds) throws FrameworkException {
        StringList formattedDisplayDateTimeList = FrameworkUtil.split(getFormattedDisplayMeetingStartDateTime(context, meetingObj, clientTZOffset), " ");
        String time = (String)formattedDisplayDateTimeList.get(3);
        String aa = (String)formattedDisplayDateTimeList.get(4);
        time = trimSeconds ? time.substring(0, time.lastIndexOf(':')) : time;
        return  time + " " + aa;
    }

    /**
     * This will return the meeting start date in the user preference time zone.
     * @param context
     * @return meeting start date in the user preference time zone.
     * @throws FrameworkException
     */

    private String getFormattedMeetingStartDate(Context context, Meeting meetingObj, double clientTZOffset) throws FrameworkException {
        StringList formattedDisplayDateTimeList = FrameworkUtil.split(getFormattedDisplayMeetingStartDateTime(context, meetingObj, clientTZOffset), " ");
        return (String)formattedDisplayDateTimeList.get(0) + " " +
               (String)formattedDisplayDateTimeList.get(1)+ " " +
               (String)formattedDisplayDateTimeList.get(2);
    }

    /**
     * Access Program for Create Meeting Command.
     *
     * @param context the eMatrix Context object
     * @param String array contains Meetings Id
     * @return boolean
     * @throws Exception if the operation fails
     * @since V6 R207 Author : Louis M
     * @grade 0
     */
    public boolean showCreateNewMeeting(Context context, String[] args)throws Exception {
        com.matrixone.apps.common.Person busPerson = com.matrixone.apps.common.Person.getPerson(context);
        return "Yes".equals(busPerson.getAttributeValue(context,ATTRIBUTE_HOST_MEETING));
    }
    /**
     * Gets Agenda Items for a particular Meeting.
     *
     * @param context the eMatrix Context object
     * @param String array contains Meetings Id
     * @return MapList
     * @throws Exception if the operation fails
     * @since V6 R207 Author : Louis M
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getMeetingAgendas(Context context, String[] args)throws Exception {
      final String strAttrib_ATTRIBUTE_TOPIC="attribute[" + ATTRIBUTE_TOPIC + "]";
      final String strAttrib_ATTRIBUTE_TOPIC_DURATION="attribute[" + ATTRIBUTE_TOPIC_DURATION + "]";
        String attrAgendaSeqSel = "attribute["+ATTRIBUTE_SEQUENCE+"]";

        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        String strMeetingId = (String)programMap.get("objectId");

        DomainObject MeetingObj = DomainObject.newInstance(context);
        MeetingObj.setId(strMeetingId);

        StringList selectRelStmts = new StringList();
        selectRelStmts.add(DomainConstants.SELECT_RELATIONSHIP_ID);
//Added:30-Apr-2010:di1:R210:PRG:Meeting Usability
    selectRelStmts.add(strAttrib_ATTRIBUTE_TOPIC);
    selectRelStmts.add(strAttrib_ATTRIBUTE_TOPIC_DURATION);
//Addition End:30-Apr-2010:di1:R210:PRG:Meeting Usability
        selectRelStmts.add(attrAgendaSeqSel);

        ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), null, context.getVault().getName());
        MapList agendaItemList = MeetingObj.getRelatedObjects(context,
                RELATIONSHIP_AGENDA_ITEM,  //String relPattern
                SYMB_WILD, //String typePattern
                new StringList(SELECT_ID),          //StringList objectSelects,
                selectRelStmts,                     //StringList relationshipSelects,
                false,                     //boolean getTo,
                true,                     //boolean getFrom,
                (short)1,                 //short recurseToLevel,
                "",          //String objectWhere,
                "",                       //String relationshipWhere,
                0,
                null,                     //Pattern includeType,
                null,                     //Pattern includeRelationship,
                null);
        ContextUtil.popContext(context);
        MapList agendaItemsListGroupBySeqNo = new MapList();
        StringList seqNos = new StringList();

        for (Iterator iter = agendaItemList.iterator(); iter.hasNext();) {
            Map agendaItemMap = (Map) iter.next();
            String sAgendaSeq = (String)agendaItemMap.get(attrAgendaSeqSel);
            String sRelaAgendaItemRelId = (String)agendaItemMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            String repAgainstId = (String) agendaItemMap.get(SELECT_ID);
//Added:30-Apr-2010:di1:R210:PRG:Meeting Usability
      String sAgendaTopic = XSSUtil.encodeForXML(context, (String) agendaItemMap.get(strAttrib_ATTRIBUTE_TOPIC));
      String sAgendaTopicDuration = (String) agendaItemMap.get(strAttrib_ATTRIBUTE_TOPIC_DURATION);
//Addition End:30-Apr-2010:di1:R210:PRG:Meeting Usability
            int index = seqNos.indexOf(sAgendaSeq);
            if(index == -1)
            {
                HashMap newMap = new HashMap();
                newMap.put(DomainConstants.SELECT_RELATIONSHIP_ID, sRelaAgendaItemRelId);
                newMap.put(ATTRIBUTE_SEQUENCE, sAgendaSeq);
                newMap.put(SELECT_ID, strMeetingId);
                newMap.put("ReportedAgainst", repAgainstId);
//Added:30-Apr-2010:di1:R210:PRG:Meeting Usability
                newMap.put(SELECT_NAME, sAgendaTopic);
                newMap.put(strAttrib_ATTRIBUTE_TOPIC_DURATION,sAgendaTopicDuration);
                newMap.put("relationship", RELATIONSHIP_AGENDA_ITEM);
//Addition End:30-Apr-2010:di1:R210:PRG:Meeting Usability
                seqNos.add(sAgendaSeq);
                agendaItemsListGroupBySeqNo.add(newMap);
            } else {
                Map agendaMap = (Map) agendaItemsListGroupBySeqNo.get(index);
                String repAgainst = (String) agendaMap.get("ReportedAgainst");
                repAgainst = repAgainst + "|" + repAgainstId;
                agendaMap.put("ReportedAgainst", repAgainst);
            }
        }
        return agendaItemsListGroupBySeqNo;
    }

    /**
     * Gets Meeting Summary List.
     *
     * @param context the eMatrix Context object
     * @param String array contains Meetings Id
     * @return MapList
     * @throws Exception if the operation fails
     * @since V6 R207 Author : Louis M
     * @grade 0
     */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getMeetingSummaryList(Context context,String[] args) throws Exception{
            StringBuffer relPattern = new StringBuffer("");
            relPattern.append(DomainConstants.RELATIONSHIP_MEETING_CONTEXT);
            relPattern.append(",");
            relPattern.append(DomainConstants.RELATIONSHIP_ASSIGNED_MEETINGS);

            MapList MeetingList = new MapList();

            StringList select = new StringList();
            select.addElement(DomainConstants.SELECT_ID);
            select.addElement(DomainConstants.SELECT_POLICY);
//Added:30-Apr-2010:di1:R210:PRG:Meeting Usability
          select.addElement(DomainConstants.SELECT_NAME);
        select.addElement(DomainConstants.SELECT_TYPE);
        select.addElement(DomainConstants.SELECT_OWNER);
        select.addElement(DomainConstants.SELECT_CURRENT);
        select.addElement(DomainConstants.SELECT_DESCRIPTION);
        select.addElement("attribute["+ DomainConstants.ATTRIBUTE_MEETING_DURATION + "]");
            select.addElement("attribute["+ DomainConstants.ATTRIBUTE_MEETING_START_DATETIME + "]");
//Addition End:30-Apr-2010:di1:R210:PRG:Meeting Usability

            com.matrixone.apps.common.Person busPerson=com.matrixone.apps.common.Person.getPerson(context);
            busPerson.open(context);
            String personid = busPerson.getId();
            DomainObject personDom = DomainObject.newInstance(context, personid);

            StringBuffer expandTypeWhere = new StringBuffer();
            expandTypeWhere.append("('").append(DomainConstants.SELECT_OWNER).append("' == '").append(busPerson.getName());
            expandTypeWhere.append("' || '");
            expandTypeWhere.append(DomainObject.SELECT_CURRENT).append("' != 'Create')");

            MeetingList = personDom.getRelatedObjects(context,
                    relPattern.toString(),               //String relPattern
                    DomainConstants.TYPE_MEETING,              //String typePattern
                    select,          //StringList objectSelects,
                    null,                     //StringList relationshipSelects,
                    false,                    //boolean getTo,
                    true,                     //boolean getFrom,
                    (short)0,                 //short recurseToLevel,
                    expandTypeWhere.toString(),          //String objectWhere,
                    "",                       //String relationshipWhere,
                    0,
                    null,        //Pattern includeType,
                    null,                     //Pattern includeRelationship,
                    null);                    //Map includeMap
            // MeetingList = DomainObject.findObjects(context,DomainConstants.TYPE_MEETING,"*","*","*","eService Production","",true,select);
            return MeetingList;

     }
     /**
         * Access Program to display Context Column on Meeting Summary Page.
         * If it is for Global Meeting Summary page Context colun will be displayed
         * @param context the eMatrix Context object
         * @param String array contains Meetings Id
         * @return boolean
         * @throws Exception if the operation fails
         * @since V6 R207 Author : Louis M
         * @grade 0
         */
  public boolean isContextVisible(Context context, String[] args) throws Exception {
    HashMap requestMap          = (HashMap) JPO.unpackArgs(args);
    String strObjId = (String)requestMap.get("objectId");
        return !(strObjId!=null && strObjId.length() > 0);
  }
    /**
     * Program HTML output value to display Actions column on Meeting Summary Page.
     * If it is for Global Meeting Summary page Context colun will be displayed
     * @param context the eMatrix Context object
     * @param String array contains Meetings Id
     * @return Vector containing the Actions Link
     * @throws Exception if the operation fails
     * @since V6 R207 Author : Louis M
     * @grade 0
     */
  public Vector getMeetingSummaryActions(Context context,String[] args) throws Exception{
      Map programMap                  =   (Map)JPO.unpackArgs(args);
      HashMap  requestMap             = (HashMap) programMap.get("requestMap");
      HashMap paramMap = (HashMap)programMap.get("paramList");
      String parentObjectId = (String)paramMap.get("objectId");
      MapList objectList              =   (MapList)programMap.get("objectList");
      String strReturnPage = (parentObjectId!= null) ? "MeetingSummary" : "GlobalMeetingSummary";
      String stateScheduled = (String)FrameworkUtil.lookupStateName(context,
              DomainObject.POLICY_MEETING,//java.lang.String absolutePolicyName,
              STATE_SCHEDULED);//java.lang.String symbolicStateName)
      //state_InProgress

      String stateInProgress = (String)FrameworkUtil.lookupStateName(context,
              DomainObject.POLICY_MEETING,//java.lang.String absolutePolicyName,
              STATE_IN_PROGRESS);//java.lang.String symbolicStateName)
      //String strRangesLanguage = "";
      StringBuffer strRangesLanguage1 = new StringBuffer("");
      String strBusMeetSiteName = "";
      String strMeetingType = null;
      com.matrixone.apps.common.Person personObject = com.matrixone.apps.common.Person.getPerson(context);
      BusinessObject busContextOrganization   = null;
      String businessUnit = personObject.getInfo(context, com.matrixone.apps.common.Person.SELECT_BUSINESS_UNIT_ID);
      if(businessUnit == null || "null".equals(businessUnit) || "".equals(businessUnit)){
          busContextOrganization = personObject.getCompany(context);
          // strBusMeetSiteName = getAttribute(context,busContextOrganization,ATTRIBUTE_MEETING_SITE_NAME);
      }else{
          busContextOrganization = new BusinessObject(businessUnit);
          //strBusMeetSiteName = getAttribute(context,busContextOrganization,ATTRIBUTE_MEETING_SITE_NAME);
          String strBusMeetSiteId = "";
          // String strBusMeetSiteId   = getAttribute(context,busContextOrganization,ATTRIBUTE_MEETING_SITE_ID);
          if(strBusMeetSiteName == null || "".equals(strBusMeetSiteName) || "null".equals(strBusMeetSiteName) || strBusMeetSiteId == null || "".equals(strBusMeetSiteId) || "null".equals(strBusMeetSiteId)){
              busContextOrganization = personObject.getCompany(context);

          }
      }

      String strWebExLanguageURL = EnoviaResourceBundle.getProperty(context,"emxCommon.WebExLanguageURL");
      if (strWebExLanguageURL!= null && !"".equals(strWebExLanguageURL) && strWebExLanguageURL.indexOf(",") > 0 ) {
          StringTokenizer sTokGeneric = new StringTokenizer(strWebExLanguageURL, ";");
          //strRangesLanguage += "<select name = \"meetingKey\" >";
          strRangesLanguage1.append("<select name = \"meetingKey\" >");
          while(sTokGeneric.hasMoreElements()) {
              String strToken = (String)sTokGeneric.nextToken();
              String strName = strToken.substring(0,strToken.indexOf(","));
              String strValue = strToken.substring(strToken.indexOf(",")+1, strToken.length());
              if(!"".equals(strBusMeetSiteName) && strBusMeetSiteName!=null){
                  strRangesLanguage1.append("<option value = \"");
                  strRangesLanguage1.append(FrameworkUtil.encodeURL(strBusMeetSiteName));
                  strRangesLanguage1.append("\">");
                  strRangesLanguage1.append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),strName));
                  strRangesLanguage1.append("</option>");

              }else{
                  strRangesLanguage1.append("<option value = \"");
                  strRangesLanguage1.append(FrameworkUtil.encodeURL(strValue));
                  strRangesLanguage1.append("\">");
                  strRangesLanguage1.append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),strName));
                  strRangesLanguage1.append("</option>");
              }
          }
          //strRangesLanguage += "</select>";
          strRangesLanguage1.append("</select>");
      }

      Vector ActionString = new Vector();

      for(Iterator itr=objectList.iterator();itr.hasNext();){
          Map objectMap = (Map) itr.next();
          StringBuffer stringbuffer = new StringBuffer(256);
          //get Meeting objects owner
          String strMeetingId   = (String) objectMap.get(DomainConstants.SELECT_ID);
          StringList select = new StringList();
          select.addElement(DomainConstants.SELECT_ID);
          select.addElement(DomainConstants.SELECT_CURRENT);
          select.addElement(DomainConstants.SELECT_OWNER);
          select.addElement("attribute["+ATTRIBUTE_MEETING_TYPE+"]");
          DomainObject MeetingDom = DomainObject.newInstance(context,strMeetingId);
          java.util.Map MeetingInfoList= (java.util.Map)MeetingDom.getInfo(context,
                  select);
          String strMeetingOwner   = (String) MeetingInfoList.get(DomainConstants.SELECT_OWNER);
          String strMeetingStatus  = (String) MeetingInfoList.get(DomainConstants.SELECT_CURRENT);
          strMeetingType = (String) MeetingInfoList.get("attribute["+ATTRIBUTE_MEETING_TYPE+"]");
          String temp = strRangesLanguage1.toString();
	   //[IR-071535V6R2016x]:START [Tooltip]
	  Locale strLocale = new Locale(context.getLocale().getLanguage());
	  String startMeeting = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",strLocale,"emxComponents.Meeting.StartMeeting");
	  String joinMeeting = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",strLocale, "emxComponents.Meeting.JoinMeeting");
	  String closeMeeting = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",strLocale,"emxComponents.Meeting.CloseMeeting");
		  //[IR-071535V6R2016x]:START	
          if("3D Visual Meeting".equals(strMeetingType)){
               if ( context.getUser().equals(strMeetingOwner) ) {
                  if ( stateScheduled.equals(strMeetingStatus) ) {
					  stringbuffer.append("<a href=\"javascript:emxTableColumnLinkClick('" );
					  stringbuffer.append(FrameworkUtil.encodeURL("../integrations/c3d/C3DStartJoinAutoVueMeeting.jsp?meetingId="+XSSUtil.encodeForJavaScript(context, strMeetingId)+"&strAction=StartMeeting&meetingType="+strMeetingType));
					  stringbuffer.append("','700','600','false','popup','');\">");
					  stringbuffer.append("<img src=\"images/iconActionStartMeeting.gif\" valign=\"middle\" border=\"0\" height=\"15\" width=\"15\" title=\""+ startMeeting + "\" alt=\""+ startMeeting + "\"/></a>");
                  }
                  else if (stateInProgress.equals(strMeetingStatus) ) {
					  stringbuffer.append("<a href=\"javascript:emxTableColumnLinkClick('");
					  stringbuffer.append(FrameworkUtil.encodeURL("../integrations/c3d/C3DStartJoinAutoVueMeeting.jsp?meetingId="+XSSUtil.encodeForJavaScript(context, strMeetingId)+"&strAction=JoinMeeting&meetingType="+strMeetingType));
					  stringbuffer.append("','700','600','false','popup','');\">");
					  stringbuffer.append("<img src=\"images/iconActionJoinMeeting.gif\" valign=\"middle\" border=\"0\" title=\""+ joinMeeting + "\" alt=\""+ joinMeeting + "\"/></a>");
					  stringbuffer.append("<a href=\"javascript:emxTableColumnLinkClick('");
					  stringbuffer.append(FrameworkUtil.encodeURL("../components/emxMeetingProcess.jsp?action=CloseMeeting&notification=Yes&sReturnPage="+strReturnPage+"&objectId="+XSSUtil.encodeForJavaScript(context, strMeetingId)));
					  stringbuffer.append("','700','600','false','listHidden','');\">");
					  stringbuffer.append("<img src=\"images/iconActionStopMeeting.gif\" valign=\"middle\" border=\"0\" title=\""+ closeMeeting + "\" alt=\""+ closeMeeting + "\"/></a>");
                  }else {
                      stringbuffer.append("");
                  }
              }else  if ( stateInProgress.equals(strMeetingStatus) ) {
				  stringbuffer.append("<a href=\"javascript:emxTableColumnLinkClick('" );
				  stringbuffer.append(FrameworkUtil.encodeURL("../integrations/c3d/C3DStartJoinAutoVueMeeting.jsp?meetingId="+XSSUtil.encodeForJavaScript(context, strMeetingId)+"&strAction=JoinMeeting&meetingType="+strMeetingType));
				  stringbuffer.append("','700','600','false','popup','');\">");
				  stringbuffer.append("<img src=\"images/iconActionJoinMeeting.gif\" valign=\"middle\" border=\"0\" title=\""+ joinMeeting + "\" alt=\""+ joinMeeting + "\"/></a>");
              }
              else {
                  stringbuffer.append("");
              }
          } //[IR-071535V6R2016x]:END	//else meeting type is other than "None"
		  else if(!"None".equals(strMeetingType)){
              // Not an Offline Meeting, Meeting can be of type WebEx.The icons for Start, Join and close Meeting will be visible.
              if ( context.getUser().equals(strMeetingOwner) ) {
                  if ( stateScheduled.equals(strMeetingStatus) ) {
                      stringbuffer.append(strRangesLanguage1.toString());
                      stringbuffer.append("<a href=\"javascript:emxTableColumnLinkClick('"+FrameworkUtil.encodeURL("../components/emxMeetingProcess.jsp?action=StartMeeting&notification=Yes&sReturnPage="+strReturnPage+"&objectId="+XSSUtil.encodeForJavaScript(context, strMeetingId))+"','700','600','false','popup','');\">");

                      stringbuffer.append("<img src=\""+FrameworkUtil.encodeURL("images/iconMeetingStart.gif")+"\" valign=\"middle\" border=\"0\" title=\""+ startMeeting + "\" alt=\""+ startMeeting + "\"/></a>");
                  }
                  else if ( stateInProgress.equals(strMeetingStatus) ) {
                      stringbuffer.append(strRangesLanguage1.toString());
                      stringbuffer.append("<a href=\"javascript:emxTableColumnLinkClick('"+FrameworkUtil.encodeURL("../components/emxMeetingProcess.jsp?action=JoinMeeting&notification=Yes&sReturnPage="+strReturnPage+"&objectId="+XSSUtil.encodeForJavaScript(context, strMeetingId))+"','700','600','false','popup','');\">");
                      stringbuffer.append("<img src=\""+FrameworkUtil.encodeURL("images/iconMeetingJoin.gif")+"\" valign=\"middle\" border=\"0\" title=\""+ joinMeeting + "\" alt=\""+ joinMeeting + "\"/></a>");
                      stringbuffer.append("<a href=\"javascript:emxTableColumnLinkClick('"+FrameworkUtil.encodeURL("../components/emxMeetingProcess.jsp?action=CloseMeeting&notification=Yes&sReturnPage="+strReturnPage+"&objectId="+XSSUtil.encodeForJavaScript(context, strMeetingId))+"','700','600','false','listHidden','');\">");
                      stringbuffer.append("<img src=\""+FrameworkUtil.encodeURL("images/iconCloseMeetingSmall.gif")+"\" valign=\"middle\" border=\"0\" title=\""+ closeMeeting + "\" alt=\""+ closeMeeting + "\"/></a>");
                  }else {
                      stringbuffer.append("");
                  }


              }else  if ( stateInProgress.equals(strMeetingStatus) ) {
                  stringbuffer.append(strRangesLanguage1.toString());
                  stringbuffer.append("<img src=\""+FrameworkUtil.encodeURL("images/iconMeetingJoin.gif")+"\" valign=\"middle\" border=\"0\" title=\""+ joinMeeting + "\" alt=\""+ joinMeeting + "\"/></a>");
              }//[IR-071535V6R2016x]:END [Tooltip]
              else {
                  stringbuffer.append("");
              }

          }else{
              // Offline Meeting
              stringbuffer.append("");
          }
          ActionString.add(stringbuffer.toString());

      }
      return ActionString;

  }
    /**
     * This method returns vector of Meeting Start time for all the Meetings to display in Meeting Summary page
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Vector getMeetingSummaryStartTime(Context context, String[] args) throws Exception {
        Map programMap =   (Map)JPO.unpackArgs(args);
        MapList objectList =   (MapList)programMap.get("objectList");
        double clientTZOffset = Double.parseDouble((String)((Map)programMap.get("paramList")).get("timeZone"));

        Vector meetingStartTime = new Vector();
    for (Iterator itr = objectList.iterator(); itr.hasNext();) {
            Map objectMap = (Map) itr.next();
      String strObjectId = (String) objectMap.get(SELECT_ID);
//Added:30-Apr-2010:di1:R210:PRG:Meeting Usability
      String strObjectType = (String) objectMap.get(SELECT_TYPE);

      if (TYPE_MEETING.equals(strObjectType)) {
        meetingStartTime.add(getFormattedMeetingStartTime(context,
            new Meeting(strObjectId), clientTZOffset, true));
      }

      else {
        meetingStartTime.add("");
      }
//Addition End:30-Apr-2010:di1:R210:PRG:Meeting Usability
        }
        return meetingStartTime;
    }

    /**
     * Program HTL output value to display Context field on Meeting Summary Page.
     *
     * @param context the eMatrix Context object
     * @param String array contains Meetings Id
     * @return Vector containing the Context object
     * @throws Exception if the operation fails
     * @since V6 R207 Author : Louis M
     * @grade 0
     */
  public Vector getMeetingSummaryContext(Context context, String[] args) throws Exception {
        Map programMap =   (Map)JPO.unpackArgs(args);
        MapList objectList =   (MapList)programMap.get("objectList");
  Vector objContext = new Vector();
  for (Iterator itr = objectList.iterator(); itr.hasNext();) {
            Map objectMap = (Map) itr.next();

      String strObjId = (String) objectMap.get(DomainConstants.SELECT_ID);
      DomainObject objDom = DomainObject.newInstance(context, strObjId);

            StringList selects = new StringList();
            selects.addElement(DomainConstants.SELECT_NAME);
            selects.addElement(DomainConstants.SELECT_ID);

      Map contextObjectMap = getMeetingContextObject(context, objDom, selects, false);

            if(contextObjectMap == null) {
        // If context map == null mean the login person may not have
        // access to the context object.
        // Get the context object by pushing the context and show it
        // with out hyper link.
        contextObjectMap = getMeetingContextObject(context, objDom, selects, true);
        if (contextObjectMap == null) // Context object might have
          // deleted.
          objContext.add("");
                else
          objContext.add(contextObjectMap.get(DomainConstants.SELECT_NAME));

            } else {
//Added:30-Apr-2010:di1:R210:PRG:Meeting Usability
              String strobjType = (String) objectMap.get(DomainConstants.SELECT_TYPE);
        if (TYPE_MEETING.equals(strobjType)) {
          String Context = (String) (contextObjectMap.get(SELECT_NAME));
          String contextId = (String) (contextObjectMap.get(SELECT_ID));
                StringBuffer stringbuffer = new StringBuffer(256);
          stringbuffer.append("<a href=\"javascript:emxTableColumnLinkClick('");
          stringbuffer.append(FrameworkUtil.encodeURL("emxTree.jsp?mode=replace&objectId="));
          stringbuffer.append(contextId);
          stringbuffer.append("','700','600','false','popup','');\">");
          stringbuffer.append(XSSUtil.encodeForXML(context,Context));
                stringbuffer.append("</a>");
          objContext.add(stringbuffer.toString());
        } else {
          objContext.add("");
            }
//Addition End:30-Apr-2010:di1:R210:PRG:Meeting Usability
            }
        }
    return objContext;
    }
    /**
     * Access program for Edit Meeting command.
     *
     * @param context the eMatrix Context object
     * @param String array contains Meetings Id
     * @return boolean
     * @throws Exception if the operation fails
     * @since V6 R207 Author : Louis M
     * @grade 0
     */
  public boolean checkEditMeeting(Context context, String[] args) throws Exception{
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    String strMeetingId = (String)programMap.get("objectId");
    String stateScheduled = (String)FrameworkUtil.lookupStateName(context,
                                               DomainObject.POLICY_MEETING,//java.lang.String absolutePolicyName,
                                               STATE_SCHEDULED);//java.lang.String symbolicStateName)
    //state_InProgress
    String stateInProgress = (String)FrameworkUtil.lookupStateName(context,
                                               DomainObject.POLICY_MEETING,//java.lang.String absolutePolicyName,
                                               STATE_IN_PROGRESS);//java.lang.String symbolicStateName)
    String stateComplete = (String)FrameworkUtil.lookupStateName(context,
                                               DomainObject.POLICY_MEETING,//java.lang.String absolutePolicyName,
                                               STATE_COMPLETE);//java.lang.String symbolicStateName)


    String strLoggedInUser = context.getUser();
    DomainObject MeetingDom = DomainObject.newInstance(context,strMeetingId);
    StringList selects = new StringList();
    selects.addElement(DomainConstants.SELECT_OWNER);
    selects.addElement(DomainConstants.SELECT_CURRENT);
    java.util.Map MeetingInfoList = (java.util.Map)MeetingDom.getInfo(context,selects);
    String strOwner = (String)MeetingInfoList.get(DomainConstants.SELECT_OWNER);
    String strMeetingStatus = (String)MeetingInfoList.get(DomainConstants.SELECT_CURRENT);
    if(strLoggedInUser.equals(strOwner)){
      if (!stateScheduled.equals(strMeetingStatus) && !stateComplete.equals(strMeetingStatus) && !stateInProgress.equals(strMeetingStatus)) {
        return true;
      }else return false;
    }else
      return false;


  }
     /**
     * Method to Promote Meeting.
     *
     * @param context the eMatrix Context object
     * @param String array contains Meetings Id
     * @return void
     * @throws Exception if the operation fails
     * @since V6 R207 Author : Louis M
     * @grade 0
     */
  public void promoteMeeting(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId = (String)programMap.get("objectId");
        if(!"Promote".equals(programMap.get("strAction"))) {
            return;
        }
        Meeting busMeeting = new Meeting(strObjectId);
        try {
            ContextUtil.startTransaction(context, true);
            busMeeting.promote(context);
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw e;
        }
  }
    /**
     * Method to Demote Meeting.
     *
     * @param context the eMatrix Context object
     * @param String array contains Meetings Id
     * @return void
     * @throws Exception if the operation fails
     * @since V6 R207 Author : Louis M
     * @grade 0
     */
    public void demoteMeeting(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId = (String)programMap.get("objectId");
        if(!"Demote".equals(programMap.get("strAction"))) {
            return;
        }
        Meeting busMeeting = new Meeting(strObjectId);
        try {
            ContextUtil.startTransaction(context, true);
            busMeeting.demote(context);
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw e;
        }
    }
    /**
     * Post process method after Promote or Demote Meeting.
     * Send notification to the recipients when the Meeting is Scheduled
     * @param context the eMatrix Context object
     * @param String array contains Meetings Id
     * @return void
     * @throws Exception if the operation fails
     * @since V6 R207 Author : Louis M
     * @grade 0
     */
    public void postPromoteDemoteProcess(Context context, String[] args) throws Exception{

        String strObjectId = args[0];
        String strNotification = args[1];

        Meeting busMeeting = new Meeting(strObjectId);
        busMeeting.open(context);

        String strStatus        = (String)busMeeting.getInfo(context,DomainObject.SELECT_CURRENT);
        String stateScheduled = FrameworkUtil.lookupStateName(context, DomainObject.POLICY_MEETING, STATE_SCHEDULED);
        if(strStatus.equals(stateScheduled)){
            if(strNotification != null){
                try {
                    meetingNotification(context, strObjectId, strNotification);
                }catch(Exception ex){
                    throw ex;
                }
            }

            Pattern patternRelationship  = new Pattern(DomainConstants.RELATIONSHIP_ASSIGNED_MEETINGS);
            Pattern patternType          = new Pattern(DomainConstants.TYPE_PERSON);

            StringList relSelects = new StringList();
            relSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
            relSelects.addElement("attribute["+DomainConstants.ATTRIBUTE_MEETING_ATTENDEE+"]");

            StringList typeSelects = new StringList(DomainConstants.SELECT_ID);

            MapList mapPerson = busMeeting.getRelatedObjects(context,
                    patternRelationship.getPattern(),//String relPattern
                    patternType.getPattern(), //String typePattern
                    typeSelects,          //StringList objectSelects,
                    relSelects,              //StringList relationshipSelects,
                    true,                     //boolean getTo,
                    false,                     //boolean getFrom,
                    (short)1,                 //short recurseToLevel,
                    "",                       //expandTypeWhere,          //String objectWhere,
                    "",                       //String relationshipWhere,
                    null,
                    null,                     //Pattern includeRelationship,
                    null);
            try{
                ContextUtil.startTransaction(context, true);
                Iterator mapItr = mapPerson.iterator();
                while (mapItr.hasNext()) {
                    Map map  = (Map)mapItr.next();
                    String sMeetingAttendee = (String)map.get("attribute["+DomainConstants.ATTRIBUTE_MEETING_ATTENDEE+"]");
                    if("Remove".equals(sMeetingAttendee)) {
                      String sRelId = (String) map.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                      DomainRelationship.disconnect(context ,sRelId);
                    }
               }
                ContextUtil.commitTransaction(context);
            } catch (Exception e) {
                ContextUtil.abortTransaction(context);
                throw e;
            }
        }
    }
    /**
     * Method to delete selected Meetings.
     *
     * @param context the eMatrix Context object
     * @param String array contains Meetings Ids to delete
     * @return void
     * @throws Exception if the operation fails
     * @since V6 R207 Author : Louis M
     * @grade 0
     */
    public HashMap deleteMeeting(Context context, String[] args)throws Exception{
        HashMap errorMap = new HashMap();
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId = (String)programMap.get("objectId");
        String[] meetingIds = (String[])programMap.get("MeetingIds");

        if (meetingIds == null)
            return errorMap;
        try {
            checksToDeleteMeetings(context, strObjectId, meetingIds);
        } catch (Exception e) {
            errorMap.put("Message", e.getMessage());
            return errorMap;
        }

        //read the meeting list passed in to be deleted
        Document document        = (Document) DomainObject.newInstance(context,DomainConstants.TYPE_DOCUMENT);
        StringList selStmts  = new StringList(DomainConstants.SELECT_ID);
        String doc_Vault_Rel = "to["+DomainConstants.RELATIONSHIP_VAULTED_OBJECTS+"].from.id";
        StringList docSelList = new StringList(doc_Vault_Rel);
        try {
            ContextUtil.startTransaction(context, true);
            for (int i = 0; i < meetingIds.length; i++) {
                Meeting meetingObj = (Meeting) DomainObject.newInstance(context,meetingIds[i]);
                MapList docMapList = meetingObj.getRelatedObjects(context,
                        DomainConstants.RELATIONSHIP_MEETING_ATTACHMENTS,
                        DomainConstants.TYPE_DOCUMENT,
                        selStmts,
                        new StringList(),
                        false,
                        true,
                        (short)1,
                        "",
                        "");
                if(docMapList != null) {
                    ListIterator listItr = docMapList.listIterator();
                    while(listItr.hasNext()) {
                        Map map = (Map)listItr.next();
                        map.remove("level");

                        String documentID = (String)map.get(DomainConstants.SELECT_ID);
                        if (documentID != null) {
                            document.setId(documentID);
                            Map wsMap = document.getInfo(context, docSelList);
                            StringList wsList = (StringList)wsMap.get(doc_Vault_Rel);
                            if (wsList != null) {
                                meetingObj.disconnect(context,new RelationshipType(DomainConstants.RELATIONSHIP_MEETING_ATTACHMENTS),true,(BusinessObject)document);
                            } else {
                                String[] sArray = new String[1];
                                sArray[0] = documentID;
                                Document.deleteDocuments(context, sArray);
                            }
                        }
                    }
                }
                try {
                    ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), null, context.getVault().getName());
                    meetingObj.remove( context );
                } catch (Exception e) {
                    throw e;
                } finally {
                    ContextUtil.popContext(context);
                }
            }
            // Commit the transaction
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            errorMap.put("Message", e.getMessage());
            ContextUtil.abortTransaction(context);
        }
        return errorMap;
    }

    private void checksToDeleteMeetings(Context context, String strObjectId, String[] meetingIds) throws FrameworkException, Exception {
        if (meetingIds == null)
            return;

        Person loginUser = Person.getPerson(context);
        String loginUserName = loginUser.getInfo(context, DomainConstants.SELECT_NAME);

        StringList nonHostMeetings = new StringList();
        StringList startedStateMeetings = new StringList();

        for (int i = 0; i < meetingIds.length; i++) {
            StringTokenizer stk = new StringTokenizer(meetingIds[i],"|");
            if(stk!=null)
                meetingIds[i] = stk.nextToken();

            DomainObject meetingObj = DomainObject.newInstance(context, meetingIds[i]);
            StringList objsel = new StringList();
            objsel.add(DomainObject.SELECT_OWNER);
            objsel.add(DomainObject.SELECT_CURRENT);
            objsel.add(DomainObject.SELECT_NAME);
            Map meetingObjSelValues = meetingObj.getInfo(context , objsel);

            if(!loginUserName.equals(meetingObjSelValues.get(DomainObject.SELECT_OWNER))) {
                nonHostMeetings.add((String)meetingObjSelValues.get(DomainObject.SELECT_NAME));
            }
            String strMeetingState = (String) meetingObjSelValues.get(DomainConstants.SELECT_CURRENT);
            if(!("Create".equals(strMeetingState) || "Complete".equals(strMeetingState))) {
                startedStateMeetings.add((String)meetingObjSelValues.get(DomainObject.SELECT_NAME));
            }
        }

        if(nonHostMeetings.size() != 0 || startedStateMeetings.size() != 0) {
            String locale = context.getLocale().getLanguage();
            StringBuffer err = new StringBuffer(1024);
            if(nonHostMeetings.size() != 0) {
                err.append("\\n").append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", context.getLocale(),"emxComponents.Meeting.DeleteMeetingByNonHostPerson"));
                err.append("\\n").append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", context.getLocale(),"emxComponents.Meeting.DeleteMeetingErrorHeader"));
                err.append("\\n\\t").append(nonHostMeetings.toString());
            }
            if(startedStateMeetings.size() != 0) {
                err.append("\\n").append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", context.getLocale(),"emxComponents.Meeting.DeleteMeetingWarning"));
                err.append("\\n").append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", context.getLocale(),"emxComponents.Meeting.DeleteMeetingErrorHeader"));
                err.append("\\n\\t").append(startedStateMeetings.toString());
            }
            throw new FrameworkException(err.toString());
        }
    }
    //
    /**
     * Method to get range values for an attribute.
     *
     * @param context the eMatrix Context object
     * @param String array contains Meetings Ids to delete
     * @return TreeMap
     * @throws Exception if the operation fails
     * @since V6 R207 Author : Louis M
     * @grade 0
     */
    public TreeMap getRangeValuesForAttribute(Context context, String[] args,
            String strAttributeName) throws Exception {
        TreeMap rangeMap = new TreeMap();
        matrix.db.AttributeType attribName = new matrix.db.AttributeType(
                strAttributeName);
        attribName.open(context);
        // actual range values

         List attributeRange = attribName.getChoices();
        // display range values
        List attributeDisplayRange = i18nNow.getAttrRangeI18NStringList(
                strAttributeName, (StringList) attributeRange, context
                        .getSession().getLanguage());
        for (int i = 0; i < attributeRange.size(); i++) {
            rangeMap.put((String) attributeDisplayRange.get(i),(String) attributeRange.get(i));
        }
        return rangeMap;
    }// end of the method

     /**
     * Method to retrieve Range values for attribute "Meeting Type".
     *
     * @param context the eMatrix Context object
     * @param String array contains Meetings Ids to delete
     * @return TreeMap
     * @throws Exception if the operation fails
     * @since V6 R207 Author : Louis M
     * @grade 0
     */
    public TreeMap getRangeValuesForMeetingType(Context context,
            String[] args) throws Exception {
            return getRangeValuesForAttribute(context, args,ATTRIBUTE_MEETING_TYPE);
    }
    /**
     * Method to retrieve Range values for Language Meeting Details Page.
     *
     * @param context the eMatrix Context object
     * @param String array contains Meetings Ids to delete
     * @return String
     * @throws Exception if the operation fails
     * @since V6 R207 Author : Louis M
     * @grade 0
     */
    public String getRangeValuesForLanguage(Context context, String[] args)throws Exception {
        //String strRangesLanguage = "";
        StringBuffer strRangesLanguageSB = new StringBuffer("");
        String strBusMeetSiteName = "";
        com.matrixone.apps.common.Person personObject = com.matrixone.apps.common.Person.getPerson(context);
        BusinessObject busContextOrganization   = null;
        String businessUnit = personObject.getInfo(context, com.matrixone.apps.common.Person.SELECT_BUSINESS_UNIT_ID);
        if(businessUnit == null || "null".equals(businessUnit) || "".equals(businessUnit)){
            busContextOrganization = personObject.getCompany(context);
            //strBusMeetSiteName = getAttribute(context,busContextOrganization,ATTRIBUTE_MEETING_SITE_NAME);

           }else{
               busContextOrganization = new BusinessObject(businessUnit);
               //strBusMeetSiteName = getAttribute(context,busContextOrganization,ATTRIBUTE_MEETING_SITE_NAME);
               String strBusMeetSiteId = "";
               //String strBusMeetSiteId   = getAttribute(context,busContextOrganization,ATTRIBUTE_MEETING_SITE_ID);
               if(strBusMeetSiteName == null || "".equals(strBusMeetSiteName) || "null".equals(strBusMeetSiteName) || strBusMeetSiteId == null || "".equals(strBusMeetSiteId) || "null".equals(strBusMeetSiteId)){
                   busContextOrganization = personObject.getCompany(context);

                 }
           }
        //String strWebExLanguageURL = getAppProperty(application,"emxTeamCentral.webExLanguageURL");
        String strWebExLanguageURL = EnoviaResourceBundle.getProperty(context,"emxCommon.WebExLanguageURL");
        //String strWebExLanguageURL = "emxTeamCentral.Language.English,http://mcapi-latest.webex.com;emxTeamCentral.Language.Japanese,http://mcapi-latest.webex.com;emxTeamCentral.Language.French,http://mcapi-latest.webex.com;emxTeamCentral.Language.Italy,http://mcapi-latest_it.webex.com;emxTeamCentral.Language.Korean,http://mcapi_ko.webex.com;";
        if (strWebExLanguageURL!= null && !"".equals(strWebExLanguageURL) && strWebExLanguageURL.indexOf(",") > 0 ) {
            StringTokenizer sTokGeneric = new StringTokenizer(strWebExLanguageURL, ";");
            //strRangesLanguage += "<select name = meetingKey >";
            strRangesLanguageSB.append("<select name = meetingKey >");
            while(sTokGeneric.hasMoreElements()) {
              String strToken = (String)sTokGeneric.nextToken();
              String strName = strToken.substring(0,strToken.indexOf(","));
              String strValue = strToken.substring(strToken.indexOf(",")+1, strToken.length());
              if(!"".equals(strBusMeetSiteName) && strBusMeetSiteName!=null){
                  strRangesLanguageSB.append("<option value = ");
                  strRangesLanguageSB.append(strBusMeetSiteName);
                  strRangesLanguageSB.append(">");
                  strRangesLanguageSB.append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),strName));
                  strRangesLanguageSB.append("</option>");
              }else{
                  strRangesLanguageSB.append("<option value = ");
                  strRangesLanguageSB.append(strValue);
                  strRangesLanguageSB.append(">");
                  strRangesLanguageSB.append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),strName));
                  strRangesLanguageSB.append("</option>");


              }

            }
            //strRangesLanguage += "</select>";
            strRangesLanguageSB.append("</select>");
          }

        return strRangesLanguageSB.toString();

    }
     /**
     * Method to check whether the logged user is authenticated user or not.
     *
     * @param context the eMatrix Context object
     * @param String array contains Meetings Ids to delete
     * @return void
     * @throws Exception if the operation fails
     * @since V6 R207 Author : Louis M
     * @grade 0
     */
    public void userIsAuthenticated(Context context, String objectId , String sLanguage) throws Exception{
      //check if user has access on the object
      String hasReadAccess = null;
      try{
        DomainObject BaseObject = DomainObject.newInstance(context , objectId);
        BaseObject.setId(objectId);
        hasReadAccess = BaseObject.getInfo(context, "current.access[read,checkout]");
      }catch(Exception e)
      {
        throw new MatrixException(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(sLanguage),"emxComponents.Common.PageAccessDenied"));
      }
      if(!hasReadAccess.equals("TRUE")) {
        throw new MatrixException(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(sLanguage),"emxComponents.Common.PageAccessDenied"));
      }
    }

    //
  static public String getAttribute(matrix.db.Context context,BusinessObject busObj,String attrName) throws MatrixException {
    if (busObj == null){
        return "";
    }

        //matrix.db.Context context = getPageContext();
        StringList selectStmts = new StringList();
        selectStmts.addElement("attribute[" + attrName + "]");

        BusinessObjectWithSelect _objectSelect = null;
    try {
    _objectSelect = busObj.select(context, selectStmts);
    }catch (MatrixException e){
        busObj.open(context);
        _objectSelect = busObj.select(context, selectStmts);
        busObj.close(context);
    }
    finally {
            return _objectSelect.getSelectData("attribute[" + attrName + "]");
    }
  }
  // method isHostCompanyRepresentative
  //emxTeamUtil.inc
  public static boolean isHostCompanyRepresentative (Context context, BusinessObject person, BusinessObject myOrganization ) throws MatrixException
  {
    //matrix.db.Context context = getPageContext();
    boolean hostCompanyRep = false;
    boolean companyRep = isCompanyRepresentative(context, person);

    // get the Name of the host company
    String hostCompanyGroupStr = PropertyUtil.getSchemaProperty(context,"role_CompanyName");


    if (!myOrganization.isOpen()){
      myOrganization.open(context);
    }
    if( myOrganization.getName().equals( hostCompanyGroupStr )) {
      hostCompanyRep = true;
    }

    return hostCompanyRep;
  }
//method isCompanyRepresentative
  //emxTeamUtil.inc
   public static boolean isCompanyRepresentative (Context context, BusinessObject person ) throws MatrixException
  {
    //matrix.db.Context context = getPageContext();
    boolean companyRep = false;

    String companyRepresentativeRelStr = PropertyUtil.getSchemaProperty(context,"relationship_CompanyRepresentative");

    String relName = null;

    RelationshipItr companyRepRelItr = new RelationshipItr(person.getToRelationship(context));
    Relationship companyRepRel = null;
    while ( companyRepRelItr.next()){
      companyRepRel = companyRepRelItr.obj();
      relName = companyRepRel.getTypeName();
      if ( relName.equals( companyRepresentativeRelStr )){
        companyRep = true;
        break;
      }
    }
    return companyRep;
  }
   /**
    * Program HTML Output to display the Sequence Number in Combobox.
    *
    * @param context the eMatrix Context object
    * @param String array contains Meetings Ids to delete
    * @return Vector
    * @throws Exception if the operation fails
    * @since V6 R207 Author : Louis M
    * @grade 0
    */
   public Vector displaySequenceNumber(Context context, String[] args)throws Exception{
       Map programMap                  =   (Map)JPO.unpackArgs(args);
       HashMap  requestMap             = (HashMap) programMap.get("requestMap");
       MapList objectList              =   (MapList)programMap.get("objectList");
       int count = 1;
       String strSequenceMax = EnoviaResourceBundle.getProperty(context,"emxComponents.SequenceMaxLimit");
       Vector SequenceVector = new Vector();
       for(Iterator itr=objectList.iterator();itr.hasNext();){
           StringBuffer strMeetingSequence = new StringBuffer("");
           Map objectMap = (Map) itr.next();
           strMeetingSequence.append( "<div align=\"left\">");
           strMeetingSequence.append( "<select name=\"Sequence\">");
           for(int i = 1 ; i < Integer.parseInt(strSequenceMax); i++){
               //strMeetingSequence.append("<option value=\""+i+"\">"+i+"</option>");
               strMeetingSequence.append("<option value=\"");
               strMeetingSequence.append(i);
               strMeetingSequence.append("\">");
               strMeetingSequence.append(i);
               strMeetingSequence.append("</option>");

           }
           strMeetingSequence.append("</select></div>");
           SequenceVector.add(strMeetingSequence.toString());

       }
       return SequenceVector;
   }
   /**
    * Method to update Sequence Number in Agenda Item Summary Page.
    *
    * @param context the eMatrix Context object
    * @param String array contains Meetings Ids to delete
    * @return Void
    * @throws Exception if the operation fails
    * @since V6 R207 Author : Louis M
    * @grade 0
    */
   public void updateAgendaItemSequence(Context context, String[] args)throws Exception {
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       HashMap paramMap = (HashMap)programMap.get("paramMap");
       HashMap requestMap = (HashMap)programMap.get("requestMap");

       String meetingId = (String)requestMap.get("objectId");
       String strAgendaItemRelId = (String)paramMap.get("relId");
       String strNewSeqNumber = (String)paramMap.get("New Value");

       Meeting meeting = new Meeting(meetingId);
       StringList existingAgendaItemRelIdList = meeting.getAllAgendaItemRelationshipIDs(context, strAgendaItemRelId);
       AttributeList list = new AttributeList(1);
       list.addElement(new Attribute(new AttributeType(ATTRIBUTE_SEQUENCE), strNewSeqNumber));
       for (Iterator iter = existingAgendaItemRelIdList.iterator(); iter.hasNext();) {
           DomainRelationship domRelAgendaItem = DomainRelationship.newInstance(context, (String) iter.next());
           domRelAgendaItem.setAttributes(context, list);
       }
   }
   //
   /**
    * Method to find persons for Agenda Item Responsibility.
    *
    * @param context the eMatrix Context object
    * @param String array contains Meetings Ids to delete
    * @return MapList
    * @throws Exception if the operation fails
    * @since V6 R207 Author : Louis M
    * @grade 0
    */
   public static MapList getPersons(Context context, String[] args)throws Exception {
       MapList personList = new MapList();
       StringList typeSelects = new StringList();
       typeSelects.add(DomainConstants.SELECT_NAME);
       StringList relSelects = new StringList();
       com.matrixone.apps.common.Person busPerson=com.matrixone.apps.common.Person.getPerson(context);
       busPerson.open(context);
       String strCompanyId = busPerson.getCompanyId(context);
       DomainObject companyDom = DomainObject.newInstance(context,strCompanyId);
       //find objects
       //personList =
       return personList;

   }

   /**
    * Program HTML Output for the Column Sequence in Agenda Summary Table.
    *
    * @param context the eMatrix Context object
    * @param String array contains Program Map, Sequence
    * @return StringList
    * @throws Exception if the operation fails
    * @since V6 R207 Author : Louis M
    * @grade 0
    */
   public StringList displaySequeanceRanges(Context context,String[] args)throws Exception{
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       MapList objectList = (MapList)programMap.get("objectList");
       Iterator agendaSequenceListItr = objectList.iterator();
       Map map = null;
       StringList SequenceRangeList = new StringList();
       while(agendaSequenceListItr.hasNext())
       {
           map              = (Map)agendaSequenceListItr.next();
           String sSequence  = (String) map.get(ATTRIBUTE_SEQUENCE);
           SequenceRangeList.add(sSequence);

       }
       return SequenceRangeList;
   }

   /**
    * This will update the meeting start date time with the given date and time.
    * This will consider user locale and time zone preference and convert it to system time zone.
    * Updates the 'Formatted Input Date Time' this method gets formatted input date time using using
    * eMatrixDateFormat.getFormattedInputDateTime() method.
    * @param context
    * @param meetingObj
    * @param meetingDate
    * @param meetingTime
    * @param clientTZOffset
    * @throws Exception
    */
   private void updateMeetingStartDateTime(Context context, Meeting meetingObj, String meetingDate, String meetingTime, double clientTZOffset , Locale locale) throws Exception {
	   
       String strDateTime = eMatrixDateFormat.getFormattedInputDateTime(context, meetingDate, meetingTime, clientTZOffset, locale);
       if(strDateTime!=null && strDateTime.length()!=0 && !"".equals(strDateTime) )
       {
           meetingObj.setAttributeValue(context, DomainObject.ATTRIBUTE_MEETING_START_DATETIME, strDateTime);
       }
   }

// Start modification for HF-027208V6R2010_ : KYP
   /**
    * This will update the meeting start date time with the given date and time.
    * This will consider user locale and time zone preference and convert it to system time zone.
    * Updates the 'Formatted Input Date Time' this method gets formatted input date time using using
    * eMatrixDateFormat.getFormattedInputDateTime() method.
    * @param context
    * @param meetingObj
    * @param meetingDate
    * @param meetingTime
    * @param clientTZOffset
    * @throws Exception
    */
   private void updateMeetingStartDateTime(Context context, Meeting meetingObj, long meetingDate, String meetingTime, double clientTZOffset) throws Exception {

     // From msec get the formatted display date, we don't have to use any other locale now!
       String strMeetingDate = eMatrixDateFormat.getDateValue(context, String.valueOf(meetingDate), String.valueOf(clientTZOffset),  Locale.US);
       // From display date get the formatted input date, also now we don't have to use any other locale now!
     String strDateTime = eMatrixDateFormat.getFormattedInputDateTime(context, strMeetingDate, meetingTime, clientTZOffset, Locale.US);
       if(strDateTime!=null && strDateTime.length()!=0 && !"".equals(strDateTime) )
       {
           meetingObj.setAttributeValue(context, DomainObject.ATTRIBUTE_MEETING_START_DATETIME, strDateTime);
       }
   }

// End modification for HF-027208V6R2010_ : KYP

   /**
    * Update program to update meeting start date and time from Meeting edit dialog
    * @param context
    * @param args
    * @throws Exception
    */
   public void updateMeetingStartTime(Context context,String[] args)throws Exception{
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       HashMap requestMap = (HashMap)programMap.get("requestMap");
       HashMap paramMap = (HashMap)programMap.get("paramMap");
       String strMeetingId = (String)paramMap.get("objectId");
       String strNewStartTime = (String)paramMap.get("New Value");
       
       String[] startTime = (String[]) requestMap.get("StartTime");

       Object mode = requestMap.get("mode");
       boolean isCreateMode = false;
       if (mode instanceof String) {
           isCreateMode = "create".equals(mode);
       } else if (mode instanceof String[]) {
           isCreateMode = "create".equals(((String[]) mode)[0]);
       }
       /**
        * In Crate Mode updating the meeting start time is taken crate in emxMeetingProcess.jsp
        */
       if(isCreateMode)
           return;

       strNewStartTime  = strNewStartTime.substring(strNewStartTime.indexOf("~") + 1, strNewStartTime.length());
       double clientTZOffset  = Double.parseDouble((String)requestMap.get("timeZone"));

    // Start modification for HF-027208V6R2010_ : KYP
       String[] MeetingDateMsec = (String[])requestMap.get("MeetingDate_msvalue");
       long nMeetingDataMsec = Long.parseLong(MeetingDateMsec[0]);
       if(startTime!=null)
       updateMeetingStartDateTime(context, new Meeting(strMeetingId), nMeetingDataMsec, startTime[0], clientTZOffset);
       else
    	   updateMeetingStartDateTime(context, new Meeting(strMeetingId), nMeetingDataMsec, strNewStartTime, clientTZOffset);
     
    // End modification for HF-027208V6R2010_ : KYP
   }

   /**
    * Update program to update the meeting date from Meeting Summary Structure Browser page
    * @param context
    * @param args
    * @throws Exception
    */
   public void updateMeetingDate(Context context,String[] args)throws Exception{
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       HashMap requestMap = (HashMap)programMap.get("requestMap");
       HashMap paramMap = (HashMap)programMap.get("paramMap");
       Locale locale = (Locale)requestMap.get("locale"); 
       String strMeetingId = (String)paramMap.get("objectId");
       String strNewDate = (String)paramMap.get("New Value");
       double clientTZOffset  = Double.parseDouble((String)requestMap.get("timeZone"));
       Meeting meetingObj = new Meeting(strMeetingId);
       String meetingTime = getFormattedMeetingStartTime(context, meetingObj, clientTZOffset, false);
      
       updateMeetingStartDateTime(context, meetingObj, strNewDate, meetingTime, clientTZOffset, locale);
   }

   /**
    * Update program to update the meeting time from Meeting Summary Structure Browser page
    * @param context
    * @param args
    * @throws Exception
    */
   public void updateMeetingTime(Context context, String[] args) throws Exception {
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       HashMap requestMap = (HashMap)programMap.get("requestMap");
       HashMap paramMap = (HashMap)programMap.get("paramMap");
       Locale locale = (Locale)requestMap.get("locale"); 
       String strMeetingId = (String)paramMap.get("objectId");
       String strNewTime = (String)paramMap.get("New Value");
       double clientTZOffset  = Double.parseDouble((String)requestMap.get("timeZone"));
       Meeting meetingObj = new Meeting(strMeetingId);
       String meetingDate = getFormattedMeetingStartDate(context, meetingObj, clientTZOffset);
       updateMeetingStartDateTime(context, meetingObj, meetingDate, strNewTime, clientTZOffset, locale.US);
   }

   /**
    * Method to find the Meeting Attendees.
    *
    * @param context the eMatrix Context object
    * @param String array contains Meetings Ids to delete
    * @return MapList.
    * @throws Exception if the operation fails
    * @since V6 R207 Author : Louis M
    * @grade 0
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getMeetingttendee(Context context, String[] args)throws Exception
    {
        HashMap requestMap          = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap2          = (HashMap)requestMap.get("requestMap");
        MapList AgendaItemList = new MapList();
        String strMeetingId = (String)requestMap.get("objectId");
        //matrix.util.Pattern typePattern = new matrix.util.Pattern("Agenda Item");
        //matrix.util.Pattern relPattern  = new matrix.util.Pattern("Agenda Item");
        DomainObject MeetingObj = DomainObject.newInstance(context);
        MeetingObj.setId(strMeetingId);
        StringList selectTypeStmts = new StringList();

        selectTypeStmts.add(MeetingObj.SELECT_ID);
        selectTypeStmts.add(MeetingObj.SELECT_TYPE);
        selectTypeStmts.add(MeetingObj.SELECT_NAME);
        selectTypeStmts.add(MeetingObj.SELECT_DESCRIPTION);
        selectTypeStmts.add(MeetingObj.SELECT_OWNER);
        selectTypeStmts.add(MeetingObj.SELECT_CURRENT);
        AgendaItemList = MeetingObj.getRelatedObjects(context,
                                       DomainConstants.RELATIONSHIP_ASSIGNED_MEETINGS,  //String relPattern
                                       DomainConstants.TYPE_PERSON, //String typePattern
                                       selectTypeStmts,          //StringList objectSelects,
                                       null,                     //StringList relationshipSelects,
                                       true,                     //boolean getTo,
                                       true,                     //boolean getFrom,
                                       (short)1,                 //short recurseToLevel,
                                       "",          //String objectWhere,
                                       "",                       //String relationshipWhere,
                                       null,                     //Pattern includeType,
                                       null,                     //Pattern includeRelationship,
                                       null);

        return AgendaItemList;
    }
   /**
    * Method to Delete Agenda Items.
    *
    * @param context the eMatrix Context object
    * @param String array contains Meetings Ids to delete
    * @return MapList.
    * @throws Exception if the operation fails
    * @since V6 R207 Author : Louis M
    * @grade 0
    */
   public void deleteAgendaItem(Context context, String[] args)throws Exception{
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       String strObjectId = (String)programMap.get("objectId");
       String[] agendaItemArray = (String[])programMap.get("AgendaItemIds");
       MapList agendaItemList = new MapList();

       DomainObject meetingDom = DomainObject.newInstance(context,strObjectId);

       String strSelectedId = "";
       StringList strlRelIds = new StringList();
       String strAgendaRelId = "";
       try
       {
       for (int j=0; j< agendaItemArray.length; j ++)
        {
       String relId = (String)FrameworkUtil.split(agendaItemArray[j],"|").get(0);
       StringList selStmts = new StringList();
       selStmts.add(SELECT_ID);
       DomainRelationship agendaDom = DomainRelationship.newInstance(context,relId);
       String attTopic = PropertyUtil.getSchemaProperty(context,"attribute_Topic");
       String attSeqNo =   PropertyUtil.getSchemaProperty(context,"attribute_Sequence");
       String topicName = (String) agendaDom.getAttributeValue(context, attTopic);
       String seqNum = (String) agendaDom.getAttributeValue(context, attSeqNo);
       StringBuffer whereSB = new StringBuffer();
       whereSB.append("(attribute[" + "Topic" + "] == \""+ topicName +"\")");
       whereSB.append(" && ");
       whereSB.append("(attribute[" + "Sequence" + "] == \""+ seqNum +"\")");
       
       MapList objList = meetingDom.getRelatedObjects(context,
               RELATIONSHIP_AGENDA_ITEM,//relPattern.getPattern(),
               SYMB_WILD,//typePattern.getPattern(),
               null,selStmts,
               false,
               true,
               (short)1,
               "",
               whereSB.toString());
       StringList strAgendaItemList = new StringList();
       Iterator agendaObjectListItr = objList.iterator();

       while(agendaObjectListItr.hasNext())
       {
          Map map = (Map) agendaObjectListItr.next();
          strAgendaItemList.add((String) map.get(SELECT_ID));
       }

           for(int i=0;i<strAgendaItemList.size();i++)
           {
               DomainRelationship.disconnect(context,(String)strAgendaItemList.get(i));
           }
         }
       }
       catch(Exception ex)
       {

       }
   }

  public static MapList findTypesForAgendaItems(
           Context context,
           String[] args)
           throws Exception {
               MapList mapList = null;
               try {

                       Map programMap = (Map) JPO.unpackArgs(args);
                       short sQueryLimit =
                               (short) (java
                                       .lang
                                       .Integer
                                       .parseInt((String) programMap.get("queryLimit")));

                       String strType = (String) programMap.get("hdnType");

                       if (strType == null
                               || strType.equals("")
                               || "null".equalsIgnoreCase(strType)) {
                               strType = SYMB_WILD;
                       }

                       String strName = (String) programMap.get("txtName");

                       if (strName == null
                               || strName.equals("")
                               || "null".equalsIgnoreCase(strName)) {
                               strName = SYMB_WILD;
                       }

                       String strRevision = (String) programMap.get("txtRevision");

                       if (strRevision == null
                               || strRevision.equals("")
                               || "null".equalsIgnoreCase(strRevision)) {
                               strRevision = SYMB_WILD;
                       } else {
                               strRevision = strRevision.trim();
                       }

                       String strDesc = (String) programMap.get("txtDescription");

                       String strOwner = (String) programMap.get("txtOwner");

                       if (strOwner == null
                               || strOwner.equals("")
                               || "null".equalsIgnoreCase(strOwner)) {
                               strOwner = SYMB_WILD;
                       } else {
                               strOwner = strOwner.trim();
                       }

                       String strState = (String) programMap.get("txtState");

                       if (strState == null
                               || strState.equals("")
                               || "null".equalsIgnoreCase(strState)) {
                               strState = SYMB_WILD;
                       } else {
                               strState = strState.trim();
                       }

                       String strVault = "";
                       String strVaultOption = (String) programMap.get(VAULT_OPTION);

                         if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                                                                       strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
                                                else
                                                                       strVault = (String)programMap.get("vaults");

                       StringList slSelect = new StringList(1);
                       slSelect.addElement(DomainConstants.SELECT_ID);

                       boolean bStart = true;
                       StringBuffer sbWhereExp = new StringBuffer(150);

                       if (strDesc != null
                               && (!strDesc.equals(SYMB_WILD))
                               && (!strDesc.equals(""))
                               && !("null".equalsIgnoreCase(strDesc))) {
                               if (bStart) {
                                       sbWhereExp.append(SYMB_OPEN_PARAN);
                                       bStart = false;
                               }
                               sbWhereExp.append(SYMB_OPEN_PARAN);
                               sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
                               sbWhereExp.append(SYMB_MATCH);
                               sbWhereExp.append(SYMB_QUOTE);
                               sbWhereExp.append(strDesc);
                               sbWhereExp.append(SYMB_QUOTE);
                               sbWhereExp.append(SYMB_CLOSE_PARAN);
                       }

                       if (strState != null
                               && (!strState.equals(SYMB_WILD))
                               && (!strState.equals(""))
                               && !("null".equalsIgnoreCase(strState))) {
                               if (bStart) {
                                       sbWhereExp.append(SYMB_OPEN_PARAN);
                                       bStart = false;
                               } else {
                                       sbWhereExp.append(SYMB_AND);
                               }
                               sbWhereExp.append(SYMB_OPEN_PARAN);
                               sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                               sbWhereExp.append(SYMB_MATCH);
                               sbWhereExp.append(SYMB_QUOTE);
                               sbWhereExp.append(strState);
                               sbWhereExp.append(SYMB_QUOTE);
                               sbWhereExp.append(SYMB_CLOSE_PARAN);
                       }
                       String strFilteredExpression = getFilteredExpression(context,programMap);

                       if ((strFilteredExpression != null)
                               && !("null".equalsIgnoreCase(strFilteredExpression))
                               && !strFilteredExpression.equals("")) {
                               if (bStart) {
                                       sbWhereExp.append(SYMB_OPEN_PARAN);
                                       bStart = false;
                               } else {
                                       sbWhereExp.append(SYMB_AND);
                               }
                               sbWhereExp.append(strFilteredExpression);
                       }
                       if (!bStart) {
                               sbWhereExp.append(SYMB_CLOSE_PARAN);
                       }

                       mapList =
                               DomainObject.findObjects(
                                       context,
                                       strType,
                                       strName,
                                       strRevision,
                                       strOwner,
                                       strVault,
                                       sbWhereExp.toString(),
                                       "",
                                       true,
                                       (StringList) slSelect,
                                       sQueryLimit);
               } catch (Exception excp) {
                       throw excp;
               }

               return mapList;
       }
   /**
    * The function to filter the object selection and apppend the default query in the where clause.
    *
            * @param context - the eMatrix <code>Context</code> object
    * @param programMap - with the following entries
    *              object Id - Object Id of the context object
    * @return - String after constructing the Where clause appropriately
    * @throws Exception when problems occurred in the AEF
    * @since AEF 10.0.5.0
    */
   protected static String getFilteredExpression(Context context,Map programMap)
           throws Exception {

           String strMode = (String) programMap.get(Search.REQ_PARAM_MODE);
           String strObjectId =
                   (String) programMap.get(Search.REQ_PARAM_OBJECT_ID);
           String strSrcDestRelNameSymb =
                   (String) programMap.get(Search.REQ_PARAM_SRC_DEST_REL_NAME);
           String strIsTo = (String) programMap.get(Search.REQ_PARAM_IS_TO);
           String strDQ = (String) programMap.get(Search.REQ_PARAM_DEFAULT_QUERY);
           String strMidDestRelNameSymb =
                   (String) programMap.get(Search.REQ_PARAM_MID_DEST_REL_NAME);
           String strSrcMidRelNameSymb =
                   (String) programMap.get(Search.REQ_PARAM_SRC_MID_REL_NAME);

           String strMidDestRelName = "";
           if (strMidDestRelNameSymb != null
                   && !strMidDestRelNameSymb.equals("")
                   && !("null".equalsIgnoreCase(strMidDestRelNameSymb))) {
                   strMidDestRelName =
                           PropertyUtil.getSchemaProperty(context,strMidDestRelNameSymb);
           }

           String strSrcMidRelName = "";
           if (strSrcMidRelNameSymb != null
                   && !strSrcMidRelNameSymb.equals("")
                   && !("null".equalsIgnoreCase(strSrcMidRelNameSymb))) {
                   strSrcMidRelName =
                           PropertyUtil.getSchemaProperty(context,strSrcMidRelNameSymb);
           }

           String strSrcDestRelName = "";
           if (strSrcDestRelNameSymb != null
                   && !strSrcDestRelNameSymb.equals("")
                   && !("null".equalsIgnoreCase(strSrcDestRelNameSymb))) {
                   strSrcDestRelName =
                           PropertyUtil.getSchemaProperty(context,strSrcDestRelNameSymb);
           }

           StringBuffer sbWhereExp = new StringBuffer(50);
           //sbWhereExp.append(SYMB_OPEN_PARAN);
           boolean bStart = true;

           String strCommand = (String) programMap.get(Search.REQ_PARAM_COMMAND);
           // If add exisitng Object of type other that Part
           if ((strCommand != null)
                   && !strCommand.equals("")
                   && !("null".equalsIgnoreCase(strCommand))) {

                   if ((strMode.equals(Search.ADD_EXISTING))
                           && (strObjectId != null)
                           && (!strObjectId.equals(""))
                           && !("null".equalsIgnoreCase(strObjectId))) {
                           bStart = false;
                           sbWhereExp.append(SYMB_OPEN_PARAN);

                           /* Case where we don't have an intermediate relationship */

                           if (strIsTo.equalsIgnoreCase(TRUE)) {
                                   //sbWhereExp.append("!('to[");
                                   sbWhereExp.append(SYMB_NOT);
                                   sbWhereExp.append(SYMB_OPEN_PARAN);
                                   sbWhereExp.append(SYMB_QUOTE);
                                   sbWhereExp.append(SYMB_TO);
                                   sbWhereExp.append(SYMB_OPEN_BRACKET);
                                   sbWhereExp.append(strSrcDestRelName);
                                   //sbWhereExp.append("].from.");
                                   sbWhereExp.append(SYMB_CLOSE_BRACKET);
                                   sbWhereExp.append(SYMB_DOT);
                                   sbWhereExp.append(SYMB_FROM);
                                   sbWhereExp.append(SYMB_DOT);
                                   sbWhereExp.append(DomainConstants.SELECT_ID);
                                   //sbWhereExp.append("'==");
                                   sbWhereExp.append(SYMB_QUOTE);
                                   sbWhereExp.append(SYMB_EQUAL);
                                   sbWhereExp.append(strObjectId);
                                   //sbWhereExp.append(")");
                                   sbWhereExp.append(SYMB_CLOSE_PARAN);

                           } else {
                                   //sbWhereExp.append("!('from[");
                                   sbWhereExp.append(SYMB_NOT);
                                   sbWhereExp.append(SYMB_OPEN_PARAN);
                                   sbWhereExp.append(SYMB_QUOTE);
                                   sbWhereExp.append(SYMB_FROM);
                                   sbWhereExp.append(SYMB_OPEN_BRACKET);
                                   sbWhereExp.append(strSrcDestRelName);
                                   //sbWhereExp.append("].to.");
                                   sbWhereExp.append(SYMB_CLOSE_BRACKET);
                                   sbWhereExp.append(SYMB_DOT);
                                   sbWhereExp.append(SYMB_TO);
                                   sbWhereExp.append(SYMB_DOT);
                                   sbWhereExp.append(DomainConstants.SELECT_ID);
                                   //sbWhereExp.append("'==");
                                   sbWhereExp.append(SYMB_QUOTE);
                                   sbWhereExp.append(SYMB_EQUAL);
                                   sbWhereExp.append(strObjectId);
                                   //sbWhereExp.append(")");
                                   sbWhereExp.append(SYMB_CLOSE_PARAN);

                           }

                           sbWhereExp.append(SYMB_CLOSE_PARAN);
                           /* To remove the duplicate object ids, from Add Existing sub types... */
                           sbWhereExp.append(SYMB_AND);
                           sbWhereExp.append(SYMB_OPEN_PARAN);
                           sbWhereExp.append(DomainConstants.SELECT_ID);
                           //sbWhereExp.append("!='");
                           sbWhereExp.append(SYMB_NOT_EQUAL);
                           sbWhereExp.append(SYMB_QUOTE);
                           sbWhereExp.append(strObjectId);
                           sbWhereExp.append(SYMB_QUOTE);
                           sbWhereExp.append(SYMB_CLOSE_PARAN);

                   }
           }

           if (strDQ != null
                   && !strDQ.equals("")
                   && !("null".equalsIgnoreCase(strDQ))) {
                   if (!bStart) {
                           sbWhereExp.append(SYMB_AND);
                           bStart = false;
                   }
                   sbWhereExp.append(SYMB_OPEN_PARAN);
                   sbWhereExp.append(strDQ);
                   sbWhereExp.append(SYMB_CLOSE_PARAN);
           }

           String strFilteredExp = "";
           String strWhereExp = sbWhereExp.toString();

           if (strWhereExp != null
                   && !strWhereExp.equals("")
                   && !("null".equalsIgnoreCase(strWhereExp))) {
                   strFilteredExp = strWhereExp;
           }

           return strFilteredExp;
   }
   //createAgendaPostProcess
   /**
    * The function do the processing of Creating Agenda item.
    * Connects the Meeting object to the associatedobject with the relationship "Agenda item"
            * @param context - the eMatrix <code>Context</code> object
    * @param String array args containing the programMap, Meeting Id
    * @author Louis M
    * @return - void
    * @throws Exception when problems occurred
    * @since AEF 10.0.5.0
    */
   @com.matrixone.apps.framework.ui.PostProcessCallable
   public void createAgendaPostProcess(Context context, String[] args)throws Exception{
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       HashMap requestMap = (HashMap)programMap.get("requestMap");
       HashMap paramMap = (HashMap)programMap.get("paramMap");

       String strMeetingId = (String)requestMap.get("objectId");
       String associatedObjectIDs = (String)requestMap.get("TopicOID");
       String strTopicDescription = (String)requestMap.get("TopicDescription");
       String strSequence = (String)paramMap.get("SequenceNumber");
       String strDuration = (String)requestMap.get("Duration");
       String strResponsibleOID = (String)requestMap.get("ResponsiblePersonOID");

       StringBuffer whereSB = new StringBuffer(50);
       whereSB.append(getAttributeSelect(ATTRIBUTE_SEQUENCE)).append("==").append(strSequence);

       StringList selStmts = new StringList();
       selStmts.add(Meeting.SELECT_ID);
       // To check if the Same Sequence number is already there
       DomainObject meetingDom = DomainObject.newInstance(context,strMeetingId);
       MapList agendaItemsWithSameSeq = meetingDom.getRelatedObjects(context,
                                           RELATIONSHIP_AGENDA_ITEM,//relPattern.getPattern(),
                                           SYMB_WILD,//typePattern.getPattern(),
                                           selStmts,
                                           new StringList(),
                                           false,
                                           true,
                                           (short)1,
                                           "",
                                           whereSB.toString());

       if(agendaItemsWithSameSeq!=null && agendaItemsWithSameSeq.size()>0){
           //Alert user to choose another sequence number
           throw new FrameworkException(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", context.getLocale(),"emxComponents.Meeting.AgendaItemSequenceNoUnique"));
       }

       AttributeList attrListAgendaItemRel = new AttributeList();
       if(!"".equals(strSequence)&& strSequence!=null && strSequence.length()!=0){
           attrListAgendaItemRel.addElement(new Attribute(new AttributeType(ATTRIBUTE_SEQUENCE), strSequence));
       }
       if(!"".equals(strDuration)&& strDuration!=null && strDuration.length()!=0){
           attrListAgendaItemRel.addElement(new Attribute(new AttributeType(ATTRIBUTE_TOPIC_DURATION), strDuration));
       }
       if(!"".equals(strTopicDescription)&& strTopicDescription!=null && strTopicDescription.length()!=0){
           attrListAgendaItemRel.addElement(new Attribute(new AttributeType(ATTRIBUTE_TOPIC), strTopicDescription));
       }

       StringList responsiblePersonIDs = FrameworkUtil.split(strResponsibleOID, ",");
       StringList associatedObjectIDsList = associatedObjectIDs == null || associatedObjectIDs.equals("") || associatedObjectIDs.equals("null") ?
                                       new StringList(strMeetingId) : FrameworkUtil.split(associatedObjectIDs, "|");

       for (int i = 0; i < associatedObjectIDsList.size(); i++) {
           String associatedObjectID = (String) associatedObjectIDsList.get(i);

           DomainObject associatedDom = DomainObject.newInstance(context,associatedObjectID);
           DomainRelationship relAgendaItem = meetingDom.connect(context, RELATIONSHIP_AGENDA_ITEM, associatedDom, false);
           //Set Attributes on the Relationship Agenda item
           relAgendaItem.setAttributes(context, attrListAgendaItemRel);

           String strAgendaRelId = relAgendaItem.getName();
           for(int count = 0; count < responsiblePersonIDs.size() ; count++){
               String strPersonId = (String) responsiblePersonIDs.get(count);

               StringBuffer mqlCommandSB = new StringBuffer(300);
               mqlCommandSB.append("add connection ").append("$1").append(" fromrel ");
               mqlCommandSB.append("$2");
               mqlCommandSB.append(" to ");
               mqlCommandSB.append("$3");
               mqlCommandSB.append(";");
               MqlUtil.mqlCommand(context, mqlCommandSB.toString(), RELATIONSHIP_AGENDA_RESPONSIBILITY, strAgendaRelId, strPersonId);
           }
       }
   }
   /**
    * Method to get the Responsible Persons for Agenda Item.
    *
    * @param context the eMatrix Context object
    * @param String array contains Meetings Ids to delete
    * @return Vector.
    * @throws Exception if the operation fails
    * @since V6 R207 Author : Louis M
    * @grade 0
    */
   public Vector getAgendaResponsiblePerson(Context context, String[] args) throws Exception {
       Vector agendaResponsiblePersons = new Vector();
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       MapList objectList = (MapList)programMap.get("objectList");
       String[] agendaResponsibleIdsArray = null;
       String sRelationshipId = null;
       Map map                = null;
       //<Fix 372291> For table Export or printer friendly (when reportFormat != null)
       //do not return HTML or href links
       Map paramList = (Map) programMap.get("paramList");
       boolean isReport =   paramList != null && !isEmpty((String)paramList.get("reportFormat"));
       //</Fix 372291>

       Iterator agendaObjectListItr = objectList.iterator();
       int ct=0;
       while(agendaObjectListItr.hasNext())
       {
           map              = (Map)agendaObjectListItr.next();
           sRelationshipId  = (String) map.get(DomainConstants.SELECT_RELATIONSHIP_ID);
           try
           {
               String strResult = MqlUtil.mqlCommand(context,"print connection $1 select $2 $3 dump $4", sRelationshipId, "frommid.to.name", "frommid.to.id", "|");
               StringList strResultList = FrameworkUtil.split(strResult,"|");
               int size = strResultList.size();
               if(size > 0)
               {
                   String strAgendaItemRspPersonsListHiddenOID = "";
                   String strAgendaItemRspPersonsList = "";
                   for(int i=0; i<size/2; i++)
                   {

                       if(strAgendaItemRspPersonsList != null && !strAgendaItemRspPersonsList.equals(""))
                           strAgendaItemRspPersonsList = strAgendaItemRspPersonsList + "," + ((String) strResultList.get(i));
                       else
                           strAgendaItemRspPersonsList = (String) strResultList.get(i);
                   }
                   for(int j=size/2; j<size; j++)
                   {

                       if(strAgendaItemRspPersonsListHiddenOID != null && !strAgendaItemRspPersonsListHiddenOID.equals(""))
                           strAgendaItemRspPersonsListHiddenOID = strAgendaItemRspPersonsListHiddenOID + "," + ((String) strResultList.get(j));
                       else
                           strAgendaItemRspPersonsListHiddenOID = (String) strResultList.get(j);
                   }
                   //<Fix 372291> For table Export or printer friendly do not return HTML or href links
                   StringBuffer respSB = new StringBuffer("");
                   respSB.append(strAgendaItemRspPersonsList);
                   if(!isReport) {
                   respSB.append(" <input type=\"hidden\" name=\"RespOID\" value=\"");
                   respSB.append(XSSUtil.encodeForHTMLAttribute(context,strAgendaItemRspPersonsListHiddenOID));
                   respSB.append("\"/>");
                   }
                   //</Fix 372291>
                   agendaResponsiblePersons.add(respSB.toString());
               }
               else
                   agendaResponsiblePersons.add("");

           }
           catch(Exception e)
           {
               e.printStackTrace();
               throw e;
           }

       }
       return agendaResponsiblePersons;

   }
   /**
    * Access function for Promote Meeting Command.
    *
    * @param context the eMatrix Context object
    * @param String array contains Meetings Ids to delete
    * @return boolean.
    * @throws Exception if the operation fails
    * @since V6 R207 Author : Louis M
    * @grade 0
    */
   public static Boolean showPromoteMeeting(Context context, String[] args)
   throws Exception{
       Boolean bPromoteMeetingAccess = Boolean.valueOf(false);
       String strCurrentState = null;
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       String strMeetingId = (String)programMap.get("objectId");
       DomainObject meetingDom = DomainObject.newInstance(context,strMeetingId);
       strCurrentState = meetingDom.getInfo(context,DomainConstants.SELECT_CURRENT);
       String strHost = meetingDom.getInfo(context,DomainConstants.SELECT_OWNER);
       String strCtxUser = context.getUser();
       String strMeetingType = (String)meetingDom.getInfo(context,"attribute["+PropertyUtil.getSchemaProperty(context,"attribute_MeetingType")+"]");
       if("None".equals(strMeetingType)){
           if(!"Complete".equals(strCurrentState)&& strHost.equalsIgnoreCase(strCtxUser)){
               bPromoteMeetingAccess = Boolean.valueOf(true);
           }

       }else{
           // Meeting type is WebEx
           if("Create".equals(strCurrentState)){
               bPromoteMeetingAccess = Boolean.valueOf(true);

           }
       }
       return bPromoteMeetingAccess;
   }

   //updateResponsiblePerson
   /**
    * Method to update Responsible Person while Editing the Agenda Item.
    *
    * @param context the eMatrix Context object
    * @param String array contains Meetings Ids to delete
    * @return void.
    * @throws Exception if the operation fails
    * @since V6 R207 Author : Louis M
    * @grade 0
    */
   public void updateResponsiblePerson(Context context, String[] args)throws Exception{
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       HashMap paramMap = (HashMap)programMap.get("paramMap");
       HashMap requestMap = (HashMap)programMap.get("requestMap");

       String meetingId = (String)requestMap.get("objectId");
       String strAgendaItemRelId = (String)paramMap.get("relId");
       String strNewPersons = (String)paramMap.get("New Value");

       Meeting meeting = new Meeting(meetingId);
       StringList existingAgendaItemRelIdList = meeting.getAllAgendaItemRelationshipIDs(context, strAgendaItemRelId);

       for (Iterator iter = existingAgendaItemRelIdList.iterator(); iter.hasNext();) {
           String agendaRelId = (String) iter.next();
           String strAgendaResponsibleRelID = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump $3", agendaRelId, "frommid.id", ",");
           StringList resPersonConnectionIds = FrameworkUtil.split(strAgendaResponsibleRelID, ",");
           for (Iterator iterator = resPersonConnectionIds.iterator(); iterator.hasNext();) {
               StringBuffer buffer = new StringBuffer(50);
               buffer.append("delete connection ").append("$1").append(';');
               MqlUtil.mqlCommand(context, buffer.toString(), (String)iterator.next());
           }
       }

       StringList personIds = new StringList();
       StringTokenizer stk = new StringTokenizer(strNewPersons,",");
       while(stk.hasMoreTokens()){
           String token = stk.nextToken();
           if("".equals(token))
               continue;
           personIds.add(PersonUtil.getPersonObjectID(context, token));
       }

       for (Iterator iter = existingAgendaItemRelIdList.iterator(); iter.hasNext();) {
           String agendaRelId = (String) iter.next();
           for (Iterator personIdItr = personIds.iterator(); personIdItr.hasNext();) {
               StringBuffer buffer = new StringBuffer(100);
               buffer.append("add connection ").
                      append("$1").
                      append(" fromrel ").
                      append("$2").
                      append(" to ").
                      append("$3");
               MqlUtil.mqlCommand(context, buffer.toString(), RELATIONSHIP_AGENDA_RESPONSIBILITY, agendaRelId, (String)personIdItr.next());
           }
       }
   }
   /**
    * Access program to display Close Meeting.
    *
    * @param context the eMatrix Context object
    * @param String array contains Meetings Ids to delete
    * @return boolean.
    * @throws Exception if the operation fails
    * @since V6 R207 Author : Louis M
    * @grade 0
    */
   public static Boolean showCloseMeetingCommand(Context context, String[] args)
   throws Exception{
       Boolean bCloseMeetingAccess = Boolean.valueOf(false);
       String strCurrentState = null;
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       String strMeetingId = (String)programMap.get("objectId");
       DomainObject meetingDom = DomainObject.newInstance(context,strMeetingId);
       strCurrentState = meetingDom.getInfo(context,DomainConstants.SELECT_CURRENT);
       String strMeetingType = (String)meetingDom.getInfo(context,"attribute["+ATTRIBUTE_MEETING_TYPE+"]");
       if(!"None".equals(strMeetingType)){
           if("In Progress".equals(strCurrentState)){
               bCloseMeetingAccess = Boolean.valueOf(true);
           }

       }
       return bCloseMeetingAccess;

   }
   /**
    * Access program to display Start Meeting.
    *
    * @param context the eMatrix Context object
    * @param String array contains Meetings Ids to delete
    * @return boolean.
    * @throws Exception if the operation fails
    * @since V6 R207 Author : Louis M
    * @grade 0
    */
   public static Boolean showStartMeetingCommand(Context context, String[] args)
   throws Exception{
       Boolean bStartMeetingAccess = Boolean.valueOf(false);
       String strCurrentState = null;
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       String strMeetingId = (String)programMap.get("objectId");
       DomainObject meetingDom = DomainObject.newInstance(context,strMeetingId);
       String strMeetingOwner = meetingDom.getInfo(context,DomainConstants.SELECT_OWNER);
       strCurrentState = meetingDom.getInfo(context,DomainConstants.SELECT_CURRENT);
       String strMeetingType = (String)meetingDom.getInfo(context,"attribute["+ATTRIBUTE_MEETING_TYPE+"]");
       if(!"None".equals(strMeetingType)){
           if("Scheduled".equals(strCurrentState)){
               //Logged in person Owner
               if(context.getUser().equals(strMeetingOwner)){
                   bStartMeetingAccess = Boolean.valueOf(true);

               }

           }

       }

       return bStartMeetingAccess;

   }
   //getRangeValuesForSequence
//Fix for the bug 366418
   public TreeMap getRangeValuesForSequence(Context context, String[] args)throws Exception {
       TreeMap rangeMap = new TreeMap();
       TreeMap sMap = new TreeMap();
       String strSequenceMax = EnoviaResourceBundle.getProperty(context,"emxComponents.SequenceMaxLimit");
       strSequenceMax = getSequenceNumOnCreate(context,args);
       for(int i = 1; i <= Integer.parseInt(strSequenceMax)+4; i++){
           rangeMap.put(String.valueOf(i),String.valueOf(i));
       }
       return rangeMap;

   }
   //getRangeValuesMeetingTypeCreateForm
   public Map getRangeValuesMeetingTypeCreateForm(Context context, String[] args)throws Exception {
	   Map returnMap = new HashMap();
       StringList rangeMeetingTypeDisplay = new StringList();
       StringList rangeMeetingTypeActual = new StringList();
       String strAttributeName = ATTRIBUTE_MEETING_TYPE;
       matrix.db.AttributeType attribName = new matrix.db.AttributeType(
               strAttributeName);
       attribName.open(context);
       // actual range values
        List attributeRange = attribName.getChoices();
        // display range values
       List attributeDisplayRange = i18nNow.getAttrRangeI18NStringList(
               strAttributeName, (StringList) attributeRange, context
                       .getSession().getLanguage());
       for (int i = 0; i < attributeRange.size(); i++) {
           if("3D Visual Meeting".equals(attributeRange.get(i))){
               if(isViewerIntegrationInstalled()){
            	   rangeMeetingTypeDisplay.add(attributeDisplayRange.get(i));
            	   rangeMeetingTypeActual.add(attributeRange.get(i));
               }
           }
           else{
        	   rangeMeetingTypeDisplay.add(attributeDisplayRange.get(i));
        	   rangeMeetingTypeActual.add(attributeRange.get(i));
           }
           
           
           
       }
       
       returnMap.put("field_choices", rangeMeetingTypeActual);
       returnMap.put("field_display_choices", rangeMeetingTypeDisplay);
       return returnMap;

   }
   // Method for cimmetry ViewerIntegration code
   static public boolean isViewerIntegrationInstalled()
    {
        try
        {
          //Check if com/cimmetry/servlet/VueServlet.class is installed using reflection
          String strClass = "com.cimmetry.servlet.VueServlet";
          Class cl = Class.forName(strClass);

          return true;

        }
        catch (Exception e)
        {
            return false;
        }
  }

   /**
    * Access program to display Add Workspace Content.
    *
    * @param context the eMatrix Context object
    * @param String array contains program map
    * @return boolean.
    * @throws Exception if the operation fails
    * @since R207
    * @grade 0
    */

   public boolean checkWorkspaceMeeting (Context context, String args[]) throws Exception
   {
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       String relationship = PropertyUtil.getSchemaProperty(context,"relationship_MeetingContext");

       String meetingId = (String) programMap.get("objectId");
       DomainObject dObj = new DomainObject(meetingId);
       // Test whether the to side connected of Meeting object is Project Memeber or not. If project Member then display 'Add Workspace Content' command
       // in toolbar.
       MapList workspaceList = dObj.getRelatedObjects(context,
                        relationship,
                        "*",
                        new StringList("id"),
                        null,
                        true,
                        false,
                        (short)1,
                        null,
                        null);
      if(workspaceList.size() > 0)
        return true;
      return false;

   }
   // Update function for Meeting Description
   // Fix for bug 366284
   public void updateMeetingDescription(Context context, String[] args)throws Exception {
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    HashMap paramMap = (HashMap)programMap.get("paramMap");
    MapList objectList = (MapList) programMap.get("objectList");
      String strNewDescription = (String)paramMap.get("New Value");
    String strObjId = (String) paramMap.get("objectId");
    Map mapRowData = null;
    String strColumnValues, strColumnType;
    DomainObject objDom = DomainObject.newInstance(context, strObjId);
    try{
//Modified:30-Apr-2010:di1:R210:PRG:Meeting Usability
        objDom.setDescription(context, strNewDescription);
//Modification End:30-Apr-2010:di1:R210:PRG:Meeting Usability
    }catch(Exception e){
      e.printStackTrace();
    }

   }
   // Update function for Meeting Duration
   // Fix for bug 366284
   public void updateMeetingDuration(Context context, String[] args)throws Exception {
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    HashMap paramMap = (HashMap)programMap.get("paramMap");
      String strNewDuration = (String)paramMap.get("New Value");
    String strMeetingId = (String)paramMap.get("objectId");
    AttributeType attrMeetingDuration       = new AttributeType(DomainObject.ATTRIBUTE_MEETING_DURATION);
    AttributeList attrListMeeting   = new AttributeList();
    DomainObject meetingDom = DomainObject.newInstance(context,strMeetingId);
    if(strNewDuration!=null && strNewDuration.length()!=0 && !"".equals(strNewDuration) )
      {
      attrListMeeting.addElement(new Attribute(attrMeetingDuration, strNewDuration));
      }
    try{
//Added:30-Apr-2010:di1:R210:PRG:Meeting Usability
      if (meetingDom.isKindOf(context, TYPE_MEETING)) {
    meetingDom.setAttributes(context,attrListMeeting);
        }
//Addition End:30-Apr-2010:di1:R210:PRG:Meeting Usability
    }catch(Exception e){
      e.printStackTrace();
    }

   }
   //getAgendaReportedAgainst
   public Vector getAgendaReportedAgainst(Context context, String[] args) throws Exception {
       Vector agendaAgainstItems = new Vector();
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       //<Fix 372291> For table Export or printer friendly (when reportFormat != null)
       //do not return HTML or href links
       Map paramList = (Map) programMap.get("paramList");
       boolean contextPopped=false;
       boolean hasAccess = false;
       String strObjType=null;
       String strAgendaAgainstName = null;
       boolean isReport =   paramList != null && !isEmpty((String)paramList.get("reportFormat"));
       //</Fix 372291>
       MapList objectList = (MapList)programMap.get("objectList");
       Iterator agendaObjectListItr = objectList.iterator();
       DomainObject domainObject = null;
       String sAgendaAgainstId = null;
       while(agendaObjectListItr.hasNext())
       {
           StringBuffer agendaSB = new StringBuffer();
           Map map = (Map)agendaObjectListItr.next();
//Modified:30-Apr-2010:di1:R210:PRG:Meeting Usability
           String strReportedAgainstItem = (String) map.get("ReportedAgainst");
           strObjType = (String) map.get(DomainConstants.SELECT_TYPE);
           
           if( RELATIONSHIP_AGENDA_ITEM.equals(strObjType) || null==strObjType){
//Modification End:30-Apr-2010:di1:R210:PRG:Meeting Usability
           StringTokenizer RepAgainstTkn = new StringTokenizer(strReportedAgainstItem, "|");
           while(RepAgainstTkn.hasMoreElements()){
               sAgendaAgainstId = RepAgainstTkn.nextToken();
               try{
                 domainObject = DomainObject.newInstance(context,sAgendaAgainstId);
               }catch(Exception ex){}
            if(null!=domainObject)
            {
              hasAccess = true;
              strAgendaAgainstName = domainObject.getInfo(context,SELECT_NAME);
            }
            else
            {
              ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), null, context.getVault().getName());
              domainObject = DomainObject.newInstance(context,sAgendaAgainstId);
              if(null!=domainObject)
                {
                strAgendaAgainstName = domainObject.getInfo(context,SELECT_NAME);
                }
              ContextUtil.popContext(context);
            }

               //<Fix 372291> For table Export or printer friendly when reportFormat != null
               //do not return HTML or href links
               if(isReport)
                   agendaSB.append(strAgendaAgainstName);
               else
               {
                 if(hasAccess){
                   agendaSB.append("<a href=\"javascript:emxTableColumnLinkClick('");
                   agendaSB.append(FrameworkUtil.encodeURL("emxTree.jsp?mode=replace&objectId="+sAgendaAgainstId));
                   agendaSB.append("','700','600','false','popup','');\">");
                   agendaSB.append(XSSUtil.encodeForXML(context,strAgendaAgainstName));
                   agendaSB.append("</a>");
               }
                 else
                 {
                   agendaSB.append(strAgendaAgainstName);
                 }
               }
               //Append comma(,) only when tokenizer has next token
               if(RepAgainstTkn.hasMoreElements())
                   agendaSB.append(",");
               //</Fix 372291>
           }
           agendaAgainstItems.add(agendaSB.toString());
       }
           else{
           agendaAgainstItems.add("");
           }
       }
       return agendaAgainstItems;
   }

   public String getCreateAgendaItemSeqNoHTMLOutPut(Context context, String[] args) throws Exception {
       String seqNo = getSequenceNumOnCreate(context, args);
       return seqNo + "<input type=\"hidden\" name=\"SequenceNumber\" value=\"" + XSSUtil.encodeForHTMLAttribute(context,seqNo) + "\"/>";
   }

  //getSequenceNumOnCreate
//Fix for  the bug 366418
  public String getSequenceNumOnCreate(Context context, String[] args) throws Exception {
      String SequenceNum = "0";
      MapList agendaItemList = new MapList();
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap          = (HashMap)programMap.get("requestMap");
        String strMeetingId = (String)requestMap.get("objectId");
        DomainObject MeetingObj = DomainObject.newInstance(context);
        MeetingObj.setId(strMeetingId);

        StringList selectRelStmts = new StringList();
        selectRelStmts.add(DomainConstants.SELECT_RELATIONSHIP_ID);
        selectRelStmts.add(DomainConstants.SELECT_NAME);
        selectRelStmts.add("attribute["+ATTRIBUTE_SEQUENCE+"]");
        StringList selectTypeStmts = new StringList();
        selectTypeStmts.add(MeetingObj.SELECT_ID);
        agendaItemList = MeetingObj.getRelatedObjects(context,
                RELATIONSHIP_AGENDA_ITEM,  //String relPattern
                SYMB_WILD, //String typePattern
                selectTypeStmts,          //StringList objectSelects,
                selectRelStmts,                     //StringList relationshipSelects,
                false,                     //boolean getTo,
                true,                     //boolean getFrom,
                (short)1,                 //short recurseToLevel,
                "",          //String objectWhere,
                "",                       //String relationshipWhere,
                null,                     //Pattern includeType,
                null,                     //Pattern includeRelationship,
                null);
                Vector temp = new Vector();
                MapList newAgendaList = new MapList();
                HashMap newMap = null;
                Iterator mapItr = agendaItemList.iterator();
                while (mapItr.hasNext()) {
                    Map map  = (Map)mapItr.next();
                    String sAgendaSeq = (String)map.get("attribute["+ATTRIBUTE_SEQUENCE+"]");
                    if(Integer.parseInt(sAgendaSeq) > Integer.parseInt(SequenceNum))
                        SequenceNum = sAgendaSeq;//valueOf(Integer.parseInt(sAgendaSeq));
                }
                int SeqNo = 1;
                if(Integer.parseInt(SequenceNum)!=0){
                SeqNo = Integer.parseInt(SequenceNum) + 1 ;
                }
                return Integer.toString(SeqNo);
  }

  /**
   * Program to get the connected project member if meeting is created under context of other objects like Workspace, Project etc..
   *
   * @param context the eMatrix Context object
   * @param String array contains connected context object object id
   * @throws Matrix Exception if the operation fails
   * @since V6 R207 Author : Haripriya K
   * @grade 0
   */
  public BusinessObject getProjectMember( matrix.db.Context context,String projectId , BusinessObject person) throws MatrixException
  {
    StringList EMPTY_STRING_LIST = new StringList(0);
    Hashtable projectMemberHashtable = null;
    Hashtable personHashtable = null;
    projectMemberHashtable = new Hashtable();
    personHashtable = new Hashtable();
    projectMemberHashtable.put(person.getObjectId(), personHashtable);
    String sProjectMembershipRel = PropertyUtil.getSchemaProperty(context,"relationship_ProjectMembership");
    String sProjectMembersRel = PropertyUtil.getSchemaProperty(context,"relationship_ProjectMembers");
    String sProjectMemberType = PropertyUtil.getSchemaProperty(context,"type_ProjectMember");
    String sProjectType = PropertyUtil.getSchemaProperty(context,"type_Project");
    String typePattern = sProjectMemberType;
    String relPattern =  sProjectMembershipRel;
    String strWhereClause = "to[" + sProjectMembersRel + "].from.id ==" + projectId;
    ContextUtil.startTransaction(context,false);
    ExpansionIterator expItr = person.getExpansionIterator(context, relPattern, typePattern, EMPTY_STRING_LIST,
            EMPTY_STRING_LIST,false, true, (short)1,strWhereClause,null,(short)0,false,false,(short)100, false);
    BusinessObject busProjectMember = null;
    try {
        if(expItr.hasNext())
            busProjectMember = expItr.next().getTo();
    } finally {
        expItr.close();
    }
    ContextUtil.commitTransaction(context);


    return busProjectMember;
  }

  /**
   * Program to get the cell level access for meeting objects under Workspace etc.
   *
   * @param context the eMatrix Context object
   * @@param String array contains Meetings Ids for edit
   * @throws Matrix Exception if the operation fails
   * @since V6 R207 Author : Haripriya K
   * @grade 0
   */

  public static StringList getCellLevelEditAccessforMeeting(Context context, String args[])throws Exception
  {
  HashMap inputMap = (HashMap)JPO.unpackArgs(args);

  MapList objectMap = (MapList) inputMap.get("objectList");

  StringList returnStringList = new StringList (objectMap.size());
  Iterator objectItr = objectMap.iterator();
  while (objectItr.hasNext())
  {
      Map curObjectMap = (Map) objectItr.next();
      String curObjectID = (String) curObjectMap.get("id");
      String objectName = (String) curObjectMap.get("Name");

      DomainObject dObj = DomainObject.newInstance(context,curObjectID);
      String strCurrent = (String)dObj.getInfo(context,DomainConstants.SELECT_CURRENT);

      if(strCurrent.equalsIgnoreCase("Create"))
      {
          returnStringList.addElement(Boolean.valueOf(true));
      }
      else
      {
          returnStringList.addElement(Boolean.valueOf(false));
      }
  }
  return returnStringList;
  }

public boolean isCreateAndDeleteAgendaItemEnabled(Context context, String[] args)throws Exception
  {
    return showCommandInStates(context, args, new String[]{STATE_MEETING_CREATE, STATE_MEETING_IN_PROGRESS, STATE_MEETING_SCHEDULED}).booleanValue();
  }
/**
 * Method to update Topic Duration in Agenda Item Summary Page.
 *
 * @param context the eMatrix Context object
 * @param String array contains RelIds to edit
 * @return Void
 * @throws Exception if the operation fails
 * @since V6 R207 Author : Haripriya
 * @grade 0
 */
public void updateTopicDuration(Context context, String[] args)throws Exception {

    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    HashMap paramMap = (HashMap)programMap.get("paramMap");
    HashMap requestMap = (HashMap)programMap.get("requestMap");

    String meetingId = (String)requestMap.get("objectId");
//Added:30-Apr-2010:di1:R210:PRG:Meeting Usability
    if (null == meetingId || "".equals(meetingId)
        || "null".equals(meetingId)) {
      meetingId = (String) paramMap.get("objectId");
    }
//Addition End:30-Apr-2010:di1:R210:PRG:Meeting Usability
    String strAgendaItemRelId = (String)paramMap.get("relId");
    String strNewTopicDuration = (String)paramMap.get("New Value");

    Meeting meeting = new Meeting(meetingId);
    StringList existingAgendaItemRelIdList = meeting.getAllAgendaItemRelationshipIDs(context, strAgendaItemRelId);
    AttributeList list = new AttributeList();
    list.addElement(new Attribute(new AttributeType(ATTRIBUTE_TOPIC_DURATION), strNewTopicDuration));
    for (Iterator iter = existingAgendaItemRelIdList.iterator(); iter.hasNext();) {
        DomainRelationship domRelAgendaItem = DomainRelationship.newInstance(context, (String) iter.next());
        domRelAgendaItem.setAttributes(context, list);
    }
}


    public static Object getMeetingStartTimeRangeValues(Context context, String[] args) throws Exception
    {
        HashMap tempMap = new HashMap();
        StringList fieldRangeValues = new StringList();
        StringList fieldDisplayRangeValues = new StringList();
        //     initialize the Stringlists fieldRangeValues, fieldDisplayRangeValues
        fieldRangeValues.addElement("12:00:00 AM");
        fieldRangeValues.addElement("12:30:00 AM");
        fieldRangeValues.addElement("1:00:00 AM");
        fieldRangeValues.addElement("1:30:00 AM");
        fieldRangeValues.addElement("2:00:00 AM");
        fieldRangeValues.addElement("2:30:00 AM");
        fieldRangeValues.addElement("3:00:00 AM");
        fieldRangeValues.addElement("3:30:00 AM");
        fieldRangeValues.addElement("4:00:00 AM");
        fieldRangeValues.addElement("4:30:00 AM");
        fieldRangeValues.addElement("5:00:00 AM");
        fieldRangeValues.addElement("5:30:00 AM");
        fieldRangeValues.addElement("6:00:00 AM");
        fieldRangeValues.addElement("6:30:00 AM");
        fieldRangeValues.addElement("7:00:00 AM");
        fieldRangeValues.addElement("7:30:00 AM");
        fieldRangeValues.addElement("8:00:00 AM");
        fieldRangeValues.addElement("8:30:00 AM");
        fieldRangeValues.addElement("9:00:00 AM");
        fieldRangeValues.addElement("9:30:00 AM");
        fieldRangeValues.addElement("10:00:00 AM");
        fieldRangeValues.addElement("10:30:00 AM");
        fieldRangeValues.addElement("11:00:00 AM");
        fieldRangeValues.addElement("11:30:00 AM");
        fieldRangeValues.addElement("12:00:00 PM");
        fieldRangeValues.addElement("12:30:00 PM");
        fieldRangeValues.addElement("1:00:00 PM");
        fieldRangeValues.addElement("1:30:00 PM");
        fieldRangeValues.addElement("2:00:00 PM");
        fieldRangeValues.addElement("2:30:00 PM");
        fieldRangeValues.addElement("3:00:00 PM");
        fieldRangeValues.addElement("3:30:00 PM");
        fieldRangeValues.addElement("4:00:00 PM");
        fieldRangeValues.addElement("4:30:00 PM");
        fieldRangeValues.addElement("5:00:00 PM");
        fieldRangeValues.addElement("5:30:00 PM");
        fieldRangeValues.addElement("6:00:00 PM");
        fieldRangeValues.addElement("6:30:00 PM");
        fieldRangeValues.addElement("7:00:00 PM");
        fieldRangeValues.addElement("7:30:00 PM");
        fieldRangeValues.addElement("8:00:00 PM");
        fieldRangeValues.addElement("8:30:00 PM");
        fieldRangeValues.addElement("9:00:00 PM");
        fieldRangeValues.addElement("9:30:00 PM");
        fieldRangeValues.addElement("10:00:00 PM");
        fieldRangeValues.addElement("10:30:00 PM");
        fieldRangeValues.addElement("11:00:00 PM");
        fieldRangeValues.addElement("11:30:00 PM");


        fieldDisplayRangeValues.addElement("12:00 AM");
        fieldDisplayRangeValues.addElement("12:30 AM");
        fieldDisplayRangeValues.addElement("1:00 AM");
        fieldDisplayRangeValues.addElement("1:30 AM");
        fieldDisplayRangeValues.addElement("2:00 AM");
        fieldDisplayRangeValues.addElement("2:30 AM");
        fieldDisplayRangeValues.addElement("3:00 AM");
        fieldDisplayRangeValues.addElement("3:30 AM");
        fieldDisplayRangeValues.addElement("4:00 AM");
        fieldDisplayRangeValues.addElement("4:30 AM");
        fieldDisplayRangeValues.addElement("5:00 AM");
        fieldDisplayRangeValues.addElement("5:30 AM");
        fieldDisplayRangeValues.addElement("6:00 AM");
        fieldDisplayRangeValues.addElement("6:30 AM");
        fieldDisplayRangeValues.addElement("7:00 AM");
        fieldDisplayRangeValues.addElement("7:30 AM");
        fieldDisplayRangeValues.addElement("8:00 AM");
        fieldDisplayRangeValues.addElement("8:30 AM");
        fieldDisplayRangeValues.addElement("9:00 AM");
        fieldDisplayRangeValues.addElement("9:30 AM");
        fieldDisplayRangeValues.addElement("10:00 AM");
        fieldDisplayRangeValues.addElement("10:30 AM");
        fieldDisplayRangeValues.addElement("11:00 AM");
        fieldDisplayRangeValues.addElement("11:30 AM");
        fieldDisplayRangeValues.addElement("12:00 PM");
        fieldDisplayRangeValues.addElement("12:30 PM");
        fieldDisplayRangeValues.addElement("1:00 PM");
        fieldDisplayRangeValues.addElement("1:30 PM");
        fieldDisplayRangeValues.addElement("2:00 PM");
        fieldDisplayRangeValues.addElement("2:30 PM");
        fieldDisplayRangeValues.addElement("3:00 PM");
        fieldDisplayRangeValues.addElement("3:30 PM");
        fieldDisplayRangeValues.addElement("4:00 PM");
        fieldDisplayRangeValues.addElement("4:30 PM");
        fieldDisplayRangeValues.addElement("5:00 PM");
        fieldDisplayRangeValues.addElement("5:30 PM");
        fieldDisplayRangeValues.addElement("6:00 PM");
        fieldDisplayRangeValues.addElement("6:30 PM");
        fieldDisplayRangeValues.addElement("7:00 PM");
        fieldDisplayRangeValues.addElement("7:30 PM");
        fieldDisplayRangeValues.addElement("8:00 PM");
        fieldDisplayRangeValues.addElement("8:30 PM");
        fieldDisplayRangeValues.addElement("9:00 PM");
        fieldDisplayRangeValues.addElement("9:30 PM");
        fieldDisplayRangeValues.addElement("10:00 PM");
        fieldDisplayRangeValues.addElement("10:30 PM");
        fieldDisplayRangeValues.addElement("11:00 PM");
        fieldDisplayRangeValues.addElement("11:30 PM");
        tempMap.put("field_choices", fieldRangeValues);
        tempMap.put("field_display_choices", fieldDisplayRangeValues);
        return tempMap;
    }

public static String getCompanyFieldHTMLOutput(Context context, String[] args) throws Exception{

        String strUserName = context.getUser();
        Person personObj =      Person.getPerson(context,strUserName);
        Company company = personObj.getCompany(context);
        String compName = company.getName();
        String hostCompanyName = PropertyUtil.getSchemaProperty(context,"role_CompanyName");

        StringBuffer companySelect = null;

        if(!compName.equals(hostCompanyName))
        {
            companySelect = new StringBuffer(150);
            companySelect.append("<input type=\"hidden\" name=\"Company\"  value=\"").append(XSSUtil.encodeForHTMLAttribute(context,company.getId())).append("\">").append("&nbsp;").append(XSSUtil.encodeForHTML(context, company.getName()));

        } else {
            StringList slSelect = new StringList();
            slSelect.add(SELECT_ID);
            slSelect.add(SELECT_NAME);

            MapList companies = DomainObject.findObjects(context, TYPE_COMPANY, SYMB_WILD, null, slSelect);
            Collections.sort(companies, new Comparator() {
                public int compare(Object arg0, Object arg1) {
                    Map map1 = (Map) arg0;
                    Map map2 = (Map) arg1;
                    String compName1 = (String) map1.get(SELECT_NAME);
                    String compName2 = (String) map2.get(SELECT_NAME);
                    return compName1.compareToIgnoreCase(compName2);
                }
            });
            companySelect = new StringBuffer(companies.size() * 100);
            companySelect.append("<select name=\"Company\" size=\"1\">");
            for (Iterator iter = companies.iterator(); iter.hasNext();) {
                Map companyDetails = (Map) iter.next();
                String id = (String) companyDetails.get(SELECT_ID);
                String name = (String) companyDetails.get(SELECT_NAME);
                String selected = name.equals(hostCompanyName) ? " selected" : "";
                companySelect.append("<option value=\"").append(XSSUtil.encodeForHTMLAttribute(context,id)).append('"').append(selected).append('>').append(XSSUtil.encodeForHTML(context, name)).append("</option>");
            }
            companySelect.append("</select>");
        }
        return companySelect.toString();
    }

  @com.matrixone.apps.framework.ui.ProgramCallable
  public static MapList getMeetingAttendeeSearchPersons(Context context, String[] args)
  throws Exception {

      Map programMap = (Map) JPO.unpackArgs(args);

      String strName = (String) programMap.get("User Name");
      String strFirstName = (String) programMap.get("First Name");
      String strLastName = (String) programMap.get("Last Name");
      String strCompany = (String) programMap.get("Company");

      String meetingObjectId = (String) programMap.get("objectId");
      String searchScope = (String) programMap.get("searchScope");
      boolean searchInMeetingAttendees = !isEmpty(searchScope) && searchScope.equals("MeetingAttendees");

      short sQueryLimit =
          (short) (java.lang.Integer.parseInt((String) programMap.get("queryLimit")));

      String strType = DomainConstants.TYPE_PERSON;
      String strAttribName = DomainConstants.ATTRIBUTE_DETAILS_NAME;
      String strAttribFirstName = DomainConstants.ATTRIBUTE_FIRST_NAME;
      String strAttribLastName = DomainConstants.ATTRIBUTE_LAST_NAME;
      String strRelEmployee = DomainConstants.RELATIONSHIP_EMPLOYEE;

      StringList slSelect = new StringList(DomainConstants.SELECT_ID);

      boolean bStart = true;
      StringBuffer sbWhereExp = new StringBuffer(100);

      if(searchInMeetingAttendees && !isEmpty(strName) &&  !strName.equals(SYMB_WILD)) {
          if (bStart) {
              sbWhereExp.append(SYMB_OPEN_PARAN);
              bStart = false;
          }
          sbWhereExp.append(SYMB_OPEN_PARAN);
          sbWhereExp.append(strAttribName);
          sbWhereExp.append(SYMB_MATCH);
          sbWhereExp.append(SYMB_QUOTE);
          sbWhereExp.append(strName);
          sbWhereExp.append(SYMB_QUOTE);
          sbWhereExp.append(SYMB_CLOSE_PARAN);
      }
      if (!isEmpty(strFirstName) && !strFirstName.equals(SYMB_WILD)) {
          if (bStart) {
              sbWhereExp.append(SYMB_OPEN_PARAN);
              bStart = false;
          }
          sbWhereExp.append(SYMB_OPEN_PARAN);
          sbWhereExp.append(SYMB_ATTRIBUTE);
          sbWhereExp.append(SYMB_OPEN_BRACKET);
          sbWhereExp.append(strAttribFirstName);
          sbWhereExp.append(SYMB_CLOSE_BRACKET);
          sbWhereExp.append(SYMB_MATCH);
          sbWhereExp.append(SYMB_QUOTE);
          sbWhereExp.append(strFirstName);
          sbWhereExp.append(SYMB_QUOTE);
          sbWhereExp.append(SYMB_CLOSE_PARAN);
      }

      if (!isEmpty(strLastName) && !strLastName.equals(SYMB_WILD)) {
          if (bStart) {
              sbWhereExp.append(SYMB_OPEN_PARAN);
              bStart = false;
          } else {
              sbWhereExp.append(SYMB_AND);
          }
          sbWhereExp.append(SYMB_OPEN_PARAN);
          sbWhereExp.append(SYMB_ATTRIBUTE);
          sbWhereExp.append(SYMB_OPEN_BRACKET);
          sbWhereExp.append(strAttribLastName);
          sbWhereExp.append(SYMB_CLOSE_BRACKET);
          sbWhereExp.append(SYMB_MATCH);
          sbWhereExp.append(SYMB_QUOTE);
          sbWhereExp.append(strLastName);
          sbWhereExp.append(SYMB_QUOTE);
          sbWhereExp.append(SYMB_CLOSE_PARAN);
      }

      if (!isEmpty(strCompany) && !strCompany.equals(SYMB_WILD)) {
          if (bStart) {
              sbWhereExp.append(SYMB_OPEN_PARAN);
              bStart = false;
          } else {
              sbWhereExp.append(SYMB_AND);
          }
          sbWhereExp.append(SYMB_OPEN_PARAN);
          sbWhereExp.append(SYMB_TO);
          sbWhereExp.append(SYMB_OPEN_BRACKET);
          sbWhereExp.append(strRelEmployee);
          sbWhereExp.append(SYMB_CLOSE_BRACKET);
          sbWhereExp.append(SYMB_DOT);
          sbWhereExp.append(SYMB_FROM);
          sbWhereExp.append(SYMB_DOT);
          sbWhereExp.append(DomainConstants.SELECT_ID);
          sbWhereExp.append(SYMB_MATCH);
          sbWhereExp.append(SYMB_QUOTE);
          sbWhereExp.append(strCompany);
          sbWhereExp.append(SYMB_QUOTE);
          sbWhereExp.append(SYMB_CLOSE_PARAN);

      }

      String strFilteredExpression = getFilteredExpression(context,programMap);

      if (!isEmpty(strFilteredExpression)) {
          if (bStart) {
              sbWhereExp.append(SYMB_OPEN_PARAN);
              bStart = false;
          } else {
              sbWhereExp.append(SYMB_AND);
          }
          sbWhereExp.append(strFilteredExpression);
      }

      if (!bStart) {
          sbWhereExp.append(SYMB_CLOSE_PARAN);
      }

      if(searchInMeetingAttendees) {
          DomainObject meetingObj = DomainObject.newInstance(context);
          meetingObj.setId(meetingObjectId);
          return meetingObj.getRelatedObjects(context,
                  DomainConstants.RELATIONSHIP_ASSIGNED_MEETINGS,  //String relPattern
                  DomainConstants.TYPE_PERSON, //String typePattern
                  slSelect,          //StringList objectSelects,
                  null,                     //StringList relationshipSelects,
                  true,                     //boolean getTo,
                  true,                     //boolean getFrom,
                  (short)1,                 //short recurseToLevel,
                  sbWhereExp.toString(),    //String objectWhere,
                  "",                       //String relationshipWhere,
                  sQueryLimit,              //Query Limit
                  null,                     //Pattern includeType,
                  null,                     //Pattern includeRelationship,
                  null);


      } else {
          String strVault = PersonUtil.getSearchVaults(context,false, PersonUtil.SEARCH_ALL_VAULTS);
          return  DomainObject.findObjects(
                  context,
                  strType,
                  isEmpty(strName) ? SYMB_WILD : strName,
                  SYMB_WILD,
                  SYMB_WILD,
                  strVault,
                  sbWhereExp.toString(),
                  "",
                  true,
                  slSelect,
                  sQueryLimit);
      }
   }

  private static boolean isEmpty (String stringToCheck) {
      return stringToCheck == null  || stringToCheck.equals("") || "null".equalsIgnoreCase(stringToCheck);
  }
  private Map getMeetingContextObject(Context context, DomainObject meetingObject, StringList selectItems, boolean userSuperUserContext) throws Exception {
      boolean isContextPushed = false;

      try {
          if(userSuperUserContext) {
              ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), null, context.getVault().getName());
              isContextPushed = true;
          }

          MapList mapList = meetingObject.getRelatedObjects(context,
                  DomainConstants.RELATIONSHIP_MEETING_CONTEXT,
                  SYMB_WILD,   //String typePattern
                  selectItems,  //StringList objectSelects,
                  null,        //StringList relationshipSelects,
                  true,        //boolean getTo,
                  false,       //boolean getFrom,
                  (short)1,    //short recurseToLevel,
                  "",          //expandTypeWhere,
                  "",          //String relationshipWhere,
                  null,        //Pattern includeType,
                  null,        //Pattern includeRelationship,
                  null);

          if(mapList.size() == 0) {
              return null;
          } else if(mapList.size() == 1) {
              return (Map) mapList.get(0);
          } else {
              //Some thing wrong, Meeting object should be connected to only one context object allways
              throw new RuntimeException(ComponentsUtil.i18nStringNow("emxComponents.MeetingBase.MoreThan1ContextConnectedToMeeting", context.getLocale().getLanguage()));
          }
      } finally {
          if(isContextPushed)
              ContextUtil.popContext(context);
      }
  }

  /**
   * Method to update Topic of agenda item while Editing the Agenda Item.
   *
   * @param context the eMatrix Context object
   * @param String array contains Meetings Ids to delete
   * @return void.
   * @throws Exception if the operation fails
   * @since V6 R207 Author : LVC
   * @grade 0
   */
  public void updateAgendaTopicName(Context context, String[] args)throws Exception {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      HashMap paramMap = (HashMap)programMap.get("paramMap");
      HashMap requestMap = (HashMap)programMap.get("requestMap");

      String meetingId = (String)requestMap.get("objectId");
      String strAgendaItemRelId = (String)paramMap.get("relId");
      String strNewTopicName = (String)paramMap.get("New Value");

      Meeting meeting = new Meeting(meetingId);
      StringList existingAgendaItemRelIdList = meeting.getAllAgendaItemRelationshipIDs(context, strAgendaItemRelId);
      AttributeList list = new AttributeList(1);
      list.addElement(new Attribute(new AttributeType(ATTRIBUTE_TOPIC), strNewTopicName));
      for (Iterator iter = existingAgendaItemRelIdList.iterator(); iter.hasNext();) {
          DomainRelationship domRelAgendaItem = DomainRelationship.newInstance(context, (String) iter.next());
          domRelAgendaItem.setAttributes(context, list);
      }
   }

  /**
   * Update program to update Agenda Reported Againt from Agenda Summary Page(structure browser)
   * @param context
   * @param args
   * @throws Exception
   */
  public void updateAgendaReportedAgainst(Context context, String[] args)throws Exception {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      HashMap paramMap = (HashMap)programMap.get("paramMap");
      HashMap requestMap = (HashMap)programMap.get("requestMap");

      String meetingId = (String)requestMap.get("objectId");
      String strAgendaItemRelId = (String)paramMap.get("relId");
      String strNewIds = (String)paramMap.get("New Value");

      /**
       *In Agenda Item Edit post process we are
       */
      if(!"true".equals(requestMap.get("shouldUpdateMeetingAgendaTopicItems")))
          return;

      Meeting meeting = new Meeting(meetingId);
      DomainRelationship agendaItemRel = new DomainRelationship(strAgendaItemRelId);
      Map agendaItemRelAttrMap = agendaItemRel.getAttributeMap(context, false);
      String connectedPersons = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump $3", strAgendaItemRelId, "frommid.to.id", ",");

      StringList existingAgendaItemRelIdList = meeting.getAllAgendaItemRelationshipIDs(context, strAgendaItemRelId);
      StringList newReportedAgainstList = FrameworkUtil.split(strNewIds, ",");
      StringList connectedPersonIdList = FrameworkUtil.split(connectedPersons, ",");

      for (Iterator iter = existingAgendaItemRelIdList.iterator(); iter.hasNext();) {
          StringBuffer removeAgendaItemsCommand = new StringBuffer(50);
          removeAgendaItemsCommand.append("delete connection ").append("$1").append(';');
          MqlUtil.mqlCommand(context, removeAgendaItemsCommand.toString(), (String)iter.next());
      }

      AttributeList attrListAgendaItemRel = new AttributeList();
      for (Iterator iter = agendaItemRelAttrMap.keySet().iterator(); iter.hasNext();) {
          String attribute = (String) iter.next();
          String value = (String) agendaItemRelAttrMap.get(attribute);
          AttributeType attributeType = new AttributeType(attribute);
          attrListAgendaItemRel.add(new Attribute(attributeType, value));
      }

      if(newReportedAgainstList.isEmpty())
          newReportedAgainstList.add(meetingId);

      for (Iterator iter = newReportedAgainstList.iterator(); iter.hasNext();) {
        String associatedObjectId = (String) iter.next();
        DomainObject associatedDom = DomainObject.newInstance(context, associatedObjectId);
        DomainRelationship relAgendaItem = DomainRelationship.connect(context, meeting, RELATIONSHIP_AGENDA_ITEM, associatedDom);
        relAgendaItem.setAttributes(context, attrListAgendaItemRel);
        for (Iterator personIdItr = connectedPersonIdList.iterator(); personIdItr.hasNext();) {
            StringBuffer connectPersonsCommand = new StringBuffer(100);

            connectPersonsCommand.append("add connection ").
                                  append("$1").
                                  append(" fromrel ").append("$2").
                                  append(" to ").
                                  append("$3").append(';');
            MqlUtil.mqlCommand(context, connectPersonsCommand.toString(), RELATIONSHIP_AGENDA_RESPONSIBILITY, relAgendaItem.toString().trim(), (String)personIdItr.next());
        }
      }
  }

  /**
   * Returns the Current Steate of the given meeting object
   * @param context
   * @param meetingObj
   * @return
   * @throws Exception
   */
  private static String getMeetingState(Context context, Meeting meetingObj) throws Exception {
      return meetingObj.getInfo(context, DomainConstants.SELECT_CURRENT);
  }

  /**
   * Returns true if the context user is the owner of the given meeting
   * @param context
   * @param meetingObj
   * @return
   * @throws Exception
   */
  private static boolean isMeetingHost(Context context, Meeting meetingObj) throws Exception {
      return context.getUser().equals(meetingObj.getInfo(context, DomainConstants.SELECT_OWNER));
  }

  /**
   * This method return boolean value to show or hide command
   * Returns false if the context user is not the meeting host
   * Returns false if the current state of meeting is not in the given array of states
   * @param context
   * @param args
   * @param updateInvitation
   * @return
   * @throws Exception
   */
  private static Boolean showCommandInStates(Context context, String[] args, String[] states)throws Exception {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      String strMeetingId = (String)programMap.get("objectId");
      Meeting meetingObj = new Meeting(strMeetingId);
      if(!isMeetingHost(context, meetingObj))
          return Boolean.valueOf(false);
      String currentState = getMeetingState(context, meetingObj);
      for (int i = 0; i < states.length; i++) {
        if(currentState.equals(states[i]))
            return Boolean.valueOf(true);

      }
      return Boolean.valueOf(false);
  }

  /**
   * This method return boolean value to show or hide MeetingSend Or UpdateInvitations commands
   * Returns false if the context user is not the meeting host
   * Returns false if the current state of meeting is not Create
   * If updateInvitation is true and the meeting is promoted to scheduled state returns true otherwise return false
   * If updateInvitation is false and the meeting is not promoted to scheduled state returns true otherwise return false
   * @param context
   * @param args
   * @param updateInvitation
   * @return
   * @throws Exception
   */
  private static Boolean showSendOrUpdateInvitationsCommand(Context context, String[] args, boolean updateInvitation)  throws Exception {
      if(!showCommandInStates(context, args, new String[] {STATE_MEETING_CREATE}).booleanValue())
          return Boolean.valueOf(false);

      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      String strMeetingId = (String)programMap.get("objectId");
      /**
       * Check whether this meeting is promoted to Scheduled state
       * If the start time stamp is not blank this meeting is promoted to Scheduled state earlier
       * state[Scheduled].start returns empty string if the object has never been in the state.
       */
      boolean promoted = MqlUtil.mqlCommand(context,
                                            "print bus $1 select $2 dump $3", strMeetingId, "state["+STATE_MEETING_SCHEDULED+"].start", ",").trim().length() > 0;
      return updateInvitation ? Boolean.valueOf(promoted) : Boolean.valueOf(!promoted);
  }

  /**
   * Access Program for APPMeetingPromoteSendInvitations command
   * This method return boolean value to show or hide Send Invitations command
   * also see <code>showMeetingSendOrUpdateInvitationsCommand(context, args[], pdateInvitation)</code> method
   * @param context
   * @param args
   * @return
   * @throws Exception
   */
  public static Boolean showMeetingPromoteSendInvitationsCommand(Context context, String[] args)  throws Exception {
      return showSendOrUpdateInvitationsCommand(context, args, false);
  }

  /**
   * Access Program for APPMeetingPromoteUpdateInvitations command
   * This method return boolean value to show or hide Update Invitations command
   * also see <code>showMeetingSendOrUpdateInvitationsCommand(context, args[], pdateInvitation)</code> method
   * @param context
   * @param args
   * @return
   * @throws Exception
   */

  public static Boolean showMeetingPromoteUpdateInvitationsCommand(Context context, String[] args)  throws Exception {
      return showSendOrUpdateInvitationsCommand(context, args, true);
  }

  /**
   * Access Program for APPMeetingPromoteStartMeeting command
   * This method return boolean value to show or hide 'Start Meeting' command
   * Retrun true when logged in user is meeting host and meeting is in Scheduled state
   * also see <code>showCommandInStates(context, args[], states)</code> method
   * @param context
   * @param args
   * @return
   * @throws Exception
   */

  public static Boolean showMeetingPromoteStartMeetingCommand(Context context, String[] args) throws Exception{
      return (showCommandInStates(context, args, new String[] {STATE_MEETING_SCHEDULED}));
  }

  /**
   * Access Program for APPMeetingPromoteCompleteMeeting command
   * This method return boolean value to show or hide 'Close Meeting' command
   * Retrun true when logged in user is meeting host and meeting is in In Progress state
   * also see <code>showCommandInStates(context, args[], states)</code> method
   * @param context
   * @param args
   * @return
   * @throws Exception
   */
  public static Boolean showMeetingPromoteCompleteCommand(Context context, String[] args)  throws Exception {
      return showCommandInStates(context, args, new String[] {STATE_MEETING_IN_PROGRESS});
  }

  /**
   * Access Program for APPMeetingDemoteForEdit command
   * This method return boolean value to show or hide 'Demote for Editing' command
   * Retrun true when logged in user is meeting host and meeting is in Scheduled state
   * also see <code>showCommandInStates(context, args[], states)</code> method
   * @param context
   * @param args
   * @return
   * @throws Exception
   */

  public static Boolean showMeetingDemoteForEditCommand(Context context, String[] args)  throws Exception {
      return showCommandInStates(context, args, new String[] {STATE_MEETING_SCHEDULED});
  }

  /**
   * Access Program for APPMeetingDomote command
   * This method return boolean value to show or hide 'Close Meeting' command
   * Retrun true when logged in user is meeting host and meeting is in In Progress or Complete state
   * also see <code>showCommandInStates(context, args[], states)</code> method
   * @param context
   * @param args
   * @return
   * @throws Exception
   */
  public static Boolean showMeetingDemoteCommand(Context context, String[] args) throws Exception {
      return showCommandInStates(context, args, new String[] {STATE_MEETING_IN_PROGRESS, STATE_MEETING_COMPLETE});
  }

  @com.matrixone.apps.framework.ui.PreProcessCallable
  public Map meetingAgendaItemEditPreProcess(Context context, String[] args) throws Exception {
      Map programMap = (Map) JPO.unpackArgs(args);
      Map paramMap = (Map) programMap.get("paramMap");
      String meetingId = (String) paramMap.get("objectId");
      Meeting meeting = new Meeting(meetingId);
	  Locale strLocale = new Locale(context.getLocale().getLanguage());

      String agendaRelIds = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", meetingId, "relationship["+ RELATIONSHIP_AGENDA_ITEM + "].id");

      Map returnMap = new HashMap();
      returnMap.put("ObjectList", new MapList());
      if(agendaRelIds.equals(""))
      {
          returnMap.put ("Action", "continue");
      } else if(isMeetingHost(context, meeting))
      {
          if(getMeetingState(context, meeting).equals(STATE_MEETING_COMPLETE))
          {
              returnMap.put ("Action", "stop");
              returnMap.put("Message",EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",strLocale, "emxComponents.Meeting.Agenda.CanNotEditInMeetingCompleteState")); 
          } else
          {
              returnMap.put ("Action", "continue");
          }
      } else
      {
          returnMap.put ("Action", "stop");
          returnMap.put("Message",
                  EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",strLocale, "emxComponents.Meeting.Agenda.EditByNonHostPerson"));
      }

      return returnMap;
  }

  @com.matrixone.apps.framework.ui.PostProcessCallable
  public Map meetingAgendaItemEditPostProcess(Context context, String[] args) throws Exception {
      Map programMap = (Map) JPO.unpackArgs(args);

      Map requestMap = (Map) programMap.get("requestMap");
      String timeZone = (String)requestMap.get("timeZone");

      com.matrixone.jdom.Document doc = (com.matrixone.jdom.Document)programMap.get("XMLDoc");

      boolean reloadSB = updateMeetingAgendaTopicItems(context, doc, timeZone, context.getLocale());

      Map returnMap = new HashMap();
      if(reloadSB) {
          returnMap.put ("Action", "execScript");
          returnMap.put("Message", "{ main:function()  {window.location.href=window.location.href;}}");
      } else {
          returnMap.put ("Action", "continue");
      }
      return returnMap;
  }

  private boolean updateMeetingAgendaTopicItems(Context context, com.matrixone.jdom.Document document, String timeZone, java.util.Locale locale) throws Exception
  {
      boolean refreshSB = false;

      UITableIndented uiti = new UITableIndented();
      Element root = document.getRootElement();
      HashMap requestMap = uiti.getRequestMapFromDocument(context, document);
      HashMap columnsMap = uiti.getColumnsMapFromDocument(context, document);
      java.util.List elmlist = root.getChildren("object");
      java.util.Iterator itr = elmlist.iterator();
      while(itr.hasNext()) {
          Element elm = (Element)itr.next();
          String objectId = elm.getAttributeValue("objectId");
          String relId = elm.getAttributeValue("relId");
          String strMarkup = elm.getAttributeValue("markup");
          if("changed".equals(strMarkup)){
              HashMap chgColumnMap = uiti.getChangedColumnMapFromElement(context, elm);
              if(chgColumnMap.containsKey("AgendaAgainst")) {
                  HashMap agendaAgainstMap = new HashMap();
                  agendaAgainstMap.put("AgendaAgainst", chgColumnMap.get("AgendaAgainst"));
                  requestMap.put("shouldUpdateMeetingAgendaTopicItems", "true");
                  uiti.updateTableData(context, requestMap, objectId, relId, timeZone, columnsMap, agendaAgainstMap, locale);
                  refreshSB = true;
              }
          }
      }
      return refreshSB;
  }


//IR-031335V6R2011
    /**
     * Method used to check if the context user is an Attendee of the Meeting
     *
     * @param context - the eMatrix <code>Context</code> object
     * @param args - string array containing the packed program information
     * @return boolean - true if the context user is a Meeting attendee
     * @throws Exception if the operation fails
     * @since v6r2011
     */
  public boolean isMeetingAttendee( Context context, String[] args ) throws Exception {
    boolean bReturn = true;
    try {
      Map programMap         = (HashMap) JPO.unpackArgs( args );
      String sMeetingId      = (String) programMap.get( "objectId" );
      DomainObject doMeeting = DomainObject.newInstance(context, sMeetingId);
      if(doMeeting.getType(context).equals(DomainObject.TYPE_MEETING)){
        String sWhereExp = "( id == \"" + PersonUtil.getPersonObjectID( context, context.getUser() ) + "\" )";
        MapList mlAttendees    = doMeeting.getRelatedObjects( context,
                                                              RELATIONSHIP_ASSIGNED_MEETINGS,
                                                              TYPE_PERSON,
                                                              new StringList( SELECT_ID ),
                                                              new StringList( SELECT_RELATIONSHIP_ID ),
                                                              true,
                                                              false,
                                                              (short) 1,
                                                              sWhereExp,
                                                              EMPTY_STRING,
                                                              0 );
        if( mlAttendees.size() < 1 ) bReturn = false;
      }

    } catch( Exception ex ) {
      throw ex;
    }

    return bReturn;
  }

//Added (Methods):30-Apr-2010:di1:R210:PRG:Meeting Usability

  /**
   * Returns the related objects for Meeting (i.e. Agendaitem/Decision/Person)
   * object in APPMeetingSummary
   *
   * @param context
   *            Matrix Context object
   * @param args
   *            String array
   * @return vector holding mentioned values
   * @throws Exception
   *             if operation fails
   */

  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getTableMeetingData(Context context, String[] args)
      throws Exception {
    MapList resultList = new MapList();
    Map tempMap = null;

    HashMap programMap = (HashMap) JPO.unpackArgs(args);
    String parentId = (String) programMap.get("parentId");
    String objectId = (String) programMap.get("objectId");

    if (null != parentId && null != objectId && !parentId.equals(objectId)) {

      MapList agendaList = getMeetingAgendas(context, args);
      MapList attendeeList = getMeetingttendee(context, args);
      MapList decisionList = (MapList) JPO.invoke(context, "emxDecision",
          null, "getRelatedDecisions", args, MapList.class);

      for (int i = 0; i < agendaList.size(); i++) {
        tempMap = (Map) agendaList.get(i);
        tempMap.put(DomainConstants.SELECT_TYPE,
            RELATIONSHIP_AGENDA_ITEM);
        tempMap.put("disableSelection","true");
        resultList.add(tempMap);
      }

      for (int i = 0; i < attendeeList.size(); i++) {
          tempMap = (Map) attendeeList.get(i);
          tempMap.put("disableSelection","true");
          resultList.add(tempMap);
      }

      for (int i = 0; i < decisionList.size(); i++) {
          tempMap = (Map) decisionList.get(i);
          tempMap.put("disableSelection","true");
          resultList.add(tempMap);
      }
    }
    return resultList;
  }

  /**
   * Returns the name values for Meeting/Agendaitem/Decision/person object in
   * APPMeetingSummary
   *
   * @param context
   *            Matrix Context object
   * @param args
   *            String array
   * @return vector holding mentioned values
   * @throws Exception
   *             if operation fails
   */

  public Vector getColumnMeetingNameData(Context context, String[] args) throws Exception {
    try {
      // Create result vector
      Vector vecResult = new Vector();
      // Get object list information from packed arguments
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      MapList objectList = (MapList) programMap.get("objectList");
      HashMap paramList = (HashMap) programMap.get("paramList");
      String strPrinterFriendly = (String) paramList.get("reportFormat");
      boolean isPrinterFriendly = strPrinterFriendly != null;
      Map mapRowData = null;
      StringBuffer columnName = null;
      String strColumnName, strColumnId, strColumnType;
      for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
        columnName = new StringBuffer();
        mapRowData = (Map) itrObjects.next();
        strColumnName = (String) mapRowData.get(SELECT_NAME);
        strColumnId = (String) mapRowData.get(SELECT_ID);
        strColumnType = (String) mapRowData.get(SELECT_TYPE);

        if (RELATIONSHIP_AGENDA_ITEM.equals(strColumnType)) {
          columnName.append("<img title='"+strColumnName+"' src='./images/iconSmallDecision.gif' border='0' valign='absmiddle'/> ");
          columnName.append(strColumnName);
        } else if (TYPE_DECISION.equals(strColumnType)) {
          columnName.append("<img title='"+strColumnName+"' src='./images/iconSmallDecision.gif' border='0' valign='absmiddle'/> ");
          columnName.append(strColumnName);
        } else if (TYPE_PERSON.equals(strColumnType)) {
          columnName.append("<img title='"+strColumnName+"' src='./images/iconSmallPerson.gif' border='0' valign='absmiddle'/> ");
          columnName.append(strColumnName);
        } else {

          columnName.append("<a href=\"JavaScript:showDialog('");
          columnName.append(FrameworkUtil.encodeURL("./emxTree.jsp?form=type_MeetingForm&mode=insert&Mode=view&objectId="+ strColumnId));
          columnName.append("')\">");
          columnName.append("<img src='./images/iconSmallMeeting.gif' border='0' valign='absmiddle'/>  ");
          columnName.append(XSSUtil.encodeForXML(context,strColumnName));
          columnName.append("</a>");
        }
        if(!isPrinterFriendly){
        vecResult.add(columnName.toString());
        }else if ("HTML".equals(strPrinterFriendly)){
          vecResult.add("<label>"+XSSUtil.encodeForHTML(context,strColumnName)+"</label>");
        }else{
          vecResult.add(strColumnName);
        }
      }
      return vecResult;
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new MatrixException(ex);
    }
  }

  /**
   * Returns the Description for Meeting and its realted objects
   * Item/Decision/person object in APPMeetingSummary
   *
   * @param context
   *            Matrix Context object
   * @param args
   *            String array
   * @return vector holding mentioned values
   * @throws Exception
   *             if operation fails
   */

  public Vector getColumnMeetingDescriptionData(Context context, String[] args)
      throws Exception {
    try {
      // Create result vector
      Vector vecResult = new Vector();
      // Get object list information from packed arguments
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      MapList objectList = (MapList) programMap.get("objectList");
      Map mapRowData = null;
      String strColumnValues = null;
      String strObjId = null;
      String strObjType = null;
      DomainObject objDom = null;
      for (Iterator itrObjects = objectList.iterator(); itrObjects
          .hasNext();) {
        mapRowData = (Map) itrObjects.next();
        strObjId = (String) mapRowData.get(SELECT_ID);
        strObjType = (String) mapRowData.get(SELECT_TYPE);
        if (!RELATIONSHIP_AGENDA_ITEM.equals(strObjType)) {
          objDom = newInstance(context, strObjId);
          strColumnValues = objDom.getDescription(context);
        } else {
          strColumnValues = "";
        }
        vecResult.add(strColumnValues);
      }
      return vecResult;
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new MatrixException(ex);
    }

  }

  /**
   * Returns the Start Date value for Meeting and its related object in
   * APPMeetingSummary
   *
   * @param context
   *            Matrix Context object
   * @param args
   *            String array
   * @return vector holding mentioned values
   * @throws Exception
   *             if operation fails
   */

  public Vector getColumnMeetingDateData(Context context, String[] args) throws Exception {
    try {
      // Create result vector
      Vector vecResult = new Vector();
      // Get object list information from packed arguments
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      MapList objectList = (MapList) programMap.get("objectList");
      double clientTZOffset = Double.parseDouble((String) ((Map) programMap.get("paramList")).get("timeZone"));
      Map mapRowData = null;
      String strObjectId = null;
      String strColumnValues = null;
      for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
        mapRowData = (Map) itrObjects.next();
        String strObjectType = (String) mapRowData.get(SELECT_TYPE);

        if (strObjectType.equals(TYPE_MEETING)) {
          strObjectId = (String) mapRowData.get(SELECT_ID);
          String strMeetingDate = new Meeting(strObjectId).getInfo(context, "attribute["+DomainConstants.ATTRIBUTE_MEETING_START_DATETIME+"]");
    	  int index = strMeetingDate.indexOf(" ");
          vecResult.add(strMeetingDate.substring(0, index));
          
        } else {
          vecResult.add("");
        }
      }
      return vecResult;
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new MatrixException(ex);
    }

  }

  /**
   * Returns the Meeting Duration attribute value for Meeting/Agenda
   * Item/Decision/person object in APPMeetingSummary
   *
   * @param context
   *            Matrix Context object
   * @param args
   *            String array
   * @return vector holding mentioned values
   * @throws Exception
   *             if operation fails
   */

  public Vector getColumnMeetingDurationData(Context context, String[] args)
      throws Exception {
    try {
      // Create result vector
      Vector vecResult = new Vector();
      // Get object list information from packed arguments
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      HashMap paramList = (HashMap) programMap.get("paramList");
      boolean isPrinterFriendly = false;
      String strPrinterFriendly = (String) paramList.get("reportFormat");

      if (strPrinterFriendly != null) {
        isPrinterFriendly = true;
      }
      else
      {
        strPrinterFriendly="";
      }
      MapList objectList = (MapList) programMap.get("objectList");
      Map mapRowData = null;
      String strColumnValues = null;
      String strObjId = null;
      String strObjRelId = null;
      for (Iterator itrObjects = objectList.iterator(); itrObjects
          .hasNext();) {
        mapRowData = (Map) itrObjects.next();
        strObjId = (String) mapRowData.get(SELECT_ID);
        DomainObject objDbo = newInstance(context, strObjId);
        if (objDbo.isKindOf(context, TYPE_MEETING)) {
          strObjRelId = (String) mapRowData.get(DomainConstants.SELECT_RELATIONSHIP_ID);
          Map resultList = null;
          if (null == strObjRelId || "".equals(strObjRelId)
              || "null".equalsIgnoreCase(strObjRelId)) {
            strColumnValues = objDbo.getAttributeValue(context,
                ATTRIBUTE_MEETING_DURATION);
          } else {
            ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), null, context.getVault().getName());
            DomainRelationship RelDobj = DomainRelationship
                .newInstance(context, strObjRelId);
            strColumnValues = RelDobj.getAttributeValue(context,
                ATTRIBUTE_TOPIC_DURATION);
            ContextUtil.popContext(context);
          }
        } else {
          strColumnValues = "";
        }
        if (!isPrinterFriendly) {
          vecResult.add(strColumnValues);
        }else if ("HTML".equals(strPrinterFriendly)){
                    vecResult.add(strColumnValues);
        }else{
          vecResult.add(strColumnValues);
        }
      }
      return vecResult;
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new MatrixException(ex);
    }
  }

  /**
   * Returns the Current State for Meeting/Agenda Item/Decision/person object
   * in APPMeetingSummary
   *
   * @param context
   *            Matrix Context object
   * @param args
   *            String array
   * @return vector holding mentioned values
   * @throws Exception
   *             if operation fails
   */

  public Vector getColumnMeetingCurrentStateData(Context context,
      String[] args) throws Exception {
    try {
      // Create result vector
      Vector vecResult = new Vector();
      // Get object list information from packed arguments
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      MapList objectList = (MapList) programMap.get("objectList");
      HashMap paramList  = (HashMap) programMap.get("paramList");
      String strLanguage = (String) paramList.get("languageStr");
      Map mapRowData = null;
      String strColumnValues,strColumnPolicy = null;
      //getting the updated value for state.
      StringList currentInfo = new StringList(2);
      currentInfo.add(SELECT_CURRENT);
      currentInfo.add(SELECT_POLICY);
      objectList = getUpdatedColumnValues(context, objectList, currentInfo);
      
      for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
        mapRowData = (Map) itrObjects.next();
        strColumnValues = (String) mapRowData.get(SELECT_CURRENT);
        strColumnPolicy = (String) mapRowData.get(SELECT_POLICY);
        strColumnValues=i18nNow.getStateI18NString(strColumnPolicy,strColumnValues, strLanguage);
        vecResult.add(strColumnValues);
      }
      return vecResult;
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new MatrixException(ex);
    }
  }
  /**
   * Getting the updated value of the object list.
   */
  private static MapList getUpdatedColumnValues(Context context, MapList objectList, StringList selectables) throws Exception {
	  String oidsArray[] = new String[objectList.size()];
	  for (int i = 0; i < objectList.size(); i++)
	  {
		  oidsArray[i] = (String)((Map)objectList.get(i)).get("id");
	  }
	  return DomainObject.getInfo(context, oidsArray, selectables);
  }
  /**
   * Returns the Relationship between Meeting and Agenda Item/Decision/Person
   * object in APPMeetingSummary
   *
   * @param context
   *            Matrix Context object
   * @param args
   *            String array
   * @return vector holding mentioned values
   * @throws Exception
   *             if operation fails
   */

  public Vector getColumnMeetingRelationshipData(Context context,
      String[] args) throws Exception {
    try {
      // Create result vector
      Vector vecResult = new Vector();
      // Get object list information from packed arguments
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      MapList objectList = (MapList) programMap.get("objectList");
      HashMap paramList  = (HashMap) programMap.get("paramList");
      String strLanguage = (String) paramList.get("languageStr");
      Map mapRowData = null;
      String strColumnValues = null;
      String strMeetingAttendee=EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale(strLanguage),"emxFramework.Attribute.Meeting_Attendee");
      String strDecision= EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale(strLanguage),"emxFramework.Relationship.Decision");
      String strAgendaItem= EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale(strLanguage),"emxFramework.Relationship.Agenda_Item");
      for (Iterator itrObjects = objectList.iterator(); itrObjects
          .hasNext();) {
        mapRowData = (Map) itrObjects.next();
        if (mapRowData.get(SELECT_TYPE).equals(TYPE_MEETING)) {
          strColumnValues = "";
        } else if (mapRowData.get(SELECT_TYPE).equals(TYPE_PERSON)) {
          strColumnValues=strMeetingAttendee;
        } else if (mapRowData.get(SELECT_TYPE).equals(TYPE_DECISION)) {
          strColumnValues = strDecision;
        }else if (mapRowData.get(SELECT_TYPE).equals(RELATIONSHIP_AGENDA_ITEM)) {
          strColumnValues = strAgendaItem;
        }
        vecResult.add(strColumnValues);
      }
      return vecResult;
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new MatrixException(ex);
    }
  }

  /**
   * Returns the Type for Meeting/Agenda Item/Decision/person object in
   * APPMeetingSummary
   *
   * @param context
   *            Matrix Context object
   * @param args
   *            String array
   * @return vector holding mentioned values
   * @throws Exception
   *             if operation fails
   */

  public Vector getColumnMeetingTypeData(Context context, String[] args)
      throws Exception {
    // $<attribute[attribute_MeetingDuration]>
    try {
      // Create result vector
      Vector vecResult = new Vector();
      // Get object list information from packed arguments
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      MapList objectList = (MapList) programMap.get("objectList");
      HashMap paramList  = (HashMap) programMap.get("paramList");
      String strLanguage = (String) paramList.get("languageStr");
      Map mapRowData = null;
      String strColumnValues = null;
      for (Iterator itrObjects = objectList.iterator(); itrObjects
          .hasNext();) {
        mapRowData = (Map) itrObjects.next();
        if(RELATIONSHIP_AGENDA_ITEM.equals(mapRowData.get(SELECT_TYPE)))
        {
          strColumnValues="";
        }else
        {
          strColumnValues = i18nNow.getTypeI18NString((String) mapRowData.get(SELECT_TYPE), strLanguage);
        }
        vecResult.add(strColumnValues);
      }
      return vecResult;
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new MatrixException(ex);
    }
  }

  /**
   * Returns the Object Owner value for Meeting/Agenda Item/Decision/Person
   * object in APPMeetingSummary
   *
   * @param context
   *            Matrix Context object
   * @param args
   *            String array
   * @return vector holding mentioned values
   * @throws Exception
   *             if operation fails
   */

  public Vector getColumnMeetingOwnerData(Context context, String[] args)
      throws Exception {
    try {
      // Create result vector
      Vector vecResult = new Vector();
      // Get object list information from packed arguments
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      MapList objectList = (MapList) programMap.get("objectList");
      Map mapRowData = null;
      String strColumnValues = null;
      for (Iterator itrObjects = objectList.iterator(); itrObjects
          .hasNext();) {
        mapRowData = (Map) itrObjects.next();
        strColumnValues = (String) mapRowData.get(SELECT_OWNER);
        vecResult.add(strColumnValues);
      }
      return vecResult;
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new MatrixException(ex);
    }
  }

  /**
   * Returns the Popup icon & its related links for Meeting/Agenda Item/Decision/Person object in
   * APPMeetingSummary
   *
   * @param context
   *            Matrix Context object
   * @param args
   *            String array
   * @return vector holding mentioned values
   * @throws Exception
   *             if operation fails
   */

    public Vector getColumnMeetingPopup(Context context, String[] args)
    throws Exception {

try {
      // Create result vector
    Vector vecResult = new Vector();
      // Get object list information from packed arguments
      HashMap programMap = (HashMap) JPO.unpackArgs(args);

     HashMap paramList = (HashMap) programMap.get("paramList");
    MapList objectList = (MapList) programMap.get("objectList");
    String strPrinterFriendly = (String) paramList.get("reportFormat");
    boolean isPrinterFriendly = strPrinterFriendly != null;

    StringBuffer popuplink = null;
    Map mapRowData = null;
    String strObjType = null;
    String strPopup = null;
    String strObjectId = null;
    Iterator itrObjects= objectList.iterator();

      while (itrObjects.hasNext())
        {
                mapRowData = (Map) itrObjects.next();

                if(!isPrinterFriendly)
                  {
                          popuplink = new StringBuffer();

                         strObjType = (String) mapRowData.get(SELECT_TYPE);
                      if (TYPE_MEETING.equals(strObjType)
                          || TYPE_PERSON.equals(strObjType)
                              || TYPE_DECISION.equals(strObjType))
                          {
                          strObjectId = (String) mapRowData.get(SELECT_ID);
                              popuplink
                                   .append("<a href=\"javascript:emxTableColumnLinkClick('");
                             popuplink.append(FrameworkUtil
                                    .encodeURL("emxTree.jsp?mode=replace&objectId="
                                           + strObjectId));
                               popuplink.append("','700','600','false','popup','');\" >");
                             popuplink
                                       .append("<img src=\"images/iconNewWindow.gif\" valign=\"middle\" border=\"0\" />");
                            popuplink.append("</a>");
                            }
                             else {
                            strObjectId = (String) mapRowData.get(SELECT_ID);
                            popuplink
                                    .append("<a href=\"javascript:emxTableColumnLinkClick('");
                              popuplink
                                .append(FrameworkUtil
                                    .encodeURL("emxTree.jsp?mode=replace&treeNodeKey=node.Meeting&DefaultCategory=APPMeetingAgenda&objectId="
                                               + strObjectId));
                            popuplink.append("','700','600','false','popup','');\" >");
                             popuplink
                                 .append("<img src=\"images/iconNewWindow.gif\" valign=\"middle\" border=\"0\" />");
                             popuplink.append("</a>");
                              }
                        vecResult.add(popuplink.toString());
                   }
                else
                  {
                    vecResult.add("");
                 }
          }

    return vecResult;
} catch (Exception ex) {
          ex.printStackTrace();
          throw new MatrixException(ex);
}
}

 /**
     * This will return the formatted new start date in the user preference time zone.
     * @param context
     * @return meeting start date in the user preference time zone.
     * @throws FrameworkException
     */

    private String getFormattedNewMeetingStartDate(Context context, String strNewDate, double clientTZOffset) throws FrameworkException {


        StringList formattedDisplayDateTimeList = FrameworkUtil.split(eMatrixDateFormat.getFormattedDisplayDateTime(context, strNewDate, true, DateFormat.MEDIUM, clientTZOffset, context.getLocale())," ");
        return (String)formattedDisplayDateTimeList.get(0) + " " +
               (String)formattedDisplayDateTimeList.get(1)+ " " +
               (String)formattedDisplayDateTimeList.get(2);
    }

  /**
   * Program to get the cell level access for Meeting Summary Table
   *
   * @param context
   *            the eMatrix Context object
   * @@param String array contains Meetings Ids for edit
   * @throws Matrix
   *             Exception if the operation fails
   */

  public static StringList getCellAccessforMeetingSummary(
      Context context, String args[]) throws Exception {
    HashMap inputMap = (HashMap) JPO.unpackArgs(args);

    MapList objectMap = (MapList) inputMap.get("objectList");
    StringList returnStringList = new StringList(objectMap.size());
    String curObjectID = null;
    Map curObjectMap = null;
    String strCurrent, strObjType;
    for (Iterator objectItr = objectMap.iterator(); objectItr.hasNext();) {
      curObjectMap = (Map) objectItr.next();
      curObjectID = (String) curObjectMap.get(DomainObject.SELECT_ID);
      strCurrent = (String) curObjectMap
          .get(DomainConstants.SELECT_CURRENT);
      strObjType = (String) curObjectMap.get(DomainConstants.SELECT_TYPE);
      if (TYPE_MEETING.equals(strObjType)) {
        if (strCurrent.equalsIgnoreCase(STATE_MEETING_CREATE)) {
          returnStringList.addElement(Boolean.valueOf(true));
        } else {
          returnStringList.addElement(Boolean.valueOf(false));
        }
      } else {
        returnStringList.addElement(Boolean.valueOf(false));
      }
    }
    return returnStringList;
  }

  @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
  public StringList getAgendaResponsiblePersonIncludeIDs(Context context, String[] args) throws FrameworkException {
      try {
            Map programMap = (Map) JPO.unpackArgs(args);
            String meetingObjectId = (String) programMap.get("objectId");
            DomainObject meetingObj = DomainObject.newInstance(context);
            meetingObj.setId(meetingObjectId);
            MapList attendees = meetingObj.getRelatedObjects(context,
                                DomainConstants.RELATIONSHIP_ASSIGNED_MEETINGS,  //String relPattern
                                DomainConstants.TYPE_PERSON, //String typePattern
                                new StringList(SELECT_ID),          //StringList objectSelects,
                                null,                     //StringList relationshipSelects,
                                true,                     //boolean getTo,
                                true,                     //boolean getFrom,
                                (short)1,                 //short recurseToLevel,
                                null,    //String objectWhere,
                                "",                       //String relationshipWhere,
                                0,              //Query Limit
                                null,                     //Pattern includeType,
                                null,                     //Pattern includeRelationship,
                                null);

            StringList ids = new StringList(attendees.size());
            for (int i = 0; i < attendees.size(); i++) {
                Map attendee = (Map) attendees.get(i);
                ids.add(attendee.get(SELECT_ID));
            }
            return ids;
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }
  
  public String getMeetingDate(Context context, String[] args) throws FrameworkException {
      try {
    	  Map programMap = (Map) JPO.unpackArgs(args);
    	  String strMeetingDate = null;
    	  Map requestMap = (Map) programMap.get("requestMap");
    	  String objectId = (String) requestMap.get("objectId");
    	  DomainObject meetingObj = new DomainObject(objectId);
    	  
    	  strMeetingDate = meetingObj.getAttributeValue(context, DomainConstants.ATTRIBUTE_MEETING_STARTDATETIME);
    	  int index = strMeetingDate.indexOf(" ");
    	  return strMeetingDate.substring(0, index);
    	  
      } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

}


