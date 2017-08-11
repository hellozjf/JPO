/*
 *  emxApplicabilityDecisionBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /java/JPOsrc/base/${CLASSNAME}.java 1.2.1.1 Tue Dec 30 21:55:42 2008 GMT dmcelhinney Experimental$
 *
 */


import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.AttributeType;
import matrix.db.AttributeTypeList;
import matrix.db.BusinessInterface;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.RelationshipType;
import matrix.db.State;
import matrix.db.StateList;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.enterprisechange.Decision;
import com.matrixone.apps.enterprisechange.EnterpriseChangeConstants;
import com.matrixone.apps.enterprisechange.EnterpriseChangeUtil;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.productline.ProductLineUtil;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.Namespace;
import com.matrixone.jdom.input.SAXBuilder;


/**
 * The <code>emxApplicabilityDecisionBase</code> class contains methods related to Decision admin type.
 * @version EnterpriseChange R207 - Copyright (c) 2008-2016, Dassault Systemes, Inc.
 *
 */
public class emxApplicabilityDecisionBase_mxJPO extends emxDecision_mxJPO
{

	protected String RANGE_YES = "";
	protected String RANGE_NO = "";
	protected String UPWARD_COMPATIBILITY_RANGE_YES = "";
	protected String UPWARD_COMPATIBILITY_RANGE_NO = "";
	//Added for IR-046491V6R2011
	private int COUNT = 0;
	/**
	 * Default Constructor.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R207
	 * @grade 0
	 */
	public emxApplicabilityDecisionBase_mxJPO (Context context, String[] args) throws Exception
	{
		super(context, args);
		//use default translation for all database comparisons
		RANGE_YES = EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Track_Applicability.Yes","en");
		RANGE_NO = EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Track_Applicability.No","en");
		UPWARD_COMPATIBILITY_RANGE_YES = EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Upward_Compatibility.Yes","en");
		UPWARD_COMPATIBILITY_RANGE_NO = EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Upward_Compatibility.No","en");

	}

	/**
	 * Main entry point.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return an integer status code (0 = success)
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R207
	 * @grade 0
	 */
	public int mxMain(Context context, String[] args) throws Exception
	{
		if (!context.isConnected())
		{
			String language = context.getSession().getLanguage();
			String strContentLabel = EnoviaResourceBundle.getProperty(context,"Components","emxComponents.Error.UnsupportedClient",language);
			throw new Exception(strContentLabel);
		}
		return 0;
	}

	/**
	 * Get the list of all Applicable Items on the context Decision.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId String
	 * @return bus ids of applicable items
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R207
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getApplicableItems (Context context, String[] args) throws Exception {

		try	{
			MapList applItemList = new MapList();
			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String decisionId = (String) paramMap.get("objectId");
			String strDiscipline = (String) paramMap.get("discipline");

			if (decisionId!=null && !decisionId.isEmpty()) {
				Decision decsion = new Decision(decisionId);
				if (strDiscipline!=null && !strDiscipline.isEmpty()) {
					applItemList = decsion.getApplicableItems(context, new StringList(PropertyUtil.getSchemaProperty(context,strDiscipline)));
				} else {
					applItemList = decsion.getApplicableItems(context, null);
				}
			}
			return applItemList;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Get the list of all Applicable Items for the given object..
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectId String
	 * @return bus ids of applicable items
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R207
	 * @deprecated : since R418 Use Decision.getApplicableItems(Context context, StringList selectedChangeDisciplines)
	 */

	public MapList getApplicableItems (Context context, String objectId) throws Exception {
		try	{
			MapList applItemList = new MapList();
			if (objectId!=null && !objectId.isEmpty()) {
				applItemList = new Decision(objectId).getApplicableItems(context, null);
			}
			return applItemList;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Add Applicable Items on the context Decision.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId String
	 *        1 - emxTableRowId String array of selected items to add
	 * @return String containing error message or blank on success.
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R207
	 */

	public String addApplicableItems (Context context, String[] args) throws Exception {
		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap= (HashMap) programMap.get("paramMap");

			String errorMsg = "";
			String decisionId = (String) paramMap.get("objectId");
			String[] emxTableRowId = (String[]) paramMap.get("emxTableRowId");
			String selectedChangeDiscipline = (String) paramMap.get("changeDiscipline");

			int intNumApplicableItems = emxTableRowId.length;
			DomainObject decisionObj = DomainObject.newInstance(context, decisionId);
			DomainRelationship doTargetAfectedItemRel = null;
			String strCommand = "";
			String strMessage = "";
			String relId = "";
			String strDefaultUpwardCompatibility=EnoviaResourceBundle.getProperty(context,"emxEnterpriseChange.UpwardCompatibility.Default");
			if (strDefaultUpwardCompatibility != null && "Yes".equalsIgnoreCase(strDefaultUpwardCompatibility)) {
				strDefaultUpwardCompatibility = UPWARD_COMPATIBILITY_RANGE_YES;
			} else {
				strDefaultUpwardCompatibility = UPWARD_COMPATIBILITY_RANGE_NO;
			}
			try {
				StringList authorizedChangeDisciplines = new StringList();
				if (selectedChangeDiscipline!=null && !selectedChangeDiscipline.isEmpty()) {
					authorizedChangeDisciplines.addElement(selectedChangeDiscipline);
				} else {
					authorizedChangeDisciplines = new Decision(decisionId).getAuthorizedChangeDisciplinesForApplicability(context);
				}
				String appItemId = "";
				DomainObject appItemObj = null;
				for(int i=0; i < intNumApplicableItems; i++)
				{
					appItemId = emxTableRowId[i];
					appItemObj = DomainObject.newInstance(context, appItemId);
					doTargetAfectedItemRel = DomainRelationship.connect(context, decisionObj, EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM, appItemObj);

					//add interface attribute for Upward Compatibility
					//strCommand = "modify connection " + doTargetAfectedItemRel +" add interface \'"+ EnterpriseChangeConstants.INTERFACE_UPWARD_COMPATIBILITY + "\'";
					strCommand = "modify connection $1 add interface $2";
					strMessage = MqlUtil.mqlCommand(context,strCommand,doTargetAfectedItemRel.toString(),EnterpriseChangeConstants.INTERFACE_UPWARD_COMPATIBILITY);

					//set default value for Upward Compatibility
					doTargetAfectedItemRel.setAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_UPWARD_COMPATIBILITY, strDefaultUpwardCompatibility);

					String strInterfaceName = EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE;
					//Check if an the change discipline interface has been already connected
					//strCommand = "print connection " + doTargetAfectedItemRel +" select interface["+ strInterfaceName + "] dump";
					strCommand = "print connection $1 select $2 dump";
					//If no interface --> add one
					if((com.matrixone.apps.domain.util.MqlUtil.mqlCommand(context,strCommand,doTargetAfectedItemRel.toString(),"interface["+ strInterfaceName + "]")).equalsIgnoreCase("false")){
						//String strAddInterface = "modify connection " + doTargetAfectedItemRel +" add interface \'"+ strInterfaceName + "\'";
						String strAddInterface = "modify connection $1 add interface $2";
						com.matrixone.apps.domain.util.MqlUtil.mqlCommand(context,strAddInterface,doTargetAfectedItemRel.toString(),strInterfaceName);
					}

					StringList changeDisciplines = EnterpriseChangeUtil.getChangeDisciplines(context);
					StringList associatedChangeDisciplines = EnterpriseChangeUtil.getObjectChangeDisciplinesAssociation(context, appItemId);
					Iterator<String> changeDisciplinesItr = changeDisciplines.iterator();
					while (changeDisciplinesItr.hasNext()) {
						String changeDiscipline = changeDisciplinesItr.next();
						if (changeDiscipline!=null && !changeDiscipline.isEmpty()) {
							if (authorizedChangeDisciplines.contains(changeDiscipline)) {
								if (associatedChangeDisciplines.contains(changeDiscipline)) {
									doTargetAfectedItemRel.setAttributeValue(context, changeDiscipline, EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE);
								} else {
									doTargetAfectedItemRel.setAttributeValue(context, changeDiscipline, EnterpriseChangeConstants.CHANGE_DISCIPLINE_FALSE);
								}
							} else {
								doTargetAfectedItemRel.setAttributeValue(context, changeDiscipline, EnterpriseChangeConstants.CHANGE_DISCIPLINE_FALSE);
							}
						}
					}//End of while
				}
			} catch (Exception ex) {
				errorMsg = ex.getMessage();
			}
			return errorMsg;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Remove Applicable Items on the context Decision.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId String
	 * @return bus ids of applicable items
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R207
	 * @deprecated : since R418 Use removeDecisionAppliesTo(matrix.db.Context,java.lang.String[])
	 */

	public MapList removeApplicableItems (Context context, String[] args)
	throws Exception
	{
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap= (HashMap) programMap.get("paramMap");

		String decisionId = (String) paramMap.get("objectId");
		String[] emxTableRowId = (String[]) paramMap.get("emxTableRowId");
		int intNumApplicableItems = emxTableRowId.length;
		DomainObject decisionObj = DomainObject.newInstance(context, decisionId);
		DomainRelationship doTargetAfectedItemRel = null;
		try
		{
			String appItemId = "";
			DomainObject appItemObj = null;
			for(int i=0; i < intNumApplicableItems; i++)
			{
				appItemId = emxTableRowId[i];
				appItemObj = DomainObject.newInstance(context, appItemId);
				decisionObj.disconnect(context, new RelationshipType(EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM),true,appItemObj);
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}

		return null;
	}

	/**
	 * Gets the Subsequent Applicability for the Applicable Item..
	 * If the Applicable Item is the last revision, then displays 'True',
	 * Otherwise displays 'False'.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList of objectIds
	 * @return Vector of subsequent applicability flag values
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R207
	 * @deprecated since R418
	 */

	public Vector getSubsequentApplicabilityFlag (Context context, String[] args)
	throws Exception
	{
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		MapList objList = (MapList)paramMap.get("objectList");
		Vector columnVals = new Vector(objList.size());
		Locale locale = context.getLocale();
		String strLanguage = locale.getLanguage().toString();

		Iterator i = objList.iterator();
		String objectId = null;
		while (i.hasNext())
		{
			Map m = (Map) i.next();
			objectId = (String) m.get(DomainConstants.SELECT_ID);
			if(isLastRevision(context, objectId))
			{
				columnVals.add(EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Common.True",strLanguage));
			}
			else
			{
				columnVals.add(EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Common.False",strLanguage));
			}
		}
		return columnVals;
	}

	/**
	 * Returns true if the object is the last revision.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectId - String of the object id
	 * @return boolean true if last revision otherwise false
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R207
	 */

	private boolean isLastRevision (Context context, String objectId)
	throws Exception
	{
		DomainObject domObj = DomainObject.newInstance(context,objectId);
		// get the current revision of the object
		String currRev = domObj.getInfo(context, SELECT_REVISION);
		// get the last revision of the object
		String lastRev = domObj.getInfo(context, "last");
		if(currRev != null && lastRev != null && (currRev == lastRev || currRev.equals(lastRev)))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Returns true if the Propagate Applicability command should show
	 * This command will only be visible if there are Decisions
	 * with Applicability Tracking enabled related to the Change
	 * Task and the applicability has not already been propagated.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId String
	 * @return true to show the command otherwise false
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R207
	 */

	public boolean showPropagateApplicability (Context context, String[] args)
	throws Exception
	{
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String strChangeTaskObjId = (String)programMap.get("objectId");
		DomainObject changeTaskObj = DomainObject.newInstance(context, strChangeTaskObjId);

		String strAppProp = changeTaskObj.getAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_APPLICABILITY_PROPAGATED);
		if (strAppProp == null || "".equals(strAppProp) || RANGE_YES.equalsIgnoreCase(strAppProp)) {
			return false;
		} else {
			StringList selectStmts = new StringList(1);
			selectStmts.addElement(SELECT_ID);
			String whereclause = "(attribute[" + EnterpriseChangeConstants.ATTRIBUTE_TRACK_APPLICABILITY + "]" + "== \"" + RANGE_YES + "\")" ;

			//get all Decisions connected where Track Applicability attribute is yes
			MapList mapList = changeTaskObj.getRelatedObjects(context,
					EnterpriseChangeConstants.RELATIONSHIP_DECISION_APPLIES_TO,            // relationship pattern
					TYPE_DECISION,                      // object pattern
					selectStmts,                        // object selects
					null,                               // relationship selects
					true,                               // to direction
					false,                              // from direction
					(short) 1,                          // recursion level
					whereclause,                        // object where clause
					null,                              // relationship where clause
					0);

			if (mapList.size() > 0) {
				//check states for Engineering Change type
				emxChangeTask_mxJPO changeTaskBase = new emxChangeTask_mxJPO();
				selectStmts.addElement(SELECT_CURRENT);

				MapList changeList = changeTaskObj.getRelatedObjects(context, RELATIONSHIP_TASK_DELIVERABLE, changeTaskBase.getTypePatternString(context),
						selectStmts, null, false, true, (short)1, null, null, 0);
				String changeId = "";
				if(!changeList.isEmpty()) {
					//Modified for IR-046491V6R2011
					boolean showCommand=false;
					for(int i=0;i<changeList.size();i++){
						Map map = (Map)changeList.get(i);
						changeId = (String)map.get(SELECT_ID);
						DomainObject changeObj = DomainObject.newInstance(context, changeId);

						//modified for IR-057187V6R2011x -- added Defect and Defect Action types
						if (changeObj.isKindOf(context, TYPE_ECO)|| changeObj.isKindOf(context, TYPE_ECR) || changeObj.isKindOf(context, EnterpriseChangeConstants.TYPE_DEFECT) || changeObj.isKindOf(context, EnterpriseChangeConstants.TYPE_DEFECT_ACTION)) {
							continue;
						} else {
							String changeState = (String) map.get(SELECT_CURRENT);
							if (changeState.equals(EnterpriseChangeConstants.STATE_ECA_SHARED)) {
								showCommand = false;
								break;
							} else {
								showCommand = true;
							}
						}
					}
					return showCommand;
				}
			}
		}
		return false;
	}

	/**
	 * Propagate Applicability from the Decision to Change Tasks
	 * Sets the "Applicability Propagated" attribute on the Change Task to "Yes".
	 * Then the "Applicable Items" that are connected to the Decision will be connected
	 * to the Change Task's Change object using the "Applicable Item" relationship
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId String
	 * @return String with error message or blank on success
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R207
	 * @deprecated : since R418 Use propagateApplicabilityForOpenness(matrix.db.Context,java.lang.String[])
	 */

	public String propagateApplicability (Context context, String[] args) throws Exception{
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String strChangeTaskObjId = (String)programMap.get("objectId");
		//optimistic - set success message
		//Modified for IR-046491V6R2011
		String errorMessage = "";
		boolean bError = false;
		boolean decisionReleased=false;
		DomainObject changeTaskObj = DomainObject.newInstance(context);

		try{
			if(strChangeTaskObjId != null && !"null".equalsIgnoreCase(strChangeTaskObjId) && 	!"".equals(strChangeTaskObjId)){
				changeTaskObj.setId(strChangeTaskObjId);
			}

			String strAppProp = changeTaskObj.getAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_APPLICABILITY_PROPAGATED);
			if(strAppProp != null && RANGE_YES.equalsIgnoreCase(strAppProp)){
				errorMessage = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.AlreadyPropagated",context.getSession().getLanguage());
				bError = true;
				return errorMessage;
			}

			String strInterfaceName = EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE;
			BusinessInterface busInterface = new BusinessInterface(strInterfaceName, context.getVault());
			AttributeTypeList listChangeDisciplineAttributes = busInterface.getAttributeTypes(context);

			Iterator listChangeDisciplineAttributesItr = listChangeDisciplineAttributes.iterator();
			Boolean hasNoChangeDeliverable = true;
			while(listChangeDisciplineAttributesItr.hasNext()){
				String changeDisciplineName = ((AttributeType) listChangeDisciplineAttributesItr.next()).getName();
				String changeDisciplineValue = changeTaskObj.getAttributeValue(context, changeDisciplineName);
				String changeDisciplineNameSmall = changeDisciplineName.replaceAll(" ", "");
				//Check if Change Task is set for the Change Discipline
				//If yes continue propagation process
				//If no don't propagate for this applicability
				if(changeDisciplineValue.equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE)){

					//Get deliverable types for the Change Discipline
					String deliverableTypes = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChange." + changeDisciplineNameSmall + ".DeliverableTypes");
					if(deliverableTypes!=null && !deliverableTypes.equalsIgnoreCase("")){
						//Transform deliverableTypes with the proper syntax (type_XXX into XXX)
						String strDeliverableTypes = "";
						StringList listDeliverableTypes = FrameworkUtil.split(deliverableTypes, ",");
						if(listDeliverableTypes!=null && !listDeliverableTypes.isEmpty()){
							Iterator listDeliverableTypesItr = listDeliverableTypes.iterator();
							while(listDeliverableTypesItr.hasNext()){
								String schemaPropertyTemp = PropertyUtil.getSchemaProperty(context,listDeliverableTypesItr.next().toString());
								if(schemaPropertyTemp!=null && !schemaPropertyTemp.isEmpty()){
									strDeliverableTypes += schemaPropertyTemp + ",";
								}
							}
						}

						//get the Change Object id
						//no longer a 1-1 relationship - must expand for Change types
						//Documents are also Deliverables but there should be only one change type
						//Begin Bug#374614
						emxChangeTask_mxJPO changeTaskBase = new emxChangeTask_mxJPO();
						//Added for IR-046491V6R2011
						boolean hasEcoEcr = false;
						HashSet changeType = new HashSet();
						//End of IR-046491V6R2011
						StringList selectStmts = new StringList(1);
						selectStmts.addElement(SELECT_ID);
						selectStmts.addElement(SELECT_CURRENT);
						//Modified for IR-046491V6R2011
						MapList mapList = changeTaskObj.getRelatedObjects(context, RELATIONSHIP_TASK_DELIVERABLE, strDeliverableTypes,
								selectStmts, null, false, true, (short)1, null, null, 0);

						String changeId = "";
						if(!mapList.isEmpty()){
							hasNoChangeDeliverable = false;
							//IR-041102
							for(Iterator iterator = mapList.iterator(); iterator.hasNext();){
								Map map = (Map) iterator.next();
								changeId = (String)map.get(SELECT_ID);

								//End Bug#374614

								if(changeId == null || "null".equalsIgnoreCase(changeId) || "".equals(changeId)){
									//No Deliverable - can't propagate
									errorMessage = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.NoDeliverable",context.getSession().getLanguage());
									bError = true;
									return errorMessage;
								}

								DomainObject changeObj = DomainObject.newInstance(context, changeId);
								//modified for IR-057187V6R2011x -- added Defect and Defect Action types
								if(changeObj.isKindOf(context, TYPE_ECR) || changeObj.isKindOf(context, TYPE_ECO) || changeObj.isKindOf(context, EnterpriseChangeConstants.TYPE_DEFECT) || changeObj.isKindOf(context, EnterpriseChangeConstants.TYPE_DEFECT_ACTION)){
									//Modified for IR-046491V6R2011
									//Do not allow propagation for ECR and ECO at this time
									bError = true;
									hasEcoEcr = true;
									continue;
									// return errorMessage;
								}
								//Get the latest Released Decision with Track Applicability for the Change Task
								String decisionId = getLatestReleasedDecision(context, strChangeTaskObjId);
								if(decisionId.length()>0){
									//Added for IR-020195V6R2011
									decisionReleased=true;
									//get applicable items for this decision
									HashMap paramMap = new HashMap();
									paramMap.put("objectId", decisionId);
									paramMap.put("discipline", FrameworkUtil.getAliasForAdmin(context, "attribute", changeDisciplineName, true));
									String[] methodargs = JPO.packArgs(paramMap);

									MapList applItems =  getApplicableItems (context, methodargs);
									//MapList applItems =  getApplicableItems (context, decisionId);

									//handle interoperability using command object
									//Added for IR-046491V6R2011
									String changeState = (String)map.get(SELECT_CURRENT);
									if(changeObj.isKindOf(context, EnterpriseChangeConstants.TYPE_ENGINEERING_CHANGE)&&!(changeState.equals(EnterpriseChangeConstants.STATE_ENGINEERING_CHANGE_SUBMIT) || changeState.equals(EnterpriseChangeConstants.STATE_ENGINEERING_CHANGE_EVALUATE))) {
										continue;
									}
									if(changeObj.isKindOf(context, EnterpriseChangeConstants.TYPE_ENGINEERING_CHANGE)){
										String ecType = EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Type.Engineering_Change",context.getSession().getLanguage());
										changeType.add(ecType);

									}else{
										String actualEcaType = changeObj.getInfo(context, SELECT_TYPE);
										String propertyEntryChangeTypeKey = "emxFramework.Type." + actualEcaType ;
										String displayEcaType = EnoviaResourceBundle.getProperty(context,"Framework",propertyEntryChangeTypeKey,context.getSession().getLanguage());
										if(!propertyEntryChangeTypeKey.equalsIgnoreCase(displayEcaType)){
											changeType.add(displayEcaType);
										}else{
											changeType.add(actualEcaType);
										}
									}
									//Added for IR-046491V6R2011
									COUNT++;
									//End of IR-046491V6R2011
									dynamicPropagateApplicability(context, changeId, decisionId, applItems);
								}else{
									bError=false;
									break;
								}
							}//for
						}
						if(hasNoChangeDeliverable){
							bError = true;
							errorMessage = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.NoDeliverable",context.getSession().getLanguage());
						}
						//Added for IR-046491V6R2011
						if(!changeType.isEmpty()){
							bError=false;
							errorMessage = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.SuccessfullyPropagated" ,context.getSession().getLanguage());
							Iterator itr=changeType.iterator();
							while(itr.hasNext()){
								errorMessage = errorMessage + itr.next();
								if(itr.hasNext()){
									errorMessage = errorMessage + ",";
								}else{
									errorMessage = errorMessage + ".";
								}
							}
						}
						if(hasEcoEcr){
							errorMessage = errorMessage + EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.PropagateApplicabilityNotSupportedForEcoEcr" ,context.getSession().getLanguage());
						}
						//End of IR-046491V6R2011
						//Added for IR-020195V6R2011
						if(!decisionReleased && !bError){
							errorMessage=EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.PropagationEnabled" ,context.getSession().getLanguage());
						}
					}
				}
			}
		}catch (Exception ex){
			errorMessage = ex.getMessage();
			bError = true;
		}
		//Added for IR-057309V6R2011x
		finally{
			//String sErrorMsg=i18nNow.getI18nString("emxEnterpriseChange.Error.IncompatibleUpwardCompatibility","emxEnterpriseChangeStringResource", context.getSession().getLanguage());
			//String isErrorMsg=PropertyUtil.getGlobalRPEValue(context, "ErrorMsg");
			//if(isErrorMsg.equalsIgnoreCase("Yes")&& isErrorMsg != null && !"".equals(isErrorMsg)){
			//	${CLASS:emxContextUtil}.mqlNotice(context,sErrorMsg);
			//}
		}
		//End of IR-057309V6R2011x
		if(!bError){
			changeTaskObj.setAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_APPLICABILITY_PROPAGATED, RANGE_YES);
		}
		return errorMessage;
	}

	/**
	 * Propagate Applicability from the Decision to Change Tasks
	 * Sets the "Applicability Propagated" attribute on the Change Task to "Yes".
	 * Then the "Applicable Items" that are connected to the Decision will be connected
	 * to the Change Task's Change object using the "Applicable Item" relationship
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId String
	 * @return String with error message or blank on success
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */


	public MapList propagateApplicabilityForOpenness(Context context, String[] args)throws Exception{
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		MapList mlError = new MapList();
		String strChangeTaskObjId = "";
		StringList slobjectId = new StringList();
		String strErrorMsg = "";
		Object objObjectId = (Object)programMap.get("objectId");

		if(objObjectId instanceof Map ){
			Map mapObject = (Map)objObjectId;
			slobjectId = (StringList)mapObject.get("slobjectId");
		}else if(objObjectId instanceof String){
			strChangeTaskObjId = (String)programMap.get("objectId");
		}else{

			strChangeTaskObjId = (String)programMap.get("objectId");
			slobjectId.add(strChangeTaskObjId);
		}

		String bPropagate = (String)programMap.get("bPropagate");
		String errorMessage = "";
		boolean bError = false;
		boolean decisionReleased=false;
		DomainObject changeTaskObj = DomainObject.newInstance(context);
		StringList slPropagateOjects = new StringList();
		StringList slNoPropagateOjects = new StringList();

		try{
			for(int c=0;c<slobjectId.size();c++){
				strChangeTaskObjId = (String)slobjectId.get(c);


				if(strChangeTaskObjId != null && !"null".equalsIgnoreCase(strChangeTaskObjId) && 	!"".equals(strChangeTaskObjId)){
					changeTaskObj.setId(strChangeTaskObjId);
				}
				String strAppProp = changeTaskObj.getAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_APPLICABILITY_PROPAGATED);
				if(strAppProp != null && RANGE_YES.equalsIgnoreCase(strAppProp)){
					errorMessage = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.AlreadyPropagated",context.getSession().getLanguage());
					bError = true;
				}
				else{
					String strInterfaceName = EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE;
					BusinessInterface busInterface = new BusinessInterface(strInterfaceName, context.getVault());
					AttributeTypeList listChangeDisciplineAttributes = busInterface.getAttributeTypes(context);

					Iterator listChangeDisciplineAttributesItr = listChangeDisciplineAttributes.iterator();
					Boolean hasNoChangeDeliverable = true;
					String changeDisciplineName = "";

					StringList selectStmts = new StringList(1);
					selectStmts.addElement(SELECT_ID);
					selectStmts.addElement(SELECT_NAME);
					selectStmts.addElement(SELECT_CURRENT);
					selectStmts.addElement(SELECT_TYPE);
					MapList mapTaskDeliverableList = changeTaskObj.getRelatedObjects(context,
							RELATIONSHIP_TASK_DELIVERABLE,
							"*", // * type pattern becoz there can be different types from diff products, EC,ECA,Defect,ECO,etc
							selectStmts,
							null,
							false,
							true,
							(short)1,
							null,
							null,
							0);

					MapList mlSupportedDeliverableTypes = new MapList();
					MapList mlNonSupportedDeliverableTypes = new MapList();

					for(Iterator iterator = mapTaskDeliverableList.iterator(); iterator.hasNext();){
						Map map = (Map) iterator.next();
						String strTaskDeliverableId = (String)map.get(SELECT_ID);
						String strTaskDeliverabletype = (String)map.get(SELECT_TYPE);
						String strTaskDeliverableName = (String)map.get(SELECT_NAME);
						String strTaskDeliverableState = (String)map.get(SELECT_CURRENT);
						boolean issupported = false;
						// Decide the change object is supported for applicability or not
						issupported = supportApplicabiilty(context,strTaskDeliverableId,strTaskDeliverabletype);
						if(issupported){
							Map mapsupported = new HashMap();
							mapsupported.put("strTaskDeliverableId",strTaskDeliverableId);
							mapsupported.put("strChangeObjectType",strTaskDeliverabletype);
							mapsupported.put("changeObjectName",strTaskDeliverableName);
							mapsupported.put("changeState",strTaskDeliverableState);
							mlSupportedDeliverableTypes.add(mapsupported);
						}else{
							Map mapnonsupported = new HashMap();
							mapnonsupported.put("strTaskDeliverableId",strTaskDeliverableId);
							mapnonsupported.put("strChangeObjectType",strTaskDeliverabletype);
							mapnonsupported.put("changeObjectName",strTaskDeliverableName);
							mapnonsupported.put("changeState",strTaskDeliverableState);
							mlNonSupportedDeliverableTypes.add(mapnonsupported);
						}
					}

					MapList mlcanPropagate = new MapList();
					MapList mlcanNotPropagate = new MapList();

					for(Iterator iterator = mlSupportedDeliverableTypes.iterator(); iterator.hasNext();){
						Map map = (Map) iterator.next();
						String strTaskDeliverableId = (String)map.get("strTaskDeliverableId");
						String strTaskDeliverabletype = (String)map.get("strChangeObjectType");
						String strTaskDeliverableName = (String)map.get("changeObjectName");
						String strTaskDeliverableState = (String)map.get("changeState");
						String strDecisionId = getLatestReleasedDecision(context, strChangeTaskObjId);
						MapList mlApplicableItems = new MapList();


						if(strDecisionId.length()>0){
							//Added for IR-020195V6R2011
							decisionReleased=true;
							//  get applicable items for this decision
							StringList changeDisciplinesAssociation = EnterpriseChangeUtil.getObjectChangeDisciplinesAssociation(context, strTaskDeliverableId);
							Iterator<String> changeDisciplinesAssociationItr = changeDisciplinesAssociation.iterator();
							while (changeDisciplinesAssociationItr.hasNext()) {
								String changeDisciplineAssociation = changeDisciplinesAssociationItr.next();
								if (changeDisciplineAssociation!=null && !changeDisciplineAssociation.isEmpty()) {
									HashMap paramMap = new HashMap();
									paramMap.put("objectId", strDecisionId);
									paramMap.put("discipline", FrameworkUtil.getAliasForAdmin(context, "attribute", changeDisciplineAssociation, true));
									mlApplicableItems.addAll(getApplicableItems (context, JPO.packArgs(paramMap)));
								}
							}
						}
						//Decide the supported change object is allowed for propagation or not
						String strCanPropagate  = canPropagate(context,strTaskDeliverableId,strTaskDeliverabletype, strChangeTaskObjId);

						if(null == strCanPropagate || "null".equals(strCanPropagate) || strCanPropagate.length() == 0){
							boolean isPropagationAllowed = true;
							Map mapCanPropagate= new HashMap();
							mapCanPropagate.put("strTaskDeliverableId", strTaskDeliverableId);
							mapCanPropagate.put("strChangeObjectType", strTaskDeliverabletype);
							mapCanPropagate.put("changeObjectName",strTaskDeliverableName);
							mapCanPropagate.put("decisionId",strDecisionId);
							mapCanPropagate.put("changeState", strTaskDeliverableState);
							mapCanPropagate.put("mlApplicableItems", mlApplicableItems);
							mapCanPropagate.put("decisionReleased", decisionReleased);
							mapCanPropagate.put("isPropagationAllowed", isPropagationAllowed);
							mapCanPropagate.put("bPropagate",bPropagate);
							mlcanPropagate.add(mapCanPropagate);
							mlError.add(mapCanPropagate);
						}else{
							boolean isPropagationAllowed = false;
							Map mapCanNotPropagate= new HashMap();
							mapCanNotPropagate.put("strTaskDeliverableId", strTaskDeliverableId);
							mapCanNotPropagate.put("strChangeObjectType", strTaskDeliverabletype);
							mapCanNotPropagate.put("changeObjectName",strTaskDeliverableName);
							mapCanNotPropagate.put("changeState", strTaskDeliverableState);
							mapCanNotPropagate.put("decisionId",strDecisionId);
							mapCanNotPropagate.put("mlApplicableItems", mlApplicableItems);
							mapCanNotPropagate.put("decisionReleased", decisionReleased);
							mapCanNotPropagate.put("isPropagationAllowed", isPropagationAllowed);
							mapCanNotPropagate.put("bPropagate",bPropagate);
							mlcanNotPropagate.add(mapCanNotPropagate);
							mlError.add(mapCanNotPropagate);
						}
					}
					if(mlcanNotPropagate.size() == 0 ){
						boolean isException = false;
						for(Iterator iterator = mlcanPropagate.iterator(); iterator.hasNext();){
								Map map = (Map) iterator.next();
								String strTaskDeliverableId = (String)map.get("strTaskDeliverableId");
								String strTaskDeliverabletype = (String)map.get("strChangeObjectType");
								MapList mlApplicableItems = (MapList)map.get("mlApplicableItems");
								String decisionId = (String)map.get("decisionId");
							if (decisionId!=null && !decisionId.isEmpty()) {
								try{
								dynamicPropagateApplicability(context, strTaskDeliverableId, decisionId, mlApplicableItems);
								}catch(Exception ex){
									String strErrMsg = ex.getMessage();
									map.put("strErrMsg", strErrMsg);
									mlError.remove(map.containsValue(strTaskDeliverableId));
									mlError.add(map);
									isException = true;
								}
							}
							if(!isException){
								bError = true;
							}
							}
						}
				}
			}
		}catch (Exception ex){
			ex.printStackTrace();
			errorMessage = ex.getMessage();
		}

		if(bError){
			changeTaskObj.setAttributeValue(context,EnterpriseChangeConstants.ATTRIBUTE_APPLICABILITY_PROPAGATED, RANGE_YES);
		}

		return mlError;
	}


	/**
	 * propagateApplicabilityReportDisplay will return the list of the change objects with the info whether they are allowed for propagation or not.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId String
	 * @return String with error message or blank on success
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList propagateApplicabilityReportDisplay (Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		MapList mlError = new MapList();

		String strChangeTaskObjId = (String)programMap.get("changeTaskIds");
		String bPropagate = (String)programMap.get("bPropagate");
		String bException = (String)programMap.get("bException");
		String errorMessage = "";
		boolean decisionReleased=false;

		String strCT = "";
			strCT = strChangeTaskObjId.substring(1,strChangeTaskObjId.length()-1);
			String[] strArray = strCT.split(",");			
		StringList slobjectId = new StringList();

			for(int c=0;c<strArray.length;c++){
				strChangeTaskObjId = (String)strArray[c].trim();
				slobjectId.add(strChangeTaskObjId);
			}

		try{
			for(int c=0;c<slobjectId.size();c++){
				strChangeTaskObjId = (String)slobjectId.get(c);
				strChangeTaskObjId.trim();
				DomainObject dmoChangeTask = new DomainObject(strChangeTaskObjId);

				StringList selectStmts = new StringList(1);
				selectStmts.addElement(SELECT_ID);
				selectStmts.addElement(SELECT_NAME);
				selectStmts.addElement(SELECT_CURRENT);
				selectStmts.addElement(SELECT_TYPE);
				MapList mapTaskDeliverableList = dmoChangeTask.getRelatedObjects(context,
						RELATIONSHIP_TASK_DELIVERABLE,
						"*", // * type pattern becoz there can be different types from diff products, EC,ECA,Defect,ECO,etc
						selectStmts,
						null,
						false,
						true,
						(short)1,
						null,
						null,
						0);

				MapList mlSupportedDeliverableTypes = new MapList();
				MapList mlNonSupportedDeliverableTypes = new MapList();

				for(Iterator iterator = mapTaskDeliverableList.iterator(); iterator.hasNext();){
					Map map = (Map) iterator.next();
					String strTaskDeliverableId = (String)map.get(SELECT_ID);
					String strTaskDeliverabletype = (String)map.get(SELECT_TYPE);
					String strTaskDeliverableName = (String)map.get(SELECT_NAME);
					String strTaskDeliverableState = (String)map.get(SELECT_CURRENT);
					boolean issupported = false;
					issupported = supportApplicabiilty(context,strTaskDeliverableId,strTaskDeliverabletype);
					if(issupported){
						Map mapsupported = new HashMap();
						mapsupported.put("strTaskDeliverableId",strTaskDeliverableId);
						mapsupported.put("strChangeObjectType",strTaskDeliverabletype);
						mapsupported.put("changeObjectName",strTaskDeliverableName);
						mapsupported.put("changeState",strTaskDeliverableState);
						mlSupportedDeliverableTypes.add(mapsupported);
					}else{
						Map mapnonsupported = new HashMap();
						mapnonsupported.put("strTaskDeliverableId",strTaskDeliverableId);
						mapnonsupported.put("strChangeObjectType",strTaskDeliverabletype);
						mapnonsupported.put("changeObjectName",strTaskDeliverableName);
						mapnonsupported.put("changeState",strTaskDeliverableState);
						mlNonSupportedDeliverableTypes.add(mapnonsupported);
					}
				}

				MapList mlcanPropagate = new MapList();
				MapList mlcanNotPropagate = new MapList();

				for(Iterator iterator = mlSupportedDeliverableTypes.iterator(); iterator.hasNext();){
					Map map = (Map) iterator.next();
					String strTaskDeliverableId = (String)map.get("strTaskDeliverableId");
					String strTaskDeliverabletype = (String)map.get("strChangeObjectType");
					String strTaskDeliverableName = (String)map.get("changeObjectName");
					String strTaskDeliverableState = (String)map.get("changeState");

					String strDecisionId = getLatestReleasedDecision(context, strChangeTaskObjId);
					if(strDecisionId.length()>0){
						decisionReleased=true;
					}
					String strCanPropagate  = canPropagate(context,strTaskDeliverableId,strTaskDeliverabletype, strChangeTaskObjId);

					if(null == strCanPropagate || "null".equals(strCanPropagate) || strCanPropagate.length() == 0){
						boolean isPropagationAllowed = true;
						Map mapCanPropagate= new HashMap();
						mapCanPropagate.put("strTaskDeliverableId", strTaskDeliverableId);
						mapCanPropagate.put("strChangeObjectType", strTaskDeliverabletype);
						mapCanPropagate.put("changeObjectName", strTaskDeliverableName);
						mapCanPropagate.put("changeState", strTaskDeliverableState);
						mapCanPropagate.put("decisionReleased", decisionReleased);
						mapCanPropagate.put("isPropagationAllowed", isPropagationAllowed);
						mapCanPropagate.put("bPropagate",bPropagate);
						mapCanPropagate.put("bException",bException);
						mlError.add(mapCanPropagate);
					}else{
						boolean isPropagationAllowed = false;
						Map mapCanNotPropagate= new HashMap();
						mapCanNotPropagate.put("strTaskDeliverableId", strTaskDeliverableId);
						mapCanNotPropagate.put("strChangeObjectType", strTaskDeliverabletype);
						mapCanNotPropagate.put("changeObjectName", strTaskDeliverableName);
						mapCanNotPropagate.put("changeState", strTaskDeliverableState);
						mapCanNotPropagate.put("decisionReleased", decisionReleased);
						mapCanNotPropagate.put("isPropagationAllowed", isPropagationAllowed);
						mapCanNotPropagate.put("bPropagate",bPropagate);
						mapCanNotPropagate.put("bException",bException);
						mlError.add(mapCanNotPropagate);
					}
				}
			}
		}catch (Exception ex){
			ex.printStackTrace();
			errorMessage = ex.getMessage();
		}

		return mlError;
	}


	/**
	 * getChangeObjectName will return the the name of the change object to display in the Propagation Report Dialog table.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId String
	 * @return Vector
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */

	public Vector getChangeObjectName (Context context, String[] args) throws Exception
	{
		Vector vResult = new Vector();
		try{
			Map mapProgram = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) mapProgram.get("objectList");
			Map mapObjectInfo = null;
			String itemTypeIcon = "";
			for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
			{
				mapObjectInfo = (Map) itrTableRows.next();
				String changeObjectName = (String)mapObjectInfo.get("changeObjectName");
				String strChangeObjectType = (String)mapObjectInfo.get("strChangeObjectType");

				// Find type icon
				String strTypeSymName = FrameworkUtil.getAliasForAdmin(context, "Type", strChangeObjectType, true);
				try{
					itemTypeIcon = EnoviaResourceBundle.getProperty(context,"emxFramework.smallIcon." + strTypeSymName);
				}catch(Exception ex){

				}
				String html = "";
				html = "<img src=\"../common/images/" +XSSUtil.encodeForHTMLAttribute(context,itemTypeIcon)+ "\" border=\"0\"  TITLE=\"" + XSSUtil.encodeForHTMLAttribute(context,changeObjectName) + "\"/>";
				html += XSSUtil.encodeForHTML(context,changeObjectName);
				vResult.add(html);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return vResult ;
	}


	/**
	 * getErrorMessage will return the the message to display in the Propagation Report Dialog table.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId String
	 * @return Vector
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public Vector getErrorMessage (Context context, String[] args) throws Exception
	{
		Vector vResult = new Vector();
		try{
			Map mapProgram = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) mapProgram.get("objectList");
			Map mapObjectInfo = null;
			for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
			{
				mapObjectInfo = (Map) itrTableRows.next();
				String strChangeObjectType = (String)mapObjectInfo.get("strChangeObjectType");
				String html = "";

				boolean isPropagationAllowed = (Boolean)mapObjectInfo.get("isPropagationAllowed");
				boolean isDecisionReleased = (Boolean)mapObjectInfo.get("decisionReleased");
				String  bPropagate = (String)mapObjectInfo.get("bPropagate");
				String changeObjectName = (String)mapObjectInfo.get("changeObjectName");
				String changeState = (String)mapObjectInfo.get("changeState");
				String bException = (String)mapObjectInfo.get("bException");
				if("true".equals(bException)){
					html = "The applicability propagation failed for this Change object";
					vResult.addElement(html);

				}else{
				if("false".equals(bPropagate)){
					if(!isPropagationAllowed){
						html = "The applicability can not be propagated.The"+strChangeObjectType+" is in " + changeState +" state";
						vResult.addElement(html);
					}else{
						html = "The applicability can be propagated.";
						vResult.addElement(html);
					}
				}else{
					if( !isDecisionReleased && isPropagationAllowed ){
						String sErrorMsg=EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.PropagationEnabled" ,context.getSession().getLanguage());
						html = sErrorMsg ;
						vResult.addElement(html);
					}else{
						String sErrorMsg=EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.ApplicabilityPropagated", context.getSession().getLanguage());
						html = sErrorMsg;
						vResult.addElement(html);
					}
				}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return vResult ;
	}

	/**
	 * getIcon will return the icon to display in the Propagation Report Dialog table.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId String
	 * @return Vector
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public Vector getChangeObjectIcon (Context context, String[] args) throws Exception
	{
		//XSSOK -hardcoded
		Vector vResult = new Vector();
		try{
			Map mapProgram = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) mapProgram.get("objectList");
			Map mapObjectInfo = null;
			for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
			{
				mapObjectInfo = (Map) itrTableRows.next();
				String strChangeObjectType = (String)mapObjectInfo.get("strChangeObjectType");
				String html = "";
				boolean isPropagationAllowed = (Boolean)mapObjectInfo.get("isPropagationAllowed");
				boolean isDecisionReleased = (Boolean)mapObjectInfo.get("decisionReleased");
				String changeObjectName = (String)mapObjectInfo.get("changeObjectName");
				String changeState = (String)mapObjectInfo.get("changeState");
				String bException = (String)mapObjectInfo.get("bException");

				if("true".equals(bException)){
					String image = null;
					image = "../common/images/iconStatusError.gif";
					html = "<img src=\"" + image + "\" border=\"0\"  align=\"middle\" />";
					vResult.addElement(html);
				}else{

					if(isPropagationAllowed){
						String image = null;
						image = "../common/images/iconStatusComplete.gif";
						html = "<img src=\"" + image + "\" border=\"0\"  align=\"middle\" />";
						vResult.addElement(html);
					}
					if(!isPropagationAllowed ){
						String image = null;
						image = "../common/images/iconStatusError.gif";
						html = "<img src=\"" + image + "\" border=\"0\"  align=\"middle\" />";
						vResult.addElement(html);
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return vResult ;
	}


	/**
	 * supportApplicabiilty method gives the change objects which needs to be propagated
	 * @param context the eMatrix <code>Context</code> object
	 * @param strChangeTaskObjId Change Task objectid
	 * @return void
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R207
	 */
	private boolean supportApplicabiilty(Context context, String ChangeObjectId, String strChangeObjectType)throws Exception {
		MapList mlResult = new MapList();
		boolean isSupported = false;
		try{
			//handle interoperability using command object
			HashMap cmdMap = UICache.getCommand(context, EnterpriseChangeConstants.COMMAND_INTEROP_SUPPORT_APPLICABILITY);
			if (cmdMap == null || "null".equals(cmdMap) || cmdMap.size() <= 0)
			{
				String errorMessage = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.ECHSupportApplicabilityCommandNotRegistered" ,context.getSession().getLanguage());
				throw new Exception(errorMessage);
			}
			HashMap settingsList = (HashMap) cmdMap.get("settings");
			if (settingsList == null || "null".equals(settingsList) || settingsList.size() <= 0)
			{
				String errorMessage = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.PropApplicabilityInvalidSettings",context.getSession().getLanguage());
				throw new Exception(errorMessage);
			}
			StringList slapplicabilitySupportedTypes = new StringList();
			StringBuffer applicabilitySupportedTypes = new StringBuffer();
			Iterator<Entry> settingsListItr = settingsList.entrySet().iterator();
			while (settingsListItr.hasNext()) {
				Entry entry = settingsListItr.next();
				if (entry!=null) {
					String settingName = (String) entry.getKey();
					if (settingName!=null && settingName.startsWith("ApplicabilitySupportType_")) {
						String settingValue = (String)settingsList.get(settingName);
						if (settingValue!=null && !settingValue.isEmpty()) {
							if (applicabilitySupportedTypes!=null && !applicabilitySupportedTypes.toString().isEmpty()) {
								applicabilitySupportedTypes.append(",");
							}
							String schemaPropertyTemp = PropertyUtil.getSchemaProperty(context,settingValue);
							StringList slChildTypes = ProductLineUtil.getChildrenTypes(context, schemaPropertyTemp);
							if(slChildTypes.size() != 0 ){
								for(int m=0;m<slChildTypes.size();m++){

									String strchild=(String)slChildTypes.get(m);
									slapplicabilitySupportedTypes.add(strchild);
								}
							}
							applicabilitySupportedTypes.append(settingValue);
						}
					}
				}
			}//End of while

			StringTokenizer strSelectMPTok = new StringTokenizer(applicabilitySupportedTypes.toString(), ",");
			String strSelectIdKey = null;
			while(strSelectMPTok.hasMoreTokens())
			{
				strSelectIdKey = strSelectMPTok.nextToken();
				if(strSelectIdKey != null && !"".equalsIgnoreCase(strSelectIdKey))
				{
					String schemaPropertyTemp = PropertyUtil.getSchemaProperty(context,strSelectIdKey);
					slapplicabilitySupportedTypes.add(schemaPropertyTemp);
				}
			}
			if(slapplicabilitySupportedTypes.contains(strChangeObjectType)){
				isSupported = true;
				return isSupported;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return isSupported;
	}

	/**
	 * canPropagate decides whether to propagate the different change object or not.
	 * @param context the eMatrix <code>Context</code> object
	 * @param HashMap containing
	 *        objectId String id of context change object
	 * @return Map containing Propagation allowed - true or false
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212
	 */

	private String canPropagate(Context context,
			String strChangeObjectId,
			String strChangeObjectType,
			String strChangeTaskObjId)throws Exception {

		String  strResult = "";
		try{
			//handle interoperability using command object
			HashMap cmdMap = UICache.getCommand(context, EnterpriseChangeConstants.COMMAND_INTEROP_CAN_PROPAGATE_APPLICABILITY);
			if (cmdMap == null || "null".equals(cmdMap) || cmdMap.size() <= 0)
			{
				String errorMessage = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.ECHCanPropagateApplicabilityCommandNotRegistered",context.getSession().getLanguage());
				throw new Exception(errorMessage);
			}
			HashMap settingsList = (HashMap) cmdMap.get("settings");
			if (settingsList == null || "null".equals(settingsList) || settingsList.size() <= 0)
			{
				String errorMessage = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.PropApplicabilityInvalidSettings",context.getSession().getLanguage());
				throw new Exception(errorMessage);
			}
			if(null != (strChangeObjectType)){
				String typeAlias = FrameworkUtil.getAliasForAdmin(context, SELECT_TYPE, strChangeObjectType, false);

				String programMethod = (String)settingsList.get(typeAlias);

				while (programMethod == null || "null".equalsIgnoreCase(programMethod) || programMethod.length() <= 0)
				{
					//look up the parent tree for a setting
					String strType = UICache.getParentTypeName(context,strChangeObjectType);
					if (strType == null || "null".equalsIgnoreCase(strType) || strType.length() <= 0)
					{
						break;
					}
					typeAlias = FrameworkUtil.getAliasForAdmin(context, "type", strType, true);
					programMethod = (String)settingsList.get(typeAlias);
					strChangeObjectType = strType;
				}
				if (programMethod == null || "null".equalsIgnoreCase(programMethod) || programMethod.length() <= 0)
				{

					String errorMessage = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.PropApplicabilityMissingSetting",context.getSession().getLanguage());
					throw new Exception(errorMessage + typeAlias);
				}

				StringTokenizer tokens = new StringTokenizer(programMethod, ":");
				String strProgram = "";
				String strMethod = "";
				if (tokens.hasMoreTokens())
				{
					strProgram = tokens.nextToken();
					strMethod = tokens.nextToken();
				}
				else
				{
					String errorMessage = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.PropApplicabilityInvalidValue",context.getSession().getLanguage());
					throw new Exception(errorMessage + typeAlias);
				}

				HashMap paramMap = new HashMap();
				paramMap.put("changeobjectId", strChangeObjectId);
				paramMap.put("changeTaskobjectId", strChangeTaskObjId);
				String[] methodargs = JPO.packArgs(paramMap);
//				StringBuffer command = new StringBuffer();
//				command.append("print program '");
//				command.append(strProgram);
//				command.append("' select classname dump |");
				String strCommand="print program $1 select $2 dump $3";
				String className = MqlUtil.mqlCommand(context, strCommand,strProgram,"classname","|");
				try
				{
					strResult = (String) JPO.invokeLocal(context, className, new String[0], strMethod, methodargs,String.class);
				}
				catch (Throwable err)
				{
					throw new Exception(err);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return strResult;
	}
	/**
	 * isPropagationForECAllowed decides whether to propagate the engineering change object or not.
	 * @param context the eMatrix <code>Context</code> object
	 * @param HashMap containing
	 *        objectId String id of context change object
	 * @return Map containing Propagation allowed - true or false
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212
	 */
	public String isPropagationForECAllowed(Context context,String[] args) throws Exception{
		String strResult = "";
		try{
			HashMap paramMap1 = (HashMap)JPO.unpackArgs(args);
			String strChangeId = (String) paramMap1.get("changeobjectId");
			DomainObject changeObj= DomainObject.newInstance(context, strChangeId);
			String strChangeTaskObjId = (String) paramMap1.get("changeTaskobjectId");
			String changeState = changeObj.getInfo(context, SELECT_CURRENT);

			if(!(changeState.equals(EnterpriseChangeConstants.STATE_ENGINEERING_CHANGE_SUBMIT) || changeState.equals(EnterpriseChangeConstants.STATE_ENGINEERING_CHANGE_EVALUATE))) {
				strResult = "No Propagation";
			}
		}catch(Exception ex){
			throw new Exception(ex);
		}
		return strResult;
	}
	/**
	 * isPropagationForECAllowed decides whether to propagate the change object or not.
	 * @param context the eMatrix <code>Context</code> object
	 * @param HashMap containing
	 *        objectId String id of context change object
	 * @return Map containing Propagation allowed - true or false
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R418
	 */
	public String isPropagationForCAAllowed(Context context,String[] args) throws Exception{
		String strResult = "";
		try{
			HashMap paramMap1 = (HashMap)JPO.unpackArgs(args);
			String strChangeId = (String) paramMap1.get("changeobjectId");
			DomainObject changeObj= DomainObject.newInstance(context, strChangeId);
			String strChangeTaskObjId = (String) paramMap1.get("changeTaskobjectId");
			String changeState = changeObj.getInfo(context, SELECT_CURRENT);
			String changePolicy = changeObj.getInfo(context, SELECT_POLICY);

			if(!(changeState.equals(PropertyUtil.getSchemaProperty("policy", changePolicy, "state_Pending")) ||changeState.equals(PropertyUtil.getSchemaProperty("policy", changePolicy, "state_Prepare")) || changeState.equals(PropertyUtil.getSchemaProperty("policy", changePolicy, "state_InWork")))) {
				strResult = "No Propagation";
			}
		}catch(Exception ex){
			throw new Exception(ex);
		}
		return strResult;
	}

	public String isPropagationAllowed(Context context,	String[] args) throws Exception{
		String strResult = "";
		try{
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return strResult;
	}
	/**
	 * Update Applicability during propagation
	 * All applicable items connected to the Decision will be connected
	 * to the Change Task's Change object using the "Applicable Item" relationship
	 * @param context the eMatrix <code>Context</code> object
	 * @param HashMap containing
	 *        objectId String id of context object to connect from
	 *        applItems MapList containing object ids of applicable items to connect to
	 * @return void
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R207
	 */

	public void updateApplicability (Context context, String[] args) throws Exception{
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		String changeObjId = (String)paramMap.get("objectId");
		MapList mlapplItems = (MapList)paramMap.get("applItems");
		String sErrorMsg = "No";//Modified for IR-057309V6R2011x
		//Added for IR-046491V6R2011
		//int applItemsCount = (Integer)paramMap.get("count");

		try{
			DomainObject changeObj = DomainObject.newInstance(context, changeObjId);

			//first disconnect any existing applicability
			StringList selectRelStmts = new StringList(1);
			selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
			StringList selectStmts = new StringList(1);
			selectStmts.addElement(SELECT_ID);

			String strObjectPAttern = EnterpriseChangeConstants.TYPE_CONFIGURATION_FEATURES + "," +  EnterpriseChangeConstants.TYPE_LOGICAL_FEATURE + "," + EnterpriseChangeConstants.TYPE_MANUFACTURING_PLAN + "," + EnterpriseChangeConstants.TYPE_PRODUCTS ;

			MapList oldapplItemList = changeObj.getRelatedObjects(context,
					EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM,        // relationship pattern
					strObjectPAttern,  // object pattern
					selectStmts,       // object selects
					selectRelStmts,    // relationship selects
					false,             // to direction
					true,              // from direction
					(short) 1,         // recursion level
					null,              // object where clause
					null,
					0);             // relationship where clause
			Map map = null;
			String applItemRelId = "";
			Iterator itr = oldapplItemList.iterator();
			//Added forIR-049387V6R2011x:RPE to get the count of Changes being added.
			String strCount=PropertyUtil.getGlobalRPEValue(context,"TaskDeliverables");

			while(itr.hasNext()){
				map = (Map) itr.next();
				applItemRelId = (String)map.get(SELECT_RELATIONSHIP_ID);
				DomainRelationship.disconnect(context,applItemRelId);
			}

			//connect new applicable items
			String applItemId = "";
			DomainObject appItemObj = DomainObject.newInstance(context);
			boolean toBreak = false;
			for(int i=0;i<mlapplItems.size();i++){
				Iterator itr1 = null;
				Object objapplItems = (Object)mlapplItems.get(i);

				if(objapplItems instanceof MapList){
					MapList applItems = (MapList)mlapplItems.get(i);
					if(applItems.size() == 0 && mlapplItems.size()<1){
						applItems = mlapplItems;
					}
					itr1 = applItems.iterator();

				}else{
					itr1 = mlapplItems.iterator();
					toBreak = true;
				}

				DomainRelationship domRel = null;
				String strCommand = "";
				String strMessage = "";
				String upwardCompatability = "";
				boolean lastRev = false;
				Map map1 = new HashMap();
				while(itr1.hasNext()){
					map1 = (Map) itr1.next();
					applItemId = (String)map1.get(SELECT_ID);
					applItemRelId = (String)map1.get(DomainConstants.SELECT_RELATIONSHIP_ID);
					upwardCompatability = (String)map1.get("attribute[" + EnterpriseChangeConstants.ATTRIBUTE_UPWARD_COMPATIBILITY + "]");
					appItemObj.setId(applItemId);
					domRel = DomainRelationship.connect(context, changeObj, EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM, appItemObj);
					lastRev = false;

					String strInterfaceName = EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE;
					BusinessInterface busInterface = new BusinessInterface(strInterfaceName, context.getVault());
					AttributeTypeList listChangeDisciplineAttributes = busInterface.getAttributeTypes(context);
					//Check if an the change discipline interface has been already connected
					//String strCommandChangeDiscipline = "print connection " + domRel +" select interface["+ strInterfaceName + "] dump";
					String strCommandChangeDiscipline = "print connection $1 select $2 dump";

					//If no interface --> add one
					if((com.matrixone.apps.domain.util.MqlUtil.mqlCommand(context,strCommandChangeDiscipline,domRel.toString(),"interface["+ strInterfaceName + "]")).equalsIgnoreCase("false")){
						//String strAddInterface = "modify connection " + domRel +" add interface \'"+ strInterfaceName + "\'";
						String strAddInterface = "modify connection $1 add interface $2";
						String strMessageChangeDiscipline = com.matrixone.apps.domain.util.MqlUtil.mqlCommand(context,strAddInterface,domRel.toString(),strInterfaceName);
					}

					//Get Change Discipline value for Decision Applicable Item and copy value to the new Applicable Item rel
					if(applItemRelId!=null && !applItemRelId.isEmpty()){
						DomainRelationship applItemRelDom = new DomainRelationship(applItemRelId);
						Iterator listChangeDisciplineAttributesItr = listChangeDisciplineAttributes.iterator();
						while(listChangeDisciplineAttributesItr.hasNext()){
							String changeDisciplineName = ((AttributeType)listChangeDisciplineAttributesItr.next()).getName();
							String changeDisciplineValue = applItemRelDom.getAttributeValue(context, changeDisciplineName);
							domRel.setAttributeValue(context, changeDisciplineName, changeDisciplineValue);
						}
					}

					//add interface attribute for Upward Compatibility
					if(upwardCompatability != null && !"".equals(upwardCompatability)){
						//strCommand = "modify connection " + domRel +" add interface \'"+ EnterpriseChangeConstants.INTERFACE_UPWARD_COMPATIBILITY + "\'";
						strCommand = "modify connection $1 add interface $2";
						strMessage = MqlUtil.mqlCommand(context,strCommand,domRel.toString(),EnterpriseChangeConstants.INTERFACE_UPWARD_COMPATIBILITY);
						if(changeObj.isKindOf(context, EnterpriseChangeConstants.TYPE_ENGINEERING_CHANGE)){
							lastRev = isLastRevision(context, applItemId);
							if(upwardCompatability.equalsIgnoreCase(UPWARD_COMPATIBILITY_RANGE_NO) && lastRev){
								//Modified for IR-057309V6R2011x
								sErrorMsg="Yes";
								//sErrorMsg=i18nNow.getI18nString("emxEnterpriseChange.Error.IncompatibleUpwardCompatibility","emxEnterpriseChangeStringResource", context.getSession().getLanguage());
								upwardCompatability = UPWARD_COMPATIBILITY_RANGE_YES;
							}else if(upwardCompatability.equalsIgnoreCase(UPWARD_COMPATIBILITY_RANGE_YES) && !lastRev){
								//Modified for IR-057309V6R2011x
								sErrorMsg="Yes";
								//sErrorMsg=i18nNow.getI18nString("emxEnterpriseChange.Error.IncompatibleUpwardCompatibility","emxEnterpriseChangeStringResource", context.getSession().getLanguage());
								upwardCompatability = UPWARD_COMPATIBILITY_RANGE_NO;
							}
						}
						domRel.setAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_UPWARD_COMPATIBILITY,upwardCompatability);
						// Modified for IR-057309V6R2011x
						PropertyUtil.setGlobalRPEValue(context,"ErrorMsg",sErrorMsg);
						//Added for IR-046491V6R2011
						// if(applItemsCount>1){
						// sErrorMsg="";
						// }
						//End of IR-046491V6R2011
						//Added forIR-049387V6R2011x
						if(strCount != null && !"null".equalsIgnoreCase(strCount) && !"".equals(strCount)){
							if(Integer.parseInt(strCount)>0){
								// sErrorMsg="";
								PropertyUtil.setGlobalRPEValue(context,"ErrorMsg","No");
							}else{
								PropertyUtil.setGlobalRPEValue(context,"ErrorMsg","Yes");
							}
						}
						//End of IR-049387V6R2011x
						//End of IR-057309V6R2011x
					}
				}

				if(toBreak){
					break;
				}
			}
		}catch (Exception ex){
			throw new FrameworkException(ex);
		}
		// Commented for IR-057309V6R2011x
		// finally
		// {
		// if (sErrorMsg != null && sErrorMsg.length() > 0)
		// {
		// ${CLASS:emxContextUtil}.mqlNotice(context,sErrorMsg);
		// }
		// }
		// return;
	}

	/**
	 * Dynamically propagate applicability based on a command setting.
	 * Command object ECHInterOpPropagateApplicability setting determines
	 * class and method to call.  Used for Interoperability between applications
	 * @param context the eMatrix <code>Context</code> object
	 * @param HashMap containing
	 *        objectId String id of context object to connect from
	 *        latestValidDecisionId String containing the Id of the latest valid Decision
	 *        applItems MapList containing object ids of applicable items to connect to
	 * @return void
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R207
	 */

	private void dynamicPropagateApplicability (Context context, String objectId, String latestValidDecisionId, MapList applItems)
	throws Exception
	{
		//handle interoperability using command object
		HashMap cmdMap = UICache.getCommand(context, EnterpriseChangeConstants.COMMAND_INTEROP_PROPAGATE_APPLICABILITY);
		if (cmdMap == null || "null".equals(cmdMap) || cmdMap.size() <= 0)
		{
			String errorMessage = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.PropApplicabilityCommandNotRegistered" ,context.getSession().getLanguage());
			throw new Exception(errorMessage);
		}
		HashMap settingsList = (HashMap) cmdMap.get("settings");
		if (settingsList == null || "null".equals(settingsList) || settingsList.size() <= 0)
		{
			String errorMessage = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.PropApplicabilityInvalidSettings" ,context.getSession().getLanguage());
			throw new Exception(errorMessage);
		}

		//based on the Change Object type, call the appropriate connect method
		DomainObject domObj = DomainObject.newInstance(context, objectId);

		String strType = domObj.getInfo(context, SELECT_TYPE);

		//get symbolic name for change type
		String typeAlias = FrameworkUtil.getAliasForAdmin(context, SELECT_TYPE, strType, false);

		String programMethod = (String)settingsList.get(typeAlias);
		while (programMethod == null || "null".equalsIgnoreCase(programMethod) || programMethod.length() <= 0)
		{
			//look up the parent tree for a setting
			strType = UICache.getParentTypeName(context,strType);
			if (strType == null || "null".equalsIgnoreCase(strType) || strType.length() <= 0)
			{
				break;
			}
			typeAlias = FrameworkUtil.getAliasForAdmin(context, "type", strType, true);
			programMethod = (String)settingsList.get(typeAlias);
		}
		if (programMethod == null || "null".equalsIgnoreCase(programMethod) || programMethod.length() <= 0)
		{
			return;
			//String errorMessage = i18nNow.getI18nString("emxEnterpriseChange.Error.PropApplicabilityMissingSetting" ,"emxEnterpriseChangeStringResource",context.getSession().getLanguage());
			//throw new Exception(errorMessage + typeAlias);
		}



		StringTokenizer tokens = new StringTokenizer(programMethod, ":");
		String strProgram = "";
		String strMethod = "";
		if (tokens.hasMoreTokens())
		{
			strProgram = tokens.nextToken();
			strMethod = tokens.nextToken();
		}
		else
		{
			String errorMessage = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.PropApplicabilityInvalidValue",context.getSession().getLanguage());
			throw new Exception(errorMessage + typeAlias);
		}

		StringList changeDisciplinesAssociation = EnterpriseChangeUtil.getObjectChangeDisciplinesAssociation(context, objectId);
		String xmlApplicabilityExpression = new Decision(latestValidDecisionId).getXMLApplicabilityExpression(context, changeDisciplinesAssociation);

		HashMap paramMap = new HashMap();
		paramMap.put("objectId", objectId);
		paramMap.put("applItems", applItems);
		paramMap.put("xmlApplicabilityExpression", xmlApplicabilityExpression);
		//Added for 	IR-046491V6R2011
		paramMap.put("count", COUNT);

		String[] methodargs = JPO.packArgs(paramMap);
//		StringBuffer command = new StringBuffer();
//		command.append("print program '");
//		command.append(strProgram);
//		command.append("' select classname dump |");
		String strCommand="print program $1 select $2 dump $3";
		String className = MqlUtil.mqlCommand(context, strCommand,strProgram,"classname","|");

		//String className = MqlUtil.mqlCommand(context, command.toString());
		try
		{
			JPO.invokeLocal(context, className, new String[0], strMethod, methodargs);
		}
		catch (Throwable err)
		{
			throw new Exception(err);
		}
		return;
	}


	/**
	 * Propagate Applicability when a new Deliverable or Decision is connected
	 * or when a Decision is released.
	 * Called from connect trigger on Change Task as well as promote trigger on Decision
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId String
	 * @return int 0 on success 1 on failure
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R207
	 */

	public int propagateApplicabilityTrigger (Context context, String[] args) throws Exception{
		try{
			String sObjectId = args[0];
			String sFromObjectId = args[1];
			String relid = args[2]; // contains the new rel id
			String sToObjectId="";

			boolean bPropagate = false;
			MapList changeTaskList = null;
			String decisionId = "";
			DomainObject decisionObj = null;
			DomainObject changeTaskObj = null;

			if((relid == null || "".equals(relid)) && (sObjectId != null && !"".equals(sObjectId))){
				//this is a release of Decision
				decisionId = sObjectId;
				decisionObj = DomainObject.newInstance(context, decisionId);
				String strTrackApplicability = decisionObj.getAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_TRACK_APPLICABILITY);
				if(strTrackApplicability == null || "".equals(strTrackApplicability) || !RANGE_YES.equalsIgnoreCase(strTrackApplicability)){
					bPropagate = false;
				}else{
					bPropagate = true;
					//get all Change Tasks for this Decision
					StringList selectStmts = new StringList(SELECT_ID);
					changeTaskList = decisionObj.getRelatedObjects(context,
							EnterpriseChangeConstants.RELATIONSHIP_DECISION_APPLIES_TO,   // relationship pattern
							EnterpriseChangeConstants.TYPE_CHANGE_TASK,                   // object pattern
							selectStmts,                        // object selects
							null,                               // relationship selects
							false,                              // to direction
							true,                               // from direction
							(short) 1,                          // recursion level
							null,                               // object where clause
							null,                              // relationship where clause
							0);
				}
			}else if(relid !=null && !"".equals(relid)){
				//this is rel connection
				StringList selectRelStmts = new StringList(3);
				selectRelStmts.addElement(SELECT_TYPE);
				selectRelStmts.addElement(SELECT_FROM_ID);
				selectRelStmts.addElement(SELECT_TO_ID);
				selectRelStmts.addElement(SELECT_TO_TYPE);
				String relIds[] = new String[1];
				relIds[0] = relid;

				MapList mapList = DomainRelationship.getInfo(context, relIds, selectRelStmts);
				Map map = (Map)mapList.get(0);

				String relType = (String)map.get(SELECT_TYPE);
				sToObjectId = (String)map.get(SELECT_TO_ID);
				sFromObjectId = (String)map.get(SELECT_FROM_ID);
				String torelType = (String)map.get(SELECT_TO_TYPE);
				if(relType != null){
					//IR-041102
					// if (relType.equals(DomainConstants.RELATIONSHIP_TASK_DELIVERABLE) || relType.equals(EnterpriseChangeConstants.RELATIONSHIP_DECISION_APPLIES_TO))
					//{
					//if(relType.equals(DomainConstants.RELATIONSHIP_TASK_DELIVERABLE) && mxType.isOfParentType(context,torelType,EnterpriseChangeConstants.TYPE_CHANGE)){
					if(relType.equals(DomainConstants.RELATIONSHIP_TASK_DELIVERABLE) && EnterpriseChangeUtil.isChangeObject(context,sToObjectId)){
						//have change task id - need to fetch decision id
						//Get latest released Decision with Track Applicability for Change Task
						decisionId = getLatestReleasedDecision(context, sFromObjectId);
						if(decisionId.length()>0){
							bPropagate = true;
							HashMap changeMap = new HashMap();
							changeMap.put(SELECT_ID, sFromObjectId);
							changeTaskList = new MapList();
							changeTaskList.add(changeMap);
						}
					}else if(relType.equals(EnterpriseChangeConstants.RELATIONSHIP_DECISION_APPLIES_TO)){
						//Decision Applies To
						decisionId = sFromObjectId;
						String latestDecisionId = getLatestReleasedDecision(context, sToObjectId);

						if(latestDecisionId != null &&  latestDecisionId.equals(decisionId)){
							bPropagate = true;
							HashMap changeMap = new HashMap();
							changeMap.put(SELECT_ID, sToObjectId);
							changeTaskList = new MapList();
							changeTaskList.add(changeMap);
						}
					}
					// }
					//some other rel - don't care - just return
				}
			}
			if(bPropagate){
				//propagate for each change task
				//Iterator i = changeTaskList.iterator();

				ContextUtil.startTransaction(context, true); //Added for IR-057309V6R2011x
				String strDeliverableTypes = "";
				MapList mlApplicableItem = new MapList();

				if(changeTaskList.size() > 0){
					//Start Added for ECH Change Discipline
					String strInterfaceName = EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE;
					BusinessInterface busInterface = new BusinessInterface(strInterfaceName, context.getVault());
					AttributeTypeList listChangeDisciplineAttributes = busInterface.getAttributeTypes(context);

					Iterator listChangeDisciplineAttributesItr = listChangeDisciplineAttributes.iterator();

					String changeId = "";

					while(listChangeDisciplineAttributesItr.hasNext()){

						//propagate for each change task
						Iterator i = changeTaskList.iterator();

						String changeDisciplineName = ((AttributeType) listChangeDisciplineAttributesItr.next()).getName();
						String changeDisciplineValue = "";
						String changeDisciplineNameSmall = changeDisciplineName.replaceAll(" ", "");

//						//get the decisions applicable items
//						HashMap paramMap = new HashMap();
//						paramMap.put("objectId", decisionId);
//						paramMap.put("discipline", FrameworkUtil.getAliasForAdmin(context, "attribute", changeDisciplineName, true));
//						String[] methodargs = JPO.packArgs(paramMap);
//
//						MapList applItemList =  getApplicableItems(context, methodargs);
//						//MapList applItemList = getApplicableItems(context, decisionId);
//
//						mlApplicableItem.add(applItemList);
//						//applItemList.add(mlApplicableItem);


						String changeTaskId = "";
						String strAppProp = "";

						Map map = null;
						changeTaskObj = DomainObject.newInstance(context);

						while(i.hasNext()){
							map = (Map) i.next();
							changeTaskId = (String)map.get(SELECT_ID);
							if(changeTaskId != null){
								changeTaskObj.setId(changeTaskId);

								//Get Change Task value for Change Discipline
								changeDisciplineValue = changeTaskObj.getAttributeValue(context, changeDisciplineName);

								strAppProp = changeTaskObj.getAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_APPLICABILITY_PROPAGATED);

								//only propagate if already manually propagated
								if(strAppProp != null && RANGE_YES.equalsIgnoreCase(strAppProp)){
									//Check if Change Task is set for the Change Discipline
									//If yes continue propagation process
									//If no don't propagate for this applicability
									if(changeDisciplineValue.equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE)){

										//Get deliverable types for the Change Discipline
										String deliverableTypes = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChange." + changeDisciplineNameSmall + ".DeliverableTypes");
										if(deliverableTypes!=null && !deliverableTypes.equalsIgnoreCase("")){
											//Transform deliverableTypes with the proper syntax (type_XXXX into XXXX)
											//String strDeliverableTypes = "";
											StringList listDeliverableTypes = FrameworkUtil.split(deliverableTypes, ",");
											if(listDeliverableTypes!=null && !listDeliverableTypes.isEmpty()){
												Iterator listDeliverableTypesItr = listDeliverableTypes.iterator();
												while(listDeliverableTypesItr.hasNext()){
													String schemaPropertyTemp = PropertyUtil.getSchemaProperty(context,listDeliverableTypesItr.next().toString());
													if(schemaPropertyTemp!=null && !schemaPropertyTemp.isEmpty()){
														//strDeliverableTypes += PropertyUtil.getSchemaProperty(listDeliverableTypesItr.next().toString()) + ",";
														strDeliverableTypes += schemaPropertyTemp + ",";
													}
												}
											}
										}
									}
								}
							}
						}
					}

					if(sToObjectId!=null && !"".equals(sToObjectId)){
						StringList selectStmts = new StringList(1);
						selectStmts.addElement(SELECT_ID);
						selectStmts.addElement(SELECT_CURRENT);
						//Modified for IR-046491V6R2011
						MapList mapList = changeTaskObj.getRelatedObjects(
								context,
								RELATIONSHIP_TASK_DELIVERABLE,
								//changeTaskBase.getTypePatternString(context),
								strDeliverableTypes,
								selectStmts, null, false,
								true, (short) 1, null,
								null, 0);
						if(!mapList.isEmpty()){


							StringList changeDisciplinesAssociation = EnterpriseChangeUtil.getObjectChangeDisciplinesAssociation(context, sToObjectId);
							Iterator<String> changeDisciplinesAssociationItr = changeDisciplinesAssociation.iterator();
							while (changeDisciplinesAssociationItr.hasNext()) {
								String changeDisciplineAssociation = changeDisciplinesAssociationItr.next();
								if (changeDisciplineAssociation!=null && !changeDisciplineAssociation.isEmpty()) {
									HashMap paramMap = new HashMap();
									paramMap.put("objectId", decisionId);
									paramMap.put("discipline", FrameworkUtil.getAliasForAdmin(context, "attribute", changeDisciplineAssociation, true));
									mlApplicableItem.addAll(getApplicableItems (context, JPO.packArgs(paramMap)));
								}
							}

							propagateApplicabilityTriggerHelper(context, sToObjectId, decisionId, mlApplicableItem);
						}
					}else{
						//End of IR-049387V6R2011x
						//Added for IR-046491V6R2011
						StringList selectStmts = new StringList(1);
						selectStmts.addElement(SELECT_ID);
						selectStmts.addElement(SELECT_CURRENT);
						//Modified for IR-046491V6R2011
						MapList mapList = changeTaskObj.getRelatedObjects(
								context,
								RELATIONSHIP_TASK_DELIVERABLE,
								//changeTaskBase.getTypePatternString(context),
								strDeliverableTypes,
								selectStmts, null, false,
								true, (short) 1, null,
								null, 0);
						if(!mapList.isEmpty()){
							//MapList applItemList = new MapList();
							//applItemList.add(mlApplicableItem);
							// IR-041102
							for(Iterator iterator = mapList.iterator(); iterator.hasNext();){
								Map childMap = (Map) iterator.next();
								// Map childMap =
								// (Map)mapList.get(0);
								changeId = (String)childMap.get(SELECT_ID);
								// End Bug#374614
								//Added for IR-049387V6R2011x
								StringList changeDisciplinesAssociation = EnterpriseChangeUtil.getObjectChangeDisciplinesAssociation(context, changeId);
								Iterator<String> changeDisciplinesAssociationItr = changeDisciplinesAssociation.iterator();
								while (changeDisciplinesAssociationItr.hasNext()) {
									String changeDisciplineAssociation = changeDisciplinesAssociationItr.next();
									if (changeDisciplineAssociation!=null && !changeDisciplineAssociation.isEmpty()) {
										HashMap paramMap = new HashMap();
										mlApplicableItem.clear();
										paramMap.put("objectId", decisionId);
										paramMap.put("discipline", FrameworkUtil.getAliasForAdmin(context, "attribute", changeDisciplineAssociation, true));
										mlApplicableItem.addAll(getApplicableItems (context, JPO.packArgs(paramMap)));
									}
								}

								propagateApplicabilityTriggerHelper(context, changeId, decisionId, mlApplicableItem);
							}// for
						}// if
					}

					//}

					//get Deliverable and delete all applicable items
					//Added for Bug#374614 - Changed bcoz even documents are also connected by Task Deliverable rel
					//${CLASS:emxChangeTask} changeTaskBase = new ${CLASS:emxChangeTask}();
					//Added for IR-049387V6R2011x


					//}
				}//End Check Change Discipline value
				ContextUtil.commitTransaction(context); //Added for IR-057309V6R2011x
			}
		}catch (Exception ex){
			//added for IR-057309V6R2011x - start
			PropertyUtil.setGlobalRPEValue(context,"ErrorMsg","No");
			ContextUtil.abortTransaction(context);
			//added for IR-057309V6R2011x - end
			throw new FrameworkException(ex);
		}
		//Added for IR-057309V6R2011x
		finally{
			//String sErrorMsg=i18nNow.getI18nString("emxEnterpriseChange.Error.IncompatibleUpwardCompatibility","emxEnterpriseChangeStringResource", context.getSession().getLanguage());
			//String isErrorMsg=PropertyUtil.getGlobalRPEValue(context,"ErrorMsg");
			//if(isErrorMsg.equalsIgnoreCase("Yes")&& isErrorMsg != null && !"".equals(isErrorMsg)){
			//	${CLASS:emxContextUtil}.mqlNotice(context,sErrorMsg);
			//}
		}
		//End  for IR-057309V6R2011x
		return 0;
	}

	/**
	 * Show Add and Remove actions only if state of Decision is less than Release
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId String
	 * @return Boolean true to show links, false to hide them
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R207
	 */
	public Boolean showAddRemoveActions(Context context, String[] args) throws Exception {
		//unpacking the arguments from variable args
		HashMap programMap           = (HashMap)JPO.unpackArgs(args);

		//getting parent object Id from args
		String strObjectId           = (String)programMap.get("objectId");
		strObjectId = strObjectId.trim();
		DomainObject domObj = DomainObject.newInstance(context, strObjectId);

		StringList objectSelects = new StringList(3);
		objectSelects.add(DomainConstants.SELECT_TYPE);
		objectSelects.add(DomainConstants.SELECT_CURRENT);
		objectSelects.add(DomainConstants.SELECT_POLICY);

		Map objMap = domObj.getInfo(context,objectSelects);

		String strType              = (String)objMap.get(DomainConstants.SELECT_TYPE);
		String strCurrentState      = (String)objMap.get(DomainConstants.SELECT_CURRENT);
		String strObjPolicy         = (String)objMap.get(DomainConstants.SELECT_POLICY);

		// flag decides whether to show links 'Add Existing or Remove'  depending on the restricted state
		Boolean showLink            = new Boolean(false);
		int currentStatePos = -1;
		int restrictStatePos = -1;

		//if (!strType.equals(DomainConstants.TYPE_DECISION))
		if (!mxType.isOfParentType(context, strType,DomainConstants.TYPE_DECISION))
		{
			showLink = new Boolean(false);
			return showLink;
		}

		// getting and representing the restricted state
		String strRestrictState         = FrameworkUtil.lookupStateName(context,strObjPolicy,"state_Release");

		// no state based restrictions for policies not having the specified state
		if((strRestrictState== null) || "".equals(strRestrictState)) {
			showLink = new Boolean(true);
		} else {
			StateList stateList         = domObj.getStates(context);
			for (int i = 0; i < stateList.size(); i++) {
				String strState = (((State)stateList.elementAt(i)).getName());
				if(strState.equals(strCurrentState)) {
					currentStatePos = i;
				}
				if(strState.equals(strRestrictState)) {
					restrictStatePos = i;
				}
			}
			if (currentStatePos < restrictStatePos) {
				showLink = new Boolean(true);
			} else {
				showLink = new Boolean(false);
			}
		}
		return showLink;
	}

	/**
	 * Dynamically remove applicability for a Change Object.
	 * Command object ECHInterOpRemoveApplicability setting determines
	 * class and method to call.  Used for Interoperability between applications
	 * @param context the eMatrix <code>Context</code> object
	 * @param HashMap containing
	 *        objectId String id of context object to connect from
	 * @return void
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R207
	 */

	public void dynamicRemoveApplicabilityForChange (Context context, String objectId) throws Exception {
		//handle interoperability using command object
		HashMap cmdMap = UICache.getCommand(context, EnterpriseChangeConstants.COMMAND_INTEROP_REMOVE_APPLICABILITY);
		if (cmdMap == null || "null".equals(cmdMap) || cmdMap.size() <= 0)
		{
			String errorMessage = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.PropApplicabilityCommandNotRegistered",context.getSession().getLanguage());
			throw new Exception(errorMessage);
		}
		HashMap settingsList = (HashMap) cmdMap.get("settings");
		if (settingsList == null || "null".equals(settingsList) || settingsList.size() <= 0)
		{
			String errorMessage = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.PropApplicabilityInvalidSettings" ,context.getSession().getLanguage());
			throw new Exception(errorMessage);
		}

		//based on the Change Object type, call the appropriate connect method
		DomainObject domObj = DomainObject.newInstance(context, objectId);

		String strType = domObj.getInfo(context, SELECT_TYPE);

		//get symbolic name for change type
		String typeAlias = FrameworkUtil.getAliasForAdmin(context, SELECT_TYPE, strType, false);

		String programMethod = (String)settingsList.get(typeAlias);
		//fix for removing child types of EC -- start
		while (programMethod == null || "null".equalsIgnoreCase(programMethod) || programMethod.length() <= 0)
		{
			//look up the parent tree for a setting
			strType = UICache.getParentTypeName(context,strType);
			if (strType == null || "null".equalsIgnoreCase(strType) || strType.length() <= 0)
			{
				break;
			}
			typeAlias = FrameworkUtil.getAliasForAdmin(context, "type", strType, true);
			programMethod = (String)settingsList.get(typeAlias);
		}
		//fix for removing child types of EC -- end
		if (programMethod == null || "null".equals(programMethod) || programMethod.length() <= 0)
		{
			//temporary fix for IR-015818V6R2010x
			return;
			//String errorMessage = i18nNow.getI18nString("emxEnterpriseChange.Error.PropApplicabilityMissingSetting" ,"emxEnterpriseChangeStringResource",context.getSession().getLanguage());
			//throw new Exception(errorMessage + typeAlias);
		}
		StringTokenizer tokens = new StringTokenizer(programMethod, ":");
		String strProgram = "";
		String strMethod = "";
		if (tokens.hasMoreTokens())
		{
			strProgram = tokens.nextToken();
			strMethod = tokens.nextToken();
		}
		else
		{
			String errorMessage = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.PropApplicabilityInvalidValue" ,context.getSession().getLanguage());
			throw new Exception(errorMessage + typeAlias);
		}

		HashMap paramMap = new HashMap();
		paramMap.put("objectId", objectId);

		String[] methodargs = JPO.packArgs(paramMap);
//		StringBuffer command = new StringBuffer();
//		command.append("print program '");
//		command.append(strProgram);
//		command.append("' select classname dump |");
		String strCommand="print program $1 select $2 dump $3";
		String className = MqlUtil.mqlCommand(context, strCommand,strProgram,"classname","|");

//		String className = MqlUtil.mqlCommand(context, command.toString());
		try
		{
			JPO.invokeLocal(context, className, new String[0], strMethod, methodargs);
		}
		catch (Throwable err)
		{
			throw new Exception(err);
		}
		return;
	}

	/**
	 * Remove Applicability for a Change Object
	 * @param context the eMatrix <code>Context</code> object
	 * @param HashMap containing
	 *        objectId String id of context object to connect from
	 * @return void
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R207
	 */

	public void removeApplicabilityForChange (Context context, String[] args)
	throws Exception
	{
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		String changeObjId = (String)paramMap.get("objectId");

		try
		{
			DomainObject changeObj = DomainObject.newInstance(context, changeObjId);

			//first disconnect any existing applicability
			StringList selectRelStmts = new StringList(1);
			selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
			StringList selectStmts = new StringList(1);
			selectStmts.addElement(SELECT_ID);
			String strObjectPAttern = EnterpriseChangeConstants.TYPE_CONFIGURATION_FEATURES + "," +  EnterpriseChangeConstants.TYPE_LOGICAL_FEATURE + "," + EnterpriseChangeConstants.TYPE_MANUFACTURING_PLAN + "," + EnterpriseChangeConstants.TYPE_PRODUCTS ;
			MapList oldapplItemList = changeObj.getRelatedObjects(context,
					EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM,        // relationship pattern
					strObjectPAttern,  // object pattern
					selectStmts,       // object selects
					selectRelStmts,    // relationship selects
					false,             // to direction
					true,              // from direction
					(short) 1,         // recursion level
					null,              // object where clause
					null,             // relationship where clause
					0);
			Map map = null;
			String applItemRelId = "";
			Iterator itr = oldapplItemList.iterator();
			while (itr.hasNext())
			{
				map = (Map) itr.next();
				applItemRelId = (String)map.get(SELECT_RELATIONSHIP_ID);
				DomainRelationship.disconnect(context,applItemRelId);
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
		return;
	}

	/**
	 * Get the latest released Decision with Track Applicability for the Change Task
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @return String id of latest released Decision
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R207
	 */

	public String getLatestReleasedDecision(Context context, String changeTaskId)
	throws Exception
	{
		DomainObject changeTaskObj = DomainObject.newInstance(context, changeTaskId);
		String latestDecisionId = "";
		StringList selectStmts = new StringList(2);
		selectStmts.addElement(SELECT_ID);
		selectStmts.addElement("current.actual");
		String whereclause = "(attribute[" + EnterpriseChangeConstants.ATTRIBUTE_TRACK_APPLICABILITY + "]" + "== \"" + RANGE_YES + "\" && current == \"" + EnterpriseChangeConstants.STATE_DECISION_RELEASE + "\")" ;
		MapList mapList = changeTaskObj.getRelatedObjects(context,
				EnterpriseChangeConstants.RELATIONSHIP_DECISION_APPLIES_TO,            // relationship pattern
				TYPE_DECISION,                      // object pattern
				selectStmts,                        // object selects
				null,                               // relationship selects
				true,                               // to direction
				false,                              // from direction
				(short) 1,                          // recursion level
				whereclause,                        // object where clause
				null,                              // relationship where clause
				0);

		if (mapList.size() > 0)
		{
			//find the latest released
			Iterator itr = mapList.iterator();
			Map map = null;
			String currDecisionId = "";
			String strCurrReleaseDate = "";
			String strLatestReleaseDate = "";
			Date currRelDate;
			Date latestRelDate;
			while (itr.hasNext())
			{
				map = (Map) itr.next();

				strCurrReleaseDate = (String)map.get("current.actual");
				currDecisionId = (String)map.get(SELECT_ID);
				currRelDate = eMatrixDateFormat.getJavaDate(strCurrReleaseDate);
				if (strLatestReleaseDate.length() > 0)
				{
					latestRelDate = eMatrixDateFormat.getJavaDate(strLatestReleaseDate);
					if (currRelDate.compareTo(latestRelDate) > 0)
					{
						strLatestReleaseDate = strCurrReleaseDate;
						latestDecisionId = currDecisionId;
					}
				}
				else
				{
					strLatestReleaseDate = strCurrReleaseDate;
					latestDecisionId = currDecisionId;
				}
			}
		}
		return latestDecisionId;
	}

	/**
	 * Method to update the Track Applicability attribute
	 * If the attrbute is updated to NO, we have to remove the applicable items attached.
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void updateTrackApplicability(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap)programMap.get("paramMap");

		String objectId  = (String)paramMap.get("objectId");
		String newTrackApplicability = (String) paramMap.get("New Value");

		DomainObject decision = new DomainObject(objectId);
		decision.setAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_TRACK_APPLICABILITY, newTrackApplicability);

		if("no".equalsIgnoreCase(newTrackApplicability)) {
			StringList relIdList = decision.getInfoList(context, "from["+ EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM +"].id");
			Iterator itr = relIdList.iterator();
			while(itr.hasNext()) {
				DomainRelationship.disconnect(context, (String)itr.next());
			}
		}
	}
	//Added for IR-049387V6R2011x
	/**
	 * Method for auto-propagation of applicability, helper method of propagateApplicabilityTrigger()
	 * @param context
	 * @param changeId
	 * @param latestValidDecisionId
	 * @param applItemList
	 * @throws Exception
	 * @since R210
	 */

	private void propagateApplicabilityTriggerHelper(Context context,String changeId, String latestValidDecisionId, MapList applItemList)
	throws Exception {
		try{
		// changeId = changeTaskObj.getInfo(context,
		// SELECT_CHANGE_TASK_DELIVERABLE);
		// Added for IR-046491V6R2011
		DomainObject changeObj = DomainObject.newInstance(context, changeId);
		if (changeId == null || "null".equalsIgnoreCase(changeId)
				|| "".equals(changeId)) {
			// no deliverable can't propagate
			return;
		}
		//modified for IR-057187V6R2011x -- added Defect and Defect Action types
		if (changeObj.isKindOf(context, TYPE_ECO)|| changeObj.isKindOf(context, TYPE_ECR) || changeObj.isKindOf(context, EnterpriseChangeConstants.TYPE_DEFECT) || changeObj.isKindOf(context, EnterpriseChangeConstants.TYPE_DEFECT_ACTION))  {
			// ECR/ECO can't propagate
			return;
		}
		// String changeState = (String)childMap.get(SELECT_CURRENT);
		String changeState = changeObj.getInfo(context, SELECT_CURRENT);
		// End of IR-046491V6R2011
		// Modified for IR-046491V6R2011
		if (changeObj.isKindOf(context, EnterpriseChangeConstants.TYPE_ENGINEERING_CHANGE)&& !(changeState.equals(EnterpriseChangeConstants.STATE_ENGINEERING_CHANGE_SUBMIT) || changeState.equals(EnterpriseChangeConstants.STATE_ENGINEERING_CHANGE_EVALUATE))) {
			// if state of EC is beyond evaluate - can't propagate
			//return; //commented out for IR-057309V6R2011x
			String alertNotice = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Alert.DecisionCannotbePromoted" ,context.getSession().getLanguage());
			throw new FrameworkException(alertNotice); //added for IR-057309V6R2011x
		}
		// handle interoperability using command object
		// Added for IR-046491V6R2011
		COUNT++;
		dynamicPropagateApplicability(context, changeId, latestValidDecisionId, applItemList);
		}catch(Exception ex){
			throw new Exception(ex);
		}
	}

	/**
	 * Method to determine if Decision Applicable Item command should be displayed
	 * If the Decision Applies To Change Task has at least one Change Discipline, the display Applicable Item
	 * @param context
	 * @param args
	 * @throws Exception
	 * @since EnterpriseChange R211
	 */
	public Boolean showDecisionApplicableItems(Context context, String[] args) throws Exception {
		try {
			Boolean returnBoolean = false;
			//unpacking the arguments from variable args
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("objectId");

			if(strObjectId!=null && !strObjectId.isEmpty()){
				DomainObject domObject = new DomainObject(strObjectId);
				if(!domObject.isKindOf(context, DomainConstants.TYPE_DECISION)){
					returnBoolean = true;
				}else{
					//if (!EnterpriseChangeUtil.isDerivationEnabled(context)) {
						StringList changeDisciplines = EnterpriseChangeUtil.getChangeDisciplines(context);
						Iterator<String> changeDisciplinesItr = changeDisciplines.iterator();
						while (changeDisciplinesItr.hasNext()){
							String changeDiscipline = changeDisciplinesItr.next();
							if (changeDiscipline!=null && !changeDiscipline.isEmpty()) {
								programMap.put("changeDiscipline", changeDiscipline);
								String[] methodargs = JPO.packArgs(programMap);
								Boolean tempBoolean = showDecisionDomainApplicableItems(context, methodargs);
								if (tempBoolean) {
									returnBoolean = true;
								}
							}
						}
					//}
				}
			}
			return returnBoolean;
		} catch (Exception e) {
			throw e;
		}
	}


	/**
	 * Method to determine if Decision Design Applicable Item command should be displayed
	 * If the Decision Applies To Change Task has Design Change Discipline, the display Design Applicable Item
	 * @param context
	 * @param args
	 * @throws Exception
	 * @since EnterpriseChange R211
	 */
	public Boolean showDecisionDesignApplicableItems(Context context, String[] args) throws Exception {
		try {
			Boolean returnBoolean = false;
			//if (!EnterpriseChangeUtil.isDerivationEnabled(context)) {
				//unpacking the arguments from variable args
				HashMap programMap = (HashMap)JPO.unpackArgs(args);
				programMap.put("changeDiscipline", EnterpriseChangeConstants.ATTRIBUTE_CHANGE_DISCIPLINE_DESIGN);
				String[] methodargs =JPO.packArgs(programMap);
				returnBoolean = showDecisionDomainApplicableItems(context, methodargs);
			//}
			return returnBoolean;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Method to determine if Decision Manufacturing Applicable Item command should be displayed
	 * If the Decision Applies To Change Task has Manufacturing Change Discipline, the display Manufacturing Applicable Item
	 * @param context
	 * @param args
	 * @throws Exception
	 * @since EnterpriseChange R211
	 */
	public Boolean showDecisionManufacturingApplicableItems(Context context, String[] args) throws Exception {
		try{
			Boolean returnBoolean = false;
			//if (!EnterpriseChangeUtil.isDerivationEnabled(context)) {
				//unpacking the arguments from variable args
				HashMap programMap = (HashMap)JPO.unpackArgs(args);
				programMap.put("changeDiscipline", EnterpriseChangeConstants.ATTRIBUTE_CHANGE_DISCIPLINE_MANUFACTURING);
				String[] methodargs =JPO.packArgs(programMap);
				returnBoolean = showDecisionDomainApplicableItems(context, methodargs);
			//}
			return returnBoolean;
		}catch (Exception e){
			throw e;
		}
	}

	/**
	 * Global Method call by Change Discipline to determine if Decision Applicable Item command for proper Change Discipline should be displayed
	 * @param context
	 * @param args
	 * @throws Exception
	 * @since EnterpriseChange R211
	 */
	public Boolean showDecisionDomainApplicableItems(Context context, String[] args) throws Exception {
		try {
			Boolean returnBoolean = false;
			if (!EnterpriseChangeUtil.isApplicabilityManagementEnabled(context)) {
				//unpacking the arguments from variable args
				HashMap programMap = (HashMap)JPO.unpackArgs(args);

				//getting parent object Id from args
				String strObjectId = (String)programMap.get("objectId");
				//getting Change Discipline Type
				String changeDiscipline = (String)programMap.get("changeDiscipline");

				StringList authorizedChangeDisciplines = new Decision(strObjectId).getAuthorizedChangeDisciplinesForApplicability(context);
				if (authorizedChangeDisciplines!=null && authorizedChangeDisciplines.contains(changeDiscipline)) {
					returnBoolean = true;
				}
			}
			return returnBoolean;
		} catch (Exception e) {
			throw e;
		}
	}



	/**
	 * Display Applicable Item Change Discipline Values
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return String - containing Change Discipline Values
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public Vector getApplicableItemChangeDiscipline (Context context,String[] args) throws Exception {
		try {
			Vector returnVector = new Vector();
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			HashMap paramList = (HashMap)programMap.get("paramList");
			String languageStr = (String)paramList.get("languageStr");
			String editTableMode = (String)paramList.get("editTableMode");
			String parentOID = (String)paramList.get("parentOID");

			StringList authorizedChangeDisciplines = new Decision(parentOID).getAuthorizedChangeDisciplinesForApplicability(context);
			StringList changeDisciplines = EnterpriseChangeUtil.getChangeDisciplines(context);
			Iterator objectListItr = objectList.iterator();
			while(objectListItr.hasNext()){
				StringBuffer strBuffer = new StringBuffer();
				Map object = (Map) objectListItr.next();
				if(object!=null && object.size()>0){
					String objectId = (String)object.get(DomainConstants.SELECT_ID);
					String objectRelId = (String)object.get(DomainConstants.SELECT_RELATIONSHIP_ID);
					if ((objectId!=null && !objectId.isEmpty()) && (objectRelId!=null && !objectRelId.isEmpty())) {
						DomainObject objectDom = new DomainObject(objectId);
						String objectPhysicalId = objectDom.getInfo(context, "physicalid");
						DomainRelationship objectRelDom = new DomainRelationship(objectRelId);
						MapList listToDisplayAttributes = new MapList();
						StringList selectedChangeDisciplines = new StringList();
						StringList associatedChangeDisciplines = EnterpriseChangeUtil.getObjectChangeDisciplinesAssociation(context, objectId);
						Iterator<String> changeDisciplinesItr = changeDisciplines.iterator();
						while (changeDisciplinesItr.hasNext()) {
							Map attributeToDisplay = new HashMap();
							String isChecked = "";
							String isDisabled = "";
							String inputType = "";
							Boolean isDisplayed = true;
							Boolean canBeEnabled = true;

							String changeDiscipline = changeDisciplinesItr.next();
							String changeDisciplineI18N = i18nNow.getAttributeI18NString(changeDiscipline,context.getSession().getLanguage());
							String changeDisciplineSmall = changeDiscipline.replaceAll(" ", "") + objectPhysicalId;
							String changeDisciplineSmallHidden = changeDisciplineSmall + "Hidden";
							String attrNameValue = objectRelDom.getAttributeValue(context, changeDiscipline);

							if (attrNameValue.equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE)) {
								isChecked = "checked";
								selectedChangeDisciplines.addElement(changeDiscipline);
							} else if (attrNameValue.equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_FALSE)) {
								isChecked = "";
							}

							if (!associatedChangeDisciplines.contains(changeDiscipline) || !authorizedChangeDisciplines.contains(changeDiscipline)) {
								//Don't even display
								isDisplayed = false;
								canBeEnabled = false;
							}

							if (associatedChangeDisciplines.size()==1) {
								isDisabled = "disabled";
							}

							//If mode is view, greys out all checkboxes
							if(editTableMode.equalsIgnoreCase("false")){
								isDisabled = "disabled";
							}

							attributeToDisplay.put("attrName",changeDiscipline);
							attributeToDisplay.put("attrNameI18N",changeDisciplineI18N);
							attributeToDisplay.put("attrNameSmall",changeDisciplineSmall);
							attributeToDisplay.put("attrNameSmallHidden",changeDisciplineSmallHidden);
							attributeToDisplay.put("attrNameValue",attrNameValue);
							attributeToDisplay.put("isChecked",isChecked);
							attributeToDisplay.put("isDisabled",isDisabled);
							attributeToDisplay.put("isDisplayed",isDisplayed.toString());
							attributeToDisplay.put("canBeEnabled",canBeEnabled.toString());
							listToDisplayAttributes.add(attributeToDisplay);
						}//End of while discipline

						Boolean isPreviousDisplayed = false;
						Iterator listToDisplayAttributesItr = listToDisplayAttributes.iterator();
						StringBuffer tempStrBuffer = new StringBuffer();
						while(listToDisplayAttributesItr.hasNext()){
							Map attributeToDisplay = (Map)listToDisplayAttributesItr.next();

							if ((tempStrBuffer.toString()!=null && !tempStrBuffer.toString().isEmpty()) && isPreviousDisplayed && Boolean.valueOf((String)attributeToDisplay.get("isDisplayed")).booleanValue()) {
								tempStrBuffer.append("<br/>");
							}
							if (Boolean.valueOf((String)attributeToDisplay.get("isDisplayed")).booleanValue()) {
								isPreviousDisplayed = true;
							} else {
								isPreviousDisplayed = false;
							}

							//If only one discipline greys out the checkbox
							if(selectedChangeDisciplines.contains((String)attributeToDisplay.get("attrName")) && selectedChangeDisciplines.size()==1){
								attributeToDisplay.remove("isDisabled");
								attributeToDisplay.put("isDisabled","disabled");
							}
							StringBuffer onClick = new StringBuffer();
							//Define javascript when user clicks checkbox
							onClick.append("var " + (String)attributeToDisplay.get("attrNameSmallHidden") + " = document.getElementById('" + (String)attributeToDisplay.get("attrNameSmallHidden") + "');");
							onClick.append("if(this.checked){");
							onClick.append((String)attributeToDisplay.get("attrNameSmallHidden") + ".value='Yes';");
							for(int k=0;k<listToDisplayAttributes.size();k++){
								Map tempMap = (Map)listToDisplayAttributes.get(k);
								String tempNameSmall = (String)tempMap.get("attrNameSmall");
								if(!tempNameSmall.equalsIgnoreCase((String)attributeToDisplay.get("attrNameSmall"))){
									if(Boolean.valueOf((String)tempMap.get("isDisplayed")).booleanValue()){
										//if(!((String)tempMap.get("isDisabled")).equalsIgnoreCase("disabled")){
										if(Boolean.valueOf((String)tempMap.get("canBeEnabled")).booleanValue()){
											onClick.append("document.getElementById('" + tempNameSmall + "').disabled=false;");
										}
									}
								}
							}
							onClick.append("}");
							onClick.append("else if(!this.checked){");
							onClick.append((String)attributeToDisplay.get("attrNameSmallHidden") + ".value='No';");
							for(int k=0;k<listToDisplayAttributes.size();k++){
								Map tempMap = (Map)listToDisplayAttributes.get(k);
								String tempNameSmall = (String)tempMap.get("attrNameSmall");
								if(!tempNameSmall.equalsIgnoreCase((String)attributeToDisplay.get("attrNameSmall"))){
									if(Boolean.valueOf((String)tempMap.get("isDisplayed")).booleanValue()){
										onClick.append("if(!this.checked ");
										for(int l=0;l<listToDisplayAttributes.size();l++){
											Map tempContextMap = (Map)listToDisplayAttributes.get(l);
											String tempContextNameSmall = (String)tempContextMap.get("attrNameSmall");
											if(!tempContextNameSmall.equalsIgnoreCase((String)attributeToDisplay.get("attrNameSmall")) && !tempContextNameSmall.equalsIgnoreCase(tempNameSmall)){
												if(Boolean.valueOf((String)tempContextMap.get("isDisplayed")).booleanValue()){
													onClick.append("&& !document.getElementById('" + tempContextNameSmall + "').checked ");
												}
											}
										}
										onClick.append("){");
										//onClick.append((String)attributeToDisplay.get("attrNameSmallHidden") + ".value='No';");
										onClick.append("document.getElementById('" + tempNameSmall + "').disabled=true;");
										onClick.append("}");
									}
								}
							}
							onClick.append("}");
							if(Boolean.valueOf((String)attributeToDisplay.get("isDisplayed")).booleanValue()){
								tempStrBuffer.append("<input type=\"checkbox\" id=\"" + XSSUtil.encodeForHTMLAttribute(context,(String)attributeToDisplay.get("attrNameSmall")) + "\" name=\"" + XSSUtil.encodeForHTMLAttribute(context,(String)attributeToDisplay.get("attrNameSmall")) + "\" " + (String)attributeToDisplay.get("isChecked") + " " + (String)attributeToDisplay.get("isDisabled") + " onClick=\"" + XSSUtil.encodeForHTMLAttribute(context,onClick.toString()) + "\"/>");
								tempStrBuffer.append(" ");
								tempStrBuffer.append((String)attributeToDisplay.get("attrNameI18N"));
							}
							tempStrBuffer.append("<input type='hidden' id='"+ XSSUtil.encodeForHTMLAttribute(context,(String)attributeToDisplay.get("attrNameSmallHidden")) + "' name='" + XSSUtil.encodeForHTMLAttribute(context,(String)attributeToDisplay.get("attrNameSmallHidden")) + "' value='" + XSSUtil.encodeForHTMLAttribute(context,(String)attributeToDisplay.get("attrNameValue")) + "'/>");
						}//End of while for javascript
						strBuffer.append(tempStrBuffer.toString());
					}
				}
				returnVector.add(strBuffer.toString());
			}//End of while objectList
			return returnVector;
		} catch (Exception e) {
			throw e;
		}
	}


	/**
	 * Update Applicable Item Change Discipline in case of Applicable Item type belongs to several Change Disciplines
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 *          paramMap - contains relId
	 * @return Boolean - true if everything went good
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212_HF3
	 * @deprecated since R418
	 */
	public boolean updateApplicableItemChangeDiscipline(Context context,String args[]) throws Exception	{
		try {
			Boolean returnBoolean = false;
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String objectId = (String) paramMap.get("objectId");
			String relId = (String) paramMap.get("relId");
			HashMap requestMap = (HashMap) programMap.get("requestMap");

			if ((objectId!=null && !objectId.isEmpty()) && (relId!=null && !relId.isEmpty())) {
				DomainObject objectDom = new DomainObject(objectId);
				DomainRelationship objectRelDom = new DomainRelationship(relId);

				String strInterfaceName = EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE;

				//Check if an the change discipline interface has been already connected
				//String strCommand = "print connection " + relId +" select interface["+ strInterfaceName + "] dump";
				String strCommand = "print connection $1 select $2 dump";
				//If no interface --> add one
				if ((MqlUtil.mqlCommand(context,strCommand,relId,"interface["+ strInterfaceName + "]")).equalsIgnoreCase("false")) {
//					String strAddInterface = "modify connection " + relId +" add interface \'"+ strInterfaceName + "\'";
					String strAddInterface = "modify connection $1 add interface $2";
					String strMessage = MqlUtil.mqlCommand(context,strAddInterface,relId,strInterfaceName);
				}

				StringList changeDisciplines = EnterpriseChangeUtil.getChangeDisciplines(context);
				Iterator<String> changeDisciplinesItr = changeDisciplines.iterator();
				while (changeDisciplinesItr.hasNext()) {
					String changeDiscipline = changeDisciplinesItr.next();
					String changeDisciplineSmall = changeDiscipline.replaceAll(" ", "") + objectDom.getInfo(context, "physicalid");
					String changeDisciplineSmallHidden = changeDisciplineSmall + "Hidden";
					String[] changeDisciplineValues = (String[])requestMap.get(changeDisciplineSmallHidden);
					Boolean disconnectManufacturingIntent = false;

					if (changeDisciplineValues!=null) {
						String changeDisciplineValue =  changeDisciplineValues[0];
						objectRelDom.setAttributeValue(context, changeDiscipline, changeDisciplineValue);
						if (changeDisciplineValue.equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_FALSE) && changeDiscipline.equalsIgnoreCase(EnterpriseChangeConstants.ATTRIBUTE_CHANGE_DISCIPLINE_MANUFACTURING)) {
							disconnectManufacturingIntent = true;
						}
					} else {
						objectRelDom.setAttributeValue(context, changeDiscipline, EnterpriseChangeConstants.CHANGE_DISCIPLINE_FALSE);
						if (changeDiscipline.equalsIgnoreCase(EnterpriseChangeConstants.ATTRIBUTE_CHANGE_DISCIPLINE_MANUFACTURING)) {
							disconnectManufacturingIntent = true;
						}
					}

					if (disconnectManufacturingIntent) {
						//String applicabilityIntentsList = MqlUtil.mqlCommand(context, "print connection " + relId + " select frommid.id dump |");
						String applicabilityIntentsList = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump $3",relId,"frommid.id","|");
						StringList applicabilityIntents = FrameworkUtil.split(applicabilityIntentsList, "|");
						if (applicabilityIntents!=null && !applicabilityIntents.isEmpty()) {
							Iterator<String> applicabilityIntentsItr = applicabilityIntents.iterator();
							while (applicabilityIntentsItr.hasNext()) {
								String applicabilityIntent = applicabilityIntentsItr.next();
								if(applicabilityIntent!=null && !applicabilityIntent.isEmpty()){
									DomainRelationship.disconnect(context, applicabilityIntent, false);
								}
							}
						}
					}
				}
				returnBoolean = true;
			}
			return returnBoolean;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Exclude object already connected to Decision as Applicable Item
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return StringList - containing id of object already connected to Decision as Applicable Item
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeDecisionApplicableItems(Context context, String[] args)throws Exception{
		StringList returnStringList = new StringList();
		try{
			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String decisionId = (String) paramMap.get("objectId");

			DomainObject decisionObj = new DomainObject(decisionId);
			StringList selectStmts = new StringList(1);
			selectStmts.addElement(SELECT_ID);

			StringList selectRelStmts = new StringList(1);
			selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

			String strInterfaceName = EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE;
			BusinessInterface busInterface = new BusinessInterface(strInterfaceName, context.getVault());
			AttributeTypeList listInterfaceAttributes = busInterface.getAttributeTypes(context);

			String objectTempTypes = "";

			Iterator listInterfaceAttributesItr = listInterfaceAttributes.iterator();
			while (listInterfaceAttributesItr.hasNext()){
				String attrName = ((AttributeType) listInterfaceAttributesItr.next()).getName();
				if(objectTempTypes!=null && !objectTempTypes.equalsIgnoreCase("")){
					objectTempTypes = objectTempTypes + ",";
				}
				if(attrName!=null && !attrName.equalsIgnoreCase("")){
					objectTempTypes = objectTempTypes + EnoviaResourceBundle.getProperty(context, "emxEnterpriseChange." + attrName.replaceAll(" ", "") + ".ApplicableItemTypes");
				}
			}

			String objectTypes = "";

			if(objectTempTypes!=null && !objectTempTypes.equalsIgnoreCase("")){
				StringList strListTypes = FrameworkUtil.split(objectTempTypes, ",");
				if(strListTypes!=null && strListTypes.size()>0){
					for(int i=0;i<strListTypes.size();i++){
						String objectType = PropertyUtil.getSchemaProperty(context,(String)strListTypes.get(i));
						if(objectTypes!=null && !objectTypes.equalsIgnoreCase("")){
							objectTypes = objectTypes + ",";
						}
						objectTypes = objectTypes + objectType;
					}
				}
			}

			MapList relatedApplicableItems = decisionObj.getRelatedObjects(context,
					EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM ,
					objectTypes,
					new StringList(DomainConstants.SELECT_ID),
					new StringList(DomainConstants.SELECT_RELATIONSHIP_ID),
					false,	//to relationship
					true,	//from relationship
					(short)1,
					DomainConstants.EMPTY_STRING, // object where clause
					DomainConstants.EMPTY_STRING, // relationship where clause
					0);

			Iterator tempIterator = relatedApplicableItems.iterator();
			while(tempIterator.hasNext()){
				Map tempMap = (Map)tempIterator.next();
				String tempId = (String)tempMap.get(DomainConstants.SELECT_ID);
				returnStringList.add(tempId);
			}

		}catch (Exception e){
			System.out.println(e.getMessage());
		}finally{
			return returnStringList;
		}
	}

	/**
	 * Exclude design object already connected to Decision as Applicable Item
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return StringList - containing id of design object already connected to Decision as Applicable Item
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeDecisionDesignApplicableItems(Context context, String[] args)throws Exception{
		StringList returnStringList = new StringList();
		try{
			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String decisionId = (String) paramMap.get("objectId");

			DomainObject decisionObj = new DomainObject(decisionId);
			StringList selectStmts = new StringList(1);
			selectStmts.addElement(SELECT_ID);

			StringList selectRelStmts = new StringList(1);
			selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

			StringBuffer relWhere = new StringBuffer();
			relWhere.append(DomainConstants.EMPTY_STRING);
			relWhere.append("attribute[");
			relWhere.append(EnterpriseChangeConstants.ATTRIBUTE_CHANGE_DISCIPLINE_DESIGN);
			relWhere.append("] == ");
			relWhere.append(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE);

			String objectTypes = "";
			String objectTempTypes = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChange." + EnterpriseChangeConstants.ATTRIBUTE_CHANGE_DISCIPLINE_DESIGN.replaceAll(" ", "") + ".ApplicableItemTypes");
			if(objectTempTypes!=null && !objectTempTypes.equalsIgnoreCase("")){
				StringList strListTypes = FrameworkUtil.split(objectTempTypes, ",");
				if(strListTypes!=null && strListTypes.size()>0){
					for(int i=0;i<strListTypes.size();i++){
						String objectType = PropertyUtil.getSchemaProperty(context,(String)strListTypes.get(i));
						if(objectTypes!=null && !objectTypes.equalsIgnoreCase("")){
							objectTypes = objectTypes + ",";
						}
						objectTypes = objectTypes + objectType;
					}
				}
			}

			MapList relatedApplicableItems = decisionObj.getRelatedObjects(context,
					EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM ,
					objectTypes,
					new StringList(DomainConstants.SELECT_ID),
					new StringList(DomainConstants.SELECT_RELATIONSHIP_ID),
					false,	//to relationship
					true,	//from relationship
					(short)1,
					DomainConstants.EMPTY_STRING, // object where clause
					relWhere.toString(), // relationship where clause
					0);

			Iterator tempIterator = relatedApplicableItems.iterator();
			while(tempIterator.hasNext()){
				Map tempMap = (Map)tempIterator.next();
				String tempId = (String)tempMap.get(DomainConstants.SELECT_ID);
				returnStringList.add(tempId);
			}

		}catch (Exception e){
			System.out.println(e.getMessage());
		}finally{
			return returnStringList;
		}
	}

	/**
	 * Exclude manufacturing object already connected to Decision as Applicable Item
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return StringList - containing id of manufacturing object already connected to Decision as Applicable Item
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeDecisionManufacturingApplicableItems(Context context, String[] args)throws Exception{
		StringList returnStringList = new StringList();
		try{
			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String decisionId = (String) paramMap.get("objectId");

			DomainObject decisionObj = new DomainObject(decisionId);
			StringList selectStmts = new StringList(1);
			selectStmts.addElement(SELECT_ID);

			StringList selectRelStmts = new StringList(1);
			selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

			StringBuffer relWhere = new StringBuffer();
			relWhere.append(DomainConstants.EMPTY_STRING);
			relWhere.append("attribute[");
			relWhere.append(EnterpriseChangeConstants.ATTRIBUTE_CHANGE_DISCIPLINE_MANUFACTURING);
			relWhere.append("] == ");
			relWhere.append(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE);

			String objectTypes = "";
			String objectTempTypes = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChange." + EnterpriseChangeConstants.ATTRIBUTE_CHANGE_DISCIPLINE_MANUFACTURING.replaceAll(" ", "") + ".ApplicableItemTypes");
			if(objectTempTypes!=null && !objectTempTypes.equalsIgnoreCase("")){
				StringList strListTypes = FrameworkUtil.split(objectTempTypes, ",");
				if(strListTypes!=null && strListTypes.size()>0){
					for(int i=0;i<strListTypes.size();i++){
						String objectType = PropertyUtil.getSchemaProperty(context,(String)strListTypes.get(i));
						if(objectTypes!=null && !objectTypes.equalsIgnoreCase("")){
							objectTypes = objectTypes + ",";
						}
						objectTypes = objectTypes + objectType;
					}
				}
			}

			MapList relatedApplicableItems = decisionObj.getRelatedObjects(context,
					EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM ,
					objectTypes,
					new StringList(DomainConstants.SELECT_ID),
					new StringList(DomainConstants.SELECT_RELATIONSHIP_ID),
					false,	//to relationship
					true,	//from relationship
					(short)1,
					DomainConstants.EMPTY_STRING, // object where clause
					relWhere.toString(), // relationship where clause
					0);

			Iterator tempIterator = relatedApplicableItems.iterator();
			while(tempIterator.hasNext()){
				Map tempMap = (Map)tempIterator.next();
				String tempId = (String)tempMap.get(DomainConstants.SELECT_ID);
				returnStringList.add(tempId);
			}

		}catch (Exception e){
			System.out.println(e.getMessage());
		}finally{
			return returnStringList;
		}
	}


	/**
	 * Exclude manufacturing object already connected to Decision as Manufacturing Intent Implemented At
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return StringList - containing id of manufacturing object already connected to Decision as Manufacturing Intent Implemented At
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeDecisionManufacturingIntentImplementedAt(Context context, String[] args)throws Exception{
		StringList returnStringList = new StringList();
		try{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			//objectId = Manufacturing Intent object
			String objectId = (String) programMap.get("objectId");
			//relId = Manufacturing Applicability Intent
			String relId = (String) programMap.get("relId");

			if(relId!=null && !relId.equalsIgnoreCase("")){
				//String strMQL = MqlUtil.mqlCommand(context, "print connection " + relId + " select frommid.id dump |");
				String strMQL = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump $3",relId,"frommid.id","|");
				StringList stringlist = FrameworkUtil.split(strMQL, "|");
				if(stringlist!=null && stringlist.size()>0){
					for(int i=0;i<stringlist.size();i++){
						String tempRelId = (String)stringlist.get(i);
						if(tempRelId!=null && !tempRelId.equalsIgnoreCase("")){
							//String strMQL2 = MqlUtil.mqlCommand(context, "print connection " + tempRelId + " select to.id dump |");
							String strMQL2 = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump $3",tempRelId,"to.id","|");
							returnStringList = FrameworkUtil.split(strMQL2, "|");
						}
					}
				}
			}
		}catch (Exception e){
			System.out.println(e.getMessage());
		}finally{
			return returnStringList;
		}
	}


	/**
	 * Display Manufacturing Applicable Item Manufacturing Intent
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return MapList - return list of Manufacturing Intent
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getManufacturingApplicableItemsManufacturingIntent(Context context, String[] args) throws Exception{
		MapList returnItemList = new MapList();
		try{
			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String manufacturingPlanId = (String) paramMap.get("objectId");
			String relId = (String) paramMap.get("relId");

			DomainObject manufacturingPlanObj = DomainObject.newInstance(context, manufacturingPlanId);

			StringList stringlist = new StringList();

			//String strMQL = MqlUtil.mqlCommand(context, "print connection " + relId + " select frommid.id dump |");
			String strMQL = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump $3",relId,"frommid.id","|");
			stringlist = FrameworkUtil.split(strMQL, "|");

			if(stringlist!=null && stringlist.size()>0){
				for(int i=0;i<stringlist.size();i++){
					String tempRelId = (String)stringlist.get(i);
					if(tempRelId!=null && !tempRelId.equalsIgnoreCase("")){
						DomainRelationship domRel = new DomainRelationship(tempRelId);
						StringList selectRelStmts = new StringList(1);
						selectRelStmts.addElement(DomainConstants.SELECT_TYPE);
						selectRelStmts.addElement(DomainConstants.SELECT_TO_ID);
						MapList domRelInfos = domRel.getInfo(context,new String[]{tempRelId},selectRelStmts);
						if(domRelInfos!=null && !domRelInfos.isEmpty()){
							Map domRelInfo = (Map)domRelInfos.get(0);
							if(domRelInfo!=null && !domRelInfo.isEmpty()){
								String domRelType = (String)domRelInfo.get(DomainConstants.SELECT_TYPE);
								if(domRelType.equalsIgnoreCase(EnterpriseChangeConstants.RELATIONSHIP_MANUFACTURING_APPLICABILITY_INTENT)){
									String toId = (String)domRelInfo.get(DomainConstants.SELECT_TO_ID);
									if(toId!=null && !toId.isEmpty()){
										DomainObject domTemp = new DomainObject(toId);
										String tempObjId = (String)domTemp.getInfo(context, DomainConstants.SELECT_ID);
										if(tempObjId!=null && !tempObjId.equalsIgnoreCase("")){
											Map tempMap = new HashMap();
											tempMap.put("relationship",EnterpriseChangeConstants.RELATIONSHIP_MANUFACTURING_APPLICABILITY_INTENT);
											tempMap.put("level","2");
											tempMap.put(DomainConstants.SELECT_RELATIONSHIP_ID,tempRelId);
											tempMap.put(DomainConstants.SELECT_ID,tempObjId);
											returnItemList.add(tempMap);
										}
									}
								}
							}
						}
					}
				}
			}

		}catch (Exception e){
			throw e;
		}finally{
			return returnItemList;
		}
	}


	/**
	 * Display Manufacturing Applicable Item Implemented At
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return Vector - containing the Implemented At object
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public Vector getManufacturingApplicabilityImplementedAt(Context context, String[] args)throws Exception{
		//XSSOK
		Vector returnVector = new Vector();
		try{
			String prefixItemUrl = "<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=";
			String itemId = null;
			String suffixItemUrl = "', '930', '650', 'false', 'popup', '')\" class=\"object\">";
			String defaultItemIcon = null;
			String itemType = null;
			String itemName = null;
			String itemRevision = null;
			String itemTypeIcon = null;
			String anchorEnd = "</a>";

			Map programMap = (HashMap) JPO.unpackArgs(args);

			Map paramMap = (Map) programMap.get("paramList");
			String strReportFormat = (String) paramMap.get("reportFormat");

			MapList objectList = (MapList) programMap.get("objectList");
			Iterator objectListItr = objectList.iterator();

			while(objectListItr.hasNext()){

				StringBuffer stbTNR = new StringBuffer();
				Map objectMap = (Map) objectListItr.next();
				String strObjIdtemp = (String)objectMap.get(DomainConstants.SELECT_ID);
				String strRelIdtemp = (String)objectMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);

				if(strRelIdtemp!=null && !strRelIdtemp.equalsIgnoreCase("")){
					StringList stringlist = new StringList();

					//String strMQL = MqlUtil.mqlCommand(context, "print connection " + strRelIdtemp + " select frommid.id dump |");
					String strMQL = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump $3",strRelIdtemp,"frommid.id","|");
					stringlist = FrameworkUtil.split(strMQL, "|");

					if(stringlist!=null && stringlist.size()>0){
						for(int i=0;i<stringlist.size();i++){
							String tempRelId = (String)stringlist.get(i);
							if(tempRelId!=null && !tempRelId.equalsIgnoreCase("")){
								DomainRelationship domRel = new DomainRelationship(tempRelId);
								StringList selectRelStmts = new StringList(1);
								selectRelStmts.addElement(DomainConstants.SELECT_TYPE);
								selectRelStmts.addElement(DomainConstants.SELECT_TO_ID);
								selectRelStmts.addElement(DomainConstants.SELECT_TO_TYPE);
								selectRelStmts.addElement(DomainConstants.SELECT_TO_NAME);
								selectRelStmts.addElement(DomainConstants.SELECT_TO_REVISION);
								MapList domRelInfos = domRel.getInfo(context,new String[]{tempRelId},selectRelStmts);
								if(domRelInfos!=null && !domRelInfos.isEmpty()){
									Map domRelInfo = (Map)domRelInfos.get(0);
									if(domRelInfo!=null && !domRelInfo.isEmpty()){
										String domRelType = (String)domRelInfo.get(DomainConstants.SELECT_TYPE);
										if(domRelType.equalsIgnoreCase(EnterpriseChangeConstants.RELATIONSHIP_MANUFACTURING_APPLICABILITY_IMPLEMENTED_AT)){

											String toId = (String)domRelInfo.get(DomainConstants.SELECT_TO_ID);
											itemType = (String)domRelInfo.get(DomainConstants.SELECT_TO_TYPE);
											itemName = (String)domRelInfo.get(DomainConstants.SELECT_TO_NAME);
											itemRevision = (String)domRelInfo.get(DomainConstants.SELECT_TO_REVISION);


											if(toId!=null && !toId.isEmpty()){
												if (null== strReportFormat || "null".equalsIgnoreCase(strReportFormat) || "".equals(strReportFormat)){
												DomainObject domTemp = new DomainObject(toId);
												//String tempObjId = (String)domTemp.getInfo(context, DomainConstants.SELECT_ID);
												if(toId!=null && !toId.equalsIgnoreCase("")){
													//if(tempObjId!=null && !tempObjId.equalsIgnoreCase("")){
													//itemId = tempObjId;
													itemId = toId;
													DomainObject domObject = new DomainObject(itemId);
													//itemType = (String)domObject.getInfo(context, DomainConstants.SELECT_TYPE);
													//itemName = (String)domObject.getInfo(context, DomainConstants.SELECT_NAME);
													//itemRevision = (String)domObject.getInfo(context, DomainConstants.SELECT_REVISION);

													// Find type icon
													String strTypeSymName = FrameworkUtil.getAliasForAdmin(context, "Type", itemType, true);
													itemTypeIcon = EnoviaResourceBundle.getProperty(context,"emxFramework.smallIcon." + strTypeSymName);
													defaultItemIcon = "<img src=\"../common/images/" +itemTypeIcon+ "\" border=\"0\" />";

													stbTNR.append(prefixItemUrl);
													stbTNR.append(itemId);
													stbTNR.append(suffixItemUrl);
													stbTNR.append(defaultItemIcon);
													stbTNR.append(XSSUtil.encodeForHTMLAttribute(context,itemName));
													stbTNR.append(" ");
													stbTNR.append(itemRevision);
													stbTNR.append(anchorEnd);

													stbTNR.append("<br />");
												}else{stbTNR.append("");}
											}else{
												stbTNR.append(itemName);
											}
										}else{stbTNR.append("");}
											// }
										}else{stbTNR.append("");}
									}else{stbTNR.append("");}
								}else{stbTNR.append("");}
							}else{stbTNR.append("");}
						}
					}else{stbTNR.append("");}
				}else{stbTNR.append("");}

				returnVector.add(stbTNR.toString());
			}

		}catch (Exception e){
			System.out.println(e.getMessage());
		}finally{
			return returnVector;
		}
	}


	/**
	 * Method to connect Manufacturing Intent to Manufacturing Applicable item
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return String - containing the error message
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public String connectManufacturingApplicableItemManufacturingIntent(Context context, String[] args)throws Exception{
		String errorMsg = "";
		try{
			HashMap paramMap = (HashMap)JPO.unpackArgs(args);

			String strContextId = (String) paramMap.get("objectId");
			String strContextRelId = (String) paramMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
			String[] emxTableRowIds = (String[]) paramMap.get("emxTableRowId");

			String strTableRowId = "";
			StringList slEmxTableRowId = new StringList();
			for (int i = 0; i < emxTableRowIds.length; i++){
				strTableRowId = emxTableRowIds[i];
				slEmxTableRowId = FrameworkUtil.split(strTableRowId, "|");
				if (slEmxTableRowId.size() > 0){
					strTableRowId = (String)slEmxTableRowId.get(0);
					//String strCommand = "add connection \"" + EnterpriseChangeConstants.RELATIONSHIP_MANUFACTURING_APPLICABILITY_INTENT + "\" fromrel " + strContextRelId + " to " + strTableRowId + " select id dump |;";
					String strCommand = "add connection $1 fromrel $2 to $3 select $4 dump $5";
					String strNewRelId = MqlUtil.mqlCommand(context,strCommand,EnterpriseChangeConstants.RELATIONSHIP_MANUFACTURING_APPLICABILITY_INTENT,strContextRelId,strTableRowId,"id","|");

					//Automatically link same object as Applicable Item Implements At if Applicable Item Implements is not a Manufacturing Plan
					if((strNewRelId!=null && !strNewRelId.equalsIgnoreCase(""))
							&& (!(new DomainObject(strTableRowId).isKindOf(context, EnterpriseChangeConstants.TYPE_MANUFACTURING_PLAN)))){
						//String strCommand2 = "add connection '" + EnterpriseChangeConstants.RELATIONSHIP_MANUFACTURING_APPLICABILITY_IMPLEMENTED_AT + "' fromrel " + strNewRelId + " to " + strTableRowId + ";";
						String strCommand2 = "add connection $1 fromrel $2 to $3";
						String returnMessage2 = MqlUtil.mqlCommand(context,strCommand2,EnterpriseChangeConstants.RELATIONSHIP_MANUFACTURING_APPLICABILITY_IMPLEMENTED_AT,strNewRelId,strTableRowId);
					}
				}
			}
		}catch (Exception e){
			errorMsg = e.getMessage();
		}finally{
			return errorMsg;
		}
	}


	/**
	 * Method to update Manufacturing Applicable Item Upward Compatibility
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return void -
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public void updateManufacturingApplicableItemsUpwardCompatibility(Context context, String[] args) throws Exception{
		try{
			Map programMap = (HashMap) JPO.unpackArgs(args);

			HashMap requestMap = (HashMap) programMap.get("requestMap");
			//Object parentOID = Decision
			String parentOID = (String) requestMap.get("parentOID");

			if(parentOID!=null && !parentOID.equalsIgnoreCase("")){
				DomainObject domParentOID = new DomainObject(parentOID);
				if(!domParentOID.getInfo(context, DomainConstants.SELECT_CURRENT).equalsIgnoreCase(EnterpriseChangeConstants.STATE_DECISION_RELEASE)
						&& !domParentOID.getInfo(context, DomainConstants.SELECT_CURRENT).equalsIgnoreCase(EnterpriseChangeConstants.STATE_DECISION_SUPERCEDED)){
					HashMap paramMap = (HashMap) programMap.get("paramMap");
					String relId = (String) paramMap.get("relId");
					String newValue = (String) paramMap.get("New Value");
					if(relId!=null && !relId.equalsIgnoreCase("")){
						DomainRelationship domRelId = new DomainRelationship(relId);
						if(newValue!=null && !newValue.equalsIgnoreCase("")){
							domRelId.setAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_UPWARD_COMPATIBILITY, newValue);
						}
					}
				}else{
					String language = context.getSession().getLanguage();
					String strLanguage = context.getSession().getLanguage();
					String sStrAlert = (EnoviaResourceBundle.getProperty(context,"EnterpriseChange",
							"emxEntepriseChange.DecisionRelease.CannotUpdateUpwardCompatibility",strLanguage))
							.trim();
					MQLCommand mql = new MQLCommand();
					//boolean boolResult = mql.executeCommand(context, "notice '" + sStrAlert + "'");
					boolean boolResult = mql.executeCommand(context, "notice $1", sStrAlert);
				}
			}
		}catch (Exception e){
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Method to update Manufacturing Applicable Item Manufacturing Intent
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return void -
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public void updateManufacturingApplicableItemsManufacturingIntent(Context context, String[] args) throws Exception{
		try{
			Map programMap = (HashMap) JPO.unpackArgs(args);

			HashMap requestMap = (HashMap) programMap.get("requestMap");
			//Object parentOID = Decision
			String parentOID = (String) requestMap.get("parentOID");

			if(parentOID!=null && !parentOID.equalsIgnoreCase("")){
				DomainObject domParentOID = new DomainObject(parentOID);
				if(!domParentOID.getInfo(context, DomainConstants.SELECT_CURRENT).equalsIgnoreCase(EnterpriseChangeConstants.STATE_DECISION_RELEASE)
						&& !domParentOID.getInfo(context, DomainConstants.SELECT_CURRENT).equalsIgnoreCase(EnterpriseChangeConstants.STATE_DECISION_SUPERCEDED)){
					HashMap paramMap = (HashMap) programMap.get("paramMap");
					String relId = (String) paramMap.get("relId");
					String newValue = (String) paramMap.get("New Value");
					if(relId!=null && !relId.equalsIgnoreCase("")){
						DomainRelationship domRelId = new DomainRelationship(relId);
						if(newValue!=null && !newValue.equalsIgnoreCase("")){
							domRelId.setAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_MANUFACTURING_INTENT, newValue);
						}
					}
				}else{
					String language = context.getSession().getLanguage();
					String strLanguage = context.getSession().getLanguage();
					String sStrAlert = (EnoviaResourceBundle.getProperty(context,"EnterpriseChange",
							"emxEntepriseChange.DecisionRelease.CannotUpdateManufacturingIntent",strLanguage))
							.trim();
					MQLCommand mql = new MQLCommand();
				//	boolean boolResult = mql.executeCommand(context, "notice '"
				//			+ sStrAlert + "'");
					boolean boolResult = mql.executeCommand(context, "notice $1", sStrAlert);
				}
			}
		}catch (Exception e){
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Method to update Manufacturing Applicable Item Implemented At
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return void -
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public void updateManufacturingApplicabilityImplementedAt(Context context, String[] args) throws Exception{
		try{
			StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
			StringList relationshipSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

			Map programMap = (HashMap) JPO.unpackArgs(args);

			HashMap requestMap = (HashMap) programMap.get("requestMap");
			//Object parentOID = Decision
			String parentOID = (String) requestMap.get("parentOID");

			if(parentOID!=null && !parentOID.equalsIgnoreCase("")){
				DomainObject domParentOID = new DomainObject(parentOID);
				if(!domParentOID.getInfo(context, DomainConstants.SELECT_CURRENT).equalsIgnoreCase(EnterpriseChangeConstants.STATE_DECISION_RELEASE)
						&& !domParentOID.getInfo(context, DomainConstants.SELECT_CURRENT).equalsIgnoreCase(EnterpriseChangeConstants.STATE_DECISION_SUPERCEDED)){
					HashMap paramMap = (HashMap) programMap.get("paramMap");

					//Object Id = Object To relationship Manufacturing Intent (Model, Product, ...)
					String objectId = (String) paramMap.get("objectId");
					String relId = (String) paramMap.get("relId");

					//newApplicableItemImplementsAtId = new Applicable Item Implements At
					String newApplicableItemImplementsAtId = (String) paramMap.get("New Value");

					if(relId!=null && !relId.equalsIgnoreCase("")){
						//String strMQL = MqlUtil.mqlCommand(context, "print connection " + relId + " select frommid.id dump |");
						String strMQL = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump $3",relId,"frommid.id","|");
						StringList stringlist = FrameworkUtil.split(strMQL, "|");
						if(stringlist!=null && stringlist.size()>0){
							for(int i=0;i<stringlist.size();i++){
								String tempRelId = (String)stringlist.get(i);
								if(tempRelId!=null && !tempRelId.equalsIgnoreCase("")){
									DomainRelationship.disconnect(context, tempRelId, false);
								}
							}
						}

						if(newApplicableItemImplementsAtId!=null && !newApplicableItemImplementsAtId.equalsIgnoreCase("")){
							//String strCommand = "verb on;add connection '" + EnterpriseChangeConstants.RELATIONSHIP_MANUFACTURING_APPLICABILITY_IMPLEMENTED_AT + "' fromrel " + relId + " to " + newApplicableItemImplementsAtId + ";verb off;";
							String strCommand = "add connection $1 fromrel $2 to $3";
							String returnMessage = MqlUtil.mqlCommand(context,strCommand,EnterpriseChangeConstants.RELATIONSHIP_MANUFACTURING_APPLICABILITY_IMPLEMENTED_AT,relId,newApplicableItemImplementsAtId);
						}
					}
				}else{
					String language = context.getSession().getLanguage();
					String strLanguage = context.getSession().getLanguage();
					String sStrAlert = (EnoviaResourceBundle.getProperty(context,"EnterpriseChange",
							"emxEntepriseChange.DecisionRelease.CannotUpdateManufacturingIntentImplementedAt",strLanguage))
							.trim();
					MQLCommand mql = new MQLCommand();
				//	boolean boolResult = mql.executeCommand(context, "notice '"
				//			+ sStrAlert + "'");
					boolean boolResult = mql.executeCommand(context, "notice $1", sStrAlert);
				}
			}
		}catch (Exception e){
			System.out.println(e.getMessage());
		}
	}


	/**
	 * Method to refresh the table after clicking on Apply
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return HashMap -
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public HashMap postProcessRefresh (Context context, String[] args) throws Exception{
		// unpack the incoming arguments
		HashMap programMap = (HashMap)JPO.unpackArgs(args);

		HashMap returnMap = new HashMap(1);
		returnMap.put("Action","refresh");
		return returnMap;
	}


	/**
	 * Method to get the cell level access for Implemented At on Manufacturing Applicable Item table
	 *
	 * @param context the eMatrix Context object
	 * @param String array contains Meetings Ids for edit
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public StringList getManufacturingApplicabilityImplementedAtEditAccess(Context context, String args[]) throws Exception{
		StringList returnStringList = new StringList();
		try{
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");

			if(objectList!=null && !objectList.isEmpty()){
				Iterator objectListItr = objectList.iterator();
				while(objectListItr.hasNext()){
					StringBuffer stbTNR = new StringBuffer();
					Map object = (Map)objectListItr.next();
					if(object!=null && !object.isEmpty()){
						String objectId = (String)object.get(DomainConstants.SELECT_ID);
						if(objectId!=null && !objectId.isEmpty()){
							DomainObject objectDom = new DomainObject(objectId);
							if(objectDom.isKindOf(context, EnterpriseChangeConstants.TYPE_MANUFACTURING_PLAN)){
								returnStringList.addElement(new Boolean(false));
							}else{
								returnStringList.addElement(new Boolean(true));
							}
						}
					}
				}
			}
		}catch (Exception e){
			System.out.println(e.getMessage());
		}finally{
			return returnStringList;
		}
	}

	/**
	 * Method to remove unneeded Decision Applicable Item objects
	 * when a Decision Applies To object is disconnected to avoid inconsistent Decision in term of Change Discipline
	 *
	 * @param context the eMatrix Context object
	 * @param String array contains Decision Id
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public void removeDecisionAppliesTo(Context context, String args[]) throws Exception{
		try{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			//objectId = Decision id
			String objectId = (String)programMap.get("objectId");

			if(objectId!=null && !objectId.isEmpty()){
				Decision decision = new Decision(objectId);
				StringList changeDisciplines = EnterpriseChangeUtil.getChangeDisciplines(context);
				StringList authorizedChangeDisciplines = decision.getAuthorizedChangeDisciplinesForApplicability(context);

				MapList applicableItems = decision.getApplicableItems(context, null);

				Iterator<Map<String,String>> applicableItemsItr = applicableItems.iterator();
				while (applicableItemsItr.hasNext()) {
					Map<String,String> applicableItem = applicableItemsItr.next();
					if (applicableItem!=null && !applicableItem.isEmpty()) {
						String relId = applicableItem.get(DomainConstants.SELECT_RELATIONSHIP_ID);
						DomainRelationship relDom = new DomainRelationship(relId);
						Iterator<String> changeDisciplinesItr = changeDisciplines.iterator();
						Integer selectedDisciplines = 0;
						while (changeDisciplinesItr.hasNext()) {
							String changeDiscipline = changeDisciplinesItr.next();
							if (changeDiscipline!=null && !changeDiscipline.isEmpty()) {
								String applicableItemChangeDisciplineValue = applicableItem.get("attribute[" + changeDiscipline + "]");
								if (applicableItemChangeDisciplineValue.equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE)) {
									if (!authorizedChangeDisciplines.contains(changeDiscipline)) {
										relDom.setAttributeValue(context, changeDiscipline, EnterpriseChangeConstants.CHANGE_DISCIPLINE_FALSE);
									} else {
										selectedDisciplines++;
									}
								}
							}
						}//End of while
						if (selectedDisciplines<=0) {
							DomainRelationship.disconnect(context, relId, false);
						}
					}
				}
			}
		}catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Method to promote the Decision object to released state
	 *
	 * @param context the eMatrix Context object
	 * @param String array
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212
	 */
	public void	promoteDecisiontoReleaseState(Context context, String args[]) throws Exception{
		try{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			MapList mlError = new MapList();
			String strDecisionObjId = (String)programMap.get("objectId");
			DomainObject domObjID = new DomainObject(strDecisionObjId);
			String strType = domObjID.getInfo(context,SELECT_TYPE);
			String strDecisionState = domObjID.getInfo(context,SELECT_CURRENT);
			String SELECT_CHANGE_TASK = "from["+RELATIONSHIP_DECISION + "].to.id";
			if(mxType.isOfParentType(context,strType,EnterpriseChangeConstants.TYPE_DECISION) && "Active".equals(strDecisionState)){
				PropertyUtil.setGlobalRPEValue(context,"DecisionRelease","true");
				domObjID.promote(context);
			}
		}catch(Exception e){
			System.out.println(e.getMessage());
			throw e;
		}
	}

	/**
	 * Method called on the check trigger for Decision active state
	 *
	 * @param context the eMatrix Context object
	 * @param String array
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212
	 */
	public int decisionActivetoReleaseCheckTrigger (Context context, String[] args) throws Exception{
		try{
			String strDecisionObjId = args[0];
			DomainObject decisionObj = DomainObject.newInstance(context, strDecisionObjId);

			StringList selectStmts = new StringList();
			selectStmts.addElement(SELECT_ID);
			String strType = TYPE_CHANGE_TASK;
			String strRelationship = RELATIONSHIP_DECISION + "," + RELATIONSHIP_DECISION_APPLIES_TO ;
			MapList changeTaskList = new MapList();
			changeTaskList = decisionObj.getRelatedObjects(context,
					strRelationship,        // relationship pattern
					strType,               // object pattern
					selectStmts,       // object selects
					null,    // relationship selects
					false,             // to direction
					true,              // from direction
					(short) 1,         // recursion level
					null,              // object where clause
					null,              // relationship where clause
					0);            // use cache

			Integer intReturn = 0;
			String strChangeTaskObjId = "";

			Map mapTaskData = new HashMap();
			if(changeTaskList.size() >0){
				for (Iterator itrObjects = changeTaskList.iterator(); itrObjects.hasNext();)
				{
					mapTaskData = (Map) itrObjects.next();
					strChangeTaskObjId = (String)mapTaskData.get(SELECT_ID);
					DomainObject changeTaskObj = DomainObject.newInstance(context, strChangeTaskObjId);
					String strAppProp = changeTaskObj.getAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_APPLICABILITY_PROPAGATED);

					if (strAppProp != null && !"".equals(strAppProp) && RANGE_YES.equalsIgnoreCase(strAppProp)) {
						String strTrackApplicability = decisionObj.getAttributeValue(context,  EnterpriseChangeConstants.ATTRIBUTE_TRACK_APPLICABILITY);
						if ("Yes".equals(strTrackApplicability)) {
							String strCreateModel = PropertyUtil.getGlobalRPEValue(context,"DecisionRelease");
							if("true".equals(strCreateModel)){
								intReturn = 0;
							}else{
								intReturn = 1;
								String errorMessage = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Error.DecisionReleaseFromPropertiesPage",context.getSession().getLanguage());
								emxContextUtil_mxJPO.mqlError(context, errorMessage);
							}
						}
						else{
							intReturn = 0;
						}
					}
				}
			}
			return intReturn;
		}catch (Exception ex){
			PropertyUtil.setGlobalRPEValue(context,"ErrorMsg","No");
			ContextUtil.abortTransaction(context);
			throw ex;
		}
	}

	/**
	 * Method called in the command ECHDecisionReleaseLifecycle to decide whether
	 * to display the Release command in the decision's properties page or not.
	 *
	 * @param context the eMatrix Context object
	 * @param String array
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212
	 */
	public boolean showReleaseCommand (Context context, String[] args)
	throws Exception
	{
		boolean showCommand = false;
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String strChangeTaskObjId = "";
		String strDecisionObjId = (String)programMap.get("objectId");
		DomainObject decisionObj = DomainObject.newInstance(context, strDecisionObjId);
		String strdecState = decisionObj.getInfo(context, SELECT_CURRENT);

		if(EnterpriseChangeConstants.STATE_DECISION_RELEASE .equals(strdecState) || EnterpriseChangeConstants.STATE_DECISION_SUPERCEDED.equals(strdecState) ){
			showCommand = false;
		}else{
			StringList selectStmts = new StringList();
			selectStmts.addElement(SELECT_ID);
			String strType = TYPE_CHANGE_TASK;
			String strRelationship = RELATIONSHIP_DECISION + "," + RELATIONSHIP_DECISION_APPLIES_TO ;
			MapList changeTaskList = new MapList();
			changeTaskList = decisionObj.getRelatedObjects(context,
					strRelationship,        // relationship pattern
					strType,               // object pattern
					selectStmts,       // object selects
					null,    // relationship selects
					false,             // to direction
					true,              // from direction
					(short) 1,         // recursion level
					null,              // object where clause
					null,              // relationship where clause
					0);            // use cache

			Map mapTaskData = new HashMap();
			if(changeTaskList.size() > 0){
				for (Iterator itrObjects = changeTaskList.iterator(); itrObjects.hasNext();)
				{
					mapTaskData = (Map) itrObjects.next();
					strChangeTaskObjId = (String)mapTaskData.get(SELECT_ID);


					if(!"null".equals(strChangeTaskObjId) && null != strChangeTaskObjId && !"".equals(strChangeTaskObjId)){
						DomainObject changeTaskObj = DomainObject.newInstance(context, strChangeTaskObjId);
						String strAppProp = changeTaskObj.getAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_APPLICABILITY_PROPAGATED);

						if (strAppProp != null && !"".equals(strAppProp) && RANGE_YES.equalsIgnoreCase(strAppProp)) {
							//get all Decisions connected where Track Applicability attribute is yes
							String strTrackApplicability = decisionObj.getAttributeValue(context,  EnterpriseChangeConstants.ATTRIBUTE_TRACK_APPLICABILITY);
							if ("Yes".equals(strTrackApplicability)) {
								showCommand = true;
							}else{
								showCommand = false;
							}
						}
					}
				}
			}
		}
		return showCommand;

	}

	/**
	 * Trigger Method called in the relationship "Task Deliverable" create check to decide whether
	 * to allow the selected change objects as deliverable in the change task
	 * if the change task applicability is already enabled and decision is already released.
	 *
	 * @param context the eMatrix Context object
	 * @param String array
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212
	 */

	public int connectChangeTasktoDeliverableCheckTrigger (Context context, String[] args) throws Exception
	{
		try{
			Integer intReturn = 0;
			String strchangeTaskId = args[0];
			String strchangeObjectId = args[1];
			DomainObject dmoChangeTask = new DomainObject(strchangeTaskId);
			String strType = dmoChangeTask.getInfo(context, SELECT_TYPE);
			String strResult = "";

			DomainObject dmoChangeObject = new DomainObject(strchangeObjectId);
			String strChnageObjectType = dmoChangeObject.getInfo(context, SELECT_TYPE);

			if(TYPE_CHANGE_TASK.equals(strType)){
				String strAppProp = dmoChangeTask.getAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_APPLICABILITY_PROPAGATED);

				String SELECT_DECISION = "to["+DomainConstants.RELATIONSHIP_DECISION_APPLIES_TO + "].from.id";
				String strDecision = dmoChangeTask.getInfo(context, SELECT_DECISION);
				if(null != strDecision && !"null".equals(strDecision) && !"".equals(strDecision)){
					DomainObject dmoDecision = new DomainObject(strDecision);
					String strDecisionState = dmoDecision.getInfo(context, SELECT_CURRENT);


					if(RANGE_YES .equals(strAppProp) && EnterpriseChangeConstants.STATE_DECISION_RELEASE .equals(strDecisionState)){
						HashMap paramMap = new HashMap();
						paramMap.put("changeTaskObjectID", strchangeTaskId);
						paramMap.put("changeObjectID", strchangeObjectId);
						String[] methodargs = JPO.packArgs(paramMap);

						strResult = canPropagate(context,strchangeObjectId,strChnageObjectType, strchangeTaskId);
						if(null != strResult && strResult.length() != 0 ){
							intReturn = 1;
						}
					}
				}
			}
			if(intReturn == 1){
				String strLanguage = context.getSession().getLanguage();
				String sStrAlert = (EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.NoChangeObjectAdded.Alert",strLanguage)).trim();
				MQLCommand mql = new MQLCommand();
				//boolean boolResult=  mql.executeCommand(context, "notice '"+sStrAlert+"'");
				boolean boolResult= mql.executeCommand(context, "notice $1", sStrAlert);
			}
			return intReturn;

		}catch (Exception ex){
			System.out.println(ex.getMessage());
			ContextUtil.abortTransaction(context);
			throw ex;
		}
	}

	/**
	 * Method to determine if Decision Applicability command should be displayed
	 * If Derivation is enabled and the Decision Applies To to at least one Change Task then the command can be displayed
	 * @param context
	 * @param args
	 * @throws Exception
	 * @since EnterpriseChange R212.HFDerivation
	 */
	public Boolean showDecisionApplicability(Context context, String[] args) throws Exception {
		try {
			Boolean returnBoolean = false;
			//if (EnterpriseChangeUtil.isDerivationEnabled(context)) {
				HashMap programMap = (HashMap)JPO.unpackArgs(args);
				//objectId is Decision ID
				String objectId = (String)programMap.get("objectId");
				if (objectId!=null && !objectId.isEmpty()) {
					Decision decision = new Decision(objectId);
					StringList authorizedChangeDisciplines = decision.getAuthorizedChangeDisciplinesForApplicability(context);
					if (authorizedChangeDisciplines!=null && !authorizedChangeDisciplines.isEmpty()) {
						returnBoolean = true;
					}
				}
			//}
			return returnBoolean;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Method to determine if Decision Manufacturing Intent command should be displayed
	 * If Derivation is enabled and the Decision Applies To to at least one Change Task with Manufacturing Change Discipline then the command can be displayed
	 * @param context
	 * @param args
	 * @throws Exception
	 * @since EnterpriseChange R212.HFDerivation
	 */
	public Boolean showDecisionManufacturingIntents(Context context, String[] args) throws Exception {
		try {
			Boolean returnBoolean = false;
			//if (EnterpriseChangeUtil.isDerivationEnabled(context)) {
				HashMap programMap = (HashMap)JPO.unpackArgs(args);
				//objectId is Decision ID
				String objectId = (String)programMap.get("objectId");
				if (objectId!=null && !objectId.isEmpty()) {
					Decision decision = new Decision(objectId);
					StringList authorizedChangeDisciplines = decision.getAuthorizedChangeDisciplinesForApplicability(context);
					if (authorizedChangeDisciplines!=null && !authorizedChangeDisciplines.isEmpty()) {
						if (authorizedChangeDisciplines.contains(EnterpriseChangeConstants.ATTRIBUTE_CHANGE_DISCIPLINE_MANUFACTURING)) {
							returnBoolean = true;
						}
					}
				}
			//}
			return returnBoolean;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Get the Common Models For Applicability
	 * The system will retrieve the Decision Change Tasks. The system will then find the common Change Task Models defined as Applicability Context
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * 	- objectId String: corresponds to the Decision Id
	 * @return MapList: containing the Common Models For Applicability
	 * @throws Exception if error during processing
	 * @since EnterpriseChange R212_HFDerivations
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getDecisionCommonModelsForApplicability(Context context, String[] args) throws Exception {
		try {
			MapList returnMapList = new MapList();
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String) programMap.get("objectId");
			if (objectId!=null && !objectId.isEmpty()) {
				Decision decision = new Decision(objectId);
				StringList commonModels = decision.getCommonModelsForApplicability(context);
				if (commonModels!=null && !commonModels.isEmpty()) {
					Iterator<String> commonModelsItr = commonModels.iterator();
					while (commonModelsItr.hasNext()) {
						String commonModelId = commonModelsItr.next();
						if (commonModelId!=null && !commonModelId.isEmpty()) {
							Map<String,String> returnMap = new HashMap<String,String>();
							returnMap.put(DomainConstants.SELECT_ID, commonModelId);
							returnMapList.add(returnMap);
						}
					}
				}
			}
			return returnMapList;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Generate the needed Decision Applicability column according to the authorized Change Disciplines
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * 	- objectId String: corresponds to the Decision Id
	 * @return MapList: containing the Columns to display with the proper settings
	 * @throws Exception if error during processing
	 * @since EnterpriseChange R212_HFDerivations
	 */
	public MapList getDecisionApplicabilityChangeDisciplineColumns(Context context, String args[]) throws Exception {
		try {
			boolean isCFFInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionEffectivityFramework",false,null,null);

			MapList returnMapList = new MapList();

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String languageStr = (String)requestMap.get("languageStr");
			//objectId is the DecisionID
			String objectId = (String) requestMap.get("objectId");

			if (objectId!=null && !objectId.isEmpty()) {
				Decision decision = new Decision(objectId);
				String decisionCurrent = decision.getInfo(context, DomainConstants.SELECT_CURRENT);

				StringList changeDisciplines = decision.getAuthorizedChangeDisciplinesForApplicability(context);
				if (changeDisciplines!=null && !changeDisciplines.isEmpty()) {
					Iterator<String> changeDisciplinesItr = changeDisciplines.iterator();
					while (changeDisciplinesItr.hasNext()) {
						String changeDiscipline = changeDisciplinesItr.next();
						if (changeDiscipline!=null && !changeDiscipline.isEmpty()) {
							//Map<String,Map<String,Map<String,MapList>>> applicableItems = decision.getApplicabilitySummary(context, null, new StringList(changeDiscipline), null);
							String changeDisciplineNLS = i18nNow.getAttributeI18NString(changeDiscipline,languageStr);

							try {
								String effectivityTypes = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChange." + changeDiscipline.replaceAll(" ", "") + ".ApplicabilityCommands");
								if (effectivityTypes!=null && !effectivityTypes.isEmpty()) {
									StringList effectivityTypesList = FrameworkUtil.split(effectivityTypes, ",");
									Iterator<String> effectivityTypesListItr = effectivityTypesList.iterator();
									while (effectivityTypesListItr.hasNext()) {
										String effectivityType = effectivityTypesListItr.next();
										if (effectivityType!=null && !effectivityType.isEmpty()) {
											HashMap commandInfo = UICache.getCommand(context, effectivityType);
											if (!(commandInfo!=null && !commandInfo.isEmpty())) {
												throw new Exception("Command " + effectivityType + " doesn't exists");
											}
										}
									}

									String colEdit = i18nNow.getI18nString("emxEnterpriseChange.Command.EditAll" ,"emxEnterpriseChangeStringResource",languageStr);
									HashMap columnSettings = new HashMap();
									columnSettings.put("Column Type","programHTMLOutput");
									//if (decisionCurrent.equalsIgnoreCase(EnterpriseChangeConstants.STATE_DECISION_ACTIVE)) {
									//	columnSettings.put("Editable","true");
									//} else {
									//	columnSettings.put("Editable","false");
									//}
									columnSettings.put("function","getDecisionApplicabilityDisciplineSummary");
									columnSettings.put("program","emxApplicabilityDecision");
									columnSettings.put("Mouse Over Popup","enable");
									columnSettings.put("Mass Update","false");
									columnSettings.put("Update Function","updateDecisionApplicabilityDisciplineSummary");
									columnSettings.put("Update Program","emxApplicabilityDecision");
									columnSettings.put("Registered Suite","EnterpriseChange");
									columnSettings.put("Group Header","emxEnterpriseChange.Interface.ChangeDiscipline");
									columnSettings.put("Width","400");
									columnSettings.put("Export","true");

									HashMap newColumn = new HashMap();
									newColumn.put("settings",columnSettings);
									newColumn.put("label",changeDisciplineNLS);
									newColumn.put("name",changeDiscipline);
									newColumn.put("changeDiscipline",changeDiscipline);
									//newColumn.put("applicableItems",applicableItems);
									newColumn.put("effectivityTypes",effectivityTypes);
									HashMap columnSettings1 = new HashMap();
									columnSettings1.put("Column Icon","images/iconActionEdit.gif");
									columnSettings1.put("Column Type","icon");
									columnSettings1.put("Target Location","popup");
									columnSettings1.put("Popup Modal","true");
									columnSettings1.put("Group Header","emxEnterpriseChange.Interface.ChangeDiscipline");
									columnSettings1.put("Registered Suite","EnterpriseChange");
									HashMap newColumn1 = new HashMap();
									newColumn1.put("settings",columnSettings1);
									newColumn1.put("label",colEdit+ " " +changeDisciplineNLS);
									newColumn1.put("name",colEdit+ " " +changeDiscipline);
									newColumn1.put("effectivityTypes",effectivityTypes);
									if (decisionCurrent.equalsIgnoreCase(EnterpriseChangeConstants.STATE_DECISION_ACTIVE)) {
										if (isCFFInstalled) {
											newColumn.put("range","../enterprisechange/ApplicabilityDefinitionDialog.jsp?modetype=edit&changeDiscipline="+changeDiscipline+"&effectivityTypes="+effectivityTypes+"&languageStr="+languageStr+"&suiteKey=Effectivity&StringResourceFileId=EffectivityStringResource&SuiteDirectory=effectivity&postProcessCFFURL=../enterprisechange/ApplicabilityDefinitionDialogProcess.jsp");
											newColumn1.put("href","../enterprisechange/ApplicabilityDefinitionDialog.jsp?modetype=edit&rootObjectId="+objectId+"&formName=emxTableForm&changeDiscipline="+changeDiscipline+"&effectivityTypes="+effectivityTypes+"&languageStr="+languageStr+"&suiteKey=Effectivity&StringResourceFileId=EffectivityStringResource&SuiteDirectory=effectivity&postProcessCFFURL=../enterprisechange/ApplicabilityDefinitionDialogProcess.jsp");
										}
									}
									returnMapList.add(newColumn);
									if(!UINavigatorUtil.isMobile(context)){
									returnMapList.add(newColumn1);
									}
								}
							} catch (Exception e) {
								throw new Exception("Applicability Commands not defined for " + changeDiscipline);
							}
						}
					}//End of while
				}
			}
			return returnMapList;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Get the Decision Applicability summary for the concerned Model Context and Change Discipline
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * 	- objectList MapList: corresponds to the Model Context
	 * 	- columnMap Map: containing the Column Name
	 * @return Vector: containing the the Decision Applicability summary for the concerned Model Context and Change Discipline
	 * @throws Exception if error during processing
	 * @since EnterpriseChange R212_HFDerivations
	 */
	public Vector getDecisionApplicabilityDisciplineSummary(Context context, String[] args) throws Exception {
		try{
			PropertyUtil.setGlobalRPEValue(context, "ENO_ECH", "true");
			Vector returnVector = new Vector();
			Map programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramList = (HashMap)programMap.get("paramList");
			String languageStr = (String)paramList.get("languageStr");
			MapList objectList = (MapList) programMap.get("objectList");
			Map columnMap = (Map) programMap.get("columnMap");
			String changeDiscipline = (String) columnMap.get("name");
			String effectivityTypes = (String) columnMap.get("effectivityTypes");
			// Added to fix IR-201492V6R2014
			String decisionId = (String) paramList.get("objectId");
			Decision decision = new Decision(decisionId);
			Map<String,Map<String,Map<String,MapList>>> applicableItemsSortedByMastersAndDisciplinesAndTypes = decision.getApplicabilitySummary(context, null, new StringList(changeDiscipline), null);
			// Commented to fix IR-201492V6R2014
			//Map<String,Map<String,Map<String,MapList>>> applicableItemsSortedByMastersAndDisciplinesAndTypes = (Map<String,Map<String,Map<String,MapList>>>) columnMap.get("applicableItems");

			Iterator objectListItr = objectList.iterator();
			while (objectListItr.hasNext()) {
				StringBuffer strBuffer = new StringBuffer();
				Map objectMap = (Map) objectListItr.next();
				if ((changeDiscipline!=null && !changeDiscipline.isEmpty()) && (applicableItemsSortedByMastersAndDisciplinesAndTypes!=null && !applicableItemsSortedByMastersAndDisciplinesAndTypes.isEmpty())) {
					if (objectMap!=null && !objectMap.isEmpty()) {
						String objectId = (String) objectMap.get(DomainConstants.SELECT_ID);
						if (objectId!=null && !objectId.isEmpty()) {
							Map<String,Map<String,MapList>> masterApplicableItems = applicableItemsSortedByMastersAndDisciplinesAndTypes.get(objectId);
							if (masterApplicableItems!=null && !masterApplicableItems.isEmpty()) {
								Map<String,MapList> applicableItemsByTypes = masterApplicableItems.get(changeDiscipline);
								if (applicableItemsByTypes!=null && !applicableItemsByTypes.isEmpty()) {
									//Get all Type keys
									Set<String> typeKeys = applicableItemsByTypes.keySet();
									Iterator<String> typeKeysItr = typeKeys.iterator();
									while (typeKeysItr.hasNext()) {
										String typeKey = typeKeysItr.next();
										if (typeKey!=null && !typeKey.isEmpty()) {
											MapList applicableItems = applicableItemsByTypes.get(typeKey);
											if (applicableItems!=null && !applicableItems.isEmpty()) {
												MapList appItemsList = new MapList();
												Iterator<Map<String,String>> applicableItemsItr = applicableItems.iterator();
												while (applicableItemsItr.hasNext()) {
													Map<String,String> applicableItem = applicableItemsItr.next();
													if (applicableItem!=null && !applicableItem.isEmpty()) {
														String applicableItemId = applicableItem.get(DomainConstants.SELECT_ID);
														String applicableItemType = applicableItem.get(DomainConstants.SELECT_TYPE);
														String applicableItemName = applicableItem.get(DomainConstants.SELECT_NAME);
														String applicableItemRevision = applicableItem.get(DomainConstants.SELECT_REVISION);
														String applicableItemBuildUnitNumber = applicableItem.get("attribute[" + EnterpriseChangeConstants.ATTRIBUTE_BUILD_UNIT_NUMBER + "]");
														String applicableItemUpwardCompatibility = applicableItem.get("attribute[" + EnterpriseChangeConstants.ATTRIBUTE_UPWARD_COMPATIBILITY + "]");

														String applicableItemParentId = applicableItem.get("to[" + EnterpriseChangeConstants.RELATIONSHIP_MAIN_PRODUCT + "].from.id");

														if(applicableItemParentId == null || "null".equalsIgnoreCase(applicableItemParentId) || "".equalsIgnoreCase(applicableItemParentId)){
															applicableItemParentId = applicableItem.get("to[" + EnterpriseChangeConstants.RELATIONSHIP_PRODUCTS + "].from.id");
														}

														// still it is null that means Root MP is used in Expression
														if(applicableItemParentId == null || "null".equalsIgnoreCase(applicableItemParentId) || "".equalsIgnoreCase(applicableItemParentId)){
															applicableItemParentId = applicableItem.get("to[" + EnterpriseChangeConstants.RELATIONSHIP_MANAGED_ROOT + "].from.from[" + EnterpriseChangeConstants.RELATIONSHIP_SERIES_MASTER + "].to.id");
														}
														//Still it is null then it's MP object
														if(applicableItemParentId == null || "null".equalsIgnoreCase(applicableItemParentId) || "".equalsIgnoreCase(applicableItemParentId)){
															applicableItemParentId = applicableItem.get("to[" + EnterpriseChangeConstants.RELATIONSHIP_MANAGED_SERIES + "].from.from[" + EnterpriseChangeConstants.RELATIONSHIP_SERIES_MASTER + "].to.id");
														}
														if(applicableItemParentId == null || "null".equalsIgnoreCase(applicableItemParentId) || "".equalsIgnoreCase(applicableItemParentId)){
															applicableItemParentId = applicableItem.get("to[" + EnterpriseChangeConstants.RELATIONSHIP_MAIN_DERIVED + "].from.id");
														}
														if(applicableItemParentId == null || "null".equalsIgnoreCase(applicableItemParentId) || "".equalsIgnoreCase(applicableItemParentId)){
															applicableItemParentId = applicableItem.get("to[" + EnterpriseChangeConstants.RELATIONSHIP_DERIVED + "].from.id");
														}






														Map appItem = new HashMap();
														//appItem.put("objId", applicableItemId);
														appItem.put(DomainObject.SELECT_ID, applicableItemId);
														appItem.put(DomainObject.SELECT_TYPE, applicableItemType);
														appItem.put(DomainObject.SELECT_NAME, applicableItemName);
														if (applicableItemType!=null && (mxType.isOfParentType(context, applicableItemType, EnterpriseChangeConstants.TYPE_PRODUCTS) || mxType.isOfParentType(context, applicableItemType, EnterpriseChangeConstants.TYPE_MANUFACTURING_PLAN))) {
															//appItem.put("seqSel", applicableItemRevision);
															appItem.put(DomainObject.SELECT_REVISION, applicableItemRevision);
														} else if(applicableItemType!=null && mxType.isOfParentType(context, applicableItemType, EnterpriseChangeConstants.TYPE_BUILDS)) {
															//appItem.put("seqSel", applicableItemBuildUnitNumber);
															appItem.put(DomainObject.SELECT_REVISION, applicableItemBuildUnitNumber);
														}

														if (applicableItemUpwardCompatibility.equalsIgnoreCase(EnterpriseChangeConstants.RANGE_YES)) {
															appItem.put("upwardCompatible", "true");
														} else if (applicableItemUpwardCompatibility.equalsIgnoreCase(EnterpriseChangeConstants.RANGE_NO)) {
															appItem.put("upwardCompatible", "false");
														}
														appItem.put("parentId", applicableItemParentId);
														appItemsList.add(appItem);
													}
												}//End of while applicableItemsItr
												if (appItemsList!=null && !appItemsList.isEmpty()) {
													String strEffectivityType = "";
													if (effectivityTypes!=null && !effectivityTypes.isEmpty()) {
														StringList effectivityCommandsList = FrameworkUtil.split(effectivityTypes, ",");
														Iterator<String> effectivityCommandsListItr = effectivityCommandsList.iterator();
														while (effectivityCommandsListItr.hasNext()) {
															String effectivityCommand = effectivityCommandsListItr.next();
															if (effectivityCommand!=null && !effectivityCommand.isEmpty()) {
																if (effectivityCommand.contains(typeKey.replace(" ", ""))) {
																	strEffectivityType = effectivityCommand.substring("CFFEffectivity".length());
																}
															}
														}//End of while
													}

													if (strEffectivityType!=null && !strEffectivityType.isEmpty()) {
														if (EnterpriseChangeUtil.isCFFInstalled(context)) {
														HashMap requestMap = new HashMap();
															requestMap.put("contextModelId", objectId);
															requestMap.put("applicableItemsList", appItemsList);
															String displayExpression = (String) JPO.invoke(context, "emxEffectivityFramework", null, "getDisplayExpression", JPO.packArgs(requestMap), String.class);
															displayExpression = displayExpression.replace("<", "\u003C");
															strBuffer.append(displayExpression);
														}
													}
												}
											}
										}
									}//End of while typeKeysItr
								}
							}
						}
					}
				}
				returnVector.addElement(strBuffer.toString());
			}
			return returnVector;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Update the Decision Applicability Discipline Summary
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * 	- objectList MapList: corresponds to the Model Context
	 * 	- columnMap Map: containing the Column Name
	 * @return void
	 * @throws Exception if error during processing
	 * @since EnterpriseChange R212_HFDerivations
	 */
	public void updateDecisionApplicabilityDisciplineSummary(Context context, String[] args) throws Exception {
		try {
			Map programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			HashMap columnMap = (HashMap) programMap.get("columnMap");

			String decisionId = (String) requestMap.get("parentOID");

			String changeDiscipline = (String) columnMap.get("name");

			String applicabilityContextId = (String) paramMap.get("objectId");
			String newValue = (String) paramMap.get("New Value");
			//newValue = newValue.replaceAll("&lt;","<");
			//newValue = newValue.replaceAll("&gt;",">");
			//newValue = newValue.replaceAll("&quot;","\"");
			//newValue = newValue.replaceAll("&nbsp;"," ");

			StringList changeDisciplines = EnterpriseChangeUtil.getChangeDisciplines(context);

			if ((decisionId!=null && !decisionId.isEmpty()) && (changeDiscipline!=null && !changeDiscipline.isEmpty()) && (applicabilityContextId!=null && !applicabilityContextId.isEmpty())) {
				Decision decision = new Decision(decisionId);
				Map<String,Map<String,Map<String,MapList>>> applicableItemsSortedByMastersAndDisciplinesAndTypes = decision.getApplicabilitySummary(context, new StringList(applicabilityContextId), new StringList(changeDiscipline), null);
				if (applicableItemsSortedByMastersAndDisciplinesAndTypes!=null && !applicableItemsSortedByMastersAndDisciplinesAndTypes.isEmpty()) {
					//Loop on the Applicable Items to disconnect
					Map<String,MapList> applicableItemsSortedByTypes = (applicableItemsSortedByMastersAndDisciplinesAndTypes.get(applicabilityContextId)).get(changeDiscipline);
					if (applicableItemsSortedByTypes!=null && !applicableItemsSortedByTypes.isEmpty()) {
						Set<String> typeKeys = applicableItemsSortedByTypes.keySet();
						Iterator<String> typeKeysItr = typeKeys.iterator();
						while (typeKeysItr.hasNext()) {
							String typeKey = typeKeysItr.next();
							if (typeKey!=null && !typeKey.isEmpty()) {
								MapList applicableItems = applicableItemsSortedByTypes.get(typeKey);
								if (applicableItems!=null && !applicableItems.isEmpty()) {
									Iterator<Map<String,Object>> applicableItemsItr = applicableItems.iterator();
									while (applicableItemsItr.hasNext()) {
										Map<String,Object> applicableItem = applicableItemsItr.next();
										if (applicableItem!=null && !applicableItem.isEmpty()) {
											String applicableItemRelId = (String) applicableItem.get(DomainConstants.SELECT_RELATIONSHIP_ID);
											if (applicableItemRelId!=null && !applicableItemRelId.isEmpty()) {
												int nbChangeDisciplines = 0;
												//Check if the Applicable Item is not Applicable for several Discipline
												Iterator<String> changeDisciplinesItr = changeDisciplines.iterator();
												while (changeDisciplinesItr.hasNext()) {
													if (((String) applicableItem.get("attribute[" + changeDisciplinesItr.next() + "]")).equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE)) {
														nbChangeDisciplines++;
													}
												}
												//If yes, only set the Discipline to no
												if (nbChangeDisciplines>1) {
													new DomainRelationship(applicableItemRelId).setAttributeValue(context, changeDiscipline, EnterpriseChangeConstants.CHANGE_DISCIPLINE_FALSE);
												}
												//Else disconnect the Applicable Item
												else {
													DomainRelationship.disconnect(context, applicableItemRelId, false);
												}
											}
										}
									}
								}
							}
						}//End of while typeKeys
					}
				}
				if (newValue!=null && !newValue.isEmpty()) {
					String xmlApplicabilityExpression = FrameworkUtil.decodeURL(newValue, "UTF-8");
					if (xmlApplicabilityExpression!=null && !xmlApplicabilityExpression.isEmpty()) {
						SAXBuilder builder = new SAXBuilder();
						builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
						builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
						builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
						Document document = builder.build(new StringReader(xmlApplicabilityExpression));
						Element root = document.getRootElement();
						Namespace xmlns = root.getNamespace();
						//Based on the XML, connect the new Applicable Item to the Decision

						List<Element> serieElements = new ArrayList<Element>();
						//Series elements are for Builds
						serieElements.addAll(EnterpriseChangeUtil.getElementByName(context, root, "Series"));
						//TreeSerie elements are for Products and Manufacturing Plans
						serieElements.addAll(EnterpriseChangeUtil.getElementByName(context, root, "TreeSeries"));
						Iterator<Element> serieElementsItr = serieElements.iterator();
						while (serieElementsItr.hasNext()) {
							Element serieElement = serieElementsItr.next();
							if (serieElement!=null) {
								String serieElementTagName = serieElement.getName();
								String serieElementName = serieElement.getAttributeValue("Name");
								if (serieElementTagName!=null && !serieElementTagName.isEmpty()) {
									if (serieElementTagName.equalsIgnoreCase("TreeSeries")) {
										String serieElementType = serieElement.getAttributeValue("Type");
										if (serieElementType!=null && !serieElementType.isEmpty()) {
											String searchType = "";
											if (serieElementType.equalsIgnoreCase("ProductState")) {
												searchType = EnterpriseChangeConstants.TYPE_PRODUCTS;
											} else if (serieElementType.equalsIgnoreCase("ManufPlan")) {
												searchType = EnterpriseChangeConstants.TYPE_MANUFACTURING_PLAN;
											}
											List<Element> serieElementChildren = serieElement.getChildren();
											Iterator<Element> serieElementChildrenItr = serieElementChildren.iterator();
											while (serieElementChildrenItr.hasNext()) {
												Element serieElementChild = serieElementChildrenItr.next();
												if (serieElementChild!=null) {
													String serieElementChildName = serieElementChild.getName();
													if (serieElementChildName!=null && !serieElementChildName.isEmpty()) {
														if (serieElementChildName.equalsIgnoreCase("Single")) {



															String treeRootName = serieElementChild.getAttributeValue("Name");
															String treeRootRevision = serieElementChild.getAttributeValue("Revision");
															if ((treeRootName!=null && !treeRootName.isEmpty()) && (treeRootRevision!=null && !treeRootRevision.isEmpty())) {
																MapList existingObjects = DomainObject.findObjects(context,
																		searchType,
																		treeRootName,
																		treeRootRevision,
																		DomainConstants.QUERY_WILDCARD,
																		DomainConstants.QUERY_WILDCARD,
																		DomainConstants.EMPTY_STRING,
																		true,
																		new StringList(DomainConstants.SELECT_ID));


																if(existingObjects!=null && !existingObjects.isEmpty() && existingObjects.size()==1){
																	Map<String,String> existingObject = (Map<String, String>) existingObjects.get(0);
																	if(existingObject!=null && !existingObject.isEmpty()){
																		String applicableItemId = existingObject.get(DomainConstants.SELECT_ID);
																		if (applicableItemId!=null && !applicableItemId.isEmpty()) {
																			//Check if the Relationship doesn't already exists
																			//String relId = MqlUtil.mqlCommand(context, "print bus " + decisionId + " select from[" + EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM + "|to.id==" + applicableItemId + "].id dump");
																			String relId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",decisionId,"from[" + EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM + "|to.id==" + applicableItemId + "].id");
																			if (relId!=null && !relId.isEmpty()) {
																				new DomainRelationship(relId).setAttributeValue(context, changeDiscipline, EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE);
																			} else {
																				DomainRelationship applicableItemRel = DomainRelationship.connect(context, decision, EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM, new DomainObject(applicableItemId));
																				String commandMQL = "";
																				//Check if Upward Compatibility Interface is already on the connection
																				//commandMQL = "print connection " + applicableItemRel + " select interface[" + EnterpriseChangeConstants.INTERFACE_UPWARD_COMPATIBILITY + "] dump";
																				commandMQL = "print connection $1 select $2 dump";
																				//If no interface --> add one
																				  if((MqlUtil.mqlCommand(context,commandMQL,applicableItemRel.toString(),"interface["+ EnterpriseChangeConstants.INTERFACE_UPWARD_COMPATIBILITY + "]")).equalsIgnoreCase("false")){
																				//if ((MqlUtil.mqlCommand(context, commandMQL,applicableItemRel.toString(),"interface[" + EnterpriseChangeConstants.INTERFACE_UPWARD_COMPATIBILITY + "]")).equalsIgnoreCase("false")) {
																					//String strAddInterface = "modify connection " + applicableItemRel + " add interface \'"+ EnterpriseChangeConstants.INTERFACE_UPWARD_COMPATIBILITY + "\'";
																					String strAddInterface = "modify connection $1 add interface $2";
																					String strAddInterfaceMessage = MqlUtil.mqlCommand(context,strAddInterface,applicableItemRel.toString(),EnterpriseChangeConstants.INTERFACE_UPWARD_COMPATIBILITY);
																				}

																				//Set Upward Compatibility Value
																				applicableItemRel.setAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_UPWARD_COMPATIBILITY, EnterpriseChangeConstants.RANGE_YES);

																				//Check if Change Discipline interface is already on the connection
																				//commandMQL = "print connection " + applicableItemRel + " select interface[" + EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE + "] dump";
																				commandMQL = "print connection $1 select $2 dump";
																				//If no interface --> add one
																				if ((MqlUtil.mqlCommand(context,commandMQL,applicableItemRel.toString(),"interface[" + EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE + "] ")).equalsIgnoreCase("false")) {
																					//String strAddInterface = "modify connection " + applicableItemRel + " add interface \'" + EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE + "\'";
																					String strAddInterface = "modify connection $1 add interface $2";
																					String strAddInterfaceMessage = MqlUtil.mqlCommand(context,strAddInterface,applicableItemRel.toString(), EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE);
																				}

																				Iterator<String> changeDisciplinesItr = changeDisciplines.iterator();
																				while (changeDisciplinesItr.hasNext()) {
																					String changeDisciplineTemp = changeDisciplinesItr.next();
																					if (changeDisciplineTemp!=null && !changeDisciplineTemp.isEmpty()) {
																						String changeDisciplineValue = "";
																						if (changeDisciplineTemp.equalsIgnoreCase(changeDiscipline)) {
																							changeDisciplineValue = EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE;
																						} else {
																							changeDisciplineValue = EnterpriseChangeConstants.CHANGE_DISCIPLINE_FALSE;
																						}
																						applicableItemRel.setAttributeValue(context, changeDisciplineTemp, changeDisciplineValue);
																					}
																				}
																			}
																		}
																	}
																}
															}


														} else if (serieElementChildName.equalsIgnoreCase("Tree")) {
															Element treeRoot = serieElementChild.getChild("Root", xmlns);
															Element treeLeaf = serieElementChild.getChild("Leaf", xmlns);
															if (treeRoot!=null) {
																String treeRootName = treeRoot.getAttributeValue("Name");
																String treeRootRevision = treeRoot.getAttributeValue("Revision");
																if ((treeRootName!=null && !treeRootName.isEmpty()) && (treeRootRevision!=null && !treeRootRevision.isEmpty())) {
																	MapList existingObjects = DomainObject.findObjects(context,
																			searchType,
																			treeRootName,
																			treeRootRevision,
																			DomainConstants.QUERY_WILDCARD,
																			DomainConstants.QUERY_WILDCARD,
																			DomainConstants.EMPTY_STRING,
																			true,
																			new StringList(DomainConstants.SELECT_ID));


																	if(existingObjects!=null && !existingObjects.isEmpty() && existingObjects.size()==1){
																		Map<String,String> existingObject = (Map<String, String>) existingObjects.get(0);
																		if(existingObject!=null && !existingObject.isEmpty()){
																			String applicableItemId = existingObject.get(DomainConstants.SELECT_ID);
																			if (applicableItemId!=null && !applicableItemId.isEmpty()) {
																				//Check if the Relationship doesn't already exists
																				String relId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",decisionId,"from[" + EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM + "|to.id==" + applicableItemId + "].id");
																				if (relId!=null && !relId.isEmpty()) {
																					new DomainRelationship(relId).setAttributeValue(context, changeDiscipline, EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE);
																				} else {
																					DomainRelationship applicableItemRel = DomainRelationship.connect(context, decision, EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM, new DomainObject(applicableItemId));
																					String commandMQL = "";
																					//Check if Upward Compatibility Interface is already on the connection
																					//commandMQL = "print connection " + applicableItemRel + " select interface[" + EnterpriseChangeConstants.INTERFACE_UPWARD_COMPATIBILITY + "] dump";
																					commandMQL = "print connection $1 select $2 dump";
																					//If no interface --> add one
																					if ((MqlUtil.mqlCommand(context, commandMQL,applicableItemRel.toString(),"interface[" + EnterpriseChangeConstants.INTERFACE_UPWARD_COMPATIBILITY + "]")).equalsIgnoreCase("false")) {
																						//String strAddInterface = "modify connection " + applicableItemRel + " add interface \'"+ EnterpriseChangeConstants.INTERFACE_UPWARD_COMPATIBILITY + "\'";
																						String strAddInterface = "modify connection $1 add interface $2";
																						String strAddInterfaceMessage = MqlUtil.mqlCommand(context,strAddInterface,applicableItemRel.toString(),EnterpriseChangeConstants.INTERFACE_UPWARD_COMPATIBILITY);
																					}

																					//Set Upward Compatibility Value
																					applicableItemRel.setAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_UPWARD_COMPATIBILITY, EnterpriseChangeConstants.RANGE_YES);

																					//Check if Change Discipline interface is already on the connection
																					//commandMQL = "print connection " + applicableItemRel + " select interface[" + EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE + "] dump";
																					commandMQL = "print connection $1 select $2 dump";
																					//If no interface --> add one
																					if ((MqlUtil.mqlCommand(context,commandMQL,applicableItemRel.toString(),"interface[" + EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE + "]")).equalsIgnoreCase("false")) {
																						//String strAddInterface = "modify connection " + applicableItemRel + " add interface \'" + EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE + "\'";
																						String strAddInterface = "modify connection $1 add interface $2";
																						String strAddInterfaceMessage = MqlUtil.mqlCommand(context,strAddInterface,applicableItemRel.toString(),EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE);
																					}

																					Iterator<String> changeDisciplinesItr = changeDisciplines.iterator();
																					while (changeDisciplinesItr.hasNext()) {
																						String changeDisciplineTemp = changeDisciplinesItr.next();
																						if (changeDisciplineTemp!=null && !changeDisciplineTemp.isEmpty()) {
																							String changeDisciplineValue = "";
																							if (changeDisciplineTemp.equalsIgnoreCase(changeDiscipline)) {
																								changeDisciplineValue = EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE;
																							} else {
																								changeDisciplineValue = EnterpriseChangeConstants.CHANGE_DISCIPLINE_FALSE;
																							}
																							applicableItemRel.setAttributeValue(context, changeDisciplineTemp, changeDisciplineValue);
																						}
																					}
																				}
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}//End of while serieElementChildren
										}
									} else if (serieElementTagName.equalsIgnoreCase("Series")) {
										List<Element> serieElementChildren = serieElement.getChildren();
										Iterator<Element> serieElementChildrenItr = serieElementChildren.iterator();
										while (serieElementChildrenItr.hasNext()) {
											Element serieElementChild = serieElementChildrenItr.next();
											if (serieElementChild!=null) {
												String serieElementChildName = serieElementChild.getName();
												if (serieElementChildName!=null && !serieElementChildName.isEmpty()) {
													if (serieElementChildName.equalsIgnoreCase("SingleValue")) {

													} else if (serieElementChildName.equalsIgnoreCase("Interval")) {
														Element intervalFrom = serieElementChild.getChild("From", xmlns);
														Element intervalTo = serieElementChild.getChild("To", xmlns);
														if (intervalFrom!=null) {
															String intervalFromValue = intervalFrom.getAttributeValue("Value");
															if (intervalFromValue!=null && !intervalFromValue.isEmpty()) {
																//String MQLCommand = "print bus \"" + EnterpriseChangeConstants.TYPE_MODEL + "\" \"" + serieElementName + "\" \"" + "\" " + "select from[" + EnterpriseChangeConstants.RELATIONSHIP_MODEL_BUILD + "|to.attribute[" + EnterpriseChangeConstants.ATTRIBUTE_BUILD_UNIT_NUMBER + "]==" + intervalFromValue + "].to.id dump";
																String MQLCommand = "print bus $1 $2 $3 select $4 dump";
																String MQLResult = MqlUtil.mqlCommand(context, MQLCommand,EnterpriseChangeConstants.TYPE_MODEL,serieElementName,"","from[" + EnterpriseChangeConstants.RELATIONSHIP_MODEL_BUILD + "|to.attribute[" + EnterpriseChangeConstants.ATTRIBUTE_BUILD_UNIT_NUMBER + "]==" + intervalFromValue + "].to.id");
																if (MQLResult!=null && !MQLResult.isEmpty()) {
																	//Check if the Relationship doesn't already exists
																	String relId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",decisionId,"from[" + EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM + "|to.id==" + MQLResult + "].id");
																	if (relId!=null && !relId.isEmpty()) {
																		new DomainRelationship(relId).setAttributeValue(context, changeDiscipline, EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE);
																	} else {
																		DomainRelationship applicableItemRel = DomainRelationship.connect(context, decision, EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM, new DomainObject(MQLResult));
																		String commandMQL = "";
																		//Check if Upward Compatibility Interface is already on the connection
																		//commandMQL = "print connection " + applicableItemRel + " select interface[" + EnterpriseChangeConstants.INTERFACE_UPWARD_COMPATIBILITY + "] dump";
																		commandMQL = "print connection $1 select $2 dump";
																		//If no interface --> add one
																		if ((MqlUtil.mqlCommand(context, commandMQL,applicableItemRel.toString(),"interface[" + EnterpriseChangeConstants.INTERFACE_UPWARD_COMPATIBILITY + "]")).equalsIgnoreCase("false")) {
																			//String strAddInterface = "modify connection " + applicableItemRel + " add interface \'"+ EnterpriseChangeConstants.INTERFACE_UPWARD_COMPATIBILITY + "\'";
																			String strAddInterface = "modify connection $1 add interface $2";
																			String strAddInterfaceMessage = MqlUtil.mqlCommand(context,strAddInterface,applicableItemRel.toString(),EnterpriseChangeConstants.INTERFACE_UPWARD_COMPATIBILITY);
																		}

																		//Set Upward Compatibility Value
																		applicableItemRel.setAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_UPWARD_COMPATIBILITY, EnterpriseChangeConstants.RANGE_YES);

																		//Check if Change Discipline interface is already on the connection
																		//commandMQL = "print connection " + applicableItemRel + " select interface[" + EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE + "] dump";
																		commandMQL = "print connection $1 select $2 dump";
																		//If no interface --> add one
																		if ((MqlUtil.mqlCommand(context,commandMQL,applicableItemRel.toString(),"interface[" + EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE + "]")).equalsIgnoreCase("false")) {
																			//String strAddInterface = "modify connection " + applicableItemRel + " add interface \'" + EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE + "\'";
																			String strAddInterface = "modify connection $1 add interface $2";
																			String strAddInterfaceMessage = MqlUtil.mqlCommand(context,strAddInterface,applicableItemRel.toString(),EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE);
																		}

																		Iterator<String> changeDisciplinesItr = changeDisciplines.iterator();
																		while (changeDisciplinesItr.hasNext()) {
																			String changeDisciplineTemp = changeDisciplinesItr.next();
																			if (changeDisciplineTemp!=null && !changeDisciplineTemp.isEmpty()) {
																				String changeDisciplineValue = "";
																				if (changeDisciplineTemp.equalsIgnoreCase(changeDiscipline)) {
																					changeDisciplineValue = EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE;
																				} else {
																					changeDisciplineValue = EnterpriseChangeConstants.CHANGE_DISCIPLINE_FALSE;
																				}
																				applicableItemRel.setAttributeValue(context, changeDisciplineTemp, changeDisciplineValue);
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}//End of while serieElementChildren
									}
								}
							}
						}//End of while TreeSeries
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Exclude the Manufacturing Intents already connected
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * 	- id[connection] String: corresponds to the Rel Id
	 * @return StringList: containing the Id of the objects already connected
	 * @throws Exception if error during processing
	 * @since EnterpriseChange R212_HFDerivations
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeDecisionManufacturingIntents(Context context, String[] args)throws Exception {
		try {
			StringList returnStringList = new StringList();

			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			programMap.put("relId", (String) programMap.get("id[connection]"));

			MapList manufacturingIntents = this.getManufacturingApplicableItemsManufacturingIntent(context, JPO.packArgs(programMap));
			Iterator<Map<String,String>> manufacturingIntentsItr = manufacturingIntents.iterator();
			while (manufacturingIntentsItr.hasNext()) {
				Map<String,String> manufacturingIntent = manufacturingIntentsItr.next();
				if (manufacturingIntent!=null && !manufacturingIntent.isEmpty()) {
					String manufacturingIntentId = manufacturingIntent.get(DomainConstants.SELECT_ID);
					if (manufacturingIntentId!=null && !manufacturingIntentId.isEmpty()) {
						returnStringList.addElement(manufacturingIntentId);
					}
				}
			}
			return returnStringList;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Method to define if the Upward Compatibility column should be displayed in the Decision Manufacturing Applicable Items table
	 *
	 * @param context the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return Boolean - true if field should be displayed and false if not
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212_HFDerivations
	 */
	public boolean displayDecisionManufacturingApplicableItemsUpwardCompatibility(Context context, String[] args) throws Exception {
		try {
			//return !EnterpriseChangeUtil.isDerivationEnabled(context);
			return false;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
  	 * Check For Manufacturing Intent and Mobile Mode Enable. If both are true then it will return true.
  	 *
  	 * @param context 
  	 * 			the eMatrix <code>Context</code> object
  	 * @param args 
  	 * 			string array containing packed arguments.
  	 * @return boolean
  	 * @throws FrameworkException 
  	 * 			If the operation fails
  	 */
  	 public boolean showDecisionManufacturingIntentsMobileModeEnabled (Context context, String args[]) throws FrameworkException {
  		 try {
			return showDecisionManufacturingIntents(context,args) && UINavigatorUtil.isMobile(context);
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		} 
  	 }

  	/**
  	 * Check For Manufacturing Intent and Mobile Mode Disable. If both are true then it will return true.
  	 *
  	 * @param context 
  	 * 			the eMatrix <code>Context</code> object
  	 * @param args 
  	 * 			string array containing packed arguments.
  	 * @return boolean
  	 * @throws FrameworkException 
  	 * 			If the operation fails
  	 */
  	 public boolean showDecisionManufacturingIntentsMobileModeDisabled (Context context, String args[]) throws FrameworkException {
  		try {
			return showDecisionManufacturingIntents(context,args) && !UINavigatorUtil.isMobile(context);
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		} 
  	 }

}

