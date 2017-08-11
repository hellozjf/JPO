/*
 *  emxDecisionBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * The <code>emxDecisionBase</code> class contains methods related to Decision admin type.
 * @author INFOSYS
 * @version RequirementsManagement V6R2008-2 - Copyright (c) 2004, MatrixOne, Inc.
 *
 */
public class emxDecisionBase_mxJPO extends emxDomainObject_mxJPO
{
    protected static final String STR_OBJECT_LIST = "objectList";
    protected static final String SYMB_OBJECT_ID  = "objectId";

    /**
     * Default Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2008-2
     * @grade 0
     */
	public emxDecisionBase_mxJPO (Context context, String[] args) throws Exception
    {
        super(context, args);
    }

    /**
     * Main entry point.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2008-2
     * @grade 0
     */
    public int mxMain(Context context, String[] args) throws Exception
    {
        if (!context.isConnected())
        {
            String strContentLabel = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Error.UnsupportedClient");
            throw new Exception(strContentLabel);
        }
        return 0;
    }

    /**
     * Get the list of all Decisions on the context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return bus ids of feature
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2008-2
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRelatedDecisions(Context context, String[] args) throws Exception
    {
        MapList relBusObjPageList = new MapList();
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
//Added:30-Apr-2010:di1:R210:PRG:Meeting Usability
        /* additional Decision information is required to display it in Meeting Summary table.
         * i.e. name,type,policy,description,owner and current state
         * */
        objectSelects.add(DomainConstants.SELECT_NAME);
        objectSelects.add(DomainConstants.SELECT_TYPE);
        objectSelects.add(DomainConstants.SELECT_POLICY);
        objectSelects.add(DomainConstants.SELECT_DESCRIPTION);
        objectSelects.add(DomainConstants.SELECT_OWNER);
        objectSelects.add(DomainConstants.SELECT_CURRENT);
//Addition End:30-Apr-2010:di1:R210:PRG:Meeting Usability

        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        relSelects.add(DomainConstants.SELECT_RELATIONSHIP_NAME);

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String strObjectId = (String) programMap.get("objectId");

        //Domain Object initialized with the object id.
        DomainObject doObj = DomainObject.newInstance(context,strObjectId);
        //setId(strObjectId);

        short sRecursionLevel = 1;
        String strType = DomainConstants.TYPE_DECISION;
        String strRelName = DomainConstants.RELATIONSHIP_DECISION + "," + DomainConstants.RELATIONSHIP_DECISION_APPLIES_TO;

        relBusObjPageList = doObj.getRelatedObjects(context, strRelName, strType, objectSelects, relSelects, true, false,
                sRecursionLevel, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);

        return relBusObjPageList;
    }

    /**
     * Get the list of all 'from' Requirement Object and rel ids by which requirement object are connected to requirement object linked to decision Object by rel2rel on the context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    paramMap  - a Map containing the paramMap which contains object ID
     * @return bus ids of 'from' Requirement objects and rel ids through which two Requirement objects connected.
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2008-2
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getWhereUsedRel(Context context, String[] args) throws Exception
    {
        MapList resultList = new MapList();
        // Unpack the arguments
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        // Get the objectId from the param map.
        String strObjectId = (String) programMap.get(SYMB_OBJECT_ID);


		//execute the mql command with super user & assign the result to a variable.
		String result = MqlUtil.mqlCommand(context, "print bus $1 select $2 $3 dump $4", strObjectId, "from.torel.from.id", "from.torel.id", "|");

		//make a list of 'from' side object ids & rel ids by separatinh them with '|'.
		StringList fromObjectRelIdList = FrameworkUtil.split(result,"|");

		if(fromObjectRelIdList != null)
		{
			//get the 'from' side object count.Since the list contains object ids & rel ids so to get only object ids count divided by two.
			int fromObjectCount = (fromObjectRelIdList.size())/2;

			//iterate the loop on the basis of from side object count value
			for(int itr=0; itr < fromObjectCount; itr++)
			{
				java.util.Map fromMap = new HashMap();
				//put the 'from' object ids value in the map with "id" as key.
				fromMap.put(DomainConstants.SELECT_ID,fromObjectRelIdList.elementAt(itr));
				//put the rel ids value in the map with "id[connection]" as key.
                /* Above mql query returns the connection id of req-req. We need to get the connection id b/w decision
                 * and Derived Req or Sub Req. We need to pass that id over id[connection] so that when the user performs
                 * remove operations from where used channel it will remove only that relationship. Not b/w the req-req.
                 */
                String connectionId = (String) fromObjectRelIdList.elementAt(fromObjectCount + itr);
                if(connectionId != null && !"".equals(connectionId))
                {
                    result = MqlUtil.mqlCommand(context,"print connection $1 select $2 $3 dump $4", connectionId, "tomid.from.id", "tomid.id", "|");
                    StringList toRelObjectIdList = FrameworkUtil.split(result,"|");
                    if(toRelObjectIdList != null)
                    {
                        int toObjectCount = (toRelObjectIdList.size())/2;
                        //String id_connection = "";
                        for(int j=0; j<toObjectCount; j++)
                        {
                            String toObjectId = (String) toRelObjectIdList.elementAt(j);
                            if(strObjectId.equalsIgnoreCase(toObjectId))
                            {
                                connectionId = (String) toRelObjectIdList.elementAt(toObjectCount + j);
                                break;
                            }
                        }

                    }
                }
                fromMap.put(DomainConstants.SELECT_RELATIONSHIP_ID,connectionId);
				//add the map containing key-value pairs to the resultList maplist.
				resultList.add(fromMap);
			} //end of iteration
		}

        return resultList;
    }

	/**
	* Method call to get all the decisions in the data base.
	*
	* @param context the eMatrix <code>Context</code> object
	* @param args  Holds the parameters passed from the calling method
	* @return Object - MapList containing the id of all Decision objects
	* @throws Exception if the operation fails
	* @since RequirementsManagement V6R2008-2
	* @grade 0
	*/
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllDecisions (Context context, String[] args)
        throws Exception
    {
        // forming the where clause
        String strWhereExpression = null;
        //Calls the protected method to retrieve the data
        MapList mapBusIds = getDecisions(context, strWhereExpression,null);
        return(mapBusIds);
    }

	/**
	* Get the list of all owned decisions.
	*
	* @param context the eMatrix <code>Context</code> object
	* @param args  Holds the parameters passed from the calling method
	* @return MapList - MapList containing the id of all owned Decision objects.
	* @throws Exception if the operation fails
	* @since RequirementsManagement V6R2008-2
	* @grade 0
	*/
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getOwnedDecisions (Context context, String[] args)
        throws Exception
    {
        // forming the Owner Pattern clause
        String strOwnerCondition = context.getUser();
        //Calls the protected method to retrieve the data
        MapList mapBusIds = getDecisions(context, null,strOwnerCondition);
        return(mapBusIds);
    }

	/**
	* Get the list of Decisions.
	*
	* @param context the eMatrix <code>Context</code> object
	* @param strWhereCondition - String value containing the condition based on which results are to be filtered.
	* @param strOwnerCondition - String value containing the owner condition based on which results are to be filtered.
	* @return MapList - MapList containing the id of Decision objects based on whereCondition .
	* @throws Exception if the operation fails
	* @since RequirementsManagement V6R2008-2
	* @grade 0
	*/
    protected MapList getDecisions (Context context, String strWhereCondition, String strOwnerCondition)
        throws Exception
    {
        //String list initialized to retrieve data for the Requirements
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        String strType = DomainConstants.TYPE_DECISION;

        //The findobjects method is invoked to get the list of products
        MapList mapBusIds = findObjects(context, strType, null,null,strOwnerCondition,DomainConstants.QUERY_WILDCARD,strWhereCondition,true, objectSelects);
        return(mapBusIds);
    }

	/**
	* Get the To Object on the owning relationship of a Decision
	*
	* @param context the eMatrix <code>Context</code> object
	* @param args  Holds the parameters passed from the calling method
	* @return List - List containing the links to the Objects
	* @throws Exception if the operation fails
	* @since RequirementsManagement V6R2008-2
	* @grade 0
	*/
	public List getRelToObject(Context context, String[] args) throws Exception
    {
		// Unpack the arguments
        Map programMap = (HashMap) JPO.unpackArgs(args);

        // Get the objectList from the param map.
        List objectList = (MapList) programMap.get(STR_OBJECT_LIST);
        Iterator objectListItr = objectList.iterator();

		StringList lstRelSelects = new StringList();
		lstRelSelects.add(DomainRelationship.SELECT_TO_ID);

        String strRelId = DomainConstants.EMPTY_STRING;

        Map objectMap = new HashMap();
        List lstReturn = new StringList();

        // Iterate through each element in the objectlist
        while (objectListItr.hasNext())
        {
            objectMap = (Map) objectListItr.next();
            strRelId = (String) objectMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            if(strRelId != null && strRelId.length() > 0)
                strRelId = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump $3", strRelId, "torel.id", "|");

			if (strRelId != null && strRelId.length() > 0)
			{
				DomainRelationship domRel = new DomainRelationship(strRelId);

				Hashtable ht = domRel.getRelationshipData(context, lstRelSelects);
				StringList toObjIdList = (StringList)ht.get(DomainRelationship.SELECT_TO_ID);
				if (toObjIdList != null && toObjIdList.size() > 0)
				{
					String toObjId = (String)toObjIdList.get(0);

					DomainObject domObj = new DomainObject(toObjId);
					String toObjName = domObj.getName(context);
					String toObjType = domObj.getType(context);

					String attIcon = UINavigatorUtil.getTypeIconProperty(context, toObjType);
					String aHref = "<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=common&parentOID=null&jsTreeID=null&suiteKey=Framework&objectId=" + toObjId + "', '700', '600', 'false', 'popup', '')\" class=\"object\">";
					String attHtml = "<table border=\"0\">";
					attHtml = attHtml + "<tr>";
					attHtml = attHtml + "<td rmb=\"\" valign=\"top\" rmbID=" + XSSUtil.encodeForHTMLAttribute(context, toObjId) + ">" + aHref + "<img src='../common/images/" +attIcon+ "' border=0></a></td>";
					attHtml = attHtml + "<td rmb=\"\" rmbID=" + XSSUtil.encodeForHTMLAttribute(context, toObjId) + ">" + aHref + XSSUtil.encodeForHTML(context, toObjName) + "</a></td>";
					attHtml = attHtml + "</tr>";
					attHtml = attHtml + "</table>";

					lstReturn.add(attHtml);
				}
			}
        }
        return lstReturn;
	}

	/**
	* Get all the objects the Decision is used on
	*
	* @param context the eMatrix <code>Context</code> object
	* @param args  Holds the parameters passed from the calling method
	* @return MapList - MapList containing the id to the Objects
	* @throws Exception if the operation fails
	* @since Components R207
	* @grade 0
	*/
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getWhereUsed(Context context, String[] args) throws Exception
    {
        MapList relBusObjPageList = new MapList();
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String strObjectId = (String) programMap.get("objectId");

        //Domain Object initialized with the object id.
        setId(strObjectId);

        short sRecursionLevel = 1;
        String strRelName = DomainConstants.RELATIONSHIP_DECISION;

        relBusObjPageList = getRelatedObjects(context, strRelName, "*", objectSelects, relSelects, false, true,
                sRecursionLevel, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);

        return relBusObjPageList;
    }

	/**
	* Get all the objects the Decision applies to
	*
	* @param context the eMatrix <code>Context</code> object
	* @param args  Holds the parameters passed from the calling method
	* @return MapList - MapList containing the id to the Objects
	* @throws Exception if the operation fails
	* @since Components R207
	* @grade 0
	*/
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAppliesTo(Context context, String[] args) throws Exception
    {
        MapList relBusObjPageList = new MapList();
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String strObjectId = (String) programMap.get("objectId");

        //Domain Object initialized with the object id.
        setId(strObjectId);

        short sRecursionLevel = 1;
        String strType = "*";
        String strRelName = DomainConstants.RELATIONSHIP_DECISION_APPLIES_TO;

        relBusObjPageList = getRelatedObjects(context, strRelName, strType, objectSelects, relSelects, false, true,
                sRecursionLevel, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);

        return relBusObjPageList;
    }


    /**
	    * To obtain the list of Object IDs to be excluded from the search for Add Existing Decision
	    *
	    * @param context- the eMatrix <code>Context</code> object
	    * @param args- holds the HashMap containing the following arguments
	    * @return  StringList- consisting of the object ids to be excluded from the Search Results
	    * @throws Exception if the operation fails
	    */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeDecisions(Context context, String[] args)
	throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		String strObjectId = (String)programMap.get("objectId");

		StringList excludeList = new StringList();

		DomainObject domObject = new DomainObject(strObjectId);
		MapList ancestors = domObject.getRelatedObjects(context,
			DomainConstants.RELATIONSHIP_DECISION,          // Expand this relationship
			DomainConstants.QUERY_WILDCARD,                // retrieving all object types
			true, false, 1,                                // in the from direction, 1 level
			new StringList(DomainConstants.SELECT_ID),     // just get the objectid att
			DomainConstants.EMPTY_STRINGLIST,              // don't bother with rel atts
			DomainConstants.EMPTY_STRING,                  // empty object where clause
			DomainConstants.EMPTY_STRING,                  // empty rel where clause
			null,                                          // don't filter out any relationships
			null,                                      	   // don't filter the return type(s)
			null);

		for(int ii = 0; ii < ancestors.size(); ii++)
		{
			Map tempMap = (Map)ancestors.get(ii);
			excludeList.add((String)tempMap.get(DomainConstants.SELECT_ID));
		}

		return excludeList;
	}

	/**
	* To obtain the list of Object IDs to be excluded from the search for Add Existing Where Used Decision
	*
	* @param context- the eMatrix <code>Context</code> object
	* @param args- holds the HashMap containing the following arguments
	* @return  StringList- consisting of the object ids to be excluded from the Search Results
	* @throws Exception if the operation fails
	*/
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeDecisionWhereUsed(Context context, String[] args)
	throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		String strObjectId = (String)programMap.get("objectId");

		StringList excludeList = new StringList();

		DomainObject domObject = new DomainObject(strObjectId);
		MapList ancestors = domObject.getRelatedObjects(context,
			DomainConstants.RELATIONSHIP_DECISION,         // Expand this relationship
			DomainConstants.QUERY_WILDCARD,                // retrieving all object types
			false, true, 1,                                // in the to direction, 1 level
			new StringList(DomainConstants.SELECT_ID),     // just get the objectid att
			DomainConstants.EMPTY_STRINGLIST,              // don't bother with rel atts
			DomainConstants.EMPTY_STRING,                  // empty object where clause
			DomainConstants.EMPTY_STRING,                  // empty rel where clause
			null,                                          // don't filter out any relationships
			null,                                      	   // don't filter the return type(s)
			null);

		for(int ii = 0; ii < ancestors.size(); ii++)
		{
			Map tempMap = (Map)ancestors.get(ii);
			excludeList.add((String)tempMap.get(DomainConstants.SELECT_ID));
		}

		return excludeList;
	}

  	/**
	* To obtain the list of Object IDs to be excluded from the search for Add Existing Decision Applies To
	*
	* @param context- the eMatrix <code>Context</code> object
	* @param args- holds the HashMap containing the following arguments
	* @return  StringList- consisting of the object ids to be excluded from the Search Results
	* @throws Exception if the operation fails
	*/
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeDecisionAppliesTo(Context context, String[] args)
	throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		String strObjectId = (String)programMap.get("objectId");
		StringList slRel = new StringList();

		StringList excludeList = new StringList();

		DomainObject domObject = new DomainObject(strObjectId);
		StringList slOBjects = new StringList(DomainConstants.SELECT_ID);
		MapList ancestors = domObject.getRelatedObjects(context,
			DomainConstants.RELATIONSHIP_DECISION_APPLIES_TO,          // Expand this relationship
			DomainConstants.QUERY_WILDCARD,                // retrieving all object types
			false, true, 1,                                // in the to direction, 1 level
			slOBjects,     // just get the objectid att
			slRel,              // don't bother with rel atts
			DomainConstants.EMPTY_STRING,                  // empty object where clause
			DomainConstants.EMPTY_STRING,                  // empty rel where clause
			null,                                          // don't filter out any relationships
			null,                                      	   // don't filter the return type(s)
			null);

		for(int ii = 0; ii < ancestors.size(); ii++)
		{
			Map tempMap = (Map)ancestors.get(ii);
			excludeList.add((String)tempMap.get(DomainConstants.SELECT_ID));
		}

		return excludeList;
	}

    /**
     * Gives the releas dates of Desicion, added for the bug 372465
     * @param context
     * @param args
     * @return Vector with release dates of decisions
     * @throws Exception
     */
    public Vector getReleaseDate(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList)programMap.get("objectList");
        Vector releaseDates = new Vector();

        Iterator objListItr = objectList.iterator();
        while(objListItr.hasNext()) {
            Map map = (Map)objListItr.next();
            String objectId = (String)map.get(DomainConstants.SELECT_ID);
            setObjectId(objectId);

            StringList historyData = getHistory(context);
            String releaseDate = "";
            for(int i=historyData.size()-1;i>=0;i--){
                String historyRecord = ((String)historyData.elementAt(i)).trim();
                if( UIUtil.isNotNullAndNotEmpty(historyRecord) && historyRecord.startsWith("promote") && historyRecord.endsWith(STATE_PART_RELEASE)){
                    releaseDate = historyRecord.substring(historyRecord.indexOf("time:")+6, historyRecord.indexOf("state:")-2).trim();
                    break;
                }
            }
            releaseDates.addElement(releaseDate);
        }
        return releaseDates;
    }

    /**
    * Get the To Object on the owning relationship of a Decision
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args  Holds the parameters passed from the calling method
    * @return List - List containing the links to the Objects
    * @throws Exception if the operation fails
    * @since RequirementsManagement V6R2010x
    * @grade 0
    */
    public List getRelationshipType(Context context, String[] args) throws Exception
    {
        // Unpack the arguments
        Map programMap = (HashMap) JPO.unpackArgs(args);

        // Get the objectList from the param map.
        List objectList = (MapList) programMap.get(STR_OBJECT_LIST);
        Iterator objectListItr = objectList.iterator();

        StringList lstRelSelects = new StringList();
        lstRelSelects.add(DomainRelationship.SELECT_TO_ID);

        String strRelId = DomainConstants.EMPTY_STRING;

        Map objectMap = new HashMap();
        List lstReturn = new StringList();

        // Iterate through each element in the objectlist
        while (objectListItr.hasNext())
        {
            objectMap = (Map) objectListItr.next();
            strRelId = (String) objectMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            if(strRelId != null && strRelId.length() > 0)
                strRelId = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump $3", strRelId, "torel.name", "|");
            lstReturn.add(strRelId);

        }
        return lstReturn;
    }
    
    
    /**
     * Gives the context of Desicion
     * @param context
     * @param args
     * @return Vector with context of decisions
     * @throws Exception
     */
    public Vector getDecisionContext(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList)programMap.get("objectList");
        Vector decisionContext = new Vector();
				
		String strGeenralDecision="";
		String strChangeTrackingDecision="";
		strGeenralDecision = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Decision.GeneralDecision");
		strChangeTrackingDecision = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Decision.ChangeTrackingDecision");

        Iterator objListItr = objectList.iterator();
        while(objListItr.hasNext()) {
            Map map = (Map)objListItr.next();
            String strRelName = (String)map.get(DomainConstants.SELECT_RELATIONSHIP_NAME);
            String strName="";
            if(DomainConstants.RELATIONSHIP_DECISION.equals(strRelName)){
            	decisionContext.addElement(strGeenralDecision);
            }else if(DomainConstants.RELATIONSHIP_DECISION_APPLIES_TO.equals(strRelName)){
            	decisionContext.addElement(strChangeTrackingDecision);
            }
        }
        return decisionContext;
    }

}
