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
import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.StringList;

import java.util.Iterator;
import java.util.Map;

// Usage:
//     exec prog emxCommonFindObjects 1000 'Project Template' E:/temp;
//     exec prog emxProjectTemplateFolderMigrationBase E:/temp 1 n;
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
public class emxProjectTemplateFolderMigrationBase_mxJPO extends emxCommonMigration_mxJPO {

    public emxProjectTemplateFolderMigrationBase_mxJPO(Context context, String[] args) throws Exception {
        super(context, args);
    }

    private static String TYPE_PROJECT_TEMPLATE;
    private static String STATE_ACTIVE;         // emxFramework.State.Project_Template.Active
    private static String RELATIONSHIP_DATAVAULTS;
    private static String INTERFACE_TEMPLATE_FLDR;
    private static String ATTRIBUTE_ACCESSTYPE;

    private boolean error = false;
    private static StringList busSelects;
    private static StringList relSelects;
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
        STATE_ACTIVE          = PropertyUtil.getSchemaProperty(context, "policy", "Project Template", "state_Active");
        whereActive           = "current=='" + STATE_ACTIVE + "'";
        RELATIONSHIP_DATAVAULTS  = EnoviaResourceBundle.getProperty(context, "Framework","emxFramework.Relationship.Data_Vaults", context.getSession().getLanguage());
        INTERFACE_TEMPLATE_FLDR = PropertyUtil.getSchemaProperty(context,"interface_TemplateFolder");
        ATTRIBUTE_ACCESSTYPE   = PropertyUtil.getSchemaProperty(context,"attribute_AccessType");

    }

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


    private StringList buildRelSelects()
    {
        StringList slSelects = new StringList(1);
        slSelects.add(DomainConstants.SELECT_FROM_ID);
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

    public void migrateObjects(Context context, StringList objectList) throws Exception
    {
        int migrated = 0, unconverted = 0;
        init(context);
        busSelects                 = buildSelects();
        relSelects                 = buildRelSelects();

        String[] oidsArray         = new String[objectList.size()];
        oidsArray                  = (String[])objectList.toArray(oidsArray);
        MapList mlProjectTemplates = DomainObject.getInfo(context, oidsArray, busSelects);

        // ContextUtil.pushContext(context, "creator", "", "");
        MqlUtil.mqlCommand(context, "trigger $1", "off");

        mqlLogRequiredInformationWriter("\n=============================================================" + "\n");
        try
        {
            if(!mlProjectTemplates.isEmpty())
            {
                // loop through Project Templates and get subfolders
                Iterator itrProjectTemplates = mlProjectTemplates.iterator();
                while(itrProjectTemplates.hasNext())
                {
                    Map mProjectTemplate = (Map)itrProjectTemplates.next();
                    String templateOID  = (String)mProjectTemplate.get(DomainConstants.SELECT_ID);
                    String templateName = (String)mProjectTemplate.get(DomainConstants.SELECT_NAME);
                    String templateRev  = (String)mProjectTemplate.get(DomainConstants.SELECT_REVISION);
                    DomainObject doProjectTemplate = DomainObject.newInstance(context, templateOID);

                    MapList mlFolders = doProjectTemplate.getRelatedObjects(context,
                                                         RELATIONSHIP_DATAVAULTS+","+DomainConstants.RELATIONSHIP_SUBVAULTS, // rel
                                                                            DomainConstants.QUERY_WILDCARD,   // type
                                                                            busSelects,                    // objSelect
                                                                            relSelects,                       // relSelect
                                                                            false,                            // to
                                                                            true,                             // from
                                                                            (short) 0,                        // level - expand to all levels
                                                                            null,                             // objWhere
                                                                            null,                             // relWhere
                                                                            0);                               // limit - expand all
                    if(!mlFolders.isEmpty())
                    {
                        // loop through subfolders
                        Iterator itrFolders = mlFolders.iterator();
                        error = false;
                        while(itrFolders.hasNext())
                        {
                            Map mFolder = (Map)itrFolders.next();
                            String folderOID  = (String)mFolder.get(DomainConstants.SELECT_ID);
                            String folderName = (String)mFolder.get(DomainConstants.SELECT_NAME);
                            String folderRev  = (String)mFolder.get(DomainConstants.SELECT_REVISION);
                            String parentOID    = (String)mFolder.get(DomainConstants.SELECT_FROM_ID);
                            String folderTitle  = (String)mFolder.get("attribute[" + ATTRIBUTE_TITLE + "]");

                            // check if interface "TemplateFolder" connected - All Template Folders need this
                            if(!isConnectedToTemplateFolderInterface(context, folderOID)) {
                                MqlUtil.mqlCommand(context, "modify businessobject $1 add interface $2", folderOID, INTERFACE_TEMPLATE_FLDR);
                                mqlLogRequiredInformationWriter("\n(FLDR ATTRIB)\t Object Id=" + folderOID + ",\tObject Name=" + folderName + ", Revision=" + folderRev + " >> Title=" + folderTitle);
                            }
                               // if folder attr Access Type is Specific then add owner access
                                    DomainAccess.createObjectOwnership(context, folderOID, parentOID, "", true);
                                    mqlLogRequiredInformationWriter("\n(FLDR ACCESS) \t Object Id="+folderOID+",\tObject Name="+folderName+", Revision="+folderRev+" >> Title="+folderTitle);
                        }
                    }

                    // check if interface is connected to top level Project Template
                    if(!isConnectedToTemplateFolderInterface(context, templateOID))
                    {
                       MqlUtil.mqlCommand(context, "modify businessobject $1 add interface $2", templateOID, INTERFACE_TEMPLATE_FLDR);
                       mqlLogRequiredInformationWriter("\n(TEMPLATE)\t Object Id="+templateOID+",\tObject Name="+templateName+", Revision="+templateRev);
                    }

                    if (!error)
                    {
                        loadMigratedOids(templateOID);
                        migrated++;
                    }
                    else
                    {
                        writeUnconvertedOID(templateOID);
                        unconverted++;
                    }
                    mqlLogRequiredInformationWriter("\n***(Template Complete)  "+templateOID+"  ***\n\n");

                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        finally
        {
            MqlUtil.mqlCommand(context, "trigger $1", "on");
            // ContextUtil.popContext(context);
        }

        mqlLogRequiredInformationWriter("\n=============================================================" + "\n");
    }

    public boolean isConnectedToTemplateFolderInterface (Context context, String folderOID) throws Exception
    {
        boolean bConnected = false;
        MQLCommand connectInterface  = new MQLCommand();
        String strMQLCommand = "print bus "+ folderOID +" select interface dump |";
        connectInterface.executeCommand(context,strMQLCommand);
        String strResults = connectInterface.getResult();

        if(strResults != null && !strResults.equals("null") && !strResults.equals("") && strResults.contains("TemplateFolder"))
        {
            bConnected = true;
        }

        return bConnected;
    }

}
