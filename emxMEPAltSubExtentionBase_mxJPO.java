import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

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
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.engineering.EngineeringConstants;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.engineering.RelToRelUtil;
import com.matrixone.apps.framework.ui.UIUtil;

/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 */

public class emxMEPAltSubExtentionBase_mxJPO extends emxCommonPart_mxJPO implements EngineeringConstants{


	/**
	 * Constructor.
	 *
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args holds no arguments.
	 * @throws Exception if the operation fails.
	 * @since EC 9.5.JCI.0.
	 */
	public emxMEPAltSubExtentionBase_mxJPO (Context context, String[] args)
	throws Exception {

		super(context, args);
	}

	/**
	 * This method is executed if a specific method is not specified.
	 *
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args holds no arguments.
	 * @return int.
	 * @throws Exception if the operation fails.
	 * @since EC 9.5.JCI.0.
	 */
	public int mxMain(Context context, String[] args)
	throws Exception {
		if (true) {
			throw new Exception("must specify method on emxMEPAltSubExtention invocation");
		}

		return 0;
	}


	/*******  Added for ALTERNATE/SUBSTITUTE feature for ENG by D2E -- Start ******************/

	@com.matrixone.apps.framework.ui.ProgramCallable

	/**
	 * Access Function to show MEP Usage (Alt/Sub) columns. Checks for Alt/Sub
	 * feature is enabled from ENG property file. 
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args holds the input arguments.
	 * @return StringList of Part Usage.
	 * @throws Exception if the operation fails.
	 */

	public boolean showAltSub(Context context, String[] args) 
	throws Exception {

		return Boolean.parseBoolean(EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.AlternateSubstitute.EquivalentPartExtensionEnabled"));
	}

	@com.matrixone.apps.framework.ui.ProgramCallable

	/**
	 * This method gives MEP usage (Alternate, Substitute or none/blank)
	 * for a context part connected as MFG Equivalent relationship.  
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args holds the input arguments.
	 * @throws Exception if the operation fails.
	 * @return StringList of Part Usage(Alternate/Substitute/blank).
	 */

	public StringList getMEPUsage(Context context, String[] args)
	throws Exception {

		HashMap programMap 		= (HashMap)JPO.unpackArgs(args);
		HashMap paramList 		= (HashMap)programMap.get("paramList");
		MapList objectList 		= (MapList)programMap.get("objectList");
		String contextPartId 	= (String)paramList.get("objectId");

		StringList slMfgPartUsage = new StringList(objectList.size());

		String ALTERNATE	= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", Locale.ENGLISH, "emxMBOM.MBOM.Alternate");
		String SUBSTITUTE 	= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", Locale.ENGLISH, "emxMBOM.MBOMMS.Substitute");
		String LEGACY 		= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", Locale.ENGLISH, "emxEngineeringCentral.MEP.Legacy");

		String strMEPId;
		String mepUsage;
		StringList mepAltSubInfo;

		try {

			for(int i = 0; i < objectList.size();i++){

				strMEPId = ((Map<String,String>)objectList.get(i)).get(SELECT_ID);
				mepUsage = EMPTY_STRING;

				if(EngineeringUtil.isPartAuthorizedToHostCompany(context, strMEPId)){

					mepAltSubInfo = FrameworkUtil.split(MqlUtil.mqlCommand(context,
							"print bus $1 select $2 $3 $4 $5 dump", 
							strMEPId, 
							"to["+RELATIONSHIP_ALTERNATE+"|from.id=='"+contextPartId+"']", 
							"to["+RELATIONSHIP_EBOM_SUBSTITUTE+"|fromrel.to.id=='"+contextPartId+"']",
							SELECT_TYPE, SELECT_CURRENT), ",");

					if(!TYPE_MPN.equals(mepAltSubInfo.get(2))) {

						if("True".equals(mepAltSubInfo.get(0))) 
							mepUsage = ALTERNATE;

						else if("True".equals(mepAltSubInfo.get(1))) 
							mepUsage = SUBSTITUTE;

						else if(STATE_MANUFACTURER_EQUIVALENT_RELEASE.equals(mepAltSubInfo.get(3)))
							mepUsage = LEGACY;
					}
				}

				slMfgPartUsage.addElement(mepUsage);
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			//throw new Exception();
		}

		return slMfgPartUsage;
	}

	@com.matrixone.apps.framework.ui.ProgramCallable

	/**
	 * Column program for MEP Usage column. Displays if MEP is added as 
	 * alternate or Subtitute or none of them (blank) to context part.
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args holds the input arguments.
	 * @throws Exception if the operation fails.
	 * @return StringList of Part Usage(Alternate/Substitute/blank).
	 */

	public  StringList getPartUsageForMEP(Context context, String[] args) 
	throws Exception {

		HashMap programMap 		= (HashMap)JPO.unpackArgs(args);
		MapList objectList 		= (MapList)programMap.get("objectList");

		StringList slMfgPartUsage = new StringList(objectList.size());

		String ALTERNATE	= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", Locale.ENGLISH, "emxMBOM.MBOM.Alternate");
		String SUBSTITUTE 	= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", Locale.ENGLISH, "emxMBOM.MBOMMS.Substitute");
		String displayAlt	= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(), "emxMBOM.MBOM.Alternate");
		String displaySub 	= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(), "emxMBOM.MBOMMS.Substitute");

		String mepUsage =  EMPTY_STRING;

		try {

			StringList altSubInfo = getMEPUsage(context, args);

			for(int i=0; i < altSubInfo.size(); i++){

				mepUsage = (String)altSubInfo.get(i);

				if(ALTERNATE.equals(mepUsage)){
					slMfgPartUsage.addElement(displayAlt);
				}
				else if(SUBSTITUTE.equals(mepUsage)){
					slMfgPartUsage.addElement(displaySub);
				}
				else
					slMfgPartUsage.addElement(EMPTY_STRING);
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}

		return slMfgPartUsage;
	}	

	@com.matrixone.apps.framework.ui.ProgramCallable

	/**
	 * Column program for MEP Usage Context column. Displays context parent part 
	 * for which the MEP is added as Subtitute and blank for alternate.
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args holds the input arguments.
	 * @throws Exception if the operation fails.
	 * @return StringList of Context Parent Parts.
	 */

	public StringList getContextPartsForMEP(Context context, String[] args)
	throws Exception{

		HashMap programMap 		= (HashMap)JPO.unpackArgs(args);
		HashMap paramList 		= (HashMap)programMap.get("paramList");
		MapList objectList 		= (MapList)programMap.get("objectList");
		String contextPartId 	= (String)paramList.get("objectId");

		StringList slMfgPartUsageContext = new StringList(objectList.size());
		MapList subContextPartList;
		StringBuilder sbCellData;

		StringList ebomSelectables = new StringList(2);
		ebomSelectables.addElement(SELECT_FROM_ID);
		ebomSelectables.addElement(SELECT_FROM_NAME);

		String strMEPId;
		String ebomRelId;

		try {

			for(int i = 0; i < objectList.size();i++){

				strMEPId = ((Map<String,String>)objectList.get(i)).get(SELECT_ID);
				ebomRelId = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump", strMEPId, "to["+RELATIONSHIP_EBOM_SUBSTITUTE+"|fromrel.to.id=='"+contextPartId+"'].fromrel.id");

				sbCellData = new StringBuilder(100);    			
				if(EngineeringUtil.isPartAuthorizedToHostCompany(context, strMEPId) && !UIUtil.isNullOrEmpty(ebomRelId)){	

					subContextPartList = DomainRelationship.getInfo(context, ebomRelId.split(","), ebomSelectables);

					for(int j=0;j<subContextPartList.size();j++){

						sbCellData.append("<b><a href=\"javascript:showModalDialog('../common/emxTree.jsp?emxSuiteDirectory=engineeringcentral&amp;objectId=");
						sbCellData.append(((Map<String,String>)subContextPartList.get(j)).get(SELECT_FROM_ID));
						sbCellData.append( "', '700', '600')\">");
						sbCellData.append(((Map<String,String>)subContextPartList.get(j)).get(SELECT_FROM_NAME));
						sbCellData.append("</a></b><br/>");
					}
				}

				slMfgPartUsageContext.addElement(sbCellData.toString());
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			//throw new Exception();
		}

		return slMfgPartUsageContext;
	}

	@com.matrixone.apps.framework.ui.ProgramCallable

	/**
	 * Column program for MEP Usage Actions column. Displays edit Icon for alternate 
	 * MEP and edit/add/remove icons for substitute MEP and blank for others.
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args holds the input arguments.
	 * @throws Exception if the operation fails.
	 * @return StringList of Context Parent Parts.
	 */

	public StringList getActionsForMEP(Context context, String[] args)
	throws Exception {

		HashMap programMap 		= (HashMap) JPO.unpackArgs(args);
		HashMap paramList 		= (HashMap)programMap.get("paramList");
		MapList objectList 		= (MapList) programMap.get("objectList");
		String contextPartId 	= (String)paramList.get("objectId");

		StringList actions 		= new StringList(objectList.size());
		StringBuilder cellData;

		String ALTERNATE 		= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", Locale.ENGLISH, "emxMBOM.MBOM.Alternate");
		String SUBSTITUTE 		= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", Locale.ENGLISH, "emxMBOM.MBOMMS.Substitute");
		String LEGACY 			= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", Locale.ENGLISH, "emxEngineeringCentral.MEP.Legacy");
		String strModifyUsage 	= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource",context.getLocale(),"emxEngineeringCentral.MEP.ModifyUsage");
		String strAddSub 		= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource",context.getLocale(),"emxEngineeringCentral.MEP.AddSubstitute");
		String strRemoveSub 	= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource",context.getLocale(),"emxEngineeringCentral.MEP.RemoveSubstitutes");
		String strDefineAlt 	= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource",context.getLocale(),"emxEngineeringCentral.MEP.DefineAsAlternate");

		String strMEPId;
		String sMsg;

		try {

			boolean bHasModifyAccess = PersonUtil.hasAnyAssignment(context, "role_SeniorDesignEngineer,role_VPLMLeader,role_VPLMProjectLeader");

			StringList mepAltSubList = getMEPUsage(context, args);

			for(int i = 0; i < objectList.size();i++){

				strMEPId = ((Map<String,String>)objectList.get(i)).get(SELECT_ID);

				cellData = new StringBuilder(300);

				if(EngineeringUtil.isPartAuthorizedToHostCompany(context, strMEPId)){

					if(ALTERNATE.equals(mepAltSubList.get(i))){

						if(bHasModifyAccess){

							sMsg = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.MEP.Alert2");

							cellData.append("<a href=\"javascript:confirmSwitchMEP('");
							cellData.append(XSSUtil.encodeForJavaScript(context, sMsg));
							cellData.append("','../common/emxIndentedTable.jsp?field=TYPES=type_Part&amp;table=ENCAlternateSubstitutePartSummary&amp;selection=multiple&amp;MEPActions=DefineSubstitute&amp;submitAction=refreshCaller&amp;submitURL=../engineeringcentral/emxEngrMEPUsage.jsp&amp;SubmitLabel=Done&amp;CancelButton=true&amp;objectId=");
							cellData.append(XSSUtil.encodeForURL(context, strMEPId));
							cellData.append("&amp;parentOID=");
							cellData.append(XSSUtil.encodeForURL(context, contextPartId));
							cellData.append("&amp;program=emxMEPAltSubExtention:getContextParents')\"><img border=\"0\" src=\"images/iconActionEdit.gif\" alt=\"");
							cellData.append(XSSUtil.encodeForXML(context, strModifyUsage));
							cellData.append("\" title=\"");
							cellData.append(XSSUtil.encodeForXML(context, strModifyUsage));
							cellData.append("\"/></a>");
						}
						else {

							sMsg = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Alert.SeniorDesignEngineerHasAccess");

							cellData.append("<a href=\"javascript:alert('");
							cellData.append(XSSUtil.encodeForJavaScript(context, sMsg));
							cellData.append("')\"><img border=\"0\" src=\"images/iconActionEdit.gif\" alt=\"");
							cellData.append(XSSUtil.encodeForXML(context, strModifyUsage));
							cellData.append("\" title=\"");
							cellData.append(XSSUtil.encodeForXML(context, strModifyUsage));
							cellData.append("\"/></a>");
						}
					}
					else if(SUBSTITUTE.equals(mepAltSubList.get(i))){

						if(bHasModifyAccess){

							sMsg = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.MEP.RemoveSub.ConfirmAlert");

							cellData.append("<a href=\"javascript:confirmSwitchMEP('");
							cellData.append(XSSUtil.encodeForJavaScript(context, sMsg));
							cellData.append("', '../engineeringcentral/emxEngrMEPUsage.jsp?MEPActions=ModifyUsage&amp;submitAction=refreshCaller&amp;objectId=");
							cellData.append(XSSUtil.encodeForURL(context, strMEPId));
							cellData.append("&amp;parentOID=");
							cellData.append(XSSUtil.encodeForURL(context, contextPartId));
							cellData.append("')\"><img border=\"0\" src=\"images/iconActionEdit.gif\" alt=\"");
							cellData.append(XSSUtil.encodeForXML(context, strModifyUsage));
							cellData.append("\" title=\"");
							cellData.append(XSSUtil.encodeForXML(context, strModifyUsage));
							cellData.append("\"/></a>");	

							String sMsg1 = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.MEP.Alert2");
							
							cellData.append("<a href=\"javascript:confirmSwitchMEP('");
							cellData.append(XSSUtil.encodeForJavaScript(context, sMsg1));
							cellData.append("', '../common/emxIndentedTable.jsp?field=TYPES=type_Part&amp;table=ENCAlternateSubstitutePartSummary&amp;selection=multiple&amp;submitAction=refreshCaller&amp;MEPActions=DefineSubstitute&amp;submitURL=../engineeringcentral/emxEngrMEPUsage.jsp&amp;SubmitLabel=Done&amp;CancelButton=true&amp;objectId=");
							cellData.append(XSSUtil.encodeForURL(context, strMEPId));
							cellData.append("&amp;parentOID=");
							cellData.append(XSSUtil.encodeForURL(context, contextPartId));
							cellData.append("&amp;program=emxMEPAltSubExtention:getContextParents', '700', '600', 'false', 'popup', '')\"><img border=\"0\" src=\"images/iconActionAddExistingPart.gif\" alt=\"");
							cellData.append(XSSUtil.encodeForXML(context, strAddSub));
							cellData.append("\" title=\"");
							cellData.append(XSSUtil.encodeForXML(context, strAddSub));
							cellData.append("\"/></a>");

							cellData.append("<a href=\"javascript:confirmSwitchMEP('");
							cellData.append(XSSUtil.encodeForJavaScript(context, sMsg));
							cellData.append("', '../common/emxIndentedTable.jsp?field=TYPES=type_Part&amp;table=ENCAlternateSubstitutePartSummary&amp;selection=multiple&amp;submitAction=refreshCaller&amp;MEPActions=RemoveSubstitute&amp;submitURL=../engineeringcentral/emxEngrMEPUsage.jsp&amp;SubmitLabel=Done&amp;CancelButton=true&amp;objectId=");
							cellData.append(XSSUtil.encodeForURL(context, strMEPId));
							cellData.append("&amp;parentOID=");
							cellData.append(XSSUtil.encodeForURL(context, contextPartId));
							cellData.append("&amp;program=emxMEPAltSubExtention:getContextParents', '700', '600', 'false', 'popup', '')\"><img border=\"0\" src=\"images/iconActionRemove.gif\" alt=\"");
							cellData.append(XSSUtil.encodeForXML(context, strRemoveSub));
							cellData.append("\" title=\"");
							cellData.append(XSSUtil.encodeForXML(context, strRemoveSub));
							cellData.append("\"/></a>");
						}
						else {

							sMsg = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Alert.SeniorDesignEngineerHasAccess");

							cellData.append("<a href=\"javascript:alert('");
							cellData.append(XSSUtil.encodeForJavaScript(context, sMsg));
							cellData.append("');\"><img border=\"0\" src=\"images/iconActionEdit.gif\" alt=\"");
							cellData.append(XSSUtil.encodeForXML(context, strModifyUsage));
							cellData.append("\" title=\"");
							cellData.append(XSSUtil.encodeForXML(context, strModifyUsage));
							cellData.append("\"/></a>");	

							cellData.append("<a href=\"javascript:alert('");
							cellData.append(XSSUtil.encodeForJavaScript(context, sMsg));
							cellData.append("');\"><img border=\"0\" src=\"images/iconActionAddExistingPart.gif\" alt=\"");
							cellData.append(XSSUtil.encodeForXML(context, strAddSub));
							cellData.append("\" title=\"");
							cellData.append(XSSUtil.encodeForXML(context, strAddSub));
							cellData.append("\"/></a>");

							cellData.append("<a href=\"javascript:alert('");
							cellData.append(XSSUtil.encodeForJavaScript(context, sMsg));
							cellData.append("');\"><img border=\"0\" src=\"images/iconActionRemove.gif\" alt=\"");
							cellData.append(XSSUtil.encodeForXML(context, strRemoveSub));
							cellData.append("\" title=\"");
							cellData.append(XSSUtil.encodeForXML(context, strRemoveSub));
							cellData.append("\"/></a>");
						}
					}
					else if(LEGACY.equals(mepAltSubList.get(i))){
						
						if(bHasModifyAccess){

							sMsg = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.MEP.Alert3");

							cellData.append("<a href=\"javascript:confirmSwitchMEP('");
							cellData.append(XSSUtil.encodeForJavaScript(context, sMsg));
							cellData.append("', '../engineeringcentral/emxEngrMEPUsage.jsp?MEPActions=DefineAlternate&amp;submitAction=refreshCaller&amp;objectId=");
							cellData.append(XSSUtil.encodeForURL(context, strMEPId));
							cellData.append("&amp;parentOID=");
							cellData.append(XSSUtil.encodeForURL(context, contextPartId));
							cellData.append("')\"><img border=\"0\" src=\"images/iconActionEdit.gif\" alt=\"");
							cellData.append(XSSUtil.encodeForXML(context, strDefineAlt));
							cellData.append("\" title=\"");
							cellData.append(XSSUtil.encodeForXML(context, strDefineAlt));
							cellData.append("\"/></a>");	
						}
						else {

							sMsg = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Alert.SeniorDesignEngineerHasAccess");

							cellData.append("<a href=\"javascript:alert('");
							cellData.append(XSSUtil.encodeForJavaScript(context, sMsg));
							cellData.append("');\"><img border=\"0\" src=\"images/iconActionEdit.gif\" alt=\"");
							cellData.append(XSSUtil.encodeForXML(context, strDefineAlt));
							cellData.append("\" title=\"");
							cellData.append(XSSUtil.encodeForXML(context, strDefineAlt));
							cellData.append("\"/></a>");
						}
					}
				}

				actions.add(cellData.toString());
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}

		return actions;
	}


	@com.matrixone.apps.framework.ui.ProgramCallable

	/**
	 * Program to select context parents for MEP. Only context parents of MEP connected as Substitute are 
	 * returned for remove case. Add case returns parents of MEP not connected as substitute. 
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args holds the input arguments.
	 * @throws Exception if the operation fails.
	 * @return StringList of Context Parent Parts.
	 */

	public MapList getContextParents(Context context, String[] args)
	throws Exception {

		HashMap programMap  	= (HashMap)JPO.unpackArgs(args);
		String actions     		= (String)programMap.get("MEPActions");
		String strContextPartId = (String)programMap.get("parentOID");
		String selectedMEPId 	= (String)programMap.get("objectId");
		String selectSubMEP 	= new StringBuilder("frommid[").append(RELATIONSHIP_EBOM_SUBSTITUTE).append("].to.id").toString();

		MapList addSubList = new MapList();
		MapList removeSubList = new MapList();

		StringList relSelect = new StringList(2);
		relSelect.addElement(SELECT_RELATIONSHIP_ID);
		relSelect.addElement(selectSubMEP);

		try{

			//MqlUtil.mqlCommand(context, "notice '$1'", EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.MEP.Alert2"));

			MapList contextParentList = DomainObject.newInstance(context, strContextPartId)
			.getRelatedObjects(context,
					RELATIONSHIP_EBOM,
					TYPE_PART,                      
					new StringList(SELECT_ID),
					relSelect,
					true, false, (short) 1,
					null, null, 0);

			for(int i=0; i<contextParentList.size(); i++){

				Map contextParent = (Map)contextParentList.get(i);

				if(selectedMEPId.equals(contextParent.get(selectSubMEP)))
					removeSubList.add(contextParent);
				else
					addSubList.add(contextParent);
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}

		return "DefineSubstitute".equals(actions)? addSubList : removeSubList;
	}

	@com.matrixone.apps.framework.ui.ProgramCallable

	/**
	 * Method is used to switch the MEP Usage from Alt to Sub and vice versa.
	 * It also add/remove new parents as Sub for a MEP. It performs the 
	 * operations of Actions column.
	 * @param context the eMatrix <code>Context</code> object.
	 * @param <code>HashMap</code> with input parameters.
	 * @return void
	 * @throws Exception
	 */
	public void modifyUsageForMEP(Context context, String[] args)
	throws Exception {

		HashMap paramMap = JPO.unpackArgs(args);

		String[] slParentId 	= (String[])paramMap.get("parentId");
		String sMEPId 			= (String)paramMap.get("objectId");
		String sAction 			= (String)paramMap.get("action");
		String strPrimaryPartId = (String)paramMap.get("parentOID");

		String sEBOMRelId; 
		String sEBOMSubRelId;
		String sAltRelId; 

		try{

			ContextUtil.startTransaction(context, true);

			if("DefineAlternate".equalsIgnoreCase(sAction)) {
				
				connectAlternatePart(context, sMEPId, strPrimaryPartId);
			}
			else if("DefineSubstitute".equalsIgnoreCase(sAction)) {
				
				if(slParentId == null || "null".equals(slParentId) || slParentId.length == 0){
					throw new FrameworkException(EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Common.PleaseMakeSelection"));
				}

				for (int i = 0; i < slParentId.length; i++) {

					sEBOMRelId = (String)FrameworkUtil.split((String)slParentId[i], "|").get(0);
					//sParentId=(String)FrameworkUtil.split((String)slParentId[i], "|").get(1);

					sEBOMSubRelId = (new RelToRelUtil()).connect(context, RELATIONSHIP_EBOM_SUBSTITUTE, sEBOMRelId, sMEPId, false, true);

					ContextUtil.pushContext(context);
					(new DomainRelationship(sEBOMSubRelId)).setAttributeValues(context, (new DomainRelationship(sEBOMRelId)).getAttributeMap(context));
					ContextUtil.popContext(context);

					sAltRelId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",strPrimaryPartId, "from["+RELATIONSHIP_ALTERNATE+"|to.id=='"+sMEPId+"'].id");

					if(!UIUtil.isNullOrEmpty(sAltRelId)){
						DomainRelationship.disconnect(context, sAltRelId);
					}
				}
			}
			else if("RemoveSubstitute".equalsIgnoreCase(sAction)){
				
				if(slParentId == null || "null".equals(slParentId) || slParentId.length == 0){
					throw new FrameworkException(EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Common.PleaseMakeSelection"));
				}

				for (int i = 0; i < slParentId.length; i++) {

					sEBOMRelId = (String)FrameworkUtil.split((String)slParentId[i], "|").get(0);

					sEBOMSubRelId = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump", sEBOMRelId, "frommid["+RELATIONSHIP_EBOM_SUBSTITUTE+"|to.id=="+sMEPId+"].id");

					if(!UIUtil.isNullOrEmpty(sEBOMSubRelId)){
						DomainRelationship.disconnect(context, sEBOMSubRelId.split(","));
					}

					String sEBOMSubstitute = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", sMEPId, "to["+RELATIONSHIP_EBOM_SUBSTITUTE+"|fromrel.to.id=='"+strPrimaryPartId+"']");

					if("False".matches(sEBOMSubstitute)){
						connectAlternatePart(context, sMEPId, strPrimaryPartId);
					}
				}
			}
			else if("ModifyUsage".equalsIgnoreCase(sAction)){

				sEBOMSubRelId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", sMEPId, "to["+ RELATIONSHIP_EBOM_SUBSTITUTE+"|fromrel.to.id=='"+strPrimaryPartId+"'].id");

				if(!UIUtil.isNullOrEmpty(sEBOMSubRelId)){
					DomainRelationship.disconnect(context, sEBOMSubRelId.split(","));
				}

				connectAlternatePart(context, sMEPId, strPrimaryPartId);
			}

			ContextUtil.commitTransaction(context);
		}
		catch(Exception e){
			e.printStackTrace();
			ContextUtil.abortTransaction(context);
			//throw e; 
		}
	}

	@com.matrixone.apps.framework.ui.ProgramCallable

	/**
	 * Method connects the MEP as Alternate to the part 
	 * if it is not already connected.
	 * @param context the eMatrix <code>Context</code> object.
	 * @param <code>String</code> Id of MEP.
	 * @param <code>String</code> Id of Part.
	 * @return <code>String</code> Id of new connection.
	 * @throws Exception
	 */

	public String connectAlternatePart(Context context, String strMEPId, String primaryPartId)throws Exception{

		String alternateRelId=EMPTY_STRING;
		String isAlt;

		try{
			boolean bHasModifyAccess = PersonUtil.hasAssignment(context, PropertyUtil.getSchemaProperty(context, "role_SeniorDesignEngineer"));
			if(bHasModifyAccess){
			isAlt = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", primaryPartId, "from["+RELATIONSHIP_ALTERNATE+"|to.id=='"+strMEPId+"']");

			if("False".equalsIgnoreCase(isAlt)){
				alternateRelId = DomainRelationship.connect(context, new DomainObject(primaryPartId), RELATIONSHIP_ALTERNATE, new DomainObject(strMEPId)).toString();
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}

		return alternateRelId;
	}	


	@com.matrixone.apps.framework.ui.ProgramCallable

	/**
	 * Trigger Program on create Manufacturing Equivalent Rel and on promote approved of Manufacturing Equivalent policy.
	 * If the part policy is MEP and state is released, it is added as alternate while adding or releasing MEP. This 
	 * will be done if Alt/Sub feature is enabled and MEP is authorized to Host Company.  
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args holds the input arguments.
	 * @throws Exception if the operation fails.
	 * @return void of Context Parent Parts.
	 */

	public void defineAsAlternate(Context context, String[] args) throws Exception {

		String strMEPId = args[0];
		String equivalentPartExtensionEnabled = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.AlternateSubstitute.EquivalentPartExtensionEnabled");

		StringList mepSelect = new StringList(3);
		mepSelect.addElement(SELECT_TYPE);
		mepSelect.addElement(SELECT_CURRENT);
		mepSelect.addElement(SELECT_POLICY);

		try {

			Map<String, String> mepInfo = DomainObject.newInstance(context, strMEPId).getInfo(context, mepSelect);

			if("false".equalsIgnoreCase(equivalentPartExtensionEnabled) 
					|| !EngineeringUtil.isPartAuthorizedToHostCompany(context, strMEPId)
					|| !POLICY_MANUFACTURER_EQUIVALENT.equals(mepInfo.get(SELECT_POLICY))
					|| TYPE_MPN.equals(mepInfo.get(SELECT_TYPE)))
			{
				return;
			}
			else if(STATE_MANUFACTURER_EQUIVALENT_RELEASE.equals(mepInfo.get(SELECT_CURRENT))){

				String parentsForMEP = MqlUtil.mqlCommand(context, 
						"print bus $1 select $2 $3 dump", 
						strMEPId, 
						"to["+RELATIONSHIP_MANUFACTURER_EQUIVALENT+"|from.type=='"+TYPE_PART+"'].from.id",
						"to["+RELATIONSHIP_MANUFACTURER_EQUIVALENT+"|from.type=='"+TYPE_LOCATION_EQUIVALENT_OBJECT+"'].from.to["+RELATIONSHIP_LOCATION_EQUIVALENT+"].from.id");

				StringList parentforMEPList = FrameworkUtil.split(parentsForMEP, ",");

				for (int k = 0; k < parentforMEPList.size(); k++) {
					connectAlternatePart(context, strMEPId, (String)parentforMEPList.get(k));
				}
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@com.matrixone.apps.framework.ui.ProgramCallable

	/**
	 * Trigger Program on delete Manufacturing Equivalent Rel. If the part policy is MEP 
	 * and it is deleted as Manufacturing Equivalent, then all Alt/Sub definition related
	 * to that MEP will also be deleted. 
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args holds the input arguments.
	 * @throws Exception if the operation fails.
	 * @return void of Context Parent Parts.
	 */

	public void deleteAltSubRels(Context context, String[] args) throws Exception {

		String strMEPId = args[0];
		String contextPartId = args[1];

		String selectAltRelIds = (new StringBuilder("to[")).append(RELATIONSHIP_ALTERNATE).append("|from.id=='").append(contextPartId).append("'].id").toString();
		String selectSubRelIds = (new StringBuilder("to[")).append(RELATIONSHIP_EBOM_SUBSTITUTE).append("|fromrel.to.id=='").append(contextPartId).append("'].id").toString();
		String query = "print bus $1 select $2 $3 dump";

		try{

			String[] mepInfo = MqlUtil.mqlCommand(context, query, strMEPId, SELECT_POLICY, SELECT_TYPE).split(",");

			if(POLICY_MANUFACTURER_EQUIVALENT.equals(mepInfo[0]) && !TYPE_MPN.equals(mepInfo[1])){

				String altSubRelIds = MqlUtil.mqlCommand(context, query, strMEPId, selectAltRelIds, selectSubRelIds);

				if(!UIUtil.isNullOrEmpty(altSubRelIds)){
					DomainRelationship.disconnect(context, altSubRelIds.split(","));
				}
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}

	/*******  Added for ALTERNATE/SUBSTITUTE feature for ENG by D2E -- End ******************/


}
