/*
 ** emxEnterpriseChangeBase
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 */



import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import matrix.db.AttributeType;
import matrix.db.AttributeTypeList;
import matrix.db.BusinessInterface;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.enterprisechange.EnterpriseChangeConstants;
import com.matrixone.apps.enterprisechange.EnterpriseChangeUtil;
import com.matrixone.apps.program.Task;


/**
 * This JPO class has some methods pertaining to Product Line type.
 * @author Enovia MatrixOne
 * @version EnterpriseChange X+5
 */
public class emxEnterpriseChangeBase_mxJPO extends emxDomainObject_mxJPO {

	/**
	 * Create a new emxProductLine object from a given id.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments.
	 * @return a emxProductLine object
	 * @throws Exception if operation fails
	 * @since ProductCentral 10.0.0.0
	 * @grade 0
	 */
	public emxEnterpriseChangeBase_mxJPO (Context context, String[] args) throws Exception {
		super(context, args);
	}

	/**
	 * Main entry point.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return an integer status code (0 = success)
	 * @throws Exception if operation fails
	 * @since EnterpriseChange X+5
	 * @grade 0
	 */
	public int mxMain (Context context, String[] args) throws Exception {
		if (!context.isConnected()) {
			// i18nNow i18nnow = new i18nNow();
			// String language = context.getSession().getLanguage();
			// String strContentLabel = i18nnow.GetString("emxProductLineStringResource", language, "emxProduct.Alert.FeaturesCheckFailed");
			String strContentLabel="";
			throw  new Exception(strContentLabel);
		}
		return  0;
	}
	
	/**
	 * Method to define if the Applicability Management has been enabled
	 *
	 * @param context the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return Boolean - true if Applicability Management is enabled and false if not
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212.HFDerivations
	 */
	public boolean isApplicabilityManagementEnabled(Context context, String[] args) throws Exception {
		try {
			return EnterpriseChangeUtil.isApplicabilityManagementEnabled(context);
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * Method to define if the Derivation has been enabled for Products and Manufacturing Plans
	 *
	 * @param context the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return Boolean - true if Derivation is enabled and false if not
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212.HFDerivations
	 */
//	public boolean isDerivationEnabled(Context context, String[] args) throws Exception {
//		try {
//			return EnterpriseChangeUtil.isDerivationEnabled(context);
//		} catch (Exception e) {
//			throw e;
//		}
//	}

	/**
	 * Connects Product Line with Program
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId, Old Value for product name and new value
	 * @return int - returns zero if connect successful
	 * @throws Exception if the operation fails
	 * @since ProductLine X+5
	 * @deprecated : since R418 ${CLASS:emxProductLineBase}.updateProgramOnProductLine(Context context, String[] args)
	 */

	public int updateProgramOnProductLine(Context context, String[] args) throws Exception {
		try {
			int returnInt = 0;
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");
			String productLineId = (String)paramMap.get("objectId");
			String newProgramId = (String)paramMap.get("New OID");

			if (productLineId!=null && !productLineId.isEmpty()) {
				DomainObject productLineDom = new DomainObject(productLineId);

				//Get the Program Change Projects List
				StringList relatedChangeProjectsList = new StringList();
				String programId = productLineDom.getInfo(context,"from["+ EnterpriseChangeConstants.RELATIONSHIP_PRODUCT_LINE_PROGRAM +"].to.id");
				if (programId!=null && !programId.isEmpty()) {
					DomainObject programDom = new DomainObject(programId);
					MapList relatedChangeProjects = programDom.getRelatedObjects(context,
							EnterpriseChangeConstants.RELATIONSHIP_PROGRAM_PROJECT,
							EnterpriseChangeConstants.TYPE_CHANGE_PROJECT,
							new StringList(DomainConstants.SELECT_ID),
							new StringList(DomainConstants.SELECT_RELATIONSHIP_ID),
							false, //to rel
							true, //from rel
							(short)1, //recurse
							null, //objectWhere
							null, //relWhere
							0);

					//Convert the MapList into a StringList
					Iterator<Map<String,String>> relatedChangeProjectsItr = relatedChangeProjects.iterator();
					while (relatedChangeProjectsItr.hasNext()) {
						Map<String,String> relatedChangeProject = relatedChangeProjectsItr.next();
						if (relatedChangeProject!=null && !relatedChangeProject.isEmpty()) {
							String relatedChangeProjectId = relatedChangeProject.get(DomainConstants.SELECT_ID);
							if (relatedChangeProjectId!=null && !relatedChangeProjectId.isEmpty()) {
								relatedChangeProjectsList.addElement(relatedChangeProjectId);
							}
						}
					}//End of while
				}

				//Check if the Product Line has Models already connected to Change Projects
				Boolean hasModelAlreadyConnected = false;
				StringBuffer warningMessage = new StringBuffer();
				warningMessage.append(EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.ProductLine.CannotDisconnectProgram", context.getSession().getLanguage()));

				StringList objectsSelect = new StringList();
				objectsSelect.addElement(DomainConstants.SELECT_ID);
				objectsSelect.addElement("from[" + EnterpriseChangeConstants.RELATIONSHIP_RELATED_PROJECTS + "|to.type == '" + EnterpriseChangeConstants.TYPE_CHANGE_PROJECT + "'].to.id");

				MapList relatedModels = productLineDom.getRelatedObjects(context,
						EnterpriseChangeConstants.RELATIONSHIP_PRODUCT_LINE_MODELS,
						EnterpriseChangeConstants.TYPE_MODEL,
						new StringList(DomainConstants.SELECT_ID),
						new StringList(DomainConstants.SELECT_RELATIONSHIP_ID),
						false, //to rel
						true, //from rel
						(short)1, //recurse
						null, //objectWhere
						null, //relWhere
						0);

				Iterator<Map<String,String>> relatedModelsItr = relatedModels.iterator();
				while (relatedModelsItr.hasNext()) {
					StringBuffer modelDetails = new StringBuffer();
					Map<String,String> relatedModel = relatedModelsItr.next();
					if (relatedModel!=null && !relatedModel.isEmpty()) {
						String relatedModelId = relatedModel.get(DomainConstants.SELECT_ID);
						if (relatedModelId!=null && !relatedModelId.isEmpty()) {
							DomainObject relatedModelDom = new DomainObject(relatedModelId);

							MapList relatedProjects = relatedModelDom.getRelatedObjects(context,
									EnterpriseChangeConstants.RELATIONSHIP_RELATED_PROJECTS,
									EnterpriseChangeConstants.TYPE_CHANGE_PROJECT,
									new StringList(DomainConstants.SELECT_ID),
									new StringList(DomainConstants.SELECT_RELATIONSHIP_ID),
									false, //to rel
									true, //from rel
									(short)1, //recurse
									null, //objectWhere
									null, //relWhere
									0);

							Iterator<Map<String,String>> relatedProjectsItr = relatedProjects.iterator();
							while (relatedProjectsItr.hasNext()) {
								Map<String,String> relatedProject = relatedProjectsItr.next();
								if (relatedProject!=null && !relatedProject.isEmpty()) {
									String relatedProjectId = relatedProject.get(DomainConstants.SELECT_ID);
									if (relatedProjectId!=null && !relatedProjectId.isEmpty()) {
										if (relatedChangeProjectsList.contains(relatedProjectId)) {
											hasModelAlreadyConnected = true;
											if (modelDetails!=null && !(modelDetails.toString()).isEmpty()) {modelDetails.append("\\n");}
											modelDetails.append("   - " + new DomainObject(relatedProjectId).getInfo(context, DomainConstants.SELECT_NAME));
										}
									}
								}
							}//End of while

							//Add the Model Name
							if (modelDetails!=null && !(modelDetails.toString()).isEmpty()) {
								modelDetails.insert(0, " - " + relatedModelDom.getInfo(context, DomainConstants.SELECT_NAME) + "\\n");
								if (warningMessage!=null && !(warningMessage.toString()).isEmpty()) {warningMessage.append("\\n");}
								warningMessage.append(modelDetails.toString());
							}
						}
					}
				}//End of while

				//If so don't allow the Program modification
				if (hasModelAlreadyConnected) {
					returnInt = 1;
					emxContextUtilBase_mxJPO.mqlNotice(context,warningMessage.toString());
				} else {
					//Else allow the Program modification
					String productLineProgramRelId = productLineDom.getInfo(context,"from["+ EnterpriseChangeConstants.RELATIONSHIP_PRODUCT_LINE_PROGRAM +"].id");

					if (productLineProgramRelId != null && !"".equals(productLineProgramRelId)) {
						//Disconnecting the existing relationship
						DomainRelationship.disconnect(context, productLineProgramRelId);
					}

					if (newProgramId!=null && !newProgramId.isEmpty()) {
						//Connect the new Program
						DomainRelationship.connect(context,productLineDom,EnterpriseChangeConstants.RELATIONSHIP_PRODUCT_LINE_PROGRAM, new DomainObject(newProgramId));
					}
				}
			}
			return returnInt;
		} catch (Exception e) {
			throw e;
		}
	}

	public MapList getChangeDisciplinesToDisplay(Context context,String[] args)throws Exception {
		try{
			MapList returnMapList = new MapList();
			HashMap programMap = (HashMap)JPO.unpackArgs(args);

			String objectId = (String)programMap.get("objectId");
			String strMode = (String) programMap.get("mode");
			StringList objectBackDisciplineList = (StringList) programMap.get("objectBackDisciplineList");
			String wizType = (String) programMap.get("wizType");

			StringList objectSelects = new StringList(2);
			objectSelects.addElement(DomainConstants.SELECT_ID);
			objectSelects.addElement(DomainConstants.SELECT_TYPE);

			StringList relationshipSelects = new StringList(1);
			relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

			MapList relatedUptasks = new MapList();
			MapList relatedSubtasks = new MapList();
			MapList relatedChangeDeliverables = new MapList();

			DomainObject domObj = new DomainObject();

			if (objectId!=null && !objectId.isEmpty()) {
				domObj.setId(objectId);

				//Get all upper tasks only if not Template or Clone, meaning for Edit
				if (!(wizType.equalsIgnoreCase("Template") || wizType.equalsIgnoreCase("Clone"))) {
					//Get all upper tasks (Change Project, Change Task)
					MapList tempRelatedUptasks = domObj.getRelatedObjects(context,
							DomainConstants.RELATIONSHIP_SUBTASK,
							DomainConstants.QUERY_WILDCARD,
							objectSelects,
							relationshipSelects,
							true,	//to relationship
							false,	//from relationship
							(short)0,
							DomainConstants.EMPTY_STRING,
							DomainConstants.EMPTY_STRING,
							0);

					if (tempRelatedUptasks!=null && !tempRelatedUptasks.isEmpty()) {
						//Sort MapList
						tempRelatedUptasks.sort(DomainConstants.SELECT_LEVEL, "ascending", "string");
						//Find the first upper Change Project or Change Task
						Boolean flag = false;
						Iterator tempRelatedUptasksItr = tempRelatedUptasks.iterator();
						while (tempRelatedUptasksItr.hasNext() && !flag) {
							Map tempRelatedUptask = (Map)tempRelatedUptasksItr.next();
							if (tempRelatedUptask!=null && !tempRelatedUptask.isEmpty()) {
								String tempRelatedUptaskId = (String)tempRelatedUptask.get(DomainConstants.SELECT_ID);
								String tempRelatedUptaskType = (String)tempRelatedUptask.get(DomainConstants.SELECT_TYPE);
								if (tempRelatedUptaskId!=null && !tempRelatedUptaskId.equalsIgnoreCase("")) {
									DomainObject tempRelatedUptaskDom = new DomainObject(tempRelatedUptaskId);
									if (tempRelatedUptaskDom.isKindOf(context,DomainConstants.TYPE_CHANGE_PROJECT) || tempRelatedUptaskDom.isKindOf(context,DomainConstants.TYPE_CHANGE_TASK)) {
										flag = true;
										relatedUptasks.add(tempRelatedUptask);
									}
								}
							}
						}
					}
				}

				//Get all sub tasks (Change Project, Change Task)
				MapList tempRelatedSubtasks = domObj.getRelatedObjects(context,
						DomainConstants.RELATIONSHIP_SUBTASK,
						DomainConstants.QUERY_WILDCARD,
						objectSelects,
						relationshipSelects,
						false,	//to relationship
						true,	//from relationship
						(short)0,
						DomainConstants.EMPTY_STRING,
						DomainConstants.EMPTY_STRING,
						0);

				if (tempRelatedSubtasks!=null && !tempRelatedSubtasks.isEmpty()) {
					//Sort MapList
					tempRelatedSubtasks.sort(DomainConstants.SELECT_LEVEL, "ascending", "string");
					//Find only Change Project or Change Task
					Iterator tempRelatedSubtasksItr = tempRelatedSubtasks.iterator();
					while (tempRelatedSubtasksItr.hasNext()) {
						Map tempRelatedSubtask = (Map)tempRelatedSubtasksItr.next();
						if (tempRelatedSubtask!=null && !tempRelatedSubtask.isEmpty()) {
							String tempRelatedSubtaskId = (String)tempRelatedSubtask.get(DomainConstants.SELECT_ID);
							String tempRelatedSubtaskType = (String)tempRelatedSubtask.get(DomainConstants.SELECT_TYPE);
							if (tempRelatedSubtaskId!=null && !tempRelatedSubtaskId.equalsIgnoreCase("")) {
								DomainObject tempRelatedSubtaskDom = new DomainObject(tempRelatedSubtaskId);
								if (tempRelatedSubtaskDom.isKindOf(context,DomainConstants.TYPE_CHANGE_PROJECT) || tempRelatedSubtaskDom.isKindOf(context,DomainConstants.TYPE_CHANGE_TASK)) {
									relatedSubtasks.add(tempRelatedSubtask);
								}
							}
						}
					}
				}

				//If Change Task --> Get all Change Deliverable (Projects, Tasks)
				if (domObj.getInfo(context, DomainConstants.SELECT_TYPE).equalsIgnoreCase(EnterpriseChangeConstants.TYPE_CHANGE_TASK)) {
				    String TYPE_ECA = PropertyUtil.getSchemaProperty(context,"type_VPLMtyp@PLMActionBase");
					if(TYPE_ECA==null || TYPE_ECA.equals(""))
						TYPE_ECA = PropertyUtil.getSchemaProperty(context,"type_PLMActionBase");
						
					relatedChangeDeliverables = domObj.getRelatedObjects(context,
							DomainConstants.RELATIONSHIP_TASK_DELIVERABLE,
							EnterpriseChangeConstants.TYPE_CHANGE + "," + TYPE_ECA,
							objectSelects,
							relationshipSelects,
							false,	//to relationship
							true,	//from relationship
							(short)1,
							DomainConstants.EMPTY_STRING,
							DomainConstants.EMPTY_STRING,
							0);
				}
			}

			MapList listToDisplayAttributes = new MapList();

			String strInterfaceName = EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE;
			BusinessInterface busInterface = new BusinessInterface(strInterfaceName, context.getVault());
			AttributeTypeList listInterfaceAttributes = busInterface.getAttributeTypes(context);

			//Check how many discipline has the Change Project/Task
			//If only one the checkbox will have to be greyed out
			StringList objectDisciplineList = new StringList();
			if (objectId!=null && !objectId.isEmpty()) {
				for (int i=0;i<listInterfaceAttributes.size();i++) {
					String attrName = ((AttributeType) listInterfaceAttributes.get(i)).getName();
					String attrNameValue = domObj.getAttributeValue(context, attrName);
					if (attrNameValue!=null && attrNameValue.equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE)) {
						objectDisciplineList.add(attrName);
					}
				}
			}

			Iterator listInterfaceAttributesItr = listInterfaceAttributes.iterator();
			Boolean isPreviousDisplayed = false;

			while (listInterfaceAttributesItr.hasNext()) {
				Map attributeToDisplay = new HashMap();
				String isChecked = "";
				String isDisabled = "";
				String inputType = "";
				Boolean isDisplayed = true;
				Boolean canBeEnabled = true;

				String attrName = ((AttributeType) listInterfaceAttributesItr.next()).getName();
				String attrNameI18N = i18nNow.getAttributeI18NString(attrName,context.getSession().getLanguage());
				String attrNameSmall = attrName.replaceAll(" ", "");
				String attrNameSmallHidden = attrNameSmall + "Hidden";
				String attrNameDefaultValue = com.matrixone.apps.domain.util.FrameworkProperties.getProperty(context, "emxEnterpriseChange." + attrName.replaceAll(" ", "") + ".Default");
				String attrNameValue = "";

				if (objectId!=null && !objectId.isEmpty()) {
					if (domObj.isKindOf(context, EnterpriseChangeConstants.TYPE_CHANGE_PROJECT) || domObj.isKindOf(context, EnterpriseChangeConstants.TYPE_CHANGE_TASK)) {
						attrNameValue = domObj.getAttributeValue(context, attrName);
					} else {
						if (relatedUptasks.isEmpty() && relatedSubtasks.isEmpty() && relatedChangeDeliverables.isEmpty()) {
							attrNameValue = attrNameDefaultValue;
						}
					}
				} else {
					if (objectBackDisciplineList!=null && !objectBackDisciplineList.isEmpty()) {
						if (objectBackDisciplineList.contains(attrName)) {
							attrNameValue = EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE;
						} else {
							attrNameValue = EnterpriseChangeConstants.CHANGE_DISCIPLINE_FALSE;
						}
					} else {
						attrNameValue = attrNameDefaultValue;
					}
				}

				//Check uptask --> if all no then disable and hide
				//if(relatedUptasks!=null && relatedUptasks.size()>0){
				if (relatedUptasks!=null && !relatedUptasks.isEmpty()) {
					Iterator relatedUptasksItr = relatedUptasks.iterator();
					while (relatedUptasksItr.hasNext()) {
						Map relatedUptask = (Map)relatedUptasksItr.next();
						//if(relatedUptask!=null && relatedUptask.size()>0){
						if (relatedUptask!=null && !relatedUptask.isEmpty()) {
							String relatedUptaskId = (String)relatedUptask.get(DomainConstants.SELECT_ID);
							if (relatedUptaskId!=null && !relatedUptaskId.equalsIgnoreCase("")) {
								DomainObject relatedUptaskDom = new DomainObject(relatedUptaskId);
								String relatedUptaskDomAttValue = relatedUptaskDom.getAttributeValue(context, attrName);
								//Change Domain Uptask = null --> Don't even show
								if (relatedUptaskDomAttValue == null) {
									isDisabled = "disabled";
									isDisplayed = false;
								}//Change Domain Uptask = No --> Don't even show
								else if (relatedUptaskDomAttValue.equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_FALSE)) {
									isDisabled = "disabled";
									isDisplayed = false;
								}//Change Domain Uptask = Yes --> Allow user to select it
								else if (relatedUptaskDomAttValue.equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE)) {
									isDisabled = "";
									isDisplayed = true;
								}
							}
						}
					}
				}

				//Check subtask --> if at least one is yes then disable
				//if(relatedSubtasks!=null && relatedSubtasks.size()>0){
				if (relatedSubtasks!=null && !relatedSubtasks.isEmpty()) {
					Iterator relatedSubtasksItr = relatedSubtasks.iterator();
					while (relatedSubtasksItr.hasNext()) {
						Map relatedSubtask = (Map)relatedSubtasksItr.next();
						//if(relatedSubtask!=null && relatedSubtask.size()>0){
						if (relatedSubtask!=null && !relatedSubtask.isEmpty()) {
							String relatedSubtaskId = (String)relatedSubtask.get(DomainConstants.SELECT_ID);
							if (relatedSubtaskId!=null && !relatedSubtaskId.equalsIgnoreCase("")) {
								DomainObject relatedSubtaskDom = new DomainObject(relatedSubtaskId);
								String relatedSubtaskDomAttValue = relatedSubtaskDom.getAttributeValue(context, attrName);
								if (relatedSubtaskDomAttValue.equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE)) {
									isDisabled = "disabled";
									isDisplayed = true;
									canBeEnabled = false;
								}
							}
						}
					}
				}

				//Check Change Deliverable --> if at least one connected then disable
				if (relatedChangeDeliverables!=null && !relatedChangeDeliverables.isEmpty()) {
					//Get change Deliverable Type for the discipline
					String deliverableTypes = com.matrixone.apps.domain.util.FrameworkProperties.getProperty(context, "emxEnterpriseChange." + attrNameSmall + ".DeliverableTypes");
					StringList deliverableTypesList = FrameworkUtil.split(deliverableTypes,",");
					//Compare Change Deliverable with the Discipline Change Deliverable
					Iterator relatedChangeDeliverablesItr = relatedChangeDeliverables.iterator();
					while (relatedChangeDeliverablesItr.hasNext()) {
						Map relatedChangeDeliverable = (Map)relatedChangeDeliverablesItr.next();
						if (relatedChangeDeliverable!=null && !relatedChangeDeliverable.isEmpty()) {
							String relatedChangeDeliverableId = (String)relatedChangeDeliverable.get(DomainConstants.SELECT_ID);
							if (relatedChangeDeliverableId!=null && !relatedChangeDeliverableId.equalsIgnoreCase("")) {
								for (int i=0;i<deliverableTypesList.size();i++) {
									if (new DomainObject(relatedChangeDeliverableId).isKindOf(context, PropertyUtil.getSchemaProperty((String) deliverableTypesList.get(i)))) {
										//If Change Task has a discipline Change Deliverable linked --> greys out the checkbox
										//for this discipline
										isDisabled = "disabled";
										canBeEnabled = false;
									}
								}
							}
						}
					}
				}

				//To determine if Checkbox is checked or not
				if (attrNameValue==null) {
					attrNameValue = EnterpriseChangeConstants.CHANGE_DISCIPLINE_FALSE;
					isChecked = "";
				} else if(attrNameValue.equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE)) {
					isChecked = "checked";
				} else if(attrNameValue.equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_FALSE)) {
					isChecked = "";
				}

				//In case of Copy or Template
				if (isDisabled.equalsIgnoreCase("disabled") && isDisplayed && isChecked.equalsIgnoreCase("")) {
					isChecked = "checked";
					attrNameValue = EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE;
				}

				//If mode is view, greys out all checkboxes
				if (strMode.equalsIgnoreCase("view")) {
					isDisabled = "disabled";
				}

				attributeToDisplay.put("attrName",attrName);
				attributeToDisplay.put("attrNameI18N",attrNameI18N);
				attributeToDisplay.put("attrNameSmall",attrNameSmall);
				attributeToDisplay.put("attrNameSmallHidden",attrNameSmallHidden);
				attributeToDisplay.put("attrNameValue",attrNameValue);
				attributeToDisplay.put("isChecked",isChecked);
				attributeToDisplay.put("isDisabled",isDisabled);
				attributeToDisplay.put("isDisplayed",isDisplayed.toString());
				attributeToDisplay.put("canBeEnabled",canBeEnabled.toString());
				listToDisplayAttributes.add(attributeToDisplay);
			}

			Iterator listToDisplayAttributesItr = listToDisplayAttributes.iterator();
			while (listToDisplayAttributesItr.hasNext()) {
				Map attributeToDisplay = (Map)listToDisplayAttributesItr.next();
				StringBuffer onClick = new StringBuffer();
				//Define javascript when user clicks checkbox
				onClick.append("var " + (String)attributeToDisplay.get("attrNameSmallHidden") + " = document.getElementById('" + (String)attributeToDisplay.get("attrNameSmallHidden") + "');");
				onClick.append("if(this.checked){");
				onClick.append((String)attributeToDisplay.get("attrNameSmallHidden") + ".value='Yes';");
				for (int k=0;k<listToDisplayAttributes.size();k++) {
					Map tempMap = (Map)listToDisplayAttributes.get(k);
					String tempNameSmall = (String)tempMap.get("attrNameSmall");
					if (!tempNameSmall.equalsIgnoreCase((String)attributeToDisplay.get("attrNameSmall"))) {
						if (Boolean.valueOf((String)tempMap.get("isDisplayed")).booleanValue()) {
							//if(!((String)tempMap.get("isDisabled")).equalsIgnoreCase("disabled")){
							if (Boolean.valueOf((String)tempMap.get("canBeEnabled")).booleanValue()) {
								onClick.append("document.getElementById('" + tempNameSmall + "').disabled=false;");
							}
						}
					}
				}
				onClick.append("}");
				onClick.append("else if(!this.checked){");
				onClick.append((String)attributeToDisplay.get("attrNameSmallHidden") + ".value='No';");
				for (int k=0;k<listToDisplayAttributes.size();k++) {
					Map tempMap = (Map)listToDisplayAttributes.get(k);
					String tempNameSmall = (String)tempMap.get("attrNameSmall");
					if(!tempNameSmall.equalsIgnoreCase((String)attributeToDisplay.get("attrNameSmall"))){
						if (Boolean.valueOf((String)tempMap.get("isDisplayed")).booleanValue()) {
							onClick.append("if(!this.checked ");
							for (int l=0;l<listToDisplayAttributes.size();l++ ){
								Map tempContextMap = (Map)listToDisplayAttributes.get(l);
								String tempContextNameSmall = (String)tempContextMap.get("attrNameSmall");
								if (!tempContextNameSmall.equalsIgnoreCase((String)attributeToDisplay.get("attrNameSmall")) && !tempContextNameSmall.equalsIgnoreCase(tempNameSmall)) {
									if (Boolean.valueOf((String)tempContextMap.get("isDisplayed")).booleanValue()) {
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
				attributeToDisplay.put("onClick",onClick.toString());

				//If only one discipline greys out the checkbox
				if (objectDisciplineList.contains((String)attributeToDisplay.get("attrName")) && objectDisciplineList.size()==1) {
					attributeToDisplay.remove("isDisabled");
					attributeToDisplay.put("isDisabled","disabled");
				}

				//In case of Back button test the objectBackDisciplineList
				//If only one discipline greys out the checkbox
				if (objectBackDisciplineList.contains((String)attributeToDisplay.get("attrName")) && objectBackDisciplineList.size()==1) {
					attributeToDisplay.remove("isDisabled");
					attributeToDisplay.put("isDisabled","disabled");
				}

				returnMapList.add(attributeToDisplay);
			}
			return returnMapList;
		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	 * Display Change Project or Change Task Change Discipline Values checkboxes
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return String - containing Change Discipline Values checkboxes
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public String displayChangeDiscipline(Context context,String[] args)throws Exception {
		try {
			String returnString = "";
			StringBuffer strBuffer = new StringBuffer();
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");
			String objectId = (String)paramMap.get("objectId");

			Map requestMap = (Map) programMap.get("requestMap");
			String strMode = (String) requestMap.get("mode");

			//Reformat the parameters
			HashMap progMap = new HashMap();
			progMap.put("objectId", objectId);
			progMap.put("mode", strMode);
			progMap.put("objectBackDisciplineList", new StringList());
			progMap.put("wizType", "");
			String[] methodargs = JPO.packArgs(progMap);
			MapList listToDisplayAttributes = getChangeDisciplinesToDisplay(context, methodargs);

			//Check how many discipline has the Change Project/Task
			//If only one the checkbox will have to be greyed out
			StringList objectDisciplineList = getAuthorizedChangeDisciplinesList(context, objectId);

			Boolean isPreviousDisplayed = false;
			Iterator listToDisplayAttributesItr = listToDisplayAttributes.iterator();
			while (listToDisplayAttributesItr.hasNext()) {
				Map attributeToDisplay = (Map)listToDisplayAttributesItr.next();

				if (Boolean.valueOf((String)attributeToDisplay.get("isDisplayed")).booleanValue()) {
					isPreviousDisplayed = true;
				} else {
					isPreviousDisplayed = false;
				}

				if ((strBuffer.toString()!=null && !strBuffer.toString().equalsIgnoreCase("")) && isPreviousDisplayed && Boolean.valueOf((String)attributeToDisplay.get("isDisplayed")).booleanValue()) {
					strBuffer.append("<br/>");
				}

				//If only one discipline greys out the checkbox
				if (objectDisciplineList.contains((String)attributeToDisplay.get("attrName")) && objectDisciplineList.size()==1) {
					attributeToDisplay.remove("isDisabled");
					attributeToDisplay.put("isDisabled","disabled");
				}

				if (Boolean.valueOf((String)attributeToDisplay.get("isDisplayed")).booleanValue()) {
					strBuffer.append("<input type=\"checkbox\" id=\"" + XSSUtil.encodeForHTMLAttribute(context,(String)attributeToDisplay.get("attrNameSmall")) + "\" name=\"" + XSSUtil.encodeForHTMLAttribute(context,(String)attributeToDisplay.get("attrNameSmall")) + "\" " + XSSUtil.encodeForHTMLAttribute(context,(String)attributeToDisplay.get("isChecked")) + " " + XSSUtil.encodeForHTMLAttribute(context,(String)attributeToDisplay.get("isDisabled")) + " onClick=\"" + XSSUtil.encodeForHTMLAttribute(context,(String)attributeToDisplay.get("onClick")) + "\"/>");
					strBuffer.append((String)attributeToDisplay.get("attrNameI18N"));
				}
				strBuffer.append("<input type='hidden' id='"+ XSSUtil.encodeForHTMLAttribute(context,(String)attributeToDisplay.get("attrNameSmallHidden")) + "' name='" + XSSUtil.encodeForHTMLAttribute(context,(String)attributeToDisplay.get("attrNameSmallHidden")) + "' value='" + XSSUtil.encodeForHTMLAttribute(context,(String)attributeToDisplay.get("attrNameValue")) + "'/>");

			}
			//Added:04-Jan-11:IQA:R211:ECH:IR-075431V6R2012
			//The input name should be the same as form field name
			strBuffer.insert(0, "<input type='hidden' name='Change Discipline' value='dummy'>");
			//End:04-Jan-11:IQA:R211:ECH:IR-075431V6R2012
			returnString = strBuffer.toString() ;
			return returnString;
		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	 * Update Change Project or Change Task Change Discipline Values
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId, new Value for Change Discipline
	 * @return Boolean - false if update fails
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public boolean updateChangeDiscipline(Context context,String args[]) throws Exception {
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String objectId = (String) paramMap.get("objectId");
			DomainObject domObj = new DomainObject(objectId);
			HashMap requestMap = (HashMap) programMap.get("requestMap");

			String strInterfaceName = EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE;

			//Check if an the change discipline interface has been already connected
			//String strCommand = "print bus " + objectId +" select interface["+ strInterfaceName + "] dump";
			String strCommand = "print bus $1 select $2 dump";

			//If no interface --> add one
			if ((com.matrixone.apps.domain.util.MqlUtil
					.mqlCommand(context, strCommand, objectId, "interface["
							+ strInterfaceName + "]"))
					.equalsIgnoreCase("false")) {
				//String strAddInterface = "modify bus " + objectId +" add interface \'"+ strInterfaceName + "\'";
				String strAddInterface = "modify bus $1 add interface $2";
				com.matrixone.apps.domain.util.MqlUtil.mqlCommand(context,strAddInterface,objectId,strInterfaceName);
			}

			BusinessInterface busInterface = new BusinessInterface(strInterfaceName, context.getVault());
			AttributeTypeList listInterfaceAttributes = busInterface.getAttributeTypes(context);

			Iterator listInterfaceAttributesItr = listInterfaceAttributes.iterator();
			while (listInterfaceAttributesItr.hasNext()) {
				String attrName = ((AttributeType)listInterfaceAttributesItr.next()).getName();
				String attrNameSmall = attrName.replaceAll(" ", "");
				String attrNameSmallHidden = attrNameSmall + "Hidden";
				String[] attrNameValue = (String[])requestMap.get(attrNameSmallHidden);

				if (attrNameValue!=null) {
					domObj.setAttributeValue(context, attrName, attrNameValue[0]);
				} else {
					domObj.setAttributeValue(context, attrName, EnterpriseChangeConstants.CHANGE_DISCIPLINE_FALSE);
				}
			}
			return true;	
		} catch (Exception e) {
			throw e;
		}
	}


	/**
	 * Display Change Discipline values for related Change Projects
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId, new Value for Change Discipline
	 * @return StringList - containing Change Discipline values
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public StringList displayChangeDisciplineValue(Context context,String[] args) throws Exception {
		try {
			StringList returnStringList = new StringList();
			String languageStr  =  context.getSession().getLanguage();

			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");
			returnStringList.setSize(objectList.size());

			String strInterfaceName = EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE;
			BusinessInterface busInterface = new BusinessInterface(strInterfaceName, context.getVault());
			AttributeTypeList listInterfaceAttributes = busInterface.getAttributeTypes(context);

			Iterator objectListItr = objectList.iterator();
			int i = 0;
			while (objectListItr.hasNext()) {
				StringBuffer stringBuffer = new StringBuffer();
				Map objectMap = (Map) objectListItr.next();
				if (objectMap!=null && objectMap.size()>0) {
					String objectMapId = (String)objectMap.get(DomainConstants.SELECT_ID);
					if (objectMapId!=null && !objectMapId.equalsIgnoreCase("")) {
						DomainObject domObjectMapId = new DomainObject(objectMapId);

						Iterator listInterfaceAttributesItr = listInterfaceAttributes.iterator();
						while (listInterfaceAttributesItr.hasNext()) {
							String attrName = ((AttributeType) listInterfaceAttributesItr.next()).getName();
							String attrValue = domObjectMapId.getAttributeValue(context, attrName);
							if (attrValue.equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE)) {
								if (!stringBuffer.toString().equalsIgnoreCase("")) {
									stringBuffer.append(",");
								}
								stringBuffer.append(i18nNow.getAttributeI18NString(attrName,languageStr));
							}
						}
					}
					returnStringList.set(i, stringBuffer.toString());
					i++;
				}
			}
			return returnStringList;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Add interface Change Discipline on Change Project or Change Task types
	 * Called at Change Project or Change Task creation
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId String
	 * @return int 0 on success 1 on failure
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public int addChangeProjectChangeTaskChangeDisciplineInterface(Context context, String[] args) throws Exception {
		try {
			int returnInt = 1;
			String objectId = args[0];
			if (objectId!=null && !objectId.equalsIgnoreCase("")) {
				String strInterfaceName = EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE;

				//Check if an the change discipline interface has been already connected
				//String strCommand = "print bus " + objectId +" select interface["+ strInterfaceName + "] dump";
				String strCommand = "print bus $1 select $2 dump";
				//If no interface --> add one
				if ((com.matrixone.apps.domain.util.MqlUtil.mqlCommand(context,strCommand,objectId,"interface["+ strInterfaceName + "]")).equalsIgnoreCase("false")) {
					//String strAddInterface = "modify bus " + objectId +" add interface \'"+ strInterfaceName + "\'";
					String strAddInterface = "modify bus $1 add interface $2";
					com.matrixone.apps.domain.util.MqlUtil.mqlCommand(context,strAddInterface,objectId,strInterfaceName);
					returnInt = 0;
				}
			}
			return returnInt;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Add interface Change Discipline on Applicable Item relationship
	 * Called at Applicable Item creation
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - relId String
	 * @return int 0 on success 1 on failure
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public int addApplicableItemChangeDisciplineInterface(Context context, String[] args) throws Exception {
		try {
			int returnInt = 1;
			String relId = args[0];
			if (relId!=null && !relId.equalsIgnoreCase("")) {
				String strInterfaceName = EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE;
				String strInterfaceNamemql = "interface["+strInterfaceName+"]";

				//Check if an the change discipline interface has been already connected
			//	String strCommand = "print connection " + relId +" select interface["+ strInterfaceName + "] dump";
				
				 String strCommand = "print connection $1 select $2 dump";
				//If no interface --> add one
				if ((com.matrixone.apps.domain.util.MqlUtil.mqlCommand(context, strCommand, relId, strInterfaceNamemql)).equalsIgnoreCase("false")) {
					//String strAddInterface = "modify connection " + relId +" add interface \'"+ strInterfaceName + "\'";
					String strAddInterface = "modify Connection $1 add interface $2";
					//String strMessage = com.matrixone.apps.domain.util.MqlUtil.mqlCommand(context,strAddInterface);
					String strMessage = com.matrixone.apps.domain.util.MqlUtil.mqlCommand(context, strAddInterface, relId, strInterfaceName);
					returnInt = 0;
				}
			}
			return returnInt;
		} catch (Exception e) {
			throw e;
		}
	}


	/**
	 * Method called by PRG jsp files (emxProgramCentralWBSAddDialog.jsp and emxProgramCentralWBSInsertDialog.jsp)
	 * Return the MapList containing needed information to display Change Discipline checkboxes with proper js
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          programMap - contains parentOID
	 * @return MapList - return list containing needed information to display Change Discipline
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public MapList getChangeDisciplineToDisplayForChangeTask(Context context, String[] args) throws Exception {
		try {
			MapList returnMapList = new MapList();
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String parentOID = (String) programMap.get("parentOID");
			String languageStr = context.getSession().getLanguage();

			MapList listToDisplayAttributes = new MapList();

			if (parentOID!=null && !parentOID.equalsIgnoreCase("")) {
				Task task = new Task(parentOID);

				String strInterfaceName = EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE;
				BusinessInterface busInterface = new BusinessInterface(strInterfaceName, context.getVault());
				AttributeTypeList listInterfaceAttributes = busInterface.getAttributeTypes(context);

				//Check how many discipline has the father
				StringList fatherDisciplineList = new StringList();
				for (int i=0;i<listInterfaceAttributes.size();i++) {
					String attrName = ((AttributeType) listInterfaceAttributes.get(i)).getName();
					String attrNameParentValue = task.getAttributeValue(context, attrName);
					if(attrNameParentValue!=null && attrNameParentValue.equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE)){
						fatherDisciplineList.add(attrName);
					}
				}

				//If father has no discipline --> bubble up WBS to find one with Discipline
				if (fatherDisciplineList==null || fatherDisciplineList.isEmpty()) {
					Boolean flag = false;
					String parentId = task.getInfo(context, task.SELECT_ID);
					while (!flag) {
						MapList parentsList = new com.matrixone.apps.program.Task(parentId).getParentInfo(context,1,null);
						if (parentsList!=null && !parentsList.isEmpty()) {
							java.util.Iterator parentsListItr = parentsList.iterator();
							while (parentsListItr.hasNext()) {
								Map parentList = (Map)parentsListItr.next();
								if (parentList!=null && !parentList.isEmpty()) {
									parentId = (String)parentList.get(DomainConstants.SELECT_ID);
									if(parentId!=null && !parentId.equalsIgnoreCase("")){
										for (int i=0;i<listInterfaceAttributes.size();i++) {
											String attrName = ((AttributeType) listInterfaceAttributes.get(i)).getName();
											String attrNameParentValue = new com.matrixone.apps.program.Task(parentId).getAttributeValue(context, attrName);
											if (attrNameParentValue!=null && attrNameParentValue.equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE)) {
												fatherDisciplineList.add(attrName);
											}
										}
										if (fatherDisciplineList!=null && !fatherDisciplineList.isEmpty()) {
											flag = true;
										}
									} else {flag=true;}
								} else {flag=true;}
							}
						} else {flag=true;}
					}
				}

				//If any Discipline found in WBS --> allow all Discipline
				if (fatherDisciplineList==null || fatherDisciplineList.isEmpty()) {
					for (int i=0;i<listInterfaceAttributes.size();i++) {
						String attrName = ((AttributeType) listInterfaceAttributes.get(i)).getName();
						fatherDisciplineList.add(attrName);
					}
				}

				for (int j=0;j<listInterfaceAttributes.size();j++) {
					Map attributeToDisplay = new HashMap();
					String attrName = ((AttributeType) listInterfaceAttributes.get(j)).getName();
					String attrNameI18N = i18nNow.getAttributeI18NString(attrName,languageStr);
					String attrNameSmall = attrName.replaceAll(" ", "");
					String attrNameSmallHidden = attrNameSmall + "Hidden";
					String attrNameValue = "";

					String attrNameParentValue = task.getAttributeValue(context, attrName);
					String attrNameDefaultValue = com.matrixone.apps.domain.util.FrameworkProperties.getProperty(context, "emxEnterpriseChange." + attrNameSmall + ".Default");
					String isChecked = "";
					String isDisabled = "";

					//Check if father has the Change Displine
					Boolean isDisplayed = true;
					//If father has the discipline --> display discipline
					if (fatherDisciplineList.contains(attrName)) {
						attrNameValue = EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE;
						isDisplayed = true;
						//If father has only one discipline --> force to select
						if (fatherDisciplineList.size()==1) {
							isChecked = "checked";
							isDisabled = "disabled";
						}
						//if father has multiple disciplines --> select by default
						else {
							attrNameValue = EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE;
							isDisabled = "";
							isChecked = "checked";
						}
					}
					//If father doesn't have discipline --> don't display discipline
					else {
						attrNameValue = EnterpriseChangeConstants.CHANGE_DISCIPLINE_FALSE;
						isDisabled = "disabled";
						isChecked = "";
						isDisplayed = false;
					}

					attributeToDisplay.put("attrName",attrName);
					attributeToDisplay.put("attrNameI18N",attrNameI18N);
					attributeToDisplay.put("attrNameSmall",attrNameSmall);
					attributeToDisplay.put("attrNameSmallHidden",attrNameSmallHidden);
					attributeToDisplay.put("attrNameValue",attrNameValue);
					attributeToDisplay.put("attrNameParentValue",attrNameParentValue);
					attributeToDisplay.put("attrNameDefaultValue",attrNameDefaultValue);
					attributeToDisplay.put("isChecked",isChecked);
					attributeToDisplay.put("isDisabled",isDisabled);
					attributeToDisplay.put("isDisplayed",isDisplayed.toString());
					listToDisplayAttributes.add(attributeToDisplay);
				}
			}

			Iterator listToDisplayAttributesItr = listToDisplayAttributes.iterator();
			while (listToDisplayAttributesItr.hasNext()) {
				Map attributeToDisplay = (Map)listToDisplayAttributesItr.next();

				StringBuffer strBuffer = new StringBuffer();
				strBuffer.append("var " + (String)attributeToDisplay.get("attrNameSmallHidden") + " = document.getElementById('" + (String)attributeToDisplay.get("attrNameSmallHidden") + "');");
				strBuffer.append("if(this.checked){");
				strBuffer.append((String)attributeToDisplay.get("attrNameSmallHidden") + ".value='Yes';");
				for (int k=0;k<listToDisplayAttributes.size();k++) {
					Map tempMap = (Map)listToDisplayAttributes.get(k);
					String tempNameSmall = (String)tempMap.get("attrNameSmall");
					if (!tempNameSmall.equalsIgnoreCase((String)attributeToDisplay.get("attrNameSmall"))) {
						if (Boolean.valueOf((String)tempMap.get("isDisplayed")).booleanValue()) {
							strBuffer.append("document.getElementById('" + tempNameSmall + "').disabled=false;");
						}
					}
				}
				strBuffer.append("}");

				for (int k=0;k<listToDisplayAttributes.size();k++) {
					Map tempMap = (Map)listToDisplayAttributes.get(k);
					String tempNameSmall = (String)tempMap.get("attrNameSmall");
					if (!tempNameSmall.equalsIgnoreCase((String)attributeToDisplay.get("attrNameSmall"))) {
						if (Boolean.valueOf((String)tempMap.get("isDisplayed")).booleanValue()) {
							strBuffer.append("else if(!this.checked ");
							for (int l=0;l<listToDisplayAttributes.size();l++) {
								Map tempContextMap = (Map)listToDisplayAttributes.get(l);
								String tempContextNameSmall = (String)tempContextMap.get("attrNameSmall");
								if (!tempContextNameSmall.equalsIgnoreCase((String)attributeToDisplay.get("attrNameSmall")) && !tempContextNameSmall.equalsIgnoreCase(tempNameSmall)) {
									if (Boolean.valueOf((String)tempContextMap.get("isDisplayed")).booleanValue()) {
										strBuffer.append("&& !document.getElementById('" + tempContextNameSmall + "').checked ");
									}

								}
							}
							strBuffer.append("){");
							strBuffer.append((String)attributeToDisplay.get("attrNameSmallHidden") + ".value='No';");
							strBuffer.append("document.getElementById('" + tempNameSmall + "').disabled=true;");
							strBuffer.append("}");
						}

					}
				}
				attributeToDisplay.put("onClick",strBuffer.toString());
				returnMapList.add(attributeToDisplay);
			}
			return returnMapList;
		} catch(Exception e) {
			throw e;
		}
	}

	/**
	 * Method called to have authorized Change Discipline for an object
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectId - objectId of the object to determine authorized Change Discipline
	 * @return MapList - return list containing authorized Change Discipline
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public StringList getAuthorizedChangeDisciplinesList(Context context, String objectId) throws Exception {
		try {
			StringList returnStringList = new StringList();
			String strInterfaceName = EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE;
			BusinessInterface busInterface = new BusinessInterface(strInterfaceName, context.getVault());
			AttributeTypeList listChangeDisciplineAttributes = busInterface.getAttributeTypes(context);

			StringList objectSelects = new StringList(2);
			objectSelects.addElement(DomainConstants.SELECT_ID);
			objectSelects.addElement(DomainConstants.SELECT_TYPE);

			StringList relationshipSelects = new StringList(1);
			relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

			//First get the Change Disciplines of the parentOID
			//If objectId is Change Project or Change Task, get directly its Change Disciplines
			//If objectId is not Change Project or Change Task, get first up Change Project or up Change Task and get its Change Disciplines
			if (objectId!=null && !objectId.isEmpty()) {
				//Get Change Discipline for this object
				DomainObject objectDom = new DomainObject(objectId);
				if (objectDom.isKindOf(context, EnterpriseChangeConstants.TYPE_CHANGE_PROJECT) || objectDom.isKindOf(context, EnterpriseChangeConstants.TYPE_CHANGE_TASK)) {
					Iterator listChangeDisciplineAttributesItr = listChangeDisciplineAttributes.iterator();
					while (listChangeDisciplineAttributesItr.hasNext()) {
						String changeDisciplineName = ((AttributeType)listChangeDisciplineAttributesItr.next()).getName();
						if (objectDom.getAttributeValue(context, changeDisciplineName).equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE)) {
							returnStringList.add(changeDisciplineName);
						}
					}
				} else {
					//Get all upper tasks (Change Project, Change Task)
					MapList tempRelatedUptasks = objectDom.getRelatedObjects(context,
							DomainConstants.RELATIONSHIP_SUBTASK,
							DomainConstants.QUERY_WILDCARD,
							objectSelects,
							relationshipSelects,
							true,	//to relationship
							false,	//from relationship
							(short)0,
							DomainConstants.EMPTY_STRING,
							DomainConstants.EMPTY_STRING,
							0);

					if (tempRelatedUptasks!=null && !tempRelatedUptasks.isEmpty()) {
						//Sort MapList
						tempRelatedUptasks.sort(DomainConstants.SELECT_LEVEL, "ascending", "string");
						//Find the first upper Change Project or Change Task
						Boolean flag = false;
						Iterator tempRelatedUptasksItr = tempRelatedUptasks.iterator();
						while (tempRelatedUptasksItr.hasNext() && !flag) {
							Map tempRelatedUptask = (Map)tempRelatedUptasksItr.next();
							if (tempRelatedUptask!=null && !tempRelatedUptask.isEmpty()) {
								String tempRelatedUptaskId = (String)tempRelatedUptask.get(DomainConstants.SELECT_ID);
								String tempRelatedUptaskType = (String)tempRelatedUptask.get(DomainConstants.SELECT_TYPE);
								if (tempRelatedUptaskId!=null && !tempRelatedUptaskId.equalsIgnoreCase("")) {
									DomainObject tempRelatedUptaskDom = new DomainObject(tempRelatedUptaskId);
									if (tempRelatedUptaskDom.isKindOf(context,DomainConstants.TYPE_CHANGE_PROJECT) || tempRelatedUptaskDom.isKindOf(context,DomainConstants.TYPE_CHANGE_TASK)) {
										flag = true;
										Iterator listChangeDisciplineAttributesItr = listChangeDisciplineAttributes.iterator();
										while (listChangeDisciplineAttributesItr.hasNext()) {
											String changeDisciplineName = ((AttributeType)listChangeDisciplineAttributesItr.next()).getName();
											if (tempRelatedUptaskDom.getAttributeValue(context, changeDisciplineName).equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE)) {
												returnStringList.add(changeDisciplineName);
											}
										}
									}
								}
							}
						}
					}
				}
			}
			return returnStringList;
		} catch(Exception e) {
			throw e;
		}
	}

	/**
	 * Method called by PRG jsp files (emxprojectCreateWizardClone.jsp)
	 * Return the MapList containing available Project List compliant in term of Change Discipline
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          programMap - contains parentOID and projectListwithparent
	 * @return MapList - return list containing available Project List compliant in term of Change Discipline
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public MapList filterProjectListWithParentForChangeDisciplines(Context context, String[] args) throws Exception {
		try {
			MapList returnMapList = new MapList();
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String) programMap.get("objectId");
			MapList projectListwithparents = (MapList) programMap.get("projectListwithparent");
			String wizType = (String) programMap.get("wizType");

			if (projectListwithparents!=null && !projectListwithparents.isEmpty()) {
				StringList objectSelects = new StringList(2);
				objectSelects.addElement(DomainConstants.SELECT_ID);
				objectSelects.addElement(DomainConstants.SELECT_TYPE);

				StringList relationshipSelects = new StringList(1);
				relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

				String strInterfaceName = EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE;
				BusinessInterface busInterface = new BusinessInterface(strInterfaceName, context.getVault());
				AttributeTypeList listChangeDisciplineAttributes = busInterface.getAttributeTypes(context);

				StringList allowedChangeDisciplines = new StringList();

				if (objectId!=null && !objectId.isEmpty()) {
					allowedChangeDisciplines = getAuthorizedChangeDisciplinesList(context, objectId);
					if (allowedChangeDisciplines!=null && !allowedChangeDisciplines.isEmpty()) {
						//Then get the Change Discipline for each of the original Project List
						Iterator projectListwithparentsItr = projectListwithparents.iterator();
						MapList tempMapList = new MapList();
						while (projectListwithparentsItr.hasNext()) {
							Map projectListwithparent = (Map)projectListwithparentsItr.next();
							if (projectListwithparent!=null && !projectListwithparent.isEmpty()) {
								String projectListwithparentId = (String) projectListwithparent.get(DomainConstants.SELECT_ID);
								if (projectListwithparentId!=null && !projectListwithparentId.isEmpty()) {
									DomainObject projectListwithparentDom = new DomainObject(projectListwithparentId);
									//wizType != Copy -> get the Change Project which have exclusively the allowed Disciplines
									//Ex: allowed = Design -> only Design, allowed = Manuf -> only Manuf

									//wizType = Copy -> get the Change Project which have at least the allowed Disciplines
									//Ex: allowed = Design -> only Design or Design and Manuf, allowed = Manuf -> only Manuf or Design and Manuf

									//If project is kind of Change Project or Change Task, get directly its Change Disciplines
									if (projectListwithparentDom.isKindOf(context,DomainConstants.TYPE_CHANGE_PROJECT) || projectListwithparentDom.isKindOf(context,DomainConstants.TYPE_CHANGE_TASK)) {
										Iterator listChangeDisciplineAttributesItr = listChangeDisciplineAttributes.iterator();
										Boolean flag = false;
										while (listChangeDisciplineAttributesItr.hasNext()) {
											String changeDisciplineName = ((AttributeType)listChangeDisciplineAttributesItr.next()).getName();
											if (allowedChangeDisciplines.contains(changeDisciplineName)) {
												if ((projectListwithparentDom.getAttributeValue(context, changeDisciplineName)).equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE)) {
													flag = true;
												}
											}
										}

										if (flag) {
											if (!wizType.equalsIgnoreCase("Copy")) {
												//Filter again the to get the Change Project which have exclusively the allowed Disciplines
												Iterator listChangeDisciplineAttributes2Itr = listChangeDisciplineAttributes.iterator();
												flag = true;
												while (listChangeDisciplineAttributes2Itr.hasNext()) {
													String changeDisciplineName = ((AttributeType)listChangeDisciplineAttributes2Itr.next()).getName();
													if ((!allowedChangeDisciplines.contains(changeDisciplineName) && projectListwithparentDom.getAttributeValue(context, changeDisciplineName).equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE))) {
														flag = false;
													}
												}
												if (flag) {
													returnMapList.add(projectListwithparent);
												}
											} else {
												returnMapList.add(projectListwithparent);
											}
										}
									}

									//If project id not Change Project or Change Task, get sub Change Projects or Change Tasks and gets its Change Disciplines
									else {
										Boolean flag = true;
										Boolean flagSummary = true;
										//Get all sub tasks (Change Project, Change Task)
										MapList tempRelatedSubtasks = projectListwithparentDom.getRelatedObjects(context,
												DomainConstants.RELATIONSHIP_SUBTASK,
												DomainConstants.QUERY_WILDCARD,
												objectSelects,
												relationshipSelects,
												//false,	//to relationship
												false,	//to relationship
												true,	//from relationship
												(short)0,
												DomainConstants.EMPTY_STRING,
												DomainConstants.EMPTY_STRING,
												0);

										if (tempRelatedSubtasks!=null && !tempRelatedSubtasks.isEmpty()) {
											//Sort MapList
											tempRelatedSubtasks.sort(DomainConstants.SELECT_LEVEL, "ascending", "string");
											//Find only Change Project or Change Task
											Iterator tempRelatedSubtasksItr = tempRelatedSubtasks.iterator();
											while (tempRelatedSubtasksItr.hasNext()) {
												Map tempRelatedSubtask = (Map)tempRelatedSubtasksItr.next();
												if (tempRelatedSubtask!=null && !tempRelatedSubtask.isEmpty()) {
													String tempRelatedSubtaskId = (String)tempRelatedSubtask.get(DomainConstants.SELECT_ID);
													String tempRelatedSubtaskType = (String)tempRelatedSubtask.get(DomainConstants.SELECT_TYPE);
													if (tempRelatedSubtaskId!=null && !tempRelatedSubtaskId.equalsIgnoreCase("")) {
														DomainObject tempRelatedSubtaskDom = new DomainObject(tempRelatedSubtaskId);
														if (tempRelatedSubtaskDom.isKindOf(context,DomainConstants.TYPE_CHANGE_PROJECT) || tempRelatedSubtaskDom.isKindOf(context,DomainConstants.TYPE_CHANGE_TASK)) {
															Iterator listChangeDisciplineAttributesItr = listChangeDisciplineAttributes.iterator();
															while (listChangeDisciplineAttributesItr.hasNext()) {
																String changeDisciplineName = ((AttributeType)listChangeDisciplineAttributesItr.next()).getName();
																if (allowedChangeDisciplines.contains(changeDisciplineName)) {
																	if ((tempRelatedSubtaskDom.getAttributeValue(context, changeDisciplineName)).equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE)) {
																		flag = true;
																	}
																}
															}

															if (flag) {
																if (!wizType.equalsIgnoreCase("Copy")) {
																	//Filter again the to get the Change Project which have exclusively the allowed Disciplines
																	Iterator listChangeDisciplineAttributes2Itr = listChangeDisciplineAttributes.iterator();
																	flag = true;
																	while (listChangeDisciplineAttributes2Itr.hasNext()) {
																		String changeDisciplineName = ((AttributeType)listChangeDisciplineAttributes2Itr.next()).getName();
																		if ((!allowedChangeDisciplines.contains(changeDisciplineName) && tempRelatedSubtaskDom.getAttributeValue(context, changeDisciplineName).equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE))) {
																			flag = false;
																		}
																	}
																}
															}
														}
													}
												}
												if (!flag) {
													flagSummary = false;
												}
											}
										}
										if (flagSummary) {
											returnMapList.add(projectListwithparent);
										}
									}
								}
							}
						}
					}
					//If no Up Change Task or Change Project found, allow all discipline, so return original list
					else {
						returnMapList.addAll(projectListwithparents);
					}
				}
				//If objectId is null it means that is an initial creation. So return all
				else {
					returnMapList.addAll(projectListwithparents);
				}
			}
			return returnMapList;
		}catch(Exception e){
			throw e;
		}
	}

	/* Method called by PRG jsp files (emxProgramCentralWBSSummary.jsp)
	 * Return the MapList containing available WBS List compliant in term of Change Discipline
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          programMap - contains parentOID and WBSprojectListwithparent
	 * @return MapList - return list containing available WBS List compliant in term of Change Discipline
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public MapList filterWBSProjectListWithParentForChangeDisciplines(Context context, String[] args) throws Exception {
		try {
			MapList returnMapList = new MapList();
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			//objectId = selected Object in WBS
			String objectId = (String) programMap.get("objectId");
			MapList wbsMasterList = (MapList) programMap.get("wbsMasterList");

			if (wbsMasterList!=null && !wbsMasterList.isEmpty()) {
				if (objectId!=null && !objectId.isEmpty()) {
					String strInterfaceName = EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE;
					BusinessInterface busInterface = new BusinessInterface(strInterfaceName, context.getVault());
					AttributeTypeList listChangeDisciplineAttributes = busInterface.getAttributeTypes(context);

					StringList allowedChangeDisciplines = getAuthorizedChangeDisciplinesList(context, objectId);
					if (allowedChangeDisciplines!=null && !allowedChangeDisciplines.isEmpty() ){
						//Then get the Change Discipline for each of the original WBS List
						Iterator wbsMasterListItr = wbsMasterList.iterator();
						while (wbsMasterListItr.hasNext()) {
							Map wbsMaster = (Map)wbsMasterListItr.next();
							if (wbsMaster!=null && !wbsMaster.isEmpty()) {
								String wbsMasterId = (String) wbsMaster.get(DomainConstants.SELECT_ID);
								String wbsMasterLevel = (String) wbsMaster.get(DomainConstants.SELECT_LEVEL);
								if (wbsMasterId!=null && !wbsMasterId.isEmpty()) {
									if(wbsMasterLevel.equalsIgnoreCase("0")){
										returnMapList.add(wbsMaster);
									} else {
										DomainObject wbsMasterDom = new DomainObject(wbsMasterId);
										//If project is kind of Change Project or Change Task, get directly its Change Disciplines
										if (wbsMasterDom.isKindOf(context,DomainConstants.TYPE_CHANGE_PROJECT) || wbsMasterDom.isKindOf(context,DomainConstants.TYPE_CHANGE_TASK)) {
											Iterator listChangeDisciplineAttributesItr = listChangeDisciplineAttributes.iterator();
											Boolean flag = false;
											while (listChangeDisciplineAttributesItr.hasNext()) {
												String changeDisciplineName = ((AttributeType)listChangeDisciplineAttributesItr.next()).getName();
												if (allowedChangeDisciplines.contains(changeDisciplineName)) {
													if ((wbsMasterDom.getAttributeValue(context, changeDisciplineName)).equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE)) {
														flag = true;
													}
												}
											}
											if (flag) {
												//Filter again the to get the Change Project which have exclusively the allowed Disciplines
												Iterator listChangeDisciplineAttributes2Itr = listChangeDisciplineAttributes.iterator();
												flag = true;
												while (listChangeDisciplineAttributes2Itr.hasNext()) {
													String changeDisciplineName = ((AttributeType)listChangeDisciplineAttributes2Itr.next()).getName();
													if ((!allowedChangeDisciplines.contains(changeDisciplineName) && wbsMasterDom.getAttributeValue(context, changeDisciplineName).equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE))) {
														flag = false;
													}
												}
												if (flag) {
													returnMapList.add(wbsMaster);
												}
											}
										} else {
											returnMapList.add(wbsMaster);
										}
									}
								}
							}
						}
					}
					//If no Up Change Task or Change Project found, allow all discipline, so return original list
					else {
						returnMapList.addAll(wbsMasterList);
					}
				}
			}
			//returnMapList.addAll(wbsMasterList);
			return returnMapList;
		} catch(Exception e) {
			throw e;
		}
	}
	/**
	 * Add interface Change Discipline On Change Task
	 * Called when any of the Task Type i.e. Task,Gate,Phase, Change Task or Change Task is changed
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId String
	 * @throws FrameworkException if the operation fails
	 * @since EnterpriseChange R215
	 */
	public void addChangeDisciplineInterfaceOnChangeTask(Context context,
			String[] args) throws FrameworkException {
		try {
			String objectId = args[0];
			if (objectId != null && !objectId.equalsIgnoreCase("")) {
				DomainObject dom = new DomainObject(objectId);
				if (dom.isKindOf(context,
						EnterpriseChangeConstants.TYPE_CHANGE_TASK)) {
					String strInterfaceName = EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE;
					String strInterfaceNamemql = "interface["+strInterfaceName+"]";
					// Check if an the change discipline interface has been
					// already connected
/*					String strCommand = "print bus " + objectId + " select interface[" + strInterfaceName + "] dump";

					// If no interface --> add one
					*/

					String strCommand = "print bus $1 select $2 dump";
					
					if((com.matrixone.apps.domain.util.MqlUtil.mqlCommand(context, strCommand, objectId, strInterfaceNamemql)).equalsIgnoreCase("false")){
					//String strAddInterface = "modify bus " + objectId + " add interface \'" + strInterfaceName + "\'"; 
					String strAddInterface = "modify bus $1 add interface $2";
					//String strMessage = com.matrixone.apps.domain.util.MqlUtil.mqlCommand(context, strAddInterface);
				    String strMessage = com.matrixone.apps.domain.util.MqlUtil.mqlCommand(context, strAddInterface, objectId, strInterfaceName);
					}
				}
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
	}
	
}
