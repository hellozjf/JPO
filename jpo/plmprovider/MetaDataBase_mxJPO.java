package jpo.plmprovider;
// MetaDataBase.java
//
// Created on Jun 19, 2006
//
// Copyright (c) 2005-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UITable;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.jsystem.util.MxLinkedHashMap;
import com.matrixone.apps.plmprovider.AttributeDefinition;
import com.matrixone.apps.plmprovider.MetaInfo;
import com.matrixone.apps.plmprovider.NodeType;
import com.matrixone.apps.plmprovider.PlmProviderUtil;


/**
 * @author bucknam
 *
 * The <code>MetaDataBase</code> class provides web services associated with meta data.
 *
 * @version AEF 10.7.1.0 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class MetaDataBase_mxJPO extends jpo.plmprovider.Mat3DLive_mxJPO
{
    //constants for the 3D Live property keys - GShilpi
    private static final String PROPERTY_3DLIVE_TRACE = "3DLive.Trace";
    private static final String PROPERTY_3DLIVE_ENVIRONMENT = "3DLive.Environments";
    private static final String PROPERTY_3DLIVE_APPLICATION = "3DLive.Applications";
    private static final String PROPERTY_3DLIVE_DOMAIN_PREFIX = "3DLive.Domains.";
    private static final String PROPERTY_3DLIVE_EXPAND_REL_PATTERN_PREFIX = "3DLive.ExpandRelationshipPattern.";
    private static final String PROPERTY_3DLIVE_EXPAND_TYPE_PATTERN_PREFIX = "3DLive.ExpandTypePattern.";
    private static final String PROPERTY_3DLIVE_GET_PARENTS_REL_PATTERN_PREFIX = "3DLive.GetParentsRelationshipPattern.";
    private static final String PROPERTY_3DLIVE_GET_PARENTS_TYPE_PATTERN_PREFIX = "3DLive.GetParentsTypePattern.";
    private static final String PROPERTY_3DLIVE_SKIP_INTERMEDIATE_NODES_DURING_EXPAND = "3DLive.SkipIntermediateNodesDuringExpand.";
    private static final String PROPERTY_3DLIVE_DOCUMENT_REL_PATTERN_PREFIX = "3DLive.DocumentRelationshipPattern.";
    private static final String PROPERTY_3DLIVE_VIEW_TABLE_PREFIX = "3DLive.ViewTable.";
    private static final String PROPERTY_3DLIVE_IMAGE_PATH_PREFIX = "3DLive.ImagePaths.";
    private static final String PROPERTY_3DLIVE_IMAGE_FORMAT_PREFIX = "3DLive.ImageFormats.";
    private static final String PROPERTY_3DLIVE_THUMBNAIL_PATH_PREFIX = "3DLive.ThumbnailPaths.";
    private static final String PROPERTY_3DLIVE_THUMBNAIL_FORMAT_PREFIX = "3DLive.ThumbnailFormats.";
    private static String type_DOCUMENTS = "";

    //hash map of attribute selects keyed by attribute hash names
    private HashMap attributeNameToSelectMap;

    //hash map of attribute types keyed by attribute hash names
    private HashMap attributeNameToTypeMap;
    
    /**
     * Constructor.
     *
     * @since AEF 10.7.1.0
     */

    public MetaDataBase_mxJPO()
    {
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an int 0, status code.
     * @throws Exception if the operation fails
     * @since AEF 10.7.1.0
     */
    public int mxMain(Context context, String[] args)
    throws Exception
    {
        if (true)
        {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.MetaData.SpecifyMethodOnMetaDataInvocation", context.getLocale().getLanguage()));
        }
        return 0;
    }

    /**
     * Get the meta info modeled on this server.  This is the web service
     * entry point.
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param language the language for translations
     * @return the meta info
     * @since AEF 10.7.1.0
     */
   public MetaInfo getMetaInfo(String username, String password, String language)
   {
       MetaInfo metaData = null;

       try
       {
           log("getMetaInfo: start");
           initContext(username, password);
           log("getMetaInfo: initContext OK");
           metaData = getMetaData(getContext(), language);
           // printNodes(metaData.getNodeTypes());
           log("getMetaInfo: getMetaData OK");
       }
       catch (Exception e)
       {
           e.printStackTrace();
       }

       return (metaData);
   }
   
   //for rest
   public MetaInfo getMetaInfo(Context context, String [] args)
   {
	   MetaInfo metaData = null;
	   try
       {
	   Map inputMap = JPO.unpackArgs(args);
	   String language = (String)inputMap.get("language");
       

       
           log("getMetaInfo: start");
          // initContext(username, password);
           log("getMetaInfo: initContext OK");
           metaData = getMetaData(context, language);
           // printNodes(metaData.getNodeTypes());
           log("getMetaInfo: getMetaData OK");
       }
       catch (Exception e)
       {
           e.printStackTrace();
       }

       return (metaData);
   }

   /**
    * Get the meta info modeled on this server.  This method is for testing
    * purposes only.
    *
    * @param context the matrix context
    * @param args the only member of the array should contain the language for translations
    * @return MetaInfo
    * @throws Exception if there are errors retrieving the meta info
    * @since AEF 10.7.1.0
    */
   public MetaInfo getMetaData(Context context, String[] args)
       throws Exception
   {
       String language = "en";

       if (args != null)
       {
           language = args[0];
       }

       MetaInfo metaData = getMetaData(context, language);
       printNodes(metaData.getNodeTypes());
       return (metaData);
   }

   /**
    * Get the meta info modeled on this server.
    *
    * @param context the matrix context
    * @param language the language for string translations.
    * @return the meta info
    * @throws Exception if there are errors retrieving the meta info
    * @since AEF 10.7.1.0
    */
   @SuppressWarnings("unchecked")
    private MetaInfo getMetaData(Context context, String language)
    throws Exception
   {

        // default to english if no language passed
        if (language == null || language.length() == 0)
        {
            language = "en";
        }

        log("language: " + language);
       
        try
        {
            // first check if the nodeMap is already loaded
            if (nodeMap == null || nodeMap.size() == 0)
            {
                String traceFlag = EnoviaResourceBundle.getProperty(context, PROPERTY_3DLIVE_TRACE);
                if (traceFlag != null && traceFlag.equalsIgnoreCase("true"))
                {
                    setTrace(context,true);
                }

                type_DOCUMENTS = PropertyUtil.getSchemaProperty(context, "type_DOCUMENTS");
                //Get the application name from property like VPM, TEAM etc
                String strApplications      = "";
                try {
                    strApplications         = EnoviaResourceBundle.getProperty(context, PROPERTY_3DLIVE_APPLICATION);
                }catch (Exception ex) {
                     log("Application property is not found ");
                }
                StringList listApplications = FrameworkUtil.split(strApplications,",");
                //Insert application NONE (i.e Normal application, without any security context)
                listApplications.insertElementAt(PlmProviderUtil.APPLICATION_DEFAULT, 0);
                String applicationName;

                //MetaDataCache will have cache for each application
                //key would be application name and value would HashMap of nodes
                for (int appItr = 0 ; appItr < listApplications.size() ; appItr++) {
                    applicationName  = (String)listApplications.get(appItr);
                    MetaDataCache.put(applicationName, new ArrayList());
                }

                //Get the Environments delimited with ',' from emxSystem.properties
                String strEnvironments      = EnoviaResourceBundle.getProperty(context, PROPERTY_3DLIVE_ENVIRONMENT);
                StringList listEnvironments = FrameworkUtil.split(strEnvironments,",");

                String environmentName;

                log(listEnvironments.size() + " environments found");
                for (int envItr = 0 ; envItr < listEnvironments.size() ; envItr++) {
                    environmentName     = (String) listEnvironments.get(envItr);

                    String environmentNameforProperty = FrameworkUtil.findAndReplace(environmentName, " ", "_");

                    log("environment name " + environmentName);

                    // add node for the environment
                    NodeType environmentNode = addNode(nodeMap,
                                                        environmentName,
                                                        EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource",context.getLocale(), environmentName));

                    for (int appItr = 0 ; appItr < listApplications.size() ; appItr++) {
                        applicationName          = (String)listApplications.get(appItr);
                        String APP_DOMAIN_PREFIX = PROPERTY_3DLIVE_DOMAIN_PREFIX;
                        if (! applicationName.equals(PlmProviderUtil.APPLICATION_DEFAULT)) {
                            APP_DOMAIN_PREFIX = APP_DOMAIN_PREFIX + applicationName + ".";
                        }
                        ((ArrayList)MetaDataCache.get(applicationName)).add(environmentNode);
                        ((ArrayList)MetaDataCache.get(applicationName)).addAll(getDomains(context, APP_DOMAIN_PREFIX , environmentNameforProperty, language));
                    }
                }
                printNodes((NodeType[]) nodeMap.values().toArray(new NodeType[nodeMap.size()]));
            }
        }
        catch (Throwable ex)
        {
            throw new MatrixException(ex.getMessage());
        }
        List appMetaInfoList    =(ArrayList) MetaDataCache.get(PlmProviderUtil.APPLICATION_DEFAULT);
        String userApplication = PlmProviderUtil.getApplication(context);
        if (MetaDataCache.containsKey(userApplication)) {
            appMetaInfoList    = (ArrayList)MetaDataCache.get(userApplication);
        }
        return (new MetaInfo((NodeType[]) appMetaInfoList.toArray(new NodeType[appMetaInfoList.size()])));

    }
   
    /**
     * Returns List of domain typenodes for a given environment
     * 
     * @param context the matrix context
     * @param appDomainPrefix the appDomainPrefix
     *      eg. 3DLive.Domains.
     *          3DLive.Domains.TEAM
     * @param environmentNameforProperty the environment Name
     *      eg.Engineering_Central
     * @param language the language
     * @return List of nodetypes
     * @throws Exception
     */
    private List getDomains(Context context, 
                            String appDomainPrefix,
                            String environmentNameforProperty,
                            String language)
    throws Exception
    {
        String strDomains   = "";
        try{
            strDomains        = EnoviaResourceBundle.getProperty(context, appDomainPrefix+environmentNameforProperty);
        }catch (Exception ex) {
            strDomains = "";
        }
        
        StringList listDomains   = FrameworkUtil.split(strDomains,",");
        Vector assignments       = PersonUtil.getAssignments(context);
        List typeNodeList       = new ArrayList();
        String symbolicTypeName;
        String typeName;
        NodeType typeNode;
        MapList mlcolumns;
        
        Iterator typeItr = listDomains.iterator();
        while (typeItr.hasNext())
        {
        
        symbolicTypeName = (String) typeItr.next();
        log("Symbolic Type Name: " + symbolicTypeName);
        
            try
            {
               // Create a hash based on domain name.  Note that any single
               // type can only be used in one Environment.  Lookup the
               // display name based on the language setting.
               typeName = PropertyUtil.getSchemaProperty(context, symbolicTypeName);
               if(typeName != null && !"".equals(typeName)) {
                 
                   if (nodeMap.containsKey(symbolicTypeName)) {
                       typeNode = (NodeType)nodeMap.get(symbolicTypeName);
                   } else {
                       typeNode = addNode(nodeMap, symbolicTypeName, UINavigatorUtil.getAdminI18NString("Type", typeName, language));
            
                       typeNode.setParentName(environmentNameforProperty);
            
                       // Following statements gets the Expand Relationship Pattern value from emxSystem.properties file
                       typeNode.setExpandRelationshipPattern(getPropertyValue(context, PROPERTY_3DLIVE_EXPAND_REL_PATTERN_PREFIX,
                       environmentNameforProperty, typeName));
            
                       //Following statements gets the Expand Type Pattern value from emxSystem.properties file
                       typeNode.setExpandTypePattern(getPropertyValue(context, PROPERTY_3DLIVE_EXPAND_TYPE_PATTERN_PREFIX,
                       environmentNameforProperty, typeName));
            
                       // Following statements gets the Expand Relationship Pattern value from emxSystem.properties file
                       typeNode.setGetParentsRelationshipPattern(getPropertyValue(context, PROPERTY_3DLIVE_GET_PARENTS_REL_PATTERN_PREFIX,
                       environmentNameforProperty, typeName));
            
                       //Following statements gets the Expand Type Pattern value from emxSystem.properties file
                       typeNode.setGetParentsTypePattern(getPropertyValue(context, PROPERTY_3DLIVE_GET_PARENTS_TYPE_PATTERN_PREFIX,
                       environmentNameforProperty, typeName));
            
                       //Following statements gets the Skip Intermediate Nodes During Expand value from emxSystem.properties file
                       typeNode.setSkipIntermediateNodeDuringExpand(getPropertyValue(context, PROPERTY_3DLIVE_SKIP_INTERMEDIATE_NODES_DURING_EXPAND,
                       environmentNameforProperty, typeName));
            
            
                       //Following statement gets the View Table value from emxSystem.properties file
                       String symbolicViewTable = getPropertyValue(context, PROPERTY_3DLIVE_VIEW_TABLE_PREFIX,
                       environmentNameforProperty, typeName);
                       String strViewTable = PropertyUtil.getSchemaProperty(context, symbolicViewTable);
                       typeNode.setAttributesTableName(strViewTable);
            
                       //Following statement gets the Image Path values (comma separated list) from emxSystem.properties file
                       typeNode.setImagePath(getPropertyValue(context, PROPERTY_3DLIVE_IMAGE_PATH_PREFIX,
                       environmentNameforProperty, typeName));
            
                       //Following statement gets the Image Format values (comma separated list) from emxSystem.properties file
                       typeNode.setImageFormat(getPropertyValue(context, PROPERTY_3DLIVE_IMAGE_FORMAT_PREFIX,
                       environmentNameforProperty, typeName));
            
                       //Following statement gets the Thumbnail Path values (comma separated list) from emxSystem.properties file
                       typeNode.setThumbnailPath(getPropertyValue(context, PROPERTY_3DLIVE_THUMBNAIL_PATH_PREFIX,
                       environmentNameforProperty, typeName));
            
                       //Following statement gets the Thumbnail Format values (comma separated list) from emxSystem.properties file
                       typeNode.setThumbnailFormat(getPropertyValue(context, PROPERTY_3DLIVE_THUMBNAIL_FORMAT_PREFIX,
                       environmentNameforProperty, typeName));
            
                       // Following statements gets the Document Relationship Pattern value from emxSystem.properties file
                       typeNode.putDocumentRelationshipPattern(getPropertyValue(context, PROPERTY_3DLIVE_DOCUMENT_REL_PATTERN_PREFIX,
                       environmentNameforProperty, typeName));
            
                       //Following statement gets the value of exclude versioned documents from PlmProviderUtil
                       //Vesrioned Documents should be excluded, if a this type is a kind of "DOCUMENTS" and not a kind of "MCAD*"
                       typeNode.putExcludeVersionedDocuments(PlmProviderUtil.isKindof(context, typeName, type_DOCUMENTS) && 
                       ! PlmProviderUtil.isKindof(context, typeName, "MCAD*"));
            
                       // put the hash name and type name (not symbolic name) in the map, keyed by hash name
                       hashNameToTypeNameMap.put(typeNode.getName(), typeName);
                       log("hash<<" + typeNode.getName() + ">> type <<" + typeName + ">>");
            
                       // put the hash name and type name (not symbolic name) in the map, keyed by type name
                       typeNameToHashNameMap.put(hashNameToTypeNameMap.get(typeNode.getName()), typeNode.getName());
            
                       log("type name <<" + getTypeFromHash(typeNode.getName()) + ">> hash <<" + typeNode.getName() + ">> display <<" + typeNode.getDisplayName()+ ">>");
            
                       //get column details for the table
                       mlcolumns = getTableColumns(context,strViewTable,assignments,language);
            
                       typeNode.setAttributeDefinitions(processTableColumns(context, environmentNameforProperty, typeName, mlcolumns));
            
                       //put column details in type node
                       typeNode.putTableColumns(mlcolumns);
            
                       //put attribute selects Map(keyed by attribute hash name) in type node
                       typeNode.putTableColumnSelects(attributeNameToSelectMap);
            
                       //put attribute types Map (keyed by attribute hash name) in type node
                       typeNode.putTableColumnTypes(attributeNameToTypeMap);
                   }
                   typeNodeList.add(typeNode);
               } else {
                   log("TYPE does not exist for Symbolic Type Name :" + symbolicTypeName);
               }
            }
            catch (Exception e)
            {
               removeNode(nodeMap, symbolicTypeName);
               log(e.getMessage());
            }
        }
        return typeNodeList;
    }


  /**
   * Add a node to the node map.  If a node of that name already exists,
   * the do not add a new node but return the existing one.
   *
   * @param nodeMap the map of nodes
   * @param name the name of the new node
   * @param displayName the display name of the new node
   * @return the node from the map
   * @throws NoSuchAlgorithmException if the name hash class can not be found
   * @since AEF 10.7.1.0
   */
  private NodeType addNode(Map nodeMap, String name, String displayName)
      throws NoSuchAlgorithmException
  {
      NodeType node = (NodeType) nodeMap.get(name);

      if (node == null)
      {
          node = new NodeType();
          node.setName(getHashName(name));
          node.setDisplayName(displayName);
          nodeMap.put(name, node);
      }

      return (node);
  }


  /**
   * Remove a node from the node map.
   *
   * @param nodeMap the map of nodes
   * @param name the name of the new node
   * @return the node from the map
   * @since AEF 10.7.1.0
   */
  private NodeType removeNode(Map nodeMap, String name)
  {
      return((NodeType) nodeMap.remove(name));
  }

  /**
   * Print the nodes contained in the array.  This method is meant for
   * debugging purposes only.
   *
   * @param nodes the array of node types
   * @since AEF 10.7.1.0
   */
  private void printNodes(NodeType[] nodes)
  {
      try
      {
          String message = "";
          debug(message);

          for (int i = 0; i < nodes.length; i++)
          {
              message = "";
              debug("Name<<" + nodes[i].getName() + " " + nodes[i].getDisplayName() + ">> parent<<" + nodes[i].getParentName() + ">>");
              debug("Image Formats<<" + nodes[i].getImageFormat() + ">> Image Paths<<" + nodes[i].getImagePath() + ">>");
              debug("Thumbnail Formats<<" + nodes[i].getThumbnailFormat() + ">> Thumbnail Paths<<" + nodes[i].getThumbnailPath() + ">>");
              debug("Rel Pattern<<" + nodes[i].getExpandRelationshipPattern() + ">> Type Pattern<<" + nodes[i].getExpandTypePattern() + ">> Table<<" + nodes[i].getAttributesTableName() + ">>");

              AttributeDefinition[] attributes = nodes[i].getAttributeDefinitions();
              if (attributes != null)
              {
                  message = "attributes<<";

                  for (int j = 0; j < attributes.length; j++)
                  {
                      if (j > 0)
                          message += ", ";

                      message += attributes[j].getName();
                      message += " ";
                      message += attributes[j].getDisplayName();
                      message += " ";
                      message += attributes[j].isIsDisplay();
                      message += " ";
                      message += attributes[j].getType();
                  }

                  message += ">>";
              }

              debug(message);
          }
      }
      catch (Exception e)
      {
          e.printStackTrace(System.out);
      }
  }

  /**
   * Send a message to the log.  Made this separate method
   * just to catch the exception.
   *
   * @param message the message for the log
   * @since AEF 10.7.1.0
   */
  private void debug(String message)
  {
      try
      {
          log(message);
      }
      catch (Exception e)
      {
          // do nothing
      }
  }

  /**
   * Add an attribute definition to the list of definitions.
   *
   * @param attributeMap the map of attribute definitions.
   * @param attributeName the name of the attribute
   * @param hashName the hash name of the attribute
   * @param type the type of attribute (string, integer, double, date)
   * @since AEF 10.7.1.0
   */
  private AttributeDefinition addAttribute(HashMap attributeMap, String attributeName, String hashName, String select, String type)
  {
      AttributeDefinition attribute = (AttributeDefinition) attributeMap.get(hashName);

      if (attribute == null)
      {
          attribute = new AttributeDefinition();
          attribute.setName(hashName);

          if ("string".equalsIgnoreCase(type))
            attribute.setType("string");
          else if ("integer".equalsIgnoreCase(type))
            attribute.setType("integer");
          else if ("real".equalsIgnoreCase(type))
            attribute.setType("double");
          else if ("date".equalsIgnoreCase(type))
              attribute.setType("date");
          else if ("boolean".equalsIgnoreCase(type))
              attribute.setType("boolean");
          else
            attribute.setType("string");

          attributeMap.put(hashName, attribute);

          // put the hash name and attribute name (not symbolic name) in the map, keyed by hash name
          hashNameToAttributeNameMap.put(hashName, attributeName);

          // put the hash name and attribute name (not symbolic name) in the map, keyed by attribute name
          attributeNameToHashNameMap.put(attributeName, hashName);

      }

      return (attribute);
  }

  /**
   * Create the attribute definitions from the table columns.
   *
   * @param context the matrix context
   * @param environmentName Name of the Environment which is exposed to 3D Live
   * @param typeName Name of the Domain which is exposed to 3D Live under the Environment environmentName
   * @param mlColumns the MapList of Table Columns
   * @return an array of attribute definitions
   * @throws Exception if the table can not be found
   * @since AEF 10.7.1.0
   */
  private AttributeDefinition[] processTableColumns(Context context, String environmentName, String typeName, MapList mlColumns)
  throws Exception
  {
      MxLinkedHashMap attributes    = null;
      AttributeDefinition attribute = null;

      try
      {
          // build the attribute definitions
          attributes                = new MxLinkedHashMap();
          Iterator columnItr        = mlColumns.iterator();
          String select             = null;
          String displayName        = null;
          String hashName           = null;
          String columnType         = null;
          attributeNameToSelectMap  = new HashMap();
          attributeNameToTypeMap    = new HashMap();
          HashMap column;
 
          for (int i = 0; columnItr.hasNext(); i++)
          {
              column = (HashMap) columnItr.next();
              
              if (isEmptyColumn(column) == false)
              {
                  select       = getColumnSelect(context, column);
                  displayName  = UITable.getLabel(column);
                  hashName     = getHashName(displayName);
                  log("column label <<" + displayName + ">> column select <<" + select +
                          ">> hash <<" + hashName + ">>");

                  attribute = addAttribute(attributes, 
                                      displayName,
                                      hashName,
                                      select,
                                      UITable.getSetting(column, SETTING_SORT_TYPE));
                  
                  attribute.setDisplayName(displayName);
                  
                  columnType = UITable.getSetting(column,SETTING_COLUMN_TYPE);
                  
                  if (columnType != null && (columnType.equals("program")))
                  {
                      attribute.setIsReadOnly(true);
                  }
                  else if(columnType != null && (columnType.equals("businessobject") || columnType.equals("relationship")))
                  {
                      attribute.setIsDisplay("true".equalsIgnoreCase(UITable.getSetting(column, SETTING_EASY_QUERY)));
                  }
                  
                  attribute.setIsInternal("true".equalsIgnoreCase(UITable.getSetting(column, SETTING_HIDDEN_ATTRIBUTE)));

                  // set the "basic" name if this is marked as a basic attribute
                  if ("true".equalsIgnoreCase(UITable.getSetting(column, SETTING_BASIC_ATTRIBUTE)))
                  {
                      attribute.setBasicName(displayName);
                  }
                  
                  attributeNameToSelectMap.put(hashName,select);
                  attributeNameToTypeMap.put(hashName,attribute.getType());

              }
          }
      }
      catch (Exception e)
      {
          throw (new Exception("Invalid 'View Table' or 'View Table' property not defined for the Domain " + typeName + " under the Environment " + environmentName));
      }

      return ((AttributeDefinition[]) attributes.values().toArray(new AttributeDefinition[attributes.size()]));
  }

  /**
   * Return the property value of the given key and type, or any of its parent types.
   * This method is for testing purpose only.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args the array should contain prefix, environment and type.
   * @return the property value
   * @throws Exception if operation fails
   * @since R208
   */  
  public String getPropertyValue(Context context, String args[]) throws Exception
  {
      HashMap programMap            = (HashMap)JPO.unpackArgs(args);
      String prefix                 = (String)programMap.get("prefix");
      String environment            = (String)programMap.get("environment");
      String type                   = (String)programMap.get("type");
      return getPropertyValue(context, prefix, environment, type);
  }   
  
  /**
   * Return the property value of the given key and type, or any of its parent types.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param prefix the property key prefix
   * @param environment the environment to append to the property key
   * @param type the type to append to the property key
   * @return the property value
   * @throws Exception if operation fails
   * @since AEF 10.7.1.0
   */

   public String getPropertyValue(Context context, String prefix,
            String environment, String type) throws Exception
   {
        String result = "";
        StringBuffer key = new StringBuffer();
        key.append(prefix).append(environment).append(".");
        try
        {
            String parentType = null;
            String symbolicTypeName = FrameworkUtil.getAliasForAdmin(context,
                    "type", type, true);

            try
            {
                result = EnoviaResourceBundle.getProperty(context,key.toString()
                        + symbolicTypeName);
            }
            catch (Exception e)
            {
                // no property found
                BusinessType busType = new BusinessType(type, context.getVault());
                StringList parentTypes = parentTypes = busType.getParents(context);
                while (parentTypes.size() > 0)
                {
                    Iterator itr = parentTypes.iterator();
                    while (itr.hasNext())
                    {
                        if (result != null && result.length() > 0)
                        {
                            break;
                        }
                        parentType = (String) itr.next();
                        symbolicTypeName = FrameworkUtil.getAliasForAdmin(context,
                                "type", parentType, true);
                        try
                        {
                            result = EnoviaResourceBundle.getProperty(context,key.toString()
                                    + symbolicTypeName);
                        } catch (Exception e1)
                        {
                            // keep trying
                        }
                    }
                    if (result != null && result.length() > 0)
                    {
                        break;
                    }
                    busType = new BusinessType(parentType, context.getVault());
                    parentTypes = busType.getParents(context);
                }
                if (result.length() == 0)
                {
                    // make one last attempt by looking at the environment
                    key.deleteCharAt(key.length()-1);
                    try
                    {
                        result = EnoviaResourceBundle.getProperty(context,key.toString());
                    } catch (Exception e1)
                    {
                    }
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        if (result.length() == 0)
        {
            log("No property found for prefix <<" + prefix + ">> for env <<" + environment + ">> with type <<" + type + ">>");
        }
        return result;
    }
   
    /**
     * This method returns all type names (symbolic names) that are 
     * configured in emxSystem.properties file for 3DLive/3DVIA. 
     * 
     * @param context the eMatrix <code>Context</code> object
     * @param args the array should contain launguage.
     * @return String Containing Configured types
     * @throws Exception
     * @since R210
     */
    public String getConfiguredTypes(Context context, String[] args)
    throws Exception
    {
        HashMap programMap    = (HashMap)JPO.unpackArgs(args);
        String language       = (String)programMap.get("launguage");
        // call getMetaData for caching
        MetaInfo metadata     = getMetaData(context,language);
        return nodeMap.keySet().toString();
    }
   
}
