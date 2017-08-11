/*
 ** emxBuildBase
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not  evidence any actual or intended publication of such program.
 **
 ** static const char RCSID[] = $Id: /java/JPOsrc/base/emxBuildBase.java 1.4.2.22.1.1.1.5 Sat Dec 20 12:57:12 2008 GMT ds-spaul ExperimentalemxBuildBase.java 1.4.2.2 Thu May 22 13:48:21 2008 GMT ds-avvs Experimental$
 *
 * formatted with JxBeauty (c) protectedjohann.langhofer@nextra.at
 */

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.FileList;
import matrix.db.Format;
import matrix.db.FormatItr;
import matrix.db.FormatList;
import matrix.db.JPO;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.dassault_systemes.enovia.e6w.foundation.ServiceBase;
import com.matrixone.apps.common.util.DocumentUtil;
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
import com.matrixone.apps.domain.util.PolicyUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.productline.Build;
import com.matrixone.apps.productline.FileObj;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;
import com.matrixone.apps.domain.util.mxType;

/**
 * The <code>emxBuildBase</code> class contains methods related to Build admin
 * type.
 *
 * @author Enovia MatrixOne
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 *
 */
public class emxBuildBase_mxJPO extends emxProductFile_mxJPO {

	protected String SUITE_KEY="ProductLine";
	/**
	 * Create a new emxBuildBase object from a given id.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds no arguments.
	 * @return a emxPLCBuildJPO
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.0.0.0
	 */
	public emxBuildBase_mxJPO(Context context, String[] args) throws Exception {
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
	 * @since ProductCentral 10.0.0.0
	 */
	public int mxMain(Context context, String[] args) throws Exception {
		if (!context.isConnected()) {
			String strContentLabel = EnoviaResourceBundle.getProperty(context, "ProductLine","emxProduct.Error.UnsupportedClient", context.getSession().getLanguage());
			throw new Exception(strContentLabel);
		}
		return 0;
	}

	/**
	 * Get the list of all builds.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds no arguments.
	 * @return Object of type MapList
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.0.0.0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAllBuilds(Context context, String[] args)
			throws Exception {
		// When all the Builds are to be fetched the where condition is empty
		String strOwnerCondition = null;
		// Calls the protected method to retrieve the data
		MapList objectList = findBuilds(context, strOwnerCondition);
		return objectList;
	}

	/**
	 * Get the list of all builds owned by the context user.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds no arguments.
	 * @return Object of type MapList
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.0.0.0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getOwnedBuilds(Context context, String[] args)
			throws Exception {
		// When all the Builds are to be fetched the where condition is empty
		String strOwnerCondition = context.getUser();
		// Calls the protected method to retrieve the data
		MapList objectList = findBuilds(context, strOwnerCondition);
		return objectList;
	}

	/**
	 * Method call to get all the builds in the data base satisfying the
	 * nescessary condition.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param strOwnerCondition
	 *            - String value containing the owner condition based on which
	 *            results are to be filtered
	 * @return MapList - MapList containing the id of Build objects
	 * @throws Exception
	 *             if errors are encountered.
	 * @since ProductCentral 10.0.0.0
	 * @grade 0
	 */
	protected MapList findBuilds(Context context, String strOwnerCondition)
			throws Exception {
		// String list initialized to retrieve data for the products
		StringList objectList = new StringList(DomainConstants.SELECT_ID);
		objectList.add("current.access[modify]");

		// Gets the actual name of the base type of the product
		String strType = ProductLineConstants.TYPE_BUILDS;
		// The findobjects method is invoked to get the list of products
		MapList relBusObjList = findObjects(context, strType,
				DomainConstants.QUERY_WILDCARD, DomainConstants.QUERY_WILDCARD,
				strOwnerCondition, DomainConstants.QUERY_WILDCARD, null, true,
				objectList);
		if ((relBusObjList == null)){
			throw new Exception();
		}else{
			Map dataMap = null;
			String isEdiatble = null;
			String objectId = null;
			boolean isReleased = false;
			String stateRelease = PropertyUtil.getSchemaProperty(context, "policy", "Build", "state_Release");
			for(int i=0; i<relBusObjList.size(); i++){
				dataMap = (Map)relBusObjList.get(i);
				isEdiatble = (String) dataMap.get("current.access[modify]");
				objectId = (String) dataMap.get(DomainConstants.SELECT_ID);
				isReleased = PolicyUtil.checkState(context, objectId, stateRelease, PolicyUtil.GE);
				if (isReleased || isEdiatble.equalsIgnoreCase("false")) {
					dataMap.put("RowEditable", "readonly");
				}
			}
		}
		return relBusObjList;
	}

	/**
	 * Method to get all the builds related to an object in context.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the HashMap containg the following arguments: paramList
	 *            - HashMap containing the Object Id and relationship name
	 * @return MapList - MapList containing the id of Build objects
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.0.0.0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public List getAllRelatedBuilds(Context context, String[] args)
			throws Exception {
		// When all the Builds are to be fetched the where condition is empty
		String strWhereCondition = null;
		// Calls the protected method to retrieve the data
		List objectList = getRelatedBuilds(context, strWhereCondition, args);
		return objectList;
	}

	/**
	 * Method to get all the Product Builds related to a Product in context.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the HashMap having the following arguments: paramList -
	 *            HashMap containing the Object Id
	 * @return MapList - MapList containing the id of Build objects
	 * @throws Exception
	 *             if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public List getAllProductBuilds(Context context, String[] args)
			throws Exception {
		// When all the Builds are to be fetched the where condition is empty
		String strWhereCondition = null;
		// Calls the protected method to retrieve the data
		List objectList = getRelatedBuilds(context, strWhereCondition,
				ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD, args);
		return objectList;
	}

	/**
	 * Method to get all the Product Configuration Builds related to a Product
	 * Configuration in context.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the HashMap containg the following arguments: paramList
	 *            - HashMap containing the Object Id and relationship name
	 * @return MapList - MapList containing the id of Build objects
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.0.0.0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public List getAllProductConfigurationBuilds(Context context, String[] args)
			throws Exception {
		// When all the Builds are to be fetched the where condition is empty
		String strWhereCondition = null;
		// Calls the protected method to retrieve the data
		List objectList = getRelatedBuilds(context, strWhereCondition,
				ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION_BUILD,
				args);
		return objectList;
	}

	/**
	 * Method to get all the Model Builds related to a Model in context.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the HashMap containg the following arguments: paramList
	 *            - HashMap containing the Object Id and relationship name
	 * @return MapList - MapList containing the id of Build objects
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.0.0.0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public List getAllModelBuilds(Context context, String[] args)
			throws Exception {
		// When all the Builds are to be fetched the where condition is empty
		String strWhereCondition = null;
		String RELATIONSHIP_MODEL_BUILD = PropertyUtil.getSchemaProperty(
				context, "relationship_ModelBuild");
		// Calls the protected method to retrieve the data
		List objectList = getRelatedBuilds(context, strWhereCondition,
				RELATIONSHIP_MODEL_BUILD, args);
		return objectList;
	}

	/**
	 * Method to get all builds connected to Part
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public List getAllPartBuilds(Context context, String[] args)
			throws Exception {
		// When all the Builds are to be fetched the where condition is empty
		String strWhereCondition = null;
		String RELATIONSHIP_ASSOCIATED_BUILD = PropertyUtil.getSchemaProperty(
				context, "relationship_AssociatedBuild");
		// Calls the protected method to retrieve the data
		List objectList = getRelatedBuilds(context, strWhereCondition,
				RELATIONSHIP_ASSOCIATED_BUILD, args);
		return objectList;
	}

	/**
	 * Method to get all owned builds connected Part
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public List getOwnedPartBuilds(Context context, String[] args)
			throws Exception {
		// When all the Builds are to be fetched the where condition is empty
		String strWhereCondition = new StringBuffer(35).append("owner== \"")
				.append(context.getUser()).append("\"").toString();
		String RELATIONSHIP_ASSOCIATED_BUILD = PropertyUtil.getSchemaProperty(
				context, "relationship_AssociatedBuild");
		// Calls the protected method to retrieve the data
		List objectList = getRelatedBuilds(context, strWhereCondition,
				RELATIONSHIP_ASSOCIATED_BUILD, args);
		return objectList;
	}

	/**
	 * Method to get all the builds related to an object in context owned by the
	 * context user.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the HashMap containg the following arguments: paramList
	 *            - HashMap containing the Object Id and relationship name
	 * @return MapList - MapList containing the id of Build objects
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.0.0.0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public List getOwnedRelatedBuilds(Context context, String[] args)
			throws Exception {
		// When all the Builds are to be fetched the where condition is empty
		String strWhereCondition = new StringBuffer(35).append("owner== \"")
				.append(context.getUser()).append("\"").toString();
		// Calls the protected method to retrieve the data
		List objectList = getRelatedBuilds(context, strWhereCondition, args);
		return objectList;
	}

	/**
	 * Method to get all the Product Builds connected to the context Product and
	 * owned by the context user.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the HashMap containing the following arguments:
	 *            paramList - HashMap containing the Object Id and relationship
	 *            name
	 * @return MapList - MapList containing the id of Build objects
	 * @throws Exception
	 *             if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public List getOwnedProductBuilds(Context context, String[] args)
			throws Exception {
		// When all the Builds are to be fetched the where condition is empty
		String strWhereCondition = new StringBuffer(35).append("owner== \"")
				.append(context.getUser()).append("\"").toString();
		// Calls the protected method to retrieve the data
		List objectList = getRelatedBuilds(context, strWhereCondition,
				ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD, args);
		return objectList;
	}

	/**
	 * Method to get all the Product Configuration Builds connected to the
	 * context Product and owned by the context user.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the HashMap containg the following arguments: paramList
	 *            - HashMap containing the Object Id and relationship name
	 * @return MapList - MapList containing the id of Build objects
	 * @throws Exception
	 *             if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public List getOwnedProductConfigurationBuilds(Context context,
			String[] args) throws Exception {
		// When all the Builds are to be fetched the where condition is empty
		String strWhereCondition = new StringBuffer(35).append("owner== \"")
				.append(context.getUser()).append("\"").toString();
		// Calls the protected method to retrieve the data
		List objectList = getRelatedBuilds(context, strWhereCondition,
				ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION_BUILD,
				args);
		return objectList;
	}

	/**
	 * Get the list of all builds under a context.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the HashMap containg the following arguments: paramList
	 *            - HashMap containing the Object Id and relationship name
	 * @param strWhereCondition
	 *            a String containing the where condition
	 * @return Object of type MapList
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.0.0.0
	 */
	protected List getRelatedBuilds(Context context, String strWhereCondition,
			String[] args) throws Exception {
		// Unpacking the arguments
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		// Obtaining the object id and relationship name from the arguments
		String strObjectId = (String) programMap.get("objectId");
		String strRelationshipName = (String) programMap.get("relationName");

		// Instantiating a Maplist object that will contain the object ids
		List relObjIdList = new MapList();
		// Stringlists containing the objectSelects & relationshipSelects
		// parameters
		StringList ObjectSelectsList = new StringList(DomainConstants.SELECT_ID);
		ObjectSelectsList.add("current.access[modify]");

		StringList RelSelectsList = new StringList(
				DomainConstants.SELECT_RELATIONSHIP_ID);
		// Recurse level is set to 1
		short sRecurseLevel = 1;
		// Instantiating DomainObject
		DomainObject domainObject = new DomainObject(strObjectId);
		// Using the symbolic name to get the name of the type
		String strType = ProductLineConstants.TYPE_BUILDS;
		// Calling the getRelatedObjects() method of DomainObject
		relObjIdList = domainObject.getRelatedObjects(context,
				strRelationshipName, strType, ObjectSelectsList,
				RelSelectsList, false, true, sRecurseLevel, strWhereCondition,
				DomainConstants.EMPTY_STRING);
		if ((relObjIdList == null)){
			throw new Exception();
		}else{
			Map dataMap = null;
			String isEdiatble = null;
			String objectId = null;
			boolean isReleased = false;
			String stateRelease = PropertyUtil.getSchemaProperty(context, "policy", "Build", "state_Release");
			for(int i=0; i<relObjIdList.size(); i++){
				dataMap = (Map)relObjIdList.get(i);
				isEdiatble = (String) dataMap.get("current.access[modify]");
				objectId = (String) dataMap.get(DomainConstants.SELECT_ID);
				isReleased = PolicyUtil.checkState(context, objectId, stateRelease, PolicyUtil.GE);
				if (isReleased || isEdiatble.equalsIgnoreCase("false")) {
					dataMap.put("RowEditable", "readonly");
				}
			}
		}
		// Returning the Maplist of Object Ids
		return relObjIdList;
	}

	/**
	 * Get the list of all builds under a context.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the HashMap containg the following arguments: paramList
	 *            - HashMap containing the Object Id and relationship name
	 * @param strWhereCondition
	 *            a String containing the where condition
	 * @param strRelationshipName
	 *            a String containing the relationship name
	 * @return Object of type MapList
	 * @throws Exception
	 *             if the operation fails
	 */
	protected List getRelatedBuilds(Context context, String strWhereCondition,
			String strRelationshipName, String[] args) throws Exception {
		// Unpacking the arguments
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		// Obtaining the object id and relationship name from the arguments
		String strObjectId = (String) programMap.get("objectId");

		// Instantiating a Maplist object that will contain the object ids
		List relObjIdList = new MapList();
		// Stringlists containing the objectSelects & relationshipSelects
		// parameters
		StringList ObjectSelectsList = new StringList(DomainConstants.SELECT_ID);
		ObjectSelectsList.add("current.access[modify]");

		StringList RelSelectsList = new StringList(
				DomainConstants.SELECT_RELATIONSHIP_ID);
		// Recurse level is set to 1
		short sRecurseLevel = 1;
		// Instantiating DomainObject
		DomainObject domainObject = new DomainObject(strObjectId);
		// Using the symbolic name to get the name of the type
		String strType = ProductLineConstants.TYPE_BUILDS;
		// Calling the getRelatedObjects() method of DomainObject
		relObjIdList = domainObject.getRelatedObjects(context,
				strRelationshipName, strType, ObjectSelectsList,
				RelSelectsList, false, true, sRecurseLevel, strWhereCondition,
				DomainConstants.EMPTY_STRING, 0);
		if ((relObjIdList == null)){
			throw new Exception();
		}else{
			Map dataMap = null;
			String isEdiatble = null;
			String objectId = null;
			boolean isReleased = false;
			String stateRelease = PropertyUtil.getSchemaProperty(context, "policy", "Build", "state_Release");
			for(int i=0; i<relObjIdList.size(); i++){
				dataMap = (Map)relObjIdList.get(i);
				isEdiatble = (String) dataMap.get("current.access[modify]");
				objectId = (String) dataMap.get(DomainConstants.SELECT_ID);
				isReleased = PolicyUtil.checkState(context, objectId, stateRelease, PolicyUtil.GE);
				if (isReleased || isEdiatble.equalsIgnoreCase("false")) {
					dataMap.put("RowEditable", "readonly");
				}
			}
		}
		// Returning the Maplist of Object Ids
		return relObjIdList;
	}

	/**
	 * Connects build responsibility.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the HashMap containing the following arguments
	 *            paramMap - contains ObjectId, Old Value for organization name
	 *            and new value
	 * @return int - returns zero if connect successful
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.0.0.0
	 */
	public int connectBuildResponsibility(Context context, String[] args)
			throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String strBuildId = (String) paramMap.get("objectId");

		String strOldOrganizationName = (String) paramMap.get("Old value");

		String strNewOrganizationId = (String) paramMap.get("New Value");

		String strDesignRespRelationship = ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY;

		setId(strBuildId);
		List organizationList = new MapList();
		if (!strOldOrganizationName.equals("")) {
			StringList ObjectSelectsList = new StringList(
					DomainConstants.SELECT_ID);
			StringList RelSelectsList = new StringList(
					DomainConstants.SELECT_RELATIONSHIP_ID);

			String strOrganizationType = ProductLineConstants.TYPE_ORGANIZATION;

			StringBuffer sbWhereCondition = new StringBuffer(25);
			sbWhereCondition = sbWhereCondition.append("name==\"");
			sbWhereCondition = sbWhereCondition.append(strOldOrganizationName);
			sbWhereCondition = sbWhereCondition.append("\"");
			String strWhereCondition = sbWhereCondition.toString();

			organizationList = getRelatedObjects(context,
					strDesignRespRelationship, strOrganizationType,
					ObjectSelectsList, RelSelectsList, true, true, (short) 1,
					strWhereCondition, DomainConstants.EMPTY_STRING);

			String strRelId = (String) ((Hashtable) organizationList.get(0))
					.get(DomainConstants.SELECT_RELATIONSHIP_ID);
			// Disconnecting the existing relationship
			DomainRelationship.disconnect(context, strRelId);
		}

		setId(strNewOrganizationId);
		DomainObject domainObjectToType = newInstance(context, strBuildId);

		DomainRelationship.connect(context, this, strDesignRespRelationship,
				domainObjectToType);

		return 0;
	}

	/**
	 * Connects build with product.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the HashMap containing the following arguments
	 *            paramMap - contains ObjectId, Old Value for product name and
	 *            new value
	 * @return int - returns zero if connect successful
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.0.0.0
	 */
	public int connectProduct(Context context, String[] args) throws Exception {

		/* this is yet to be completed */
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String strBuildId = (String) paramMap.get("objectId");

		String strOldProductName = (String) paramMap.get("Old value");

		String strNewProductId = (String) paramMap.get("New Value");

		String strProductBuildRelationship = ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD;

		setId(strBuildId);
		List productList = new MapList();
		if (!strOldProductName.equals("")) {
			StringList ObjectSelectsList = new StringList(
					DomainConstants.SELECT_ID);
			StringList RelSelectsList = new StringList(
					DomainConstants.SELECT_RELATIONSHIP_ID);
			String strProductType = ProductLineConstants.TYPE_PRODUCTS;
			StringBuffer sbWhereCondition = new StringBuffer(25);
			sbWhereCondition = sbWhereCondition.append("name==\"");
			sbWhereCondition = sbWhereCondition.append(strOldProductName);
			sbWhereCondition = sbWhereCondition.append("\"");
			String strWhereCondition = sbWhereCondition.toString();
			productList = getRelatedObjects(context,
					strProductBuildRelationship, strProductType,
					ObjectSelectsList, RelSelectsList, true, true, (short) 1,
					strWhereCondition, DomainConstants.EMPTY_STRING);
			String strRelId = (String) ((Hashtable) productList.get(0))
					.get(DomainConstants.SELECT_RELATIONSHIP_ID);
			// Disconnecting the existing relationship
			DomainRelationship.disconnect(context, strRelId);
		}

		setId(strNewProductId);
		DomainObject domainObjectToType = newInstance(context, strBuildId);

		DomainRelationship.connect(context, this, strProductBuildRelationship,
				domainObjectToType);

		return 0;
	}

	/**
	 * Connects assigned part.
	 *
	 * @param context
	 *            - the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the HashMap containing the following arguments
	 *            paramMap - contains ObjectId, Old Value for product name and
	 *            new value
	 * @return int - returns zero if connect successful
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.0.0.0
	 */
	public int connectAssignedPart(Context context, String[] args)
			throws Exception {

		/* this is yet to be completed */
		// Unpacking the arguments
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");

		String strOldPartName = (String) paramMap.get("Old value");
		String strBuildId = (String) paramMap.get("objectId");
		String strNewPartId = (String) paramMap.get("New Value");
		String strAssignedPartRelationship = ProductLineConstants.RELATIONSHIP_ASSIGNED_PART;
		setId(strBuildId);

		List partList = new MapList();
		if (!strOldPartName.equals("")) {
			StringList ObjectSelectsList = new StringList(
					DomainConstants.SELECT_ID);
			StringList RelSelectsList = new StringList(
					DomainConstants.SELECT_RELATIONSHIP_ID);
			String strPartType = "Part";
			StringBuffer sbWhereCondition = new StringBuffer(25);
			sbWhereCondition = sbWhereCondition.append("name==\"");
			sbWhereCondition = sbWhereCondition.append(strOldPartName);
			sbWhereCondition = sbWhereCondition.append("\"");
			String strWhereCondition = sbWhereCondition.toString();
			partList = getRelatedObjects(context, strAssignedPartRelationship,
					strPartType, ObjectSelectsList, RelSelectsList, true, true,
					(short) 1, strWhereCondition, DomainConstants.EMPTY_STRING);
			String strRelId = (String) ((Hashtable) partList.get(0))
					.get(DomainConstants.SELECT_RELATIONSHIP_ID);
			// Disconnecting the existing relationship
			DomainRelationship.disconnect(context, strRelId);
		}

		DomainObject domainObjectToType = newInstance(context, strNewPartId);
		DomainRelationship.connect(context, this, strAssignedPartRelationship,
				domainObjectToType);

		return 0;
	}

	/**
	 * Gets the Status icon based on the Estimated and Actual Date comparisons.
	 * if the Estimated or the Actual Date is null then it does not give any
	 * icon.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the hashmap containing the folowing arguments
	 *            objectList MapList containing the object list
	 *            paramList.Hashmap containing the parameters like object id
	 * @return vector of status icon images
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.0.0.0
	 */
	public List slipDaysIcon(Context context, String[] args) throws Exception {
		// Unpacking the arguments
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList = (MapList) programMap.get("objectList");
		// This hashmap contains the object id and threshold among others
		// Retrieving the object id and threshold(from property file)
		String strStatusRed = "";
		String strStatusGreen = "";
		String strLanguage = context.getSession().getLanguage();
		try {
			// Begin of Modify by Enovia MatrixOne for Bug # 303528 Date
			// 04/29/2005
			strStatusRed = EnoviaResourceBundle
					.getProperty(context,"emxProduct.Build.IconRedDays");
			strStatusGreen = EnoviaResourceBundle
					.getProperty(context,"emxProduct.Build.IconGreenDays");
			// End of Modify by Enovia MatrixOne for Bug # 303528 Date
			// 04/29/2005
		} catch (Exception e) {
			String strContentLabel = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
					"emxProduct.Error.MissingStatusIkonValue",strLanguage);
			throw new Exception(strContentLabel);
		}
		// Parsing the strThreshold from String to int
		int nRedIconDays = Integer.parseInt(strStatusRed);
		int nGreenIconDays = Integer.parseInt(strStatusGreen);
		// Get the number of objects in objectList
		int iNumOfObjects = relBusObjPageList.size();
		// Initialising a vector based on the number of objects.
		List StatusIconTagList = new Vector(iNumOfObjects);
		String arrObjId[] = new String[iNumOfObjects];
		// Getting the bus ids for objects in the table
		for (int i = 0; i < iNumOfObjects; i++) {
			Object obj = relBusObjPageList.get(i);
			if (obj instanceof HashMap) {
				arrObjId[i] = (String) ((HashMap) relBusObjPageList.get(i))
						.get(DomainConstants.SELECT_ID);
			} else if (obj instanceof Hashtable) {
				arrObjId[i] = (String) ((Hashtable) relBusObjPageList.get(i))
						.get(DomainConstants.SELECT_ID);
			}
		}
		String strPlannedDate = "";
		String strActualDate = "";
		String strStatusIconTag = DomainConstants.EMPTY_STRING;

		StringList listSelect = new StringList(2);
		String strAttrb1 = "attribute["
				+ ProductLineConstants.ATTRIBUTE_PLANNED_BUILD_DATE + "]";
		String strAttrb2 = "attribute["
				+ ProductLineConstants.ATTRIBUTE_ACTUAL_BUILD_DATE + "]";
		listSelect.addElement(strAttrb1);
		listSelect.addElement(strAttrb2);
        //XSSOK
		String GREEN_ICON = "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\"  align=\"middle\"/>";
        //XSSOK		
		String RED_ICON = "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\"  align=\"middle\"/>";
        //XSSOK		
		String YELLOW_ICON = "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\"  align=\"middle\"/>";

		// Instantiating BusinessObjectWithSelectList of matrix.db and fetching
		// attributes of the objectids
		BusinessObjectWithSelectList attributeList = getSelectBusinessObjectData(
				context, arrObjId, listSelect);

		for (int i = 0; i < iNumOfObjects; i++) {
			strPlannedDate = attributeList.getElement(i).getSelectData(
					strAttrb1);
			strActualDate = attributeList.getElement(i)
					.getSelectData(strAttrb2);
			// Initialising a String which will contain the status icon
			strStatusIconTag = DomainConstants.EMPTY_STRING;
			// If both the dates are null then no icon is to be displayed
			if (strActualDate.equalsIgnoreCase(DomainConstants.EMPTY_STRING)
					|| strPlannedDate
							.equalsIgnoreCase(DomainConstants.EMPTY_STRING)) {
				strStatusIconTag = DomainConstants.EMPTY_STRING;
			}
			// If both the dates are non null values go ahead with the
			// processing
			else if (!(strActualDate
					.equalsIgnoreCase(DomainConstants.EMPTY_STRING))
					&& !(strPlannedDate
							.equalsIgnoreCase(DomainConstants.EMPTY_STRING))) {
				int nDuration = ProductLineUtil.daysBetween(strActualDate,
						strPlannedDate);
				// If the Duration is equal to or Greater than the RedIconDays
				// than show Red
				// If it is between the RedIconDays and Green Icon Days(if it is
				// < Red Days and > Green Days) show
				// yellow if not show green.
				// Assumption:The property file entry for the RedIconDays needs
				// to be greater than the
				// value of the Green Icon Days always
				if (nDuration >= nRedIconDays) {
					strStatusIconTag = RED_ICON;
				} else if ((nDuration < nRedIconDays)
						&& (nDuration >= nGreenIconDays)) {
					strStatusIconTag = YELLOW_ICON;
				} else {
					strStatusIconTag = GREEN_ICON;
				}
			} else {
				// If actual build date is not null
				strStatusIconTag = DomainConstants.EMPTY_STRING;
			}
			StatusIconTagList.add(strStatusIconTag);
		}
		// Returning the Status icon
		return StatusIconTagList;
	}

	/**
	 * Checks Product Configuration states to allow Remove/Delete Operations.
	 *
	 * @param context
	 *            - the eMatrix <code>Context</code> object
	 * @param args
	 *            Holds the following arguments 0 - HashMap containing the
	 *            parent object id.
	 * @return boolean returns true if check successful
	 * @throws Exception
	 *             if the operation fails
	 */
	public boolean checkAccessForProdConfig(Context context, String args[])
			throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strParentObjId = (String) programMap.get("parentOID");
		//commneted for 268396V6R2014x
		boolean hasVPLMViewerRole = PersonUtil.hasAssignment(context,PropertyUtil.getSchemaProperty(context, "role_VPLMViewer"));
		boolean hasVPLMCreatorRole = PersonUtil.hasAssignment(context,PropertyUtil.getSchemaProperty(context, "role_VPLMCreator"));
		
		if(hasVPLMViewerRole && !hasVPLMCreatorRole)
		return false;
		
		if (strParentObjId != null
				&& !DomainConstants.EMPTY_STRING.equals(strParentObjId)
				&& !"null".equals(strParentObjId)) {
			DomainObject prodConfig = new DomainObject(strParentObjId);
			if (prodConfig.isKindOf(context,
					ProductLineConstants.TYPE_PRODUCT_CONFIGURATION)) {
				return PolicyUtil
						.checkState(
								context,
								strParentObjId,
								ProductLineConstants.STATE_PRODUCT_CONFIGURATION_VALIDATE_CONFIGURATION,
								PolicyUtil.LE);
			}
		}
		return true;
	}

	/**
	 * Method for checking in multiple files.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            Holds the HashMap with following arguments fcsEnabled store
	 *            paramList objectId format fileName.
	 * @return map of files
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.0.0.0
	 */
	public Map multiFileCheckin(Context context, String[] args)
			throws Exception {
		HashMap uploadParamsMap = (HashMap) JPO.unpackArgs(args);
		String fcsEnabled = (String) uploadParamsMap.get("fcsEnabled");
		String receiptValue = (String) uploadParamsMap.get(DocumentUtil
				.getJobReceiptParameterName(context));
		String store = (String) uploadParamsMap.get("store");

		Map objectMap = multiFileCheckinCreate(context, args);

		StringList objectIds = (StringList) objectMap.get("objectId");
		StringList formats = (StringList) objectMap.get("format");
		StringList fileNames = (StringList) objectMap.get("fileName");

		if ("true".equalsIgnoreCase(fcsEnabled) && objectIds.size() > 0) {
			multiFileCheckinUpdate(context, objectIds, store, formats,
					fileNames, receiptValue);
		}
		return objectMap;
	}

	/**
	 * This method is executed to create an object and checkin using FCS.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the HashMap containing the following arguments
	 *            fcsEnabled noOfFiles parentId parentRelId format fileName.
	 * @return String new Document Id
	 * @throws Exception
	 *             if the operation fails
	 * @since Product Central 10.0.0.0
	 */
	public Map multiFileCheckinCreate(Context context, String[] args)
			throws Exception {
		Map objectMap = new HashMap();

		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}
		HashMap uploadParamsMap = (HashMap) JPO.unpackArgs(args);

		String fcsEnabled = (String) uploadParamsMap.get("fcsEnabled");

		String strCount = (String) uploadParamsMap.get("noOfFiles");
		String parentId = (String) uploadParamsMap.get("parentId");
		String parentRelName = (String) uploadParamsMap.get("parentRelName");
		if (parentRelName != null) {
			parentRelName = PropertyUtil.getSchemaProperty(context,
					parentRelName);
		}

		int count = new Integer(strCount).intValue();
		StringList objectIds = new StringList(count);
		StringList formats = new StringList(count);
		StringList fileNames = new StringList(count);
		objectMap.put("format", formats);
		objectMap.put("fileName", fileNames);
		objectMap.put("objectId", objectIds);

		DomainObject distribObject = new DomainObject();
		setId(parentId);
		HashMap attribMap = new HashMap();
		boolean append = true;
		boolean unlock = true;
		for (int i = 0; i < count; i++) {
			String format = (String) uploadParamsMap.get("format" + i);
			String title = (String) uploadParamsMap.get("title" + i);
			String description = (String) uploadParamsMap
					.get("description" + i);
			if (title != null && !"".equals(title) && !"null".equals(title)) {
				String uniqueName = distribObject.getUniqueName("DF_");
				distribObject.createAndConnect(context,
						ProductLineConstants.TYPE_DISTRIBUTION_FILE,
						uniqueName, parentRelName, this, true);
				String objectId = distribObject.getInfo(context,
						DomainConstants.SELECT_ID);
				attribMap.put(DomainConstants.ATTRIBUTE_NOTES, description);
				attribMap.put(DomainConstants.ATTRIBUTE_ORIGINATOR,
						context.getUser());
				distribObject.setAttributeValues(context, attribMap);
				if ("false".equalsIgnoreCase(fcsEnabled)) {
					StringList strFileList = new StringList();
					strFileList.add(title);
					distribObject.checkinFromServer(context, unlock, append,
							format, null, strFileList);
				} else {
					objectIds.addElement(objectId);
					formats.addElement(format);
					fileNames.addElement(title);
				}
			}
		}
		return objectMap;
	}

	/**
	 * Method for getting info on distribution files.
	 *
	 * @param context
	 *            - the eMatrix <code>Context</code> object
	 * @param args
	 *            Holds the following arguments 0 - HashMap containing the
	 *            object id
	 * @return map of files
	 * @since Product Central 10.0.0.0
	 * @throws Exception
	 *             if the operation fails
	 */
	public HashMap getDistributionFilesInfo(Context context, String[] args)
			throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) programMap.get("objectId");

		setId(objectId);

		short level = 1;
		StringList objectSelects = new StringList();
		objectSelects.add(DomainConstants.SELECT_ID);
		objectSelects.add("attribute[" + DomainConstants.ATTRIBUTE_NOTES + "]");

		MapList distribList = getRelatedObjects(context,
				ProductLineConstants.RELATIONSHIP_DISTRIBUTION_FILE,
				ProductLineConstants.TYPE_DISTRIBUTION_FILE, objectSelects,
				null, false, true, level, "", "");

		HashMap returnMap = new HashMap();

		DomainObject distribObject = new DomainObject();
		StringList selectList = new StringList();
		selectList.add(DomainConstants.SELECT_FILE_NAME);
		selectList.add(DomainConstants.SELECT_FILE_SIZE);

		for (int i = 0; i < distribList.size(); i++) {
			Map tmpMap = (Hashtable) distribList.get(i);
			String description = (String) tmpMap.get("attribute["
					+ DomainConstants.ATTRIBUTE_NOTES + "]");
			String distribObjId = (String) tmpMap
					.get(DomainConstants.SELECT_ID);
			distribObject.setId(distribObjId);

			Map distribInfo = distribObject.getInfo(context, selectList);

			StringList fileNameList = (StringList) distribInfo
					.get(DomainConstants.SELECT_FILE_NAME);
			StringList fileSizeList = (StringList) distribInfo
					.get(DomainConstants.SELECT_FILE_SIZE);
			FormatList formatList = distribObject.getFormats(context);

			String strFileName = (String) fileNameList.get(0);
			String strFileSize = (String) fileSizeList.get(0);
			String strFormat = "";
			if (formatList == null) {
				throw new Exception(
						"No formats associated with Business Object");
			}

			FormatItr formatItr = new FormatItr(formatList);

			while (formatItr.next()) {
				Format fileFormat = formatItr.obj();
				strFormat = fileFormat.getName();
				FileList fileList = getFiles(context, strFormat);

				if (fileList != null && fileList.size() > 0) {
					break;
				}
			}

			FileObj tmpFO = new FileObj();
			tmpFO.setFileName(strFileName);
			tmpFO.setFileSize(strFileSize);
			tmpFO.setFormat(strFormat);
			tmpFO.setComment(description);
			tmpFO.setBusObjectId(distribObjId);
			returnMap.put(fileNameList.get(0), tmpFO);
		}
		return returnMap;
	}

	/**
	 * This is the base method executed in common Document model to
	 * checkin/update/createmaster using FCS/NonFCS.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the HashMap containing the following arguments
	 *            fcsEnabled store paramList objectId format fileName.
	 * @return Map objectMap which contains objectId, filename, format pairs and
	 *         errorMessage if any error.
	 * @throws Exception
	 *             if the operation fails
	 * @since VCP 10.5.0.0
	 */
	public Map commonDocumentCheckin(Context context, String[] args)
			throws Exception {
		HashMap uploadParamsMap = (HashMap) JPO.unpackArgs(args);
		String fcsEnabled = (String) uploadParamsMap.get("fcsEnabled");
		String receiptValue = (String) uploadParamsMap.get(DocumentUtil
				.getJobReceiptParameterName(context));
		String store = (String) uploadParamsMap.get("store");

		Map objectMap = CheckinProcess(context, args);

		StringList objectIds = (StringList) objectMap.get("objectId");
		StringList formats = (StringList) objectMap.get("format");
		StringList fileNames = (StringList) objectMap.get("fileName");

		if ("true".equalsIgnoreCase(fcsEnabled) && objectIds.size() > 0) {
			multiFileCheckinUpdate(context, objectIds, store, formats,
					fileNames, receiptValue);
		}
		return objectMap;
	}

	/**
	 * This method is executed to create an object and checkin using FCS.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the HashMap with following arguments fcsEnabled
	 *            noOfFiles parentId parentRelId.
	 * @return String new Document Id
	 * @throws Exception
	 *             if the operation fails
	 * @since Common 10.0.0.0
	 */
	public Map CheckinProcess(Context context, String[] args) throws Exception {
		Map objectMap = new HashMap();

		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}
		HashMap uploadParamsMap = (HashMap) JPO.unpackArgs(args);

		String fcsEnabled = (String) uploadParamsMap.get("fcsEnabled");
		String errorMessage = "";
		String strCount = (String) uploadParamsMap.get("noOfFiles");
		String parentId = (String) uploadParamsMap.get("parentId");
		String parentRelName = (String) uploadParamsMap.get("parentRelName");
		if (parentRelName != null) {
			parentRelName = PropertyUtil.getSchemaProperty(context,
					parentRelName);
		}

		int count = new Integer(strCount).intValue();
		StringList objectIds = new StringList(count);
		StringList formats = new StringList(count);
		StringList fileNames = new StringList(count);
		objectMap.put("format", formats);
		objectMap.put("fileName", fileNames);
		objectMap.put("objectId", objectIds);

		DomainObject distribObject = new DomainObject();
		setId(parentId);
		HashMap attribMap = new HashMap();
		boolean append = true;
		boolean unlock = true;
		for (int i = 0; i < count; i++) {
			String format = (String) uploadParamsMap.get("format" + i);
			String title = (String) uploadParamsMap.get("fileName" + i);
			String description = (String) uploadParamsMap.get("comments" + i);
			if (title != null && !"".equals(title) && !"null".equals(title)) {
				if (!checkDuplicate(context, uploadParamsMap, title, parentId)) {

					String uniqueName = distribObject.getUniqueName("DF_");
					distribObject.createAndConnect(context,
							ProductLineConstants.TYPE_DISTRIBUTION_FILE,
							uniqueName, parentRelName, this, true);
					String objectId = distribObject.getInfo(context,
							DomainConstants.SELECT_ID);
					attribMap.put(DomainConstants.ATTRIBUTE_NOTES, description);
					attribMap.put(DomainConstants.ATTRIBUTE_ORIGINATOR,
							context.getUser());
					distribObject.setAttributeValues(context, attribMap);
					if ("false".equalsIgnoreCase(fcsEnabled)) {
						StringList strFileList = new StringList();
						strFileList.add(title);
						distribObject.checkinFromServer(context, unlock,
								append, format, null, strFileList);
					} else {
						objectIds.addElement(objectId);
						formats.addElement(format);
						fileNames.addElement(title);
					}
				} else {
					if (!errorMessage.equals("")) {
						errorMessage += ", ";
					}
					errorMessage += title;
				}
			}
		}
		if (!errorMessage.equals("")) {
			StringList errorFileList = FrameworkUtil.split(errorMessage, ",");
			errorMessage =  EnoviaResourceBundle.getProperty(context, SUITE_KEY,
					"emxProduct.Alert.FileAlreadyCheckedIn",context.getSession()
							.getLanguage());
			Iterator itr = errorFileList.iterator();
			while (itr.hasNext()) {
				errorMessage += " \n" + (String) itr.next();
			}
		}
		objectMap.put("errorMessage", errorMessage);
		return objectMap;
	}

	/**
	 * This method checks for the duplicate files being checked in.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param uploadParamsMap
	 *            holds Parameters
	 * @param fileName
	 *            holds name of the file to be checked in.
	 * @param objectId
	 *            holds the object Id.
	 * @return boolean
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.5
	 */

	public boolean checkDuplicate(Context context, HashMap uploadParamsMap,
			String fileName, String objectId) throws Exception {
		if (objectId == null || "".equals(objectId) || "null".equals(objectId)) {
			return false;
		}
		boolean isDuplicateFile = false;
		Map argsMap = new HashMap();
		argsMap.put("objectId", objectId);
		String[] args = JPO.packArgs(argsMap);

		Map fileInfoMap = getDistributionFilesInfo(context, args);

		FileObj oneFO;
		Set keySet = fileInfoMap.keySet();
		Iterator keyIter = keySet.iterator();

		while (keyIter.hasNext()) {
			oneFO = (FileObj) fileInfoMap.get(keyIter.next());
			String strFileName = oneFO.getFileName();

			if (fileName.equals(strFileName)) {
				isDuplicateFile = true;
				break;
			}

		} // while : loop through all of the file objects

		return isDuplicateFile;
	}

	/**
	 * This method is used to get the information about files related to a
	 * Distribution Object.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the parameters to be passed to the method
	 *            getDistribFileInfo which is called from this method.
	 * @return MapList mapList which contains objectId, filename,filesize format
	 *         and comments
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.5SP1
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getDistribFiles(Context context, String[] args)
			throws Exception {
		MapList mapList = new MapList();

		Map mapFileObjs = getDistributionFilesInfo(context, args);
		// make the maplist : for every file that is here (represented by one
		// FileObj)
		Map mapOneEntry;
		FileObj oneFO;
		Set keySet = mapFileObjs.keySet();
		Iterator keyIter = keySet.iterator();

		// loop through all the file objects

		while (keyIter.hasNext()) {
			oneFO = (FileObj) mapFileObjs.get(keyIter.next());

			if (oneFO == null) {
				continue;
			}

			mapOneEntry = new HashMap();

			mapOneEntry.put("fileName", oneFO.getFileName());
			mapOneEntry.put("fileSize", oneFO.getFileSize());
			mapOneEntry.put("format", oneFO.getFormat());
			mapOneEntry.put("reasonForChange", oneFO.getComment());
			mapOneEntry.put("objectId", oneFO.getBusObjectId());
			mapOneEntry.put(DomainConstants.SELECT_ID, oneFO.getBusObjectId());

			mapList.add(mapOneEntry);

		} // End of the while loop

		return mapList;
	} // End of the method

	/**
	 * This method is used to get the filenames of all the files. which are
	 * associated with Distribution File object.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the parameters passed from the calling method When
	 *            this array is unpacked, arguments corresponding to the
	 *            following String keys are found:- objectList- MapList
	 *            Containing the objectIds.
	 * @return Vector
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.5SP1
	 **/

	public Vector getDistributionFileNames(Context context, String[] args)
			throws Exception {
		Vector fileNameVector = new Vector();

		Map programMap = (Map) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		Iterator objectListItr = objectList.iterator();
		String strFileName = "";

		// loop through all the files

		Map objectMap = new HashMap();

		while (objectListItr.hasNext()) {
			objectMap = (Map) objectListItr.next();
			strFileName = (String) objectMap.get("fileName");
			fileNameVector.add(XSSUtil.encodeForHTML(context,strFileName));
		}// End of while loop

		return fileNameVector;
	}

	/**
	 * This method is used to get the Comments for all the files. which are
	 * associated with Distribution File object.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the parameters passed from the calling method When
	 *            this array is unpacked, arguments corresponding to the
	 *            following String keys are found:- objectList- MapList
	 *            Containing the objectIds.
	 * @return Vector
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.5SP1
	 **/
	public Vector getDistributionFileComments(Context context, String[] args)
			throws Exception {
		Vector fileCommentVector = new Vector();

		Map programMap = (Map) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		Iterator objectListItr = objectList.iterator();
		String strFileComment = "";

		Map objectMap = new HashMap();
		// loop through all the files
		while (objectListItr.hasNext()) {
			objectMap = (Map) objectListItr.next();
			strFileComment = (String) objectMap.get("reasonForChange");
			fileCommentVector.add(XSSUtil.encodeForHTML(context,strFileComment));
		}// End of while loop

		return fileCommentVector;

	} // End of the method

	/**
	 * This method is used to get the filesize for all the files. which are
	 * associated with Distribution File object.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the parameters passed from the calling method When
	 *            this array is unpacked, arguments corresponding to the
	 *            following String keys are found:- objectList- MapList
	 *            Containing the objectIds.
	 * @return Vector
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.5SP1
	 **/

	public Vector getDistributionFileSizes(Context context, String[] args)
			throws Exception {
		Vector fileSizeVector = new Vector();

		Map programMap = (Map) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		Iterator objectListItr = objectList.iterator();
		String strFileSize = "";

		Map objectMap = new HashMap();
		// loop through all the files
		while (objectListItr.hasNext()) {
			objectMap = (Map) objectListItr.next();
			strFileSize = (String) objectMap.get("fileSize");
			fileSizeVector.add(XSSUtil.encodeForHTML(context,strFileSize));
		}// End of while loop

		return fileSizeVector;

	} // End of the method

	/**
	 * This method is used to get the format for all the files. which are
	 * associated with Distribution File object.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the parameters passed from the calling method When
	 *            this array is unpacked, arguments corresponding to the
	 *            following String keys are found:- objectList- MapList
	 *            Containing the objectIds.
	 * @return Vector
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductCentral 10.5SP1
	 **/

	public Vector getDistributionFileFormats(Context context, String[] args)
			throws Exception {
		Vector fileFormatVector = new Vector();

		Map programMap = (Map) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		String strLanguage = context.getSession().getLanguage();
		i18nNow i18nnow = new i18nNow();
		Iterator objectListItr = objectList.iterator();
		String strFileFormat = "";

		Map objectMap = new HashMap();
		// loop through all the files
		while (objectListItr.hasNext()) {
			objectMap = (Map) objectListItr.next();
			strFileFormat = (String) objectMap.get("format");
			// Modified by Sandeep, Enovia MatrixOne for Bug # 311164 on
			// 16-Nov-05
			strFileFormat = i18nNow.getFormatI18NString(strFileFormat, context
					.getSession().getLanguage());
			fileFormatVector.add(XSSUtil.encodeForHTML(context,strFileFormat));
		} // End of while loop

		return fileFormatVector;

	} // End of the method

	/**
	 * This method generates DownLoad Icons which acts as hyperLinks.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            an array of arguments corresponding to the following String
	 *            keys are found:- objectList:a MapList containing all the
	 *            objectIds paramList a Map containing parameters like
	 *            reportFormat and languageStr
	 * @return Vector object that contains a vector of html code to construct
	 *         the DownLoad Column.
	 * @throws Exception
	 *             if the operation fails
	 *
	 * @since Common 10.5SP1
	 **/

	public static Vector getDistributionFileDownLoad(Context context,
			String[] args) throws Exception {
		Vector fileActionsVector = new Vector();

		Map programMap = (Map) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		Map paramList = (Map) programMap.get("paramList");

		boolean isprinterFriendly = false;
		if (paramList.get("reportFormat") != null) {
			isprinterFriendly = true;
		}

		Iterator objectListItr = objectList.iterator();
		String languageStr = (String) paramList.get("languageStr");

		String strMasterId = "";
		String strFileActions = "";
		String strFileName = "";
		String strFileFormat = "";
		String strEncodedFileName = "";
		String strEncodedFormat = "";
		boolean canDownload = true;
		String strTipDownload =  EnoviaResourceBundle.getProperty(context, "Components",
				"emxComponents.DocumentSummary.ToolTipDownload",
				languageStr);

		// loop through all the files
		while (objectListItr.hasNext()) {
			strFileName = "";
			strFileActions = "";
			StringBuffer fileActionsStrBuff = new StringBuffer(70);

			Map objectMap = (Map) objectListItr.next();
			strMasterId = (String) objectMap.get("objectId");

			strFileName = (String) objectMap.get("fileName");
			strEncodedFileName = strFileName;

			strFileFormat = (String) objectMap.get("format");

			strEncodedFormat = strFileFormat;

			if (canDownload) {
				if (!isprinterFriendly) {

					fileActionsStrBuff.append("<a href='javascript:callCheckout(\"");
					fileActionsStrBuff.append(XSSUtil.encodeForHTMLAttribute(context,strMasterId));
					fileActionsStrBuff.append("\",\"download\", \"\", \"\",\"");
					fileActionsStrBuff.append("\"");
                    fileActionsStrBuff.append(")'>");
					fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\"");
					fileActionsStrBuff.append(XSSUtil.encodeForHTMLAttribute(context,strTipDownload));
					fileActionsStrBuff.append("\" title=\"");
					fileActionsStrBuff.append(XSSUtil.encodeForHTMLAttribute(context,strTipDownload));
					fileActionsStrBuff.append("\"></img></a>&#160;");
				} else {
					fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\"");
					fileActionsStrBuff.append(XSSUtil.encodeForHTMLAttribute(context,strTipDownload));
					fileActionsStrBuff.append("\"></img>&#160;");
				}// End of inner if
			} // End of outer if
			strFileActions = fileActionsStrBuff.toString();

			fileActionsVector.add(strFileActions);
		} // End of while loop

		return fileActionsVector;

	}// End of the method

	/**
	 * This method gets the object Structure List for the context Build
	 * object.This method gets invoked by settings in the command which displays
	 * the Structure Navigator for Build type objects
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: paramMap - Map having
	 *            object Id String
	 * @return MapList containing the object list to display in Build structure
	 *         navigator
	 * @throws Exception
	 *             if the operation fails
	 * @since Product Central 10-6
	 */

	public static MapList getStructureList(Context context, String[] args)
			throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String objectId = (String) paramMap.get("objectId");

		DomainObject domObj = DomainObject.newInstance(context, objectId);
		String objectType = domObj.getInfo(context, DomainObject.SELECT_TYPE);
		String objectParentType = emxPLCCommon_mxJPO.getParentType(context,
				objectType);

		MapList buildStructList = new MapList();

		// if(objectParentType != null &&
		// objectParentType.equals(ProductLineConstants.TYPE_BUILDS)) {
		// X+5 PLC Build Enhancements 05/09/2008
		if (objectParentType != null
				&& objectParentType.equals(ProductLineConstants.TYPE_UNIT)) {
			// X+5 PLC Build Enhancements 05/09/2008
			Pattern relPattern = new Pattern(
					ProductLineConstants.RELATIONSHIP_BUILD_SATISFIES);
			// include type 'Features' in Builds structure navigation list
			Pattern typePattern = new Pattern(
					ProductLineConstants.TYPE_FEATURES);

			try {
				buildStructList = ProductLineCommon.getObjectStructureList(
						context, objectId, relPattern, typePattern);
			} catch (Exception ex) {
				throw new FrameworkException(ex);
			}
		} else {
			buildStructList = (MapList) emxPLCCommon_mxJPO
					.getStructureListForType(context, args);
		}
		return buildStructList;
	}

	/**
	 * Display Number of Builds Field in the Build Create Web Form
	 *
	 * @param context
	 *            the Matrix Context
	 * @param args
	 *            no args needed for this method
	 * @returns Object - return the HTML of textbox with onblur() event
	 * @throws Exception
	 *             if the operation fails
	 * @since PLC X4
	 */
	public Object getNoOfBuildstoCreate(Context context, String[] args)
			throws Exception {

		try {
			StringBuffer strBuf = new StringBuffer();
			// strBuf.append("<input type=\"textbox\" id=\"noofbuilds\" value=\"1\" name=\"noofbuilds\" onblur=\"checkNameField();\"  />");
			// this is now changed to Onclick event for bug no:359387
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String operation = (String) requestMap.get("operation");
			if ("asbuilt".equals(operation)) {
				strBuf.append("<input type=\"textbox\" id=\"noofbuilds\" value=\"1\" name=\"noofbuilds\" readOnly='true' />");
			} else {
				strBuf.append("<input type=\"textbox\" id=\"noofbuilds\" value=\"1\" name=\"noofbuilds\" />");
			}
			return strBuf.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
	}

	/**
	 * Display Context Product Name in the WebForm
	 *
	 * @param context
	 *            the Matrix Context
	 * @param args
	 *            no args needed for this method
	 * @returns String
	 * @throws Exception
	 *             if the operation fails
	 * @since PLC X4
	 */
	public String displayContextProduct(Context context, String[] args)
			throws Exception {
		// unpacking the Arguments from variable args
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		// fixed for Mx378056
		// start- get Product's Id
		String strObjId = "";
		String parentObjID = (String) requestMap.get("parentOID");
		String objID = (String) requestMap.get("objectId");
		String returnStr = "";
		// if both objectId and parentOID is equal, means build is creating
		// under product context and Product node is selected.
		if (objID != null && parentObjID.equals(objID)) {
			strObjId = parentObjID;
		} else {
			DomainObject dstrObjId1 = DomainObject.newInstance(context, objID);
			// if objectId pass is not of type Product,means build is selected
			// or build is under PC,as Product node selected.
			if (!dstrObjId1.isKindOf(context,
					ProductLineConstants.TYPE_PRODUCTS)) {
				Object emxTableRowId = requestMap.get("emxTableRowId");
				// if build is selected,i.e. objectID is build
				if (emxTableRowId != null) {
					String emxTableRowIdstr = emxTableRowId.toString();
					StringTokenizer st = new StringTokenizer(emxTableRowIdstr,
							"|");
					String relId = "";
					// get the first token, the rel id.
					relId = st.nextToken();
					if (relId != null) {
						DomainRelationship domrelPCBId = new DomainRelationship(
								relId);
						StringList sList = new StringList();
						sList.add(DomainRelationship.SELECT_FROM_ID);
						String[] arr = new String[1];
						arr[0] = relId;
						MapList objMapList = DomainRelationship.getInfo(
								context, arr, sList);
						// To get the PC Id
						for (int j = 0; j < objMapList.size(); j++) {
							Map objFLMap = (Map) objMapList.get(j);
							strObjId = (String) objFLMap
									.get(DomainRelationship.SELECT_FROM_ID);
						}

					}
				} else {
					// create build under PC,objectID is PC
					strObjId = parentObjID;
				}
			}
		}
		// end- get Product's Id
		String strContext = (String) requestMap.get("createContext");

		Map fieldMap = (HashMap) programMap.get("fieldMap");
		String strFieldName = (String) fieldMap.get("name");

		StringBuffer sbBuffer = new StringBuffer();

		DomainObject dstrObjId = DomainObject.newInstance(context, strObjId);
		if (dstrObjId.isKindOf(context, ProductLineConstants.TYPE_PRODUCTS)) {
			if (strObjId != null && strObjId.length() > 0) {
				setId(strObjId);
				DomainObject dProductId = DomainObject.newInstance(context,
						strObjId);
				// getting the value
				String sProductName = dProductId.getInfo(context,
						DomainObject.SELECT_NAME);
				sbBuffer.append("<input type=\"text\" readonly=\"true\" ");
				sbBuffer.append("name=\"");
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
				sbBuffer.append("Display\" id=\"" + XSSUtil.encodeForHTMLAttribute(context,strObjId) + "\" value=\""
						+ XSSUtil.encodeForHTMLAttribute(context,sProductName) + "\"");
				sbBuffer.append("/>");
				sbBuffer.append("<input type=\"hidden\" name=\"");
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
				sbBuffer.append("\" value=\"" + XSSUtil.encodeForHTMLAttribute(context,strObjId) + "\"");
				sbBuffer.append("/>");
				sbBuffer.append("<input type=\"hidden\" name=\"");
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
				sbBuffer.append("OID\" value=\"" + XSSUtil.encodeForHTMLAttribute(context,strObjId) + "\"");
				sbBuffer.append("/>");
				returnStr = sbBuffer.toString();
				returnStr = returnStr.replaceAll("&", "&amp;");
				return returnStr;
			}

		} else {
			if (strObjId != null && strObjId.length() > 0) {
				setId(strObjId);
				DomainObject dProductConfigurationId = DomainObject
						.newInstance(context, strObjId);

				String strProductId = "";
				String strProductName = "";

				StringList strListSelectstmts = new StringList();
				strListSelectstmts.add(DomainObject.SELECT_ID);
				strListSelectstmts.add(DomainObject.SELECT_NAME);

				MapList productConfigurationRelatedObjects = dProductConfigurationId
						.getRelatedObjects(
								context,
								ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION,
								ProductLineConstants.TYPE_PRODUCTS,
								strListSelectstmts, null, true, true,
								(short) 1, null, null);
				if (productConfigurationRelatedObjects != null) {
					for (int i = 0; i < productConfigurationRelatedObjects
							.size(); i++) {
						Hashtable productMap = (Hashtable) productConfigurationRelatedObjects
								.get(i);
						strProductName = (String) productMap.get("name");
						strProductId = (String) productMap.get("id");
					}
				}

				sbBuffer.append("<input type=\"text\" readonly=\"true\" ");
				sbBuffer.append("name=\"");
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
				sbBuffer.append("Display\" id=\"" + XSSUtil.encodeForHTMLAttribute(context,strProductId)
						+ "\" value=\"" + XSSUtil.encodeForHTMLAttribute(context,strProductName) + "\"");
				sbBuffer.append("/>");
				sbBuffer.append("<input type=\"hidden\" name=\"");
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
				sbBuffer.append("\" value=\"" + strProductId + "\"");
				sbBuffer.append("/>");
				sbBuffer.append("<input type=\"hidden\" name=\"");
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
				sbBuffer.append("OID\" value=\"" + XSSUtil.encodeForHTMLAttribute(context,strProductId) + "\"");
				sbBuffer.append("/>");
				returnStr = sbBuffer.toString();
				returnStr = returnStr.replaceAll("&", "&amp;");
				return returnStr;
			}

		}
		return "";
	}

	/**
	 * Display Context Product Configuration Name in the WebForm
	 *
	 * @param context
	 *            the Matrix Context
	 * @param args
	 *            no args needed for this method
	 * @returns String
	 * @throws Exception
	 *             if the operation fails
	 * @since PLC X4
	 */
	public String displayContextProductConfiguration(Context context,
			String[] args) throws Exception {
		// unpacking the Arguments from variable args
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		// fixed for Mx378056
		// start fix for IR-028728V6R2011-
		String strObjId = "";
		String parentObjID = (String) requestMap.get("parentOID");
		String objID = (String) requestMap.get("objectId");
		String returnStr = "";
		// Either PC or PRD
		if (objID != null && parentObjID != null && parentObjID.equals(objID)) {
			strObjId = parentObjID;
		} else {
			DomainObject dstrObjId1 = DomainObject.newInstance(context, objID);
			strObjId = objID;
			// if parenOID pass is not of type PC
			if (!dstrObjId1.isKindOf(context,
					ProductLineConstants.TYPE_PRODUCT_CONFIGURATION)) {
				Object emxTableRowId = requestMap.get("emxTableRowId");
				String emxTableRowIdstr = emxTableRowId.toString();
				StringTokenizer st = new StringTokenizer(emxTableRowIdstr, "|");
				String relId = "";
				// get the first token, the rel id.
				relId = st.nextToken();
				if (relId != null) {
					DomainRelationship domrelPCBId = new DomainRelationship(
							relId);
					StringList sList = new StringList();
					sList.add(DomainRelationship.SELECT_FROM_ID);
					String[] arr = new String[1];
					arr[0] = relId;
					MapList objMapList = DomainRelationship.getInfo(context,
							arr, sList);
					// To get the PC Id
					for (int j = 0; j < objMapList.size(); j++) {
						Map objFLMap = (Map) objMapList.get(j);
						strObjId = (String) objFLMap
								.get(DomainRelationship.SELECT_FROM_ID);
					}

				}
			}
		}
		// end fix for IR-028728V6R2011-
		String strContext = (String) requestMap.get("createContext");

		Map fieldMap = (HashMap) programMap.get("fieldMap");
		String strFieldName = (String) fieldMap.get("name");
		StringBuilder sbBuffer = new StringBuilder();

		DomainObject dstrObjId = DomainObject.newInstance(context, strObjId);
		if (dstrObjId.isKindOf(context, ProductLineConstants.TYPE_PRODUCTS)) {
			if (strObjId != null && strObjId.length() > 0) {
				setId(strObjId);
				DomainObject dProductId = DomainObject.newInstance(context,
						strObjId);

				sbBuffer.append("<input type=\"text\" readonly=\"true\" ");
				sbBuffer.append("name=\"");
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
				sbBuffer.append("Display\" id=\"\" value=\"");
				sbBuffer.append("\" />");
				sbBuffer.append("<input type=\"hidden\" name=\"");
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
				sbBuffer.append("\" value=\"");
				sbBuffer.append("\" />");
				sbBuffer.append("<input type=\"hidden\" name=\"");
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
				sbBuffer.append("OID\" value=\"");
				sbBuffer.append("\" />");
				sbBuffer.append("<input ");
				sbBuffer.append("type=\"button\" name=\"btnProductConfigurationChooser\"");
				sbBuffer.append(" size=\"200\" value=\"...\" ");
				sbBuffer.append("onClick=\"javascript:autonomySearchProductConfiguration();");
				sbBuffer.append("\" />");
				sbBuffer.append("<a href=\"javascript:basicClear('"); // IR-024327V6R2011
																		// :
																		// Added
																		// basicClear
																		// instead
																		// of
																		// clear
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
				sbBuffer.append("');\">");
				//start of IR-213179V6R2014
				/*String strClear = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
						context.getSession().getLanguage(),"emxProduct.Button.Clear");*/
				String strClear = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
						"emxProduct.Button.Clear",context.getSession().getLanguage());
				//End of IR-213179V6R2014
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strClear));
				sbBuffer.append("</a>");

				returnStr = sbBuffer.toString();
				returnStr = returnStr.replaceAll("&", "&amp;");
				return returnStr;
			}
		} else {
			if (strObjId != null && strObjId.length() > 0) {
				setId(strObjId);
				DomainObject dProductConfigurationId = DomainObject
						.newInstance(context, strObjId);

				// getting the value
				String sProductConfigurationtName = dProductConfigurationId
						.getInfo(context, DomainObject.SELECT_NAME);

				sbBuffer.append("<input type=\"text\" readonly=\"true\" ");
				sbBuffer.append("name=\"");
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
				sbBuffer.append("Display\" id=\"" + XSSUtil.encodeForHTMLAttribute(context,strObjId) + "\" value=\""
						+ XSSUtil.encodeForHTMLAttribute(context,sProductConfigurationtName)+ "\"");
				sbBuffer.append("/>");
				sbBuffer.append("<input type=\"hidden\" name=\"");
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
				sbBuffer.append("\" value=\"" + XSSUtil.encodeForHTMLAttribute(context,strObjId) + "\"");
				sbBuffer.append("/>");
				sbBuffer.append("<input type=\"hidden\" name=\"");
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
				sbBuffer.append("OID\" value=\"" + XSSUtil.encodeForHTMLAttribute(context,strObjId) + "\"");
				sbBuffer.append("/>");

				returnStr = sbBuffer.toString();
				returnStr = returnStr.replaceAll("&", "&amp;");
				return returnStr;
			}
		}
		return "";
	}

	/**
	 * Display Context Design Responsibility Name in the WebForm
	 *
	 * @param context
	 *            the Matrix Context
	 * @param args
	 *            no args needed for this method
	 * @returns String
	 * @throws Exception
	 *             if the operation fails
	 * @since PLC X4
	 */
	public Object displayContextDesignResponsibility(Context context,
			String[] args) throws Exception {
		// unpacking the Arguments from variable args
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		// fixed for Mx378056
		String strObjId = (String) requestMap.get("parentOID");
		String strContext = (String) requestMap.get("createContext");

		Map fieldMap = (HashMap) programMap.get("fieldMap");
		String strFieldName = (String) fieldMap.get("name");

		StringBuffer sbBuffer = new StringBuffer();
		String strDesignResponsibilityId = "";
		String strDesignResponsibilitytName = "";
		String returnStr = "";

		DomainObject dstrObjId = DomainObject.newInstance(context, strObjId);

		if (dstrObjId.isKindOf(context, ProductLineConstants.TYPE_PRODUCTS)
				|| dstrObjId.isKindOf(context,
						ProductLineConstants.TYPE_PRODUCT_CONFIGURATION)) {
			if (strObjId != null && strObjId.length() > 0) {
				setId(strObjId);
				if (dstrObjId.isKindOf(context,
						ProductLineConstants.TYPE_PRODUCTS)) {
					DomainObject dProductId = DomainObject.newInstance(context,
							strObjId);

					// getting the value
					strDesignResponsibilityId = dProductId
							.getInfo(
									context,
									"to["
											+ ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY
											+ "].from.id");
					strDesignResponsibilitytName = dProductId
							.getInfo(
									context,
									"to["
											+ ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY
											+ "].from.name");
				} else {
					// It is in PCContext
					DomainObject dProductConfigurationId = DomainObject
							.newInstance(context, strObjId);

					// getting the value of connected Product
					String strProductId = " ";
					strProductId = dProductConfigurationId
							.getInfo(
									context,
									"to["
											+ ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION
											+ "].from.id");
					DomainObject dProductId = DomainObject.newInstance(context,
							strProductId);

					// If Product Configuration is created in context of Feature
					if ((strProductId != null) && !(strProductId.equals(""))
							|| "Unassigned".equalsIgnoreCase(strProductId)
							|| "null".equalsIgnoreCase(strProductId)) {
						strDesignResponsibilityId = dProductId
								.getInfo(
										context,
										"to["
												+ ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY
												+ "].from.id");
						strDesignResponsibilitytName = dProductId
								.getInfo(
										context,
										"to["
												+ ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY
												+ "].from.name");
					}
				}
				if (strDesignResponsibilityId == null) {
					strDesignResponsibilityId = "";
					strDesignResponsibilitytName = "";
				}

				sbBuffer.append("<input type=\"text\" readonly=\"true\" ");
				sbBuffer.append("name=\"");
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
				sbBuffer.append("Display\" id=\"\" value=\""
						+ XSSUtil.encodeForHTMLAttribute(context,strDesignResponsibilitytName));
				sbBuffer.append("\" />");
				sbBuffer.append("<input type=\"hidden\" name=\"");
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
				sbBuffer.append("\" value=\"" + XSSUtil.encodeForHTMLAttribute(context,strDesignResponsibilityId));
				sbBuffer.append("\" />");
				sbBuffer.append("<input type=\"hidden\" name=\"");
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
				sbBuffer.append("OID\" value=\"" + XSSUtil.encodeForHTMLAttribute(context,strDesignResponsibilityId));
				sbBuffer.append("\" />");
				sbBuffer.append("<input ");
				sbBuffer.append("type=\"button\" name=\"btnDesignResponsibilityChooser\"");
				sbBuffer.append(" size=\"200\" value=\"...\" ");
				sbBuffer.append("onClick=\"javascript:autonomySearchDesignResponsibility();");
				sbBuffer.append("\" />");
				sbBuffer.append("<a href=\"javascript:ClearDesignResponsibility('");
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
				sbBuffer.append("');\">");

				String strClear = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
						"emxProduct.Button.Clear",context.getSession().getLanguage());
				sbBuffer.append(strClear);
				sbBuffer.append("</a>");

				returnStr = sbBuffer.toString();
				returnStr = returnStr.replaceAll("&", "&amp;");
				return returnStr;
			}
		}
		returnStr = sbBuffer.toString();
		returnStr = returnStr.replaceAll("&", "&amp;");
		return returnStr;
	}

	/**
	 * Connects Product to the build
	 *
	 * @param context
	 *            the Matrix Context
	 * @param args
	 *            no args needed for this method
	 * @returns void
	 * @throws Exception
	 *             if the operation fails
	 * @since PLC X4
	 */
	public void connectToProduct(Context context, String[] args)
			throws Exception {
		// unpacking the Arguments from variable args
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		HashMap requestMap = (HashMap) programMap.get("requestMap");

		String[] strProductOID = (String[]) requestMap.get("ProductOID");
		String strProductID = strProductOID[0];
		String strbuildId = (String) paramMap.get("objectId");

		DomainObject domBuild = new DomainObject(strbuildId);

		if ((strProductID != null) && !(strProductID.equals(""))
				|| "Unassigned".equalsIgnoreCase(strProductID)
				|| "null".equalsIgnoreCase(strProductID)) {
			DomainObject domProduct = new DomainObject(strProductID);
			setId(strProductID);
			ContextUtil.pushContext(context);
			DomainRelationship.connect(context, this,
					ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD, domBuild);
			ContextUtil.popContext(context);
		}
	}

	/**
	 * Connects Product Configuration to the build
	 *
	 * @param context
	 *            the Matrix Context
	 * @param args
	 *            no args needed for this method
	 * @returns void
	 * @throws Exception
	 *             if the operation fails
	 * @since PLC X4
	 */
	public void connectToProductConfiguration(Context context, String[] args)
			throws Exception {
		// unpacking the Arguments from variable args
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		HashMap requestMap = (HashMap) programMap.get("requestMap");

		String[] strProductConfigurationOID = (String[]) requestMap
				.get("ProductConfigurationOID");

		if ((strProductConfigurationOID != null)
				&& !(strProductConfigurationOID.equals(""))
				|| "null".equals(strProductConfigurationOID)) {

			String strProductConfigurationID = strProductConfigurationOID[0];
			String strbuildIdId = (String) paramMap.get("objectId");
			DomainObject domBuild = new DomainObject(strbuildIdId);
			if ((strProductConfigurationID != null)
					&& !(strProductConfigurationID.equals(""))
					|| "Unassigned".equalsIgnoreCase(strProductConfigurationID)
					|| "null".equalsIgnoreCase(strProductConfigurationID)) {
				DomainObject domProductConfiguration = new DomainObject(
						strProductConfigurationID);
				DomainRelationship domRel = DomainRelationship
						.connect(
								context,
								domProductConfiguration,
								ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION_BUILD,
								domBuild);
			}
		}

	}

	/**
	 * Connects Design Responsibility to the build
	 *
	 * @param context
	 *            the Matrix Context
	 * @param args
	 *            no args needed for this method
	 * @returns void
	 * @throws Exception
	 *             if the operation fails
	 * @since PLC X4
	 */
	public void connectToDesignResponsibility(Context context, String[] args)
			throws Exception {
		// unpacking the Arguments from variable args
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");

		String strDesignResponsibilityId = (String) paramMap.get("New OID");
		if ((strDesignResponsibilityId == null)
				|| (strDesignResponsibilityId.equals("")))
			strDesignResponsibilityId = (String) paramMap.get("New Value");
		String strbuildIdId = (String) paramMap.get("objectId");

		if ((strDesignResponsibilityId != null)
				&& !(strDesignResponsibilityId.equals(""))
				|| "Unassigned".equalsIgnoreCase(strDesignResponsibilityId)
				|| "null".equalsIgnoreCase(strDesignResponsibilityId)) {
			DomainObject domDesignResponsibility = new DomainObject(
					strDesignResponsibilityId);
			DomainObject domBuild = new DomainObject(strbuildIdId);
			DomainRelationship domRel = DomainRelationship.connect(context,
					domDesignResponsibility,
					ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY,
					domBuild);
		}
	}

	/**
	 * Connects Plant to build
	 *
	 * @param context
	 *            the Matrix Context
	 * @param args
	 *            no args needed for this method
	 * @returns void
	 * @throws Exception
	 *             if the operation fails
	 * @since PLC X4
	 */
	public void connectToPlant(Context context, String[] args) throws Exception {
		try {
			ContextUtil
					.pushContext(context, PropertyUtil.getSchemaProperty(
							context, "person_UserAgent"),
							DomainConstants.EMPTY_STRING,
							DomainConstants.EMPTY_STRING);
			// unpacking the Arguments from variable args
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");

			String strPlantId = (String) paramMap.get("New OID");
			String strbuildIdId = (String) paramMap.get("objectId");

			if ((strPlantId != null) && !(strPlantId.equals(""))
					|| "Unassigned".equalsIgnoreCase(strPlantId)
					|| "null".equalsIgnoreCase(strPlantId)) {
				DomainObject domPlant = new DomainObject(strPlantId);
				DomainObject domBuild = new DomainObject(strbuildIdId);
				DomainRelationship domRel = DomainRelationship.connect(context,
						domBuild,
						ProductLineConstants.RELATIONSHIP_MANUFACTURING_PLANT,
						domPlant);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Set the context back to the context user
			ContextUtil.popContext(context);
		}
	}

	/**
	 * Connects Customer to the build
	 *
	 * @param context
	 *            the Matrix Context
	 * @param args
	 *            no args needed for this method
	 * @returns void
	 * @throws Exception
	 *             if the operation fails
	 * @since PLC X4
	 */
	public void connectToCustomer(Context context, String[] args)
			throws Exception {
		try {
			ContextUtil
					.pushContext(context, PropertyUtil.getSchemaProperty(
							context, "person_UserAgent"),
							DomainConstants.EMPTY_STRING,
							DomainConstants.EMPTY_STRING);
			// unpacking the Arguments from variable args
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");

			String strCustomerId = (String) paramMap.get("New OID");
			String strbuildIdId = (String) paramMap.get("objectId");

			if ((strCustomerId != null) && !(strCustomerId.equals(""))
					|| "Unassigned".equalsIgnoreCase(strCustomerId)
					|| "null".equalsIgnoreCase(strCustomerId)) {
				DomainObject domCompany = new DomainObject(strCustomerId);
				DomainObject domBuild = new DomainObject(strbuildIdId);
				DomainRelationship domRel = DomainRelationship.connect(context,
						domCompany,
						ProductLineConstants.RELATIONSHIP_CUSTOMER_UNIT,
						domBuild);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Set the context back to the context user
			ContextUtil.popContext(context);
		}
	}

	/**
	 * Added for Bug 356766 - To make creation of Multiple Builds a background
	 * job
	 *
	 * @param context
	 *            the Matrix Context
	 * @param args
	 * @throws Exception
	 *             if the operation fails
	 */
	public void createMultipleBuildsUsingBgpJob(Context context, String[] args)
			throws Exception {
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String noofbuilds = (String) requestMap.get("noofbuilds");
			String strVaultName = (String) requestMap.get("Vault");
			String strPolicy = (String) requestMap.get("Policy");
			// Added for X+5 build on parts 08/10/08
			boolean isUNTInstalled = FrameworkUtil.isSuiteRegistered(context,
					"appInstallTypeUnitTracking", false, null, null);
			String createMode = (String) requestMap.get("createMode");
			String partId = (String) requestMap.get("objectId");
			// End X+5
			String strProductID = (String) requestMap.get("ProductOID");
			String strProductConfigurationID = (String) requestMap
					.get("ProductConfigurationOID");

			String strDesignResponsibilityId = (String) requestMap
					.get("DesignResponsibilityOID");
			String strCustomerID = (String) requestMap.get("CustomerOID");
			String strPlantID = (String) requestMap.get("PlantOID");

			// Retrieve the attribute values
			String strActualBuildDate = (String) requestMap
					.get("ActualBuildDate");
			String strPlannedBuildDate = (String) requestMap
					.get("PlannedBuildDate");
			String strDateShipped = (String) requestMap.get("DateShipped");
			String strBuildDescription = (String) requestMap.get("Description");
			String strModelContext = (String) requestMap.get("modelContext");

			// set the attributeMap for the build Object
			HashMap attributeMap = new HashMap();
			attributeMap.put(ProductLineConstants.ATTRIBUTE_ACTUAL_BUILD_DATE,
					strActualBuildDate);
			attributeMap.put(ProductLineConstants.ATTRIBUTE_PLANNED_BUILD_DATE,
					strPlannedBuildDate);
			attributeMap.put(ProductLineConstants.ATTRIBUTE_DATE_SHIPPED,
					strDateShipped);

			if (isUNTInstalled) {
				String strBuildDisposition = (String) requestMap
						.get("BuildDisposition");
				String strContractNumber = (String) requestMap
						.get("ContractNumber");
				attributeMap.put(
						ProductLineConstants.ATTRIBUTE_BUILD_DISPOSITION,
						strBuildDisposition);
				attributeMap.put(
						ProductLineConstants.ATTRIBUTE_CONTRACT_NUMBER,
						strContractNumber);
			}
			String strRevision = "";
			if (noofbuilds == null || "".equals(noofbuilds)) {
				noofbuilds = "1";
			}
			int iNoOfBuilds = Integer.parseInt(noofbuilds);
			String strBuildType = (String) requestMap.get("TypeActual");

			ProductLineCommon createBuild = new ProductLineCommon();

			String strbuildId = null;
			StringList buildList = new StringList();
			for (int iSize = 0; iSize < iNoOfBuilds - 1; iSize++) {
				strbuildId = (String) createBuild.create(context, strBuildType,
						"", strRevision, strBuildDescription, strPolicy,
						strVaultName, attributeMap, "", "", "", false);
				buildList.addElement(strbuildId);
			}

			int iBuildCount = buildList.size();

			// Array initialized to hold the object ids stored in the
			// StringList.
			String[] arrBuildId = new String[iBuildCount];

			for (int k = 0; k < iBuildCount; k++) {
				arrBuildId[k] = (String) buildList.elementAt(k);
			}
			ContextUtil
					.pushContext(context, PropertyUtil.getSchemaProperty(
							context, "person_UserAgent"),
							DomainConstants.EMPTY_STRING,
							DomainConstants.EMPTY_STRING);
			// Added for X+5 build on parts 08/10/08
			if (isUNTInstalled && "connectToPart".equals(createMode)
					&& partId != null && !"".equals(partId)) {
				DomainObject domPartID = new DomainObject(partId);
				DomainRelationship.connect(context, domPartID,
						ProductLineConstants.RELATIONSHIP_ASSIGNED_PART, false,
						arrBuildId);
			}
			// End for X+5
			if ((strProductID != null) && !(strProductID.equals(""))
					|| "Unassigned".equalsIgnoreCase(strProductID)
					|| "null".equalsIgnoreCase(strProductID)) {
				DomainObject domProductID = new DomainObject(strProductID);
				DomainRelationship.connect(context, domProductID,
						ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD, true,
						arrBuildId);
			}

			if ((strProductConfigurationID != null)
					&& !(strProductConfigurationID.equals(""))
					|| "Unassigned".equalsIgnoreCase(strProductConfigurationID)
					|| "null".equalsIgnoreCase(strProductConfigurationID)) {
				DomainObject domProductConfigurationID = new DomainObject(
						strProductConfigurationID);
				DomainRelationship
						.connect(
								context,
								domProductConfigurationID,
								ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION_BUILD,
								true, arrBuildId);
			}

			if ((strDesignResponsibilityId != null)
					&& !(strDesignResponsibilityId.equals(""))
					|| "Unassigned".equalsIgnoreCase(strDesignResponsibilityId)
					|| "null".equalsIgnoreCase(strDesignResponsibilityId)) {
				DomainObject domDesignResponsibilityId = new DomainObject(
						strDesignResponsibilityId);
				DomainRelationship
						.connect(
								context,
								domDesignResponsibilityId,
								ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY,
								true, arrBuildId);
			}

			if ((strCustomerID != null) && !(strCustomerID.equals(""))
					|| "Unassigned".equalsIgnoreCase(strCustomerID)
					|| "null".equalsIgnoreCase(strCustomerID)) {
				DomainObject domCustomerID = new DomainObject(strCustomerID);
				DomainRelationship.connect(context, domCustomerID,
						ProductLineConstants.RELATIONSHIP_CUSTOMER_UNIT, true,
						arrBuildId);
			}

			if ((strPlantID != null) && !(strPlantID.equals(""))
					|| "Unassigned".equalsIgnoreCase(strPlantID)
					|| "null".equalsIgnoreCase(strPlantID)) {
				DomainObject domPlantID = new DomainObject(strPlantID);
				DomainRelationship.connect(context, domPlantID,
						ProductLineConstants.RELATIONSHIP_MANUFACTURING_PLANT,
						false, arrBuildId);
			}
			// Added for X+5 Feature - Unit Tracking and Allocation - Start
			if ((strModelContext != null) && (strModelContext.equals("true"))) {
				String strModelId = "";
				strModelId = (String) requestMap.get("parentOID");
				if (strModelId != null && strModelId.length() > 0) {
					DomainObject domModel = new DomainObject(strModelId);
					DomainRelationship.connect(context, domModel,
							ProductLineConstants.RELATIONSHIP_MODEL_BUILD,
							true, arrBuildId);
				}
			}
			// Added for X+5 Feature - Unit Tracking and Allocation - End

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Set the context back to the context user
			ContextUtil.popContext(context);
		}
	}

	/**
	 * Creates build
	 *
	 * @param context
	 *            the Matrix Context
	 * @param args
	 *            no args needed for this method
	 * @returns void
	 * @throws Exception
	 *             if the operation fails
	 * @since PLC X4
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public StringList createMultipleBuilds(Context context, String[] args)
			throws Exception {
		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);programMap.get("objectid");
			HashMap paramMap = (HashMap)programMap.get("paramMap");
			HashMap requestMap = (HashMap)programMap.get("requestMap");
			String objectId = (String)paramMap.get("objectId");
			requestMap.put("newObjectId", objectId);
			StringList buildIds = Build.createMultipleBuilds(context, requestMap);
			return buildIds;
		} catch (Exception e) {
			e.printStackTrace();
			throw (new FrameworkException(e));
		}
	}

	/**
	 * Connects build with Customer.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the HashMap containing the following arguments
	 *            paramMap - contains ObjectId, Old Value for product name and
	 *            new value
	 * @return int - returns zero if connect successful
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductLine X+4
	 */

	public int updateCustomer(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String strBuildId = (String) paramMap.get("objectId");

		String strOldProductName = (String) paramMap.get("Old value");
		// Read from OID. If OID not available read from Value
		String strNewCustomerId = (String) paramMap.get("New OID");
		if ((strNewCustomerId == null) || (strNewCustomerId.equals("")))
			strNewCustomerId = (String) paramMap.get("New Value");

		String strCustomerUnitRelationship = ProductLineConstants.RELATIONSHIP_CUSTOMER_UNIT;

		setId(strBuildId);
		DomainObject domainObjectToType = newInstance(context, strBuildId);
		if (strOldProductName != null && !strOldProductName.equals("")) {
			String strRelId = domainObjectToType.getInfo(context, "to["
					+ strCustomerUnitRelationship + "].id");
			// Disconnecting the existing relationship
			DomainRelationship.disconnect(context, strRelId);
		}
		setId(strNewCustomerId);
		if (!strNewCustomerId.equals("")) {
			DomainRelationship.connect(context, this,
					strCustomerUnitRelationship, domainObjectToType);
		}

		return 0;
	}

	/**
	 * Connects build with Manufacturing Plant.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the HashMap containing the following arguments
	 *            paramMap - contains ObjectId, Old Value for product name and
	 *            new value
	 * @return int - returns zero if connect successful
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductLine X+4
	 */

	public int updateManufacturingPlant(Context context, String[] args)
			throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String strBuildId = (String) paramMap.get("objectId");

		Build build = new Build(strBuildId);
		StringList objectSelects = new StringList();
		objectSelects.addElement("current.access[modify]");
		objectSelects.addElement("from["+ProductLineConstants.RELATIONSHIP_MANUFACTURING_PLANT+"].id");

		Map buildMap = build.getInfo(context, objectSelects);
		String hasEditAccess = 	(String) buildMap.get("current.access[modify]");
		String existingRelId = (String) buildMap.get("from["+ProductLineConstants.RELATIONSHIP_MANUFACTURING_PLANT+"].id");
		if("true".equalsIgnoreCase(hasEditAccess)){
			try {
				ContextUtil
					.pushContext(context, PropertyUtil.getSchemaProperty(
						context, "person_UserAgent"),
						DomainConstants.EMPTY_STRING,
						DomainConstants.EMPTY_STRING);

				String strNewPlantId = (String) paramMap.get("New OID");
				//SB sends the value as New Value, Form sends it as New OID. This method is used for Build Form and SB
				if(strNewPlantId==null || "".equals(strNewPlantId)){
					strNewPlantId = (String) paramMap.get("New Value");
				}

				if (existingRelId != null && existingRelId.length() > 0) {
					DomainRelationship.disconnect(context, existingRelId);
				}
				if (strNewPlantId != null && strNewPlantId.length() > 0) {
					DomainRelationship.connect(context, build,
							ProductLineConstants.RELATIONSHIP_MANUFACTURING_PLANT, new DomainObject(strNewPlantId));
				}
			} catch (FrameworkException e) {
				//e.printStackTrace();
				throw e;
			} finally {
				ContextUtil.popContext(context);
			}
			return 0;
		}else{
			throw new FrameworkException(UINavigatorUtil
					.getI18nString(
							"emxProduct.Alert.PlantEditBeyondCompleteState",
							"emxProductLineStringResource",
							context.getSession().getLanguage()));
		}
	}

	/**
	 * Connects build with Product Configuration
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the HashMap containing the following arguments
	 *            paramMap - contains ObjectId, Old Value for product name and
	 *            new value
	 * @return int - returns zero if connect successful
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductLine X+4
	 */

	public int updateProductConfiguration(Context context, String[] args)
			throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String strBuildId = (String) paramMap.get("objectId");

		String strOldProductConfiguraionName = (String) paramMap.get("Old OID");

		String strNewProductConfiguraionId = (String) paramMap.get("New OID");

		String strProductConfigurationRelationship = ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION_BUILD;

		setId(strBuildId);
		DomainObject domainObjectToType = newInstance(context, strBuildId);
		if ((strOldProductConfiguraionName != null)
				&& !(strOldProductConfiguraionName.equals(""))
				|| "null".equals(strOldProductConfiguraionName))
		// if (!strOldProductConfiguraionName.equals("") )
		{
			String strRelId = domainObjectToType.getInfo(context, "to["
					+ strProductConfigurationRelationship + "].id");

			// Added if condition on 16th Jun 2008, to fix bug 355025
			if (strRelId != null && !"".equals(strRelId)) {
				// Disconnecting the existing relationship
				DomainRelationship.disconnect(context, strRelId);
			}
		}
		setId(strNewProductConfiguraionId);
		if (!strNewProductConfiguraionId.equals("")) {
			DomainRelationship.connect(context, this,
					strProductConfigurationRelationship, domainObjectToType);
		}

		return 0;
	}

	/**
	 * Connects build with Product
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the HashMap containing the following arguments
	 *            paramMap - contains ObjectId, Old Value for product name and
	 *            new value
	 * @return int - returns zero if connect successful
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductLine X+4
	 */

	public int updateProduct(Context context, String[] args) throws Exception {
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String strBuildId = (String) paramMap.get("objectId");

			String strOldProductName = (String) paramMap.get("Old value");

			String strNewProductId = (String) paramMap.get("New OID");

			String strProductBuildRelationship = ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD;
			// Made Changes to fix Bug #356851
			String strOldModelId = "";
			String strNewModelId = "";
			String strOldShorthandNotation = "";
			String strOldUnitNumber = "";

			String strDefaultUnitNumber = "";
			String strDefaultShorthandNotation = "";

			setId(strBuildId);
			DomainObject domainObjectToType = newInstance(context, strBuildId);
			if (!strOldProductName.equals("")) {
				String strRelId = domainObjectToType.getInfo(context, "to["
						+ strProductBuildRelationship + "].id");
				if (strRelId != null) {
					// Code added on 20th Jun 2008, To fix Bug 355815 - Starts
					String[] relIdarr = new String[1];
					relIdarr[0] = strRelId;
					StringList relSelects = new StringList();
					relSelects.add(DomainRelationship.SELECT_FROM_ID);
					MapList objMapList = DomainRelationship.getInfo(context,
							relIdarr, relSelects);
					if (!objMapList.isEmpty()) {
						Map objProductMap = (Map) objMapList.get(0);
						String strOldProductId = (String) objProductMap
								.get(DomainRelationship.SELECT_FROM_ID);

						// Chanages done on 3rd July 2008, to fix 356862 -
						// Starts
						DomainObject domainObject = new DomainObject(
								strOldProductId);

						StringList sList = new StringList(1);
						sList.addElement(DomainConstants.SELECT_ID);
						StringList selectRelStmts = new StringList(1);
						MapList mListRelatedData = domainObject
								.getRelatedObjects(context, "", PropertyUtil
										.getSchemaProperty(context,
												"type_PUEECO"), sList,
										selectRelStmts, true, false, (short) 0,
										"", "");

						if (mListRelatedData != null
								&& !mListRelatedData.isEmpty()) {
							String strAlertMessage = UINavigatorUtil
									.getI18nString(
											"emxProduct.Confirm.ProductPUEECO.Clear",
											"emxProductLineStringResource",
											context.getSession().getLanguage());
							throw new FrameworkException(strAlertMessage);
						}
						// Made Changes to fix Bug #356851
						if (strOldProductId != null
								&& !strOldProductId.equals("null")
								&& !strOldProductId.equals("")) {
							DomainObject objOldProduct = newInstance(context,
									strOldProductId);
							strOldModelId = objOldProduct
									.getInfo(
											context,
											"to["
													+ ProductLineConstants.RELATIONSHIP_PRODUCTS
													+ "].from.id");
							if(strOldModelId == null || "".equals(strOldModelId)){
								strOldModelId = objOldProduct
								.getInfo(context,
										"to[" + ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT
										+ "].from.id");
							}
						}
						// Chanages done on 3rd July 2009, to fix 356862 - Ends
						if (strNewProductId != null && strOldProductId != null
								&& strNewProductId.equals(strOldProductId)) {
							return 0;
						}
					}
				}
				// Code added on 20th Jun 2008, To fix Bug 355815 - Ends
				if (strRelId != null && !strRelId.equals("")) {
					// Disconnecting the existing relationship
					DomainRelationship.disconnect(context, strRelId);
				}
			}

			if (strNewProductId != null && !strNewProductId.equals("null")
					&& !strNewProductId.equals("")) {
				setId(strNewProductId);
				// Made Changes to fix Bug #356851
				strNewModelId = this.getInfo(context, "to["
						+ ProductLineConstants.RELATIONSHIP_PRODUCTS
						+ "].from.id");
				if(strNewModelId == null || "".equals(strNewModelId)){
					strNewModelId = this.getInfo(context, "to["
							+ ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT
							+ "].from.id");
				}

				strOldUnitNumber = domainObjectToType.getAttributeValue(
						context,
						ProductLineConstants.ATTRIBUTE_BUILD_UNIT_NUMBER);
				strOldShorthandNotation = domainObjectToType.getAttributeValue(
						context, ProductLineConstants.ATTRIBUTE_PREFIX);

				DomainRelationship.connect(context, this,
						strProductBuildRelationship, domainObjectToType);

				// Made Changes to fix Bug #356851
				if (strOldModelId != null && !strOldModelId.equals("null")
						&& strNewModelId != null
						&& !strNewModelId.equals("null")
						&& strOldModelId.equals(strNewModelId)) {
					HashMap objAttribMap = new HashMap(2);
					objAttribMap.put(
							ProductLineConstants.ATTRIBUTE_BUILD_UNIT_NUMBER,
							strOldUnitNumber);
					objAttribMap.put(ProductLineConstants.ATTRIBUTE_PREFIX,
							strOldShorthandNotation);
					domainObjectToType
							.setAttributeValues(context, objAttribMap);
				}
			} else {
				// If condition added for IR-019598
				if (!isModelBuild(context, domainObjectToType)) {
					HashMap objAttribMap = new HashMap(2);
					objAttribMap.put(
							ProductLineConstants.ATTRIBUTE_BUILD_UNIT_NUMBER,
							strDefaultUnitNumber);

					ContextUtil.pushContext(context, PropertyUtil
							.getSchemaProperty(context, "person_UserAgent"),
							DomainConstants.EMPTY_STRING,
							DomainConstants.EMPTY_STRING);
					objAttribMap.put(ProductLineConstants.ATTRIBUTE_PREFIX,
							strDefaultShorthandNotation);
					domainObjectToType
							.setAttributeValues(context, objAttribMap);
					ContextUtil.popContext(context);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	// This method is added as part of fix for IR-019598
	// This method checks for the UNT installationa and model build.
	// If its a model build then Unit number has to be retained
	private boolean isModelBuild(Context context, DomainObject domObj)
			throws Exception {
		if (isUNTInstalled(context, null)) {
			String strModel = domObj.getInfo(context, "to["
					+ ProductLineConstants.RELATIONSHIP_MODEL_BUILD
					+ "].from.id");
			if (strModel != null && strModel.length() > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Connects build with Assigned Part
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the HashMap containing the following arguments
	 *            paramMap - contains ObjectId, Old Value for product name and
	 *            new value
	 * @return int - returns zero if connect successful
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductLine X+4
	 */

	public int updateAssignedPart(Context context, String[] args)
			throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String strBuildId = (String) paramMap.get("objectId");

		String strOldAssignedPartName = (String) paramMap.get("Old value");
		// Read from OID. If OID not available read from Value
		String strNewAssignedPartId = (String) paramMap.get("New OID");
		if ((strNewAssignedPartId == null) || (strNewAssignedPartId.equals("")))
			strNewAssignedPartId = (String) paramMap.get("New Value");

		String strAssignedPartRelationship = ProductLineConstants.RELATIONSHIP_ASSIGNED_PART;

		setId(strBuildId);
		DomainObject domainObjectToType = newInstance(context, strBuildId);
		if (!strOldAssignedPartName.equals("")) {
			String strRelId = domainObjectToType.getInfo(context, "from["
					+ strAssignedPartRelationship + "].id");
			// Disconnecting the existing relationship
			DomainRelationship.disconnect(context, strRelId);
		}
		setId(strNewAssignedPartId);
		if (!strNewAssignedPartId.equals("")) {
			DomainRelationship.connect(context, domainObjectToType,
					strAssignedPartRelationship, this);
		}

		return 0;
	}

	/**
	 * Connects build with Design Responsibility
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the HashMap containing the following arguments
	 *            paramMap - contains ObjectId, Old Value for product name and
	 *            new value
	 * @return int - returns zero if connect successful
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductLine X+4
	 */

	public int updateDesignResponsibility(Context context, String[] args)
			throws Exception {
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String strBuildId = (String) paramMap.get("objectId");

			// String strOldDesignResponsibilityName =
			// (String)paramMap.get("Old value");

			String strOldDesignResponsibilityName = (String) paramMap
					.get("Old OID");
			// String strNewDesignResponsibilityId =
			// (String)paramMap.get("New Value");

			String strNewDesignResponsibilityId = (String) paramMap
					.get("New OID");

			String strDesignResponsibilityRelationship = ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY;

			setId(strBuildId);
			DomainObject domainObjectToType = newInstance(context, strBuildId);
			strOldDesignResponsibilityName = domainObjectToType
					.getInfo(
							context,
							"to["
									+ ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY
									+ "].from.name");
			String strRelId = null;
			if (strOldDesignResponsibilityName != null) {
				if ((!strOldDesignResponsibilityName.equals(""))) {
					strRelId = domainObjectToType.getInfo(context, "to["
							+ strDesignResponsibilityRelationship + "].id");
					// Disconnecting the existing relationship
					DomainRelationship.disconnect(context, strRelId);
				}
			}
			setId(strNewDesignResponsibilityId);
			if (!strNewDesignResponsibilityId.equals("")) {
				DomainRelationship
						.connect(context, this,
								strDesignResponsibilityRelationship,
								domainObjectToType);
			}

			if (strNewDesignResponsibilityId != null
					&& strNewDesignResponsibilityId.equals("")
					&& strOldDesignResponsibilityName != null
					&& !strOldDesignResponsibilityName.equals("")) {
				// String strRelId =
				// domainObjectToType.getInfo(context,"to["+strDesignResponsibilityRelationship+"].id");
				// Disconnecting the existing relationship
				// DomainRelationship.disconnect(context, strRelId);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	/**
	 * Method to get only the Product Configurations based on the selected
	 * Product Shows all the Product Configurations if the Product is not
	 * selected.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the HashMap containing the following arguments
	 *            paramMap - contains ObjectId, Old Value for product name and
	 *            new value
	 * @return StringList - returns List of ObjectIDs which should be Excluded
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductLine X+4
	 */

	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList filterRelatedProductConfiguration(Context context,
			String[] args) throws Exception {
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strProductId = (String) programMap.get("objectId");
			StringList tempStrList = new StringList();
			String strBuildId = (String) programMap.get("buildId");
			String strModelId = (String) programMap.get("modelOID");
			String strOperation = (String) programMap.get("operation");
			if (strProductId != null && !"".equalsIgnoreCase(strProductId)) {
				DomainObject domProductId = new DomainObject(strProductId);
				StringList strProductConfIds = domProductId
						.getInfoList(
								context,
								"from["
										+ ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION
										+ "].to.id");

				// get all the Product Configuration in the entire Database
				StringList objSelect = new StringList(2);
				objSelect.addElement(DomainConstants.SELECT_ID);
				MapList lstPCList = DomainObject.findObjects(context,
						ProductLineConstants.TYPE_PRODUCT_CONFIGURATION,
						DomainConstants.QUERY_WILDCARD, "", objSelect);

				String strId = "";

				// Iterate the All the product configuration in database and
				// exclude the ones which are not connected to
				// the context product
				for (int i = 0; i < lstPCList.size(); i++) {
					Map productConfigMap = (Map) lstPCList.get(i);
					strId = productConfigMap.get("id").toString();

					if (!strProductConfIds.contains(strId)) {
						tempStrList.addElement(strId);
					}
				}
				return tempStrList;
			} else if (strModelId != null && strModelId.length() > 0) {
				tempStrList = getModelContextProductConfigurations(context,
						strModelId);

				return tempStrList;
			} else {
				StringList objSelect = new StringList(2);
				objSelect.addElement(DomainConstants.SELECT_ID);
				objSelect
						.addElement("to["
								+ ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION
								+ "].from.type");

				// get all the Product Configuration in the entire Database
				MapList lstPCList = DomainObject.findObjects(context,
						ProductLineConstants.TYPE_PRODUCT_CONFIGURATION,
						DomainConstants.QUERY_WILDCARD, "", objSelect);
				String strId = "";
				String strPCType = "";

				for (int i = 0; i < lstPCList.size(); i++) {
					Map productConfigMap = (Map) lstPCList.get(i);
					strPCType = (String) productConfigMap
							.get("to["
									+ ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION
									+ "].from.type");

					// if(strPCType!=null && !"".equalsIgnoreCase(strPCType) &&
					// strPCType.equalsIgnoreCase("null"))
					if ((strPCType != null) && !(strPCType.equals(""))
							|| "Unassigned".equalsIgnoreCase(strPCType)
							|| "null".equalsIgnoreCase(strPCType)) {
						if (!mxType.isOfParentType(context, strPCType,
								ProductLineConstants.TYPE_PRODUCTS)) {
							String strObjId = (String) productConfigMap
									.get(DomainConstants.SELECT_ID);
							HashMap objMap = new HashMap(1);
							objMap.put(DomainConstants.SELECT_ID, strObjId);
							tempStrList.add(strObjId);
						}
					}
				}
				if (isUNTInstalled(context, args)) {
					if (strBuildId != null && strBuildId.length() > 0) {
						DomainObject obj = new DomainObject(strBuildId);
						StringList selectables = new StringList();
						String strModelBuildSelectable = "to["
								+ ProductLineConstants.RELATIONSHIP_MODEL_BUILD
								+ "].from.id";
						String strProductBuildSelectable = "to["
								+ ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
								+ "].from.id";
						selectables.add(strModelBuildSelectable);
						selectables.add(strProductBuildSelectable);
						Map list = obj.getInfo(context, selectables);
						String strBuildModelId = (String) list
								.get(strModelBuildSelectable);
						if (strBuildModelId != null
								&& strBuildModelId.length() > 0) {
							if (strOperation != null
									&& strOperation.equalsIgnoreCase("AsBuilt")) {
								tempStrList = getModelContextProductConfigurationsForAsBuilt(
										context, strBuildModelId);
							} else {
								tempStrList = getModelContextProductConfigurations(
										context, strBuildModelId);
								return tempStrList;
							}
						}
					}
					tempStrList.addAll(checkForIPUAndGetRelatedProdConfId(
							context, strBuildId));
					// Following if condition added for IR-050574
					if (strOperation != null
							&& strOperation.equalsIgnoreCase("AsBuilt")) {
						StringList lst = getFirstLevelProdConfList(context,
								strBuildId);
						tempStrList.removeAll(lst);
					}
				}
				return tempStrList;
			}
		} catch (Exception e) {
			throw new FrameworkException(e);
		}
	}

	// Below 2 methods added for IR-032817
	private StringList getFirstLevelProdConfList(Context context,
			String strBuildId) throws Exception {
		StringList listProdConfIds = new StringList();
		StringList listProdIds = getFirstLevelProdList(context, strBuildId);
		for (int i = 0; i < listProdIds.size(); i++) {
			String objid = (String) listProdIds.get(i);
			DomainObject tempObj = new DomainObject(objid);
			StringList list = tempObj.getInfoList(context, "from["
					+ ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION
					+ "].to.id");
			if (list != null && list.size() > 0) {
				listProdConfIds.addAll(list);
			}
		}
		return listProdConfIds;

	}

	private StringList getModelContextProductConfigurationsForAsBuilt(
			Context context, String strModelId) throws Exception,
			FrameworkException {
		StringList tempStrList = new StringList();

		StringList objectSelects =new StringList(DomainConstants.SELECT_ID);
		StringList relSelects =new StringList();

		DomainObject domModel = new DomainObject(strModelId);

		MapList relatedProducts = domModel.getRelatedObjects(context , ProductLineConstants.RELATIONSHIP_PRODUCTS,
				ProductLineConstants.TYPE_PRODUCTS, objectSelects, relSelects,false,true, (short) 1, "", "", 0);

		StringList strProductIds = new StringList();
		Map productMap = null;
		for(int i =0; i<relatedProducts.size(); i++){
			productMap = (Map)relatedProducts.get(i);
			strProductIds.add((String) productMap.get(DomainConstants.SELECT_ID));
		}
		// get all the Product Configuration in the entire Database
		StringList objSelect = new StringList(2);
		objSelect.addElement(DomainConstants.SELECT_ID);
		StringList strProductConfIds = new StringList();
		for (int i = 0; i < strProductIds.size(); i++) {

			DomainObject domProductId = new DomainObject(
					(String) strProductIds.get(i));
			StringList strProductConfIdsTemp = domProductId
					.getInfoList(
							context,
							"from["
									+ ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION
									+ "].to.id");
			strProductConfIds.addAll(strProductConfIdsTemp);
		}

		return strProductConfIds;
	}

	private StringList getModelContextProductConfigurations(Context context,
			String strModelId) throws Exception, FrameworkException {
		StringList tempStrList = new StringList();

		StringList objectSelects =new StringList(DomainConstants.SELECT_ID);
		StringList relSelects =new StringList();

		DomainObject domModel = new DomainObject(strModelId);

		MapList relatedProducts = domModel.getRelatedObjects(context , ProductLineConstants.RELATIONSHIP_PRODUCTS,
				ProductLineConstants.TYPE_PRODUCTS, objectSelects, relSelects,false,true, (short) 1, "", "", 0);

		StringList strProductIds = new StringList();
		Map productMap = null;
		for(int i =0; i<relatedProducts.size(); i++){
			productMap = (Map)relatedProducts.get(i);
			strProductIds.add((String) productMap.get(DomainConstants.SELECT_ID));
		}

		// get all the Product Configuration in the entire Database
		StringList objSelect = new StringList(2);
		objSelect.addElement(DomainConstants.SELECT_ID);
		MapList lstPCList = DomainObject.findObjects(context,
				ProductLineConstants.TYPE_PRODUCT_CONFIGURATION,
				DomainConstants.QUERY_WILDCARD, "", objSelect);
		StringList strProductConfIds = new StringList();
		String strId = "";
		for (int i = 0; i < strProductIds.size(); i++) {

			DomainObject domProductId = new DomainObject(
					(String) strProductIds.get(i));
			StringList strProductConfIdsTemp = domProductId
					.getInfoList(
							context,
							"from["
									+ ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION
									+ "].to.id");
			strProductConfIds.addAll(strProductConfIdsTemp);
		}

		for (int j = 0; j < lstPCList.size(); j++) {
			Map productConfigMap = (Map) lstPCList.get(j);
			strId = productConfigMap.get("id").toString();

			if (!strProductConfIds.contains(strId)) {
				tempStrList.addElement(strId);
			}
		}
		return tempStrList;
	}

	/**
	 * Method to get only the Product based on the selected Product
	 * Configuration Shows all the Product if the Product Configuration is not
	 * selected.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the HashMap containing the following arguments
	 *            paramMap - contains ObjectId, Old Value for product name and
	 *            new value
	 * @return StringList - returns List of ObjectIDs which should be Excluded
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductLine X+4
	 */

	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList filterRelatedProduct(Context context, String[] args)
			throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strProductConfigurationId = (String) programMap.get("objectId");
		String strBuildId = (String) programMap.get("buildId");
		StringList tempStrList = new StringList();
		String strIntendProdId = (String) programMap.get("intendProdId");
		String strModelId = (String) programMap.get("modelOID");
		String strOperation = (String) programMap.get("operation");
		if (ProductLineCommon.isNotNull(strIntendProdId)) {
			DomainObject domBuild = new DomainObject(strIntendProdId);
			String strProductIds = domBuild.getInfo(context, "to["
					+ ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
					+ "].from.id");
			tempStrList.add(strProductIds);
		}

		if (strProductConfigurationId != null
				&& strProductConfigurationId.length() > 0) {
			strProductConfigurationId = strProductConfigurationId.trim();
			DomainObject domProductId = new DomainObject(
					strProductConfigurationId);

			String strProductConfIds = domProductId.getInfo(context, "to["
					+ ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION
					+ "].from.id");

			// get all the Products in the entire Database
			StringList objSelect = new StringList(2);
			objSelect.addElement(DomainConstants.SELECT_ID);
			MapList lstProductList = DomainObject.findObjects(context,
					ProductLineConstants.TYPE_PRODUCTS,
					DomainConstants.QUERY_WILDCARD, "", objSelect);

			String strId = "";

			// Iterate the All the products in database and exclude the ones
			// which are not connected to
			// Product Configuration
			for (int i = 0; i < lstProductList.size(); i++) {
				Map productMap = (Map) lstProductList.get(i);
				strId = productMap.get("id").toString();

				if (strProductConfIds != null && strProductConfIds.length() > 0
						&& !strProductConfIds.equals(strId)) {
					tempStrList.addElement(strId);
				}
			}
		}
		if (strModelId != null && strModelId.length() > 0) {
			tempStrList = getModelContextProducts(context, strModelId);
		}
		if (isUNTInstalled(context, args)) {
			if (strBuildId != null && strBuildId.length() > 0) {
				DomainObject obj = new DomainObject(strBuildId);
				StringList selectables = new StringList();
				String strModelBuildSelectable = "to["
						+ ProductLineConstants.RELATIONSHIP_MODEL_BUILD
						+ "].from.id";
				String strProductBuildSelectable = "to["
						+ ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
						+ "].from.id";
				selectables.add(strModelBuildSelectable);
				selectables.add(strProductBuildSelectable);
				Map list = obj.getInfo(context, selectables);
				String strBuildModelId = (String) list
						.get(strModelBuildSelectable);
				if (strBuildModelId != null && strBuildModelId.length() > 0) {
					if (strOperation != null
							&& strOperation.equalsIgnoreCase("AsBuilt"))
						tempStrList = getModelContextProductsForAsBuilt(
								context, strBuildModelId);
					else {
						tempStrList = getModelContextProducts(context,
								strBuildModelId);
						return tempStrList;
					}
				}
			}

			tempStrList.addAll(checkForIPUAndGetRelatedProdId(context,
					strBuildId));
			// Following if condition added for IR-050574
			if (strOperation != null
					&& strOperation.equalsIgnoreCase("AsBuilt")) {
				StringList lst = getFirstLevelProdList(context, strBuildId);
				tempStrList.removeAll(lst);
			}
		}
		return tempStrList;
	}

	private StringList getFirstLevelProdList(Context context, String strBuildId)
			throws Exception {
		StringList lst = new StringList();
		// If condition added for IR-035528
		if (strBuildId != null && strBuildId.length() > 0) {
			DomainObject domBuild = new DomainObject(strBuildId);
			StringList result = domBuild.getInfoList(context, "from["
					+ ProductLineConstants.RELATIONSHIP_UNITBOM + "].to.id");
			DomainObject domUBOMBuild = null;
			String tempProdId = "";
			String strTempId = "";
			for (int i = 0; i < result.size(); i++) {
				strTempId = (String) result.get(i);
				domUBOMBuild = new DomainObject(strTempId);
				tempProdId = (String) domUBOMBuild.getInfo(context, "to["
						+ ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
						+ "].from.id");
				if (tempProdId != null && tempProdId.length() > 0) {
					lst.add(tempProdId);
				}
			}
		}
		return lst;
	}

	private StringList getModelContextProductsForAsBuilt(Context context,
			String strModelId) throws Exception {
		StringList tempStrList = new StringList();
		DomainObject domModelId = new DomainObject(strModelId);
		StringList objectSels = new StringList();
		objectSels.addElement(DomainConstants.SELECT_ID);
		objectSels.addElement(DomainConstants.SELECT_NAME);
		MapList productIds = domModelId.getRelatedObjects(context,
				ProductLineConstants.RELATIONSHIP_PRODUCTS, ProductLineConstants.TYPE_PRODUCTS,
				objectSels, null, false, true, (short) 1, null, null, 0);
		for (int i = 0; i < productIds.size(); i++) {
			Map map = (Map) productIds.get(i);
			String strId = (String) map.get(DomainConstants.SELECT_ID);
			tempStrList.add(strId);
		}
		return tempStrList;
	}

	private StringList getModelContextProducts(Context context,
			String strModelId) throws Exception, FrameworkException {
		StringList tempStrList = new StringList();
		DomainObject domModelId = new DomainObject(strModelId);
		StringList objectSels = new StringList();
		objectSels.addElement(DomainConstants.SELECT_ID);
		objectSels.addElement(DomainConstants.SELECT_NAME);
		MapList productIds = domModelId.getRelatedObjects(context,
				ProductLineConstants.RELATIONSHIP_PRODUCTS, ProductLineConstants.TYPE_PRODUCTS,
				objectSels, null, false, true, (short) 1, null, null, 0);
		StringList objSelect = new StringList(2);
		objSelect.addElement(DomainConstants.SELECT_ID);
		MapList lstProductList = DomainObject.findObjects(context,
				ProductLineConstants.TYPE_PRODUCTS,
				DomainConstants.QUERY_WILDCARD, "", objSelect);
		String strId = "";
		for (int i = 0; i < lstProductList.size(); i++) {
			Map productMap = (Map) lstProductList.get(i);
			strId = productMap.get("id").toString();

			if (!isExistsInList(productIds, strId)) {
				tempStrList.addElement(strId);
			}
		}
		return tempStrList;
	}

	/**
	 * This method is added to filter out the context IPUs for Unittracking.
	 *
	 * @param context
	 *            - Context
	 * @param strBuildId
	 *            - String
	 * @return - StringList
	 * @throws Exception
	 */
	private StringList checkForIPUAndGetRelatedProdId(Context context,
			String strBuildId) throws Exception {
		StringList listProdIds = new StringList();
		if (strBuildId != null && strBuildId.length() > 0) {
			DomainObject obj = new DomainObject(strBuildId);
			// if(obj.getInfo(context,"relationship["+ProductLineConstants.RELATIONSHIP_INTENDED_PRODUCT_UNIT+"]").equalsIgnoreCase("True")
			// ||
			// obj.getInfo(context,"relationship["+ProductLineConstants.RELATIONSHIP_UNITBOM+"]").equalsIgnoreCase("True"))
			// {

			StringList selectStmts = new StringList(1);
			selectStmts.addElement(DomainConstants.SELECT_ID);
			selectStmts.addElement(DomainConstants.SELECT_NAME);
			selectStmts.addElement("to["
					+ ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
					+ "].from.id");
			MapList listIPUFrom = obj.getRelatedObjects(context,
					ProductLineConstants.RELATIONSHIP_INTENDED_PRODUCT_UNIT
							+ "," + ProductLineConstants.RELATIONSHIP_UNITBOM, // relationship
																				// pattern
					ProductLineConstants.TYPE_HARDWARE_BUILD, // object pattern
					selectStmts, // object selects
					null, // relationship selects
					false, // to direction
					true, // from direction
					(short) 0, // recursion level
					null, // object where clause
					null);
			MapList listIPUTo = obj.getRelatedObjects(context,
					ProductLineConstants.RELATIONSHIP_INTENDED_PRODUCT_UNIT
							+ "," + ProductLineConstants.RELATIONSHIP_UNITBOM, // relationship
																				// pattern
					ProductLineConstants.TYPE_HARDWARE_BUILD, // object pattern
					selectStmts, // object selects
					null, // relationship selects
					true, // to direction
					false, // from direction
					(short) 0, // recursion level
					null, // object where clause
					null);

			MapList listIPU = new MapList();
			listIPU.addAll(listIPUFrom);
			listIPU.addAll(listIPUTo);
			if (listIPU != null && listIPU.size() > 0) {
				for (int i = 0; i < listIPU.size(); i++) {
					Map map = (Map) listIPU.get(i);
					String tempId = (String) map.get("to["
							+ ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
							+ "].from.id");
					if (tempId != null && tempId.length() > 0) {
						listProdIds.add(tempId);
					}
				}
			}
			// }
		}
		return listProdIds;
	}

	/**
	 * This method is added to filter out the context IPUs for Unittracking.
	 *
	 * @param context
	 *            - Context
	 * @param strBuildId
	 *            - String
	 * @return - StringList
	 * @throws Exception
	 */
	private StringList checkForIPUAndGetRelatedProdConfId(Context context,
			String strBuildId) throws Exception {
		StringList listProdConfIds = new StringList();
		StringList listProdIds = checkForIPUAndGetRelatedProdId(context,
				strBuildId);
		for (int i = 0; i < listProdIds.size(); i++) {
			String objid = (String) listProdIds.get(i);
			DomainObject tempObj = new DomainObject(objid);
			StringList list = tempObj.getInfoList(context, "from["
					+ ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION
					+ "].to.id");
			if (list != null && list.size() > 0) {
				listProdConfIds.addAll(list);
			}
		}
		return listProdConfIds;
	}

	private boolean isExistsInList(MapList productIds, String strId) {
		String strProdID = "";
		for (int i = 0; i < productIds.size(); i++) {
			Map productMap = (Map) productIds.get(i);
			strProdID = productMap.get("id").toString();
			if (strId.equals(strProdID)) {
				return true;
			}
		}
		return false;
	}

	// Fix For Bug 358998 Start
	/**
	 * Method to display the Short Hand Notation in the Build Properties Page
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the HashMap containing the following arguments
	 *            paramMap - contains ObjectId, Old Value for product name and
	 *            new value
	 * @return String - returns the textbox if the Form Mode is edit or will
	 *         display the Product Name
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductLine X+4
	 */
	public String getShortHandNotation(Context context, String[] args)
			throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		Map fieldMap = (HashMap) programMap.get("fieldMap");
		String strFieldName = (String) fieldMap.get("name");
		Map relBusObjPageList = (HashMap) programMap.get("paramMap");
		Map mapRequest = (HashMap) programMap.get("requestMap");
		String strObjectId = (String) relBusObjPageList.get("objectId");
		DomainObject objectModel = new DomainObject(strObjectId);

		String strShortHandNotation = objectModel.getAttributeValue(context,
				ProductLineConstants.ATTRIBUTE_PREFIX);

		StringBuffer sb = new StringBuffer();
		sb.append("<input type=\"text\" readonly=\"true\" ");
		sb.append("name=\"");
		sb.append(strFieldName + "\"");
		sb.append(" id=\" \"");
		sb.append(" value=\"");
		sb.append(strShortHandNotation + "\"");
		sb.append(" maxlength=\" \"");
		sb.append("size=\"");
		sb.append("20");
		sb.append("\" />");

		return sb.toString();
	}

	/**
	 * Method to display the Build Unit Number in the Build Properties Page
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the HashMap containing the following arguments
	 *            paramMap - contains ObjectId, Old Value for product name and
	 *            new value
	 * @return String - returns the textbox if the Form Mode is edit or will
	 *         display the Product Name
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductLine X+4
	 */
	public String getBuildUnitNumber(Context context, String[] args)
			throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		Map fieldMap = (HashMap) programMap.get("fieldMap");
		String strFieldName = (String) fieldMap.get("name");
		Map relBusObjPageList = (HashMap) programMap.get("paramMap");
		Map mapRequest = (HashMap) programMap.get("requestMap");
		String strObjectId = (String) relBusObjPageList.get("objectId");
		DomainObject objectModel = new DomainObject(strObjectId);

		String strBuildUnitNumber = objectModel.getAttributeValue(context,
				ProductLineConstants.ATTRIBUTE_BUILD_UNIT_NUMBER);

		StringBuffer sb = new StringBuffer();
		sb.append("<input type=\"text\" readonly=\"true\" ");
		sb.append("name=\"");
		sb.append(strFieldName + "\"");
		sb.append(" id=\" \"");
		sb.append(" value=\"");
		sb.append(strBuildUnitNumber + "\"");
		sb.append(" maxlength=\" \"");
		sb.append("size=\"");
		sb.append("20");
		sb.append("\" />");

		return sb.toString();
	}

	// Fix For Bug 358998 End

	/**
	 * Method to exclude context Product Builds/context Build for Add Existing
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the HashMap containing the following arguments
	 *            paramMap - contains ObjectId, Old Value for product name and
	 *            new value
	 * @return StringList - returns List of ObjectIDs which should be Excluded
	 * @throws Exception
	 *             if the operation fails
	 * @since X+5 PLC Enhancements
	 */

	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList filterProductBuilds(Context context, String[] args)
			throws Exception {
		StringList tempStrList = new StringList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strObjectId = (String) programMap.get("objectId");
		tempStrList.add(strObjectId);
		try {
			String strCtxtBuildId = "";
			String strCtxtProdId = "";
			DomainObject ctxtBuildObj = new DomainObject(strObjectId);
			strCtxtProdId = ctxtBuildObj.getInfo(context, "to["
					+ ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
					+ "].from.id");

			// Excluding context Product Builds for Add Existing
			if (ProductLineCommon.isNotNull(strCtxtProdId)) {
				DomainObject domProduct = new DomainObject(strCtxtProdId);
				StringList strCtxtBuildIds = domProduct
						.getInfoList(
								context,
								"from["
										+ ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
										+ "].to.id");
				for (int j = 0; j < strCtxtBuildIds.size(); j++) {
					strCtxtBuildId = (String) strCtxtBuildIds.elementAt(j);
					tempStrList.add(strCtxtBuildId);
				}
			}
			tempStrList.addAll(getParentIPUs(context, ctxtBuildObj));
			StringList tmpIPUList = getAllContextBuilds(context, ctxtBuildObj,
					new StringList());
			// removeOtherContextProdBuilds(context, tmpIPUList, ctxtBuildObj);
			tempStrList.addAll(tmpIPUList);
			return tempStrList;
		} catch (Exception e) {
			throw new FrameworkException(e);
		}
	}

	public StringList getAllContextBuilds(Context context,
			DomainObject ctxtBuildObj, StringList strList) throws Exception {
		StringList tempStrList = new StringList();
		tempStrList.addAll(getParentIPUs(context, ctxtBuildObj));
		StringList tmpIPUList = filterContextAndRelatedBuildsForIPU(context,
				ctxtBuildObj, new StringList());
		tmpIPUList.addAll(getContextAndOtherProdBuildsToExclude(context,
				ctxtBuildObj));
		// removeOtherContextProdBuilds(context, tmpIPUList, ctxtBuildObj);
		tempStrList.addAll(tmpIPUList);
		return tmpIPUList;
	}

	private StringList getContextAndOtherProdBuildsToExclude(Context context,
			DomainObject objBuild) throws Exception {
		StringList list = new StringList();
		StringList selectStmts = new StringList(1);
		selectStmts.addElement(DomainConstants.SELECT_ID);
		selectStmts.addElement(DomainConstants.SELECT_NAME);

		StringList prodbuilds = new StringList();
		StringList prodbuilds1 = new StringList();
		StringList tempList = new StringList();
		MapList buildsList= null;
		if (objBuild.getInfo(context,
				"to[" + ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD + "]")
				.equalsIgnoreCase("True")) {

			prodbuilds = objBuild
					.getInfoList(context, "to["
							+ ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
							+ "].from.to["
							+ ProductLineConstants.RELATIONSHIP_PRODUCTS
							+ "].from.from["
							+ ProductLineConstants.RELATIONSHIP_MODEL_BUILD
							+ "].to.id");
			prodbuilds1 = objBuild
					.getInfoList(context, "to["
					+ ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
					+ "].from.to["
					+ ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT
					+ "].from.from["
					+ ProductLineConstants.RELATIONSHIP_MODEL_BUILD
					+ "].to.id");
			if(prodbuilds1 != null && !prodbuilds1.isEmpty()){
				prodbuilds.addAll(prodbuilds1);
			}
		} else if (objBuild.getInfo(context,
				"to[" + ProductLineConstants.RELATIONSHIP_MODEL_BUILD + "]")
				.equalsIgnoreCase("True")) {
			prodbuilds = objBuild
					.getInfoList(context, "to["
							+ ProductLineConstants.RELATIONSHIP_MODEL_BUILD
							+ "].from.from["
							+ ProductLineConstants.RELATIONSHIP_MODEL_BUILD
							+ "].to.id");
		}
		list.addAll(prodbuilds);

		StringList tempIPUList = new StringList();
		StringList objSelect = new StringList(2);
		objSelect.addElement(DomainConstants.SELECT_ID);
		objSelect.addElement(DomainConstants.SELECT_NAME);
		MapList contextIPUList = DomainObject.findObjects(context,
				ProductLineConstants.TYPE_HARDWARE_BUILD,
				DomainConstants.QUERY_WILDCARD, "relationship["
						+ ProductLineConstants.RELATIONSHIP_MODEL_BUILD
						+ "]==True", objSelect);
		for (int i = 0; i < contextIPUList.size(); i++) {
			Map map = (Map) contextIPUList.get(i);
			DomainObject tempObj = new DomainObject(
					(String) map.get(DomainConstants.SELECT_ID));
			tempIPUList = filterContextAndRelatedBuildsForIPU(context, tempObj,
					new StringList());
			if (contextProdBuildExists(prodbuilds, tempIPUList)) {
				list.add((String) map.get(DomainConstants.SELECT_ID));
			}
		}
		list.addAll(getFromSideProdBuilds(context, objBuild));
		return list;
	}

	private boolean contextProdBuildExists(StringList prodbuilds,
			StringList tempIPUList) {
		for (int i = 0; i < prodbuilds.size(); i++) {
			String temp = (String) prodbuilds.get(i);
			if (tempIPUList.contains(temp)) {
				return true;
			}
		}
		return false;
	}

	private StringList getFromSideProdBuilds(Context context,
			DomainObject ctxtBuildObj) throws Exception {
		StringList selectStmts = new StringList(1);
		selectStmts.addElement(DomainConstants.SELECT_ID);
		selectStmts.addElement(DomainConstants.SELECT_NAME);
		StringList prodList = new StringList();
		MapList contextIPUList = ctxtBuildObj.getRelatedObjects(context,
				ProductLineConstants.RELATIONSHIP_INTENDED_PRODUCT_UNIT + ","
						+ ProductLineConstants.RELATIONSHIP_UNITBOM, // relationship
																		// pattern
				ProductLineConstants.TYPE_HARDWARE_BUILD, // object pattern
				selectStmts, // object selects
				null, // relationship selects
				true, // to direction
				false, // from direction
				(short) 0, // recursion level
				null, // object where clause
				null);
		for (int i = 0; i < contextIPUList.size(); i++) {
			Map map = (Map) contextIPUList.get(i);
			String objId = (String) map.get(DomainConstants.SELECT_ID);
			DomainObject obj = new DomainObject(objId);
			String prodId = obj.getInfo(context, "to["
					+ ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
					+ "].from.id");
			if (prodId != null && prodId.length() > 0) {
				prodList.addAll(obj.getInfoList(context, "to["
						+ ProductLineConstants.RELATIONSHIP_MODEL_BUILD
						+ "].from." + "from["
						+ ProductLineConstants.RELATIONSHIP_MODEL_BUILD
						+ "].to.id"));

			}
		}

		return prodList;

	}

	/**
	 * This method is added to filter out the builds which are having IPU or
	 * UBOM relationships with context builds It also filters the context
	 * product builds.
	 *
	 * @param context
	 * @param ctxtBuildObj
	 * @param orgList
	 * @return
	 * @throws Exception
	 */
	public StringList filterContextAndRelatedBuildsForIPU(Context context,
			DomainObject ctxtBuildObj, StringList orgList) throws Exception {
		StringList list = new StringList();
		StringList prodbuilds = new StringList();
		StringList tempList = new StringList();
		StringList selectStmts = new StringList(1);
		selectStmts.addElement(DomainConstants.SELECT_ID);
		selectStmts.addElement(DomainConstants.SELECT_NAME);
		selectStmts.addElement(DomainConstants.SELECT_TYPE);
		MapList listIPU = ctxtBuildObj.getRelatedObjects(context,
				ProductLineConstants.RELATIONSHIP_INTENDED_PRODUCT_UNIT + ","
						+ ProductLineConstants.RELATIONSHIP_UNITBOM, // relationship
																		// pattern
				ProductLineConstants.TYPE_HARDWARE_BUILD, // object pattern
				selectStmts, // object selects
				null, // relationship selects
				true, // to direction
				true, // from direction
				(short) 0, // recursion level
				null, // object where clause
				null);

		for (int i = 0; i < listIPU.size(); i++) {
			Map map = (Map) listIPU.get(i);
			if (((String) map.get(DomainConstants.SELECT_TYPE))
					.equalsIgnoreCase(ProductLineConstants.TYPE_HARDWARE_BUILD)
					&& !list.contains(DomainConstants.SELECT_ID)) {
				list.add(map.get(DomainConstants.SELECT_ID));
			}
		}
		return list;
	}

	/**
	 * Mathod added as part of fic bor BUG: 370415 -- This will return the Build
	 * which is the parent IPU to avoid cyclic dependency.
	 *
	 * @param context
	 * @param ctxtBuildObj
	 * @return
	 * @throws Exception
	 */
	private StringList getParentIPUs(Context context, DomainObject ctxtBuildObj)
			throws Exception {
		MapList maplist = new MapList();
		StringList selectStmts = new StringList(1);
		selectStmts.addElement(DomainConstants.SELECT_ID);
		String strIPUToRel = "to["
				+ ProductLineConstants.RELATIONSHIP_INTENDED_PRODUCT_UNIT
				+ "].id";
		selectStmts.addElement(strIPUToRel);
		maplist = ctxtBuildObj.getRelatedObjects(context,
				ProductLineConstants.RELATIONSHIP_INTENDED_PRODUCT_UNIT, // relationship
																			// pattern
				ProductLineConstants.TYPE_HARDWARE_BUILD, // object pattern
				selectStmts, // object selects
				null, // relationship selects
				true, // to direction
				false, // from direction
				(short) 0, // recursion level
				null, // object where clause
				null);
		StringList list = new StringList();
		if (maplist != null) {
			for (int i = 0; i < maplist.size(); i++) {
				Map map = (Map) maplist.get(i);
				if (map.get(strIPUToRel) == null) {
					list.addElement((String) map.get(DomainConstants.SELECT_ID));
				}
			}
		}
		return list;
	}

	/**
	 * Get the list of all builds.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds no arguments.
	 * @return Object of type MapList
	 * @throws Exception
	 *             if the operation fails
	 * @since X+5
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getIntendedProductUnits(Context context, String[] args)
			throws Exception {

		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		MapList ubomList = new MapList();
		String sBuildId = (String) paramMap.get("objectId");
		DomainObject domBuild = new DomainObject(sBuildId);
		String sRelName = PropertyUtil.getSchemaProperty(context,
				"relationship_IntendedProductUnit");
		String sTypeName = PropertyUtil.getSchemaProperty(context, "type_Unit");
		int level = 1;

		StringList selectStmts = new StringList(1);
		StringList selectRelStmts = new StringList(1);
		selectStmts.addElement(SELECT_ID);
		selectStmts.addElement(SELECT_TYPE);
		selectStmts.addElement(SELECT_NAME);
		selectStmts.addElement(SELECT_REVISION);
		selectStmts.addElement(SELECT_DESCRIPTION);

		selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

		// Get IPUs
		ubomList = domBuild.getRelatedObjects(context, sRelName, sTypeName,
				selectStmts, selectRelStmts, false, true, (short) level, null,
				null);

		return ubomList;

	}

	/**
	 * Method to display the Product Name in the Build Properties Page
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the HashMap containing the following arguments
	 *            paramMap - contains ObjectId, Old Value for product name and
	 *            new value
	 * @return String - returns the textbox if the Form Mode is edit or will
	 *         display the Product Name
	 * @throws Exception
	 *             if the operation fails
	 * @since ProductLine X+4
	 */

	public String getBuildProduct(Context context, String[] args)
			throws Exception {

		Map programMap = (HashMap) JPO.unpackArgs(args);
		Map fieldMap = (HashMap) programMap.get("fieldMap");
		String strFieldName = (String) fieldMap.get("name");
		Map relBusObjPageList = (HashMap) programMap.get("paramMap");
		Map mpRequest = (HashMap) programMap.get("requestMap");
		String strObjectId = (String) relBusObjPageList.get("objectId");
		String strMode = (String) mpRequest.get("mode");
		String strModelContext = (String) mpRequest.get("modelContext");
		String strParentOID = (String) mpRequest.get("parentOID");
		String strContext = (String) mpRequest.get("createContext");
		String strOperation = (String) mpRequest.get("operation");
		String strContextBuildId = (String) mpRequest.get("parentOID");
		String strProductName = null;
		String strProductId = null;

		if (strObjectId != null) {
			DomainObject domBuild = new DomainObject(strObjectId);
			strProductName = domBuild.getInfo(context, "to["
					+ ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
					+ "].from.name");
			strProductId = domBuild.getInfo(context, "to["
					+ ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
					+ "].from.id");

		}

		if (strProductId == null) {
			strProductName = "";
			strProductId = "";
		}

		StringBuffer sbBuffer = new StringBuffer();

		if ((strMode != null && !strMode.equals("")
				&& !strMode.equalsIgnoreCase("null") && strMode
				.equalsIgnoreCase("edit"))
				|| (strContext != null && !strContext.equals(""))) {
			sbBuffer.append("<input type=\"text\" readonly=\"true\" ");
			sbBuffer.append("name=\"");
			sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
			sbBuffer.append("Display\" id=\"" + strProductId + "\" value=\"");
			sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strProductName));
			sbBuffer.append("\" />");
			sbBuffer.append("<input type=\"hidden\" name=\"");
			sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
			sbBuffer.append("\" value=\"");
			sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strProductId));
			sbBuffer.append("\" />");
			sbBuffer.append("<input type=\"hidden\" name=\"");
			sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
			sbBuffer.append("OID\" value=\"");
			sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strProductId));
			sbBuffer.append("\" />");
			sbBuffer.append(getModelContextString(context,strModelContext, strParentOID));
			sbBuffer.append(getAsBuiltContextString(context,strOperation,
					strContextBuildId));
			sbBuffer.append("<input ");
			sbBuffer.append("type=\"button\" name=\"btnProductChooser\"");
			sbBuffer.append(" size=\"200\" value=\"...\" ");
			sbBuffer.append("onClick=\"javascript:autonomySearchProduct()");
			sbBuffer.append("\" />");
			sbBuffer.append("<a href=\"javascript:clearProduct('");
			sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
			sbBuffer.append("')\">");

			String strClear = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Button.Clear",
					context.getSession().getLanguage());
			sbBuffer.append(strClear);
			sbBuffer.append("</a>");

		} else {
			if (strProductId != null && !strProductId.equals("")) {
				sbBuffer.append("<A HREF=\"JavaScript:showDetailsPopup('../common/emxTree.jsp?objectId=");
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strProductId));
				sbBuffer.append("&mode=replace");
				sbBuffer.append("&AppendParameters=true");
				sbBuffer.append("&reloadAfterChange=true");
				sbBuffer.append("')\"class=\"object\">");
				sbBuffer.append("<img border=\"0\" src=\"");
				sbBuffer.append("images/iconSmallProduct.gif");
				sbBuffer.append("\"</img>");
				sbBuffer.append("</A>");
				sbBuffer.append("&nbsp");
				sbBuffer.append("<A HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
				sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strProductId));
				sbBuffer.append("&mode=replace");
				sbBuffer.append("&AppendParameters=true");
				sbBuffer.append("&reloadAfterChange=true");
				sbBuffer.append("')\"class=\"object\">");
				sbBuffer.append(XSSUtil.encodeForHTML(context,strProductName));
				sbBuffer.append("</A>");
				return sbBuffer.toString();
			}
		}
		return sbBuffer.toString();
	}

	// added by P Voggu
	/*
	 * This method is used to display the Design Responsibility field in the
	 * Build properties page
	 */

	public String getDesignResponsibilityForBuild(Context context, String[] args)
			throws Exception {

		Map programMap = (HashMap) JPO.unpackArgs(args);
		Map fieldMap = (HashMap) programMap.get("fieldMap");
		String strFieldName = (String) fieldMap.get("name");
		Map relBusObjPageList = (HashMap) programMap.get("paramMap");
		Map mpRequest = (HashMap) programMap.get("requestMap");
		String strObjectId = (String) relBusObjPageList.get("objectId");
		String strMode = (String) mpRequest.get("mode");
		String strModelContext = (String) mpRequest.get("modelContext");
		String strParentOID = (String) mpRequest.get("parentOID");
		String strContext = (String) mpRequest.get("createContext");
		String strOperation = (String) mpRequest.get("operation");
		String strContextBuildId = (String) mpRequest.get("objectId");
		String strDesignResponsibilityName = null;
		String strDesignResponsibilityId = null;

		if (strObjectId != null) {
			DomainObject domBuild = new DomainObject(strObjectId);
			strDesignResponsibilityName = domBuild.getInfo(context, "to["
					+ ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY
					+ "].from.name");
			strDesignResponsibilityId = domBuild.getInfo(context, "to["
					+ ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY
					+ "].from.id");

		}

		if (strDesignResponsibilityId == null) {
			strDesignResponsibilityName = "";
			strDesignResponsibilityId = "";
		}

		StringBuffer sbBuffer = new StringBuffer();

		if ((strMode != null && !strMode.equals("")
				&& !strMode.equalsIgnoreCase("null") && strMode
				.equalsIgnoreCase("edit"))
				|| (strContext != null && !strContext.equals(""))) {
			sbBuffer.append("<input type=\"text\" readonly=\"true\" ");
			sbBuffer.append("name=\"");
			sbBuffer.append(strFieldName);
			sbBuffer.append("Display\" id=\"" + strDesignResponsibilityId
					+ "\" value=\"");
			sbBuffer.append(strDesignResponsibilityName);
			sbBuffer.append("\" />");
			sbBuffer.append("<input type=\"hidden\" name=\"");
			sbBuffer.append(strFieldName);
			sbBuffer.append("\" value=\"");
			sbBuffer.append(strDesignResponsibilityId);
			sbBuffer.append("\" />");
			sbBuffer.append("<input type=\"hidden\" name=\"");
			sbBuffer.append(strFieldName);
			sbBuffer.append("OID\" value=\"");
			sbBuffer.append(strDesignResponsibilityId);
			sbBuffer.append("\" />");
			sbBuffer.append("<input ");
			sbBuffer.append("type=\"button\" name=\"btnDesignResponsibilityChooser\"");
			sbBuffer.append(" size=\"200\" value=\"...\" ");
			sbBuffer.append("onClick=\"javascript:autonomySearchDesignResponsibility()");
			sbBuffer.append("\" />");
			sbBuffer.append("<a href=\"javascript:ClearDesignResponsibility('");
			sbBuffer.append(strFieldName);
			sbBuffer.append("')\">");

			String strClear = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Button.Clear",
					context.getSession().getLanguage());
			sbBuffer.append(strClear);
			sbBuffer.append("</a>");

		} else {
			if (strDesignResponsibilityId != null
					&& !strDesignResponsibilityId.equals("")) {
				sbBuffer.append("<A HREF=\"JavaScript:showDetailsPopup('../common/emxTree.jsp?objectId=");
				sbBuffer.append(strDesignResponsibilityId);
				sbBuffer.append("&mode=replace");
				sbBuffer.append("&AppendParameters=true");
				sbBuffer.append("&reloadAfterChange=true");
				sbBuffer.append("')\"class=\"object\">");
				sbBuffer.append("</A>");
				sbBuffer.append("&nbsp");
				sbBuffer.append("<A HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
				sbBuffer.append(strDesignResponsibilityId);
				sbBuffer.append("&mode=replace");
				sbBuffer.append("&AppendParameters=true");
				sbBuffer.append("&reloadAfterChange=true");
				sbBuffer.append("')\"class=\"object\">");
				sbBuffer.append(strDesignResponsibilityName);
				sbBuffer.append("</A>");
				return sbBuffer.toString();
			}
		}
		return sbBuffer.toString();
	}

	private String getAsBuiltContextString(Context context,String strOperation,
			String strContextbuild) {
		if (strOperation == null || strOperation.length() == 0
				|| strContextbuild == null || strContextbuild.length() == 0
				|| !strOperation.equalsIgnoreCase("asbuilt")) {
			return "";
		}
		StringBuffer sbBuffer = new StringBuffer();
		sbBuffer.append("<input type=\"hidden\" name=\"");
		sbBuffer.append("contextBuild");
		sbBuffer.append("OID\" value=\"");
		sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strContextbuild));
		sbBuffer.append("\" />");
		return sbBuffer.toString();
	}

	/**
	 * Method added to display the model related information
	 *
	 * @param strModelContext
	 * @param strParentOID
	 * @return
	 */
	private String getModelContextString(Context context,String strModelContext,
			String strParentOID) {
		if (strModelContext == null || strModelContext.length() == 0
				|| strParentOID == null || strParentOID.length() == 0) {
			return "";
		}
		StringBuffer sbBuffer = new StringBuffer();
		sbBuffer.append("<input type=\"hidden\" name=\"");
		sbBuffer.append("model");
		sbBuffer.append("OID\" value=\"");
		sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strParentOID));
		sbBuffer.append("\" />");
		return sbBuffer.toString();
	}

	/**
	 * This method updates the Build Unit Number and Shorthand Notation of Build
	 * based on the Product it is connected to It is called as a create trigger
	 * when the Product Build relationship
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the Product Id, Build Id and Relationship Id
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 * @since Product Line X+4
	 */

	public void assignBuildUnitNumberAndShorthandNotation(Context context,
			String[] args) throws Exception {
		try {
			ContextUtil
					.pushContext(context, PropertyUtil.getSchemaProperty(
							context, "person_UserAgent"),
							DomainConstants.EMPTY_STRING,
							DomainConstants.EMPTY_STRING);

			String strProductBuildRelId = args[0];
			String[] arrRelId = new String[1];
			String strModelId = "";
			arrRelId[0] = strProductBuildRelId;
			StringList relSelects = new StringList(2);
			relSelects.add(DomainRelationship.SELECT_FROM_ID);
			relSelects.add(DomainRelationship.SELECT_TO_ID);
			MapList objRelMapList = DomainRelationship.getInfo(context,
					arrRelId, relSelects);
			Map objRelMap = (Map) objRelMapList.get(0);
			DomainObject objProduct = newInstance(context,
					(String) objRelMap.get(DomainRelationship.SELECT_FROM_ID));
			DomainObject objBuild = newInstance(context,
					(String) objRelMap.get(DomainRelationship.SELECT_TO_ID));
			String strIsVersion = objProduct.getAttributeValue(context,
					ProductLineConstants.ATTRIBUTE_IS_VERSION);
			if (strIsVersion.equalsIgnoreCase("FALSE")) {
				strModelId = objProduct.getInfo(context, "to["
						+ ProductLineConstants.RELATIONSHIP_PRODUCTS + "].from.id");
	           // If the Model Id comes back empty, and derivations is turned on, check the Main Product Relationship.
		       if((strModelId == null || strModelId.equals("null")||strModelId.equals(""))) {
		           strModelId = objProduct.getInfo(context,"to["+ ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT + "].from.id");
	    	   }
			} else {
				strModelId = objProduct.getInfo(context, "to["
						+ ProductLineConstants.RELATIONSHIP_PRODUCT_VERSION
						+ "].from.relationship["
						+ ProductLineConstants.RELATIONSHIP_PRODUCTS
						+ "].from.id");
			}
			if (strModelId == null || strModelId.equals("null")
					|| strModelId.equals("")) {
				return;
			}

			String[] arg = new String[1];
			arg[0] = strModelId;
			emxModel_mxJPO objModel = new emxModel_mxJPO(context, arg);
			String strPrefix = objModel.getInfo(context, "attribute["
					+ ProductLineConstants.ATTRIBUTE_PREFIX + "]");
			if (strPrefix == null
					|| (strPrefix != null && strPrefix.equalsIgnoreCase("null"))) {
				strPrefix = "";
			}
			HashMap attributeMap = new HashMap();
			String strUnitNumber = objBuild.getAttributeValue(context,
					ProductLineConstants.ATTRIBUTE_BUILD_UNIT_NUMBER);
			String strUpdatedUnitNumber = strUnitNumber;
			// Check if the Unit number exists for the build...if exists then
			// return.
			if (strUnitNumber == null
					|| (strUnitNumber != null && (strUnitNumber
							.equalsIgnoreCase("") || strUnitNumber
							.equalsIgnoreCase("0")))) {

				int iUnitNumber = objModel.getLastUnitNumber(context,
						strModelId);

				strUpdatedUnitNumber = String.valueOf(iUnitNumber + 1);
				String[] arrParameters = new String[2];
				arrParameters[0] = strModelId;
				arrParameters[1] = strUpdatedUnitNumber;
				objModel.setLastUnitNumber(context, arrParameters);

				attributeMap.put(
						ProductLineConstants.ATTRIBUTE_BUILD_UNIT_NUMBER,
						strUpdatedUnitNumber);
			}
			String strShorthandNotation = strPrefix
					.concat(strUpdatedUnitNumber);
			attributeMap.put(ProductLineConstants.ATTRIBUTE_PREFIX,
					strShorthandNotation);
			objBuild.setAttributeValues(context, attributeMap);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Set the context back to the context user
			ContextUtil.popContext(context);
		}
	}

	/**
	 * This method disconnects the Builds from Product Configuration when they
	 * are disconnected from parent Products It is called as a delete trigger on
	 * the Product Build relationship
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the Product Id, Build Id
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 * @since Product Line X+4
	 */
	public void disconnectBuildsFromProductConfigurationOnProductBuildDelete(
			Context context, String[] args) throws Exception {

		String strBuildId = args[0];
		DomainObject objBuild = newInstance(context, strBuildId);
		String relId = objBuild.getInfo(context, "to["
				+ ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION_BUILD
				+ "].id");
		if (relId != null && !relId.equals("null") && !relId.equals("")) {
			DomainRelationship.disconnect(context, relId);
		}
	}

	/**
	 * This method connects the Buildto parent Product when they are connected
	 * to Product Configurations It is called as a create trigger on the Product
	 * Configuration Build relationship
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the Product COnfiguration Build Relationship id
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 * @since Product Line X+4
	 */
	public void connectBuildsToProductOnProductConfigurationBuildsCreate(
			Context context, String[] args) throws Exception {
		String strProductBuildRelId = args[0];
		String[] arrRelId = new String[1];
		arrRelId[0] = strProductBuildRelId;
		StringList relSelects = new StringList(2);
		relSelects.add(DomainRelationship.SELECT_FROM_ID);
		relSelects.add(DomainRelationship.SELECT_TO_ID);

		MapList objRelMapList = DomainRelationship.getInfo(context, arrRelId,
				relSelects);
		Map objRelMap = (Map) objRelMapList.get(0);
		String strBuildId = (String) objRelMap
				.get(DomainRelationship.SELECT_TO_ID);
		DomainObject objBuild = newInstance(context,
				(String) objRelMap.get(DomainRelationship.SELECT_TO_ID));
		DomainObject objProductConfiguration = newInstance(context,
				(String) objRelMap.get(DomainRelationship.SELECT_FROM_ID));
		StringList busSelects = new StringList(3);
		busSelects.add(DomainConstants.SELECT_ID);
		busSelects.add(DomainConstants.SELECT_NAME);
		busSelects.add(DomainConstants.SELECT_REVISION);
		StringList relSelect = new StringList(1);
		relSelect.add(DomainConstants.SELECT_RELATIONSHIP_ID);
		MapList objMapList = objProductConfiguration.getRelatedObjects(context,
				ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION,
				ProductLineConstants.TYPE_PRODUCTS, true, false, 1, busSelects,
				relSelect, "", "", "", "", null);
		Map objMap = null;
		if (!objMapList.isEmpty()) {
			StringList slProductidList = objBuild.getInfoList(context, "to["
					+ ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
					+ "].from.id");
			objMap = (Map) objMapList.get(0);
			String strProductId = (String) objMap
					.get(DomainConstants.SELECT_ID);
			DomainObject objProduct = newInstance(context, strProductId);
			StringList strProductBuildID = objProduct.getInfoList(context,
					"from[" + ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
							+ "].to.id");
			if (!strProductBuildID.contains(strBuildId)) {
				DomainRelationship.connect(context, objProduct,
						ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD,
						objBuild);
			}
		}
	}

	/**
	 * This method checks whether the builds are in Complete or Release
	 * state/Products are in Release state and stops the user from deleting the
	 * builds
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the builds Id
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 * @since PLC Enhancements X+5
	 */

	public int checkForBuildDeleteRules(Context context, String[] args)
			throws Exception {
		String strBuildId = args[0];
		String prodState = "";
		String strProductId = "";
		boolean isBuildComplete = false;
		boolean isProdRelease = false;
		boolean isAsBuilt = false;
		String strProdAlertMessage = "";
		String strBuildAlertMessage = "";
		String strAsBuiltAlertMessage = "";
		String language = context.getSession().getLanguage();
		String strUBOMRel = ProductLineConstants.RELATIONSHIP_UNITBOM;
		DomainObject dombuild = new DomainObject(strBuildId);
		StringList selectStmts = new StringList();
		selectStmts.addElement("from[" + strUBOMRel + "].to.id");
		selectStmts.addElement(SELECT_CURRENT);
		Map map = dombuild.getInfo(context, selectStmts);
		String strUnitToId = "";
		String curState = "";
		for (int i = 0; i < map.size(); i++) {
			strUnitToId = (String) map.get("from[" + strUBOMRel + "].to.id");
			curState = (String) map.get(SELECT_CURRENT);
		}
		if (strUnitToId != null) {
			isAsBuilt = true;
		}
		if (isAsBuilt) {
			strAsBuiltAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
					"emxProduct.Alert.AsBuiltAlertMessage", language);
			// new MQLCommand().executeCommand(context, "notice '" +
			// strAsBuiltAlertMessage + "'");
			MqlUtil.mqlCommand(context, "notice $1", strAsBuiltAlertMessage);
			return 1;
		}
		Map objPmap = null;
		StringList busSelects = new StringList(1);
		busSelects.add(DomainConstants.SELECT_ID);
		objPmap = dombuild.getRelatedObject(context,
				ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD, false,
				busSelects, null);
		if (objPmap != null) {
			strProductId = (String) objPmap.get(DomainConstants.SELECT_ID);
			DomainObject domProd = new DomainObject(strProductId);
			prodState = domProd.getInfo(context, SELECT_CURRENT);
		}
		if (prodState.equals("Release")) {
			isProdRelease = true;
		}
		if (isProdRelease) {

			strProdAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
					"emxProduct.Alert.ProductDeleteReleaseState",language);
			// new MQLCommand().executeCommand(context, "notice '" +
			// strProdAlertMessage + "'");
			MqlUtil.mqlCommand(context, "notice $1", strProdAlertMessage);
			return 1;
		}

		if (curState.equals("Complete") || curState.equals("Release")) {
			isBuildComplete = true;
		}
		if (isBuildComplete) {
			strBuildAlertMessage =EnoviaResourceBundle.getProperty(context,SUITE_KEY,
					"emxProduct.Alert.BuildDeleteCompleteState",language);

			// new MQLCommand().executeCommand(context, "notice '" +
			// strBuildAlertMessage + "'");
			MqlUtil.mqlCommand(context, "notice $1", strBuildAlertMessage);
			return 1;
		}
		return 0;
	}

	/**
	 * This method checks whether the builds are in Complete or Release
	 * state/Products are in Release state and stops the user from removing the
	 * builds
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the builds Id and Product Id
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 * @since PLC Enhancements X+5
	 */

	public int checkForBuildRemoveRules(Context context, String[] args)
			throws Exception {

		String strBuildId = args[0];
		String strProductId = args[1];
		boolean isBuildComplete = false;
		boolean isProdRelease = false;
		String strProdAlertMessage = "";
		String strBuildAlertMessage = "";

		DomainObject dombuild = new DomainObject(strBuildId);
		String curState = dombuild.getInfo(context, SELECT_CURRENT);

		DomainObject domProd = new DomainObject(strProductId);
		String prodState = domProd.getInfo(context, SELECT_CURRENT);

		if (prodState.equals("Release")) {
			isProdRelease = true;
		}

		if (isProdRelease) {
			String language = context.getSession().getLanguage();
			strProdAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
					"emxProduct.Alert.ProductRemoveReleaseState",language);
			// new MQLCommand().executeCommand(context, "notice '" +
			// strProdAlertMessage + "'");
			MqlUtil.mqlCommand(context, "notice $1", strProdAlertMessage);
			return 1;

		}

		if (curState.equals("Complete") || curState.equals("Release")) {
			isBuildComplete = true;
		}

		if (isBuildComplete) {
			String language = context.getSession().getLanguage();
			strBuildAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
					"emxProduct.Alert.BuildRemoveCompleteState",language);
			// new MQLCommand().executeCommand(context, "notice '" +
			// strBuildAlertMessage + "'");
			MqlUtil.mqlCommand(context, "notice $1", strBuildAlertMessage);
			return 1;
		}
		return 0;
	}

	public boolean isUNTInstalled(Context context, String args[]) {
		return FrameworkUtil.isSuiteRegistered(context,
				"appInstallTypeUnitTracking", false, null, null);
	}

	/**
	 *
	 * @param context
	 *            - Matrix context
	 * @param args
	 *            - String[]
	 * @return - StringList
	 * @throws Exception
	 * @since Product Line X+5 - Partmarking
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList getSerializedParts(Context context, String[] args)
			throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		StringList sList = new StringList();
		String sBuildId = (String) programMap.get("objectId");
		sList.add(sBuildId);

		try {
			StringBuffer strBuffer = new StringBuffer(128);
			strBuffer.append("(from["
					+ ProductLineConstants.RELATIONSHIP_PART_MARKING
					+ "].to.attribute["
					+ ProductLineConstants.ATTRIBUTE_MARKED_BY
					+ "].value == \"");
			strBuffer.append("Lot");
			strBuffer.append("\") ");

			String sWhereExp = strBuffer.toString();

			StringList objSelect = new StringList(2);
			objSelect.addElement(DomainConstants.SELECT_ID);
			objSelect.addElement(DomainConstants.SELECT_NAME);
			MapList buildsList = DomainObject.findObjects(context,
					DomainConstants.TYPE_PART, DomainConstants.QUERY_WILDCARD,
					sWhereExp, objSelect);
			for (int i = 0; i < buildsList.size(); i++) {
				Map buildMap = (Map) buildsList.get(i);
				sList.add((String) buildMap.get(DomainConstants.SELECT_ID));
			}
			return sList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}

	/**
	 * To check whether CFB is Installed or not.
	 *
	 * @param context
	 * @param args
	 * @return Boolean
	 * @throws Exception
	 * @since R209
	 */
	public boolean isCFPInstalled(Context context, String[] args)
			throws Exception {
		return FrameworkUtil.isSuiteRegistered(context,
				"featureVersionDMCPlanning", false, null, null);
	}

	/**
	 * To check whether to display the Product column or not
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @exclude
	 */
	public boolean displayProductField(Context context, String[] args)
			throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String parentOID = (String) programMap.get("parentOID");

		if (parentOID != null && !"".equals(parentOID)) {
			DomainObject obj = new DomainObject(parentOID);
			String type = obj.getInfo(context, DomainConstants.SELECT_TYPE);
			if (obj.isKindOf(context, ProductLineConstants.TYPE_PRODUCTS)
					|| obj.isKindOf(context,
							ProductLineConstants.TYPE_PRODUCT_CONFIGURATION))
				return false;
		}
		return true;
	}

	/**
	 * To check whether to display the Product Configuration column or not
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @exclude
	 */
	public boolean displayProductConfigurationField(Context context,
			String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String parentOID = (String) programMap.get("parentOID");

		if (parentOID != null && !"".equals(parentOID)) {
			DomainObject obj = new DomainObject(parentOID);
			String type = obj.getInfo(context, DomainConstants.SELECT_TYPE);
			if (obj.isKindOf(context,
					ProductLineConstants.TYPE_PRODUCT_CONFIGURATION))
				return false;
		}
		return true;
	}

	/**
	 * To determine whether the Shorthand Notation field can be editable or not.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            hold the list of object ids as objectList
	 * @return StringList of boolean values
	 * @throws Exception
	 *             if operation fails
	 */
	public StringList isShorthandNotationEditable(Context context, String[] args)
			throws Exception {
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList list = (MapList) programMap.get("objectList");
			StringList resultList = new StringList(list.size());
			Iterator itr = list.iterator();
			Map objectMap = null;
			String id = null;
			Build build = new Build();
			for (int i = 0; itr.hasNext(); i++) {
				objectMap = (Map) itr.next();
				id = (String) objectMap.get("id");
				build.setId(id);
				resultList.addElement(build.isProductBuild(context));
			}
			return resultList;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getBuildsForMassEdit(Context context, String args[])throws Exception {
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap requestValuesMap = (HashMap)programMap.get("RequestValuesMap");
		StringList objectSelect = new StringList(2);
		objectSelect.addElement(DomainConstants.SELECT_ID);
		objectSelect.addElement(DomainConstants.SELECT_NAME);

		String objectIds[] = (String[])requestValuesMap.get("objectIds");
		String objids = objectIds[0].substring(1, objectIds[0].length()-1);
		StringList objList = FrameworkUtil.split(objids, ",");
		StringList resultList = new StringList(objList.size());
		String temp = "";
		for(int i=0; i<objList.size(); i++){
			temp = (String)objList.get(i);
			resultList.addElement(temp.trim());
		}

		String[] buildIdList = new String[resultList.size()];
		resultList.toArray(buildIdList);

		MapList list = new MapList();
		try{
			list = DomainObject.getInfo(context, buildIdList, objectSelect);
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	return list;
	}

	public boolean showNoOfBuildsField(Context context, String args[]) throws Exception{
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String createMultiple = (String)programMap.get("multiBuildCreation");
		if("true".equalsIgnoreCase(createMultiple)) return true;
		return false;
	}

    /**
     * Gets the binary composition data for the Build's Product allocation
     *
	 * @param context The eMatrix <code>Context</code> object
	 * @param args holds a packed hashmap containing objectId
	 * @return MapList containing Product composition information for the Build
	 * @throws Exception if the operation fails
     */
    public MapList getProductAllocation(Context context, String[] args) throws Exception {
        MapList productList = new MapList();
        Map programMap = (HashMap) JPO.unpackArgs(args);
    	String buildId = (String)programMap.get("objectId");

		String RELATIONSHIP_PRODUCTS = PropertyUtil.getSchemaProperty(context, "relationship_Products");
		String RELATIONSHIP_MAIN_PRODUCT = PropertyUtil.getSchemaProperty(context, "relationship_MainProduct");
		String SELECT_MODEL_ID = "to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.to["+RELATIONSHIP_PRODUCTS+"].from.physicalid";
		String SELECT_MODEL_NAME = "to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.to["+RELATIONSHIP_PRODUCTS+"].from.name";
		String SELECT_MODEL_ID_MAINPRODUCT = "to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.to["+RELATIONSHIP_MAIN_PRODUCT+"].from.physicalid";
		String SELECT_MODEL_NAME_MAINPRODUCT = "to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.to["+RELATIONSHIP_MAIN_PRODUCT+"].from.name";
		String ATTRIBUTE_NODE_INDEX = PropertyUtil.getSchemaProperty(context, "attribute_NodeIndex");

    	StringList selectStmts = new StringList(14);
    	//get basic data for Build
    	selectStmts.add(DomainObject.SELECT_ID);
    	selectStmts.add(DomainObject.SELECT_TYPE);
    	selectStmts.add(DomainObject.SELECT_NAME);
    	selectStmts.add(DomainObject.SELECT_REVISION);
    	selectStmts.add("attribute["+ProductLineConstants.ATTRIBUTE_BUILD_UNIT_NUMBER+"]");
    	selectStmts.add("physicalid");

    	//get the Product data
    	selectStmts.add("to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.id");
    	selectStmts.add("to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.type");
        selectStmts.add("to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.name");
        selectStmts.add("to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.revision");
        selectStmts.add("to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.physicalid");
        selectStmts.add("to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.revindex");
        selectStmts.add("to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.attribute["+ATTRIBUTE_NODE_INDEX+"]");
        //get the Model context
    	selectStmts.add(SELECT_MODEL_ID);
    	selectStmts.add(SELECT_MODEL_NAME);

    	DomainObject buildObj = DomainObject.newInstance(context, buildId);
    	Map buildData = buildObj.getInfo(context, selectStmts);

    	//Create a MapList where the first in the list is the Build and the next is the Product
    	//This is the expected format by composition binary code
    	Map contextMap = new HashMap();
    	contextMap.put(DomainObject.SELECT_ID, (String)buildData.get(DomainObject.SELECT_ID));
    	contextMap.put(DomainObject.SELECT_TYPE, (String)buildData.get(DomainObject.SELECT_TYPE));
    	contextMap.put(DomainObject.SELECT_NAME, (String)buildData.get(DomainObject.SELECT_NAME));
    	contextMap.put(DomainObject.SELECT_REVISION, (String)buildData.get(DomainObject.SELECT_REVISION));
    	contextMap.put("physicalid", (String)buildData.get("physicalid"));
    	contextMap.put("revindex", (String)buildData.get("attribute["+ProductLineConstants.ATTRIBUTE_BUILD_UNIT_NUMBER+"]"));
    	//Product may be connected to model as Product or Main Product
    	String modelId = (String)buildData.get(SELECT_MODEL_ID);
    	String modelName = (String)buildData.get(SELECT_MODEL_NAME);
    	if (modelId == null || "null".equalsIgnoreCase(modelId) || modelId.length() == 0)
    	{
    		modelId = (String)buildData.get(SELECT_MODEL_ID_MAINPRODUCT);
    		modelName = (String)buildData.get(SELECT_MODEL_NAME_MAINPRODUCT);
    	}
    	contextMap.put("configContextId", modelId);
    	contextMap.put("configContextName", modelName);
    	productList.add(contextMap);

    	//Add map for Product data if any found
    	String productId = (String)buildData.get("to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.id");
    	if (productId != null && !"null".equalsIgnoreCase(productId) && productId.length() >0)
    	{
    		Map productMap = new HashMap();
    		productMap.put(DomainObject.SELECT_ID, (String)buildData.get("to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.id"));
    		productMap.put(DomainObject.SELECT_TYPE, (String)buildData.get("to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.type"));
    		productMap.put(DomainObject.SELECT_NAME, (String)buildData.get("to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.name"));
    		productMap.put(DomainObject.SELECT_REVISION, (String)buildData.get("to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.revision"));
    		productMap.put("physicalid", (String)buildData.get("to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.physicalid"));

    		//get the Node Index attribute
    		productMap.put("revindex", (String)buildData.get("to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.attribute["+ATTRIBUTE_NODE_INDEX+"]"));
    		productMap.put("configContextId", modelId);
    		productMap.put("configContextName", modelName);
    		productList.add(productMap);
    	}
    	return productList;
    }
//oj3 for WIDGET change
    /**
	 * Method to give the date after specified days to the current data
	 * @param days
	 * @return
	 */
	public String getDateAfter(int days){
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		int day1 = 0 - days;
		cal.add(Calendar.DAY_OF_MONTH, day1);
		String dateNumofDaysBack = (cal.get(Calendar.MONTH) + 1) + "/"+ cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR);
		return dateNumofDaysBack;
	}
	/**
	 * Returns IDs for UNT Lots and Builds Widget	
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public MapList getUNTWidgetData(Context context, String[] args) throws Exception {
		Map<String, Object> programMap = (Map<String, Object>) JPO.unpackArgs(args);
        Map jpoArgs = (Map) programMap.get(ServiceBase.JPO_ARGS);
		String loggedInUser= context.getUser();
        String selType       = (String) jpoArgs.get("type");
        String SuiteKey      = (String) jpoArgs.get("SuiteKey");

		String  prefdays = EnoviaResourceBundle.getProperty(context, "emxUnitTracking.MyView.DefaultNoOfDays");
	    int days 		 = Integer.parseInt(prefdays);
		String dateNumofDaysBack=getDateAfter(days);

		StringList objectSelects = new StringList(5);
		objectSelects.addElement(SELECT_ID);
		objectSelects.addElement(SELECT_TYPE);
		objectSelects.addElement(SELECT_POLICY);
		objectSelects.addElement(SELECT_CURRENT);
		objectSelects.addElement(SELECT_MODIFIED);
		
		String POLICY_LOT = PropertyUtil.getSchemaProperty(context,"policy_Lot");
		String POLICY_PART_MARKING = PropertyUtil.getSchemaProperty(context,"policy_PartMarking");
		
	    String STATE_LOT_OBSOLETE  = PropertyUtil.getSchemaProperty(context,"policy",POLICY_LOT,"state_Obsolete");
	    String STATE_PM_SUPERSEDED = PropertyUtil.getSchemaProperty(context,"policy",POLICY_PART_MARKING,"state_Superseded");
		String stateWhereClause    = SELECT_CURRENT+" != '"+STATE_LOT_OBSOLETE+"' && "+SELECT_CURRENT+" != '"+STATE_PM_SUPERSEDED+"'";

		String whereExpression  = "("+SELECT_OWNER+"=='"+loggedInUser+"'||"+SELECT_ORIGINATOR+"=='"+loggedInUser+"') && ("+SELECT_MODIFIED+">='"+dateNumofDaysBack+"') " +
								 "&& ("+stateWhereClause+")";
		
		
		
		return DomainObject.findObjects(context, selType, QUERY_WILDCARD, // namepattern
															QUERY_WILDCARD, 		// revpattern
															"*", 					// owner pattern
															QUERY_WILDCARD, 		// vault pattern
															whereExpression, 		// where exp
															true, objectSelects);

	}
	    
public void getReferenceDocumentsCountAndLabel(Context context, String[] args) throws Exception {		
    try {
	      Map programMap     = (Map) JPO.unpackArgs(args);
	      String fieldKey    = (String) programMap.get(ServiceBase.JPO_WIDGET_FIELD_KEY);
	      MapList objectList = (MapList) programMap.get(ServiceBase.JPO_WIDGET_DATA);    	    
	      String MATRIX_DELIMITER = matrix.db.SelectConstants.cSelectDelimiter;
		  String label = EnoviaResourceBundle.getProperty(context,  "emxUnitTrackingStringResource", context.getLocale(),"emxUnitTracking.Widget.ReferenceDocumentLabel");
		  String mouseOverText = EnoviaResourceBundle.getProperty(context,  "emxUnitTrackingStringResource", context.getLocale(),"emxUnitTracking.Widget.ReferenceDocumentMouseOverText");
		  
		  Map<String, String> widgetArgs = (Map<String, String>) programMap.get(ServiceBase.JPO_WIDGET_ARGS);
		  String ARG_BASE_URI = widgetArgs.get(ServiceBase.ARG_BASE_URI);

		  Map<String, String> objInfo;
	      StringList relatedObjList;String id;String count;
	      if(null != objectList) {
	    	  for (int i=0; i < objectList.size(); i++) {
	              objInfo  = (Map<String, String>) objectList.get(i);
	              id = objInfo.get("id");
            	  relatedObjList = getListValue(objInfo.get(fieldKey), MATRIX_DELIMITER);	            	  
	              count = relatedObjList != null ? String.valueOf(relatedObjList.size()) : "0";
	              objInfo.put(fieldKey, getHTMLForCountAndLabel(count,label,id,mouseOverText,"Lot".equalsIgnoreCase(objInfo.get("type")) ? "UNTLotReferenceDocumentTreeCategory" : "PLCBuildReferenceDocumentTreeCategory",ARG_BASE_URI));             
          	}
  	  	}
    }
    catch (Exception ex) {
    	System.out.println("Exception occurred in emxBuildBase :: getReferenceDocumentsCountAndLabel");
    	ex.printStackTrace();
    }
}

public StringList getListValue(Object data,String delimiter) {
		return (data == null) ? new StringList(0) : ((data instanceof String) ? FrameworkUtil.split((String)data, delimiter) : (StringList) data);
	}

public void getPartMarkingSpecificationsCountAndLabel(Context context, String[] args) throws Exception {		
    try {
	      Map programMap     = (Map) JPO.unpackArgs(args);
	      String fieldKey    = (String) programMap.get(ServiceBase.JPO_WIDGET_FIELD_KEY);
	      MapList objectList = (MapList) programMap.get(ServiceBase.JPO_WIDGET_DATA);    	    
	      String MATRIX_DELIMITER = matrix.db.SelectConstants.cSelectDelimiter;
		  String label = EnoviaResourceBundle.getProperty(context,  "emxUnitTrackingStringResource", context.getLocale(),"emxUnitTracking.Widget.MarkingSpecficationsLabel");
		  String mouseOverText = EnoviaResourceBundle.getProperty(context,  "emxUnitTrackingStringResource", context.getLocale(),"emxUnitTracking.Widget.PartMarkingSpecificationsMouseOverText");
		  Map<String, String> widgetArgs = (Map<String, String>) programMap.get(ServiceBase.JPO_WIDGET_ARGS);
		  String ARG_BASE_URI = widgetArgs.get(ServiceBase.ARG_BASE_URI);

		  
		  Map<String, String> objInfo;
	      StringList relatedObjList;String id;String count;
	      if(null != objectList) {
	    	  for (int i=0; i < objectList.size(); i++) {
	              objInfo  = (Map<String, String>) objectList.get(i);
	              id = objInfo.get("id");
            	  relatedObjList = relatedObjList = getListValue(objInfo.get(fieldKey), MATRIX_DELIMITER);	            	  
	              count = relatedObjList != null ? String.valueOf(relatedObjList.size()) : "0";
	              objInfo.put(fieldKey, getHTMLForCountAndLabel(count,label,id,mouseOverText,"UNTPartMarkingSpecification",ARG_BASE_URI));             
          	}
  	  	}
    }
    catch (Exception ex) {
    	System.out.println("Exception occurred in emxBuildBase :: getReferenceDocumentsCountAndLabel");
    	ex.printStackTrace();
    }
}
private String getHTMLForCountAndLabel(String count,String label,String id,String mouseOverText,String categoryTreeName,String baseURI) {
	  //return "<a target=\"content\" href=\""+baseURI+"common/emxSecurityContextSelection.jsp?DefaultCategory="+categoryTreeName+"&amp;objectId="+id+"\"><span><div class=\"image-container\" style=\"width: 75px;text-decoration:none;text-align: center;font-weight: bold;color: #333333;padding: 6px 8px;\" title=\""+mouseOverText+"\">"+count+"<br>"+label+"</div></span></a>";
	return "<div style=\"width: 75px;text-decoration:none;text-align: center;font-size:13px;font-weight: bold;color: #333333;padding: 6px 8px;-moz-box-sizing: border-box;background: linear-gradient(to bottom, #EDF2F4 0%, #94B1BE 100%) repeat scroll 0 0 transparent;border: 1px solid #7A7A7A;box-shadow: 0 0 4px 1px #666666;height: 62px;margin-left: auto;margin-right: auto;\" title=\""+mouseOverText+"\">"+count+"<br>"+label+"</div>";
}
//Widget-End	
/**
 * This method returns the status of the build i.e., Allocated or
 * Unallocated based on the model build relationship.
 *
 * @param context
 *            the eMatrix <code>Context</code> object
 * @param args
 *            holds the parammap, objectlist
 * @return Vector
 * @throws Exception
 *             if operation fails
 */
public StringList getBuildStatus(Context context, String[] args)
		throws Exception {
	HashMap programMap = (HashMap) JPO.unpackArgs(args);
	
	MapList objList = (MapList) programMap.get("objectList");
	StringList columnVals = new StringList(objList.size());
	Iterator i = objList.iterator();
	i18nNow i18nnow = new i18nNow();
	String language = context.getSession().getLanguage();
	String strAllocated = EnoviaResourceBundle.getProperty(context ,
			"emxProductLineStringResource", context.getLocale(),
			"emxProductLine.Units.BuildStatusAllocated");
	String strUnallocated = EnoviaResourceBundle.getProperty(context ,
			"emxProductLineStringResource", context.getLocale(),
			"emxProductLine.Units.BuildStatusUnAllocated");

	String strProductName = "";

	while (i.hasNext()) {
		Map m = (Map) i.next();
		String strbuildID = (String) m.get("id");
		DomainObject domObj = new DomainObject(strbuildID);
		strProductName = domObj.getInfo(context, "to["
				+ ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
				+ "].from.id");
		if (strProductName != null && strProductName.length() > 0) {
			columnVals.addElement(strAllocated);
		} else {
			columnVals.addElement(strUnallocated);
		}
	}

	return columnVals;
}	
}// End of class
