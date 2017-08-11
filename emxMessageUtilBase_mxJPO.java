/*
 ** emxMessageUtilBase
 **
 ** Copyright (c) 1999-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.common.util.SubscriptionUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.jdom.Document;
import com.matrixone.jsystem.util.MxLinkedHashMap;
import com.matrixone.jsystem.util.StringUtils;

/**
 * The <code>emxMessageUtilBase</code> class contains static methods for preparing mail content
 *
 * @version AEF 10-7 - Copyright (c) 2006, MatrixOne, Inc.
 */
public class emxMessageUtilBase_mxJPO extends emxDomainObject_mxJPO
{
    /**
     * Create a new emxMessageUtilBase object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     * @since AEF 10-7
     */
    public emxMessageUtilBase_mxJPO(Context context, String[] args) throws Exception {
        super(context, args);
}

/**
 * Main entry point.
 *
 * @param context the eMatrix <code>Context</code> object
 * @param args holds no arguments
 * @return an integer status code (0 = success)
 * @throws Exception if the operation fails
 * @since AEF 10-7
 */
    public int mxMain(Context context, String[] args) throws Exception {
            return 0;
    }

    /**
     * getBodyHTML - generates the xml string which would be passed on to make html message
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return xml String
     * @throws Exception if the operation fails
     * @since AEF 10-7
     * @grade 0
     */
    public static String getBodyHTML(Context context, String args[]) throws Exception
    {
        Map info = (Map)JPO.unpackArgs(args);
        info.put("messageType", "html");
		Document doc = getMailXML(context, info);
        return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "html"));
	}
    /**
     * getMailXML - generates the xml Document which would be passed on to make html message
     * @param context the eMatrix <code>Context</code> object
     * @param info - Map containing information about notification and subscription
     * @return xml Document
     * @throws Exception if the operation fails
     * @since AEF 10-7
     * @grade 0
     */
    public static Document getMailXML(Context context, Map info) throws Exception
    {
    	// get new message information
    	String newMsgId = (String)info.get("id");
        // get base url
        String baseURL = (String)info.get("baseURL");
		String bundleName = (String)info.get("bundleName");
		String locale = ((Locale)info.get("locale")).toString();
		String messageType = (String)info.get("messageType");

    	StringList sl = new StringList();
    	sl.addElement(getAttributeSelect(ATTRIBUTE_SUBJECT));
    	sl.addElement(SELECT_DESCRIPTION);
        DomainObject newMes = DomainObject.newInstance(context, newMsgId);
        Map newMsgMap = newMes.getInfo(context,sl);
        // header data
        HashMap headerInfo = new HashMap();
       	headerInfo.put("header",newMsgMap.get(getAttributeSelect(ATTRIBUTE_SUBJECT)));
        headerInfo.put("creatorText",
        			   FrameworkUtil.findAndReplace((String)newMsgMap.get(SELECT_DESCRIPTION),"\n", "<br/>"));

    	// get first message information
    	String firstMsgObjId = getRootMessageId(context,newMsgId);
    	sl = new StringList();
        sl.addElement(getAttributeSelect(ATTRIBUTE_SUBJECT));
        sl.addElement("to["+RELATIONSHIP_MESSAGE+"].from.to["+RELATIONSHIP_THREAD+"].from.id");
        sl.addElement("to["+RELATIONSHIP_MESSAGE+"].from.to["+RELATIONSHIP_THREAD+"].from.type");
        sl.addElement("to["+RELATIONSHIP_MESSAGE+"].from.to["+RELATIONSHIP_THREAD+"].from.name");
        sl.addElement("to["+RELATIONSHIP_MESSAGE+"].from.to["+RELATIONSHIP_THREAD+"].from.revision");
        sl.addElement("to["+RELATIONSHIP_MESSAGE+"].from.to["+RELATIONSHIP_THREAD+"].from.owner");
        sl.addElement("attribute["+ATTRIBUTE_SUBJECT+"]");
        DomainObject firstMsgObj = new DomainObject(firstMsgObjId);
        Map firstObjMap = firstMsgObj.getInfo(context,sl);
        TreeMap bodyInfo = new TreeMap();
        MxLinkedHashMap basicInfo = new MxLinkedHashMap();
        basicInfo.put(EnoviaResourceBundle.getProperty(context,bundleName,new Locale(locale), "emxComponents.Common.Type"),
                      UINavigatorUtil.getAdminI18NString("Type",(String)firstObjMap.get("to["+RELATIONSHIP_MESSAGE+"].from.to["+RELATIONSHIP_THREAD+"].from.type"),locale));
        StringBuffer rootObjName = new StringBuffer(1000);
        if (messageType.equalsIgnoreCase("html"))
		{
          	rootObjName.append("<a href='");
          	rootObjName.append(baseURL);
          	rootObjName.append("?objectId=");
          	rootObjName.append((String)firstObjMap.get("to["+RELATIONSHIP_MESSAGE+"].from.to["+RELATIONSHIP_THREAD+"].from.id"));
          	rootObjName.append("'>");
          	rootObjName.append((String)firstObjMap.get("to["+RELATIONSHIP_MESSAGE+"].from.to["+RELATIONSHIP_THREAD+"].from.name"));
          	rootObjName.append("</a>");
        }
        else
        {
			rootObjName.append((String)firstObjMap.get("to["+RELATIONSHIP_MESSAGE+"].from.to["+RELATIONSHIP_THREAD+"].from.name"));
			rootObjName.append(" ");
			rootObjName.append(baseURL);
			rootObjName.append("?objectId=");
			rootObjName.append((String)firstObjMap.get("to["+RELATIONSHIP_MESSAGE+"].from.to["+RELATIONSHIP_THREAD+"].from.id"));
        }
        basicInfo.put(EnoviaResourceBundle.getProperty(context,bundleName, new Locale(locale), "emxComponents.Common.Name"),
        											rootObjName.toString());
        basicInfo.put(EnoviaResourceBundle.getProperty(context,bundleName, new Locale(locale), "emxComponents.Common.Revision"),
        											(String)firstObjMap.get("to["+RELATIONSHIP_MESSAGE+"].from.to["+RELATIONSHIP_THREAD+"].from.revision"));
        basicInfo.put(EnoviaResourceBundle.getProperty(context,bundleName, new Locale(locale), "emxComponents.Common.Owner"),
        											(String)firstObjMap.get("to["+RELATIONSHIP_MESSAGE+"].from.to["+RELATIONSHIP_THREAD+"].from.owner"));
        StringBuffer discussion = new StringBuffer(1000);
        if (messageType.equalsIgnoreCase("html"))
		{
          	discussion.append("<a href='");
          	discussion.append(baseURL);
          	discussion.append("?objectId=");
          	discussion.append(firstMsgObjId);
          	discussion.append("'>");
          	discussion.append((String)firstObjMap.get(getAttributeSelect(ATTRIBUTE_SUBJECT)));
          	discussion.append("</a>");
        }
        else
        {
			discussion.append((String)firstObjMap.get(getAttributeSelect(ATTRIBUTE_SUBJECT)));
			discussion.append(" ");
			discussion.append(baseURL);
			discussion.append("?objectId=");
			discussion.append(firstMsgObjId);
        }
        basicInfo.put(EnoviaResourceBundle.getProperty(context,bundleName, new Locale(locale),"emxComponents.Discussions.DiscussionThread"),discussion.toString());

        bodyInfo.put(EnoviaResourceBundle.getProperty(context,bundleName, new Locale(locale),"emxComponents.Discussion.BasicInfoHeader"),basicInfo);

		return (emxSubscriptionUtil_mxJPO.prepareMailXML(context, headerInfo, bodyInfo, null));
    }

    /**
     * getBodyText - generates the xml string which would be passed on to make text message
     * @param context the eMatrix <code>Context</code> object
     * @param args - Map containing notification and subscription information
     * @return xml String
     * @throws Exception if the operation fails
     * @since AEF 10-7
     * @grade 0
     */
	public static String getBodyText(Context context, String args[]) throws Exception
    {
        Map info = (Map)JPO.unpackArgs(args);
        info.put("messageType", "text");
		Document doc = getMailXML(context, info);
		return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "text"));
    }

    /**
     * getRootMessageId - gets the root message object id from given object.
     * @param context the eMatrix <code>Context</code> object
     * @param messageObjId the object id to which the parent message object id has to be fetched
     * @return object id
     * @throws Exception if the operation fails
     * @since AEF 10-7
     * @grade 0
     */
    public static String getRootMessageId(Context context,String messageObjId) throws Exception
    {
        String rootMsgObjId = "";
        String isReplyObj =  new DomainObject(messageObjId).getInfo(context,"to["+DomainConstants.RELATIONSHIP_REPLY+"]");
        if(isReplyObj.equalsIgnoreCase("True"))
        {
            rootMsgObjId = MqlUtil.mqlCommand(context,"expand bus $1 to rel $2 recurse to end select bus $3 dump $4", messageObjId, DomainConstants.RELATIONSHIP_REPLY, "id", "|");
            rootMsgObjId = rootMsgObjId.substring(rootMsgObjId.lastIndexOf("|")+1,rootMsgObjId.length());
        }
        else
        {
            rootMsgObjId = messageObjId;
        }

        return rootMsgObjId;
    }

    public static MapList searchDiscussionsOnKeywords(Context context,String[] args) throws Exception
    {
             HashMap programMap = (HashMap)JPO.unpackArgs(args);
             MapList objectList = (MapList)programMap.get("objectList");
             String keywords = (String)programMap.get("keywords");
             try
             {
                 String objectId="";
                 String objectType="";
                 String strAttributeTitle="";
                 String strAttributeKeywords="";
                 String relPattern = DomainConstants.RELATIONSHIP_MESSAGE+","+ DomainConstants.RELATIONSHIP_REPLY;

                 keywords="*"+keywords+"*";

                 Map objectInfoMap=null;
                 MapList filterList=new MapList();

                 SelectList resultSelects = new SelectList(7);
                 resultSelects.add(DomainConstants.SELECT_ID);
                 resultSelects.add(DomainConstants.SELECT_DESCRIPTION);

                 Iterator iterator =objectList.iterator();

             while(iterator.hasNext())
             {
                Map objectMap=(Map)iterator.next();

                if (objectMap.containsKey("from["+DomainConstants.RELATIONSHIP_THREAD+"].to.id"))
                {
                    objectId=(String)objectMap.get("from["+DomainConstants.RELATIONSHIP_THREAD+"].to.id");
                    objectType=(String) objectMap.get(DomainConstants.SELECT_TYPE);
                }
                else
                {
                    DomainObject domObject=new DomainObject((String)objectMap.get(DomainConstants.SELECT_ID));

                    StringList objectselectables=new StringList(3);
                    objectselectables.add(DomainConstants.SELECT_ID);
                    objectselectables.add(DomainConstants.SELECT_TYPE);
                    objectselectables.add("from["+DomainConstants.RELATIONSHIP_THREAD+"].to.id");
                    objectselectables.add("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
                    objectselectables.add("attribute["+DomainConstants.ATTRIBUTE_KEYWORDS+"]");

                    objectInfoMap=domObject.getInfo(context,objectselectables);
                    objectId=(String)objectInfoMap.get("from["+DomainConstants.RELATIONSHIP_THREAD+"].to.id");
                    objectType=(String) objectInfoMap.get(DomainConstants.SELECT_TYPE);
                }

                if (DomainConstants.TYPE_BUG.equals(objectType))
                {
                        if (objectMap.containsKey("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]"))
                        {
                              strAttributeTitle=(String)objectMap.get("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
                              strAttributeKeywords=(String)objectMap.get("attribute["+DomainConstants.ATTRIBUTE_KEYWORDS+"]");
                        }
                        else
                        {
                               strAttributeTitle=(String)objectInfoMap.get("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
                               strAttributeKeywords=(String)objectInfoMap.get("attribute["+DomainConstants.ATTRIBUTE_KEYWORDS+"]");
                        }
                        strAttributeTitle=strAttributeTitle.toLowerCase();
                        strAttributeKeywords=strAttributeKeywords.toLowerCase();

                        String regexpKeywords = FrameworkUtil.findAndReplace(keywords,"*",".*");
                        regexpKeywords = FrameworkUtil.findAndReplace(regexpKeywords,"?",".?");
                        regexpKeywords = regexpKeywords.toLowerCase();

                        if (strAttributeTitle!=null && !"null".equals(strAttributeTitle) && !"".equals(strAttributeTitle))
                        {
                            if (StringUtils.matches(strAttributeTitle, regexpKeywords))
                            {
                               filterList.add(objectMap);
                               continue;
                            }
                        }
                        if (strAttributeKeywords!=null && !"null".equals(strAttributeKeywords) && !"".equals(strAttributeKeywords))
                        {
                            if (StringUtils.matches(strAttributeKeywords, regexpKeywords))
                            {
                               filterList.add(objectMap);
                               continue;
                            }
                        }
                 }

                if ( (objectId!=null && !"null".equals(objectId) && !"".equals(objectId)) )
                 {
                    String strResult = MessageUtil.getTempQueryResult(context,keywords,objectId,relPattern);
                    strResult = strResult.substring(strResult.lastIndexOf("=")+1,strResult.length()-1);
                    int objCount = Integer.parseInt(strResult);

                      if(objCount!=0)
                      {
                         filterList.add(objectMap);
                      }
                 }
            }// end while

        return filterList;

        }
        catch(Exception e)
        {
            throw new FrameworkException(e);
        }
    }
    
    public void subscribeToDiscussionOnMessageReply(Context context, String[] args) throws Exception {
        if(args == null || args.length == 0) {
            throw new IllegalArgumentException("Invalid arguments");
        }
        String rootMessageId = getRootMessageId(context, args[0]);
        //This method checks and create subscription if user is not already subscribed.
        SubscriptionUtil.createSubscription(context, false, SubscriptionUtil.ADMIN_BUSINESS_TYPE, rootMessageId, "New Reply", true, null);
    }
}

