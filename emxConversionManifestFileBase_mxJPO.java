/*
** emxConversionManifestFileBase
**
**  Copyright (c) 1992-2015 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import java.io.File;

import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.input.SAXBuilder;

import matrix.db.Context;

/**
 * The <code>emxConversionManifestFileBase</code> class contains methods for ActionLinkAccess.
 *
 * @version AEF 10.Next - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxConversionManifestFileBase_mxJPO
{
    // conversion manifest File Keys
    private static final String KEY_VERSION = "Version";
    private static final String KEY_PREINSTALL = "PreInstall";
    private static final String KEY_POSTINSTALL = "PostInstall";
    private static final String KEY_CONVERSION = "Conversion";
    private static final String KEY_PROGRAM = "Program";
    private static final String ATTRIBUTE_CONVERSION_TYPE = "Type";
    private static final String ATTRIBUTE_CONVERSION_TYPE_TCL = "TCL";
    private static final String ATTRIBUTE_CONVERSION_TYPE_JPO = "JPO";
    private static final String KEY_CONSTRUCTOR_ARGS = "ConstructorArg";
    private static final String KEY_METHOD = "Method";
    private static final String KEY_METHOD_ARGS = "MethodArg";

    private static final String KEY_NAME = "Name";
    private static final String KEY_PRE_REQUISITE_ADMINS = "PreRequisiteAdmins";

    // conversion manifest file attributes
    private static final String ATTRIBUTE_ID = "ID";
    private static final String ATTRIBUTE_VERSION = "Version";

    // Directory name will be cached here
    private String conversionManifestFileDir = null;

    // Conversion Manifest files will be cached here
    private Document conversionManifestFile = null;

    // System information hook
    private static final String SYSTEM_INFORMATION_PROGRAM = "eServiceSystemInformation.tcl";

    /**
     * This method loads conversion manifest file.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param xmlFile holds conversion manifest file name with full path
     * @param bReloadCache if set to true it will reload the cache
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public void loadConversionManifestFile(Context context, File xmlFile)
        throws Exception
    {
        // if reload cache is set to ture or
        // if xml file is not cached then parse xml file
        SAXBuilder xmlBuilder = new SAXBuilder();
        xmlBuilder.setFeature("http://xml.org/sax/features/external-general-entities", false);
        xmlBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        Document xmlDoc = xmlBuilder.build(xmlFile);
        conversionManifestFile = xmlDoc;

        // get directory
        conversionManifestFileDir = xmlFile.getParent();
        conversionManifestFileDir = conversionManifestFileDir.replace('\\', '/');
    }

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param conMftFile holds conversion manifest file handle
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public emxConversionManifestFileBase_mxJPO (Context context, File conMftFile)
        throws Exception
    {
        loadConversionManifestFile(context, conMftFile);
    }

    /**
     * check if conversion is already executed
     *
     * @param context the eMatrix <code>Context</code> object
     * @param sConversion name
     * @returns true if executed else false
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public boolean isConversionExecuted(Context context, String sConversion)
        throws Exception
    {
        String sCmd = "print program \"" + SYSTEM_INFORMATION_PROGRAM + "\" " +
                      "select property[" + sConversion + "].value " +
                      "dump |";
        String sResult = emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);

        if (sResult.equals("Executed"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * check if pre requisite admins are installed and upto required version
     *
     * @param context the eMatrix <code>Context</code> object
     * @param aPreRequisiteAdmins ArrayList holding all pre requisite admins
     * @returns true if all admins exists else false
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public boolean checkPreRequisiteAdmins(Context context, ArrayList aPreRequisiteAdmins)
        throws Exception
    {
        for (int i = 0; aPreRequisiteAdmins != null && i < aPreRequisiteAdmins.size(); i++)
        {
            String sAdminSymName = (String)aPreRequisiteAdmins.get(i);
            String sAdminName = emxAdminCache_mxJPO.getName(context, sAdminSymName);

            if (sAdminName == null || sAdminName.length() == 0)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * gets pre conversions
     *
     * @param context the eMatrix <code>Context</code> object
     * @param sVersion version name
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public ArrayList getPreConversions(Context context, String sVersion)
        throws Exception
    {
        return (getConversions(context, sVersion, true));
    }

    /**
     * gets post conversions
     *
     * @param context the eMatrix <code>Context</code> object
     * @param sVersion version name
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public ArrayList getPostConversions(Context context, String sVersion)
        throws Exception
    {
        return (getConversions(context, sVersion, false));
    }

    /**
     * gets conversions
     *
     * @param context the eMatrix <code>Context</code> object
     * @param sVersion version name
     * @param isPreInstall returns pre install conversion if true else post
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public ArrayList getConversions(Context context, String sVersion, boolean isPreInstall)
        throws Exception
    {
        // return list
        ArrayList aConversions = new ArrayList();

        // Get root
        Element root = conversionManifestFile.getRootElement();

        // Get all the versions
        List lVersions = root.getChildren(KEY_VERSION);

        // for each child node
        for (int i = 0; lVersions != null && i < lVersions.size(); i++)
        {
            Element eVersion = (Element)lVersions.get(i);

            if (sVersion.equals(eVersion.getAttributeValue(ATTRIBUTE_ID).trim()))
            {
                // Get pre/post install section
                Element eInstall = null;
                if (isPreInstall)
                {
                    eInstall = eVersion.getChild(KEY_PREINSTALL);
                }
                else
                {
                    eInstall = eVersion.getChild(KEY_POSTINSTALL);
                }

                if (eInstall != null)
                {
                    List lConversions = eInstall.getChildren(KEY_CONVERSION);

                    // for each conversion
                    for (int ii = 0; lConversions != null && ii < lConversions.size(); ii++)
                    {
                        // get conversion
                        Element eConversion = (Element)lConversions.get(ii);

                        // get name
                        String sName = eConversion.getChildText(KEY_NAME).trim();
                        // get pre requisite admins
                        String sPreRequisiteAdmins = eConversion.getChildText(KEY_PRE_REQUISITE_ADMINS).trim();
                        ArrayList aPreRequisiteAdmins = new ArrayList();
                        StringTokenizer stPreRequisiteAdmins = new StringTokenizer(sPreRequisiteAdmins, "\n");
                        while(stPreRequisiteAdmins.hasMoreTokens())
                        {
                            String sPreRequisiteAdmin = stPreRequisiteAdmins.nextToken().trim();
                            if (sPreRequisiteAdmin != null && sPreRequisiteAdmin.length() > 0)
                            {
                                aPreRequisiteAdmins.add(sPreRequisiteAdmin);
                            }
                        }

                        if (!isConversionExecuted(context, sName) && checkPreRequisiteAdmins(context, aPreRequisiteAdmins))
                        {
                            // get program
                            Element eProgram = eConversion.getChild(KEY_PROGRAM);
                            String sProgramType = eProgram.getAttributeValue(ATTRIBUTE_CONVERSION_TYPE);
                            String sProgram = eProgram.getText().trim();
                            if (sProgramType.equals(ATTRIBUTE_CONVERSION_TYPE_TCL))
                            {
                                aConversions.add("run \"" + conversionManifestFileDir + File.separator + sProgram + "\"");
                            }
                            else
                            {
                                String sCompileProgCmd = "compile program \"" + sProgram + "\";";
								aConversions.add(sCompileProgCmd);

								String sCmd = "exec program \"" + sProgram + "\" ";

                                // Get constructor args
                                List lConstArgs = eConversion.getChildren(KEY_CONSTRUCTOR_ARGS);
                                if (lConstArgs != null && !lConstArgs.isEmpty())
                                {
                                    for (int iii = 0; iii < lConstArgs.size(); iii++)
                                    {
                                        Element eConstArg = (Element)lConstArgs.get(iii);
                                        sCmd += "-construct \"" + eConstArg.getText().trim() + "\" ";
                                    }
                                }

                                // get method name
                                String sMethod = eConversion.getChildText(KEY_METHOD);
                                if (sMethod != null)
                                {
                                    sCmd += "-method " + sMethod + " ";
                                }
                                else
                                {
                                    sCmd += "-method mxMain ";
                                }

                                // Get method args
                                List lMethodArgs = eConversion.getChildren(KEY_METHOD_ARGS);
                                if (lMethodArgs != null && !lMethodArgs.isEmpty())
                                {
                                    for (int iii = 0; iii < lMethodArgs.size(); iii++)
                                    {
                                        Element eMethodArg = (Element)lMethodArgs.get(iii);
                                        sCmd += "\"" + eMethodArg.getText().trim() + "\" ";
                                    }
                                }
                                aConversions.add(sCmd);
                            }
                        }
                    }
                }

                break;
            }
        }

        return aConversions;
    }
}
