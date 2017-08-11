/*
** emxAdminCacheBase
**
**  Copyright (c) 1992-2015 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import java.util.TreeMap;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.text.MessageFormat;

import matrix.db.Context;

import com.matrixone.jsystem.util.StringUtils;

/**
 * The <code>emxSchemaDefinitionFileBase</code> class contains methods for ActionLinkAccess.
 *
 * @version AEF 10.Next - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxAdminCacheBase_mxJPO
{
    // Symbolic names hook
    private static final String SCHEMA_MAPPING_PROGRAM = "eServiceSchemaVariableMapping.tcl";

    // symbolic name cache
    private static TreeMap adminCache = null;
    // adminCache keys
    private static final String KEY_ACTUAL_NAME = "ActualName";
    private static final String KEY_VERSION = "Version";
    private static final String KEY_STATES = "States";
    private static final String KEY_APPLICATION = "Application";
    private static final String KEY_UNRESOLVED_REFERENCES = "Unresolved References";

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args[0] holds schema definition file name with full path
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public emxAdminCacheBase_mxJPO (Context context, String[] args)
        throws Exception
    {
    }

    /**
     * Loads Symbolic Names map
     *
     * @param context             the eMatrix <code>Context</code> object
     * @throws Exception          if operation fails
     * @since                     AEF 10.Next
     */
    public static synchronized void loadAdminCache(Context context) throws Exception
    {
        if (adminCache == null)
        {
            adminCache = new TreeMap();
        }

        // Exception if eServiceSchemaVariableMapping.tcl doesn't exists
        String cmd = "list program \"" + SCHEMA_MAPPING_PROGRAM + "\"";
        if (!SCHEMA_MAPPING_PROGRAM.equals(emxInstallUtil_mxJPO.executeMQLCommand(context, cmd)))
        {
            throw (new Exception(SCHEMA_MAPPING_PROGRAM + " does not exists"));
        }

        // Get to side admin connected of the property
        cmd = "print program '" + SCHEMA_MAPPING_PROGRAM + "' select property.to";
        String sCmdOutput = emxInstallUtil_mxJPO.executeMQLCommand(context, cmd);
        StringTokenizer tokenizerCmdOutput = new StringTokenizer(sCmdOutput, "\n");

        // loop through each property
        boolean firstTokenSkipped = false;
        MessageFormat mf = new MessageFormat("property[{0}].to = {1}");
        while (tokenizerCmdOutput.hasMoreTokens())
        {
            if (!firstTokenSkipped)
            {
                firstTokenSkipped = true;
                continue;
            }
            // Get Line
            String sLine = tokenizerCmdOutput.nextToken().trim();
            if (sLine.length() == 0)
            {
                continue;
            }
            Object[] objs = null;
            try
            {
                objs = mf.parse(sLine);
            }
            catch(Exception ex)
            {
                continue;
            }

            // Get property name
            String sPropName = ((String)objs[0]).trim();
            String sAdmin = ((String)objs[1]).trim();

            // skip if admin object doesn't exists
            if (sAdmin.length() == 0)
            {
                continue;
            }

            // Get admin type
            String sAdminType = sAdmin.substring(0, sAdmin.indexOf(' '));
            if (sAdminType.equals("att"))
            {
                sAdminType = "attribute";
            }
            else if (sAdminType.equals("lattice"))
            {
                sAdminType = "vault";
            }
            // Get admin name
            String sAdminName = sAdmin.substring(sAdmin.indexOf(' ') + 1);

            // Get admin type map
            TreeMap hAdminType = (TreeMap)emxInstallUtil_mxJPO.getKeyValue(adminCache, sAdminType, TreeMap.class);
            // Get admin info map
            TreeMap hAdminInfo = (TreeMap)emxInstallUtil_mxJPO.getKeyValue(hAdminType, sPropName, TreeMap.class);

            // put property and respective name.
            hAdminInfo.put(KEY_ACTUAL_NAME, sAdminName);
        }
    }

    /**
     * Gets admin object name from symbolic name
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sAdminType          type of admin object
     * @param sSymbolicName       symbolic name
     * @throws exception
     * @returns name of the admin object
     * @since                     AEF 10.Next
     */
    public static String getName(Context context, String sAdminType, String sSymbolicName)
        throws Exception
    {
        String name = null;

        // load symbolic names first time
        if (adminCache == null)
        {
            loadAdminCache(context);
        }

        // get admin map
        TreeMap hAdminMap = (TreeMap)adminCache.get(sAdminType);
        if (hAdminMap != null)
        {
             // get admin info cache
             TreeMap hAdminInfo = (TreeMap)hAdminMap.get(sSymbolicName);
             if (hAdminInfo != null)
             {
                 name = (String)hAdminInfo.get(KEY_ACTUAL_NAME);
             }
        }

        // return name
        return name;
    }

    /**
     * Gets admin object name from symbolic name
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sSymbolicName       symbolic name
     * @throws exception
     * @returns name of the admin object
     * @since                     AEF 10.Next
     */
    public static String getName(Context context, String sSymbolicName)
        throws Exception
    {
        String sAdminType = sSymbolicName.substring(0, sSymbolicName.indexOf('_'));

        return(getName(context, sAdminType, sSymbolicName));
    }

    /**
     * Gets admin object version from symbolic name
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sSymbolicName       symbolic name
     * @throws exception
     * @returns version of the admin object
     * @since                     AEF 10.Next
     */
    public static String getVersion(Context context, String sSymbolicName)
        throws Exception
    {
        String sAdminType = sSymbolicName.substring(0, sSymbolicName.indexOf('_'));

        return(getVersion(context, sAdminType, sSymbolicName));
    }

    /**
     * Gets admin object version from symbolic name
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sAdminType          type of admin object
     * @param sSymbolicName       symbolic name
     * @throws exception
     * @returns version of the admin object
     * @since                     AEF 10.Next
     */
    public static String getVersion(Context context, String sAdminType, String sSymbolicName)
        throws Exception
    {
        String version = null;

        // load symbolic names first time
        if (adminCache == null)
        {
            loadAdminCache(context);
        }

        // get admin map
        TreeMap hAdminMap = (TreeMap)adminCache.get(sAdminType);
        if (hAdminMap != null)
        {
             // get admin info cache
             TreeMap hAdminInfo = (TreeMap)hAdminMap.get(sSymbolicName);
             if (hAdminInfo != null)
             {
                 version = (String)hAdminInfo.get(KEY_VERSION);
                 if (version == null)
                 {
                     String name = (String)hAdminInfo.get(KEY_ACTUAL_NAME);
                     if (sAdminType.equalsIgnoreCase("association"))
                     {
                         sAdminType = "user";
                     }
                     String sCmd = "print " + sAdminType + " \"" + name + "\" select property[version].value property[application].value dump |";
                     String sPropInfo = emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                     StringTokenizer stPropInfo = new StringTokenizer(sPropInfo, "|");
                     if (stPropInfo.countTokens() == 2)
                     {
                         version = stPropInfo.nextToken();
                         if (version == null || version.length() == 0)
                         {
                             throw (new Exception("Error: version information missing on " + sSymbolicName));
                         }
                         hAdminInfo.put(KEY_VERSION, version);
                         String application = stPropInfo.nextToken();
                         hAdminInfo.put(KEY_APPLICATION, application);
                     }
                     else
                     {
                         if (sPropInfo.length() == 0)
                         {
                             throw (new Exception("Error: version information missing on " + sSymbolicName));
                         }
                         else
                         {
                             sCmd = "print " + sAdminType + " \"" + name + "\" select property[version].value dump |";
                             version = emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                             if (version == null || version.length() == 0)
                             {
                                 throw (new Exception("Error: version information missing on " + sSymbolicName));
                             }
                             hAdminInfo.put(KEY_VERSION, version);
                             hAdminInfo.put(KEY_APPLICATION, "");
                         }
                     }
                 }
             }
        }

        // return version
        return version;
    }

    /**
     * Gets admin object application from symbolic name
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sSymbolicName       symbolic name
     * @throws exception
     * @returns version of the admin object
     * @since                     AEF 10.Next
     */
    public static String getApplication(Context context, String sSymbolicName)
        throws Exception
    {
        String sAdminType = sSymbolicName.substring(0, sSymbolicName.indexOf('_'));

        return(getApplication(context, sAdminType, sSymbolicName));
    }

    /**
     * Gets admin object application from symbolic name
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sAdminType          type of admin object
     * @param sSymbolicName       symbolic name
     * @throws exception
     * @returns version of the admin object
     * @since                     AEF 10.Next
     */
    public static String getApplication(Context context, String sAdminType, String sSymbolicName)
        throws Exception
    {
        String application = null;

        // load symbolic names first time
        if (adminCache == null)
        {
            loadAdminCache(context);
        }

        // get admin map
        TreeMap hAdminMap = (TreeMap)adminCache.get(sAdminType);
        if (hAdminMap != null)
        {
             // get admin info cache
             TreeMap hAdminInfo = (TreeMap)hAdminMap.get(sSymbolicName);
             if (hAdminInfo != null)
             {
                 application = (String)hAdminInfo.get(KEY_APPLICATION);
                 if (application == null)
                 {
                     String name = (String)hAdminInfo.get(KEY_ACTUAL_NAME);
                     if (sAdminType.equalsIgnoreCase("association"))
                     {
                         sAdminType = "user";
                     }
                     String sCmd = "print " + sAdminType + " \"" + name + "\" select property[version].value property[application].value dump |";
                     String sPropInfo = emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                     StringTokenizer stPropInfo = new StringTokenizer(sPropInfo, "|");
                     if (stPropInfo.countTokens() == 2)
                     {
                         String version = stPropInfo.nextToken();
                         if (version == null || version.length() == 0)
                         {
                             throw (new Exception("Error: version information missing on " + sSymbolicName));
                         }
                         hAdminInfo.put(KEY_VERSION, version);
                         application = stPropInfo.nextToken();
                         hAdminInfo.put(KEY_APPLICATION, application);
                     }
                     else
                     {
                         if (sPropInfo.length() == 0)
                         {
                             throw (new Exception("Error: version information missing on " + sSymbolicName));
                         }
                         else
                         {
                             sCmd = "print " + sAdminType + " \"" + name + "\" select property[application].value dump |";
                             application = emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                             if (application != null && application.length() != 0)
                             {
                                 throw (new Exception("Error: version information missing on " + sSymbolicName));
                             }
                             hAdminInfo.put(KEY_VERSION, sPropInfo);
                             hAdminInfo.put(KEY_APPLICATION, application);
                         }
                     }
                 }
             }
        }

        // return application
        return application;
    }

    /**
     * Puts admin object version from symbolic name
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sAdminType          type of admin object
     * @param sSymbolicName       symbolic name
     * @param sVersion            version
     * @throws exception
     * @returns version of the admin object
     * @since                     AEF 10.Next
     */
    public static void putVersion(Context context, String sAdminType, String sSymbolicName, String sVersion)
        throws Exception
    {
        // load symbolic names first time
        if (adminCache == null)
        {
            loadAdminCache(context);
        }

        // get admin map
        TreeMap hAdminMap = (TreeMap)emxInstallUtil_mxJPO.getKeyValue(adminCache, sAdminType, TreeMap.class);

        // get admin info cache
        TreeMap hAdminInfo = (TreeMap)emxInstallUtil_mxJPO.getKeyValue(hAdminMap, sSymbolicName, TreeMap.class);

        // put name into cache
        hAdminInfo.put(KEY_VERSION, sVersion);
    }

    /**
     * Puts admin object version from symbolic name
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sSymbolicName       symbolic name
     * @param sVersion            version
     * @throws exception
     * @returns version of the admin object
     * @since                     AEF 10.Next
     */
    public static void putVersion(Context context, String sSymbolicName, String sVersion)
        throws Exception
    {
        String sAdminType = sSymbolicName.substring(0, sSymbolicName.indexOf('_'));
        putVersion(context, sAdminType, sSymbolicName, sVersion);
    }

    /**
     * Puts admin object UnresolvedReferences from symbolic name
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sAdminType          type of admin object
     * @param sSymbolicName       symbolic name
     * @param sUnresolvedReferences            unresolved References
     * @throws exception
     * @returns version of the admin object
     * @since                     AEF 10.Next
     */
    public static void putUnresolvedReferences(Context context, String sAdminType, String sSymbolicName, ArrayList aUnresolvedReferences)
        throws Exception
    {
        // load symbolic names first time
        if (adminCache == null)
        {
            loadAdminCache(context);
        }

        // get admin map
        TreeMap hAdminMap = (TreeMap)emxInstallUtil_mxJPO.getKeyValue(adminCache, sAdminType, TreeMap.class);

        // get admin info cache
        TreeMap hAdminInfo = (TreeMap)emxInstallUtil_mxJPO.getKeyValue(hAdminMap, sSymbolicName, TreeMap.class);

        // put name into cache
        hAdminInfo.put(KEY_UNRESOLVED_REFERENCES, aUnresolvedReferences);
    }

    /**
     * Gets admin object unresolvedReferences from symbolic name
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sSymbolicName       symbolic name
     * @throws exception
     * @returns version of the admin object
     * @since                     AEF 10.Next
     */
    public static ArrayList getUnresolvedReferences(Context context, String sSymbolicName)
        throws Exception
    {
        String sAdminType = sSymbolicName.substring(0, sSymbolicName.indexOf('_'));

        return(getUnresolvedReferences(context, sAdminType, sSymbolicName));
    }

    /**
     * Gets admin object unresolvedReferences from symbolic name
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sAdminType          type of admin object
     * @param sSymbolicName       symbolic name
     * @throws exception
     * @returns version of the admin object
     * @since                     AEF 10.Next
     */
    public static ArrayList getUnresolvedReferences(Context context, String sAdminType, String sSymbolicName)
        throws Exception
    {
        ArrayList aUnresolvedReferences = new ArrayList();

        // load symbolic names first time
        if (adminCache == null)
        {
            loadAdminCache(context);
        }

        // get admin map
        TreeMap hAdminMap = (TreeMap)adminCache.get(sAdminType);
        if (hAdminMap != null)
        {
             // get admin info cache
             TreeMap hAdminInfo = (TreeMap)hAdminMap.get(sSymbolicName);
             if (hAdminInfo != null)
             {
                 aUnresolvedReferences = (ArrayList)hAdminInfo.get(KEY_UNRESOLVED_REFERENCES);
                 if (aUnresolvedReferences == null)
                 {
                     aUnresolvedReferences = new ArrayList();
                     String name = (String)hAdminInfo.get(KEY_ACTUAL_NAME);
                     if (sAdminType.equalsIgnoreCase("association"))
                     {
                         sAdminType = "user";
                     }
                     String sCmd = "print " + sAdminType + " \"" + name + "\" select property.name dump |";
                     String sPropInfo = emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                     StringTokenizer stPropInfo = new StringTokenizer(sPropInfo, "|");

                     while (stPropInfo.hasMoreTokens())
                     {
                         String sPropName = stPropInfo.nextToken();
                         if (sPropName.startsWith("mxSym_"))
                         {
                             aUnresolvedReferences.add(sPropName.substring(6));
                         }
                     }

                     hAdminInfo.put(KEY_UNRESOLVED_REFERENCES, aUnresolvedReferences);
                 }
             }
        }

        // return unresolvedReferences
        return aUnresolvedReferences;
    }

    /**
     * Gets all symbolic names in the cache for given admin types
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sAdminType          type of admin object
     * @throws exception
     * @returns ArrayList         list of all the symbolic names in cache
     * @since                     AEF 10.Next
     */
    public static ArrayList getAllSymbolicNames(Context context, String sAdminType)
        throws Exception
    {
        ArrayList aAllSymNames = new ArrayList();

        // load symbolic names first time
        if (adminCache == null)
        {
            loadAdminCache(context);
        }

        // get admin map
        TreeMap hAdminMap = (TreeMap)adminCache.get(sAdminType);
        if (hAdminMap != null)
        {
             // get all the keys
             aAllSymNames = new ArrayList(hAdminMap.keySet());
        }

        // return list
        return aAllSymNames;
    }

    /**
     * Puts admin object UnresolvedReferences from symbolic name
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sSymbolicName       symbolic name
     * @param sUnresolvedReferences            UnresolvedReferences
     * @throws exception
     * @returns version of the admin object
     * @since                     AEF 10.Next
     */
    public static void putUnresolvedReferences(Context context, String sSymbolicName, ArrayList aUnresolvedReferences)
        throws Exception
    {
        String sAdminType = sSymbolicName.substring(0, sSymbolicName.indexOf('_'));
        putUnresolvedReferences(context, sAdminType, sSymbolicName, aUnresolvedReferences);
    }

    /**
     * Puts admin object application from symbolic name
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sAdminType          type of admin object
     * @param sSymbolicName       symbolic name
     * @param sApplication        application
     * @throws exception
     * @returns application of the admin object
     * @since                     AEF 10.Next
     */
    public static void putApplication(Context context, String sAdminType, String sSymbolicName, String sApplication)
        throws Exception
    {
        // load symbolic names first time
        if (adminCache == null)
        {
            loadAdminCache(context);
        }

        // get admin map
        TreeMap hAdminMap = (TreeMap)emxInstallUtil_mxJPO.getKeyValue(adminCache, sAdminType, TreeMap.class);

        // get admin info cache
        TreeMap hAdminInfo = (TreeMap)emxInstallUtil_mxJPO.getKeyValue(hAdminMap, sSymbolicName, TreeMap.class);

        // put name into cache
        hAdminInfo.put(KEY_APPLICATION, sApplication);
    }

    /**
     * Puts admin object application from symbolic name
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sSymbolicName       symbolic name
     * @param sApplication        application
     * @throws exception
     * @returns application of the admin object
     * @since                     AEF 10.Next
     */
    public static void putApplication(Context context, String sSymbolicName, String sApplication)
        throws Exception
    {
        String sAdminType = sSymbolicName.substring(0, sSymbolicName.indexOf('_'));
        putApplication(context, sAdminType, sSymbolicName, sApplication);
    }

    /**
     * Gets state name
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sPolicySymbolicName symbolic name of policy
     * @param sStateSymbolicName  symbolic name of state
     * @throws exception
     * @returns state name
     * @since                     AEF 10.Next
     */
    public static boolean isStateExists(Context context, String sPolicySymbolicName, String sStateName)
        throws Exception
    {
        // load symbolic names first time
        if (adminCache == null)
        {
            loadAdminCache(context);
        }

        // get admin map
        TreeMap hPolicyMap = (TreeMap)adminCache.get("policy");
        if (hPolicyMap != null)
        {
             // get admin info cache
             TreeMap hPolicyInfo = (TreeMap)hPolicyMap.get(sPolicySymbolicName);
             if (hPolicyInfo != null)
             {
                 TreeMap hStates = (TreeMap)emxInstallUtil_mxJPO.getKeyValue(hPolicyInfo, KEY_STATES, TreeMap.class);
                 if (hStates.isEmpty())
                 {
                     String sPolicyName = getName(context, sPolicySymbolicName);
                     String sCmd = "print policy" + " \"" + sPolicyName + "\" select property.name dump |";
                     String sProperty = emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                     StringTokenizer stProperty = new StringTokenizer(sProperty, "|");
                     sCmd = "print policy" + " \"" + sPolicyName + "\" select property.value dump |";
                     String sValues = emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                     StringTokenizer stValue = new StringTokenizer(sValues, "|");
                     sCmd = "print policy" + " \"" + sPolicyName + "\" select state dump |";
                     String sStateNames = emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                     StringTokenizer stStateNames = new StringTokenizer(sStateNames, "|");
                     ArrayList aStates = new ArrayList();
                     while(stStateNames.hasMoreTokens())
                     {
                         aStates.add(stStateNames.nextToken());
                     }
                     while (stProperty.hasMoreTokens())
                     {
                         String sProp = stProperty.nextToken();
                         String sValue = stValue.nextToken();
                         if (sProp.startsWith("state_") && aStates.contains(sValue))
                         {
                             hStates.put(sProp, sValue);
                         }
                     }
                 }

                 ArrayList aStateNames = new ArrayList(hStates.values());

                 return (aStateNames.contains(sStateName));
             }
        }

        // return false
        return false;
    }

    /**
     * Gets state name
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sPolicySymbolicName symbolic name of policy
     * @param sStateSymbolicName  symbolic name of state
     * @throws exception
     * @returns state name
     * @since                     AEF 10.Next
     */
    public static String getStateName(Context context, String sPolicySymbolicName, String sStateSymbolicName)
        throws Exception
    {
        String name = null;

        // load symbolic names first time
        if (adminCache == null)
        {
            loadAdminCache(context);
        }

        // get admin map
        TreeMap hPolicyMap = (TreeMap)adminCache.get("policy");
        if (hPolicyMap != null)
        {
             // get admin info cache
             TreeMap hPolicyInfo = (TreeMap)hPolicyMap.get(sPolicySymbolicName);
             if (hPolicyInfo != null)
             {
                 TreeMap hStates = (TreeMap)emxInstallUtil_mxJPO.getKeyValue(hPolicyInfo, KEY_STATES, TreeMap.class);
                 if (!hStates.containsKey(sStateSymbolicName))
                 {
                     String sPolicyName = (String)hPolicyInfo.get(KEY_ACTUAL_NAME);
                     String sCmd = "print policy" + " \"" + sPolicyName + "\" select property[" + sStateSymbolicName + "].value dump";
                     name = emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                     sCmd = "print policy" + " \"" + sPolicyName + "\" select state dump |";
                     String sStateNames = emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                     StringTokenizer stStateNames = new StringTokenizer(sStateNames, "|");
                     ArrayList aStates = new ArrayList();
                     while(stStateNames.hasMoreTokens())
                     {
                         aStates.add(stStateNames.nextToken());
                     }
                     if (aStates.contains(name))
                     {
                         hStates.put(sStateSymbolicName, name);
                     }
                     else
                     {
                         name = "";
                     }
                 }
                 else
                 {
                     name = (String)hStates.get(sStateSymbolicName);
                 }
             }
        }

        // return name
        return name;
    }

    public static void refreshStateSymbolicNames(Context context, String sPolicySymbolicName, boolean bUpdateDB) throws Exception {
    	refreshStateSymbolicNames(context, sPolicySymbolicName, bUpdateDB, false);
    }

    /**
     * refreshes state symbolic name cache
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sPolicySymbolicName symbolic name of policy
     * @param bUpdateDB           if true then updates database.
     * @throws exception
     * @since                     AEF 10.Next
     */
    public static void refreshStateSymbolicNames(Context context, String sPolicySymbolicName, boolean bUpdateDB, boolean isDeferred)
        throws Exception
    {
    	int action = emxInstallUtil_mxJPO.getActionForUnsafeChanges(context);
        // load symbolic names first time
        if (adminCache == null)
        {
            loadAdminCache(context);
        }

        // get admin map
        TreeMap hPolicyMap = (TreeMap)adminCache.get("policy");
        if (hPolicyMap != null)
        {
             // get admin info cache
             TreeMap hPolicyInfo = (TreeMap)hPolicyMap.get(sPolicySymbolicName);
             if (hPolicyInfo != null)
             {
                 // get policy name
                 String name = (String)hPolicyInfo.get(KEY_ACTUAL_NAME);

                 // get all the properties on the policy
                 String sCmd = "print policy" + " \"" + name + "\" select property.name dump |";
                 String sStatePropNames = emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                 StringTokenizer stStatePropNames = new StringTokenizer(sStatePropNames, "|");

                 // get all the property values
                 sCmd = "print policy" + " \"" + name + "\" select property.value dump |";
                 String sStatePropValues = emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                 StringTokenizer stStatePropValues = new StringTokenizer(sStatePropValues, "|");
                 TreeMap hStates = new TreeMap();

                 // update state properties into db if flag is set.
                 if (bUpdateDB)
                 {
                     // get all the states
                     sCmd = "print policy" + " \"" + name + "\" select state dump |";
                     String sStateNames = emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                     StringTokenizer stStateNames = new StringTokenizer(sStateNames, "|");
                     ArrayList aStates = new ArrayList();
                     while(stStateNames.hasMoreTokens())
                     {
                         aStates.add(stStateNames.nextToken());
                     }

                     // delete properties for deleted states
                     while(stStatePropNames.hasMoreTokens() && stStatePropValues.hasMoreTokens())
                     {
                         String sStatePropName = stStatePropNames.nextToken();
                         String sStatePropValue = stStatePropValues.nextToken();
                         if (sStatePropName.startsWith("state_") && !aStates.contains(sStatePropValue))
                         {
                             sCmd = "delete property \"" + sStatePropName + "\" on policy \"" + name + "\"";
                             if(!isDeferred || (action & emxInstallUtil_mxJPO.ACTION_EXECUTE) != 0) {
                            	 emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                             }
                             if (isDeferred && emxSchemaManifest_mxJPO.fLiveUpgrade != null) {
                             	emxSchemaManifest_mxJPO.fLiveUpgrade.println(sCmd);
                             	emxSchemaManifest_mxJPO.fLiveUpgrade.println();
                             }
                         }
                         else
                         {
                             hStates.put(sStatePropName, sStatePropValue);
                         }
                     }

                     String sPostCmd = "";
                     for(int i = 0; i < aStates.size(); i++)
                     {
                         String sStateName = (String)aStates.get(i);
                         String sStateSymbolicName = "state_" + StringUtils.replaceAll(sStateName, " ", "");
                         sPostCmd += " property \"" + sStateSymbolicName + "\" value \"" + sStateName + "\"";
                         if (!hStates.containsKey(sStateSymbolicName))
                         {
                             hStates.put(sStateSymbolicName, sStateName);
                         }
                     }

                     if (sPostCmd.length() > 0)
                     {
                         sCmd = "modify policy" + " \"" + name + "\"" + sPostCmd;
                         if(!isDeferred || (action & emxInstallUtil_mxJPO.ACTION_EXECUTE) != 0) {
                        	 emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                         }
                         if (isDeferred && emxSchemaManifest_mxJPO.fLiveUpgrade != null) {
                          	emxSchemaManifest_mxJPO.fLiveUpgrade.println(sCmd);
                          	emxSchemaManifest_mxJPO.fLiveUpgrade.println();
                          }
                     }
                 }
                 else
                 {
                     while(stStatePropNames.hasMoreTokens() && stStatePropValues.hasMoreTokens())
                     {
                         String sStatePropName = stStatePropNames.nextToken();
                         String sStatePropValue = stStatePropValues.nextToken();
                         if (sStatePropName.startsWith("state_"))
                         {
                             hStates.put(sStatePropName, sStatePropValue);
                         }
                     }
                 }

                 // refresh state cache.
                 hPolicyInfo.put(KEY_STATES, hStates);
            }
        }
    }

    /**
     * puts name in the cache
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sSymbolicName       symbolic name
     * @param sName  symbolic     name of the object
     * @throws exception
     * @since                     AEF 10.Next
     */
    public static void putName(Context context, String sSymbolicName, String sName)
        throws Exception
    {
        String sAdminType = sSymbolicName.substring(0, sSymbolicName.indexOf('_'));
        putName(context, sAdminType, sSymbolicName, sName);
    }


    /**
     * puts name in the cache
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sAdminType          admin type name
     * @param sSymbolicName       symbolic name
     * @param sName  symbolic     name of the object
     * @throws exception
     * @since                     AEF 10.Next
     */
    public static void putName(Context context, String sAdminType, String sSymbolicName, String sName)
        throws Exception
    {
        // load symbolic names first time
        if (adminCache == null)
        {
            loadAdminCache(context);
        }

        // get admin map
        TreeMap hAdminMap = (TreeMap)emxInstallUtil_mxJPO.getKeyValue(adminCache, sAdminType, TreeMap.class);

        // get admin info cache
        TreeMap hAdminInfo = (TreeMap)emxInstallUtil_mxJPO.getKeyValue(hAdminMap, sSymbolicName, TreeMap.class);

        // put name into cache
        hAdminInfo.put(KEY_ACTUAL_NAME, sName);
    }

    /**
     * removes symbolic name from the cache
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sSymbolicName       symbolic name
     * @throws exception
     * @since                     AEF 10.Next
     */
    public static void removeSymbolicName(Context context, String sSymbolicName)
        throws Exception
    {
        String sAdminType = sSymbolicName.substring(0, sSymbolicName.indexOf('_'));

        // return if cache is not loaded
        if (adminCache == null)
        {
            return;
        }

        // get admin map
        TreeMap hAdminMap = (TreeMap)adminCache.get(sAdminType);
        // return if admin map is not present
        if (adminCache == null)
        {
            return;
        }

        // if admin info cache present then remove it
        if (hAdminMap.containsKey(sSymbolicName))
        {
            hAdminMap.remove(sSymbolicName);
        }
    }

    /**
     * reload symbolic name from the cache
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sSymbolicName       symbolic name
     * @throws exception
     * @since                     AEF 10.Next
     */
    public static void reloadSymbolicName(Context context, String sSymbolicName)
        throws Exception
    {
         removeSymbolicName(context, sSymbolicName);
         String cmd = "print program '" + SCHEMA_MAPPING_PROGRAM + "' select property[" + sSymbolicName + "].to dump |";
         String sResult = emxInstallUtil_mxJPO.executeMQLCommand(context, cmd);
         if (sResult != null && sResult.length() > 0)
         {
             String sAdminName = sResult.substring(sResult.indexOf(' ') + 1);
             putName(context, sSymbolicName, sAdminName);
         }
    }

    /**
     * reload symbolic name from the cache
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sSymbolicName       symbolic name
     * @throws exception
     * @since                     AEF 10.Next
     */
    public static void reloadSymbolicName(Context context, String args[])
        throws Exception
    {
		 for (int i = 0; i < args.length; i++)
		 {
			 reloadSymbolicName(context, args[i]);
		 }
    }
}
