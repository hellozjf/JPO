/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Relationship;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.engineering.EngineeringConstants;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.engineering.Part;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.json.*;
import com.matrixone.jsystem.util.StringUtils;
import com.matrixone.apps.domain.util.PropertyUtil;


/**
 * The <code>emxPartBase</code> class contains implementation code for emxPart.
 *
 * @version EC 9.5.JCI.0 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class enoEngDragAndDropBase_mxJPO
{

	private String fnLength = "";
	private String rdlen =  "";
    public enoEngDragAndDropBase_mxJPO(Context context, String[] args)
			throws Exception {
		fnLength = EnoviaResourceBundle.getProperty(context,"emxEngineeringCentral.FindNumberLength");
    	rdlen = EnoviaResourceBundle.getProperty(context,"emxEngineeringCentral.ReferenceDesignatorLength");
		// TODO Auto-generated constructor stub
	}
	
    /*
     *   Pads zero or nine's to the find number or refdesg  based on the 
     *   emxEngineeringCentral.FindNumberLength value or emxEngineeringCentral.ReferenceDesignatorLength
     *
     *   @value : Value the actual string which needs to be padded
     *   @length : Nos times the value has to be padded.
     *   @return returns the Padded string
     *   @throws Exception if error encountered while carrying out the request
     */
    
    String numberPadding(String value,String length) throws Exception {

        int iLength = Integer.parseInt(length);
        String val = value;
        String fnValuesVector = "";

   	 if(value == null || "null".equalsIgnoreCase(value)) {
   		value = "";
   	 }
            for(int i=0;i<=iLength;i++)
            {
               if(value.length()<iLength)
               {

            	   value = val+value;
               }
               else
               {
               	fnValuesVector= value;
                   break;
               }
             }
        return fnValuesVector;
    }
    
    /*
     *   Returns the highest Find Number in a BOM under a given parent
     *
     *   @param : context.
     *   @return returns the highest value in a stringList
     *   @throws Exception if error encountered while carrying out the request
     */
		public static int getHighestNumber(StringList slValues) {
	    	int iNumber;
	    	int highestNumber = 0;
	    	for (int i = 0; i < slValues.size(); i++) {
	    		String slValue = (String) slValues.get(i);
	    		slValue = slValue.contains(".")? slValue.replaceAll("\\..*", ""):slValue;
	    		if(UIUtil.isNotNullAndNotEmpty(slValue)){
	    		iNumber = Integer.parseInt(slValue);
	    		if (iNumber > highestNumber) { highestNumber = iNumber; }
	    		}
	    	}
	    	
	    	return highestNumber;
	    }
		
		 /*
	     *   Returns the format if the initial Find Number in a BOM under a given parent
	     *   @param : context
	     *   @return returns the initial Find Number format based on setting.
	     *   @throws Exception if error encountered while carrying out the request
	     */
		public int getInitialNumber(Context context) throws Exception{
			int initialFn = 1;
		     String fnDisplayLeadingZeros = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.FindNumberDisplayLeadingZeros");
	    	if(UIUtil.isNotNullAndNotEmpty(fnDisplayLeadingZeros) && fnDisplayLeadingZeros.equalsIgnoreCase("true"))
	    	{
	    		//String fnValue = (String)JPO.invoke(context,"emxPart",null,"findNumberPadding",JPO.packArgs(argMap),String.class);
	    		String fnValue = numberPadding("0",fnLength)+"1";
	    		initialFn = Integer.parseInt(fnValue);
	    	}
	    	return initialFn;
	    }
		
		/*
	     *   Returns the default reference Designator on dropping a Part under a part
	     *
	     *   @return returns default reference Designator value.
	     *   @throws Exception if error encountered while carrying out the request
	     */
		 public String refDesig(Context context) throws Exception{
			 String rd= "";
			 Random randomGenerator = new Random();
			 String fnnrdReq =  EnoviaResourceBundle.getProperty(context,"emxEngineeringCentral.EBOMUniquenessOperator");
			 String rdPrefix =  EnoviaResourceBundle.getProperty(context,"emxEngineeringCentral.ReferenceDesignatorPrefix");
			 String fnRequired = "";
			 String actualType = "";
			 Map fnReq = EngineeringUtil.typeFNRDRequiredStatusMap(context);
			 Iterator iterator = fnReq.keySet().iterator();
			 while(iterator.hasNext()) {
			   actualType  = (String)iterator.next();
			   if (actualType==null || "".equals(actualType))
			         continue;
			   StringList sValue = (StringList)fnReq.get(actualType);
			   fnRequired = (String)sValue.get(0);
			 }
			 String length = "0";
			 int random;
			 int len;
			 if((UIUtil.isNotNullAndNotEmpty(fnnrdReq) && fnnrdReq.equalsIgnoreCase("&&"))|| (UIUtil.isNotNullAndNotEmpty(fnRequired) && fnRequired.equalsIgnoreCase("false"))){
				 length = ("0".equals(rdlen)) ? "9999" : numberPadding("9",rdlen);
				 len = (Integer.parseInt(rdlen)<= 0)? 9999 : Integer.parseInt(length);
				 random = randomGenerator.nextInt(len);
				 rd = rdPrefix+random;
			 }
			 return rd;
		 }
		
				 
		 /*
		     *   Returns a pass/failure message by validating whether multiple revisions of same part exists under a parent part
		     *   @dropParentinfo: parent parts info.
		     *   @childDetls: child parts info.
		     *   @dropParentPart: Parent part object.
		     *   @return validation message.
		     *   @throws Exception if error encountered while carrying out the request
		     */
		 
		 public String checkForRecurssion(Context context, Map dropParentinfo,Map childDetls,Part dropParentPart){
			 String checkIfRecurssionExists = "";
			 try{
				 				
				 String childID = (String)childDetls.get(EngineeringConstants.SELECT_ID);		
				 
//				 StringList parentWhereused = dropParentPart.getInfoList(context, "to["+EngineeringConstants.RELATIONSHIP_EBOM+"].from.id");
				 MapList chRevisionsInfo = DomainObject.newInstance(context, childID).getRevisionsInfo(context, new StringList(), new StringList(childID));
				 
				 Map mpResult;
				 String chrevId = ""; 
				 for(int i=0;i<chRevisionsInfo.size();i++){
					 mpResult = (Map)chRevisionsInfo.get(i);
					 chrevId = (String)mpResult.get(EngineeringConstants.SELECT_ID);
					 if(chrevId.contains(dropParentPart.getId(context))){
							 checkIfRecurssionExists =EnoviaResourceBundle.getProperty(context,"emxEngineeringCentralStringResource",context.getLocale(),"emxEngineeringCentral.Alert.RecursionError");
							 break;
					 }
//					 if(parentWhereused.contains(chrevId)){
//						 checkIfRecurssionExists =EnoviaResourceBundle.getProperty(context,"emxEngineeringCentralStringResource",context.getLocale(),"emxEngineeringCentral.Alert.RecursionError");
//						 break;
//					 }
				 }
			 }catch (Exception e)
			 	{
			         e.printStackTrace();
			    }
				 return checkIfRecurssionExists;
				 
			 }
		 
		 /*
		     *   Returns a pass/failure message by validating whether multiple revisions of same part exists under a parent part
		     *   @dropParentinfo: parent parts info.
		     *   @childDetls: child parts info.
		     *   @dropParentPart: Parent part object.
		     *   @return validation message.
		     *   @throws Exception if error encountered while carrying out the request
		     */
		 
		 public String multipleRevisionValidationOnDrop(Context context, JSONArray jDragObjects) throws Exception {
             StringList objectIdList = EngineeringUtil.getValueForKey(jDragObjects, "oid");
             
             int dragObjectSize = objectIdList.size();
            
             String objectId;
             String validationResult = "";
            
             StringList revisionIdList;
            
             MapList mlRevisionInfoList;
            
             for (int i = 0; i < dragObjectSize; i++) {
                   objectId = DomainObject.newInstance(context, (String)objectIdList.get(i)).getInfo(context, DomainObject.SELECT_ID);
             	  	objectIdList.remove(i);
             	  	objectIdList.add(i, objectId);
                   
                    mlRevisionInfoList = DomainObject.newInstance(context, objectId).getRevisionsInfo(context, new StringList(DomainConstants.SELECT_ID), new StringList());
                   
                    revisionIdList = EngineeringUtil.getValueForKey(mlRevisionInfoList, DomainConstants.SELECT_ID);
                    revisionIdList.remove(objectId);
                   
                    objectIdList.removeAll(revisionIdList);
                   
                    if (objectIdList.size() != dragObjectSize) {
                         validationResult = EngineeringUtil.i18nStringNow(context, "FloatOnEBOMManagement.AddExisting.MultipleRevisionsNotAllowed", context.getSession().getLanguage());
                         validationResult = validationResult.replace("CHILDNAME", DomainObject.newInstance(context, objectId).getName(context));
                         break;
                    }
                   
             }
            
             return validationResult;
		 }
		 /*
		     *   Returns a pass/failure message by validating whether multiple revisions of same part exists under a parent part
		     *   @dropParentinfo: parent parts info.
		     *   @childDetls: child parts info.
		     *   @dropParentPart: Parent part object.
		     *   @return validation message.
		     *   @throws Exception if error encountered while carrying out the request
		     */
		 
		 public String multipleRevisionValidation(Context context, Map dropParentinfo,Map childDetls,Part dropParentPart) throws Exception{
			 String multRevExists = "";
			 try{
				 
				 String parentName = (String)dropParentinfo.get(EngineeringConstants.SELECT_NAME);
				
				 String childID = (String)childDetls.get(EngineeringConstants.SELECT_ID);
				 String childName = (String)childDetls.get(EngineeringConstants.SELECT_NAME);
				 String childRevision = (String)childDetls.get(EngineeringConstants.SELECT_REVISION);
				 
				 StringList childIds = new StringList();
				 StringList childList = dropParentPart.getInfoList(context, "from["+EngineeringConstants.RELATIONSHIP_EBOM+"].to.name");
				 if(childList.contains(childName)){
					 childIds = dropParentPart.getInfoList(context, "from["+EngineeringConstants.RELATIONSHIP_EBOM+"].to.id");
					 if(!childIds.contains(childID)){
						 multRevExists = EngineeringUtil.i18nStringNow(context,"FloatOnEBOMManagement.AddExisting.MultipleRevisionsConflict",context.getSession().getLanguage());
						 multRevExists = multRevExists.replace("CHILDNAME", childName);
						 multRevExists = multRevExists.replace("CHILDREVISION", childRevision);
						 multRevExists = multRevExists.replace("PARENTNAME", parentName);
					 }
					 
				 }
			 }catch (Exception e)
		      {
		         throw new Exception(e.toString());
		      }
			 return multRevExists;
		 }
		 
		 /*returns if Parent Part is in Priliminary state or not*/
		 public String isDropObjectInPri(Context context,Part dropParentPart,Map dropParentinfo) throws Exception{
			 String isDropAllowed = "";
			 String locale = context.getSession().getLanguage();
			 String parentCurrentState = (String)dropParentinfo.get(EngineeringConstants.SELECT_CURRENT);
			 if(!parentCurrentState.equals(EngineeringConstants.STATE_PART_PRELIMINARY)){
				 String[] messageValues = new String[4];
		            messageValues[0] = UINavigatorUtil.getAdminI18NString("Type", (String)dropParentinfo.get(EngineeringConstants.SELECT_TYPE), locale.toString());
		            messageValues[1] = (String)dropParentinfo.get(EngineeringConstants.SELECT_NAME);
		            messageValues[2] = (String)dropParentinfo.get(EngineeringConstants.SELECT_REVISION);
		            messageValues[3] = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",context.getLocale(),"emxFramework.State.EC_Part.Preliminary");
				 isDropAllowed = MessageUtil.getMessage(context,null,
                         "emxEngineeringCentral.DragDrop.NotPreliminaryChildPart.message",
                         messageValues,null,
                         context.getLocale(),"emxEngineeringCentralStringResource");
			 }
			 return isDropAllowed;
		 }
		 
		 /*
		     *   Returns a pass/failure message by validating whether Drop operation can be performed under a part. 
		     *   @dropParentinfo: parent parts info.
		     *   @childDetls: child parts info.
		     *   @dropParentPart: Parent part object.
		     *   @return validation message.
		     *   @throws Exception if error encountered while carrying out the request
		     */
		 public String allowDropValidation(Context context, StringList objSel,Map childDetls,Part dropParentPart) throws Exception{
			 boolean policyCheck = true;
			 String isDropAllowed = "";
			 try{
				 
				 Map dropParentinfo = dropParentPart.getInfo(context, objSel);
				 String parentType = (String)dropParentinfo.get(EngineeringConstants.SELECT_TYPE);
				 String parentName = (String)dropParentinfo.get(EngineeringConstants.SELECT_NAME);
				 String parentRevision = (String)dropParentinfo.get(EngineeringConstants.SELECT_REVISION);
				 String parentPolicy = (String)dropParentinfo.get(EngineeringConstants.SELECT_POLICY);
				 String parentCurrentState = (String)dropParentinfo.get(EngineeringConstants.SELECT_CURRENT);
				 String childPolicy = (String)childDetls.get(EngineeringConstants.SELECT_POLICY);
				 String childName = (String)childDetls.get(EngineeringConstants.SELECT_NAME);
				 String partVersion = (String)childDetls.get("attribute["+PropertyUtil.getSchemaProperty(context,"attribute_IsVersion")+"]");
				 String attrSparePart = PropertyUtil.getSchemaProperty(context,"attribute_SparePart");
				 //String childrelPhase = (String)childDetls.get(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);
				 String childCurrentState = (String)childDetls.get(EngineeringConstants.SELECT_CURRENT);
				 String SparePart = (String)childDetls.get("attribute["+attrSparePart+"]");
				 
				 
				 String locale = context.getSession().getLanguage();
				 
				 
//				 String checkForRecurssion = checkForRecurssion(context,dropParentinfo,childDetls,dropParentPart);
//				 
//				 if(!checkForRecurssion.equals("")){
//					 isDropAllowed = checkForRecurssion;
//					 return isDropAllowed;
//				 }
				 
				 String multiRevexists = multipleRevisionValidation(context,dropParentinfo,childDetls,dropParentPart);
				 if(!multiRevexists.equals("")){
					 isDropAllowed = multiRevexists;
					 return isDropAllowed;
				 }
				 
				 if(!(childPolicy.equals(EngineeringConstants.POLICY_EC_PART)) && !(childPolicy.equals(EngineeringConstants.POLICY_CONFIGURED_PART))){
					 String[] messageValues = new String[1];
					 messageValues[0] = childPolicy;
				 	 isDropAllowed = MessageUtil.getMessage(context,null,
	                         "emxEngineeringCentral.DragDrop.AddError",
	                         messageValues,null,
	                         context.getLocale(),"emxEngineeringCentralStringResource");
				 	 //EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.DragDrop.AddError",locale);
				 	 //isDropAllowed = isDropAllowed.replace("PARTPOLICY", childPolicy);
					 return isDropAllowed;
				 }
				 
				 if(childPolicy.equals(EngineeringConstants.POLICY_CONFIGURED_PART)){
					 policyCheck = !(parentPolicy.equals(EngineeringConstants.POLICY_CONFIGURED_PART)) ? false : true;
					 if(!policyCheck){
						 isDropAllowed = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.DragDrop.ModifyErrorConfiguredPart",locale);
						 return isDropAllowed;
					 }
				 }
//				 if(parentrelPhase.equalsIgnoreCase("Development") && !parentPolicy.equals(EngineeringConstants.POLICY_CONFIGURED_PART))
//				 {
//					 relPhaseCheck = (childrelPhase.equalsIgnoreCase("Production")) ? false : true;
//					 if(!relPhaseCheck)
//					 {
//						 isDropAllowed = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.DragDrop.ModifyErrorDevelopmentPart",context.getSession().getLanguage());
//						 return isDropAllowed;
//					 }
//				 }
				 if(childCurrentState.equals(EngineeringConstants.STATE_PART_OBSOLETE)){
					 isDropAllowed = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.DragDrop.ObsoletePart.message",locale);
					 return isDropAllowed;
				 }
				 if(!parentCurrentState.equals(EngineeringConstants.STATE_PART_PRELIMINARY)){
					 String[] messageValues = new String[4];
			            messageValues[0] = UINavigatorUtil.getAdminI18NString("Type", (String)dropParentinfo.get(EngineeringConstants.SELECT_TYPE), locale.toString());
			            messageValues[1] = (String)dropParentinfo.get(EngineeringConstants.SELECT_NAME);
			            messageValues[2] = (String)dropParentinfo.get(EngineeringConstants.SELECT_REVISION);
			            messageValues[3] = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",context.getLocale(),"emxFramework.State.EC_Part.Preliminary");
					 isDropAllowed = MessageUtil.getMessage(context,null,
	                         "emxEngineeringCentral.DragDrop.NotPreliminaryChildPart.message",
	                         messageValues,null,
	                         context.getLocale(),"emxEngineeringCentralStringResource");
//					 isDropAllowed = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.DragDrop.NotPreliminaryChildPart.message1",locale) +
//					    		" "+ parentType + " " + parentName+ " " + parentRevision + " " +
//					    		EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.DragDrop.NotPreliminaryChildPart.message2",locale);
					 return isDropAllowed;
				 }
				 
				 if("Yes".equalsIgnoreCase(SparePart)){
					 isDropAllowed = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.DragDrop.ModifyErrorAddPart",locale);
					 isDropAllowed = isDropAllowed.replace("CHILDPART", EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",context.getLocale(),"emxFramework.Attribute.Spare_Part"));
					 isDropAllowed = isDropAllowed.replace("PARENTPART", parentName);
					 return isDropAllowed;
				 }
				 
				 if("TRUE".equalsIgnoreCase(partVersion)){
					 isDropAllowed = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.DragDrop.ModifyErrorAddPart",locale);
					 isDropAllowed = isDropAllowed.replace("CHILDPART", childName);
					 isDropAllowed = isDropAllowed.replace("PARENTPART", parentName);
					 return isDropAllowed;
				 }
				 
			 }
			 catch (Exception e)
	        {
	           throw new Exception(e.toString());
	        }
			 return isDropAllowed;
		 }
		 
		 
		 
		 public synchronized JSONObject connectDropedObjects(Context context, JSONObject jDropObject, JSONArray jDragObjects,StringList objSel,Part dropParentPart,String dropAction) throws Exception{
			 JSONObject ret = new JSONObject();
			 	
			 	StringBuffer returnMsgBuffer = new StringBuffer();
			 	
			 	String dropRowID = jDropObject.getString("id");
			 	String dropRowInfo = getParentRowIdInfo(dropRowID);
			    HashMap hmRelAttributesMap = new HashMap();
			    int initialFn;
			    String dropSuccessful = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",context.getLocale(),"emxFramework.DropProcess.DropOperationSuccessful");
			    int highestFn;
			    String rd = "";
			    Relationship rel;
			    String unitOfMeasure = "";
			    
			    String locale = context.getSession().getLanguage();
			    
			    String dropObjectId = dropParentPart.getId(context);
			    Map dropChildinfo; 
			    DomainObject dragObject;

		    	//check for VPMDefault value
		    	String vpmControlState = "";
		    	String isVPMVisible = "";
		        boolean isENGSMBInstalled = EngineeringUtil.isENGSMBInstalled(context, false);    
		        
			    //Check the highest fn for the parent Part
		    	StringList fn = dropParentPart.getInfoList(context, "from["+ EngineeringConstants.RELATIONSHIP_EBOM+"].attribute["+EngineeringConstants.ATTRIBUTE_FIND_NUMBER+"].value" );
		    	initialFn = getInitialNumber(context);
			    highestFn = (fn.size()>0)? getHighestNumber(fn)+1 : initialFn;		
			    rd = refDesig(context);
			    
			    for (int i=0; i<jDragObjects.length(); i++) {
				    	JSONObject jDragObject = jDragObjects.getJSONObject(i);
				    	String dragObjectId = DomainObject.newInstance(context,(String)jDragObject.getString("oid")).getInfo(context, DomainObject.SELECT_ID);
					    String dragObjectcRelId = jDragObject.getString("rid");
				    	dragObject = new DomainObject(dragObjectId);
				    	
				    	dropChildinfo = dragObject.getInfo(context, objSel);
				        
				    	if(isENGSMBInstalled) {     		
				        	String mqlQuery = new StringBuffer(100).append("print bus $1 select $2 dump").toString();
				    		vpmControlState = MqlUtil.mqlCommand(context, mqlQuery,dropObjectId,"from["+DomainConstants.RELATIONSHIP_PART_SPECIFICATION+"|to.type.kindof["+EngineeringConstants.TYPE_VPLM_CORE_REF+"]].to.attribute["+EngineeringConstants.ATTRIBUTE_VPM_CONTROLLED+"]");    		
				        }
				        if(isENGSMBInstalled && "true".equalsIgnoreCase(vpmControlState)) { 
				        	isVPMVisible = "False";
			            } else {
			            	isVPMVisible = "True";
			            } 
			        	unitOfMeasure = (String) dropChildinfo.get(EngineeringConstants.SELECT_ATTRIBUTE_UNITOFMEASURE);
			        	
				    	hmRelAttributesMap.put(EngineeringConstants.ATTRIBUTE_FIND_NUMBER, highestFn+"");
				    	hmRelAttributesMap.put(EngineeringConstants.ATTRIBUTE_UNIT_OF_MEASURE, unitOfMeasure);
				    	hmRelAttributesMap.put(EngineeringConstants.ATTRIBUTE_REFERENCE_DESIGNATOR, rd);
				    	hmRelAttributesMap.put("isVPMVisible", isVPMVisible);
				    	
				    	highestFn+=1;
				    	
				    	if(dropAction.equalsIgnoreCase("Move") && UIUtil.isNotNullAndNotEmpty(dragObjectcRelId))
				    	{
				    		
				    		String cmd = "mod connection $1_relID $2_direction $3_destinationid";
	    					MqlUtil.mqlCommand(context, cmd, dragObjectcRelId, "from", dropObjectId);
	    					new DomainRelationship(dragObjectcRelId).setAttributeValues(context, hmRelAttributesMap);

		    				ret.put("result", "pass");
		    				ret.put("messaage", dropSuccessful);
		    				ret.put("relIds", dragObjectcRelId);

				    	}
				    	else{
				    		returnMsgBuffer.append(" FreezePaneregister(\"")
							   .append(dropRowInfo)
							   .append("\"); rebuildView();")
						       .append(" emxEditableTable.addToSelected('<mxRoot><action>add</action><data status=\"committed\">");
					    	rel = new BusinessObject(dropObjectId).connect(context, new RelationshipType(EngineeringConstants.RELATIONSHIP_EBOM),true, new BusinessObject(dragObjectId));
					    	new DomainRelationship(rel).setAttributeValues(context, hmRelAttributesMap);
					    	returnMsgBuffer.append(" <item oid=\""+dragObjectId+"\" relType=\""+EngineeringConstants.RELATIONSHIP_EBOM +"\" relId=\""+rel+"\" pid=\""+dropObjectId+"\" direction=\"\"></item>")
	    					.append(" </data></mxRoot>');");
						    returnMsgBuffer.append(" FreezePaneunregister(\"")
							   .append(dropRowInfo)
							   .append("\");rebuildView();");

						    ret.put("result", "pass");
						    ret.put("onDrop", "function () {"+returnMsgBuffer.toString()+"}");
				    	}

					}
			 return ret;
		 }
		 
		 /*
		     *   Returns a JSONObject with required information to perform a Drag and drop Operation. 
		     *   @args: Will have the information of the Dragged Part and the Dropped Part in the following format.
		     *   		drop={"window":"ENCBOM","columnName":,"timestamp":"","object":{"oid":,:,"rid":}}
		     * 			drag={"objects":[{"icon":,"id":,"oid":,"rid":,"type":},{"icon":,"id":,"oid":,"rid":,"type":}],"action":,"window":}}
		     *   @return JSONObject with contains the information about the operation is pass or fail along with relationship id's of the connected objects.
		     *   @throws Exception if error encountered while carrying out the request
		     */
		 public JSONObject dragPartProcess(Context context, String[] args) throws Exception{
			
			    Map param = (Map)JPO.unpackArgs(args);
			    System.out.println(param);
			    
			    
			    JSONObject ret = new JSONObject();
			    try{
			    
					    JSONObject jDrop = (JSONObject)param.get("drop");
					    JSONObject jDropObject = jDrop.getJSONObject("object");
					    JSONObject jDrag = (JSONObject)param.get("drag");
					    JSONArray jDragObjects = jDrag.getJSONArray("objects");
					    Object jAction = jDrag.get("action");
					    
					    Object jContextWindow = jDrop.get("window");
					    
					    String dropAction = jAction.toString();
					    String dropWindow = jContextWindow.toString();
					    String dropObjectId = jDropObject.getString("oid");
					    DomainObject dragObject;
					    
					    String multipleRevisionValidationOnDrop = multipleRevisionValidationOnDrop(context,jDragObjects);
					    if(!"".equals(multipleRevisionValidationOnDrop))
					    {
					    	ret.put("message", multipleRevisionValidationOnDrop);
						    ret.put("result", "false");
					    	return ret;
					    }
					    
					    
					    String locale = context.getSession().getLanguage();
					    
					    String attrSparePart = PropertyUtil.getSchemaProperty(context,"attribute_SparePart");
					    					
						StringList objSel = new StringList();
						objSel.add(EngineeringConstants.SELECT_ID);
						objSel.add(EngineeringConstants.SELECT_TYPE);
						objSel.add(EngineeringConstants.SELECT_NAME);
						objSel.add(EngineeringConstants.SELECT_REVISION);
						objSel.add(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);
						objSel.add(EngineeringConstants.SELECT_POLICY);
						objSel.add(EngineeringConstants.SELECT_CURRENT);
						objSel.add(EngineeringConstants.SELECT_ATTRIBUTE_UNITOFMEASURE);
						objSel.add("attribute["+PropertyUtil.getSchemaProperty(context,"attribute_IsVersion")+"]");
						objSel.add("attribute["+attrSparePart+"]");
						
						Part dropParentPart = new Part(dropObjectId);
						Map dropParentinfo = dropParentPart.getInfo(context, objSel);
						Map dropChildinfo = null;
					    
						boolean result = true;
						String dropAllowed = "";
					    

					    HashMap argMap = new HashMap();
						argMap.put("objectId", dropObjectId);
						
						dropAllowed = isDropObjectInPri(context,dropParentPart,dropParentinfo);
						if(!"".equals(dropAllowed))
						{
							ret.put("message", dropAllowed);
							ret.put("result", "fail");
						    return ret;
						}
					    
						boolean applyAllow = (Boolean)JPO.invoke(context,"emxENCActionLinkAccess",null,"isApplyAllowed",JPO.packArgs(argMap),Boolean.class);
					    if(!applyAllow){
					    	ret.put("result", "fail");
							ret.put("message", EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.DragDrop.Message.NoModifyAccess",locale));
							return ret;
					    }
					    
		
					  //Check for configured part
				    	boolean isXCEInstalled = FrameworkUtil.isSuiteRegistered(context,
								"appVersionEngineeringConfigurationCentral", false, null, null);
				    	
			    		if(isXCEInstalled ){
			    			if("PUEUEBOM".equalsIgnoreCase(dropWindow) && EngineeringConstants.POLICY_EC_PART.equals(dropParentinfo.get(EngineeringConstants.SELECT_POLICY))){
			    				ret.put("result", "fail");
							    ret.put("message", EnoviaResourceBundle.getProperty(context, "emxUnresolvedEBOMStringResource", context.getLocale(),"emxUnresolvedEBOM.CommonView.Alert.Invalidselection"));
							    return ret;
			    			}
			    			if(EngineeringConstants.POLICY_CONFIGURED_PART.equals(dropParentinfo.get(EngineeringConstants.SELECT_POLICY))){
				    			boolean isInWipMode	= true;
						    	String  isWipBomAllowed  = EnoviaResourceBundle.getProperty(context,"emxUnresolvedEBOM.WIPBOM.Allowed");
						    	isInWipMode = ("true".equalsIgnoreCase(isWipBomAllowed) && "Development".equalsIgnoreCase((String)dropParentinfo.get(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE)))?true:false;
						    	if(!isInWipMode){
								    ret.put("result", "fail");
								    ret.put("message", EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.DragDrop.PartReleasePhaseCheck",locale));
								    return ret;
						    	}
					    	}
				    	}
				    	
					    for (int i=0; i<jDragObjects.length(); i++) {
					    	JSONObject jDragObject = jDragObjects.getJSONObject(i);
					    	String dragObjectId = DomainObject.newInstance(context,(String)jDragObject.getString("oid")).getInfo(context, DomainObject.SELECT_ID);
						    String dragObjectcRelId = jDragObject.getString("rid");
					    	dragObject = new DomainObject(dragObjectId);
					    	dropChildinfo = dragObject.getInfo(context, objSel);
						    dropAllowed = allowDropValidation(context,objSel,dropChildinfo,dropParentPart);
					    	if(!("".equals(dropAllowed))){
					    		result = false;
					    		break;
					    	}
					    	if("Move".equalsIgnoreCase(dropAction) && UIUtil.isNotNullAndNotEmpty(dragObjectcRelId))
					    	{
					    		String[] relId = {dragObjectcRelId};
					    		MapList relDetail = DomainRelationship.getInfo(context, relId, new StringList("from.current"));
					    		if(relDetail.size()>0){
					    			Map dragFromparent = (Map) relDetail.get(0);
					    			if(dragFromparent.size()>0 && !dragFromparent.get("from."+EngineeringConstants.SELECT_CURRENT).equals(EngineeringConstants.STATE_EC_PART_PRELIMINARY)){
					    				String[] messageValues = new String[4];
							            messageValues[0] = UINavigatorUtil.getAdminI18NString("Type", (String)dropParentinfo.get(EngineeringConstants.SELECT_TYPE), locale.toString());
							            messageValues[1] = (String)dropParentinfo.get(EngineeringConstants.SELECT_NAME);
							            messageValues[2] = (String)dropParentinfo.get(EngineeringConstants.SELECT_REVISION);
							            messageValues[3] = messageValues[3] = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",context.getLocale(),"emxFramework.State.EC_Part.Preliminary");
							            dropAllowed = MessageUtil.getMessage(context,null,
					                         "emxEngineeringCentral.DragDrop.NotPreliminaryParentPart.message",
					                         messageValues,null,
					                         context.getLocale(),"emxEngineeringCentralStringResource");
//					    				dropAllowed = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.DragDrop.NotPreliminaryParentPart.message3",locale);
//					    				dropAllowed = dropAllowed.replace("TYPE",(String) dropChildinfo.get(EngineeringConstants.SELECT_TYPE));
//					    				dropAllowed = dropAllowed.replace("NAME",(String) dropChildinfo.get(EngineeringConstants.SELECT_NAME));
//					    				dropAllowed = dropAllowed.replace("REVISION",(String) dropChildinfo.get(EngineeringConstants.SELECT_REVISION));
					    				result = false;
							    		break;
					    			}
					    		}
					    	}
					    }
					    
					    if(result && applyAllow)
					    { 
						    ret = connectDropedObjects(context, jDropObject,jDragObjects,objSel,dropParentPart,dropAction);
					    	return ret;
					    }
					    
					    ret.put("message", dropAllowed);
					    ret.put("result", "fail");
					    
					}
					 catch (Exception e)
			        {
					   ret.put("result", "fail");
					   ret.put("message", e.toString());
			           throw new Exception(e.toString());
			        }
		    return ret;
		}
		private String getParentRowIdInfo(String rowId) {
	         String[] emxTableRowId = StringUtils.split(rowId,",");
	         String newRowId = "";
	         for(int i=0; i<emxTableRowId.length; i++) {
	             if(i == 0){
	                 newRowId = emxTableRowId[i];
	             } else {
	                 newRowId = newRowId+","+emxTableRowId[i];
	             }
	         }
	         return "|||"+newRowId;
	     }

		
}



