/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.ExpansionIterator;
import matrix.db.JPO;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.RouteTemplate;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.engineering.Part;
import com.matrixone.apps.engineering.TBEUtil;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * The <code>emxENCFullSearchBase</code> class contains implementation code for emxENCFullSearchBase.
 *
  */
public class emxENCFullSearchBase_mxJPO
{
	static HashMap _typeHierarchy = null;
	static HashMap _typeParent = null;
		// The operator symbols
	/** A string constant with the value &&. */
	protected static final String SYMB_AND = " && ";
	/** A string constant with the value ~~. */
	protected static final String SYMB_MATCH = " ~~ ";
	/** A string constant with the value !=. */
	protected static final String SYMB_NOT_EQUAL = " != ";
	/** A string constant with the value '. */
	protected static final String SYMB_QUOTE = "'";
	/** A string constant with the value *. */
	protected static final String SYMB_WILD = "*";
	/** A string constant with the value (. */
	protected static final String SYMB_OPEN_PARAN = "(";
	/** A string constant with the value ). */
	protected static final String SYMB_CLOSE_PARAN = ")";
	/** A string constant with the value attribute. */
	protected static final String SYMB_ATTRIBUTE = "attribute";
	/** A string constant with the value [. */
	protected static final String SYMB_OPEN_BRACKET = "[";
	/** A string constant with the value ]. */
	protected static final String SYMB_CLOSE_BRACKET = "]";
	/** A string constant with the value to. */
	protected static final String SYMB_TO = "to";
	/** A string constant with the value from. */
	protected static final String SYMB_FROM = "from";
	/** A string constant with the value ".". */
	protected static final String SYMB_DOT = ".";
	/** A string constant with the value "null". */
	protected static final String SYMB_NULL = "null";

	/**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     * @since MBOM X-3.
     */
    public emxENCFullSearchBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super();
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return an int.
     * @throws Exception if the operation fails.
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception("must specify method on search invocation");
        }
        return 0;
    }
/**
  * searchNotIsVertionTrue() method returns Part policy not equals to Manufacturing Part and type not equals to Phantom Part.
* @param context Context : User's Context.
* @param args String array parameter having Part's details
* @return The StringList value of searchNotIsVertionTrue.
* @throws Exception if searching Parts object fails.
* @since X3
* @author Suman Kumar
*/
	public StringList searchNotIsVertionTrue(Context context, String[] args)
			throws Exception
	{
		String sWhereExp ="";
		String isVersion="";
		String objectId="";
		String attIsVersion=PropertyUtil.getSchemaProperty(context, "attribute_IsVersion");
		StringList resultList=new StringList();
		HashMap object;

		HashMap params = (HashMap)JPO.unpackArgs(args);
		String parentId = (String) params.get("objectId");
//Check parentId is null
if(parentId!=null){
	try{
//Search query
	String queryLimit = "";
		if (queryLimit == null || queryLimit.equals("null") || queryLimit.equals("")) {
			queryLimit = "0";
			}

//Added for X-3-Start
	isVersion="attribute["+attIsVersion+"]==TRUE";//Added for X-3
		if (sWhereExp == null || sWhereExp.equalsIgnoreCase("null") || sWhereExp.length()<=0 ) {
			sWhereExp = isVersion;//Modified for X-3
			} else {
			sWhereExp += isVersion;//Modified for X-3
		}
//Declare display variables
	SelectList resultSelects = new SelectList(1);
	resultSelects.add(DomainObject.SELECT_ID);
	resultSelects.add(DomainObject.SELECT_NAME);

		MapList totalresultList = DomainObject.findObjects(context,
															  DomainConstants.TYPE_PART,
				  DomainConstants.QUERY_WILDCARD,
				  DomainConstants.QUERY_WILDCARD,
				  DomainConstants.QUERY_WILDCARD,
				  DomainConstants.QUERY_WILDCARD,
				  sWhereExp,
				  null,
				  true,
				  resultSelects,
				  Short.parseShort(queryLimit),
				  DomainConstants.QUERY_WILDCARD,
															  DomainConstants.EMPTY_STRING // search text
				  );

	Iterator itr = totalresultList.iterator();
	while (itr.hasNext()){
		object = (HashMap) itr.next();
		objectId = (String) object.get(DomainConstants.SELECT_ID);
		resultList.add(objectId);
		}//End of for loop
	}catch(Exception e){
		throw e;
	}//End of Catch
}
	return resultList;
	}//End of searchNotIsVertionTrue


/**
  * excludeConnectedAlternateOIDs() method returns MCO objects data depending search criteria
* @param context Context : User's Context.
* @param args String array parameter having Part's details
* @return The StringList value of excludeConnectedAlternateOIDs.
* @throws Exception if searching Parts object fails.
* @since X3
* @author Suman Kumar
*/
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeConnectedAlternateOIDs(Context context, String[] args)
		throws Exception
	{
		HashMap params = (HashMap)JPO.unpackArgs(args);
		String parentId = (String)params.get("objectId");

		Map object;
		String objectId="";
		StringList resultExclude=new StringList();

//Check parentId is null
if(parentId!=null){
	try{

//Exclude context part
		resultExclude.add(parentId);

		DomainObject domConnectId=new DomainObject(parentId);
MapList totalConnectedList = null;
	//selectables for query
		StringList busSelList = new StringList();
		StringList relSelList = new StringList();
		busSelList.add(DomainObject.SELECT_ID);
		busSelList.add(DomainObject.SELECT_NAME);
		relSelList.add(DomainRelationship.SELECT_ID);

	totalConnectedList=domConnectId.getRelatedObjects(context,
										DomainConstants.RELATIONSHIP_ALTERNATE,
										DomainConstants.TYPE_PART,
										busSelList,
										relSelList,
										false,
										true,
										(short)1,
										null,
										DomainConstants.EMPTY_STRING);

//Gettinf all connected Object Id
	Iterator itr = totalConnectedList.iterator();
	while (itr.hasNext()){
		object = (Map) itr.next();
		objectId = (String) object.get(DomainConstants.SELECT_ID);
		resultExclude.add(objectId);
		}//End of for loop
	
	// Added For Alternate/Substitute for ENG by D2E -- Start
	
	String  RELATIONSHIP_EBOM_SUBSTITUTE =   PropertyUtil.getSchemaProperty("relationship_EBOMSubstitute");
	String substituteSelect = "frommid["+RELATIONSHIP_EBOM_SUBSTITUTE+"].to.id";
	
	MapList substituteConnectedList = domConnectId.getRelatedObjects(context, 
																		DomainConstants.RELATIONSHIP_EBOM,
																		DomainConstants.TYPE_PART, 
																		new StringList(DomainConstants.SELECT_ID), 
																		new StringList(substituteSelect), 
																		true, false, (short)1, 
																		null, null, 0);
	
	Map substituteInfo;
	for(int i=0; i<substituteConnectedList.size();i++){
		substituteInfo = (Map)substituteConnectedList.get(i);
		if(substituteInfo.get(substituteSelect)!=null){
			if(substituteInfo.get(substituteSelect) instanceof StringList)
				resultExclude.addAll((StringList)substituteInfo.get(substituteSelect));
			else
				resultExclude.addElement((String)substituteInfo.get(substituteSelect));
		}
	}
	
	// Added For Alternate/Substitute for ENG by D2E -- End 

//Searching All Policy!="Manufacturing Part" && attribute[is Vertion].value!=TRUE
	StringList sIsVertionPartList=searchNotIsVertionTrue(context, args);
	int sIsVertionListSize=sIsVertionPartList.size();
	for(int i=0;i<sIsVertionListSize;i++){
		String sManuPartListId=(String)sIsVertionPartList.elementAt(i);
		resultExclude.add(sManuPartListId);
		}//End of for loop



	}catch(Exception e){
		throw e;
	}//End of Catch
}//End of if loop
	return resultExclude;
}//End of excludeConnectedAlternateOIDs method


	/******* Added For ALTERNATE/SUNSTITUTE for ENG by D2E -- Start ******/

	/**
	* this method excludes connected alternate OIDs.
	* @param context Context : User's Context.
	* @param args String array parameter having Part's details
	* @return The StringList value of excludeOIDs.
	* @throws Exception if searching Parts object fails.
	* @author D2E
	*/
	
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	
	public StringList excludeOIDsForSub(Context context, String[] args)
		throws Exception {
		
		HashMap params = (HashMap)JPO.unpackArgs(args);
		String partId = (String)params.get("sEBOMPartId");
		String excludeIds = (String)params.get("excludeOID");
		
		StringList excludeOIDs = new StringList();
		
		if (UIUtil.isNotNullAndNotEmpty(excludeIds)) {
			excludeOIDs.addAll(FrameworkUtil.splitString(excludeIds, ","));
		}
		
		String excludeAlternateIds = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3", partId, "from["+DomainRelationship.RELATIONSHIP_ALTERNATE+"].to.id", "|");
		
		if (UIUtil.isNotNullAndNotEmpty(excludeAlternateIds)) {		
			excludeOIDs.addAll(FrameworkUtil.splitString(excludeIds, "|"));
		}
		
		return excludeOIDs;
	}
	
	/******* Added For ALTERNATE/SUNSTITUTE for ENG by D2E -- End ******/

	/**
	  * excludeConnectedSpareOIDs() method returns OIDS already connected to the given Part objects data depending search criteria
	  * @param context Context : User's Context.
	  * @param args String array parameter having Part's details
	  * @return The StringList value of excludeConnectedAlternateOIDs.
	  * @throws Exception if searching Parts object fails.
	  * @since R212
	  * @author cv4
	  */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeConnectedSpareOIDs(Context context, String[] args) throws Exception {
		HashMap params = (HashMap)JPO.unpackArgs(args);
		String objectId = (String)params.get("objectId");

		StringList resultExclude=new StringList();
		if(objectId != null){
			DomainObject domConnectId=new DomainObject(objectId);
			resultExclude = domConnectId.getInfoList(context,"from["+DomainConstants.RELATIONSHIP_SPARE_PART +"].to.id");
			resultExclude.add(objectId);
		}

		return resultExclude;
	}

/**
  * getEBOMs() method returns Parts objects data connected with context part with EBOM Relationship.
* @param context Context : User's Context.
* @param args String array parameter having Part's details
* @return The StringList value of getEBOMs.
* @throws Exception if searching Parts object fails.
* @since X3
* @author Suman Kumar
*/
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList getEBOMs(Context context, String[] args)
		throws Exception
	{
		HashMap params = (HashMap)JPO.unpackArgs(args);
		String parentId = (String) params.get("objectId");

		Map object;
		String objectId="";
		StringList finalEBOMList=new StringList();

		StringList slAllPart=new StringList();

//Check parentId is null
if(parentId!=null){
	try{
//Search query
	String queryLimit = "";
		if (queryLimit == null || queryLimit.equals("null") || queryLimit.equals("")) {
			queryLimit = "0";
			}
		DomainObject domConnectId=new DomainObject(parentId);


	//selectables for query
		StringList busSelList = new StringList();
		StringList relSelList = new StringList();
		busSelList.add(DomainObject.SELECT_ID);
		busSelList.add(DomainObject.SELECT_NAME);
		relSelList.add(DomainRelationship.SELECT_ID);

	MapList    totalConnectedList=(MapList)domConnectId.getRelatedObjects(context,
																		DomainConstants.RELATIONSHIP_EBOM,
																		DomainConstants.TYPE_PART,
																		busSelList,
																		relSelList,
																		false,
																		true,
																		(short)1,
																		null,
																		null);


//Gettinf all connected Object Id
	Iterator itr = totalConnectedList.iterator();
		StringList slEBOMPart=new StringList();
	while (itr.hasNext()){
		object = (Map) itr.next();
		objectId = (String) object.get(DomainConstants.SELECT_ID);
		slEBOMPart.add(objectId);
	}//End of for loop

//Declare display variables
	SelectList resultSelects = new SelectList(1);
	resultSelects.add(DomainObject.SELECT_ID);
	resultSelects.add(DomainObject.SELECT_NAME);

		MapList totalresultList = DomainObject.findObjects(context,
															  DomainConstants.TYPE_PART,
															  DomainConstants.QUERY_WILDCARD,
															  DomainConstants.QUERY_WILDCARD,
															  DomainConstants.QUERY_WILDCARD,
															  DomainConstants.QUERY_WILDCARD,
															  DomainConstants.EMPTY_STRING,
															  null,
															  true,
															  resultSelects,
															  Short.parseShort(queryLimit),
															  DomainConstants.QUERY_WILDCARD,
															  DomainConstants.EMPTY_STRING // search text
															  );

	itr = totalresultList.iterator();
		while (itr.hasNext()){
			object = (HashMap) itr.next();
			objectId = (String) object.get(DomainConstants.SELECT_ID);
			slAllPart.add(objectId);
			}
//Getting EBOM Part
		for(int i=0;i<slAllPart.size();i++){
			String sObjId=(String)slAllPart.elementAt(i);
			if(!slEBOMPart.contains(sObjId)){
				finalEBOMList.add(sObjId);
			}//End of if loop
		}//End of for loop
	}catch(Exception e){
		throw e;
	}//End of Catch
}//End of if loop
return finalEBOMList;
	}//End of getEBOMs

/**
  * excludeConnectedSubstituteOIDs() method returns Parts objects data except selected EBOM and connected Substitute with selected EBOM.
* @param context Context : User's Context.
* @param args String array parameter having Part's details
* @return The StringList value of excludeConnectedSubstituteOIDs.
* @throws Exception if searching Parts object fails.
* @since X3
* @author Suman Kumar
*/
	public StringList excludeConnectedSubstituteOIDs(Context context, String[] args)
		throws Exception
	{
		String RELATIONSHIP_EBOM_SUBSTITUTE = PropertyUtil.getSchemaProperty(context,"relationship_EBOMSubstitute");
		HashMap params = (HashMap)JPO.unpackArgs(args);
		String parentId = (String) params.get("objectId");
		String sRelId = (String) params.get("sRelId");
		String sEBOMPartId = (String) params.get("sEBOMPartId");
		String sSubsPartId="";
		StringList resultExclude=new StringList();
//Check parentId is null
if(parentId!=null){
	try{
//Excluding selected EBOM Part Id
		resultExclude.add(sEBOMPartId);
//Excluding context part
		resultExclude.add(parentId);

//All connected Substitute Part with selected EBOM Rel Id.
String  sQuery3 = "print connection $1 select $2 dump $3;";
	String sSubspartIdResult=MqlUtil.mqlCommand(context,sQuery3,sRelId,"frommid["+RELATIONSHIP_EBOM_SUBSTITUTE+"].to.id","|");
//If selected EBOM Rel Id id not connected with Subs Part.
	if(!"".equals(sSubspartIdResult)){
		StringList slSubsPartId = FrameworkUtil.split(sSubspartIdResult, "|");
		int size=slSubsPartId.size();
		for (int i=0; i<size; i++){
			sSubsPartId = (String)slSubsPartId.get(i);
			resultExclude.add(sSubsPartId);
		}//End of for loop
	}//End of if loop

//Searching All Policy!="Manufacturing Part" && attribute[is Vertion].value!=TRUE
	StringList sIsVertionPartList=searchNotIsVertionTrue(context, args);
	int sIsVertionListSize=sIsVertionPartList.size();

	for(int i=0;i<sIsVertionListSize;i++){
		String sManuPartListId=(String)sIsVertionPartList.elementAt(i);
		resultExclude.add(sManuPartListId);
		}//End of for loop
	}catch(Exception e){
		throw e;
	}//End of Catch
}//End of if loop
	return resultExclude;
	}//End of excludeConnectedSubstituteOIDs method

/**
  * excludeOIDPersons() method returns Person objects that do not belong to the same business unit.
* @param context Context : User's Context.
* @param args String array parameter having Change Details
* @return The StringList value of excludeOIDPersons.
* @throws Exception if searching Person object fails.
* @since X3
*/

@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
public StringList excludeOIDPersons(Context context, String[] args)
		throws Exception
{
	String memberrel  = PropertyUtil.getSchemaProperty(context,"relationship_Member");
	String sPerson  = PropertyUtil.getSchemaProperty(context,"type_Person");

	HashMap params = (HashMap)JPO.unpackArgs(args);

	String strOrgId = (String) params.get("orgId");

	MapList totalresultList = null;
	StringList strlExcludeOIDPersons = new StringList();

	if(strOrgId != null)
	{

		String strWhereClause =  null;

		strWhereClause = "!(to[" + memberrel + "].from.id == " + strOrgId + ")";

		StringList strlPersonSelects = new StringList(1);
		strlPersonSelects.add(DomainConstants.SELECT_ID);

		totalresultList = DomainObject.findObjects(context,
												 sPerson,
												 "*",
												 "*",
												 "*",
												 "*",
												 strWhereClause,
												 null,
												 true,
												 strlPersonSelects,
												 (short) 0);

		Iterator itrPersons = totalresultList.iterator();

		while (itrPersons.hasNext())
		{
			Map mapPerson = (Map) itrPersons.next();
			String strPersonId = (String) mapPerson.get(DomainConstants.SELECT_ID);
			strlExcludeOIDPersons.add(strPersonId);
		}
	}
	return strlExcludeOIDPersons;
}

/**
  * excludeOIDChangeObjs() method returns Person objects that do not belong to the same business unit.
* @param context Context : User's Context.
* @param args String array parameter having Change Object Details
* @return The StringList value of excludeOIDChangeObjs.
* @throws Exception if searching Person object fails.
* @since X3
*/

	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeOIDChangeObjs(Context context, String[] args)
			throws Exception
		{


		HashMap params = (HashMap)JPO.unpackArgs(args);

		String sRelDesignResponsibility = PropertyUtil.getSchemaProperty(context,"relationship_DesignResponsibility");

		String strObjectId = (String) params.get("objectId");

		StringList strlExcludeOIDChangeObjs = new StringList();

		if (strObjectId != null)
		{

			DomainObject doPart = new DomainObject(strObjectId);

			String strRDOId = doPart.getInfo(context, "to[" + sRelDesignResponsibility + "].from.id");

			if (strRDOId != null && strRDOId.length() > 0)
			{
				String strWhereClause = null;

				StringList strlChangeObjselects = new StringList(2);
				strlChangeObjselects.add(DomainConstants.SELECT_ID);
				strlChangeObjselects.add("to[" + sRelDesignResponsibility + "].from.id");

				MapList totalresultList = DomainObject.findObjects(context,
																 DomainConstants.TYPE_ECO,
																 "*",
																 "*",
																 "*",
																 "*",
																 strWhereClause,
																 null,
																 true,
																 strlChangeObjselects,
																 (short) 0);

				Iterator itrChangeObjs = totalresultList.iterator();

				while (itrChangeObjs.hasNext())
				{
					Map mapECO = (Map) itrChangeObjs.next();

					String strECOId = (String) mapECO.get(DomainConstants.SELECT_ID);
                    String strECORDOId = "";
					//Fix for the IR-025254
                    //String strECORDOId = (String) mapECO.get("to[" + sRelDesignResponsibility + "].from.id");
                    try
                    {
                        strECORDOId = (String) mapECO.get("to[" + sRelDesignResponsibility + "].from.id");
                    }
                    catch (Exception e)
                    {
                        strECORDOId =  ((String)((StringList) mapECO.get("to[" + sRelDesignResponsibility + "].from.id")).get(0));
                    }
                    //IR-025254 ends

                    if (strECORDOId != null && strECORDOId.length() > 0 && !strRDOId.equals(strECORDOId))
					{
						strlExcludeOIDChangeObjs.add(strECOId);
					}
				}
			}

			StringList objectSelects = new StringList(1);
			objectSelects.addElement(DomainConstants.SELECT_ID);

			MapList mapListChangeObjs = doPart.getRelatedObjects( context,
												 //PropertyUtil.getSchemaProperty(context,"relationship_AffectedItem") + "," + PropertyUtil.getSchemaProperty(context,"relationship_NewPartPartRevision") + "," + PropertyUtil.getSchemaProperty(context,"relationship_NewSpecificationSpecificationRevision") + "," + PropertyUtil.getSchemaProperty(context,"relationship_RequestSpecificationRevision") + "," + PropertyUtil.getSchemaProperty(context,"relationship_RequestPartRevision") + "," + PropertyUtil.getSchemaProperty(context,"relationship_RequestPartObsolescence"),
                                                 PropertyUtil.getSchemaProperty(context,"relationship_AffectedItem"),
												 PropertyUtil.getSchemaProperty(context,"type_Change"),
												 objectSelects,
												 null,
												 true,
												 false,
												 (short)1,
												 null,
												 null);

			Iterator itrChangeObjs = mapListChangeObjs.iterator();

			while (itrChangeObjs.hasNext())
			{
				Map mapChangeObj = (Map) itrChangeObjs.next();

				String strChangeObjId = (String) mapChangeObj.get(DomainConstants.SELECT_ID);

				strlExcludeOIDChangeObjs.add(strChangeObjId);
			}

		}

		return strlExcludeOIDChangeObjs;

	}


	/**
	  * Gets all the parent parts for the given part at all levels.
	  *   This is used by the Full Text search as a selectable.
	  *
	  * @param context the eMatrix <code>Context</code> object
	  * @param args holds the HashMap containing the following arguments
	  *      objectId - contains the part id.
	  * @return String - String containing the list of parent part ids.
	  * @throws Exception if the operation fails
	  * @since  EngineeringCentral BX3
	  *
	  */
	 public String getWhereUsedEBOM(Context context, String[] args) throws Exception
	 {
		 String partId = args[0];

		 DomainObject doPart = new DomainObject(partId);

		 SelectList objSelects = new SelectList(1);
		 objSelects.add(DomainConstants.SELECT_ID);

		 String retValue = "";
		 String parentPartId = "";

		  String strType = (String) doPart.getInfo(context, DomainConstants.SELECT_TYPE);

		  if (mxType.isOfParentType(context,strType,DomainConstants.TYPE_PART))
		  {

			  ContextUtil.startTransaction(context, true);
			 try
			 {
                 StringList relSelects = new StringList();
                 MapList ebomList = FrameworkUtil.toMapList(doPart.getExpansionIterator(context, DomainConstants.RELATIONSHIP_EBOM, "*",
                         objSelects, relSelects, true, false, (short)0,
                            null, null, (short)0,
                            false, false, (short)0, false),
                            (short)0, null, null, null, null);

				 if (ebomList != null)
				 {

					 Iterator itr = ebomList.iterator();

					 while (itr.hasNext())
					 {
						 Map mapObject = (Map)itr.next();
						 parentPartId = (String)mapObject.get(DomainConstants.SELECT_ID);
						 retValue = retValue + matrix.db.SelectConstants.cSelectDelimiter + parentPartId;
					 }
				 }
				 ContextUtil.commitTransaction(context);
			 }
			 catch (Exception err)
			 {
				 ContextUtil.abortTransaction(context);
				 throw err;
			 }

		 }

		 return retValue;
	 }


/**
  * excludeOIDNonLeadOrganizations() method returns organization objects without ECR Chairman and Cordinator roles.
* @param context Context : User's Context.
* @param args String array parameter having Change Details
* @return The StringList value of excludeOIDNonLeadOrganizations.
* @throws Exception if searching Organization object fails.
* @since X3
*/

@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
public StringList excludeOIDNonLeadOrganizations(Context context, String[] args)
		throws Exception
{
	String leadresponsibilityrel  = PropertyUtil.getSchemaProperty(context,"relationship_LeadResponsibility");

	String sOrganization  = PropertyUtil.getSchemaProperty(context,"type_Organization");
	String sProjectSpace  = PropertyUtil.getSchemaProperty(context,"type_ProjectSpace");
	String sProjectRole  = PropertyUtil.getSchemaProperty(context,"attribute_ProjectRole");

	StringList strlExcludeOIDNonLeadOrganizations = new StringList();

	StringList strlNonLeadOrganizationselects = new StringList(1);
	strlNonLeadOrganizationselects.add(DomainConstants.SELECT_ID);
	strlNonLeadOrganizationselects.add("from[" + leadresponsibilityrel + "].attribute[" + sProjectRole  + "]");

	MapList totalresultList = DomainObject.findObjects(context,
												 sOrganization + "," + sProjectSpace,
												 "*",
												 "*",
												 "*",
												 "*",
												 null,
												 null,
												 true,
												 strlNonLeadOrganizationselects,
												 (short) 0);

	Iterator itrNonLeadOrganizations = totalresultList.iterator();

	while (itrNonLeadOrganizations.hasNext())
	{
		Map mapOrganization = (Map) itrNonLeadOrganizations.next();
		String strOrganizationId = (String) mapOrganization.get(DomainConstants.SELECT_ID);
		String strLeadResps = (String) mapOrganization.get("from[" + leadresponsibilityrel + "].attribute[" + sProjectRole  + "]");

		if (strLeadResps != null)
		{
			if (!((strLeadResps.indexOf("role_ECRCoordinator") != -1) && (strLeadResps.indexOf("role_ECRChairman") != -1)))
			{
				strlExcludeOIDNonLeadOrganizations.add(strOrganizationId);
			}
		}
		else
		{
			strlExcludeOIDNonLeadOrganizations.add(strOrganizationId);
		}
	}

	return strlExcludeOIDNonLeadOrganizations;
}


/**
	 * Returns a StringList of the parent object ids which are connected using EBOM Relationship
	 * for a given context.
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args contains a packed HashMap containing objectId of object
	 * @return StringList.
     * @since EngineeringCentral X3
	 * @throws Exception if the operation fails.
	*/
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeRecursiveOIDAddExisting(Context context, String args[])	throws Exception
	{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String  selPartObjectId = (String) programMap.get("selPartObjectId");
        String  excludeOID = (String) programMap.get("selPartParentOId");
        StringList result = new StringList();
        if (selPartObjectId == null){
        	return (result);
        }else{
			result.add(selPartObjectId);
			result.add(excludeOID);
		}
	    DomainObject domObj = new DomainObject(selPartObjectId);
		String strTypePart = PropertyUtil.getSchemaProperty(context,"type_Part");
		StringBuffer sbTypePattern = new StringBuffer(strTypePart);
		String relToExpand = PropertyUtil.getSchemaProperty(context,"relationship_EBOM");
		StringList selectStmts  = new StringList(1);
        selectStmts.addElement(DomainConstants.SELECT_ID);
	    MapList mapList = domObj.getRelatedObjects(context,
																					relToExpand,                        // relationship pattern
																					sbTypePattern.toString(),   // object pattern
																					selectStmts,                               // object selects
																					null,                     // relationship selects
																					true,                              // to direction
																					false,                               // from direction
																					(short) 0,                          // recursion level
																					null,                               // object where clause
																					null);                              // relationship where clause

            Iterator i1 = mapList.iterator();
            while (i1.hasNext())
            {
            	Map m1 = (Map) i1.next();
				String strId = (String)m1.get(DomainConstants.SELECT_ID);
				DomainObject strDomObj = DomainObject.newInstance(context, strId);
			    
			    MapList revMap = (MapList)strDomObj.getRevisionsInfo(context, new StringList(DomainConstants.SELECT_ID), new StringList());
			    StringList revList = EngineeringUtil.getValueForKey(revMap,DomainConstants.SELECT_ID);
			    
			    result.addAll(revList);
            }
		return result;
    }
	/**
	 * Returns a StringList of the parent object ids which are connected using EBOM Relationship
	 * for a given context.
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args contains a packed HashMap containing objectId of object
	 * @return StringList.
     * @since EngineeringCentral X3
	 * @throws Exception if the operation fails.
	*/
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeRecursiveOIDCopyFrom(Context context, String args[])	throws Exception
	{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String  selPartObjectId = (String) programMap.get("selPartObjectId");
        String  excludeOID = (String) programMap.get("selPartParentOId");
        StringList result = new StringList();
        if (selPartObjectId == null){
        	return (result);
        }else{
        	selPartObjectId = DomainObject.newInstance(context, selPartObjectId).getInfo(context, DomainObject.SELECT_ID);
			if (excludeOID != null){
        	excludeOID = DomainObject.newInstance(context, excludeOID).getInfo(context, DomainObject.SELECT_ID);
			result.add(excludeOID);
        }
        	
			result.add(selPartObjectId);
		}
		return result;
    }
	
    /**
     *  Added for the Fix 672436
     * Returns a StringList of the parent object ids from parent OID which are connected using EBOM Relationship
     * for a given context.
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap containing objectId of object
     * @return StringList.
     * @since EngineeringCentral X3
     * @throws Exception if the operation fails.
    */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList excludeOIDAddNextAndReplaceExisting(Context context, String args[])    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String  selPartParentObjectId = (String) programMap.get("selPartParentOId");
        String  excludeOID = (String) programMap.get("selPartObjectId");
        StringList result = new StringList();
        if (selPartParentObjectId == null){
            return (result);
        }else{
            result.add(selPartParentObjectId);
            result.add(excludeOID);
        }
        DomainObject domObj = new DomainObject(selPartParentObjectId);
        String strTypePart = PropertyUtil.getSchemaProperty(context,"type_Part");
        StringBuffer sbTypePattern = new StringBuffer(strTypePart);
        String relToExpand = PropertyUtil.getSchemaProperty(context,"relationship_EBOM");
        StringList selectStmts  = new StringList(1);
        selectStmts.addElement(DomainConstants.SELECT_ID);
        MapList mapList = domObj.getRelatedObjects(context,
                                                                                    relToExpand,                        // relationship pattern
                                                                                    sbTypePattern.toString(),   // object pattern
                                                                                    selectStmts,                               // object selects
                                                                                    null,                     // relationship selects
                                                                                    true,                              // to direction
                                                                                    false,                               // from direction
                                                                                    (short) 0,                          // recursion level
                                                                                    null,                               // object where clause
                                                                                    null);                              // relationship where clause

            Iterator i1 = mapList.iterator();
            while (i1.hasNext())
            {
            	Map m1 = (Map) i1.next();
				String strId = (String)m1.get(DomainConstants.SELECT_ID);
				DomainObject strDomObj = DomainObject.newInstance(context, strId);
			    
			    MapList revMap = (MapList)strDomObj.getRevisionsInfo(context, new StringList(DomainConstants.SELECT_ID), new StringList());
			    StringList revList = EngineeringUtil.getValueForKey(revMap,DomainConstants.SELECT_ID);
			    
			    result.addAll(revList);
            }
        return result;
    }


 //Fix 672436 ends
    /**
     * Returns a | delimited list of rel ids for which the part is used as a subsitute
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap containing objectId of object
     * @return String.
     * @since EngineeringCentral X4
     * @throws Exception if the operation fails.
    */
    public String getSubstitutes(Context context, String args[]) throws Exception
    {
        String finalReturn   = " ";
        String sPartId = args[0];
        DomainObject domPart = new DomainObject(sPartId);
        //Modified for IR-046392V6R2011
        String strPartType = PropertyUtil.getSchemaProperty(context,"type_Part");
        //if (domPart.isKindOf(context, "Part"))
        if (domPart.isKindOf(context, strPartType))
        {
            //Modified for IR-048513V6R2012x, IR-118107V6R2012x start
			String sRelEbomSub = PropertyUtil.getSchemaProperty(context,"relationship_EBOMSubstitute");
			if ("TRUE".equalsIgnoreCase(domPart.getInfo(context, "to["+sRelEbomSub+"]")))
            {
			     finalReturn = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3",sPartId,"to["+sRelEbomSub+"].fromrel.id","|");
          //Modified for IR-048513V6R2012x, IR-118107V6R2012x end
            }
        }
        return finalReturn;
     }
    /**
     * Returns a StringList of ids which are ebom child of context part for includeOIDprogram param
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap containing objectId of context object
     * @return StringList.
     * @since EngineeringCentral X3
     * @throws Exception if the operation fails.
*/
@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
public StringList getEBOMsWithinContext(Context context, String args[]) throws Exception
{
    StringList slEBOMList = new StringList();
    HashMap programMap = (HashMap) JPO.unpackArgs(args);

    String objID = "";
    String contextPart = (String)programMap.get("contextPart");
    String revisionFilter = (String)programMap.get("revisionFilter");

    Part part = new Part(contextPart);
        MapList ebomList = null;
    Map mapChangePartId = new HashMap();

    //construct selects
    SelectList objSelects = new SelectList(2);
    SelectList relSelects = new SelectList(1);
    objSelects.add(DomainConstants.SELECT_ID);
    objSelects.add(DomainConstants.SELECT_REVISION);
    relSelects.add(DomainConstants.SELECT_FROM_ID);

    //fetch all parts in the given bom that have children
    ebomList = part.getRelatedObjects(context,
                                           DomainConstants.RELATIONSHIP_EBOM,  // relationship pattern
                                       DomainConstants.TYPE_PART,          // object pattern
                                       objSelects,                         // object selects
                                       relSelects,                         // relationship selects
                                       false,                              // to direction
                                       true,                               // from direction
                                       (short)0,                           // recursion level
                                       null,                        // object where clause
                                       null);                              // relationship where clause
      if (ebomList != null)
      {
          Iterator i = ebomList.iterator();
          while (i.hasNext())
          {
              Map m = (Map) i.next();
              String strParentId = (String)m.get(DomainConstants.SELECT_FROM_ID);
              String strParentDup = (String)mapChangePartId.get(strParentId);
              if(strParentDup!=null){
                  strParentId = strParentDup;
              }
              String strPartId = (String)m.get(DomainConstants.SELECT_ID);
              if(revisionFilter!=null && revisionFilter.equalsIgnoreCase("Latest")){
                  String oldRev = (String)m.get(DomainConstants.SELECT_REVISION);
                  DomainObject domObj = DomainObject.newInstance(context,strPartId);
                  BusinessObject bo = domObj.getLastRevision(context);
                  bo.open(context);
                  objID = bo.getObjectId();
                  String newRev = bo.getRevision();
                  bo.close(context);

                  if(!oldRev.equals(newRev)){
                      mapChangePartId.put(strPartId, objID);
                      strPartId = objID;
                      domObj = DomainObject.newInstance(context,strPartId);
                  }
              }
              slEBOMList.add(strPartId);
        }
        }
    if (slEBOMList.size()==0)
    {
        slEBOMList.add("None");
    }
    return slEBOMList;
}
/**
 * To filter the parts having next released revisions from the selected Affected Items list.
 * @param context
 * @param args Contains all the affectedItems selected to add/move to a Change
 * @return Returns a Map containing two separate filtered lists.
 * @throws Exception
 * @since R207
 * @author zgq
 *
 */
public Map filterPartsWithNextReleasedRevision(Context context,String[] args) throws Exception
{

    String[] strObjectIds = args;
    String partName = "";
    String partRev = "";
    StringBuffer strPartsWithRevision = new StringBuffer();
    StringList slFilteredParts = new StringList();
    Map partMap = new HashMap();
    try{
        for(int i=0; i < strObjectIds.length; i++)
        {
            StringTokenizer st = new StringTokenizer(strObjectIds[i], "|");
            String sObjId = st.nextToken();
            DomainObject domObject = new DomainObject(sObjId);
            partName = domObject.getInfo(context, DomainConstants.SELECT_NAME);
            partRev = domObject.getInfo(context,DomainConstants.SELECT_REVISION);
            String nextRevision =domObject.getInfo(context, "next.current");
            if(nextRevision != null && nextRevision.equals(DomainConstants.STATE_PART_RELEASE))
            {
                    if(strPartsWithRevision.length() > 0)
                    {
                        strPartsWithRevision.append(", ");
                    }
                    strPartsWithRevision.append(partName);
                    strPartsWithRevision.append(' ');
                    strPartsWithRevision.append(partRev);
                    continue;
            }
                slFilteredParts.add(strObjectIds[i]);
        }
        String strFilteredParts[] = new String[slFilteredParts.size()];
        for(int i=0; i<slFilteredParts.size(); i++){
            strFilteredParts[i]=(String)slFilteredParts.get(i);
        }
        partMap.put("partsWithRevision", strPartsWithRevision.toString());
        partMap.put("filteredParts", strFilteredParts);
    }
    catch (Exception e){
        e.printStackTrace();
    }
    return partMap;
}
//373447
/**
  * includeOIDLeadOrganizations() method returns OIDs of organization/ProjectSpace
  * Having person with Lead role as "ECR Chairman" and "ECR Coordinator"
  * @param context Context : User's Context.
  * @param args String array
  * @return The StringList value of includeOIDLeadOrganizations.
  * @throws Exception if searching Parts object fails.
  * @since X3-HF54
  * @author Deepika Das
 */

@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
public StringList includeOIDLeadOrganizations(Context context, String[] args)
    throws Exception
{
	// Check if it is OCDX user
	boolean bOnCloud = false;
	String UserRole = context.getRole();
	int startIndex = UserRole.indexOf("::");
	int endIndex = UserRole.indexOf(".");
	String role = UserRole.substring(startIndex+2, endIndex);
	if (role.equalsIgnoreCase("VPLMProjectLeader")||role.equalsIgnoreCase("VPLMCreator"))
	{
		bOnCloud = true;
	}
	

    String leadresponsibilityrel  = PropertyUtil.getSchemaProperty(context,"relationship_LeadResponsibility");
    String sOrganization  = PropertyUtil.getSchemaProperty(context,"type_Organization");
    String sProjectRole  = PropertyUtil.getSchemaProperty(context,"attribute_ProjectRole");
    StringList strlIncludeOIDLeadOrganizations = new StringList();
    StringList strlLeadOrganizationselects = new StringList();
    strlLeadOrganizationselects.add(DomainConstants.SELECT_ID);
    strlLeadOrganizationselects.add("from[" + leadresponsibilityrel + "].attribute[" + sProjectRole  + "]");
    MapList totalresultList = DomainObject.findObjects(context,
                                                     sOrganization,
                                                     "*",
                                                     "*",
                                                     "*",
                                                     "*",
                                                     null,
                                                     null,
                                                     true,
                                                     strlLeadOrganizationselects,
                                                     (short) 0);
    Iterator itrLeadOrganizations = totalresultList.iterator();
    while (itrLeadOrganizations.hasNext()){
        Map mapOrganization = (Map) itrLeadOrganizations.next();
        String strOrganizationId = (String) mapOrganization.get(DomainConstants.SELECT_ID);
		// IR-383306: If OCDX, then no need of LeadRole check
    	if (bOnCloud) {
    		strlIncludeOIDLeadOrganizations.add(strOrganizationId);
    	}
    	else {
	        String strLeadResps = (String) mapOrganization.get("from[" + leadresponsibilityrel + "].attribute[" + sProjectRole  + "]");
	        if (strLeadResps != null){
	            if ((strLeadResps.indexOf("role_ECRCoordinator") != -1) && (strLeadResps.indexOf("role_ECRChairman") != -1)){
	            	strlIncludeOIDLeadOrganizations.add(strOrganizationId);
	            }
	        }
    	}
    }

    if (strlIncludeOIDLeadOrganizations.size()==0){
        strlIncludeOIDLeadOrganizations.add("");
    }

    return strlIncludeOIDLeadOrganizations;
}
//End

//  373876
    /**
     * includeEBOMOIDs() method returns OIDs of child parts.
     * @param context Context : User's Context.
     * @param args String array parameter having Part's details
     * @return The StringList value of includeEBOMOIDs.
     * @throws Exception if searching Parts object fails.
     */
     public StringList includeEBOMOIDs(Context context, String[] args)
         throws Exception
     {
         HashMap params = (HashMap)JPO.unpackArgs(args);
         String objectId = (String) params.get("objectId");
         DomainObject partObj = new DomainObject(objectId);



         String strTypePart = PropertyUtil.getSchemaProperty(context,"type_Part");
         String relToExpand = PropertyUtil.getSchemaProperty(context,"relationship_EBOM");
         StringList selectStmts  = new StringList(1);
         selectStmts.addElement(DomainConstants.SELECT_ID);
         MapList mapList = partObj.getRelatedObjects(context,
                                               relToExpand,                        // relationship pattern
                                               strTypePart,   // object pattern
                                               selectStmts,                               // object selects
                                               null,                     // relationship selects
                                               false,                              // to direction
                                               true,                               // from direction
                                               (short) 1,                          // recursion level
                                               null,                               // object where clause
                                               null);                              // relationship where clause

         StringList ebomList = new StringList(mapList.size());
         Iterator i1 = mapList.iterator();
         while (i1.hasNext())
         {
             Map m1 = (Map) i1.next();
             String strId = (String)m1.get(DomainConstants.SELECT_ID);
             ebomList.addElement(strId);
         }
         return ebomList;

     }

     /**
	  * excludeAffectedItems() method returns OIDs of Affect Items[Part/Spec/CAD Drawing]
	  * which are already connected to context change[ECR/ECO] object
	  * @param context Context : User's Context.
	  * @param args String array
	  * @return The StringList value of OIDs
	  * @throws Exception if searching Parts object fails.
	  * @since V6R2010x
	  * @author Chetan Kumar M
	 */
	 @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	 public StringList excludeAffectedItems(Context context, String args[])    throws Exception
	 {
	         HashMap programMap = (HashMap) JPO.unpackArgs(args);
	         String  strChangeId = (String) programMap.get("objectId");
	         StringList strlAffItemList = new StringList();
	         if (strChangeId == null){
	             return (strlAffItemList);
	         }
	         try {

	             DomainObject domObj = new DomainObject(strChangeId);
	             String srelPattern = PropertyUtil.getSchemaProperty(context, "relationship_AffectedItem");
	             StringList selectStmts  = new StringList(1);
	             selectStmts.addElement(DomainConstants.SELECT_ID);
	             MapList mapList = domObj.getRelatedObjects(context,
	                                                         srelPattern,         //java.lang.String relationshipPattern,
	                                                         "*",                 //java.lang.String typePattern,
	                                                         selectStmts,         //matrix.util.StringList objectSelects,
	                                                         null,                //matrix.util.StringList relationshipSelects,
	                                                         false,               //boolean getTo,
	                                                         true,                //boolean getFrom,
	                                                         (short)1,            //short recurseToLevel,
	                                                         null,                //java.lang.String objectWhere,
	                                                         null);               //java.lang.String relationshipWhere)

	             Iterator i1 = mapList.iterator();
	             while (i1.hasNext()){
	                 Map m1 = (Map) i1.next();
	                 String strId = (String)m1.get(DomainConstants.SELECT_ID);
	                 strlAffItemList.addElement(strId);
	             }
	         }catch (Exception e) {
	             e.printStackTrace();
	     }
	     return strlAffItemList;
	}
//377819

	  /* includeAssigneeOIDs() method returns OIDs of Person having Senior Design Engineer (or) Design Engineer role
	  * @param context Context : User's Context.
	  * @param args String array
	  * @return The StringList value of PersonIds
	  * @throws Exception if operation fails.
	  * @since R208
	  */
	 @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	 public StringList includeAssigneeOIDs(Context context, String[] args)
		  throws Exception
		  {
	             //Start : IR-009795
                 HashMap programMap = (HashMap) JPO.unpackArgs(args);
                 String  parentObjectId = (String) programMap.get("objectId");
                 String strRelName = PropertyUtil.getSchemaProperty(context,"relationship_AssignedEC");
                 //End: IR-009795
				  StringList strlPersonsIDs = new StringList();
                  StringList str2PersonsIDs = new StringList();
				  String strRole1 = PropertyUtil.getSchemaProperty(context,"role_SeniorDesignEngineer");
				  String strRole2 = PropertyUtil.getSchemaProperty(context,"role_DesignEngineer");
				  StringList strlRoles = new StringList(2);
				  strlRoles.addElement(strRole1);
				  strlRoles.addElement(strRole2);
				  try {
					  Iterator itr = strlRoles.iterator();
					  while(itr.hasNext()){
						  String strTempRole = (String)itr.next();
						  String cmd = "print role $1 select $2 dump $3";
						  String strRolePersonIds = MqlUtil.mqlCommand(context,cmd,strTempRole,"person.object.id",",");
						  StringList strlRolePersonIds = FrameworkUtil.split(strRolePersonIds, ",");
						  Iterator itr1 = strlRolePersonIds.iterator();
						  //check n Add this persons ids to final list
						  while(itr1.hasNext()){
							  String strTempPersonIds = (String)itr1.next();
							  if(strlPersonsIDs.size()>0){
								  if(!strlPersonsIDs.contains(strTempPersonIds))
									  strlPersonsIDs.add(strTempPersonIds);
							  }else{
								  strlPersonsIDs.add(strTempPersonIds);
							  }
						  }
					  }

					  //If TBE is installed, TBE role people are should be included in assignees
					  if(EngineeringUtil.isENGSMBInstalled(context, true)) {
						  strlPersonsIDs.addAll(TBEUtil.getContextProjectUsers(context));
					  }

                       //Start : IR-009795
                        Iterator itr2 =  strlPersonsIDs.iterator();
                        DomainObject doChange = DomainObject.newInstance(context);
                        doChange.setId(parentObjectId);
                        StringList personIds = doChange.getInfoList(context,"to["+strRelName+"].from.id");
                        while(itr2.hasNext()){
                            String strTempPersonIds1 = (String)itr2.next();
                            if(personIds.indexOf(strTempPersonIds1)==-1){
                                str2PersonsIDs.add(strTempPersonIds1);
                            }
                        }
                        //End : IR-009795
				  }catch (FrameworkException e) {
					  e.printStackTrace();
			 }
			 return strlPersonsIDs;
		}

 //373351 - Starts
/**
 *includeRDE() method returns OIDs of Person having Senior Design Engineer role
	 *@param context Context : User's Context.
	 * @param args String array
	 * @return The StringList value of includeRDE
     * @throws Exception on failure.
	 */
	 @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	 public StringList includeRDE(Context context, String[] args)
	 throws Exception
	 {
		 String sSDERole  = PropertyUtil.getSchemaProperty(context,"role_SeniorDesignEngineer");
		 return getRolePersonIds(context, sSDERole);
	 }

     //373351
     /**
     * getRolePersonIds() gets all the person OIDs whose has the given role
     * @param context Context : User's Context.
     * @param String role name
     * @return StringList contains person OIDs
     * @throws Exception on failure
     */
	 public StringList getRolePersonIds(Context context, String role) throws Exception
	 {
		 StringList strlRolePersonIds = new StringList(1000);
		 String cmd = "print role $1 select $2 dump $3";
		 String strRolePersonIds = MqlUtil.mqlCommand(context,cmd,role,"person.object.id",",");
		 strlRolePersonIds = FrameworkUtil.split(strRolePersonIds, ",");
		 return strlRolePersonIds;
	 }

	 //373351
	 /**
	 *includeRME() method returns OIDs of Person having Senior Manufacturing Engineer role
	 * @param context Context : User's Context.
	 * @param args String array
	 * @return The StringList value of includeRME.
     * @throws Exception on failure
	 */
	 @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	 public StringList includeRME(Context context, String[] args)
	 throws Exception
	 {
		 String sSMERole  = PropertyUtil.getSchemaProperty(context,"role_SeniorManufacturingEngineer");
		 return getRolePersonIds(context, sSMERole);
	 }
     //End
     //373351 - Ends


/*  This method does the multiLevelRecursionCheck for EBOM and EBOM Pending relationships.
 ** Inputs : args[0]---Selected From Object Id
 			 args[1]---Selected To Object Id
 			 args[2]---relationship passed
 			 args[3]---boolean includeRevisionCheck boolean value to check for cyclic condition on revisions. If true revisions will be checked as part of cyclic and false will not include revision check
 ** Output : Returns 0 or 1 which specifies object should get connect or not
 */
	 public int multiLevelRecursionCheck(Context context,String[] args) throws Exception
	 {
		 boolean recursionFlag = false; //assume recursion not exists initially
		 String fromObjectId = args[0];
		 String toObjectId	 = args[1];
		 String relType 	 = args[2];
		 String includeRevisions = args[3];

		 boolean includeRevisionCheck = "true".equalsIgnoreCase(includeRevisions)?true:false;

		 StringList relTypeList = (StringList)FrameworkUtil.split(relType, ",");
		 StringBuffer relTypeBuffer = new StringBuffer();
		 Iterator relItr = relTypeList.iterator();
		 while (relItr.hasNext()) {
			 relType = PropertyUtil.getSchemaProperty(context,relItr.next().toString());
			 relTypeBuffer = (relTypeBuffer.length() > 0 )?relTypeBuffer.append(",").append(relType):relTypeBuffer.append(relType);
		 }

		 try {
			 	//this api returns true if recursion exists
			 	recursionFlag = (Boolean)DomainObject.multiLevelRecursionCheck(context,fromObjectId,toObjectId,relTypeBuffer.toString(),includeRevisionCheck);
		 	 }

			 catch (Exception e)
			 {
				  e.printStackTrace();
				  throw new FrameworkException(e.getMessage());
			 }

			 //If recursion exists, return recursion exists message to the User
			 if (recursionFlag) {
				//Multitenant
				 String recursionMesssage = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Alert.RecursionError");
		             throw new FrameworkException(recursionMesssage);
			 }

		 return 0;
	 }


	/*Recursion check for substitute parts*/
	 public int multiLevelRecursionCheckForSubstitute(Context context,String[] args) throws Exception
	 {
         String recPartName = "";
		  try{

	             DomainObject sub=new DomainObject(args[1]);
	             String subName = sub.getName(context);
	             String subType = sub.getType(context);
	             String st[] = {args[0]};
	             StringList relSelect = new StringList();
	             relSelect.add(DomainObject.SELECT_TO_ID);
	             MapList ml = DomainRelationship.getInfo(context, st, relSelect);
	             String primaryId = (String) ((Map) ml.get(0)).get(DomainObject.SELECT_TO_ID);
	             DomainObject prim = new DomainObject(primaryId);
	             StringList selectStmts = new StringList();
	             selectStmts.add(DomainObject.SELECT_NAME);
	             selectStmts.add(DomainObject.SELECT_TYPE);
	             String strName ="";
	             String strType ="";
	             boolean recursionFlag = false;

	             MapList m1 = prim.getRelatedObjects(context,
						  DomainConstants.RELATIONSHIP_EBOM,      // relationship pattern
						  DomainConstants.TYPE_PART,              // object pattern
						  selectStmts,                            // object selects
						  null,                                   // relationship selects
						  false,                                  // to direction
						  true,                                   // from direction
						 (short) 0,                               // recursion level
						  null,                                   // object where clause
						  null,									  // relationship where clause
						  0);


					Iterator itr = m1.iterator();
					while (itr.hasNext())
					{
						Map map = (Map) itr.next();
						strName = (String)map.get(DomainConstants.SELECT_NAME);
						strType = (String)map.get(DomainConstants.SELECT_TYPE);
						if(strName.equals(subName) && strType.equals(subType))
						{
							recursionFlag=true;
							break;
						}
					}

	             if (!recursionFlag) {
					MapList m2 = prim.getRelatedObjects(context,
						  DomainConstants.RELATIONSHIP_EBOM,      // relationship pattern
						  DomainConstants.TYPE_PART,              // object pattern
						  selectStmts,                            // object selects
						  null,                                   // relationship selects
						  true,                                   // to direction
						  false,                                  // from direction
						 (short) 0,                               // recursion level
						  null,                                   // object where clause
						  null,									  // relationship where clause
						  0);

					itr = m2.iterator();
					while (itr.hasNext())
					{
						Map map = (Map) itr.next();
						strName = (String)map.get(DomainConstants.SELECT_NAME);
						strType = (String)map.get(DomainConstants.SELECT_TYPE);
						if(strName.equals(subName) && strType.equals(subType))
						{
							recursionFlag=true;
                            recPartName = strName;
							break;
						}
					}
	             }
		  if (recursionFlag){
			//Multitenant
			  String msg = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Alert.RecursionError");
              emxContextUtil_mxJPO.mqlNotice(context,msg+":"+recPartName);
              return 1;

		  }

		  else
			  return 0;
		  }
				 	 catch(Exception e)
					{
				 		 String strENGResFileId = "emxEngineeringCentralStringResource";
				 		 String msg = EnoviaResourceBundle.getProperty(context, strENGResFileId, context.getLocale(),"emxEngineeringCentral.Alert.RecursionError");
				 		 emxContextUtil_mxJPO.mqlNotice(context,msg);
						return 1;
					}
	 }
     //R208.HF1 - Ends
     /*Recursion check for Alternate parts*/
     public int multiLevelRecursionCheckForAlternate(Context context,String[] args) throws Exception
     {
           String recPartName = "";
          try{

                 DomainObject sub=new DomainObject(args[1]);
                 String subName = sub.getName(context);
                 String subType = sub.getType(context);

                 String primaryId = args[0];
                 DomainObject prim = new DomainObject(primaryId);
                 StringList selectStmts = new StringList();
                 selectStmts.add(DomainObject.SELECT_NAME);
                 selectStmts.add(DomainObject.SELECT_TYPE);
                 String strName ="";
                 String strType ="";
                 boolean recursionFlag = false;

                 MapList m1 = prim.getRelatedObjects(context,
                          DomainConstants.RELATIONSHIP_EBOM,      // relationship pattern
                          DomainConstants.TYPE_PART,              // object pattern
                          selectStmts,                            // object selects
                          null,                                   // relationship selects
                          false,                                  // to direction
                          true,                                   // from direction
                         (short) 0,                               // recursion level
                          null,                                   // object where clause
                          null,                                   // relationship where clause
                          0);


                    Iterator itr = m1.iterator();
                    while (itr.hasNext())
                    {
                        Map map = (Map) itr.next();
                        strName = (String)map.get(DomainConstants.SELECT_NAME);
                        strType = (String)map.get(DomainConstants.SELECT_TYPE);
                        if(strName.equals(subName) && strType.equals(subType))
                        {
                            recursionFlag=true;
                            recPartName = strName;
                            break;
                        }
                    }

                 if (!recursionFlag) {
                    MapList m2 = prim.getRelatedObjects(context,
                          DomainConstants.RELATIONSHIP_EBOM,      // relationship pattern
                          DomainConstants.TYPE_PART,              // object pattern
                          selectStmts,                            // object selects
                          null,                                   // relationship selects
                          true,                                   // to direction
                          false,                                  // from direction
                         (short) 0,                               // recursion level
                          null,                                   // object where clause
                          null,                                   // relationship where clause
                          0);

                    itr = m2.iterator();
                    while (itr.hasNext())
                    {
                        Map map = (Map) itr.next();
                        strName = (String)map.get(DomainConstants.SELECT_NAME);
                        strType = (String)map.get(DomainConstants.SELECT_TYPE);
                        if(strName.equals(subName) && strType.equals(subType))
                        {
                            recursionFlag=true;
                            recPartName = strName;
                            break;
                        }
                    }
                 }
          if (recursionFlag){
              throw new Exception();
          }

          else
              return 0;
          }
                     catch(Exception e)
                    {

                    	//Multitenant
                    	 String msg = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Alert.RecursionError");
                  throw new FrameworkException(msg+":"+recPartName);
                    }



     }
	 /**
	  * Added to fix IR-059595V6R2011x
	  * @param context
	  * @param args
	  * @return
	  * @throws Exception
	  */
		public StringList getMfgUsageForSearchTable(Context context, String[] args)
		throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList mlObjList = (MapList) programMap.get("objectList");
		StringList slColVal = new StringList();
		Iterator itrObjList = mlObjList.iterator();

		try {
			if (mlObjList.size() > 0) {
				String ebomSubstitute = PropertyUtil.getSchemaProperty(context,"relationship_EBOMSubstitute");
				StringList relSelect = new StringList();
				relSelect.add("to["+DomainRelationship.RELATIONSHIP_EBOM+"]");
				relSelect.add("to["+ebomSubstitute+"]");
				relSelect.add("to["+DomainRelationship.RELATIONSHIP_ALTERNATE+"]");

				String partId = null;
				Part partObj = null;
				Map mfgPartUseDetails = null;
				Map objectMap = null;

				String sPRIMARY = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxMBOM.MBOMMS.Primary");
				String sALTERNATE = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxMBOM.MBOM.Alternate");
				String sSUBSTITUTE = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxMBOM.MBOMMS.Substitute");

				while (itrObjList.hasNext()) {
					objectMap = (Map) itrObjList.next();
					partId = (String) objectMap.get("id");
					partObj = new Part(partId);

					mfgPartUseDetails = partObj.getInfo(context, relSelect);
					if("true".equalsIgnoreCase((String)mfgPartUseDetails.get("to["+DomainRelationship.RELATIONSHIP_EBOM+"]")))
					{
						slColVal.add(sPRIMARY);
					}
					else if("true".equalsIgnoreCase((String)mfgPartUseDetails.get("to["+ebomSubstitute+"]")))
					{
						slColVal.add(sSUBSTITUTE);
					}
					else if("true".equalsIgnoreCase((String)mfgPartUseDetails.get("to["+DomainRelationship.RELATIONSHIP_ALTERNATE+"]")))
					{
						slColVal.add(sALTERNATE);
					}
					else {
						slColVal.add("");
					}
				}
			}
		} catch (Exception exObj) {
	}
	return slColVal;
}
		 /**
		  * Added to fix IR-059595V6R2011x
		  * @param context
		  * @param args
		  * @return
		  * @throws Exception
		  */
		 public StringList getBOMLevel(Context context, String[] args)
		 throws Exception {
			StringList slColVal = new StringList();
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList mlObjList = (MapList) programMap.get("objectList");

			Iterator itrObjList = mlObjList.iterator();
			String sLevel = "";
			try {
				if (mlObjList.size() > 0) {

					 StringList objectSelects=new StringList();

				     objectSelects.add(DomainConstants.SELECT_ID);
				     MapList objectsMapListOne = null;
				     String id                 = null;
				     String parentPartId       = null;
				     DomainObject pObj         = null;
				     Map objectMap             = null;
					 while (itrObjList.hasNext()) {
						objectMap = (Map) itrObjList.next();

						if(objectsMapListOne==null)//get Object ids once
						{
							parentPartId = (String) objectMap.get("id[parent]");
							if(parentPartId!=null)
							{
								 pObj = new DomainObject(parentPartId);
								 objectsMapListOne = pObj.getRelatedObjects(context,
										 DomainConstants.RELATIONSHIP_EBOM,
										 DomainConstants.TYPE_PART,
		                                 objectSelects,
		                                 new StringList(),
		                                 false,
		                                 true,
		                                 (short)0,
		                                 "",
		                                 null);
							}
						}
						id = (String) objectMap.get("id");
						if(id!=null && objectsMapListOne!=null)
						{
							 for(int i=0; i<objectsMapListOne.size(); i++){
				                 Map ebomMap = (Map)objectsMapListOne.get(i);
				                 if(id.equals(ebomMap.get("id")))
				                 {
				                	 sLevel = (String)ebomMap.get("level");
				                	 if (sLevel != null && !"".equals(sLevel)) {
				 						slColVal.add(sLevel);
				                	 }
				                	 else {
				 						slColVal.add("");
				 					 }
				                	 break;
				                 }
							 }
						}
						 else {
		 						slColVal.add("");
		 					 }
					}
				}
			} catch (Exception exObj) {
			}
			return slColVal;
		 }

		 /**
		     * Gets the Location/Organization list page.
		     *
		     * @param context
		     *            the eMatrix <code>Context</code> object.
		     * @param args
		     *            holds a packed HashMap which caontains-type and name to be
		     *            searched for.
		     * @return a maplist of location/Organizations
		     * @throws FrameworkException
		     *             if the operation fails.
		     */
		  public StringList getManufacturingLocationSearchResults(Context context, String[] args)
		    throws FrameworkException {

			StringList locationList = new StringList();
			MapList organizationList = new MapList();
				try {

			    HashMap paramMap = (HashMap) JPO.unpackArgs(args);

			    String sType = (String) paramMap.get("type");
			    String sName = (String) paramMap.get("Name");
			    String sSelManufacturerId=(String)paramMap.get("companyId");

			    String strRelLocation  = "*";
			    SelectList busSelects  = new SelectList(1);
			    busSelects.add(DomainConstants.SELECT_ID);

			    StringBuffer sWhereExp = new StringBuffer(32);
			    String txtVault        ="";
			    String relWhere        = "";

			    String txtVaultOption = (String) paramMap.get("vaultOption");

			    if(sSelManufacturerId != null &&  !"null".equalsIgnoreCase(sSelManufacturerId) && !"".equals(sSelManufacturerId))
			    {
			      if ("DEFAULT_VAULT".equals(txtVaultOption))
			        {
			            txtVault = "Vault ==\""+context.getVault().getName()+"\"" ;
			        }


			      if (!"*".equals(sName)) {
			             if(sWhereExp.length()>0) {sWhereExp.append(" && ");}
			             sWhereExp.append('(');
			             sWhereExp.append("name ~~ \"");
			             sWhereExp.append(sName);
			             sWhereExp.append('\"');
			             sWhereExp.append(')');

			         }

			         if(sWhereExp.length()>0) {
			        	 sWhereExp.append(" && ");
			        	 }

			         sWhereExp.append(txtVault);


			        sWhereExp.append(" && current == \"" + PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_LOCATION,"state_Active") +"\"");
	  		       DomainObject domObj = new DomainObject(sSelManufacturerId.trim());

			         organizationList = domObj.getRelatedObjects(context, strRelLocation,
			                 sType, busSelects, null, false, true, (short) 1, sWhereExp.toString(),
			                 relWhere);

			          }
			        else{

					    com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject
					            .newInstance(context, DomainConstants.TYPE_PERSON);
					    Company company = person.getPerson(context).getCompany(context);


					     organizationList = company.getRelatedObjects(context, strRelLocation,
					                      sType, busSelects, null, false, true, (short) 1, sWhereExp.toString(),
					            relWhere);
			          }

			    if(organizationList!=null)
			    {
			    	Iterator itr = organizationList.iterator();
			    	Hashtable hTable = null;
			    	String objectId = null;
			    	while (itr.hasNext()){
			    		hTable = (Hashtable) itr.next();
			    		objectId = (String) hTable.get(DomainConstants.SELECT_ID);
			    		locationList.add(objectId);
			    	}
			    }
			}
			catch (Exception Ex)
		    {
				Ex.printStackTrace();
			    throw new FrameworkException(Ex);
		    }
	    	return locationList;
		}


		@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
		public StringList excludeRelatedItems(Context context, String[] args)
			throws Exception
			{
				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				String  strChangeId = (String) programMap.get("objectId");
				StringList strRelatedItemList = new StringList();
				if (strChangeId == null){
					return (strRelatedItemList);
				}
				try {

					DomainObject domObj = new DomainObject(strChangeId);
					String srelPattern = PropertyUtil.getSchemaProperty(context, "relationship_PartSpecification");
					StringList selectStmts  = new StringList(1);

					selectStmts.addElement(DomainConstants.SELECT_ID);
					MapList mapList = domObj.getRelatedObjects(context,
							srelPattern,         //java.lang.String relationshipPattern,
							"*",                 //java.lang.String typePattern,
							selectStmts,         //matrix.util.StringList objectSelects,
							null,                //matrix.util.StringList relationshipSelects,
							true,                //boolean getTo,
							false,               //boolean getFrom,
						    (short)1,            //short recurseToLevel,
						    null,                //java.lang.String objectWhere,
						    null,				 //java.lang.String relationshipWhere)
							0);
					Iterator i1 = mapList.iterator();
					while (i1.hasNext()){
						Map m1 = (Map) i1.next();
						String strId = (String)m1.get(DomainConstants.SELECT_ID);
						strRelatedItemList.addElement(strId);
					}

				}catch (Exception e) {
					e.printStackTrace();
				}
				return strRelatedItemList;
	}

    /**
     * Returns RDO OBJECTID if connected to RDO, or Unassigned if not connected.
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains objectId of object
     * @return String.
     * @since EngineeringCentral R211
     * @throws Exception if the operation fails.
    */
    public String getRDO(Context context, String args[]) throws Exception {
        String sPartId = args[0];
        String finalReturn = "";

        if (sPartId != null && !"".equals(sPartId)) {
        	DomainObject domObj = DomainObject.newInstance(context, sPartId);
        	finalReturn = domObj.getInfo(context, "to[" + DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.id");
        }

        return (finalReturn == null || "".equals(finalReturn)) ? "Unassigned" : finalReturn;
    }
    /**
	 * Returns a StringList of the  object ids of Parts which are are used in the context Part's BOM and can be connected as Spare Part
	 * for a given context.
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args contains a packed HashMap containing objectId of object
	 * @return StringList.
         * @since R212
	 * @throws Exception if the operation fails.
	*/
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList includeBOMConnectedSpareOIDs(Context context, String[] args) throws Exception {
		HashMap params = (HashMap)JPO.unpackArgs(args);
		String parentId = (String)params.get("objectId");
		Part part = new Part();
		part.setId(parentId);
	    MapList totalresultList = null;
	    StringList selectStmts = new StringList(5);
	    selectStmts.addElement(part.SELECT_ID);
	    selectStmts.addElement(part.SELECT_NAME);
	    selectStmts.addElement(part.SELECT_REVISION);
	    selectStmts.addElement(part.SELECT_TYPE);
	    selectStmts.addElement(part.SELECT_DESCRIPTION);
	    selectStmts.addElement(part.SELECT_RELATIONSHIP_ID);

	    totalresultList = part.getSparePartChoices(context, "Part", "*", "*", selectStmts, false);

		Map object;
		String objectId="";
		StringList resultInclude=new StringList();
		Iterator itr = totalresultList.iterator();
		while (itr.hasNext()){
			object = (Map) itr.next();
			objectId = (String) object.get(DomainConstants.SELECT_ID);
			resultInclude.add(objectId);
		}

		return resultInclude;
	}

	//89733
	/**
     * Returns  Member relationship's attribute[Project Role]+ objectId of the organization connected to Person
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains objectId of object
     * @return String.
     * @since EngineeringCentral R211
     * @throws Exception if the operation fails.
    */
    public String getProjectRoles(Context context, String args[]) throws Exception {
        String sPersonId = args[0];
        String finalReturn = "";
        String strOrgId = "";
        String query="";
        //Added for autonomy search 
        String strDel = matrix.db.SelectConstants.cSelectDelimiter;
        
        if (args.length >= 2)  {
        	strDel = "|";
        }

        if (sPersonId != null && !"".equals(sPersonId)) {
        	DomainObject doPerson = DomainObject.newInstance(context, sPersonId);
             StringList relSelectList = new StringList(2);
             relSelectList.add(DomainConstants.SELECT_FROM_ID);
             relSelectList.add("attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"].value");
             java.util.List orgList = new MapList();
             orgList = doPerson.getRelatedObjects(context,
                                                  DomainConstants.RELATIONSHIP_MEMBER,
                                                  "*",
                                                  null,
                                                  relSelectList,
                                                  true,
                                                  false,
                                                  (short) 1,
                                                  null,//object where clause
                                                  null);//relationship where clause
             if (!orgList.isEmpty())
             {
                Iterator mapItr = orgList.iterator();
                Map mapOrg = null;

                while(mapItr.hasNext())
                {

                    mapOrg = (Map)mapItr.next();
                    strOrgId=(String)mapOrg.get(DomainConstants.SELECT_FROM_ID);
                    query=(String)mapOrg.get("attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"].value");
                    if (query!=null && !"null".equals(query) && !"".equals(query))
                	{
                		StringList strlRoles = FrameworkUtil.split(query, "~");
                		Iterator itr = strlRoles.iterator();
                		while (itr.hasNext()) {
                			String role = (String)itr.next();
                			finalReturn=finalReturn + strDel + role+strOrgId;
                		}
                	}

                }
             }

        }

        return finalReturn;
    }
    /**
     * Includes the  parts that are common across the Revised parts of an ECO.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args String array
     * @return The StringList value of object IDs of the Parts.
     * @throws FrameworkException if the operation fails
     */
    @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
    public StringList includeCommonParts(Context context, String[] args)
    throws Exception
    {

    	HashMap params = (HashMap)JPO.unpackArgs(args);
    	String parentId = (String)params.get("objectId");
		Part part = new Part();
		part.setId(parentId);
    	String []selectedObjects =   ((String)params.get("selectedObjs")).split("\\|");
    	StringList strlIncludeOIDCommonParts = new StringList();
    	StringList objSelects = new StringList(7);
    	objSelects.addElement("id");
    	objSelects.addElement("type");
    	objSelects.addElement("name");
    	objSelects.addElement("revision");
    	objSelects.addElement("description");
    	objSelects.addElement("current");
    	objSelects.addElement("policy");
    	StringList relSelects = new StringList();
    	MapList commonPartsList = part.getCommonParts(context,
    			selectedObjects,
    			objSelects,
    			relSelects,
    			(short)1);
    	Iterator itrCommonParts = commonPartsList.iterator();
    	while( itrCommonParts.hasNext()){
    		Map mapCommonPart = (Map) itrCommonParts.next();
    		String strOrganizationId = (String) mapCommonPart.get(DomainConstants.SELECT_ID);
    		strlIncludeOIDCommonParts.add(strOrganizationId);
    	}

    	if(strlIncludeOIDCommonParts.size()==0){
    		strlIncludeOIDCommonParts.add("");
    	}

    	return strlIncludeOIDCommonParts;
}

 //Add for RDO Convergence start
    /**
     * includeOIDMemberOrganizations() method returns list of Active Organization where User is member.
   * @param context Context : User's Context.
   * @param args String array 
   * @return The StringList value of Organizatons.
   * @throws Exception if operation fails.
   */
   @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
    public StringList includeOIDMemberOrganizations(Context context, String[] args)
    throws Exception
    {    	
    	Person person = Person.getPerson(context, context.getUser());
    	String sPersonId = person.getId();    	    	        
    	StringList strlIncludeOIDOrgs = new StringList();
    	StringList selectStmts = new StringList();
	  	selectStmts.addElement(DomainConstants.SELECT_ID);
	  	selectStmts.addElement(DomainConstants.SELECT_NAME);
    
	    DomainObject dObj = DomainObject.newInstance(context, sPersonId);	  	     
	    MapList result = dObj.getRelatedObjects(context,
	            DomainConstants.RELATIONSHIP_MEMBER,
	            DomainConstants.TYPE_ORGANIZATION,
	            selectStmts,
	            null,
	            true,
	            false,
	            (short)0,
	            null,
	            null);
	                 
	    Iterator itrOrganizations = result.iterator();
	    while (itrOrganizations.hasNext()){
	        Map mapOrganization = (Map) itrOrganizations.next();	        
	        String strOrgId = (String) mapOrganization.get(DomainConstants.SELECT_ID);
	        strlIncludeOIDOrgs.add(strOrgId);
	    }
	
	    if(strlIncludeOIDOrgs.size()==0){
	    	strlIncludeOIDOrgs.add("");
	    }
	
	    return strlIncludeOIDOrgs;
	}
          
    /**
     * excludeECOObjs() method returns ECO ID based on the Altowner1 value.
   * @param context Context : User's Context.
   * @param args String array 
   * @return The StringList value of excludeECOObjs.
   * @throws Exception if operation fails.
   */
   @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
   public StringList excludeECOObjs(Context context, String[] args)
	throws Exception {

		HashMap params = (HashMap) JPO.unpackArgs(args);

		String sRelDesignResponsibility = PropertyUtil.getSchemaProperty(context, "relationship_DesignResponsibility");

		String strObjectId = (String) params.get("objectId");

		StringList strlExcludeOIDChangeObjs = new StringList();

		if (strObjectId != null) {

			DomainObject doPart = new DomainObject(strObjectId);		
			String strPartOrg = doPart.getAltOwner1(context).toString();	

			if (strPartOrg != null && strPartOrg.length() > 0) {
				String strWhereClause = null;

				StringList strlChangeObjselects = new StringList(3);
				strlChangeObjselects.add(DomainConstants.SELECT_ID);
				strlChangeObjselects.add("to[" + sRelDesignResponsibility+ "].from.id");
				strlChangeObjselects.add("altowner1");
				
				strWhereClause = "altowner1 != '" + strPartOrg+ "'";  				
 				
				MapList totalresultList = DomainObject.findObjects(context,
						DomainConstants.TYPE_ECO, 
						"*", 
						"*",
						"*",
						"*",
						strWhereClause,
						null,
						true,
						strlChangeObjselects,
						(short) 0);
				

				Iterator itrChangeObjs = totalresultList.iterator();

				while (itrChangeObjs.hasNext()) {
					Map mapECO = (Map) itrChangeObjs.next();

					String strECOId = (String) mapECO.get(DomainConstants.SELECT_ID);
																			
					String strECOOrg = (String) mapECO.get("altowner1");				
										
					if (strECOOrg != null && strECOOrg.length() > 0 && !strPartOrg.equalsIgnoreCase(strECOOrg)) {
						strlExcludeOIDChangeObjs.add(strECOId);
					}
				}
			}

			StringList objectSelects = new StringList(1);
			objectSelects.addElement(DomainConstants.SELECT_ID);

			MapList mapListChangeObjs = doPart.getRelatedObjects(context,
					PropertyUtil.getSchemaProperty(context,	"relationship_AffectedItem"), 
					PropertyUtil.getSchemaProperty(context, "type_Change"),
					objectSelects,
					null,
					true,
					false,
					(short) 1,
					null,
					null);

			Iterator itrChangeObjs = mapListChangeObjs.iterator();

			while (itrChangeObjs.hasNext()) {
				Map mapChangeObj = (Map) itrChangeObjs.next();
				String strChangeObjId = (String) mapChangeObj.get(DomainConstants.SELECT_ID);
				strlExcludeOIDChangeObjs.add(strChangeObjId);
			}

		}		
		return strlExcludeOIDChangeObjs;

	}
    
//Add for RDO Convergence End
   
   
   
 //Add for IR-216979 start
   /**
    * getDefaultOrg() method returns current logged in Organization of the User.
  * @param context Context : User's Context.
  * @param args String array 
  * @return The String value of Organizaton Id.
  * @throws Exception if operation fails.
  */
   @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
   public StringList getDefaultOrg(Context context, String[] args)
   throws Exception
   {    	
	    String strDefRDOId = "";
	    StringList strlIncludeOIDOrgs = new StringList();
 	   	String strDefRDOName = EngineeringUtil.getDefaultOrganization(context); 		 			
 	    try {	            
 	    	strDefRDOId = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 select $4 dump $5",DomainConstants.TYPE_ORGANIZATION,strDefRDOName,"*","id","|"); 	        
 	        strDefRDOId = strDefRDOId.substring(strDefRDOId.lastIndexOf('|')+1);	 	        
 	        strlIncludeOIDOrgs.add(strDefRDOId);
 	    } catch(Exception Ex) {	        	 
 	    } 		 	
 		
 	   if(strlIncludeOIDOrgs.size()==0){
	    	strlIncludeOIDOrgs.add("");
	    }
 	    
 	    return strlIncludeOIDOrgs;
 	} 
   //Added for IR-216979 End
   
 //ENG Compliance Start
   /**
   * This method returns Project roles of the users.
   * @param context Context : User's Context.
   * @param strOrg logged in Default Organization
   * @return Maplist containing Project Roles and Person Id.
   * @throws Exception if operation fails.
   */
    public MapList getUserProjectRoles(Context context, String strOrg) throws Exception {
 		StringList objectSelects = new StringList(2);

 		objectSelects.addElement("to[" + DomainConstants.RELATIONSHIP_MEMBER+ "|from.name==\'"+strOrg+"\'].to.id");

 		objectSelects.addElement("to[" + DomainConstants.RELATIONSHIP_MEMBER+ "|from.name==\'"+strOrg+"\'].attribute["+ DomainConstants.ATTRIBUTE_PROJECT_ROLE + "]");


 		String strWhereClause = "to["+DomainConstants.RELATIONSHIP_MEMBER+"|from.name==\'"+strOrg+"\'].attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"] != \'\' " +

 		"&& to["+DomainConstants.RELATIONSHIP_MEMBER+"].from.name==\'" +strOrg+"\'";


 		MapList mlProjectRoles = DomainObject.findObjects(context,
 													DomainConstants.TYPE_PERSON,
 													"*",
 													"*",
 													"*", 
 													"*",
 													strWhereClause, 
 													null, 
 													true, 
 													objectSelects, 
 													(short) 0);
 	   return mlProjectRoles;
    }
    
    /**
     * This method returns Uers with VPLMProjectLeader role assigned.
     * @param context Context : User's Context.
     * @param strOrg logged in Default Organization 
     * @return StringList containing PersonId.
     * @throws Exception if operation fails.
     */
    public StringList getVPLMProjectLeaders(Context context, String strOrg) throws Exception {
 	   
 	    StringList slProjectLeaders = new StringList();
 	    String cmd = "list person $1 where $2 select $3 dump $4";
 		String strRolePersonIds = MqlUtil.mqlCommand(
 				context, cmd, "*", "assignment.org==\"" + strOrg + "\" && (assignment.parent==\"" + PropertyUtil.getSchemaProperty(context,
 								"role_VPLMProjectLeader") + "\")", "object.id", "|");
 		
 		slProjectLeaders = FrameworkUtil.split(strRolePersonIds,"\n");
 		
 	    return slProjectLeaders;
    }
    
    
    /**
     * This method returns Users with VPLMProjectLeader or Senior Design Engineer roles assigned.
     * @param context Context : User's Context.
     * @param args String array 
     * @return StringList containing PersonId.
     * @throws Exception if operation fails.
     */
 	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
 	public StringList getRDEDynamicSearch(Context context, String[] args)
 			throws Exception {
 		
 		StringList strlRolePersonIds = new StringList();
 		String strDefRDOName = EngineeringUtil.getDefaultOrganization(context);	
 				
 		MapList mlProjectRoles = getUserProjectRoles(context, strDefRDOName);
 		
 		if (mlProjectRoles != null && mlProjectRoles.size() > 0) {
 			for (int i = 0; i < mlProjectRoles.size(); i++) {
 				Map mPerson = (Map) mlProjectRoles.get(i);				
 				String strPersonId = (mPerson.get("to[" + DomainConstants.RELATIONSHIP_MEMBER+ "].to.id").toString()).trim();				
 				String strRoles = mPerson.get("to[" + DomainConstants.RELATIONSHIP_MEMBER+ "].attribute["+ DomainConstants.ATTRIBUTE_PROJECT_ROLE + "]").toString();

 				if (strRoles.contains("role_SeniorDesignEngineer")) {					
 					if(!strlRolePersonIds.contains(strPersonId))
 						strlRolePersonIds.add(strPersonId);
 				}
 			}
 		}
 				
 		strlRolePersonIds.addAll(getVPLMProjectLeaders(context, strDefRDOName));

 		return strlRolePersonIds;

 	}
 	
 	
 	 /**
 	    * This method returns Users with VPLMProjectLeader or Senior Manufacturing Engineer roles assigned.
 	    * @param context Context : User's Context.
 	    * @param args String array 
 	    * @return StringList containing PersonId.
 	    * @throws Exception if operation fails.
 	    */
 	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
 	public StringList getRMEDynamicSearch(Context context, String[] args)
 			throws Exception {

 		StringList strlRolePersonIds = new StringList();
 		String strDefRDOName = EngineeringUtil.getDefaultOrganization(context);	
 		
 		MapList mlProjectRoles = getUserProjectRoles(context, strDefRDOName);
 		
 		if (mlProjectRoles != null && mlProjectRoles.size() > 0) {
 			for (int i = 0; i < mlProjectRoles.size(); i++) {
 				Map mPerson = (Map) mlProjectRoles.get(i);
 				String strPersonId = (mPerson.get("to[" + DomainConstants.RELATIONSHIP_MEMBER+ "].to.id").toString()).trim();
 				String strRoles = mPerson.get("to[" + DomainConstants.RELATIONSHIP_MEMBER+ "].attribute["+ DomainConstants.ATTRIBUTE_PROJECT_ROLE + "]").toString();

 				if (strRoles.contains("role_SeniorManufacturingEngineer")) {
 					if (!strlRolePersonIds.contains(strPersonId))
 						strlRolePersonIds.add(strPersonId);
 				}
 			}
 		}

 		strlRolePersonIds.addAll(getVPLMProjectLeaders(context, strDefRDOName));
 		
 		return strlRolePersonIds;

 	}
    
 	//ENG Compliance END
 	/**
	 * Returns a StringList of the Context object id
	 * for a given context.
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args contains a packed HashMap containing objectId of object
	 * @return StringList.
	 * @throws Exception if the operation fails.
	*/
 	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
 	public StringList excludeObjectForMassChange(Context context, String[] args)
 	throws Exception{
 		
 		  HashMap programMap = (HashMap) JPO.unpackArgs(args);
 	      String  selPartObjectId = (String) programMap.get("selPartObjectId");
 	      String  contextObjectId = (String) programMap.get("contextObjectId");
 	      String  functionality = (String) programMap.get("Functionality");
 	     
 	      StringList result = new StringList();
 	      
 	     if (selPartObjectId == null){
         	return (result);
         }else{
 			result.add(selPartObjectId);
 		}
 	     if(functionality!= null && contextObjectId!=null && "MassChangeReplace".equals(functionality))
 	     {
 	    	result.add(contextObjectId);
 	     }	      
 	      
 		return result;
 	}
		/**
	 * Method for including Review Route templates owned by context user
	 * @param context
	 * @param args
	 * @return String
	 * @throws Exception
	 */
 	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public Object includeReviewRouteTemplates(Context context, String[] args) throws Exception {
		String objWhere = "attribute[" + DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE + "]== Review && current == Active && revision==last";
		MapList routeTemplateList = (MapList)getRouteTemplates(context,objWhere);
		return  getStringListFromMapList(routeTemplateList,DomainConstants.SELECT_ID);
	}
	/**
	 * Method for including Approval Route templates
	 * @param context
	 * @param args
	 * @return String
	 * @throws Exception
	 */
 	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public Object includeApprovalRouteTemplates(Context context, String[] args) throws Exception {
		String objWhere = "attribute[" + DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE + "]== Approval && current == Active && revision==last";
		MapList routeTemplateList = (MapList)getRouteTemplates(context,objWhere);
		return  getStringListFromMapList(routeTemplateList,DomainConstants.SELECT_ID);
	}
	
	/**
	   * Prepares Stringlist of values for the given key and from the given MapList.
	   * @param mList Maplist used to iterate.
	   * @param key the key used to retrieve from the map
	   * @return StringList of results
	   */
	   public StringList getStringListFromMapList(MapList mList, String key) {
	   	Map map;
	   	int size = mList.size();
	   	StringList listReturn = new StringList(size);

	   	for (int i = 0; i < size; i++) {
	   		map = (Map) mList.get(i);
	   		listReturn.addAll(getListValue(map, key));
	   	}

	   	return listReturn;
	   }
	   
	   /**
	    * Returns StringList of results by handling object either instance of String or StringList
	    * @param map the given map
	    * @param key the given key used to retrieve value form map.
	    * @return StringList of data
	    */
	   public static StringList getListValue(Map map, String key) {
	  		Object data = map.get(key);
	  		return (data == null) ? new StringList(0) : ((data instanceof String) ? new StringList((String) data) : (StringList) data);
	  	}
	   
	   /**
	     * Gets the MapList containing all Route Templates.
	     * @param context the eMatrix <code>Context</code> object
	     * @param args holds input arguments.
	     * @return a MapList containing all Route Templates.
	     * @throws Exception if the operation fails.
	     * @since ECM R216
	     */
	    
	    public Object getRouteTemplates(Context context, String objWhere) throws Exception
	    {

	         
	         String WorkspaceId                    = "to["+RouteTemplate.RELATIONSHIP_ROUTE_TEMPLATES+"].from.id";
	         String WorkspaceName                  = "to["+RouteTemplate.RELATIONSHIP_ROUTE_TEMPLATES+"].from.name";
	         String typeFilter                     = "to["+RouteTemplate.RELATIONSHIP_ROUTE_TEMPLATES+"].from.type";

	         String buFilter = RouteTemplate.SELECT_OWNING_ORGANIZATION_ID;         
	         if (objWhere != null)
	         {             
	             if(objWhere.indexOf("current == Active") != -1)
	                 objWhere=objWhere+" && latest==TRUE";
	              else
	                 objWhere=objWhere+" && revision==last";             
	         }else{
	             objWhere="revision==last";
	         }

	         SelectList selectStmts = new SelectList();

	         selectStmts.add(RouteTemplate.SELECT_NAME);
	         selectStmts.add(RouteTemplate.SELECT_DESCRIPTION);
	         selectStmts.add(RouteTemplate.SELECT_REVISION);
	         selectStmts.add(RouteTemplate.SELECT_OWNER);
	         selectStmts.add(RouteTemplate.SELECT_ROUTE_TEMPLATES_TYPE);

	         selectStmts.add(RouteTemplate.SELECT_OWNING_ORGANIZATION_ID);
	         selectStmts.add(RouteTemplate.SELECT_TYPE);
	         selectStmts.add(RouteTemplate.SELECT_ID);
	         selectStmts.add(WorkspaceId);
	         selectStmts.add(WorkspaceName);
	         selectStmts.add(typeFilter);
	         selectStmts.add(RouteTemplate.SELECT_RESTRICT_MEMBERS);
	         selectStmts.add( RouteTemplate.SELECT_OWNING_ORGANIZATION_NAME );



	         MapList templateMapList = new MapList();
	         MapList templatePersonMapList = new MapList();
	         MapList finalTemplateMap = new MapList();
	         try
	         {
	             String orgId = PersonUtil.getUserCompanyId(context);
	             DomainObject templateObj = DomainObject.newInstance(context);
	             Pattern OrgRelPattern = new Pattern(DomainConstants.RELATIONSHIP_ROUTE_TEMPLATES);
	             OrgRelPattern.addPattern(DomainConstants.RELATIONSHIP_DIVISION);
	             OrgRelPattern.addPattern(DomainConstants.RELATIONSHIP_COMPANY_DEPARTMENT );
	             Pattern OrgTypePattern = new Pattern(DomainConstants.TYPE_ROUTE_TEMPLATE);
	             OrgTypePattern.addPattern(DomainConstants.TYPE_BUSINESS_UNIT);
	             OrgTypePattern.addPattern(DomainConstants.TYPE_DEPARTMENT);
	             Pattern includeOrgRelPattern = new Pattern(DomainConstants.RELATIONSHIP_ROUTE_TEMPLATES);
	             Pattern includeOrgTypePattern = new Pattern(DomainConstants.TYPE_ROUTE_TEMPLATE);

	             //Get the enterprise level RouteTemplates
	             if(orgId != null)
	             {
	                 templateObj.setId(orgId);
	                 templateMapList = templateObj.getRelatedObjects(context,
	                                                                 OrgRelPattern.getPattern(),
	                                                                 OrgTypePattern.getPattern(),
	                                                                 selectStmts,
	                                                                 null,
	                                                                 false,//modified for bug 352540
	                                                                 true,
	                                                                 (short)0,
	                                                                 objWhere,
	                                                                 null,
	                                                                 includeOrgTypePattern,
	                                                                 includeOrgRelPattern,
	                                                                 null);

	             }

	 				templateObj.setId(PersonUtil.getPersonObjectID(context));

	                 ContextUtil.startTransaction(context,false);
	                 ExpansionIterator expIter = templateObj.getExpansionIterator(context,
	                		                                                     DomainConstants.RELATIONSHIP_ROUTE_TEMPLATES,
	                		                                                     DomainConstants.TYPE_ROUTE_TEMPLATE,
	                                                                             selectStmts,
	                                                                             new StringList(),
	                                                                             false,
	                                                                             true,
	                                                                             (short)1,
	                                                                             objWhere,
	                                                                             null,
	                                                                             (short)0,
	                                                                             false,
	                                                                             false,
	                                                                             (short)100,
	                                                                             false);

	                 templatePersonMapList =  FrameworkUtil.toMapList(expIter,(short)0,null,null,null,null);
	                 expIter.close();
	                 ContextUtil.commitTransaction(context);

	                 for (int i=0;i<templatePersonMapList.size();i++)
	                 {
	                     templateMapList.add(templatePersonMapList.get(i));
	                 }

	            //get the BUs for the context person
	 			ArrayList buList = getBURelatedToPerson (context);
	            Iterator templateMapListItr = templateMapList.iterator();
	            String contextPerson = context.getUser();
	            
	            String busUnitId = "";
	            String owner = "";
	            
	             Map templateMap;
	             DomainObject busUnitBO;

	             while(templateMapListItr.hasNext()){
	             	String type = "";
	                 templateMap = (Map)templateMapListItr.next();
	                 templateMap.remove("level");
	                // Modified for bug 352071
	                 String buFilterQuery=buFilter;
	                busUnitId = (String) templateMap.get(buFilterQuery);
	                String buTypeOfCompany=PropertyUtil.getSchemaProperty(context,"type_BusinessUnit");
	                String deptTypeOfCompany=PropertyUtil.getSchemaProperty(context,"type_Department");
	                 if(busUnitId != null && !"".equals(busUnitId))
	                 {
	                     busUnitBO = DomainObject.newInstance(context, busUnitId);
	                     type = (String) busUnitBO.getInfo(context,DomainConstants.SELECT_TYPE);
	                 }

	                 owner = (String) templateMap.get("owner");
	                 if (contextPerson.equals(owner)) {
	                     finalTemplateMap.add(templateMap);
	                 }

	                else if (buTypeOfCompany.equals(type)) {
	 				   	//Add the Template if the Context Person is part of the BU 					
	 				   	if (buList.contains(busUnitId)) {
	 						finalTemplateMap.add(templateMap);
	 					}
	                }
	                 else if (deptTypeOfCompany.equals(type)) {
	                         //Add the Template if the Context Person is part of the BU                         
	                         if (buList.contains(busUnitId)) {
	                             finalTemplateMap.add(templateMap);
	                         }
	                    }
	                  else {
	                    finalTemplateMap.add(templateMap);
	                }
	            }

	        } catch (Exception e) {

	        }

	        return finalTemplateMap;

	     }
	    
	    public ArrayList getBURelatedToPerson (Context context) throws Exception {

			ArrayList BUList = new ArrayList();
			SelectList selectStmts = new SelectList(1);
			selectStmts.add("id");

			DomainObject personObj = DomainObject.newInstance(context);
	        personObj.setId(com.matrixone.apps.common.Person.getPerson(context).getObjectId());
	        Pattern typePattern = new Pattern(PropertyUtil.getSchemaProperty(context,"type_BusinessUnit"));
	        typePattern.addPattern(DomainConstants.TYPE_BUSINESS_UNIT);
	        typePattern.addPattern(DomainConstants.TYPE_DEPARTMENT);
	        Pattern relPattern = new Pattern(PropertyUtil.getSchemaProperty(context,"relationship_BusinessUnitEmployee"));
	        relPattern.addPattern(DomainConstants.RELATIONSHIP_BUSINESS_UNIT_EMPLOYEE);
	        relPattern.addPattern(DomainConstants.RELATIONSHIP_MEMBER );
			String objWhere  = "";

	        MapList BUMapList = personObj.getRelatedObjects(context,
	                        relPattern.getPattern(),
	                        typePattern.getPattern(),
	                        selectStmts,
	                        null,
	                        true,
	                        true,
	                        (short)1,
	                        objWhere,
	                        "",
	                        null,
	                        null,
	                        null);

			Iterator BUListItr = BUMapList.iterator();
			String busUnitId = "";
			Map BUMap = null;
			while(BUListItr.hasNext()){
				BUMap = (Map)BUListItr.next();
				busUnitId = (String)BUMap.get("id");
				if (busUnitId!=null && !busUnitId.equals("")) {
					BUList.add(busUnitId);
				}
			}

			return BUList;
	}
 	
}
