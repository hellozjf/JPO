import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.db.BusinessObject;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
//import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeManagement;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeRequest;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeOrder;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.dassault_systemes.enovia.bom.ReleasePhase;
import com.dassault_systemes.enovia.changeaction.exceptions.ChangeActionException;
import com.dassault_systemes.enovia.changeaction.factory.ChangeActionFactory;
import com.dassault_systemes.enovia.changeaction.factory.ProposedActivityFactory;
import com.dassault_systemes.enovia.changeaction.interfaces.IBusinessObjectOrRelationshipObject;
import com.dassault_systemes.enovia.changeaction.interfaces.IChangeAction;
import com.dassault_systemes.enovia.changeaction.interfaces.IChangeActionServices;
import com.dassault_systemes.enovia.changeaction.interfaces.IProposedActivity;
import com.dassault_systemes.enovia.changeaction.interfaces.IProposedChanges;
import com.matrixone.apps.engineering.EngineeringConstants;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.engineering.ReleasePhaseManager;
import com.matrixone.apps.framework.ui.UIUtil;

import com.dassault_systemes.enovia.partmanagement.modeler.interfaces.parameterization.IParameterization;


/**
 * The <code>${CLASSNAME}</code> class provides various APIs
 * that support Set To Production process. This class is
 * intended for use within jsp pages.
  * @version Copyright (c) 1992-2016 Dassault Systemes.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class enoBGTPManagerBase_mxJPO  extends emxCommonPart_mxJPO {


	/**
	 * constructor
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public enoBGTPManagerBase_mxJPO(Context context, String[] args) throws Exception {
		super(context, args);

	}

	/**
	 * Access program to Access ENCUnderFormalChange command
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 */
	public boolean showHideUnderFormalChangeCommand(Context context,String args[]) throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strPartId = (String) programMap.get("objectId");

		DomainObject sPartObj = DomainObject.newInstance(context, strPartId);
		String sRelProcess = sPartObj.getInfo(context, EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);

		return(!EngineeringConstants.PRODUCTION.equals(sRelProcess));
	}

	/**
	 * Method to update Release Phase attribute value on part
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void updateReleaseProcess(Context context, String[] args) throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap)programMap.get("paramMap");
		HashMap requestMap = (HashMap)programMap.get("requestMap");

		String releaseProcessVal = (String)paramMap.get("New Value");
		String objectId = (String)paramMap.get("objectId");
		
		String[] configured = (String[])requestMap.get("Configured");
		//Modified below line to add "UIUtil.isNullOrEmpty(releaseProcessVal) &&" **CMM Convergence**
		if(UIUtil.isNullOrEmpty(releaseProcessVal) && configured != null && configured.length>0 && "true".equals(configured[0])){
			String[] changeControlled = (String[])requestMap.get("ChangeControlled");
			if(changeControlled != null && changeControlled.length>0 && "true".equals(changeControlled[0])){
				releaseProcessVal = EngineeringConstants.PRODUCTION;
			}
			else {
				releaseProcessVal = EngineeringConstants.DEVELOPMENT;
			}
		}

		if (UIUtil.isNullOrEmpty(releaseProcessVal)) {
			String[] createMode = (String[]) requestMap.get("createMode");
			
			if (createMode != null && createMode.length > 0 && "assignTopLevelPart".equals(createMode[0])) {
				String[] changeControlled = (String[]) requestMap.get("ChangeControlled");
				if (changeControlled != null && changeControlled.length > 0 && "true".equals(changeControlled[0])) {
					releaseProcessVal = EngineeringConstants.PRODUCTION;
				} else {
					releaseProcessVal = EngineeringConstants.DEVELOPMENT;
				}
			} else {			
				releaseProcessVal = EngineeringConstants.PRODUCTION;
			}
		}

		DomainObject part = DomainObject.newInstance(context, objectId);
		part.setAttributeValue(context, EngineeringConstants.ATTRIBUTE_RELEASE_PHASE, releaseProcessVal);
	}

	/**
	 * method to update Change Controlled attribute value on part
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void updateChangeControlled(Context context, String[] args) throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap)programMap.get("paramMap");
		HashMap requestMap = (HashMap)programMap.get("requestMap");
		String sChangeControlled = EngineeringConstants.FALSE;
		String[] configured = (String[])requestMap.get("Configured");
		String[] changeControlled = (String[])requestMap.get("ChangeControlled");
		/*if(configured != null && configured.length>0 && "true".equals(configured[0])){
			if(changeControlled != null && changeControlled.length>0 && "true".equals(changeControlled[0])){
				sChangeControlled = EngineeringConstants.TRUE;
			}
			else {
				sChangeControlled = EngineeringConstants.FALSE;
			}
		}*/
		//else {
			if(changeControlled != null && changeControlled.length>0 && "true".equals(changeControlled[0])){
				sChangeControlled = EngineeringConstants.TRUE;
			}
			else {
				String[] sReleaseProcess = (String[])requestMap.get("ReleaseProcess");
				String sChangeRequired = "Optional";
				if(sReleaseProcess != null && sReleaseProcess.length>0){
					sChangeRequired = ReleasePhase.getChangeControlled(context, sReleaseProcess[0]);
				}
				if(UIUtil.isNotNullAndNotEmpty(sChangeRequired) && EngineeringConstants.MANDATORY.equalsIgnoreCase(sChangeRequired))
					sChangeControlled = EngineeringConstants.TRUE;
				else
					sChangeControlled = EngineeringConstants.FALSE;
			}
	//	}
		String objectId = (String)paramMap.get("objectId");

		DomainObject part = DomainObject.newInstance(context, objectId);
		part.setAttributeValue(context, EngineeringConstants.ATTRIBUTE_CHANGE_CONTROLLED, sChangeControlled);
	}

	/**
	 * Method to show Change Controlled on type_Part form and returns whether its editable/non editable
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 */
	public boolean showChangeControlled(Context context, String[] args) throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap settingsMap = (HashMap) programMap.get("SETTINGS");

		String objectId = (String)programMap.get("objectId");
		String editableOption = (String)settingsMap.get("Editable");
		String productionOption = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", Locale.ENGLISH, "emxFramework.Range.Release_Phase.Production");

		DomainObject part = DomainObject.newInstance(context, objectId);
		Map<String,String> partInfoMap = part.getInfo(context, StringList.create(EngineeringConstants.SELECT_POLICY, EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE,SELECT_TYPE,"previous.id","next.id"));
		
		String sType = partInfoMap.get(SELECT_TYPE);
		boolean displayPolicy = ReleasePhase.showPolicyField(context, new StringList(sType));
		
		if(displayPolicy)
			return false;
		String policy = partInfoMap.get(EngineeringConstants.SELECT_POLICY);
		String releaseProcess = partInfoMap.get(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE); 
		if(EngineeringConstants.POLICY_CONFIGURED_PART.equals(policy) || productionOption.equals(releaseProcess)){
			settingsMap.put("Editable","false");	
		}
		else {
			settingsMap.put("Editable","true");
		}
		return true;
	}

	/**
	 * Method which gives default Release Phase attribute value
	 * @param context
	 * @param args
	 * @return HashMap
	 * @throws Exception
	 */
	public HashMap getDefaultReleasePhase(Context context,String[] args) throws Exception
	{
		HashMap defaultMap = new HashMap();
		
		String releasePhaseDefaultOption = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", Locale.ENGLISH, "emxFramework.Range.Release_Phase.Production");		
		
		defaultMap.put("Default_AddNewRow", releasePhaseDefaultOption);
		
		return defaultMap;
	}
	
	/**
	 * Method which gives default Change Controlled attribute value
	 * @param context
	 * @param args
	 * @return HashMap
	 * @throws Exception
	 */
	public HashMap getDefaultChangeControl(Context context,String[] args) throws Exception {
		
		HashMap defaultMap = new HashMap();
		
		String changeControlledDefaultOption = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", Locale.ENGLISH, "emxFramework.Range.Change_Controlled.True");		
		
		defaultMap.put("Default_AddNewRow", changeControlledDefaultOption);
		
		return defaultMap;
	}
	
	/**
	 * Method to show configured option
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 *//*
	public boolean showConfiguredOption(Context context, String[] args) 
	throws Exception {
		
		return (EngineeringUtil.isCMMInstalled(context)) && displayReleaseProcessFieldsOnCreatePart(context,args);
	}*/

	/**
	 * Method gives configured and Change Controlled checkboxes on part create page 
	 * @param context
	 * @param args
	 * @return String
	 * @throws Exception
	 */
	public String getConfiguredOption(Context context,String[] args) throws Exception {
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		HashMap requestMap = (HashMap)paramMap.get("requestMap");

	        // Retrieve the default value of of isVPMVisible from the IParamaterization
		IParameterization iParam = IParameterization.getService(context);
		String defDCVal = iParam.getDefaultDesignCollaboration(context);
		
		
		Locale locale = (Locale) requestMap.get("localeObj");
		if (locale == null) { context.getLocale(); }
		
		boolean boolConfiguredContextPart = false;
		
		String selectedObjectId = (String) requestMap.get("bomObjectId");
		
		if ( UIUtil.isNullOrEmpty(selectedObjectId) ) { selectedObjectId = (String) requestMap.get("ContextObjectId"); } // BOM powerview Insert New Part operation
		if ( UIUtil.isNullOrEmpty(selectedObjectId) ) { selectedObjectId = (String) requestMap.get("selPartObjectId"); } // BOM powerview Insert New Part Next operation 
		
		if ( UIUtil.isNotNullAndNotEmpty(selectedObjectId) ) {
			boolConfiguredContextPart = EngineeringConstants.POLICY_CONFIGURED_PART.equals( DomainObject.newInstance(context, selectedObjectId).getPolicy(context).getName() );
		}
		
		StringBuffer sbHTMLOutPut = new StringBuffer(2048);
		sbHTMLOutPut.append("<div name=\"divBGTP\">");
		sbHTMLOutPut.append("<table>");
		sbHTMLOutPut.append("<tr>");
		sbHTMLOutPut.append("<td>");
		//sbHTMLOutPut.append("<span>");
		sbHTMLOutPut.append("<input type=\"checkbox\" name=\"ChangeControlled\" value=\"false\" checked=\"true\" onclick=\"javascript:onChangeControlled()\"/>");
		sbHTMLOutPut.append(EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", locale, "emxEngineeringCentral.Common.ChangeControlled"));
		//sbHTMLOutPut.append("</span>");
		sbHTMLOutPut.append("</td>");
		sbHTMLOutPut.append("</tr>");
		if ( boolConfiguredContextPart || ( UIUtil.isNullOrEmpty(selectedObjectId) && EngineeringUtil.isCMMInstalled(context) ) ) {
			sbHTMLOutPut.append("<tr>");
			sbHTMLOutPut.append("<td height=\"30\">");
			//sbHTMLOutPut.append("<span style=\"padding-left:40px\"></span>");
			//sbHTMLOutPut.append("<span>");
			sbHTMLOutPut.append(" <input type=\"checkbox\" name=\"Configured\" value=\"false\" onclick=\"javascript:onChangeConfigured()\"/>");
			sbHTMLOutPut.append(EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", locale, "emxEngineeringCentral.Common.Configured"));
			//sbHTMLOutPut.append("</span>");
			sbHTMLOutPut.append("</td>");
			sbHTMLOutPut.append("</tr>");
		}
		if(EngineeringUtil.isVPMInstalled(context, args)){
			sbHTMLOutPut.append("<tr>");
			sbHTMLOutPut.append("<td>");
			//sbHTMLOutPut.append("<span>");

			if("true".equalsIgnoreCase(defDCVal)){
				sbHTMLOutPut.append("<input type=\"checkbox\" name=\"isVPMVisible\" checked=\"true\" value=\"true\"/>");
			} else {
				sbHTMLOutPut.append("<input type=\"checkbox\" name=\"isVPMVisible\" value=\"false\"/>");
			}
			
			sbHTMLOutPut.append(EnoviaResourceBundle.getProperty(context, "emxVPMCentralStringResource", locale, "emxVPMCentral.Label.DesignCollaboration"));
			//sbHTMLOutPut.append("</span>");
			sbHTMLOutPut.append("</td>");
			sbHTMLOutPut.append("</tr>");
		}
		sbHTMLOutPut.append("</table>");
		sbHTMLOutPut.append("</div>");
		
		return sbHTMLOutPut.toString();
	}
	
	/**
	 * Method to reload Change Controlled 
	 * @param context
	 * @param args
	 * @return HashMap
	 * @throws Exception
	 */
	public HashMap reloadChangeControlled(Context context, String[] args) 
	throws Exception {

		HashMap retMap= new HashMap(3);
		StringList changeControlledDisplayList = new StringList(2);
		StringList changeControlledActualList = new StringList(2);

		
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		HashMap columnValues = (HashMap)paramMap.get("columnValues");
		HashMap requestMap = (HashMap)paramMap.get("requestMap");
		
		String valReleaseProcess = (String)columnValues.get("ReleaseProcess");
		boolean isConfigBOM = Boolean.parseBoolean((String)requestMap.get("fromConfigBOM"));
		
		
		
		if(EngineeringConstants.PRODUCTION.equals(valReleaseProcess)){

			changeControlledDisplayList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Range.Change_Controlled.True"));
			changeControlledActualList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", Locale.ENGLISH, "emxFramework.Range.Change_Controlled.True"));
		}
		else {

			changeControlledDisplayList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Range.Change_Controlled.False"));
			changeControlledActualList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", Locale.ENGLISH, "emxFramework.Range.Change_Controlled.False"));

			//if(!isConfigBOM){
				
				changeControlledDisplayList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Range.Change_Controlled.True"));
				changeControlledActualList.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", Locale.ENGLISH, "emxFramework.Range.Change_Controlled.True"));
			//}
		}

		retMap.put("RangeValues", changeControlledActualList);
		retMap.put("RangeDisplayValue", changeControlledDisplayList);
		retMap.put("Input Type", "combobox");

		return retMap;	
	}
	
	/**
	 * @param context
	 * @param args
	 * @return HashMap
	 * @throws Exception
	 */public HashMap getReleaseProcess(Context context,String[] args) 
	throws Exception {
		
		HashMap rangeMap = new HashMap(2);
		StringList columnActual = new StringList(2);
		StringList columnDisplay = new StringList(2);
		
		columnActual.addElement(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", Locale.ENGLISH, "emxFramework.Range.Release_Phase.Production"));
		columnDisplay.addElement(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Range.Release_Phase.Production"));
		
		rangeMap.put("field_choices",columnActual);
		rangeMap.put("field_display_choices", columnDisplay);

		return rangeMap;
	}
	
	 /**
	  * @param context
	  * @param args
	  * @return HashMap
	  * @throws Exception
	  */
	public HashMap getChangeControlled(Context context,String[] args) 
	throws Exception {
		
		HashMap rangeMap = new HashMap(2);
		StringList columnActual = new StringList(2);
		StringList columnDisplay = new StringList(2);
		
		columnActual.addElement(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", Locale.ENGLISH, "emxFramework.Range.Change_Controlled.True"));
		columnDisplay.addElement(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Range.Change_Controlled.True"));
		
		rangeMap.put("field_choices",columnActual);
		rangeMap.put("field_display_choices", columnDisplay );

		return rangeMap;
	}
	
	/**
	 * For Release Phase Production, the method returns true
	 * @param context
	 * @param args
	 * @return Boolean
	 * @throws Exception
	 */
	public Boolean isChangeRequired(Context context, String[] args) 
	throws Exception {
		
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objectId      = (String) paramMap.get("objectId");
              
        String valReleaseProcess = DomainObject.newInstance(context, objectId).getInfo(context, EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);
        
        return EngineeringConstants.PRODUCTION.equals(valReleaseProcess);
    }
	
	/**
	 * This method connect the change order to the part as part of Set To Production process to modify the Release Process attribute.
	 * The requested Change will either be 'For Update' or 'For Revise' based on the current state of the part
	 * @param context : Ematrix context object 
	 * @param args
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void addChange(Context context, String args[]) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		HashMap paramMap   = (HashMap) programMap.get("paramMap");
		String sCAId ="";
		String sAlreadyConnectedCAId = EMPTY_STRING;
		String ChildPartID ="";
		String sReleasePhase ="";
		Map mChangeMap = null;
		StringList affetctedItemsIDs = new StringList();

		String changeOID = requestMap.containsKey("ChangeToReleaseOID") ? (String) requestMap.get("ChangeToReleaseOID") : (String)paramMap.get("newObjectId");		
		String parentPartId = requestMap.containsKey("partId") ? (String) requestMap.get("partId") : (String)requestMap.get("objectId");
		String sForRevise = (String) requestMap.get("strForRevise");
		if(EngineeringConstants.TRUE.equalsIgnoreCase(sForRevise)){
			ReleasePhaseManager.gotoProductionForRevise(context, parentPartId, changeOID);
			return;
		}
		ContextUtil.startTransaction(context, true);
		DomainObject partObj = new DomainObject(parentPartId);
		ChangeRequest changeRequest	  = new ChangeRequest(changeOID);
		ChangeOrder co = new ChangeOrder(changeOID);
		DomainObject changeObj = DomainObject.newInstance(context, changeOID);
		StringList slAlreadyConnectedPart = new StringList();
		if(!UIUtil.isNullOrEmpty(changeOID))
		{
			StringList busSelects = new StringList(SELECT_POLICY);
			busSelects.addElement(SELECT_ID);
			busSelects.addElement(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);
			
			String objWhere = "policy == '"+DomainConstants.POLICY_EC_PART+"' && current != "+ DomainConstants.STATE_PART_OBSOLETE;
			if(EngineeringConstants.POLICY_CONFIGURED_PART.equals(partObj.getInfo(context,DomainObject.SELECT_POLICY)))
	    	{
	    		objWhere = "";
	    		objWhere = "policy=='"+ EngineeringConstants.POLICY_CONFIGURED_PART+"'";
	    	}
			
			MapList mapList = partObj.getRelatedObjects(context,
					RELATIONSHIP_EBOM, TYPE_PART, busSelects,
					null, false, true, (short) 0, objWhere, null, 0);
			
			if(!mapList.isEmpty()){
				for (int i = 0; i < mapList.size(); i++)
				{
					Map map = (Map)mapList.get(i);
					ChildPartID = (String)map.get(SELECT_ID);
					sReleasePhase = (String)map.get(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);
					if(EngineeringConstants.DEVELOPMENT.equals(sReleasePhase))
					{
						if(!affetctedItemsIDs.contains(ChildPartID))
							affetctedItemsIDs.addElement(ChildPartID);
					}
				}
				affetctedItemsIDs.addElement(parentPartId);
			}else{
				affetctedItemsIDs.addElement(parentPartId);
			}
			
			if(EngineeringConstants.POLICY_CONFIGURED_PART.equals(partObj.getInfo(context,DomainObject.SELECT_POLICY)))
	    	{
				MapList mlConnectedItemList = co.getProposedItems(context);
				for(int i=0; i<mlConnectedItemList.size();i++){
					String sObjectId = (String)((Map)mlConnectedItemList.get(i)).get(SELECT_ID);
					sAlreadyConnectedCAId = (String)((Map)mlConnectedItemList.get(i)).get("relatedCAId");
					if(affetctedItemsIDs.contains(sObjectId)){
						slAlreadyConnectedPart.addElement(sObjectId);
						affetctedItemsIDs.remove(sObjectId);
					}
				}
	    	}
			if(changeObj.isKindOf(context, ChangeConstants.TYPE_CHANGE_REQUEST))
			{
				mChangeMap = changeRequest.connectAffectedItems(context,affetctedItemsIDs);
			}else{
				mChangeMap = co.connectAffectedItems(context, affetctedItemsIDs);
			}
		}
		if(affetctedItemsIDs.size()>0) {
			Map mCAObjMap = (Map)mChangeMap.get("objIDCAMap");
			sCAId = (String)mCAObjMap.get(parentPartId);
			IChangeAction iCa=EngineeringUtil.getChangeAction(context, sCAId);
			List<IProposedChanges> iProposedChanges = iCa.getProposedChanges(context);
			List<IProposedActivity> iProposedActivity;
			for(int i=0; i<iProposedChanges.size();i++){
				IProposedChanges proposed = iProposedChanges.get(i);
				iProposedActivity = proposed.getActivites();
				String sObjectPhyId = iProposedChanges.get(i).getWhere().getName();
				String sObjectId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",sObjectPhyId,DomainConstants.SELECT_ID);
				String strPreviousReasonForChange = EMPTY_STRING;
				if(affetctedItemsIDs.contains(sObjectId)) {
					IBusinessObjectOrRelationshipObject  iBusinessObjectOrRelationshipObject=ProposedActivityFactory.CreateIProposedActivityArgument(new BusinessObject(sObjectId));
					//1 First delete the activities behind proposed
					for( int j=0;j<iProposedActivity.size();j++)
					{
						IProposedActivity activity=iProposedActivity.get(j);
						strPreviousReasonForChange=activity.getWhy();
						activity.delete(context);
					}

					//2 If the proposed change is not none, set it as none
					String proposedWhat = proposed.getWhat();
					if(strPreviousReasonForChange.isEmpty()){
						strPreviousReasonForChange=proposed.getWhy();
					}
					if(!proposedWhat.equalsIgnoreCase("None"))
					{
						proposed.setProposedChangeAsNone(context);									
					}

					//3 Add the subactivity
					proposed.createModifyActivity(context, 0, iBusinessObjectOrRelationshipObject, null,strPreviousReasonForChange, null, null);

				}
			}
		}
		ContextUtil.commitTransaction(context);
		//Create item mark-up and approve
		//affetctedItemsIDs.addAll(slAlreadyConnectedPart);
		if(affetctedItemsIDs.size()>0)
			createItemMarkupAndApprove(context,affetctedItemsIDs, sCAId, changeOID);
		if(slAlreadyConnectedPart.size()>0)
			createItemMarkupAndApprove(context,slAlreadyConnectedPart, sAlreadyConnectedCAId, changeOID);
	}
	
	/**
	 * This method creates an Item mark-up in order to change the below listed attributes and approves it
	 * <li> attribute_ReleasePhase </li>
	 * <li> attribute_ChangeControlled </li>
	 * @param context : Ematrix context object
	 * @param sPartId: Part id 
	 * @param sCAId : Corresponding CA Id
	 * @param changeOID: Corresponding CO Id
	 * @throws Exception
	 */
	public void createItemMarkupAndApproveForAlreadyConnectedChange(Context context, String args[]) throws Exception{
		HashMap hmArgs = (HashMap)JPO.unpackArgs(args);
		String sPartId = (String)hmArgs.get("partId");
		String sCAId =  (String)hmArgs.get("CAId");
		String sChangeOID = (String)hmArgs.get("COId");
		createItemMarkupAndApprove(context,new StringList(sPartId),sCAId,sChangeOID);
	}
	/**
	 * This method creates an Item mark-up in order to change the below listed attributes and approves it
	 * <li> attribute_ReleasePhase </li>
	 * <li> attribute_ChangeControlled </li>
	 * @param context : Ematrix context object
	 * @param sPartId: Part id 
	 * @param sCAId : Corresponding CA Id
	 * @param changeOID: Corresponding CO Id
	 * @throws Exception
	 */
	public void createItemMarkupAndApprove(Context context, StringList sPartIds, String sCAId, String changeOID) throws Exception
	{
		String sPartId = "";

		for(int i=0;i<sPartIds.size();i++)
		{

			sPartId = (String)sPartIds.get(i);
			DomainObject doPartObj = DomainObject.newInstance(context, sPartId);

			//Create an Item mark-up
			DomainObject doMarkup;
			String sMarkupId = FrameworkUtil.autoName(context,"type_ItemMarkup","policy_PartMarkup");
			doMarkup = new DomainObject(sMarkupId);
			//add interface for Set To Production"
			MqlUtil.mqlCommand(context, "mod bus $1 add interface $2",sMarkupId,EngineeringConstants.INTERFACE_SET_TO_PRODUCTION);

			//Connect mark-up to part object
			RelationshipType relTypePartToMarkup = new RelationshipType(PropertyUtil.getSchemaProperty(context,"relationship_EBOMMarkup"));
			DomainRelationship.connect(context,doPartObj,relTypePartToMarkup,new DomainObject(sMarkupId));

			//Connect change to mark-up
			RelationshipType relTypeChangeToMarkup = null;
			DomainObject doChange = DomainObject.newInstance(context, sCAId);
			if(doChange.isKindOf(context, DomainConstants.TYPE_ECR))
				relTypeChangeToMarkup = new RelationshipType(PropertyUtil.getSchemaProperty(context,"relationship_ProposedMarkup"));
			else
				relTypeChangeToMarkup = new RelationshipType(PropertyUtil.getSchemaProperty(context,"relationship_AppliedMarkup"));

			DomainRelationship.connect(context,doChange,relTypeChangeToMarkup,new DomainObject(sMarkupId));

			//Create mark-up xml
			com.matrixone.jdom.Element rootElement = createXMLForItemMarkUp(context, doPartObj, doMarkup);
			DomainObject doCOObj = DomainObject.newInstance(context, changeOID);
			com.matrixone.jdom.Element changeElement = new com.matrixone.jdom.Element("Change");
			changeElement.setAttribute("name",doCOObj.getInfo(context, SELECT_NAME));
			changeElement.setAttribute("id",changeOID);
			rootElement.addContent(changeElement);

			com.matrixone.jdom.Document docXML = new com.matrixone.jdom.Document(rootElement);

			//Check-in the xml into mark-up object
			try
			{
				String sTransPath = context.createWorkspace();
				// create a file object.
				java.io.File fEmatrixWebRoot = new java.io.File(sTransPath);

				String sbusObjMarkupName = doMarkup.getInfo(context,"name");
				java.io.File srcXMLFile = new java.io.File(fEmatrixWebRoot, sbusObjMarkupName+ ".xml");

				String strEBOMCharset = "UTF-8";
				// set the font properties to xml outputter object.
				com.matrixone.jdom.output.XMLOutputter  xmlOutputter = com.matrixone.util.MxXMLUtils.getOutputter(true,strEBOMCharset);
				// create io buffer writer.
				java.io.BufferedWriter buf = new java.io.BufferedWriter(new java.io.FileWriter(srcXMLFile));
				// put xml document to putter.
				xmlOutputter.output(docXML, buf);
				buf.flush();
				buf.close();
				ContextUtil.pushContext(context);
				String mqlCmd = "checkin bus $1 $2 $3 $4";
				MqlUtil.mqlCommand(context, mqlCmd, PropertyUtil.getSchemaProperty("type_ItemMarkup"), sbusObjMarkupName,"",sTransPath+"/"+sbusObjMarkupName+".xml");
				ContextUtil.popContext(context);

				srcXMLFile.delete();
			}
			catch(Exception e)
			{
				throw e;
			}

			doMarkup.close(context);

			// delete workspace.
			context.deleteWorkspace();        

			//Approve the mark-up
			context.setCustomData("fromMarkupActions", "TRUE");
			doMarkup.setAttributeValue(context, PropertyUtil.getSchemaProperty("attribute_BranchTo"), "None");
			doMarkup.promote(context);
		}
	}



	/**
	 * This method creates the item mark-up xml in order to be checked into the Markup object
	 * @param context
	 * @param doPartObj
	 * @param doMarkup
	 * @return
	 * @throws Exception
	 */
	public com.matrixone.jdom.Element createXMLForItemMarkUp(Context context, DomainObject doPartObj, DomainObject doMarkup) throws Exception
	{
		
		SelectList sPartSelStmts = new SelectList(1);
		sPartSelStmts.addElement(SELECT_TYPE);

	    Map objMap = doPartObj.getInfo(context, (StringList)sPartSelStmts);
	    StringList targetPhaseList = ReleasePhase.getNextPhaseList(context,(String)objMap.get(SELECT_TYPE),EngineeringConstants.DEVELOPMENT);
		//Create a root element.
		com.matrixone.jdom.Element rootElement = new com.matrixone.jdom.Element("businessObject");
		//Add attributes id and name to root element.
		rootElement.setAttribute("id",doMarkup.getInfo(context,"id"));
		rootElement.setAttribute("name", doMarkup.getInfo(context,"name"));

		//Create Release Process attribute Element
		com.matrixone.jdom.Element attElement = new com.matrixone.jdom.Element("attribute");
		com.matrixone.jdom.Element attNameElement = new com.matrixone.jdom.Element("name");
		com.matrixone.jdom.Element attOldElement = new com.matrixone.jdom.Element("oldvalue");
		com.matrixone.jdom.Element attNewElement = new com.matrixone.jdom.Element("newvalue");        
		attNameElement.setText("Release Phase");
		attOldElement.setText(doPartObj.getInfo(context, EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE));
		attNewElement.setText((String)targetPhaseList.get(0));
		attElement.addContent(attNameElement);
		attElement.addContent(attOldElement);
		attElement.addContent(attNewElement);
		rootElement.addContent(attElement);

		//Create Change Controlled attribute element
		attElement = new com.matrixone.jdom.Element("attribute");
		attNameElement = new com.matrixone.jdom.Element("name");
		attOldElement = new com.matrixone.jdom.Element("oldvalue");
		attNewElement = new com.matrixone.jdom.Element("newvalue");        
		attNameElement.setText(EngineeringConstants.ATTRIBUTE_CHANGE_CONTROLLED);
		attOldElement.setText(doPartObj.getInfo(context, EngineeringConstants.SELECT_ATTRIBUTE_CHANGE_CONTROLLED));
		attNewElement.setText(EngineeringConstants.TRUE);
		attElement.addContent(attNameElement);
		attElement.addContent(attOldElement);
		attElement.addContent(attNewElement);
		rootElement.addContent(attElement);

		return rootElement;
	}

	/**
	 * Method to provide revise checkbox on add change form as part of Set To Production process
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String getForReviseOption(Context context,String[] args) throws Exception {
		return "<input type=\"checkbox\" name=\"For Revise\" value=\"true\"/>";
	}

	/**
	 * 
	 * @param context
	 * @param args
	 * @return 
	 * @throws Exception
	 */
	public boolean showForReviseOption(Context context, String[] args) throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap settingsMap = (HashMap) programMap.get("SETTINGS");

		String objectId = (String)programMap.get("partObjectId");
		String editableOption = (String)settingsMap.get("Editable");

		DomainObject part = DomainObject.newInstance(context, objectId);
		String sCurrentState = part.getInfo(context, SELECT_CURRENT);

		if(EngineeringConstants.STATE_EC_PART_RELEASE.equals(sCurrentState)){
			return !Boolean.toString(false).equals(editableOption);			
		}
		else {
			return Boolean.toString(false).equals(editableOption);
		}
	}



	/**
	 * Access method for Set To Production command
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 */
	public boolean accessUnderFormalChangeCommand(Context context, String[] args) throws Exception{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		
		String showRMBCommands    = (String) programMap.get("showRMBInlineCommands");
		String frmRMB    = (String) programMap.get("frmRMB");
		String toolbar    = (String) programMap.get("toolbar");
		
		String objectID = (String) programMap.get("parentOID");
		String isRMB = (String) programMap.get("isRMB");
		String strWhere = "current == Approved";
		if("true".equalsIgnoreCase(isRMB))
			objectID = (String) programMap.get("RMBID");
		if(UIUtil.isNullOrEmpty(objectID))
		{
			objectID = (String) programMap.get("objectId");
		}
		DomainObject partObj = DomainObject.newInstance(context,objectID);
		BusinessObject nextRev = partObj.getNextRevision(context);
		//if no next rev exists getNextRevision returns ..
		MapList itemMarkupList = partObj.getRelatedObjects(context, 
				RELATIONSHIP_EBOM_MARKUP, 
				EngineeringConstants.TYPE_ITEM_MARKUP, 
				new StringList(SELECT_ID), 
				new StringList(SELECT_RELATIONSHIP_ID), 
				true, true,(short)1, 
				strWhere, null, 0);
		if(itemMarkupList.size()>0){
			for(int i =0;i<itemMarkupList.size();i++){
				Map itmMarkup = (Map)itemMarkupList.get(i);
				String strId = (String)itmMarkup.get(SELECT_ID);
				boolean isMarkupForSetToProduction = ReleasePhase.isItemMarkupForSetToProduction(context,strId);
				if(isMarkupForSetToProduction)
					return false;
			}
			
		}
		boolean revVal = nextRev.toString().contains("..");
		if(nextRev != null && !revVal)
			return false;
		else if("Obsolete".equals(partObj.getInfo(context, SELECT_CURRENT)))
			return false;
		else if(EngineeringConstants.PRODUCTION.equals(partObj.getInfo(context, EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE)))
			return false;	
		else {
			if("ENCpartPartDetailsToolBar".equals(toolbar)){
				return true;
			}
			if("ENCOpenBOMMarkupToolBar".equals(toolbar))
			{
				return !("true".equals(showRMBCommands) || "true".equals(frmRMB));
			}
			return ("true".equalsIgnoreCase(showRMBCommands));
		}
			
		
	}
	/**
	 * Method to display policy on create part
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean displayPolicyOnCreatePart(Context context, String[] args) throws Exception{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String sTypeAdmin = (String)programMap.get(SELECT_TYPE);
		if(sTypeAdmin.indexOf("_selectedType:")>-1)
			sTypeAdmin = sTypeAdmin.substring("_selectedType:".length());
		StringList sType_Admin_List = FrameworkUtil.split(sTypeAdmin, ",");
		boolean displayPolicy = ReleasePhase.showPolicyField(context, sType_Admin_List);
		return displayPolicy;
	}
	
	/**
	 * Method to display release phase field on create part 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean displayReleaseProcessFieldsOnCreatePart(Context context, String[] args) throws Exception{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String sTypeAdmin = (String)programMap.get(SELECT_TYPE);
		if(sTypeAdmin.indexOf("_selectedType:")>-1)
			sTypeAdmin = sTypeAdmin.substring("_selectedType:".length());
		StringList sType_Admin_List = FrameworkUtil.split(sTypeAdmin, ",");
		boolean displayPolicy = ReleasePhase.showPolicyField(context, sType_Admin_List);
		return !displayPolicy;
	}
	
	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean displayReleaseProcessFieldsOnProperty(Context context, String[] args) throws Exception{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String)programMap.get("objectId");
		DomainObject doObj = DomainObject.newInstance(context, objectId);
		String sType = doObj.getInfo(context, SELECT_TYPE);
		boolean displayPolicy = ReleasePhase.showPolicyField(context, new StringList(sType));
		return !displayPolicy;
	}
	/**
	 * Method to get the release phase
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public HashMap getReleasePhase(Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap)programMap.get("requestMap");
		
		String sTypeAdmin = (String)requestMap.get(SELECT_TYPE);
		String objectId = (String)requestMap.get("objectId");
		
		if(sTypeAdmin.indexOf("_selectedType:")>-1)
			sTypeAdmin = sTypeAdmin.substring("_selectedType:".length());
		StringList sType_Admin_List = FrameworkUtil.split(sTypeAdmin, ",");
		String sType = (String)sType_Admin_List.get(0);
		sType = ("*".equals(sType)) ? DomainObject.newInstance(context, objectId).getInfo(context, DomainConstants.SELECT_TYPE) : sType;
		sType = ((sType).indexOf("type_") >-1) ? PropertyUtil.getSchemaProperty(context, sType):sType;
		
		HashMap hmRangeMap = ReleasePhase.getPhaseList(context, sType);
		return hmRangeMap;
	}
	
	
	/**
	 * Method to allow edit of Change Controlled on multipart create page
	 * @param context
	 * @param args
	 * @return String List
	 * @throws Exception
	 */
	public StringList allowEditInMulitpartCreatePage(Context context, String[] args) throws Exception
	{
		StringList slReturnList = new StringList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList mlObjectList = (MapList)programMap.get("objectList");
		StringList slSelectables = new StringList();
		slSelectables.add( EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);
		slSelectables.add(SELECT_POLICY);
		for(int i =0; i<mlObjectList.size();i++){
			Map objDetailMap = (Map)mlObjectList.get(i);
			String sId = (String)objDetailMap.get(SELECT_ID);
			Map objInfo = DomainObject.newInstance(context, sId).getInfo(context,slSelectables);
			String sReleasePhase =(String)objInfo.get(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);
			String sPolicy = (String)objInfo.get(SELECT_POLICY);
			String changeControlledReqd = ReleasePhase.getChangeControlled(context, sReleasePhase);
			if(EngineeringConstants.MANDATORY.equalsIgnoreCase(changeControlledReqd) || EngineeringConstants.POLICY_CONFIGURED_PART.equals(sPolicy))
				slReturnList.add(false);
			else
				slReturnList.add(true);
		}
		return slReturnList;
	}
	
	/**
	 * Method to check the part with release phase development to have BGTP Item Markup which is not applied
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public int checkIfPartHasItemMarkupNotApplied(Context context, String []args)  throws Exception {
		final String sMarkupCurrent = PropertyUtil.getSchemaProperty(context,"policy", DomainConstants.RELATIONSHIP_EBOM_MARKUP, "state_Applied");
		String partId = args[0];

		DomainObject partObj = DomainObject.newInstance(context, partId);

		SelectList objSelects = new SelectList(2);
		objSelects.add(DomainConstants.SELECT_ID);
		objSelects.add(DomainConstants.SELECT_CURRENT);
		String objWhere = "(attribute[" + EngineeringConstants.ATTRIBUTE_RELEASE_PHASE + "]=="+EngineeringConstants.DEVELOPMENT+")" ;
		
		MapList sMarkupIDState = partObj.getRelatedObjects(context, DomainConstants.RELATIONSHIP_EBOM_MARKUP, EngineeringConstants.TYPE_ITEM_MARKUP , objSelects, null, false, true, (short) 1, null, null,0);
		
		for(int i = 0; i < sMarkupIDState.size(); i++)
		{
			Map map = (Map)sMarkupIDState.get(i);
			String sMarkupId = (String)map.get(DomainConstants.SELECT_ID);
			String sMarkupState = (String)map.get(DomainConstants.SELECT_CURRENT);
			boolean isBGTPMarkup = ReleasePhase.isItemMarkupForSetToProduction(context,sMarkupId);

			if(isBGTPMarkup && !sMarkupCurrent.equals(sMarkupState))
			{
				String strMessage = EngineeringUtil.i18nStringNow(context,"ENCBOMGoToProduction.Confirm.ApplyBGTPItemMarkup",
						context.getSession().getLanguage());
				emxContextUtil_mxJPO.mqlNotice(context,strMessage);
				return 1;
			}
		}
		return 0;
	}
	
	/**
	 * Access method for Change Controlled and Release Phase columns in My Engineering View
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 */
	public boolean isTypePartFromMyEngView(Context context, String[] args) throws Exception{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String portalCommandName = (String)programMap.get("portalCmdName");
		if(UIUtil.isNotNullAndNotEmpty(portalCommandName) && "MyENGView_Parts".equals(portalCommandName))
			return true;
		else
			return false;
	}
	/**
	 * Access method for Change Controlled and Release Phase columns
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 */
	public boolean displayReleaseProcessColumns(Context context, String[] args) throws Exception{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String sCallerPage = (String)programMap.get("callerPage");
		if("ItemMarkup".equals(sCallerPage))
			return false;
		else
			return true;
	}
	
}
