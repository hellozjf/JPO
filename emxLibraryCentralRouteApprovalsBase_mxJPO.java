/*
 *  emxLibraryCentralRouteApprovalsBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * This JPO contains methods to display Route Approvals
 */


import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.common.Route;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;

/**
*
* @exclude
*/
public class emxLibraryCentralRouteApprovalsBase_mxJPO {

    /**
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @throws Exception if the operation fails
    */
   public emxLibraryCentralRouteApprovalsBase_mxJPO (Context context, String[] args)
   throws Exception
   {

   }

    /**
     * This method gets all Route Approvals data connected to the given object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectId       - object Id
     *    languageStr    - language String
     * @throws Exception if a major failure occurs
     * @since R212
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRouteApprovals(Context context , String[] args)
    throws Exception
    {
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        String sObjectId            = (String) programMap.get("objectId");
        String sLanguageStr         = (String) programMap.get("languageStr");

        String sLifeCycle           = EnoviaResourceBundle.getProperty(context,"emxDocumentCentralStringResource",new Locale(sLanguageStr),"emxDocumentCentral.Route.LifeCycle");
        String sAdHoc               = EnoviaResourceBundle.getProperty(context,"emxDocumentCentralStringResource",new Locale(sLanguageStr),"emxDocumentCentral.Route.AdHoc");
        String sSignStatus_Approved = EnoviaResourceBundle.getProperty(context,"emxDocumentCentralStringResource",new Locale(sLanguageStr),"emxDocumentCentral.Common.Approved");
        String sSignStatus_Ignored  = EnoviaResourceBundle.getProperty(context,"emxDocumentCentralStringResource",new Locale(sLanguageStr),"emxDocumentCentral.Common.Ignored");
        String sSignStatus_Rejected = EnoviaResourceBundle.getProperty(context,"emxDocumentCentralStringResource",new Locale(sLanguageStr),"emxDocumentCentral.Common.Rejected");
        String sSignStatus_Signed   = EnoviaResourceBundle.getProperty(context,"emxDocumentCentralStringResource",new Locale(sLanguageStr),"emxDocumentCentral.Common.Signed");

        String sStateName           = null;
        String sIl8StateName        = null;
        String sRouteName           = null;
        String sRouteId             = null;
        String sPersonName          = null;

        DomainObject doObj          = DomainObject.newInstance(context,sObjectId);
        String sPolicy              = doObj.getInfo(context, DomainObject.SELECT_POLICY);
        MapList mlRouteAprrovals    = doObj.getApprovalsInfo(context);

        MapList mlFilteredRouteList = new MapList();
        MapList mlMemberList        = null;

        Vector vecRoutes            = null;

        StringList slSignature      = null;
        StringList slSigner         = null;
        StringList slStatus         = null;
        StringList slDescription    = null;

        StringList slObjSelects     = new StringList();
        StringList slRelSelects     = new StringList();

        slObjSelects.addElement(Route.SELECT_NAME);

        slRelSelects.addElement(Route.SELECT_COMMENTS);
        slRelSelects.addElement(Route.SELECT_APPROVAL_STATUS);
        slRelSelects.addElement(Route.SELECT_APPROVERS_RESPONSIBILITY);
        slRelSelects.addElement(Route.SELECT_ROUTE_TASK_USER);

        HashMap hmStateRouteMap     = null;
        HashMap hmFilteredRouteMap  = null;

        Hashtable hmMemberMap       = null;

        Route routeObj              = null;

        // Iterate all route objects and for each rout object check if there is any signature
        // associated with and if it is there display the Signature details. If There is no
        // signature then get the details from Route
        for (Iterator itrObjects    = mlRouteAprrovals.iterator(); itrObjects.hasNext();) {
            hmStateRouteMap         = (HashMap) itrObjects.next();
            sStateName              = (String) hmStateRouteMap.get(DomainObject.SELECT_NAME);
            sIl8StateName           = UINavigatorUtil.getStateI18NString(sPolicy, sStateName.trim(), sLanguageStr);

            if (! UIUtil.isNullOrEmpty(sStateName)) {
                slSignature             = (StringList) hmStateRouteMap.get(DomainObject.KEY_SIGNATURE);
                boolean bhasSignature   = !sStateName.equals("Ad Hoc Routes") && slSignature.size() > 0 ;

                if (bhasSignature) {
                    slSigner            = (StringList) hmStateRouteMap.get(DomainObject.KEY_SIGNER);
                    slStatus            = (StringList) hmStateRouteMap.get(DomainObject.KEY_STATUS);
                    slDescription       = (StringList) hmStateRouteMap.get(DomainObject.KEY_COMMENTS);

                    for (int signItr = 0; signItr < slSignature.size(); signItr++) {
                        hmFilteredRouteMap      = new HashMap();
                        String sSignStatus      = (String) slStatus.get(signItr);
                        if (sSignStatus.equalsIgnoreCase("Approved")) {
                            sSignStatus = sSignStatus_Approved;
                        } else if (sSignStatus.equalsIgnoreCase("Ignored")) {
                            sSignStatus = sSignStatus_Ignored;
                        } else if (sSignStatus.equalsIgnoreCase("Rejected")) {
                            sSignStatus = sSignStatus_Rejected;
                        } else  if (sSignStatus.equalsIgnoreCase("Signed")) {
                            sSignStatus = sSignStatus_Signed;
                        } else {
                            sSignStatus = "";
                        }
                        hmFilteredRouteMap.put(DomainObject.SELECT_STATES, sIl8StateName);
                        hmFilteredRouteMap.put(DomainObject.SELECT_NAME, sLifeCycle);
                        hmFilteredRouteMap.put(DomainObject.KEY_SIGNATURE, (String) slSignature.get(signItr));
                        hmFilteredRouteMap.put(DomainObject.KEY_SIGNER, (String) slSigner.get(signItr));
                        hmFilteredRouteMap.put(DomainObject.KEY_STATUS, sSignStatus);
                        hmFilteredRouteMap.put(DomainObject.SELECT_DESCRIPTION, (String) slDescription.get(signItr));

                        mlFilteredRouteList.add(hmFilteredRouteMap);
                    }
                } else {
                    vecRoutes               = (Vector)hmStateRouteMap.get(DomainObject.KEY_ROUTES);

                    for (int routeItr = 0; routeItr < vecRoutes.size(); routeItr++) {
                        sRouteId            = (String) vecRoutes.get(routeItr);

                        if(!UIUtil.isNullOrEmpty(sRouteId)) {
                            routeObj        = new Route(sRouteId);
                            sRouteName      = routeObj.getName(context);
                            mlMemberList    = routeObj.getRouteMembers(context, slObjSelects, slRelSelects, false);

                            if (mlMemberList.size() > 0) {
                                for(int memeberItr = 0; memeberItr < mlMemberList.size() ; memeberItr++) {

                                    hmMemberMap     = (Hashtable) mlMemberList.get(memeberItr);
                                    sPersonName     = (String) hmMemberMap.get(Route.SELECT_ROUTE_TASK_USER);

                                    if(UIUtil.isNullOrEmpty(sPersonName)) {
                                        sPersonName         = (String) hmMemberMap.get(Route.SELECT_NAME);
                                    } else {
                                        //sPersonName       = Framework.getPropertyValue(session, sPersonName);
                                    }

                                    hmFilteredRouteMap      = new HashMap();

                                    hmFilteredRouteMap.put(DomainObject.SELECT_STATES,sIl8StateName);
                                    hmFilteredRouteMap.put(DomainObject.SELECT_NAME,sRouteName);
                                    hmFilteredRouteMap.put(DomainObject.KEY_SIGNATURE, (String) hmMemberMap.get(Route.SELECT_APPROVERS_RESPONSIBILITY));
                                    hmFilteredRouteMap.put(DomainObject.KEY_SIGNER, sPersonName);
                                    hmFilteredRouteMap.put(DomainObject.KEY_STATUS, (String) hmMemberMap.get(Route.SELECT_APPROVAL_STATUS));
                                    hmFilteredRouteMap.put(DomainObject.SELECT_DESCRIPTION, (String) hmMemberMap.get(Route.SELECT_COMMENTS));
                                    mlFilteredRouteList.add(hmFilteredRouteMap);
                                }
                            } else {
                                hmFilteredRouteMap      = new HashMap();

                                hmFilteredRouteMap.put(DomainObject.SELECT_STATES, sIl8StateName);
                                hmFilteredRouteMap.put(DomainObject.SELECT_NAME,sRouteName);
                                hmFilteredRouteMap.put(DomainObject.KEY_SIGNER, sPersonName);
                                hmFilteredRouteMap.put(DomainObject.KEY_SIGNATURE, "");
                                hmFilteredRouteMap.put(DomainObject.KEY_STATUS, "");
                                hmFilteredRouteMap.put(DomainObject.SELECT_DESCRIPTION, "");
                                mlFilteredRouteList.add(hmFilteredRouteMap);
                            }
                        }
                    }
                }
            }
        }
        return mlFilteredRouteList;
    }

    /**
     * This method sends State of the Object (The state in the object's
     * life cycle where the approval is required)
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList     - should contain object states
     * @throws Exception if a major failure occurs
     * @since R212
     */
    public Vector getObjectStateForRoute(Context context , String[] args)
    throws Exception
    {
        Vector vecResult            = new Vector();
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        MapList objectList          = (MapList) programMap.get("objectList");
        Map stateRouteMap           = null;
        for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
            stateRouteMap           = (Map) itrObjects.next();
            vecResult.add((String)stateRouteMap.get(DomainObject.SELECT_STATES));
        }
        return vecResult;
    }

    /**
     * This method sends Route Name (The route used to collect approvals)
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList     - should contain Route Name
     * @throws Exception if a major failure occurs
     * @since R212
     */
    public Vector getRouteName(Context context , String[] args)
    throws Exception
    {
        Vector vecResult            = new Vector();
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        MapList objectList          = (MapList) programMap.get("objectList");
        Map stateRouteMap           = null;
        for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
            stateRouteMap           = (Map) itrObjects.next();
            vecResult.add((String)stateRouteMap.get(DomainObject.SELECT_NAME));
        }
        return vecResult;
    }

    /**
     * This method sends Route Signature (The purpose of the approval)
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList     - should contain Route Signature
     * @throws Exception if a major failure occurs
     * @since R212
     */
    public Vector getRouteSignature(Context context , String[] args)
    throws Exception
    {
        Vector vecResult            = new Vector();
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        MapList objectList          = (MapList) programMap.get("objectList");
        Map stateRouteMap           = null;
        for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
            stateRouteMap           = (Map) itrObjects.next();
            vecResult.add((String)stateRouteMap.get(DomainObject.KEY_SIGNATURE));
        }
        return vecResult;
    }

    /**
     * This method sends Route Signer (The user whose approval is
     * required before the object can be released).
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList     - should contain Route Signer
     * @throws Exception if a major failure occurs
     * @since R212
     */
    public Vector getRouteSigner(Context context , String[] args)
    throws Exception
    {
        Vector vecResult            = new Vector();
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        MapList objectList          = (MapList) programMap.get("objectList");
        Map stateRouteMap           = null;
        for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
            stateRouteMap           = (Map) itrObjects.next();
            vecResult.add((String)stateRouteMap.get(DomainObject.KEY_SIGNER));
        }
        return vecResult;
    }

    /**
     * This method sends Route Status (Approved or Rejected as specified by the Signer.
     * If blank, the Signer has not yet acted on this approval)
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList     - should contain Route Status
     * @throws Exception if a major failure occurs
     * @since R212
     */
    public Vector getRouteStatus(Context context , String[] args)
    throws Exception
    {
        Vector vecResult            = new Vector();
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        MapList objectList          = (MapList) programMap.get("objectList");
        Map stateRouteMap           = null;
        for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
            stateRouteMap           = (Map) itrObjects.next();
            vecResult.add((String)stateRouteMap.get(DomainObject.KEY_STATUS));
        }
        return vecResult;
    }

    /**
     * This method sends Route Description (Any text entered by the person who
     * approved or rejected the object)
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList     - should contain Route Description
     * @throws Exception if a major failure occurs
     * @since R212
     */
    public Vector getRouteDescription(Context context , String[] args)
    throws Exception
    {
        Vector vecResult            = new Vector();
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        MapList objectList          = (MapList) programMap.get("objectList");
        Map stateRouteMap           = null;
        for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
            stateRouteMap           = (Map) itrObjects.next();
            vecResult.add((String)stateRouteMap.get(DomainObject.SELECT_DESCRIPTION));
        }
        return vecResult;
    }

}
