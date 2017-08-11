/*
 * Copyright (c) 2003-2016 Dassault Systemes.  All Rights Reserved.
 *  This program contains proprietary and trade secret information of
 *  Dassault Systemes.  Copyright notice is precautionary only and does not
 *  evidence any actual or intended publication of such program.
 *
 *    $Id:$
 */


import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.program.ProjectTemplate;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.util.StringList;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;


// Usage:
//     export bus 'Project Template' * * xml continue !archive into file "C:\Temp\ProjectTemplate_preMigrate.xml";
//     exec prog emxCommonFindObjects 1000 'Project Template' D:/temp;
//     exec prog emxProjectTemplateMigrationBase D:/temp 1 n;
//     export bus 'Project Template' * * xml continue !archive into file "C:\Temp\'Project Template'_postMigrate.xml";
//
//      type = args[1]
//      documentDirectory = args[2];
//      migrationProgramName = args[3]
//      name = args[4]
//      revision = args[5]
//      isRel = args[6]

// documentDirectory does not ends with "/" add it
//
@SuppressWarnings({ "rawtypes", "unchecked", "serial"})
public class emxProjectTemplateVersionMigrationBase_mxJPO extends emxCommonMigration_mxJPO {

    public emxProjectTemplateVersionMigrationBase_mxJPO(Context context, String[] args)
            throws Exception
    {
        super(context, args);
    }

    MapList mlRevisionChain = new MapList(1000); // OIDs addressed in Revision Chain

    private static String TYPE_PROJECT_TEMPLATE;
    private static String ATTRIBUTE_TITLE;
    private static String STATE_ACTIVE;         // emxFramework.State.Project_Template.Active
    private boolean error       = false;
    private boolean bCheckState = false;        // Disregard non Active Project Templates - DEFAULT: Active and InActive
    private boolean bBuildChain = true;         // Going to attempt Revision Chains based on Create Dates
    private boolean bDoMQL      = true;       // Perform the MQL - false for diagnostics messages only
    private static StringList busSelects;
    private static String whereActive;

    /**
     * Init all the Constants here
     * @param context
     * @throws FrameworkException
     */
    private static void init (Context context)
            throws FrameworkException
    {
        TYPE_PROJECT_TEMPLATE = PropertyUtil.getSchemaProperty(context, "type_ProjectTemplate");
        ATTRIBUTE_TITLE       = PropertyUtil.getSchemaProperty(context, "attribute_Title");
        STATE_ACTIVE          = PropertyUtil.getSchemaProperty(context, "policy", "Project Template", "state_Active");
        whereActive           = "current=='" + STATE_ACTIVE + "'";
    }

    // temp query bus 'Project Template' * * select id current versionid nextminor previousminor;
    private StringList buildSelects()
    {
        StringList slSelects = new StringList(8);
        slSelects.add(DomainConstants.SELECT_ID);
        slSelects.add(DomainConstants.SELECT_NAME);
        slSelects.add(DomainConstants.SELECT_REVISION);
        slSelects.add(DomainConstants.SELECT_OWNER);
        slSelects.add(DomainConstants.SELECT_ORIGINATED);
        slSelects.add(DomainConstants.SELECT_MODIFIED);
        slSelects.add(DomainConstants.SELECT_CURRENT);
        slSelects.add("attribute[" + ATTRIBUTE_TITLE + "]");
        return slSelects;
    }


    /**
     * This method writes the objectId to the sequential file, called from within JPO query where clause
     * This is a CommonMigrationBase method that can be overridden as a filter method
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args  - [0]ObjectId, [1]type
     * @returns boolean
     * @throws Exception if the operation fails
     */
    public boolean writeOID(Context context, String[] args) throws Exception
    {
        return super.writeOID(context, args);
//        String type     = args[1];
//        String objectId = args[0];
//         String writeIdStr = writeObjectId(context, args);
//        if ( writeIdStr != null && !"".equals(writeIdStr) )
//        {
//            fileWriter(writeIdStr);
//        }
//        return false;
    }



    /**
     * The Migration needs to perform the following:
     *      Check if Oid has been done already (from previous Name collection)
     *      Get Template information on Active Templates
     *      Find Templates with Same Name - These are possible revision Chains
     *      Determine if there is a logical sequence (date, common owner)
     *      Change Revision to non-random value (Try BusinessObject.setRevision(String revision)
     *          if MQL:
     *          Must temporarily Change Neme with new Revision
     *          Must revert Name change
     *
     *      Must user BusinessObject class - addAsMajorRevision(context, String versionId, int majorOrder)
     *
     * @param context
     * @param objectList
     * @throws Exception
     */
    public void migrateObjects(Context context, StringList objectList)
            throws Exception
    {
        int migrated = 0, unconverted = 0;


        init(context);    // LAMPKIN - need to init before building selectables

        busSelects           = buildSelects();
        String[] oidsArray   = new String[objectList.size()];
        oidsArray            = (String[])objectList.toArray(oidsArray);
        MapList mapList      = DomainObject.getInfo(context, oidsArray, busSelects);

        String versionCmd = "print bus $1 select $2 $3)";

        ContextUtil.pushContext(context, "creator", "", "");
        MqlUtil.mqlCommand(context, "trigger $1", "off");

        mqlLogRequiredInformationWriter("\n=============================================================" + "\n");

        Iterator<?> itr = mapList.iterator();
        while(itr.hasNext())
        {
            Map<?, ?> map = (Map<?, ?>)itr.next();

            String objectId = (String)map.get(SELECT_ID);
            String sCurrent = (String)map.get(SELECT_CURRENT);
            String sName    = (String)map.get(SELECT_NAME);
            String sRevision = (String)map.get(SELECT_REVISION);
            String sOwner    = (String) map.get (SELECT_OWNER);

            // If I'm checking the state and the object is not active I don't care
            if (bCheckState && !sCurrent.equalsIgnoreCase(STATE_ACTIVE)){
                mqlLogRequiredInformationWriter("\n(INACTIVE)*\t Object Id="+objectId+",\tObject Name="+sName+", Revision="+sRevision + ", Current="+sCurrent );
                writeUnconvertedOID(objectId);
                continue;
            }

            // If I have addressed this as part of another revision chain
            // we do not add the object id in the loop since it will not be encountered again
            if (mlRevisionChain.contains(objectId)) {
                mqlLogRequiredInformationWriter("\n(CHAIN)*\t Object Id="+objectId+",\tObject Name="+sName+ ", Revision=" + sRevision );
                continue;
            }

            // This may be a mistake since the access changes are bypassed
            if (!hasCandidateRevision(context, objectId, sRevision)) {
                mqlLogRequiredInformationWriter("\n(REV OK)*\t Object Id=" + objectId + ",\tObject Name=" + sName + ", Revision=" + sRevision);
                continue;
            }
            error = false;
            mqlLogRequiredInformationWriter("\n(PROCESS)\t Object Id="+objectId+",\tObject Name="+sName+", Revision="+sRevision);

            // **************  RELATED PROCESSING *********
            // Process Related Named Objects

            int iNextRevision = 2;
            MapList mlRelated = getSimilarTemplates (context, map);
            Iterator<?> itrRev = mlRelated.iterator();
            String sVersionId = DomainConstants.EMPTY_STRING;

            while(itrRev.hasNext()) {
                Map<?, ?> mapRev = (Map<?, ?>) itrRev.next();
                String tmpId   = (String) mapRev.get(SELECT_ID);
                String tmpName = (String) mapRev.get(SELECT_NAME);
                String tmpRev  = (String) mapRev.get(SELECT_REVISION);
                String tmpOwner = (String) mapRev.get(SELECT_OWNER);

                // If I have addressed this on previously, skip it here
                if (mlRevisionChain.contains(tmpId)) {
                    continue;
                }

                // Change the revision
                mqlLogRequiredInformationWriter("\n  (RELATED)\t Object Id="+tmpId+",\tObject Name="+tmpName+", From="+tmpRev);
                if ( bDoMQL ) {
                    MqlUtil.mqlCommand(context, "modify businessobject $1 name $2 revision $3", tmpId, "ProjectTemplateMigrationWIP", String.valueOf(iNextRevision));
                    MqlUtil.mqlCommand(context, "modify businessobject $1 name $2", tmpId, tmpName);

                   // Template should be available to the whole company.
                   // String defaultOrg = PersonUtil.getDefaultOrganization(context, context.getUser());
                    // DomainAccess.createObjectOwnership(context, projectTemplate.getId(context), defaultOrg, null, "Project Member", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP, true);
                    String defaultOrg = PersonUtil.getDefaultOrganization(context, tmpOwner);
                    DomainAccess.createObjectOwnership(context, tmpId, defaultOrg, null, "Project Member", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP, true);
                }

                // Get the major version of the first item in the list for use in remaining
                if ( sVersionId.isEmpty()) {
                    sVersionId = MqlUtil.mqlCommand(context, versionCmd, tmpId, "majorid", "dump");
                    System.out.println("\nVersionID: " + sVersionId);
                    continue;
                }

                // We don't want it terminating - just kick it
                try {
                    BusinessObject boObj = new BusinessObject(tmpId);
                    boObj.open(context);
                    boObj.addAsMinorRevision(context, sVersionId, iNextRevision);
                    boObj.close(context);

                } catch (FrameworkException ex) {
                    mqlLogRequiredInformationWriter("\n  (REVCHAIN)*\t" + ex.getMessage());
                    writeUnconvertedOID(objectId);
                    unconverted++;
                }
                mlRevisionChain.add(tmpId);
                iNextRevision++;
            }
            // ************** END RELATED PROCESSING *********

            if (!error)
            {
                loadMigratedOids(objectId);
                migrated++;
            }
            else
            {
                writeUnconvertedOID(objectId);
                unconverted++;
            }
        }

        mqlLogRequiredInformationWriter("\n=============================================================\n\nProcessing complete. " +
                migrated + " Project Template(s) migrated. " + unconverted + " Project Template(s) require further attention.\n");
    }

    /**
     * Get a list of similarly named Templates and sort by origination date
     * for use in the revision chain
     * @param context
     * @param mapObject
     * @return
     */

    private MapList getSimilarTemplates (Context context, Map mapObject) {

        String sName  = (String) mapObject.get(DomainConstants.SELECT_NAME);
        String sOwner = (String) mapObject.get(DomainConstants.SELECT_OWNER);
        String objId  = (String) mapObject.get(DomainConstants.SELECT_ID);

        String sWhere = DomainConstants.EMPTY_STRING;
        if (bCheckState) {
            sWhere = whereActive;
        }

        MapList mlUnique    = new MapList();    // List of same name candidates
        try {
            MapList ml = DomainObject.findObjects(context,
                    TYPE_PROJECT_TEMPLATE,              // Type
                    sName,                              // Name
                    DomainConstants.QUERY_WILDCARD,     // Revision
                    sOwner,                             // Owner
                    DomainConstants.QUERY_WILDCARD,     // Vault
                    sWhere,                             // Where
                    false,                              // Expand Type
                    busSelects
            );

            // Eliminate ME - no! keep for sort
//            Iterator<?> itr = ml.iterator();
//            while(itr.hasNext()) {
//                Map<?, ?> map = (Map<?, ?>) itr.next();
//                String objectId = (String) map.get(SELECT_ID);
//                if (!objectId.equalsIgnoreCase(objId)) {
//                    mlUnique.add(map);
//                }
//            }

            Collections.sort(ml, new Comparator() {
                        public int compare(Object arg0, Object arg1) {
                            Map map1 = (Map) arg0;
                            Map map2 = (Map) arg1;
                            String created1 = (String) map1.get(SELECT_ORIGINATED);
                            String created2 = (String) map2.get(SELECT_ORIGINATED);
                            return (eMatrixDateFormat.getJavaDate((String) map1.get(SELECT_ORIGINATED))).compareTo(eMatrixDateFormat.getJavaDate((String) map2.get(SELECT_ORIGINATED)));
                        }
                    });

                mlUnique=ml;

            }catch (FrameworkException ex) {
            ex.printStackTrace();
        }
        return mlUnique;
    }
    /**
     *  There are many ways to test this but length seems the simpliest
     *  If I am short - I still need to check full numeric
     * @param context
     * @param objectId
     * @param sRevision
     * @return
     */
    private boolean hasCandidateRevision (Context context, String objectId, String sRevision) {

        if (sRevision.length() > 4) return true;
        try {
            int irev = Integer.parseInt(sRevision);
            return false;

        } catch (Exception ex) {
            return true;
        }
    }

}
