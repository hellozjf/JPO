/*
** emxSchemaDefinitionBase
**
**  Copyright (c) 1992-2015 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import java.util.TreeMap;

import com.matrixone.jsystem.util.MxLinkedHashMap;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;
import java.util.Comparator;
import java.util.StringTokenizer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.input.SAXBuilder;

import matrix.db.Context;
import matrix.db.JPO;

import com.matrixone.jsystem.util.StringUtils;

/**
 * The <code>emxSchemaDefinitionBase</code> class contains methods for ActionLinkAccess.
 *
 * @version AEF 10.Next - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxSchemaDefinitionBase_mxJPO
{
    // application name
    private static final String FRAMEWORK_APPLICATION_NAME = "Framework";

    // Install properties
    private static final String PROP_INSTALLER = "installer";
    private static final String PROP_VERSION = "version";
    private static final String PROP_INSTALLED_DATE = "installed date";
    private static final String PROP_ORIGINAL_NAME = "original name";
    private static final String PROP_APPLICATION = "application";
    // Install properties values
    private static final String PROP_INSTALLER_VALUE = "ENOVIAEngineering";
    private static final String PROP_APPLICATION_VALUE = "Framework";

    // rename prefix
    private static final String RENAME_PREFIX = "emx";
    private static final String RENAME_SUFIX = "~";

    // Schema Definition File Keys
    private static final String KEY_ADMIN_TYPE = "AdminType";
    private static final String KEY_ORIGNAL_NAME = "OrignalName";
    private static final String KEY_ORIGINAL_NAME = "OriginalName";
    private static final String KEY_SYMBOLIC_NAME = "SymbolicName";
    private static final String KEY_VERSION_HISTORY = "VersionHistory";
    private static final String KEY_COMMAND_INFO = "CommandInfo";
    private static final String KEY_COMMAND = "Command";
    private static final String KEY_SYMBOLIC_NAME_START = "<SYM>";
    private static final String KEY_SYMBOLIC_NAME_END = "</SYM>";
    private static final String KEY_ENV_START = "<ENV>";
    private static final String KEY_ENV_END = "</ENV>";
    private static final String KEY_JPO = "JPO";
    private static final String KEY_SCRIPT = "Script";
    private static final String KEY_CONSTRUCTOR_ARGS = "ConstructorArg";
    private static final String KEY_METHOD = "Method";
    private static final String KEY_METHOD_ARGS = "MethodArg";
    private static final String KEY_SCRIPT_ARGS = "ScriptArg";

    // Schema definition file attributes
    private static final String ATTRIBUTE_VERSION = "Version";
    private static final String ATTRIBUTE_SKIP = "Skip";
    private static final String ATTRIBUTE_SKIP_YES = "Yes";
    private static final String ATTRIBUTE_SKIP_NO = "No";
    private static final String ATTRIBUTE_COMMAND_TYPE = "Type";
    private static final String ATTRIBUTE_COMMAND_TYPE_MQL = "MQL";
    private static final String ATTRIBUTE_COMMAND_TYPE_JPO = "JPO";
    private static final String ATTRIBUTE_COMMAND_TYPE_TCL = "TCL";
    private static final String ATTRIBUTE_RELOAD_CACHE = "ReloadCache";
    private static final String ATTRIBUTE_RELOAD_CACHE_YES = "Yes";
    private static final String ATTRIBUTE_RELOAD_CACHE_NO = "No";
	private static final String ATTRIBUTE_DEFERRED = "deferred";
	private static final String ATTRIBUTE_DEFERRED_YES = "Yes";
	private static final String ATTRIBUTE_DEFERRED_NO = "No";

    // Symbolic names hook
    private static final String SCHEMA_MAPPING_PROGRAM = "eServiceSchemaVariableMapping.tcl";


    // Schema Definition files will be cached here
    private Document schemaDefinitionFile = null;

    // version util
    private emxAppVersionUtil_mxJPO appVersionUtil = null;



    /**
     * Constructor.
     *
     * Should be used while firing JPO throgh command line
     * @param context the eMatrix <code>Context</code> object
     * @param args[0] holds schema definition file name with full path
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public emxSchemaDefinitionBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        // throw exception if improper arguments are passed
        if (args.length < 1)
        {
            throw (new Exception("Could not instantiate JPO:  Schema definition xml file name should be passed as input parameter."));
        }

        // load AppInfo.rul file
        String sAppInfoFile = null;
        if (args.length >= 2 && args[1].trim().length() > 0)
        {
            sAppInfoFile = args[1];
        }
        appVersionUtil = new emxAppVersionUtil_mxJPO(context, FRAMEWORK_APPLICATION_NAME, sAppInfoFile);

        // Get reload cache flag is on.
        // by default it will be set to false.
        boolean bReloadCache = true;
        if (args.length >= 3 && args[2].equalsIgnoreCase("FALSE"))
        {
            bReloadCache = false;
        }

        loadSchemaDefinition(context, args[0], bReloadCache);

    }

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param schemaDefFile holds schema definition file handle
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public emxSchemaDefinitionBase_mxJPO (Context context, File schemaDefFile, emxAppVersionUtil_mxJPO appVersions)
        throws Exception
    {
        loadSchemaDefinition(context, schemaDefFile, true);

        if (appVersions != null)
        {
            appVersionUtil = appVersions;
        }
        else
        {
            appVersionUtil = new emxAppVersionUtil_mxJPO(context, FRAMEWORK_APPLICATION_NAME, null);
        }
    }

    /**
     * Empty Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param schemaDefFile holds schema definition file handle
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public emxSchemaDefinitionBase_mxJPO ()
        throws Exception
    {
    }

    /**
     * This method loads schema definition file.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param xmlFile holds schema definition file name with full path
     * @param bReloadCache if set to true it will reload the cache
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public void loadSchemaDefinition(Context context, String xmlFile, boolean bReloadCache)
        throws Exception
    {
        loadSchemaDefinition(context, new File(xmlFile), bReloadCache);
    }


    /**
     * This method loads schema definition file.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param xmlFile holds schema definition file name with full path
     * @param bReloadCache if set to true it will reload the cache
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public void loadSchemaDefinition(Context context, File xmlFile, boolean bReloadCache)
        throws Exception
    {
        // if reload cache is set to ture or
        // if xml file is not cached then parse xml file
        if (bReloadCache || schemaDefinitionFile == null)
        {
            SAXBuilder xmlBuilder = new SAXBuilder();
            xmlBuilder.setFeature("http://xml.org/sax/features/external-general-entities", false);
            xmlBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            Document xmlDoc = xmlBuilder.build(xmlFile);
            schemaDefinitionFile = xmlDoc;
        }
    }

    /**
     * Get schema definition file admin type.
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public String getAdminType(Context context)
        throws Exception
    {
        // Get root
        Element root = schemaDefinitionFile.getRootElement();
        // Get admin type node text
        String sAdminType = root.getChildText(KEY_ADMIN_TYPE).trim();
        return sAdminType;
    }

    /**
     * Get schema definition file original name.
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public String getOriginalName(Context context)
        throws Exception
    {
        // get root
        Element root = schemaDefinitionFile.getRootElement();
        // get original name text
        String sOriginalName = root.getChildText(KEY_ORIGINAL_NAME);
        if (sOriginalName == null)
        {
            sOriginalName = root.getChildText(KEY_ORIGNAL_NAME);
        }

        if (sOriginalName != null)
            sOriginalName = sOriginalName.trim();

        return sOriginalName;
    }

    /**
     * Get schema definition file Symbolic Name.
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public String getSymbolicName(Context context)
        throws Exception
    {
        // get root
        Element root = schemaDefinitionFile.getRootElement();
        // get symbolic name text
        String sSymbolicName = root.getChildText(KEY_SYMBOLIC_NAME).trim();
        return sSymbolicName;
    }

    /**
     * Get version list
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public ArrayList getVersionList(Context context)
        throws Exception
    {
        // get root
        Element root = schemaDefinitionFile.getRootElement();
        // get all version history nodes
        List lVersionHistory = root.getChildren(KEY_VERSION_HISTORY);
        ArrayList aVersion = new ArrayList(lVersionHistory.size());

        // for each version history node
        for (int i = 0; i < lVersionHistory.size(); i++)
        {
            Element eVersionHistory = (Element)lVersionHistory.get(i);
            // get version
            String sVersion = eVersionHistory.getAttributeValue(ATTRIBUTE_VERSION);
            aVersion.add(sVersion);
        }

        // return all versions
        return aVersion;
    }

    public TreeMap getCommandList(Context context, emxSchemaDefinition_mxJPO customSDF)
        throws Exception
    {
		return getCommandList(context, customSDF, false);
	}
    /**
     * Get command list
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if the operation fails
     * @TreeMap returns a map of version and command list
     * @since AEF 10.Next
     */
    public TreeMap getCommandList(Context context, emxSchemaDefinition_mxJPO customSDF, boolean isDBCompare)
        throws Exception
    {
        TreeMap hCommandList = null;
        // if custom SDF passed then search commands in it first
        if (customSDF != null)
        {
            hCommandList = customSDF.getCommandList(context, null, isDBCompare);
        }
        else
        {
            hCommandList = new TreeMap(new VersionComparator(context, isDBCompare));
        }

        // get root
        Element root = schemaDefinitionFile.getRootElement();
        // get all version history nodes
        List lVersionHistory = root.getChildren(KEY_VERSION_HISTORY);

        // for each version history node
        for (int i = 0; i < lVersionHistory.size(); i++)
        {
            Element eVersionHistory = (Element)lVersionHistory.get(i);

            // get version
            String sSchemaDefVersion = eVersionHistory.getAttributeValue(ATTRIBUTE_VERSION).trim();

            if (hCommandList.containsKey(sSchemaDefVersion))
            {
                continue;
            }

            // get skip attribute
            String sSkip = eVersionHistory.getAttributeValue(ATTRIBUTE_SKIP);
            boolean bSkip = false;
            if (sSkip != null && sSkip.trim().equalsIgnoreCase(ATTRIBUTE_SKIP_YES))
            {
                bSkip = true;
            }

            ArrayList aCommand = new ArrayList();
            if (!bSkip)
            {
                // get all command info nodes
                List lCommandInfo = eVersionHistory.getChildren(KEY_COMMAND_INFO);

                // for each command info node
                for (int ii = 0; ii < lCommandInfo.size(); ii++)
                {
                    Element eCommandInfo = (Element)lCommandInfo.get(ii);

                    // get commands
                    List lCommand = eCommandInfo.getChildren(KEY_COMMAND);
                    // for each command
                    for (int iii = 0; iii < lCommand.size(); iii++)
                    {
                        Element eCommand = (Element)lCommand.get(iii);
                        aCommand.add(eCommand);
                    }
                }
            }

            hCommandList.put(sSchemaDefVersion, aCommand);
        }
        return hCommandList;
    }

    /**
     * Replaces environment variables in the command with their values
     * The environment variables are prefixed and suffixed by <ENV> and </ENV>
     * tags.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param sCmd the command to be processed
     * @throws Exception if the operation fails
     * @String returns command with replaced env variables.
     * @since AEF 10.Next
     */
    private static String replaceEnvVariables(Context context, String sCmd)
        throws Exception
    {
        String retString = sCmd;
        StringBuffer sb = new StringBuffer(sCmd);
        int index = retString.indexOf(KEY_ENV_START);
        while (index >= 0)
        {
            int index1 = retString.indexOf(KEY_ENV_END, index + KEY_ENV_START.length());

            String sEnvName = retString.substring(index + KEY_ENV_START.length(), index1);
            String sEnvValue = emxInstallUtil_mxJPO.executeMQLCommand(context, "get env \"" + sEnvName + "\"");

            sb.replace(index, index1 + KEY_ENV_END.length(), sEnvValue);
            retString = sb.toString();
            index = retString.indexOf(KEY_ENV_START);
        }
        return retString;
    }

    /**
     * Removes symbolic name references from MQL command that don't exists
     * @param context the eMatrix <code>Context</code> object
     * @param sCmd the command to be processed
     * @param sAdminSymbolicName symbolic name of admin object
     * @param sInputCmd MQL command
     * @returns new command without unresolved admins.
     * @throws Exception if the operation fails
     * @String returns command with replaced env variables.
     * @since AEF 10.Next
     */
    private static String removeUnresolvedSchemaReferences(Context context, String sAdminSymbolicName, String sName, String sInputCmd)
        throws Exception
    {
        // get admin type
        String sAdminType = sAdminSymbolicName.substring(0, sAdminSymbolicName.indexOf('_'));
        String sNewCmd = "";

        // if admin type is type then look for attributes
        if (sAdminType.equals("type"))
        {
            ArrayList aKeys = new ArrayList();
            aKeys.add("add attribute");
            aKeys.add("remove attribute");
            aKeys.add("attribute");
            sNewCmd = removeUnresolvedSchemaReferences(context, sAdminSymbolicName, aKeys, sInputCmd);
        }

        // if admin type is relationship then look for attributes and types
        if (sAdminType.equals("relationship"))
        {
            ArrayList aKeys = new ArrayList();
            aKeys.add("add attribute");
            aKeys.add("remove attribute");
            aKeys.add("attribute");
            aKeys.add("add type");
            aKeys.add("remove type");
            aKeys.add("type");
            sNewCmd = removeUnresolvedSchemaReferences(context, sAdminSymbolicName, aKeys, sInputCmd);
        }

        // if admin type is interface then look for attributes types and relationships
        if (sAdminType.equals("interface"))
        {
            ArrayList aKeys = new ArrayList();
            aKeys.add("add attribute");
            aKeys.add("remove attribute");
            aKeys.add("attribute");
            aKeys.add("add type");
            aKeys.add("remove type");
            aKeys.add("type");
            aKeys.add("add relationship");
            aKeys.add("remove relationship");
            aKeys.add("relationship");
            sNewCmd = removeUnresolvedSchemaReferences(context, sAdminSymbolicName, aKeys, sInputCmd);
        }

        // if admin is role or group then look for person
        if (sAdminType.equals("role") || sAdminType.equals("group"))
        {
            ArrayList aKeys = new ArrayList();
            aKeys.add("add assign person");
            aKeys.add("remove assign person");
            aKeys.add("assign person");
            sNewCmd = removeUnresolvedSchemaReferences(context, sAdminSymbolicName, aKeys, sInputCmd);
        }

        // if admin is person then look for group and role
        if (sAdminType.equals("person"))
        {
            ArrayList aKeys = new ArrayList();
            aKeys.add("remove assign role");
            aKeys.add("assign role");
            aKeys.add("remove assign group");
            aKeys.add("assign group");
            sNewCmd = removeUnresolvedSchemaReferences(context, sAdminSymbolicName, aKeys, sInputCmd);
        }

        // if admin is policy then look for type,store,format,user
        if (sAdminType.equals("policy"))
        {
            ArrayList aKeys = new ArrayList();
            aKeys.add("add type");
            aKeys.add("remove type");
            aKeys.add("type");
            aKeys.add("add format");
            aKeys.add("remove format");
            aKeys.add("format");
            aKeys.add("defaultformat");
            aKeys.add("store");
            sNewCmd = removeUnresolvedSchemaReferences(context, sAdminSymbolicName, aKeys, sInputCmd);
            sNewCmd = removeUnresolvedUsers(context, sAdminSymbolicName, sName, sNewCmd);
        }

        // if admin type is application then look for all admin types
        if (sAdminType.equals("application"))
        {
            ArrayList aKeys = new ArrayList();
            aKeys.add("add member store");
            aKeys.add("remove member store");
            aKeys.add("member store");
            aKeys.add("add member vault");
            aKeys.add("remove member vault");
            aKeys.add("member vault");
            aKeys.add("add member dimension");
            aKeys.add("remove member dimension");
            aKeys.add("member dimension");
            aKeys.add("add member attribute");
            aKeys.add("remove member attribute");
            aKeys.add("member attribute");
            aKeys.add("add member type");
            aKeys.add("remove member type");
            aKeys.add("member type");
            aKeys.add("add member relationship");
            aKeys.add("remove member relationship");
            aKeys.add("member relationship");
            aKeys.add("add member interface");
            aKeys.add("remove member interface");
            aKeys.add("member interface");
            aKeys.add("add member format");
            aKeys.add("remove member format");
            aKeys.add("member format");
            aKeys.add("add member person");
            aKeys.add("remove member person");
            aKeys.add("member person");
            aKeys.add("add member role");
            aKeys.add("remove member role");
            aKeys.add("member role");
            aKeys.add("add member group");
            aKeys.add("remove member group");
            aKeys.add("member group");
            aKeys.add("add member association");
            aKeys.add("remove member association");
            aKeys.add("member association");
            aKeys.add("add member policy");
            aKeys.add("remove member policy");
            aKeys.add("member policy");
            aKeys.add("add member rule");
            aKeys.add("remove member rule");
            aKeys.add("member rule");
            aKeys.add("add member index");
            aKeys.add("remove member index");
            aKeys.add("member index");
            sNewCmd = removeUnresolvedSchemaReferences(context, sAdminSymbolicName, aKeys, sInputCmd);
        }

        if (sNewCmd.length() == 0)
        {
            sNewCmd = sInputCmd;
        }

        return sNewCmd;
    }

    /**
     * Removes user references from MQL command that don't exists
     * @param context the eMatrix <code>Context</code> object
     * @param sAdminSymbolicName symbolic name of admin object
     * @param sInputCmd MQL command
     * @returns new command without unresolved admins.
     * @throws Exception if the operation fails
     * @String returns command with replaced env variables.
     * @since AEF 10.Next
     */
    private static String removeUnresolvedUsers(Context context, String sAdminSymbolicName, String sName, String sInputCmd)
        throws Exception
    {
        String sCmd = sInputCmd;
        ArrayList aFieldKeyWords = new ArrayList();
        aFieldKeyWords.add("add user");
        aFieldKeyWords.add("remove user");
        aFieldKeyWords.add("user");

        ArrayList aOrgUnresolvedRef = emxAdminCache_mxJPO.getUnresolvedReferences(context, sAdminSymbolicName);

        ArrayList aCmd = getWordsFromCmd(context, sCmd, aFieldKeyWords);

        String sNewCmd = "";
        ArrayList aUnresolvedSyms = new ArrayList();
        for (int i = 0; i < aCmd.size(); i++)
        {
            String sToken = (String)aCmd.get(i);
            boolean bIsKeyWord = false;
            for (int ii = 0; ii < aFieldKeyWords.size(); ii++)
            {
                String sFieldKeyWord = (String)aFieldKeyWords.get(ii);
                if (sToken.equals(sFieldKeyWord) && !((String)aCmd.get(i - 1)).equals("route"))
                {
                    String sFilter = null;
                    String sUser = (String)aCmd.get(++i);
                    int index = sUser.indexOf(KEY_SYMBOLIC_NAME_START);
                    int index1 = sUser.indexOf(KEY_SYMBOLIC_NAME_END, index + KEY_SYMBOLIC_NAME_START.length());
                    String sUserSymbolicName = sUser.substring(index + KEY_SYMBOLIC_NAME_START.length(), index1);

                    String sKey = null;
                    String sAccess = (String)aCmd.get(++i);
                    if (sAccess.equals("key")) {
						sKey = (String)aCmd.get(++i);
						sAccess = (String)aCmd.get(++i);
					}

                    boolean bUserFound = true;
                    try
                    {
                        sUser = replaceSymbolicNames(context, sAdminSymbolicName, sName, sUser);

                        if (aOrgUnresolvedRef.contains(sUserSymbolicName)) {
                            throw(new Exception());
                        }
                    }
                    catch (Exception ex)
                    {
                        bUserFound = false;
                        aUnresolvedSyms.add(sUserSymbolicName);
                    }

                    boolean bFilterFound = true;
                    String sFilterKey = null;
                    String sNewAccess = "";

					for (int j = 0; j < 5 && i+2 <= aCmd.size() - 1; j++) {

						i++;
						i++;
						sFilterKey = (String)aCmd.get(i);
						if (sFilterKey.equals("organization") || sFilterKey.equals("project") || sFilterKey.equals("owner") || sFilterKey.equals("reserve") || sFilterKey.equals("maturity")) {
							sNewAccess += (String)aCmd.get(i - 1) + " " + sFilterKey + " ";
						} else {
							i--;
							i--;
							break;
						}
					}


                    if (i <= aCmd.size() - 3) {
						i++;
						sFilterKey = (String)aCmd.get(i);
						if (sFilterKey.equals("filter") || sFilterKey.equals("localfilter"))
						{
							bFilterFound = true;
							i++;
							sFilter = (String)aCmd.get(i);

							try
							{
								sFilter = replaceSymbolicNames(context, sAdminSymbolicName, sName, sFilter);
							}
							catch (Exception ex)
							{
								bFilterFound = false;
								aUnresolvedSyms.add(sUserSymbolicName);
							}
						} else {
							i--;
						}
					}

                    if (sFilter == null && bFilterFound)
                    {
                        if (bUserFound)
                        {
                            sNewCmd += sFieldKeyWord + " " + sUser + " ";
                            if (sKey != null) {
								sNewCmd += "key " + sKey + " ";
							}
							sNewCmd += sAccess + " " + sNewAccess;
                        }
                    }
                    else
                    {
                        if (bUserFound && bFilterFound)
                        {
                            sNewCmd += sFieldKeyWord + " " + sUser + " ";
                            if (sKey != null) {
								sNewCmd += "key " + sKey + " ";
							}
							sNewCmd += sAccess + " " + sNewAccess + " " + sFilterKey + " " + sFilter + " ";
                        }
                    }

                    bIsKeyWord = true;
                    break;
                }
            }

            if (!bIsKeyWord)
            {
                sNewCmd += sToken + " ";
            }
        }
        if (aUnresolvedSyms.size() > 0)
        {
            ArrayList aUnresolvedRef = emxAdminCache_mxJPO.getUnresolvedReferences(context, sAdminSymbolicName);
            for (int i = 0; i < aUnresolvedSyms.size(); i++)
            {
                String sAdminSym = (String)aUnresolvedSyms.get(i);
                if (aUnresolvedRef.contains(sAdminSym))
                {
                    continue;
                }
                else
                {
                    sNewCmd += "property mxSym_" + sAdminSym + " value UNRESOLVED ";
                    aUnresolvedRef.add(sAdminSym);
                }
            }
            emxAdminCache_mxJPO.putUnresolvedReferences(context, sAdminSymbolicName, aUnresolvedRef);
        }
        return sNewCmd;
    }

    /**
     * Gets word list from MQL command
     * @param context the eMatrix <code>Context</code> object
     * @param sInputCmd MQL command to be parsed to get word list
     * @param aKeyWords list of key words to be identified
     * @returns ArrayList containing list of all the words
     * @throws Exception if the operation fails
     * @since AEF 10.6
     */
    private static ArrayList getWordsFromCmd(Context context, String sInputCmd, ArrayList aKeyWords)
        throws Exception
    {
        // get command in one single line
        StringTokenizer st = new StringTokenizer(sInputCmd, "\n");
        String sCmd = "";
        while (st.hasMoreTokens())
        {
            sCmd += st.nextToken().trim() + " ";
        }

        // replace all the key words separated by spaces
        ArrayList aNewKeyWords = new ArrayList();
        for (int i = 0; aKeyWords != null && i < aKeyWords.size(); i++)
        {
            String sKeyWord = (String)aKeyWords.get(i);
            String sNewKeyWord = sKeyWord.replace(' ', '_');
            aNewKeyWords.add(sNewKeyWord);
            sCmd = StringUtils.replaceAll(sCmd, sKeyWord, sNewKeyWord);
        }

        ArrayList aCmd = new ArrayList();
        st = new StringTokenizer(sCmd, " ");
        boolean bIsQuoteOn = false;
        String sQuoteType = null;
        String sCurrentWord = "";
        while (st.hasMoreTokens())
        {
            String sToken = st.nextToken();
            if (!bIsQuoteOn && sToken.startsWith("\""))
            {
                bIsQuoteOn = true;
                sQuoteType = "\"";
                sCurrentWord = "";
            }
            else if (!bIsQuoteOn && sToken.startsWith("'"))
            {
                bIsQuoteOn = true;
                sQuoteType = "'";
                sCurrentWord = "";
            }

            if (bIsQuoteOn)
            {
                if (sCurrentWord.length() == 0)
                {
                    sCurrentWord += sToken;
                }
                else
                {
                    sCurrentWord += " " + sToken;
                }
            }
            else
            {
                sCurrentWord = sToken;
            }

            if (bIsQuoteOn && sToken.endsWith(sQuoteType))
            {
                bIsQuoteOn = false;
            }

            if (!bIsQuoteOn)
            {
                if (aNewKeyWords.contains(sCurrentWord))
                {
                    sCurrentWord = sCurrentWord.replace('_', ' ');
                }

                aCmd.add(sCurrentWord);
            }
        }

        return aCmd;
    }

    /**
     * Removes symbolic name references from MQL command that don't exists
     * @param context the eMatrix <code>Context</code> object
     * @param sCmd the command to be processed
     * @param sAdminSymbolicName symbolic name of admin object
     * @param aFieldKeyWords keys to be searched in MQL
     * @param sInputCmd MQL command
     * @returns new command without unresolved admins.
     * @throws Exception if the operation fails
     * @String returns command with replaced env variables.
     * @since AEF 10.Next
     */
    private static String removeUnresolvedSchemaReferences(Context context, String sAdminSymbolicName, ArrayList aFieldKeyWords, String sInputCmd)
        throws Exception
    {
        String sCmd = sInputCmd;

        ArrayList aCmd = getWordsFromCmd(context, sCmd, aFieldKeyWords);

        String sNewCmd = "";
        ArrayList aUnresolvedSyms = new ArrayList();
        for (int i = 0; i < aCmd.size(); i++)
        {
            String sToken = (String)aCmd.get(i);

            boolean bIsKeyWord = false;
            for (int ii = 0; ii < aFieldKeyWords.size(); ii++)
            {
                String sFieldKeyWord = (String)aFieldKeyWords.get(ii);
                if (sToken.equals(sFieldKeyWord))
                {
                    String sSymNames = (String)aCmd.get(++i);

                    StringTokenizer st2 = new StringTokenizer(sSymNames, ",");
                    ArrayList aSyms = new ArrayList();
                    while (st2.hasMoreTokens())
                    {
                        String sToken2 = st2.nextToken().trim();

                        if (sToken2.equals("all") || sToken2.equals("\"all\"") || sToken2.equals("'all'"))
                        {
                            aSyms.add("all");
                        }
                        else
                        {
                            int index = sToken2.indexOf(KEY_SYMBOLIC_NAME_START);
                            int index1 = sToken2.indexOf(KEY_SYMBOLIC_NAME_END, index + KEY_SYMBOLIC_NAME_START.length());
                            String sSymbolicName = sToken2.substring(index + KEY_SYMBOLIC_NAME_START.length(), index1);
                            String sAdminName = emxAdminCache_mxJPO.getName(context, sSymbolicName);
                            if (sAdminName == null || sAdminName.length() == 0)
                            {
                                aUnresolvedSyms.add(sSymbolicName);
                            }
                            else
                            {
                                aSyms.add(sAdminName);
                            }
                        }
                    }
                    if (aSyms.size() > 0)
                    {
                        sNewCmd += sFieldKeyWord + " ";
                        for (int iii = 0; iii < aSyms.size(); iii++)
                        {
                            sNewCmd += "\"" + (String)aSyms.get(iii) + "\"";
                            if (iii < aSyms.size() - 1)
                            {
                                sNewCmd += ",";
                            }
                            sNewCmd += " ";
                        }
                        if (sToken.startsWith("member") || sToken.startsWith("add member"))
                        {
                            sNewCmd += (String)aCmd.get(i + 1) + " ";
                            sNewCmd += (String)aCmd.get(i + 2) + " ";
                        }
                    }
                    if (sToken.startsWith("member") || sToken.startsWith("add member"))
                    {
                        ++i;
                        ++i;
                    }

                    bIsKeyWord = true;
                    break;
                }
            }

            if (!bIsKeyWord)
            {
                sNewCmd += sToken + " ";
            }

        }
        if (aUnresolvedSyms.size() > 0)
        {
            ArrayList aUnresolvedRef = emxAdminCache_mxJPO.getUnresolvedReferences(context, sAdminSymbolicName);
            for (int i = 0; i < aUnresolvedSyms.size(); i++)
            {
                String sAdminSym = (String)aUnresolvedSyms.get(i);
                if (aUnresolvedRef.contains(sAdminSym))
                {
                    continue;
                }
                else
                {
                    sNewCmd += "property mxSym_" + sAdminSym + " value UNRESOLVED ";
                    aUnresolvedRef.add(sAdminSym);
                }
            }
            emxAdminCache_mxJPO.putUnresolvedReferences(context, sAdminSymbolicName, aUnresolvedRef);
        }

        return sNewCmd;
    }

    /**
     * Replaces symbolic names in the command with their values
     * The symbolic names are prefixed and suffixed by <SYM> and </SYM>
     * tags.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param sCmd the command to be processed
     * @throws Exception if the operation fails
     * @String returns command with replaced symbolic names.
     * @since AEF 10.Next
     */
    private static String replaceSymbolicNames(Context context, String sAdminSymbolicName, String sName, String sCmd)
        throws Exception
    {
        String retString = sCmd;
        StringBuffer sb = new StringBuffer(sCmd);
        int index = retString.indexOf(KEY_SYMBOLIC_NAME_START);
        while (index >= 0)
        {
            int index1 = retString.indexOf(KEY_SYMBOLIC_NAME_END, index + KEY_SYMBOLIC_NAME_START.length());

            String sSymbolicName = retString.substring(index + KEY_SYMBOLIC_NAME_START.length(), index1);

            String sAdminType = sSymbolicName.substring(0, sSymbolicName.indexOf("_"));
            String sAdminName = "";
            if (sAdminType.equals("state"))
            {
                sAdminName = emxAdminCache_mxJPO.getStateName(context, sAdminSymbolicName, sSymbolicName);
                if (sAdminName == null || sAdminName.length() == 0)
                {
                    throw (new Exception("Symbolic name " + sSymbolicName + " not found"));
                }
            }
            else
            {
                sAdminName = emxAdminCache_mxJPO.getName(context, sSymbolicName);
                if (sAdminName == null || sAdminName.length() == 0)
                {
                    if (sCmd.trim().startsWith("add") && sSymbolicName.equals(sAdminSymbolicName))
                    {
                        sAdminName = sName;
                        emxAdminCache_mxJPO.putName(context, sAdminSymbolicName, sName);
                    }
                    else
                    {
                        throw (new Exception("Error: Symbolic name " + sSymbolicName + " not found"));
                    }
                }
            }

            sb.replace(index, index1 + KEY_SYMBOLIC_NAME_END.length(), sAdminName);
            retString = sb.toString();
            index = retString.indexOf(KEY_SYMBOLIC_NAME_START);
        }
        return retString;
    }

    /**
     * installs admin object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param sVersion version to be installed
     * @throws Exception if the operation fails
     * @returns boolean true/false indicating install sucess.
     * @since AEF 10.Next
     */
    public void install(Context context, String sInstallVersion, PrintStream fMQLLog)
        throws Exception
    {
        install(context, sInstallVersion, fMQLLog, null);
    }

    private String isTenantInstall = null;
    /**
     * installs admin object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param sVersion version to be installed
     * @throws Exception if the operation fails
     * @returns boolean true/false indicating install sucess.
     * @since AEF 10.Next
     */
    public void install(Context context, String sInstallVersion, PrintStream fMQLLog, emxSchemaDefinition_mxJPO customSDF)
        throws Exception
    {

        // Get all the commands
        TreeMap hCommandList = getCommandList(context, customSDF, false);

        // if no commands found then return
        if (hCommandList == null || hCommandList.isEmpty())
        {
            return;
        }

        // Get admin type, original name, symbolic name
        String sAdminType = getAdminType(context);
        String sOriginalName = getOriginalName(context);
        String sSymbolicName = getSymbolicName(context);
        // Get actual name from symbolic name lookup
        String sDBName = emxAdminCache_mxJPO.getName(context, sAdminType, sSymbolicName);
        String sDBVersion = emxAdminCache_mxJPO.getVersion(context, sAdminType, sSymbolicName);
        String sDBApplication = emxAdminCache_mxJPO.getApplication(context, sAdminType, sSymbolicName);

        boolean tenant = false;
        if( isTenantInstall == null )
        {
            String tenantCmd = "print context select tenant dump;";
            String tenantResult = emxInstallUtil_mxJPO.executeMQLCommand(context, tenantCmd);
            if( tenantResult != null && !"".equals(tenantResult))
            {
                tenant = true;
                isTenantInstall = "true";
            }
        } else if( "true".equals(isTenantInstall) )
        {
            tenant = true;
        }
        boolean preventedAdminByTenantUser = false;
        if( tenant  && (sAdminType.equals("vault") || sAdminType.equals("store")) )
        {
            preventedAdminByTenantUser =true;
        }
        // Set name of the admin object
        String sName = null;
        if (sDBName != null)
        {
            sName = sDBName;
        }
        else
        {
            String sResult = null;
            if (sAdminType.equals("association"))
            {
                sResult = emxInstallUtil_mxJPO.executeMQLCommand(context, "list user \"" + sOriginalName + "\"");
            }
            else if (!preventedAdminByTenantUser)
            {
                sResult = emxInstallUtil_mxJPO.executeMQLCommand(context, "list " + sAdminType + " \"" + sOriginalName + "\"");
            }
            if (sResult != null && sResult.equals(sOriginalName))
            {
                 String sLatestVersion = appVersionUtil.getLatestVersion(context);
                 sName = RENAME_PREFIX + sLatestVersion + RENAME_SUFIX + sOriginalName;
                 emxInstallUtil_mxJPO.writeSchemaLog(context, "Renaming", sAdminType, sOriginalName, "");
            }
            else
            {
                 sName = sOriginalName;
            }
        }

        // current version getting installed
        String sVersion = null;

        // for each version listed in sdf
        try
        {
            Iterator itr = hCommandList.keySet().iterator();
            while (itr.hasNext())
            {
                // get sdf version
                sVersion = (String)itr.next();
                //get internal version
                sVersion = appVersionUtil.getInternalVersion(sVersion);


				String schemaVersion = appVersionUtil.getSchemaVersion();
				// During first iteration, bring the schema left over to the schema level previously installed.
				if (schemaVersion != null
						&& schemaVersion.length() > 0
						&& appVersionUtil.compareVersion(context, sAdminType,
								sName, sInstallVersion, schemaVersion) == 1) {
					if (appVersionUtil.compareVersion(context, sAdminType,
							sName, sVersion, schemaVersion) >= 2) {
						continue;
					}
				} else if (!sInstallVersion.equals(sVersion)) {
					continue;
				}

				// compare database version if admin already exists
                // or database version is lesser then sdf version
                if (sDBName == null || appVersionUtil.compareVersion(context, sAdminType, sName, sVersion, sDBVersion) > 0)
                {
                    // get command list for current version
                    ArrayList aCmds = (ArrayList)hCommandList.get(sVersion);

                    if (!aCmds.isEmpty())
                    {
                        if (sDBName == null)
                        {
                            emxInstallUtil_mxJPO.writeSchemaLog(context, "Adding", sAdminType, sName, sVersion);
                        }
                        else
                        {
                            emxInstallUtil_mxJPO.writeSchemaLog(context, "Modifying", sAdminType, sName, sVersion);
                        }
                    }

                    boolean isDeferred = false;
                    // execute all the commands.
                    if (!preventedAdminByTenantUser)
                    {
                        for (int i = 0; i < aCmds.size(); i++)
                        {
                            Element eCmd = (Element)aCmds.get(i);
                            String sCmdType = eCmd.getAttributeValue(ATTRIBUTE_COMMAND_TYPE).trim();
                            String sDeferred = eCmd.getAttributeValue(ATTRIBUTE_DEFERRED);
                            if (ATTRIBUTE_DEFERRED_YES.equalsIgnoreCase(sDeferred)) {
                            	isDeferred = true;
                            }
                            int action = emxInstallUtil_mxJPO.getActionForUnsafeChanges(context);
                            if (sCmdType.equalsIgnoreCase(ATTRIBUTE_COMMAND_TYPE_JPO))
                            {
                                // Get JPO name
                                String sJPO = eCmd.getChildText(KEY_JPO).trim();
                                // Get method name
                                String sMethod = eCmd.getChildText(KEY_METHOD).trim();
                                // Get constructor args
                                List lConstArgs = eCmd.getChildren(KEY_CONSTRUCTOR_ARGS);
                                String aConstArgs[] = null;
                                if (lConstArgs != null && !lConstArgs.isEmpty())
                                {
                                    aConstArgs = new String[lConstArgs.size()];
                                    for (int ii = 0; ii < lConstArgs.size(); ii++)
                                    {
                                        Element eConstArg = (Element)lConstArgs.get(ii);
                                        aConstArgs[ii] = eConstArg.getText().trim();
                                    }
                                }
                                // Get method args
                                List lMethodArgs = eCmd.getChildren(KEY_METHOD_ARGS);
                                String aMethodArgs[] = null;
                                if (lMethodArgs != null && !lMethodArgs.isEmpty())
                                {
                                    aMethodArgs = new String[lMethodArgs.size()];
                                    for (int ii = 0; ii < lMethodArgs.size(); ii++)
                                    {
                                        Element eMethodArg = (Element)lMethodArgs.get(ii);
                                        aMethodArgs[ii] = eMethodArg.getText().trim();
                                    }
                                }

                                // Invoke JPO
                            	String sCmd = "exec program " + sJPO;
                            	if (lConstArgs != null && !lConstArgs.isEmpty()) {
									for (String sConstArg: aConstArgs) {
										sCmd += " -construct \"";
										sCmd += sConstArg;
										sCmd += "\"";
									}
                            	}
                            	sCmd += " -method " + sMethod;
                            	if (lMethodArgs != null && !lMethodArgs.isEmpty()) {
									for (String sMethodArg: aMethodArgs) {
										sCmd += " \"";
										sCmd += sMethodArg;
										sCmd += "\"";
									}
								}
                                if (isDeferred && emxSchemaManifest_mxJPO.fLiveUpgrade != null) {
                                	emxSchemaManifest_mxJPO.fLiveUpgrade.println(sCmd);
                                	emxSchemaManifest_mxJPO.fLiveUpgrade.println();
                                }
                                if(!isDeferred || (action & emxInstallUtil_mxJPO.ACTION_EXECUTE) != 0) {
                                	JPO.invoke(context, sJPO, aConstArgs, sMethod, aMethodArgs);
                                    // write MQL log
                                    if (fMQLLog != null)
                                    {
                                        fMQLLog.println(sCmd);
                                        fMQLLog.println("");
                                    }
                                }
                            }
                            else if(sCmdType.equalsIgnoreCase(ATTRIBUTE_COMMAND_TYPE_TCL))
                            {
                                // Get Program Name
                                String sCmd = "exec program \"";
                                String sProgram = eCmd.getChildText(KEY_SCRIPT).trim();
                                sCmd += sProgram + "\" ";
                                // Get args
                                List lProgramArgs = eCmd.getChildren(KEY_SCRIPT_ARGS);
                                if (lProgramArgs != null && !lProgramArgs.isEmpty())
                                {
                                    for (int ii = 0; ii < lProgramArgs.size(); ii++)
                                    {
                                        Element eProgramArg = (Element)lProgramArgs.get(ii);
                                        sCmd += "\"" + eProgramArg.getText().trim() + "\"";
                                    }
                                }
                                // execute TCL program
                                if (isDeferred && emxSchemaManifest_mxJPO.fLiveUpgrade != null) {
                                	emxSchemaManifest_mxJPO.fLiveUpgrade.println(sCmd);
                                	emxSchemaManifest_mxJPO.fLiveUpgrade.println("");
                                }
                                if(!isDeferred || (action & emxInstallUtil_mxJPO.ACTION_EXECUTE) != 0) {
                                	emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                                    // write MQL log
                                    if (fMQLLog != null)
                                    {
                                        fMQLLog.println(sCmd);
                                        fMQLLog.println("");
                                    }
                                }
                            }
                            else
                            {
                                String sCmd = eCmd.getText();
                                sCmd = replaceEnvVariables(context, sCmd);
                                sCmd = removeUnresolvedSchemaReferences(context, sSymbolicName, sName, sCmd);
                                sCmd = replaceSymbolicNames(context, sSymbolicName, sName, sCmd);

                                if ((sDBName != null) && sCmd.trim().startsWith("add"))
                                {
                                    emxInstallUtil_mxJPO.writeSchemaLog(context, "Exists", sAdminType, sName, sVersion);
                                    emxInstallUtil_mxJPO.writeSchemaLog(context, "Skipping", "Command", "", "");
                                    emxInstallUtil_mxJPO.println(context, sCmd);
                                    continue;
                                }
                                if (isDeferred && emxSchemaManifest_mxJPO.fLiveUpgrade != null) {
                                	emxSchemaManifest_mxJPO.fLiveUpgrade.println(sCmd);
                                	emxSchemaManifest_mxJPO.fLiveUpgrade.println("");
                                }
                                if(!isDeferred || (action & emxInstallUtil_mxJPO.ACTION_EXECUTE) != 0) {
                                	emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                                    // write MQL log
                                    if (fMQLLog != null)
                                    {
                                        fMQLLog.println(sCmd);
                                        fMQLLog.println("");
                                    }
                                }
                            }
                            String sReloadCache = eCmd.getAttributeValue(ATTRIBUTE_RELOAD_CACHE);
                            if (sReloadCache != null && sReloadCache.trim().equalsIgnoreCase(ATTRIBUTE_RELOAD_CACHE_YES))
                            {
                                sName = emxAdminCache_mxJPO.getName(context, sAdminType, sSymbolicName);
                            }
                        }
                    }

                    // if admin to be added or modified is policy then
                    // register symbolic names for states.
                    if (!aCmds.isEmpty())
                    {
                        if (sAdminType.equalsIgnoreCase("policy"))
                        {
                            emxAdminCache_mxJPO.refreshStateSymbolicNames(context, sSymbolicName, true, isDeferred);
                        }

                        // if admin being added first time then
                        // add application, installed date, original name, installer properties
                        if (sDBName == null)
                        {
                            String sCmd = "";
                            if(!preventedAdminByTenantUser)
                            {
                                sCmd = "modify "+ sAdminType + " \"" + sName + "\"";
                                sCmd += " property \"" + PROP_VERSION + "\" value \"" + sVersion + "\"";
                                sCmd += " property \"" + PROP_APPLICATION + "\" value \"" + PROP_APPLICATION_VALUE + "\"";
                                sCmd += " property \"" + PROP_INSTALLER + "\" value \"" + PROP_INSTALLER_VALUE + "\"";
                                sCmd += " property \"" + PROP_ORIGINAL_NAME + "\" value \"" + sOriginalName + "\"";
                                sCmd += " property \"" + PROP_INSTALLED_DATE + "\" value \"" + new Date().toString() + "\"";
                                emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                            }

                            // register symbolic name if admin is getting added first time.
                            sCmd = "add property \"" + sSymbolicName + "\" " +
                                        "on program \"" + SCHEMA_MAPPING_PROGRAM + "\" " +
                                        "to " + sAdminType + " \"" + sName + "\"";
                            emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                            emxAdminCache_mxJPO.putName(context, sAdminType, sSymbolicName, sName);
                            emxAdminCache_mxJPO.putVersion(context, sAdminType, sSymbolicName, sVersion);
                            sDBName = sName;
                            sDBVersion = sVersion;
                        }
                        else
                        {
                            String sCmd = "modify "+ sAdminType + " \"" + sName + "\"";
                            sCmd += " property \"" + PROP_VERSION + "\" value \"" + sVersion + "\"";
                            emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                            emxAdminCache_mxJPO.putVersion(context, sAdminType, sSymbolicName, sVersion);
                            sDBVersion = sVersion;
                        }
                    }
                }
                // Suncing and skipping messages if application property
                // is FrameworkFuture
                else if (sDBName != null && sDBApplication.equals("FrameworkFuture"))
                {
                    // If version is same then set application to Framework and
                    // Message Syncing
                    if (appVersionUtil.compareVersion(context, sAdminType, sName, sVersion, sDBVersion) == 0)
                    {
                        emxInstallUtil_mxJPO.writeSchemaLog(context, "Syncing", sAdminType, sName, sVersion);
                        String sCmd = "modify "+ sAdminType + " \"" + sName + "\"";
                        sCmd += " property \"" + PROP_APPLICATION + "\" value \"Framework\"";
                        emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                        emxAdminCache_mxJPO.putApplication(context, sAdminType, sSymbolicName, "Framework");
                    }
                    // If version is lower then skipping message.
                    else
                    {
                        emxInstallUtil_mxJPO.writeSchemaLog(context, "Skipping", sAdminType, sName, sVersion);
                    }

                    // get command list for current version
                    ArrayList aCmds = (ArrayList)hCommandList.get(sVersion);
                    // get all commands
                    for (int i = 0; i < aCmds.size(); i++)
                    {
                        Element eCmd = (Element)aCmds.get(i);
                        String sCmdType = eCmd.getAttributeValue(ATTRIBUTE_COMMAND_TYPE).trim();
                        if (sCmdType.equalsIgnoreCase(ATTRIBUTE_COMMAND_TYPE_MQL))
                        {
                            String sCmd = eCmd.getText();
                            sCmd = replaceEnvVariables(context, sCmd);
                            emxInstallUtil_mxJPO.writeSchemaLog(context, "Skipping", "Command", "", "");
                            emxInstallUtil_mxJPO.println(context, sCmd);
                        }
                    }
                }
            }
        }
        catch(Exception ex)
        {
            if (sDBName == null)
            {
                emxAdminCache_mxJPO.removeSymbolicName(context, sSymbolicName);
            }
            else
            {
                emxAdminCache_mxJPO.refreshStateSymbolicNames(context, sSymbolicName, false);
            }
            throw ex;
        }
    }

    /**
     * installs admin object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param sVersion version to be installed
     * @throws Exception if the operation fails
     * @returns boolean true/false indicating install sucess.
     * @since AEF 10.Next
     */
    public void install(Context context, String args[])
        throws Exception
    {
        emxSchemaDefinition_mxJPO customSDF = null;
        if (args != null && args.length > 0 && args[0].trim().length() > 0)
        {
            customSDF = new emxSchemaDefinition_mxJPO(context, new File(args[0]), appVersionUtil);
        }
        install(context, null, null, customSDF);
    }

    private class VersionComparator implements Comparator
    {
        private Context cntx = null;
        private boolean isDB = false;

        public VersionComparator(Context context, boolean isDB)
        {
            cntx = context;
            this.isDB = isDB;
        }

        public int compare(Object o1, Object o2)
        {
            // get versions
            String sVersion1 = (String)o1;
            String sVersion2 = (String)o2;

            // return version compare results.
            try
            {
                if (isDB) {
                    return(appVersionUtil.compareDBVersion(cntx, sVersion1, sVersion2));
                } else {
                    return(appVersionUtil.compareVersion(cntx, null, null, sVersion1, sVersion2));
                }
            }
            catch(Exception ex)
            {
                return 0;
            }
        }
    }

    /**
     * resolves admin object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param sVersion version to be installed
     * @throws Exception if the operation fails
     * @returns boolean true/false indicating install sucess.
     * @since AEF 10.Next
     */
    public void resolve(Context context, PrintStream fMQLLog)
        throws Exception
    {
        resolve(context, fMQLLog, null);
    }

    /**
     * installs admin object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param sVersion version to be installed
     * @throws Exception if the operation fails
     * @returns boolean true/false indicating install sucess.
     * @since AEF 10.Next
     */
    public void resolve(Context context, PrintStream fMQLLog, emxSchemaDefinition_mxJPO customSDF)
        throws Exception
    {
        // Get all the commands
        TreeMap hCommandList = getCommandList(context, customSDF, true);

        // if no commands found then return
        if (hCommandList == null || hCommandList.isEmpty())
        {
            return;
        }

        // Get admin type, original name, symbolic name
        String sAdminType = getAdminType(context);
        String sSymbolicName = getSymbolicName(context);

        // Get actual name from symbolic name lookup
        String sDBName = emxAdminCache_mxJPO.getName(context, sAdminType, sSymbolicName);
        if (sDBName == null)
        {
            return;
        }
        String sDBVersion = emxAdminCache_mxJPO.getVersion(context, sAdminType, sSymbolicName);

        // for each version listed in sdf
        try
        {
            Iterator itr = hCommandList.keySet().iterator();
            boolean bVersionPassed = false;
            ArrayList aAllCmds = new ArrayList();
            while (itr.hasNext() && !bVersionPassed)
            {
                // get sdf version
                String sVersion = (String)itr.next();
                // if install version not passed then install all
                // otherwise install only that specific version
                if (sDBVersion.equals(sVersion))
                {
                    bVersionPassed = true;
                }

                // get command list for current version
                ArrayList aCmds = (ArrayList)hCommandList.get(sVersion);

                // execute all the commands.
                for (int i = 0; i < aCmds.size(); i++)
                {
                    Element eCmd = (Element)aCmds.get(i);
                    String sCmdType = eCmd.getAttributeValue(ATTRIBUTE_COMMAND_TYPE).trim();
                    if(sCmdType.equalsIgnoreCase(ATTRIBUTE_COMMAND_TYPE_MQL))
                    {
                        String sCmd = eCmd.getText();
                        aAllCmds.add(sCmd);
                    }
                }
            }

            aAllCmds = resolveSchemaReferences(context, sSymbolicName, sDBName, aAllCmds);

            for (int i = 0; i < aAllCmds.size(); i++)
            {
                String sCmd = (String)aAllCmds.get(i);
                if (sCmd == null || sCmd.length() == 0)
                {
                    continue;
                }
                emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);

                // write MQL log
                if (fMQLLog != null)
                {
                    fMQLLog.println(sCmd);
                    fMQLLog.println("");
                }
           }
        }
        catch(Exception ex)
        {
            throw ex;
        }
    }

    /**
     * Removes symbolic name references from MQL command that don't exists
     * @param context the eMatrix <code>Context</code> object
     * @param sCmd the command to be processed
     * @param sAdminSymbolicName symbolic name of admin object
     * @param sInputCmd MQL command
     * @returns new command without unresolved admins.
     * @throws Exception if the operation fails
     * @String returns command with replaced env variables.
     * @since AEF 10.Next
     */
    private static ArrayList resolveSchemaReferences(Context context, String sAdminSymbolicName, String sName, ArrayList aInputCmd)
        throws Exception
    {
        // get admin type
        String sAdminType = sAdminSymbolicName.substring(0, sAdminSymbolicName.indexOf('_'));
        ArrayList aNewCmd = null;

        // if admin type is type then look for attributes
        if (sAdminType.equals("type"))
        {
            MxLinkedHashMap aKeys = new MxLinkedHashMap();
            aKeys.put("add attribute", "add attribute");
            aKeys.put("remove attribute", "remove attribute");
            aKeys.put("attribute", "add attribute");
            ArrayList aRefAdmins = new ArrayList();
            aRefAdmins.add("attribute");
            aNewCmd = resolveSchemaReferences(context, sAdminSymbolicName, aKeys, aRefAdmins, aInputCmd);
        }

        // if admin type is relationships then look for attributes and types
        if (sAdminType.equals("relationship"))
        {
            MxLinkedHashMap aKeys = new MxLinkedHashMap();
            aKeys.put("add attribute", "add attribute");
            aKeys.put("remove attribute", "remove attribute");
            aKeys.put("attribute", "add attribute");
            aKeys.put("add type", "add type");
            aKeys.put("remove type", "remove type");
            aKeys.put("type", "add type");
            ArrayList aRefAdmins = new ArrayList();
            aRefAdmins.add("attribute");
            aRefAdmins.add("type");
            aNewCmd = resolveSchemaReferences(context, sAdminSymbolicName, aKeys, aRefAdmins, aInputCmd);
        }

        // if admin type is interfaces then look for attributes types & relationships
        if (sAdminType.equals("interface"))
        {
            MxLinkedHashMap aKeys = new MxLinkedHashMap();
            aKeys.put("add attribute", "add attribute");
            aKeys.put("remove attribute", "remove attribute");
            aKeys.put("attribute", "add attribute");
            aKeys.put("add type", "add type");
            aKeys.put("remove type", "remove type");
            aKeys.put("type", "add type");
            aKeys.put("add relationship", "add relationship");
            aKeys.put("remove relationship", "remove relationship");
            aKeys.put("relationship", "add relationship");
            ArrayList aRefAdmins = new ArrayList();
            aRefAdmins.add("attribute");
            aRefAdmins.add("type");
            aRefAdmins.add("relationship");
            aNewCmd = resolveSchemaReferences(context, sAdminSymbolicName, aKeys, aRefAdmins, aInputCmd);
        }

        // if admin is role or group then look for person
        if (sAdminType.equals("role") || sAdminType.equals("group"))
        {
            MxLinkedHashMap aKeys = new MxLinkedHashMap();
            aKeys.put("add assign person", "add assign person");
            aKeys.put("remove assign person", "remove assign person");
            aKeys.put("assign person", "add assign person");
            ArrayList aRefAdmins = new ArrayList();
            aRefAdmins.add("person");
            aNewCmd = resolveSchemaReferences(context, sAdminSymbolicName, aKeys, aRefAdmins, aInputCmd);
        }

        // if admin is person then look for group and role
        if (sAdminType.equals("person"))
        {
            MxLinkedHashMap aKeys = new MxLinkedHashMap();
            aKeys.put("remove assign role", "remove assign role");
            aKeys.put("assign role", "assign role");
            aKeys.put("remove assign group", "remove assign group");
            aKeys.put("assign group", "assign group");
            ArrayList aRefAdmins = new ArrayList();
            aRefAdmins.add("role");
            aRefAdmins.add("group");
            aNewCmd = resolveSchemaReferences(context, sAdminSymbolicName, aKeys, aRefAdmins, aInputCmd);
        }

        // if admin is policy then look for type,store,format,user
        if (sAdminType.equals("policy"))
        {
            MxLinkedHashMap aKeys = new MxLinkedHashMap();
            aKeys.put("add type", "add type");
            aKeys.put("remove type", "remove type");
            aKeys.put("type", "add type");
            aKeys.put("add format", "add format");
            aKeys.put("remove format", "remove format");
            aKeys.put("format", "add format");
            aKeys.put("defaultformat", "defaultformat");
            aKeys.put("store", "store");
            ArrayList aRefAdmins = new ArrayList();
            aRefAdmins.add("type");
            aRefAdmins.add("format");
            aRefAdmins.add("store");
            aNewCmd = resolveSchemaReferences(context, sAdminSymbolicName, aKeys, aRefAdmins, aInputCmd);
            aNewCmd.add(resolveUsers(context, sAdminSymbolicName, sName, aInputCmd));
        }

        // if admin type is application then look for all types
        if (sAdminType.equals("application"))
        {
            MxLinkedHashMap aKeys = new MxLinkedHashMap();
            aKeys.put("add member store", "add member store");
            aKeys.put("remove member store", "remove member store");
            aKeys.put("member store", "add member store");
            aKeys.put("add member vault", "add member vault");
            aKeys.put("remove member vault", "remove member vault");
            aKeys.put("member vault", "add member vault");
            aKeys.put("add member dimension", "add member dimension");
            aKeys.put("remove member dimension", "remove member dimension");
            aKeys.put("member dimension", "add member dimension");
            aKeys.put("add member attribute", "add member attribute");
            aKeys.put("remove member attribute", "remove member attribute");
            aKeys.put("member attribute", "add member attribute");
            aKeys.put("add member type", "add member type");
            aKeys.put("remove member type", "remove member type");
            aKeys.put("member type", "add member type");
            aKeys.put("add member relationship", "add member relationship");
            aKeys.put("remove member relationship", "remove member relationship");
            aKeys.put("member relationship", "add member relationship");
            aKeys.put("add member interface", "add member interface");
            aKeys.put("remove member interface", "remove member interface");
            aKeys.put("member interface", "add member interface");
            aKeys.put("add member format", "add member format");
            aKeys.put("remove member format", "remove member format");
            aKeys.put("member format", "add member format");
            aKeys.put("add member person", "add member person");
            aKeys.put("remove member person", "remove member person");
            aKeys.put("member person", "add member person");
            aKeys.put("add member role", "add member role");
            aKeys.put("remove member role", "remove member role");
            aKeys.put("member role", "add member role");
            aKeys.put("add member group", "add member group");
            aKeys.put("remove member group", "remove member group");
            aKeys.put("member group", "add member group");
            aKeys.put("add member association", "add member association");
            aKeys.put("remove member association", "remove member association");
            aKeys.put("member association", "add member association");
            aKeys.put("add member policy", "add member policy");
            aKeys.put("remove member policy", "remove member policy");
            aKeys.put("member policy", "add member policy");
            aKeys.put("add member rule", "add member rule");
            aKeys.put("remove member rule", "remove member rule");
            aKeys.put("member rule", "add member rule");
            aKeys.put("add member index", "add member index");
            aKeys.put("remove member index", "remove member index");
            aKeys.put("member index", "add member index");
            ArrayList aRefAdmins = new ArrayList();
            aRefAdmins.add("store");
            aRefAdmins.add("vault");
            aRefAdmins.add("dimension");
            aRefAdmins.add("attribute");
            aRefAdmins.add("type");
            aRefAdmins.add("relationship");
            aRefAdmins.add("interface");
            aRefAdmins.add("format");
            aRefAdmins.add("person");
            aRefAdmins.add("role");
            aRefAdmins.add("group");
            aRefAdmins.add("association");
            aRefAdmins.add("policy");
            aRefAdmins.add("rule");
            aRefAdmins.add("index");

            aNewCmd = resolveSchemaReferences(context, sAdminSymbolicName, aKeys, aRefAdmins, aInputCmd);
        }

        return aNewCmd;
    }

    /**
     * Resolves symbolic name references from MQL command that don't exists
     * @param context the eMatrix <code>Context</code> object
     * @param sCmd the command to be processed
     * @param sAdminSymbolicName symbolic name of admin object
     * @param aFieldKeyWords keys to be searched in MQL
     * @param sInputCmd MQL command
     * @returns new command without unresolved admins.
     * @throws Exception if the operation fails
     * @String returns command with replaced env variables.
     * @since AEF 10.Next
     */
    private static ArrayList resolveSchemaReferences(Context context, String sAdminSymbolicName, MxLinkedHashMap aFieldKeyWords, ArrayList aRefAdmins, ArrayList aInputCmd)
        throws Exception
    {
        ArrayList aReturnCmd = new ArrayList();
        ArrayList aUnresolvedRef = emxAdminCache_mxJPO.getUnresolvedReferences(context, sAdminSymbolicName);

        ArrayList aFoundFef = new ArrayList();

        String sAdminType = sAdminSymbolicName.substring(0, sAdminSymbolicName.indexOf('_'));

        String sAdminName = emxAdminCache_mxJPO.getName(context, sAdminSymbolicName);

        for (int i = 0; i < aInputCmd.size(); i++)
        {
            String sReturnCmd = "";
            String sInputCmd = (String)aInputCmd.get(i);
            ArrayList aCmd = getWordsFromCmd(context, sInputCmd, new ArrayList(aFieldKeyWords.keySet()));
            boolean bIsTo = false;
            for (int ii = 0; ii < aCmd.size(); ii++)
            {
                String sCmdWord = (String)aCmd.get(ii);
                if (sAdminType.equals("relationship") && sCmdWord.equals("to"))
                {
                    bIsTo = true;
                }
                if (sAdminType.equals("relationship") && sCmdWord.equals("from"))
                {
                    bIsTo = false;
                }
                for (int iii = 0; iii < aUnresolvedRef.size(); iii++)
                {
                    String sUnresolvedRef = (String)aUnresolvedRef.get(iii);
                    if (!aRefAdmins.contains(sUnresolvedRef.substring(0, sUnresolvedRef.indexOf('_'))))
                    {
                         continue;
                    }
                    String sUnresolvedRefName = emxAdminCache_mxJPO.getName(context, sUnresolvedRef);
                    if (sUnresolvedRefName == null || sUnresolvedRefName.length() == 0)
                    {
                        continue;
                    }
                    else
                    {
                        if (!aFoundFef.contains(sUnresolvedRef))
                        {
                            aFoundFef.add(sUnresolvedRef);
                        }
                    }
                    if(sCmdWord.indexOf(KEY_SYMBOLIC_NAME_START + sUnresolvedRef + KEY_SYMBOLIC_NAME_END) >= 0)
                    {
                        String sKey = (String)aCmd.get(ii - 1);

                        String sKeyValue = (String)aFieldKeyWords.get(sKey);

                        if (sKeyValue == null)
                        {
                            continue;
                        }
                        sCmdWord = StringUtils.replaceAll(sCmdWord, KEY_SYMBOLIC_NAME_START + sUnresolvedRef + KEY_SYMBOLIC_NAME_END, sUnresolvedRefName);
                        if (sAdminType.equals("relationship"))
                        {
                            if (bIsTo)
                            {
                                sReturnCmd += "to " + sKeyValue + " " + sCmdWord + " ";
                            }
                            else
                            {
                                sReturnCmd += "from " + sKeyValue + " " + sCmdWord + " ";
                            }
                        }
                        else
                        {
                           sReturnCmd += sKeyValue + " " + sCmdWord + " ";
                           if (sKey.startsWith("member") || sKey.startsWith("add member"))
                           {
                               sReturnCmd += (String)aCmd.get(ii + 1) + " " + (String)aCmd.get(ii + 2);
                           }

                        }
                    }
                }
            }
            if (sReturnCmd.length() > 0)
            {
                sReturnCmd = "modify " + sAdminType +  " \"" + sAdminName + "\" " + sReturnCmd;
            }
            aReturnCmd.add(sReturnCmd);
        }

        for (int i = 0; i < aFoundFef.size(); i++)
        {
            String sFoundRef = (String)aFoundFef.get(i);
            emxInstallUtil_mxJPO.executeMQLCommand(context, "delete property mxSym_" + sFoundRef + " on " + sAdminType + " \"" + sAdminName + "\" ");
            aUnresolvedRef.remove(aUnresolvedRef.indexOf(sFoundRef));
        }

        return aReturnCmd;
    }

    /**
     * Resolves symbolic name references from MQL command that don't exists
     * @param context the eMatrix <code>Context</code> object
     * @param sCmd the command to be processed
     * @param sAdminSymbolicName symbolic name of admin object
     * @param aFieldKeyWords keys to be searched in MQL
     * @param sInputCmd MQL command
     * @returns new command without unresolved admins.
     * @throws Exception if the operation fails
     * @String returns command with replaced env variables.
     * @since AEF 10.Next
     */
    private static String resolveUsers(Context context, String sAdminSymbolicName, String sAdminName, ArrayList aInputCmd)
        throws Exception
    {
        ArrayList aReturnCmd = new ArrayList();
        ArrayList aUnresolvedRef = emxAdminCache_mxJPO.getUnresolvedReferences(context, sAdminSymbolicName);

        ArrayList aFoundFef = new ArrayList();
        ArrayList aNotFoundFef = new ArrayList();
        ArrayList aFieldKeyWords = new ArrayList();
        aFieldKeyWords.add("user");
        aFieldKeyWords.add("add user");
        aFieldKeyWords.add("remove user");
        ArrayList aAdminList = new ArrayList();
        aAdminList.add("person");
        aAdminList.add("role");
        aAdminList.add("group");
        aAdminList.add("association");

        String sAdminType = sAdminSymbolicName.substring(0, sAdminSymbolicName.indexOf('_'));
        TreeMap mStateInfo = new TreeMap();

        for (int i = 0; i < aInputCmd.size(); i++)
        {
            String sReturnCmd = "";
            String sInputCmd = (String)aInputCmd.get(i);

            ArrayList aCmd = getWordsFromCmd(context, sInputCmd, aFieldKeyWords);
            String sCurrentState = null;
            for (int ii = 0; ii < aCmd.size(); ii++)
            {
                String sCmdWord = (String)aCmd.get(ii);

                if (sCmdWord.equals("state") || sCmdWord.equals("allstate"))
                {
                    if (sCmdWord.equals("state")) {
                        sCurrentState = (String)aCmd.get(ii + 1);
				    } else {
						sCurrentState = "allstate";
					}

                    if (sCurrentState.startsWith("\"") || sCurrentState.startsWith("'"))
                    {
                        sCurrentState = sCurrentState.substring(1, sCurrentState.length() - 1);
                        if (sCurrentState.indexOf(KEY_SYMBOLIC_NAME_START) >= 0)
                        {
                             try
                             {
                                 sCurrentState = replaceSymbolicNames(context, sAdminSymbolicName, sAdminName, sCurrentState);
                             }
                             catch(Exception Ex)
                             {
                                 sCurrentState = null;
                                 continue;
                             }
                        }
                    }

                    if (!sCurrentState.equals("allstate")) {
						if (!emxAdminCache_mxJPO.isStateExists(context, sAdminSymbolicName, sCurrentState))
						{
							String sNewSymName = "state_" + StringUtils.replaceAll(sCurrentState, " ", "");
							String sNewStateName = emxAdminCache_mxJPO.getStateName(context, sAdminSymbolicName, sNewSymName);

							if (sNewStateName == null || sNewStateName.length() == 0)
							{
								sCurrentState = null;
								continue;
							}
							else
							{
								sCurrentState = sNewStateName;
							}
						}
					}

                }
                if (sCurrentState == null)
                {
                    continue;
                }

                for (int iii = 0; iii < aUnresolvedRef.size(); iii++)
                {
                    String sUnresolvedRef = (String)aUnresolvedRef.get(iii);

                    String sUnresolvedRefAdmin = sUnresolvedRef.substring(0, sUnresolvedRef.indexOf('_'));

                    if (!aAdminList.contains(sUnresolvedRefAdmin))
                    {
                        continue;
                    }

                    String sUnresolvedRefName = emxAdminCache_mxJPO.getName(context, sUnresolvedRef);
                    if (sUnresolvedRefName == null || sUnresolvedRefName.length() == 0)
                    {
                        continue;
                    }
                    else
                    {

                        if(sCmdWord.indexOf(KEY_SYMBOLIC_NAME_START + sUnresolvedRef + KEY_SYMBOLIC_NAME_END) >= 0)
                        {
                            String sKey = (String)aCmd.get(ii - 1);

                            if (!aFieldKeyWords.contains(sKey))
                            {
                                continue;
                            }

                            sCmdWord = StringUtils.replaceAll(sCmdWord, KEY_SYMBOLIC_NAME_START + sUnresolvedRef + KEY_SYMBOLIC_NAME_END, sUnresolvedRefName);
                            TreeMap mUserInfo = (TreeMap)emxInstallUtil_mxJPO.getKeyValue(mStateInfo, sCurrentState, TreeMap.class);
                            ArrayList aUserInfo = (ArrayList)emxInstallUtil_mxJPO.getKeyValue(mUserInfo, sUnresolvedRef, ArrayList.class);
                            ArrayList sAccessAndFilter = new ArrayList(2);
                            aUserInfo.add(sAccessAndFilter);

                            String sAccess = (String)aCmd.get(++ii);
                            String sKeyValue = null;
                            if (sAccess.equals("key"))
                            {
								sKeyValue = (String)aCmd.get(++ii);
								sAccess = (String)aCmd.get(++ii);
							}


							boolean found = true;
							String sufixCmd = "";
							for (int j = 0; j < 5 && found; j++) {
								if (aCmd.size() - 1 > ii + 2) {
									if (((String)aCmd.get(ii + 2)).equals("project")) {
										sufixCmd += (String)aCmd.get(++ii) + " " + "project ";
										ii++;
										continue;
									}
									if (((String)aCmd.get(ii + 2)).equals("organization")) {
										sufixCmd += (String)aCmd.get(++ii) + " " + "organization ";
										ii++;
										continue;
									}
									if (((String)aCmd.get(ii + 2)).equals("owner")) {
										sufixCmd += (String)aCmd.get(++ii) + " " + "owner ";
										ii++;
										continue;
									}
									if (((String)aCmd.get(ii + 2)).equals("reserve")) {
										sufixCmd += (String)aCmd.get(++ii) + " " + "reserve ";
										ii++;
										continue;
									}
									if (((String)aCmd.get(ii + 2)).equals("maturity")) {
										sufixCmd += (String)aCmd.get(++ii) + " " + "maturity ";
										ii++;
										continue;
									}

									found = false;
								}
							}

                            String sUserCmd = sKey + " " + sCmdWord + " ";
                            if (sKeyValue != null) {
								sUserCmd += "key " + sKeyValue + " ";
							}
							sUserCmd += sAccess + " " + sufixCmd + " ";

                            if (aCmd.size() - 1 >= ii + 2 && (((String)aCmd.get(ii + 1)).equals("filter") || ((String)aCmd.get(ii + 1)).equals("localfilter")))
                            {
								 String sFilterWord = (String)aCmd.get(++ii);

                                 String sFilter = (String)aCmd.get(++ii);

                                 try
                                 {
                                     sFilter = replaceSymbolicNames(context, sAdminSymbolicName, sAdminName, sFilter);
                                 }
                                 catch(Exception ex)
                                 {
									 sAccessAndFilter.add(sUserCmd);
                                     sAccessAndFilter.add("UNRESOLVED");
                                     continue;
                                 }
                                 sUserCmd += sFilterWord + " ";


                                 sAccessAndFilter.add(sUserCmd);
                                 sAccessAndFilter.add(sFilter);
                            }
                            else
                            {
								 sAccessAndFilter.add(sUserCmd);
							}
                        }
                    }
                }
            }
        }

        Iterator i = mStateInfo.keySet().iterator();
        String sReturnCmd = "";
        while (i.hasNext())
        {
            String sCurrentState = (String)i.next();
            TreeMap mUserInfo = (TreeMap)mStateInfo.get(sCurrentState);

            Iterator ii = mUserInfo.keySet().iterator();
            while (ii.hasNext())
            {
                String sCurrentUserProp = (String)ii.next();
                String sCurrentUser = emxAdminCache_mxJPO.getName(context, sCurrentUserProp);

                ArrayList aAccessInfo = (ArrayList)mUserInfo.get(sCurrentUserProp);

                boolean bUnResolved = false;
                for (int iii = aAccessInfo.size() - 1; iii >= 0; iii--)
                {
                    ArrayList aCmd = (ArrayList)aAccessInfo.get(iii);

                    if (aCmd.size() == 2)
                    {
                        if (((String)aCmd.get(1)).equals("UNRESOLVED"))
                        {
                            bUnResolved = true;
                            if (!aNotFoundFef.contains(sCurrentUserProp)) {
                                aNotFoundFef.add(sCurrentUserProp);
                            }
                            break;
                        }
                        else
                        {
                            bUnResolved = false;
                            if (!aFoundFef.contains(sCurrentUserProp)) {
                                aFoundFef.add(sCurrentUserProp);
                            }
                            break;
                        }
                    }
                    else
                    {
                        if (!aFoundFef.contains(sCurrentUserProp)) {
                            aFoundFef.add(sCurrentUserProp);
                        }
                    }
                }

                if (!bUnResolved)
                {
                    for (int iii = 0; iii < aAccessInfo.size(); iii++)
                    {
                        ArrayList aCmd = (ArrayList)aAccessInfo.get(iii);
                        if (sCurrentState.equals("allstate")) {
							sReturnCmd += sCurrentState + " " + aCmd.get(0) + " ";
						} else {
                            sReturnCmd += "state \"" + sCurrentState + "\" " + aCmd.get(0) + " ";

						}

                        if (aCmd.size() == 2)
                        {
                            if (!((String)aCmd.get(1)).equals("UNRESOLVED"))
                            {
                                sReturnCmd += aCmd.get(1);
                            }
                        }
                    }
                }
                else
                {
					if (sCurrentState.equals("allstate")) {
						sReturnCmd += sCurrentState + " remove user \"" + sCurrentUser + "\" all ";
					} else {
						sReturnCmd += "state \"" + sCurrentState + "\" remove user \"" + sCurrentUser + "\" all ";
					}

                }

            }
        }

        for (int j = 0; j < aFoundFef.size(); j++)
        {
            String sFoundRef = (String)aFoundFef.get(j);
            if (!aNotFoundFef.contains(sFoundRef))
            {
                emxInstallUtil_mxJPO.executeMQLCommand(context, "delete property mxSym_" + sFoundRef + " on " + sAdminType + " \"" + sAdminName + "\" ");
                aUnresolvedRef.remove(aUnresolvedRef.indexOf(sFoundRef));
            }
        }

        if (sReturnCmd.length() > 0) {
            sReturnCmd = "modify policy \"" + sAdminName + "\" " + sReturnCmd;
        }
        return sReturnCmd;
    }

}
