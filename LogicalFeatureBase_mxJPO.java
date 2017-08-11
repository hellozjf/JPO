/*
 ** ${CLASSNAME}
 **
 **Copyright (c) 1993-2016 Dassault Systemes.
 ** All Rights Reserved.getActiveManufacturingPlans
 **This program contains proprietary and trade secret information of
 **Dassault Systemes.
 **Copyright notice is precautionary only and does not evidence any actual
 **or intended publication of such program
 */

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Access;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.db.RelationshipType;
import matrix.db.SelectConstants;
import matrix.util.MatrixException;
import matrix.util.StringList;
import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.ConfigurationFeature;
import com.matrixone.apps.configuration.ConfigurationUtil;
import com.matrixone.apps.configuration.LogicalFeature;
import com.matrixone.apps.configuration.Model;
import com.matrixone.apps.configuration.Part;
import com.matrixone.apps.configuration.ProductConfiguration;
import com.matrixone.apps.configuration.ProductVariant;
import com.matrixone.apps.configuration.RuleProcess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.Job;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.effectivity.EffectivityFramework;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.input.SAXBuilder;
import com.matrixone.json.JSONObject;
import com.matrixone.apps.framework.ui.UITableIndented;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * This JPO class has some methods pertaining to Logical Feature.
 *
 * @author IVU
 * @since FTR R211
 */
public class LogicalFeatureBase_mxJPO extends emxDomainObject_mxJPO {

	public static final String SYMB_NOT_EQUAL = " != ";
    /** A string constant with the value "'". */
    public static final String SYMB_QUOTE = "'";
    public static final String strSymbReleaseState = "state_Release";
    public static final String strSymbObsoleteState = "state_Obsolete";
    public static final String SUITE_KEY = "Configuration";
    public static final String KEYWORD_PHYSID_PREFIX = "PHY@EF:";
	/**
	 * map will hold IR id and its type (Simple/Complex)
	 */
	private Map ruleType = new HashMap();
	List relIdList = new StringList();

    protected static final String FIELD_DISPLAY_CHOICES = "field_display_choices";

    /** A string constant with the value field_choices. */
    protected static final String FIELD_CHOICES = "field_choices";


	/**
	 * Default Constructor.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds no arguments
	 * @return noting,constructor
	 * @throws Exception
	 *             if the operation fails
	 */
	public LogicalFeatureBase_mxJPO(Context context, String[] args)
	throws Exception {
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
	 */
	public int mxMain(Context context, String[] args) throws Exception {
		if (!context.isConnected())
			throw new Exception("Not supported on desktop client");
		return 0;
	}


	/**
	 * This Methods is used to get the Logical Feature Structure.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the Logical Feature Structure Details
	 * @throws Exception
	 *             if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getLogicalFeatureStructure(Context context, String[] args)
	throws Exception {

		MapList mapLogicalStructure =null;
		int limit = 32767; //Integer.parseInt(FrameworkProperties.getProperty(context,"emxConfiguration.Search.QueryLimit"));
		String strObjWhere = DomainObject.EMPTY_STRING;
		StringBuffer strRelWhere = new StringBuffer(DomainObject.EMPTY_STRING);

		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			//Gets the objectids and the relation names from args
			String strObjectId = (String)programMap.get("objectId");
			String strparentOID = (String)programMap.get("parentOID");
			String strIsStructureCompare = (String) programMap.get("IsStructureCompare");

			String strObjectid = "";
			StringTokenizer objIDs = new StringTokenizer(strObjectId, ",");
			if (objIDs.countTokens() > 1) {
				// Context Feature ID
				strObjectid = objIDs.nextToken().trim();
				// Context Product ID
				strparentOID = objIDs.nextToken().trim();
			}else{
				strObjectid = (String)programMap.get("objectId");
				 strparentOID = (String)programMap.get("parentOID");
			}

			if(strIsStructureCompare!=null && strIsStructureCompare.equalsIgnoreCase("true")){
				strObjectid = (String)programMap.get("objectId");
			    strparentOID = strObjectid;
			}

			DomainObject domObject = new DomainObject(strObjectid);
			String strType = domObject.getInfo(context, DomainObject.SELECT_TYPE);
			//String strparentOID = (String)programMap.get("parentOID");
			String sNameFilterValue = (String) programMap.get("FTRLogicalFeatureNameFilterCommand");
			String sLimitFilterValue = (String) programMap.get("FTRLogicalFeatureLimitFilterCommand");
			
			String strContextProductId = "";
			if(programMap.containsKey("productID")){
				strContextProductId = (String)programMap.get("productID");
			}else if(programMap.containsKey("ProductID")){
				strContextProductId = (String)programMap.get("ProductID");
			}


			if (strparentOID!=null && !strparentOID.equals(strObjectid) && mxType.isOfParentType(context, strType,ConfigurationConstants.TYPE_PRODUCTS)) {
				return new MapList();
			}
			else{
				boolean isCalledFromRule=false;
			  	if(sNameFilterValue==null){
			  		sNameFilterValue = (String) programMap.get("FTRLogicalFeatureNameFilterForRuleDialog");
			  		if(sNameFilterValue!=null) isCalledFromRule= true;
			  	}
	            if(sLimitFilterValue==null)
	            	sLimitFilterValue = (String) programMap.get("FTRLogicalFeatureLimitFilterForRuleDialog");
				// emxConfiguration.Search.QueryLimit
				if (sLimitFilterValue != null
						&& !(sLimitFilterValue.equalsIgnoreCase("*"))) {
					if (sLimitFilterValue.length() > 0) {
						limit = (short) Integer.parseInt(sLimitFilterValue);
						if (limit < 0) {
							limit = 32767;
						}
					}
				}
				StringBuffer strObjWherebuffer = new StringBuffer();
				if (sNameFilterValue != null
						&& !(sNameFilterValue.equalsIgnoreCase("*"))) {

					strObjWherebuffer.append("attribute[");
					strObjWherebuffer.append(ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME);
					strObjWherebuffer.append("] ~~ '");
					strObjWherebuffer.append(sNameFilterValue);
					strObjWherebuffer.append("'");
				}
				strObjWhere = strObjWherebuffer.toString();
				//if this is called from Rule, then add object where, to prevent invalid state object being seen in Rule context Tree
				if(isCalledFromRule){
					if(!strObjWhere.trim().isEmpty())
						strObjWhere=strObjWhere+" && "+RuleProcess.getObjectWhereForRuleTreeExpand(context,ConfigurationConstants.TYPE_LOGICAL_FEATURE);
					else
						strObjWhere=RuleProcess.getObjectWhereForRuleTreeExpand(context,ConfigurationConstants.TYPE_LOGICAL_FEATURE);
				}

				if(!ProductLineCommon.isNotNull(strparentOID)){
					strparentOID = strObjectid;
				}

				strRelWhere.append("from.type.kindof !=\"");
				strRelWhere.append(ConfigurationConstants.TYPE_PRODUCTS);
				strRelWhere.append("\" || from.id ==\"");
				strRelWhere.append(strparentOID);
				strRelWhere.append("\"");

				// call method to get the level details
				int iLevel = ConfigurationUtil.getLevelfromSB(context,args);
				String filterExpression = (String) programMap.get("CFFExpressionFilterInput_OID");

				// @To DO
				// need to revisit the selectables
				LogicalFeature cfBean = new LogicalFeature(strObjectid);

				StringList relSelect = new StringList();
				relSelect.add("tomid["+
						ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].from.id");
				relSelect.add("tomid["+ ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].attribute["+
						ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
				relSelect.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+ "].from.id");
				relSelect.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+ "].from.type");
				relSelect.add("tomid["
						+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
						+ "].from.attribute["
						+ ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION
						+ "]");


				relSelect.add("tomid["
						+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
						+ "].from.attribute["
						+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION
						+ "]");
				relSelect.addElement(SELECT_FROM_NAME);

				StringList objSelectables = new StringList();
				objSelectables.addElement("attribute["
						+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME
						+ "]");
				objSelectables.addElement(DomainConstants.SELECT_REVISION);
				objSelectables.addElement("attribute["
						+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME
						+ "]");
				objSelectables.addElement("physicalid");
				objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_MANAGED_REVISION+ "]."+DomainObject.SELECT_FROM_NAME);
				objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+ "]."+DomainObject.SELECT_FROM_NAME);
				objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_MANAGED_REVISION+ "]."+DomainObject.SELECT_FROM_ID);
				objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+ "]."+DomainObject.SELECT_FROM_ID);

				mapLogicalStructure = (MapList)cfBean.getLogicalFeatureStructure(context,"", null, objSelectables, relSelect, false,
						true,iLevel,limit, strObjWhere, strRelWhere.toString(), DomainObject.FILTER_STR_AND_ITEM,filterExpression);

				for (int i = 0; i < mapLogicalStructure.size(); i++) {
					Map tempMAp = (Map) mapLogicalStructure.get(i);
					if(tempMAp.containsKey("expandMultiLevelsJPO")){
						mapLogicalStructure.remove(i);
						i--;
					}
					if(tempMAp.containsKey("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
		    				+ "].from.attribute["
		    				+ ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION + "]")){

						tempMAp.put("attribute["+ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION+"]", tempMAp.get("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
			    				+ "].from.attribute["
			    				+ ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION + "]"));
					}

					if(tempMAp.containsKey("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
		    				+ "].from.attribute["
		    				+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION + "]")){

						tempMAp.put("attribute["+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]",tempMAp.get("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
			    				+ "].from.attribute["
			    				+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION + "]") );
					}
				}

				// fetching EBOM related data
				if (mxType.isOfParentType(context, strType,ConfigurationConstants.TYPE_PART)) {
					MapList partRelatedObjects = (MapList) LogicalFeature.getEBOMsWithRelSelectables(context, args);
					HashMap hmTemp = new HashMap();
					hmTemp.put("expandMultiLevelsJPO", "true");
					partRelatedObjects.add(hmTemp);
					return partRelatedObjects;
				}

				for(Iterator iterator = mapLogicalStructure.iterator(); iterator.hasNext();){
					Map map = (Map) iterator.next();
					map.put("ContextProductId", strContextProductId);
				}

				if (mapLogicalStructure != null) {
					HashMap hmTemp = new HashMap();
					hmTemp.put("expandMultiLevelsJPO", "true");
					mapLogicalStructure.add(hmTemp);
				}


			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return mapLogicalStructure;
	}

	/**
	 * This Methods is used to get the  top level Logical Features.
	 * This is used while launching the Logical Features from My Desk
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the id of Top Level Logical Features
	 * @throws Exception
	 *             if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTopLevelLogicalFeatures(Context context, String[] args)
	throws Exception {

		MapList mapTop = null;
		int limit = 32767; //Integer.parseInt(FrameworkProperties.getProperty(context,"emxConfiguration.Search.QueryLimit"));
		String queryName = DomainObject.EMPTY_STRING;

		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String sNameFilterValue = (String) programMap.get("FTRLogicalFeatureNameFilterCommand");
			String sLimitFilterValue = (String) programMap.get("FTRLogicalFeatureLimitFilterCommand");

			// emxConfiguration.Search.QueryLimit
			if (sLimitFilterValue != null
					&& !(sLimitFilterValue.equalsIgnoreCase("*"))) {
				if (sLimitFilterValue.length() > 0) {
					limit = (short) Integer.parseInt(sLimitFilterValue);
					if (limit < 0) {
						limit = 32767;
					}
				}
			}
			
			StringBuffer strWhere = new StringBuffer();
			strWhere.append("(to["+ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+"]=='False'");
			strWhere.append(" || ");
			strWhere.append("to["+ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+"].from.type.kindof["+
					ConfigurationConstants.TYPE_LOGICAL_STRUCTURES+"]!=TRUE)");

			if (sNameFilterValue != null
					&& !(sNameFilterValue.equalsIgnoreCase("*"))) {
				strWhere.append(" && ");
				strWhere.append("attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME+ "] ~~ '"+sNameFilterValue+ "'");
			}

			mapTop = DomainObject.findObjects(context,
					ConfigurationConstants.TYPE_LOGICAL_FEATURE,"*", "*","*","*",
					strWhere.toString(),queryName,true,ConfigurationUtil.getBasicObjectSelects(context), (short)limit);

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return mapTop;
	}
	/**
	 * This Methods is used to get the  top level Logical Features that are owned by the user.
	 * This is used while launching the Logical Features from My Desk
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the id of Top Level Logical Features
	 * @throws Exception
	 *             if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTopLevelOwnedLogicalFeatures(Context context, String[] args)
	throws Exception {

		MapList mapTop = null;
		int limit = 32767; //Integer.parseInt(FrameworkProperties.getProperty(context,"emxConfiguration.Search.QueryLimit"));
		String queryName = DomainObject.EMPTY_STRING;

		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String sNameFilterValue = (String) programMap.get("FTRLogicalFeatureNameFilterCommand");
			String sLimitFilterValue = (String) programMap.get("FTRLogicalFeatureLimitFilterCommand");

			// emxConfiguration.Search.QueryLimit
			if (sLimitFilterValue != null
					&& !(sLimitFilterValue.equalsIgnoreCase("*"))) {
				if (sLimitFilterValue.length() > 0) {
					limit = (short) Integer.parseInt(sLimitFilterValue);
					if (limit < 0) {
						limit = 32767;
					}
				}
			}

			StringBuffer strWhere = new StringBuffer();
			strWhere.append("(to["+ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+"]=='False'");
			strWhere.append(" || ");
			strWhere.append("to["+ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+"].from.type.kindof["+
					ConfigurationConstants.TYPE_LOGICAL_STRUCTURES+"]!=TRUE)");

			if (sNameFilterValue != null
					&& !(sNameFilterValue.equalsIgnoreCase("*"))) {
				strWhere.append(" && ");
				strWhere.append("attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME+ "] ~~ '"+sNameFilterValue+ "'");
			}

			mapTop = DomainObject.findObjects(context,
					ConfigurationConstants.TYPE_LOGICAL_FEATURE,"*", "*",context.getUser(),"*",
					strWhere.toString(),queryName,true,ConfigurationUtil.getBasicObjectSelects(context), (short)limit);

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return mapTop;
	}

	/**
	 * This is wrapper method to load the Candidate Logical Features of the Context Model
	 *
	 * @param context -  the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the Map containing the Context Model Id.
	 * @return mapLogicalStructure- MapList Containing the Maps of Logical Features. Each Map will consist of:
	 * 					 id= Logical Feature ID,
	 * 					 type = Logical Feature or Sub-types of Logical Features
	 * 					 name = name of logical feature
	 * 					 revision = revision of  logical feature
	 * 					 name[connection]= name of the relationship
	 * 					 id[connection] = relationship id
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getCandidateLogicalFeatures(Context context, String[] args)
	throws Exception {

		MapList mapLogicalStructure =null;
		StringList objSelectables = new StringList("physicalid");
		objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_MANAGED_REVISION+ "]."+DomainObject.SELECT_FROM_NAME);
		objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+ "]."+DomainObject.SELECT_FROM_NAME);
		objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_MANAGED_REVISION+ "]."+DomainObject.SELECT_FROM_ID);
		objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+ "]."+DomainObject.SELECT_FROM_ID);

		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);

			String strObjectid = (String)programMap.get("objectId");
			String parentOID = (String)programMap.get("parentOID");
			if(strObjectid.equals(parentOID)){
				// call method to get the level details
				int iLevel = ConfigurationUtil.getLevelfromSB(context,args);
				String filterExpression = (String) programMap.get("CFFExpressionFilterInput_OID");

				Model configModel = new Model(strObjectid);
				mapLogicalStructure = configModel.getCandidateLogicalFeatures(context, DomainObject.EMPTY_STRING,
						DomainObject.EMPTY_STRING, objSelectables, null, false,	true, iLevel, 0, DomainObject.EMPTY_STRING,
						DomainObject.EMPTY_STRING, DomainObject.FILTER_STR_AND_ITEM, filterExpression);
			}
			else{
				mapLogicalStructure = getLogicalFeatureStructure(context, args);
			}

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return mapLogicalStructure;
	}


	/**
	 * This is wrapper method to load the Committed Logical Features of the Context Model
	 *
	 * @param context -  the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the Map containing the Context Model Id.
	 * @return mapLogicalStructure- MapList Containing the Maps of Logical Features. Each Map will consist of:
	 * 					 id= Logical Feature ID,
	 * 					 type = Logical Feature or Sub-types of Logical Features
	 * 					 name = name of logical feature
	 * 					 revision = revision of  logical feature
	 * 					 name[connection]= name of the relationship
	 * 					 id[connection] = relationship id
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getCommittedLogicalFeatures(Context context, String[] args)throws Exception {

		MapList mapLogicalStructure =null;
		StringList objSelectables = new StringList("physicalid");
		objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_MANAGED_REVISION+ "]."+DomainObject.SELECT_FROM_NAME);
		objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+ "]."+DomainObject.SELECT_FROM_NAME);
		objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_MANAGED_REVISION+ "]."+DomainObject.SELECT_FROM_ID);
		objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+ "]."+DomainObject.SELECT_FROM_ID);
		StringList slRelSelects = new StringList("frommid["+ConfigurationConstants.RELATIONSHIP_COMMITTED_CONTEXT+"].to.id");

		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strObjectid = (String)programMap.get("objectId");

			// call method to get the level details
			int iLevel = ConfigurationUtil.getLevelfromSB(context,args);
			String filterExpression = (String) programMap.get("CFFExpressionFilterInput_OID");

			Model configModel = new Model(strObjectid);
			mapLogicalStructure = configModel.getCommittedLogicalFeatures(context, DomainObject.EMPTY_STRING,
					DomainObject.EMPTY_STRING, objSelectables, slRelSelects, false,	true, iLevel, 0, DomainObject.EMPTY_STRING,
					DomainObject.EMPTY_STRING, DomainObject.FILTER_STR_AND_ITEM, filterExpression);

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return mapLogicalStructure;
	}

	/**
	 * This Method is called to connect the Part Family to Logical Feature, in the Create Page.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @throws Exception
	 *             if the operation fails
	 */
	public void setPartFamily(Context context, String[] args) throws Exception{
		try {
			//unpacking the Arguments from variable args
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap   = (HashMap)programMap.get("paramMap");
			String strObjId = (String)paramMap.get("objectId");
			String strNewOId = (String) paramMap.get("New OID");
			if(strNewOId.equals(DomainConstants.EMPTY_STRING)){
				strNewOId = (String) paramMap.get("New Value");
			}
			if(!strNewOId.equals(DomainConstants.EMPTY_STRING)){
				DomainObject dom = new DomainObject(strObjId);
				DomainRelationship.connect(context, dom, ConfigurationConstants.RELATIONSHIP_GBOM, new DomainObject(strNewOId));
			}
		}catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
	}

	/**
	 * This is a trigger method to add the interface on Business Object.
	 *
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args - Holds the following arguments 0 - Object ID, 1 - Interface Names to be added
	 * @return iReturn - 0 - if operation is successful
	 * 					1 - if operation fails.
	 * @throws Exception
	 */
	public int addInterfaceToBus(Context context, String args[])
	throws Exception {

		int iReturn = 0;
		try {
			iReturn = addInterface(context, args, "bus");
		} catch (Exception e) {
			iReturn = 1;
			throw new FrameworkException(e.getMessage());
		}
		return iReturn;
	}

	/**
	 * This is a trigger method to add interface on relationship
	 *
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args - Holds the following arguments 0 - connectionId, 1 - Interface Names to be added
	 * @return iReturn - 0 - if operation is successful
	 * 					1 - if operation fails.
	 * @throws Exception
	 */
	public int addInterfaceToRel(Context context, String args[])
	throws Exception {
		int iReturn = 0;
		try {
			iReturn = addInterface(context, args, "connection");
		} catch (Exception e) {
			iReturn = 1;
			throw new FrameworkException(e.getMessage());
		}
		return iReturn;
	}


	/**
	 * This is a private method called from trigger methods to add interfaces either on BusinessObject or
	 * Relationship.
	 *
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args - Holds the following arguments 0 - Object ID, 1 - Interface Names to be added
	 * @param relBus - argument to specify to what the interfaces need to be applied:
	 * 				  possible values: connection - if interface need to be added on relationship
	 * 								   bus - if interface need to be added on business object
	 * @return iReturn - 0 - if operation is successful
	 * 					1 - if operation fails.
	 * @throws Exception
	 */

	private int addInterface(Context context, String args[], String relBus)
	throws Exception {

		int iReturn = 0;
		try {

			String objectId = args[0];
			String strInterfaceName = args[1];
			//StringBuffer strBAddInterface = new StringBuffer();
			//StringTokenizer strTknInterfaces = new StringTokenizer(strInterfaceName, ",");
			StringList slInterfaces =new StringList();
			slInterfaces = FrameworkUtil.split(strInterfaceName, ",");
			StringBuffer strBAddInterface2 = new StringBuffer();
			for(int i=0;i<slInterfaces.size();i++){
				strBAddInterface2.append(" add interface ");
				strBAddInterface2.append("$");
				strBAddInterface2.append(i+3);
				strBAddInterface2.append(" ");
			}
			
			slInterfaces.add(0, relBus);
			slInterfaces.add(1, objectId);

			StringBuffer strBQuery = new StringBuffer();
			strBQuery.append("modify ");
			strBQuery.append("$1");
			strBQuery.append(" ");
			strBQuery.append("$2");
			strBQuery.append(" ");
			strBQuery.append(strBAddInterface2.toString());

			MqlUtil.mqlCommand(context,strBQuery.toString(),slInterfaces);

		} catch (Exception e) {
			iReturn = 1;
			throw new FrameworkException(e.getMessage());
		}
		return iReturn;
	}

	/**
	 * Method to search features need to exclude from emxFullSearchPage
	 *
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args -
	 * @return featureList - String list of Object IDs to exclude
	 * @throws Exception
	 */

	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeAvailableLogicalFeature(Context context, String [] args)
	throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		String strSourceObjectId = (String) programMap.get("objectId");

		String strObjectPattern = ConfigurationConstants.TYPE_LOGICAL_FEATURE+","+ConfigurationConstants.TYPE_HARDWARE_PRODUCT+","+ConfigurationConstants.TYPE_SOFTWARE_PRODUCT+","+ConfigurationConstants.TYPE_SERVICE_PRODUCT;
		String strRelPattern = ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES;
		StringList objectSelects = new StringList(DomainConstants.SELECT_ID);

		MapList relatedFromFeatureList = new MapList();

		StringList sRelSelects = new StringList();
		sRelSelects.addElement(DomainRelationship.SELECT_NAME);
		sRelSelects.addElement(DomainRelationship.SELECT_ID);

		ConfigurationUtil util = new ConfigurationUtil(strSourceObjectId);
		relatedFromFeatureList = util.getObjectStructure(context, strObjectPattern, strRelPattern,
				objectSelects, sRelSelects, false, true, 1, 0, DomainConstants.EMPTY_STRING,
				   DomainConstants.EMPTY_STRING, (short)0, DomainConstants.EMPTY_STRING);

		HashSet featureList = new HashSet();
		//add the context Feature
		featureList.add(strSourceObjectId);

		for(int i=0;i<relatedFromFeatureList.size();i++)
		{
			Map mapFeatureObj = (Map) relatedFromFeatureList.get(i);
			if(mapFeatureObj.containsKey(objectSelects.get(0)))
			{
				Object idsObject = mapFeatureObj.get(objectSelects.get(0));
				if(idsObject.getClass().toString().contains("StringList"))
				{
					featureList.addAll((StringList)idsObject);
				}
				else
				{
					featureList.add((String)idsObject);
				}
			}
		}

		ArrayList featureListArray = new ArrayList(featureList);

		return new StringList(featureListArray);
	}

	/**
	 * Method to search candidate features need to exclude from emxFullSearchPage
	 *
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args -
	 * @return featureList - String list of Object IDs to exclude
	 * @throws Exception
	 */

	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeAvailableCandidateLogicalFeature(Context context, String [] args)
	throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		String strSourceObjectId = (String) programMap.get("objectId");
		String strObjectPattern = ConfigurationConstants.TYPE_LOGICAL_FEATURE+","+ConfigurationConstants.TYPE_HARDWARE_PRODUCT+","+ConfigurationConstants.TYPE_SOFTWARE_PRODUCT+","+ConfigurationConstants.TYPE_SERVICE_PRODUCT;
		//along with available candidate features committed features also needs to be excluded
		//IR-211892V6R2014 fix
		String strRelPattern = ConfigurationConstants.RELATIONSHIP_CANDIDTAE_LOGICAL_FEATURES+","+ConfigurationConstants.RELATIONSHIP_COMMITTED_LOGICAL_FEATURES;
		StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
		MapList relatedFromFeatureList = new MapList();
		
		StringList tempStringList = new StringList();
		ConfigurationUtil util = new ConfigurationUtil(strSourceObjectId);
		//Replaced DomainConstants.EMPTY_STRINGLIST with tempStringList for stale object issue
		relatedFromFeatureList = util.getObjectStructure(context, strObjectPattern, strRelPattern,
				objectSelects, tempStringList, true, true, 0, 0, DomainConstants.EMPTY_STRING,
				   DomainConstants.EMPTY_STRING, (short)0, DomainConstants.EMPTY_STRING);

		HashSet featureList = new HashSet();
		//add the context Feature
		featureList.add(strSourceObjectId);

		for(int i=0;i<relatedFromFeatureList.size();i++)
		{
			Map mapFeatureObj = (Map) relatedFromFeatureList.get(i);
			if(mapFeatureObj.containsKey(objectSelects.get(0)))
			{
				Object idsObject = mapFeatureObj.get(objectSelects.get(0));
				if(idsObject.getClass().toString().contains("StringList"))
				{
					featureList.addAll((StringList)idsObject);
				}
				else
				{
					featureList.add((String)idsObject);
				}
			}
		}

		ArrayList featureListArray = new ArrayList(featureList);

		return new StringList(featureListArray);
	}

	/**
	 * This will be used to get the active design variant connected with the
	 * logical feature.
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public MapList getActiveDesignVariants(Context context, String[] args)
	throws Exception {
		MapList objectList = new MapList();
		try {
			Map programMap = (HashMap) JPO.unpackArgs(args);
			String logicalFeatureID = "";
			// getting the context feature Id
			String strObjectID = (String) programMap.get("objectId");
			// TODO- Do we require this if
			StringList slobjIDs=FrameworkUtil.split(strObjectID, ",");
			if (slobjIDs.size() > 1) {
				// Context Feature ID
				logicalFeatureID = slobjIDs.get(0).toString().trim();
			} else
				logicalFeatureID = strObjectID;

			LogicalFeature compFtr = new LogicalFeature(logicalFeatureID);
			// TODO- Check if we need to check if parentOID is equal to objctiId
			// OR
			// need to check if it of type Logical Feature

			String relWhere = DomainObject.EMPTY_STRING;
			String objWhere = DomainObject.EMPTY_STRING;
			// Obj and Rel pattern
			String typePattern = DomainObject.EMPTY_STRING;
			String relPattern = DomainObject.EMPTY_STRING;
			// Obj and Rel Selects
			// configuration Feature's Marketing name
			StringList objSelects = new StringList();
			objSelects.add("attribute["
					+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME + "]");
			objSelects.add("physicalid");

			StringList relSelects = new StringList();
			int iLevel = 1;
			String filterExpression = (String) programMap
			.get("CFFExpressionFilterInput_OID");

			// retrieve Active DV list
			objectList = compFtr.getActiveDesignVariants(context, typePattern,
					relPattern, objSelects, relSelects, false, true, iLevel, 0,
					objWhere, relWhere, DomainObject.FILTER_STR_AND_ITEM,
					filterExpression);
		} catch (FrameworkException e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
		return objectList;
	}

	/**
	 * This is dynamic column JPO method for the Design Variants in GBOM part
	 * Table
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public MapList getDesignVariantColumnsForFO(Context context, String[] args)
	throws Exception {
		// This will be MapList of the parts connected with GBOM relationship to
		// the logical Feature
		MapList returnList = new MapList();

		try {
			Map programMap = (HashMap) JPO.unpackArgs(args);
			Map requestMap = (HashMap) programMap.get("requestMap");
			String strContextProdId = (String) requestMap.get("prodId");
			int ruleDisplay=RuleProcess.getRuleDisplaySetting(context);
			//-----------------------------------------------------------
			// TODO Start Revisit for the Merge replace related code
			//-----------------------------------------------------------
			int noOfTargetFeatures = 0;
			MapList newDesignVariantList = new MapList();
			MapList allDVListInSM = new MapList();

			if (requestMap.get("noOfTargetFeatures") != null) {
				noOfTargetFeatures = Integer.parseInt((String) requestMap
						.get("noOfTargetFeatures"));
			}
			if (noOfTargetFeatures > 0) {
				String featureId = "";
				HashMap featureMap = null;
				String[] arg = null;
				MapList designVariantList = null;
				Map tempMap = null;
				for (int j = 0; j < noOfTargetFeatures; j++) {
					// In case of Merge and Replace get ids for Target Features
					featureId = (String) requestMap.get("objectIDs" + j);
					featureMap = new HashMap();
					featureMap.put("objectId", featureId);
					featureMap.put("parentOID", featureId);

					arg = JPO.packArgs(featureMap);
					designVariantList = getActiveDesignVariants(context, arg);

					for (int k = 0; k < designVariantList.size(); k++) {
						tempMap = (Map) designVariantList.get(k);
						tempMap.put("FeatureId", featureId);
					}
					newDesignVariantList.addAll(designVariantList);
					allDVListInSM.addAll(designVariantList);
				}
			}
			//-----------------------------------------------------------
			// TODO END Revisit for the Merge replace related code
			//-----------------------------------------------------------

			// use the Logical Feature and retrieve active design variants
			String[] arg = JPO.packArgs(requestMap);
			String logicalFeatureId = (String) requestMap.get("objectId");
			String strRelationshipId = (String) requestMap.get("relationshipId");
			MapList designVariantList = getActiveDesignVariants(context, arg);
			allDVListInSM.addAll(designVariantList);
			//-----------------------------------------------------------------------------------
			// TODO- Need to revisit--In case of Merge and Replace add column maps
			// for New Design Variants
			//-----------------------------------------------------------------------------------
			boolean remove = false;
			Map newDesignVariant = null;
			String newDesignVariantId = "";
			Map designVariant = null;
			String designVariantId = "";
			for (int k = newDesignVariantList.size(); k > 0; k--) {
				remove = false;
				newDesignVariant = (Map) newDesignVariantList.get(k - 1);
				newDesignVariantId = (String) newDesignVariant
				.get(DomainConstants.SELECT_ID);

				// Remove the Design Variants from New Design Variants List which
				// are connected to Master Feature
				for (int m = 0; m < designVariantList.size(); m++) {
					designVariant = (Map) designVariantList.get(m);
					designVariantId = (String) designVariant
					.get(DomainConstants.SELECT_ID);
					if (newDesignVariantId.equals(designVariantId))
						remove = true;
				}
				if (remove)
					newDesignVariantList.remove(newDesignVariant);
			}
			//-----------------------------------------------------------------------------------
			// TODO- END Need to revisit--In case of Merge and Replace add column maps
			// for New Design Variants
			//-----------------------------------------------------------------------------------
			// Defining column Maps for the design variant
			Map setting = null;
			Map column = null;
			String designVariantID = "";
			String strVBId = "";
			DomainObject dVariant = null;
			String dvRev = "";
			String displayName = "";

			// iterate on all the Active DVs
			for (int i = 0; i < designVariantList.size(); i++) {
				setting = new HashMap();
				column = new HashMap();
				designVariant = (Map) designVariantList.get(i);
				// get DV id name rev (configuration Feature) and varies by rel id
				designVariantID = designVariant.get(DomainConstants.SELECT_ID)
				.toString();
				dvRev = designVariant.get(DomainConstants.SELECT_REVISION)
				.toString();
				strVBId = designVariant.get(DomainConstants.SELECT_RELATIONSHIP_ID)
				.toString();
				// marketing name of DV
				String displayNameAttr = designVariant.get(
						"attribute["
						+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME
						+ "]").toString();
				String name = designVariant.get(DomainObject.SELECT_NAME).toString();
				String type = designVariant.get(DomainObject.SELECT_TYPE).toString();

                if(ruleDisplay==RuleProcess.RULE_DISPLAY_FULL_NAME){
                	displayName = ConfigurationUtil.geti18FrameworkString(context,type)+" "+name+" "+dvRev;
                }else if(ruleDisplay==RuleProcess.RULE_DISPLAY_MARKETING_NAME){
                	displayName = displayNameAttr;
                }else{
                	displayName = displayNameAttr+" "+dvRev;
                }

				setting.put("Registered Suite", "Configuration");
				//setting.put("Group Header", groupHeader);
				setting.put("Editable", "true");
				setting.put("Column Type", "programHTMLOutput");
				setting.put("function", "getDVColumnValuesForFO");
				setting.put("program", "LogicalFeature");
				setting.put("Submit", "true");
				setting.put("Export", "true");
				setting.put("TypeAhead", "true");
				setting.put("TypeAhead Character Count", "1");

				if (requestMap.get("noOfTargetFeatures") == null) {
					setting.put("Update Program", "LogicalFeature");
					setting.put("Update Function", "updateEffectivityExpressionValues");
				}
				column.put("settings", setting);

				//Lable will be always DV's display Name
				column.put("label", displayName);
				column.put("name", designVariantID);
				column.put("FeatureId", logicalFeatureId);
				if (noOfTargetFeatures > 0)
				 column.put("ActiveDVList", allDVListInSM);
				else
				 column.put("ActiveDVList", designVariantList);
				if (strContextProdId != null && !"".equals(strContextProdId)) {
					column.put("strContextProdId", strContextProdId);
				}

				column.put(DomainConstants.SELECT_ID, designVariantID);
				column.put("RelationshipId", strRelationshipId);
				
				// TODO- CG and Context Product related
				
				// setting range value for DV column
				StringBuffer rangeHref = new StringBuffer();
				/*rangeHref.append("../common/emxIndentedTable.jsp?submitURL=../configuration/GBOMDVChooserSubmit.jsp?mode=submitDVChoice&header=emxProduct.label.DesignChoicesFor&submitLabel=emxProduct.Button.Done&cancelLabel=emxProduct.Button.Cancel&Style=dialog&suiteKey=Configuration&table=");
				rangeHref.append(tabName);
				rangeHref.append("&program=ConfigurationFeature:getRangeValuesForDesignVariant&designVariantID=");
				rangeHref.append(designVariantID);
				rangeHref.append("&strVarBId=");
				rangeHref.append(strVBId);
				rangeHref.append("&strFeatureObjId=");
				rangeHref.append(logicalFeatureId);
				rangeHref.append("&strContextProdObjId=");
				rangeHref.append(strContextProdId);
				rangeHref.append("&isOnLoad=true&objectId=");
				rangeHref.append(designVariantID);
				rangeHref.append("&FTRGBOMDVNameCustomFilterCommand=*&FTRGBOMDVLimitCustomFilterCommand=*&toolbar=FTRGBOMTableDVChooserToolbar&selection=single&ShowIcons=true&relationship=relationship_ConfigurationOptions&direction=from&HelpMarker=emxhelpgbom&expandLevelFilter=false&selectedOption=");
				rangeHref.append(selectedOption); */

				rangeHref.append("../common/emxFullSearch.jsp?formName=featureOptions&table=FTRConfigurationFeaturesSearchResultsTable&selection=single&mode=ProductConfigurationChooser&field=TYPES=type_CONFIGURATIONFEATURES&CONFIG_PARENT_ID=");
				rangeHref.append(designVariantID);
				rangeHref.append("&parentId='+contextObjId+'&submitAction=refreshCaller&suiteKey=Configuration&includeOIDprogram=LogicalFeature:getDVOptions&submitURL=../configuration/FOEffectivitySearchUtil.jsp&mode=Chooser&chooserType=FormChooser&typeAheadTable=FTRFOTypeAheadTable");



				//set range href for column
				column.put("range",rangeHref.toString());

				returnList.add(column);
			}
			//-----------------------------------------------------------------------------------
			// TODO- Need to revisit--In case of Merge and Replace add column maps
			// for New Design Variants
			//-----------------------------------------------------------------------------------
			String label = "";

			for (int i = 0; i < newDesignVariantList.size(); i++) {

				setting = new HashMap();
				column = new HashMap();

				designVariant = (Map) newDesignVariantList.get(i);

				designVariantID = designVariant.get(DomainConstants.SELECT_ID)
				.toString();
				dvRev = designVariant.get(DomainConstants.SELECT_REVISION)
				.toString();
				strVBId = designVariant.get(DomainConstants.SELECT_RELATIONSHIP_ID)
				.toString();
				// marketing name of DV
				String displayNameAttr = designVariant.get(
						"attribute["
						+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME
						+ "]").toString();
				String name = designVariant.get(DomainObject.SELECT_NAME).toString();
				String type = designVariant.get(DomainObject.SELECT_TYPE).toString();

                if(ruleDisplay==RuleProcess.RULE_DISPLAY_FULL_NAME){
                	displayName = ConfigurationUtil.geti18FrameworkString(context,type)+" "+name+" "+dvRev;
                }else if(ruleDisplay==RuleProcess.RULE_DISPLAY_MARKETING_NAME){
                	displayName = displayNameAttr;
                }else{
                	displayName = displayNameAttr+" "+dvRev;
                }

				label = designVariant.get(DomainConstants.SELECT_ID).toString();
				dVariant = new DomainObject(new BusinessObject(label));
				
				logicalFeatureId = (String) designVariant.get("FeatureId");

				setting.put("Registered Suite", "Configuration");
				setting.put("Group Header", "emxProduct.Table.NewDesignVariant");
				setting.put("Editable", "true");
				setting.put("Column Type", "programHTMLOutput");
				setting.put("function", "getDVColumnValuesForFO");
				setting.put("program", "LogicalFeature");
				setting.put("Validate", "validateDesignVariantValue");
				setting.put("Export", "true");
				setting.put("TypeAhead", "true");
				setting.put("TypeAhead Character Count", "1");

				if (requestMap.get("noOfTargetFeatures") == null) {
					setting.put("Update Program", "LogicalFeature");
					setting.put("Update Function", "updateEffectivityExpressionValues");
				}
				column.put("settings", setting);

				//Lable will be always DV's display Name
				column.put("label", displayName);
				column.put("name", designVariantID);
				column.put("FeatureId", logicalFeatureId);
				column.put("ActiveDVList", allDVListInSM);

				//TODO -- Its Merge and Replace case-- not required to do getinfo here
				StringList objSelects = new StringList();
				objSelects.add("attribute["
						+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME + "]");
				objSelects.addElement(DomainObject.SELECT_REVISION);
				Map dvMap=dVariant.getInfo(context, objSelects);
				column.put("label", dvMap.get("attribute["
						+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME + "]")
						+ " "
						+ dvMap.get(DomainObject.SELECT_REVISION));
				column.put("FeatureId", logicalFeatureId);
				column.put("FeatureId", logicalFeatureId);

				if (strContextProdId != null && !"".equals(strContextProdId)) {
					column.put("strContextProdId", strContextProdId);
				}
				column.put("name", dVariant.getInfo(context,
						DomainConstants.SELECT_ID));
				column.put(DomainConstants.SELECT_ID, dVariant.getInfo(context,
						DomainConstants.SELECT_ID));
				column.put("RelationshipId", (String) designVariant
						.get(DomainConstants.SELECT_RELATIONSHIP_ID));
				
				StringBuffer rangeHref = new StringBuffer();
			/*	rangeHref.append("../common/emxIndentedTable.jsp?submitURL=../configuration/GBOMDVChooserSubmit.jsp?mode=submitDVChoice&header=emxProduct.label.DesignChoicesFor&submitLabel=Done&cancelLabel=Cancel&Style=dialog&suiteKey=Configuration&table=");
				rangeHref.append(tabName);
				rangeHref.append("&program=ConfigurationFeature:getRangeValuesForDesignVariant&designVariantID=");
				rangeHref.append(designVariantID);
				rangeHref.append("&strVarBId=");
				rangeHref.append(strVBId);
				rangeHref.append("&strFeatureObjId=");
				rangeHref.append(logicalFeatureId);
				rangeHref.append("&strContextProdObjId=");
				rangeHref.append(strContextProdId);
				rangeHref.append("&isOnLoad=true&objectId=");
				rangeHref.append(designVariantID);
				rangeHref.append("&toolbar=FTRGBOMTableDVChooserToolbar&selection=single&ShowIcons=true&HelpMarker=emxhelpgbom&expandLevelFilter=false&selectedOption=");
				rangeHref.append(selectedOption); */

				rangeHref.append("../common/emxFullSearch.jsp?formName=featureOptions&table=FTRConfigurationFeaturesSearchResultsTable&selection=single&mode=ProductConfigurationChooser&field=TYPES=type_CONFIGURATIONFEATURES&CONFIG_PARENT_ID=");
				rangeHref.append(designVariantID);
				rangeHref.append("&parentId='+contextObjId+'&submitAction=refreshCaller&suiteKey=Configuration&includeOIDprogram=LogicalFeature:getDVOptions&submitURL=../configuration/FOEffectivitySearchUtil.jsp&mode=Chooser&chooserType=FormChooser&typeAheadTable=FTRFOTypeAheadTable");


				//set Range Href
				column.put("range",rangeHref.toString());

				returnList.add(column);
			}
			//-----------------------------------------------------------------------------------
			// TODO- END Need to revisit--In case of Merge and Replace add column maps
			// for New Design Variants
			//-----------------------------------------------------------------------------------
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
		return returnList;
	}

	public MapList getDesignVariantColumnsForGridEff(Context context, String[] args)
	throws Exception {


		// This will be MapList of the parts connected with GBOM relationship to
		// the logical Feature
		MapList returnList = new MapList();

		try {
			Map programMap = (HashMap) JPO.unpackArgs(args);
			Map requestMap = (HashMap) programMap.get("requestMap");
			String strContextProdId = (String) requestMap.get("prodId");
			int ruleDisplay=RuleProcess.getRuleDisplaySetting(context);
			//-----------------------------------------------------------
			// TODO Start Revisit for the Merge replace related code
			//-----------------------------------------------------------
			int noOfTargetFeatures = 0;
			MapList newDesignVariantList = new MapList();
			MapList allDVListInSM = new MapList();

			if (requestMap.get("noOfTargetFeatures") != null) {
				noOfTargetFeatures = Integer.parseInt((String) requestMap
						.get("noOfTargetFeatures"));
			}
			if (noOfTargetFeatures > 0) {
				String featureId = "";
				HashMap featureMap = null;
				String[] arg = null;
				MapList designVariantList = null;
				Map tempMap = null;
				for (int j = 0; j < noOfTargetFeatures; j++) {
					// In case of Merge and Replace get ids for Target Features
					featureId = (String) requestMap.get("objectIDs" + j);
					featureMap = new HashMap();
					featureMap.put("objectId", featureId);
					featureMap.put("parentOID", featureId);

					arg = JPO.packArgs(featureMap);
					designVariantList = getActiveDesignVariants(context, arg);

					for (int k = 0; k < designVariantList.size(); k++) {
						tempMap = (Map) designVariantList.get(k);
						tempMap.put("FeatureId", featureId);
					}
					newDesignVariantList.addAll(designVariantList);
					allDVListInSM.addAll(designVariantList);
				}
			}
			// use the Logical Feature and retrieve active design variants
			String[] arg = JPO.packArgs(requestMap);
			String logicalFeatureId = (String) requestMap.get("objectId");

			MapList designVariantList = getActiveDesignVariants(context, arg);
			allDVListInSM.addAll(designVariantList);
			boolean remove = false;
			Map newDesignVariant = null;
			String newDesignVariantId = "";
			Map designVariant = null;
			String designVariantId = "";
			for (int k = newDesignVariantList.size(); k > 0; k--) {
				remove = false;
				newDesignVariant = (Map) newDesignVariantList.get(k - 1);
				newDesignVariantId = (String) newDesignVariant
				.get(DomainConstants.SELECT_ID);

				// Remove the Design Variants from New Design Variants List which
				// are connected to Master Feature
				for (int m = 0; m < designVariantList.size(); m++) {
					designVariant = (Map) designVariantList.get(m);
					designVariantId = (String) designVariant
					.get(DomainConstants.SELECT_ID);
					if (newDesignVariantId.equals(designVariantId))
						remove = true;
				}
				if (remove)
					newDesignVariantList.remove(newDesignVariant);
			}
			// Defining column Maps for the design variant
			Map setting = null;
			Map column = null;
			String designVariantID = "";
			String strVBId = "";
			DomainObject dVariant = null;
			String dvRev = "";
			String displayName = "";

			// iterate on all the Active DVs
			for (int i = 0; i < designVariantList.size(); i++) {
				setting = new HashMap();
				column = new HashMap();
				designVariant = (Map) designVariantList.get(i);
				// get DV id name rev (configuration Feature) and varies by rel id
				designVariantID = designVariant.get(DomainConstants.SELECT_ID)
				.toString();
				dvRev = designVariant.get(DomainConstants.SELECT_REVISION)
				.toString();
				strVBId = designVariant.get(DomainConstants.SELECT_RELATIONSHIP_ID)
				.toString();
				// marketing name of DV
				String displayNameAttr = designVariant.get(
						"attribute["
						+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME
						+ "]").toString();
			/*
                if(ruleDisplay==RuleProcess.RULE_DISPLAY_FULL_NAME){
                	displayName = ConfigurationUtil.geti18FrameworkString(context,type)+" "+name+" "+dvRev;
                }else if(ruleDisplay==RuleProcess.RULE_DISPLAY_MARKETING_NAME){
                	displayName = displayNameAttr;
                }else{ */
                	displayName = displayNameAttr;
               // }

				setting.put("Registered Suite", "Configuration");
				setting.put("Editable", "true");
				setting.put("Column Type", "programHTMLOutput");
				setting.put("function", "getDVColumnValuesForGridEff");
				setting.put("program", "LogicalFeature");
				setting.put("Submit", "true");
				setting.put("Export", "true");
				setting.put("TypeAhead", "true");
				setting.put("TypeAhead Character Count", "1");
				setting.put("Type Ahead Mapping", "DISPLAY_NAME,NAME");
				setting.put("Show Clear Button", "true");
				setting.put("Edit Access Program", "LogicalFeature");
				setting.put("Edit Access Function", "isEffGridEditableForContext");

				column.put("settings", setting);

				//Lable will be always DV's display Name
				column.put("label", displayName);
				column.put("name", designVariantID);
				column.put("FeatureId", logicalFeatureId);
				if (noOfTargetFeatures > 0)
				 column.put("ActiveDVList", allDVListInSM);
				else
				 column.put("ActiveDVList", designVariantList);
				if (strContextProdId != null && !"".equals(strContextProdId)) {
					column.put("strContextProdId", strContextProdId);
				}

				column.put(DomainConstants.SELECT_ID, designVariantID);
				column.put("RelationshipId", strVBId);
				
				
				// setting range value for DV column
				StringBuffer rangeHref = new StringBuffer();

				rangeHref.append("../common/emxFullSearch.jsp?formName=featureOptions&table=FTRConfigurationFeaturesSearchResultsTable&showInitialResults=true&selection=single&mode=ProductConfigurationChooser&field=TYPES=type_CONFIGURATIONFEATURES&CONFIG_PARENT_ID=");				
				rangeHref.append(designVariantID);
				rangeHref.append("&submitAction=refreshCaller&suiteKey=Configuration&includeOIDprogram=LogicalFeature:getDVOptions&submitURL=../configuration/FTREffectivitySearchUtil.jsp&effType=FOEffectivity&typeAheadTable=FTRFOTypeAheadTable");

				//set range href for column
				column.put("range",rangeHref.toString());

				returnList.add(column);
			}

			String label = "";
			
			for (int i = 0; i < newDesignVariantList.size(); i++) {

				setting = new HashMap();
				column = new HashMap();

				designVariant = (Map) newDesignVariantList.get(i);

				designVariantID = designVariant.get(DomainConstants.SELECT_ID)
				.toString();
				dvRev = designVariant.get(DomainConstants.SELECT_REVISION)
				.toString();
				strVBId = designVariant.get(DomainConstants.SELECT_RELATIONSHIP_ID)
				.toString();
				// marketing name of DV
				String displayNameAttr = designVariant.get(
						"attribute["
						+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME
						+ "]").toString();
				String name = designVariant.get(DomainObject.SELECT_NAME).toString();
				String type = designVariant.get(DomainObject.SELECT_TYPE).toString();

                if(ruleDisplay==RuleProcess.RULE_DISPLAY_FULL_NAME){
                	displayName = ConfigurationUtil.geti18FrameworkString(context,type)+" "+name+" "+dvRev;
                }else if(ruleDisplay==RuleProcess.RULE_DISPLAY_MARKETING_NAME){
                	displayName = displayNameAttr;
                }else{
                	displayName = displayNameAttr+" "+dvRev;
                }

				label = designVariant.get(DomainConstants.SELECT_ID).toString();
				dVariant = new DomainObject(new BusinessObject(label));
				
				logicalFeatureId = (String) designVariant.get("FeatureId");

				setting.put("Registered Suite", "Configuration");
				setting.put("Group Header", "emxProduct.Table.NewDesignVariant");
				setting.put("Editable", "true");
				setting.put("Column Type", "programHTMLOutput");
				setting.put("function", "getDVColumnValuesForGridEff");
				setting.put("program", "LogicalFeature");
				setting.put("Validate", "validateDesignVariantValue");
				setting.put("Export", "true");
				setting.put("TypeAhead", "true");
				setting.put("TypeAhead Character Count", "1");
				setting.put("Type Ahead Mapping", "DISPLAY_NAME,NAME");
				setting.put("Show Clear Button", "true");
				setting.put("Edit Access Program", "LogicalFeature");
				setting.put("Edit Access Function", "isEffGridEditableForContext");
				//setting.put("OnChange Handler", "modifyForRemoveOption");
				//setting.put("Validate", "modifyForRemoveOption");
			/*
				if (requestMap.get("noOfTargetFeatures") == null) {
					setting.put("Update Program", "LogicalFeature");
					setting.put("Update Function", "updateEffectivityExpressionValues1");
				}
			*/
				column.put("settings", setting);

				//Lable will be always DV's display Name
				column.put("label", displayName);
				column.put("name", designVariantID);
				column.put("FeatureId", logicalFeatureId);
				column.put("ActiveDVList", allDVListInSM);

				//TODO -- Its Merge and Replace case-- not required to do getinfo here
				StringList objSelects = new StringList();
				objSelects.add("attribute["
						+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME + "]");
				objSelects.addElement(DomainObject.SELECT_REVISION);
				Map dvMap=dVariant.getInfo(context, objSelects);
				column.put("label", dvMap.get("attribute["
						+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME + "]")
						+ " "
						+ dvMap.get(DomainObject.SELECT_REVISION));
				column.put("FeatureId", logicalFeatureId);

				if (strContextProdId != null && !"".equals(strContextProdId)) {
					column.put("strContextProdId", strContextProdId);
				}
				column.put("name", dVariant.getInfo(context,
						DomainConstants.SELECT_ID));
				column.put(DomainConstants.SELECT_ID, dVariant.getInfo(context,
						DomainConstants.SELECT_ID));
				column.put("RelationshipId", (String) designVariant
						.get(DomainConstants.SELECT_RELATIONSHIP_ID));

				
				StringBuffer rangeHref = new StringBuffer();
			
				rangeHref.append("../common/emxFullSearch.jsp?formName=featureOptions&table=FTRConfigurationFeaturesSearchResultsTable&showInitialResults=true&selection=single&mode=ProductConfigurationChooser&field=TYPES=type_CONFIGURATIONFEATURES&CONFIG_PARENT_ID=");				
				rangeHref.append(designVariantID);
				rangeHref.append("&submitAction=refreshCaller&suiteKey=Configuration&includeOIDprogram=LogicalFeature:getDVOptions&submitURL=../configuration/FTREffectivitySearchUtil.jsp&effType=FOEffectivity&typeAheadTable=FTRFOTypeAheadTable");


				//set Range Href
				column.put("range",rangeHref.toString());

				returnList.add(column);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
		return returnList;

	}

	/**
	 * This will render the DV column's value
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public List getDVColumnValuesForFO(Context context, String[] args)
	throws Exception {
		List valueList = new StringList();

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			
			// Map paramMap = (Map) programMap.get ("paramList");
			List ObjectIdsList = (MapList) programMap.get("objectList");
			HashMap requestMap = (HashMap) programMap.get("paramList");

			Map columnMap = (HashMap) programMap.get("columnMap");
			for (int i = 0; i < ObjectIdsList.size(); i++) {

				//Relationshipid LF/GBOM
				String strRelationshipId = (String) requestMap.get("relationshipId");

				//Get the id of the Object in given Col
				String strColumnID = (String) columnMap.get("id");

				//Get Info related to this rel id and display the value for given col
				DomainRelationship domRel = new DomainRelationship(strRelationshipId);
				StringList relInfoList = new StringList();
				relInfoList.addElement(SELECT_TO_ID);

				relInfoList.addElement("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.to.id");
				relInfoList.addElement("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.to.name");
				relInfoList.addElement("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.from.id");
				Map relInfoMap = domRel.getRelationshipData(context, relInfoList);
				

				Object objSelDVVal = relInfoMap.get("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.to.id");
				Object objDVColumnId = relInfoMap.get("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.from.id");

				StringList sLSelDVValName = new StringList();
				StringList sLDVColumnId = new StringList();

				if(!ConfigurationUtil.isObjectNull(context, objSelDVVal))
				{
					 if(objSelDVVal instanceof StringList)
					 {
						 sLSelDVValName = (StringList)relInfoMap.get("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.to.name");

					 }else if(objSelDVVal instanceof String)
					 {
						 sLSelDVValName.addElement((String)relInfoMap.get("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.to.name"));
				     }
				}


				if(!ConfigurationUtil.isObjectNull(context, objDVColumnId))
				{
					 if(objDVColumnId instanceof StringList)
					 {
						 sLDVColumnId = (StringList)relInfoMap.get("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.from.id");

					 }else if(objDVColumnId instanceof String)
					 {
						 sLDVColumnId.addElement((String)relInfoMap.get("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.from.id"));
				     }
				}

				for(int iCnt=0;iCnt<sLDVColumnId.size();iCnt++){
					String strDVColumnId = (String)sLDVColumnId.get(iCnt);
					String strDVColumnName = (String)sLSelDVValName.get(iCnt);
					if(strColumnID!=null && strColumnID.equals(strDVColumnId)){
						valueList.add(i, strDVColumnName);
					}/*else{
						valueList.add(i, "");
					}*/
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}

		if(valueList.size()== 0){

			valueList.add(0, "-");
		}

		return valueList;
	}

	public List getDVColumnValuesForGridEff(Context context, String[] args)
	throws Exception {
		List valueList = new StringList();
		boolean flagOptionAdded = false;
		try {
			
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
		      
			Map paramMap = (Map) programMap.get ("paramList");
			List ObjectIdsList = (MapList) programMap.get("objectList");
			
			// For export to CSV
			String exportFormat = null;
			boolean exportToExcel = false;
			if(paramMap!=null && paramMap.containsKey("reportFormat")){
      			exportFormat = (String)paramMap.get("reportFormat");
      		}
      		if("CSV".equals(exportFormat)){
      			exportToExcel = true;
      		}
			
			Map columnMap = (HashMap) programMap.get("columnMap");
			Map partObj = null;
			String relationshipID = "";
			StringBuffer strDVColumnOidList = null;
			StringBuffer strDVColumnNameList = null;
			for (int i = 0; i < ObjectIdsList.size(); i++) {
				flagOptionAdded = false;
				strDVColumnOidList = new StringBuffer();
				strDVColumnNameList = new StringBuffer();
				
				//Relationshipid LF/GBOM
				//String strRelationshipId = (String) requestMap.get("relationshipId");
				//changes for milford workshop
				// get the Object
				partObj = (Map) ObjectIdsList.get(i);
				// retrieve the GBOM rel id
				relationshipID = (String) partObj
				.get(DomainConstants.SELECT_RELATIONSHIP_ID);
				//END changes for milford workshop
				
				//Get the id of the Object in given Col
				String strColumnID = (String) columnMap.get("id");
				
				//Get Info related to this rel id and display the value for given col
				DomainRelationship domRel = new DomainRelationship(relationshipID);
				StringList relInfoList = new StringList();
				relInfoList.addElement("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.to.id");
				relInfoList.addElement("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.to.name");
				relInfoList.addElement("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.from.id");
				relInfoList.addElement("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.to.attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME+"].value");
				
				Map relInfoMap = domRel.getRelationshipData(context, relInfoList);
								
				Object objSelDVVal = relInfoMap.get("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.to.id");
				
				StringList sLSelDVValName = new StringList();
				StringList sLSelDVValOid = new StringList();
				StringList sLDVColumnId = new StringList();
				StringList sLDVDisplayName = new StringList();
				Set tempSet = null;
				String[] tempStrArr = new String[]{};
				if(!ConfigurationUtil.isObjectNull(context, objSelDVVal))
				{
					 if(objSelDVVal instanceof StringList)
					 {
						 sLSelDVValName = (StringList)relInfoMap.get("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.to.name");
						 sLSelDVValOid = (StringList)relInfoMap.get("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.to.id");
						 sLDVColumnId = (StringList)relInfoMap.get("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.from.id");
						 sLDVDisplayName = (StringList)relInfoMap.get("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.to.attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME+"].value");
					 }else if(objSelDVVal instanceof String)
					 {
						 sLSelDVValName.addElement((String)relInfoMap.get("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.to.name"));
						 sLSelDVValOid.addElement((String)relInfoMap.get("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.to.id"));
						 sLDVColumnId.addElement((String)relInfoMap.get("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.from.id"));
						 sLDVDisplayName.addElement((String)relInfoMap.get("frommid["+ConfigurationConstants.RELATIONSHIP_EFFECTIVY_USAGE+"].torel.to.attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME+"].value"));
				     }
				}

				tempSet = new LinkedHashSet(sLSelDVValName);
				tempStrArr = (String[])tempSet.toArray(tempStrArr);
				sLSelDVValName = new StringList(tempStrArr);

				tempSet = new LinkedHashSet(sLSelDVValOid);
				tempStrArr = (String[])tempSet.toArray(tempStrArr);
				sLSelDVValOid = new StringList(tempStrArr);

                StringList objectSelects = new StringList();
                objectSelects.add(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
                objectSelects.add(ConfigurationConstants.SELECT_ID);
				MapList mLSelDVVal = DomainObject.getInfo(context, tempStrArr, objectSelects);
				Map mpDisplayName = new HashMap();
				for(int j=0;j<mLSelDVVal.size();j++){
					Map mpDVVal = (Map) mLSelDVVal.get(j);
					String strID = (String) mpDVVal.get(ConfigurationConstants.SELECT_ID);
					String strDisplayName = (String) mpDVVal.get(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
					mpDisplayName.put(strID, strDisplayName);
				}
				
				tempSet = new LinkedHashSet(sLDVColumnId);
				tempStrArr = (String[])tempSet.toArray(tempStrArr);
				sLDVColumnId = new StringList(tempStrArr);

			
				String strDVDisplayName= "";
				for(int iCnt=0;iCnt<sLDVColumnId.size();iCnt++){
					String strDVColumnId = (String)sLDVColumnId.get(iCnt);
					String strDVColumnOid = (String)sLSelDVValOid.get(iCnt);
					strDVDisplayName = (String) mpDisplayName.get(strDVColumnOid);
					
					//if(strColumnID!=null && strColumnID.equals(strDVColumnId)){
					if(strColumnID!=null && isRevisionsUsedInEffectivity(context, strColumnID,strDVColumnId) ){
						strDVColumnOidList.append(strDVColumnOid);
						strDVColumnOidList.append(",");
						strDVColumnNameList.append(strDVDisplayName);
						strDVColumnNameList.append(",");

						flagOptionAdded = true;
						break;
					}
				}
				if(flagOptionAdded){
									
					strDVColumnOidList.delete(strDVColumnOidList.lastIndexOf(","),(strDVColumnOidList.lastIndexOf(",")+1));		
					strDVColumnNameList.delete(strDVColumnNameList.lastIndexOf(","),(strDVColumnNameList.lastIndexOf(",")+1));		
					
					if(exportToExcel){
						valueList.add(strDVDisplayName);
					}else{
					valueList.add(strDVColumnNameList + 
							"<input type=\"hidden\" name=\"" + strColumnID +"_"+"ORG"+"_"+ relationshipID + "\" id=\"" + strColumnID +"_"+"ORG"+"_"+ relationshipID + "\" value=\""+ strDVColumnOidList +"\" />"
					);
					}
					
				}
				else{
					if(exportToExcel){
				
						valueList.add("");
					}else{
					valueList.add(
							"<input type=\"hidden\" name=\"" + strColumnID +"_"+"ORG"+"_"+ relationshipID + "\" id=\"" + strColumnID +"_"+"ORG"+"_"+ relationshipID + "\" value=\"\" />"
					);
				}		
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}

		if(valueList.size()== 0){

			valueList.add(0, "-");
		}

		return valueList;
	}
	
	/**
	 * This method is used to check whether the CF revision is used in any Effectivity
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	private boolean isRevisionsUsedInEffectivity(Context context,
			String strColumnID, String strDVColumnId) {
		// TODO Auto-generated method stub
		try {
			DomainObject domObj = new DomainObject(strDVColumnId);
			
			StringList singleValueSelects = new StringList(DomainObject.SELECT_ID);
			StringList multiValueSelects = new StringList();
			
			List listValues = domObj.getRevisionsInfo(context, singleValueSelects, multiValueSelects);
			ArrayList objectIds = new ArrayList();
			Map strList = null;
			for (int i = 0; i < listValues.size(); i++) {
				strList = (Map) listValues.get(i);
				objectIds.add(strList.get(DomainConstants.SELECT_ID));
			}
			if(objectIds.contains(strColumnID)){
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * This is column JPO method used to render Display Name and revision for
	 * the DV chooser
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */

	public List getDVChooserDisplayNameRev(Context context, String[] args)
	throws Exception {
        //XSSOK -Deprecated
		Vector vecDVDisplayNameRev = new Vector();
		try {
			String strName = "";
			String strRev = "";
			String strDisplayId = "";
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramList");
			String suiteDir = (String) paramMap.get("SuiteDirectory");
			String suiteKey = (String) paramMap.get("suiteKey");
			String exportFormat = null;
			if(paramMap!=null && paramMap.containsKey("reportFormat")){
				exportFormat = (String)paramMap.get("reportFormat");
			}
			MapList objectList = (MapList) programMap.get("objectList");
			int iNumOfObjects = objectList.size();
			Map tempMap = null;
			//Logic to display blank first
			objectList.addSortKey("attribute["
					+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME
					+ "]", "ascending", "string");
			objectList.sortStructure();
			for (int iCnt = 0; iCnt < iNumOfObjects; iCnt++) {
				tempMap = (Map) objectList.get(iCnt);
				// Add display name and revision, for Blank add - in Display name
				StringBuffer temp = new StringBuffer();
				if(ConfigurationUtil.isNotNull(exportFormat) && ("CSV".equalsIgnoreCase(exportFormat))){
					if (tempMap.containsKey("attribute["
							+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME
							+ "]")) {
						strName = (String) tempMap.get("attribute["
								+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME
								+ "]");
						strRev = (String) tempMap.get("revision");
						vecDVDisplayNameRev.add(strName+" "+strRev);
					}
					else
						vecDVDisplayNameRev.add("-");
			}
				else{
				if (tempMap.containsKey("attribute["
						+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME
						+ "]")) {
					strName = (String) tempMap.get("attribute["
							+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME
							+ "]");
					strRev = (String) tempMap.get("revision");
						strDisplayId = (String) tempMap.get(DomainConstants.SELECT_ID);
						temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
						temp.append(suiteDir);
						temp.append("&amp;suiteKey=");
						temp.append(suiteKey);
						temp.append("&amp;objectId=");
						temp.append(strDisplayId);
						temp.append("', '450', '300', 'true', 'popup')\">");
						temp.append(strName+" "+strRev);
						temp.append("</a>");
						vecDVDisplayNameRev.add(temp.toString());
				} else
					vecDVDisplayNameRev.add("-");
			}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
		return vecDVDisplayNameRev;

	}

	/**
	 * This is column JPO method used to render Name and revision for the DV
	 * chooser
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */

	public List getDVChooserNameRev(Context context, String[] args)
	throws Exception {

		Vector vecDVNameRev = new Vector();
		try {
			String strName = "";
			String strRev = "";
			StringBuffer stbNameRev;

			HashMap programMap = (HashMap) JPO.unpackArgs(args);

			MapList objectList = (MapList) programMap.get("objectList");
			int iNumOfObjects = objectList.size();
			Map tempMap = null;
			for (int iCnt = 0; iCnt < iNumOfObjects; iCnt++) {
				stbNameRev = new StringBuffer(100);
				tempMap = (Map) objectList.get(iCnt);
				// Add display name and revision, for Blank add - in Display name
				if (tempMap.containsKey("name")) {
					strName = (String) tempMap.get("name");
					strRev = (String) tempMap.get("revision");

					stbNameRev = stbNameRev.append(strName);
					stbNameRev.append(" ");
					stbNameRev.append(strRev);
					vecDVNameRev.add(stbNameRev.toString());
				} else
					vecDVNameRev.add(DomainConstants.EMPTY_STRING);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
		return vecDVNameRev;

	}

	/**
	 *This is column JPO method used to render Type of DV options
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Vector getDVChooserType(Context context, String args[])
	throws Exception {
		Vector vecType = new Vector();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strLanguage = context.getSession().getLanguage();
			String strType = "";
			String typeKey = "";
			MapList objectList = (MapList) programMap.get("objectList");
			int iNumOfObjects = objectList.size();
			Map tempMap = null;
			for (int iCnt = 0; iCnt < iNumOfObjects; iCnt++) {
				tempMap = (Map) objectList.get(iCnt);
				if (tempMap.containsKey(DomainConstants.SELECT_TYPE)) {
					strType = (String) tempMap.get(DomainConstants.SELECT_TYPE);
					if (strType
							.equalsIgnoreCase(ConfigurationConstants.TYPE_CONFIGURATION_FEATURE)) {
						typeKey = "emxConfiguration.Type.ConfigurationFeature";
					} else if (strType
							.equalsIgnoreCase(ConfigurationConstants.TYPE_CONFIGURATION_OPTION)) {
						typeKey = "emxConfiguration.Type.ConfigurationOption";
					}
					vecType.add(EnoviaResourceBundle.getProperty(context, SUITE_KEY,typeKey,strLanguage));
				} else {
					vecType.add(DomainConstants.EMPTY_STRING);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
		return vecType;
	}

	/**
	 * This is column JPO method used to render State of DV options
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Vector getDVChooserState(Context context, String args[])
	throws Exception {
		Vector vecState = new Vector();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);

			MapList objectList = (MapList) programMap.get("objectList");
			int iNumOfObjects = objectList.size();
			Map tempMap = null;
			String strLanguage = context.getSession().getLanguage();
			String strState = "";
			String stateKey = "";
			for (int iCnt = 0; iCnt < iNumOfObjects; iCnt++) {
				tempMap = (Map) objectList.get(iCnt);
				if (tempMap.containsKey(DomainConstants.SELECT_CURRENT)) {
					strState = (String) tempMap
							.get(DomainConstants.SELECT_CURRENT);
					stateKey = "emxFramework.State." + strState;
					vecState.add(EnoviaResourceBundle.getProperty(context, "Framework",
								 stateKey,strLanguage));
				} else {
					vecState.add(DomainConstants.EMPTY_STRING);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
		return vecState;
	}

	/**
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public static Object getFieldChoices(Context context, String[] args)
	throws Exception {
		HashMap tempMap = new HashMap();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);

			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String strSelectedOption = (String) requestMap.get("selectedOption");

			StringList fieldRangeValDisp = new StringList();
			StringList fieldRangeValues = new StringList();
			String languageStr = context.getSession().getLanguage();
			String strCommonGroups = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
			"emxProduct.GBOMPartTable.CommonGroups",languageStr);
			String confOptions =  EnoviaResourceBundle.getProperty(context, SUITE_KEY,
			"emxProduct.GBOMPartTable.ConfigurationOption",languageStr);
			if ("ConfigurationOption".equalsIgnoreCase(strSelectedOption)) {
				fieldRangeValDisp.addElement(confOptions);
				fieldRangeValDisp.addElement(strCommonGroups);
				fieldRangeValues.addElement("ConfigurationOption");
				fieldRangeValues.addElement("Common Groups");
			} else {
				fieldRangeValDisp.addElement(strCommonGroups);
				fieldRangeValDisp.addElement(confOptions);
				fieldRangeValues.addElement("Common Groups");
				fieldRangeValues.addElement("ConfigurationOption");
			}
			tempMap.put("field_choices", fieldRangeValues);
			tempMap.put("field_display_choices", fieldRangeValDisp);
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
		return tempMap;
	}

	/**
	 * This is update program , will be called on applying after DV choosing
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	/*
	public HashMap updateDesignVariantValues(Context context, String[] args)
	throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);

		Map columnMap = (HashMap) programMap.get("columnMap");
		Map paramMap = (HashMap) programMap.get("paramMap");
		Map requestMap = (HashMap) programMap.get("requestMap");
		//New Value will be COnfiguration Option OID |Configuration options rel ID
		String newValue = (String) paramMap.get("New Value");
		String strIRuleId = "";
		// GBOM rel ID
		String relationshipID = (String) paramMap.get("relId");
		// retrieve inclusion rule ID from GBOM rel id
		DomainRelationship domRelGBOM = new DomainRelationship(relationshipID);
		Map ht = domRelGBOM.getRelationshipData(context, new StringList(
				"tomid[" + ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
				+ "].from.id"));
		if (!((StringList) ht.get("tomid["
				+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
				+ "].from.id")).isEmpty())
			strIRuleId = (String) ((StringList) ht.get("tomid["
					+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
					+ "].from.id")).get(0);

		// DV -configuration Feature ID
		String designVariantId = (String) columnMap
		.get(DomainConstants.SELECT_NAME);

		RelationshipType rightExp = new RelationshipType(
				ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION);

		// Strings initialized to retieve the corresponding GBOM object
		String newConfOptionId = newValue;
		String confOptionsRelID = "";

		String strContextProductId = (String) requestMap.get("prodId");

		Map argMap = new HashMap();
		argMap.put("IRuleId", strIRuleId);
		argMap.put("GBOMRelId", relationshipID);
		argMap.put("DesignVariantId", designVariantId);
		if (((strContextProductId != null))
				&& (!(strContextProductId.equalsIgnoreCase("")))) {
			argMap.put("strContextProdObjId", strContextProductId);
		}
		LogicalFeature logFTR= new LogicalFeature();
		argMap = logFTR.getDVValueForPart(context, argMap);

		boolean isCommonGroup = false;

		if (!newValue.equals("-")) {
			StringTokenizer newValueTZ = new StringTokenizer(newValue, "|");
			if (newValueTZ.hasMoreElements()) {
				newConfOptionId = newValueTZ.nextToken();
			}
			DomainObject domObj = new DomainObject(newConfOptionId);
			//TODO - CG related
			if (!domObj.exists(context)) {
				isCommonGroup = true;
			}
			if (newValueTZ.hasMoreElements()) {
				confOptionsRelID = newValueTZ.nextToken();
			}
		}

		if(isCommonGroup){
			argMap.put("New Configuration Option Id", newConfOptionId);
			argMap.put("New Configuration Options rel Id",newConfOptionId);
			argMap.put("isCommonGroup", Boolean.valueOf(isCommonGroup));
		}else{
			argMap.put("New Configuration Option Id", newConfOptionId);
			argMap.put("New Configuration Options rel Id",confOptionsRelID);
			argMap.put("isCommonGroup", Boolean.valueOf(isCommonGroup));
		}

		LogicalFeature.updateDesignVariantValue(context, argMap);
		String parentOID=(String) requestMap
		.get("parentOID");
		// Code to start background job for Duplicate Part XML.
		Part.updateDuplicatePartXML(context, parentOID);
		ProductConfiguration.updateBOMXMLAttributeOnPC(context,parentOID);

		return programMap;
	}
	*/

	public HashMap updateDesignVariantValues(Context context, String[] args)
	throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);

		Map columnMap = (HashMap) programMap.get("columnMap");
		Map paramMap = (HashMap) programMap.get("paramMap");
		Map requestMap = (HashMap) programMap.get("requestMap");
		//New Value will be COnfiguration Option OID |Configuration options rel ID
		String newValue = (String) paramMap.get("New Value");
		String strIRuleId = "";
		// GBOM rel ID
		String relationshipID = (String) paramMap.get("relId");
		// retrieve inclusion rule ID from GBOM rel id
		DomainRelationship domRelGBOM = new DomainRelationship(relationshipID);
		Map ht = domRelGBOM.getRelationshipData(context, new StringList(
				"tomid[" + ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
				+ "].from.id"));
		if (!((StringList) ht.get("tomid["
				+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
				+ "].from.id")).isEmpty())
			strIRuleId = (String) ((StringList) ht.get("tomid["
					+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
					+ "].from.id")).get(0);

		// DV -configuration Feature ID
		String designVariantId = (String) columnMap
		.get(DomainConstants.SELECT_NAME);

		
		// Strings initialized to retieve the corresponding GBOM object
		String newConfOptionId = newValue;
		String confOptionsRelID = "";

		String strContextProductId = (String) requestMap.get("prodId");

		Map argMap = new HashMap();
		argMap.put("IRuleId", strIRuleId);
		argMap.put("GBOMRelId", relationshipID);
		argMap.put("DesignVariantId", designVariantId);
		if (((strContextProductId != null))
				&& (!(strContextProductId.equalsIgnoreCase("")))) {
			argMap.put("strContextProdObjId", strContextProductId);
		}
		LogicalFeature logFTR= new LogicalFeature();
		argMap = logFTR.getDVValueForPart(context, argMap);

		boolean isCommonGroup = false;

		if (!newValue.equals("-")) {
			StringTokenizer newValueTZ = new StringTokenizer(newValue, "|");
			if (newValueTZ.hasMoreElements()) {
				newConfOptionId = newValueTZ.nextToken();
			}
			DomainObject domObj = new DomainObject(newConfOptionId);
			//TODO - CG related
			if (!domObj.exists(context)) {
				isCommonGroup = true;
			}
			if (newValueTZ.hasMoreElements()) {
				confOptionsRelID = newValueTZ.nextToken();
			}
		}

		if(isCommonGroup){
			argMap.put("New Configuration Option Id", newConfOptionId);
			argMap.put("New Configuration Options rel Id",newConfOptionId);
			argMap.put("isCommonGroup", Boolean.valueOf(isCommonGroup));
		}else{
			argMap.put("New Configuration Option Id", newConfOptionId);
			argMap.put("New Configuration Options rel Id",confOptionsRelID);
			argMap.put("isCommonGroup", Boolean.valueOf(isCommonGroup));
		}

		LogicalFeature.updateDesignVariantValue(context, argMap);
		String parentOID=(String) requestMap
		.get("parentOID");
		// Code to start background job for Duplicate Part XML.
		Part.updateDuplicatePartXML(context, parentOID);
		ProductConfiguration.updateBOMXMLAttributeOnPC(context,parentOID);

		return programMap;
	}



	public HashMap updateEffectivityExpressionValues(Context context, String[] args)
	throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);

		Map columnMap = (HashMap) programMap.get("columnMap");
		Map paramMap = (HashMap) programMap.get("paramMap");
		Map requestMap = (HashMap) programMap.get("requestMap");
		//New Value will be COnfiguration Option OID |Configuration options rel ID
		String newValue = (String) paramMap.get("New Value");
		String strIRuleId = "";
		// GBOM rel ID
		String relationshipID = (String) paramMap.get("relId");


		String parentFeatureID = (String)columnMap.get("name");
		String optionFeatureID = (String)paramMap.get("New Value");

        JSONObject effObj = new JSONObject();
        effObj.put("parentId", parentFeatureID);
        effObj.put("objId", optionFeatureID);

        if("".equalsIgnoreCase(relationshipID)|| "null".equalsIgnoreCase(relationshipID) || relationshipID == null){

        	relationshipID = getRelID(context, parentFeatureID, optionFeatureID);
        }

        effObj.put("relId", relationshipID);
        
       
		// retrieve inclusion rule ID from GBOM rel id
		DomainRelationship domRelGBOM = new DomainRelationship(relationshipID);
		Map ht = domRelGBOM.getRelationshipData(context, new StringList(
				"tomid[" + ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
				+ "].from.id"));
		if (!((StringList) ht.get("tomid["
				+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
				+ "].from.id")).isEmpty())
			strIRuleId = (String) ((StringList) ht.get("tomid["
					+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
					+ "].from.id")).get(0);

		// DV -configuration Feature ID
		String designVariantId = (String) columnMap
		.get(DomainConstants.SELECT_NAME);

				
		String strContextProductId = (String) requestMap.get("prodId");

		Map argMap = new HashMap();
		argMap.put("IRuleId", strIRuleId);
		argMap.put("GBOMRelId", relationshipID);
		argMap.put("DesignVariantId", designVariantId);
		if (((strContextProductId != null))
				&& (!(strContextProductId.equalsIgnoreCase("")))) {
			argMap.put("strContextProdObjId", strContextProductId);
		}
		LogicalFeature logFTR= new LogicalFeature();
		argMap = logFTR.getDVValueForPart(context, argMap);

		if (!newValue.equals("-")) {
			StringTokenizer newValueTZ = new StringTokenizer(newValue, "|");
			if (newValueTZ.hasMoreElements()) {
				newValueTZ.nextToken();
			}
			
			if (newValueTZ.hasMoreElements()) {
				newValueTZ.nextToken();
			}
		}

		return programMap;
	}

	/**
	 * This method is used to return the Name of the part family of Logical Feature
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds arguments
	 * @return List- the List of Strings containing part families name.
	 * @throws Exception
	 *             if the operation fails
	 */
	public List getPartFamily(Context context, String[] args) throws Exception {
		// unpack the arguments
		Map programMap = (HashMap) JPO.unpackArgs(args);

		List lstobjectList = (MapList) programMap.get("objectList");
		// initialise the local variables
		Map objectMap = new HashMap();
		matrix.util.List partFamily = new StringList();

		DomainObject domFeature = new DomainObject();
		StringList objSelects = new StringList();
		objSelects.addElement(DomainConstants.SELECT_NAME);

		// Relationship and Type pattern for the gerRelatedObject call to get only the Part Family objects
		String strRelPattern = ConfigurationConstants.RELATIONSHIP_GBOM;
		String strTypePattern = ConfigurationConstants.TYPE_PART_FAMILY;

		// relationship Varies By traversal to show the Part Family,
		// if Design Variants are available then show the Part Family Value
		String[] argsTemp = new String[3];
		argsTemp[1] = "from";
		argsTemp[2] = ConfigurationConstants.RELATIONSHIP_VARIES_BY;


		String strListDesignVariants = "";
		String strFeatureId ="";
		// loop through all the records
		for (int i = 0; i < lstobjectList.size(); i++) {
			objectMap = (Map) lstobjectList.get(i);
			strFeatureId = (String) objectMap.get(DomainConstants.SELECT_ID);
			argsTemp[0] = strFeatureId;
			domFeature.setId(strFeatureId);
			String sParentType = domFeature.getInfo(context,
					DomainConstants.SELECT_TYPE);
			// check if the feature is of type TYPE_LOGICAL_STRUCTURES
			if (mxType.isOfParentType(context, sParentType,
					ConfigurationConstants.TYPE_LOGICAL_STRUCTURES)) {
				//to call to emxDomainObject hasRelationship, which will check if there are objects connected
				// to the Feature with Varies By Relationship or not.
				strListDesignVariants = (String)hasRelationship(context,argsTemp);
				// if the technical feature design Variant then proceed.
				if (strListDesignVariants.equalsIgnoreCase("true")) {
					// traverse through GBOM relationship of the Logical Feature
					// and get only the Part Family objects
					MapList mapGBOmPartFamily =   domFeature.getRelatedObjects(context, strRelPattern, strTypePattern,
							objSelects, new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID), false, true, (short) -1,
							DomainConstants.EMPTY_STRING,
							DomainConstants.EMPTY_STRING,0);
					if(mapGBOmPartFamily.size()==0){
						partFamily.add("");
					}else{
						for (int j=0;j<mapGBOmPartFamily.size();j++ ){
							Map mapListObj = (Map) mapGBOmPartFamily.get(j);
							partFamily.add((String)mapListObj.get(DomainConstants.SELECT_NAME));
						}
					}
				}
				else{
					partFamily.add("");
				}
			}else{
				partFamily.add("");
			}
		}
		return partFamily;
	}

	/**
	 * This Method is used to get the range HREF for second Product
	 * in Structure Compare
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the following arguments 0 - HashMap containing the following arguments
	 *               Object Id
	 *               Field Name
	 * @return String  - Range Href for Second Object
	 * @throws Exception if the operation fails
	 */
	public String getProductTwoRangeHref(Context context, String[] args) throws Exception
	{
		String strTypes = EnoviaResourceBundle.getProperty(context,"Configuration.LogicalCompareTypesForObject2.type_Products");
		return "TYPES="+strTypes;
	}

	/**
	 * This Method is used to get the range HREF for second Product
	 * in Structure Compare
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the following arguments 0 - HashMap containing the following arguments
	 *               Object Id
	 *               Field Name
	 * @return String  - Range Href for Second Object
	 * @throws Exception if the operation fails
	 */
	public String getProductVariantTwoRangeHref(Context context, String[] args) throws Exception
	{
		String strTypes = EnoviaResourceBundle.getProperty(context,"Configuration.LogicalCompareTypesForObject2.type_ProductVariant");
		return "TYPES="+strTypes;
	}

	/**
	 * This Method is used to get the range HREF for EBOM
	 * in Structure Compare
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the following arguments 0 - HashMap containing the following arguments
	 *               Object Id
	 *               Field Name
	 * @return String  - Range Href for Second Object
	 * @throws Exception if the operation fails
	 */
	public String getEBOMRangeHref(Context context, String[] args) throws Exception
	{
		String strTypes = EnoviaResourceBundle.getProperty(context,"Configuration.EBOMCompareTypesForObject2.type_All");
		return "TYPES="+strTypes;
	}



	/**
	 * This method is used to Synchronize the EBOM to the product by creating and connecting
	 * a Logical Feature to the context Product
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 	   - Holds the following arguments 0 - HashMap containing the following arguments
	 *                   Object Id
	 *                   Document Element
	 * @return Map
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ConnectionProgramCallable
	public Map synchronizeEbomToProduct(Context context, String[] args)throws Exception {
		HashMap returnMap = new HashMap();
		MapList mlItems = new MapList();
		HashMap doc = new HashMap();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		Element elm = (Element) programMap.get("contextData");
		MapList chgRowsMapList = com.matrixone.apps.framework.ui.UITableIndented.getChangedRowsMapFromElement(context, elm);
		String strContextObjectId = (String) programMap.get("objectId");
		DomainObject domParent = new DomainObject(strContextObjectId);
		String strObjectType = domParent.getInfo(context, SELECT_TYPE);
		String language = context.getSession().getLanguage();
		String strErrorMasterEquipmentFeatureAdd =  EnoviaResourceBundle.getProperty(context, SUITE_KEY,
		"emxProduct.Alert.StructureCompare.CannotSync.Add.MasterEquipmentFeatures",language);
		String strErrorMasterEquipmentFeatureRemove = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
		"emxProduct.Alert.StructureCompare.CannotSync.Remove.MasterEquipmentFeatures",language);
		for (int i = 0; i < chgRowsMapList.size(); i++) {
			HashMap tempMap = (HashMap) chgRowsMapList.get(i);
			String strRelId = (String) tempMap.get("relId");
			String childObjId = (String) tempMap.get("childObjectId");
			String markUpMode = (String) tempMap.get("markup");
			String strRowId = (String) tempMap.get("rowId");
			HashMap columnsMap = (HashMap) tempMap.get("columns");
			DomainObject domFeature = new DomainObject(childObjId);
			String strName = domFeature.getInfo(context,DomainObject.SELECT_NAME);
			if (markUpMode.equalsIgnoreCase("add")) {
				try {
					if(!strObjectType.equalsIgnoreCase(ConfigurationConstants.TYPE_EQUIPMENT_FEATURE)
							&& !strObjectType.equalsIgnoreCase(ConfigurationConstants.TYPE_MASTER_FEATURE)){
						/* get the attribute values from the Part */
						HashMap attributeMap = new HashMap();
						DomainObject dompart = new DomainObject(childObjId);
						String strUsage = (String) dompart.getInfo(context, "to["
								+ ConfigurationConstants.RELATIONSHIP_EBOM
								+ "].attribute["
								+ ConfigurationConstants.ATTRIBUTE_USAGE + "]");
						String strComponentLocation = (String) columnsMap.get("Component Location");
						String strFindNumber = (String) columnsMap.get("Find Number");
						String strQuantity = (String) columnsMap.get("Quantity");
						String strRefrenceDesignator = (String) columnsMap.get("Reference Designator");
						attributeMap.put(ConfigurationConstants.ATTRIBUTE_COMPONENT_LOCATION,strComponentLocation);
						attributeMap.put(ConfigurationConstants.ATTRIBUTE_USAGE,strUsage);
						attributeMap.put(ConfigurationConstants.ATTRIBUTE_QUANTITY,strQuantity);
						attributeMap.put(ConfigurationConstants.ATTRIBUTE_REFERENCE_DESIGNATOR,strRefrenceDesignator);
						attributeMap.put(ConfigurationConstants.ATTRIBUTE_FIND_NUMBER,strFindNumber);
						LogicalFeature lfbean = new LogicalFeature();
						DomainRelationship domRelLogicalFeatures = lfbean.createLogicalFeatureConnectToProduct(context,strContextObjectId);
						domRelLogicalFeatures.setAttributeValues(context, (Map) attributeMap);
						StringList sList1 = new StringList();
						sList1.add(DomainRelationship.SELECT_FROM_ID);
						sList1.add(DomainRelationship.SELECT_TO_ID);
						String strFeatureId = null;
						String[] arr1 = new String[1];
						arr1[0] = domRelLogicalFeatures.toString();
						MapList objMapList1 = DomainRelationship.getInfo(context,arr1, sList1);
						// To get the Logical Feature Id
						for (int j = 0; j < objMapList1.size(); j++) {
							Map objFLMap1 = (Map) objMapList1.get(j);
							strFeatureId = (String) objFLMap1.get(DomainRelationship.SELECT_TO_ID);
						}
						DomainObject dom = new DomainObject(strFeatureId);
						dom.setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME,strName);
						returnMap.put("rowId", strRowId);
						returnMap.put("markup", markUpMode);
						returnMap.put("oid", childObjId);
						mlItems.add(returnMap); // returnMap having all the details
						// abt the changed row.
						doc.put("Action", "success"); // Here the action can be
						// "Success" or "refresh"
						doc.put("changedRows", mlItems);// Adding the key
						// "ChangedRows" which
						// having all the data for
						// changed Rows
					}// End of type check condition for Master/ Equipment Feature
					else {
						doc.put("Message", strErrorMasterEquipmentFeatureAdd);
						doc.put("Action", "ERROR");
					}
				} catch (FrameworkException e) {
					doc.put("Message", e.getMessage());
					doc.put("Action", "ERROR");
				}
			}
			/* Code to remove the selected Feature from the target Product */
			else if (markUpMode.equalsIgnoreCase("cut")) {
				try {
					// Added the condition, to block Sync under Master
					if(!strObjectType.equalsIgnoreCase(ConfigurationConstants.TYPE_MASTER_FEATURE)){

						String strErrorMessage = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
						"emxProduct.Alert.StructureCompare.FeatureConnectedtoPC",language);
						StringList sList = new StringList();
						sList.add(DomainRelationship.SELECT_FROM_ID);

						String[] arr = new String[1];
						arr[0] = strRelId;

						DomainRelationship relObj = new DomainRelationship(strRelId);
						relObj.remove(context);
						//-------------------------------------
						//TODO---------------canRemoveFeature Check
						//-------------------------------------
						Boolean canRemoveFeature = true;//featurebean.synchronizeStructureCutinProduct(context,childObjId,strFeatureListId);
						if (canRemoveFeature) {
							returnMap.put("rowId", strRowId);
							returnMap.put("markup", markUpMode);
							mlItems.add(returnMap); // returnMap having all the
							// details abt the changed row.
							doc.put("Action", "success"); // Here the action can
							// be "Success" or
							// "refresh"
							doc.put("changedRows", mlItems);// Adding the key
							// "ChangedRows" which
							// having all the data
							// for changed Rows
						} else {
							doc.put("Message", strErrorMessage);
							doc.put("Action", "ERROR"); // Here the action can be
							// "Success" or "refresh"
						}
					}// End of type check condition for Master/ Equipment Feature
					else {
						doc.put("Message", strErrorMasterEquipmentFeatureRemove);
						doc.put("Action", "ERROR");
					}

				} catch (Exception e) {
					doc.put("Action", "ERROR"); // Here the action can be
					// "Success" or "refresh"
					doc.put("Message", e.getMessage());
				}
				//-------------------------------------
				//TODO---------------Update Design Variants for both Add & Cut
				//-------------------------------------
			}
		}
		return doc;
	}

	/**
	 * This is a refresh JPO method to refresh the Candidate Configuration Features page once the Commit of the Logical Features is done
	 *
	 * @param context - Matrix context
	 * @param args - contains the Program Map.
	 * @return returnMap - Map consisting of the Javascript code to refresh the Page.
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public Map refreshCandidateFeaturePageOnApply(Context context, String[] args) throws Exception {

		Map returnMap = new HashMap();

		returnMap.put ("Action", "execScript");
		returnMap.put("Message", "{ main:function()  {getTopWindow().close();var listFrame = findFrame(getTopWindow().opener.getTopWindow(),'detailsDisplay');listFrame.location.href=listFrame.location.href;}}");
		return returnMap;
	}

	/**
	 * This is a public method used to commit the Candidate Logical Feature to selected Product Revisions of a Context Model
	 *
	 * @param context - Matrix context
	 * @param args - contains the Maps with Context Model Id, Selected product revisions and the Candidate Logical Features to commit to.
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ConnectionProgramCallable
	public void commitCandidateFeatureToProduct (Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String strProductObjId= (String)programMap.get("parentOID");
		String strModelId  = (String)paramMap.get("objectId");

		Element elem = (Element) programMap.get("contextData");
		MapList chgRowsMapList = com.matrixone.apps.framework.ui.UITableIndented.getChangedRowsMapFromElement(context, elem);

		StringList strLogicalFeatureIds  = new StringList(chgRowsMapList.size());

		for (int i = 0; i < chgRowsMapList.size(); i++) {
			HashMap tempMap = (HashMap) chgRowsMapList.get(i);
			strLogicalFeatureIds.addElement((String) tempMap.get("childObjectId"));
		}
		StringList strProductRevisionIds = new StringList(strProductObjId);

		// call the Model API to commit the selected Logical features to the selected Product Revisions.
		Model confModel = new Model(strModelId);
		confModel.commitCandidateLogicalFeatures(context, strLogicalFeatureIds, strProductRevisionIds);
	}


	/**
	 * connects the newly created logical feature to the Context Object with approriate relationship
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 */
	public void connectLogicalFeatureToContext(Context context, String[] args) throws Exception {

			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap   = (HashMap)programMap.get("paramMap");
			HashMap requestMap   = (HashMap)programMap.get("requestMap");

			String[] strarrayparentOID = (String[])requestMap.get("parentOID");
			if(strarrayparentOID != null){
				String strParentID = strarrayparentOID[0];
				DomainObject parentObj = new DomainObject(strParentID);

				String strParentType = parentObj.getInfo(context, DomainConstants.SELECT_TYPE);
				String newObjID = (String)paramMap.get("objectId");

				if(strParentType != null && !"".equals(strParentType) && mxType.isOfParentType(context,	strParentType,ConfigurationConstants.TYPE_MODEL)){
					Model confModel = new Model(strParentID);
					confModel.connectCandidateLogicalFeature(context, newObjID);
				}
				else if(strParentType != null && !"".equals(strParentType) &&  mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_GENERAL_CLASS)){

					DomainRelationship.connect(context, strParentID, ConfigurationConstants.RELATIONSHIP_CLASSIFIED_ITEM, newObjID,false);
				}
			}
	}
	/**
	 * This is a dummy method to provide the feasibility of adding configuration feature markup under a product to commit, without showing already committed features
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the Logical Feature Structure Details
	 * @throws Exception
	 *             if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTempCommittedLogicalFeatures(Context context, String[] args)
	throws Exception {

		return new MapList();
	}

	/**
	 * This method will return the Active and Inactive Design variants connected to the Context Logical Feature with
	 * Varies By and Inactive Varies By relationship
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getActiveInactiveDesignVariants(Context context, String[] args)
	throws Exception {



		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList mapBusIds = new MapList();
		// getting the context feature Id
		String strObjectID = (String) programMap.get("objectId");
		StringTokenizer st = new StringTokenizer(strObjectID,",");
		st.nextToken();
		String strContextObject = "";
		String strProductID = (String) programMap.get("ProductID");

		if(st.hasMoreTokens()){
			strContextObject = st.nextToken();
		}else if(strContextObject.equals("") && strProductID!=null){
			strContextObject = strProductID;
		}


		String strProdType = (String) new DomainObject(strObjectID).getInfo(context,DomainConstants.SELECT_TYPE);

		String featureID = "";
		String prodID = "";

		if(!mxType.isOfParentType(context,strProdType,ConfigurationConstants.TYPE_PRODUCTS)){
		if((null == strProductID || "null".equals(strProductID)  || "".equals(strProductID))
				|| (mxType.isOfParentType(context,strProdType,ConfigurationConstants.TYPE_LOGICAL_FEATURE))	){
			StringTokenizer objIDs = new StringTokenizer(strObjectID, ",");
			if (objIDs.countTokens() > 1) {
				// Context Feature ID
				featureID = objIDs.nextToken().trim();
				// Context Product ID
				prodID = objIDs.nextToken().trim();

			}else{
				featureID = strObjectID;
				//Added by IXE
				if(programMap.containsKey("parentOID")){
					prodID = (String) programMap.get("parentOID");
				}
			}


			LogicalFeature compFtr = new LogicalFeature(featureID);

			String relWhere = DomainObject.EMPTY_STRING;
			String objWhere = DomainObject.EMPTY_STRING;
			// Obj and Rel pattern
			StringBuffer typePattern = new StringBuffer();
			typePattern.append(ConfigurationConstants.TYPE_CONFIGURATION_FEATURE);

			StringBuffer relPattern = new StringBuffer();
			relPattern.append(ConfigurationConstants.RELATIONSHIP_VARIES_BY);
			relPattern.append(",");
			relPattern.append(ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY);
			// Obj and Rel Selects
			// configuration Feature's Marketing name
			StringList objSelects = new StringList();

			StringList relSelects = new StringList();
			relSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_CONDITION+"]");
			// if the context is product variant get additional selectables to show invalid DVs
			if(ProductLineCommon.isNotNull(prodID)&&(mxType.isOfParentType(context,
					new DomainObject(prodID).getInfo(context,DomainObject.SELECT_TYPE).toString(),
					ConfigurationConstants.TYPE_PRODUCT_VARIANT))){
				relSelects.addElement("tomid["+ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT+"].fromrel.from.id");
			}
			int iLevel = 1;
			String filterExpression = (String) programMap.get("CFFExpressionFilterInput_OID");

			// retrieve Active DV list
			mapBusIds = compFtr.getDesignVariants(context, typePattern.toString(),
					relPattern.toString(), objSelects, relSelects, false, true, iLevel, 0,
					objWhere, relWhere, DomainObject.FILTER_STR_AND_ITEM,
					filterExpression);

			for(Iterator iterator = mapBusIds.iterator(); iterator.hasNext();){
				Map map = (Map) iterator.next();
				map.put("contextProductID", strContextObject);
			}
		}
	}
		return mapBusIds;

	}

	/**
	 * This method is used to get the Design Variant status. This is Status Column Method in the Design Variant Summary page.
	 *
	 * @param context
	 * @param args
	 * @return
	 */
	public List getDesignVariantStatus(Context context, String args[]) throws Exception {
		List statuslist = new StringList();
		StringBuffer sb;
		String strRelName = DomainConstants.EMPTY_STRING;
		String language = context.getSession().getLanguage();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String status = DomainConstants.EMPTY_STRING;
		String strAttVal = DomainConstants.EMPTY_STRING;

		MapList lstObjectIdsList = (MapList) programMap.get("objectList");

		// get the ParentID to determine if the context is Product Variant or not.
		Map paramList = (HashMap) programMap.get("paramList");
		String strparentOID = (String) paramList.get("parentOID");
		//if loop added by ixe
		if(strparentOID.length()==0){
			strparentOID = (String) paramList.get("objectId");
		}
		String featureID = "";
		String prodID = "";
		String strProdType = "";
		StringTokenizer objIDs = new StringTokenizer(strparentOID, ",");
		if (objIDs.countTokens() > 1) {
			// Context Logical Feature ID
			featureID = objIDs.nextToken().trim();
			// Context Product / Product Variant ID
			prodID = objIDs.nextToken().trim();
			if(ProductLineCommon.isNotNull(prodID)){
			strProdType = new DomainObject(prodID).getInfo(context,DomainObject.SELECT_TYPE);
			}
		}else{
			featureID = strparentOID;
			//Added by IXE
			strProdType = new DomainObject(featureID).getInfo(context,DomainObject.SELECT_TYPE);
		}



		for (int i = 0; i < lstObjectIdsList.size(); i++) {
			sb = new StringBuffer(100);
			Map tempMap = (Map) lstObjectIdsList.get(i);
			strRelName = (String) tempMap.get(ConfigurationConstants.SELECT_RELATIONSHIP_NAME);
			if(null!=strRelName && strRelName.equals(ConfigurationConstants.RELATIONSHIP_VARIES_BY)){
				if(strProdType!=null && !strProdType.equals("") && (mxType.isOfParentType(context,
						strProdType,ConfigurationConstants.TYPE_PRODUCT_VARIANT))){
					Object obj = tempMap.get("tomid["+ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT+"].fromrel.from.id");
					if(obj!=null && obj.toString().contains(prodID)){
						strAttVal = "Invalid";
					}else{
						strAttVal = ConfigurationConstants.EFFECTIVITY_STATUS_ACTIVE;
					}
				}else{
					strAttVal = ConfigurationConstants.EFFECTIVITY_STATUS_ACTIVE;
				}

			}else if(null != strRelName && strRelName.equals(ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY)){
				strAttVal = ConfigurationConstants.EFFECTIVITY_STATUS_INACTIVE;
			}
			status = EnoviaResourceBundle.getProperty(context, SUITE_KEY, "emxProduct.DesignVariantStatus."+ strAttVal,language);
			sb = sb.append(status);
			statuslist.add(sb.toString());
		}
		return statuslist;
	}

	/**
	 * This method is used to show the Effectivity Condition Column value in
	 * View Design Variants summary.
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public List getEffectivityCondition(Context context, String args[]) throws Exception {
		List statuslist = new StringList();
		String strRelId = DomainConstants.EMPTY_STRING;

		String language = context.getSession().getLanguage();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String status = DomainConstants.EMPTY_STRING;

		MapList lstObjectIdsList = (MapList) programMap.get("objectList");
		// get the ParentID to determine if the context is Product Variant or not.
		Map paramList = (HashMap) programMap.get("paramList");
		String strparentOID = (String) paramList.get("parentOID");
		String prodID = "";
		String strProdType = "";
		StringTokenizer objIDs = new StringTokenizer(strparentOID, ",");
		if (objIDs.countTokens() > 1) {
			// Context Logical Feature ID
			objIDs.nextToken();
			// Context Product / Product Variant ID
			prodID = objIDs.nextToken().trim();
			if(ProductLineCommon.isNotNull(prodID)){
			strProdType = new DomainObject(prodID).getInfo(context,DomainObject.SELECT_TYPE);
			}
		}

		// Selectable for getting the
		/*String strRelPattern = ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT + "," +
		ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT;

		StringList strSelect = ConfigurationUtil.getBasicObjectSelects(context);
		strSelect.add("attribute["+ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_CONDITION+"]");*/


		for (int i = 0; i < lstObjectIdsList.size(); i++) {
			Map tempMap = (Map) lstObjectIdsList.get(i);
			strRelId = (String) tempMap.get(DomainRelationship.SELECT_ID);
			String strEffCondition = "";
			if(strProdType!=null && !strProdType.equals("")){
				if(mxType.isOfParentType(context,strProdType,ConfigurationConstants.TYPE_PRODUCT_VARIANT)){
					 /*StringBuffer sBufWhereCond = new StringBuffer(200);
					 sBufWhereCond.append("fromrel.from.id==");
					 sBufWhereCond.append(prodID);
					 sBufWhereCond.append(" && ");
					 sBufWhereCond.append(" torel.id==");
					 sBufWhereCond.append(strRelId);

					 MapList mLstVInvalidDVDetails = ProductLineCommon.queryConnection(context,
							 strRelPattern,strSelect , sBufWhereCond.toString());*/

					MapList mLstVInvalidDVDetails = new MapList();
					StringList strSelect = new StringList();
					strSelect.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT +"].id");
					strSelect.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT +"].id");
					strSelect.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT +"].physicalid");
					strSelect.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT +"].physicalid");
					strSelect.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT +"].attribute["+ ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_CONDITION +"]");
					strSelect.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT +"].attribute["+ ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_CONDITION +"]");
					strSelect.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT +"].torel["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].id");
					strSelect.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT +"].torel["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].id");
					 
					DomainObject.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT +"].id");
					DomainObject.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT +"].id");
					DomainObject.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT +"].physicalid");
					DomainObject.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT +"].physicalid");
					DomainObject.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT +"].attribute["+ ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_CONDITION +"]");
					DomainObject.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT +"].attribute["+ ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_CONDITION +"]");
					DomainObject.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT +"].torel["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].id");
					DomainObject.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT +"].torel["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].id");
					 
					DomainObject PVObject = new DomainObject(prodID);
					Map mapOfRelIds       = PVObject.getInfo(context, strSelect);
					
					DomainObject.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT +"].id");
					DomainObject.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT +"].id");
					DomainObject.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT +"].physicalid");
					DomainObject.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT +"].physicalid");
					DomainObject.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT +"].attribute["+ ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_CONDITION +"]");
					DomainObject.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT +"].attribute["+ ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_CONDITION +"]");
					DomainObject.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT +"].torel["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].id");
					DomainObject.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT +"].torel["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].id");
					
					StringList listOfIVCRelIds           = (StringList) mapOfRelIds.get("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT +"].id");
					StringList listOfIVCRelPhysicalIds   = (StringList) mapOfRelIds.get("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT +"].physicalid");
					StringList listOfIVCEffecCondAttrVal = (StringList) mapOfRelIds.get("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT +"].attribute["+ ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_CONDITION +"]");
					StringList listOfIVCVBRelIds         = (StringList) mapOfRelIds.get("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT +"].torel["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].id");
					
					StringList listOfVCRelIds            = (StringList) mapOfRelIds.get("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT +"].id");
					StringList listOfVCRelPhysicalIds    = (StringList) mapOfRelIds.get("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT +"].physicalid");
					StringList listOfVCEffecCondAttrVal  = (StringList) mapOfRelIds.get("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT +"].attribute["+ ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_CONDITION +"]");
					StringList listOfVCVBRelIds          = (StringList) mapOfRelIds.get("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +"].frommid["+ ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT +"].torel["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].id");
					
					if(listOfIVCRelIds != null){
						for(int j = 0; j < listOfIVCRelIds.size(); j++)
						{
							if(strRelId.equals(listOfIVCVBRelIds.get(j))){
							Map mapObject = new HashMap();
							mapObject.put("RelInfo", ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT);
							mapObject.put("attribute["+ ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_CONDITION +"]", listOfIVCEffecCondAttrVal.get(j));
							mapObject.put("physicalid", listOfIVCRelPhysicalIds.get(j));
							mapObject.put("name", ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT);
							mapObject.put("id", listOfIVCRelIds.get(j));
							mapObject.put("type", ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT);
							
							mLstVInvalidDVDetails.add(mapObject);
							}
						}
					}
					
					if(listOfVCRelIds != null){
						for(int j = 0; j < listOfVCRelIds.size(); j++)
						{
							if(strRelId.equals(listOfVCVBRelIds.get(j))){
							Map mapObject = new HashMap();
							mapObject.put("RelInfo", ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT);
							mapObject.put("attribute["+ ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_CONDITION +"]", listOfVCEffecCondAttrVal.get(j));
							mapObject.put("physicalid", listOfVCRelPhysicalIds.get(j));
							mapObject.put("name", ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT);
							mapObject.put("id", listOfVCRelIds.get(j));
							mapObject.put("type", ConfigurationConstants.RELATIONSHIP_VALID_CONTEXT);
							
							mLstVInvalidDVDetails.add(mapObject);
							}
						}
					}
					
					
					 if(mLstVInvalidDVDetails.size()>0){
						 for(int j=0; j<mLstVInvalidDVDetails.size();j++){
							 HashMap hMapVIVContext = (HashMap)mLstVInvalidDVDetails.get(j);
							 strEffCondition = (String)hMapVIVContext.get("attribute["+ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_CONDITION+"]");
							 status = EnoviaResourceBundle.getProperty(context, SUITE_KEY, "emxConfiguration.EffectivityCondition."+ strEffCondition,language);
						 }
					 }else{
						strEffCondition = (String) tempMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_CONDITION+"]");
						if(strEffCondition!=null && !strEffCondition.equals("")){
							status = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxConfiguration.EffectivityCondition."+ strEffCondition,language);
						}else{
							status = EnoviaResourceBundle.getProperty(context, SUITE_KEY, "emxConfiguration.EffectivityCondition.System",language);
						}
					 }
				}
				else{
					strEffCondition = (String) tempMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_CONDITION+"]");
					if(strEffCondition!=null && !strEffCondition.equals("")){
						status = EnoviaResourceBundle.getProperty(context, SUITE_KEY, "emxConfiguration.EffectivityCondition."+ strEffCondition,language);
					}else{
						status = EnoviaResourceBundle.getProperty(context, SUITE_KEY, "emxConfiguration.EffectivityCondition.System",language);
					}
				}
			}
			statuslist.add(status);
		}
		return statuslist;
	}


	/**
	 * This method is used to show the Effectivity Condition Column value in
	 * View Design Variants summary.
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public List getNonContextEffectivityCondition(Context context, String args[]) throws Exception {
		List statuslist = new StringList();
		
		String language = context.getSession().getLanguage();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String status = DomainConstants.EMPTY_STRING;

		MapList lstObjectIdsList = (MapList) programMap.get("objectList");
		
		for (int i = 0; i < lstObjectIdsList.size(); i++) {
			Map tempMap = (Map) lstObjectIdsList.get(i);
			String strEffCondition = "";
			strEffCondition = (String) tempMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_CONDITION+"]");
			if(strEffCondition!=null && !strEffCondition.equals("")){
				status = EnoviaResourceBundle.getProperty(context, SUITE_KEY, "emxConfiguration.EffectivityCondition."+ strEffCondition,language);
			}else{
				status = EnoviaResourceBundle.getProperty(context, SUITE_KEY, "emxConfiguration.EffectivityCondition.System",language);
			}
			statuslist.add(status);
		}
		return statuslist;
	}


	/**
	 * This method is used to exclude already connected Design Variants to the context logical feature.
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeDesignVariant(Context context, String[] args) throws Exception
	{
		MapList mapAllDV = getActiveInactiveDesignVariants(context, args);
		StringList strExcludeLst = new StringList(mapAllDV.size());
		for(int i=0;i<mapAllDV.size();i++){
			Map tempMap = (Map) mapAllDV.get(i);
			strExcludeLst.add(tempMap.get(ConfigurationConstants.SELECT_ID).toString());
		}
		return strExcludeLst;
	}



	/**
	 * This method is used to control the access levels for product variant
	 *
	 * @param context
	 *            The ematrix context object
	 * @param String
	 *            The args
	 * @return Boolean
	 */
	public static boolean isNotPVAndNotLeafLevel(Context context, String[] args)
	throws Exception {
		String featureID = "";
		String objProductID = "";
		DomainObject domObjProd = null;

		boolean bFieldAccess = false;
		HashMap requestMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) requestMap.get("objectId");
		StringTokenizer objIDs = new StringTokenizer(objectId, ",");

		if(objIDs.countTokens()>1){
		// Context Feature ID
		featureID = objIDs.nextToken().trim();
		// Context Product ID
		objProductID = objIDs.nextToken().trim();
		}else{
			featureID=objIDs.nextToken().trim();
		}

		if(!objProductID.equals("")){
		domObjProd = new DomainObject(objProductID);
		// getting the type of the product
		String strProdType = domObjProd.getInfo(context,DomainConstants.SELECT_TYPE);
		if(!strProdType.equalsIgnoreCase(ConfigurationConstants.TYPE_PRODUCT_VARIANT)){
			bFieldAccess = true;
		}
		if(bFieldAccess){
			String strArgs[] = new String[3];
			strArgs[0] = featureID;
			strArgs[1] = "from";
			strArgs[2] = ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES;
			String strResult = ConfigurationUtil.hasRelationship(context, strArgs);
			if(strResult!=null && strResult.equalsIgnoreCase("false")){
				bFieldAccess = true;
			}else{
				bFieldAccess = false;
			}
		}
		}
		return bFieldAccess;
	}

	/**
	 * This method is used to control the access command FTRMakeDesignVariantValidInvalid
	 *
	 * @param context
	 *            The ematrix context object
	 * @param String
	 *            The args
	 * @return Boolean
	 */
    public static Boolean isProductVariant(Context context, String[] args)
            throws Exception {

		String objProductID = "";
		
		boolean bFieldAccess = false;
		HashMap requestMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) requestMap.get("objectId");
		if(objectId!=null && !objectId.trim().equals("")){
			StringTokenizer objIDs = new StringTokenizer(objectId, ",");
			if(objIDs.countTokens()>1){
				// Context Feature ID
				objIDs.nextToken();
				// Context Product ID
				objProductID = objIDs.nextToken().trim();
			}else{
				objProductID=objIDs.nextToken().trim();
			}
			// getting the type of the product
			String strProdType = (String) new DomainObject(objProductID).getInfo(context,DomainConstants.SELECT_TYPE);
			if((mxType.isOfParentType(context,strProdType,ConfigurationConstants.TYPE_PRODUCT_VARIANT))){
				bFieldAccess = true;
			}else{
				bFieldAccess = false;
			}
		}
    	return bFieldAccess;
    }

    /**
	 * This method is used to control the access on command FTRAddExistingDesignVariantCommand
	 *
	 * @param context
	 *            The ematrix context object
	 * @param String
	 *            The args
	 * @return Boolean
	 */
    
    public static Boolean isValidContextForDV(Context context, String[] args)
            throws Exception {

		String objProductID = "";
		
		boolean bFieldAccess = false;
		HashMap requestMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) requestMap.get("objectId");
		if(objectId!=null && !objectId.trim().equals("")){
			StringTokenizer objIDs = new StringTokenizer(objectId, ",");
			if(objIDs.countTokens()>1){
				// Context Feature ID
				objIDs.nextToken();
				// Context Product ID
				objProductID = objIDs.nextToken().trim();
			}else{
				objProductID=objIDs.nextToken().trim();
			}
			// getting the type of the product
			String strProdType = (String) new DomainObject(objProductID).getInfo(context,DomainConstants.SELECT_TYPE);
			if((mxType.isOfParentType(context,strProdType,ConfigurationConstants.TYPE_PRODUCT_VARIANT)) || strProdType.equalsIgnoreCase("Manufacturing Feature")){
				bFieldAccess = false;
			}else{
				bFieldAccess = true;
			}
		}
    	return bFieldAccess;
    }
    
    
		/**
	 * This method is used to control the access on command FTRMakeDesignVariantActiveInactive
	 *
	 * @param context
	 *            The ematrix context object
	 * @param String
	 *            The args
	 * @return Boolean
	 */
    public static Boolean isNotProductVariant(Context context, String[] args)
    	throws Exception {
    	boolean bFieldAccess = isProductVariant(context,args);
    	boolean bCommandAccess = false;
    	if(!bFieldAccess){
    		bCommandAccess = true;
    	}
    	return bCommandAccess;
    }



	/**
	 * This is a trigger method to add the interface on Business Object.
	 *
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args - Holds the following arguments 0 - Object ID, 1 - Interface Names to be added
	 * @return iReturn - 0 - if operation is successful
	 * 					1 - if operation fails.
	 * @throws Exception
	 */
	public int rollupDesignVariantsOnAddLogicalFeatures(Context context, String args[])
	throws Exception {

		int iReturn = 0;
		String strChildLFId = args[0];
		String strParentLFId = args[1];
		try {
			String ignoreDVCopyForVersion = PropertyUtil.getGlobalRPEValue(context,"IgnoreDVCopyForVersion");
			if(!(ignoreDVCopyForVersion != null && "TRUE".equals(ignoreDVCopyForVersion))){
			LogicalFeature comFtr = new LogicalFeature(strChildLFId);
			comFtr.triggerRollupDVsToParentOnAddLF(context,strParentLFId);
			}
		} catch (Exception e) {
			iReturn = 1;
			throw new FrameworkException(e.getMessage());
		}
		return iReturn;
	}

	/**
	 * This is a trigger method to add the interface on Business Object.
	 *
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args - Holds the following arguments 0 - Object ID, 1 - Interface Names to be added
	 * @return iReturn - 0 - if operation is successful
	 * 					1 - if operation fails.
	 * @throws Exception
	 */
	public int rollupDesignVariantsOnDeleteLogicalFeatures(Context context, String args[])
	throws Exception {
		int iReturn = 0;
		String strChildLFId = args[0];
		String strParentLFId = args[1];
		// get all the sub features of the Logical Feature being removed.
		LogicalFeature logicalFeature = new LogicalFeature(strChildLFId);
		MapList mapLogicalStructure = (MapList)logicalFeature.getLogicalFeatureStructure(context,"", null, null, null, false,
				true,0,0,"",DomainObject.EMPTY_STRING, DomainObject.FILTER_STR_AND_ITEM,"");
		StringList sLstLFIds = new StringList();
		sLstLFIds.add(strChildLFId);
		for(int i=0; i<mapLogicalStructure.size();i++){
			Map mapLF = (Map)mapLogicalStructure.get(i);
			sLstLFIds.add(mapLF.get(DomainObject.SELECT_ID).toString());
		}
		PropertyUtil.setGlobalRPEValue(context,"LF_REMOVED_ID",ConfigurationUtil.convertStringListToString(context, sLstLFIds));
	    String  LFsRelIDToDisconnect =  MqlUtil.mqlCommand(context,"get env $1","RELID");
		StringList relInfoList = new StringList();
		relInfoList.addElement("tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id");
		DomainRelationship domRel = new DomainRelationship(LFsRelIDToDisconnect);
		Map relInfoMap = domRel.getRelationshipData(context, relInfoList);
		StringList pflFromIDOfRelToDisconnected  = (StringList)relInfoMap.get("tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id");
        System.out.println("pflFromIDOfRelToDisconnected--"+pflFromIDOfRelToDisconnected);
        PropertyUtil.setGlobalRPEValue(context,"PLF_FROM_IDS",ConfigurationUtil.convertStringListToString(context, pflFromIDOfRelToDisconnected));
			
		try {
			LogicalFeature comFtr = new LogicalFeature(strChildLFId);
			comFtr.triggerRollupDVsToParentOnDeleteLF(context,strParentLFId);
			ProductConfiguration.deltaUpdateBOMXMLAttributeOnPC(context,
					strParentLFId+"|"+strChildLFId, "RemoveFeatureUpdate");
		} catch (Exception e) {
			iReturn = 1;
			throw new FrameworkException(e.getMessage());
		}
		return iReturn;
	}

	/**
	 * This is a Access Function to show the Committed To column in the Committed Logical Features view in the Model Context.
	 *
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args Holds ParamMap having the key "fromContext" which will determine if the columns need to be displayed or not
	 * @return bResult - true - if the context is Committed Features
	 * 					 false - if the Context is not Committed Features
	 * @throws Exception  - throws Exception if anyoperation fails.
	 */

	public boolean isCommittedToAcceessible (Context context, String args[]) throws Exception{
		boolean bResult = false;

        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        String strFromContext = (String)paramMap.get("fromContext");
        if(strFromContext!=null && !strFromContext.equals("")){
        	if(strFromContext.equalsIgnoreCase("Committed")){
        		bResult = true;
        	}
        }
		return bResult;
	}

	/**
	 * This is a "Committed To" column JPO function to show the Product Revisions to which the Logical Feature is Committed To.
	 *
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args - contain the ParamMap with the ObjectList of all the Committed Logical Features
	 * @return committedProd - StringList of the Product Revision with Name and Revsisions of the Committed Logical Features.
	 * @throws Exception - throws Exception if anyoperation fails.
	 */
	public StringList getCommittedToProductList(Context context, String[] args)
    throws Exception
    {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList lstObjectIdsList = (MapList)programMap.get("objectList");
		HashMap paramMap = (HashMap)programMap.get("paramList");
		String suiteDir = (String) paramMap.get("SuiteDirectory");
        String suiteKey = (String) paramMap.get("suiteKey");
		StringList committedProd = new StringList();
		String reportFormat = (String)paramMap.get("reportFormat");

		String strProductName = "";
		String strProductRev = "";
		String strProductID = "";
		for (int i = 0 ;i <lstObjectIdsList.size() ;i++ )
	    {
			String output = "";
			Map map = (Map)lstObjectIdsList.get(i) ;
            Object objPRIds = (Object)map.get("frommid["+ ConfigurationConstants.RELATIONSHIP_COMMITTED_CONTEXT+ "].to.id");

			if(objPRIds instanceof StringList){
				//committedProd.addAll((StringList) objPRIds);
				StringList strTempList = new StringList();
				strTempList = (StringList) objPRIds;
				String[] strPRIds = new String[strTempList.size()];
				for(int j=0; j<strTempList.size();j++ ){
					strPRIds[j] = (String) strTempList.get(j);
				}
				MapList mapPRDetails = DomainObject.getInfo(context,strPRIds ,ConfigurationUtil.getBasicObjectSelects(context));

				for(int k=0; k<mapPRDetails.size();k++){
					Map tempMap = (Map)mapPRDetails.get(k);
					strProductName = tempMap.get(DomainObject.SELECT_NAME).toString();
					strProductRev = tempMap.get(DomainObject.SELECT_REVISION).toString();
					strProductID =  tempMap.get(DomainObject.SELECT_ID).toString();
					StringBuffer temp = new StringBuffer();
					if (reportFormat != null && !("null".equalsIgnoreCase(reportFormat)) && reportFormat.length()>0){
						temp.append(XSSUtil.encodeForHTML(context,strProductName+" "+strProductRev));
					}else{
						temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
						temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteDir));
						temp.append("&amp;suiteKey=");
						temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteKey));
						temp.append("&amp;objectId=");
						temp.append(XSSUtil.encodeForHTMLAttribute(context, strProductID));
						temp.append("', '450', '300', 'true', 'popup')\">");
						temp.append(XSSUtil.encodeForHTML(context,strProductName+" "+strProductRev));
						temp.append(" <img border=\"0\" src=\"");
						temp.append("images/iconSmallProduct.gif");
						temp.append("\" /> ");
						temp.append("</a>");
					}

					output = output + "," + temp;
				}
				output = output.replaceFirst(",", "");
				committedProd.add(output);
			}else if (objPRIds instanceof String ){
				DomainObject domProdRev = new DomainObject((String)objPRIds);
				Map mapPRDetails = domProdRev.getInfo(context, ConfigurationUtil.getBasicObjectSelects(context));
				strProductName = mapPRDetails.get(DomainObject.SELECT_NAME).toString();
				strProductRev = mapPRDetails.get(DomainObject.SELECT_REVISION).toString();
				strProductID =  mapPRDetails.get(DomainObject.SELECT_ID).toString();
				StringBuffer temp = new StringBuffer();
				if (reportFormat != null && !("null".equalsIgnoreCase(reportFormat)) && reportFormat.length()>0){
					temp.append(strProductName+" "+strProductRev);
				}else{
					temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
					temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteDir));
					temp.append("&amp;suiteKey=");
					temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteKey));
					temp.append("&amp;objectId=");
					temp.append(XSSUtil.encodeForHTMLAttribute(context, strProductID));
					temp.append("', '450', '300', 'true', 'popup')\">");
					temp.append(XSSUtil.encodeForHTML(context,strProductName+" "+strProductRev));
					temp.append(" <img border=\"0\" src=\"");
					temp.append("images/iconSmallProduct.gif");
					temp.append("\" /> ");
					temp.append("</a>");
				}

				committedProd.add(temp.toString());
			}else if (objPRIds==null ){
				committedProd.add("");
			}
        }

		return committedProd;
    }
	/**
	 * This method will generate the Duplicate Part XML attribute for the
	 * Logical Feature Object
	 *
	 * @param context
	 *            The ematrix context object
	 * @param String
	 *            The args contains the Feature Id
	 */
	public void updateDuplicatePartXML(Context context, String args[])
	throws Exception {
		String strlogicalFTRId = args[0];
		LogicalFeature logicalFTR= new LogicalFeature(strlogicalFTRId);
		logicalFTR.updateDuplicatePartXML(context);
	}

	/**
	 * This method will read the Duplicate Part XML attribute of Context Object,
	 * and create Maplist from the XML, which will be used to rendet DUplicate
	 * part table
	 *
	 * @param context
	 * @param args
	 * @return MapList containing Duplicate Parts
	 * @throws Exception
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAllDuplicatePartsForSelectedFearture(Context context,
			String[] args) throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList mapGBOMPart = new MapList();
		String strFeatureId = (String) programMap.get("objectId");
		LogicalFeature logicalFTR= new LogicalFeature(strFeatureId);
		mapGBOMPart= logicalFTR.getDuplicatePartMaplist(context);
		return mapGBOMPart;
	}
	/**
	 * This is method to show the Logical Features along with the GBOM, used in Logical Feature Option GBOM View Summary
	 *
	 * @param context
	 * @param args
	 * @return mapLogicalStructure - MapList consisting of Logical Features and associated GBOM objects
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getLogicalFeatureOptionGBOMStructure(Context context, String[] args)
	throws Exception {

		MapList mapLogicalStructure =null;
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String strObjectid = (String)programMap.get("objectId");

		int limit = 32767; //Integer.parseInt(FrameworkProperties.getProperty(context,"emxConfiguration.Search.QueryLimit"));
		String strObjWhere = DomainObject.EMPTY_STRING;

		String sNameFilterValue = (String) programMap.get("FTRLogicalFeatureGBOMReportNameFilterCommand");
		String sLimitFilterValue = (String) programMap.get("FTRLogicalFeatureGBOMReportLimitFilterCommand");

		// emxConfiguration.Search.QueryLimit
		if (sLimitFilterValue != null
				&& !(sLimitFilterValue.equalsIgnoreCase("*"))) {
			if (sLimitFilterValue.length() > 0) {
				limit = (short) Integer.parseInt(sLimitFilterValue);
				if (limit < 0) {
					limit = 32767;
				}
			}
		}

			if (sNameFilterValue != null && !(sNameFilterValue.equalsIgnoreCase("*"))) {
				strObjWhere = "name ~~ '" + sNameFilterValue + "'";
			}


		StringBuffer typePattern = new StringBuffer();
		typePattern.append(ConfigurationConstants.TYPE_LOGICAL_STRUCTURES);
		typePattern.append(",");
		typePattern.append(ConfigurationConstants.TYPE_PART);
		typePattern.append(",");
		typePattern.append(ConfigurationConstants.TYPE_PART_FAMILY);
		typePattern.append(",");
		typePattern.append(ConfigurationConstants.TYPE_PRODUCTS);


		StringBuffer relPattern = new StringBuffer();
		relPattern.append(ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES);
		relPattern.append(",");
		relPattern.append(ConfigurationConstants.RELATIONSHIP_GBOM);

		StringList relSelect = new StringList();
		relSelect.add("tomid["+
				ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].from.id");
		relSelect.add("tomid["+ ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].attribute["+
				ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");

		relSelect.addElement(SELECT_FROM_NAME);

		relSelect.add(ConfigurationConstants.SELECT_INTERFACE_EBOM);

		StringList objSelectables = new StringList();
		objSelectables.addElement("attribute["
				+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME
				+ "]");
		objSelectables.addElement(DomainConstants.SELECT_REVISION);
		objSelectables.addElement("attribute["
				+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME
				+ "]");
		objSelectables.addElement("physicalid");
		objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_MANAGED_REVISION+ "]."+DomainObject.SELECT_FROM_NAME);
		objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+ "]."+DomainObject.SELECT_FROM_NAME);
		objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_MANAGED_REVISION+ "]."+DomainObject.SELECT_FROM_ID);
		objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+ "]."+DomainObject.SELECT_FROM_ID);

		int iLevel = ConfigurationUtil.getLevelfromSB(context,args);
		String filterExpression = (String) programMap.get("CFFExpressionFilterInput_OID");



		ConfigurationUtil confUtil = new ConfigurationUtil(strObjectid);
		mapLogicalStructure = confUtil.getObjectStructure(context,typePattern.toString(),relPattern.toString(),
				objSelectables, relSelect,false,true, iLevel,limit,
				strObjWhere, DomainObject.EMPTY_STRING,(short)0,filterExpression);
		for (int i = 0; i < mapLogicalStructure.size(); i++) {
			Map tempMAp = (Map) mapLogicalStructure.get(i);
			if(tempMAp.containsKey("expandMultiLevelsJPO")){
				mapLogicalStructure.remove(i);
				i--;
			}
		}
		if(iLevel==0)
		{
			HashMap hmTemp = new HashMap();
			hmTemp.put("expandMultiLevelsJPO", "true");
			mapLogicalStructure.add(hmTemp);
		}
		return mapLogicalStructure;
	}

	/**
	 * It is a trigger method. Fires when a instance of "Logical Features" is created.
	 * It is used to create connection between "Logical Features" instance and Products
	 * with Product Feature List
	 * @param context
	 *            The ematrix context object.
	 * @param String[]
	 *            The args .
	 * @return List Value containing newly created relationship id and from id.
	 */
	public void pflconnection(Context context, String[] args)throws Exception
	{
        String strFATAttr = PropertyUtil.getGlobalRPEValue(context,"attribute_FeatureAllocationType");
        String strPFLFATAttribute = "";
        if(strFATAttr!=null & !strFATAttr.equals("")){
        	strPFLFATAttribute = strFATAttr;
        }
		String relid = args[0];
		String fromobjectid = args[1];
		String toobjectid = args[2];


		StringList typeattributeList = new StringList();
		StringList relattributeList = new StringList();
		HashSet productIds = new HashSet();
		HashSet logicalFeaturesidhs = new HashSet();
		StringList productIdlist = new StringList();
		StringList logicalFeaturesidList = new StringList();
		boolean fromtypeProduct;
		ProductLineCommon connectPFL = new ProductLineCommon(relid);
		RelationshipType relationtype = new RelationshipType(ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST);

		try{

			DomainObject domLogicalFeature = new DomainObject(fromobjectid);
			fromtypeProduct = domLogicalFeature.isKindOf(context, ConfigurationConstants.TYPE_PRODUCTS);

			if(fromtypeProduct)//If from type object is TYPE Products.
			{
				productIds.add(fromobjectid);
			}

			//Connect the ALL Products to newly created relationship with PFL
			//Need to get the Product Revision IDs
			//Relationship Pattern
			StringBuffer relPattern = new StringBuffer();
			relPattern.append(ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES);
			//Type Pattern
			StringBuffer typePattern = new StringBuffer();
			typePattern.append(ConfigurationConstants.TYPE_PRODUCTS);
			typePattern.append( ",");
			typePattern.append(ConfigurationConstants.TYPE_LOGICAL_FEATURE);
			//Selectables on relationship
			relattributeList.addElement(DomainConstants.SELECT_FROM_TYPE);
			relattributeList.addElement(DomainConstants.SELECT_FROM_ID);
			relattributeList.addElement(DomainConstants.SELECT_ID);
			String strWhere = null;
			int level = -1;//recurse to END
			short filterFlag = 0;
			//Get all Products Ids, traversing to Upward
			ConfigurationUtil confgiUtil = new ConfigurationUtil(fromobjectid);
			MapList mapListStruc = confgiUtil.getObjectStructure(context,typePattern.toString(),relPattern.toString(),
					typeattributeList, relattributeList,true, false, level,0,null, strWhere,filterFlag, null);

			//Adding all product ids in HashMap to avoid duplicates
			for(int i= 0; i<mapListStruc.size(); i++)
			{
				Map mp = (Map) mapListStruc.get(i);
				String strProductID = (String)mp.get(DomainConstants.SELECT_FROM_ID);
				DomainObject domObject = new DomainObject(strProductID);
				if(domObject.isKindOf(context, ConfigurationConstants.TYPE_PRODUCTS))
				productIds.add(strProductID);
			}
			Iterator iterator = productIds.iterator();
			while(iterator.hasNext())
			{
				productIdlist.addElement((String)iterator.next());
			}
			String[] productIdsArray = (String[]) productIdlist.toArray(new String[0]);
			//Connecting the ProductIds with Logical Features relationship with Relationship PFL
			Map mapPFLDetails = connectPFL.connectObjects(context,relationtype,productIdsArray, true,false);

			if(!strPFLFATAttribute.equals("")){

				DomainRelationship domPFLRel = new DomainRelationship(mapPFLDetails.get(fromobjectid).toString());
				domPFLRel.setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE,strPFLFATAttribute);
			}

			/*Check Connected Logical Feature contain sub Features
					   If Yes, Connect the sub Features relationships with Product Revision with PFL*/

			//Type pattern only Logical Feature
			String strtypepattern = ConfigurationConstants.TYPE_LOGICAL_FEATURE;
			//Rel pattern only Logical Features
			String strrelpattern = ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES;
			//Get all Logical Features Ids which are connected to newly added Logical Feature, traversing to Downwards
			if(fromtypeProduct)//If from type object is TYPE Products.
			{
				fromobjectid = toobjectid;
			}
			MapList mapLogicStruc = confgiUtil.getObjectStructure(context, strtypepattern,strrelpattern,
					typeattributeList, relattributeList,false, true, 0,0,null, strWhere, filterFlag, null);
			//Adding all Logical Features ids in HashMap to avoid duplicates
			for(int i= 0; i<mapLogicStruc.size(); i++)
			{
				Map mp = (Map) mapLogicStruc.get(i);
				String logicalFeaturesid = (String) mp.get(DomainConstants.SELECT_ID);
				logicalFeaturesidhs.add(logicalFeaturesid);
			}

			Iterator iter = logicalFeaturesidhs.iterator();
			while(iter.hasNext())
			{
				logicalFeaturesidList.addElement((String)iter.next());
			}

			String[] logicalFeaturesidArry = (String[]) logicalFeaturesidList.toArray(new String[0]);
			StringList selectables = new StringList();
			selectables.addElement("tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+
					"]."+DomainConstants.SELECT_FROM_ID);
			DomainRelationship domainrelation = new DomainRelationship();
			//Getting all connected Product ids in mplist
			MapList mplist = domainrelation.getInfo(context, logicalFeaturesidArry, selectables);
			DomainRelationship domPFLRel = null;
			/* productIdlist contain all product ids
			 * In MapList mplist, in the variable listids contain connected product ids
			 * Comparing the list, and unconnected Products are connected with PFL
			 */
			for(int i= 0; i<logicalFeaturesidList.size(); i++)
			{
				Object objectlistids;
				StringList listids = new StringList();

				String logicalFeaturesid = (String)logicalFeaturesidList.get(i);
				Map newmap = (Map) mplist.get(i);
				objectlistids = (Object)newmap.get("tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+
						"]."+DomainConstants.SELECT_FROM_ID);
				if(objectlistids instanceof String){
					listids.add(objectlistids.toString());

				}else if(objectlistids instanceof StringList){
					listids= (StringList)objectlistids;
				}

				for(int j=0; j<productIdlist.size();j++)
				{
					String productid = (String)productIdlist.get(j);
					if(!listids.contains(productid))
					{
						ProductLineCommon connectPFLallproducts = new ProductLineCommon(logicalFeaturesid);
						String strConnection = connectPFLallproducts.connectObject(context,relationtype,productid, true);
						if(!strPFLFATAttribute.equals("")){
							domPFLRel = new DomainRelationship(strConnection);
							domPFLRel.setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE,strPFLFATAttribute);
						}
					}
				}
			}
		}

		catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

	}


	/**
	 * This is a Access Function to show or hide the Column LogicalSelectionType
	 *  depends upon the param context=Logical passed in request parameter
	 *
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args Holds ParamMap having the key "context", value of which will determine if the columns need to be displayed or not
	 * @return bResult - true - if the context is Committed Features
	 * 					 false - if the Context is not Committed Features
	 * @throws Exception  - throws Exception if any operation fails.
	 */
	public Object isLogicalFeatureContext(Context context, String[] args)
    throws Exception
    {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestValuesMap = (HashMap)programMap.get("RequestValuesMap");
		String[] ctx = null;
		if(requestValuesMap!=null){
			ctx = (String[])requestValuesMap.get("context");
		}
		else{
			ctx = new String[]{programMap.get("context").toString()};
		}

		if(ctx!= null && ctx[0] != null && ctx[0].equals("LogicalFeature")){
			return Boolean.valueOf(true);
		}else{
			return Boolean.valueOf(false);
		}
    }

	/**
	 * It is a trigger method. Fires when a instance of "Logical Features" is Deleted.
	 * It check whether Master Feature is connected to more than one Logical Feature, If it is no
	 * than Master Feature is deleted.
	 * @param context
	 *            The ematrix context object.
	 * @param String[]
	 *            The args.
	 * @return void
	 */
	public void deleteConnectedMasterFeature (Context context, String[] args)
    throws Exception
    {

		try{
			String objectid = args[0];
			int connectedlogicalfeature=0;
			String masterFeatureID = DomainObject.EMPTY_STRING;
			ContextUtil.pushContext(context, PropertyUtil
                    .getSchemaProperty(context, "person_UserAgent"),
                    DomainConstants.EMPTY_STRING,
                    DomainConstants.EMPTY_STRING);
			DomainObject domainobject = new DomainObject(objectid);


			//Type Pattern
			StringBuffer strTypePattern = new StringBuffer();
			strTypePattern.append(ConfigurationConstants.TYPE_LOGICAL_STRUCTURES);
			strTypePattern.append(",");
			strTypePattern.append(ConfigurationConstants.TYPE_MASTER_FEATURE);
			//Relationship Pattern
			StringBuffer strRelPattern = new StringBuffer();
			strRelPattern.append(ConfigurationConstants.RELATIONSHIP_MANAGED_REVISION);
			//Selectables
			StringList slObjSelects = new StringList();
			slObjSelects.add(DomainObject.SELECT_ID);
			StringList slRelSelects = new StringList();
			slRelSelects.add(DomainRelationship.SELECT_ID);

	        ConfigurationUtil utilObj = new ConfigurationUtil(objectid);

	        MapList mapList = utilObj.getObjectStructure(context, strTypePattern.toString(), strRelPattern.toString(),
	        		slObjSelects, slRelSelects, true,true, 2, 0, DomainObject.EMPTY_STRING, DomainObject.EMPTY_STRING,
	        		(short)0, DomainObject.EMPTY_STRING);

	        for (int i=0;i<mapList.size();i++)
	        {
	        	Map map = (Map) mapList.get(i);
	        	String type = (String) map.get(DomainObject.SELECT_TYPE);
	        	if (domainobject.isKindOf(context, type))
	        			{
	        			connectedlogicalfeature++;
	        			}
	        	else
	        	{
	        		masterFeatureID=(String)map.get(DomainObject.SELECT_ID);
	        	}
	        }
	        //if more than one Logical Feature is connected to Master Feature, than Master Feature is not deleted
        	if (connectedlogicalfeature == 1)
        	{
        		DomainObject dombject = new DomainObject(masterFeatureID);
        		//Deleting Master Feature
        		if(masterFeatureID!=null)
				dombject.deleteObject(context);
        	}
			ContextUtil.popContext(context);
		}
		catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

    }
	/**
	 * It is used for get Logical Feature in the context of DB chooser while creating Product Variant
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public StringList getLogicalFeatureForDB(Context context,String[] args)throws Exception
  	{
  		StringList managedRevisionsIdList = new StringList();
  		HashMap programMap = (HashMap) JPO.unpackArgs(args);
  		String strlogicalFeatureId = (String)  programMap.get("logicalFeatureId");
  		
  		try
  		{
  			DomainObject domObj = new DomainObject(strlogicalFeatureId);
  			StringList objSelects = new StringList();
  			objSelects.addElement(DomainConstants.SELECT_ID);
  			StringList relSelects = new StringList();
  			relSelects.add(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
  			StringBuffer stbRelName = new StringBuffer(50);
  	        StringBuffer stbTypeName = new StringBuffer(50);
  	        stbRelName.append(ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES);
            stbTypeName.append(ConfigurationConstants.TYPE_PRODUCTS);
            stbTypeName.append(",");
            stbTypeName.append(ConfigurationConstants.TYPE_LOGICAL_FEATURE);

             // StringList strProductStructure = new DomainObject(strProductId).getInfoList(context,"from["+ManufacturingPlanConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].to.from["+ManufacturingPlanConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.id");
              MapList mapList = domObj.getRelatedObjects(context, stbRelName.toString(), stbTypeName.toString(), objSelects, relSelects, false, true, (short)1, ConfigurationConstants.EMPTY_STRING, ConfigurationConstants.EMPTY_STRING, 0, null, null, null);

              Map tempMap = new HashMap();

  			String strId = "";
  	        for(int i=0;i<mapList.size();i++)
  	          {
  	          	tempMap = (Map)mapList.get(i);
  	          	strId = (String)tempMap.get(DomainConstants.SELECT_ID);
  	          	managedRevisionsIdList.addElement(strId);
  	          }


  		}
  		catch (Exception e) {
  			// TODO: handle exception
  			e.printStackTrace();
  		}

  		return managedRevisionsIdList;
  	}

    /**
     * This method returns List of Equipment and parts related to an equipment,
     * in Context with Product revision
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public MapList getEquipmentList(Context context, String[] args) throws Exception {

        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList mapListReport = new MapList();
        String objectId = (String) paramMap.get("objectId");
        String XML_EQUIPMENT_FEATURE = "EquipmentFeature";
        String XML_PART = "Part";
        String XML_EMPTY = "";

        try{
        	DomainObject domObject = new DomainObject(objectId);
        	String strType = domObject.getInfo(context, DomainObject.SELECT_TYPE);

        	//TODO- need to check - if(strType.equals(ConfigurationConstants.TYPE_EQUIPMENT_FEATURE)){}
        	if(mxType.isOfParentType(context, strType,ConfigurationConstants.TYPE_PRODUCTS)){

        		String eqpXML = domObject.getAttributeValue(context,
        				ConfigurationConstants.ATTRIBUTE_EQUIPMENT_LIST_REPORT_XML);

        		if(!(eqpXML.equals(XML_EMPTY) || eqpXML == null)){
        			SAXBuilder saxb = new SAXBuilder();
        			saxb.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
					saxb.setFeature("http://xml.org/sax/features/external-general-entities", false);
					saxb.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        			Document document = saxb.build(new StringReader(eqpXML));
        			Element root = document.getRootElement();

        			List listFeature = (List) root.getChildren(XML_EQUIPMENT_FEATURE);
        			Element elementFeature = null;
        			String fId = "";
        			String fType = "";
        			String fName = "";
        			String fRevision = "";
        			String connectionId = "";
        			List listParts = null;

        			StringList partSelects = new StringList();
        			partSelects.add(ConfigurationConstants.SELECT_DESCRIPTION);
        			partSelects.add(ConfigurationConstants.SELECT_CURRENT);
        			partSelects.add(ConfigurationConstants.SELECT_POLICY);
        			for (int ix = 0; ix < listFeature.size(); ix++) {

        				elementFeature = (Element) listFeature.get(ix);
        				fId = elementFeature.getAttributeValue(DomainConstants.SELECT_ID);
        				fType = elementFeature.getAttributeValue(DomainConstants.SELECT_TYPE);
        				fName = elementFeature.getAttributeValue(DomainConstants.SELECT_NAME);
        				fRevision = elementFeature.getAttributeValue(DomainConstants.SELECT_REVISION);
        				connectionId = elementFeature.getAttributeValue("logicalFeatureRelID");

        				HashMap hmTemp = null;

        				listParts = (List) elementFeature.getChildren(XML_PART);
        				//Iterate on Part XML
        				int partSize = listParts.size();
        				if(partSize>=0){
        					DomainObject partObj = null;
        					Element elementPart = null;
        					String pId = "";
        					String pName = "";
        					String pRevision = "";
        					String strGBOMId = "";
        					String strIRuleId = "";
        					String strLeftExp = "";
        					String strRightExp = "";

        					Map partMap = null;
        					for(int i=0; i<partSize; i++){
        						hmTemp = new HashMap();
        						hmTemp.put(DomainConstants.SELECT_ID,fId);
        						hmTemp.put(DomainConstants.SELECT_TYPE,fType);
        						hmTemp.put(DomainConstants.SELECT_NAME,fName);
        						hmTemp.put(DomainConstants.SELECT_REVISION,fRevision);
        						//set Logical Features relationship as connection ID- this will be used in column rendering
        						hmTemp.put(DomainConstants.SELECT_RELATIONSHIP_ID,connectionId);
        						hmTemp.put("relationship",ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES);


        						elementPart = (Element) listParts.get(i);
        						pId = elementPart.getAttributeValue(DomainConstants.SELECT_ID);
        						pName = elementPart.getAttributeValue(DomainConstants.SELECT_NAME);
        						pRevision = elementPart.getAttributeValue(DomainConstants.SELECT_REVISION);
        						strGBOMId = elementPart.getAttributeValue("GBOMRELID");
        						strIRuleId = elementPart.getAttributeValue("iRuleID");
        						strLeftExp = elementPart.getAttributeValue("LeftExpr");
        						strRightExp = elementPart.getAttributeValue("RightExp");

        						partObj = new DomainObject(pId);
        						partMap = partObj.getInfo(context, partSelects);
        						String state= partMap.get(ConfigurationConstants.SELECT_CURRENT).toString();
        						String policy= partMap.get(ConfigurationConstants.SELECT_POLICY).toString();
        						policy=policy.trim().replace(" ", "_");
        						state=state.trim().replace(" ", "_");

        						hmTemp.put("GBOMPartNumber",pName);
        						hmTemp.put("PartDescription",partMap.get(ConfigurationConstants.SELECT_DESCRIPTION));
        						hmTemp.put("PartRevision",pRevision);
        						hmTemp.put("PartState",policy+"."+state);
        						hmTemp.put("PartID",pId);
        						hmTemp.put("GBOMRELID",strGBOMId);
        						hmTemp.put("tomid["
        								+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
        								+ "].from.id",strIRuleId);
        						hmTemp.put("tomid["
        								+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
        								+ "].from.attribute["
        								+ ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION
        								+ "]",strLeftExp);
        						hmTemp.put("tomid["
        								+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
        								+ "].from.attribute["
        								+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION
        								+ "]",strRightExp);
        						//TODO- check for ENG Change column
        						//hmTemp.put("EngineeringChange","");
        						mapListReport.add(hmTemp);
        					}
        				}
        			}
        		}
        	}
        }catch(Exception e){
        	e.printStackTrace();
        }
      return mapListReport;
    }
    /**
     * This method is used to restrict from displaying the row
     * in Products and Features Property page
     * if Library Central is not installed
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args:
     *          args for the method
     *
     * @throws Exception if the operation fails
     * @return boolean
     */
    public boolean showClassificationPath(Context context,String[] args) throws Exception
    {
    	 boolean isLCInstalled = ConfigurationUtil.isLCInstalled(context);
	     return isLCInstalled;

    }
	 /**
	  *  It is used in Merge/Replace. It is a exclude ID program for a search page.
	  *  Get all Sub Feature and Parent Features of selected Logical Feature and Master Logical Feature and exclude these ids in search window
	  * @param context
	  * @param args contains selected Logical Feature Ids and Master Logical Feature Id
	  * @return StringList list of excluded Ids
	  * @throws Exception
	  */

	 @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	 public StringList searchFeatureforReplace(Context context, String [] args)
	 throws Exception
	 {
		 Map programMap = (Map) JPO.unpackArgs(args);
		 String strSourceObjectId = (String) programMap.get("objectId");
		 StringBuffer strObjectPattern = new StringBuffer();
		 strObjectPattern.append(ConfigurationConstants.TYPE_LOGICAL_STRUCTURES);
		 strObjectPattern.append(",");
		 strObjectPattern.append(ConfigurationConstants.TYPE_PRODUCTS);
		 String strRelPattern = ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES;
		 StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
		 StringList relSelects = new StringList();
		 
		 StringList logicalfeatureList = new StringList();
		 //add the context Feature
		 logicalfeatureList.add(strSourceObjectId);

		 ConfigurationUtil confUtil = new ConfigurationUtil(strSourceObjectId);

		 // USe getLogicalStructure from LF bean to get Logical structure don't use these
		 //for to side object
        MapList objListForSide =  confUtil.getObjectStructure(context,strObjectPattern.toString(),strRelPattern,
       		 objectSelects, relSelects,false, true, 0,0,
        		DomainObject.EMPTY_STRING, DomainObject.EMPTY_STRING,DomainObject.FILTER_ITEM,DomainObject.EMPTY_STRING);

		 for(int i=0;i<objListForSide.size();i++)
		 {
			 Map mapFeatureObj = (Map) objListForSide.get(i);
			 if(mapFeatureObj.containsKey(DomainConstants.SELECT_ID))
			 {
				 Object idsObject = mapFeatureObj.get(DomainConstants.SELECT_ID);
				 if(idsObject instanceof StringList)
				 {
					 logicalfeatureList.addAll((StringList)idsObject);
				 }
				 else
				 {
					 logicalfeatureList.add((String)idsObject);
				 }
			 }
		 }

		 //for from side object
		 MapList objListToSide = confUtil.getObjectStructure(context,strObjectPattern.toString(),strRelPattern,
       		 objectSelects, relSelects,true, false, 0,0,
         		DomainObject.EMPTY_STRING, DomainObject.EMPTY_STRING,DomainObject.FILTER_ITEM,DomainObject.EMPTY_STRING);

		 for(int i=0;i<objListToSide.size();i++)
		 {
			 Map mapFeatureObj = (Map) objListToSide.get(i);
			 if(mapFeatureObj.containsKey(DomainConstants.SELECT_ID))
			 {
				 Object idsObject = mapFeatureObj.get(DomainConstants.SELECT_ID);

				 if(idsObject instanceof StringList)
				 {
					 logicalfeatureList.addAll((StringList)idsObject);
				 }
				 else
				 {
					 logicalfeatureList.add((String)idsObject);
				 }
			 }
		 }

		 objectSelects.add("from["+ ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES +"].to.id");

		 MapList objListSameLevel = confUtil.getObjectStructure(context,strObjectPattern.toString(),strRelPattern,
	       		 objectSelects, relSelects,true, false, 1,0,
	         		DomainObject.EMPTY_STRING, DomainObject.EMPTY_STRING,DomainObject.FILTER_ITEM,DomainObject.EMPTY_STRING);

		 for(int i=0;i<objListSameLevel.size();i++)
		{
			Map mapFeatureObj = (Map) objListSameLevel.get(i);

			if(mapFeatureObj.containsKey("from["+ ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES +"].to.id"))
			{
				Object idsObject = mapFeatureObj.get("from["+ ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES +"].to.id");
				if(idsObject.getClass().toString().contains("StringList"))
				{
					logicalfeatureList.addAll((StringList)idsObject);
				}
				else
				{
					logicalfeatureList.add((String)idsObject);
				}
			}
		}

		 return logicalfeatureList;
	 }
	 /**
	  *  It is used in Merge/Replace. Used to display Product Column of search Window
	  * @param context
	  * @param args contains selected Logical Feature Ids and Master Logical Feature Id
	  * @return Name and Revision of Products/Product Versions
	  * @throws Exception
	  */
	 public List getProductsColumnValue(Context context, String[] args)
	 throws Exception {
		 List retunList = new StringList();
		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
		 MapList objectList = (MapList) programMap.get("objectList");

		 for (int i = 0; i < objectList.size(); i++) {
			 Map mapObject = (Map) objectList.get(i);
			 String strValue = "";
			 String[] tempArgs = JPO.packArgs(mapObject);
			 MapList listProduct = getAllProductsForFeature(context, tempArgs);
			 for (int j = 0; j < listProduct.size(); j++) {
				 Map tempProdMap = (Map) listProduct.get(j);
				 if (!strValue.equals(""))
					 strValue += ", "
						 + (String) tempProdMap
						 .get(ConfigurationConstants.SELECT_NAME);
				 else
					 strValue += (String) tempProdMap
					 .get(ConfigurationConstants.SELECT_NAME);
			 }
			 retunList.add(strValue);
		 }

		 return retunList;
}
	 /**
	  *  It is used in Merge/Replace. Used to display Product Column of search Window. Called by getProductsColumnValue
	  * @param context
	  * @param args contains selected Logical Feature Ids and Master Logical Feature Id
	  * @return Name and Revision of Products/Product Versions
	  * @throws Exception
	  */
	 private MapList getAllProductsForFeature(Context context, String[] args)
     throws Exception {

	 HashMap programMap = (HashMap) JPO.unpackArgs(args);
     String strObjectId = (String) programMap
             .get(ConfigurationConstants.SELECT_ID);
     DomainObject domObject = new DomainObject(strObjectId);
     MapList returnList = new MapList();


    StringList lstProductsname = domObject.getInfoList(context, "to["+ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+
    		 "].tomid[" + ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST + "]."+DomainConstants.SELECT_FROM_NAME);

    StringList lstProductsRev = domObject.getInfoList(context, "to["+ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+
   		 "].tomid[" + ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST + "]."+DomainConstants.SELECT_FROM_REVISION);

     for(int i=0;i<lstProductsname.size();i++)
     {
    	 Map mapProduct = new HashMap();
    	 String strProductName = (String)lstProductsname.get(i);
    	 String strProductRevision = (String)lstProductsRev.get(i);
    	 mapProduct.put(ConfigurationConstants.SELECT_NAME,strProductName + " "+ strProductRevision);
    	 returnList.add(mapProduct);
     }

     return returnList;
	 }
 	 /**
	     * This method used get dynamic columns for split features
	     *
	     * @param context
	     * @param args
	     * @return MapList of columns with their setting
	     * @throws Exception
	     */
	    public MapList getSplitFeatureColumns(Context context, String[] args)
	            throws Exception {

	        int noOfTargetFeatures = 0;
	        HashMap programMap = (HashMap) JPO.unpackArgs(args);
	        HashMap requestMap = (HashMap) programMap.get("requestMap");

	        if (requestMap.get("NumberOfInstances") != null)
	            noOfTargetFeatures = Integer.parseInt(((String) requestMap
	                    .get("NumberOfInstances")).trim());

	        MapList returnList = new MapList(noOfTargetFeatures);
	        Map column = new HashMap();
	        setId((String) requestMap.get("objectId"));
	        Map setting = new HashMap();

	        setting.put("Registered Suite", "Configuration");
	        setting.put("Group Header", "emxProduct.Table.MasterFeature");
	        setting.put("Column Type", "programHTMLOutput");
	        setting.put("Width", "120");
	        setting.put("function", "getSplitFeatureColumnValue");
	        setting.put("program", "LogicalFeature");
	        column.put("settings", setting);
	        column.put("label", getInfo(context, "attribute["
	                + ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME + "]"));
	        column.put("name", getInfo(context, DomainConstants.SELECT_ID));
	        column.put(DomainConstants.SELECT_ID, getInfo(context,
	                DomainConstants.SELECT_ID));

	        returnList.add(column);

	        if (noOfTargetFeatures > 0) {
	            for (int j = 0; j < noOfTargetFeatures; j++) {
	                String featureId = (String) requestMap.get("TargetID" + j);
	                Map columnNewFeature = new HashMap();
	                Map settingNewFeature = new HashMap();
	                setId(featureId);
	                settingNewFeature.put("Registered Suite", "Configuration");
	                setting.put("Width", "120");
	                settingNewFeature.put("Group Header",
	                        "emxProduct.Table.NewFeature");
	                settingNewFeature.put("Column Type", "programHTMLOutput");
	                settingNewFeature.put("function", "getSplitFeatureColumnValue");
	                settingNewFeature.put("program", "LogicalFeature");
	                columnNewFeature.put("settings", settingNewFeature);
	                columnNewFeature.put("label",getInfo(context,"attribute["+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME+ "]"));
	                columnNewFeature.put("name", getInfo(context,
	                        DomainConstants.SELECT_ID));
	                columnNewFeature.put(DomainConstants.SELECT_ID, getInfo(
	                        context, DomainConstants.SELECT_ID));
	                returnList.add(columnNewFeature);
	            }
	        }
	        return returnList;
	    }

	    /**
	     * This method get programHTML output for columns of split features.
	     * @param context
	     * @param args
	     * @throws Exception
	     */
	    public List getSplitFeatureColumnValue(Context context, String[] args)
	            throws Exception {
	        List retunList = new StringList();
	        HashMap programMap = (HashMap) JPO.unpackArgs(args);
	        MapList objectList = (MapList) programMap.get("objectList");
	        HashMap requestMap = (HashMap) programMap.get("paramList");
	        HashMap columnMap = (HashMap) programMap.get("columnMap");
	        Map settings = (Map) columnMap.get("settings");
	        String strGrpHeader = (String) settings.get("Group Header");
	        String strActGrpHeader = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
	                "emxProduct.Table.MasterFeature",context.getSession()
	                        .getLanguage());
	        String noOfSelectedParts = (String) requestMap.get("noOfSelectedParts");
	        StringList selectedPartList = new StringList();
	        if (noOfSelectedParts != null) {
	            int noOfParts = Integer.parseInt(noOfSelectedParts);
	            for (int i = 0; i < noOfParts; i++) {
	                String SelectedPartId = (String) requestMap.get("selectedParts"
	                        + i);
	                selectedPartList.add(SelectedPartId);
	            }
	        }
	        try {
	            for (int i = 0; i < objectList.size(); i++) {
	                Map part = (Map) objectList.get(i);
	                String strId = (part.get(DomainConstants.SELECT_ID)).toString()
	                        + "|"
	                        + (columnMap.get(DomainConstants.SELECT_NAME))
	                                .toString();
	                String strValue = (part.get(DomainConstants.SELECT_ID))
	                        .toString()
	                        + "|"
	                        + (part.get(DomainConstants.SELECT_RELATIONSHIP_ID))
	                                .toString()
	                        + "|"
	                        + (columnMap.get(DomainConstants.SELECT_NAME))
	                                .toString();

	                StringBuffer strChkBox = new StringBuffer(150);
	                strChkBox.append("<input type=\"checkbox\" id=\"");
	                strChkBox.append(strId);
	                strChkBox.append("\" value=\"");
	                strChkBox
	                        .append(strValue
	                                + "\" onclick=\"doCheckboxClick(this); doSelectAllCheck(this)\"");
	                if (noOfSelectedParts == null
	                        && strGrpHeader.equals(strActGrpHeader))
	                    strChkBox
	                            .append(" name=\"emxTablePartRowIds\" checked=\"true\"  />  ");
	                else if (noOfSelectedParts != null
	                        && selectedPartList.contains(strValue))
	                    strChkBox
	                            .append(" name=\"emxTablePartRowIds\" checked=\"true\"  />  ");
	                else
	                    strChkBox.append(" name=\"emxTablePartRowIds\"  />");

	                retunList.add(strChkBox.toString());
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return retunList;
	    }

	    /**
	     * This method gets called for Split & Replace functionality of Feature
	     * get the Immediate Parents of Logical feature
	     * @param context
	     * @param args
	     * @throws Exception
	     */
	    @com.matrixone.apps.framework.ui.ProgramCallable
	    public MapList getImidiateParentWithProductDetails(Context context, String[] args) throws Exception
	    {
	    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    	String strParentId = (String) programMap.get("objectId");
	    	setObjectId(strParentId);
	    	//Products connected to Logical Feature
	    	ConfigurationUtil configUtil = new ConfigurationUtil(strParentId);
	    	//attribute list on type
	    	StringList typeattributeList = new StringList();
	    	typeattributeList.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME+"]");
	    	//attribute list on relationship
	    	StringList relationattributeList = new StringList();
	    	relationattributeList.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
	    	String Relatioship_Pattern=ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES;
	    	//Type pattern
	    	StringBuffer  Type_Pattern= new StringBuffer(200);
	    	Type_Pattern.append(ConfigurationConstants.TYPE_LOGICAL_FEATURE);
	    	Type_Pattern.append(",");
	    	Type_Pattern.append(ConfigurationConstants.TYPE_PRODUCTS);
	    	String strEffectivityExpression = null;
	    	MapList mlLogicalFeatureData  = configUtil.getObjectStructure(context, Type_Pattern.toString(), Relatioship_Pattern,
	    			typeattributeList, relationattributeList, true, false, 1, 0, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, (short)0, strEffectivityExpression);

	    	return mlLogicalFeatureData;
	    }

	    /**
	      * In Step3 of should split & replace, display parent Features in the Name Column
	      * This is called from split & replace functionality for the column
	      * @param context
	      * @param args
	      * @throws Exception
	      */

	    public List getContextProducts(Context context, String args[]) throws Exception
	    {
	    	List lstContextProduct = new StringList();
	    	HashMap requestMap = (HashMap)JPO.unpackArgs(args);
	    	MapList ObjectList = (MapList) requestMap.get("objectList");
	    	for (int count=0; count<ObjectList.size(); count++)
	        {
	        	String toReturn = "";
		        Map objMap = (Map)ObjectList.get(count);
		        String strDisplayName = (String)objMap.get("Name");
		        if(strDisplayName!=null)
		        	toReturn =  strDisplayName;
		        else
		        	toReturn =  DomainConstants.EMPTY_STRING;

		        lstContextProduct.add(toReturn);

	        }
	    	return lstContextProduct;
	    }

	     /**
	      * In Step3 of should split & replace, should display parent Features in the Name Column
	      * This is called from split & replace functionality for the column
	      * @param context
	      * @param args
	      * @throws Exception
	      */
	     public List getContextProductLines(Context context, String args[]) throws Exception
	     {
	    	List lstContextPL = new StringList();
	    	String strName = null;
	    	HashMap requestMap = (HashMap)JPO.unpackArgs(args);

		    	List ObjectList = (MapList) requestMap.get("objectList");

		        for (int count=0; count<ObjectList.size(); count++)
		        {
		        	String parentPLId = "";
			        Map objMap = (Map)ObjectList.get(count);
			        String strObjectID = (String)objMap.get(ConfigurationConstants.SELECT_ID);

			        DomainObject objParent = new DomainObject(strObjectID);
			        
			        Map stPLIDs = objParent.getInfo(context, new StringList("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].from.to["+ConfigurationConstants.RELATIONSHIP_PRODUCTLINE_MODELS+"].from.id"));

                    if(stPLIDs.containsKey("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].from.to["+ConfigurationConstants.RELATIONSHIP_PRODUCTLINE_MODELS+"].from.id"))
                    	parentPLId = (String)stPLIDs.get("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].from.to["+ConfigurationConstants.RELATIONSHIP_PRODUCTLINE_MODELS+"].from.id");
                    else if(stPLIDs.containsKey("to["+ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT+"].from.to["+ConfigurationConstants.RELATIONSHIP_PRODUCTLINE_MODELS+"].from.id"))
                    	parentPLId = (String)stPLIDs.get("to["+ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT+"].from.to["+ConfigurationConstants.RELATIONSHIP_PRODUCTLINE_MODELS+"].from.id");

                    if(ProductLineCommon.isNotNull(parentPLId)){
                    	DomainObject objProd = new DomainObject(parentPLId);
    	        		strName = objProd.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_MARKETING_NAME);
                    }
                    else{
                    	strName = "";
                    }

			        lstContextPL.add(strName);

		        }
		        return lstContextPL;
		     }

	     /**
	      * This method to get combo box with split features as options for list of
	      * product
	      * @param context
	      * @param args
	      * @return
	      * @throws Exception
	      */

	     public List getSplitFeature(Context context, String[] args)
	             throws Exception {
	         List valueList = new StringList();
	         DomainObject domObj;
	         HashMap programMap = (HashMap) JPO.unpackArgs(args);
	         List ObjectIdsList = (MapList) programMap.get("objectList");
	         Map paramMap = (Map) programMap.get("paramList");
	         try {
	             int numberOfInstances = Integer.parseInt((String) paramMap
	                     .get("NumberOfInstances"));

	             for (int i = 0; i < ObjectIdsList.size(); i++) {
	                 Map object = (Map) ObjectIdsList.get(i);


	                 String strFLTID = (String)object.get(ConfigurationConstants.SELECT_RELATIONSHIP_ID);

	                 StringBuffer strValue = new StringBuffer("<select id=\""
	                         + XSSUtil.encodeForHTMLAttribute(context,object.get(DomainConstants.SELECT_ID).toString())
	                         + "\"   name=\"emxTableProdFeatureSelection\" >");
	                 domObj = new DomainObject(paramMap.get("objectId").toString());

	                 String strName = "";

	                 	strName = domObj.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME);

	                 strName = FrameworkUtil.findAndReplace(strName , "&","&amp;");
	                 strName = FrameworkUtil.findAndReplace(strName , "<", "&lt;");
	                 strName = FrameworkUtil.findAndReplace(strName , ">", "&gt;");
	                 strName = FrameworkUtil.findAndReplace(strName , "\"", "&quot;");

	                 strValue.append("<option id=\"");
	                 strValue.append(XSSUtil.encodeForHTMLAttribute(context,paramMap.get("objectId").toString()));
	                 strValue.append("|");
	                 strValue.append(XSSUtil.encodeForHTMLAttribute(context,strFLTID));
	                 strValue.append("\" value =\"");
	                 strValue.append(XSSUtil.encodeForHTMLAttribute(context,paramMap.get("objectId").toString()));
	                 strValue.append("|");
	                 strValue.append(XSSUtil.encodeForHTMLAttribute(context,strFLTID));
	                 strValue.append("\" >");
	                 strValue.append(XSSUtil.encodeForHTML(context,strName));
	                 strValue.append(":");
	                 strValue.append(XSSUtil.encodeForHTML(context,domObj.getInfo(context, DomainConstants.SELECT_NAME)));
	                 strValue.append("</option>");

	                 for (int j = 0; j < numberOfInstances; j++) {

	                     domObj = new DomainObject(paramMap.get("TargetID" + j).toString());
	                     strValue.append("<option id=\"");
	                     strValue.append(XSSUtil.encodeForHTMLAttribute(context,paramMap.get("TargetID" + j).toString()));
	                     strValue.append("|");
	                     strValue.append(XSSUtil.encodeForHTMLAttribute(context,strFLTID));
	                     strValue.append("\" value =\"");
	                     strValue.append(XSSUtil.encodeForHTMLAttribute(context,paramMap.get("TargetID" + j).toString()));
	                     strValue.append("|");
	                     strValue.append(XSSUtil.encodeForHTMLAttribute(context,strFLTID));
	                     strValue.append("\" >");
	                     strValue.append(XSSUtil.encodeForHTML(context,strName));
	                     strValue.append(":");
	                     strValue.append(XSSUtil.encodeForHTML(context,domObj.getInfo(context, DomainConstants.SELECT_NAME)));
	                     strValue.append("</option>");
	                 }
	                 strValue.append("</select>");
	                 valueList.add(strValue.toString());
	             }
	         } catch (Exception e) {
	             e.printStackTrace();
	         }
	         return valueList;
	     }
	 /**
	  * Get RMB for Logical Feature for View GBOM summary
	  * @param context
	  * @param args
	  * @return
	  * @throws Exception
	  */
	 public HashMap getViewGBOMSummaryRMB(Context context, String[] args)
	 throws Exception {
		 HashMap hmpDummy = new HashMap();
		 hmpDummy.put("type", "menu");
		 hmpDummy.put("label", "I am dummy map");
		 hmpDummy
		 .put("description", "get all the files checked into the object");
		 hmpDummy.put("roles", new StringList("all"));
		 hmpDummy.put("settings", null);
		 MapList mapContent = new MapList();
		 HashMap hmpInput = (HashMap) JPO.unpackArgs(args);

		 HashMap paramMap = (HashMap) hmpInput.get("paramMap");
		 HashMap commandMap = (HashMap) hmpInput.get("commandMap");
		 HashMap requestMap = (HashMap) hmpInput.get("requestMap");
		 String strParentOId = requestMap.get("objectId").toString();
		 HashMap hmpSettings = (HashMap) commandMap.get("settings");
		 hmpSettings.remove("Dynamic Command Function");
		 hmpSettings.remove("Dynamic Command Program");

		 String tempRowId = paramMap.get("rmbTableRowId").toString();
		 String tempId = "";

		 StringTokenizer stk = new StringTokenizer(tempRowId, "|");
		 tempId = stk.nextElement().toString();
		 if (tempRowId != null && tempRowId.indexOf("|") > 0)
			 tempId = stk.nextElement().toString();

		 commandMap.put("settings", hmpSettings);
		 commandMap.put("href",
				 "../configuration/ViewGBOMSummaryPreProcess.jsp?mode=RMBGBOM&objectId="
				 + tempId + "&productID=" + strParentOId);
		 mapContent.add(commandMap);
		 hmpDummy.put("Children", mapContent);
		 return hmpDummy;
	 }

	 /**
	  * Get RMB for Logical Feature for View Design Variants
	  * @param context
	  * @param args
	  * @return
	  * @throws Exception
	  */
	 public HashMap getViewDesignVariantsRMB(Context context, String[] args)
	 throws Exception {
		 HashMap hmpDummy = new HashMap();
		 hmpDummy.put("type", "menu");
		 hmpDummy.put("label", "I am dummy map");
		 hmpDummy
		 .put("description", "get all the files checked into the object");
		 hmpDummy.put("roles", new StringList("all"));
		 hmpDummy.put("settings", null);
		 MapList mapContent = new MapList();
		 HashMap hmpInput = (HashMap) JPO.unpackArgs(args);

		 HashMap paramMap = (HashMap) hmpInput.get("paramMap");
		 HashMap commandMap = (HashMap) hmpInput.get("commandMap");
		 HashMap requestMap = (HashMap) hmpInput.get("requestMap");
		 String strParentOId = requestMap.get("objectId").toString();
		 HashMap hmpSettings = (HashMap) commandMap.get("settings");
		 hmpSettings.remove("Dynamic Command Function");
		 hmpSettings.remove("Dynamic Command Program");

		 String tempRowId = paramMap.get("rmbTableRowId").toString();
		 String tempId = "";

		 StringTokenizer stk = new StringTokenizer(tempRowId, "|");
		 tempId = stk.nextElement().toString();
		 if (tempRowId != null && tempRowId.indexOf("|") > 0)
			 tempId = stk.nextElement().toString();

		 commandMap.put("settings", hmpSettings);
		 commandMap
		 .put(
				 "href",
				 "../configuration/DesignVariantPreProcess.jsp?mode=RMBviewDesignVariant&objectId="
				 + tempId + "&productID=" + strParentOId);
		 mapContent.add(commandMap);
		 hmpDummy.put("Children", mapContent);
		 return hmpDummy;
	 }

	 /**
	  * Get RMB for Logical Feature for Create BOM Quantity Rule
	  * @param context
	  * @param args
	  * @return
	  * @throws Exception
	  */
	 public HashMap getCreateBOMQuantityRuleRMB(Context context, String[] args)
	 throws Exception {
		 HashMap hmpDummy = new HashMap();
		 hmpDummy.put("type", "menu");
		 hmpDummy.put("label", "I am dummy map");
		 hmpDummy
		 .put("description", "get all the files checked into the object");
		 hmpDummy.put("roles", new StringList("all"));
		 hmpDummy.put("settings", null);
		 MapList mapContent = new MapList();
		 HashMap hmpInput = (HashMap) JPO.unpackArgs(args);

		 HashMap paramMap = (HashMap) hmpInput.get("paramMap");
		 HashMap commandMap = (HashMap) hmpInput.get("commandMap");
		 HashMap requestMap = (HashMap) hmpInput.get("requestMap");
		 String strParentOId = requestMap.get("objectId").toString();
		 HashMap hmpSettings = (HashMap) commandMap.get("settings");
		 hmpSettings.remove("Dynamic Command Function");
		 hmpSettings.remove("Dynamic Command Program");

		 String tempRowId = paramMap.get("rmbTableRowId").toString();
		 String tempId = "";

		 StringTokenizer stk = new StringTokenizer(tempRowId, "|");
		 tempId = stk.nextElement().toString();
		 if (tempRowId != null && tempRowId.indexOf("|") > 0)
			 tempId = stk.nextElement().toString();

		 commandMap.put("settings", hmpSettings);
		 commandMap
		 .put(
				 "href",
				 "../configuration/CreateRuleDialog.jsp?modetype=create&commandName=FTRQuantityRuleSettings&ruleType=QuantityRule" +
				 "&submitURL=../configuration/QuantityRuleCreatePostProcess.jsp?mode=create&ruleType=QuantityRule&submitAction=refreshCallermode=RMBviewDesignVariant&objectId="
				 + tempId + "&parentOID=" + strParentOId);
		 mapContent.add(commandMap);
		 hmpDummy.put("Children", mapContent);
		 return hmpDummy;
	 }
	 /**
	  * Get RMB for Logical Feature for Merge And Replace
	  * @param context
	  * @param args
	  * @return
	  */
	 public HashMap getMergeAndReplaceRMB(Context context, String[] args) throws Exception{
		 HashMap hmpDummy = new HashMap();
		 hmpDummy.put("type", "menu");
		 hmpDummy.put("label", "I am dummy map");
		 hmpDummy
		 .put("description", "get all the files checked into the object");
		 hmpDummy.put("roles", new StringList("all"));
		 hmpDummy.put("settings", null);
		 MapList mapContent = new MapList();
		 HashMap hmpInput = (HashMap) JPO.unpackArgs(args);

		 HashMap paramMap = (HashMap) hmpInput.get("paramMap");
		 HashMap commandMap = (HashMap) hmpInput.get("commandMap");
		 HashMap requestMap = (HashMap) hmpInput.get("requestMap");
		 String strParentOId = requestMap.get("objectId").toString();
		 HashMap hmpSettings = (HashMap) commandMap.get("settings");
		 hmpSettings.remove("Dynamic Command Function");
		 hmpSettings.remove("Dynamic Command Program");

		 String tempRowId = paramMap.get("rmbTableRowId").toString();
		 String tempId = "";
		 String relId = "";
		 StringTokenizer stk = new StringTokenizer(tempRowId, "|");
		 relId = stk.nextElement().toString();
		 if (tempRowId != null && tempRowId.indexOf("|") > 0)
			 tempId = stk.nextElement().toString();

		 commandMap.put("settings", hmpSettings);
		 commandMap
		 .put(
				 "href",
				 "../configuration/LogicalFeatureMergeReplaceSearchPreProcess.jsp?mode=MergeReplace&context=RMB&Step=SearchFeature&HelpMarker=emxhelpmergeandreplaceuse&objectId="
				 + tempId + "&parentOID=" + strParentOId+ "&relId=" + relId);
		 mapContent.add(commandMap);
		 hmpDummy.put("Children", mapContent);
		 return hmpDummy;
	 }
	 /**
	  * Get RMB for Logical Feature for Split And Replace
	  * @param context
	  * @param args
	  * @return
	  */
	 public HashMap getSplitAndReplaceRMB(Context context, String[] args) throws Exception{
		 HashMap hmpDummy = new HashMap();
		 hmpDummy.put("type", "menu");
		 hmpDummy.put("label", "I am dummy map");
		 hmpDummy
		 .put("description", "get all the files checked into the object");
		 hmpDummy.put("roles", new StringList("all"));
		 hmpDummy.put("settings", null);
		 MapList mapContent = new MapList();
		 HashMap hmpInput = (HashMap) JPO.unpackArgs(args);

		 HashMap paramMap = (HashMap) hmpInput.get("paramMap");
		 HashMap commandMap = (HashMap) hmpInput.get("commandMap");
		 HashMap requestMap = (HashMap) hmpInput.get("requestMap");
		 String strParentOId = requestMap.get("objectId").toString();
		 HashMap hmpSettings = (HashMap) commandMap.get("settings");
		 hmpSettings.remove("Dynamic Command Function");
		 hmpSettings.remove("Dynamic Command Program");

		 String tempRowId = paramMap.get("rmbTableRowId").toString();
		 String tempId = "";
		 String relId = "";
		 StringTokenizer stk = new StringTokenizer(tempRowId, "|");
		 relId = stk.nextElement().toString();
		 if (tempRowId != null && tempRowId.indexOf("|") > 0)
			 tempId = stk.nextElement().toString();

		 commandMap.put("settings", hmpSettings);
		 commandMap
		 .put(
				 "href",
				 "../configuration/LogicalFeatureSplitReplaceStepOnePreProcess.jsp?mode=SplitReplace&context=RMB&Step=SplitReplace&view=details&autoFilter=false&displayView=details&HelpMarker=emxhelpsplitandreplaceuse&objectId="
				 + tempId + "&parentOID=" + strParentOId+"&relId=" + relId);
		 mapContent.add(commandMap);
		 hmpDummy.put("Children", mapContent);
		 return hmpDummy;
	 }

	 /**
	  * Get RMB for Logical Feature for Create Assembly Configuration
	  * @param context
	  * @param args
	  * @return
	  */
	 public HashMap getCreateAssemblyConfigurationRMB(Context context, String[] args) throws Exception{
		 HashMap hmpDummy = new HashMap();
		 hmpDummy.put("type", "menu");
		 hmpDummy.put("label", "I am dummy map");
		 hmpDummy
		 .put("description", "get all the files checked into the object");
		 hmpDummy.put("roles", new StringList("all"));
		 hmpDummy.put("settings", null);
		 MapList mapContent = new MapList();
		 HashMap hmpInput = (HashMap) JPO.unpackArgs(args);

		 HashMap paramMap = (HashMap) hmpInput.get("paramMap");
		 String _sContextOID = (String)paramMap.get("objectId");
		 HashMap commandMap = (HashMap) hmpInput.get("commandMap");
		 HashMap hmpSettings = (HashMap) commandMap.get("settings");
		 hmpSettings.remove("Dynamic Command Function");
		 hmpSettings.remove("Dynamic Command Program");

		 String tempRowId = paramMap.get("rmbTableRowId").toString();
		 String tempId = "";
		 String relId = "";
		 StringTokenizer stk = new StringTokenizer(tempRowId, "|");
		 relId = stk.nextElement().toString();
		 if (tempRowId != null && tempRowId.indexOf("|") > 0)
			 tempId = stk.nextElement().toString();

		 commandMap.put("settings", hmpSettings);
		 commandMap
		 .put(
				 "href",
				 "../configuration/ProductConfigurationCreateProcess.jsp?objectId="
				 + tempId + "&relId=" + relId+"&parentObjectID="+_sContextOID);
		 mapContent.add(commandMap);
		 hmpDummy.put("Children", mapContent);
		 return hmpDummy;
	 }
   	     /**
	      * It is used to display design Varinats in Logical Feature structure table
	      * @param context
	      * @param args
	      * @return returns Yes if Design Variants are connected, else No
	      * @throws FrameworkException
	      */
	     public Vector getDesignVariants(Context context, String[] args)
	     throws FrameworkException {

	    	 try{




	    		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    		 Map paramList = (Map) programMap.get("paramList");

	    		 String reportFormat = (String)paramList.get("reportFormat");
	    		 
	    		 MapList lstObjectIdsList = (MapList) programMap.get("objectList");
	    		 Vector contextDesignVariants = new Vector();
	    		 String strParentId = (String) paramList.get("parentOID");
	    		 DomainObject featureObject = null;
	    	//	 StringBuffer sTabLink = new StringBuffer(260);
//	    		 sTabLink.append("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/DesignVariantPreProcess.jsp?mode="+strMode+"&amp;objectId=${OBJECT_ID},"+strParentId);
//	    		 String strTabPart1 = "', '800', '700', 'true', 'listHidden')\"";
//	    		 String strTabPart2 = ">";
//	    		 String strTabPart3 = "</a>";
	    		 String strDesVar = "No";
	    		 String strVaraintExist = "False";
	    		 String strLanguage = context.getSession().getLanguage();
	    		 
	    		
	    		 for (int i = 0; i < lstObjectIdsList.size(); i++)
	    		 {
	    			 Map mpLF = (Map)lstObjectIdsList.get(i);
	    			 String strObjId = (String) mpLF.get(DomainObject.SELECT_ID);
	    			 featureObject = DomainObject.newInstance(context,strObjId);
	    			 String strRootNode = (String)mpLF.get("Root Node");
	    			 String strValue = "";
	    			 //String ctxProductID = (String)mpLF.get("id[parent]");
	    			 if(strRootNode!=null && strRootNode.equalsIgnoreCase("True")){
	    				 String strType = featureObject.getInfo(context, SELECT_TYPE);
	    				 if(mxType.isOfParentType(context, strType,ConfigurationConstants.TYPE_LOGICAL_STRUCTURES)){
	    					 strVaraintExist = featureObject.getInfo(context,"from["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"]");
	    					 strDesVar =  EnoviaResourceBundle.getProperty(context, SUITE_KEY,
	    					 "emxConfiguration.DesignVarianceExists.FalseValue",strLanguage);

	    					 if (strVaraintExist != null && strVaraintExist.equalsIgnoreCase("True"))
	    					 {
	    						 strDesVar = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
	    						 "emxConfiguration.DesignVarianceExists.TrueValue",strLanguage);
	    					 }
	    					 strValue = strDesVar;
	    					 contextDesignVariants.add(strValue);
	    				 }else{
	    					 contextDesignVariants.add(DomainConstants.EMPTY_STRING);
	    				 }
	    			 }
	    			 else if(featureObject.isKindOf(context, ConfigurationConstants.TYPE_PRODUCTS))
	    				 contextDesignVariants.add(DomainConstants.EMPTY_STRING);
	    			 else{

	    				 strVaraintExist = featureObject.getInfo(context,"from["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"]");
	    				 strDesVar =  EnoviaResourceBundle.getProperty(context, SUITE_KEY,
	    				 "emxConfiguration.DesignVarianceExists.FalseValue",strLanguage);

	    				 if (strVaraintExist != null && strVaraintExist.equalsIgnoreCase("True"))
	    				 {
	    					 strDesVar = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
	    					 "emxConfiguration.DesignVarianceExists.TrueValue",strLanguage);
	    				 }

	    				 strValue = strDesVar;
	    				 	// Added for IR-190361V6R2014

	    				 	//contextDesignVariants.add(strValue);
		    				StringBuffer temp = new StringBuffer();
		    				if (reportFormat != null && !("null".equalsIgnoreCase(reportFormat)) && reportFormat.equalsIgnoreCase("CSV"))
		    				{
			            		temp.append(strValue);
			            	}
		    				else{
		    				temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/DesignVariantPreProcess.jsp?mode=hyperlinkViewDesignVariant");
	 						temp.append("&amp;objectId=");
	 						temp.append(XSSUtil.encodeForHTMLAttribute(context, strObjId));
	 						temp.append("&amp;productID=");
							temp.append(XSSUtil.encodeForHTMLAttribute(context, strParentId));
	 						temp.append("', '450', '300', 'true', 'hiddenFrame')\">");
	 						temp.append(XSSUtil.encodeForXML(context,strValue));
	 						temp.append("</a>");
		    				}
	 						String output =  temp.toString();
							contextDesignVariants.add(output);

							// End of IR-190361V6R2014
	    			 }
	    		 }
	    		 return contextDesignVariants;

	    	 }
	    	 catch (Exception e) {
	    		 throw new FrameworkException(e.toString());
	    	 }

	     }
		    /** This method gets the object Structure List for the context Logical Feature.This method gets invoked
		     * by settings in the command which displays the Structure Navigator for Logical Feature type objects
		     *  @param context the eMatrix <code>Context</code> object
		     *  @param args    holds the following input arguments:
		     *      		   contextObjId - String having context object Id
		     *  @return MapList containing the object list to display in Product Line structure navigator
		     *  @throws Exception if the operation fails
		     */

		    public static MapList getStructureList(Context context, String[] args)throws FrameworkException {
		    	MapList logFeatureStructList = new MapList();
		    	try{
		    		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		    		HashMap paramMap   = (HashMap)programMap.get("paramMap");
		    		String contextObjId    = (String)paramMap.get("objectId");
			        String strParentSymType = "";
			        String strTypePattern = "*";
			        String strRelPattern = "";
			        String[] arrRel = null;
			        DomainObject domContextObj = new DomainObject(contextObjId);
			        String strType = domContextObj.getInfo(context, DomainObject.SELECT_TYPE);
			        if(mxType.isOfParentType(context, strType,ConfigurationConstants.TYPE_LOGICAL_FEATURE)){
				        strParentSymType = "type_LogicalFeature";
				        String strAllowedSTRel = EnoviaResourceBundle.getProperty(context, "emxConfiguration.StructureTree.SelectedRel."+strParentSymType);
					    if(strAllowedSTRel!=null && !strAllowedSTRel.equals("")){
					    	arrRel = strAllowedSTRel.split(",");

						    for(int i=0; i< arrRel.length; i++){
						    	strRelPattern = strRelPattern + "," + PropertyUtil.getSchemaProperty(context,arrRel[i]);
						    }
						    strRelPattern = strRelPattern.replaceFirst(",", "");
					    }
				        StringList objectSelects = new StringList(3);
			            objectSelects.add(DomainConstants.SELECT_ID);
			            objectSelects.add(DomainConstants.SELECT_TYPE);
			            objectSelects.add(DomainConstants.SELECT_NAME);
						ConfigurationUtil confUtil = new ConfigurationUtil(contextObjId);
						logFeatureStructList = confUtil.getObjectStructure(context,strTypePattern,strRelPattern,
								objectSelects, null ,false, true, (short) 1,0,
								"", "",DomainObject.FILTER_STR_AND_ITEM, DomainObject.EMPTY_STRING);
			        }else {
			        	logFeatureStructList = (MapList) emxPLCCommon_mxJPO.getStructureListForType(context, args);
				  	}
			        return logFeatureStructList;

		    	}
		    	catch (Exception e) {

		    		throw new FrameworkException(e.toString());
				}


		    }

		    /**
		     * Used to display Revision in Create Page of Logical Feature, when the policy filed is Updated.
		     * @param context
		     * @param args
		     * @return revision series of a given policy
		     * @throws FrameworkException
		     */
		    public HashMap getRevision (Context context,String[] args)throws FrameworkException
		    {

		    	try{
		    		HashMap hmProgramMap = (HashMap) JPO.unpackArgs(args);
			    	HashMap fieldValues = (HashMap) hmProgramMap.get( "fieldValues" );
			    	HashMap returnMap = new HashMap();

			    	try{
			    		String strPolicy = (String)fieldValues.get("Policy");
			    		Policy adminPolicy = new Policy(strPolicy);
			    		String strRevision = adminPolicy.getFirstInSequence(context);
			    		returnMap.put("SelectedValues", strRevision);
			    		returnMap.put("SelectedDisplayValues", strRevision);

			    	}catch (Exception ex) {
			    		throw ex;
			    	}
			    	return returnMap;

		    	}
		    	catch (Exception e) {
		    		throw new FrameworkException(e.toString());
				}

		    }

		    /**
		     * Method call to get the list of all related Products of a given context.
		     *
		     * @param context the eMatrix <code>Context</code> object
		     * @param args - Holds parameters passed from the calling method
		     * @return - Maplist of bus ids of candidate Products
		     * @throws FrameworkException if the operation fails
		     */
		    @com.matrixone.apps.framework.ui.ProgramCallable
		    public MapList getRelatedProductsForContext (Context context, String[] args) throws FrameworkException {
		    	try{
		    		HashMap programMap = (HashMap)JPO.unpackArgs(args);
			    	String parentOID = (String)programMap.get("parentOID");
			    	MapList relBusObjPageList = new MapList();

			    	relBusObjPageList = ConfigurationUtil.getRelatedProductsForContext(context, parentOID);

			        return  relBusObjPageList;
		    	}
		    	catch (Exception e) {
		    		throw new FrameworkException(e.toString());
				}

		    }

		    /**
		     * This method is used to return the status icon of an object
		     * @param context the eMatrix <code>Context</code> object
		     * @param args holds arguments
		     * @return List- the List of Strings in the form of 'Name Revision'
		     * @throws FrameworkException if the operation fails
		    **/

		    public List getStatusIcon (Context context, String[] args) throws FrameworkException{

		    	try{
		    		//unpack the arguments
			        Map programMap = (HashMap) JPO.unpackArgs(args);
			        List lstobjectList = (MapList) programMap.get("objectList");
			        Map paramList = (HashMap)programMap.get("paramList");
			        String reportFormat = (String)paramList.get("reportFormat");

			        Iterator objectListItr = lstobjectList.iterator();
			        //initialise the local variables
			        Map objectMap = new HashMap();
			        String strObjId = DomainConstants.EMPTY_STRING;
			        String strObjState = DomainConstants.EMPTY_STRING;
			        String strIcon = DomainConstants.EMPTY_STRING;
			        String strObjPolicy = DomainConstants.EMPTY_STRING;
			        String strObjPolicySymb = DomainConstants.EMPTY_STRING;
			        String strObjStateSymb = DomainConstants.EMPTY_STRING;
			        StringBuffer sbStatePolicyKey = new StringBuffer();
			        boolean flag = false;
			        List lstNameRev = new StringList();
			        StringBuffer stbNameRev = new StringBuffer(100);
			        DomainObject domObj = null;
			        //loop through all the records
			        while(objectListItr.hasNext())
			        {
			            objectMap = (Map) objectListItr.next();
			            strObjId = (String)objectMap.get(DomainConstants.SELECT_ID);
			            domObj = DomainObject.newInstance(context, strObjId);
			            strObjState = domObj.getInfo(context, DomainConstants.SELECT_CURRENT);
			            strObjPolicy = domObj.getInfo(context, DomainConstants.SELECT_POLICY);

			            // Getting symbolic names for both policy & state
			            strObjPolicySymb = FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_POLICY,strObjPolicy,true);
			            strObjStateSymb = FrameworkUtil.reverseLookupStateName(context, strObjPolicy, strObjState);

			            // Forming the key which is to be looked up
			            sbStatePolicyKey = new StringBuffer("emxProduct.LCStatusImage.");
			            sbStatePolicyKey.append(strObjPolicySymb)
			                            .append(".")
			                            .append(strObjStateSymb);

			            // Getting the value for the corresponding key, if not catching it to set flag = false
			            try{
			                strIcon = EnoviaResourceBundle.getProperty(context,sbStatePolicyKey.toString());
			                flag = true;
			            }
			            catch(Exception ex)
			            {
			                flag = false;
			            }

			            if(flag)
			            {
			                strObjState = FrameworkUtil.findAndReplace(strObjState," ", "");
			                StringBuffer sbStateKey = new StringBuffer("emxFramework.State.");
			                sbStateKey.append(strObjState);
			                strObjState = EnoviaResourceBundle.getProperty(context,"ProductLine", sbStateKey.toString(),context.getSession().getLanguage());
			                stbNameRev.delete(0, stbNameRev.length());

			            	if (reportFormat != null && !("null".equalsIgnoreCase(reportFormat)) && reportFormat.length()>0){
			            		lstNameRev.add(strObjState);
			            	}else{

				                stbNameRev = stbNameRev.append("<img src=\"../common/images/")
				                                .append(strIcon)
				                                .append("\" border=\"0\"  align=\"middle\" ")
				                                .append("TITLE=\"")
				                                .append(" ")
				                                .append(XSSUtil.encodeForXML(context, strObjState))
				                                .append("\"")
				                                .append("/>");
				                lstNameRev.add(stbNameRev.toString());
			            	}
			            }
			            else
			            {
			                lstNameRev.add(DomainConstants.EMPTY_STRING);
			            }
			        }
			        return lstNameRev;

		    	}
		    	catch (Exception e) {
		    		throw new FrameworkException(e.toString());
				}

		    }
		    /**
		     * This method is used to return the Name of the part family with Hyper link of Logical Feature Table
		     *
		     * @param context
		     *            the eMatrix <code>Context</code> object
		     * @param args
		     *            holds arguments
		     * @return Vector- the List of Strings containing part families name.
		     * @throws FrameworkException
		     *             if the operation fails
		     */
		    public Vector getPartFamilyLink(Context context, String[] args) throws FrameworkException {
		    	try{
		    		HashMap programMap = (HashMap)JPO.unpackArgs(args);
			    	MapList objectMapList = (MapList)programMap.get("objectList");
			    	HashMap paramMap = (HashMap)programMap.get("paramList");
			    	String reportFormat = (String)paramMap.get("reportFormat");
			    	String suiteDir = (String) paramMap.get("SuiteDirectory");
			    	String suiteKey = (String) paramMap.get("suiteKey");
			    	Vector partList = new Vector();
			    	String[] objectsarray = new String[objectMapList.size()];
			    	for(int i=0;i<objectMapList.size();i++)
			    	{
			    		Map objectMap = (Map) objectMapList.get(i);
			    		String logicalFeatureID = (String)objectMap.get(DomainConstants.SELECT_ID);
			    		objectsarray[i]= logicalFeatureID;
			    	}
			    	StringList objectSelectables = new StringList();
			    	//String strID = "from["+ConfigurationConstants.RELATIONSHIP_GBOM+"]."+DomainConstants.SELECT_TO_ID;
			    	String strID = "from["+ConfigurationConstants.RELATIONSHIP_GBOM+"].to["+ConfigurationConstants.TYPE_PART_FAMILY+"]."+DomainConstants.SELECT_ID;
			    	DomainConstants.MULTI_VALUE_LIST.add(strID);
			    	objectSelectables.add(strID);
			    	MapList logicalFeaturesMapList = DomainObject.getInfo(context, objectsarray, objectSelectables);
			    	DomainConstants.MULTI_VALUE_LIST.remove(strID);
			    	String strPartFamilyName = "";
			    	String strPartFamilyID = "";
			    	for(int i=0;i<logicalFeaturesMapList.size();i++)
			    	{
			    		Map objectMap = (Map) logicalFeaturesMapList.get(i);
			    		String output = "";
			    		if(objectMap.size()>0){
			    			Object objGBOMds = (Object)objectMap.get(strID);

			    				StringList strTempList = new StringList();
			    				strTempList = (StringList) objGBOMds;
			    				String[] strPRIds = new String[strTempList.size()];
			    				for(int j=0; j<strTempList.size();j++ ){
			    					strPRIds[j] = (String) strTempList.get(j);
			    				}
			    				MapList mapGBOMDetails = DomainObject.getInfo(context,strPRIds ,ConfigurationUtil.getBasicObjectSelects(context));
			    				for(int k=0; k<mapGBOMDetails.size();k++){
			    					Map tempMap = (Map)mapGBOMDetails.get(k);
			    					strPartFamilyName = tempMap.get(DomainObject.SELECT_NAME).toString();
			    					strPartFamilyID =  tempMap.get(DomainObject.SELECT_ID).toString();
			    					String objectType =  tempMap.get(DomainObject.SELECT_TYPE).toString();
			    					if(objectType.equalsIgnoreCase(ConfigurationConstants.TYPE_PART_FAMILY))
			    					{
			    						StringBuffer temp = new StringBuffer();
			    						if (reportFormat != null && !("null".equalsIgnoreCase(reportFormat)) && reportFormat.length()>0){
			    							temp.append(XSSUtil.encodeForXML(context,strPartFamilyName));
			    						}else{
				    						temp.append(" <img border=\"0\" src=\"");
				    						temp.append("images/iconSmallPartFamily.gif");
				    						temp.append("\" /> ");
				    						temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
				    						temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteDir));
				    						temp.append("&amp;suiteKey=");
				    						temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteKey));
				    						temp.append("&amp;objectId=");
				    						temp.append(XSSUtil.encodeForHTMLAttribute(context, strPartFamilyID));
				    						temp.append("', '450', '300', 'true', 'popup')\">");
				    						temp.append(XSSUtil.encodeForXML(context,strPartFamilyName));
				    						temp.append("</a>");
			    						}

			    						output = output + "," + temp;
			    					}

			    				}
			    				if(output.length()>0){
			    					output = output.replaceFirst(",", "");
			    					partList.add(output);
			    				}
			    				else{
			    					partList.add(DomainConstants.EMPTY_STRING);
			    				}

			    		}

			    		else{
			    			partList.add(DomainConstants.EMPTY_STRING);
			    		}

			    	}

			    	return  partList;

		    	}
		    	catch (Exception e) {
		    		throw new FrameworkException(e.toString());
				}

		    }

		    /**
		     * Method call to Display the Display Name column in DV table. In a specific Context
		     *
		     * @param context the eMatrix <code>Context</code> object
		     * @param args - Holds parameters passed from the calling method
		     * @return - true if a context specific
		     * @throws FrameworkException if the operation fails
		     */
		    public boolean dvDependentContext (Context context, String args[]) throws FrameworkException{
				boolean bResult = false;
				try{
					HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			        String strFromContext = (String)paramMap.get("contextIndependent");
			        if(strFromContext!=null && !strFromContext.equals("")){
			        	if(strFromContext.equalsIgnoreCase("no")){
			        		bResult = true;
			        	}
			        }
					return bResult;

		    	}
		    	catch (Exception e) {
		    		throw new FrameworkException(e.toString());
				}

			}
		    /**
		     * Method call to Display the Display Name column in DV table. In a independent of Context
		     *
		     * @param context the eMatrix <code>Context</code> object
		     * @param args - Holds parameters passed from the calling method
		     * @return - true if independent context
		     * @throws FrameworkException if the operation fails
		     */
		    public boolean dvInDependentContext (Context context, String args[]) throws FrameworkException{
				boolean bResult = false;
				try{
					 HashMap paramMap = (HashMap) JPO.unpackArgs(args);
				        String strFromContext = (String)paramMap.get("contextIndependent");
				        if(strFromContext!=null && !strFromContext.equals("")){
				        	if(strFromContext.equalsIgnoreCase("yes")){
				        		bResult = true;
				        	}
				        }
						return bResult;
		    	}
		    	catch (Exception e) {
		    		throw new FrameworkException(e.toString());
				}

			}
		    /**
		     * Method returns the First Sequence revision for the policy Logical Feature.
		     * It is called for the create page of Logical Feature
		     * @param context the eMatrix <code>Context</code> object
		     * @param args - Holds parameters passed from the calling method
		     * @return - returns the First Sequence revision
		     * @throws FrameworkException if the operation fails
		     */
		    public String getDefaultRevision (Context context,String[] args)throws Exception
		    {
		    	//TODO -Policy in combobox is not in order in which getPolicies return
				String strType=ConfigurationConstants.TYPE_LOGICAL_FEATURE;
				MapList policyList = com.matrixone.apps.domain.util.mxType.getPolicies(context,strType,false);
				String strDefaultPolicy = (String)((HashMap)policyList.get(0)).get(DomainConstants.SELECT_NAME);

		    	Policy policyObject = new Policy(strDefaultPolicy);
		    	String strRevision = policyObject.getFirstInSequence(context);
				return strRevision;
		    }

		    /**
		     * Method Disables/Enables the Design Selection Criteria in the Create page of Logical Feature
		     * It is called for the create page of Logical Feature
		     * @param context the eMatrix <code>Context</code> object
		     * @param args - Holds parameters passed from the calling method
		     * @return - returns true for Logical Feature.
		     * @throws FrameworkException if the operation fails
		     */
		    public boolean selectionTypeDisable (Context context, String args[]) throws FrameworkException{
		    	boolean bResult = false;
		    	try{
		    		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			    	String strType = (String) paramMap.get(DomainConstants.SELECT_TYPE);
			    	if(strType.equalsIgnoreCase("type_LogicalFeature"))
			    			bResult=true;
			    	else{
			    		StringTokenizer strtoken = new StringTokenizer(strType,":");
			    		strtoken.nextToken();
			    		StringTokenizer strtok = new StringTokenizer(strtoken.nextToken(),",");
			    		String strtempType = (String) strtok.nextToken();
			    		if(strtempType.equalsIgnoreCase(ConfigurationConstants.TYPE_LOGICAL_FEATURE))
			    			bResult=true;
			    	}
					return bResult;

		    	}
		    	catch (Exception e) {
		    		throw new FrameworkException(e.toString());
				}

			}
		    /**
			 * This is an Utility method to check if the passed Logical Feature business object is used in any Rule Expression
			 * @param context
			 * @param strObjectIdList -- Business Objects ID List
			 * @return If used in any Rule Expression return true, else false.
			 * @throws FrameworkException
			 */
			public static int isUsedInRulesForTypeCheck(Context context, String []args) throws Exception{

				  String strLanguage = context.getSession().getLanguage();
		    	  String strSubjectKey = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
		    			"emxProduct.Alert.UsedInRuleExpression",strLanguage);

				  String ContextID = args[0];
		       	  DomainObject domContextBus = new DomainObject(ContextID);

		          StringList selectables = new StringList();

		          String strObjBCRLE = "to["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE+"]";
		          String strObjBCRRE = "to["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE+"]";
		          String strObjMPRLE = "to["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_MARKETING_PREFERENCE+"]";
		          String strObjMPRRE = "to["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_MARKETING_PREFERENCE+"]";
		          String strObjQRLE = "to["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_QUANTITY_RULE+"]";
		          String strObjQRRE = "to["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_QUANTITY_RULE+"]";
		          String strObjIRRE = "to["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_INCLUSION_RULE+"]";
		          String strRelBCRLE = "to["+ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE+"]";
		          String strRelBCRRE = "to["+ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE+"]";
		          String strRelMPRLE = "to["+ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_MARKETING_PREFERENCE+"]";
		          String strRelMPRRE = "to["+ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_MARKETING_PREFERENCE+"]";
		          String strRelQRLE = "to["+ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_QUANTITY_RULE+"]";
		          String strRelQRRE = "to["+ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_QUANTITY_RULE+"]";
		          String strRelIRRE = "to["+ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_INCLUSION_RULE+"]";

		          selectables.add(strObjBCRLE);
		          selectables.add(strObjBCRRE);
		          selectables.add(strObjMPRLE);
		          selectables.add(strObjMPRRE);
		          selectables.add(strObjQRLE);
		          selectables.add(strObjQRRE);
		          selectables.add(strObjIRRE);
		          selectables.add(strRelBCRLE);
		          selectables.add(strRelBCRRE);
		          selectables.add(strRelMPRLE);
		          selectables.add(strRelMPRRE);
		          selectables.add(strRelQRLE);
		          selectables.add(strRelQRRE);
		          selectables.add(strRelIRRE);

		          DomainConstants.MULTI_VALUE_LIST.add(strObjBCRLE);
		          DomainConstants.MULTI_VALUE_LIST.add(strObjBCRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strObjMPRLE);
		          DomainConstants.MULTI_VALUE_LIST.add(strObjMPRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strObjQRLE);
		          DomainConstants.MULTI_VALUE_LIST.add(strObjQRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strObjIRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelBCRLE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelBCRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelMPRLE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelMPRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelQRLE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelQRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelIRRE);

		          Map mapUsedResult = domContextBus.getInfo(context, selectables);

		          DomainConstants.MULTI_VALUE_LIST.remove(strObjBCRLE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strObjBCRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strObjMPRLE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strObjMPRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strObjQRLE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strObjQRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strObjIRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelBCRLE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelBCRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelMPRLE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelMPRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelQRLE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelQRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelIRRE);

		          StringList slUsage = ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strObjBCRLE));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strObjBCRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strObjMPRLE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strObjMPRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strObjQRLE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strObjQRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strObjIRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelBCRLE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelBCRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelMPRLE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelMPRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelQRLE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelQRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelIRRE)));

		          if(slUsage.contains("TRUE") || slUsage.contains("True")){
		        	  throw new FrameworkException(strSubjectKey);
		          }
		          return 0;
			}

			/**
			 * This is an Utility method to check if the passed Logical Feature relationship is used in any Rule Expression
			 * @param context
			 * @param strObjectIdList -- Business Objects ID List
			 * @return If used in any Rule Expression return true, else false.
			 * @throws FrameworkException
			 */
			public static int isUsedInRulesForRelCheck(Context context, String []args) throws Exception{

				  String strLanguage = context.getSession().getLanguage();
		    	  String strSubjectKey = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
		    			"emxProduct.Alert.UsedInRuleExpression",strLanguage);

				  String relID = args[0];

		          StringList selectables = new StringList();

	              String strRelBCRLE = "tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE+"]";
	              String strRelBCRRE = "tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE+"]";
	              String strRelMPRLE = "tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_MARKETING_PREFERENCE+"]";
	              String strRelMPRRE = "tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_MARKETING_PREFERENCE+"]";
	              String strRelQRLE = "tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_QUANTITY_RULE+"]";
	              String strRelQRRE = "tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_QUANTITY_RULE+"]";
	              String strRelIRRE = "tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_INCLUSION_RULE+"]";

	              selectables.add(strRelBCRLE);
	              selectables.add(strRelBCRRE);
	              selectables.add(strRelMPRLE);
	              selectables.add(strRelMPRRE);
	              selectables.add(strRelQRLE);
	              selectables.add(strRelQRRE);
	              selectables.add(strRelIRRE);

		          DomainConstants.MULTI_VALUE_LIST.add(strRelBCRLE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelBCRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelMPRLE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelMPRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelQRLE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelQRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelIRRE);

		          MapList mlUsedResult = DomainRelationship.getInfo(context, new String[]{relID}, selectables);

		          DomainConstants.MULTI_VALUE_LIST.remove(strRelBCRLE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelBCRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelMPRLE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelMPRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelQRLE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelQRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelIRRE);

		          Map mapUsedResult = (Map)mlUsedResult.get(0);

		          StringList slUsage = ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelBCRLE));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelBCRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelMPRLE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelMPRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelQRLE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelQRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelIRRE)));

		          if(slUsage.contains("TRUE") || slUsage.contains("True")){
		        	  throw new FrameworkException(strSubjectKey);
		          }
		          return 0;
			}
			/**
			 * Method which does not do anything, But require as Update Program for revision field in Create Page of Logical Feature
			 * @param context
			 * @param args
			 * @throws FrameworkException
			 */
			public void emptyProgram(Context context, String []args) throws FrameworkException{

			}
			/** It is used to display "Display Name" Column in Structure  Browser of Logical Feature
			 * @param context
			 * @param args
			 * @return  Marketing Name For Products and Display Name for Logical Feature
			 * @throws FrameworkException
			 */
			public StringList displayNameForLogicalFeature(Context context, String []args) throws FrameworkException{

				StringList displayNameList = new StringList();
				try{
					HashMap programMap = (HashMap) JPO.unpackArgs(args);
					MapList objectMapList = (MapList)programMap.get("objectList");
				    String strMName = null;
			    	String strDName = null;
	    			String strRevision = null;
	    			String strDisplayName = null;
	    			String attrMarketName = "attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]";
	    			String attrRevision = DomainConstants.SELECT_REVISION;
	    			String attrDisplayName = "attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME	+"]";

			    	for(int i=0;i<objectMapList.size();i++)
			    	{
			    		Map objectMap = (Map) objectMapList.get(i);
			    		strMName = (String) objectMap.get(attrMarketName);
			    		strDName = (String) objectMap.get(attrDisplayName);
			    		strRevision = (String) objectMap.get(attrRevision);
			    		String strLogicalFeatureID = (String)objectMap.get(DomainConstants.SELECT_ID);
			    		DomainObject domObject = new DomainObject(strLogicalFeatureID);
	                                
                        if(ProductLineCommon.isNotNull(strDName)){
			    			strDisplayName =strDName + " " + strRevision;
			    		}
			    		else if(ProductLineCommon.isNotNull(strMName)){
			    			strDisplayName = strMName + " " + strRevision;
			    		}
			    		else{
			    		if (domObject.exists(context)
						&& (domObject.isKindOf(context,
								ConfigurationConstants.TYPE_PRODUCTS)
								|| domObject.isKindOf(context,
										ConfigurationConstants.TYPE_MODEL) || domObject
								.isKindOf(
										context,
										ConfigurationConstants.TYPE_PRODUCT_LINE)
										|| domObject
										.isKindOf(
												context,
												ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION)))
			    		{
			    				strDisplayName = getDisplayName(context,strLogicalFeatureID,false);
			    			}
			    		else
			    		{
			    				strDisplayName = getDisplayName(context,strLogicalFeatureID,true);
			    			}
			    		}
			    		displayNameList.addElement(strDisplayName);
			    	}
				}
				catch (Exception e) {
					throw new FrameworkException(e);
				}
				return displayNameList;
			}
			/**
			 * Its is used to get the display name for Logical Feature and Market name for Products
			 * @param context
			 * @param objectID -- Logical Feature ID/Product ID
			 * @param displayName - boolean, true for LF and false for Products
			 * @return DisplayName for LF and Market Name for Products
			 * @throws FrameworkException
			 */
			private String getDisplayName (Context context, String objectID, boolean displayName)throws FrameworkException{
				String strDisplayName = null;
				try{
					DomainObject domObject = new DomainObject(objectID);
					String strName = null;
	    			String strRevision = null;
	    			String attrMarketName = "attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]";
	    			String attrRevision = DomainConstants.SELECT_REVISION;
	    			String attrDisplayName = "attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME	+"]";
	    			String name = DomainConstants.SELECT_NAME;
					StringList selectables = new StringList();
	    			selectables.addElement(attrRevision);
	    			if(displayName){
	    				selectables.addElement(attrDisplayName);
	    				selectables.addElement(name);
	    			}
	    			else{
	    				selectables.addElement(attrMarketName);
	    			}
	    			Map tempMap = domObject.getInfo(context, selectables);
	    			if(displayName){
	    				strName = (String) tempMap.get(attrDisplayName);
	    			}
	    			else{
	    				strName = (String) tempMap.get(attrMarketName);
	    			}
	    			if("".equalsIgnoreCase(strName)|| "null".equalsIgnoreCase(strName) || strName == null){
	    				strName = (String) tempMap.get(name);
	    			}
	    			strRevision = (String) tempMap.get(attrRevision);
	    			strDisplayName = strName + " " + strRevision;

				}
				catch (Exception e) {
					throw new FrameworkException(e);
				}
				return strDisplayName;
			}


			private String getDisplayNameWithoutRevision (Context context, String objectID, boolean displayName)throws FrameworkException{
				String strDisplayName = null;
				try{
					DomainObject domObject = new DomainObject(objectID);
					String strName = null;
	    			String attrMarketName = "attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]";
	    			String attrDisplayName = "attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME	+"]";
	    			String name = DomainConstants.SELECT_NAME;
					StringList selectables = new StringList();
	    			if(displayName){
	    				selectables.addElement(attrDisplayName);
	    				selectables.addElement(name);
	    			}
	    			else{
	    				selectables.addElement(attrMarketName);
	    			}
	    			Map tempMap = domObject.getInfo(context, selectables);
	    			if(displayName){
	    				strName = (String) tempMap.get(attrDisplayName);
	    			}
	    			else{
	    				strName = (String) tempMap.get(attrMarketName);
	    			}
	    			if("".equalsIgnoreCase(strName)|| "null".equalsIgnoreCase(strName) || strName == null){
	    				strName = (String) tempMap.get(name);
	    			}
	    			strDisplayName = strName;

				}
				catch (Exception e) {
					throw new FrameworkException(e);
				}
				return strDisplayName;
			}

			/**
		     * This method is used to return the Name of the part family with Hyper link of Logical Feature Properties Page
		     *
		     * @param context
		     *            the eMatrix <code>Context</code> object
		     * @param args
		     *            holds arguments
		     * @return Vector- the List of Strings containing part families name.
		     * @throws FrameworkException
		     *             if the operation fails
		     */
			public String getPartFamilyLinkForForm(Context context, String[] args) throws FrameworkException {
				try{
					HashMap programMap = (HashMap)JPO.unpackArgs(args);
					HashMap requestMap = (HashMap)programMap.get("requestMap");
					String strMode = (String)requestMap.get("mode");
					String suiteDir = (String) requestMap.get("SuiteDirectory");
					String suiteKey = (String) requestMap.get("suiteKey");
					Map fieldMap = (HashMap) programMap.get("fieldMap");
					String strFieldName = (String)fieldMap.get("name");
					String partList = null;
					String strobjectId = (String)requestMap.get("objectId");
					setId(strobjectId);
					ConfigurationUtil util = new ConfigurationUtil(strobjectId);
					StringList objectSelectables = new StringList();
					String strID = DomainConstants.SELECT_ID;
					String strName = DomainConstants.SELECT_NAME;
					String strRevision = DomainConstants.SELECT_REVISION;
					objectSelectables.add(strID);
					objectSelectables.add(strName);
					objectSelectables.add(strRevision);
					StringBuffer excludePFOID = new StringBuffer();
					//Type Pattern
					String strTypePattern = ConfigurationConstants.TYPE_PART_FAMILY;
					//Rel PAttern
					String strRelPattern = ConfigurationConstants.RELATIONSHIP_GBOM +"," +ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM;

					StringList tempStringList = new StringList();
					//Replaced DomainConstants.EMPTY_STRINGLIST with tempStringList for stale object issue
					MapList logicalFeaturePFList = util.getObjectStructure(context,strTypePattern,strRelPattern,
							objectSelectables, tempStringList,false, true, 1,0,
							DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING,(short) 1,DomainConstants.EMPTY_STRING);
					String strPartFamilyName = "";
					String strPartFamilyID = "";

					Boolean bDVConnected = Boolean.valueOf(false);
					String output = "";
					boolean editMode=true;
					if(strMode!=null && !strMode.equals("") &&
							!strMode.equalsIgnoreCase("null") && strMode.equalsIgnoreCase("edit"))
					{
						//Check For Design Variants
						String strContextId = this.getId(context);
						String strArgs[] = new String[4];
						strArgs[0] = strContextId;
						strArgs[1] = "from";
						strArgs[2] = ConfigurationConstants.RELATIONSHIP_VARIES_BY+ "," + ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY;

						//DB Call
						String strPFconnected = ConfigurationUtil.hasRelationship(context,strArgs);
						if(strPFconnected!=null && strPFconnected.equalsIgnoreCase("false")){
							bDVConnected = false;
						}else{
							bDVConnected = true;
						}

						//DV and PF both are connected non edit mode
						if(bDVConnected && logicalFeaturePFList.size()>0)
								{
							editMode=false;
						}
						//DV connected and PF non connected edit mode
						else if(bDVConnected && logicalFeaturePFList.size()==0)
									{
							editMode=true;
									}
						//DV non connected and PF connected edit mode
						else if(!bDVConnected)
						{
							editMode=true;
								}

						if(editMode){
							if(logicalFeaturePFList.size()>0){


									for(int i=0;i<logicalFeaturePFList.size();i++)
								{
										Map logicalFeaturePFMap = (Map) logicalFeaturePFList.get(i);
										strPartFamilyName = (String) logicalFeaturePFMap.get(strName);

										strPartFamilyID =  (String) logicalFeaturePFMap.get(strID);
										excludePFOID .append(XSSUtil.encodeForHTMLAttribute(context, strPartFamilyID));
										excludePFOID .append(",");
									StringBuffer temp = new StringBuffer();
										temp.append(" <img border=\"0\" src=\"");
										temp.append("images/iconSmallPartFamily.gif");
										temp.append("\" /> ");
											temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
											temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteDir));
											temp.append("&amp;suiteKey=");
											temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteKey));
											temp.append("&amp;objectId=");
									temp.append(XSSUtil.encodeForHTMLAttribute(context, strPartFamilyID));
											temp.append("', '450', '300', 'true', 'popup')\">");
											temp.append(XSSUtil.encodeForHTMLAttribute(context, strPartFamilyName));
											temp.append("</a>");
											output = output + "<BR/>" + temp;

										output = output + "<BR/>";
									}
								}

							StringBuffer strBuffer = new StringBuffer();
							if(excludePFOID.length()>0)
							excludePFOID.deleteCharAt(excludePFOID.length()-1);
							strBuffer.append("<input type=\"text\" READONLY ");
							strBuffer.append("name=\"");
							strBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
							strBuffer.append("Display\" id=\"\" value=\"");
							strBuffer.append("\">");
							strBuffer.append("<input type=\"hidden\" name=\"");
							strBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
							strBuffer.append("\" value=\"");
							strBuffer.append("\">");
							strBuffer.append("<input type=\"hidden\" name=\"");
							strBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
							strBuffer.append("OID\" value=\"");
							strBuffer.append("\">");
							strBuffer.append("<input ");
							strBuffer.append("type=\"button\" name=\"btnPartFamily\"");
							strBuffer.append("size=\"200\" value=\"...\" alt=\"\" enabled=\"true\" ");
							strBuffer.append("onClick=\"javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES=type_PartFamily:CURRENT!=policy_Classification.state_Obsolete&excludeOID=" +excludePFOID+
									"&table=FTRFeatureSearchResultsTable&selection=single&showSavedQuery=true&hideHeader=true&HelpMarker=emxhelpfullsearch&submitURL=../configuration/LogicalFeatureSearchUtil.jsp&mode=Chooser&chooserType=SlideInFormChooser&fieldNameActual=PartFamilyOID&fieldNameDisplay=PartFamilyDisplay");
							strBuffer.append("&HelpMarker=emxhelpfullsearch','850','630')\">");
							strBuffer.append("&nbsp;&nbsp;");
							strBuffer.append("<a href=\"javascript:ClearPartFamily('");
							strBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
							strBuffer.append("')\">");

									String strClear =
										 EnoviaResourceBundle.getProperty(context, SUITE_KEY,
												"emxProduct.Button.Clear",context.getSession().getLanguage());
							strBuffer.append(strClear);
							strBuffer.append("</a>");

							output = output + strBuffer;
										partList=output;
									}
					}

					else{
						editMode=false;
					}
					if(!editMode){

						if(logicalFeaturePFList.size()>0){
							for(int i=0;i<logicalFeaturePFList.size();i++){
							Map logicalFeaturePFMap = (Map) logicalFeaturePFList.get(i);
							strPartFamilyName = (String) logicalFeaturePFMap.get(strName);
							strPartFamilyID =  (String) logicalFeaturePFMap.get(strID);

										StringBuffer temp = new StringBuffer();
										temp.append(" <img border=\"0\" src=\"");
										temp.append("images/iconSmallPartFamily.gif");
										temp.append("\" /> ");
										temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
										temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteDir));
										temp.append("&amp;suiteKey=");
										temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteKey));
										temp.append("&amp;objectId=");
										temp.append(XSSUtil.encodeForHTMLAttribute(context, strPartFamilyID));
										temp.append("', '450', '300', 'true', 'popup')\">");
										temp.append(XSSUtil.encodeForHTMLAttribute(context, strPartFamilyName));
										temp.append("</a>");
										output = output + "<BR/>" + temp;
								}
							}
							if(output.length()>0){
								output = output.replaceFirst("<BR/>", "");
								partList=output;
							}
							else{
								partList=DomainConstants.EMPTY_STRING;
							}
						}

					return  partList;
				}
				catch (Exception e) {
					throw new FrameworkException(e.toString());
				}

			}
		   /** Method call as a trigger to check if the sub-features associated with the
		     * Logical Feature are in. Release state.
		     *
		     * @param context
		     *            the eMatrix <code>Context</code> object
		     * @param args -
		     *            Holds the parameters passed from the calling method
		     * @return int - Returns 0 in case of Check trigger is success and 1 in case
		     *         of failure
		     * @throws Exception
		     *             if operation fails
		     */
		    public int checkSubFeaturesForLogicalFeaturePromote(Context context, String args[])
		    throws Exception {
		    	// return value of the function
		    	int iReturn = 0;
		    	// The feature object id sent by the emxTriggerManager is retrieved
		    	// here.
		    	String objectId = args[0];
		    	String strSelect = DomainObject.SELECT_CURRENT;
		    	// ObjectSelects StringList is initialized
		    	StringList objectSelects = new StringList(strSelect);
		    	ConfigurationUtil util = new ConfigurationUtil();
		    	LogicalFeature logicalFeature = new  LogicalFeature(objectId);
		    	MapList relBusObjList =   logicalFeature.getLogicalFeatureStructure(context,DomainObject.EMPTY_STRING,
		    			DomainObject.EMPTY_STRING,objectSelects, null , false,true, 0, 0,
		    			DomainObject.EMPTY_STRING,DomainObject.EMPTY_STRING, (short) 1,DomainObject.EMPTY_STRING);

		    	boolean isFrozen = false;
		    	int iNumberOfObjects = relBusObjList.size();
		    	if (iNumberOfObjects > 0) {

		    		for (int i = 0; i < iNumberOfObjects; i++) {
		    			Map relBusObjMap = (Map)relBusObjList.get(i);
		    			String strSubFeatureID = (String)relBusObjMap.get(DomainConstants.SELECT_ID);
		    			isFrozen = util.isFrozenState(context,strSubFeatureID);
		    			if (!isFrozen) {
		    				String language = context.getSession().getLanguage();
		    				String strAlertMessage = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
		    						"emxProduct.Alert.LogicalFeaturesPromoteFailedStateNotRelease",language);
		    				emxContextUtilBase_mxJPO
		    				.mqlNotice(context, strAlertMessage);
		    				iReturn = 1;
		    				break;
		    			}

		    		}
		    	}
		    	// Return 0 is validation is passed
		    	return iReturn;
		    }

		    /**
		     * Method call as a trigger to check if the Parts associated with the
		     * Logical Feature are in Release state.
		     *
		     * @param context
		     *            the eMatrix <code>Context</code> object
		     * @param args -
		     *            Holds the parameters passed from the calling method
		     * @return int - Returns 0 in case of Check trigger is success, all parts in
		     *         Release state and 1 in case of failure, any of the parts not
		     *         released
		     * @throws Exception
		     *             if operation fails
		     */
		    public int checkPartsForLogicalFeaturePromote(Context context, String args[])
		    throws Exception {
		    	// return value of the function
		    	int iReturn = 0;
		    	// The feature object id sent by the emxTriggerManager is retrieved
		    	// here.
		    	String objectId = args[0];
		    	String strSelectState = DomainConstants.SELECT_CURRENT;
		    	String strSelectType = DomainConstants.SELECT_TYPE;
		    	// ObjectSelects StringList is initialized
		    	StringList objectSelects = new StringList(strSelectState);
		    	objectSelects.add(strSelectType);
		    	LogicalFeature logicalFeature = new  LogicalFeature(objectId);
		    	StringList tempStringList = new StringList();
		    	//Replaced DomainConstants.EMPTY_STRINGLIST with tempStringList for stale object issue
		    	MapList relBusObjList = logicalFeature.getActiveGBOMStructure(context,
		    			DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, objectSelects,
		    			tempStringList , false,true, 0, 0, DomainConstants.EMPTY_STRING,
		    			DomainConstants.EMPTY_STRING, (short) 1, DomainConstants.EMPTY_STRING);

		    	// The number of Parts connected is obtained.
		    	int iNumberOfObjects = relBusObjList.size();
		    	// Validation check only if there is any part conected.
		    	if (iNumberOfObjects > 0) {

		    		for (int i = 0; i < iNumberOfObjects; i++) {
		    			// The state is retreived from the MapList obtained.
		    			Map relBusObjMap = (Map)relBusObjList.get(i);
		    			String strState = (String)relBusObjMap.get(strSelectState);
		    			String strObjectType = (String)relBusObjMap.get(strSelectType);
		    			// If the object is of type PartFamily no check will be done for
		    			// the state.
		    			if (!strObjectType.equals(ConfigurationConstants.TYPE_PARTFAMILY)) {

		    				if (!strState
		    						.equalsIgnoreCase(ConfigurationConstants.STATE_RELEASE)) {
		    					String language = context.getSession().getLanguage();
		    					String strAlertMessage =  EnoviaResourceBundle.getProperty(context, SUITE_KEY,
		    							"emxProduct.Alert.PartsCheckFailed",language);
		    					emxContextUtilBase_mxJPO.mqlNotice(context,
		    							strAlertMessage);
		    					iReturn = 1;
		    					break;

		    				}
		    			}
		    		}
		    	}
		    	// Return 0 is validation is passed
		    	return iReturn;
		    }

		    /**
		     * Method call as a trigger to promote all the rules to the Release state if they already are not in that state.
		     *
		     * @param context the eMatrix <code>Context</code> object
		     * @param args - Holds the parameters passed from the calling method
		     * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
		     * @throws Exception if the operation fails
		     */
		     public int promoteRules(Context context, String args[]) throws Exception
		     {
		       //The product object id sent by the emxTriggerManager is retrieved here.
		       String objectId = args[0];
		       //The object id is set to the context
		       setId(objectId);
		       DomainObject domObject = new DomainObject(objectId);
		       String strComma = ",";
		       String strRelationshipPattern ="";
		       if(domObject.isKindOf(context, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES))
		    	   strRelationshipPattern = ConfigurationConstants.RELATIONSHIP_QUANTITY_RULE + strComma;
		       strRelationshipPattern = strRelationshipPattern + ConfigurationConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE + strComma

		       + ConfigurationConstants.RELATIONSHIP_PRODUCT_RULEEXTENSION + strComma
		       + ConfigurationConstants.RELATIONSHIP_PRODUCT_FIXEDRESOURCE;
		       //Object where condition to retrieve the objects that are not already in Release state.
		       String objectWhere = "("+DomainConstants.SELECT_CURRENT+ " != \""+ConfigurationConstants.STATE_RELEASE+"\")";
		       //Type to fetched is all types returned by the relationship.
		       String strTypePattern ="";
		       if(domObject.isKindOf(context, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES))
		    	  strTypePattern = ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE + strComma;
		       strTypePattern =strTypePattern + ConfigurationConstants.TYPE_QUANTITY_RULE + strComma
		       + ConfigurationConstants.TYPE_RULE_EXTENSION + strComma
		       + ConfigurationConstants.TYPE_FIXED_RESOURCE;
		       //ObjectSelects retreives the parameters of the objects that are to be retreived.
		       StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
		       ConfigurationUtil util = new ConfigurationUtil(objectId);


		       StringList tempStringList = new StringList();
		       //The objects connected to the product based on the relationships defined are obtained.
		       //Replaced DomainConstants.EMPTY_STRINGLIST with tempStringList for stale object issue
		       MapList relBusObjList = util.getObjectStructure(context, strTypePattern, strRelationshipPattern,
						objectSelects, tempStringList, true, true, 0, 0, objectWhere,
						   DomainConstants.EMPTY_STRING, (short)0, DomainConstants.EMPTY_STRING);
		       //The number of objects connected is obtained.
		       int iNumberOfObjects = relBusObjList.size();
		       //The promotion is to happen only if there are any connected objects
		       if (iNumberOfObjects > 0)
		       {
		         //Processing for each of the object connected to the product
		         for (int i = 0;i < iNumberOfObjects ; i++)
		         {
		           //Each id of the object connected to product is obtained.
		           String strTempObjectId = (String)((Map)relBusObjList.get(i)).get(DomainConstants.SELECT_ID);
		           //The context is set with the object id obtained.
		           setId(strTempObjectId);
		           //The state of the context object is set to release.
		           setState(context,ConfigurationConstants.STATE_RELEASE);
		         }
		       }
		       //0 returned just to indicate the end of processing.
		       return 0;
		     }

		     /**
			     * Method call as a trigger to promote all the sub Features to the Release state if
			     * they already are not in that.
			     *
			     * @param context
			     *            the eMatrix <code>Context</code> object
			     * @param args -
			     *            Holds the parameters passed from the calling method
			     * @return int - Returns 0 in case of Check trigger is success and 1 in case
			     *         of failure
			     * @throws Exception
			     *             if operation fails
			     */
			    public int promoteSubFeatures(Context context, String args[]) throws Exception {
			        // The Parent Feature object id sent by the emxTriggerManager
			        String objectId = args[0];
			        String state = ConfigurationConstants.STATE_RELEASE;
			        // The object id is set to the context
			        setId(objectId);
			        //boolean isFrozen = ConfigurationUtil.isFrozenState(context,objectId);


				    	String strSelect = DomainConstants.SELECT_ID;
				    	// ObjectSelects StringList is initialized
				    	StringList objectSelects = new StringList(strSelect);
				    	LogicalFeature logicalFeature = new  LogicalFeature(objectId);
				    	// Object where condition to retrieve the sub feature objects that are not already in Release state.
				    	StringList tempStringList = new StringList();
				    	String objectWhere = "("+DomainConstants.SELECT_CURRENT+ " != \""+ConfigurationConstants.STATE_RELEASE+"\")";
				    	//Replaced DomainConstants.EMPTY_STRINGLIST with tempStringList for stale object issue
				    	MapList relBusObjList =   logicalFeature.getLogicalFeatureStructure(context,DomainConstants.EMPTY_STRING,
				    			DomainConstants.EMPTY_STRING,objectSelects, tempStringList , false,true, 0, 0,
				    			objectWhere,DomainConstants.EMPTY_STRING, (short) 1,DomainConstants.EMPTY_STRING);

				        // The number of objects connected is obtained.
				        int iNumberOfObjects = relBusObjList.size();
				        // The promotion is to happen only if there are any connected objects
				        if (iNumberOfObjects > 0) {
				            // Processing for each of the object connected to the product
				            for (int i = 0; i < iNumberOfObjects; i++) {
				                // Each id of the object connected to product is obtained.
				                String strTempObjectId = (String) ((Hashtable) relBusObjList
				                        .get(i)).get(objectSelects.get(0));
				                // The context is set with the object id obtained.
				                setId(strTempObjectId);
				                // The state of the context object is set to release.
				                setState(context, state);
				            }
				        }

			        // 0 returned just to indicate the end of processing.
			        return 0;
			    }

			    /**
			     * Method call as a trigger to demote all the rules to the PRELIMINARY state if they already are not in that state.
			     *
			     * @param context the eMatrix <code>Context</code> object
			     * @param args - Holds the parameters passed from the calling method
			     * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
			     * @throws Exception if the operation fails
			     */
			     public int demoteRules(Context context, String args[]) throws Exception
			     {
			       //The product object id sent by the emxTriggerManager is retrieved here.
			       String objectId = args[0];
			       //The object id is set to the context
			       setId(objectId);
			       DomainObject domObject = new DomainObject(objectId);
			       String strComma = ",";
			       String strRelationshipPattern ="";
			       if(domObject.isKindOf(context, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES))
			    	   strRelationshipPattern = ConfigurationConstants.RELATIONSHIP_QUANTITY_RULE + strComma;
			       strRelationshipPattern = strRelationshipPattern + ConfigurationConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE + strComma

			       + ConfigurationConstants.RELATIONSHIP_PRODUCT_RULEEXTENSION + strComma
			       + ConfigurationConstants.RELATIONSHIP_PRODUCT_FIXEDRESOURCE;
			       //Object where condition to retrieve the objects that are not already in Release state.
			       String objectWhere = "("+DomainConstants.SELECT_CURRENT+ " == \""+ConfigurationConstants.STATE_RELEASE+"\")";
			       //Type to fetched is all types returned by the relationship.
			       String strTypePattern = ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE + strComma
			       + ConfigurationConstants.TYPE_QUANTITY_RULE + strComma
			       + ConfigurationConstants.TYPE_RULE_EXTENSION + strComma
			       + ConfigurationConstants.TYPE_FIXED_RESOURCE;
			       //ObjectSelects retreives the parameters of the objects that are to be retreived.
			       StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
			       ConfigurationUtil util = new ConfigurationUtil(objectId);

			       StringList tempStringList = new StringList();
			       //The objects connected to the product based on the relationships defined are obtained.
			       //Replaced DomainConstants.EMPTY_STRINGLIST with tempStringList for stale object issue
			       MapList relBusObjList = util.getObjectStructure(context, strTypePattern, strRelationshipPattern,
							objectSelects, tempStringList, true, true, 0, 0, objectWhere,
							   DomainConstants.EMPTY_STRING, (short)0, DomainConstants.EMPTY_STRING);
			       //The number of objects connected is obtained.
			       int iNumberOfObjects = relBusObjList.size();
			       //The promotion is to happen only if there are any connected objects
			       if (iNumberOfObjects > 0)
			       {
			         //Processing for each of the object connected to the product
			         for (int i = 0;i < iNumberOfObjects ; i++)
			         {
			           //Each id of the object connected to product is obtained.
			           String strTempObjectId = (String)((Map)relBusObjList.get(i)).get(DomainConstants.SELECT_ID);
			           //The context is set with the object id obtained.
			           setId(strTempObjectId);
			           //The state of the context object is set to release.
			           setState(context,ConfigurationConstants.STATE_PRELIMINARY);
			         }
			       }
			       //0 returned just to indicate the end of processing.
			       return 0;
			     }

			     /**
			 	 * It is a trigger method. Fires when a instance of "Logical Features" is created.
			 	 * It is used to check the cyclic condition
			 	 * @param context
			 	 *            The ematrix context object.
			 	 * @param String[]
			 	 *            The args .
			 	 * @return zero when cyclic condition is false, throws exception if it is true
			 	 */
			     public static int multiLevelRecursionCheck(Context context,String[] args)throws FrameworkException {

			 		 String strRemovedLFId = PropertyUtil.getGlobalRPEValue(context,"CyclicCheckRequired");
			 		 int iResult = 0;
				 	 if(strRemovedLFId==null || strRemovedLFId.equalsIgnoreCase("")) {

				    	 String fromObjectId = args[0];
				    	 String toObjectId = args[1];
				    	 String relType = args[2];
				    	 try {
				    		 boolean recursionCheck =  ConfigurationUtil.multiLevelRecursionCheck(context,fromObjectId,toObjectId,relType);
				    		 if (recursionCheck) {
		    					String language = context.getSession().getLanguage();
		    					String strAlertMessage =  EnoviaResourceBundle.getProperty(context, SUITE_KEY,
		    							"emxConfiguration.Add.CyclicCheck.Error",language);
		    					throw new FrameworkException(strAlertMessage);
				    		 } else {
				    			 iResult = 0;
				    		 }
				    	 }catch (Exception e) {
		    					throw new FrameworkException(e.getMessage());
				    	 }
				 	}else if(strRemovedLFId.equalsIgnoreCase("False")){
			 			iResult=0;
			 		}
				 	return iResult;
			     }

			 	 /**
			 	 * It is a trigger method. Fires when a "Logical Feature" promoted  from review state to release state.
			 	 * If the promoted LF have previous revision than it will replace with the latest revision in Products/Logical Feature
			 	 * which are not in Release state.
			 	 * @param context
			 	 *            The ematrix context object.
			 	 * @param String[]
			 	 *            The args .
			 	 * @return
			 	 */
			     public void logicalFeatureFloatOnRelease(Context context, String[] args)throws FrameworkException{
			    	 try{
			    		 //Logical Feature ID
			    		 String lfLatestOID = args[0];
			    		 DomainObject domObjectLatestLF = new DomainObject(lfLatestOID);
			    		 //get Previsous revision LF ID
			    		 BusinessObject boPreviousRevision= domObjectLatestLF.getPreviousRevision(context);
			    		 if(boPreviousRevision.exists(context)){
			    			 StringList lstProductChildTypes = ProductLineUtil.getChildTypesIncludingSelf(context, ProductLineConstants.TYPE_PRODUCTS);

			    			 String strPreviousRevID = boPreviousRevision.getObjectId(context);
			    			 //------------------------------------------------------------------------------			    			 
			    			 //Get the applicable item and PV of it and check if Applicability Applies on float on release
			    			 //------------------------------------------------------------------------------
			    			 boolean bApplicabilityApplies = false;
			    			 String RELATIONSHIP_APPLICABLE_ITEM = PropertyUtil.getSchemaProperty(context,"relationship_ApplicableItem");
			    			 String strApplicableItem = "to["
			    					 + ProductLineConstants.RELATIONSHIP_EC_IMPLEMENTED_ITEM + "].from.from["
			    					 + RELATIONSHIP_APPLICABLE_ITEM + "].to."
			    					 + DomainConstants.SELECT_ID ;
			    			 String strApplicableItemPV = "to["
			    					 + ProductLineConstants.RELATIONSHIP_EC_IMPLEMENTED_ITEM + "].from.from["
			    					 + RELATIONSHIP_APPLICABLE_ITEM + "].to.from["+
			    					 ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION+"]."
			    					 +DomainObject.SELECT_TO_ID;
			    			 DomainConstants.MULTI_VALUE_LIST.add(strApplicableItem);
			    			 DomainConstants.MULTI_VALUE_LIST.add(strApplicableItemPV);
			    			 StringList slSelectables=new StringList(strApplicableItem);
			    			 slSelectables.add(strApplicableItemPV);
			    			 Map MpApplicableItems = domObjectLatestLF.getInfo(context, slSelectables);
			    			 DomainConstants.MULTI_VALUE_LIST.remove(strApplicableItem);
			    			 DomainConstants.MULTI_VALUE_LIST.remove(strApplicableItemPV);
			    			 StringList applicableItemList= new StringList();
			    			 if(MpApplicableItems.containsKey(strApplicableItem) && MpApplicableItems.get(strApplicableItem)!=null){
			    				 StringList slApplicableItemPRD = ConfigurationUtil.convertObjToStringList(context,
			    						 (Object)MpApplicableItems.get(strApplicableItem));
			    				 applicableItemList.addAll(slApplicableItemPRD);
			    			 }
			    			 if(MpApplicableItems.containsKey(strApplicableItemPV) && MpApplicableItems.get(strApplicableItemPV)!=null){
			    				 StringList slApplicableItemPV = ConfigurationUtil.convertObjToStringList(context,
			    						 (Object)MpApplicableItems.get(strApplicableItemPV));
			    				 applicableItemList.addAll(slApplicableItemPV);
			    			 }	 
			    			 if (applicableItemList != null && !"null".equals(applicableItemList) && applicableItemList.size() > 0)
			    				 bApplicabilityApplies = true;
				 				
			    			 //---------------------------------------------------------------
			    			 //Traversing to one level Up with LFs Rel for Previous Revision
			    			 //---------------------------------------------------------------
			    			 String strTypePattern = DomainObject.QUERY_WILDCARD;
			    			 String strRelPattern = ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES;
			    			 String strRelease = FrameworkUtil.lookupStateName(context,
			    					 ProductLineConstants.POLICY_PRODUCT, strSymbReleaseState);
			    			 String strObselete = FrameworkUtil.lookupStateName(context,
			    					 ProductLineConstants.POLICY_PRODUCT, strSymbObsoleteState);
			    			 StringBuffer sbBuffer = new StringBuffer();
			    			 String strBusWhereClause = sbBuffer.toString();
			    			 //---------------------------------------------------------------
			    			 //One level UP get all Released/Non Released Parent Product or LF			 				
			    			 //----------------------------------------------------------------
			    			 ConfigurationUtil utilPrevRevLF = new ConfigurationUtil(strPreviousRevID);
			    			 StringList tempStringList = new StringList();
			    			 tempStringList.add(DomainObject.SELECT_TYPE);
			    			 tempStringList.add(DomainObject.SELECT_CURRENT);
			    			 StringList tempRelStringList = new StringList();
			    			 String productid = "tomid["
			    					 + ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST + "]."
			    					 + DomainObject.SELECT_FROM_ID;
			    			 String productCurrent= "tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+
			    					 "].from.current";
			    			 String productType= "tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+
			    					 "].from.type";
			    			 String productFeatureListID= "tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+
			    					 "]."+DomainObject.SELECT_ID;
			    			 tempRelStringList.add(productid);
			    			 tempRelStringList.add(productCurrent);
			    			 tempRelStringList.add(productType);
			    			 tempRelStringList.add(productFeatureListID);
			    			 //TODO - Need to evaluate case in PV as parent
			    			 MapList prevLFListUp = utilPrevRevLF.getObjectStructure(context,strTypePattern,strRelPattern,
			    					 tempStringList, tempRelStringList,true, false, 1,0,
			    					 strBusWhereClause, DomainObject.EMPTY_STRING,(short) 1,DomainObject.EMPTY_STRING);

			    			 //---------------------------------------------------------------
			    			 //Traversing to one level Down with LFs Rel for PREVISION REVISION LF, also get PFL/Productid
			    			 //---------------------------------------------------------------
			    			 StringList objectSelectables = new  StringList();
			    			 objectSelectables.add(DomainObject.SELECT_ID);
			    			 StringList relSelectables = new  StringList();
			    			 String productID= "tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+
			    					 "].from.id";
			    			 relSelectables.add(productFeatureListID);
			    			 relSelectables.add(productCurrent);
			    			 relSelectables.add(productType);
			    			 relSelectables.add(productid);
			    			 MapList prevLFRelIDListDown = utilPrevRevLF.getObjectStructure(context,strTypePattern,strRelPattern,
			    					 objectSelectables, relSelectables,false, true, 1,0,
			    					 DomainObject.EMPTY_STRING, DomainObject.EMPTY_STRING,(short) 1,DomainObject.EMPTY_STRING);

			    			 //---------------------------------------------------------------
			    			 //Traversing to one level Down with LFs Rel for LATEST REVISION LF, also get PFL/Productid
			    			 //---------------------------------------------------------------
			    			 ConfigurationUtil utilLatestLF = new ConfigurationUtil(lfLatestOID);
			    			 MapList latestLFRelIDListDown = utilLatestLF.getObjectStructure(context,strTypePattern,strRelPattern,
			    					 objectSelectables, null,false, true, 1,0,
			    					 DomainObject.EMPTY_STRING, DomainObject.EMPTY_STRING,(short) 1,DomainObject.EMPTY_STRING);


			    			 //------------------------------------------------------------------------------------------------------------------------------
			    			 // will check if Previous Revision of LF has Parent- for non released Parent- LF rel from that parent need to move to new revision LF
			    			 //------------------------------------------------------------------------------------------------------------------------------
			    			 ProductLineCommon plCommon = new ProductLineCommon();
			    			 //prevPFLIDListFinal will hold all parent ids which are not in frozen state, 
			    			 //this List will be used to make PFL connection for LF strucure which is newly added under LF
			    			 StringList prevPFLIDListFinal = new StringList();
			    			 if(prevLFListUp.size()>0){
			    				 //Iterate previous revision LF's Parent
			    				 for(int i=0;i<prevLFListUp.size();i++){
			    					 Map logicalFeaturesRelIDMapUp = (Map)prevLFListUp.get(i);
			    					 String lfRelID = (String)logicalFeaturesRelIDMapUp.get(DomainObject.SELECT_RELATIONSHIP_ID);
			    					 String lfParentOID = (String)logicalFeaturesRelIDMapUp.get(DomainObject.SELECT_ID);
			    					 String lfParentType = (String)logicalFeaturesRelIDMapUp.get(DomainObject.SELECT_TYPE);
			    					 StringList prevPFLIDList = ConfigurationUtil.convertObjToStringList(context,
			    							 (Object)logicalFeaturesRelIDMapUp.get(productFeatureListID));
			    					 StringList prevPFLFfromIDList = ConfigurationUtil.convertObjToStringList(context,
			    							 (Object)logicalFeaturesRelIDMapUp.get(productid));
			    					 StringList prevPFLFfromCurrentList = ConfigurationUtil.convertObjToStringList(context,
			    							 (Object)logicalFeaturesRelIDMapUp.get(productCurrent));
			    					 StringList prevPFLFfromTypeList = ConfigurationUtil.convertObjToStringList(context,
			    							 (Object)logicalFeaturesRelIDMapUp.get(productType));
			    					 //prevPFLIDListFinal update start---
			    					 List slFrozenProductList=new StringList();
			    					 for(int ij=0;ij<prevPFLFfromCurrentList.size();ij++){
			    						 String strCurrent=(String)prevPFLFfromCurrentList.get(ij);
			    						 String strType=(String)prevPFLFfromTypeList.get(ij);
			    						 if(mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_PRODUCT_VARIANT)){
			    							 String strPVid=(String)prevPFLFfromIDList.get(ij);
			    							 String strParentProductState = DomainObject.newInstance(context,strPVid).getInfo(context, "to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION+"].from.current");
			    							 if(strParentProductState.equals(strObselete)||strParentProductState.equals(strRelease)
			    									 ||strCurrent.equals(strObselete)||strCurrent.equals(strRelease)){
			    								 slFrozenProductList.add((String)prevPFLFfromIDList.get(ij));
			    							 }
			    						 }else{
			    							 if(strCurrent.equals(strObselete)||strCurrent.equals(strRelease)){
			    								 slFrozenProductList.add((String)prevPFLFfromIDList.get(ij));
			    							 }
			    						 }
			    					 }
			    					 prevPFLFfromIDList.removeAll(slFrozenProductList);
			    					 if(bApplicabilityApplies){
			    						 prevPFLFfromIDList.retainAll(applicableItemList);
			    					 }
			    					 prevPFLIDListFinal.addAll(prevPFLFfromIDList);
			    					 
			    					 // If Parent of current Logical Feature is Product then below code will get call.
			    					    
			    					    if(!ConfigurationConstants.TYPE_PRODUCT_VARIANT.equalsIgnoreCase(lfParentType) && lstProductChildTypes.contains(lfParentType) && !slFrozenProductList.contains(lfParentOID)){
			    					    	updateRelToConfigurationFeatures(context, lfParentOID, strPreviousRevID, lfLatestOID);
			    					    }
			    					 //
			    					 
			    					 if(bApplicabilityApplies){
			    						 //get previous revision LF's Parent's current state, if Parent is not in Frozen state LF rel will be moved to latest LF Revision
			    						 String strParentState = (String)logicalFeaturesRelIDMapUp.get(DomainObject.SELECT_CURRENT);
			    						 if(lfRelID!=null && !strParentState.equals(strObselete)&& !strParentState.equals(strRelease) && applicableItemList.contains(lfParentOID)){
			    							 //Here case if user corrected structure for all new revision- END
			    							 DomainRelationship.setToObject(context, lfRelID,domObjectLatestLF);
			    						 }
			    					 }else{
			    						 //get previous revision LF's Parent's current state, if Parent is not in Frozen state LF rel will be moved to latest LF Revision
			    						 String strParentState = (String)logicalFeaturesRelIDMapUp.get(DomainObject.SELECT_CURRENT);
			    						 if(lfRelID!=null && !strParentState.equals(strObselete)&& !strParentState.equals(strRelease)){
			    							 //Here case if user corrected structure for all new revision- END
			    							 DomainRelationship.setToObject(context, lfRelID,domObjectLatestLF);
			    						 }			    						 
			    					 }
			    				 }
			    			 }

			    			 //------------------------------------------------------------------------------------------------------------------------------
			    			 // Once LF rel is updated for non-frozen Parent, need to float PFL connections
			    			 // Create Map with old child LF id and its PFL IDs which has to float
			    			 //------------------------------------------------------------------------------------------------------------------------------
			    			 List pfltoFloat=new StringList();
			    			 Map prevRevLFTOPFLIDs=new HashMap();
			    			 //Iterate previous revision LF's child
			    			 for(int i=0;i<prevLFRelIDListDown.size();i++){
			    				 Map prevLFRelIDmp = (Map)prevLFRelIDListDown.get(i);
			    				 String prevLFID = (String)prevLFRelIDmp.get(DomainObject.SELECT_ID);
			    				 StringList prevPFLIDList = ConfigurationUtil.convertObjToStringList(context,
			    						 (Object)prevLFRelIDmp.get(productFeatureListID));
			    				 StringList prevPFLFfromCurrentList = ConfigurationUtil.convertObjToStringList(context,
			    						 (Object)prevLFRelIDmp.get(productCurrent));
			    				 StringList prevPFLFfromTypeList = ConfigurationUtil.convertObjToStringList(context,
			    						 (Object)prevLFRelIDmp.get(productType));
			    				 StringList prevPFLFfromIDList = ConfigurationUtil.convertObjToStringList(context,
			    						 (Object)prevLFRelIDmp.get(productID));
			    				 if(bApplicabilityApplies){
			    					 for(int ii=prevPFLFfromIDList.size()-1;ii>=0;ii--){
			    						 String PFLfromID=(String)prevPFLFfromIDList.get(ii);
			    						 if (PFLfromID != null && !applicableItemList.contains(PFLfromID)){
			    							 prevPFLIDList.remove(ii);
			    							 prevPFLFfromCurrentList.remove(ii);
			    							 prevPFLFfromTypeList.remove(ii);
			    							 prevPFLFfromIDList.remove(ii);
			    						 }
			    					 }
			    				 }
			    				 
			    				 // exclude PFL - for which parent is in Frozen state
			    				 List slFrozenProductList=new StringList();
			    				 for(int ij=0;ij<prevPFLFfromCurrentList.size();ij++){
			    					 String strCurrent=(String)prevPFLFfromCurrentList.get(ij);
			    					 String strType=(String)prevPFLFfromTypeList.get(ij);
			    					 if(mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_PRODUCT_VARIANT)){
			    						 String strPVid=(String)prevPFLFfromIDList.get(ij);
			    						 String strParentProductState = DomainObject.newInstance(context,strPVid).getInfo(context, "to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION+"].from.current");
			    						 if(strParentProductState.equals(strObselete)||strParentProductState.equals(strRelease)
			    								 ||strCurrent.equals(strObselete)||strCurrent.equals(strRelease)){
			    							 slFrozenProductList.add((String)prevPFLIDList.get(ij));
			    						 }
			    					 }else{
			    						 if(strCurrent.equals(strObselete)||strCurrent.equals(strRelease)){
			    							 slFrozenProductList.add((String)prevPFLIDList.get(ij));
			    						 }
			    					 }
			    				 }
			    				 //prevPFLIDList will be having all PFL which are pointing to child LF and which parent in not in frozen state.
			    				 prevPFLIDList.removeAll(slFrozenProductList);
			    				 //for each child LF ID add PFL IDs to float
			    				 prevRevLFTOPFLIDs.put(prevLFID,prevPFLIDList);
			    				 //also create list of PFL IDs which will be going to float
			    				 pfltoFloat.addAll(prevPFLIDList);
			    			 }

			    			 //------------------------------------------------------------------------------
			    			 //iterate on latest revision LF childs
			    			 //------------------------------------------------------------------------------		 					
			    			 List pflActallyFloated=new StringList();
			    			 for(int p=0;p<latestLFRelIDListDown.size();p++){
			    				 Map latestLFRelIDmp = (Map)latestLFRelIDListDown.get(p);
			    				 String latestLFID = (String)latestLFRelIDmp.get(DomainObject.SELECT_ID);
			    				 String latestLFRelID = (String)latestLFRelIDmp.get(DomainObject.SELECT_RELATIONSHIP_ID);
			    				 //will check if prev revision is in the map;
			    				 String previousRevisionOID= new DomainObject(latestLFID).getPreviousRevision(context).getObjectId(context);
			    				 if(prevRevLFTOPFLIDs.containsKey(latestLFID) || prevRevLFTOPFLIDs.containsKey(previousRevisionOID)){
			    					 StringList prevPFLIDList=(StringList)prevRevLFTOPFLIDs.get(latestLFID);
			    					 if(prevRevLFTOPFLIDs.containsKey(latestLFID)){
			    						 prevPFLIDList=(StringList)prevRevLFTOPFLIDs.get(latestLFID);
			    					 }else if(prevRevLFTOPFLIDs.containsKey(previousRevisionOID)){
			    						 prevPFLIDList=(StringList)prevRevLFTOPFLIDs.get(previousRevisionOID);
			    					 }
			    					 for(int q=0; q<prevPFLIDList.size();q++){
			    						 String prevPFLID = (String)prevPFLIDList.get(q);
			    						 //set to for PFL connection
			    						 if(prevPFLID!=null && latestLFRelID!=null){
			    							 plCommon.setToRelationship(context,prevPFLID,latestLFRelID,false);
			    							 pflActallyFloated.add(prevPFLID);
			    						 }
			    					 }
			    				 }else{
			    					 //----------------------------------------------------------------------------
			    					 //Case in which newly revised structure is modified to add LF structure in it.
			    					 // we need to setup PFL fo newly added LF in new revision
			    					 //----------------------------------------------------------------------------					 				
			    					 ProductLineCommon connectPFL = new ProductLineCommon(latestLFRelID);
			    					 RelationshipType relationtype = new RelationshipType(ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST);
			    					 //will use Non -Frozen Product list which was created 
			    					 String[] productIdsArray = (String[]) prevPFLIDListFinal.toArray(new String[prevPFLIDListFinal.size()]);
			    					 Map mapPFLDetails = connectPFL.connectObjects(context,relationtype,productIdsArray, true,false);
			    					 //for newly addded LF need to traverse to get all child LF with Rel IDs
			    					 ConfigurationUtil utilLFnewlyAdded = new ConfigurationUtil(latestLFID);
			    					 //also will need to traverse childs of newly added LF, to make PFL connection.
			    					 MapList newlyAddedListDown = utilLFnewlyAdded.getObjectStructure(context,strTypePattern,strRelPattern,
			    							 objectSelectables, null,false, true, 0,0,
			    							 DomainObject.EMPTY_STRING, DomainObject.EMPTY_STRING,(short) 1,DomainObject.EMPTY_STRING);		 							
			    					 for(int pr=0;pr<newlyAddedListDown.size();pr++){
			    						 Map newLFRelIDmp = (Map)newlyAddedListDown.get(pr);
			    						 String newLFRelIDmpLFRelID = (String)newLFRelIDmp.get(DomainObject.SELECT_RELATIONSHIP_ID);
			    						 ProductLineCommon connectChildPFL = new ProductLineCommon(newLFRelIDmpLFRelID);
			    						 connectChildPFL.connectObjects(context,relationtype,productIdsArray, true,false);
			    					 }
			    				 }
			    			 }
			    			 //----------------------------------------------------------------------------
			    			 //Case in which newly revised structure is modified to remove LF structure in it.
			    			 //PFL which are not float is the case when corrsponding LF is removed, and the old PFL will pointing to prev revision
			    			 //Need to explicitly remove this PFL
			    			 //----------------------------------------------------------------------------		 					
			    			 pfltoFloat.removeAll(pflActallyFloated);
			    			 for(int q1=0; q1<pfltoFloat.size();q1++){
			    				 String prevPFLID = (String)pfltoFloat.get(q1);
			    				 DomainRelationship.disconnect(context,prevPFLID);
			    			 }
			    		 }
			    	 }
			    	 catch (Exception e) {
			    		 throw new FrameworkException(e.getMessage());
			    	 }
			     }

				/**
				 * This Method is used to get the range HREF for second Configuration Feature
				 * in Structure Compare
				 * @param context the eMatrix <code>Context</code> object
				 * @param args - Holds the following arguments 0 - HashMap containing the following arguments
				 *               Object Id
				 *               Field Name
				 * @return String  - Range Href for Second Object
				 * @throws Exception if the operation fails
				 */
				public String getConfigurationFeatureTwoRangeHref(Context context, String[] args) throws Exception
				{
					String strTypes = EnoviaResourceBundle.getProperty(context,"Configuration.LogicalCompareTypesForObject2.type_ConfigurationFeature");
					return "TYPES="+strTypes;
				}


				/**
				 * This method is used to Synchronize the Logical Feature
				 * @param context the eMatrix <code>Context</code> object
				 * @param args 	   - Holds the following arguments 0 - HashMap containing the following arguments
				 *                   Object Id
				 *                   Document Element
				 * @return Map
				 * @throws Exception if the operation fails
				 */
			 	@com.matrixone.apps.framework.ui.ConnectionProgramCallable
			 	public Map synchronizeLogicalFeature(Context context, String[] args)throws Exception {
			 		HashMap returnMap = new HashMap();
			 		MapList mlItems = new MapList();
			 		HashMap doc = new HashMap();
			 		HashMap programMap = (HashMap) JPO.unpackArgs(args);
			 		//DomainObject domleftSideObject = new DomainObject(leftSideObjectID);
			 		Element elm = (Element) programMap.get("contextData");
			 		MapList chgRowsMapList = com.matrixone.apps.framework.ui.UITableIndented.getChangedRowsMapFromElement(context, elm);
			 		//DomainObject domrightSideObject = new DomainObject(rightSideObjectID);
			 		String strObjectID = (String) programMap.get("parentOID");
			 		for (int i = 0; i < chgRowsMapList.size(); i++) {
			 			HashMap tempMap = (HashMap) chgRowsMapList.get(i);
			 			String strRelId = (String) tempMap.get("relId");
			 			String strRowId = (String) tempMap.get("rowId");
			 			String childObjId = (String) tempMap.get("childObjectId");
			 			String markUpMode = (String) tempMap.get("markup");
			 		    HashMap colMap     = (HashMap) tempMap.get("columns");
			 		    StringList strAttrList = new StringList();
			 		    strAttrList.add(ConfigurationConstants.ATTRIBUTE_USAGE);
			 		    strAttrList.add(ConfigurationConstants.ATTRIBUTE_COMPONENT_LOCATION);
			 		    strAttrList.add(ConfigurationConstants.ATTRIBUTE_QUANTITY);
			 		    strAttrList.add(ConfigurationConstants.ATTRIBUTE_FIND_NUMBER);
			 		    strAttrList.add(ConfigurationConstants.ATTRIBUTE_REFERENCE_DESIGNATOR);
			 			if (markUpMode.equalsIgnoreCase("add")) {
			 				try {
			 					if(strObjectID!=null && childObjId!=null)
			 					{
			 						DomainObject domchildObjId = new DomainObject(childObjId);
			 						String strState = domchildObjId.getInfo(context,"current");
			 						if(strState!=null && strState.equalsIgnoreCase(ConfigurationConstants.STATE_OBSOLETE))
			 						{
			 							//Exception
				    					String language = context.getSession().getLanguage();
				    					String strAlertMessage = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
				    							"emxConfiguration.sync.Obsolete.Error",language);
				    					throw new FrameworkException(strAlertMessage);
			 						}
			 						else{
			 							DomainRelationship domRel = com.matrixone.apps.domain.DomainRelationship.
			 						    connect(context,new DomainObject(strObjectID),ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES,new DomainObject(childObjId));
			 							HashMap attributeMap = new HashMap(); 
			 							for(int j = 0; j < strAttrList.size() ;j++){
			 								if(colMap.containsKey(strAttrList.get(j))){
			 									attributeMap.put((String) strAttrList.get(j), (String) colMap.get(strAttrList.get(j)));
			 								}
			 							}
			 							domRel.setAttributeValues(context, attributeMap);
			 							
			 						}
			 					}
			 				} catch (Exception e) {
			 					
			 					String strErrMessage = e.toString();
			 	    			if(strErrMessage != null && strErrMessage.contains("#5000001:"))
			 	    			{
			 	    				int pos = strErrMessage.lastIndexOf("#5000001:");
			 	    				if (pos > -1)
			 	    				{
			 	    					strErrMessage = strErrMessage.substring(pos+9).trim();
			 	    				}
			 	    			}
			 	    			throw new FrameworkException(strErrMessage);
			 				}
			 			}
			 			else if (markUpMode.equalsIgnoreCase("cut")) {
			 				try {

			 					//Disconnect using strRelId
			 					if(strRelId!=null)
			 						com.matrixone.apps.domain.DomainRelationship.
			 						disconnect( context, strRelId);
			 				} catch (Exception e) {
			 					throw new FrameworkException(e.toString());

			 				}
			 			}

			 			returnMap.put("rowId", strRowId);
						returnMap.put("markup", markUpMode);
						returnMap.put("oid", childObjId);
						mlItems.add(returnMap);
						doc.put("Action", "success"); // Here the action can be
						// "Success" or "refresh"
						doc.put("changedRows", mlItems);// Adding the key
			 		}
			 		return doc;
			 	}



			 	/**
				 * This trigger method and it calls an Utility method to check if the passed business object is in frozen state or not
				 * It compare the current state and policy pair with the state and policy pair defined as frozen in configuration file
				 *
				 * @param context
				 * @param args
				 * @return - iReturn - 1 if object found in frozen state
				 * 				     - 0 otherwise.
				 * @throws FrameworkException
				 */
			 	public int checkForFrozenState(Context context, String[] args)throws Exception
				{
					String fromobjectid = args[1];
					ConfigurationUtil util = new ConfigurationUtil();
					int iReturn = 0;
					boolean frozen = util.isFrozenState(context, fromobjectid);
					if(frozen)
					{
    					String language = context.getSession().getLanguage();
    					String strAlertMessage =  EnoviaResourceBundle.getProperty(context, SUITE_KEY,
    							"emxProduct.Alert.LogicalFeatureReleased",language);
    					emxContextUtilBase_mxJPO.mqlNotice(context,
    							strAlertMessage);
    					iReturn = 1;
					}
					return iReturn;
				}
	/**
	 * This trigger is called on Product Revise, to make the PFL connection to the Logical Feature Structure.
	 *
	 * @param context
	 * @param args
	 * @return - iReturn - 1 if object found in frozen state
	 * 				     - 0 otherwise.
	 * @throws FrameworkException
	 */
	public int makePFLConnections(Context context, String[] args)throws Exception
	{
//		String strReviseWithEffectivity= PropertyUtil.getGlobalRPEValue(context,"ProductRevise");
//		int iReturn = 0;
//	 	try {
//	 		if(strReviseWithEffectivity==null || !strReviseWithEffectivity.equals("EditEffectivity")){
//	 			// getting the new Revision Object ID
//	 			String strProductRevisionId = args[0];
//				DomainObject domObjOldRev = new DomainObject(strProductRevisionId);
//				String strNextRevId = domObjOldRev.getNextRevision(context).getObjectId(context);
//
//				// Get the prevous Products PFL connections with FAT attribute details for cloning the attribute to new Revision
//				StringList strSelect = ConfigurationUtil.getBasicRelSelects(context);
//				strSelect.addElement("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
//				strSelect.addElement("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.to.id");
//				DomainConstants.MULTI_VALUE_LIST.add("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
//				DomainConstants.MULTI_VALUE_LIST.add("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.to.id");
//
//				StringBuffer sBufWhereCond = new StringBuffer(200);
//				sBufWhereCond.append("from.id==");
//				sBufWhereCond.append(strProductRevisionId);
//				// changes for IR-226509V6R2012x start
//				Map mPFLDetails = domObjOldRev.getInfo(context, strSelect);
//				StringList featureList =  (StringList) mPFLDetails.get("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.to.id");
//				StringList attributeFAT = (StringList) mPFLDetails.get("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
//				DomainConstants.MULTI_VALUE_LIST.remove("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
//				DomainConstants.MULTI_VALUE_LIST.remove("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.to.id");
//				ConfigurationUtil confUtil = new ConfigurationUtil();
//
//				StringList objSelectables = new StringList();
//				objSelectables.addElement("physicalid");
//
//				StringList sLstRelSelect = new StringList();
//				sLstRelSelect.addElement("tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id");
//
//				StringBuffer sbRelWher = new StringBuffer(16);
//				sbRelWher.append("from.type.kindof !=\"");
//                sbRelWher.append(ConfigurationConstants.TYPE_PRODUCTS);
//                sbRelWher.append("\"");
//                sbRelWher.append("||");
//                sbRelWher.append("from.id==");
//                sbRelWher.append(strNextRevId);
//				LogicalFeature logicalfeature = new LogicalFeature(strNextRevId);
//				MapList mapLogicalStructure = logicalfeature.getLogicalFeatureStructure(context,null, null, objSelectables, sLstRelSelect, false,
//						true,0,0,DomainObject.EMPTY_STRING,"", DomainObject.FILTER_STR_AND_ITEM,DomainObject.EMPTY_STRING);
//
//				RelationshipType relationtype = new RelationshipType(ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST);
//
//				StringList idConnectionStruct = new StringList();
//				for (int i = 0; i < mapLogicalStructure.size(); i++) {
//	                Map tempMap = (Map) mapLogicalStructure.get(i);
//	                String strLFRelId = (String) tempMap.get(DomainRelationship.SELECT_ID);
//	                String strLFId = (String) tempMap.get(DomainObject.SELECT_ID);
//	                StringList strPFLConnections = confUtil.convertObjToStringList(context,
//	                							tempMap.get("tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id"));
//	                String idConnection = (String) tempMap.get(DomainObject.SELECT_RELATIONSHIP_ID);
//	                if(!strPFLConnections.contains(strNextRevId)&& (idConnectionStruct.isEmpty() || !idConnectionStruct.contains(idConnection))){
//		                ProductLineCommon connectPFLallproducts = new ProductLineCommon(strLFRelId);
//						String strConnection = connectPFLallproducts.connectObject(context,relationtype,strNextRevId, true);
//						//traverse the previous product revisions PFL rel and get the attribute
//						for(int j=0; j<featureList.size();j++){
//							String strPFLLFId = (String) featureList.get(j);
//							if(strLFId.equals(strPFLLFId)){
//								String strFATAttibute = (String) attributeFAT.get(j);
//								DomainRelationship domRel = new DomainRelationship(strConnection);
//								domRel.setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE,strFATAttibute);
//								// changes for IR-226509V6R2012x end
//							}
//						}
//						idConnectionStruct.add(idConnection);
//	                }
//				}
//	 		}
//		} catch (Exception e) {
//			throw new FrameworkException(e.getMessage());
//		}
		return 0;
	}
			 	/**
				 * It is a trigger method. Fires when a instance of "Logical Features" is deleted/removed.
				 * It is used to disconnection Product Feature List connection between "Logical Features" instance and Products
				 * @param context
				 *            The ematrix context object.
				 * @param String[]
				 *            The args .
				 * @return
				 */
			 	public void disconnectPFLConnections(Context context, String[] args)throws Exception
			 	{
			 		String fromobjectid = args[0];
			 		String toobjectid = args[1];
			 		StringList prodcutVariantList = new StringList();
			 		try{
							DomainObject domObject = new DomainObject(fromobjectid);
							String strWhere = null;
							if(domObject.isKindOf(context, ConfigurationConstants.TYPE_PRODUCTS))
							{
								prodcutVariantList.add(fromobjectid);
								//get Product Variants connected to Product
								ProductVariant pvutil = new ProductVariant(fromobjectid);
								MapList pvList = pvutil.getProductVariants(context, fromobjectid);
								for(int p=0;p<pvList.size();p++)
								{
									Map pvMap = (Map)pvList.get(p);
									String pvID = (String)pvMap.get(DomainObject.SELECT_ID);
									prodcutVariantList.add(pvID);
								}
							}
							String strProductId ="tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+
									"]."+DomainConstants.SELECT_FROM_ID;
							String strRelease = FrameworkUtil.lookupStateName(context,
									ProductLineConstants.POLICY_PRODUCT, strSymbReleaseState);
							String strObselete = FrameworkUtil.lookupStateName(context,
									ProductLineConstants.POLICY_PRODUCT, strSymbObsoleteState);
							String strProductState ="tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+
									"].from.current";

							short filterFlag = 0;
							StringList typeattributeList = new StringList();
							StringList relattributeList = new StringList();
							String strPFLId = "tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+
									"]."+DomainConstants.SELECT_ID;
							DomainConstants.MULTI_VALUE_LIST.add(strPFLId);
							DomainConstants.MULTI_VALUE_LIST.add(strProductId);
							DomainConstants.MULTI_VALUE_LIST.add(strProductState);
							relattributeList.add(strPFLId);
							relattributeList.add(strProductId);
							relattributeList.add(strProductState);
							//Type pattern only Logical Feature
							String strtypepattern = ConfigurationConstants.TYPE_LOGICAL_FEATURE;
							//Rel pattern only Logical Features
							String strrelpattern = ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES;
							/*Check DisConnected Logical Feature contain sub Features
						   If Yes, DisConnect the sub Features PFL connections */
							ConfigurationUtil confgiUtil = new ConfigurationUtil(toobjectid);
							MapList mapListLogicStruc = confgiUtil.getObjectStructure(context, strtypepattern,strrelpattern,
									typeattributeList, relattributeList,false, true, 0,0,null, strWhere, filterFlag, null);
							ConfigurationUtil confUtil = new ConfigurationUtil();
							String pflFromIDOfRelToDisconnected = PropertyUtil.getGlobalRPEValue(context,"PLF_FROM_IDS");
							StringList slpflFromIDOfRelToDisconnected=FrameworkUtil.split(pflFromIDOfRelToDisconnected,","); 
							for(int i= 0; i<mapListLogicStruc.size(); i++)
							{
								Map mapLogicStruc = (Map)mapListLogicStruc.get(i);
								StringList pflIDList = confUtil.convertObjToStringList(context, mapLogicStruc.get(strPFLId));
								StringList productIDList = confUtil.convertObjToStringList(context, mapLogicStruc.get(strProductId));
								StringList productCurrentList = confUtil.convertObjToStringList(context, mapLogicStruc.get(strProductState));
								if(pflIDList!=null && pflIDList.size()>0)
								{
									for(int j=0;j<pflIDList.size();j++)
									{
										//DisConnect the PFL IDs
										String strRelId = (String)pflIDList.get(j);
										String productID = (String)productIDList.get(j);
										String prdOrPVState = (String)productCurrentList.get(j);
										if(pflFromIDOfRelToDisconnected.contains(productID)
												&& !prdOrPVState.equals(strObselete)&&!prdOrPVState.equals(strRelease)){
											if(domObject.isKindOf(context, ConfigurationConstants.TYPE_PRODUCTS))
											{
												if(prodcutVariantList.contains(productID))
													if(strRelId!=null)
														com.matrixone.apps.domain.DomainRelationship.disconnect( context, strRelId);
											}
											else{
												if(strRelId!=null)
													com.matrixone.apps.domain.DomainRelationship.disconnect( context, strRelId);
											}
										}
									}
								}
							}
							DomainConstants.MULTI_VALUE_LIST.remove(strPFLId);
							DomainConstants.MULTI_VALUE_LIST.remove(strProductId);
			 		}

			 		catch (Exception e) {
			 			throw new FrameworkException(e.getMessage());
			 		}
			 	}
	     /**
	      * This is a background job method called to do the roll up process of Valid and Invalid condidions for Product Variant Context
	      *
	      * @param context
	      * @param args
	      * @throws Exception
	      * @exclude
	      */
	     public void rollUpDesignVariantOnPVCreate(Context context,  String args[]) throws Exception {
	    	 try {
    	        HashMap mapArgs = (HashMap) JPO.unpackArgs(args);
    			HashMap hMapPVPFLs = new HashMap();

    			String strPVID = mapArgs.get("ProductVariantId").toString();
    			String strProductRevisionId = mapArgs.get("ProductRevisionId").toString();
    			String strSelectedLFIds = mapArgs.get("SelectedLFIds").toString();
    			hMapPVPFLs =(HashMap)mapArgs.get("PVPFLRelIds");

    		 	ProductVariant productVariant = new ProductVariant(strPVID);
    			ContextUtil.startTransaction(context, true);
    			productVariant.rollUpDesignVariant(context,strSelectedLFIds,strProductRevisionId,hMapPVPFLs,true);
    			productVariant.rollDesignVariantsToProductVariantOnCreate(context);
    			ContextUtil.commitTransaction(context);
			} catch (Exception e) {
				throw new FrameworkException(e.getMessage());
			}
	     }
	     /**
		     * This method is used to return the Name of the Master Composition with Hyper link of Logical Feature Table
		     *
		     * @param context
		     *            the eMatrix <code>Context</code> object
		     * @param args
		     *            holds arguments
		     * @return Vector- the List of Strings containing Master Composition name.
		     * @throws FrameworkException
		     *             if the operation fails
		     */
	     public Vector getMasterCompositionLink(Context context,  String args[]) throws Exception {
	    	 Vector partList = new Vector();
	    	 try {

    	        HashMap programMap = (HashMap) JPO.unpackArgs(args);
    			MapList objectMapList =(MapList)programMap.get("objectList");
    			HashMap paramMap = (HashMap)programMap.get("paramList");
    			String reportFormat = (String)paramMap.get("reportFormat");
		    	String suiteDir = (String) paramMap.get("SuiteDirectory");
		    	String suiteKey = (String) paramMap.get("suiteKey");
		    	String strMasterFeatureName = null;
    			String strMasterFeatureID = null;
    			String attrDisplayName = "to["+ConfigurationConstants.RELATIONSHIP_MANAGED_REVISION+ "]."+DomainObject.SELECT_FROM_NAME;
    			String attrMarketName = "to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+ "]."+DomainObject.SELECT_FROM_NAME;
        		String attrMarketFeatueID = "to["+ConfigurationConstants.RELATIONSHIP_MANAGED_REVISION+ "]."+DomainObject.SELECT_FROM_ID;
        		String attrModelID = "to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+ "]."+DomainObject.SELECT_FROM_ID;
        		String strImage = null;
		    	for(int i=0;i<objectMapList.size();i++)
		    	{
		    		String output = "";
		    		Map objectMap = (Map) objectMapList.get(i);
		    		String strLogicalFeatureID = (String)objectMap.get(DomainConstants.SELECT_ID);
		    		DomainObject domObject = new DomainObject(strLogicalFeatureID);
		    		if (domObject.exists(context)
					&& (domObject.isKindOf(context,
							ConfigurationConstants.TYPE_PRODUCTS)
							|| domObject.isKindOf(context,
									ConfigurationConstants.TYPE_MODEL) || domObject
							.isKindOf(
									context,
									ConfigurationConstants.TYPE_PRODUCT_LINE)))
		    		{

		    			strMasterFeatureName = (String) objectMap.get(attrMarketName);
		    			strImage = "images/iconSmallProduct.gif";
		    			strMasterFeatureID = (String) objectMap.get(attrModelID);
		    			if(!(strMasterFeatureName!=null && !strMasterFeatureName.equals("")))
		    			{
		    				Map tempMap = getMasterComposition(context,strLogicalFeatureID,false);

		    				if(objectMap.containsKey("to["+ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT+ "]."+DomainObject.SELECT_FROM_ID)){
		    					strMasterFeatureName = (String)tempMap.get("to["+ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT+ "]."+DomainObject.SELECT_FROM_NAME);
			    				strMasterFeatureID = (String)tempMap.get("to["+ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT+ "]."+DomainObject.SELECT_FROM_ID);

		    				}else{
		    					strMasterFeatureName = (String)tempMap.get(attrMarketName);
		    					strMasterFeatureID = (String)tempMap.get(attrModelID);
		    				}
		    			}

		    		}
		    		else
		    		{
		    			strMasterFeatureName = (String) objectMap.get(attrDisplayName);
		    			strImage = "images/iconSmallMasterFeature.gif";
		    			strMasterFeatureID = (String) objectMap.get(attrMarketFeatueID);
		    			if(!(strMasterFeatureName!=null && !strMasterFeatureName.equals("")))
		    			{
		    				Map tempMap = getMasterComposition(context,strLogicalFeatureID,true);
		    				strMasterFeatureName = (String)tempMap.get(attrDisplayName);
		    				strMasterFeatureID = (String)tempMap.get(attrMarketFeatueID);
		    			}
		    		}
		    		if(strMasterFeatureName!=null)
		    		{
		    			StringBuffer temp = new StringBuffer();
		    			if (reportFormat != null && !("null".equalsIgnoreCase(reportFormat)) && reportFormat.length()>0){
		    				temp.append(strMasterFeatureName);
						}else{
							temp.append(" <img border=\"0\" src=\"");
							temp.append(strImage);
							temp.append("\" /> ");
							temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
							temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteDir));
							temp.append("&amp;suiteKey=");
							temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteKey));
							temp.append("&amp;objectId=");
							temp.append(XSSUtil.encodeForHTMLAttribute(context, strMasterFeatureID));
							temp.append("', '450', '300', 'true', 'popup')\">");
							temp.append(XSSUtil.encodeForXML(context,strMasterFeatureName));
							temp.append("</a>");
						}
						output =  temp.toString();
						if(output.length()>0)
	    					partList.add(output);
		    		}
		    		else{
		    			partList.add(DomainObject.EMPTY_STRING);
		    		}

		    	}


			} catch (Exception e) {
				throw new FrameworkException(e.getMessage());
			}
			return partList;
	     }


	     /**
			 * Its is used to get the Master Feature for Logical Feature and Model name for Products
			 * @param context
			 * @param objectID -- Logical Feature ID/Product ID
			 * @param displayName - boolean, true for LF and false for Products
			 * @return Master Feature name for LF and Model Name for Products
			 * @throws FrameworkException
			 */
			private Map getMasterComposition (Context context, String objectID, boolean displayName)throws FrameworkException{
				Map strMasterCompMap = new HashMap();
				try{
					DomainObject domObject = new DomainObject(objectID);
					String attrDisplayName = "to["+ConfigurationConstants.RELATIONSHIP_MANAGED_REVISION+ "]."+DomainObject.SELECT_FROM_NAME;
	    			String attrMarketName = "to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+ "]."+DomainObject.SELECT_FROM_NAME;
	        		String attrMarketFeatueID = "to["+ConfigurationConstants.RELATIONSHIP_MANAGED_REVISION+ "]."+DomainObject.SELECT_FROM_ID;
	        		String attrModelID = "to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+ "]."+DomainObject.SELECT_FROM_ID;
					StringList selectables = new StringList();

	    			if(displayName){
	    				selectables.addElement(attrDisplayName);
	    				selectables.addElement(attrMarketFeatueID);}

	    			else{
	    				selectables.addElement(attrMarketName);
	    				selectables.addElement(attrModelID);}
	    				strMasterCompMap = domObject.getInfo(context, selectables);


				}
				catch (Exception e) {
					throw new FrameworkException(e);
				}
				return strMasterCompMap;
			}


	/**
	 * This Methods is used to get the Product which are connected as Logical Features. This is used
	 * Filter for PCR
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the id of Top Level Logical Features
	 * @throws Exception
	 *             if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getProductsAsLogicalFeatures(Context context, String[] args)
			throws Exception {

		MapList mapPrdsAsLFStructure = null;
		int limit = 32767; // Integer.parseInt(FrameworkProperties.getProperty(context,"emxConfiguration.Search.QueryLimit"));
		
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strObjectid = (String)programMap.get("objectId");
			String sNameFilterValue = (String) programMap
					.get("FTRLogicalFeatureNameFilterCommand");
			String sLimitFilterValue = (String) programMap
					.get("FTRLogicalFeatureLimitFilterCommand");
			boolean isCalledFromRule=false;
		  	if(sNameFilterValue==null){
		  		sNameFilterValue = (String) programMap.get("FTRLogicalFeatureNameFilterForRuleDialog");
		  		if(sNameFilterValue!=null) isCalledFromRule= true;
		  	}
            if(sLimitFilterValue==null)
            	sLimitFilterValue = (String) programMap.get("FTRLogicalFeatureLimitFilterForRuleDialog");

			if (sLimitFilterValue != null
					&& !(sLimitFilterValue.equalsIgnoreCase("*"))) {
				if (sLimitFilterValue.length() > 0) {
					limit = (short) Integer.parseInt(sLimitFilterValue);
					if (limit < 0) {
						limit = 32767;
					}
				}
			}
            String strObjWhere="";
			if (sNameFilterValue != null
					&& !(sNameFilterValue.equalsIgnoreCase("*"))) {

				strObjWhere = "attribute["
					+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME
					+ "] ~~ '" + sNameFilterValue + "'";
			}
			if(isCalledFromRule){
				if(!strObjWhere.trim().isEmpty())
					strObjWhere=strObjWhere+" && "+RuleProcess.getObjectWhereForRuleTreeExpand(context,ConfigurationConstants.TYPE_LOGICAL_FEATURE);
				else
					strObjWhere=RuleProcess.getObjectWhereForRuleTreeExpand(context,ConfigurationConstants.TYPE_LOGICAL_FEATURE);
			}
			// call method to get the level details
			int iLevel = ConfigurationUtil.getLevelfromSB(context,args);
			String filterExpression = (String) programMap.get("CFFExpressionFilterInput_OID");
			LogicalFeature cfBean = new LogicalFeature(strObjectid);
			String strTypePattern = ConfigurationConstants.TYPE_SOFTWARE_PRODUCT+","+ConfigurationConstants.TYPE_HARDWARE_PRODUCT+","+ConfigurationConstants.TYPE_SERVICE_PRODUCT;
			StringList objSelectables= new StringList(DomainObject.SELECT_ID);
			objSelectables.add(DomainObject.SELECT_TYPE);
			objSelectables.add(DomainObject.SELECT_NAME);

			StringList relSelect= new StringList(DomainRelationship.SELECT_ID);
			mapPrdsAsLFStructure = (MapList)cfBean.getLogicalFeatureStructure(context,strTypePattern, null, objSelectables, relSelect, false,
					true,iLevel,limit, strObjWhere, DomainObject.EMPTY_STRING, DomainObject.FILTER_STR_AND_ITEM,filterExpression);

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return mapPrdsAsLFStructure;
	}


	/**
	 * This method is an Update Function for Force Part Reuse attribute
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void updateForcePartReuse(Context context, String[] args)
			throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String strRelId = (String) paramMap.get("relId");
		String strNewAttributeValue = (String) paramMap.get("New Value");
		DomainRelationship domRel = new DomainRelationship(strRelId);
		// For IR-271846V6R2015, added Push - Pop Context to get modify access on Attribute "Force Part Reuse" for user.
		ContextUtil.pushContext(context, PropertyUtil
                .getSchemaProperty(context, "person_UserAgent"),
                DomainConstants.EMPTY_STRING,
                DomainConstants.EMPTY_STRING);
		domRel.setAttributeValue(context,
				ConfigurationConstants.ATTRIBUTE_FORCE_PART_REUSE,
				strNewAttributeValue);
		ContextUtil.popContext(context);
		StringList relInfoList = new StringList();
		relInfoList.addElement(SELECT_TO_ID);
		relInfoList.addElement("tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id");
		Map relInfoMap = domRel.getRelationshipData(context, relInfoList);
		String strFeatureId = (String) ((StringList) relInfoMap.get(SELECT_TO_ID)).get(0);

		if(!((StringList)relInfoMap.get("tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id")).isEmpty()){

			String strPrdId = (String)((StringList)relInfoMap.get("tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id")).get(0);
			ProductConfiguration.deltaUpdateBOMXMLAttributeOnPC(context,
					strPrdId+"|"+strFeatureId, "GBOMUpdate");

		}else{

			ProductConfiguration.deltaUpdateBOMXMLAttributeOnPC(context,
					strFeatureId, "GBOMUpdate");
		}
	}

    /**
	 * This is a Access Function to Standard/Custom actions column in Preview BOM page.
	 *
	 * @param context - the eMatrix <code>Context</code> object
	 * @return bResult - true  : if emxConfiguration.PreviewBOM.EnableCustomPartMode =true
	 * 					 false
	 * @throws FrameworkException  - throws Exception if any operation fails.
	 * @since R212
	 */
	public Object isForcePartEnable(Context context, String[] args)
			throws FrameworkException {
		try {
			String strForcePartMode = EnoviaResourceBundle.getProperty(context,
					"emxConfiguration.PreviewBOM.ForcePartReuse.Enabled");
			if (ProductLineCommon.isNotNull(strForcePartMode)
					&& strForcePartMode.equalsIgnoreCase("true")) {
				return Boolean.valueOf(true);
			} else {
				return Boolean.valueOf(false);
			}
		} catch (Exception e) {
			return Boolean.valueOf(false);
		}
	}
	     /**
	      * This is a Access Function for Design Usage Column, if the Context is Logical Features then this will be hidden
	      * else will be displayed.
	      *
	      * @param context
	      * @param args
	      * @return bResult - true if no Logical Featuer
	      * 				   false if Logical Feature Context
	      * @throws Exception
	      */
	     public boolean showDesignUsage(Context context, String[] args)
	     throws Exception {
	    	boolean bResult = true;

  		  	Map programMap = (Map) JPO.unpackArgs(args);
	    	 if(programMap.get("parentOID")!=null){
    		String strCntxtProductId = programMap.get("parentOID").toString();
    		DomainObject dom = new DomainObject(strCntxtProductId);

    		if(mxType.isOfParentType(context, dom.getInfo(context,DomainObject.SELECT_TYPE),
					ConfigurationConstants.TYPE_LOGICAL_STRUCTURES)){
    			bResult = false;
    		}
	    	 }
	    	 else
	    	 {
	    		 bResult = false;
	    	 }
	     	return bResult;
     	}
	public static int updateSubFeatureCountForAddition(Context context, String []args) throws FrameworkException{
		try{
			  String fromObjectID = args[1];

	          StringList selectables = new StringList();
	          selectables.add(ConfigurationConstants.SELECT_TYPE);
	          selectables.add(ConfigurationConstants.SELECT_ATTRIBUTE_SUBFEATURECOUNT );

	          DomainObject fromObject = new DomainObject(fromObjectID);
	          Map selectableList = fromObject.getInfo(context,selectables);

	          String strType = (String)selectableList.get(ConfigurationConstants.SELECT_TYPE);
	          String subFeatureCount = "";

	          if(strType!=null && mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES)){
	        	  subFeatureCount = (String)selectableList.get(ConfigurationConstants.SELECT_ATTRIBUTE_SUBFEATURECOUNT);
	        	  int count = Integer.parseInt(subFeatureCount);
	        	  count = count + 1;
	        	  fromObject.setAttributeValue(context, ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT, String.valueOf(count));
	          }
		}
		catch(Exception e){
			throw new FrameworkException(e);
		}
        return 0;
	}
	public static int updateSubFeatureCountForDeletion(Context context, String []args) throws FrameworkException{
		try{
			  String fromObjectID = args[1];

	          StringList selectables = new StringList();
	          selectables.add(ConfigurationConstants.SELECT_TYPE);
	          selectables.add(ConfigurationConstants.SELECT_ATTRIBUTE_SUBFEATURECOUNT);

	          DomainObject fromObject = new DomainObject(fromObjectID);
	          Map selectableList = fromObject.getInfo(context,selectables);

	          String strType = (String)selectableList.get(ConfigurationConstants.SELECT_TYPE);
	          String subFeatureCount = "";

	          if(strType!=null && mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES)){
	        	  subFeatureCount = (String)selectableList.get(ConfigurationConstants.SELECT_ATTRIBUTE_SUBFEATURECOUNT);
	        	  int count = Integer.parseInt(subFeatureCount);
	        	  count = count - 1;
	        	  fromObject.setAttributeValue(context, ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT, String.valueOf(count));
	          }
		}
		catch(Exception e){
			throw new FrameworkException(e);
		}
        return 0;
	}
	/**
	 * returns level from the objectlist of program,used as column JPO for
	 * dependency report
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	 public List getLevel(Context context, String[] args)
	 throws Exception {
		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
		 MapList lstObjectIdsList = (MapList) programMap.get("objectList");
		 String level="";

		 int size = lstObjectIdsList.size();
		 List resultList = new StringList(size);
		 for (int i = 0; i < size; i++)
		 {
			 Map tempMap = (Map)lstObjectIdsList.get(i);
			 level= (String)tempMap.get("level");
			 if(!ConfigurationUtil.isObjectNull(context, level)){
				 resultList.add(level);
			 }else{
				 resultList.add("");
			 }
		 }
		 return resultList;
	 }
	 /**
	 * Returns the Maplist containing the Products which are connected to the
	 * product used in dependency reports
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	 @com.matrixone.apps.framework.ui.ProgramCallable
	 public MapList getDependencyReport(Context context, String[] args)
	 throws Exception {

		MapList dependecyProds = null;

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strObjectId = "";
			String strMode = (String) programMap.get("mode");
			if (strMode.equals("PropertyPage")) {
				strObjectId = (String) programMap.get("objectId");
			} else if (strMode.equals("ListPage")) {
				strObjectId = (String) programMap.get("emxTableRowId");

			}
			String filterExpression = (String) programMap
					.get("CFFExpressionFilterInput_OID");
			LogicalFeature cfBean = new LogicalFeature(strObjectId);

			StringList objSelectables = new StringList(DomainObject.SELECT_ID);
			objSelectables.add(DomainObject.SELECT_TYPE);
			objSelectables.add(DomainObject.SELECT_NAME);

			StringList relSelect = new StringList(DomainRelationship.SELECT_ID);
			String strTypePattern = ConfigurationConstants.TYPE_LOGICAL_STRUCTURES+","+ConfigurationConstants.TYPE_PRODUCTS;
			String strRelPattern = ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES+","+ConfigurationConstants.RELATIONSHIP_MANUFACTURING_STRUCTURES;
			dependecyProds = (MapList) cfBean.getLogicalFeatureStructure(
					context, strTypePattern, strRelPattern, objSelectables, relSelect, false,
					true, 0, 0, DomainObject.EMPTY_STRING,
					DomainObject.EMPTY_STRING, DomainObject.FILTER_STR_AND_ITEM,
					filterExpression);

			//remove Maps which are not of type Products
			List objListToRemove= new MapList();
			for (int i = 0; i < dependecyProds.size(); i++) {
				if (!mxType
						.isOfParentType(context, (String) ((Map) dependecyProds
								.get(i)).get(SELECT_TYPE),
								ProductLineConstants.TYPE_PRODUCTS)) {
					objListToRemove.add((Map) dependecyProds
							.get(i));
				}
			}
			dependecyProds.removeAll(objListToRemove);
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return dependecyProds;
	}


	 /**
	  * This method is called from Trigger of LF relationship create for connecting selected option and update BOM XML
	  * @param context
	  * @param args
	  * @return
	  * @throws FrameworkException
	  */
	 public  int connectSelectedOptions(Context context, String []args) throws FrameworkException{
		 try{
			 String relID = args[0];
			 String fromObjectID = args[1];
			 String toObjectID = args[2];

			 //get logical structure of toObjectID
			 LogicalFeature lfObj = new LogicalFeature(toObjectID);
			 StringList slObjSelects = new StringList();
			 slObjSelects.addElement(SELECT_ID);
			 StringList slRelSelects = new StringList();
			 slRelSelects.addElement(SELECT_RELATIONSHIP_ID);
			 MapList lfList = lfObj.getLogicalFeatureStructure(context,
					                          null,
					                          null,
					                          slObjSelects,
					                          slRelSelects,
					                          false,
					                          true,
					                          0,
					                          0,
					                          null,
					                          null,
					                          (short)0,
					                          null);
			 StringList lfRelIds = new StringList();
			 StringList lfIds = new StringList();
			 Iterator lfListItr = lfList.iterator();
			 while(lfListItr.hasNext())
			 {
				 Map lfInfoMap = (Map)lfListItr.next();
				 String lfRelId = (String)lfInfoMap.get(SELECT_RELATIONSHIP_ID);
				 String lfId = (String)lfInfoMap.get(SELECT_ID);
				 lfRelIds.addElement(lfRelId);
				 lfIds.addElement(lfId);
			 }
			 lfRelIds.addElement(relID);
			 lfIds.addElement(toObjectID);


			 StringList objectSelects = new StringList();
			 objectSelects.addElement("to["+ ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+ "].tomid["+ ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].from.id");
			 objectSelects.addElement("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].to.id");
			 objectSelects.addElement("to["+ ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+ "].tomid["+ ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].from.to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.type");
			 objectSelects.addElement("to["+ ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+ "].tomid["+ ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].from.to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.id");

			 DomainObject.MULTI_VALUE_LIST.add("to["+ ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+ "].tomid["+ ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].from.id");
			 DomainObject.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+ "].to.id");
			 DomainObject.MULTI_VALUE_LIST.add("to["+ ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+ "].tomid["+ ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].from.to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.type");
			 DomainObject.MULTI_VALUE_LIST.add("to["+ ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+ "].tomid["+ ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].from.to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.id");


			 MapList relatedPCsList = DomainObject.getInfo(context,	new String[]{fromObjectID}, objectSelects);
			 DomainObject.MULTI_VALUE_LIST.remove("to["+ ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+ "].tomid["+ ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].from.id");
			 DomainObject.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION + "].to.id");
			 DomainObject.MULTI_VALUE_LIST.remove("to["+ ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+ "].tomid["+ ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].from.to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.type");
			 DomainObject.MULTI_VALUE_LIST.remove("to["+ ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+ "].tomid["+ ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].from.to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.id");

			 Iterator relatedPCsListItr = relatedPCsList.iterator();
			 while(relatedPCsListItr.hasNext())
			 {
				 Map pcMap = (Map)relatedPCsListItr.next();
				 StringList selOptPCList = ConfigurationUtil.convertObjToStringList(context, pcMap.get(objectSelects.get(0)));
				 StringList contextPCList = ConfigurationUtil.convertObjToStringList(context, pcMap.get(objectSelects.get(1)));
				 StringList selOptPCContextTypesList = ConfigurationUtil.convertObjToStringList(context, pcMap.get(objectSelects.get(2)));
				 StringList selOptPCContextIdsList = ConfigurationUtil.convertObjToStringList(context, pcMap.get(objectSelects.get(3)));

				 Iterator selOptPCListItr = selOptPCList.iterator();
				 Iterator contextPCListItr = contextPCList.iterator();
				 Iterator selOptPCContextTypesListItr = selOptPCContextTypesList.iterator();
				 Iterator selOptPCContextIdsListItr = selOptPCContextIdsList.iterator();


				 while(selOptPCListItr.hasNext())
				 {
					 String strPCId = (String)selOptPCListItr.next();
					 String strPCContextType = (String)selOptPCContextTypesListItr.next();
					 selOptPCContextIdsListItr.next();
					 if(!mxType.isOfParentType(context, strPCContextType, ConfigurationConstants.TYPE_PRODUCT_VARIANT))
					 {
						 RelationshipType relationtype = new RelationshipType(ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS);
						 Iterator lfRelIdsItr = lfRelIds.iterator();
						 while(lfRelIdsItr.hasNext())
						 {
							 String lfRelId = (String)lfRelIdsItr.next();
							 ProductLineCommon connectSelectedOption = new ProductLineCommon(lfRelId);
							connectSelectedOption.connectObject(context,relationtype,strPCId, true);
						 }

                         Job job = new Job();
						 String[] arrJPOArgs = new String[3];
						 arrJPOArgs[0] = strPCId;
						 arrJPOArgs[1] = lfIds.toString();
						 arrJPOArgs[2] = "AddFeatureUpdate";
						 job = new Job("emxProductConfigurationEBOM","deltaUpdateBOMXMLForProductConfiguration", arrJPOArgs );
						 job.setContextObject(strPCId);
						 job.setTitle(" Delta Update BOM XML");
						 job.setDescription("Updating the BOM XML for related Product Configuration");
						 job.createAndSubmit(context);


					 }



				 }

				 while(contextPCListItr.hasNext())
				 {
					 String strPCId = (String)contextPCListItr.next();
					 RelationshipType relationtype = new RelationshipType(ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS);
					 Iterator lfRelIdsItr = lfRelIds.iterator();
					 while(lfRelIdsItr.hasNext())
					 {
						 String lfRelId = (String)lfRelIdsItr.next();
						 ProductLineCommon connectSelectedOption = new ProductLineCommon(lfRelId);
						 connectSelectedOption.connectObject(context,relationtype,strPCId, true);
					 }
				 }
			 }
		}
		 catch(Exception e){
			 throw new FrameworkException(e);
		 }
		 return 0;
	 }

	 /**
		 * This method is an Update Function on edit of Usage
		 * @param context
		 * @param args
		 * @throws Exception
		 */
		public void updateUsage(Context context, String[] args)
				throws Exception {

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String strRelId = (String) paramMap.get("relId");
			String strNewAttributeValue = (String) paramMap.get("New Value");
			DomainRelationship domRel = new DomainRelationship(strRelId);
			domRel.setAttributeValue(context,
					ConfigurationConstants.ATTRIBUTE_USAGE,
					strNewAttributeValue);
			StringList relInfoList = new StringList();
			relInfoList.addElement(SELECT_TO_ID);
			Map relInfoMap = domRel.getRelationshipData(context, relInfoList);
			String strFeatureId = (String) ((StringList) relInfoMap.get(SELECT_TO_ID)).get(0);
			ProductConfiguration.deltaUpdateBOMXMLAttributeOnPC(context, strFeatureId, "UsageUpdate");

		}

		 /**
		  * This method is called from Trigger of LF relationship delete for disconnecting selected option and update BOM XML
		  * @param context
		  * @param args
		  * @return
		  * @throws FrameworkException
		  */
		 public  int disconnectSelectedOptions(Context context, String []args) throws FrameworkException{
			 try{
				 String fromObjectID = args[0];
				 String toObjectID = args[1];

				 //get logical structure of toObjectID
				 LogicalFeature lfObj = new LogicalFeature(toObjectID);
				 StringList slObjSelects = new StringList();
				 slObjSelects.addElement(SELECT_ID);
				 StringList slRelSelects = new StringList();
				 slRelSelects.addElement(SELECT_RELATIONSHIP_ID);
				 slRelSelects.addElement("tomid["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].id");
				 slRelSelects.addElement("tomid["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].from.id");
				 MapList lfList = lfObj.getLogicalFeatureStructure(context,
						                          null,
						                          null,
						                          slObjSelects,
						                          slRelSelects,
						                          false,
						                          true,
						                          0,
						                          0,
						                          null,
						                          null,
						                          (short)0,
						                          null);
				 StringList lfIds = new StringList();
				 Iterator lfListItr = lfList.iterator();
				 while(lfListItr.hasNext())
				 {
					 Map lfInfoMap = (Map)lfListItr.next();
					 String lfId = (String)lfInfoMap.get(SELECT_ID);
					 lfIds.addElement(lfId);
				 }

				 lfIds.addElement(toObjectID);

				 StringList objectSelects = new StringList();
				 objectSelects.addElement("to["+ ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+ "].tomid["+ ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].from.id");
				 objectSelects.addElement("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].to.id");

				 DomainObject.MULTI_VALUE_LIST.add("to["+ ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+ "].tomid["+ ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].from.id");
				 DomainObject.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+ "].to.id");

				 MapList relatedPCsList = DomainObject.getInfo(context,	new String[]{fromObjectID}, objectSelects);
				 DomainObject.MULTI_VALUE_LIST.remove("to["+ ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+ "].tomid["+ ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].from.id");
				 DomainObject.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION + "].to.id");

				 if (relatedPCsList.size() > 0)
				 {
					 Map mp = (Map) relatedPCsList.get(0);
					 StringList pcList = new StringList();
					 pcList.addAll(ConfigurationUtil.convertObjToStringList(context, mp.get(objectSelects.get(0))));
					 pcList.addAll(ConfigurationUtil.convertObjToStringList(context, mp.get(objectSelects.get(1))));
					 for (int i = 0; i < pcList.size(); i++) {
						 String strPCId = (String) pcList.get(i);
						 Iterator lfListItr1 = lfList.iterator();
						 while(lfListItr1.hasNext())
						 {
							 Map lfInfoMap = (Map)lfListItr1.next();
							 Object objSelOptPCList = lfInfoMap.get("tomid["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].from.id");
							 StringList selOptPCList = new StringList();
							 StringList selOptRelIdList = new StringList();
							 if(!ConfigurationUtil.isObjectNull(context, objSelOptPCList))
							 {
								 if(objSelOptPCList instanceof StringList)
								 {
									 selOptPCList = (StringList)lfInfoMap.get("tomid["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].from.id");
									 selOptRelIdList = (StringList)lfInfoMap.get("tomid["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].id");
								 }else if(objSelOptPCList instanceof String)
								 {
									 selOptPCList.addElement((String)lfInfoMap.get("tomid["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].from.id"));
									 selOptRelIdList.addElement((String)lfInfoMap.get("tomid["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].id"));
								 }
							 }
							 if(selOptPCList.size()>0)
							 {
								 for(int j=0;j<selOptPCList.size();j++)
								 {
									 String pcId = (String)selOptPCList.get(j);
									 if(pcId.equals(strPCId))
									 {
										 String selOptRelId = (String)selOptRelIdList.get(j);
										 DomainRelationship.disconnect(context, selOptRelId);
									 }
								 }
							 }

						 }
					 }
				 }
			}
			 catch(Exception e){
				 throw new FrameworkException(e);
			 }
			 return 0;
		 }

		 /**
		  * It is used in product version/copy to copy LogicalFeature to the newly created product
		  * @param context
		  * @param args
		  * @return Map containing old created Logical Feature rel id as key and pipe separated
		  * old rel physical id, new rel physical id, new rel id as value
		  * @throws FrameworkException
		  * @exclude
		  */
		 public Map copyLogicalFeature(Context context, String[] args)
			throws FrameworkException
			{
			 Map clonedObjIDMap = null;

			 try{
				 ArrayList programMap = (ArrayList)JPO.unpackArgs(args);

				 String sourceObjectId = (String) programMap.get(0);
				 String destinationObjectId = (String) programMap.get(1);

				 clonedObjIDMap = LogicalFeature.copyLogicalFeatureStructure(context, sourceObjectId, destinationObjectId);
			 }
			 catch(Exception e){
				 throw new FrameworkException(e);
			 }
			 return clonedObjIDMap;
			}

			/**
			 * Method to search features need to exclude from emxFullSearchPage in Replace functionality
			 *
			 * @param context - the eMatrix <code>Context</code> object
			 * @param args -
			 * @return featureList - String list of Object IDs to exclude
			 * @throws Exception
			 */

			@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
			public StringList excludeAvailableLogicalFeatureForRepalce(Context context, String [] args)
			throws Exception
			{
				Map programMap = (Map) JPO.unpackArgs(args);
				String strSourceObjectId = (String) programMap.get("objectId");
				String parentOID = (String)programMap.get("parentOID");

				String strObjectPattern = ConfigurationConstants.TYPE_LOGICAL_FEATURE+","+ConfigurationConstants.TYPE_HARDWARE_PRODUCT+","+ConfigurationConstants.TYPE_SOFTWARE_PRODUCT+","+ConfigurationConstants.TYPE_SERVICE_PRODUCT;
				String strRelPattern = ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES;
				StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
				objectSelects.add("from["+ ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES +"].to.id");

				MapList relatedFromFeatureList = new MapList();

				StringList sRelSelects = new StringList();
				sRelSelects.addElement(DomainRelationship.SELECT_NAME);
				sRelSelects.addElement(DomainRelationship.SELECT_ID);

				ConfigurationUtil util = new ConfigurationUtil(strSourceObjectId);
				relatedFromFeatureList = util.getObjectStructure(context, strObjectPattern, strRelPattern,
						objectSelects, sRelSelects, true, false, 1, 0, DomainConstants.EMPTY_STRING,
						   DomainConstants.EMPTY_STRING, (short)0, DomainConstants.EMPTY_STRING);

				HashSet featureList = new HashSet();
				//add the context Feature
				featureList.add(strSourceObjectId);
				if(parentOID != null && !parentOID.equals("")){
					featureList.add(parentOID);
				}

				for(int i=0;i<relatedFromFeatureList.size();i++)
				{
					Map mapFeatureObj = (Map) relatedFromFeatureList.get(i);
					if(mapFeatureObj.containsKey(DomainConstants.SELECT_ID))
					{
						Object idsObject = mapFeatureObj.get(DomainConstants.SELECT_ID);
						if(idsObject.getClass().toString().contains("StringList"))
						{
							featureList.addAll((StringList)idsObject);
						}
						else
						{
							featureList.add((String)idsObject);
						}
					}

					if(mapFeatureObj.containsKey("from["+ ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES +"].to.id"))
					{
						Object idsObject = mapFeatureObj.get("from["+ ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES +"].to.id");
						if(idsObject.getClass().toString().contains("StringList"))
						{
							featureList.addAll((StringList)idsObject);
						}
						else
						{
							featureList.add((String)idsObject);
						}
					}
				}

				ArrayList featureListArray = new ArrayList(featureList);

				return new StringList(featureListArray);
			}
		 	/**
		 	 * This is a delete check trigger method to check if there are Rules connected on the Varies By Relationship,
		 	 * if yes returns 1
		 	 *
		 	 * @param context
		 	 * @param args
		 	 * @return
		 	 * @throws Exception
		 	 * @exclude
		 	 */
			public int checkForConnectedRules(Context context, String args[])
			throws Exception {

				int iReturn = 0;
				String strDVId = args[0];
				String strLFId = args[1];
				try {
					LogicalFeature comFtr = new LogicalFeature(strLFId);
					boolean bResult = comFtr.isIRConnectedToDV(context,new StringList(strDVId));
					if(bResult){
				        String strLanguage = context.getSession().getLanguage();
						String strErrorMsg =  EnoviaResourceBundle.getProperty(context, SUITE_KEY,
		                        "emxConfiguration.Remove.Alert.DVConnectedToIR",strLanguage);
						throw new FrameworkException(strErrorMsg);
					}

				} catch (Exception e) {
					iReturn = 1;
					throw new FrameworkException(e.getMessage());
				}
				return iReturn;
			}

			/**
			 * This is a create check trigger on varies by relationship to check if the Context logical Feature has more than 1 part Family.
			 * If yes returns 1
			 *
			 * @param context
			 * @param args
			 * @return
			 * @throws Exception
			 * @exclude
			 */
			public int checkForMultiplePartFamilies(Context context, String args[])
			throws Exception {
				int iReturn = 0;
				String strLFId = args[1];
				try {
			          // check for Multiple Part Families
		              LogicalFeature compFtr = new LogicalFeature(strLFId);
			          String relWhere = DomainObject.EMPTY_STRING;
			          String objWhere = DomainObject.EMPTY_STRING;
			          // Obj and Rel pattern
			          StringBuffer typePattern = new StringBuffer();
			          typePattern.append(ConfigurationConstants.TYPE_PART_FAMILY);
			          StringBuffer relPattern = new StringBuffer();

			          int iLevel = 1;

			          MapList objectList = compFtr.getGBOMStructure(context,typePattern.toString() ,
			                  relPattern.toString(), null, null, false, true, iLevel, 0,
			                  objWhere, relWhere, DomainObject.FILTER_STR_AND_ITEM, DomainObject.EMPTY_STRING);
			          if(objectList.size()>1){
					        String strLanguage = context.getSession().getLanguage();
							String strErrorMsg =  EnoviaResourceBundle.getProperty(context, SUITE_KEY,
			                        "emxConfiguration.Error.MultiplePartFamilies",strLanguage);
							throw new FrameworkException(strErrorMsg);
			          }
				} catch (Exception e) {
					iReturn = 1;
					throw new FrameworkException(e.getMessage());
				}
				return iReturn;
			}

			/**
			  * It is used in product version/copy to copy Part to the newly created product
			  * @param context
			  * @param args
			  * @return Map containing old created GBOM rel id as key and pipe separated
			  * old rel physical id, new rel physical id, new rel id as value
			  * @throws FrameworkException
			  * @exclude
			  */
			public Map copyGBOMStructure(Context context, String[] args)
			throws FrameworkException
			{
			 Map clonedObjIDMap = null;

			 try{
				 ArrayList programMap = (ArrayList)JPO.unpackArgs(args);

				 String sourceObjectId = (String) programMap.get(0);
				 String destinationObjectId = (String) programMap.get(1);

				 clonedObjIDMap = LogicalFeature.copyGBOMStructure(context, sourceObjectId, destinationObjectId);
			 }
			 catch(Exception e){
				 throw new FrameworkException(e);
			 }
			 return clonedObjIDMap;
			}


			/**
		     * This method is used to control the access for Logical Features commands
		     * @param context The ematrix context object
		     * @param String The args
		     * @return Boolean
		     * @throws FrameworkException - - when operation fails
			 * @author A69
		     * @since R212
		     */
		    public static Boolean isFrozenState(Context context, String[] args )throws FrameworkException
		    {
		    	boolean bInvalidState = false;
		    	try{
			    	HashMap requestMap = (HashMap) JPO.unpackArgs(args);
			    	String objectId = (String) requestMap.get("objectId");
			    	if(ConfigurationUtil.isNotNull(objectId)){
			    		bInvalidState = ConfigurationUtil.isFrozenState(context, objectId);
			    	}
		    	}
		    	catch(Exception e){
					throw new FrameworkException(e);
				}

		    	return !bInvalidState;
		    }

	     /**
	     *
		 *
	     * @param context the eMatrix <code>Context</code> object
	     * @param args holds the following input arguments
	     *        0 - id of the business object
	     *        1 - Expression
	     * @return String
	     * @throws Exception if the operation fails
	     */

	    public String getSelecatableVal(Context context, String args[]) throws Exception
	    {
	    	//return MqlUtil.mqlCommand(context, "print bus "+ args[0] +" select "+args[1]+" dump |");
	    	return MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3",args[0],args[1],ConfigurationConstants.DELIMITER_PIPE);
	    }
	    /**
	     *
	     * @param context
	     * @param args
	     */
	    public void disconnectManufacturingPlanOfProduct(Context context, String args[])
	    {
	    	//Disconnected the Manufacturing Plan BreakDown on replacing the feature or product
    		boolean isDMCInstalled = FrameworkUtil.isSuiteRegistered(context,
    				"appVersionDMCPlanning", false, null, null);


    		if (isDMCInstalled) {

    			try {
					JPO.invoke(context,	"ManufacturingPlan", args,
							"disconnectManufacturingPlanOfProduct",
							args, null);
				} catch (MatrixException e) {
					e.printStackTrace();
				}
    		}
	    }



	    /**
	     * This is a Access Function for Display Name Column, if the Context is Logical Features then this will be hidden
	     * else will be displayed.
	     *
	     * @param context
	     * @param args
	     * @return bResult - true if no Logical Feature
	     * 				     false if Logical Feature Context
	     * @throws Exception
	     */
	    public boolean showRMB(Context context, String[] args)
	    throws Exception {
	    	boolean bResult = true;
	    	String strCntxtProductId = "";
	    	Map programMap = (Map) JPO.unpackArgs(args);
	    	if(programMap.get("parentOID")!=null){
	    		strCntxtProductId = programMap.get("parentOID").toString();
	    		DomainObject dom = new DomainObject(strCntxtProductId);
	    		if(mxType.isOfParentType(context, dom.getInfo(context,DomainObject.SELECT_TYPE),
	    				ConfigurationConstants.TYPE_LOGICAL_STRUCTURES)){
	    			bResult = false;
	    		}
	    	}else if (programMap.get("objectId")!=null){
	    		strCntxtProductId = programMap.get("objectId").toString();
	    		DomainObject dom = new DomainObject(strCntxtProductId);
	    		if(mxType.isOfParentType(context, dom.getInfo(context,DomainObject.SELECT_TYPE),
	    				ConfigurationConstants.TYPE_LOGICAL_STRUCTURES)){
	    			bResult = false;
	    		}
	    	}else{
	    		bResult = false;
	    	}
	    	return bResult;
	    }

	    /**
	     * This is a Access Function for Display Name Column, if the Context other than Logical Features then this will be hidden
	     * else will be displayed.
	     *
	     * @param context
	     * @param args
	     * @return bResult - true if Logical Feature
	     * 				     false if no Logical Feature Context
	     * @throws Exception
	     */
	    public boolean showNoRMB(Context context, String[] args)
	    throws Exception {
	    	boolean bResult = false;

	    	Map programMap = (Map) JPO.unpackArgs(args);
	    	if(programMap.get("parentOID")!=null){
	    		String strCntxtProductId = programMap.get("parentOID").toString();
	    		DomainObject dom = new DomainObject(strCntxtProductId);

	    		if(mxType.isOfParentType(context, dom.getInfo(context,DomainObject.SELECT_TYPE),
	    				ConfigurationConstants.TYPE_LOGICAL_STRUCTURES)){
	    			bResult = true;
	    		}
	    	}else if(programMap.get("objectId")!=null){
	    		String strCntxtProductId = programMap.get("objectId").toString();
	    		DomainObject dom = new DomainObject(strCntxtProductId);

	    		if(mxType.isOfParentType(context, dom.getInfo(context,DomainObject.SELECT_TYPE),
	    				ConfigurationConstants.TYPE_LOGICAL_STRUCTURES)){
	    			bResult = true;
	    		}
	    	}
	    	else
	    	{
	    		bResult =true ;
	    	}
	    	return bResult;
	    }

/** Method call as a trigger to check if the sub-features associated with the
	     * Product are in Release state.
	     *
	     * @param context
	     *            the eMatrix <code>Context</code> object
	     * @param args -
	     *            Holds the parameters passed from the calling method
	     * @return int - Returns 0 in case of Check trigger is success and 1 in case
	     *         of failure
	     * @throws Exception
	     *             if operation fails
	     */
	    public int checkSubFeaturesForProductPromote(Context context, String args[])
	    throws Exception {
	    	// return value of the function
	    	int iReturn = 0;
	    	// The feature object id sent by the emxTriggerManager is retrieved
	    	// here.
	    	String objectId = args[0];
	    	String strSelect = DomainObject.SELECT_CURRENT;
	    	// ObjectSelects StringList is initialized
	    	StringList objectSelects = new StringList(strSelect);
	    	StringList tempStringList = new StringList();

	    	StringBuffer strTypePattern = new StringBuffer();
	    	strTypePattern.append(ConfigurationConstants.TYPE_LOGICAL_FEATURE);
	    	strTypePattern.append(",");
	    	strTypePattern.append(ConfigurationConstants.TYPE_PRODUCTS);
	    	strTypePattern.append(",");
	    	strTypePattern.append(ConfigurationConstants.TYPE_CONFIGURATION_FEATURE);
	    	strTypePattern.append(",");
	    	strTypePattern.append(ConfigurationConstants.TYPE_MANUFACTURING_FEATURE);


	    	StringBuffer strRelPattern = new StringBuffer();
	    	strRelPattern.append(ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES);
	    	strRelPattern.append(",");
	    	strRelPattern.append(ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES);
	    	strRelPattern.append(",");
	    	strRelPattern.append(ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES);

	    	ConfigurationUtil confUtil = new ConfigurationUtil(objectId);
	    	MapList mapLogicalStructure = confUtil.getObjectStructure(context,strTypePattern.toString(),strRelPattern.toString(),
	    			objectSelects, tempStringList, false, true, 1,0,
	    			DomainObject.EMPTY_STRING, DomainObject.EMPTY_STRING,(short) 1,DomainObject.EMPTY_STRING);

	    	boolean isFrozen = false;
	    	int iNumberOfObjects = mapLogicalStructure.size();
	    	if (iNumberOfObjects > 0) {

	    		for (int i = 0; i < iNumberOfObjects; i++) {
	    			Map relBusObjMap = (Map)mapLogicalStructure.get(i);
	    			String strSubFeatureID = (String)relBusObjMap.get(DomainConstants.SELECT_ID);
	    			isFrozen = ConfigurationUtil.isFrozenState(context,strSubFeatureID);
	    			if (!isFrozen) {
	    				String language = context.getSession().getLanguage();
	    				String strAlertMessage =  EnoviaResourceBundle.getProperty(context, SUITE_KEY,
	    						"emxProduct.Alert.ProductPromoteFailedStateNotRelease",language);
	    				emxContextUtilBase_mxJPO
	    				.mqlNotice(context, strAlertMessage);
	    				iReturn = 1;
	    				break;
	    			}

	    		}
	    	}
	    	return iReturn;
	    }

	    /**
	 	 * Label program for  Tree structure
	 	 * @param context
	 	 * @param args
	 	 * @return
	 	 * @throws Exception
	      * @since R212
	      */
	     public String getDisplayNameForNavigator(Context context,String[] args) throws Exception
	     {
	 		String strTreeName = ConfigurationUtil.getDisplayNameForFeatureNavigator(context, args);

	 		return strTreeName;
	 	 }

	 	/**
	      * This method is used to connect the derived Logical Feature revisions to the Parent Feature.
	      *
	      * @param context :
	      *              The eMatrix Context object
	      * @param featureId :
	      *              The source feature Object id
	      * @exception Exception :
	      *              Throws exception if operation fails
	      * @since R213
	      * @author A69
	      */
	     public void connectLogicalFeatureOnRevise(Context context, String args[])
	         throws FrameworkException {
	         String featureId = args[0];
	         DomainObject contextFeature = null;
	         BusinessObject nextRevision = null;
	         try {
	             contextFeature = new DomainObject(featureId);
	             nextRevision = contextFeature.getNextRevision(context);
	             DomainRelationship.connect(context, new DomainObject(nextRevision),
	                    ConfigurationConstants.RELATIONSHIP_DERIVED,
	                    contextFeature);
	         } catch (Exception e) {
	 			throw new FrameworkException(e);
	 		}
	     }

	     /** It is used to display "Display Name" Column in Structure  Browser of Logical Feature tab of Design Variant Reprot
			 * @param context
			 * @param args
			 * @return  Marketing Name For Products and Display Name for Logical Feature
			 * @throws FrameworkException
			 */
	     public Vector displayNameForLogicalFeatureofDVReport(Context context, String []args) throws FrameworkException{
	    	 // Create result vector
	    	 Vector vecResult = new Vector();
	    	 try{
	    		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    		 MapList objectMapList = (MapList)programMap.get("objectList");
	    		 
	    		 Map pramMap = (Map)programMap.get("paramList");
	    		 String contextProductOID = (String)pramMap.get("ProductID");

	    		 String strReportFormat = (String) pramMap.get("reportFormat");

	    		 String strName = null;
	    		 String strDisplayName = null;
	    		 String attrMarketName = "attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]";
	    		 String attrDisplayName = "attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME	+"]";
	    		 String strDisplayName1 = "";
	    		 for(int i=0;i<objectMapList.size();i++)
	    		 {
	    			 Map objectMap = (Map) objectMapList.get(i);
	    			 String strLogicalFeatureID = (String)objectMap.get(DomainConstants.SELECT_ID);

	    			 StringTokenizer objIDs = new StringTokenizer(strLogicalFeatureID, ",");
	    			 if(objIDs.countTokens()>1){
	    				 // Context Feature ID
	    				 strLogicalFeatureID = objIDs.nextToken().trim();
	    			 }


	    			 DomainObject domObject = new DomainObject(strLogicalFeatureID);
	    			 if (domObject.exists(context)
	    					 && (domObject.isKindOf(context,
	    							 ConfigurationConstants.TYPE_PRODUCTS)
	    							 || domObject.isKindOf(context,
	    									 ConfigurationConstants.TYPE_MODEL) || domObject
	    									 .isKindOf(
	    											 context,
	    											 ConfigurationConstants.TYPE_PRODUCT_LINE)
	    											 || domObject
	    											 .isKindOf(
	    													 context,
	    													 ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION)))
	    			 {

	    				 strName = (String) objectMap.get(attrMarketName);

	    				 if("".equalsIgnoreCase(strName)|| "null".equalsIgnoreCase(strName) || strName == null){
	    					 String SELECT_MARKETING_NAME =  "attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]";
	    					 strName = domObject.getInfo(context, SELECT_MARKETING_NAME);
	 	    			}


	    				 strDisplayName1 = strName ;

	    				 if((strReportFormat!=null
	    						 &&!strReportFormat.equals("null")
	    						 &&!strReportFormat
	    						 .equals(DomainConstants.EMPTY_STRING))){
	    					 strDisplayName = strDisplayName1;
	    				 }else{

	    					 if(!(strName!=null && !strName.equals("")))
	    					 {
	    						 strDisplayName1 = getDisplayNameWithoutRevision(context,strLogicalFeatureID,false);
	    						 strDisplayName=("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/DesignVariantPreProcess.jsp?portal=FTRConfigurationDesignVariantPortal&amp;channel=FTRDVConfigurationFeatureChannel&amp;isIndentedTable=true");
	    						 strDisplayName+=("&amp;contextIndependent=no&amp;mode=viewConfigurationFeatures&amp;contextProductOID=" + XSSUtil.encodeForHTMLAttribute(context, contextProductOID) + "&amp;parentOID=");
	    						 strDisplayName+=("', '', '', 'false', 'hiddenFrame');\" >");
	    						 strDisplayName+=(XSSUtil.encodeForXML(context, strDisplayName1));
	    						 strDisplayName+=("</a>");

	    						 //								strDisplayName=("<a href=\"JavaScript:showDialog('");
	    						 //								strDisplayName+=(FrameworkUtil
	    						 //										.encodeURL("../common/emxIndentedTable.jsp?objectCompare=false&amp;program=LogicalFeature:getActiveInactiveDesignVariants&amp;"+
	    						 //												"table=FTRDVConfigurationFeatureTable&amp;toolbar=FTRDesignVariantLFContextToolbar&amp;selection=multiple&amp;"+
	    						 //												"header=emxProduct.DesignVariantStructureBrowser.ViewDesignVariantHeader&amp;"+
	    						 //										"HelpMarker=emxhelpdesignvariantview&amp;contextIndependent=no"));
	    						 //
	    						 //								strDisplayName+=("')\">");
	    						 //								strDisplayName+=(strDisplayName1);
	    						 //								strDisplayName+=("</a>");

	    					 }else{

	    						 strDisplayName=("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/DesignVariantPreProcess.jsp?portal=FTRConfigurationDesignVariantPortal&amp;channel=FTRDVConfigurationFeatureChannel&amp;isIndentedTable=true");
	    						 strDisplayName+=("&amp;contextIndependent=no&amp;mode=viewConfigurationFeatures&amp;contextProductOID=" + XSSUtil.encodeForHTMLAttribute(context, contextProductOID) + "&amp;parentOID=");
	    						 strDisplayName+=("', '', '', 'false', 'hiddenFrame');\" >");
	    						 strDisplayName+=(XSSUtil.encodeForXML(context, strDisplayName1));
	    						 strDisplayName+=("</a>");

	    						 //								strDisplayName=("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxRefreshChannel.jsp?portal=FTRConfigurationDesignVariantPortal&amp;channel=FTRDVConfigurationFeatureChannel&amp;isIndentedTable=true");
	    						 //								strDisplayName+=("&amp;contextIndependent=no&amp;mode=insert&amp;contextProductOID=" + contextProductOID + "&amp;parentOID=");
	    						 //								strDisplayName+=("', '', '', 'false', 'hiddenFrame');\" >");
	    						 //								strDisplayName+=(strDisplayName1);
	    						 //								strDisplayName+=("</a>");
	    						 //
	    						 //								strDisplayName=("<a href=\"JavaScript:showDialog('");
	    						 //								strDisplayName+=(FrameworkUtil
	    						 //										.encodeURL("../common/emxIndentedTable.jsp?objectCompare=false&amp;program=LogicalFeature:getActiveInactiveDesignVariants&amp;"+
	    						 //												"table=FTRDVConfigurationFeatureTable&amp;toolbar=FTRDesignVariantLFContextToolbar&amp;selection=multiple&amp;"+
	    						 //												"header=emxProduct.DesignVariantStructureBrowser.ViewDesignVariantHeader&amp;"+
	    						 //										"HelpMarker=emxhelpdesignvariantview&amp;contextIndependent=no"));
	    						 //
	    						 //								strDisplayName+=("')\">");
	    						 //								strDisplayName+=(strDisplayName1);
	    						 //								strDisplayName+=("</a>");
	    					 }
	    				 }
	    			 }
	    			 else
	    			 {
	    				 strName = (String) objectMap.get(attrDisplayName);
	    				 if("".equalsIgnoreCase(strName)|| "null".equalsIgnoreCase(strName) || strName == null){
	    		    			String name = DomainConstants.SELECT_NAME;
	    						StringList selectables = new StringList();
	    		    				selectables.addElement(attrDisplayName);
	    		    				selectables.addElement(name);
	    		    				selectables.addElement(attrMarketName);
	    		    			Map tempMap = domObject.getInfo(context, selectables);
	    		    			String strLFDisplayName = (String) tempMap.get(attrDisplayName);
	    		    			String strLFMarketingName = (String) tempMap.get(attrMarketName);
	    		    			String strLFName = (String) tempMap.get(name);

	    		    			if("".equalsIgnoreCase(strLFDisplayName)|| "null".equalsIgnoreCase(strLFDisplayName) || strLFDisplayName == null){
	    		    				if("".equalsIgnoreCase(strLFMarketingName)|| "null".equalsIgnoreCase(strLFMarketingName) || strLFMarketingName == null){
	    		    					strLFMarketingName = strLFName;
		    		    			}
		    		    				strName = strLFMarketingName;
	    		    			}else{
	    		    				strName = strLFDisplayName;
	    		    			}
	 	    			}
	    				 strDisplayName1 = strName;

	    				 if((strReportFormat!=null
	    						 &&!strReportFormat.equals("null")
	    						 &&!strReportFormat
	    						 .equals(DomainConstants.EMPTY_STRING))){
	    					 strDisplayName = strDisplayName1;
	    				 }else{
	    					 if(!(strName!=null && !strName.equals("")))
	    					 {
	    						 strDisplayName1 = getDisplayNameWithoutRevision(context,strLogicalFeatureID,true);
	    						 strDisplayName=("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/DesignVariantPreProcess.jsp?portal=FTRConfigurationDesignVariantPortal&amp;channel=FTRDVConfigurationFeatureChannel&amp;isIndentedTable=true");
	    						 strDisplayName+=("&amp;contextIndependent=no&amp;objectId="+ XSSUtil.encodeForHTMLAttribute(context, strLogicalFeatureID)+","+XSSUtil.encodeForHTMLAttribute(context, contextProductOID) +"&amp;mode=viewConfigurationFeatures&amp;ProdID=" + XSSUtil.encodeForHTMLAttribute(context, contextProductOID) + "&amp;parentOID=");
	    						 strDisplayName+=("', '', '', 'false', 'hiddenFrame');\" >");
	    						 strDisplayName+=(XSSUtil.encodeForXML(context,strDisplayName1));
	    						 strDisplayName+=("</a>");
	    					 }else{
	    						 strDisplayName=("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/DesignVariantPreProcess.jsp?portal=FTRConfigurationDesignVariantPortal&amp;channel=FTRDVConfigurationFeatureChannel&amp;isIndentedTable=true");
	    						 strDisplayName+=("&amp;contextIndependent=no&amp;objectId="+ XSSUtil.encodeForHTMLAttribute(context, strLogicalFeatureID)+","+XSSUtil.encodeForHTMLAttribute(context, contextProductOID) +"&amp;mode=viewConfigurationFeatures&amp;contextProductOID=" + XSSUtil.encodeForHTMLAttribute(context, contextProductOID) + "&amp;parentOID=");
	    						 strDisplayName+=("', '', '', 'false', 'hiddenFrame');\" >");
	    						 strDisplayName+=(XSSUtil.encodeForXML(context,strDisplayName1));
	    						 strDisplayName+=("</a>");
	    					 }
	    				 }
	    			 }
	    			 vecResult.add(strDisplayName);
	    		 }
	    	 }
	    	 catch (Exception e) {
	    		 throw new FrameworkException(e);
	    	 }
	    	 return vecResult;
	     }

	     /** It is used to decide whether the command is allowed to display in DV report depending on the type of object
			 * @param context
			 * @param args
			 * @return  true if the object is of type Logical Feature
			 * @throws FrameworkException
			 */
	     public static Boolean isCommandAllowedinDesignVariantReport(Context context, String[] args)
         throws Exception {

		String featureID = "";
		String objProductID = "";
		
		boolean bFieldAccess = false;
		HashMap requestMap = (HashMap) JPO.unpackArgs(args);
		boolean isSet = false;
		StringBuffer strContextId = new StringBuffer() ;

		if(requestMap.get("ProductID")!=null){
			strContextId.append((String) requestMap.get("ProductID"));
		}


		String parentOID = (String) requestMap.get("parentOID");
		if(parentOID!=null && !parentOID.trim().equals("")){
			StringTokenizer objIDs = new StringTokenizer(parentOID, ",");
			if(objIDs.countTokens()>1){
				// Context Feature ID
				featureID = objIDs.nextToken().trim();
				// Context Product ID
				objProductID = objIDs.nextToken().trim();
				if(featureID.equals(objProductID)){
					bFieldAccess = false;
					isSet = true;
				}
			}
		}

		if(!isSet){
			String objectId = (String) requestMap.get("objectId");
			if(objectId!=null && !objectId.trim().equals("")){
				StringTokenizer objIDs = new StringTokenizer(objectId, ",");
				if(objIDs.countTokens()>1){
					// Context Feature ID
					featureID = objIDs.nextToken().trim();
					String strFeatureType = (String) new DomainObject(featureID).getInfo(context,DomainConstants.SELECT_TYPE);
					// Context Product ID
					objProductID = objIDs.nextToken().trim();
					if(!featureID.equals(objProductID)){
						if(( mxType.isOfParentType(context,strFeatureType,ConfigurationConstants.TYPE_LOGICAL_FEATURE)) || (mxType.isOfParentType(context,strFeatureType,ConfigurationConstants.TYPE_CONFIGURATION_FEATURE))){
							bFieldAccess = true;
						}else{
							bFieldAccess = false;
						}
					}else{
						bFieldAccess = false;
					}
				}else{
					objProductID=objIDs.nextToken().trim();
					String strProdType = (String) new DomainObject(objProductID).getInfo(context,DomainConstants.SELECT_TYPE);
					if(( mxType.isOfParentType(context,strProdType,ConfigurationConstants.TYPE_LOGICAL_FEATURE)) || (mxType.isOfParentType(context,strProdType,ConfigurationConstants.TYPE_CONFIGURATION_FEATURE))){
						bFieldAccess = true;
					}else{
						bFieldAccess = false;
					}
				}
			}
		}
 	return bFieldAccess;
 }

	     /** It is used to decide whether the channel is allowed to display in DV report depending on the context
			 * @param context
			 * @param args
			 * @return  true if the context is Product Variant
			 * @throws FrameworkException
			 */
	     public static Boolean isChannelAllowedinPVDesignVariantReport(Context context, String[] args)
	     throws Exception {
	    	 boolean bFieldAccess = false;
	    	 try{
	    		 HashMap requestMap = (HashMap) JPO.unpackArgs(args);
	        	 StringBuffer strContextId = new StringBuffer() ;
	        	 if(requestMap.get("ProductID")!=null){
	        		 strContextId.append((String) requestMap.get("ProductID"));
	        	 }

		    	 String objProductID = "";

		    	 String objectId = (String) requestMap.get("objectId");
		    	 if(objectId!=null && !objectId.trim().equals("")){
		    		 StringTokenizer objIDs = new StringTokenizer(objectId, ",");
		    		 if(objIDs.countTokens()>1){
		    			 objIDs.nextToken().trim();
		    			 objProductID = objIDs.nextToken().trim();
		    		 }
		    	 }
		       	 if(strContextId.toString().equals("")){
			       		strContextId.append(objProductID);
			     }
		       	 //For My Desk >> Logical Feature - Context Object will be null
		       	if(ProductLineCommon.isNotNull(strContextId.toString())){
	    		 // getting the type of the product
	    		 String strProdType = (String) new DomainObject(strContextId.toString()).getInfo(context,DomainConstants.SELECT_TYPE);
	    		 if((mxType.isOfParentType(context,strProdType,ConfigurationConstants.TYPE_PRODUCT_VARIANT))){
	    			 bFieldAccess = true;
	    		 }else{
	    			 bFieldAccess = false;
	    		 }
		       	}
	    	 }catch(Exception exp){
	    		 exp.printStackTrace();
	    		 throw new FrameworkException(exp);
	    	 }
	    	 return bFieldAccess;
	     }

	     /** It is used to decide whether the channel is allowed to display in DV report depending on the context
			 * @param context
			 * @param args
			 * @return  true if the context is Product
			 * @throws FrameworkException
			 */
    public static Boolean isChannelAllowedinProductDesignVariantReport(Context context, String[] args)
    throws Exception {
    	boolean bFieldAccess = false;
    	try{
    		 HashMap requestMap = (HashMap) JPO.unpackArgs(args);
	       	 StringBuffer strContextId = new StringBuffer() ;
	       	 if(requestMap.get("ProductID")!=null){
	       		 strContextId.append((String) requestMap.get("ProductID"));
	       	 }

	       	 String objProductID = "";

	    	 String objectId = (String) requestMap.get("objectId");
	    	 if(objectId!=null && !objectId.trim().equals("")){
	    		 StringTokenizer objIDs = new StringTokenizer(objectId, ",");
	    		 // this is when action invoked from Reports --> View DV
	    		 if(objIDs.countTokens()>1){
	    			 // Context Feature ID
	    			 objIDs.nextToken();
	    			 // Context Product ID
	    			 objProductID = objIDs.nextToken().trim();
	    		 }
	    	 }
	       	 if(strContextId.toString().equals("")){
	       		strContextId.append(objProductID);
	       	 }
	       //For My Desk >> Logical Feature - Context Object will be null.So will use Product >> LF expand structure 
	       	if(ProductLineCommon.isNotNull(strContextId.toString())){
	    		// getting the type of the product
	    		String strProdType = (String) new DomainObject(strContextId.toString()).getInfo(context,DomainConstants.SELECT_TYPE);
	    		if((!mxType.isOfParentType(context,strProdType,ConfigurationConstants.TYPE_PRODUCT_VARIANT))){
	    			bFieldAccess = true;
	    		}else{
	    			bFieldAccess = false;
	    		}
	        }else{
	       		bFieldAccess = true;
	       	 }
    	}catch(Exception exp){
    		exp.printStackTrace();
    		throw new FrameworkException(exp);
    	}
    	return bFieldAccess;
    }

    /** It is used to display "Disign Variant" Column in Structure  Browser of Logical Feature tab of Design Variant Reprot
	 * @param context
	 * @param args
	 * @return  Yes/No
	 * @throws FrameworkException
	 */
    public Vector getDesignVariantsforDVReport(Context context, String[] args)
    throws FrameworkException {
    	try{
    		HashMap programMap = (HashMap) JPO.unpackArgs(args);
    		MapList lstObjectIdsList = (MapList) programMap.get("objectList");
    		Vector contextDesignVariants = new Vector();
    		DomainObject featureObject = null;
    		String strDesVar = "No";
    		String strVaraintExist = "False";
    		boolean personAccess = false;
    		String strLanguage = context.getSession().getLanguage();
    		
    		if (PersonUtil.hasAssignment(context, PropertyUtil.getSchemaProperty(context,"role_ProductManager")) ||
    				PersonUtil.hasAssignment(context, PropertyUtil.getSchemaProperty(context,"role_SystemEngineer")))
    		{
    			personAccess = true;
    		}

    		for (int i = 0; i < lstObjectIdsList.size(); i++)
    		{
    			Map mpLF = (Map)lstObjectIdsList.get(i);
    			String strObjId = (String) mpLF.get(DomainObject.SELECT_ID);
    			featureObject = DomainObject.newInstance(context,strObjId);
    			String strRootNode = (String)mpLF.get("Root Node");
    			//String ctxProductID = (String)mpLF.get("id[parent]");
    			if(strRootNode!=null && strRootNode.equalsIgnoreCase("True")){
    				if(featureObject.isKindOf(context, ConfigurationConstants.TYPE_LOGICAL_FEATURE)){
    					strVaraintExist = featureObject.getInfo(context,"from["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"]");
    					strDesVar =  EnoviaResourceBundle.getProperty(context, SUITE_KEY,
    					"emxConfiguration.DesignVarianceExists.FalseValue",strLanguage);

    					if (strVaraintExist != null && strVaraintExist.equalsIgnoreCase("True"))
    					{
    						strDesVar = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
    						"emxConfiguration.DesignVarianceExists.TrueValue",strLanguage);
    					}
    					String strValue = "";
    					if (personAccess)
    					{
    						strValue = strDesVar;
    					}else
    					{
    						strValue = strDesVar;
    					}
    					contextDesignVariants.add(strValue);
    				}else{
    					contextDesignVariants.add(DomainConstants.EMPTY_STRING);
    				}
    			}
    			else if(featureObject.isKindOf(context, ConfigurationConstants.TYPE_PRODUCTS))
    				contextDesignVariants.add(DomainConstants.EMPTY_STRING);
    			else{

    				strVaraintExist = featureObject.getInfo(context,"from["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"]");
    				strDesVar =  EnoviaResourceBundle.getProperty(context, SUITE_KEY,
    				"emxConfiguration.DesignVarianceExists.FalseValue",strLanguage);

    				if (strVaraintExist != null && strVaraintExist.equalsIgnoreCase("True"))
    				{
    					strDesVar = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
    					"emxConfiguration.DesignVarianceExists.TrueValue",strLanguage);
    				}
    				String strValue = "";
    				if (personAccess)
    				{
    					strValue = strDesVar;
    				}else
    				{
    					strValue = strDesVar;
    				}
    				contextDesignVariants.add(strValue);
    			}
    		}
    		return contextDesignVariants;
    	}
    	catch (Exception e) {
    		throw new FrameworkException(e.toString());
    	}
    }
    /**
 	 * column  program for  Search Table for CopyTo/CopyFrom in LF Context
 	 * @param context
 	 * @param args
 	 * @return
 	 * @throws Exception
      * @since R214
      */
	public StringList displayNameForSearchTable(Context context, String[] args)
			throws Exception {
		try {
			StringList displayNameList = ConfigurationUtil
					.displayNameForSearchTable(context, args);
			// for IR-379084-3DEXPERIENCER2016x - hyperlink for global search
			// and hyperlink not for add existing/chooser

			StringList displayNameHyperlinkList = new StringList();
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramList");
			String isParentIDAvailable = (String) paramMap.get("parentOID");

			if (!ProductLineCommon.isNotNull(isParentIDAvailable)) {
				String strName = "";
				String strRev = "";
				String strDisplayId = "";
				String suiteDir = (String) paramMap.get("SuiteDirectory");
				String suiteKey = (String) paramMap.get("suiteKey");
				MapList objectList = (MapList) programMap.get("objectList");
				int iNumOfObjects = objectList.size();
				Map tempMap = null;

				for (int iCnt = 0; iCnt < iNumOfObjects; iCnt++) {
					tempMap = (Map) objectList.get(iCnt);
					StringBuffer temp = new StringBuffer();
					strName = (String) displayNameList.get(iCnt);
					strRev = (String) tempMap.get("revision");
					strDisplayId = (String) tempMap
							.get(DomainConstants.SELECT_ID);
					temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
					temp.append(suiteDir);
					temp.append("&amp;suiteKey=");
					temp.append(suiteKey);
					temp.append("&amp;objectId=");
					temp.append(strDisplayId);
					temp.append("', '450', '300', 'true', 'content')\">");
					temp.append(XSSUtil.encodeForXML(context, strName));
					temp.append("</a>");
					displayNameHyperlinkList.add(temp.toString());
				}
				return displayNameHyperlinkList;
			} else {
				return displayNameList;
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}

     /** It is used to display "Name" Column in Structure  Browser of Logical Feature tab of Design Variant Reprot
 	 * @param context
 	 * @param args
 	 * @return  Yes/No
 	 * @throws FrameworkException
 	 */

     public Vector getNameForDVReprot (Context context,String[] args)throws Exception {
    	 // Create result vector
    	 Vector vecResult = new Vector();
    	 try{
    		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
    		 MapList objectMapList = (MapList)programMap.get("objectList");
    		 
    		 Map pramMap = (Map)programMap.get("paramList");
    		 String contextProductOID = (String)pramMap.get("ProductID");

    		 String strReportFormat = (String) pramMap.get("reportFormat");

    		 String strName = null;
    		 String strDisplayName = null;
    		 String strDisplayName1 = null;
    		 for(int i=0;i<objectMapList.size();i++)
    		 {
    			 Map objectMap = (Map) objectMapList.get(i);
    			 String strLogicalFeatureID = (String)objectMap.get(DomainConstants.SELECT_ID);

    			 StringTokenizer objIDs = new StringTokenizer(strLogicalFeatureID, ",");
    			 if(objIDs.countTokens()>1){
    				 // Context Feature ID
    				 strLogicalFeatureID = objIDs.nextToken().trim();
    			 }


    			 DomainObject domObject = new DomainObject(strLogicalFeatureID);
    			 if (domObject.exists(context)
    					 && (domObject.isKindOf(context,
    							 ConfigurationConstants.TYPE_PRODUCTS)
    							 || domObject.isKindOf(context,
    									 ConfigurationConstants.TYPE_MODEL) || domObject
    									 .isKindOf(
    											 context,
    											 ConfigurationConstants.TYPE_PRODUCT_LINE)
    											 || domObject
    											 .isKindOf(
    													 context,
    													 ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION)))
    			 {
    				 strName = (String) objectMap.get(DomainConstants.SELECT_NAME);
    				 if("".equalsIgnoreCase(strName)|| "null".equalsIgnoreCase(strName) || strName == null){
    					 strName = domObject.getInfo(context, SELECT_NAME);
 	    			}

    				 strDisplayName1 = strName ;

    				 if((strReportFormat!=null
    						 &&!strReportFormat.equals("null")
    						 &&!strReportFormat
    						 .equals(DomainConstants.EMPTY_STRING))){
    					 strDisplayName = strDisplayName1;
    				 }else{

    					 if(!(strName!=null && !strName.equals("")))
    					 {
    						 strDisplayName1 = getDisplayNameWithoutRevision(context,strLogicalFeatureID,false);
    						 strDisplayName=("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/DesignVariantPreProcess.jsp?portal=FTRConfigurationDesignVariantPortal&amp;channel=FTRDVConfigurationFeatureChannel&amp;isIndentedTable=true");
    						 strDisplayName+=("&amp;contextIndependent=no&amp;mode=viewConfigurationFeatures&amp;contextProductOID=" + XSSUtil.encodeForHTMLAttribute(context, contextProductOID) + "&amp;parentOID=");
    						 strDisplayName+=("', '', '', 'false', 'hiddenFrame');\" >");
    						 strDisplayName+=(XSSUtil.encodeForXML(context,strDisplayName1));
    						 strDisplayName+=("</a>");


    					 }else{

    						 strDisplayName=("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/DesignVariantPreProcess.jsp?portal=FTRConfigurationDesignVariantPortal&amp;channel=FTRDVConfigurationFeatureChannel&amp;isIndentedTable=true");
    						 strDisplayName+=("&amp;contextIndependent=no&amp;mode=viewConfigurationFeatures&amp;contextProductOID=" + XSSUtil.encodeForHTMLAttribute(context, contextProductOID) + "&amp;parentOID=");
    						 strDisplayName+=("', '', '', 'false', 'hiddenFrame');\" >");
    						 strDisplayName+=(XSSUtil.encodeForXML(context,strDisplayName1));
    						 strDisplayName+=("</a>");
    					 }
    				 }
    			 }
    			 else
    			 {
    				 strName = (String) objectMap.get(DomainConstants.SELECT_NAME);
    				 if("".equalsIgnoreCase(strName)|| "null".equalsIgnoreCase(strName) || strName == null){
 		    			String name = DomainConstants.SELECT_NAME;
 		    			 String attrMarketName = "attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]";
 			    		 String attrDisplayName = "attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME	+"]";
 						StringList selectables = new StringList();
 		    				selectables.addElement(attrDisplayName);
 		    				selectables.addElement(name);
 		    				selectables.addElement(attrMarketName);
 		    			Map tempMap = domObject.getInfo(context, selectables);
 		    			String strLFDisplayName = (String) tempMap.get(attrDisplayName);
 		    			String strLFMarketingName = (String) tempMap.get(attrMarketName);
 		    			String strLFName = (String) tempMap.get(name);

 		    			if("".equalsIgnoreCase(strLFDisplayName)|| "null".equalsIgnoreCase(strLFDisplayName) || strLFDisplayName == null){
 		    				if("".equalsIgnoreCase(strLFMarketingName)|| "null".equalsIgnoreCase(strLFMarketingName) || strLFMarketingName == null){
 		    					strLFMarketingName = strLFName;
	    		    			}
	    		    				strName = strLFMarketingName;
 		    			}else{
 		    				strName = strLFDisplayName;
 		    			}
	    			}
    				 strDisplayName1 = strName;

    				 if((strReportFormat!=null
    						 &&!strReportFormat.equals("null")
    						 &&!strReportFormat
    						 .equals(DomainConstants.EMPTY_STRING))){
    					 strDisplayName = strDisplayName1;
    				 }else{
    					 if(!(strName!=null && !strName.equals("")))
    					 {
    						 strDisplayName1 = getDisplayNameWithoutRevision(context,strLogicalFeatureID,true);
    						 strDisplayName=("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/DesignVariantPreProcess.jsp?portal=FTRConfigurationDesignVariantPortal&amp;channel=FTRDVConfigurationFeatureChannel&amp;isIndentedTable=true");
    						 strDisplayName+=("&amp;contextIndependent=no&amp;objectId="+ XSSUtil.encodeForHTMLAttribute(context, strLogicalFeatureID)+","+XSSUtil.encodeForHTMLAttribute(context, contextProductOID) +"&amp;mode=viewConfigurationFeatures&amp;ProdID=" + XSSUtil.encodeForHTMLAttribute(context, contextProductOID) + "&amp;parentOID=");
    						 strDisplayName+=("', '', '', 'false', 'hiddenFrame');\" >");
    						 strDisplayName+=(XSSUtil.encodeForXML(context,strDisplayName1));
    						 strDisplayName+=("</a>");
    					 }else{
    						 strDisplayName=("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/DesignVariantPreProcess.jsp?portal=FTRConfigurationDesignVariantPortal&amp;channel=FTRDVConfigurationFeatureChannel&amp;isIndentedTable=true");
    						 strDisplayName+=("&amp;contextIndependent=no&amp;objectId="+ XSSUtil.encodeForHTMLAttribute(context, strLogicalFeatureID)+","+XSSUtil.encodeForHTMLAttribute(context, contextProductOID) +"&amp;mode=viewConfigurationFeatures&amp;contextProductOID=" + XSSUtil.encodeForHTMLAttribute(context, contextProductOID) + "&amp;parentOID=");
    						 strDisplayName+=("', '', '', 'false', 'hiddenFrame');\" >");
    						 strDisplayName+=(XSSUtil.encodeForXML(context,strDisplayName1));
    						 strDisplayName+=("</a>");
    					 }
    				 }
    			 }
    			 vecResult.add(strDisplayName);
    		 }
    	 }
    	 catch (Exception e) {
    		 throw new FrameworkException(e);
    	 }
    	 return vecResult;
     }
     /**
      * Returns true if the LibraryCentral is installed otherwise false.
      * @mx.whereUsed This method will be called from part property pages
      * @mx.summary   This method check whether LibraryCentral is installed or not, this method can be used as access program to show/hide the ClassificationAttributes
      * @param context the eMatrix <code>Context</code> object.
      * @return boolean true or false based condition.
      * @throws Exception if the operation fails.
      * @since R214
      */
     public boolean isLBCInstalled(Context context,String[] args) throws Exception
     {
     boolean lcInstalled= FrameworkUtil.isSuiteRegistered(context,"appVersionLibraryCentral",false,null,null);
     if (lcInstalled)
           return true;
     else
           return false;
     }



     /** It is used to decide whether the command is allowed to display in DV report depending on the type of object
		 * @param context
		 * @param args
		 * @return  true if the object is of type Product Variant
		 * @throws FrameworkException
		 * For Add, Remove, Make Active / Inactive of DV Actions
		 */
     public static Boolean showDVActionsWhenLFInProductContext(Context context, String[] args)
     throws Exception {

    	 String featureID = "";
    	 String objProductID = "";

    	 boolean bFieldAccess = false;
    	 HashMap requestMap = (HashMap) JPO.unpackArgs(args);

    	 StringBuffer strContextId = new StringBuffer() ;
    	 if(requestMap.get("ProductID")!=null){
    		 strContextId.append((String) requestMap.get("ProductID"));
    	 }

    	 String objectId = (String) requestMap.get("objectId");
    	 if(objectId!=null && !objectId.trim().equals("")){
    		 StringTokenizer objIDs = new StringTokenizer(objectId, ",");
    		 // this is when action invoked from Reports --> View DV
    		 if(objIDs.countTokens()>1){
    			 // Context Feature ID
    			 featureID = objIDs.nextToken().trim();
    			 // Context Product ID
    			 objProductID = objIDs.nextToken().trim();
    			 String strProdType = "";
                 if(ProductLineCommon.isNotNull(objProductID)){
                 strProdType= (String) new DomainObject(objProductID).getInfo(context,DomainConstants.SELECT_TYPE);
                 }
    			 if(featureID.equals(objProductID)){
    				 bFieldAccess = false;
    			 } else if(!featureID.equals(objProductID) && mxType.isOfParentType(context,strProdType,ConfigurationConstants.TYPE_PRODUCT_VARIANT)){
    				 bFieldAccess = false;
    			 }
    			 else{
    				 String strFeatureType = (String) new DomainObject(featureID).getInfo(context,DomainConstants.SELECT_TYPE);
    				 if(( mxType.isOfParentType(context,strFeatureType,ConfigurationConstants.TYPE_LOGICAL_FEATURE))){
    					 bFieldAccess = true;
    				 }
    			 }
    		 }else if(ProductLineCommon.isNotNull(strContextId.toString())){ // this is when RMB --> View DV
    			 objProductID=objIDs.nextToken().trim();

    			 String strProdType = (String) new DomainObject(strContextId.toString()).getInfo(context,DomainConstants.SELECT_TYPE);
    			 if(strContextId.toString().equals(objProductID)){
    				 bFieldAccess = false;
    			 } else if(!objProductID.equals(strContextId.toString()) && mxType.isOfParentType(context,strProdType,ConfigurationConstants.TYPE_PRODUCT_VARIANT)){
    				 bFieldAccess = false;
    			 }
    			 else{
    				 bFieldAccess = true;
    			 }
    		 }else if(ProductLineCommon.isNotNull(objectId)){// this is from LF Context->DV Category
    			 String strType = (String) new DomainObject(objectId.toString()).getInfo(context,DomainConstants.SELECT_TYPE);
    			 if(( mxType.isOfParentType(context,strType,ConfigurationConstants.TYPE_LOGICAL_FEATURE))){
    				 bFieldAccess = true;
    			 }
    		 }
    	 }
    	 return bFieldAccess;
     }


     /** It is used to decide whether the command is allowed to display in DV report depending on the type of object
		 * @param context
		 * @param args
		 * @return  true if the object is of type Product Variant
		 * @throws FrameworkException
		 * For Make Valid / Invalid of DV Actions
		 */
     public static Boolean showDVActionsWhenLFInProductVariantContext(Context context, String[] args)
     throws Exception {

    	 String featureID = "";
    	 String objProductID = "";

    	 boolean bFieldAccess = false;
    	 HashMap requestMap = (HashMap) JPO.unpackArgs(args);

    	 StringBuffer strContextId = new StringBuffer() ;
    	 if(requestMap.get("ProductID")!=null){
    		 strContextId.append((String) requestMap.get("ProductID"));
    	 }

    	 String objectId = (String) requestMap.get("objectId");
    	 if(objectId!=null && !objectId.trim().equals("")){
    		 StringTokenizer objIDs = new StringTokenizer(objectId, ",");
    		 // this is when action invoked from Reports --> View DV
    		 if(objIDs.countTokens()>1){
    			 // Context Feature ID
    			 featureID = objIDs.nextToken().trim();
    			 // Context Product ID
    			 objProductID = objIDs.nextToken().trim();
    			 String strProdType = "";
                 if(ProductLineCommon.isNotNull(objProductID)){
                 strProdType = (String) new DomainObject(objProductID).getInfo(context,DomainConstants.SELECT_TYPE);
                 }
    			 if(featureID.equals(objProductID)){
    				 bFieldAccess = false;
    			 } else if(!featureID.equals(objProductID) && mxType.isOfParentType(context,strProdType,ConfigurationConstants.TYPE_PRODUCT_VARIANT)){
    				 bFieldAccess = true;
    			 }
    			 else{
    				 bFieldAccess = false;
    			 }
    		 }else if(ProductLineCommon.isNotNull(strContextId.toString())){ // this is when RMB --> View DV
    			 objProductID=objIDs.nextToken().trim();

    			 String strProdType = (String) new DomainObject(strContextId.toString()).getInfo(context,DomainConstants.SELECT_TYPE);
    			 if(strContextId.toString().equals(objProductID)){
    				 bFieldAccess = false;
    			 } else if(!objProductID.equals(strContextId.toString()) && mxType.isOfParentType(context,strProdType,ConfigurationConstants.TYPE_PRODUCT_VARIANT)){
    				 bFieldAccess = true;
    			 }
    			 else{
    				 bFieldAccess = false;
    			 }
    		 }else if(ProductLineCommon.isNotNull(objectId)){// this is from LF Context->DV Category
    			 String strType = (String) new DomainObject(objectId.toString()).getInfo(context,DomainConstants.SELECT_TYPE);
    			 if(( mxType.isOfParentType(context,strType,ConfigurationConstants.TYPE_LOGICAL_FEATURE))){
    				 bFieldAccess = false;
    			 }
    		 }
    	 }
    	 return bFieldAccess;
     }



     /** It is used to decide whether the command is allowed to display in DV report depending on the type of object
      * @param context
      * @param args
      * @return  true if the object is of type Product Variant
      * @throws FrameworkException
      * For View Common Group of DV Actions
      */
     public static Boolean showViewCommonGroupAction(Context context, String[] args)
     throws Exception {

    	 String featureID = "";
    	 String objProductID = "";

    	 boolean bFieldAccess = false;
    	 HashMap requestMap = (HashMap) JPO.unpackArgs(args);

    	 StringBuffer strContextId = new StringBuffer() ;
    	 if(requestMap.get("ProductID")!=null){
    		 strContextId.append((String) requestMap.get("ProductID"));
    	 }

    	 String objectId = (String) requestMap.get("objectId");
    	 if(objectId!=null && !objectId.trim().equals("")){
    		 StringTokenizer objIDs = new StringTokenizer(objectId, ",");
    		 // this is when action invoked from Reports --> View DV
    		 if(objIDs.countTokens()>1){
    			 // Context Feature ID
    			 featureID = objIDs.nextToken().trim();
    			 // Context Product ID
    			 objProductID = objIDs.nextToken().trim();

    			 if(featureID.equals(objProductID)){
    				 bFieldAccess = false;
    			 }else{
    				 String strFeatureType = (String) new DomainObject(featureID).getInfo(context,DomainConstants.SELECT_TYPE);
    				 if(( mxType.isOfParentType(context,strFeatureType,ConfigurationConstants.TYPE_LOGICAL_FEATURE))){
    					 bFieldAccess = true;
    				 }
    			 }
    		 }else  if(ProductLineCommon.isNotNull(strContextId.toString())){ // this is when RMB --> View DV
    			 objProductID=objIDs.nextToken().trim();

    			 if(strContextId.toString().equals(objProductID)){
    				 bFieldAccess = false;
    			 }else{
    				 bFieldAccess = true;
    			 }
    		 }else if(ProductLineCommon.isNotNull(objectId)){// this is from LF Context->DV Category
    			 String strType = (String) new DomainObject(objectId.toString()).getInfo(context,DomainConstants.SELECT_TYPE);
    			 if(( mxType.isOfParentType(context,strType,ConfigurationConstants.TYPE_LOGICAL_FEATURE))){
    				 bFieldAccess = true;
    			 }
    		 }
    	 }
    	 return bFieldAccess;
     }

     /** It is used to display "Display Name" and Name Column in Structure  Browser of Product Configuration Selected Options with the hyperlink for object id
		 * @param context
		 * @param args
		 * @return  Marketing Name For Products and Display Name for Logical Feature for Display Name column, Name+' '+Revision for Name column
		 * @throws FrameworkException
		 */
		public StringList displayNameHyperlinkForLogicalFeature(Context context, String []args) throws FrameworkException{
			String strHyperlinkName = "";
			StringList displayNameList = new StringList();
			try{
				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				MapList objectMapList = (MapList)programMap.get("objectList");
				HashMap columnMap = (HashMap)programMap.get("columnMap");
				String colName = (String)columnMap.get("name");
				
				HashMap paramMap = (HashMap)programMap.get("paramList");
				String reportFormat = (String)paramMap.get("reportFormat");

		    	String strName = null;
			String strRevision = null;
			String strDisplayName = null;
			String attrMarketName = "attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]";
			String attrRevision = DomainConstants.SELECT_REVISION;
			String attrDisplayName = "attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME	+"]";
			Map tempMap = null;
			StringList selectables = new StringList(ConfigurationConstants.SELECT_NAME);
			selectables.addElement(ConfigurationConstants.SELECT_REVISION);

		    	for(int i=0;i<objectMapList.size();i++)
		    	{
		    		Map objectMap = (Map) objectMapList.get(i);
		    		String strLogicalFeatureID = (String)objectMap.get(DomainConstants.SELECT_ID);
		    		DomainObject domObject = new DomainObject(strLogicalFeatureID);

		    		if(ProductLineCommon.isNotNull(colName) && colName.equals("Name")){
		    			strName = (String) objectMap.get(attrMarketName);
		    			strRevision = (String) objectMap.get(attrRevision);

		    			if(!(strName!=null && !strName.equals("")))
		    			{
			    			tempMap = domObject.getInfo(context, selectables);
			    			strName = (String) tempMap.get(ConfigurationConstants.SELECT_NAME);
			    			strRevision = (String) tempMap.get(ConfigurationConstants.SELECT_REVISION);
		    			}

		    			strDisplayName = strName + " " + strRevision;
					}
		    		else if (domObject.exists(context)
					&& (domObject.isKindOf(context,
							ConfigurationConstants.TYPE_PRODUCTS)
							|| domObject.isKindOf(context,
									ConfigurationConstants.TYPE_MODEL) || domObject
							.isKindOf(
									context,
									ConfigurationConstants.TYPE_PRODUCT_LINE)
									|| domObject
									.isKindOf(
											context,
											ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION)))
		    		{
		    			strName = (String) objectMap.get(attrMarketName);
		    			strRevision = (String) objectMap.get(attrRevision);
		    			strDisplayName = strName + " " + strRevision;
		    			if(!(strName!=null && !strName.equals("")))
		    			{
		    				strDisplayName = getDisplayName(context,strLogicalFeatureID,false);
		    			}
		    		}
		    		else
		    		{
		    			strName = (String) objectMap.get(attrDisplayName);
		    			strRevision = (String) objectMap.get(attrRevision);
		    			strDisplayName = strName + " " + strRevision;
		    			if(!(strName!=null && !strName.equals("")))
		    			{
		    				strDisplayName = getDisplayName(context,strLogicalFeatureID,true);
		    			}
		    		}

				if (ProductLineCommon.isNotNull(reportFormat)) {
					strHyperlinkName = strDisplayName;
				} else {
					strDisplayName=XSSUtil.encodeForXML(context, strDisplayName);
					strHyperlinkName = "<a href='javascript:showNonModalDialog(\"../common/emxTree.jsp?objectId="
							+ XSSUtil.encodeForHTMLAttribute(context,strLogicalFeatureID)
							+ "\",575,575)'>"
							+ strDisplayName + "</a>";

				}
		    		displayNameList.addElement(strHyperlinkName);
		    	}
			}
			catch (Exception e) {
				throw new FrameworkException(e);
			}
			return displayNameList;
		}
		/**
		 * Gets the Product composition for this Product in order to generate composition binary
		 *
		 * @param context The eMatrix <code>Context</code> object
		 * @param args holds a packed hashmap containing objectId
		 * @return MapList containing for Product composition data for the Product
		 * @throws Exception if the operation fails
		 */

		public MapList getProductComposition(Context context, String[] args)
		throws Exception {

			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strObjectid = (String)programMap.get("objectId");
			MapList mapPrdsAsLFStructure=new MapList();
			DomainObject domPrd=new DomainObject(strObjectid);
		if (domPrd.exists(context)
				&& (domPrd.hasRelatedObjects(context,
						ProductLineConstants.RELATIONSHIP_PRODUCTS, false) || domPrd
						.hasRelatedObjects(context,
								ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT,
								false))) {

				String RELATIONSHIP_PRODUCTS = PropertyUtil.getSchemaProperty(context, "relationship_Products");
				String RELATIONSHIP_MAIN_PRODUCT = PropertyUtil.getSchemaProperty(context, "relationship_MainProduct");
				String SELECT_MODEL_ID = "to["+RELATIONSHIP_PRODUCTS+"].from.physicalid";
				String SELECT_MODEL_NAME = "to["+RELATIONSHIP_PRODUCTS+"].from.name";
				String SELECT_MODEL_ID_MAINPRODUCT = "to["+RELATIONSHIP_MAIN_PRODUCT+"].from.physicalid";
				String SELECT_MODEL_NAME_MAINPRODUCT = "to["+RELATIONSHIP_MAIN_PRODUCT+"].from.name";
				String ATTRIBUTE_NODE_INDEX = PropertyUtil.getSchemaProperty(context, "attribute_NodeIndex");
				String RELATIONSHIP_PRODUCT_FEATURE_LIST = PropertyUtil.getSchemaProperty(context, "relationship_ProductFeatureList");
				String ATTRIBUTE_FEATURE_ALLOCATION_TYPE = PropertyUtil.getSchemaProperty(context, "attribute_FeatureAllocationType");

				StringList selectStmts = new StringList(9);
				//get basic data for Build
				selectStmts.add(DomainObject.SELECT_ID);
				selectStmts.add(DomainObject.SELECT_TYPE);
				selectStmts.add(DomainObject.SELECT_NAME);
				selectStmts.add(DomainObject.SELECT_REVISION);
				selectStmts.add("physicalid");
				selectStmts.add("revindex");
				selectStmts.add("attribute["+ATTRIBUTE_NODE_INDEX+"]");
				//Add selects for Model context
				selectStmts.add(SELECT_MODEL_ID);
				selectStmts.add(SELECT_MODEL_NAME);

				LogicalFeature logicalFeature = new LogicalFeature(strObjectid);
				String strTypePattern = ConfigurationConstants.TYPE_SOFTWARE_PRODUCT+","+ConfigurationConstants.TYPE_HARDWARE_PRODUCT+","+ConfigurationConstants.TYPE_SERVICE_PRODUCT;
				StringList relSelect= new StringList(DomainRelationship.SELECT_ID);

				StringBuffer whereRelClause = new StringBuffer(100);
				whereRelClause.append("tomid[");
				whereRelClause.append(RELATIONSHIP_PRODUCT_FEATURE_LIST);
				whereRelClause.append("].attribute[");
				whereRelClause.append(ATTRIBUTE_FEATURE_ALLOCATION_TYPE);
				whereRelClause.append("]==");
				whereRelClause.append(ConfigurationConstants.RANGE_VALUE_STANDARD);

				mapPrdsAsLFStructure = (MapList)logicalFeature.getLogicalFeatureStructure(context,strTypePattern, null, selectStmts, relSelect, false,
						true,1, 0, null, whereRelClause.toString(), (short)0,"");

				//The context object must be added as the first in the list
				Map parentProductMap = logicalFeature.getInfo(context, selectStmts);
				// Product Derivations are always enabled.
				boolean derivationsEnabled = true;
				parentProductMap.put("derivationsEnabled", derivationsEnabled);
				mapPrdsAsLFStructure.add(0,parentProductMap);

				//Need to re-key the Map for config context Id and Name so Composition Binary code can get it
				//add the configContext to each map
				for (int idx=0; idx < mapPrdsAsLFStructure.size(); idx++)
				{
					Map dataMap = (Map)mapPrdsAsLFStructure.get(idx);
					//Check if this is for Main Product, then need to get different selectable
					String modelId = (String)dataMap.get(SELECT_MODEL_ID);
					String modelName = (String)dataMap.get(SELECT_MODEL_NAME);
					if (modelId == null || "null".equalsIgnoreCase(modelId) || modelId.length() == 0)
					{
						modelId = (String)dataMap.get(SELECT_MODEL_ID_MAINPRODUCT);
						modelName = (String)dataMap.get(SELECT_MODEL_NAME_MAINPRODUCT);
					}
					dataMap.put("configContextId", modelId);
					dataMap.put("configContextName", modelName);
					//revindex should be different depending if derivations are enabled
					if (derivationsEnabled)
					{
						//put the Node Index attribute as the revindex value
						String nodeIndex = (String)dataMap.get("attribute["+ATTRIBUTE_NODE_INDEX+"]");
						if (nodeIndex == null || "null".equalsIgnoreCase(nodeIndex) || nodeIndex.length() <= 0)
						{
							nodeIndex = "0";
						}
						dataMap.put("revindex", nodeIndex);
					}
				}
			}
			return mapPrdsAsLFStructure;
		}

	     /**
	      * Relationship trigger invoked on modifying attribute on relationship Logical Structures to not allow
	      * setting of Find Number Quantity while structure is in frozen state.
	      * @param context
	      * @param args
	      * @return
	      * @throws Exception
	      */
	     public int checkAccessForSettingFindNumber(Context context, String [] args) throws Exception
	     {
	    	 int iResult = 0;
	    	 String ignoreModifyAttributeOnLogicalFeature = PropertyUtil.getGlobalRPEValue(context,"IgnoreModifyAttributeOnLogicalFeature");

			 if(!(ignoreModifyAttributeOnLogicalFeature != null && "TRUE".equals(ignoreModifyAttributeOnLogicalFeature))){

				 String strNoAccessMessage = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.Alert.AllProdFrozenState",context.getSession().getLanguage());
				 boolean isAttribute = false;
				 String fromObjectID = args[0];
				 String attrName = args[1];
				 StringList _attrList = new StringList();
				 _attrList.add(ConfigurationConstants.ATTRIBUTE_FIND_NUMBER);
				 _attrList.add(ConfigurationConstants.ATTRIBUTE_QUANTITY);
				 if(_attrList.contains(attrName))
				 {
					 isAttribute = true;
				 }
				 boolean isfrozen = false;

				 if(isAttribute){
					 isfrozen = ConfigurationUtil.isFrozenState(context, fromObjectID);
					 PropertyUtil.setGlobalRPEValue(context,"IgnoreModifyAttributeOnLogicalFeature","TRUE");
				 }
				if(isfrozen)
				//Modified for IR-180324V6R2014
				{
 					//Modified for removing check trigger block error message when user tries to update F/N & QTY value of logical feature
 					//${CLASS:emxContextUtilBase}.mqlNotice(context, strNoAccessMessage);
					iResult=1;
					throw new FrameworkException(strNoAccessMessage);
					//Modified for removing check trigger block error message when user tries to update F/N & QTY value of logical feature
				}
				//End of IR-180324V6R2014
			}

	    	 return iResult;

	     }
    // --------------------------------------------------------------------------------------
    // R212.Derivations JPO methods
    // --------------------------------------------------------------------------------------

	/** Method called as a trigger to fix the Product Feature List relationship when a product is
     * derived from another.
     *
     * @param context
     *            The eMatrix <code>Context</code> object
     * @param args []
     *            Holds the parameters passed from the calling method
     * @return void
     * @throws Exception
     *            If operation fails
     */
    public void replicateProductFeatureListRelationship (Context context,String args[]) throws Exception {

    	try {

	        // Get the Product Id of the new Derived Product.
	        String strParentId = args[0];
	        String strNewObjectName = args[1];
	        String strNewObjectRev = args[2];
	        String strNewObjectVault = args[3];

	        if (strParentId == null || strParentId.equals("null") || strParentId.equals("") ||
	        		strNewObjectName == null || strNewObjectName.equals("null") || strNewObjectName.equals("") ||
	        				strNewObjectRev == null || strNewObjectRev.equals("null") || strNewObjectRev.equals("")) {
	               return;
	        }

	        // Getting the new object Id
	        DomainObject doParent = DomainObject.newInstance(context, strParentId);
	        BusinessObject busObj = new BusinessObject(doParent.getInfo(context,SELECT_TYPE), strNewObjectName, strNewObjectRev, strNewObjectVault);
	        String strProductId = busObj.getObjectId(context);

	        // Top Level Logical Feature Relationships
	    	String LogicalFeatureRelSelectable =
	    			"from[" + ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES + "].id";
	    	// Top Level Logical Feature Objects
	    	String LogicalFeatureObjSelectable =
	    			"from[" + ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES + "].to.id";

	    	// Getting the PFL relationship once we have the correct logical features
	    	// These need to be run once we have the Top Level Logical Feature Objects.
	    	// This will return the PFL relationships IDS for both OLD and NEW Products.
	    	String ProductFeatureListSelectable_ID =
	    			"to[" + ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES + "].tomid[" +
	    					ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST + "].id";
	    	// This will return the corresponding Product IDS for both OLD and NEW Products.
	    	String ProductFeatureListSelectable_ProductID =
	    			"to[" + ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES + "].tomid[" +
	    					ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST + "].from.id";

	    	// Build the string array for Product Ids.
	    	String[] arrObjectIds = new String[2];
	    	arrObjectIds[0] = strParentId;
	    	arrObjectIds[1] = strProductId;

	    	// Build the select list for Logical Features
	    	StringList slLogSelects = new StringList(3);
	    	slLogSelects.add(DomainObject.SELECT_ID);
	    	slLogSelects.add(LogicalFeatureRelSelectable);
	    	slLogSelects.add(LogicalFeatureObjSelectable);

	    	// Get the Logical Feature Information
			MapList mlProductInfo = DomainObject.getInfo(context, arrObjectIds, slLogSelects);
			if (mlProductInfo == null) {
				return;
			}

			Map mapParentInfo = new HashMap();
			Map mapProductInfo = new HashMap();

			// Break the Maps down.
			for (int i = 0; i < mlProductInfo.size(); i++) {
				Map tempMap = (Map)mlProductInfo.get(i);
				String mapId = (String)tempMap.get(DomainObject.SELECT_ID);
				if (mapId.equals(strParentId)) {
					mapParentInfo = tempMap;
				} else  {
					mapProductInfo = tempMap;
				}
			}

			// Set up the variables for easy access.
			String sParentLFRels = (String)mapParentInfo.get(LogicalFeatureRelSelectable);
			if (ConfigurationUtil.isNotNull(sParentLFRels)) {
				//arrParentLFRels = sParentLFRels.split(SelectConstants.cSelectDelimiter);
			} else {
				return;
			}

			String[] arrParentLFObjs;
			String sParentLFObjs = (String)mapParentInfo.get(LogicalFeatureObjSelectable);
			if (ConfigurationUtil.isNotNull(sParentLFObjs)) {
				arrParentLFObjs = sParentLFObjs.split(SelectConstants.cSelectDelimiter);
			} else {
				return;
			}

			String[] arrProductLFRels;
			String sProductLFRels = (String)mapProductInfo.get(LogicalFeatureRelSelectable);
			if (ConfigurationUtil.isNotNull(sProductLFRels)) {
				arrProductLFRels = sProductLFRels.split(SelectConstants.cSelectDelimiter);
			} else {
				return;
			}

			String[] arrProductLFObjs;
			String sProductLFObjs = (String)mapProductInfo.get(LogicalFeatureObjSelectable);
			if (ConfigurationUtil.isNotNull(sProductLFObjs)) {
				arrProductLFObjs = sProductLFObjs.split(SelectConstants.cSelectDelimiter);
			} else {
				return;
			}

	    	// Build the select list for Product Feature List
	    	StringList slPFLSelects = new StringList(3);
	    	slPFLSelects.add(DomainObject.SELECT_ID);
	    	slPFLSelects.add(ProductFeatureListSelectable_ID);
	    	slPFLSelects.add(ProductFeatureListSelectable_ProductID);

	    	// Get the Product Feature List for the Parent
			MapList mlPFLInfo = DomainObject.getInfo(context, arrParentLFObjs, slPFLSelects);
			if (mlPFLInfo == null) {
				return;
			}

			// Process to find the broken PFL for each Logical Feature.
			for (int i = 0; i < mlPFLInfo.size(); i++) {
				Map tempMap = (Map)mlPFLInfo.get(i);
				String tempLFId = (String)tempMap.get(DomainObject.SELECT_ID);

				// Product Feature List Ids
				String[] arrPFLIds;
				String strPFLIds = (String)tempMap.get(ProductFeatureListSelectable_ID);
				if (ConfigurationUtil.isNotNull(strPFLIds)) {
					arrPFLIds = strPFLIds.split(SelectConstants.cSelectDelimiter);
				} else {
					return;
				}


				// Product Ids to find the bad one.
				String[] arrProductIds;
				String strProductIds = (String)tempMap.get(ProductFeatureListSelectable_ProductID);
				if (ConfigurationUtil.isNotNull(strPFLIds)) {
					arrProductIds = strProductIds.split(SelectConstants.cSelectDelimiter);
				} else {
					return;
				}

				String strPFLToChange = "";
				String strPFLNewToEnd = "";

				for (int j = 0; j < arrPFLIds.length; j++) {
					if (arrProductIds[j].equals(strProductId)) {
						// We have found the PFL we need to change the end of.
						strPFLToChange = arrPFLIds[j];
						// Find the connection it needs to be assigned to.
						for (int k = 0; k < arrProductLFObjs.length; k++) {
							if (tempLFId.equals(arrProductLFObjs[k])) {
								strPFLNewToEnd = arrProductLFRels[k];
								break;
							}
						}
						if (strPFLToChange.isEmpty() || strPFLNewToEnd.isEmpty()) {
							return;
						}

						// Now Change the end of the relationship.
				        ContextUtil.startTransaction(context, true);
 					    //String strMqlCommand = "modify connection "	+ strPFLToChange + " torel " + strPFLNewToEnd;
						String strMqlCommand = "modify connection $1 torel $2";
  					    MqlUtil.mqlCommand(context, strMqlCommand, true,strPFLToChange,strPFLNewToEnd);
  						ContextUtil.commitTransaction(context);
					}
				}
			}

    	} catch (Exception e) {
    		ContextUtil.abortTransaction(context);
			throw new FrameworkException(e.toString());
    	}

        return;
    }

	         /** This method is column program for displaying new window column.
	          * History : In DV scenario, objectId being sent is objectId follwed by comman and then product Id, its this way to manipulate
	          * commands based on context. But it gives error in case of new window scenario since AEF JPO receives comman in Object Id
	          * This JPO replaces the standard new window column approach by a programHTMLOutput column
			 * @param context
			 * @param args
			 * @return  Marketing Name For Products and Display Name for Logical Feature
	         * @throws Exception
			 */
	     public Vector displayNewWindowIcons(Context context, String []args) throws Exception{
	    	 Vector iconsList = new Vector();
	    	 HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    	 MapList objectMapList = (MapList)programMap.get("objectList");
	    	 StringBuffer newWindowLink = new StringBuffer();
	    	 HashMap paramMap = (HashMap)programMap.get("paramList");
		     String reportFormat = (String)paramMap.get("reportFormat");

		     try {
	    		 for(int i=0;i<objectMapList.size();i++)
	    		 {
	    			 Map objectMap = (Map) objectMapList.get(i);
	    			 String objectId = (String)objectMap.get(DomainConstants.SELECT_ID);
	    			 String strLogicalFeatureID ="";
	    			 if(ProductLineCommon.isNotNull(objectId) && objectId.indexOf(",") > 1){
	    				 strLogicalFeatureID = objectId.substring(0, objectId.indexOf(","));
	    			 }else{
	    				 strLogicalFeatureID = objectId;
	    			 }
	    			 if(!ProductLineCommon.isNotNull(reportFormat)) {
	    				 newWindowLink = newWindowLink
	    				 .append(" <a><img src=\"../common/images/iconNewWindow.gif")
	    				 .append("\" border=\"0\" align=\"middle\" ")
	    				 .append("onclick=\"javascript:showDialog('../common/emxTree.jsp?mode=insert&amp;AppendParameters=true&amp;treeSource=NewWindow&amp;objectId="
	    						 + XSSUtil.encodeForHTMLAttribute(context, strLogicalFeatureID)
	    						 + "')\"")
	    						 .append("/></a> ");
	    				 iconsList.add(newWindowLink.toString());
	    				 newWindowLink.delete(0, newWindowLink.capacity());
	    			 }else{
	    				 iconsList.add(newWindowLink.toString());
	    			 }
	    		 }
	    	 } catch (Exception e) {
	    		 e.printStackTrace();
	    	 }
	    	 return iconsList;
	     }



	     @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	     public StringList getDVOptions(Context context, String[] args) throws FrameworkException {
	         try {
	               Map programMap = (Map) JPO.unpackArgs(args);
	               String CONFIG_PARENT_ID = (String) programMap.get("CONFIG_PARENT_ID");

	               DomainObject contextFeatureObj = DomainObject.newInstance(context);
	               contextFeatureObj.setId(CONFIG_PARENT_ID);
	               MapList attendees = contextFeatureObj.getRelatedObjects(context,
	                                   ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES,  //String relPattern
	                                   ConfigurationConstants.TYPE_CONFIGURATION_FEATURES, //String typePattern
	                                   new StringList(SELECT_ID),          //StringList objectSelects,
	                                   null,                     //StringList relationshipSelects,
	                                   false,                     //boolean getTo,
	                                   true,                     //boolean getFrom,
	                                   (short)1,                 //short recurseToLevel,
	                                   null,    //String objectWhere,
	                                   "",                       //String relationshipWhere,
	                                   0,              //Query Limit
	                                   null,                     //Pattern includeType,
	                                   null,                     //Pattern includeRelationship,
	                                   null);

	               StringList ids = new StringList(attendees.size());
	               for (int i = 0; i < attendees.size(); i++) {
	                   Map attendee = (Map) attendees.get(i);
	                   ids.add(attendee.get(SELECT_ID));
	               }

	               return ids;
	           } catch (Exception e) {
	               throw new FrameworkException(e);
	           }
	       }

	     @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	     public StringList getContextMilestone(Context context, String[] args) throws FrameworkException {
	         try {
	        	   StringList ids = new StringList();
	               Map programMap = (Map) JPO.unpackArgs(args);
	               String LOGICAL_FEATURE_ID = (String) programMap.get("LOGICAL_FEATURE_ID");

	               LogicalFeature lfBean = new LogicalFeature(LOGICAL_FEATURE_ID);
	               StringList ctxModelList = lfBean.getAllContextModels(context, false);

	               String modelID = null;
	               com.matrixone.apps.productline.Model objModel = new com.matrixone.apps.productline.Model();
	               MapList milestoneMapList = null;
	               Map milestoneMap = null;
	               MapList enggMilestoneMapList = null;
	               Map enggMilestoneMap = null;
	               String milestoneId = null;

	               Iterator itr = ctxModelList.iterator();

	               while(itr.hasNext()){
	            	   modelID = (String)itr.next();
	            	   milestoneMapList = objModel.getModelMilestoneTracks(context, modelID, new StringList("Engineering"), new StringList(ConfigurationConstants.SELECT_ID),
	            			   new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID), true, false);

	            	   for(int j=0; j< milestoneMapList.size(); j++){
	            		   milestoneMap = (Map)milestoneMapList.get(j);
	            		   enggMilestoneMapList = (MapList)milestoneMap.get("Engineering");

	            		   for(int k=0; k< enggMilestoneMapList.size(); k++){
	            			   enggMilestoneMap = (Map)enggMilestoneMapList.get(k);
	            			   milestoneId = (String)enggMilestoneMap.get(ConfigurationConstants.SELECT_ID);
	            			   ids.add(milestoneId);
	            		   }
	            	   }
	               }

	               return ids;
	           } catch (Exception e) {
	               throw new FrameworkException(e);
	           }
	       }


	     public static StringList getBOMVisualCue (Context context, String[] args) throws FrameworkException{
		 		try{
		 			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		 			MapList objectMap = (MapList) inputMap.get("objectList");
		 			Map paramList = (Map) inputMap.get("paramList");
		 			String reportFormat = (String)paramList.get("reportFormat");
		 			String strIcon = "iconSmallBOMAttributeOverride.gif";
		 			
		 			boolean isOverride = false;
		 			String overrideValue = "";

		 			StringList returnStringList = new StringList (objectMap.size());
		 			for (int i = 0; i < objectMap.size(); i++) {

		 				Map outerMap = (Map)objectMap.get(i);

		 				isOverride =  isGBOMOverride(context, outerMap);

		 				if(isOverride){

		 					if(ProductLineCommon.isNotNull(reportFormat) && reportFormat.equals("CSV")){
		 						overrideValue = EnoviaResourceBundle.getProperty(context,"Configuration", "emxConfiguration.Image.BOMAttribute.ToolTip",context.getSession().getLanguage()).trim();
		 					}
		 					else{
		 						overrideValue = "<img src=\"../common/images/"
		                            + strIcon
		                            + "\" border=\"0\"  align=\"middle\" "
		                            + "TITLE=\""
		                            + " "
		                            + EnoviaResourceBundle.getProperty(context,"Configuration", "emxConfiguration.Image.BOMAttribute.ToolTip",context.getSession().getLanguage()).trim()
		                            + "\""
		                            + "/>";
		 					}
		 				}
		 				else{
		 					overrideValue = "";
		 				}

		 				returnStringList.addElement(overrideValue);

		 			}
		 			return returnStringList;
		 		}catch(Exception e) {
		 			e.printStackTrace();
		 			throw new FrameworkException(e.getMessage());
		 		}
		 	}

	     private static boolean isGBOMOverride (Context context, Map requestMap) throws FrameworkException{
		 		try{

		 			boolean isBOMInterfacePresent = false;

		 			String interfaceResult =  (String)requestMap.get(ConfigurationConstants.SELECT_INTERFACE_EBOM );

		 			if(ProductLineCommon.isNotNull(interfaceResult) && "true".equalsIgnoreCase(interfaceResult)){
		 				isBOMInterfacePresent = true;
		 			}

		 			return isBOMInterfacePresent;

		 		}catch(Exception e) {
		 			e.printStackTrace();
		 			throw new FrameworkException(e.getMessage());
		 		}
		 	}


	     public static StringList getQuantityGBOMAttribute (Context context, String[] args) throws FrameworkException{
	 		try{
	 			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
	 			MapList objectMap = (MapList) inputMap.get("objectList");
	 			String quantity = "";
	 			StringList returnStringList = new StringList (objectMap.size());

	 			for (int i = 0; i < objectMap.size(); i++) {
	 				Map outerMap = (Map)objectMap.get(i);

	 				quantity = (String)outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_QUANTITY);

	 				returnStringList.addElement(quantity);
	 			}
	 			return returnStringList;
	 		}catch(Exception e) {
	 			e.printStackTrace();
	 			throw new FrameworkException(e.getMessage());
	 		}
	 	}


	     public void updateQuantityGBOMAttribute(Context context, String[] args) throws Exception
	 	{

	    	 try {
	    		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    		 HashMap paramMap = (HashMap) programMap.get("paramMap");
	    		 String strRelId = (String) paramMap.get("relId");
	    		 String strNewAttributeValue = (String) paramMap.get("New Value");
	    		 Map attributeMap = new HashMap();
	    		 attributeMap.put(ConfigurationConstants.ATTRIBUTE_QUANTITY, strNewAttributeValue);

	    		 String [] strRelIdArr = new String[]{strRelId};
	    		 StringList strSelectable = new StringList(ConfigurationConstants.SELECT_INTERFACE_EBOM);
	    		 MapList mlRelInterfaceInfo = DomainRelationship.getInfo(context, strRelIdArr, strSelectable);
	    		 Map relInterfaceInfo = (Map)mlRelInterfaceInfo.get(0);
	    		 String isEBOMPresent = (String)relInterfaceInfo.get(ConfigurationConstants.SELECT_INTERFACE_EBOM);

	    		 if(ProductLineCommon.isNotNull(isEBOMPresent) && isEBOMPresent.equalsIgnoreCase("true")){
					DomainRelationship domRel = new DomainRelationship(strRelId);
					domRel.setAttributeValues(context, attributeMap);
	    		 }
	    		 else{
	    			 Map requestMap = (HashMap) programMap.get("requestMap");
		    		 String LFId = (String) requestMap.get("parentOID");
		    		 String contextId = (String) requestMap.get("prodId");
		    		 LogicalFeature logicalFTR= new LogicalFeature(LFId);
		    		 logicalFTR.setGBOMContext(context, contextId);

		    		 Map contextInfoMap = logicalFTR.getGBOMContextStructure(context);
		    		 if(contextId!=null && contextInfoMap.size()==0){
		    			 contextInfoMap = getBOMAttributes(context,LFId,contextId);
		    		 }
	    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_FIND_NUMBER, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_FIND_NUMBER));
	    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_COMPONENT_LOCATION, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_COMPONENT_LOCATION));
	    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_REFERENCE_DESIGNATOR, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR));
	    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_USAGE, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_USAGE));

	    			 ConfigurationUtil.addInterfaceAndSetAttributes(context, strRelId, "connection",
	    					 ConfigurationConstants.INTERFACE_EBOM, attributeMap);
	    		 }

	    	 } catch (FrameworkException e) {
	 			e.printStackTrace();
	 			throw new FrameworkException(e.getMessage());
	 		}
	 	}


	     public static StringList getFNGBOMAttribute (Context context, String[] args) throws FrameworkException{
	 		try{
	 			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
	 			MapList objectMap = (MapList) inputMap.get("objectList");
	 			String quantity = "";
	 			StringList returnStringList = new StringList (objectMap.size());

	 			for (int i = 0; i < objectMap.size(); i++) {
	 				Map outerMap = (Map)objectMap.get(i);

	 				quantity = (String)outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_FIND_NUMBER );

	 				returnStringList.addElement(quantity);

	 			}
	 			return returnStringList;
	 		}catch(Exception e) {
	 			e.printStackTrace();
	 			throw new FrameworkException(e.getMessage());
	 		}
	 	}


	     public void updateFNGBOMAttribute(Context context, String[] args) throws Exception
	 	{

	    	 try {
	    		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    		 HashMap paramMap = (HashMap) programMap.get("paramMap");
	    		 String strRelId = (String) paramMap.get("relId");
	    		 String strNewAttributeValue = (String) paramMap.get("New Value");
	    		 Map attributeMap = new HashMap();
	    		 attributeMap.put(ConfigurationConstants.ATTRIBUTE_FIND_NUMBER, strNewAttributeValue);

	    		 String [] strRelIdArr = new String[]{strRelId};
	    		 StringList strSelectable = new StringList(ConfigurationConstants.SELECT_INTERFACE_EBOM);
	    		 MapList mlRelInterfaceInfo = DomainRelationship.getInfo(context, strRelIdArr, strSelectable);
	    		 Map relInterfaceInfo = (Map)mlRelInterfaceInfo.get(0);
	    		 String isEBOMPresent = (String)relInterfaceInfo.get(ConfigurationConstants.SELECT_INTERFACE_EBOM);

	    		 if(ProductLineCommon.isNotNull(isEBOMPresent) && isEBOMPresent.equalsIgnoreCase("true")){
					DomainRelationship domRel = new DomainRelationship(strRelId);
					domRel.setAttributeValues(context, attributeMap);	    		 }
	    		 else{
	    			 Map requestMap = (HashMap) programMap.get("requestMap");
		    		 String LFId = (String) requestMap.get("parentOID");
		    		 String contextId = (String) requestMap.get("prodId");
		    		 LogicalFeature logicalFTR= new LogicalFeature(LFId);
		    		 logicalFTR.setGBOMContext(context, contextId);

		    		 Map contextInfoMap = logicalFTR.getGBOMContextStructure(context);
		    		 if(contextId!=null && contextInfoMap.size()==0){
		    			 contextInfoMap = getBOMAttributes(context,LFId,contextId);
		    		 }
	    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_QUANTITY, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_QUANTITY));
	    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_COMPONENT_LOCATION, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_COMPONENT_LOCATION));
	    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_REFERENCE_DESIGNATOR, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR));
	    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_USAGE, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_USAGE));

	    			 ConfigurationUtil.addInterfaceAndSetAttributes(context, strRelId, "connection",
	    					 ConfigurationConstants.INTERFACE_EBOM, attributeMap);
	    		 }

	    	 } catch (FrameworkException e) {
	 			e.printStackTrace();
	 			throw new FrameworkException(e.getMessage());
	 		}
	 	}


	     public static StringList getComponentLocationGBOMAttribute (Context context, String[] args) throws FrameworkException{
	 		try{
	 			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
	 			MapList objectMap = (MapList) inputMap.get("objectList");
	 			String quantity = "";
	 			StringList returnStringList = new StringList (objectMap.size());

	 			for (int i = 0; i < objectMap.size(); i++) {
	 				Map outerMap = (Map)objectMap.get(i);

	 				quantity = (String)outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_COMPONENT_LOCATION );

	 				returnStringList.addElement(quantity);

	 			}
	 			return returnStringList;
	 		}catch(Exception e) {
	 			e.printStackTrace();
	 			throw new FrameworkException(e.getMessage());
	 		}
	 	}


	     public void updateComponentLocationGBOMAttribute(Context context, String[] args) throws Exception
	 	{

	    	 try {
	    		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    		 HashMap paramMap = (HashMap) programMap.get("paramMap");
	    		 String strRelId = (String) paramMap.get("relId");
	    		 String strNewAttributeValue = (String) paramMap.get("New Value");
	    		 Map attributeMap = new HashMap();
	    		 attributeMap.put(ConfigurationConstants.ATTRIBUTE_COMPONENT_LOCATION, strNewAttributeValue);

	    		 String [] strRelIdArr = new String[]{strRelId};
	    		 StringList strSelectable = new StringList(ConfigurationConstants.SELECT_INTERFACE_EBOM);
	    		 MapList mlRelInterfaceInfo = DomainRelationship.getInfo(context, strRelIdArr, strSelectable);
	    		 Map relInterfaceInfo = (Map)mlRelInterfaceInfo.get(0);
	    		 String isEBOMPresent = (String)relInterfaceInfo.get(ConfigurationConstants.SELECT_INTERFACE_EBOM);

	    		 if(ProductLineCommon.isNotNull(isEBOMPresent) && isEBOMPresent.equalsIgnoreCase("true")){
					DomainRelationship domRel = new DomainRelationship(strRelId);
					domRel.setAttributeValues(context, attributeMap);	    		 }
	    		 else{
	    			 Map requestMap = (HashMap) programMap.get("requestMap");
		    		 String LFId = (String) requestMap.get("parentOID");
		    		 String contextId = (String) requestMap.get("prodId");
		    		 LogicalFeature logicalFTR= new LogicalFeature(LFId);
		    		 logicalFTR.setGBOMContext(context, contextId);

		    		 Map contextInfoMap = logicalFTR.getGBOMContextStructure(context);
		    		 if(contextId!=null && contextInfoMap.size()==0){
		    			 contextInfoMap = getBOMAttributes(context,LFId,contextId);
		    		 }
	    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_QUANTITY, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_QUANTITY));
	    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_FIND_NUMBER, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_FIND_NUMBER));
	    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_REFERENCE_DESIGNATOR, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR));
	    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_USAGE, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_USAGE));

	    			 ConfigurationUtil.addInterfaceAndSetAttributes(context, strRelId, "connection",
	    					 ConfigurationConstants.INTERFACE_EBOM, attributeMap);
	    		 }

	    	 } catch (FrameworkException e) {
	 			e.printStackTrace();
	 			throw new FrameworkException(e.getMessage());
	 		}
	 	}


	     public static StringList getRefDesignatorGBOMAttribute (Context context, String[] args) throws FrameworkException{
	 		try{
	 			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
	 			MapList objectMap = (MapList) inputMap.get("objectList");
	 			String quantity = "";
	 			StringList returnStringList = new StringList (objectMap.size());

	 			for (int i = 0; i < objectMap.size(); i++) {
	 				Map outerMap = (Map)objectMap.get(i);

	 				quantity = (String)outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR );

	 				returnStringList.addElement(quantity);

	 			}
	 			return returnStringList;
	 		}catch(Exception e) {
	 			e.printStackTrace();
	 			throw new FrameworkException(e.getMessage());
	 		}
	 	}


	     public void updateRefDesignatorGBOMAttribute(Context context, String[] args) throws Exception
	 	{

	    	 try {
	    		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    		 HashMap paramMap = (HashMap) programMap.get("paramMap");
	    		 String strRelId = (String) paramMap.get("relId");
	    		 String strNewAttributeValue = (String) paramMap.get("New Value");
	    		 Map attributeMap = new HashMap();
	    		 attributeMap.put(ConfigurationConstants.ATTRIBUTE_REFERENCE_DESIGNATOR, strNewAttributeValue);

	    		 String [] strRelIdArr = new String[]{strRelId};
	    		 StringList strSelectable = new StringList(ConfigurationConstants.SELECT_INTERFACE_EBOM);
	    		 MapList mlRelInterfaceInfo = DomainRelationship.getInfo(context, strRelIdArr, strSelectable);
	    		 Map relInterfaceInfo = (Map)mlRelInterfaceInfo.get(0);
	    		 String isEBOMPresent = (String)relInterfaceInfo.get(ConfigurationConstants.SELECT_INTERFACE_EBOM);

	    		 if(ProductLineCommon.isNotNull(isEBOMPresent) && isEBOMPresent.equalsIgnoreCase("true")){
					DomainRelationship domRel = new DomainRelationship(strRelId);
					domRel.setAttributeValues(context, attributeMap);	    		 }
	    		 else{
	    			 Map requestMap = (HashMap) programMap.get("requestMap");
		    		 String LFId = (String) requestMap.get("parentOID");
		    		 String contextId = (String) requestMap.get("prodId");
		    		 LogicalFeature logicalFTR= new LogicalFeature(LFId);
		    		 logicalFTR.setGBOMContext(context, contextId);

		    		 Map contextInfoMap = logicalFTR.getGBOMContextStructure(context);
		    		 if(contextId!=null && contextInfoMap.size()==0){
		    			 contextInfoMap = getBOMAttributes(context,LFId,contextId);
		    		 }
	    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_QUANTITY, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_QUANTITY));
	    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_FIND_NUMBER, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_FIND_NUMBER));
	    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_COMPONENT_LOCATION, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_COMPONENT_LOCATION));
	    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_USAGE, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_USAGE));

	    			 ConfigurationUtil.addInterfaceAndSetAttributes(context, strRelId, "connection",
	    					 ConfigurationConstants.INTERFACE_EBOM, attributeMap);
	    		 }

	    	 } catch (FrameworkException e) {
	 			e.printStackTrace();
	 			throw new FrameworkException(e.getMessage());
	 		}
	 	}

	     public static StringList getUsageGBOMAttribute (Context context, String[] args) throws FrameworkException{
		 		try{
		 			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		 			MapList objectMap = (MapList) inputMap.get("objectList");
		 			String strUsage = "";
		 			String strI18Value = "";
		 			StringList returnStringList = new StringList (objectMap.size());

		 			for (int i = 0; i < objectMap.size(); i++) {
		 				Map outerMap = (Map)objectMap.get(i);

		 				strUsage = (String)outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_USAGE );

		 				if(strUsage!=null && strUsage.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_AS_REQUIRED)){
							strI18Value = (EnoviaResourceBundle.getProperty(context,"Configuration", "emxProduct.Range.Usage.AsRequired",context.getSession().getLanguage())).trim();
						}
		 				else if(strUsage!=null && strUsage.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_PER_SALES_ORDER)){
							strI18Value = (EnoviaResourceBundle.getProperty(context,"Configuration", "emxProduct.Range.Usage.PerSalesOrder",context.getSession().getLanguage())).trim();
						}
		 				else if(strUsage!=null && strUsage.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_REFERENCE)){
							strI18Value = (EnoviaResourceBundle.getProperty(context,"Configuration", "emxProduct.Range.Usage.Reference",context.getSession().getLanguage())).trim();
						}
		 				else if(strUsage!=null && strUsage.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_REFERENCE_ENG)){
							strI18Value = (EnoviaResourceBundle.getProperty(context,"Configuration", "emxProduct.Range.Usage.Reference-Eng",context.getSession().getLanguage())).trim();
						}
		 				else if(strUsage!=null && strUsage.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_REFERENCE_MFG)){
							strI18Value = (EnoviaResourceBundle.getProperty(context,"Configuration", "emxProduct.Range.Usage.Reference-Mfg",context.getSession().getLanguage())).trim();
						}
		 				else{ //for putting default Standard value in case of GBOM category where context is missing
		 					strI18Value = (EnoviaResourceBundle.getProperty(context,"Configuration", "emxProduct.Range.Usage.Standard",context.getSession().getLanguage())).trim();
		 				}

		 				returnStringList.addElement(strI18Value);
		 			}

		 			return returnStringList;
		 		}catch(Exception e) {
		 			e.printStackTrace();
		 			throw new FrameworkException(e.getMessage());
		 		}
		 	}


		     public void updateUsageGBOMAttribute(Context context, String[] args) throws Exception
		     {

		    	 try {
		    		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
		    		 HashMap paramMap = (HashMap) programMap.get("paramMap");
		    		 String strRelId = (String) paramMap.get("relId");
		    		 String strNewAttributeValue = (String) paramMap.get("New Value");
		    		 Map attributeMap = new HashMap();
		    		 attributeMap.put(ConfigurationConstants.ATTRIBUTE_USAGE, strNewAttributeValue);

		    		 String [] strRelIdArr = new String[]{strRelId};
		    		 StringList strSelectable = new StringList(ConfigurationConstants.SELECT_INTERFACE_EBOM);
		    		 MapList mlRelInterfaceInfo = DomainRelationship.getInfo(context, strRelIdArr, strSelectable);
		    		 Map relInterfaceInfo = (Map)mlRelInterfaceInfo.get(0);
		    		 String isEBOMPresent = (String)relInterfaceInfo.get(ConfigurationConstants.SELECT_INTERFACE_EBOM);

		    		 if(ProductLineCommon.isNotNull(isEBOMPresent) && isEBOMPresent.equalsIgnoreCase("true")){
		    			 DomainRelationship domrel = new DomainRelationship(strRelId);
						 domrel.setAttributeValues(context,attributeMap);
		    		 }
		    		 else{
		    			 Map requestMap = (HashMap) programMap.get("requestMap");
			    		 String LFId = (String) requestMap.get("parentOID");
			    		 String contextId = (String) requestMap.get("prodId");
			    		 LogicalFeature logicalFTR= new LogicalFeature(LFId);
			    		 logicalFTR.setGBOMContext(context, contextId);

			    		 Map contextInfoMap = logicalFTR.getGBOMContextStructure(context);
			    		 if(contextId!=null && contextInfoMap.size()==0){
			    			 contextInfoMap = getBOMAttributes(context,LFId,contextId);
			    		 }
		    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_QUANTITY, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_QUANTITY));
		    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_FIND_NUMBER, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_FIND_NUMBER));
		    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_COMPONENT_LOCATION, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_COMPONENT_LOCATION));
		    			 attributeMap.put(ConfigurationConstants.ATTRIBUTE_REFERENCE_DESIGNATOR, (String)contextInfoMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR));

		    			 ConfigurationUtil.addInterfaceAndSetAttributes(context, strRelId, "connection",
		    					 ConfigurationConstants.INTERFACE_EBOM, attributeMap);
		    		 }

		    	 } catch (FrameworkException e) {
		    		 e.printStackTrace();
		    		 throw new FrameworkException(e.getMessage());
		    	 }
		     }
		     /**
		      * Method to get the LF BOM Attributes
		      *
		      * @param context
		      * @param lFId
		      * @param contextId
		      * @return
		      * @throws Exception
		      */
		     private Map getBOMAttributes(Context context, String lFId,
		    		 String contextId) throws Exception{
		    	 Map contextInfoMap = new HashMap();
		    	 LogicalFeature logicalFTR= new LogicalFeature(lFId);

		    	 String RelWhereForContext = "tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id=="+ contextId;

		    	 String typePatternForContext = ConfigurationConstants.TYPE_PRODUCTS+","+ConfigurationConstants.TYPE_LOGICAL_FEATURE;

		    	 String relPatternForContext = ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES;

		    	 StringList slRelSelects = new StringList();
		    	 slRelSelects.add(ConfigurationConstants.SELECT_ATTRIBUTE_QUANTITY );
		    	 slRelSelects.add(ConfigurationConstants.SELECT_ATTRIBUTE_FIND_NUMBER );
		    	 slRelSelects.add(ConfigurationConstants.SELECT_ATTRIBUTE_COMPONENT_LOCATION );
		    	 slRelSelects.add(ConfigurationConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR );
		    	 slRelSelects.add(ConfigurationConstants.SELECT_ATTRIBUTE_USAGE );
		    	 MapList contextObjectInfoList = logicalFTR.getLogicalFeatureStructure(context, typePatternForContext, relPatternForContext, new StringList("id"),
		    			 slRelSelects, true, false, 1, 1, DomainObject.EMPTY_STRING, RelWhereForContext,
		    			 (short)0, null);

		    	 if(contextObjectInfoList!=null && contextObjectInfoList.size()>0){
		    		 contextInfoMap = (Map)contextObjectInfoList.get(0);
		    	 }
		    	 return contextInfoMap;
		     }

		     public Map getRangeValuesForUsage(Context context,String[] args) throws Exception
		     {
		         String strAttributeName = ConfigurationConstants.ATTRIBUTE_USAGE;
		         HashMap rangeMap = new HashMap();
		         matrix.db.AttributeType attribName = new matrix.db.AttributeType(
		                 strAttributeName);
		         attribName.open(context);

		         StringList attributeRange = attribName.getChoices();

		         StringList attributeI18DisplayRange = EnoviaResourceBundle.
		         						getAttrRangeI18NStringList(context, strAttributeName, attributeRange, context.getSession().getLanguage());

		         rangeMap.put(FIELD_CHOICES, attributeRange);
		         rangeMap.put(FIELD_DISPLAY_CHOICES, attributeI18DisplayRange);

		         return rangeMap;
		     }



		     public String getRelID (Context context , String strParentId ,String strChildId )throws Exception {
					String strRelId  = "";
					try{
						if (strParentId == null || strChildId  == null ){
							return "";
						}

						String strMqlCmd = "expand bus $1 from relationship $2 select $3 $4 where $5 dump $6 " ;
						String strRelID = MqlUtil.mqlCommand(context ,strMqlCmd ,true, strParentId, ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS,
								"rel", "id", "to.id == "+strChildId, ConfigurationConstants.DELIMITER_PIPE) ;

						StringTokenizer selRelTokenizer = new StringTokenizer(strRelID,"|");

						while(selRelTokenizer.hasMoreTokens()){
							strRelId = selRelTokenizer.nextToken();
						}
						if (strRelId == null ){
							strRelId = "";
						}
						String strTempRelId[] = strRelId.split("\n");
						strRelId = strTempRelId[0];

					}catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					return strRelId ;
		     }

		     public Map getRelIDMap (Context context , String strParentId ,String strChildId )throws Exception {
		    	 	Map relMap = null;
					try{
						if (strParentId == null || strChildId  == null ){
							return null;
						}
						StringList relSelect = new StringList();
						relSelect.add("physicalid");
						relSelect.add("from.physicalid");
						relSelect.add("to.physicalid");

						String strObjWhere = "id == "+ strChildId;
						ConfigurationFeature cfBean = new ConfigurationFeature(strParentId);
						MapList mapConfigurationStructure = (MapList)cfBean.getConfigurationFeatureStructure(context,ConfigurationConstants.TYPE_CONFIGURATION_OPTION,
								ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS, new StringList(ConfigurationConstants.SELECT_ID),
								relSelect, false, true,0,0, strObjWhere.toString(), DomainObject.EMPTY_STRING,
								DomainObject.FILTER_STR_AND_ITEM,"");

						relMap = (Map)mapConfigurationStructure.get(0);
					}catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					return relMap ;
		     }

	     /**
	      * Access function for restrict the Leaf Level field for edit field
	      * @param context
	      * @param args
	      * @return
	      * @throws Exception
	      */
	     	public static boolean isLeafLevelShown(Context context, String[] args)
	     			throws Exception {
	     		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
	     		boolean isLeafLevel = false;
	     		String strContextOId = (String) paramMap.get("objectId");
	     		StringBuffer sb = new StringBuffer();
	     		sb.append("from[");
	     		sb.append(ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES);
	     		sb.append("].to.id");
	     		String strSelectable = sb.toString();
	     		DomainObject domContextObj = new DomainObject(strContextOId);
	     		String childAvail = domContextObj.getInfo(context, strSelectable);
	     		if (childAvail == null) {
	     			isLeafLevel = true;
	     		}
	     		return isLeafLevel;
	     	}
	     	/**
	     	 * filter the leaf level features in full search for copy/to
	     	 * @param context
	     	 * @param args
	     	 * @return
	     	 * @throws Exception
	     	 */
	     	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	     	public StringList filterLeafLevel(Context context, String[] args)
	     			throws Exception {
	     		StringList excludeLeafLevel = new StringList();
	     		try {
	     			HashMap programMap = (HashMap) JPO.unpackArgs(args);
	     			String strProductId = (String) programMap.get("objectId");

	     			excludeLeafLevel.add(strProductId);
	     			// get all the Logical Feature in the entire Database
	     			StringList objSelect = new StringList(2);
	     			objSelect.addElement(DomainConstants.SELECT_ID);
	     			objSelect
	     					.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_LEAF_LEVEL);
	     			MapList lstLFList = DomainObject.findObjects(context,
	     					ConfigurationConstants.TYPE_LOGICAL_FEATURE,
	     					DomainConstants.QUERY_WILDCARD, "", objSelect);

	     			for (int i = 0; i < lstLFList.size(); i++) {
	     				Map tempMAp = (Map) lstLFList.get(i);
	     				String atrValue = (String) tempMAp
	     						.get(ConfigurationConstants.SELECT_ATTRIBUTE_LEAF_LEVEL);
	     				String strId = (String) tempMAp.get(DomainConstants.SELECT_ID);
	     				if (atrValue
	     						.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_LEAFLEVEL_YES)) {
	     					excludeLeafLevel.add(strId);
	     				}
	     			}

	     		} catch (Exception e) {
	     			e.printStackTrace();
	     		}
	     		return excludeLeafLevel;
	     	}

	     	public static StringList isGBOMObjectEditable (Context context, String[] args) throws FrameworkException{
	    	    try{
	    	        HashMap inputMap = (HashMap)JPO.unpackArgs(args);
	    	        MapList objectMap = (MapList) inputMap.get("objectList");

	    	        StringList returnStringList = new StringList (objectMap.size());
	    	        for (int i = 0; i < objectMap.size(); i++) {

	    	            String isEBOMPresent = (String) ((Map) objectMap.get(i)).get("interface[EBOM]");

	    	            if ("TRUE".equalsIgnoreCase(isEBOMPresent))
	    	            {
	    	            	returnStringList.add(new Boolean(true));
	    	           	}else {
	    	           		returnStringList.add(new Boolean(false));
	    	           	}
	    	        }
	    	        return returnStringList;
	    	    }catch(Exception e) {
	    	        e.printStackTrace();
	    	        throw new FrameworkException(e.getMessage());
	    	    }
	    	}

	        /**
	         * Updates the composition binary when a Products relationship
	         * is created.
	         *
	         * @param context the ENOVIA <code>Context</code> object
	         * @param args String array of arguments in the following order
	         *   			[0] = relationship id
	         * 				[1] = relationship type
	         * 				[2] = from object id
	         * 				[3] = to object id
	         * @return int 0 for success and 1 for failure
	         * @throws Exception throws exception if the operation fails
	         */
	        public int updateCompositionBinaryOnProducts(Context context, String[]args) throws Exception
	        {
	            int returnStatus = 0;
	    		String strIPMLCommandName=PropertyUtil.getGlobalRPEValue(context,"IPMLCommandName");
	       		if (strIPMLCommandName == null|| "".equalsIgnoreCase(strIPMLCommandName)|| "null".equalsIgnoreCase(strIPMLCommandName)){
	    			String relType = args[1];
	    			if(!relType.equals(PropertyUtil.getSchemaProperty(context, "relationship_MainProduct"))){
	    				returnStatus = JPO.invoke(context, "emxCompositionBinary", args, "updateCompositionBinary", args);
	    			}
	    		}
	        	return returnStatus;
	        }

	/**
	 * This is used in Indexed search as program to get the PL/Product context
	 * which will be used to search DV under this context, in LF->add DV context
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String getDVContextIndexedSearch(Context context, String args[])
			throws Exception {
		String strCFIDArg = args[0];
		String strTypeArg = args[1];

		StringList objectSelects = new StringList(DomainConstants.SELECT_NAME);
		objectSelects.add(DomainConstants.SELECT_ID);
		String strType = PropertyUtil.getSchemaProperty(context, strTypeArg);

		List finalList = new StringList();
		String plNameToIndex = "";
		DomainObject domContextBus = new DomainObject(strCFIDArg);
		MapList mapContextIds = domContextBus.getRelatedObjects(context,
				ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES,
				strType, objectSelects, new StringList(), true, false,
				(short) 1, "", "", (short) 0, DomainObject.CHECK_HIDDEN,
				DomainObject.PREVENT_DUPLICATES,
				(short) DomainObject.PAGE_SIZE, null, null, null,
				DomainObject.EMPTY_STRING);

		if (!mapContextIds.isEmpty()) {
			Iterator itrContxt = mapContextIds.iterator();
			while (itrContxt.hasNext()) {
				Map mapContext = (Map) itrContxt.next();
				String sId = (String) mapContext.get(DomainConstants.SELECT_ID);
				if (!finalList.contains(sId)) {
					plNameToIndex += (String) mapContext
							.get(DomainConstants.SELECT_NAME);
					plNameToIndex += ConfigurationConstants.DELIMITER_PIPE;
					finalList.add(sId);
				}
			}
		}
		if (plNameToIndex.endsWith(ConfigurationConstants.DELIMITER_PIPE)) {
			int j = plNameToIndex.lastIndexOf(ConfigurationConstants.DELIMITER_PIPE);
			plNameToIndex = plNameToIndex.substring(0, j);
		}
		return plNameToIndex;
	}

	public MapList getMilestoneEffColumnsForGBOM(Context context, String[] args)
	throws Exception {

		// This will be MapList of the parts connected with GBOM relationship to
		// the logical Feature
		MapList returnList = new MapList();

		try {
			Map programMap = (HashMap) JPO.unpackArgs(args);
			Map requestMap = (HashMap) programMap.get("requestMap");
			String strRel = (String) requestMap.get("effectivityRelationship");
			String strObjectId = (String) requestMap.get("objectId");
			strRel = PropertyUtil.getSchemaProperty(context, strRel);
			String typeEffectivity = null;
			boolean msEffectivityFound = false;
			boolean modelAttachedFound = false;
			
			LogicalFeature lfBean = new LogicalFeature(strObjectId);
			StringList ctxModelList = lfBean.getAllContextModels(context, false);

			if(ctxModelList != null && ctxModelList.size() > 0){
				modelAttachedFound = true;
			}

			String cmd = "print relationship $1 select $2 dump";
			String effType =	MqlUtil.mqlCommand(context, cmd, true, strRel, "property[ENO_Effectivity].value");

			StringTokenizer stEff = new StringTokenizer(effType, ",");


			while(stEff.hasMoreTokens()){
				typeEffectivity = stEff.nextToken();
				if("Milestone".equals(typeEffectivity)){
					msEffectivityFound = true;
					break;
				}
			}

			StringBuffer rangeHref = new StringBuffer();

			rangeHref.append("../common/emxFullSearch.jsp?table=FTRMilestoneSearchResultTable&showInitialResults=true&selection=single&field=TYPES=type_Milestone&LOGICAL_FEATURE_ID=");
			rangeHref.append(strObjectId);
			rangeHref.append("&submitAction=refreshCaller&suiteKey=Configuration&includeOIDprogram=LogicalFeature:getContextMilestone&submitURL=../configuration/FTREffectivitySearchUtil.jsp&effType=MilestoneEffectivity");


			if(msEffectivityFound && modelAttachedFound){
				Map startMilestoneColumn = new HashMap();
				Map startSetting = new HashMap();
				startSetting.put("Registered Suite", "Configuration");
				startSetting.put("Editable", "true");
				startSetting.put("Column Type", "programHTMLOutput");
				startSetting.put("function", "getMSEffectivityValue");
				startSetting.put("program", "LogicalFeature");
				startSetting.put("Export", "true");
				startSetting.put("TypeAhead", "false");
				startSetting.put("Show Clear Button", "true");
				startSetting.put("Edit Access Program", "LogicalFeature");
				startSetting.put("Edit Access Function", "isEffGridEditableForContext");
				startMilestoneColumn.put("settings", startSetting);
				startMilestoneColumn.put("label", "emxConfiguration.Label.Table.StartMilestone");
				startMilestoneColumn.put("name", "StartMilestone");
				startMilestoneColumn.put("range",rangeHref.toString());

				returnList.add(startMilestoneColumn);

				Map endMilestoneColumn = new HashMap();
				Map endSetting = new HashMap();
				endSetting.put("Registered Suite", "Configuration");
				endSetting.put("Editable", "true");
				endSetting.put("Column Type", "programHTMLOutput");
				endSetting.put("function", "getMSEffectivityValue");
				endSetting.put("program", "LogicalFeature");
				endSetting.put("Export", "true");
				endSetting.put("TypeAhead", "false");
				endSetting.put("Show Clear Button", "true");
				endSetting.put("Edit Access Program", "LogicalFeature");
				endSetting.put("Edit Access Function", "isEffGridEditableForContext");
				endMilestoneColumn.put("settings", endSetting);
				endMilestoneColumn.put("label", "emxConfiguration.Label.Table.EndMilestone");
				endMilestoneColumn.put("name", "EndMilestone");
				endMilestoneColumn.put("range",rangeHref.toString());
				returnList.add(endMilestoneColumn);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
		return returnList;

	}

	public List getMSEffectivityValue(Context context, String[] args)
	throws Exception {
		List valueList = new StringList();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			List ObjectIdsList = (MapList) programMap.get("objectList");
			Map columnMap = (HashMap) programMap.get("columnMap");
			String columnName = (String) columnMap.get("name");
			String gbomRelID = null;
			Map partObj = null;
			MapList expressionMapList = null;
			Map expressionMap = null;
			String actualExpression = null;
			String individualCFExp = null;
			String unitExp = null;
			String mSPhyId = null;
			boolean effExist = false;
			String[] contextInfoSplit = null;
			String contextInfo = null;
			String contextName = null;
			String msName = null;
			int msIndex = 0;
			StringBuffer appendValueName = null;

			com.matrixone.apps.effectivity.EffectivityFramework Eff = new com.matrixone.apps.effectivity.EffectivityFramework();

			for (int i = 0; i < ObjectIdsList.size(); i++) {
				appendValueName = new StringBuffer();
				effExist = false;
				msIndex = 0;
				partObj = (Map) ObjectIdsList.get(i);
				gbomRelID = (String) partObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);

				expressionMapList = Eff.getRelExpression(context,gbomRelID);
				expressionMap = (Map)expressionMapList.get(0);
				actualExpression = (String)expressionMap.get("actualValue");
				//@EF_FO(PHY@EF:C0BE2927000005B851CBDCD8000002A7~C0BE292700001A9451CABF5900000467) AND
				//@EF_FO(PHY@EF:C0BE2927000005B851CBDD480000052C~C0BE292700001A9451CABF5900000467) AND
				//@EF_MS(PHY@EF:C0BE292700001A9451CAC1A300000780[C0BE292700001A9451CABEC600000270-C0BE292700001A9451CABECA000002C6])
				if(ProductLineCommon.isNotNull(actualExpression.trim())){
					String[] expressionSplit =  actualExpression.split("OR"); //this will devide for each Feature

					for(int j=0; j<expressionSplit.length; j++){ // for iterating AND

						individualCFExp = expressionSplit[j];
						String[] individualCFExpSplit = individualCFExp.split("AND"); //this will devide for each Option

						for(int k=0; k<individualCFExpSplit.length; k++){ // for iterating OR
							unitExp = individualCFExpSplit[k];
							unitExp = unitExp.trim();
							if(unitExp.contains("@EF_MS")){ //means this is MS Eff Part; process it...
								unitExp = unitExp.replaceAll(KEYWORD_PHYSID_PREFIX, "");
								if(ProductLineCommon.isNotNull(columnName) && columnName.equals("StartMilestone")){ // start Date Eff
									mSPhyId = unitExp.substring(unitExp.indexOf("[")+1,unitExp.indexOf("-"));
								}
								else{
									mSPhyId = unitExp.substring(unitExp.indexOf("-")+1,unitExp.indexOf("]"));
								}

								if(!mSPhyId.equals("^")){
									contextInfo = LogicalFeature.getMilestoneContext(context, mSPhyId);
									contextInfoSplit = contextInfo.split("\\|");

			                         if(contextInfoSplit.length == 4){
			                        	 contextName = contextInfoSplit[0];
			                        	 msName = contextInfoSplit[3];
			                         }

			                         if(msIndex > 0){
			                        	 appendValueName.append(" <br /> ");
			                        	 appendValueName.append(contextName);
			                        	 appendValueName.append("{");
			                        	 appendValueName.append(msName);
			                        	 appendValueName.append("}");
			                         }
			                         else{
			                        	 appendValueName.append(contextName);
			                        	 appendValueName.append("{");
			                        	 appendValueName.append(msName);
			                        	 appendValueName.append("}");
			                         }
			                         msIndex++;
								}
								effExist = true;
							}
						}
					}
				}
				if(effExist){
					valueList.add(appendValueName.toString());
				}
				else{
					valueList.add("");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}

		if(valueList.size()== 0){

			valueList.add(0, "");
		}

		return valueList;
	}

	public Vector displayMilestoneContext(Context context, String[] args)
    throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList lstObjectIdsList = (MapList) programMap.get("objectList");
		Map contextMap = null;
		Vector contextModel = new Vector();
		String strObjectId = null;
		Object type = null;
		Object name = null;
		StringList slTypeList = null;
		StringList slNameList = null;
		String strType = null;
		String strName = null;
		String[] strArrObjectId = new String[lstObjectIdsList.size()];
		String contextType = "to[Configuration Item].from.to[Configuration Criteria].from.type";
		String contextName = "to[Configuration Item].from.to[Configuration Criteria].from.name";

		DomainConstants.MULTI_VALUE_LIST.add(contextType);
		DomainConstants.MULTI_VALUE_LIST.add(contextName);

		StringList slSelectable = new StringList(contextType);
		slSelectable.add(contextName);

		for (int i = 0; i < lstObjectIdsList.size(); i++) {

			strObjectId = (String) ((Map) lstObjectIdsList.get(i))
		            .get(ConfigurationConstants.SELECT_ID);
			strArrObjectId[i] = strObjectId;
		}

		MapList contextMapList = DomainObject.getInfo(context, strArrObjectId, slSelectable);

		DomainConstants.MULTI_VALUE_LIST.remove(contextType);
		DomainConstants.MULTI_VALUE_LIST.remove(contextName);

		for(int j=0; j< contextMapList.size(); j++){
			contextMap = (Map)contextMapList.get(j);
			type = contextMap.get(contextType);
			name = contextMap.get(contextName);

			if(type != null){
				slTypeList = ProductLineCommon.convertObjToStringList(context, type);
				slNameList = ProductLineCommon.convertObjToStringList(context, name);
				for(int k=0; k< slTypeList.size(); k++){

					strType = (String)slTypeList.get(k);

					if(ProductLineCommon.isNotNull(strType) && mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_MODEL)){
						strName = (String)slNameList.get(k);
						contextModel.add(strName);
						break;
					}
				}
			}
			else{
				contextModel.add(DomainConstants.EMPTY_STRING);
			}
		}

		return contextModel;
	}

	public static boolean isEffectivityGridActive(Context context, String[] args )throws Exception
	{
		boolean isEffectivityGridActive = ConfigurationUtil.isEffectivityGridActive(context);
		//if Grid is on then check if LF has Model connected- 		
		if(isEffectivityGridActive){
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strParentId = (String) programMap.get("objectId");
			if(strParentId!=null){
				LogicalFeature lfBean = new LogicalFeature(strParentId);
				StringList slModelId = lfBean.getAllContextModels(context, true);
				if(slModelId.size()==0){ //it is not having any model will not show Grid Effectivity column
					isEffectivityGridActive=false;
				}
			}
		}
		return isEffectivityGridActive;
	}
	public static boolean isEffectivityGridDeactive(Context context, String[] args )throws FrameworkException
	{
		return !ConfigurationUtil.isEffectivityGridActive(context);
	}

	public static boolean isMSEffectivityGridActive(Context context, String[] args )throws Exception
	{
		boolean isEffGrid = ConfigurationUtil.isEffectivityGridActive(context);
		boolean isPRGInstalled = ProductLineUtil.isPRGInstalled(context);
		boolean isMSEffectivityGridActive = isEffGrid && isPRGInstalled;

		return isMSEffectivityGridActive;
	}

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getFirstLevelLogicalFeature(Context context, String[] args)
	throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strParentId = (String) programMap.get("objectId");
		String objWhere = ConfigurationConstants.EMPTY_STRING;
		String relWhere = ConfigurationConstants.EMPTY_STRING;

		LogicalFeature logicalFTR= new LogicalFeature(strParentId);

		int limit = 32767;

		// Obj and Rel pattern
		String typePattern = "";
		String relPattern = "";

		// Obj and Rel Selects
		StringList objSelects = new StringList(ConfigurationConstants.SELECT_ID);
		StringList relSelects = new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID);

		String filterExpression = (String) programMap
				.get("CFFExpressionFilterInput_OID");

		MapList firstLevelChildInfoList = logicalFTR.getLogicalFeatureStructure(context, typePattern, relPattern, objSelects,
				relSelects, false, true, 1, limit, objWhere, relWhere, (short)0, filterExpression);

		return firstLevelChildInfoList;
	}

	public int rebuildEffectivityExpressionForNewContext(Context context, String args[])
	throws Exception {

		int iReturn = 0;
		String strChildLFId = args[0];
		try {

			LogicalFeature comFtr = new LogicalFeature(strChildLFId);
			comFtr.rebuildEffectivityExpressionForNewContext(context);

		} catch (Exception e) {
			iReturn = 1;
			throw new FrameworkException(e.getMessage());
		}
		return iReturn;
	}
	
	
	/**
	 * This Methods is used to get the Logical Feature Structure.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the Logical Feature Structure Details
	 * @throws Exception
	 *             if the operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getLogicalFeatureStructureForProductContext(Context context, String[] args)
	throws Exception {

		MapList mapLogicalStructure =null;
		int limit = 32767; //Integer.parseInt(FrameworkProperties.getProperty(context,"emxConfiguration.Search.QueryLimit"));
		String strObjWhere = DomainObject.EMPTY_STRING;
		StringBuffer strRelWhere = new StringBuffer(DomainObject.EMPTY_STRING);

		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			//Gets the objectids and the relation names from args
			String strObjectid = (String)programMap.get("objectId");
			DomainObject domObject = new DomainObject(strObjectid);
			String strType = domObject.getInfo(context, DomainObject.SELECT_TYPE);			
			String strparentOID = (String)programMap.get("parentOID");			
			String sNameFilterValue = (String) programMap.get("FTRLogicalFeatureNameFilterCommand");
			String sLimitFilterValue = (String) programMap.get("FTRLogicalFeatureLimitFilterCommand");
					
			if (strparentOID!=null && !strparentOID.equals(strObjectid) && mxType.isOfParentType(context, strType,ConfigurationConstants.TYPE_PRODUCTS)) {
				return new MapList();
			}
			else{				
				boolean isCalledFromRule=false;
			  	if(sNameFilterValue==null){
			  		sNameFilterValue = (String) programMap.get("FTRLogicalFeatureNameFilterForRuleDialog");
			  		if(sNameFilterValue!=null) isCalledFromRule= true;
			  	}
	            if(sLimitFilterValue==null)
	            	sLimitFilterValue = (String) programMap.get("FTRLogicalFeatureLimitFilterForRuleDialog");
				// emxConfiguration.Search.QueryLimit
				if (sLimitFilterValue != null
						&& !(sLimitFilterValue.equalsIgnoreCase("*"))) {
					if (sLimitFilterValue.length() > 0) {
						limit = (short) Integer.parseInt(sLimitFilterValue);
						if (limit < 0) {
							limit = 32767;
						}
					}
				}
				StringBuffer strObjWherebuffer = new StringBuffer(); 
				if (sNameFilterValue != null
						&& !(sNameFilterValue.equalsIgnoreCase("*"))) {

					strObjWherebuffer.append("attribute[");
					strObjWherebuffer.append(ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME);
					strObjWherebuffer.append("] ~~ '");
					strObjWherebuffer.append(sNameFilterValue);
					strObjWherebuffer.append("'");
				}
				strObjWhere = strObjWherebuffer.toString();
				//if this is called from Rule, then add object where, to prevent invalid state object being seen in Rule context Tree
				if(isCalledFromRule){
					if(!strObjWhere.trim().isEmpty())
						strObjWhere=strObjWhere+" && "+RuleProcess.getObjectWhereForRuleTreeExpand(context,ConfigurationConstants.TYPE_LOGICAL_FEATURE);
					else
						strObjWhere=RuleProcess.getObjectWhereForRuleTreeExpand(context,ConfigurationConstants.TYPE_LOGICAL_FEATURE);
				}
				
				if(!ProductLineCommon.isNotNull(strparentOID)){
					strparentOID = strObjectid;
				}
				
				strRelWhere.append("from.type.kindof !=\"");
				strRelWhere.append(ConfigurationConstants.TYPE_PRODUCTS);
				strRelWhere.append("\" || from.id ==\"");
				strRelWhere.append(strparentOID);
				strRelWhere.append("\"");
				
				// call method to get the level details
				int iLevel = ConfigurationUtil.getLevelfromSB(context,args);
				String filterExpression = (String) programMap.get("CFFExpressionFilterInput_OID");

				// @To DO
				// need to revisit the selectables
				LogicalFeature lfBean = new LogicalFeature(strObjectid);

				StringList relSelect = new StringList();
				relSelect.add("tomid["+
						ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].from.id");
				relSelect.add("tomid["+ ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].attribute["+
						ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
				relSelect.addElement("tomid["+ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION+"].from["+ConfigurationConstants.TYPE_INCLUSION_RULE+"].id");
				relSelect.addElement("tomid["+ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION+"].from["+ConfigurationConstants.TYPE_INCLUSION_RULE+"].attribute["+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+"].value");

				// attribute selectable
				relSelect.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
				relSelect.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_FORCE_PART_REUSE);
				relSelect.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
				relSelect.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_QUANTITY);
				relSelect.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_USAGE);
				relSelect.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_FIND_NUMBER);
				relSelect.addElement("attribute["+ ConfigurationConstants.ATTRIBUTE_RULE_TYPE + "]");
				relSelect.addElement("attribute["+ ConfigurationConstants.ATTRIBUTE_USAGE + "]");
				
				//Pre-Fetch CFF attributes for better performance
			    relSelect = EffectivityFramework.addEffectivitySelectables(context, relSelect);

				StringList objSelectables = new StringList();
				objSelectables.addElement("attribute["+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME+ "]");
				objSelectables.addElement(DomainConstants.SELECT_REVISION);
				objSelectables.addElement(DomainConstants.SELECT_NAME);
				objSelectables.addElement(DomainConstants.SELECT_TYPE);
				objSelectables.addElement(DomainConstants.SELECT_CURRENT);
				objSelectables.addElement(DomainConstants.SELECT_OWNER);
				objSelectables.addElement("attribute["+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+ "]");
				objSelectables.addElement("physicalid");			
				objSelectables.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_VARIANT_DISPLAY_TEXT);
				objSelectables.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_LOGICAL_SELECTION_TYPE);
				objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_MANAGED_REVISION+ "]."+DomainObject.SELECT_FROM_NAME);
				objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+ "]."+DomainObject.SELECT_FROM_NAME);
				objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_MANAGED_REVISION+ "]."+DomainObject.SELECT_FROM_ID);
				objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+ "]."+DomainObject.SELECT_FROM_ID);
				objSelectables.addElement("from["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"]");
				objSelectables.addElement("from["+ConfigurationConstants.RELATIONSHIP_GBOM+"]."+DomainConstants.SELECT_TO_ID);
				objSelectables.addElement("from["+ConfigurationConstants.RELATIONSHIP_GBOM+"]."+DomainConstants.SELECT_TO_REVISION);
				objSelectables.addElement("from["+ConfigurationConstants.RELATIONSHIP_GBOM+"]."+DomainConstants.SELECT_TO_NAME);
				objSelectables.addElement("from["+ConfigurationConstants.RELATIONSHIP_GBOM+"]."+DomainConstants.SELECT_TO_TYPE);
				objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT+ "]."+DomainObject.SELECT_FROM_ID);
				objSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT+ "]."+DomainObject.SELECT_FROM_NAME);
				objSelectables.addElement("next");
				objSelectables.addElement(DomainConstants.SELECT_POLICY);
				mapLogicalStructure = (MapList)lfBean.getLogicalFeatureStructure(context,"", null, objSelectables, relSelect, false,
						true,iLevel,limit, strObjWhere, strRelWhere.toString(), DomainObject.FILTER_STR_AND_ITEM,filterExpression);
				// fetching EBOM related data
				if (mxType.isOfParentType(context, strType,ConfigurationConstants.TYPE_PART)) {
					MapList partRelatedObjects = (MapList) LogicalFeature.getEBOMsWithRelSelectables(context, args);
					HashMap hmTemp = new HashMap();
					hmTemp.put("expandMultiLevelsJPO", "true");
					partRelatedObjects.add(hmTemp);
					return partRelatedObjects;
				}

				if (mapLogicalStructure != null) {
					HashMap hmTemp = new HashMap();
					hmTemp.put("expandMultiLevelsJPO", "true");
					mapLogicalStructure.add(hmTemp);
				}
			}			
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return mapLogicalStructure;
	}
	/**
	 * This method is return the value for RDO  column 
	 * @param context
	 *            The ematrix context object.
	 * @param String[]
	 *            The args .
	 * @return StringList.	 
	 * @throws Exception
	 */
	public static StringList getRDO(Context context, String[] args) throws FrameworkException{
		try{
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			MapList objectMap = (MapList) inputMap.get("objectList");
			String strRDO = "";
			String strObjId = "";	
			DomainObject domObj=null;
		      // selectable for root node  
		   	 StringList objectSelects = new StringList();	    
	 	     objectSelects.add("to[Design Responsibility].from.name");
		    StringList returnStringList = new StringList (objectMap.size());
			for (int i = 0; i < objectMap.size(); i++) {
				Map outerMap = new HashMap();
				outerMap = (Map)objectMap.get(i);							
				strRDO =  (String)outerMap.get("to[Design Responsibility].from.name");
				if(outerMap.containsKey("parentLevel")){
					strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
		            domObj = DomainObject.newInstance(context, strObjId);				      
		            Map rootMap= domObj.getInfo(context,objectSelects);
		            strRDO=(String)rootMap.get("to[Design Responsibility].from.name");
		            returnStringList.add(strRDO);
				}
				returnStringList.add(strRDO);
			}
			return returnStringList;
		}catch(Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}
	/**
	 * This method is return the value for Display Text column 
	 * @param context
	 *            The ematrix context object.
	 * @param String[]
	 *            The args .
	 * @return StringList.	 
	 * @throws Exception
	 */
	public static StringList getDisplayText(Context context, String[] args)
			throws FrameworkException {
		try {
			HashMap inputMap = (HashMap) JPO.unpackArgs(args);
			MapList objectMap = (MapList) inputMap.get("objectList");
			String strDisplayText = "";
			String strObjId="";
			DomainObject domObj =null;
		      // selectable for root node  
		   	 StringList objectSelects = new StringList();	    
	 	      objectSelects.add(ConfigurationConstants.SELECT_ATTRIBUTE_VARIANT_DISPLAY_TEXT);
			StringList returnStringList = new StringList(objectMap.size());
			for (int i = 0; i < objectMap.size(); i++) {
				Map outerMap = new HashMap();
				outerMap = (Map) objectMap.get(i);
				strDisplayText = (String) outerMap
						.get(ConfigurationConstants.SELECT_ATTRIBUTE_VARIANT_DISPLAY_TEXT);
				 if(outerMap.containsKey("parentLevel")){
						strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
			            domObj = DomainObject.newInstance(context, strObjId);				      
			            Map rootMap= domObj.getInfo(context,objectSelects);
			            strDisplayText=(String)rootMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_VARIANT_DISPLAY_TEXT);
			            returnStringList.add(strDisplayText);
					}
				returnStringList.add(strDisplayText);
			}
			return returnStringList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}
	/**
	 * This method is return the value for selection type  column 
	 * @param context
	 *            The ematrix context object.
	 * @param String[]
	 *            The args .
	 * @return StringList.	 
	 * @throws Exception
	 */
	public static StringList getSelectionType(Context context, String[] args)
			throws FrameworkException {
		try {
			HashMap inputMap = (HashMap) JPO.unpackArgs(args);
			MapList objectMap = (MapList) inputMap.get("objectList");
			String strSelectionType = "";		
			
			String strLanguage = context.getSession().getLanguage();
			String strSelectionCriteriaSingle = (EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Logical_Selection_Type.Single", strLanguage)).trim();
			String strSelectionCriteriaMultiple = (EnoviaResourceBundle.getProperty(context, "Framework","emxFramework.Range.Logical_Selection_Type.Multiple", strLanguage)).trim();
			StringList returnStringList = new StringList(objectMap.size());
			for (int i = 0; i < objectMap.size(); i++) {
				Map outerMap = new HashMap();
				outerMap = (Map) objectMap.get(i);
				//strSelectionType = (String) outerMap
				//		.get(ConfigurationConstants.SELECT_ATTRIBUTE_LOGICAL_SELECTION_TYPE);
				if (outerMap
						.containsKey(ConfigurationConstants.SELECT_ATTRIBUTE_LOGICAL_SELECTION_TYPE)
						&& ProductLineCommon
						.isNotNull((String) outerMap
								.get(ConfigurationConstants.SELECT_ATTRIBUTE_LOGICAL_SELECTION_TYPE))){
					strSelectionType =  (String)outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_LOGICAL_SELECTION_TYPE);
				}else if (outerMap
						.containsKey(DomainObject.SELECT_ID)
						&& ProductLineCommon
								.isNotNull((String) outerMap
										.get(DomainObject.SELECT_ID))){
					DomainObject domObj = DomainObject.newInstance(context, (String) outerMap
							.get(DomainObject.SELECT_ID));
					strSelectionType=domObj.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_LOGICAL_SELECTION_TYPE);
				}
				String strI18Value = "";
				if (strSelectionType != null) {
					if (strSelectionType
							.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_SINGLE)) {
						strI18Value = strSelectionCriteriaSingle;

					} else if (strSelectionType
							.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_MULTIPLE)) {
						strI18Value = strSelectionCriteriaMultiple;
					}
				}
				if (!("".equalsIgnoreCase(strI18Value)
						|| "null".equalsIgnoreCase(strI18Value) || strI18Value == null)) {
					returnStringList.addElement(strI18Value);
				} else {
					returnStringList.addElement("");
				}
			}
			return returnStringList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}
 /**
  * Method to return the  range value of Logical selection type attribute
  * @param context
  * @param args
  * @return
  * @throws Exception
  */
 public Map getRangeValuesForSelectionType(Context context,String[] args) throws Exception 
 {
		String strAttributeName = ConfigurationConstants.ATTRIBUTE_LOGICAL_SELECTION_TYPE;
		HashMap rangeMap = new HashMap();
		matrix.db.AttributeType attribName = new matrix.db.AttributeType(
				strAttributeName);
		attribName.open(context);

		List attributeRange = attribName.getChoices();
		List attributeDisplayRange = i18nNow.getAttrRangeI18NStringList(ConfigurationConstants.ATTRIBUTE_LOGICAL_SELECTION_TYPE,(StringList) attributeRange, context.getSession().getLanguage());
		rangeMap.put(FIELD_CHOICES, attributeRange);
		rangeMap.put(FIELD_DISPLAY_CHOICES, attributeDisplayRange);
		return rangeMap;
	}
 /**
	 * This method is an Update Function on edit of Selection Type
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void updateSelectionType(Context context, String[] args) throws Exception 
 {

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String strObjId = (String) paramMap.get("objectId");
			String strNewAttributeValue = (String) paramMap.get("New Value");
			DomainObject domObj = new DomainObject(strObjId);
			domObj.setAttributeValue(context,
					ConfigurationConstants.ATTRIBUTE_LOGICAL_SELECTION_TYPE,
					strNewAttributeValue);
		} catch (FrameworkException e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}
	/**
	 * This method is return the value for Force Part Reuse  column 
	 * @param context
	 *            The ematrix context object.
	 * @param String[]
	 *            The args .
	 * @return StringList.	 
	 * @throws Exception
	 */
	public static StringList getForcePartReuse(Context context, String[] args)
			throws FrameworkException {
		try {
			HashMap inputMap = (HashMap) JPO.unpackArgs(args);
			MapList objectMap = (MapList) inputMap.get("objectList");
			String strForcePartReuse = "";
			StringList returnStringList = new StringList(objectMap.size());
			for (int i = 0; i < objectMap.size(); i++) {
				Map outerMap = new HashMap();
				outerMap = (Map) objectMap.get(i);
				//strForcePartReuse = (String) outerMap
				//		.get(ConfigurationConstants.SELECT_ATTRIBUTE_FORCE_PART_REUSE);
				if (outerMap
						.containsKey(ConfigurationConstants.SELECT_ATTRIBUTE_FORCE_PART_REUSE)
						&& ProductLineCommon
								.isNotNull((String) outerMap
										.get(ConfigurationConstants.SELECT_ATTRIBUTE_FORCE_PART_REUSE))){
					strForcePartReuse = (String) outerMap
							.get(ConfigurationConstants.SELECT_ATTRIBUTE_FORCE_PART_REUSE);
				}else if (outerMap
						.containsKey(DomainRelationship.SELECT_ID)
						&& ProductLineCommon
								.isNotNull((String) outerMap
										.get(DomainRelationship.SELECT_ID))){
					DomainRelationship domRel = new DomainRelationship((String) outerMap
							.get(DomainRelationship.SELECT_ID));
					strForcePartReuse=domRel.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_FORCE_PART_REUSE);
				}
				returnStringList.add(strForcePartReuse);
			}
			return returnStringList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}
	 /**
	  * Method to return the  range value of Force Part Reuse attribute
	  * @param context
	  * @param args
	  * @return
	  * @throws Exception
	  */
	 public Map getRangeValuesForForcePartReuse(Context context,String[] args) throws Exception 
 {
		String strAttributeName = ConfigurationConstants.ATTRIBUTE_FORCE_PART_REUSE;
		HashMap rangeMap = new HashMap();
		matrix.db.AttributeType attribName = new matrix.db.AttributeType(
				strAttributeName);
		attribName.open(context);

		List attributeRange = attribName.getChoices();
		List attributeDisplayRange = i18nNow.getAttrRangeI18NStringList(ConfigurationConstants.ATTRIBUTE_FORCE_PART_REUSE,(StringList) attributeRange, context.getSession().getLanguage());
		rangeMap.put(FIELD_CHOICES, attributeRange);
		rangeMap.put(FIELD_DISPLAY_CHOICES, attributeDisplayRange);
		return rangeMap;
	}
	
		/**
		 * This method is return the value for Component Location  column 
		 * @param context
		 *            The ematrix context object.
		 * @param String[]
		 *            The args .
		 * @return StringList.	 
		 * @throws Exception
		 */
	public static StringList getComponentLocation(Context context, String[] args)
			throws FrameworkException {
		try {
			HashMap inputMap = (HashMap) JPO.unpackArgs(args);
			MapList objectMap = (MapList) inputMap.get("objectList");
			String strComponentLocation = "";
			StringList returnStringList = new StringList(objectMap.size());
			for (int i = 0; i < objectMap.size(); i++) {
				Map outerMap = new HashMap();
				outerMap = (Map) objectMap.get(i);
				strComponentLocation = (String) outerMap
						.get(ConfigurationConstants.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
				returnStringList.add(strComponentLocation);
			}
			return returnStringList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}
	  /**
	 * This method is an Update Function on edit of Component Location
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void updateComponentLocation(Context context, String[] args) throws Exception 
 {

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String strRelId = (String) paramMap.get("relId");
			String strNewAttributeValue = (String) paramMap.get("New Value");
			DomainRelationship domRel = new DomainRelationship(strRelId);
			domRel.setAttributeValue(context,
					ConfigurationConstants.ATTRIBUTE_COMPONENT_LOCATION,
					strNewAttributeValue);
		} catch (FrameworkException e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}
	/**
	 * This method is return the value for Reference Designator  column 
	 * @param context
	 *            The ematrix context object.
	 * @param String[]
	 *            The args .
	 * @return StringList.	 
	 * @throws Exception
	 */
	public static StringList getReferenceDesignator(Context context,
			String[] args) throws FrameworkException {
		try {
			HashMap inputMap = (HashMap) JPO.unpackArgs(args);
			MapList objectMap = (MapList) inputMap.get("objectList");
			String strReferenceDesignator = "";
			StringList returnStringList = new StringList(objectMap.size());
			for (int i = 0; i < objectMap.size(); i++) {
				Map outerMap = new HashMap();
				outerMap = (Map) objectMap.get(i);
				//strReferenceDesignator = (String) outerMap
				//		.get(ConfigurationConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
				if (outerMap
						.containsKey(ConfigurationConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR)
						&& (null != outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR))){
					strReferenceDesignator = (String) outerMap
							.get(ConfigurationConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);

				}else if (outerMap
						.containsKey(DomainRelationship.SELECT_ID)
						&& ProductLineCommon
								.isNotNull((String) outerMap
										.get(DomainRelationship.SELECT_ID))){
					DomainRelationship domRel = new DomainRelationship((String) outerMap
							.get(DomainRelationship.SELECT_ID));
					strReferenceDesignator=domRel.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_REFERENCE_DESIGNATOR);
				}
				returnStringList.add(strReferenceDesignator);
			}
			return returnStringList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}
	
	  /**
	 * This method is an Update Function on edit of Reference Designator 
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void updateReferenceDesignator(Context context, String[] args) throws Exception 
 {

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String strRelId = (String) paramMap.get("relId");
			String strNewAttributeValue = (String) paramMap.get("New Value");
			DomainRelationship domRel = new DomainRelationship(strRelId);
			domRel.setAttributeValue(context,
					ConfigurationConstants.ATTRIBUTE_REFERENCE_DESIGNATOR,
					strNewAttributeValue);
		} catch (FrameworkException e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}
	/**
	 * This method is return the value for Quantity  column 
	 * @param context
	 *            The ematrix context object.
	 * @param String[]
	 *            The args .
	 * @return StringList.	 
	 * @throws Exception
	 */
	public static StringList getQuantity(Context context, String[] args)
			throws FrameworkException {
		try {
			HashMap inputMap = (HashMap) JPO.unpackArgs(args);
			MapList objectMap = (MapList) inputMap.get("objectList");
			String strQuantity = "";
			StringList returnStringList = new StringList(objectMap.size());
			for (int i = 0; i < objectMap.size(); i++) {
				Map outerMap = new HashMap();
				outerMap = (Map) objectMap.get(i);
				//strQuantity = (String) outerMap
				//		.get(ConfigurationConstants.SELECT_ATTRIBUTE_QUANTITY);
				if (outerMap
						.containsKey(ConfigurationConstants.SELECT_ATTRIBUTE_QUANTITY)
						&& ProductLineCommon
								.isNotNull((String) outerMap
										.get(ConfigurationConstants.SELECT_ATTRIBUTE_QUANTITY))){
					strQuantity = (String) outerMap
							.get(ConfigurationConstants.SELECT_ATTRIBUTE_QUANTITY);
				}else if (outerMap
						.containsKey(DomainRelationship.SELECT_ID)
						&& ProductLineCommon
								.isNotNull((String) outerMap
										.get(DomainRelationship.SELECT_ID))){
					DomainRelationship domRel = new DomainRelationship((String) outerMap
							.get(DomainRelationship.SELECT_ID));
					strQuantity=domRel.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_QUANTITY);
				}
				returnStringList.add(strQuantity);
			}
			return returnStringList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}
	  /**
	 * This method is an Update Function on edit of Quantity 
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void updateQuantity(Context context, String[] args) throws Exception 
 {

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String strRelId = (String) paramMap.get("relId");
			String strNewAttributeValue = (String) paramMap.get("New Value");
			DomainRelationship domRel = new DomainRelationship(strRelId);
			domRel.setAttributeValue(context,
					ConfigurationConstants.ATTRIBUTE_QUANTITY,
					strNewAttributeValue);
		} catch (FrameworkException e) {
			
			//Added for removing check trigger block error message when user tries to update quantity of logical feature
			String strErrMessage = e.getMessage();
			if(strErrMessage != null && strErrMessage.contains("#5000001:"))
			{
				int pos = strErrMessage.indexOf("#5000001:");
				if (pos > -1)
				{
					strErrMessage = strErrMessage.substring(pos+9).trim();
				}
			}
			throw new FrameworkException(strErrMessage);
			//Added for removing check trigger block error message when user tries to update quantity of logical feature
		}
	}
	/**
	 * This method is return the value for Find Number  column 
	 * @param context
	 *            The ematrix context object.
	 * @param String[]
	 *            The args .
	 * @return StringList.	 
	 * @throws Exception
	 */
	public static StringList getFindNumber(Context context, String[] args)
			throws FrameworkException {
		try {
			HashMap inputMap = (HashMap) JPO.unpackArgs(args);
			MapList objectMap = (MapList) inputMap.get("objectList");
			String strFindNumber = "";
			StringList returnStringList = new StringList(objectMap.size());
			for (int i = 0; i < objectMap.size(); i++) {
				Map outerMap = new HashMap();
				outerMap = (Map) objectMap.get(i);
				//strFindNumber = (String) outerMap
				//		.get(ConfigurationConstants.SELECT_ATTRIBUTE_FIND_NUMBER);
				if (outerMap
						.containsKey(ConfigurationConstants.SELECT_ATTRIBUTE_FIND_NUMBER) 
						&& (null != outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_FIND_NUMBER))){
					strFindNumber =  (String)outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_FIND_NUMBER);
				}else if (outerMap
						.containsKey(DomainRelationship.SELECT_ID)
						&& ProductLineCommon
								.isNotNull((String) outerMap
										.get(DomainRelationship.SELECT_ID))){
					DomainRelationship domRel = new DomainRelationship((String) outerMap
							.get(DomainRelationship.SELECT_ID));
					strFindNumber=domRel.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_FIND_NUMBER);
				}
				returnStringList.add(strFindNumber);
			}
			return returnStringList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}
	  /**
	 * This method is an Update Function on edit of Find Number 
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void updateFindNumber(Context context, String[] args) throws Exception 
 {

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String strRelId = (String) paramMap.get("relId");
			String strNewAttributeValue = (String) paramMap.get("New Value");
			DomainRelationship domRel = new DomainRelationship(strRelId);
			domRel.setAttributeValue(context,
					ConfigurationConstants.ATTRIBUTE_FIND_NUMBER,
					strNewAttributeValue);
		} catch (FrameworkException e) {
			//Added for removing check trigger block error message when user tries to update F/N value of logical feature
			String strErrMessage = e.getMessage();
			if(strErrMessage != null && strErrMessage.contains("#5000001:"))
			{
				int pos = strErrMessage.indexOf("#5000001:");
				if (pos > -1)
				{
					strErrMessage = strErrMessage.substring(pos+9).trim();
				}
			}
			throw new FrameworkException(strErrMessage);
			//Added for removing check trigger block error message when user tries to update F/N value of logical feature
		}
		
	}
	  /**
	 * This method is an Update Function on edit of Display Text
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void updateDisplayText(Context context, String[] args) throws Exception 
 {

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String strObjId = (String) paramMap.get("objectId");
			String strNewAttributeValue = (String) paramMap.get("New Value");
			DomainObject domObj = new DomainObject(strObjId);
			domObj.setAttributeValue(context,
					ConfigurationConstants.ATTRIBUTE_VARIANT_DISPLAY_TEXT,
					strNewAttributeValue);
		} catch (FrameworkException e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}
	/**
	 * Its is used to get the display name for Logical Feature and Market name for Products
	 * @param context
	 * @param objectID -- Logical Feature ID/Product ID
	 * @param displayName - boolean, true for LF and false for Products
	 * @return DisplayName for LF and Market Name for Products
	 * @throws FrameworkException
	 */
	public static StringList getDisplayNameForProductContext(Context context,
			String[] args) throws FrameworkException {
		try {
			HashMap inputMap = (HashMap) JPO.unpackArgs(args);
			MapList objectMap = (MapList) inputMap.get("objectList");
			String strDisplayName = ConfigurationConstants.EMPTY_STRING;
			String strMarktgName = ConfigurationConstants.EMPTY_STRING;
			String strRev = ConfigurationConstants.EMPTY_STRING;
			String strObjId = ConfigurationConstants.EMPTY_STRING;
			DomainObject domObj = null;
			// selectable for root node
			StringList objectSelects = new StringList();
			objectSelects.add(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
			objectSelects.add(ConfigurationConstants.SELECT_ATTRIBUTE_MARKETING_NAME);
			objectSelects.add(ConfigurationConstants.SELECT_REVISION);
			StringList returnStringList = new StringList(objectMap.size());
			for (int i = 0; i < objectMap.size(); i++) {
				Map outerMap = new HashMap();
				outerMap = (Map) objectMap.get(i);
				strDisplayName = (String) outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
				strMarktgName = (String) outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_MARKETING_NAME);
				strRev = (String) outerMap.get(ConfigurationConstants.SELECT_REVISION);
				String isRootNode = (String) outerMap.get("Root Node");
				if ("true".equals(isRootNode) || isRootNode == null && strDisplayName == null && strMarktgName == null) {
					strObjId = (String) outerMap.get(DomainConstants.SELECT_ID);
					domObj = DomainObject.newInstance(context, strObjId);
					Map rootMap = domObj.getInfo(context, objectSelects);
					strDisplayName = (String) rootMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
					String strMarketingName = (String) rootMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_MARKETING_NAME);
					strRev = (String) rootMap.get(ConfigurationConstants.SELECT_REVISION);
					if (strDisplayName == null || strDisplayName.equals("")) {
						strDisplayName = strMarketingName;
					}
					returnStringList.add(strDisplayName + " " +  strRev);

				} else {
					if (strDisplayName == null || strDisplayName.equals("")) {
						strDisplayName = strMarktgName;
					}
					returnStringList.add(strDisplayName + " " +  strRev);
				}
			}
			return returnStringList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}
	     /**
     * It is used to display design Varinats in Logical Feature structure table
     * @param context
     * @param args
     * @return returns Yes if Design Variants are connected, else No
     * @throws FrameworkException
     */
    public Vector getDesignVariantsForProductContext(Context context, String[] args)
 throws FrameworkException {

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			Map paramList = (Map) programMap.get("paramList");
			String reportFormat = (String)paramList.get("reportFormat");
			String strCallingContext = (String) paramList.get("FromContext");
			String strModelCallingContext = (String) paramList
					.get("fromContext");
			String strMode = "";
			if (strCallingContext != null && !strCallingContext.equals("")
					&& strCallingContext.equals("Logical")) {
				strMode = "viewDesignVariantinLF";
			} else if (strModelCallingContext != null
					&& !strModelCallingContext.equals("")
					&& strModelCallingContext.equals("Candidate")) {
				strMode = "viewDesignVariantinLF";
			}
			MapList lstObjectIdsList = (MapList) programMap.get("objectList");
			Vector contextDesignVariants = new Vector();
			String strParentId = (String) paramList.get("parentOID");
			StringBuffer sTabLink = new StringBuffer(260);
			sTabLink
					.append("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/DesignVariantPreProcess.jsp?mode="
							+ strMode
							+ "&amp;objectId=${OBJECT_ID},"
							+ strParentId);
			String strTabPart1 = "', '800', '700', 'true', 'listHidden')\"";
			String strTabPart2 = ">";
			String strTabPart3 = "</a>";
			String strDesVar = "No";
			String strVaraintExist = "False";
			boolean personAccess = false;
			String strLanguage = context.getSession().getLanguage();
			
			String strNo = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxConfiguration.DesignVarianceExists.FalseValue",strLanguage);
			String strYes = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxConfiguration.DesignVarianceExists.TrueValue",strLanguage);

			if (PersonUtil.hasAssignment(context, PropertyUtil
					.getSchemaProperty(context, "role_ProductManager"))
					|| PersonUtil.hasAssignment(context, PropertyUtil
							.getSchemaProperty(context, "role_SystemEngineer"))) {
				personAccess = true;
			}

			for (int i = 0; i < lstObjectIdsList.size(); i++) {
				Map mpLF = (Map) lstObjectIdsList.get(i);
				String strObjId = (String) mpLF.get(DomainObject.SELECT_ID);
				String strRootNode = (String) mpLF.get("Root Node");
				if (strRootNode != null && strRootNode.equalsIgnoreCase("True"))
					contextDesignVariants.add(DomainConstants.EMPTY_STRING);
				else {
					strVaraintExist = (String) mpLF.get("from["
							+ ConfigurationConstants.RELATIONSHIP_VARIES_BY
							+ "]");
					strDesVar = strNo;

					if (strVaraintExist != null
							&& strVaraintExist.equalsIgnoreCase("True")) {
						strDesVar = strYes;
					}
					String strValue = "";
					if (personAccess) {

						strValue = FrameworkUtil.findAndReplace(sTabLink
								.toString(), "${OBJECT_ID}", strObjId)
								+ strTabPart1
								+ strTabPart2
								+ strDesVar
								+ strTabPart3;

					contextDesignVariants.add(strValue);
					} else {
						strValue = strDesVar;
					
				 	// Added for IR-190361V6R2014

				 	//contextDesignVariants.add(strValue);
    				StringBuffer temp = new StringBuffer();
    				if (reportFormat != null && !("null".equalsIgnoreCase(reportFormat)) && reportFormat.equalsIgnoreCase("CSV"))
    				{
	            		temp.append(strValue);
	            	}
    				else{
    				temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/DesignVariantPreProcess.jsp?mode=hyperlinkViewDesignVariant");
						temp.append("&amp;objectId=");
						temp.append(XSSUtil.encodeForHTMLAttribute(context, strObjId));
						temp.append("&amp;productID=");
					temp.append(XSSUtil.encodeForHTMLAttribute(context, strParentId));
						temp.append("', '450', '300', 'true', 'hiddenFrame')\">");
						temp.append(XSSUtil.encodeForXML(context,strValue));
						temp.append("</a>");
    				}
						String output =  temp.toString();
					contextDesignVariants.add(output);

					// End of IR-190361V6R2014
				}

					//contextDesignVariants.add(strValue);
				}

			}
			return contextDesignVariants;

		} catch (Exception e) {
			throw new FrameworkException(e.toString());
		}

	}
     /**
     * This method is used to return the Name of the part family with Hyper link of Logical Feature Table
    *
    * @param context
    *            the eMatrix <code>Context</code> object
    * @param args
    *            holds arguments
    * @return Vector- the List of Strings containing part families name.
    * @throws FrameworkException
    *             if the operation fails
    */
	public Vector getPartFamilyLinkForProductContext(Context context,
			String[] args) throws FrameworkException {
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectMapList = (MapList) programMap.get("objectList");
			HashMap paramMap = (HashMap) programMap.get("paramList");
			String reportFormat = (String) paramMap.get("reportFormat");
			String suiteDir = (String) paramMap.get("SuiteDirectory");
			String suiteKey = (String) paramMap.get("suiteKey");
			Vector partList = new Vector();
			Object objPartFamilyName =null ;
			Object objPartFamilyID=null;
			Object objGBOMTOTYPE=null;

			String strPartFamilyName = "";
			String strPartFamilyID = "";

			String strObjId = "";
			DomainObject domObj = null;
			// selectable for create and add existing
			StringList objectSelects = new StringList();
			String strID = "from["+ConfigurationConstants.RELATIONSHIP_GBOM+"]."+DomainConstants.SELECT_TO_ID;
			String strType = "from["+ConfigurationConstants.RELATIONSHIP_GBOM+"]."+DomainConstants.SELECT_TO_TYPE;
			String strName = "from["+ConfigurationConstants.RELATIONSHIP_GBOM+"]."+DomainConstants.SELECT_TO_NAME;

			DomainConstants.MULTI_VALUE_LIST.add(strID);
			DomainConstants.MULTI_VALUE_LIST.add(strType);
			DomainConstants.MULTI_VALUE_LIST.add(strName);

			objectSelects.add(strID);
			objectSelects.add(strType);
			objectSelects.add(strName);

			for (int i = 0; i < objectMapList.size(); i++) {
				Map objectMap = (Map) objectMapList.get(i);
				String output = "";
				//for IR-368989-3DEXPERIENCER2016x - display issue after adding part family under LF
				objPartFamilyName =null ;
				objPartFamilyID=null;
				objGBOMTOTYPE=null;
				if (objectMap.size() > 0) {
					String objectType = "";
					if (objectMap.containsKey(strID)) {
						objPartFamilyName = objectMap
								.get(strName);
						objPartFamilyID = objectMap
								.get(strID);
						objGBOMTOTYPE = objectMap
								.get(strType);
					} else if (objectMap.containsKey("parentLevel")) {
						strObjId = (String) objectMap
								.get(DomainConstants.SELECT_ID);
						domObj = DomainObject.newInstance(context, strObjId);
						Map rootMap = domObj.getInfo(context, objectSelects);
						if (rootMap.containsKey(strID)) {
							objPartFamilyID = rootMap.get(strID);
							objGBOMTOTYPE = rootMap.get(strType);
							objPartFamilyName = rootMap.get(strName);
						}
					}
					StringList slGBOMToTypes=ConfigurationUtil.convertObjToStringList(context, objGBOMTOTYPE);
					StringList slPartFamilyID=ConfigurationUtil.convertObjToStringList(context, objPartFamilyID);
					StringList slPartFamilyName=ConfigurationUtil.convertObjToStringList(context, objPartFamilyName);
					StringBuffer temp = new StringBuffer();
					for (int k = 0; k < slGBOMToTypes.size(); k++) {
						if(mxType.isOfParentType(context, (String) slGBOMToTypes.get(k),
								ConfigurationConstants.TYPE_PART_FAMILY)){
							if (reportFormat != null
									&& !("null".equalsIgnoreCase(reportFormat))
									&& reportFormat.length() > 0) {
								temp.append(XSSUtil.encodeForHTML(context,(String) slPartFamilyName.get(k)));
								temp.append(",");
							} else {
								temp = new StringBuffer();
								temp.append(" <img border=\"0\" src=\"");
								temp.append("images/iconSmallPartFamily.gif");
								temp.append("\" /> ");
								temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=configuration");
								temp.append("&amp;suiteKey=Configuration");
								temp.append("&amp;objectId=");
								temp.append(XSSUtil.encodeForHTMLAttribute(context,  (String) slPartFamilyID.get(k)));
								temp.append("', '450', '300', 'true', 'popup')\">");
								temp.append(XSSUtil.encodeForXML(context, (String) slPartFamilyName.get(k)));
								temp.append("</a>");
								temp.append(",");
							}
							output = output + temp;
						}
					}

					if(output.length()>0){
						int j = output.lastIndexOf(",");
						output = output.substring(0, j);
						partList.add(output);
					}
					else{
						partList.add(DomainConstants.EMPTY_STRING);
					}
				}
			}

			return partList;

		} catch (Exception e) {
			throw new FrameworkException(e.toString());
		}

	}
    /**
     * This method is used to return the Name of the Master Composition with Hyper link of Logical Feature Table
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds arguments
     * @return Vector- the List of Strings containing Master Composition name.
     * @throws FrameworkException
     *             if the operation fails
     */
	public Vector getMasterCompositionLinkForProductContext(Context context,
			String args[]) throws Exception {
		Vector partList = new Vector();
		try {

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectMapList = (MapList) programMap.get("objectList");
			HashMap paramMap = (HashMap) programMap.get("paramList");
			String reportFormat = (String) paramMap.get("reportFormat");
			String suiteDir = (String) paramMap.get("SuiteDirectory");
			String suiteKey = (String) paramMap.get("suiteKey");
			String strMasterFeatureName = null;
			String strMasterFeatureID = null;
			String attrMarketName = "to["
					+ ConfigurationConstants.RELATIONSHIP_PRODUCTS + "]."
					+ DomainObject.SELECT_FROM_NAME;
			String attrModelID = "to["
					+ ConfigurationConstants.RELATIONSHIP_PRODUCTS + "]."
					+ DomainObject.SELECT_FROM_ID;
			String strImage = null;
			for (int i = 0; i < objectMapList.size(); i++) {
				String output = "";
				Map objectMap = (Map) objectMapList.get(i);
				String strLogicalFeatureID = (String) objectMap
						.get(DomainConstants.SELECT_ID);
				String strLogicalFeatureType = (String) objectMap
						.get(DomainConstants.SELECT_TYPE);
				DomainObject domObject = new DomainObject(strLogicalFeatureID);
				if (objectMap.containsKey("parentLevel")) {
					strLogicalFeatureType = domObject.getInfo(context,
							DomainConstants.SELECT_TYPE);
				}
				String isRootNode = (String) objectMap.get("Root Node");

				if (strLogicalFeatureType != null
						&& strLogicalFeatureType
								.equalsIgnoreCase(ConfigurationConstants.TYPE_HARDWARE_PRODUCT)
						|| (isRootNode != null && isRootNode
								.equalsIgnoreCase("true"))) {

					strMasterFeatureName = (String) objectMap
							.get(attrMarketName);
					strImage = "images/iconSmallProduct.gif";
					strMasterFeatureID = (String) objectMap.get(attrModelID);
					if (!(strMasterFeatureName != null && !strMasterFeatureName
							.equals(""))) {
						Map tempMap = getMasterComposition(context,
								strLogicalFeatureID, false);

						if (objectMap
								.containsKey("to["
										+ ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT
										+ "]." + DomainObject.SELECT_FROM_ID)) {
							strMasterFeatureName = (String) tempMap
									.get("to["
											+ ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT
											+ "]."
											+ DomainObject.SELECT_FROM_NAME);
							strMasterFeatureID = (String) tempMap
									.get("to["
											+ ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT
											+ "]."
											+ DomainObject.SELECT_FROM_ID);

						} else {
							strMasterFeatureName = (String) tempMap
									.get(attrMarketName);
							strMasterFeatureID = (String) tempMap
									.get(attrModelID);
						}
					}
					if (strMasterFeatureName != null) {
						StringBuffer temp = new StringBuffer();
						if (reportFormat != null
								&& !("null".equalsIgnoreCase(reportFormat))
								&& reportFormat.length() > 0) {
							temp.append(strMasterFeatureName);
						} else {
							temp.append(" <img border=\"0\" src=\"");
							temp.append(strImage);
							temp.append("\" /> ");
							temp
									.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
							temp.append(suiteDir);
							temp.append("&amp;suiteKey=");
							temp.append(suiteKey);
							temp.append("&amp;objectId=");
							temp.append(strMasterFeatureID);
							temp.append("', '450', '300', 'true', 'popup')\">");
							temp.append(XSSUtil.encodeForHTMLAttribute(context,
									strMasterFeatureName));
							temp.append("</a>");
						}
						output = temp.toString();
						if (output.length() > 0)
							partList.add(output);
					} else {
						partList.add(DomainObject.EMPTY_STRING);
					}

				} else {
					partList.add(DomainObject.EMPTY_STRING);
				}

			}

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return partList;
	}
	/**
	 * This method is return the value for Usage   column 
	 * @param context
	 *            The ematrix context object.
	 * @param String[]
	 *            The args .
	 * @return StringList.	 
	 * @throws Exception
	 */
	public static StringList getUsage(Context context, String[] args)
			throws FrameworkException {
		try {
			HashMap inputMap = (HashMap) JPO.unpackArgs(args);
			MapList objectMap = (MapList) inputMap.get("objectList");			
			String strUsage = "";		
			
			String strLanguage = context.getSession().getLanguage();
			String strUsage_Standard = (EnoviaResourceBundle.getProperty(context, "Framework","emxFramework.Range.Usage.Standard",strLanguage)).trim();
			String strUsage_Reference = (EnoviaResourceBundle.getProperty(context, "Framework","emxFramework.Range.Usage.Reference", strLanguage)).trim();
			String strUsage_PerSalesOrder = (EnoviaResourceBundle.getProperty(context, "Framework","emxFramework.Range.Usage.Per_Sales_Order", strLanguage)).trim();
			String strUsage_AsRequired = (EnoviaResourceBundle.getProperty(context, "Framework","emxFramework.Range.Usage.As_Required",strLanguage)).trim();
			String strUsage_ReferenceEng = (EnoviaResourceBundle.getProperty(context, "Framework","emxFramework.Range.Usage.Reference-Eng", strLanguage)).trim();
			String strUsage_ReferenceMfg = (EnoviaResourceBundle.getProperty(context, "Framework","emxFramework.Range.Usage.Reference-Mfg", strLanguage)).trim();
			StringList returnStringList = new StringList(objectMap.size());
			for (int i = 0; i < objectMap.size(); i++) {
				Map outerMap = new HashMap();
				outerMap = (Map) objectMap.get(i);
				//strUsage = (String) outerMap
				//		.get(ConfigurationConstants.SELECT_ATTRIBUTE_USAGE);
				if (outerMap
						.containsKey(ConfigurationConstants.SELECT_ATTRIBUTE_USAGE)
						&& ProductLineCommon
								.isNotNull((String) outerMap
										.get(ConfigurationConstants.SELECT_ATTRIBUTE_USAGE))){
					strUsage = (String) outerMap
							.get(ConfigurationConstants.SELECT_ATTRIBUTE_USAGE);

				}else if (outerMap
						.containsKey(DomainRelationship.SELECT_ID)
						&& ProductLineCommon
								.isNotNull((String) outerMap
										.get(DomainRelationship.SELECT_ID))){
					DomainRelationship domRel = new DomainRelationship((String) outerMap
							.get(DomainRelationship.SELECT_ID));
					strUsage=domRel.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_USAGE);
				}
				String strI18Value = "";
				if (strUsage != null) {
					if (strUsage
							.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_STANDARD)) {
						strI18Value = strUsage_Standard;

					} else if (strUsage
							.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_REFERENCE)) {
						strI18Value = strUsage_Reference;
					}else if (strUsage
							.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_PER_SALES_ORDER)) {
						strI18Value = strUsage_PerSalesOrder;
					}
					else if (strUsage
							.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_AS_REQUIRED)) {
						strI18Value = strUsage_AsRequired;
					}
					else if (strUsage
							.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_REFERENCE_ENG)) {
						strI18Value = strUsage_ReferenceEng;
					}
					else if (strUsage
							.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_REFERENCE_MFG)) {
						strI18Value = strUsage_ReferenceMfg;
					}
				}
				if (!("".equalsIgnoreCase(strI18Value)
						|| "null".equalsIgnoreCase(strI18Value) || strI18Value == null)) {
					returnStringList.addElement(strI18Value);
				} else {
					returnStringList.addElement("");
				}
			}
			return returnStringList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}
	
	 /** 
	  * Method to get the  name column 
	  * @param context
	  * @param args
	  * @return
	  * @throws FrameworkException
	  */
	 public static StringList getName(Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strName =ConfigurationConstants.EMPTY_STRING;
				String strObjId=ConfigurationConstants.EMPTY_STRING;
				String strRev=ConfigurationConstants.EMPTY_STRING;
				DomainObject domObj=null;
			      // selectable for root node  
			   	 StringList objectSelects = new StringList();	    
		 	      objectSelects.add(DomainConstants.SELECT_NAME);
				objectSelects.add(ConfigurationConstants.SELECT_REVISION);
			    StringList returnStringList = new StringList (objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);							
					strName =  (String)outerMap.get(ConfigurationConstants.SELECT_NAME);
					strRev =  (String)outerMap.get(ConfigurationConstants.SELECT_REVISION);
					String isRootNode = (String) outerMap.get("Root Node");
				    if ("true".equalsIgnoreCase(isRootNode) || isRootNode==null && strName==null ){
						strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
			            domObj = DomainObject.newInstance(context, strObjId);				      
			            Map rootMap= domObj.getInfo(context,objectSelects);
			            strName=(String)rootMap.get(DomainConstants.SELECT_NAME);
			            strRev=(String)rootMap.get(DomainConstants.SELECT_REVISION);
			            returnStringList.add(strName + " " + strRev );
						
					}
					else{
					returnStringList.add(strName + " " + strRev);
					}
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
	 /** 
	  * Method to get the revision column 
	  * @param context
	  * @param args
	  * @return
	  * @throws FrameworkException
	  */
		public static StringList getRevisionForProductContext(Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strRevision ="" ;
				String strObjId="";
				DomainObject domObj=null;
			      // selectable for root node  
			   	 StringList objectSelects = new StringList();	    
		 	      objectSelects.add(DomainConstants.SELECT_REVISION);
			    StringList returnStringList = new StringList (objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);							
					strRevision =  (String)outerMap.get(ConfigurationConstants.SELECT_REVISION);
					String isRootNode = (String) outerMap.get("Root Node");
					if(isRootNode!=null && isRootNode.equalsIgnoreCase("true") ){
		            	 strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
				            domObj = DomainObject.newInstance(context, strObjId);				      
				            Map rootMap= domObj.getInfo(context,objectSelects);
				            strRevision=(String)rootMap.get(DomainConstants.SELECT_REVISION);
				            returnStringList.add(strRevision);
					}
					else if(outerMap.containsKey("parentLevel")){
						strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
			            domObj = DomainObject.newInstance(context, strObjId);				      
			            Map rootMap= domObj.getInfo(context,objectSelects);
			            strRevision=(String)rootMap.get(DomainConstants.SELECT_REVISION);
			            returnStringList.add(strRevision);
					}else{
					returnStringList.add(strRevision);
					}
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
		/** 
		 * Method to get the type column 
		 * @param context
		 * @param args
		 * @return
		 * @throws FrameworkException
		 */
		public static StringList getType(Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strType ="" ;
				String strObjId="";
				DomainObject domObj=null;
			      // selectable for root node  
			   	 StringList objectSelects = new StringList();	    
		 	      objectSelects.add(DomainConstants.SELECT_TYPE);
			    StringList returnStringList = new StringList (objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);							
					strType =  (String)outerMap.get(ConfigurationConstants.SELECT_TYPE);
					String isRootNode = (String) outerMap.get("Root Node");
					if(isRootNode!=null && isRootNode.equalsIgnoreCase("true") ){
		            	 strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
				            domObj = DomainObject.newInstance(context, strObjId);				      
				            Map rootMap= domObj.getInfo(context,objectSelects);
				            strType=(String)rootMap.get(DomainConstants.SELECT_TYPE);
				            returnStringList.add(strType);
					}else if(outerMap.containsKey("parentLevel")){
						strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
			            domObj = DomainObject.newInstance(context, strObjId);				      
			            Map rootMap= domObj.getInfo(context,objectSelects);
			            strType=(String)rootMap.get(DomainConstants.SELECT_TYPE);
			            returnStringList.add(strType);
					}else{
					returnStringList.add(strType);
					}
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
		/** 
		 * Method to get the state column 
		 * @param context
		 * @param args
		 * @return
		 * @throws FrameworkException
		 */		
		public static StringList getState(Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strLanguage = context.getSession().getLanguage();
				String strState ="" ;	
				String strPolicy ="" ;	
				String strObjId="";
				DomainObject domObj=null;
			      // selectable for root node  
			   	 StringList objectSelects = new StringList();	    
		 	     objectSelects.add(DomainConstants.SELECT_CURRENT);
		 	     objectSelects.add(DomainConstants.SELECT_POLICY);
			    StringList returnStringList = new StringList (objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);							
					strState =  (String)outerMap.get(ConfigurationConstants.SELECT_CURRENT);
					strPolicy =  (String)outerMap.get(ConfigurationConstants.SELECT_POLICY);
					String isRootNode = (String) outerMap.get("Root Node");
					if(isRootNode!=null && isRootNode.equalsIgnoreCase("true") ){
		            	 strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
				            domObj = DomainObject.newInstance(context, strObjId);				      
				            Map rootMap= domObj.getInfo(context,objectSelects);
				            strState=(String)rootMap.get(DomainConstants.SELECT_CURRENT);
				            strPolicy=(String)rootMap.get(DomainConstants.SELECT_POLICY);
				            strState= EnoviaResourceBundle.getStateI18NString(context, strPolicy, strState, strLanguage);
				            returnStringList.add(strState);
					}else if(outerMap.containsKey("parentLevel")){
						strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
			            domObj = DomainObject.newInstance(context, strObjId);				      
			            Map rootMap= domObj.getInfo(context,objectSelects);
			            strState=(String)rootMap.get(DomainConstants.SELECT_CURRENT);
			            strPolicy=(String)rootMap.get(DomainConstants.SELECT_POLICY);
			            strState= EnoviaResourceBundle.getStateI18NString(context, strPolicy, strState, strLanguage);
			            returnStringList.add(strState);
					}else{
						strState= EnoviaResourceBundle.getStateI18NString(context, strPolicy, strState, strLanguage);
						returnStringList.add(strState);
					}
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
		/** 
		 * Method to get the Owner column 
		 * @param context
		 * @param args
		 * @return
		 * @throws FrameworkException
		 */
		public static StringList getOwner(Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strOwner ="" ;
				String strObjId="";
				DomainObject domObj=null;
			      // selectable for root node  
			   	 StringList objectSelects = new StringList();	    
		 	      objectSelects.add(DomainConstants.SELECT_OWNER);
			    StringList returnStringList = new StringList (objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);							
					strOwner =  (String)outerMap.get(ConfigurationConstants.SELECT_OWNER);
					String isRootNode = (String) outerMap.get("Root Node");
					if(isRootNode!=null && isRootNode.equalsIgnoreCase("true") ){
		            	 strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
				            domObj = DomainObject.newInstance(context, strObjId);				      
				            Map rootMap= domObj.getInfo(context,objectSelects);
				            strOwner=(String)rootMap.get(DomainConstants.SELECT_OWNER);
				        	returnStringList.add(strOwner);
					}	else if(outerMap.containsKey("parentLevel")){
						strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
			            domObj = DomainObject.newInstance(context, strObjId);				      
			            Map rootMap= domObj.getInfo(context,objectSelects);
			            strOwner=(String)rootMap.get(DomainConstants.SELECT_OWNER);
			            returnStringList.add(strOwner);
					}else{
					returnStringList.add(strOwner);
					}
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
		public List getStatusIconForProductContext (Context context, String[] args) throws FrameworkException{
		 	List lstNameRev = new StringList();	    
		     try{
		     	Map programMap = (HashMap) JPO.unpackArgs(args);
			        List lstobjectList = (MapList) programMap.get("objectList");
			        Iterator objectListItr = lstobjectList.iterator();
			        Map paramList = (HashMap)programMap.get("paramList");
			        String reportFormat = (String)paramList.get("reportFormat");
			    	String strLanguage = context.getSession().getLanguage();		    	
			        //initialise the local variables
			        Map objectMap = new HashMap();
			        String strObjId = DomainConstants.EMPTY_STRING;
			        String strObjState = DomainConstants.EMPTY_STRING;
			        String strIcon = DomainConstants.EMPTY_STRING;
			        String strObjPolicy = DomainConstants.EMPTY_STRING;
			        String strObjPolicySymb = DomainConstants.EMPTY_STRING;
			        String strObjStateSymb = DomainConstants.EMPTY_STRING;
			        StringBuffer sbStatePolicyKey = new StringBuffer();
			        boolean flag = false;
			        StringBuffer stbNameRev = new StringBuffer(100);
			        DomainObject domObj = null;		        
			        Map cacheMap=new HashMap();	
			      // selectable for root node  
			   	 StringList objectSelects = new StringList();	    
		 	 objectSelects.add(DomainConstants.SELECT_CURRENT);
		 	 objectSelects.add(DomainConstants.SELECT_POLICY);    	 
		 	//I18 conversion 
			       String strStateCFPreliminary=EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxFramework.State.Features.Preliminary", strLanguage);
			       String strStateCFRelease=EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxFramework.State.Features.Release", strLanguage);
			       String strStateCFReview=EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxFramework.State.Features.Review", strLanguage);
			       String strStateCFObsolete=EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxFramework.State.Features.Obsolete", strLanguage);			      
			       String strStatePreliminary=EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxFramework.State.Preliminary",strLanguage);
			       String strStateProductManagement=EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxFramework.State.ProductManagement",strLanguage);
			       String strStateRelease=EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxFramework.State.Release", strLanguage);
			       String strStateDesignEngineering=EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxFramework.State.DesignEngineering", strLanguage);
			       String strStateObsolete=EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxFramework.State.Obsolete", strLanguage);
			       String strStateReview=EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxFramework.State.Review", strLanguage);
			       
			        //loop through all the records
			        while(objectListItr.hasNext())
			        {
			            objectMap = (Map) objectListItr.next();
			            String isRootNode= (String)objectMap.get("Root Node");
			         if(ProductLineCommon.isNotNull(isRootNode)){
			            if(isRootNode.equalsIgnoreCase("true")){
			            	 strObjId = (String)objectMap.get(DomainConstants.SELECT_ID);
					            domObj = DomainObject.newInstance(context, strObjId);				      
					            Map rootMap= domObj.getInfo(context,objectSelects);
					            strObjState=(String)rootMap.get(DomainConstants.SELECT_CURRENT);
					            strObjPolicy=(String)rootMap.get(DomainConstants.SELECT_POLICY);
					            
			            }
			         }	else if(objectMap.containsKey("parentLevel")){
			        	 strObjId = (String)objectMap.get(DomainConstants.SELECT_ID);
				            domObj = DomainObject.newInstance(context, strObjId);				      
				            Map rootMap= domObj.getInfo(context,objectSelects);
				            strObjState=(String)rootMap.get(DomainConstants.SELECT_CURRENT);
				            strObjPolicy=(String)rootMap.get(DomainConstants.SELECT_POLICY);
						}else{
			            strObjState = (String)objectMap.get(DomainConstants.SELECT_CURRENT);
			            strObjPolicy = (String)objectMap.get(DomainConstants.SELECT_POLICY);
			            }
			         
			            if(cacheMap.containsKey(strObjPolicy)){
			            	strObjPolicySymb=(String)cacheMap.get(strObjPolicy);         
			            }
			            else{
			            	 strObjPolicySymb = FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_POLICY,strObjPolicy,true);
			            	 cacheMap.put(strObjPolicy, strObjPolicySymb);
			            }
			            if(cacheMap.containsKey(strObjState)){
			            	strObjStateSymb=(String)cacheMap.get(strObjState);         
			            }
			            else{
			            	strObjStateSymb = FrameworkUtil.reverseLookupStateName(context, strObjPolicy, strObjState);
			            	cacheMap.put(strObjState, strObjStateSymb);
			            }
			            // Forming the key which is to be looked up
			            sbStatePolicyKey = new StringBuffer("emxProduct.LCStatusImage.");
			            sbStatePolicyKey.append(strObjPolicySymb)
			                            .append(".")
			                            .append(strObjStateSymb);

			            // Getting the value for the corresponding key, if not catching it to set flag = false
			            try{
			            	  strIcon = EnoviaResourceBundle.getProperty(context,sbStatePolicyKey.toString());
			                flag = true;
			            }
			            catch(Exception ex)
			            {
			                flag = false;
			            }

			            if(flag)
			            {
			                if(ProductLineCommon.isNotNull(isRootNode)){
			                	strObjPolicy = FrameworkUtil.findAndReplace(strObjPolicy," ", "");
			                StringBuffer sbStateKey = new StringBuffer("emxFramework.State.");
			                sbStateKey.append(strObjPolicy);
			                sbStateKey.append(".");
			                sbStateKey.append(strObjState);
			                strObjState = EnoviaResourceBundle.getProperty(context,"Framework",sbStateKey.toString(), strLanguage);
			                }else{
			            	if(strObjPolicy.equalsIgnoreCase(ConfigurationConstants.POLICY_LOGICAL_FEATURE)){
			            		if(strObjState.equalsIgnoreCase(ConfigurationConstants.STATE_PRELIMINARY)){
			            			strObjState=strStateCFPreliminary;	
			            		}else if(strObjState.equalsIgnoreCase(ConfigurationConstants.STATE_REVIEW)){
			            			strObjState=strStateCFReview;
			            		}else if(strObjState.equalsIgnoreCase(ConfigurationConstants.STATE_RELEASE)){
			            			strObjState=strStateCFRelease;
			            		}else{
			            			strObjState=strStateCFObsolete;
			            		}	
			            	}else if(strObjPolicy.equalsIgnoreCase(ConfigurationConstants.POLICY_PRODUCT)){
			            		if(strObjState.equalsIgnoreCase(ConfigurationConstants.STATE_PRELIMINARY)){
			            			strObjState=strStatePreliminary;	
			            		}else if(strObjState.equalsIgnoreCase(ConfigurationConstants.STATE_REVIEW)){
			            			strObjState=strStateReview;
			            		}else if(strObjState.equalsIgnoreCase(ConfigurationConstants.STATE_RELEASE)){
			            			strObjState=strStateRelease;
			            		}
			            		else if(strObjState.equalsIgnoreCase(ConfigurationConstants.STATE_DESIGNENGINEERING)){
			            			strObjState=strStateDesignEngineering;
			            		}
			            		else if(strObjState.equalsIgnoreCase(ConfigurationConstants.STATE_PRODUCTMANAGEMENT)){
			            			strObjState=strStateProductManagement;
			            		}else{
			            			strObjState=strStateObsolete;
			            		}	
			            	}
			              }
			                stbNameRev.delete(0, stbNameRev.length());
			                if (reportFormat != null && !("null".equalsIgnoreCase(reportFormat)) && reportFormat.length()>0){
			            		lstNameRev.add(strObjState);
			            	}else{
				              stbNameRev=stbNameRev.append("<img src=\"../common/images/")
		                     .append(XSSUtil.encodeForXML(context,strIcon))
		                     .append("\" border=\"0\"  align=\"middle\" TITLE=\" ")                                           
		                     .append(XSSUtil.encodeForXML(context,strObjState))
		                     .append("\"/>");                            
				                lstNameRev.add(stbNameRev.toString());
			            	}
			            }
			            else
			            {
			                lstNameRev.add(DomainConstants.EMPTY_STRING);
			            }
			            
			        
			        }
			        }
		     catch(Exception e){
					throw new FrameworkException(e);
				}
			
		     return lstNameRev;
		 }
	/**
 	 * Trigger will be invoked on Logical Features Delete Action. It will remove the Committed Context relationship
 	 * and Change the Committed Logical Features relation to Candidate Logical Features relation.
 	 * 
 	 * @param context
 	 * @param args
 	 * @return
 	 * @throws FrameworkException
 	 */
    public void removeCommittedContextForLogicalFeatures(Context context, String args[]) throws FrameworkException {
		 String objectID=args[1];
		 String configurationFeatureID=args[2];
		 MapList mapModelStructure = null;
		 String modelID = null;
		 List committedContextRelList = new StringList();
		 StringList relList = new StringList();
		 StringList relIDList = new StringList();
		 
		 try{		 
		 	StringList objectSelects = new StringList();
			objectSelects.add(DomainObject.SELECT_ID);
			objectSelects.add(DomainObject.SELECT_TYPE);
			objectSelects.add(DomainObject.SELECT_NAME);			
			
			StringList relationshipSelects = new StringList();
			relationshipSelects.add(DomainRelationship.SELECT_ID);
			relationshipSelects.add(DomainRelationship.SELECT_TYPE);
			relationshipSelects.add("frommid["+ ConfigurationConstants.RELATIONSHIP_COMMITTED_CONTEXT+ "].id");			
			
			StringBuffer strModelRelPattern = new StringBuffer(ConfigurationConstants.RELATIONSHIP_COMMITTED_LOGICAL_FEATURES);
			StringBuffer strwherePattern = new StringBuffer("id=="+configurationFeatureID);		  	
			
			DomainObject domContextObj = new DomainObject(objectID);
			
			if(domContextObj.isKindOf(context, ConfigurationConstants.TYPE_PRODUCTS)){
				if( ! ProductLineUtil.isCommittedContextOnProduct(context, domContextObj, ConfigurationConstants.RELATIONSHIP_COMMITTED_CONTEXT) )
					return;
				
			 final String SELECT_PARENT_PRODUCT = ("to["+ ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].from.id");
			 final String SELECT_PARENT_MAIN_PRODUCT = ("to["+ ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT+"].from.id");			 
			 final String SELECT_COMMITTED_CONTEXT_REL = ("to["+ ConfigurationConstants.RELATIONSHIP_COMMITTED_CONTEXT+"].id");

			 StringList selectables = new StringList();
			 selectables.add(SELECT_PARENT_PRODUCT);
			 selectables.add(SELECT_PARENT_MAIN_PRODUCT);
			 selectables.add(SELECT_COMMITTED_CONTEXT_REL);
			 selectables.add(DomainObject.SELECT_TYPE);
			 
			 DomainConstants.MULTI_VALUE_LIST.add(SELECT_COMMITTED_CONTEXT_REL);
			 Map mapContext = domContextObj.getInfo(context, selectables);
			 DomainConstants.MULTI_VALUE_LIST.remove(SELECT_COMMITTED_CONTEXT_REL);			 
			
			 if(mapContext.containsKey(SELECT_PARENT_PRODUCT))
		
					modelID = (String)mapContext.get(SELECT_PARENT_PRODUCT);
		
			 else if(mapContext.containsKey(SELECT_PARENT_MAIN_PRODUCT))
		
					modelID = (String)mapContext.get(SELECT_PARENT_MAIN_PRODUCT);
			 
			 if(modelID == null)
				 return;
		
			 if(mapContext.containsKey(SELECT_COMMITTED_CONTEXT_REL))
			 {
						relList=  (StringList) mapContext.get(SELECT_COMMITTED_CONTEXT_REL);
			 }
					
			 DomainObject modelContextObj = new DomainObject(modelID);
			 mapModelStructure = modelContextObj.getRelatedObjects(context, strModelRelPattern.toString(), DomainObject.QUERY_WILDCARD,
							  	objectSelects, relationshipSelects,
							  	false, true, (short) 0, 
							  	strwherePattern.toString(), DomainConstants.EMPTY_STRING, 0);
			 
				for(int i=0; i<mapModelStructure.size(); i++){
					
					String relationshipId = (String)((Map)mapModelStructure.get(i)).get(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
						if(relationshipId!=null && !relationshipId.equals("")){
							
							  Object obj = (Object)((Map)mapModelStructure.get(i)).get("frommid["+ ConfigurationConstants.RELATIONSHIP_COMMITTED_CONTEXT+ "].id");
							  if(obj instanceof String){
								  committedContextRelList.add((String)obj);
							  }else if(obj instanceof StringList){
								  committedContextRelList.addAll((StringList) obj);
							  }						 
							  int commitConfFeatureCount = committedContextRelList.size();							 
							  for(int j=0 ; j<commitConfFeatureCount ; j++){
								  if(relList.contains(committedContextRelList.get(j))){
									  relIDList.add(committedContextRelList.get(j));							  
								  }
							  }
							  if(commitConfFeatureCount==1){		
									DomainRelationship.setType(context, relationshipId, ConfigurationConstants.RELATIONSHIP_CANDIDTAE_LOGICAL_FEATURES);
							  }
							  String[] relIDs = new String[relIDList.size()]; 
							  for(int k=0; k<relIDList.size() ;k++){
									relIDs[k]=(String) relIDList.get(k);
							  }
							  DomainRelationship.disconnect(context, relIDs);
						}
					}
				}
		 	}catch(Exception ex){
				throw new FrameworkException(ex);
		 	}
	 	}


		public static StringList isEffGridEditableForContext(Context context, String[] args) throws FrameworkException{
		try{
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			Map requestMap = (Map) inputMap.get("requestMap");
			String objectId = (String) requestMap.get("objectId");	 
			String relType = (String) requestMap.get("effectivityRelationship");
			String strRel = PropertyUtil.getSchemaProperty(context, relType);
			boolean isLFStructureEff = ProductLineCommon.isKindOfRel(context, strRel,
					 ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES);
			
			boolean isEditable = false;
			
			if(isLFStructureEff){
				BusinessObject boIssue = new BusinessObject(objectId);
	            Access accIssueState = boIssue.getAccessMask(context);
	            boolean isChangeTypeAccess = accIssueState.hasChangeTypeAccess();
	            boolean isModifyAccess = accIssueState.hasModifyAccess();
	            boolean isFTRUser = ConfigurationUtil.isFTRUser(context);
	            boolean isCMMUser = ConfigurationUtil.isCMMUser(context);
				if(isChangeTypeAccess && isModifyAccess && (isFTRUser||isCMMUser)){
					isEditable = true;
				}
			}
			else{
				isEditable = true;
			}
			
			MapList objectMap = (MapList) inputMap.get("objectList");	      
			StringList returnStringList = new StringList(objectMap.size());

			for(int i = 0; i < objectMap.size(); i++){
				returnStringList.add(new Boolean(isEditable));
			}

			return returnStringList;

		}catch(Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}
	

		/**
	 	 * This method will be called to save Effectivity coming from modification of Grid Table
	 	 * 
	 	 * @param context
	 	 * @param args
	 	 * @return Map for success or error msg for BPS to commit or abort UI
	 	 * @throws Exception
	 	 */
		@com.matrixone.apps.framework.ui.PostProcessCallable
		public Map EffectivityGridApplyProcess(Context context, String[] args) throws Exception {
			
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);			
			Map returnMap = new HashMap();
			try{
				
				Document tableDoc = (Document) inputMap.get("XMLDoc");
				Map requestMap = (Map) inputMap.get("requestMap");
				Map paramMap = (Map)inputMap.get("paramMap");
				String logicalFeatureID = "";
		        
				if (requestMap != null) {
					logicalFeatureID = (String) paramMap.get("objectId");
				}

				if (tableDoc != null) {
					Element rootElement = tableDoc.getRootElement();
					
					MapList changeMapList = UITableIndented.getChangedRowsMapFromElement(context, rootElement);
					System.out.println("changeMapList---"+changeMapList);
					for(int m=0;m<changeMapList.size();m++){
						Map mapChangeMap = (Map)changeMapList.get(m);
						Map changedColumnMap = (HashMap)mapChangeMap.get("columns");
						Iterator iterator = changedColumnMap.keySet().iterator();
						String key;
						while (iterator.hasNext()) {
							key = (String) iterator.next();
							boolean isObject=new DomainObject(key).exists(context);
							if(isObject){
								String strValue = (String)changedColumnMap.get(key);
								if(ProductLineCommon.isNotNull(strValue)){
								changedColumnMap.put(key,(String)new DomainObject(strValue).getInfo(context, DomainObject.SELECT_ID));
								}
							}
						}
					}
					System.out.println("changeMapList updated---"+changeMapList);
					
					LogicalFeature lfBean = new LogicalFeature(logicalFeatureID);
					lfBean.updateEffectivityExpressionValues(context, changeMapList);
				
				returnMap.put ("Action", "success");
				returnMap.put("Message", "");	
				}
			}
			catch(Exception e){
				returnMap.put ("Action", "error");
				returnMap.put("Message", e.toString());				
			}
			return returnMap;			
		}
		

	     public void copyEffectivityOnRevise(Context context,
	    		 String args[]) throws Exception{

	    	 HashMap relIDMapOnDestination = new HashMap();
	    	 HashMap relIDMapOnSource = new HashMap();

	    	 //ObjectId of the context product
	    	 String strSourceObjectId = args[0];
	    	 String strDestObjectName = args[1];
	    	 String strDestObjectRev = args[2];
	    	 String strDestObjectVault = args[3];

	    	 StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
	    	 objectSelects.addElement(DomainConstants.SELECT_NAME);

	    	 StringList relationshipSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

	    	 if (!UIUtil.isNotNullAndNotEmpty(strSourceObjectId) || !UIUtil.isNotNullAndNotEmpty(strDestObjectName) ||
	    			 !UIUtil.isNotNullAndNotEmpty(strDestObjectRev) || !UIUtil.isNotNullAndNotEmpty(strDestObjectVault)) {
	    		 return;
	    	 }
	    	 DomainObject dom = new DomainObject(strSourceObjectId);
	    	 String strSorceObjType = dom.getInfo(context, DomainConstants.SELECT_TYPE);
	    	 // Getting the new object Id	        
	    	 BusinessObject busObj = new BusinessObject(strSorceObjType, strDestObjectName, strDestObjectRev, strDestObjectVault);
	    	 String strDestinationObjectId = busObj.getObjectId(context);



	    	 StringBuffer sb = new StringBuffer(250);
	    	 String strComma = ",";
	    	 String relationshipPattern = sb.append(ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES).append(strComma)
	    	 .append(ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES).toString();

	    	 sb = new StringBuffer(250);
	    	 String typePattern = sb.append(ConfigurationConstants.TYPE_LOGICAL_STRUCTURES).append(strComma)
	    	 .append(ConfigurationConstants.TYPE_PRODUCTS).append(strComma)
	    	 .append(ConfigurationConstants.TYPE_CONFIGURATION_FEATURES).toString();

	    	 MapList sourceObjFeatureList = dom.getRelatedObjects(context, relationshipPattern, typePattern,
	    			 false, true, (short) 1,objectSelects, relationshipSelects,
	    			 DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, (short) 0,
	    			 null, null, null);

	    	 DomainObject domDestination = new DomainObject(strDestinationObjectId);
	    	 MapList destinationeObjFeatureList = domDestination.getRelatedObjects(context, relationshipPattern, typePattern,
	    			 false, true, (short) 1,objectSelects, relationshipSelects,
	    			 DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, (short) 0,
	    			 null, null, null);

	    	 for(int i=0;i<destinationeObjFeatureList.size();i++)
	    	 {
	    		 Map destinationeObjFeatureMap = (Map)destinationeObjFeatureList.get(i);
	    		 Map sourceObjFeatureMap = (Map)sourceObjFeatureList.get(i);
	    		 String newRelID = (String)destinationeObjFeatureMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
	    		 String objIdOnDestination = (String)destinationeObjFeatureMap.get(SELECT_ID);
	    		 relIDMapOnDestination.put(objIdOnDestination, newRelID);
	    		 String oldRelID = (String)sourceObjFeatureMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
	    		 String objIdOnSource = (String)sourceObjFeatureMap.get(SELECT_ID);
	    		 relIDMapOnSource.put(objIdOnSource, oldRelID);
	    	 }

	    	 if(relIDMapOnSource.size() == relIDMapOnDestination.size()){
	    		 Iterator itr = relIDMapOnSource.keySet().iterator();
	    		 while(itr.hasNext()){
	    			 String objId = (String)itr.next();
	    			 String oldRelId = (String)relIDMapOnSource.get(objId);
	    			 String newRelId = (String)relIDMapOnDestination.get(objId);
	    			 EffectivityFramework effectivityFramework=new EffectivityFramework();
	    			 try{   			
	    				 MapList mLstActualExpression=effectivityFramework.getRelExpression(context,  oldRelId);
	    				 Map mActualExpression=(Map)mLstActualExpression.get(0);
	    				 if(mActualExpression.containsKey(EffectivityFramework.ACTUAL_VALUE)){
	    					 effectivityFramework.setRelExpression(context,
	    							 newRelId,mActualExpression.get(EffectivityFramework.ACTUAL_VALUE).toString());
	    				 }
	    			 }catch(Exception exception)	{
	    				 throw new FrameworkException(exception.getMessage());
	    			 }
	    		 }        		 
	    	 }

	     }//end of function

	     /**
	      * Transaction Trigger on Products Type- Only a Case in which Products history has "revisioned -" in 1st occurence.
	      * 
	      * In case of Creating Revision and Derivation for Product this Trigger will not be fired as in this case Product get cloned and not revised.
	      * For Clone PFL are replicated and so this implementation is not required.
	      * 
	      * @param context
	      * @param args
	      * @throws Exception
	      */
	     public void updatePFLOnProductRevisioned(Context context, String[] args)throws Exception {
	    	 try {
	    		 String transHistories = args[0];
	    		 //Traversing History--
	    		 StringBuffer subTransHistory = new StringBuffer();
	    		 String strPrevRevPRDId="";
	    		 subTransHistory.append(transHistories);
	    		 int idIndex = transHistories.indexOf("id=");
	    		 int idLastIndex = transHistories.lastIndexOf("id=");
	    		 if (subTransHistory.length()!=0 && idIndex!=-1 &&idLastIndex!=-1 && idLastIndex!=idIndex){
	    			 subTransHistory.delete(subTransHistory.lastIndexOf("id="),subTransHistory.length());
	    		 }
	    		 int itypeIndex = subTransHistory.indexOf("type=");
	    		 if(itypeIndex != -1){
	    			 String strType = (subTransHistory.substring(subTransHistory.lastIndexOf("type=")+5,subTransHistory.lastIndexOf("triggerName=")).trim());
	    			 String strHistory = (subTransHistory.substring(subTransHistory.lastIndexOf("history=")+8).trim());
	    			 if("businessobject".equals(strType) && !"null".equals(strType) && strHistory.contains("revisioned -")){
	    				 strPrevRevPRDId=(subTransHistory.substring(subTransHistory.lastIndexOf("id=")+3,subTransHistory.lastIndexOf("type=")).trim());
	    			 }
	    		 }
	    		 if(ProductLineCommon.isNotNull(strPrevRevPRDId)){
	    			 ContextUtil.pushContext(context, PropertyUtil
	    					 .getSchemaProperty(context, "person_UserAgent"),
	    					 DomainConstants.EMPTY_STRING,
	    					 DomainConstants.EMPTY_STRING);	    			 
	    			 // getting the new Revision Object ID
	    			 DomainObject domObjOldRev = new DomainObject(strPrevRevPRDId);
	    			 String strNextRevId = domObjOldRev.getNextRevision(context).getObjectId(context);
	    			 //--------------------------------------------------------------------
	    			 // Get the previous revision Products PFL connections with FAT attribute details for cloning the attribute to new Revision
	    			 //--------------------------------------------------------------------	    			 
	    			 StringList strSelect = ConfigurationUtil.getBasicRelSelects(context);
	    			 strSelect.addElement("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
	    			 strSelect.addElement("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.logicalid");
	    			 strSelect.addElement("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.id");
	    			 DomainConstants.MULTI_VALUE_LIST.add("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
	    			 DomainConstants.MULTI_VALUE_LIST.add("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.logicalid");
	    			 DomainConstants.MULTI_VALUE_LIST.add("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.id");
	    			 //Get Info on previous rev Product to get PFL Information
	    			 Map mPFLDetails = domObjOldRev.getInfo(context, strSelect);

	    			 StringList featureListLFRELLOGICALID =  (StringList) mPFLDetails.get("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.logicalid");
	    			 StringList featureListLFRELID =  (StringList) mPFLDetails.get("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.id");
	    			 StringList attributeFAT = (StringList) mPFLDetails.get("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
	    			 DomainConstants.MULTI_VALUE_LIST.remove("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
	    			 DomainConstants.MULTI_VALUE_LIST.remove("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.logicalid");
	    			 DomainConstants.MULTI_VALUE_LIST.remove("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.id");

	    			 Map mpOLDLogicalIDTOObjectID=new HashMap();
	    			 for(int cnt1=0;cnt1<featureListLFRELLOGICALID.size();cnt1++){
	    				 String strLFRelFAT = (String) attributeFAT.get(cnt1);
	    				 String strLFRelLogicalId = (String) featureListLFRELLOGICALID.get(cnt1);
	    				 mpOLDLogicalIDTOObjectID.put(strLFRelLogicalId, strLFRelFAT);
	    			 }
	    			 //--------------------------------------------------------------------
	    			 // Get the new Revision Products Child LF Structure
	    			 //--------------------------------------------------------------------	    			 
	    			 ConfigurationUtil confUtil = new ConfigurationUtil();
	    			 StringList objSelectables = new StringList();
	    			 objSelectables.addElement("physicalid");
	    			 StringList sLstRelSelect = new StringList();
	    			 sLstRelSelect.addElement("tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id");
	    			 sLstRelSelect.addElement("logicalid");
	    			 StringBuffer sbRelWher = new StringBuffer(16);
	    			 sbRelWher.append("from.type.kindof !=\"");
	    			 sbRelWher.append(ConfigurationConstants.TYPE_PRODUCTS);
	    			 sbRelWher.append("\"");
	    			 sbRelWher.append("||");
	    			 sbRelWher.append("from.id==");
	    			 sbRelWher.append(strNextRevId);
	    			 LogicalFeature logicalfeature = new LogicalFeature(strNextRevId);
	    			 MapList mapLogicalStructure = logicalfeature.getLogicalFeatureStructure(context,null, null, objSelectables, sLstRelSelect, false,
	    					 true,0,0,DomainObject.EMPTY_STRING,"", DomainObject.FILTER_STR_AND_ITEM,DomainObject.EMPTY_STRING);

	    			 RelationshipType relationtype = new RelationshipType(ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST);

	    			 //--------------------------------------------------------------------
	    			 // Iterate on new Revision Products Child LF Structure
	    			 //-------------------------------------------------------------------
	    			 Map mpLogicalIDTOObjectID=new HashMap();
	    			 
	    			 StringList idConnectionStruct = new StringList();
	    			 for (int i = 0; i < mapLogicalStructure.size(); i++) {
	    				 Map tempMap = (Map) mapLogicalStructure.get(i);
	    				 String strLFRelId = (String) tempMap.get(DomainRelationship.SELECT_ID);
	    				 String strLFRelLogicalId = (String) tempMap.get("logicalid");
	    				 mpLogicalIDTOObjectID.put(strLFRelLogicalId, strLFRelId);
	    				 String strLFId = (String) tempMap.get(DomainObject.SELECT_ID);
	    				 StringList strPFLConnections = confUtil.convertObjToStringList(context,
	    						 tempMap.get("tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id"));
	    				 String idConnection = (String) tempMap.get(DomainObject.SELECT_RELATIONSHIP_ID);
	    				 if(!strPFLConnections.contains(strNextRevId)&& (idConnectionStruct.isEmpty() || !idConnectionStruct.contains(idConnection))){
	    					 ProductLineCommon connectPFLallproducts = new ProductLineCommon(strLFRelId);
	    					 String strConnection = connectPFLallproducts.connectObject(context,relationtype,strNextRevId, true);
	    					 //traverse the previous product revisions PFL rel and get the attribute
	    	    			 DomainObject domObjLF = new DomainObject(strLFId);
	    	    			 String strPrevRevId = domObjLF.getPreviousRevision(context).getObjectId(context);
	    	    			 if(ProductLineCommon.isNotNull(strPrevRevId) && (mpOLDLogicalIDTOObjectID.containsKey(strLFRelLogicalId))){
	    	    				 String strFATAttibute=(String)mpOLDLogicalIDTOObjectID.get(strLFRelLogicalId);
	    	    				 DomainRelationship domRel = new DomainRelationship(strConnection);
	    	    				 domRel.setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE,strFATAttibute);
	    	    			 }
	    					 idConnectionStruct.add(idConnection);
	    				 }
	    			 }
	    			 
	    			 //--------------------------------------------------------------------
	    			 // get PV ids on Latest PRD revision 
	    			 //-------------------------------------------------------------------
	    			 ProductVariant pvutil = new ProductVariant(strNextRevId);
	    			 MapList pvList = pvutil.getProductVariants(context, strNextRevId);
	    			 for(int p=0;p<pvList.size();p++){
	    				 Map pvMap = (Map)pvList.get(p);
	    				 String pvID = (String)pvMap.get(DomainObject.SELECT_ID);
	    				 StringList slPFLToRelLogicalID = confUtil.convertObjToStringList(context,
	    						 pvMap.get("from[" + ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST + "].torel.logicalid"));
	    				 StringList slPFLRelID = confUtil.convertObjToStringList(context,
	    						 pvMap.get("from[" + ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST + "].id"));
	    				 for(int cnt=0;cnt<slPFLRelID.size();cnt++){
	    					 String strPFLRelID=(String)slPFLRelID.get(cnt);
	    					 String strPFLToRelLogicalID=(String)slPFLToRelLogicalID.get(cnt);
	    					 if(mpLogicalIDTOObjectID.containsKey(strPFLToRelLogicalID)){
	    						 String strNewLFRELID=(String)mpLogicalIDTOObjectID.get(strPFLToRelLogicalID);
	    						 if(strPFLRelID!=null && strNewLFRELID!=null){
	    							 ProductLineCommon plCommon = new ProductLineCommon();
	    							 plCommon.setToRelationship(context,strPFLRelID,strNewLFRELID,false);
	    						 }	    						 
	    					 }
	    				 }

	    			 }
	    			 ContextUtil.popContext(context);
	    		 }
	    	 } catch (Exception e) {
	    		 throw new FrameworkException(e.getMessage());
	    	 }
	     }	  

	     /** This method is used to update the Varies By or Inactive Varies By Relationship with Configuration Features Relationship on Product.
	      *  If CF is connected with Product using Varies By or Inactive Varies By Relationship and that CF is not connected with any Logical Feature using Varies By or Inactive Varies By Relationship.
	      *  Then connect that CF with Product using Configuration Features Relationship.
	      *  @param context 
	      *               the eMatrix <code>Context</code> object
	      *  @param strProductId
	      *               Product Id
	      *  @param strLFPrevId
	      *               Previous Logical Feature Revision Id
	      *  @param strNewLFRevId
	      *               New Revision Logical Feature Id
	      *  @return void
	      *  @throws Exception
	      *  @since R420                          
	      */
	     private void updateRelToConfigurationFeatures(Context context, String strProductId, String strLFPrevId, String strNewLFRevId) throws Exception
	     { 
	    	 try 
	    	 {
	    	    	// Get all Varies By, Inactive Varies By RelIds from strProductId
	    		    System.out.println("Start Of updateRelToConfigurationFeatures() method. ");
		    	    StringList lstSelect = new StringList();
		    	    lstSelect.add("from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].to.id");
		    	    lstSelect.add("from["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY +"].to.id");
		    	    lstSelect.add("from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].id");
		    	    lstSelect.add("from["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY +"].id");
		    	    
		    	    DomainObject.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].to.id");
		    	    DomainObject.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY +"].to.id");
		    	    DomainObject.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].id");
		    	    DomainObject.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY +"].id");
		    	    
		    	    DomainObject productObj     = DomainObject.newInstance(context, strProductId);
					Map mapVBAndIVBConnectedCFs = (Map) productObj.getInfo(context, lstSelect);
					
					DomainObject.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].to.id");
		    	    DomainObject.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY +"].to.id");
		    	    DomainObject.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].id");
		    	    DomainObject.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY +"].id");
		    	    
		    	    StringList lstPrdVBCFIds   = (StringList) mapVBAndIVBConnectedCFs.get("from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].to.id");
		    	    StringList lstPrdIVBCFIds  = (StringList) mapVBAndIVBConnectedCFs.get("from["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY +"].to.id");
		    	    StringList lstPrdVBRelIds  = (StringList) mapVBAndIVBConnectedCFs.get("from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].id");
		    	    StringList lstPrdIVBRelIds = (StringList) mapVBAndIVBConnectedCFs.get("from["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY +"].id");
		    	    
		    	    StringList listPrdVBandIVBCFIds = new StringList();
		    	    listPrdVBandIVBCFIds.addAll(lstPrdVBCFIds);
		    	    listPrdVBandIVBCFIds.addAll(lstPrdIVBCFIds);
		    	    
		    	    HashMap mapVBandIVBCFs = new HashMap();
		    	    
		    	    if(lstPrdVBCFIds != null && lstPrdVBCFIds.size() > 0){
		    	      for(int i = 0; i < lstPrdVBCFIds.size(); i++)
		    	      {
		    	    	  mapVBandIVBCFs.put(lstPrdVBCFIds.get(i), lstPrdVBRelIds.get(i));
		    	      }
		    	    }
		    	    
		    	    if(lstPrdIVBCFIds != null && lstPrdIVBCFIds.size() > 0){
		    	      for(int i = 0; i < lstPrdIVBCFIds.size(); i++)
			    	  {
			    	      mapVBandIVBCFs.put(lstPrdIVBCFIds.get(i), lstPrdIVBRelIds.get(i));
			    	  }
		    	    }
		    	    
		    	    // Get all Varies By, Inactive Varies By RelIds from Logical Features of strProductId 
		    	    
		    	    String strRelPattern = ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES;
		    	    String strTypes      = ConfigurationConstants.TYPE_LOGICAL_STRUCTURES;
		    	    
		    	    StringList objSelect = new StringList();
		    	    objSelect.add("from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].to.id");
		    	    objSelect.add("from["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY +"].to.id");
		    	    objSelect.add(DomainObject.SELECT_ID);
					
		    	    StringList listPrdLFVBandIVBCFIds = new StringList();
		    	    StringList listPreLFVBandIVBCFIds = new StringList();
		    	    
		    	    DomainObject.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].to.id");
		    	    DomainObject.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY +"].to.id");
		    	    
			    	LogicalFeature logFeatBean        = new LogicalFeature(strProductId);
			    	List<Map<?,?>> mapListofLFObjects = logFeatBean.getLogicalFeatureStructure(context, strTypes, strRelPattern, objSelect, new StringList(), false, true, 1, 0, null, null, (short)0, null);
			    	
			    	DomainObject.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].to.id");
		    	    DomainObject.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY +"].to.id");
		    	    
			    	for(Map mapOfLFObject : mapListofLFObjects)
			    	{
			    		StringList listLFVBCFIds  = ConfigurationUtil.convertObjToStringList(context,(Object)mapOfLFObject.get("from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].to.id"));
			    		StringList listLFIVBCFIds = ConfigurationUtil.convertObjToStringList(context,(Object)mapOfLFObject.get("from["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY +"].to.id"));
			    		
			    		if(!strLFPrevId.equals(mapOfLFObject.get(DomainConstants.SELECT_ID))){
				    		if(listLFVBCFIds != null && listLFVBCFIds.size() > 0){
				    			for(int j = 0; j < listLFVBCFIds.size(); j++){
				    				String strVBCFId = (String) listLFVBCFIds.get(j);
				    				listPrdLFVBandIVBCFIds.add(strVBCFId);
				    			}
				    		}
				    		
				    		if(listLFIVBCFIds != null && listLFIVBCFIds.size() > 0){
				    			for(int k = 0; k < listLFIVBCFIds.size(); k++){
				    				String strIVBCFId = (String) listLFIVBCFIds.get(k);
				    				listPrdLFVBandIVBCFIds.add(strIVBCFId);
				    			}
				    		}
			    		}	
			    	}
			    	
			    	// Compare List of CFs connected with Varies By and Inactive Varies By on Product with Logical Features.
			    	// Remove the common(Connected with Product and Logical Feature with Varies By or Inactive Varies By Rel) CF Ids from listPrdVBandIVBRelIds List 
			    	
			        listPrdVBandIVBCFIds.removeAll(listPrdLFVBandIVBCFIds);
			        
			    	// Get all Varies By, Inactive Varies By RelIds on New LF Rev strNewLFRevId
					
		    	    DomainObject.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].to.id");
		    	    DomainObject.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY +"].to.id");
		    	    DomainObject newLFRevObj      = DomainObject.newInstance(context, strNewLFRevId);
		    	    Map mapNewLFRevVBAndIVBRelIds = (Map) newLFRevObj.getInfo(context, lstSelect);
		    	    DomainObject.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].to.id");
		    	    DomainObject.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY +"].to.id");
		    	    StringList lstNewLFRevVBCFIds  = (StringList) mapNewLFRevVBAndIVBRelIds.get("from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY +"].to.id");
		    	    StringList lstNewLFRevIVBCFIds = (StringList) mapNewLFRevVBAndIVBRelIds.get("from["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY +"].to.id");
		    	    
		    	    StringList listNewLFRevVBandIVBCFIds = new StringList();
		    	    listNewLFRevVBandIVBCFIds.addAll(lstNewLFRevVBCFIds);
		    	    listNewLFRevVBandIVBCFIds.addAll(lstNewLFRevIVBCFIds);
		    	    
		    	    // Compare List of CFs connected with Varies By and Inactive Varies By on Product with Logical Features.
		    	    // Remove the common(Connected with Product and New Revision of Logical Feature with Varies By or Inactive Varies By Rel) CF Ids from listPrdVBandIVBRelIds List 
		    	    
		    	    listPrdVBandIVBCFIds.removeAll(listNewLFRevVBandIVBCFIds);
		    	    
		    	    // If Configuration Features is connected with Product using Varies By or Inactive Varies By Relationship, but it is not connected with Logical Features using Varies By or Inactive Varies By Relationship.
		    	    // Then remove Varies By or Inactive Varies By Relationship of Configuration Feature with Product and Connect that Configuration Feature with Product using Configuration Features Relationship. 
		    	    if(listPrdVBandIVBCFIds != null && listPrdVBandIVBCFIds.size() > 0)
		    	    {
		    	    	for(int m = 0; m < listPrdVBandIVBCFIds.size(); m++)
		    	    	{
		    	    		String strCFId = (String) listPrdVBandIVBCFIds.get(m);
			    	    	DomainRelationship.setType(context, (String) mapVBandIVBCFs.get(strCFId), ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES);
		    	    	}
		    	    }   
		    	    System.out.println("End Of updateRelToConfigurationFeatures() method. ");
			  } 
	    	  catch (Exception e) {
				throw new FrameworkException(e.getMessage());	
					
			  }
	     }
	     
	     /**
			* This method is used to refresh the Structure Tree On Add/Remove for Logical Features.
			* 
			* @param context
			* @param String[]
			* @return Map
			* @throws FrameworkException
			* @since R419.HF5
			*/
			
			@com.matrixone.apps.framework.ui.PostProcessCallable
			public static Map refreshTree(Context context, String[] args) throws Exception
			{
				Map returnMap = new HashMap();
				returnMap.put("Action", "execScript");
				returnMap.put("Message","{ main:function __main(){refreshTreeForAddObj(xmlResponse,'Logical Feature')}}");
				return returnMap;
			}
}





