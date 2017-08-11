/*
 ** emxPLCNotificationUtilBase.java
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of MatrixOne, Inc.
 ** Copyright notice is precautionary only and does not evidence any actual or intended
 ** publication of such program.
 **
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.common.Issue;
import com.matrixone.apps.framework.ui.*;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.jdom.Document;

/**
 * The <code>emxPLCNotificationUtilBase</code> class contains common notification utility methods for PLC subscription services
 * @version Variant Configuration R207 - Copyright (c) 2008-2016 Dassault Systemes.
 * @since PLC R207
 */
public class emxPLCNotificationUtilBase_mxJPO
{
   private final static String MESSAGE_TYPE_HTML = "html";
   private final static String MESSAGE_TYPE_TEXT = "text";

   /**
    * Constructor.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @throws Exception if the operation fails
    */
   public emxPLCNotificationUtilBase_mxJPO (Context context, String[] args) throws Exception {}

   public static int transactionNotifications(Context context, String[] args) throws Exception
   {
      //String[] constructorIns = new String[] {args[1]};
      //Integer result = (Integer)(JPO.invoke(context, "emxPLCTransactionNotificationUtil", constructorIns, "transactionNotifications", args, Integer.class));
      Integer result = (Integer)(JPO.invoke(context, "emxPLCTransactionNotificationUtil", null, "transactionNotifications", args, Integer.class));
      return 0;
   }

   /**
    * This trigger method creates a mail notification when a feature or a product
    * is added as an affected item to an engineering change via the EC Affected Item
    * relationship.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        objectId - the Id for the To side object for the EC Affected Item relationship
    *        notificationName - PLCEngineeringChangeRaisedEvent
    *        toType - the type for the To side object for the EC Affected Item relationship
    *
    * A notification is generated only if the object is of a child type of Products or Features.
    *
    * @return an int 0 status code
    * @throws Exception if the operation fails
    */
   public static int notifyOnEngineeringChangeRaisedEvent(Context context, String[] args) throws Exception
   {
      //System.out.println("emxPLCNotificationUtilBase.notifyOnEngineeringChangeRaisedEvent begin");
      if (args == null || args.length < 3) { throw (new IllegalArgumentException()); }

      int result = 0;
      String toType = args[2];
      if (toType == null || toType.trim().length() == 0) { throw (new IllegalArgumentException()); }

      //Generate the notification if the object is a child of Products or Features
      if (mxType.isOfParentType(context, toType, ProductLineConstants.TYPE_PRODUCTS))
      {
         //System.out.println("Delegate to emxNotificationUtil.objectNotification for object type: " + toType);
         result = (emxNotificationUtil_mxJPO.objectNotification(context, args));
      }
      //System.out.println("emxPLCNotificationUtilBase.notifyOnEngineeringChangeRaisedEvent end");
      return result;
   }

   /**
    * This trigger method creates a mail notification when a feature or a product
    * is added an issue via the Issue relationship.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        objectId - the Id for the To side object for the Issue relationship
    *        notificationName - PLCIssueRaisedEvent
    *        toType - the type for the To side object for the Issue relationship
    *        fromType - the type for the From side object for the Issue relationship
    *
    * A notification is generated only if the From side object is of type Issue and
    * the To side object is of a child type of Products or Features.
    *
    * @return an int 0 status code
    * @throws Exception if the operation fails
    */
   public static int notifyOnIssueRaisedEvent(Context context, String[] args) throws Exception
   {
      //System.out.println("emxPLCNotificationUtilBase.notifyOnIssueRaisedEvent begin");
      if (args == null || args.length < 4) { throw (new IllegalArgumentException()); }

      int result = 0;
      String toType = args[2];
      String fromType = args[3];
      if (fromType == null || fromType.trim().length() == 0 || toType == null || toType.trim().length() == 0)
      { throw (new IllegalArgumentException()); }

      String typeIssue = PropertyUtil.getSchemaProperty(context,Issue.SYMBOLIC_type_Issue);

      //Generate the notification if the From side object is an Issue and the To side object is a child of Products
      if (fromType.equals(typeIssue) && (mxType.isOfParentType(context, toType, ProductLineConstants.TYPE_PRODUCTS)))
      {
         //System.out.println("Delegate to emxNotificationUtil.objectNotification for object type: " + toType);
         result = (emxNotificationUtil_mxJPO.objectNotification(context, args));
      }

      //System.out.println("emxPLCNotificationUtilBase.notifyOnIssueRaisedEvent end");
      return result;
   }

   /**
    * This trigger method creates a mail notification when a product version is created.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        objectId - the Id of the product that has a new product version just created
    *        notificationName - PLCProductVersionEvent
    *        toType - the type for the To side object for the Product Version relationship
    *
    * A notification is generated only if the To side object is not of type Product Variant.
    *
    * @return an int 0 status code
    * @throws Exception if the operation fails
    */
   public static int notifyOnProductVersionEvent(Context context, String[] args) throws Exception
   {
      //System.out.println("emxPLCNotificationUtilBase.notifyOnProductVersionEvent begin");
      if (args == null || args.length < 3) { throw (new IllegalArgumentException()); }

      int result = 0;
      String toType = args[2];
      if (toType == null || toType.trim().length() == 0) { throw (new IllegalArgumentException()); }

      //Generate the notification if the To side object is not of type Product Variant
      if (!toType.equals(ProductLineConstants.TYPE_PRODUCT_VARIANT))
      {
         //System.out.println("Delegate to emxNotificationUtil.objectNotification for object type: " + toType);
          //Fix for Bug: 371600 -- create notification object to initialize the variables instead of calling the static method directly. 
//        result = ${CLASS:emxNotificationUtil}.objectNotification(context, args);
         emxNotificationUtil_mxJPO notificationUtil = new emxNotificationUtil_mxJPO(context, args);
          result = notificationUtil.objectNotification(context, args);
      }

      //System.out.println("emxPLCNotificationUtilBase.notifyOnProductVersionEvent end");
      return result;
   }

   /**
    * This trigger method creates a mail notification when a product variant is created.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        objectId - the Id of the product that has a new product variant just created
    *        notificationName - PLCProductVariantEvent
    *        toType - the type for the To side object for the Product Version relationship
    *
    * A notification is generated only if the To side object is of type Product Variant.
    *
    * @return an int 0 status code
    * @throws Exception if the operation fails
    */
   public static int notifyOnProductVariantEvent(Context context, String[] args) throws Exception
   {
      //System.out.println("emxPLCNotificationUtilBase.notifyOnProductVariantEvent begin");
      if (args == null || args.length < 3) { throw (new IllegalArgumentException()); }

      int result = 0;
      String toType = args[2];
      if (toType == null || toType.trim().length() == 0) { throw (new IllegalArgumentException()); }

      //Generate the notification if the To side object is not of type Product Variant
      if (toType.equals(ProductLineConstants.TYPE_PRODUCT_VARIANT))
      {
         //System.out.println("Delegate to emxNotificationUtil.objectNotification for object type: " + toType);
//          Fix for Bug: 371600 -- create notification object to initialize the variables instead of calling the static method directly. 
         //result = (${CLASS:emxNotificationUtil}.objectNotification(context, args));
         emxNotificationUtil_mxJPO notificationUtil = new emxNotificationUtil_mxJPO(context, args);
         result = notificationUtil.objectNotification(context, args);
      }

      //System.out.println("emxPLCNotificationUtilBase.notifyOnProductVariantEvent end");
      return result;
   }

   public static String getNotificationBodyHTML(Context context, String[] args) throws Exception
   {
      Document doc = getMailXML(context, args, MESSAGE_TYPE_HTML);
      String result = (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, MESSAGE_TYPE_HTML));
      //System.out.println("getNotificationBodyHTML: " + result);
      return result;
   }

   public static String getNotificationBodyText(Context context, String[] args) throws Exception
   {
      Document doc = getMailXML(context, args, MESSAGE_TYPE_TEXT);
      String result = (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, MESSAGE_TYPE_TEXT));
      //System.out.println("getNotificationBodyText: " + result);
      return result;
   }

   private static Document getMailXML(Context context, String[] args, String messageType) throws Exception
   {
      Map info = (Map)JPO.unpackArgs(args);
      String notificationName = (String)info.get("notificationName");
      HashMap eventCmdMap = UIMenu.getCommand(context, notificationName);
      String eventName = UIComponent.getSetting(eventCmdMap, "Event Type");
      String eventKey = "PublishSubscribe.Event." + eventName.replace(" ", DomainConstants.EMPTY_STRING);
      String bundleName = (String)info.get("bundleName");
      String locale = ((Locale)info.get("locale")).toString();

      String objectId = (String)info.get("id"); //the context object,e.g., a product or a feature
      DomainObject domainObject = DomainObject.newInstance(context, objectId);
      StringList selectList = new StringList(3);
      selectList.addElement(DomainConstants.SELECT_TYPE);
      selectList.addElement(DomainConstants.SELECT_NAME);
      selectList.addElement(DomainConstants.SELECT_REVISION);

      Map objectInfo = domainObject.getInfo(context, selectList);
      String objType = (String)objectInfo.get(DomainConstants.SELECT_TYPE);
      String i18NObjType = UINavigatorUtil.getAdminI18NString("type", objType, locale);
      String objName = (String)objectInfo.get(DomainConstants.SELECT_NAME);
      String objRev = (String)objectInfo.get(DomainConstants.SELECT_REVISION);

      String[] msgValues = new String[] {i18NObjType, objName, objRev};
      String boName = MessageUtil.getMessage(context, null, "PublishSubscribe.Notification.DomainObjectName", msgValues, null, context.getLocale(), bundleName);

      // Modified by KXB IR - Mx378256  STARTS
      //String  user =  MqlUtil.mqlCommand(context,"get env USER");
      String  user =  MqlUtil.mqlCommand(context,"get env $1","USER");
      if (user.equalsIgnoreCase("User Agent"))
      {
    	  //user =  MqlUtil.mqlCommand(context,"get env APPREALUSER");
    	  user =  MqlUtil.mqlCommand(context,"get env $1","APPREALUSER");
          if(user.equals(""))
          {
        	  user=context.getUser();
          }
      }
      // Modified by KXB IR - Mx378256  ENDS
      String headerKey = eventKey + ".Notification.Header";
      msgValues = new String[] {boName, user};
      String msgHeader = MessageUtil.getMessage(context, null, headerKey, msgValues, null, context.getLocale(), bundleName);

      HashMap headerInfo = new HashMap();
      headerInfo.put("header", msgHeader);

      ArrayList dataLineInfo = new ArrayList();
      String viewLinkKey = null;
      String baseURL = (String)info.get("baseURL") + "?objectId=" + objectId;

      if (messageType.equals(MESSAGE_TYPE_HTML))
      {
         msgValues = new String[]{baseURL, boName};
         viewLinkKey = "PublishSubscribe.Notification.HtmlMail.ViewLink";
      }
      else
      {
         msgValues = new String[]{boName};
         viewLinkKey = "PublishSubscribe.Notification.TextMail.ViewLink";
      }

      String msgViewLink = MessageUtil.getMessage(context, null, viewLinkKey, msgValues, null, context.getLocale(), bundleName);
      dataLineInfo.add(msgViewLink);

      if (messageType.equals(MESSAGE_TYPE_TEXT))
         dataLineInfo.add(baseURL);

      HashMap footerInfo = new HashMap();
      footerInfo.put("dataLines", dataLineInfo);

      return (emxSubscriptionUtil_mxJPO.prepareMailXML(context, headerInfo, null, footerInfo));
   }//getMailXML
   /**
    * This trigger method creates a mail notification when a product revised
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        objectId - the Id of the product that is going to revised/derived
    *        notificationName - PLCProductDerivationEvent
    *        toType - the type for the To side object for the Main Derived relationship
    *        fromType-the type for the From side object for the Main Derived relationship
    *        
    *        A notification is generated only if the To side and From side object is type of  PRODUCTS.
    * @return an int 0 status code
    * @throws Exception if the operation fails
    */
   public static int notifyOnPLCProductEvolutionEvent(Context context, String[] args) throws Exception
 {
	    int result = 0;
		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}

		// Added code to check Product is Revised or Derived from History. Depending upon it respective event will get called.
		// i.e. PLCProductRevisionEvent or PLCProductDerivationEvent
		// Changes made for IR-367109-3DEXPERIENCER2017x Start
		
        StringBuffer strHistory  = new StringBuffer(args[0]);
        String       strHist1    = "";
        String       strHist2    = "";
        if(strHistory != null && !"".equals(strHistory)){
            strHist1    = strHistory.substring(strHistory.indexOf("id"), strHistory.lastIndexOf("id"));
            strHist2    = strHistory.substring(strHistory.lastIndexOf("id"));
        }
        if(strHist1.contains("history=modify") || strHist1.contains("history=revisioned")){
        	args[0] = strHist1.substring(strHist1.indexOf("id")+3, strHist1.lastIndexOf("type")).trim();
        }
        else if(strHist2.contains("history=modify") || strHist2.contains("history=revisioned")){
        	args[0] = strHist2.substring(strHist2.indexOf("id")+3, strHist2.lastIndexOf("type")).trim();
        }
        else
        {
        	return result;
        }
        
        args[2] = ProductLineConstants.TYPE_HARDWARE_PRODUCT;
        args[3] = ProductLineConstants.TYPE_HARDWARE_PRODUCT;
        if(strHist1.contains("connect Main Derived") || strHist2.contains("connect Main Derived"))
        {
        	args[1] = "PLCProductRevisionEvent";
        }
        else if(strHist1.contains("connect Derived") || strHist2.contains("connect Derived"))
        {
        	args[1] = "PLCProductDerivationEvent";
        }
        // Fixed for IR-367109-3DEXPERIENCER2017x End
        
		
		String toType = args[2];
		String fromType = args[3];
		if ((toType == null || toType.trim().length() == 0)
				&& (fromType == null || fromType.trim().length() == 0)) {
			throw (new IllegalArgumentException());
		}

		// Generate the notification if the To side object is not of type
		// Product
		if ((mxType.isOfParentType(context, toType,
				ProductLineConstants.TYPE_PRODUCTS))
				&& (mxType.isOfParentType(context, fromType,
						ProductLineConstants.TYPE_PRODUCTS)) && ProductLineCommon.isNotNull(args[1])) {
			emxNotificationUtil_mxJPO notificationUtil = new emxNotificationUtil_mxJPO(
					context, args);
			result = notificationUtil.objectNotification(context, args);
		}

		return result;
	}
   /**
    * This trigger method creates a mail notification when a product derived .
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        objectId - the Id of the product that is going to revised/derived
    *        notificationName - PLCProductDerivationEvent
    *        toType - the type for the To side object for the Derived relationship
    *        fromType-the type for the From side object for the Derived relationship
    *        
    *        A notification is generated only if the To side and From side object is type of  PRODUCTS.
    * @return an int 0 status code
    * @throws Exception if the operation fails
    */
   public static int notifyOnPLCProductDerivationEvent(Context context, String[] args) throws Exception
 {

		if (args == null || args.length < 3) {
			throw (new IllegalArgumentException());
		}

		int result = 0;
		String toType = args[2];
		String fromType = args[3];
		if ((toType == null || toType.trim().length() == 0)
				&& (fromType == null || fromType.trim().length() == 0)) {
			throw (new IllegalArgumentException());
		}
		// Generate the notification if the To side object is not of type Product
		if ((mxType.isOfParentType(context, toType,
				ProductLineConstants.TYPE_PRODUCTS))
				&& (mxType.isOfParentType(context, fromType,
						ProductLineConstants.TYPE_PRODUCTS))) {

			emxNotificationUtil_mxJPO notificationUtil = new emxNotificationUtil_mxJPO(
					context, args);
			result = notificationUtil.objectNotification(context, args);
		}

		return result;
	}
}//End of class
