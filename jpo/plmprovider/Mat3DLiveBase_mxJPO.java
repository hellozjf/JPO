package jpo.plmprovider;
// Mat3DLiveBase.java
//
// Created on Oct 3, 2006
//
// Copyright (c) 2005-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MatrixLogWriter;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UITable;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.jsystem.util.MxLinkedHashMap;
import com.matrixone.apps.plmprovider.PlmProviderUtil;
import com.matrixone.apps.domain.util.MapList;

/**
 * @author bucknam
 *
 * The <code>Mat3DLiveBase</code> class acts as a basis for other classes
 * in this package.
 *
 * @version AEF 10.7.1.0 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class Mat3DLiveBase_mxJPO {

    // need this param for UITable to translate column headers
    protected static final String PARAM_LANGUAGE = "languageStr";

    // ripped from UITable
    protected static final String SETTING_SORT_TYPE = "Sort Type";

    // table column setting, true to display field in easy query
    protected static final String SETTING_EASY_QUERY = "3DLive Easy Query";
    protected static final String SETTING_HIDDEN_ATTRIBUTE = "3DLive Hidden Attribute";
    protected static final String SETTING_BASIC_ATTRIBUTE = "3DLive Basic Attribute";
    protected static final String SETTING_COLUMN_TYPE = "Column Type";

    // hard-coded CGM file paths
    // as it is more confusing to the adminsitrator to define paths for every image types
    // generally CGM files are always located in this path, there is no need to keep them in emxSystem
    protected static final String cgmFilePath   = "$<from[relationship_ActiveVersion].to.id>," +
                                                  "$<from[relationship_ActiveVersion].to.from[relationship_DerivedOutput].to.id>," +
                                                  "$<from[relationship_ActiveVersion].to.from[relationship_AssociatedDrawing].to.id>," +
                                                  "$<from[relationship_ActiveVersion].to.from[relationship_AssociatedDrawing].to.from[relationship_DerivedOutput].to.id>," +
                                                  "$<from[relationship_AssociatedDrawing].to.id>," +
                                                  "$<from[relationship_AssociatedDrawing].to.from[relationship_DerivedOutput].to.id>," +
                                                  "$<id>," +
                                                  "$<from[relationship_DerivedOutput].to.id>," + 
												  "$<from[relationship_PartSpecification].to.from[relationship_DerivedOutput].to[type_DerivedOutput].id>";
    protected static final String cgmFileFormat = "format_CGM";

    protected Context _ctx;

    // trace flag
    protected static boolean trace = false;

    // hash map of type nodes
    protected static Map nodeMap = Collections.synchronizedMap(new MxLinkedHashMap(128));

    
    @SuppressWarnings({ "rawtypes" })
    protected static Map MetaDataCache =  Collections.synchronizedMap(new MxLinkedHashMap(128));
    
    // hash map of type names keyed by hash name
    protected static Map hashNameToTypeNameMap = Collections.synchronizedMap(new HashMap());

    // hash map of hash names keyed by type name
    protected static Map typeNameToHashNameMap = Collections.synchronizedMap(new HashMap());

    // hash map of attribute names keyed by hash name
    protected static Map hashNameToAttributeNameMap = Collections.synchronizedMap(new HashMap());

    // hash map of hash names keyed by attribute name
    protected static Map attributeNameToHashNameMap = Collections.synchronizedMap(new HashMap());

    // hash map of attribute names keyed by table select
    protected static Map selectToAttributeNameMap = Collections.synchronizedMap(new HashMap());

//  hash map of table select keyed by attribute names
    protected static Map attributeNameToSelectMap = Collections.synchronizedMap(new HashMap());

    // hash map of attribute types keyed by attribute name
    protected static Map attributeNameToTypeMap = Collections.synchronizedMap(new HashMap());

    // logwriter to redirect the logs to '3DLive.log' file
    protected static MatrixLogWriter logWriter  = null;

    private static final String SELECT_TYPE             = "type";

    public static final String REL_VPM_REP_INSTANCE     = PropertyUtil.getSchemaProperty("relationship_VPMRepInstance");
    public static final String TYPE_VPM_REP_REFERENCE   = PropertyUtil.getSchemaProperty("type_VPMRepReference");
    public static final String TYPE_VPM_REFERENCE       = PropertyUtil.getSchemaProperty("type_VPMReference");
    public static final String ATTR_VPM_VUSAGE          = PropertyUtil.getSchemaProperty("attribute_PLMEntity.V_usage");
    public static final String APPLICATION_VPLM         = "VPLM";

    // The following variables are used for forming the dummy relationship ID
    static int firstPart    = 1;
    static int SecondPart   = 1;
    static int thirdPart    = 1;
    static int fourthPart   = 1;


    protected void log(String s) {
        if (isTrace()) {
            try {
                logWriter.write("[" + new Date() + "] PLM: "+s);
            }catch(Exception ex) {
                ex.printStackTrace(System.out);
            }
        }
    }

    protected Mat3DLiveBase_mxJPO() {
        log("---------------");
    }

    protected String initContext(String username, String password) throws Exception {
        return initContext(username, password, null);
    }

    
    protected String initContext(String username, String password, String securityContext) throws Exception {

        log("initContext user<<" + username + ">> pass<<" + password + ">>");
        String key = null;
        try {         
            _ctx = PlmProviderUtil.createContext(username, password, securityContext);   
            key = "SUCCESS:" + _ctx.getCustomData(PlmProviderUtil.PROPERTY_3DLIVE_ENCRYPTED_KEY);
            log("initContext OK" + key);
        } catch (MatrixException e) {
            key = "FAILURE:" + e.toString();
            log("initContext: ERROR:" + e);
        }

        log("initContext: returning: "+ key);
        return key;
    }

    protected Context getContext()
    {
        return (_ctx);
    }

    /**
     * Returns a context for a given user name and password.
     *
     * @param username the matrix user name
     * @param password the user password
     * @return context for a given user
     * @since R208
     */

    protected Context getContext(String username, String password)
    {
        Context context = null;
        try
        {
            initContext(username, password);
            context = getContext();
        }
        catch(Exception e)
        {
            log("getContext: Exception: " +e.getMessage());
        }
        return context;
    }

    /**
     * Returns a unique name used as the 3DLive ID.  3DLive requires this name be <= 40 bytes.
     * This method is for testing purpose only
     *
     * @param context the matrix context
     * @param args the array should contain the name
     * @return the hashed name
     * @throws Exception
     * @since R208
     */

    public String getHashName(Context context, String args[]) throws Exception
    {
        HashMap programMap          = (HashMap)JPO.unpackArgs(args);
        String name                 = (String)programMap.get("name");
        return getHashName(name);
    }

    /**
     * Returns a unique name used as the 3DLive ID.  3DLive requires this name be <= 40 bytes.
     * Until R206 we used the 32-byte hex-encoded MD5 hash.
     * but from R207, hashName generation logic is changed to accomodate
     * 3DXML export functionality. Using the older logic, 3DXML export was not usable
     * So it was requsted to chnage this to keep hashName as human readbale
     * In this: server just replaces space by _, because space is limitation on the client
     * @param name the given name
     * @return the hashed name
     * @throws NoSuchAlgorithmException
     * @since AEF 10.7.1.0
     */

    public static String getHashName(String name) throws NoSuchAlgorithmException
    {
        StringBuffer hashStringBuf = new StringBuffer(40);
        // if the name is longer than 40 charecters, truncate it to 40 - client limitation
        String hashString = name;
        hashString = name.replace(" ", "_");
        hashString = hashString.replace("@", "_");

        if(hashString.length() > 40)
        {
            hashString = hashString.substring(0, 40);
        }

        return (hashString);
    }

    /**
     * Check if the table column is empty, meaning that
     * it is missing object/relationship expression and
     * column Type equal to Program.
     *
     * @param column the table column information
     * @return true if the column is "empty"
     * @since AEF 10.7.1.0
     */
    protected static boolean isEmptyColumn(HashMap column)
    {
        String columnType   = UITable.getSetting(column,SETTING_COLUMN_TYPE);
        String expression   = null;
        boolean empty       = true;

        if(columnType != null && "program".equals(columnType))
        {
            expression  = UITable.getLabel(column);
            empty       = (expression == null || expression.length() == 0);
        }
        else
        {
            expression  = UITable.getBusinessObjectSelect(column);
            empty       = (expression == null || expression.length() == 0);

            if (empty)
            {
                expression  = UITable.getRelationshipSelect(column);
                empty       = (expression == null || expression.length() == 0);
            }
        }

        return (empty);
    }

   /**
     * Returns select statement for a given column
     *
     * It returns column label if a column has setting column type equal to
     * program, else it will return the select expression .
     *
     * @param context the eMatrix <code>Context</code> object
     * @param column the column map
     * @return column select
     * @since AEF 10.7.1.0
     */
    protected static String getColumnSelect(Context context, HashMap column)
        throws Exception
    {
       String expression    = null;
       boolean empty        = true;
       String columnType    = UITable.getSetting(column,SETTING_COLUMN_TYPE);

       if(columnType != null && "program".equals(columnType))
       {
           expression = UITable.getLabel(column);
           empty = (expression == null || expression.length() == 0);
       }
       else
       {
           expression = UITable.getBusinessObjectSelect(column);
           empty = (expression == null || expression.length() == 0);

           if (empty)
           {
               expression = UITable.getRelationshipSelect(column);
               empty = (expression == null || expression.length() == 0);
           }
       }

       if (empty)
       {
           expression = null;
       }

        return (PlmProviderUtil.substituteValues(context, expression));
    }

    /**
     *
     * @since AEF 10.7.1.0
     */
    protected static String getTypeFromHash(String hashName)
    {
        return ((String) hashNameToTypeNameMap.get(hashName));
    }

    /**
     *
     * @since AEF 10.7.1.0
     */
    protected static String getHashFromType(String typeName)
    {
        return ((String) typeNameToHashNameMap.get(typeName));
    }

    /**
     *
     * @since AEF 10.7.1.0
     */
    protected static String getAttributeFromHash(String hashName)
    {
        return ((String) hashNameToAttributeNameMap.get(hashName));
    }

    /**
     *
     * @since AEF 10.7.1.0
     */
    protected static String getHashFromAttribute(String attributeName)
    {
        return ((String) attributeNameToHashNameMap.get(attributeName));
    }

    /**
     * @deprecated
     * @since AEF 10.7.1.0
     */
    protected static String getTypeFromAttribute(String attributeName)
    {
        return ((String) attributeNameToTypeMap.get(attributeName));
    }

   /**
    * @deprecated
    * return the column label for a column select
    * @since R207
    */
   protected static String getAttributeFromSelect(String select)
   {
       return ((String) selectToAttributeNameMap.get(select));
    }

    /**
     * @deprecated
     * return the column select from attribute name
     * @since R207
     */
    protected static String getSelectFromAttribute(String attributeName)
    {
           return ((String) attributeNameToSelectMap.get(attributeName));
   }

   /**
     * @return trace flag value (true means display traces to console)
     */
    protected static boolean isTrace() {
        return trace;
    }

    /**
     * This method is used to turn traces on programmatically 
     * 
     * @param context - matrix context
     * @param args  - string array should have trace value (boolean)
     * @throws Exception
     */
    public static void setTrace(Context context, String[] args) 
    throws Exception
    {
        HashMap programMap       = (HashMap)JPO.unpackArgs(args);
        Boolean bTrace           = (Boolean)programMap.get("trace");
        setTrace(context, bTrace.booleanValue());
    }
    /**
     * @param trace the value for the trace flag
     */
    protected static void setTrace(Context context,boolean trace) {
        Mat3DLiveBase_mxJPO.trace = trace;
        if (trace) {
            setLogWriter(context);                 
        }
    }
    
    private static synchronized void setLogWriter(Context context){
    	if(logWriter == null){
            String strFileName        = "3DLive.log";
            String strLogtype         = "PLM";
            boolean bAllFlag          = true;
            logWriter                 = new MatrixLogWriter(context,strFileName,strLogtype,bAllFlag);
        }
    }

    /**
     * utility method that returns the related minor object for a major object
     * this is relavent for object types that are sub-types of DOCUMENTS
     * bcz, only these objects will have major/minor objects
     * it is used/called primiarliy during the Markup object creation
     * bcz, Markup are conneted to minor objects, even when user is attempting to create a Markup on the major object
     * @param context the eMatrix <code>Context</code> object
     * @param objectID - objectId for which we need to get minor object ID
     * @return minor object ID if exists, else the returns the same
     */
    protected DomainObject getMinorObject(Context context, String objectID) throws Exception {
        log("Mat3DLive.getMinorObject Start");
        DomainObject object = new DomainObject(objectID);
        String type_MCADModel = PropertyUtil.getSchemaProperty(context, "type_MCADModel");

        if (object.isKindOf(context, type_MCADModel))
        {
            StringList objectSelects = new StringList(1);
            String selectActiveVersion = "from[" +
            PropertyUtil.getSchemaProperty(context, "relationship_ActiveVersion")   + "].to.id";
            objectSelects.add(selectActiveVersion);
            Map dsMap = object.getInfo(context, objectSelects);

            String activeId = null;
              try{
                StringList activeIdList = (StringList)dsMap.get(selectActiveVersion);
                if(activeIdList != null)
                {
                  activeId = (String)activeIdList.get(0);
                }
              }
              catch(ClassCastException cexp )
              {
                activeId = (String)dsMap.get(selectActiveVersion);
              }
              if (activeId != null && activeId.length() > 0)
              {
                  object = new DomainObject(activeId);
                  log("Mat3DLive.getMinorObject Object was of Major object type");
              }
        }

        log("Mat3DLive.getMinorObject complete");
        return object;
    }

    /**
     * utility method to find the object that contains the object structure
     * this is custom code for DSC object type that maintain the parent/child structure of
     * assemblies under minor object when object is WIP state
     * In these cases, we need to navigate to the right object to get the object structure
     * it is used/called primiarliy during the object expand i.e. doExpand calls
     * @param context the eMatrix <code>Context</code> object
     * @param objectID - objectId for which we need to get object that has the structure
     * @return String objectID that corresponds to the object having structure
     */
    protected String getActiveObjectID(Context context, String objectID) throws Exception {
        log("Mat3DLive.getActiveID Start");

        DomainObject object = new DomainObject(objectID);
        String activeId = null;
        String type_MCADModel = PropertyUtil.getSchemaProperty(context, "type_MCADModel");
        String type_MCADDrawing = PropertyUtil.getSchemaProperty(context, "type_MCADDrawing");

        if (object.isKindOf(context, type_MCADModel) || object.isKindOf(context, type_MCADDrawing))
        {
            log("Mat3DLive.getActiveID Object is a DSC type");
            
            StringList inputIds = FrameworkUtil.split(objectID, ":");
            if(inputIds.size()>1 && UIUtil.isNotNullAndNotEmpty((String)inputIds.get(1))){
                String minorId = (String)inputIds.get(1);
                if(!minorId.equals("0.0.0.0")){
                	activeId = minorId;
                }else{
                	activeId = (String)inputIds.get(0);
                }
                
            }else{
                StringList objectSelects = new StringList(3);
                String selectFromCADSubComponent = "from[" +
                PropertyUtil.getSchemaProperty(context, "relationship_CADSubComponent") + "].to.id";
                String selectToCADSubComponent = "to[" +
                PropertyUtil.getSchemaProperty(context, "relationship_CADSubComponent") + "].from.id";
                String selectActiveVersion = "from[" +
                PropertyUtil.getSchemaProperty(context, "relationship_ActiveVersion")   + "].to.id";
                objectSelects.add(selectFromCADSubComponent);
                objectSelects.add(selectToCADSubComponent);
                objectSelects.add(selectActiveVersion);
                Map dsMap = object.getInfo(context, objectSelects);
                String childId = null;
    
                try
                {
                    StringList childIdList = (StringList)dsMap.get(selectFromCADSubComponent);
                    if(childIdList != null)
                    {
                       childId = (String)childIdList.get(0);
                    }
                }
                catch(ClassCastException cexp)
                {
                    childId = (String)dsMap.get(selectFromCADSubComponent);
                }
    
                if (childId == null || childId.length() == 0)
                {
                    // work in progress case, or leaf part
                    String parentId = (String)dsMap.get(selectToCADSubComponent);
    
                    if (parentId == null || parentId.length() == 0)
                    {
                       // not a leaf part
                      try{
                        StringList activeIdList = (StringList)dsMap.get(selectActiveVersion);
                        if(activeIdList != null)
                        {
                          activeId = (String)activeIdList.get(0);
                        }
                      }
                      catch(ClassCastException cexp )
                      {
                        activeId = (String)dsMap.get(selectActiveVersion);
                      }
                      if (activeId != null && activeId.length() > 0)
                      {
                          // work in progress case, side step to active version
                          log("Mat3DLive.getActiveID Object is in WIP state");
                      }
                    }
                }
            }
        }

        if(activeId == null)
        {
            activeId = objectID;
        }
        return activeId;
    }

    /**
     * This method gets Column details for a given table.
     * This method is for testing purpose only.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param strTableName the Table name
     * @param assignments the assignments
     * @param strLanguage the Language
     * @return MapList of Column columns
     * @since R208
     */

    public static MapList getTableColumns(Context context, String args[])
    throws Exception
    {
        HashMap programMap          = (HashMap)JPO.unpackArgs(args);
        String tableName            = (String)programMap.get("tableName");
        Vector assignments          = (Vector)programMap.get("assignments");
        String strLanguage          = (String)programMap.get("language");

        return getTableColumns(context, tableName, assignments, strLanguage);
    }

    /**
     * Returns Columns Map List
     *
     * This method gets Column details for a given table.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param strTableName the Table name
     * @param assignments the assignments
     * @param strLanguage the Language
     * @return MapList of Column columns
     * @since R208
     */

    public static MapList getTableColumns(Context context, String strTableName, Vector assignments, String strLanguage)
    throws Exception
    {
        // get user assignments
        if (assignments == null)
        {
            assignments = new Vector(1);
            assignments.add("all");
        }

        if (strLanguage == null || strLanguage.length() == 0)
        {
            strLanguage = "en";
        }


        UITable table   = new UITable();

        MapList columns = UITable.getColumns(context, strTableName, assignments);

        if (columns == null)
        {
            String[] formatArgs = {strTableName};
            String message =  ComponentsUIUtil.getI18NString(context, "emxComponents.Mat3DLive.UnableToFindTable",formatArgs);
            throw new Exception(message);
        }

        HashMap tableControlMap = new HashMap();
        HashMap requestMap      = new HashMap();
        requestMap.put(PARAM_LANGUAGE, strLanguage);

        return(table.processColumns(context, tableControlMap, columns, requestMap));

    }


    /**
     * Returns a whether a given Object is synchronized from VPLM or not
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectID the object ID
     * @return VPM Part or not
     * @since BPS R209
     */

     protected boolean isPartFromVPLMSync(Context context, String objectID) throws Exception
     {
         log("Mat3DLive.isPartFromVPLMSync Start");

         boolean isPartFromVPM      = false;
         try
         {
             ContextUtil.pushContext(context,null,null,null);
             String type_Part                   = PropertyUtil.getSchemaProperty(context, "type_Part");
             String SELECT_REL_PART_SPEC_TO_TYPE= "from[" + DomainConstants.RELATIONSHIP_PART_SPECIFICATION + "].to["+TYPE_VPM_REFERENCE+"].type";
             String strFromtype                 = null;
             String strToType                   = null;
             Map map                            = null;
             DomainObject object                = new DomainObject(objectID);
             StringList objectSelects           = new StringList(2);

             objectSelects.add(SELECT_TYPE);
             objectSelects.add(SELECT_REL_PART_SPEC_TO_TYPE);

             map                        = object.getInfo(context, objectSelects);
             
             String key_SELECT_REL_PART_SPEC_TO_TYPE = "";
             Iterator itr  =  map.keySet().iterator();
             while(itr.hasNext()){
                 String key = (String)itr.next();
                 if(key.startsWith("from["+DomainConstants.RELATIONSHIP_PART_SPECIFICATION + "].to[") && key.endsWith("].type")){
                     key_SELECT_REL_PART_SPEC_TO_TYPE = key;
                     break;
                 }
             }
             
             strFromtype                = (String)map.get(SELECT_TYPE);
             strToType                  = (String)map.get(key_SELECT_REL_PART_SPEC_TO_TYPE);

             if(strFromtype != null && PlmProviderUtil.isKindof(context, strFromtype, type_Part) &&
                strToType != null && PlmProviderUtil.isKindof(context, strToType, TYPE_VPM_REFERENCE))
             {
                 isPartFromVPM          = true;
             }

         }
         catch(Exception ex)
         {

             log("Exception in Mat3DLive.isPartFromVPLMSync " +ex.getMessage());
         }
	 finally
         {
             ContextUtil.popContext(context);
         }

         log("Mat3DLive.isPartFromVPLMSync end: is Part From VPM " +isPartFromVPM);

         return isPartFromVPM;
     }

     /**
      * Returns a dummy id
      *
      * @return dummy id
      * @since BPS R209
      */
     protected String getDummyID()
     {
        String relID = firstPart + "." +SecondPart + "." + thirdPart + "." +fourthPart;
        if (++fourthPart == 10000)
        {
            fourthPart = 1;
            if (++thirdPart == 10000)
            {
                thirdPart = 1;
                if (++SecondPart == 10000)
                {
                    SecondPart = 1;
                    if ( ++firstPart == 1000)
                    {
                        firstPart = 1;
                    }
                }
            }
        }

        return relID;
     }

}
