/*
** emxSchemaManifestBase
**
**  Copyright (c) 1992-2015 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.Context;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.input.SAXBuilder;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * The <code>emxSchemaManifestBase</code> class contains methods for installation.
 *
 * @version AEF 10.Next - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxSchemaManifestBase_mxJPO
{
    // application name
    private static final String APPLICATION_NAME = "SchemaInstaller";
    private static final String FRAMEWORK_APPLICATION_NAME = "Framework";
    private static final String SCHEMA_MAPPING_FILE = "SchemaMapping.xml";

    // admin list
    private static final String ADMIN_LIST[] = {
                                                   "store",
                                                   "vault",
                                                   "dimension",
                                                   "attribute",
                                                   "type",
                                                   "relationship",
                                                   "interface",
                                                   "format",
                                                   "person",
                                                   "group",
                                                   "role",
                                                   "association",
                                                   "expression",
                                                   "policy",
                                                   "pathtype",
                                                   "rule",
                                                   "index",
                                                   "application",
                                                   "package",
                                               };

    // Admin List will be cached here
    private Document schemaList = null;

    // Directory name will be cached here
    private String schemDefFileDir = null;

    // Schema name
    private String schemaName = null;

    // sdf objects will be cached here
    private HashMap sdfCache = new HashMap();

    // conversion manifest file
    private emxConversionManifestFile_mxJPO conMft = null;

    // version util
    private emxAppVersionUtil_mxJPO appVersionUtil = null;

    // Admin Manifest File Keys
    private static final String KEY_APPLICATION_NAME = "ApplicationName";
    private static final String KEY_SCHEMA_LIST = "SchemaList";

    // Custom Admin Manifest File Keys
    private static final String KEY_EXCLUDE = "Exclude";
    private static final String KEY_ADD = "Add";

    // All custom admin manifest files will be cached here
    private ArrayList aCustomManifest = null;

    // All Admin list will be cached here.
    private TreeMap mAdminList = new TreeMap();
	public static PrintStream fLiveUpgrade;

    /**
     * Constructor.
     *
     * Should be used while firing JPO throgh command line
     * @param context the eMatrix <code>Context</code> object
     * @param args[0] holds schema definition file name with full path
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public emxSchemaManifestBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        // throw exception if improper arguments are passed
        // Admin manifest file argument is mandatory
        if (args.length < 1)
        {
            throw (new Exception("Could not instantiate JPO:  schema list file must be passed as input parameter."));
        }

        // load admin manifest file
        loadSchemaListFile(context, new File(args[0]));

        // get schema definition directory
        // if not passed then schema definition files will be searched
        // in admin manifest file directory.
        if (args.length >= 2 && args[1].trim().length() > 0)
        {
            schemDefFileDir = args[1];
        }

        // load AppInfo.rul file
        if (args.length >= 3 && args[2].trim().length() > 0)
        {
            appVersionUtil = new emxAppVersionUtil_mxJPO(context, FRAMEWORK_APPLICATION_NAME, args[2], schemaName);
        }

        // load all the sdf files in Schema def directory
        sdfCache = loadSDFFiles(context, schemDefFileDir);

        // get conversion manifest file
        if (args.length >= 4 && args[3].trim().length() > 0)
        {
            conMft = new emxConversionManifestFile_mxJPO(context, new File(args[3]));
        }
    }

    /**
     * This method loads schema list file.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param xmlFile holds schema list file
     * @param bReloadCache if set to true it will reload the cache
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public void loadSchemaListFile(Context context, File xmlFile)
        throws Exception
    {
        // Get content
        SAXBuilder xmlBuilder = new SAXBuilder();
        xmlBuilder.setFeature("http://xml.org/sax/features/external-general-entities", false);
        xmlBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Document xmlDoc = xmlBuilder.build(xmlFile);
        schemaList = xmlDoc;

        // Get schema def directory
        schemDefFileDir = xmlFile.getParent();

        // Get application name
        Element root = schemaList.getRootElement();

        schemaName = root.getChildText(KEY_APPLICATION_NAME).trim();
    }

    /**
     * gets symbolic names
     *
     * @param context the eMatrix <code>Context</code> object
     * @param sAdminType admin type
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public ArrayList getSymbolicNames(Context context, String sAdminType, String sCustomDir)
        throws Exception
    {
        // return list from cache if exists
        if (mAdminList.containsKey(sAdminType))
        {
            return((ArrayList)mAdminList.get(sAdminType));
        }

        // Initialise Remove Map to empty
        ArrayList aRemovedNames = new ArrayList();
        // Initialise Add Map to empty
        ArrayList aAddNames = new ArrayList();
        // for each xml file in custom dir
        if (sCustomDir != null && sCustomDir.length() > 0)
        {

            // Cache custom manifest files
            if (aCustomManifest == null)
            {
                aCustomManifest = new ArrayList();
                SAXBuilder xmlBuilder = new SAXBuilder();
                xmlBuilder.setFeature("http://xml.org/sax/features/external-general-entities", false);
                xmlBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                File fCustomMfts[] = (new File(sCustomDir)).listFiles(new XMLFilter());
                for (int i = 0; fCustomMfts != null && i < fCustomMfts.length; i++)
                {
                    if (SCHEMA_MAPPING_FILE.equals(fCustomMfts[i].getName()))
                    {
                        continue;
                    }
                    Document xmlDoc = xmlBuilder.build(fCustomMfts[i]);
                    aCustomManifest.add(xmlDoc);
                }
            }

            for (int i = 0; aCustomManifest != null && i < aCustomManifest.size(); i++)
            {
                Document xmlDoc = (Document)aCustomManifest.get(i);
                // Get application name
                Element root = xmlDoc.getRootElement();
                String customSchemaName = root.getChildText(KEY_APPLICATION_NAME).trim();

                // if custom schema name maches with that of schema name
                // then
                if (schemaName.equals(customSchemaName))
                {
                    // Get SchemaList node
                    Element eSchemaList = root.getChild(KEY_SCHEMA_LIST);

                    // Get admin type node
                    Element eAdminTypeNode = eSchemaList.getChild(sAdminType);

                    // If no element found then break
                    if (eAdminTypeNode == null)
                    {
                        break;
                    }
                    // get removed admin list
                    String sRemovedNames = eAdminTypeNode.getChildText(KEY_EXCLUDE);

                    if (sRemovedNames != null && sRemovedNames.trim().length() > 0)
                    {
                        StringTokenizer st = new StringTokenizer(sRemovedNames.trim(), " \n");
                        while(st.hasMoreTokens())
                        {
                            aRemovedNames.add(st.nextToken());
                        }
                    }
                    // get added admin list
                    String sAddedNames = eAdminTypeNode.getChildText(KEY_ADD);
                    if (sAddedNames != null && sAddedNames.trim().length() > 0)
                    {
                        StringTokenizer st = new StringTokenizer(sAddedNames.trim(), " \n");
                        while(st.hasMoreTokens())
                        {
                            aAddNames.add(st.nextToken());
                        }
                    }
                    break;
                }
            }
        }

        // return Map.
        ArrayList aSymbolicNames = new ArrayList();

        // Get root
        Element root = schemaList.getRootElement();
        // Get SchemaList node
        Element eSchemaList = root.getChild(KEY_SCHEMA_LIST);
        // Get all the symbolic names for given admin type
        String sSymbolicNames = eSchemaList.getChildText(sAdminType);

        // If element found then
        if (sSymbolicNames != null)
        {
            StringTokenizer st = new StringTokenizer(sSymbolicNames.trim(), " \n");
            while(st.hasMoreTokens())
            {
                String sToken = st.nextToken().trim();
                if (!aRemovedNames.contains(sToken))
                {
                    aSymbolicNames.add(sToken);
                }
            }
        }

        aSymbolicNames.addAll(aAddNames);
        mAdminList.put(sAdminType, aSymbolicNames);
        return aSymbolicNames;
    }

    public HashMap loadSDFFiles(Context context, String sSDFDir)
        throws Exception
    {
        HashMap mSDFMap = new HashMap();
        File fAllSDFs[] = (new File(sSDFDir)).listFiles(new XMLFilter());
        for (int i = 0; fAllSDFs != null && i < fAllSDFs.length; i++)
        {
            try
            {
                emxSchemaDefinition_mxJPO sdf = new emxSchemaDefinition_mxJPO(context, fAllSDFs[i], appVersionUtil);
                String sSDFSymName = sdf.getSymbolicName(context);
                mSDFMap.put(sSDFSymName, sdf);
            }
            catch(Exception ex)
            {
            }
        }
        return mSDFMap;
    }

    public void installSchema(Context context, String sCustomManifestDir, String sCustomSDFDir)
        throws Exception
    {
        // set context application to first application.
        String sAllApps = emxInstallUtil_mxJPO.executeMQLCommand(context, "list application");
        if (sAllApps.length() > 0) {
            StringTokenizer stAllApps = new StringTokenizer(sAllApps, "\n");
            String sFirstApp = stAllApps.nextToken();
            context.setApplication(sFirstApp);
        }
        // load custom SDF files
        HashMap customSDFCache = new HashMap();
        if (sCustomSDFDir != null && sCustomSDFDir.length() > 0)
        {
            customSDFCache = loadSDFFiles(context, sCustomSDFDir);
        }

        // get status file directory
        String sStatusFileDir = emxInstallUtil_mxJPO.executeMQLCommand(context, "get env MXAPPBUILD");
        // create MQL log file if MXAPPBUILD env is set
        PrintStream fMQLLog = null;
        boolean bStatus = false;
        if (sStatusFileDir != null && sStatusFileDir.length() > 0)
        {
            String sLatestVersion = appVersionUtil.getLatestVersion(context);
            fMQLLog = new PrintStream(new FileOutputStream(sStatusFileDir + File.separator + "SchemaChangesMQL" + sLatestVersion + ".log", true));
            // get status
            String sStatus = emxInstallUtil_mxJPO.executeMQLCommand(context, "get env MXSTATUS");
            if (sStatus.equalsIgnoreCase("TRUE"))
            {
                bStatus = true;
            }
        }

        int action = emxInstallUtil_mxJPO.getActionForUnsafeChanges(context);
        //create live upgrade script
        if (sStatusFileDir != null && sStatusFileDir.length() > 0 && ((action & emxInstallUtil_mxJPO.ACTION_GENERATE_TCL)  != 0))
        {
        	fLiveUpgrade = new PrintStream(new FileOutputStream(sStatusFileDir + File.separator + "LiveUpgrade.mql" , true));
        }

        // get transaction flag
        String sTran = emxInstallUtil_mxJPO.executeMQLCommand(context, "get env MXTRANSACTION");
        boolean bTran = false;
        if (sTran.equalsIgnoreCase("TRUE"))
        {
            bTran = true;
        }

        // get version list
        ArrayList aVersions = appVersionUtil.getVersionHistory(context);

        if (bStatus)
        {
            File fCountStatus = new File(sStatusFileDir, "NoOfFiles=" + aVersions.size() + "=" + APPLICATION_NAME + schemaName.replace(' ', '_') + "=status");
            fCountStatus.createNewFile();
        }

        int nStatusCouter = 0;
        boolean bIfRollBackTran = false;
        String sLastSuccessfulVersion = null;

        // for each version
        for (int i = 0; i < aVersions.size(); i++)
        {
            // get version
            String sVersion = (String)aVersions.get(i);
            emxInstallUtil_mxJPO.println(context, "");
            emxInstallUtil_mxJPO.println(context, ">Upgrading " + sVersion + " " + schemaName);
            emxInstallUtil_mxJPO.println(context, "");

            if (fMQLLog != null)
            {
                fMQLLog.println("#############################################");
                fMQLLog.println("#Upgrading " + schemaName + " Schema : " + sVersion);
                fMQLLog.println("#############################################");
            }

            // start transaction
            if (bTran)
            {
                emxInstallUtil_mxJPO.println(context, ">Starting Transaction For " + sVersion + " " + schemaName);
                context.start(true);
            }

            if (conMft != null)
            {

                ArrayList aConversions = conMft.getPreConversions(context, sVersion);

                for (int ii = 0; aConversions != null && ii < aConversions.size(); ii++)
                {
                    String sConversion = ((String)aConversions.get(ii)).trim();
                    try
                    {
                        emxInstallUtil_mxJPO.println(context, ">" + sConversion);
                        String sConLog = emxInstallUtil_mxJPO.executeMQLCommand(context, sConversion);
                        emxInstallUtil_mxJPO.println(context, sConLog);
                    }
                    catch (Exception ex)
                    {
                        emxInstallUtil_mxJPO.println(context, "Error : " + ex.toString());
                        bIfRollBackTran = true;
                    }
                }
            }

            // for each admin type
            for (int ii = 0; ii < ADMIN_LIST.length; ii++)
            {
                // get symbolic names
                ArrayList aSymbolicNames = getSymbolicNames(context, ADMIN_LIST[ii], sCustomManifestDir);

                // if no symbolic names then skip
                if (aSymbolicNames == null || aSymbolicNames.isEmpty())
                {
                    continue;
                }

                // for each symbolic name
                for (int iii = 0; iii < aSymbolicNames.size(); iii++)
                {
                    // get symbolic name
                    String sSymbolicName  = (String)aSymbolicNames.get(iii);
                    try
                    {

                        emxSchemaDefinition_mxJPO sdf = null;
                        boolean isSDFSameAsCustom = false;
                        if (sdfCache.containsKey(sSymbolicName))
                        {
                            sdf = (emxSchemaDefinition_mxJPO)sdfCache.get(sSymbolicName);
                        }
                        else
                        {
                            if (customSDFCache.containsKey(sSymbolicName))
                            {
                                sdf = (emxSchemaDefinition_mxJPO)customSDFCache.get(sSymbolicName);
                                isSDFSameAsCustom = true;
                            }
                            else
                            {
                                // throw exception
                                throw (new Exception("Error: Could not load schema definition file for " + sSymbolicName));
                            }
                        }

                        if (!isSDFSameAsCustom && customSDFCache.containsKey(sSymbolicName))
                        {
                            emxSchemaDefinition_mxJPO customSDF = (emxSchemaDefinition_mxJPO)customSDFCache.get(sSymbolicName);
                            sdf.install(context, sVersion, fMQLLog, customSDF);
                        }
                        else
                        {
                            sdf.install(context, sVersion, fMQLLog);
                        }
                    }
                    catch(Exception ex)
                    {
                        emxInstallUtil_mxJPO.println(context, "Error : " + ex.toString());
                        bIfRollBackTran = true;
                        //throw(ex);
                    }
                }
            }

            // for each admin type
            for (int ii = 0; ii < ADMIN_LIST.length; ii++)
            {
                // install final section for all the admins
                // present in database
                if (
                       ADMIN_LIST[ii].equals("interface") ||
                       ADMIN_LIST[ii].equals("type") ||
                       ADMIN_LIST[ii].equals("relationship") ||
                       ADMIN_LIST[ii].equals("role") ||
                       ADMIN_LIST[ii].equals("group") ||
                       ADMIN_LIST[ii].equals("person") ||
                       ADMIN_LIST[ii].equals("assignment") ||
                       ADMIN_LIST[ii].equals("policy") ||
                       ADMIN_LIST[ii].equals("application")
                   )
                {
                    ArrayList aAllSymNames = emxAdminCache_mxJPO.getAllSymbolicNames(context, ADMIN_LIST[ii]);
                    for (int iii = 0; iii < aAllSymNames.size(); iii++)
                    {
                        String sSymbolicName  = (String)aAllSymNames.get(iii);
                        try
                        {

                            emxSchemaDefinition_mxJPO sdf = null;
                            boolean isSDFSameAsCustom = false;
                            if (sdfCache.containsKey(sSymbolicName))
                            {
                                sdf = (emxSchemaDefinition_mxJPO)sdfCache.get(sSymbolicName);
                            }
                            else
                            {
                                if (customSDFCache.containsKey(sSymbolicName))
                                {
                                    sdf = (emxSchemaDefinition_mxJPO)customSDFCache.get(sSymbolicName);
                                    isSDFSameAsCustom = true;
                                }
                                else
                                {
                                    continue;
                                }
                            }

                            if (!isSDFSameAsCustom && customSDFCache.containsKey(sSymbolicName))
                            {
                                emxSchemaDefinition_mxJPO customSDF = (emxSchemaDefinition_mxJPO)customSDFCache.get(sSymbolicName);
                                sdf.resolve(context, fMQLLog, customSDF);
                            }
                            else
                            {
                                sdf.resolve(context, fMQLLog);
                            }
                        }
                        catch(Exception ex)
                        {
                            emxInstallUtil_mxJPO.println(context, "Error : " + ex.toString());
                            bIfRollBackTran = true;
                            //throw(ex);
                        }
                    }
                }
            }

            if (conMft != null)
            {
                ArrayList aConversions = conMft.getPostConversions(context, sVersion);

                for (int ii = 0; aConversions != null && ii < aConversions.size(); ii++)
                {
                    String sConversion = ((String)aConversions.get(ii)).trim();
                    try
                    {
                        emxInstallUtil_mxJPO.println(context, ">" + sConversion);
                        String sConLog = emxInstallUtil_mxJPO.executeMQLCommand(context, sConversion);
                        emxInstallUtil_mxJPO.println(context, sConLog);
                    }
                    catch (Exception ex)
                    {
                        emxInstallUtil_mxJPO.println(context, "Error : " + ex.toString());
                        bIfRollBackTran = true;
                    }
                }
            }
            if (bTran)
            {
                if (bIfRollBackTran)
                {
                    // abort transaction in case of failure
                    emxInstallUtil_mxJPO.println(context, ">Aborting Transaction For " + sVersion + " " + schemaName);
                    context.abort();
                }
                else
                {
                    // commit transaction
                    emxInstallUtil_mxJPO.println(context, ">Commiting Transaction For " + sVersion + " " + schemaName);
                    context.commit();
                }
            }

            // set last successful version
            if (!bIfRollBackTran)
            {
                sLastSuccessfulVersion = sVersion;
            }

            if (bStatus)
            {
                // abort if install shield setup has been aborted
                File fAbortFile = new File(sStatusFileDir, "SetupAbort=status");
                if (fAbortFile.exists())
                {
                    return;
                }
                nStatusCouter++;
                File fCountStatus = new File(sStatusFileDir, nStatusCouter + "=" + sVersion + "=" + APPLICATION_NAME + schemaName.replace(' ', '_') + "=status");
                fCountStatus.createNewFile();
            }
        }

        // register last successful version
        if (sLastSuccessfulVersion != null)
        {
            emxAppVersionUtil_mxJPO.setSchemaVersion(context, schemaName, sLastSuccessfulVersion);
        }

        if (bIfRollBackTran)
        {
            emxInstallUtil_mxJPO.println(context, "");
            emxInstallUtil_mxJPO.println(context, "1");

            if (bStatus)
            {
                File fCountStatus = new File(sStatusFileDir, "Result=Failure=" + APPLICATION_NAME + schemaName.replace(' ', '_') + "=status");
                fCountStatus.createNewFile();
            }
        }
        else
        {
            emxInstallUtil_mxJPO.println(context, "");
            emxInstallUtil_mxJPO.println(context, "0");
            if (bStatus)
            {
                File fCountStatus = new File(sStatusFileDir, "Result=Success=" + APPLICATION_NAME + schemaName.replace(' ', '_') + "=status");
                fCountStatus.createNewFile();
            }
        }

        if (fMQLLog != null)
        {
            fMQLLog.close();
        }
        if (fLiveUpgrade != null)
        {
        	fLiveUpgrade.close();
        }
    }

    public void installSchema(Context context, String[] args)
        throws Exception
    {
        // get MODE
        String sMode = emxInstallUtil_mxJPO.executeMQLCommand(context, "get env MXMODE");
        // no schema install if it is service pack
        if (sMode.equals("SERVICE_PACK")) {
            return;
        }

        String sCustomManifestDir = null;
        String sCustomSDFDir = null;
        // get custom manifest file directory
        if (args != null && args.length >= 1)
        {
            sCustomManifestDir = args[0];
        }
        // get custom sdf dir
        if (args != null && args.length >= 2)
        {
            sCustomSDFDir = args[1];
        }

        // install schema
        installSchema(context, sCustomManifestDir, sCustomSDFDir);
    }

    private class XMLFilter implements FilenameFilter
    {
        public boolean accept(File dir, String name)
        {
            if (name.endsWith(".xml"))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }
}

