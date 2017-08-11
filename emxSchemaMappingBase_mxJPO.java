/*
** emxSchemaMappingBase
**
**  Copyright (c) 1992-2015 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
**   static const char RCSID[] = $Id: emxSchemaMappingBase.java.rca 1.13 Tue Oct 28 23:01:45 2008 przemek Experimental przemek $
*/

import java.util.TreeMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileOutputStream;

import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.input.SAXBuilder;

import matrix.db.Context;


/**
 * The <code>emxSchemaMappingBase</code> class contains methods for ActionLinkAccess.
 *
 * @version AEF 10.Next - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxSchemaMappingBase_mxJPO
{
    // Schema Manifest Info Keys
    private static final String KEY_SCHEMA_MANIFEST = "SchemaManifest";
    private static final String KEY_NAME = "Name";
    private static final String KEY_DESCRIPTION = "Description";
    private static final String KEY_FORCE_INSTALL = "ForceInstall";
    private static final String KEY_FILE_NAME = "FileName";
    private static final String KEY_ORDER = "Order";
    private static final String KEY_DISPLAY = "Display";
    private static final String KEY_CONVERSIONS = "Conversions";
    private static final String KEY_PRE_REQUISITE_SCHEMA = "PreRequisiteSchema";
    private static final String ATTRIBUTE_REMOVE = "Remove";
    private static final String ATTRIBUTE_REMOVE_YES = "Yes";
    private static final String ATTRIBUTE_REMOVE_NO = "No";

    // Schema Manifest info will be cached here
    private Document schemaMapping = null;

    // Schema Manifest file directory
    private String schemaMappingDir = null;

    /**
     * Constructor.
     *
     * Should be used while firing JPO throgh command line
     * @param context the eMatrix <code>Context</code> object
     * @param args[0] holds schema definition file name with full path
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public emxSchemaMappingBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        // throw exception if improper arguments are passed
        if (args.length < 1)
        {
            throw (new Exception("Could not instantiate JPO:  Schema definition xml file name should be passed as input parameter."));
        }

        // Get reload cache flag is on.
        // by default it will be set to false.
        boolean bReloadCache = true;
        if (args.length >= 2 && args[1].equalsIgnoreCase("FALSE"))
        {
            bReloadCache = false;
        }

        // load schema manifest info file
        loadSchemaMapping(context, args[0], bReloadCache);
    }


    /**
     * Constructor.
     *
     * Should be used while firing JPO throgh command line
     * @param context the eMatrix <code>Context</code> object
     * @param args[0] holds schema definition file name with full path
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public emxSchemaMappingBase_mxJPO (Context context, File xmlFile, boolean bReloadCache)
        throws Exception
    {
        // load schema manifest info file
        loadSchemaMapping(context, xmlFile, bReloadCache);
    }

    /**
     * This method loads schema definition file.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param xmlFile holds schema manifest info file name with full path
     * @param bReloadCache if set to true it will reload the cache
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public void loadSchemaMapping(Context context, String xmlFile, boolean bReloadCache)
        throws Exception
    {
        loadSchemaMapping(context, new File(xmlFile), bReloadCache);
    }

    /**
     * This method loads schema definition file.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param xmlFile holds schema manifest info file
     * @param bReloadCache if set to true it will reload the cache
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public void loadSchemaMapping(Context context, File xmlFile, boolean bReloadCache)
        throws Exception
    {
        // if reload cache is set to ture or
        // if xml file is not cached then parse xml file
        if (bReloadCache || schemaMapping == null)
        {
            SAXBuilder xmlBuilder = new SAXBuilder();
            xmlBuilder.setFeature("http://xml.org/sax/features/external-general-entities", false);
            xmlBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            Document xmlDoc = xmlBuilder.build(xmlFile);
            schemaMapping = xmlDoc;
            schemaMappingDir = xmlFile.getParent();
        }
    }

    /**
     * Get ordered schema names
     *
     * @param context the eMatrix <code>Context</code> object
     * @param customMapping custom schema mapping file
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public TreeMap getOrderedSchemaElements(Context context, emxSchemaMappingBase_mxJPO customMapping)
        throws Exception
    {
        TreeMap lReturnList = new TreeMap();

        // if custom mapping found then
        if (customMapping != null)
        {
            lReturnList = customMapping.getOrderedSchemaElements(context, null);

        }
        // get root
        Element root = schemaMapping.getRootElement();
        // get childrens
        List lSchemas = root.getChildren(KEY_SCHEMA_MANIFEST);
        // for each manifest node
        for (int i = 0; lSchemas != null && i < lSchemas.size(); i++)
        {
            Element eSchema = (Element)lSchemas.get(i);

            // get name
            String sName = eSchema.getChildText(KEY_NAME).trim();
            // get order
            String sOrder = eSchema.getChildText(KEY_ORDER).trim();

            // skip if name already exists
            boolean bFound = false;
            Iterator itr = lReturnList.keySet().iterator();
            while(itr.hasNext())
            {
                Float fKey = (Float)itr.next();
                Element e = (Element)lReturnList.get(fKey);
                if (sName.equals(e.getChildText(KEY_NAME).trim()))
                {
                    bFound = true;
                }
            }
            if (bFound)
            {
                continue;
            }

            // increament order number if it already exists
            Float fOrder = new Float(sOrder);
            while (lReturnList.containsKey(fOrder))
            {
                 float fNewOrder = (float)fOrder.floatValue() + (float)0.01;
                 fOrder = new Float(fNewOrder);
            }

            lReturnList.put(fOrder, eSchema);
        }

        return lReturnList;
    }

    /**
     * Get Pre Requisite Schema hierarchy
     *
     * @param context the eMatrix <code>Context</code> object
     * @param iniFileName of the ini info file
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public ArrayList GetPreSchemaHierarchy(Context context, String sSchemaName, emxSchemaMappingBase_mxJPO customMapping)
        throws Exception
    {
        // return list
        ArrayList lPreSchema = new ArrayList();

        // Get ordered schema names
        ArrayList lSchemaNames = new ArrayList(getOrderedSchemaElements(context, customMapping).values());
        for (int i = 0; lSchemaNames != null && i < lSchemaNames.size(); i++)
        {
            Element eSchema = (Element)lSchemaNames.get(i);

            // get remove attribute
            String sIsRemove = eSchema.getAttributeValue(ATTRIBUTE_REMOVE);
            if (sIsRemove != null && sIsRemove.equalsIgnoreCase(ATTRIBUTE_REMOVE_YES))
            {
                continue;
            }

            // get name
            String sName = eSchema.getChildText(KEY_NAME).trim();
            if (sName.equals(sSchemaName))
            {
                String sPreSchemas = eSchema.getChildText(KEY_PRE_REQUISITE_SCHEMA).trim();
                if (sPreSchemas.length() != 0)
                {
                    StringTokenizer stPreSchema = new StringTokenizer(sPreSchemas, "\n");
                    while (stPreSchema.hasMoreTokens())
                    {
                         String sPreSchema = stPreSchema.nextToken().trim();
                         if (sPreSchema.length() == 0)
                         {
                             continue;
                         }
                         lPreSchema.addAll(GetPreSchemaHierarchy(context, sPreSchema, customMapping));
                         lPreSchema.add(sPreSchema);
                    }
                }
            }
        }
        return lPreSchema;
    }

    /**
     * Creates ini info file
     *
     * @param context the eMatrix <code>Context</code> object
     * @param iniFileName of the ini info file
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public void createIniInfoFile(Context context, String customDir, String iniFileName)
        throws Exception
    {
        emxSchemaMappingBase_mxJPO customMapping =  null;

        // if custom directory specified then
        if (customDir != null || customDir.length() > 0)
        {
            File fCustomMapping = new File(customDir, "SchemaMapping.xml");

            if (fCustomMapping.exists())
            {
                customMapping = new emxSchemaMappingBase_mxJPO(context, fCustomMapping, true);
            }
        }

        // Get ordered schema names
        ArrayList lSchemaNames = new ArrayList(getOrderedSchemaElements(context, customMapping).values());

        // for each schema item
        String sSchemaNamesSection = "[SchemaNames]\n";

        sSchemaNamesSection += "Order=";
        String sSchemaDetailsSection = "";
        for (int i = 0; lSchemaNames != null && i < lSchemaNames.size(); i++)
        {
            Element eSchema = (Element)lSchemaNames.get(i);
            // get remove attribute
            String sIsRemove = eSchema.getAttributeValue(ATTRIBUTE_REMOVE);
            if (sIsRemove != null && sIsRemove.equalsIgnoreCase(ATTRIBUTE_REMOVE_YES))
            {
                continue;
            }

            // get file name
            String sFileName = eSchema.getChildText(KEY_FILE_NAME).trim();
            if (new File(customDir, sFileName).exists() || new File(schemaMappingDir ,sFileName).exists())
            {
                // get name
                String sName = eSchema.getChildText(KEY_NAME).trim();
                sSchemaNamesSection += sName + ",";
                sSchemaDetailsSection += "[" + sName + "]\n";
                // get display name
                String sDescription = eSchema.getChildText(KEY_DESCRIPTION);
                if (sDescription == null) {
					sDescription = "None";
				}
				sSchemaDetailsSection += "Description=" + sDescription.trim() + "\n";
                sSchemaDetailsSection += "FileName=" + sFileName + "\n";
                // get display
                String sDisplay = eSchema.getChildText(KEY_DISPLAY).trim();
                sSchemaDetailsSection += "Display=" + sDisplay + "\n";
                // get force install name
                String sForceInstall = eSchema.getChildText(KEY_FORCE_INSTALL);
                if (sForceInstall == null) {
					sForceInstall = "No";
				}
				sSchemaDetailsSection += "ForceInstall=" + sForceInstall.trim() + "\n";
                // get conversion manifest file
                String sConversions = eSchema.getChildText(KEY_CONVERSIONS).trim();
                if (sConversions.length() == 0)
                {
                    sConversions = "None";
                }
                sSchemaDetailsSection += "Conversions=" + sConversions + "\n";
                // get PreRequisiteSchema
                ArrayList aPreSchema = GetPreSchemaHierarchy(context, sName, customMapping);

                if (aPreSchema != null && !aPreSchema.isEmpty())
                {
                    sSchemaDetailsSection += "PreRequisiteSchema=";
                    Iterator ii = aPreSchema.iterator();
                    while (ii.hasNext())
                    {
                         String sPreSchema = (String)ii.next();
                         sSchemaDetailsSection += sPreSchema + ",";
                    }
                    int iLastIndex = sSchemaDetailsSection.lastIndexOf(',');
                    if (iLastIndex >= 0)
                    {
                        sSchemaDetailsSection = sSchemaDetailsSection.substring(0, iLastIndex);
                    }
                }
                else
                {
                    sSchemaDetailsSection += "PreRequisiteSchema=None";
                }
                sSchemaDetailsSection += "\n\n";

            }
        }
        int iLastIndex = sSchemaNamesSection.lastIndexOf(',');
        if (iLastIndex >= 0)
        {
            sSchemaNamesSection = sSchemaNamesSection.substring(0, iLastIndex);
        }
        sSchemaNamesSection += "\n\n";

        // create ini file
        PrintWriter iniFile = new PrintWriter(new FileOutputStream(iniFileName), true);
        iniFile.print(sSchemaNamesSection + sSchemaDetailsSection);
        iniFile.close();
    }

    /**
     * Creates ini info file
     *
     * @param context the eMatrix <code>Context</code> object
     * @param iniFileName of the ini info file
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public void createIniInfoFile(Context context, String iniFileName)
        throws Exception
    {
        createIniInfoFile(context, null, iniFileName);
    }

    /**
     * Creates ini info file
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args[0] ini file name
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public void createIniInfoFile(Context context, String args[])
        throws Exception
    {
        if (args.length < 1)
        {
            throw (new Exception("Could not instantiate JPO:  Schema Manifest Info xml file name should be passed as input parameter."));
        }

        if (args.length == 1)
        {
            createIniInfoFile(context, args[0]);
        }

        if (args.length == 2)
        {
            createIniInfoFile(context, args[0], args[1]);
        }
    }
}
