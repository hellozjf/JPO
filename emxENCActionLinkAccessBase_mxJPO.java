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
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import matrix.db.Access;
import matrix.db.BusinessObject;
import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.PolicyList;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.dassault_systemes.enovia.bom.ReleasePhase;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PolicyUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.engineering.EBOMFloat;
import com.matrixone.apps.engineering.EngineeringConstants;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.engineering.Part;
import com.matrixone.apps.engineering.PartFamily;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * The <code>emxENCActionLinkAccessBase</code> class contains implementation code for emxENCActionLinkAccess.
 * @version EC 10.5 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxENCActionLinkAccessBase_mxJPO extends emxDomainObject_mxJPO
{
	 /** the "ECO" policy. */
    public static final String POLICY_ECO = PropertyUtil.getSchemaProperty("policy_ECO");

	 /** the "ECR" policy. */
    public static final String POLICY_ECR = PropertyUtil.getSchemaProperty("policy_ECR");

   /** state "Create" for the "ECO" policy. */
    public static final String STATE_ECO_CREATE = PropertyUtil.getSchemaProperty("policy", POLICY_ECO, "state_Create");

    /** state "Define Components" for the "ECO" policy. */
    public static final String STATE_ECO_DEFINE_COMPONENTS = PropertyUtil.getSchemaProperty("policy", POLICY_ECO, "state_DefineComponents");

    public static final String STATE_ECO_RELEASE = PropertyUtil.getSchemaProperty("policy", POLICY_ECO, "state_Release");

    /** state "Design Work" for the "ECO" policy. */
    public static final String STATE_ECO_DESIGN_WORK = PropertyUtil.getSchemaProperty("policy", POLICY_ECO, "state_DesignWork");

    /** A string constant with the value ECR state_Evaluate */
    public static final String SYMBOLIC_NAME_FOR_STATE_ECR_EVALUATE = "state_Evaluate";

    /** A string constant with the value ECO state_DesignWork */
    public static final String SYMBOLIC_NAME_FOR_STATE_ECO_DESIGN_WORK = "state_DesignWork";

 /** A string constant with the value MECO state_Review */
    public static final String SYMBOLIC_NAME_FOR_STATE_MECO_REVIEW = "state_Review";
    //373962 start
    public static final String SYMBOLIC_NAME_FOR_STATE_MECO_CREATE = "state_Create";
    //373962 end
 /** A string constant with the value DCR state_Review */
    public static final String SYMBOLIC_NAME_FOR_STATE_DCR_REVIEW = "state_Review";

    public static final String TYPE_PART_MARKUP                = PropertyUtil.getSchemaProperty("type_PARTMARKUP");
    /** state "Plan ECO" for the "ECR" policy. */
    public static final String STATE_ECR_PLAN_ECO = PropertyUtil.getSchemaProperty("policy", POLICY_ECR, "state_PlanECO");

	/** A string constant with the value ECR state_Review */
    public static final String SYMBOLIC_NAME_FOR_STATE_ECR_REVIEW = "state_Review";

    public static final String ATTRIBUTE_REFERENCE_TYPE = PropertyUtil.getSchemaProperty("attribute_ReferenceType");
	public static final String RELATIONSHIP_CLASSIFIED_ITEM = PropertyUtil.getSchemaProperty("relationship_ClassifiedItem");
	public static final String RELATIONSHIP_PART_FAMILY_REFERENCE = PropertyUtil.getSchemaProperty("relationship_PartFamilyReference");
    public static final String RELATIONSHIP_AFFECTED_ITEM = PropertyUtil.getSchemaProperty("relationship_AffectedItem");
    
    public static final String SYMB_M = "M" ;
    public static final String SYMB_R = "R" ;

	public static final String POLICY_PARTMARKUP = PropertyUtil.getSchemaProperty("policy_PartMarkup");
    public static final String POLICY_EBOM_MARKUP = PropertyUtil.getSchemaProperty("policy_EBOMMarkup");
	public static final String proposedState = PropertyUtil.getSchemaProperty("policy", POLICY_PARTMARKUP, "state_Proposed");
	public static final String approvedState = PropertyUtil.getSchemaProperty("policy", POLICY_PARTMARKUP, "state_Approved");

    public static final String RELATIONSHIP_PROPOSED_MARKUP = PropertyUtil.getSchemaProperty("relationship_ProposedMarkup");
    public static final String RELATIONSHIP_APPLIED_MARKUP = PropertyUtil.getSchemaProperty("relationship_AppliedMarkup");
    public static final String RELATIONSHIP_APPLIED_PART_MARKUP = PropertyUtil.getSchemaProperty("relationship_AppliedPartMarkup");
    //373962 start
    /* A string constant with the value state_Create. */
    public static final String SYMB_CREATE = "state_Create";
   //373962 end
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     * @since EC 10.5.
     *
     */
    public emxENCActionLinkAccessBase_mxJPO (Context context, String[] args)
      throws Exception
    {
        super(context, args);

    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return int.
     * @throws Exception if the operation fails.
     * @since EC 10.5.
     */
    public int mxMain(Context context, String[] args)
      throws Exception
    {
      if (true)
      {
        throw new Exception("must specify metemxENCActionLinkAccess invocation");
      }
      return 0;
    }

     /**
        * Gets the Boolean value of the a parameter.
        *
        * @param context the eMatrix <code>Context</code> object.
        * @param args holds the objectId.
        * @return Boolean.
        * @throws Exception If the operation fails.
        * @since EC 10-5.
        *
        */
        public  Boolean hasDisplayLink(Context context,String[] args)
            throws Exception
        {
          HashMap paramMap = (HashMap)JPO.unpackArgs(args);
          String objectId      = (String) paramMap.get("objectId");
               // For checking polocies.........................
                if(!isTypeAccessable(context,args).booleanValue()){
                    return Boolean.FALSE;
                }

          //creating Part Object
          Part partObj = (Part)DomainObject.newInstance(context,DomainConstants.TYPE_PART,DomainConstants.ENGINEERING);
          partObj.setId(objectId);

          SelectList sPartSelStmts = new SelectList(1);
          sPartSelStmts.add(DomainConstants.SELECT_CURRENT);
          sPartSelStmts.add(DomainConstants.SELECT_POLICY);

          Map objMap = partObj.getInfo(context, sPartSelStmts);
          String state = (String)objMap.get(DomainConstants.SELECT_CURRENT);
          String sPolicy = (String)objMap.get(DomainConstants.SELECT_POLICY);

          boolean bDisplayLinks = false;

          String stateComplete  = FrameworkUtil.lookupStateName(context, DomainConstants.POLICY_DEVELOPMENT_PART, "state_Complete");

          String policyClass = EngineeringUtil.getPolicyClassification(context,sPolicy);
          if("Production".equals(policyClass)) {
            // Display links for EC Part for latest Released rev's
            if(partObj.canAttachECR(context)){
            bDisplayLinks = true;
            }

            } else {
              // Display links for Development Parts for Complete state and property set to false
              String sChangeMgmt = "true";
              
              String sAllowChangeMgmtforMEP  = "false";

              if((state.equalsIgnoreCase(stateComplete) && "false".equalsIgnoreCase(sChangeMgmt))
              || ("Equivalent".equals(policyClass) && "true".equalsIgnoreCase(sAllowChangeMgmtforMEP)) ){
              bDisplayLinks = true;
            }
          }
          return Boolean.valueOf(bDisplayLinks);
      }

    /**
    * Checking the PartFamily object status.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-5.
    *
    */

    public Boolean isPartFamilyObsolete(Context context,String[] args) throws Exception
    {

       HashMap paramMap = (HashMap)JPO.unpackArgs(args);
       Boolean bIsObsolete=Boolean.FALSE;
       if(!isTypeAccessable(context,args).booleanValue()){
        return Boolean.FALSE;
       }
       try {
           String objectId      = (String) paramMap.get("objectId");
          //creating PartFamily object
           PartFamily partFamily = (PartFamily)DomainObject.newInstance(context,DomainConstants.TYPE_PART_FAMILY,DomainConstants.ENGINEERING);
           partFamily.setId(objectId);
           if (partFamily.isNotObsolete(context)) {
           bIsObsolete =Boolean.TRUE;
         }
         }catch (Exception e) {
         throw new Exception(e.toString());
      }
      return bIsObsolete;

    }
     /**
        * Checking the connecting access for tht Part.
        *
        * @param context the eMatrix <code>Context</code> object.
        * @param args holds nothing.
        * @return Boolean.
        * @throws Exception If the operation fails.
        * @since EC10-5.
        *
        */

    public Boolean hasCreateNewAccess(Context context,String[] args) throws Exception
    {
       Boolean hasCreateAccess = Boolean.FALSE;
       try {
           boolean isSourcingCentralInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionSourcingCentral",false,null,null);
           if( isSourcingCentralInstalled ) {
               String type = TYPE_RFQ;
               ContextUtil.pushContext(context);
               MapList policyList = mxType.getPolicies(context, type, true);
               ContextUtil.popContext(context);



               if(policyList.size()>0) {
                   hasCreateAccess = Boolean.TRUE;
               }
           }
      }catch (Exception e) {
         throw new Exception(e.toString());
      }
      return hasCreateAccess;
    }

    /**
     * Checking the Condition for Go To Production action command.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the objectId.
     * @return Boolean.
     * @throws Exception If the operation fails.
     * @since EC10-5.
     *
     */

    public Boolean hasBOMGoToProduction(Context context,String[] args) throws Exception {
   	 //Check for Latest COmplete view form the request map
   	 //If false, return false
   	 //HashMap paramMap = (HashMap)JPO.unpackArgs(args);
   	 //String revFilter= (String)paramMap.get("ENCBOMRevisionCustomFilter");
   	 
        boolean hasAccess = false;
        
        //if ("Latest Complete".equals(revFilter)){
       	 hasAccess = hasGoToProduction(context, args);
        //}
        
        return hasAccess;         
    }
     
    /**
    * Checking the Condition for Go To Production action command.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds the objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-5.
    *
    */

    public Boolean hasGoToProduction(Context context,String[] args) throws Exception
    {

      HashMap paramMap = (HashMap)JPO.unpackArgs(args);
      Boolean hasRequiredAccess;
      String sHasReqAccess= "";

       try {
          String objectIdArray[] = new String[2];
		  objectIdArray[0] = "BGTPCmdAccess";
          objectIdArray[1] = (String) paramMap.get("objectId");
          Map resultMap = hasAccessForGoToProduction(context, objectIdArray);
          sHasReqAccess = (String)resultMap.get("hasRequiredAccess");
          hasRequiredAccess = Boolean.valueOf(sHasReqAccess);
         }catch (Exception e) {
           throw new Exception(e.toString());
         }

      return hasRequiredAccess;
    }

	/**
	 * To check that the required privileges are available with the user for the Go To Production Operation on the Parts.
	 * @param context
	 * @param objectId
	 * @return Boolean
	 * @throws FrameworkException
	 */
	public Map hasAccessForGoToProduction(Context context, String[] objectIdArray) throws Exception {
		String partName;
		String partLatestRev;
		String partPolicyName;
                //Modified for IR-084922V6R2012 	
 		String policyClassification=DomainConstants.EMPTY_STRING;
 		String sPartReleaseProcess = DomainConstants.EMPTY_STRING;     //BGP Changes
		boolean hasAccess = false;

		// Construct the select list
		SelectList busSelects = new SelectList(5);
		busSelects.add(DomainConstants.SELECT_NAME);
		busSelects.add(DomainConstants.SELECT_REVISION);
		busSelects.add(DomainConstants.SELECT_CURRENT);
		busSelects.add(DomainConstants.SELECT_POLICY);
		busSelects.add(DomainConstants.SELECT_STATES);
		busSelects.add(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);  //BGP Changes
		busSelects.add("last");
		busSelects.add("last.current");
		busSelects.add("current.access[revise]");
		busSelects.add("current.access[changepolicy]");
		busSelects.add("current.access[modify]");

		StringBuffer incorrectStateObjs = new StringBuffer();
		StringBuffer hasNoAccesObjs = new StringBuffer();
		StringBuffer notice = new StringBuffer();
		Map resultMap = new HashMap();
		int flag = 0;
		String sParentPartId = null;
		String sParentState = null;
		boolean isECPartWithDevMode = false;
		try{
    		Part part = (Part) DomainObject.newInstance(context,
    				DomainConstants.TYPE_PART, DomainConstants.ENGINEERING);
    
    		if(objectIdArray.length>0 && "BGTPCmdAccess".equals(objectIdArray[0])){
				flag =1;
			}
    		
    		if(null != objectIdArray && objectIdArray.length>1 && "BOMPreReqs".equals(objectIdArray[0])){
				flag =2;
				sParentPartId = objectIdArray[1];
				sParentState = DomainObject.newInstance(context, sParentPartId).getInfo(context, "current"); 
			}
    		
			for (int i = flag; i < objectIdArray.length; i++) {
    			part.setId(objectIdArray[i]);
    
    			// Getting the Part info based on selects
    			Map boInfo = part.getInfo(context, busSelects);
    			partName = (String) boInfo.get(DomainConstants.SELECT_NAME);
    			partPolicyName = (String) boInfo.get(DomainConstants.SELECT_POLICY);
    			//sPartReleaseProcess = (String) boInfo.get(EngineeringConstants.SELECT_RELEASE_PROCESS);    //BGP Changes
    			
    			isECPartWithDevMode = ReleasePhase.isECPartWithDevMode(context, objectIdArray[i]);
    			
    			policyClassification = EngineeringUtil.getPolicyClassification(
    					context, partPolicyName);
    			if("Development".equals(policyClassification)) {
    				// Getting the Part Access Info
    				StringList stateList = new StringList();
                    hasAccess = false;
    				try {
    					String sStates = (String) boInfo.get(DomainConstants.SELECT_STATES);
    					if (sStates != null) {
    						stateList.addElement(sStates);
    					}
    				} catch (ClassCastException classCastEx) {
    					stateList = (StringList) boInfo.get(DomainConstants.SELECT_STATES);
    				}
    
    				partLatestRev = (String) boInfo.get("last");
    				String latestrevcurrent=(String) boInfo.get("last.current");
    				String reviseacc = (String) boInfo.get("current.access[revise]");
    				String policyacc = (String) boInfo.get("current.access[changepolicy]");
    				String modifyacc = (String) boInfo.get("current.access[modify]");
    
    				if ((reviseacc != null && reviseacc.equalsIgnoreCase("TRUE"))
    						&& (policyacc != null && policyacc.equalsIgnoreCase("TRUE"))
    						&& (modifyacc != null && modifyacc.equalsIgnoreCase("TRUE"))) {
    					hasAccess = true;
    				}
    				
    				if(!DomainConstants.STATE_DEVELOPMENT_PART_COMPLETE.equals(latestrevcurrent)){
    					
						if(!(DomainConstants.STATE_DEVELOPMENT_PART_CREATE.equals(latestrevcurrent) || DomainConstants.STATE_DEVELOPMENT_PART_PEER_REVIEW.equals(latestrevcurrent))){
    						if(notice.length()>0){
        						notice.append(',');
        					}
        					notice.append(partName);
        					notice.append(' ');
							notice.append(partLatestRev);
    					}
						else{
    					if(incorrectStateObjs.length()>0){
    						incorrectStateObjs.append(',');
    					}
    					incorrectStateObjs.append(partName);
    					incorrectStateObjs.append(' ');
						incorrectStateObjs.append(partLatestRev);
						}
					}
    				
    				
                    if(!hasAccess && (hasNoAccesObjs.toString()).indexOf(partName)<0) {
    					if(hasNoAccesObjs.length()>0){
    							hasNoAccesObjs.append(',');
                        }
    					hasNoAccesObjs.append(partName);
                    }
    			}
    			else if(isECPartWithDevMode)		//BGP Changes
    			{
    				// Getting the Part Access Info
    				StringList stateList = new StringList();
                    hasAccess = false;
    
    				partLatestRev = (String) boInfo.get("last");
    				String latestrevcurrent=(String) boInfo.get("last.current");
    				String modifyacc = (String) boInfo.get("current.access[modify]");
    
    				if (modifyacc != null && modifyacc.equalsIgnoreCase("TRUE")) {
    					hasAccess = true;
    				}
    				
    				if(flag == 2 && null != sParentState && !sParentState.equals(latestrevcurrent))
    				{
    					if(incorrectStateObjs.length()>0){
    						incorrectStateObjs.append(',');
    					}
    					incorrectStateObjs.append(partName);
    					incorrectStateObjs.append(' ');
						incorrectStateObjs.append(partLatestRev);
    					
    				}
   				
                    if(!hasAccess && (hasNoAccesObjs.toString()).indexOf(partName)<0) {
    					if(hasNoAccesObjs.length()>0){
    							hasNoAccesObjs.append(',');
                        }
    					hasNoAccesObjs.append(partName);
                    }
    				
    			}
    		}
			if(flag == 2 && !("".equals(notice.toString())) && "".equals(incorrectStateObjs.toString()) && "".equals(hasNoAccesObjs.toString())){
    			String strLanguage = context.getSession().getLanguage();    			
    			//resultMap.put("notice", notice.toString());
    			emxContextUtil_mxJPO.mqlNotice(context, EngineeringUtil.i18nStringNow("emxEngineeringCentral.Common.ConfirmMsg", strLanguage)+notice.toString());
    			resultMap.put("hasRequiredAccess", "true");
    		}
    		else if(("".equals(incorrectStateObjs.toString())&& "".equals(hasNoAccesObjs.toString()))){
    			resultMap.put("hasRequiredAccess", "true");
    		} 
			
			else {
    			
    			resultMap.put("incorrectStateObjs", incorrectStateObjs.toString());
    			resultMap.put("hasNoAccesObjs", hasNoAccesObjs.toString());
    			resultMap.put("hasRequiredAccess", "false");
    		}
        } catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		return resultMap;
    }
     /**
        * Checking the condtion for ShowLink.
        *
        * @param context the eMatrix <code>Context</code> object.
        * @param args holds objectId.
        * @return Boolean.
        * @throws Exception If the operation fails.
        * @since EC10-5.
        *
        */
        public  Boolean hasShowLink(Context context,String[] args)
            throws Exception
        {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            String objectId      = (String) paramMap.get("objectId");

            // For checking polocies exists or not
            if(!isTypeAccessable(context,args).booleanValue()){
                return Boolean.FALSE;
            }
            if(!hasReadAccess(context,args).booleanValue()){
              return Boolean.FALSE;
            }
            //creating DomainObject.
            DomainObject obj = DomainObject.newInstance(context);
            obj.setId(objectId);

             //363480
             String nextRevState = obj.getInfo(context, "next.current");
             if(nextRevState != null && nextRevState.equals(DomainConstants.STATE_PART_RELEASE)){
                 return Boolean.FALSE;
             }
             //363480

            StringList select = new StringList(2);

            String strAffectedItemSelect = "to[" + RELATIONSHIP_AFFECTED_ITEM + "].from.id";
            select.addElement(strAffectedItemSelect);

            Map connectedECOs = obj.getInfo(context, select);

            boolean bShowLinks = false;
            boolean bECOConnected = false;
            String sDevelopmentInUse = "true";

            boolean bDevelopmentInUse = false;
            if("true".equals(sDevelopmentInUse)) {
            bDevelopmentInUse = true;
            }
            String sAllowChangeMgmtforMEP = "false";

            boolean bAllowChangeMgmtforMEP = false;
            if("true".equalsIgnoreCase(sAllowChangeMgmtforMEP)) {
            bAllowChangeMgmtforMEP = true;
            }

            String id1 = (String)connectedECOs.get(strAffectedItemSelect);
            if(id1 != null ) {
            bECOConnected = true;
            }

            String policy = EngineeringUtil.getPolicyClassification(context,obj.getPolicy(context).getName());
            if(("Development".equals(policy) && bDevelopmentInUse) || ("Equivalent".equals(policy) && !bAllowChangeMgmtforMEP) ||("Production".equals(policy)) )
            {
              if("Production".equals(policy))
              {
                 if(bECOConnected) {
                 bShowLinks = false;
                 }
                 else {
                 bShowLinks = true;
                 }
              }
              else
              {
                 bShowLinks = false;
              }
            }
            else {
             if(bECOConnected) {
               bShowLinks = false;
             }
             else {
               bShowLinks = true;
             }
            }
             return Boolean.valueOf(bShowLinks);

       }

     /**
        * Gets the Boolean value.
        *
        * @param context the eMatrix <code>Context</code> object.
        * @param args holds objectId.
        * @return Boolean.
        * @throws Exception If the operation fails.
        * @since EC10-5.
        *
        */
        public  Boolean canAttachECR(Context context,String[] args)
            throws Exception
        {
      boolean bCanAttachECR = false;
      try{
      HashMap paramMap = (HashMap)JPO.unpackArgs(args);
      String objectId      = (String) paramMap.get("objectId");
      Part partObj = (Part)DomainObject.newInstance(context, DomainConstants.TYPE_PART,DomainConstants.ENGINEERING);
       partObj.setId(objectId);
      // Role based access
      if ( (partObj.canAttachECR(context))  && hasReadAccess(context,args).booleanValue())
      {
        bCanAttachECR = true;
      }
      }catch(Exception ee)
      {
      }
      return Boolean.valueOf(bCanAttachECR);
      }

     /**
        * Checking to Connect or DisConnect.
        *
        * @param context the eMatrix <code>Context</code> object.
        * @param args holds objectId.
        * @return Boolean.
        * @throws Exception If the operation fails.
        * @since EC10-5.
        *
        */
        public  Boolean isToConnectOrDisConnect(Context context, String[] args)
            throws Exception
        {
          boolean bOkToConnectOrDisconnect = false;
          try{
              if( hasReadAccess(context,args).booleanValue())
                {
          //creating HashMap Object
          HashMap paramMap = (HashMap)JPO.unpackArgs(args);
          String objectId      = (String) paramMap.get("objectId");
          //creating Part Object based on the objectId
          Part part = (Part)DomainObject.newInstance(context, DomainConstants.TYPE_PART,DomainConstants.ENGINEERING);
                                                    part.setId(objectId);
          String policy = part.getPolicy(context).getName();
          String state = part.getInfo(context, SELECT_CURRENT);//getCurrentState(context).getName();
          String type = part.getInfo(context,SELECT_TYPE);
          if( type.equals(DomainConstants.TYPE_PART_FAMILY) ) {
              PartFamily partFamilyObj = (PartFamily)DomainObject.newInstance(context,DomainConstants.TYPE_PART_FAMILY,DomainConstants.ENGINEERING);
              partFamilyObj.setId(objectId);
              if(partFamilyObj.isNotObsolete(context)) {
                  bOkToConnectOrDisconnect = true;
              }
          }
          //Checking for connection
          if (policy.equals(DomainConstants.POLICY_PART) && state.equals(DomainConstants.STATE_PART_PRELIMINARY))
          {
            bOkToConnectOrDisconnect = true;
          }
          else if (policy.equals(DomainConstants.POLICY_DEVELOPMENT_PART) && state.equals(DomainConstants.STATE_DEVELOPMENT_PART_CREATE))
          {
            bOkToConnectOrDisconnect = true;
          }
               }
          }catch(Exception ee){
          }
          return Boolean.valueOf(bOkToConnectOrDisconnect);
         }

    /**
    * Checking the Read access.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since AEF.
    *
    */
    public Boolean hasReadAccess(Context context,String[] args) throws Exception
    {
       HashMap paramMap = (HashMap)JPO.unpackArgs(args);
       boolean bHasReadAccess = false;
       matrix.db.Access mAccess  = null;
       String objectId      = (String) paramMap.get("objectId");
             try {

          DomainObject bosAccess = DomainObject.newInstance(context);
          bosAccess.setId(objectId);
          mAccess = bosAccess.getAccessMask(context);
          if(mAccess.has(Access.cRead))
          {
          bHasReadAccess = true;
          }
        }catch (Exception e) {
         throw new Exception(e.toString());
        }
      return  Boolean.valueOf(bHasReadAccess);

    }

       /**
        * Checking the CheckIn access for the user.
        *
        * @param context the eMatrix <code>Context</code> object.
        * @param args holds objectId.
        * @return Boolean.
        * @throws Exception If the operation fails.
        * @since EC10-5.
        *
        */

    public Boolean hasCheckInAccess(Context context,String[] args) throws Exception
    {
       //getting the parameter Map
       HashMap paramMap = (HashMap)JPO.unpackArgs(args);
       boolean bHasCheckInAccess = false;
       matrix.db.Access mAccess  = null;
       String objectId      = (String) paramMap.get("objectId");
       try {
         //creating the Domain Object
        DomainObject bosAccess = DomainObject.newInstance(context);
        bosAccess.setId(objectId);

        mAccess = bosAccess.getAccessMask(context);
        if(mAccess.has(Access.cCheckin))
        {
        bHasCheckInAccess = true;
        }
      }catch (Exception e) {
         throw new Exception(e.toString());
      }
      return  Boolean.valueOf(bHasCheckInAccess);

    }

     /**
        * Checking the Read  and Checkin access.
        *
        * @param context the eMatrix <code>Context</code> object.
        * @param args holds objectId.
        * @return Boolean.
        * @throws Exception If the operation fails.
        * @since EC10-5.
        *
        */

    public Boolean hasReadAndCheckAccess(Context context,String[] args) throws Exception
    {

       boolean bReadAndCheckAcess = false;
       try {
      if( (hasReadAccess(context,args) ).booleanValue()  && (hasCheckInAccess(context,args)).booleanValue()  && isPartFamilyObsolete(context,args).booleanValue())
      bReadAndCheckAcess = true;

      }catch (Exception e) {
         throw new Exception(e.toString());
      }
      return  Boolean.valueOf(bReadAndCheckAcess);

    }

  /**
    * Checking the Current state.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @param sCurrentState details of the state.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-5.
    *
    */
    public Boolean isCurrentState(Context context,String[] args, String sCurrentState) throws Exception
    {
      HashMap paramMap = (HashMap)JPO.unpackArgs(args);
      String objectId      = (String) paramMap.get("objectId");
      boolean isCurrentState = false;
      try {

        Part part = (Part)DomainObject.newInstance(context, DomainConstants.TYPE_PART,DomainConstants.ENGINEERING);
        part.setId(objectId);
        String state = part.getInfo(context, SELECT_CURRENT);//getCurrentState(context).getName();

        if( state.equals(sCurrentState))
        isCurrentState = true;

      }catch (Exception e) {
        throw new Exception(e.toString());
      }
      return  Boolean.valueOf(isCurrentState);

    }

  /**
    * Checking the Current state.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-5.
    *
    */

    public Boolean  currentState(Context context,String[] args) throws Exception
    {

      boolean bCrrentState = false;
      try{
       if( ( isCurrentState(context,args,DomainConstants.STATE_DEVELOPMENT_PART_COMPLETE) ).booleanValue()   || (isCurrentState(context,args,DomainConstants.STATE_PART_RELEASE) ).booleanValue() )
        bCrrentState= true;
      }catch (Exception e) {
      throw new Exception(e.toString());
      }
      return  Boolean.valueOf(bCrrentState);

    }

   /**
    * Checking the Current state And CheckIn.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-5.
    *
    */
    public Boolean  isCheckInAndCurrentState(Context context,String[] args) throws Exception
    {

      boolean bisCheckAndCrrentState = false;
      try {
            if( !( currentState(context,args) ).booleanValue() && (hasCheckInAccess(context,args)).booleanValue())
              bisCheckAndCrrentState= true;
            }catch (Exception e) {
               throw new Exception(e.toString());
            }
      return  Boolean.valueOf(bisCheckAndCrrentState);
    }

  /**
    * Checking the whether Type has Polocies or not.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-5.
    *
    */

    public Boolean  isTypeAccessable(Context context,String[] args) throws Exception
    {
          boolean isTypeAccessable = false;
          HashMap paramMap = (HashMap)JPO.unpackArgs(args);
          String objectId  = (String) paramMap.get("objectId");
          setId(objectId);
          String type = getInfo(context,SELECT_TYPE);
          MapList policyList = mxType.getPolicies(context, type, true);
          if(policyList.size()>0) {
              isTypeAccessable = true;
          }
          return Boolean.valueOf(isTypeAccessable);
    }

     /**
      * This method is used for showing the Cancel ECO Link.
      * @param context the eMatrix <code>Context</code> object.
      * @param args holds objectId.
      * @return Boolean.
      * @throws Exception If the operation fails.
      * @since AEF Rossini.
      */

       public Boolean getCancelECO(Context context,String[] args) throws Exception
       {
            boolean bGetCancelECO=false;
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            String objectId  = (String) paramMap.get("objectId");
            setId(objectId);
            String hasModifyAccess = getInfo(context,"current.access[modify]");
            if(PolicyUtil.checkState(context, objectId, STATE_ECO_RELEASE, PolicyUtil.LT) && hasModifyAccess.equalsIgnoreCase("true")){
              bGetCancelECO= true;
            }
          return Boolean.valueOf(bGetCancelECO);
       }

  /**
      * This method is used for showing the Cancel ECR Link.
      * @param context the eMatrix <code>Context</code> object.
      * @param args holds objectId.
      * @return Boolean.
      * @throws Exception If the operation fails.
      * @since X3.
      */

       public Boolean getCancelECR(Context context,String[] args) throws Exception
       {
            boolean bGetCancelECR=false;
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            String objectId  = (String) paramMap.get("objectId");
            setId(objectId);
            //Added for IR-060044V6R2011x
			String strECRState = PropertyUtil.getSchemaProperty(context,"policy",PropertyUtil.getSchemaProperty(context, "policy_ECR"),"state_PlanECO");
            String hasModifyAccess = (getInfo(context,"current.access[modify]"));
            if(PolicyUtil.checkState(context, objectId, strECRState, PolicyUtil.LT) && hasModifyAccess.equalsIgnoreCase("true")){
              bGetCancelECR= true;
            }
          return Boolean.valueOf(bGetCancelECR);
       }
  /**
    * Checks whther the logged in person is having create access to a type creation.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-5.
    *
    */

    public Boolean  hasCreateAccess(Context context, String type) throws Exception
    {
          boolean hasCreateAccess = false;
          BusinessType busType = new BusinessType(type, context.getVault());
          PolicyList policyList = busType.getPoliciesForPerson(context,false);
          if(policyList.size()>0) {
              hasCreateAccess = true;
          }
          return Boolean.valueOf(hasCreateAccess);
    }

    /**
     * Checks for access for ENCPartClone command and restricts the clone operation for configured part
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @return Boolean.
     * @throws Exception If the operation fails.
     * @since R418.
     *
     */

     public Boolean hasCloneAccess(Context context, String []args) throws Exception     
     {
         HashMap paramMap = (HashMap)JPO.unpackArgs(args);
         String objectId  = (String) paramMap.get("objectId");
		 String launched = (String)paramMap.get("launched");
         boolean allowClone = true;
         if(UIUtil.isNotNullAndNotEmpty(objectId)) {
    		 String policyClass = DomainObject.newInstance(context, objectId).getInfo(context, "policy.property[PolicyClassification].value");
    		 if("Unresolved".equalsIgnoreCase(policyClass)|| "true".equalsIgnoreCase(launched)) { allowClone = false; }
        }
         return Boolean.valueOf(allowClone) && checkCreatePartAccess(context,args);
     }
    
    
  /**
    * Checks Part create access to display in the Actions Menu.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-5.
    *
    */

    public Boolean  checkCreatePartAccess(Context context, String []args) throws Exception
    {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objectId      = (String) paramMap.get("objectId");
        boolean isRestrictedClassification = false;

		if (!"null".equalsIgnoreCase(objectId) && !"".equalsIgnoreCase(objectId) && objectId != null)
        {
            setObjectId(objectId);
            String policy = getInfo(context,DomainConstants.SELECT_POLICY);
            String policyClass = EngineeringUtil.getPolicyClassification(context,policy);
            // modified the condition to not to give create access along for following
            // "Plant","Reported", "Sub-tier" MCC parts along with Equivalent for EC
            if ( policyClass.equalsIgnoreCase("Equivalent"))
            {
                isRestrictedClassification = true;
            }
            else
            {
              boolean mccInstall = FrameworkUtil.isSuiteRegistered(context,"appVersionMaterialsComplianceCentral",false,null,null);
              if(mccInstall)
              {
                String sCompliancePlantSpecificPart = PropertyUtil.getSchemaProperty(context, "type_CompliancePlantSpecificPart");
                String sComplianceReportedPart = PropertyUtil.getSchemaProperty(context, "type_ComplianceReportedPart");
                String sComplianceSubtierPart = PropertyUtil.getSchemaProperty(context, "type_ComplianceSubtierPart");
                if(isKindOf(context, sCompliancePlantSpecificPart) || isKindOf(context, sComplianceReportedPart)
                 || isKindOf(context, sComplianceSubtierPart))
                {
                  isRestrictedClassification = true;
                }
              }
            }
        }

      boolean hasCreateAccess = false;
      BusinessType busType = new BusinessType(DomainConstants.TYPE_PART, context.getVault());
      PolicyList policyList = busType.getPoliciesForPerson(context,false);
      if(policyList.size()>0 && !isRestrictedClassification) {
          hasCreateAccess = true;
      }
          return Boolean.valueOf(hasCreateAccess);
    }

    
    /**
     * Checks Part create access and this api make sure to enable this command only at mark up open window(pop up support)
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @return Boolean.
     * @throws Exception If the operation fails.
     *
     */

     public Boolean  showAddNewAtMarkupWindow(Context context, String []args) throws Exception {       	  	
    	 if(checkCreatePartAccess(context,args)) {
  	        HashMap paramMap      = (HashMap)JPO.unpackArgs(args);
 	        String fromMarkupView = (String) paramMap.get("fromOpenMarkup");
 	        HashMap settings  	  = (HashMap)paramMap.get("SETTINGS");
 	        String targetLocation = (String) settings.get("Target Location");
	        
	        if("true".equalsIgnoreCase(fromMarkupView) && "popup".equalsIgnoreCase(targetLocation)) {
	        	return Boolean.TRUE;
	        }
    	 }
    	 return Boolean.FALSE;
     }
     
     /**
      * Checks Part create access and this api make sure to enable this command only at mark up open window(pop up support)
      * @param context the eMatrix <code>Context</code> object.
      * @param args holds objectId.
      * @return Boolean.
      * @throws Exception If the operation fails.
      *
      */

      public Boolean  showAddNewAtBOMWindow(Context context, String []args) throws Exception {       	  	
     	 if(checkCreatePartAccess(context,args)) {
 	        HashMap paramMap      = (HashMap)JPO.unpackArgs(args);
 	        String selectedProgram = (String)paramMap.get("selectedProgram");
 	        if("emxPart:getStoredEBOM".equals(selectedProgram)){
	       		return Boolean.FALSE;
	       	}
 	        String fromMarkupView = (String) paramMap.get("fromOpenMarkup");
 	        HashMap settings  	  = (HashMap)paramMap.get("SETTINGS");
 	        String targetLocation = (String) settings.get("Target Location");
 	        String sFromCommand = (String) settings.get("Command Name");
 	        String showRMBCommands    = (String) paramMap.get("showRMBInlineCommands");
 	        String frmRMB    = (String) paramMap.get("isRMB");
 	        String objectId    = (String) paramMap.get("RMBID");
 	       	String fromConfigBOM  = (String) paramMap.get("fromConfigBOM");
 	      	HashMap Map = new HashMap();
	   	 	paramMap.put("fromConfigBOM", fromConfigBOM);
			boolean isConfigBOM = JPO.invoke(context, "emxENCActionLinkAccess", null, "showOrHideRMBCommandsInConfigBOM", JPO.packArgs(paramMap),Boolean.class);
	        if((UIUtil.isNullOrEmpty(fromMarkupView) && "slidein".equalsIgnoreCase(targetLocation)) || (UIUtil.isNotNullAndNotEmpty(sFromCommand) && sFromCommand.equals("ENCInsertNewPart"))) {
	        	if("true".equalsIgnoreCase(frmRMB)){
	        		DomainObject domObj = UIUtil.isNotNullAndNotEmpty(objectId)?new DomainObject(objectId):null;
	      	        String currentState = UIUtil.isNotNullAndNotEmpty(objectId)?domObj.getInfo(context, DomainObject.SELECT_CURRENT): "";
	        		if(!("true".equalsIgnoreCase(showRMBCommands)) || isConfigBOM){
	        			return Boolean.FALSE;
	        		}
			        
	        	}
	        	return Boolean.TRUE;
	        }
     	 }
     	 return Boolean.FALSE;
      }
     


  /**
    * Checks ECR create access to display in the Actions Menu.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-5.
    *
    */

    public Boolean  checkCreateECRAccess(Context context, String []args) throws Exception
    {
         return hasCreateAccess(context,DomainConstants.TYPE_ECR);
    }


  /**
    * Checks ECO create access to display in the Actions Menu.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-5.
    *
    */

    public Boolean  checkCreateECOAccess(Context context, String []args) throws Exception
    {
        return hasCreateAccess(context,DomainConstants.TYPE_ECO);
    }

  /**
    * Checks CAD Model create access to display in the Actions Menu.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-5.
    *
    */

    public Boolean  checkCreateCADModelAccess(Context context, String []args) throws Exception
    {
          return hasCreateAccess(context,DomainConstants.TYPE_CAD_MODEL);
    }

  /**
    * Checks CAD Drawing create access to display in the Actions Menu.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-5.
    *
    */

    public Boolean  checkCreateCADDrawingAccess(Context context, String []args) throws Exception
    {
          return hasCreateAccess(context,DomainConstants.TYPE_CAD_DRAWING);
    }

  /**
    * Checks Drawing Print create access to display in the Actions Menu.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-5.
    *
    */

    public Boolean  checkCreateDrawingPrintAccess(Context context, String []args) throws Exception
    {
          return hasCreateAccess(context,DomainConstants.TYPE_DRAWINGPRINT);
    }

  /**
    * Checks Sketch create access to display in the Actions Menu.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-5.
    *
    */

    public Boolean  checkCreateSketchAccess(Context context, String []args) throws Exception
    {
          return hasCreateAccess(context,DomainConstants.TYPE_SKETCH);
    }

  /**
    * Checks Product line create access to display in the Actions Menu.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-5.
    *
    */

    public Boolean  checkCreateProductLineAccess(Context context, String []args) throws Exception
    {
          return hasCreateAccess(context,DomainConstants.TYPE_PRODUCTLINE);
    }

  /**
    * Checks Part Family create access to display in the Actions Menu.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-5.
    *
    */

    public Boolean  checkCreatePartFamilyAccess(Context context, String []args) throws Exception
    {
          return hasCreateAccess(context,DomainConstants.TYPE_PART_FAMILY);
    }


  /**
    * Checking if modification is allowed for this object.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-5.
    *
    */
     public Boolean isModificationAllowed(Context context,String[] args) throws Exception
    {
//Added below code for the bug 288183
      boolean allowChanges = true;
     String strPartFamilyRef= PropertyUtil.getSchemaProperty(context,"relationship_PartFamilyReference");
      try{
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            
            String parentId      = "";
            
            // Specific check for RMB button
            String rmbTableRowId = (String) paramMap.get("rmbTableRowId");
            
            // The If condition is added specifically for a Right Mouse Button Usecase 
            if (rmbTableRowId != null && !"".equals(rmbTableRowId)) {
              StringList slList = FrameworkUtil.split(rmbTableRowId, "|");
      	      
      	    // This check is for cases where its not a structure browser
            // and the rmbTableRowId is in different format      
              
              if (slList.size() == 3) {
            	  parentId = (String) slList.get(0);
              } else if (slList.size() == 4) {
            	  parentId = (String) slList.get(1);
              } else if (slList.size() == 2) {
            	  parentId = (String) slList.get(1);
              } else {
            	  parentId = rmbTableRowId;
              }
              
            } else {
            	parentId      = (String) paramMap.get("objectId");	
            }
            
            
            
            
	 String obsoleteState = PropertyUtil.getSchemaProperty(context, "policy",DomainConstants.POLICY_EC_PART, "state_Obsolete");
     String approvedState  = PropertyUtil.getSchemaProperty(context, "policy",DomainConstants.POLICY_EC_PART, "state_Approved");
     String PendingObsoleteState  = PropertyUtil.getSchemaProperty(context, "policy",DomainConstants.POLICY_MANUFACTURER_EQUIVALENT, "state_PendingObsolete");

          //check the parent obj state
          StringList strList  = new StringList(2);
          strList.add(SELECT_CURRENT);
          strList.add("policy");
          strList.add(SELECT_OWNER); //Added code for the bug 346336

           DomainObject domObj = new DomainObject(parentId);

			String sMEPPolicy = PropertyUtil.getSchemaProperty(context,"policy_ManufacturerEquivalent");//Added code for the bug 346336

           Map map = domObj.getInfo(context,strList);

          String objState = (String)map.get(SELECT_CURRENT);
          String objPolicy = (String)map.get("policy");
          String objOwner = (String)map.get(SELECT_OWNER);//Added code for the bug 346336
          String policyClass = EngineeringUtil.getPolicyClassification(context,objPolicy);
          String propAllowLevel = (String)FrameworkProperties.getProperty(context, "emxEngineeringCentral.Part.RestrictPartEdit");
          StringList propAllowLevelList = new StringList();

          if(propAllowLevel != null && !"null".equals(propAllowLevel) && propAllowLevel.length() > 0)
          {
            StringTokenizer stateTok = new StringTokenizer(propAllowLevel, ",");
            while (stateTok.hasMoreTokens())
             {
                String tok = (String)stateTok.nextToken();
                propAllowLevelList.add(FrameworkUtil.lookupStateName(context, objPolicy, tok));
             }
          }
            if("Production".equals(policyClass)){
                 allowChanges = (!propAllowLevelList.contains(objState));
            }else if(EngineeringUtil.isMBOMInstalled(context) && EngineeringUtil.isManuPartPolicy(context,objPolicy))
            {
                allowChanges = (!objState.equals(DomainObject.STATE_PART_REVIEW) && !objState.equals(approvedState) && !objState.equals(DomainObject.STATE_PART_RELEASE) && !objState.equals(obsoleteState));
            }

          String propAllowLevelDevPart = (String)FrameworkProperties.getProperty(context, "emxEngineeringCentral.Part.RestrictDevelopmentPartEdit");
          StringList propAllowLevelListDevPart = new StringList();

          if(propAllowLevelDevPart != null && !"null".equals(propAllowLevelDevPart) && propAllowLevelDevPart.length() > 0)
          {
            StringTokenizer stateToken = new StringTokenizer(propAllowLevelDevPart, ",");
            while (stateToken.hasMoreTokens())
             {
                String token = (String)stateToken.nextToken();
                propAllowLevelListDevPart.add(FrameworkUtil.lookupStateName(context, objPolicy, token));
             }
          }
        if("Development".equals(policyClass)){
             allowChanges = (!propAllowLevelListDevPart.contains(objState));
            }
			 //Ended code for the bug 288183
			 //Added Code for Bug No 329545 Dated 4/13/2007 Begin
            Access access=new Access();
			BusinessObject boPart=new BusinessObject(parentId);
			access=boPart.getAccessMask(context);
            if(!access.hasModifyAccess())
               {
                 allowChanges=false;
               }
			//Added Code for Bug No 329545 Dated 4/13/2007 Ends
   /**
         * MBOM-LG Structure Edit Configure Action commands to appear based on Mode and View Selector
         * the following code ristricts not to show the Add Existing command in Common View
         */
        String sBOMViewFilter = "";
        if(EngineeringUtil.isMBOMInstalled(context)){
            sBOMViewFilter    = (String) paramMap.get("ENCBillOfMaterialsViewCustomFilter");
            if(sBOMViewFilter!=null&&!"engineering".equalsIgnoreCase(sBOMViewFilter))
            {
                allowChanges=false;
            }
        }
        /**Added for MBOM - Ends Here */
       // Added Code For Part Series Functionality Starts
        
        String resClassifiedItemId = "";
        String toolBar = (String) paramMap.get("toolbar");
        if ("ENCpartSpecificationSummaryToolBar".equals(toolBar)) { // Added for IR-106890V6R2012x
        	if(access.hasFromConnectAccess() || access.hasFromDisconnectAccess()){ // Added for IR-433552-3DEXPERIENCER2017x
        		allowChanges = true;
        	}
			 String strattr = domObj.getInfo(context,"attribute["+ATTRIBUTE_REFERENCE_TYPE+"]");
             String PartSeriesEnabled = (String)FrameworkProperties.getProperty(context, "emxEngineeringCentral.PartSeries.PartSeriesActive");
             if(("true".equalsIgnoreCase(PartSeriesEnabled)) && (strattr.equalsIgnoreCase(SYMB_R))) {
 				String command = "print bus $1 select $2 dump $3";
 				String strres = MqlUtil.mqlCommand(context,command,parentId,"relationship["+RELATIONSHIP_CLASSIFIED_ITEM+"].id","|");
 				String command1 = "query connection type $1 where $2 select $3 dump";
				StringTokenizer stCIRecords = new StringTokenizer(strres,"|");
 				while (stCIRecords.hasMoreTokens()) {
 					resClassifiedItemId = stCIRecords.nextToken();
 					
 				String strres1 = MqlUtil.mqlCommand(context,command1,strPartFamilyRef, "fromrel.id ==" + resClassifiedItemId  ,"torel.to.id");
				StringList strlIds = FrameworkUtil.split(strres1, ",");
				if (strlIds.size() == 2)
				 {
					String strres2 = (String) strlIds.get(1);
 				DomainObject partId = new DomainObject(strres2);
 				String strattrValue = partId.getInfo(context,"attribute["+ATTRIBUTE_REFERENCE_TYPE+"]");
 				if (strattrValue.equalsIgnoreCase(SYMB_M))  {
 					allowChanges = false;
 			    }
				 }
				 }
 		     }
        }
        // Added Code For Part Series Functionality Ends
//Added below code for the bug 346336
        if(sMEPPolicy.equals(objPolicy)){
			if(!(context.getUser().equals(objOwner))|| objState.equals(DomainObject.STATE_PART_RELEASE) || objState.equals(DomainObject.STATE_PART_OBSOLETE) || objState.equals(PendingObsoleteState))
	             allowChanges = false;
            }
//Added above code for the bug 346336

      }catch (Exception e) {
      throw new Exception(e.toString());
      }
      return  Boolean.valueOf(allowChanges);

    }

	    public Boolean isMFGMCONodeDisplay(Context context, String []args) throws Exception
    {
	return Boolean.valueOf(isMFGandMCOkey(context,args));
    }

    public Boolean isMFGECONodeDisplay(Context context, String []args) throws Exception
    {
	return Boolean.valueOf(!isMFGandMCOkey(context,args));
    }

    public boolean isMFGandMCOkey(Context context, String []args) throws Exception
    {
        boolean showMCONode = false;
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objId =     (String)paramMap.get("objectId");
		DomainObject obj = new DomainObject(objId);
		String sPolicy = obj.getInfo(context,"policy");
		String sMfgPolicy      = PropertyUtil.getSchemaProperty(context,"policy_ManufacturingPart");

        try
        {
            if(sPolicy.equalsIgnoreCase(sMfgPolicy))
			{
				String sImplementKey = FrameworkProperties.getProperty(context, "emxMBOM.MBOM.ImplementChange");
				if( sImplementKey!=null && "MCO".equalsIgnoreCase(sImplementKey.trim()) )
				{
					showMCONode = true;
				}
			}
        }
        catch(Exception e)
        {
            showMCONode = false;
        }
        return showMCONode;
    }

/**
  * Checking the condtion for ShowLink.
  *
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds objectId.
  * @return Boolean.
  * @throws Exception If the operation fails.
  * @since EC10-5.
  *
  */
   public Boolean hasECRViewPdfLink(Context context, String []args) throws Exception
   {
        boolean hasECRViewPdfLink = false;
        String sECRPdfLink = FrameworkProperties.getProperty(context, "emxEngineeringCentral.ECRECO.ViewPdfSummary");
        if("true".equalsIgnoreCase(sECRPdfLink))
        {
           hasECRViewPdfLink=true;
       }
     return Boolean.valueOf(hasECRViewPdfLink);
 }
 /**
  * Checking the condtion for ShowLink.
  *
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds objectId.
  * @return Boolean.
  * @throws Exception If the operation fails.
  * @since EC10-5.
  *
  */
   public Boolean hasECOViewPdfLink(Context context, String []args) throws Exception
   {
        boolean hasECOViewPdfLink = false;
        String sECOPdfLink = FrameworkProperties.getProperty(context, "emxEngineeringCentral.ECRECO.ViewPdfSummary");
        if("true".equalsIgnoreCase(sECOPdfLink))
        {
            hasECOViewPdfLink=true;
        }
      return Boolean.valueOf(hasECOViewPdfLink);
 }



     /**
    * Checking the Read access and checking whether the Spec object is not in Released state.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since AEF.
    *
    */
    public Boolean hasReadAndStateNotRelease(Context context,String[] args) throws Exception
    {
       HashMap paramMap = (HashMap)JPO.unpackArgs(args);
       boolean bHasReadAccess = false;
       matrix.db.Access mAccess  = null;
       String objectId      = (String) paramMap.get("objectId");
       try
           {

              DomainObject bosAccess = DomainObject.newInstance(context);
              bosAccess.setId(objectId);

              SelectList sPartSelStmts = new SelectList(1);
              sPartSelStmts.add(DomainConstants.SELECT_CURRENT);
              sPartSelStmts.add(DomainConstants.SELECT_POLICY);

              Map objMap = bosAccess.getInfo(context, sPartSelStmts);
              String stateCurrent = (String)objMap.get(DomainConstants.SELECT_CURRENT);
              String policy = (String)objMap.get(DomainConstants.SELECT_POLICY);

              //Added for Bug: 308765
              String releasedState = com.matrixone.apps.engineering.EngineeringUtil.getReleaseState(context,policy);

              if (!stateCurrent.equalsIgnoreCase(releasedState))
              {
                  mAccess = bosAccess.getAccessMask(context);
                  if(mAccess.has(Access.cRead))
                  {
                       bHasReadAccess = true;
                  }
              }
         }
        catch (Exception e)
            {
                throw new Exception(e.toString());
            }
      return  Boolean.valueOf(bHasReadAccess);

    }

 /**
  * Checking the condtion for modify access and displaying Specification--->"Edit Details" Link for MEP part for Component Role.
  *
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds objectId.
  * @return Boolean.
  * @throws Exception If the operation fails.
  * @since EC10-6.
  *
  */
   public Boolean isShowMEPSpecEditDetailsAccess(Context context, String []args) throws Exception
   {
        boolean editDetailsViewable = false;
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String strRelId = (String)paramMap.get("relId");
        String objId =     (String)paramMap.get("objectId");
        try
        {
            if(strRelId !=null && !strRelId.equals(""))
            {
                DomainObject domObj = new DomainObject(objId);
                String strModifyAccess =domObj.getInfo(context,"current.access[modify]");
                String strRelArr[] = new String[1];
                strRelArr[0] =strRelId;
                StringList relationshipSelects =new StringList(2);
                relationshipSelects.addElement("from.policy.property[PolicyClassification].value");
                //return only one Map
                MapList mapList =DomainRelationship.getInfo(context,strRelArr,relationshipSelects);
                Iterator i = mapList.iterator();
                Map map = (Map) i.next();
                String strPolicyClassification =(String)map.get("from.policy.property[PolicyClassification].value");
                if("Equivalent".equals(strPolicyClassification) && "true".equalsIgnoreCase(strModifyAccess))
                {
                    editDetailsViewable=true;
                }
            }
       }catch (Exception e) {
           throw new Exception(e.toString());
         }
         return Boolean.valueOf(editDetailsViewable);
   }

/**
  * Checking the condtion for displaying Specification--->"Edit Details" Link for EC part other than Component Role and
  * also displays "Edit Details" command  when we create the specification through top menu Actions-->Engineering.
  *
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds objectId.
  * @return Boolean.
  * @throws Exception If the operation fails.
  * @since EC10-6.
  *
  */
  public Boolean isShowECSpecEditDetailsAccess(Context context, String []args) throws Exception
  {
        boolean editDetailsViewable = false;
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String strRelId = (String)paramMap.get("relId");
        String objId =     (String)paramMap.get("objectId");
        DomainObject domObj =null;
        String strModifyAccess =null;
        try
        {
            if(strRelId !=null && !strRelId.equals(""))
            {
                domObj = new DomainObject(objId);
                strModifyAccess =domObj.getInfo(context,"current.access[modify]");
                String strRelArr[] = new String[1];
                strRelArr[0] =strRelId;
                StringList relationshipSelects =new StringList(1);
                relationshipSelects.addElement("from.policy.property[PolicyClassification].value");
                //return only one Map
                MapList mapList =DomainRelationship.getInfo(context,strRelArr,relationshipSelects);
                Iterator i = mapList.iterator();
                Map map = (Map) i.next();
                String strPolicyClassification =(String)map.get("from.policy.property[PolicyClassification].value");
                if(!"Equivalent".equals(strPolicyClassification) && "true".equalsIgnoreCase(strModifyAccess))
                {
                    editDetailsViewable=true;
                }
            }
            else
            {   //enter when creating the specification through top menu Actions-->Engineering
                domObj = new DomainObject(objId);
                strModifyAccess =domObj.getInfo(context,"current.access[modify]");
                if("true".equalsIgnoreCase(strModifyAccess))
                {
                    editDetailsViewable=true;
                }
            }
        }catch (Exception e) {
           throw new Exception(e.toString());
         }
         return Boolean.valueOf(editDetailsViewable);
  }


    /**
    * This method returns true if emxEngineeringCentral.DisplayEBOMIndentedTable = true.
    * Used to display the commands related to EBOM Indented Table
    *
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-6.
    *
    */
    public Boolean isEBOMIndentedTable(Context context, String []args) throws Exception
    {
        boolean bShowIndentedTable = false;
        try
        {
            String sShowIndentedTable = FrameworkProperties.getProperty(context, "emxEngineeringCentral.DisplayEBOMIndentedTable");
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        	String objectId = (String)paramMap.get("objectId");
        	String policy = DomainObject.newInstance(context, objectId).getInfo(context, DomainObject.SELECT_POLICY);
            if( sShowIndentedTable!=null && "true".equalsIgnoreCase(sShowIndentedTable.trim()) && !policy.equals(EngineeringConstants.POLICY_CONFIGURED_PART))
            {	
            		bShowIndentedTable = true;
            }
        }
        catch(Exception e)
        {
            bShowIndentedTable = false;
        }
        return Boolean.valueOf(bShowIndentedTable);
    }

    /**
    * This method returns true if emxEngineeringCentral.DisplayEBOMIndentedTable = false.
    * Used to display the commands related to EBOM Indented Table
    *
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-6.
    *
    */
    public Boolean isEBOMemxTable(Context context, String []args) throws Exception
    {
        Boolean ShowIndentedTable = isEBOMIndentedTable(context, args);
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    	String objectId = (String)paramMap.get("objectId");
    	String policy = DomainObject.newInstance(context, objectId).getInfo(context, DomainObject.SELECT_POLICY);
    	if(policy.equals(EngineeringConstants.POLICY_CONFIGURED_PART)){
    		ShowIndentedTable = true;
    	}
        return Boolean.valueOf(!ShowIndentedTable.booleanValue());
    }


    /**
    *  Showing "Delete Selected" command if EBOM is displayed in emxTable.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-6.
    *
    */
    public Boolean showEBOMTableDeleteSelected(Context context, String []args)throws Exception
    {
       //modified for bug 308466 to call isEBOMModificationAllowed instead of isModificationAllowed
        boolean showCommand = false;
        boolean isEBOMModificationAllowed = (isEBOMModificationAllowed(context,args).booleanValue());

        if(isEBOMModificationAllowed && isEBOMemxTable(context,args).booleanValue())
        {
            showCommand = true;
        }

        return Boolean.valueOf(showCommand);
    }

    /**
    * Showing "Delete Selected" command if EBOM is displayed in IndentedTable.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-6.
    *
    */
    public Boolean showIndentedTableDeleteSelected(Context context, String []args)throws Exception
    {
        //modified for bug 308466 to call isEBOMModificationAllowed instead of isModificationAllowed
        boolean showCommand = false;
        boolean isEBOMModificationAllowed = (isEBOMModificationAllowed(context,args).booleanValue());
        if(isEBOMModificationAllowed && isEBOMIndentedTable(context,args).booleanValue())
        {
            showCommand = true;
        }
        return Boolean.valueOf(showCommand);
    }

    /**
    * To Show Edit ALL link in EBOM SB Indented Table.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-6.
    *
    */
    public Boolean showEBOMIndentedTableEditAll(Context context, String []args) throws Exception
    {
    	boolean showCommand = false;
    	//modified for bug 308466 to call isEBOMModificationAllowed instead of isModificationAllowed

		boolean isEBOMModificationAllowed = (isEBOMModificationAllowed(context,args).booleanValue());
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		String selectedProgram = (String)paramMap.get("selectedProgram");
		String strMode			= (String)paramMap.get("BOMMode");
		String sFilteredView = (String)paramMap.get("ENCBOMRevisionCustomFilter");	
		String showSubstituteEditIcon = (String)paramMap.get("showSubstituteEditIcon");
		
		if (null == strMode) {
			strMode = "";
		}
		if(isEBOMModificationAllowed &&("emxPart:getStoredEBOM".equals(selectedProgram) || "ENG".equals(strMode)))
		{
			showCommand = true;
		}
		if("true".equalsIgnoreCase(showSubstituteEditIcon))
		{ 
			showCommand = true;
		}
    	if(sFilteredView != null && !sFilteredView.equals("As Stored")){
    		showCommand = false;
    	}

    	return Boolean.valueOf(showCommand);
    }
    /**
     * To Show Inline commands in EBOM SB Indented Table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @return Boolean.
     * @throws Exception If the operation fails.
     * @since EC10-6.
     *
     */
    public Boolean HideInlineCommands(Context context, String []args) throws Exception
    {
    	boolean InlineCommands = true;
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		String value = (String)paramMap.get("HideInlineCommands");
		String strObjectId = (String)paramMap.get("objectId");
		StringList slSelectables = new StringList();
		slSelectables.add(DomainConstants.SELECT_POLICY);
		slSelectables.add(DomainConstants.SELECT_CURRENT);
		
		if("true".equals(value))
		{
			InlineCommands = false;
		}
		else if(UIUtil.isNotNullAndNotEmpty(strObjectId)){
			Map mObjInfo = DomainObject.newInstance(context,strObjectId).getInfo(context,slSelectables);
			String sCurrentState = (String)mObjInfo.get(DomainConstants.SELECT_CURRENT);
			String sPolicy = (String)mObjInfo.get(DomainConstants.SELECT_POLICY);
			if(DomainConstants.POLICY_EC_PART.equals(sPolicy) && !EngineeringConstants.STATE_EC_PART_PRELIMINARY.equals(sCurrentState)){
				InlineCommands = false;
			}
		}
    	return InlineCommands;
    }
    /**
    * To Show Edit ALL link in EBOM emxTable
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-6.
    *
    */
    public Boolean showEBOMTableEditAll(Context context, String []args) throws Exception
    {
       boolean showCommand = false;
       if(isEBOMemxTable(context, args).booleanValue())
       {
        //modified for bug 308466 to call isEBOMModificationAllowed instead of isModificationAllowed
            boolean isEBOMModificationAllowed = (isEBOMModificationAllowed(context,args).booleanValue());
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            String selectedProgram = (String)paramMap.get("selectedFilter");
            if(isEBOMModificationAllowed && "emxPart:getEBOMsWithRelSelectables".equals(selectedProgram))
            {
                showCommand = true;
            }
        }

        return Boolean.valueOf(showCommand);
    }
//fix for bug 308466 . added new method for access check on edit column in EBOM pages.
    /**
    * To Show Edit icon/ Edit ALL link in EBOM emxTable/Indented  Table
    * based on the state of the Parent assembly Part
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC11-0.
    *
    */

    public Boolean isEBOMModificationAllowed(Context context, String[] args)
        throws Exception
    {
         boolean allowChanges = true;
      try
        {
          HashMap paramMap = (HashMap)JPO.unpackArgs(args);
          String parentId      = (String) paramMap.get("objectId");
 //modified as per changes for bug 311050
          //check the parent obj state
          StringList strList  = new StringList(2);
          strList.add(SELECT_CURRENT);
          strList.add("policy");

           DomainObject domObj = new DomainObject(parentId);
           Map map = domObj.getInfo(context,strList);

          String objState = (String)map.get(SELECT_CURRENT);
          String objPolicy = (String)map.get("policy");

          // Added for UI convergence of ENG and XCE.
          if ( objPolicy.equals( EngineeringConstants.POLICY_CONFIGURED_PART ) ) { return true; }
          
          String propAllowLevel = (String)FrameworkProperties.getProperty(context, "emxEngineeringCentral.Part.RestrictPartModification");
          StringList propAllowLevelList = new StringList();

          if(propAllowLevel != null && !"null".equals(propAllowLevel) && propAllowLevel.length() > 0)
          {
            StringTokenizer stateTok = new StringTokenizer(propAllowLevel, ",");
            while (stateTok.hasMoreTokens())
             {
                String tok = (String)stateTok.nextToken();
                propAllowLevelList.add(FrameworkUtil.lookupStateName(context, objPolicy, tok));
             }
          }
           allowChanges = (!propAllowLevelList.contains(objState));
//end of changes
        }catch (Exception e)
        {
           throw new Exception(e.toString());
        }

        return Boolean.valueOf(allowChanges);
    }

   /** fix for bug 314740
    * To Show Add Existing/ remove selected links in ECO New Part/Revised Parts summary Table
    * based on the state of the ECO
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC11-0.
    *
    */

    public Boolean isECOModificationAllowed(Context context, String[] args)
        throws Exception
  {
         boolean allowChanges = false;
    try
      {
        if( hasReadAccess(context,args).booleanValue())
        {
          HashMap paramMap = (HashMap)JPO.unpackArgs(args);
          String parentId      = (String) paramMap.get("objectId");
          String objState = (new DomainObject(parentId).getInfo(context,SELECT_CURRENT));
          String objPolicy = (new DomainObject(parentId).getInfo(context,"policy"));

          String propAllowLevel = (String)FrameworkProperties.getProperty(context, "emxEngineeringCentral.Part.RestrictECOConnectStates");

          StringList propAllowLevelList = new StringList();

          if(propAllowLevel != null && !"null".equals(propAllowLevel) && propAllowLevel.length() > 0)
          {
            StringTokenizer stateTok = new StringTokenizer(propAllowLevel, ",");
            while (stateTok.hasMoreTokens())
             {
                String tok = (String)stateTok.nextToken();
                propAllowLevelList.add(FrameworkUtil.lookupStateName(context, objPolicy, tok));
             }

          }

         allowChanges = (!propAllowLevelList.contains(objState));
        }
      }catch (Exception e)
        {
           throw new Exception(e.toString());
        }

        return Boolean.valueOf(allowChanges);
  }

 /** modified for bug 318452
  * Checking the condtion for Active ECR/ECO ShowIcon.
  *
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds objectId.
  * @return Boolean.
  * @throws Exception If the operation fails.
  * @since EC11-0.
  *
  */
   public Boolean displayECRECOViewIcon(Context context, String []args) throws Exception
   {
        boolean displayECRECOViewIcon = false;
        String sECRECOIcon = FrameworkProperties.getProperty(context, "emxEngineeringCentral.Search.DisplayActiveECRECOIcon");
        if("true".equalsIgnoreCase(sECRECOIcon))
        {
            displayECRECOViewIcon=true;
        }
      return Boolean.valueOf(displayECRECOViewIcon);
 }

  /**
    * Checking the whether object can be revised.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC10-7SP1.
    *
    */

    public Boolean  hasReviseAccess(Context context,String[] args) throws Exception
    {
        boolean hasReviseAccess = false;
        if(isTypeAccessable(context,args).booleanValue()){
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            String objectId  = (String) paramMap.get("objectId");
            DomainObject dmObj = DomainObject.newInstance(context, objectId);
            
            StringList slSel = new StringList(DomainConstants.SELECT_POLICY);
            slSel.add("current.access[revise]");
			Map mObjInfo = dmObj.getInfo(context, slSel);
			String reviseacc = (String)mObjInfo.get("current.access[revise]");

            if(reviseacc != null && reviseacc.equalsIgnoreCase("TRUE") && !((String)mObjInfo.get(DomainConstants.SELECT_POLICY)).equalsIgnoreCase(EngineeringConstants.POLICY_MANUFACTURING_PART)) {
              hasReviseAccess = true;
            }
        }
        return Boolean.valueOf(hasReviseAccess);
    }
 /**
     * To hide Related ECR in Create ECO webform
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @return Boolean.
     * @throws Exception If the operation fails.
     * @since EC X3
     */
     public boolean hideRelatedECR(Context context, String []args) throws Exception
     {
        boolean hideRelatedECR = false;
             //creating parmaMap.
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId =(String) paramMap.get("objectId");
          if(strObjectId == null ){
              hideRelatedECR=true;
         }
        return hideRelatedECR;
     }
  /**
     * To hide Related Related Part in Create ECO webform
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @return Boolean.
     * @throws Exception If the operation fails.
     * @since EC X3
     */
 public boolean hideRelatedPart(Context context, String []args) throws Exception
     {
        boolean hideRelatedPart = true;
             //creating parmaMap.
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId =(String) paramMap.get("objectId");
          if(strObjectId == null ){
             hideRelatedPart=false;
          }
        return hideRelatedPart;
     }
  /**
         * Checking the condtion for ShowLink.
         *
         * @param context the eMatrix <code>Context</code> object.
         * @param args holds objectId.
         * @return Boolean.
         * @throws Exception If the operation fails.
         * @since EC X3
         */
         public  Boolean hasShowActionLink(Context context,String[] args)
             throws Exception
         {
             HashMap paramMap = (HashMap)JPO.unpackArgs(args);
             String objectId     = (String) paramMap.get("objectId");

             // For checking polocies exists or not
             if(!isTypeAccessable(context,args).booleanValue()){
                 return Boolean.FALSE;
             }
             if(!hasReadAccess(context,args).booleanValue()){
               return Boolean.FALSE;
             }
             //creating DomainObject.
             DomainObject obj = DomainObject.newInstance(context);
             obj.setId(objectId);

            //363480
            String nextRevState = obj.getInfo(context, "next.current");
            if(nextRevState != null && nextRevState.equals(DomainConstants.STATE_PART_RELEASE)){
                return Boolean.FALSE;
            }
            //363480

             boolean bShowLinks = true;
             String sDevelopmentInUse = "true";

             boolean bDevelopmentInUse = false;
             if("true".equals(sDevelopmentInUse)) {
             bDevelopmentInUse = true;
             }
             
             String sAllowChangeMgmtforMEP = "false";

             boolean bAllowChangeMgmtforMEP = false;
             if("true".equalsIgnoreCase(sAllowChangeMgmtforMEP)) {
             bAllowChangeMgmtforMEP = true;
             }

             String policy = EngineeringUtil.getPolicyClassification(context,obj.getPolicy(context).getName());
             if(("Development".equals(policy) && bDevelopmentInUse) || ("Equivalent".equals(policy) && !bAllowChangeMgmtforMEP))
             {
                   bShowLinks = false;
             }
              return Boolean.valueOf(bShowLinks);
         }
  /**
     * To hide Related Related ECR Field in Create ECO webform
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @return Boolean.
     * @throws Exception If the operation fails.
     * @since EC X3
     */
   public boolean hideRelatedECRField(Context context, String []args) throws Exception
     {
        boolean hideECRField = true;
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String relECR =(String) paramMap.get("ECRId");
           if(relECR == null ){
           hideECRField=false;
          }
         return hideECRField;
    }
/**
    * To hide  Related ECR Field in Create ECO webform
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since EC X3
    */
  public boolean hideECRField(Context context, String []args) throws Exception
    {
       boolean hideECRField = true;
       HashMap paramMap = (HashMap)JPO.unpackArgs(args);
       String relECR =(String) paramMap.get("ECRId");
          if(relECR != null ){
          hideECRField=false;
         }
        return hideECRField;
    }
/**
     * To hide any Form Field in Create ECO webform
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @return Boolean.
     * @throws Exception If the operation fails.
     * @since EC X3
     */
   public boolean hideFieldInEdit(Context context, String []args) throws Exception
     {
	    boolean hideField = true;
	    try{

        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String mode =(String) paramMap.get("mode");
           if("edit".equals(mode)){
           hideField=false;
		   }
		}catch(Exception ex){
		}
		return hideField;
    }
       /**
     * Checks the view mode of the Table display.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a HashMap containing the following entries:
     * mode - a String containing the mode.
     * @return Object - boolean true if the mode is view
     * @throws Exception if operation fails
     * @since EngineeringCentral X3
     */

    public Object checkViewMode(Context context, String[] args)
        throws Exception
    {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String strMode = (String) programMap.get("mode");
        Boolean isViewMode = Boolean.FALSE;
        // check the mode of the table.
        if( (strMode == null) || (strMode != null && ("null".equals(strMode) || "view".equalsIgnoreCase(strMode) || "".equals(strMode))) ) {
            isViewMode = Boolean.TRUE;
        }
        return isViewMode;
    }
     /**
        * Gets the Boolean value of the a parameter.
        *
        * @param context the eMatrix <code>Context</code> object.
        * @param args holds the objectId.
        * @return Boolean.
        * @throws Exception If the operation fails.
        * @since EC V6R2009-1.
        *
        */
        public  Boolean hasDisplayLinkForAssignToECO(Context context,String[] args)
            throws Exception
        {
			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String objectId      = (String) paramMap.get("objectId");
			boolean bDisplayLinks = false;

			DomainObject doChangeObj = new DomainObject(objectId);
			String strChangeType	=	doChangeObj.getInfo(context, DomainConstants.SELECT_TYPE);
			String currentChangeStatus	=	doChangeObj.getInfo(context, DomainConstants.SELECT_CURRENT);
			if(strChangeType.equals(DomainConstants.TYPE_ECR))
				if((currentChangeStatus.equals(STATE_ECR_PLAN_ECO)))
					bDisplayLinks = true;
			return Boolean.valueOf(bDisplayLinks);
      }
     /**
        * Gets the Boolean value of the a parameter.
        *
        * @param context the eMatrix <code>Context</code> object.
        * @param args holds the objectId.
        * @return Boolean.
        * @throws Exception If the operation fails.
        * @since EC V6R2009-1.
        *
        */
        public  Boolean hasDisplayLinkForMoveToECO(Context context,String[] args)
            throws Exception
        {
			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String objectId      = (String) paramMap.get("objectId");
			boolean bDisplayLinks = false;

			DomainObject doChangeObj = new DomainObject(objectId);
			String strChangeType	=	doChangeObj.getInfo(context, DomainConstants.SELECT_TYPE);
			String currentChangeStatus	=	doChangeObj.getInfo(context, DomainConstants.SELECT_CURRENT);
			if(strChangeType.equals(DomainConstants.TYPE_ECO))
				if((currentChangeStatus.equals(STATE_ECO_CREATE))||(currentChangeStatus.equals(STATE_ECO_DEFINE_COMPONENTS))||(currentChangeStatus.equals(STATE_ECO_DESIGN_WORK)))
					bDisplayLinks = true;
			return Boolean.valueOf(bDisplayLinks);
      }


/**
     * To hide Related ECO Field in Create ECO webform
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @return Boolean.
     * @throws Exception If the operation fails.
     * @since EC X3
     */
public boolean hideCreateECOField(Context context, String []args) throws Exception
     {
        boolean hideECOField = false;
             //creating parmaMap.
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId =(String) paramMap.get("OBJId");

          if(strObjectId == null ){
             hideECOField=true;
          }

        return hideECOField;
     }


/**
 * To hide Related ECO Field in Create ECO webform
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds objectId.
 * @return Boolean.
 * @throws Exception If the operation fails.
 * @since EC V6R2012
 */
public boolean hideECRFieldForCreateECO(Context context, String []args) throws Exception
 {
    boolean hideECOField = false;
         //creating parmaMap.
    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    String strObjectId =(String) paramMap.get("OBJId");
   if(strObjectId == null || (strObjectId != null && !("").equals(strObjectId) && !("null").equals(strObjectId) && isRelatedECRNotExists(context, paramMap))){
	 
         hideECOField=true;
      }

    return hideECOField;
 }

private boolean isRelatedECRNotExists(Context context, HashMap paramMap) throws Exception {



    String strChangeId = (String) paramMap.get("OBJId");
    String sTypeECO = PropertyUtil.getSchemaProperty(context,"type_ECR");
    DomainObject domObj = new DomainObject(strChangeId);
    String strCreateMode = (String) paramMap.get("CreateMode");
    String strType =  domObj.getInfo(context,SELECT_TYPE);
    String sRelationship = PropertyUtil.getSchemaProperty(context,"relationship_ECOChangeRequestInput");;
    if(strType.equals(DomainConstants.TYPE_ECO) && "MoveToECO".equals(strCreateMode)){
        // Getting all the connected Items with the Context Object with the RelationShip "Affected Item"
        StringList busSelects = new StringList(2);
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_NAME);
        StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

        MapList mapList = domObj.getRelatedObjects(context,
                                                                sRelationship,
                                                                sTypeECO,
                                                                busSelects,
                                                                relSelectsList,
                                                                false,
                                                                true,
                                                                (short)1,
                                                                null,
                                                                null);
        if (mapList.size() == 0) return true;
    }
    return false;
}

/**
     * To hide Related ECO Field in Create ECO webform
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @return Boolean.
     * @throws Exception If the operation fails.
     * @since EC X3
     */
public boolean donotHideCreateECOField(Context context, String []args) throws Exception
     {
        boolean hideECOField = true;
             //creating parmaMap.
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId =(String) paramMap.get("OBJId");

        if(strObjectId == null ){
           hideECOField=false;
        }

        return hideECOField;
     }

     /**
     * To hide Related ECO Field in Create ECO webform and also check create Policy Default
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @return Boolean.
     * @throws Exception If the operation fails.
     * @since EC X3
     */
     public boolean hideCreateECOFieldDynamicApprovalPolicy(Context context, String []args) throws Exception
     {
        boolean hideECOField = false;
             //creating parmaMap.
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId =(String) paramMap.get("OBJId");

        if(strObjectId == null ){
           hideECOField=true;
        }

        // check create ECO policy default - only show this field for Dynamic Approval policies
        if (hideECOField)
        {
            // 374591
            String policyClassification = FrameworkUtil.getPolicyClassification(context, POLICY_ECO);
            if (!"DynamicApproval".equals(policyClassification))
            {
                hideECOField = false;
            }
        }
        return hideECOField;
     }

     /**
     * To hide Related ECO Field in Create ECO webform and also check create Policy Default
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @return Boolean.
     * @throws Exception If the operation fails.
     * @since EC X3
     */
     public boolean donotHideCreateECOFieldDynamicApprovalPolicy(Context context, String []args) throws Exception
     {
        boolean hideECOField = true;
             //creating parmaMap.
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId =(String) paramMap.get("OBJId");

          if(strObjectId == null ){
             hideECOField=false;
          }
        // check create ECO policy default - only show this field for Dynamic Approval policies
        if (hideECOField)
        {
            // 374591
            String policyClassification = FrameworkUtil.getPolicyClassification(context, POLICY_ECO);
            if (!"DynamicApproval".equals(policyClassification))
            {
                hideECOField = false;
            }
        }

        return hideECOField;
     }


	   /**
     * The method is used to show / hide 'Add Existing'/'Remove' actions links in  Change Object
     * Affected Item page. The links will be enabled till all states prior to 'Review' state
	 * for ECR and prior to Define Components for ECO.
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id
     * @return        a <code>Boolean</code> object representing boolean true or false depending on whether to enable / disalbe link
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral V6R2009-1
     **
     */

   		public Boolean showAddRemoveAffectedItemsLink(Context context, String[] args) throws Exception {
   			HashMap programMap           = (HashMap)JPO.unpackArgs(args);
            String strChangeObjId           = (String)programMap.get("objectId");
			DomainObject changeObj = new DomainObject(strChangeObjId);

            SelectList sSelStmts = new SelectList(4);
            sSelStmts.add(DomainConstants.SELECT_TYPE);
            sSelStmts.add("policy.property[PolicyClassification].value");
            sSelStmts.add("type.kindof["+DomainConstants.TYPE_ECR+"]");
            sSelStmts.add("type.kindof["+DomainConstants.TYPE_ECO+"]");

            if(EngineeringUtil.isMBOMInstalled(context)){
			    sSelStmts.add("type.kindof["+PropertyUtil.getSchemaProperty(context, "type_MECO")+"]");
			    sSelStmts.add("type.kindof["+PropertyUtil.getSchemaProperty(context, "type_DCR")+"]");
            }

            Map objMap = changeObj.getInfo(context, sSelStmts);
            String sParentType = (String)objMap.get(DomainConstants.SELECT_TYPE);

            emxCommonEngineeringChange_mxJPO CECBJPO = null;
            String strArrArgs [] = new String [1];
            CECBJPO = new emxCommonEngineeringChange_mxJPO(context, strArrArgs);

			if (sParentType.equals(DomainObject.TYPE_ECO)) {
				return (CECBJPO.showActionsLink(context, args,
						SYMBOLIC_NAME_FOR_STATE_ECO_DESIGN_WORK));
			} else if (sParentType.equals(DomainObject.TYPE_ECR)) {
				return (CECBJPO.showActionsLink(context, args,
						SYMBOLIC_NAME_FOR_STATE_ECR_EVALUATE));
			} else if (sParentType.equals(EngineeringConstants.TYPE_DCR)) {
				return (CECBJPO.showActionsLink(context, args,
						SYMBOLIC_NAME_FOR_STATE_DCR_REVIEW));
			} else if (sParentType.equals(EngineeringConstants.TYPE_MECO)) {
				return (CECBJPO.showActionsLink(context, args,
						SYMBOLIC_NAME_FOR_STATE_MECO_CREATE));
			} else if (changeObj.isKindOf(context, PropertyUtil.getSchemaProperty(context,"type_DECO"))) {
				return (CECBJPO.showActionsLink(context, args,
						SYMBOLIC_NAME_FOR_STATE_ECO_DESIGN_WORK));
			}

			return Boolean.FALSE;
        }

        /**
         * The method is used to show / hide 'Add Existing'/'Remove' actions links in  Change Object
         * Assign/Reference Document page. The links will be enabled till all states prior to 'Create' state
         * for  MECO.
         * @param context the eMatrix <code>Context</code> object
         * @param args    holds the following input arguments:
         *           0 -  HashMap containing object id
         * @return        a <code>Boolean</code> object representing boolean true or false depending on whether to enable / disalbe link
         * @throws        Exception if the operation fails
         * @since
         **
         */
        //Added for bug 373962
        public Boolean showAddRemoveItemsLink(Context context, String[] args)
        throws Exception
        {
            // unpacking the arguments from variable args
             HashMap programMap           = (HashMap)JPO.unpackArgs(args);
           //getting parent object Id from args
            String strChangeObjId           = (String)programMap.get("objectId");
            DomainObject changeObj = new DomainObject(strChangeObjId);
           String strTypeName = changeObj.getInfo(context,DomainConstants.SELECT_TYPE);
           String strobjState = changeObj.getInfo(context,DomainConstants.SELECT_CURRENT);
           String strPolicy=changeObj.getInfo(context,DomainConstants.SELECT_POLICY);
           //Getting and representing the state 'Create'
           String strCreate =
                   FrameworkUtil.lookupStateName(context, strPolicy, SYMB_CREATE);
            if(strTypeName.equals(EngineeringConstants.TYPE_MECO))
            {
                if(strobjState.equalsIgnoreCase(strCreate))
                {
                    return Boolean.TRUE;
                }

                else
                {
                    return Boolean.FALSE;
                }
            }
            else
            {
                return Boolean.TRUE;
            }
        }

       //373962 end
    /**
     * To hide Related ECO Field in Create ECO webform
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @return Boolean.
     * @throws Exception If the operation fails.
     * @since EC X3
     */
        public boolean hideRelatedECRFieldPart(Context context, String []args) throws Exception
		     {
		        boolean hideECOField = true;
		             //creating parmaMap.
		        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		        String strObjectId =(String) paramMap.get("OBJId");

				if(strObjectId != null && !("").equals(strObjectId) && !("null").equals(strObjectId) && !isRelatedECRNotExists(context, paramMap)){
                	DomainObject domObj = new DomainObject(strObjectId);
					if (domObj.isKindOf(context, DomainConstants.TYPE_PART)) {
		             hideECOField=false;
		          }
				  }
				  else
				 {
					     hideECOField=false;
				 }
		        return hideECOField;
     }


    /**
    * To Show Conflict Column in EBOM Markup Indented  Table
    * based on the object type, if markup the conflict column will be displayed
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds HashMap.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since X+3.
    *
    */
    public Boolean isConflictAllowed(Context context, String[] args)
        throws Exception
    {

        ContextUtil.pushContext(context);
		boolean showConflicts = false;

		HashMap paramMap = (HashMap)JPO.unpackArgs(args);

		String markupId      = (String) paramMap.get("markupIds");

		if (markupId != null)
		{
			StringList strlMarkupIds = FrameworkUtil.split(markupId, ",");

			if (strlMarkupIds.size() > 1)
			{
			showConflicts = true;
		}
		else
		{
				DomainObject doMarkup = new DomainObject(markupId);
				String strCurrent = doMarkup.getInfo(context, SELECT_CURRENT);

				if ("Applied".equals(strCurrent))
				{
					showConflicts = false;
				}
				else
				{
			showConflicts = true;
		}
			}
		}
		else
		{
			showConflicts = false;
		}

            ContextUtil.popContext(context);

		return Boolean.valueOf(showConflicts);

}

    /**
    * To Show Markup Save/Save As link in EBOM emxTable/Indented  Table
    * based on the state of the Parent assembly Part
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since X+3.
    *
    */

    public Boolean isSaveMarkupAllowed(Context context, String[] args)
        throws Exception
    {
         boolean allowSave = false;
      try
        {
          HashMap paramMap = (HashMap)JPO.unpackArgs(args);
          String parentId      = (String) paramMap.get("objectId");
          HashMap settings    = (HashMap)paramMap.get("SETTINGS");
          String parentPartId = (String) paramMap.get("parentPartId");

		  String sImage = null;
		  Boolean sMarkUp = Boolean.FALSE;   //added for bug 355252
		  if(settings != null) {
			sImage = (String)settings.get("Image");
			if (sImage != null)
			{
				sImage = sImage.trim();
			}
         //added for bug 355252 starts
		    if (settings.containsKey("itemMarkup"))
			{
			  sMarkUp = Boolean.valueOf((String)settings.get("itemMarkup"));
			}
		 //added for bug 355252 ends
		  }


          if(parentPartId != null && !"null".equals(parentPartId)){
			  parentId = parentPartId;
		  }

		//modified as per changes for bug 311050
          //check the parent obj state
          StringList strList  = new StringList(4);
          strList.add(SELECT_CURRENT);
          strList.add(SELECT_POLICY);
          strList.add(SELECT_REVISION);
          strList.add("first.revision");

           DomainObject domObj = new DomainObject(parentId);
           Map map = domObj.getInfo(context,strList);

          String objState = (String)map.get(SELECT_CURRENT);
          String objPolicy = (String)map.get(SELECT_POLICY);

		  String propAllowLevel = null;
          //371781 - Modified the if else condition to check for PolicyClassification instead of Policy
          String policyClassification = EngineeringUtil.getPolicyClassification(context,objPolicy);
          if("Production".equalsIgnoreCase(policyClassification))
		  {
			  propAllowLevel = (String)FrameworkProperties.getProperty(context, "emxEngineeringCentral.ECPart.AllowSaveMarkup");
		  }
          else if ("Development".equalsIgnoreCase(policyClassification) && (!sMarkUp.booleanValue())) //modified for bug 355252
		  {
			  propAllowLevel = (String)FrameworkProperties.getProperty(context, "emxEngineeringCentral.DevelopmentPart.AllowSaveMarkup");
		  }

          StringList propAllowLevelList = new StringList();

          if(propAllowLevel != null && !"null".equals(propAllowLevel) && propAllowLevel.length() > 0)
          {
            StringTokenizer stateTok = new StringTokenizer(propAllowLevel, ",");
            while (stateTok.hasMoreTokens())
             {
                String tok = (String)stateTok.nextToken();
                propAllowLevelList.add(FrameworkUtil.lookupStateName(context, objPolicy, tok));
             }
          }
           allowSave = propAllowLevelList.contains(objState);
       if(EngineeringUtil.isMBOMInstalled(context)){
    	   if(parentPartId != null && !"null".equals(parentPartId) && sImage!= null && sImage.indexOf("iconSmallMarkup.gif")!=-1){
    		   allowSave = false;
    	   }
       }
		//end of changes
        }catch (Exception e)
        {
           throw new Exception(e.toString());
        }

        return Boolean.valueOf(allowSave);
    }

    /**
    * To Show Apply link in EBOM emxTable/Indented  Table
    * based on the state of the Parent assembly Part
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since X+3.
    *
    */

    public Boolean isApplyAllowed(Context context, String[] args)
        throws Exception
    {
         boolean allowApply = false;
      try
        {
          HashMap paramMap = (HashMap)JPO.unpackArgs(args);
          String parentId      = (String) paramMap.get("objectId");
 //modified as per changes for bug 311050
          //check the parent obj state
          StringList strList  = new StringList(4);
          strList.add(SELECT_CURRENT);
          strList.add(SELECT_POLICY);
          strList.add(SELECT_REVISION);
          strList.add("first.revision");

           DomainObject domObj = new DomainObject(parentId);
           Map map = domObj.getInfo(context,strList);

          String objState = (String)map.get(SELECT_CURRENT);
          String objPolicy = (String)map.get(SELECT_POLICY);
          String objRev = (String)map.get(SELECT_REVISION);
          String objFirstRev = (String)map.get("first.revision");

		  String propAllowLevel = null;

          matrix.db.Access mAccess = domObj.getAccessMask(context);
          //371781 - modified the if else condition to check for PolicyClassification instead of Policy
          String policyClassification = EngineeringUtil.getPolicyClassification(context,objPolicy);
          if(mAccess.has(Access.cModify))
          {
          if("Production".equalsIgnoreCase(policyClassification))
		  {
			  propAllowLevel = (String)FrameworkProperties.getProperty(context, "emxEngineeringCentral.ECPart.AllowApply");
		  }
          else if ("Development".equalsIgnoreCase(policyClassification))
		  {
			  propAllowLevel = (String)FrameworkProperties.getProperty(context, "emxEngineeringCentral.DevelopmentPart.AllowApply");
		  }
          else if ("Unresolved".equalsIgnoreCase(policyClassification))
		  {
			  propAllowLevel = (String)FrameworkProperties.getProperty(context, "emxUnresolvedEBOM.ConfiguredPart.AllowApply");
		  }

          StringList propAllowLevelList = new StringList();

          if(propAllowLevel != null && !"null".equals(propAllowLevel) && propAllowLevel.length() > 0)
          {
            StringTokenizer stateTok = new StringTokenizer(propAllowLevel, ",");
            while (stateTok.hasMoreTokens())
             {
                String tok = (String)stateTok.nextToken();
                propAllowLevelList.add(FrameworkUtil.lookupStateName(context, objPolicy, tok));
             }
          }
           allowApply = propAllowLevelList.contains(objState);

           //371781 - Modified to check for PolicyClassification instead of Policy
           if (allowApply && "Production".equalsIgnoreCase(policyClassification))
           {
				String strIsVersion = domObj.getInfo(context, "attribute[" + PropertyUtil.getSchemaProperty(context,"attribute_IsVersion") + "]");

				if((!objRev.equals(objFirstRev) || "TRUE".equals(strIsVersion)) && (!(DomainConstants.STATE_PART_PRELIMINARY).equals(objState)))
				{
					allowApply = false;
				}
		   }
		   }
//end of changes
        }catch (Exception e)
        {
           throw new Exception(e.toString());
        }

        return Boolean.valueOf(allowApply);
    }

    /**
    * To Show Apply link in Part Markups Summary  Table
    * based on the state of the Parent assembly Part
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since X+3.
    *
    */

    public Boolean isPartApplyAllowed(Context context, String[] args)
        throws Exception
    {
         boolean allowApply = false;
      try
        {
          HashMap paramMap = (HashMap)JPO.unpackArgs(args);
          String parentId      = (String) paramMap.get("objectId");
 //modified as per changes for bug 311050
          //check the parent obj state
          StringList strList  = new StringList(5);
          strList.add(SELECT_CURRENT);
          strList.add(SELECT_POLICY);
          strList.add(SELECT_REVISION);
          strList.add("first.revision");
          strList.add(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);
          
           DomainObject domObj = new DomainObject(parentId);
           Map map = domObj.getInfo(context,strList);

          String objState = (String)map.get(SELECT_CURRENT);
          String objPolicy = (String)map.get(SELECT_POLICY);
          String objRev = (String)map.get(SELECT_REVISION);
          String objFirstRev = (String)map.get("first.revision");
          String objRelPlase = (String)map.get(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);
          
		  String propAllowLevel = null;
          //371781 - Modified the if else condition to check for PolicyClassification instead of Policy
          String policyClassification = EngineeringUtil.getPolicyClassification(context,objPolicy);
          if ("Production".equalsIgnoreCase(policyClassification) && "Production".equalsIgnoreCase(objRelPlase))
		  {
        	  propAllowLevel = (String)FrameworkProperties.getProperty(context, "emxEngineeringCentral.ECPart.PartApply");
        	    
		  }
          else if ("Production".equalsIgnoreCase(policyClassification) && "Development".equalsIgnoreCase(objRelPlase))
		  {
			  propAllowLevel = (String)FrameworkProperties.getProperty(context, "emxEngineeringCentral.DevelopmentPart.PartApply");
		  }

          StringList propAllowLevelList = new StringList();

          if(propAllowLevel != null && !"null".equals(propAllowLevel) && propAllowLevel.length() > 0)
          {
            StringTokenizer stateTok = new StringTokenizer(propAllowLevel, ",");
            while (stateTok.hasMoreTokens())
             {
                String tok = (String)stateTok.nextToken();
                propAllowLevelList.add(FrameworkUtil.lookupStateName(context, objPolicy, tok));
             }
          }
           allowApply = propAllowLevelList.contains(objState);

           //371781 - Modified the if else condition to check for PolicyClassification instead of Policy
           if (allowApply && "Production".equalsIgnoreCase(policyClassification))
           {
				if(!objRev.equals(objFirstRev))
				{
					allowApply = false;
				}
		   }
//end of changes
        }catch (Exception e)
        {
           throw new Exception(e.toString());
        }

        return Boolean.valueOf(allowApply);
    }

    /**
    * To Show Apply link in Affected Items  Table
    * based on the type of the Change
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since X+3.
    *
    */

    public Boolean isChangeApplyAllowed(Context context, String[] args)
        throws Exception
    {
         boolean allowApply = false;
      try
        {
          HashMap paramMap = (HashMap)JPO.unpackArgs(args);
          String sENCAffectedItemsTypeFilter = (String) paramMap.get("ENCAffectedItemsTypeFilter");
          
          if (TYPE_PART_MARKUP.equals(sENCAffectedItemsTypeFilter))
          {
          return Boolean.FALSE;
          }
          String parentId      = (String) paramMap.get("objectId");
 //modified as per changes for bug 311050
          //check the parent obj state
          StringList strList  = new StringList(1);
          strList.add(SELECT_TYPE);

           DomainObject domObj = new DomainObject(parentId);
           Map map = domObj.getInfo(context,strList);

          String objType = (String)map.get(SELECT_TYPE);

		  String propAllowLevel = (String)FrameworkProperties.getProperty(context, "emxEngineeringCentral.Change.AllowApply");

          StringList propAllowLevelList = new StringList();

          if(propAllowLevel != null && !"null".equals(propAllowLevel) && propAllowLevel.length() > 0)
          {
            StringTokenizer stateTok = new StringTokenizer(propAllowLevel, ",");
            while (stateTok.hasMoreTokens())
             {
                String tok = (String)stateTok.nextToken();
                propAllowLevelList.add(PropertyUtil.getSchemaProperty(context,tok));
             }
          }
           allowApply = propAllowLevelList.contains(objType);

//end of changes
        }catch (Exception e)
        {
           throw new Exception(e.toString());
        }

        return Boolean.valueOf(allowApply);
    }

    /**
    * Checking the Modify access.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since X3.
    *
    */
    public Boolean hasModificationAccess(Context context,String[] args) throws Exception
    {
       HashMap paramMap = (HashMap)JPO.unpackArgs(args);
       boolean bHasModifyAccess = false;
       matrix.db.Access mAccess  = null;
       String objectId      = (String) paramMap.get("objectId");
             try {

          DomainObject bosAccess = DomainObject.newInstance(context);
          bosAccess.setId(objectId);
          mAccess = bosAccess.getAccessMask(context);
          if(mAccess.has(Access.cModify))
          {
          bHasModifyAccess = true;
          }
        }catch (Exception e) {
         throw new Exception(e.toString());
        }
      return  Boolean.valueOf(bHasModifyAccess);

    }

        // method is added for the bug 347771 starts
    /**
    * Checking the ECR Modify access.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since X3.
    *
    */
    public Boolean hasECRModificationAccess(Context context,String[] args) throws Exception
    {

       HashMap paramMap = (HashMap)JPO.unpackArgs(args);

       boolean bHasModifyAccess = false;
       matrix.db.Access mAccess  = null;
       String strECRState = null;
       String objectId  = (String) paramMap.get("objectId");
       try {
    	  DomainObject bosAccess = DomainObject.newInstance(context);
          bosAccess.setId(objectId);
          mAccess = bosAccess.getAccessMask(context);
          //Added for IR-060044V6R2011x
          strECRState = PropertyUtil.getSchemaProperty(context, "policy",PropertyUtil.getSchemaProperty(context, "policy_ECR"),"state_PlanECO");
          if(mAccess.has(Access.cModify)&& PolicyUtil.checkState(context, objectId, strECRState, PolicyUtil.LT))
          {
        	  bHasModifyAccess = true;
          }
          //Ends here
        }catch (Exception e) {
         throw new Exception(e.toString());
        }

      return  Boolean.valueOf(bHasModifyAccess);

    }
//  method is added for the bug 347771 ends
	   /**
     * The method is used to show / hide 'Edit All' actions links in  Change Object
     * Affected Item page. The links will be enabled till all states up to 'Review' state
	 * for ECR and prior to Define Components for ECO.
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id
     * @return        a <code>Boolean</code> object representing boolean true or false depending on whether to enable / disalbe link
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **
     */

   		public Boolean showEditAllAffectedItemsLink(Context context, String[] args)
        throws Exception
		{
		  //unpacking the arguments from variable args
			 HashMap programMap           = (HashMap)JPO.unpackArgs(args);
          //getting parent object Id from args
            String strChangeObjId           = (String)programMap.get("objectId");
			DomainObject changeObj = new DomainObject(strChangeObjId);

            SelectList sSelStmts = new SelectList(4);
            sSelStmts.add(DomainConstants.SELECT_TYPE);
            sSelStmts.add("policy.property[PolicyClassification].value");
            sSelStmts.add("type.kindof["+DomainConstants.TYPE_ECR+"]");
            sSelStmts.add("type.kindof["+DomainConstants.TYPE_ECO+"]");
            sSelStmts.add("type.kindof["+PropertyUtil.getSchemaProperty(context, "type_MECO")+"]");

            Map objMap = changeObj.getInfo(context, sSelStmts);
            String policyClass = (String)objMap.get("policy.property[PolicyClassification].value");
            String isECR = (String)objMap.get("type.kindof["+DomainConstants.TYPE_ECR+"]");
            String isECO = (String)objMap.get("type.kindof["+DomainConstants.TYPE_ECO+"]");
            String isMECO = (String)objMap.get("type.kindof["+PropertyUtil.getSchemaProperty(context, "type_MECO")+"]");
            
            String sENCAffectedItemsTypeFilter = (String) programMap.get("ENCAffectedItemsTypeFilter");
            if (TYPE_PART_MARKUP.equals(sENCAffectedItemsTypeFilter))
            {
               return Boolean.FALSE;
            }
            if ("DynamicApproval".equals(policyClass))
            {
    			// Calling showActionsLink method of emxCommonEngineeringChangeBase JPO

                //emxCommonEngineeringChangeBase changed to emxCommonEngineeringChange for IR-022893V6R2011 - Starts
                emxCommonEngineeringChange_mxJPO CECBJPO = null;
                String strArrArgs [] = new String [1];
                CECBJPO = new emxCommonEngineeringChange_mxJPO(context, strArrArgs);
                //emxCommonEngineeringChangeBase changed to emxCommonEngineeringChange for IR-022893V6R2011 - Ends
                Boolean showLink = Boolean.FALSE;

                if (isECO.equalsIgnoreCase("TRUE"))
						{
							showLink = CECBJPO.showActionsLink(context,args,SYMBOLIC_NAME_FOR_STATE_ECO_DESIGN_WORK);
							return showLink;
						}
                else if (isECR.equalsIgnoreCase("TRUE"))
    					{
							showLink = CECBJPO.showActionsLink(context,args,SYMBOLIC_NAME_FOR_STATE_ECR_REVIEW);
							return showLink;
						}
                else if (isMECO.equalsIgnoreCase("TRUE"))
						{
							showLink = CECBJPO.showActionsLink(context,args,SYMBOLIC_NAME_FOR_STATE_MECO_REVIEW);
							return showLink;
						}

            }
                return Boolean.TRUE;
        }

	   /**
     * The method is used to show / hide the Class Code column in Visual Compare report
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     * @return        a <code>Boolean</code> object representing boolean true or false depending on whether to display Class Code column
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **
     */
	public Boolean isClassCodePresent(Context context, String[] args) throws Exception
	{
		  //unpacking the arguments from variable args
			 HashMap programMap           = (HashMap)JPO.unpackArgs(args);
          //getting Class Code from args
            String strClassCode           = (String)programMap.get("ClassCode");

            if ("true".equals(strClassCode))
            {
				return Boolean.TRUE;
			}
			else
			{
				return Boolean.FALSE;
			}
        }


    /**
    * To Show Create Item Markup link on part properties page
    * based on the state of the Part
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return Boolean.
    * @throws Exception If the operation fails.
    * @since X+3.
    *
    */

    public Boolean isSaveItemMarkupAllowed(Context context, String[] args)
        throws Exception
    {
         boolean allowSave = false;
        	HashMap paramMap = (HashMap)JPO.unpackArgs(args);
 			String objectId      = (String) paramMap.get("objectId");DomainObject dObj = new DomainObject(objectId);			
			String policyClassification = EngineeringUtil.getPolicyClassification(context, dObj.getPolicy(context).getName());
		    if (policyClassification.equalsIgnoreCase("Production")) {	
				 if (isSaveMarkupAllowed(context, args).booleanValue()) {			
					String relId      = (String) paramMap.get("relId");
					DomainObject doPart = new DomainObject(objectId);
					
					//BGTP changes
					String releasePhaseVal = doPart.getInfo(context, EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);
					if(EngineeringConstants.DEVELOPMENT.equals(releasePhaseVal))
					{
						allowSave = false;
						return Boolean.valueOf(allowSave);
					}
					
					String strWhereClause = null;
					if (relId != null && relId.length() > 0)
					{
						strWhereClause = "(current == " + proposedState + " || current == " + approvedState + ")";
						String strFromObjectId = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump",relId,"from.id");
						if(strFromObjectId != null && strFromObjectId.trim().length()>0) {
							DomainObject doChange = new DomainObject(strFromObjectId);
			//					String strType = doChange.getInfo(context, DomainConstants.SELECT_TYPE);
							if (doChange.isKindOf(context, PropertyUtil.getSchemaProperty(context,"type_Change")))
							{
								String strAppliedRel = "to[" + PropertyUtil.getSchemaProperty(context,"relationship_AppliedMarkup") + "].from.id == " + strFromObjectId;
								String strProposedRel = "to[" + PropertyUtil.getSchemaProperty(context,"relationship_ProposedMarkup") + "].from.id == " + strFromObjectId;
			
								strWhereClause = strWhereClause + " && (" + strAppliedRel + " || " + strProposedRel + ")";
							}
			
							MapList mapListMarkups = doPart.getRelatedObjects(context,
												DomainConstants.RELATIONSHIP_EBOM_MARKUP,
												PropertyUtil.getSchemaProperty(context,"type_ItemMarkup"),
												new StringList(DomainConstants.SELECT_ID),
													null, true, true, (short) 1, strWhereClause, null);
							if (mapListMarkups.size() == 0)
							{
								allowSave = true;
							} else { //Modified for ENG convergence
								allowSave = false;
							}
				 		}
					}
					else
					{
						allowSave = true;
					}
				}
         } else 
        	 allowSave = false;
	
		return Boolean.valueOf(allowSave);

  }

	 /**
     * The method is used to show / hide 'Add to Change' action link for an EBOM Markup
     * The link will be enabled only if the EBOM Markup is not connected to a change context.
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id
     * @return        a <code>Boolean</code> object representing boolean true or false depending on whether to enable / disalbe link
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **
     */

   		public Boolean showEBOMMarkupAddToChangeAction(Context context, String[] args)
        throws Exception
		{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
            String strMarkupId = (String)programMap.get("objectId");
			DomainObject markupObj = new DomainObject(strMarkupId);

            SelectList sSelStmts = new SelectList(2);
            String SELECT_PROPOSED_MARKUP_REL = "relationship[" +
                  RELATIONSHIP_PROPOSED_MARKUP + "]";
            String SELECT_APPLIED_MARKUP_REL = "relationship[" +
                  RELATIONSHIP_APPLIED_MARKUP + "]";
            String SELECT_APPLIED_PART_MARKUP_REL = "relationship[" +
            RELATIONSHIP_APPLIED_PART_MARKUP + "]";
            String SELECT_EBOM_MARKUP_DEVREL_POLICY = "relationship[" +
            POLICY_EBOM_MARKUP + "].from.policy";
            sSelStmts.add(SELECT_PROPOSED_MARKUP_REL);
            sSelStmts.add(SELECT_APPLIED_MARKUP_REL);
            sSelStmts.add(SELECT_APPLIED_PART_MARKUP_REL);
            sSelStmts.add(SELECT_EBOM_MARKUP_DEVREL_POLICY);
            Map objMap = markupObj.getInfo(context, sSelStmts);
            String strProposedMarkup = (String) objMap.get(SELECT_PROPOSED_MARKUP_REL);
            String strAppliedMarkup = (String) objMap.get(SELECT_APPLIED_MARKUP_REL);
            String strAppliedPartMarkup = (String) objMap.get(SELECT_APPLIED_PART_MARKUP_REL);
            String strPartPolicy = (String) objMap.get(SELECT_EBOM_MARKUP_DEVREL_POLICY);
            boolean showAction = true;
            if (DomainConstants.POLICY_DEVELOPMENT_PART.equals(strPartPolicy)|| "True".equalsIgnoreCase(strProposedMarkup)  || "True".equalsIgnoreCase(strAppliedMarkup) ||  "True".equalsIgnoreCase(strAppliedPartMarkup))
            {
                showAction = false;
            }
            return Boolean.valueOf (showAction);
        }
    /**
     * Added to check for dynamic approval.
     * This method will return true if the reviewer list field in ECO needs to be shown
     * @param context
     * @param args
     * @return boolean
     * @throws Exception
     * @since 2011x
     */
     public boolean checkCreateECOFieldDynamicApprovalPolicy(Context context, String []args) throws Exception
     {
        boolean hideECOField = true;
        String policyClassification = FrameworkUtil.getPolicyClassification(context, POLICY_ECO);
        if (!"DynamicApproval".equals(policyClassification)) {
                hideECOField = false;
        }
        return hideECOField;
     }
     /** Access Function for Estimated Cost and Target Cost fields of Part Clone page to check whether  PolicyClassification of Policy does not equal"Reported Part"
      * @param context
      * @param args
      * @return
      * @throws Exception
      * @since  R211
      */
     public boolean hideIfReportedPart(Context context, String []args) throws Exception
     {
        boolean hideField = false;
              //creating parmaMap.
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId =(String) paramMap.get("copyObjectId");
        String TYPE_CEP = PropertyUtil.getSchemaProperty(context,"type_ComplianceEnterprisePart");
        if(strObjectId != null ){
            DomainObject obj = new DomainObject(strObjectId);
           if(TYPE_CEP!=null && !obj.isKindOf(context, TYPE_CEP))
            {
            String policyClassification = EngineeringUtil.getPolicyClassification(context,obj.getPolicy(context).getName());
            if(!"Reported Part".equals(policyClassification))
            	hideField=true;
          }

          }
        return hideField;
     }
     public boolean isTypePartMarkup(Context context,
             String[] args) throws Exception
         {
            HashMap programMap         = (HashMap)JPO.unpackArgs(args);

            String sENCAffectedItemsTypeFilter = (String) programMap.get("ENCAffectedItemsTypeFilter");
             
             if (TYPE_PART_MARKUP.equals(sENCAffectedItemsTypeFilter))
             {
             return Boolean.FALSE;
             }
             else
             {
             return Boolean.TRUE;
             }
         }

   //Added for JSP to Common components conversion. Specification ->Related ECRs/ECOs
	public Boolean isDSCInstalled(Context context , String[] args) throws Exception {
		boolean isDSCInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionDesignerCentral",false,null,null);
		return Boolean.valueOf(isDSCInstalled);
	}
	
	/**
	 * Method to check whether ENG is installed
	 * 
	 * appVersionX-BOMEngineering to check for ENG installation
	 * featureVersionX-BOMEngineering to check for ECC installation
	 * 
	 * @param context
	 * @return boolean
	 * @throws Exception
	 */
	public Boolean isENGInstalled(Context context, String[] args) throws FrameworkException {
		return EngineeringUtil.isENGInstalled(context, args);
	}    
	
	/**
	 * Method to display ActiveECOECR filed in part properties page (only in view mode)
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Boolean displayActiveECOECRField(Context context, String[] args) throws Exception {
		if(!EngineeringUtil.isENGInstalled(context, args)) {
			return false;
		}
		
		return (Boolean)checkViewMode(context, args);
	}
	
	/**
	 * Method to restrict chart command only for Engineering BOM View
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Boolean displayChartCommand(Context context, String[] args) throws Exception {
        HashMap paramMap  = (HashMap)JPO.unpackArgs(args);        
        String bomMode	  = (String)paramMap.get("BOMMode");
        return ("ENG".equalsIgnoreCase(bomMode)) ? true : false;
	}
	
	/**
	 * Method to display basic attributes in part properties
	 * page when opened in SlideIn or Launch. 
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 */
	public boolean showInSlideIn(Context context, String[] args) throws Exception {
		
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		boolean isLanched = Boolean.parseBoolean((String)programMap.get("launched"));
		String targetLocation = (String)programMap.get("targetLocation");
		
		if(!isLanched)
			return "slidein".equalsIgnoreCase(targetLocation);
		
		return isLanched;
	}
	
	/**
	 * Method to hide Add Change menu in part properties
	 * page when opened in Launch. 
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 */
	public boolean hideAddChangeInLaunch(Context context, String[] args) throws Exception {		
		HashMap programMap = (HashMap)JPO.unpackArgs(args);	
		String showChangeMgtToolbar=(String)programMap.get("showChangeMgtToolbar");
		String launched = (String)programMap.get("launched");
		
		if (UIUtil.isNullOrEmpty(launched)) { launched = "false"; } 
		
		if("true".equals(showChangeMgtToolbar) && "false".equals(launched)){
			return JPO.invoke(context, "enoECMChangeUtil", null, "displayConnectedCACOLegacyObjects", args, Boolean.class);
		}
		
		return false;
		
		
	}

	public boolean hideImportBOM(Context context, String[] args) throws Exception {
		HashMap programMap = JPO.unpackArgs(args);

		String hideImportBOM = (String) programMap.get("fromOpenMarkup");
		
		if ( UIUtil.isNullOrEmpty(hideImportBOM) ) { hideImportBOM = (String) programMap.get("hideImportBOM"); } // used to hide the BOM import command from Configured BOM view.
		
		return !"true".equals(hideImportBOM);
	}

	//Access function to hide/show BOM replace existing/new commands  in bom powerview/RMB
	public Boolean showOrHideReplaceNewCommandInBOM(Context context, String[] args)
	throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);		
		String showRMBCommands    = (String) programMap.get("showRMBInlineCommands");
        String frmRMB    = (String) programMap.get("isRMB");
        /*if("true".equalsIgnoreCase(frmRMB)){
	        	return Boolean.FALSE;
        }*/
		return showOrHideReplaceCommandsInBOM(context,args) && checkCreatePartAccess(context,args);
	}

	//Access function to hide/show BOM replace existing/new commands  in bom powerview/RMB
	public Boolean showOrHideReplaceCommandsInBOM(Context context, String[] args)
	throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);		
		String bomMode	      = (String) programMap.get("BOMMode");
		String fromMarkupView = (String) programMap.get("fromOpenMarkup");
		String showReplaceCommands = (String) programMap.get("showReplaceCommands");
		String showRMBCommands    = (String) programMap.get("showRMBInlineCommands");
        String frmRMB    = (String) programMap.get("isRMB");
        String mode    = (String) programMap.get("sbMode");
        /*if("true".equalsIgnoreCase(frmRMB)){
	        	return Boolean.FALSE;
        }*/
		return ("ENG".equalsIgnoreCase(bomMode) || "true".equalsIgnoreCase(fromMarkupView) ||  "true".equalsIgnoreCase(showReplaceCommands)) ? true : false;
	}
	
	//Access function to hide/show BOM replace existing/new commands  in Configured BOM
	public Boolean showOrHideReplaceCommandsInConfigBOM(Context context, String[] args)
	throws Exception
	{
		HashMap programMap    = (HashMap) JPO.unpackArgs(args);		
		String fromConfigBOM  = (String) programMap.get("fromConfigBOM");
		String frmRMB    = (String) programMap.get("isRMB");
        if("true".equalsIgnoreCase(frmRMB)){
	        	return Boolean.FALSE;
        }
		return Boolean.valueOf("true".equalsIgnoreCase(fromConfigBOM) ? true : false) ;
	}
	public Boolean showOrHideRMBCommandsInConfigBOM(Context context, String[] args)
			throws Exception
			{
				HashMap programMap    = (HashMap) JPO.unpackArgs(args);		
				String fromConfigBOM  = (String) programMap.get("fromConfigBOM");
				String frmRMB    = (String) programMap.get("isRMB");
		        
				return Boolean.valueOf("true".equalsIgnoreCase(fromConfigBOM) ? true : false) ;
			}
	public Boolean showOrHideFloatRMBCommandsInBOMorPartWhereUsed(Context context, String[] args)
	throws Exception
	{
		HashMap programMap   = (HashMap) JPO.unpackArgs(args);	
		String bomMode	     = (String) programMap.get("BOMMode");
		String partWhereUsed = (String) programMap.get("partWhereUsed");
		String fromConfigBOM = (String) programMap.get("fromConfigBOM");
		HashMap settingsMap  = (HashMap)programMap.get("SETTINGS");
		
		// Disable float RMB commands in configured part Where Used
		if("true".equalsIgnoreCase(partWhereUsed)) {
			String policy = DomainObject.newInstance(context, (String)programMap.get("objectId")).getInfo(context, SELECT_POLICY);
			if(PropertyUtil.getSchemaProperty(context, "policy_ConfiguredPart").equalsIgnoreCase(policy)) {
				return Boolean.FALSE;
			}
		}
		
		//Show these commands only in case of BOM or part where used
		return ("true".equalsIgnoreCase(partWhereUsed) || "ENG".equalsIgnoreCase(bomMode) || "true".equalsIgnoreCase(fromConfigBOM)) ? true : false;
	}
	
	//Access function to hide/show BOM replace existing/new commands  in Configured BOM
	public Boolean displayTitleInENGView(Context context, String[] args)
	throws Exception
	{
		HashMap programMap    = (HashMap) JPO.unpackArgs(args);		
		String type           = (String) programMap.get("type");
		
		//If type is Change type then title column should not be displayed
		return (UIUtil.isNotNullAndNotEmpty(type) && type.indexOf("Change") > -1 ) ? Boolean.FALSE : Boolean.TRUE ;
	}
	
	public Boolean hideOrShowMarkUpCommands(Context context, String[] args)
			throws Exception
			{
				HashMap programMap    = (HashMap) JPO.unpackArgs(args);		
				String objectId           = (String) programMap.get("objectId");
				
				DomainObject dObj = DomainObject.newInstance(context, objectId);
				StringList slObjectSels = new StringList();
				slObjectSels.addElement(SELECT_TYPE);
				slObjectSels.addElement(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);
				
				Map mpSel = dObj.getInfo(context, slObjectSels);
				String type = (String) mpSel.get(SELECT_TYPE);
				String strRelphase = (String) mpSel.get(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);
				
				if(!(type.indexOf("Change")>-1)){
					return ("Production".equals(strRelphase)) ? true : false;
				}
				return true;
			}
	public Boolean hideOrShowMarkUpCommandsForDevPart(Context context, String[] args)
			throws Exception
			{
				return !(hideOrShowMarkUpCommands(context,args));
			}
	
	 /**
     * Method to check 3D play command show
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Boolean show3DPlayToggleCommand(Context context, String[] args) throws Exception {
    	String s3DPlayshow = EnoviaResourceBundle.getProperty(context,"emxComponents.Toggle.3DViewer");
    	if(!EngineeringUtil.checkForDECorVPMInstallation(context) || !"true".equalsIgnoreCase(s3DPlayshow)) {
            return false;
        }

    	String pref3DPlay = PropertyUtil.getAdminProperty(context, "Person", context.getUser(), "preference_3DPlayToggle");
    	boolean flag = ("Hide".equalsIgnoreCase(pref3DPlay) || "".equalsIgnoreCase(pref3DPlay));
  

    	if(flag) {
    		return EngineeringUtil.isReportedAgainstItemPart(context, args);
    	}

	    return flag;
    }

    /**
     * Method to check 3D play command hide
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Boolean hide3DPlayToggleCommand(Context context, String[] args) throws Exception {
    	String s3DPlayshow = EnoviaResourceBundle.getProperty(context,"emxComponents.Toggle.3DViewer");
    	if(!EngineeringUtil.checkForDECorVPMInstallation(context) || !"true".equalsIgnoreCase(s3DPlayshow)) {
            return false;
        }

    	String pref3DPlay = PropertyUtil.getAdminProperty(context, "Person", context.getUser(), "preference_3DPlayToggle");
    	boolean flag = "Show".equalsIgnoreCase(pref3DPlay);

    	if(flag) {
    		return EngineeringUtil.isReportedAgainstItemPart(context, args);
    	}

	    return flag;
    }
    /**
     * This method checks whether 3DPlay channel enabled or not
     * returns true if all all the following conditions are satisfied
     *   1)3dPlay properties is true or false
     *
     * @param context the matrix context
     * @param args
     * @return true if all the above mentioned conditions are satisfied
     * @throws Exception
     */
    public Boolean show3dPlayChannelForPartProperties (Context context,String[] args) throws Exception {
  		String s3DPlayshow = EnoviaResourceBundle.getProperty(context,"emxComponents.Toggle.3DViewer");
          if("true".equalsIgnoreCase(s3DPlayshow)){
              return true;
          }
          return false;
    }
    /**
     * This method checks whether 3Dlive command enabled or not
     * returns true if all all the following conditions are satisfied
     *   1)VPM/TBE or DEC installed
     *   2)3dPlay properties is true or false
     *
     * @param context the matrix context
     * @param args
     * @return true if all the above mentioned conditions are satisfied
     * @throws Exception
     */
    public Boolean showCrossHighlightForPartProperties (Context context,String[] args) throws Exception {
  		String s3DPlayshow = EnoviaResourceBundle.getProperty(context,"emxComponents.Toggle.3DViewer");
          if(!EngineeringUtil.checkForDECorVPMInstallation(context) || "true".equalsIgnoreCase(s3DPlayshow)){
              return false;
          }
          return true;
    }
    /**
     * This method checks whether cross highlight should be enabled or not
     * returns true if all all the following conditions are satisfied
     *   1)VPM/TBE or DEC installed
     *   2)User preference is On
     *
     * @param context the matrix context
     * @param args
     * @return true if all the above mentioned conditions are satisfied
     * @throws Exception
     */
    public Boolean showCrossHighlight (Context context,String[] args) throws Exception {
  		String s3DPlayshow = EnoviaResourceBundle.getProperty(context,"emxComponents.Toggle.3DViewer");
          if(!EngineeringUtil.checkForDECorVPMInstallation(context) || "true".equalsIgnoreCase(s3DPlayshow)){
              return false;
          }
          String pref3DLive = PropertyUtil.getAdminProperty(context, "Person", context.getUser(), "preference_3DLiveExamineToggle");
          return "Show".equalsIgnoreCase(pref3DLive);
      }

     /**
      * Method to checks to show or hide 3d play commands.
      * @param context the matrix context
      * @return boolean true/false
      * @throws Exception
      */
 	public Boolean showOrHide3DPlay(Context context,String[] args)throws Exception{
 				String s3DPlayshow = EnoviaResourceBundle.getProperty(context, "emxComponents.Toggle.3DViewer");
 				String pref3DPlay = PropertyUtil.getAdminProperty(context, "Person", context.getUser(), "preference_3DPlayToggle");
 		    	boolean flag = "Show".equalsIgnoreCase(pref3DPlay);
 		    	
 				return ("true".equalsIgnoreCase(s3DPlayshow) && flag) ? true : false;
 			} 
    /**
     * Method to checks to show or hide 3d play commands for BOMCompare.
     * @param context the matrix context
     * @return boolean true/false
     * @throws Exception
     */
	public Boolean showOrHide3DPlayforBOMCompare(Context context,String[] args)throws Exception{
				String s3DPlayshow = EnoviaResourceBundle.getProperty(context, "emxComponents.Toggle.3DViewer");		    	
				return ("true".equalsIgnoreCase(s3DPlayshow)) ? true : false;
			} 
	/**
     * Method to checks to show or hide Spare Part within context command.
     * @param context the matrix context
     * @return boolean true/false
     * @throws Exception
     */
	public Boolean showOrHideFilterSparePartWithinBomContext(Context context,String[] args)throws Exception{
				String sFilterSparePartsWithinBOMContext = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.Command.SparePartWithinBOMContextEnabled");	
				boolean connectOrDisconnect = isToConnectOrDisConnect(context,args);
				return (connectOrDisconnect && "Yes".equalsIgnoreCase(sFilterSparePartsWithinBOMContext)) ? true : false;
			} 

	/**
	 * Access function to display Export All to Excel command only in My Engineering View .
	 * @param context the eMatrix Context object.
	 * @param args.
	 * @return boolean. True if it is from My Engineering View and false in other navigations.
	 * @throws Exception
	 */
	public boolean showInMyENGViewOnly(Context context, String[] args) throws Exception{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String scustomSearchCriteria = (String)programMap.get("customSearchCriteria");
		return "true".equals(scustomSearchCriteria);
	}

}
