/*   emxCommonAccessRequestBase
**   Copyright (c) 2006-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**   This JPO contains the implementation of emxAccessRequestBase
*/
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
import java.util.Locale;
 import java.util.Map;
 import java.util.Vector;

 import matrix.db.BusinessObject;
 import matrix.db.Context;
 import matrix.db.JPO;
 import matrix.util.StringList;

 import com.matrixone.apps.common.CommonDocument;
 import com.matrixone.apps.common.Issue;
 import com.matrixone.apps.domain.DomainConstants;
 import com.matrixone.apps.domain.DomainObject;
 import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
 import com.matrixone.apps.domain.util.FrameworkException;
 import com.matrixone.apps.domain.util.FrameworkProperties;
 import com.matrixone.apps.domain.util.FrameworkUtil;
 import com.matrixone.apps.domain.util.MapList;
 import com.matrixone.apps.domain.util.MessageUtil;
 import com.matrixone.apps.domain.util.MqlUtil;
 import com.matrixone.apps.domain.util.PersonUtil;
 import com.matrixone.apps.domain.util.PropertyUtil;
 import com.matrixone.apps.domain.util.RegistrationUtil;
import com.matrixone.apps.domain.util.XSSUtil;
 import com.matrixone.apps.domain.util.eMatrixDateFormat;
 import com.matrixone.apps.domain.util.i18nNow;
 import com.matrixone.apps.framework.ui.UINavigatorUtil;

public class emxCommonAccessRequestBase_mxJPO extends emxDomainObject_mxJPO
{
	/**
	* Constructor.
	* @param context the eMatrix <code>Context</code> object
	* @param args holds no arguments
	* @throws Exception if the operation fails
	* @since Common 11.0.0.0
	* @grade 0
	*/
	public emxCommonAccessRequestBase_mxJPO (Context context, String[] args) throws Exception
	{
		super(context, args);
	}

	public static final String TYPE_PERSON = PropertyUtil.getSchemaProperty("type_Person");
	public static final String RELATIONSHIP_REQUESTED_DOCUMENT =  PropertyUtil.getSchemaProperty("relationship_RequestedDocument");
	public static final String RELATIONSHIP_REQUESTED_ASSIGNEE = PropertyUtil.getSchemaProperty("relationship_RequestedAssignee");
	public static final String POLICY_DOWNLOAD_ACCESS_REQUEST = PropertyUtil.getSchemaProperty("policy_AccessRequest");
	public static final String TYPE_DOWNLOAD_ACCESS_REQUEST = PropertyUtil.getSchemaProperty("type_AccessRequest");
	public static final String ATTRIBUTE_GRANT_EXPIRATION_DATE = PropertyUtil.getSchemaProperty("attribute_GrantExpirationDate");
	public static final String ATTRIBUTE_INCLUDE_SELF = PropertyUtil.getSchemaProperty("attribute_IncludeSelf");
	public static final String ATTRIBUTE_TITLE = PropertyUtil.getSchemaProperty("attribute_Title");
	public static final String PERSON_REQUEST_ACCESS_GRANTOR = PropertyUtil.getSchemaProperty("person_RequestAccessGrantor");

	public static final String SELECT_ATTRIBUTE_TITLE = "attribute["+ATTRIBUTE_TITLE+"]";

	/** A string constant with the value emxComponentsStringResource. */
	public static final String RESOURCE_BUNDLE_COMPONENTS_STR = "emxComponentsStringResource";

	/** A string constant with the value emxComponents. */
	public static final String RESOURCE_BUNDLE_COMPONENTS = "emxComponents";

	/** A string constant with the value state_Submitted. */
	public static final String SYMB_SUBMITTED = "state_Submitted";
	/** A string constant with the value state_Review. */
	public static final String SYMB_REVIEW = "state_Review";
	/** A string constant with the value state_Expired. */
	public static final String SYMB_EXPIRED = "state_Expired";
	/** A string constant with the value state_Approved. */
	public static final String SYMB_APPROVED = "state_Approved";

	public static final String LOCK = "lock";
	public static final String VERSION = "version";
	public static final String ACTIONS = "actions";
	public static final String LAUNCH = "launch";



	/**
	* This method used to show all connected persons with access request object.
	* @param context the eMatrix <code>Context</code> object
	* @param args holds objectId as first argument
	* @return Vector containing Person Names
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public Vector showRequestedAssignees( Context context, String[] args ) throws Exception
	{
		HashMap programMap = ( HashMap ) JPO.unpackArgs( args );
		MapList objectList = ( MapList )programMap.get( "objectList" );

		int iObjectListSize = objectList.size();
		Vector namesVector = new Vector( iObjectListSize );

		/**
		* Selectables and Multi value list
		**/
		StringList strList = new StringList( 2 );
		strList.addElement( "to[" + RELATIONSHIP_REQUESTED_ASSIGNEE + "].from.attribute[" + DomainConstants.ATTRIBUTE_FIRST_NAME + "]" );
		strList.addElement( "to[" + RELATIONSHIP_REQUESTED_ASSIGNEE + "].from.attribute[" + DomainConstants.ATTRIBUTE_LAST_NAME + "]" );

		DomainConstants.MULTI_VALUE_LIST.add( "to[" + RELATIONSHIP_REQUESTED_ASSIGNEE + "].from.attribute[" + DomainConstants.ATTRIBUTE_FIRST_NAME + "]" );
		DomainConstants.MULTI_VALUE_LIST.add( "to[" + RELATIONSHIP_REQUESTED_ASSIGNEE + "].from.attribute[" + DomainConstants.ATTRIBUTE_LAST_NAME + "]" );

		String objIdArray[] = new String[ iObjectListSize ];

		//Get the array of Object Ids to be paased into the methods
		for ( int i = 0; i < objectList.size(); i++ )
		{
			Map objMap = (Map)objectList.get( i );
			objIdArray[ i ]  = (String)objMap.get( DomainConstants.SELECT_ID );
		}

		// All Access Request Assignee Information
		MapList reqMapList = DomainObject.getInfo( context, objIdArray, strList );

		// Assignees concatenation
		for ( int i = 0;i<reqMapList.size() ;i++ )
		{
			StringBuffer sbFullName = new StringBuffer();
			Map assigneeMap = ( Map ) reqMapList.get( i );
			if ( assigneeMap.size() == 0)
			{
				namesVector.add( "" );
				continue;
			}

			Object objFistName = assigneeMap.get( "to[" + RELATIONSHIP_REQUESTED_ASSIGNEE + "].from.attribute[" + DomainConstants.ATTRIBUTE_FIRST_NAME + "]" );
			Object objLastName = assigneeMap.get( "to[" + RELATIONSHIP_REQUESTED_ASSIGNEE + "].from.attribute[" + DomainConstants.ATTRIBUTE_LAST_NAME + "]" );

			if ( objFistName instanceof StringList)
			{
				StringList slFirstName = ( StringList ) objFistName;
				StringList slLastName = ( StringList ) objLastName;
				for (int j = 0; j<slFirstName.size(); j++ )
				{
				sbFullName.append( ( String )slLastName.get( j ) + ", " + ( String ) slFirstName.get( j ) + "<br>" );
				}
			}
			else
			{
				sbFullName.append( ( String ) objFistName + ", " + ( String ) objLastName );
			}
			namesVector.add( sbFullName.toString() );
		}

		synchronized(this)
		{
			DomainConstants.MULTI_VALUE_LIST.remove("to[" + RELATIONSHIP_REQUESTED_ASSIGNEE + "].from.attribute[" + DomainConstants.ATTRIBUTE_FIRST_NAME + "]");
			DomainConstants.MULTI_VALUE_LIST.remove("to[" + RELATIONSHIP_REQUESTED_ASSIGNEE + "].from.attribute[" + DomainConstants.ATTRIBUTE_LAST_NAME + "]");
		}

		return namesVector;
	}

	/**
	* This method used to display all connected documents with access request object.
	* @param context the eMatrix <code>Context</code> object
	* @param args holds objectId as first argument
	* @return Vector containing Person Names
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public Vector showRequestedDocuments( Context context, String[] args ) throws Exception
	{
		HashMap programMap = ( HashMap ) JPO.unpackArgs( args );
		HashMap paramList = ( HashMap ) programMap.get( "paramList" );
		MapList objectList = ( MapList )programMap.get( "objectList" );

		/**
		* Printer Friendly Check
		**/
		boolean isprinterFriendly = false;
		if( paramList.get( "reportFormat" ) != null )
		{
			isprinterFriendly = true;
		}

		int iObjectListSize = objectList.size();
		Vector namesVector = new Vector( iObjectListSize );

		/**
		* Selectables and Multi value list
		**/
		StringList strList = new StringList(3);
		strList.addElement( "from[" + RELATIONSHIP_REQUESTED_DOCUMENT + "].to.id" );
		strList.addElement( "from[" + RELATIONSHIP_REQUESTED_DOCUMENT + "].to.name" );
		strList.addElement( "from[" + RELATIONSHIP_REQUESTED_DOCUMENT + "].to.type" );

		DomainConstants.MULTI_VALUE_LIST.add( "from[" + RELATIONSHIP_REQUESTED_DOCUMENT + "].to.id" );
		DomainConstants.MULTI_VALUE_LIST.add( "from[" + RELATIONSHIP_REQUESTED_DOCUMENT + "].to.name" );
		DomainConstants.MULTI_VALUE_LIST.add( "from[" + RELATIONSHIP_REQUESTED_DOCUMENT + "].to.type" );

		String objIdArray[] = new String[ iObjectListSize ];
		//Get the array of Object Ids to be paased into the methods
		for (int i = 0; i < objectList.size(); i++)
		{
			Map objMap = ( Map )objectList.get( i );
			objIdArray[ i ]  = ( String )objMap.get( DomainConstants.SELECT_ID );
		}

		// All Access Request Documentation Information
		ContextUtil.pushContext(context);
		MapList reqMapList = null;
		try
		{
			reqMapList = DomainObject.getInfo( context, objIdArray, strList );
		}
		finally
		{
			ContextUtil.popContext(context);
		}


		RegistrationUtil regUtil =  new RegistrationUtil();

		//  documents concatenation
		if(reqMapList!=null)
		{
			int reqMapListLength = reqMapList.size();
			for ( int i = 0;i<reqMapListLength ;i++ )
			{
				StringBuffer sbNames = new StringBuffer();
				Map docMap = ( Map ) reqMapList.get( i );

				if ( docMap.size() == 0)
				{
					namesVector.add("");
					continue;
				}

				Object objId = docMap.get( "from[" + RELATIONSHIP_REQUESTED_DOCUMENT + "].to.id" );
				Object objName = docMap.get( "from[" + RELATIONSHIP_REQUESTED_DOCUMENT + "].to.name" );
				Object objType = docMap.get( "from[" + RELATIONSHIP_REQUESTED_DOCUMENT + "].to.type" );

				if ( objId instanceof StringList)
				{
					StringList slId = ( StringList ) objId;
					StringList slName = ( StringList ) objName;
					StringList slType= ( StringList ) objType;

					HashMap objectAccessMap = getAccessMap(context, (String[])slId.toArray(new String[slId.size()]));

					for (int j = 0; j<slId.size(); j++ )
					{
						if( isprinterFriendly || !hasReadShowAccess(objectAccessMap, (String)slId.get(j)))
						{
							sbNames.append( ( String ) slName.get( j ) );
							if("CSV".equals(paramList.get( "reportFormat" )))
								sbNames.append( ";" );
							else
								sbNames.append( "<br>" );
						}
						else
						{
							//changes for LBC FT to SB
							String href="../common/emxTree.jsp?emxSuiteDirectory=components&amp;treeMenu=" + (String) regUtil.getRegisteredList(context, (String) slType.get(j)) + "&amp;objectId=" + (String)slId.get(j);
							String link="<b><a href=\"javascript:emxTableColumnLinkClick('"+href+"',600,400,'false','popup')\" >";
							sbNames.append(link);
							sbNames.append(XSSUtil.encodeForHTML(context, (String)slName.get(j)));
							sbNames.append( "</a></b>");
						}
					}
				}
				else
				{

					String[] arObjId = {(String)objId};


					HashMap objectAccessMap = getAccessMap(context, arObjId);

					if( isprinterFriendly || !hasReadShowAccess(objectAccessMap, (String)objId))
					{
						sbNames.append( XSSUtil.encodeForHTML(context, (String)objName) );
						sbNames.append( "<br>" );
					}
					else
					{
						//changes for LBC FT to SB
						String href="../common/emxTree.jsp?emxSuiteDirectory=components&amp;treeMenu=" + (String) regUtil.getRegisteredList(context, (String) objType) + "&amp;objectId=" + (String)objId;
						String link="<b><a href=\"javascript:emxTableColumnLinkClick('"+href+"',600,400,'false','popup')\" >";
						sbNames.append(link);
						sbNames.append(XSSUtil.encodeForHTML(context, (String)objName));
						sbNames.append( "</a></b>");
					}
				}
				namesVector.add( sbNames.toString());
			}
		}

		synchronized(this)
		{
			DomainConstants.MULTI_VALUE_LIST.remove( "from[" + RELATIONSHIP_REQUESTED_DOCUMENT + "].to.id" );
			DomainConstants.MULTI_VALUE_LIST.remove( "from[" + RELATIONSHIP_REQUESTED_DOCUMENT + "].to.name" );
			DomainConstants.MULTI_VALUE_LIST.remove( "from[" + RELATIONSHIP_REQUESTED_DOCUMENT + "].to.type" );
		}
		return namesVector;
	}

	/**
	* This method used to display all connected persons with access request object.
	* @param context the eMatrix <code>Context</code> object
	* @param args holds objectId as first argument
	* @return Vector containing Person Names
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public String displayRequestedAssignees( Context context, String[] args )throws Exception
	{
		HashMap programMap = ( HashMap ) JPO.unpackArgs( args );
		HashMap requestMap = ( HashMap ) programMap.get( "requestMap" );
		return getAssigneeName( context, ( String ) requestMap.get( "objectId" ) );
	}

	/**
	* This method used to display all connected documents with access request object.
	* @param context the eMatrix <code>Context</code> object
	* @param args holds objectId as first argument
	* @return Vector containing Person Names
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public String displayRequestedDocuments( Context context, String[] args )throws Exception
	{
		HashMap programMap = ( HashMap ) JPO.unpackArgs( args );
		HashMap requestMap = ( HashMap ) programMap.get( "requestMap" );
		return getDocumentName( context, ( String ) requestMap.get( "objectId" ) );
	}

	/**
	* Get the list of Assignees connected to a Access Request.
	* @param context the eMatrix <code>Context</code> object
	* @param args holds the following input arguments:
	*        0 - MapList objectList
	* @return MapList containing Assignee
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public MapList getAssignees( Context context, String objectId )  throws Exception
	{
		//object for holding selectables for object
		StringList objSelects = new StringList( 2 );
		objSelects.add( DomainConstants.SELECT_ID );
		objSelects.add( DomainConstants.SELECT_NAME );

		DomainObject domObj = new DomainObject( objectId );
		return domObj.getRelatedObjects(  context,
														RELATIONSHIP_REQUESTED_ASSIGNEE,
														TYPE_PERSON,
														objSelects,
														null,
														true,
														false,
														(short)1,
														null,
														null);
	}

	/**
	* Get the list of Documents connected to a Access Request.
	* @param context the eMatrix <code>Context</code> object
	* @param args holds the following input arguments:
	*        0 - MapList objectList
	* @return MapList containing Assignee
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public MapList getDocuments(Context context, String objectId)  throws Exception
	{
		//object for holding selectables for object
		StringList objSelects = new StringList( 2 );
		objSelects.add( DomainConstants.SELECT_ID );
		objSelects.add( DomainConstants.SELECT_NAME );
		ContextUtil.pushContext(context);
		try
		{
			DomainObject domObj = new DomainObject( objectId );
			return domObj.getRelatedObjects(  context,
															RELATIONSHIP_REQUESTED_DOCUMENT,
															DomainConstants.QUERY_WILDCARD,
															objSelects,
															null,
															false,
															true,
															(short)1,
															null,
															null);
		}
		finally
		{
			ContextUtil.popContext(context);
		}
	}


	/**
	* Get the list of Assignees connected to a Access Request.
	* @param context the eMatrix <code>Context</code> object
	* @param args holds the following input arguments:
	*        0 - MapList objectList
	* @return MapList containing Assignee
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getRequestedAssignees( Context context, String[] args )  throws Exception
	{
		HashMap programMap = ( HashMap) JPO.unpackArgs( args );
		return getAssignees( context, ( String )programMap.get( "objectId" ) );
	}

	/**
	* Get the list of Assignees connected to a Access Request.
	* @param context the eMatrix <code>Context</code> object
	* @param args holds the following input arguments:
	*        0 - MapList objectList
	* @return MapList containing Assignee
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getRequestedDocuments( Context context, String[] args ) throws Exception
	{
		HashMap programMap = ( HashMap ) JPO.unpackArgs( args );
		return getDocuments( context, ( String )programMap.get( "objectId" ) );
	}

	/**
	* This method returns Last, First Names of connected persons with access request object.
	* @param context the eMatrix <code>Context</code> object
	* @argument is objectId
	* @return String containing Person Names
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public String getAssigneeName( Context context, String objectId ) throws Exception
	{
		StringList strList = new StringList(2);
		strList.addElement( "to[" + RELATIONSHIP_REQUESTED_ASSIGNEE + "].from.attribute[" + DomainConstants.ATTRIBUTE_FIRST_NAME + "]"  );
		strList.addElement( "to[" + RELATIONSHIP_REQUESTED_ASSIGNEE + "].from.attribute[" + DomainConstants.ATTRIBUTE_LAST_NAME + "]"  );

		// setting object id
		setId(objectId);

		// Assignee Info List
		Map resultList = getInfo(context, strList, strList);
		StringBuffer strBuffer = new StringBuffer();

		if ( resultList.size() >0 )
		{
			StringList slFirstName = (StringList)resultList.get("to[" + RELATIONSHIP_REQUESTED_ASSIGNEE + "].from.attribute[" + DomainConstants.ATTRIBUTE_FIRST_NAME + "]" );
			StringList slLastName = (StringList)resultList.get("to[" + RELATIONSHIP_REQUESTED_ASSIGNEE + "].from.attribute[" + DomainConstants.ATTRIBUTE_LAST_NAME + "]" );
			for(int i=0; i<slFirstName.size(); i++)
			{
				strBuffer.append(slLastName.get(i)+", "+slFirstName.get(i)+"; ");
			}

		}
		return strBuffer.toString();
	}

/**
* This method returns Document Names of connected Documents with access request object.
* @param context the eMatrix <code>Context</code> object
* @argument is objectId
* @return String containing Document Names
* @throws Exception if the operation fails
* @since Common 11.0
*/
public String getDocumentName( Context context, String objectId ) throws Exception
{
	StringList strList = new StringList(1);
	strList.addElement( "from[" + RELATIONSHIP_REQUESTED_DOCUMENT + "].to.name" );

	// setting object id
	setId(objectId);

	// Document Info List
	Map resultList = new HashMap();
	ContextUtil.pushContext(context);
	try
	{
		resultList = getInfo( context, strList, strList );
	}
	finally
	{
		ContextUtil.popContext(context);
	}
	StringBuffer strBuffer = new StringBuffer();

	if ( resultList.size() >0 )
	{
		StringList slDocumentName = (StringList)resultList.get( "from[" + RELATIONSHIP_REQUESTED_DOCUMENT + "].to.name" );
		for(int i=0; i<slDocumentName.size(); i++)
		{
			strBuffer.append( ( String ) slDocumentName.get( i ) );
			strBuffer.append( ";" );
		}
		strBuffer.setLength(strBuffer.length()-1);
	}
	return strBuffer.toString();
}

	/**
	* This Method returns the MapList of  Access Requests based on Filter options.This method
	* will be called in the emxTable.jsp. This value will be passed as Link Href of menu's LCAccessRequestsMyDesk and LCLibrariesMyDesk.
	* @param context the eMatrix <code>Context</code> object
	* @param args holds all arguments that are being passed in the to emxTable.jsp in the above specified Command Href.
	* @returns MapList of Object IDs.
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAllAccessRequests( Context context, String[] args ) throws Exception
	{
		HashMap programMap = ( HashMap ) JPO.unpackArgs( args );

		String strStatusFilterValues = EnoviaResourceBundle.getProperty(context,"emxComponents.AccessRequest.StatusFilterValues");
		StringList slStatusValues = FrameworkUtil.split( strStatusFilterValues,"|" );

		String  strState = ( String ) programMap.get( "APPAccessRequestStateFilter" );
		String  strStatus  = ( String ) programMap.get( "APPAccessRequestStatusFilter" );

		StringBuffer strWhere = new StringBuffer();
		StringList objectSelects = new StringList(1);
		objectSelects.addElement( DomainObject.SELECT_ID );

		String strSubmitted = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_SUBMITTED);

		if ( "All".equals( strState ) )
		{
			strWhere.append( "" );
		}
		else if ( null == strState || "null".equals( strState ) || "".equals( strState ) )
		{
			strWhere.append( "( current  == \"" );
			strWhere.append( strSubmitted );
			strWhere.append( "\") && " );
		}
		else
		{
			strWhere.append( "( current  == \"" );
			strWhere.append( strState );
			strWhere.append( "\") && " );
		}

		if ( null == strStatus || "null".equals( strStatus ) || "".equals( strStatus ) )
		{
			strWhere.append( " ( attribute[Originator] == \"");
			strWhere.append( context.getUser() );
			strWhere.append( "\")" );
		}
		else if ( slStatusValues.get(0).equals(strStatus))
		{
			strWhere.append( " ( attribute[Originator] == \"");
			strWhere.append( context.getUser() );
			strWhere.append( "\")" );
		}
		else if ( slStatusValues.get(1).equals(strStatus))
		{
			strWhere.append( " ( ( owner == \"");
			strWhere.append( context.getUser() );
			strWhere.append( "\") && ( current != \"" );
			strWhere.append( strSubmitted );
			strWhere.append( "\") )" );
		}
		else if ( slStatusValues.get(2).equals(strStatus))
		{
			strWhere.append( " ( current == \"" );
			strWhere.append( strSubmitted );
			strWhere.append( "\")" );
		}
		else if(strWhere.length()!=0)
		{
			strWhere.setLength(strWhere.length()-3);
		}
		return DomainObject.findObjects(	context,
														TYPE_DOWNLOAD_ACCESS_REQUEST,
														DomainConstants.QUERY_WILDCARD,
														strWhere.toString(),
														objectSelects );
	}

	/**
	* Method Expire Requests.
	* @param context - the eMatrix <code>Context</code> object
	* @param args - args
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public void setAccessRequestGrant( Context context, String objectId,String strOriginator ) throws Exception
	{
		MapList docMapList = getDocuments( context, objectId );
		MapList assigneeMapList = getAssignees( context, objectId );

		Iterator docMapListItr = docMapList.iterator();
		DomainObject requestObj = new DomainObject( objectId );

		StringList sl = new StringList(2);
		sl.add( "attribute["+ ATTRIBUTE_INCLUDE_SELF+"]");
		sl.add( DomainConstants.SELECT_DESCRIPTION);

		Map map = requestObj.getInfo( context, sl );

		String strIncludeSelf = ( String) map.get( "attribute["+ATTRIBUTE_INCLUDE_SELF+"]" );
		String strDesKey = ( String) map.get( DomainConstants.SELECT_DESCRIPTION );

		String strCheckout = "CheckOut";
		String strRead = "read";
		String strShow = "show";

		ContextUtil.pushContext( context, PERSON_REQUEST_ACCESS_GRANTOR, null, null );
		try {
			while(docMapListItr.hasNext()) {
				Iterator assigneeMapListItr = assigneeMapList.iterator();
				Map docMap = (Map) docMapListItr.next();
				String strDocumentId = (String) docMap.get(DomainConstants.SELECT_ID);

				// Access Grant for Originator
				if("Yes".equalsIgnoreCase(strIncludeSelf)) {
                                       MqlUtil.mqlCommand(context,"modify bus $1 grant \"$2\" access $3,$4,$5 key $6",strDocumentId,strOriginator,strCheckout,strRead,strShow,strDesKey);
				}

				// Access Grant for Assignees
				while(assigneeMapListItr.hasNext()) {
					Map assigneeMap = (Map) assigneeMapListItr.next();
					String strUserName = (String) assigneeMap.get(DomainConstants.SELECT_NAME);
					if(!strOriginator.equals(strUserName)) {
												MqlUtil.mqlCommand(context,"modify bus $1 grant \"$2\" access $3,$4,$5 key $6",strDocumentId,strUserName,strCheckout,strRead,strShow,strDesKey);
						ContextUtil.pushContext(context);
						try {
							MqlUtil.mqlCommand(context, "modify bus $1 grant \"$2\" access $3,$4", objectId,strUserName,strRead,strShow);
						} finally {
							ContextUtil.popContext(context);
						}
					}
				}
			}
		} finally {
			ContextUtil.popContext(context);
		}
	} // End - Method

	/**
	* Method Revoke.
	* @param context - the eMatrix <code>Context</code> object
	* @param args
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public void setAccessRequestRevoke( Context context, String objectId,String strOriginator ) throws Exception
	{
		MapList docMapList = getDocuments( context, objectId );
		MapList assigneeMapList = getAssignees( context, objectId );

		DomainObject domRequestObj = new DomainObject( objectId );
		StringList sl = new StringList(3);
		sl.add( "attribute["+ ATTRIBUTE_INCLUDE_SELF+"]" );
		sl.add( DomainConstants.SELECT_DESCRIPTION );
		sl.add( DomainConstants.SELECT_OWNER );

		Map map = domRequestObj.getInfo(context, sl);

		String strOwner = (String)map.get( DomainConstants.SELECT_OWNER );
		String strIncludeSelf = ( String) map.get("attribute["+ATTRIBUTE_INCLUDE_SELF+"]" );
		String strDesKey = ( String) map.get( DomainConstants.SELECT_DESCRIPTION );

		Iterator docMapListItr = docMapList.iterator();
		ContextUtil.pushContext( context,PERSON_REQUEST_ACCESS_GRANTOR,null,null );
		try {
			while(docMapListItr.hasNext()) {
				Iterator assigneeMapListItr = assigneeMapList.iterator();
				Map docMap = (Map) docMapListItr.next();
				String strDocumentId = (String) docMap.get(DomainConstants.SELECT_ID);

				// Access Revoke for Originator
				if("Yes".equalsIgnoreCase(strIncludeSelf)) {
					MqlUtil.mqlCommand(context, "modify bus $1 revoke grantee $2 key $3", strDocumentId, strOriginator, strDesKey);
				}

				// Access Revoke for Assignees
				while(assigneeMapListItr.hasNext()) {
					Map assigneeMap = (Map) assigneeMapListItr.next();
					String strUserName = (String)assigneeMap.get(DomainConstants.SELECT_NAME);
					if(!strOriginator.equals(strUserName)) {
						MqlUtil.mqlCommand(context, "modify bus $1 revoke grantee $2 key $3", strDocumentId, strUserName, strDesKey);
					}
				}
			}
		} finally {
			ContextUtil.popContext(context);
		}
	} // End - Method


	/**
	* Method Expire Requests.
	* @param context - the eMatrix <code>Context</code> object
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public void autoExpireRequests( Context context, String[] args ) throws Exception
	{
		//Getting and representing the state 'Submitted'
		String strApproved = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_APPROVED);

		StringList objectSelects = new StringList(2);
		objectSelects.addElement( DomainObject.SELECT_ID );
		objectSelects.addElement( "attribute[" + ATTRIBUTE_GRANT_EXPIRATION_DATE + "]" );

		MapList requestMapList = DomainObject.findObjects(  context,
																				TYPE_DOWNLOAD_ACCESS_REQUEST,
																				DomainConstants.QUERY_WILDCARD,
																				"( current == \"" + strApproved + "\" )",
																				objectSelects );

		Iterator requestMapListItr = requestMapList.iterator();
		SimpleDateFormat sdf = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
        /**
         * While checking for the expirydate we will not considering time.
         * So setting current time and expiry time also to 12:00:00 AM
         */
        Calendar todayCalender = Calendar.getInstance(Locale.US);
        todayCalender.set(Calendar.HOUR, 0);
        todayCalender.set(Calendar.MINUTE, 0);
        todayCalender.set(Calendar.SECOND, 0);
        todayCalender.set(Calendar.MILLISECOND, 0);
        todayCalender.set(Calendar.AM_PM, Calendar.AM);

        String strTodaysDate = sdf.format( todayCalender.getTime() );
		while( requestMapListItr.hasNext() )
		{
			Map requestMap = (Map) requestMapListItr.next();
			String strObjectId = (String)requestMap.get( DomainConstants.SELECT_ID );
			String strGrantExpirationDate = (String)requestMap.get( "attribute[" + ATTRIBUTE_GRANT_EXPIRATION_DATE + "]" );
			//Getting the Duration

			int slipDays = 0;
			if ( ! ( null  == strGrantExpirationDate || "null".equals( strGrantExpirationDate ) || "".equals( strGrantExpirationDate ) ) )
			{

                Calendar expCal = Calendar.getInstance(Locale.US);
                expCal.setTime(eMatrixDateFormat.getJavaDate(strGrantExpirationDate));
                expCal.set(Calendar.HOUR, 0);
                expCal.set(Calendar.MINUTE, 0);
                expCal.set(Calendar.SECOND, 0);
                expCal.set(Calendar.MILLISECOND, 0);
                expCal.set(Calendar.AM_PM, Calendar.AM);

                slipDays = (int)Issue.daysBetween(sdf.format(expCal.getTime()), strTodaysDate);
				if ( slipDays < 0 )
				{
						BusinessObject busObj = new BusinessObject( strObjectId );
						busObj.promote( context );
				}
				else if ( slipDays <= 10 )
				{
					notifyOfPendingExpiration( context, strObjectId );
				}
			}
		}
	} // End - Method

	/**
	* This method notifies owner and all the assignees connected
	* to Request Access  by "Requested Assignees" relationship when the Request Access is going to expire
	* @param context The ematrix context of the request.
	* @param objectId The string containing object id of Request Access Object:
	* @throws Exception
	* @throws FrameworkException
	* @since Common 11.0
	*/
	public void notifyOfPendingExpiration( Context context, String objectId ) throws Exception,FrameworkException
	{
		DomainObject domRequest = DomainObject.newInstance( context, objectId );
		String strOriginator = ( String )domRequest.getInfo( context,DomainConstants.SELECT_ORIGINATOR );

		StringList slAssignees = new StringList();
		slAssignees.add( strOriginator );

		String strLanguage = context.getSession().getLanguage();
		Locale strLocale = context.getLocale();
		String strSubjectKey = EnoviaResourceBundle.getProperty(context,
																		 RESOURCE_BUNDLE_COMPONENTS_STR,
																		 strLocale,"emxComponents.AccessRequest.MessageSubject.PendingExpiration");
		String strMessageKey = EnoviaResourceBundle.getProperty(context,
																		  RESOURCE_BUNDLE_COMPONENTS_STR,
																		  strLocale, "emxComponents.AccessRequest.MessageDescription.PendingExpiration");
		strMessageKey = MessageUtil.substituteValues(context, strMessageKey, objectId, strLanguage);

		String[] subjectKeys = {};
		String[] subjectValues = {};
		String[] messageKeys = {};
		String[] messageValues = {};

		//Form the message attachment
		StringList lstAttachments = new StringList();
		lstAttachments.add(objectId);

		//Send the notification to the owner & memberList
		emxMailUtil_mxJPO.sendNotification( context,
																	(StringList)slAssignees,
																	null,
																	null,
																	strSubjectKey,
																	subjectKeys,
																	subjectValues,
																	strMessageKey,
																	messageKeys,
																	messageValues,
																	(StringList)lstAttachments,
																	null);
	}

	/**
	* showStatusIcon - gets the status gif to be shown in the column of the Access Request Summary table
	* @param context the eMatrix <code>Context</code> object
	* @param args holds the following input arguments:
	*        0 - objectList MapList
	* @returns Object of type Vector
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public Vector showStatusIcon( Context context, String[] args ) throws Exception
	{
		HashMap programMap = ( HashMap ) JPO.unpackArgs( args );
		MapList objectList = ( MapList )programMap.get( "objectList" );
		int iObjectListSize = objectList.size();
		Vector enableCheckbox = new Vector( iObjectListSize );
		String strLanguage = context.getSession().getLanguage();

		// Check Days from properties file
		int strRedDays =  ( int ) Integer.parseInt( EnoviaResourceBundle.getProperty(context, "emxComponents.AccessRequest.RedDays") );
		int strGreenDays = ( int ) Integer.parseInt( EnoviaResourceBundle.getProperty(context, "emxComponents.AccessRequest.GreenDays") );

		SimpleDateFormat sdf = new SimpleDateFormat( eMatrixDateFormat.getEMatrixDateFormat() );
		DomainObject requestObject = new DomainObject();

		/**
		* Selectables and Multi value list
		**/
		StringList objectSelects = new StringList( 2 );
		objectSelects.add( "attribute[" + ATTRIBUTE_GRANT_EXPIRATION_DATE + "]" );
		objectSelects.add( DomainConstants.SELECT_CURRENT );

		DomainConstants.MULTI_VALUE_LIST.add( "attribute[" + ATTRIBUTE_GRANT_EXPIRATION_DATE + "]" );
		DomainConstants.MULTI_VALUE_LIST.add( DomainConstants.SELECT_CURRENT );

		String objIdArray[] = new String[ iObjectListSize ];
		//Get the array of Object Ids to be paased into the methods
		for ( int i = 0; i < objectList.size(); i++ )
		{
			Map objMap = (Map)objectList.get( i );
			objIdArray[ i ]  = (String)objMap.get( DomainConstants.SELECT_ID );
		}

		// All Access Request Assignee Information
		MapList reqMapList = DomainObject.getInfo( context, objIdArray, objectSelects );

		// Todays Date
		String strTodaysDate = sdf.format( Calendar.getInstance().getTime() );
		String strApproved = FrameworkUtil.lookupStateName( context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_APPROVED );
		String strSubmitted = FrameworkUtil.lookupStateName( context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_SUBMITTED );

		// Assignees concatenation
		for ( int i = 0;i<reqMapList.size() ;i++ )
		{
			StringBuffer sbFullName = new StringBuffer();
			Map requestMap = ( Map ) reqMapList.get( i );

			if ( requestMap.size() == 0)
			{
				enableCheckbox.add( "" );
				continue;
			}

			StringList slGrantExpirationDate = (StringList) requestMap.get( "attribute[" + ATTRIBUTE_GRANT_EXPIRATION_DATE + "]" );
			StringList slCurrentState = (StringList) requestMap.get( DomainConstants.SELECT_CURRENT  );
			String strCurrentState = ( String ) slCurrentState.get(0);

			String strStatus = null;
			/**
			* check grant expiration date with current date
			**/
// IR-020143V6R2011x - Start
			int slipDays = 0;
// IR-020143V6R2011x - End
			if ( null != slGrantExpirationDate)
			{
				String strGrantExpirationDate = ( String ) slGrantExpirationDate.get(0);
				if(strGrantExpirationDate!=null && !"null".equals(strGrantExpirationDate) && !"".equals(strGrantExpirationDate))
				{
					// get slip days : days between expiration date and todays date
					slipDays = ( int ) Issue.daysBetween( strGrantExpirationDate, strTodaysDate );

					if ( strCurrentState.equals( strApproved ))
					{
						if ( slipDays <  strRedDays) strStatus = "Red";
						else if ( slipDays >  strGreenDays)	 strStatus = "Green";
						else 	strStatus = "Yellow";
					}
					else if ( strCurrentState.equals( strSubmitted ) )
					{
						strStatus = "Grey";
					}
				}
				else
				{
					if ( strCurrentState.equals( strSubmitted ) )
					{
							strStatus = "Grey";
					}
					if ( strCurrentState.equals( strApproved ) )
					{
							strStatus = "Green";
					}
				}
			}
			if ( strStatus == null)
			{
				enableCheckbox.add("");
			}
			else
			{
// IR-020143V6R2011x - Start
				String strToolTip = DomainConstants.EMPTY_STRING;
				if( "Yellow".equals( strStatus ) ) {
					strToolTip = slipDays + " " + EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(),"emxComponents.AccessRequest.ToolTip"+strStatus+"Days" );
				} else {
					strToolTip = EnoviaResourceBundle.getProperty( context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(),"emxComponents.AccessRequest.ToolTip"+strStatus+"Days" );
				}
// IR-020143V6R2011x - End
				enableCheckbox.add("<img border='0' src='../common/images/iconStatus" + strStatus + ".gif' name='" + strStatus + "' id='" + strStatus + "' alt=\"" + strToolTip + "\" title=\"" + strToolTip + "\" />");
			}
		}
		synchronized(this)
		{
			DomainConstants.MULTI_VALUE_LIST.remove( "attribute[" + ATTRIBUTE_GRANT_EXPIRATION_DATE + "]" );
			DomainConstants.MULTI_VALUE_LIST.remove( DomainConstants.SELECT_CURRENT );
		}
		//XSSOK
		return enableCheckbox;
	}

	/**
	* revise a Access Request.
	* @param context the eMatrix <code>Context</code> object
	* @param args holds the following input arguments:
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public void autoAccessRequestRevise(Context context, String[] args)  throws Exception
	{
		String strLanguage = context.getSession().getLanguage();
		// the domain object
		DomainObject requestObj = new DomainObject( args[0] );
		String strLastRevison =  requestObj.getLastRevision( context ).getRevision();

		if ( strLastRevison.equals( requestObj.getInfo( context , DomainConstants.SELECT_REVISION) ) )
		{
			BusinessObject revisedRequest = requestObj.reviseObject( context, true);
		}
	}

	/* Method to get States
	* @param context - the eMatrix <code>Context</code> object
	* @param args - args contains a Map with the following entries
	*                      objectId - The object Id of Context object
	* @return - boolean (true or false)
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public HashMap getFilterStates(Context context, String[] args) throws Exception
	{
		//Unpacking the arguments
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		// initialize the return variable HashMap stateMap = new HashMap();
		HashMap stateMap = new HashMap();

		// initialize the Stringlists fieldRangeValues, fieldDisplayRangeValues
		StringList fieldRangeValues = new StringList();
		StringList fieldDisplayRangeValues = new StringList();

		// Process information to obtain the range values and add them to fieldRangeValues
		// Get the internationlized value of the range values and add them to fieldDisplayRangeValues -- internationlised
		String strStateFilterValues = EnoviaResourceBundle.getProperty(context,"emxComponents.AccessRequest.StateFilterValues");
		StringList slStateValues = FrameworkUtil.split( strStateFilterValues,"|" );

		for( int i=0;i<slStateValues.size();i++ )
		{
		    String strStateFilterValue = EnoviaResourceBundle.getProperty( context , RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(),"emxComponents.AccessRequest."+ (String)slStateValues.get(i) );
			fieldRangeValues.addElement( slStateValues.get(i) );
			fieldDisplayRangeValues.addElement( strStateFilterValue );
		}
		stateMap.put("field_choices", fieldRangeValues);
		stateMap.put("field_display_choices", fieldDisplayRangeValues);
		return stateMap;
	}

	/* Method to get Status from properties
	* @param context - the eMatrix <code>Context</code> object
	* @param args - args contains a Map with the following entries
	*                      objectId - The object Id of Context object
	* @return - boolean (true or false)
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public HashMap getFilterStatuses(Context context, String[] args) throws Exception
	{
		// initialize the return variable HashMap statusMap = new HashMap();
		HashMap statusMap = new HashMap();

		// initialize the Stringlists fieldRangeValues, fieldDisplayRangeValues
		StringList fieldRangeValues = new StringList();
		StringList fieldDisplayRangeValues = new StringList();

		// Process information to obtain the range values and add them to fieldRangeValues
		// Get the internationlized value of the range values and add them to fieldDisplayRangeValues -- internationlised
		String strStatusFilterValues = EnoviaResourceBundle.getProperty(context,"emxComponents.AccessRequest.StatusFilterValues");
		StringList slStatusValues = FrameworkUtil.split( strStatusFilterValues,"|" );

		for( int i=0;i<slStatusValues.size();i++ )
		{
		    String strStatusFilterValue = EnoviaResourceBundle.getProperty( context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(),"emxComponents.AccessRequest.StatusFilterValue"+FrameworkUtil.findAndReplace( (String)slStatusValues.get(i) ," ", "_" ) );
			fieldRangeValues.addElement( slStatusValues.get(i) );
			fieldDisplayRangeValues.addElement( strStatusFilterValue );
		}
		statusMap.put("field_choices", fieldRangeValues);
		statusMap.put("field_display_choices", fieldDisplayRangeValues);
		return statusMap;
	}

	/**
	* Method to check for display for the Grant Expiration Date Display field.
	* @param context - the eMatrix <code>Context</code> object
	* @param args - args contains a Map with the following entries
	*                      objectId - The object Id of Context object
	* @return - boolean (true or false)
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public boolean grantExpirationDateNotDisplay(Context context, String[] args) throws Exception
	{
		//Getting and representing the state 'Submitted'
		String strSubmitted = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_SUBMITTED);

		//Getting and representing the state 'Review'
		String strReview = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_REVIEW);

		//Unpacking the arguments
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		DomainObject domRequest = new DomainObject( (String) programMap.get("objectId") );

		StringList objectSelects = new StringList(2);
		objectSelects.add( DomainConstants.SELECT_CURRENT );
		objectSelects.add( DomainConstants.SELECT_OWNER );

		Map requestMap = domRequest.getInfo( context, objectSelects );

		//Getting the current state
		String strCurrentState = ( String) requestMap.get( DomainConstants.SELECT_CURRENT);
		String strOwner = ( String) requestMap.get( DomainConstants.SELECT_OWNER);

		return ! ( strOwner.equals( context.getUser() ) && ! strCurrentState.equals(strSubmitted) );
	}

	/**
	* Method to check for display for the Grant Expiration Date Display field.
	* @param context - the eMatrix <code>Context</code> object
	* @param args - args contains a Map with the following entries
	*                      objectId - Object Id of the Context object
	* @return - boolean (true or false)
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public boolean grantExpirationDateDisplay(Context context, String[] args) throws Exception
	{
		//Getting and representing the state 'Submitted'
		String strSubmitted = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_SUBMITTED);

		//Getting and representing the state 'Review'
		String strReview = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_REVIEW);

		//Unpacking the arguments
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		DomainObject domRequest = new DomainObject( (String) programMap.get("objectId") );

		StringList objectSelects = new StringList(2);
		objectSelects.add( DomainConstants.SELECT_CURRENT );
		objectSelects.add( DomainConstants.SELECT_OWNER );

		Map requestMap =domRequest.getInfo( context, objectSelects );

		//Getting the current state
		String strCurrentState = ( String) requestMap.get( DomainConstants.SELECT_CURRENT);
		String strOwner = ( String) requestMap.get( DomainConstants.SELECT_OWNER);
		return ( strOwner.equals( context.getUser() ) && ! strCurrentState.equals(strSubmitted) );
	}

	/**
	* Method to check for display for the Extension Date Display field.
	* @param context - the eMatrix <code>Context</code> object
	* @param args - args contains a Map with the following entries
	*                      objectId - The object Id of Context object
	* @return - boolean (true or false)
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public boolean extensionDateNotDisplay(Context context, String[] args) throws Exception
	{
		//Getting and representing the state 'Expired'
		String strExpired = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_EXPIRED);

		//Getting and representing the state 'Approved'
		String strApproved = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_APPROVED);

		//Unpacking the arguments
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		DomainObject domRequest = new DomainObject( (String) programMap.get("objectId") );


		StringList objectSelects = new StringList(3);
		objectSelects.add( DomainConstants.SELECT_CURRENT );
		objectSelects.add( DomainConstants.SELECT_ORIGINATOR );
		objectSelects.add( DomainConstants.SELECT_REVISION );

		Map requestMap =domRequest.getInfo( context, objectSelects );

		//Getting the current state
		String strCurrentState = ( String) requestMap.get( DomainConstants.SELECT_CURRENT);
		String strOriginator = ( String) requestMap.get( DomainConstants.SELECT_ORIGINATOR);

		String strLastRevison =  domRequest.getLastRevision( context ).getRevision();
		boolean isEditable = strLastRevison.equals(  ( String) requestMap.get( DomainConstants.SELECT_REVISION) ) ;

		return ! ( isEditable && ( strCurrentState.equals(strApproved) || strCurrentState.equals(strExpired)  ) && strOriginator.equals(context.getUser() )  );
	}

	/**
	* Method to check for display for the Extension Date Display field.
	* @param context - the eMatrix <code>Context</code> object
	* @param args - args contains a Map with the following entries
	*                      objectId - Object Id of the Context object
	* @return - boolean (true or false)
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public boolean  extensionDateDisplay(Context context, String[] args) throws Exception
	{
		//Getting and representing the state 'Expired'
		String strExpired = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_EXPIRED);

		//Getting and representing the state 'Approved'
		String strApproved = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_APPROVED);

		//Unpacking the arguments
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		DomainObject domRequest = new DomainObject( (String) programMap.get("objectId") );

		StringList objectSelects = new StringList(3);
		objectSelects.add( DomainConstants.SELECT_CURRENT );
		objectSelects.add( DomainConstants.SELECT_ORIGINATOR );
		objectSelects.add( DomainConstants.SELECT_REVISION );

		Map requestMap =domRequest.getInfo( context, objectSelects );

		//Getting the current state
		String strCurrentState = ( String) requestMap.get( DomainConstants.SELECT_CURRENT);
		String strOriginator = ( String) requestMap.get( DomainConstants.SELECT_ORIGINATOR);

		String strLastRevison =  domRequest.getLastRevision( context ).getRevision();
		boolean isEditable = strLastRevison.equals(  ( String) requestMap.get( DomainConstants.SELECT_REVISION) ) ;

		return ( isEditable && ( strCurrentState.equals(strApproved) || strCurrentState.equals(strExpired) ) && strOriginator.equals(context.getUser() ) );
	}

	/**
	* Method to check for display for the Reason For Request Display field.
	* @param context - the eMatrix <code>Context</code> object
	* @param args - args contains a Map with the following entries
	*                      objectId - The object Id of Context object
	* @return - boolean (true or false)
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public boolean reasonForRequestNotDisplay(Context context, String[] args) throws Exception
	{
		//Getting and representing the state 'Submitted'
		String strSubmitted = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_SUBMITTED);

		//Unpacking the arguments
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		DomainObject domRequest = new DomainObject( (String) programMap.get("objectId") );

		StringList objectSelects = new StringList(2);
		objectSelects.add( DomainConstants.SELECT_CURRENT );
		objectSelects.add( DomainConstants.SELECT_ORIGINATOR );

		Map requestMap =domRequest.getInfo( context, objectSelects );

		//Getting the current state
		String strCurrentState = ( String) requestMap.get( DomainConstants.SELECT_CURRENT);
		String strOriginator = ( String) requestMap.get( DomainConstants.SELECT_ORIGINATOR);

		return ! ( strCurrentState.equals(strSubmitted) && strOriginator.equals( context.getUser() ) );
	}

	/**
	* Method to check for display for the Reason For Request Display field.
	* @param context - the eMatrix <code>Context</code> object
	* @param args - args contains a Map with the following entries
	*                      objectId - Object Id of the Context object
	* @return - boolean (true or false)
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public boolean reasonForRequestDisplay(Context context, String[] args) throws Exception
	{
		//Getting and representing the state 'Submitted'
		String strSubmitted = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_SUBMITTED);
		//Unpacking the arguments
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		DomainObject domRequest = new DomainObject( (String) programMap.get("objectId") );

		StringList objectSelects = new StringList(2);
		objectSelects.add( DomainConstants.SELECT_CURRENT );
		objectSelects.add( DomainConstants.SELECT_ORIGINATOR );

		Map requestMap =domRequest.getInfo( context, objectSelects );

		//Getting the current state
		String strCurrentState = ( String) requestMap.get( DomainConstants.SELECT_CURRENT);
		String strOriginator = ( String) requestMap.get( DomainConstants.SELECT_ORIGINATOR);

		return ( strCurrentState.equals(strSubmitted) && strOriginator.equals( context.getUser() ) );
	}

	/**
	* Method to check for display for the include Self Display field.
	* @param context - the eMatrix <code>Context</code> object
	* @param args - args contains a Map with the following entries
	*                      objectId - Object Id of the Context object
	* @return - boolean (true or false)
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public boolean includeSelfNotDisplay(Context context, String[] args) throws Exception
	{
		//Getting and representing the state 'Submitted'
		String strSubmitted = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_SUBMITTED);

		//Unpacking the arguments
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		DomainObject domRequest = new DomainObject( (String) programMap.get("objectId") );

		StringList objectSelects = new StringList(2);
		objectSelects.add( DomainConstants.SELECT_CURRENT );
		objectSelects.add( DomainConstants.SELECT_ORIGINATOR );
		Map requestMap =domRequest.getInfo( context, objectSelects );

		//Getting the current state
		String strCurrentState = ( String) requestMap.get( DomainConstants.SELECT_CURRENT);
		String strOriginator = ( String) requestMap.get( DomainConstants.SELECT_ORIGINATOR);

		return !( strCurrentState.equals(strSubmitted) && strOriginator.equals( context.getUser() ) );
	}

	/**
	* Method to check for display for the include Self Display field.
	* @param context - the eMatrix <code>Context</code> object
	* @param args - args contains a Map with the following entries
	*                      objectId - Object Id of the Context object
	* @return - boolean (true or false)
	* @throws Exception if the operation fails
	* @since Common 11.0
	*/
	public boolean includeSelfDisplay(Context context, String[] args) throws Exception
	{
		//Getting and representing the state 'Submitted'
		String strSubmitted = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_SUBMITTED);

		//Unpacking the arguments
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		DomainObject domRequest = new DomainObject( (String) programMap.get("objectId") );

		StringList objectSelects = new StringList(2);
		objectSelects.add( DomainConstants.SELECT_CURRENT );
		objectSelects.add( DomainConstants.SELECT_ORIGINATOR );
		Map requestMap =domRequest.getInfo( context, objectSelects );

		//Getting the current state
		String strCurrentState = ( String) requestMap.get( DomainConstants.SELECT_CURRENT);
		String strOriginator = ( String) requestMap.get( DomainConstants.SELECT_ORIGINATOR);

		return ( strCurrentState.equals(strSubmitted) && strOriginator.equals( context.getUser() ) );
	}



	/**
	* This method notifies owner and all the assignees connected
	* to Request Access  by "Requested Assignees" relationship when the Request Access is promoted to
	* the Review , Rejected Or Approved and Expired.
	* @param context The ematrix context of the request.
	* @param objectId The string containing object id of Request Access Object:
	* @param strNextState The string containing promoted state of Request Access Object:
	* @throws Exception
	* @throws FrameworkException
	* @since Common 11.0
	*/
	public void setUserAccessAndNotification( Context context, String[] args ) throws Exception,FrameworkException
	{
		//Get the Object Id of the context Request Access object.
		String objectId = args[0];
		//Get the Object Promte State  of the context Request Access object.
		String strNextState = args[1];
		//Get the Object Current State  of the context Request Access object.
		String strCurrentState = args[2];

		//Getting and representing the state 'Submitted'
		String strSubmitted = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_SUBMITTED);
		//Getting and representing the state 'Review'
		String strReview = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_REVIEW);
		//Getting and representing the state 'Approved'
		String strApproved = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_APPROVED);
		//Getting and representing the state 'Expired'
		String strExpired = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_EXPIRED);

		String strLanguage = context.getSession().getLanguage();
		Locale strLocale = context.getLocale();
		Object nameObj = null;

		// the domain object
		DomainObject domRequest = DomainObject.newInstance( context, objectId );

		StringList slAssignees =  new StringList();
		StringList strList = new StringList( 4 );
		strList.addElement( "to[" + RELATIONSHIP_REQUESTED_ASSIGNEE + "].from.attribute[" + DomainConstants.ATTRIBUTE_FIRST_NAME + "]" );
		strList.addElement( "to[" + RELATIONSHIP_REQUESTED_ASSIGNEE + "].from.attribute[" + DomainConstants.ATTRIBUTE_LAST_NAME + "]" );
		strList.addElement( "to[" + RELATIONSHIP_REQUESTED_ASSIGNEE + "].from.name" );
		strList.addElement( DomainConstants.SELECT_ORIGINATOR );

		// set object id
		setId(objectId);

		// get all connected assignee list
		Map infoMap =  getInfo(context, strList, strList);

		StringList slOriginator = (StringList)infoMap.get( DomainConstants.SELECT_ORIGINATOR );
		String strOriginator = (String)slOriginator.get(0);
		slAssignees.add( strOriginator );

		// set Owner
		domRequest.setOwner( context, context.getUser() );
		/**
			Strore object id into description for access key purpose in giveing the  grant/revoke access on Document Object
		**/
		domRequest.setDescription(context, objectId );

		slAssignees.add( context.getUser() );
		String strAccessOriginator = EnoviaResourceBundle.getProperty(context,"emxComponents.AccessRequest.AccessOriginator");
		if ( strNextState.equals( strReview ) )
		{
			ContextUtil.pushContext(context);
			try
			{
				String command = "modify bus " + objectId + " grant \"" + strOriginator + "\" access " + strAccessOriginator ;
				MqlUtil.mqlCommand(context, command);
			}
			finally
			{
				ContextUtil.popContext(context);
			}
		}
		else if( strNextState.equals( strApproved ))
		{
			// set grant access
			setAccessRequestGrant( context, objectId,strOriginator );
			//Cast the output to either String or List depending upon the type.
			nameObj = (Object) infoMap.get(  "to[" + RELATIONSHIP_REQUESTED_ASSIGNEE + "].from.name"  );
		}
		else  if( strNextState.equals( strExpired ))
		{
			// revoke grant access
			setAccessRequestRevoke( context, objectId,strOriginator );
			//Cast the output to either String or List depending upon the type.
		    nameObj = (Object) infoMap.get(  "to[" + RELATIONSHIP_REQUESTED_ASSIGNEE + "].from.name"  );
		}

		if ( nameObj != null)
		{
			if ( nameObj instanceof StringList)
				slAssignees.addAll( (StringList) nameObj );
			else
				slAssignees.add( (String) nameObj );
		}

		MapList docMapList = getDocuments( context, objectId);
		StringBuffer sbDocNames = new StringBuffer();
		//Form the message attachment
		StringList lstAttachments = new StringList();
		lstAttachments.add( objectId );

		for( int i = 0;i < docMapList.size(); i++ )
		{
			Map docMap = (Map)docMapList.get(i);
			sbDocNames.append( (String)docMap.get( SELECT_NAME ) );
			sbDocNames.append( ";" );
		}
		sbDocNames.setLength(sbDocNames.length()-1);

		String strSendNotification = EnoviaResourceBundle.getProperty(context,"emxComponents.AccessRequest.SendNotification");

		if ( "true".equals( strSendNotification ) )
		{
			String strSubjectKey = EnoviaResourceBundle.getProperty( context, RESOURCE_BUNDLE_COMPONENTS_STR, strLocale,"emxComponents.AccessRequest.Message.Subject.AccessRequest");
			strSubjectKey = strSubjectKey + " " + strCurrentState + " state to " + strNextState + " state.";

			String strMessageKey = EnoviaResourceBundle.getProperty( context, RESOURCE_BUNDLE_COMPONENTS_STR, strLocale,"emxComponents.AccessRequest.Message.Description.AccessRequest");
			strMessageKey = strMessageKey + " Documents:" + sbDocNames + "\n";
			strMessageKey = strMessageKey + " Request raised by:" + PersonUtil.getFullName( context, strOriginator ) + "\n";
			strMessageKey = strMessageKey + " Additional Users:" + getAssigneeName(  context, objectId ) +"\n";
			strMessageKey = strMessageKey.trim();
			strMessageKey = MessageUtil.substituteValues(context, strMessageKey, objectId, strLanguage);

			String[] subjectKeys = {};
			String[] subjectValues = {};
			String[] messageKeys = {};
			String[] messageValues = {};

			//Send the notification to the owner & assignees

			emxMailUtil_mxJPO.sendNotification( context,
																		(StringList)slAssignees,
																		null,
																		null,
																		strSubjectKey,
																		subjectKeys,
																		subjectValues,
																		strMessageKey,
																		messageKeys,
																		messageValues,
																		(StringList)lstAttachments,
																		null);
		}
	}

	/**
	* This method remove signature on demote from approved to review
	* @param context The ematrix context of the request.
	* @param objectId The string containing object id of Request Access Object:
	* @throws Exception
	* @throws FrameworkException
	* @since Common 11.0
	*/
	public void removeSignatureOnDemote( Context context, String[] args ) throws Exception,FrameworkException
	{
		String arg = args[0];
		MqlUtil.mqlCommand( context, "unsign bus $1 signature ToApproved", arg );//qbq
		DomainObject domRequestObj =  new DomainObject( args[0] );
		String strOriginator = (String) domRequestObj.getInfo( context,DomainConstants.SELECT_ORIGINATOR );
		setAccessRequestRevoke( context, args[0] ,strOriginator );
	}

	/**
	* This method assigs the originator as owner to given Request Object.
	* @param context The ematrix context of the request.
	* @param objectId The string containing object id of Request Access Object:
	* @throws Exception
	* @throws FrameworkException
	* @since Common 11.0
	*/
	public void assignOwner( Context context, String[] args ) throws Exception,FrameworkException
	{
		String strObjId = args[0];
		String strFromState = args[1];

		DomainObject domRequestObj =  new DomainObject( args[0] );
		String strOriginator = domRequestObj.getInfo(context,DomainConstants.SELECT_ORIGINATOR);
		domRequestObj.setOwner(context,strOriginator);
	}


	/**
	* This method is used to get the list of description of each object in the object list that is passed as arguments.
	* @param context The ematrix context of the request.
	* @param args The String array containing Packed arguments, This also contains list of object ids
	* @return a Vector object containing discriptions of each object in the object list that is passed as arguments
	* @throws Exception
	* @since Common 11.0
	*/
	public Vector getObjectDescription(Context context, String[] args) throws Exception
	{
		return getColumnValues(context, args, DomainConstants.SELECT_DESCRIPTION);
	}

	/**
	* This method is used to get the list of current state of each object in the object list that is passed as arguments.
	* @param context The ematrix context of the request.
	* @param args The String array containing Packed arguments, This also contains list of object ids
	* @return a Vector object containing current state of each object in the object list that is passed as arguments
	* @throws Exception
	* @since Common 11.0
	*/
	public Vector getObjectState(Context context, String[] args) throws Exception
	{
		return getColumnValues(context, args, DomainConstants.SELECT_CURRENT);
	}

	/**
	* This method is used to get the list of owner of each object in the object list that is passed as arguments.
	* @param context The ematrix context of the request.
	* @param args The String array containing Packed arguments, This also contains list of object ids
	* @return a Vector object containing owner of each object in the object list that is passed as arguments
	* @throws Exception
	* @since Common 11.0
	*/
	public Vector getObjectOwner(Context context, String[] args) throws Exception
	{
		//XSSOK
		return getColumnValues(context, args, DomainConstants.SELECT_OWNER);
	}

	/**
	* This method is used to get the list of Title attribute of each object in the object list that is passed as arguments.
	* @param context The ematrix context of the request.
	* @param args The String array containing Packed arguments, This also contains list of object ids
	* @return a Vector object containing value of Title attribute of each object in the object list that is passed as arguments
	* @throws Exception
	* @since Common 11.0
	*/
	public Vector getObjectTitle(Context context, String[] args) throws Exception
	{
		return getColumnValues(context, args, SELECT_ATTRIBUTE_TITLE);
	}

	/**
	* This method is used to get the html output for the name of each object in the object list that is passed as arguments.
	* @param context The ematrix context of the request.
	* @param args The String array containing Packed arguments, This also contains list of object ids
	* @return a Vector object containing html output for the name of each object in the object list that is passed as arguments
	* @throws Exception
	* @since Common 11.0
	*/
	public Vector getObjectName(Context context, String[] args) throws Exception
	{
		return getColumnValues(context, args, DomainConstants.SELECT_NAME);
	}

	/**
	* This method is used to get Details Popup links (HTML output) for all objects in the object list that is passed as arguments.
	* @param context The ematrix context of the request.
	* @param args The String array containing Packed arguments, This also contains list of object ids
	* @return a Vector object containing Details Popup links (HTML output) for all objects in the object
	   list that is passed as arguments
	* @throws Exception
	* @since Common 11.0
	*/
	public Vector getObjectLaunch(Context context, String[] args) throws Exception
	{
		return getColumnValues(context, args, LAUNCH);
	}

	/**
	* This method is used to get HTML output for the Revision of each object in the object list that is passed as arguments.
	* @param context The ematrix context of the request.
	* @param args The String array containing Packed arguments, This also contains list of object ids
	* @return a Vector object containing HTML output for the Revision of each object in the object list that is passed as arguments
	* @throws Exception
	* @since Common 11.0
	*/
	public Vector getObjectRevision(Context context, String[] args) throws Exception
	{
		return getColumnValues(context, args, CommonDocument.SELECT_REVISION);
	}


	/**
	* This method is used to get HTML output for Route status icon of each object in the object list that is passed as arguments.
	* @param context The ematrix context of the request.
	* @param args The String array containing Packed arguments, This also contains list of object ids
	* @return a Vector object containing HTML output for Route status icon of each object in the object list that is passed as arguments
	* @throws Exception
	* @since Common 11.0
	*/
	public Vector getObjectRouteStatus(Context context, String[] args) throws Exception
	{
		//XSSOK
		return getColumnValues(context, args, CommonDocument.SELECT_HAS_ROUTE);
	}


	/**
	* This method is used to get HTML output for the table columns based on the column parameter passes.
	* @param context The ematrix context of the request.
	* @param args The String array containing Packed arguments, This also contains list of object ids
	* @param column a String to which HTML output is to be generated
	* @return a Vector object containing HTML output the table columns based on the column parameter passes
	* @throws Exception
	* @since Common 11.0
	*/
	private Vector getColumnValues(Context context, String[] args, String column) throws Exception
	{
		HashMap programMap = ( HashMap ) JPO.unpackArgs( args );
		HashMap paramList = ( HashMap ) programMap.get( "paramList" );
		HashMap columnMap = ( HashMap ) programMap.get( "columnMap" );
		HashMap columnSettings = ( HashMap ) columnMap.get( "settings" );
		MapList objectList = ( MapList )programMap.get( "objectList" );

		String jsTreeId = (String)paramList.get("jsTreeID");
		String parentObjId = (String)paramList.get("objectId");
		String suiteKey = (String)paramList.get("suiteKey");


		String showTypeIcon = (String)columnSettings.get("Show Type Icon");
		boolean showIcon = false;

		if(showTypeIcon != null && "true".equalsIgnoreCase(showTypeIcon))
		{
			showIcon = true;
		}

		/**
		* Printer Friendly Check
		**/
		boolean isprinterFriendly = false;
		if( paramList.get( "reportFormat" ) != null )
		{
			isprinterFriendly = true;
		}


		int iObjectListSize = objectList.size();
		Vector htmlColumnValues = new Vector( iObjectListSize );

		StringList strList = new StringList(2);
		strList.addElement(DomainConstants.SELECT_ID);
		strList.addElement(DomainConstants.SELECT_NAME);
		strList.addElement(DomainConstants.SELECT_TYPE);
		strList.addElement(DomainConstants.SELECT_REVISION);
		strList.addElement(CommonDocument.SELECT_HAS_ROUTE);
		strList.addElement(column);

		String objIdArray[] = new String[ iObjectListSize ];

		for ( int i = 0; i < objectList.size(); i++ )
		{
			Map objMap = (Map)objectList.get( i );
			objIdArray[ i ]  = (String)objMap.get(DomainConstants.SELECT_ID);
		}

		HashMap objectAccessMap = getAccessMap(context, objIdArray);

		ContextUtil.pushContext(context);
		MapList reqMapList = null;
		try
		{
			reqMapList = DomainObject.getInfo(context, objIdArray, strList);
		}
		finally
		{
			ContextUtil.popContext(context);
		}

		if(reqMapList!=null)
		{
			int reqMapListLength = reqMapList.size();

			StringBuffer sbColumValues = new StringBuffer();

			if(LAUNCH.equals(column) || DomainConstants.SELECT_NAME.equals(column))
			{
				StringBuffer sbObjectIcon = new StringBuffer();

				for (int i = 0; i<reqMapListLength; i++)
				{
					sbColumValues.setLength(0);
					Map docMap = (Map) reqMapList.get(i);

					if (docMap.size() == 0)
					{
						htmlColumnValues.add("");
						continue;
					}
					String objId = (String)docMap.get(DomainConstants.SELECT_ID);
					String objName = (String)docMap.get(DomainConstants.SELECT_NAME);
					String objType = (String)docMap.get(DomainConstants.SELECT_TYPE);

					sbObjectIcon.setLength(0);

					if(showIcon)
					{
						String objectIcon = UINavigatorUtil.getTypeIconProperty(context, objType);
						sbObjectIcon.append("<img src=\"../common/images/");
						if (objectIcon == null || objectIcon.length() == 0 )
						{
							sbObjectIcon.append("iconSmallDefault.gif");
						}
						else
						{
							sbObjectIcon.append(objectIcon);
						}
						sbObjectIcon.append("\" border=\"0\"> ");

					}


					if( isprinterFriendly || !hasReadShowAccess(objectAccessMap, objId))
					{
						if(DomainConstants.SELECT_NAME.equals(column))
						{
							if("HTML".equals(paramList.get("reportFormat")))
							{
								sbColumValues.append(sbObjectIcon.toString());
							}
							sbColumValues.append(XSSUtil.encodeForHTML(context, objName));
						}
						else if(LAUNCH.equals(column))
						{
							if("HTML".equals(paramList.get("reportFormat")))
							{
								sbColumValues.append("<img border=\"0\" src=\"../common/images/iconActionNewWindow.gif\" title=\"Open in new window\">");
							}
						}
					}
					else
					{
						sbColumValues.append("<b><a ");
						sbColumValues.append(" href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?mode=insert&DefaultCategory=APPDocumentFiles&emxSuiteDirectory=components&jsTreeID=" );
						sbColumValues.append(XSSUtil.encodeForJavaScript(context, jsTreeId));
						sbColumValues.append("&parentOID=");
						sbColumValues.append(XSSUtil.encodeForJavaScript(context, parentObjId));
						sbColumValues.append("&suiteKey=");
						sbColumValues.append(XSSUtil.encodeForJavaScript(context, suiteKey));
						sbColumValues.append("&objectId=");
						sbColumValues.append(XSSUtil.encodeForJavaScript(context, objId));
						if(LAUNCH.equals(column))
						{
							sbColumValues.append("', '', '', 'false', 'popup','')\"> ");
							sbColumValues.append("<img border=\"0\" src=\"../common/images/iconActionNewWindow.gif\" title=\"Open in new window\">");
						}
						else if(DomainConstants.SELECT_NAME.equals(column))
						{
							sbColumValues.append("', '', '', 'false', 'content','')\"> ");
							sbColumValues.append(sbObjectIcon.toString());
							sbColumValues.append(XSSUtil.encodeForHTML(context, objName));
						}
						sbColumValues.append("</a></b>");
					}
					htmlColumnValues.add( sbColumValues.toString());
				}

			}
			else if(DomainConstants.SELECT_REVISION.equals(column))
			{
				for ( int i = 0;i<reqMapListLength ;i++ )
				{
					sbColumValues.setLength(0);
					Map docMap = ( Map ) reqMapList.get( i );

					if ( docMap.size() == 0)
					{
						htmlColumnValues.add("");
						continue;
					}
					String objId = (String)docMap.get(DomainConstants.SELECT_ID);
					String objType = (String) docMap.get(CommonDocument.SELECT_TYPE);
					String objRev = (String)docMap.get(DomainConstants.SELECT_REVISION);

					String parentType = CommonDocument.getParentType(context, objType);

					if( isprinterFriendly || !parentType.equals(CommonDocument.TYPE_DOCUMENTS)
						|| !hasReadShowAccess(objectAccessMap, objId))
					{
						sbColumValues.append(XSSUtil.encodeForHTML(context, objRev));
					}
					else
					{
						sbColumValues.append("<a ");
						sbColumValues.append(" href =\"javascript:showModalDialog('");
						sbColumValues.append("emxTable.jsp?program=emxCommonDocumentUI:getRevisions&popup=true&table=APPDocumentRevisions&header=emxComponents.Common.RevisionsPageHeading&subHeader=emxComponents.Menu.SubHeaderDocuments&suiteKey=Components");
						sbColumValues.append("&objectId=");
						sbColumValues.append(XSSUtil.encodeForJavaScript(context, objId));
						sbColumValues.append("',730,450)\">");
						sbColumValues.append(XSSUtil.encodeForHTML(context, objRev));
						sbColumValues.append("</a>");
					}
					htmlColumnValues.add( sbColumValues.toString());
				}
			}
			else if(CommonDocument.SELECT_HAS_ROUTE.equals(column))
			{
				for ( int i = 0;i<reqMapListLength ;i++ )
				{
					Map docMap = ( Map ) reqMapList.get( i );
					String routeId = null;

					if ( docMap.size() == 0)
					{
						htmlColumnValues.add("");
						continue;
					}
					try
					{
						routeId = (String) docMap.get(CommonDocument.SELECT_HAS_ROUTE);

					} catch (ClassCastException cex)
					{
						StringList routeIds = (StringList) docMap.get(CommonDocument.SELECT_HAS_ROUTE);
						routeId = (String)routeIds.get(0);
					}

					String showObjectRoute = "&nbsp;";

					if (routeId != null && FrameworkUtil.isObjectId(context,routeId))
					{
						showObjectRoute = "<img border='0' src='../common/images/iconSmallRoute.gif' alt=''>";
					}
					//XSSOK
					htmlColumnValues.add(showObjectRoute);
				}
			}
			else
			{
				for ( int i = 0;i<reqMapListLength ;i++ )
				{
					Map docMap = ( Map ) reqMapList.get( i );

					if ( docMap.size() == 0)
					{
						htmlColumnValues.add("");
						continue;
					}
					String objColumnValue = (String)docMap.get(column);
					htmlColumnValues.add(objColumnValue);
				}
			}
		}
		return htmlColumnValues;
	}


	/**
	* This method is used to get HTML output for the lock status of each object in the object list that is passed as arguments.
	* @param context The ematrix context of the request.
	* @param args The String array containing Packed arguments, This also contains list of object ids
	* @return a Vector object containing HTML output for the lock status of each object in the object list that is passed as arguments
	* @throws Exception
	* @since Common 11.0
	*/
	public Vector getLockStatus(Context context, String[] args) throws Exception
	{
		//XSSOK
		return getDocumentUIColumnValues(context, args, LOCK);
	}

	/**
	* This method is used to get HTML output for the Version of each object in the object list that is passed as arguments.
	* @param context The ematrix context of the request.
	* @param args The String array containing Packed arguments, This also contains list of object ids
	* @return a Vector object containing HTML output for the Version of each object in the object list that is passed as arguments
	* @throws Exception
	* @since Common 11.0
	*/
	public Vector getVersionStatus(Context context, String[] args) throws Exception
	{
		return getDocumentUIColumnValues(context, args, VERSION);
	}


	/**
	* This method is used to get HTML output for the Document Action Icons of each object in the object list that
	  is passed as arguments.
	* @param context The ematrix context of the request.
	* @param args The String array containing Packed arguments, This also contains list of object ids
	* @return a Vector object containing HTML output for the Document Action Icons of each object in the object
	   list that is passed as arguments
	* @throws Exception
	* @since Common 11.0
	*/
	public Vector getDocumentActions(Context context, String[] args) throws Exception
	{
		return getDocumentUIColumnValues(context, args, ACTIONS);
	}

	/**
	* This is a wraper method that internally calls emxCommonDocumentUI JPO methods to get respective values.
	* @param context The ematrix context of the request.
	* @param args The String array containing Packed arguments, This also contains list of object ids
	* @param column a String to which HTML output is to be generated
	* @return a Vector object containing HTML output the table columns based on the column parameter passes
	* @throws Exception
	* @since Common 11.0
	*/
	private Vector getDocumentUIColumnValues(Context context, String[] args, String column) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs( args );
		HashMap paramList = (HashMap) programMap.get( "paramList" );
		MapList objectList = (MapList)programMap.get( "objectList" );

		String reportFormat = (String)paramList.get("reportFormat");

		int iObjectListSize = objectList.size();

		String objIdArray[] = new String[ iObjectListSize ];
		ArrayList objIdList = new ArrayList(iObjectListSize);

		for ( int i = 0; i < objectList.size(); i++ )
		{
			Map objMap = (Map)objectList.get( i );
			objIdArray[ i ]  = (String)objMap.get(DomainConstants.SELECT_ID);
			objIdList.add(objIdArray[ i ]);
		}

		MapList accessibleObjIds = new MapList();
		MapList unAccessibleObjIds = new MapList();

		ArrayList alAccessibleObjIds = new ArrayList();
		ArrayList alUnAccessibleObjIds = new ArrayList();

		HashMap objectAccessMap = getAccessMap(context, objIdArray);

		for(int i=0; i<iObjectListSize; i++)
		{
			Map objMap = (Map)objectList.get( i );

			if(hasReadShowAccess(objectAccessMap, objIdArray[i]))
			{
				accessibleObjIds.add(objMap);
				alAccessibleObjIds.add(objIdArray[i]);
			}
			else
			{
				unAccessibleObjIds.add(objMap);
				alUnAccessibleObjIds.add(objIdArray[i]);
			}
		}


		Vector accessibleColumnValues = null;
		Vector unAccessibleColumnValues = null;
		Vector finalColumnValues = new Vector(iObjectListSize);

		try
		{
			emxCommonDocumentUI_mxJPO objCommonDocumentUI = new emxCommonDocumentUI_mxJPO(context,null);
			programMap.put("objectList",accessibleObjIds);
			if(LOCK.equals(column))
			{
				ContextUtil.pushContext(context);
				try
				{
					accessibleColumnValues = objCommonDocumentUI.getLockStatus(context, JPO.packArgs(programMap));
				}
				finally
				{
					ContextUtil.popContext(context);
				}
			}
			else if(VERSION.equals(column))
			{
				ContextUtil.pushContext(context);
				try
				{
					accessibleColumnValues = objCommonDocumentUI.getVersionStatus(context, JPO.packArgs(programMap));
				}
				finally
				{
					ContextUtil.popContext(context);
				}
			}
			else if(ACTIONS.equals(column))
			{
				accessibleColumnValues = objCommonDocumentUI.getDocumentActions(context, JPO.packArgs(programMap));
			}


			programMap.put("objectList",unAccessibleObjIds);
			((Map)programMap.get("paramList")).put("reportFormat","HTML");
			if(LOCK.equals(column))
			{
				ContextUtil.pushContext(context);
				try
				{
					unAccessibleColumnValues = objCommonDocumentUI.getLockStatus(context, JPO.packArgs(programMap));
				}
				finally
				{
					ContextUtil.popContext(context);
				}
			}else if(VERSION.equals(column))
			{
				ContextUtil.pushContext(context);
				try
				{
					unAccessibleColumnValues = objCommonDocumentUI.getVersionStatus(context, JPO.packArgs(programMap));
				}
				finally
				{
					ContextUtil.popContext(context);
				}
			}

			for(int i=0; i<iObjectListSize; i++)
			{
				String strObjectId = objIdArray[i];
				int objIndex =  -1;

				if(accessibleColumnValues !=null && (objIndex = alAccessibleObjIds.indexOf(strObjectId)) != -1)
				{
					finalColumnValues.add(i, accessibleColumnValues.get(objIndex));
				}
				else if(unAccessibleColumnValues != null && (objIndex = alUnAccessibleObjIds.indexOf(strObjectId)) != -1)
				{
					finalColumnValues.add(i, unAccessibleColumnValues.get(objIndex));
				}
				else
				{
					finalColumnValues.add(i, "");
				}
			}
		}
		finally
		{
			programMap.put("objectList",objectList);
			if(reportFormat!=null)
			{
				((Map)programMap.get("paramList")).put("reportFormat",reportFormat);
			}
			else
			{
				((Map)programMap.get("paramList")).remove("reportFormat");
			}
		}
		//XSSOK
		return finalColumnValues;
	}


	/**
	* This method is used to get access masks(for the context user) of each object in the object list that is passed as arguments.
	* @param context The ematrix context of the request.
	* @param objectIds a String array containing Object Ids
	* @return HashMap object containing map of object id and its access mask for the context user
	   list that is passed as arguments
	* @throws Exception
	* @since Common 11.0
	*/
	private HashMap getAccessMap(Context context, String[] objectIds) throws Exception
	{
		try
		{
			StringList strList = new StringList(2);
			strList.addElement(DomainConstants.SELECT_ID);
			strList.addElement("current.access");

			MapList accessList = DomainObject.getInfo( context, objectIds, strList );

			HashMap docObjectAccessMap = new HashMap(objectIds.length);

			for(int i=0; i<accessList.size(); i++)
			{
				Map tempAccessMap = (Map)accessList.get(i);
				String objectId = (String)tempAccessMap.get(DomainConstants.SELECT_ID);
				String accessMask = (String)tempAccessMap.get("current.access");
				docObjectAccessMap.put(objectId, accessMask);
			}
			return docObjectAccessMap;
		}
		catch(Exception e)
		{
			System.out.println(e);
			return (new HashMap(0));
		}
	}


	/**
	* This method is used to check whether user has Read and Show access on object
	* @param objAccessMap a HashMap of Object Id to Access Mask.
	* @param objId a String containing Object Id
	* @return boolean true if object has read and show access otherwise false
	* @since Common 11.0
	*/

	private boolean hasReadShowAccess(HashMap objAccessMap, String objId)
	{
		if(objAccessMap != null)
		{
			String objAccessMask = (String)objAccessMap.get(objId);
			if(objAccessMask==null || "null".equalsIgnoreCase(objAccessMask)
				|| (objAccessMask.indexOf("all") == -1 && (objAccessMask.indexOf("read") == -1
				||  objAccessMask.indexOf("show") == -1)))
			{
				return false;
			}
		}
		return true;
	}


	/**
	* This method is used to get HTML output for Reviewer each object in the object list that is passed as arguments.
	* @param context The ematrix context of the request.
	* @param args The String array containing Packed arguments, This also contains list of object ids
	* @return a Vector object containing HTML output for the Reviewer of each object in the object list that is passed as arguments
	* @throws Exception
	* @since Common 11.0
	*/

	public Vector showAssignedReviewer(Context context, String[] args) throws Exception
	{

		HashMap programMap = ( HashMap ) JPO.unpackArgs( args );
		HashMap paramList = ( HashMap ) programMap.get( "paramList" );
		HashMap columnMap = ( HashMap ) programMap.get( "columnMap" );
		HashMap columnSettings = ( HashMap ) columnMap.get( "settings" );
		MapList objectList = ( MapList )programMap.get( "objectList" );

		String parentObjId = (String)paramList.get("objectId");
		String suiteKey = (String)paramList.get("suiteKey");

		/**
		* Printer Friendly Check
		**/
		boolean isprinterFriendly = false;
		if( paramList.get( "reportFormat" ) != null )
		{
			isprinterFriendly = true;
		}


		int iObjectListSize = objectList.size();
		Vector htmlReviewers = new Vector( iObjectListSize );

		StringList strList = new StringList(3);
		strList.addElement(DomainConstants.SELECT_ID);
		strList.addElement(DomainConstants.SELECT_OWNER);
		strList.addElement(DomainConstants.SELECT_CURRENT);

		String objIdArray[] = new String[ iObjectListSize ];

		for ( int i = 0; i < objectList.size(); i++ )
		{
			Map objMap = (Map)objectList.get( i );
			objIdArray[ i ]  = (String)objMap.get(DomainConstants.SELECT_ID);
		}

		HashMap objectAccessMap = getAccessMap(context, objIdArray);

		MapList reqMapList = DomainObject.getInfo(context, objIdArray, strList);


		for ( int i = 0, reqMapListLength = reqMapList.size();i<reqMapListLength ;i++ )
		{
			Map docMap = ( Map ) reqMapList.get( i );

			if ( docMap.size() == 0)
			{
				htmlReviewers.add("");
				continue;
			}
			String objId = (String)docMap.get(DomainConstants.SELECT_ID);
			String objOwner = (String)docMap.get(DomainConstants.SELECT_OWNER);
			String objState = (String)docMap.get(DomainConstants.SELECT_CURRENT);

			objOwner = PersonUtil.getFullName(context,objOwner);
			String strSubmitted = FrameworkUtil.lookupStateName(context, POLICY_DOWNLOAD_ACCESS_REQUEST, SYMB_SUBMITTED);
			if(objState!=null && objState.equals(strSubmitted))
			{
				htmlReviewers.add("");
			}
			else
			{
				htmlReviewers.add(XSSUtil.encodeForHTML(context, objOwner));
			}
		}
		return htmlReviewers;

	}

/**
    * This Access program is used to check for Library Central installation
    * @param context The ematrix context of the request.
    * @return a boolean
    * @throws Exception
    * @since since Common V6R2009x
    */
     public boolean checkForLibraryCentral(Context context,String[] args)throws Exception
       {
           boolean libCentralStatus = false;
           try{

               String command = "print program eServiceSystemInformation.tcl select property[appVersionLibraryCentral] dump |";
               String result  = MqlUtil.mqlCommand(context,command);

               if(!result.equals(""))
               {
                   libCentralStatus = true;
               }
           }catch(Exception ex){
               ex.printStackTrace();
           }
           return libCentralStatus;
       }

} // Class End
