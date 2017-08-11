// Copyright (c) 1993-2016 Dassault Systemes.
// All Rights Reserved
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.VaultUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.apps.framework.ui.UISearch;
import com.matrixone.apps.framework.ui.UISearchUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.input.SAXBuilder;
import com.matrixone.json.JSONArray;
import com.matrixone.json.JSONObject;
import com.matrixone.search.index.Config;
import com.matrixone.apps.domain.util.PropertyUtil;
/**
 * @author ixk
 *
 * The <code>${CLASSNAME}</code> class/interface contains ...
 *
 * @version AEF 11.0.0.0 - Copyright (c) 2005, MatrixOne, Inc.
 */
public class emxAEFUtilBase_mxJPO {
    //  General Constants used in this file
    private static final String DYNAMIC = "Dynamic";
    private static final String UTF8 = "UTF-8";
    private static final String NULL = "null";
    private static final String PIPE_SEPARATOR = "|";
    private static final String WORD_LAST = "last";

    //Full Text Search Constants
    private static final String PARAM_LIBRARIES = "LIBRARIES";
    private static final String PARAM_TYPES = "TYPES";
    private static final String EQUALS = "EQUALS";
    private static final String GREATER = "GREATER";
    private static final String LESS = "LESS";

    private static final String USER_NAME = "<User Name>";
    private static final String LAST_NAME = "<Last Name>";
    private static final String FIRST_NAME = "<First Name>";

	private static final String SETTING_RELATIONSHIP = "Relationship";
	private static final String SETTING_DIRECTION = "Direction";
	private static final String SETTING_USE_SUPER_USER = "Use Super User";
	private static final String SETTING_DISCONNECT_PREV = "Disconnect Previous";

    protected static final String PARAM_FREEZE_PANE = "freezePane";

    //Added for FullText Search Dynamic Filter Columns
    protected static final String PARAM_FILTER_COLUMN_POSITION = "filterColumnPosition";

    protected static final String CONTROL_HAS_FILTER_COLUMNS   = "hasFilterColumns";

    protected static final String BASIC_FILTER_COLUMNS         = "basicFilterColumns";

    protected static final String EXPRESSION_FILTER_COLUMNS    = "expressionFilterColumns";
    /**
     * Whats up?
     * @param context
     * @param args
     * @throws Exception
     */
    public emxAEFUtilBase_mxJPO(Context context, String[] args)throws Exception {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

    /**
     * @param context
     * @param args
     * @return boolean
     * @throws Exception
     */
    public static boolean accessEditActions(Context context, String[] args)
    throws Exception
    {
        boolean bFieldAccess = true;
        HashMap requestMap          = (HashMap) JPO.unpackArgs(args);
        String strEditRel           = (String) requestMap.get("editRelationship");
        String strConnectionProgram = (String) requestMap.get("connectionProgram");
        String strSelectedProgram   = (String) requestMap.get("selectedProgram");
        if(strConnectionProgram != null && !"".equals(strConnectionProgram)){
            bFieldAccess = true;
        }
        else if(strSelectedProgram != null && !"".equals(strSelectedProgram)){
            bFieldAccess = true;
        }else{
            if(strEditRel != null && !"".equals(strEditRel)){
                bFieldAccess = true;
            }else{
                bFieldAccess = false;
            }
        }
        return bFieldAccess;
    }

    /**
     * @param context
     * @param args
     * @return boolean
     * @throws Exception
     */
    public static boolean accessEditAdd(Context context, String[] args)
    throws Exception
    {
        boolean access = true;
        HashMap requestMap      = (HashMap) JPO.unpackArgs(args);
        String newRow           = (String) requestMap.get("insertNewRow");
        String applyURL         = (String) requestMap.get("applyURL");
        String uiType           = (String) requestMap.get("uiType");
        return ("structureBrowser".equalsIgnoreCase(uiType) && ("true".equalsIgnoreCase(newRow) || ( !"false".equalsIgnoreCase(newRow) && applyURL != null && !"".equals(applyURL))));
    }

	public static boolean hasAccessPageURL(Context context, String[] args)  throws Exception
    {
    	String sShowPAGEURL = EnoviaResourceBundle.getProperty(context, "emxFramework.ShowPageURL");
    	if(sShowPAGEURL.equals("true")){
    		return true;
    	}else{
			return false;
		}
    }

    /**
     * @param context
     * @param args
     * @return boolean
     * @throws Exception
     */
    public static boolean accessEditRemove(Context context, String[] args)
    throws Exception
    {
        boolean access = true;
        HashMap requestMap      = (HashMap) JPO.unpackArgs(args);
        String newRow           = (String) requestMap.get("insertNewRow");
        String applyURL         = (String) requestMap.get("applyURL");
        String lookupJPO        = (String) requestMap.get("lookupJPO");
        String uiType           = (String) requestMap.get("uiType");
        return ("structureBrowser".equalsIgnoreCase(uiType) && ((newRow != null && "true".equalsIgnoreCase(newRow)) ||
                lookupJPO != null && !"".equals(lookupJPO) || (applyURL != null && !"".equals(applyURL))));
    }

    /**
     * @param context
     * @param args
     * @return boolean
     * @throws Exception
     */
    public static boolean accessEditSeparator(Context context, String[] args)
    throws Exception
    {
        boolean access = true;
        HashMap requestMap      = (HashMap) JPO.unpackArgs(args);
        String newRow           = (String) requestMap.get("insertNewRow");
        String applyURL         = (String) requestMap.get("applyURL");
        String lookupJPO        = (String) requestMap.get("lookupJPO");
        String uiType           = (String) requestMap.get("uiType");
        return ("structureBrowser".equalsIgnoreCase(uiType) && ((newRow != null && "true".equalsIgnoreCase(newRow)) ||
                lookupJPO != null && !"".equals(lookupJPO) || (applyURL != null && !"".equals(applyURL))));
    }

    /**
     * @param context
     * @param args
     * @return boolean
     * @throws Exception
     */
    public static boolean accessEditExisting(Context context, String[] args)
    throws Exception
    {
        boolean access = true;
        HashMap requestMap      = (HashMap) JPO.unpackArgs(args);
        //String addJPO           = (String) requestMap.get("addJPO");
        //String applyURL         = (String) requestMap.get("applyURL");
        String lookupJPO        = (String) requestMap.get("lookupJPO");
        String uiType           = (String) requestMap.get("uiType");
        return ("structureBrowser".equalsIgnoreCase(uiType) && lookupJPO != null && !"".equals(lookupJPO));
    }

    /**
     * @param context
     * @param args
     * @return vector containing image details for Retained Search Image Column
     * @throws Exception
     */
    public Vector getRetainedImage (Context context,String[] args)throws Exception
    {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList)programMap.get("objectList");
        Vector vec = new Vector(relBusObjPageList.size());

        try{
            for (int i=0; i < relBusObjPageList.size(); i++) {
            Map hashMap = new HashMap();
            hashMap  = (Map)relBusObjPageList.get(i);
            String retained = (String)hashMap.get("retained");
            String image ="";
            if("true".equalsIgnoreCase(retained))
                image = "<a>&#160;<img height=\"16\" src=\"../common/images/iconRetainedSearch.gif\" align=\"middle\" border=\"0\"/></a>";
            else
                image ="<h1></h1>";
             vec.addElement(image);
            }
        } catch (Exception e) {
            throw new Exception(e.toString());
        }

    return vec;
   }

   /**
 * @param context
 * @param args
 * @return boolean
 * @throws Exception
 */
public boolean isConsolidatedSearch(Context context,String[] args)throws Exception
   {
        HashMap requestMap = (HashMap) JPO.unpackArgs(args);
        //  Get the parameter values from "requestMap" - if required
        String strSearchType = (String) requestMap.get("searchType");

        if(!"Consolidated".equalsIgnoreCase(strSearchType)){
            return false;
        }else{
            return true;
        }
   }

    /**
     * @param context
     * @param args
     * @return boolean
     * @throws Exception
     */
    public boolean accessSyncCommands(Context context,String[] args) throws Exception{
        HashMap requestMap = (HashMap) JPO.unpackArgs(args);
        String reportType = (String)requestMap.get("reportType");
        if("Complete_Summary_Report".equals(reportType)){
            return true;
        }else{
            return false;
        }
    }
//Added for BUG:356012
public boolean checkIsRootNodeSelected(String [] memberIds)throws Exception
{
    StringList strList = new StringList();
    String rowId = "";
    if(memberIds != null && memberIds.length > 0){
       for(int i = 0; i < memberIds.length ; i++){
        if(memberIds[i].indexOf("|") != -1){
             strList = FrameworkUtil.split(memberIds[i], "|");
             if (strList.size() == 3){
                 rowId = (String)strList.get(2);
             }else{
                 rowId = (String)strList.get(3);
             }
             if("0".equalsIgnoreCase(rowId))
                 return true;
          }
        }
     }
     return false;
}

    /**
 * @param context
 * @param args
 * @return Map
 * @throws Exception
 */

public Map deleteSelectedObjects(Context context,String[] args) throws Exception
{

    HashMap requestMap       = (HashMap)JPO.unpackArgs(args);
    HashMap requestValuesMap = (HashMap)requestMap.get("RequestValuesMap");
    String [] memberIds      = (String[])requestValuesMap.get("emxTableRowId");
    String objectId          = (String)requestMap.get("objectId");
    String rootId            = objectId;//(String)requestMap.get("rootObjectId");
    String language          = (String)requestMap.get("language");
    String fromRMB           = (String)requestMap.get("fromRMB");
    String emxTableRowIdRMB  = (String)requestMap.get("emxTableRowIdRMB");
    String rootError         = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale().getLanguage(), "emxFramework.GenericDelete.RootError");
    String uiType            = (String)requestMap.get("uiType");
    boolean sbroot           = false;
    String sbrootError       = "";
    String errorxml          = "";
    boolean errorOccured     = false;
    Map returnMap            = new HashMap();
    StringList objectIds     = new StringList();
    boolean rootSelected = false;
    if(fromRMB != null && "true".equalsIgnoreCase(fromRMB))
    {
        objectIds.add(emxTableRowIdRMB);
    }else
    {
        objectIds  = getObjectIds(context,memberIds);
    }
    //Added for BUG:356012
    String [] selectedIds = new String[objectIds.size()] ;
    if("true".equalsIgnoreCase(fromRMB))
        selectedIds = memberIds;
    else
        selectedIds = memberIds;
    if("StructureBrowser".equalsIgnoreCase(uiType)){
	        rootSelected = (boolean)checkIsRootNodeSelected(selectedIds);
    }
    //End for BUG:356012
    Set uniqueSet  = new HashSet(objectIds);
    StringList failedList    = new StringList();
    StringList erroredList   = new StringList();

    if(uniqueSet != null && !uniqueSet.isEmpty())
    {
        if(rootId != null) {
            sbroot = uniqueSet.contains(rootId);
            if(sbroot && uiType != null && "StructureBrowser".equalsIgnoreCase(uiType) && rootSelected) {
                uniqueSet.remove(rootId);
                sbrootError = rootId +"|"+rootError;
                failedList.add(rootId);
                erroredList.add(sbrootError);
                errorOccured = true;
            }
        }
   }

   if(uniqueSet != null && !uniqueSet.isEmpty())
   {
      java.util.Iterator iterator = uniqueSet.iterator();
      MqlUtil.mqlCommand(context, "notice " +"<mxRoot>");
      while(iterator.hasNext())
      {
         String oid   = (String) iterator.next();
         String error = "";
         DomainObject domain = new DomainObject(oid);
         if(domain.exists(context))
         {
             error  = deleteObject(context,oid);
         }
         if(error != null && !"".equals(error))
         {
             errorOccured = true;
             error = oid+"|"+error;
             failedList.add(oid);
             erroredList.add(error);
         }
      }
      MqlUtil.mqlCommand(context, "notice " +"</mxRoot>");
   }

   Set failedSet = new HashSet(failedList);
   uniqueSet.removeAll(failedSet);

   if(uiType != null && "StructureBrowser".equalsIgnoreCase(uiType))
   {
      TreeMap indexedList        = (TreeMap)requestMap.get("IndexedObjectList");
      StringList deletedLevelIds = getdeletedLevelId(context,uniqueSet,indexedList);

      returnMap.put("responseXML",getResponseXML(context,deletedLevelIds));
   }
   else if(uiType != null && "table".equalsIgnoreCase(uiType))
   {
      MapList objectList = (MapList)requestMap.get("ObjectList");
      objectList         = filteredObjectList(context,objectList,uniqueSet);

      returnMap.put("ObjectList",objectList);
   }

   returnMap.put("errorOccured",Boolean.valueOf(errorOccured));
   returnMap.put("erroredList",erroredList);

   return returnMap;
}

/**
 * @param context
 * @param objectId
 * @return Map
 * @throws Exception
 */

public String deleteObject(Context context,String objectId) throws Exception
{
    String error = "";
    try
    {
        ContextUtil.startTransaction(context, true);
        MqlUtil.mqlCommand(context, "notice " +"<object id=\""+ objectId + "\"><![CDATA[");
        if ( null != objectId )
        {
        	try{
        		MqlUtil.mqlCommand(context, "set context user $1 password $2",context.getUser(),context.getPassword());
        	    MqlUtil.mqlCommand(context, "delete bus $1",objectId);
        	}catch(Exception ex){
        		MqlUtil.mqlCommand(context, "notice " +"]]><status deleted=\"false\"/></object>");
        		throw new FrameworkException(ex.getMessage());
        	}
        }
        MqlUtil.mqlCommand(context, "notice " +"]]><status deleted=\"true\"/></object>");
        ContextUtil.commitTransaction(context);
    }
    catch (Exception e)
    {
        ContextUtil.abortTransaction(context);
        error = e.getMessage();

        if(error.lastIndexOf(":") != -1) {
            int lastIndex = error.lastIndexOf(":");
                     error = error.substring(lastIndex+1);
        }
        if(error.indexOf("to") != -1) {
            int lastIndex = error.indexOf("to");
                     error = error.substring(0,lastIndex);
        }
        if("No delete access".contains(error.trim())) {
        	error = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.GenericDelete.NoDeleteAccess", context.getLocale());
        }
    }

    return error;
}


/**
 * @param context
 * @param deletedLevelIds
 * @return Map
 * @throws Exception
 */

public String getResponseXML(Context context,StringList deletedLevelIds) throws Exception
{
    StringBuffer itemList = new StringBuffer();

    itemList.append("<mxRoot>");
    itemList.append("<action>remove</action>");

    if(deletedLevelIds != null && !deletedLevelIds.isEmpty())
    {
        for(int i = 0 ; i < deletedLevelIds.size() ; i++)
        {
            String levelId  = (String)deletedLevelIds.get(i);
            String item     = "<item id='"+levelId+"'/>";
            itemList.append(item);
        }
    }
    itemList.append("</mxRoot>");
    return itemList.toString();
}


/**
 * @param context
 * @param objectList
 * @param passedSet
 * @return Map
 * @throws Exception
 */

public MapList filteredObjectList(Context context,MapList objectList,Set passedSet) throws Exception
{
    MapList returnMapList = new MapList(objectList);
    MapList passedMapList = new MapList();

    Iterator passIterator = passedSet.iterator();

    while(passIterator.hasNext())
    {
       String passId = (String)passIterator.next();
       Iterator objectListIterator = objectList.iterator();

       while(objectListIterator.hasNext())
       {
          Map objectInfo = (Map)objectListIterator.next();
          String oid     = (String)objectInfo.get("id");
          if(passId.equalsIgnoreCase(oid))
          {
             passedMapList.add(objectInfo);
          }
       }

    }
    returnMapList.removeAll(passedMapList);
    return returnMapList;
}

/**
 * @param context
 * @param erroredList
 * @return Map
 * @throws Exception
 */

public String createAndSaveErrorXML(Context context,StringList erroredList) throws Exception
{
    StringBuffer xmlbuff = new StringBuffer();
    xmlbuff.append("<root>");

    if(erroredList != null && !erroredList.isEmpty())
    {
        for(int i = 0 ; i < erroredList.size() ; i++)
        {
            String message = (String) erroredList.get(i);
            StringList strList = FrameworkUtil.split(message,"|");

            if(strList != null && !strList.isEmpty())
            {
                String id    = (String)strList.get(0);
                String error = (String)strList.get(1);
                String row   = "<error id='"+id+"'>"+error+"</error>";
                xmlbuff.append(row);
            }
        }
    }
    xmlbuff.append("</root>");
    String encodedXML = FrameworkUtil.encodeURL(xmlbuff.toString(),"UTF-8");
    String xmlName    = ".emx"+System.currentTimeMillis();
    UISearch.saveSearch(context,xmlName,encodedXML);

    return xmlName;
}

/**
 * @param context
 * @param memberIds
 * @return Map
 * @throws Exception
 */

public StringList getObjectIds(Context context,String [] memberIds) throws Exception
{
    StringList strList = new StringList();
    StringList objectIdList = new StringList();
    String oid = "";

    if(memberIds != null && memberIds.length > 0)
    {
       for(int i = 0; i < memberIds.length ; i++)
       {
          if(memberIds[i].indexOf("|") != -1)
          {
             strList = FrameworkUtil.split(memberIds[i], "|");
             if (strList.size() == 3)
             {
                 oid = (String)strList.get(0);
             }else
             {
                 oid = (String)strList.get(1);
             }
          }else
          {
             oid = memberIds[i];
          }
           objectIdList.add(oid);
        }
     }

    return objectIdList;
}

/**
 * @param context
 * @param deletedSet
 * @param indexedMap
 * @return Map
 * @throws Exception
 */

public StringList getdeletedLevelId(Context context,Set deletedSet,TreeMap indexedMap) throws Exception
{
    StringList deletedLevelIds = new StringList();
    Iterator deletedIterator = deletedSet.iterator();

    while(deletedIterator.hasNext())
    {
        String deletedId     = (String)deletedIterator.next();
        Set indexkeySet      = indexedMap.keySet();
        Iterator keyIterator = indexkeySet.iterator();

        while(keyIterator.hasNext())
        {
            String key     = (String)keyIterator.next();
            Map objectInfo = (Map)indexedMap.get(key);
            String oid     = (String)objectInfo.get("id");

            if(deletedId.equalsIgnoreCase(oid))
            {
                deletedLevelIds.add(key);
            }
        }

    }

    return deletedLevelIds;
}


/**
 * @param context
 * @param args
 * @return Map
 * @throws Exception
 */

@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getErrorObjectIds(Context context,String[] args)throws Exception
{

    HashMap requestMap  = (HashMap)JPO.unpackArgs(args);
    String errorxml     = (String) requestMap.get("errorxml");
    MapList reportList  =  new MapList();

    String searchData   = UISearch.getSearchData(context,errorxml);
    String xml          = FrameworkUtil.decodeURL(searchData, "UTF-8");

    SAXBuilder saxb     = new SAXBuilder();
    saxb.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    saxb.setFeature("http://xml.org/sax/features/external-general-entities", false);
    saxb.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

    Document document   = saxb.build(new StringReader(xml));

    Element root        = document.getRootElement();
    List children       = root.getChildren();

    Iterator itr = children.iterator();
    while (itr.hasNext())
    {
       Element child = (Element) itr.next();
       String id     = child.getAttributeValue("id");
       String error  = child.getText();

       Map errorMap = new HashMap();
       errorMap.put("id",id);
       errorMap.put("error",error);
       reportList.add(errorMap);
    }
    UISearch.deleteSearch(context, errorxml);
    return reportList;
}


/**
 * @param context
 * @param args
 * @return Vector
 * @throws Exception
 */

public Vector getErrorMessage(Context context,String[] args) throws Exception
{
    HashMap programMap        = (HashMap) JPO.unpackArgs(args);
    MapList relBusObjPageList = (MapList)programMap.get("objectList");
    Vector errorVec           = new Vector(relBusObjPageList.size());

    for (int i=0; i < relBusObjPageList.size(); i++) {
         Map collMap = (Map)relBusObjPageList.get(i);
         String error  = (String)collMap.get("error");
         errorVec.addElement(error);
    }
    return errorVec;
}

/**
 * @param context
 * @param args
 * @return HashMap
 * @throws Exception
 */

public HashMap getGenericDelete(Context context,String [] args) throws Exception
{
    HashMap deleteCmd   = new HashMap();
    HashMap inputMap    = (HashMap) JPO.unpackArgs(args);
    HashMap paramMap    = (HashMap) inputMap.get("paramMap");
    HashMap commandMap  = (HashMap) inputMap.get("commandMap");
    HashMap settingsMap = (HashMap) commandMap.get("settings");
    HashMap requestMap  = (HashMap) inputMap.get("requestMap");
    String sbrootId     = (String)  requestMap.get("objectId");
    String uiType       = (String)  requestMap.get("uiType");
    String objectId     = (String)  paramMap.get("objectId");
    String href         = (String)  commandMap.get("href");

    if(href.indexOf("?") != -1) {
        href += "&emxTableRowIdRMB=" + objectId + "&objectId=" + objectId + "&rootObjectId=" + sbrootId  + "&uiType=" + uiType +"&fromRMB=true";
    }else {
        href += "?emxTableRowIdRMB=" + objectId + "&objectId=" + objectId + "&rootObjectId=" + sbrootId + "&uiType=" + uiType +"&fromRMB=true";
    }

    commandMap.put("href",href);
    settingsMap.remove("Dynamic Command Function");
    settingsMap.remove("Dynamic Command Program");
    settingsMap.remove("Row Select");

    MapList mapContent = new MapList();
    mapContent.add(commandMap);

    deleteCmd.put("Children",mapContent);
    return deleteCmd;
}

    /**
     * @param context
     * @param args
     * @return boolean
     * @throws Exception
     */
    public static boolean getFullSearchAccess(Context context, String[] args)
    throws Exception
    {
        HashMap requestMap          = (HashMap) JPO.unpackArgs(args);
        UISearchUtil searchUtil = new UISearchUtil();
        return searchUtil.isAutonomySearch(context,requestMap);
    }

    /**
     * @param context
     * @param args
     * @return boolean
     * @throws Exception
     */
    public static String getWelcomeMessage(Context context, String[] args) throws Exception{
		DomainObject personObject = PersonUtil.getPersonObject(context, context.getUser());
		StringList objectSelects= new StringList();
		objectSelects.addElement(DomainConstants.SELECT_ATTRIBUTE_FIRSTNAME);
		objectSelects.addElement(DomainConstants.SELECT_ATTRIBUTE_LASTNAME);
		Map personInfo = personObject.getInfo(context, objectSelects);
		String firstName = (String)personInfo.get(DomainConstants.SELECT_ATTRIBUTE_FIRSTNAME);
		String lastName = (String)personInfo.get(DomainConstants.SELECT_ATTRIBUTE_LASTNAME);

		String strPersonName = PersonUtil.getFullName(context,context.getUser());

		if(UIUtil.isNullOrEmpty(firstName) || UIUtil.isNullOrEmpty(lastName)){
			return strPersonName;
		}

    	 String strFullNameFormat = EnoviaResourceBundle.getProperty(context,"emxFramework.FullName.WelcomeFormat");
         if (strFullNameFormat != null)
         {
             String strPattern = FrameworkUtil.findAndReplace(strFullNameFormat, USER_NAME, "{0}");
             strPattern = FrameworkUtil.findAndReplace(strPattern, FIRST_NAME, "{1}");
             strPattern = FrameworkUtil.findAndReplace(strPattern, LAST_NAME, "{2}");
             Object arrKeyValues [] = new Object [] {strPersonName,firstName,lastName};
             strPersonName = MessageFormat.format(strPattern, arrKeyValues);
         }
         return strPersonName;
    }

/**
 * @param context
 * @param args
 * @return HashMap
 * @throws Exception
 */

    public HashMap getFullTextFilterColumns(Context context,String [] args) throws Exception
    {
        HashMap retMap = new HashMap();
        try
        {

            HashMap inputMap    = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap    = (HashMap) inputMap.get("requestMap");
            MapList tablecolumns = (MapList) inputMap.get("columnMap");
            HashMap controlMap    = (HashMap) inputMap.get("controlMap");
            String ftsFilters     = (String)requestMap.get("ftsFilters");
            String dynColPos   = (String)requestMap.get("filterColumnPosition");
            String TagsView = (String) requestMap.get("TagsView");
            Config _config     = Config.getInstance(context);
            MapList dynColumns = new MapList();
            if (UIUtil.isNotNullAndNotEmpty(ftsFilters))
            {
                HashSet expressionList = UISearchUtil.getCurrentTableExpressionList(tablecolumns);
                JSONObject jsonFilters = null;
                try{
                    jsonFilters = new JSONObject(com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(ftsFilters));
                }catch(Exception ex){
                    jsonFilters = new JSONObject(ftsFilters);
                }
                JSONArray jsonValues   = null;
                String fieldName       = null;
                Iterator filterKeyIter = jsonFilters.keys();
                StringList dynFields   = new StringList();
                boolean containsLibrary= false;
                String strKey = "";
                StringList slTaxonomy = FrameworkProperties.getTokenizedProperty(context,"emxFramework.FullTextSearch.Libraries", ",");
                Boolean canAddLibraryAttr = (Boolean)requestMap.get("canAddLibraryAttr");
                if(canAddLibraryAttr.booleanValue())
                {
                    Iterator itr = jsonFilters.keys();
                    boolean hasLibrary = false;
                    // ISS - 1469
                    while (itr.hasNext()) {
                        String key = (String) itr.next();
                        if (slTaxonomy.contains(key)) {
                            hasLibrary = true;
                            strKey = key;
                        }
                    }
                    if(hasLibrary)
                    {
                        JSONArray jsonLibArray = jsonFilters.getJSONArray(strKey);
                        StringList libAttrs    = null;
                        if(jsonLibArray.length() > 0 )
                        {
                            String libJsonValue = jsonLibArray.getString(0);
                            String value        = libJsonValue.substring(libJsonValue.indexOf(PIPE_SEPARATOR) + 1, libJsonValue.length());
                            libAttrs            = UISearchUtil.getFieldsForClassificationAttributes(context, value, true, true, true);
                            containsLibrary     = libAttrs != null && libAttrs.size() > 0;
                            if(containsLibrary) {
                                dynFields.addAll(libAttrs);
                            }
                        }
                    }
                }

                while (filterKeyIter.hasNext())
                {
                    fieldName  = (String) filterKeyIter.next();
                    jsonValues = jsonFilters.getJSONArray(fieldName);
                    boolean addExactMatch = "true".equalsIgnoreCase(FrameworkProperties.getProperty(context,"emxFramework.FullTextSearch.DynamicColumns.AddExactMatch"));
                    if(!PARAM_TYPES.equals(fieldName) && !strKey.equals(fieldName) && jsonValues.length() > 0) {
                        boolean create    = false;
                        String jsonValue  = jsonValues.getString(0);
                        String operator   = jsonValue.substring(0, jsonValue.indexOf(PIPE_SEPARATOR));
                        String value      = jsonValue.substring(jsonValue.indexOf(PIPE_SEPARATOR) + 1, jsonValue.length());
                        Config.Field field = _config.indexedBOField(fieldName);
                        String fieldSep = "";
                        if(field != null){
                            fieldSep = (String) field.attributes.get("fieldSeparator");
                            if(fieldSep != null && !"".equals(fieldSep) && !"true".equals(TagsView)){
                                boolean valueFound = false;
                                JSONObject jsonFilterValue = new JSONObject(value);
                                jsonValues = jsonFilterValue.getJSONArray(fieldName).getJSONArray(0);
                            }
                            String isHidden = (String) field.attributes.get("hidden");
                            if("true".equalsIgnoreCase(isHidden)){
                                continue;
                            }
                        }

                        if(EQUALS.equalsIgnoreCase(operator) && jsonValues.length() > 1){
                            create = true;
                        }else if(EQUALS.equalsIgnoreCase(operator) && field != null && !field.parametric && jsonValues.length() == 1 && (value.indexOf("*") != -1 || value.indexOf("?") != -1)){
                            create = true;
                        }else if((GREATER.equalsIgnoreCase(operator) || LESS.equalsIgnoreCase(operator)) && jsonValues.length() == 1){
                            create = true;
                        }else if(!EQUALS.equalsIgnoreCase(operator) && value != null && value.length() > 0){
                            create = true;
                        }else if(EQUALS.equalsIgnoreCase(operator) && jsonValues.length() == 1 && addExactMatch){
                            create = true;
                        }

                        if(EQUALS.equalsIgnoreCase(operator) && containsLibrary && dynFields.contains(fieldName)){
                            if((addExactMatch && jsonValues.length() == 0) ||
                                    (!addExactMatch && jsonValues.length() <= 1 )){
                                dynFields.remove(fieldName);
                                continue;
                            }
                        }

                        if(create && !dynFields.contains(fieldName)) {
                            if(!(fieldName.equals("LASTREVISION")||fieldName.equals("LATESTREVISION")) ){
                            dynFields.add(fieldName);
                            }
                        }
                    }
                }

                //If there are library classification attributes, add as dynamic columns
                if(dynFields.size() > 0)
                {
                    Iterator fieldItr = dynFields.iterator();
                    while(fieldItr.hasNext())
                    {
                        String attrFieldName = (String) fieldItr.next();
                        Config.Field field   = _config.indexedBOField(attrFieldName);

                        if(field != null && field.selectable != null) {

                        	if("name".equalsIgnoreCase(field.selectable)){
                        		String expr = "evaluate[IF(attribute[Title] != '') THEN attribute[Title] ELSE (IF(attribute[V_Name] != '') THEN attribute[V_Name] ELSE(name)) ]";
                        		if(UISearchUtil.IsColumnExists("BusinessObject",expr,expressionList)){
                        		continue;
                        		}
                        	}


                            if (!UISearchUtil.IsColumnExists("BusinessObject",field.selectable,expressionList) && !UISearchUtil.IsColumnExists("RelationShip",field.selectable,expressionList )
                                    && !UISearchUtil.IsColumnExists("BusinessObject",field.selectable + ".value",expressionList) && !UISearchUtil.IsColumnExists("RelationShip",field.selectable + ".value",expressionList )){
                               HashMap column = UISearchUtil.createColumn(context,controlMap,field.selectable,field.name);
                               dynColumns.add(column);
                           }
                        }
                    }
                }

                /*if(dynColumns.size() > 0) {
                   insertColumns(tablecolumns,dynColumns,requestMap,controlMap,dynColPos);
                }*/
            }

            retMap.put("columnMap", dynColumns);
            retMap.put("controlMap", controlMap);
            return retMap;

        }catch(Exception ex)
        {
            throw new FrameworkException(ex);
        }
    }

    public boolean isPageHistoryEnabled (Context context,String[] args) throws Exception
    {
    	boolean isPaginationEnabled = false;
    	try
    	{
    		String enablePageHistory = EnoviaResourceBundle.getProperty(context, "emxFramework.EnablePageHistory");
    		if( "true".equals(enablePageHistory))
    		{
    			isPaginationEnabled = true;
    		}

    	} catch(Exception ex)
    	{
    		// Do nothing as default value is set in definetion of variable.
    	}
    	return isPaginationEnabled;
    }

	public boolean canChangePassword (Context context,String[] args) throws Exception
    {
    	boolean canchgPwd = true;
    	try
    	{
    		String strcanchgPwd = EnoviaResourceBundle.getProperty(context, "emxFramework.External.Authentication");
    		if( "true".equalsIgnoreCase(strcanchgPwd))
    		{
    			canchgPwd = false;
    		}

    	} catch(Exception ex)
    	{
    		// Do nothing as default value is set in definetion of variable.
    	}
    	return canchgPwd;
    }

	/** To check the index server is enabled or not.
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public boolean isIndexSearch(Context context,String[] args) throws Exception
    {
		 HashMap inputMap    = (HashMap) JPO.unpackArgs(args);
    	return UISearchUtil.isAutonomySearch(context,inputMap);
    }

	public static Map getCollaborativeSpace(Context context, String[] args) throws Exception
	{
		//StringList projList = new StringList();
		String strOrg = PersonUtil.getDefaultOrganization(context, context.getUser());
		HashSet<String> hashSet = new HashSet<String>();
		StringList objProjList = PersonUtil.getProjects(context, context.getUser(), "");
		for(int i = 0; i < objProjList.size(); i++)
        {
            hashSet.add((String) objProjList.get(i));
        }
		objProjList.clear();
		objProjList.addAll(hashSet);

		UIMenu uiMenu = new UIMenu();
		Map projCmdMap = new HashMap();
		MapList objCmdMapList = new MapList();

		for(int i=0; i < objProjList.size(); i++)
		{

			Map cmdDetails = new HashMap();
			cmdDetails  = uiMenu.getCommand(context, "AEFSwitchCollabSpace");
			cmdDetails.put("label", objProjList.get(i));
			cmdDetails.put("name", objProjList.get(i));
			cmdDetails.put("href","emxSecurityContextCollaborativeProjectProcess.jsp?CollabSpace="+objProjList.get(i));
			objCmdMapList.add(cmdDetails);

		}
		projCmdMap.put("Children", objCmdMapList);
		return projCmdMap;
    }

	public static String getSecurityContextForThePersonLoggedIn(Context context, String[] args) throws Exception
	{
		String str = PersonUtil.getDefaultSecurityContext(context,context.getUser());
		if (str == null || "".equalsIgnoreCase(str))
		{
			str = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",context.getLocale().getLanguage(),"emxFramework.SecurityContextSelection.SetSecurityContextLabel");
		}
		return str;
    }
	public boolean hasSecurityContext(Context context,String[] args) throws Exception
    {
    	boolean hasSecurityContext = true;
    	try
    	{
    		hasSecurityContext = PersonUtil.hasSecurityContext(context, context.getUser());
    	} catch(Exception ex)
    	{
    		// Do nothing as default value is set in definetion of variable.
    	}
    	return hasSecurityContext;
    }

	 public boolean canDisplayCreateCommand(Context context , String[] args)throws Exception {
	         boolean value=true;
	         String loggedInRole = PersonUtil.getDefaultSecurityContext(context);
	         String roleOwner =   PropertyUtil.getSchemaProperty(context,"role_VPLMProjectAdministrator");
	         String roleAdmin =   PropertyUtil.getSchemaProperty(context,"role_VPLMAdmin");
	         if (loggedInRole.contains(roleOwner) || loggedInRole.contains(roleAdmin))  {
	                value=false;
	         }
	         return value;
	  }

	/**
	 * Method to connect two objects
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @throws Exception
	 *             if any operation fails. *
	 */
	public void formConnectObj(Context _context, String[] _args) throws Exception {
		DomainObject dObjFrom, dObjTo;
		HashMap programMap = (HashMap) JPO.unpackArgs(_args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		HashMap fieldMap = (HashMap) programMap.get("fieldMap");
		programMap.get("requestMap");
		HashMap settings = (HashMap) fieldMap.get("settings");

		String strNewValue = (String) paramMap.get("New Value");
		String strOldValue = (String) paramMap.get("Old value");

		if (strNewValue.equalsIgnoreCase(strOldValue)) {
			return;
		} else {
			String strRelationship = (String) settings.get(SETTING_RELATIONSHIP);
			if (strRelationship.startsWith("relationship_")) {
				strRelationship = PropertyUtil.getSchemaProperty(_context, strRelationship);
			}
			String strObjectId = (String) paramMap.get("objectId");

			String strDirection = (String) settings.get(SETTING_DIRECTION);

			// Disconnect Previous Default is FALSE
			boolean bDisconnectPrevious = true;
			boolean bPushContext = false;
			String strDisconnectPrevious = (String) settings.get(SETTING_DISCONNECT_PREV);
			String strPushContext = (String) settings.get(SETTING_USE_SUPER_USER);
			// Set to true if a DISCONNECTPREVIOUS setting is found in
			// parameters
			if ((strDisconnectPrevious != null) && !"".equals(strDisconnectPrevious)) {
				bDisconnectPrevious = Boolean.parseBoolean(strDisconnectPrevious);
			}

			// Set to true if a PUSHCONTEXT setting is found in parameters
			if ((strPushContext != null) && !"".equals(strPushContext)) {
				bPushContext = Boolean.parseBoolean(strPushContext);
			}

			if ((null != strObjectId) && !"".equals(strObjectId)) {
				// Create Domain Object and get the Type of the Object which is
				// getting created or edited
				DomainObject dObjInstance = DomainObject.newInstance(_context, strObjectId);
				dObjInstance.getInfo(_context, DomainConstants.SELECT_TYPE);

				// Disconnect
				if ((null != strOldValue) && !"".equals(strOldValue)) {
					// If DISCONNECTPREVIOUS setting is TRUE
					if (bDisconnectPrevious) {
						String strRelationshipId = null;

						// Modified to Disconnect Multiple Objects
						StringList slRelationshipIdList = new StringList();

						if (strDirection.equalsIgnoreCase("from")) {
							slRelationshipIdList = (StringList) dObjInstance.getInfoList(_context,
									"from[" + strRelationship + "].id");
						} else {

							slRelationshipIdList = (StringList) dObjInstance.getInfoList(_context,
									"to[" + strRelationship + "].id");

						}
						int size = slRelationshipIdList.size();
						if ((null != slRelationshipIdList) && !"".equals(slRelationshipIdList) && size != 0) {

							for (int i = 0; i < size; i++) {
								strRelationshipId = (String) slRelationshipIdList.get(i);
								DomainRelationship domRel = new DomainRelationship(strRelationshipId);

								try {
									// Checking for Push Context required for
									// connection .. New Parameter In Settings
									// "PUSHCONTEXT"
									if (bPushContext) {
										try {
											// Pushing context of "User Agent"
											// in order to disconnect objects.
											ContextUtil.pushContext(_context,
													PropertyUtil.getSchemaProperty(_context, "person_UserAgent"), "",
													"");
											domRel.remove(_context);
										} catch (Exception e) {
											throw new FrameworkException(e);
										} finally {
											ContextUtil.popContext(_context);

										}
									} else {
										ContextUtil.pushContext(_context);
										domRel.remove(_context);
										ContextUtil.popContext(_context);
									}
								} catch (Exception e) {
									MqlUtil.mqlCommand(_context,
											"notice 'There was an Exception in Disconnetion: " + e.getMessage() + "'");
									e.printStackTrace();
									throw e;
								}
							}
						}
					}
				}

				// Connect - If there is a new value Added
				if ((null != strNewValue) && !"".equals(strNewValue)) {
					String strNewObjID = (String) paramMap.get("New OID");

					// Modified code for connecting Multiple objects
					StringList slNewIdsList = (strNewObjID == null || (strNewObjID.length() == 0)
							|| "null".equals(strNewObjID)) ? new StringList()
									: strNewObjID.indexOf("|") != -1 ? FrameworkUtil.split(strNewObjID, "|")
											: FrameworkUtil.split(strNewObjID, ",");

					int size = slNewIdsList.size();
					if ((null != strNewObjID) && !"".equals(strNewObjID)) {
						for (int i = 0; i < size; i++) {
							strNewObjID = (String) slNewIdsList.get(i);
							// Create the Domain Object of New Object
							DomainObject dObjNewInstance = DomainObject.newInstance(_context, strNewObjID);
							// Check and assign FromType according to the
							// setting present on the Web Form Field
							if (strDirection.equalsIgnoreCase("from")) {
								dObjFrom = dObjInstance;
								dObjTo = dObjNewInstance;
							} else {
								dObjTo = dObjInstance;
								dObjFrom = dObjNewInstance;
							}
							try {
								// Checking for Push Context required for
								// connection .. New Parameter In Settings
								// "PUSHCONTEXT"
								if (bPushContext) {
									try {
										// Pushing context of "User Agent" in
										// order to connect objects.
										ContextUtil.pushContext(_context,
												PropertyUtil.getSchemaProperty(_context, "person_UserAgent"), "", "");
										DomainRelationship.connect(_context, dObjFrom, strRelationship, dObjTo);
									} catch (Exception e) {
										throw new FrameworkException(e);
									} finally {
										ContextUtil.popContext(_context);

									}
								} else {
									// Connection of the two Objects
									DomainRelationship.connect(_context, dObjFrom, strRelationship, dObjTo);
								}
							} catch (Exception e) {
								MqlUtil.mqlCommand(_context, "notice 'There was an Exception: " + e.getMessage() + "'");
								e.printStackTrace();
								throw e;
							}
						}

					}

				}
			}
		}
	}

	/**
	 * Method to connect two objects
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @throws Exception
	 *             if any operation fails. *
	 */
	public void sbConnectObjects(Context _context, String[] _args) throws Exception {
		DomainObject dObjFrom, dObjTo;
		HashMap programMap = (HashMap) JPO.unpackArgs(_args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		HashMap columnMap = (HashMap) programMap.get("columnMap");
		programMap.get("requestMap");
		HashMap settings = (HashMap) columnMap.get("settings");

		String strNewValue = (String) paramMap.get("New Value");

		String strRelationship = (String) settings.get(SETTING_RELATIONSHIP);
		if (strRelationship.startsWith("relationship_")) {
			strRelationship = PropertyUtil.getSchemaProperty(_context, strRelationship);
		}
		String strObjectId = (String) paramMap.get("objectId");

		String strDirection = (String) settings.get(SETTING_DIRECTION);

		// Disconnect Previous Default is FALSE
		boolean bDisconnectPrevious = true;
		boolean bPushContext = false;
		String strDisconnectPrevious = (String) settings.get(SETTING_DISCONNECT_PREV);
		String strPushContext = (String) settings.get(SETTING_USE_SUPER_USER);
		// Set to true if a DISCONNECTPREVIOUS setting is found in
		// parameters
		if ((strDisconnectPrevious != null) && !"".equals(strDisconnectPrevious)) {
			bDisconnectPrevious = Boolean.parseBoolean(strDisconnectPrevious);
		}

		// Set to true if a PUSHCONTEXT setting is found in parameters
		if ((strPushContext != null) && !"".equals(strPushContext)) {
			bPushContext = Boolean.parseBoolean(strPushContext);
		}

		if ((null != strObjectId) && !"".equals(strObjectId)) {
			// Create Domain Object and get the Type of the Object which is
			// getting created or edited
			DomainObject dObjInstance = DomainObject.newInstance(_context, strObjectId);
			dObjInstance.getInfo(_context, DomainConstants.SELECT_TYPE);

			// Disconnect
			// If DISCONNECTPREVIOUS setting is TRUE
			if (bDisconnectPrevious) {
				String strRelationshipId = null;

				// Modified to Disconnect Multiple Objects
				StringList slRelationshipIdList = new StringList();

				if (strDirection.equalsIgnoreCase("from")) {
					slRelationshipIdList = (StringList) dObjInstance.getInfoList(_context,
							"from[" + strRelationship + "].id");
				} else {

					slRelationshipIdList = (StringList) dObjInstance.getInfoList(_context,
							"to[" + strRelationship + "].id");

				}
				int size = slRelationshipIdList.size();
				if ((null != slRelationshipIdList) && !"".equals(slRelationshipIdList) && size != 0) {

					for (int i = 0; i < size; i++) {
						strRelationshipId = (String) slRelationshipIdList.get(i);
						DomainRelationship domRel = new DomainRelationship(strRelationshipId);

						try {
							// Checking for Push Context required for
							// connection .. New Parameter In Settings
							// "PUSHCONTEXT"
							if (bPushContext) {
								try {
									// Pushing context of "User Agent"
									// in order to disconnect objects.
									ContextUtil.pushContext(_context,
											PropertyUtil.getSchemaProperty(_context, "person_UserAgent"), "", "");
									domRel.remove(_context);
								} catch (Exception e) {
									throw new FrameworkException(e);
								} finally {
									ContextUtil.popContext(_context);

								}
							} else {
								ContextUtil.pushContext(_context);
								domRel.remove(_context);
								ContextUtil.popContext(_context);
							}
						} catch (Exception e) {
							MqlUtil.mqlCommand(_context,
									"notice 'There was an Exception in Disconnetion: " + e.getMessage() + "'");
							e.printStackTrace();
							throw e;
						}
					}
				}
			}

			// Connect - If there is a new value Added
			if ((null != strNewValue) && !"".equals(strNewValue)) {

				// Modified code for connecting Multiple objects
				StringList slNewIdsList = (strNewValue == null || (strNewValue.length() == 0)
						|| "null".equals(strNewValue)) ? new StringList()
								: strNewValue.indexOf("|") != -1 ? FrameworkUtil.split(strNewValue, "|")
										: FrameworkUtil.split(strNewValue, ",");

				int size = slNewIdsList.size();
				if ((null != strNewValue) && !"".equals(strNewValue)) {
					for (int i = 0; i < size; i++) {
						strNewValue = (String) slNewIdsList.get(i);
						// Create the Domain Object of New Object
						DomainObject dObjNewInstance = DomainObject.newInstance(_context, strNewValue);
						// Check and assign FromType according to the
						// setting present on the Web Form Field
						if (strDirection.equalsIgnoreCase("from")) {
							dObjFrom = dObjInstance;
							dObjTo = dObjNewInstance;
						} else {
							dObjTo = dObjInstance;
							dObjFrom = dObjNewInstance;
						}
						try {
							// Checking for Push Context required for
							// connection .. New Parameter In Settings
							// "PUSHCONTEXT"
							if (bPushContext) {
								try {
									// Pushing context of "User Agent" in
									// order to connect objects.
									ContextUtil.pushContext(_context,
											PropertyUtil.getSchemaProperty(_context, "person_UserAgent"), "", "");
									DomainRelationship.connect(_context, dObjFrom, strRelationship, dObjTo);
								} catch (Exception e) {
									throw new FrameworkException(e);
								} finally {
									ContextUtil.popContext(_context);

								}
							} else {
								// Connection of the two Objects
								DomainRelationship.connect(_context, dObjFrom, strRelationship, dObjTo);
							}
						} catch (Exception e) {
							MqlUtil.mqlCommand(_context, "notice 'There was an Exception: " + e.getMessage() + "'");
							e.printStackTrace();
							throw e;
						}
					}

				}

			}
		}
	}

	public static boolean checkVaultBL(Context context, String args[]) {
		return !(VaultUtil.isMultiValutEnabled());
	}
}
