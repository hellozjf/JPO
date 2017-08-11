/*
 ** Copyright (c) 1999-2016 Dassault Systemes.
 ** All Rights Reserved.
 */

import java.io.PrintWriter;
import java.io.StringBufferInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import matrix.db.BusinessInterface;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.Context;
import matrix.db.IconMail;
import matrix.db.IconMailList;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.MatrixWriter;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.common.util.SubscriptionUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.AccessUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.framework.ui.UIComponent;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.Namespace;
import com.matrixone.jdom.Text;
import com.matrixone.jdom.input.SAXBuilder;
import com.matrixone.jdom.transform.JDOMResult;
import com.matrixone.jdom.transform.JDOMSource;
import com.matrixone.jsystem.util.StringUtils;
import com.matrixone.util.MxXMLUtils;


/**
 * The <code>emxIssueBase</code> class contains methods related to Issue Management.
 * @version  - AEF 10.0.5.0 Copyright (c) 2004, MatrixOne, Inc.
 * @author INFOSYS
 */
public class emxSubscriptionUtilBase_mxJPO extends emxDomainObject_mxJPO
{
        StringList filterList  = new StringList();
        public Map TYPE_EVENT_SUBSCRIBERS = new HashMap();
        static protected final String iconMail = SubscriptionUtil.ICON_MAIL;
        static protected final String email = SubscriptionUtil.EMAIL;
        static protected final String both = SubscriptionUtil.BOTH;

        private static final String OBJECT = "Object";
        private static final String RELATIONSHIP = "Relationship";

        /**
         * Create a new emxIssueBase object.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds no arguments.
         * @return a emxIssueBase object.
         * @throws Exception if the operation fails.
         * @since AEF 10.0.5.0
         */
        public emxSubscriptionUtilBase_mxJPO(Context context, String[] args) throws Exception {
                super(context, args);
        }

        /**
         * Main entry point.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds no arguments
         * @return an integer status code (0 = success)
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        public int mxMain(Context context, String[] args) throws Exception {
                return 0;
        }

        /**
         * Method to obtain all Subscribers including push and self which used in Notification Objects.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args - args contains a Map with the following entries:
         *                      ObjectId - The object Id of the context.
         * @throws Exception if the operation fails
         * @since AEF 11.0
         */
        public StringList getSubscribersList(Context context, String[] args) throws Exception
        {
                //unpacking the Arguments from variable args
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                StringList subscribersList = new StringList();
                String idType = (String) programMap.get("idType");
                String objectId = (String) programMap.get("id");
                String eventCmdName = (String) programMap.get("notificationName");
                subscribersList = SubscriptionUtil.getSubscribersList(context, objectId, eventCmdName, idType, true);

                return subscribersList;
        }

        /**
         * Method to obtain all Subscribers including push and self which used in Notification Objects.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args - args contains a Map with the following entries:
         *                      ObjectId - The object Id of the context.
         * @throws Exception if the operation fails
         * @since V6R2009-HF0
         */
        public StringList getSubscribersListForType(Context context, String[] args) throws Exception
        {
                StringList subscribersList = new StringList();
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                String objectId = (String) programMap.get("id");
                String eventCmdName = (String) programMap.get("notificationName");

                DomainObject obj = DomainObject.newInstance(context, objectId);
                String typeEventKey = obj.getInfo(context, obj.SELECT_TYPE);
                typeEventKey += "_" + eventCmdName;

                if( TYPE_EVENT_SUBSCRIBERS.containsKey(typeEventKey) )
                {
                    subscribersList = (StringList)TYPE_EVENT_SUBSCRIBERS.get(typeEventKey);
                } else {
                    //unpacking the Arguments from variable args
                    subscribersList = SubscriptionUtil.getSubscribersListByType(context, objectId, eventCmdName);
                    TYPE_EVENT_SUBSCRIBERS.put(typeEventKey, subscribersList);
                }

                return subscribersList;
        }

        /**
         * Method to obtain all self Subscribers which used in subscriptions list of an event.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args - args contains a Map with the following entries:
         *                      ObjectId - The object Id of the context.
         * @throws Exception if the operation fails
         * @since AEF 10-7
         */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getSelfSubscribersList(Context context, String[] args) throws Exception
        {
                //unpacking the Arguments from variable args
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                //Initializing the return type
                MapList subscribersList = new MapList();
                String objectId = (String) programMap.get("objectId");
                String eventCmdName = (String) programMap.get("eventCmdName");
                StringList personList = SubscriptionUtil.getSubscribersList(context, objectId, eventCmdName, false);
                if(personList != null && personList.size() > 0)
                {
                    Iterator it       = personList.iterator();
                    HashMap tempMap;
                    while (it.hasNext())
                    {
                        //Code Modified for Bug Id - 362921
                        /* Cause : personList returns result in form of Person|attribute */
						tempMap = new HashMap();
                        String str=(String) it.next();
                        StringList strUserLst = FrameworkUtil.split(str,"|");
                        tempMap.put("id", PersonUtil.getPersonObjectID(context, (String) strUserLst.get(0)));
                        subscribersList.add(tempMap);
                    }

                }
                return subscribersList;
        }

        /**
         * Method to obtain all Push Subscribers which used in subscriptions list of an event.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args - args contains a Map with the following entries:
         *                      ObjectId - The object Id of the context.
         * @throws Exception if the operation fails
         * @since AEF 10-7
         */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getPushSubscribersList(Context context, String[] args)
                throws Exception
        {
                //unpacking the Arguments from variable args
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                //Initializing the return type
                MapList subscribersList = new MapList();
                String objectId = (String) programMap.get("objectId");
                String eventName = (String) programMap.get("eventName");
                subscribersList = SubscriptionUtil.getPushSubscribersListByObject(context, objectId, eventName);
                return subscribersList;
        }

        /**
         * Get Objects for the specified criteria in Person Search.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args contains a Map with the following entries:
         *    userName      - a String of specified criteria user name
         *    lastName      - a String of specified criteria last name
         *    firstName     - a String of specified criteria first name
         *    orgId         - a String of specified criteria Organization
         *    QueryLimit    - a String of limit on the number of objects found
         * @return MapList containing objects for search result
         * @throws Exception if the operation fails
         * @since AEF 10-7
         */

        public MapList getSubscriptionPersonSearchResult(Context context , String[] args)
           throws Exception
        {

            HashMap paramMap = (HashMap)JPO.unpackArgs(args);

            //Retrieve Search criteria
            String strUserName      = (String)paramMap.get("userName");
            String strLastName      = (String)paramMap.get("lastName");
            String strFirstName     = (String)paramMap.get("firstName");
            String strOrgId         = (String)paramMap.get("orgId");

            String queryLimit = (String)paramMap.get("QueryLimit");
            if (queryLimit == null || queryLimit.equals("null") || queryLimit.equals("")){
                queryLimit = "0";
            }

            if (strUserName == null || strUserName.equalsIgnoreCase("null") || strUserName.length() <= 0)
            {
                  strUserName = "*";
            }
            if (strLastName == null || strLastName.equalsIgnoreCase("null") || strLastName.length() <= 0)
            {
                  strLastName = "*";
            }
            if (strFirstName == null || strFirstName.equalsIgnoreCase("null") || strFirstName.length() <= 0)
            {
                  strFirstName = "*";
            }
            if (strOrgId == null || strOrgId.equalsIgnoreCase("null") || strOrgId.length() <= 0)
            {
                strOrgId = "*";
            }

            String attrLastName         = DomainObject.ATTRIBUTE_LAST_NAME;
            String attrFirstName        = DomainObject.ATTRIBUTE_FIRST_NAME;
            String strRelWhere = "";

            String sMatch = "~~";
            if(!strLastName.equals("*") && !strLastName.equals(""))
            {
                if(strLastName.indexOf("*") == -1)
                {
                    if(strRelWhere.length()>0) {strRelWhere += " && ";}
                    strRelWhere += "(attribute["+attrLastName+"] == '"+strLastName+"')";
                } else {
                    if(strRelWhere.length()>0) {strRelWhere += " && ";}
                    strRelWhere += "(attribute["+attrLastName+ "] " +  sMatch + " '"+strLastName+"')";
                }
            }

            if(!strFirstName.equals("*") && !strFirstName.equals(""))
            {
                if(strFirstName.indexOf("*") == -1)
                {
                    if(strRelWhere.length()>0) {strRelWhere += " && ";}
                    strRelWhere += "(attribute["+attrFirstName+"] == '"+strFirstName+"')";
                } else {
                    if(strRelWhere.length()>0) {strRelWhere += " && ";}
                    strRelWhere += "(attribute["+attrFirstName+"] " +  sMatch + " '"+strFirstName+"')";
                }
            }

            if(!strOrgId.equals("*") && !strOrgId.equals(""))
            {
                if(strOrgId.indexOf("*") == -1)
                {
                    if(strRelWhere.length()>0) {strRelWhere += " && ";}
                    strRelWhere += "(to[" + DomainConstants.RELATIONSHIP_EMPLOYEE +"].from.name == '"+ strOrgId + "')";
                } else {
                    if(strRelWhere.length()>0) {strRelWhere += " && ";}
                    strRelWhere += "(to[" + DomainConstants.RELATIONSHIP_EMPLOYEE + "].from.name " +  sMatch + " '"+ strOrgId + "')";
                }
            }

            SelectList selectStmts = new SelectList(3);
            selectStmts.add(DomainObject.SELECT_ID);
            selectStmts.add(DomainObject.SELECT_NAME);
            selectStmts.add(DomainObject.SELECT_CURRENT);

            MapList mapList = DomainObject.findObjects(context,
                    DomainConstants.TYPE_PERSON,
                    strUserName,
                    null,
                    null,
                    "*",
                    strRelWhere,
                    null,
                    true,
                    selectStmts,
                    Short.parseShort(queryLimit));

            return mapList;
      }
     /**
      * This method returns the persons based on search criteria and  people whoc has access to the object which we are trying to do Push Subscription.
      *
      * @param context
      * @param args
      * @return
      * @throws Exception
      */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getPushSubscriptionPersonSearchResult(Context context , String[] args)
        throws Exception
     {
         //This method is added to fix 370784
         MapList searchResults = getSubscriptionPersonSearchResult(context, args);

         HashMap paramMap = (HashMap)JPO.unpackArgs(args);
         String objectId = (String)paramMap.get("objectId");
         if(objectId == null) {
            return  searchResults;
         }

         ArrayList personNames = new ArrayList();
         for (Iterator iter = searchResults.iterator(); iter.hasNext();) {
            Map personMap = (Map) iter.next();
            if(("Active").equals(personMap.get(DomainConstants.SELECT_CURRENT))){
            personNames.add(personMap.get(DomainConstants.SELECT_NAME));
         }
         }

         ArrayList accessMask = new ArrayList(1);
         accessMask.add("read");
         ArrayList personsWithoutAccess = AccessUtil.hasAccess(context, objectId, personNames, accessMask);

         MapList filteredList = new MapList();
         for (Iterator iter = searchResults.iterator(); iter.hasNext();) {
             Map personMap = (Map) iter.next();
             if((!personsWithoutAccess.contains(personMap.get(DomainConstants.SELECT_NAME)))&&("Active").equals(personMap.get(DomainConstants.SELECT_CURRENT)))
                 filteredList.add(personMap);
         }

         return filteredList;
     }

        /**
         * showCheckbox - determines if the checkbox needs to be enabled in the column of the Discussion Summary table
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - objectList MapList
         * @returns Object of type Vector
         * @throws Exception if the operation fails
         * @since Common 10-0-0-0
         * @grade 0
         */
        public Vector showCheckbox(Context context, String[] args)
            throws Exception
        {
            try
            {
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                MapList objectList = (MapList)programMap.get("objectList");
                HashMap requestMap = (HashMap)programMap.get("paramList");
                Vector enableCheckbox = new Vector();
                String user = context.getUser();
                String eventType = (String)requestMap.get("eventName");
                String objectId = (String)requestMap.get("objectId");
                Iterator objectListItr = objectList.iterator();
                while(objectListItr.hasNext())
                {
                    String enable = "true";
                    Map objectMap = (Map) objectListItr.next();
                    String strPerson = (String)objectMap.get(SELECT_NAME);
                    if(strPerson.equalsIgnoreCase(user))
                        enable = "false";
                    else if(SubscriptionUtil.checkforPushSubscription(context, objectId, eventType, strPerson))
                        enable = "false";

                    enableCheckbox.add(enable);
                }
                return enableCheckbox;
            }
            catch (Exception ex)
            {
                System.out.println("Error in showCheckbox= " + ex.getMessage());
                throw ex;
            }
        }
        /**
         * prepareBodyHTML - takes the xml string and generates the HTML formatted message
         * @param context the eMatrix <code>Context</code> object
         * @param xml - Document object containing the xml
         * @return HTML String
         * @throws Exception if the operation fails
         * @since AEF 10-7
         * @grade 0
         */
        public static String prepareBodyHTML(Context context, Document xml)
        {
            return getMessageBody(context, xml, "html");
        }
       /**
         * prepareBodyText - takes the xml string and generates the text formatted message
         * @param context the eMatrix <code>Context</code> object
         * @param xml - Document object containing the xml
         * @return text String
         * @throws Exception if the operation fails
         * @since AEF 10-7
         * @grade 0
         */
        public static String prepareBodyText(Context context, Document xml)
        {
            return getMessageBody(context, xml, "text");
        }
        /**
         * getMessageBody - takes the xml string and transforms the string againgst the proper xsl to generate the html or text message
         * @param context the eMatrix <code>Context</code> object
         * @param xml - Document object containing the xml
         * @return format - HTML or text format in which the mail has to be generated.
         * @throws Exception if the operation fails
         * @since AEF 10-7
         * @grade 0
         */
        public static String getMessageBody(Context context, Document xml, String format)
        {
            String returnString = "";
            try
            {
                MQLCommand mql = new MQLCommand();
                mql.open(context);
                if("html".equalsIgnoreCase(format))
                {
                    mql.executeCommand(context, "print program MessageBodyHTML.xsl select code dump");
                }
                else
                {
                    mql.executeCommand(context, "print program MessageBodyText.xsl select code dump");
                }
                mql.close(context);
               Transformer transformer = TransformerFactory.newInstance()
                                     .newTransformer(new StreamSource(new StringBufferInputStream(mql.getResult())));
               JDOMSource in = new JDOMSource(xml);
               JDOMResult out = new JDOMResult();
               transformer.transform(in, out);
                if("html".equalsIgnoreCase(format))
                {
                    returnString = (MxXMLUtils.getOutputter()).outputString(out.getDocument());
                }
                else
                {
                    returnString = out.getDocument().getRootElement().getText();
                }
            }
            catch(Exception e)
            {
                returnString = e.toString();
            }
            return returnString;
        }

        /**
         * setElementContent - takes the xml string and transforms the string againgst the proper xsl to generate the html or text message
         * @param context the eMatrix <code>Context</code> object
         * @param elmt - Element which is a section of the message
         * @return text - the text of the section
         * @throws Exception if the operation fails
         * @since AEF 10-7
         * @grade 0
         */
        private static void setElementContent(Context context, Element elmt, String text) throws Exception
        {
            String xmlText = "<mxRoot>" + text + "</mxRoot>";
            SAXBuilder builder = new SAXBuilder();
            builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
            builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            Document doc  = builder.build(new java.io.CharArrayReader(xmlText.toCharArray()));
            List content = doc.getRootElement().getContent();
            for (int i = 0; i < content.size(); i++)
            {
                try {
                    Text textElement = (Text)content.get(i);
                    elmt.addContent((Text)textElement.clone());
                } catch (Exception ex) {
                }
                try {
                    Element element = (Element)content.get(i);
                    elmt.addContent((Element)element.clone());
                } catch (Exception ex) {

                }
            }
        }
          /**
         * prepareMailXML - takes the information about the different sections of the mail message and forms the html or text message
         * @param context the eMatrix <code>Context</code> object
         * @param headerInfo - Map containing information about the heading section
         * @param bodyInfo  - Map containing information about the body section
         * @param footerInfo  - Map containing information about the footer section
         * @return Document - xml Document with different sections
         * @throws Exception if the operation fails
         * @since AEF 10-7
         * @grade 0
         */
        public static Document prepareMailXML(Context context, Map headerInfo, Map bodyInfo, Map footerInfo) throws Exception
        {
            // aef namespace
            Namespace aef = Namespace.getNamespace("aef", "http://www.matrixone.com/aef");

            // root element
            Element mxRoot = new Element("mxRoot", aef);
            com.matrixone.jdom.Document doc = new com.matrixone.jdom.Document(mxRoot);

            // header data
            if (headerInfo != null && headerInfo.size() > 0)
            {
                Element headerData = new Element("headerData", aef);
                mxRoot.addContent(headerData);
                String headerText = (String)headerInfo.get("header");
                if (headerText != null)
                {
                    Element header = new Element("header", aef);
                    setElementContent(context, header, headerText);
                    headerData.addContent(header);
                }
                String creatorText = (String)headerInfo.get("creatorText");
                if (creatorText != null)
                {
                    Element creator = new Element("creatorText", aef);
                    setElementContent(context, creator, creatorText);
                    headerData.addContent(creator);
                }
            }
            // body data
            if (bodyInfo != null && bodyInfo.size() > 0)
            {
                Element bodyData = new Element("bodyData", aef);
                mxRoot.addContent(bodyData);
                Element sections = new Element("sections", aef);
                bodyData.addContent(sections);

                Iterator i = bodyInfo.keySet().iterator();
                while (i.hasNext())
                {
                    String sectionName = (String)i.next();
                    Element section = new Element("section", aef);
                    sections.addContent(section);
                    Element sectionHeader = new Element("sectionHeader", aef);
                    setElementContent(context, sectionHeader, sectionName);
                    section.addContent(sectionHeader);
                    Element fields = new Element("fields", aef);
                    section.addContent(fields);
                    Map sectionInfo = (Map)bodyInfo.get(sectionName);
                    if (sectionInfo != null && sectionInfo.size() > 0)
                    {
                        Iterator ii = sectionInfo.keySet().iterator();
                        while (ii.hasNext())
                        {
                            Element field = new Element("field", aef);
                            fields.addContent(field);
                            String labelText = (String)ii.next();
                            Element label = new Element("label", aef);
                            setElementContent(context, label, labelText);
                            field.addContent(label);
                            String valueText = (String)sectionInfo.get(labelText);
                            Element value = new Element("value", aef);
                            setElementContent(context, value, XSSUtil.encodeForXML(context, valueText));
                            field.addContent(value);
                        }
                    }
                }
            }

            // footer data
            if (footerInfo != null && footerInfo.size() > 0)
            {

                Element footerData = new Element("footerData", aef);
                mxRoot.addContent(footerData);
                Element dataLines = new Element("dataLines", aef);
                footerData.addContent(dataLines);

                List dataLineList = (List)footerInfo.get("dataLines");
                for (int i = 0; dataLineList != null && i < dataLineList.size() ; i++)
                {
                    String dataLineText = (String)dataLineList.get(i);
                    Element dataLine = new Element("dataLine", aef);
                    setElementContent(context, dataLine, dataLineText);
                    dataLines.addContent(dataLine);
                }
                String signatureText = (String)headerInfo.get("signature");
                if (signatureText != null)
                {
                    Element signature = new Element("signature", aef);
                    setElementContent(context, signature, signatureText);
                    footerData.addContent(signature);
                }
            }
            return doc;
        }

    /**
     * Gets all the notifications (Icon Mails) matching the criteria passed as the arguments. Icon mails are filterd based
     * on Type Pattern (Type of the object attached), Event Pattern (Event that generated this mail), From Date and To Date
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - Type Pattern - Type pattern. To filter the icon mails based on the type of the objects attached.
     *                           It accepts comma ',' separated type names e.g. "ECR,Part" and . '*' and '' to get all events.
     *        1 - Event Pattern - Event pattern. To filter the icon mails based on the Events for which they are generated.
     *                            It accepts comma ',' separated type names e.g. "ECR,Part" and . '*' and '' to get all events.
     *        2 - From Date - Date string, of format "MM/DD/YYYY"
     *        3 - To Date - Date string, of format "MM/DD/YYYY"
     *        4 - canDelete - String with true/false. true - Delete the added icon mails. False - Do not delete
     * @returns MapList List of the map containing the information of the Iconmails
     * @throws MatrixException if   the operation fails
     * @since Common X2
     * @grade 0
     */
    public MapList getTypeNotifications(Context context, String[] args) throws MatrixException
    {
        if(args.length<5)
        {
            String errMsg = "";
            try
            {
                errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Common.GetNotificationWorongParameters");
            }catch(Exception e){
                throw new MatrixException(e);
            }
            throw new MatrixException(errMsg);
        }
        return getNotifications(context, args[0], args[1], args[2], args[3], args[4], false, OBJECT);
    }

    /**
     * Gets all the notifications (Icon Mails) matching the criteria passed as the arguments. Icon mails are filterd based
     * on Object Id, Event Pattern (Event that generated this mail), From Date and To Date
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - Object Id - Type pattern. To filter the icon mails based on the object id attached.
     *                           It accepts comma ',' separated type names e.g. "ECR,Part" and . '*' and '' to get all events.
     *        1 - Event Pattern - Event pattern. To filter the icon mails based on the Events for which they are generated.
     *                            It accepts comma ',' separated type names e.g. "ECR,Part" and . '*' and '' to get all events.
     *        2 - From Date - Date string, of format "MM/DD/YYYY"
     *        3 - To Date - Date string, of format "MM/DD/YYYY"
     *        4 - canDelete - String with true/false. true - Delete the added icon mails. False - Do not delete
     * @returns MapList List of the map containing the information of the Iconmails
     * @throws MatrixException if   the operation fails
     * @since Common vX2
     * @grade 0
     */
    public MapList getObjectNotifications(Context context, String[] args) throws MatrixException
    {
        if(args.length<5)
        {
            String errMsg = "";
            try
            {
                errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Common.GetNotificationWorongParameters");
            }catch(Exception e){
                throw new MatrixException(e);
            }
            throw new MatrixException(errMsg);
        }
        return getNotifications(context, args[0], args[1], args[2], args[3], args[4], true, OBJECT);
    }

    /**
     * Gets all the notifications (Icon Mails) matching the criteria passed as the arguments. Icon mails are filterd based
     * on Type Pattern, Event Pattern, Start Date and End Date
     *
     * @param context the eMatrix <code>Context</code> object
     * @param typePatternOrOID - Type pattern or Object Id string. It accepts comma ',' separated type names e.g. "ECR,Part" and . '*'
     *                           and '' to get all events (or) Object ID, this will be distinguished based on the forObject boolean argument.
     * @param eventPattern - Event pattern. To filter the icon mails based on the Events for which they are generated.
     *                       It accepts comma ',' separated type names e.g. "Part Deleted,Part Revised" and . '*' and '' to get all events.
     * @param fromDate - Date string, of format "MM/DD/YYYY"
     * @param toDate - Date string, of format "MM/DD/YYYY"
     * @param canDelete - String argument that is true if user wants to delete the added icon mail,
     *                    false if user doesnt want the added icon mail to be deleted
     * @param forObject - A boolena argument that desides whether typePatternOrOID argument contains type pattern or object id
     *                    typePatternOrOID has Object Id if true
     *                    typePatternOrOID has Type Pattern if false
     *
     * @returns MapList List of the map containing the information of the Iconmails
     * @throws MatrixException if   the operation fails
     * @since Common vX2
     * @grade 0
     *
     * @deprecated Use <code>public MapList getNotifications(Context context, String typePatternOrOID, String eventPattern,
                        String fromDate, String toDate, String canDelete, boolean forObject, String idType) throws MatrixException</code> method

     */

    public MapList getNotifications(Context context, String typePatternOrOID, String eventPattern, String fromDate, String toDate, String canDelete, boolean forObject) throws MatrixException
    {
        return getNotifications(context, typePatternOrOID, eventPattern, fromDate, toDate, canDelete, forObject, OBJECT);
    }

    /**
     * Gets all the notifications (Icon Mails) matching the criteria passed as the arguments. Icon mails are filterd based
     * on Type Pattern, Event Pattern, Start Date and End Date
     *
     * @param context the eMatrix <code>Context</code> object
     * @param typePatternOrOID - Type pattern or Object Id string. It accepts comma ',' separated type names e.g. "ECR,Part" and . '*'
     *                           and '' to get all events (or) Object ID, this will be distinguished based on the forObject boolean argument.
     * @param eventPattern - Event pattern. To filter the icon mails based on the Events for which they are generated.
     *                       It accepts comma ',' separated type names e.g. "Part Deleted,Part Revised" and . '*' and '' to get all events.
     * @param fromDate - Date string, of format "MM/DD/YYYY"
     * @param toDate - Date string, of format "MM/DD/YYYY"
     * @param canDelete - String argument that is true if user wants to delete the added icon mail,
     *                    false if user doesnt want the added icon mail to be deleted
     * @param forObject - A boolena argument that desides whether typePatternOrOID argument contains type pattern or object id
     *                    typePatternOrOID has Object Id if true
     *                    typePatternOrOID has Type Pattern if false
     * @param idType - String contains Relationship or Object to identify the arg typePatternOrOID is Interface/Relationship or Type/Object
     *
     * @returns MapList List of the map containing the information of the Iconmails
     * @throws MatrixException if   the operation fails
     * @since Common V6R2009x
     * @grade 0
     */
    public MapList getNotifications(Context context, String typePatternOrOID, String eventPattern,
                        String fromDate, String toDate, String canDelete, boolean forObject, String idType) throws MatrixException
    {
        PrintWriter pw = new PrintWriter(new MatrixWriter(context));

        String strObjectId = null;
        String strTypePattern = null;
        String strEventPattern = eventPattern;
        String strStartDate = fromDate; // date format should be mm/dd/yyyy
        String strEndDate = toDate; // date format should be mm/dd/yyyy
        String errMsg = "";
        String lang = (String)context.getSession().getLanguage();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        Date currentDate = new Date();
        Date startDate= null;
        Date endDate= null;

        MapList retIconMailList = new MapList();

        strEventPattern = (("".equals(strEventPattern.trim()) || strEventPattern == null)?"*":strEventPattern);
        String strTypePatternOrOIDPattern = (("".equals(typePatternOrOID.trim()) || typePatternOrOID == null)?"*":typePatternOrOID);

        if(forObject)
        {
            strObjectId = strTypePatternOrOIDPattern;
            if(strObjectId == null || "".equals(strObjectId.trim()))
            {
                pw.println("Log - Exception - null or empty object id");
            try
            {
                errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Common.EmptyObjectId");
            }catch(Exception e){
                throw new MatrixException(e);
            }
                throw new MatrixException(errMsg);
            }

        }
        else
        {
            strTypePattern = strTypePatternOrOIDPattern;
            //if typePattern is empty then make it * to match all the types as * is wild card character
            strTypePattern = StringUtils.replaceAll(strTypePattern,"\\s+,\\s+",","); //replace all the occurrences of " , " or " ," or ", " with ","
            //strTypePattern = strTypePattern.replaceAll("\\s+,\\s+",","); //replace all the occurrences of " , " or " ," or ", " with ","
            pw.println("Log - strTypePattern...."+strTypePattern);
        }



        if(strEndDate!=null && !"".equals(strEndDate.trim()))
        {
            try
            {
                endDate = sdf.parse(strEndDate);
                //Validation - End date should not be greater than current date i.e future dates are not allowed
                if(endDate.compareTo(currentDate)>0)
                {
                    pw.println("Log - Exception - End date is greater than current date");
                try
                {
                    errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Common.NotificationDateException");
                }catch(Exception e){
                    throw new MatrixException(e);
                }
                    throw new MatrixException(errMsg);
                }
            }
            catch(ParseException pe)
            {
                pw.println("Log - Exception - Unable to parse End Date - Allowed format: MM/DD/YYYY");
                try
                {
                    errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Common.AllowedDateFormat");
                }catch(Exception e){
                    throw new MatrixException(e);
                }
                throw new MatrixException(errMsg);
            }
        }
        else
        {
            //If end date is empty then take current date as end date
            endDate = currentDate;
        }

        /*As we are not capturing the time stamp, date will take 00 hrs time i.e. 06/07/2007 00:00:00 thats why we are incrementing date by 1 day and so that it will get all the
        notifications generated before 06/08/2007 00:00:00 i.e. 06/07/2007 23:59:59*/

        endDate.setDate(endDate.getDate()+1);

        if(strStartDate!=null && !"".equals(strStartDate.trim()))
        {
            try{
                startDate = sdf.parse(strStartDate);
                //Validation - Start date should not be greater than current date i.e future dates are not allowed
                if(startDate.compareTo(currentDate)>0)
                {
                    pw.println("Log - Exception - Start date is greater than current date");
                try
                {
                    errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Common.DateValidation");
                }catch(Exception e){
                    throw new MatrixException(e);
                }
                    throw new MatrixException(errMsg);
                }
                else if(startDate.compareTo(endDate)>0) //Validation - Start date should not be greater than end date
                {
                    pw.println("Log - Exception - Start date is greater than end date");
                try
                {
                    errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Common.DateValidation1");
                }catch(Exception e){
                    throw new MatrixException(e);
                }
                    throw new MatrixException(ComponentsUtil.i18nStringNow("emxComponents.SubscriptionUtilBase.ErrorMsg", lang));
                }
            }
            catch(ParseException pe)
            {
                pw.println("Log - Exception - Unable to parse End Date - Allowed format: MM/DD/YYYY");
                try
                {
                    errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Common.AllowedDateFormat");
                }catch(Exception e){
                    throw new MatrixException(e);
                }
                throw new MatrixException(errMsg);
            }
        }
        pw.println("Query Criteria - \n\tType Pattern: " + strTypePattern+"\n\tEvent Pattern: "+strEventPattern+"\n\tStart Date: "+startDate+"\n\t End Date: "+endDate);

        try
        {
            IconMail userIconMail = null;

            pw.println("Log - Getting Icon Mails....");
            //Get all icon mails for the context user - It might be a performance issue but there is no option to filter the icon mails
            IconMailList userIconMailList = IconMail.getMail(context);

            if(userIconMailList!=null && userIconMailList.size()>0)
            {
                Iterator itrUserIconMail = userIconMailList.iterator();

                if(itrUserIconMail!=null)
                {

                    String iconMailSubject = null;
                    String attachedObjetId = null;
                    String attachedObjetType = null;
                    String attachedObjetName = null;
                    String attachedObjeRev = null;
                    long iconMailNumber = -1;

                    Date iconMailDate = null;
                    StringBuffer mqlQuery = new StringBuffer();

                    boolean iconMailAdded = false;
                    boolean busObjDeletedFormMail = false;

                    pw.println("Log - Processing Icon Mails....");
                    while(itrUserIconMail.hasNext())
                    {
                        try
                        {
                            iconMailAdded = false;
                            busObjDeletedFormMail = false;
                            userIconMail = (IconMail) itrUserIconMail.next();
                            userIconMail.open(context,null);
                            iconMailSubject = userIconMail.getSubject();
                            iconMailDate = new Date(Date.parse(userIconMail.getDate()));
                            iconMailNumber = userIconMail.getNumber();
                            /*check, if Icon mail subject matches the event pattern as subject contains Event Name and if it is
                            sent between start date and end date*/
                            if(("*".equals(strEventPattern) || strEventPattern.indexOf(iconMailSubject)!=-1)
                                && (startDate == null || iconMailDate.compareTo(startDate)>=0) && iconMailDate.compareTo(endDate)<0)
                            {
                                HashMap iconMailInfo = null;

                                /* Validate if the incoming id is for object or relationship.
                                 * if it is for relationship then we can't find the objects attached in IconMail.
                                 * Get the rel id from the Message Body of the Mail.
                                 */



                                if(idType.equals(OBJECT))
                                {
                                    //Check if icon mail has a business object as an attachment
                                    if(userIconMail.hasObjects())
                                    {
                                        //get all the objects attached
                                        BusinessObjectList attachedBusObjList = userIconMail.getObjects();

                                        if(attachedBusObjList!=null && attachedBusObjList.size()>0)
                                        {
                                            Iterator itrAttachedBusObject = attachedBusObjList.iterator();
                                            if(itrAttachedBusObject != null)
                                            {
                                                while(itrAttachedBusObject.hasNext())
                                                {
                                                    BusinessObject attachedObject = (BusinessObject) itrAttachedBusObject.next();
                                                    attachedObjetType = attachedObject.getTypeName();
                                                    attachedObjetId = attachedObject.getObjectId();
                                                    attachedObjetName = attachedObject.getName();
                                                    attachedObjeRev = attachedObject.getRevision();
													//Added iconMailNumber for 363532
                                                    if("*".equals(strTypePatternOrOIDPattern) || (forObject && attachedObjetId.equals(strObjectId))||(!forObject || strTypePattern.indexOf(attachedObjetType)!=-1))
                                                    {
                                                        iconMailInfo = new HashMap(9);
                                                        iconMailInfo.put("From", userIconMail.getFrom());
                                                        iconMailInfo.put("Subject", userIconMail.getSubject());
                                                        iconMailInfo.put("Date", userIconMail.getDate());
                                                        iconMailInfo.put("ReadStatus", Boolean.valueOf(userIconMail.isRead()));
                                                        iconMailInfo.put("Message", userIconMail.getMessage());
                                                        iconMailInfo.put("MailNumber",new Long(iconMailNumber));
                                                        iconMailInfo.put("Type", attachedObjetType);
                                                        iconMailInfo.put("Name", attachedObjetName);
                                                        iconMailInfo.put("Rev", attachedObjeRev);
                                                        retIconMailList.add(iconMailInfo);
                                                        iconMailAdded = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        else if("*".equals(strTypePatternOrOIDPattern))
                                        {
                                            /*If an icon mail has attached bus object and is deleted from it then userIconMail.hasObjects() will
                                              retun true even though there are no objects remaining in attachements. So the following boolean variable is used to handle such situations */
                                            busObjDeletedFormMail = true;
                                        }

                                    }
                                    if(iconMailInfo == null && ("*".equals(strTypePatternOrOIDPattern) || busObjDeletedFormMail))
                                    {
                                        iconMailInfo = new HashMap(9);
                                        iconMailInfo.put("From", userIconMail.getFrom());
                                        iconMailInfo.put("Subject", userIconMail.getSubject());
                                        iconMailInfo.put("Date", userIconMail.getDate());
                                        iconMailInfo.put("ReadStatus", Boolean.valueOf(userIconMail.isRead()));
                                        iconMailInfo.put("Message", userIconMail.getMessage());
                                        iconMailInfo.put("MailNumber",new Long(iconMailNumber));	//Added iconMailNumber for 363532
                                        iconMailInfo.put("Type", attachedObjetType);
                                        iconMailInfo.put("Name", attachedObjetName);
                                        iconMailInfo.put("Rev", attachedObjeRev);
                                        retIconMailList.add(iconMailInfo);
                                        iconMailAdded = true;
                                    }
                              }
                              else
                              {
                                String strMessage = userIconMail.getMessage();
                                // Getting the Icon Mail message and validate if url parameter contains the relationshipID. If exists
                                // then get the associated interface with the connection id and match with the interface name passed.
                               // if(strMessage.indexOf("emxNavigator.jsp?relationshipID=") != -1)
                                if(strMessage.indexOf("relationshipID=") != -1)
                                {
                                    String strRelId = (strMessage.substring(strMessage.lastIndexOf("=")+1)).trim();
                                    String relName = SubscriptionUtil.getTypeNameFromId(context, SubscriptionUtil.ADMIN_RELATIONSHIP_TYPE, strRelId);
                                    if("*".equals(strTypePatternOrOIDPattern) || (forObject && strRelId.equals(strObjectId)) || (!forObject && (relName != null || !"".equals(relName))&& strTypePattern.indexOf(relName)!= -1))
                                    {
                                        iconMailInfo = new HashMap(6);
                                        iconMailInfo.put("From", userIconMail.getFrom());
                                        iconMailInfo.put("Subject", userIconMail.getSubject());
                                        iconMailInfo.put("Date", userIconMail.getDate());
                                        iconMailInfo.put("ReadStatus", Boolean.valueOf(userIconMail.isRead()));
                                        iconMailInfo.put("Message", userIconMail.getMessage());
                                        iconMailInfo.put("MailNumber",new Long(iconMailNumber));    //Added iconMailNumber for 363532
                                        //iconMailInfo.put("Type", attachedObjetType);
                                        //iconMailInfo.put("Name", attachedObjetName);
                                        //iconMailInfo.put("Rev", attachedObjeRev);
                                        retIconMailList.add(iconMailInfo);
                                        iconMailAdded = true;
                                    }
                                }
                              }


                            }
                            userIconMail.close(context);
                            //Once the icon mail is added to returnIconMailList and if the icon mail is opted for deletion, Delete the icon mail from the matrix database.
                            if(iconMailAdded && "true".equalsIgnoreCase(canDelete))
                            {
                                mqlQuery.setLength(0);
                                mqlQuery.append("delete mail $1");
                                try
                                {
                                    MqlUtil.mqlCommand(context, mqlQuery.toString(),Long.toString(iconMailNumber));
                                }
                                catch(FrameworkException fe)
                                {
                                    pw.println("Log - Unable to delete the IconMail with number: "+iconMailNumber);
                                }
                            }
                        }
                        catch(FrameworkException fexp)
                        {
                            pw.println("Exception: "+fexp);
                        }
                    }
                }
                else
                {
                    pw.println("Log - No Icon Mails found....");
                }
            }
            pw.println("Log - End of Processing Icon Mails....");
            return retIconMailList;
        }catch(Exception e)
        {
            e.printStackTrace(pw);
            throw new MatrixException(e);
        }
    }

    /**
     * Gets all subscribable events configured on the given type
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - Type of the Object
     * @returns MapList - List of the Maps containing the information of all the commands configured on the type
     * @throws MatrixException if the operation fails
     * @since Common vX2
     * @grade 0
     */
    public static MapList getTypeSubscribableEvents(Context context, String args[]) throws MatrixException
    {

        if(args.length>0) {
            try {
                return SubscriptionUtil.getSubscribableEventsList(context, SubscriptionUtil.ADMIN_BUSINESS_TYPE, args[0]);
            } catch (Exception e) {
                throw new MatrixException(e);
            }
        }
        return null;
    }

    /**
     * Gets all subscribable events configured on the given type
     *
     * @param context the eMatrix <code>Context</code> object
     * @param typeName Type of the object
     * @returns MapList - List of the Maps containing the information of all the commands configured on the type
     * @throws MatrixException if the operation fails
     * @since Common vX2
     */
    protected static MapList getTypeSubscribableEvents(Context context, String typeName) throws MatrixException
    {
        try {
            return SubscriptionUtil.getSubscribableEventsList(context, SubscriptionUtil.ADMIN_BUSINESS_TYPE, typeName);
        } catch (Exception e) {
            throw new MatrixException(e);
        }
    }

    /**
    * Allows user to subscribe for a global event on a type. Creates subscription object model for the context user,
    * i.e it creates Publish Subscribe object with symbolic name of the type passed as its name and then creates and
    * connects Event object to it and then Event object is connected to context user
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *          0 Type
    *          1  Event Name
    *          2  Notification Type
    * @returns void
    * @throws MatrixException if the operation fails
    * @since Common vX2
    */
    public void subscribeOnType(Context context, String [] args) throws MatrixException
    {
        try {
            checkArgsForCreateDeleteSubscription(context, args, true, true, SubscriptionUtil.ADMIN_BUSINESS_TYPE);
            createSubscription(context, true, OBJECT, args[0], args[1], args.length > 2 ? args[2] : null);
        } catch (Exception e) {
            throw new MatrixException(e);
        }
    }

    /**
    * Allows user to subscribe for a global event on a type of the object passed and if event is not global
    * then subscribes on that particular object. Creates subscription object model for the context user,
    * i.e it creates Publish Subscribe object with symbolic name of the type passed as its name and then
    * creates and connects Event object to it and then Event object is connected to context user
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *          0  Object id
    *          1  Event Name
    *          2  Notification Type
    * @returns void
    * @throws MatrixException if the operation fails
    * @since Common x+2
    * @grade 0
    */
    public void subscribeOnObject(Context context, String [] args) throws MatrixException
    {
        try {
            checkArgsForCreateDeleteSubscription(context, args, true, false, SubscriptionUtil.ADMIN_BUSINESS_TYPE);
            createSubscription(context, false, OBJECT, args[0], args[1], args.length > 2 ? args[2] : null);
        } catch (Exception e) {
            throw new MatrixException(e);
        }
    }


    /**
    * createSubscription - This method will take care of creating the Subscription object
    * @param context the eMatrix <code>Context</code> object
    * @param typeOrObjId - Type or Object Id string. this will be distinguished based on the forObject boolean
    *                      argument.
    * @param eventName - Event Name
    * @param notificationType - Notification Type
    * @param forObject - Used to determine if typeOrObjId contains Type or Object Id
    *                    true - typeOrObjId is objectId
    *                    false - typeOrObjId is type of the object
    * @returns void
    * @throws MatrixException if the operation fails
    * @since Common vX2
    *
    * @deprecated Use <code>
    * protected void createSubscription(Context context, boolean globalSubscription, String idType, String id, String eventType, String notificationType) throws Exception
    * </code> method.
    *
    */
    public void createSubscription(Context context, String typeOrObjId, String eventName, String notificationType, boolean forObject) throws MatrixException
    {
        try {
            createSubscription(context, !forObject, OBJECT, typeOrObjId, eventName, notificationType);
        } catch (Exception e) {
            throw new MatrixException(e);
        }
    }

    /**
    * Delets global events subscription on the type or non global events on object
    * Deletes the subscribed event for the context user, i.e disconnect the context user from the event and if the event dont have
    * any subscribers the delete the Event object
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 objectid of the Object
    *        1 Event Name pattern
    *        2 Type of the notification, which user wants to unsubscribe for. Notification Types are Email, IconMail, Both
    * @returns void
    * @throws MatrixException if the operation fails
    * @since Common vX2
    */
    public void deleteSubscriptionOnObject(Context context, String [] args) throws MatrixException
    {
        try {
            checkArgsForCreateDeleteSubscription(context, args, false, false, SubscriptionUtil.ADMIN_BUSINESS_TYPE);
            unSubscribeEvents(context, args[0], args[1], true, args.length > 2 ? args[2] : null, OBJECT);
        } catch (Exception e) {
            throw new MatrixException(e);
        }
    }

    /**
    * Delets global events subscription on the type
    * Deletes the subscribed event for the context user, i.e disconnect the context user from the event and if the event dont have
    * any subscribers the delete the Event object
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 objectid of the Object
    *        1 Event Name Pattern
    *        2 Type of the notification, which user wants to unsubscribe for. Notification Types are Email, IconMail, Both
    * @returns void
    * @throws MatrixException if the operation fails
    * @since Common vX2
    */
    public void deleteSubscriptionOnType(Context context, String [] args) throws MatrixException
    {
        try {
            checkArgsForCreateDeleteSubscription(context, args, true, false, SubscriptionUtil.ADMIN_BUSINESS_TYPE);
            unSubscribeEvents(context, args[0], args[1], false, args.length > 2 ? args[2] : null, OBJECT);
        } catch (Exception e) {
            throw new MatrixException(e);
        }
    }


    /**
    * Deletes the subscription for global events on the type or nonglobal events on the object
    * @param context the eMatrix <code>Context</code> object
    * @param typeOrObjId - Type or Object Id string. this will be distinguished based on the forObject boolean argument.
    * @param eventPattern - Event pattern. To filter the icon mails based on the Events for which they are generated.
     *                       It accepts comma ',' separated type names e.g. "Part Deleted,Part Revised" and . '*' and '' to get all events.
    * @param notificationType - Notification Type
    * @param forObject - Used to determine if typeOrObjId contains Type or Object Id
    *                    true - typeOrObjId has objectId
    *                    false - typeOrObjId has type of the object
    * @param notificationType - Type of the notification, which user wants to unsubscribe for. Notification Types are Email, IconMail, Both
    * @returns void
    * @throws MatrixException if the operation fails
    * @since Common vX2
    * @deprecated for unSubscribeEvents use unSubscribeEvents method with idType
    * <code>
    *    protected void unSubscribeEvents(Context context, String typeOrObjId, String eventPattern, boolean forObject, String notificationType, String idType) throws MatrixException
    * </code>
    *
    */
    public void unSubscribeEvents(Context context, String typeOrObjId, String eventPattern, boolean forObject, String notificationType) throws MatrixException
    {
        try {
            unSubscribeEvents(context, typeOrObjId, eventPattern, forObject, notificationType, OBJECT);
        } catch (Exception e) {
            throw new MatrixException(e);
        }
    }


    /**
    * Deletes the subscription for global events on the type/interface or nonglobal events on the object
    * @param context the eMatrix <code>Context</code> object
    * @param typeOrObjId - Type or Object Id string. this will be distinguished based on the forObject boolean argument.
    * @param eventPattern - Event pattern. To filter the icon mails based on the Events for which they are generated.
     *                       It accepts comma ',' separated type names e.g. "Part Deleted,Part Revised" and . '*' and '' to get all events.
    * @param notificationType - Notification Type
    * @param forObject - Used to determine if typeOrObjId contains Type or Object Id
    *                    true - typeOrObjId has objectId
    *                    false - typeOrObjId has type of the object
    * @param notificationType - Type of the notification, which user wants to unsubscribe for. Notification Types are Email, IconMail, Both
    * @param idType - String contains Relationship or Object to identify the arg typePatternOrOID is Relationship/Type or Object or Rel Id
    * @returns void
    * @throws MatrixException if the operation fails
    * @since Common V6R2009x
    */

    protected void unSubscribeEvents(Context context, String typeOrObjId, String eventPattern, boolean forObject, String notificationType, String idType) throws MatrixException
    {
        try {
            SubscriptionUtil.deleteSubscriptions(context, !forObject, getAdminObjTypeFromIdType(idType), typeOrObjId, eventPattern, notificationType);
        } catch (Exception e) {
            throw new MatrixException(e);
        }
    }

    /**
    * Disconnects the person from the event object and deletes event object if the disconnected person is the last subscriber
    *
    * @param context the eMatrix <code>Context</code> object
    * @param strPubSubObjId - Publish Subscribe object id
    * @param strEventPattern - Event pattern. To filter the icon mails based on the Events for which they are generated.
     *                       It accepts comma ',' separated type names e.g. "Part Deleted,Part Revised" and . '*' and '' to get all events.
    * @param eventMenuCommands - Maplist containing event command information (A MapList returned by UIMenu.getMenu() method)
    * @param forObject - Used to determine whether to unsubscribe on type or object
    *                    true - Unsubscribe on Object
    *                    false - Unsubscribe on Type
    * @param notificationType - Type of the notification, which user wants to unsubscribe for. Notification Types are Email, IconMail, Both
    * @returns void
    * @throws MatrixException if the operation fails
    * @since Common vX2
    *
    * @deprecated for delete subscriptions use
    * <code>
    *    protected void deleteSubscriptions(Context context, boolean globalSubscription, String adminObjType, String id, String eventPattern, String notificationType) throws Exception
    * </code>
    * this method will disconnect the event from Event object.
    */
    public void disconnectEvents(Context context, String strPubSubObjId, String strEventPattern, MapList eventMenuCommands , boolean forObject, String notificationType) throws MatrixException
    {
        PrintWriter mw = new PrintWriter(new MatrixWriter(context));
        String userName = context.getUser();
        DomainObject objPubSub = new DomainObject();
        objPubSub.setId(strPubSubObjId);

        SelectList selectStmts    = new SelectList();
        int eventDeleteCounter = 0;

        selectStmts.addElement(DomainConstants.SELECT_SUBSCRIBED_PERSON_NAME);
        selectStmts.addElement(DomainConstants.SELECT_SUBSCRIBED_PERSON_REL_ID);
        selectStmts.addElement(DomainConstants.SELECT_ID);
        selectStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_EVENT_TYPE);
        selectStmts.addElement(DomainConstants.SELECT_SUBSCRIBED_PERSON_ATTRIBUTE_NOTIFICATION_TYPE);



        StringBuffer whereClause = new StringBuffer();
        whereClause.append(DomainConstants.SELECT_ATTRIBUTE_EVENT_TYPE);
        whereClause.append(" matchlist const '");
        whereClause.append(strEventPattern); //matching with unsubscribe evnet list
        whereClause.append("' ','");

        // getting all the events list matching the list of the event for which user wants to unsubscribe
        MapList psEventList = objPubSub.getRelatedObjects(context,
            DomainConstants.RELATIONSHIP_PUBLISH,
            DomainConstants.TYPE_EVENT,
            selectStmts,
            null,
            false,
            true,
            (short) 1,
            whereClause.toString(),
            null,
            null,
            null,
            null);
        if(psEventList !=null && psEventList.size()>0)
        {
            Iterator itrEventList = psEventList.iterator();
            Map eventObjMap = null;
            String strIsGlobal = null;
            Map selectedEvent = null;
            StringList personList = null;
            String personNotificationType = null;

            while(itrEventList.hasNext())
            {
                eventObjMap = (Map)itrEventList.next();
                //getting Person list form the map

                String subsPersonRelId = null;
                int personIndex = -1;
                boolean personDisconnected = false;


                Object subscribersList = eventObjMap.get(DomainConstants.SELECT_SUBSCRIBED_PERSON_NAME);
                if(subscribersList != null)
                {

                    if(subscribersList instanceof StringList)
                    {
                        personList = (StringList)subscribersList;
                        personIndex = personList.indexOf(userName);
                        if(personIndex>-1)
                        {
                            StringList relidList = (StringList)eventObjMap.get(DomainConstants.SELECT_SUBSCRIBED_PERSON_REL_ID);
                            //added for the bug no 349465
                            StringList notifyList = (StringList)eventObjMap.get(DomainConstants.SELECT_SUBSCRIBED_PERSON_ATTRIBUTE_NOTIFICATION_TYPE);
                            subsPersonRelId = (String)relidList.get(personIndex);
                            personNotificationType = (String)notifyList.get(personIndex);

                        }

                    }
                    else
                    {
                        if(userName.equals((String)subscribersList))
                        {
                            subsPersonRelId = (String)eventObjMap.get(DomainConstants.SELECT_SUBSCRIBED_PERSON_REL_ID);
                            //added for the bug no 349465
                             personNotificationType = (String)eventObjMap.get(DomainConstants.SELECT_SUBSCRIBED_PERSON_ATTRIBUTE_NOTIFICATION_TYPE);
                        }
                    }

                    selectedEvent = SubscriptionUtil.getSubscribableEvent(eventMenuCommands, (String)eventObjMap.get(DomainConstants.SELECT_ATTRIBUTE_EVENT_TYPE));
                    strIsGlobal = UIComponent.getSetting(selectedEvent, "Global");
                    if(notificationType == null || "null".equalsIgnoreCase(notificationType) || "".equalsIgnoreCase(notificationType.trim()) || both.equalsIgnoreCase(notificationType) || personNotificationType.equalsIgnoreCase(notificationType))
                    {
                        /* following if-else block ensures that the person is disconnected from the event if it is delete subscription on type and the event is global
                         * or it is delete subscription on object
                         */
                        if(subsPersonRelId !=null && forObject)
                        {
                            DomainRelationship.disconnect(context,subsPersonRelId);
                            personDisconnected = true;
                        }
                        else if(subsPersonRelId !=null && !forObject && "true".equalsIgnoreCase(strIsGlobal))
                        {
                            DomainRelationship.disconnect(context,subsPersonRelId);
                            personDisconnected = true;
                        }

                        //delete the event object if it has no other subscribers
                        if(personDisconnected && subscribersList instanceof String)
                        {
                            try
                            {
                                DomainObject objEvent = new DomainObject((String)eventObjMap.get(DomainConstants.SELECT_ID));
                                objEvent.deleteObject(context);
                                eventDeleteCounter++;

                            }
                            catch(Exception e)
                            {
                                throw new MatrixException(e);
                            }
                        }
                    }
                    else
                    {
                        //enters in to this condition iff user has subscription to get both the notification and he wants to unsbuscribe for any of the notification type
                        if(email.equalsIgnoreCase(notificationType))
                        {
                            //if unsubscription notification type is Email then modify notificaiton type attribute from Both to IconMail
                            DomainRelationship.setAttributeValue(context,subsPersonRelId, DomainConstants.ATTRIBUTE_NOTIFICATION_TYPE, iconMail);
                        }
                        else
                        {
                            //if unsubscription notification type is IconMail then modify notificaiton type attribute from Both to Email
                            DomainRelationship.setAttributeValue(context,subsPersonRelId, DomainConstants.ATTRIBUTE_NOTIFICATION_TYPE, email);
                        }

                    }
                }
            }
        }// eof if if
    }

    /**
    * Allows user to retrieve the subscribed events and their notification types on a Type/Object.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *          0 Type /ObjectId
    *          1  User Name (optional)
    * @returns void
    * @throws MatrixException if the operation fails
    * @since Common vX2
    */
    public StringList getSubscribedEventsAndNotificationTypes(Context context, String [] args) throws MatrixException,Exception
    {
        PrintWriter mw = new PrintWriter(new MatrixWriter(context));
        checkArgsForGetSubscribedEventNotifications(context, args, SubscriptionUtil.ADMIN_BUSINESS_TYPE);
        StringList finalList  = getSubscribedEventsAndNotificationTypes(context, args[0], args.length == 2 ? args[1] : context.getUser(), OBJECT);
        mw.println("Type and/or Events and Notifications::"+finalList);
        return  finalList;
    }


    /**
     * Allows user to retrieve the subscribed events and their notification types on a Interface/Connection Id.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *          0 Interface /ConnectionId
     *          1  User Name (optional)
     * @returns StringList Containing the list of Events and their Notification Types
     * @throws MatrixException if the operation fails
     * @since V6R2009x
     * @deprecated Interface related subscriptions are deprecated from V6R2011x, use Subscription on Relationships
     */
     public StringList getSubscribedEventsAndNotificationTypesOnInterface(Context context, String [] args) throws MatrixException,Exception
     {
         PrintWriter mw = new PrintWriter(new MatrixWriter(context));
         mw.println("Interface Subscriptions are no more supported");
         return  new StringList();
     }

 /**
    * This method is called when userName is not specified(assumes Context user as the required user) This internally calls the overloaded method with context user as the user Parameter.
    * Returns the StringList of events and their notification types on a particular Type/Object for the specified User
    * @param context - the eMatrix <code>Context</code> object
    * @param userName - User who needs the list of events and notification Types
    * @param typeOrObjId - Type or Object Id whose subscribed events and Notifications are to be retrieved based on the user.
    * @returns StringList Containing the list of Events and their Notification Types
    * @since Common vX2
    *
    * @deprecated Use
    * <code>
    * protected StringList getSubscribedEventsAndNotificationTypes(Context context, String id, String userName, String idType) throws Exception
    * </code>
    *     */
    protected StringList getSubscribedEventsAndNotificationTypes(Context context, String typeOrObjId) throws Exception
    {
        return getSubscribedEventsAndNotificationTypes(context, typeOrObjId, context.getUser(), OBJECT);
    }


    /**
     * This method is called when userName is not specified(assumes Context user as the required user) This internally calls the overloaded method with context user as the user Parameter.
     * Returns the StringList of events and their notification types on a particular Interface/Connection for the specified User
     * @param context - the eMatrix <code>Context</code> object
     * @param typeOrObjId - Interface or Connection Id whose subscribed events and Notifications are to be retrieved based on the user.
     * @returns StringList Containing the list of Events and their Notification Types
     * @since V6R2009x
     * @deprecated Interface related subscriptions are deprecated from V6R2011x, use Subscription on Relationships
     */
     public StringList getSubscribedEventsAndNotificationTypesOnInterface(Context context, String typeOrObjId) throws Exception
     {
         return new StringList();
     }


 /**
    * This method is called when user is explicitly specified.
    * Returns the StringList of events and their notification types on a particular Type/Object for the specified User
    * @param context - the eMatrix <code>Context</code> object
    * @param userName - User who needs the list of events and notification Types
    * @param typeOrObjId - Type or Object Id whose subscribed events and Notifications are to be retrieved based on the user.
    * @returns StringList Containing the list of Events and their Notification Types
    * @since Common vX2
    *
    * @deprecated Use
    * <code>
    * protected StringList getSubscribedEventsAndNotificationTypes(Context context, String id, String userName, String idType) throws Exception
    * </code>
    *
    */
     protected StringList getSubscribedEventsAndNotificationTypes(Context context, String typeOrObjId, String userName) throws Exception
    {
         return getSubscribedEventsAndNotificationTypes(context, typeOrObjId, userName, OBJECT);
    }


 /**
    * This method is called when user is explicitly specified.
    * Returns the StringList of events and their notification types on a particular Type/Object for the specified User
    * @param context - the eMatrix <code>Context</code> object
    * @param typeOrObjId - Type or Object Id whose subscribed events and Notifications are to be retrieved based on the user.
    * @param userName - User who needs the list of events and notification Types    *
    * @param idType - String contains Relationship or Object to identify the arg typePatternOrOID is Interface/Relationship or Type/Object
    * @returns StringList Containing the list of Events and their Notification Types
    * @since Common vX2
    *
    */


    protected StringList getSubscribedEventsAndNotificationTypes(Context context, String id, String userName, String idType) throws Exception
    {
        StringList finalList  = new StringList();

        if(userName == null || "".equals(userName) || "*".equals(userName)) {
            userName = context.getUser();
        } else {
            if(!userName.equals(MqlUtil.mqlCommand(context, "list user $1", true ,userName))) {
                return finalList;
            }
        }
        String adminObjType = getAdminObjTypeFromIdType(idType);
        boolean globalSubscription = !FrameworkUtil.isObjectId(context, id);
        //for object/relationship connection level subscriptions
        if(!globalSubscription) {
            // Validate the id is object or relationship id, then get the publish subscribe object using Publish Subscribe relationship.
            String pubSubId = SubscriptionUtil.getPubSubObjectId(context, false,adminObjType, id);
            finalList = pubSubId == null ? finalList : getFinalList(context, pubSubId, userName) ;

        } else {
            /*For type level (global) subscriptions, get the list if publish subscribe Id's with the name that of the symbolic name of that Type
            and its Parent Types*/
            StringList slObjectSelects = new StringList(2);
            slObjectSelects.add(DomainObject.SELECT_ID);
            slObjectSelects.add(DomainObject.SELECT_NAME);
            StringList symbolicNameList = new StringList(10);

            while(id != null && !"".equals(id)) {
                String symbolicName =  FrameworkUtil.getAliasForAdmin(context, adminObjType, id, true);
                if(symbolicName == null || "".equals(symbolicName)) {
                    String[] formatArgs = {id};
                    String message =  ComponentsUIUtil.getI18NString(context, "emxComponents.SubscriptionUtilBase.TypeNotRegistered",formatArgs);
                    throw new Exception(message);
                }
                symbolicNameList.add(symbolicName);
                id = MqlUtil.mqlCommand(context, "print $1 $2 select derived dump", true, adminObjType, id );
            }

            /**
             * When we add "a", "b" to list, list.toString returns the value as [a, b]
             * using regular expression we are replacing ", " with ","
             * After that deleting the char at first and last index
             */
            StringBuffer symbolicNamesPattern = new StringBuffer(StringUtils.replaceAll(symbolicNameList.toString(), ",\\s+", ","));
            symbolicNamesPattern.deleteCharAt(0);
            symbolicNamesPattern.deleteCharAt(symbolicNamesPattern.length() - 1);

            MapList objMapList = DomainObject.findObjects(context, DomainConstants.TYPE_PUBLISH_SUBSCRIBE, symbolicNamesPattern.toString(), "*", null, null,"revision == last", false, slObjectSelects);
            String[] pubSubIds = new String[symbolicNameList.size()];
            for (Iterator iter = objMapList.iterator(); iter.hasNext();) {
                Map pubSubObj = (Map) iter.next();
                int index = symbolicNameList.indexOf(pubSubObj.get(DomainConstants.SELECT_NAME));
                if(index != -1) {
                    pubSubIds[index] = (String) pubSubObj.get(DomainConstants.SELECT_ID);
                }
            }

            for (int i = 0; i < pubSubIds.length; i++) {
               if(pubSubIds[i] != null && !pubSubIds.equals(""))
                   finalList.addAll(getFinalList(context, pubSubIds[i], userName));
            }
         }

         filterList.clear();
         return finalList;
    }


    /**
    * Returns the finalList of events and their notification types on a particular Type/Object for the specified User
    * @param context - the eMatrix <code>Context</code> object
    * @param pubSubObjId - Publish Subscribe Object Id
    * @param userName - User who needs the list of events and notification Types
    * @returns StringList Containing the list of Events and their Notification Types
    * @since Common vX2
    */
    protected StringList getFinalList(Context context, String pubSubObjId, String userName) throws Exception
    {
        StringList finalList  = new StringList();
        String ctxUser = context.getUser();
        // getting all the events list matching the list of the event pattern
        SelectList selectlist = new SelectList();
        selectlist.addElement(DomainConstants.SELECT_SUBSCRIBED_PERSON_NAME);
        selectlist.addElement(DomainConstants.SELECT_ATTRIBUTE_EVENT_TYPE);
        selectlist.addElement(DomainConstants.SELECT_SUBSCRIBED_PERSON_ATTRIBUTE_NOTIFICATION_TYPE);

        MapList eventsList = null;
        DomainObject pubSubObject = new DomainObject(pubSubObjId);
        try{
            ContextUtil.pushContext(context);
            eventsList = pubSubObject.getRelatedObjects(context,
                    DomainConstants.RELATIONSHIP_PUBLISH,
                    DomainConstants.TYPE_EVENT,
                    selectlist, null, false, true, (short) 1, null, null, 0, null, null, null);
        } catch (Exception e) {
            throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        for (Iterator iter = eventsList.iterator(); iter.hasNext();) {
            Map eventMap = (Map) iter.next();
            String eventName = (String)eventMap.get(DomainConstants.SELECT_ATTRIBUTE_EVENT_TYPE);
            if(!filterList.contains(eventName)) {
                Object subscribers = eventMap.get(DomainConstants.SELECT_SUBSCRIBED_PERSON_NAME);
                String notificationType = null;
                if(subscribers instanceof StringList) {
                    int personIndex = ((StringList)subscribers).indexOf(ctxUser);
                    if(personIndex>-1) {
                        notificationType = (String)((StringList)eventMap.get(DomainConstants.SELECT_SUBSCRIBED_PERSON_ATTRIBUTE_NOTIFICATION_TYPE)).get(personIndex);
                    }
                } else if(ctxUser.equals((String)subscribers)) {
                    notificationType = (String)eventMap.get(DomainConstants.SELECT_SUBSCRIBED_PERSON_ATTRIBUTE_NOTIFICATION_TYPE);
                }
                if(notificationType == null || "".equalsIgnoreCase(notificationType) || "null".equalsIgnoreCase(notificationType))
                    continue;

                filterList.add(eventName);
                String typeName = PropertyUtil.getSchemaProperty(context,pubSubObject.getInfo(context, DomainConstants.SELECT_NAME));
                if(typeName!=null && !"".equals(typeName) && !"null".equalsIgnoreCase(typeName))
                {
                    finalList.add(typeName + "|"+ eventName+ "|" + notificationType);
                }
                else
                {
                    finalList.add(eventName + "|" + notificationType);
                }
            }
        }
        return finalList;
    }

    /**
     * Gets all subscribable events configured on the given interface
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - Interface Name associated with the Object
     * @returns MapList - List of the Maps containing the information of all the commands configured on the interface
     * @throws MatrixException if the operation fails
     * @since V6R2009x
     * @grade 0
     * @deprecated Interface related subscriptions are deprecated from V6R2011x, use Subscription on Relationships
     *
     */

    public MapList getInterfaceSubscribableEvents(Context context, String [] args)throws MatrixException
    {
        MapList eventList = null;
        if(args[0] != null)
        {

            String menuName = "";
            String interfaceName = args[0];

            try
            {
                //get the menu name
                menuName = PropertyUtil.getAdmintoProperty(context, "interface", interfaceName, "SubscriptionEventsMenu");

                String strInterfaceType = interfaceName;
                //if menu is not defined, then loop through type hierarcy until menu is found or top parent is reached
                while(menuName == null || "".equals(menuName))
                {
                    BusinessInterface busType =new BusinessInterface(strInterfaceType, context.getVault());
                    String parentInterfaceType = busType.getParent(context);

                    if(parentInterfaceType != null && !"".equalsIgnoreCase(parentInterfaceType))
                    {
                        menuName = PropertyUtil.getAdmintoProperty(context,"interface",parentInterfaceType,"SubscriptionEventsMenu");
                        strInterfaceType = parentInterfaceType;
                    }
                    else
                    {
                        break;
                    }
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
            if(menuName!= null && !"".equals(menuName))
            {
                // getting the eventlist for the given interface type, events are configured using menus and commands
                menuName = menuName.substring(5);
                eventList = UIMenu.getMenu(context, menuName, true);
            }
        }
        return eventList;
    }

    // This method is used to query all the notifications (Icon Mails) generated by a connection id.


    /**
     * Gets all the notifications (Icon Mails) matching the criteria passed as the arguments. Icon mails are filterd based
     * on Connection Id, Event Pattern (Event that generated this mail), From Date and To Date
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - Connection Id -  To filter the icon mails based on the connection id attached.
     *                           It accepts comma ',' separated type names e.g. "ECR,Part" and . '*' and '' to get all events.
     *        1 - Event Pattern - Event pattern. To filter the icon mails based on the Events for which they are generated.
     *                            It accepts comma ',' separated type names e.g. "ECR,Part" and . '*' and '' to get all events.
     *        2 - From Date - Date string, of format "MM/DD/YYYY"
     *        3 - To Date - Date string, of format "MM/DD/YYYY"
     *        4 - canDelete - String with true/false. true - Delete the added icon mails. False - Do not delete
     * @returns MapList List of the map containing the information of the Iconmails
     * @throws MatrixException if   the operation fails
     * @since Common V6R2009x
     * @grade 0
     *
     * @deprecated Interface related subscriptions are deprecated from V6R2011x, use Subscription on Relationships
     */

    public MapList getInterfaceInstanceNotifications(Context context, String [] args) throws MatrixException
    {
        MapList iconMailList = null;
        if(args.length < 5)
        {
            String errMsg = "";
            String lang = (String)context.getSession().getLanguage();
            try
            {
                errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Common.GetNotificationWorongParameters");
            }catch(Exception e){
                throw new MatrixException(e);
            }
            throw new MatrixException(errMsg);
        }
        //boolean isDeleteMail = args[4].equalsIgnoreCase("true")? true : false;
        return getNotifications(context, args[0], args[1], args[2], args[3], args[4], true,RELATIONSHIP);
    }


    /**
     * Gets all the notifications (Icon Mails) matching the criteria passed as the arguments. Icon mails are filterd based
     * on Interface Name (interface name associated with the connection id), Event Pattern (Event that generated this mail), From Date and To Date
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - Interface name- Interface. To filter the icon mails based on the type of interface associated with connection id.
     *                           It accepts comma ',' separated type names e.g. "InterfaceI1,Interface I2" and . '*' and '' to get all events.
     *        1 - Event Pattern - Event pattern. To filter the icon mails based on the Events for which they are generated.
     *                            It accepts comma ',' separated type names e.g. "Attribute Modified,Modify Description" and . '*' and '' to get all events.
     *        2 - From Date - Date string, of format "MM/DD/YYYY"
     *        3 - To Date - Date string, of format "MM/DD/YYYY"
     *        4 - canDelete - String with true/false. true - Delete the added icon mails. False - Do not delete
     * @returns MapList List of the map containing the information of the Iconmails
     * @throws MatrixException if   the operation fails
     * @since Common V6R2009x
     * @grade 0
     * @deprecated Interface related subscriptions are deprecated from V6R2011x, use Subscription on Relationships
     */
    public MapList getInterfaceNotifications(Context context, String[] args) throws MatrixException
    {
        if(args.length<5)
        {
            String errMsg = "";
            String lang = (String)context.getSession().getLanguage();
            try
            {
                errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Common.GetNotificationWorongParameters");
            }catch(Exception e){
                throw new MatrixException(e);
            }
            throw new MatrixException(errMsg);
        }
        return getNotifications(context, args[0], args[1], args[2], args[3], args[4], false, RELATIONSHIP);
    }


    /**
     * Delets events subscription on the connection id
     * Deletes the subscribed event for the context user, i.e disconnect the context user from the event and if the event dont have
     * any subscribers the delete the Event object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 connection id
     *        1 Event Name pattern
     *        2 Type of the notification, which user wants to unsubscribe for. Notification Types are Email, IconMail, Both
     * @returns void
     * @throws MatrixException if the operation fails
     * @since Common V6R2009x
     *
     * @deprecated Interface related subscriptions are deprecated from V6R2011x, use Subscription on Relationships
     *
     */
     public void deleteSubscriptionOnInterfaceInstance(Context context, String [] args) throws MatrixException
     {
         if(args.length < 2)
         {
             String errMsg = "";
             String lang = (String)context.getSession().getLanguage();
             try
             {
                 errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(lang),"emxComponents.SubscriptionDeleteonRelationship.WrongParameters");
             }catch(Exception e){
                 throw new MatrixException(e);
             }
             throw new MatrixException(errMsg);
         }
         String notificationType = null;
         if(args.length > 2)
         {
             notificationType = args[2];
         }
        // calling the unSubscribe Events to unsubcribe for events on the passed connection id with idType as Relationship
         unSubscribeEvents(context,args[0],args[1],true,notificationType,RELATIONSHIP);
     }

     /**
     * Delets global events subscription for the interface passed
     * Deletes the subscribed event for the context user, i.e disconnect the context user from the event and if the event dont have
     * any subscribers the delete the Event object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 interface name
     *        1 Event Name Pattern
     *        2 Type of the notification, which user wants to unsubscribe for. Notification Types are Email, IconMail, Both
     * @returns void
     * @throws MatrixException if the operation fails
     * @since Common V6R2009x
     * @deprecated Interface related subscriptions are deprecated from V6R2011x, use Subscription on Relationships
     */
     public void deleteSubscriptionOnInterface(Context context, String [] args) throws MatrixException
     {
         if(args.length < 2)
         {
             String errMsg = "";
             String lang = (String)context.getSession().getLanguage();
             try
             {
                 errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(lang),"emxComponents.SubscriptionDeleteonRelationshipEvent.WrongParameters");
             }catch(Exception e){
                 throw new MatrixException(e);
             }
             throw new MatrixException(errMsg);
         }
         String notificationType = null;
         if(args.length > 2)
         {
             notificationType = args[2];
         }
         // calling the unSubscribe Events to unSubscribe events on the passed interface name with idType as Relationship
         unSubscribeEvents(context,args[0],args[1],false,notificationType,RELATIONSHIP);
     }



    /**
    * Allows user to subscribe for a particular relationship instance (connection id) passed and subscribe
    * notification particular interface associated with that instance. Creates subscription object model for
    * the context user, i.e it creates Publish Subscribe object with auto generated name and then
    * creates and connects Event object to it and then Event object is connected to context user
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *          0  Connection id
    *          1  Event Name
    *          2  Notification Type
    * @returns void
    * @throws MatrixException if the operation fails
    * @since Common V6R2009x
    * @grade 0
    * @deprecated From V6R2011x, Subscription on Interfaces is deprecated, impleted Subscripion on relationships.
    *
    */


    public void subscribeOnInterfaceInstance(Context context, String [] args)throws MatrixException
    {
        String errMsg = "";
        String lang = (String)context.getSession().getLanguage();
        if(args.length < 3)
        {
            try
            {
               errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(lang),"emxComponents.subscribeOnInterfaceInstance.WrongParameters");
            }catch(Exception e){
                throw new MatrixException(e);
            }
            throw new MatrixException(errMsg);
        }
        if("".equals(args[0]) || "".equals(args[1]) || "".equals(args[2]))
        {
            try
            {
                errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(lang),"emxComponents.subscribeOnInterfaceInstance.WrongParameters");
            }catch(Exception e){
                throw new MatrixException(e);
            }
            throw new MatrixException(errMsg);
        }
        createSubscriptionOnInterface(context, args[0], args[1], args[2], true);
    }


    /**
    * Allows user to subscribe for a global event on a interface. Creates subscription object model for the context user,
    * i.e it creates Publish Subscribe object with symbolic name of the interface passed as its name and then creates and
    * connects Event object to it and then Event object is connected to context user
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *          0 Interface name
    *          1  Event Name
    *          2  Notification Type
    * @returns void
    * @throws MatrixException if the operation fails
    * @since Common V6R2009x
    *
    * @deprecated From V6R2011x, Subscription on Interfaces is deprecated, impleted Subscripion on relationships.
    */

    public void subscribeOnInterface(Context context, String [] args) throws MatrixException
    {
        String errMsg = "";
        String lang = (String)context.getSession().getLanguage();
        if(args.length < 3)
        {
            try
            {
                errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(lang),"emxComponents.SubscribeOnInterfaceType.WrongParameters");
            }catch(Exception e){
                throw new MatrixException(e);
            }
            throw new MatrixException(errMsg);
        }
        if("".equals(args[0]) || "".equals(args[1]) || "".equals(args[2]))
        {
            try
            {
                errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(lang),"emxComponents.SubscribeOnInterfaceType.WrongParameters");
            }catch(Exception e){
                throw new MatrixException(e);
            }
            throw new MatrixException(errMsg);
        }
        createSubscriptionOnInterface(context, args[0], args[1], args[2], false);
    }

    /**
    * createSubscriptionOnInterface - This method will take care of creating the Subscription object
    * @param context the eMatrix <code>Context</code> object
    * @param interfaceOrConnectionId - Interface name or Object Id string. this will be distinguished based on
    * the forInterfaceInstance boolean argument.
    * @param eventName - Event Name
    * @param notificationType - Notification Type
    * @param forInterfaceInstance - Used to determine if interfaceOrConnectionId contains Interface or Object Id
    *                        true - interfaceOrConnectionId is objectId
    *                       false - interfaceOrConnectionId is interface name.
    * @returns void
    * @throws MatrixException if the operation fails
    * @since Common V6R2009x
    *
    * @deprecated From V6R2011x, Subscription on Interfaces is deprecated, impleted Subscripion on relationships.
    */

    public void createSubscriptionOnInterface (Context context, String interfaceOrConnectionId, String eventName, String notificationType,boolean forInterfaceInstance) throws MatrixException
    {
        PrintWriter mw = new PrintWriter(new MatrixWriter(context));
        String strInterfaceType = null;
        String strEventPattern = eventName;
        String symbolicName = null;
        String errMsg = "";
        String lang = (String)context.getSession().getLanguage();

        String POLICY_PUBLISH_SUSCRIBE= PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_policy_PublishSubscribe);

        if(forInterfaceInstance)
        {
            if(interfaceOrConnectionId == null || "".equals(interfaceOrConnectionId) || "null".equals(interfaceOrConnectionId))
            {
                mw.println("Log - Exception - null or empty Object Id");
                try
                {
                    errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(lang),"emxComponents.Common.EmptyConnectionId");
                }catch(Exception e){
                   throw new MatrixException(e);
                }
                throw new MatrixException(errMsg);
            }
            else
            {
                try {
                    //gets the Interface name associated with the connection id
                    //Modified for Bug 363449
                    strInterfaceType = SubscriptionUtil.getInterface(context,interfaceOrConnectionId);
                    // To check whether the connection id is associated with any interface or not. If it is not associated with any interface
                    // throw error message says that Empty Interface name
                    if("".equals(strInterfaceType) || strInterfaceType == null)
                    {
                        mw.println("Log - Exception - Interface is not associated with connection id");
                        try
                        {
                            errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(lang),"emxComponents.Common.Connection.EmptyInterfaceType");
                        }catch(Exception e){
                           throw new MatrixException(e);
                        }
                        throw new MatrixException(errMsg);

                    }
                } catch (Exception e) {
                    throw new MatrixException(e);
                }
            }
        }
        else
        {
            if(interfaceOrConnectionId == null || "".equals(interfaceOrConnectionId) || "null".equals(interfaceOrConnectionId))
            {
                mw.println("Log - Exception - null or empty Interface");
                try
                {
                    errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(lang),"emxComponents.Common.EmptyInterfaceType");
                }catch(Exception e){
                   throw new MatrixException(e);
                }
                throw new MatrixException(errMsg);
            }
            else
            {
                strInterfaceType = interfaceOrConnectionId;

            }
            // get the symbolic name of the interface passed. Use this symbolic name of the interface to create the publish subcribe object for interface level
            symbolicName = FrameworkUtil.getAliasForAdmin(context, "interface",strInterfaceType, true);
        }

        if(strEventPattern == null || "".equals(strEventPattern) || "null".equals(strEventPattern))
        {
            mw.println("Log - Exception - null or empty Event Pattern");
            try
            {
                errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(lang),"emxComponents.Common.EmptyEventPattren");
            }catch(Exception e){
               throw new MatrixException(e);
            }
            throw new MatrixException(errMsg);
        }
        else
        {
            strEventPattern = StringUtils.replaceAll(strEventPattern,"\\s+,\\s+",",");
            //strEventPattern = strEventPattern.replaceAll("\\s+,\\s+",",");
        }



        if(notificationType == null || "".equals(notificationType.trim()) || "null".equals(notificationType))
        {
            mw.println("Log - Exception - null or empty Notification Type");
            try
            {
                errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(lang),"emxComponents.Common.EmptyNotificationType");
            }catch(Exception e){
               throw new MatrixException(e);
            }
            throw new MatrixException(errMsg);
        }


        Map commandMap = null;
        String[] args = {strInterfaceType};

        MapList eventList = getInterfaceSubscribableEvents(context, args);

        if(eventList!=null  && eventList.size()>0)
        {
            commandMap = SubscriptionUtil.getSubscribableEvent(eventList, eventName);
        }
        DomainRelationship domRel = null;
        try
        {
            if (commandMap!=null)
            {
                if(forInterfaceInstance)
                {
                    /*if it is subscription on Inteface Instance then continue with the existing
                    subscription functionality i.e Subscribe for an event on the object passed as argument, but not on interface*/
                    SubscriptionUtil.createSubscriptionOnInterfaceInstance(context, interfaceOrConnectionId, eventName, false, notificationType );
                }
                else
                {
                    String settingGlobal =  UIComponent.getSetting(commandMap, "Global");
                    //if global setting is true then subscribe on type
                    if ("true".equalsIgnoreCase(settingGlobal))
                    {
                        String masterObjVault= context.getVault().getName();
                        BusinessObject personObject=PersonUtil.getPersonObject(context);
                        DomainObject boEvent = null;
                        StringList selects = new StringList(1);
                        selects.add(DomainObject.SELECT_ID);
                        MapList pubSubObjList= DomainObject.findObjects(context,DomainConstants.TYPE_PUBLISH_SUBSCRIBE, symbolicName, "*", "*", "*",null, false, selects);
                        //check if user has any subscriptions on the interface
                        if(pubSubObjList.size() == 0)
                        {
                            DomainObject objPubSub = new DomainObject();
                            // creating the Public Subscribe Object with the symbolic name
                            objPubSub.createObject(context, DomainConstants.TYPE_PUBLISH_SUBSCRIBE, symbolicName, "", POLICY_PUBLISH_SUSCRIBE, masterObjVault);

                            String sEventId = FrameworkUtil.autoName(context, DomainSymbolicConstants.SYMBOLIC_type_Event, "", DomainSymbolicConstants.SYMBOLIC_policy_Event, masterObjVault);
                            boEvent = new DomainObject(sEventId);

                            //connecting the Publish Subscribe Object
                            objPubSub.connect(context, new RelationshipType(DomainConstants.RELATIONSHIP_PUBLISH), true, boEvent);
                            boEvent.open(context);
                            boEvent.setAttributeValue(context, DomainObject.ATTRIBUTE_EVENT_TYPE, eventName);
                            //connecting the evnet
                            domRel = new DomainRelationship(boEvent.connect(context, new RelationshipType(DomainConstants.RELATIONSHIP_SUBSCRIBED_PERSON), true, personObject));

                        }
                        else
                        {
                            Map pubSubMap = (Map) pubSubObjList.get(0);
                            String pubSubObjId = (String)pubSubMap.get(DomainObject.SELECT_ID);
                            DomainObject objPubSub = new DomainObject(pubSubObjId);

                            SelectList selectStmts    = new SelectList();
                            selectStmts.add(DomainConstants.SELECT_ATTRIBUTE_EVENT_TYPE);
                            selectStmts.addId();
                                                        //Modified for Double Revision events
                            String whereclause = DomainConstants.SELECT_ATTRIBUTE_EVENT_TYPE + " == const'" + eventName + "'";
                            MapList psEventList = null;
                            try
                            {
                                //getting all the Subscribed Events List
                                psEventList = objPubSub.getRelatedObjects(context,
                                DomainConstants.RELATIONSHIP_PUBLISH,
                                DomainConstants.TYPE_EVENT,
                                selectStmts,
                                null,
                                false,
                                true,
                                (short) 1,
                                whereclause,
                                null);
                            }
                            catch(Exception e) {
                                e.printStackTrace();
                            }

                            boolean isEvtNew = false;

                            if(psEventList !=null && psEventList.size() > 0)
                            {
                                Iterator itr = psEventList.iterator();
                                Map map = (Map)itr.next();
                                boEvent = new DomainObject((String)map.get(DomainObject.SELECT_ID));
                            }
                            else
                            {
                                String sEventId = FrameworkUtil.autoName(context, DomainSymbolicConstants.SYMBOLIC_type_Event, "", DomainSymbolicConstants.SYMBOLIC_policy_Event, masterObjVault);
                                isEvtNew = true;
                                boEvent = new DomainObject(sEventId);
                                objPubSub.connect(context, new RelationshipType(DomainConstants.RELATIONSHIP_PUBLISH), true, boEvent);
                                boEvent.open(context);
                                boEvent.setAttributeValue(context, DomainObject.ATTRIBUTE_EVENT_TYPE, eventName);
                            }

                            if(isEvtNew)
                            {
                                domRel = new DomainRelationship(boEvent.connect(context, new RelationshipType(DomainConstants.RELATIONSHIP_SUBSCRIBED_PERSON), true, personObject));
                            }
                            else
                            {
                                selects = new SelectList(1);
                                selects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
                                String strwhere = "name == '" +context.getUser() +"'";
                                //to get the Personlist
                                MapList personList = boEvent.getRelatedObjects(context,DomainConstants.RELATIONSHIP_SUBSCRIBED_PERSON,  DomainConstants.TYPE_PERSON, null, selects, false, true, (short)1, strwhere, null);

                                if(personList != null && personList.size() > 0)
                                {
                                    domRel = new DomainRelationship((String)((Map)personList.get(0)).get(DomainConstants.SELECT_RELATIONSHIP_ID));
                                }
                                else
                                {
                                    domRel = new DomainRelationship(boEvent.connect(context, new RelationshipType(DomainConstants.RELATIONSHIP_SUBSCRIBED_PERSON), true, personObject));
                                }
                            }
                        } //eof else
                        if(domRel!=null)
                        {
                            domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_NOTIFICATION_TYPE, notificationType);
                        }
                    } // end of if global setting
                    else
                    {
                        //throw exception if it is subscription on type and event is not global
                        mw.println("Slected Event is not Global");
                        try
                        {
                            errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(lang),"emxComponents.Common.EventNONGlobal");
                        }catch(Exception e){
                           throw new MatrixException(e);
                        }
                        throw new MatrixException(errMsg);
                    }
                }

            } // eof for event Found
            else
            {
                //if the given event is not configured as subscribable event on the given interface
                mw.println("Event is not Supported by Interface");
                try
                {
                    errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(lang),"emxComponents.Common.EventNONSupportedInterfaceType");
                }catch(Exception e){
                   throw new MatrixException(e);
                }
                throw new MatrixException(errMsg);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace(mw);
        }

    }

    /**
    * Method will gets invoked when pub sub object gets created. Used to udpate the cache variable if it
    * contains any of the child type name.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *          0  Pub Sub object id
    *          1  Pub Sub object name
    * @returns void
    * @throws Exception if the operation fails
    * @since Common V6R2009x
    */

    public void updatePubSubCache(Context context, String args[]) throws Exception
    {
        String strPubSubId = args[0];
        String strName = args[1];
        SubscriptionUtil.updatePubSubCacheMap(context,strPubSubId,strName);
    }


    private String getAdminObjTypeFromIdType(String idType) throws Exception {
        if(OBJECT.equals(idType)) {
            return SubscriptionUtil.ADMIN_BUSINESS_TYPE;
        } else if(RELATIONSHIP.equals(idType)) {
            return SubscriptionUtil.ADMIN_RELATIONSHIP_TYPE;
        } else {
            throw new Exception ("Invalid idType : " + idType);
        }
    }

    private void checkArgsForCreateDeleteSubscription(Context context, String[] args, boolean createSubscription, boolean globalSubscription, String adminObjType) throws Exception {
        boolean isBusinessType = SubscriptionUtil.isBusinessType(adminObjType);
        int minArgs = isBusinessType ? 2 : 3;
        boolean exception = false;
        String lang = context.getSession().getLanguage();

        if(args == null || args.length < minArgs) {
            exception = true;
        } else {
            for (int i = 0; i < args.length; i++) {
               if(args[i].trim().equals("")) {
                   exception = true;
               }
            }
            //For Relationship 2nd Argument should be true/false
            if(!exception && !isBusinessType) {
                globalSubscription = "true".equalsIgnoreCase(args[1]);
                if(!globalSubscription) {
                    if(!"false".equalsIgnoreCase(args[1])) {
                        exception = true;
                    }
                }
            }
        }

        if(exception) {
            String key = createSubscription ? "emxComponents.Subscription.Create." : "emxComponents.Subscription.Delete.";
            key = key + (isBusinessType ? (globalSubscription ? "Type.WrongParameters"  : "Object.WrongParameters" ) : "Relationship.WrongParameters");
            throw new Exception(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(lang),key));
        }
    }

    private void checkArgsForGetSubscribedEventNotifications(Context context, String[] args, String adminObjType) throws Exception {
        boolean isBusinessType = SubscriptionUtil.isBusinessType(adminObjType);
        if(args == null || args.length < 1 || "".equals(args[0])) {
            String lang = context.getSession().getLanguage();
            String key = isBusinessType ? "emxComponents.Subscription.EventsNotifications.Type.WrongParameters" : "emxComponents.Subscription.EventsNotifications.Relationship.WrongParameters";
            throw new Exception(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(lang),key));
        }
    }

    /**
     * CreateSubscription - This method will take care of creating the Subscription object
     * @param context the eMatrix <code>Context</code> object
     * @param globalSubscription - whether to create global subscription or object/connection level subscription
     * @param id - if globalSubscription pass admin object name(i.e. business type/relationship name e.g. "Part") otherwise pass object id or rel id
     * @param idType - Whether creating subscription for business type/ business object/relationship type/relationship connection
     *                       Pass 'Object' if creating global subscription for business type or creating object level subscription
     *                       Pass 'Relationship' if creating global subscription for relationship or creating connection level subscription
     * @param eventType - Event Type for which subscription need to be created for the user
     * @param notificationType - Notification Type, valid values are Both, IconMail, Email
     *                           Takes the default value Both, if it is null or invalid value is passed.
     * @returns void
     * @throws MatrixException if the operation fails
     */

     protected void createSubscription(Context context, boolean globalSubscription, String idType, String id, String eventType, String notificationType) throws Exception
     {
         SubscriptionUtil.createSubscriptions(context, globalSubscription, getAdminObjTypeFromIdType(idType), new StringList(id), new String[] {eventType}, notificationType, null);
     }


     /**
      * This method used to get the entire events list that can be subscribed by the context user on the given relationship.
      * args[0] -> Relationship Id - Mandatory
      * @param context
      * @param args
      * @return
      * @throws Exception
      */

     public MapList getRelationshipSubscribableEvents(Context context, String [] args) throws Exception {
         if(args == null || args[0] == null || "".equals(args[0])) {
             throw new Exception(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Subscription.EventsList.Relationship.WrongParameters"));
         }
         String relName = SubscriptionUtil.getTypeNameFromId(context, SubscriptionUtil.ADMIN_RELATIONSHIP_TYPE, args[0]);
         return SubscriptionUtil.getSubscribableEventsList(context, SubscriptionUtil.ADMIN_RELATIONSHIP_TYPE, relName);
     }

    /**
     * This method is used to create subscription for an event on a relationship instance/ relationship type (for context user).
     * Whenever the event occurs on that relationship instance/relationship type, system sends notifications to the user.
     * args[0] -> Relationship Id  or Relationship type - Mandatory
     * args[1] -> Boolean to indicate whether creating subscription for relationship instance/ relationship type - Mandatory ( true for relationship type and false for relationship instance)
     * args[2] -> Event Type   - Mandatory
     * args[3] -> Notification Type - Optional (default it  takes Both as notification type, if no value is passed or invalid notification type is passed).
     * @param context
     * @param args
     * @throws Exception
     * if  Arguments are not passed properly
     *     Invalid relationship id/name is given
     *     Event (that is passed in ) is not configured on Relation (or its hierarchy)
     */

    public void createRelationshipSubscription(Context context, String[] args) throws Exception {

        checkArgsForCreateDeleteSubscription(context, args, true, false, SubscriptionUtil.ADMIN_RELATIONSHIP_TYPE);
        boolean globalSubscription = "true".equalsIgnoreCase(args[1]);
        createSubscription(context, globalSubscription, RELATIONSHIP, args[0], args[2], args.length == 4 ? args[3] : null);
    }

    /**
     * Delete subscriptions created by the context user on the connection (instance of this relationship) for passed in events.
     * args[0] -> Rel id / Relationship Name - Mandatory
     * args[1] -> Boolean to indicate whether creating deleting subscription for relationship instance/ relationship type - Mandatory ( true for relationship type and false for relationship instance)
     * args[2] -> Event Type Pattern - Mandatory
     *    Get all the matching events (for passed in Event Pattern) and removes the subscription if user has subscribed for that event. We can pass multiple events with a comma separated value
     *    e.g. Part Deleted, Part Revised you can pass "Part*" or "*"
     * args[3] -> Notification Type - Optional (default it  takes Both as notification type, if no value is passed or invalid notification type is passed).
     *  Notification Type passed is null or Both - Context user is unsubscribed for both the notification types and disconnected from Event Object
     *  Notification Type passed is Email -
     *      o   If context user has subscribed for only Email then the context user is unsubscribed for Email notification type and disconnected from the Event Object
     *      o   If context user has subscribed for Both the types then the context user is unsubscribed for Email notification type and updates the notification type i.e. Notification Type attribute on the Subscribed Person with IconMail
     *      o   If context user has subscribed for only IconMail , (just returns without doing any change for this Subscription).
     *  Notification Type passed is IconMail -
     *      o   If context user has subscribed for only IconMail then the context user is unsubscribed for IconMail notification type and disconnected from the Event Object
     *      o   If context user has subscribed for Both the types then the context user is unsubscribed for IconMail notification type and updates the notification type i.e. Notification Type attribute on the Subscribed Person with Email
     *      o   If context user has subscribed for only Email , (just returns without doing any change for this Subscription).
     *
     * @param context
     * @param args
     * @throws Exception if
     *     If arguments length is not proper
     *     If invalid rel id/rel name is passed.
     *
     */

    public void deleteRelationshipSubscription(Context context, String[] args) throws Exception {
        checkArgsForCreateDeleteSubscription(context, args, false, false, SubscriptionUtil.ADMIN_RELATIONSHIP_TYPE);
        boolean globalSubscription = "true".equalsIgnoreCase(args[1]);
        unSubscribeEvents(context, args[0], args[2], !globalSubscription, args.length > 3 ? args[3] : null, RELATIONSHIP);
    }

     /**
      * Get all the notifications (Icon Mails) matching the criteria passed as the arguments,
      * Icon mails are filtered based on rel Id, Event Pattern (Event that generated this mail), From Date and To Date
      *     args[0] -> Rel Id /Rel Name - Mandatory, If not provided method will raise an exception, just pass empty string like "" if result is not to be constrained on the connection id.
      *     args[1] -> Boolean to indicate whether creating subscription getting notifications for relationship instance/ relationship type - Mandatory ( true for relationship type and false for relationship instance)
      *     args[2] -> Event Name Pattern - Mandatory, If not provided method will raise exception.
      *         It accepts comma "," separated event names e.g. "Attribute Modified,Modify Description"
                "*" and "" to get all events
      *     args[3] -> Start Date - Mandatory, If not provided method will raise exception, just pass empty string
              i.e. "" if result is not to be constrained on the start date. Date Format must be MM/DD/YYYY
      *     args[4] -> End Date - Mandatory, If not provided method will raise exception just pass empty string
              i.e. "" if result is not to be constrained on the end date, If empty string is passed then current date is taken
              as end date. Date Format must be MM/DD/YYYY
      *     args[5] -> can Delete - string with true/false should be passed. If true delete the icon mails.
      * @param context
      * @param args
      * @return
      * @throws Exception
      */
     public MapList getRelationshipNotifications (Context context, String [] args) throws Exception {
         if(args.length < 6)
         {
             String errMsg = "";
             String lang = (String)context.getSession().getLanguage();
             try
             {
                 errMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Common.GetNotificationWorongParameters");
             }catch(Exception e){
                 throw new MatrixException(e);
             }
             throw new MatrixException(errMsg);
         }
         boolean globalSubscription = "true".equalsIgnoreCase(args[1]);
         if(!globalSubscription) {
             if(!"false".equalsIgnoreCase(args[1])) {
                 //TODO Need to add i18N message
                 throw new MatrixException(ComponentsUtil.i18nStringNow("emxComponents.SubscriptionUtilBase.Argument2ShouldBeBoolean", context.getSession().getLanguage()));
             }
         }
         return getNotifications(context, args[0], args[2], args[3], args[4], args[5], !globalSubscription, RELATIONSHIP);
     }

     /**
      * Gets all subscribable events configured on the given Relationship
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the following input arguments:
      *        0 - Relationship Name or Rel Id
      * @returns MapList - List of the Maps containing the information of all the commands configured on the interface
      * @throws MatrixException if the operation fails
      * @since V6R2009x
      * @grade 0
      */

     public StringList getSubscribedEventsAndNotificationTypesOnRelationship (Context context, String [] args) throws Exception {
         PrintWriter mw = new PrintWriter(new MatrixWriter(context));
         checkArgsForGetSubscribedEventNotifications(context, args, SubscriptionUtil.ADMIN_RELATIONSHIP_TYPE);
         StringList finalList  = getSubscribedEventsAndNotificationTypes(context, args[0], args.length == 2 ? args[1] : context.getUser(), RELATIONSHIP);
         mw.println("Events and Notifications::"+finalList);
         return  finalList;
     }

     /*
     * Gets all hrefs of all subscribed types with the registered tree menu of type
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * @returns Vector - List of the hrefs of all types
     * @throws MatrixException if the operation fails
     * @since V6R2012
     * @grade 0
     */
    public Vector subscriptionNameColumn(Context context,String[] args) throws Exception
    {

       Vector subscriptionTypes=new Vector();

       HashMap programMap=(HashMap)JPO.unpackArgs(args);

       MapList objectList  = (MapList)programMap.get("objectList");
       HashMap paramMap    = (HashMap)programMap.get("paramList");
       HashMap columnMap    = (HashMap)programMap.get("columnMap");
       String exportFormat = (String) paramMap.get("exportFormat");
       String reportFormat = (String)paramMap.get("reportFormat");
       String jsTreeID     = (String)paramMap.get("jsTreeID");
       String columnName     = (String)columnMap.get("name");

       boolean isExportFormat = ((exportFormat != null) && (exportFormat.length() > 0) && ("CSV".equals(exportFormat))) ? true : false;
       boolean isPrinterFriendly = (null != reportFormat && !"null".equals(reportFormat)) ? true : false;

       for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
       {
           Hashtable eventType=(Hashtable)objectListItr.next();
           String objectId = (String)eventType.get("id");
           DomainObject typeObj = DomainObject.newInstance(context, objectId);
           StringList selectStmt = new StringList();
           selectStmt.add("to[" + RELATIONSHIP_PROJECT_VAULTS + "].from.type");
           selectStmt.add(DomainConstants.SELECT_NAME);
           selectStmt.add(DomainConstants.SELECT_TYPE);
           Hashtable typeInfo = (Hashtable)typeObj.getInfo(context, selectStmt);
           String parentType = (String)typeInfo.get("to[" + RELATIONSHIP_PROJECT_VAULTS + "].from.type");
           String name = (String)typeInfo.get(DomainConstants.SELECT_NAME);
           String type = (String)typeInfo.get(DomainConstants.SELECT_TYPE);

           StringBuffer buffer = new StringBuffer(50);
           String treeUrl      = "";
           buffer.append("emxTree.jsp?objectId=").append(objectId);
           buffer.append("&amp;jsTreeID=").append(jsTreeID);
           if(null != parentType && !"null".equals(parentType) && parentType.equals(DomainConstants.TYPE_PROJECT_SPACE))
           buffer.append("&amp;treeMenu=PMCtype_ProjectVault");

           String typeIcon = com.matrixone.apps.framework.ui.UINavigatorUtil.getTypeIconProperty(context, type);
           if((isExportFormat || isPrinterFriendly)&& !columnName.equals("newWindow"))
           {
               treeUrl = name;
           } else if(!isPrinterFriendly){
               if(columnName.equals("newWindow")) {
            	   treeUrl = "<a href=\"javascript:emxTableColumnLinkClick('"+ buffer.toString() + "','700','600','false','popup','');\"><img src=\"images/iconNewWindow.gif\" border=\"0\"/></a>";
               } else {
                   treeUrl = "<a href=\""+ buffer.toString() + "\" target=\"content\" ><img src=\"images/"+ typeIcon +"\" border=\"0\"/>"+ name +"</a>";
               }
           }
           subscriptionTypes.add(treeUrl);
       }

       return subscriptionTypes;
   }
}
