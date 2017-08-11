/*
 *  emxECHDecisionUtilBase.java
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.enterprisechange.Decision;
import com.matrixone.apps.enterprisechange.EnterpriseChangeConstants;
import com.matrixone.apps.enterprisechange.EnterpriseChangeUtil;

/**
 * The <code>emxECHDecisionUtilBase</code> class contains methods related to Decision admin type.
 * @version EnterpriseChange R212 - Copyright (c) 2008-2016, Dassault Systemes, Inc.
 *
 */
public class emxECHDecisionUtilBase_mxJPO extends emxDecision_mxJPO{
	public static final String SUITE_KEY ="EnterpriseChange";
	/**
	 * Default Constructor.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212
	 * @grade 0
	 */
	public emxECHDecisionUtilBase_mxJPO (Context context, String[] args) throws Exception{
		super(context, args);
	}

	/**
	 * Main entry point.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return an integer status code (0 = success)
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212
	 * @grade 0
	 */
	public int mxMain(Context context, String[] args) throws Exception{
		if(!context.isConnected()){
			String language = context.getSession().getLanguage();
			String strContentLabel = EnoviaResourceBundle.getProperty(context,"Components", "emxComponents.Error.UnsupportedClient",language);
			throw new Exception(strContentLabel);
		}
		return 0;
	}

	/**
	 * Method to define if the Applicability Summary field should be displayed on the Decision Properties
	 *
	 * @param context the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return Boolean - true if field should be displayed and false if not
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212
	 */
	public boolean showApplicabilitySummary(Context context, String[] args) throws Exception {
		try {
			Boolean returnBoolean = false;
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String objectId = (String)programMap.get("objectId");
		//	if (EnterpriseChangeUtil.isApplicabilityManagementEnabled(context)) {
				if (objectId!=null && !objectId.isEmpty()) {
					DomainObject domObject = new DomainObject(objectId);
					String trackApplicability = domObject.getAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_TRACK_APPLICABILITY);
					if (trackApplicability!=null && trackApplicability.equalsIgnoreCase("Yes")) {
						returnBoolean = true;
					}
				}
			//}
			return returnBoolean;
		} catch(Exception e){
			throw(e);
		}
	}



	/**
	 *  Method to display the Decision Applicability Summary in Decision properties
	 * @param context the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId (corresponds to the DecisionId)
	 * @return String - a HTML formatted string to properly display the Decision Applicability Summary
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212
	 */
	public String getDecisionApplicabilitySummaryHTML(Context context, String[] args) throws Exception{
		try{
			String returnString = "";
			//if (EnterpriseChangeUtil.isDerivationEnabled(context)) {
				returnString = getDecisionApplicabilityDerivationsSummary(context, args);
			//} else {
			//	returnString = getNewDecisionApplicabilitySummary(context, args);
			//}
			return returnString;
		}catch(Exception e){
			throw e;
		}
	}

	/**
	 * @deprecated : Use Decision.getApplicabilitySummary(Context context, StringList selectedChangeDisciplines)
	 * Method to get the Decision Applicability Summary
	 *
	 * @param context the eMatrix Context object
	 * @param decisionId - corresponds to the DecisionId
	 * @return String - a string containing the Decision Applicability Summary
	 * 		String format - masterId1:value01-value02;value03-value04|masterId2:value05-value06;value07-value08,...
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212
	 */
	public static String getDecisionApplicabilitySummary(Context context, String decisionId) throws Exception{
		try{
			String returnString = "";
			if(decisionId!=null && !decisionId.isEmpty()){
				returnString = new Decision(decisionId).getApplicabilitySummary(context);
			}
			return returnString;
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * Method to display the New Decision Applicability Summary in Decision properties
	 *
	 * @param context the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId (corresponds to the DecisionId)
	 * @return String - a new HTML formatted string to properly display the Decision Applicability Summary
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212_HF3
	 * @deprecated : since R418 getDecisionApplicabilitySummaryHTML
	 */
	public String getNewDecisionApplicabilitySummaryHTML(Context context, String[] args) throws Exception {
		try {
			String returnString = "";
			//if (EnterpriseChangeUtil.isDerivationEnabled(context)) {
				returnString = getDecisionApplicabilityDerivationsSummary(context, args);
			//} else {
			//	returnString = getNewDecisionApplicabilitySummary(context, args);
			//}
			return returnString;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * Method to display the New Decision Applicability Summary in Decision properties if Derivation is not enabled
	 *
	 * @param context the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId (corresponds to the DecisionId)
	 * @return String - a new HTML formatted string to properly display the Decision Applicability Summary
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212_HFDerivations
	 * @deprecated : since R418 Use getDecisionApplicabilityDerivationsSummary(matrix.db.Context,java.lang.String[])
	 */
	private String getNewDecisionApplicabilitySummary(Context context, String[] args) throws Exception {
		try {
			String returnString = "";
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");
			String decisionId = (String)paramMap.get("objectId");
			String languageStr = (String)paramMap.get("languageStr");
			if (decisionId!=null && !decisionId.isEmpty()) {
				String APPLICABILITY_INFINITY = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChange.Display.Infinity");
				Decision decision = new Decision(decisionId);
				String dottedBorders = "border-bottom:1px dotted #959595;";
				StringList disciplineKeys = EnterpriseChangeUtil.getChangeDisciplines(context);
				Map<String,Map<String,Map<String,String>>> applicableItemsSortedByMastersAndDisciplinesAndTypes = decision.getApplicabilitySummaryWithIntervals(context, null, null, null);
				if (applicableItemsSortedByMastersAndDisciplinesAndTypes!=null && !applicableItemsSortedByMastersAndDisciplinesAndTypes.isEmpty()) {
					StringBuffer strBuffer = new StringBuffer();
					strBuffer.append("<table style=\"width:100%;\">");
					strBuffer.append("<tr>");
					strBuffer.append("<th style=\"" + dottedBorders + "\">");
					strBuffer.append("<b>" + i18nNow.getTypeI18NString(EnterpriseChangeConstants.TYPE_MODEL,languageStr) + "</b>");
					strBuffer.append("</th>");
					Iterator<String> disciplineKeysItr = disciplineKeys.iterator();
					while (disciplineKeysItr.hasNext()) {
						String disciplineKey = disciplineKeysItr.next();
						strBuffer.append("<th style=\"" + dottedBorders + "\">");
						strBuffer.append("<b>" + i18nNow.getAttributeI18NString(disciplineKey,languageStr) + "</b>");
						strBuffer.append("</th>");
					}
					strBuffer.append("</tr>");
					Set<String> masterKeys = applicableItemsSortedByMastersAndDisciplinesAndTypes.keySet();
					Integer modelWidth = 10;
					Integer columnWidth = (100-modelWidth)/disciplineKeys.size();
					Iterator<String> masterKeysItr = masterKeys.iterator();
					while(masterKeysItr.hasNext()){
						String masterKey = masterKeysItr.next();
						if (masterKey!=null && !masterKey.isEmpty()) {
							DomainObject masterDom = new DomainObject(masterKey);
							strBuffer.append("<tr>");
							if (masterKeysItr.hasNext()) {
								strBuffer.append("<td style=\"width:" + modelWidth + "%;" + dottedBorders + "\">");
							} else {
								strBuffer.append("<td style=\"width:" + modelWidth + "%;\">");
							}
							strBuffer.append(masterDom.getInfo(context, DomainConstants.SELECT_NAME));
							strBuffer.append("</td>");
							Map<String, Map<String,String>> applicableItemsSortedByDisciplinesAndTypes = applicableItemsSortedByMastersAndDisciplinesAndTypes.get(masterKey);
							Iterator<String> disciplineKeysItr2 = disciplineKeys.iterator();
							while (disciplineKeysItr2.hasNext()) {
								String disciplineKey = disciplineKeysItr2.next();
								if (masterKeysItr.hasNext()) {
									strBuffer.append("<td style=\"width:" + columnWidth + "%;" + dottedBorders + "\">");
								} else {
									strBuffer.append("<td style=\"width:" + columnWidth + "%;\">");
								}
								Map<String,String> applicableItemsByTypes = applicableItemsSortedByDisciplinesAndTypes.get(disciplineKey);
								if (applicableItemsByTypes!=null && !applicableItemsByTypes.isEmpty()) {
									Set<String> typeKeys = applicableItemsByTypes.keySet();
									Iterator<String> typeKeysItr = typeKeys.iterator();
									while (typeKeysItr.hasNext()) {
										String typeKey = typeKeysItr.next();
										String intervals = applicableItemsByTypes.get(typeKey);
										if (intervals!=null && !intervals.isEmpty()) {
											strBuffer.append(i18nNow.getTypeI18NString(typeKey,languageStr));
											strBuffer.append(": ");
											//Split by ;
											StringList intervalsList = FrameworkUtil.split(intervals,";");
											Iterator<String> intervalsListItr = intervalsList.iterator();
											while (intervalsListItr.hasNext()) {
												String interval = intervalsListItr.next();
												if (interval!=null && !interval.isEmpty()) {
													if (interval.contains("-")) {
														//This is an Interval
														String lowerValue = interval.substring(0,interval.indexOf("-"));
														String upperValue = interval.substring(interval.lastIndexOf("-")+1);
														if ((lowerValue!=null && !lowerValue.isEmpty()) && (upperValue!=null && !upperValue.isEmpty())) {
															DomainObject lowerDom = new DomainObject(lowerValue);
															if (lowerDom.isKindOf(context, EnterpriseChangeConstants.TYPE_PRODUCTS) || lowerDom.isKindOf(context, EnterpriseChangeConstants.TYPE_MANUFACTURING_PLAN)) {
																strBuffer.append(String.valueOf(Integer.parseInt(lowerDom.getInfo(context, "revindex"))+1));
															} else if (lowerDom.isKindOf(context, EnterpriseChangeConstants.TYPE_BUILDS)) {
																strBuffer.append(lowerDom.getAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_BUILD_UNIT_NUMBER));
															}
															strBuffer.append("-");
															if (!upperValue.equalsIgnoreCase(APPLICABILITY_INFINITY)) {
																DomainObject upperDom = new DomainObject(upperValue);
																if (upperDom.isKindOf(context, EnterpriseChangeConstants.TYPE_PRODUCTS) || upperDom.isKindOf(context, EnterpriseChangeConstants.TYPE_MANUFACTURING_PLAN)) {
																	strBuffer.append(String.valueOf(Integer.parseInt(upperDom.getInfo(context, "revindex"))+1));
																} else if (upperDom.isKindOf(context, EnterpriseChangeConstants.TYPE_BUILDS)) {
																	strBuffer.append(upperDom.getAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_BUILD_UNIT_NUMBER));
																}
															} else {
																strBuffer.append(APPLICABILITY_INFINITY);
															}
														}
													} else {
														//This is a Single Value
														DomainObject intervalDom = new DomainObject(interval);
														if (intervalDom.isKindOf(context, EnterpriseChangeConstants.TYPE_PRODUCTS) || intervalDom.isKindOf(context, EnterpriseChangeConstants.TYPE_MANUFACTURING_PLAN)) {
															strBuffer.append(String.valueOf(Integer.parseInt(intervalDom.getInfo(context, "revindex"))+1));
														} else if (intervalDom.isKindOf(context, EnterpriseChangeConstants.TYPE_BUILDS)) {
															strBuffer.append(intervalDom.getAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_BUILD_UNIT_NUMBER));
														}
													}
												}
												if (intervalsListItr.hasNext()) {
													strBuffer.append(";");
												}
											}
										}
										if (typeKeysItr.hasNext()) {
											strBuffer.append("<br>");
										}
									}//End of while typeKeys
								}else {
									strBuffer.append("");
								}
								strBuffer.append("</td>");
							}
							strBuffer.append("</tr>");
						}
					}
					strBuffer.append("</table>");
					returnString = strBuffer.toString();
				}
			}
			return returnString;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * Method to display the New Decision Applicability Summary in Decision properties if Derivation is enabled
	 *
	 * @param context the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId (corresponds to the DecisionId)
	 * @return String - a new HTML formatted string to properly display the Decision Applicability Summary
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212_HFDerivations
	 */
	private String getDecisionApplicabilityDerivationsSummary(Context context, String[] args) throws Exception {
		try {
			PropertyUtil.setGlobalRPEValue(context, "ENO_ECH", "true");
			String returnString = "";
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");
			String decisionId = (String)paramMap.get("objectId");
			String languageStr = (String)paramMap.get("languageStr");
			if (decisionId!=null && !decisionId.isEmpty()) {
				Decision decision = new Decision(decisionId);
				String dottedBorders = "border-bottom:1px dotted #959595;";
				StringList disciplineKeys = decision.getAuthorizedChangeDisciplinesForApplicability(context);
				if (disciplineKeys!=null && !disciplineKeys.isEmpty()) {
					StringBuffer strBuffer = new StringBuffer();
					strBuffer.append("<table style=\"width:100%;\">");
					strBuffer.append("<tr>");
					strBuffer.append("<th style=\"" + dottedBorders + "\">");
					strBuffer.append("<b>" + i18nNow.getTypeI18NString(EnterpriseChangeConstants.TYPE_MODEL,languageStr) + "</b>");
					strBuffer.append("</th>");
					Iterator<String> disciplineKeysItr = disciplineKeys.iterator();
					while (disciplineKeysItr.hasNext()) {
						String disciplineKey = disciplineKeysItr.next();
						strBuffer.append("<th style=\"" + dottedBorders + "\">");
						strBuffer.append("<b>" + i18nNow.getAttributeI18NString(disciplineKey,languageStr) + "</b>");
						strBuffer.append("</th>");
					}//End of while disciplineKeysItr
					StringList masterKeys = decision.getCommonModelsForApplicability(context);
					if (masterKeys!=null && !masterKeys.isEmpty()) {
						Integer modelWidth = 10;
						Integer columnWidth = (100-modelWidth)/disciplineKeys.size();
						Map<String,Map<String,Map<String,MapList>>> applicableItemsSortedByMastersAndDisciplinesAndTypes = decision.getApplicabilitySummary(context, null, null, null);
						Iterator<String> masterKeysItr = masterKeys.iterator();
						while (masterKeysItr.hasNext()) {
							String masterKey = masterKeysItr.next();
							DomainObject masterDom = new DomainObject(masterKey);
							strBuffer.append("<tr>");
							if (masterKeysItr.hasNext()) {
								strBuffer.append("<td style=\"width:" + modelWidth + "%;" + dottedBorders + "\">");
							} else {
								strBuffer.append("<td style=\"width:" + modelWidth + "%;\">");
							}
							strBuffer.append(masterDom.getInfo(context, DomainConstants.SELECT_NAME));
							strBuffer.append("</td>");
							
							Iterator<String> disciplineKeysItr2 = disciplineKeys.iterator();
							while (disciplineKeysItr2.hasNext()) {
								String disciplineKey = disciplineKeysItr2.next();
								if (masterKeysItr.hasNext()) {
									strBuffer.append("<td style=\"width:" + columnWidth + "%;" + dottedBorders + "\">");
								} else {
									strBuffer.append("<td style=\"width:" + columnWidth + "%;\">");
								}
								
								//Check if there is Applicable Item for Master key and Change Discipline key
								if (applicableItemsSortedByMastersAndDisciplinesAndTypes!=null && !applicableItemsSortedByMastersAndDisciplinesAndTypes.isEmpty()) {
									Map<String,Map<String,MapList>> applicableItemsSortedByDisciplinesAndTypes = applicableItemsSortedByMastersAndDisciplinesAndTypes.get(masterKey);
									if (applicableItemsSortedByDisciplinesAndTypes!=null && !applicableItemsSortedByDisciplinesAndTypes.isEmpty()) {
										Map<String,MapList> applicableItemsSortedByTypes = applicableItemsSortedByDisciplinesAndTypes.get(disciplineKey);
										if (applicableItemsSortedByTypes!=null && !applicableItemsSortedByTypes.isEmpty()) {
											//Applicable Items for Master key and Discipline key exist. Need to generate the XML Applicability Expression
											//Loop for all Types
											Set<String> typeKeys = applicableItemsSortedByTypes.keySet();
											Iterator<String> typeKeysItr = typeKeys.iterator();
											boolean hasNext = false;
											while (typeKeysItr.hasNext()) {
												String typeKey = typeKeysItr.next();
												if (typeKey!=null && !typeKey.isEmpty()) {
													//Add Type key translation
													if (hasNext) {
														strBuffer.append("<br>");
													}
													//strBuffer.append(i18nNow.getTypeI18NString(typeKey,languageStr));
													//strBuffer.append(": ");
													MapList applicableItems = applicableItemsSortedByTypes.get(typeKey);
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
																
																
																//Chagned by IXE
																//String applicableItemParentId = applicableItem.get("to[" + EnterpriseChangeConstants.RELATIONSHIP_DERIVED + "].from.id");

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
																	appItem.put(DomainObject.SELECT_REVISION, applicableItemRevision);
																} else if(applicableItemType!=null && mxType.isOfParentType(context, applicableItemType, EnterpriseChangeConstants.TYPE_BUILDS)) {
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
															String effectivityTypes = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChange." + disciplineKey.replace(" ", "") + ".ApplicabilityCommands");
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
																	requestMap.put("applicableItemsList", appItemsList);
																	String displayExpression = (String) JPO.invoke(context, "emxEffectivityFramework", null, "getDisplayExpression", JPO.packArgs(requestMap), String.class);
																	displayExpression = displayExpression.replace("<", "\u003C");																			
																	strBuffer.append(XSSUtil.encodeForHTML(context,displayExpression));
																}
															}
														}
														if (typeKeysItr.hasNext()) {
															hasNext = true;
														} else {
															hasNext = false;
														}
													}
												}
											}//End of while typeKeysItr
										}
									}
								}
								strBuffer.append("</td>");
							}//End of while disciplineKeysItr2
							strBuffer.append("</tr>");
						}//End of while masterKeysItr
					}
					strBuffer.append("</tr>");
					strBuffer.append("</table>");
					returnString = strBuffer.toString();
				}
			}
			return returnString;
		} catch (Exception e) {
			throw e;
		}
	}

}


