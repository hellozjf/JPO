/*
** emxRequirementSpecificationBase
**
** Copyright (c) 2008-2016 Dassault Systemes.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
**
** static const char RCSID[] = $Id: /ENORequirementsManagementBase/CNext/Modules/ENORequirementsManagementBase/JPOsrc/base/${CLASSNAME}.java 1.9.2.6.1.1 Wed Oct 29 22:20:01 2008 GMT przemek Experimental$
*
*/
/*
** # @quickreview T25 DJH 13:04:19 Method getSpecStructureRMBMenu() modified to display new contextual menu ( RMTSpecStructureSubRequirementRMB ) for Sub and derived Req in Spec.
** # @quickreview T25 DJH 13:05:13 Correction IR IR-234500V6R2014. Method getSpecStructureRMBMenu() modified to display new contextual menu ( RMTSpecStructureFolderRMB ) for Specification Folder.
** # @quickreview HAT1 ZUD 2014:5:28  IR-272999V6R2015.STP: In Requirement Specification, adding Project Folder from Properties category is KO.
** # @quickreview QYG      2015:08:28 IR-390635-3DEXPERIENCER2016x: handle dynamic RMB menu items
*  # @quickreview KIE1 ZUD 2015:10:12 IR-395963-3DEXPERIENCER2017x: R418-STP: Unused commands are present in 'Contextual Menu' of Requirement Specification overview.
*  # @quickreview KIE1 ZUD 2015:11:23 IR-395977-3DEXPERIENCER2017x: R418-FUN053188:Expand All command is KO, while Collapse All working properly. On Requirement Overview. 
*  # @quickreview KIE1 ZUD 2017:06:31 IR-510602-3DEXPERIENCER2017x: Requirement Specifications - ALL Option does not work and gives the error
*/

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.dassault_systemes.requirements.ReqConstants;
import com.dassault_systemes.requirements.ReqSchemaUtil;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.WorkspaceVault;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIToolbar;
import com.matrixone.apps.requirements.RequirementsCommon;
import com.matrixone.apps.requirements.RequirementsUtil;


/**
* This JPO class has some methods pertaining to Requirement Specification type.
*
* @author
* @version RequirementCentral V6R2009-1
*/
public class emxRequirementSpecificationBase_mxJPO extends emxDomainObject_mxJPO
{
    protected static final String SYMB_OBJECT_ID          = "objectId";

    // The operator names
    protected static final String OP_INCLUDES             = "Includes";
    protected static final String OP_IS_EXACTLY           = "IsExactly";
    protected static final String OP_IS_NOT               = "IsNot";
    protected static final String OP_IS_MATCHES           = "Matches";
    protected static final String OP_BEGINS_WITH          = "BeginsWith";
    protected static final String OP_ENDS_WITH            = "EndsWith";
    protected static final String OP_EQUALS               = "Equals";
    protected static final String OP_DOES_NOT_EQUAL       = "DoesNotEqual";
    protected static final String OP_IS_BETWEEN           = "IsBetween";
    protected static final String OP_IS_ATMOST            = "IsAtMost";
    protected static final String OP_IS_ATLEAST           = "IsAtLeast";
    protected static final String OP_IS_MORE_THAN         = "IsMoreThan";
    protected static final String OP_IS_LESS_THAN         = "IsLessThan";
    protected static final String OP_IS_ON                = "IsOn";
    protected static final String OP_IS_ON_OR_BEFORE      = "IsOnOrBefore";
    protected static final String OP_IS_ON_OR_AFTER       = "IsOnOrAfter";

    // The operator symbols
    protected static final String SYMB_AND                = " && ";
    protected static final String SYMB_OR                 = " || ";
    protected static final String SYMB_EQUAL              = " == ";
    protected static final String SYMB_NOT_EQUAL          = " != ";
    protected static final String SYMB_GREATER_THAN       = " > ";
    protected static final String SYMB_LESS_THAN          = " < ";
    protected static final String SYMB_GREATER_THAN_EQUAL = " >= ";
    protected static final String SYMB_LESS_THAN_EQUAL    = " <= ";
    protected static final String SYMB_MATCH              = " ~~ ";
    protected static final String SYMB_QUOTE              = "'";
    protected static final String SYMB_WILD               = "*";
    protected static final String SYMB_OPEN_PARAN         = "(";
    protected static final String SYMB_CLOSE_PARAN        = ")";
    protected static final String SYMB_ATTRIBUTE          = "attribute";
    protected static final String SYMB_OPEN_BRACKET       = "[";
    protected static final String SYMB_CLOSE_BRACKET      = "]";
    protected static final String SYMB_TO                 = "to";
    protected static final String SYMB_FROM               = "from";
    protected static final String SYMB_DOT                = ".";
    protected static final String SYMB_NULL               = "null";
    protected static final String SYMB_COMMA              = ",";
    protected static final String COMBO_PREFIX            = "cd_";
    protected static final String TXT_PREFIX              = "txt_";
    protected static final String OBJECT_IDS              = "objectIDs";
    protected static final String ALL                     = "All";
    protected static final String DEFAULT                 = "Default";
    protected static final String ADD_REMOVE              = "Add Remove";

    /**
     * Create a new emxRequirementSpecificationBase object.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @return a emxRequirementBase object.
     * @throws Exception
     *             if the operation fails
     * @since RequirementCentral X2
     * @grade 0
     */
    public emxRequirementSpecificationBase_mxJPO(Context context, String[] args) throws Exception
    {
        super(context, args);
    }

    /**
     * Main entry point.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception
     *             if the operation fails
     * @since RequirementCentral X2
     * @grade 0
     */
    public int mxMain(Context context, String[] args) throws Exception
    {
        if (!context.isConnected())
        {
            String language = context.getSession().getLanguage();
            String strContentLabel = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Alert.FeaturesCheckFailed"); 
            throw new Exception(strContentLabel);
        }
        return 0;
    }


	/**
	* Method call to get all the Requirement Specifications in the data base.
	*
	* @param context the eMatrix <code>Context</code> object
	* @param args  Not used
	* @return Object - MapList containing the id of all Requirement Specifications objects
	* @throws Exception if the operation fails
	* @since RequirementsManagement V6R2009x
	* @grade 0
	*/
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllRequirementSpecifications (Context context, String[] args)
        throws Exception
    {
        MapList mapBusIds = getRequirementSpecs(context, null);
        return(mapBusIds);
    }

	/**
	* Get the list of all Requirement Specifications owned by the current user.
	*
	* @param context the eMatrix <code>Context</code> object
	* @param args  not used
	* @return MapList - MapList containing the id of all owned Requirement Specifications objects.
	* @throws Exception if the operation fails
	* @since RequirementsManagement V6R2009x
	* @grade 0
	*/
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getOwnedRequirementSpecifications (Context context, String[] args)
        throws Exception
    {
        // forming the Owner Pattern clause
        String strOwnerCondition = context.getUser();
        //Calls the protected method to retrieve the data
        MapList mapBusIds = getRequirementSpecs(context, strOwnerCondition);
        return(mapBusIds);
    }

	/**
	* Get the list of Requirement Specifications.
	*
	* @param context the eMatrix <code>Context</code> object
	* @param strOwnerCondition - String value containing the owner condition based on which results are to be filtered.
	* @return MapList - MapList containing the id of Requirement Specifications objects based on owner string .
	* @throws Exception if the operation fails
	* @since RequirementsManagement V6R2009x
	* @grade 0
	*/
    protected MapList getRequirementSpecs (Context context, String strOwnerCondition)
        throws Exception
    {
        String strType = ReqSchemaUtil.getRequirementSpecificationType(context);
        StringList objectSelects = new StringList(SELECT_ID);
        objectSelects.addElement(ReqConstants.SELECT_READ_ACCESS);

        // KIE1 added for IR-510602-3DEXPERIENCER2017x
        int searchLimit = 0;
      		try {
      			String property = EnoviaResourceBundle.getProperty(context,"emxRequirements.RequirementsObject.SearchLimit");
      			if (property != null && property.trim().length() > 0) {
      				searchLimit = Integer.valueOf(property.trim());
      			}
      		} catch (Exception ex) {
      		}
        
        MapList mapBusIds = findObjects(context, strType, null,null,strOwnerCondition,DomainConstants.QUERY_WILDCARD,null,null,true, objectSelects,(short)searchLimit);
        
        String[] objectIds = new String[mapBusIds.size()];
        for(int i = 0; i < mapBusIds.size(); i++){
            objectIds[i] = (String)((Map<?, ?>)mapBusIds.get(i)).get(SELECT_ID);
        }

        MapList specList;
        objectSelects.addElement(SELECT_POLICY);

        ContextUtil.pushContext(context);
        try{
            specList = DomainObject.getInfo(context, objectIds, objectSelects);
        }finally{
            ContextUtil.popContext(context);
        }
        String sPolicy = ReqSchemaUtil.getVersionPolicy(context);
        
        for(int i = specList.size() - 1; i >= 0; i--){
            @SuppressWarnings("unchecked")
            Map<String, String> m = (Map<String, String>)specList.get(i);
            Map<?, ?> accessMap = (Map<?, ?>)mapBusIds.get(i);
            
            if(sPolicy.equals(m.get(SELECT_POLICY))){
                specList.remove(i);
            }else{
                m.put(ReqConstants.SELECT_READ_ACCESS, (String)accessMap.get(ReqConstants.SELECT_READ_ACCESS));
                
            }
        }
        return specList;
    }

    
    /**
     * 
     * Returns all owned "root" groups and all specification unattached to any groups
     * @param context matrixOne context
     * @param args	packed argument
     * @return	a MapList containing all relevant requirements groups and requirement specifications
     * @throws Exception if operation fails
     * @since RequirementsManagement V6R2009x
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getOwnedSpecsAndGroups(Context context, String[] args)
    throws Exception
    {
    	String strOwnerCondition = context.getUser();
    	return getSpecificationsAndGroups(context,strOwnerCondition );    
    }
    
    /**
     * Returns all "root" groups and all specification unattached to any groups (regardless of the owner)
     * @param context matrixOne context
     * @param args	packed argument
     * @return	a MapList containing all relevant requirements groups and requirement specifications
     * @throws Exception if operation fails
     * @since RequirementsManagement V6R2009x
     */    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllSpecsAndGroups(Context context, String[] args)
    throws Exception
    {

    	return getSpecificationsAndGroups(context,null);	
    }
    
    protected MapList getSpecificationsAndGroups(Context context, String strOwnerCondition)
    throws Exception
    {
    	
    	//getting specifications
        String strType = ReqSchemaUtil.getRequirementSpecificationType(context);
        StringList objectSelects = new StringList(SELECT_ID);
        objectSelects.addElement(ReqConstants.SELECT_READ_ACCESS);

        // KIE1 added for IR-510602-3DEXPERIENCER2017x
        int searchLimit = 0;
		try {
			String property = EnoviaResourceBundle.getProperty(context,"emxRequirements.RequirementsObject.SearchLimit");
			if (property != null && property.trim().length() > 0) {
				searchLimit = Integer.valueOf(property.trim());
			}
		} catch (Exception ex) {
		}
		
        MapList mapBusIds = findObjects(context, strType, null,null,strOwnerCondition,DomainConstants.QUERY_WILDCARD,null,null,true, objectSelects, (short)searchLimit);

        String[] objectIds = new String[mapBusIds.size()];
        for(int i = 0; i < mapBusIds.size(); i++){
            objectIds[i] = (String)((Map<?, ?>)mapBusIds.get(i)).get(SELECT_ID);
        }

        MapList specList;
        objectSelects.addElement(SELECT_POLICY);
        objectSelects.addElement("to[Requirement Group Content]");
        ContextUtil.pushContext(context);
        try{
            specList = DomainObject.getInfo(context, objectIds, objectSelects);
        }finally{
            ContextUtil.popContext(context);
        }
        String sPolicy = ReqSchemaUtil.getVersionPolicy(context);
        for(int i = specList.size() - 1; i >= 0; i--){
            @SuppressWarnings("unchecked")
            Map<String, String> m = (Map<String, String>)specList.get(i);
            Map<?, ?> accessMap = (Map<?, ?>)mapBusIds.get(i);
            
            if(sPolicy.equals(m.get(SELECT_POLICY)) || "TRUE".equalsIgnoreCase((String)m.get("to[Requirement Group Content]"))){
                specList.remove(i);
            }else{
                m.put(ReqConstants.SELECT_READ_ACCESS, (String)accessMap.get(ReqConstants.SELECT_READ_ACCESS));
                
            }
        }

        //getting groups
        objectSelects = new StringList(SELECT_ID);
        objectSelects.addElement(ReqConstants.SELECT_READ_ACCESS);
        MapList mapGroupsIds = findObjects(context, "Requirement Group", null,null,strOwnerCondition,DomainConstants.QUERY_WILDCARD,null,null,true, objectSelects, (short)searchLimit);
        objectIds = new String[mapGroupsIds.size()];
        for(int i = 0; i < mapGroupsIds.size(); i++){
            objectIds[i] = (String)((Map<?, ?>)mapGroupsIds.get(i)).get(SELECT_ID);
        }
        MapList groupList;
        
        objectSelects.addElement(SELECT_POLICY);
        objectSelects.addElement("to[Sub Requirement Group]");
        ContextUtil.pushContext(context);
        try{
            groupList = DomainObject.getInfo(context, objectIds, objectSelects);
        }finally{
            ContextUtil.popContext(context);
        }
        for(int i = groupList.size() - 1; i >= 0; i--){
            @SuppressWarnings("unchecked")
            Map<String, String> m = (Map<String, String>)groupList.get(i);
            Map<?, ?> accessMap = (Map<?, ?>)mapGroupsIds.get(i);
            
            if("TRUE".equalsIgnoreCase((String)m.get("to[Sub Requirement Group]"))){
                groupList.remove(i);
            }else{
                m.put(ReqConstants.SELECT_READ_ACCESS, (String)accessMap.get(ReqConstants.SELECT_READ_ACCESS));
            }
        }
        
        specList.addAll(groupList);
        
        return specList;
    }
    /**
     * Returns all the active requirement specifications owned by the context
     * user. By default, the active requirement specifications will be listed in
     * the table.
     *
     * @mx.whereUsed Invoked by the user from My Requirement Specifications List
     *               view
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args an array of String arguments for this method
     * @return maplist
     * @throws Exception
     *             if the operation fails
     * @since RequirementCentral X2
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public static MapList getActiveRequirementSpecifications(Context context, String args[]) throws Exception
    {
        return getRequirementSpecifications(context, "Active");
    }

    /**
     * Returns all the released requirement specifications owned by the context
     * user.
     *
     * @mx.whereUsed Invoked by the user from My Requirement Specifications List
     *               view
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args an array of String arguments for this method
     * @return maplist
     * @throws Exception
     *             if the operation fails
     * @since RequirementCentral X2
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public static MapList getReleasedRequirementSpecifications(Context context, String args[]) throws Exception
    {
        return getRequirementSpecifications(context, "Released");
    }

    /**
     * Returns all the obsolete requirement specifications owned by the context
     * user.
     *
     * @mx.whereUsed Invoked by the user from My Requirement Specifications List
     *               view
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args an array of String arguments for this method
     * @return maplist
     * @throws Exception
     *             if the operation fails
     * @since RequirementCentral X2
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public static MapList getObsoleteRequirementSpecifications(Context context, String args[]) throws Exception
    {
        return getRequirementSpecifications(context, "Obsolete");
    }

    /**
     * Returns the requirement specifications owned by the context user, and
     * matching the filter criteria
     *
     * @mx.whereUsed Invoked by the user from My Requirement Specifications List
     *               view
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param filter
     *            filter value chosen by the user
     * @return maplist
     * @throws Exception
     *             if the operation fails
     * @since RequirementCentral X2
     * @grade 0
     */
    public static MapList getRequirementSpecifications(Context context, String filter) throws Exception
    {
        // get the context user
        String user = context.getUser();
        StringList objectSelects = new StringList();
        String type = ReqSchemaUtil.getRequirementSpecificationType(context);
        String sPolicy = ReqSchemaUtil.getVersionPolicy(context);
        MapList resultList = new MapList();
        MapList finalresultList = new MapList();
        StringBuffer sbattrib = new StringBuffer();
        StringBuffer whereExp = new StringBuffer();
        int resultListSize = 0;
        
        // KIE1 added for IR-510602-3DEXPERIENCER2017x
        int searchLimit = 0;
		try {
			String property = EnoviaResourceBundle.getProperty(context,"emxRequirements.RequirementsObject.SearchLimit");
			if (property != null && property.trim().length() > 0) {
				searchLimit = Integer.valueOf(property.trim());
			}
		} catch (Exception ex) {
		}
		
        Map objectMap = new HashMap();

        String objectId = "";
        String latestRevId = "";

        objectSelects.add(SELECT_ID);
        objectSelects.add(SELECT_NAME);
        objectSelects.add(SELECT_OWNER);
        objectSelects.add(SELECT_CURRENT);

        sbattrib.append(SYMB_ATTRIBUTE);
        sbattrib.append(SYMB_OPEN_BRACKET);
        sbattrib.append(ReqSchemaUtil.getTitleAttribute(context));
        sbattrib.append(SYMB_CLOSE_BRACKET);

        objectSelects.add(sbattrib.toString());
        objectSelects.add(SELECT_FILE_NAME);

        // If the context user is the owner of the objects.
        whereExp.append("owner=='" + user + "'");
        whereExp.append(SYMB_AND);
        whereExp.append("(policy!=" + sPolicy + ") ");
        whereExp.append(SYMB_AND);

        //forming the where clause for the filters based on the selection

        //If active filter is selected do not show the requirement specifications
        //which are in Release or Obsolete state
        if (filter.equalsIgnoreCase("Active"))
        {
            whereExp.append("((");
            whereExp.append("current!= Release");
            whereExp.append(") && (");
            whereExp.append("current!= Obsolete))");
        }
        else if (filter.equalsIgnoreCase("Released"))
        {
            whereExp.append("current==Release");
        }
        else if (filter.equalsIgnoreCase("Obsolete"))
        {
            whereExp.append("current==Obsolete");
        }

        try
        {
            //find all the objects of type requirement specification satisfying the where clause pattern
            resultList = DomainObject.findObjects(context, type, QUERY_WILDCARD,
                    QUERY_WILDCARD, null, QUERY_WILDCARD, whereExp.toString(), null,
                    true, objectSelects, (short) searchLimit, null, null);

            if (resultList != null)
            {

                resultListSize = resultList.size();
            }

            //iterate through the object list and get the last revision values.
            for (int i = 0; i < resultListSize; i++)
            {
                objectMap = (Map) resultList.get(i);
                objectId = (String) objectMap.get(SELECT_ID);

                DomainObject dom = new DomainObject(objectId);
                matrix.db.BusinessObject bo = RequirementsCommon.getLastRevision(context, dom);

                latestRevId = bo.getObjectId(context);
                if (objectId.equals(latestRevId))
                {
                    finalresultList.add(objectMap);
                }
            }
        } //end of try
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        return finalresultList;
    }

    /**
     * getNameOrTitle - Returns the Title of the requirement specifications for Requirement Specification Summary
     * Table.
     *
     * @mx.whereUsed Invoked by the user from My Requirement Specifications List
     *               view.It will be called in the Title Column Requirement Specification Summary
     *               Table
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: 0 - objectList MapList
     * @return vector
     * @throws Exception
     *             if the operation fails
     * @since RequirementCentral X2
     * @grade 0
     */
    public static Vector getNameOrTitle(Context context, String args[]) throws Exception
    {
        Vector showTitle = new Vector();

        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map objectMap = null;
            int objectListSize = 0;
            String name = "";
            String title = "";
            int fileTitleList = 0;

            if (objectList != null)
                objectListSize = objectList.size();

            //iterate through the object list and set the title values based on the cases.
            for (int i = 0; i < objectListSize; i++)
            {
                objectMap = (Map) objectList.get(i);
                name = (String) objectMap.get(SELECT_NAME);
                title = (String) objectMap.get("attribute[" + ReqSchemaUtil.getTitleAttribute(context) + "]");

                StringList fileTitle = (StringList) objectMap.get("format.file.name");
                StringBuffer files = new StringBuffer(50);

                if (title != null && !title.equals("") && !"null".equals(title))
                {
                    // For the requirement specification object the Title value entered by the user.
                    showTitle.add(title);
                }
                else if (!"".equals(fileTitle.get(0)))
                {
                    // If user had not entered title value take the title value
                    // from the file name which is checked-in.
                    fileTitleList = fileTitle.size();
                    for (int count = 0; count < fileTitleList; count++)
                    {
                        files.append((String) fileTitle.get(count));
                        if (count < (fileTitleList - 1))
                        {
                            files.append(" , ");
                        }
                    }

                    showTitle.add(files.toString());
                }
                else
                {
                    // Display the name of the object in title field.
                    showTitle.add(name);
                }
            } //end of iteration
        } //end of try
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        return showTitle;
    }

    /**
     * getNameOrTitleValue - Returns the Title of the requirement specification for Requirement Specification web form
     * 
     *
     * @mx.whereUsed Invoked by the user from Requirement Specification properties
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: 0 - objectId 
     * @return StringList
     * @throws Exception
     *             if the operation fails
     * @since RequirementCentral X5
     * @grade 0
     */
    public static Object getNameOrTitleValue(Context context, String args[]) throws Exception
    {
        StringList fieldValues = new StringList();

        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            HashMap paramMap = (HashMap) programMap.get("paramMap");

            String strMode = (String) requestMap.get("mode");
            String strObjectId = (String) paramMap.get(SYMB_OBJECT_ID);
            
            DomainObject domObj = DomainObject.newInstance(context, strObjectId);
            String title = domObj.getInfo(context, "attribute[" + ReqSchemaUtil.getTitleAttribute(context) + "]");
            StringList fileTitle = domObj.getInfoList(context, SELECT_FILE_NAME); 

            if (title != null && !title.equals("") && !"null".equals(title))
            {
                // For the requirement specification object the Title value entered by the user.
            	fieldValues.add(title);
            }
            else if ("edit".equalsIgnoreCase(strMode))
            {
            	fieldValues.add("");
            }
            else if (!"".equals(fileTitle.get(0)))
            {
                // If user had not entered title value take the title value
                // from the file name which is checked-in.
                int fileTitleList = 0;
                fileTitleList = fileTitle.size();
                for (int count = 0; count < fileTitleList; count++)
                {
                	fieldValues.add((String) fileTitle.get(count));
                }

            }
            else
            {
                String name = domObj.getName();
                // Display the name of the object in title field.
            	fieldValues.add(name);
            }
        } //end of try
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        return fieldValues;
    }    
    /**
     * updateTitle - update the Title of the requirement specification for Requirement Specification web form
     *
     * @mx.whereUsed Invoked by the user from Requirement Specification properties
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments: 0 - objectId 
     * @returns void 
     * @throws Exception if the operation fails
     * @since RequirementCentral X5
     * @grade 0
     */
    public static void updateTitle(Context context, String args[]) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String newValue = (String)paramMap.get("New Value");

        String strObjectId = (String) paramMap.get(SYMB_OBJECT_ID);

        DomainObject domObj = DomainObject.newInstance(context, strObjectId);
        domObj.setAttributeValue(context, ReqSchemaUtil.getTitleAttribute(context), newValue); 
    }    
    
    /**
     * Returns the active projects list of which the context user is the member, and matching the other
     * criteria
     *
     * @mx.whereUsed Invoked by the user from Add to Project folder command or edit or create pages of requirement specification.
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    show           - a String of specified criteria show
     *    page           - a String of specified criteria page
     *    Name           - a String of specified criteria name
     *    Description    - a String of specified criteria description
     *    Owner          - a String of specified criteria owner
     *    Originator     - a String of specified criteria originator
     *    BusinessUnit   - a String of specified criteria businessUnit
     *    vaultType      - a String of specified criteria vault
     *    companyName    - a String of specified criteria companyName
     *    companyId      - a String of specified criteria companyId
     *    ProgramName    - a String of specified criteria programName
     *    ProgramId      - a String of specified criteria ProgramId
     *    QueryLimit     - a String of limit on the number of objects found
     *    txtKeyword     - a String containing the Advanced Findlike parameters
     *    txtFormat      - a String containing the Advanced Findlike parameters
     *    comboFormat    - a String containing the Advanced Findlike parameters
     * @return MapList containing projects objects for search result
     * @throws Exception if the operation fails
     * @since RequirementCentral X2
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public static MapList getActiveProjectFolders(Context context, String[] args) throws Exception
    {
        MapList projectList = new MapList();
        String fromPage = "";

        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);

            String sDisplayProjects = "";
            boolean start = true;

            //get the value of show key if it contains "active" value display only active projects of which user is a member
            sDisplayProjects = (String) programMap.get("show");

            if (sDisplayProjects == null || sDisplayProjects.trim().equals(""))
                sDisplayProjects = "";

            String projectType = ReqSchemaUtil.getProjectSpaceType(context);
            String relMember = ReqSchemaUtil.getMemberRelationship(context);

            // object selects
            StringList selectStmts = new StringList(3);
            selectStmts.addElement(SELECT_ID);
            selectStmts.addElement(SELECT_NAME);
            StringBuffer whereExp = new StringBuffer();

            //forming the where clause to display only active projects which are not in complete or archieve state
            whereExp.append("((");
            whereExp.append("current!= Complete");
            whereExp.append(") && (");
            whereExp.append("current!= Archieve))");
            start = false;

            //if show key contains "active" value display only active projects
            if (sDisplayProjects.equalsIgnoreCase("Active"))
            {
                Person sPerson = Person.getPerson(context);
                String sPersonId = sPerson.getId();

                DomainObject domObjPerson = new DomainObject(sPersonId.trim());
                projectList = domObjPerson.getRelatedObjects(context, relMember, projectType, selectStmts, null, true,
                        false, (short) 0, whereExp.toString(), null);
            }

            //start of all projects search criteria
            else
            {
                //get the value of page key if it contains "basic" value display all the projects.
                //User can refine the projects on the basis of basic search criteria
                fromPage = (String) programMap.get("page");

                if (fromPage.equalsIgnoreCase("basic"))
                {
                    String sNameSelected = (String) programMap.get("Name");
                    String sdescription = (String) programMap.get("Description");
                    String sOwnerSelected = (String) programMap.get("Owner");
                    String originator = (String) programMap.get("Originator");
                    String businessUnit = (String) programMap.get("BusinessUnit");
                    String strVaultOption = (String) programMap.get("vaultType");
                    String companyId = (String) programMap.get("companyId");
                    String programId = (String) programMap.get("ProgramId");

                    String strVault = "";

                    if ("".equals(programId) || QUERY_WILDCARD.equals(programId))
                        programId = "";

                    String relBusinessUnitProject = ReqSchemaUtil.getBusinessUnitProjectRelationship(context);

                    //Assign user selection of a value from vault field
                    if (PersonUtil.SEARCH_DEFAULT_VAULT.equalsIgnoreCase(strVaultOption)
                        || PersonUtil.SEARCH_LOCAL_VAULTS.equalsIgnoreCase(strVaultOption)
                        || PersonUtil.SEARCH_ALL_VAULTS.equalsIgnoreCase(strVaultOption))
                    {
                        strVault = PersonUtil.getSearchVaults(context, false, strVaultOption);
                    }
                    else
                    {
                        strVault = (String) programMap.get("selectedVaults");
                    }

                    //forming the where clause pattern for the description field
                    if (sdescription != null && (!sdescription.equals(QUERY_WILDCARD))
                        && (!sdescription.equals("")) && !("null".equalsIgnoreCase(sdescription)))
                    {
                        if (start)
                        {
                            whereExp.append(SYMB_OPEN_PARAN);
                            start = false;
                        }
                        else
                        {
                            whereExp.append(SYMB_AND);
                        }

                        whereExp.append(SYMB_OPEN_PARAN);
                        whereExp.append(SELECT_DESCRIPTION);
                        whereExp.append(SYMB_MATCH);
                        whereExp.append("const ");
                        whereExp.append(SYMB_QUOTE);
                        whereExp.append(sdescription);
                        whereExp.append(SYMB_QUOTE);
                        whereExp.append(SYMB_CLOSE_PARAN);
                    }

                    //forming the where clause pattern for the originator field
                    if (originator != null && (!originator.equals(QUERY_WILDCARD))
                        && (!originator.equals("")) && !("null".equalsIgnoreCase(originator)))
                    {
                        if (start)
                        {
                            whereExp.append(SYMB_OPEN_PARAN);
                            start = false;
                        }
                        else
                        {
                            whereExp.append(SYMB_AND);
                        }

                        whereExp.append(SYMB_OPEN_PARAN);
                        whereExp.append(SYMB_ATTRIBUTE);
                        whereExp.append(SYMB_OPEN_BRACKET);
                        whereExp.append("Originator");
                        whereExp.append(SYMB_CLOSE_BRACKET);
                        whereExp.append("~=");
                        whereExp.append("const ");
                        whereExp.append(SYMB_QUOTE);
                        whereExp.append(originator);
                        whereExp.append(SYMB_QUOTE);
                        whereExp.append(SYMB_CLOSE_PARAN);
                    }

                    //forming the where clause pattern for the businessUnit field
                    if (businessUnit != null && (!businessUnit.equals(QUERY_WILDCARD))
                        && (!businessUnit.equals("")) && !("null".equalsIgnoreCase(businessUnit)))
                    {
                        if (start)
                        {
                            whereExp.append(SYMB_OPEN_PARAN);
                            start = false;
                        }
                        else
                        {
                            whereExp.append(SYMB_AND);
                        }

                        whereExp.append(SYMB_OPEN_PARAN);
                        whereExp.append(SYMB_TO);
                        whereExp.append(SYMB_OPEN_BRACKET);
                        whereExp.append(relBusinessUnitProject);
                        whereExp.append(SYMB_CLOSE_BRACKET);
                        whereExp.append(SYMB_DOT);
                        whereExp.append(SYMB_FROM);
                        whereExp.append(SYMB_DOT);
                        whereExp.append("name ");
                        whereExp.append("~=");
                        whereExp.append(SYMB_QUOTE);
                        whereExp.append(businessUnit);
                        whereExp.append(SYMB_QUOTE);
                        whereExp.append(SYMB_CLOSE_PARAN);
                    }

                    //forming the where clause pattern for the vault field
                    if ((!PersonUtil.SEARCH_ALL_VAULTS.equals(strVaultOption))
                        && (!"".equals(companyId) && !QUERY_WILDCARD.equals(companyId)))
                    {
                        if (start)
                        {
                            whereExp.append(SYMB_OPEN_PARAN);
                            start = false;
                        }
                        else
                        {
                            whereExp.append(SYMB_AND);
                        }

                        whereExp.append("vault ");
                        whereExp.append("matchlist");
                        whereExp.append(SYMB_QUOTE);
                        whereExp.append(strVault);
                        whereExp.append(SYMB_QUOTE);
                        whereExp.append(" ");
                        whereExp.append(SYMB_QUOTE);
                        whereExp.append(SYMB_COMMA);
                        whereExp.append(SYMB_QUOTE);
                    }

                    //forming the where clause pattern for the program field
                    if (programId == null || "".equals(programId))
                    {
                        if (!(null == companyId) && !"".equals(companyId)
                            && !companyId.equals(QUERY_WILDCARD))
                        {
                            if (start)
                            {
                                whereExp.append(SYMB_OPEN_PARAN);
                                start = false;
                            }
                            else
                            {
                                whereExp.append(SYMB_AND);
                            }
                            whereExp.append(SYMB_OPEN_PARAN);
                            whereExp.append(SYMB_TO);
                            whereExp.append(SYMB_OPEN_BRACKET);
                            whereExp.append(ReqSchemaUtil.getCompanyProjectRelationship(context));
                            whereExp.append(SYMB_CLOSE_BRACKET);
                            whereExp.append(SYMB_DOT);
                            whereExp.append(SYMB_FROM);
                            whereExp.append(SYMB_DOT);
                            whereExp.append("id ");
                            whereExp.append(SYMB_EQUAL);
                            whereExp.append(companyId);
                            whereExp.append(SYMB_CLOSE_PARAN);
                        }

                        projectList = DomainObject.findObjects(context, projectType, sNameSelected, QUERY_WILDCARD,
                                sOwnerSelected, strVault, whereExp.toString(), false, selectStmts);
                    }
                    else
                    {
                        //get all the projects using getProjects of program class
                        DomainObject domObjProgram = new DomainObject(programId);
                        projectList = domObjProgram.getRelatedObjects(context, ReqSchemaUtil.getProgramProjectRelationship(context),
                                "*", selectStmts, null, false, true, (short) 1, whereExp.toString(), null);
                    }
                }

                //start of adv search criteria loop
                else
                {
                    //get the value of page key if it contains "adv" value display all the projects.
                    //User can refine the projects on the basis of advance search criteria

                    String sTextStartDate = "";
                    String strGetValues = "";
                    String sAttrib = "";
                    String sAttribValue = "";
                    String eMatrixFromDate = "";
                    String eMatrixToDate = "";
                    String eMatrixDate = "";
                    StringList dateAttrList = new StringList();
                    String timeZone = (String) programMap.get("timeZone");
                    timeZone = timeZone.substring(1);
                    double iClientTimeOffset = (new Double(timeZone)).doubleValue();

                    // to construct Query object
                    matrix.db.Query query = new matrix.db.Query("");
                    StringBuffer sbWhereExpSecond = new StringBuffer(150);

                    // Get the Advanced Findlike parameters
                    String sTxtKeyword = (String) programMap.get("txtKeyword");
                    String sTxtFormat = (String) programMap.get("txtFormat");
                    String sFormat = (String) programMap.get("comboFormat");

                    if (sTxtKeyword == null)
                        sTxtKeyword = "";
                    else
                        sTxtKeyword = sTxtKeyword.trim();

                    if (sTxtFormat == null)
                        sTxtFormat = "";
                    else
                        sTxtFormat = sTxtFormat.trim();

                    // Set format as All
                    if (sFormat == null)
                        sFormat = ALL;
                    else
                        sFormat = sFormat.trim();

                    // Set the search text
                    if (!sTxtKeyword.equals(""))
                        query.setSearchText(sTxtKeyword);
                    else
                        query.setSearchText("");

                    // Set the Search Format
                    if (!sTxtFormat.equals(""))
                    {
                        query.setSearchFormat(sTxtFormat);
                    }
                    else if (sFormat != null)
                    {
                        if (sFormat.equals(ALL))
                            query.setSearchFormat(QUERY_WILDCARD);
                        else
                            query.setSearchFormat(sFormat);
                    }

                    Set keySet = programMap.keySet();
                    int iPrefixLength = COMBO_PREFIX.length();
                    for (Iterator iter = keySet.iterator(); iter.hasNext();)
                    {
                        String key = (String) iter.next();
                        if (key.length() > iPrefixLength)
                        {
                            if (key.startsWith(COMBO_PREFIX))
                            {
                                String strOperator = (String) programMap.get(key);
                                strOperator = strOperator.trim();
                                if (!strOperator.equals(SYMB_WILD))
                                {
                                    String strSelectFieldName = key.substring(iPrefixLength);
                                    String strActualFieldName = TXT_PREFIX + strSelectFieldName;
                                    String strActualValue = (String) programMap.get(strSelectFieldName);

                                    if ((strActualValue == null) || strActualValue.equals(""))
                                    {
                                        strActualValue = (String) programMap.get(strActualFieldName);
                                    }
                                    strGetValues = (String) programMap.get("dateAttrListStr");
                                    dateAttrList = FrameworkUtil.split(strGetValues, SYMB_COMMA);

                                    if (key.length() > 4)
                                    {
                                        // Truncating the parameter name and add that in
                                        // the pattern object
                                        // Replaced comboDiscriptor with cd to make URL
                                        // smaller-URL has a 2k limit
                                        if (key.substring(0, 3).equals(COMBO_PREFIX))
                                        {
                                            sAttrib = key.substring(3, key.length());

                                            if (dateAttrList.contains(sAttrib))
                                            {
                                                sAttribValue = sAttrib.replace(' ', '_');
                                                sTextStartDate = (String) programMap.get(sAttribValue);
                                                // Update "sValue" with selected date - this variable will be
                                                // used later to prepare the where clause
                                                strActualValue = sTextStartDate;
                                            }
                                            if (strActualValue != null)
                                            {
                                                if (!(strActualValue.equals(QUERY_WILDCARD))
                                                    && !(strActualValue.equals("")))
                                                {
                                                    if (start)
                                                    {
                                                        sbWhereExpSecond.append(SYMB_OPEN_PARAN);
                                                        start = false;
                                                    }

                                                    if (strOperator.equals(OP_IS_BETWEEN))
                                                    {
                                                        sbWhereExpSecond.append(SYMB_OPEN_PARAN);
                                                    }
                                                    sbWhereExpSecond.append(SYMB_OPEN_PARAN);

                                                    if ((!strSelectFieldName
                                                            .equalsIgnoreCase(SELECT_NAME))
                                                        && (!strSelectFieldName
                                                                .equalsIgnoreCase(SELECT_DESCRIPTION))
                                                        && (!strSelectFieldName
                                                                .equalsIgnoreCase(SELECT_REVISION))
                                                        && (!strSelectFieldName
                                                                .equalsIgnoreCase(SELECT_OWNER)))
                                                    {
                                                        sbWhereExpSecond.append(SYMB_ATTRIBUTE);
                                                        sbWhereExpSecond.append(SYMB_OPEN_BRACKET);
                                                    }
                                                    sbWhereExpSecond.append(strSelectFieldName);

                                                    if ((!strSelectFieldName
                                                            .equalsIgnoreCase(SELECT_NAME))
                                                        && (!strSelectFieldName
                                                                .equalsIgnoreCase(SELECT_DESCRIPTION))
                                                        && (!strSelectFieldName
                                                                .equalsIgnoreCase(SELECT_REVISION))
                                                        && (!strSelectFieldName
                                                                .equalsIgnoreCase(SELECT_OWNER)))
                                                    {
                                                        sbWhereExpSecond.append(SYMB_CLOSE_BRACKET);
                                                    }
                                                    //forming the where clause by appending operators
                                                    if (strOperator.equals(OP_INCLUDES))
                                                    {
                                                        sbWhereExpSecond.append(SYMB_MATCH);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(SYMB_WILD);
                                                        sbWhereExpSecond.append(strActualValue);
                                                        sbWhereExpSecond.append(SYMB_WILD);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_PARAN);

                                                    }
                                                    else if (strOperator.equals(OP_IS_EXACTLY))
                                                    {
                                                        sbWhereExpSecond.append(SYMB_EQUAL);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(strActualValue);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
                                                    }
                                                    else if (strOperator.equals(OP_IS_NOT))
                                                    {
                                                        sbWhereExpSecond.append(SYMB_NOT_EQUAL);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(strActualValue);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
                                                    }
                                                    else if (strOperator.equals(OP_IS_MATCHES))
                                                    {
                                                        sbWhereExpSecond.append(SYMB_MATCH);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(strActualValue);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
                                                    }
                                                    else if (strOperator.equals(OP_BEGINS_WITH))
                                                    {
                                                        sbWhereExpSecond.append(SYMB_MATCH);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(strActualValue);
                                                        sbWhereExpSecond.append(SYMB_WILD);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_PARAN);

                                                    }
                                                    else if (strOperator.equals(OP_ENDS_WITH))
                                                    {
                                                        sbWhereExpSecond.append(SYMB_MATCH);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(SYMB_WILD);
                                                        sbWhereExpSecond.append(strActualValue);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
                                                    }
                                                    else if (strOperator.equals(OP_EQUALS))
                                                    {
                                                        sbWhereExpSecond.append(SYMB_EQUAL);
                                                        sbWhereExpSecond.append(strActualValue);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
                                                    }
                                                    else if (strOperator.equals(OP_DOES_NOT_EQUAL))
                                                    {
                                                        sbWhereExpSecond.append(SYMB_NOT_EQUAL);
                                                        sbWhereExpSecond.append(strActualValue);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
                                                    }
                                                    else if (strOperator.equals(OP_IS_BETWEEN))
                                                    {
                                                        strActualValue = strActualValue.trim();

                                                        int iSpace = strActualValue.indexOf(" ");
                                                        String strLow = "";
                                                        String strHigh = "";

                                                        if (iSpace == -1)
                                                        {
                                                            strLow = strActualValue;
                                                            strHigh = strActualValue;
                                                        }
                                                        else
                                                        {
                                                            strLow = strActualValue.substring(0, iSpace);
                                                            strHigh = strActualValue.substring(strLow.length() + 1);

                                                            iSpace = strHigh.indexOf(" ");

                                                            if (iSpace != -1)
                                                            {
                                                                strHigh = strHigh.substring(0, iSpace);
                                                            }

                                                        }
                                                        sbWhereExpSecond.append(SYMB_GREATER_THAN_EQUAL);
                                                        sbWhereExpSecond.append(strLow);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
                                                        sbWhereExpSecond.append(SYMB_AND);
                                                        sbWhereExpSecond.append(SYMB_OPEN_PARAN);
                                                        sbWhereExpSecond.append(SYMB_ATTRIBUTE);
                                                        sbWhereExpSecond.append(SYMB_OPEN_BRACKET);
                                                        sbWhereExpSecond.append(strSelectFieldName);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_BRACKET);
                                                        sbWhereExpSecond.append(SYMB_LESS_THAN_EQUAL);
                                                        sbWhereExpSecond.append(strHigh);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
                                                    }
                                                    else if (strOperator.equals(OP_IS_ATMOST))
                                                    {
                                                        sbWhereExpSecond.append(SYMB_LESS_THAN_EQUAL);
                                                        sbWhereExpSecond.append(strActualValue);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
                                                    }
                                                    else if (strOperator.equals(OP_IS_ATLEAST))
                                                    {
                                                        sbWhereExpSecond.append(SYMB_GREATER_THAN_EQUAL);
                                                        sbWhereExpSecond.append(strActualValue);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
                                                    }
                                                    else if (strOperator.equals(OP_IS_MORE_THAN))
                                                    {
                                                        sbWhereExpSecond.append(SYMB_GREATER_THAN);
                                                        sbWhereExpSecond.append(strActualValue);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
                                                    }
                                                    else if (strOperator.equals(OP_IS_LESS_THAN))
                                                    {
                                                        sbWhereExpSecond.append(SYMB_LESS_THAN);
                                                        sbWhereExpSecond.append(strActualValue);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
                                                    }
                                                    else if (strOperator.equals(OP_IS_ON))
                                                    {

                                                        //All objects created after 12:00:00AM and before 11:59:59PM on the selected date are to be retrieved - including the specified time
                                                        eMatrixFromDate = eMatrixDateFormat.getFormattedInputDateTime(
                                                                context, strActualValue, "12:00:00 AM",
                                                                iClientTimeOffset);
                                                        eMatrixToDate = eMatrixDateFormat.getFormattedInputDateTime(
                                                                context, strActualValue, "11:59:59 PM",
                                                                iClientTimeOffset);

                                                        sbWhereExpSecond.append(SYMB_GREATER_THAN_EQUAL);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(eMatrixFromDate);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(SYMB_AND);

                                                        sbWhereExpSecond.append(SYMB_ATTRIBUTE);
                                                        sbWhereExpSecond.append(SYMB_OPEN_BRACKET);
                                                        sbWhereExpSecond.append(sAttrib);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_BRACKET);

                                                        sbWhereExpSecond.append(SYMB_LESS_THAN_EQUAL);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(eMatrixToDate);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);

                                                        sbWhereExpSecond.append(SYMB_CLOSE_PARAN);

                                                    }
                                                    else if (strOperator.equals(OP_IS_ON_OR_BEFORE))
                                                    {
                                                        //All objects created before 11:59:59PM on the selected date are to be retrieved - including the specified time
                                                        eMatrixDate = eMatrixDateFormat.getFormattedInputDateTime(
                                                                context, strActualValue, "11:59:59 PM",
                                                                iClientTimeOffset);

                                                        sbWhereExpSecond.append(SYMB_LESS_THAN_EQUAL);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(eMatrixDate);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
                                                    }
                                                    else if (strOperator.equals(OP_IS_ON_OR_AFTER))
                                                    {
                                                        // All objects created after 12:00:00AM
                                                        // on the selected date are to be
                                                        // retrieved - including the specified
                                                        // time
                                                        eMatrixDate = eMatrixDateFormat.getFormattedInputDateTime(
                                                                context, strActualValue, "12:00:00 AM",
                                                                iClientTimeOffset);
                                                        sbWhereExpSecond.append(SYMB_GREATER_THAN_EQUAL);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(eMatrixDate);
                                                        sbWhereExpSecond.append(SYMB_QUOTE);
                                                        sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    String strWhereExpSecond = sbWhereExpSecond.toString();
                    String strWhereExp = strWhereExpSecond;

                    // Executing the Query and storing the result in the business object
                    // list
                    BusinessObjectWithSelectList boList = null;
                    //if the format is not empty
                    if (!sTxtFormat.equals(""))
                    {
                        query.setBusinessObjectName(QUERY_WILDCARD);
                        query.setBusinessObjectType(projectType);
                        query.setBusinessObjectRevision(QUERY_WILDCARD);
                        query.setOwnerPattern(QUERY_WILDCARD);
                        query.setVaultPattern(QUERY_WILDCARD);

                        query.setWhereExpression(sbWhereExpSecond.toString());

                        boList = query.select(context, selectStmts);
                        projectList = FrameworkUtil.toMapList(boList);
                    }
                    else
                    {
                        //find all the objects satisfying the where clause pattern
                        projectList = DomainObject.findObjects(context, projectType, QUERY_WILDCARD,
                                QUERY_WILDCARD, QUERY_WILDCARD,
                                QUERY_WILDCARD, strWhereExp, false, selectStmts);
                    }
                } //end of adv search criteria loop
            } //end of all projects search criteria
        } //end of try
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        return projectList;
    }

    /**
     * Returns all the folders of a project with add remove access on it to the context user
     *
     * @mx.whereUsed Invoked when expanding a project displayed in the structure browser.
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectId       - a String containing the objectId
     * @return MapList containing only those folder objects with add remove access to the user
     * @throws Exception if the operation fails
     * @since RequirementCentral X2
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public static MapList getProjectFolders(Context context, String[] args) throws Exception
    {
        MapList folderList = new MapList();
        MapList foldersWithAddRemoveAccessList = new MapList();
        try
        {
            Map objectMap = null;
            String sFolferId = "";
            String userAccess = "";
            String user = context.getUser();
            HashMap programMap = (HashMap) JPO.unpackArgs(args);

            String objectId = (String) programMap.get(SYMB_OBJECT_ID);
            String folderType = ReqSchemaUtil.getWorkspaceVaultType(context);

            StringBuffer stbRelSelect = new StringBuffer(50);
//          Modified:6-Feb-09:kyp:R207:RMT Bug 368668
            stbRelSelect = stbRelSelect.append(ReqSchemaUtil.getProjectVaultsRelationship(context)).append(SYMB_COMMA)
                                        .append(ReqSchemaUtil.getSubVaultsRelationship(context)).append(SYMB_COMMA)
                                        .append(ReqSchemaUtil.getLinkedFoldersRelationship(context));

            final String SELECT_ATTRIBUTE_TITLE = "attribute[" + ReqSchemaUtil.getTitleAttribute(context) + "]";
            final String SELECT_KINDOF_CONTROLLED_FOLDER = "type.kindof[" + ReqSchemaUtil.getControlledFolderType(context) + "]";

//          End:R207:RMT Bug 368668

            StringList selectStmts = new StringList(3);
            selectStmts.addElement(SELECT_ID);
            selectStmts.addElement(SELECT_NAME);

//          Modified:6-Feb-09:kyp:R207:RMT Bug 368668
            selectStmts.addElement(SELECT_CURRENT);
            selectStmts.addElement(SELECT_ATTRIBUTE_TITLE);
            selectStmts.addElement(SELECT_KINDOF_CONTROLLED_FOLDER);
//          End:R207:RMT Bug 368668

            DomainObject domObjProject = new DomainObject(objectId.trim());

            //get all the folders  & sub-folders related to project
            folderList = domObjProject.getRelatedObjects(context, stbRelSelect.toString(), folderType, selectStmts,
                    null, false, true, (short) 1, null, null);

            if (folderList != null)
            {
                //iterate through each folder to check the access given to user on it.
                for (int i = 0; i < folderList.size(); i++)
                {
                    objectMap = (Map) folderList.get(i);
                    sFolferId = (String) objectMap.get(SELECT_ID);

//                  Added:6-Feb-09:kyp:R207:RMT Bug 368668
                    // The Superceded folders should not be shown in the listing
                    boolean isControlledFolder = "true".equalsIgnoreCase((String) objectMap.get(SELECT_KINDOF_CONTROLLED_FOLDER));
                    if (isControlledFolder) {
                        String strCurrentState = (String) objectMap.get(SELECT_CURRENT);
                        if (STATE_CONTROLLED_FOLDER_SUPERCEDED.equals(strCurrentState)) {
                            continue;
                        }
                    }
//                  End:R207:RMT Bug 368668


                    WorkspaceVault workspaceVault = (WorkspaceVault) DomainObject.newInstance(context, sFolferId);

                    // Need to get a Map of all vaultMembers
                    Map vaultMemberMap = workspaceVault.getUserPermissions(context);

                    // If projectMember is also a vaultMember, then
                    if (vaultMemberMap.containsKey(user))
                    {
                        //get the access provided to the context user
                        userAccess = (String) vaultMemberMap.get(user);
                        //get the context user has add/remove access on the folder, add the folder to the list
                        if (ADD_REMOVE.equalsIgnoreCase(userAccess))
                            foldersWithAddRemoveAccessList.add(objectMap);
                    }
                } //end of iteration
            } //end of condition check for folders
            if(((String) programMap.get("fullTextSearch")).equalsIgnoreCase("true")){
    	        int MapSize = foldersWithAddRemoveAccessList.size();
    	        foldersWithAddRemoveAccessList.add(0, MapSize);
            }
        } //end of try
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        
        return foldersWithAddRemoveAccessList;
    }

    /**
     * Returns a vector to display the access for the projects and folders.
     *
     * @mx.whereUsed Invoked when projects and folders are displayed in the structure browser table.
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectList       - a MapList containing the objectList
     * @return Vector containing "add remove" access for the folders
     * @throws Exception
     *             if the operation fails
     * @since RequirementCentral X2
     * @grade 0
     */
    public Vector showProjectFolders(Context context, String[] args) throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
//            String folderType = ReqSchemaUtil.getWorkspaceVaultType(context);

            final String SELECT_IS_KINDOF_PROJECT_FOLDER = "type.kindof[" + ReqSchemaUtil.getWorkspaceVaultType(context) + "]";

            int objSize = 0;
            Vector folderAccessList = new Vector(objSize);

            if (objectList != null)
            {
                objSize = objectList.size();
                folderAccessList = new Vector(objSize);

                String[] idArray = new String[objSize];
                //iterate through each object in the objectList and form an array of the ids.
                for (int itr = 0; itr < objSize; itr++)
                {
                    Map tempMap = (Map) objectList.get(itr);
                    idArray[itr] = (String) tempMap.get(SELECT_ID);
                }

                StringList objectSelects = new StringList();
//                objectSelects.addElement(SELECT_TYPE);
                objectSelects.addElement(SELECT_IS_KINDOF_PROJECT_FOLDER);

                //Get the type of the object for an array of the ids.
                MapList mlObjInfo = DomainObject.getInfo(context, idArray, objectSelects);
                for (int itr = 0; itr < mlObjInfo.size(); itr++)
                {
                    Map mapObjInfo = (Map) mlObjInfo.get(itr);

                    //As the maplist contains only two types of objects so if the type is folder then add "Add remove" access to the vector
                    if ("TRUE".equalsIgnoreCase((String)mapObjInfo.get(SELECT_IS_KINDOF_PROJECT_FOLDER)))
                    {
                        folderAccessList.add(ADD_REMOVE);
                    }
                    else
                    { //if the type is project then add "" to the vector
                        folderAccessList.add("");
                    }
                }
            }

            return folderAccessList;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * Returns a vector to display the projects in the Requirement Specification table summary page.
     *
     * @mx.whereUsed Invoked when projects are displayed in the Requirement Specification table summary page.
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectList       - a MapList containing the objectList
     * @return Vector containing projects.
     * @throws Exception
     *             if the operation fails
     * @since RequirementCentral X2
     * @grade 0
     */
    public static Vector getProjectsForRequirementSpecification(Context context, String[] args) throws Exception
    {
        Vector projectResultsVec = new Vector();
        StringList folderIdList = new StringList();
        StringList sortedFolderList = new StringList();

        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");

            Map objectMap = null;
            int objectListSize = 0;
            String projectId = "";
            String projectName = "";
            String objectId = "";

            String typeIcon = "";
            String defaultTypeIcon = "";
            String uniqueProjectId = "";

            String projectType = ReqSchemaUtil.getProjectSpaceType(context);

            if (objectList != null)
                objectListSize = objectList.size();

            for (int i = 0; i < objectListSize; i++)
            {
                StringBuffer sbProjectsName = new StringBuffer();
                StringBuffer sbRelName = new StringBuffer();
                objectMap = (Map) objectList.get(i);
                objectId = (String) objectMap.get(SELECT_ID);

                DomainObject domObjProject = new DomainObject(objectId.trim());

                sbRelName.append(SYMB_TO);
                sbRelName.append(SYMB_OPEN_BRACKET);
                sbRelName.append(ReqSchemaUtil.getVaultedDocumentsRev2Relationship(context));
                sbRelName.append(SYMB_CLOSE_BRACKET);
                sbRelName.append(SYMB_DOT);
                sbRelName.append(SYMB_FROM);
                sbRelName.append(SYMB_DOT);
                sbRelName.append(SYMB_TO);
                sbRelName.append(SYMB_OPEN_BRACKET);
                sbRelName.append(ReqSchemaUtil.getProjectVaultsRelationship(context));
                sbRelName.append(SYMB_CLOSE_BRACKET);
                sbRelName.append(SYMB_DOT);
                sbRelName.append(SYMB_FROM);
                sbRelName.append(SYMB_DOT);
                sbRelName.append(SELECT_ID);

                folderIdList = domObjProject.getInfoList(context, sbRelName.toString());
                if (folderIdList != null && folderIdList.size() > 0 && !folderIdList.contains(" "))
                {
                    Iterator itr = folderIdList.iterator();

                    //add unique project ids to the list so that same project is not displayed twice.
                    while (itr.hasNext())
                    {
                        projectId = (String) itr.next();
                        if (!(sortedFolderList.contains(projectId)))
                            sortedFolderList.add(projectId);
                    }
                }

                typeIcon = UINavigatorUtil.getTypeIconProperty(context, projectType);
                defaultTypeIcon = "<img src='../common/images/" + typeIcon + "' border=0>";

                //form the html to display the projects in the project column of Requirement Specification table summary page.
                if (folderIdList != null && folderIdList.size() > 0 && !folderIdList.contains(""))
                {
                    Iterator itrFolder = sortedFolderList.iterator();
                    while (itrFolder.hasNext())
                    {
                        sbProjectsName.append(defaultTypeIcon);
                        sbProjectsName
                                .append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
                        sbProjectsName.append("&suiteKey=");
                        sbProjectsName.append("&objectId=");

                        uniqueProjectId = (String) itrFolder.next();
                        sbProjectsName.append(uniqueProjectId);

                        sbProjectsName.append("', '', '', 'false', 'content', '')\">");

                        DomainObject dom = new DomainObject(uniqueProjectId.trim());
                        projectName = dom.getInfo(context, SELECT_NAME);
                        sbProjectsName.append(XSSUtil.encodeForHTML(context, projectName));
                        sbProjectsName.append("</a>");

                        if (itrFolder.hasNext())
                            sbProjectsName.append("<br>");
                    }

                    sortedFolderList.clear();
                }
                projectResultsVec.add(sbProjectsName.toString());
            }

            return projectResultsVec;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
    * Displays the Projects in structure browser.
    *
    * @mx.whereUsed Invoked when add to Project folder field's chooser of edit page clicked.
    * @param context
    *            the eMatrix <code>Context</code> object
    * @param args contains a Map with the following entries:
    *    paramMap       - a Map containing the paramMap which contains object ID
    *    fieldMap       - a Map containing the fieldMap which contains name
    * @return String containing projects.
    * @throws Exception
    *             if the operation fails
    * @since RequirementCentral X2
    * @grade 0
    */
    public String getHTMLForAddToProject(Context context, String[] args) throws Exception
    {
        StringBuffer sbBuffer = new StringBuffer();
        try
        {
            final String SELECT_ATTRIBUTE_TITLE = "attribute[" + ReqSchemaUtil.getTitleAttribute(context) + "]";
            final String SELECT_IS_KINDOF_CONTROLLED_FOLDER = "type.kindof[" + ReqSchemaUtil.getControlledFolderType(context) + "]";
            
            Map programMap = (HashMap) JPO.unpackArgs(args);
            Map relBusObjPageList = (HashMap) programMap.get("paramMap");
            Map fieldMap = (HashMap) programMap.get("fieldMap");
            Map requestMap = (HashMap) programMap.get("requestMap");
            String strMode = (String) requestMap.get("mode");
            String strObjectId = (String) relBusObjPageList.get(SYMB_OBJECT_ID);
            String strFieldName = (String) fieldMap.get(SELECT_NAME);

            String strRelObjId = "";
            String strRelObjName = "";
            String strRelObjTitle = "";
            String strAllRelObjId = "";
            String strAllRelObjName = "";
            boolean isControlledFolder = false;

            StringBuffer stbRelSelect = new StringBuffer(50);
            StringList objSelectList = new StringList(2);

            String relVaultedDocs = ReqSchemaUtil.getVaultedDocumentsRev2Relationship(context);
            String folderType = ReqSchemaUtil.getWorkspaceVaultType(context);

            objSelectList.add(SELECT_NAME);
            objSelectList.add(SELECT_ID);
            objSelectList.add(SELECT_IS_KINDOF_CONTROLLED_FOLDER);
            objSelectList.add(SELECT_ATTRIBUTE_TITLE);

            stbRelSelect = stbRelSelect.append(ReqSchemaUtil.getProjectVaultsRelationship(context)).append(SYMB_COMMA).append(
                    ReqSchemaUtil.getSubVaultsRelationship(context));

            // Get the Attributes by changing the context to super user
            DomainObject domObj = DomainObject.newInstance(context, strObjectId);
            ContextUtil.pushContext(context);

            // Get all the related folder objects from requirement Specification object
            MapList reqObjMapList = domObj.getRelatedObjects(context, relVaultedDocs, folderType,
                    objSelectList, null, true, true, (short) 0, null, null);
            Iterator itr = reqObjMapList.iterator();

            while (itr.hasNext())
            {
                Map reqObjMap = (Map) itr.next();
                strRelObjId = (String) reqObjMap.get(SELECT_ID);
                strRelObjName = (String) reqObjMap.get(SELECT_NAME);

                // If this is Controlled Folder then the name of the folder is actually Title attribute
                strRelObjTitle = (String) reqObjMap.get(SELECT_ATTRIBUTE_TITLE);
                isControlledFolder = "TRUE".equalsIgnoreCase((String) reqObjMap.get(SELECT_IS_KINDOF_CONTROLLED_FOLDER));
                if (isControlledFolder) {
                    strRelObjName = strRelObjTitle;
                }

                if ("".equals(strAllRelObjId) || "null".equals(strAllRelObjId))
                {
                    strAllRelObjId = strRelObjId;
                    strAllRelObjName = strRelObjName;
                }
                else
                {
                    strAllRelObjId = strAllRelObjId + SYMB_COMMA + strRelObjId;
                    strAllRelObjName = strAllRelObjName + SYMB_COMMA + strRelObjName;
                }

            }

            ContextUtil.popContext(context);
            boolean pmcInstall = FrameworkUtil.isSuiteRegistered(context,
                    "appVersionProgramCentral", false, null, null);

            String sNoticeMessage1 = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Alert.PMCNotInstalled"); 

            // If the mode is edit, display the add to project folder field as textbox with a chooser.
            if (strMode != null && !"null".equals(strMode) && "edit".equalsIgnoreCase(strMode))
            {
                sbBuffer.append("<input type=\"text\" READONLY ");
                sbBuffer.append("name=\"");
                sbBuffer.append(strFieldName);
                sbBuffer.append("Display\" id=\"\" value=\"");
                sbBuffer.append(strAllRelObjName);
                sbBuffer.append("\">");
                sbBuffer.append("<input type=\"hidden\" name=\"");
                sbBuffer.append(strFieldName);
                sbBuffer.append("\" value=\"");
                sbBuffer.append(strAllRelObjId);
                sbBuffer.append("\">");
                sbBuffer.append("<input type=\"hidden\" name=\"");
                sbBuffer.append(strFieldName);
                sbBuffer.append("OID\" value=\"");
                sbBuffer.append(strAllRelObjId);
                sbBuffer.append("\">");
                sbBuffer.append("<input ");
                sbBuffer.append("type=\"button\" name=\"btnSpecAddtoProject\" ");
                sbBuffer.append("size=\"200\" value=\"...\" alt=\"\" enabled=\"true\" ");
                sbBuffer.append("onClick=\"if(");
                sbBuffer.append(!pmcInstall);
                sbBuffer.append("){alert('");
                sbBuffer.append(sNoticeMessage1.replaceAll("'", "\\\\'")); //BUG:358218
                
                //IR-272999V6R2015 HAT1:ZUD to call /common/emxFullSearch.jsp instead of /requirements/SpecificationProcess.jsp.
                sbBuffer.append("')} else showChooser('../common/emxFullSearch.jsp?table=RMTProjectFolderList&field=TYPES=type_ProjectVault"+"&mode=addToProject");
                //sbBuffer.append("')} else showChooser('../requirements/SpecificationProcess.jsp?formName=editDataForm&mode=addToProject");                
                sbBuffer.append("&frameName=formEditDisplay");
                sbBuffer.append("&fieldNameActual=");
                sbBuffer.append(strFieldName);
                sbBuffer.append("&fieldNameDisplay=");
                sbBuffer.append(strFieldName);
                sbBuffer.append("Display");
                sbBuffer.append("&fieldNameOID=");
                sbBuffer.append(strFieldName);
                sbBuffer.append("OID");
                sbBuffer.append("&searchmode=chooser");
                sbBuffer.append("&suiteKey=Requirements");
                sbBuffer.append("&objectId=");
                sbBuffer.append(strObjectId);
              //IR-272999V6R2015 HAT1 : ZUD on Submit calling SpecificationConnectToFolderProcess.jsp.Appending 3 more attributes formName, fieldNameDisplay, fieldNameActual. 
//IR-272999V6R2015 start
                sbBuffer.append("&submitURL=../requirements/SpecificationConnectToFolderProcess.jsp&suiteKey=Requirements&HelpMarker=emxhelpprojectsearchresults&header=emxRequirements.Heading.ProjectSearchResults&selection=multiple&submitLabel=emxFramework.Common.Done&cancelLabel=emxFramework.Common.Cancel&toolbar=RMTRequirementSpecificationStructureBrowserToolbar&show=active");
                sbBuffer.append("&formName="+"editDataForm");
                sbBuffer.append("&fieldNameDisplay="+strFieldName);
                sbBuffer.append("&fieldNameActual="+strFieldName);
//IR-272999V6R2015 ends
                sbBuffer.append("','700','500')\">");
                sbBuffer.append("&nbsp;&nbsp;");
                sbBuffer.append("<a href=\"javascript:basicClear('");
                sbBuffer.append(strFieldName);
                sbBuffer.append("')\">");
                String strClear = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Button.Clear"); 
                sbBuffer.append(strClear);
                sbBuffer.append("</a>");
            }
            else
            {
                sbBuffer.append(strAllRelObjName);
            }
        }
        catch (Exception e)
        {
            throw new Exception(e.getMessage());
        }
        return sbBuffer.toString();
    }

    /**
     * update the requirement specification object with selected folders.
     *
     * @mx.whereUsed Invoked when values are selected in the add to Project folder field of edit page.It update the object with the selected values.
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    paramMap       - a Map containing the paramMap which contains object ID
     *    requestMap     - a Map containing the requestMap.
     * @return int.
     * @throws Exception
     *             if the operation fails
     * @since RequirementCentral X2
     * @grade 0
     */
    public int updateAddToProject(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        HashMap requestMap = (HashMap) programMap.get("requestMap");

        String strObjectId = (String) paramMap.get(SYMB_OBJECT_ID);
        String[] strNewValues = (String[]) requestMap.get("ProjectFolder");
        String strNewValue = strNewValues[0];
        String strRelatedObjId = "";

        String relVaultedDocs = ReqSchemaUtil.getVaultedDocumentsRev2Relationship(context);
        String folderType = ReqSchemaUtil.getWorkspaceVaultType(context);

        StringList idsList = new StringList();
        StringList selectionList = new StringList();
        DomainObject domObj = DomainObject.newInstance(context, strObjectId);
        ContextUtil.startTransaction(context, true);

        try
        {
            StringList objSelectList = new StringList(2);
            objSelectList.add(SELECT_ID);
            
            StringList relSelectList = new StringList();
            relSelectList.add(DomainRelationship.SELECT_ID);
            
            setId(strObjectId);

            //get the all related folder objects from requirement Specification object
            MapList reqObjMapList = domObj.getRelatedObjects(context, relVaultedDocs, folderType,
                  objSelectList, relSelectList, true, true, (short) 0, null, null);

            Iterator itr = reqObjMapList.iterator();
            while (itr.hasNext())
            {
                Map reqObjMap = (Map) itr.next();
                idsList.add((String)reqObjMap.get(SELECT_ID));
            }

            if ((strNewValue == null) || SYMB_NULL.equalsIgnoreCase(strNewValue) || "".equals(strNewValue)) {
                strNewValue = "";
            }
            
                selectionList = FrameworkUtil.split(strNewValue, SYMB_COMMA);

            // Find out the existing folders to be disconnected
            StringList slFoldersToDisconnect = new StringList();
            for (Iterator itrExistingFolders = reqObjMapList.iterator(); itrExistingFolders.hasNext();) {
                Map reqObjMap = (Map)itrExistingFolders.next();
                String strExistingFolderId = (String) reqObjMap.get(SELECT_ID);
                String strExistingFolderRelId = (String) reqObjMap.get(DomainRelationship.SELECT_ID);
                if (!selectionList.contains(strExistingFolderId)) {
                    slFoldersToDisconnect.add(strExistingFolderRelId);
                    }
                }

            // Find out the existing folders to be connected
            StringList slFoldersToConnect = new StringList();
            for (Iterator itrSelectedFolders = selectionList.iterator(); itrSelectedFolders.hasNext();) {
                String strSelectedFolderId = (String) itrSelectedFolders.next();
                if (!idsList.contains(strSelectedFolderId)) {
                    slFoldersToConnect.add(strSelectedFolderId);
                }
                }

            String[] strFoldersToConnect = new String[slFoldersToConnect.size()];
            strFoldersToConnect = (String[])slFoldersToConnect.toArray(strFoldersToConnect);
            
            if (strFoldersToConnect.length > 0) {
                //To connect the requirement specification object to the selected folders
                DomainRelationship.connect(context, domObj, relVaultedDocs, false, strFoldersToConnect);
            }
            
            String[] strFoldersToDisconnect = new String[slFoldersToDisconnect.size()];
            strFoldersToDisconnect = (String[])slFoldersToDisconnect.toArray(strFoldersToDisconnect);
            
            if (strFoldersToDisconnect.length > 0) {
                DomainRelationship.disconnect(context, strFoldersToDisconnect);
            }
            
            ContextUtil.commitTransaction(context);
            return 0;
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction(context);
            throw new Exception(e.getMessage());
        }
    }// End of updateAddToProject method

    /**
     * Displays the New Icon Window for Projects and Folders in structure browser.
     *
     * @mx.whereUsed Invoked when add to Project folder field's chooser of edit or create page clicked or add to Project folder Command is clicked.
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *     objectList       - a MapList containing the objectList
     * @return Vector containing new Icons for Projects and Folders.
     * @throws Exception
     *             if the operation fails
     * @since RequirementCentral X2
     * @grade 0
     */
    public static Vector getNewIconsForProjectsAndFolders(Context context, String[] args) throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            String folderType = ReqSchemaUtil.getWorkspaceVaultType(context);

            String uniqueFolderId = "";
            String uniqueProjectId = "";

            int objSize = 0;
            Vector newIconList = new Vector();

            if (objectList != null)
            {
                objSize = objectList.size();

                String[] idArray = new String[objSize];
                //iterate through each object in the objectList and form an array of the ids.
                for (int itr = 0; itr < objSize; itr++)
                {
                    Map tempMap = (Map) objectList.get(itr);
                    idArray[itr] = (String) tempMap.get(SELECT_ID);
                }

                StringList objectSelects = new StringList();
                objectSelects.addElement(SELECT_TYPE);

                //Get the type of the object for an array of the ids.
                MapList listTypes = DomainObject.getInfo(context, idArray, objectSelects);
                for (int itr = 0; itr < listTypes.size(); itr++)
                {
                    Map typeMap = (Map) listTypes.get(itr);
                    StringBuffer sbNewIconValues = new StringBuffer();
                    //As the maplist contains only two types of objects so if the type is folder then add treeMenu also.
                    if (folderType.equals(typeMap.get(SELECT_TYPE)))
                    {
                        sbNewIconValues.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?");
                        sbNewIconValues.append("objectId=");
                        uniqueFolderId = idArray[itr];
                        sbNewIconValues.append(uniqueFolderId);
                        sbNewIconValues.append("&amp;treeMenu=PMCtype_ProjectVault&amp;mode=insert");
                        sbNewIconValues.append("', '875', '550', 'false', 'popup', '')\">");
                        sbNewIconValues.append("<img src=\"images/iconNewWindow.gif\" border=\"0\" />");
                        sbNewIconValues.append("</a>");
                        newIconList.add(sbNewIconValues.toString());
                    }
                    else
                    {
                        sbNewIconValues.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?");
                        sbNewIconValues.append("objectId=");
                        uniqueProjectId = idArray[itr];
                        sbNewIconValues.append(uniqueProjectId);
                        sbNewIconValues.append("', '875', '550', 'false', 'popup', '')\">");
                        sbNewIconValues.append("<img border=\"0\" src=\"images/iconNewWindow.gif\" />");
                        sbNewIconValues.append("</a>");
                        newIconList.add(sbNewIconValues.toString());
                    }
                }
            }
            return newIconList;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * Return the RMB menu settings for specification structure.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args JPO arguments
     * @throws Exception
     *             if the operation fails
     * @return HashMap Map of Specification Structure Objects
     * @since RequirementCentral X4
     */
    public HashMap getSpecStructureRMBMenu(Context context,String[] args )throws Exception
	{
		HashMap hmpInput  = (HashMap)JPO.unpackArgs(args);

		HashMap requestMap = (HashMap) hmpInput.get("requestMap");
		String strlanguage = (String) requestMap.get("languageStr");

		//HashMap commandMap = (HashMap) hmpInput.get("commandMap");

		HashMap paramMap = (HashMap) hmpInput.get("paramMap");
		String objectId = (String)paramMap.get("objectId");
    String parentId       = null;
		//use rmbTableRowId to override objectId
		String tableRowId = (String)paramMap.get("rmbTableRowId");
		if(tableRowId != null){
			String[] tokens = tableRowId.split("[|]", -1);
			objectId = tokens[1];
			parentId = tokens[2];
		}

		DomainObject obj = new DomainObject();
		obj.setId(objectId);
		String type = obj.getInfo(context, DomainConstants.SELECT_TYPE);
		String menuName = null;
		if(mxType.isOfParentType(context, type, ReqSchemaUtil.getRequirementSpecificationType(context))){
			menuName = "RMTSpecStructureSpecRMB";
		}
		//T25 DJH 13:04:19
		else if(mxType.isOfParentType(context, type, ReqSchemaUtil.getRequirementType(context))){
	    	if(RequirementsUtil.isRequirement(context, objectId) && 
	    			(parentId.trim().length() == 0 || RequirementsUtil.isRequirement(context, parentId)) || RequirementsUtil.isParameter(context, objectId))
	    	{
	    		menuName = "RMTSpecStructureSubRequirementRMB"; // common RMB menu for Sub and Derived Reqs
	    	}
	    	else
	    	{
			menuName = "RMTSpecStructureRequirementRMB";
	    	}
		}
		//End T25 DJH
		else if(mxType.isOfParentType(context, type, ReqSchemaUtil.getChapterType(context))){
			menuName = "RMTSpecStructureChapterRMB";
		}
		else if(mxType.isOfParentType(context, type, ReqSchemaUtil.getCommentType(context))){
			menuName = "RMTSpecStructureCommentRMB";
		}
		//T25 DJH 13:05:13
		else if(mxType.isOfParentType(context, type, ReqSchemaUtil.getRequirementGroupType(context))){
			menuName = "RMTSpecStructureFolderRMB";
		}
		//End T25 DJH
		else{
			menuName = "AEFDefaultRMB";
		}

		HashMap menuMap = UIToolbar.getToolbar(context, menuName, PersonUtil.getAssignments(context), objectId, requestMap, strlanguage);
		
		MapList statChildren = UIToolbar.getChildren(menuMap);
		MapList resolvedChildren	=	new MapList();
	    for (int i = 0; i < statChildren.size(); i++)
	    {
	        HashMap child	=	(HashMap) statChildren.get(i);
	        String strJPOName	=	UIToolbar.getSetting(child,"Dynamic Command Program");
	        String strMethodName	=	UIToolbar.getSetting(child,"Dynamic Command Function");
	        //If there is no Dynamic Setting,or incase Item is a Menu
	        //add it to the MapList
	        if (strJPOName == null || "".equals(strJPOName) || UIToolbar.isMenu(child))
	        {
	        	resolvedChildren.add(child);
	        }
	        //If the item is a command with  a dynamic Setting,invoke the JPO
	        else
	        {
	            Map commandMap = (HashMap)child.clone();
	            
	            HashMap hmpJPOInput = new HashMap();
		        hmpJPOInput.put("paramMap",paramMap);
		        hmpJPOInput.put("commandMap",commandMap);
		        hmpJPOInput.put("requestMap",requestMap);
		        HashMap hmpDynMenu = (HashMap)JPO.invoke(context, strJPOName, null, strMethodName,
	                    JPO.packArgs(hmpJPOInput), HashMap.class);
		        MapList dynamicCmdOnMenu = UIToolbar.getChildren(hmpDynMenu);
	            if (dynamicCmdOnMenu != null)
	            {
	                for (int j = 0; j < dynamicCmdOnMenu.size(); j++)
	                {
	                	child = (HashMap) dynamicCmdOnMenu.get(j);
				        resolvedChildren.add(child);
	                }
	            }

	        }
	    }

	    menuMap.put("Children", resolvedChildren);
		return menuMap;

	}
    
    /** This method gets the object Structure List for the context RequirementSpec object.This method gets invoked
     *  by settings in the command which displays the Structure Navigator for RequirementSpec type objects
     *  @param context the eMatrix <code>Context</code> object
     *  @param args    holds the following input arguments:
     *      paramMap   - Map having object Id String
     *  @return MapList containing the object list to display in RequirementSpec structure navigator
     *  @throws Exception if the operation fails
     *  @since R216
     */
    public static MapList getStructureList(Context context, String[] args) throws Exception {
        MapList requirementStructList = new MapList();
        return (requirementStructList);
    }
    
    /**
     * Returns list of active specifications excluding a specified spec.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args JPO arguments
     * @throws Exception
     *             if the operation fails
     * @return MapList list of source specification
     * @since RequirementCentral X4
     */
    public static MapList getSourceSpecifications(Context context, String args[]) throws Exception
    {
		Map programMap = (Map) JPO.unpackArgs(args);
    	String srcSpecId = (String)programMap.get("srcSpecId");
    	MapList specs = getActiveRequirementSpecifications(context, args);
		if(specs != null && srcSpecId != null){
			// Now, remove the excluded Objects from the list of all Objects...
			for (int jj = specs.size()-1; jj >= 0; jj--)
			{
				Map objectMap = (Map) specs.get(jj);
				String objectId = (String) objectMap.get(DomainConstants.SELECT_ID);

				if (objectId != null && objectId.equals(srcSpecId)){
					specs.remove(jj);
				}
			}
		}
		return specs;
    }

    //Added:2-Feb-09:kyp:R207:Bug 361383
    /**
     * Returns values of Project column for RMTRequirementSpecificationsList UI table
     *
     * @param context The Matrix Context object
     * @param args The packed arguments for the UI table column method
     * @return The Vector containing value for each row of the table column
     * @throws MatrixException if operation fails
     * @since X3
     */
    public Vector getRequirementSpecificationProject(Context context, String[] args) throws MatrixException {
        try{
            Map programMap = (Map) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramMap = (Map)programMap.get("paramList");
            Vector vecResult = new Vector(objectList.size());

            String strSuiteKey = (String)paramMap.get("suiteKey");
            String strSuiteDir = EnoviaResourceBundle.getProperty(context, "eServiceSuite" + strSuiteKey + ".Directory");
            //
            // Take care of the printer friendly and exporting behavior.
            // If there is reportFormat key present in paramMap, it means that currently
            // it is not normal table rendering, but either it is rendering for printer friendly, HTML or
            // CSV format
            //
            String strReportFormat = (String)paramMap.get("reportFormat");
            boolean isReporting = (strReportFormat != null);
            boolean isHTMLReporting = "HTML".equalsIgnoreCase(strReportFormat);

            Map mapObjInfo = null;
            StringBuffer sbHTML = null;
            String strObjectId = null;
            Map mapTypeIcons = new HashMap();
            MapList mlParentProjects = null;

            StringList slParentProjectBusSelect = new StringList();
            slParentProjectBusSelect.add(SELECT_ID);
            slParentProjectBusSelect.add(SELECT_TYPE);
            slParentProjectBusSelect.add(SELECT_NAME);

            final String TEMPLATE_HTML_ANCHOR = "<img src=\"../common/images/${ICON_NAME}\" border=\"0\"/>";
            final String TEMPLATE_HTML_PRINTER_FRIENDLY = "<img src=\"../common/images/${ICON_NAME}\" border=\"0\"/>${PROJECT_NAME}";
            for (Iterator itrObjectList = objectList.iterator(); itrObjectList.hasNext();) {
                mapObjInfo = (Map)itrObjectList.next();

                // What is current RSP id?
                strObjectId = (String)mapObjInfo.get(DomainConstants.SELECT_ID);
                String access = (String)mapObjInfo.get(ReqConstants.SELECT_READ_ACCESS);
                if(access == null || ReqConstants.DENIED.equals(access)){
                    vecResult.add("");
                    continue;
                }

                // Which all projects this RSP related?
                mlParentProjects = findParentProjects(context, strObjectId, slParentProjectBusSelect);
                sbHTML = new StringBuffer(128);

                for (Iterator itrParentProjects = mlParentProjects.iterator(); itrParentProjects
                        .hasNext();) {
                    Map mapParentProjectInfo = (Map) itrParentProjects.next();

                    String strParentProjectId = (String)mapParentProjectInfo.get(SELECT_ID);
                    String strParentProjectType = (String)mapParentProjectInfo.get(SELECT_TYPE);
                    String strParentProjectName = (String)mapParentProjectInfo.get(SELECT_NAME);

                    // Get icon for this type
                    String strTypeIcon = (String)mapTypeIcons.get(strParentProjectType);
                    if (strTypeIcon == null) {
                        //emxFramework.smallIcon.type_ProjectSpace
                        String strParentProjectSymType = FrameworkUtil.getAliasForAdmin(context, "Type", strParentProjectType, true);
                        try {
                            strTypeIcon = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon." + strParentProjectSymType);
                        }
                        catch (FrameworkException frameworkException) {
                            strTypeIcon = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon.defaultType");
                        }

                        mapTypeIcons.put(strParentProjectType, strTypeIcon);
                    }

                    //Form the HTML link
                    if (isReporting) {
                        if (isHTMLReporting) {
                            String strHTML = FrameworkUtil.findAndReplace(TEMPLATE_HTML_PRINTER_FRIENDLY, "${ICON_NAME}", strTypeIcon);
                            strHTML = FrameworkUtil.findAndReplace(strHTML, "${PROJECT_NAME}", strParentProjectName);
                            if (sbHTML.length() != 0) {
                                sbHTML.append("<br/>");
                            }
                            sbHTML.append(strHTML);
                        }
                        else {
                            if (sbHTML.length() != 0) {
                                sbHTML.append(", ");
                            }
                            sbHTML.append(strParentProjectName);
                        }
                    }
                    else {
                        StringBuffer sbFinalHTMLURL = new StringBuffer();
                    	String strHTML = FrameworkUtil.findAndReplace(TEMPLATE_HTML_ANCHOR, "${ICON_NAME}", strTypeIcon);
                    	
                    	sbFinalHTMLURL.append(strHTML);
                    	sbFinalHTMLURL.append(" ");
                    	sbFinalHTMLURL.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=");
                    	sbFinalHTMLURL.append(strParentProjectId);
                    	sbFinalHTMLURL.append("', '875', '550', 'false', 'popup', '')\">");
                    	sbFinalHTMLURL.append(XSSUtil.encodeForHTML(context, strParentProjectName));
                    	sbFinalHTMLURL.append("</a>");
                    	
                        if (sbHTML.length() != 0) {
                            sbHTML.append("<br/>");
                        }
                        sbHTML.append(sbFinalHTMLURL);
                    }
                }//for each RSP
                vecResult.add(sbHTML.toString());
            }
            return vecResult;
        }
        catch (Exception e) {
            throw new MatrixException(e);
        }
    }

    /**
     * Finds projects for the given RSP object. It is assumed that the RSP is added to a folder under PMC project.
     *
     * @param context The Matrix Context object
     * @param strRSPId The id of the Requirement Specification object
     * @param slBusSelect The information to be selected on the project object
     * @return MapList containing map which has information about each parent project of the RSP
     * @throws MatrixException if operation fails
     * @since X3
     */
    protected MapList findParentProjects (Context context, String strRSPId, StringList slBusSelect) throws MatrixException {
        try {
            final String RELATIONSHIP_DATA_VAULTS = PropertyUtil.getSchemaProperty(context, "relationship_ProjectVaults");

            final String SELECT_IS_KIND_OF_PROJECT_SPACE = "type.kindof[" + ReqSchemaUtil.getProjectSpaceType(context) + "]";

            StringList slObjectSelects = new StringList(slBusSelect);
            if (!slObjectSelects.contains(DomainConstants.SELECT_ID)) {
                slObjectSelects.add(SELECT_ID);
            }
            if (!slObjectSelects.contains(SELECT_IS_KIND_OF_PROJECT_SPACE)) {
                slObjectSelects.add(SELECT_IS_KIND_OF_PROJECT_SPACE);
            }

            //
            // After PMC Controlled Folders implementation the folders can be connected via
            // Data Vaults, Sub Vaults or Linked Folders relationship, also RSP will be connected to
            // folder using Vaulted Documents Rev2 relationship.
            //

            String strRelationshipPattern =  RELATIONSHIP_DATA_VAULTS + "," + ReqSchemaUtil.getSubVaultsRelationship(context) + "," + ReqSchemaUtil.getLinkedFoldersRelationship(context) + "," + ReqSchemaUtil.getVaultedDocumentsRev2Relationship(context);
            String strTypePattern = ReqSchemaUtil.getProjectSpaceType(context) + "," + ReqSchemaUtil.getWorkspaceVaultType(context);

            DomainObject dmoRSP = DomainObject.newInstance(context, strRSPId);
            MapList mlParentHierarchy = dmoRSP.getRelatedObjects(context, strRelationshipPattern, strTypePattern, slObjectSelects, null, true, false, (short)0, null, null);

            // Filter this hierarchy for removing non-project objects
            MapList mlParentProjects = new MapList();
            for (Iterator itrParentObjects = mlParentHierarchy.iterator(); itrParentObjects.hasNext();) {
                Map mapParentObjInfo = (Map) itrParentObjects.next();

                boolean isProjectObj = "true".equalsIgnoreCase((String)mapParentObjInfo.get(SELECT_IS_KIND_OF_PROJECT_SPACE));
                if (!isProjectObj) {
                    continue;
                }

                // If the RSP is added to multiple folders under the same project then
                // this project will come more than once in the final result, so avoid duplicate entry.
                // now this comparison will fail if the same project is found at different levels in expansion
                // because the map now will have level key with different values. We actually do not need level
                // key here, so removing this from map will serve our purpose.
                mapParentObjInfo.remove(SELECT_LEVEL);
                if (!mlParentProjects.contains(mapParentObjInfo)) {
                    mlParentProjects.add(mapParentObjInfo);
                }
            }

            return mlParentProjects;
        }
        catch (Exception e) {
            throw new MatrixException(e);
        }
    }
    //End:R207:Bug 361383

//  Added:6-Feb-09:kyp:R207:RMT Bug 368668
    /**
     * Provides list of values for Name colummn of RMTProjectFolderList table
     *
     * @param context The Matrix Context object
     * @param args The packed argument sent by UI table framework
     * @return list of values for Name colummn of RMTProjectFolderList table
     * @throws Exception if operation fails
     * @since X3
     */
    public static Vector getProjectFolderNameOrTitle(Context context, String args[]) throws Exception
    {
        Vector vecColumnData = new Vector();

        try
        {
            Map programMap = (Map) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");

            final String SELECT_ATTRIBUTE_TITLE = "attribute[" + ReqSchemaUtil.getTitleAttribute(context) + "]";
            final String SELECT_KINDOF_CONTROLLED_FOLDER = "type.kindof[" + ReqSchemaUtil.getControlledFolderType(context) + "]";

            for (Iterator itrObjectList = objectList.iterator(); itrObjectList.hasNext();) {
                Map mapObjectInfo = (Map) itrObjectList.next();

                //This can be Project or Folder's name
                String strFolderName = (String)mapObjectInfo.get(SELECT_NAME);
                if(strFolderName == null){
                	String id = (String)mapObjectInfo.get(SELECT_ID);
                	DomainObject domObj = DomainObject.newInstance(context,id);
                	domObj.open(context);
                	strFolderName = domObj.getName();
                	domObj.close(context);
                }
                String strFolderTitle = (String)mapObjectInfo.get(SELECT_ATTRIBUTE_TITLE);
                if(strFolderTitle==null){
                	String id = (String)mapObjectInfo.get(SELECT_ID);
                	DomainObject domObj = DomainObject.newInstance(context,id);
                	domObj.open(context);
                	strFolderTitle = domObj.getAttributeValue(context, SELECT_ATTRIBUTE_TITLE);
                	domObj.close(context);
                }
                String strColumnValue = strFolderName;

                boolean isControlledFolder = "true".equalsIgnoreCase((String)mapObjectInfo.get(SELECT_KINDOF_CONTROLLED_FOLDER));
                if (isControlledFolder) {
                   strColumnValue = strFolderTitle;
                }

                vecColumnData.add(strColumnValue);
            }

        } //end of try
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        return vecColumnData;
    }
//  End:R207:RMT Bug 368668


    /**
     * return the RMB menu for SCE tree view checkboxes
     * @param context The Matrix Context object
     * @param args packed argument
     * @return menu Map for the RMB menu of SCE tree view checkboxes
     * @throws Exception
     */
	public HashMap getSCETreeRMBMenu(Context context,String[] args )throws Exception
	{
		HashMap hmpInput  = (HashMap)JPO.unpackArgs(args);
	
		HashMap requestMap = (HashMap) hmpInput.get("requestMap");
		String strlanguage = (String) requestMap.get("languageStr");
		
		HashMap paramMap = (HashMap) hmpInput.get("paramMap");
		String objectId = (String)paramMap.get("objectId");
		DomainObject domObject = DomainObject.newInstance(context, objectId);
	    String objectType           = domObject.getInfo(context, DomainConstants.SELECT_TYPE);
	
		//use rmbTableRowId to override objectId
		String tableRowId = (String)paramMap.get("rmbTableRowId");
		if(tableRowId != null){
			String[] tokens = tableRowId.split("[|]", -1);
			objectId = tokens[1];
		}
	
		String menuName = (String)paramMap.get("treeRMBMenu");
		String portalCmdName = (String)requestMap.get("portalCmdName");
		String portalName = (String)requestMap.get("portal");
		
		if(menuName == null || "null".equals(menuName))
		{
			// ++ KIE1 ZUD added for IR-395963-3DEXPERIENCER2017x
			if((domObject != null && domObject.isKindOf(context, ReqSchemaUtil.getRequirementSpecificationType(context)) && portalName != null && "RMTSpecificationRelatedObjetcs".equals(portalName) && portalCmdName != null && "RMTFullTraceabilityReport".equals(portalCmdName))
					|| (domObject != null && domObject.isKindOf(context, ReqSchemaUtil.getRequirementType(context)) && portalName != null && "RMTRelatedObjetcs".equals(portalName) && portalCmdName != null && "RMTFullTraceabilityReport".equals(portalCmdName)))
			{
				menuName = "RMTSpecificationRMBMenu";
			}
			else
			{
				menuName = "RMTSCETreeRMB";
			}
		}
	
		HashMap commandMap = UIToolbar.getToolbar(context, menuName, PersonUtil.getAssignments(context), objectId, requestMap, strlanguage);
		
		// ++ KIE1 ZUD added for IR-395963-3DEXPERIENCER2017x
		if("RMTSpecificationRMBMenu".equals(menuName))
		{
			fixCommandForRMB(commandMap, tableRowId);
			return commandMap;
		}
		// -- KIE1 ZUD added for IR-395963-3DEXPERIENCER2017x
		
		String menuToAppend = (String)paramMap.get("appendRMBMenu");
		
		if(menuToAppend == null)
		{
			menuToAppend = UIMenu.getRMBMenu(context,objectId);
		}
		
		if(menuToAppend != null && !menuToAppend.equals(""))
		{
			HashMap appendMap = getDynamicToolbar(context, menuToAppend, PersonUtil.getAssignments(context), objectId, requestMap, paramMap, strlanguage);
			
			MapList children = UIToolbar.getChildren(commandMap);
			MapList childrenToAdd = UIToolbar.getChildren(appendMap);
			if(children != null && childrenToAdd != null)
			{
				children.addAll(childrenToAdd);
			}
		}
		
		fixCommandForRMB(commandMap, tableRowId);

		return commandMap;
	
	}
	
	protected HashMap getDynamicToolbar(Context context, String toolbarName, Vector assignments, String objectId, HashMap requestMap, HashMap paramMap, String language) throws Exception
	{
		HashMap commandMap = UIToolbar.getToolbar(context, toolbarName, assignments, objectId, requestMap, language);
	    String strJPOParentMenu	=	UIToolbar.getSetting(commandMap,"Dynamic Command Program");
	    String strParentMethod	=	UIToolbar.getSetting(commandMap,"Dynamic Command Function");
	    //if the Menu has a setting Dynamic Command .Invoke the JPO
	    if (strJPOParentMenu != null && !"".equals(strJPOParentMenu))
        {
	        HashMap newCommandMap = (HashMap)commandMap.clone();
	        HashMap hmpJPOInput = new HashMap(); 
	        hmpJPOInput.put("paramMap",paramMap);
	        hmpJPOInput.put("commandMap",newCommandMap);
	        hmpJPOInput.put("requestMap",requestMap);
	        commandMap = (HashMap)JPO.invoke(context, strJPOParentMenu, null, strParentMethod,
                    JPO.packArgs(hmpJPOInput), HashMap.class);
	    }
		return commandMap;
	}

    /**
     * add tableRowId as the last parameter of javascript href
     *
     * @param commandMap
     *            the commandMap to fix
     * @param tableRowId rmbTableRowId
     * @throws Exception
     *             if the operation fails
     * @since RequirementCentral R2013
     */
    void fixCommandForRMB(Map commandMap, String tableRowId){
    	
    	if(commandMap != null){
    		if("command".equalsIgnoreCase((String)commandMap.get("type"))){
    			String name = (String)commandMap.get("name");
    			String href = (String)commandMap.get("href");
    			if(href != null && href.toLowerCase().trim().startsWith("javascript:")){
    				int start = href.indexOf("(");
    				int end = href.indexOf(")");
    				String seperator = href.substring(start + 1, end).trim().length() == 0 ? "" : ", ";
    				href = href.substring(0, end)  + seperator + "'" + tableRowId + "')";
    				commandMap.put("href", href);
    			}
    			
    		}else{
    			MapList children = (MapList)commandMap.get("Children");
    			if(children != null){
	    			for(int i = 0; i < children.size(); i++){
	    				fixCommandForRMB((Map)children.get(i), tableRowId);
	    			}
    			}
    		}
    	}
    	
    }
    
}
