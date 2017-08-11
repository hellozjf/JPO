/*   emxRouteDocumentBase.java
**
**   Copyright (c) 2004-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   This JPO contains the code for checkin
**
*/

import matrix.db.*;
import matrix.util.*;
import java.util.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.common.*;
import com.matrixone.apps.common.util.*;
/**
 * The <code>emxCommonFileBase</code> class contains code for checkin.
 *
 * @version VCP 10.5.0.0 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxRouteDocumentBase_mxJPO extends emxVCDocument_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since VCP 10.5.0.0
     * @grade 0
     */
    public emxRouteDocumentBase_mxJPO (Context context, String[] args)
        throws Exception
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
     * @since VCP 10.5.0.0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.RouteDocument.MustSpecifyMethod", context.getLocale().getLanguage()));
        }
        return 0;
    }


    /**
     * This method is executed before create of document object
     *  to check the parent id is RFQ Template and that is active then
     *   to revise RFQ Tempalte and return new Template Id.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param uploadParamsMap HashMap
     * @param objectId String
     * @return Map containing new Parent Id
     * @throws Exception if the operation fails
     * @since Sourcing 10-5
     */
    public Map preCheckin(Context context, HashMap uploadParamsMap, String objectId) throws Exception
    {

        Map preCheckinMap = new HashMap();

        String folderId = (String) uploadParamsMap.get("folderId");
        String parentId = (String) uploadParamsMap.get("parentId");

        // If we are in the Route Wizard the folder becomes the parent.
        if ("RouteWizard".equalsIgnoreCase(parentId))
        {
           preCheckinMap.put("parentId", folderId);
        }

        return preCheckinMap;
    }

    /**
     * This method is executed after create of document object
     *  to check if it should be connected to a folder.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param uploadParamsMap HashMap
     * @param objectIds StringList
     * @return void
     * @throws Exception if the operation fails
     * @since 10-5
     */
    public void postCheckin(Context context, HashMap uploadParamsMap, StringList objectIds) throws Exception
    {
      Iterator i = objectIds.iterator();
      String objectId = "";
      if (i.hasNext())
        objectId = (String)i.next();

      // If 'routeContent' is not null, then we've added content from the
      // Route Content Summary page.
      String routeContent = (String)uploadParamsMap.get("routeContent");
      String routeContentFromInboxtask = (String)uploadParamsMap.get("routeContentId");
      if ((routeContent != null) && (!"".equals(routeContent)) && (!"null".equals(routeContent)))
      {
         String routeId = (String)uploadParamsMap.get("parentOID");
         Route route = (Route)DomainObject.newInstance(context, routeId);
         String idArray[] = {objectId};
         route.AddContent(context, idArray);

         //<Fix 372839>
         emxRoute_mxJPO routeJPO = new emxRoute_mxJPO(context, new String[]{routeId});
         routeJPO.inheritAccesstoContent(context, idArray);
         //</Fix 372839>
         SubscriptionManager subscriptionMgr = new SubscriptionManager(route);
         subscriptionMgr.publishEvent(context,
                                      route.EVENT_CONTENT_ADDED,
                                      objectId);
      }
	  else if ((routeContentFromInboxtask != null) && (!"".equals(routeContentFromInboxtask)) && (!"null".equals(routeContentFromInboxtask)))
	  {
		 String routeId = (String)uploadParamsMap.get("routeContentId");
         Route route = (Route)DomainObject.newInstance(context, routeId);
         String idArray[] = {objectId};
         route.AddContent(context, idArray);
         //<Fix 372839>
         emxRoute_mxJPO routeJPO = new emxRoute_mxJPO(context, new String[]{routeId});
         routeJPO.inheritAccesstoContent(context, idArray);
         //</Fix 372839>

         SubscriptionManager subscriptionMgr = new SubscriptionManager(route);
         subscriptionMgr.publishEvent(context,
                                      route.EVENT_CONTENT_ADDED,
                                      objectId);
	  }

      return;
    }
    /**
     * This method is executed after deletion of Object Route relationship
     *  to send the subscription notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return void
     * @throws Exception if the operation fails
     * @since 10-5-SP1
     */

    public void deleteAction(Context context,String []args) throws Exception
    {
       if (args.length < 2 )
       {
          throw new IllegalArgumentException();
       }
       String routeId = args[0];
       String objectId = args[1];
       DomainObject object  = DomainObject.newInstance(context, routeId);

       if((object.getType(context)).equals(DomainConstants.TYPE_ROUTE))
       {
           Route route = (Route)DomainObject.newInstance(context, routeId);
           SubscriptionManager subscriptionMgr = new SubscriptionManager(route);
           subscriptionMgr.publishEvent(context,
                                      route.EVENT_CONTENT_REMOVED,
                                      objectId);
       }
    }
}
