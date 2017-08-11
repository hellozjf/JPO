/*
 ** emxEnterpriseChangeReportBase
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
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.enterprisechange.ChangeTask;
import com.matrixone.apps.enterprisechange.Decision;
import com.matrixone.apps.enterprisechange.EnterpriseChangeConstants;
import com.matrixone.apps.enterprisechange.EnterpriseChangeUtil;


public class emxEnterpriseChangeReportBase_mxJPO extends emxDomainObject_mxJPO {

	/**
	 * Create a new emxEnterpriseChangeReportBase object.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments.
	 * @return an emxEnterpriseChangeReportBase object
	 * @throws Exception if operation fails
	 * @since EnterpriseChange R211.HF3
	 * @grade 0
	 */
	public emxEnterpriseChangeReportBase_mxJPO (Context context, String[] args) throws Exception {
		super(context, args);
	}

	/**
	 * Main entry point.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return an integer status code (0 = success)
	 * @throws Exception if operation fails
	 * @since EnterpriseChange R211.HF3
	 * @grade 0
	 */
	public int mxMain (Context context, String[] args) throws Exception {
		if (!context.isConnected()) {
			throw  new Exception(EnoviaResourceBundle.getProperty(context,"Components","emxComponents.Error.UnsupportedClient",context.getSession().getLanguage()));
		}
		return  0;
	}
	
	public enum ModeFilter {
		Sigma,
		Delta
	};

	public enum ApplicableBy {
		Added,
		Inherited,
		Removed
	};


	/**
	 * Method to define if the Applicable Change Tasks command should be available
	 *
	 * @param context the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return Boolean - true if field should be displayed and false if not
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211.HF3
	 * @deprecated since R418
	 */
	public boolean isApplicableChangeTasksEnabled(Context context, String[] args) throws Exception {
		try{
			Boolean returnBoolean = false;
			if (EnterpriseChangeUtil.isApplicabilityManagementEnabled(context)) {
				try{
					Boolean showApplicabilitySummary = Boolean.valueOf(EnoviaResourceBundle.getProperty(context, "emxEnterpriseChange.ApplicableChangeTasks.Enable"));
					if(showApplicabilitySummary!=null){
						returnBoolean = showApplicabilitySummary;
					}
				}catch(Exception e){
					returnBoolean = false;
				}
			}
			return returnBoolean;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Method to display the Applicable Change Tasks
	 * The method will dispatch according to the selected Mode (Delta, Sigma)
	 *
	 * @param context the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return MapList - containing the List of Change Tasks Applicable for the context object
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211.HF3
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getApplicableChangeTasks(Context context, String[] args) throws Exception {
		try {
			MapList returnMapList = new MapList();

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String) programMap.get("objectId");

			//Get filter parameters
			String modeFilterValue = (String)programMap.get(EnterpriseChangeConstants.COMMAND_APPLICABLE_CHANGE_TASKS_MODE_FILTER);
			ModeFilter modeFilter = null;
			if (modeFilterValue!=null && !modeFilterValue.isEmpty()) {
				modeFilter = ModeFilter.valueOf(modeFilterValue);
			}

			if ((objectId!=null && !objectId.isEmpty()) && modeFilter!=null) {
				switch (modeFilter) {
					case Delta:	returnMapList = this.getDeltaApplicableChangeTasks(context, objectId);break;
					case Sigma:	returnMapList = this.getSigmaApplicableChangeTasks(context, objectId);break;
				}
			}
			return returnMapList;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Method to display the Delta Applicable Change Tasks
	 *
	 * @param context the eMatrix Context object
	 * @param objectId String : String corresponding to the objectId from where the Applicable Change Tasks are looked for
	 * @return MapList - containing the List of the Delta Change Tasks Applicable for the context object
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211.HF3
	 */
	public MapList getDeltaApplicableChangeTasks(Context context, String objectId) throws Exception {
		try {
			MapList returnMapList = new MapList();

			if (objectId!=null && !objectId.isEmpty()) {
				//Get the Sigma of the objectId
				MapList currentSigmaApplicableChangeTasks = this.getSigmaApplicableChangeTasks(context, objectId);
				//Find the Change Tasks tagged as Added
				MapList addedChangeTasks = new MapList();
				Iterator<Map<String,String>> currentSigmaApplicableChangeTasksIterator = currentSigmaApplicableChangeTasks.iterator();
				while (currentSigmaApplicableChangeTasksIterator.hasNext()) {
					Map<String,String> currentSigmaApplicableChangeTask = currentSigmaApplicableChangeTasksIterator.next();
					if (currentSigmaApplicableChangeTask!=null && !currentSigmaApplicableChangeTask.isEmpty()) {
						String currentSigmaImpactingChangeTaskApplicableBy = currentSigmaApplicableChangeTask.get("applicableBy");
						if (currentSigmaImpactingChangeTaskApplicableBy!=null && currentSigmaImpactingChangeTaskApplicableBy.equalsIgnoreCase(ApplicableBy.Added.toString())) {
							currentSigmaApplicableChangeTask.remove("applicableBy");
							addedChangeTasks.add(currentSigmaApplicableChangeTask);
						}
					}
				}//End of while
				returnMapList.addAll(this.addApplicableByParameter(context, addedChangeTasks, ApplicableBy.Added));

				if (currentSigmaApplicableChangeTasks!=null) {
					String previousRevId = this.getPrevious(context, objectId);					
					if (previousRevId!=null && !previousRevId.isEmpty()) {
						//Get the Sigma of the previousRevId
						MapList previousSigmaApplicableChangeTasks = this.getSigmaApplicableChangeTasks(context, previousRevId);
						if (previousSigmaApplicableChangeTasks!=null && !previousSigmaApplicableChangeTasks.isEmpty()) {
							MapList removedChangeTasks = new MapList();
							//Do the compare
							//Find the Change Task in previousSigma which is no more in currentSigma
							Iterator<Map<String,String>> previousSigmaApplicableChangeTasksItr = previousSigmaApplicableChangeTasks.iterator();
							while (previousSigmaApplicableChangeTasksItr.hasNext()) {
								Map<String,String> previousSigmaApplicableChangeTask = previousSigmaApplicableChangeTasksItr.next();
								if (previousSigmaApplicableChangeTask!=null && !previousSigmaApplicableChangeTask.isEmpty()) {
									String previousSigmaApplicableChangeTaskId = previousSigmaApplicableChangeTask.get(DomainConstants.SELECT_ID);
									if (previousSigmaApplicableChangeTaskId!=null && !previousSigmaApplicableChangeTaskId.isEmpty()) {
										Iterator<Map<String,String>> currentSigmaApplicableChangeTasksItr = currentSigmaApplicableChangeTasks.iterator();
										Boolean isRemoved = true;
										while (currentSigmaApplicableChangeTasksItr.hasNext()) {
											Map<String,String> currentSigmaApplicableChangeTask = currentSigmaApplicableChangeTasksItr.next();
											if (currentSigmaApplicableChangeTask!=null && !currentSigmaApplicableChangeTask.isEmpty()) {
												String currentSigmaApplicableChangeTaskId = currentSigmaApplicableChangeTask.get(DomainConstants.SELECT_ID);
												if (currentSigmaApplicableChangeTaskId!=null && currentSigmaApplicableChangeTaskId.equalsIgnoreCase(previousSigmaApplicableChangeTaskId)) {
													isRemoved = false;
												}
											}
										}//End of while
										if (isRemoved) {
											previousSigmaApplicableChangeTask.remove("applicableBy");
											removedChangeTasks.add(previousSigmaApplicableChangeTask);
										}
									}
								}
							}//End of while
							returnMapList.addAll(this.addApplicableByParameter(context, removedChangeTasks, ApplicableBy.Removed));
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
	 * Method created for the Flow Down openness
	 *
	 * @param context the eMatrix Context object
	 * @param objectId String : String corresponding to the objectId from where the Applicable Change Tasks are looked for
	 * @return MapList - containing the List of the Sigma Change Tasks Applicable for the context object
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212.HFDerivations
	 */
	public MapList getSigmaApplicableChangeTasks(Context context, String[] args) throws Exception {
		try {
			MapList returnMapList = new MapList();
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String) programMap.get("objectId");
			if (objectId!=null && !objectId.isEmpty()) {
				returnMapList = this.getSigmaApplicableChangeTasks(context, objectId);
			}
			return returnMapList;
		} catch (Exception e) {
			throw e;
		}
	}
	

	/**
	 * Method to display the Sigma Applicable Change Tasks
	 *
	 * @param context the eMatrix Context object
	 * @param objectId String : String corresponding to the objectId from where the Applicable Change Tasks are looked for
	 * @return MapList - containing the List of the Sigma Change Tasks Applicable for the context object
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211.HF3
	 */
	public MapList getSigmaApplicableChangeTasks(Context context, String objectId) throws Exception {
		try {
			MapList returnMapList = new MapList();

			if (objectId!=null && !objectId.isEmpty()) {
				//Get the Change Discipline(s) defined for the objectId
				StringList membershipChangeDisciplines = EnterpriseChangeUtil.getObjectChangeDisciplinesAssociation(context, objectId);
				if (membershipChangeDisciplines!=null && !membershipChangeDisciplines.isEmpty()) {
					MapList relatedDecisions = new MapList();

					Boolean hasPrevious = true;
					String previousId = objectId;
					while (hasPrevious) {
						relatedDecisions.addAll(this.getRelatedDecisions(context, previousId));
						
						previousId = this.getPrevious(context, previousId);
						if (!(previousId!=null && !previousId.isEmpty())) {
							hasPrevious = false;
						}
					}//End of while

					StringList decisions = new StringList();
					Iterator<Map<String,String>> relatedDecisionsItr = relatedDecisions.iterator();
					while (relatedDecisionsItr.hasNext()) {
						Map<String,String> relatedDecision = relatedDecisionsItr.next();
						if (relatedDecision!=null && !relatedDecision.isEmpty()) {
							String relatedDecisionId = relatedDecision.get(DomainConstants.SELECT_ID);
							if (relatedDecisionId!=null && !relatedDecisionId.isEmpty()) {
								if (!decisions.contains(relatedDecisionId)) {
									decisions.addElement(relatedDecisionId);
								}
							}
						}
					}//End of while

					if (decisions!=null && !decisions.isEmpty()) {
						MapList applicableChangeTasks = new MapList();
						//if (EnterpriseChangeUtil.isDerivationEnabled(context)) {
							applicableChangeTasks.addAll(this.getDerivationsApplicableChangeTasks(context, objectId, decisions));
						//} else {
						//	applicableChangeTasks.addAll(this.getApplicableChangeTasks(context, objectId, decisions));
						//}
						Iterator<Map<String,String>> applicableChangeTasksItr = applicableChangeTasks.iterator();
						while (applicableChangeTasksItr.hasNext()) {
							Map<String,String> applicableChangeTask = applicableChangeTasksItr.next();
							if (applicableChangeTask!=null && !applicableChangeTask.isEmpty()) {
								String applicableChangeTaskId = applicableChangeTask.get(DomainConstants.SELECT_ID);
								String applicableChangeTaskDecisionId = applicableChangeTask.get("decision");
								if ((applicableChangeTaskId!=null && !applicableChangeTaskId.isEmpty()) && (applicableChangeTaskDecisionId!=null && !applicableChangeTaskDecisionId.isEmpty())) {
									ChangeTask changeTask = new ChangeTask(applicableChangeTaskId);
									Boolean isIncluded = false;
									Iterator<String> membershipChangeDisciplinesItr = membershipChangeDisciplines.iterator();
									while (membershipChangeDisciplinesItr.hasNext()) {
										String membershipChangeDiscipline = membershipChangeDisciplinesItr.next();
										if (membershipChangeDiscipline!=null && !membershipChangeDiscipline.isEmpty()) {
											if (changeTask.isLatestValidDecisionForChangeDiscipline(context, applicableChangeTaskDecisionId, membershipChangeDiscipline)) {
												isIncluded = true;
											}
										}
									}//End of while
									if (isIncluded) {
										returnMapList.add(applicableChangeTask);
									}
								}
							}
						}//End of while
					}
				}
			}
			return returnMapList;
		} catch (Exception e) {
			throw e;
		}
	}


	/**
	 * Get the valid Decision associated to the object through the "Applicable Item" relationship
	 * To retrieve only the "Valid" Decision, the system will filtered according to the setting emxEnterpriseChange.ValidDecision.Current
	 * According to the object Change Discipline membership, the system will take only Decisions which have Applicable Item compliant with the Change Disciplines
	 *
	 * @param context the eMatrix Context object
	 * @param objectId String : String corresponding to the objectId from where the Decisions are looked for
	 * @return MapList - containing the List of the valid Decision
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211.HF3
	 */
	public MapList getRelatedDecisions(Context context, String objectId) throws Exception {
		try {
			MapList returnMapList = new MapList();

			if (objectId!=null && !objectId.isEmpty()) {
				StringBuffer objectWhere = new StringBuffer();
				String validDecisionCurrentValues = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChange.ValidDecision.Current");
        		if (validDecisionCurrentValues!=null && validDecisionCurrentValues.contains(".")) {
        			StringList validDecisionCurrentValuesList = FrameworkUtil.split(validDecisionCurrentValues, ",");
        			Iterator<String> validDecisionCurrentValuesListItr = validDecisionCurrentValuesList.iterator();
        			while (validDecisionCurrentValuesListItr.hasNext()) {
        				String validDecisionCurrentValue = validDecisionCurrentValuesListItr.next();
        				if (validDecisionCurrentValue!=null && !validDecisionCurrentValue.isEmpty()) {
        					StringList validPolicyState = FrameworkUtil.split(validDecisionCurrentValue, ".");
        					if (validPolicyState!=null && validPolicyState.size()==2) {
        						String validPolicy = (String)validPolicyState.get(0);
        						String validState = (String)validPolicyState.get(1);

        						if (PropertyUtil.getSchemaProperty(context,validPolicy)!=null && !PropertyUtil.getSchemaProperty(context,validPolicy).isEmpty()) {
        							if (PropertyUtil.getSchemaProperty(context,DomainConstants.SELECT_POLICY, PropertyUtil.getSchemaProperty(context,validPolicy), validState)!=null && !PropertyUtil.getSchemaProperty(context,DomainConstants.SELECT_POLICY, PropertyUtil.getSchemaProperty(context,validPolicy), validState).isEmpty()) {
        								//Policy and state exist
        								if (objectWhere!=null && !objectWhere.toString().isEmpty()) {objectWhere.append(" || ");}
        								objectWhere.append("(");
        								objectWhere.append(DomainConstants.SELECT_POLICY);
        								objectWhere.append("==");
        								objectWhere.append(PropertyUtil.getSchemaProperty(context,validPolicy));
        								objectWhere.append(" && ");
        								objectWhere.append(DomainConstants.SELECT_CURRENT);
        								objectWhere.append("==");
        								objectWhere.append(PropertyUtil.getSchemaProperty(context,"policy", PropertyUtil.getSchemaProperty(context,validPolicy), validState));
        								objectWhere.append(")");
        							} else { throw new Exception(EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.ValidDecision.CurrentSetting.NotValid", context.getSession().getLanguage())); }
        						} else { throw new Exception(EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.ValidDecision.CurrentSetting.NotValid", context.getSession().getLanguage())); }
        					} else { throw new Exception(EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.ValidDecision.CurrentSetting.Missformed", context.getSession().getLanguage())); }
        				}
        			}//End of while

        			if (objectWhere!=null && !objectWhere.toString().isEmpty()) {

        				returnMapList = new DomainObject(objectId).getRelatedObjects(context,
        						EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM,
        						EnterpriseChangeConstants.TYPE_DECISION,
        						new StringList(DomainConstants.SELECT_ID),
        						new StringList(DomainConstants.SELECT_RELATIONSHIP_ID),
        						true, //to rel
        						false, //from rel
        						(short)1, //recurse
        						objectWhere.toString(), //objectWhere
        						DomainConstants.EMPTY_STRING, //relWhere
        						0);
        			}
        		}
			}
			return returnMapList;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Get Change Tasks linked to a Decision where its Applicability contains the objectId
	 *
	 * @param context the eMatrix Context object
	 * @param objectId String : String corresponding to the objectId to test if included in Decision applicability
	 * @param decisions StringList : containing the list of DecisionId to test
	 * @return MapList - containing the List of the Change Tasks
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211.HF3
	 * @deprecated : since R418 Use getDerivationsApplicableChangeTasks(matrix.db.Context,java.lang.String,matrix.util.StringList)
	 */
	public MapList getApplicableChangeTasks(Context context, String objectId, StringList decisions) throws Exception {
		try {
			MapList returnMapList = new MapList();
			if ((objectId!=null && !objectId.isEmpty()) && (decisions!=null && !decisions.isEmpty())) {
				String APPLICABILITY_INFINITY = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChange.Display.Infinity");
				DomainObject domObject = new DomainObject(objectId);
				StringList associatedChangeDisciplines = EnterpriseChangeUtil.getObjectChangeDisciplinesAssociation(context, objectId);
				//Get the master of the object Id
				Integer indexNumber = null;
				String targetMaster = "";
				String targetType = "";
				
				if (domObject.isKindOf(context, EnterpriseChangeConstants.TYPE_PRODUCTS)) {
					targetMaster = domObject.getInfo(context, "to[" + EnterpriseChangeConstants.RELATIONSHIP_PRODUCTS + "].from.id");
					indexNumber = Integer.parseInt(domObject.getInfo(context, "revindex"));
					targetType = EnterpriseChangeConstants.TYPE_PRODUCTS;
				} else if (domObject.isKindOf(context, EnterpriseChangeConstants.TYPE_MANUFACTURING_PLAN)) {
					targetMaster = domObject.getInfo(context, "to[" + EnterpriseChangeConstants.RELATIONSHIP_MANAGED_SERIES + "].from.from[" + EnterpriseChangeConstants.RELATIONSHIP_SERIES_MASTER + "].to.id");
					indexNumber = Integer.parseInt(domObject.getInfo(context, "revindex"));
					targetType = EnterpriseChangeConstants.TYPE_MANUFACTURING_PLAN;
				} else if (domObject.isKindOf(context, EnterpriseChangeConstants.TYPE_BUILDS)) {
					targetMaster = domObject.getInfo(context, "to[" + EnterpriseChangeConstants.RELATIONSHIP_MODEL_BUILD + "].from.id");
					indexNumber = Integer.parseInt(domObject.getAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_BUILD_UNIT_NUMBER));
					targetType = EnterpriseChangeConstants.TYPE_BUILDS;
				} else if (domObject.isKindOf(context, EnterpriseChangeConstants.TYPE_CONFIGURATION_FEATURES)) {

				}

				if ((targetMaster!=null && !targetMaster.isEmpty()) && (associatedChangeDisciplines!=null && !associatedChangeDisciplines.isEmpty()) && indexNumber!=null) {
					//Loop with the decisions list
					Iterator<String> decisionsItr = decisions.iterator();
					while (decisionsItr.hasNext()) {
						String decisionId = decisionsItr.next();
						if (decisionId!=null && !decisionId.isEmpty()) {
							Decision decision = new Decision(decisionId);
							Map<String,Map<String,Map<String,String>>> applicableItemsSortedByMastersAndDisciplinesAndTypes = decision.getApplicabilitySummaryWithIntervals(context, new StringList(targetMaster), associatedChangeDisciplines, new StringList(targetType));
							if (applicableItemsSortedByMastersAndDisciplinesAndTypes!=null && !applicableItemsSortedByMastersAndDisciplinesAndTypes.isEmpty()) {
								Boolean isIncluded = false;
								ApplicableBy applicableBy = null;
								//Check if we have the targetMaster
								Map<String,Map<String,String>> applicableItemsSortedByDisciplinesAndTypes = applicableItemsSortedByMastersAndDisciplinesAndTypes.get(targetMaster);
								StringList targetedChangeDisciplines = new StringList();
								if (applicableItemsSortedByDisciplinesAndTypes!=null && !applicableItemsSortedByDisciplinesAndTypes.isEmpty()) {
									//Check if we have the targetDiscipline
									Iterator<String> associatedChangeDisciplinesItr = associatedChangeDisciplines.iterator();
									while (associatedChangeDisciplinesItr.hasNext()) {
										String associatedChangeDiscipline = associatedChangeDisciplinesItr.next();
										if (associatedChangeDiscipline!=null && !associatedChangeDiscipline.isEmpty()) {
											Map<String,String> applicableItemsSortedByTypes = applicableItemsSortedByDisciplinesAndTypes.get(associatedChangeDiscipline);
											if (applicableItemsSortedByTypes!=null && !applicableItemsSortedByTypes.isEmpty()) {
												//Check if we have the targetType
												String intervals = applicableItemsSortedByTypes.get(targetType);
												if (intervals!=null && !intervals.isEmpty()) {
													StringList intervalsList = FrameworkUtil.split(intervals, ";");
													Iterator<String> intervalsListItr = intervalsList.iterator();
													while (intervalsListItr.hasNext()) {
														String interval = intervalsListItr.next();
														if (interval!=null && !interval.isEmpty()) {
															if (interval.contains("-")) {
																String lowerValue = interval.substring(0,interval.indexOf("-"));
																String upperValue = interval.substring(interval.lastIndexOf("-")+1);
																if ((lowerValue!=null && !lowerValue.isEmpty()) && (upperValue!=null && !upperValue.isEmpty())) {
																	DomainObject lowerDom = new DomainObject(lowerValue);
																	Integer lowerDomvalue = null;
																	if (lowerDom.isKindOf(context, EnterpriseChangeConstants.TYPE_PRODUCTS) || lowerDom.isKindOf(context, EnterpriseChangeConstants.TYPE_MANUFACTURING_PLAN)) {
																		lowerDomvalue = Integer.parseInt(lowerDom.getInfo(context, "revindex"));
																	} else if (lowerDom.isKindOf(context, EnterpriseChangeConstants.TYPE_BUILDS)) {
																		lowerDomvalue = Integer.parseInt(lowerDom.getAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_BUILD_UNIT_NUMBER));
																	}
																	if (lowerDomvalue <= indexNumber) {
																		if (lowerDomvalue == indexNumber) {
																			isIncluded = true;
																			applicableBy = ApplicableBy.Added;
																		} else {
																			if (upperValue.equalsIgnoreCase(APPLICABILITY_INFINITY)) {
																				isIncluded = true;
																				applicableBy = ApplicableBy.Inherited;
																			} else {
																				DomainObject upperDom = new DomainObject(upperValue);
																				Integer upperDomValue = null;
																				if (upperDom.isKindOf(context, EnterpriseChangeConstants.TYPE_PRODUCTS) || upperDom.isKindOf(context, EnterpriseChangeConstants.TYPE_MANUFACTURING_PLAN)) {
																					upperDomValue = Integer.parseInt(upperDom.getInfo(context, "revindex"));
																				} else if (upperDom.isKindOf(context, EnterpriseChangeConstants.TYPE_BUILDS)) {
																					upperDomValue = Integer.parseInt(upperDom.getAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_BUILD_UNIT_NUMBER));
																				}
																				if (upperDomValue >= indexNumber) {
																					isIncluded = true;
																					applicableBy = ApplicableBy.Inherited;
																				}
																			}
																		}
																	}
																}
															} else {
																DomainObject intervalDom = new DomainObject(interval);
																Integer intervalDomValue = null;
																if (intervalDom.isKindOf(context, EnterpriseChangeConstants.TYPE_PRODUCTS) || intervalDom.isKindOf(context, EnterpriseChangeConstants.TYPE_MANUFACTURING_PLAN)) {
																	intervalDomValue = Integer.parseInt(intervalDom.getInfo(context, "revindex"));
																} else if (intervalDom.isKindOf(context, EnterpriseChangeConstants.TYPE_BUILDS)) {
																	intervalDomValue = Integer.parseInt(intervalDom.getAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_BUILD_UNIT_NUMBER));
																}
																if (intervalDomValue == indexNumber) {
																	isIncluded = true;
																	applicableBy = ApplicableBy.Added;
																}
															}
														}
													}//End of while
												}
											}
										}
										if (isIncluded) {
											targetedChangeDisciplines.addElement(associatedChangeDiscipline);
										}
									}//End of while associatedChangeDisciplines
								}
								if(isIncluded && (targetedChangeDisciplines!=null && !targetedChangeDisciplines.isEmpty())) {
									StringBuffer objectWhere = new StringBuffer();
									Iterator<String> changeDisciplinesItr = targetedChangeDisciplines.iterator();
									while (changeDisciplinesItr.hasNext()) {
										String changeDiscipline = changeDisciplinesItr.next();
										if (changeDiscipline!=null && !changeDiscipline.isEmpty()) {
											if (objectWhere!=null && !objectWhere.toString().isEmpty()) {
												objectWhere.append(" || ");
											}
											objectWhere.append("attribute[");
											objectWhere.append(changeDiscipline);
											objectWhere.append("]");
											objectWhere.append("==");
											objectWhere.append(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE);
										}
									}//End of while

									MapList relatedChangeTasks = decision.getRelatedObjects(context,
											EnterpriseChangeConstants.RELATIONSHIP_DECISION_APPLIES_TO,
											EnterpriseChangeConstants.TYPE_CHANGE_TASK,
											new StringList(DomainConstants.SELECT_ID),
											new StringList(DomainConstants.SELECT_RELATIONSHIP_ID),
											false, //to rel
											true, //from rel
											(short)1, //recurse
											objectWhere.toString(), //objectWhere
											null, //relWhere
											0);

									Iterator<Map<String,String>> relatedChangeTasksItr = relatedChangeTasks.iterator();
									while (relatedChangeTasksItr.hasNext()) {
										Map<String,String> relatedChangeTask = relatedChangeTasksItr.next();
										if (relatedChangeTask!=null && !relatedChangeTask.isEmpty()) {
											relatedChangeTask.put("decision", decision.getInfo(context,DomainConstants.SELECT_ID));
											relatedChangeTask.put("applicableBy", applicableBy.toString());
											returnMapList.add(relatedChangeTask);
										}
									}//End of while
								}
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
	 * Get Change Tasks linked to a Decision where its Applicability contains the objectId
	 *
	 * @param context the eMatrix Context object
	 * @param objectId String : String corresponding to the objectId to test if included in Decision applicability
	 * @param decisions StringList : containing the list of DecisionId to test
	 * @return MapList - containing the List of the Change Tasks
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212_HFDerivations
	 */
	public MapList getDerivationsApplicableChangeTasks(Context context, String objectId, StringList decisions) throws Exception {
		try {
			MapList returnMapList = new MapList();
			if ((objectId!=null && !objectId.isEmpty()) && (decisions!=null && !decisions.isEmpty())) {
				DomainObject domObject = new DomainObject(objectId);
				StringList associatedChangeDisciplines = EnterpriseChangeUtil.getObjectChangeDisciplinesAssociation(context, objectId);
				//Get the master of the object Id
				String targetMaster = "";
				String targetType = "";
				StringList previousList = this.getPreviousList(context, objectId);
				if (domObject.isKindOf(context, EnterpriseChangeConstants.TYPE_PRODUCTS)) {
					targetMaster = domObject.getInfo(context, "to[" + EnterpriseChangeConstants.RELATIONSHIP_PRODUCTS + "].from.id");
					if (!(targetMaster!=null && !targetMaster.isEmpty())) {
						targetMaster = domObject.getInfo(context, "to[" + EnterpriseChangeConstants.RELATIONSHIP_MAIN_PRODUCT + "].from.id");
					}
					targetType = EnterpriseChangeConstants.TYPE_PRODUCTS;
				} else if (domObject.isKindOf(context, EnterpriseChangeConstants.TYPE_MANUFACTURING_PLAN)) {
					targetMaster = domObject.getInfo(context, "to[" + EnterpriseChangeConstants.RELATIONSHIP_MANAGED_SERIES + "].from.from[" + EnterpriseChangeConstants.RELATIONSHIP_SERIES_MASTER + "].to.id");
					if (!(targetMaster!=null && !targetMaster.isEmpty())) {
						targetMaster = domObject.getInfo(context, "to[" + EnterpriseChangeConstants.RELATIONSHIP_MANAGED_ROOT + "].from.from[" + EnterpriseChangeConstants.RELATIONSHIP_SERIES_MASTER + "].to.id");
					}
					targetType = EnterpriseChangeConstants.TYPE_MANUFACTURING_PLAN;
				} else if (domObject.isKindOf(context, EnterpriseChangeConstants.TYPE_BUILDS)) {
					targetMaster = domObject.getInfo(context, "to[" + EnterpriseChangeConstants.RELATIONSHIP_MODEL_BUILD + "].from.id");
					targetType = EnterpriseChangeConstants.TYPE_BUILDS;
				} else if (domObject.isKindOf(context, EnterpriseChangeConstants.TYPE_CONFIGURATION_FEATURES)) {

				}

				if ((targetMaster!=null && !targetMaster.isEmpty()) && (associatedChangeDisciplines!=null && !associatedChangeDisciplines.isEmpty())) {
					//Loop with the decisions list
					Iterator<String> decisionsItr = decisions.iterator();
					while (decisionsItr.hasNext()) {
						String decisionId = decisionsItr.next();
						if (decisionId!=null && !decisionId.isEmpty()) {
							Decision decision = new Decision(decisionId);
							Map<String,Map<String,Map<String,MapList>>> applicableItemsSortedByMastersAndDisciplinesAndTypes = decision.getApplicabilitySummary(context, new StringList(targetMaster), associatedChangeDisciplines, new StringList(targetType));
							if (applicableItemsSortedByMastersAndDisciplinesAndTypes!=null && !applicableItemsSortedByMastersAndDisciplinesAndTypes.isEmpty()) {
								Boolean isIncluded = false;
								ApplicableBy applicableBy = null;
								//Check if we have the targetMaster
								Map<String,Map<String,MapList>> applicableItemsSortedByDisciplinesAndTypes = applicableItemsSortedByMastersAndDisciplinesAndTypes.get(targetMaster);
								StringList targetedChangeDisciplines = new StringList();
								if (applicableItemsSortedByDisciplinesAndTypes!=null && !applicableItemsSortedByDisciplinesAndTypes.isEmpty()) {
									//Check if we have the targetDiscipline
									Iterator<String> associatedChangeDisciplinesItr = associatedChangeDisciplines.iterator();
									while (associatedChangeDisciplinesItr.hasNext()) {
										String associatedChangeDiscipline = associatedChangeDisciplinesItr.next();
										if (associatedChangeDiscipline!=null && !associatedChangeDiscipline.isEmpty()) {
											Map<String,MapList> applicableItemsSortedByTypes = applicableItemsSortedByDisciplinesAndTypes.get(associatedChangeDiscipline);
											if (applicableItemsSortedByTypes!=null && !applicableItemsSortedByTypes.isEmpty()) {
												//Check if we have the targetType
												MapList applicableItems = applicableItemsSortedByTypes.get(targetType);
												if (applicableItems!=null && !applicableItems.isEmpty()) {
													StringList applicableItemsList = new StringList();
													//Here we have the Applicable Items and the Previous List
													//Always And Subsequent
													//From Applicable Item create a StringList
													Iterator<Map<String,String>> applicableItemsItr = applicableItems.iterator();
													while (applicableItemsItr.hasNext()) {
														Map<String,String> applicableItem = applicableItemsItr.next();
														if (applicableItem!=null && !applicableItem.isEmpty()) {
															String applicableItemId = applicableItem.get(DomainConstants.SELECT_ID);
															if (applicableItemId!=null && !applicableItemId.isEmpty()) {
																applicableItemsList.addElement(applicableItemId);
															}
														}
													}//End of while applicableItems
													
													if (applicableItemsList.contains(objectId)) {
														isIncluded = true;
														applicableBy = ApplicableBy.Added;
													}
													Iterator<String> previousListItr = previousList.iterator();
													while(previousListItr.hasNext()) {
														String previousId = previousListItr.next();
														if (previousId!=null && applicableItemsList.contains(previousId)) {
															isIncluded = true;
															applicableBy = ApplicableBy.Inherited;
														}
													}
												}
											}
										}
										if (isIncluded) {
											targetedChangeDisciplines.addElement(associatedChangeDiscipline);
										}
									}//End of while associatedChangeDisciplines
								}
								if(isIncluded && (targetedChangeDisciplines!=null && !targetedChangeDisciplines.isEmpty())) {
									StringBuffer objectWhere = new StringBuffer();
									Iterator<String> changeDisciplinesItr = targetedChangeDisciplines.iterator();
									while (changeDisciplinesItr.hasNext()) {
										String changeDiscipline = changeDisciplinesItr.next();
										if (changeDiscipline!=null && !changeDiscipline.isEmpty()) {
											if (objectWhere!=null && !objectWhere.toString().isEmpty()) {
												objectWhere.append(" || ");
											}
											objectWhere.append("attribute[");
											objectWhere.append(changeDiscipline);
											objectWhere.append("]");
											objectWhere.append("==");
											objectWhere.append(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE);
										}
									}//End of while

									MapList relatedChangeTasks = decision.getRelatedObjects(context,
											EnterpriseChangeConstants.RELATIONSHIP_DECISION_APPLIES_TO,
											EnterpriseChangeConstants.TYPE_CHANGE_TASK,
											new StringList(DomainConstants.SELECT_ID),
											new StringList(DomainConstants.SELECT_RELATIONSHIP_ID),
											false, //to rel
											true, //from rel
											(short)1, //recurse
											objectWhere.toString(), //objectWhere
											null, //relWhere
											0);

									Iterator<Map<String,String>> relatedChangeTasksItr = relatedChangeTasks.iterator();
									while (relatedChangeTasksItr.hasNext()) {
										Map<String,String> relatedChangeTask = relatedChangeTasksItr.next();
										if (relatedChangeTask!=null && !relatedChangeTask.isEmpty()) {
											relatedChangeTask.put("decision", decision.getInfo(context,DomainConstants.SELECT_ID));
											relatedChangeTask.put("applicableBy", applicableBy.toString());
											returnMapList.add(relatedChangeTask);
										}
									}//End of while
								}
							}
						}
					}//End of while Decisions
				}
			}
			return returnMapList;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Add the ApplicableBy parameter to each Map of the Maplist
	 *
	 * @param context the eMatrix Context object
	 * @param referenceList MapList : containing the Maps where the parameter will be added
	 * @param applicableBy ApplicableBy : the value of the parameter to add
	 * @return MapList - containing the List of modified Maps
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211.HF3
	 */
	private MapList addApplicableByParameter (Context context, MapList referenceList, ApplicableBy applicableBy) throws Exception {
		try {
			MapList returnMapList = new MapList();
			if (applicableBy!=null) {
				Iterator<Map<String,String>> referenceListItr = referenceList.iterator();
				while (referenceListItr.hasNext()) {
					Map<String,String> reference = referenceListItr.next();
					if (reference!=null && !reference.isEmpty()) {
						reference.put("applicableBy", applicableBy.toString());
						returnMapList.add(reference);
					}
				}
			}
			return returnMapList;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Check if the Delta From Filter combobox is displayed or not.
	 * If selected mode is Sigma, then Delta From Filter is not displayed
	 * If selected mode is delta, then Delta From Filter is displayed
	 *
	 * @param context the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return Boolean - true if displayed, false if not displayed
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211.HF3
	 */
	public Boolean isDeltaFromFilterDisplayed(Context context, String[] args) throws Exception {
		try {
			Boolean returnBoolean = false;

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			//Get filter parameters
			String modeFilterValue = (String)programMap.get(EnterpriseChangeConstants.COMMAND_APPLICABLE_CHANGE_TASKS_MODE_FILTER);
			if (modeFilterValue!=null && !modeFilterValue.isEmpty()) {
				switch (ModeFilter.valueOf(modeFilterValue)) {
					case Delta: returnBoolean = true;break;
				}
			}
			return returnBoolean;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Get the Range values of the Mode Filter combobox
	 *
	 * @param context the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return Map - containing the range values
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211.HF3
	 */
	public Map<String,StringList> getModeFilterRange(Context context, String[] args) throws Exception {
		try {
			Map<String,StringList> returnMap = new HashMap<String,StringList>();

			StringList slOriginalList = new StringList();
			StringList slDisplayList = new StringList();

			slOriginalList.addElement(DomainConstants.EMPTY_STRING);
			slDisplayList.addElement(DomainConstants.EMPTY_STRING);

			ModeFilter[] modeFilter = ModeFilter.values();
			for (int i=0;i<modeFilter.length;i++) {
				slOriginalList.addElement(modeFilter[i].toString());
				slDisplayList.addElement(EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Filter.Mode." + modeFilter[i].toString(),context.getSession().getLanguage()));
			}

			returnMap.put("field_choices",slOriginalList);
			returnMap.put("field_display_choices",slDisplayList);

			return returnMap;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Get the Range values of the Delta From Filter combobox
	 *
	 * @param context the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return Map - containing the range values
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211.HF3
	 */
	public Map<String,StringList> getDeltaFromFilterRange(Context context, String[] args) throws Exception {
		try {
			Map<String,StringList> returnMap = new HashMap<String,StringList>();

			StringList slOriginalList = new StringList();
			StringList slDisplayList = new StringList();
						
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap requestMap =  (HashMap)programMap.get("requestMap");
			String objectId = (String) requestMap.get("objectId");

			if (objectId!=null && !objectId.isEmpty()) {
				Boolean hasPrevious = true;
				String previousId = objectId;
				while (hasPrevious) {
					previousId = this.getPrevious(context, previousId);
					if (previousId!=null && !previousId.isEmpty()) {
						DomainObject previousDom = new DomainObject(previousId);
						if (previousDom.isKindOf(context, EnterpriseChangeConstants.TYPE_PRODUCTS) || previousDom.isKindOf(context, EnterpriseChangeConstants.TYPE_MANUFACTURING_PLAN)) {
							slOriginalList.addElement(previousId);
							slDisplayList.addElement(previousDom.getInfo(context, DomainConstants.SELECT_NAME) + " " + previousDom.getInfo(context, DomainConstants.SELECT_REVISION));
						} else if (previousDom.isKindOf(context, EnterpriseChangeConstants.TYPE_BUILDS)) {
							slOriginalList.addElement(previousId);
							slDisplayList.addElement(previousDom.getInfo(context, DomainConstants.SELECT_NAME));
						}
					} else {
						hasPrevious = false;
					}
					//Break the loop as we want to display only the previous revision
					//when we will display all previous rev just need to remove the flag
					hasPrevious = false;
				}//End of while
			}

			if (!(slDisplayList!=null && !slDisplayList.isEmpty())) {
				slOriginalList.add(EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.ApplicableChangeTasks.NoPreviousRev", context.getSession().getLanguage()));
				slDisplayList.add(EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.ApplicableChangeTasks.NoPreviousRev", context.getSession().getLanguage()));
			}

			returnMap.put("field_choices",slOriginalList);
			returnMap.put("field_display_choices",slDisplayList);

			return returnMap;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Get the proper icon to display in the table according to the ApplicableBy value
	 *
	 * @param context the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return Vector - containing the proper icon for each rows of the table
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211.HF3
	 */
	public Vector<String> getApplicableByIcon(Context context, String[] args) throws Exception {
		try {
			Vector<String> returnVector = new Vector<String>();

			String iconAdded = "iconStatusAdded.gif";
			String iconInherited = "iconStatusResequenced.gif";
			String iconRemoved = "iconStatusRemoved.gif";

			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");

			Iterator<Map<String,String>> objectListItr = objectList.iterator();
			while (objectListItr.hasNext()) {
				StringBuffer strBuffer = new StringBuffer();
				String toolTip = new String();
				Map<String,String> objectMap = objectListItr.next();
				if (objectMap!=null && !objectMap.isEmpty()) {
					String applicableByValue = objectMap.get("applicableBy");
					if (applicableByValue!=null && !applicableByValue.isEmpty()) {
						ApplicableBy applicableBy = ApplicableBy.valueOf(applicableByValue);
						switch (applicableBy) {
							case Added:	toolTip = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Common.ApplicableBy.Added", context.getSession().getLanguage());
										strBuffer.append("<center><img src=\"../common/images/" + XSSUtil.encodeForHTMLAttribute(context,iconAdded) + "\" border=\"0\"" + " alt=\"" + XSSUtil.encodeForHTMLAttribute(context,toolTip) + "\" title=\"" + XSSUtil.encodeForHTMLAttribute(context,toolTip) + "\" /></center>");
										break;
							case Inherited:	toolTip = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Common.ApplicableBy.Inherited", context.getSession().getLanguage());
											strBuffer.append("<center><img src=\"../common/images/" + XSSUtil.encodeForHTMLAttribute(context,iconInherited) + "\" border=\"0\"" + " alt=\"" + XSSUtil.encodeForHTMLAttribute(context,toolTip) + "\" title=\"" + XSSUtil.encodeForHTMLAttribute(context,toolTip) + "\" /></center>");
											break;
							case Removed:	toolTip = EnoviaResourceBundle.getProperty(context,"EnterpriseChange","emxEnterpriseChange.Common.ApplicableBy.Removed", context.getSession().getLanguage());
											strBuffer.append("<center><img src=\"../common/images/" + XSSUtil.encodeForHTMLAttribute(context,iconRemoved) + "\" border=\"0\"" + " alt=\"" + XSSUtil.encodeForHTMLAttribute(context,toolTip) + "\" title=\"" + XSSUtil.encodeForHTMLAttribute(context,toolTip) + "\" /></center>");
											break;
							default: strBuffer.append("");break;
						}
					}
				}
				returnVector.add(strBuffer.toString());
			}//End of while
			return returnVector;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Get the Decision associated to the Change Task to display in the table
	 *
	 * @param context the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return Vector - containing the proper Decision for each rows of the table
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211.HF3
	 */
	public Vector getApplicableChangeTasksDecision(Context context, String[] args) throws Exception {
		try {
			Vector returnVector = new Vector();

			String prefixItemUrl = "<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=";
			String suffixItemUrl = "', '930', '650', 'false', 'popup', '')\" class=\"object\">";
			String anchorEnd = "</a>";

			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");

			Iterator<Map<String,String>> objectListItr = objectList.iterator();
			while (objectListItr.hasNext()) {
				StringBuffer strBuffer = new StringBuffer();
				String toolTip = new String();
				Map<String,String> objectMap = objectListItr.next();
				if (objectMap!=null && !objectMap.isEmpty()) {
					String decisionId = objectMap.get("decision");
					if (decisionId!=null && !decisionId.isEmpty()) {
						Decision decision = new Decision(decisionId);
						strBuffer.append("<img src=\"../common/images/");
						strBuffer.append(EnoviaResourceBundle.getProperty(context,"emxFramework.smallIcon." + FrameworkUtil.getAliasForAdmin(context, "Type", decision.getInfo(context, DomainConstants.SELECT_TYPE), true)));
						strBuffer.append("\" border=\"0\" />");
						strBuffer.append(prefixItemUrl);
						strBuffer.append(XSSUtil.encodeForHTMLAttribute(context,decisionId));
						strBuffer.append(suffixItemUrl);
						strBuffer.append(XSSUtil.encodeForHTML(context,decision.getInfo(context, DomainConstants.SELECT_NAME)));
						strBuffer.append(anchorEnd);
					} else {strBuffer.append("");}
				} else {strBuffer.append("");}
				returnVector.add(strBuffer.toString());
			}//End of while
			return returnVector;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Get the Decision state associated to the Change Task to display in the table
	 *
	 * @param context the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return StringList - containing the proper Decision state for each rows of the table
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211.HF3
	 */
	public StringList getApplicableChangeTasksDecisionState(Context context, String[] args) throws Exception {
		try {
			StringList returnStringList = new StringList();

			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");

			Iterator<Map<String,String>> objectListItr = objectList.iterator();
			while (objectListItr.hasNext()) {
				StringBuffer strBuffer = new StringBuffer();
				String toolTip = new String();
				Map<String,String> objectMap = objectListItr.next();
				if (objectMap!=null && !objectMap.isEmpty()) {
					String decisionId = objectMap.get("decision");
					if (decisionId!=null && !decisionId.isEmpty()) {
						Decision decision = new Decision(decisionId);
						returnStringList.addElement(decision.getInfo(context, DomainConstants.SELECT_CURRENT));
					} else {returnStringList.addElement("");}
				} else {returnStringList.addElement("");}
			}//End of while
			return returnStringList;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Expand a Change Task displayed in the table to retrieve its sub Change Tasks or Dependent Change Tasks
	 *
	 * @param context the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return MapList - containing the sub Change Tasks or Dependent Change Tasks
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211.HF3
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList expandApplicableChangeTasks(Context context, String[] args) throws Exception {
		try {
			MapList returnMapList = new MapList();

			Map programMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String)programMap.get("objectId");

			if (objectId!=null && !objectId.isEmpty()) {
				returnMapList = new DomainObject(objectId).getRelatedObjects(context,
						EnterpriseChangeConstants.RELATIONSHIP_SUBTASK + "," + EnterpriseChangeConstants.RELATIONSHIP_DEPENDENCY,
						EnterpriseChangeConstants.TYPE_CHANGE_TASK,
						new StringList(DomainConstants.SELECT_ID),
						new StringList(DomainConstants.SELECT_RELATIONSHIP_ID),
						false, //to rel
						true, //from rel
						(short)1, //recurse
						DomainConstants.EMPTY_STRING, //objectWhere
						DomainConstants.EMPTY_STRING, //relWhere
						0);
			}

			return returnMapList;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * Get the Previous object of Product, Manufacturing Plan or Builds
	 *
	 * @param context the eMatrix Context object
	 * @param objectId String : String corresponding to the objectId which Previous Object is looked for
	 * @param decisions StringList : containing the list of DecisionId to test
	 * @return String - corresponding to the Previous Object
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212_HFDerivations
	 */
	private String getPrevious(Context context, String objectId) throws Exception {
		try {
			String returnString = "";
			if (objectId!=null && !objectId.isEmpty()) {
				DomainObject domObject = new DomainObject(objectId);
				if (domObject.isKindOf(context, EnterpriseChangeConstants.TYPE_PRODUCTS) || domObject.isKindOf(context, EnterpriseChangeConstants.TYPE_MANUFACTURING_PLAN)) {
					StringList selects = new StringList();
					selects.addElement("to[" + EnterpriseChangeConstants.RELATIONSHIP_DERIVED + "].from.id");
					selects.addElement("to[" + EnterpriseChangeConstants.RELATIONSHIP_MAIN_DERIVED + "].from.id");
					Map<String,String> derivedIds = domObject.getInfo(context, selects);
					
					if (derivedIds!=null && !derivedIds.isEmpty()) {
						if (derivedIds.containsKey("to[" + EnterpriseChangeConstants.RELATIONSHIP_DERIVED + "].from.id")){
							returnString = derivedIds.get("to[" + EnterpriseChangeConstants.RELATIONSHIP_DERIVED + "].from.id");
						} else if (derivedIds.containsKey("to[" + EnterpriseChangeConstants.RELATIONSHIP_MAIN_DERIVED + "].from.id")){
							returnString = derivedIds.get("to[" + EnterpriseChangeConstants.RELATIONSHIP_MAIN_DERIVED + "].from.id");
						}
					}
				} else if (domObject.isKindOf(context, EnterpriseChangeConstants.TYPE_BUILDS)) {
					String buildUnitNumber = domObject.getAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_BUILD_UNIT_NUMBER);
					if (buildUnitNumber!=null && !buildUnitNumber.isEmpty()) {
						//returnString = MqlUtil.mqlCommand(context, "print bus " + objectId + " select to[" + EnterpriseChangeConstants.RELATIONSHIP_MODEL_BUILD + "].from.from[" + EnterpriseChangeConstants.RELATIONSHIP_MODEL_BUILD +"|to.attribute[" + EnterpriseChangeConstants.ATTRIBUTE_BUILD_UNIT_NUMBER + "]==" + String.valueOf(Integer.parseInt(buildUnitNumber)-1) + "].to.id dump");
						String strString = "to[" + EnterpriseChangeConstants.RELATIONSHIP_MODEL_BUILD + "].from.from[" + EnterpriseChangeConstants.RELATIONSHIP_MODEL_BUILD +"|to.attribute[" + EnterpriseChangeConstants.ATTRIBUTE_BUILD_UNIT_NUMBER + "]==" + String.valueOf(Integer.parseInt(buildUnitNumber)-1) + "].to.id";
						String strMQL = "print bus $1 select $2 dump";
						returnString = MqlUtil.mqlCommand(context, strMQL, objectId, strString);
					}
				}
			}
			return returnString;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * Get all Previous objects of Product, Manufacturing Plan or Builds
	 *
	 * @param context the eMatrix Context object
	 * @param objectId String : String corresponding to the objectId which Previous Object is looked for
	 * @param decisions StringList : containing the list of DecisionId to test
	 * @return StringList - corresponding to the list of Previous Objects
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212_HFDerivations
	 */
	private StringList getPreviousList(Context context, String objectId) throws Exception {
		try {
			StringList returnStringList = new StringList();
			if (objectId!=null && !objectId.isEmpty()) {
				boolean hasPrevious = true;
				String previousId = objectId;
				while (hasPrevious) {
					previousId = this.getPrevious(context, previousId);
					if (previousId!=null && !previousId.isEmpty()) {
						returnStringList.addElement(previousId);
					} else {
						hasPrevious = false;
					}
				}
			}
			return returnStringList;
		} catch (Exception e) {
			throw e;
		}
	}

}


