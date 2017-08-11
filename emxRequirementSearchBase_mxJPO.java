/*
 *  emxRequirementSearchBase.java
 *
 *  JPO for finding Requirements, Decisions, and Use Cases.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 * 					    MM:DD:YY
 * @quickreview JX5		11:28:14 		IR-332766-3DEXPERIENCER2015x : Object linked as covered or derived requirement are getting displayed in search with in context
 * @quickreview LX6		06:11:15 		IR-350188-3DEXPERIENCER2016x : Search with in context  displays incorrect result. 
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.dassault_systemes.requirements.ReqConstants;
import com.dassault_systemes.requirements.ReqSchemaUtil;
import com.dassault_systemes.requirements.UnifiedAutonamingServices;
import com.matrixone.apps.common.Search;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.UISearchUtil;
import com.matrixone.apps.requirements.RequirementsUtil;
import com.matrixone.search.AccessRefinement;
import com.matrixone.search.AttributeRefinement;
import com.matrixone.search.CaseSensitiveAttributeRefinement;
import com.matrixone.search.CaseSensitiveMatrixSearch;
import com.matrixone.search.SearchRecord;
import com.matrixone.search.SearchRefinement;
import com.matrixone.search.SearchResult;
import com.matrixone.search.TaxonomyRefinement;


/**
 * The <code>emxRequirementSearchBase</code> class provides the functionality related to Search Requirement
 * @since X3
 */
public class emxRequirementSearchBase_mxJPO extends emxProductSearch_mxJPO
{
	// The operator names
	protected static final String OP_INCLUDES = "Includes";
	protected static final String OP_IS_EXACTLY = "IsExactly";
	protected static final String OP_IS_NOT = "IsNot";
	protected static final String OP_IS_MATCHES = "Matches";
	protected static final String OP_BEGINS_WITH = "BeginsWith";
	protected static final String OP_ENDS_WITH = "EndsWith";
	protected static final String OP_EQUALS = "Equals";
	protected static final String OP_DOES_NOT_EQUAL = "DoesNotEqual";
	protected static final String OP_IS_BETWEEN = "IsBetween";
	protected static final String OP_IS_ATMOST = "IsAtMost";
	protected static final String OP_IS_ATLEAST = "IsAtLeast";
	protected static final String OP_IS_MORE_THAN = "IsMoreThan";
	protected static final String OP_IS_LESS_THAN = "IsLessThan";
	protected static final String OP_IS_ON = "IsOn";
	protected static final String OP_IS_ON_OR_BEFORE = "IsOnOrBefore";
	protected static final String OP_IS_ON_OR_AFTER = "IsOnOrAfter";
	protected static final String OP_IS_MATCHLONG = "matchlong";

	// The operator symbols
	protected static final String SYMB_AND = " && ";
	protected static final String SYMB_OR = " || ";
	protected static final String SYMB_EQUAL = " == ";
	protected static final String SYMB_NOT_EQUAL = " != ";
	protected static final String SYMB_GREATER_THAN = " > ";
	protected static final String SYMB_LESS_THAN = " < ";
	protected static final String SYMB_GREATER_THAN_EQUAL = " >= ";
	protected static final String SYMB_LESS_THAN_EQUAL = " <= ";
	protected static final String SYMB_MATCH = " ~~ ";  // Short term fix for Bug #243366, was " ~~ "
	protected static final String SYMB_QUOTE = "'";
	protected static final String SYMB_WILD = "*";
	protected static final String SYMB_OPEN_PARAN = "(";
	protected static final String SYMB_CLOSE_PARAN = ")";
	protected static final String SYMB_ATTRIBUTE = "attribute";
	protected static final String SYMB_OPEN_BRACKET = "[";
	protected static final String SYMB_CLOSE_BRACKET = "]";

	protected static final String COMBO_PREFIX = "comboDescriptor_";
	protected static final String TXT_PREFIX = "txt_";

    // Keep the arguments for getRTTypeAhead()
    private Map<String, String> _inputDataTypeAhead = null;
    protected static final int NB_RESULT_PRINTED_TYPE_AHEAD = 5;


	/**
	 * The default constructor.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments.
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10.0.0.0
	 */
    public emxRequirementSearchBase_mxJPO(Context context, String[] args) throws Exception {
        super(context, args);

        // TypeAhead
        try {
            _inputDataTypeAhead = JPO.unpackArgs(args);
        } catch (Exception e) {
            // NOP
            _inputDataTypeAhead = null;
        }
    }

	/**
	 * Main entry point.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return an integer status code (0 = success)
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10-0-0-0
	 */
	public int mxMain(Context context, String[] args)
	throws Exception
	{
		if (!context.isConnected()){
			String sContentLabel = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Error.UnsupportedClient");
			throw  new Exception(sContentLabel);
		}
		return 0;
	}


	/**
	 * To obtain the Requirements.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containing the following arguments
	 *            queryLimit
	 *            Requirement Type
	 *            State
	 *            Owner
	 *            Name
	 *            Description
	 *            Priority
	 *            Vault Option
	 *            Vault Display
	 * @return MapList , the Object ids matching the search criteria
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10-0-0-0
	 */
	public static Object getRequirementsOLD(Context context, String[] args)
	throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));
		String strType = (String)programMap.get("TypeRequirement");
		if (strType==null || strType.equals(""))
			strType = SYMB_WILD;

		String strName = (String)programMap.get("Name");
		if (strName==null || strName.equals(""))
			strName = SYMB_WILD;

		String strRadio = (String)programMap.get("radio");
		String strRevision = null;
		boolean bLatestRevision = false;
		boolean bLatestReleasedRevision = false;
		if (strRadio != null) {
			if (strRadio.equals("input")) {
				strRevision = (String)programMap.get("Revision");
				strRevision = strRevision.trim();
			}
			else if (strRadio.equals("latestReleased")) {
				bLatestReleasedRevision = true;
			}
			else if (strRadio.equals("latest")) {
				bLatestRevision = true;
			}
		}
		if ((strRevision == null) || (strRevision.equals("")))
			strRevision = SYMB_WILD;

		String strState = (String)programMap.get("State");
		String strOwner = (String)programMap.get("OwnerDisplay");
		if (strOwner==null || strOwner.equals(""))
			strOwner = SYMB_WILD;

		String strDesc = (String)programMap.get("Description");
		String strPriority = (String)programMap.get("Priority");

		String strVault = null;
		String strVaultOption = (String)programMap.get("vaultOption");
		if (strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT) ||
		    strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS) ||
		    strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS)
		   )
			strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
		else
			strVault = (String)programMap.get("vaults");

		StringList select = new StringList(1);
		select.addElement(DomainConstants.SELECT_ID);

		boolean start = true;
		StringBuffer sbWhereExp = new StringBuffer(120);

		if ( strDesc!=null && (!strDesc.equals(SYMB_WILD)) && (!strDesc.equals("")) ) {
			if (start) {
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
			sbWhereExp.append(SYMB_MATCH);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(strDesc);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}

		if ( strState!=null && (!strState.equals(SYMB_WILD)) && (!strState.equals("")) ) {
			if (start) {
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			} else {
				sbWhereExp.append(SYMB_AND);
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(DomainConstants.SELECT_CURRENT);
			sbWhereExp.append(SYMB_MATCH);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(strState);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}

		if (bLatestReleasedRevision) {
			if (start) {
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			} else {
				sbWhereExp.append(SYMB_AND);
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(DomainConstants.SELECT_CURRENT);
			sbWhereExp.append(SYMB_EQUAL);
			sbWhereExp.append("Release");
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}

		if (bLatestRevision) {
			if (start) {
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			} else {
				sbWhereExp.append(SYMB_AND);
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(DomainConstants.SELECT_REVISION);
			sbWhereExp.append(SYMB_EQUAL);
			sbWhereExp.append(new Policy(ReqSchemaUtil.getRequirementPolicy(context)).hasMajorSequence(context) ? "majorid.lastmajorid.bestsofar.majorrevision" : "last");
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}

		if (  strPriority!=null && (!strPriority.equals(SYMB_WILD)) && (!strPriority.equals("")) ) {
			if (start) {
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			} else {
				sbWhereExp.append(SYMB_AND);
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(SYMB_ATTRIBUTE);
			sbWhereExp.append(SYMB_OPEN_BRACKET);
			sbWhereExp.append(ReqSchemaUtil.getPriorityAttribute(context));
			sbWhereExp.append(SYMB_CLOSE_BRACKET);
			sbWhereExp.append(SYMB_MATCH);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(strPriority);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}

		String strFilteredExpression = getFilteredExpression(context,programMap);

		if ( (strFilteredExpression != null) ) {
			if (start) {
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			} else {
				sbWhereExp.append(SYMB_AND);
			}
			sbWhereExp.append(strFilteredExpression);
		}

		if (!start) {
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}

		MapList mapList = null;
		mapList = DomainObject.findObjects(context, strType,strName, strRevision, strOwner, strVault, sbWhereExp.toString(), "", true, select, sQueryLimit);
		//Begin of Add by Vibhu,Enovia MatrixOne for Bug 311884 on 11/15/2005
		if(bLatestReleasedRevision) {
			mapList = filterForLatestRevisions(context,mapList);
		}
		//End of Add by Vibhu,Enovia MatrixOne for Bug 311884 on 11/15/2005
		mapList = obtainNonParentRequirements(context,mapList,programMap);
		return mapList;
	}


	/**
	 * To Obtain the Requirements excluding the cyclically dependent Requirements.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param mapList holds all the Requirements obtained by the mentioned Search criteria
	 * @param programMap holds the unpacked arguments
	 * @return a MapList of ids excluding the cyclically dependent ids.
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10-0-0-0
	 */
	protected static MapList obtainNonParentRequirements(Context context, MapList mapList, Map programMap)
	throws Exception
	{
		String strMode = (String)programMap.get(Search.REQ_PARAM_MODE);
		String strObjectId = (String)programMap.get(Search.REQ_PARAM_OBJECT_ID);

		if (strMode.equals(Search.ADD_EXISTING) )
		{
			StringList objSelects = new StringList(1);
			objSelects.addElement(DomainConstants.SELECT_ID);

			String strRelationship = ReqSchemaUtil.getSubRequirementRelationship(context);

			int sRecurse = 0;

			String strRelType =  strRelationship;
			String strType = ReqSchemaUtil.getRequirementType(context);

			DomainObject dom = new DomainObject(strObjectId);
			MapList newMapList = null;
			newMapList = dom.getRelatedObjects(context,strRelType,strType,true,false,sRecurse,objSelects,null,null,null,null,strType,null);

			for(int i=0;i<mapList.size();i++)
			{
				String strSelectedId = (String)( (Map)mapList.get(i) ).get(DomainConstants.SELECT_ID);
				strSelectedId = strSelectedId.trim();
				for(int j=0;j<newMapList.size();j++)
				{
					String strParentId = (String)( (Map)newMapList.get(j) ).get(DomainConstants.SELECT_ID);
					strParentId = strParentId.trim();

					if ( strSelectedId.equals(strParentId) )
					{
						mapList.remove(i);
						newMapList.remove(j);
						break;
					}
				}
			}
		}

		return mapList;
	}


	/**
	 * To obtain the Use Cases.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments of the request type
	 * @return MapList , the Object ids matching the search criteria
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10-0-0-0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public static Object getUseCases(Context context, String[] args)
	throws Exception
	{
		String strType = ReqSchemaUtil.getUseCaseType(context);
		return getCases(context, args,strType);
	}


	/**
	 * This method returns the default policy states of the type "UseCase".
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments of the request type
	 * @return String containing the HTML tag
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10-0-0-0
	 */
	public static Object getUseCaseStates(Context context, String[] args)
	throws Exception
	{
		return getStates(context, ReqSchemaUtil.getUseCaseType(context));
	}


	/**
	 * This method returns the default policy states of the type "Requirement".
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments of the request type
	 * @return String containing the HTML tag
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10-0-0-0
	 */
	public static Object getRequirementsStates(Context context, String[] args)
	throws Exception
	{
		return getStates(context, ReqSchemaUtil.getRequirementType(context));
	}


	//April 06,2006: Added by Enovia MatrixOne for Bug# 317903
	/**
	 * To obtain the Candidate Requirements.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containing the following arguments
	 *            queryLimit
	 *            Requirement Type
	 *            State
	 *            Owner
	 *            Name
	 *            Description
	 *            Priority
	 *            Vault Option
	 *            Vault Display
	 * @return MapList , the Object ids matching the search criteria
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10-6-SP2
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public static Object getCandidateRequirements(Context context, String[] args)
	throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));
		String strType = (String)programMap.get("TypeRequirement");
		if ( strType==null || strType.equals("")) {
			strType = SYMB_WILD;
		}
		String strName = (String)programMap.get("Name");
		if (strName==null || strName.equals("") )
		{
			strName = SYMB_WILD;
		}
		String strRadio = (String)programMap.get("radio");
		String strRevision = null;
		boolean bLatestRevision = false;
		boolean bLatestReleasedRevision = false;
		if (strRadio != null)
		{
			if (strRadio.equals("input"))
			{
				strRevision = (String)programMap.get("Revision");
				strRevision = strRevision.trim();
			}
			else if (strRadio.equals("latestReleased"))
			{
				bLatestReleasedRevision = true;
			}
			else if (strRadio.equals("latest"))
			{
				bLatestRevision = true;
			}
		}
		if ((strRevision == null) || (strRevision.equals("")))
		{
			strRevision = SYMB_WILD;
		}
		String strState = (String)programMap.get("State");
		String strOwner = (String)programMap.get("OwnerDisplay");
		if (  strOwner==null || strOwner.equals("") )
		{
			strOwner = SYMB_WILD;
		}
		String strDesc = (String)programMap.get("Description");
		String strPriority = (String)programMap.get("Priority");

		String strVault = null;
		String strVaultOption = (String)programMap.get("vaultOption");
		if (strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT) ||
		    strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS) ||
		    strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS)
		   )
			strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
		else
			strVault = (String)programMap.get("vaults");

		StringList select = new StringList(1);
		select.addElement(DomainConstants.SELECT_ID);
		boolean start = true;
		StringBuffer sbWhereExp = new StringBuffer(120);
		if ( strDesc!=null && (!strDesc.equals(SYMB_WILD)) && (!strDesc.equals("")) )
		{
			if (start)
			{
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
			sbWhereExp.append(SYMB_MATCH);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(strDesc);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}
		if ( strState!=null && (!strState.equals(SYMB_WILD)) && (!strState.equals("")) )
		{
			if (start)
			{
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			}
			else
			{
				sbWhereExp.append(SYMB_AND);
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(DomainConstants.SELECT_CURRENT);
			sbWhereExp.append(SYMB_MATCH);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(strState);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}
		if (bLatestReleasedRevision)
		{
			if (start)
			{
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			}
			else
			{
				sbWhereExp.append(SYMB_AND);
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(DomainConstants.SELECT_CURRENT);
			sbWhereExp.append(SYMB_EQUAL);
			sbWhereExp.append("Release");
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}
		if (bLatestRevision)
		{
			if (start)
			{
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			}
			else
			{
				sbWhereExp.append(SYMB_AND);
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(DomainConstants.SELECT_REVISION);
			sbWhereExp.append(SYMB_EQUAL);
			sbWhereExp.append(new Policy(ReqSchemaUtil.getRequirementPolicy(context)).hasMajorSequence(context) ? "majorid.lastmajorid.bestsofar.majorrevision" : "last");
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}
		if (  strPriority!=null && (!strPriority.equals(SYMB_WILD)) && (!strPriority.equals("")) )
		{
			if (start)
			{
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			}
			else
			{
				sbWhereExp.append(SYMB_AND);
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(SYMB_ATTRIBUTE);
			sbWhereExp.append(SYMB_OPEN_BRACKET);
			sbWhereExp.append(ReqSchemaUtil.getPriorityAttribute(context));
			sbWhereExp.append(SYMB_CLOSE_BRACKET);
			sbWhereExp.append(SYMB_MATCH);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(strPriority);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}
		//String strFilteredExpression = getFilteredExpression(context,programMap);
		String strObjectId = (String)programMap.get("objectId");
		if (start)
		{
			sbWhereExp.append(SYMB_OPEN_PARAN);
			start = false;
		}
		else
		{
			sbWhereExp.append(SYMB_AND);
		}
		sbWhereExp.append(SYMB_OPEN_PARAN);
		sbWhereExp.append(SYMB_OPEN_PARAN);
		sbWhereExp.append("!('to[");
		sbWhereExp.append(ReqSchemaUtil.getCandidateItemRelationship(context));
		sbWhereExp.append("].from.");
		sbWhereExp.append(DomainConstants.SELECT_ID);
		sbWhereExp.append("'==");
		sbWhereExp.append(strObjectId);
		sbWhereExp.append(")");
		sbWhereExp.append(SYMB_CLOSE_PARAN);
		sbWhereExp.append(SYMB_AND);
		sbWhereExp.append(SYMB_OPEN_PARAN);
		sbWhereExp.append("!('to[");
		sbWhereExp.append(ReqSchemaUtil.getCommittedItemRelationship(context));
		sbWhereExp.append("].from.");
		sbWhereExp.append(DomainConstants.SELECT_ID);
		sbWhereExp.append("'==");
		sbWhereExp.append(strObjectId);
		sbWhereExp.append(")");
		sbWhereExp.append(SYMB_CLOSE_PARAN);
		sbWhereExp.append(SYMB_AND);
		sbWhereExp.append(SYMB_OPEN_PARAN);
		sbWhereExp.append(DomainConstants.SELECT_ID);
		sbWhereExp.append("!='");
		sbWhereExp.append(strObjectId);
		sbWhereExp.append("'");
		sbWhereExp.append(SYMB_CLOSE_PARAN);
		sbWhereExp.append(SYMB_CLOSE_PARAN);
		if (!start)
		{
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}
		MapList mapList = null;
		mapList = DomainObject.findObjects(context, strType,strName, strRevision, strOwner, strVault, sbWhereExp.toString(), "", true, select, sQueryLimit);
		if(bLatestReleasedRevision)
		{
			mapList = filterForLatestRevisions(context,mapList);
		}
		mapList = obtainNonParentRequirements(context,mapList,programMap);
		return mapList;
	}

	/**
	 * Finds all the Decisions that are not currently linked to this Requirement.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments of the request type
	 * @return MapList , the Object ids matching the search criteria
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10-0-0-0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public static Object getDecisions(Context context, String[] args)
	throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

		String strName = (String)programMap.get("Name");
		if (strName==null || strName.equals(""))
			strName = SYMB_WILD;

		String strState = (String)programMap.get("State");
		String strTitle = (String)programMap.get("Title");
		String strDesc = (String)programMap.get("Description");
		String strOwner = (String)programMap.get("OwnerDisplay");
		if (strOwner==null || strOwner.equals(""))
			strOwner = SYMB_WILD;

		String strVault = null;
		String strVaultOption = (String)programMap.get("vaultOption");
		if (strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT) ||
		    strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS) ||
		    strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS)
		   )
			strVault = PersonUtil.getSearchVaults(context, false, strVaultOption);
		else
			strVault = (String) programMap.get("vaults");

		// T:Bug N:348631 R:0  Fixed 1/25/2008 by srickus
		StringBuffer sbWhereExp = new StringBuffer();
		if (strDesc != null && !strDesc.equals(SYMB_WILD) && !strDesc.equals(""))
		{
			sbWhereExp.append(getStatement(DomainConstants.SELECT_DESCRIPTION, strDesc, SYMB_MATCH));
		}
		if (strTitle != null && !strTitle.equals(SYMB_WILD) && !strTitle.equals(""))
		{
			if (sbWhereExp.length() > 0)
				sbWhereExp.append(SYMB_AND);

			String attName = SYMB_ATTRIBUTE + SYMB_OPEN_BRACKET + ReqSchemaUtil.getTitleAttribute(context) + SYMB_CLOSE_BRACKET;
			sbWhereExp.append(getStatement(attName, strTitle, SYMB_MATCH));
		}
		if (strState != null && !strState.equals(SYMB_WILD) && !strState.equals(""))
		{
			if (sbWhereExp.length() > 0)
				sbWhereExp.append(SYMB_AND);

			sbWhereExp.append(getStatement(DomainConstants.SELECT_CURRENT, strState, SYMB_MATCH));
		}

		String strWhere = (sbWhereExp.length() > 0? "(" + sbWhereExp + ")": null);
		StringList objSelects = new StringList(1);
		objSelects.addElement(DomainConstants.SELECT_ID);

		// First, find all the Decisions in the database...
		MapList objectList = DomainObject.findObjects(context,
			ReqSchemaUtil.getDecisionType(context),
			strName,
			SYMB_WILD,
			strOwner,
			strVault,
			strWhere,
			"",
			true,
			objSelects,
			sQueryLimit);

		MapList excludeList = null;
		String strRootId = (String)programMap.get(Search.REQ_PARAM_OBJECT_ID);
		if (strRootId != null)
		{
			// Then get a list of all Decisions linked to this Requirement...
			int sRecurse = 0;
			String strRelType = PropertyUtil.getSchemaProperty(context,(String)programMap.get(Search.REQ_PARAM_SRC_DEST_REL_NAME));
			DomainObject dom = new DomainObject(strRootId);
			excludeList = dom.getRelatedObjects(context,
				strRelType,
				ReqSchemaUtil.getDecisionType(context),
				false,
				true,
				sRecurse,
				objSelects,
				null,
				null,
				null,
				null,
				ReqSchemaUtil.getDecisionType(context),
				null);
		}

		// T:Bug N:348275 R:0  Fixed 1/25/2008 by srickus
		if (excludeList != null && excludeList.size() > 0)
		{
			// Build a list of IDs of all the Objects that should be excluded...
			HashSet excludeIds = new HashSet();
			for (int ii = 0; ii < excludeList.size(); ii++)
			{
				Map excludeMap = (Map) excludeList.get(ii);
				String excludeId = (String) excludeMap.get(DomainConstants.SELECT_ID);

				if (excludeId != null)
					excludeIds.add(excludeId);
			}

			// Now, remove the excluded Objects from the list of all Objects...
			for (int jj = objectList.size()-1; jj >= 0; jj--)
			{
				Map objectMap = (Map) objectList.get(jj);
				String objectId = (String) objectMap.get(DomainConstants.SELECT_ID);

				if (objectId != null && excludeIds.contains(objectId))
					objectList.remove(jj);
			}
		}

		return(objectList);
	}



	/**
	 * Finds all the Chapters that are not currently part of this Specification.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments of the request type
	 * @return MapList , the Object ids matching the search criteria
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10-0-0-0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public static Object getChapters(Context context, String[] args)
	throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

		String strName = (String)programMap.get("Name");
		if (strName==null || strName.equals(""))
			strName = SYMB_WILD;

		String strState = (String)programMap.get("State");
		String strDesc = (String)programMap.get("Description");
		String strOwner = (String)programMap.get("OwnerDisplay");
		if (strOwner==null || strOwner.equals(""))
			strOwner = SYMB_WILD;

		String strVault = null;
		String strVaultOption = (String)programMap.get("vaultOption");
		if (strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT) ||
		    strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS) ||
		    strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS)
		   )
			strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
		else
			strVault = (String)programMap.get("vaults");

		// T:Bug N:348631 R:0  Fixed 1/25/2008 by srickus
		StringBuffer sbWhereExp = new StringBuffer();
		if (strDesc != null && !strDesc.equals(SYMB_WILD) && !strDesc.equals(""))
		{
			sbWhereExp.append(getStatement(DomainConstants.SELECT_DESCRIPTION, strDesc, SYMB_MATCH));
		}
		if (strState != null && !strState.equals(SYMB_WILD) && !strState.equals(""))
		{
			if (sbWhereExp.length() > 0)
				sbWhereExp.append(SYMB_AND);

			sbWhereExp.append(getStatement(DomainConstants.SELECT_CURRENT, strState, SYMB_MATCH));
		}

		String strWhere = (sbWhereExp.length() > 0? "(" + sbWhereExp + ")": null);
		StringList objSelects = new StringList(1);
		objSelects.addElement(DomainConstants.SELECT_ID);

		// First, find all the Chapters in the database...
		MapList objectList = DomainObject.findObjects(context,
			ReqSchemaUtil.getChapterType(context),
			strName,
			SYMB_WILD,
			strOwner,
			strVault,
			strWhere,
			"",
			true,
			objSelects,
			sQueryLimit);

		MapList excludeList = null;
		String strRootId = (String)programMap.get(Search.REQ_PARAM_OBJECT_ID);
		if (strRootId != null)
		{
			// Then get a list of all Chapters within this Specification...
			int sRecurse = 0;
			String strRelType = PropertyUtil.getSchemaProperty(context,(String)programMap.get(Search.REQ_PARAM_SRC_DEST_REL_NAME));
			DomainObject dom = new DomainObject(strRootId);
			excludeList = dom.getRelatedObjects(context,
				strRelType,
				ReqSchemaUtil.getChapterType(context),
				false,
				true,
				sRecurse,
				objSelects,
				null,
				null,
				null,
				null,
				ReqSchemaUtil.getChapterType(context),
				null);
		}

		// T:Bug N:348275 R:0  Fixed 1/16/2008 by srickus
		if (excludeList != null && excludeList.size() > 0)
		{
			// Build a list of IDs of all the Objects that should be excluded...
			HashSet excludeIds = new HashSet();
			for (int ii = 0; ii < excludeList.size(); ii++)
			{
				Map excludeMap = (Map) excludeList.get(ii);
				String excludeId = (String) excludeMap.get(DomainConstants.SELECT_ID);

				if (excludeId != null)
					excludeIds.add(excludeId);
			}

			// Now, remove the excluded Objects from the list of all Objects...
			for (int jj = objectList.size()-1; jj >= 0; jj--)
			{
				Map objectMap = (Map) objectList.get(jj);
				String objectId = (String) objectMap.get(DomainConstants.SELECT_ID);

				if (objectId != null && excludeIds.contains(objectId))
					objectList.remove(jj);
			}
		}

		return(objectList);
	}

	/**
	 * To obtain the Comments.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments of the request type
	 * @return MapList , the Object ids matching the search criteria
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10-0-0-0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public static Object getComments(Context context, String[] args)
	throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

		String strName = (String)programMap.get("Name");
		if (strName==null || strName.equals(""))
			strName = SYMB_WILD;

		String strState = (String)programMap.get("State");
		String strDesc = (String)programMap.get("Description");
		String strOwner = (String)programMap.get("OwnerDisplay");
		if (strOwner==null || strOwner.equals(""))
			strOwner = SYMB_WILD;

		String strVault = null;
		String strVaultOption = (String)programMap.get("vaultOption");
		if (strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT) ||
		    strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS) ||
		    strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS)
		   )
			strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
		else
			strVault = (String)programMap.get("vaults");

		// T:Bug N:348631 R:0  Fixed 1/25/2008 by srickus
		StringBuffer sbWhereExp = new StringBuffer();
		if (strDesc != null && !strDesc.equals(SYMB_WILD) && !strDesc.equals(""))
		{
			sbWhereExp.append(getStatement(DomainConstants.SELECT_DESCRIPTION, strDesc, SYMB_MATCH));
		}
		if (strState != null && !strState.equals(SYMB_WILD) && !strState.equals(""))
		{
			if (sbWhereExp.length() > 0)
				sbWhereExp.append(SYMB_AND);

			sbWhereExp.append(getStatement(DomainConstants.SELECT_CURRENT, strState, SYMB_MATCH));
		}

		String strWhere = (sbWhereExp.length() > 0? "(" + sbWhereExp + ")": null);
		StringList objSelects = new StringList(1);
		objSelects.addElement(DomainConstants.SELECT_ID);

		MapList objectList = DomainObject.findObjects(context,
			ReqSchemaUtil.getCommentType(context),
			strName,
			SYMB_WILD,
			strOwner,
			strVault,
			strWhere,
			"",
			true,
			objSelects,
			sQueryLimit);

		return(objectList);
	}



	/**
	 * This method returns the default policy states of the type "Decision".
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments of the request type
	 * @return String containing the HTML tag
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10-0-0-0
	 */
	public static Object getDecisionStates(Context context, String[] args)
	throws Exception
	{
		return getStates(context, ReqSchemaUtil.getDecisionType(context));
	}

	/**
	 * This method returns the default policy states of the type "Chapter".
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments of the request type
	 * @return String containing the HTML tag
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10-0-0-0
	 */
	public static Object getChapterStates(Context context, String[] args)
	throws Exception
	{
		return getStates(context, ReqSchemaUtil.getChapterType(context));
	}

	/**
	 * This method returns the default policy states of the type "Chapter".
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments of the request type
	 * @return String containing the HTML tag
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10-0-0-0
	 */
	public static Object getCommentStates(Context context, String[] args)
	throws Exception
	{
		return getStates(context, ReqSchemaUtil.getCommentType(context));
	}

	/**
	 * To obtain the Requirements.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containing the following arguments
	 *            queryLimit
	 *            Requirement Type
	 *            State
	 *            Name
	 *            Description
	 *            Priority
	 *            Vault Option
	 *            Vault Display and attribues
	 * @return MapList , the Object ids matching the search criteria
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10-7-x+2
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public static Object getRequirements(Context context, String[] args)
	throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));
		String strType = (String)programMap.get("txtTypeActual");
		String strActualContainedIn = (String)programMap.get("txtActualContainedIn");
		String strName = (String)programMap.get("Name");
		String strRadio = (String)programMap.get("radio");
		String strRevision = null;
		String strState = (String)programMap.get("State");
		String strOwner = (String)programMap.get("OwnerDisplay");
		String strDesc = (String)programMap.get("Description");
		String strPriority = (String)programMap.get("Priority");
		String strVault = null;
		String strVaultOption = (String)programMap.get("vaultOption");
		String strCommandName = (String)programMap.get("CommandName");
		String strTimeZone = (String) programMap.get("timeZone");

		double clientTZOffset =(new Double(strTimeZone)).doubleValue();
		java.util.Locale locale = (java.util.Locale)programMap.get("localeObj");
		boolean bLatestRevision = false;
		boolean bLatestReleasedRevision = false;

		if ("null".equals(strType) || "".equals(strType) || strType == null) {
			strType = SYMB_WILD;
		}

		if ("null".equals(strName) || "".equals(strName) || strName == null ) {
			strName = SYMB_WILD;
		}

		if (!"null".equals(strRadio) && !"".equals(strRadio) && strRadio != null) {
			if (strRadio.equals("input")) {
				strRevision = (String)programMap.get("Revision");
				strRevision = strRevision.trim();
			} else if (strRadio.equals("latestReleased")) {
				bLatestReleasedRevision = true;
			} else if (strRadio.equals("latest")) {
				bLatestRevision = true;
			}
		}
		if ("null".equals(strRevision) || "".equals(strRevision) || strRevision == null)  {
			strRevision = SYMB_WILD;
		}


		if ("null".equals(strOwner) || "".equals(strOwner) || strOwner == null)  {
			strOwner = SYMB_WILD;
		}


		if (PersonUtil.SEARCH_DEFAULT_VAULT.equalsIgnoreCase(strVaultOption) ||
				PersonUtil.SEARCH_LOCAL_VAULTS.equalsIgnoreCase(strVaultOption) ||
				PersonUtil.SEARCH_ALL_VAULTS.equalsIgnoreCase(strVaultOption)
		   )
			strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
		else
			strVault = (String)programMap.get("vaults");

		StringList select = new StringList(1);
		select.addElement(DomainConstants.SELECT_ID);

		boolean start = true;
		StringBuffer sbWhereExp = new StringBuffer(120);

		if ( strDesc!=null && (!strDesc.equals(SYMB_WILD)) && (!strDesc.equals("")) ) {
			if (start) {
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
			sbWhereExp.append(SYMB_MATCH);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(strDesc);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}

		if ( strState!=null && (!strState.equals(SYMB_WILD)) && (!strState.equals("")) ) {
			if (start) {
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			} else {
				sbWhereExp.append(SYMB_AND);
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(DomainConstants.SELECT_CURRENT);
			sbWhereExp.append(SYMB_MATCH);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(strState);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}

		if (bLatestReleasedRevision) {
			if (start) {
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			} else {
				sbWhereExp.append(SYMB_AND);
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(DomainConstants.SELECT_CURRENT);
			sbWhereExp.append(SYMB_EQUAL);
			sbWhereExp.append("Release");
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}

		if (bLatestRevision) {
			if (start) {
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			} else {
				sbWhereExp.append(SYMB_AND);
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(DomainConstants.SELECT_REVISION);
			sbWhereExp.append(SYMB_EQUAL);
			sbWhereExp.append(new Policy(ReqSchemaUtil.getRequirementPolicy(context)).hasMajorSequence(context) ? "majorid.lastmajorid.bestsofar.majorrevision" : "last");
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}

		if (  strPriority!=null && (!strPriority.equals(SYMB_WILD)) && (!strPriority.equals("")) ) {
			if (start) {
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			} else {
				sbWhereExp.append(SYMB_AND);
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(SYMB_ATTRIBUTE);
			sbWhereExp.append(SYMB_OPEN_BRACKET);
			sbWhereExp.append(ReqSchemaUtil.getPriorityAttribute(context));
			sbWhereExp.append(SYMB_CLOSE_BRACKET);
			sbWhereExp.append(SYMB_MATCH);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(strPriority);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}

		//Filter the specificaions with version policy
		if ("RMTSpecificationSearchCommand".equals(strCommandName) || "RMTSpecificationSearchImport".equals(strCommandName)
				|| "RMTGlobalSearchSpecificationCommand".equals(strCommandName) || "RMTCreateTargetSearchCommand".equals(strCommandName)) {
			if (start) {
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			} else {
				sbWhereExp.append(SYMB_AND);
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(DomainObject.SELECT_POLICY);
			sbWhereExp.append(SYMB_NOT_EQUAL);
			sbWhereExp.append(ReqSchemaUtil.getVersionPolicy(context));
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}

		String strAdvOperator = (String)programMap.get("radAdvancedOperator");
		String strIntermediateOperator = SYMB_AND;
		if (strAdvOperator != null) {
			if (strAdvOperator.equalsIgnoreCase("and")) {
				strIntermediateOperator = SYMB_AND;
			} else {
				strIntermediateOperator = SYMB_OR;
			}
		}
		StringBuffer sbWhereExpSecond = new StringBuffer(150);
		Set keySet = programMap.keySet();
		int iPrefixLength = COMBO_PREFIX.length();
		for (Iterator iter=keySet.iterator(); iter.hasNext(); ) {
			String key = (String)iter.next();
			if (key.length() > iPrefixLength) {
				if (key.startsWith(COMBO_PREFIX)) {
					String strOperator = (String)programMap.get(key);
					strOperator = strOperator.trim();
					if (!strOperator.equals(SYMB_WILD)) {
						String strSelectFieldName = key.substring(iPrefixLength);
						String strActualFieldName = TXT_PREFIX + strSelectFieldName;
						String strActualValue = (String)programMap.get(strSelectFieldName);
						if ((strActualValue == null) || strActualValue.equals("")) {
							strActualValue = (String)programMap.get(strActualFieldName);
						}
						if (strActualValue != null) {
							if ( !(strActualValue.equals("*")) && !(strActualValue.equals("")) ) {
								if (start) {
									sbWhereExpSecond.append(SYMB_OPEN_PARAN);
									start = false;
								} else {
									sbWhereExpSecond.append(strIntermediateOperator);
								}
								if (strOperator.equals(OP_IS_BETWEEN)) {
									sbWhereExpSecond.append(SYMB_OPEN_PARAN);
								}
								sbWhereExpSecond.append(SYMB_OPEN_PARAN);
								if ((!strSelectFieldName.equalsIgnoreCase(DomainConstants.SELECT_MODIFIED)) && (!strSelectFieldName.equalsIgnoreCase(DomainConstants.SELECT_ORIGINATED))) {
									sbWhereExpSecond.append(SYMB_ATTRIBUTE);
									sbWhereExpSecond.append(SYMB_OPEN_BRACKET);
								}
								sbWhereExpSecond.append(strSelectFieldName);
								if ((!strSelectFieldName.equalsIgnoreCase(DomainConstants.SELECT_MODIFIED)) && (!strSelectFieldName.equalsIgnoreCase(DomainConstants.SELECT_ORIGINATED))) {
									sbWhereExpSecond.append(SYMB_CLOSE_BRACKET);
								}
								if (strOperator.equals(OP_INCLUDES)) {
									if (strSelectFieldName.equals(ReqSchemaUtil.getContentTextAttribute(context)))
										sbWhereExpSecond.append(OP_IS_MATCHLONG);
									else
										sbWhereExpSecond.append(SYMB_MATCH);
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(SYMB_WILD);
									sbWhereExpSecond.append(strActualValue);
									sbWhereExpSecond.append(SYMB_WILD);
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
								} else if (strOperator.equals(OP_IS_EXACTLY)) {
									sbWhereExpSecond.append(SYMB_EQUAL);
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(strActualValue);
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
								} else if (strOperator.equals(OP_IS_NOT)) {
									sbWhereExpSecond.append(SYMB_NOT_EQUAL);
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(strActualValue);
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
								} else if (strOperator.equals(OP_IS_MATCHES)) {
									if (strSelectFieldName.equals(ReqSchemaUtil.getContentTextAttribute(context)))
										sbWhereExpSecond.append(OP_IS_MATCHLONG);
									else
										sbWhereExpSecond.append(SYMB_MATCH);
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(strActualValue);
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
								} else if (strOperator.equals(OP_BEGINS_WITH)) {
									if (strSelectFieldName.equals(ReqSchemaUtil.getContentTextAttribute(context)))
										sbWhereExpSecond.append(OP_IS_MATCHLONG);
									else
										sbWhereExpSecond.append(SYMB_MATCH);
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(strActualValue);
									sbWhereExpSecond.append(SYMB_WILD);
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
								} else if (strOperator.equals(OP_ENDS_WITH)) {
									if (strSelectFieldName.equals(ReqSchemaUtil.getContentTextAttribute(context)))
										sbWhereExpSecond.append(OP_IS_MATCHLONG);
									else
										sbWhereExpSecond.append(SYMB_MATCH);
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(SYMB_WILD);
									sbWhereExpSecond.append(strActualValue);
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
								} else if (strOperator.equals(OP_EQUALS)) {
									sbWhereExpSecond.append(SYMB_EQUAL);
									sbWhereExpSecond.append(strActualValue);
									sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
								} else if (strOperator.equals(OP_DOES_NOT_EQUAL)) {
									sbWhereExpSecond.append(SYMB_NOT_EQUAL);
									sbWhereExpSecond.append(strActualValue);
									sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
								} else if (strOperator.equals(OP_IS_BETWEEN)) {
									strActualValue = strActualValue.trim();

									int iSpace   = strActualValue.indexOf(" ");
									String strLow  = "";
									String strHigh = "";

									if (iSpace == -1) {
										strLow  = strActualValue;
										strHigh = strActualValue;
									} else {
										strLow  = strActualValue.substring(0,iSpace);
										strHigh = strActualValue.substring(strLow.length()+1);

										// Check for extra values and ignore
										iSpace = strHigh.indexOf(" ");

										if (iSpace != -1) {
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
								} else if (strOperator.equals(OP_IS_ATMOST)) {
									sbWhereExpSecond.append(SYMB_LESS_THAN_EQUAL);
									sbWhereExpSecond.append(strActualValue);
									sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
								} else if (strOperator.equals(OP_IS_ATLEAST)) {
									sbWhereExpSecond.append(SYMB_GREATER_THAN_EQUAL);
									sbWhereExpSecond.append(strActualValue);
									sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
								} else if (strOperator.equals(OP_IS_MORE_THAN)) {
									sbWhereExpSecond.append(SYMB_GREATER_THAN);
									sbWhereExpSecond.append(strActualValue);
									sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
								} else if (strOperator.equals(OP_IS_LESS_THAN)) {
									sbWhereExpSecond.append(SYMB_LESS_THAN);
									sbWhereExpSecond.append(strActualValue);
									sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
								} else if (strOperator.equals(OP_IS_ON)) {
									sbWhereExpSecond.append(SYMB_LESS_THAN_EQUAL);
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(eMatrixDateFormat.getFormattedInputDateTime(context, strActualValue,"11:59:59 PM", clientTZOffset,locale));
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(SYMB_AND);
									sbWhereExpSecond.append(strSelectFieldName);
									sbWhereExpSecond.append(SYMB_GREATER_THAN_EQUAL);
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(eMatrixDateFormat.getFormattedInputDateTime(context, strActualValue,"12:00:00 AM", clientTZOffset,locale));
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
								} else if (strOperator.equals(OP_IS_ON_OR_BEFORE)) {
									sbWhereExpSecond.append(SYMB_LESS_THAN_EQUAL);
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(eMatrixDateFormat.getFormattedInputDateTime(context, strActualValue,"11:59:59 PM", clientTZOffset,locale));
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
								} else if (strOperator.equals(OP_IS_ON_OR_AFTER)) {
									sbWhereExpSecond.append(SYMB_GREATER_THAN_EQUAL);
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(eMatrixDateFormat.getFormattedInputDateTime(context, strActualValue,"12:00:00 AM", clientTZOffset,locale));
									sbWhereExpSecond.append(SYMB_QUOTE);
									sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
								}
							}
						}
					}
				}
			}
		}


		if (!start) {
			sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
		}

		String strWhereExpFirst = sbWhereExp.toString();
		int iLenFirst = strWhereExpFirst.length();
		String strWhereExpSecond = sbWhereExpSecond.toString();
		int iLenSecond = strWhereExpSecond.length();

		StringBuffer sbfinalWhereExp = new StringBuffer(250);
		String strWhereExp = "";

		if ( (iLenFirst > 0) && (iLenSecond > 0) ) {
			sbfinalWhereExp.append(strWhereExpFirst);
			sbfinalWhereExp.append(strWhereExpSecond);
			strWhereExp = sbfinalWhereExp.toString();
		} else if (iLenFirst > 0) {
			strWhereExp = strWhereExpFirst;
		} else if (iLenSecond > 0) {
			strWhereExp = strWhereExpSecond;
		}
		MapList mapList = new MapList();
		StringList idsList = new StringList();
		if("".equals(strActualContainedIn) || "null".equals(strActualContainedIn) || strActualContainedIn==null) {
			mapList = DomainObject.findObjects(context, strType,strName, strRevision, strOwner, strVault, strWhereExp, "", true, select, sQueryLimit);
		}
		else {
			DomainObject domObj = null;
			String strContainedInObj = "";
			//Commented the below code to get only the Requirement objects, which are connected to particular Specification object through SpecificationStructure relationship

			/*StringBuffer relNamesBuffer=new StringBuffer();
				relNamesBuffer.append(RELATIONSHIP_SPECIFICATION_STRUCTURE);
				relNamesBuffer.append(SYMB_COMMA);
				relNamesBuffer.append(RELATIONSHIP_SUB_REQUIREMENT);
				relNamesBuffer.append(SYMB_COMMA);
				relNamesBuffer.append(RELATIONSHIP_DERIVED_REQUIREMENT);
				*/
			start=true;
			StringBuffer sbObjWhereExp = new StringBuffer(120);
			if(!"".equals(strWhereExp) &&!"null".equals(strWhereExp) && strWhereExp!=null ) {
				sbObjWhereExp.append(strWhereExp);
				start=false;
			}

			if(!"*".equals(strName)) {
				if(!start) {
					sbObjWhereExp.append(SYMB_AND);
				}
				else {
					start=false;
				}
				sbObjWhereExp.append(SYMB_OPEN_PARAN);
				sbObjWhereExp.append(DomainConstants.SELECT_NAME);
				sbObjWhereExp.append(SYMB_MATCH);
				sbObjWhereExp.append(SYMB_QUOTE);
				sbObjWhereExp.append(strName);
				sbObjWhereExp.append(SYMB_QUOTE);
				sbObjWhereExp.append(SYMB_CLOSE_PARAN);
			}
			if(!"*".equals(strOwner)) {
				if(!start) {
					sbObjWhereExp.append(SYMB_AND);
				}
				else {
					start=false;
				}
				sbObjWhereExp.append(SYMB_OPEN_PARAN);
				sbObjWhereExp.append(DomainConstants.SELECT_OWNER);
				sbObjWhereExp.append(SYMB_MATCH);
				sbObjWhereExp.append(SYMB_QUOTE);
				sbObjWhereExp.append(strOwner);
				sbObjWhereExp.append(SYMB_QUOTE);
				sbObjWhereExp.append(SYMB_CLOSE_PARAN);
			}
			if(!"*".equals(strRevision)) {
				if(!start) {
					sbObjWhereExp.append(SYMB_AND);
				}
				else {
					start=false;
				}
				sbObjWhereExp.append(SYMB_OPEN_PARAN);
				sbObjWhereExp.append(DomainConstants.SELECT_REVISION);
				sbObjWhereExp.append(SYMB_MATCH);
				sbObjWhereExp.append(SYMB_QUOTE);
				sbObjWhereExp.append(strRevision);
				sbObjWhereExp.append(SYMB_QUOTE);
				sbObjWhereExp.append(SYMB_CLOSE_PARAN);
			}
			if(!"".equals(strVault) && !"null".equals(strVault) && strVault!=null) {
				if(!start) {
					sbObjWhereExp.append(SYMB_AND);
				}
				else {
					start=false;
				}
				sbObjWhereExp.append(SYMB_OPEN_PARAN);
				sbObjWhereExp.append(DomainConstants.SELECT_VAULT);
				sbObjWhereExp.append(SYMB_MATCH);
				sbObjWhereExp.append(SYMB_QUOTE);
				sbObjWhereExp.append(strVault);
				sbObjWhereExp.append(SYMB_QUOTE);
				sbObjWhereExp.append(SYMB_CLOSE_PARAN);
			}

			String strObjWhereExp =sbObjWhereExp.toString();
			if(!"".equals(strObjWhereExp) &&!"null".equals(strObjWhereExp) && strObjWhereExp!=null)
			{
				sbObjWhereExp.insert(0,SYMB_OPEN_PARAN);
				sbObjWhereExp.append(SYMB_CLOSE_PARAN);
			}
			strObjWhereExp =sbObjWhereExp.toString();
			StringList objSelectList = new StringList();
			objSelectList.add("id");
			StringTokenizer st = new StringTokenizer(strActualContainedIn, ",");
			while (st.hasMoreTokens() && (sQueryLimit == 0 || sQueryLimit > mapList.size())) {
				strContainedInObj=st.nextToken().trim();
				domObj = new DomainObject(strContainedInObj);
				MapList reqObjMapList = domObj.getRelatedObjects(context,ReqSchemaUtil.getSpecStructureRelationship(context),"*",objSelectList,null,false,true,(short)0,strObjWhereExp,null);
				Iterator itr = reqObjMapList.iterator();
				while (itr.hasNext() && (sQueryLimit == 0 || sQueryLimit > mapList.size()))  {
					Map reqObjMap = (Map)itr.next();
					String strObjId = (String)reqObjMap.get("id");
					String strQuery = "print bus $1 select $2 dump";
					String strResult = MqlUtil.mqlCommand(context, strQuery, strObjId, "type.kindOf[Requirement]");
					if ("TRUE".equalsIgnoreCase(strResult)){
						if (!idsList.contains(strObjId)){
							Map tempMap = new HashMap();
							tempMap.put("id",strObjId);
							mapList.add(tempMap);
							idsList.add(strObjId);
						}
					}
				}
			}
		}

		if("RMTDerivedRequirementSearchCommand".equals(strCommandName) || "RMTSubRequirementSearchCommand".equals(strCommandName)){
			String tableRowId = (String)programMap.get("emxParentTableRowId");
        	String[] tokens = tableRowId.split("[|]", -1);
        	String strRootId = null;
        	if(tokens.length == 1){
        		strRootId = tokens[0];
        	}else{
        		strRootId = tokens[1];
        	}


			StringList objSelects = new StringList(1);
			objSelects.addElement(DomainConstants.SELECT_ID);

			MapList excludeList = null;
			if (strRootId != null)
			{
				// Then get a list of parent requirements
				int sRecurse = 0;
				StringBuffer sbRelSelect = new StringBuffer();
				sbRelSelect = sbRelSelect.append(ReqSchemaUtil.getSubRequirementRelationship(context))
                					 .append(",")
                					 .append(ReqSchemaUtil.getDerivedRequirementRelationship(context));
				DomainObject dom = new DomainObject(strRootId);
				excludeList = dom.getRelatedObjects(context,
					sbRelSelect.toString(),
					ReqSchemaUtil.getRequirementType(context),
					true,
					false,
					sRecurse,
					objSelects,
					null,
					null,
					null,
					null,
					null,
					null);

				// Build a list of IDs of all the Objects that should be excluded...
				HashSet excludeIds = new HashSet();
				for (int ii = 0; ii < excludeList.size(); ii++)
				{
					Map excludeMap = (Map) excludeList.get(ii);
					String excludeId = (String) excludeMap.get(DomainConstants.SELECT_ID);

					if (excludeId != null)
						excludeIds.add(excludeId);
				}

				excludeIds.add(strRootId);

				// Now, remove the excluded Objects from the list of all Objects...
				for (int jj = mapList.size()-1; jj >= 0; jj--)
				{
					Map objectMap = (Map) mapList.get(jj);
					String objectId = (String) objectMap.get(DomainConstants.SELECT_ID);

					if (objectId != null && excludeIds.contains(objectId))
						mapList.remove(jj);
				}
			}
		}

		return mapList;
	} // End of getRequirement method

	/**
	 * Method exclude the Parent Requirements Ids
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return StringList list of the all the requirement objects having parent Ids
	 * @throws Exception if operation fails
	 * @since X3
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public static StringList excludeParentRequirements(Context context, String[] args)
	   throws Exception
    {
      StringList excludeIds = new StringList();

      Map programMap = (Map) JPO.unpackArgs(args);
      String strObjectId = (String) programMap.get("objectId");
      String strTblRowId = (String) programMap.get("emxTableRowId");

      String[] objectIds = strObjectId == null? strTblRowId.split("[|]"): strObjectId.split("[|]");

      if (objectIds != null && objectIds.length > 1)
         strObjectId = objectIds[1];

      if (strObjectId != null && strObjectId.length() > 0){
			StringList objSelects = new StringList(1);
			objSelects.addElement(DomainConstants.SELECT_ID);

			MapList excludeList = null;
				// Then get a list of parent requirements
				int sRecurse = 0;
				StringBuffer sbRelSelect = new StringBuffer();
				sbRelSelect = sbRelSelect.append(ReqSchemaUtil.getSubRequirementRelationship(context))
                					 .append(",")
                					 .append(ReqSchemaUtil.getDerivedRequirementRelationship(context));
				DomainObject dom = new DomainObject(strObjectId);
				excludeList = dom.getRelatedObjects(context, sbRelSelect.toString(), ReqSchemaUtil.getRequirementType(context),
					true, false, sRecurse, objSelects, null, null, null, null, null, null);

				// Build a list of IDs of all the Objects that should be excluded...
				for (int ii = 0; ii < excludeList.size(); ii++)
				{
					Map excludeMap = (Map) excludeList.get(ii);
					String excludeId = (String) excludeMap.get(DomainConstants.SELECT_ID);

					if (excludeId != null)
						excludeIds.add(excludeId);
				}

				//Start:oep
				String data = MqlUtil.mqlCommand(context, "PRINT BUS $1 SELECT $2  DUMP $3", strObjectId, "revisions.id", "|");
				if(data!=null)
				{
					StringTokenizer toker = new StringTokenizer(data, "|", false);
					while(toker.hasMoreElements()){
						String strObjId = toker.nextToken();
						excludeIds.add(strObjId);
					}
				}
				else
				{
					excludeIds.add(strObjectId);
				}
				//END:oep
      }

      return(excludeIds);
    }

	protected static String getStatement(String name, String value, String operator)
	{
		StringBuffer sbStatement = new StringBuffer();
		sbStatement.append(SYMB_OPEN_PARAN);
		sbStatement.append(name);
		sbStatement.append(operator);
		sbStatement.append(SYMB_QUOTE);
		sbStatement.append(value);
		sbStatement.append(SYMB_QUOTE);
		sbStatement.append(SYMB_CLOSE_PARAN);
		return sbStatement.toString();
	}

	/**
	 * Method used to calculate all the Specification on list page
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return StringList list of the all the requirement objects having parent Ids
	 * @throws Exception if operation fails
	 * @since X3
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public static Object getSpecifications(Context context, String[] args)
	throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

		String strType = (String) programMap.get("TypeSpecification");
		if (strType == null || strType.equals(""))
			strType = SYMB_WILD;

		String strName = (String) programMap.get("Name");
		if (strName == null || strName.equals(""))
			strName = SYMB_WILD;

		String strRevision = (String) programMap.get("Revision");
		if (strRevision == null || strRevision.equals(""))
			strRevision = SYMB_WILD;

		String strTitle = (String)programMap.get("Title");
		String strDesc = (String)programMap.get("Description");
		String strState = (String)programMap.get("State");
		if (strState != null)
			strState = strState.trim();
		else
			strState = "";

		String strPolicy = (String)programMap.get("Policy");
		String strOwner = (String)programMap.get("OwnerDisplay");
		if (strOwner == null || strOwner.equals(""))
			strOwner = SYMB_WILD;

		String strVault = null;
		String strVaultOption = (String)programMap.get("vaultOption");
		if (strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT) ||
		    strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS) ||
		    strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS)
		   )
			strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
		else
			strVault = (String)programMap.get("vaults");

		StringList select = new StringList(1);
		select.addElement(DomainConstants.SELECT_ID);

		boolean start = true;
		StringBuffer sbWhereExp = new StringBuffer(120);
		if ( strDesc != null && !strDesc.equals(SYMB_WILD) && !strDesc.equals("")) {
			if (start) {
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
			sbWhereExp.append(SYMB_MATCH);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(strDesc);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}
// Commented from Onsite coz this condition has a problem so the search results will not come up
/*		if (strPolicy!=null && (!strPolicy.equals(SYMB_WILD)) && (!strPolicy.equals("")) ) {
			if (start) {
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			} else {
				sbWhereExp.append(SYMB_AND);
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(DomainConstants.SELECT_POLICY);
			sbWhereExp.append(SYMB_MATCH);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(strPolicy);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}
*/
		// Praveen
		sbWhereExp.append(SYMB_OPEN_PARAN);
		sbWhereExp.append(DomainObject.SELECT_POLICY);
		sbWhereExp.append(SYMB_NOT_EQUAL);
		sbWhereExp.append(ReqSchemaUtil.getVersionPolicy(context));
		sbWhereExp.append(SYMB_CLOSE_PARAN);
		//Praveen

		if ( strTitle!=null && (!strTitle.equals(SYMB_WILD)) && (!strTitle.equals("")) ) {
			if (start) {
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			} else {
				sbWhereExp.append(SYMB_AND);
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(SYMB_ATTRIBUTE);
			sbWhereExp.append(SYMB_OPEN_BRACKET);
			sbWhereExp.append(ReqSchemaUtil.getTitleAttribute(context));
			sbWhereExp.append(SYMB_CLOSE_BRACKET);
			sbWhereExp.append(SYMB_MATCH);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(strTitle);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}

		if ( strState!=null && (!strState.equals(SYMB_WILD)) && (!strState.equals("")) ) {
			if (start) {
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			} else {
				sbWhereExp.append(SYMB_AND);
			}
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(DomainConstants.SELECT_CURRENT);
			sbWhereExp.append(SYMB_MATCH);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(strState);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}

		String strFilteredExpression = getFilteredExpression(context,programMap);

		if ( (strFilteredExpression != null) ) {
			if (start) {
				sbWhereExp.append(SYMB_OPEN_PARAN);
				start = false;
			} else {
				sbWhereExp.append(SYMB_AND);
			}
			sbWhereExp.append(strFilteredExpression);
		}

		if (!start) {
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}

		MapList mapList = null;
		mapList = DomainObject.findObjects(context, strType,strName, strRevision, strOwner, strVault, sbWhereExp.toString(), "", true, select, sQueryLimit);
		return mapList;
	}

    /**
     * Returns the search result of specifications excluding a specified spec.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args JPO arguments
     * @throws Exception if the operation fails
     * @since RequirementCentral X4
     * @return Object source specification Object
     */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public static Object getSourceSpecifications(Context context, String[] args)
	throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
    	String srcSpecId = (String)programMap.get("srcSpecId");

		MapList specs = (MapList)getRequirements(context, args);
		if(specs != null){
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

	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public static StringList getContextObjects(final Context context, final String[] args) throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		MapList ObjectList = null;
		MapList filteredObjectList = null;
		SelectList objSelects = new SelectList(1);
		SelectList relSelects = new SelectList(1);
	    objSelects.add(DomainConstants.SELECT_ID);
	    relSelects.add(DomainConstants.SELECT_FROM_ID);
		String contextObjectId = (String) programMap.get("objectId");
		String selectedProgram = (String) programMap.get("selectedProgram");
		//START: LX6 IR-350188-3DEXPERIENCER2016x : Search with in context  displays incorrect result. 
		String allowedOIDList = (String) programMap.get("allowedOIDs")==null?"":(String) programMap.get("allowedOIDs");
		//END: LX6 IR-350188-3DEXPERIENCER2016x : Search with in context  displays incorrect result.
		programMap.put("fullTextSearch", "false");
		programMap.put("expandLevel", "All");
		String[] functionName = null;
		if(selectedProgram!=null&&selectedProgram.length()>0){
			functionName = selectedProgram.split(":");
		}
		DomainObject Obj = new DomainObject(contextObjectId);
		if((selectedProgram == null) ||("".equals(selectedProgram)))
		{
		    //JX5 IR-332766-3DEXPERIENCER2015x
			String relationshipPattern = PropertyUtil.getSchemaProperty(context, ReqConstants.SYMBOLIC_RELATIONSHIP_SPECIFICATION_STRUCTURE) + "," +
					PropertyUtil.getSchemaProperty(context, ReqConstants.SYMBOLIC_RELATIONSHIP_SUB_REQUIREMENT);
			String typePattern = ReqSchemaUtil.getRequirementType(context)+","+ReqSchemaUtil.getRequirementSpecificationType(context)+","+ReqSchemaUtil.getChapterType(context);
		    ObjectList = Obj.getRelatedObjects(context,
		    		  relationshipPattern,				  // relationship pattern
		    		  typePattern,						  // object pattern
	                  false,                              // to direction
	                  true,                               // from direction
	                  0,								  // recursion level
	                  objSelects,                         // object selects
	                  relSelects,                         // relationship selects
	                  null,								  // object where clause
	                  null,								  // relationship where clause
	                  0,								  // No expand limit
	                  null,                        		  // postRelPattern
	                  null,								  // postTypePattern
	                  null);                              // postPatterns                           // relationship where clause
		}
		else
		{
			if((functionName.length != 2)||("".equals(functionName[0]))||("".equals(functionName[1])))
			{
				throw(new IllegalArgumentException("the class name or function name is not correct for the use of invoke"));
			}
			else
			{
				ObjectList = (MapList)JPO.invoke(context, functionName[0], null, functionName[1], args, MapList.class);
			}

		}

		//${CLASS:emxSpecificationStructureBase}.


	    Iterator ObjListIter = ObjectList.iterator();
	    StringList Ids = new StringList();
	    while(ObjListIter.hasNext())
	    {
	    	Map objInfo = (Map)ObjListIter.next();
	    	//START: LX6 IR-350188-3DEXPERIENCER2016x : Search with in context  displays incorrect result.
	    	String objId = (String)objInfo.get(DomainConstants.SELECT_ID);
	    	if(allowedOIDList.contains(objId)){
	    		Ids.addElement(objId);
	    	}	    	
	    	//END: LX6 IR-350188-3DEXPERIENCER2016x : Search with in context  displays incorrect result.
	    }
		return Ids;
	}

    /**
     * This method allow to search an object (Requirement, Chapter, Comment)
     * using the Inline Data Entry.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            JPO arguments
     * @return a list who contains the data to fill the rows. If a row contains
     *         an invalid data, an error is printed directly in the SB.
     * @throws Exception
     */
    public MapList lookupEntries(Context context, String[] args) throws Exception {

        HashMap inputMap = (HashMap) JPO.unpackArgs(args);
        MapList objectMap = (MapList) inputMap.get("objectList");
        HashMap<String, String> returnMap = new HashMap<String, String>(objectMap.size());
        Iterator objectItr = objectMap.iterator();

        MapList returnList = new MapList();
        while (objectItr.hasNext()) {

            // Get the information about the current row
            HashMap curObjectMap = (HashMap) objectItr.next();
            String objectName = (String) curObjectMap.get("Name");

            // Extract the name. Format : NAME [TYPE]
            // If we have several objects for a same name, the user can see
            // which he has to pick
            String strNameToSearch = "";
            try {
                strNameToSearch = objectName.split("\\[")[0].trim();
                objectName.split("\\[")[1].replace("]", "").trim();
            } catch (Exception e) {
                strNameToSearch = objectName;
            }

            // If the user specify a type
            String strTypeToSearch = "*";
            String strType = (String) curObjectMap.get("Type");
            if (!strType.isEmpty())
                strTypeToSearch = strType;

            SearchResult result = searchByTypeAndName(context, strTypeToSearch, strNameToSearch, 2, 2, false);
            SearchRecord records[] = result.getRecords();

            // If there are several objects corresponding to the parameters
            // wanted, if not we return the ID
            int resultCount = result.getTotalCount();
            if (resultCount > 1) {
                StringBuilder strBuilder = new StringBuilder();
                strBuilder.append("Found more than one match!\n");
                for (int i = 0; i < resultCount && i < NB_RESULT_PRINTED_TYPE_AHEAD; i++) {

                    DomainObject dmoRootReq = DomainObject.newInstance(context, records[i].getMXId());
                    StringList slRootReqSelect = new StringList();
                    slRootReqSelect.add(SELECT_NAME);
                    slRootReqSelect.add(SELECT_TYPE);

                    Map<?, ?> mapRootInfo = dmoRootReq.getInfo(context, slRootReqSelect);
                    String strName = (String) mapRootInfo.get(SELECT_NAME);
                    String strObjectType = (String) mapRootInfo.get(SELECT_TYPE);

                    strBuilder.append(strName);
                    strBuilder.append(" [");
                    strBuilder.append(strObjectType);
                    strBuilder.append("]\n");
                }
                returnMap.put("Error", strBuilder.toString());
            } else if (result.getTotalCount() == 0) {
                returnMap.put("Error", EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource",
                        context.getLocale(), "emxFramework.Common.NoMatchFound"));
            } else {
                returnMap.put("id", records[0].getMXId());
            }
            returnList.add(returnMap);
        }
        return returnList;
    }

    /**
     * Search one/several object(s) using name/type.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param typeToSearch
     *            the type of the object
     * @param nameToSearch
     *            the name of the object
     * @param findLimit
     *            the results limit
     * @param pageSize
     *            the size of the page
     * @param pageSize
     *            if false, we'll add '*' to the name, nothing otherwise
     * @return the result of the search
     * @throws Exception
     */
    protected SearchResult searchByTypeAndName(Context context, String typeToSearch, String nameToSearch,
            int findLimit, int pageSize, boolean absoluteSearch) throws Exception {

        if (absoluteSearch)
            nameToSearch += "*";

        if (typeToSearch == null)
            typeToSearch = "*";

        // Now we can search the corresponding object, we only need one
        CaseSensitiveMatrixSearch msearch = new CaseSensitiveMatrixSearch();
        msearch.setFindLimit(findLimit);
        msearch.setPageSize(pageSize);

        // Refinement, set the vault, name, type, ...
        AttributeRefinement vaultRefinement = new CaseSensitiveAttributeRefinement("VAULT",
                PersonUtil.getSearchDefaultVaults(context), AttributeRefinement.OPERATOR_EQUAL, true);
        TaxonomyRefinement typeRefinement = new TaxonomyRefinement(UISearchUtil.FIELD_TYPES, typeToSearch, true);
        AttributeRefinement nameRefinement = new CaseSensitiveAttributeRefinement("NAME", nameToSearch,
                AttributeRefinement.OPERATOR_EQUAL, true);
        String[] accessRefinement = PersonUtil.getMemberOrganizations(context);
        AccessRefinement accRefinement = new AccessRefinement(accessRefinement);

        // Set the refinement
        ArrayList<SearchRefinement> refinementList = new ArrayList<SearchRefinement>();
        refinementList.add(vaultRefinement);
        refinementList.add(typeRefinement);
        refinementList.add(nameRefinement);
        refinementList.add(accRefinement);

        // Search
        ContextUtil.startTransaction(context, true);
        SearchRefinement[] refinements = (SearchRefinement[]) refinementList
                .toArray(new SearchRefinement[refinementList.size()]);
        SearchResult result = msearch.search(context, refinements, 0);
        ContextUtil.commitTransaction(context);
        return result;
    }

    /**
     * Help the user to find an object using word completion.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            JPO arguments
     * @return the search results (XML format) only for the first 5.
     * @throws Exception
     */
    public String getRTTypeAhead(Context context, String[] args) throws Exception {

        try {
            // The arguments MUST be kept before the call of this method
            assert _inputDataTypeAhead != null;

            String inputStringToSearch = (String) JPO.unpackArgs(args);

            String[] args2 = JPO.packArgs(_inputDataTypeAhead);
            // We build the object to generate the XML
            emxTypeAhead_mxJPO typeAheadJPO = new emxTypeAhead_mxJPO(context, args2);

            // Search & Destroy : only three types are available here
            SearchResult result = searchByTypeAndName(context, "Requirement,Chapter,Comment", inputStringToSearch,
                    NB_RESULT_PRINTED_TYPE_AHEAD, NB_RESULT_PRINTED_TYPE_AHEAD, true);
            SearchRecord records[] = result.getRecords();
            int totalCount = result.getTotalCount();

            if (totalCount == 0) {
                String strNoMatch = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource",
                        context.getLocale(), "emxFramework.Common.NoMatchFound");
                typeAheadJPO.addValue(strNoMatch, strNoMatch);
            }

            for (int i = 0; i < totalCount; i++) {
                DomainObject dmoRootReq = DomainObject.newInstance(context, records[i].getMXId());

                StringList slRootReqSelect = new StringList();
                slRootReqSelect.add(SELECT_NAME);
                slRootReqSelect.add(SELECT_TYPE);

                Map<?, ?> mapRootInfo = dmoRootReq.getInfo(context, slRootReqSelect);
                String strName = (String) mapRootInfo.get(SELECT_NAME);
                String strObjectType = (String) mapRootInfo.get(SELECT_TYPE);

                typeAheadJPO.addValue(strName + " [" + strObjectType + "]", strName);
            }
            return typeAheadJPO.toXML();
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Get the default value to show corresponding to the different fields.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            JPO arguments
     * @return the default value to show.
     * @throws Exception
     */
    public HashMap<String, String> getDefaultDataRMTTable(Context context, String[] args) throws Exception {
        HashMap programMap = JPO.unpackArgs(args);
        HashMap columnMap = (HashMap) programMap.get("columnMap");
        HashMap requestMap = (HashMap) programMap.get("requestMap");

        HashMap<String, String> defaultMap = new HashMap<String, String>();
        String columnName = (String) columnMap.get("name");

        // Owner
        if ("Owner".equals(columnName)) {
            defaultMap.put("Default_ExistingRow", PersonUtil.getFullName(context));
            defaultMap.put("Default_AddNewRow", PersonUtil.getFullName(context));
        }

        return defaultMap;
    }

    /**
     * Updates the type values depending of the current type.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            the JPO arguments
     * @return the types
     * @throws Exception
     */
    public Map<String, String> updateValuesRMTTableType(Context context, String[] args) throws Exception {
        String parentID = JPO.unpackArgs(args);
        LinkedHashMap<String, String> valuesMap = new LinkedHashMap<String, String>();
        String licenseToCheck = "ENO_RMF_TP";

        try {
            // License check
            ComponentsUtil.checkLicenseReserved (context, licenseToCheck);
        } catch (Exception ex) {
            String licenseError = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Login.LicenseError") + licenseToCheck;
            valuesMap.put("ERR", licenseError);
            return valuesMap;
        }

        try {
            DomainObject dmoParent = DomainObject.newInstance(context, parentID);
            if (dmoParent.isKindOf(context, ReqSchemaUtil.getRequirementSpecificationType(context))) {
                valuesMap.put(ReqSchemaUtil.getRequirementType(context), ReqSchemaUtil.getRequirementType(context));
                valuesMap.put(ReqSchemaUtil.getChapterType(context), ReqSchemaUtil.getChapterType(context));
                valuesMap.put(ReqSchemaUtil.getCommentType(context), ReqSchemaUtil.getCommentType(context));
            }
            else if (RequirementsUtil.isRequirement(context, parentID)) {
                valuesMap.put(ReqSchemaUtil.getSubRequirementRelationship(context), ReqSchemaUtil.getSubRequirementRelationship(context));
                valuesMap.put(ReqSchemaUtil.getDerivedRequirementRelationship(context), ReqSchemaUtil.getDerivedRequirementRelationship(context));
            }
            else if (dmoParent.isKindOf(context, ReqSchemaUtil.getChapterType(context))) {
                valuesMap.put(ReqSchemaUtil.getRequirementType(context), ReqSchemaUtil.getRequirementType(context));
                valuesMap.put(ReqSchemaUtil.getChapterType(context), ReqSchemaUtil.getChapterType(context));
                valuesMap.put(ReqSchemaUtil.getCommentType(context), ReqSchemaUtil.getCommentType(context));
            }
            else if (dmoParent.isKindOf(context, ReqSchemaUtil.getCommentType(context))) {
                // No operation for a comment
                valuesMap.put("NOP", "NOP");
            }
        } catch (Exception e) {
            valuesMap.clear();
            valuesMap.put("NOP", "NOP");
        }
        return valuesMap;
    }

    /**
     * Constructs an autoName depending of the current type.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            the type
     * @return the autoName
     */
    public String getAutoNameValueRMT(Context context, String[] args) {
        String strName = "";
        try {
            String strType = JPO.unpackArgs(args);
            strName = UnifiedAutonamingServices.autoname(context, strType);
        } catch (Exception e) {
            // NOP
        }
        return strName;
    }
}//End of the class
