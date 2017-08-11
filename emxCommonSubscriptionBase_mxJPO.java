/*   emxCommonSubscriptionBase.java
**
**   Copyright (c) 2003-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   This JPO contains the implementation of emxCommonSubscription
**
*/

import java.util.ArrayList; 
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.util.SubscriptionUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.AccessUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.framework.ui.UIComponent;

/**
 * The <code>emxCommonSubscriptionBase</code> class contains implementation code for emxCommonSubscription.
 *
 * @version Common 10.6 - Copyright (c) 2003, MatrixOne, Inc.
 */
public class emxCommonSubscriptionBase_mxJPO extends emxDomainObject_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     * @grade 0
     */
    public emxCommonSubscriptionBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

/**
     * This method used to Send Subscription notification to the user.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds objectId as first argument
     * @throws Exception if the operation fails
     * @since VCP 10.6
     */
  public void classifiedItemContentAddRemove(matrix.db.Context context, String[] args)
    throws Exception
  {
      try
      {
        if ( args == null || args.length == 0)
        {
          throw new IllegalArgumentException();
        }
        String objectId = args[0];
		String event = args[1];
        if (objectId != null && !"".equals(objectId) && !"null".equals(objectId)){
          String[] oids = new String[1];
          oids[0] = objectId;
          emxSubscriptionManager_mxJPO subMgr = new emxSubscriptionManager_mxJPO(context, oids);
          subMgr.publishEvent (context, event,objectId);
        } //end check for empty objectid param.
      }catch(Exception e){
        throw e;
      }
  }
  
  	/**
	 * This is a program to display table data
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args containing RequestValuesMap,objectId
	 * @return MapList containing name of the Events
	 * @throws Exception if Operation Fails
	 * @exclude
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getSubscriptionEventsTableData (Context context, String[] args) throws Exception
	{
		MapList mlReturn = new MapList();
		try {
			MapList mlEvents;
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strEnablePush = (String) programMap.get("enablePush");
			boolean bEnablePush =  "true".equalsIgnoreCase(strEnablePush) ? true : false;
			HashMap requestMap = (HashMap) programMap.get("RequestValuesMap");
			String strObjectId = (String)programMap.get("objectId");

			if(strObjectId != null && !"".equals(strObjectId))
			{
				DomainObject domObj = DomainObject.newInstance(context, strObjectId);
				String objType = domObj.getInfo(context, DomainConstants.SELECT_TYPE);
				mlEvents = SubscriptionUtil.getObjectSubscribableEventsList(context, strObjectId, objType, requestMap);

				HashMap tempMap;
				String strEventCmdName;
				String strEventName;
				String strI18nEventName;

				for (Object object : mlEvents) 
				{
					tempMap = (HashMap) object;
					strEventCmdName = UIComponent.getName(tempMap);
					strEventName = UIComponent.getSetting(tempMap, "Event Type"); 
					strI18nEventName = UIComponent.getLabel(tempMap);
					HashMap hm = (HashMap) new HashMap();
					hm.put(DomainConstants.SELECT_NAME, strEventName);
					hm.put(DomainConstants.SELECT_ID, strEventName);
					hm.put(DomainConstants.SELECT_TYPE, "Event");
					hm.put("Event Label", strI18nEventName);
					hm.put("subscribers", SubscriptionUtil.getSubscribersList(context, strObjectId, strEventCmdName, bEnablePush));
					mlReturn.add(hm);
				}
			}
		} catch (Exception e)
		{
			throw new MatrixException(e);
		}
		return mlReturn;
	}
	/**
	 * This is a expand program for APPSubscriptionTable Table
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args containing objectId and parentId
	 * @return MapList containing objectIds of Person and MemberList
	 * @throws Exception if operation fails
	 * @exclude
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getSubscribers(Context context,String args[])throws Exception
	{
		MapList resultList = new MapList();
		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);  
			String strObjectId = (String)programMap.get("objectId"); 
			String strParentId = (String)programMap.get("parentId");  

			String strPush = (String)programMap.get("enablePush");  
			DomainObject domObject = DomainObject.newInstance(context,strParentId);
			String strPersonType = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_type_Person);
			if(domObject.exists(context))
			{
				Map mpEvents =  SubscriptionUtil.getAllSubscribers(context, strParentId);
				if (mpEvents.containsKey(strObjectId))
				{
					MapList ml = (MapList)mpEvents.get(strObjectId);
					ml.remove(0);
					resultList.addAll(ml);
				}
				//code for checkbox selection
				//disableSelection=true\false
				if(!"true".equalsIgnoreCase(strPush))
				{
					for (Object object : resultList)
					{
						Map map=(Map)object;
						map.put("disableSelection", "true");
					}
				}else{
					for (Object object : resultList)
					{
						Map map=(Map)object;
						String strRel = (String)map.get("relationship");
						if(PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_relationship_SubscribedPerson).equals(strRel)){
							map.put("disableSelection", "true");
						}
						if(strPersonType.equals((String)map.get(DomainConstants.SELECT_TYPE))){
							map.put("hasChildren", "false");
						}
					}
				}
			}
			else
			{
				domObject = DomainObject.newInstance(context,strObjectId); 
				resultList = domObject.getRelatedObjects(context,
						PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_relationship_ListMember),
						strPersonType,
						new StringList(DomainConstants.SELECT_ID),
						null,
						false,
						true,
						(short)0,
						null,
						null,
						0);

				for (Object object : resultList)
				{
					Map map=(Map)object;
					map.put("disableSelection", "true");
					map.put("hasChildren", "false");
				}

			}
		} 
		catch (Exception e) 
		{
			throw new MatrixException(e);
		}
		return resultList;
	}

	/**
	 * This is a column program for 'Type' column of APPSubscriptionTable table
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args containing objectList
	 * @return StringList containing types
	 * @throws Exception if operation fails
	 * @exclude
	 */
	public StringList getSubscriptionTypeColumnData(Context context,String[] args) throws Exception
	{    	 
		StringList resultList = new StringList();
		try 
		{
			String strObjectId = "";
			String strObjectName = "";
			HashMap paramMap = (HashMap)JPO.unpackArgs(args);    	
			MapList objectList = (MapList)paramMap.get("objectList");
			Map tempMap;
			for (Object object : objectList)
			{
				tempMap = (Map) object;
				strObjectId =  (String)tempMap.get(DomainConstants.SELECT_ID);
				strObjectName =  (String)tempMap.get(DomainConstants.SELECT_NAME);
				String strObjectType = "";
				if(!strObjectId.equals(strObjectName))
				{
					DomainObject domObject = DomainObject.newInstance(context,strObjectId);
					strObjectType = domObject.getInfo(context, DomainConstants.SELECT_TYPE);
				}
				else
				{
					strObjectType = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_type_Event);
				}
				Locale strLocale = context.getLocale();
				strObjectType = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource",strLocale, "emxFramework.Type."+ strObjectType.replaceAll(" ","_"));
				resultList.addElement(strObjectType);
			}
		} catch (Exception e)
		{
			throw new MatrixException(e);
		}	 
		return resultList;

	}
	/**
	 * This is a column program for 'Subscription Events' column of APPSubscriptionTable Table
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args containing objectList
	 * @return StringList containing name of the Persons and MemberLists
	 * @throws Exception if operation fails
	 * @exclude
	 */
	public StringList getSubscriptionEventsColumnData(Context context,String args[]) throws Exception
	{
		StringList resultList = new StringList();
		try 
		{
			String strObjectId = ""; 
			String strObjectName = ""; 
			String strObjectType = "";
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");

			Map tempMap;
			for (Object object : objectList) 
			{
				tempMap = (Map) object;
				strObjectId = (String)tempMap.get(DomainConstants.SELECT_ID);
				strObjectName = (String)tempMap.get(DomainConstants.SELECT_NAME);   

				if(!strObjectId.equals(strObjectName))
				{
					DomainObject doObject = DomainObject.newInstance(context,strObjectId);						   		
					strObjectName = doObject.getInfo(context, DomainConstants.SELECT_NAME); 
					strObjectType = doObject.getInfo(context,DomainConstants.SELECT_TYPE);

					StringBuilder sbLink =new StringBuilder();

					if(strObjectType.equals(PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_type_Person))) 
					{
						String strType = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_type_Person); 
						String strTypeSymName = FrameworkUtil.getAliasForAdmin(context, "type", strType, true);
						String strTypeIcon = FrameworkProperties.getProperty(context,"emxFramework.smallIcon." + strTypeSymName);
						sbLink.append("<img src=\"images/"+strTypeIcon+"\" alt=\"\"></img>");
					}
					else
					{
						String strType = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_type_MemberList); // CHANGE
						String strTypeSymName = FrameworkUtil.getAliasForAdmin(context, "type", strType, true);
						String strTypeIcon = FrameworkProperties.getProperty(context,"emxFramework.smallIcon." + strTypeSymName);
						sbLink.append("<img src=\"images/"+strTypeIcon+"\" alt=\"\"></img>");
					}    		 
					String strURL = "../common/emxTree.jsp?objectId="+XSSUtil.encodeForJavaScript(context, strObjectId);
					sbLink.append("<a href=\"javascript:showModalDialog('"+strURL+"',700,600,false)\">");
					sbLink.append(XSSUtil.encodeForXML(context, strObjectName)+ "</a>");
					resultList.addElement(sbLink.toString());
				}
				else
				{
					String strEventLabel = (String)tempMap.get("Event Label");   
					StringBuilder sbLink = new StringBuilder();
					sbLink.append(XSSUtil.encodeForXML(context, strEventLabel));
					resultList.addElement(sbLink.toString());
				}
			}
		} 
		catch (Exception ex) 
		{
			throw new MatrixException(ex);
		}
		return resultList;
	}

	/**
	 * This is a column program for 'Email Id' column of APPSubscriptionTable table 
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args containing objectList
	 * @return StringList containing email id
	 * @throws Exception if operation fails
	 * @exclude
	 */
	public StringList getSubscriptionEmailIdColumnData(Context context,String[] args) throws MatrixException
	{
		StringList resultList = new StringList();
		try 
		{
			String strObjectId = "";
			String strObjectName = "";
			HashMap paramMap = (HashMap)JPO.unpackArgs(args); 
			MapList objectList = (MapList)paramMap.get("objectList");
			Map tempMap;
			for (Object object : objectList) 
			{
				tempMap = (Map) object;
				strObjectId =  (String)tempMap.get(DomainConstants.SELECT_ID);
				strObjectName =  (String)tempMap.get(DomainConstants.SELECT_NAME); 
				if(!strObjectId.equals(strObjectName))
				{
					DomainObject domObject = DomainObject.newInstance(context,strObjectId);
					String strEmail = domObject.getAttributeValue(context, PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_attribute_EmailAddress));
					resultList.addElement("<a href=\"mailto:"+XSSUtil.encodeForHTMLAttribute(context, strEmail)+"\">"+XSSUtil.encodeForHTML(context, strEmail)+"</a>");
				}
				else
				{
					resultList.addElement(DomainConstants.EMPTY_STRING);
				}

			}

		} catch (Exception ex) 
		{
			throw new MatrixException(ex);
		}
		return resultList;
	}
	
	/**
	 * This is a column program for 'Subscription Type' column of APPSubscriptionTable table 
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args containing objectList
	 * @return StringList containing type of subscription
	 * @throws Exception if operation fails
	 * @exclude
	 */
	public StringList getSubscriptionStatusColumnData(Context context,String[] args) throws MatrixException
	{
		StringList resultList = new StringList();
		try 
		{
			Map paramMap = (Map)JPO.unpackArgs(args);
			HashMap paramList = (HashMap)paramMap.get("paramList");
			
			String strEnablePush = (String) paramList.get("enablePush");
			boolean bEnablePush = "true".equalsIgnoreCase(strEnablePush) ? true : false;
			
			MapList objectList = (MapList)paramMap.get("objectList");
			String strLoginPerson=Person.getPerson(context).getName();
			
			Locale strLocale = context.getLocale();
			String strObjectId = (String)paramList.get("objectId");
			DomainObject dom = DomainObject.newInstance(context, strObjectId);
			String objType = dom.getInfo(context, DomainObject.SELECT_TYPE);
			MapList mlEvents = SubscriptionUtil.getObjectSubscribableEventsList(context, strObjectId, objType, paramList);
			Map mEvents = new HashMap();
			for (Object object : mlEvents) {
				HashMap map = (HashMap) object;
				mEvents.put(UIComponent.getSetting(map, "Event Type"), UIComponent.getName(map));
			}
			
			Map tempMap;
			for (Object object : objectList) 
			{
				tempMap = (Map) object;
				
				String strType=(String)tempMap.get(DomainConstants.SELECT_TYPE);
				String strRel = (String)tempMap.get("relationship");
				StringList slSubscribers = null;
				
				if("Event".equals(strType)){
					String strEventName = (String)tempMap.get("id");
					String strEventCmdName = (String)mEvents.get(strEventName);
					slSubscribers=SubscriptionUtil.getSubscribersList(context, strObjectId, strEventCmdName, bEnablePush);
				}
				
				
				StringList slSubscribersList=new StringList();

				if(null!=slSubscribers && slSubscribers.size() > 0)
				{
					for (Iterator<String> iterator = slSubscribers.iterator(); iterator.hasNext();)
					{
						String object2 =  iterator.next();
						StringList slTempList=FrameworkUtil.split(object2, "|");
						slSubscribersList.addAll(slTempList);
					}
				}

				boolean isLoginPersonSubscribed=false;
				boolean hasOtherSubscribers=false;

				if(null!=slSubscribersList && slSubscribersList.size() > 0) 
				{
					hasOtherSubscribers=true;
					if (slSubscribersList.contains(strLoginPerson))
					{
						isLoginPersonSubscribed=true;
					}
				}
				StringBuilder sbLink = new StringBuilder(256);
				if(bEnablePush){
					sbLink.append("<div align=\"center\">");
				}
				if(strRel!= null && strRel.equals(PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_relationship_PushedSubscription)))
				{
					String strTitle = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource",strLocale, "emxComponents.Tooltip.PushSubscribed");
					sbLink.append("<img src=\"images/iconActionPushSubscribe.gif\" title=\"").append(XSSUtil.encodeForHTMLAttribute(context,strTitle)).append("\" />");
				}
				else if(strRel!= null && strRel.equals(PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_relationship_SubscribedPerson)))
				{
					String strTitle = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource",strLocale, "emxComponents.Tooltip.SelfSubscribed");
					sbLink.append("<img src=\"images/iconSmallSubscription.gif\" title=\"").append(XSSUtil.encodeForHTMLAttribute(context,strTitle)).append("\"/>");
				}
				else if(null!=strType && !"".equals(strType) && !"null".equals(strType) && "Event".equals(strType))
				{
					if(isLoginPersonSubscribed && hasOtherSubscribers)
					{
						String strTitle = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource",strLocale, "emxComponents.Tooltip.ContextUserSubscribed");
						sbLink.append("<img src=\"images/iconStatusUserSubscribedToEvent.gif\" title=\"").append(XSSUtil.encodeForHTMLAttribute(context,strTitle)).append("\" />");
					}
					else if(!isLoginPersonSubscribed && hasOtherSubscribers && bEnablePush)
					{
						String strTitle = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource",strLocale, "emxComponents.Tooltip.ContextUserNotEventSubscribed");
						sbLink.append("<img src=\"images/iconStatusUserNotSubscribedToEvent.gif\" title=\"").append(XSSUtil.encodeForHTMLAttribute(context,strTitle)).append("\"/>");
					}
					else
					{
						sbLink.append(DomainConstants.EMPTY_STRING);
					}
				}

				else
				{
					sbLink.append(DomainConstants.EMPTY_STRING);
				}
				
				if(bEnablePush){
					sbLink.append("</div>");
				}
				resultList.add(sbLink.toString());
			}

		} 
		catch (Exception ex) 
		{
			throw new MatrixException(ex);
		}
		return resultList;
	}
	/**
	 * Access Program for Push Subscription
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args holds programMap with enablePush attribute value
	 * @return true if enablePush=true
	 * @throws Exception if operation fails
	 */
	public boolean checkAccessForPushSubscription(Context context,String[] args) throws Exception
	{
		HashMap paramMap = (HashMap)JPO.unpackArgs(args); 
		String strEnablePush=(String)paramMap.get("enablePush");

		if(null!=strEnablePush && !"null".equals(strEnablePush) && !"".equals(strEnablePush))
		{
			return Boolean.parseBoolean(strEnablePush);
		}
		else 
		{
			return false;
		}

	}
	/**
	 * Exclude OID program to exclude persons not having at least read access
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args holds programMap
	 * @return List of ids for exclusion
	 * @throws MatrixException if operation fails
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList getNonReadableExcludePersonOID(Context context,String[] args) throws MatrixException
	{
		try 
		{
			Map paramMap = (Map)JPO.unpackArgs(args); 
			String strObjectId=(String)paramMap.get("objectId");
			StringList slNonReadAccessList=new StringList();

			StringList slselectList=new StringList();
			slselectList.add(DomainConstants.SELECT_ID);
			slselectList.add(DomainConstants.SELECT_NAME);

			String typePattern = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_type_Person);
			String strPersonActiveState = PropertyUtil.getSchemaProperty(context, "policy", DomainConstants.POLICY_PERSON, "state_Active");
			String whereClause = "current" + " == " + "\'" + strPersonActiveState + "\'";

			MapList mlAllPersonList=DomainObject.findObjects(context, typePattern, "*",whereClause, slselectList);

			for (Iterator iterator = mlAllPersonList.iterator(); iterator.hasNext();) 
			{
				Map object = (Map) iterator.next();
				String strUser=(String)object.get(DomainConstants.SELECT_NAME);
				String strUserId=(String)object.get(DomainConstants.SELECT_ID);

				if(null!=strObjectId && !"".equals(strObjectId))
				{
					ArrayList accessMaskList = new ArrayList();
					accessMaskList.add("read");
					ArrayList personNamesList = new ArrayList();
					personNamesList.add(strUser);
					ArrayList personsWithoutAccessList = AccessUtil.hasAccess(context, strObjectId, personNamesList, accessMaskList);
					
					DomainObject dmoObject=DomainObject.newInstance(context,strObjectId);
					//AccessList accessList =dmoObject.getAccessForGrantee(context, strUser);
					if(personsWithoutAccessList.size() > 0)
					{
						slNonReadAccessList.add(strUserId);
					}
				} 
			}
			return  slNonReadAccessList;
		}

		catch (Exception ex)
		{
			throw new MatrixException(ex);
		}
	}
}
