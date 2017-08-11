/*
 *  emxLibraryCentralClassificationRuleBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 *  static const RCSID [] = "$Id: ${CLASSNAME}.java.rca 1.12 Wed Oct 22 16:02:26 2008 przemek Experimental przemek $";
 */

import java.util.HashMap;
import java.io.*;
import java.net.*;
import java.util.*;

import matrix.db.*;
import matrix.util.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.library.LibraryCentralConstants;
import com.matrixone.apps.common.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.*;
import com.matrixone.apps.common.util.*;

/**
 * The class ${CLASSNAME} provides API for validation and checks on Classifications
 * and classified end items.
 * @exclude
 */
public class emxLibraryCentralClassificationRuleBase_mxJPO implements
        LibraryCentralConstants {

    /**
     * Creates ${CLASSNAME} object
     *
     * @param context  the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */
    public emxLibraryCentralClassificationRuleBase_mxJPO(Context context,
            String[] args) throws Exception {

    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int
     * @throws Exception if the operation fails
     * @exclude
     */
    public int mxMain(Context context, String[] args) throws FrameworkException {
        if (!context.isConnected())
            throw new FrameworkException("not supported on desktop client");
        return 0;
    }

    /**
     * This method ensures that no attribute values are lost for objects that get disconnected.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param releasedObjectId object ID of the released revision
     * @param flgDisconnectLaterRevs flag indicating whether to disconnect later revisions
     * @return String 0.0
     * @throws Exception if the operation fails
     */
    public static String releasedRevDisplaceOtherRevs(Context context,
            String releasedObjectId, boolean flgDisconnectLaterRevs)
            throws Exception {
        try {
            // All the work done in this method with mxsysHolderInterface etc.
            // is to ensure that no attribute values are lost for objects that
            // get disconnected. In the end, the objects that get disconnected
            // will still be attached to the Classification object's
            // corresponding
            // interface; all the trouble here is to prevent the disconnect
            // trigger
            // from temporarily severing this precious connection.
            // Additionally, disconnected objects get associated with one more
            // interface, for purposes of filtering them from LC queries.

            Vector vecResult = new Vector();
            Vector revList = getClassifiedItemRevisions(context,
                    releasedObjectId, flgDisconnectLaterRevs);
            DomainObject releasedObj = new DomainObject(releasedObjectId);
            String strType = releasedObj.getInfo(context,
                    DomainConstants.SELECT_TYPE);

            DomainObject doObj = new DomainObject();

            ContextUtil.startTransaction(context, true);
            Iterator revIter = revList.iterator();
            while (revIter.hasNext()) {
                String strObjectId = (String) revIter.next();
                StringBuffer strHolderName = new StringBuffer();
                strHolderName.append("mxsysHolderInterface.");
                strHolderName.append(strObjectId);

                doObj.setId(strObjectId);
                String strOriginalInterfacesCmd = "print bus $1 select $2 dump $3;";
                String strOriginalInterfacesResult = MqlUtil.mqlCommand(
                        context, strOriginalInterfacesCmd, true, strObjectId, "interface", ",").trim();
                StringList originalInterfaces = FrameworkUtil.split(
                        strOriginalInterfacesResult, ",");

                // If this object is not classified at all, or was previously
                // classified,
                // but is already in the "hidden" state, no need to do anything
                // with it.
                if (originalInterfaces.isEmpty()
                        || originalInterfaces
                                .contains(LibraryCentralConstants.INTERFACE_CLASSIFICATION_SEARCH_FILTER)) {
                    continue;
                }


               String[] mkHolderInterfaceCmdArgs = new String[originalInterfaces.size()+2];


                // Create the holder interface, deriving from everything that
                // this rev is interfacing
               StringBuffer strMkHolderInterfaceCmd = new StringBuffer("add interface $1 type $2");

               mkHolderInterfaceCmdArgs[0] = strHolderName.toString();
               mkHolderInterfaceCmdArgs[1] = "all";

               int mkHolderInterfaceCmdArgsIndex = 2;

               Iterator originalInterfaceIterator = originalInterfaces.iterator();
               while(originalInterfaceIterator.hasNext()){
                   String originalInterface = (String)originalInterfaceIterator.next();
                   strMkHolderInterfaceCmd.append(" derived $"+(mkHolderInterfaceCmdArgsIndex+1));
                   mkHolderInterfaceCmdArgs[mkHolderInterfaceCmdArgsIndex] = originalInterface;
                   mkHolderInterfaceCmdArgsIndex++;
               }

               MqlUtil.mqlCommand(context, strMkHolderInterfaceCmd.toString(), mkHolderInterfaceCmdArgs);

                // Put the holder interface on this rev. The holder will whether
                // the
                // disconnect trigger storm and preserve attribute values on
                // this rev.
                String strAddHolderToObjCmd = "modify bus $1 add interface $2";
                MqlUtil.mqlCommand(context, strAddHolderToObjCmd, true, strObjectId, strHolderName.toString());

                // find out what Classification objects this rev is connected to
                StringList sSelects = new StringList();
                sSelects.add(DomainObject.SELECT_ID);
                StringList relSelects = new StringList(1);
                relSelects.add(DomainRelationship.SELECT_ID);
                MapList containingClassesInfoList = doObj.getRelatedObjects(
                        context, RELATIONSHIP_CLASSIFIED_ITEM, TYPE_CLASSIFICATION,
                        sSelects, relSelects, true, false, (short) 1, null,
                        null,0);

                // Where ever a classification object contains this rev, replace
                // it
                // with the released rev
                Iterator containerInfoIter = containingClassesInfoList
                        .iterator();
                while (containerInfoIter.hasNext()) {
                    Map tempMap = (Map) containerInfoIter.next();
                    String strId = (String) tempMap.get(DomainObject.SELECT_ID);
                    DomainObject fromObject = new DomainObject(strId);
                    String strRelId = (String) tempMap
                            .get(DomainRelationship.SELECT_ID);

                    // Disconnect this rev from that containing class
                    DomainRelationship.disconnect(context, strRelId);

                    // then determine whether that class already contains the
                    // released
                    // object rev
                    StringBuffer strBusWhere = new StringBuffer();
                    strBusWhere.append("id == '");
                    strBusWhere.append(strId);
                    strBusWhere.append("'");
                    MapList alreadyContainingReleasedObj = releasedObj
                            .getRelatedObjects(context, RELATIONSHIP_CLASSIFIED_ITEM,
                                    TYPE_CLASSIFICATION, sSelects, relSelects, true, false,
                                    (short) 1, strBusWhere.toString(), null,0);

                    //then determine whether the released object rev is already part
                    //of other classes/interfaces.This indicates that the released object rev
                    //is already Reclassfied to some other class.

                    String strReleasedInterfacesCmd = "print bus $1 select $2 dump $3;";
                    String strReleasedInterfacesResult = MqlUtil.mqlCommand(
                             context, strReleasedInterfacesCmd, true, releasedObjectId, "interface", ",").trim();
                     StringList strReleasedInterfaces = FrameworkUtil.split(
                             strReleasedInterfacesResult, ",");


                    // if not, then connect the released rev there, as a
                    // replacement for the
                    // rev that we just disconnected
                    if (alreadyContainingReleasedObj.size() == 0 && strReleasedInterfaces.size()==0) {
                        DomainRelationship.connect(context, fromObject,
                                RELATIONSHIP_CLASSIFIED_ITEM, releasedObj);
                    }
                }

                // after the disconnect operation, see what interfaces that rev
                // still has on it; we will keep them and add back the ones that
                // were temporarily removed by the disconnect triggers. Minus of
                // course the holder, which will no longer be needed. But plus
                // the search filter interface.

                String strRemainingInterfacesCmd = "print bus $1 select $2 dump $3;";
                String strRemainingInterfacesResult = MqlUtil.mqlCommand(
                        context, strRemainingInterfacesCmd, true, strObjectId, "interface", "," ).trim();
                StringList remainingInterfaces = FrameworkUtil.split(
                        strRemainingInterfacesResult, ",");

                StringList addThese = (StringList) originalInterfaces.clone();
                addThese.removeAll(remainingInterfaces);
                // add in the search filter interface, if it's not already there
                if (!remainingInterfaces
                        .contains(LibraryCentralConstants.INTERFACE_CLASSIFICATION_SEARCH_FILTER)) {
                    addThese.add(LibraryCentralConstants.INTERFACE_CLASSIFICATION_SEARCH_FILTER);
                }

                String[] commandArgs = new String[addThese.size()+2];
                StringBuffer strModInterfacesCmd = new StringBuffer();
                strModInterfacesCmd.append("modify bus $1");
                commandArgs[0] = strObjectId;

                int i = 1;
                if (!addThese.isEmpty()) {
                    Iterator itr = addThese.iterator();
                    while(itr.hasNext()){
                        String interfaceToBeAdded = (String)itr.next();
                        strModInterfacesCmd.append(" add interface $"+(i+1));
                        commandArgs[i] = interfaceToBeAdded;
                        i++;
                    }
                }

                strModInterfacesCmd.append(" remove interface $"+(i+1));
                commandArgs[i] = strHolderName.toString();

                MqlUtil.mqlCommand(context, strModInterfacesCmd.toString(), true,commandArgs);

                // Finally, get rid of the holder
                String strDeleteHolderCmd = "delete interface $1";
                MqlUtil.mqlCommand(context, strDeleteHolderCmd, true,strHolderName.toString());
            }
            ContextUtil.commitTransaction(context);
        } catch (Exception ex) {
            ContextUtil.abortTransaction(context);
            throw new FrameworkException(ex);
        }
        return "0.0";
    }

    /**
     * The method gets the objectId of the Clasified Items revision
     *
     * @param context the eMatrix <code>Context</code> object
     * @param strObjectId object ID of the released revision
     * @param flgDisconnectLaterRevs flag indicating whether to include later revision
     * @return vector containg Object IDs of the revisions
     * @throws Exception if the operation fails
     */
    public static Vector getClassifiedItemRevisions(Context context,
            String strObjectId, boolean flgDisconnectLaterRevs)
            throws Exception {
        Vector vecResult = new Vector();
        DomainObject doObj = new DomainObject(strObjectId);
        StringList sSelects = new StringList(2);
        StringList relSelects = new StringList(1);
        sSelects.add(DomainObject.SELECT_ID);
        if (flgDisconnectLaterRevs) {
            MapList revList = doObj.getRevisionsInfo(context, sSelects,
                    new StringList());
            int iSize = revList.size();
            for (int i = 0; i < iSize; i++) {
                HashMap tempMap = (HashMap) revList.get(i);
                vecResult.addElement((String) tempMap.get("id"));
            }
            vecResult.removeElement(strObjectId);
        } else {
            vecResult = getPreviousRevisions(context, doObj);
        }
        return vecResult;
    }

    /**
     * The method gets the objectId of the previous revision.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param DomainObject doObj
     * @return Object ID of the previous revision
     * @throws Exception if the operation fails
     */
    public static String getPreviousRevision(Context context, DomainObject doObj)
            throws Exception {

        String strObjectId = "";
        BusinessObject busObj = (BusinessObject) doObj
                .getPreviousRevision(context);
        if (busObj != null) {
            strObjectId = busObj.getObjectId(context);
        }
        return strObjectId;
    }

    /**
     * The method gets all the previous revisions.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param DomainObject doObj of the released revision
     * @return Vector containg non released revision
     * @throws Exception if the operation fails
     */
    public static Vector getPreviousRevisions(Context context,
            DomainObject doObj) throws Exception {
        Vector vecOldRevisions = new Vector();
        if (doObj != null) {
            String strObjectId = getPreviousRevision(context, doObj);
            while (strObjectId != null && !strObjectId.equals("")) {
                vecOldRevisions.addElement(strObjectId);
                strObjectId = getPreviousRevision(context, new DomainObject(
                        strObjectId));
            }
        }
        return vecOldRevisions;
    }

    /**
     * Deletes the released revisions and also the later revisions based on the inputs.
     *
     * @param context the eMatrix <code>Context</code> user's context
     * @param args contains a Map with the following entries:
     *          0 - Object ID
     *          1 - whether to delete later revisions true/false
     * @ return String 0.0
     * @throws Exception if a major failure occurs
     */
    public static String releasedRevDisplaceOtherRevs(Context ctx, String[] args)
            throws Exception {
        String strObjectID = args[0];
        String strDisconnectLaterRevs = args[1];
        boolean disconnectLaterRevisions = false;
        if (strDisconnectLaterRevs.equals("true")) {
            disconnectLaterRevisions = true;
        }
        releasedRevDisplaceOtherRevs(ctx, strObjectID, disconnectLaterRevisions);
        return "0.0";
    }
}
