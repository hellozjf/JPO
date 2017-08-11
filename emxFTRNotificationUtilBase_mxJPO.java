/*
 ** emxFTRNotificationUtilBase.java
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of MatrixOne, Inc.
 ** Copyright notice is precautionary only and does not evidence any actual or intended
 ** publication of such program.
 **
 */

import java.util.Iterator;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.common.Issue;
import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.productline.ProductLineConstants;

/**
 * The <code>emxFTRNotificationUtilBase</code> class contains common notification utility methods for FTR subscription services
 * @version Variant Configuration R207 - Copyright (c) 2008-2016 Dassault Systemes.
 * @since FTR R207
 */
public class emxFTRNotificationUtilBase_mxJPO extends emxPLCNotificationUtil_mxJPO
{

    public static final String ATTRIBUTE_RIGHT_EXPRESSION = PropertyUtil.getSchemaProperty("attribute_RightExpression");

   /**
    * Constructor.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @throws Exception if the operation fails
    */
   public emxFTRNotificationUtilBase_mxJPO (Context context, String[] args) throws Exception {
	   super(context, args);
   }

   public static int transactionNotifications(Context context, String[] args) throws Exception
   {
      JPO.invoke(context, "emxFTRTransactionNotificationUtil", null, "transactionNotifications", args, Integer.class);
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
    *        notificationName - FTREngineeringChangeRaisedEvent
    *        toType - the type for the To side object for the EC Affected Item relationship
    *
    * A notification is generated only if the object is of a child type of Products or Features.
    *
    * @return an int 0 status code
    * @throws Exception if the operation fails
    */
   public static int notifyOnEngineeringChangeRaisedEvent(Context context, String[] args) throws Exception
   {
      //System.out.println("emxFTRNotificationUtilBase.notifyOnEngineeringChangeRaisedEvent begin");
      if (args == null || args.length < 3) { throw (new IllegalArgumentException()); }

      int result = 0;
      String toType = args[2];
      if (toType == null || toType.trim().length() == 0) { throw (new IllegalArgumentException()); }

      //Generate the notification if the object is a child of Products or Features
      if (mxType.isOfParentType(context, toType, ConfigurationConstants.TYPE_PRODUCTS) || 
          mxType.isOfParentType(context, toType, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES) ||
          mxType.isOfParentType(context, toType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURES) ||
          mxType.isOfParentType(context, toType, ConfigurationConstants.TYPE_TEST_CASE))
      {
         //System.out.println("Delegate to emxNotificationUtil.objectNotification for object type: " + toType);
         result = (emxNotificationUtil_mxJPO.objectNotification(context, args));
      }
      //System.out.println("emxFTRNotificationUtilBase.notifyOnEngineeringChangeRaisedEvent end");
      return result;
   }

   /**
    * This trigger method creates a mail notification when a feature or a product
    * is added an issue via the Issue relationship.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        objectId - the Id for the To side object for the Issue relationship
    *        notificationName - FTRIssueRaisedEvent
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
      //System.out.println("emxFTRNotificationUtilBase.notifyOnIssueRaisedEvent begin");
      if (args == null || args.length < 4) { throw (new IllegalArgumentException()); }

      int result = 0;
      String toType = args[2];
      String fromType = args[3];
      if (fromType == null || fromType.trim().length() == 0 || toType == null || toType.trim().length() == 0)
      { throw (new IllegalArgumentException()); }

      String typeIssue = PropertyUtil.getSchemaProperty(context,Issue.SYMBOLIC_type_Issue);

      //Generate the notification if the From side object is an Issue and the To side object is a child of Products or Features
      if (fromType.equals(typeIssue) && 
    	 (mxType.isOfParentType(context, toType, ConfigurationConstants.TYPE_PRODUCTS) || 
    	  mxType.isOfParentType(context, toType, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES) ||
    	  mxType.isOfParentType(context, toType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURES) ||
    	  mxType.isOfParentType(context, toType, ConfigurationConstants.TYPE_TEST_CASE)))
      {
         //System.out.println("Delegate to emxNotificationUtil.objectNotification for object type: " + toType);
         result = (emxNotificationUtil_mxJPO.objectNotification(context, args));
      }

      //System.out.println("emxFTRNotificationUtilBase.notifyOnIssueRaisedEvent end");
      return result;
   }

   /**
    * This trigger method creates a mail notification when an inclusion rule
    * is updated for a feature or a product's GBOM part.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        relId - the Id for the Right Expression relationship created each time
    *			 			when a part inclusion rule is changed or created.
    *        notificationName - FTRPartInclusionRuleUpdatedEvent
    *
    * A notification is generated only if the From side object is of type Inclusion Rule and
    * the To side object is of type Feature List.
    *
    * @return an int 0 status code
    * @throws Exception if the operation fails
    */
   public static int notifyOnPartInclusionRuleUpdatedEvent(Context context, String[] args) throws Exception
   {
      //System.out.println("emxFTRNotificationUtilBase.notifyOnPartInclusionRuleUpdatedEvent begin");
      if (args == null || args.length < 2) { throw (new IllegalArgumentException()); }
      //System.out.println("args[0]=" + args[0]);
      //System.out.println("args[1]=" + args[1]);

      // Determine whether we need to fire based on parsing the history and finding out
      // whether the Right Expression has been modified.
      StringList idList = new StringList();
      boolean modifiedRightExpression = false;
      String singleId = "";
      String[] transHistories = args[0].split("\n");
      for (int i=0; i<transHistories.length; i++) {
          int idx = transHistories[i].indexOf("id=");
          if (idx == 0) {
        	  if (i > 0 && modifiedRightExpression) {
        		  // In the case of one or more Ids in the history
        		  idList.addElement(singleId);
        	  }
              singleId = transHistories[i].substring(3);
              continue;
          }
          
          idx = transHistories[i].indexOf("modify - ");
          if (idx == 0) {
              if (transHistories[i].indexOf(ATTRIBUTE_RIGHT_EXPRESSION) != -1)
                  modifiedRightExpression = true;  
          }
      }
      if (modifiedRightExpression)
		  idList.addElement(singleId);

      // For each of the IDs gathered, determine the ID of the corresponding LOGICAL STRUCTURES or Products
      // object that the user may have subscribed to.
      Iterator itrIds = idList.iterator();                        
      while (itrIds.hasNext()) {
          String ctxObjectId = _getObjectForRightExpression(context, (String)itrIds.next());
          //System.out.println("Processing=" + ctxObjectId);
          if (ctxObjectId == null || ctxObjectId.trim().length() == 0) continue;
          args[0] = ctxObjectId;   //this is the product or feature object id that a user may have subscribed.
          //System.out.println("Delegate to emxNotificationUtil.objectNotification for object Id: " + ctxObjectId);
          JPO.invoke(context, "emxNotificationUtil", null, "objectNotification", args, Integer.class);
          //System.out.println("emxFTRNotificationUtilBase.notifyOnPartInclusionRuleUpdatedEvent end");
      }
      

      return 0;
   }

   //When a part inclusion rule is created or updated, a Right Expression is created from an Inclusion Rule object
   //to a LOGICAL STRUCTURES or Products object.  Follow the Right Expression to the correct object and use that for
   //the notification.
   private static String _getObjectForRightExpression(Context context, String ruleId) throws Exception
   {
	   //System.out.println("emxFTRNotificationUtilBase._getObjectForRightExpression: " + ruleId);
       String objectId = "";
	   DomainObject domObj = DomainObject.newInstance(context, ruleId);
	   StringList objSelects = new StringList(1);
	   objSelects.add(DomainConstants.SELECT_ID);
       Pattern typePattern = new Pattern(ProductLineConstants.TYPE_LOGICAL_STRUCTURES);
       typePattern.addPattern(ProductLineConstants.TYPE_PRODUCTS);

	   MapList mapList = domObj.getRelatedObjects(context, ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION,
    		  typePattern.getPattern(), objSelects, null, false, true, (short)1, null, null,0);
	   if (mapList.isEmpty()) return objectId; //""; //not the right relationship
	   //System.out.println("   LOGICAL/PROD: " + mapList.toString());
	   objectId = (String)((Map)mapList.get(0)).get(DomainConstants.SELECT_ID);
       return objectId;
   }

}//End of class
