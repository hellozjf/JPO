/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.engineering.Part;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import com.matrixone.jsystem.util.StringUtils;

public class emxECSearchMassUpdateBase_mxJPO extends emxDomainObject_mxJPO{
	/**
	 *
	/**
	 * This method is executed if a specific method is not specified.
	 *
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args holds no arguments.
	 * @return int.
	 * @throws Exception if the operation fails.
	 * @since EC 10.0.0.0.
	 */
	public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception("must specify method on search invocation");
        }
        return 0;
    }
	/** Simple date format */
	 public emxECSearchMassUpdateBase_mxJPO (Context context, String[] args)
     throws Exception
 {
     super(context,args);
 }

	/* Method to update Attribute based columns in table  */
	public Boolean updateTableColumn(Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap)programMap.get("paramMap");

		// get the columnMap
		HashMap columnMap = (HashMap)programMap.get("columnMap");
		// get the settingsMap
		HashMap settingsMap = (HashMap)columnMap.get("settings");
		// get the Update Program Arguments
        
        //Get the requestMap
        //IR-046329V6R2011
        HashMap requestMap = (HashMap)programMap.get("requestMap");
		String strAdminType=(String)settingsMap.get("Admin Type");
		String strFormat = (String) settingsMap.get("format");

		String objectId  = (String)paramMap.get("objectId");

		String newCLValue = (String)paramMap.get("New Value");

        DomainObject domObj = new DomainObject(objectId);

		if ("date".equals(strFormat)) {
        	
            //Modified for IR-033986V6R2011-Starts
            if(newCLValue!=null && !newCLValue.equalsIgnoreCase("")){
            //IR-046329V6R2011-Starts
            double iClientTimeOffset = (new Double((String) requestMap.get("timeZone"))).doubleValue();
            String endEffectivityDate= eMatrixDateFormat.getFormattedInputDate(newCLValue,iClientTimeOffset,(java.util.Locale)(requestMap.get("locale")));
            //IR-046329V6R2011 - Ends
	    domObj.setAttributeValue(context, PropertyUtil.getSchemaProperty(context,strAdminType), endEffectivityDate);
            }else{
                domObj.setAttributeValue(context, PropertyUtil.getSchemaProperty(context,strAdminType), newCLValue);
            }
            //Modified for IR-033986V6R2011-Ends
		} else {
			if("numeric".equals(strFormat))
			{
				int i=newCLValue.indexOf(',');
				if(i>-1)
				{
					newCLValue=StringUtils.replace(newCLValue, ",", ".");
					if(newCLValue.indexOf(".0")>-1)
					{
					newCLValue=newCLValue.substring(0, newCLValue.length()-2);
        }
        }

				domObj.setAttributeValue(context, PropertyUtil.getSchemaProperty(context,strAdminType),newCLValue);
			}
			else
			{
			domObj.setAttributeValue(context, PropertyUtil.getSchemaProperty(context,strAdminType), newCLValue);
			}
        }
		return Boolean.TRUE;
	}

	/* Method to update Attribute based columns in table  */
	public Boolean updateTableColumnDescription(Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap)programMap.get("paramMap");

		String objectId  = (String)paramMap.get("objectId");

		String newCLValue = (String)paramMap.get("New Value");

		DomainObject domObj = new DomainObject(objectId);
		domObj.setDescription(context, newCLValue);

		return Boolean.TRUE;
	}

	/* Method to update vault in table column */
	public void updateVault(Context context, String[] args)throws Exception {
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap)programMap.get("paramMap");
		// get the columnMap
		HashMap columnMap = (HashMap)programMap.get("columnMap");
		String strColumnLabel = (String)columnMap.get("label");
		String strNewVault = (String)paramMap.get("New Value");

		String strObjectId = (String)paramMap.get("objectId");
		//Multitenant
		String strError = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.TypeAheadError.Message");
		try
		{
			String cmd = "list vault $1" ;
			String strVaultList = MqlUtil.mqlCommand(context, cmd, strNewVault);
			if (strNewVault!=null && !strNewVault.equals("") && strNewVault.equals(strVaultList))
			{
				DomainObject dObj = DomainObject.newInstance(context,strObjectId);
				dObj.setVault(context, strNewVault);
			}
			else
	        {
	        	MqlUtil.mqlCommand(context,"notice $1",strError+" "+strColumnLabel);
	        }
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

    }


	/* Method to update Owner in table column */
	public boolean updateOwner(Context context, String[] args)throws Exception {
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap)programMap.get("paramMap");
		String objectId  = (String)paramMap.get("objectId");
		DomainObject dobjPart=DomainObject.newInstance(context,objectId);
		// get the columnMap
		HashMap columnMap = (HashMap)programMap.get("columnMap");
		String strColumnLabel = (String)columnMap.get("label");
		String strNewOwner = (String)paramMap.get("New Value");
		//Multitenant
		String strError = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.TypeAheadError.Message");
		String[] params = new String[1];
        params[0]= strNewOwner;
        emxTypeAheadSearchEdit_mxJPO tempObject= new emxTypeAheadSearchEdit_mxJPO();
        strNewOwner=tempObject.isObject(context,params);
        if(strNewOwner!= null && !strNewOwner.equals("") && !strNewOwner.equals("null"))
        {
        DomainObject dobjOwner=DomainObject.newInstance(context,strNewOwner);
		strNewOwner= dobjOwner.getInfo(context, DomainConstants.SELECT_NAME);

		try{

			dobjPart.open(context);
			dobjPart.setOwner(context, strNewOwner);

		}catch(Exception e){
			e.printStackTrace();
		}
		finally
        {
			dobjPart.close(context);
        }
       }
        else
        {
        	MqlUtil.mqlCommand(context,"notice $1",strError+" "+strColumnLabel);
        }
       return true;
	}

	/* Method to get selected objects in Search page to MassUpdate Edit Page */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getPartSearchEditDetails(Context context,String args[])throws Exception
	{
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap requestValuesMap = (HashMap)programMap.get("RequestValuesMap");
		String [] Ids      = (String[])requestValuesMap.get("objIds");
		String strIds =Ids[0];
		//Modified for 025584
        //IR-036950 - Starts
        StringList slIds = FrameworkUtil.split(strIds,"~");
        //IR-036950 - Ends
		String strID="";
		Integer len = new Integer(slIds.size()-1);
		MapList mp=new MapList(3);
		mp.add(0, len );
		MapList returnList = new MapList(3);
		
		String fulltextsearch=(String)programMap.get("fullTextSearch");
		if(!"false".equals(fulltextsearch)){
			returnList.add(0, len);
		}
		//returnList.add(0, len);
		StringList  slRelNameSelect = new StringList();
		slRelNameSelect.add(DomainConstants.SELECT_NAME);
		slRelNameSelect.add(DomainConstants.SELECT_CURRENT);
		slRelNameSelect.add(DomainConstants.SELECT_ID);
		slRelNameSelect.add("current.access[modify]");
		slRelNameSelect.add(DomainConstants.SELECT_POLICY);
		String strAccessValue="emxEngineeringCentral.Common.EditAccess";
		//Multitenant
		strAccessValue = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),strAccessValue);
		StringList tempList= new StringList();
		String strOId = "";
		for(int i=0;i<slIds.size();i++)
		{
			strOId =(String) slIds.get(i);
			if(strOId.length() >0)
				tempList.add(strOId);
		}
		String[] arrObjectIds= new String[tempList.size()];
		tempList.toArray(arrObjectIds);
		//tempList.s
		
		String propAllowLevelEC =  (String)FrameworkProperties.getProperty(context, "emxEngineeringCentral.Part.RestrictPartEdit");
		StringList propAllowLevelListEC = new StringList();
		
          if(propAllowLevelEC != null && !"null".equals(propAllowLevelEC) && propAllowLevelEC.length() > 0)
          {
            StringTokenizer stateTok = new StringTokenizer(propAllowLevelEC, ",");
            while (stateTok.hasMoreTokens())
             {
                String tok = (String)stateTok.nextToken();
                propAllowLevelListEC.add("["+FrameworkUtil.lookupStateName(context, DomainConstants.POLICY_EC_PART, tok)+"]");
             }
          } 
	      String propAllowLevelDev = (String)FrameworkProperties.getProperty(context, "emxEngineeringCentral.Part.RestrictDevelopmentPartEdit");
          StringList propAllowLevelListDev = new StringList();

          if(propAllowLevelDev != null && !"null".equals(propAllowLevelDev) && propAllowLevelDev.length() > 0)
          {
            StringTokenizer stateToken = new StringTokenizer(propAllowLevelDev, ",");
            while (stateToken.hasMoreTokens())
             {
                String token = (String)stateToken.nextToken();
                propAllowLevelListDev.add("["+FrameworkUtil.lookupStateName(context, DomainConstants.POLICY_DEVELOPMENT_PART, token)+"]");
             }
          }  

		BusinessObjectWithSelectList BOselectList=BusinessObject.getSelectBusinessObjectData(context, arrObjectIds, slRelNameSelect);
		for(int j=0;j<BOselectList.size();j++)
		{
			String strState="";
			BusinessObjectWithSelect bows = BOselectList.getElement(j);
			strState =  bows.getSelectDataList((String)slRelNameSelect.elementAt(1)).toString();
			strID =  bows.getSelectDataList((String)slRelNameSelect.elementAt(2)).toString();
			String strAccess =  bows.getSelectDataList((String)slRelNameSelect.elementAt(3)).toString();
			
			String objPolicy = bows.getSelectDataList((String)slRelNameSelect.elementAt(4)).toString();
			objPolicy = StringUtils.replace(objPolicy, "[", "");
			objPolicy = StringUtils.replace(objPolicy, "]", "");
			
	          
		
			strID = StringUtils.replace(strID, "[", "");
			strID = StringUtils.replace(strID, "]", "");

			HashMap ObjectMap=new HashMap();
			if(strState.equals("["+DomainConstants.STATE_PART_RELEASE+"]") || strAccess.equals(strAccessValue))
			{

				ObjectMap.put("RowEditable", "readonly");
			}
			else if(objPolicy.equals(DomainConstants.POLICY_DEVELOPMENT_PART))
			{		
				if(propAllowLevelListDev != null && propAllowLevelListDev.contains(strState))
				{
					ObjectMap.put("RowEditable", "readonly");
				}			
			} 
			else if (objPolicy.equals(DomainConstants.POLICY_EC_PART)) 
			{
				if(propAllowLevelListEC != null && propAllowLevelListEC.contains(strState))
				{
					ObjectMap.put("RowEditable", "readonly");
				}			
			}

			ObjectMap.put(DomainConstants.SELECT_ID,strID);
			returnList.add(ObjectMap);

		}
		return returnList;
	}

	/**
	 * This method provides the generic code for update of relationship based columns. The column that is updated could be displaying
	 * any object connected to object shown in the Structure browser row.
	 * eg. BOM power view can have column showing the Design Responsibility connected to the part in the row in one of the column.
	 * This column can be updated using this JPO.
	 * This method needs to be used along with 'Update Program Arguments' with value like 'relationship=<symbolic_name_of_relationship>,isFrom=true/false'
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains the 'Update Program Arguments'
	 * @returns true/false
	 * @throws Exception if the operation fails
	 * @since Common 10.6
	 * @grade 0
	 */
	public int updateConnectedObject(Context context, String[] args) throws Exception {
		String relationshipName="";
		String strSymbolicNameRel="";
		try
		{
			// un-pack the arguments
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			// get the papamMap
			HashMap paramMap = (HashMap)programMap.get("paramMap");
			// get the columnMap
			HashMap columnMap = (HashMap)programMap.get("columnMap");
			// get the settingsMap
			HashMap settingsMap = (HashMap)columnMap.get("settings");
			String strColumnLabel = (String)columnMap.get("label");
			// get the Update Program Arguments
			String strError = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.TypeAheadError.Message");
			String strUpdateProgramArguments=(String)settingsMap.get("Update Program Arguments");
			if(strUpdateProgramArguments!=null && !strUpdateProgramArguments.equals("") && !strUpdateProgramArguments.equals("null") &&  strUpdateProgramArguments.length()!=0)
			{
				// Parse the string to get the required values
				relationshipName=strUpdateProgramArguments.substring(strUpdateProgramArguments.indexOf("relationship=")+ "relationship=".length(), strUpdateProgramArguments.indexOf(','));
				if(relationshipName.equalsIgnoreCase("relationship_ClassifiedItem")){
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING); //366577
				}
				strSymbolicNameRel=relationshipName;
				relationshipName = PropertyUtil.getSchemaProperty(context,relationshipName);

				int i = strUpdateProgramArguments.indexOf("isFrom=")+ "isFrom=".length();
				String strFrom=strUpdateProgramArguments.substring(i, i+"true".length());

			// get the existing connection id
String relId  = "";

// get the object id
String sObjectId  = (String)paramMap.get("objectId");
   // get the new object id for connecting
				String newObjectId = (String)paramMap.get("New Value");
				String strOrg = newObjectId;//Added for 216979

				DomainObject contextObject = new DomainObject(sObjectId);
				if(newObjectId != null && !newObjectId.equals("") && !newObjectId.equals("null") &&  newObjectId.length()!=0)
				{
				String[] params = new String[1];
                params[0]= newObjectId;
                emxTypeAheadSearchEdit_mxJPO tempObject= new emxTypeAheadSearchEdit_mxJPO();
                newObjectId=tempObject.isObject(context,params);
                if(newObjectId== null || newObjectId.equals("null") ||newObjectId.equals("")){
                	MqlUtil.mqlCommand(context,"notice $1",strError+" "+strColumnLabel);
                	return 1;
                    }
				}
				// IR- HF-028635V6R2010x starts

				String strType = contextObject.getInfo(context, DomainConstants.SELECT_TYPE) ;

				if(relationshipName.equals(DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY) && strType.equals(DomainConstants.TYPE_SKETCH))
				{
					String error=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ConnectionError.Message",context.getSession().getLanguage());
					MqlUtil.mqlCommand(context,"notice $1",error);
					return 1;
				}
				else
				{
           // check the direction of connection and connect accordingly
				if("true".equals(strFrom))
				{
				if (DomainConstants.RELATIONSHIP_CLASSIFIED_ITEM.equalsIgnoreCase(relationshipName)) {
					
					relId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",sObjectId,"to["+ DomainConstants.RELATIONSHIP_CLASSIFIED_ITEM +"|from.type=='"+ DomainConstants.TYPE_PART_FAMILY +"'].id");
						
					}
				else {
					relId = contextObject.getInfo(context,"to[" + relationshipName + "].id");
					}
					connectedObject(context,newObjectId,relationshipName,sObjectId,relId,true) ;
					//Added for IR-216979 start  		
					if(relationshipName.equals(DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY)){
				  		if(UIUtil.isNotNullAndNotEmpty(strOrg)) {
				  			contextObject.setPrimaryOwnership(context, EngineeringUtil.getDefaultProject(context), strOrg); 
				  		} else {
				  			contextObject.setPrimaryOwnership(context, EngineeringUtil.getDefaultProject(context), EngineeringUtil.getDefaultOrganization(context));
				  		}
					}
			  		//Added for IR-216969 End
				}
				else
				{
					relId = contextObject.getInfo(context,"from[" + relationshipName + "].id");
					connectedObject(context,newObjectId,relationshipName,sObjectId,relId,false) ;
				}
				}
// IR- HF-028635V6R2010x ends
			}
			else
			{
				String error = "No Update Program Arguments setting on column.Please enter values as: relationship=<symbolic name of relationship>,isFrom=<true/false>";
				MqlUtil.mqlCommand(context,"notice $1",error);
				return 1;
			}

			if(strSymbolicNameRel.equalsIgnoreCase("relationship_ClassifiedItem")){
				 ContextUtil.popContext(context);
			}
}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
}

		return 0;
}

	private boolean connectedObject(Context context,String newObjectId,String relationshipName,String sObjectId,String relId,boolean isFrom) throws Exception {

		StringList selectRelStmts = new StringList();
		selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
		//Modified for IR-048513V6R2012x, IR-118107V6R2012x start
		String sAttrArgUnitPrice = PropertyUtil.getSchemaProperty(context,"attribute_AgreedUnitPrice");
		String sAttrRTSID = PropertyUtil.getSchemaProperty(context,"attribute_RTSID");
		String sAttrShowSubCom = PropertyUtil.getSchemaProperty(context,"attribute_ShowSubComponents");
		String sAttrShowTarCost = PropertyUtil.getSchemaProperty(context,"attribute_ShowTargetCost");
		String sAttrSrcQuaSts = PropertyUtil.getSchemaProperty(context,"attribute_SourceQualificationStatus");
		String sAttrSrcSelSts = PropertyUtil.getSchemaProperty(context,"attribute_SourceSelectionStatus");
		String sAttrSubComLev = PropertyUtil.getSchemaProperty(context,"attribute_SubComponentLevel");

		selectRelStmts.addElement("attribute["+sAttrArgUnitPrice+"]");
		selectRelStmts.addElement("attribute["+DomainConstants.ATTRIBUTE_COMMENTS+"]");
		selectRelStmts.addElement("attribute["+sAttrRTSID+"]");
		selectRelStmts.addElement("attribute["+sAttrShowSubCom+"]");
		selectRelStmts.addElement("attribute["+sAttrShowTarCost+"]");
		selectRelStmts.addElement("attribute["+sAttrSrcQuaSts+"]");
		selectRelStmts.addElement("attribute["+sAttrSrcSelSts+"]");
		selectRelStmts.addElement("attribute["+sAttrSubComLev+"]");
       //Modified for IR-048513V6R2012x, IR-118107V6R2012x end
        StringList selectStmts = new StringList();
        selectStmts.addElement(SELECT_ID);

        DomainObject newRelObj = new DomainObject(sObjectId);
		String strRelId = "";
		String strAgreedUnitPrice = "";
		String strComments = "";
		String strRTSID ="";
		String strShowSubComponents = "";
		String strShowTargetCost = "";
		String strSourceQualificationStatus = "";
		String strSourceSelectionStatus = "";
		String strSubComponentLevel = "";

		HashMap hmRelAttributesMap = new HashMap();
		if(relationshipName.equals(PropertyUtil.getSchemaProperty(context,"relationship_DesignResponsibility")))
		{
			//Modified for IR-048513V6R2012x, IR-118107V6R2012x start
			MapList mlRelObjects = newRelObj.getRelatedObjects(context,
									DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY,
									"*",
									selectStmts,
				   					selectRelStmts,
									true,
									false,
									(short)1,
									null,
									null,
									0);
			//Modified for IR-048513V6R2012x, IR-118107V6R2012x end

			if(mlRelObjects!=null && !mlRelObjects.isEmpty())
			{
			 strRelId = (String)((Map)mlRelObjects.get(0)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
			//Modified for IR-048513V6R2012x, IR-118107V6R2012x start

				strAgreedUnitPrice = (String)((Map)mlRelObjects.get(0)).get("attribute["+sAttrArgUnitPrice+"]");
				strComments = (String)((Map)mlRelObjects.get(0)).get("attribute["+DomainConstants.ATTRIBUTE_COMMENTS+"]");
				strRTSID = (String)((Map)mlRelObjects.get(0)).get("attribute["+sAttrRTSID+"]");
				strShowSubComponents = (String)((Map)mlRelObjects.get(0)).get("attribute["+sAttrShowSubCom+"]");
				strShowTargetCost = (String)((Map)mlRelObjects.get(0)).get("attribute["+sAttrShowTarCost+"]");
				strSourceQualificationStatus = (String)((Map)mlRelObjects.get(0)).get("attribute["+sAttrSrcQuaSts+"]");
				strSourceSelectionStatus = (String)((Map)mlRelObjects.get(0)).get("attribute["+sAttrSrcSelSts+"]");
				strSubComponentLevel = (String)((Map)mlRelObjects.get(0)).get("attribute["+sAttrSubComLev+"]");


				hmRelAttributesMap.put(sAttrArgUnitPrice,strAgreedUnitPrice);
				hmRelAttributesMap.put(DomainConstants.ATTRIBUTE_COMMENTS,strComments);
				hmRelAttributesMap.put(sAttrRTSID,strRTSID);
				hmRelAttributesMap.put(sAttrShowSubCom,strShowSubComponents);
				hmRelAttributesMap.put(sAttrShowTarCost,strShowTargetCost);
				hmRelAttributesMap.put(sAttrSrcQuaSts,strSourceQualificationStatus);
				hmRelAttributesMap.put(sAttrSrcSelSts,strSourceSelectionStatus);
				hmRelAttributesMap.put(sAttrSubComLev,strSubComponentLevel);

			  //Modified for IR-048513V6R2012x, IR-118107V6R2012x end
			}

		}

		DomainObject domnewObjectId = new DomainObject(newObjectId);
		DomainObject domsObjectId = new DomainObject(sObjectId);
		DomainRelationship domRel = new DomainRelationship();
		if(relId==null ||relId.equals("") || relId.equals("null") )
		{
			if( newObjectId==null ||newObjectId.equals(" ") || newObjectId.equals("null")|| newObjectId.equals("") )
			{
				return false;
						}
			else
			{
				DomainRelationship.connect(context,newObjectId,relationshipName,sObjectId,true);
				return  true;
			}
		}
		else
		{
				if(newObjectId==null || newObjectId.equals("") || newObjectId.equals(" ") || newObjectId.equals("null"))
			{
							DomainRelationship.disconnect(context, relId);
				return  true;
			}
			else
			{
							ContextUtil.startTransaction(context, true);
				if(isFrom){
					DomainRelationship.disconnect(context, relId);
					domRel = DomainRelationship.connect(context,domnewObjectId,relationshipName,domsObjectId);

					if(relId.equals(strRelId))
					{
						domRel.setAttributeValues(context, hmRelAttributesMap);
					}
				}
				else{
					DomainRelationship.disconnect(context, relId);
					domRel = DomainRelationship.connect(context,domsObjectId,relationshipName,domnewObjectId);
					if(relId.equals(strRelId))
					{
						domRel.setAttributeValues(context, hmRelAttributesMap);
					}
				}
							ContextUtil.commitTransaction(context);
				return true;
						}
					}

				}


/*
     * Added this is for IR-025583V6R2011
     * Method to display All the policies in Policy drop down column
     * Arguments should have symbolic relationship name and isFrom connection
     * true/false
     */
    public HashMap getPolicies (Context context, String[] args)
        throws Exception
    {

        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap rangeMap = new HashMap();
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        String objectId  = (String)paramMap.get("objectId");
        DomainObject doObj = new DomainObject(objectId);
        String strTypeName = doObj.getInfo(context,DomainConstants.SELECT_TYPE);
        String strDrawType =   PropertyUtil.getSchemaProperty(context,"type_DrawingPrint");
        String strCADModelType =   PropertyUtil.getSchemaProperty(context,"type_CADModel");
        String strSketchType =   PropertyUtil.getSchemaProperty(context,"type_Sketch");
        StringList columnVals = new StringList();
        StringList columnVals_Choices = new StringList();
        String strCADDrawingPolicy =   PropertyUtil.getSchemaProperty(context,"policy_CADDrawing");
        String strCADModelPolicy =   PropertyUtil.getSchemaProperty(context,"policy_CADModel");
        String strDesignPolicy = PropertyUtil.getSchemaProperty(context,"policy_DesignPolicy");
        String strDrawPolicy = PropertyUtil.getSchemaProperty(context,"policy_DrawingPrint");
        String strSketchPolicy = PropertyUtil.getSchemaProperty(context,"policy_ECRSupportingDocument");
        if(!"".equals(strTypeName) && !"null".equals(strTypeName) && strDrawType.equals(strTypeName))
        {
            columnVals.add(strDrawPolicy);
            columnVals_Choices.add(strDrawPolicy);
        }
        else if((!"".equals(strTypeName) && !"null".equals(strTypeName) && strSketchType.equals(strTypeName)))
        {
        	 columnVals.add(strSketchPolicy);
             columnVals_Choices.add(strSketchPolicy);
        }
        else if((!"".equals(strTypeName) && !"null".equals(strTypeName) && strCADModelType.equals(strTypeName)))
        {
            columnVals.add(strCADModelPolicy);
            columnVals.add(strDesignPolicy);
            columnVals_Choices.add(strCADModelPolicy);
            columnVals_Choices.add(strDesignPolicy);
        }
        else
        {
            columnVals.add(strCADDrawingPolicy);
            columnVals.add(strDesignPolicy);
            columnVals_Choices.add(strCADDrawingPolicy);
            columnVals_Choices.add(strDesignPolicy);
        }

        rangeMap.put("field_choices",columnVals_Choices);
        rangeMap.put("field_display_choices", columnVals );

        return rangeMap;

    }
 // Method to update the policy
public void updatePolicy(Context context, String[] args)
throws Exception
{

    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    HashMap paramMap = (HashMap)programMap.get("paramMap");

    String objectId  = (String)paramMap.get("objectId");
    String newCLValue = (String)paramMap.get("New Value");
    DomainObject doObj = new DomainObject(objectId);

    doObj.setPolicy(context,newCLValue);

}
}

