/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

/**
 * This JPO contains the code formerly contained in the following files:
 *   com/matrixone/framework/beans/PartDefinition.java
 *   com/matrixone/framework/beans/CADDrawing.java
 *   com/matrixone/framework/beans/CADModel.java
 *   com/matrixone/framework/beans/DrawingPrint.java
 * for triggers.
 *
 */

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Attribute;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.FileList;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DebugUtil;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PolicyUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.engineering.ECO;
import com.matrixone.apps.engineering.EngineeringConstants;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.engineering.PartDefinition;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * The <code>emxPartDefinitionBase</code> class contains promote triggers for the following types:
 *
 *     CAD Drawing, CAD Model, and Drawing Print.
 *
 * @version EC Rossini - Copyright (c) 2003, MatrixOne, Inc.
 */
public class emxPartDefinitionBase_mxJPO extends emxDomainObject_mxJPO
{
	public static final String RELATIONSHIP_AFFECTED_ITEM =
             PropertyUtil.getSchemaProperty("relationship_AffectedItem");

	public static final String RELATIONSHIP_ASSIGNED_AFFECTED_ITEM =
    	PropertyUtil.getSchemaProperty("relationship_AssignedAffectedItem");

	public static final String ATTRIBUTE_REQUESTED_CHANGE =
    	PropertyUtil.getSchemaProperty("attribute_RequestedChange");

	public static final String sDgnEngr = PropertyUtil.getSchemaProperty( "role_DesignEngineer");
	public static final  String sSrDgnEngr = PropertyUtil.getSchemaProperty( "role_SeniorDesignEngineer");
	public static final  String sMfgEngr = PropertyUtil.getSchemaProperty( "role_ManufacturingEngineer");
	public static final  String sSrMfgEngr = PropertyUtil.getSchemaProperty( "role_SeniorManufacturingEngineer");
	public static final  String sECREvtr = PropertyUtil.getSchemaProperty( "role_ECREvaluator");
	public static final  String sECRCdtr = PropertyUtil.getSchemaProperty( "role_ECRChairman");
	public static final  String sECRChrm = PropertyUtil.getSchemaProperty( "role_ECRCoordinator");

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     * @since EC Rossini.
     */
    public emxPartDefinitionBase_mxJPO (Context context, String[] args)
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
     * @since AEF 9.5.0.0.
     */

    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception("must specify method on emxPartDefinition invocation");
        }
        return 0;
    }

    /**
     * Floats the unfulfilled ECRs attached to the previous revison of the specification to this revision.
     * Uses the Request Specification Revision relationship and calls
     * floatUnfulfilledECR method which does the following:
     *   - Checks the previous rev for connected ECRs that are not
     *     fulfilled or rejected.
     *   - Floats them to this revision if they do not have an ECO attached.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no input arguments.
     * @return void.
     * @throws Exception if the operation fails.
     * @since EC 10.0.0.0.
     * @trigger PolicyCADDrawingStateReviewPromoteAction.
     * @trigger PolicyCADModelStateReviewPromoteAction.
     * @trigger PolicyDrawingPrintStateReviewPromoteAction.
     */
    public void floatUnfulfilledECRs(Context context, String[] args)
        throws Exception
    {
        DebugUtil.debug("emxPartDefinition:floatUnfulfilledECRs");

        // Float Unfulfilled ECR to Released CAD Drawing
        StringList relTypes = new StringList();
		relTypes.addElement(RELATIONSHIP_AFFECTED_ITEM);
      String triggerPolicyStates = POLICY_ECR;
      triggerPolicyStates += "|";
      triggerPolicyStates += STATE_ECR_COMPLETE;
      triggerPolicyStates += "|";
        floatUnfulfilledECR(context,relTypes,triggerPolicyStates);
    }

    /**
     * Floats the unfulfilled ECRs attached to the previous revison of the specification to this revision.
     * The following steps are performed:
     *   - Checks the previous rev for connected ECRs that are not
     *     fulfilled or rejected.
     *   - Floats them to this revision if they do not have an ECO attached.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param relTypes list of relationship types.
     * @param policyStates states for the policy.
     * @return void.
     * @throws Exception if the operation fails.
     * @since EC 10.0.0.0.
     */
    protected void floatUnfulfilledECR(Context context, StringList relTypes, String policyStates)
                    throws Exception
    {
        Pattern relPattern = new Pattern(null);
        Enumeration e = relTypes.elements();
        while (e.hasMoreElements())
            relPattern.addPattern((String)e.nextElement());

        StringList selectRelStmts = new StringList(2);
        selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
        selectRelStmts.addElement(SELECT_RELATIONSHIP_TYPE);

        StringList selectStmts  = new StringList(3);
        selectStmts.addElement(SELECT_ID);
        selectStmts.addElement(SELECT_CURRENT);
        selectStmts.addElement(SELECT_POLICY);

        try
        {
            BusinessObject bo = getPreviousRevision(context);
            if (bo == null || bo.getTypeName().equals(""))
                return;

            PartDefinition prevRevPartDefinition = new PartDefinition(bo);


            MapList mapList =
                    prevRevPartDefinition.getRelatedObjects(
                                        context,
                                        relPattern.getPattern(),           // relationship pattern
                                        DomainConstants.TYPE_ECR,              // object pattern
                                        selectStmts,                       // object selects
                                        selectRelStmts,                   // relationship selects
                                        true,                                 // to direction
                                        false,                               // from direction
                                        (short) 1,                          // recursion level
                                        EMPTY_STRING,                // object where clause
                                        EMPTY_STRING);               // relationship where clause

            Vector policies = new Vector();
            Vector states = new Vector();
            Vector reconnectList = new Vector();
            Vector idsOfECRsConnectedToECO = new Vector();
            boolean hasECRsConnectedToECO = false;
            StringTokenizer tokens = new StringTokenizer(policyStates,"|");
            while(tokens.hasMoreTokens())
            {
                policies.addElement(tokens.nextToken());
                states.addElement(tokens.nextToken());
            }
            int polSize = policies.size();
            if (mapList.size() > 0)
            {
                // if they're are ECRs, then get the ECO (to check later if ECRs are connected to ECO)
                selectRelStmts = new StringList();
                selectStmts  = new StringList(1);
                selectStmts.addElement(SELECT_ID);
                MapList ecoMapList = getECO(context,selectStmts,selectRelStmts);
                // if there is an ECO connected get it's ECRs
                if (ecoMapList.size() > 0)
                {
                    Map ecoMap = (Map)ecoMapList.get(0);
                    ECO eco = new ECO((String)ecoMap.get(SELECT_ID));
                    MapList ecrMapList = eco.getECRs(context,selectStmts,selectRelStmts);
                    int mapSize = ecrMapList.size();
                    if (mapSize > 0)
                    {
                        hasECRsConnectedToECO = true;
                        for (int i = 0; i < mapSize; i++)
                        {
                          Map ecrMap = (Map)ecrMapList.get(i);
                            idsOfECRsConnectedToECO.addElement((String)ecrMap.get(SELECT_ID));
                        }
                    }
                }
            }
            int mapSize = mapList.size();
            for (int i = 0; i < mapSize; i++)
            {
              Map map = (Map)mapList.get(i);
                String policy = (String)map.get(SELECT_POLICY);
                String state = (String)map.get(SELECT_CURRENT);
                boolean reconnect = true;
                for(int j = 0; j < polSize; j++)
                {
                    if (policy.equals(policies.elementAt(j)) && state.equals(states.elementAt(j)))
                    {
                        reconnect = false;
                        break;
                    }
                }
                if (reconnect && hasECRsConnectedToECO)
                {
                    // Make sure ecr is not attached to ECO
                    String id = (String)map.get(SELECT_ID);
                    e = idsOfECRsConnectedToECO.elements();
                    while (e.hasMoreElements())
                    {
                        if (id.equals((String)e.nextElement()))
                        {
                            reconnect = false;
                            break;
                        }
                    }
                }
                if (reconnect)
                    reconnectList.addElement(map);
            }
            e = reconnectList.elements();
            while (e.hasMoreElements())
            {
                Map map = (Map)e.nextElement();
                String relId = (String)map.get(SELECT_RELATIONSHIP_ID);
                String relType = (String)map.get(SELECT_RELATIONSHIP_TYPE);
                String objId = (String)map.get(SELECT_ID);

				String strRelName = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump", relId, "name");
				if (strRelName != null && strRelName.length() > 0 && strRelName.equals(RELATIONSHIP_AFFECTED_ITEM)){
				String strAssignedECRelId = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",relId,"tomid["+ RELATIONSHIP_ASSIGNED_AFFECTED_ITEM +"].fromrel[" + DomainConstants.RELATIONSHIP_ASSIGNED_EC + "].id");
				DomainRelationship domRel = new DomainRelationship(relId);
				String strRequestedChangeValue = domRel.getAttributeValue(context,ATTRIBUTE_REQUESTED_CHANGE);
                DomainRelationship.disconnect(context,relId);
                // connect to this rev
                ContextUtil.pushContext(context);
				MqlUtil.mqlCommand(context,"trigger off");
				 ContextUtil.popContext(context);
				DomainRelationship dNewAffectedRel = DomainRelationship.connect(context,new DomainObject(objId),relType,this);
				String strNewAffectedRelId = dNewAffectedRel.toString();
				try{//IR:054393
				if (strRequestedChangeValue != null && strRequestedChangeValue.length() > 0){
				DomainRelationship domNewAffectedRel = new DomainRelationship(strNewAffectedRelId);
				domNewAffectedRel.setAttributeValue(context,ATTRIBUTE_REQUESTED_CHANGE,strRequestedChangeValue);
				}
				}finally{
					ContextUtil.pushContext(context);
				MqlUtil.mqlCommand(context,"trigger on");
				ContextUtil.popContext(context);
				}
				MqlUtil.mqlCommand(context,"add connection $1 fromrel $2 torel $3",RELATIONSHIP_ASSIGNED_AFFECTED_ITEM,strAssignedECRelId,strNewAffectedRelId);
				} else {
                // disconnect from prev rev
                DomainRelationship.disconnect(context,relId);

                // connect to this rev
                DomainRelationship.connect(context,new DomainObject(objId),relType,this);
            }
        }
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    /**
     * Gets the ECO for the part definition.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param selectStmts The list of selects.
     * @param selectRelStmts The list of relationship selects.
     * @return a MapList of ECO information.
     * @throws Exception if the operation fails.
     * @since EC 10.0.0.0.
     */
    protected MapList getECO(Context context,StringList selectStmts,StringList selectRelStmts)
                throws Exception
    {
        DebugUtil.debug("emxPartDefinition:getECO");
        Pattern relPattern = new Pattern(RELATIONSHIP_AFFECTED_ITEM);
        return (getRelatedObjects(
                        context,
                         relPattern.getPattern(),      // relationship pattern
                        QUERY_WILDCARD,                                           // object pattern
                        selectStmts,                                                     // object selects
                        selectRelStmts,                                                // relationship selects
                        true,                                                              // to direction
                        false,                                                             // from direction
                        (short)1,                                                        // recursion level
                        EMPTY_STRING,                                             // object where clause
                        EMPTY_STRING));                                           // relationship where clause
    }
    
    /**
	 * Get Incomplete Realized Change Order from object id.
	 * 
	 * @param context
	 * @param slSelectables . Change order selectables.
	 * @param object Id
	 * @retrun incomplete Change Order Id
	 * @throws Exception
	 */
    
public static String getIncompleteRealizedCOId(Context context, StringList slSelectables, String sObjectId) throws MatrixException{
	
	String sChangeOrderId = EMPTY_STRING;
	if(UIUtil.isNullOrEmpty(sObjectId)){
		return sChangeOrderId;
	}
	Map  realizedCOData = com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil.getChangeObjectsInRealized(context, slSelectables, new String[]{sObjectId}, 2);
	MapList realizedchangeOrderList = (MapList)realizedCOData.get(sObjectId);
	  for(int i=0; i<realizedchangeOrderList.size();i++){
		  Map mCAInfo = (Map)realizedchangeOrderList.get(i);
			String sCOId = (String)mCAInfo.get(EngineeringConstants.SELECT_ID);
			String sState = DomainObject.newInstance(context,sCOId).getInfo(context, SELECT_CURRENT);
			if(!ChangeConstants.STATE_COMPLETE.equals(sState)){
				sChangeOrderId=  sCOId;
			}
	  }
	  return sChangeOrderId;
}
/**
 * Get Incomplete Proposed Change Order from object id.
 * 
 * @param context
 * @param slSelectables . Change order selectables.
 * @param object Id
 * @retrun incomplete Change Order Id
 * @throws Exception
 */
public static String getIncompleteProposedCOId(Context context, StringList slSelectables, String sObjectId) throws MatrixException{
	String sChangeOrderId = EMPTY_STRING;
	if(UIUtil.isNullOrEmpty(sObjectId)){
		return sChangeOrderId;
	}
	Map proposedCAData  = com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil.getChangeObjectsInProposed(context, slSelectables, new String[]{sObjectId}, 1);
	MapList proposedchangeActionList = (MapList)proposedCAData.get(sObjectId);
	  for(int i=0; i<proposedchangeActionList.size();i++){
		  Map mCAInfo = (Map)proposedchangeActionList.get(i);
			String sCOId = (String)mCAInfo.get(EngineeringConstants.SELECT_ID);
			String sState = DomainObject.newInstance(context,sCOId).getInfo(context, SELECT_CURRENT);
			if(!ChangeConstants.STATE_COMPLETE.equals(sState)){
				sChangeOrderId=  sCOId;
			}
	  }
	  return sChangeOrderId;
}

  /**
   * Floats the latest Released or higher Reference Documents attached to the previous revision
   * of this part to this revision.
   * The following steps are performed:
   *   - Checks the previous rev for connected objects that are connected
   * with Reference Document Relationship.
   *   - Floats the latest released and higher revisions to this revision.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds following input arguments:
   *     0 - policy - a String holding the Policy name.
   *     1 - floatOnState - a String holding the State name.
   * @throws Exception if the operation fails.
   * @return void.
   * @since EC 10.0.0.0.
   * @trigger PolicyCADDrawingStateApprovedPromoteAction.
   * @trigger PolicyCADModelStateApprovedPromoteAction.
   * @trigger PolicyDrawingPrintStateApprovedPromoteAction.
   */
public void floatReferenceDocumentsOnRelease(Context context, String[] args)
          throws Exception
  {
    String state = "";
    String oid = "";
    boolean checkPartExists = false;

    try
    {
      String policy = (String) args[0];
      String floatOnState = (String) args[1];
      StringList strListID = getInfoList(context,"to["+RELATIONSHIP_REFERENCE_DOCUMENT+"].from.id");
      
      String objectId=this.getId(context);      
    //checking float to older revision key is disable or not 
  	  String sFloatToOlderRevision= FrameworkProperties.getProperty("eServiceEngineeringCentral.ManagePartFloatwhenRevisedoneSameChange");
  	  String sChangeOrder=null;
  	  if("True".equalsIgnoreCase(sFloatToOlderRevision)){
  		  StringList selectStmts = new StringList();
  		  selectStmts.add(SELECT_ID);
  		  selectStmts.add(SELECT_CURRENT);
  		  sChangeOrder = getIncompleteRealizedCOId(context, selectStmts,objectId);

  		  if(UIUtil.isNullOrEmpty(sChangeOrder)){
  			  sChangeOrder = getIncompleteProposedCOId(context, selectStmts,objectId);
  		  }

  	  }
      
      if(policy == null || "".equals(policy))
      {
        policy = getInfo(context,SELECT_POLICY);
      }
      else
      {
        policy = PropertyUtil.getSchemaProperty(context,policy);
      }

      BusinessObject bo = getPreviousRevision(context);

      ContextUtil.pushContext(context);

      String stateRelease = PropertyUtil.getSchemaProperty(context,"policy", policy, floatOnState);

      if (bo == null || bo.getTypeName().equals(""))
      {
        return;
      }

      // Bus selects
      StringList selectStmts  = new StringList(8);
      selectStmts.addElement(SELECT_ID);
      selectStmts.addElement("next.current");
      
      if("True".equalsIgnoreCase(sFloatToOlderRevision)){	
	      selectStmts.addElement(DomainConstants.SELECT_CURRENT);
		  selectStmts.addElement(DomainConstants.SELECT_REVISION);
		  selectStmts.addElement(DomainConstants.SELECT_NAME);
		  selectStmts.addElement("next.current");
		  selectStmts.addElement("next.id");
      }
      // Rel selects
      StringList selectRelStmts = new StringList(1);
      selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

      DomainObject prevRevObject = new DomainObject(bo);

      MapList mapList = prevRevObject.getRelatedObjects(context,
                               RELATIONSHIP_REFERENCE_DOCUMENT, // relationship pattern
                               TYPE_PART,                               // object pattern
                               selectStmts,                       // object selects
                               selectRelStmts,                    // relationship selects
                               true,                              // to direction
                               false,                             // from direction
                               (short) 1,                         // recursion level
                               EMPTY_STRING,                      // object where clause
                               EMPTY_STRING);                             // relationship where clause


      if (mapList.size() == 0)
      {
        return;
      }
      String partId;
      String sCurrentState;
      String sNextRevState ;
      String sNextRevId;
      String sNextRevAssociatedCO = null;
         
      String releaseState  = PropertyUtil.getSchemaProperty(context, "policy",DomainConstants.POLICY_EC_PART, "state_Release");
        
      Iterator partItr = mapList.iterator();

      while (partItr.hasNext())
      {
          Map partInfo = (Map) partItr.next();
          // Get the part id and the relationship id
          partId = (String)partInfo.get(SELECT_ID);
          String relId = (String)partInfo.get(SELECT_RELATIONSHIP_ID);
          if(strListID.contains(partId)){
        	  checkPartExists = true;
          }
          if(checkPartExists){
        	  continue;
          }
          if("True".equalsIgnoreCase(sFloatToOlderRevision)){
        	  sCurrentState = (String)partInfo.get(DomainConstants.SELECT_CURRENT);
        	  sNextRevState = (String)partInfo.get("next.current");
        	  sNextRevId = (String)partInfo.get("next.id");
        	  sNextRevAssociatedCO = getIncompleteRealizedCOId(context, selectStmts, sNextRevId);
        	  if(UIUtil.isNullOrEmpty(sNextRevAssociatedCO)){
        		  sNextRevAssociatedCO = getIncompleteProposedCOId(context, selectStmts, sNextRevId);
        	  }
        	  //connect latest released rev with latest reference document(added additional condition to check next revision state is not in released to find only latest released revision)
        	  if(PolicyUtil.checkState(context, partId, releaseState,PolicyUtil.LT) || (releaseState.equalsIgnoreCase(sCurrentState) && (!(sChangeOrder.equalsIgnoreCase(sNextRevAssociatedCO)) && !releaseState.equalsIgnoreCase(sNextRevState))) )	
        		  DomainRelationship.setToObject(context, relId, this);
          }
          else{
        	  // Check if the rev is the highest released or greater..
        	  /*DomainObject part = new DomainObject(partId);
        	  StringList selState = new StringList(1);
        	  selState.addElement(SELECT_CURRENT);
        	  MapList revisionsMapList = part.getRevisionsInfo(context,selState, new StringList());
        	  revisionsMapList.sort(SELECT_REVISION, "descending", "string");

        	  Iterator revsMapItr = revisionsMapList.iterator();
        	  while(revsMapItr.hasNext())
        	  {
        		  Map revInfo = (Map)revsMapItr.next();
        		  state = (String) revInfo.get(SELECT_CURRENT);
        		  oid = (String) revInfo.get(SELECT_ID);

        		  if (oid.equals(partId))
        		  {
        			  //disconnect the relationship
        			  DomainRelationship.disconnect(context, relId);
        			  //connect to the new revision
        			  DomainRelationship.connect(context, part, DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT, this);
        			  break;
        		  }
        		  if (state.equals(stateRelease)){
        			  break;
        		  }
        	  }*/
        	// Check if the rev is the highest released or greater..
              String nextRevState = (String)partInfo.get("next.current");
              if(!stateRelease.equalsIgnoreCase(nextRevState))
            	  DomainRelationship.setToObject(context, relId, this);
              
          }
        }
     }
     catch (Exception ex)
     {
        throw ex;
     }
     finally
     {
         ContextUtil.popContext(context);
     }
  }

  /**
   * Checks that an ECO is connected when the object is promoted.
   * Allows the promotion of the object when the object is connected as only a Reference Document
   * (i.e., there are no 'New Specification/Specification Revision relationships' ) and
   * the flag that change management is not required if reference only is set to true.
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds following input arguments:
   *     0 - noChangeManagementIfReferenceOnly - a String holding true or false to specify
   *         if change management for Reference Documents is allowed.
   * @return int 0-success 1-failure.
   * @throws Exception if the operation fails.
   * @since EC 10.0.0.0.
   */
  public int ensureECOConnected(Context context, String[] args)
                    throws Exception
  {

    // Get the RPE variable MX_SKIP_PART_PROMOTE_CHECK, if it is not null and equal to "true"
    // it indicates that object is getting promoted because of ECO promotion to "release" state
    // in this case, no need to do the checks specified in this trigger logic, skip it.
    // In other words, when ECO gets promoted to Release state all the connected items get promoted, these can be many objects
    // this check trigger gets fired for each of these objects being promoted, which is not needed in this case.
    // This also results in performance improvment for ECO promote action

      String skipTriggerCheck = PropertyUtil.getRPEValue(context, "MX_SKIP_PART_PROMOTE_CHECK", false);
      if(skipTriggerCheck != null && "true".equals(skipTriggerCheck))
      {
      return 0;
    }

      String noChangeManagementIfReferenceOnly = args[0];

      StringList selectStmts  = new StringList(2);
      selectStmts.addElement(SELECT_ID);

      // Rel selects

      StringList selectRelStmts = new StringList(1);
      selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

String strRelPattern = RELATIONSHIP_AFFECTED_ITEM;
//strRelPattern = strRelPattern + "," +  ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM + "," + ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM;

String strTypePattern = DomainConstants.TYPE_ECO;
strTypePattern = strTypePattern + "," + ChangeConstants.TYPE_CHANGE_ACTION ;
String sObjectId = getId(context);
Map proposedCAData  = com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil.getChangeObjectsInProposed(context, selectStmts, new String[]{sObjectId}, 1);
MapList proposedchangeActionList = (MapList)proposedCAData.get(sObjectId);
Map  realizedCAData = com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil.getChangeObjectsInRealized(context, selectStmts, new String[]{sObjectId}, 1);
MapList realizedchangeActionList = (MapList)realizedCAData.get(sObjectId);

            MapList mapListECOs =
                                    getRelatedObjects(context,
                                          strRelPattern,
                                          strTypePattern, // object pattern
                                          selectStmts, // object selects
                                          selectRelStmts, // relationship selects
                                          true, // to direction
                                          false, // from direction
                                          (short) 1, // recursion level
                                          null, // object where clause
                                          null); // relationship where clause

            if (mapListECOs.size() > 0 || proposedchangeActionList.size() > 0 || realizedchangeActionList.size() > 0)
            {
                  return 0;
            }

            //Modified for IR-169021 start
           // StringList ecPartList = getInfoList(context,"to["+ RELATIONSHIP_PART_SPECIFICATION +"|from.policy.property[PolicyClassification].value ==Production].from.id");
            StringList ecPartList = getInfoList(context,"to["+ RELATIONSHIP_PART_SPECIFICATION +"|from.policy.property[PolicyClassification].value ==Production && from."+EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE+" == "+EngineeringConstants.PRODUCTION+"].from.id");
            StringList devPartList = getInfoList(context,"to["+ RELATIONSHIP_PART_SPECIFICATION +"|from.policy.property[PolicyClassification].value ==Development].from.id");

        //Change management is not required if reference only
      if ( (devPartList != null && (devPartList.size() == 0)) && (ecPartList != null && (ecPartList.size() == 0)))
      {
            if("true".equalsIgnoreCase(noChangeManagementIfReferenceOnly)){
                return 0;
            }else{
              // If this option is made false, it needs an ECO.---
              String langStr = context.getSession().getLanguage();
             // String strMessage = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CheckIfECOConnected.Message1",langStr) + " " + EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ChangeOrderChangeAction",langStr);
              String strMessage = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CheckIfChangeConnected.Message",langStr);
              emxContextUtil_mxJPO.mqlNotice(context,strMessage);
              return 1;
            }
      }
      else if(ecPartList.size() > 0)
      {
          // If this object is connected as a specification, it needs an ECO.
          String langStr = context.getSession().getLanguage();
          //String strMessage = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CheckIfECOConnected.Message1",langStr) + " " + EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ChangeOrderChangeAction",langStr) + " " + EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CheckIfECOConnected.Message2",langStr) +" "+  ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM + "," + ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM;;
          String strMessage = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CheckIfChangeConnected.Message",langStr);
          emxContextUtil_mxJPO.mqlNotice(context,strMessage);
          return 1;
      } else
    	  return 0;
    //Modified for IR-169021 end
   }

    /**
 * Displays whether the Specification is connected to an active ECR or ECO.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds a HashMap containing the following entries:
 * paramMap - a HashMap containing the following keys, "objectId", "languageStr".
 * @return Object - a String object containing "yes" if connected otherwise a String object containing "no".
 * @throws Exception if operation fails
 * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
public Object displayActiveECRorECO(Context context, String[] args)
throws Exception
  {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strSpecId = (String) paramMap.get("objectId");
        String languageStr = (String) paramMap.get("languageStr");

        String strYes=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Yes",languageStr);
        String strNo=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.No",languageStr);
        String strActiveTip = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.ActiveECRorECO",languageStr);
        String strImage = "<img src=\"../common/images/iconSmallECRO.gif\" border=\"0\" align=\"middle\" alt=\""+strActiveTip+"\">";
        String reportFormat = (String)requestMap.get("reportFormat");
        String activeECRorECO = strNo;
        // return yes if specification is connected to active ecr or eco otherwise no.
        boolean hasActiveECRECO = EngineeringUtil.hasActiveECRECO(context, strSpecId);
        if(hasActiveECRECO) {
             if (reportFormat==null || reportFormat.length()==0 || "null".equals(reportFormat))
            {
                activeECRorECO = strImage+strYes;
            }
            else
            {
                activeECRorECO = strYes;
            }
        }

        return activeECRorECO;
  }

    /**
 * Checks the view mode of the web form display.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds a HashMap containing the following entries:
 * mode - a String containing the mode.
 * @return Object - Boolean true if the mode is view otherwise false.
 * @throws Exception if operation fails
 * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
    public Object checkViewMode(Context context, String[] args)
          throws Exception
      {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String strMode = (String) programMap.get("mode");
        Boolean isViewMode = Boolean.FALSE;

        // check the mode of the web form.
        if( (strMode == null) || (strMode != null && ("null".equals(strMode) || "view".equalsIgnoreCase(strMode) || "".equals(strMode))) ) {
            isViewMode = Boolean.TRUE;
        }

      return isViewMode;
  }

    /**
 * Checks the edit mode of the web form display.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds a HashMap containing the following entries:
     * mode - a String containing the mode.
 * @return Object - Boolean true if the mode is edit otherwise false.
 * @throws Exception if operation fails
 * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
    public Object checkEditMode(Context context, String[] args)
            throws Exception
      {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String strMode = (String) programMap.get("mode");
        Boolean isEditMode = Boolean.FALSE;

        // check the mode of the web form.
        if( strMode != null && "edit".equals(strMode) ) {
            isEditMode = Boolean.TRUE;
        }

        return isEditMode;
  }

/**
 * Checks the type of the Specification.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds a HashMap containing the following entries:
     * objectId - a String holding the Specification object id.
 * @return Object - Boolean true if object contains attribute "Model Type".
 * @throws Exception if operation fails
 * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
    public Object checkModelTypeDisplay(Context context, String[] args)
              throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String strSpecId = (String) programMap.get("objectId");

        Boolean ModelTypeDisplay = Boolean.TRUE;
        setId(strSpecId);

        //Check if the object is having attribute "Model Type"

        String strAttrModelType = PropertyUtil.getSchemaProperty(context,"attribute_ModelType");
        Attribute attrModelType = getAttribute(context,strAttrModelType);

        if(attrModelType == null)
        {
            ModelTypeDisplay = Boolean.FALSE;
        }

        return ModelTypeDisplay;
  }

/**
 * Displays the Policy drop down based on the Specification type.
 * @param context the eMatrix <code>Context</code> object
     * @param args holds a HashMap containing the following entries:
     * paramMap - a HashMap containing the following keys, "objectId", "languageStr".
 * @return Object - String object which contains the policy drop down.
 * @throws Exception if operation fails
 * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
public Object getPolicy(Context context, String[] args)
          throws Exception
{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String strSpecId = (String) paramMap.get("objectId");
        String languageStr = (String) paramMap.get("languageStr");

        StringBuffer sbPolicy = new StringBuffer(64);
		sbPolicy.append("<select name=\"Policy\">");
        setId(strSpecId);
        String sCurrentPolicyName= getInfo(context,SELECT_POLICY);
        //Get the policies associated with the specification
        MapList policies = getPolicies(context);

        Iterator listItr = policies.iterator();
        Map object = null;

        String strPolicyName = "";
        String sOtherPolicyName = "";

        String sPolicySelected ="selected";

         //Construct the policy dropw down
         while (listItr.hasNext()){
            object = (Map) listItr.next();
            strPolicyName = (String) object.get(SELECT_NAME);
            sOtherPolicyName = i18nNow.getAdminI18NString("Policy", strPolicyName , languageStr);

            sbPolicy.append("<option value=\""+strPolicyName+"\" "+((strPolicyName.equals(sCurrentPolicyName))?sPolicySelected:"")+" >"+sOtherPolicyName+"</option>");
         }
         sbPolicy.append("</select>");

         String strPolicy = "";
         HashMap requestMap = (HashMap) programMap.get("requestMap");
         String strMode = (String) requestMap.get("mode");

         if("edit".equalsIgnoreCase(strMode)) {
             strPolicy = sbPolicy.toString();
         } else {
             strPolicy = i18nNow.getAdminI18NString("Policy", sCurrentPolicyName.trim() ,languageStr);
         }

      return strPolicy;
}

    /**
     * Changes the Policy of a Specification.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a HashMap containing the following entries:
     * paramMap - a HashMap containing the following keys, "objectId", "languageStr", "New Value".
     * @return Object - Boolean true if operation successful otherwise false.
     * @throws Exception if operation fails
     * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
     */
     public Object changePolicy(Context context, String[] args)
     throws Exception
     {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String strSpecId = (String) paramMap.get("objectId");

        setId(strSpecId);
        String strCurrentPolicy = getInfo(context,SELECT_POLICY);
        String strPolicy = (String) paramMap.get("New Value");

        //Fix for bug 308161
        if (strPolicy != null && strPolicy.length()!= 0 && !("null".equals(strPolicy))) {
        //end of fix 308161

        //Change policy if the current one is modified.
          if( !strCurrentPolicy.equals(strPolicy)) {
            setPolicy(context,strPolicy);
          }
        }

        return Boolean.TRUE;
     }

    /**
    * Disconnects the existing ECR and connect a new ECR.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds a HashMap containing the following entries:
    * paramMap - a HashMap containing the following keys, "objectId", "Old value", "New OID".
    * @return Object - Boolean true if operation successful otherwise false.
    * @throws Exception if operation fails
    * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
    */
    public Object connectECR(Context context, String args[]) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String strSketchId = (String) paramMap.get("objectId");

        String strOldECRName = (String) paramMap.get("Old value");
        String strNewECRId = (String) paramMap.get("New OID");
        String strRelationship = DomainRelationship.RELATIONSHIP_ECR_SUPPORTING_DOCUMENT;

        setId(strSketchId);
        java.util.List ECRList = new MapList();
        if (strOldECRName == null  || "null".equals(strOldECRName))
                strOldECRName = "";

        if (!"".equals(strOldECRName))
        {
            StringList ObjectSelectsList = new StringList(DomainConstants.SELECT_ID);
            StringList RelSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            String strProductLineType = DomainConstants.TYPE_ECR;
            StringBuffer sbWhereCondition = new StringBuffer(25);

            sbWhereCondition = sbWhereCondition.append("name==\"");
            sbWhereCondition = sbWhereCondition.append(strOldECRName);
            sbWhereCondition = sbWhereCondition.append("\"");
            String strWhereCondition = sbWhereCondition.toString();

            ECRList =
                    getRelatedObjects(
                            context,
                            strRelationship,
                            strProductLineType+","+ChangeConstants.TYPE_CHANGE_REQUEST,
                            ObjectSelectsList,
                            RelSelectsList,
                            true,
                            false,
                            (short) 1,
                            strWhereCondition,
                            DomainConstants.EMPTY_STRING);
            if (ECRList != null && !ECRList.isEmpty())
            {
                    String strRelId = (String) ((Hashtable) ECRList.get(0)).get( DomainConstants.SELECT_RELATIONSHIP_ID);
                    //Disconnecting the existing relationship
                    DomainRelationship.disconnect(context, strRelId);
            }
        }

        if (strNewECRId == null || "null".equals(strNewECRId)){
                strNewECRId = "";
        }

      if (!"".equals(strNewECRId))
        {
                setId(strNewECRId);
                DomainObject domainObjectToType = newInstance(context, strSketchId);
                DomainRelationship.connect(context,this,strRelationship,domainObjectToType);
        }

        return Boolean.TRUE;
    }

    /**
     * Checks the previous rev for connected objects that are connected with Part Specification Relationship.
     * Floats the relationship objects connected with 'Part Specification' to this revision if the connected
     * object's higher revisions are not in 'Released' state.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     *        0 - Revised object id
     *        1 - Newly created revision
     *        2 - Type of the object
     *        3 - Comma sparated type symbolic names to excuded the exection,
     *            Example: type_CADModel,type_ABC,type_XYZ
     * @return Returns 0
     * @throws Exception if the operation fails.
     * @since EC V6R2009.HFLG.
     * @trigger TypeCADModelReviseAction.
     */
    public int connectSpecToRelatedParts(Context context, String[] args)
    throws Exception
    {

        String All_REVISIONS = "revisions";

        String sOid = args[0];
        String sNewRevision = args[1];
        String sObjType = args[2];
        String sTypesToExclude = args[3];

        sObjType = FrameworkUtil.getAliasForAdmin(context, "Type", sObjType, true);

        StringList slTypesToExclude = FrameworkUtil.split(sTypesToExclude, ",");
        for(int i=0;i<slTypesToExclude.size();i++) {
            if(sObjType.equals(slTypesToExclude.get(i)))
                return 0;
        }

        StringList slObjectSle = new StringList(DomainConstants.SELECT_ID);
        slObjectSle.addElement(DomainConstants.SELECT_TYPE);
        slObjectSle.addElement(DomainConstants.SELECT_NAME);
        slObjectSle.addElement(DomainConstants.SELECT_REVISION);
        slObjectSle.addElement(DomainConstants.SELECT_CURRENT);
        slObjectSle.addElement(All_REVISIONS);

        StringList slRelSle = new StringList(DomainRelationship.SELECT_RELATIONSHIP_ID);

        Map mapCurrent = null;
        Map mapCompare = null;
        MapList mlFinalResult = new MapList();

        String sTypeCur = "";
        String sNameCur = "";
        String sRevCur = "";

        String sTypeCom = "";
        String sNameCom = "";
        String sRevCom = "";

        StringList slAllRev = new StringList();
        String sAllRev = "";
        Map mapRev = null;
        int revIndex = 0;
        DomainObject doDocObject = new DomainObject();

        try
        {
            doDocObject.setId(sOid);
            String sRelPartSpec = DomainConstants.RELATIONSHIP_PART_SPECIFICATION;

            // Get All Parts connected through 'Part Specification' relationship and their information.
            // Discard all the objects in Obsolete state within lAllObjList.
            String sObjectWhere = "(next.from["+sRelPartSpec+"].businessobject.id == "+sOid+" || previous.from["+sRelPartSpec+"].businessobject.id == "+sOid+") && !(current == Obsolete)" ;
            MapList mlPartsConnected = doDocObject.getRelatedObjects(context, sRelPartSpec, "*", slObjectSle, slRelSle, true, false, (short)1, sObjectWhere, "");
            mlPartsConnected.sort("revision", "ascending", "String");
            // Keep the later revision objects within mlPartsConnected.
            for (int i=0; i<mlPartsConnected.size(); i++) {

                mapCurrent = (Map)mlPartsConnected.get(i);
                sTypeCur = (String)mapCurrent.get(DomainConstants.SELECT_TYPE);
                sNameCur = (String)mapCurrent.get(DomainConstants.SELECT_NAME);
                sRevCur = (String)mapCurrent.get(DomainConstants.SELECT_REVISION);

                try
                {
                    slAllRev = (StringList)mapCurrent.get(All_REVISIONS);
                }
                catch (Exception ex)
                {
                    sAllRev = (String)mapCurrent.get(All_REVISIONS);

                    if (sAllRev != null)
                    {
                        slAllRev.addElement(sAllRev);
                    }
                }

                mapRev = new HashMap();

                for(int k = 0;k < slAllRev.size(); k++) {
                    mapRev.put(new Integer(k), slAllRev.get(k));
                    if(sRevCur.equals(slAllRev.get(k))) {
                        revIndex = k+1;
                    }
                }

                boolean bLaterRevFound = false;
                for(int j=i+1; j<mlPartsConnected.size(); j++) {

                    mapCompare = (Map)mlPartsConnected.get(j);
                    sTypeCom = (String)mapCompare.get(DomainConstants.SELECT_TYPE);
                    sNameCom = (String)mapCompare.get(DomainConstants.SELECT_NAME);
                    sRevCom = (String)mapCompare.get(DomainConstants.SELECT_REVISION);

                    if(sTypeCur.equals(sTypeCom) && sNameCur.equals(sNameCom) && !(sRevCom.equals(sRevCur))) {
                        for(int x = revIndex; x < slAllRev.size(); x++) {
                            if(sRevCom.equals((String)mapRev.get(new Integer(x)))) {
                                bLaterRevFound = true;
                                break;
                            }
                        }


                    }
                }

                // Discard all the objects in Release and Complete state.
                if(!bLaterRevFound) {
                    if(!(DomainConstants.STATE_PART_RELEASE.equals((String)mapCurrent.get(DomainConstants.SELECT_CURRENT))) && !(DomainConstants.STATE_DEVELOPMENT_PART_COMPLETE.equals((String)mapCurrent.get(DomainConstants.SELECT_CURRENT)))) {
                        mlFinalResult.add(mapCurrent);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }

        // Move connection to Later revision of Drawing.
        try {
            ContextUtil.pushContext(context);
            String sRelId = "";
            String sNewRevId = "";
            for(int i=0;i<mlFinalResult.size();i++) {
                mapCurrent = (Map)mlFinalResult.get(i);
                sRelId = (String)mapCurrent.get(DomainRelationship.SELECT_RELATIONSHIP_ID);
                sNewRevId = MqlUtil.mqlCommand(context, "print bus $1 $2 $3 select $4 dump",doDocObject.getInfo(context, DomainConstants.SELECT_TYPE),doDocObject.getInfo(context, DomainConstants.SELECT_NAME),sNewRevision,"id");
                DomainRelationship.setToObject(context, sRelId, new DomainObject(sNewRevId));
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            ContextUtil.popContext(context);
        }
        return 0;
    }

	/**
     * Check that at other revisions of this object do not exist.
     * If one does, fail with a message telling the user to use
     * "OBJECT | NEW | REVISION" instead of "OBJECT | NEW | ORIGINAL.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     *        0 - Type of the object trying to create
     *        1 - Name of the object trying to create
     *        2 - Revision of the object trying to create
     *        2 - Comma sparated type symbolic names to excuded the exection,
     *            Example: type_CADModel,type_ABC,type_XYZ
     * @return If filure returns 1
     * @throws Exception if the operation fails.
     * @since EC V6R2009.HFLG.
     * @trigger TypeCADModelReviseAction.
     */
	public int specEnsureNoRevisionsExist(Context context, String[] args)
    throws Exception
    {
        String sGlobalEnvCheckMCADInteg = MqlUtil.mqlCommand(context,"get env global $1",true,"MCADINTEGRATION_CONTEXT");

        //if sGlobalEnvCheckMCADInteg == true, We are in the MCAD Integration context, so exit gracefully
        //go ahead only if the global env varaible 'MCADINTEGRATION_CONTEXT' is not set

        if(sGlobalEnvCheckMCADInteg !=null  && !"null".equals(sGlobalEnvCheckMCADInteg) && sGlobalEnvCheckMCADInteg.indexOf("true") < 0){
            String sExpand = args[0];
            String sExpandFromSpecificLevel = args[1];
            String sType = args[2];
            String sTypesToExclude = args[3];
            String sName = args[4];
            String sRevision = args[5];
            String sPolicy = args[6];

            String sCmd = "";
            String sMqlRes = "";
            String sQueryRev = "*";
            String sPolicyClass = "";

            String displayErrorMes = ""; // Added to fix IR-073078V6R2012.

            String sObjType = FrameworkUtil.getAliasForAdmin(context, "Type", sType, true);

            StringList slTypesToExclude = FrameworkUtil.split(sTypesToExclude, ",");
            for(int i=0;i<slTypesToExclude.size();i++) {
                if(sObjType.equals(slTypesToExclude.get(i)))
                    return 0;
            }

            if(sExpand.equalsIgnoreCase("EXPAND_FROM_SPECIFIC_LEVEL")) {
                if(!"".equals(sExpandFromSpecificLevel)) {
                    String sExpandFromSpecificLevelOrg = PropertyUtil.getSchemaProperty(context, sExpandFromSpecificLevel);
                        if(!"".equals(sExpandFromSpecificLevelOrg)) {
                            sType = sExpandFromSpecificLevelOrg;
                            sCmd = "temp query bus " + "$1 $2 $3 dump $4";
                        }
                }
            } else if(sExpand.equalsIgnoreCase("DO_NOT_EXPAND")) {
                sCmd  = "temp query !expand bus " + "$1 $2 $3 dump $4";
            } else if(sExpand.equalsIgnoreCase("EXPAND_FROM_SAME_LEVEL")) {
                sCmd = "temp query bus " + "$1 $2 $3 dump $4";
            } else {
                if(!"".equals(sPolicy)){

					sPolicyClass = PropertyUtil.getSchemaProperty(context, "policy", sPolicy, "PolicyClassification");

                    if("Equivalent".equals(sPolicyClass)) {
                        sQueryRev = sRevision;
                         sCmd = "temp query bus " + "$1 $2 $3 dump $4";
                  } else {

                    	StringList objectSelects = new StringList(1);
                    	objectSelects.addElement(DomainConstants.SELECT_ID);


                    	MapList mlLst  = (MapList)findObjects(context,
                    										sType,
                    										sName,
                    										sQueryRev,
                    										"*",
                    										"*",
                    										null,
                    										false,
                    										objectSelects) ;
                    if(mlLst.size() > 0 ) {
                        displayErrorMes = "Display"; // Added to fix IR-073078V6R2012.
                    }
                 }
              }
           }

            if ("".equals(sCmd)) { // Added to fix IR-073078V6R2012.
            	sMqlRes = displayErrorMes;
            } else {
            	sMqlRes = MqlUtil.mqlCommand(context, sCmd, sType, sName, sQueryRev,"|");
            }

            // if mlOtherRev is populated with value,it indicates there are objects of same type of different revisions are existing
            // display the error and block the event
            if(!"".equals(sMqlRes)) {
                String [] mailArguments = new String [8];
                if("Equivalent".equals(sPolicyClass)) {
                    mailArguments[0] = "emxEngineeringCentral.MEP.TNRExists";
                    mailArguments[1] = "0";
                    mailArguments[2] = "";
                    mailArguments[3] = "emxEngineeringCentralStringResource";
                } else {
                    mailArguments[0] = "emxFramework.ProgramObject.eServiceValidRevisionChange_if.NoCreate";
                    mailArguments[1] = "3";
                    mailArguments[2] = "Type";
                    mailArguments[3] = sType;
                    mailArguments[4] = "Name";
                    mailArguments[5] = sName;
                    mailArguments[6] = "Rev";
                    mailArguments[7] = sRevision;
                }
                String strMessage =  emxMailUtil_mxJPO.getMessage(context,mailArguments);
                emxContextUtil_mxJPO.mqlNotice(context,strMessage);
                return 1;
            }
        }
        return 0;
    }

	/**
	 * To create the part object from create component
	 *
	 * @param context
	 * @param args
	 * @return Map
	 * @throws Exception
	 * @Since R211
	 */
	@com.matrixone.apps.framework.ui.CreateProcessCallable
	public Map reviseSpecJPO(Context context, String[] args) throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);

		String strPartId = (String) programMap.get("copyObjectId");
		String sCustomRevisionLevel = (String) programMap.get("CustomRevisionLevel");
		String sVault = (String) programMap.get("lastRevVault");
		String copyFiles = (String) programMap.get("CopyFiles");
		boolean copyFilesBol = "on".equalsIgnoreCase(copyFiles);

		Map returnMap = new HashMap();

		PartDefinition spec = new PartDefinition(strPartId);
		DomainObject nextRev = new DomainObject(spec.reviseSpec(context, sCustomRevisionLevel, sVault, copyFilesBol));
		returnMap.put("id", nextRev.getId(context));

		return returnMap;
	}

	/**
	 * Method to display checkbox to select copy files option
	 * @param context
	 * @param args
	 * @return String
	 * @throws Exception
	 */
	public String displayCopyFilesCheckBox(Context context, String[] args) throws Exception {
		return "<input type=\"checkbox\" name=\"CopyFiles\"/> " + EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Yes", context.getSession().getLanguage());
	}
	/**
	 * Added for JSP to Common components conversion. Specification ->Related ECRs/ECOs
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	   @com.matrixone.apps.framework.ui.ProgramCallable
	   public MapList getSpecRelatedECRs(Context context, String[] args)   throws Exception
	   {
		   boolean isUserHasModifyAccess = false;
		   String typePattern = PropertyUtil.getSchemaProperty(context,"type_ECR");
		   MapList objMapList = getSpecRelatedChanges(context, args, typePattern);
		   Person ctxPerson = Person.getPerson(context, context.getUser());
			if(ctxPerson.hasRole(context, sDgnEngr)||ctxPerson.hasRole(context, sSrDgnEngr)||ctxPerson.hasRole(context, sMfgEngr)||ctxPerson.hasRole(context, sSrMfgEngr)||ctxPerson.hasRole(context, sECREvtr)||ctxPerson.hasRole(context, sECRCdtr)||ctxPerson.hasRole(context, sECRChrm))
			 {
				  String initargs[] = {};
				  emxENCActionLinkAccess_mxJPO engAccessLink = new emxENCActionLinkAccess_mxJPO(context, initargs);
				  MapList tempMapLst = new MapList();
				  tempMapLst.addAll(objMapList) ;
				  objMapList = new MapList();
				  Map tmpMap = null ;
				  HashMap requestMap = null ;
				  for(int i = 0 ; i<tempMapLst.size();i++){
					  requestMap = new HashMap();
					  tmpMap = (Map)tempMapLst.get(i) ;
					  requestMap.put("objectId",(String)(tmpMap.get(DomainConstants.SELECT_ID)));
					  isUserHasModifyAccess  = engAccessLink.showEditAllAffectedItemsLink(context,JPO.packArgs (requestMap));
					  if(!isUserHasModifyAccess){
						  tmpMap.put("RowEditable", "readonly");
					  }
					  objMapList.add(tmpMap);
				  }
			  }

	   	return objMapList ;
	   }

	   /**
	    *
	    * @param context
	    * @param args
	    * @param typePattern
	    * @return
	    * @throws Exception
	    */
	   public MapList getSpecRelatedChanges(Context context, String[] args, String typePattern)   throws Exception
	   {
		    HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strObjId = (String) programMap.get("objectId");

			DomainObject specObj = new DomainObject(strObjId);

			String sDescofchange = PropertyUtil.getSchemaProperty (context, "attribute_SpecificDescriptionofChange");
            String relPattern = PropertyUtil.getSchemaProperty(context,"relationship_AffectedItem");

					SelectList selectRelStmts = new SelectList(3);
					selectRelStmts.addElement(DomainRelationship.SELECT_ID);
					selectRelStmts.addElement(DomainRelationship.SELECT_NAME);
					selectRelStmts.addAttribute(sDescofchange);

					SelectList selectStmts = new SelectList(11);
					selectStmts.addElement(DomainObject.SELECT_ID);
					selectStmts.addElement(DomainObject.SELECT_POLICY);
					selectStmts.addType();
					selectStmts.addName();
					selectStmts.addDescription();
					selectStmts.addRevision();
					selectStmts.addElement("current");

					if(typePattern.equals(PropertyUtil.getSchemaProperty(context,"type_ECR")))
					{
						String sRAESelect = "from[" + EngineeringConstants.RELATIONSHIP_RAISED_AGAINST_ECR + "]";

						selectStmts.addElement(sRAESelect+ ".id");
						selectStmts.addElement(sRAESelect+ ".name");
						selectStmts.addElement(sRAESelect+ ".to.name");
						selectStmts.addElement(sRAESelect+ ".to.type");
					}
					MapList objMapList = null;
					try{
						ContextUtil.startTransaction(context,false);

						 objMapList =FrameworkUtil.toMapList(specObj.getExpansionIterator(context, relPattern, typePattern,
						          selectStmts, selectRelStmts, true, true, (short)1,
						           null, null, (short)0,
						           false, false, (short)1, false),
						           (short)0, null, null, null, null);

					}catch(Exception exp){
						ContextUtil.abortTransaction(context);
					}
					finally{
						ContextUtil.commitTransaction(context);
					}
					return objMapList;

	   }
          /**
           * Added for JSP to Common components conversion. Specification ->Related ECRs/ECOs
	   * @param context
	   * @param args
	   * @return
	   * @throws Exception
	   */
	   @com.matrixone.apps.framework.ui.ProgramCallable
	   public MapList getSpecRelatedECOs(Context context, String[] args)   throws Exception
	   {
			String typePattern = PropertyUtil.getSchemaProperty(context,"type_ECO");
			MapList objMapList = getSpecRelatedChanges(context, args, typePattern);
		    Person ctxPerson = Person.getPerson(context, context.getUser());
			  if( ! (ctxPerson.hasRole(context, sDgnEngr)&& ctxPerson.hasRole(context, sSrDgnEngr)&& ctxPerson.hasRole(context, sMfgEngr)&&ctxPerson.hasRole(context, sSrMfgEngr)&&ctxPerson.hasRole(context, sECREvtr)&&ctxPerson.hasRole(context, sECRCdtr)&&ctxPerson.hasRole(context, sECRChrm)))
			  {
				  MapList tempMapLst = new MapList();
				  tempMapLst.addAll(objMapList) ;
				  objMapList = new MapList();
				  Map tmpMap = null ;

				  for(int i = 0 ; i<tempMapLst.size();i++){
					  tmpMap = (Map)tempMapLst.get(i) ;
					  tmpMap.put("RowEditable", "readonly");
					  objMapList.add(tmpMap);
				  }
			  }

	   	return objMapList ;
	   }

	  /**
	   * Added for JSP to Common components conversion. Specification ->Related ECRs/ECOs
	   * @param context
	   * @param args
	   * @return
	   * @throws Exception
	   */
	   public Vector getECOFile(Context context, String[] args) throws Exception {
		    HashMap programMap = (HashMap)JPO.unpackArgs(args);
		    MapList objList = (MapList)programMap.get("objectList");

		    Vector columnVals = new Vector(objList.size());

		    String fileURL = "../components/emxComponentsCheckout.jsp?" ;
	   	    String defaultFormat;
			String fileName;
			String id;

			StringBuffer sbBuffer;

		    Iterator iterator = objList.iterator();

		    DomainObject domObj;

		    FileList fileNameList;
		    matrix.db.File defaultFile;

		    while (iterator.hasNext()) {
		    	sbBuffer = new StringBuffer(100);

		        id = (String) ((Map) iterator.next()).get(DomainConstants.SELECT_ID);

		        domObj = DomainObject.newInstance(context, id);

			    defaultFormat = domObj.getDefaultFormat(context);
				fileNameList  = domObj.getFiles(context, defaultFormat);

				if (fileNameList.size() > 0) {
					defaultFile = (matrix.db.File)fileNameList.elementAt(0);
					fileName = defaultFile.getName();

				fileURL = fileURL + "objectId=" + id + "&amp;action=download" + "&amp;format=" + defaultFormat +  "&amp;fileName=" + fileName;
					sbBuffer.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");

					sbBuffer.append("<a href=\"javascript:showModalDialog('").append(fileURL).append('\'');
					sbBuffer.append(", '700', '600')\">");

					sbBuffer.append("<img src='");
					sbBuffer.append("../common/images/iconSmallECO.gif'");
					sbBuffer.append(" alt='");
					sbBuffer.append(fileName);
					sbBuffer.append('\'');
					sbBuffer.append("></img>");

					sbBuffer.append("</a>");
				} else {
					sbBuffer.append("<img src=\"../common/images/iconSmallECO.gif\"/>");
				}

				columnVals.add(sbBuffer.toString());
		    }

		    return columnVals ;
	   }
}

