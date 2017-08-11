/*
** emxAppVersionUtilBase
**
**  Copyright (c) 1992-2015 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/


import java.util.TreeMap;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.ResourceBundle;
import java.util.Enumeration;


import java.text.MessageFormat;

import matrix.db.Context;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.jsystem.util.StringUtils;

/**
 * The <code>emxAppVersionUtilBase</code> class contains utility methods.
 *
 * @version AEF 10.Next - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxAppVersionUtilBase_mxJPO
{
    // System information hook
    private static final String SYSTEM_INFORMATION_PROGRAM = "eServiceSystemInformation.tcl";

    // program that stores all the possible versions
    private static final String APPINFO = "AppInfo.rul";

    // final version
    private static final String FINAL_VERSION = "FINAL";

    // Application install version history will be cached here
    private ArrayList aVersions = null;
    private ArrayList aDeltaVersions = null;
    private ArrayList dbVersionList = null;

    // Application Name
    private String sApplication = null;

    // external to internal version mapping
    private TreeMap mVersionMap = null;

    // schema name
    private String schemaName = null;
    private String schemaVersion = null;

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param sAppInfoFile AppInfo.rul file name
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    @Deprecated
    public emxAppVersionUtilBase_mxJPO (Context context, String sApplicationName, String sAppInfoFile)
        throws Exception
    {
        loadAppVersionHistory(context, sApplicationName, sAppInfoFile, true);
    }

    public emxAppVersionUtilBase_mxJPO (Context context, String sApplicationName, String sAppInfoFile, String schemaName)
        throws Exception
    {
        this.schemaName = schemaName;
        loadAppVersionHistory(context, sApplicationName, sAppInfoFile, true);
    }

    /**
     * loads list of all the versions of application in cache
     *
     * @param context the eMatrix <code>Context</code> object
     * @sApplicationName application name
     * @sAppInfoFile app info file name with path
     * @bReload weather to reload.
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public void loadAppVersionHistory(Context context, String sApplicationName, String sAppInfoFile, boolean bReload)
        throws Exception
    {
        if (bReload || aVersions == null)
        {
            sApplication = sApplicationName;
            // create application version map
            aVersions = new ArrayList();
            if (sAppInfoFile != null && sAppInfoFile.length() != 0)
            {
                // first read from AppInfo.rul file
                String sAppInfoFileTCL = sAppInfoFile.replace('\\', '/');
                String sVersions = emxInstallUtil_mxJPO.executeMQLCommand(context, "exec program emxIniFileReader.tcl \"" + sAppInfoFileTCL + "\" \"VERSION_LIST\"");
                StringTokenizer stVersions = new StringTokenizer(sVersions, " ");
                while(stVersions.hasMoreTokens())
                {
                    aVersions.add(stVersions.nextToken().trim());
                }
            }

            // Get all the version information from database
            String sCode = emxInstallUtil_mxJPO.executeMQLCommand(context, "print program \"" + APPINFO + "\" select code dump");

            StringTokenizer lines = new StringTokenizer(sCode, "\n");

            // for each line in the code
            dbVersionList = new ArrayList();
            while (lines.hasMoreTokens())
            {
                String sLine = lines.nextToken().trim();

                // skip blank and comment lines
                if (sLine.length() == 0 || sLine.startsWith("#"))
                {
                    continue;
                }

                dbVersionList.add(sLine);
            }

            // merge db versions in appinfo version
            int iLastAppInfoIndex = -1;
            for (int ii = 0; ii < dbVersionList.size(); ii++)
            {
                String sdbVersion = (String)dbVersionList.get(ii);

                int iAppInfoIndex = aVersions.indexOf(sdbVersion);
                if (iLastAppInfoIndex >= 0 && iAppInfoIndex < 0)
                {
                    aVersions.add(iLastAppInfoIndex + 1, sdbVersion);
                    iAppInfoIndex = iLastAppInfoIndex + 1;
                }
                if (iAppInfoIndex >= 0)
                {
                    iLastAppInfoIndex = iAppInfoIndex;
                }
            }

            // Get external versions and their mapping to internal versions
            mVersionMap = new TreeMap();
            String strMatch = "emxFramework.HelpAbout.Version.";
            String aTemp = "";
            ResourceBundle sProp = EnoviaResourceBundle.getBundle(context, "emxSystem", context.getLocale().getLanguage());
            Enumeration enum1 = sProp.getKeys();
            while(enum1.hasMoreElements()){
                aTemp = (String)enum1.nextElement();
                int index = aTemp.indexOf(strMatch);
                if (index >= 0 ){
                    mVersionMap.put(sProp.getString(aTemp), aTemp.substring(strMatch.length()));
                }
            }
            //Get only delta versions
            aDeltaVersions = aVersions;
            String sSchemaVersion = emxInstallUtil_mxJPO.executeMQLCommand(context, "print program \"" + SYSTEM_INFORMATION_PROGRAM + "\" select property[appSchema" + schemaName.replace(" ", "") + "].value dump");
            if (sSchemaVersion != null || sSchemaVersion.trim().length() > 0) {
                int index = aVersions.indexOf(sSchemaVersion);
                if (index < 0) {
                    int HFindex = sSchemaVersion.indexOf(".HF");
                    String sBaseVersion = sSchemaVersion;
                    if(HFindex > 0 ) {
                        sBaseVersion = sSchemaVersion.substring(0, HFindex);
                    }
                    index = aVersions.indexOf(sBaseVersion);
                }
                if (index >= 0) {
                    aDeltaVersions = new ArrayList(aVersions.subList(index + 1, aVersions.size()));
                }
                schemaVersion = sSchemaVersion;
            }
        }
    }

    /**
     * gets list of all the versions of application
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if the operation fails
     * @returns ArrayList list of versions in sequence
     * @since AEF 10.Next
     */
    public ArrayList getVersionHistory(Context context)
        throws Exception
    {
        return aDeltaVersions;
    }

    /**
     * gets latest version of an application
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if the operation fails
     * @returns String latest version
     * @since AEF 10.Next
     */
    public String getLatestVersion(Context context)
        throws Exception
    {
        // get all the versions of an application
        if (aVersions == null || aVersions.isEmpty())
        {
            return null;
        }

        // return last version
        return ((String)aVersions.get(aVersions.size() - 1));
    }


    /**
     * set Schema Version
     *
     * @param context the eMatrix <code>Context</code> object
     * @param schema name of the schema
     * @param version version of the schema
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public static void setSchemaVersion(Context context, String sSchemaName, String sVersion)
        throws Exception
    {
        // remove all the spaces in the name
        String sNewSchemaName = StringUtils.replaceAll(sSchemaName, " ", "");

        // set property on system information program
        String sCmd = "modify program \"" + SYSTEM_INFORMATION_PROGRAM + "\" " +
                      "property appSchema" + sNewSchemaName + " value \"" + sVersion + "\" ";

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy h:mm a", Locale.US);
        String sCode = emxInstallUtil_mxJPO.executeMQLCommand(context, "print program \"" + SYSTEM_INFORMATION_PROGRAM + "\" select code dump");

        sCode += "\nSchema" + sNewSchemaName + " " + sVersion + " " + sdf.format(new Date()) + " SCHEMA";

        sCmd += "code \"" + sCode + "\"";

        emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
    }

    private int getVersionIndex(Context context, String sAdminType, String sAdminName, String sVersion)
        throws Exception
    {
        int index = aVersions.indexOf(sVersion);
        if (index < 0 && mVersionMap.containsKey(sVersion))
        {
            String internalVersion = (String)mVersionMap.get(sVersion);
            index = aVersions.indexOf(internalVersion);
        }

        if (index < 0)
        {
            int HFindex = sVersion.indexOf(".HF");
            if (HFindex > 0) {
                String sBaseVersion = sVersion.substring(0, HFindex);
                //System.out.println("Considering Base Version " + sBaseVersion + " on " + sAdminType + " " + sAdminName + " instead of " + sVersion);
                index = aVersions.indexOf(sBaseVersion);
                if (index < 0 && mVersionMap.containsKey(sBaseVersion))
                {
                    String internalBaseVersion = (String)mVersionMap.get(sBaseVersion);
                    index = aVersions.indexOf(internalBaseVersion);
                }
            }
            if (index < 0) {
                throw (new Exception("Version " + sVersion + " not found on " + sAdminType + " " + sAdminName));
            }
        }

        return index;
    }

    public String getInternalVersion(String sExternalVersion)
        throws Exception
    {
        String sInternalVersion = sExternalVersion;
        if (mVersionMap.containsKey(sExternalVersion))
        {
            sInternalVersion = (String)mVersionMap.get(sExternalVersion);
        }
        return sInternalVersion;
    }

    /**
     * compares two versions
     *
     * @param context the eMatrix <code>Context</code> object
     * @sApplicationName application versions to be compared
     * @param sVersion1 first version
     * @param sVersion2 second version
     * @throws Exception if the operation fails
     * @returns int > 0 if sVersion1 > sVersion2, = 0 if sVersion1 = sVersion2, < 0 if sVersion1 < sVersion2
     * @since AEF 10.Next
     */
    public int compareVersion(Context context, String sAdminType, String sAdminName, String sVersion1, String sVersion2)
        throws Exception
    {
        // If any of the version is FINAL then
        // that is the latest version
        if (sVersion1.equals(FINAL_VERSION) && sVersion2.equals(FINAL_VERSION))
        {
            return 0;
        }
        else if (sVersion1.equals(FINAL_VERSION))
        {
            return 1;
        }
        else if (sVersion2.equals(FINAL_VERSION))
        {
            return -1;
        }

        int index1 = getVersionIndex(context, sAdminType, sAdminName, sVersion1);
        int index2 = getVersionIndex(context, sAdminType, sAdminName, sVersion2);

        // > 0 if sVersion1 > sVersion2,
        // = 0 if sVersion1 = sVersion2,
        // < 0 if sVersion1 < sVersion2
        return (index1 - index2);
    }

    public int compareDBVersion(Context context, String sVersion1, String sVersion2) throws Exception {
        int index1 = dbVersionList.indexOf(sVersion1);
        if (index1 < 0) {
            throw new Exception(sVersion1 + " not found");
        }
        int index2 = dbVersionList.indexOf(sVersion2);
        if (index2 < 0) {
            throw new Exception(sVersion2 + " not found");
        }
        return (index1 - index2);
    }

        public String getSchemaVersion() {
            return schemaVersion;

        }

}



